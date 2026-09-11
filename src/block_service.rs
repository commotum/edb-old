//! Serialized block authority with one bounded asynchronous index preparer.
use super::*;
use crate::persistent_tree::TreeConfig;
use crate::storage::indexing::{IndexInput, PreparedIndex};
use crate::storage::{
    BlockDatabase, BlockReadConfig, BlockTransactor, BlockWriterOptions, PgBlockStore,
};

const MAINTENANCE_POLL: Duration = Duration::from_millis(25);

#[cfg(test)]
type PreparationPause = (mpsc::SyncSender<()>, mpsc::Receiver<()>);

#[cfg(test)]
fn preparation_pauses() -> &'static Mutex<BTreeMap<String, PreparationPause>> {
    static PAUSES: std::sync::OnceLock<Mutex<BTreeMap<String, PreparationPause>>> =
        std::sync::OnceLock::new();
    PAUSES.get_or_init(|| Mutex::new(BTreeMap::new()))
}

#[cfg(test)]
pub(super) fn pause_preparation(identity: &str) -> (mpsc::Receiver<()>, mpsc::SyncSender<()>) {
    let (ready, received) = mpsc::sync_channel(1);
    let (release, wait) = mpsc::sync_channel(1);
    preparation_pauses()
        .lock()
        .unwrap()
        .insert(identity.into(), (ready, wait));
    (received, release)
}

#[cfg(test)]
fn pause_prepared(shared: &Shared) {
    let pause = preparation_pauses()
        .lock()
        .unwrap()
        .remove(&shared.database_id);
    if let Some((ready, release)) = pause {
        let _ = ready.try_send(());
        while shared.accepting.load(Ordering::Acquire) {
            match release.recv_timeout(MAINTENANCE_POLL) {
                Err(mpsc::RecvTimeoutError::Timeout) => {}
                _ => break,
            }
        }
    }
}

pub(super) fn connection_unavailable(error: &SemanticError) -> bool {
    (error.category == ErrorCategory::Unavailable && error.code == "storage/postgres")
        || is_postgres_connection_error(error)
}

