use crate::log_generation::request_key_hash;
use crate::postgres::{
    CapacityLimits, CommitReceipt, PostgresStore, SharedProgramCache, TransactorLease,
    WriterResidencyStats, is_postgres_connection_error, postgres_error,
    read_authenticated_log_range, shared_program_cache_stats,
};
use crate::{
    DatabaseValue, Datom, Digest, ErrorCategory, IndexBuildFault, PostgresConnectionConfig,
    PostgresIndexer, ProgramCacheStats, ProgramCall, RecoveryStats, SemanticError, TxForm, TxOp,
};
use std::collections::{BTreeMap, VecDeque};
use std::mem::size_of;
use std::sync::atomic::{AtomicBool, AtomicU64, AtomicUsize, Ordering};
use std::sync::{Arc, Mutex, Weak, mpsc};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

const DEFAULT_MEMORY_INDEX_THRESHOLD_BYTES: u64 = 32 * 1024 * 1024;
const DEFAULT_MEMORY_INDEX_MAX_BYTES: u64 = 512 * 1024 * 1024;
// Recovered `process-request-index` permits the initial publication attempt
// plus two retries before failing the transactor process.
const MAX_INDEX_JOB_RETRIES: usize = 2;
// RecentTier retains at most four raw BTSet entries per datom. Its
// allocator-independent conservative account reserves a fifth reference for
// persistent-log/tree overhead; keep admission on that same bound.
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
}

#[derive(Clone, Debug)]
pub struct TransactionServiceConfig {
    pub connection: String,
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
}

impl ServiceTransactionReport {
    fn from_commit(commit: CommitReceipt) -> Self {
        Self {
            db_before: commit.db_before,
            db_after: commit.database,
            basis_t: commit.basis_t,
            tx_hash: commit.tx_hash,
            tx_data: commit.tx_data,
            tempids: commit.tempids,
            replayed: commit.replayed,
        }
    }
}

struct Work {
    request: TransactionRequest,
    request_hash: Digest,
    response: mpsc::SyncSender<Result<ServiceTransactionReport, SemanticError>>,
}

#[derive(Clone, Copy, Debug)]
struct Novelty {
    basis_t: u64,
    datoms: u64,
    bytes: u64,
}

#[derive(Debug)]
struct IndexingSeed {
    /// Stable database identity used to make client request identifiers
    /// opaque before they can enter diagnostics.  A lineage survives
    /// generation replacement, so tickets remain reconcilable across an
    /// excision or restore race without retaining plaintext in an error.
    lineage_id: String,
    /// Newest fully authenticated publication selected for replay.
    published_revision: u64,
    published_basis_t: u64,
    /// Greatest physical publication coordinate, independent of validity.
    newest_observed_revision: u64,
    target_basis_t: u64,
    pending: VecDeque<Novelty>,
    /// No usable native publication covers the newest observed revision. A
    /// positive generation's canonical basis-zero value is publishable too.
    needs_publication: bool,
}

#[derive(Debug)]
struct IndexingBacklog {
    published_revision: u64,
    published_basis_t: u64,
    newest_observed_revision: u64,
    target_basis_t: u64,
    pending: VecDeque<Novelty>,
    total_datoms: u64,
    total_bytes: u64,
    indexing_through: Option<u64>,
    needs_publication: bool,
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
}

impl BackgroundIndexing {
    fn new(
        config: BackgroundIndexingConfig,
        seed: IndexingSeed,
        sender: mpsc::SyncSender<IndexCommand>,
    ) -> Self {
        let total_datoms = seed
            .pending
            .iter()
            .fold(0_u64, |total, item| total.saturating_add(item.datoms));
        let total_bytes = seed
            .pending
            .iter()
            .fold(0_u64, |total, item| total.saturating_add(item.bytes));
        Self {
            config,
            backlog: Mutex::new(IndexingBacklog {
                published_revision: seed.published_revision,
                published_basis_t: seed.published_basis_t,
                newest_observed_revision: seed.newest_observed_revision,
                target_basis_t: seed.target_basis_t,
                pending: seed.pending,
                total_datoms,
                total_bytes,
                indexing_through: None,
                needs_publication: seed.needs_publication,
            }),
            sender,
            jobs_started: AtomicU64::new(0),
            jobs_completed: AtomicU64::new(0),
            jobs_failed: AtomicU64::new(0),
            backpressure_stalls: AtomicU64::new(0),
            backpressure_rejections: AtomicU64::new(0),
            last_failure: Mutex::new(None),
        }
    }

    fn note_commit(&self, basis_t: u64, tx_data: &[Datom]) {
        let changes_avet_membership = tx_data.iter().any(|datom| {
            matches!(
                u64::from(datom.attribute),
                crate::DB_INDEX | crate::DB_UNIQUE
            )
        });
        let novelty = Novelty {
            basis_t,
            datoms: tx_data.len() as u64,
            bytes: accounted_novelty_bytes(tx_data),
        };
        let should_wake = {
            let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
            if basis_t <= backlog.published_basis_t {
                return;
            }
            if backlog
                .pending
                .back()
                .is_some_and(|previous| previous.basis_t >= basis_t)
            {
                // Fresh authoritative commits are ordered by the one writer.
                // A duplicate basis can only be a replay/race already covered
                // by a publication and must not be charged twice.
                return;
            }
            backlog.target_basis_t = backlog.target_basis_t.max(basis_t);
            backlog.pending.push_back(novelty);
            backlog.total_datoms = backlog.total_datoms.saturating_add(novelty.datoms);
            backlog.total_bytes = backlog.total_bytes.saturating_add(novelty.bytes);
            // AVET membership is information-derived from :db/index and
            // :db/unique. Enabling either requires a broad durable rebuild of
            // prior values even when ordinary novelty is below the byte
            // threshold; disabling it should publish the matching physical
            // projection as well.
            backlog.needs_publication |= changes_avet_membership;
            should_index(&backlog, self.config)
        };
        if should_wake {
            self.wake();
        }
    }

