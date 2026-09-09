use crate::peer::Peer;
use crate::{
    DatabaseValue, Datom, Entity, EntityIdentifier, IndexBoundary, IndexOrder, IndexPrefix,
    PostgresConnectionConfig, PullPattern, Query, QueryControl, QueryExtensions, QueryInput,
    QueryOutcome, QueryValue, SemanticError, ServiceTransactionReport, TransactionClient,
    TransactionRequest, TransactionService, TransactionServiceConfig, TransactionTicket,
};
use std::sync::{Arc, Mutex, mpsc};
use std::thread::JoinHandle;
use std::time::{Duration, Instant};

type NoticeChannel = (
    mpsc::SyncSender<Arc<ServiceTransactionReport>>,
    mpsc::Receiver<Arc<ServiceTransactionReport>>,
);

/// Stable identity of one logical database catalog entry.
///
/// A database name is an address chosen by an operator.  The lineage is the
/// durable identity behind that address and prevents a long-lived connection
/// from silently retargeting if a catalog entry is dropped and recreated.
#[derive(Clone, Debug, Eq, Hash, Ord, PartialEq, PartialOrd)]
pub struct DatabaseIdentity {
    database_id: String,
    lineage_id: String,
}

impl DatabaseIdentity {
    pub(crate) fn new(database_id: impl Into<String>, lineage_id: impl Into<String>) -> Self {
        Self {
            database_id: database_id.into(),
            lineage_id: lineage_id.into(),
        }
    }

    pub fn database_id(&self) -> &str {
        &self.database_id
    }

    pub fn lineage_id(&self) -> &str {
        &self.lineage_id
    }
}

struct ConnectionCore {
    identity: DatabaseIdentity,
    peer: Peer,
    client: Mutex<Option<TransactionClient>>,
    observer: Mutex<Option<crate::service::NativeObserver>>,
    notices: mpsc::SyncSender<Arc<ServiceTransactionReport>>,
    observation_error: Arc<Mutex<Option<SemanticError>>>,
    _advancement: Advancement,
    // The embedded service owns the single-writer lease and both worker
    // lifetimes. It is intentionally retained by the last Connection clone.
    _service: Option<TransactionService>,
}

struct Advancement {
    stop: mpsc::Sender<()>,
    worker: Option<JoinHandle<()>>,
}

impl Drop for Advancement {
    fn drop(&mut self) {
        let _ = self.stop.send(());
        if let Some(worker) = self.worker.take()
            && worker.is_finished()
        {
            let _ = worker.join();
        }
        // A pending peer read may be waiting on SQL or another snapshot's I/O.
        // Request stop without making dropping a read handle wait for it. The
        // worker owns only one peer, releases it when that operation returns,
        // and never starts another iteration after observing stop. Its pins
        // legitimately remain live while the in-flight read is using them.
    }
}

/// Cloneable, long-lived native connection to one PostgreSQL database.
///
/// Like recovered `datomic.peer.Connection`, the mutable connection is only
/// an observation/co-ordination point. Every `db` call returns an immutable
/// value, and advancing this shared connection never changes an older value.
/// Reads follow the authoritative PostgreSQL log independently of the writer.
/// An attached TransactionClient is an in-process submission endpoint; opening
/// a read connection does not start or retain a transaction service.
#[derive(Clone)]
pub struct Connection {
    core: Arc<ConnectionCore>,
}

impl Connection {
    /// Open an independently advancing native read connection, without a writer.
    pub fn connect(
        connection: &str,
        database_id: impl Into<String>,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured(
            PostgresConnectionConfig::plaintext(connection),
            database_id,
            cache_capacity,
        )
    }

    pub fn connect_configured(
        connection: PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_cache_limits(
            connection,
            database_id,
            cache_capacity,
            cache_capacity.saturating_mul(512 * 1024),
        )
    }

    /// Open a writer-independent read connection with separate immutable-node
    /// cache entry and accounted-byte limits. Zero disables cache retention.
    /// These limits do not cap recent information, resident roots/schema,
    /// caller-retained values, or total process memory.
    pub fn connect_with_cache_limits(
        connection: &str,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_cache_limits(
            PostgresConnectionConfig::plaintext(connection),
            database_id,
            cache_entries,
            cache_bytes,
        )
    }