pub(super) fn start(
    config: TransactionServiceConfig,
    options: ServiceOptions,
    database: BlockDatabase,
) -> Result<TransactionService, SemanticError> {
    let connection = config.connection.clone();
    TransactionService::validate_config(&config)?;
    let indexing_config = options.indexing.validate()?;
    options.excision.validate()?;
    options.fulltext_build_limits.validate()?;
    let hint_worker = crate::transaction_hints::HintWorkerSlot::new(options.hint_prefetch)?;
    crate::tree_read::validate_index_preparation_parallelism(
        options.index_preparation_parallelism,
    )?;
    let reads = BlockReadConfig {
        cache_entries: config.capacity_limits.writer_tree_cache_entries,
        cache_bytes: config.capacity_limits.writer_tree_cache_bytes,
        // Committed data remains readable after a restart with smaller local
        // thresholds. Receipt-first fresh admission remains byte-bounded.
        ..BlockReadConfig::default()
    };
    let mut writer = BlockTransactor::claim(
        &connection,
        database.clone(),
        BlockWriterOptions {
            lease_duration: config.lease_duration,
            capacity: config.capacity_limits,
            reads,
            execution: options.execution,
        },
    )?;
    let (recovery_stats, residency) = match writer.activate() {
        Ok(stats) => stats,
        Err(mut error) => {
            let _ = writer.release();
            if error.category == ErrorCategory::Fault {
                error.message.push_str(
                    "; inspect the database before retrying. If only its derived current index is damaged, use atomic consolidate to rebuild it from the authenticated log; canonical or retained-receipt damage requires separate recovery",
                );
            }
            return Err(error);
        }
    };
    let identity = crate::storage::engine::identity_string(database.identity);
    let route = crate::storage::engine::identity_string(database.route);
    // Diagnostic carrier only; BlockTransactor owns the opaque lease token.
    let transport_lease = TransactorLease {
        database_id: route.clone(),
        holder_id: config.holder_id.clone(),
        epoch: writer.writer_epoch(),
    };
    let (index_sender, index_wakes) = mpsc::sync_channel(1);
    let pending = writer
        .index_backlog()
        .into_iter()
        .map(|entry| Novelty {
            basis_t: entry.basis_t,
            datoms: entry.datoms,
            bytes: entry.accounted_bytes,
        })
        .collect();
    let pending_avet = writer.pending_avet_projections();
    let indexing = Arc::new(BackgroundIndexing::new(
        indexing_config,
        IndexingSeed {
            published_revision: residency.publication_revision,
            published_basis_t: recovery_stats.base_t,
            pending_avet_projections: pending_avet,
            newest_observed_revision: residency.publication_revision,
            target_basis_t: recovery_stats.target_t,
            pending,
            publication_work_through: (pending_avet != 0).then_some(recovery_stats.base_t),
            required_publication_t: if pending_avet != 0 {
                recovery_stats.target_t
            } else {
                0
            },
            needs_publication: pending_avet != 0,
        },
        index_sender,
    ));
    let mut shared = Shared::new(
        crate::storage::log::MAX_LOG_TRANSACTION_BYTES,
        residency,
        indexing,
        route,
        identity,
        transport_lease,
        database.identity,
    );
    shared.hint_worker = hint_worker;
    shared.program_cache = writer.program_cache();
    shared.telemetry = options.telemetry;
    let shared = Arc::new(shared);
    let (jobs, job_receiver) = mpsc::sync_channel(1);
    let (completed, completions) = mpsc::sync_channel(1);
    let index_shared = Arc::clone(&shared);
    let index_connection = connection.clone();
    let preparation_parallelism = options.index_preparation_parallelism;
    let excision_config = options.excision;
    let fulltext_limits = options.fulltext_build_limits;
    let index_fulltext_limits = fulltext_limits.clone();
    let index_worker = match thread::Builder::new()
        .name(format!("atomic-index-{}", config.holder_id))
        .spawn(move || {
            prepare_worker(
                index_connection,
                job_receiver,
                completed,
                preparation_parallelism,
                index_fulltext_limits,
                &index_shared,
            )
        }) {
        Ok(worker) => worker,
        Err(error) => {
            let _ = writer.release();
            return Err(spawn_error(error));
        }
    };
    let (sender, receiver) = mpsc::sync_channel(config.queue_capacity);
    // A failed spawn still leaves the real lease available for cleanup.
    let owner = Arc::new(Mutex::new(Some(writer)));
    let worker_owner = Arc::clone(&owner);
    let worker_shared = Arc::clone(&shared);
    let worker = match thread::Builder::new()
        .name(format!("atomic-transactor-{}", config.holder_id))
        .spawn(move || {
            let writer = worker_owner
                .lock()
                .expect("writer startup mutex poisoned")
                .take()
                .expect("one writer thread");
            run(
                writer,
                connection,
                config.renew_interval,
                receiver,
                IndexLane {
                    jobs,
                    completions,
                    wakes: index_wakes,
                    failures: 0,
                    retry_at: None,
                },
                excision_config,
                fulltext_limits,
                config.queue_capacity,
                &worker_shared,
            );
        }) {
        Ok(worker) => worker,
        Err(error) => {
            shared.accepting.store(false, Ordering::Release);
            if let Some(writer) = owner.lock().expect("writer startup mutex poisoned").take() {
                let _ = writer.release();
            }
            let _ = index_worker.join();
            return Err(spawn_error(error));
        }
    };
    Ok(TransactionService {
        client: TransactionClient { sender, shared },
        recovery_stats,
        worker: Some(worker),
        index_worker: Some(index_worker),
    })
}

fn spawn_error(error: std::io::Error) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "service/spawn",
        error.to_string(),
    )
}

fn refresh_progress(writer: &BlockTransactor, shared: &Shared) {
    *shared
        .writer_residency
        .lock()
        .expect("writer residency mutex poisoned") = writer.writer_residency_stats();
}

struct IndexLane {
    jobs: mpsc::SyncSender<IndexInput>,
    completions: mpsc::Receiver<Result<PreparedIndex, SemanticError>>,
    wakes: mpsc::Receiver<IndexCommand>,
    failures: usize,
    retry_at: Option<Instant>,
}

