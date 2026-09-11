use crate::collections::persistent_map::SharedMap;
use crate::index::compare_prefix;
use crate::index::overlay::{EavSet, OverlayIndexCursor, OverlayIndexes};
use crate::model::identity::validate_frontier;
use crate::{
    AttributeName, DB_IDENT, Database, Datom, EntityIdentifier, ErrorCategory, IndexBoundary,
    IndexComponents, IndexOrder, IndexPrefix, IndexTransaction, Keyword, PeerCursorStats,
    PeerSnapshot, Schema, SemanticError, TimePoint, TupleSpec, Value, ValueType, eid_to_eidx,
    schema_eid_to_attr_id, tx_to_t,
};
use std::cmp::Ordering;
use std::collections::{BTreeMap, VecDeque};
use std::fmt;
use std::iter::{Cloned, Rev};
use std::slice::Iter;
use std::sync::{Arc, Mutex, OnceLock};
use std::vec::IntoIter;

type ReadFilter = dyn Fn(&DatabaseValue, &Datom) -> bool + Send + Sync;

/// A value component accepted at the raw-index API boundary.
///
/// `Stored` keeps an ordinary value unambiguous, while `Entity` requests
/// ident or lookup-ref resolution against this exact immutable database
/// value. `Tuple` permits that same distinction recursively in ref-typed
/// tuple slots; `None` slots remain tuple nils. Read boundaries deliberately
/// have no tempid form. Tuple boundaries may contain a leading subset of the
/// schema's slots, including an empty subset. They are virtual positions, not
/// stored assertions: a shorter tuple sorts before its longer extensions.
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum RawIndexValue {
    Stored(Value),
    Entity(EntityIdentifier),
    Tuple(Vec<Option<RawIndexValue>>),
}

impl From<Value> for RawIndexValue {
    fn from(value: Value) -> Self {
        Self::Stored(value)
    }
}

impl From<EntityIdentifier> for RawIndexValue {
    fn from(identifier: EntityIdentifier) -> Self {
        Self::Entity(identifier)
    }
}

const MAX_TRANSACTION_PREFIX_MEMO_ENTRIES: usize = 4_096;
const MAX_TRANSACTION_PREFIX_MEMO_BYTES: u64 = 64 * 1024 * 1024;

/// Exact logical database work delivered through transaction read APIs.
///
/// This deliberately does not infer work from persistent-tree node loads:
/// cache hits still deliver datoms, while one loaded leaf can contain many
/// datoms outside the requested range. The writer carries one observer across
/// generation, assessment, validation, and successor construction; the
/// attempt-local prefix memo prevents repeated source deliveries while replay
/// remains logically charged.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct LogicalReadWork {
    pub(crate) datoms: u64,
    pub(crate) retained_bytes: u64,
}

/// Transaction-attempt-local read evidence.
///
/// `logical_*` counts every datom delivered to a transaction phase, including
/// memo replays. `source_*` counts successful datoms delivered by an
/// underlying exact-prefix cursor on cache misses. The latter is deliberately
/// a semantic source-delivery counter rather than a PostgreSQL page-I/O
/// counter: a tree leaf may contain candidates outside the requested prefix.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct TransactionReadWork {
    pub(crate) logical_datoms: u64,
    pub(crate) logical_retained_bytes: u64,
    pub(crate) source_datoms: u64,
    pub(crate) source_retained_bytes: u64,
    pub(crate) prefix_hits: u64,
    pub(crate) prefix_misses: u64,
    pub(crate) memo_admissions: u64,
    pub(crate) memo_rejections: u64,
    pub(crate) memo_peak_entries: usize,
    pub(crate) memo_peak_retained_bytes: u64,
    pub(crate) native_cursor_ranges: u64,
    pub(crate) native_cache_hits: u64,
    pub(crate) native_cache_misses: u64,
    pub(crate) native_sql_root_reads: u64,
    pub(crate) native_sql_directory_reads: u64,
    pub(crate) native_sql_leaf_reads: u64,
    pub(crate) native_sql_reads: u64,
    pub(crate) native_sql_read_bytes: u64,
    pub(crate) native_recent_datoms_examined: u64,
    pub(crate) native_recent_datoms_yielded: u64,
}

/// Stable identity of one immutable database value/view while it participates
/// in a transaction attempt. Cache keys retain this allocation, so pointer
/// identity cannot be recycled while an entry remains live.
#[derive(Debug)]
struct ReadValueIdentity;

#[derive(Clone, Debug)]
struct PrefixMemoKey {
    value: Arc<ReadValueIdentity>,
    history: bool,
    prefix: IndexPrefix,
}

impl PrefixMemoKey {
    /// Stable retained-memory admission weight for the owned map key.
    ///
    /// The enum and its inline fields are covered by `size_of`; owned value
    /// payloads are charged recursively. B-tree node and allocator overhead
    /// remain intentionally outside this allocator-independent estimate.
    fn retained_bytes(&self) -> u64 {
        let value_heap = match &self.prefix {
            IndexPrefix::Eavt { value, .. }
            | IndexPrefix::Aevt { value, .. }
            | IndexPrefix::Avet { value, .. } => {
                value.as_ref().map_or(0, Value::retained_heap_bytes)
            }
            IndexPrefix::Vaet { value, .. } => value.retained_heap_bytes(),
        };
        (std::mem::size_of::<Self>() as u64).saturating_add(value_heap)
    }
}

impl PartialEq for PrefixMemoKey {
    fn eq(&self, other: &Self) -> bool {
        Arc::ptr_eq(&self.value, &other.value)
            && self.history == other.history
            && self.prefix == other.prefix
    }
}

impl Eq for PrefixMemoKey {}

impl PartialOrd for PrefixMemoKey {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for PrefixMemoKey {
    fn cmp(&self, other: &Self) -> Ordering {
        let left = Arc::as_ptr(&self.value) as usize;
        let right = Arc::as_ptr(&other.value) as usize;
        left.cmp(&right)
            .then_with(|| self.history.cmp(&other.history))
            .then_with(|| self.prefix.cmp(&other.prefix))
    }
}

#[derive(Default)]
struct PrefixMemoState {
    entries: BTreeMap<PrefixMemoKey, Arc<[Datom]>>,
    retained_bytes: u64,
    source_datoms: u64,
    source_retained_bytes: u64,
    prefix_hits: u64,
    prefix_misses: u64,
    admissions: u64,
    rejections: u64,
    peak_entries: usize,
    peak_retained_bytes: u64,
    native_cursor_ranges: u64,
    native_cache_hits: u64,
    native_cache_misses: u64,
    native_sql_root_reads: u64,
    native_sql_directory_reads: u64,
    native_sql_leaf_reads: u64,
    native_sql_reads: u64,
    native_sql_read_bytes: u64,
    native_recent_datoms_examined: u64,
    native_recent_datoms_yielded: u64,
}

/// One bounded exact-prefix memo shared by all phases of a transaction
/// attempt. It is discardable acceleration only; immutable database values
/// and their observer remain the semantic boundary.
pub(crate) struct TransactionReadContext {
    observer: Arc<LogicalReadObserver>,
    memo: Mutex<PrefixMemoState>,
    max_entries: usize,
    max_retained_bytes: u64,
    max_entry_datoms: u64,
}

impl TransactionReadContext {
    pub(crate) fn new(max_datoms: u64, max_retained_bytes: u64) -> Self {
        Self {
            observer: Arc::new(LogicalReadObserver::new(max_datoms, max_retained_bytes)),
            memo: Mutex::new(PrefixMemoState::default()),
            max_entries: usize::try_from(max_datoms)
                .unwrap_or(usize::MAX)
                .min(MAX_TRANSACTION_PREFIX_MEMO_ENTRIES),
            max_retained_bytes: max_retained_bytes.min(MAX_TRANSACTION_PREFIX_MEMO_BYTES),
            max_entry_datoms: max_datoms,
        }
    }

    pub(crate) fn remaining(&self) -> Result<LogicalReadWork, SemanticError> {
        self.observer.remaining()
    }

    pub(crate) fn snapshot(&self) -> Result<TransactionReadWork, SemanticError> {
        let logical = self.observer.snapshot()?;
        let memo = self.memo.lock().map_err(|_| read_context_poisoned())?;
        Ok(TransactionReadWork {
            logical_datoms: logical.datoms,
            logical_retained_bytes: logical.retained_bytes,
            source_datoms: memo.source_datoms,
            source_retained_bytes: memo.source_retained_bytes,
            prefix_hits: memo.prefix_hits,
            prefix_misses: memo.prefix_misses,
            memo_admissions: memo.admissions,
            memo_rejections: memo.rejections,
            memo_peak_entries: memo.peak_entries,
            memo_peak_retained_bytes: memo.peak_retained_bytes,
            native_cursor_ranges: memo.native_cursor_ranges,
            native_cache_hits: memo.native_cache_hits,
            native_cache_misses: memo.native_cache_misses,
            native_sql_root_reads: memo.native_sql_root_reads,
            native_sql_directory_reads: memo.native_sql_directory_reads,
            native_sql_leaf_reads: memo.native_sql_leaf_reads,
            native_sql_reads: memo.native_sql_reads,
            native_sql_read_bytes: memo.native_sql_read_bytes,
            native_recent_datoms_examined: memo.native_recent_datoms_examined,
            native_recent_datoms_yielded: memo.native_recent_datoms_yielded,
        })
    }

    pub(crate) fn observer(&self) -> Arc<LogicalReadObserver> {
        Arc::clone(&self.observer)
    }

    fn lookup(&self, key: &PrefixMemoKey) -> Result<Option<Arc<[Datom]>>, SemanticError> {
        let mut memo = self.memo.lock().map_err(|_| read_context_poisoned())?;
        let found = memo.entries.get(key).cloned();
        if found.is_some() {
            memo.prefix_hits = memo.prefix_hits.saturating_add(1);
        } else {
            memo.prefix_misses = memo.prefix_misses.saturating_add(1);
        }
        Ok(found)
    }

    fn record_source_datom(&self, datom: &Datom) -> Result<(), SemanticError> {
        let mut memo = self.memo.lock().map_err(|_| read_context_poisoned())?;
        memo.source_datoms = memo.source_datoms.saturating_add(1);
        memo.source_retained_bytes = memo
            .source_retained_bytes
            .saturating_add(datom.retained_bytes());
        Ok(())
    }

    /// Record the physical work of one native cursor in this transaction
    /// attempt. Unlike shared peer load counters, this attribution cannot be
    /// contaminated by concurrent readers using the same immutable core.
    pub(crate) fn record_native_cursor(&self, stats: PeerCursorStats) -> Result<(), SemanticError> {
        let mut memo = self.memo.lock().map_err(|_| read_context_poisoned())?;
        memo.native_cursor_ranges = memo.native_cursor_ranges.saturating_add(1);
        memo.native_cache_hits = memo.native_cache_hits.saturating_add(stats.tree.cache_hits);
        memo.native_cache_misses = memo
            .native_cache_misses
            .saturating_add(stats.tree.cache_misses);
        memo.native_sql_root_reads = memo
            .native_sql_root_reads
            .saturating_add(stats.tree.root_reads);
        memo.native_sql_directory_reads = memo
            .native_sql_directory_reads
            .saturating_add(stats.tree.directory_reads);
        memo.native_sql_leaf_reads = memo
            .native_sql_leaf_reads
            .saturating_add(stats.tree.leaf_reads);
        let sql_reads = stats
            .tree
            .root_reads
            .saturating_add(stats.tree.directory_reads)
            .saturating_add(stats.tree.leaf_reads);
        memo.native_sql_reads = memo.native_sql_reads.saturating_add(sql_reads);
        memo.native_sql_read_bytes = memo
            .native_sql_read_bytes
            .saturating_add(stats.tree.decoded_bytes);
        memo.native_recent_datoms_examined = memo
            .native_recent_datoms_examined
            .saturating_add(stats.recent.datoms_examined);
        memo.native_recent_datoms_yielded = memo
            .native_recent_datoms_yielded
            .saturating_add(stats.recent.datoms_yielded);
        Ok(())
    }

    fn admit(
        &self,
        key: PrefixMemoKey,
        datoms: Vec<Datom>,
        retained_bytes: u64,
    ) -> Result<(), SemanticError> {
        let mut memo = self.memo.lock().map_err(|_| read_context_poisoned())?;
        if memo.entries.contains_key(&key) {
            return Ok(());
        }
        let next_bytes = memo.retained_bytes.checked_add(retained_bytes);
        if memo.entries.len() >= self.max_entries
            || next_bytes.is_none_or(|bytes| bytes > self.max_retained_bytes)
        {
            memo.rejections = memo.rejections.saturating_add(1);
            return Ok(());
        }
        memo.retained_bytes = next_bytes.expect("checked above");
        memo.entries.insert(key, datoms.into());
        memo.admissions = memo.admissions.saturating_add(1);
        memo.peak_entries = memo.peak_entries.max(memo.entries.len());
        memo.peak_retained_bytes = memo.peak_retained_bytes.max(memo.retained_bytes);
        Ok(())
    }

    fn reject(&self) -> Result<(), SemanticError> {
        let mut memo = self.memo.lock().map_err(|_| read_context_poisoned())?;
        memo.rejections = memo.rejections.saturating_add(1);
        Ok(())
    }
}

pub(crate) struct LogicalReadObserver {
    max_datoms: u64,
    max_retained_bytes: u64,
    work: Mutex<LogicalReadWork>,
}

impl LogicalReadObserver {
    pub(crate) fn new(max_datoms: u64, max_retained_bytes: u64) -> Self {
        Self {
            max_datoms,
            max_retained_bytes,
            work: Mutex::new(LogicalReadWork::default()),
        }
    }

    pub(crate) fn snapshot(&self) -> Result<LogicalReadWork, SemanticError> {
        self.work
            .lock()
            .map(|work| *work)
            .map_err(|_| read_observer_poisoned())
    }

    pub(crate) fn remaining(&self) -> Result<LogicalReadWork, SemanticError> {
        let work = self.snapshot()?;
        Ok(LogicalReadWork {
            datoms: self.max_datoms.saturating_sub(work.datoms),
            retained_bytes: self.max_retained_bytes.saturating_sub(work.retained_bytes),
        })
    }

    pub(crate) fn charge_datom(&self, datom: &Datom) -> Result<(), SemanticError> {
        self.charge(LogicalReadWork {
            datoms: 1,
            retained_bytes: datom.retained_bytes(),
        })
    }

    fn charge(&self, additional: LogicalReadWork) -> Result<(), SemanticError> {
        let mut work = self.work.lock().map_err(|_| read_observer_poisoned())?;
        let next_datoms = work
            .datoms
            .checked_add(additional.datoms)
            .ok_or_else(read_work_overflow)?;
        let next_bytes = work
            .retained_bytes
            .checked_add(additional.retained_bytes)
            .ok_or_else(read_work_overflow)?;
        if next_datoms > self.max_datoms || next_bytes > self.max_retained_bytes {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "transaction/read-capacity",
                format!(
                    "transaction reads exceed {} logical datoms or {} retained bytes",
                    self.max_datoms, self.max_retained_bytes
                ),
            ));
        }
        work.datoms = next_datoms;
        work.retained_bytes = next_bytes;
        Ok(())
    }
}

fn read_work_overflow() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Busy,
        "transaction/read-capacity",
        "transaction logical read accounting overflowed",
    )
}

fn read_observer_poisoned() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Fault,
        "transaction/read-observer-poisoned",
        "transaction logical read observer mutex was poisoned",
    )
}

fn read_context_poisoned() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Fault,
        "transaction/read-context-poisoned",
        "transaction prefix memo mutex was poisoned",
    )
}

/// One immutable value lineage's derived transaction-instant coordinate.
///
/// Stable Rust does not yet expose fallible `OnceLock` initialization, so the
/// small initialization mutex preserves retry-after-error behavior while the
/// populated fast path remains lock-free. This is a discardable memo, not
/// mutable database state.
#[derive(Debug)]
struct LastTxInstantMemo {
    value: OnceLock<Option<i64>>,
    initialization: Mutex<()>,
}

impl LastTxInstantMemo {
    fn empty() -> Self {
        Self {
            value: OnceLock::new(),
            initialization: Mutex::new(()),
        }
    }

    fn seeded(value: Option<i64>) -> Self {
        Self {
            value: OnceLock::from(value),
            initialization: Mutex::new(()),
        }
    }

    fn get_or_try_init(
        &self,
        initialize: impl FnOnce() -> Result<Option<i64>, SemanticError>,
    ) -> Result<Option<i64>, SemanticError> {
        if let Some(value) = self.value.get() {
            return Ok(*value);
        }
        let _initialization = self.initialization.lock().map_err(|_| {
            SemanticError::new(
                ErrorCategory::Fault,
                "database/tx-instant-memo-poisoned",
                "transaction-instant memo initialization mutex was poisoned",
            )
        })?;
        if let Some(value) = self.value.get() {
            return Ok(*value);
        }
        let value = initialize()?;
        self.value.set(value).map_err(|_| {
            SemanticError::new(
                ErrorCategory::Fault,
                "database/tx-instant-memo-race",
                "transaction-instant memo was initialized outside its serialization boundary",
            )
        })?;
        Ok(value)
    }
}

/// One exact immutable database value used by read-side APIs.
///
/// Its basis may be an eager memory value, an immutable block snapshot, or a
/// speculative delta. Each representation supports the same selective reads
/// and transaction assessor.
/// Temporal and custom filters belong to this value, so every consumer sees
/// the same information instead of receiving a detached `View` hint.
#[derive(Clone)]
pub struct DatabaseValue {
    basis: ReadBasis,
    pub(crate) entity_origin: crate::entity_identity::DatabaseOrigin,
    read_identity: Arc<ReadValueIdentity>,
    last_tx_instant_memo: Arc<LastTxInstantMemo>,
    as_of_t: Option<u64>,
    since_t: Option<u64>,
    history: bool,
    filters: Arc<[Arc<ReadFilter>]>,
    read_observer: Option<Arc<LogicalReadObserver>>,
    read_context: Option<Arc<TransactionReadContext>>,
    // A speculative binding has no durable generation reference. Keep its
    // authenticated dependency closure alive independently of cache eviction
    // and program garbage collection. Branches share immutable lookup paths.
    programs: SharedMap<crate::ProgramHash, Arc<crate::program::ValidatedProgram>>,
}

#[derive(Clone)]
enum ReadBasis {
    Eager(Arc<Database>),
    // Values and lazy entities clone a shared immutable handle, not the
    // snapshot's reader/cache configuration on every navigation step.
    Block(Arc<crate::storage::BlockSnapshot>),
    TransactionOverlay(Arc<TransactionOverlay>),
}

/// One immutable speculative db-after over an exact committed base.
///
/// Path-copied indexes share canonical speculative datoms and ident metadata.
/// Prefix reads merge a selective index range with the committed base; no
/// complete durable index is retained or materialized. Every branch references
/// that same base directly, so cursor/drop depth is bounded by tree height.
#[derive(Clone)]
struct TransactionOverlay {
    base: DatabaseValue,
    indexes: OverlayIndexes,
    schema: Arc<Schema>,
    newly_enabled_avet: Arc<[u32]>,
    basis_t: u64,
    eidx_frontier: u64,
    last_tx_instant: i64,
    reserved_allocation: Option<crate::reserved_allocation::ReservedAllocation>,
}

/// The result of a pure transaction. These values never advance a connection
/// or create a durable transaction receipt.
#[derive(Clone, Debug)]
pub struct SpeculativeTransactionReport {
    pub db_before: DatabaseValue,
    pub db_after: DatabaseValue,
    pub tx_data: Vec<Datom>,
    pub tempids: BTreeMap<String, u64>,
}

/// A fallible forward scan of one exact immutable database value.
///
/// Native scans retain the lazy persistent-tree/recent-tier merge; an
/// assessment overlay adds only its bounded transaction delta; temporal and
/// custom windows consume and collapse history incrementally. Every yielded
/// datom is owned, so advancing the connection or evicting a tree node cannot
/// mutate an already observed result.
pub struct DatabaseValueScanCursor<'a> {
    inner: DatabaseValueScanCursorInner<'a>,
    operation: Option<crate::sql_io::OperationContext>,
    observer: Option<Arc<LogicalReadObserver>>,
    physical_context: Option<Arc<TransactionReadContext>>,
    physical_recorded: bool,
    failed: bool,
}

