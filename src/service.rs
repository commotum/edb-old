#[cfg(test)]
use crate::Datom;
use crate::connection::DatabaseIdentity;
use crate::runtime::{
    CapacityLimits, TransactorLease, WriterResidencyStats, is_postgres_connection_error,
};
use crate::{
    DatabaseValue, Digest, DurableTransaction, ErrorCategory, OperationContext, OperationKind,
    PostgresConnectionConfig, ProgramCacheStats, ProgramCall, RecoveryStats, SemanticError, TxForm,
    TxOp,
};
use std::collections::{BTreeMap, BTreeSet, VecDeque};
#[cfg(test)]
use std::mem::size_of;
use std::sync::atomic::{AtomicBool, AtomicU64, AtomicUsize, Ordering};
use std::sync::{Arc, Mutex, Weak, mpsc};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

#[path = "block_service.rs"]
mod block_service;

const DEFAULT_MEMORY_INDEX_THRESHOLD_BYTES: u64 = 32 * 1024 * 1024;
const DEFAULT_MEMORY_INDEX_MAX_BYTES: u64 = 512 * 1024 * 1024;
// Recovered `process-request-index` permits the initial publication attempt
// plus two retries before failing the transactor process.
const MAX_INDEX_JOB_RETRIES: usize = 2;
// RecentTier retains at most four raw BTSet entries per datom. Its
// allocator-independent conservative account reserves a fifth reference for
// persistent-log/tree overhead; keep admission on that same bound.
#[cfg(test)]
const MAX_RECENT_REFERENCES_PER_DATOM: u64 = 5;

/// Bounded novelty settings for the transactor-owned background indexer.
///
/// The byte measure is a deterministic native approximation: the retained
/// Rust `Datom`, canonical value bytes, and up to five compact references for
/// the four raw recent BTSet indexes plus persistent chunk-log/tree overhead.
/// It deliberately does not pretend to be allocator RSS. The defaults retain
/// Datomic Pro's documented 32 MiB scheduling point and 512 MiB back-pressure
/// point while tests and small deployments can use explicit lower values
/// without changing `TransactionServiceConfig` literals.
/// At the maximum, the writer parks ordinary dequeue while continuing lease
/// renewal and prioritizing indexing. The bounded admission queue may then
/// return `Busy` when it is genuinely full; committed idempotency retries that
/// have already reached the worker remain reconcilable without extending the
/// log. This preserves Datomic's index-first hard-limit behavior without an
/// unbounded native submission queue.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct BackgroundIndexingConfig {
    pub memory_index_threshold_bytes: u64,
    pub memory_index_max_bytes: u64,
}

impl Default for BackgroundIndexingConfig {
    fn default() -> Self {
        Self {
            memory_index_threshold_bytes: DEFAULT_MEMORY_INDEX_THRESHOLD_BYTES,
            memory_index_max_bytes: DEFAULT_MEMORY_INDEX_MAX_BYTES,
        }
    }
}

impl BackgroundIndexingConfig {
    fn validate(self) -> Result<Self, SemanticError> {
        if self.memory_index_threshold_bytes == 0
            || self.memory_index_max_bytes < self.memory_index_threshold_bytes
        {
            return Err(SemanticError::incorrect(
                "service/invalid-indexing-config",
                "memory index threshold must be positive and no greater than its maximum",
            ));
        }
        Ok(self)
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct BackgroundIndexingFailure {
    pub category: ErrorCategory,
    pub code: &'static str,
    pub message: String,
}

/// Observations of the coherent background index/search projection job.
/// The field name is retained, but failures can arise in canonical preparation,
/// search preparation, or guarded adoption; they are not search-only failures.
/// A successful job can find no fulltext attributes. These service observations
/// do not imply that an independent peer has caught up.
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct BackgroundFulltextStats {
    /// Source basis of the last successfully adopted coherent projection job.
    /// Prepared but unadopted candidates never advance this observation.
    pub checked_basis_t: Option<u64>,
    /// Frozen source basis of the last preparation attempt. A dispatch failure
    /// before preparation records the requested basis instead.
    pub attempted_basis_t: Option<u64>,
    pub attempts: u64,
    /// Failed whole-job attempts, including preparation and adoption failures.
    pub failures: u64,
    /// Always zero: there is no separate idle fulltext retry worker. Whole-job
    /// retries increment `attempts` and use the ordinary index retry policy.
    pub idle_retries: u64,
    /// Until the next real whole-job retry; None also covers a running attempt.
    pub retry_in: Option<Duration>,
    /// The whole-job retry budget was exhausted or ownership was lost, so this
    /// service stopped accepting work. Startup creates a new observation set.
    pub retry_exhausted: bool,
    pub last_failure: Option<BackgroundIndexingFailure>,
}

#[derive(Debug, Default)]
struct ProjectionObservations {
    stats: BackgroundFulltextStats,
    retry_at: Option<Instant>,
}

impl ProjectionObservations {
    fn started(&mut self, basis_t: u64) {
        self.stats.attempts = self.stats.attempts.saturating_add(1);
        self.stats.attempted_basis_t = Some(basis_t);
        self.retry_at = None;
        self.stats.retry_exhausted = false;
    }

    fn failed(&mut self, error: &SemanticError, retry_at: Option<Instant>, exhausted: bool) {
        self.stats.failures = self.stats.failures.saturating_add(1);
        self.stats.last_failure = Some(BackgroundIndexingFailure {
            category: error.category,
            code: error.code,
            message: error.message.clone(),
        });
        self.retry_at = retry_at;
        self.stats.retry_exhausted = exhausted;
    }

    fn adopted(&mut self, basis_t: u64) {
        self.stats.checked_basis_t = Some(basis_t);
        self.stats.last_failure = None;
        self.stats.retry_exhausted = false;
        self.retry_at = None;
    }

    fn snapshot_with_messages(&self, now: Instant, messages: bool) -> BackgroundFulltextStats {
        let mut stats =
            BackgroundFulltextStats {
                checked_basis_t: self.stats.checked_basis_t,
                attempted_basis_t: self.stats.attempted_basis_t,
                attempts: self.stats.attempts,
                failures: self.stats.failures,
                idle_retries: self.stats.idle_retries,
                retry_in: self.stats.retry_in,
                retry_exhausted: self.stats.retry_exhausted,
                last_failure: self.stats.last_failure.as_ref().map(|error| {
                    BackgroundIndexingFailure {
                        category: error.category,
                        code: error.code,
                        message: if messages {
                            error.message.clone()
                        } else {
                            String::new()
                        },
                    }
                }),
            };
        stats.retry_in = self.retry_at.map(|at| at.saturating_duration_since(now));
        stats
    }
}

/// Direct observations from the one background-indexing worker.
///
/// `indexing_*` is the frozen prefix being merged and `memory_index_*` is
/// novelty committed after that job was requested. Their sum is the pressure
/// used by admission, mirroring recovered `datomic.indexer/IndexerImpl`.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct BackgroundIndexingStats {
    /// Newest fully authenticated physical publication adopted by the
    /// service's backlog accounting.
    pub published_revision: u64,
    pub published_basis_t: u64,
    /// Authenticated broad AVET projection jobs still represented by the
    /// adopted manifest. This is physical cleanup/backfill observability, not
    /// Datomic's positive-add `t-needing-avet` / `sync-schema` coordinate:
    /// drops are included here too.
    pub pending_avet_projections: u64,
    /// Greatest append-only physical revision observed, usable or corrupt.
    pub newest_observed_revision: u64,
    pub target_basis_t: u64,
    pub memory_index_transactions: u64,
    pub memory_index_datoms: u64,
    pub memory_index_bytes: u64,
    pub indexing_transactions: u64,
    pub indexing_datoms: u64,
    pub indexing_bytes: u64,
    pub total_transactions: u64,
    pub total_datoms: u64,
    pub total_bytes: u64,
    pub jobs_started: u64,
    pub jobs_completed: u64,
    pub jobs_failed: u64,
    /// Times the worker deliberately stopped dequeuing ordinary work while
    /// the frozen/recent tiers were at the configured hard bound.
    pub backpressure_stalls: u64,
    /// Requests rejected only because the bounded admission queue filled
    /// while indexing pressure had the writer parked.
    pub backpressure_rejections: u64,
    pub job_in_flight: bool,
    pub last_failure: Option<BackgroundIndexingFailure>,
    pub fulltext: BackgroundFulltextStats,
    /// Accounted excision work is maintenance, not recent-tier residency.
    pub excision: crate::ExcisionProgress,
    pub excision_failure: Option<BackgroundIndexingFailure>,
}

#[derive(Clone, Debug)]
pub struct TransactionServiceConfig {
    /// One transport and I/O policy shared by every service-owned role.
    pub connection: PostgresConnectionConfig,
    /// Public catalog name at startup. A service resolves it once and retains
    /// the resulting immutable storage ID for its lease, reader and workers.
    pub database_id: String,
    pub holder_id: String,
    pub lease_duration: Duration,
    pub renew_interval: Duration,
    pub queue_capacity: usize,
    pub capacity_limits: CapacityLimits,
}

#[derive(Clone, Debug)]
pub struct TransactionRequest {
    pub request_key: String,
    /// One unordered collection of declarative transaction-data forms.
    /// Persisted function calls and entity maps are transaction data, not a
    /// second out-of-band request channel.
    pub forms: Vec<TxForm>,
    pub compare_basis_t: Option<u64>,
    pub tx_instant_override: Option<i64>,
}

impl TransactionRequest {
    pub fn new(request_key: impl Into<String>, operations: Vec<TxOp>) -> Self {
        Self {
            request_key: request_key.into(),
            forms: operations.into_iter().map(TxForm::Op).collect(),
            compare_basis_t: None,
            tx_instant_override: None,
        }
    }

    pub fn from_forms(request_key: impl Into<String>, forms: Vec<TxForm>) -> Self {
        Self {
            request_key: request_key.into(),
            forms,
            compare_basis_t: None,
            tx_instant_override: None,
        }
    }

    pub fn with_forms(mut self, forms: impl IntoIterator<Item = TxForm>) -> Self {
        self.forms.extend(forms);
        self
    }

    pub fn comparing_basis(mut self, basis_t: u64) -> Self {
        self.compare_basis_t = Some(basis_t);
        self
    }

    pub fn calling(mut self, call: ProgramCall) -> Self {
        self.forms.push(TxForm::ProgramCall(call));
        self
    }

    pub fn calling_all(mut self, calls: impl IntoIterator<Item = ProgramCall>) -> Self {
        self.forms
            .extend(calls.into_iter().map(TxForm::ProgramCall));
        self
    }

    pub fn with_tx_instant(mut self, instant: i64) -> Self {
        self.tx_instant_override = Some(instant);
        self
    }
}

#[derive(Clone, Debug)]
pub struct ServiceTransactionReport {
    /// The exact immutable value assessed by the transactor. Native values
    /// retain their authenticated root and recent tier without materializing
    /// the complete database in either the writer or this report.
    pub db_before: DatabaseValue,
    /// The exact immutable successor installed after durable publication.
    pub db_after: DatabaseValue,
    pub basis_t: u64,
    pub tx_hash: Digest,
    pub tx_data: Vec<crate::Datom>,
    pub tempids: BTreeMap<String, u64>,
    pub replayed: bool,
    /// Present only for a diagnostic submission or configured telemetry.
    /// Not encoded in durable receipts or reconstructed remote reports.
    pub diagnostics: Option<Arc<crate::TransactionDiagnostics>>,
}

struct Work {
    request: TransactionRequest,
    request_hash: Digest,
    response: mpsc::SyncSender<Result<ServiceTransactionReport, SemanticError>>,
    operation: OperationContext,
    hints: Option<crate::transaction_hints::PendingHints>,
}

/// A deterministic seam around the response-observation boundary. Production
/// contains no injectable path; crate tests key one fault to one database and
/// request so parallel tests cannot alter ordinary work.
#[cfg(test)]
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum CommitObservationFault {
    AbsentUnknownOutcome,
    BeforePublication,
    AfterCommitBeforeResponse,
}

#[cfg(test)]
fn observation_faults() -> &'static Mutex<BTreeMap<(String, String), CommitObservationFault>> {
    static FAULTS: std::sync::OnceLock<Mutex<BTreeMap<(String, String), CommitObservationFault>>> =
        std::sync::OnceLock::new();
    FAULTS.get_or_init(|| Mutex::new(BTreeMap::new()))
}

#[cfg(test)]
fn arm_observation_fault(database_id: &str, request_key: &str, fault: CommitObservationFault) {
    observation_faults()
        .lock()
        .expect("observation fault mutex poisoned")
        .insert((database_id.to_owned(), request_key.to_owned()), fault);
}

#[cfg(test)]
pub(crate) fn take_observation_fault(
    database_id: &str,
    request_key: &str,
) -> Option<CommitObservationFault> {
    observation_faults()
        .lock()
        .expect("observation fault mutex poisoned")
        .remove(&(database_id.to_owned(), request_key.to_owned()))
}

#[derive(Clone, Copy, Debug)]
struct Novelty {
    basis_t: u64,
    datoms: u64,
    bytes: u64,
}

#[derive(Debug)]
struct IndexingSeed {
    /// Newest fully authenticated publication selected for replay.
    published_revision: u64,
    published_basis_t: u64,
    pending_avet_projections: u64,
    /// Greatest physical publication coordinate, independent of validity.
    newest_observed_revision: u64,
    target_basis_t: u64,
    pending: VecDeque<Novelty>,
    /// A published value whose live-set/AVET work must finish independently
    /// of the ordinary novelty threshold.
    publication_work_through: Option<u64>,
    /// Latest schema-membership or explicit hard-cap demand that must reach
    /// a fully projected root before the special demand can be cleared.
    required_publication_t: u64,
    /// No usable native publication covers the newest observed revision. A
    /// positive generation's canonical basis-zero value is publishable too.
    needs_publication: bool,
}

#[derive(Debug)]
struct IndexingBacklog {
    published_revision: u64,
    published_basis_t: u64,
    pending_avet_projections: u64,
    newest_observed_revision: u64,
    target_basis_t: u64,
    pending: VecDeque<Novelty>,
    // Keep exact private totals so subtraction after publication still reports
    // the correct suffix even if a public u64 counter has saturated. A queue
    // of at most usize::MAX entries, each carrying u64 counts, fits in u128.
    total_datoms: u128,
    total_bytes: u128,
    indexing_through: Option<u64>,
    indexing_totals: IndexingTotals,
    publication_work_through: Option<u64>,
    required_publication_t: u64,
    needs_publication: bool,
}

#[derive(Clone, Copy, Debug, Default)]
struct IndexingTotals {
    transactions: u64,
    datoms: u128,
    bytes: u128,
}

fn public_index_count(count: u128) -> u64 {
    u64::try_from(count).unwrap_or(u64::MAX)
}

/// Finite committed frontier captured by an asynchronous indexing request.
///
/// `scheduled` means this call added indexing demand. False means an existing
/// request/job already covers the target, or it is already published. Neither
/// value is a completion receipt: use `Connection::sync_index(target_t, timeout)`.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct IndexRequest {
    pub target_t: u64,
    pub scheduled: bool,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
enum IndexCommand {
    Wake,
    Shutdown,
}

#[derive(Debug)]
struct BackgroundIndexing {
    config: BackgroundIndexingConfig,
    backlog: Mutex<IndexingBacklog>,
    sender: mpsc::SyncSender<IndexCommand>,
    jobs_started: AtomicU64,
    jobs_completed: AtomicU64,
    jobs_failed: AtomicU64,
    backpressure_stalls: AtomicU64,
    backpressure_rejections: AtomicU64,
    last_failure: Mutex<Option<SemanticError>>,
    fulltext: Mutex<ProjectionObservations>,
    excision: Mutex<(crate::ExcisionProgress, Option<SemanticError>)>,
    excision_requested: AtomicBool,
}

