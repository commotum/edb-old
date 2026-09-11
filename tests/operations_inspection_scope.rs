mod common;
use atomic_core::storage::log::LogRoot;
use atomic_core::storage::root::DatabaseRoot;
use atomic_core::storage::{BlockDatabase, BlockTransactor, BlockWriterOptions, PgBlockStore};
use atomic_core::*;
use std::time::Duration;

#[test]
fn another_database_corruption_does_not_contaminate_scoped_inspection_or_preview() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "operator_scope");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut databases = Vec::new();
    for name in ["a", "b"] {
        let database = BlockDatabase::create(&config, name, Schema::new()).unwrap();
        let mut writer =
            BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default())
                .unwrap();
        writer
            .transact(&TransactionRequest::new(
                "one",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: DB_DOC as u32,
                    value: Value::String(name.to_owned()).into(),
                }],
            ))
            .unwrap();
        writer.release().unwrap();
        databases.push(database);
    }
    let mut operator = PostgresOperator::connect_configured(&config).unwrap();
    let b = databases[1].reference_key()[10..].to_owned();
    let baseline = operator.inspect_database(&b, true).unwrap();
    assert!(baseline.healthy(), "{:?}", baseline.problems);
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reference = store
        .read_ref(&databases[0].reference_key())
        .unwrap()
        .unwrap();
    let root_id = reference.value.as_deref().unwrap().try_into().unwrap();
    let root = DatabaseRoot::decode(&root_id, &store.get(root_id).unwrap().unwrap()).unwrap();
    let entry = LogRoot::open(&mut store, root.log.unwrap())
        .unwrap()
        .read_record(&mut store, 1)
        .unwrap()
        .unwrap()
        .id;
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    sql.execute("UPDATE atomic_objects SET payload=set_byte(payload,octet_length(payload)-1,get_byte(payload,octet_length(payload)-1)#1) WHERE id=$1", &[&&entry[..]]).unwrap();
    let report = operator.inspect_database(&b, true).unwrap();
    assert!(report.healthy(), "{:?}", report.problems);
    assert_eq!(report.metrics, baseline.metrics);
    let bad = operator
        .inspect_database(&databases[0].reference_key()[10..], true)
        .unwrap();
    assert!(!bad.healthy());
    let preview = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(!preview.applied);
    assert_eq!(
        preview.collection.objects_removed, 0,
        "metadata preview makes no deletion promises for an unvalidated graph"
    );
}