enum DatabaseValueScanCursorInner<'a> {
    Block(Box<crate::storage::BlockIndexCursor>),
    Overlay(Box<TransactionOverlayScanCursor<'a>>),
    Eager(Cloned<Iter<'a, Datom>>),
    EagerReverse(Cloned<Rev<Iter<'a, Datom>>>),
    Window(Box<DatabaseValueWindowCursor<'a>>),
}

/// Cooperative controls belong below a temporal/custom window: one request
/// for a visible datom may otherwise examine an unbounded rejected prefix.
/// A stopped range is not a completed prefix and must never enter its memo.
struct ReadCursorControl<'a> {
    check: &'a mut dyn FnMut(Option<&Datom>) -> Result<bool, SemanticError>,
    stopped: bool,
}

impl ReadCursorControl<'_> {
    fn check(&mut self, datom: Option<&Datom>) -> Result<bool, SemanticError> {
        if self.stopped {
            return Ok(false);
        }
        let proceed = (self.check)(datom)?;
        self.stopped = !proceed;
        Ok(proceed)
    }

    fn accept(
        &mut self,
        item: Option<Result<Datom, SemanticError>>,
    ) -> Option<Result<Datom, SemanticError>> {
        match item {
            Some(Ok(datom)) => match self.check(Some(&datom)) {
                Ok(true) => Some(Ok(datom)),
                Ok(false) => None,
                Err(error) => Some(Err(error)),
            },
            item => item,
        }
    }
}

impl DatabaseValueScanCursor<'_> {
    /// Poll before source advancement (`None`) and inspect each ordered source
    /// candidate (`Some`) before view filters/collapse. False ends this cursor
    /// without claiming that its underlying source prefix was exhausted.
    pub(crate) fn next_with_control(
        &mut self,
        check: &mut dyn FnMut(Option<&Datom>) -> Result<bool, SemanticError>,
    ) -> Option<Result<Datom, SemanticError>> {
        self.next_controlled(&mut ReadCursorControl {
            check,
            stopped: false,
        })
    }

    fn record_physical_work(&mut self) -> Result<(), SemanticError> {
        if self.physical_recorded {
            return Ok(());
        }
        self.physical_recorded = true;
        if let (Some(context), DatabaseValueScanCursorInner::Block(cursor)) =
            (&self.physical_context, &self.inner)
        {
            context.record_native_cursor(cursor.stats())?;
        }
        Ok(())
    }

    fn next_controlled(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Option<Result<Datom, SemanticError>> {
        let operation = self.operation.clone();
        let _scope = operation
            .as_ref()
            .map(crate::sql_io::OperationContext::enter);
        if self.failed {
            return None;
        }
        match control.check(None) {
            Ok(true) => {}
            Ok(false) => {
                self.failed = true;
                let _ = self.record_physical_work();
                return None;
            }
            Err(error) => {
                self.failed = true;
                let _ = self.record_physical_work();
                return Some(Err(error));
            }
        }
        let window = matches!(&self.inner, DatabaseValueScanCursorInner::Window(_));
        let item = match &mut self.inner {
            DatabaseValueScanCursorInner::Block(cursor) => {
                cursor.next_with_poll(&mut || control.check(None))
            }
            DatabaseValueScanCursorInner::Overlay(cursor) => cursor.next_controlled(control),
            DatabaseValueScanCursorInner::Eager(cursor) => cursor.next().map(Ok),
            DatabaseValueScanCursorInner::EagerReverse(cursor) => cursor.next().map(Ok),
            DatabaseValueScanCursorInner::Window(cursor) => cursor.next_controlled(control),
        };
        let item = if window { item } else { control.accept(item) };
        self.failed |= control.stopped;
        let Some(item) = item else {
            if let Err(error) = self.record_physical_work() {
                self.failed = true;
                return Some(Err(error));
            }
            return None;
        };
        match item {
            Ok(datom) => {
                if let Some(observer) = &self.observer
                    && let Err(error) = observer.charge_datom(&datom)
                {
                    let _ = self.record_physical_work();
                    self.failed = true;
                    return Some(Err(error));
                }
                Some(Ok(datom))
            }
            Err(error) => {
                let _ = self.record_physical_work();
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}

impl Iterator for DatabaseValueScanCursor<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        self.next_with_control(&mut |_| Ok(true))
    }
}

impl Drop for DatabaseValueScanCursor<'_> {
    fn drop(&mut self) {
        let _ = self.record_physical_work();
    }
}

struct TransactionOverlayScanCursor<'a> {
    base: TransactionOverlayBaseCursor<'a>,
    delta: OverlayDeltaCursor,
    removals: EavSet,
    schema: Arc<Schema>,
    history: bool,
    order: IndexOrder,
    reverse: bool,
    observer: Option<Arc<LogicalReadObserver>>,
    base_next: Option<OverlayCursorDatom>,
    delta_next: Option<OverlayCursorDatom>,
    failed: bool,
}

enum TransactionOverlayBaseCursor<'a> {
    Scan(DatabaseValueScanCursor<'a>),
    Prefix(DatabaseValuePrefixCursor<'a>),
    Empty,
}

impl Iterator for TransactionOverlayBaseCursor<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        match self {
            Self::Scan(cursor) => cursor.next(),
            Self::Prefix(cursor) => cursor.next(),
            Self::Empty => None,
        }
    }
}

struct OverlayCursorDatom {
    datom: Datom,
    /// An AVET-enablement fallback must read an AEVT range and reorder it
    /// before it can be merged. Those source datoms are charged while that
    /// explicit broad operation is collected, so the merged cursor must not
    /// charge them a second time.
    precharged: bool,
}

/// Merge a lazy resident range with the one exceptional materialized source:
/// an AVET-enablement backfill. Ordinary reads allocate no novelty-sized vector.
struct OverlayDeltaCursor {
    indexed: OverlayIndexCursor,
    indexed_next: Option<Arc<Datom>>,
    backfill: std::iter::Peekable<IntoIter<OverlayCursorDatom>>,
    backfill_eavs: EavSet,
    schema: Arc<Schema>,
    history: bool,
    order: IndexOrder,
    reverse: bool,
}

impl OverlayDeltaCursor {
    fn new(
        indexed: OverlayIndexCursor,
        mut backfill: Vec<OverlayCursorDatom>,
        schema: Arc<Schema>,
        history: bool,
        order: IndexOrder,
        reverse: bool,
        boundary: Option<&crate::index::NormalizedIndexBoundary>,
    ) -> Self {
        let mut backfill_eavs = EavSet::default();
        if !history {
            for item in &backfill {
                backfill_eavs.insert(Arc::new(item.datom.clone()));
            }
        }
        // Duplicate current facts belong to the base even when its original
        // transaction coordinate lies outside this requested boundary.
        if let Some(boundary) = boundary {
            backfill.retain(|item| {
                let compared = boundary.compare_datom(&item.datom);
                if reverse {
                    !compared.is_gt()
                } else {
                    !compared.is_lt()
                }
            });
        }
        backfill.sort_by(|left, right| left.datom.cmp_in(&right.datom, order));
        if reverse {
            backfill.reverse();
        }
        Self {
            indexed,
            indexed_next: None,
            backfill: backfill.into_iter().peekable(),
            backfill_eavs,
            schema,
            history,
            order,
            reverse,
        }
    }
}

impl OverlayDeltaCursor {
    fn next_controlled(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Result<Option<OverlayCursorDatom>, SemanticError> {
        if self.indexed_next.is_none() {
            loop {
                if !control.check(None)? {
                    return Ok(None);
                }
                let Some(datom) = self.indexed.next() else {
                    break;
                };
                if overlay_index_member(&self.schema, &datom, self.order)
                    && (self.history || !self.backfill_eavs.contains(&datom))
                {
                    self.indexed_next = Some(datom);
                    break;
                }
            }
        }
        let take_indexed = match (&self.indexed_next, self.backfill.peek()) {
            (None, _) => false,
            (Some(_), None) => true,
            (Some(indexed), Some(backfill)) => {
                let compared = indexed.cmp_in(&backfill.datom, self.order);
                if self.reverse {
                    compared.is_ge()
                } else {
                    compared.is_le()
                }
            }
        };
        Ok(if take_indexed {
            self.indexed_next.take().map(|datom| OverlayCursorDatom {
                datom: (*datom).clone(),
                precharged: false,
            })
        } else {
            self.backfill.next()
        })
    }
}

/// Lazy current-index cursor over one exact point-in-time database value.
///
/// Eager values borrow their immutable index slice. Native values own a
/// immutable peer cursor which lower-bound seeks both the durable tree and
/// its recent tier. Every yielded item is owned so callers cannot retain a
/// cache or tree-node borrow across cursor advancement.
pub struct DatabaseValuePrefixCursor<'a> {
    inner: DatabaseValuePrefixCursorInner<'a>,
    operation: Option<crate::sql_io::OperationContext>,
    observer: Option<Arc<LogicalReadObserver>>,
    physical_context: Option<Arc<TransactionReadContext>>,
    physical_recorded: bool,
    memo_source: Option<PrefixMemoSource>,
    memo_hit: bool,
    failed: bool,
}

enum DatabaseValuePrefixCursorInner<'a> {
    Eager(Cloned<Iter<'a, Datom>>),
    Block(Box<crate::storage::BlockIndexCursor>),
    Overlay(Box<TransactionOverlayScanCursor<'a>>),
    Window(Box<DatabaseValueWindowCursor<'a>>),
    Memoized { datoms: Arc<[Datom]>, next: usize },
}

impl DatabaseValuePrefixCursor<'_> {
    pub(crate) fn next_with_control(
        &mut self,
        check: &mut dyn FnMut(Option<&Datom>) -> Result<bool, SemanticError>,
    ) -> Option<Result<Datom, SemanticError>> {
        self.next_controlled(&mut ReadCursorControl {
            check,
            stopped: false,
        })
    }

    pub(crate) fn is_memo_hit(&self) -> bool {
        self.memo_hit
    }

    fn record_physical_work(&mut self) -> Result<(), SemanticError> {
        if self.physical_recorded {
            return Ok(());
        }
        self.physical_recorded = true;
        if let (Some(context), DatabaseValuePrefixCursorInner::Block(cursor)) =
            (&self.physical_context, &self.inner)
        {
            context.record_native_cursor(cursor.stats())?;
        }
        Ok(())
    }
}

struct PrefixMemoSource {
    context: Arc<TransactionReadContext>,
    key: Option<PrefixMemoKey>,
    datoms: Vec<Datom>,
    retained_bytes: u64,
    cacheable: bool,
    completed: bool,
}

impl Iterator for DatabaseValuePrefixCursor<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        self.next_with_control(&mut |_| Ok(true))
    }
}

impl DatabaseValuePrefixCursor<'_> {
    fn next_controlled(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Option<Result<Datom, SemanticError>> {
        let operation = self.operation.clone();
        let _scope = operation
            .as_ref()
            .map(crate::sql_io::OperationContext::enter);
        if self.failed {
            return None;
        }
        match control.check(None) {
            Ok(true) => {}
            Ok(false) => {
                self.failed = true;
                let _ = self.record_physical_work();
                return None;
            }
            Err(error) => {
                self.failed = true;
                let _ = self.record_physical_work();
                return Some(Err(error));
            }
        }
        let window = matches!(&self.inner, DatabaseValuePrefixCursorInner::Window(_));
        let item = match &mut self.inner {
            DatabaseValuePrefixCursorInner::Eager(cursor) => cursor.next().map(Ok),
            DatabaseValuePrefixCursorInner::Block(cursor) => {
                cursor.next_with_poll(&mut || control.check(None))
            }
            DatabaseValuePrefixCursorInner::Overlay(cursor) => cursor.next_controlled(control),
            DatabaseValuePrefixCursorInner::Window(cursor) => cursor.next_controlled(control),
            DatabaseValuePrefixCursorInner::Memoized { datoms, next } => {
                let datom = datoms.get(*next).cloned();
                *next = next.saturating_add(1);
                datom.map(Ok)
            }
        };
        let item = if window { item } else { control.accept(item) };
        self.failed |= control.stopped;
        let Some(item) = item else {
            if let Err(error) = self.record_physical_work() {
                self.failed = true;
                return Some(Err(error));
            }
            if let Some(source) = &mut self.memo_source
                && !source.completed
            {
                source.completed = true;
                let result = if source.cacheable && !control.stopped {
                    source.context.admit(
                        source
                            .key
                            .take()
                            .expect("an incomplete memo source retains its key"),
                        std::mem::take(&mut source.datoms),
                        source.retained_bytes,
                    )
                } else {
                    source.key.take();
                    source.context.reject()
                };
                if let Err(error) = result {
                    self.failed = true;
                    return Some(Err(error));
                }
            }
            return None;
        };
        match item {
            Ok(datom) => {
                if let Some(source) = &mut self.memo_source
                    && let Err(error) = source.context.record_source_datom(&datom)
                {
                    source.key.take();
                    source.completed = true;
                    let _ = self.record_physical_work();
                    self.failed = true;
                    return Some(Err(error));
                }
                if let Some(observer) = &self.observer
                    && let Err(error) = observer.charge_datom(&datom)
                {
                    if let Some(source) = &mut self.memo_source {
                        let _ = source.context.reject();
                        source.key.take();
                        source.completed = true;
                    }
                    let _ = self.record_physical_work();
                    self.failed = true;
                    return Some(Err(error));
                }
                if let Some(source) = &mut self.memo_source
                    && source.cacheable
                {
                    let retained_bytes = datom.retained_bytes();
                    let next_bytes = source.retained_bytes.checked_add(retained_bytes);
                    let next_datoms = u64::try_from(source.datoms.len())
                        .unwrap_or(u64::MAX)
                        .saturating_add(1);
                    if next_bytes.is_none_or(|bytes| bytes > source.context.max_retained_bytes)
                        || next_datoms > source.context.max_entry_datoms
                    {
                        source.cacheable = false;
                        source.datoms.clear();
                    } else {
                        source.retained_bytes = next_bytes.expect("checked above");
                        source.datoms.push(datom.clone());
                    }
                }
                Some(Ok(datom))
            }
            Err(error) => {
                if let Some(source) = &mut self.memo_source {
                    let _ = source.context.reject();
                    source.key.take();
                    source.completed = true;
                }
                let _ = self.record_physical_work();
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}

impl Drop for DatabaseValuePrefixCursor<'_> {
    fn drop(&mut self) {
        let _ = self.record_physical_work();
        if let Some(source) = &mut self.memo_source
            && !source.completed
        {
            let _ = source.context.reject();
            source.key.take();
            source.completed = true;
        }
    }
}

/// Recovered `windowed` as an incremental state machine.
///
/// The source is retained behind one box to break the recursive cursor shape:
/// a window wraps a raw-history scan/prefix cursor, while the public cursor in
/// turn owns the window. No source datoms are retained beyond the current
/// logical E/A/V group.
struct DatabaseValueWindowCursor<'a> {
    source: DatabaseValueWindowSource<'a>,
    filter_database: DatabaseValue,
    filters: Arc<[Arc<ReadFilter>]>,
    as_of_t: Option<u64>,
    since_t: Option<u64>,
    history: bool,
    order: IndexOrder,
    reverse: bool,
    forward_boundary: Option<crate::index::NormalizedIndexBoundary>,
    group: Option<WindowGroup>,
    retracted_representations: Vec<Value>,
    reverse_pending: Option<Datom>,
    reverse_output: VecDeque<Datom>,
    failed: bool,
}

enum DatabaseValueWindowSource<'a> {
    Scan(Box<DatabaseValueScanCursor<'a>>),
    Prefix(Box<DatabaseValuePrefixCursor<'a>>),
}

impl Iterator for DatabaseValueWindowSource<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        match self {
            Self::Scan(cursor) => cursor.next(),
            Self::Prefix(cursor) => cursor.next(),
        }
    }
}

impl DatabaseValueWindowSource<'_> {
    fn next_controlled(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Option<Result<Datom, SemanticError>> {
        match self {
            Self::Scan(cursor) => cursor.next_controlled(control),
            Self::Prefix(cursor) => cursor.next_controlled(control),
        }
    }
}

struct WindowGroup {
    entity: u64,
    attribute: u32,
    value: Value,
}

impl WindowGroup {
    fn from_datom(datom: &Datom) -> Self {
        Self {
            entity: datom.entity,
            attribute: datom.attribute,
            value: datom.value.clone(),
        }
    }

    fn matches(&self, datom: &Datom) -> bool {
        self.entity == datom.entity
            && self.attribute == datom.attribute
            && self.value.index_cmp(&datom.value).is_eq()
    }
}

impl<'a> DatabaseValueWindowCursor<'a> {
    fn new(
        database: &DatabaseValue,
        source: DatabaseValueWindowSource<'a>,
        order: IndexOrder,
        reverse: bool,
    ) -> Self {
        Self {
            source,
            filter_database: database.without_filters(),
            filters: Arc::clone(&database.filters),
            as_of_t: database.as_of_t,
            since_t: database.since_t,
            history: database.history,
            order,
            reverse,
            forward_boundary: None,
            group: None,
            retracted_representations: Vec::new(),
            reverse_pending: None,
            reverse_output: VecDeque::new(),
            failed: false,
        }
    }

    fn next_filtered(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Result<Option<Datom>, SemanticError> {
        loop {
            let Some(datom) = self.source.next_controlled(control).transpose()? else {
                return Ok(None);
            };
            let t = tx_to_t(datom.tx)?;
            if self.as_of_t.is_some_and(|as_of| t > as_of)
                || self.since_t.is_some_and(|since| t <= since)
                || !self
                    .filters
                    .iter()
                    .all(|predicate| predicate(&self.filter_database, &datom))
            {
                continue;
            }
            return Ok(Some(datom));
        }
    }

    /// Reverse history visits one logical E/A/V group oldest-first. Match
    /// recovered `rseek-datoms`: temporal/custom predicates run first, then
    /// the last visible event determines current membership. Atomic retains
    /// one winner per strict stored V representation, consistent with the
    /// native kernel's representation-distinct top-level BigDecimals.
    fn next_reverse_current(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Result<Option<Datom>, SemanticError> {
        loop {
            if let Some(datom) = self.reverse_output.pop_front() {
                return Ok(Some(datom));
            }

            let first = match self.reverse_pending.take() {
                Some(datom) => datom,
                None => {
                    let Some(datom) = self.next_filtered(control)? else {
                        return Ok(None);
                    };
                    datom
                }
            };
            let group = WindowGroup::from_datom(&first);
            let mut winners = Vec::<Datom>::new();

            let retain = |candidate: Datom, winners: &mut Vec<Datom>| match winners
                .binary_search_by(|prior| prior.value.stored_cmp(&candidate.value))
            {
                Ok(position) => winners[position] = candidate,
                Err(position) => winners.insert(position, candidate),
            };
            retain(first, &mut winners);

            while let Some(candidate) = self.next_filtered(control)? {
                if group.matches(&candidate) {
                    retain(candidate, &mut winners);
                } else {
                    self.reverse_pending = Some(candidate);
                    break;
                }
            }

            winners.retain(|datom| datom.added);
            winners.sort_by(|left, right| right.cmp_in(left, self.order));
            self.reverse_output.extend(winners);
        }
    }

    fn visible_current(&mut self, datom: &Datom) -> bool {
        if self
            .group
            .as_ref()
            .is_none_or(|group| !group.matches(datom))
        {
            self.group = Some(WindowGroup::from_datom(datom));
            self.retracted_representations.clear();
        }

        let stored = self
            .retracted_representations
            .binary_search_by(|value| value.stored_cmp(&datom.value));
        if datom.added {
            // Recovered `filter-retractions` yields every assertion until a
            // later visible retraction. Atomic deliberately keeps top-level
            // strict-scale BigDecimal facts representation-distinct, so the
            // one-group skip set is keyed by stored equality; tuple members
            // retain their established recursive logical equality.
            stored.is_err()
        } else {
            if let Err(position) = stored {
                self.retracted_representations
                    .insert(position, datom.value.clone());
            }
            false
        }
    }

    fn with_forward_boundary(mut self, boundary: crate::index::NormalizedIndexBoundary) -> Self {
        self.forward_boundary = Some(boundary);
        self
    }
}

impl DatabaseValueWindowCursor<'_> {
    fn next_controlled(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Option<Result<Datom, SemanticError>> {
        if self.failed {
            return None;
        }
        if self.reverse && !self.history {
            return match self.next_reverse_current(control) {
                Ok(Some(datom)) => Some(Ok(datom)),
                Ok(None) => None,
                Err(error) => {
                    self.failed = true;
                    Some(Err(error))
                }
            };
        }
        loop {
            let datom = match self.next_filtered(control) {
                Ok(Some(datom)) => datom,
                Ok(None) => return None,
                Err(error) => {
                    self.failed = true;
                    return Some(Err(error));
                }
            };
            if self.history || self.visible_current(&datom) {
                if self
                    .forward_boundary
                    .as_ref()
                    .is_some_and(|boundary| boundary.compare_datom(&datom).is_lt())
                {
                    continue;
                }
                return Some(Ok(datom));
            }
        }
    }
}

impl fmt::Debug for DatabaseValue {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_struct("DatabaseValue")
            .field(
                "basis",
                &match &self.basis {
                    ReadBasis::Eager(_) => "eager",
                    ReadBasis::Block(snapshot) => {
                        if snapshot.is_repository() {
                            "backup"
                        } else {
                            "block"
                        }
                    }
                    ReadBasis::TransactionOverlay(_) => "transaction-overlay",
                },
            )
            .field("basis_t", &self.basis_t())
            .field("as_of_t", &self.as_of_t)
            .field("since_t", &self.since_t)
            .field("history", &self.history)
            .field("filter_count", &self.filters.len())
            .finish()
    }
}

impl DatabaseValue {
    /// Database-filter docs: `with` computes over the full basis, then applies
    /// the supplied view. An as-of view is not a branch of the past.
    pub(crate) fn speculation_base(&self) -> Result<Self, SemanticError> {
        if self.history {
            return Err(SemanticError::incorrect(
                "transaction/history-with",
                "history values cannot be used for speculative transactions",
            ));
        }
        let mut base = self.clone();
        base.read_identity = Arc::new(ReadValueIdentity);
        base.as_of_t = None;
        base.since_t = None;
        base.filters = Arc::default();
        Ok(base)
    }

