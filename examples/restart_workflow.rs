//! Crash/restart a dedicated PostgreSQL server holding an existing workload.
//! This is destructive to server availability, not to database files. It refuses
//! to run without explicit disposable-server opt-in and matching data_directory.
use atomic_core::{
    AttributeName, BackgroundIndexingConfig, CapacityLimits, Connection, EntityRef, ErrorCategory,
    IndexPrefix, PostgresConnectionConfig, PostgresIoPolicy, PullAttribute, PullPattern, TimePoint,
    TransactionRequest, TransactionService, TransactionServiceConfig, TxOp, Value,
};
use std::path::PathBuf;
use std::process::Command;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

type Result<T> = std::result::Result<T, Box<dyn std::error::Error>>;
const WAIT: Duration = Duration::from_secs(90);

struct DisposableServer {
    pg_ctl: PathBuf,
    data: PathBuf,
    log: PathBuf,
    options: String,
    stopped: bool,
}

impl DisposableServer {
    fn verified(connection: &PostgresConnectionConfig) -> Result<Self> {
        if std::env::var("ATOMIC_ALLOW_DISPOSABLE_PG_CRASH").as_deref() != Ok("1") {
            return Err(
                "set ATOMIC_ALLOW_DISPOSABLE_PG_CRASH=1 only for a dedicated disposable server"
                    .into(),
            );
        }
        let data = PathBuf::from(std::env::var("ATOMIC_RESTART_POSTGRES_DATA")?).canonicalize()?;
        let temporary = std::env::temp_dir().canonicalize()?;
        if data == temporary || !data.starts_with(&temporary) {
            return Err("this example only controls a dedicated data directory below the system temporary directory".into());
        }
        let mut sql = connection.connect()?;
        let observed: String = sql.query_one("SHOW data_directory", &[])?.get(0);
        if data != PathBuf::from(observed).canonicalize()? {
            return Err("connection and configured PostgreSQL data directory differ".into());
        }
        for setting in ["fsync", "synchronous_commit", "full_page_writes"] {
            let value: String = sql.query_one(&format!("SHOW {setting}"), &[])?.get(0);
            if value != "on" {
                return Err(format!("crash durability acceptance requires {setting}=on").into());
            }
        }
        Ok(Self {
            pg_ctl: PathBuf::from(std::env::var("ATOMIC_RESTART_PG_CTL")?).canonicalize()?,
            data,
            log: PathBuf::from(std::env::var("ATOMIC_RESTART_POSTGRES_LOG")?),
            options: std::env::var("ATOMIC_RESTART_POSTGRES_OPTIONS")?,
            stopped: false,
        })
    }

    fn crash(&mut self) -> Result<()> {
        self.stopped = true;
        let status = Command::new(&self.pg_ctl)
            .arg("-D")
            .arg(&self.data)
            .args(["-m", "immediate", "-w", "-t", "30", "stop"])
            .status()?;
        if !status.success() {
            return Err("dedicated PostgreSQL immediate stop failed".into());
        }
        Ok(())
    }

    fn start(&mut self) -> Result<()> {
        let status = Command::new(&self.pg_ctl)
            .arg("-D")
            .arg(&self.data)
            .arg("-l")
            .arg(&self.log)
            .args(["-o", &self.options, "-w", "-t", "30", "start"])
            .status()?;
        if !status.success() {
            return Err("dedicated PostgreSQL restart failed".into());
        }
        self.stopped = false;
        Ok(())
    }
}

impl Drop for DisposableServer {
    fn drop(&mut self) {
        if self.stopped {
            // Restore fixture availability even when a later assertion fails.
            let _ = self.start();
        }
    }
}

fn memory(label: &str) -> Result<()> {
    let status = std::fs::read_to_string("/proc/self/status")?;
    for line in status
        .lines()
        .filter(|line| line.starts_with("VmRSS:") || line.starts_with("VmHWM:"))
    {
        println!("memory phase={label} {line}");
    }
    Ok(())
}

fn writer_config(database: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: String::new(), // configured constructor is the sole source.
        database_id: database.into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(3),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 16,
        capacity_limits: CapacityLimits {
            writer_tree_cache_entries: 256,
            writer_tree_cache_bytes: 4 * 1024 * 1024,
            ..CapacityLimits::default()
        },
    }
}

fn start_writer(
    connection: &PostgresConnectionConfig,
    database: &str,
    holder: &str,
) -> Result<TransactionService> {
    let deadline = Instant::now() + WAIT;
    loop {
        match TransactionService::start_configured_with_indexing(
            writer_config(database, holder),
            connection.clone(),
            BackgroundIndexingConfig {
                memory_index_threshold_bytes: 2 * 1024 * 1024,
                memory_index_max_bytes: 8 * 1024 * 1024,
            },
        ) {
            Ok(writer) => return Ok(writer),
            Err(error)
                if error.category == ErrorCategory::Unavailable
                    && error.code == "postgres/lease-held"
                    && Instant::now() < deadline =>
            {
                std::thread::sleep(Duration::from_millis(50));
            }
            Err(error) => return Err(error.into()),
        }
    }
}

