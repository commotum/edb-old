use atomic_core::{
    Attribute, BackgroundIndexingConfig, CapacityLimits, Cardinality, EntityRef, ErrorCategory,
    Keyword, PostgresIndexer, PostgresStore, Schema, TransactionRequest, TransactionService,
    TransactionServiceConfig, TxOp, TxValue, USER_PARTITION, Value, ValueType, make_eid,
};
use postgres::{Client, NoTls};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const ITEM_COUNT: u32 = 1_000;

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
            ITEM_COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn config(
    connection: &str,
    database_id: &str,
    holder: &str,
    capacity_limits: CapacityLimits,
) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: holder.to_owned(),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits,
    }
}

fn set_request(key: impl Into<String>, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Id(make_eid(USER_PARTITION, 42).unwrap()),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(value)),
        }],
    )
}

fn publication_count(client: &mut Client, database_id: &str) -> i64 {
    client
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0)
}

#[test]
fn missing_late_native_root_requires_explicit_admin_consolidation() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("startup_missing_native_root");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut setup = PostgresStore::connect(&connection).unwrap();
    let created = setup.create_database(&database_id, schema()).unwrap();
    drop(setup);

    // Basis 1 is the deliberate bounded exception: the first service creates
    // the bootstrap/application-schema root. Keep subsequent novelty below
    // the background threshold so there is definitely positive log history.
    let service = TransactionService::start_with_indexing(
        config(
            &connection,
            &database_id,
            "seed-missing-root",
            CapacityLimits::default(),
        ),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let committed = service
        .client()
        .transact(set_request("positive-history", 1), Duration::from_secs(5))
        .unwrap();
    assert!(committed.basis_t > created.basis_t());
    service.shutdown();

    // This is a deliberately destructive, test-only simulation of a missing
    // derived index. Disabling replica triggers makes the fault explicit and
    // leaves the authoritative log/head untouched.
    let mut sql = Client::connect(&connection, NoTls).unwrap();
    assert_eq!(publication_count(&mut sql, &database_id), 1);
    common::with_replica_triggers_disabled(&mut sql, |sql| {
        sql.execute(
            "DELETE FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )?;
        sql.execute(
            "DELETE FROM atomic_tree_manifests WHERE database_id = $1",
            &[&database_id],
        )?;
        Ok(())
    })
    .unwrap();
    assert_eq!(publication_count(&mut sql, &database_id), 0);

    let error = match TransactionService::start(config(
        &connection,
        &database_id,
        "must-not-rebuild",
        CapacityLimits::default(),
    )) {
        Ok(service) => {
            service.shutdown();
            panic!("ordinary startup unexpectedly rebuilt a missing late native root")
        }
        Err(error) => error,
    };
    assert_eq!(error.category, ErrorCategory::Unavailable);
    assert_eq!(error.code, "service/native-index-required");
    assert_eq!(error.details["cause"], "peer/exact-no-native-publication");
    assert_eq!(
        publication_count(&mut sql, &database_id),
        0,
        "failed ordinary activation performed hidden index work"
    );

    let built = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    assert_eq!(built.basis_t, committed.basis_t);
    assert!(built.input_datoms > 0);
    assert_eq!(publication_count(&mut sql, &database_id), 1);

    let recovered = TransactionService::start(config(
        &connection,
        &database_id,
        "after-admin-build",
        CapacityLimits::default(),
    ))
    .unwrap();
    assert_eq!(recovered.recovery_stats().base_t, committed.basis_t);
    assert_eq!(recovered.recovery_stats().tail_transactions, 0);
    recovered.shutdown();
}

#[test]
fn over_hard_startup_tail_requires_explicit_admin_consolidation() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("startup_over_hard_tail");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut setup = PostgresStore::connect(&connection).unwrap();
    setup.create_database(&database_id, schema()).unwrap();
    drop(setup);

    let service = TransactionService::start_with_indexing(
        config(
            &connection,
            &database_id,
            "seed-over-hard",
            CapacityLimits::default(),
        ),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let client = service.client();
    let mut final_basis = 0;
    for ordinal in 0..32 {
        final_basis = client
            .transact(
                set_request(format!("tail-{ordinal}"), i64::from(ordinal)),
                Duration::from_secs(5),
            )
            .unwrap()
            .basis_t;
    }
    service.shutdown();

    let mut sql = Client::connect(&connection, NoTls).unwrap();
    let publications_before = publication_count(&mut sql, &database_id);
    assert_eq!(publications_before, 1);

    let constrained = CapacityLimits {
        max_transaction_ops: 1,
        max_transaction_bytes: 1_024,
        ..CapacityLimits::default()
    };
    let error = match TransactionService::start_with_indexing(
        config(&connection, &database_id, "must-not-catch-up", constrained),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1,
            memory_index_max_bytes: 1,
        },
    ) {
        Ok(service) => {
            service.shutdown();
            panic!("ordinary startup unexpectedly consolidated an over-hard tail")
        }
        Err(error) => error,
    };
    assert_eq!(error.category, ErrorCategory::Unavailable);
    assert_eq!(error.code, "service/native-index-required");
    assert_eq!(error.details["cause"], "recent/hard-capacity");
    assert_eq!(
        error.details["cause_preflight_transactions"], "3",
        "writer startup must stop at the first prefix above the hard limit, including its configured one-transaction overshoot allowance"
    );
    assert_eq!(error.details["cause_tail_transactions"], "32");
    assert_eq!(
        error.details["cause_preflight_range_reads"], "1",
        "writer activation must stream the long tail once, not issue one SQL range read per scanned transaction and then reread it"
    );
    assert!(
        error.details["cause_preflight_accounted_bytes"]
            .parse::<u64>()
            .unwrap()
            > 1,
        "writer startup must reject on the first over-hard retained prefix"
    );
    assert_eq!(
        publication_count(&mut sql, &database_id),
        publications_before,
        "failed ordinary activation performed hidden catch-up indexing"
    );

    let built = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    assert_eq!(built.basis_t, final_basis);
    assert!(built.tail_datoms > 0);

    let recovered = TransactionService::start_with_indexing(
        config(
            &connection,
            &database_id,
            "after-admin-catch-up",
            constrained,
        ),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1,
            memory_index_max_bytes: 1,
        },
    )
    .unwrap();
    assert_eq!(recovered.recovery_stats().base_t, final_basis);
    assert_eq!(recovered.recovery_stats().tail_transactions, 0);
    recovered.shutdown();
}
