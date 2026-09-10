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
use crate::{
    Datom, Digest, DurableTransaction, ErrorCategory, INITIAL_EIDX_FRONTIER, IndexOrder, MAX_EIDX,
    SemanticError, eid_to_eidx, eid_to_part, sha256,
};
use std::collections::{BTreeMap, BTreeSet};

const MAGIC: &[u8; 4] = b"ATLC";
const VERSION: u16 = 1;
const KIND_TRANSACTION_CONTENT: u8 = 1;
const HEADER_LEN: usize = 16;
const CHECKSUM_LEN: usize = 32;
const MAX_LINEAGE_BYTES: usize = 64;
// The existing transaction codec accepts a 64 MiB body. The small allowance
// covers the lineage and collection headers while avoiding a new lower limit.
const MAX_BODY_BYTES: usize = 64 * 1024 * 1024 + 1024;
const MAX_ENVELOPE_BYTES: usize = HEADER_LEN + MAX_BODY_BYTES + CHECKSUM_LEN;
const MEMBERSHIP_DOMAIN: &[u8] = b"atomic/generation-membership/v1\0";

/// Immutable logical content shared by every physical generation that carries
/// the same transaction information.
#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct LineageTransactionContent {
    pub(crate) lineage_id: String,
    pub(crate) basis_t: u64,
    pub(crate) eidx_frontier: u64,
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
            allocations: allocations.into_iter().collect(),
            tx_data: transaction.tx_data.clone(),
        };
        content.validate()?;
        Ok(content)
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
        bytes.extend_from_slice(&VERSION.to_be_bytes());
        bytes.push(KIND_TRANSACTION_CONTENT);
        bytes.push(0);
        bytes.extend_from_slice(&(body_len as u64).to_be_bytes());
        bytes.extend_from_slice(&lineage_len.to_be_bytes());
        bytes.extend_from_slice(lineage);
        bytes.extend_from_slice(&self.basis_t.to_be_bytes());
        bytes.extend_from_slice(&self.eidx_frontier.to_be_bytes());
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
        if version != VERSION {
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
