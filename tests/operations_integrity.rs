use atomic_core::{
    Attribute, CapacityLimits, Cardinality, EntityRef, ErrorCategory, Keyword, PostgresIndexer,
    PostgresOperator, PostgresStore, Schema, TxOp, TxValue, USER_PARTITION, Value, ValueType,
    make_eid,
};
use postgres::{Client, NoTls};
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

const ITEM_VALUE: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
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
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add(value: &str) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(user(42)),
        attribute: ITEM_VALUE,
        value: TxValue::Scalar(Value::String(value.into())),
    }
}

#[test]
fn inspector_crosschecks_authoritative_and_derived_state_and_metrics() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("inspect");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let first = common::transact(&service, "one", created.basis_t(), &[add("one")], 1_000);
    let second = common::transact(&service, "two", first.basis_t, &[add("two")], 2_000);
    service.shutdown();
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let report = operator.inspect_database(&database_id, true).unwrap();
    assert!(report.healthy(), "{:?}", report.problems);
    assert_eq!(report.metrics.basis_t, second.basis_t);
    assert_eq!(report.metrics.transactions, second.basis_t);
    assert_eq!(report.metrics.requests, second.basis_t);
    assert_eq!(report.metrics.index_lag, 0);
    assert!(report.metrics.history_datoms >= 4);
    assert!(report.metrics.transaction_bytes > 0);
    assert!(report.metrics.tree_publications > 0);
    assert!(report.metrics.tree_nodes > 0);

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let corrupt_basis = i64::try_from(first.basis_t).unwrap();
    let original: Vec<u8> = client
        .query_one(
            "SELECT payload FROM atomic_transactions WHERE database_id = $1 AND basis_t = $2",
            &[&database_id, &corrupt_basis],
        )
        .unwrap()
        .get(0);
    client
        .batch_execute("ALTER TABLE atomic_transactions DISABLE TRIGGER USER")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_transactions SET payload = set_byte(payload, 16, \
             get_byte(payload, 16) # 1) WHERE database_id = $1 AND basis_t = $2",
            &[&database_id, &corrupt_basis],
        )
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_transactions ENABLE TRIGGER USER")
        .unwrap();
    let corrupt = operator.inspect_database(&database_id, true).unwrap();
    assert!(!corrupt.healthy());
    assert!(
        corrupt
            .problems
            .iter()
            .any(|problem| problem.code.contains("hash") || problem.code.contains("checksum"))
    );
    client
        .batch_execute("ALTER TABLE atomic_transactions DISABLE TRIGGER USER")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_transactions SET payload = $2 WHERE database_id = $1 AND basis_t = $3",
            &[&database_id, &&original[..], &corrupt_basis],
        )
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_transactions ENABLE TRIGGER USER")
        .unwrap();
    assert!(
        operator
            .inspect_database(&database_id, true)
            .unwrap()
            .healthy()
    );
}

#[test]
fn configured_operation_byte_and_history_limits_fail_before_publication() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("capacity");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let initial_basis = created.basis_t();
    let op_limited = common::start_service_with_limits(
        &connection,
        &database_id,
        CapacityLimits {
            max_transaction_ops: 1,
            ..CapacityLimits::default()
        },
    );
    let error = common::try_transact(
        &op_limited,
        "too-many",
        initial_basis,
        &[add("one"), add("two")],
        1_000,
    )
    .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Busy, "postgres/transaction-op-capacity")
    );
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        initial_basis
    );
    op_limited.shutdown();

    let byte_limited = common::start_service_with_limits(
        &connection,
        &database_id,
        CapacityLimits {
            max_transaction_bytes: 1,
            ..CapacityLimits::default()
        },
    );
    assert_eq!(
        common::try_transact(
            &byte_limited,
            "too-large",
            initial_basis,
            &[add("one")],
            1_000,
        )
        .unwrap_err()
        .code,
        "service/request-byte-capacity"
    );
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        initial_basis
    );
    byte_limited.shutdown();

    let history_limited = common::start_service_with_limits(
        &connection,
        &database_id,
        CapacityLimits {
            max_history_transactions: initial_basis + 1,
            ..CapacityLimits::default()
        },
    );
    common::transact(&history_limited, "one", initial_basis, &[add("one")], 1_000);
    assert_eq!(
        common::try_transact(
            &history_limited,
            "two",
            initial_basis + 1,
            &[add("two")],
            2_000,
        )
        .unwrap_err()
        .code,
        "postgres/history-capacity"
    );
    assert!(
        common::transact(&history_limited, "one", initial_basis, &[add("one")], 1_000,).replayed,
        "capacity limits do not hide an already durable outcome"
    );
    history_limited.shutdown();
}
