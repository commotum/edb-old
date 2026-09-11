use atomic_core::storage::{BlockDatabase, BlockTransactor, BlockWriterOptions};
use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, IndexPrefix, Keyword, Peer, Schema, TxOp, TxValue,
    Unique, Value, ValueType,
};
use atomic_core::{
    PostgresConnectionConfig, SemanticError, ServiceTransactionReport, TransactionRequest,
};
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

const EMPTY_ID: u32 = 1_000;
const HISTORICAL_ID: u32 = 1_001;
const PENDING_ID: u32 = 1_002;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, name) in [
        (EMPTY_ID, "empty-id"),
        (HISTORICAL_ID, "historical-id"),
        (PENDING_ID, "pending-id"),
    ] {
        schema
            .install(Attribute::new(
                id,
                Keyword::new("item", name),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
    }
    schema
}

fn unique_attribute(database: &Database, attribute: u32) -> Attribute {
    let mut descriptor = database.schema().attribute(attribute).unwrap().clone();
    descriptor.unique = Some(Unique::Identity);
    descriptor
}

#[test]
fn eager_unique_enablement_uses_history_not_only_current_values() {
    let database = Database::new(schema()).unwrap();
    let empty = database
        .with(
            &[TxOp::AlterAttribute(unique_attribute(&database, EMPTY_ID))],
            1_000,
        )
        .unwrap()
        .db_after;
    assert_eq!(
        empty.schema().attribute(EMPTY_ID).unwrap().unique,
        Some(Unique::Identity)
    );

    let asserted = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("past".into()),
                attribute: HISTORICAL_ID,
                value: TxValue::Scalar(Value::String("gone".into())),
            }],
            1_000,
        )
        .unwrap();
    let entity = asserted.tempids["past"];
    let historical = asserted
        .db_after
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: HISTORICAL_ID,
                value: Some(TxValue::Scalar(Value::String("gone".into()))),
            }],
            2_000,
        )
        .unwrap()
        .db_after;
    assert!(historical.values(entity, HISTORICAL_ID).is_empty());
    let error = historical
        .with(
            &[TxOp::AlterAttribute(unique_attribute(
                &historical,
                HISTORICAL_ID,
            ))],
            3_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "schema/unique-requires-avet");
}

fn connection() -> Option<common::PostgresFixture> {
    std::env::var("ATOMIC_POSTGRES_URL")
        .ok()
        .map(|url| common::PostgresFixture::new(&url, "current_semantics"))
}

