//! Immutable, authenticated recent-transaction tier.
//!
//! Datomic peers combine durable shallow trees with an in-memory index rebuilt
//! from the authoritative log. This module owns that bounded semantic value;
//! it neither persists data nor evicts an uncovered transaction. A soft limit
//! requests consolidation, while a separate hard limit applies write
//! backpressure if consolidation has not caught up.

use crate::encoding::validate_persistent_index_datoms;
use crate::recent_btset::{BtCursor, BtWork, RecentBtSet, RecentDatomRef};
use crate::{
    Datom, Digest, DurableTransaction, ErrorCategory, IndexOrder, Schema, SemanticError, ValueType,
    encode_transaction, t_to_tx, transaction_hash, tx_to_t,
};
use std::cmp::Ordering;
use std::mem::size_of;
use std::sync::Arc;

const MAX_BTSET_REFERENCES_PER_DATOM: u64 = 5;
const LOG_CHUNK_SIZE: usize = 32;

/// Transitional accounting name used by the transaction service. This now
/// denotes one Arc-backed raw-tree reference, not an entry/ordinal array slot.
pub(crate) type RecentLocator = RecentDatomRef;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct RecentLimits {
    /// Optional deterministic test/deployment guard. Production scheduling is
    /// byte-authoritative; defaults leave this dimension unconstrained.
    pub soft_datoms: u64,
    pub soft_bytes: u64,
    /// Optional deterministic test/deployment guard. Production scheduling is
    /// byte-authoritative; defaults leave this dimension unconstrained.
    pub hard_datoms: u64,
    pub hard_bytes: u64,
}

impl Default for RecentLimits {
    fn default() -> Self {
        Self {
            // Datomic sizes the memory index in bytes at the transactor. A
            // peer must accept that authoritative window; an unrelated local
            // datom-count default would reject valid tails based on value
            // shape rather than configured novelty capacity.
            soft_datoms: u64::MAX,
            soft_bytes: 32 * 1024 * 1024,
            hard_datoms: u64::MAX,
            hard_bytes: 512 * 1024 * 1024,
        }
    }
}

impl RecentLimits {
    fn validate(self) -> Result<(), SemanticError> {
        if self.soft_datoms == 0
            || self.soft_bytes == 0
            || self.hard_datoms == 0
            || self.hard_bytes == 0
            || self.soft_datoms > self.hard_datoms
            || self.soft_bytes > self.hard_bytes
        {
            return Err(SemanticError::incorrect(
                "recent/invalid-limits",
                "recent soft limits must be positive and cannot exceed hard limits",
            ));
        }
        Ok(())
    }
}

/// Endpoint schema used for AVET/VAET membership and, only during an explicit
/// consolidation, the current `:db/noHistory` flag.
///
/// The recent/log tier never projects history away. Datomic documents
/// noHistory as permission for future indexing jobs to forget matching pairs,
/// with no precise removal guarantee and no immediate effect on historical
/// values. Keeping that policy at consolidation avoids inventing stronger
/// database-value semantics.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct EndpointProjection {
    schema: Arc<Schema>,
}

impl EndpointProjection {
    pub fn new(schema: Schema) -> Self {
        Self {
            schema: Arc::new(schema),
        }
    }

    pub fn from_schema(schema: Arc<Schema>) -> Self {
        Self { schema }
    }

    pub fn schema(&self) -> &Schema {
        &self.schema
    }
}

/// One immutable authenticated log entry retained by the recent value.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct RecentEntry {
    pub transaction: Arc<DurableTransaction>,
    pub hash: Digest,
    pub encoded_bytes: u64,
    pub accounted_bytes: u64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct RecentStats {
    pub base_t: u64,
    pub end_t: u64,
    pub transactions: u64,
    pub datoms: u64,
    pub encoded_bytes: u64,
    /// Conservative allocator-independent account of retained transaction
    /// values plus persistent-tree datom references.
    pub accounted_bytes: u64,
    pub base_hash: Digest,
    pub end_hash: Digest,
    pub needs_consolidation: bool,
    /// Four raw event-tree entries are retained at most. AVET and VAET are
    /// conditional; no endpoint or touched-key duplicate trees exist.
    pub raw_index_entries: u64,
    pub index_nodes: u64,
    pub max_index_height: u32,
    pub log_chunks: u64,
}

/// Deterministic work evidence for construction of an immutable recent value.
/// These counters make algorithmic regressions testable without wall clocks.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct RecentWork {
    pub authenticated_datoms: u64,
    pub index_insert_attempts: u64,
    pub comparisons: u64,
    pub node_visits: u64,
    pub nodes_copied: u64,
    pub node_splits: u64,
    pub bulk_rebuild_datoms: u64,
    pub schema_backfill_datoms: u64,
    pub log_entries_copied: u64,
}

impl RecentWork {
    fn absorb_bt(&mut self, work: BtWork) {
        self.comparisons = self.comparisons.saturating_add(work.comparisons);
        self.node_visits = self.node_visits.saturating_add(work.node_visits);
        self.nodes_copied = self.nodes_copied.saturating_add(work.nodes_copied);
        self.node_splits = self.node_splits.saturating_add(work.node_splits);
    }

    fn saturating_add(self, other: Self) -> Self {
        Self {
            authenticated_datoms: self
                .authenticated_datoms
                .saturating_add(other.authenticated_datoms),
            index_insert_attempts: self
                .index_insert_attempts
                .saturating_add(other.index_insert_attempts),
            comparisons: self.comparisons.saturating_add(other.comparisons),
            node_visits: self.node_visits.saturating_add(other.node_visits),
            nodes_copied: self.nodes_copied.saturating_add(other.nodes_copied),
            node_splits: self.node_splits.saturating_add(other.node_splits),
            bulk_rebuild_datoms: self
                .bulk_rebuild_datoms
                .saturating_add(other.bulk_rebuild_datoms),
            schema_backfill_datoms: self
                .schema_backfill_datoms
                .saturating_add(other.schema_backfill_datoms),
            log_entries_copied: self
                .log_entries_copied
                .saturating_add(other.log_entries_copied),
        }
    }
}

/// Exact history pair an indexing job may omit for a currently noHistory
/// attribute. Keeping both datoms explicit prevents a caller from turning the
/// storage hint into a broad attribute/time purge.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct NoHistoryPair {
    pub retraction: Datom,
    pub assertion: Datom,
}

#[derive(Clone, Debug)]
struct RecentIndexes {
    eavt: RecentBtSet,
    aevt: RecentBtSet,
    avet: RecentBtSet,
    vaet: RecentBtSet,
}

impl RecentIndexes {
    fn empty() -> Self {
        Self {
            eavt: RecentBtSet::empty(IndexOrder::Eavt),
            aevt: RecentBtSet::empty(IndexOrder::Aevt),
            avet: RecentBtSet::empty(IndexOrder::Avet),
            vaet: RecentBtSet::empty(IndexOrder::Vaet),
        }
    }

    fn get(&self, order: IndexOrder) -> &RecentBtSet {
        match order {
            IndexOrder::Eavt => &self.eavt,
            IndexOrder::Aevt => &self.aevt,
            IndexOrder::Avet => &self.avet,
            IndexOrder::Vaet => &self.vaet,
        }
    }

    fn replace(&mut self, order: IndexOrder, tree: RecentBtSet) {
        match order {
            IndexOrder::Eavt => self.eavt = tree,
            IndexOrder::Aevt => self.aevt = tree,
            IndexOrder::Avet => self.avet = tree,
            IndexOrder::Vaet => self.vaet = tree,
        }
    }

    fn entries(&self) -> u64 {
        self.eavt
            .len()
            .saturating_add(self.aevt.len())
            .saturating_add(self.avet.len())
            .saturating_add(self.vaet.len())
    }

    fn nodes(&self) -> u64 {
        self.eavt
            .node_count()
            .saturating_add(self.aevt.node_count())
            .saturating_add(self.avet.node_count())
            .saturating_add(self.vaet.node_count())
    }

    fn max_height(&self) -> u32 {
        [
            self.eavt.height(),
            self.aevt.height(),
            self.avet.height(),
            self.vaet.height(),
        ]
        .into_iter()
        .max()
        .unwrap_or(0)
    }
}

#[derive(Debug)]
struct LogChunk {
    previous: Option<Arc<LogChunk>>,
    entries: Arc<[RecentEntry]>,
    total_len: u64,
}

/// Reverse-linked immutable chunks make successor extension bounded while
/// retaining chronological replay. A partial tail copies at most 31 entry
/// records; all older chunks and all transaction payloads remain shared.
#[derive(Clone, Debug, Default)]
struct PersistentLog {
    head: Option<Arc<LogChunk>>,
}

