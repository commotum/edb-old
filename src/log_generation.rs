//! Canonical lineage transaction content and small physical-generation links.
//!
//! Generation zero keeps the exact alias-bound format-3 transaction bytes.
//! New writes store one lineage-bound `ATLC` content value without a mutable
//! alias, physical generation, predecessor hash, or caller-chosen tempid
//! names. A generation contains small membership rows which bind these shared
//! values into an ordered chain. Excision therefore creates content only for
//! transactions whose datoms changed; later predecessor changes do not force
//! an otherwise identical payload to be copied.

use crate::encoding::{
    canonical_datom_bytes, decode_canonical_datoms, validate_transaction_content,
};
use crate::reserved_allocation::ReservedAllocation;
use crate::{
    Datom, Digest, DurableTransaction, ErrorCategory, INITIAL_EIDX_FRONTIER, IndexOrder, MAX_EIDX,
    SemanticError, eid_to_eidx, eid_to_part, sha256,
};
use std::collections::{BTreeMap, BTreeSet};

const MAGIC: &[u8; 4] = b"ATLC";
const LEGACY_VERSION: u16 = 1;
const RESERVED_ALLOCATION_VERSION: u16 = 2;
const KIND_TRANSACTION_CONTENT: u8 = 1;
const HEADER_LEN: usize = 16;
const CHECKSUM_LEN: usize = 32;
const MAX_LINEAGE_BYTES: usize = 64;
// The existing transaction codec accepts a 64 MiB body. The small allowance
// covers the lineage and collection headers while avoiding a new lower limit.
const MAX_BODY_BYTES: usize = 64 * 1024 * 1024 + 1024;
pub(crate) const MAX_ENVELOPE_BYTES: usize = HEADER_LEN + MAX_BODY_BYTES + CHECKSUM_LEN;
const MEMBERSHIP_DOMAIN: &[u8] = b"atomic/generation-membership/v1\0";

/// Immutable logical content shared by every physical generation that carries
/// the same transaction information.
#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct LineageTransactionContent {
    pub(crate) lineage_id: String,
    pub(crate) basis_t: u64,
    pub(crate) eidx_frontier: u64,
    /// Explicit format selection: None preserves ATLC v1 exactly. ATLC v2
    /// authenticates retained reserved issuance even when physical excision
    /// removes every datum that originally witnessed a reference-only ID.
    pub(crate) reserved_frontier: Option<u64>,
    /// Distinct newly issued non-transaction entity ids. Names are absent.
    pub(crate) allocations: Vec<u64>,
    pub(crate) tx_data: Vec<Datom>,
}

impl LineageTransactionContent {
    /// Convert an authenticated source transaction while retaining only the
    /// numeric witnesses needed to prove frontier advancement.
    pub(crate) fn from_transaction(
        lineage_id: &str,
        prior_eidx_frontier: u64,
        transaction: &DurableTransaction,
    ) -> Result<Self, SemanticError> {
        let mut allocations = BTreeSet::new();
        for entity in transaction.tempids.values().copied() {
            let entity_index = eid_to_eidx(entity).map_err(|error| {
                fault(
                    "generation/invalid-allocation",
                    format!("transaction allocation is invalid: {error}"),
                )
            })?;
            if entity_index >= prior_eidx_frontier && eid_to_part(entity)? != crate::TX_PARTITION {
                allocations.insert(entity);
            }
        }
        let content = Self {
            lineage_id: lineage_id.to_owned(),
            basis_t: transaction.basis_t,
            eidx_frontier: transaction.eidx_frontier,
            reserved_frontier: None,
            allocations: allocations.into_iter().collect(),
            tx_data: transaction.tx_data.clone(),
        };
        content.validate()?;
        Ok(content)
    }

    /// New-format conversion is deliberately separate from the legacy
    /// constructor. Merely upgrading the binary must never change a v1
    /// payload's identity while copying or verifying older durable content.
    pub(crate) fn from_transaction_v2(
        lineage_id: &str,
        prior_eidx_frontier: u64,
        prior_reserved: ReservedAllocation,
        after_reserved: ReservedAllocation,
        transaction: &DurableTransaction,
    ) -> Result<Self, SemanticError> {
        ReservedAllocation::from_frontier(prior_reserved.frontier(), prior_eidx_frontier)?;
        let mut allocations = BTreeSet::new();
        for entity in transaction.tempids.values().copied() {
            let partition = eid_to_part(entity)?;
            let index = eid_to_eidx(entity)?;
            let fresh = if partition == crate::DB_PARTITION {
                index >= prior_reserved.frontier()
            } else {
                partition != crate::TX_PARTITION && index >= prior_eidx_frontier
            };
            if fresh {
                allocations.insert(entity);
            }
        }
        let content = Self {
            lineage_id: lineage_id.to_owned(),
            basis_t: transaction.basis_t,
            eidx_frontier: transaction.eidx_frontier,
            reserved_frontier: Some(after_reserved.frontier()),
            allocations: allocations.into_iter().collect(),
            tx_data: transaction.tx_data.clone(),
        };
        content.validate_reserved_transition(prior_reserved)?;
        Ok(content)
    }