impl BackgroundIndexing {
    fn new(
        config: BackgroundIndexingConfig,
        seed: IndexingSeed,
        sender: mpsc::SyncSender<IndexCommand>,
    ) -> Self {
        let (total_datoms, total_bytes) =
            seed.pending
                .iter()
                .fold((0_u128, 0_u128), |(datoms, bytes), item| {
                    (
                        datoms + u128::from(item.datoms),
                        bytes + u128::from(item.bytes),
                    )
                });
        Self {
            config,
            backlog: Mutex::new(IndexingBacklog {
                published_revision: seed.published_revision,
                published_basis_t: seed.published_basis_t,
                pending_avet_projections: seed.pending_avet_projections,
                newest_observed_revision: seed.newest_observed_revision,
                target_basis_t: seed.target_basis_t,
                pending: seed.pending,
                total_datoms,
                total_bytes,
                indexing_through: None,
                indexing_totals: IndexingTotals::default(),
                publication_work_through: seed.publication_work_through,
                required_publication_t: seed.required_publication_t,
                needs_publication: seed.needs_publication,
            }),
            sender,
            jobs_started: AtomicU64::new(0),
            jobs_completed: AtomicU64::new(0),
            jobs_failed: AtomicU64::new(0),
            backpressure_stalls: AtomicU64::new(0),
            backpressure_rejections: AtomicU64::new(0),
            last_failure: Mutex::new(None),
            fulltext: Mutex::new(ProjectionObservations::default()),
            excision: Mutex::new(Default::default()),
            excision_requested: AtomicBool::new(true),
        }
    }

    fn note_commit(&self, novelty: Novelty, changes_avet_membership: bool) {
        let should_wake = {
            let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
            if novelty.basis_t <= backlog.published_basis_t {
                return;
            }
            if backlog
                .pending
                .back()
                .is_some_and(|previous| previous.basis_t >= novelty.basis_t)
            {
                // Fresh authoritative commits are ordered by the one writer.
                // A duplicate basis can only be a replay/race already covered
                // by a publication and must not be charged twice.
                return;
            }
            backlog.target_basis_t = backlog.target_basis_t.max(novelty.basis_t);
            backlog.pending.push_back(novelty);
            backlog.total_datoms += u128::from(novelty.datoms);
            backlog.total_bytes += u128::from(novelty.bytes);
            // AVET membership is information-derived from :db/index and
            // :db/unique. Enabling either requires a broad durable rebuild of
            // prior values even when ordinary novelty is below the byte
            // threshold; disabling it should publish the matching physical
            // projection as well.
            backlog.needs_publication |= changes_avet_membership;
            if changes_avet_membership {
                backlog.required_publication_t =
                    backlog.required_publication_t.max(novelty.basis_t);
            }
            should_index(&backlog, self.config)
        };
        if should_wake {
            self.wake();
        }
    }

    /// Demand a publication independently of the ordinary novelty threshold.
    /// This is the liveness escape hatch when `RecentTier` refuses a
    /// not-yet-assessed append at its own exact hard-cap boundary. The request
    /// remains parked and is retried only after the covering publication.
    fn force_publication(&self) -> bool {
        let can_advance = {
            let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
            if backlog.target_basis_t <= backlog.published_basis_t {
                false
            } else {
                backlog.needs_publication = true;
                // A force racing an older publication's maintenance must
                // survive that publication's final completion signal.
                backlog.required_publication_t =
                    backlog.required_publication_t.max(backlog.target_basis_t);
                true
            }
        };
        if can_advance {
            self.wake();
        }
        can_advance
    }

    fn request_index(&self) -> IndexRequest {
        let request = {
            let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
            // Only note_commit (after durable success) and verified publications
            // advance this frontier. Queued/unassessed transactions are excluded.
            let target_t = backlog.target_basis_t;
            let covered = backlog
                .published_basis_t
                .max(backlog.required_publication_t)
                .max(backlog.indexing_through.unwrap_or(0));
            let scheduled = target_t > covered;
            if scheduled {
                backlog.required_publication_t = target_t;
                backlog.needs_publication = true;
            }
            IndexRequest {
                target_t,
                scheduled,
            }
        };
        if request.scheduled {
            self.wake();
        }
        request
    }

    fn wake(&self) {
        match self.sender.try_send(IndexCommand::Wake) {
            Ok(()) | Err(mpsc::TrySendError::Full(_)) => {}
            Err(mpsc::TrySendError::Disconnected(_)) => {}
        }
    }

    fn shutdown(&self) {
        let _ = self.sender.send(IndexCommand::Shutdown);
    }

    fn begin_job(&self) -> Option<u64> {
        let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
        if backlog.indexing_through.is_some() {
            return None;
        }
        if !should_index(&backlog, self.config) {
            return None;
        }
        let through = backlog
            .publication_work_through
            .unwrap_or(backlog.target_basis_t);
        // An ordinary job freezes the entire currently committed tail. A
        // maintenance job works only on an already-published value: pending
        // novelty is strictly newer and stays in the live memory index.
        backlog.indexing_totals = if backlog.publication_work_through.is_some() {
            debug_assert!(through <= backlog.published_basis_t);
            IndexingTotals::default()
        } else {
            IndexingTotals {
                transactions: backlog.pending.len() as u64,
                datoms: backlog.total_datoms,
                bytes: backlog.total_bytes,
            }
        };
        backlog.indexing_through = Some(through);
        self.jobs_started.fetch_add(1, Ordering::Relaxed);
        Some(through)
    }

    fn complete_job(
        &self,
        published_revision: u64,
        published_basis_t: u64,
        pending_avet_projections: usize,
        index_work_remaining: bool,
    ) {
        self.publish_completed(
            published_revision,
            published_basis_t,
            pending_avet_projections,
            index_work_remaining,
        );
        self.jobs_completed.fetch_add(1, Ordering::Relaxed);
    }

    fn publish_completed(
        &self,
        published_revision: u64,
        published_basis_t: u64,
        pending_avet_projections: usize,
        index_work_remaining: bool,
    ) {
        let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
        backlog.published_revision = backlog.published_revision.max(published_revision);
        backlog.newest_observed_revision = backlog.newest_observed_revision.max(published_revision);
        backlog.published_basis_t = backlog.published_basis_t.max(published_basis_t);
        backlog.target_basis_t = backlog.target_basis_t.max(published_basis_t);
        backlog.pending_avet_projections = pending_avet_projections as u64;
        while backlog
            .pending
            .front()
            .is_some_and(|novelty| novelty.basis_t <= published_basis_t)
        {
            if let Some(novelty) = backlog.pending.pop_front() {
                backlog.total_datoms -= u128::from(novelty.datoms);
                backlog.total_bytes -= u128::from(novelty.bytes);
            }
        }
        backlog.indexing_through = None;
        backlog.indexing_totals = IndexingTotals::default();
        backlog.publication_work_through = index_work_remaining.then_some(published_basis_t);
        backlog.needs_publication = index_work_remaining
            || pending_avet_projections != 0
            || backlog.published_revision < backlog.newest_observed_revision
            || backlog.published_basis_t < backlog.required_publication_t;
    }

    fn fail_job(&self, error: SemanticError) {
        {
            let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
            backlog.indexing_through = None;
            backlog.indexing_totals = IndexingTotals::default();
        }
        *self
            .last_failure
            .lock()
            .expect("index failure mutex poisoned") = Some(error);
        self.jobs_failed.fetch_add(1, Ordering::Relaxed);
    }

    /// A failed preparation/adoption has published no observed completion.
    /// Keep all demand/novelty while releasing its frozen in-flight accounting.
    fn retry_job(&self) {
        let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
        backlog.indexing_through = None;
        backlog.indexing_totals = IndexingTotals::default();
    }

    fn should_continue(&self) -> bool {
        let backlog = self.backlog.lock().expect("index backlog mutex poisoned");
        should_index(&backlog, self.config)
    }

    fn limiting_error(&self) -> Option<SemanticError> {
        if let Some(error) = self
            .last_failure
            .lock()
            .expect("index failure mutex poisoned")
            .as_ref()
        {
            return Some(
                SemanticError::new(
                    ErrorCategory::Unavailable,
                    "service/indexing-failed",
                    "background indexing failed; writes are closed",
                )
                .detail("cause_code", error.code)
                .detail("cause", error.message.clone()),
            );
        }
        let total_bytes = self
            .backlog
            .lock()
            .expect("index backlog mutex poisoned")
            .total_bytes;
        if total_bytes > u128::from(self.config.memory_index_max_bytes) {
            return Some(
                SemanticError::new(
                    ErrorCategory::Busy,
                    "service/index-backpressure",
                    "recent index novelty reached the configured hard limit",
                )
                .detail("backlog_bytes", public_index_count(total_bytes).to_string())
                .detail(
                    "memory_index_max_bytes",
                    self.config.memory_index_max_bytes.to_string(),
                ),
            );
        }
        None
    }

    fn has_failure(&self) -> bool {
        self.last_failure
            .lock()
            .expect("index failure mutex poisoned")
            .is_some()
    }

    #[cfg(test)]
    fn published_revision(&self) -> u64 {
        self.backlog
            .lock()
            .expect("index backlog mutex poisoned")
            .published_revision
    }

    fn at_hard_limit(&self) -> bool {
        self.backlog
            .lock()
            .expect("index backlog mutex poisoned")
            .total_bytes
            > u128::from(self.config.memory_index_max_bytes)
    }

    fn record_backpressure_stall(&self) {
        self.backpressure_stalls.fetch_add(1, Ordering::Relaxed);
    }

    fn record_backpressure_rejection(&self) {
        self.backpressure_rejections.fetch_add(1, Ordering::Relaxed);
    }

    fn projection_started(&self, basis_t: u64) {
        self.fulltext
            .lock()
            .expect("projection status mutex poisoned")
            .started(basis_t);
    }

    fn projection_failed(&self, error: &SemanticError, retry_at: Option<Instant>, exhausted: bool) {
        self.fulltext
            .lock()
            .expect("projection status mutex poisoned")
            .failed(error, retry_at, exhausted);
    }

    fn projection_adopted(&self, basis_t: u64) {
        self.fulltext
            .lock()
            .expect("projection status mutex poisoned")
            .adopted(basis_t);
    }

    fn stats(&self) -> BackgroundIndexingStats {
        self.stats_with_messages(true)
    }

    fn stats_with_messages(&self, messages: bool) -> BackgroundIndexingStats {
        let backlog = self.backlog.lock().expect("index backlog mutex poisoned");
        // Status sampling must not make fixed-size commits proportional to the
        // accumulated tail. Only startup and retiring a published prefix walk
        // pending entries; frozen/live counts are maintained by their writers.
        let indexing = backlog.indexing_totals;
        let total_transactions = backlog.pending.len() as u64;
        let last_failure = self
            .last_failure
            .lock()
            .expect("index failure mutex poisoned")
            .as_ref()
            .map(|error| BackgroundIndexingFailure {
                category: error.category,
                code: error.code,
                message: if messages {
                    error.message.clone()
                } else {
                    String::new()
                },
            });
        let excision = self
            .excision
            .lock()
            .expect("excision status mutex poisoned");
        BackgroundIndexingStats {
            excision: excision.0.clone(),
            excision_failure: excision.1.as_ref().map(|e| BackgroundIndexingFailure {
                category: e.category,
                code: e.code,
                message: if messages {
                    e.message.clone()
                } else {
                    String::new()
                },
            }),
            published_revision: backlog.published_revision,
            published_basis_t: backlog.published_basis_t,
            pending_avet_projections: backlog.pending_avet_projections,
            newest_observed_revision: backlog.newest_observed_revision,
            target_basis_t: backlog.target_basis_t,
            memory_index_transactions: total_transactions - indexing.transactions,
            memory_index_datoms: public_index_count(backlog.total_datoms - indexing.datoms),
            memory_index_bytes: public_index_count(backlog.total_bytes - indexing.bytes),
            indexing_transactions: indexing.transactions,
            indexing_datoms: public_index_count(indexing.datoms),
            indexing_bytes: public_index_count(indexing.bytes),
            total_transactions,
            total_datoms: public_index_count(backlog.total_datoms),
            total_bytes: public_index_count(backlog.total_bytes),
            jobs_started: self.jobs_started.load(Ordering::Relaxed),
            jobs_completed: self.jobs_completed.load(Ordering::Relaxed),
            jobs_failed: self.jobs_failed.load(Ordering::Relaxed),
            backpressure_stalls: self.backpressure_stalls.load(Ordering::Relaxed),
            backpressure_rejections: self.backpressure_rejections.load(Ordering::Relaxed),
            job_in_flight: backlog.indexing_through.is_some(),
            last_failure,
            fulltext: self
                .fulltext
                .lock()
                .expect("projection status mutex poisoned")
                .snapshot_with_messages(Instant::now(), messages),
        }
    }
}

fn should_index(backlog: &IndexingBacklog, config: BackgroundIndexingConfig) -> bool {
    // Positive log generations have a canonical native basis-zero value.
    // `needs_publication` therefore drives fresh-database bootstrap and
    // same-basis repair even before the first ordinary transaction.
    backlog.needs_publication
        || (backlog.target_basis_t > backlog.published_basis_t
            && backlog.total_bytes > u128::from(config.memory_index_threshold_bytes))
}

#[derive(Debug)]
struct ReportSubscriber {
    sender: mpsc::Sender<ServiceTransactionReport>,
    pending: Arc<AtomicUsize>,
    retention: Arc<Mutex<VecDeque<ReportRetention>>>,
}

#[derive(Clone, Debug)]
struct ReportRetention {
    basis_t: u64,
    payload_bytes: u64,
    roots: [Option<Digest>; 2],
    generations: [Option<u64>; 2],
}

impl ReportRetention {
    fn from_report(report: &ServiceTransactionReport) -> Self {
        let before = report.db_before.native_retention_coordinate();
        let after = report.db_after.native_retention_coordinate();
        let tx_data_bytes = report.tx_data.iter().fold(0_u64, |bytes, datom| {
            bytes.saturating_add(datom.retained_bytes())
        });
        let tempid_bytes = report.tempids.keys().fold(0_u64, |bytes, tempid| {
            bytes
                .saturating_add(std::mem::size_of::<String>() as u64)
                .saturating_add(tempid.capacity() as u64)
                .saturating_add(std::mem::size_of::<u64>() as u64)
        });
        Self {
            basis_t: report.basis_t,
            // Stable owned-payload account. Shared immutable database trees
            // are represented separately by distinct root/generation counts;
            // allocator and mpsc-node overhead are intentionally excluded.
            payload_bytes: (std::mem::size_of::<ServiceTransactionReport>() as u64)
                .saturating_add(tx_data_bytes)
                .saturating_add(tempid_bytes),
            roots: [
                before.and_then(|value| value.1),
                after.and_then(|value| value.1),
            ],
            generations: [before.map(|value| value.0), after.map(|value| value.0)],
        }
    }
}

pub(crate) type CommitObserver = Arc<dyn Fn(Arc<ServiceTransactionReport>) + Send + Sync>;

/// Registration does not own the writer. Removing it releases the peer and
/// does not affect admitted transactions or other connections.
pub(crate) struct NativeObserver {
    id: u64,
    shared: Weak<Shared>,
}

impl Drop for NativeObserver {
    fn drop(&mut self) {
        if let Some(shared) = self.shared.upgrade() {
            shared
                .observers
                .lock()
                .expect("observer mutex poisoned")
                .remove(&self.id);
        }
    }
}