    fn wake(&self) {
        match self.sender.try_send(IndexCommand::Wake) {
            Ok(()) | Err(mpsc::TrySendError::Full(_)) => {}
            Err(mpsc::TrySendError::Disconnected(_)) => {}
        }
    }

    /// A lost COMMIT acknowledgement may or may not have advanced the log.
    /// Do not guess or replay transaction data; force the indexer to read the
    /// authoritative head and publish/adopt whatever endpoint PostgreSQL made
    /// visible.
    fn note_unknown_outcome(&self) {
        self.backlog
            .lock()
            .expect("index backlog mutex poisoned")
            .needs_publication = true;
        self.wake();
    }

    fn shutdown(&self) {
        let _ = self.sender.send(IndexCommand::Shutdown);
    }

    fn begin_job(&self) -> bool {
        let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
        if backlog.indexing_through.is_some() {
            return false;
        }
        if !should_index(&backlog, self.config) {
            return false;
        }
        backlog.indexing_through = Some(backlog.target_basis_t);
        self.jobs_started.fetch_add(1, Ordering::Relaxed);
        true
    }

    fn complete_job(&self, published_revision: u64, published_basis_t: u64) {
        let mut backlog = self.backlog.lock().expect("index backlog mutex poisoned");
        backlog.published_revision = backlog.published_revision.max(published_revision);
        backlog.newest_observed_revision = backlog.newest_observed_revision.max(published_revision);
        backlog.published_basis_t = backlog.published_basis_t.max(published_basis_t);
        backlog.target_basis_t = backlog.target_basis_t.max(published_basis_t);
        while backlog
            .pending
            .front()
            .is_some_and(|novelty| novelty.basis_t <= published_basis_t)
        {
            if let Some(novelty) = backlog.pending.pop_front() {
                backlog.total_datoms = backlog.total_datoms.saturating_sub(novelty.datoms);
                backlog.total_bytes = backlog.total_bytes.saturating_sub(novelty.bytes);
            }
        }
        backlog.indexing_through = None;
        backlog.needs_publication = backlog.published_revision < backlog.newest_observed_revision;
        self.jobs_completed.fetch_add(1, Ordering::Relaxed);
    }