    pub(crate) fn with_speculation_view(mut self, original: &Self) -> Self {
        self.read_identity = Arc::new(ReadValueIdentity);
        self.as_of_t = original.as_of_t;
        self.since_t = original.since_t;
        self.filters = Arc::clone(&original.filters);
        self
    }

    pub(crate) fn resolve_program(
        &self,
        hash: crate::ProgramHash,
    ) -> Result<Arc<crate::program::ValidatedProgram>, SemanticError> {
        if let Some(program) = self.programs.get(&hash) {
            return Ok(Arc::clone(program));
        }
        match &self.basis {
            ReadBasis::Block(snapshot) => snapshot.resolve_program(hash),
            ReadBasis::TransactionOverlay(overlay) => overlay.base.resolve_program(hash),
            ReadBasis::Eager(_) => Err(SemanticError::new(
                ErrorCategory::NotFound,
                "postgres/program-not-found",
                "program content is not available in this database value",
            )),
        }
    }

    pub(crate) fn committed_block_parts(
        &self,
    ) -> Result<
        (
            crate::storage::BlockSnapshot,
            Option<u64>,
            Option<u64>,
            bool,
        ),
        SemanticError,
    > {
        if !self.filters.is_empty() {
            return Err(SemanticError::new(
                ErrorCategory::Unsupported,
                "database/opaque-filter-snapshot",
                "Opaque filtered values have no portable snapshot reference",
            ));
        }
        match &self.basis {
            ReadBasis::Block(snapshot)
                if snapshot.route_id().is_some() && !snapshot.is_repository() =>
            {
                Ok((
                    snapshot.as_ref().clone(),
                    self.as_of_t,
                    self.since_t,
                    self.history,
                ))
            }
            _ => Err(SemanticError::new(
                ErrorCategory::Unsupported,
                "database/uncommitted-snapshot",
                "Only committed block values have portable references",
            )),
        }
    }

    pub(crate) fn retain_programs(
        mut self,
        programs: crate::program_bindings::ResolvedPrograms,
    ) -> Self {
        for (hash, program) in programs {
            self.programs.insert(hash, program);
        }
        self
    }

    pub fn eager(database: Arc<Database>) -> Self {
        let last_tx_instant_memo = Arc::new(LastTxInstantMemo::seeded(database.last_tx_instant()));
        Self {
            entity_origin: database.entity_origin.clone(),
            basis: ReadBasis::Eager(database),
            read_identity: Arc::new(ReadValueIdentity),
            last_tx_instant_memo,
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
            read_observer: None,
            read_context: None,
            programs: SharedMap::default(),
        }
    }

    pub fn native(snapshot: PeerSnapshot) -> Self {
        snapshot.database_value()
    }

    /// An immutable value over the first-release opaque block store. This uses
    /// the same query/window/speculation algorithms as other database values.
    pub fn block(snapshot: crate::storage::BlockSnapshot) -> Self {
        Self {
            entity_origin: crate::entity_identity::DatabaseOrigin::durable(snapshot.lineage_id()),
            basis: ReadBasis::Block(Arc::new(snapshot)),
            read_identity: Arc::new(ReadValueIdentity),
            last_tx_instant_memo: Arc::new(LastTxInstantMemo::empty()),
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
            read_observer: None,
            read_context: None,
            programs: SharedMap::default(),
        }
    }

    pub(crate) fn block_snapshot(&self) -> Option<crate::storage::BlockSnapshot> {
        match &self.basis {
            ReadBasis::Block(snapshot) => Some(snapshot.as_ref().clone()),
            ReadBasis::TransactionOverlay(overlay) => overlay.base.block_snapshot(),
            _ => None,
        }
    }

    pub(crate) fn block_log_snapshot(&self) -> Option<crate::storage::BlockSnapshot> {
        match &self.basis {
            ReadBasis::Block(snapshot) if self.filters.is_empty() => {
                Some(snapshot.as_ref().clone())
            }
            _ => None,
        }
    }

    pub(crate) fn fulltext_history_cursor(
        &self,
        attribute: u32,
    ) -> Result<DatabaseValuePrefixCursor<'_>, SemanticError> {
        self.basis_prefix_cursor(
            true,
            &IndexPrefix::Aevt {
                attribute,
                entity: None,
                value: None,
            },
            None,
            None,
        )
    }

    /// Assertion candidates not covered by the captured search attachment.
    /// The authenticated recent tier starts after the exact canonical index
    /// basis to which that attachment is bound; speculative indexes are the
    /// disjoint continuation after the committed value. Attribute-prefix seeks
    /// avoid scanning unrelated facts or materializing the durable database.
    pub(crate) fn fulltext_unindexed_cursor(
        &self,
        attribute: u32,
    ) -> Result<impl Iterator<Item = Datom>, SemanticError> {
        let prefix = IndexPrefix::Aevt {
            attribute,
            entity: None,
            value: None,
        };
        let recent = self
            .block_snapshot()
            .map(|snapshot| snapshot.recent_tier().prefix_cursor(true, &prefix))
            .transpose()?;
        let local = match &self.basis {
            ReadBasis::TransactionOverlay(overlay) => Some(overlay.indexes.cursor(
                true,
                IndexOrder::Aevt,
                |datom| compare_prefix(datom, &prefix),
                false,
                Some(prefix.clone()),
            )),
            _ => None,
        };
        Ok(recent
            .into_iter()
            .flatten()
            .chain(local.into_iter().flatten().map(|datom| (*datom).clone())))
    }

    /// Build an exact db-after for assessment or pure speculation. Chained
    /// values share one committed base and retain only speculative information.
    /// A committed successor still must install an authenticated tiered value.
    #[cfg(test)]
    pub(crate) fn transaction_overlay(
        base: DatabaseValue,
        tx_data: Arc<[Datom]>,
        schema: Arc<Schema>,
        basis_t: u64,
        eidx_frontier: u64,
        last_tx_instant: i64,
    ) -> Result<Self, SemanticError> {
        let mut allocation = base.reserved_allocation()?;
        if let Some(allocation) = &mut allocation {
            allocation.observe_datoms(tx_data.iter())?;
        }
        Self::transaction_overlay_with_allocation(
            base,
            tx_data,
            schema,
            basis_t,
            eidx_frontier,
            last_tx_instant,
            allocation,
        )
    }

    /// Assessment supplies its final retained cursor, including allocated
    /// tempids which left no datoms. It cannot be derived from schema alone.
    pub(crate) fn transaction_overlay_with_allocation(
        mut base: DatabaseValue,
        tx_data: Arc<[Datom]>,
        schema: Arc<Schema>,
        basis_t: u64,
        eidx_frontier: u64,
        last_tx_instant: i64,
        reserved_allocation: Option<crate::reserved_allocation::ReservedAllocation>,
    ) -> Result<Self, SemanticError> {
        if !base.direct_current() {
            return Err(SemanticError::incorrect(
                "database/overlay-requires-current",
                "a transaction overlay requires an unfiltered point-current db-before",
            ));
        }
        let programs = base.programs.clone();
        let expected_basis = base.basis_t().checked_add(1).ok_or_else(|| {
            SemanticError::incorrect(
                "database/overlay-basis-overflow",
                "a transaction overlay cannot advance the maximum database basis",
            )
        })?;
        if basis_t != expected_basis {
            return Err(SemanticError::incorrect(
                "database/overlay-noncontiguous-basis",
                format!("expected overlay basis {expected_basis}, got {basis_t}"),
            ));
        }
        validate_frontier(eidx_frontier)?;
        if let Some(allocation) = reserved_allocation {
            crate::reserved_allocation::ReservedAllocation::from_frontier(
                allocation.frontier(),
                eidx_frontier,
            )?;
        }
        if eidx_frontier < base.eidx_frontier() {
            return Err(SemanticError::incorrect(
                "database/overlay-frontier-regression",
                "a transaction overlay cannot move the entity issuance frontier backward",
            ));
        }
        let expected_tx = crate::t_to_tx(basis_t)?;
        if tx_data.iter().any(|datom| datom.tx != expected_tx) {
            return Err(SemanticError::incorrect(
                "database/overlay-transaction-mismatch",
                "every overlay datom must name the successor transaction",
            ));
        }
        if tx_data
            .windows(2)
            .any(|pair| !pair[0].cmp_in(&pair[1], IndexOrder::Eavt).is_lt())
        {
            return Err(SemanticError::incorrect(
                "database/overlay-noncanonical-datoms",
                "overlay datoms must be strictly ordered in canonical EAVT order",
            ));
        }
        if base
            .last_tx_instant()?
            .is_some_and(|previous| last_tx_instant < previous)
        {
            return Err(SemanticError::incorrect(
                "database/overlay-non-monotonic-instant",
                "overlay transaction instant precedes its db-before",
            ));
        }

        // Recovered ident reconstruction folds assertions only: retractions
        // do not erase aliases, and a later assertion may repurpose one. Keep
        // just this transaction's delta and resolve it before the base cache.
        let mut ident_assertions = Vec::new();
        for datom in tx_data
            .iter()
            .filter(|datom| datom.added && u64::from(datom.attribute) == DB_IDENT)
        {
            let Value::Keyword(ident) = &datom.value else {
                return Err(SemanticError::incorrect(
                    "database/overlay-invalid-ident",
                    ":db/ident overlay assertions must contain keywords",
                ));
            };
            if ident_assertions.iter().any(|(prior_ident, prior_entity)| {
                prior_ident == ident || *prior_entity == datom.entity
            }) {
                return Err(SemanticError::incorrect(
                    "database/overlay-conflicting-ident",
                    "overlay ident assertions must be unique by ident and entity",
                ));
            }
            ident_assertions.push((ident.clone(), datom.entity));
        }

        let read_context = base.read_context.clone();
        let mut indexes = OverlayIndexes::default();
        let mut unchanged_avet = None;
        if let ReadBasis::TransactionOverlay(prior) = &base.basis {
            indexes = prior.indexes.clone();
            if Arc::ptr_eq(&schema, &prior.schema) {
                unchanged_avet = Some(prior.newly_enabled_avet.clone());
            }
            base = prior.base.clone();
        }
        indexes.extend(&tx_data, ident_assertions);
        let base_schema = base.schema_arc();
        let newly_enabled_avet: Arc<[u32]> = if let Some(unchanged) = unchanged_avet {
            unchanged
        } else if Arc::ptr_eq(&schema, &base_schema) {
            Arc::from([])
        } else {
            schema
                .attributes()
                .filter(|attribute| {
                    schema_has_avet(&schema, attribute.id)
                        && !schema_has_avet(&base_schema, attribute.id)
                })
                .map(|attribute| attribute.id)
                .collect::<Vec<_>>()
                .into()
        };

        // The overlay is the logical read boundary. Keeping an observer or
        // memo context on both it and its wrapped base would charge/cache
        // every base datom twice. The outer value inherits the same attempt
        // context under its own immutable-value identity.
        base.read_observer = None;
        base.read_context = None;
        Ok(Self {
            entity_origin: base.entity_origin.clone(),
            basis: ReadBasis::TransactionOverlay(Arc::new(TransactionOverlay {
                base,
                indexes,
                schema,
                newly_enabled_avet,
                basis_t,
                eidx_frontier,
                last_tx_instant,
                reserved_allocation,
            })),
            read_identity: Arc::new(ReadValueIdentity),
            last_tx_instant_memo: Arc::new(LastTxInstantMemo::seeded(Some(last_tx_instant))),
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
            read_observer: read_context.as_ref().map(|context| context.observer()),
            read_context,
            programs,
        })
    }

    /// Attach one transaction-scoped logical read observer to this value.
    ///
    /// This is intentionally crate-private: ordinary immutable database
    /// values carry no mutable diagnostics. For an overlay, observation is
    /// installed only at the outer merged-value boundary.
    #[cfg(test)]
    pub(crate) fn with_read_observer(mut self, observer: Arc<LogicalReadObserver>) -> Self {
        if let ReadBasis::TransactionOverlay(overlay) = &self.basis {
            let mut overlay = (**overlay).clone();
            overlay.base.read_observer = None;
            overlay.base.read_context = None;
            self.basis = ReadBasis::TransactionOverlay(Arc::new(overlay));
        }
        self.read_context = None;
        self.read_observer = Some(observer);
        self
    }

    /// Attach the shared, bounded prefix memo and logical observer for one
    /// transaction attempt. This context must never escape into a committed
    /// writer snapshot, receipt, or report queue; the publication handoff
    /// strips it from both db-before and db-after values.
    pub(crate) fn with_transaction_read_context(
        mut self,
        context: Arc<TransactionReadContext>,
    ) -> Self {
        if let ReadBasis::TransactionOverlay(overlay) = &self.basis {
            let mut overlay = (**overlay).clone();
            overlay.base.read_observer = None;
            overlay.base.read_context = None;
            self.basis = ReadBasis::TransactionOverlay(Arc::new(overlay));
        }
        self.read_observer = Some(context.observer());
        self.read_context = Some(context);
        self
    }

    pub(crate) fn transaction_read_context(&self) -> Option<Arc<TransactionReadContext>> {
        self.read_context.clone()
    }

    pub(crate) fn without_transaction_read_context(mut self) -> Self {
        self.read_context = None;
        self.read_observer = None;
        if let ReadBasis::TransactionOverlay(overlay) = &self.basis {
            let mut overlay = (**overlay).clone();
            overlay.base.read_context = None;
            overlay.base.read_observer = None;
            self.basis = ReadBasis::TransactionOverlay(Arc::new(overlay));
        }
        self
    }

    /// The basis remains the basis of the underlying immutable value even
    /// when an as-of or since window exposes less information.
    pub fn basis_t(&self) -> u64 {
        match &self.basis {
            ReadBasis::Eager(database) => database.basis_t(),
            ReadBasis::Block(snapshot) => snapshot.basis_t(),
            ReadBasis::TransactionOverlay(overlay) => overlay.basis_t,
        }
    }

    /// Exclusive entity-index issuance frontier at this immutable basis.
    pub fn eidx_frontier(&self) -> u64 {
        match &self.basis {
            ReadBasis::Eager(database) => database.eidx_frontier(),
            ReadBasis::Block(snapshot) => snapshot.eidx_frontier(),
            ReadBasis::TransactionOverlay(overlay) => overlay.eidx_frontier,
        }
    }

    pub(crate) fn reserved_allocation(
        &self,
    ) -> Result<Option<crate::reserved_allocation::ReservedAllocation>, SemanticError> {
        match &self.basis {
            ReadBasis::Eager(database) => Ok(database.reserved_allocation()),
            ReadBasis::Block(snapshot) => Ok(snapshot.reserved_allocation()),
            ReadBasis::TransactionOverlay(overlay) => Ok(overlay.reserved_allocation),
        }
    }

    /// The most recent transaction instant at this immutable basis.
    ///
    /// A native value resolves the single transaction entity through its lazy
    /// index at most once across immutable clones, without materializing the
    /// full database.
    pub fn last_tx_instant(&self) -> Result<Option<i64>, SemanticError> {
        self.last_tx_instant_memo
            .get_or_try_init(|| match &self.basis {
                ReadBasis::Eager(database) => Ok(database.last_tx_instant()),
                ReadBasis::Block(snapshot) => Ok(snapshot.last_tx_instant()),
                ReadBasis::TransactionOverlay(overlay) => Ok(Some(overlay.last_tx_instant)),
            })
    }

    pub fn schema(&self) -> &Schema {
        match &self.basis {
            ReadBasis::Eager(database) => database.schema(),
            ReadBasis::Block(snapshot) => snapshot.schema(),
            ReadBasis::TransactionOverlay(overlay) => &overlay.schema,
        }
    }

    /// Share the immutable schema projection carried by this database value.
    /// This is the ownership counterpart of [`Self::schema`]: assessment can
    /// retain an unchanged projection without a whole-schema clone.
    pub(crate) fn schema_arc(&self) -> Arc<Schema> {
        match &self.basis {
            ReadBasis::Eager(database) => database.schema_arc(),
            ReadBasis::Block(snapshot) => snapshot.schema_arc(),
            ReadBasis::TransactionOverlay(overlay) => Arc::clone(&overlay.schema),
        }
    }

    /// Resolve an ident from the immutable basis' derived ident cache.
    /// Datomic's temporal predicates window index data, not this dictionary.
    pub fn entid(&self, ident: &Keyword) -> Option<u64> {
        match &self.basis {
            ReadBasis::Eager(database) => database.entid(ident),
            ReadBasis::Block(snapshot) => snapshot.entid(ident),
            ReadBasis::TransactionOverlay(overlay) => overlay
                .indexes
                .entids
                .get(ident)
                .copied()
                .or_else(|| overlay.base.entid(ident)),
        }
    }

    pub fn ident(&self, entity: u64) -> Option<&Keyword> {
        match &self.basis {
            ReadBasis::Eager(database) => database.ident(entity),
            ReadBasis::Block(snapshot) => snapshot.ident(entity),
            ReadBasis::TransactionOverlay(overlay) => overlay
                .indexes
                .idents
                .get(&entity)
                .or_else(|| overlay.base.ident(entity)),
        }
    }

    pub fn as_of_t(&self) -> Option<u64> {
        self.as_of_t
    }

    pub fn since_t(&self) -> Option<u64> {
        self.since_t
    }

    /// Resolve a documented time point to the boundary used by `as-of` and
    /// `since`.
    ///
    /// T and Tx are exact.  Instant resolution mirrors recovered 1.0.7705
    /// `as-of-t`: one AVET lower-bound seek finds the first transaction at or
    /// after the millisecond.  An exact duplicate-millisecond match therefore
    /// selects the earliest matching transaction; `since` remains exclusive
    /// of that resolved T and can expose later transactions from the same
    /// millisecond.  This is the documented imprecision of instant points.
    pub fn resolve_time_point(&self, time_point: TimePoint) -> Result<u64, SemanticError> {
        match time_point {
            TimePoint::T(t) => {
                crate::t_to_tx(t)?;
                Ok(t)
            }
            TimePoint::Tx(tx) => crate::tx_to_t(tx),
            TimePoint::Instant(instant) => self.t_at_or_before_instant(instant),
        }
    }

    /// Resolve and install an inclusive as-of boundary without changing the
    /// immutable basis, schema, or ident dictionary.
    pub fn as_of_time_point(mut self, time_point: TimePoint) -> Result<Self, SemanticError> {
        let t = self.resolve_time_point(time_point)?;
        self.read_identity = Arc::new(ReadValueIdentity);
        self.as_of_t = Some(t);
        Ok(self)
    }

