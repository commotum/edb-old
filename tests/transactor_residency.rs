use atomic_core::{
    Attribute, BackgroundIndexingConfig, CapacityLimits, Cardinality, EntityRef, Keyword,
    PostgresStore, Schema, TransactionRequest, TransactionService, TransactionServiceConfig, TxOp,
    TxValue, Value, ValueType,
};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const ITEM_VALUE: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
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

fn service_config(connection: &str, database_id: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: holder.to_owned(),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits {
            max_transaction_ops: 128,
            max_transaction_bytes: 64 * 1024,
            writer_tree_cache_entries: 64,
            writer_tree_cache_bytes: 1024 * 1024,
            ..CapacityLimits::default()
        },
    }
}

fn wait_until_indexed(service: &TransactionService, basis_t: u64) {
    let deadline = Instant::now() + Duration::from_secs(20);
    loop {
        let stats = service.background_indexing_stats();
        if stats.published_basis_t >= basis_t && stats.total_bytes == 0 && !stats.job_in_flight {
            return;
        }
        assert!(Instant::now() < deadline, "indexing timed out: {stats:?}");
        std::thread::sleep(Duration::from_millis(10));
    }
}

fn item_request(key: String, ordinal: u64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Temp(format!("item-{ordinal}")),
            attribute: ITEM_VALUE,
            value: TxValue::Scalar(Value::Long(ordinal as i64)),
        }],
    )
}

#[test]
fn production_writer_residency_is_bounded_by_recent_and_cache_tiers() {
    let Some(connection) = connection() else {
        return;
    };
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let database_id = unique("writer_residency_bounded");
    let mut setup = PostgresStore::connect(&connection).unwrap();
    let initial_t = setup
        .create_database(&database_id, schema())
        .unwrap()
        .basis_t();

    let indexing = BackgroundIndexingConfig {
        memory_index_threshold_bytes: 1,
        memory_index_max_bytes: 8 * 1024,
    };
    let service = TransactionService::start_with_indexing(
        service_config(
            &connection,
            &database_id,
            &unique("writer-residency-holder"),
        ),
        indexing,
    )
    .unwrap();
    let client = service.client();
    let mut final_report = None;
    let mut first_entity = None;
    for ordinal in 0..128_u64 {
        let report = client
            .transact(
                item_request(format!("writer-residency-{ordinal}"), ordinal),
                Duration::from_secs(5),
            )
            .unwrap();
        if ordinal == 0 {
            first_entity = report.tempids.get("item-0").copied();
        }
        final_report = Some(report);
    }
    let final_report = final_report.unwrap();
    assert_eq!(final_report.basis_t, initial_t + 128);
    wait_until_indexed(&service, final_report.basis_t);

    // Replaying the last request causes the writer to adopt the completed
    // publication without adding novelty. The retained value is now exactly
    // the durable root, regardless of the 128-transaction history behind it.
    let replay = client
        .transact(
            item_request("writer-residency-127".to_owned(), 127),
            Duration::from_secs(5),
        )
        .unwrap();
    assert!(replay.replayed);
    let bounded = service.writer_residency_stats();
    assert_eq!(bounded.eager_database_values, 0);
    assert_eq!(bounded.eager_current_facts, 0);
    assert_eq!(bounded.eager_history_datoms, 0);
    assert_eq!(bounded.recent_datoms, 0);
    assert!(bounded.tree_cache_entries <= 64);
    assert!(bounded.tree_cache_bytes <= 1024 * 1024);
    service.shutdown();

    // Cold activation opens that same exact root and an empty tail. A local
    // update performs bounded prefix/commitment work instead of replaying or
    // retaining the historical database.
    let restarted = TransactionService::start_with_indexing(
        service_config(
            &connection,
            &database_id,
            &unique("writer-residency-restart"),
        ),
        indexing,
    )
    .unwrap();
    assert_eq!(restarted.recovery_stats().base_t, final_report.basis_t);
    assert_eq!(restarted.recovery_stats().tail_transactions, 0);
    let first_entity = first_entity.expect("first tempid was allocated");
    let localized = restarted
        .client()
        .transact(
            TransactionRequest::new(
                "writer-residency-localized",
                vec![TxOp::Add {
                    entity: EntityRef::Id(first_entity),
                    attribute: ITEM_VALUE,
                    value: TxValue::Scalar(Value::Long(10_000)),
                }],
            ),
            Duration::from_secs(5),
        )
        .unwrap();
    assert_eq!(localized.basis_t, final_report.basis_t + 1);
    let after = restarted.writer_residency_stats();
    assert_eq!(after.eager_database_values, 0);
    assert!(after.last_transaction_read_datoms < 128);
    assert!(after.last_commitment_node_visits < 128);
    assert!(after.tree_cache_entries <= 64);
    assert!(after.tree_cache_bytes <= 1024 * 1024);
    restarted.shutdown();
}
