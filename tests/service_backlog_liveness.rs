use atomic_core::{
    Attribute, BackgroundIndexingConfig, CapacityLimits, Cardinality, EntityRef, Keyword,
    PostgresStore, Schema, TransactionRequest, TransactionService, TransactionServiceConfig, TxOp,
    TxValue, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const ITEM_VALUE: u32 = 1_000;
const TEMPID_BYTES: usize = 8 * 1024;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .expect("system clock precedes Unix epoch")
            .as_nanos()
    )
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn request(ordinal: u64) -> TransactionRequest {
    let mut tempid = format!("item-{ordinal}-");
    tempid.push_str(&"x".repeat(TEMPID_BYTES));
    TransactionRequest::new(
        format!("large-envelope-{ordinal}"),
        vec![TxOp::Add {
            entity: EntityRef::Temp(tempid),
            attribute: ITEM_VALUE,
            value: TxValue::Scalar(Value::Long(ordinal as i64)),
        }],
    )
}

fn wait_for(
    service: &TransactionService,
    predicate: impl Fn(&atomic_core::BackgroundIndexingStats) -> bool,
) -> atomic_core::BackgroundIndexingStats {
    let deadline = Instant::now() + Duration::from_secs(15);
    loop {
        let stats = service.background_indexing_stats();
        if predicate(&stats) {
            return stats;
        }
        assert!(Instant::now() < deadline, "indexing timed out: {stats:?}");
        std::thread::sleep(Duration::from_millis(10));
    }
}

#[test]
fn large_envelopes_schedule_exact_backlog_before_recent_capacity_and_keep_progressing() {
    let Some(connection) = connection() else {
        return;
    };
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let database_id = unique("service_exact_backlog");
    let mut setup = PostgresStore::connect(&connection).unwrap();
    let initial_basis = setup
        .create_database(&database_id, schema())
        .unwrap()
        .basis_t();
    drop(setup);

    let capacity_limits = CapacityLimits {
        max_transaction_ops: 8,
        max_transaction_bytes: 24 * 1024,
        ..CapacityLimits::default()
    };
    let service = TransactionService::start_with_indexing(
        TransactionServiceConfig {
            connection: connection.clone(),
            database_id: database_id.clone(),
            holder_id: unique("service-exact-backlog-holder"),
            lease_duration: Duration::from_secs(4),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 4,
            capacity_limits,
        },
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 4 * 1024,
            memory_index_max_bytes: 12 * 1024,
        },
    )
    .unwrap();
    let client = service.client();
    wait_for(&service, |stats| {
        stats.published_basis_t == initial_basis && stats.total_bytes == 0
    });

    // Freeze publication long enough to compare the scheduler's counter with
    // the exact RecentTier retained by the writer. One datom has a tiny value,
    // but its durable envelope and tempid map are intentionally large.
    let mut blocker = Client::connect(&connection, NoTls).unwrap();
    let mut publication_lock = blocker.transaction().unwrap();
    publication_lock
        .batch_execute("LOCK TABLE atomic_tree_publications IN SHARE MODE")
        .unwrap();
    let first = client.transact(request(0), Duration::from_secs(3)).unwrap();
    let pressured = wait_for(&service, |stats| {
        stats.job_in_flight && stats.target_basis_t == first.basis_t
    });
    let writer = service.writer_residency_stats();
    assert_eq!(pressured.total_transactions, 1);
    assert_eq!(pressured.total_datoms, first.tx_data.len() as u64);
    assert_eq!(
        pressured.total_bytes, writer.recent_accounted_bytes,
        "scheduler and hard-cap admission must use one retained-entry account"
    );
    assert!(
        pressured.total_bytes > 12 * 1024,
        "large tempid/envelope must be visible to the hard-pressure gate"
    );

    // The next request is parked without assessment while the exact backlog
    // is over max. Releasing publication must wake it and preserve progress.
    let parked = client.submit(request(1)).unwrap();
    wait_for(&service, |stats| stats.backpressure_stalls >= 1);
    publication_lock.commit().unwrap();
    let second = parked.wait(Duration::from_secs(10)).unwrap();
    assert_eq!(second.basis_t, first.basis_t + 1);

    // Under the old datom-only estimate these transactions never reached the
    // 4 KiB scheduling threshold before the RecentTier hard cap rejected an
    // append forever. Repeated large envelopes now alternate naturally with
    // indexing and every admitted request completes.
    let mut final_basis = second.basis_t;
    for ordinal in 2..24 {
        let report = client
            .transact(request(ordinal), Duration::from_secs(10))
            .unwrap();
        assert_eq!(report.basis_t, final_basis + 1);
        final_basis = report.basis_t;
    }
    let caught_up = wait_for(&service, |stats| {
        stats.published_basis_t >= final_basis && stats.total_bytes == 0 && !stats.job_in_flight
    });
    assert!(caught_up.jobs_completed >= 2);
    assert_eq!(caught_up.jobs_failed, 0);
    assert!(client.is_available());
    service.shutdown();
}