    pub(crate) fn version(&self) -> u16 {
        if self.reserved_frontier.is_some() {
            RESERVED_ALLOCATION_VERSION
        } else {
            LEGACY_VERSION
        }
    }

    /// Check the reserved side of a v2 transition against its authenticated
    /// predecessor. Ordinary contiguous issuance remains the replay caller's
    /// responsibility and must exclude partition-zero witnesses in v2.
    ///
    /// The checkpoint may be higher than the surviving witnesses: input-only
    /// explicit references and physical excision legitimately retain such a
    /// high-water mark. It may never regress or omit a visible claim.
    pub(crate) fn validate_reserved_transition(
        &self,
        prior: ReservedAllocation,
    ) -> Result<ReservedAllocation, SemanticError> {
        self.validate()?;
        let frontier = self.reserved_frontier.ok_or_else(|| {
            incorrect(
                "generation/reserved-allocation-version",
                "ATLC v1 has no authenticated reserved allocation checkpoint",
            )
        })?;
        let after = ReservedAllocation::from_frontier(frontier, self.eidx_frontier)?;
        if frontier < prior.frontier() {
            return Err(incorrect(
                "generation/reserved-frontier-regression",
                "reserved allocation checkpoint regresses from its predecessor",
            ));
        }
        prior.validate_fresh_witnesses(self.allocations.iter().copied())?;
        Ok(after)
    }

    /// Reconstruct the kernel recovery record. Synthetic names exist only to
    /// reuse the strict allocation/frontier verifier; they are canonical and
    /// contain no caller data.
    pub(crate) fn to_transaction(&self, previous_hash: Digest) -> DurableTransaction {
        DurableTransaction {
            database_id: self.lineage_id.clone(),
            basis_t: self.basis_t,
            previous_hash,
            eidx_frontier: self.eidx_frontier,
            tempids: self
                .allocations
                .iter()
                .copied()
                .enumerate()
                .map(|(ordinal, entity)| (format!("allocation-{ordinal}"), entity))
                .collect::<BTreeMap<_, _>>(),
            tx_data: self.tx_data.clone(),
        }
    }

    pub(crate) fn encode(&self) -> Result<Vec<u8>, SemanticError> {
        self.validate()?;
        let lineage = self.lineage_id.as_bytes();
        let lineage_len = u16::try_from(lineage.len()).map_err(|_| {
            incorrect(
                "generation/lineage-length",
                "database lineage exceeds the canonical content limit",
            )
        })?;
        let allocation_count = u32::try_from(self.allocations.len()).map_err(|_| {
            incorrect(
                "generation/allocation-count",
                "transaction allocation count exceeds u32",
            )
        })?;
        let mut datoms = self
            .tx_data
            .iter()
            .map(|datom| Ok((datom, canonical_datom_bytes(datom)?)))
            .collect::<Result<Vec<_>, SemanticError>>()?;
        datoms.sort_by(|(left, left_bytes), (right, right_bytes)| {
            left.cmp_in(right, IndexOrder::Eavt)
                .then_with(|| left_bytes.cmp(right_bytes))
        });
        let datom_count = u32::try_from(datoms.len()).map_err(|_| {
            incorrect(
                "generation/datom-count",
                "transaction datom count exceeds u32",
            )
        })?;

        let datom_bytes = datoms
            .iter()
            .try_fold(0_usize, |length, (_, bytes)| {
                length.checked_add(bytes.len())
            })
            .ok_or_else(|| {
                incorrect(
                    "generation/content-length",
                    "transaction content length overflows usize",
                )
            })?;
        let allocation_bytes = self.allocations.len().checked_mul(8).ok_or_else(|| {
            incorrect(
                "generation/content-length",
                "transaction allocation length overflows usize",
            )
        })?;
        let body_len = 2_usize
            .checked_add(lineage.len())
            .and_then(|length| length.checked_add(8 + 8 + 4))
            .and_then(|length| {
                length.checked_add(if self.reserved_frontier.is_some() {
                    8
                } else {
                    0
                })
            })
            .and_then(|length| length.checked_add(allocation_bytes))
            .and_then(|length| length.checked_add(4))
            .and_then(|length| length.checked_add(datom_bytes))
            .ok_or_else(|| {
                incorrect(
                    "generation/content-length",
                    "transaction content length overflows usize",
                )
            })?;
        if body_len > MAX_BODY_BYTES {
            return Err(incorrect(
                "generation/content-length",
                format!("transaction content exceeds {MAX_BODY_BYTES} bytes"),
            ));
        }

        let mut bytes = Vec::with_capacity(HEADER_LEN + body_len + CHECKSUM_LEN);
        bytes.extend_from_slice(MAGIC);
        bytes.extend_from_slice(&self.version().to_be_bytes());
        bytes.push(KIND_TRANSACTION_CONTENT);
        bytes.push(0);
        bytes.extend_from_slice(&(body_len as u64).to_be_bytes());
        bytes.extend_from_slice(&lineage_len.to_be_bytes());
        bytes.extend_from_slice(lineage);
        bytes.extend_from_slice(&self.basis_t.to_be_bytes());
        bytes.extend_from_slice(&self.eidx_frontier.to_be_bytes());
        if let Some(frontier) = self.reserved_frontier {
            bytes.extend_from_slice(&frontier.to_be_bytes());
        }
        bytes.extend_from_slice(&allocation_count.to_be_bytes());
        for entity in &self.allocations {
            bytes.extend_from_slice(&entity.to_be_bytes());
        }
        bytes.extend_from_slice(&datom_count.to_be_bytes());
        for (_, encoded) in datoms {
            bytes.extend_from_slice(&encoded);
        }
        let checksum = sha256(&bytes);
        bytes.extend_from_slice(&checksum);
        Ok(bytes)
    }