struct Shared {
    accepting: AtomicBool,
    admission: Mutex<()>,
    queued: AtomicUsize,
    max_queued: AtomicUsize,
    processed: AtomicU64,
    rejected_full: AtomicU64,
    next_subscriber: AtomicU64,
    subscribers: Mutex<BTreeMap<u64, ReportSubscriber>>,
    observers: Mutex<BTreeMap<u64, CommitObserver>>,
    queued_reports: AtomicUsize,
    max_queued_reports: AtomicUsize,
    queued_report_payload_bytes: AtomicU64,
    max_queued_report_payload_bytes: AtomicU64,
    writer_residency: Mutex<WriterResidencyStats>,
    program_cache: crate::program_cache::SharedProgramCache,
    max_request_bytes: usize,
    hint_worker: crate::transaction_hints::HintWorkerSlot,
    indexing: Arc<BackgroundIndexing>,
    database_id: String,
    lineage_id: String,
    transport_lease: TransactorLease,
    request_identity: [u8; 16],
    telemetry: Option<crate::TelemetryEmitter>,
}

impl Shared {
    fn new(
        max_request_bytes: usize,
        writer_residency: WriterResidencyStats,
        indexing: Arc<BackgroundIndexing>,
        database_id: String,
        lineage_id: String,
        transport_lease: TransactorLease,
        request_identity: [u8; 16],
    ) -> Self {
        Self {
            accepting: AtomicBool::new(true),
            admission: Mutex::new(()),
            queued: AtomicUsize::new(0),
            max_queued: AtomicUsize::new(0),
            processed: AtomicU64::new(0),
            rejected_full: AtomicU64::new(0),
            next_subscriber: AtomicU64::new(1),
            subscribers: Mutex::new(BTreeMap::new()),
            observers: Mutex::new(BTreeMap::new()),
            queued_reports: AtomicUsize::new(0),
            max_queued_reports: AtomicUsize::new(0),
            queued_report_payload_bytes: AtomicU64::new(0),
            max_queued_report_payload_bytes: AtomicU64::new(0),
            writer_residency: Mutex::new(writer_residency),
            program_cache: Default::default(),
            max_request_bytes,
            hint_worker: crate::transaction_hints::HintWorkerSlot::default(),
            indexing,
            database_id,
            lineage_id,
            transport_lease,
            request_identity,
            telemetry: None,
        }
    }

    fn unavailable(&self) -> SemanticError {
        SemanticError::new(
            ErrorCategory::Unavailable,
            "service/unavailable",
            "transaction service is not accepting requests",
        )
    }

    fn indexing_stats(&self, messages: bool) -> BackgroundIndexingStats {
        self.indexing.stats_with_messages(messages)
    }

    fn observe(&self, report: &ServiceTransactionReport) {
        let observers = self.observers.lock().expect("observer mutex poisoned");
        if !observers.is_empty() {
            let report = Arc::new(report.clone());
            for observer in observers.values() {
                observer(Arc::clone(&report));
            }
        }
    }

    fn publish(&self, report: &ServiceTransactionReport) {
        // Also covers a durable commit discovered after an unknown outcome.
        // Native adoption is idempotent; normal outcomes were observed before
        // their originating result was delivered.
        self.observe(report);
        self.publish_reports(report);
    }

    fn publish_reports(&self, report: &ServiceTransactionReport) {
        let retention = ReportRetention::from_report(report);
        let mut subscribers = self.subscribers.lock().expect("subscriber mutex poisoned");
        subscribers.retain(|_, subscriber| {
            subscriber.pending.fetch_add(1, Ordering::AcqRel);
            let queued = self.queued_reports.fetch_add(1, Ordering::AcqRel) + 1;
            self.max_queued_reports.fetch_max(queued, Ordering::Relaxed);
            let payload_bytes = self
                .queued_report_payload_bytes
                .fetch_add(retention.payload_bytes, Ordering::AcqRel)
                .saturating_add(retention.payload_bytes);
            self.max_queued_report_payload_bytes
                .fetch_max(payload_bytes, Ordering::Relaxed);
            subscriber
                .retention
                .lock()
                .expect("report retention mutex poisoned")
                .push_back(retention.clone());
            if subscriber.sender.send(report.clone()).is_ok() {
                true
            } else {
                let removed = subscriber
                    .retention
                    .lock()
                    .expect("report retention mutex poisoned")
                    .pop_back()
                    .expect("failed report send has queued retention");
                subscriber.pending.fetch_sub(1, Ordering::AcqRel);
                self.queued_reports.fetch_sub(1, Ordering::AcqRel);
                self.queued_report_payload_bytes
                    .fetch_sub(removed.payload_bytes, Ordering::AcqRel);
                false
            }
        });
    }
}

#[derive(Clone)]
pub struct TransactionClient {
    sender: mpsc::SyncSender<Work>,
    shared: Arc<Shared>,
}

impl TransactionClient {
    /// Register a nonblocking notification hint. Setup must not hold the writer
    /// notification lock across peer I/O. Durable-log catch-up fills any gap
    /// between initial opening and registration, or a full local hint channel.
    pub(crate) fn observe_commits<T>(
        &self,
        setup: impl FnOnce() -> Result<(T, CommitObserver), SemanticError>,
    ) -> Result<(T, NativeObserver), SemanticError> {
        let (value, observer) = setup()?;
        let mut observers = self
            .shared
            .observers
            .lock()
            .expect("observer mutex poisoned");
        let id = self.shared.next_subscriber.fetch_add(1, Ordering::Relaxed);
        observers.insert(id, observer);
        Ok((
            value,
            NativeObserver {
                id,
                shared: Arc::downgrade(&self.shared),
            },
        ))
    }

    /// Stable identity of the one database this client can transact against.
    ///
    /// Request-key hashing is lineage scoped, so exposing the same pair to
    /// the native connection facade lets it reject a peer/client mismatch
    /// before either side can publish observable state.
    pub fn identity(&self) -> DatabaseIdentity {
        DatabaseIdentity::new(
            self.shared.database_id.clone(),
            self.shared.lineage_id.clone(),
        )
    }

    pub(crate) fn transport_lease(&self) -> TransactorLease {
        self.shared.transport_lease.clone()
    }

    pub fn submit(&self, request: TransactionRequest) -> Result<TransactionTicket, SemanticError> {
        self.submit_advisory(request, None)
    }

    /// Admit hints separately from canonical transaction data and request
    /// identity. The execution handle observes best-effort bounded prefetch;
    /// canceling it cannot cancel or change the submitted transaction.
    pub fn submit_with_hints(
        &self,
        request: TransactionRequest,
        hints: crate::TransactionHints,
        options: crate::HintPrefetchOptions,
    ) -> Result<(TransactionTicket, crate::HintExecution), SemanticError> {
        let execution = crate::HintExecution::default();
        let hints = crate::transaction_hints::PendingHints::new(hints, options, execution.clone());
        let ticket = self.submit_advisory(request, Some(hints))?;
        Ok((ticket, execution))
    }

    fn submit_advisory(
        &self,
        request: TransactionRequest,
        hints: Option<crate::transaction_hints::PendingHints>,
    ) -> Result<TransactionTicket, SemanticError> {
        // Attribution crosses the queue explicitly. Contexts contain counters,
        // not storage authority; no worker automatically invokes their callback.
        let detailed = self
            .shared
            .telemetry
            .as_ref()
            .is_some_and(crate::TelemetryEmitter::is_enabled);
        let operation = match (OperationContext::current(), detailed) {
            (Some(parent), true) => parent.child_diagnostic(OperationKind::Transaction),
            (Some(parent), false) => parent.child(OperationKind::Transaction),
            (None, true) => OperationContext::diagnostic(OperationKind::Transaction),
            (None, false) => OperationContext::new(OperationKind::Transaction),
        };
        let _operation_scope = operation.enter();
        if !self.shared.accepting.load(Ordering::Acquire) {
            return Err(self.shared.unavailable());
        }
        if request.request_key.is_empty() {
            return Err(SemanticError::incorrect(
                "postgres/empty-request-key",
                "idempotency request key cannot be empty",
            ));
        }
        if request.request_key.len() > 1_024 {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "service/request-key-capacity",
                "idempotency request key exceeds the 1024-byte admission limit",
            ));
        }
        let (request_hash, _) = crate::encoding::canonical_submission_request(
            &request.forms,
            request.compare_basis_t,
            request.tx_instant_override,
            self.shared.max_request_bytes,
        )?;
        let request_key = request.request_key.clone();
        let request_key_hash = crate::storage::receipts::scoped_request_key(
            &self.shared.request_identity,
            &request_key,
        )?;
        let (sender, receiver) = mpsc::sync_channel(1);
        let _admission = self
            .shared
            .admission
            .lock()
            .expect("admission mutex poisoned");
        match self.sender.try_send(Work {
            request,
            request_hash,
            response: sender,
            operation,
            hints,
        }) {
            Ok(()) => {
                let queued = self.shared.queued.fetch_add(1, Ordering::AcqRel) + 1;
                self.shared.max_queued.fetch_max(queued, Ordering::Relaxed);
                Ok(TransactionTicket {
                    request_key,
                    request_key_hash,
                    receiver,
                })
            }
            Err(mpsc::TrySendError::Full(_)) => {
                self.shared.rejected_full.fetch_add(1, Ordering::Relaxed);
                if self.shared.indexing.at_hard_limit() {
                    self.shared.indexing.record_backpressure_rejection();
                }
                Err(SemanticError::new(
                    ErrorCategory::Busy,
                    "service/queue-full",
                    "transaction service queue is full",
                ))
            }
            Err(mpsc::TrySendError::Disconnected(_)) => Err(self.shared.unavailable()),
        }
    }

    pub fn transact(
        &self,
        request: TransactionRequest,
        timeout: Duration,
    ) -> Result<ServiceTransactionReport, SemanticError> {
        self.submit(request)?.wait(timeout)
    }

    pub fn subscribe_reports(&self) -> ReportSubscription {
        let id = self.shared.next_subscriber.fetch_add(1, Ordering::Relaxed);
        let (sender, receiver) = mpsc::channel();
        let pending = Arc::new(AtomicUsize::new(0));
        let retention = Arc::new(Mutex::new(VecDeque::new()));
        self.shared
            .subscribers
            .lock()
            .expect("subscriber mutex poisoned")
            .insert(
                id,
                ReportSubscriber {
                    sender,
                    pending: Arc::clone(&pending),
                    retention: Arc::clone(&retention),
                },
            );
        ReportSubscription {
            id,
            receiver: Some(receiver),
            pending,
            retention,
            shared: Arc::downgrade(&self.shared),
        }
    }

    /// Fixed-size local counters; unlike `stats`, this never walks retained
    /// subscriber reports or takes their queue locks.
    pub fn operational_stats(&self) -> OperationalServiceStats {
        OperationalServiceStats {
            accepting: self.shared.accepting.load(Ordering::Acquire),
            queued: self.shared.queued.load(Ordering::Relaxed),
            max_queued: self.shared.max_queued.load(Ordering::Relaxed),
            processed: self.shared.processed.load(Ordering::Relaxed),
            rejected_full: self.shared.rejected_full.load(Ordering::Relaxed),
        }
    }

    pub(crate) fn operational_indexing_stats(&self) -> BackgroundIndexingStats {
        self.shared.indexing_stats(false)
    }

    pub fn stats(&self) -> ServiceStats {
        let subscribers = self
            .shared
            .subscribers
            .lock()
            .expect("subscriber mutex poisoned");
        let mut oldest_queued_report_basis_t = None;
        let mut report_roots = BTreeSet::new();
        let mut report_generations = BTreeSet::new();
        let mut queued_reports = 0_usize;
        let mut queued_report_payload_bytes = 0_u64;
        for subscriber in subscribers.values() {
            let retention = subscriber
                .retention
                .lock()
                .expect("report retention mutex poisoned");
            for retained in retention.iter() {
                queued_reports = queued_reports.saturating_add(1);
                queued_report_payload_bytes =
                    queued_report_payload_bytes.saturating_add(retained.payload_bytes);
                oldest_queued_report_basis_t = Some(
                    oldest_queued_report_basis_t
                        .map_or(retained.basis_t, |oldest: u64| oldest.min(retained.basis_t)),
                );
                report_roots.extend(retained.roots.into_iter().flatten());
                report_generations.extend(retained.generations.into_iter().flatten());
            }
        }
        ServiceStats {
            queued: self.shared.queued.load(Ordering::Relaxed),
            max_queued: self.shared.max_queued.load(Ordering::Relaxed),
            processed: self.shared.processed.load(Ordering::Relaxed),
            rejected_full: self.shared.rejected_full.load(Ordering::Relaxed),
            subscribers: subscribers.len(),
            queued_reports,
            max_queued_reports: self.shared.max_queued_reports.load(Ordering::Relaxed),
            queued_report_payload_bytes,
            max_queued_report_payload_bytes: self
                .shared
                .max_queued_report_payload_bytes
                .load(Ordering::Relaxed),
            oldest_queued_report_basis_t,
            distinct_report_roots: report_roots.len(),
            distinct_report_generations: report_generations.len(),
        }
    }

    /// Snapshot maintained live/frozen aggregates without scanning the backlog.
    pub fn background_indexing_stats(&self) -> BackgroundIndexingStats {
        self.shared.indexing_stats(true)
    }

    /// Request background indexing through the currently observed committed
    /// frontier, independent of the configured novelty threshold.
    ///
    /// Returns after bounded in-memory coordination with the existing index
    /// worker; it performs no indexing or SQL on the caller. Requests coalesce,
    /// and later commits cannot change the returned target. Service shutdown or
    /// failure may prevent completion; a timed-out waiter does not cancel the
    /// shared job. No separate worker or writer authority is created.
    /// A previous optional fulltext failure is also retried, even if the
    /// returned canonical target is already indexed. Search status is separate
    /// and does not change this request's canonical completion semantics.
    pub fn request_index(&self) -> Result<IndexRequest, SemanticError> {
        if !self.shared.accepting.load(Ordering::Acquire) || self.shared.indexing.has_failure() {
            return Err(self.shared.unavailable());
        }
        Ok(self.shared.indexing.request_index())
    }

    /// Deterministic representation-level residency of the live writer.
    /// Unlike allocator RSS, these counters distinguish the recent tier and
    /// immutable cache from a forbidden eager database value.
    pub fn writer_residency_stats(&self) -> WriterResidencyStats {
        *self
            .shared
            .writer_residency
            .lock()
            .expect("writer residency mutex poisoned")
    }

    pub fn is_available(&self) -> bool {
        self.shared.accepting.load(Ordering::Acquire) && !self.shared.indexing.has_failure()
    }
}

#[derive(Debug)]
pub struct TransactionTicket {
    request_key: String,
    request_key_hash: Digest,
    receiver: mpsc::Receiver<Result<ServiceTransactionReport, SemanticError>>,
}

impl TransactionTicket {
    pub fn wait(self, timeout: Duration) -> Result<ServiceTransactionReport, SemanticError> {
        match self.receiver.recv_timeout(timeout) {
            Ok(result) => result,
            Err(mpsc::RecvTimeoutError::Timeout) => Err(unknown_outcome(
                self.request_key_hash,
                "timed out waiting for an admitted transaction",
            )),
            Err(mpsc::RecvTimeoutError::Disconnected) => Err(unknown_outcome(
                self.request_key_hash,
                "transaction worker disconnected after admission",
            )),
        }
    }

    pub fn request_key(&self) -> &str {
        &self.request_key
    }
}

pub struct ReportSubscription {
    id: u64,
    receiver: Option<mpsc::Receiver<ServiceTransactionReport>>,
    pending: Arc<AtomicUsize>,
    retention: Arc<Mutex<VecDeque<ReportRetention>>>,
    shared: Weak<Shared>,
}

