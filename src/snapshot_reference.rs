//! Logical committed identity and bounded, non-capability snapshot references.
use super::{
    ExactEndpoint, Peer, TieredSnapshot, lock, read_database_lineage, read_excision_generation,
    reconnect_peer_io, verify_database_lineage,
};
use crate::{DatabaseValue, Digest, ErrorCategory, PostgresConnectionConfig, SemanticError};
use std::sync::Arc;

const MAGIC: &[u8; 6] = b"ATSN\0\x01";
const MAX_REFERENCE_BYTES: usize = 256 * 1024;
const MAX_NAME_BYTES: usize = 64 * 1024;

/// Inexpensive logical identity of a committed native value and supported view.
/// Physical publications, cache allocations and the catalog address are absent.
/// This identifies a database input, not a query/result (which also depends on
/// the query, inputs, rules and extension code, and noHistory retention). It is not content equality
/// between independently created databases or a security capability.
#[derive(Clone, Debug, Eq, PartialEq, Ord, PartialOrd, Hash)]
pub struct SnapshotKey {
    lineage: Arc<str>,
    generation: u64,
    basis_t: u64,
    transaction_hash: Digest,
    state_hash: Digest,
    eidx_frontier: u64,
    as_of_t: Option<u64>,
    since_t: Option<u64>,
    history: bool,
}

impl SnapshotKey {
    pub fn lineage_id(&self) -> &str {
        &self.lineage
    }
    pub fn generation(&self) -> u64 {
        self.generation
    }
    pub fn basis_t(&self) -> u64 {
        self.basis_t
    }
    pub fn transaction_hash(&self) -> Digest {
        self.transaction_hash
    }
    pub fn state_hash(&self) -> Digest {
        self.state_hash
    }
    pub fn eidx_frontier(&self) -> u64 {
        self.eidx_frontier
    }
    pub fn as_of_t(&self) -> Option<u64> {
        self.as_of_t
    }
    pub fn since_t(&self) -> Option<u64> {
        self.since_t
    }
    pub fn is_history(&self) -> bool {
        self.history
    }

    fn endpoint(&self) -> ExactEndpoint {
        ExactEndpoint {
            generation: self.generation,
            basis_t: self.basis_t,
            tx_hash: self.transaction_hash,
            state_hash: self.state_hash,
            eidx_frontier: self.eidx_frontier,
        }
    }

    fn view(&self, mut value: DatabaseValue) -> DatabaseValue {
        if let Some(t) = self.as_of_t {
            value = value.as_of(t);
        }
        if let Some(t) = self.since_t {
            value = value.since(t);
        }
        if self.history {
            value = value.history();
        }
        value
    }
}

/// Versioned route and exact logical coordinate. Holding or serializing this
/// object does not retain database data or grant access. Reopening checks the
/// supplied PostgreSQL authority, lineage, active excision generation and exact
/// authenticated endpoint. Pre-excision references are rejected even if old
/// artifacts remain; already-held values retain their existing retention policy.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct SnapshotReference {
    database_id: Arc<str>,
    key: SnapshotKey,
    // Retrieval/retention witness only, never logical key identity. In particular,
    // noHistory consolidation may omit old pairs at the same committed endpoint.
    required_manifest: Digest,
}

impl DatabaseValue {
    /// Capture the authenticated transaction log through this committed basis,
    /// with the same pins and read authority and no new connection. Temporal
    /// modifiers select database datoms, not log endpoints: use tx_range bounds
    /// on the returned log. Eager/speculative/opaque-filter values are unsupported.
    pub fn log_value(&self) -> Result<crate::LogValue, SemanticError> {
        let (snapshot, ..) = self.snapshot_parts()?;
        Ok(crate::LogValue::new(snapshot))
    }

    /// No SQL or fact scan. Eager fixtures, speculative values and opaque
    /// predicate filters have no portable committed key and return Unsupported.
    /// An as-of view keeps its original basis/schema, so it intentionally differs
    /// from reopening the older committed value itself.
    pub fn snapshot_key(&self) -> Result<SnapshotKey, SemanticError> {
        let (snapshot, as_of_t, since_t, history) = self.snapshot_parts()?;
        let endpoint = snapshot.endpoint();
        Ok(SnapshotKey {
            lineage: Arc::from(snapshot.core.lineage_id.as_str()),
            generation: endpoint.generation,
            basis_t: endpoint.basis_t,
            transaction_hash: endpoint.tx_hash,
            state_hash: endpoint.state_hash,
            eidx_frontier: endpoint.eidx_frontier,
            as_of_t,
            since_t,
            history,
        })
    }

    pub fn snapshot_reference(&self) -> Result<SnapshotReference, SemanticError> {
        let (snapshot, ..) = self.snapshot_parts()?;
        Ok(SnapshotReference {
            database_id: Arc::from(snapshot.core.database_id.as_str()),
            key: self.snapshot_key()?,
            required_manifest: snapshot.required_manifest_hash()?,
        })
    }
}

impl SnapshotReference {
    pub fn database_id(&self) -> &str {
        &self.database_id
    }
    pub fn key(&self) -> &SnapshotKey {
        &self.key
    }