    /// Resolve and install an exclusive since boundary without changing the
    /// immutable basis, schema, or ident dictionary.
    pub fn since_time_point(mut self, time_point: TimePoint) -> Result<Self, SemanticError> {
        let t = self.resolve_time_point(time_point)?;
        self.read_identity = Arc::new(ReadValueIdentity);
        self.since_t = Some(t);
        Ok(self)
    }

    /// Fabricate the EAVT entity boundary for a named or implicit partition.
    ///
    /// Recovered `entid-at` uses a different instant rule from `as-of`: it
    /// selects the first transaction at or after the instant. Recovered
    /// `partbits` accepts either a partition-zero entity (whose entity-index
    /// names the partition) or the base eid of any nonzero partition.
    pub fn entid_at(
        &self,
        partition: &EntityIdentifier,
        time_point: TimePoint,
    ) -> Result<u64, SemanticError> {
        let partition_entity = self.resolve_entity_identifier(partition)?.ok_or_else(|| {
            SemanticError::incorrect(
                "database/unknown-partition",
                "partition entity does not resolve in this database value",
            )
        })?;
        let entity_partition = crate::eid_to_part(partition_entity)?;
        let entity_index = crate::eid_to_eidx(partition_entity)?;
        let partition_bits = if entity_partition == crate::DB_PARTITION {
            u32::try_from(entity_index)
                .ok()
                .filter(|partition| *partition <= crate::MAX_PARTITION)
                .ok_or_else(|| {
                    SemanticError::incorrect(
                        "database/not-a-partition",
                        format!(
                            "partition-zero entity {partition_entity} has out-of-range partition bits {entity_index}"
                        ),
                    )
                })?
        } else if entity_index == 0 {
            entity_partition
        } else {
            return Err(SemanticError::incorrect(
                "database/not-a-partition",
                format!(
                    "entity {partition_entity} is not partition-zero or a nonzero partition base"
                ),
            ));
        };
        let t = match time_point {
            TimePoint::T(t) => {
                crate::t_to_tx(t)?;
                t
            }
            TimePoint::Tx(tx) => crate::tx_to_t(tx)?,
            TimePoint::Instant(instant) => self.t_at_or_after_instant(instant)?,
        };
        crate::make_eid(partition_bits, t)
    }

    pub fn is_history(&self) -> bool {
        self.history
    }

    pub fn is_filtered(&self) -> bool {
        !self.filters.is_empty()
    }

    /// Derive a value with an inclusive upper transaction boundary.
    /// Repeating `as_of` replaces the prior upper boundary, matching the
    /// recovered immutable `Db` record update.
    pub fn as_of(mut self, t: u64) -> Self {
        self.read_identity = Arc::new(ReadValueIdentity);
        self.as_of_t = Some(t);
        self
    }

    /// Derive a value with an exclusive lower transaction boundary.
    /// Repeating `since` replaces the prior lower boundary.
    pub fn since(mut self, t: u64) -> Self {
        self.read_identity = Arc::new(ReadValueIdentity);
        self.since_t = Some(t);
        self
    }

