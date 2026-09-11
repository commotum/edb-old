//! Immutable snapshot conveniences; these values do not own a live peer or writer.
use super::{PeerIndexCursor, transaction_hash};
use crate::index::tree::{TreeRangeResult, TreeSeekResult};
use crate::storage::BlockSnapshot;
use crate::{DatabaseValue, Datom, Digest, Entity, EntityIdentifier, ErrorCategory,
    IndexOrder, IndexPrefix, Keyword, PullPattern, Query, QueryControl, QueryExtensions,
    QueryInput, QueryOutcome, QueryValue, Schema, SemanticError};

/// One immutable database value and its observed physical publication.
#[derive(Clone)]
pub struct PeerSnapshot {
    pub(super) native: BlockSnapshot,
    pub(super) publication_revision: u64,
}

impl PeerSnapshot {
    pub fn basis_t(&self) -> u64 {
        self.native.basis_t()
    }
    pub fn eidx_frontier(&self) -> u64 {
        self.native.eidx_frontier()
    }
    pub fn last_tx_instant(&self) -> Option<i64> {
        self.native.last_tx_instant()
    }
    pub fn schema(&self) -> &Schema {
        self.native.schema()
    }
    pub fn database_value(&self) -> DatabaseValue {
        self.native.database_value()
    }
    pub fn durable_base_t(&self) -> Option<u64> {
        Some(self.native.indexed_basis_t())
    }
    pub fn durable_base_revision(&self) -> Option<u64> {
        Some(self.publication_revision)
    }
    pub 
impl DatabaseValue {
    /// Adopt an already captured peer snapshot as an immutable database value.
    pub fn native(snapshot: PeerSnapshot) -> Self {
        snapshot.database_value()
    }
}
impl From<PeerSnapshot> for DatabaseValue {
    fn from(snapshot: PeerSnapshot) -> Self { Self::native(snapshot) }
}
impl From<&PeerSnapshot> for DatabaseValue {
    fn from(snapshot: &PeerSnapshot) -> Self { Self::native(snapshot.clone()) }
}