    /// Open with explicit transport/I/O policy and independent cache limits.
    /// As with `connect_configured`, the configuration is owned by the reader;
    /// opening it does not acquire or retain a writer's lease.
    pub fn connect_configured_with_cache_limits(
        connection: PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
    ) -> Result<Self, SemanticError> {
        let peer = Peer::connect_configured_with_cache_limits(
            &connection,
            database_id,
            cache_entries,
            cache_bytes,
        )?;
        Self::from_peer(
            peer,
            None,
            None,
            None,
            Arc::new(Mutex::new(None)),
            mpsc::sync_channel(1),
        )
    }

    /// Attach to an existing writer without owning its lifetime or lease.
    pub fn attach(
        connection: &str,
        client: TransactionClient,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        Self::attach_configured(
            PostgresConnectionConfig::plaintext(connection),
            client,
            cache_capacity,
        )
    }

    pub fn attach_configured(
        connection: PostgresConnectionConfig,
        client: TransactionClient,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        Self::open_attached(connection, client, cache_capacity, None)
    }

    fn open_attached(
        connection: PostgresConnectionConfig,
        client: TransactionClient,
        cache_capacity: usize,
        service: Option<TransactionService>,
    ) -> Result<Self, SemanticError> {
        let identity = client.identity();
        let observation_error = Arc::new(Mutex::new(None));
        let notices = mpsc::sync_channel(1);
        let (peer, observer) = client.observe_commits(|| {
            let peer =
                Peer::connect_configured(&connection, identity.database_id(), cache_capacity)?;
            if peer.identity() != identity {
                return Err(identity_mismatch(&identity, &peer.identity()));
            }
            let callback = observer_for(&notices.0);
            Ok((peer, callback))
        })?;
        Self::from_peer(
            peer,
            Some(client),
            Some(observer),
            service,
            observation_error,
            notices,
        )
    }

    fn from_peer(
        peer: Peer,
        client: Option<TransactionClient>,
        observer: Option<crate::service::NativeObserver>,
        service: Option<TransactionService>,
        observation_error: Arc<Mutex<Option<SemanticError>>>,
        notices: NoticeChannel,
    ) -> Result<Self, SemanticError> {
        let (stop, stopped) = mpsc::channel();
        let follower = peer.clone();
        let error_slot = Arc::clone(&observation_error);
        let (notice_sender, notice_receiver) = notices;
        let worker = std::thread::Builder::new()
            .name("atomic-peer-observer".into())
            .spawn(move || {
                let mut refresh_at = Instant::now() + Duration::from_millis(100);
                while matches!(stopped.try_recv(), Err(mpsc::TryRecvError::Empty)) {
                    let mut needs_catchup = false;
                    match notice_receiver
                        .recv_timeout(refresh_at.saturating_duration_since(Instant::now()))
                    {
                        Ok(report) => {
                            if let Err(error) = follower.adopt_committed_report(&report) {
                                *error_slot.lock().expect("observation mutex poisoned") =
                                    Some(error);
                                needs_catchup = true;
                            }
                        }
                        Err(mpsc::RecvTimeoutError::Timeout) => {}
                        Err(mpsc::RecvTimeoutError::Disconnected) => break,
                    }
                    if needs_catchup || Instant::now() >= refresh_at {
                        let result = follower
                            .sync_database_value()
                            .and_then(|_| follower.refresh_index());
                        *error_slot.lock().expect("observation mutex poisoned") = result.err();
                        refresh_at = Instant::now() + Duration::from_millis(100);
                    }
                }
            })
            .map_err(|error| {
                SemanticError::new(
                    crate::ErrorCategory::Unavailable,
                    "connection/observer-start",
                    error.to_string(),
                )
            })?;
        Ok(Self {
            core: Arc::new(ConnectionCore {
                identity: peer.identity(),
                peer,
                client: Mutex::new(client),
                observer: Mutex::new(observer),
                notices: notice_sender,
                observation_error,
                _advancement: Advancement {
                    stop,
                    worker: Some(worker),
                },
                _service: service,
            }),
        })
    }

