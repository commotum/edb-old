mod common;
use atomic_core::storage::PgBlockStore;
use atomic_core::*;
use std::sync::{
    Arc,
    atomic::{AtomicBool, Ordering},
};
use std::time::{Duration, Instant};

fn wait(mut condition: impl FnMut() -> bool) {
    let begin = Instant::now();
    while !condition() {
        assert!(
            begin.elapsed() < Duration::from_secs(5),
            "maintenance condition not reached"
        );
        std::thread::sleep(Duration::from_millis(2));
    }
}
fn schema() -> Schema {
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        1000,
        Keyword::new("item", "value"),
        ValueType::Long,
        Cardinality::One,
    );
    attribute.indexed = true;
    schema.install(attribute).unwrap();
    schema
}
fn config(url: &str, name: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: url.into(),
        database_id: name.into(),
        holder_id: "controlled-worker".into(),
        lease_duration: Duration::from_secs(30),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    }
}

#[test]
fn pacing_has_explicit_cancellation_and_counts_only_completed_batches() {
    let ordinary = MaintenanceControl::default();
    ordinary.check().unwrap();
    ordinary.after_batch().unwrap();
    assert_eq!(ordinary.stats().completed_batches, 1);
    assert_eq!(ordinary.stats().pauses, 0);
    let cancel = Arc::new(AtomicBool::new(false));
    let control = MaintenanceControl::new(Duration::from_secs(30), cancel.clone()).unwrap();
    let worker_control = control.clone();
    let worker = std::thread::spawn(move || worker_control.after_batch());
    wait(|| control.stats().pauses == 1);
    cancel.store(true, Ordering::Release);
    assert_eq!(
        worker.join().unwrap().unwrap_err().code,
        "maintenance/canceled"
    );
    assert_eq!(control.stats().completed_batches, 1);
    assert!(control.stats().paused_nanos > 0);
    assert!(MaintenanceControl::new(Duration::MAX, Arc::new(AtomicBool::new(false))).is_err());
    assert!(
        TransactionService::start_configured_with_options(
            config("invalid", "none"),
            PostgresConnectionConfig::plaintext("invalid"),
            ServiceOptions {
                hint_prefetch: HintPrefetchConcurrency { max_workers: 9 },
                ..Default::default()
            }
        )
        .is_err()
    );
    assert!(
        TransactionService::start_configured_with_options(
            config("invalid", "none"),
            PostgresConnectionConfig::plaintext("invalid"),
            ServiceOptions {
                index_preparation_parallelism: 0,
                ..Default::default()
            }
        )
        .is_err()
    );
}

#[test]
fn restore_cancellation_keeps_inactive_data_and_retry_and_gc_remain_correct() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP restore pacing: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let whole = Instant::now();
    let source = common::PostgresFixture::new(&url, "pace_backup_source");
    let target = common::PostgresFixture::new(&url, "pace_backup_target");
    for fixture in [&source, &target] {
        PgBlockStore::install(&PostgresConnectionConfig::plaintext(&fixture.connection)).unwrap();
    }
    DatabaseCatalog::connect(&source.connection)
        .unwrap()
        .create_if_absent("source", schema())
        .unwrap();
    let service = common::start_service(&source.connection, "source");
    let request = TransactionRequest::new(
        "one",
        vec![TxOp::Add {
            entity: EntityRef::Temp("entity".into()),
            attribute: 1000,
            value: Value::Long(42).into(),
        }],
    )
    .with_tx_instant(1000);
    let report = service
        .client()
        .transact(request.clone(), Duration::from_secs(30))
        .unwrap();
    service.shutdown();
    let repository = tempfile::tempdir().unwrap();
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        std::fs::set_permissions(repository.path(), std::fs::Permissions::from_mode(0o700))
            .unwrap();
    }
    let point = PortableBackup::connect(&source.connection)
        .unwrap()
        .backup_database("source", repository.path())
        .unwrap();
    let control =
        MaintenanceControl::new(Duration::from_secs(30), Arc::new(AtomicBool::new(false))).unwrap();
    let work_control = control.clone();
    let directory = repository.path().to_owned();
    let connection = target.connection.clone();
    let worker = std::thread::spawn(move || {
        PortableBackup::connect(&connection)
            .unwrap()
            .with_maintenance_control(work_control)
            .restore_backup(&directory, point.basis_t, "restored")
    });
    wait(|| control.stats().pauses > 0 || worker.is_finished());
    assert!(control.stats().pauses > 0);
    control.cancel();
    assert_eq!(
        worker.join().unwrap().unwrap_err().code,
        "maintenance/canceled"
    );
    let pending = DatabaseCatalog::connect(&target.connection)
        .unwrap()
        .resolve("restored")
        .unwrap();
    let target_config = PostgresConnectionConfig::plaintext(&target.connection);
    let mut store = PgBlockStore::connect(&target_config).unwrap();
    assert!(
        store
            .read_ref(&format!("databases/{}", pending.database_id))
            .unwrap()
            .is_none(),
        "completed staging remains inaccessible until activation"
    );
    assert!(
        store
            .read_ref(&format!("restores/{}", pending.database_id))
            .unwrap()
            .unwrap()
            .value
            .is_some()
    );
    assert_eq!(
        Peer::connect(&target.connection, "restored", 4)
            .err()
            .unwrap()
            .code,
        "backup/database-restoring"
    );
    let mut sql = postgres::Client::connect(&target.connection, postgres::NoTls).unwrap();
    assert!(
        sql.query_one("SELECT count(*) FROM atomic_objects", &[])
            .unwrap()
            .get::<_, i64>(0)
            > 0
    );
    let retry = PortableBackup::connect(&target.connection)
        .unwrap()
        .restore_backup(repository.path(), report.basis_t, "restored")
        .unwrap();
    common::assert_same_information(&retry, &report.db_after);
    let resumed_service = common::start_service(&target.connection, "restored");
    let replay = resumed_service
        .client()
        .transact(request, Duration::from_secs(30))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, report.tx_hash);
    assert_eq!(replay.tempids, report.tempids);
    common::assert_same_information(&replay.db_before, &report.db_before);
    common::assert_same_information(&replay.db_after, &report.db_after);
    resumed_service.shutdown();
    let pace = MaintenanceControl::new(Duration::from_millis(2), Arc::new(AtomicBool::new(false)))
        .unwrap();
    PostgresOperator::connect(&target.connection)
        .unwrap()
        .with_maintenance_control(pace.clone())
        .collect_garbage(Duration::from_secs(3600))
        .unwrap();
    assert_eq!(pace.stats().completed_batches, 1);
    assert_eq!(pace.stats().pauses, 1);
    drop((
        retry, replay, report, sql, store, source, target, repository,
    ));
    eprintln!(
        "RESTORE_PACING_OK inactive_until_retry=true gc_paced=true whole={:?}",
        whole.elapsed()
    );
}