fn unique_name(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

#[test]
fn postgres_writer_rejects_logical_but_unready_avet_until_publication() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique_name("avet_readiness");
    common::install(&connection).unwrap();
    let mut store = common::TestStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let config = PostgresConnectionConfig::plaintext(&connection);
    let database = BlockDatabase::resolve(&config, &database_id).unwrap();
    let mut objects = atomic_core::storage::PgBlockStore::connect(&config).unwrap();
    let read_root = |objects: &mut atomic_core::storage::PgBlockStore| {
        let id = objects
            .read_ref(&database.reference_key())
            .unwrap()
            .unwrap()
            .value
            .unwrap()
            .try_into()
            .unwrap();
        atomic_core::storage::root::DatabaseRoot::decode(&id, &objects.get(id).unwrap().unwrap())
            .unwrap()
    };
    let fresh = read_root(&mut objects);
    assert_eq!(fresh.writer_epoch, 0);
    common::consolidate(&connection, &database_id).unwrap();
    let route = atomic_core::DatabaseCatalog::connect(&connection)
        .unwrap()
        .resolve(&database_id)
        .unwrap()
        .database_id;
    atomic_core::PostgresOperator::connect(&connection)
        .unwrap()
        .rebuild_fulltext(&route, None)
        .unwrap();
    let maintained = read_root(&mut objects);
    assert_eq!(
        maintained.writer_epoch, 0,
        "operator maintenance must not acquire writer authority"
    );
    assert_eq!(
        (
            maintained.basis,
            maintained.log,
            maintained.receipts,
            maintained.metadata
        ),
        (fresh.basis, fresh.log, fresh.receipts, fresh.metadata)
    );
    // A direct writer deliberately has no background index job; publication is
    // explicit below, so logical readiness cannot race physical backfill.
    let claim = || {
        BlockTransactor::claim(
            &config,
            BlockDatabase::resolve(&config, &database_id).unwrap(),
            BlockWriterOptions::default(),
        )
        .unwrap()
    };
    let mut service = claim();
    let empty_unique = {
        let mut descriptor = created.schema().attribute(EMPTY_ID).unwrap().clone();
        descriptor.unique = Some(Unique::Identity);
        descriptor
    };
    let empty_enabled = transact(
        &mut service,
        "unique-empty",
        created.basis_t(),
        &[TxOp::AlterAttribute(empty_unique)],
        1_000,
    )
    .unwrap();
    let empty_avet = Peer::connect(&connection, &database_id, 32)
        .unwrap()
        .snapshot()
        .datoms_with_prefix(
            true,
            &IndexPrefix::Avet {
                attribute: EMPTY_ID,
                value: None,
                entity: None,
            },
        )
        .unwrap();
    assert!(empty_avet.datoms.is_empty());
    service.release().unwrap();
    common::consolidate(&connection, &database_id).unwrap();

    // A retracted value still counts: recovered has-values? probes history
    // AEVT, not the current relation.
    let mut service = claim();
    let populated = transact(
        &mut service,
        "seed-unindexed",
        empty_enabled.basis_t,
        &[
            TxOp::Add {
                entity: EntityRef::Temp("historical".into()),
                attribute: HISTORICAL_ID,
                value: TxValue::Scalar(Value::String("gone".into())),
            },
            TxOp::Add {
                entity: EntityRef::Temp("pending".into()),
                attribute: PENDING_ID,
                value: TxValue::Scalar(Value::String("kept".into())),
            },
        ],
        2_000,
    )
    .unwrap();
    let historical_entity = populated.tempids["historical"];
    let retracted = transact(
        &mut service,
        "retract-unindexed",
        populated.basis_t,
        &[TxOp::Retract {
            entity: EntityRef::Id(historical_entity),
            attribute: HISTORICAL_ID,
            value: Some(TxValue::Scalar(Value::String("gone".into()))),
        }],
        3_000,
    )
    .unwrap();
    let mut historical_unique = retracted
        .db_after
        .schema()
        .attribute(HISTORICAL_ID)
        .unwrap()
        .clone();
    historical_unique.unique = Some(Unique::Identity);
    let history_error = transact(
        &mut service,
        "unique-historical-unindexed",
        retracted.basis_t,
        &[TxOp::AlterAttribute(historical_unique)],
        4_000,
    )
    .unwrap_err();
    assert_eq!(history_error.code, "schema/unique-requires-avet");
    service.release().unwrap();
    common::consolidate(&connection, &database_id).unwrap();

    let mut service = claim();
    let mut indexed = retracted
        .db_after
        .schema()
        .attribute(PENDING_ID)
        .unwrap()
        .clone();
    indexed.indexed = true;
    let index_enabled = transact(
        &mut service,
        "enable-pending-index",
        retracted.basis_t,
        &[TxOp::AlterAttribute(indexed)],
        5_000,
    )
    .unwrap();

    let peer = Peer::connect(&connection, &database_id, 32).unwrap();
    let avet_error = peer
        .snapshot()
        .datoms_with_prefix(
            true,
            &IndexPrefix::Avet {
                attribute: PENDING_ID,
                value: None,
                entity: None,
            },
        )
        .unwrap_err();
    assert_eq!(avet_error.code, "peer/avet-not-ready");

    let mut pending_unique = index_enabled
        .db_after
        .schema()
        .attribute(PENDING_ID)
        .unwrap()
        .clone();
    pending_unique.unique = Some(Unique::Identity);
    let pending_error = transact(
        &mut service,
        "unique-before-backfill",
        index_enabled.basis_t,
        &[TxOp::AlterAttribute(pending_unique.clone())],
        6_000,
    )
    .unwrap_err();
    assert_eq!(pending_error.code, "schema/unique-requires-avet");

    service.release().unwrap();
    common::consolidate(&connection, &database_id).unwrap();
    let mut service = claim();

    let unique_enabled = transact(
        &mut service,
        "unique-after-backfill",
        index_enabled.basis_t,
        &[TxOp::AlterAttribute(pending_unique)],
        7_000,
    )
    .unwrap();
    assert_eq!(
        unique_enabled
            .db_after
            .schema()
            .attribute(PENDING_ID)
            .unwrap()
            .unique,
        Some(Unique::Identity)
    );
    service.release().unwrap();
}

fn transact(
    writer: &mut BlockTransactor,
    key: &str,
    basis: u64,
    ops: &[TxOp],
    instant: i64,
) -> Result<ServiceTransactionReport, SemanticError> {
    writer.transact(
        &TransactionRequest::new(key, ops.to_vec())
            .comparing_basis(basis)
            .with_tx_instant(instant),
    )
}