    /// Replace or attach the submission endpoint after writer failover. Peer
    /// state and previously captured values remain independent of that writer.
    pub fn attach_writer(&self, client: TransactionClient) -> Result<(), SemanticError> {
        if client.identity() != self.core.identity {
            return Err(identity_mismatch(&client.identity(), &self.core.identity));
        }
        let mut endpoint = self.core.client.lock().expect("client mutex poisoned");
        let (_, observer) = client.observe_commits(|| {
            self.core.peer.sync_database_value()?;
            Ok(((), observer_for(&self.core.notices)))
        })?;
        *self.core.observer.lock().expect("observer mutex poisoned") = Some(observer);
        *endpoint = Some(client);
        Ok(())
    }

    /// Most recent background/adoption failure, separate from transaction
    /// outcomes. A subsequent successful catch-up clears it. Captured values
    /// remain valid even while this connection cannot advance.
    pub fn observation_error(&self) -> Option<SemanticError> {
        self.core
            .observation_error
            .lock()
            .expect("observation mutex poisoned")
            .clone()
    }

    pub fn load_stats(&self) -> crate::PeerLoadStats {
        self.core.peer.load_stats()
    }

    /// Shared immutable-node cache counters, including byte/entry residency
    /// and eviction. This is representation accounting, not process RSS.
    pub fn cache_stats(&self) -> crate::CacheStats {
        self.core.peer.cache_stats()
    }

    /// Recent-log residency of the latest adopted value, independent of the
    /// immutable-node cache and of older values retained by callers.
    pub fn recent_stats(&self) -> crate::recent::RecentStats {
        self.core.peer.recent_stats()
    }

    #[cfg(unix)]
    pub(crate) fn open_socket_report(
        &self,
        wire: crate::encoding::WireReport,
    ) -> Result<ServiceTransactionReport, SemanticError> {
        let report = self.core.peer.open_socket_report(wire)?;
        if let Err(error) = self.core.peer.adopt_committed_report(&report) {
            *self
                .core
                .observation_error
                .lock()
                .expect("observation mutex poisoned") = Some(error);
        }
        Ok(report)
    }
    /// Start the embedded transactor and strict native peer using plaintext
    /// PostgreSQL transport (the existing local/development policy).
    pub fn start(
        config: TransactionServiceConfig,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        let connection = PostgresConnectionConfig::plaintext(config.connection.clone());
        Self::start_configured(config, connection, cache_capacity)
    }

    /// Start every connection-owned role under one explicit PostgreSQL
    /// transport policy. The transactor is started first so the bounded fresh
    /// database exception can publish its native basis before the strict peer
    /// opens it.
    pub fn start_configured(
        config: TransactionServiceConfig,
        connection: PostgresConnectionConfig,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        let service = TransactionService::start_configured(config, connection.clone())?;
        let client = service.client();
        Self::open_attached(connection, client, cache_capacity, Some(service))
    }

    pub fn identity(&self) -> &DatabaseIdentity {
        &self.core.identity
    }

    /// Return the connection's most recently adopted immutable value without
    /// PostgreSQL I/O or transactor co-ordination.
    pub fn db(&self) -> DatabaseValue {
        self.core.peer.database_value()
    }

    /// Capture an immutable transaction log without contacting the writer.
    pub fn log(&self) -> crate::LogValue {
        self.core.peer.log()
    }

    /// Capture and adopt the newest durable head visible at the time of this
    /// call. This is the native synchronous counterpart of recovered
    /// zero-argument `sync`.
    pub fn sync(&self) -> Result<DatabaseValue, SemanticError> {
        self.core.peer.sync_database_value()
    }

    /// Wait until this connection has adopted at least `target_t` without
    /// communicating with a separate transactor endpoint. The background
    /// observer performs storage I/O; a stalled read cannot extend this wait.
    pub fn sync_to(
        &self,
        target_t: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.core.peer.wait_for_local_basis(target_t, timeout)?;
        Ok(self.db())
    }

    pub fn sync_index(
        &self,
        target_t: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.core.peer.sync_index(target_t, timeout)
    }

    pub fn sync_schema(
        &self,
        target_t: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.core.peer.sync_schema(target_t, timeout)
    }

    pub fn sync_excise(
        &self,
        target_t: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.core.peer.sync_excise(target_t, timeout)
    }