fn main() -> Result<()> {
    let connection =
        PostgresConnectionConfig::plaintext(std::env::var("ATOMIC_RESTART_POSTGRES_URL")?)
            .with_io_policy(PostgresIoPolicy {
                connect_timeout: Some(Duration::from_secs(2)),
                statement_timeout: Some(Duration::from_secs(30)),
                lock_timeout: Some(Duration::from_secs(5)),
                ..PostgresIoPolicy::default()
            })?;
    let database = std::env::var("ATOMIC_DATABASE_ID")?;
    let attribute: u32 = std::env::var("ATOMIC_RESTART_ATTRIBUTE")
        .unwrap_or_else(|_| "1002".into())
        .parse()?;
    let mut server = DisposableServer::verified(&connection)?;
    let run = format!(
        "restart-{}-{}",
        std::process::id(),
        SystemTime::now().duration_since(UNIX_EPOCH)?.as_nanos()
    );
    let peer = Connection::connect_configured_with_cache_limits(
        connection.clone(),
        &database,
        64,
        1024 * 1024,
    )?;
    let before = peer.db();
    // Keep a bounded sample of actual workload facts, not just the new marker.
    let sample_prefix = IndexPrefix::Aevt {
        attribute,
        entity: None,
        value: None,
    };
    let sample = before
        .prefix_cursor(&sample_prefix)?
        .take(64)
        .collect::<std::result::Result<Vec<_>, _>>()?;
    if sample.is_empty() {
        return Err("existing workload has no facts at the requested Long attribute".into());
    }
    let request = TransactionRequest::new(
        format!("{run}-acknowledged"),
        vec![TxOp::Add {
            entity: EntityRef::Temp("crash-marker".into()),
            attribute,
            value: Value::Long(710_001).into(),
        }],
    );
    let writer = start_writer(&connection, &database, &format!("{run}-before"))?;
    let submitter = Connection::attach_configured(connection.clone(), writer.client(), 2)?;
    let acknowledged = submitter.transact(request.clone(), WAIT)?;
    let marker = acknowledged.tempids["crash-marker"];
    let captured = peer.sync_to(acknowledged.basis_t, WAIT)?;
    let log = peer.log();
    let original_log = log
        .tx_range(Some(TimePoint::T(acknowledged.basis_t)), None)?
        .collect::<std::result::Result<Vec<_>, _>>()?;
    assert_eq!(original_log.len(), 1);
    assert!(before.values(marker, attribute)?.is_empty());
    assert_eq!(
        captured.values(marker, attribute)?,
        vec![Value::Long(710_001)]
    );
    memory("before-crash")?;

    let outage = Instant::now();
    server.crash()?;
    // d/db itself remains a local immutable value even while storage is down.
    assert_eq!(peer.db().basis_t(), acknowledged.basis_t);
    assert_eq!(captured.basis_t(), acknowledged.basis_t);
    assert_eq!(before.basis_t() + 1, acknowledged.basis_t);
    drop(submitter);
    writer.shutdown();
    server.start()?;
    let storage_restart = outage.elapsed();
    let replacement_started = Instant::now();
    let replacement = start_writer(&connection, &database, &format!("{run}-after"))?;
    let recovery = replacement.recovery_stats();
    let replacement_start = replacement_started.elapsed();
    let replacement_peer =
        Connection::attach_configured(connection.clone(), replacement.client(), 2)?;
    let replay = replacement_peer.transact(request, WAIT)?;
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, acknowledged.basis_t);
    assert_eq!(replay.tempids, acknowledged.tempids);
    assert_eq!(peer.sync()?.basis_t(), acknowledged.basis_t);
    assert_eq!(
        log.tx_range(Some(TimePoint::T(acknowledged.basis_t)), None)?
            .collect::<std::result::Result<Vec<_>, _>>()?,
        original_log
    );
    assert_eq!(
        before
            .prefix_cursor(&sample_prefix)?
            .take(64)
            .collect::<std::result::Result<Vec<_>, _>>()?,
        sample
    );

    let changed = replacement_peer.transact(
        TransactionRequest::new(
            format!("{run}-after-recovery"),
            vec![TxOp::Add {
                entity: EntityRef::Id(marker),
                attribute,
                value: Value::Long(710_002).into(),
            }],
        ),
        WAIT,
    )?;
    assert_eq!(changed.basis_t, acknowledged.basis_t + 1);
    let current = peer.sync_to(changed.basis_t, WAIT)?;
    assert_eq!(
        current.values(marker, attribute)?,
        vec![Value::Long(710_002)]
    );
    assert_eq!(
        captured.values(marker, attribute)?,
        vec![Value::Long(710_001)]
    );
    assert_eq!(
        current
            .clone()
            .as_of(acknowledged.basis_t)
            .values(marker, attribute)?,
        vec![Value::Long(710_001)]
    );
    let history = current
        .clone()
        .history()
        .collect_datoms_with_prefix(&IndexPrefix::Eavt {
            entity: marker,
            attribute: Some(attribute),
            value: None,
        })?;
    assert_eq!(history.len(), 3);
    let pattern =
        PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(attribute))]);
    assert_ne!(
        captured.pull(&pattern, marker)?,
        current.pull(&pattern, marker)?
    );
    let residency = replacement.writer_residency_stats();
    assert_eq!(residency.eager_database_values, 0);
    assert_eq!(residency.eager_history_datoms, 0);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    drop(replacement_peer);
    replacement.shutdown();
    let reopened =
        Connection::connect_configured_with_cache_limits(connection, &database, 64, 1024 * 1024)?;
    assert_eq!(reopened.db().basis_t(), changed.basis_t);
    assert_eq!(
        reopened.db().values(marker, attribute)?,
        vec![Value::Long(710_002)]
    );
    memory("after-recovery")?;
    println!(
        "recovery storage_restart_ms={} replacement_start_ms={} base_t={} target_t={} tail_transactions={} tail_range_reads={}",
        storage_restart.as_millis(),
        replacement_start.as_millis(),
        recovery.base_t,
        recovery.target_t,
        recovery.tail_transactions,
        recovery.tail_range_reads
    );
    println!(
        "PASS restart-workflow database={database} original_basis={} final_basis={} immediate_pg_crash=true durable_settings=on exact_replay=true old_values=true old_log=true native_eager=0",
        before.basis_t(),
        changed.basis_t
    );
    Ok(())
}