    fn t_at_or_before_instant(&self, instant: i64) -> Result<u64, SemanticError> {
        let Some(datom) = self.first_tx_instant_at_or_after(instant)? else {
            return self.next_t();
        };
        let (candidate_instant, candidate_t) = self.decode_tx_instant_candidate(&datom, instant)?;
        if candidate_instant == instant {
            Ok(candidate_t)
        } else {
            candidate_t.checked_sub(1).ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "database/invalid-tx-instant-boundary",
                    "positive transaction instant has no predecessor T boundary",
                )
            })
        }
    }

    fn t_at_or_after_instant(&self, instant: i64) -> Result<u64, SemanticError> {
        let Some(datom) = self.first_tx_instant_at_or_after(instant)? else {
            return self.next_t();
        };
        self.decode_tx_instant_candidate(&datom, instant)
            .map(|(_, t)| t)
    }

    fn next_t(&self) -> Result<u64, SemanticError> {
        self.basis_t().checked_add(1).ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Fault,
                "database/time-boundary-overflow",
                "database basis has no representable successor time boundary",
            )
        })
    }

    fn first_tx_instant_at_or_after(&self, instant: i64) -> Result<Option<Datom>, SemanticError> {
        let candidate =
            self.first_tx_instant_at_or_after_unobserved(instant, self.read_context.as_deref())?;
        if let (Some(observer), Some(datom)) = (&self.read_observer, &candidate) {
            observer.charge_datom(datom)?;
        }
        Ok(candidate.filter(|datom| datom.attribute == crate::DB_TX_INSTANT as u32))
    }

    fn first_tx_instant_at_or_after_unobserved(
        &self,
        instant: i64,
        physical_context: Option<&TransactionReadContext>,
    ) -> Result<Option<Datom>, SemanticError> {
        let prefix = IndexPrefix::Avet {
            attribute: crate::DB_TX_INSTANT as u32,
            value: Some(Value::Instant(instant)),
            entity: None,
        };
        match &self.basis {
            ReadBasis::Eager(database) => Ok(database.seek_datoms(&prefix)?.first().cloned()),
            ReadBasis::Block(snapshot) => {
                let mut cursor = snapshot.boundary_cursor(
                    false,
                    &IndexBoundary::Avet(IndexComponents::Two(
                        crate::DB_TX_INSTANT as u32,
                        Value::Instant(instant),
                    )),
                    false,
                )?;
                let candidate = cursor.next().transpose()?;
                if let Some(context) = physical_context {
                    context.record_native_cursor(cursor.stats())?;
                }
                Ok(candidate)
            }
            ReadBasis::TransactionOverlay(overlay) => {
                let candidate = overlay
                    .base
                    .first_tx_instant_at_or_after_unobserved(instant, physical_context)?;
                if candidate
                    .as_ref()
                    .is_some_and(|datom| datom.attribute == crate::DB_TX_INSTANT as u32)
                {
                    return Ok(candidate);
                }
                if overlay.last_tx_instant < instant {
                    return Ok(candidate);
                }
                overlay
                    .indexes
                    .cursor(
                        false,
                        IndexOrder::Avet,
                        |datom| {
                            datom
                                .attribute
                                .cmp(&(crate::DB_TX_INSTANT as u32))
                                .then_with(|| datom.value.index_cmp(&Value::Instant(instant)))
                        },
                        false,
                        Some(IndexPrefix::Avet {
                            attribute: crate::DB_TX_INSTANT as u32,
                            value: None,
                            entity: None,
                        }),
                    )
                    .find(|datom| {
                        datom.entity == datom.tx
                            && datom.attribute == crate::DB_TX_INSTANT as u32
                            && datom.added
                            && matches!(datom.value, Value::Instant(value) if value >= instant)
                    })
                    .map(|datom| Some((*datom).clone()))
                    .ok_or_else(|| {
                        SemanticError::new(
                            ErrorCategory::Fault,
                            "database/invalid-overlay-tx-instant",
                            "transaction overlay has no own transaction instant",
                        )
                    })
            }
        }
    }

    fn decode_tx_instant_candidate(
        &self,
        datom: &Datom,
        requested: i64,
    ) -> Result<(i64, u64), SemanticError> {
        let Value::Instant(candidate) = &datom.value else {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "database/invalid-tx-instant-index",
                ":db/txInstant AVET contains a non-instant value",
            ));
        };
        let t = tx_to_t(datom.tx).map_err(|error| {
            SemanticError::new(
                ErrorCategory::Fault,
                "database/invalid-tx-instant-index",
                format!(":db/txInstant AVET has an invalid transaction id: {error}"),
            )
        })?;
        if datom.entity != datom.tx
            || !datom.added
            || *candidate < requested
            || t == 0
            || t > self.basis_t()
        {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "database/invalid-tx-instant-index",
                ":db/txInstant AVET candidate is not a valid current transaction instant",
            ));
        }
        Ok((*candidate, t))
    }

    /// Derive a raw history value. Temporal and custom predicates remain in
    /// force; only assertion/retraction collapse is disabled.
    pub fn history(mut self) -> Self {
        self.history = true;
        self
    }

    /// Compose an additional custom datom predicate by logical conjunction.
    /// During evaluation the predicate receives the same temporal/history
    /// value with custom predicates removed, preventing recursive filtering.
    pub fn filter<F>(mut self, predicate: F) -> Self
    where
        F: Fn(&DatabaseValue, &Datom) -> bool + Send + Sync + 'static,
    {
        self.read_identity = Arc::new(ReadValueIdentity);
        let mut filters = self.filters.iter().cloned().collect::<Vec<_>>();
        filters.push(Arc::new(predicate));
        self.filters = filters.into();
        self
    }

    /// Reject operations such as entity navigation and pull that require one
    /// point in time. History remains valid for index access and queries.
    pub fn require_point_in_time(&self, operation: &'static str) -> Result<(), SemanticError> {
        if self.history {
            return Err(SemanticError::new(
                ErrorCategory::Incorrect,
                "database/history-not-point-in-time",
                format!("{operation} requires a point-in-time database value"),
            ));
        }
        Ok(())
    }

    /// Open a lazy forward cursor over one complete logical index.
    ///
    /// Construction does not visit a tree child or scan eager history.
    /// Temporal predicates and custom filters are applied before incremental
    /// current retraction collapse, matching recovered `windowed`.
    pub fn scan_cursor(
        &self,
        order: IndexOrder,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        crate::transaction_hints::record_broad_read();
        if self.direct_current() {
            return self.basis_scan_cursor(
                false,
                order,
                self.read_observer.clone(),
                self.read_context.clone(),
            );
        }
        if self.direct_history() {
            return self.basis_scan_cursor(
                true,
                order,
                self.read_observer.clone(),
                self.read_context.clone(),
            );
        }
        let source = self.basis_scan_cursor(true, order, None, self.read_context.clone())?;
        Ok(DatabaseValueScanCursor {
            inner: DatabaseValueScanCursorInner::Window(Box::new(DatabaseValueWindowCursor::new(
                self,
                DatabaseValueWindowSource::Scan(Box::new(source)),
                order,
                false,
            ))),
            observer: self.read_observer.clone(),
            physical_context: None,
            physical_recorded: false,
            operation: crate::sql_io::OperationContext::current(),
            failed: false,
        })
    }

    /// Open a lazy forward raw-index cursor at a typed virtual boundary.
    ///
    /// The supplied components choose only the starting position; iteration
    /// continues through the rest of the index. Missing suffix components
    /// position before the lowest match, including every operation/stored
    /// representation tied at a full E/A/V/T boundary.
    pub fn seek_cursor(
        &self,
        boundary: &IndexBoundary,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        crate::transaction_hints::record_broad_read();
        self.validate_raw_boundary_access(boundary)?;
        if self.direct_current() {
            return self.basis_seek_boundary_cursor(
                false,
                boundary,
                self.read_observer.clone(),
                self.read_context.clone(),
            );
        }
        if self.direct_history() {
            return self.basis_seek_boundary_cursor(
                true,
                boundary,
                self.read_observer.clone(),
                self.read_context.clone(),
            );
        }
        let order = boundary.order();
        let source_boundary = if self.history {
            boundary.clone()
        } else {
            boundary.current_group_start()
        };
        let source = self.basis_seek_boundary_cursor(
            true,
            &source_boundary,
            None,
            self.read_context.clone(),
        )?;
        let window = DatabaseValueWindowCursor::new(
            self,
            DatabaseValueWindowSource::Scan(Box::new(source)),
            order,
            false,
        );
        let window = if self.history {
            window
        } else {
            window.with_forward_boundary(boundary.normalized()?)
        };
        Ok(DatabaseValueScanCursor {
            inner: DatabaseValueScanCursorInner::Window(Box::new(window)),
            observer: self.read_observer.clone(),
            physical_context: None,
            physical_recorded: false,
            operation: crate::sql_io::OperationContext::current(),
            failed: false,
        })
    }

    /// Explicit collecting convenience over [`Self::seek_cursor`].
    pub fn collect_seek_datoms(
        &self,
        boundary: &IndexBoundary,
    ) -> Result<Vec<Datom>, SemanticError> {
        self.seek_cursor(boundary)?.collect()
    }

    /// Open the lazy reverse complement of [`Self::seek_cursor`].
    ///
    /// Traversal starts after the highest match of the virtual components and
    /// proceeds toward the beginning of the index. Temporal/custom predicates
    /// filter raw history before current retraction collapse, matching
    /// recovered `rseek-datoms` rather than reversing a collected forward
    /// result.
    pub fn reverse_seek_cursor(
        &self,
        boundary: &IndexBoundary,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        crate::transaction_hints::record_broad_read();
        self.validate_raw_boundary_access(boundary)?;
        if self.direct_current() {
            return self.basis_reverse_boundary_cursor(
                false,
                boundary,
                self.read_observer.clone(),
                self.read_context.clone(),
            );
        }
        if self.direct_history() {
            return self.basis_reverse_boundary_cursor(
                true,
                boundary,
                self.read_observer.clone(),
                self.read_context.clone(),
            );
        }
        let order = boundary.order();
        let source =
            self.basis_reverse_boundary_cursor(true, boundary, None, self.read_context.clone())?;
        Ok(DatabaseValueScanCursor {
            inner: DatabaseValueScanCursorInner::Window(Box::new(DatabaseValueWindowCursor::new(
                self,
                DatabaseValueWindowSource::Scan(Box::new(source)),
                order,
                true,
            ))),
            observer: self.read_observer.clone(),
            physical_context: None,
            physical_recorded: false,
            operation: crate::sql_io::OperationContext::current(),
            failed: false,
        })
    }

    /// Explicit collecting convenience over [`Self::reverse_seek_cursor`].
    pub fn collect_reverse_seek_datoms(
        &self,
        boundary: &IndexBoundary,
    ) -> Result<Vec<Datom>, SemanticError> {
        self.reverse_seek_cursor(boundary)?.collect()
    }

    /// Explicit collecting convenience over [`Self::scan_cursor`].
    pub fn collect_datoms(&self, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
        self.scan_cursor(order)?.collect()
    }

    /// Compatibility collecting convenience. New streaming callers should
    /// prefer [`Self::scan_cursor`] or name collection explicitly with
    /// [`Self::collect_datoms`].
    pub fn datoms(&self, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
        self.collect_datoms(order)
    }

    /// Query evaluation uses the same public lazy scan semantics.
    pub(crate) fn query_scan_cursor(
        &self,
        order: IndexOrder,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        self.scan_cursor(order)
    }

    /// Query-only AVET lower bound. A strict bound advances past every logical
    /// A/V tie, without inventing a greatest entity/transaction sentinel.
    pub(crate) fn query_avet_start_cursor(
        &self,
        attribute: u32,
        lower: Option<(&Value, bool)>,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        let boundary = IndexBoundary::Avet(match lower {
            Some((value, _)) => IndexComponents::Two(attribute, value.clone()),
            None => IndexComponents::One(attribute),
        });
        self.validate_raw_boundary_access(&boundary)?;
        let after = lower.is_some_and(|(_, inclusive)| !inclusive);
        let source = self.basis_seek_boundary_cursor_biased(
            !self.direct_current(),
            &boundary,
            after,
            if self.direct_current() || self.direct_history() {
                self.read_observer.clone()
            } else {
                None
            },
            self.read_context.clone(),
        )?;
        if self.direct_current() || self.direct_history() {
            return Ok(source);
        }
        Ok(DatabaseValueScanCursor {
            inner: DatabaseValueScanCursorInner::Window(Box::new(DatabaseValueWindowCursor::new(
                self,
                DatabaseValueWindowSource::Scan(Box::new(source)),
                IndexOrder::Avet,
                false,
            ))),
            observer: self.read_observer.clone(),
            physical_context: None,
            physical_recorded: false,
            operation: crate::sql_io::OperationContext::current(),
            failed: false,
        })
    }

    /// Open a lazy cursor over one left-contiguous logical index prefix.
    pub fn prefix_cursor(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<DatabaseValuePrefixCursor<'_>, SemanticError> {
        prefix.validate()?;
        if self.direct_current() {
            return self.memoized_prefix_cursor(false, prefix);
        }
        if self.direct_history() {
            return self.memoized_prefix_cursor(true, prefix);
        }
        let source = self.basis_prefix_cursor(true, prefix, None, self.read_context.clone())?;
        Ok(DatabaseValuePrefixCursor {
            inner: DatabaseValuePrefixCursorInner::Window(Box::new(
                DatabaseValueWindowCursor::new(
                    self,
                    DatabaseValueWindowSource::Prefix(Box::new(source)),
                    prefix.order(),
                    false,
                ),
            )),
            observer: self.read_observer.clone(),
            physical_context: None,
            physical_recorded: false,
            operation: crate::sql_io::OperationContext::current(),
            memo_source: None,
            memo_hit: false,
            failed: false,
        })
    }

    /// Explicit collecting convenience over [`Self::prefix_cursor`].
    pub fn collect_datoms_with_prefix(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<Vec<Datom>, SemanticError> {
        self.prefix_cursor(prefix)?.collect()
    }

    /// Compatibility collecting convenience. New streaming callers should
    /// prefer [`Self::prefix_cursor`] or [`Self::collect_datoms_with_prefix`].
    pub fn datoms_with_prefix(&self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        self.collect_datoms_with_prefix(prefix)
    }

    /// Query and persisted-program execution use the same public lazy prefix
    /// semantics.
    pub(crate) fn query_prefix_cursor(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<DatabaseValuePrefixCursor<'_>, SemanticError> {
        self.prefix_cursor(prefix)
    }

    /// Lazily read one left-contiguous prefix of an unfiltered current value.
    ///
    /// Transaction processing requires exactly this point-current capability.
    /// Temporal, history, and custom-filter values use [`Self::prefix_cursor`]
    /// so this stricter transaction-only entry point remains unambiguous.
    pub fn current_prefix_cursor(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<DatabaseValuePrefixCursor<'_>, SemanticError> {
        prefix.validate()?;
        if !self.direct_current() {
            return Err(SemanticError::incorrect(
                "database/prefix-cursor-requires-current",
                "lazy transaction prefix access requires an unfiltered current database value",
            ));
        }
        self.memoized_prefix_cursor(false, prefix)
    }

    /// Lazily probe historical data. A caller that consumes only the first
    /// item deliberately does not populate the complete-prefix memo.
    pub(crate) fn history_prefix_cursor(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<DatabaseValuePrefixCursor<'_>, SemanticError> {
        prefix.validate()?;
        self.memoized_prefix_cursor(true, prefix)
    }

    /// Whether an attribute's AVET projection is usable at this exact database
    /// value. This differs from the attribute's configured `indexed`/`unique`
    /// schema flags while native background backfill is pending.
    ///
    /// Known attributes without AVET return `false`; unknown attribute ids or
    /// idents return `database/unknown-attribute`. This does not wait for an
    /// index build or refresh this immutable value. Temporal/filter views
    /// retain their captured basis's schema and physical readiness.
    pub fn has_avet(&self, attribute: &AttributeName) -> Result<bool, SemanticError> {
        Ok(self.physical_avet_ready(self.resolve_attribute(attribute)?))
    }

    /// Whether the physical AVET projection for one logically indexed
    /// attribute is complete at this immutable basis. Logical schema alone is
    /// insufficient while a background backfill is pending.
    pub(crate) fn physical_avet_ready(&self, attribute: u32) -> bool {
        if !schema_has_avet(self.schema(), attribute) {
            return false;
        }
        match &self.basis {
            ReadBasis::Eager(_) => true,
            ReadBasis::Block(snapshot) => snapshot.avet_ready(attribute),
            ReadBasis::TransactionOverlay(overlay) => {
                overlay.newly_enabled_avet.binary_search(&attribute).is_ok()
                    || overlay.base.physical_avet_ready(attribute)
            }
        }
    }

    /// Coordinate retained by a queued native transaction report. This is
    /// observability for queued root/generation retention, not a read capability.
    pub(crate) fn native_retention_coordinate(&self) -> Option<(u64, Option<crate::Digest>)> {
        match &self.basis {
            ReadBasis::Eager(_) => None,
            ReadBasis::Block(snapshot) => (!snapshot.is_repository())
                .then_some((snapshot.generation(), snapshot.captured_root().indexes)),
            ReadBasis::TransactionOverlay(overlay) => overlay.base.native_retention_coordinate(),
        }
    }

    pub fn values(&self, entity: u64, attribute: u32) -> Result<Vec<Value>, SemanticError> {
        Ok(self
            .datoms_with_prefix(&IndexPrefix::Eavt {
                entity,
                attribute: Some(attribute),
                value: None,
            })?
            .into_iter()
            .map(|datom| datom.value)
            .collect())
    }

    /// Resolve a lookup ref against this exact value. `:db/ident` follows the
    /// recovered dictionary fast path; all other identities use windowed AVET.
    pub fn lookup(&self, attribute: u32, value: &Value) -> Result<Option<u64>, SemanticError> {
        self.lookup_with_control(attribute, value, &mut |_| Ok(true))
    }

    pub(crate) fn lookup_with_control(
        &self,
        attribute: u32,
        value: &Value,
        control: &mut dyn FnMut(Option<&Datom>) -> Result<bool, SemanticError>,
    ) -> Result<Option<u64>, SemanticError> {
        let schema = self.schema().attribute(attribute)?;
        if schema.unique.is_none() {
            return Err(SemanticError::incorrect(
                "transaction/lookup-non-unique",
                format!("attribute {} is not unique", schema.ident.qualified_name()),
            ));
        }
        self.schema().validate_value(schema, value)?;
        if u64::from(attribute) == DB_IDENT {
            let Value::Keyword(ident) = value else {
                return Ok(None);
            };
            return Ok(self.entid(ident));
        }
        Ok(self
            .prefix_cursor(&IndexPrefix::Avet {
                attribute,
                value: Some(value.clone()),
                entity: None,
            })?
            .next_with_control(control)
            .transpose()?
            .map(|datom| datom.entity))
    }

    /// Normalize a zero-to-four-component EAVT boundary against this exact
    /// database value.
    pub fn eavt_boundary(
        &self,
        components: IndexComponents<
            EntityIdentifier,
            AttributeName,
            RawIndexValue,
            IndexTransaction,
        >,
    ) -> Result<IndexBoundary, SemanticError> {
        let components = match components {
            IndexComponents::Empty => IndexComponents::Empty,
            IndexComponents::One(entity) => {
                IndexComponents::One(self.require_index_entity(&entity)?)
            }
            IndexComponents::Two(entity, attribute) => IndexComponents::Two(
                self.require_index_entity(&entity)?,
                self.resolve_attribute(&attribute)?,
            ),
            IndexComponents::Three(entity, attribute, value) => {
                let entity = self.require_index_entity(&entity)?;
                let attribute = self.resolve_attribute(&attribute)?;
                let value = self.normalize_index_value(attribute, value)?;
                IndexComponents::Three(entity, attribute, value)
            }
            IndexComponents::Four(entity, attribute, value, transaction) => {
                let entity = self.require_index_entity(&entity)?;
                let attribute = self.resolve_attribute(&attribute)?;
                let value = self.normalize_index_value(attribute, value)?;
                IndexComponents::Four(entity, attribute, value, transaction)
            }
        };
        Self::validated_boundary(IndexBoundary::Eavt(components))
    }

    /// Normalize a zero-to-four-component AEVT boundary against this exact
    /// database value.
    pub fn aevt_boundary(
        &self,
        components: IndexComponents<
            AttributeName,
            EntityIdentifier,
            RawIndexValue,
            IndexTransaction,
        >,
    ) -> Result<IndexBoundary, SemanticError> {
        let components = match components {
            IndexComponents::Empty => IndexComponents::Empty,
            IndexComponents::One(attribute) => {
                IndexComponents::One(self.resolve_attribute(&attribute)?)
            }
            IndexComponents::Two(attribute, entity) => IndexComponents::Two(
                self.resolve_attribute(&attribute)?,
                self.require_index_entity(&entity)?,
            ),
            IndexComponents::Three(attribute, entity, value) => {
                let attribute = self.resolve_attribute(&attribute)?;
                let entity = self.require_index_entity(&entity)?;
                let value = self.normalize_index_value(attribute, value)?;
                IndexComponents::Three(attribute, entity, value)
            }
            IndexComponents::Four(attribute, entity, value, transaction) => {
                let attribute = self.resolve_attribute(&attribute)?;
                let entity = self.require_index_entity(&entity)?;
                let value = self.normalize_index_value(attribute, value)?;
                IndexComponents::Four(attribute, entity, value, transaction)
            }
        };
        Self::validated_boundary(IndexBoundary::Aevt(components))
    }

    /// Normalize a zero-to-four-component AVET boundary and require the
    /// qualified attribute's logical membership and physical readiness.
    pub fn avet_boundary(
        &self,
        components: IndexComponents<
            AttributeName,
            RawIndexValue,
            EntityIdentifier,
            IndexTransaction,
        >,
    ) -> Result<IndexBoundary, SemanticError> {
        let components = match components {
            IndexComponents::Empty => IndexComponents::Empty,
            IndexComponents::One(attribute) => {
                IndexComponents::One(self.resolve_ready_avet_attribute(&attribute)?)
            }
            IndexComponents::Two(attribute, value) => {
                let attribute = self.resolve_ready_avet_attribute(&attribute)?;
                let value = self.normalize_index_value(attribute, value)?;
                IndexComponents::Two(attribute, value)
            }
            IndexComponents::Three(attribute, value, entity) => {
                let attribute = self.resolve_ready_avet_attribute(&attribute)?;
                let value = self.normalize_index_value(attribute, value)?;
                let entity = self.require_index_entity(&entity)?;
                IndexComponents::Three(attribute, value, entity)
            }
            IndexComponents::Four(attribute, value, entity, transaction) => {
                let attribute = self.resolve_ready_avet_attribute(&attribute)?;
                let value = self.normalize_index_value(attribute, value)?;
                let entity = self.require_index_entity(&entity)?;
                IndexComponents::Four(attribute, value, entity, transaction)
            }
        };
        Self::validated_boundary(IndexBoundary::Avet(components))
    }

    /// Normalize a zero-to-four-component VAET boundary. Its leading value is
    /// always an entity reference, independent of whether an attribute suffix
    /// is present.
    pub fn vaet_boundary(
        &self,
        components: IndexComponents<
            RawIndexValue,
            AttributeName,
            EntityIdentifier,
            IndexTransaction,
        >,
    ) -> Result<IndexBoundary, SemanticError> {
        let components = match components {
            IndexComponents::Empty => IndexComponents::Empty,
            IndexComponents::One(value) => IndexComponents::One(self.normalize_vaet_value(value)?),
            IndexComponents::Two(value, attribute) => IndexComponents::Two(
                self.normalize_vaet_value(value)?,
                self.resolve_attribute(&attribute)?,
            ),
            IndexComponents::Three(value, attribute, entity) => IndexComponents::Three(
                self.normalize_vaet_value(value)?,
                self.resolve_attribute(&attribute)?,
                self.require_index_entity(&entity)?,
            ),
            IndexComponents::Four(value, attribute, entity, transaction) => IndexComponents::Four(
                self.normalize_vaet_value(value)?,
                self.resolve_attribute(&attribute)?,
                self.require_index_entity(&entity)?,
                transaction,
            ),
        };
        Self::validated_boundary(IndexBoundary::Vaet(components))
    }

    fn validated_boundary(boundary: IndexBoundary) -> Result<IndexBoundary, SemanticError> {
        boundary.validate()?;
        Ok(boundary)
    }

    fn require_index_entity(&self, identifier: &EntityIdentifier) -> Result<u64, SemanticError> {
        self.resolve_entity_identifier(identifier)?.ok_or_else(|| {
            SemanticError::incorrect(
                "index/unresolved-entity",
                "raw index boundary entity does not resolve in this database value",
            )
        })
    }

    fn resolve_ready_avet_attribute(
        &self,
        attribute: &AttributeName,
    ) -> Result<u32, SemanticError> {
        let attribute = self.resolve_attribute(attribute)?;
        let schema = self.schema().attribute(attribute)?;
        if !(schema.indexed || schema.unique.is_some()) {
            return Err(SemanticError::incorrect(
                "index/attribute-not-in-avet",
                format!(
                    "attribute {} is not present in AVET",
                    schema.ident.qualified_name()
                ),
            ));
        }
        if !self.physical_avet_ready(attribute) {
            return Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "index/avet-not-ready",
                format!(
                    "AVET backfill is not ready for attribute {}",
                    schema.ident.qualified_name()
                ),
            ));
        }
        Ok(attribute)
    }

    fn normalize_vaet_value(&self, value: RawIndexValue) -> Result<Value, SemanticError> {
        match value {
            RawIndexValue::Entity(identifier) => {
                self.require_index_entity(&identifier).map(Value::Ref)
            }
            RawIndexValue::Stored(Value::Ref(entity)) => {
                eid_to_eidx(entity)?;
                Ok(Value::Ref(entity))
            }
            RawIndexValue::Stored(_) | RawIndexValue::Tuple(_) => Err(SemanticError::incorrect(
                "index/vaet-value-not-ref",
                "VAET boundary value must be an entity identifier or stored reference",
            )),
        }
    }

    fn normalize_index_value(
        &self,
        attribute: u32,
        value: RawIndexValue,
    ) -> Result<Value, SemanticError> {
        let schema = self.schema().attribute(attribute)?;
        let mut value = match (schema.value_type, value) {
            (ValueType::Ref, RawIndexValue::Entity(identifier)) => {
                Value::Ref(self.require_index_entity(&identifier)?)
            }
            (ValueType::Tuple, RawIndexValue::Tuple(slots)) => {
                self.normalize_index_tuple(schema, slots)?
            }
            (_, RawIndexValue::Stored(value)) => {
                Self::validate_stored_index_refs(&value)?;
                value
            }
            (_, RawIndexValue::Entity(identifier)) => {
                Value::Ref(self.require_index_entity(&identifier)?)
            }
            (_, RawIndexValue::Tuple(_)) => {
                return Err(SemanticError::incorrect(
                    "transaction/value-type",
                    format!(
                        "attribute {} requires {:?}, got tuple",
                        schema.ident.qualified_name(),
                        schema.value_type
                    ),
                ));
            }
        };
        // A virtual tuple start is allowed to omit trailing slots, unlike an
        // assertion. Pad only for the existing schema validator, so all slot
        // types, scalar limits and reference checks remain unchanged. At most
        // eight slots are admitted and no supplied payload is cloned. Remove
        // the padding before comparison: [x] must sort before [x, nil].
        let prefix_length =
            if let (ValueType::Tuple, Value::Tuple(slots)) = (schema.value_type, &mut value) {
                let length = slots.len();
                let stored_length = match schema.tuple.as_ref() {
                    Some(TupleSpec::Homogeneous(_)) if length <= 8 => length.max(2),
                    Some(TupleSpec::Heterogeneous(types)) if length <= types.len() => types.len(),
                    Some(TupleSpec::Composite(attributes)) if length <= attributes.len() => {
                        attributes.len()
                    }
                    // Leave invalid oversized values untouched: the ordinary
                    // validator supplies its established arity error below.
                    _ => length,
                };
                slots.resize_with(stored_length, || None);
                Some(length)
            } else {
                None
            };
        self.schema().validate_value(schema, &value)?;
        if let (Some(length), Value::Tuple(slots)) = (prefix_length, &mut value) {
            slots.truncate(length);
        }
        Ok(value)
    }

    fn normalize_index_tuple(
        &self,
        attribute: &crate::Attribute,
        slots: Vec<Option<RawIndexValue>>,
    ) -> Result<Value, SemanticError> {
        let value_types = match attribute.tuple.as_ref() {
            Some(TupleSpec::Homogeneous(value_type)) => vec![*value_type; 8],
            Some(TupleSpec::Heterogeneous(value_types)) => value_types.clone(),
            Some(TupleSpec::Composite(attributes)) => attributes
                .iter()
                .map(|attribute| self.schema().attribute(*attribute).map(|a| a.value_type))
                .collect::<Result<Vec<_>, _>>()?,
            None => {
                return Err(SemanticError::incorrect(
                    "schema/missing-tuple-spec",
                    "tuple specification missing",
                ));
            }
        };
        if slots.len() > value_types.len() {
            return Err(SemanticError::incorrect(
                "transaction/invalid-tuple-length",
                format!("tuple boundary accepts at most {} slots", value_types.len()),
            ));
        }
        let normalized = value_types
            .into_iter()
            .zip(slots)
            .map(|(value_type, slot)| {
                slot.map(|slot| self.normalize_index_tuple_slot(value_type, slot))
                    .transpose()
            })
            .collect::<Result<Vec<_>, _>>()?;
        Ok(Value::Tuple(normalized))
    }

    fn normalize_index_tuple_slot(
        &self,
        value_type: ValueType,
        value: RawIndexValue,
    ) -> Result<Value, SemanticError> {
        match value {
            RawIndexValue::Entity(identifier) => {
                self.require_index_entity(&identifier).map(Value::Ref)
            }
            RawIndexValue::Stored(value) => {
                Self::validate_stored_index_refs(&value)?;
                Ok(value)
            }
            RawIndexValue::Tuple(_) => Err(SemanticError::incorrect(
                "transaction/invalid-tuple-element",
                format!("tuple slot requires {value_type:?}, got tuple"),
            )),
        }
    }

    fn validate_stored_index_refs(value: &Value) -> Result<(), SemanticError> {
        match value {
            Value::Ref(entity) => {
                eid_to_eidx(*entity)?;
            }
            Value::Tuple(slots) => {
                for value in slots.iter().flatten() {
                    Self::validate_stored_index_refs(value)?;
                }
            }
            _ => {}
        }
        Ok(())
    }

    /// Resolve the public eid/ident/lookup-ref forms against this exact value.
    pub fn resolve_entity_identifier(
        &self,
        identifier: &EntityIdentifier,
    ) -> Result<Option<u64>, SemanticError> {
        self.resolve_entity_identifier_with_control(identifier, &mut |_| Ok(true))
    }

    pub(crate) fn resolve_entity_identifier_with_control(
        &self,
        identifier: &EntityIdentifier,
        control: &mut dyn FnMut(Option<&Datom>) -> Result<bool, SemanticError>,
    ) -> Result<Option<u64>, SemanticError> {
        match identifier {
            EntityIdentifier::Id(entity) => {
                eid_to_eidx(*entity)?;
                Ok(Some(*entity))
            }
            EntityIdentifier::Ident(ident) => Ok(self.entid(ident)),
            EntityIdentifier::Lookup { attribute, value } => {
                let attribute = self.resolve_attribute(attribute)?;
                self.lookup_with_control(attribute, value, control)
            }
        }
    }

    fn resolve_attribute(&self, name: &AttributeName) -> Result<u32, SemanticError> {
        let attribute = match name {
            AttributeName::Id(attribute) => *attribute,
            AttributeName::Ident(ident) => {
                let entity = self.entid(ident).ok_or_else(|| {
                    SemanticError::incorrect(
                        "database/unknown-attribute",
                        format!("unknown attribute {}", ident.qualified_name()),
                    )
                })?;
                schema_eid_to_attr_id(entity).map_err(|_| {
                    SemanticError::incorrect(
                        "database/unknown-attribute",
                        format!("{} does not identify an attribute", ident.qualified_name()),
                    )
                })?
            }
        };
        self.schema().attribute(attribute).map_err(|_| {
            SemanticError::incorrect(
                "database/unknown-attribute",
                format!("unknown attribute id {attribute}"),
            )
        })?;
        Ok(attribute)
    }

    fn direct_current(&self) -> bool {
        !self.history && self.as_of_t.is_none() && self.since_t.is_none() && self.filters.is_empty()
    }

    fn validate_raw_boundary_access(&self, boundary: &IndexBoundary) -> Result<(), SemanticError> {
        boundary.validate()?;
        if let Some(attribute) = boundary.avet_attribute() {
            self.resolve_ready_avet_attribute(&AttributeName::Id(attribute))?;
        }
        Ok(())
    }

    fn direct_history(&self) -> bool {
        self.history && self.as_of_t.is_none() && self.since_t.is_none() && self.filters.is_empty()
    }

    fn basis_seek_boundary_cursor(
        &self,
        history: bool,
        boundary: &IndexBoundary,
        observer: Option<Arc<LogicalReadObserver>>,
        physical_context: Option<Arc<TransactionReadContext>>,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        let normalized = boundary.normalized()?;
        let order = normalized.order();
        match &self.basis {
            ReadBasis::Eager(database) => {
                let datoms = database.index_datoms(history, order);
                let start = datoms.partition_point(|datom| normalized.compare_datom(datom).is_lt());
                Ok(DatabaseValueScanCursor {
                    inner: DatabaseValueScanCursorInner::Eager(datoms[start..].iter().cloned()),
                    observer,
                    physical_context: None,
                    physical_recorded: false,
                    operation: crate::sql_io::OperationContext::current(),
                    failed: false,
                })
            }
            ReadBasis::Block(snapshot) => Ok(DatabaseValueScanCursor {
                inner: DatabaseValueScanCursorInner::Block(Box::new(
                    snapshot.boundary_cursor(history, boundary, false)?,
                )),
                observer,
                physical_context,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                failed: false,
            }),
            ReadBasis::TransactionOverlay(overlay) => Ok(DatabaseValueScanCursor {
                inner: DatabaseValueScanCursorInner::Overlay(Box::new(overlay.scan_cursor_from(
                    history,
                    order,
                    observer,
                    physical_context,
                    Some((boundary, false, false)),
                )?)),
                observer: None,
                physical_context: None,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                failed: false,
            }),
        }
    }

    fn basis_seek_boundary_cursor_biased(
        &self,
        history: bool,
        boundary: &IndexBoundary,
        after: bool,
        observer: Option<Arc<LogicalReadObserver>>,
        physical_context: Option<Arc<TransactionReadContext>>,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        if !after {
            return self.basis_seek_boundary_cursor(history, boundary, observer, physical_context);
        }
        let normalized = boundary.normalized()?.after_prefix();
        let order = normalized.order();
        let inner = match &self.basis {
            ReadBasis::Eager(database) => {
                let datoms = database.index_datoms(history, order);
                let start = datoms.partition_point(|datom| normalized.compare_datom(datom).is_lt());
                DatabaseValueScanCursorInner::Eager(datoms[start..].iter().cloned())
            }
            ReadBasis::Block(snapshot) => DatabaseValueScanCursorInner::Block(Box::new(
                snapshot.normalized_cursor(history, normalized, false),
            )),
            ReadBasis::TransactionOverlay(overlay) => {
                return Ok(DatabaseValueScanCursor {
                    inner: DatabaseValueScanCursorInner::Overlay(Box::new(
                        overlay.scan_cursor_from(
                            history,
                            order,
                            observer,
                            physical_context,
                            Some((boundary, false, true)),
                        )?,
                    )),
                    observer: None,
                    physical_context: None,
                    physical_recorded: false,
                    operation: crate::sql_io::OperationContext::current(),
                    failed: false,
                });
            }
        };
        Ok(DatabaseValueScanCursor {
            inner,
            observer,
            physical_context,
            physical_recorded: false,
            operation: crate::sql_io::OperationContext::current(),
            failed: false,
        })
    }

    fn basis_reverse_boundary_cursor(
        &self,
        history: bool,
        boundary: &IndexBoundary,
        observer: Option<Arc<LogicalReadObserver>>,
        physical_context: Option<Arc<TransactionReadContext>>,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        let normalized = boundary.normalized()?;
        let order = normalized.order();
        match &self.basis {
            ReadBasis::Eager(database) => {
                let datoms = database.index_datoms(history, order);
                let end = datoms.partition_point(|datom| !normalized.compare_datom(datom).is_gt());
                Ok(DatabaseValueScanCursor {
                    inner: DatabaseValueScanCursorInner::EagerReverse(
                        datoms[..end].iter().rev().cloned(),
                    ),
                    observer,
                    physical_context: None,
                    physical_recorded: false,
                    operation: crate::sql_io::OperationContext::current(),
                    failed: false,
                })
            }
            ReadBasis::Block(snapshot) => Ok(DatabaseValueScanCursor {
                inner: DatabaseValueScanCursorInner::Block(Box::new(
                    snapshot.boundary_cursor(history, boundary, true)?,
                )),
                observer,
                physical_context,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                failed: false,
            }),
            ReadBasis::TransactionOverlay(overlay) => Ok(DatabaseValueScanCursor {
                inner: DatabaseValueScanCursorInner::Overlay(Box::new(overlay.scan_cursor_from(
                    history,
                    order,
                    observer,
                    physical_context,
                    Some((boundary, true, false)),
                )?)),
                observer: None,
                physical_context: None,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                failed: false,
            }),
        }
    }

    fn basis_scan_cursor(
        &self,
        history: bool,
        order: IndexOrder,
        observer: Option<Arc<LogicalReadObserver>>,
        physical_context: Option<Arc<TransactionReadContext>>,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        match &self.basis {
            ReadBasis::Eager(database) => Ok(DatabaseValueScanCursor {
                inner: DatabaseValueScanCursorInner::Eager(
                    database.index_datoms(history, order).iter().cloned(),
                ),
                observer,
                physical_context: None,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                failed: false,
            }),
            ReadBasis::Block(snapshot) => Ok(DatabaseValueScanCursor {
                inner: DatabaseValueScanCursorInner::Block(Box::new(
                    snapshot.cursor(history, order),
                )),
                observer,
                physical_context,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                failed: false,
            }),
            ReadBasis::TransactionOverlay(overlay) => Ok(DatabaseValueScanCursor {
                // The overlay owns observation so an AVET-enablement source
                // range can be charged while it is reordered, without later
                // double-charging those precharged datoms.
                inner: DatabaseValueScanCursorInner::Overlay(Box::new(overlay.scan_cursor(
                    history,
                    order,
                    observer,
                    physical_context,
                )?)),
                observer: None,
                physical_context: None,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                failed: false,
            }),
        }
    }

    fn basis_prefix_cursor(
        &self,
        history: bool,
        prefix: &IndexPrefix,
        observer: Option<Arc<LogicalReadObserver>>,
        physical_context: Option<Arc<TransactionReadContext>>,
    ) -> Result<DatabaseValuePrefixCursor<'_>, SemanticError> {
        match &self.basis {
            ReadBasis::Eager(database) => {
                let datoms = if history {
                    database.history_with_prefix(prefix)?
                } else {
                    database.datoms_with_prefix(prefix)?
                };
                Ok(DatabaseValuePrefixCursor {
                    inner: DatabaseValuePrefixCursorInner::Eager(datoms.iter().cloned()),
                    observer,
                    physical_context: None,
                    physical_recorded: false,
                    operation: crate::sql_io::OperationContext::current(),
                    memo_source: None,
                    memo_hit: false,
                    failed: false,
                })
            }
            ReadBasis::Block(snapshot) => Ok(DatabaseValuePrefixCursor {
                inner: DatabaseValuePrefixCursorInner::Block(Box::new(
                    snapshot.prefix_cursor(history, prefix)?,
                )),
                observer,
                physical_context,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                memo_source: None,
                memo_hit: false,
                failed: false,
            }),
            ReadBasis::TransactionOverlay(overlay) => {
                Ok(DatabaseValuePrefixCursor {
                    // As with full scans, the overlay owns observation so its one
                    // necessarily reordered AVET backfill is bounded at source.
                    inner: DatabaseValuePrefixCursorInner::Overlay(Box::new(
                        overlay.prefix_cursor(history, prefix, observer, physical_context)?,
                    )),
                    observer: None,
                    physical_context: None,
                    physical_recorded: false,
                    operation: crate::sql_io::OperationContext::current(),
                    memo_source: None,
                    memo_hit: false,
                    failed: false,
                })
            }
        }
    }

    fn memoized_prefix_cursor(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<DatabaseValuePrefixCursor<'_>, SemanticError> {
        crate::transaction_hints::record_prefix(history, prefix);
        let Some(context) = &self.read_context else {
            return self.basis_prefix_cursor(history, prefix, self.read_observer.clone(), None);
        };
        let key = PrefixMemoKey {
            value: Arc::clone(&self.read_identity),
            history,
            prefix: prefix.clone(),
        };
        if let Some(datoms) = context.lookup(&key)? {
            return Ok(DatabaseValuePrefixCursor {
                inner: DatabaseValuePrefixCursorInner::Memoized { datoms, next: 0 },
                observer: Some(context.observer()),
                physical_context: None,
                physical_recorded: false,
                operation: crate::sql_io::OperationContext::current(),
                memo_source: None,
                memo_hit: true,
                failed: false,
            });
        }
        let mut cursor = match self.basis_prefix_cursor(
            history,
            prefix,
            Some(context.observer()),
            Some(Arc::clone(context)),
        ) {
            Ok(cursor) => cursor,
            Err(error) => {
                context.reject()?;
                return Err(error);
            }
        };
        let retained_bytes = key.retained_bytes();
        cursor.memo_source = Some(PrefixMemoSource {
            context: Arc::clone(context),
            key: Some(key),
            datoms: Vec::new(),
            retained_bytes,
            cacheable: retained_bytes <= context.max_retained_bytes,
            completed: false,
        });
        Ok(cursor)
    }

    fn without_filters(&self) -> Self {
        let mut database = self.clone();
        database.read_identity = Arc::new(ReadValueIdentity);
        database.filters = Arc::default();
        database
    }
}

