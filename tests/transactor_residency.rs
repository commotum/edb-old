use atomic_core::{
    Attribute, Cardinality, EntityRef, Keyword, PostgresStore, Schema, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp, TxValue, Value, ValueType,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

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

#[test]
fn eager_writer_baseline_exposes_total_history_residency() {
    let Some(connection) = connection() else {
        return;
    };
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let database_id = unique("writer_residency_baseline");
    let mut setup = PostgresStore::connect(&connection).unwrap();
    let initial_t = setup
        .create_database(&database_id, schema())
        .unwrap()
        .basis_t();

    let service = TransactionService::start(TransactionServiceConfig {
        connection: connection.clone(),
        database_id: database_id.clone(),
        holder_id: unique("writer-residency-holder"),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: atomic_core::CapacityLimits::default(),
    })
    .unwrap();
    let client = service.client();
    for ordinal in 0..64_u64 {
        client
            .transact(
                TransactionRequest::new(
                    format!("writer-residency-{ordinal}"),
                    vec![TxOp::Add {
                        entity: EntityRef::Temp(format!("item-{ordinal}")),
                        attribute: ITEM_VALUE,
                        value: TxValue::Scalar(Value::Long(ordinal as i64)),
                    }],
                ),
                Duration::from_secs(5),
            )
            .unwrap();
    }
    service.shutdown();

    // Force a cold writer recovery. Goal 16 deliberately replaces this
    // baseline assertion with a zero-eager/bounded-tier regression gate.
    let mut cold = PostgresStore::connect(&connection).unwrap();
    let recovered = cold.recover(&database_id).unwrap();
    let residency = cold.writer_residency_stats(&database_id);
    assert_eq!(recovered.basis_t(), initial_t + 64);
    assert_eq!(residency.eager_database_values, 1);
    assert!(residency.eager_current_facts >= 64);
    assert!(residency.eager_history_datoms >= 64);
}