    pub(crate) fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        if bytes.len() < HEADER_LEN + 2 + 8 + 8 + 4 + 4 + CHECKSUM_LEN
            || bytes.len() > MAX_ENVELOPE_BYTES
        {
            return Err(fault(
                "generation/content-length",
                "transaction content has an invalid encoded size",
            ));
        }
        if bytes.get(..4) != Some(MAGIC.as_slice()) {
            return Err(fault(
                "generation/content-magic",
                "transaction content has the wrong magic",
            ));
        }
        let version = u16::from_be_bytes(bytes[4..6].try_into().expect("checked header"));
        if !matches!(version, LEGACY_VERSION | RESERVED_ALLOCATION_VERSION) {
            return Err(SemanticError::new(
                ErrorCategory::Unsupported,
                "generation/content-version",
                format!("transaction content version {version} is unsupported"),
            ));
        }
        if bytes[6] != KIND_TRANSACTION_CONTENT || bytes[7] != 0 {
            return Err(fault(
                "generation/content-header",
                "transaction content kind or reserved byte is invalid",
            ));
        }
        let body_len = usize::try_from(u64::from_be_bytes(
            bytes[8..16].try_into().expect("checked header"),
        ))
        .map_err(|_| fault("generation/content-length", "content length exceeds usize"))?;
        if body_len > MAX_BODY_BYTES
            || HEADER_LEN
                .checked_add(body_len)
                .and_then(|length| length.checked_add(CHECKSUM_LEN))
                != Some(bytes.len())
        {
            return Err(fault(
                "generation/content-length",
                "transaction content length does not match its envelope",
            ));
        }
        let checksum_at = HEADER_LEN + body_len;
        if sha256(&bytes[..checksum_at]).as_slice() != &bytes[checksum_at..] {
            return Err(fault(
                "generation/content-checksum",
                "transaction content checksum is invalid",
            ));
        }

        let body = &bytes[HEADER_LEN..checksum_at];
        let mut cursor = 0_usize;
        let lineage_len = read_u16(body, &mut cursor)? as usize;
        if lineage_len > MAX_LINEAGE_BYTES {
            return Err(fault(
                "generation/lineage-length",
                "database lineage exceeds the canonical content limit",
            ));
        }
        let lineage_id = read_string(body, &mut cursor, lineage_len)?;
        let basis_t = read_u64(body, &mut cursor)?;
        let eidx_frontier = read_u64(body, &mut cursor)?;
        let reserved_frontier = if version == RESERVED_ALLOCATION_VERSION {
            Some(read_u64(body, &mut cursor)?)
        } else {
            None
        };
        let allocation_count = read_u32(body, &mut cursor)? as usize;
        let remaining = body.len().checked_sub(cursor).ok_or_else(|| {
            fault(
                "generation/content-length",
                "transaction content cursor exceeds its body",
            )
        })?;
        if remaining < 4 || allocation_count > (remaining - 4) / 8 {
            return Err(fault(
                "generation/allocation-count",
                "transaction allocation count cannot fit in the remaining content bytes",
            ));
        }
        let mut allocations = Vec::with_capacity(allocation_count);
        for _ in 0..allocation_count {
            allocations.push(read_u64(body, &mut cursor)?);
        }
        let datom_count = read_u32(body, &mut cursor)? as usize;
        let tx_data = decode_canonical_datoms(&body[cursor..], datom_count)?;
        let content = Self {
            lineage_id,
            basis_t,
            eidx_frontier,
            reserved_frontier,
            allocations,
            tx_data,
        };
        content.validate().map_err(|error| {
            fault(
                "generation/invalid-content",
                format!("persisted transaction content failed validation: {error}"),
            )
        })?;
        if content.encode()? != bytes {
            return Err(fault(
                "generation/noncanonical-content",
                "transaction content does not have one canonical encoding",
            ));
        }
        Ok(content)
    }

    #[cfg(test)]
    pub(crate) fn hash(&self) -> Result<Digest, SemanticError> {
        Ok(sha256(&self.encode()?))
    }

    fn validate(&self) -> Result<(), SemanticError> {
        if !is_canonical_lineage(&self.lineage_id) {
            return Err(incorrect(
                "generation/lineage",
                "database lineage must be a lowercase RFC 4122 version-4 UUID",
            ));
        }
        if self.basis_t == 0 {
            return Err(incorrect(
                "generation/content-basis",
                "transaction content basis must be positive",
            ));
        }
        if !(INITIAL_EIDX_FRONTIER..=MAX_EIDX + 1).contains(&self.eidx_frontier) {
            return Err(incorrect(
                "generation/content-frontier",
                "transaction content has an invalid issued frontier",
            ));
        }
        let mut prior = None;
        for entity in &self.allocations {
            if prior.is_some_and(|prior| prior >= *entity)
                || eid_to_part(*entity).is_err()
                || eid_to_part(*entity).ok() == Some(crate::TX_PARTITION)
                || eid_to_eidx(*entity).is_err()
                || eid_to_eidx(*entity).is_ok_and(|index| index >= self.eidx_frontier)
            {
                return Err(incorrect(
                    "generation/content-allocations",
                    "allocation witnesses must be sorted distinct issued non-transaction entities",
                ));
            }
            prior = Some(*entity);
        }

        // Reuse the narrow kernel validator for datom/value, transaction-id,
        // and frontier-local invariants without allocating a synthetic map.
        // Exact frontier advancement is checked against db-before on replay.
        validate_transaction_content(self.basis_t, self.eidx_frontier, &self.tx_data)?;
        if let Some(frontier) = self.reserved_frontier {
            ReservedAllocation::from_frontier(frontier, self.eidx_frontier)?;
            let mut observed = ReservedAllocation::initial();
            observed.observe_datoms(&self.tx_data)?;
            for entity in &self.allocations {
                observed.observe_entity(*entity)?;
            }
            if observed.frontier() > frontier {
                return Err(incorrect(
                    "generation/reserved-frontier-missing-claim",
                    "reserved allocation checkpoint omits a datom or allocation witness",
                ));
            }
        }
        Ok(())
    }
}