impl TransactionOverlay {
    fn scan_cursor(
        &self,
        history: bool,
        order: IndexOrder,
        observer: Option<Arc<LogicalReadObserver>>,
        physical_context: Option<Arc<TransactionReadContext>>,
    ) -> Result<TransactionOverlayScanCursor<'_>, SemanticError> {
        self.scan_cursor_from(history, order, observer, physical_context, None)
    }

    fn scan_cursor_from(
        &self,
        history: bool,
        order: IndexOrder,
        observer: Option<Arc<LogicalReadObserver>>,
        physical_context: Option<Arc<TransactionReadContext>>,
        seek: Option<(&IndexBoundary, bool, bool)>,
    ) -> Result<TransactionOverlayScanCursor<'_>, SemanticError> {
        let reverse = seek.is_some_and(|(_, reverse, _)| reverse);
        let base = TransactionOverlayBaseCursor::Scan(match (&self.base.basis, seek) {
            (ReadBasis::Block(snapshot), Some((boundary, reverse, after)))
                if boundary.avet_attribute().is_some_and(|attribute| {
                    self.newly_enabled_avet.binary_search(&attribute).is_ok()
                }) =>
            {
                DatabaseValueScanCursor {
                    inner: DatabaseValueScanCursorInner::Block(Box::new(
                        snapshot.normalized_cursor(
                            history,
                            if after {
                                boundary.normalized()?.after_prefix()
                            } else {
                                boundary.normalized()?
                            },
                            reverse,
                        ),
                    )),
                    observer: None,
                    physical_context: physical_context.clone(),
                    physical_recorded: false,
                    operation: crate::sql_io::OperationContext::current(),
                    failed: false,
                }
            }
            (_, Some((boundary, true, _))) => self.base.basis_reverse_boundary_cursor(
                history,
                boundary,
                None,
                physical_context.clone(),
            )?,
            (_, Some((boundary, false, after))) => self.base.basis_seek_boundary_cursor_biased(
                history,
                boundary,
                after,
                None,
                physical_context.clone(),
            )?,
            (_, None) => {
                self.base
                    .basis_scan_cursor(history, order, None, physical_context.clone())?
            }
        });
        let removals = self.indexes.removals.clone();
        let mut delta = Vec::<OverlayCursorDatom>::new();

        // `add-avet` copies the attribute's existing AEVT working set into
        // the transaction-local AVET. Do the same bounded, attribute-local
        // backfill when this transaction first enables AVET; the durable base
        // remains streamed and untouched.
        if order == IndexOrder::Avet {
            for attribute in self.newly_enabled_avet.iter().copied() {
                for datom in self.base.basis_prefix_cursor(
                    history,
                    &IndexPrefix::Aevt {
                        attribute,
                        entity: None,
                        value: None,
                    },
                    None,
                    physical_context.clone(),
                )? {
                    let datom = datom?;
                    if let Some(observer) = &observer {
                        observer.charge_datom(&datom)?;
                    }
                    if history || !removals.contains(&datom) {
                        delta.push(OverlayCursorDatom {
                            datom,
                            precharged: true,
                        });
                    }
                }
            }
        }

        let normalized = seek
            .map(|(boundary, _, after)| {
                boundary.normalized().map(|normalized| {
                    if after {
                        normalized.after_prefix()
                    } else {
                        normalized
                    }
                })
            })
            .transpose()?;
        let indexed = self.indexes.cursor(
            history,
            order,
            |datom| {
                normalized
                    .as_ref()
                    .map_or(Ordering::Equal, |boundary| boundary.compare_datom(datom))
            },
            reverse,
            None,
        );
        Ok(TransactionOverlayScanCursor {
            base,
            delta: OverlayDeltaCursor::new(
                indexed,
                delta,
                self.schema.clone(),
                history,
                order,
                reverse,
                normalized.as_ref(),
            ),
            removals,
            schema: Arc::clone(&self.schema),
            history,
            order,
            reverse,
            observer,
            base_next: None,
            delta_next: None,
            failed: false,
        })
    }

    fn prefix_cursor(
        &self,
        history: bool,
        prefix: &IndexPrefix,
        observer: Option<Arc<LogicalReadObserver>>,
        physical_context: Option<Arc<TransactionReadContext>>,
    ) -> Result<TransactionOverlayScanCursor<'_>, SemanticError> {
        let source_prefix = self.source_prefix(prefix);
        let removals = self.indexes.removals.clone();
        let mut delta = Vec::<OverlayCursorDatom>::new();

        // A newly enabled AVET range is physically absent from db-before and
        // must be sourced from AEVT, whose order cannot be merged directly
        // into AVET. This is the one explicit broad prefix operation. Charge
        // each source datom before retaining it so a small transaction cap
        // stops the reorder before it can allocate the complete range.
        let base = if source_prefix.order() == prefix.order() {
            TransactionOverlayBaseCursor::Prefix(self.base.basis_prefix_cursor(
                history,
                &source_prefix,
                None,
                physical_context.clone(),
            )?)
        } else {
            for datom in self.base.basis_prefix_cursor(
                history,
                &source_prefix,
                None,
                physical_context.clone(),
            )? {
                let datom = datom?;
                if let Some(observer) = &observer {
                    observer.charge_datom(&datom)?;
                }
                if overlay_index_member(&self.schema, &datom, prefix.order())
                    && compare_prefix(&datom, prefix).is_eq()
                    && (history || !removals.contains(&datom))
                {
                    delta.push(OverlayCursorDatom {
                        datom,
                        precharged: true,
                    });
                }
            }
            TransactionOverlayBaseCursor::Empty
        };

        let indexed = self.indexes.cursor(
            history,
            prefix.order(),
            |datom| compare_prefix(datom, prefix),
            false,
            Some(prefix.clone()),
        );

        Ok(TransactionOverlayScanCursor {
            base,
            delta: OverlayDeltaCursor::new(
                indexed,
                delta,
                self.schema.clone(),
                history,
                prefix.order(),
                false,
                None,
            ),
            removals,
            schema: Arc::clone(&self.schema),
            history,
            order: prefix.order(),
            reverse: false,
            observer,
            base_next: None,
            delta_next: None,
            failed: false,
        })
    }

    fn source_prefix(&self, prefix: &IndexPrefix) -> IndexPrefix {
        match prefix {
            // Only AVET enablement needs the documented linear attribute
            // backfill. Steady indexed reads retain the caller's selective
            // AVET seek, while disabling can filter the old AVET range away.
            IndexPrefix::Avet { attribute, .. }
                if self.newly_enabled_avet.binary_search(attribute).is_ok() =>
            {
                IndexPrefix::Aevt {
                    attribute: *attribute,
                    entity: None,
                    value: None,
                }
            }
            _ => prefix.clone(),
        }
    }
}

impl TransactionOverlayScanCursor<'_> {
    fn fill_base(&mut self, control: &mut ReadCursorControl<'_>) -> Result<(), SemanticError> {
        while self.base_next.is_none() {
            // Removed/disabled base facts can form a long prefix. Poll even
            // when none of them survives the overlay merge to reach a window.
            // Range-stop predicates still see only ordered merged datoms, not
            // out-of-order base/delta peeks.
            if !control.check(None)? {
                break;
            }
            let Some(candidate) = self.base.next().transpose()? else {
                break;
            };
            if !overlay_index_member(&self.schema, &candidate, self.order) {
                continue;
            }
            if !self.history && self.removals.contains(&candidate) {
                continue;
            }
            self.base_next = Some(OverlayCursorDatom {
                datom: candidate,
                precharged: false,
            });
        }
        Ok(())
    }

    fn next_result(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Result<Option<OverlayCursorDatom>, SemanticError> {
        self.fill_base(control)?;
        if control.stopped {
            return Ok(None);
        }
        if self.delta_next.is_none() {
            self.delta_next = self.delta.next_controlled(control)?;
        }
        if control.stopped {
            return Ok(None);
        }
        match (&self.base_next, &self.delta_next) {
            (None, None) => Ok(None),
            (Some(_), None) => Ok(self.base_next.take()),
            (None, Some(_)) => Ok(self.delta_next.take()),
            (Some(base), Some(delta))
                if !self.history && same_stored_eav(&base.datom, &delta.datom) =>
            {
                if u64::from(delta.datom.attribute) == crate::DB_ALTER_ATTRIBUTE {
                    // Repeated alter hooks are distinct immutable history
                    // events, and the newest one is the current coordinate.
                    self.base_next = None;
                    Ok(self.delta_next.take())
                } else {
                    self.delta_next = None;
                    Ok(self.base_next.take())
                }
            }
            (Some(base), Some(delta)) => match if self.reverse {
                delta.datom.cmp_in(&base.datom, self.order)
            } else {
                base.datom.cmp_in(&delta.datom, self.order)
            } {
                std::cmp::Ordering::Less => Ok(self.base_next.take()),
                std::cmp::Ordering::Greater => Ok(self.delta_next.take()),
                std::cmp::Ordering::Equal => {
                    self.delta_next = None;
                    Ok(self.base_next.take())
                }
            },
        }
    }
}

