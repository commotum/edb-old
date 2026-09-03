use crate::{
    CommitReceipt, Database, Digest, ErrorCategory, PostgresStore, SemanticError, TransactorLease,
    TxOp,
};
use std::collections::BTreeMap;
use std::sync::atomic::{AtomicBool, AtomicU64, AtomicUsize, Ordering};
use std::sync::{Arc, Mutex, Weak, mpsc};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

#[derive(Clone, Debug)]
pub struct TransactionServiceConfig {
    pub connection: String,
    pub lease_scope: String,
    pub holder_id: String,
    pub lease_duration: Duration,
    pub renew_interval: Duration,
    pub queue_capacity: usize,
}

#[derive(Clone, Debug)]
pub struct TransactionRequest {
    pub database_id: String,
    pub request_key: String,
    pub expected_basis_t: u64,
    pub operations: Vec<TxOp>,
    pub tx_instant: i64,
}

#[derive(Clone, Debug)]
pub struct ServiceTransactionReport {
    pub db_before: Database,
    pub db_after: Database,
    pub basis_t: u64,
    pub tx_hash: Digest,
    pub tx_data: Vec<crate::Datom>,
    pub tempids: BTreeMap<String, u64>,
    pub replayed: bool,
}

impl ServiceTransactionReport {
    fn from_commit(db_before: Database, commit: CommitReceipt) -> Self {
        Self {
            db_before,
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
    response: mpsc::SyncSender<Result<ServiceTransactionReport, SemanticError>>,
}

struct Shared {
    accepting: AtomicBool,
    queued: AtomicUsize,
    max_queued: AtomicUsize,
    processed: AtomicU64,
    rejected_full: AtomicU64,
    subscriber_drops: AtomicU64,
    next_subscriber: AtomicU64,
    subscribers: Mutex<BTreeMap<u64, mpsc::SyncSender<ServiceTransactionReport>>>,
}

impl Shared {
    fn new() -> Self {
        Self {
            accepting: AtomicBool::new(true),
            queued: AtomicUsize::new(0),
            max_queued: AtomicUsize::new(0),
            processed: AtomicU64::new(0),
            rejected_full: AtomicU64::new(0),
            subscriber_drops: AtomicU64::new(0),
            next_subscriber: AtomicU64::new(1),
            subscribers: Mutex::new(BTreeMap::new()),
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
        subscribers.retain(|_, sender| match sender.try_send(report.clone()) {
            Ok(()) => true,
            Err(mpsc::TrySendError::Full(_)) => {
                self.subscriber_drops.fetch_add(1, Ordering::Relaxed);
                true
            }
            Err(mpsc::TrySendError::Disconnected(_)) => false,
        });
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
        let request_key = request.request_key.clone();
        let (sender, receiver) = mpsc::sync_channel(1);
        let queued = self.shared.queued.fetch_add(1, Ordering::AcqRel) + 1;
        match self.sender.try_send(Work {
            request,
            response: sender,
        }) {
            Ok(()) => {
                self.shared.max_queued.fetch_max(queued, Ordering::Relaxed);
                Ok(TransactionTicket {
                    request_key,
                    receiver,
                })
            }
            Err(mpsc::TrySendError::Full(_)) => {
                self.shared.queued.fetch_sub(1, Ordering::AcqRel);
                self.shared.rejected_full.fetch_add(1, Ordering::Relaxed);
                Err(SemanticError::new(
                    ErrorCategory::Busy,
                    "service/queue-full",
                    "transaction service queue is full",
                ))
            }
            Err(mpsc::TrySendError::Disconnected(_)) => {
                self.shared.queued.fetch_sub(1, Ordering::AcqRel);
                Err(self.shared.unavailable())
            }
        }
    }

    pub fn transact(
        &self,
        request: TransactionRequest,
        timeout: Duration,
    ) -> Result<ServiceTransactionReport, SemanticError> {
        self.submit(request)?.wait(timeout)
    }

    pub fn subscribe_reports(&self, capacity: usize) -> Result<ReportSubscription, SemanticError> {
        if capacity == 0 {
            return Err(SemanticError::incorrect(
                "service/report-capacity",
                "report subscription capacity must be positive",
            ));
        }
        let id = self.shared.next_subscriber.fetch_add(1, Ordering::Relaxed);
        let (sender, receiver) = mpsc::sync_channel(capacity);
        self.shared
            .subscribers
            .lock()
            .expect("subscriber mutex poisoned")
            .insert(id, sender);
        Ok(ReportSubscription {
            id,
            receiver,
            shared: Arc::downgrade(&self.shared),
        })
    }

    pub fn stats(&self) -> ServiceStats {
        ServiceStats {
            queued: self.shared.queued.load(Ordering::Relaxed),
            max_queued: self.shared.max_queued.load(Ordering::Relaxed),
            processed: self.shared.processed.load(Ordering::Relaxed),
            rejected_full: self.shared.rejected_full.load(Ordering::Relaxed),
            subscriber_drops: self.shared.subscriber_drops.load(Ordering::Relaxed),
        }
    }

    pub fn is_available(&self) -> bool {
        self.shared.accepting.load(Ordering::Acquire)
    }
}

pub struct TransactionTicket {
    request_key: String,
    receiver: mpsc::Receiver<Result<ServiceTransactionReport, SemanticError>>,
}

impl TransactionTicket {
    pub fn wait(self, timeout: Duration) -> Result<ServiceTransactionReport, SemanticError> {
        match self.receiver.recv_timeout(timeout) {
            Ok(result) => result,
            Err(mpsc::RecvTimeoutError::Timeout) => Err(unknown_outcome(
                &self.request_key,
                "timed out waiting for an admitted transaction",
            )),
            Err(mpsc::RecvTimeoutError::Disconnected) => Err(unknown_outcome(
                &self.request_key,
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
    pub subscriber_drops: u64,
}

pub struct TransactionService {
    client: TransactionClient,
    worker: Option<JoinHandle<()>>,
}

impl TransactionService {
    pub fn start(config: TransactionServiceConfig) -> Result<Self, SemanticError> {
        if config.queue_capacity == 0
            || config.lease_duration.is_zero()
            || config.renew_interval.is_zero()
            || config.renew_interval >= config.lease_duration
        {
            return Err(SemanticError::incorrect(
                "service/invalid-config",
                "queue capacity must be positive and renewal must precede lease expiry",
            ));
        }
        let lease_millis = duration_millis(config.lease_duration)?;
        let mut store = PostgresStore::connect(&config.connection)?;
        store.migrate()?;
        let lease = store.acquire_lease(&config.lease_scope, &config.holder_id, lease_millis)?;
        let (sender, receiver) = mpsc::sync_channel(config.queue_capacity);
        let shared = Arc::new(Shared::new());
        let worker_shared = shared.clone();
        let renew_interval = config.renew_interval;
        let worker = thread::Builder::new()
            .name(format!("atomic-transactor-{}", config.holder_id))
            .spawn(move || {
                run_worker(
                    &mut store,
                    &lease,
                    lease_millis,
                    renew_interval,
                    receiver,
                    &worker_shared,
                );
            })
            .map_err(|error| {
                SemanticError::new(
                    ErrorCategory::Unavailable,
                    "service/spawn",
                    error.to_string(),
                )
            })?;
        Ok(Self {
            client: TransactionClient { sender, shared },
            worker: Some(worker),
        })
    }

    pub fn client(&self) -> TransactionClient {
        self.client.clone()
    }

    pub fn shutdown(mut self) {
        self.stop_and_join();
    }

    fn stop_and_join(&mut self) {
        self.client.shared.accepting.store(false, Ordering::Release);
        if let Some(worker) = self.worker.take() {
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
    lease_millis: u64,
    renew_interval: Duration,
    receiver: mpsc::Receiver<Work>,
    shared: &Shared,
) {
    let mut last_renewal = Instant::now();
    loop {
        if !shared.accepting.load(Ordering::Acquire) {
            drain_unavailable(&receiver, shared);
            break;
        }
        if last_renewal.elapsed() >= renew_interval {
            if store.renew_lease(lease, lease_millis).is_err() {
                shared.accepting.store(false, Ordering::Release);
                drain_unavailable(&receiver, shared);
                break;
            }
            last_renewal = Instant::now();
        }
        let wait = renew_interval.saturating_sub(last_renewal.elapsed());
        let work = match receiver.recv_timeout(wait) {
            Ok(work) => work,
            Err(mpsc::RecvTimeoutError::Timeout) => continue,
            Err(mpsc::RecvTimeoutError::Disconnected) => break,
        };
        shared.queued.fetch_sub(1, Ordering::AcqRel);
        if !shared.accepting.load(Ordering::Acquire) {
            let _ = work.response.send(Err(shared.unavailable()));
            continue;
        }
        let result = process_work(store, lease, work.request);
        if let Ok(report) = &result {
            shared.processed.fetch_add(1, Ordering::Relaxed);
            if !report.replayed {
                shared.publish(report);
            }
        } else if result
            .as_ref()
            .is_err_and(|error| error.code == "postgres/leadership-lost")
        {
            shared.accepting.store(false, Ordering::Release);
        }
        let _ = work.response.send(result);
    }
    let _ = store.release_lease(lease);
    shared.accepting.store(false, Ordering::Release);
}

fn process_work(
    store: &mut PostgresStore,
    lease: &TransactorLease,
    request: TransactionRequest,
) -> Result<ServiceTransactionReport, SemanticError> {
    let commit = store.transact_fenced(
        lease,
        &request.database_id,
        &request.request_key,
        request.expected_basis_t,
        &request.operations,
        request.tx_instant,
    )?;
    let db_before = store.recover_basis(&request.database_id, commit.basis_t.saturating_sub(1))?;
    Ok(ServiceTransactionReport::from_commit(db_before, commit))
}

fn drain_unavailable(receiver: &mpsc::Receiver<Work>, shared: &Shared) {
    while let Ok(work) = receiver.try_recv() {
        shared.queued.fetch_sub(1, Ordering::AcqRel);
        let _ = work.response.send(Err(shared.unavailable()));
    }
}

fn duration_millis(duration: Duration) -> Result<u64, SemanticError> {
    u64::try_from(duration.as_millis()).map_err(|_| {
        SemanticError::incorrect("service/duration-overflow", "lease duration is too large")
    })
}

fn unknown_outcome(request_key: &str, message: &str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::UnknownOutcome,
        "service/unknown-outcome",
        message,
    )
    .detail("request_key", request_key)
}