/// Hash a small physical membership row. Content identity remains stable
/// across generations; this commitment binds its placement and state.
#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct GenerationTransactionMembership {
    pub(crate) lineage_id: String,
    pub(crate) generation: u64,
    pub(crate) basis_t: u64,
    pub(crate) previous_hash: Digest,
    pub(crate) content_hash: Digest,
    pub(crate) state_hash: Digest,
    pub(crate) eidx_frontier: u64,
}

impl GenerationTransactionMembership {
    pub(crate) fn encode(&self) -> Result<Vec<u8>, SemanticError> {
        encode_generation_transaction_membership(
            &self.lineage_id,
            self.generation,
            self.basis_t,
            self.previous_hash,
            self.content_hash,
            self.state_hash,
            self.eidx_frontier,
        )
    }

    pub(crate) fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        let expected_len = MEMBERSHIP_DOMAIN.len() + 36 + 8 + 8 + 32 + 32 + 32 + 8;
        if bytes.len() != expected_len || !bytes.starts_with(MEMBERSHIP_DOMAIN) {
            return Err(fault(
                "generation/membership-encoding",
                "generation membership has an invalid canonical encoding",
            ));
        }
        let mut cursor = MEMBERSHIP_DOMAIN.len();
        let lineage_id = std::str::from_utf8(take(bytes, &mut cursor, 36)?)
            .map_err(|_| {
                fault(
                    "generation/membership-lineage",
                    "membership lineage is not UTF-8",
                )
            })?
            .to_owned();
        let generation = read_u64(bytes, &mut cursor)?;
        let basis_t = read_u64(bytes, &mut cursor)?;
        let previous_hash = take(bytes, &mut cursor, 32)?
            .try_into()
            .expect("checked digest length");
        let content_hash = take(bytes, &mut cursor, 32)?
            .try_into()
            .expect("checked digest length");
        let state_hash = take(bytes, &mut cursor, 32)?
            .try_into()
            .expect("checked digest length");
        let eidx_frontier = read_u64(bytes, &mut cursor)?;
        let membership = Self {
            lineage_id,
            generation,
            basis_t,
            previous_hash,
            content_hash,
            state_hash,
            eidx_frontier,
        };
        if membership.encode()?.as_slice() != bytes {
            return Err(fault(
                "generation/membership-encoding",
                "generation membership is not canonical",
            ));
        }
        Ok(membership)
    }
}