    fn fail_job(&self, error: SemanticError) {
        self.backlog
            .lock()
            .expect("index backlog mutex poisoned")
            .indexing_through = None;
        *self
            .last_failure
            .lock()
            .expect("index failure mutex poisoned") = Some(error);
        self.jobs_failed.fetch_add(1, Ordering::Relaxed);
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
        if total_bytes > self.config.memory_index_max_bytes {
            return Some(
                SemanticError::new(
                    ErrorCategory::Busy,
                    "service/index-backpressure",
                    "recent index novelty reached the configured hard limit",
                )
                .detail("backlog_bytes", total_bytes.to_string())
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

    fn at_hard_limit(&self) -> bool {
        self.backlog
            .lock()
            .expect("index backlog mutex poisoned")
            .total_bytes
            > self.config.memory_index_max_bytes
    }

    fn record_backpressure_stall(&self) {
        self.backpressure_stalls.fetch_add(1, Ordering::Relaxed);
    }

    fn record_backpressure_rejection(&self) {
        self.backpressure_rejections.fetch_add(1, Ordering::Relaxed);
    }

    fn stats(&self) -> BackgroundIndexingStats {
        let backlog = self.backlog.lock().expect("index backlog mutex poisoned");
        let mut memory_transactions = 0_u64;
        let mut memory_datoms = 0_u64;
        let mut memory_bytes = 0_u64;
        let mut indexing_transactions = 0_u64;
        let mut indexing_datoms = 0_u64;
        let mut indexing_bytes = 0_u64;
        for novelty in &backlog.pending {
            if backlog
                .indexing_through
                .is_some_and(|through| novelty.basis_t <= through)
            {
                indexing_transactions = indexing_transactions.saturating_add(1);
                indexing_datoms = indexing_datoms.saturating_add(novelty.datoms);
                indexing_bytes = indexing_bytes.saturating_add(novelty.bytes);
            } else {
                memory_transactions = memory_transactions.saturating_add(1);
                memory_datoms = memory_datoms.saturating_add(novelty.datoms);
                memory_bytes = memory_bytes.saturating_add(novelty.bytes);
            }
        }
        let last_failure = self
            .last_failure
            .lock()
            .expect("index failure mutex poisoned")
            .as_ref()
            .map(|error| BackgroundIndexingFailure {
                category: error.category,
                code: error.code,
                message: error.message.clone(),
            });
        BackgroundIndexingStats {
            published_revision: backlog.published_revision,
            published_basis_t: backlog.published_basis_t,
            newest_observed_revision: backlog.newest_observed_revision,
            target_basis_t: backlog.target_basis_t,
            memory_index_transactions: memory_transactions,
            memory_index_datoms: memory_datoms,
            memory_index_bytes: memory_bytes,
            indexing_transactions,
            indexing_datoms,
            indexing_bytes,
            total_transactions: memory_transactions.saturating_add(indexing_transactions),
            total_datoms: backlog.total_datoms,
            total_bytes: backlog.total_bytes,
            jobs_started: self.jobs_started.load(Ordering::Relaxed),
            jobs_completed: self.jobs_completed.load(Ordering::Relaxed),
            jobs_failed: self.jobs_failed.load(Ordering::Relaxed),
            backpressure_stalls: self.backpressure_stalls.load(Ordering::Relaxed),
            backpressure_rejections: self.backpressure_rejections.load(Ordering::Relaxed),
            job_in_flight: backlog.indexing_through.is_some(),
            last_failure,
        }
    }
}

fn should_index(backlog: &IndexingBacklog, config: BackgroundIndexingConfig) -> bool {
    // Positive log generations have a canonical native basis-zero value.
    // `needs_publication` therefore drives fresh-database bootstrap and
    // same-basis repair even before the first ordinary transaction.
    backlog.needs_publication
        || (backlog.target_basis_t > backlog.published_basis_t
            && backlog.total_bytes > config.memory_index_threshold_bytes)
}

struct Shared {
    accepting: AtomicBool,
    admission: Mutex<()>,
    queued: AtomicUsize,
    max_queued: AtomicUsize,
    processed: AtomicU64,
    rejected_full: AtomicU64,
    next_subscriber: AtomicU64,
    subscribers: Mutex<BTreeMap<u64, mpsc::Sender<ServiceTransactionReport>>>,
    writer_residency: Mutex<WriterResidencyStats>,
    max_request_bytes: usize,
    indexing: Arc<BackgroundIndexing>,
    connection: PostgresConnectionConfig,
    database_id: String,
    lineage_id: String,
}

impl Shared {
    fn new(
        max_request_bytes: usize,
        writer_residency: WriterResidencyStats,
        indexing: Arc<BackgroundIndexing>,
        connection: PostgresConnectionConfig,
        database_id: String,
        lineage_id: String,
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
            writer_residency: Mutex::new(writer_residency),
            max_request_bytes,
            indexing,
            connection,
            database_id,
            lineage_id,
        }
    }

    fn unavailable(&self) -> SemanticError {
        SemanticError::new(
            ErrorCategory::Unavailable,
            "service/unavailable",
            "transaction service is not accepting requests",
        )
    }

    fn publish(&self, report: &ServiceTransactionReport) {
        let mut subscribers = self.subscribers.lock().expect("subscriber mutex poisoned");
        subscribers.retain(|_, sender| sender.send(report.clone()).is_ok());
    }

    /// At the pressure gate, already-committed idempotent requests remain
    /// resolvable. A new request receives the hard-limit marker so the worker
    /// can park it without evaluating or extending the log.
    fn check_index_gate(
        &self,
        request_key: &str,
        request_hash: Digest,
    ) -> Result<(), SemanticError> {
        let Some(limit) = self.indexing.limiting_error() else {
            return Ok(());
        };
        let mut client = self.connection.connect_for("service/index-gate-connect")?;
        let head = client
            .query_opt(
                "SELECT h.log_generation, d.lineage_id \
                   FROM atomic_heads h JOIN atomic_databases d USING (database_id) \
                  WHERE h.database_id = $1",
                &[&self.database_id],
            )
            .map_err(|error| postgres_error("service/index-gate-head", error))?
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "postgres/database-not-found",
                    format!("database {} does not exist", self.database_id),
                )
            })?;
        let generation = nonnegative_basis(head.get(0), "active log generation")?;
        let lineage_id: String = head.get(1);
        let generation_sql = i64::try_from(generation).map_err(|_| {
            SemanticError::new(
                ErrorCategory::Fault,
                "service/index-generation-overflow",
                "active log generation exceeds PostgreSQL bigint",
            )
        })?;
        let key_hash = request_key_hash(&lineage_id, request_key)?;
        let row = if generation == 0 {
            client.query_opt(
                "SELECT request_digest, 1::smallint FROM atomic_requests \
                 WHERE database_id = $1 AND request_key = $2",
                &[&self.database_id, &request_key],
            )
        } else {
            client.query_opt(
                "SELECT request_digest, request_kind FROM atomic_generation_requests \
                 WHERE database_id = $1 AND generation = $2 AND request_key_hash = $3",
                &[&self.database_id, &generation_sql, &&key_hash[..]],
            )
        }
        .map_err(|error| postgres_error("service/index-gate-read", error))?;
        let Some(row) = row else {
            return Err(limit);
        };
        if row.get::<_, i16>(1) == 0 {
            return Err(SemanticError::conflict(
                "postgres/idempotency-predates-excision",
                "request key was committed before excision, but its original receipt was deliberately erased",
            ));
        }
        let bytes: Vec<u8> = row.get(0);
        let stored: Digest = bytes.try_into().map_err(|_| {
            SemanticError::new(
                ErrorCategory::Fault,
                "service/request-digest-corrupt",
                "durable request digest is not 32 bytes",
            )
        })?;
        if stored != request_hash {
            return Err(SemanticError::conflict(
                "postgres/idempotency-key-reused",
                "idempotency key is already bound to a different request",
            ));
        }
        Ok(())
    }
}

#[derive(Clone)]
pub struct TransactionClient {
    sender: mpsc::SyncSender<Work>,
    shared: Arc<Shared>,
}

impl TransactionClient {
    pub fn submit(&self, request: TransactionRequest) -> Result<TransactionTicket, SemanticError> {
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
        let request_key_hash = request_key_hash(&self.shared.lineage_id, &request_key)?;
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
        self.shared
            .subscribers
            .lock()
            .expect("subscriber mutex poisoned")
            .insert(id, sender);
        ReportSubscription {
            id,
            receiver,
            shared: Arc::downgrade(&self.shared),
        }
    }

    pub fn stats(&self) -> ServiceStats {
        ServiceStats {
            queued: self.shared.queued.load(Ordering::Relaxed),
            max_queued: self.shared.max_queued.load(Ordering::Relaxed),
            processed: self.shared.processed.load(Ordering::Relaxed),
            rejected_full: self.shared.rejected_full.load(Ordering::Relaxed),
            subscribers: self
                .shared
                .subscribers
                .lock()
                .expect("subscriber mutex poisoned")
                .len(),
        }
    }

    pub fn background_indexing_stats(&self) -> BackgroundIndexingStats {
        self.shared.indexing.stats()
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
        self.shared.accepting.load(Ordering::Acquire)
            && self.shared.indexing.stats().last_failure.is_none()
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
    receiver: mpsc::Receiver<ServiceTransactionReport>,
    shared: Weak<Shared>,
}

impl ReportSubscription {
    pub fn recv_timeout(
        &self,
        timeout: Duration,
    ) -> Result<ServiceTransactionReport, mpsc::RecvTimeoutError> {
        self.receiver.recv_timeout(timeout)
    }

