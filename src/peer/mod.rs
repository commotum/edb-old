//! Live peer observation over the single immutable-object storage engine.
use crate::{Datom, SemanticError};
use std::sync::{Mutex, MutexGuard};
mod live;
mod snapshot;
pub use crate::index::cursor::MergeCursorStats as PeerCursorStats;
pub use crate::index::tree::cache::CacheStats;
pub use live::Peer;
pub use snapshot::PeerSnapshot;
#[cfg(test)]
mod allocation_tests;
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct RecoveryStats {
    pub base_t: u64,
    pub target_t: u64,
    pub tail_transactions: u64,
    /// PostgreSQL transaction-range streams used to authenticate the writer
    /// tail. This is zero for a covered endpoint and one for any non-empty
    /// tail, independent of transaction count.
    pub tail_range_reads: u64,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct PeerLoadStats {
    pub root_reads: u64,
    pub directory_reads: u64,
    pub leaf_reads: u64,
    /// Completed or dropped native cursors. This is the exact source-range
    /// count; memo replays do not open another cursor.
    pub cursor_ranges: u64,
    pub cursor_cache_hits: u64,
    pub cursor_cache_misses: u64,
    pub cursor_root_reads: u64,
    pub cursor_directory_reads: u64,
    pub cursor_leaf_reads: u64,
    pub cursor_sql_reads: u64,
    /// Canonical durable-node payload bytes fetched from PostgreSQL by those
    /// cursors. Cache hits correctly contribute zero bytes.
    pub cursor_sql_read_bytes: u64,
    pub cursor_recent_datoms_examined: u64,
    pub cursor_recent_datoms_yielded: u64,
}

/// Lazy ordered traversal retaining one immutable block snapshot.
pub struct PeerIndexCursor {
    cursor: crate::storage::snapshot::BlockIndexCursor,
}
impl PeerIndexCursor {
    pub(crate) fn block(cursor: crate::storage::snapshot::BlockIndexCursor) -> Self {
        Self { cursor }
    }
    pub fn stats(&self) -> PeerCursorStats {
        self.cursor.stats()
    }
    pub(crate) fn next_with_poll(
        &mut self,
        poll: &mut dyn FnMut() -> Result<bool, SemanticError>,
    ) -> Option<Result<Datom, SemanticError>> {
        self.cursor.next_with_poll(poll)
    }
}
impl Iterator for PeerIndexCursor {
    type Item = Result<Datom, SemanticError>;
    fn next(&mut self) -> Option<Self::Item> {
        self.next_with_poll(&mut || Ok(true))
    }
}
fn lock<T>(mutex: &Mutex<T>) -> MutexGuard<'_, T> {
    mutex
        .lock()
        .unwrap_or_else(std::sync::PoisonError::into_inner)
}

fn transaction_hash(&self) -> Digest {
        transaction_hash(&self.native)
    }
    pub fn entid(&self, ident: &Keyword) -> Option<u64> {
        self.native.entid(ident)
    }
    pub fn ident(&self, entity: u64) -> Option<&Keyword> {
        self.native.ident(entity)
    }
    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        self.database_value().entity(identifier)
    }
    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value().query(query, inputs, control)
    }
    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value()
            .query_with_extensions(query, inputs, control, extensions)
    }
    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        self.database_value().pull(pattern, entity)
    }
    fn ensure_order(&self, order: IndexOrder) -> Result<(), SemanticError> {
        if order == IndexOrder::Avet && !self.native.avet_unready().is_empty() {
            return Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "peer/avet-not-ready",
                "Captured AVET projections are incomplete",
            ));
        }
        Ok(())
    }
    pub fn range_cursor(
        &self,
        history: bool,
        order: IndexOrder,
        start: Option<&Datom>,
        end: Option<&Datom>,
    ) -> Result<PeerIndexCursor, SemanticError> {
        self.ensure_order(order)?;
        if let (Some(start), Some(end)) = (start, end)
            && !start.cmp_in(end, order).is_lt()
        {
            return Err(SemanticError::incorrect(
                "peer/invalid-tree-range",
                "Tree range start must precede its exclusive end",
            ));
        }
        Ok(PeerIndexCursor::block(
            self.native.range_cursor(history, order, start, end)?,
        ))
    }
    pub fn prefix_cursor(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<PeerIndexCursor, SemanticError> {
        Ok(PeerIndexCursor::block(
            self.native.prefix_cursor(history, prefix)?,
        ))
    }
    pub fn seek(
        &self,
        history: bool,
        order: IndexOrder,
        key: &Datom,
    ) -> Result<TreeSeekResult, SemanticError> {
        let mut cursor = self.range_cursor(history, order, Some(key), None)?;
        let datom = cursor.next().transpose()?;
        Ok(TreeSeekResult {
            datom,
            stats: cursor.stats().tree,
        })
    }
    pub fn range(
        &self,
        history: bool,
        order: IndexOrder,
        start: Option<&Datom>,
        end: Option<&Datom>,
    ) -> Result<TreeRangeResult, SemanticError> {
        collect(self.range_cursor(history, order, start, end)?)
    }
    pub fn datoms(
        &self,
        history: bool,
        order: IndexOrder,
    ) -> Result<TreeRangeResult, SemanticError> {
        self.range(history, order, None, None)
    }
    pub fn datoms_with_prefix(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<TreeRangeResult, SemanticError> {
        collect(self.prefix_cursor(history, prefix)?)
    }
}

fn collect(mut cursor: PeerIndexCursor) -> Result<TreeRangeResult, SemanticError> {
    let datoms = cursor.by_ref().collect::<Result<Vec<_>, _>>()?;
    Ok(TreeRangeResult {
        datoms,
        stats: cursor.stats().tree,
    })
}
fn transaction_hash(snapshot: &BlockSnapshot) -> Digest {
    snapshot
        .captured_log()
        .and_then(|log| log.latest_entry_id())
        .unwrap_or([0; 32])
}