impl PersistentLog {
    fn len(&self) -> u64 {
        self.head.as_ref().map_or(0, |head| head.total_len)
    }

    fn chunks(&self) -> u64 {
        let mut count = 0_u64;
        let mut chunk = self.head.as_ref().map(Arc::clone);
        while let Some(current) = chunk {
            count = count.saturating_add(1);
            chunk = current.previous.as_ref().map(Arc::clone);
        }
        count
    }

    fn push(&self, entry: RecentEntry, work: &mut RecentWork) -> Self {
        let head = match &self.head {
            Some(head) if head.entries.len() < LOG_CHUNK_SIZE => {
                let mut entries = Vec::with_capacity(head.entries.len() + 1);
                entries.extend(head.entries.iter().cloned());
                work.log_entries_copied = work
                    .log_entries_copied
                    .saturating_add(head.entries.len() as u64);
                entries.push(entry);
                Arc::new(LogChunk {
                    previous: head.previous.as_ref().map(Arc::clone),
                    total_len: head
                        .previous
                        .as_ref()
                        .map_or(0, |previous| previous.total_len)
                        .saturating_add(entries.len() as u64),
                    entries: entries.into(),
                })
            }
            Some(head) => Arc::new(LogChunk {
                previous: Some(Arc::clone(head)),
                entries: vec![entry].into(),
                total_len: head.total_len.saturating_add(1),
            }),
            None => Arc::new(LogChunk {
                previous: None,
                entries: vec![entry].into(),
                total_len: 1,
            }),
        };
        Self { head: Some(head) }
    }

    fn entries(&self) -> Vec<RecentEntry> {
        let mut chunks = Vec::new();
        let mut chunk = self.head.as_ref().map(Arc::clone);
        while let Some(current) = chunk {
            chunk = current.previous.as_ref().map(Arc::clone);
            chunks.push(current);
        }
        let mut entries = Vec::with_capacity(self.len() as usize);
        for chunk in chunks.iter().rev() {
            entries.extend(chunk.entries.iter().cloned());
        }
        entries
    }
}

/// Immutable recent tier. Clones share transaction payloads, completed log
/// chunks, projection metadata, and untouched B-tree nodes; failed appends
/// leave the prior value usable.
#[derive(Clone, Debug)]
pub struct RecentTier {
    database_id: Arc<str>,
    base_t: u64,
    base_hash: Digest,
    log: PersistentLog,
    projection: Arc<EndpointProjection>,
    /// The four raw event sets from recovered `IndexSet`. Current values are
    /// streamed by collapsing the newest operation in each stored E/A/V group.
    indexes: RecentIndexes,
    limits: RecentLimits,
    stats: RecentStats,
    index_builds: u64,
    work: RecentWork,
    last_work: RecentWork,
}

impl RecentTier {
    pub fn new(
        database_id: impl Into<String>,
        base_t: u64,
        base_hash: Digest,
        transactions: impl IntoIterator<Item = DurableTransaction>,
        endpoint_projection: EndpointProjection,
        limits: RecentLimits,
    ) -> Result<Self, SemanticError> {
        Self::new_entries(
            database_id,
            base_t,
            base_hash,
            transactions.into_iter().map(|transaction| (None, transaction)),
            endpoint_projection,
            limits,
        )
    }

    /// Construct a recent value from log entries whose chain hashes were
    /// already authenticated by the durable log reader. Generation logs hash
    /// their immutable membership envelope, not merely the portable
    /// transaction payload, so that hash cannot be recomputed from
    /// `DurableTransaction` alone.
    pub(crate) fn new_authenticated(
        database_id: impl Into<String>,
        base_t: u64,
        base_hash: Digest,
        transactions: impl IntoIterator<Item = (Digest, DurableTransaction)>,
        endpoint_projection: EndpointProjection,
        limits: RecentLimits,
    ) -> Result<Self, SemanticError> {
        Self::new_entries(
            database_id,
            base_t,
            base_hash,
            transactions
                .into_iter()
                .map(|(hash, transaction)| (Some(hash), transaction)),
            endpoint_projection,
            limits,
        )
    }

    fn new_entries(
        database_id: impl Into<String>,
        base_t: u64,
        base_hash: Digest,
        transactions: impl IntoIterator<Item = (Option<Digest>, DurableTransaction)>,
        endpoint_projection: EndpointProjection,
        limits: RecentLimits,
    ) -> Result<Self, SemanticError> {
        limits.validate()?;
        t_to_tx(base_t)?;
        let database_id = database_id.into();
        if database_id.is_empty() {
            return Err(SemanticError::incorrect(
                "recent/empty-database-id",
                "recent tier database id cannot be empty",
            ));
        }

        let database_id: Arc<str> = database_id.into();
        let mut expected_t = base_t;
        let mut expected_hash = base_hash;
        let mut log = PersistentLog::default();
        let mut indexes = RecentIndexes::empty();
        let mut work = RecentWork::default();
        let mut datom_count = 0_u64;
        let mut byte_count = 0_u64;
        let mut accounted_count = 0_u64;
        for (authenticated_hash, transaction) in transactions {
            expected_t = expected_t.checked_add(1).ok_or_else(|| {
                fault("recent/basis-overflow", "recent transaction basis overflow")
            })?;
            let entry = authenticate_entry(
                &database_id,
                expected_t,
                expected_hash,
                transaction,
                authenticated_hash,
            )?;
            let entry_datoms = entry.transaction.tx_data.len() as u64;
            work.authenticated_datoms = work.authenticated_datoms.saturating_add(entry_datoms);
            work.bulk_rebuild_datoms = work.bulk_rebuild_datoms.saturating_add(entry_datoms);
            datom_count = checked_add(datom_count, entry_datoms, "recent datom count")?;
            byte_count = checked_add(byte_count, entry.encoded_bytes, "recent byte count")?;
            accounted_count = checked_add(
                accounted_count,
                entry.accounted_bytes,
                "recent accounted byte count",
            )?;
            enforce_hard_limit(datom_count, accounted_count, limits)?;
            insert_entry(
                &mut indexes,
                &entry,
                endpoint_projection.schema(),
                &mut work,
            )?;
            expected_hash = entry.hash;
            log = log.push(entry, &mut work);
        }
        let stats = recent_stats(
            base_t,
            base_hash,
            &log,
            &indexes,
            datom_count,
            byte_count,
            accounted_count,
            expected_hash,
            limits,
        )?;
        Ok(Self {
            database_id,
            base_t,
            base_hash,
            log,
            projection: Arc::new(endpoint_projection),
            indexes,
            limits,
            stats,
            index_builds: 1,
            work,
            last_work: work,
        })
    }

    /// Return a successor recent value or hard-capacity backpressure. No
    /// covered/uncovered entry is removed from `self` in either case.
    pub fn append(
        &self,
        transaction: DurableTransaction,
        endpoint_projection: EndpointProjection,
    ) -> Result<Self, SemanticError> {
        self.extend(std::iter::once(transaction), endpoint_projection)
    }

    /// Authenticate and add a contiguous batch by path-copying only the four
    /// affected search paths per datom. The prior value's roots and completed
    /// log chunks remain shared.
    pub fn extend(
        &self,
        transactions: impl IntoIterator<Item = DurableTransaction>,
        endpoint_projection: EndpointProjection,
    ) -> Result<Self, SemanticError> {
        self.extend_entries(
            transactions.into_iter().map(|transaction| (None, transaction)),
            endpoint_projection,
        )
    }

    pub(crate) fn extend_authenticated(
        &self,
        transactions: impl IntoIterator<Item = (Digest, DurableTransaction)>,
        endpoint_projection: EndpointProjection,
    ) -> Result<Self, SemanticError> {
        self.extend_entries(
            transactions
                .into_iter()
                .map(|(hash, transaction)| (Some(hash), transaction)),
            endpoint_projection,
        )
    }