    pub fn try_recv(&self) -> Result<ServiceTransactionReport, mpsc::TryRecvError> {
        self.receiver.try_recv()
    }
}

impl Drop for ReportSubscription {
    fn drop(&mut self) {
        if let Some(shared) = self.shared.upgrade() {
            shared
                .subscribers
                .lock()
                .expect("subscriber mutex poisoned")
                .remove(&self.id);
        }
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct ServiceStats {
    pub queued: usize,
    pub max_queued: usize,
    pub processed: u64,
    pub rejected_full: u64,
    pub subscribers: usize,
}

pub struct TransactionService {
    client: TransactionClient,
    recovery_stats: RecoveryStats,
    program_cache: SharedProgramCache,
    worker: Option<JoinHandle<()>>,
    index_worker: Option<JoinHandle<()>>,
}

pub struct TransactionStandby {
    receiver: mpsc::Receiver<Result<TransactionService, SemanticError>>,
    stop: Arc<AtomicBool>,
    worker: Option<JoinHandle<()>>,
}

impl TransactionStandby {
    pub fn start(
        config: TransactionServiceConfig,
        poll_interval: Duration,
    ) -> Result<Self, SemanticError> {
        let connection = PostgresConnectionConfig::plaintext(config.connection.clone());
        Self::start_configured(config, connection, poll_interval)
    }

    pub fn start_configured(
        config: TransactionServiceConfig,
        connection: PostgresConnectionConfig,
        poll_interval: Duration,
    ) -> Result<Self, SemanticError> {
        if poll_interval.is_zero() {
            return Err(SemanticError::incorrect(
                "service/standby-poll",
                "standby poll interval must be positive",
            ));
        }
        let stop = Arc::new(AtomicBool::new(false));
        let worker_stop = stop.clone();
        let (sender, receiver) = mpsc::sync_channel(1);
        let worker = thread::Builder::new()
            .name(format!("atomic-standby-{}", config.holder_id))
            .spawn(move || {
                while !worker_stop.load(Ordering::Acquire) {
                    match TransactionService::start_configured(config.clone(), connection.clone()) {
                        Ok(service) => {
                            let _ = sender.send(Ok(service));
                            return;
                        }
                        Err(error)
                            if (error.category == ErrorCategory::Unavailable
                                && error.code == "postgres/lease-held")
                                || is_postgres_connection_error(&error) =>
                        {
                            thread::sleep(poll_interval);
                        }
                        Err(error) => {
                            let _ = sender.send(Err(error));
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
            worker: Some(worker),
        })
    }

    pub fn await_active(mut self, timeout: Duration) -> Result<TransactionService, SemanticError> {
        let result = match self.receiver.recv_timeout(timeout) {
            Ok(result) => result,
            Err(mpsc::RecvTimeoutError::Timeout) => Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "service/standby-timeout",
                "standby did not acquire leadership before the deadline",
            )),
            Err(mpsc::RecvTimeoutError::Disconnected) => Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "service/standby-stopped",
                "standby stopped before acquiring leadership",
            )),
        };
        self.stop.store(true, Ordering::Release);
        if let Some(worker) = self.worker.take() {
            let _ = worker.join();
        }
        result
    }
}

impl Drop for TransactionStandby {
    fn drop(&mut self) {
        self.stop.store(true, Ordering::Release);
        if let Some(worker) = self.worker.take() {
            let _ = worker.join();
        }
    }
}

impl TransactionService {
    pub fn start(config: TransactionServiceConfig) -> Result<Self, SemanticError> {
        let connection = PostgresConnectionConfig::plaintext(config.connection.clone());
        Self::start_configured(config, connection)
    }

    /// Start every writer, lease, and background-index connection under one
    /// explicit PostgreSQL transport policy. `config.connection` remains for
    /// source compatibility with the original constructor; this argument is
    /// the sole connection source used by this configured path.
    pub fn start_configured(
        config: TransactionServiceConfig,
        connection: PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        Self::start_configured_with_indexing(
            config,
            connection,
            BackgroundIndexingConfig::default(),
        )
    }

    pub fn start_with_indexing(
        config: TransactionServiceConfig,
        indexing_config: BackgroundIndexingConfig,
    ) -> Result<Self, SemanticError> {
        let connection = PostgresConnectionConfig::plaintext(config.connection.clone());
        Self::start_configured_with_indexing(config, connection, indexing_config)
    }