/// Canonical portable membership object. Its SHA-256 is exactly the live
/// generation transaction hash, allowing backup to preserve one object
/// representation instead of reconstructing an internal hash preimage.
pub(crate) fn encode_generation_transaction_membership(
    lineage_id: &str,
    generation: u64,
    basis_t: u64,
    previous_hash: Digest,
    content_hash: Digest,
    state_hash: Digest,
    eidx_frontier: u64,
) -> Result<Vec<u8>, SemanticError> {
    if !is_canonical_lineage(lineage_id)
        || generation == 0
        || basis_t == 0
        || content_hash == [0; 32]
        || state_hash == [0; 32]
        || !(INITIAL_EIDX_FRONTIER..=MAX_EIDX + 1).contains(&eidx_frontier)
    {
        return Err(incorrect(
            "generation/membership-coordinate",
            "generation membership requires valid lineage, generation, basis, content, state, and frontier",
        ));
    }
    let mut bytes = Vec::with_capacity(MEMBERSHIP_DOMAIN.len() + 156);
    bytes.extend_from_slice(MEMBERSHIP_DOMAIN);
    bytes.extend_from_slice(lineage_id.as_bytes());
    bytes.extend_from_slice(&generation.to_be_bytes());
    bytes.extend_from_slice(&basis_t.to_be_bytes());
    bytes.extend_from_slice(&previous_hash);
    bytes.extend_from_slice(&content_hash);
    bytes.extend_from_slice(&state_hash);
    bytes.extend_from_slice(&eidx_frontier.to_be_bytes());
    Ok(bytes)
}

pub(crate) fn generation_transaction_hash(
    lineage_id: &str,
    generation: u64,
    basis_t: u64,
    previous_hash: Digest,
    content_hash: Digest,
    state_hash: Digest,
    eidx_frontier: u64,
) -> Result<Digest, SemanticError> {
    Ok(sha256(&encode_generation_transaction_membership(
        lineage_id,
        generation,
        basis_t,
        previous_hash,
        content_hash,
        state_hash,
        eidx_frontier,
    )?))
}

pub(crate) fn is_canonical_lineage(value: &str) -> bool {
    let bytes = value.as_bytes();
    bytes.len() == 36
        && [8, 13, 18, 23]
            .into_iter()
            .all(|index| bytes[index] == b'-')
        && bytes[14] == b'4'
        && matches!(bytes[19], b'8' | b'9' | b'a' | b'b')
        && bytes.iter().enumerate().all(|(index, byte)| {
            [8, 13, 18, 23].contains(&index) || byte.is_ascii_digit() || matches!(byte, b'a'..=b'f')
        })
}

/// Stable lookup key for idempotency records in lineage-bound generations.
/// Caller-chosen request keys can contain business identifiers, so only this
/// domain-separated digest crosses the durable generation boundary.
pub(crate) fn request_key_hash(
    lineage_id: &str,
    request_key: &str,
) -> Result<Digest, SemanticError> {
    if !is_canonical_lineage(lineage_id) {
        return Err(incorrect(
            "generation/lineage",
            "database lineage must be a lowercase RFC 4122 version-4 UUID",
        ));
    }
    if request_key.is_empty() {
        return Err(incorrect(
            "generation/empty-request-key",
            "idempotency request key cannot be empty",
        ));
    }
    let key_length = u64::try_from(request_key.len()).map_err(|_| {
        incorrect(
            "generation/request-key-length",
            "idempotency request key length exceeds u64",
        )
    })?;
    let mut bytes = Vec::with_capacity(44 + request_key.len());
    bytes.extend_from_slice(b"atomic/generation-request-key/v1\0");
    bytes.extend_from_slice(lineage_id.as_bytes());
    bytes.extend_from_slice(&key_length.to_be_bytes());
    bytes.extend_from_slice(request_key.as_bytes());
    Ok(sha256(&bytes))
}

/// Non-reversible replacement for a copied request-content digest. It keeps a
/// stable occupied decision coordinate without retaining a dictionary-testable
/// hash of the original forms that may themselves contain excised values.
pub(crate) fn tombstone_request_digest(
    lineage_id: &str,
    generation: u64,
    basis_t: u64,
    key_hash: Digest,
) -> Result<Digest, SemanticError> {
    if !is_canonical_lineage(lineage_id) || generation == 0 || basis_t == 0 {
        return Err(incorrect(
            "generation/request-tombstone-coordinate",
            "request tombstone requires lineage and positive generation/basis",
        ));
    }
    let mut bytes = Vec::with_capacity(86);
    bytes.extend_from_slice(b"atomic/generation-request-tombstone/v1\0");
    bytes.extend_from_slice(lineage_id.as_bytes());
    bytes.extend_from_slice(&generation.to_be_bytes());
    bytes.extend_from_slice(&basis_t.to_be_bytes());
    bytes.extend_from_slice(&key_hash);
    Ok(sha256(&bytes))
}

fn take<'a>(bytes: &'a [u8], cursor: &mut usize, length: usize) -> Result<&'a [u8], SemanticError> {
    let end = cursor
        .checked_add(length)
        .ok_or_else(|| fault("generation/content-length", "content cursor overflow"))?;
    let value = bytes.get(*cursor..end).ok_or_else(|| {
        fault(
            "generation/content-length",
            "transaction content ended before its declared value",
        )
    })?;
    *cursor = end;
    Ok(value)
}