#[test]
fn concurrent_hint_lanes_share_budget_and_do_not_join_transaction_authority() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP hint concurrency: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "hint_concurrency");
    let url = &fixture.connection;
    PgBlockStore::install(&PostgresConnectionConfig::plaintext(url)).unwrap();
    DatabaseCatalog::connect(url)
        .unwrap()
        .create_if_absent("hints", schema())
        .unwrap();
    let start = || {
        let mut config = config(url, "hints");
        config.capacity_limits.writer_tree_cache_entries = 0;
        config.capacity_limits.writer_tree_cache_bytes = 0;
        TransactionService::start_configured_with_options(
            config,
            PostgresConnectionConfig::plaintext(url),
            ServiceOptions {
                hint_prefetch: HintPrefetchConcurrency { max_workers: 3 },
                ..Default::default()
            },
        )
        .unwrap()
    };
    let service = start();
    let seed = service
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                (0..12)
                    .map(|i| TxOp::Add {
                        entity: EntityRef::Temp(format!("e{i}")),
                        attribute: 1000,
                        value: Value::Long(i).into(),
                    })
                    .collect(),
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    let hints = TransactionHints::from_reads(
        seed.db_after.snapshot_reference().unwrap(),
        seed.tempids.values().map(|eid| ReadHint {
            history: false,
            prefix: IndexPrefix::Eavt {
                entity: *eid,
                attribute: Some(1000),
                value: None,
            },
        }),
        HintLimits::default(),
    )
    .unwrap();
    // No recent-tier or RAM-cache shortcut may satisfy these hinted reads.
    // Reopen on the actual consolidated source before blocking its transport.
    service.shutdown();
    common::consolidate(url, "hints").unwrap();
    let service = start();
    let mut control = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let mut locked = control.transaction().unwrap();
    // Pause only this isolated fixture's opaque object reads. Real independent
    // advisory transports must reach PostgreSQL; no semantic SQL wrapper or
    // simulated worker count is involved.
    locked
        .batch_execute("LOCK TABLE atomic_objects IN ACCESS EXCLUSIVE MODE")
        .unwrap();
    let (ticket, execution) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("parallel", vec![]),
            hints.clone(),
            HintPrefetchOptions {
                timeout: Duration::from_secs(10),
                max_datoms: 2,
                ..Default::default()
            },
        )
        .unwrap();
    wait(|| {
        let stats = execution.snapshot();
        // Three lanes start, but the shared two-datom admission allows only
        // two cursor reads. The third must finish limited, not wait on I/O.
        stats.peak_active_workers == 3 && stats.active_workers == 2
            && stats.completed_workers == 1 && locked.query_one(
            "SELECT count(*) FROM pg_locks WHERE relation='atomic_objects'::regclass AND mode='AccessShareLock' AND NOT granted", &[]
        ).unwrap().get::<_, i64>(0) >= 2
    });
    locked.commit().unwrap();
    wait(|| execution.snapshot().active_workers == 0);
    let stats = execution.snapshot();
    assert_eq!(stats.completed_workers, 3);
    assert_eq!(stats.peak_active_workers, 3);
    assert_eq!(stats.datoms, 2, "one shared datom budget, not two per lane");
    assert_eq!(stats.join_nanos, 0);
    assert!(stats.canceled_or_limited);
    let report = ticket.wait(Duration::from_secs(10)).unwrap();
    assert_eq!(report.db_after.basis_t(), seed.db_after.basis_t() + 1);
    let (ticket, execution) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("canceled-hint", vec![]),
            hints,
            HintPrefetchOptions {
                cancel: Arc::new(AtomicBool::new(true)),
                ..Default::default()
            },
        )
        .unwrap();
    ticket.wait(Duration::from_secs(10)).unwrap();
    assert_eq!(execution.snapshot().peak_active_workers, 0);
    eprintln!("opaque-store hint transports=3; shared budget=2 datoms; stats={stats:?}");
    drop((report, seed));
    service.shutdown();
}