    fn extend_entries(
        &self,
        transactions: impl IntoIterator<Item = (Option<Digest>, DurableTransaction)>,
        endpoint_projection: EndpointProjection,
    ) -> Result<Self, SemanticError> {
        let mut expected_t = self.stats.end_t;
        let mut end_hash = self.stats.end_hash;
        let mut datom_count = self.stats.datoms;
        let mut byte_count = self.stats.encoded_bytes;
        let mut accounted_count = self.stats.accounted_bytes;
        let mut log = self.log.clone();
        let mut indexes = self.indexes.clone();
        let mut last_work = RecentWork::default();
        backfill_schema_transitions(
            self.projection.schema(),
            endpoint_projection.schema(),
            &mut indexes,
            &mut last_work,
        )?;
        for (authenticated_hash, transaction) in transactions {
            expected_t = expected_t.checked_add(1).ok_or_else(|| {
                fault("recent/basis-overflow", "recent transaction basis overflow")
            })?;
            let entry = authenticate_entry(
                &self.database_id,
                expected_t,
                end_hash,
                transaction,
                authenticated_hash,
            )?;
            let entry_datoms = entry.transaction.tx_data.len() as u64;
            last_work.authenticated_datoms =
                last_work.authenticated_datoms.saturating_add(entry_datoms);
            datom_count = checked_add(datom_count, entry_datoms, "recent datom count")?;
            byte_count = checked_add(byte_count, entry.encoded_bytes, "recent byte count")?;
            accounted_count = checked_add(
                accounted_count,
                entry.accounted_bytes,
                "recent accounted byte count",
            )?;
            enforce_hard_limit(datom_count, accounted_count, self.limits)?;
            insert_entry(
                &mut indexes,
                &entry,
                endpoint_projection.schema(),
                &mut last_work,
            )?;
            end_hash = entry.hash;
            log = log.push(entry, &mut last_work);
        }
        let stats = recent_stats(
            self.base_t,
            self.base_hash,
            &log,
            &indexes,
            datom_count,
            byte_count,
            accounted_count,
            end_hash,
            self.limits,
        )?;
        Ok(Self {
            database_id: Arc::clone(&self.database_id),
            base_t: self.base_t,
            base_hash: self.base_hash,
            log,
            projection: Arc::new(endpoint_projection),
            indexes,
            limits: self.limits,
            stats,
            index_builds: self.index_builds.saturating_add(1),
            work: self.work.saturating_add(last_work),
            last_work,
        })
    }

    pub fn database_id(&self) -> &str {
        &self.database_id
    }

    pub fn projection(&self) -> Arc<EndpointProjection> {
        Arc::clone(&self.projection)
    }

    pub fn includes(&self, order: IndexOrder, datom: &Datom) -> Result<bool, SemanticError> {
        index_member(self.projection.schema(), datom, order)
    }

    pub fn entries(&self) -> Arc<[RecentEntry]> {
        self.log.entries().into()
    }

    /// Sorted authenticated tail datoms with endpoint membership for `order`.
    /// Both assertions and retractions are retained; collapse happens only at
    /// the requested current/history read boundary.
    pub fn datoms(&self, order: IndexOrder) -> Arc<[Datom]> {
        self.cursor(true, order, &RecentRange::unbounded())
            .expect("an authenticated recent tree has a valid unbounded cursor")
            .collect::<Vec<_>>()
            .into()
    }

    /// Sorted current assertions contributed by this tail at its endpoint.
    /// This is a compatibility materializer over the lazy raw-event cursor;
    /// no endpoint copy is retained in the database value.
    pub fn current_datoms(&self, order: IndexOrder) -> Arc<[Datom]> {
        self.cursor(false, order, &RecentRange::unbounded())
            .expect("an authenticated recent tree has a valid unbounded cursor")
            .collect::<Vec<_>>()
            .into()
    }

    pub(crate) fn touches_current(&self, datom: &Datom) -> bool {
        let mut cursor = self
            .indexes
            .eavt
            .seek_by(|candidate| stored_eav_cmp(candidate, datom));
        cursor
            .next()
            .is_some_and(|candidate| same_eav(candidate.datom(), datom))
    }

    pub fn history_seek(&self, order: IndexOrder, key: &Datom) -> Option<Datom> {
        self.cursor(true, order, &RecentRange::new(Some(key.clone()), None))
            .ok()?
            .next()
    }

    pub fn current_seek(&self, order: IndexOrder, key: &Datom) -> Option<Datom> {
        self.cursor(false, order, &RecentRange::new(Some(key.clone()), None))
            .ok()?
            .next()
    }

    pub fn cursor(
        &self,
        history: bool,
        order: IndexOrder,
        range: &RecentRange,
    ) -> Result<RecentCursor, SemanticError> {
        range.validate(order)?;
        Ok(RecentCursor::new(
            self.indexes.get(order),
            Arc::clone(&self.projection),
            history,
            order,
            range.clone(),
        ))
    }

    pub fn stats(&self) -> RecentStats {
        self.stats
    }

    pub fn work(&self) -> RecentWork {
        self.work
    }

    pub fn last_work(&self) -> RecentWork {
        self.last_work
    }

    /// Number of immutable construction generations in this value's lineage.
    /// Use [`RecentTier::last_work`] for actual incremental work; successors
    /// path-copy index nodes and do not rebuild complete indexes.
    pub fn index_builds(&self) -> u64 {
        self.index_builds
    }

    pub fn needs_consolidation(&self) -> bool {
        self.stats.needs_consolidation
    }

    /// Overlay the authenticated tail on a durable current range.
    ///
    /// The durable input may be a logical subrange; every tail delta is
    /// applied before the final bounds filter so a retraction cannot become
    /// invisible merely because its transaction component differs from the
    /// assertion it retracts.
    pub fn merge_current_range(
        &self,
        order: IndexOrder,
        durable_current: &[Datom],
        range: &RecentRange,
    ) -> Result<Vec<Datom>, SemanticError> {
        validate_durable_range(order, durable_current, false, self.base_t)?;
        range.validate(order)?;
        let mut current = Vec::with_capacity(durable_current.len());
        for datom in durable_current {
            if index_member(&self.projection.schema, datom, order)?
                && !self.touches_current(datom)
                && range.contains(datom, order)
            {
                current.push(datom.clone());
            }
        }
        current.extend(self.cursor(false, order, range)?);
        current.sort_by(|left, right| left.cmp_in(right, order));
        Ok(current)
    }

    /// Merge durable retained history with every authenticated recent event.
    ///
    /// noHistory never changes this read: the log/memory tier remains exact
    /// until an indexing job explicitly consolidates it. Results are owned, so
    /// callers do not retain cache/segment borrows across eviction or snapshot
    /// changes.
    pub fn merge_history_range(
        &self,
        order: IndexOrder,
        durable_history: &[Datom],
        range: &RecentRange,
    ) -> Result<Vec<Datom>, SemanticError> {
        validate_durable_range(order, durable_history, true, self.base_t)?;
        range.validate(order)?;
        let mut retained = Vec::with_capacity(durable_history.len());
        for datom in durable_history {
            if index_member(&self.projection.schema, datom, order)? && range.contains(datom, order)
            {
                retained.push(datom.clone());
            }
        }
        retained.extend(self.cursor(true, order, range)?);
        retained.sort_by(|left, right| left.cmp_in(right, order));
        retained.dedup_by(|left, right| same_stored_datom(left, right));
        Ok(retained)
    }

    /// Model the recovered indexing-time `filter-nohist-pairs` operation.
    ///
    /// See `datomic_pro_docs/03_schema/01_changing_schema.md` (future indexing
    /// jobs only) and `1.0.7705/peer/src-clj/datomic/index.clj` around
    /// `filter-nohist-pairs`.
    ///
    /// Any adjacent retraction/assertion pair in the affected merged segment
    /// is eligible while the endpoint attribute is currently noHistory. This
    /// deliberately has no transaction-boundary test: Datomic applies
    /// `filter-nohist-pairs` after merging old and new data for each segment,
    /// so a pair may straddle the persistent/recent boundary and an older pair
    /// may be forgotten incidentally when its segment is rebuilt. This remains
    /// range-local rather than a global retroactive sweep. If noHistory is
    /// false at this indexing boundary, all pairs are retained. The supplied
    /// durable range must include both sides of any E/A/V history group it
    /// expects the consolidator to consider.
    pub fn consolidate_history_range(
        &self,
        order: IndexOrder,
        durable_history: &[Datom],
        range: &RecentRange,
    ) -> Result<Vec<Datom>, SemanticError> {
        range.validate(order)?;
        let merged = self.merge_history_range(order, durable_history, &RecentRange::unbounded())?;
        let mut retained = Vec::with_capacity(merged.len());
        let mut index = 0;
        while index < merged.len() {
            let datom = &merged[index];
            let matching_assertion = merged
                .get(index + 1)
                .is_some_and(|next| self.eligible_no_history_pair(datom, next));
            if matching_assertion {
                index += 2;
            } else {
                retained.push(datom.clone());
                index += 1;
            }
        }
        Ok(retained
            .into_iter()
            .filter(|datom| range.contains(datom, order))
            .collect())
    }

