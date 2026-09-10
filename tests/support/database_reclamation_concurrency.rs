use super::*;
use std::sync::{
    Arc,
    atomic::{AtomicBool, Ordering},
};

struct CollectorGuard {
    stop: Arc<AtomicBool>,
    thread: Option<std::thread::JoinHandle<usize>>,
}
impl CollectorGuard {
    fn join(mut self) -> usize {
        self.thread.take().unwrap().join().unwrap()
    }
}
impl Drop for CollectorGuard {
    fn drop(&mut self) {
        self.stop.store(true, Ordering::Release);
        if let Some(thread) = self.thread.take() {
            let _ = thread.join();
        }
    }
}

#[test]
fn frontier_discovery_keeps_unrelated_projection_writer_fences_available() {
    let Some(f) = fixture("terminal_frontier_fences") else {
        return;
    };
    let old = seed(&f.connection, "old", 4);
    DatabaseCatalog::connect(&f.connection)
        .unwrap()
        .retire("old")
        .unwrap();
    let mut writer_pin = postgres::Client::connect(&f.connection, postgres::NoTls).unwrap();
    writer_pin.batch_execute("SELECT pg_advisory_lock_shared(atomic_fulltext_gc_pin_key()); SELECT pg_advisory_lock_shared(atomic_semantic_commitment_gc_pin_key())").unwrap();
    let mut operator = PostgresOperator::connect(&f.connection).unwrap();
    let mut decoded = 0;
    for _ in 0..32 {
        let preview = operator
            .preview_retired_database_reclamation(&old.database_id, &old.lineage_id, Duration::ZERO)
            .unwrap();
        let applied = operator
            .reclaim_retired_database(&old.database_id, &old.lineage_id, Duration::ZERO)
            .unwrap();
        assert_eq!(preview.rows_selected, applied.rows_selected);
        assert_eq!(
            applied.rows_removed, 0,
            "fixture stays in frontier discovery"
        );
        decoded += applied.objects_read;
    }
    assert!(
        decoded > 0,
        "exercise child traversal, not only a catalog seed"
    );
    writer_pin
        .batch_execute("SELECT pg_advisory_unlock_all()")
        .unwrap();
}

#[test]
fn unrelated_normal_writer_remains_healthy_during_physical_reclamation() {
    let Some(f) = fixture("terminal_concurrent_writer") else {
        return;
    };
    let old = seed(&f.connection, "old", 8);
    seed(&f.connection, "live", 4);
    DatabaseCatalog::connect(&f.connection)
        .unwrap()
        .retire("old")
        .unwrap();
    let mut operator = PostgresOperator::connect(&f.connection).unwrap();
    let mut sql = postgres::Client::connect(&f.connection, postgres::NoTls).unwrap();
    // Reach physical ownership release first: the test must not pass solely
    // while the collector performs cheap frontier seeding.
    loop {
        retry(|| {
            operator.reclaim_retired_database(&old.database_id, &old.lineage_id, Duration::ZERO)
        });
        if !sql
            .query_one(
                "SELECT EXISTS(SELECT 1 FROM atomic_heads WHERE database_id=$1)",
                &[&old.database_id],
            )
            .unwrap()
            .get::<_, bool>(0)
        {
            break;
        }
    }
    let semantic_candidates = sql.query(
        "SELECT object_hash FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=2",
        &[&old.database_id],
    ).unwrap().into_iter().map(|row|row.get::<_,Vec<u8>>(0)).collect::<Vec<_>>();
    let writer = TransactionService::start_with_indexing(
        TransactionServiceConfig {
            connection: f.connection.clone(),
            database_id: "live".into(),
            holder_id: "concurrent-normal-writer".into(),
            lease_duration: Duration::from_secs(4),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 4,
            capacity_limits: CapacityLimits::default(),
        },
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1,
            memory_index_max_bytes: 1_048_576,
        },
    )
    .unwrap();
    let connection = f.connection.clone();
    let stop = Arc::new(AtomicBool::new(false));
    let stopping = stop.clone();
    let thread = std::thread::spawn(move || {
        let mut operator = PostgresOperator::connect(&connection).unwrap();
        let mut batches = 0;
        loop {
            if stopping.load(Ordering::Acquire) {
                return batches;
            }
            let report = retry(|| {
                operator.reclaim_retired_database(&old.database_id, &old.lineage_id, Duration::ZERO)
            });
            batches += 1;
            if report.complete {
                return batches;
            }
            assert!(batches < 10000);
        }
    });
    // Even an assertion failure stops/joins the worker before the owning
    // PostgresFixture can remove its isolated schema.
    let collector = CollectorGuard {
        stop,
        thread: Some(thread),
    };
    let mut last_basis = 0;
    for i in 0..16 {
        let outcome = writer.client().transact(
            TransactionRequest::new(
                format!("normal-{i}"),
                vec![TxOp::Add {
                    entity: EntityRef::Temp("new".into()),
                    attribute: 1000,
                    value: Value::Long(i).into(),
                }],
            )
            .with_tx_instant(100 + i),
            Duration::from_secs(30),
        );
        assert!(
            outcome.is_ok(),
            "normal writer result={:?}; background={:?}",
            outcome.as_ref().err(),
            writer.background_indexing_stats().last_failure
        );
        last_basis = outcome.unwrap().basis_t;
        assert!(writer.background_indexing_stats().last_failure.is_none());
    }
    let deadline = Instant::now() + Duration::from_secs(30);
    loop {
        let stats = writer.background_indexing_stats();
        assert!(
            stats.last_failure.is_none(),
            "background failed during reclamation: {:?}",
            stats.last_failure
        );
        if stats.published_basis_t >= last_basis {
            assert!(stats.jobs_completed > 0);
            break;
        }
        assert!(
            Instant::now() < deadline,
            "normal writer indexing did not converge: {stats:?}"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
    let batches = collector.join();
    let physically_removed:i64 = sql.query_one(
        "SELECT count(*) FROM unnest($1::bytea[]) candidate(hash) WHERE NOT EXISTS(SELECT 1 FROM atomic_semantic_commitment_nodes n WHERE n.node_hash=candidate.hash)",
        &[&semantic_candidates],
    ).unwrap().get(0);
    assert!(
        physically_removed > 0,
        "concurrent fixture must delete exclusive semantic pages, not only shared-object frontier rows"
    );
    writer.shutdown();
    eprintln!(
        "concurrent physical reclamation:16normal commits+background publication, collector_batches={batches} semantic_pages_removed={physically_removed}"
    );
}