    pub fn start_configured_with_indexing(
        config: TransactionServiceConfig,
        connection: PostgresConnectionConfig,
        indexing_config: BackgroundIndexingConfig,
    ) -> Result<Self, SemanticError> {
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
        let indexing_config = indexing_config.validate()?;
        let lease_millis = duration_millis(config.lease_duration)?;
        let mut store = PostgresStore::connect_configured(&connection)?;
        store.set_capacity_limits(config.capacity_limits)?;
        store.set_writer_recent_limits(crate::recent::RecentLimits {
            soft_datoms: u64::MAX,
            soft_bytes: indexing_config.memory_index_threshold_bytes,
            hard_datoms: u64::MAX,
            // The recovered processor checks the memory-index maximum before
            // dequeuing the next ordinary request, so the transaction that
            // crosses the line is accepted and then causes indexing-first
            // backpressure. Preserve that bounded overshoot here: rejecting
            // the crossing transaction inside RecentTier would invent an
            // atomic transaction size limit stricter than the configured one.
            hard_bytes: indexing_config
                .memory_index_max_bytes
                .saturating_add(max_transaction_novelty_bytes(config.capacity_limits)),
        })?;
        let lease = store.acquire_lease(&config.database_id, &config.holder_id, lease_millis)?;
        let mut indexer =
            match PostgresIndexer::connect_configured(&connection, &config.database_id) {
                Ok(indexer) => indexer,
                Err(error) => {
                    let _ = store.release_lease(&lease);
                    return Err(error);
                }
            };
        // Probe with the strict native opener before deciding that a root must
        // be built. Only genuine absence or a valid root with an over-hard
        // tail authorizes this synchronous bootstrap/catch-up. A present but
        // corrupt native authority fails closed for explicit repair.
        let recovery_stats = match store.activate_transactor_state(&lease, lease_millis) {
            Ok(stats) => stats,
            Err(error)
                if matches!(
                    error.code,
                    "peer/exact-no-native-publication" | "recent/hard-capacity"
                ) =>
            {
                if let Err(index_error) = indexer.consolidate() {
                    let _ = store.release_lease(&lease);
                    return Err(index_error);
                }
                match store.activate_transactor_state(&lease, lease_millis) {
                    Ok(stats) => stats,
                    Err(error) => {
                        let _ = store.release_lease(&lease);
                        return Err(error);
                    }
                }
            }
            Err(error) => {
                let _ = store.release_lease(&lease);
                return Err(error);
            }
        };
        let initial_writer_residency = store.writer_residency_stats(&config.database_id);
        let seed = match load_indexing_seed(
            &connection,
            &config.database_id,
            initial_writer_residency.publication_revision,
            recovery_stats.base_t,
        ) {
            Ok(seed) => seed,
            Err(error) => {
                let _ = store.release_lease(&lease);
                return Err(error);
            }
        };
        // The seed walk can be materially longer than a heartbeat on a large
        // tail. Keep the already-authenticated, pinned writer value and extend
        // that same epoch immediately before any accepting worker is exposed.
        if let Err(error) = store.renew_lease(&lease, lease_millis) {
            let _ = store.release_lease(&lease);
            return Err(error);
        }
        let program_cache = store.program_cache_handle();
        let (index_sender, index_receiver) = mpsc::sync_channel(1);
        let lineage_id = seed.lineage_id.clone();
        let indexing = Arc::new(BackgroundIndexing::new(indexing_config, seed, index_sender));
        let (sender, receiver) = mpsc::sync_channel(config.queue_capacity);
        let shared = Arc::new(Shared::new(
            config.capacity_limits.max_transaction_bytes,
            initial_writer_residency,
            Arc::clone(&indexing),
            connection.clone(),
            config.database_id.clone(),
            lineage_id,
        ));
        let index_shared = Arc::clone(&shared);
        let index_worker = match thread::Builder::new()
            .name(format!("atomic-indexer-{}", config.holder_id))
            .spawn(move || {
                run_index_worker(indexer, index_receiver, &index_shared);
            }) {
            Ok(worker) => worker,
            Err(error) => {
                let _ = store.release_lease(&lease);
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "service/index-spawn",
                    error.to_string(),
                ));
            }
        };
        let worker_shared = shared.clone();
        let renew_interval = config.renew_interval;
        let database_id = config.database_id.clone();
        let cleanup_connection = connection;
        let cleanup_lease = lease.clone();
        let worker = match thread::Builder::new()
            .name(format!("atomic-transactor-{}", config.holder_id))
            .spawn(move || {
                run_worker(
                    &mut store,
                    &lease,
                    &database_id,
                    lease_millis,
                    renew_interval,
                    receiver,
                    &worker_shared,
                );
            }) {
            Ok(worker) => worker,
            Err(error) => {
                shared.accepting.store(false, Ordering::Release);
                indexing.shutdown();
                let _ = index_worker.join();
                if let Ok(mut cleanup) = PostgresStore::connect_configured(&cleanup_connection) {
                    let _ = cleanup.release_lease(&cleanup_lease);
                }
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "service/spawn",
                    error.to_string(),
                ));
            }
        };
        Ok(Self {
            client: TransactionClient { sender, shared },
            recovery_stats,
            program_cache,
            worker: Some(worker),
            index_worker: Some(index_worker),
        })
    }

    pub fn client(&self) -> TransactionClient {
        self.client.clone()
    }

    pub fn recovery_stats(&self) -> RecoveryStats {
        self.recovery_stats
    }

    /// Observe the transactor's bounded compiled-program cache without
    /// reaching through the single-writer service boundary.
    pub fn program_cache_stats(&self) -> ProgramCacheStats {
        shared_program_cache_stats(&self.program_cache)
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

fn run_worker(
    store: &mut PostgresStore,
    lease: &TransactorLease,
    database_id: &str,
    lease_millis: u64,
    renew_interval: Duration,
    receiver: mpsc::Receiver<Work>,
    shared: &Shared,
) {
    let mut last_renewal = Instant::now();
    let mut pending: Option<Work> = None;
    let mut pending_was_stalled = false;
    loop {
        if !shared.accepting.load(Ordering::Acquire) {
            if let Some(work) = pending.take() {
                decrement_queued(shared);
                let _ = work.response.send(Err(shared.unavailable()));
            }
            drain_unavailable(&receiver, shared);
            break;
        }
        if shared.indexing.has_failure() {
            shared.accepting.store(false, Ordering::Release);
            continue;
        }
        if last_renewal.elapsed() >= renew_interval {
            if store.renew_lease(lease, lease_millis).is_err() {
                shared.accepting.store(false, Ordering::Release);
                // A hard-limit parked request has not been assessed and
                // cannot have reached the log. Settle it as definitely
                // unavailable before releasing ownership; dropping its
                // response channel would manufacture an UnknownOutcome and
                // leave the admission count permanently inflated.
                if let Some(work) = pending.take() {
                    decrement_queued(shared);
                    let _ = work.response.send(Err(shared.unavailable()));
                }
                drain_unavailable(&receiver, shared);
                break;
            }
            last_renewal = Instant::now();
        }
        let wait = renew_interval.saturating_sub(last_renewal.elapsed());
        let work = match pending.take() {
            Some(work) => work,
            None => match receiver.recv_timeout(wait) {
                Ok(work) => work,
                Err(mpsc::RecvTimeoutError::Timeout) => continue,
                Err(mpsc::RecvTimeoutError::Disconnected) => break,
            },
        };
        if !shared.accepting.load(Ordering::Acquire) {
            decrement_queued(shared);
            let _ = work.response.send(Err(shared.unavailable()));
            continue;
        }
        if pending_was_stalled && shared.indexing.at_hard_limit() {
            // `check_index_gate` performs the one durable idempotency lookup
            // that classified this work as novel. Do not reconnect and repeat
            // that query on every throttle tick while the same request is
            // parked; no competing writer can commit it under this lease.
            shared.indexing.wake();
            pending = Some(work);
            thread::park_timeout(wait.min(Duration::from_millis(100)));
            continue;
        }
        let gate = shared.check_index_gate(&work.request.request_key, work.request_hash);
        if gate
            .as_ref()
            .is_err_and(|error| error.code == "service/index-backpressure")
        {
            // Match the recovered transactor's hard-limit behavior: retain
            // this ordinary request without evaluating it, keep renewing the
            // lease, and let the index worker run. Bounded admission—not a new
            // transaction semantic—decides whether additional callers see
            // Busy while this slot is parked.
            if !pending_was_stalled {
                shared.indexing.record_backpressure_stall();
                pending_was_stalled = true;
            }
            shared.indexing.wake();
            pending = Some(work);
            thread::park_timeout(wait.min(Duration::from_millis(100)));
            continue;
        }
        decrement_queued(shared);
        pending_was_stalled = false;
        let result = match gate {
            Ok(()) => {
                let published_revision = shared.indexing.stats().published_revision;
                match store.adopt_published_tree(database_id, published_revision) {
                    Ok(()) => {
                        process_work(store, lease, database_id, work.request, work.request_hash)
                    }
                    // An ambiguous commit deliberately invalidates the cached
                    // writer value. The authoritative transaction path locks
                    // the head and either reconstructs the durable receipt or
                    // opens that exact head before doing new work, so absence
                    // of a process-local value is not an adoption failure.
                    Err(error) if error.code == "postgres/writer-not-activated" => {
                        process_work(store, lease, database_id, work.request, work.request_hash)
                    }
                    Err(error) => Err(error),
                }
            }
            Err(error) => Err(error),
        };
        let publish = result
            .as_ref()
            .ok()
            .filter(|report| !report.replayed)
            .cloned();
        if result.is_ok() {
            shared.processed.fetch_add(1, Ordering::Relaxed);
        } else if result
            .as_ref()
            .is_err_and(|error| error.code == "postgres/leadership-lost")
        {
            shared.accepting.store(false, Ordering::Release);
        }
        if let Some(report) = &publish {
            shared.indexing.note_commit(report.basis_t, &report.tx_data);
        } else if result
            .as_ref()
            .is_err_and(|error| error.category == ErrorCategory::UnknownOutcome)
        {
            shared.indexing.note_unknown_outcome();
        }
        *shared
            .writer_residency
            .lock()
            .expect("writer residency mutex poisoned") = store.writer_residency_stats(database_id);
        let _ = work.response.send(result);
        if let Some(report) = publish {
            shared.publish(&report);
        }
    }
    let _ = store.release_lease(lease);
    shared.accepting.store(false, Ordering::Release);
}

fn run_index_worker(
    mut indexer: PostgresIndexer,
    receiver: mpsc::Receiver<IndexCommand>,
    shared: &Shared,
) {
    let mut requested = shared.indexing.should_continue();
    loop {
        if requested && shared.accepting.load(Ordering::Acquire) {
            let began = shared.indexing.begin_job();
            if began {
                let mut reconnect_before_attempt = false;
                match retry_index_job(
                    || {
                        if reconnect_before_attempt {
                            indexer.reconnect()?;
                        }
                        let result = indexer.consolidate_with_fault(IndexBuildFault::None);
                        reconnect_before_attempt =
                            result.as_ref().is_err_and(is_postgres_connection_error);
                        result
                    },
                    || shared.accepting.load(Ordering::Acquire),
                ) {
                    Ok(receipt) => {
                        shared
                            .indexing
                            .complete_job(receipt.publication_revision, receipt.basis_t);
                        if shared.indexing.should_continue() {
                            continue;
                        }
                    }
                    Err(error) => {
                        shared.indexing.fail_job(error);
                        // A writer that can no longer consolidate its bounded
                        // recent tier must relinquish service ownership so a
                        // repaired standby can fence and recover. Retaining
                        // the lease while rejecting forever creates a zombie
                        // leader and contradicts the failover contract.
                        shared.accepting.store(false, Ordering::Release);
                        return;
                    }
                }
            }
        }
        match receiver.recv() {
            Ok(IndexCommand::Wake) => requested = true,
            Ok(IndexCommand::Shutdown) | Err(_) => return,
        }
    }
}

/// Retry a complete immutable index selection/build/publication attempt.
/// Recovered `process-request-index` retries the whole job twice after its
/// initial attempt, regardless of the failure, and stops retrying during
/// shutdown. The caller supplies the raw one-attempt indexer entry point so an
/// internal publication retry cannot silently multiply this budget.
fn retry_index_job<T>(
    mut attempt: impl FnMut() -> Result<T, SemanticError>,
    keep_running: impl Fn() -> bool,
) -> Result<T, SemanticError> {
    let mut retries = 0_usize;
    loop {
        match attempt() {
            Err(error) if keep_running() => {
                if retries == MAX_INDEX_JOB_RETRIES {
                    if !matches!(
                        error.code,
                        "tree/publication-cas-lost" | "tree/publication-revision-conflict"
                    ) {
                        return Err(error);
                    }
                    return Err(SemanticError::new(
                        ErrorCategory::Unavailable,
                        "service/index-publication-race-exhausted",
                        "background index publication exhausted its bounded CAS retry budget",
                    )
                    .detail("attempts", (retries + 1).to_string())
                    .detail("cause_code", error.code)
                    .detail("cause", error.message));
                }
                retries += 1;
                thread::yield_now();
            }
            result => return result,
        }
    }
}

fn process_work(
    store: &mut PostgresStore,
    lease: &TransactorLease,
    database_id: &str,
    request: TransactionRequest,
    request_hash: Digest,
) -> Result<ServiceTransactionReport, SemanticError> {
    let commit = store.transact_authoritative_fenced(
        lease,
        database_id,
        &request.request_key,
        request.compare_basis_t,
        &request.forms,
        request.tx_instant_override,
        request_hash,
    )?;
    Ok(ServiceTransactionReport::from_commit(commit))
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

fn load_indexing_seed(
    connection: &PostgresConnectionConfig,
    database_id: &str,
    activated_publication_revision: u64,
    activated_basis_t: u64,
) -> Result<IndexingSeed, SemanticError> {
    let mut client = connection.connect_for("service/index-seed-connect")?;
    let row = client
        .query_opt(
            "SELECT h.basis_t, h.log_generation, h.tx_hash, d.lineage_id \
               FROM atomic_heads h JOIN atomic_databases d USING (database_id) \
              WHERE h.database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("service/index-seed-head", error))?
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "postgres/database-not-found",
                format!("database {database_id} does not exist"),
            )
        })?;
    let target_basis_t = nonnegative_basis(row.get(0), "index target basis")?;
    let excision_generation = nonnegative_basis(row.get(1), "index excision generation")?;
    let target_hash = candidate_digest(row.get(2)).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Fault,
            "service/index-seed-head-hash",
            "index target head has an invalid transaction hash",
        )
    })?;
    let lineage_id: String = row.get(3);
    let newest_observed_revision = client
        .query_opt(
            "SELECT publication_revision FROM atomic_tree_publications \
             WHERE database_id = $1 ORDER BY publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("service/index-seed-revision", error))?
        .map(|row| nonnegative_basis(row.get(0), "newest observed publication revision"))
        .transpose()?
        .unwrap_or(0);

    // Activation already authenticated and pinned one immutable tree value.
    // Seed backlog accounting from exactly that publication instead of
    // implementing a second, potentially divergent candidate selector here.
    let publication = client
        .query_opt(
            "SELECT basis_t, tx_hash FROM atomic_tree_publications \
              WHERE database_id = $1 AND publication_revision = $2 \
                AND log_generation = $3",
            &[
                &database_id,
                &sql_basis(
                    activated_publication_revision,
                    "activated publication revision",
                )?,
                &sql_basis(excision_generation, "index excision generation")?,
            ],
        )
        .map_err(|error| postgres_error("service/index-seed-publication", error))?
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Fault,
                "service/index-seed-publication-missing",
                "the activated and pinned native publication disappeared",
            )
        })?;
    let published_basis_t = nonnegative_basis(publication.get(0), "published index basis")?;
    if published_basis_t != activated_basis_t || published_basis_t > target_basis_t {
        return Err(SemanticError::new(
            ErrorCategory::Fault,
            "service/index-seed-publication-mismatch",
            "the activated publication disagrees with its recovered durable basis",
        ));
    }
    let published_hash = candidate_digest(publication.get(1)).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Fault,
            "service/index-seed-publication-hash",
            "the activated publication has an invalid transaction hash",
        )
    })?;
    let published_revision = activated_publication_revision;
    let needs_publication = published_revision < newest_observed_revision;

    let rows = read_authenticated_log_range(
        &mut client,
        database_id,
        excision_generation,
        published_basis_t,
        target_basis_t,
        published_hash,
    )?;
    let observed_tail_hash = rows.last().map_or(published_hash, |row| row.tx_hash);
    let mut pending = VecDeque::with_capacity(rows.len());
    for row in rows {
        let transaction = row.transaction;
        pending.push_back(Novelty {
            basis_t: transaction.basis_t,
            datoms: transaction.tx_data.len() as u64,
            bytes: accounted_novelty_bytes(&transaction.tx_data),
        });
    }
    if observed_tail_hash != target_hash {
        return Err(SemanticError::new(
            ErrorCategory::Fault,
            "service/index-seed-head-mismatch",
            "authenticated index backlog does not reach the captured head",
        ));
    }
    Ok(IndexingSeed {
        lineage_id,
        published_revision,
        published_basis_t,
        newest_observed_revision,
        target_basis_t,
        pending,
        needs_publication,
    })
}