    /// Admit a transaction without blocking. The returned ticket does not
    /// expose a successful report until ordered local observation has been
    /// attempted. Observation faults are separate from a known durable success.
    pub fn submit(
        &self,
        request: TransactionRequest,
    ) -> Result<ConnectionTransactionTicket, SemanticError> {
        let client = self.core.client.lock().expect("client mutex poisoned");
        let client = client.as_ref().ok_or_else(|| {
            SemanticError::new(
                crate::ErrorCategory::Unavailable,
                "connection/no-writer",
                "read-only connection has no attached transaction endpoint",
            )
        })?;
        Ok(ConnectionTransactionTicket {
            ticket: client.submit(request)?,
            connection: self.clone(),
        })
    }

    pub fn transact(
        &self,
        request: TransactionRequest,
        timeout: Duration,
    ) -> Result<ServiceTransactionReport, SemanticError> {
        self.submit(request)?.wait(timeout)
    }

    /// Enable the single unbounded report queue associated with this
    /// connection. Reports begin with the next transaction adopted after the
    /// queue is enabled; no already-observed prefix is manufactured.
    pub fn enable_transaction_reports(&self) -> bool {
        self.core.peer.enable_tx_reports()
    }

    pub fn try_next_transaction_report(&self) -> Option<ServiceTransactionReport> {
        self.core.peer.try_next_tx_report()
    }

    pub fn next_transaction_report(
        &self,
        timeout: Duration,
    ) -> Result<Option<ServiceTransactionReport>, SemanticError> {
        self.core.peer.next_tx_report(timeout)
    }

    /// Remove this connection's report queue and release all unconsumed
    /// immutable db-before/db-after values.
    pub fn remove_transaction_report_queue(&self) -> bool {
        self.core.peer.remove_tx_reports()
    }

    /// Query convenience that captures exactly one immutable value.
    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        let database = self.db();
        database.query(query, inputs, control)
    }

    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        let database = self.db();
        database.query_with_extensions(query, inputs, control, extensions)
    }

    /// Pull convenience that pins all recursive navigation to one immutable
    /// value even if this connection advances concurrently.
    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        let database = self.db();
        database.pull(pattern, entity)
    }

    /// Entity convenience that pins subsequent lazy navigation to the one
    /// immutable value captured here.
    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        let database = self.db();
        database.entity(identifier)
    }

    pub fn collect_datoms(&self, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
        let database = self.db();
        database.collect_datoms(order)
    }

    pub fn collect_seek_datoms(
        &self,
        boundary: &IndexBoundary,
    ) -> Result<Vec<Datom>, SemanticError> {
        let database = self.db();
        database.collect_seek_datoms(boundary)
    }

    pub fn collect_reverse_seek_datoms(
        &self,
        boundary: &IndexBoundary,
    ) -> Result<Vec<Datom>, SemanticError> {
        let database = self.db();
        database.collect_reverse_seek_datoms(boundary)
    }

    pub fn collect_datoms_with_prefix(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<Vec<Datom>, SemanticError> {
        let database = self.db();
        database.collect_datoms_with_prefix(prefix)
    }
}

/// An admitted transaction. A separate peer worker adopts ordered commits even
/// without ticket waits. Waiters coordinate with local adoption within their
/// remaining deadline; a known commit is never turned into a rejection if that
/// observation times out (see `Connection::observation_error`).
pub struct ConnectionTransactionTicket {
    ticket: TransactionTicket,
    connection: Connection,
}

impl ConnectionTransactionTicket {
    pub fn wait(self, timeout: Duration) -> Result<ServiceTransactionReport, SemanticError> {
        let started = Instant::now();
        let report = self.ticket.wait(timeout)?;
        if let Err(error) = self
            .connection
            .core
            .peer
            .wait_for_local_basis(report.basis_t, timeout.saturating_sub(started.elapsed()))
        {
            *self
                .connection
                .core
                .observation_error
                .lock()
                .expect("observation mutex poisoned") = Some(error);
        }
        Ok(report)
    }

    pub fn request_key(&self) -> &str {
        self.ticket.request_key()
    }
}

fn observer_for(
    sender: &mpsc::SyncSender<Arc<ServiceTransactionReport>>,
) -> crate::service::CommitObserver {
    let sender = sender.clone();
    Arc::new(move |report| {
        // This is a bounded hint, not the authoritative lossless report queue.
        // A full slot is safe: periodic authenticated log catch-up fills gaps.
        // Never block the writer behind a peer mutex, query, or slow consumer.
        let _ = sender.try_send(report);
    })
}