impl ReportSubscription {
    pub fn recv_timeout(
        &self,
        timeout: Duration,
    ) -> Result<ServiceTransactionReport, mpsc::RecvTimeoutError> {
        let result = self
            .receiver
            .as_ref()
            .expect("live report subscription retains its receiver")
            .recv_timeout(timeout);
        if result.is_ok() {
            self.note_received();
        }
        result
    }

    pub fn try_recv(&self) -> Result<ServiceTransactionReport, mpsc::TryRecvError> {
        let result = self
            .receiver
            .as_ref()
            .expect("live report subscription retains its receiver")
            .try_recv();
        if result.is_ok() {
            self.note_received();
        }
        result
    }

    /// Reports currently retained for this lossless subscriber. Every queued
    /// report owns its immutable db-before/db-after snapshots and their
    /// in-memory read resources until received or dropped; GC retention is age-based.
    pub fn pending_reports(&self) -> usize {
        self.pending.load(Ordering::Acquire)
    }

    fn note_received(&self) {
        let retained = self
            .retention
            .lock()
            .expect("report retention mutex poisoned")
            .pop_front()
            .expect("received report has queued retention");
        let pending = self.pending.fetch_sub(1, Ordering::AcqRel);
        debug_assert!(pending > 0, "received report was not accounted as pending");
        if let Some(shared) = self.shared.upgrade() {
            let queued = shared.queued_reports.fetch_sub(1, Ordering::AcqRel);
            debug_assert!(queued > 0, "received report was not globally accounted");
            let payload = shared
                .queued_report_payload_bytes
                .fetch_sub(retained.payload_bytes, Ordering::AcqRel);
            debug_assert!(
                payload >= retained.payload_bytes,
                "report payload accounting underflow"
            );
        }
    }
}

impl Drop for ReportSubscription {
    fn drop(&mut self) {
        if let Some(shared) = self.shared.upgrade() {
            // Match publish/stats lock order. Removing the subscriber while
            // holding this mutex waits for any in-flight publication and
            // prevents a later one from enqueueing after we drain its exact
            // retention ledger.
            let mut subscribers = shared
                .subscribers
                .lock()
                .expect("subscriber mutex poisoned");
            subscribers.remove(&self.id);
            // Drop the channel's queued report objects before removing their
            // mirrored retention ledger. Concurrent stats may briefly
            // over-report queued coordinates, but can never claim zero while reports still
            // retain immutable database roots.
            drop(self.receiver.take());
            let abandoned_payload_bytes = {
                let mut retention = self
                    .retention
                    .lock()
                    .expect("report retention mutex poisoned");
                let bytes = retention.iter().fold(0_u64, |bytes, retained| {
                    bytes.saturating_add(retained.payload_bytes)
                });
                retention.clear();
                bytes
            };
            let abandoned = self.pending.swap(0, Ordering::AcqRel);
            if abandoned > 0 {
                let queued = shared.queued_reports.fetch_sub(abandoned, Ordering::AcqRel);
                debug_assert!(queued >= abandoned, "report queue accounting underflow");
                let payload = shared
                    .queued_report_payload_bytes
                    .fetch_sub(abandoned_payload_bytes, Ordering::AcqRel);
                debug_assert!(
                    payload >= abandoned_payload_bytes,
                    "report payload accounting underflow"
                );
            }
        } else {
            drop(self.receiver.take());
            self.pending.store(0, Ordering::Release);
            self.retention
                .lock()
                .expect("report retention mutex poisoned")
                .clear();
        }
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct OperationalServiceStats {
    pub accepting: bool,
    pub queued: usize,
    pub max_queued: usize,
    pub processed: u64,
    pub rejected_full: u64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct ServiceStats {
    pub queued: usize,
    pub max_queued: usize,
    pub processed: u64,
    pub rejected_full: u64,
    pub subscribers: usize,
    /// Lossless reports currently retaining two immutable database values
    /// across all subscribers.
    pub queued_reports: usize,
    /// High-water mark for `queued_reports` during this service lifetime.
    pub max_queued_reports: usize,
    /// Stable owned report payload currently retained across subscriber
    /// queues. Shared immutable tree/cache memory is not multiplied here.
    pub queued_report_payload_bytes: u64,
    /// High-water mark for `queued_report_payload_bytes`.
    pub max_queued_report_payload_bytes: u64,
    /// Oldest committed basis still awaiting delivery to any subscriber.
    pub oldest_queued_report_basis_t: Option<u64>,
    /// Distinct durable roots referenced by queued db-before/db-after values,
    /// rather than the number of report clones. This does not register readers
    /// or control storage retention.
    pub distinct_report_roots: usize,
    /// Distinct authoritative log generations referenced by queued reports.
    pub distinct_report_generations: usize,
}

pub struct TransactionService {
    client: TransactionClient,
    recovery_stats: RecoveryStats,
    worker: Option<JoinHandle<()>>,
    index_worker: Option<JoinHandle<()>>,
}

/// Deployment/resource settings shared by direct activation and standby
/// takeover. None of these settings alter the identity of an admitted request.
#[derive(Clone, Debug)]
pub struct ServiceOptions {
    /// CPU edit-preparation lanes for incremental indexing; SQL stays serial.
    pub index_preparation_parallelism: usize,
    /// Search build admission shared by background indexing and excision.
    pub fulltext_build_limits: crate::FulltextBuildLimits,
    /// Advisory prefix-read lanes, independent of transaction authority.
    pub hint_prefetch: crate::HintPrefetchConcurrency,
    pub indexing: BackgroundIndexingConfig,
    pub execution: crate::TransactionExecutionOptions,
    pub excision: crate::ExcisionConfig,
    /// Optional bounded nonblocking diagnostics publication. The emitter owns
    /// its sink worker; transaction execution never invokes sink callbacks.
    pub telemetry: Option<crate::TelemetryEmitter>,
}

impl Default for ServiceOptions {
    fn default() -> Self {
        Self {
            index_preparation_parallelism: 1,
            fulltext_build_limits: crate::FulltextBuildLimits::default(),
            hint_prefetch: crate::HintPrefetchConcurrency::default(),
            indexing: BackgroundIndexingConfig::default(),
            execution: crate::TransactionExecutionOptions::default(),
            excision: crate::ExcisionConfig::default(),
            telemetry: None,
        }
    }
}

/// A local observation of a one-shot leadership contender. This is not a lease
/// check: after taking the service, use `TransactionClient::is_available` and
/// the application's listener state to decide whether it is write-ready.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum StandbyStatus {
    Waiting,
    Activating,
    /// Activation completed and its service is waiting to be taken.
    Ready,
    Failed,
    /// Cancellation was requested; shutdown still owns joining and cleanup.
    Stopping,
    Stopped,
    /// Ownership was transferred to the caller; stopping this contender does
    /// not stop that independently owned service.
    Transferred,
}

pub struct TransactionStandby {
    receiver: mpsc::Receiver<Result<TransactionService, SemanticError>>,
    stop: Arc<AtomicBool>,
    status: Arc<Mutex<StandbyStatus>>,
    failure: Option<SemanticError>,
    worker: Option<JoinHandle<()>>,
}

fn resolve_service_database_name(
    connection: &PostgresConnectionConfig,
    name: &str,
) -> Result<crate::storage::BlockDatabase, SemanticError> {
    crate::storage::BlockDatabase::resolve(connection, name)
}

impl TransactionStandby {
    pub fn start(
        config: TransactionServiceConfig,
        poll_interval: Duration,
    ) -> Result<Self, SemanticError> {
        Self::start_with_indexing_and_execution_options(
            config,
            BackgroundIndexingConfig::default(),
            crate::TransactionExecutionOptions::default(),
            poll_interval,
        )
    }

    /// Contend for leadership using the same deployment and resource settings
    /// on every attempt. The public name is resolved once before spawning;
    /// rename or name reuse never redirects an already waiting contender.
    ///
    /// Polling is interruptible. Synchronous PostgreSQL activation and final
    /// service cleanup are not preempted; their configured I/O limits still
    /// apply when `shutdown`, `await_active`, or Drop joins the worker.
    pub fn start_with_indexing_and_execution_options(
        config: TransactionServiceConfig,
        indexing_config: BackgroundIndexingConfig,
        options: crate::TransactionExecutionOptions,
        poll_interval: Duration,
    ) -> Result<Self, SemanticError> {
        Self::start_with_options(
            config,
            ServiceOptions {
                indexing: indexing_config,
                execution: options,
                ..Default::default()
            },
            poll_interval,
        )
    }

    pub fn start_with_options(
        config: TransactionServiceConfig,
        options: ServiceOptions,
        poll_interval: Duration,
    ) -> Result<Self, SemanticError> {
        TransactionService::validate_config(&config)?;
        options.indexing.validate()?;
        options.excision.validate()?;
        options.fulltext_build_limits.validate()?;
        options.hint_prefetch.validate()?;
        crate::tree_read::validate_index_preparation_parallelism(
            options.index_preparation_parallelism,
        )?;
        if poll_interval.is_zero() {
            return Err(SemanticError::incorrect(
                "service/standby-poll",
                "standby poll interval must be positive",
            ));
        }
        // Resolve before spawning, not on each lease attempt. A rename or name
        // reuse while waiting must never redirect this standby to another DB.
        let database = resolve_service_database_name(&config.connection, &config.database_id)?;
        let stop = Arc::new(AtomicBool::new(false));
        let worker_stop = stop.clone();
        let status = Arc::new(Mutex::new(StandbyStatus::Activating));
        let worker_status = Arc::clone(&status);
        let (sender, receiver) = mpsc::sync_channel(1);
        let worker = thread::Builder::new()
            .name(format!("atomic-standby-{}", config.holder_id))
            .spawn(move || {
                loop {
                    {
                        let mut status = worker_status.lock().expect("standby status poisoned");
                        if worker_stop.load(Ordering::Acquire) {
                            *status = StandbyStatus::Stopped;
                            return;
                        }
                        *status = StandbyStatus::Activating;
                    }
                    let result = TransactionService::start_identity_with_options(
                        config.clone(),
                        options.clone(),
                        database.clone(),
                    );
                    match result {
                        Err(error)
                            if error.code == "storage/writer-active"
                                || error.code == "storage/writer-claim-conflict"
                                || block_service::connection_unavailable(&error) =>
                        {
                            {
                                let mut status =
                                    worker_status.lock().expect("standby status poisoned");
                                if worker_stop.load(Ordering::Acquire) {
                                    *status = StandbyStatus::Stopped;
                                    return;
                                }
                                *status = StandbyStatus::Waiting;
                            }
                            // unpark carries a token, so cancellation between
                            // the stop check and parking cannot be lost.
                            thread::park_timeout(poll_interval);
                        }
                        result => {
                            let mut status = worker_status.lock().expect("standby status poisoned");
                            if worker_stop.load(Ordering::Acquire) {
                                drop(status);
                                // A successful activation raced cancellation.
                                // Release its lease/workers before reporting
                                // stopped, never leaving an unclaimed writer.
                                drop(result);
                                *worker_status.lock().expect("standby status poisoned") =
                                    StandbyStatus::Stopped;
                                return;
                            }
                            *status = if result.is_ok() {
                                StandbyStatus::Ready
                            } else {
                                StandbyStatus::Failed
                            };
                            // This one-shot, capacity-one channel cannot fill.
                            // Hold only the status lock across publication so
                            // try_active cannot race a later Ready update.
                            let sent = sender.send(result);
                            drop(status);
                            drop(sent);
                            return;
                        }
                    }
                }
            })
            .map_err(|error| {
                SemanticError::new(
                    ErrorCategory::Unavailable,
                    "service/standby-spawn",
                    error.to_string(),
                )
            })?;
        Ok(Self {
            receiver,
            stop,
            status,
            failure: None,
            worker: Some(worker),
        })
    }

    /// Observe the contender without consuming its result or performing I/O.
    pub fn status(&self) -> StandbyStatus {
        let mut status = self.status.lock().expect("standby status poisoned");
        if matches!(*status, StandbyStatus::Waiting | StandbyStatus::Activating)
            && self
                .worker
                .as_ref()
                .is_some_and(|worker| worker.is_finished())
        {
            // A panicking worker has disconnected its result channel. Do not
            // advertise an indefinitely healthy contender in that case.
            *status = StandbyStatus::Failed;
        }
        *status
    }

    /// Take a completed activation, or return None immediately while waiting.
    /// Does not wait for PostgreSQL, sleep, or join a worker. A terminal error
    /// remains observable on subsequent polls; a successful service transfers
    /// exactly once. Stopping this object never stops a transferred service.
    pub fn try_active(&mut self) -> Result<Option<TransactionService>, SemanticError> {
        let mut status = self.status.lock().expect("standby status poisoned");
        if *status == StandbyStatus::Transferred {
            return Err(SemanticError::incorrect(
                "service/standby-consumed",
                "standby activation was already transferred",
            ));
        }
        if self.stop.load(Ordering::Acquire) {
            return Err(standby_stopped());
        }
        if let Some(error) = &self.failure {
            return Err(error.clone());
        }
        match self.receiver.try_recv() {
            Ok(Ok(service)) => {
                *status = StandbyStatus::Transferred;
                Ok(Some(service))
            }
            Ok(Err(error)) => {
                *status = StandbyStatus::Failed;
                self.failure = Some(error.clone());
                Err(error)
            }
            Err(mpsc::TryRecvError::Empty) => Ok(None),
            Err(mpsc::TryRecvError::Disconnected) => {
                let error = standby_stopped();
                *status = StandbyStatus::Failed;
                self.failure = Some(error.clone());
                Err(error)
            }
        }
    }

    /// Request cancellation and wake idle polling. This does not wait for an
    /// in-flight activation or release a service already buffered for handoff;
    /// call `shutdown` (or drop this object) to join and complete cleanup.
    pub fn request_stop(&self) {
        let mut status = self.status.lock().expect("standby status poisoned");
        self.stop.store(true, Ordering::Release);
        if !matches!(*status, StandbyStatus::Transferred | StandbyStatus::Stopped) {
            *status = StandbyStatus::Stopping;
        }
        if let Some(worker) = &self.worker {
            worker.thread().unpark();
        }
    }

    pub fn shutdown(mut self) {
        self.stop_and_join();
    }

    pub fn await_active(mut self, timeout: Duration) -> Result<TransactionService, SemanticError> {
        if *self.status.lock().expect("standby status poisoned") == StandbyStatus::Transferred {
            return Err(SemanticError::incorrect(
                "service/standby-consumed",
                "standby activation was already transferred",
            ));
        }
        if self.stop.load(Ordering::Acquire) {
            return Err(standby_stopped());
        }
        if let Some(error) = self.failure.take() {
            return Err(error);
        }
        let result = match self.receiver.recv_timeout(timeout) {
            Ok(result) => result,
            Err(mpsc::RecvTimeoutError::Timeout) => Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "service/standby-timeout",
                "standby did not acquire leadership before the deadline",
            )),
            Err(mpsc::RecvTimeoutError::Disconnected) => Err(standby_stopped()),
        };
        if result.is_ok() {
            *self.status.lock().expect("standby status poisoned") = StandbyStatus::Transferred;
        }
        self.stop_and_join();
        result
    }

    fn stop_and_join(&mut self) {
        self.request_stop();
        if let Some(worker) = self.worker.take() {
            let _ = worker.join();
        }
        // Dropping an unclaimed activation runs ordinary service shutdown,
        // including lease release. There is at most one buffered result.
        drop(self.receiver.try_recv());
        let mut status = self.status.lock().expect("standby status poisoned");
        if *status != StandbyStatus::Transferred {
            *status = StandbyStatus::Stopped;
        }
    }
}

fn standby_stopped() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "service/standby-stopped",
        "standby stopped before acquiring leadership",
    )
}

