//! Untrusted, empty PostgreSQL wakeups. Only authenticated log/index reads
//! advance a peer; disconnects and lost hints are repaired by anti-entropy.
use crate::postgres::postgres_error;
use crate::sql_io::SqlClient;
use crate::{PostgresConnectionConfig, SemanticError};
use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::{OnceLock, mpsc};
use std::time::Duration;

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct NoticePublisherStats {
    pub queued: u64,
    pub dropped: u64,
    pub coalesced: u64,
    pub sent: u64,
    pub failures: u64,
}
static QUEUED: AtomicU64 = AtomicU64::new(0);
static DROPPED: AtomicU64 = AtomicU64::new(0);
static COALESCED: AtomicU64 = AtomicU64::new(0);
static SENT: AtomicU64 = AtomicU64::new(0);
static FAILURES: AtomicU64 = AtomicU64::new(0);
pub fn notice_publisher_stats() -> NoticePublisherStats {
    NoticePublisherStats {
        queued: QUEUED.load(Ordering::Relaxed),
        dropped: DROPPED.load(Ordering::Relaxed),
        coalesced: COALESCED.load(Ordering::Relaxed),
        sent: SENT.load(Ordering::Relaxed),
        failures: FAILURES.load(Ordering::Relaxed),
    }
}
type Notice = (PostgresConnectionConfig, String);
static PUBLISHER: OnceLock<Option<mpsc::SyncSender<Notice>>> = OnceLock::new();

/// Never wait on network I/O or turn hint failure into a commit outcome.
/// One process-global worker retains at most 64 queued + 64 draining hints.
pub(crate) fn publish(config: &PostgresConnectionConfig, database: &str) {
    if database.len() > 4096 {
        DROPPED.fetch_add(1, Ordering::Relaxed);
        return;
    }
    let sender = PUBLISHER.get_or_init(|| {
        let (sender, receiver) = mpsc::sync_channel::<Notice>(64);
        std::thread::Builder::new().name("atomic-notice-publisher".into()).spawn(move || {
            let context = crate::OperationContext::new(crate::OperationKind::PeerObservation);
            let _scope = context.enter();
            let mut current: Option<(PostgresConnectionConfig, SqlClient)> = None;
            while let Ok(first) = receiver.recv() {
                let mut batch = vec![first];
                for _ in 1..64 {
                    let Ok(next) = receiver.try_recv() else { break; };
                    if batch.contains(&next) { COALESCED.fetch_add(1, Ordering::Relaxed); }
                    else { batch.push(next); }
                }
                for (config, database) in batch {
                    if current.as_ref().is_none_or(|(active, client)| active != &config || client.is_closed()) {
                        current = match config.connect_for("observation/publisher-connect") {
                            Ok(client) => Some((config.clone(), client)),
                            Err(_) => { FAILURES.fetch_add(1, Ordering::Relaxed); continue; }
                        };
                    }
                    let client = &mut current.as_mut().unwrap().1;
                    let result = client.query_one("SELECT pg_catalog.pg_notify('atomic_n_' || md5(n.nspname || ':' || $1::text), '') FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid=c.relnamespace WHERE c.oid='atomic_heads'::regclass", &[&database]);
                    if result.is_ok() { SENT.fetch_add(1, Ordering::Relaxed); }
                    else { FAILURES.fetch_add(1, Ordering::Relaxed); current = None; }
                }
            }
        }).ok().map(|_| sender)
    });
    if let Some(sender) = sender
        && sender
            .try_send((config.clone(), database.to_owned()))
            .is_ok()
    {
        QUEUED.fetch_add(1, Ordering::Relaxed);
    } else {
        DROPPED.fetch_add(1, Ordering::Relaxed);
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct ObservationConfig {
    pub anti_entropy_interval: Duration,
}
impl Default for ObservationConfig {
    fn default() -> Self {
        Self {
            anti_entropy_interval: Duration::from_secs(30),
        }
    }
}
impl ObservationConfig {
    pub(crate) fn validate(self) -> Result<Self, SemanticError> {
        if self.anti_entropy_interval.is_zero()
            || self.anti_entropy_interval > Duration::from_secs(86400)
        {
            return Err(SemanticError::incorrect(
                "observation/invalid-interval",
                "anti-entropy interval must be positive and at most one day",
            ));
        }
        Ok(self)
    }
}

pub(crate) struct NoticeListener {
    client: SqlClient,
}
impl NoticeListener {
    pub(crate) fn connect(
        config: &PostgresConnectionConfig,
        database: &str,
    ) -> Result<Self, SemanticError> {
        let mut client = config.connect_for("observation/connect")?;
        let channel: String = client.query_one(
            "SELECT 'atomic_n_' || md5(n.nspname || ':' || $1::text) FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid=c.relnamespace WHERE c.oid='atomic_heads'::regclass", &[&database])
            .map_err(|e| postgres_error("observation/channel", e))?.get(0);
        // Server-generated prefix + lowercase hex only, never caller SQL.
        if !channel.starts_with("atomic_n_")
            || channel.len() != 41
            || !channel[9..].bytes().all(|b| b.is_ascii_hexdigit())
        {
            return Err(SemanticError::incorrect(
                "observation/invalid-channel",
                "database notice channel is invalid",
            ));
        }
        client
            .batch_execute(&format!("LISTEN {channel}"))
            .map_err(|e| postgres_error("observation/listen", e))?;
        Ok(Self { client })
    }
    pub(crate) fn wait(&mut self, timeout: Duration) -> Result<bool, SemanticError> {
        let notice = self
            .client
            .wait_notification(timeout)
            .map_err(|e| postgres_error("observation/receive", e))?;
        if self.client.is_closed() {
            return Err(SemanticError::new(
                crate::ErrorCategory::Unavailable,
                "observation/disconnected",
                "database notice connection disconnected",
            ));
        }
        Ok(notice)
    }
}