fn candidate_digest(bytes: Vec<u8>) -> Option<Digest> {
    bytes.try_into().ok()
}

fn sql_basis(value: u64, label: &str) -> Result<i64, SemanticError> {
    i64::try_from(value).map_err(|_| {
        SemanticError::incorrect(
            "service/index-basis-overflow",
            format!("{label} is outside PostgreSQL bigint"),
        )
    })
}

fn accounted_novelty_bytes(datoms: &[Datom]) -> u64 {
    let locator_bytes = (size_of::<crate::recent::RecentLocator>() as u64)
        .saturating_mul(MAX_RECENT_REFERENCES_PER_DATOM);
    datoms.iter().fold(0_u64, |total, datom| {
        let value_bytes = crate::encoding::encode_canonical_value(&datom.value)
            .map_or(u64::MAX, |encoded| encoded.len() as u64);
        total
            .saturating_add(datom.retained_bytes())
            .saturating_add(value_bytes)
            .saturating_add(locator_bytes)
    })
}

/// Conservative upper bound for one already-admitted transaction's recent
/// representation. Canonical transaction bytes cover every value once;
/// resident values plus the four raw indexes/log reference can account for a
/// second value copy and fixed metadata per operation. Saturation is safer
/// than wrapping a configured capacity boundary.
fn max_transaction_novelty_bytes(limits: CapacityLimits) -> u64 {
    let transaction_bytes = u64::try_from(limits.max_transaction_bytes).unwrap_or(u64::MAX);
    let operation_count = u64::try_from(limits.max_transaction_ops).unwrap_or(u64::MAX);
    let locator_bytes = (size_of::<crate::recent::RecentLocator>() as u64)
        .saturating_mul(MAX_RECENT_REFERENCES_PER_DATOM);
    transaction_bytes.saturating_mul(2).saturating_add(
        operation_count.saturating_mul((size_of::<Datom>() as u64).saturating_add(locator_bytes)),
    )
}