fn identity_mismatch(client: &DatabaseIdentity, peer: &DatabaseIdentity) -> SemanticError {
    SemanticError::new(
        crate::ErrorCategory::Fault,
        "connection/database-identity-mismatch",
        "the transaction client and native peer do not address the same database lineage",
    )
    .detail("client_database_id", client.database_id())
    .detail("client_lineage_id", client.lineage_id())
    .detail("peer_database_id", peer.database_id())
    .detail("peer_lineage_id", peer.lineage_id())
}

#[cfg(test)]
mod tests {
    use super::*;

    fn assert_clone_send_sync<T: Clone + Send + Sync>() {}

    #[test]
    fn native_connection_handle_is_cloneable_send_and_sync() {
        assert_clone_send_sync::<Connection>();
    }

    #[test]
    fn stalled_local_observation_does_not_stop_the_authoritative_writer() {
        let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
            return;
        };
        let id = format!(
            "observer-stall-{}-{}",
            std::process::id(),
            std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        crate::PostgresMigrator::connect(&postgres)
            .unwrap()
            .migrate()
            .unwrap();
        crate::PostgresStore::connect(&postgres)
            .unwrap()
            .create_database(&id, crate::Schema::new())
            .unwrap();
        let connection = Connection::start(
            TransactionServiceConfig {
                connection: postgres,
                database_id: id.clone(),
                holder_id: id,
                lease_duration: Duration::from_secs(5),
                renew_interval: Duration::from_millis(100),
                queue_capacity: 8,
                capacity_limits: Default::default(),
            },
            4,
        )
        .unwrap();
        let paused = connection.core.peer.pause_updates_for_test();
        let first = connection
            .submit(TransactionRequest::new("first", vec![]))
            .unwrap();
        let client = connection
            .core
            .client
            .lock()
            .unwrap()
            .as_ref()
            .unwrap()
            .clone();
        let second = client.transact(
            TransactionRequest::new("second", vec![]),
            Duration::from_secs(2),
        );
        drop(paused);
        let second = second.expect("a peer's stalled local observation must not block the writer");
        let first = first.wait(Duration::from_secs(5)).unwrap();
        assert_eq!(second.basis_t, first.basis_t + 1);
        connection
            .sync_to(second.basis_t, Duration::from_secs(5))
            .unwrap();
        let peer = connection.core.peer.clone();
        let paused = peer.pause_updates_for_test();
        let third = client
            .transact(
                TransactionRequest::new("third", vec![]),
                Duration::from_secs(2),
            )
            .unwrap();
        let started = Instant::now();
        let error = connection
            .sync_to(third.basis_t, Duration::from_millis(25))
            .unwrap_err();
        assert_eq!(error.code, "connection/observation-timeout");
        assert!(started.elapsed() < Duration::from_millis(250));
        let (done, finished) = mpsc::channel();
        let dropper = std::thread::spawn(move || {
            drop(connection);
            done.send(()).unwrap();
        });
        let stopped = finished.recv_timeout(Duration::from_secs(2));
        drop(paused);
        dropper.join().unwrap();
        stopped.expect("dropping a connection must not wait for stalled observer I/O");
    }

    #[test]
    fn database_identity_includes_lineage_not_only_address() {
        let first = DatabaseIdentity::new("catalog-name", "lineage-a");
        let same = DatabaseIdentity::new("catalog-name", "lineage-a");
        let replacement = DatabaseIdentity::new("catalog-name", "lineage-b");
        assert_eq!(first, same);
        assert_ne!(first, replacement);
        assert_eq!(first.database_id(), "catalog-name");
        assert_eq!(first.lineage_id(), "lineage-a");
    }

    #[test]
    fn identity_mismatch_is_a_fail_closed_fault() {
        let client = DatabaseIdentity::new("catalog-name", "lineage-a");
        let peer = DatabaseIdentity::new("catalog-name", "lineage-b");
        let error = identity_mismatch(&client, &peer);
        assert_eq!(error.category, crate::ErrorCategory::Fault);
        assert_eq!(error.code, "connection/database-identity-mismatch");
    }
}
