//! Explicit repair uses fresh isolated schemas; ordinary readers never replay.
use super::*;
use crate::storage::{BlockDatabase, BlockReader, BlockTransactor, BlockWriterOptions};
use crate::{
    Attribute, Cardinality, EntityRef, Keyword, PostgresConnectionConfig, PostgresOperator, Schema,
    TransactionRequest, TxOp, Value, ValueType,
};

struct Fixture {
    admin: postgres::Client,
    schema: String,
    config: PostgresConnectionConfig,
    database: BlockDatabase,
}
impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}
fn fixture() -> Option<Fixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block index recovery PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("block_recovery_{:032x}", crate::uuid_v7().unwrap());
    let mut admin = postgres::Client::connect(&url, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let scoped = if url.starts_with("postgres://") || url.starts_with("postgresql://") {
        format!(
            "{url}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
            if url.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{url} options='-csearch_path={schema},pg_catalog'")
    };
    let config = PostgresConnectionConfig::plaintext(scoped);
    PgBlockStore::install(&config).unwrap();
    let mut application = Schema::new();
    let mut value = Attribute::new(
        1000,
        Keyword::new("item", "value"),
        ValueType::Long,
        Cardinality::One,
    );
    value.indexed = true;
    application.install(value).unwrap();
    application
        .install(
            Attribute::new(
                1001,
                Keyword::new("item", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let database = BlockDatabase::create(&config, "recovery", application).unwrap();
    Some(Fixture {
        admin,
        schema,
        config,
        database,
    })
}
fn root(store: &mut PgBlockStore, database: &BlockDatabase) -> DatabaseRoot {
    let reference = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let id = reference.value.as_deref().unwrap().try_into().unwrap();
    DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap()
}
fn erase(f: &mut Fixture, id: ObjectId) {
    assert_eq!(
        f.admin
            .execute(
                &format!("DELETE FROM {}.atomic_objects WHERE id=$1", f.schema),
                &[&&id[..]],
            )
            .unwrap(),
        1,
        "corruption is confined to this newly created fixture"
    );
}

#[test]
fn explicit_recovery_repairs_current_reads_without_discarding_historical_damage() {
    let Some(mut f) = fixture() else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let first = writer
        .transact(
            &TransactionRequest::new(
                "first",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: 1000,
                        value: Value::Long(7).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: 1001,
                        value: Value::String("original search text".into()).into(),
                    },
                ],
            )
            .with_tx_instant(10),
        )
        .unwrap();
    let entity = first.tempids["item"];
    let second = writer
        .transact(
            &TransactionRequest::new(
                "second",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Id(entity),
                        attribute: 1000,
                        value: Value::Long(9).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Id(entity),
                        attribute: 1001,
                        value: Value::String("replacement search text".into()).into(),
                    },
                ],
            )
            .with_tx_instant(20),
        )
        .unwrap();
    let orders = [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ];
    let expected = [false, true]
        .into_iter()
        .flat_map(|history| {
            let value = if history {
                second.db_after.clone().history()
            } else {
                second.db_after.clone()
            };
            orders
                .into_iter()
                .map(move |order| value.datoms(order).unwrap())
        })
        .collect::<Vec<_>>();
    drop((first, second));
    writer.release().unwrap();
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let original = root(&mut store, &f.database);
    let old_index = original.indexes.unwrap();
    erase(&mut f, old_index);

    let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
    assert!(
        reader.capture(&f.database.reference_key()).is_err(),
        "ordinary open does not replay a damaged index"
    );
    let database_id = f.database.reference_key()["databases/".len()..].to_owned();
    let mut operator = PostgresOperator::connect_configured(&f.config).unwrap();
    let receipt = operator.consolidate_database(&database_id).unwrap();
    assert!(receipt.recovered);
    assert_eq!(receipt.basis_t, original.basis);
    assert!(receipt.input_datoms > 0);
    assert!(receipt.fulltext.is_some());
    let current = reader.capture(&f.database.reference_key()).unwrap();
    for (i, history) in [false, true].into_iter().enumerate() {
        let value = if history {
            current.database_value().history()
        } else {
            current.database_value()
        };
        for (j, order) in orders.into_iter().enumerate() {
            assert_eq!(
                value.datoms(order).unwrap(),
                expected[i * 4 + j],
                "history={history}, order={order:?}"
            );
        }
    }
    let repaired = root(&mut store, &f.database);
    assert_eq!(repaired.log, original.log);
    assert_eq!(repaired.metadata, original.metadata);
    assert_eq!(repaired.receipts, original.receipts);
    assert_ne!(repaired.indexes, original.indexes);
    assert!(
        store.get(old_index).unwrap().is_none(),
        "repair did not fabricate a historical descriptor"
    );
    let retained = super::super::read_authorization::ReadAuthorization::load(
        &mut store,
        repaired.read_authorization.unwrap(),
        &repaired.identity,
    )
    .unwrap();
    let key = super::super::read_authorization::index_key(&repaired.identity, old_index);
    assert!(
        super::super::receipts::RequestIndex::from_root(Some(retained.indexes_root()))
            .lookup(&mut store, key)
            .unwrap()
            .is_some()
    );
    assert!(
        !operator
            .inspect_database(&database_id, true)
            .unwrap()
            .healthy(),
        "deep inspection still reports damaged retained history"
    );
}

#[test]
fn cancelled_or_canonically_corrupt_recovery_never_publishes() {
    let Some(mut f) = fixture() else {
        return;
    };
    let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
    let capture = reader
        .capture_reference(&f.database.reference_key())
        .unwrap();
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let before = store.read_ref(&f.database.reference_key()).unwrap();
    let mut checks = 0;
    let cancelled = prepare_recovery(
        &mut store,
        &capture,
        &TreeConfig::default(),
        &crate::FulltextBuildLimits::default(),
        &mut || {
            checks += 1;
            if checks >= 3 {
                Err(SemanticError::new(
                    ErrorCategory::Interrupted,
                    "test/recovery-cancelled",
                    "cancelled during replay",
                ))
            } else {
                Ok(())
            }
        },
    );
    assert!(matches!(cancelled, Err(error) if error.code == "test/recovery-cancelled"));
    assert!(store.write_protection().is_none());
    assert_eq!(store.read_ref(&f.database.reference_key()).unwrap(), before);
    let original = root(&mut store, &f.database);
    let log = super::super::log::LogRoot::open(&mut store, original.log.unwrap()).unwrap();
    let entry = log
        .read_record(&mut store, original.basis)
        .unwrap()
        .unwrap()
        .id;
    erase(&mut f, original.indexes.unwrap());
    erase(&mut f, entry);
    let database_id = f.database.reference_key()["databases/".len()..].to_owned();
    assert!(
        PostgresOperator::connect_configured(&f.config)
            .unwrap()
            .consolidate_database(&database_id)
            .is_err()
    );
    assert_eq!(store.read_ref(&f.database.reference_key()).unwrap(), before);
    assert!(store.write_protection().is_none());
}
