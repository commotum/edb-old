mod common;
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
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap();
    }
    let created = PostgresStore::connect(&source.connection)
        .unwrap()
        .create_database("source", schema())
        .unwrap();
    let service = common::start_service(&source.connection, "source");
    let report = common::transact(
        &service,
        "one",
        created.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Temp("entity".into()),
            attribute: 1000,
            value: Value::Long(42).into(),
        }],
        1000,
    );
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
    let mut sql = postgres::Client::connect(&target.connection, postgres::NoTls).unwrap();
    assert_eq!(
        sql.query_one(
            // Initial restore deliberately publishes no synthetic genesis
            // head. Completed staging remains inaccessible until activation.
            "SELECT count(*) FROM atomic_heads",
            &[]
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    assert!(Peer::connect(&target.connection, "restored", 4).is_err());
    assert!(
        sql.query_one("SELECT count(*) FROM atomic_generation_transactions", &[])
            .unwrap()
            .get::<_, i64>(0)
            > 0
    );
    let retry = PortableBackup::connect(&target.connection)
        .unwrap()
        .restore_backup(repository.path(), report.basis_t, "restored")
        .unwrap();
    common::assert_same_information(&retry, &report.db_after);
    let pace = MaintenanceControl::new(Duration::from_millis(2), Arc::new(AtomicBool::new(false)))
        .unwrap();
    PostgresOperator::connect(&target.connection)
        .unwrap()
        .with_maintenance_control(pace.clone())
        .collect_garbage(Duration::from_secs(3600))
        .unwrap();
    assert_eq!(pace.stats().completed_batches, 1);
    assert_eq!(pace.stats().pauses, 1);
    drop((retry, report, sql, source, target, repository));
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
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    DatabaseCatalog::connect(url)
        .unwrap()
        .create_if_absent("hints", schema())
        .unwrap();
    let service = TransactionService::start_configured_with_options(
        config(url, "hints"),
        PostgresConnectionConfig::plaintext(url),
        ServiceOptions {
            hint_prefetch: HintPrefetchConcurrency { max_workers: 3 },
            ..Default::default()
        },
    )
    .unwrap();
    let seed = service
        .client()
        .submit(TransactionRequest::new(
            "seed",
            (0..12)
                .map(|i| TxOp::Add {
                    entity: EntityRef::Temp(format!("e{i}")),
                    attribute: 1000,
                    value: Value::Long(i).into(),
                })
                .collect(),
        ))
        .unwrap()
        .wait(Duration::from_secs(10))
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
    let mut control = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let key: i64 = control
        .query_one(
            "SELECT hashtextextended(current_schema() || '/parallel-hints', 9)",
            &[],
        )
        .unwrap()
        .get(0);
    control
        .query_one("SELECT pg_advisory_lock($1)", &[&key])
        .unwrap();
    // Only newly opened advisory pin sessions use the one-second timeout.
    // Observe genuine concurrent PG requests, not inferred task counts.
    control.batch_execute(&format!(r#"
      ALTER FUNCTION atomic_log_generation_pin_key(text,bigint) RENAME TO atomic_original_pin_key;
      CREATE FUNCTION atomic_log_generation_pin_key(candidate_database_id text, candidate_generation bigint)
      RETURNS bigint LANGUAGE plpgsql SET search_path FROM CURRENT AS $body$
      BEGIN
        IF current_setting('statement_timeout') = '1s' THEN
          PERFORM pg_advisory_lock({key}); PERFORM pg_advisory_unlock({key});
        END IF;
        RETURN atomic_original_pin_key(candidate_database_id,candidate_generation);
      END $body$;
    "#)).unwrap();
    let mut authority = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let mut locked = authority.transaction().unwrap();
    locked
        .query_one(
            "SELECT basis_t FROM atomic_heads WHERE database_id='hints' FOR UPDATE",
            &[],
        )
        .unwrap();
    let (ticket, execution) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("parallel", vec![]),
            hints.clone(),
            HintPrefetchOptions {
                timeout: Duration::from_secs(1),
                max_datoms: 2,
                ..Default::default()
            },
        )
        .unwrap();
    wait(|| {
        control.query_one("SELECT count(*) FROM pg_locks WHERE locktype='advisory' AND NOT granted AND classid::bigint=$1 AND objid::bigint=$2", &[&((key as u64 >> 32) as i64), &((key as u64 & 0xffff_ffff) as i64)]).unwrap().get::<_, i64>(0) == 3
    });
    assert_eq!(execution.snapshot().active_workers, 3);
    control
        .query_one("SELECT pg_advisory_unlock($1)", &[&key])
        .unwrap();
    wait(|| execution.snapshot().active_workers == 0);
    let stats = execution.snapshot();
    assert_eq!(stats.completed_workers, 3);
    assert_eq!(stats.peak_active_workers, 3);
    assert_eq!(
        stats.datoms, 2,
        "read cap is shared, not two datoms per lane"
    );
    assert_eq!(stats.join_nanos, 0);
    assert!(stats.canceled_or_limited);
    locked.commit().unwrap();
    let report = ticket.wait(Duration::from_secs(10)).unwrap();
    assert_eq!(report.db_after.basis_t(), seed.db_after.basis_t() + 1);
    let canceled = Arc::new(AtomicBool::new(true));
    let (ticket, execution) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("canceled-hint", vec![]),
            hints,
            HintPrefetchOptions {
                cancel: canceled,
                ..Default::default()
            },
        )
        .unwrap();
    ticket.wait(Duration::from_secs(10)).unwrap();
    assert_eq!(execution.snapshot().peak_active_workers, 0);
    eprintln!("actual PG hint concurrency=3; shared budget2datoms; completed {stats:?}");
    drop((report, seed));
    service.shutdown();
}

#[test]
fn incremental_parallel_preparation_preserves_roots_and_upload_pacing_is_resumable() {
    use atomic_core::persistent_tree::{TreeConfig, build_tree};
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP index controls: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "index_controls");
    let url = &fixture.connection;
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    DatabaseCatalog::connect(url)
        .unwrap()
        .create_if_absent("index", schema())
        .unwrap();
    let service = TransactionService::start(config(url, "index")).unwrap();
    let report = service
        .client()
        .submit(TransactionRequest::new(
            "bulk",
            (0..4096)
                .map(|i| TxOp::Add {
                    entity: EntityRef::Temp(format!("item{i}")),
                    attribute: 1000,
                    value: Value::Long(i).into(),
                })
                .collect(),
        ))
        .unwrap()
        .wait(Duration::from_secs(30))
        .unwrap();
    let captured = report.db_after.clone();
    service.shutdown();
    let mut serial = PostgresIndexer::connect(url, "index")
        .unwrap()
        .with_index_preparation_parallelism(1)
        .unwrap();
    let started = Instant::now();
    let error = serial
        .consolidate_with_fault(IndexBuildFault::AfterSegments)
        .unwrap_err();
    assert_eq!(error.code, "index/injected-failure");
    let serial_elapsed = started.elapsed();
    let serial_stats = serial.tree_store_stats();
    assert_eq!(serial_stats.index_preparation_workers_started, 0);
    let mut sql = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let expected: Vec<u8> = sql
        .query_one(
            "SELECT manifest_hash FROM atomic_tree_build_intents WHERE database_id='index' ORDER BY expected_revision DESC LIMIT 1",
            &[],
        )
        .unwrap()
        .get(0);
    let mut parallel = PostgresIndexer::connect(url, "index")
        .unwrap()
        .with_index_preparation_parallelism(4)
        .unwrap();
    let started = Instant::now();
    let indexed = parallel.consolidate().unwrap();
    let parallel_elapsed = started.elapsed();
    assert_eq!(indexed.manifest_hash.as_slice(), expected.as_slice());
    let parallel_stats = parallel.tree_store_stats();
    assert_eq!(parallel_stats.index_preparation_workers_started, 8);
    assert!(parallel_stats.index_preparation_peak_workers > 1);
    assert!(parallel_stats.index_preparation_peak_workers <= 4);
    let peer = Peer::connect(url, "index", 0).unwrap();
    let actual = peer.db();
    common::assert_same_information(&captured, &actual);
    eprintln!(
        "same candidate roots: serial interrupted-after-upload {serial_elapsed:?}, parallel retry incl publication {parallel_elapsed:?}; not an isolated speedup comparison; serial={serial_stats:?} parallel={parallel_stats:?}"
    );
    eprintln!(
        "same 4096-datom incremental input: preparation width1={}ns ({:.0} input datoms/s), width4={}ns ({:.0} input datoms/s); includes group scheduling, excludes SQL merge",
        serial_stats.index_preparation_nanos,
        4096.0e9 / serial_stats.index_preparation_nanos.max(1) as f64,
        parallel_stats.index_preparation_nanos,
        4096.0e9 / parallel_stats.index_preparation_nanos.max(1) as f64,
    );

    let datoms = (0..3)
        .map(|i| Datom {
            entity: make_eid(USER_PARTITION, 9000 + i).unwrap(),
            attribute: 1000,
            value: Value::Long(i as i64),
            tx: t_to_tx(1).unwrap(),
            added: true,
        })
        .collect::<Vec<_>>();
    let built = build_tree(
        IndexOrder::Eavt,
        false,
        datoms,
        &TreeConfig {
            max_leaf_datoms: 1,
            ..Default::default()
        },
    )
    .unwrap();
    let control =
        MaintenanceControl::new(Duration::from_secs(30), Arc::new(AtomicBool::new(false))).unwrap();
    let worker_control = control.clone();
    let connection = url.clone();
    let nodes = built.nodes.clone();
    let worker = std::thread::spawn(move || {
        let mut store = PostgresTreeStore::connect(&connection)
            .unwrap()
            .with_compressed_node_blocks(false)
            .with_maintenance_control(worker_control);
        let result = store.insert_nodes(
            nodes.iter().map(|(hash, bytes)| (*hash, bytes)),
            NodeUploadLimits {
                max_nodes: 1,
                max_bytes: 1024 * 1024,
            },
        );
        (result, store.stats())
    });
    wait(|| control.stats().pauses == 1);
    control.cancel();
    let (result, partial) = worker.join().unwrap();
    assert_eq!(result.unwrap_err().code, "maintenance/canceled");
    assert_eq!(partial.node_upload_batches, 1);
    let pace = MaintenanceControl::new(Duration::from_millis(2), Arc::new(AtomicBool::new(false)))
        .unwrap();
    let mut store = PostgresTreeStore::connect(url)
        .unwrap()
        .with_compressed_node_blocks(false)
        .with_maintenance_control(pace.clone());
    let started = Instant::now();
    store
        .insert_nodes(
            built.nodes.iter().map(|(hash, bytes)| (*hash, bytes)),
            NodeUploadLimits {
                max_nodes: 1,
                max_bytes: 1024 * 1024,
            },
        )
        .unwrap();
    let paced_elapsed = started.elapsed();
    assert_eq!(pace.stats().completed_batches, built.nodes.len() as u64);
    assert_eq!(pace.stats().pauses, built.nodes.len() as u64);
    assert!(store.stats().node_reuses >= 1);
    let mut unpaced = PostgresTreeStore::connect(url)
        .unwrap()
        .with_compressed_node_blocks(false);
    let started = Instant::now();
    unpaced
        .insert_nodes(
            built.nodes.iter().map(|(hash, bytes)| (*hash, bytes)),
            NodeUploadLimits {
                max_nodes: 1,
                max_bytes: 1024 * 1024,
            },
        )
        .unwrap();
    let unpaced_elapsed = started.elapsed();
    eprintln!(
        "canonical upload {} one-node batches: 2ms pacing={paced_elapsed:?} ({:.0} nodes/s), unpaced warmed retry={unpaced_elapsed:?} ({:.0} nodes/s); cancellation retained first valid batch; measured pacing {:?}",
        built.nodes.len(),
        built.nodes.len() as f64 / paced_elapsed.as_secs_f64(),
        built.nodes.len() as f64 / unpaced_elapsed.as_secs_f64(),
        pace.stats(),
    );
}