fn prepare_worker(
    connection: PostgresConnectionConfig,
    jobs: mpsc::Receiver<IndexInput>,
    completed: mpsc::SyncSender<Result<PreparedIndex, SemanticError>>,
    parallelism: usize,
    fulltext_limits: crate::FulltextBuildLimits,
    shared: &Shared,
) {
    let operation = OperationContext::new(OperationKind::Indexing);
    let _scope = operation.enter();
    while shared.accepting.load(Ordering::Acquire) {
        let input = match jobs.recv_timeout(MAINTENANCE_POLL) {
            Ok(input) => input,
            Err(mpsc::RecvTimeoutError::Timeout) => continue,
            Err(mpsc::RecvTimeoutError::Disconnected) => break,
        };
        shared.indexing.projection_started(input.through_basis());
        let result = (|| {
            // Never borrow the writer's reader/SQL mutex while doing preparation.
            let mut store = PgBlockStore::connect(&connection)?;
            input
                .with_fulltext_build_limits(fulltext_limits.clone())?
                .prepare_with_parallelism(
                    &mut store,
                    &TreeConfig::default(),
                    parallelism,
                    &mut || {
                        if shared.accepting.load(Ordering::Acquire) {
                            Ok(())
                        } else {
                            Err(SemanticError::new(
                                ErrorCategory::Interrupted,
                                "service/index-cancelled",
                                "Index preparation stopped before publication",
                            ))
                        }
                    },
                )
        })();
        #[cfg(test)]
        if result.is_ok() {
            pause_prepared(shared);
        }
        // Exactly one job is in flight. Nonblocking delivery also permits
        // shutdown while the writer no longer services this mailbox.
        if completed.try_send(result).is_err() {
            break;
        }
    }
}

impl IndexLane {
    fn failed(&mut self, error: SemanticError, shared: &Shared) {
        if !shared.accepting.load(Ordering::Acquire) {
            return;
        }
        self.failures += 1;
        if self.failures > MAX_INDEX_JOB_RETRIES || error.code == "storage/writer-fenced" {
            self.retry_at = None;
            shared.indexing.projection_failed(&error, None, true);
            shared.indexing.fail_job(error);
            shared.accepting.store(false, Ordering::Release);
        } else {
            self.retry_at = Some(Instant::now() + Duration::from_millis(50 * self.failures as u64));
            shared
                .indexing
                .projection_failed(&error, self.retry_at, false);
            shared.indexing.retry_job();
        }
    }

    fn poll(
        &mut self,
        writer: &mut BlockTransactor,
        connection: &PostgresConnectionConfig,
        shared: &Shared,
    ) {
        while let Ok(command) = self.wakes.try_recv() {
            if command == IndexCommand::Shutdown {
                return;
            }
        }
        match self.completions.try_recv() {
            Ok(Ok(prepared)) => {
                let descriptor_id = prepared.descriptor_id;
                let mut adopted = writer.adopt_index(prepared);
                if adopted
                    .as_ref()
                    .is_err_and(|error| error.category == ErrorCategory::UnknownOutcome)
                {
                    adopted = (|| {
                        writer.reconnect(connection)?;
                        writer.activate()?;
                        if writer.current_index_id() != Some(descriptor_id) {
                            return Err(SemanticError::conflict(
                                "service/index-not-observed",
                                "Uncertain index adoption is absent from the current root",
                            ));
                        }
                        Ok(crate::storage::engine::IndexAdoption {
                            indexed_basis: writer.indexed_basis_t(),
                            publication_revision: writer
                                .writer_residency_stats()
                                .publication_revision,
                            pending_avet: writer.pending_avet_projections(),
                        })
                    })();
                }
                match adopted {
                    Ok(receipt) => {
                        shared.indexing.projection_adopted(receipt.indexed_basis);
                        shared.indexing.complete_job(
                            receipt.publication_revision,
                            receipt.indexed_basis,
                            receipt.pending_avet as usize,
                            receipt.pending_avet != 0,
                        );
                        refresh_progress(writer, shared);
                        crate::change_notices::publish(connection, &shared.database_id);
                        self.failures = 0;
                        self.retry_at = None;
                    }
                    Err(error) if error.code == "storage/index-source-changed" => {
                        // An explicit operator may publish a newer derived
                        // index without replacing this writer. Discard stale
                        // work and recapture; it is not a failed writer/job.
                        self.failures = 0;
                        self.retry_at = None;
                        shared.indexing.retry_job();
                    }
                    Err(error) => self.failed(error, shared),
                }
            }
            Ok(Err(error)) => self.failed(error, shared),
            Err(mpsc::TryRecvError::Disconnected) => {
                if shared.accepting.load(Ordering::Acquire) {
                    let error = SemanticError::new(
                        ErrorCategory::Unavailable,
                        "service/index-worker-stopped",
                        "Index preparation worker disconnected",
                    );
                    shared.indexing.projection_failed(&error, None, true);
                    shared.indexing.fail_job(error);
                    shared.accepting.store(false, Ordering::Release);
                }
            }
            Err(mpsc::TryRecvError::Empty) => {}
        }
        if !shared.accepting.load(Ordering::Acquire)
            || self.retry_at.is_some_and(|at| Instant::now() < at)
        {
            return;
        }
        if let Some(through) = shared.indexing.begin_job() {
            let sent = writer.index_input().and_then(|input| {
                self.jobs.try_send(input).map_err(|_| {
                    SemanticError::new(
                        ErrorCategory::Fault,
                        "service/index-mailbox",
                        "The single preparation slot was unexpectedly unavailable",
                    )
                })
            });
            if let Err(error) = sent {
                // No preparer received this attempt, so record its requested
                // basis here rather than silently reporting an idle failure.
                shared.indexing.projection_started(through);
                self.failed(error, shared);
            }
        }
    }
}