    /// Enumerate exact pair removals for a copy-on-write consolidator.
    pub fn no_history_pairs(
        &self,
        order: IndexOrder,
        durable_history: &[Datom],
    ) -> Result<Vec<NoHistoryPair>, SemanticError> {
        let merged = self.merge_history_range(order, durable_history, &RecentRange::unbounded())?;
        Ok(merged
            .windows(2)
            .filter(|pair| self.eligible_no_history_pair(&pair[0], &pair[1]))
            .map(|pair| NoHistoryPair {
                retraction: pair[0].clone(),
                assertion: pair[1].clone(),
            })
            .collect())
    }

    fn eligible_no_history_pair(&self, retraction: &Datom, assertion: &Datom) -> bool {
        !retraction.added
            && assertion.added
            && same_logical_eav(retraction, assertion)
            && self
                .projection
                .schema
                .attribute(retraction.attribute)
                .is_ok_and(|attribute| attribute.no_history)
    }
}

#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct RecentRange {
    pub start: Option<Datom>,
    pub end: Option<Datom>,
}

impl RecentRange {
    pub fn unbounded() -> Self {
        Self::default()
    }

    pub fn new(start: Option<Datom>, end: Option<Datom>) -> Self {
        Self { start, end }
    }

    fn validate(&self, order: IndexOrder) -> Result<(), SemanticError> {
        if let (Some(start), Some(end)) = (&self.start, &self.end)
            && !start.cmp_in(end, order).is_lt()
        {
            return Err(SemanticError::incorrect(
                "recent/invalid-range",
                "recent range start must be strictly below its exclusive end",
            ));
        }
        Ok(())
    }

    fn contains(&self, datom: &Datom, order: IndexOrder) -> bool {
        self.start
            .as_ref()
            .is_none_or(|start| !datom.cmp_in(start, order).is_lt())
            && self
                .end
                .as_ref()
                .is_none_or(|end| datom.cmp_in(end, order).is_lt())
    }
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct RecentCursorStats {
    pub comparisons: u64,
    pub node_visits: u64,
    pub datoms_examined: u64,
    pub datoms_yielded: u64,
}

/// Lazy ordered view over one raw memory-index tree. Current cursors collapse
/// consecutive stored E/A/V operations and expose only a newest assertion.
#[derive(Clone, Debug)]
pub struct RecentCursor {
    inner: BtCursor,
    projection: Arc<EndpointProjection>,
    history: bool,
    order: IndexOrder,
    range: RecentRange,
    pending: Option<RecentDatomRef>,
    exhausted: bool,
    examined: u64,
    yielded: u64,
}

impl RecentCursor {
    fn new(
        tree: &RecentBtSet,
        projection: Arc<EndpointProjection>,
        history: bool,
        order: IndexOrder,
        range: RecentRange,
    ) -> Self {
        let inner = match (&range.start, history) {
            (Some(start), true) => tree.seek(start),
            (Some(start), false) => {
                tree.seek_by(|candidate| primary_cmp_in(candidate, start, order))
            }
            (None, _) => tree.cursor(),
        };
        Self {
            inner,
            projection,
            history,
            order,
            range,
            pending: None,
            exhausted: false,
            examined: 0,
            yielded: 0,
        }
    }

    fn next_member(&mut self) -> Option<RecentDatomRef> {
        if let Some(candidate) = self.pending.take() {
            return Some(candidate);
        }
        loop {
            let candidate = self.inner.next()?;
            self.examined = self.examined.saturating_add(1);
            if index_member(self.projection.schema(), candidate.datom(), self.order)
                .expect("authenticated recent datom has an endpoint schema attribute")
            {
                return Some(candidate);
            }
        }
    }

    pub fn stats(&self) -> RecentCursorStats {
        let work = self.inner.work();
        RecentCursorStats {
            comparisons: work.comparisons,
            node_visits: work.node_visits,
            datoms_examined: self.examined,
            datoms_yielded: self.yielded,
        }
    }
}

impl Iterator for RecentCursor {
    type Item = Datom;

    fn next(&mut self) -> Option<Self::Item> {
        if self.exhausted {
            return None;
        }
        if self.history {
            loop {
                let candidate = self.next_member()?;
                let datom = candidate.datom();
                if self
                    .range
                    .end
                    .as_ref()
                    .is_some_and(|end| !datom.cmp_in(end, self.order).is_lt())
                {
                    self.exhausted = true;
                    return None;
                }
                if self.range.contains(datom, self.order) {
                    self.yielded = self.yielded.saturating_add(1);
                    return Some(datom.clone());
                }
            }
        }

        loop {
            let winner = self.next_member()?;
            while let Some(candidate) = self.next_member() {
                if same_eav(winner.datom(), candidate.datom()) {
                    continue;
                }
                self.pending = Some(candidate);
                break;
            }
            let datom = winner.datom();
            if self
                .range
                .end
                .as_ref()
                .is_some_and(|end| !datom.cmp_in(end, self.order).is_lt())
            {
                self.exhausted = true;
                return None;
            }
            if datom.added && self.range.contains(datom, self.order) {
                self.yielded = self.yielded.saturating_add(1);
                return Some(datom.clone());
            }
        }
    }
}

fn authenticate_entry(
    database_id: &str,
    expected_t: u64,
    expected_hash: Digest,
    transaction: DurableTransaction,
    authenticated_hash: Option<Digest>,
) -> Result<RecentEntry, SemanticError> {
    if transaction.database_id != database_id {
        return Err(fault(
            "recent/database-mismatch",
            "recent transaction belongs to a different database",
        ));
    }
    if transaction.basis_t != expected_t {
        return Err(fault(
            "recent/noncontiguous-basis",
            format!(
                "expected recent transaction basis {expected_t}, got {}",
                transaction.basis_t
            ),
        ));
    }
    if transaction.previous_hash != expected_hash {
        return Err(fault(
            "recent/hash-chain-mismatch",
            "recent transaction predecessor does not match the authenticated chain",
        ));
    }
    let encoded = encode_transaction(&transaction)?;
    let hash = authenticated_hash.unwrap_or_else(|| transaction_hash(&encoded));
    let locator_bytes = (transaction.tx_data.len() as u64)
        .saturating_mul(size_of::<RecentDatomRef>() as u64)
        .saturating_mul(MAX_BTSET_REFERENCES_PER_DATOM);
    let datom_bytes = transaction.tx_data.iter().fold(0_u64, |total, datom| {
        total.saturating_add(datom.retained_bytes())
    });
    let spare_datom_capacity = (transaction
        .tx_data
        .capacity()
        .saturating_sub(transaction.tx_data.len()) as u64)
        .saturating_mul(size_of::<Datom>() as u64);
    let tempid_bytes = transaction.tempids.iter().fold(0_u64, |total, (key, _)| {
        // Conservatively account the key buffer and one independently owned
        // ordered-map entry. Exact BTree node packing is allocator-specific.
        total
            .saturating_add(key.capacity() as u64)
            .saturating_add(size_of::<(String, u64)>() as u64)
            .saturating_add(4 * size_of::<usize>() as u64)
    });
    // Canonical bytes conservatively cover transaction-owned strings,
    // tempids, map nodes, and scalar magnitudes not already represented by
    // the inline datom account. Double-counting payload is preferable to an
    // input shape that slips past the configured resident-memory ceiling.
    let accounted_bytes = (encoded.len() as u64)
        .saturating_add(size_of::<DurableTransaction>() as u64)
        .saturating_add(datom_bytes)
        .saturating_add(spare_datom_capacity)
        .saturating_add(transaction.database_id.capacity() as u64)
        .saturating_add(tempid_bytes)
        .saturating_add(locator_bytes);
    Ok(RecentEntry {
        transaction: Arc::new(transaction),
        hash,
        encoded_bytes: encoded.len() as u64,
        accounted_bytes,
    })
}

fn insert_reference(
    indexes: &mut RecentIndexes,
    order: IndexOrder,
    reference: RecentDatomRef,
    work: &mut RecentWork,
    duplicate_is_error: bool,
) -> Result<(), SemanticError> {
    work.index_insert_attempts = work.index_insert_attempts.saturating_add(1);
    let mut bt_work = BtWork::default();
    let (tree, inserted) = indexes.get(order).insert(reference, &mut bt_work);
    work.absorb_bt(bt_work);
    if !inserted && duplicate_is_error {
        return Err(fault(
            "recent/duplicate-index-datom",
            "authenticated recent transactions do not form a strict index order",
        ));
    }
    indexes.replace(order, tree);
    Ok(())
}

fn insert_entry(
    indexes: &mut RecentIndexes,
    entry: &RecentEntry,
    schema: &Schema,
    work: &mut RecentWork,
) -> Result<(), SemanticError> {
    for (datom_index, datom) in entry.transaction.tx_data.iter().enumerate() {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            validate_persistent_index_datoms(order, std::slice::from_ref(datom))?;
            if index_member(schema, datom, order)? {
                let reference = RecentDatomRef::new(Arc::clone(&entry.transaction), datom_index)
                    .map_err(|_| {
                        fault(
                            "recent/too-many-transaction-datoms",
                            "one recent transaction cannot address more than u32::MAX datoms",
                        )
                    })?;
                insert_reference(indexes, order, reference, work, true)?;
            }
        }
    }
    Ok(())
}

