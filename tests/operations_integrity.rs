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

fn replace_immutable_transaction_content(client: &mut Client, content_hash: &[u8], payload: &[u8]) {
    // Corruption tests must bypass the immutable-value guard, but must never
    // leave that guard disabled for another session. PostgreSQL makes ALTER
    // TABLE transactional, so the payload replacement and trigger restoration
    // become visible together or not at all.
    let mut transaction = client.transaction().unwrap();
    transaction
        .batch_execute(
            "ALTER TABLE atomic_transaction_contents \
             DISABLE TRIGGER atomic_transaction_contents_immutable",
        )
        .unwrap();
    assert_eq!(
        transaction
            .execute(
                "UPDATE atomic_transaction_contents SET payload = $2 \
                 WHERE content_hash = $1",
                &[&content_hash, &payload],
            )
            .unwrap(),
        1
    );
    transaction
        .batch_execute(
            "ALTER TABLE atomic_transaction_contents \
             ENABLE TRIGGER atomic_transaction_contents_immutable",
        )
        .unwrap();
    transaction.commit().unwrap();
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
    let authoritative = client
        .query_one(
            "SELECT h.log_generation, t.content_hash, c.payload, \
                    (SELECT count(*) FROM atomic_generation_transactions referenced \
                      WHERE referenced.content_hash = t.content_hash) \
               FROM atomic_heads h \
               JOIN atomic_generation_transactions t \
                 ON t.database_id = h.database_id \
                AND t.generation = h.log_generation AND t.basis_t = $2 \
               JOIN atomic_transaction_contents c ON c.content_hash = t.content_hash \
              WHERE h.database_id = $1 AND h.log_generation > 0",
            &[&database_id, &corrupt_basis],
        )
        .unwrap();
    let generation: i64 = authoritative.get(0);
    let content_hash: Vec<u8> = authoritative.get(1);
    let original: Vec<u8> = authoritative.get(2);
    let references: i64 = authoritative.get(3);
    assert!(
        generation > 0,
        "the current log must use native generations"
    );
    assert_eq!(
        references, 1,
        "the test must not corrupt ATLC content shared by another generation"
    );
    let mut damaged = original.clone();
    damaged[16] ^= 1;
    replace_immutable_transaction_content(&mut client, &content_hash, &damaged);

    // Capture the result, restore the immutable value, and prove recovery
    // before asserting on the expected failure. A changed diagnostic cannot
    // strand corrupt shared storage in the test cluster.
    let corrupt = operator.inspect_database(&database_id, true);
    replace_immutable_transaction_content(&mut client, &content_hash, &original);
    let restored = operator.inspect_database(&database_id, true);

    let corrupt = corrupt.unwrap();
    assert!(!corrupt.healthy());
    assert!(
        corrupt
            .problems
            .iter()
            .any(|problem| problem.code.contains("hash") || problem.code.contains("checksum"))
    );
    let restored = restored.unwrap();
    assert!(restored.healthy(), "{:?}", restored.problems);
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