struct Ambiguous {
    request_key: String,
    request_hash: Digest,
    operation: OperationContext,
    notify: bool,
}

// The single-slot channel transfers one owned candidate; keep it inline without another allocation.
#[allow(clippy::large_enum_variant)]
enum ExcisionEvent {
    Admitted(crate::storage::root::DatabaseRoot, mpsc::SyncSender<bool>),
    Progress(crate::ExcisionProgress),
    Finished(Result<Option<crate::storage::excision::PreparedExcision>, SemanticError>),
}

/// One independent maintenance worker, one result slot and a fresh-admission
/// gate acknowledged only after the serialized writer validates its source.
struct ExcisionLane {
    config: crate::ExcisionConfig,
    fulltext_limits: crate::FulltextBuildLimits,
    events: Option<mpsc::Receiver<ExcisionEvent>>,
    worker: Option<JoinHandle<()>>,
    cancel: Arc<AtomicBool>,
    active: bool,
    attempted_basis: Option<u64>,
    retry_at: Option<Instant>,
    pause_started: Option<Instant>,
}
impl ExcisionLane {
    fn new(config: crate::ExcisionConfig, fulltext_limits: crate::FulltextBuildLimits) -> Self {
        Self {
            config,
            fulltext_limits,
            events: None,
            worker: None,
            cancel: Arc::new(AtomicBool::new(false)),
            active: false,
            attempted_basis: None,
            retry_at: None,
            pause_started: None,
        }
    }
    fn busy(&self) -> bool {
        self.events.is_some()
    }
    fn failed(&mut self, error: SemanticError, shared: &Shared) {
        if error.category == ErrorCategory::Conflict || error.category == ErrorCategory::Unavailable
        {
            self.retry_at = Some(Instant::now() + Duration::from_millis(100));
            self.attempted_basis = None;
        }
        shared
            .indexing
            .excision
            .lock()
            .expect("excision status mutex poisoned")
            .1 = Some(error);
        self.active = false;
    }
    fn poll(
        &mut self,
        writer: &mut BlockTransactor,
        connection: &PostgresConnectionConfig,
        shared: &Shared,
    ) {
        let event = match self.events.as_ref().map(|events| events.try_recv()) {
            Some(Ok(event)) => Some(event),
            Some(Err(mpsc::TryRecvError::Disconnected)) => {
                self.events = None;
                self.failed(
                    SemanticError::new(
                        ErrorCategory::Unavailable,
                        "excision/worker-stopped",
                        "Excision worker stopped before completion",
                    ),
                    shared,
                );
                None
            }
            _ => None,
        };
        if let Some(event) = event {
            match event {
                ExcisionEvent::Admitted(source, permit) => match writer.admit_excision(&source) {
                    Ok(()) => {
                        self.active = true;
                        self.pause_started = Some(Instant::now());
                        let _ = permit.try_send(true);
                    }
                    Err(error) => {
                        let _ = permit.try_send(false);
                        self.failed(error, shared);
                    }
                },
                ExcisionEvent::Progress(progress) => {
                    let mut state = shared
                        .indexing
                        .excision
                        .lock()
                        .expect("excision status mutex poisoned");
                    state.0 = progress;
                    state.1 = None;
                }
                ExcisionEvent::Finished(result) => {
                    self.events = None;
                    match result {
                        Ok(Some(prepared)) => {
                            let expected_metadata = prepared.candidate.metadata;
                            let mut progress = prepared.progress.clone();
                            let result = writer.adopt_excision(prepared).or_else(|error| {
                                if error.category != ErrorCategory::UnknownOutcome {
                                    return Err(error);
                                }
                                writer.reconnect(connection)?;
                                writer.activate()?;
                                let snapshot = writer
                                    .hint_database_value()
                                    .and_then(|v| v.block_snapshot());
                                if snapshot
                                    .as_ref()
                                    .is_none_or(|s| s.captured_root().metadata != expected_metadata)
                                {
                                    return Err(error);
                                }
                                Ok(crate::storage::engine::IndexAdoption {
                                    indexed_basis: writer.indexed_basis_t(),
                                    publication_revision: writer
                                        .writer_residency_stats()
                                        .publication_revision,
                                    pending_avet: writer.pending_avet_projections(),
                                })
                            });
                            match result {
                                Ok(receipt) => {
                                    progress.phase = "complete";
                                    progress.complete = true;
                                    progress.fresh_write_pause_nanos =
                                        self.pause_started.map_or(0, |s| {
                                            s.elapsed().as_nanos().min(u128::from(u64::MAX)) as u64
                                        });
                                    *shared
                                        .indexing
                                        .excision
                                        .lock()
                                        .expect("excision status mutex poisoned") =
                                        (progress, None);
                                    shared.indexing.publish_completed(
                                        receipt.publication_revision,
                                        receipt.indexed_basis,
                                        receipt.pending_avet as usize,
                                        false,
                                    );
                                    refresh_progress(writer, shared);
                                    crate::change_notices::publish(connection, &shared.database_id);
                                    self.attempted_basis = None;
                                }
                                Err(error) => self.failed(error, shared),
                            }
                        }
                        Ok(None) => {}
                        Err(error) => self.failed(error, shared),
                    }
                    self.active = false;
                    self.pause_started = None;
                }
            }
        }
        if self.worker.as_ref().is_some_and(|w| w.is_finished()) {
            let _ = self.worker.take().unwrap().join();
            // A terminal event can still occupy the bounded result slot.
        }
        if !self.config.enabled
            || self.worker.is_some()
            || self.events.is_some()
            || shared.indexing.stats().job_in_flight
            || self.retry_at.is_some_and(|at| Instant::now() < at)
        {
            return;
        }
        let basis = writer.hint_database_value().map(|v| v.basis_t());
        if self.attempted_basis.is_some()
            && !shared.indexing.excision_requested.load(Ordering::Acquire)
        {
            return;
        }
        shared
            .indexing
            .excision_requested
            .store(false, Ordering::Release);
        self.attempted_basis = basis;
        self.retry_at = None;
        let config = self.config;
        let fulltext_limits = self.fulltext_limits.clone();
        let database = writer.database().clone();
        let connection = connection.clone();
        let cancel = Arc::new(AtomicBool::new(false));
        self.cancel = cancel.clone();
        let (send, events) = mpsc::sync_channel(1);
        let operation = OperationContext::current_or_process();
        match thread::Builder::new()
            .name("atomic-block-excision".into())
            .spawn(move || {
                let _scope = operation.enter();
                let mut control = || {
                    if cancel.load(Ordering::Acquire) {
                        Err(SemanticError::new(
                            ErrorCategory::Interrupted,
                            "excision/stopped",
                            "Excision stopped at a durable checkpoint",
                        ))
                    } else {
                        Ok(())
                    }
                };
                let result = (|| {
                    let mut store = PgBlockStore::connect(&connection)?;
                    let reader = crate::storage::BlockReader::connect(
                        &connection,
                        BlockReadConfig {
                            cache_bytes: (config.max_admitted_bytes / 16).min(64 * 1024 * 1024)
                                as usize,
                            max_recent_bytes: usize::try_from(config.max_admitted_bytes / 4)
                                .unwrap_or(usize::MAX),
                            ..Default::default()
                        },
                    )?;
                    let Some(mut job) =
                        crate::storage::excision::ExcisionJob::open_with_fulltext_limits(
                            reader,
                            &mut store,
                            database,
                            config,
                            fulltext_limits,
                            &mut control,
                        )?
                    else {
                        return Ok(None);
                    };
                    let (permit, allowed) = mpsc::sync_channel(1);
                    send.send(ExcisionEvent::Admitted(job.source(&mut store)?, permit))
                        .map_err(|_| {
                            SemanticError::new(
                                ErrorCategory::Interrupted,
                                "excision/stopped",
                                "Excision owner stopped",
                            )
                        })?;
                    loop {
                        control()?;
                        match allowed.recv_timeout(MAINTENANCE_POLL) {
                            Ok(true) => break,
                            Err(mpsc::RecvTimeoutError::Timeout) => {}
                            _ => {
                                return Err(SemanticError::conflict(
                                    "excision/source-changed",
                                    "Source changed during admission",
                                ));
                            }
                        }
                    }
                    let _ = send.try_send(ExcisionEvent::Progress(job.progress()));
                    while !job.step(&mut store, &mut control)? {
                        let _ = send.try_send(ExcisionEvent::Progress(job.progress()));
                    }
                    job.prepared(&mut store).map(Some)
                })();
                let _ = send.send(ExcisionEvent::Finished(result));
            }) {
            Ok(worker) => {
                self.worker = Some(worker);
                self.events = Some(events);
            }
            Err(error) => self.failed(spawn_error(error), shared),
        }
    }
}
impl Drop for ExcisionLane {
    fn drop(&mut self) {
        self.cancel.store(true, Ordering::Release);
        self.events = None;
        if let Some(worker) = self.worker.take() {
            let _ = worker.join();
        }
    }
}

