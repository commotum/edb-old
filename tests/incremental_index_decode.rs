//! Bulk path reuse must preserve exact stored-value matching across leaves.
mod common;
use atomic_core::{
    Attribute, Cardinality, EntityRef, IndexOrder, Keyword, Peer, PostgresIndexer,
    PostgresMigrator, PostgresStore, Schema, TransactionRequest, TxOp, USER_PARTITION, Value,
    ValueType, make_eid,
};
use bigdecimal::BigDecimal;
use std::str::FromStr;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

#[test]
fn cached_index_lookup_preserves_decimal_scale_ties_across_leaf_boundaries() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let database = format!(
        "index_decode_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    let mut schema = Schema::new();
    let mut amount = Attribute::new(
        1_000,
        Keyword::new("measurement", "amount"),
        ValueType::BigDec,
        Cardinality::Many,
    );
    amount.indexed = true;
    schema.install(amount).unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.create_database(&database, schema).unwrap();
    let writer = common::start_service(&connection, &database);
    let entity = make_eid(USER_PARTITION, 42).unwrap();
    let values =
        ["1.0", "1.00", "1.000"].map(|text| Value::BigDec(BigDecimal::from_str(text).unwrap()));
    let first = writer
        .client()
        .transact(
            TransactionRequest::new(
                "scales",
                values
                    .iter()
                    .map(|value| TxOp::Add {
                        entity: EntityRef::Id(entity),
                        attribute: 1_000,
                        value: value.clone().into(),
                    })
                    .collect(),
            ),
            Duration::from_secs(30),
        )
        .unwrap();
    drop(first);
    let mut indexer = PostgresIndexer::connect(&connection, &database)
        .unwrap()
        .with_segment_datoms(1)
        .unwrap();
    indexer.consolidate().unwrap();
    let old = Peer::connect(&connection, &database, 8)
        .unwrap()
        .database_value();
    let retracted = writer
        .client()
        .transact(
            TransactionRequest::new(
                "retract-middle-scale",
                vec![TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: Some(values[1].clone().into()),
                }],
            ),
            Duration::from_secs(30),
        )
        .unwrap();
    drop(retracted);
    let receipt = indexer.consolidate().unwrap();
    assert!(!receipt.reused);
    let current = Peer::connect(&connection, &database, 8)
        .unwrap()
        .database_value();
    assert_eq!(old.values(entity, 1_000).unwrap(), values.to_vec());
    assert_eq!(
        current.values(entity, 1_000).unwrap(),
        vec![values[0].clone(), values[2].clone()]
    );
    let recovered = store.recover(&database).unwrap();
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            current.datoms(order).unwrap(),
            recovered.datoms(atomic_core::View::Current, order)
        );
        assert_eq!(
            current.clone().history().datoms(order).unwrap(),
            recovered.datoms(atomic_core::View::History, order)
        );
    }
    writer.shutdown();
}