fn backfill_schema_transitions(
    previous: &Schema,
    endpoint: &Schema,
    indexes: &mut RecentIndexes,
    work: &mut RecentWork,
) -> Result<(), SemanticError> {
    for attribute in endpoint.attributes() {
        let previous_attribute = previous.attribute(attribute.id).ok();
        let previous_avet = previous_attribute
            .is_some_and(|attribute| attribute.indexed || attribute.unique.is_some());
        let endpoint_avet = attribute.indexed || attribute.unique.is_some();
        let previous_vaet =
            previous_attribute.is_some_and(|attribute| attribute.value_type == ValueType::Ref);
        let endpoint_vaet = attribute.value_type == ValueType::Ref;

        for order in [IndexOrder::Avet, IndexOrder::Vaet] {
            let enabled = match order {
                IndexOrder::Avet => endpoint_avet && !previous_avet,
                IndexOrder::Vaet => endpoint_vaet && !previous_vaet,
                _ => false,
            };
            if !enabled {
                continue;
            }
            let mut source = indexes
                .aevt
                .seek_by(|datom| datom.attribute.cmp(&attribute.id));
            while let Some(reference) = source.next() {
                let datom = reference.datom();
                if datom.attribute != attribute.id {
                    break;
                }
                work.schema_backfill_datoms = work.schema_backfill_datoms.saturating_add(1);
                insert_reference(indexes, order, reference, work, false)?;
            }
        }
    }
    Ok(())
}

#[allow(clippy::too_many_arguments)]
fn recent_stats(
    base_t: u64,
    base_hash: Digest,
    log: &PersistentLog,
    indexes: &RecentIndexes,
    datoms: u64,
    encoded_bytes: u64,
    accounted_bytes: u64,
    end_hash: Digest,
    limits: RecentLimits,
) -> Result<RecentStats, SemanticError> {
    let end_t = base_t
        .checked_add(log.len())
        .ok_or_else(|| fault("recent/basis-overflow", "recent transaction basis overflow"))?;
    Ok(RecentStats {
        base_t,
        end_t,
        transactions: log.len(),
        datoms,
        encoded_bytes,
        accounted_bytes,
        base_hash,
        end_hash,
        needs_consolidation: datoms >= limits.soft_datoms || accounted_bytes >= limits.soft_bytes,
        raw_index_entries: indexes.entries(),
        index_nodes: indexes.nodes(),
        max_index_height: indexes.max_height(),
        log_chunks: log.chunks(),
    })
}

fn index_member(schema: &Schema, datom: &Datom, order: IndexOrder) -> Result<bool, SemanticError> {
    match order {
        IndexOrder::Eavt | IndexOrder::Aevt => Ok(true),
        IndexOrder::Avet => {
            let attribute = schema.attribute(datom.attribute)?;
            Ok(attribute.indexed || attribute.unique.is_some())
        }
        IndexOrder::Vaet => Ok(schema.attribute(datom.attribute)?.value_type == ValueType::Ref),
    }
}

fn validate_durable_range(
    order: IndexOrder,
    datoms: &[Datom],
    history: bool,
    base_t: u64,
) -> Result<(), SemanticError> {
    if datoms
        .windows(2)
        .any(|pair| !pair[0].cmp_in(&pair[1], order).is_lt())
    {
        return Err(SemanticError::incorrect(
            "recent/noncanonical-durable-range",
            "durable range must be strictly ordered in the requested index",
        ));
    }
    if !history && datoms.iter().any(|datom| !datom.added) {
        return Err(SemanticError::incorrect(
            "recent/retraction-in-current-base",
            "durable current range cannot contain retractions",
        ));
    }
    if datoms
        .iter()
        .any(|datom| !tx_to_t(datom.tx).is_ok_and(|t| t <= base_t))
    {
        return Err(SemanticError::incorrect(
            "recent/durable-beyond-base",
            "durable range contains a datom newer than the recent tier base",
        ));
    }
    validate_persistent_index_datoms(order, datoms)
}

fn enforce_hard_limit(
    datoms: u64,
    accounted_bytes: u64,
    limits: RecentLimits,
) -> Result<(), SemanticError> {
    if datoms > limits.hard_datoms || accounted_bytes > limits.hard_bytes {
        return Err(SemanticError::new(
            ErrorCategory::Busy,
            "recent/hard-capacity",
            format!(
                "uncovered recent tail requires {datoms} datoms/{accounted_bytes} accounted resident bytes, above hard limits {}/{}; consolidate before accepting more",
                limits.hard_datoms, limits.hard_bytes
            ),
        ));
    }
    Ok(())
}

fn checked_add(left: u64, right: u64, name: &str) -> Result<u64, SemanticError> {
    left.checked_add(right)
        .ok_or_else(|| fault("recent/count-overflow", format!("{name} overflow")))
}

fn same_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
}

fn stored_eav_cmp(left: &Datom, right: &Datom) -> Ordering {
    left.entity
        .cmp(&right.entity)
        .then(left.attribute.cmp(&right.attribute))
        .then_with(|| left.value.stored_cmp(&right.value))
}

fn primary_cmp_in(left: &Datom, right: &Datom, order: IndexOrder) -> Ordering {
    match order {
        IndexOrder::Eavt => left
            .entity
            .cmp(&right.entity)
            .then(left.attribute.cmp(&right.attribute))
            .then_with(|| left.value.stored_cmp(&right.value)),
        IndexOrder::Aevt => left
            .attribute
            .cmp(&right.attribute)
            .then(left.entity.cmp(&right.entity))
            .then_with(|| left.value.stored_cmp(&right.value)),
        IndexOrder::Avet => left
            .attribute
            .cmp(&right.attribute)
            .then_with(|| left.value.stored_cmp(&right.value))
            .then(left.entity.cmp(&right.entity)),
        IndexOrder::Vaet => left
            .value
            .stored_cmp(&right.value)
            .then(left.attribute.cmp(&right.attribute))
            .then(left.entity.cmp(&right.entity)),
    }
}

fn same_logical_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.index_cmp(&right.value) == Ordering::Equal
}