fn read_u16(bytes: &[u8], cursor: &mut usize) -> Result<u16, SemanticError> {
    Ok(u16::from_be_bytes(
        take(bytes, cursor, 2)?
            .try_into()
            .expect("checked u16 length"),
    ))
}

fn read_u32(bytes: &[u8], cursor: &mut usize) -> Result<u32, SemanticError> {
    Ok(u32::from_be_bytes(
        take(bytes, cursor, 4)?
            .try_into()
            .expect("checked u32 length"),
    ))
}

fn read_u64(bytes: &[u8], cursor: &mut usize) -> Result<u64, SemanticError> {
    Ok(u64::from_be_bytes(
        take(bytes, cursor, 8)?
            .try_into()
            .expect("checked u64 length"),
    ))
}

fn read_string(bytes: &[u8], cursor: &mut usize, length: usize) -> Result<String, SemanticError> {
    String::from_utf8(take(bytes, cursor, length)?.to_vec()).map_err(|_| {
        fault(
            "generation/lineage-encoding",
            "database lineage is not valid UTF-8",
        )
    })
}

fn incorrect(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Incorrect, code, message)
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{USER_PARTITION, Value, make_eid, t_to_tx};

    const LINEAGE: &str = "01234567-89ab-4def-8123-456789abcdef";

    fn content() -> LineageTransactionContent {
        LineageTransactionContent {
            lineage_id: LINEAGE.to_owned(),
            basis_t: 1,
            eidx_frontier: INITIAL_EIDX_FRONTIER + 1,
            reserved_frontier: None,
            allocations: vec![make_eid(USER_PARTITION, INITIAL_EIDX_FRONTIER).unwrap()],
            tx_data: vec![Datom {
                entity: make_eid(USER_PARTITION, INITIAL_EIDX_FRONTIER).unwrap(),
                attribute: 50,
                value: Value::Instant(1_000),
                tx: t_to_tx(1).unwrap(),
                added: true,
            }],
        }
    }

    fn hex(bytes: &[u8]) -> String {
        bytes.iter().map(|byte| format!("{byte:02x}")).collect()
    }

    #[test]
    fn v1_bytes_and_hash_are_frozen_when_v2_is_available() {
        // Independent literal from the pre-v2 layout: body length 100, one
        // allocation and one canonical Instant datom, plus SHA-256 checksum.
        let expected = concat!(
            "41544c430001010000000000000000640024",
            "30313233343536372d383961622d346465662d383132332d343536373839616263646566",
            "000000000000000100000000000003e90000000100001000000003e8",
            "0000000100001000000003e8000000320600000000000003e8",
            "00000c000000000101",
            "1a0cab8ccacddededb6315866a35eee9686c109addf9cee2ddf20887f241923a"
        );
        let legacy = content();
        let bytes = legacy.encode().unwrap();
        assert_eq!(legacy.version(), 1);
        assert_eq!(hex(&bytes), expected);
        assert_eq!(
            hex(&sha256(&bytes)),
            "9f13fd86e2eac6c33c686b39db8d15be3901bbaded243c3638c058451fe20d98"
        );
        let recovered = LineageTransactionContent::decode(&bytes).unwrap();
        assert_eq!(recovered.reserved_frontier, None);
        assert_eq!(recovered.encode().unwrap(), bytes);
        // Selecting v2 is explicit, including when its frontier is initial.
        let mut modern = legacy;
        modern.reserved_frontier = Some(INITIAL_EIDX_FRONTIER);
        assert_eq!(modern.version(), 2);
        assert_ne!(modern.encode().unwrap(), bytes);
        assert_ne!(modern.hash().unwrap(), recovered.hash().unwrap());
    }

    fn receipt_only_transaction() -> DurableTransaction {
        let tx = t_to_tx(1).unwrap();
        DurableTransaction {
            database_id: "caller-alias".into(),
            basis_t: 1,
            previous_hash: [5; 32],
            eidx_frontier: 2_000_001,
            tempids: [
                ("old-system".into(), 42),
                ("new-system-a".into(), 1_000),
                ("new-system-b".into(), 1_001),
                (
                    "ordinary".into(),
                    make_eid(USER_PARTITION, 2_000_000).unwrap(),
                ),
                ("transaction".into(), tx),
            ]
            .into_iter()
            .collect(),
            tx_data: vec![Datom {
                entity: tx,
                attribute: crate::DB_TX_INSTANT as u32,
                value: Value::Instant(1_000),
                tx,
                added: true,
            }],
        }
    }

    #[test]
    fn v2_preserves_low_receipt_only_ids_without_changing_v1_selection() {
        let transaction = receipt_only_transaction();
        let prior = ReservedAllocation::initial();
        let after = ReservedAllocation::from_frontier(1_002, transaction.eidx_frontier).unwrap();
        let legacy =
            LineageTransactionContent::from_transaction(LINEAGE, 2_000_000, &transaction).unwrap();
        assert_eq!(legacy.version(), 1);
        assert_eq!(
            legacy.allocations,
            vec![make_eid(USER_PARTITION, 2_000_000).unwrap()]
        );
        let modern = LineageTransactionContent::from_transaction_v2(
            LINEAGE,
            2_000_000,
            prior,
            after,
            &transaction,
        )
        .unwrap();
        assert_eq!(
            modern.allocations,
            vec![1_000, 1_001, make_eid(USER_PARTITION, 2_000_000).unwrap()]
        );
        assert_eq!(modern.validate_reserved_transition(prior).unwrap(), after);
        let encoded = modern.encode().unwrap();
        let decoded = LineageTransactionContent::decode(&encoded).unwrap();
        assert_eq!(decoded, modern);
        assert_eq!(decoded.encode().unwrap(), encoded);
        let replay = decoded.to_transaction([9; 32]);
        assert_eq!(replay.previous_hash, [9; 32]);
        assert_eq!(
            replay.tempids.values().copied().collect::<Vec<_>>(),
            modern.allocations
        );
        assert_eq!(transaction.tempids["new-system-a"], 1_000);
        assert!(
            !encoded
                .windows("new-system-a".len())
                .any(|bytes| bytes == b"new-system-a")
        );
    }

    #[test]
    fn v2_reserved_and_ordinary_witnesses_can_share_an_index() {
        let mut transaction = receipt_only_transaction();
        transaction.eidx_frontier = 1_002;
        transaction
            .tempids
            .insert("ordinary".into(), make_eid(USER_PARTITION, 1_000).unwrap());
        let prior = ReservedAllocation::initial();
        let after = ReservedAllocation::from_frontier(1_002, 1_002).unwrap();
        let content = LineageTransactionContent::from_transaction_v2(
            LINEAGE,
            1_000,
            prior,
            after,
            &transaction,
        )
        .unwrap();
        assert_eq!(
            content.allocations,
            vec![1_000, 1_001, make_eid(USER_PARTITION, 1_000).unwrap()]
        );
        assert_eq!(
            LineageTransactionContent::decode(&content.encode().unwrap()).unwrap(),
            content
        );
    }

    #[test]
    fn v2_retains_a_source_frontier_after_reference_only_facts_are_removed() {
        let mut transaction = receipt_only_transaction();
        transaction.tempids.clear();
        let prior = ReservedAllocation::initial();
        let after = ReservedAllocation::from_frontier(500_001, transaction.eidx_frontier).unwrap();
        let content = LineageTransactionContent::from_transaction_v2(
            LINEAGE,
            2_000_000,
            prior,
            after,
            &transaction,
        )
        .unwrap();
        assert!(content.allocations.is_empty());
        let decoded = LineageTransactionContent::decode(&content.encode().unwrap()).unwrap();
        let mut state = decoded.validate_reserved_transition(prior).unwrap();
        assert_eq!(state.allocate().unwrap(), 500_001);
    }

    #[test]
    fn v2_rejects_regression_missing_claim_and_stale_witness() {
        let transaction = receipt_only_transaction();
        let prior = ReservedAllocation::initial();
        let after = ReservedAllocation::from_frontier(1_002, transaction.eidx_frontier).unwrap();
        let valid = LineageTransactionContent::from_transaction_v2(
            LINEAGE,
            2_000_000,
            prior,
            after,
            &transaction,
        )
        .unwrap();
        let mut invalid = valid.clone();
        invalid.reserved_frontier = Some(1_001);
        assert_eq!(
            invalid.encode().unwrap_err().code,
            "generation/reserved-frontier-missing-claim"
        );
        invalid = valid.clone();
        invalid.reserved_frontier = Some(transaction.eidx_frontier + 1);
        assert_eq!(
            invalid.encode().unwrap_err().code,
            "allocation/reserved-frontier-out-of-range"
        );
        let later_prior =
            ReservedAllocation::from_frontier(1_003, transaction.eidx_frontier).unwrap();
        assert_eq!(
            valid
                .validate_reserved_transition(later_prior)
                .unwrap_err()
                .code,
            "generation/reserved-frontier-regression"
        );
        let stale_prior =
            ReservedAllocation::from_frontier(1_001, transaction.eidx_frontier).unwrap();
        assert_eq!(
            valid
                .validate_reserved_transition(stale_prior)
                .unwrap_err()
                .code,
            "generation/reserved-allocation-not-fresh"
        );
        let mut omitted_ref = valid;
        omitted_ref.tx_data.push(Datom {
            entity: make_eid(USER_PARTITION, 1_000).unwrap(),
            attribute: 100,
            value: Value::Tuple(vec![None, Some(Value::Ref(1_002))]),
            tx: t_to_tx(1).unwrap(),
            added: true,
        });
        assert_eq!(
            omitted_ref.encode().unwrap_err().code,
            "generation/reserved-frontier-missing-claim"
        );
    }

    #[test]
    fn v2_version_and_checkpoint_are_authenticated_and_malformed_input_is_rejected() {
        let mut value = content();
        value.reserved_frontier = Some(INITIAL_EIDX_FRONTIER);
        let encoded = value.encode().unwrap();
        let frontier_at = HEADER_LEN + 2 + LINEAGE.len() + 8 + 8;
        let mut tampered = encoded.clone();
        tampered[frontier_at + 7] ^= 1;
        assert_eq!(
            LineageTransactionContent::decode(&tampered)
                .unwrap_err()
                .code,
            "generation/content-checksum"
        );
        for offset in 0..encoded.len() {
            assert!(LineageTransactionContent::decode(&encoded[..offset]).is_err());
        }
        for version in [0_u16, 3, u16::MAX] {
            let mut unsupported = encoded.clone();
            unsupported[4..6].copy_from_slice(&version.to_be_bytes());
            assert_eq!(
                LineageTransactionContent::decode(&unsupported)
                    .unwrap_err()
                    .code,
                "generation/content-version"
            );
        }
        let mut mislabeled = encoded;
        mislabeled[4..6].copy_from_slice(&LEGACY_VERSION.to_be_bytes());
        let checksum_at = mislabeled.len() - CHECKSUM_LEN;
        let checksum = sha256(&mislabeled[..checksum_at]);
        mislabeled[checksum_at..].copy_from_slice(&checksum);
        assert!(LineageTransactionContent::decode(&mislabeled).is_err());
    }

    #[test]
    fn transaction_content_round_trips_without_generation_or_predecessor() {
        let content = content();
        let encoded = content.encode().unwrap();
        assert_eq!(
            LineageTransactionContent::decode(&encoded).unwrap(),
            content
        );
        assert!(!encoded.windows(32).any(|window| window == [7; 32]));
        let first = generation_transaction_hash(
            LINEAGE,
            1,
            1,
            [7; 32],
            content.hash().unwrap(),
            [8; 32],
            content.eidx_frontier,
        )
        .unwrap();
        let second = generation_transaction_hash(
            LINEAGE,
            2,
            1,
            [9; 32],
            content.hash().unwrap(),
            [8; 32],
            content.eidx_frontier,
        )
        .unwrap();
        assert_ne!(first, second);
        assert_eq!(content.hash().unwrap(), sha256(&encoded));
    }

    #[test]
    fn checksum_lineage_and_allocations_are_authenticated() {
        let content = content();
        let mut encoded = content.encode().unwrap();
        encoded[20] ^= 1;
        assert_eq!(
            LineageTransactionContent::decode(&encoded)
                .unwrap_err()
                .code,
            "generation/content-checksum"
        );

        let mut invalid = content;
        invalid.allocations.push(invalid.allocations[0]);
        assert_eq!(
            invalid.encode().unwrap_err().code,
            "generation/content-allocations"
        );
    }

    #[test]
    fn allocation_witnesses_preserve_partition_bits_without_allocating_transaction_time() {
        let named = make_eid(crate::DB_PARTITION, 1_001).unwrap();
        let implicit = make_eid(
            eid_to_part(crate::implicit_part(500).unwrap()).unwrap(),
            1_002,
        )
        .unwrap();
        let tx = t_to_tx(1_000).unwrap();
        let transaction = DurableTransaction {
            database_id: "alias".into(),
            basis_t: 1_000,
            previous_hash: [0; 32],
            eidx_frontier: 1_003,
            tempids: [
                ("named".into(), named),
                ("implicit".into(), implicit),
                ("tx".into(), tx),
            ]
            .into_iter()
            .collect(),
            tx_data: vec![Datom {
                entity: tx,
                attribute: crate::DB_TX_INSTANT as u32,
                value: Value::Instant(1_000),
                tx,
                added: true,
            }],
        };
        let content =
            LineageTransactionContent::from_transaction(LINEAGE, 1_000, &transaction).unwrap();
        assert_eq!(content.allocations, vec![named, implicit]);
        assert_eq!(
            LineageTransactionContent::decode(&content.encode().unwrap()).unwrap(),
            content
        );
    }

    #[test]
    fn only_canonical_v4_uuid_lineages_are_admitted() {
        assert!(is_canonical_lineage(LINEAGE));
        for value in [
            "",
            "01234567-89AB-4DEF-8123-456789ABCDEF",
            "01234567-89ab-3def-8123-456789abcdef",
            "01234567-89ab-4def-7123-456789abcdef",
            "0123456789ab4def8123456789abcdef",
        ] {
            assert!(!is_canonical_lineage(value), "{value}");
        }
    }

    #[test]
    fn durable_request_lookup_does_not_embed_plaintext_key() {
        let key = "customer@example.test/remove";
        let digest = request_key_hash(LINEAGE, key).unwrap();
        assert_eq!(digest.len(), 32);
        assert_ne!(digest, request_key_hash(LINEAGE, "different").unwrap());
        assert_eq!(
            request_key_hash(LINEAGE, "").unwrap_err().code,
            "generation/empty-request-key"
        );
        let tombstone = tombstone_request_digest(LINEAGE, 1, 2, digest).unwrap();
        assert_ne!(tombstone, sha256(key.as_bytes()));
        assert_ne!(tombstone, digest);
    }
}
