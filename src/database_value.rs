use crate::identity::validate_frontier;
use crate::index::compare_prefix;
use crate::peer::TieredSnapshot;
use crate::{
    AttributeName, DB_IDENT, Database, Datom, EntityIdentifier, ErrorCategory, IndexOrder,
    IndexPrefix, Keyword, PeerIndexCursor, PeerSnapshot, Schema, SemanticError, Value, eid_to_eidx,
    schema_eid_to_attr_id, tx_to_t,
};
use std::fmt;
use std::iter::Cloned;
use std::slice::Iter;
use std::sync::{Arc, Mutex, OnceLock};
use std::vec::IntoIter;

type ReadFilter = dyn Fn(&DatabaseValue, &Datom) -> bool + Send + Sync;

/// Exact logical database work delivered through transaction read APIs.
///
/// This deliberately does not infer work from persistent-tree node loads:
/// cache hits still deliver datoms, while one loaded leaf can contain many
/// datoms outside the requested range. The writer carries one observer across
/// generation, assessment, validation, and successor construction; the
/// assessor's prefix memo prevents repeated database deliveries.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct LogicalReadWork {
    pub(crate) datoms: u64,
    pub(crate) retained_bytes: u64,
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

    fn charge_datoms(&self, datoms: &[Datom]) -> Result<(), SemanticError> {
        let mut work = LogicalReadWork::default();
        for datom in datoms {
            work.datoms = work.datoms.checked_add(1).ok_or_else(read_work_overflow)?;
            work.retained_bytes = work
                .retained_bytes
                .checked_add(datom.retained_bytes())
                .ok_or_else(read_work_overflow)?;
        }
        self.charge(work)
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
/// The two basis representations are deliberate rather than an extensible
/// storage abstraction: an eager semantic-kernel value, or a native
/// PostgreSQL peer snapshot backed by its persistent tree and recent tier.
/// Temporal and custom filters belong to this value, so every consumer sees
/// the same information instead of receiving a detached `View` hint.
#[derive(Clone)]
pub struct DatabaseValue {
    basis: ReadBasis,
    last_tx_instant_memo: Arc<LastTxInstantMemo>,
    as_of_t: Option<u64>,
    since_t: Option<u64>,
    history: bool,
    filters: Arc<[Arc<ReadFilter>]>,
    read_observer: Option<Arc<LogicalReadObserver>>,
}

#[derive(Clone)]
enum ReadBasis {
    Eager(Arc<Database>),
    Native(TieredSnapshot),
    TransactionOverlay(Arc<TransactionOverlay>),
}

/// One immutable assessment-local db-after over an exact db-before.
///
/// The overlay owns only canonical transaction datoms, transaction-local
/// ident assertions, and resident successor metadata. Prefix reads merge that
/// bounded delta with the wrapped value by exact stored E/A/V identity; no
/// complete durable index is retained or materialized.
#[derive(Clone)]
struct TransactionOverlay {
    base: DatabaseValue,
    tx_data: Arc<[Datom]>,
    ident_assertions: Arc<[(Keyword, u64)]>,
    schema: Arc<Schema>,
    basis_t: u64,
    eidx_frontier: u64,
    last_tx_instant: i64,
}

/// A scan of one exact immutable basis. Native scans retain the lazy
/// persistent-tree/recent-tier merge; an assessment overlay adds only its
/// bounded transaction delta to that stream. This is crate-private until the
/// complete public raw-index cursor contract (including reverse seeks) lands.
pub(crate) struct DatabaseValueScanCursor<'a> {
    inner: Box<dyn Iterator<Item = Result<Datom, SemanticError>> + 'a>,
    observer: Option<Arc<LogicalReadObserver>>,
    failed: bool,
}

impl Iterator for DatabaseValueScanCursor<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        if self.failed {
            return None;
        }
        match self.inner.next()? {
            Ok(datom) => {
                if let Some(observer) = &self.observer
                    && let Err(error) = observer.charge_datom(&datom)
                {
                    self.failed = true;
                    return Some(Err(error));
                }
                Some(Ok(datom))
            }
            Err(error) => {
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}

struct TransactionOverlayScanCursor<'a> {
    base: DatabaseValueScanCursor<'a>,
    delta: IntoIter<Datom>,
    removals: Arc<[Datom]>,
    schema: Arc<Schema>,
    history: bool,
    order: IndexOrder,
    base_next: Option<Datom>,
    delta_next: Option<Datom>,
    failed: bool,
}