    /// Bounded native v1 representation. Its checksum detects corruption, not
    /// forgery; decoded coordinates are always authenticated again when opened.
    /// Reference framing limits do not change supported database identifiers.
    pub fn encode(&self) -> Result<Vec<u8>, SemanticError> {
        let mut bytes = Vec::new();
        bytes.extend_from_slice(MAGIC);
        put_name(&mut bytes, &self.database_id)?;
        put_name(&mut bytes, &self.key.lineage)?;
        for n in [
            self.key.generation,
            self.key.basis_t,
            self.key.eidx_frontier,
        ] {
            bytes.extend_from_slice(&n.to_be_bytes());
        }
        bytes.extend_from_slice(&self.key.transaction_hash);
        bytes.extend_from_slice(&self.key.state_hash);
        bytes.extend_from_slice(&self.required_manifest);
        for bound in [self.key.as_of_t, self.key.since_t] {
            bytes.push(u8::from(bound.is_some()));
            bytes.extend_from_slice(&bound.unwrap_or(0).to_be_bytes());
        }
        bytes.push(u8::from(self.key.history));
        bytes.extend_from_slice(&crate::sha256(&bytes));
        Ok(bytes)
    }

    pub fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        if bytes.len() > MAX_REFERENCE_BYTES || bytes.len() < MAGIC.len() + 32 {
            return Err(invalid_reference());
        }
        let (body, hash) = bytes.split_at(bytes.len() - 32);
        if crate::sha256(body) != hash {
            return Err(invalid_reference());
        }
        let mut input = ReferenceInput { bytes: body };
        if input.take(MAGIC.len())? != MAGIC {
            return Err(invalid_reference());
        }
        let database_id = input.name()?;
        let lineage = input.name()?;
        let generation = input.u64()?;
        let basis_t = input.u64()?;
        let eidx_frontier = input.u64()?;
        let transaction_hash = input.take(32)?.try_into().unwrap();
        let state_hash = input.take(32)?.try_into().unwrap();
        let required_manifest = input.take(32)?.try_into().unwrap();
        let as_of_t = input.bound()?;
        let since_t = input.bound()?;
        let history = input.boolean()?;
        if !input.bytes.is_empty() {
            return Err(invalid_reference());
        }
        let reference = Self {
            database_id,
            required_manifest,
            key: SnapshotKey {
                lineage,
                generation,
                basis_t,
                transaction_hash,
                state_hash,
                eidx_frontier,
                as_of_t,
                since_t,
                history,
            },
        };
        reference.key.endpoint().validate()?;
        Ok(reference)
    }

    /// Open directly at this endpoint, without recovering/capturing the latest
    /// value first. Discovery/authenticated tail replay may perform I/O; key
    /// construction and comparison do not. Cache limits have normal peer meaning.
    pub fn open(
        &self,
        connection: &PostgresConnectionConfig,
        cache_entries: usize,
        cache_bytes: usize,
    ) -> Result<DatabaseValue, SemanticError> {
        // Check the route before traversing any tree, then check again once the
        // exact value owns its pins. These are observations, not an excision lock.
        {
            let mut client = connection.connect_for("snapshot/authorization")?;
            self.check_authority(&mut client)?;
        }
        let (snapshot, _) = TieredSnapshot::open_exact_configured(
            connection,
            self.database_id.to_string(),
            self.key.endpoint(),
            Some(self.required_manifest),
            cache_entries,
            cache_bytes,
            crate::recent::RecentLimits::default(),
        )?;
        self.finish_open(snapshot)
    }

    fn check_authority<C: crate::sql_io::GenericClient>(
        &self,
        client: &mut C,
    ) -> Result<(), SemanticError> {
        // A reference embeds a stable storage ID, never a reusable catalog
        // name. Rename leaves it valid; retirement forbids a new open even
        // while an already pinned value remains readable.
        crate::database_catalog::require_active_id_in(client, &self.database_id)?;
        if read_database_lineage(client, &self.database_id)? != self.key.lineage.as_ref() {
            return Err(wrong_identity());
        }
        if read_excision_generation(client, &self.database_id)? != self.key.generation {
            return Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "snapshot/generation-not-current",
                "snapshot reference is not in the currently authorized excision generation",
            ));
        }
        Ok(())
    }

    fn finish_open(&self, snapshot: TieredSnapshot) -> Result<DatabaseValue, SemanticError> {
        {
            let mut io = lock(&snapshot.core.io);
            if io.client.is_closed() {
                reconnect_peer_io(&snapshot.core, &mut io)?;
            }
            self.check_authority(&mut io.client)?;
        }
        let value = self.key.view(snapshot.database_value());
        if value.snapshot_key()? != self.key {
            return Err(wrong_identity());
        }
        Ok(value)
    }
}

