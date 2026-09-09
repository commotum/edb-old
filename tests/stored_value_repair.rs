use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, Keyword, Peer, PostgresIndexer, PostgresStore,
    Schema, TxOp, TxValue, USER_PARTITION, Value, ValueType, make_eid,
};
use bigdecimal::BigDecimal;
use std::str::FromStr;
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

const AMOUNT: u32 = 1_000;

fn schema() -> Schema {
    let mut schema = Schema::new();
    let mut amount = Attribute::new(
        AMOUNT,
        Keyword::new("measurement", "amount"),
        ValueType::BigDec,
        Cardinality::Many,
    );
    amount.indexed = true;
    schema.install(amount).unwrap();
    schema
}

fn scaled_values() -> [Value; 2] {
    [
        Value::BigDec(BigDecimal::from_str("1.0").unwrap()),
        Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
    ]
}

fn additions(entity: u64, reverse: bool) -> Vec<TxOp> {
    let mut values = scaled_values();
    if reverse {
        values.reverse();
    }
    values
        .into_iter()
        .map(|value| TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: AMOUNT,
            value: TxValue::Scalar(value),
        })
        .collect()
}

fn scales(database: &Database, entity: u64) -> Vec<i64> {
    let mut scales: Vec<_> = database
        .values(entity, AMOUNT)
        .into_iter()
        .map(|value| match value {
            Value::BigDec(value) => value.fractional_digit_count(),
            other => panic!("expected BigDecimal, got {other:?}"),
        })
        .collect();
    scales.sort_unstable();
    scales
}

#[test]
fn cardinality_many_retains_storage_distinct_equal_magnitudes() {
    let entity = make_eid(USER_PARTITION, 42).unwrap();
    let before = Database::new(schema()).unwrap();
    let forward = before.with(&additions(entity, false), 1_000).unwrap();
    let reverse = before.with(&additions(entity, true), 1_000).unwrap();

    assert_eq!(scales(&forward.db_after, entity), vec![1, 2]);
    assert_eq!(scales(&reverse.db_after, entity), vec![1, 2]);
    assert_eq!(forward.tx_data, reverse.tx_data);
    forward.db_after.validate_invariants().unwrap();
    reverse.db_after.validate_invariants().unwrap();
}

#[test]
fn scale_distinctions_survive_postgres_log_base_and_peer_recovery() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let suffix = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    let database_id = format!("goal10_bigdec_{}_{}", std::process::id(), suffix);
    let entity = make_eid(USER_PARTITION, 42).unwrap();

    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    common::transact(
        &service,
        "scaled-values",
        created.basis_t(),
        &additions(entity, false),
        1_000,
    );

    let recovered = store.recover(&database_id).unwrap();
    assert_eq!(scales(&recovered, entity), vec![1, 2]);

    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();
    let peer = Peer::connect(&connection, &database_id, 1).unwrap();
    assert_eq!(scales(&peer.db_compatibility(), entity), vec![1, 2]);
    peer.db_compatibility().validate_invariants().unwrap();
    service.shutdown();
}
