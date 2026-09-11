//! Administrative integrity checks authenticate the current block engine.
mod common;
use atomic_core::storage::log::LogRoot;
use atomic_core::storage::root::DatabaseRoot;
use atomic_core::storage::{
    BlockDatabase, BlockReader, BlockTransactor, BlockWriterOptions, CasOutcome, IndexDescriptor,
    PgBlockStore,
};
use atomic_core::*;

fn fixture(
    label: &str,
) -> Option<(
    common::PostgresFixture,
    PostgresConnectionConfig,
    BlockDatabase,
)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let database = BlockDatabase::create(&config, label, schema).unwrap();
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    writer
        .transact(&TransactionRequest::new(
            "one",
            vec![TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: 1000,
                value: Value::Long(7).into(),
            }],
        ))
        .unwrap();
    writer.release().unwrap();
    Some((fixture, config, database))
}

#[test]
fn shallow_metrics_are_explicit_and_deep_inspection_detects_corrupt_log_content() {
    let Some((fixture, config, database)) = fixture("inspect_integrity") else {
        return;
    };
    let mut operator = PostgresOperator::connect_configured(&config).unwrap();
    let identifier = database.reference_key()[10..].to_owned();
    let shallow = operator.inspect_database(&identifier, false).unwrap();
    assert!(shallow.healthy());
    assert_eq!(shallow.metrics.basis_t, 2);
    assert_eq!(shallow.metrics.transactions, 2);
    assert_eq!(shallow.metrics.requests, None);
    assert_eq!(shallow.metrics.reachable_objects, None);
    let deep = operator.inspect_database(&identifier, true).unwrap();
    assert!(deep.healthy(), "{:?}", deep.problems);
    assert_eq!(deep.metrics.requests, Some(1));
    assert!(deep.metrics.current_datoms.unwrap() > 0);
    assert!(deep.metrics.transaction_bytes.unwrap() > 0);
    assert!(deep.metrics.reachable_objects.unwrap() > 0);
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reference = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let root_id = reference.value.as_deref().unwrap().try_into().unwrap();
    let root = DatabaseRoot::decode(&root_id, &store.get(root_id).unwrap().unwrap()).unwrap();
    let entry = LogRoot::open(&mut store, root.log.unwrap())
        .unwrap()
        .read_record(&mut store, 2)
        .unwrap()
        .unwrap()
        .id;
    let original = store.get(entry).unwrap().unwrap();
    let mut corrupt = original.clone();
    *corrupt.last_mut().unwrap() ^= 1;
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    sql.execute(
        "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
        &[&&entry[..], &corrupt],
    )
    .unwrap();
    let report = operator.inspect_database(&identifier, true).unwrap();
    assert!(!report.healthy());
    assert!(
        report
            .problems
            .iter()
            .any(|p| p.code == "storage/object-corrupt")
    );
    sql.execute(
        "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
        &[&&entry[..], &original],
    )
    .unwrap();
    assert!(
        operator
            .inspect_database(&identifier, true)
            .unwrap()
            .healthy()
    );
}

#[test]
fn deep_inspection_rejects_coherent_indexes_that_disagree_with_authenticated_log() {
    let Some((_fixture, config, database)) = fixture("inspect_projection") else {
        return;
    };
    let reader = BlockReader::connect(&config, Default::default()).unwrap();
    let value = reader
        .capture(&database.reference_key())
        .unwrap()
        .database_value();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reference = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let root_id = reference.value.as_deref().unwrap().try_into().unwrap();
    let mut root = DatabaseRoot::decode(&root_id, &store.get(root_id).unwrap().unwrap()).unwrap();
    let old_index = root.indexes.unwrap();
    let mut indexes =
        IndexDescriptor::decode(&old_index, &store.get(old_index).unwrap().unwrap()).unwrap();
    indexes.basis = root.basis;
    indexes.trees.clear();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let mut datoms = if history {
                value.clone().history()
            } else {
                value.clone()
            }
            .datoms(order)
            .unwrap();
            for datom in &mut datoms {
                if datom.attribute == 1000 {
                    datom.value = Value::Long(99);
                }
            }
            datoms.sort_by(|a, b| a.cmp_in(b, order));
            let built = atomic_core::persistent_tree::build_tree(
                order,
                history,
                datoms,
                &Default::default(),
            )
            .unwrap();
            for (_, bytes) in built.nodes.iter() {
                store.put(bytes).unwrap();
            }
            indexes.trees.push(built.descriptor);
        }
    }
    root.indexes = Some(store.put(&indexes.encode().unwrap()).unwrap());
    let forged = store.put(&root.encode().unwrap()).unwrap();
    assert!(matches!(
        store
            .compare_exchange(
                &database.reference_key(),
                Some(reference.revision),
                Some(&forged)
            )
            .unwrap(),
        CasOutcome::Applied(_)
    ));
    let identifier = database.reference_key()[10..].to_owned();
    let mut operator = PostgresOperator::connect_configured(&config).unwrap();
    assert!(
        operator
            .inspect_database(&identifier, false)
            .unwrap()
            .healthy()
    );
    let report = operator.inspect_database(&identifier, true).unwrap();
    assert!(
        !report.healthy(),
        "structurally valid trees are not proof of semantic agreement"
    );
}