fn nonnegative_basis(value: i64, label: &str) -> Result<u64, SemanticError> {
    u64::try_from(value).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Fault,
            "service/index-negative-basis",
            format!("{label} is negative"),
        )
    })
}

fn duration_millis(duration: Duration) -> Result<u64, SemanticError> {
    u64::try_from(duration.as_millis()).map_err(|_| {
        SemanticError::incorrect("service/duration-overflow", "lease duration is too large")
    })
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
    use crate::Value;

    fn test_config() -> BackgroundIndexingConfig {
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1_024,
            memory_index_max_bytes: 2_048,
        }
    }

    #[test]
    fn basis_zero_bootstrap_is_publishable_and_first_threshold_crossing_advances_it() {
        let (sender, receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            test_config(),
            IndexingSeed {
                lineage_id: "lineage".to_owned(),
                published_revision: 0,
                published_basis_t: 0,
                newest_observed_revision: 0,
                target_basis_t: 0,
                pending: VecDeque::new(),
                needs_publication: true,
            },
            sender,
        );

        assert!(indexing.should_continue());
        assert!(indexing.begin_job());
        indexing.complete_job(1, 0);
        assert!(!indexing.should_continue());
        let novelty = Datom {
            entity: 1,
            attribute: 2,
            value: Value::Tuple(vec![None; 1_024]),
            tx: 3,
            added: true,
        };
        indexing.note_commit(1, &[novelty]);
        assert_eq!(receiver.try_recv(), Ok(IndexCommand::Wake));
        assert!(indexing.should_continue());
        assert!(indexing.begin_job());
        indexing.complete_job(2, 1);
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
                lineage_id: "lineage".to_owned(),
                published_revision: 0,
                published_basis_t: 0,
                newest_observed_revision: 1,
                target_basis_t: 1,
                pending: VecDeque::from([Novelty {
                    basis_t: 1,
                    datoms: 1,
                    bytes: 1_024,
                }]),
                needs_publication: true,
            },
            sender,
        );

        assert!(indexing.should_continue());
        assert!(indexing.begin_job());
        indexing.complete_job(2, 1);
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
            newest_observed_revision: 1,
            target_basis_t: 2,
            pending: VecDeque::new(),
            total_datoms: 1,
            total_bytes: config.memory_index_threshold_bytes,
            indexing_through: None,
            needs_publication: false,
        };
        assert!(!should_index(&backlog, config));
        backlog.total_bytes += 1;
        assert!(should_index(&backlog, config));

        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            config,
            IndexingSeed {
                lineage_id: "lineage".to_owned(),
                published_revision: 1,
                published_basis_t: 1,
                newest_observed_revision: 1,
                target_basis_t: 2,
                pending: VecDeque::from([Novelty {
                    basis_t: 2,
                    datoms: 1,
                    bytes: config.memory_index_max_bytes,
                }]),
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
    fn whole_index_retry_budget_is_exact_and_finite() {
        let mut attempts = 0_usize;
        let exhausted = retry_index_job(
            || {
                attempts += 1;
                Err::<(), _>(SemanticError::conflict(
                    "tree/publication-cas-lost",
                    "test contender won",
                ))
            },
            || true,
        )
        .unwrap_err();
        assert_eq!(attempts, 3);
        assert_eq!(
            (exhausted.category, exhausted.code),
            (
                ErrorCategory::Unavailable,
                "service/index-publication-race-exhausted"
            )
        );
        assert_eq!(exhausted.details.get("attempts"), Some(&"3".to_owned()));

        let mut attempts = 0_usize;
        let receipt = retry_index_job(
            || {
                attempts += 1;
                if attempts < 3 {
                    Err(SemanticError::conflict(
                        "tree/publication-revision-conflict",
                        "test contender won",
                    ))
                } else {
                    Ok(42_u64)
                }
            },
            || true,
        )
        .unwrap();
        assert_eq!((attempts, receipt), (3, 42));

        let mut attempts = 0_usize;
        let original = retry_index_job(
            || {
                attempts += 1;
                Err::<(), _>(SemanticError::new(
                    ErrorCategory::Fault,
                    "index/test-failure",
                    "test whole-index failure",
                ))
            },
            || true,
        )
        .unwrap_err();
        assert_eq!(attempts, 3, "non-CAS job failures use the same budget");
        assert_eq!(original.code, "index/test-failure");
    }

    #[test]
    fn tuple_of_nils_cannot_evade_novelty_backpressure() {
        let datom = Datom {
            entity: 1,
            attribute: 2,
            value: Value::Tuple(vec![None; 1_024]),
            tx: 3,
            added: true,
        };
        let canonical_value_bytes = crate::encoding::encode_canonical_value(&datom.value)
            .unwrap()
            .len() as u64;
        let locator_bytes = (size_of::<crate::recent::RecentLocator>() as u64)
            .saturating_mul(MAX_RECENT_REFERENCES_PER_DATOM);
        let canonical_only_account = (size_of::<Datom>() as u64)
            .saturating_add(canonical_value_bytes)
            .saturating_add(locator_bytes);
        let retained_account = accounted_novelty_bytes(std::slice::from_ref(&datom));
        assert!(retained_account > canonical_only_account);

        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing = BackgroundIndexing::new(
            BackgroundIndexingConfig {
                memory_index_threshold_bytes: 1,
                memory_index_max_bytes: canonical_only_account.saturating_add(1),
            },
            IndexingSeed {
                lineage_id: "lineage".to_owned(),
                published_revision: 1,
                published_basis_t: 1,
                newest_observed_revision: 1,
                target_basis_t: 1,
                pending: VecDeque::new(),
                needs_publication: false,
            },
            sender,
        );
        indexing.note_commit(2, &[datom]);
        let limit = indexing
            .limiting_error()
            .expect("retained tuple slots must cross the hard byte limit");
        assert_eq!(limit.code, "service/index-backpressure");
        assert_eq!(indexing.stats().total_bytes, retained_account);
    }
}