impl Peer {
    /// Reopen this exact retained reference without moving the live peer. A
    /// reference is not a pin; absent/collected data or old generations fail.
    pub fn reopen_snapshot(
        &self,
        reference: &SnapshotReference,
    ) -> Result<DatabaseValue, SemanticError> {
        if self.core.read.database_id != reference.database_id.as_ref()
            || self.core.read.lineage_id != reference.key.lineage.as_ref()
        {
            return Err(wrong_identity());
        }
        {
            let mut io = lock(&self.core.read.io);
            if io.client.is_closed() {
                reconnect_peer_io(&self.core.read, &mut io)?;
            }
            verify_database_lineage(
                &mut io.client,
                &self.core.read.database_id,
                &self.core.read.lineage_id,
            )?;
            reference.check_authority(&mut io.client)?;
        }
        let (snapshot, _) = self.tiered_snapshot().open_exact_sharing_core(
            &self.core.read.database_id,
            reference.key.endpoint(),
            Some(reference.required_manifest),
        )?;
        reference.finish_open(snapshot)
    }
}

fn invalid_reference() -> SemanticError {
    SemanticError::incorrect(
        "snapshot/invalid-reference",
        "unsupported or malformed snapshot reference",
    )
}
fn wrong_identity() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Forbidden,
        "snapshot/identity-mismatch",
        "snapshot reference does not match the authorized database lineage",
    )
}
fn put_name(bytes: &mut Vec<u8>, name: &str) -> Result<(), SemanticError> {
    if name.is_empty() || name.len() > MAX_NAME_BYTES {
        return Err(invalid_reference());
    }
    bytes.extend_from_slice(&(name.len() as u32).to_be_bytes());
    bytes.extend_from_slice(name.as_bytes());
    Ok(())
}
struct ReferenceInput<'a> {
    bytes: &'a [u8],
}
impl<'a> ReferenceInput<'a> {
    fn take(&mut self, count: usize) -> Result<&'a [u8], SemanticError> {
        let (value, rest) = self
            .bytes
            .split_at_checked(count)
            .ok_or_else(invalid_reference)?;
        self.bytes = rest;
        Ok(value)
    }
    fn u64(&mut self) -> Result<u64, SemanticError> {
        Ok(u64::from_be_bytes(self.take(8)?.try_into().unwrap()))
    }
    fn boolean(&mut self) -> Result<bool, SemanticError> {
        match self.take(1)?[0] {
            0 => Ok(false),
            1 => Ok(true),
            _ => Err(invalid_reference()),
        }
    }
    fn bound(&mut self) -> Result<Option<u64>, SemanticError> {
        let present = self.boolean()?;
        let t = self.u64()?;
        if !present && t != 0 {
            return Err(invalid_reference());
        }
        Ok(present.then_some(t))
    }
    fn name(&mut self) -> Result<Arc<str>, SemanticError> {
        let length = u32::from_be_bytes(self.take(4)?.try_into().unwrap()) as usize;
        if length == 0 || length > MAX_NAME_BYTES {
            return Err(invalid_reference());
        }
        Ok(Arc::from(
            std::str::from_utf8(self.take(length)?).map_err(|_| invalid_reference())?,
        ))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    fn reference() -> SnapshotReference {
        SnapshotReference {
            database_id: Arc::from("catalog"),
            required_manifest: [5; 32],
            key: SnapshotKey {
                lineage: Arc::from("lineage"),
                generation: 1,
                basis_t: 2,
                transaction_hash: [3; 32],
                state_hash: [4; 32],
                eidx_frontier: crate::INITIAL_EIDX_FRONTIER,
                as_of_t: Some(1),
                since_t: None,
                history: true,
            },
        }
    }
    #[test]
    fn bounded_reference_roundtrips_and_rejects_corruption_and_noncanonical_forms() {
        let original = reference();
        let bytes = original.encode().unwrap();
        assert_eq!(SnapshotReference::decode(&bytes).unwrap(), original);
        for end in 0..bytes.len() {
            assert!(SnapshotReference::decode(&bytes[..end]).is_err());
        }
        for i in 0..bytes.len() {
            let mut bad = bytes.clone();
            bad[i] ^= 1;
            assert!(SnapshotReference::decode(&bad).is_err());
        }
        let mut bad = bytes[..bytes.len() - 32].to_vec();
        bad.push(0);
        bad.extend_from_slice(&crate::sha256(&bad));
        assert!(SnapshotReference::decode(&bad).is_err());
        assert!(SnapshotReference::decode(&vec![0; MAX_REFERENCE_BYTES + 1]).is_err());
    }
    #[test]
    fn keys_separate_views_but_not_route_names() {
        let a = reference();
        let mut b = a.clone();
        b.database_id = Arc::from("renamed");
        assert_eq!(a.key, b.key);
        b.key.as_of_t = None;
        assert_ne!(a.key, b.key);
        b = a.clone();
        b.key.since_t = Some(0);
        assert_ne!(a.key, b.key);
        b = a.clone();
        b.key.history = false;
        assert_ne!(a.key, b.key);
        b = a.clone();
        b.key.lineage = Arc::from("another");
        assert_ne!(a.key, b.key);
        b = a.clone();
        b.key.generation += 1;
        assert_ne!(a.key, b.key);
    }
}