impl Drop for TransactionStandby {
    fn drop(&mut self) {
        self.stop_and_join();
    }
}

impl TransactionService {
    /// Start a compiled Rust transactor with an immutable native deployment
    /// registry. Accepted retries do not consult this execution configuration.
    pub fn start_with_execution_options(
        config: TransactionServiceConfig,
        options: crate::TransactionExecutionOptions,
    ) -> Result<Self, SemanticError> {
        Self::start_with_indexing_and_execution_options(
            config,
            BackgroundIndexingConfig::default(),
            options,
        )
    }

    pub fn start_with_defaults(
        config: TransactionServiceConfig,
        defaults: crate::TransactionDefaults,
    ) -> Result<Self, SemanticError> {
        Self::start_with_indexing_and_defaults(
            config,
            BackgroundIndexingConfig::default(),
            defaults,
        )
    }

    /// Start every service-owned connection with the configuration's transport policy.
    pub fn start(config: TransactionServiceConfig) -> Result<Self, SemanticError> {
        Self::start_with_indexing(config, BackgroundIndexingConfig::default())
    }
    pub fn start_with_indexing(
        config: TransactionServiceConfig,
        indexing_config: BackgroundIndexingConfig,
    ) -> Result<Self, SemanticError> {
        Self::start_with_indexing_and_defaults(
            config,
            indexing_config,
            crate::TransactionDefaults::default(),
        )
    }

    /// Start a writer with explicit fresh-transaction allocation policy. The
    /// policy is not transaction data and never changes a stored retry result.
    pub fn start_with_indexing_and_defaults(
        config: TransactionServiceConfig,
        indexing_config: BackgroundIndexingConfig,
        defaults: crate::TransactionDefaults,
    ) -> Result<Self, SemanticError> {
        Self::start_with_indexing_and_execution_options(
            config,
            indexing_config,
            crate::TransactionExecutionOptions {
                defaults,
                native: crate::NativeRegistry::default(),
            },
        )
    }

    pub fn start_with_indexing_and_execution_options(
        config: TransactionServiceConfig,
        indexing_config: BackgroundIndexingConfig,
        options: crate::TransactionExecutionOptions,
    ) -> Result<Self, SemanticError> {
        Self::start_with_options(
            config,
            ServiceOptions {
                indexing: indexing_config,
                execution: options,
                ..Default::default()
            },
        )
    }

    pub fn start_with_options(
        config: TransactionServiceConfig,
        options: ServiceOptions,
    ) -> Result<Self, SemanticError> {
        Self::validate_config(&config)?;
        options.indexing.validate()?;
        options.excision.validate()?;
        options.fulltext_build_limits.validate()?;
        options.hint_prefetch.validate()?;
        crate::tree_read::validate_index_preparation_parallelism(
            options.index_preparation_parallelism,
        )?;
        let database = resolve_service_database_name(&config.connection, &config.database_id)?;
        Self::start_identity_with_options(config, options, database)
    }

    fn validate_config(config: &TransactionServiceConfig) -> Result<(), SemanticError> {
        if config.database_id.is_empty()
            || config.queue_capacity == 0
            || config.lease_duration.is_zero()
            || config.renew_interval.is_zero()
            || config.renew_interval >= config.lease_duration
        {
            return Err(SemanticError::incorrect(
                "service/invalid-config",
                "database id and queue capacity are required and renewal must precede lease expiry",
            ));
        }
        Ok(())
    }

    // Only callers that already captured an immutable block identity may enter.
    // BlockTransactor acquires authority through guarded opaque references.
    fn start_identity_with_options(
        config: TransactionServiceConfig,
        options: ServiceOptions,
        database: crate::storage::BlockDatabase,
    ) -> Result<Self, SemanticError> {
        block_service::start(config, options, database)
    }

    pub fn client(&self) -> TransactionClient {
        self.client.clone()
    }

    /// Stable catalog identity retained by this service activation.
    pub fn identity(&self) -> DatabaseIdentity {
        self.client.identity()
    }

    pub fn recovery_stats(&self) -> RecoveryStats {
        self.recovery_stats
    }

    /// Cumulative decoded-program work in the actual writer's bounded cache.
    pub fn program_cache_stats(&self) -> ProgramCacheStats {
        crate::program_cache::stats(&self.client.shared.program_cache)
    }

    pub fn background_indexing_stats(&self) -> BackgroundIndexingStats {
        self.client.background_indexing_stats()
    }

    pub fn writer_residency_stats(&self) -> WriterResidencyStats {
        self.client.writer_residency_stats()
    }

    pub fn shutdown(mut self) {
        self.stop_and_join();
    }

    fn stop_and_join(&mut self) {
        self.client.shared.accepting.store(false, Ordering::Release);
        if let Some(worker) = self.worker.take() {
            let _ = worker.join();
        }
        self.client.shared.indexing.shutdown();
        if let Some(worker) = self.index_worker.take() {
            let _ = worker.join();
        }
    }
}

impl Drop for TransactionService {
    fn drop(&mut self) {
        self.stop_and_join();
    }
}

fn record_transaction_diagnostics(
    shared: &Shared,
    report: &mut ServiceTransactionReport,
    context: &OperationContext,
) {
    if !context.diagnostics_enabled() {
        return;
    }
    let diagnostics = Arc::new(crate::TransactionDiagnostics {
        basis_t: report.basis_t,
        replayed: report.replayed,
        report: context.report(),
    });
    if let Some(emitter) = &shared.telemetry {
        let identity = DatabaseIdentity::new(shared.database_id.clone(), shared.lineage_id.clone());
        emitter.publish_transaction(&identity, &diagnostics);
    }
    report.diagnostics = Some(diagnostics);
}

fn drain_unavailable(receiver: &mpsc::Receiver<Work>, shared: &Shared) {
    while let Ok(work) = receiver.try_recv() {
        decrement_queued(shared);
        let _ = work.response.send(Err(shared.unavailable()));
    }
}

fn decrement_queued(shared: &Shared) {
    let _admission = shared.admission.lock().expect("admission mutex poisoned");
    shared.queued.fetch_sub(1, Ordering::AcqRel);
}

fn note_report_commit(
    shared: &Shared,
    database_id: &str,
    report: &ServiceTransactionReport,
) -> Result<(), SemanticError> {
    if report
        .tx_data
        .iter()
        .any(|d| d.attribute == crate::DB_EXCISE as u32)
    {
        shared
            .indexing
            .excision_requested
            .store(true, Ordering::Release);
    }
    let transaction = DurableTransaction {
        database_id: database_id.to_owned(),
        basis_t: report.basis_t,
        // The hash bytes have fixed width in the canonical envelope and do
        // not affect retained size. The exact predecessor is authenticated
        // by the writer before this accounting-only reconstruction.
        previous_hash: [0; 32],
        eidx_frontier: report.db_after.eidx_frontier(),
        // Opaque log entries contain datoms/allocation checkpoints, while
        // caller tempid names live in the separate receipt tree.
        tempids: BTreeMap::new(),
        tx_data: report.tx_data.clone(),
    };
    let retained = crate::recent::retained_entry_stats(&transaction)?;
    let changes_avet_membership = report.tx_data.iter().any(|datom| {
        matches!(
            u64::from(datom.attribute),
            crate::DB_INDEX | crate::DB_UNIQUE
        )
    });
    shared.indexing.note_commit(
        Novelty {
            basis_t: report.basis_t,
            datoms: retained.datoms,
            bytes: retained.accounted_bytes,
        },
        changes_avet_membership,
    );
    Ok(())
}

fn unknown_outcome(request_key_hash: Digest, message: &str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::UnknownOutcome,
        "service/unknown-outcome",
        message,
    )
    .detail("request_key_hash", hex_digest(&request_key_hash))
}