/// Lazy current-index cursor over one exact point-in-time database value.
///
/// Eager values borrow their immutable index slice. Native values own a
/// root-pinned peer cursor which lower-bound seeks both the durable tree and
/// its recent tier. Every yielded item is owned so callers cannot retain a
/// cache or tree-node borrow across cursor advancement.
pub struct DatabaseValuePrefixCursor<'a> {
    inner: DatabaseValuePrefixCursorInner<'a>,
    observer: Option<Arc<LogicalReadObserver>>,
    failed: bool,
}

enum DatabaseValuePrefixCursorInner<'a> {
    Eager(Cloned<Iter<'a, Datom>>),
    Native(Box<PeerIndexCursor>),
    Owned(IntoIter<Datom>),
}

impl Iterator for DatabaseValuePrefixCursor<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        if self.failed {
            return None;
        }
        let item = match &mut self.inner {
            DatabaseValuePrefixCursorInner::Eager(cursor) => cursor.next().map(Ok),
            DatabaseValuePrefixCursorInner::Native(cursor) => cursor.next(),
            DatabaseValuePrefixCursorInner::Owned(cursor) => cursor.next().map(Ok),
        }?;
        match item {
            Ok(datom) => {
                if let Some(observer) = &self.observer
                    && let Err(error) = observer.charge_datom(&datom)
                {
                    self.failed = true;
                    return Some(Err(error));
                }
                Some(Ok(datom))
            }
            Err(error) => {
                self.failed = true;
                Some(Err(error))
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
                    ReadBasis::Native(_) => "native",
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
    pub fn eager(database: Arc<Database>) -> Self {
        let last_tx_instant_memo = Arc::new(LastTxInstantMemo::seeded(database.last_tx_instant()));
        Self {
            basis: ReadBasis::Eager(database),
            last_tx_instant_memo,
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
            read_observer: None,
        }
    }

    pub fn native(snapshot: PeerSnapshot) -> Self {
        Self::tiered(snapshot.tiered_snapshot())
    }

    pub(crate) fn tiered(snapshot: TieredSnapshot) -> Self {
        Self {
            basis: ReadBasis::Native(snapshot),
            last_tx_instant_memo: Arc::new(LastTxInstantMemo::empty()),
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
            read_observer: None,
        }
    }

    #[cfg(test)]
    pub(crate) fn shares_tiered_read_core(&self, other: &Self) -> bool {
        match (&self.basis, &other.basis) {
            (ReadBasis::Native(left), ReadBasis::Native(right)) => left.shares_read_core(right),
            _ => false,
        }
    }

    /// Build the ephemeral exact db-after used while validating one assessed
    /// transaction. A committed successor must install a new tiered value;
    /// overlays are deliberately not chainable across commits.
    pub(crate) fn transaction_overlay(
        mut base: DatabaseValue,
        tx_data: Arc<[Datom]>,
        schema: Arc<Schema>,
        basis_t: u64,
        eidx_frontier: u64,
        last_tx_instant: i64,
    ) -> Result<Self, SemanticError> {
        if !base.direct_current() {
            return Err(SemanticError::incorrect(
                "database/overlay-requires-current",
                "a transaction overlay requires an unfiltered point-current db-before",
            ));
        }
        if matches!(&base.basis, ReadBasis::TransactionOverlay(_)) {
            return Err(SemanticError::incorrect(
                "database/overlay-cannot-chain",
                "a committed tiered successor must replace an assessment overlay",
            ));
        }
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

        // The overlay is the logical read boundary. Keeping an observer on
        // both it and its wrapped base would charge every base datom twice.
        base.read_observer = None;
        Ok(Self {
            basis: ReadBasis::TransactionOverlay(Arc::new(TransactionOverlay {
                base,
                tx_data,
                ident_assertions: ident_assertions.into(),
                schema,
                basis_t,
                eidx_frontier,
                last_tx_instant,
            })),
            last_tx_instant_memo: Arc::new(LastTxInstantMemo::seeded(Some(last_tx_instant))),
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
            read_observer: None,
        })
    }

    /// Attach one transaction-scoped logical read observer to this value.
    ///
    /// This is intentionally crate-private: ordinary immutable database
    /// values carry no mutable diagnostics. For an overlay, observation is
    /// installed only at the outer merged-value boundary.
    pub(crate) fn with_read_observer(mut self, observer: Arc<LogicalReadObserver>) -> Self {
        if let ReadBasis::TransactionOverlay(overlay) = &self.basis {
            let mut overlay = (**overlay).clone();
            overlay.base.read_observer = None;
            self.basis = ReadBasis::TransactionOverlay(Arc::new(overlay));
        }
        self.read_observer = Some(observer);
        self
    }

    /// The basis remains the basis of the underlying immutable value even
    /// when an as-of or since window exposes less information.
    pub fn basis_t(&self) -> u64 {
        match &self.basis {
            ReadBasis::Eager(database) => database.basis_t(),
            ReadBasis::Native(snapshot) => snapshot.basis_t(),
            ReadBasis::TransactionOverlay(overlay) => overlay.basis_t,
        }
    }

    /// Exclusive entity-index issuance frontier at this immutable basis.
    pub fn eidx_frontier(&self) -> u64 {
        match &self.basis {
            ReadBasis::Eager(database) => database.eidx_frontier(),
            ReadBasis::Native(snapshot) => snapshot.eidx_frontier(),
            ReadBasis::TransactionOverlay(overlay) => overlay.eidx_frontier,
        }
    }

    /// The most recent transaction instant at this immutable basis.
    ///
    /// A native value resolves the single transaction entity through its lazy
    /// index at most once across immutable clones. It never invokes the eager
    /// compatibility materializer.
    pub fn last_tx_instant(&self) -> Result<Option<i64>, SemanticError> {
        self.last_tx_instant_memo
            .get_or_try_init(|| match &self.basis {
                ReadBasis::Eager(database) => Ok(database.last_tx_instant()),
                ReadBasis::Native(snapshot) => {
                    snapshot.last_tx_instant_observed(self.read_observer.as_deref())
                }
                ReadBasis::TransactionOverlay(overlay) => Ok(Some(overlay.last_tx_instant)),
            })
    }

    pub fn schema(&self) -> &Schema {
        match &self.basis {
            ReadBasis::Eager(database) => database.schema(),
            ReadBasis::Native(snapshot) => snapshot.schema(),
            ReadBasis::TransactionOverlay(overlay) => &overlay.schema,
        }
    }

    /// Share the immutable schema projection carried by this database value.
    /// This is the ownership counterpart of [`Self::schema`]: assessment can
    /// retain an unchanged projection without a whole-schema clone.
    pub(crate) fn schema_arc(&self) -> Arc<Schema> {
        match &self.basis {
            ReadBasis::Eager(database) => database.schema_arc(),
            ReadBasis::Native(snapshot) => snapshot.schema_arc(),
            ReadBasis::TransactionOverlay(overlay) => Arc::clone(&overlay.schema),
        }
    }

    /// Resolve an ident from the immutable basis' derived ident cache.
    /// Datomic's temporal predicates window index data, not this dictionary.
    pub fn entid(&self, ident: &Keyword) -> Option<u64> {
        match &self.basis {
            ReadBasis::Eager(database) => database.entid(ident),
            ReadBasis::Native(snapshot) => snapshot.entid(ident),
            ReadBasis::TransactionOverlay(overlay) => overlay
                .ident_assertions
                .iter()
                .find_map(|(candidate, entity)| (candidate == ident).then_some(*entity))
                .or_else(|| overlay.base.entid(ident)),
        }
    }

    pub fn ident(&self, entity: u64) -> Option<&Keyword> {
        match &self.basis {
            ReadBasis::Eager(database) => database.ident(entity),
            ReadBasis::Native(snapshot) => snapshot.ident(entity),
            ReadBasis::TransactionOverlay(overlay) => overlay
                .ident_assertions
                .iter()
                .find_map(|(ident, candidate)| (*candidate == entity).then_some(ident))
                .or_else(|| overlay.base.ident(entity)),
        }
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

    pub fn is_filtered(&self) -> bool {
        !self.filters.is_empty()
    }

    /// Derive a value with an inclusive upper transaction boundary.
    /// Repeating `as_of` replaces the prior upper boundary, matching the
    /// recovered immutable `Db` record update.
    pub fn as_of(mut self, t: u64) -> Self {
        self.as_of_t = Some(t);
        self
    }

    /// Derive a value with an exclusive lower transaction boundary.
    /// Repeating `since` replaces the prior lower boundary.
    pub fn since(mut self, t: u64) -> Self {
        self.since_t = Some(t);
        self
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

    /// Return datoms in one logical index after applying this value's complete
    /// temporal/custom window.
    pub fn datoms(&self, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
        let datoms = if self.direct_current() {
            self.basis_datoms(false, order)?
        } else if self.direct_history() {
            self.basis_datoms(true, order)?
        } else {
            let datoms = self.basis_datoms(true, order)?;
            self.window(datoms)?
        };
        self.charge_datoms(&datoms)?;
        Ok(datoms)
    }

    /// Stream a complete logical index for query evaluation. Point-current
    /// and raw-history values retain the native lazy cursor all the way into
    /// the query loop, so a transaction-local broad pattern does not first
    /// copy the durable database. Temporal/custom windows currently use their
    /// established materialized collapse path because they need cross-event
    /// retraction state.
    pub(crate) fn query_scan_cursor(
        &self,
        order: IndexOrder,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        if self.direct_current() {
            let mut cursor = self.basis_scan_cursor(false, order)?;
            cursor.observer = self.read_observer.clone();
            Ok(cursor)
        } else if self.direct_history() {
            let mut cursor = self.basis_scan_cursor(true, order)?;
            cursor.observer = self.read_observer.clone();
            Ok(cursor)
        } else {
            // `datoms` already charges the materialized, windowed result.
            Ok(DatabaseValueScanCursor {
                inner: Box::new(self.datoms(order)?.into_iter().map(Ok)),
                observer: None,
                failed: false,
            })
        }
    }

    /// Read a left-contiguous prefix before applying temporal/custom
    /// predicates. This preserves the important property that filtered and
    /// historical point reads do not first materialize an entire index.
    pub fn datoms_with_prefix(&self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        prefix.validate()?;
        let datoms = if self.direct_current() {
            self.basis_prefix(false, prefix)?
        } else if self.direct_history() {
            self.basis_prefix(true, prefix)?
        } else {
            let datoms = self.basis_prefix(true, prefix)?;
            self.window(datoms)?
        };
        self.charge_datoms(&datoms)?;
        Ok(datoms)
    }

    /// Lazily read one left-contiguous prefix of an unfiltered current value.
    ///
    /// Transaction processing requires exactly this point-current capability.
    /// Temporal, history, and custom-filter values intentionally use the
    /// existing materializing APIs until their retraction-window semantics can
    /// be represented by a dedicated streaming cursor.
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
        let inner = match &self.basis {
            ReadBasis::Eager(database) => DatabaseValuePrefixCursorInner::Eager(
                database.datoms_with_prefix(prefix)?.iter().cloned(),
            ),
            ReadBasis::Native(snapshot) => DatabaseValuePrefixCursorInner::Native(Box::new(
                snapshot.prefix_cursor(false, prefix)?,
            )),
            ReadBasis::TransactionOverlay(overlay) => {
                DatabaseValuePrefixCursorInner::Owned(overlay.prefix(false, prefix)?.into_iter())
            }
        };
        Ok(DatabaseValuePrefixCursor {
            inner,
            observer: self.read_observer.clone(),
            failed: false,
        })
    }

    /// Return the first historical datom in a prefix without reading the
    /// remainder. Schema validation uses this for recovered `has-values?`
    /// semantics, where even a retracted old value prevents adding uniqueness
    /// before physical AVET storage exists.
    pub(crate) fn history_prefix_first(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<Option<Datom>, SemanticError> {
        prefix.validate()?;
        let datom = match &self.basis {
            ReadBasis::Eager(database) => {
                Ok(database.history_with_prefix(prefix)?.first().cloned())
            }
            ReadBasis::Native(snapshot) => snapshot.prefix_cursor(true, prefix)?.next().transpose(),
            ReadBasis::TransactionOverlay(overlay) => {
                Ok(overlay.prefix(true, prefix)?.into_iter().next())
            }
        }?;
        if let Some(datom) = &datom {
            self.charge_datom(datom)?;
        }
        Ok(datom)
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
            ReadBasis::Native(snapshot) => snapshot.avet_ready(attribute),
            ReadBasis::TransactionOverlay(overlay) => {
                schema_has_avet(overlay.base.schema(), attribute)
                    && overlay.base.physical_avet_ready(attribute)
            }
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
            .datoms_with_prefix(&IndexPrefix::Avet {
                attribute,
                value: Some(value.clone()),
                entity: None,
            })?
            .first()
            .map(|datom| datom.entity))
    }

    /// Resolve the public eid/ident/lookup-ref forms against this exact value.
    pub fn resolve_entity_identifier(
        &self,
        identifier: &EntityIdentifier,
    ) -> Result<Option<u64>, SemanticError> {
        match identifier {
            EntityIdentifier::Id(entity) => {
                eid_to_eidx(*entity)?;
                Ok(Some(*entity))
            }
            EntityIdentifier::Ident(ident) => Ok(self.entid(ident)),
            EntityIdentifier::Lookup { attribute, value } => {
                let attribute = self.resolve_attribute(attribute)?;
                self.lookup(attribute, value)
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

    fn direct_history(&self) -> bool {
        self.history && self.as_of_t.is_none() && self.since_t.is_none() && self.filters.is_empty()
    }

    fn basis_datoms(&self, history: bool, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
        self.basis_scan_cursor(history, order)?.collect()
    }

    fn basis_scan_cursor(
        &self,
        history: bool,
        order: IndexOrder,
    ) -> Result<DatabaseValueScanCursor<'_>, SemanticError> {
        let inner: Box<dyn Iterator<Item = Result<Datom, SemanticError>> + '_> = match &self.basis {
            ReadBasis::Eager(database) => Box::new(
                database
                    .datoms(
                        if history {
                            crate::View::History
                        } else {
                            crate::View::Current
                        },
                        order,
                    )
                    .into_iter()
                    .map(Ok),
            ),
            ReadBasis::Native(snapshot) => {
                Box::new(snapshot.range_cursor(history, order, None, None)?)
            }
            ReadBasis::TransactionOverlay(overlay) => {
                Box::new(overlay.scan_cursor(history, order)?)
            }
        };
        Ok(DatabaseValueScanCursor {
            inner,
            observer: None,
            failed: false,
        })
    }

    fn basis_prefix(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<Vec<Datom>, SemanticError> {
        match &self.basis {
            ReadBasis::Eager(database) => {
                if history {
                    Ok(database.history_with_prefix(prefix)?.to_vec())
                } else {
                    Ok(database.datoms_with_prefix(prefix)?.to_vec())
                }
            }
            ReadBasis::Native(snapshot) => Ok(snapshot.datoms_with_prefix(history, prefix)?.datoms),
            ReadBasis::TransactionOverlay(overlay) => overlay.prefix(history, prefix),
        }
    }

    fn without_filters(&self) -> Self {
        let mut database = self.clone();
        database.filters = Arc::default();
        database
    }

    fn charge_datom(&self, datom: &Datom) -> Result<(), SemanticError> {
        if let Some(observer) = &self.read_observer {
            observer.charge_datom(datom)?;
        }
        Ok(())
    }

    fn charge_datoms(&self, datoms: &[Datom]) -> Result<(), SemanticError> {
        if let Some(observer) = &self.read_observer {
            observer.charge_datoms(datoms)?;
        }
        Ok(())
    }

    fn window(&self, datoms: Vec<Datom>) -> Result<Vec<Datom>, SemanticError> {
        let unfiltered = self.without_filters();
        let mut selected = Vec::with_capacity(datoms.len());
        for datom in datoms {
            let t = tx_to_t(datom.tx)?;
            if self.as_of_t.is_some_and(|as_of| t > as_of)
                || self.since_t.is_some_and(|since| t <= since)
                || !self
                    .filters
                    .iter()
                    .all(|predicate| predicate(&unfiltered, &datom))
            {
                continue;
            }
            selected.push(datom);
        }
        if self.history {
            Ok(selected)
        } else {
            Ok(collapse_retractions(selected))
        }
    }
}

impl TransactionOverlay {
    fn scan_cursor(
        &self,
        history: bool,
        order: IndexOrder,
    ) -> Result<TransactionOverlayScanCursor<'_>, SemanticError> {
        let base = self.base.basis_scan_cursor(history, order)?;
        let removals: Arc<[Datom]> = if history {
            Arc::from([])
        } else {
            self.tx_data
                .iter()
                .filter(|datom| !datom.added)
                .cloned()
                .collect::<Vec<_>>()
                .into()
        };
        let mut delta = Vec::new();

        // `add-avet` copies the attribute's existing AEVT working set into
        // the transaction-local AVET. Do the same bounded, attribute-local
        // backfill when this transaction first enables AVET; the durable base
        // remains streamed and untouched.
        if order == IndexOrder::Avet {
            for attribute in self.schema.attributes().filter(|attribute| {
                schema_has_avet(&self.schema, attribute.id)
                    && !schema_has_avet(self.base.schema(), attribute.id)
            }) {
                delta.extend(
                    self.base
                        .basis_prefix(
                            history,
                            &IndexPrefix::Aevt {
                                attribute: attribute.id,
                                entity: None,
                                value: None,
                            },
                        )?
                        .into_iter()
                        .filter(|datom| {
                            history
                                || !removals
                                    .iter()
                                    .any(|removal| same_stored_eav(removal, datom))
                        }),
                );
            }
        }

        for datom in self
            .tx_data
            .iter()
            .filter(|datom| overlay_index_member(&self.schema, datom, order))
            .filter(|datom| history || datom.added)
        {
            if history
                || !delta
                    .iter()
                    .any(|existing| same_stored_eav(existing, datom))
            {
                delta.push(datom.clone());
            }
        }
        delta.sort_by(|left, right| left.cmp_in(right, order));
        Ok(TransactionOverlayScanCursor {
            base,
            delta: delta.into_iter(),
            removals,
            schema: Arc::clone(&self.schema),
            history,
            order,
            base_next: None,
            delta_next: None,
            failed: false,
        })
    }

    fn prefix(&self, history: bool, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        let source_prefix = self.source_prefix(prefix);
        let mut datoms = self.base.basis_prefix(history, &source_prefix)?;
        datoms.retain(|datom| {
            overlay_index_member(&self.schema, datom, prefix.order())
                && compare_prefix(datom, prefix).is_eq()
        });

        let delta = self.tx_data.iter().filter(|datom| {
            overlay_index_member(&self.schema, datom, prefix.order())
                && compare_prefix(datom, prefix).is_eq()
        });
        if history {
            datoms.extend(delta.cloned());
        } else {
            for datom in delta {
                if datom.added {
                    // Repeated schema-hook assertions are transaction events
                    // even when the same stored E/A/V is already current.
                    // They enter history but do not replace the current fact's
                    // original transaction coordinate.
                    if !datoms.iter().any(|current| same_stored_eav(current, datom)) {
                        datoms.push(datom.clone());
                    }
                } else {
                    datoms.retain(|current| !same_stored_eav(current, datom));
                }
            }
        }
        datoms.sort_by(|left, right| left.cmp_in(right, prefix.order()));
        Ok(datoms)
    }

    fn source_prefix(&self, prefix: &IndexPrefix) -> IndexPrefix {
        match prefix {
            // Only AVET enablement needs the documented linear attribute
            // backfill. Steady indexed reads retain the caller's selective
            // AVET seek, while disabling can filter the old AVET range away.
            IndexPrefix::Avet { attribute, .. }
                if !schema_has_avet(self.base.schema(), *attribute)
                    && schema_has_avet(&self.schema, *attribute) =>
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
    fn fill_base(&mut self) -> Result<(), SemanticError> {
        while self.base_next.is_none() {
            let Some(candidate) = self.base.next().transpose()? else {
                break;
            };
            if !overlay_index_member(&self.schema, &candidate, self.order) {
                continue;
            }
            if !self.history
                && self
                    .removals
                    .iter()
                    .any(|removal| same_stored_eav(removal, &candidate))
            {
                continue;
            }
            self.base_next = Some(candidate);
        }
        Ok(())
    }

    fn next_result(&mut self) -> Result<Option<Datom>, SemanticError> {
        self.fill_base()?;
        if self.delta_next.is_none() {
            self.delta_next = self.delta.next();
        }
        match (&self.base_next, &self.delta_next) {
            (None, None) => Ok(None),
            (Some(_), None) => Ok(self.base_next.take()),
            (None, Some(_)) => Ok(self.delta_next.take()),
            (Some(base), Some(delta)) if !self.history && same_stored_eav(base, delta) => {
                // A repeated assertion is a transaction event in history but
                // leaves the original assertion coordinate current.
                self.delta_next = None;
                Ok(self.base_next.take())
            }
            (Some(base), Some(delta)) => match base.cmp_in(delta, self.order) {
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

impl Iterator for TransactionOverlayScanCursor<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        if self.failed {
            return None;
        }
        match self.next_result() {
            Ok(Some(datom)) => Some(Ok(datom)),
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

fn collapse_retractions(datoms: Vec<Datom>) -> Vec<Datom> {
    // Datomic's recovered index comparators place T after logical E/A/V. Our
    // physical comparators refine V with stored representation first, which is
    // necessary to retain values such as 1.0M and 1.00M but can separate one
    // exact value's events in AVET/VAET. Establish the recovered logical order
    // over this already prefix-limited result, decide visibility there, then
    // emit in the caller's original physical index order.
    let mut temporal_order = (0..datoms.len()).collect::<Vec<_>>();
    temporal_order.sort_by(|left, right| {
        let left = &datoms[*left];
        let right = &datoms[*right];
        left.entity
            .cmp(&right.entity)
            .then(left.attribute.cmp(&right.attribute))
            .then_with(|| left.value.index_cmp(&right.value))
            .then_with(|| right.tx.cmp(&left.tx))
            .then_with(|| right.added.cmp(&left.added))
            .then_with(|| left.value.stored_cmp(&right.value))
    });

    let mut visible = vec![false; datoms.len()];
    let mut start = 0;
    while let Some(first_offset) = temporal_order.get(start) {
        let first = &datoms[*first_offset];
        let mut end = start + 1;
        while temporal_order.get(end).is_some_and(|offset| {
            let candidate = &datoms[*offset];
            candidate.entity == first.entity
                && candidate.attribute == first.attribute
                && candidate.value.index_cmp(&first.value).is_eq()
        }) {
            end += 1;
        }

        // Track exact representations separately while walking newest first.
        // A binary-searched slice avoids cloning potentially large values.
        let mut retracted = Vec::<&Value>::new();
        for offset in &temporal_order[start..end] {
            let datom = &datoms[*offset];
            let stored = retracted.binary_search_by(|value| value.stored_cmp(&datom.value));
            if datom.added {
                // `filter-retractions` returns each assertion until a matching
                // retraction. This matters when a custom predicate removes an
                // intervening retraction and exposes multiple assertions.
                visible[*offset] = stored.is_err();
            } else if let Err(position) = stored {
                retracted.insert(position, &datom.value);
            }
        }

        // The recovered implementation uses `common/compare` as the exact
        // shadowing boundary. Its transaction redundancy path instead uses
        // `equals-with-strict-scale`, and native storage deliberately retains
        // 1.0M and 1.00M as distinct legal facts. Equality under `stored_cmp`
        // above is the port's `stored_eq` boundary: it keeps a window at the
        // current basis equal to the unwindowed value and recursively retains
        // the same distinction inside tuples.
        start = end;
    }
    datoms
        .into_iter()
        .zip(visible)
        .filter(|(_, visible)| *visible)
        .map(|(datom, _)| datom)
        .collect()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, Clause, DB_IDENT, DataPattern, EntityRef, FindElement, FindSpec,
        Query, QueryControl, Term, TxOp, TxReport, TxValue, USER_PARTITION, ValueType, Variable,
        make_eid,
    };
    use bigdecimal::BigDecimal;
    use std::cell::Cell;
    use std::str::FromStr;

    const AMOUNT: u32 = 1_000;
    const LINK: u32 = 1_001;

    fn decimal(value: &str) -> Value {
        Value::BigDec(BigDecimal::from_str(value).unwrap())
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
        .unwrap_err();
        assert_eq!(chained.code, "database/overlay-cannot-chain");

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
}
