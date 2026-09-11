mod common;
use atomic_core::persistent_tree::TreeConfig;
use atomic_core::{
    Attribute, BackgroundIndexingConfig, CapacityLimits, Cardinality, EntityRef, Keyword, Schema,
    TransactionRequest, TransactionService, TransactionServiceConfig, TxOp, TxValue, Value,
    ValueType,
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
    common::install(&connection).unwrap();
    let database_id = unique("writer_residency_bounded");
    let mut setup = common::TestStore::connect(&connection).unwrap();
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
    let tree_config = TreeConfig::default();
    assert!(bounded.resident_tree_root_children <= 8 * tree_config.max_directories_per_root);
    assert!(
        bounded.resident_tree_root_estimated_bytes
            <= 8 * tree_config.max_decoded_root_estimated_bytes()
    );
    assert!(bounded.resident_tree_root_children > 0);
    assert!(bounded.resident_tree_root_estimated_bytes > 0);
    assert!(bounded.resident_schema_attributes > 0);
    assert!(bounded.resident_schema_information_datoms > 0);
    assert!(bounded.resident_schema_estimated_bytes > 0);
    assert!(bounded.resident_ident_names > 0);
    assert!(bounded.resident_ident_entities > 0);
    assert!(bounded.resident_ident_estimated_bytes > 0);
    service.shutdown();

    // Cold activation opens that same exact root and an empty tail. A local
    // update performs bounded prefix/commitment work instead of replaying or
    // retaining the historical database.
    let mut restart_config = service_config(
        &connection,
        &database_id,
        &unique("writer-residency-restart"),
    );
    // Disable the discardable node cache for the measured operation so the
    // SQL read/byte counters have a deterministic physical-I/O witness.
    restart_config.capacity_limits.writer_tree_cache_entries = 0;
    restart_config.capacity_limits.writer_tree_cache_bytes = 0;
    let restarted = TransactionService::start_with_indexing(restart_config, indexing).unwrap();
    assert_eq!(restarted.recovery_stats().base_t, final_report.basis_t);
    assert_eq!(restarted.recovery_stats().tail_transactions, 0);
    let first_entity = first_entity.expect("first tempid was allocated");
    // Immutable commitment nodes are shared by content hash across database
    // ids. Use a per-run value so this witness necessarily exercises an
    // actual node INSERT instead of a legitimate global hash reuse.
    let localized_value = (SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos()
        % (i64::MAX as u128)) as i64
        + 1;
    let localized = restarted
        .client()
        .transact(
            TransactionRequest::new(
                "writer-residency-localized",
                vec![TxOp::Add {
                    entity: EntityRef::Id(first_entity),
                    attribute: ITEM_VALUE,
                    value: TxValue::Scalar(Value::Long(localized_value)),
                }],
            ),
            Duration::from_secs(5),
        )
        .unwrap();
    assert_eq!(localized.basis_t, final_report.basis_t + 1);
    let after = restarted.writer_residency_stats();
    assert_eq!(after.eager_database_values, 0);
    assert!(after.last_transaction_read_datoms < 128);
    assert!(after.last_transaction_source_read_datoms < 128);
    assert!(after.last_native_cursor_ranges > 0);
    assert_eq!(
        after.last_native_sql_reads,
        after
            .last_native_sql_root_reads
            .saturating_add(after.last_native_sql_directory_reads)
            .saturating_add(after.last_native_sql_leaf_reads)
    );
    assert_eq!(after.last_native_cache_misses, after.last_native_sql_reads);
    assert!(after.last_native_sql_reads > 0);
    assert!(after.last_native_sql_read_bytes > 0);
    assert!(after.last_native_sql_reads < 128);
    assert!(after.resident_schema_attributes > 0);
    assert!(after.resident_schema_estimated_bytes > 0);
    assert!(after.resident_ident_names > 0);
    assert!(after.resident_ident_estimated_bytes > 0);
    assert_eq!(after.tree_cache_entries, 0);
    assert_eq!(after.tree_cache_bytes, 0);
    assert!(after.resident_tree_root_children <= 8 * tree_config.max_directories_per_root);
    assert!(
        after.resident_tree_root_estimated_bytes
            <= 8 * tree_config.max_decoded_root_estimated_bytes()
    );
    assert!(after.resident_tree_root_children > 0);
    assert!(after.resident_tree_root_estimated_bytes > 0);
    restarted.shutdown();
}