impl TransactionOverlayScanCursor<'_> {
    fn next_controlled(
        &mut self,
        control: &mut ReadCursorControl<'_>,
    ) -> Option<Result<Datom, SemanticError>> {
        if self.failed {
            return None;
        }
        match self.next_result(control) {
            Ok(Some(item)) => {
                if !item.precharged
                    && let Some(observer) = &self.observer
                    && let Err(error) = observer.charge_datom(&item.datom)
                {
                    self.failed = true;
                    return Some(Err(error));
                }
                Some(Ok(item.datom))
            }
            Ok(None) => None,
            Err(error) => {
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}

fn overlay_index_member(schema: &Schema, datom: &Datom, order: IndexOrder) -> bool {
    match order {
        IndexOrder::Eavt | IndexOrder::Aevt => true,
        IndexOrder::Avet => schema
            .attribute(datom.attribute)
            .is_ok_and(|attribute| attribute.indexed || attribute.unique.is_some()),
        IndexOrder::Vaet => schema
            .attribute(datom.attribute)
            .is_ok_and(|attribute| attribute.value_type == crate::ValueType::Ref),
    }
}

fn schema_has_avet(schema: &Schema, attribute: u32) -> bool {
    schema
        .attribute(attribute)
        .is_ok_and(|attribute| attribute.indexed || attribute.unique.is_some())
}

fn same_stored_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
}

impl From<Database> for DatabaseValue {
    fn from(database: Database) -> Self {
        Self::eager(Arc::new(database))
    }
}

impl From<&Database> for DatabaseValue {
    fn from(database: &Database) -> Self {
        Self::from(database.clone())
    }
}

impl From<Arc<Database>> for DatabaseValue {
    fn from(database: Arc<Database>) -> Self {
        Self::eager(database)
    }
}

impl From<PeerSnapshot> for DatabaseValue {
    fn from(snapshot: PeerSnapshot) -> Self {
        Self::native(snapshot)
    }
}

impl From<&PeerSnapshot> for DatabaseValue {
    fn from(snapshot: &PeerSnapshot) -> Self {
        Self::native(snapshot.clone())
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, Clause, DB_IDENT, DataPattern, EntityRef, FindElement, FindSpec,
        Query, QueryControl, Term, TxOp, TxReport, TxValue, USER_PARTITION, Unique, ValueType,
        Variable, make_eid, t_to_tx,
    };
    use bigdecimal::BigDecimal;
    use std::cell::Cell;
    use std::str::FromStr;

    const AMOUNT: u32 = 1_000;
    const LINK: u32 = 1_001;
    const BOUNDARY_EMAIL: u32 = 1_000;
    const BOUNDARY_TEXT: u32 = 1_001;
    const BOUNDARY_LINK: u32 = 1_002;
    const BOUNDARY_HOMOGENEOUS_REFS: u32 = 1_003;
    const BOUNDARY_HETEROGENEOUS: u32 = 1_004;

    fn decimal(value: &str) -> Value {
        Value::BigDec(BigDecimal::from_str(value).unwrap())
    }

    #[test]
    fn speculative_index_sharing_scales_with_paths_and_selective_ranges() {
        std::thread::Builder::new().stack_size(256 * 1024).spawn(|| {
            fn indexes(value: &DatabaseValue) -> &OverlayIndexes {
                let ReadBasis::TransactionOverlay(overlay) = &value.basis else { panic!("overlay expected") };
                assert!(!matches!(overlay.base.basis, ReadBasis::TransactionOverlay(_)));
                &overlay.indexes
            }
            fn append(base: DatabaseValue, entity: u64, instant: i64) -> DatabaseValue {
                let basis = base.basis_t() + 1;
                let tx = t_to_tx(basis).unwrap();
                let schema = base.schema_arc();
                let frontier = base.eidx_frontier() + 2;
                let mut datoms = vec![
                    Datom { entity, attribute: AMOUNT, value: Value::Long(instant), tx, added: true },
                    Datom { entity: tx, attribute: crate::DB_TX_INSTANT as u32, value: Value::Instant(instant), tx, added: true },
                ];
                datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
                DatabaseValue::transaction_overlay(base, datoms.into(), schema, basis, frontier, instant).unwrap()
            }
            let mut schema = Schema::new();
            let mut amount = Attribute::new(AMOUNT, Keyword::new("item", "amount"), ValueType::Long, Cardinality::One);
            amount.indexed = true;
            schema.install(amount).unwrap();
            let original = Database::new(schema).unwrap().database_value();
            let original_basis = original.basis_t();
            let entity = make_eid(USER_PARTITION, 100_000).unwrap();
            let mut value = original.clone();
            let started = std::time::Instant::now();
            for depth in 1..=4096 {
                value = append(value, entity + depth, depth as i64);
                if [128, 512, 2048, 4096].contains(&depth) {
                    let (len, height, work) = indexes(&value).metrics();
                    let prefix = IndexPrefix::Avet { attribute: AMOUNT, value: Some(Value::Long((depth / 2) as i64)), entity: None };
                    let mut cursor = indexes(&value).cursor(false, IndexOrder::Avet, |datom| compare_prefix(datom, &prefix), false, Some(prefix.clone()));
                    let selected: Vec<_> = cursor.by_ref().collect();
                    assert_eq!(selected.len(), 1);
                    assert!(cursor.visited() <= u64::from(height) * 3 + 3);
                    assert!(work.nodes_created < len as u64 * u64::from(height + 4));
                    assert_eq!(value.datoms_with_prefix(&prefix).unwrap().len(), 1);
                    eprintln!("overlay depth={depth} history_datoms={len} history_height={height} cumulative_history_nodes_created={} cumulative_history_comparisons={} selective_avet_nodes_visited={} resident_inline_index_and_datom_bytes={} cumulative_elapsed_us={}; byte estimate excludes Arc/allocator headers and ident heaps", work.nodes_created, work.comparisons, cursor.visited(), indexes(&value).resident_bytes(), started.elapsed().as_micros());
                }
            }
            let base_nodes = indexes(&value).node_ids();
            let payload = indexes(&value).cursor(true, IndexOrder::Eavt, |_| Ordering::Equal, false, None).next().unwrap();
            let shared_payload = Arc::downgrade(&payload);
            drop(payload);
            let mut branches = Vec::new();
            let mut all_nodes = base_nodes.clone();
            let mut discarded_payloads = Vec::new();
            for branch in 0..128 {
                let fork = append(value.clone(), entity + 10_000 + branch, 5_000 + branch as i64);
                let nodes = indexes(&fork).node_ids();
                let shared = nodes.intersection(&base_nodes).count();
                assert!(shared > base_nodes.len() * 99 / 100);
                all_nodes.extend(nodes);
                let prefix = IndexPrefix::Eavt { entity: entity + 10_000 + branch, attribute: Some(AMOUNT), value: None };
                let datom = indexes(&fork).cursor(false, IndexOrder::Eavt, |datom| compare_prefix(datom, &prefix), false, Some(prefix.clone())).next().unwrap();
                discarded_payloads.push(Arc::downgrade(&datom));
                branches.push(fork);
            }
            let extra = all_nodes.len() - base_nodes.len();
            assert!(extra < 128 * 400);
            assert!(discarded_payloads.iter().all(|weak| weak.upgrade().is_some()));
            let drop_started = std::time::Instant::now();
            drop(branches);
            assert!(discarded_payloads.iter().all(|weak| weak.upgrade().is_none()));
            assert!(shared_payload.upgrade().is_some());
            eprintln!("overlay width=128 base_live_index_nodes={} retained_extra_path_nodes={extra} branch_drop_us={} discarded_new_payloads=128; prior payload stays owned", base_nodes.len(), drop_started.elapsed().as_micros());
            let last_drop = std::time::Instant::now();
            drop(value);
            assert!(shared_payload.upgrade().is_none());
            eprintln!("overlay depth=4096 final_drop_us={} stack_bytes=262144", last_drop.elapsed().as_micros());
            assert_eq!(original.basis_t(), original_basis);
        }).unwrap().join().unwrap();
    }

    fn overlay_for(report: &TxReport, tx_instant: i64) -> DatabaseValue {
        DatabaseValue::transaction_overlay(
            report.db_before.database_value(),
            Arc::from(report.tx_data.clone()),
            Arc::new(report.db_after.schema().clone()),
            report.db_after.basis_t(),
            report.db_after.eidx_frontier(),
            tx_instant,
        )
        .unwrap()
    }

    fn indexed(mut attribute: Attribute) -> Attribute {
        attribute.indexed = true;
        attribute
    }

    fn raw_boundary_fixture() -> (DatabaseValue, u64, u64, Keyword, Keyword) {
        let mut schema = Schema::new();
        schema
            .install(
                Attribute::new(
                    BOUNDARY_EMAIL,
                    Keyword::new("boundary", "email"),
                    ValueType::String,
                    Cardinality::One,
                )
                .unique(Unique::Identity),
            )
            .unwrap();
        schema
            .install(Attribute::new(
                BOUNDARY_TEXT,
                Keyword::new("boundary", "text"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        schema
            .install(indexed(Attribute::new(
                BOUNDARY_LINK,
                Keyword::new("boundary", "link"),
                ValueType::Ref,
                Cardinality::Many,
            )))
            .unwrap();
        schema
            .install(indexed(
                Attribute::new(
                    BOUNDARY_HOMOGENEOUS_REFS,
                    Keyword::new("boundary", "homogeneous-refs"),
                    ValueType::Tuple,
                    Cardinality::Many,
                )
                .tuple(TupleSpec::Homogeneous(ValueType::Ref)),
            ))
            .unwrap();
        schema
            .install(indexed(
                Attribute::new(
                    BOUNDARY_HETEROGENEOUS,
                    Keyword::new("boundary", "heterogeneous"),
                    ValueType::Tuple,
                    Cardinality::Many,
                )
                .tuple(TupleSpec::Heterogeneous(vec![
                    ValueType::Ref,
                    ValueType::String,
                    ValueType::Ref,
                ])),
            ))
            .unwrap();

        let alice_ident = Keyword::new("boundary", "alice");
        let bob_ident = Keyword::new("boundary", "bob");
        let report = Database::new(schema)
            .unwrap()
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("alice".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(alice_ident.clone()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("alice".into()),
                        attribute: BOUNDARY_EMAIL,
                        value: Value::String("alice@example.test".into()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("bob".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(bob_ident.clone()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("bob".into()),
                        attribute: BOUNDARY_EMAIL,
                        value: Value::String("bob@example.test".into()).into(),
                    },
                ],
                1_000,
            )
            .unwrap();
        (
            report.db_after.database_value(),
            report.tempids["alice"],
            report.tempids["bob"],
            alice_ident,
            bob_ident,
        )
    }

    fn assert_same_stored_datoms(actual: &[Datom], expected: &[Datom]) {
        assert_eq!(actual.len(), expected.len(), "different datom counts");
        for (actual, expected) in actual.iter().zip(expected) {
            assert_eq!(actual.entity, expected.entity);
            assert_eq!(actual.attribute, expected.attribute);
            assert_eq!(actual.tx, expected.tx);
            assert_eq!(actual.added, expected.added);
            assert!(
                actual.value.stored_eq(&expected.value),
                "different stored values: {:?} and {:?}",
                actual.value,
                expected.value
            );
        }
    }

    fn expected_seek(
        database: &DatabaseValue,
        boundary: &IndexBoundary,
        reverse: bool,
    ) -> Vec<Datom> {
        let normalized = boundary.normalized().unwrap();
        let mut datoms = database
            .datoms(boundary.order())
            .unwrap()
            .into_iter()
            .filter(|datom| {
                let comparison = normalized.compare_datom(datom);
                if reverse {
                    !comparison.is_gt()
                } else {
                    !comparison.is_lt()
                }
            })
            .collect::<Vec<_>>();
        if reverse {
            datoms.reverse();
        }
        datoms
    }

    fn assert_bidirectional_seek(database: &DatabaseValue, boundary: &IndexBoundary) {
        let forward = database
            .seek_cursor(boundary)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        let reverse = database
            .reverse_seek_cursor(boundary)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert_same_stored_datoms(&forward, &expected_seek(database, boundary, false));
        assert_same_stored_datoms(&reverse, &expected_seek(database, boundary, true));
        assert_same_stored_datoms(&database.collect_seek_datoms(boundary).unwrap(), &forward);
        assert_same_stored_datoms(
            &database.collect_reverse_seek_datoms(boundary).unwrap(),
            &reverse,
        );
    }

    #[test]
    fn eager_raw_seek_streams_both_directions_across_indexes_and_views() {
        let (report, _, first, second, ..) = overlay_fixture();
        let value = report.db_after.database_value();
        let boundaries = [
            IndexBoundary::Eavt(IndexComponents::Four(
                first,
                AMOUNT,
                decimal("1.0"),
                IndexTransaction::T(2),
            )),
            IndexBoundary::Aevt(IndexComponents::Three(AMOUNT, first, decimal("1.0"))),
            IndexBoundary::Avet(IndexComponents::Two(AMOUNT, decimal("1.0"))),
            IndexBoundary::Vaet(IndexComponents::Three(Value::Ref(second), LINK, first)),
        ];

        for database in [
            value.clone(),
            value.clone().history(),
            value.clone().as_of(2),
            value.clone().since(1).as_of(3),
        ] {
            for boundary in &boundaries {
                assert_bidirectional_seek(&database, boundary);
            }
        }

        for boundary in [
            IndexBoundary::Eavt(IndexComponents::Empty),
            IndexBoundary::Aevt(IndexComponents::Empty),
            IndexBoundary::Avet(IndexComponents::Empty),
            IndexBoundary::Vaet(IndexComponents::Empty),
        ] {
            assert_bidirectional_seek(&value, &boundary);
            assert_bidirectional_seek(&value.clone().history(), &boundary);
        }
    }

    #[test]
    fn reverse_filtered_seek_is_lazy_and_collapses_after_filtering() {
        use std::sync::atomic::{AtomicUsize, Ordering as AtomicOrdering};

        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                AMOUNT,
                Keyword::new("seek", "amount"),
                ValueType::BigDec,
                Cardinality::Many,
            ))
            .unwrap();
        let entity = make_eid(USER_PARTITION, 1).unwrap();
        let first = Database::new(schema)
            .unwrap()
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: decimal("1.0").into(),
                }],
                1_000,
            )
            .unwrap()
            .db_after;
        let second = first
            .with(
                &[TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: Some(decimal("1.0").into()),
                }],
                2_000,
            )
            .unwrap()
            .db_after;
        let latest = second
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: decimal("1.0").into(),
                }],
                3_000,
            )
            .unwrap()
            .db_after;
        let calls = Arc::new(AtomicUsize::new(0));
        let observed = Arc::clone(&calls);
        let excluded_t = latest.basis_t();
        let filtered = latest.database_value().filter(move |_, datom| {
            // Raw reverse seek continues into earlier entities/attributes.
            // Isolate this history and use its actual T: schema installation
            // already consumed the initial transaction in Database::new.
            if datom.entity != entity || datom.attribute != AMOUNT {
                return false;
            }
            observed.fetch_add(1, AtomicOrdering::Relaxed);
            tx_to_t(datom.tx).unwrap() != excluded_t
        });
        let boundary = IndexBoundary::Eavt(IndexComponents::Three(entity, AMOUNT, decimal("1.0")));

        let mut cursor = filtered.reverse_seek_cursor(&boundary).unwrap();
        assert_eq!(calls.load(AtomicOrdering::Relaxed), 0);
        assert!(
            cursor.next().is_none(),
            "after filtering the newest assertion, the visible retraction hides the older assertion"
        );
        assert_eq!(calls.load(AtomicOrdering::Relaxed), 3);

        let history_calls = Arc::new(AtomicUsize::new(0));
        let observed = Arc::clone(&history_calls);
        let history = latest.database_value().history().filter(move |_, _| {
            observed.fetch_add(1, AtomicOrdering::Relaxed);
            true
        });
        let mut cursor = history.reverse_seek_cursor(&boundary).unwrap();
        assert_eq!(history_calls.load(AtomicOrdering::Relaxed), 0);
        assert!(cursor.next().unwrap().is_ok());
        assert_eq!(history_calls.load(AtomicOrdering::Relaxed), 1);
        drop(cursor);
        assert_eq!(history_calls.load(AtomicOrdering::Relaxed), 1);
    }

    #[test]
    fn transaction_instant_memo_is_seeded_shared_and_initialized_once() {
        let database = Database::bootstrap().unwrap();
        let report = database.with(&[], 1_234).unwrap();
        let value = report.db_after.database_value();
        assert_eq!(
            value.last_tx_instant_memo.value.get(),
            Some(&Some(1_234)),
            "eager values seed their resident coordinate"
        );

        let derived = value
            .clone()
            .as_of(0)
            .history()
            .with_read_observer(Arc::new(LogicalReadObserver::new(u64::MAX, u64::MAX)));
        assert!(Arc::ptr_eq(
            &value.last_tx_instant_memo,
            &derived.last_tx_instant_memo
        ));
        assert_eq!(derived.last_tx_instant().unwrap(), Some(1_234));

        let lazy = Arc::new(LastTxInstantMemo::empty());
        let lazy_clone = Arc::clone(&lazy);
        let initializations = Cell::new(0_u32);
        assert_eq!(
            lazy.get_or_try_init(|| {
                initializations.set(initializations.get() + 1);
                Ok(Some(9_876))
            })
            .unwrap(),
            Some(9_876)
        );
        assert_eq!(
            lazy_clone
                .get_or_try_init(|| {
                    initializations.set(initializations.get() + 1);
                    Ok(Some(1))
                })
                .unwrap(),
            Some(9_876)
        );
        assert_eq!(initializations.get(), 1);
    }

    fn assert_prefix_matches_eager(
        overlay: &DatabaseValue,
        eager: &DatabaseValue,
        prefix: &IndexPrefix,
    ) {
        let overlay_current = overlay.datoms_with_prefix(prefix).unwrap();
        let eager_current = eager.datoms_with_prefix(prefix).unwrap();
        assert_same_stored_datoms(&overlay_current, &eager_current);

        let cursor_current = overlay
            .current_prefix_cursor(prefix)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert_same_stored_datoms(&cursor_current, &eager_current);

        let overlay_history = overlay
            .clone()
            .history()
            .datoms_with_prefix(prefix)
            .unwrap();
        let eager_history = eager.clone().history().datoms_with_prefix(prefix).unwrap();
        assert_same_stored_datoms(&overlay_history, &eager_history);
    }

    fn overlay_source_prefix(overlay: &DatabaseValue, prefix: &IndexPrefix) -> IndexPrefix {
        match &overlay.basis {
            ReadBasis::TransactionOverlay(overlay) => overlay.source_prefix(prefix),
            _ => panic!("expected a transaction overlay"),
        }
    }

    fn overlay_fixture() -> (TxReport, DatabaseValue, u64, u64, Keyword, Keyword, Keyword) {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                AMOUNT,
                Keyword::new("measurement", "amount"),
                ValueType::BigDec,
                Cardinality::Many,
            ))
            .unwrap();
        schema
            .install(Attribute::new(
                LINK,
                Keyword::new("measurement", "link"),
                ValueType::Ref,
                Cardinality::Many,
            ))
            .unwrap();

        let old = Keyword::new("overlay", "old");
        let new = Keyword::new("overlay", "new");
        let spare = Keyword::new("overlay", "spare");
        let database = Database::new(schema).unwrap();
        let seeded = database
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("first".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(old.clone()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("first".into()),
                        attribute: AMOUNT,
                        value: decimal("1.0").into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("first".into()),
                        attribute: AMOUNT,
                        value: decimal("1.00").into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("first".into()),
                        attribute: LINK,
                        value: TxValue::Entity(EntityRef::Temp("second".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("second".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(spare.clone()).into(),
                    },
                ],
                2_000,
            )
            .unwrap();
        let first = seeded.tempids["first"];
        let second = seeded.tempids["second"];
        let renamed = seeded
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(first),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(new.clone()).into(),
                }],
                3_000,
            )
            .unwrap();

        let mut indexed = renamed.db_after.schema().attribute(AMOUNT).unwrap().clone();
        indexed.indexed = true;
        let report = renamed
            .db_after
            .with(
                &[
                    TxOp::AlterAttribute(indexed),
                    TxOp::Retract {
                        entity: EntityRef::Id(first),
                        attribute: AMOUNT,
                        value: Some(decimal("1.0").into()),
                    },
                    TxOp::Add {
                        entity: EntityRef::Id(second),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(old.clone()).into(),
                    },
                ],
                4_000,
            )
            .unwrap();
        let overlay = overlay_for(&report, 4_000);
        (report, overlay, first, second, old, new, spare)
    }

    #[test]
    fn point_current_metadata_and_prefix_cursor_match_the_eager_oracle() {
        let database = Database::bootstrap().unwrap();
        let genesis = database.database_value();
        assert_eq!(genesis.eidx_frontier(), database.eidx_frontier());
        assert_eq!(genesis.last_tx_instant().unwrap(), None);

        let entity = make_eid(USER_PARTITION, 42).unwrap();
        let report = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(Keyword::new("cursor", "entity"))),
                }],
                1_234,
            )
            .unwrap();
        let value = report.db_after.database_value();
        assert_eq!(value.eidx_frontier(), report.db_after.eidx_frontier());
        assert_eq!(value.last_tx_instant().unwrap(), Some(1_234));

        let prefix = IndexPrefix::Eavt {
            entity,
            attribute: None,
            value: None,
        };
        let lazy = value
            .current_prefix_cursor(&prefix)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert_eq!(lazy, value.datoms_with_prefix(&prefix).unwrap());
        assert!(lazy.iter().all(|datom| datom.entity == entity));
    }

    #[test]
    fn transaction_prefix_memo_admits_only_complete_results_and_replays_with_charge() {
        let database = Database::bootstrap().unwrap();
        let entity = make_eid(USER_PARTITION, 42).unwrap();
        let report = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(Keyword::new("memo", "entity")).into(),
                }],
                1_234,
            )
            .unwrap();
        let context = Arc::new(TransactionReadContext::new(8, u64::MAX));
        let value = report
            .db_after
            .database_value()
            .with_transaction_read_context(Arc::clone(&context));
        let prefix = IndexPrefix::Eavt {
            entity,
            attribute: Some(DB_IDENT as u32),
            value: None,
        };

        let mut partial = value.current_prefix_cursor(&prefix).unwrap();
        assert!(partial.next().transpose().unwrap().is_some());
        drop(partial);
        let partial_work = context.snapshot().unwrap();
        assert_eq!(partial_work.memo_admissions, 0);
        assert_eq!(partial_work.memo_rejections, 1);
        assert_eq!(partial_work.prefix_misses, 1);

        let mut complete_cursor = value.current_prefix_cursor(&prefix).unwrap();
        let complete = complete_cursor
            .by_ref()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert!(
            complete_cursor
                .memo_source
                .as_ref()
                .is_some_and(|source| source.completed && source.key.is_none()),
            "successful admission must move the owned key into the memo"
        );
        assert_eq!(complete.len(), 1);
        let admitted = context.snapshot().unwrap();
        assert_eq!(admitted.memo_admissions, 1);
        assert_eq!(admitted.source_datoms, 2);

        let replayed = value
            .current_prefix_cursor(&prefix)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert_eq!(replayed, complete);
        let replay_work = context.snapshot().unwrap();
        assert_eq!(replay_work.prefix_hits, 1);
        assert_eq!(replay_work.source_datoms, admitted.source_datoms);
        assert_eq!(replay_work.logical_datoms, admitted.logical_datoms + 1);

        // History is a separate view coordinate over the same immutable
        // value identity, so independently derived history values can share.
        let history = value.clone().history();
        let first_history = history.datoms_with_prefix(&prefix).unwrap();
        let source_after_history = context.snapshot().unwrap().source_datoms;
        assert_eq!(
            value.clone().history().datoms_with_prefix(&prefix).unwrap(),
            first_history
        );
        assert_eq!(
            context.snapshot().unwrap().source_datoms,
            source_after_history
        );

        assert!(
            value
                .clone()
                .without_transaction_read_context()
                .transaction_read_context()
                .is_none()
        );
    }

    #[test]
    fn empty_prefix_memo_entries_obey_the_entry_bound() {
        let context = Arc::new(TransactionReadContext::new(2, u64::MAX));
        let value = Database::bootstrap()
            .unwrap()
            .database_value()
            .with_transaction_read_context(Arc::clone(&context));
        for entity in 10_000..10_003 {
            assert!(
                value
                    .datoms_with_prefix(&IndexPrefix::Eavt {
                        entity,
                        attribute: None,
                        value: None,
                    })
                    .unwrap()
                    .is_empty()
            );
        }
        let work = context.snapshot().unwrap();
        assert_eq!(work.memo_admissions, 2);
        assert_eq!(work.memo_rejections, 1);
        assert_eq!(work.memo_peak_entries, 2);
        assert_eq!(
            work.memo_peak_retained_bytes,
            2 * std::mem::size_of::<PrefixMemoKey>() as u64
        );
    }

    #[test]
    fn oversized_empty_prefix_key_is_not_retained_by_the_memo() {
        let context = Arc::new(TransactionReadContext::new(8, 1_024));
        let value = Database::bootstrap()
            .unwrap()
            .database_value()
            .with_transaction_read_context(Arc::clone(&context));
        let prefix = IndexPrefix::Eavt {
            entity: 10_000,
            attribute: Some(DB_IDENT as u32),
            value: Some(Value::String("x".repeat(4_096))),
        };

        for _ in 0..2 {
            assert!(value.datoms_with_prefix(&prefix).unwrap().is_empty());
        }

        let work = context.snapshot().unwrap();
        assert_eq!(work.prefix_misses, 2);
        assert_eq!(work.prefix_hits, 0);
        assert_eq!(work.memo_admissions, 0);
        assert_eq!(work.memo_rejections, 2);
        assert_eq!(work.memo_peak_entries, 0);
        assert_eq!(work.memo_peak_retained_bytes, 0);
    }

    #[test]
    fn capacity_failed_prefix_is_never_admitted() {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                AMOUNT,
                Keyword::new("memo", "amount"),
                ValueType::BigDec,
                Cardinality::Many,
            ))
            .unwrap();
        let entity = make_eid(USER_PARTITION, 1).unwrap();
        let database = Database::new(schema)
            .unwrap()
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Id(entity),
                        attribute: AMOUNT,
                        value: decimal("1.0").into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Id(entity),
                        attribute: AMOUNT,
                        value: decimal("2.0").into(),
                    },
                ],
                1_000,
            )
            .unwrap()
            .db_after;
        let context = Arc::new(TransactionReadContext::new(1, u64::MAX));
        let value = database
            .database_value()
            .with_transaction_read_context(Arc::clone(&context));
        let error = value
            .datoms_with_prefix(&IndexPrefix::Eavt {
                entity,
                attribute: Some(AMOUNT),
                value: None,
            })
            .unwrap_err();
        assert_eq!(error.code, "transaction/read-capacity");
        let work = context.snapshot().unwrap();
        assert_eq!(work.logical_datoms, 1);
        assert_eq!(work.memo_admissions, 0);
        assert_eq!(work.memo_rejections, 1);
    }

    #[test]
    fn temporal_prefix_stream_charges_only_yields_and_does_not_memoize_raw_history() {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                AMOUNT,
                Keyword::new("window", "amount"),
                ValueType::BigDec,
                Cardinality::Many,
            ))
            .unwrap();
        let entity = make_eid(USER_PARTITION, 1).unwrap();
        let first = Database::new(schema)
            .unwrap()
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: decimal("2.0").into(),
                }],
                1_000,
            )
            .unwrap();
        let retracted = first
            .db_after
            .with(
                &[TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: Some(decimal("2.0").into()),
                }],
                2_000,
            )
            .unwrap();
        let latest = retracted
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: decimal("2.0").into(),
                }],
                3_000,
            )
            .unwrap()
            .db_after;
        let context = Arc::new(TransactionReadContext::new(1, u64::MAX));
        let value = latest
            .database_value()
            .with_transaction_read_context(Arc::clone(&context))
            .filter(|_, datom| tx_to_t(datom.tx).unwrap() != 3);
        let prefix = IndexPrefix::Eavt {
            entity,
            attribute: Some(AMOUNT),
            value: Some(decimal("2.0")),
        };

        let mut cursor = value.prefix_cursor(&prefix).unwrap();
        assert_eq!(context.snapshot().unwrap().logical_datoms, 0);
        assert!(cursor.next().unwrap().is_ok());
        let after_one = context.snapshot().unwrap();
        assert_eq!(after_one.logical_datoms, 1);
        assert_eq!(after_one.memo_admissions, 0);
        assert_eq!(after_one.memo_rejections, 0);
        let error = cursor.next().unwrap().unwrap_err();
        assert_eq!(error.code, "transaction/read-capacity");
        assert_eq!(context.snapshot().unwrap().logical_datoms, 1);
    }

    #[test]
    fn prefix_cursor_rejects_non_current_database_values() {
        let value = Database::bootstrap().unwrap().database_value().history();
        assert_eq!(
            value
                .current_prefix_cursor(&IndexPrefix::Aevt {
                    attribute: DB_IDENT as u32,
                    entity: None,
                    value: None,
                })
                .err()
                .expect("history values must reject the point-current cursor")
                .code,
            "database/prefix-cursor-requires-current"
        );
    }

    #[test]
    fn observed_overlay_prefix_stops_at_the_logical_read_cap() {
        const TAG: u32 = 1_000;
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                TAG,
                Keyword::new("read-cap", "tag"),
                ValueType::String,
                Cardinality::Many,
            ))
            .unwrap();
        let database = Database::new(schema).unwrap();
        let entity = make_eid(USER_PARTITION, 1).unwrap();
        let ops = (0..256)
            .map(|value| TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: TAG,
                value: Value::String(format!("tag-{value:03}")).into(),
            })
            .collect::<Vec<_>>();
        let seeded = database.with(&ops, 1_000).unwrap().db_after;
        let proposed = seeded.with(&[], 2_000).unwrap();
        let observer = Arc::new(LogicalReadObserver::new(2, u64::MAX));
        let overlay = overlay_for(&proposed, 2_000).with_read_observer(Arc::clone(&observer));

        let error = overlay
            .datoms_with_prefix(&IndexPrefix::Eavt {
                entity,
                attribute: Some(TAG),
                value: None,
            })
            .unwrap_err();
        assert_eq!(
            (error.category, error.code),
            (ErrorCategory::Busy, "transaction/read-capacity")
        );
        assert_eq!(
            observer.snapshot().unwrap().datoms,
            2,
            "the cursor must charge successful yields one at a time; a late whole-prefix charge leaves this at zero"
        );
    }

    #[test]
    fn transaction_overlay_matches_eager_successor_without_materializing_the_base() {
        let (report, overlay, first, second, old, new, spare) = overlay_fixture();
        let eager = report.db_after.database_value();

        assert_eq!(overlay.basis_t(), report.db_after.basis_t());
        assert_eq!(overlay.eidx_frontier(), report.db_after.eidx_frontier());
        assert_eq!(
            overlay.last_tx_instant_memo.value.get(),
            Some(&Some(4_000)),
            "transaction overlays seed their successor coordinate"
        );
        assert_eq!(overlay.last_tx_instant().unwrap(), Some(4_000));
        assert_eq!(overlay.schema(), report.db_after.schema());

        // Ident lookup is an assertion-history cache, not a projection of
        // current :db/ident facts. The transaction-local assertion wins over
        // the base alias without cloning the complete base IdentIndex.
        assert_eq!(report.db_before.entid(&old), Some(first));
        assert_eq!(overlay.entid(&old), Some(second));
        assert_eq!(overlay.entid(&new), Some(first));
        assert_eq!(overlay.entid(&spare), Some(second));
        assert_eq!(overlay.ident(first), Some(&new));
        assert_eq!(overlay.ident(second), Some(&old));
        assert_eq!(overlay.entid(&old), eager.entid(&old));
        assert_eq!(overlay.ident(first), eager.ident(first));
        assert_eq!(overlay.ident(second), eager.ident(second));

        // Enabling :db/index makes pre-existing facts visible in successor
        // AVET. The exact 1.0M retraction must not hide equal-magnitude 1.00M.
        let amount_eavt = IndexPrefix::Eavt {
            entity: first,
            attribute: Some(AMOUNT),
            value: None,
        };
        let amount_aevt = IndexPrefix::Aevt {
            attribute: AMOUNT,
            entity: Some(first),
            value: None,
        };
        let amount_avet = IndexPrefix::Avet {
            attribute: AMOUNT,
            value: Some(decimal("1.0")),
            entity: None,
        };
        let link_vaet = IndexPrefix::Vaet {
            value: Value::Ref(second),
            attribute: Some(LINK),
            entity: None,
        };
        for prefix in [&amount_eavt, &amount_aevt, &amount_avet, &link_vaet] {
            assert_prefix_matches_eager(&overlay, &eager, prefix);
        }

        let amounts = overlay.datoms_with_prefix(&amount_eavt).unwrap();
        assert_eq!(amounts.len(), 1);
        assert!(amounts[0].value.stored_eq(&decimal("1.00")));
        assert!(!amounts[0].value.stored_eq(&decimal("1.0")));

        // Disabling :db/index suppresses both current and historical AVET
        // membership even though AEVT continues to retain the underlying data.
        let mut unindexed = report.db_after.schema().attribute(AMOUNT).unwrap().clone();
        unindexed.indexed = false;
        let unindexed_report = report
            .db_after
            .with(&[TxOp::AlterAttribute(unindexed)], 5_000)
            .unwrap();
        let unindexed_overlay = overlay_for(&unindexed_report, 5_000);
        let unindexed_eager = unindexed_report.db_after.database_value();
        assert_prefix_matches_eager(&unindexed_overlay, &unindexed_eager, &amount_avet);

        // This is the second material alteration of AMOUNT. Both overlay read
        // shapes must replace the first hook's current transaction coordinate
        // while retaining both immutable events in history.
        let alter_hook = IndexPrefix::Eavt {
            entity: crate::DB_PART_DB,
            attribute: Some(crate::DB_ALTER_ATTRIBUTE as u32),
            value: Some(Value::Ref(u64::from(AMOUNT))),
        };
        let current_hook = unindexed_overlay.datoms_with_prefix(&alter_hook).unwrap();
        assert_eq!(current_hook.len(), 1);
        assert_eq!(
            current_hook[0].tx,
            t_to_tx(unindexed_report.db_after.basis_t()).unwrap()
        );
        let scanned_hook = unindexed_overlay
            .datoms(IndexOrder::Eavt)
            .unwrap()
            .into_iter()
            .filter(|datom| {
                datom.entity == crate::DB_PART_DB
                    && datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32
                    && datom.value == Value::Ref(u64::from(AMOUNT))
            })
            .collect::<Vec<_>>();
        assert_same_stored_datoms(&scanned_hook, &current_hook);
        assert_same_stored_datoms(
            &current_hook,
            &unindexed_eager.datoms_with_prefix(&alter_hook).unwrap(),
        );
        assert_eq!(
            unindexed_overlay
                .clone()
                .history()
                .datoms_with_prefix(&alter_hook)
                .unwrap()
                .len(),
            2
        );
        assert!(
            unindexed_overlay
                .datoms_with_prefix(&amount_avet)
                .unwrap()
                .is_empty()
        );
        assert!(
            unindexed_overlay
                .history()
                .datoms_with_prefix(&amount_avet)
                .unwrap()
                .is_empty()
        );
    }

    #[test]
    fn transaction_overlay_only_backfills_aevt_when_avet_is_enabled() {
        let (enabled_report, enabled, first, ..) = overlay_fixture();
        let prefix = IndexPrefix::Avet {
            attribute: AMOUNT,
            value: Some(decimal("1.0")),
            entity: None,
        };
        assert_eq!(
            overlay_source_prefix(&enabled, &prefix),
            IndexPrefix::Aevt {
                attribute: AMOUNT,
                entity: None,
                value: None,
            }
        );

        let steady_report = enabled_report
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(first),
                    attribute: AMOUNT,
                    value: decimal("2.0").into(),
                }],
                4_500,
            )
            .unwrap();
        let steady = overlay_for(&steady_report, 4_500);
        assert_eq!(overlay_source_prefix(&steady, &prefix), prefix);

        let mut unindexed = enabled_report
            .db_after
            .schema()
            .attribute(AMOUNT)
            .unwrap()
            .clone();
        unindexed.indexed = false;
        let disabled_report = enabled_report
            .db_after
            .with(&[TxOp::AlterAttribute(unindexed)], 5_000)
            .unwrap();
        let disabled = overlay_for(&disabled_report, 5_000);
        assert_eq!(overlay_source_prefix(&disabled, &prefix), prefix);
    }

    #[test]
    fn transaction_overlay_streams_unbounded_reads_and_rejects_invalid_construction() {
        let (report, overlay, ..) = overlay_fixture();
        let eager = report.db_after.database_value();
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_same_stored_datoms(
                &overlay.datoms(order).unwrap(),
                &eager.datoms(order).unwrap(),
            );
            assert_same_stored_datoms(
                &overlay.clone().history().datoms(order).unwrap(),
                &eager.clone().history().datoms(order).unwrap(),
            );
        }

        // A fully unbound data pattern is the observable path that exposed
        // the former overlay-only limitation: eager and native values could
        // scan it, while an entity predicate's exact db-after could not.
        let entity = Variable::new("e").unwrap();
        let attribute = Variable::new("a").unwrap();
        let value = Variable::new("v").unwrap();
        let query = Query::new(
            FindSpec::Relation(vec![
                FindElement::Variable(entity.clone()),
                FindElement::Variable(attribute.clone()),
                FindElement::Variable(value.clone()),
            ]),
            vec![Clause::Pattern(Box::new(DataPattern::new(
                Term::Variable(entity),
                Term::Variable(attribute),
                Term::Variable(value),
            )))],
        );
        assert_eq!(
            overlay
                .query(&query, &[], &QueryControl::default())
                .unwrap()
                .result,
            eager
                .query(&query, &[], &QueryControl::default())
                .unwrap()
                .result,
        );

        let chained = DatabaseValue::transaction_overlay(
            overlay.clone(),
            Arc::default(),
            Arc::new(overlay.schema().clone()),
            overlay.basis_t() + 1,
            overlay.eidx_frontier(),
            5_000,
        )
        .unwrap();
        assert_eq!(chained.basis_t(), overlay.basis_t() + 1);

        let mut reversed = report.tx_data.clone();
        reversed.reverse();
        let noncanonical = DatabaseValue::transaction_overlay(
            report.db_before.database_value(),
            Arc::from(reversed),
            Arc::new(report.db_after.schema().clone()),
            report.db_after.basis_t(),
            report.db_after.eidx_frontier(),
            4_000,
        )
        .unwrap_err();
        assert_eq!(noncanonical.code, "database/overlay-noncanonical-datoms");
    }

    #[test]
    fn raw_boundaries_resolve_idents_and_lookup_refs_in_every_index_order() {
        let (value, alice, bob, alice_ident, bob_ident) = raw_boundary_fixture();
        let alice_lookup = EntityIdentifier::Lookup {
            attribute: AttributeName::Ident(Keyword::new("boundary", "email")),
            value: Value::String("alice@example.test".into()),
        };
        let bob_lookup = EntityIdentifier::Lookup {
            attribute: AttributeName::Id(BOUNDARY_EMAIL),
            value: Value::String("bob@example.test".into()),
        };
        let link = AttributeName::Ident(Keyword::new("boundary", "link"));
        let t = value.basis_t();
        let tx = t_to_tx(t).unwrap();

        assert_eq!(
            value
                .eavt_boundary(IndexComponents::Four(
                    alice_lookup.clone(),
                    link.clone(),
                    RawIndexValue::Entity(EntityIdentifier::Ident(bob_ident.clone())),
                    IndexTransaction::T(t),
                ))
                .unwrap(),
            IndexBoundary::Eavt(IndexComponents::Four(
                alice,
                BOUNDARY_LINK,
                Value::Ref(bob),
                IndexTransaction::T(t),
            ))
        );
        assert_eq!(
            value
                .aevt_boundary(IndexComponents::Four(
                    link.clone(),
                    EntityIdentifier::Ident(alice_ident.clone()),
                    RawIndexValue::Entity(bob_lookup.clone()),
                    IndexTransaction::Tx(tx),
                ))
                .unwrap(),
            IndexBoundary::Aevt(IndexComponents::Four(
                BOUNDARY_LINK,
                alice,
                Value::Ref(bob),
                IndexTransaction::Tx(tx),
            ))
        );
        assert_eq!(
            value
                .avet_boundary(IndexComponents::Four(
                    link.clone(),
                    RawIndexValue::Entity(EntityIdentifier::Ident(bob_ident)),
                    alice_lookup,
                    IndexTransaction::T(t),
                ))
                .unwrap(),
            IndexBoundary::Avet(IndexComponents::Four(
                BOUNDARY_LINK,
                Value::Ref(bob),
                alice,
                IndexTransaction::T(t),
            ))
        );
        assert_eq!(
            value
                .vaet_boundary(IndexComponents::Four(
                    RawIndexValue::Entity(bob_lookup),
                    link,
                    EntityIdentifier::Ident(alice_ident),
                    IndexTransaction::Tx(tx),
                ))
                .unwrap(),
            IndexBoundary::Vaet(IndexComponents::Four(
                Value::Ref(bob),
                BOUNDARY_LINK,
                alice,
                IndexTransaction::Tx(tx),
            ))
        );

        assert_eq!(
            value
                .avet_boundary(IndexComponents::Two(
                    AttributeName::Ident(Keyword::new("boundary", "email")),
                    RawIndexValue::Stored(Value::String("alice@example.test".into())),
                ))
                .unwrap(),
            IndexBoundary::Avet(IndexComponents::Two(
                BOUNDARY_EMAIL,
                Value::String("alice@example.test".into()),
            ))
        );
    }

    #[test]
    fn raw_tuple_boundaries_resolve_only_ref_slots_and_preserve_nil() {
        let (value, alice, bob, alice_ident, bob_ident) = raw_boundary_fixture();
        let bob_lookup = EntityIdentifier::Lookup {
            attribute: AttributeName::Id(BOUNDARY_EMAIL),
            value: Value::String("bob@example.test".into()),
        };

        assert_eq!(
            value
                .avet_boundary(IndexComponents::Two(
                    AttributeName::Ident(Keyword::new("boundary", "homogeneous-refs")),
                    RawIndexValue::Tuple(vec![
                        Some(RawIndexValue::Entity(EntityIdentifier::Ident(
                            alice_ident.clone(),
                        ))),
                        None,
                        Some(RawIndexValue::Entity(bob_lookup.clone())),
                    ]),
                ))
                .unwrap(),
            IndexBoundary::Avet(IndexComponents::Two(
                BOUNDARY_HOMOGENEOUS_REFS,
                Value::Tuple(vec![Some(Value::Ref(alice)), None, Some(Value::Ref(bob))]),
            ))
        );

        assert_eq!(
            value
                .eavt_boundary(IndexComponents::Three(
                    EntityIdentifier::Id(alice),
                    AttributeName::Id(BOUNDARY_HETEROGENEOUS),
                    RawIndexValue::Tuple(vec![
                        Some(RawIndexValue::Entity(EntityIdentifier::Ident(alice_ident))),
                        Some(RawIndexValue::Stored(Value::String("middle".into()))),
                        Some(RawIndexValue::Entity(EntityIdentifier::Ident(bob_ident))),
                    ]),
                ))
                .unwrap(),
            IndexBoundary::Eavt(IndexComponents::Three(
                alice,
                BOUNDARY_HETEROGENEOUS,
                Value::Tuple(vec![
                    Some(Value::Ref(alice)),
                    Some(Value::String("middle".into())),
                    Some(Value::Ref(bob)),
                ]),
            ))
        );
    }

    #[test]
    fn raw_boundaries_reject_unresolved_wrong_typed_and_unqualified_inputs() {
        let (value, alice, _, _, bob_ident) = raw_boundary_fixture();
        let missing = EntityIdentifier::Ident(Keyword::new("boundary", "missing"));

        assert_eq!(
            value
                .eavt_boundary(IndexComponents::One(missing.clone()))
                .unwrap_err()
                .code,
            "index/unresolved-entity"
        );
        assert_eq!(
            value
                .eavt_boundary(IndexComponents::Three(
                    EntityIdentifier::Id(alice),
                    AttributeName::Id(BOUNDARY_TEXT),
                    RawIndexValue::Entity(EntityIdentifier::Ident(bob_ident)),
                ))
                .unwrap_err()
                .code,
            "transaction/value-type"
        );
        assert_eq!(
            value
                .avet_boundary(IndexComponents::Two(
                    AttributeName::Id(BOUNDARY_HETEROGENEOUS),
                    RawIndexValue::Tuple(vec![
                        Some(RawIndexValue::Entity(EntityIdentifier::Id(alice))),
                        None,
                        None,
                        None,
                    ]),
                ))
                .unwrap_err()
                .code,
            "transaction/invalid-tuple-length"
        );
        assert_eq!(
            value
                .avet_boundary(IndexComponents::Two(
                    AttributeName::Id(BOUNDARY_HETEROGENEOUS),
                    RawIndexValue::Tuple(vec![
                        Some(RawIndexValue::Stored(Value::String("not-a-ref".into()))),
                        None,
                        Some(RawIndexValue::Entity(EntityIdentifier::Id(alice))),
                    ]),
                ))
                .unwrap_err()
                .code,
            "transaction/invalid-tuple-element"
        );
        assert_eq!(
            value
                .avet_boundary(IndexComponents::Two(
                    AttributeName::Id(BOUNDARY_HOMOGENEOUS_REFS),
                    RawIndexValue::Tuple(vec![
                        Some(RawIndexValue::Entity(missing)),
                        Some(RawIndexValue::Entity(EntityIdentifier::Id(alice))),
                    ]),
                ))
                .unwrap_err()
                .code,
            "index/unresolved-entity"
        );
        assert_eq!(
            value
                .vaet_boundary(IndexComponents::One(RawIndexValue::Stored(Value::String(
                    "not-a-ref".into()
                ),)))
                .unwrap_err()
                .code,
            "index/vaet-value-not-ref"
        );
        assert_eq!(
            value
                .avet_boundary(IndexComponents::One(AttributeName::Id(BOUNDARY_TEXT)))
                .unwrap_err()
                .code,
            "index/attribute-not-in-avet"
        );
    }

    #[test]
    fn raw_avet_boundary_can_use_a_speculative_attribute_backfill() {
        let (_, overlay, ..) = overlay_fixture();
        let boundary = overlay
            .avet_boundary(IndexComponents::One(AttributeName::Id(AMOUNT)))
            .unwrap();
        assert!(overlay.seek_cursor(&boundary).unwrap().next().is_some());
    }
}