fn same_stored_datom(left: &Datom, right: &Datom) -> bool {
    same_eav(left, right) && left.tx == right.tx && left.added == right.added
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, Database, EntityRef, INITIAL_EIDX_FRONTIER, Keyword, Schema, TxOp,
        TxReport, TxValue, USER_PARTITION, Value, View, make_eid,
    };
    use std::collections::BTreeMap;

    const DATABASE_ID: &str = "recent-test";

    fn application_schema(no_history: bool) -> Schema {
        let mut schema = Schema::new();
        let mut indexed = Attribute::new(
            1_000,
            Keyword::new("item", "number"),
            ValueType::Long,
            Cardinality::One,
        );
        indexed.indexed = true;
        indexed.no_history = no_history;
        schema.install(indexed).unwrap();
        schema
            .install(Attribute::new(
                1_001,
                Keyword::new("item", "parent"),
                ValueType::Ref,
                Cardinality::One,
            ))
            .unwrap();
        schema
            .install(Attribute::new(
                1_002,
                Keyword::new("item", "note"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        schema
    }

    fn durable(report: &TxReport, previous_hash: Digest) -> (DurableTransaction, Digest) {
        let transaction = DurableTransaction {
            database_id: DATABASE_ID.into(),
            basis_t: report.db_after.basis_t(),
            previous_hash,
            eidx_frontier: report.db_after.eidx_frontier(),
            tempids: report.tempids.clone(),
            tx_data: report.tx_data.clone(),
        };
        let hash = transaction_hash(&encode_transaction(&transaction).unwrap());
        (transaction, hash)
    }

    fn sorted(mut datoms: Vec<Datom>, order: IndexOrder) -> Vec<Datom> {
        datoms.sort_by(|left, right| left.cmp_in(right, order));
        datoms
    }

    #[test]
    fn authenticated_tail_matches_pure_database_current_and_history_in_every_order() {
        let database = Database::new(application_schema(false)).unwrap();
        let seeded = database
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: 1_000,
                        value: TxValue::Scalar(Value::Long(1)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: 1_001,
                        value: TxValue::Entity(EntityRef::Temp("parent".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: 1_002,
                        value: TxValue::Scalar(Value::String("old".into())),
                    },
                ],
                10,
            )
            .unwrap();
        let entity = seeded.tempids["item"];
        let parent = seeded.tempids["parent"];
        let base = seeded.db_after;
        let base_hash = [0x42; 32];
        let mut previous_hash = base_hash;
        let mut database = base.clone();
        let mut transactions = Vec::new();

        for (instant, ops) in [
            (
                11,
                vec![TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(2)),
                }],
            ),
            (
                12,
                vec![TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: 1_001,
                    value: Some(TxValue::Entity(EntityRef::Id(parent))),
                }],
            ),
            (
                13,
                vec![TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_002,
                    value: TxValue::Scalar(Value::String("new".into())),
                }],
            ),
        ] {
            let report = database.with(&ops, instant).unwrap();
            let (transaction, hash) = durable(&report, previous_hash);
            previous_hash = hash;
            transactions.push(transaction);
            database = report.db_after;
        }

        let tier = RecentTier::new(
            DATABASE_ID,
            base.basis_t(),
            base_hash,
            transactions,
            EndpointProjection::new(database.schema().clone()),
            RecentLimits::default(),
        )
        .unwrap();
        assert_eq!(tier.stats().end_hash, previous_hash);

        // Current reads use the endpoint memory index built with the recent
        // value, rather than replaying this event stream on every seek. All
        // three base facts were touched; the endpoint contributes only the
        // replacement count/note assertions because the parent was retracted.
        assert!(
            base.datoms(View::Current, IndexOrder::Eavt)
                .iter()
                .filter(|datom| datom.entity == entity)
                .all(|datom| tier.touches_current(datom))
        );
        let recent_current = tier.current_datoms(IndexOrder::Eavt);
        assert_eq!(
            recent_current
                .iter()
                .filter(|datom| datom.entity == entity)
                .count(),
            2
        );
        assert!(
            recent_current
                .iter()
                .any(|datom| datom.value == Value::Long(2))
        );
        assert!(
            recent_current
                .iter()
                .any(|datom| datom.value == Value::String("new".into()))
        );
        assert!(tier.datoms(IndexOrder::Eavt).len() > recent_current.len());

        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let current = tier
                .merge_current_range(
                    order,
                    &base.datoms(View::Current, order),
                    &RecentRange::unbounded(),
                )
                .unwrap();
            assert_eq!(current, database.datoms(View::Current, order));

            let history = tier
                .merge_history_range(
                    order,
                    &base.datoms(View::History, order),
                    &RecentRange::unbounded(),
                )
                .unwrap();
            assert_eq!(history, database.datoms(View::History, order));
        }
    }

    #[test]
    fn setting_no_history_true_does_not_change_ordinary_history_reads() {
        let schema = application_schema(true);
        let base_hash = [0x33; 32];
        let entity = make_eid(USER_PARTITION, 1).unwrap();
        let durable_assertion = Datom {
            entity,
            attribute: 1_000,
            value: Value::Long(1),
            tx: t_to_tx(1).unwrap(),
            added: true,
        };
        let durable_retraction = Datom {
            tx: t_to_tx(2).unwrap(),
            added: false,
            ..durable_assertion.clone()
        };
        let still_current = Datom {
            entity: make_eid(USER_PARTITION, 2).unwrap(),
            value: Value::Long(2),
            tx: t_to_tx(3).unwrap(),
            ..durable_assertion.clone()
        };
        let durable_history = sorted(
            vec![
                durable_assertion.clone(),
                durable_retraction.clone(),
                still_current.clone(),
            ],
            IndexOrder::Eavt,
        );
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [],
            EndpointProjection::new(schema),
            RecentLimits::default(),
        )
        .unwrap();

        assert_eq!(
            tier.merge_history_range(
                IndexOrder::Eavt,
                &durable_history,
                &RecentRange::unbounded(),
            )
            .unwrap(),
            durable_history
        );
        // The storage hint is applied only when an indexing job explicitly
        // rebuilds an affected segment; merely reading history is exact.
    }

    #[test]
    fn consolidation_filters_all_adjacent_pairs_in_the_affected_merged_segment() {
        let schema = application_schema(true);
        let base_hash = [0x55; 32];
        let old_pair_assertion = Datom {
            entity: make_eid(USER_PARTITION, 1).unwrap(),
            attribute: 1_000,
            value: Value::Long(1),
            tx: t_to_tx(1).unwrap(),
            added: true,
        };
        let old_pair_retraction = Datom {
            tx: t_to_tx(2).unwrap(),
            added: false,
            ..old_pair_assertion.clone()
        };
        let newly_retracted_assertion = Datom {
            entity: make_eid(USER_PARTITION, 2).unwrap(),
            value: Value::Long(2),
            tx: t_to_tx(3).unwrap(),
            ..old_pair_assertion.clone()
        };
        let durable_history = sorted(
            vec![
                old_pair_assertion.clone(),
                old_pair_retraction.clone(),
                newly_retracted_assertion.clone(),
            ],
            IndexOrder::Eavt,
        );
        let recent_retraction = Datom {
            tx: t_to_tx(11).unwrap(),
            added: false,
            ..newly_retracted_assertion.clone()
        };
        let transaction = DurableTransaction {
            database_id: DATABASE_ID.into(),
            basis_t: 11,
            previous_hash: base_hash,
            eidx_frontier: INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::new(),
            tx_data: vec![recent_retraction.clone()],
        };
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [transaction],
            EndpointProjection::new(schema),
            RecentLimits::default(),
        )
        .unwrap();

        let visible = tier
            .merge_history_range(
                IndexOrder::Eavt,
                &durable_history,
                &RecentRange::unbounded(),
            )
            .unwrap();
        assert!(
            visible
                .iter()
                .any(|datom| same_stored_datom(datom, &recent_retraction))
        );
        assert!(
            visible
                .iter()
                .any(|datom| same_stored_datom(datom, &newly_retracted_assertion))
        );

        let pairs = tier
            .no_history_pairs(IndexOrder::Eavt, &durable_history)
            .unwrap();
        assert_eq!(pairs.len(), 2);
        assert!(pairs.iter().any(|pair| {
            same_stored_datom(&pair.retraction, &old_pair_retraction)
                && same_stored_datom(&pair.assertion, &old_pair_assertion)
        }));
        assert!(pairs.iter().any(|pair| {
            same_stored_datom(&pair.retraction, &recent_retraction)
                && same_stored_datom(&pair.assertion, &newly_retracted_assertion)
        }));
        let consolidated = tier
            .consolidate_history_range(
                IndexOrder::Eavt,
                &durable_history,
                &RecentRange::unbounded(),
            )
            .unwrap();
        assert!(consolidated.is_empty());
    }

    #[test]
    fn no_history_pair_uses_logical_value_comparison_across_the_base_boundary() {
        use bigdecimal::BigDecimal;
        use std::str::FromStr;

        let schema = application_schema(true);
        let base_hash = [0x56; 32];
        let durable_assertion = Datom {
            entity: make_eid(USER_PARTITION, 1).unwrap(),
            attribute: 1_000,
            value: Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
            tx: t_to_tx(3).unwrap(),
            added: true,
        };
        let recent_retraction = Datom {
            value: Value::BigDec(BigDecimal::from_str("1.0").unwrap()),
            tx: t_to_tx(11).unwrap(),
            added: false,
            ..durable_assertion.clone()
        };
        let transaction = DurableTransaction {
            database_id: DATABASE_ID.into(),
            basis_t: 11,
            previous_hash: base_hash,
            eidx_frontier: INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::new(),
            tx_data: vec![recent_retraction.clone()],
        };
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [transaction],
            EndpointProjection::new(schema),
            RecentLimits::default(),
        )
        .unwrap();

        let pairs = tier
            .no_history_pairs(IndexOrder::Eavt, std::slice::from_ref(&durable_assertion))
            .unwrap();
        assert_eq!(pairs.len(), 1);
        assert!(same_stored_datom(&pairs[0].retraction, &recent_retraction));
        assert!(same_stored_datom(&pairs[0].assertion, &durable_assertion));
    }

    #[test]
    fn no_history_false_resumes_pair_retention_at_the_next_indexing_job() {
        let schema = application_schema(false);
        let base_hash = [0x77; 32];
        let assertion = Datom {
            entity: make_eid(USER_PARTITION, 1).unwrap(),
            attribute: 1_000,
            value: Value::Long(1),
            tx: t_to_tx(1).unwrap(),
            added: true,
        };
        let retraction = Datom {
            tx: t_to_tx(11).unwrap(),
            added: false,
            ..assertion.clone()
        };
        let transaction = DurableTransaction {
            database_id: DATABASE_ID.into(),
            basis_t: 11,
            previous_hash: base_hash,
            eidx_frontier: INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::new(),
            tx_data: vec![retraction.clone()],
        };
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [transaction],
            EndpointProjection::new(schema),
            RecentLimits::default(),
        )
        .unwrap();
        let history = tier
            .consolidate_history_range(
                IndexOrder::Eavt,
                std::slice::from_ref(&assertion),
                &RecentRange::unbounded(),
            )
            .unwrap();
        assert_eq!(history.len(), 2);
        assert!(
            history
                .iter()
                .any(|datom| same_stored_datom(datom, &assertion))
        );
        assert!(
            history
                .iter()
                .any(|datom| same_stored_datom(datom, &retraction))
        );
        assert!(
            tier.no_history_pairs(IndexOrder::Eavt, std::slice::from_ref(&assertion))
                .unwrap()
                .is_empty()
        );
    }

    fn manual_transaction(
        basis_t: u64,
        previous_hash: Digest,
        database_id: &str,
        datoms: usize,
    ) -> DurableTransaction {
        let tx = t_to_tx(basis_t).unwrap();
        DurableTransaction {
            database_id: database_id.into(),
            basis_t,
            previous_hash,
            eidx_frontier: INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::new(),
            tx_data: (0..datoms)
                .map(|offset| Datom {
                    entity: make_eid(USER_PARTITION, offset as u64 + 1).unwrap(),
                    attribute: 1_000,
                    value: Value::Long(offset as i64),
                    tx,
                    added: true,
                })
                .collect(),
        }
    }

    #[test]
    fn gaps_hash_forks_and_database_substitution_fail() {
        let schema = application_schema(false);
        let limits = RecentLimits::default();
        let base_hash = [1; 32];
        let gap = manual_transaction(12, base_hash, DATABASE_ID, 1);
        assert_eq!(
            RecentTier::new(
                DATABASE_ID,
                10,
                base_hash,
                [gap],
                EndpointProjection::new(schema.clone()),
                limits,
            )
            .unwrap_err()
            .code,
            "recent/noncontiguous-basis"
        );
        let fork = manual_transaction(11, [9; 32], DATABASE_ID, 1);
        assert_eq!(
            RecentTier::new(
                DATABASE_ID,
                10,
                base_hash,
                [fork],
                EndpointProjection::new(schema.clone()),
                limits,
            )
            .unwrap_err()
            .code,
            "recent/hash-chain-mismatch"
        );
        let foreign = manual_transaction(11, base_hash, "other", 1);
        assert_eq!(
            RecentTier::new(
                DATABASE_ID,
                10,
                base_hash,
                [foreign],
                EndpointProjection::new(schema.clone()),
                limits,
            )
            .unwrap_err()
            .code,
            "recent/database-mismatch"
        );

        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [],
            EndpointProjection::new(schema),
            limits,
        )
        .unwrap();
        let beyond_base = Datom {
            entity: make_eid(USER_PARTITION, 1).unwrap(),
            attribute: 1_000,
            value: Value::Long(1),
            tx: t_to_tx(11).unwrap(),
            added: true,
        };
        assert_eq!(
            tier.merge_current_range(IndexOrder::Eavt, &[beyond_base], &RecentRange::unbounded(),)
                .unwrap_err()
                .code,
            "recent/durable-beyond-base"
        );
    }

    #[test]
    fn batch_extend_matches_repeated_append_without_intermediate_index_builds() {
        let schema = application_schema(false);
        let base_hash = [6; 32];
        let first = manual_transaction(11, base_hash, DATABASE_ID, 3);
        let first_hash = transaction_hash(&encode_transaction(&first).unwrap());
        let second = manual_transaction(12, first_hash, DATABASE_ID, 4);
        let empty = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [],
            EndpointProjection::new(schema.clone()),
            RecentLimits::default(),
        )
        .unwrap();
        let batch = empty
            .extend(
                [first.clone(), second.clone()],
                EndpointProjection::new(schema.clone()),
            )
            .unwrap();
        let repeated = empty
            .append(first, EndpointProjection::new(schema.clone()))
            .unwrap()
            .append(second, EndpointProjection::new(schema))
            .unwrap();
        assert_eq!(batch.stats(), repeated.stats());
        assert_eq!(batch.entries(), repeated.entries());
        assert_eq!(batch.index_builds(), 2);
        assert_eq!(repeated.index_builds(), 3);
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(batch.datoms(order), repeated.datoms(order));
        }
    }

    #[test]
    fn soft_threshold_requests_work_but_hard_capacity_preserves_old_value() {
        let limits = RecentLimits {
            soft_datoms: 1,
            soft_bytes: u64::MAX - 1,
            hard_datoms: 2,
            hard_bytes: u64::MAX,
        };
        let schema = application_schema(false);
        let base_hash = [7; 32];
        let first = manual_transaction(11, base_hash, DATABASE_ID, 1);
        let first_hash = transaction_hash(&encode_transaction(&first).unwrap());
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [first],
            EndpointProjection::new(schema.clone()),
            limits,
        )
        .unwrap();
        assert!(tier.needs_consolidation());
        assert_eq!(tier.stats().datoms, 1);
        assert_eq!(tier.stats().base_t, 10);
        assert_eq!(tier.stats().end_t, 11);
        assert_eq!(tier.stats().base_hash, base_hash);
        assert_eq!(tier.stats().end_hash, first_hash);

        let overflowing = manual_transaction(12, first_hash, DATABASE_ID, 2);
        let error = tier
            .append(overflowing, EndpointProjection::new(schema))
            .unwrap_err();
        assert_eq!(error.category, ErrorCategory::Busy);
        assert_eq!(error.code, "recent/hard-capacity");
        assert_eq!(tier.stats().datoms, 1);
        assert_eq!(tier.entries().len(), 1);
    }

    #[test]
    fn resident_accounting_prevents_compact_tuple_encoding_from_evading_capacity() {
        let schema = application_schema(false);
        let base_hash = [0x7a; 32];
        let mut transaction = manual_transaction(11, base_hash, DATABASE_ID, 1);
        transaction.tx_data[0].value = Value::Tuple(vec![None; 2_048]);
        let encoded_bytes = encode_transaction(&transaction).unwrap().len() as u64;
        let limits = RecentLimits {
            soft_datoms: u64::MAX,
            soft_bytes: encoded_bytes.saturating_add(1),
            hard_datoms: u64::MAX,
            hard_bytes: encoded_bytes.saturating_add(1),
        };

        let error = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [transaction],
            EndpointProjection::new(schema),
            limits,
        )
        .unwrap_err();
        assert_eq!(error.code, "recent/hard-capacity");
    }

    #[test]
    fn raw_recent_indexes_apply_endpoint_avet_and_vaet_membership() {
        let schema = application_schema(false);
        let base_hash = [4; 32];
        let tx = t_to_tx(11).unwrap();
        let entity = make_eid(USER_PARTITION, 1).unwrap();
        let reference = make_eid(USER_PARTITION, 2).unwrap();
        let transaction = DurableTransaction {
            database_id: DATABASE_ID.into(),
            basis_t: 11,
            previous_hash: base_hash,
            eidx_frontier: INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::new(),
            tx_data: vec![
                Datom {
                    entity,
                    attribute: 1_000,
                    value: Value::Long(1),
                    tx,
                    added: true,
                },
                Datom {
                    entity,
                    attribute: 1_001,
                    value: Value::Ref(reference),
                    tx,
                    added: true,
                },
                Datom {
                    entity,
                    attribute: 1_002,
                    value: Value::String("not indexed".into()),
                    tx,
                    added: true,
                },
            ],
        };
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [transaction],
            EndpointProjection::new(schema),
            RecentLimits::default(),
        )
        .unwrap();
        assert_eq!(tier.datoms(IndexOrder::Eavt).len(), 3);
        assert_eq!(tier.datoms(IndexOrder::Aevt).len(), 3);
        assert_eq!(tier.datoms(IndexOrder::Avet).len(), 1);
        assert_eq!(tier.datoms(IndexOrder::Avet)[0].attribute, 1_000);
        assert_eq!(tier.datoms(IndexOrder::Vaet).len(), 1);
        assert_eq!(tier.datoms(IndexOrder::Vaet)[0].attribute, 1_001);
    }

    #[test]
    fn bounds_are_applied_after_retraction_collapse() {
        let schema = application_schema(false);
        let base_hash = [8; 32];
        let old = Datom {
            entity: make_eid(USER_PARTITION, 1).unwrap(),
            attribute: 1_000,
            value: Value::Long(1),
            tx: t_to_tx(1).unwrap(),
            added: true,
        };
        let retract = DurableTransaction {
            database_id: DATABASE_ID.into(),
            basis_t: 11,
            previous_hash: base_hash,
            eidx_frontier: INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::new(),
            tx_data: vec![Datom {
                tx: t_to_tx(11).unwrap(),
                added: false,
                ..old.clone()
            }],
        };
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [retract],
            EndpointProjection::new(schema),
            RecentLimits::default(),
        )
        .unwrap();
        let end = Datom {
            entity: make_eid(USER_PARTITION, 2).unwrap(),
            ..old.clone()
        };
        let range = RecentRange::new(Some(old.clone()), Some(end));
        let result = tier
            .merge_current_range(IndexOrder::Eavt, std::slice::from_ref(&old), &range)
            .unwrap();
        assert!(result.is_empty());
    }

    #[test]
    fn arc_backed_entries_and_owned_results_survive_successor_values() {
        let schema = application_schema(false);
        let base_hash = [6; 32];
        let first = manual_transaction(11, base_hash, DATABASE_ID, 1);
        let first_hash = transaction_hash(&encode_transaction(&first).unwrap());
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [first],
            EndpointProjection::new(schema.clone()),
            RecentLimits::default(),
        )
        .unwrap();
        let held_entries = tier.entries();
        let held_datoms = tier.datoms(IndexOrder::Eavt);
        let owned = tier
            .merge_current_range(IndexOrder::Eavt, &[], &RecentRange::unbounded())
            .unwrap();
        let successor = tier
            .append(
                manual_transaction(12, first_hash, DATABASE_ID, 1),
                EndpointProjection::new(schema),
            )
            .unwrap();
        drop(tier);
        assert_eq!(held_entries.len(), 1);
        assert_eq!(held_datoms.len(), 1);
        assert_eq!(owned.len(), 1);
        assert_eq!(successor.entries().len(), 2);
    }

    fn unique_transaction(
        basis_t: u64,
        previous_hash: Digest,
        entity_offset: u64,
    ) -> DurableTransaction {
        let tx = t_to_tx(basis_t).unwrap();
        DurableTransaction {
            database_id: DATABASE_ID.into(),
            basis_t,
            previous_hash,
            eidx_frontier: INITIAL_EIDX_FRONTIER.max(entity_offset + 2),
            tempids: BTreeMap::new(),
            tx_data: vec![Datom {
                entity: make_eid(USER_PARTITION, entity_offset + 1).unwrap(),
                attribute: 1_000,
                value: Value::Long(entity_offset as i64),
                tx,
                added: true,
            }],
        }
    }

    #[test]
    fn sequential_successors_do_bounded_path_work_and_share_old_structure() {
        let schema = application_schema(false);
        let base_hash = [0xa1; 32];
        let mut tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            [],
            EndpointProjection::new(schema.clone()),
            RecentLimits::default(),
        )
        .unwrap();
        let mut previous_hash = base_hash;
        let mut snapshot_at_chunk_boundary = None;
        let mut total_visits = 0_u64;
        for offset in 0..2048_u64 {
            let transaction = unique_transaction(11 + offset, previous_hash, offset);
            previous_hash = transaction_hash(&encode_transaction(&transaction).unwrap());
            let predecessor = tier.clone();
            tier = tier
                .append(transaction, EndpointProjection::new(schema.clone()))
                .unwrap();
            let work = tier.last_work();
            assert_eq!(work.authenticated_datoms, 1);
            assert_eq!(work.bulk_rebuild_datoms, 0);
            assert_eq!(work.index_insert_attempts, 3);
            assert!(work.log_entries_copied < LOG_CHUNK_SIZE as u64);
            assert!(work.node_visits <= 3 * u64::from(tier.stats().max_index_height.max(1)));
            assert!(work.nodes_copied <= 6 * u64::from(tier.stats().max_index_height.max(1)) + 6);
            total_visits = total_visits.saturating_add(work.node_visits);

            // The predecessor remains an exact database value after extension.
            assert_eq!(predecessor.stats().end_t + 1, tier.stats().end_t);
            assert_eq!(predecessor.stats().datoms + 1, tier.stats().datoms);
            if offset == 31 {
                snapshot_at_chunk_boundary = Some(tier.clone());
            }
            if offset == 32 {
                let old = snapshot_at_chunk_boundary.as_ref().unwrap();
                let new_head = tier.log.head.as_ref().unwrap();
                assert!(Arc::ptr_eq(
                    new_head.previous.as_ref().unwrap(),
                    old.log.head.as_ref().unwrap()
                ));
            }
        }
        // A full-prefix rebuild would be quadratic. The 16-way tree visits at
        // most three root-to-leaf paths per one-datom transaction.
        let logarithmic_bound = 3 * 2048 * 5;
        assert!(total_visits <= logarithmic_bound);
        assert_eq!(tier.stats().datoms, 2048);
        assert_eq!(tier.stats().raw_index_entries, 3 * 2048);

        let key = unique_transaction(1035, [0; 32], 1024).tx_data.remove(0);
        let mut cursor = tier
            .cursor(
                true,
                IndexOrder::Eavt,
                &RecentRange::new(Some(key.clone()), None),
            )
            .unwrap();
        assert_eq!(cursor.next().unwrap().entity, key.entity);
        let cursor_stats = cursor.stats();
        assert!(cursor_stats.node_visits <= 2 * u64::from(tier.stats().max_index_height) + 1);
        assert_eq!(cursor_stats.datoms_examined, 1);
    }

    #[test]
    fn avet_enable_backfills_only_the_affected_aevt_slice() {
        let mut unindexed = application_schema(false);
        let mut attribute = unindexed.attribute(1_000).unwrap().clone();
        attribute.indexed = false;
        unindexed.alter(attribute).unwrap();
        let base_hash = [0xa2; 32];
        let mut transactions = Vec::new();
        let mut previous_hash = base_hash;
        for offset in 0..96_u64 {
            let transaction = unique_transaction(11 + offset, previous_hash, offset);
            previous_hash = transaction_hash(&encode_transaction(&transaction).unwrap());
            transactions.push(transaction);
        }
        let tier = RecentTier::new(
            DATABASE_ID,
            10,
            base_hash,
            transactions,
            EndpointProjection::new(unindexed.clone()),
            RecentLimits::default(),
        )
        .unwrap();
        assert!(tier.datoms(IndexOrder::Avet).is_empty());
        let eavt_ids = tier.indexes.eavt.node_ids();
        let aevt_ids = tier.indexes.aevt.node_ids();

        let mut indexed = unindexed.clone();
        let mut attribute = indexed.attribute(1_000).unwrap().clone();
        attribute.indexed = true;
        indexed.alter(attribute).unwrap();
        let enabled = tier
            .extend([], EndpointProjection::new(indexed.clone()))
            .unwrap();
        assert_eq!(enabled.last_work().authenticated_datoms, 0);
        assert_eq!(enabled.last_work().bulk_rebuild_datoms, 0);
        assert_eq!(enabled.last_work().schema_backfill_datoms, 96);
        assert_eq!(enabled.datoms(IndexOrder::Avet).len(), 96);
        assert_eq!(enabled.indexes.eavt.node_ids(), eavt_ids);
        assert_eq!(enabled.indexes.aevt.node_ids(), aevt_ids);

        let dropped = enabled
            .extend([], EndpointProjection::new(unindexed))
            .unwrap();
        assert!(dropped.datoms(IndexOrder::Avet).is_empty());
        assert_eq!(dropped.last_work().schema_backfill_datoms, 0);
        // Re-enabling scans only this attribute. Physical entries are a set,
        // so the source-shaped retained AVET nodes do not duplicate events.
        let reenabled = dropped
            .extend([], EndpointProjection::new(indexed))
            .unwrap();
        assert_eq!(reenabled.last_work().schema_backfill_datoms, 96);
        assert_eq!(reenabled.datoms(IndexOrder::Avet).len(), 96);
    }
}
