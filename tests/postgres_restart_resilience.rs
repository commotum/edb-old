//! Real WAL crash recovery, only on an explicitly opted-in disposable server.
//! The ordinary ATOMIC_POSTGRES_URL is never used to control a PostgreSQL process.
mod common;
use atomic_core::storage::PgBlockStore;
use atomic_core::*;
use std::path::PathBuf;
use std::process::Command;
use std::time::{Duration, Instant};

const WAIT: Duration = Duration::from_secs(60);
struct DisposableServer {
    data: PathBuf,
    pg_ctl: PathBuf,
    log: PathBuf,
    options: Option<String>,
    stopped: bool,
}
impl DisposableServer {
    fn verified(config: &PostgresConnectionConfig) -> Self {
        let data = PathBuf::from(std::env::var("ATOMIC_RESTART_POSTGRES_DATA").unwrap())
            .canonicalize()
            .unwrap();
        let temporary = std::env::temp_dir().canonicalize().unwrap();
        assert!(
            data != temporary && data.starts_with(&temporary),
            "only a dedicated temporary data directory may be crashed"
        );
        let mut sql = config.connect().unwrap();
        let observed: String = sql.query_one("SHOW data_directory", &[]).unwrap().get(0);
        assert_eq!(
            data,
            PathBuf::from(observed).canonicalize().unwrap(),
            "URL and controlled cluster must match"
        );
        for setting in ["fsync", "synchronous_commit", "full_page_writes"] {
            let actual: String = sql
                .query_one(&format!("SHOW {setting}"), &[])
                .unwrap()
                .get(0);
            assert_eq!(actual, "on", "WAL acceptance requires {setting}=on");
        }
        Self {
            data,
            pg_ctl: PathBuf::from(std::env::var("ATOMIC_RESTART_PG_CTL").unwrap())
                .canonicalize()
                .unwrap(),
            log: PathBuf::from(std::env::var("ATOMIC_RESTART_POSTGRES_LOG").unwrap()),
            options: std::env::var("ATOMIC_RESTART_POSTGRES_OPTIONS").ok(),
            stopped: false,
        }
    }
    fn crash(&mut self) {
        self.stopped = true;
        assert!(
            Command::new(&self.pg_ctl)
                .arg("-D")
                .arg(&self.data)
                .args(["-m", "immediate", "-w", "-t", "30", "stop"])
                .status()
                .unwrap()
                .success()
        );
    }
    fn start(&mut self) -> bool {
        let mut command = Command::new(&self.pg_ctl);
        command.arg("-D").arg(&self.data).arg("-l").arg(&self.log);
        if let Some(options) = &self.options {
            command.args(["-o", options]);
        }
        let success = command
            .args(["-w", "-t", "30", "start"])
            .status()
            .is_ok_and(|s| s.success());
        if success {
            self.stopped = false;
        }
        success
    }
}
impl Drop for DisposableServer {
    fn drop(&mut self) {
        if self.stopped {
            let _ = self.start();
        }
    }
}
fn writer_config(connection: &PostgresConnectionConfig, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.clone(),
        database_id: "wal".into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(3),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits {
            writer_tree_cache_entries: 8,
            writer_tree_cache_bytes: 64 * 1024,
            ..Default::default()
        },
    }
}
#[test]
fn immediate_postgres_crash_preserves_acknowledged_receipts_and_live_immutable_values() {
    if std::env::var("ATOMIC_ALLOW_DISPOSABLE_PG_CRASH").as_deref() != Ok("1") {
        eprintln!(
            "SKIP real WAL crash: requires ATOMIC_ALLOW_DISPOSABLE_PG_CRASH=1 and dedicated ATOMIC_RESTART_* settings"
        );
        return;
    }
    let url = std::env::var("ATOMIC_RESTART_POSTGRES_URL").expect("dedicated crash URL");
    let base = PostgresConnectionConfig::plaintext(&url);
    let server = DisposableServer::verified(&base);
    let fixture = common::PostgresFixture::new(&url, "block_wal_restart");
    // Restart guard drops before the schema fixture even on an assertion panic.
    let mut server = server;
    let config = PostgresConnectionConfig::plaintext(&fixture.connection)
        .with_io_policy(PostgresIoPolicy {
            connect_timeout: Some(Duration::from_secs(2)),
            statement_timeout: Some(Duration::from_secs(30)),
            lock_timeout: Some(Duration::from_secs(5)),
            ..Default::default()
        })
        .unwrap();
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    DatabaseCatalog::connect_configured(&config)
        .unwrap()
        .create_if_absent("wal", schema)
        .unwrap();
    let peer =
        Connection::connect_configured_with_cache_limits(config.clone(), "wal", 8, 64 * 1024)
            .unwrap();
    let before = peer.db();
    let before_information = before
        .clone()
        .history()
        .collect_datoms(IndexOrder::Eavt)
        .unwrap();
    let writer = TransactionService::start(writer_config(&config, "before-crash")).unwrap();
    let attached = Connection::attach_configured(config.clone(), writer.client(), 8).unwrap();
    let request = TransactionRequest::new(
        "acknowledged",
        (0..128)
            .map(|n| TxOp::Add {
                entity: EntityRef::Temp(format!("item-{n}")),
                attribute: 1000,
                value: Value::Long(n).into(),
            })
            .collect(),
    )
    .with_tx_instant(1000);
    let acknowledged = attached.transact(request.clone(), WAIT).unwrap();
    writer.client().request_index().unwrap();
    attached.sync_index(acknowledged.basis_t, WAIT).unwrap();
    let captured = peer.sync_to(acknowledged.basis_t, WAIT).unwrap();
    let expected = captured
        .clone()
        .history()
        .collect_datoms(IndexOrder::Eavt)
        .unwrap();
    let log = peer.log();
    let logged = log
        .tx_range(Some(TimePoint::T(acknowledged.basis_t)), None)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_eq!(logged.len(), 1);
    let standby = TransactionStandby::start(
        writer_config(&config, "after-crash"),
        Duration::from_millis(20),
    )
    .unwrap();

    // A real open provider transaction must be rolled back by crash recovery.
    // This is a generic uncommitted reference, not a second engine publisher.
    let mut uncommitted = config.connect().unwrap();
    uncommitted.batch_execute("BEGIN").unwrap();
    uncommitted
        .execute(
            "INSERT INTO atomic_refs(key,revision,value) VALUES ('test/wal-uncommitted',1,NULL)",
            &[],
        )
        .unwrap();
    let started = Instant::now();
    server.crash();
    assert_eq!(peer.db().basis_t(), acknowledged.basis_t);
    assert_eq!(before.basis_t() + 1, captured.basis_t());
    drop(uncommitted);
    drop(attached);
    writer.shutdown();
    assert!(server.start(), "dedicated PostgreSQL did not restart");
    let restarted_ms = started.elapsed().as_millis();

    let replacement = standby.await_active(WAIT).unwrap();
    let attached = Connection::attach_configured(config.clone(), replacement.client(), 8).unwrap();
    let replay = attached.transact(request.clone(), WAIT).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, acknowledged.basis_t);
    assert_eq!(replay.tx_hash, acknowledged.tx_hash);
    assert_eq!(replay.tempids, acknowledged.tempids);
    assert_eq!(replay.tx_data, acknowledged.tx_data);
    assert_eq!(
        replay.db_before.snapshot_key(),
        acknowledged.db_before.snapshot_key()
    );
    assert_eq!(
        replay.db_after.snapshot_key(),
        acknowledged.db_after.snapshot_key()
    );
    let conflict = request.with_tx_instant(1001);
    assert_eq!(
        attached.transact(conflict, WAIT).unwrap_err().code,
        "postgres/idempotency-key-reused"
    );
    assert!(
        PgBlockStore::connect(&config)
            .unwrap()
            .read_ref("test/wal-uncommitted")
            .unwrap()
            .is_none()
    );
    assert_eq!(
        peer.sync()
            .unwrap()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    assert_eq!(
        before
            .clone()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap(),
        before_information
    );
    assert_eq!(
        log.tx_range(Some(TimePoint::T(acknowledged.basis_t)), None)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap(),
        logged
    );

    let entity = acknowledged.tempids["item-64"];
    let successor = attached
        .transact(
            TransactionRequest::new(
                "after-recovery",
                vec![TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1000,
                    value: Value::Long(999).into(),
                }],
            )
            .with_tx_instant(2000),
            WAIT,
        )
        .unwrap();
    assert_eq!(successor.basis_t, acknowledged.basis_t + 1);
    let current = peer.sync_to(successor.basis_t, WAIT).unwrap();
    assert_eq!(
        current.values(entity, 1000).unwrap(),
        vec![Value::Long(999)]
    );
    assert_eq!(
        captured.values(entity, 1000).unwrap(),
        vec![Value::Long(64)]
    );
    assert_eq!(
        current
            .clone()
            .as_of(acknowledged.basis_t)
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    drop(attached);
    replacement.shutdown();
    let reopened = Connection::connect_configured(config.clone(), "wal", 0).unwrap();
    common::assert_same_information(&reopened.db(), &current);
    eprintln!(
        "BLOCK_WAL_RESTART immediate_crash=true durability_settings=on restart_ms={restarted_ms} exact_retry=true uncommitted_reference_rolled_back=true retained_values_and_log=true"
    );
    // The fixture's original admin socket died. A fresh connection cleans only
    // the unique schema this test created; the disposable cluster remains up.
    drop(reopened);
    drop(peer);
    config
        .connect()
        .unwrap()
        .batch_execute(&format!("DROP SCHEMA {} CASCADE", fixture.schema))
        .unwrap();
}