// This worker boundary transfers its transport, queues, maintenance lanes and shared observation state.
#[allow(clippy::too_many_arguments)]
fn run(
    mut writer: BlockTransactor,
    connection: PostgresConnectionConfig,
    renew_interval: Duration,
    receiver: mpsc::Receiver<Work>,
    mut index: IndexLane,
    excision_config: crate::ExcisionConfig,
    fulltext_limits: crate::FulltextBuildLimits,
    queue_capacity: usize,
    shared: &Shared,
) {
    let maintenance = OperationContext::new(OperationKind::WriterMaintenance);
    let _maintenance_scope = maintenance.enter();
    let mut renewed_at = Instant::now();
    let mut ambiguous: Option<Ambiguous> = None;
    let mut parked: Option<Work> = None;
    let mut excision = ExcisionLane::new(excision_config, fulltext_limits);
    let mut excision_parked = VecDeque::<Work>::new();
    loop {
        if !shared.accepting.load(Ordering::Acquire) {
            break;
        }
        // Publication uncertainty outranks every maintenance/result/user item.
        if let Some(pending) = ambiguous.take() {
            let _operation = pending.operation.enter();
            let result = writer.reconnect(&connection).and_then(|()| {
                writer.resolve_request_outcome(&pending.request_key, pending.request_hash)
            });
            match result {
                Ok(report) => {
                    if let Some(mut report) = report
                        && pending.notify
                    {
                        report.replayed = false;
                        record_transaction_diagnostics(shared, &mut report, &pending.operation);
                        if let Err(error) = note_report_commit(shared, &shared.database_id, &report)
                        {
                            shared.indexing.fail_job(error);
                            shared.accepting.store(false, Ordering::Release);
                        }
                        shared.publish(&report);
                        crate::change_notices::publish(&connection, &shared.database_id);
                    }
                    if writer.activate().and_then(|_| writer.renew()).is_err() {
                        shared.accepting.store(false, Ordering::Release);
                    } else {
                        refresh_progress(&writer, shared);
                    }
                    renewed_at = Instant::now();
                }
                Err(error)
                    if connection_unavailable(&error)
                        || error.category == ErrorCategory::UnknownOutcome =>
                {
                    ambiguous = Some(pending);
                    thread::park_timeout(renew_interval.min(MAINTENANCE_POLL));
                }
                Err(_) => shared.accepting.store(false, Ordering::Release),
            }
            continue;
        }
        if renewed_at.elapsed() >= renew_interval {
            if writer.renew().is_err() {
                shared.accepting.store(false, Ordering::Release);
                continue;
            }
            renewed_at = Instant::now();
        }
        // Dedicated result slot is never behind a full queue or parked request.
        if !excision.busy() {
            index.poll(&mut writer, &connection, shared);
        }
        excision.poll(&mut writer, &connection, shared);
        if !shared.accepting.load(Ordering::Acquire) {
            continue;
        }
        if !excision.active && parked.is_some() && shared.indexing.should_continue() {
            thread::park_timeout(MAINTENANCE_POLL);
            continue;
        }
        let ready = if excision.active {
            None
        } else {
            excision_parked.pop_front().or_else(|| parked.take())
        };
        let work = match ready {
            Some(work) => work,
            None => match receiver.recv_timeout(
                renew_interval
                    .saturating_sub(renewed_at.elapsed())
                    .min(MAINTENANCE_POLL),
            ) {
                Ok(work) => work,
                Err(mpsc::RecvTimeoutError::Timeout) => continue,
                Err(mpsc::RecvTimeoutError::Disconnected) => break,
            },
        };
        let _operation = work.operation.enter();
        // This pure retained-state check is only acted on by the engine's
        // post-receipt gate. A matching retry never inherits fresh-work limits.
        let tail_admission = writer.fresh_tail_admission();
        let mut dequeued = false;
        let mut result = crate::transaction_hints::overlap(
            writer.hint_database_value(),
            work.hints.as_ref(),
            &shared.hint_worker,
            || {
                writer.transact_with_fresh_gate(&work.request, || {
                    if excision.active {
                        return Err(SemanticError::new(
                            ErrorCategory::Busy,
                            "service/excision-in-progress",
                            "Fresh writes wait for the admitted excision generation",
                        ));
                    }
                    tail_admission?;
                    if let Some(error) = shared.indexing.limiting_error() {
                        return Err(error);
                    }
                    decrement_queued(shared);
                    dequeued = true;
                    Ok(())
                })
            },
        );
        if result
            .as_ref()
            .is_err_and(|e| e.code == "service/excision-in-progress")
            && excision_parked.len() < queue_capacity
        {
            // At most one configured queue of parked work. Receipt lookups
            // continue ahead of this fresh-only gate; overflow returns Busy.
            excision_parked.push_back(work);
            continue;
        }
        if result
            .as_ref()
            .is_err_and(|error| error.code == "service/index-backpressure")
            && shared.indexing.force_publication()
        {
            shared.indexing.record_backpressure_stall();
            parked = Some(work);
            continue;
        }
        if !dequeued {
            decrement_queued(shared);
        }
        if let Ok(report) = &mut result {
            record_transaction_diagnostics(shared, report, &work.operation);
            shared.processed.fetch_add(1, Ordering::Relaxed);
            if !report.replayed
                && let Err(error) = note_report_commit(shared, &shared.database_id, report)
            {
                shared.indexing.fail_job(error);
                shared.accepting.store(false, Ordering::Release);
            }
        }
        let publish = result
            .as_ref()
            .ok()
            .filter(|report| !report.replayed)
            .cloned();
        if result.as_ref().is_err_and(|error| {
            error.code == "storage/writer-fenced" || connection_unavailable(error)
        }) {
            shared.accepting.store(false, Ordering::Release);
        }
        if let Err(error) = &result
            && error.category == ErrorCategory::UnknownOutcome
        {
            ambiguous = Some(Ambiguous {
                request_key: work.request.request_key.clone(),
                request_hash: work.request_hash,
                operation: work.operation.clone(),
                notify: error
                    .details
                    .get("ambiguity_kind")
                    .is_some_and(|kind| kind == "publication"),
            });
        }
        refresh_progress(&writer, shared);
        let _ = work.response.send(result);
        if let Some(report) = publish {
            shared.publish(&report);
            crate::change_notices::publish(&connection, &shared.database_id);
        }
    }
    shared.accepting.store(false, Ordering::Release);
    if let Some(work) = parked {
        decrement_queued(shared);
        let _ = work.response.send(Err(shared.unavailable()));
    }
    drain_unavailable(&receiver, shared);
    for work in excision_parked {
        decrement_queued(shared);
        let _ = work.response.send(Err(shared.unavailable()));
    }
    drop(excision);
    // Dropping the job sender wakes the idle worker; active preparation polls
    // accepting and cannot publish. Its result owns only protected candidates.
    drop(index);
    let _ = writer.release();
}