fn hex_digest(digest: &Digest) -> String {
    use std::fmt::Write;

    digest
        .iter()
        .fold(String::with_capacity(64), |mut hex, byte| {
            write!(hex, "{byte:02x}").expect("writing to String cannot fail");
            hex
        })
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, EntityRef, Keyword, Schema, TxValue, USER_PARTITION, Value,
        ValueType, make_eid,
    };
    use std::time::{SystemTime, UNIX_EPOCH};

    const OBSERVED_ITEM_COUNT: u32 = 1_000;

    fn postgres_connection() -> Option<String> {
        std::env::var("ATOMIC_POSTGRES_URL").ok()
    }

    struct ObservationFixture {
        admin: postgres::Client,
        schema: String,
        connection: String,
        database: crate::storage::BlockDatabase,
    }

    impl Drop for ObservationFixture {
        fn drop(&mut self) {
            let _ = self
                .admin
                .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
        }
    }

    fn observation_fixture() -> Option<ObservationFixture> {
        let connection = postgres_connection()?;
        let schema = format!("block_service_observe_{:032x}", crate::uuid_v7().unwrap());
        let mut admin = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
        admin
            .batch_execute(&format!("CREATE SCHEMA {schema}"))
            .unwrap();
        let connection =
            if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
                format!(
                    "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
                    if connection.contains('?') { '&' } else { '?' }
                )
            } else {
                format!("{connection} options='-csearch_path={schema},pg_catalog'")
            };
        let config = PostgresConnectionConfig::plaintext(&connection);
        crate::storage::PgBlockStore::install(&config).unwrap();
        let database =
            crate::storage::BlockDatabase::create(&config, "observed", observation_schema())
                .unwrap();
        Some(ObservationFixture {
            admin,
            schema,
            connection,
            database,
        })
    }

    fn unique_database(prefix: &str) -> String {
        format!(
            "{prefix}_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .expect("system clock precedes Unix epoch")
                .as_nanos()
        )
    }

    fn observation_schema() -> Schema {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                OBSERVED_ITEM_COUNT,
                Keyword::new("item", "count"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        schema
    }

    fn observation_request(key: &str, value: i64) -> TransactionRequest {
        TransactionRequest::new(
            key,
            vec![TxOp::Add {
                entity: EntityRef::Id(make_eid(USER_PARTITION, 42).unwrap()),
                attribute: OBSERVED_ITEM_COUNT,
                value: TxValue::Scalar(Value::Long(value)),
            }],
        )
    }

    fn observation_service_config(
        connection: &str,
        database_id: String,
    ) -> TransactionServiceConfig {
        TransactionServiceConfig {
            connection: PostgresConnectionConfig::parse(connection).unwrap(),
            database_id,
            holder_id: unique_database("unknown-observer"),
            lease_duration: Duration::from_secs(10),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 4,
            capacity_limits: CapacityLimits::default(),
        }
    }

    fn test_config() -> BackgroundIndexingConfig {
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1_024,
            memory_index_max_bytes: 2_048,
        }
    }

    fn bookkeeping_seed(width: usize) -> IndexingSeed {
        IndexingSeed {
            published_revision: 7,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 7,
            target_basis_t: width as u64 + 1,
            pending: (0..width)
                .map(|offset| Novelty {
                    basis_t: offset as u64 + 2,
                    datoms: offset as u64 % 3 + 1,
                    bytes: offset as u64 % 7 + 17,
                })
                .collect(),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        }
    }

    fn bookkeeping_config() -> BackgroundIndexingConfig {
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: u64::MAX - 1,
            memory_index_max_bytes: u64::MAX,
        }
    }

    fn assert_backlog_oracle(indexing: &BackgroundIndexing) {
        // This deliberately independent full scan is test-only and outside
        // measured operations. Never consult any maintained aggregate here.
        let (total, frozen, revision, basis, through) = {
            let backlog = indexing.backlog.lock().unwrap();
            let mut total = [0_u128; 3];
            let mut frozen = [0_u128; 3];
            for novelty in &backlog.pending {
                let counts = [1, u128::from(novelty.datoms), u128::from(novelty.bytes)];
                for column in 0..3 {
                    total[column] += counts[column];
                    if backlog
                        .indexing_through
                        .is_some_and(|through| novelty.basis_t <= through)
                    {
                        frozen[column] += counts[column];
                    }
                }
            }
            (
                total,
                frozen,
                backlog.published_revision,
                backlog.published_basis_t,
                backlog.indexing_through,
            )
        };
        let saturated = |value: u128| value.min(u128::from(u64::MAX)) as u64;
        let stats = indexing.stats();
        assert_eq!(
            [
                stats.total_transactions,
                stats.total_datoms,
                stats.total_bytes
            ],
            total.map(saturated)
        );
        assert_eq!(
            [
                stats.indexing_transactions,
                stats.indexing_datoms,
                stats.indexing_bytes
            ],
            frozen.map(saturated)
        );
        assert_eq!(
            [
                stats.memory_index_transactions,
                stats.memory_index_datoms,
                stats.memory_index_bytes
            ],
            std::array::from_fn(|column| saturated(total[column] - frozen[column]))
        );
        assert_eq!(stats.published_revision, revision);
        assert_eq!(indexing.published_revision(), revision);
        assert_eq!(stats.published_basis_t, basis);
        assert_eq!(stats.job_in_flight, through.is_some());
        assert_eq!(stats.last_failure.is_some(), indexing.has_failure());
    }

    #[test]
    fn backlog_aggregates_match_oracle_across_failure_retry_and_partial_adoption() {
        for width in [1, 32, 512, 8_192] {
            let (sender, _receiver) = mpsc::sync_channel(1);
            let indexing =
                BackgroundIndexing::new(bookkeeping_config(), bookkeeping_seed(width), sender);
            assert_backlog_oracle(&indexing);
            let target = width as u64 + 1;
            assert_eq!(indexing.request_index().target_t, target);
            assert_eq!(indexing.begin_job(), Some(target));
            assert_eq!(indexing.begin_job(), None);
            assert_backlog_oracle(&indexing);
            let frozen = indexing.stats();
            assert_eq!(frozen.indexing_transactions, width as u64);
            assert_eq!(frozen.memory_index_transactions, 0);

            indexing.note_commit(
                Novelty {
                    basis_t: target,
                    datoms: 100,
                    bytes: 999,
                },
                true,
            );
            assert_eq!(
                indexing.stats(),
                frozen,
                "duplicate receipt changed accounting"
            );
            indexing.note_commit(
                Novelty {
                    basis_t: target + 1,
                    datoms: 3,
                    bytes: 71,
                },
                true,
            );
            assert_backlog_oracle(&indexing);
            assert_eq!(indexing.stats().memory_index_transactions, 1);

            let partial = 1 + width as u64 / 2;
            indexing.complete_job(8, partial, 1, true);
            assert_backlog_oracle(&indexing);
            assert_eq!(indexing.stats().pending_avet_projections, 1);
            assert_eq!(indexing.begin_job(), Some(partial));
            assert_eq!(
                indexing.stats().indexing_transactions,
                0,
                "maintenance froze an unpublished suffix"
            );
            indexing.note_commit(
                Novelty {
                    basis_t: target + 2,
                    datoms: 2,
                    bytes: 41,
                },
                false,
            );
            assert_backlog_oracle(&indexing);
            indexing.complete_job(9, partial, 0, false);
            assert_backlog_oracle(&indexing);
            assert!(
                indexing.should_continue(),
                "older maintenance lost new publication demand"
            );

            assert_eq!(indexing.begin_job(), Some(target + 2));
            let before_retry = indexing.stats();
            indexing.retry_job();
            assert_backlog_oracle(&indexing);
            assert_eq!(indexing.begin_job(), Some(target + 2));
            let after_retry = indexing.stats();
            assert_eq!(
                (
                    after_retry.indexing_transactions,
                    after_retry.indexing_datoms,
                    after_retry.indexing_bytes
                ),
                (
                    before_retry.indexing_transactions,
                    before_retry.indexing_datoms,
                    before_retry.indexing_bytes
                ),
                "whole-job retry lost frozen novelty"
            );
            assert_eq!(after_retry.total_bytes, before_retry.total_bytes);
            assert_eq!(after_retry.jobs_started, before_retry.jobs_started + 1);
            indexing.fail_job(SemanticError::new(
                ErrorCategory::Fault,
                "index/test-failure",
                "terminal failure",
            ));
            assert_backlog_oracle(&indexing);
            assert_eq!(indexing.stats().indexing_transactions, 0);
            assert_eq!(indexing.stats().jobs_failed, 1);
            assert!(indexing.has_failure());
            assert_eq!(
                indexing.limiting_error().unwrap().code,
                "service/indexing-failed"
            );

            // Fatal indexing failure still closes the writer. Reconstructing
            // from authenticated startup state, not a stats read, clears it.
            let seed = {
                let backlog = indexing.backlog.lock().unwrap();
                IndexingSeed {
                    published_revision: backlog.published_revision,
                    published_basis_t: backlog.published_basis_t,
                    pending_avet_projections: backlog.pending_avet_projections,
                    newest_observed_revision: backlog.newest_observed_revision,
                    target_basis_t: backlog.target_basis_t,
                    pending: backlog.pending.clone(),
                    publication_work_through: backlog.publication_work_through,
                    required_publication_t: backlog.required_publication_t,
                    needs_publication: backlog.needs_publication,
                }
            };
            let (sender, _receiver) = mpsc::sync_channel(1);
            let restarted = BackgroundIndexing::new(bookkeeping_config(), seed, sender);
            assert!(!restarted.has_failure());
            assert_eq!(restarted.begin_job(), Some(target + 2));
            assert_backlog_oracle(&restarted);
            restarted.complete_job(10, target + 2, 0, false);
            assert_backlog_oracle(&restarted);
            assert_eq!(restarted.stats().total_transactions, 0);
            assert!(!restarted.should_continue());
            restarted.complete_job(9, partial, 0, false);
            assert_eq!(
                restarted.published_revision(),
                10,
                "adoption revision regressed"
            );
            assert_backlog_oracle(&restarted);
        }
    }

    #[test]
    fn saturated_public_backlog_counts_recover_exact_suffix_after_adoption() {
        let (sender, _receiver) = mpsc::sync_channel(1);
        let mut seed = bookkeeping_seed(2);
        seed.pending[0].datoms = u64::MAX;
        seed.pending[0].bytes = u64::MAX;
        seed.pending[1].datoms = 7;
        seed.pending[1].bytes = 7;
        let indexing = BackgroundIndexing::new(bookkeeping_config(), seed, sender);
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.stats().total_datoms, u64::MAX);
        assert!(indexing.at_hard_limit());
        assert_eq!(indexing.begin_job(), Some(3));
        indexing.note_commit(
            Novelty {
                basis_t: 4,
                datoms: 9,
                bytes: 9,
            },
            false,
        );
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.stats().memory_index_datoms, 9);
        indexing.complete_job(8, 2, 0, false);
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.stats().total_datoms, 16);
        assert_eq!(indexing.stats().total_bytes, 16);
        assert!(!indexing.at_hard_limit());
    }

    #[test]
    fn fixed_append_revision_availability_and_status_work_does_not_scan_backlog() {
        use std::hint::black_box;
        const APPENDS: u64 = 256;
        for width in [32, 128, 512, 2_048, 8_192, 32_768] {
            let (sender, _receiver) = mpsc::sync_channel(1);
            let indexing =
                BackgroundIndexing::new(bookkeeping_config(), bookkeeping_seed(width), sender);
            indexing.request_index();
            let through = indexing.begin_job().unwrap();
            assert_backlog_oracle(&indexing);
            let started = Instant::now();
            for offset in 1..=APPENDS {
                indexing.note_commit(
                    Novelty {
                        basis_t: through + offset,
                        datoms: 1,
                        bytes: 64,
                    },
                    false,
                );
                black_box(indexing.published_revision());
                black_box(indexing.has_failure());
                black_box(indexing.at_hard_limit());
                black_box(indexing.limiting_error());
                // Include the complete public aggregate snapshot and drop,
                // not only insertion or the dedicated revision getter.
                black_box(indexing.stats());
            }
            let append_elapsed = started.elapsed();
            assert_backlog_oracle(&indexing);
            assert_eq!(indexing.stats().indexing_transactions, width as u64);
            assert_eq!(indexing.stats().memory_index_transactions, APPENDS);
            let adoption_started = Instant::now();
            indexing.complete_job(8, through, 0, false);
            let adoption_elapsed = adoption_started.elapsed();
            assert_backlog_oracle(&indexing);
            assert_eq!(indexing.stats().total_transactions, APPENDS);
            eprintln!(
                "SERVICE_BOOKKEEPING retained={width} fixed_appends={APPENDS} append_revision_availability_stats_drop_ns_per_op={} adoption_removed={width} adoption_us={}",
                append_elapsed.as_nanos() / u128::from(APPENDS),
                adoption_elapsed.as_micros()
            );
        }
    }

    #[test]
    fn projection_observations_follow_attempt_retry_and_adoption_without_idle_policy() {
        let now = Instant::now();
        let error =
            SemanticError::conflict("storage/write-protection-conflict", "GC guard changed");
        let mut observation = ProjectionObservations::default();
        observation.started(3);
        observation.failed(&error, Some(now + Duration::from_millis(50)), false);
        let failed = observation.snapshot_with_messages(now, true);
        assert_eq!(
            (failed.attempts, failed.failures, failed.idle_retries),
            (1, 1, 0)
        );
        assert_eq!(failed.attempted_basis_t, Some(3));
        assert_eq!(failed.checked_basis_t, None);
        assert_eq!(failed.retry_in, Some(Duration::from_millis(50)));
        assert_eq!(failed.last_failure.unwrap().code, error.code);
        assert!(!failed.retry_exhausted);

        observation.started(3);
        let retrying = observation.snapshot_with_messages(now, false);
        assert_eq!(retrying.attempts, 2);
        assert_eq!(retrying.checked_basis_t, None);
        assert_eq!(retrying.retry_in, None);
        assert!(retrying.last_failure.unwrap().message.is_empty());
        observation.adopted(3);
        let recovered = observation.snapshot_with_messages(now, true);
        assert_eq!(recovered.checked_basis_t, Some(3));
        assert_eq!(recovered.failures, 1);
        assert!(recovered.last_failure.is_none());

        observation.started(4);
        observation.failed(&error, None, true);
        let exhausted = observation.snapshot_with_messages(now, true);
        assert_eq!(exhausted.checked_basis_t, Some(3));
        assert_eq!(exhausted.attempted_basis_t, Some(4));
        assert_eq!(exhausted.idle_retries, 0);
        assert_eq!(exhausted.retry_in, None);
        assert!(exhausted.retry_exhausted);
        assert_eq!(exhausted.last_failure.unwrap().message, error.message);
    }

    #[test]
    fn satisfied_index_request_does_not_invent_an_idle_search_retry() {
        let (sender, receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                published_revision: 1,
                published_basis_t: 1,
                pending_avet_projections: 0,
                newest_observed_revision: 1,
                target_basis_t: 1,
                pending: VecDeque::new(),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: false,
            },
            sender,
        );
        let error = SemanticError::conflict("test/projection", "attempt failed");
        indexing.projection_started(1);
        indexing.projection_failed(&error, None, false);
        assert_eq!(
            indexing.request_index(),
            IndexRequest {
                target_t: 1,
                scheduled: false
            }
        );
        assert_eq!(receiver.try_recv(), Err(mpsc::TryRecvError::Empty));
        assert_eq!(indexing.stats().fulltext.retry_in, None);
    }

    #[test]
    fn explicit_index_requests_coalesce_and_do_not_chase_later_commits() {
        let (sender, receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                published_revision: 1,
                published_basis_t: 1,
                pending_avet_projections: 0,
                newest_observed_revision: 1,
                target_basis_t: 1,
                pending: VecDeque::new(),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: false,
            },
            sender,
        );
        assert_eq!(
            indexing.request_index(),
            IndexRequest {
                target_t: 1,
                scheduled: false
            }
        );
        indexing.note_commit(
            Novelty {
                basis_t: 2,
                datoms: 1,
                bytes: 100,
            },
            false,
        );
        assert!(!indexing.should_continue());
        let first = indexing.request_index();
        assert_eq!(
            first,
            IndexRequest {
                target_t: 2,
                scheduled: true
            }
        );
        assert_eq!(
            indexing.request_index(),
            IndexRequest {
                target_t: 2,
                scheduled: false
            }
        );
        assert_eq!(receiver.try_recv(), Ok(IndexCommand::Wake));
        assert_eq!(indexing.begin_job(), Some(2));
        indexing.note_commit(
            Novelty {
                basis_t: 3,
                datoms: 1,
                bytes: 100,
            },
            false,
        );
        indexing.complete_job(2, 2, 0, false);
        assert!(
            !indexing.should_continue(),
            "a finite request chased unrequested later novelty"
        );
        assert_eq!(first.target_t, 2);
        assert_eq!(
            indexing.request_index(),
            IndexRequest {
                target_t: 3,
                scheduled: true
            }
        );
        assert_eq!(indexing.begin_job(), Some(3));
        indexing.note_commit(
            Novelty {
                basis_t: 4,
                datoms: 1,
                bytes: 100,
            },
            false,
        );
        assert_eq!(
            indexing.request_index(),
            IndexRequest {
                target_t: 4,
                scheduled: true
            }
        );
        indexing.complete_job(3, 3, 0, false);
        assert!(
            indexing.should_continue(),
            "older completion lost a newer explicit request"
        );
        assert_eq!(indexing.begin_job(), Some(4));
        indexing.complete_job(4, 4, 0, false);
        assert!(!indexing.should_continue());
        assert_eq!(
            indexing.request_index(),
            IndexRequest {
                target_t: 4,
                scheduled: false
            }
        );
    }

    #[test]
    fn basis_zero_bootstrap_is_publishable_and_first_threshold_crossing_advances_it() {
        let (sender, receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                published_revision: 0,
                published_basis_t: 0,
                pending_avet_projections: 0,
                newest_observed_revision: 0,
                target_basis_t: 0,
                pending: VecDeque::new(),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: true,
            },
            sender,
        );

        assert!(indexing.should_continue());
        assert!(indexing.begin_job().is_some());
        indexing.complete_job(1, 0, 0, false);
        assert!(!indexing.should_continue());
        indexing.note_commit(
            Novelty {
                basis_t: 1,
                datoms: 1,
                bytes: 2_048,
            },
            false,
        );
        assert_eq!(receiver.try_recv(), Ok(IndexCommand::Wake));
        assert!(indexing.should_continue());
        assert!(indexing.begin_job().is_some());
        indexing.complete_job(2, 1, 0, false);
        assert!(!indexing.should_continue());
        assert_eq!(indexing.stats().published_basis_t, 1);
        assert_eq!(indexing.stats().published_revision, 2);
    }

    #[test]
    fn corrupt_publication_at_head_forces_same_basis_repair() {
        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                published_revision: 0,
                published_basis_t: 0,
                pending_avet_projections: 0,
                newest_observed_revision: 1,
                target_basis_t: 1,
                pending: VecDeque::from([Novelty {
                    basis_t: 1,
                    datoms: 1,
                    bytes: 1_024,
                }]),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: true,
            },
            sender,
        );

        assert!(indexing.should_continue());
        assert!(indexing.begin_job().is_some());
        indexing.complete_job(2, 1, 0, false);
        let repaired = indexing.stats();
        assert_eq!(repaired.published_revision, 2);
        assert_eq!(repaired.newest_observed_revision, 2);
        assert_eq!(repaired.published_basis_t, 1);
        assert_eq!(repaired.target_basis_t, 1);
        assert_eq!(repaired.total_bytes, 0);
        assert!(!indexing.should_continue());
    }

    #[test]
    fn index_thresholds_are_strict_crossings() {
        let config = test_config();
        let mut backlog = IndexingBacklog {
            published_revision: 1,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 2,
            pending: VecDeque::new(),
            total_datoms: 1,
            total_bytes: u128::from(config.memory_index_threshold_bytes),
            indexing_through: None,
            indexing_totals: IndexingTotals::default(),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        };
        assert!(!should_index(&backlog, config));
        backlog.total_bytes += 1;
        assert!(should_index(&backlog, config));

        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            config,
            IndexingSeed {
                published_revision: 1,
                published_basis_t: 1,
                pending_avet_projections: 0,
                newest_observed_revision: 1,
                target_basis_t: 2,
                pending: VecDeque::from([Novelty {
                    basis_t: 2,
                    datoms: 1,
                    bytes: config.memory_index_max_bytes,
                }]),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: false,
            },
            sender,
        );
        assert!(!indexing.at_hard_limit());
        assert!(indexing.limiting_error().is_none());
        {
            let mut backlog = indexing
                .backlog
                .lock()
                .expect("index backlog mutex poisoned");
            backlog.total_bytes += 1;
            backlog.pending.front_mut().unwrap().bytes += 1;
        }
        assert!(indexing.at_hard_limit());
        assert_eq!(
            indexing.limiting_error().unwrap().code,
            "service/index-backpressure"
        );
    }

    #[test]
    fn exact_recent_hard_cap_can_force_a_below_threshold_publication() {
        let (sender, receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                published_revision: 1,
                published_basis_t: 1,
                pending_avet_projections: 0,
                newest_observed_revision: 1,
                target_basis_t: 2,
                pending: VecDeque::from([Novelty {
                    basis_t: 2,
                    datoms: 1,
                    bytes: 1,
                }]),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: false,
            },
            sender,
        );

        assert!(!indexing.should_continue());
        assert!(indexing.force_publication());
        assert_eq!(receiver.try_recv(), Ok(IndexCommand::Wake));
        assert!(indexing.should_continue());
        assert!(indexing.begin_job().is_some());
        indexing.complete_job(2, 2, 0, false);
        assert_eq!(indexing.stats().total_bytes, 0);
        assert!(!indexing.should_continue());

        // With no durable tail to consolidate, repeating an intrinsically
        // oversized transaction must return its capacity error rather than
        // park forever on a publication that cannot make room.
        assert!(!indexing.force_publication());
    }

    #[test]
    fn physical_progress_does_not_clear_pending_or_newer_schema_work() {
        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                published_revision: 1,
                published_basis_t: 1,
                pending_avet_projections: 0,
                newest_observed_revision: 1,
                target_basis_t: 1,
                pending: VecDeque::new(),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: false,
            },
            sender,
        );
        indexing.note_commit(
            Novelty {
                basis_t: 2,
                datoms: 1,
                bytes: 1,
            },
            true,
        );
        assert!(indexing.begin_job().is_some());
        indexing.complete_job(2, 2, 1, true);
        let partial = indexing.stats();
        assert_eq!(partial.published_basis_t, 2);
        assert_eq!(
            partial.total_bytes, 0,
            "durable EAVT progress releases recent novelty"
        );
        assert_eq!(partial.pending_avet_projections, 1);
        assert!(indexing.should_continue());

        // A newer toggle racing the older same-basis chunks must survive the
        // older job's final completion signal.
        indexing.note_commit(
            Novelty {
                basis_t: 3,
                datoms: 1,
                bytes: 1,
            },
            true,
        );
        assert!(indexing.begin_job().is_some());
        indexing.complete_job(3, 2, 0, false);
        assert!(indexing.should_continue());

        assert!(indexing.begin_job().is_some());
        indexing.complete_job(4, 3, 0, false);
        let complete = indexing.stats();
        assert_eq!(complete.pending_avet_projections, 0);
        assert_eq!(complete.total_bytes, 0);
        assert!(!indexing.should_continue());
    }

    #[test]
    fn publication_maintenance_does_not_turn_small_new_tails_into_demand() {
        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                published_revision: 1,
                published_basis_t: 1,
                pending_avet_projections: 0,
                newest_observed_revision: 1,
                target_basis_t: 2,
                pending: VecDeque::from([Novelty {
                    basis_t: 2,
                    datoms: 1,
                    bytes: 1_025,
                }]),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: false,
            },
            sender,
        );
        assert_eq!(indexing.begin_job(), Some(2));
        indexing.complete_job(2, 2, 0, true);
        indexing.note_commit(
            Novelty {
                basis_t: 3,
                datoms: 1,
                bytes: 100,
            },
            false,
        );
        assert_eq!(
            indexing.begin_job(),
            Some(2),
            "finish only the published value"
        );
        indexing.complete_job(2, 2, 0, false);
        assert_eq!(indexing.stats().total_bytes, 100);
        assert_eq!(
            indexing.begin_job(),
            None,
            "new novelty is below the threshold"
        );
        indexing.note_commit(
            Novelty {
                basis_t: 4,
                datoms: 1,
                bytes: 925,
            },
            false,
        );
        assert_eq!(indexing.begin_job(), Some(4));
    }

    #[test]
    fn forced_demand_survives_an_older_publications_final_maintenance_receipt() {
        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                published_revision: 2,
                published_basis_t: 2,
                pending_avet_projections: 0,
                newest_observed_revision: 2,
                target_basis_t: 2,
                pending: VecDeque::new(),
                publication_work_through: Some(2),
                required_publication_t: 0,
                needs_publication: true,
            },
            sender,
        );
        assert_eq!(indexing.begin_job(), Some(2));
        indexing.note_commit(
            Novelty {
                basis_t: 3,
                datoms: 1,
                bytes: 100,
            },
            false,
        );
        assert!(indexing.force_publication());
        indexing.complete_job(2, 2, 0, false);
        assert_eq!(
            indexing.begin_job(),
            Some(3),
            "the newer force remains parked"
        );
        indexing.complete_job(3, 3, 0, true);
        assert_eq!(indexing.begin_job(), Some(3));
        indexing.complete_job(3, 3, 0, false);
        assert_eq!(indexing.begin_job(), None);
    }

    #[test]
    fn tuple_of_nils_cannot_evade_novelty_backpressure() {
        let datom = Datom {
            entity: 1,
            attribute: 2,
            value: Value::Tuple(vec![None; 1_024]),
            tx: crate::t_to_tx(2).unwrap(),
            added: true,
        };
        let transaction = DurableTransaction {
            database_id: "accounting-test".to_owned(),
            basis_t: 2,
            previous_hash: [0; 32],
            eidx_frontier: crate::INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::from([("large-envelope-tempid".repeat(32), 1)]),
            tx_data: vec![datom.clone()],
        };
        let canonical_value_bytes = crate::encoding::encode_canonical_value(&datom.value)
            .unwrap()
            .len() as u64;
        let locator_bytes = (size_of::<crate::recent_btset::RecentDatomRef>() as u64)
            .saturating_mul(MAX_RECENT_REFERENCES_PER_DATOM);
        let canonical_only_account = (size_of::<Datom>() as u64)
            .saturating_add(canonical_value_bytes)
            .saturating_add(locator_bytes);
        let retained = crate::recent::retained_entry_stats(&transaction).unwrap();
        let retained_account = retained.accounted_bytes;
        assert!(retained_account > canonical_only_account);

        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            BackgroundIndexingConfig {
                memory_index_threshold_bytes: 1,
                memory_index_max_bytes: canonical_only_account.saturating_add(1),
            },
            IndexingSeed {
                published_revision: 1,
                published_basis_t: 1,
                pending_avet_projections: 0,
                newest_observed_revision: 1,
                target_basis_t: 1,
                pending: VecDeque::new(),
                publication_work_through: None,
                required_publication_t: 0,
                needs_publication: false,
            },
            sender,
        );
        indexing.note_commit(
            Novelty {
                basis_t: 2,
                datoms: retained.datoms,
                bytes: retained.accounted_bytes,
            },
            false,
        );
        let limit = indexing
            .limiting_error()
            .expect("retained tuple slots must cross the hard byte limit");
        assert_eq!(limit.code, "service/index-backpressure");
        assert_eq!(indexing.stats().total_bytes, retained_account);
    }

    #[test]
    fn unified_projection_retry_reports_adopted_search_basis() {
        let Some(fixture) = observation_fixture() else {
            eprintln!("SKIP unified projection retry: ATOMIC_POSTGRES_URL is unset");
            return;
        };
        const TEXT: u32 = 1_001;
        let connection = PostgresConnectionConfig::plaintext(&fixture.connection);
        let mut schema = observation_schema();
        schema
            .install(
                Attribute::new(
                    TEXT,
                    Keyword::new("document", "body"),
                    ValueType::String,
                    Cardinality::One,
                )
                .fulltext(),
            )
            .unwrap();
        let database =
            crate::storage::BlockDatabase::create(&connection, "projection", schema).unwrap();
        let service = TransactionService::start_with_indexing(
            observation_service_config(&fixture.connection, "projection".into()),
            BackgroundIndexingConfig {
                memory_index_threshold_bytes: 1 << 30,
                memory_index_max_bytes: 2 << 30,
            },
        )
        .unwrap();
        let client = service.client();
        let report = client
            .transact(
                TransactionRequest::new(
                    "document",
                    vec![TxOp::Add {
                        entity: EntityRef::Temp("document".into()),
                        attribute: TEXT,
                        value: Value::String("needle coherent retry".into()).into(),
                    }],
                ),
                Duration::from_secs(10),
            )
            .unwrap();
        let identity = crate::storage::engine::identity_string(database.identity);
        let (first_ready, first_release) = block_service::pause_preparation(&identity);
        assert!(client.request_index().unwrap().scheduled);
        first_ready.recv_timeout(Duration::from_secs(10)).unwrap();
        let staged = client.background_indexing_stats();
        assert_eq!(staged.fulltext.attempts, 1);
        assert_eq!(staged.fulltext.attempted_basis_t, Some(report.basis_t));
        assert_eq!(
            staged.fulltext.checked_basis_t, None,
            "staging is not adoption"
        );
        assert_eq!(staged.fulltext.failures, 0);

        // This candidate really built the search attachment. Changing only its
        // generic GC guard makes the actual adoption fail without corrupting
        // objects or pretending the failure arose in the search builder.
        let (retry_ready, retry_release) = block_service::pause_preparation(&identity);
        let mut store = crate::storage::PgBlockStore::connect(&connection).unwrap();
        let before_adoption = store
            .read_ref(&database.reference_key())
            .unwrap()
            .unwrap()
            .value;
        let gc_key = crate::storage::engine::GC_REFERENCE;
        let previous = store.read_ref(gc_key).unwrap();
        assert!(matches!(
            store
                .compare_exchange(
                    gc_key,
                    previous.as_ref().map(|r| r.revision),
                    Some(&1_u64.to_be_bytes()),
                )
                .unwrap(),
            crate::storage::CasOutcome::Applied(_)
        ));
        first_release.send(()).unwrap();
        retry_ready.recv_timeout(Duration::from_secs(10)).unwrap();
        let retrying = client.background_indexing_stats();
        assert_eq!(
            (retrying.fulltext.attempts, retrying.fulltext.failures),
            (2, 1)
        );
        assert_eq!(retrying.fulltext.checked_basis_t, None);
        assert_eq!(retrying.fulltext.idle_retries, 0);
        assert_eq!(
            retrying.fulltext.retry_in, None,
            "retry is running, not scheduled"
        );
        assert!(!retrying.fulltext.retry_exhausted);
        let failure = retrying.fulltext.last_failure.as_ref().unwrap();
        // Adoption checks the staged object's original guard before loading
        // provenance or writing its new publication root.
        assert_eq!(
            (failure.category, failure.code),
            (
                ErrorCategory::Conflict,
                "storage/index-publication-conflict",
            )
        );
        assert_eq!((retrying.jobs_completed, retrying.jobs_failed), (0, 0));
        assert_eq!(
            store
                .read_ref(&database.reference_key())
                .unwrap()
                .unwrap()
                .value,
            before_adoption,
            "a stale preparation must not publish"
        );
        assert!(retrying.last_failure.is_none());
        assert!(client.is_available());
        retry_release.send(()).unwrap();

        let deadline = Instant::now() + Duration::from_secs(10);
        let recovered = loop {
            let stats = client.background_indexing_stats();
            if stats.published_basis_t == report.basis_t && !stats.job_in_flight {
                break stats;
            }
            assert!(
                Instant::now() < deadline && client.is_available(),
                "{stats:?}"
            );
            thread::sleep(Duration::from_millis(5));
        };
        assert_eq!(recovered.fulltext.checked_basis_t, Some(report.basis_t));
        assert_eq!(
            (recovered.fulltext.attempts, recovered.fulltext.failures),
            (2, 1)
        );
        assert_eq!(recovered.fulltext.idle_retries, 0);
        assert!(recovered.fulltext.last_failure.is_none());
        assert!(recovered.fulltext.retry_in.is_none());
        assert!(!recovered.fulltext.retry_exhausted);
        assert_eq!((recovered.jobs_completed, recovered.jobs_failed), (1, 0));
        let peer = crate::Peer::connect(&fixture.connection, "projection", 64).unwrap();
        let search = peer
            .db()
            .fulltext(TEXT, "needle", &crate::FulltextOptions::default())
            .unwrap();
        assert_eq!(search.stats.index_basis_t, report.basis_t);
        assert_eq!(search.hits.len(), 1);
        assert_eq!(search.hits[0].entity, report.tempids["document"]);
        eprintln!(
            "UNIFIED_PROJECTION basis={} attempts={} failures={} adopted_jobs={} idle_retries={}",
            report.basis_t,
            recovered.fulltext.attempts,
            recovered.fulltext.failures,
            recovered.jobs_completed,
            recovered.fulltext.idle_retries
        );
        service.shutdown();
    }

    #[test]
    fn submitted_context_measures_transaction_phases_retries_and_unknown_outcomes() {
        let Some(fixture) = observation_fixture() else {
            eprintln!("SKIPPED transaction phase integration: ATOMIC_POSTGRES_URL is unset");
            return;
        };
        let database_id = crate::storage::engine::identity_string(fixture.database.identity);
        let service = TransactionService::start_with_indexing(
            observation_service_config(&fixture.connection, "observed".into()),
            BackgroundIndexingConfig {
                memory_index_threshold_bytes: 1 << 30,
                memory_index_max_bytes: 2 << 30,
            },
        )
        .unwrap();
        let client = service.client();
        let callbacks = Arc::new(AtomicUsize::new(0));
        let delivered = Arc::clone(&callbacks);
        let measured = OperationContext::with_callback(
            OperationKind::Application,
            Arc::new(move |_| {
                delivered.fetch_add(1, Ordering::Relaxed);
            }),
        );
        let request = observation_request("measured", 11);
        // The caller's scope is gone before waiting: only explicit Work
        // propagation can attribute calls made on the transactor thread.
        let ticket = {
            let _scope = measured.enter();
            client.submit(request.clone()).unwrap()
        };
        assert_eq!(
            ticket.request_key_hash,
            crate::storage::receipts::scoped_request_key(&fixture.database.identity, "measured")
                .unwrap()
        );
        let first = ticket.wait(Duration::from_secs(5)).unwrap();
        assert_eq!(first.db_before.basis_t(), service.recovery_stats().target_t);
        assert_eq!(first.basis_t, service.recovery_stats().target_t + 1);
        let first_stats = measured.snapshot();
        for kind in [
            OperationKind::Transaction,
            OperationKind::TransactionExpansion,
            OperationKind::TransactionAssessment,
            OperationKind::TransactionEncoding,
            OperationKind::TransactionCommit,
            OperationKind::TransactionReport,
        ] {
            assert!(first_stats.phases[&kind].invocations > 0);
            assert!(first_stats.phases[&kind].elapsed_nanos > 0);
        }
        // Assessment may be entirely cache-resident; SQL is not required to
        // make a real semantic phase observable.
        assert!(first_stats.by_operation[&OperationKind::TransactionCommit].calls > 0);
        assert_eq!(
            callbacks.load(Ordering::Relaxed),
            0,
            "worker invoked caller metric callback"
        );
        assert!(measured.publish());
        assert_eq!(callbacks.load(Ordering::Relaxed), 1);

        let replay_context = OperationContext::new(OperationKind::Application);
        let replay = {
            let _scope = replay_context.enter();
            client.submit(request).unwrap()
        }
        .wait(Duration::from_secs(5))
        .unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, first.tx_hash);
        let replay_stats = replay_context.snapshot();
        assert_eq!(
            replay_stats.phases[&OperationKind::TransactionReport].invocations,
            1,
            "one receipt reconstruction must record one shared report phase"
        );
        for kind in [
            OperationKind::TransactionExpansion,
            OperationKind::TransactionAssessment,
            OperationKind::TransactionEncoding,
            OperationKind::TransactionCommit,
        ] {
            assert!(
                !replay_stats.phases.contains_key(&kind),
                "replay fabricated an unexecuted phase"
            );
        }
        assert_eq!(
            measured.snapshot().phases,
            first_stats.phases,
            "sibling operation changed original phase totals"
        );

        let rejected_context = OperationContext::new(OperationKind::Application);
        let rejected = {
            let _scope = rejected_context.enter();
            client
                .submit(TransactionRequest::new(
                    "invalid-value",
                    vec![TxOp::Add {
                        entity: EntityRef::Id(make_eid(USER_PARTITION, 42).unwrap()),
                        attribute: OBSERVED_ITEM_COUNT,
                        value: TxValue::Scalar(Value::String("not a long".into())),
                    }],
                ))
                .unwrap()
        }
        .wait(Duration::from_secs(5))
        .unwrap_err();
        assert_ne!(rejected.category, ErrorCategory::UnknownOutcome);
        let rejected_stats = rejected_context.snapshot();
        assert!(
            rejected_stats
                .phases
                .contains_key(&OperationKind::TransactionAssessment)
        );
        assert!(
            !rejected_stats
                .phases
                .contains_key(&OperationKind::TransactionEncoding)
        );
        assert!(
            !rejected_stats
                .phases
                .contains_key(&OperationKind::TransactionCommit)
        );

        let unknown_context = OperationContext::new(OperationKind::Application);
        let reports = client.subscribe_reports();
        let unknown_request = observation_request("measured-unknown", 22);
        arm_observation_fault(
            &database_id,
            "measured-unknown",
            CommitObservationFault::AfterCommitBeforeResponse,
        );
        let error = {
            let _scope = unknown_context.enter();
            client.submit(unknown_request.clone()).unwrap()
        }
        .wait(Duration::from_secs(5))
        .unwrap_err();
        assert_eq!(error.category, ErrorCategory::UnknownOutcome);
        let durable = reports.recv_timeout(Duration::from_secs(5)).unwrap();
        // A subsequent request cannot overtake reconciliation; awaiting it
        // also makes that original context's post-response measurements final.
        let reconciled = client
            .transact(unknown_request, Duration::from_secs(5))
            .unwrap();
        assert!(reconciled.replayed);
        assert_eq!(reconciled.tx_hash, durable.tx_hash);
        let unknown_stats = unknown_context.snapshot();
        let report_phase = unknown_stats
            .phases
            .get(&OperationKind::TransactionReport)
            .expect("unknown-outcome reconciliation must measure exact report reconstruction");
        assert!(report_phase.invocations >= 1);
        assert!(report_phase.elapsed_nanos > 0);
        assert!(unknown_stats.by_operation[&OperationKind::TransactionReport].calls > 0);
        assert_eq!(callbacks.load(Ordering::Relaxed), 1);
        assert_eq!(service.writer_residency_stats().eager_database_values, 0);
        println!(
            "TRANSACTION_PHASES_OK sql_calls={} phases={:?} replay_sql_calls={} rejected_before_publication=true unknown_reconciled=true explicit_callbacks=1",
            first_stats.sql_calls, first_stats.phases, replay_stats.sql_calls
        );
        service.shutdown();
    }

    #[test]
    fn block_index_preparation_does_not_replace_newer_transactions_or_receipts() {
        let Some(fixture) = observation_fixture() else {
            return;
        };
        let service = TransactionService::start_with_indexing(
            observation_service_config(&fixture.connection, "observed".into()),
            BackgroundIndexingConfig {
                memory_index_threshold_bytes: 1 << 30,
                memory_index_max_bytes: 2 << 30,
            },
        )
        .unwrap();
        let client = service.client();
        let first_request = observation_request("index-prefix", 11);
        let first = client
            .transact(first_request.clone(), Duration::from_secs(10))
            .unwrap();
        let identity = crate::storage::engine::identity_string(fixture.database.identity);
        let (ready, release) = block_service::pause_preparation(&identity);
        let requested = client.request_index().unwrap();
        assert_eq!(requested.target_t, first.basis_t);
        ready.recv_timeout(Duration::from_secs(10)).unwrap();
        let newer_request = observation_request("index-newer", 22);
        let newer = client
            .transact(newer_request.clone(), Duration::from_secs(10))
            .unwrap();
        assert_eq!(newer.basis_t, first.basis_t + 1);
        release.send(()).unwrap();
        let deadline = Instant::now() + Duration::from_secs(10);
        while client.background_indexing_stats().published_basis_t < requested.target_t {
            assert!(
                Instant::now() < deadline && client.is_available(),
                "index failed: {:?}",
                client.background_indexing_stats()
            );
            thread::sleep(Duration::from_millis(5));
        }
        let stats = client.background_indexing_stats();
        assert_eq!(stats.published_basis_t, first.basis_t);
        assert_eq!(stats.target_basis_t, newer.basis_t);
        assert_eq!(
            stats.total_transactions, 1,
            "only the covered prefix retires"
        );
        assert_eq!(stats.jobs_completed, 1);
        for (request, expected) in [(first_request, &first), (newer_request, &newer)] {
            let replay = client.transact(request, Duration::from_secs(10)).unwrap();
            assert!(replay.replayed);
            assert_eq!(replay.tx_hash, expected.tx_hash);
            assert_eq!(
                replay.db_after.datoms(crate::IndexOrder::Eavt).unwrap(),
                expected.db_after.datoms(crate::IndexOrder::Eavt).unwrap()
            );
        }
        assert_eq!(
            first
                .db_after
                .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
                .unwrap(),
            vec![Value::Long(11)]
        );
        service.shutdown();
    }

    #[test]
    fn operator_index_publication_discards_a_stale_background_candidate_without_fencing() {
        let Some(fixture) = observation_fixture() else {
            return;
        };
        let service = TransactionService::start_with_indexing(
            observation_service_config(&fixture.connection, "observed".into()),
            BackgroundIndexingConfig {
                memory_index_threshold_bytes: 1 << 30,
                memory_index_max_bytes: 2 << 30,
            },
        )
        .unwrap();
        let client = service.client();
        let request = observation_request("operator-prefix", 11);
        let first = client
            .transact(request.clone(), Duration::from_secs(10))
            .unwrap();
        let identity = crate::storage::engine::identity_string(fixture.database.route);
        let (ready, release) = block_service::pause_preparation(&identity);
        assert!(client.request_index().unwrap().scheduled);
        ready.recv_timeout(Duration::from_secs(10)).unwrap();
        let manual = crate::PostgresOperator::connect(&fixture.connection)
            .unwrap()
            .consolidate_database(&identity);
        // Always release the independent worker before asserting the outcome.
        // Fixture cleanup must never race a stranded preparation thread.
        release.send(()).unwrap();
        assert_eq!(manual.unwrap().basis_t, first.basis_t);
        let deadline = Instant::now() + Duration::from_secs(10);
        let stats = loop {
            let stats = client.background_indexing_stats();
            if stats.published_basis_t >= first.basis_t && !stats.job_in_flight {
                break stats;
            }
            assert!(
                Instant::now() < deadline && client.is_available(),
                "{stats:?}"
            );
            thread::sleep(Duration::from_millis(5));
        };
        assert_eq!(stats.jobs_failed, 0);
        assert!(
            stats.jobs_started >= 2,
            "stale job was not recaptured: {stats:?}"
        );
        assert!(stats.last_failure.is_none());
        let replay = client.transact(request, Duration::from_secs(10)).unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, first.tx_hash);
        let second = client
            .transact(
                observation_request("after-operator", 22),
                Duration::from_secs(10),
            )
            .unwrap();
        assert_eq!(second.basis_t, first.basis_t + 1);
        assert_eq!(
            first
                .db_after
                .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
                .unwrap(),
            vec![Value::Long(11)]
        );
        service.shutdown();
    }

    #[test]
    fn block_index_backpressure_replays_before_gate_and_invokes_parked_callback_once() {
        let Some(fixture) = observation_fixture() else {
            return;
        };
        let called = Arc::new(AtomicUsize::new(0));
        let counted = Arc::clone(&called);
        let name = crate::Symbol::new("test.index.v1", "write");
        let mut registry = crate::NativeRegistry::builder();
        registry
            .transaction(name.clone(), move |_, _, control| {
                control.check(1)?;
                counted.fetch_add(1, Ordering::SeqCst);
                Ok(observation_request("unused", 22).forms)
            })
            .unwrap();
        let service = TransactionService::start_with_options(
            observation_service_config(&fixture.connection, "observed".into()),
            ServiceOptions {
                indexing: BackgroundIndexingConfig {
                    memory_index_threshold_bytes: 128,
                    memory_index_max_bytes: 256,
                },
                execution: crate::TransactionExecutionOptions {
                    native: registry.build(),
                    ..Default::default()
                },
                ..Default::default()
            },
        )
        .unwrap();
        let client = service.client();
        let identity = crate::storage::engine::identity_string(fixture.database.identity);
        let (ready, release) = block_service::pause_preparation(&identity);
        let request = observation_request("before-pressure", 11);
        let first = client
            .transact(request.clone(), Duration::from_secs(10))
            .unwrap();
        ready.recv_timeout(Duration::from_secs(10)).unwrap();
        assert!(client.background_indexing_stats().total_bytes > 256);
        let replay = client.transact(request, Duration::from_secs(10)).unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, first.tx_hash);
        let pending = client
            .submit(TransactionRequest::from_forms(
                "parked-native",
                vec![TxForm::ProgramCall(ProgramCall {
                    function: crate::CallableRef::Local(name),
                    arguments: vec![],
                })],
            ))
            .unwrap();
        let deadline = Instant::now() + Duration::from_secs(10);
        while client.background_indexing_stats().backpressure_stalls == 0 {
            assert!(Instant::now() < deadline && client.is_available());
            thread::sleep(Duration::from_millis(5));
        }
        assert_eq!(
            called.load(Ordering::SeqCst),
            0,
            "parked admission ran a callback"
        );
        assert_eq!(client.stats().queued, 1);
        release.send(()).unwrap();
        let second = pending.wait(Duration::from_secs(10)).unwrap();
        assert_eq!(second.basis_t, first.basis_t + 1);
        assert_eq!(called.load(Ordering::SeqCst), 1);
        assert_eq!(client.stats().queued, 0);
        assert!(client.background_indexing_stats().jobs_completed >= 1);
        assert_eq!(
            first
                .db_after
                .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
                .unwrap(),
            vec![Value::Long(11)]
        );
        service.shutdown();
    }

    #[test]
    fn unknown_outcome_reconciliation_notifies_once_only_for_a_durable_decision() {
        let Some(fixture) = observation_fixture() else {
            return;
        };
        let database_id = crate::storage::engine::identity_string(fixture.database.identity);

        // Keep committed novelty resident so the accounting assertions cannot
        // race a background publication which legitimately drains it.
        let service = TransactionService::start_with_indexing(
            observation_service_config(&fixture.connection, "observed".into()),
            BackgroundIndexingConfig {
                memory_index_threshold_bytes: 1 << 30,
                memory_index_max_bytes: 2 << 30,
            },
        )
        .unwrap();
        let client = service.client();
        let reports = client.subscribe_reports();
        let initial_basis = service.recovery_stats().target_t;
        assert_eq!(initial_basis, 1, "schema installation is a transaction");

        let committed_request = observation_request("committed-unknown", 11);
        arm_observation_fault(
            &database_id,
            "committed-unknown",
            CommitObservationFault::AfterCommitBeforeResponse,
        );
        let committed_error = client
            .submit(committed_request.clone())
            .unwrap()
            .wait(Duration::from_secs(2))
            .unwrap_err();
        assert_eq!(committed_error.category, ErrorCategory::UnknownOutcome);
        assert_eq!(committed_error.details["ambiguity_kind"], "publication");

        // The origin observes Unknown first. The worker then reconnects,
        // renews its fenced lease, resolves without resubmission, and emits
        // the notification which authoritative peer data delivery would have
        // produced even though the request acknowledgement was lost.
        let committed_report = reports.recv_timeout(Duration::from_secs(2)).unwrap();
        assert_eq!(committed_report.basis_t, initial_basis + 1);
        assert!(!committed_report.replayed);
        assert_eq!(
            committed_report
                .db_after
                .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
                .unwrap(),
            vec![Value::Long(11)]
        );
        let committed_datoms = committed_report.tx_data.len() as u64;

        let replay = client
            .transact(committed_request.clone(), Duration::from_secs(2))
            .unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, committed_report.tx_hash);
        assert_eq!(replay.tx_data, committed_report.tx_data);
        assert_eq!(replay.tempids, committed_report.tempids);
        for (left, right) in [
            (&replay.db_before, &committed_report.db_before),
            (&replay.db_after, &committed_report.db_after),
        ] {
            assert_eq!(left.basis_t(), right.basis_t());
            assert_eq!(
                left.datoms(crate::IndexOrder::Eavt).unwrap(),
                right.datoms(crate::IndexOrder::Eavt).unwrap()
            );
        }
        let first_snapshot = committed_report.db_after.block_snapshot().unwrap();
        let replay_snapshot = replay.db_after.block_snapshot().unwrap();
        let cache_before = first_snapshot.cache_stats();
        replay.db_after.datoms(crate::IndexOrder::Eavt).unwrap();
        assert!(replay_snapshot.cache_stats().hits > cache_before.hits);
        assert_eq!(
            first_snapshot.cache_stats(),
            replay_snapshot.cache_stats(),
            "receipt values share the block reader cache, not backend-specific writer state"
        );
        let after_commit = client.background_indexing_stats();
        assert_eq!(after_commit.target_basis_t, initial_basis + 1);
        assert_eq!(after_commit.total_transactions, 1);
        assert_eq!(after_commit.total_datoms, committed_datoms);
        assert!(
            matches!(
                reports.recv_timeout(Duration::from_millis(100)),
                Err(mpsc::RecvTimeoutError::Timeout)
            ),
            "an ordinary idempotent retry must not duplicate the live report"
        );
        assert_eq!(
            client.background_indexing_stats().total_transactions,
            1,
            "an ordinary replay must not charge indexing novelty twice"
        );

        arm_observation_fault(
            &database_id,
            "committed-unknown",
            CommitObservationFault::AfterCommitBeforeResponse,
        );
        let replay_read_error = client
            .submit(committed_request.clone())
            .unwrap()
            .wait(Duration::from_secs(2))
            .unwrap_err();
        assert_eq!(replay_read_error.category, ErrorCategory::UnknownOutcome);
        assert_eq!(replay_read_error.details["ambiguity_kind"], "outcome-read");
        assert!(
            matches!(
                reports.recv_timeout(Duration::from_millis(100)),
                Err(mpsc::RecvTimeoutError::Timeout)
            ),
            "acknowledgement loss while reading a replay must not duplicate its original report"
        );

        let replay_after_read_ambiguity = client
            .transact(committed_request, Duration::from_secs(2))
            .unwrap();
        assert!(replay_after_read_ambiguity.replayed);
        assert_eq!(
            replay_after_read_ambiguity.tx_hash,
            committed_report.tx_hash
        );
        assert!(
            matches!(
                reports.recv_timeout(Duration::from_millis(100)),
                Err(mpsc::RecvTimeoutError::Timeout)
            ),
            "reconciling an ambiguous replay read must remain report-suppressed"
        );
        assert_eq!(
            client.background_indexing_stats().total_transactions,
            1,
            "an ordinary replay must not charge indexing novelty twice"
        );

        arm_observation_fault(
            &database_id,
            "absent-unknown",
            CommitObservationFault::AbsentUnknownOutcome,
        );
        let absent_error = client
            .submit(observation_request("absent-unknown", 22))
            .unwrap()
            .wait(Duration::from_secs(2))
            .unwrap_err();
        assert_eq!(absent_error.category, ErrorCategory::UnknownOutcome);

        // The next ordinary request cannot overtake reconciliation. A locked,
        // successful absence decision produces no report and no novelty; only
        // this genuinely committed successor is observed.
        let successor = client
            .transact(
                observation_request("after-absent", 33),
                Duration::from_secs(2),
            )
            .unwrap();
        assert_eq!(successor.basis_t, initial_basis + 2);
        let successor_report = reports.recv_timeout(Duration::from_secs(2)).unwrap();
        assert_eq!(successor_report.tx_hash, successor.tx_hash);
        assert!(matches!(
            reports.recv_timeout(Duration::from_millis(100)),
            Err(mpsc::RecvTimeoutError::Timeout)
        ));

        arm_observation_fault(
            &database_id,
            "rolled-back",
            CommitObservationFault::BeforePublication,
        );
        let rollback_error = client
            .submit(observation_request("rolled-back", 44))
            .unwrap()
            .wait(Duration::from_secs(2))
            .unwrap_err();
        assert_eq!(
            (rollback_error.category, rollback_error.code),
            (ErrorCategory::Interrupted, "storage/injected-failure")
        );
        assert!(
            matches!(
                reports.recv_timeout(Duration::from_millis(100)),
                Err(mpsc::RecvTimeoutError::Timeout)
            ),
            "rolled-back publication work must remain invisible"
        );
        let final_stats = client.background_indexing_stats();
        assert_eq!(final_stats.target_basis_t, initial_basis + 2);
        assert_eq!(final_stats.total_transactions, 2);
        assert_eq!(
            final_stats.total_datoms,
            committed_datoms + successor_report.tx_data.len() as u64
        );

        service.shutdown();
        let config = PostgresConnectionConfig::plaintext(&fixture.connection);
        let mut verifier = crate::storage::BlockTransactor::claim(
            &config,
            fixture.database.clone(),
            crate::storage::BlockWriterOptions::default(),
        )
        .unwrap();
        assert_eq!(verifier.db().unwrap().basis_t(), initial_basis + 2);
        for (key, value, present) in [
            ("committed-unknown", 11, true),
            ("absent-unknown", 22, false),
            ("rolled-back", 44, false),
        ] {
            let request = observation_request(key, value);
            let (digest, _) = crate::encoding::canonical_submission_request(
                &request.forms,
                request.compare_basis_t,
                request.tx_instant_override,
                crate::storage::log::MAX_LOG_TRANSACTION_BYTES,
            )
            .unwrap();
            assert_eq!(
                verifier
                    .resolve_request_outcome(key, digest)
                    .unwrap()
                    .is_some(),
                present
            );
        }
        assert_eq!(
            committed_report
                .db_after
                .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
                .unwrap(),
            vec![Value::Long(11)],
            "captured pre-successor value remains immutable"
        );
        verifier.release().unwrap();
    }
}
