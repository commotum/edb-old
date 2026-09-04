use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, IndexPrefix, Keyword, Peer, PostgresIndexer,
    PostgresMigrator, PostgresStore, Schema, TxOp, TxValue, Unique, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

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

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
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
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique_name("avet_readiness");
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let mut blocker = Client::connect(&connection, NoTls).unwrap();
    let build_key: i64 = blocker
        .query_one(
            "SELECT atomic_tree_database_build_pin_key($1)",
            &[&database_id],
        )
        .unwrap()
        .get(0);

    // Empty attributes have no physical range to backfill, so uniqueness can
    // become available in the schema transaction itself.
    blocker
        .query_one("SELECT pg_advisory_lock($1)", &[&build_key])
        .unwrap();
    let service = common::start_service(&connection, &database_id);
    let empty_unique = {
        let mut descriptor = created.schema().attribute(EMPTY_ID).unwrap().clone();
        descriptor.unique = Some(Unique::Identity);
        descriptor
    };
    let empty_enabled = common::transact(
        &service,
        "unique-empty",
        created.basis_t(),
        &[TxOp::AlterAttribute(empty_unique)],
        1_000,
    );
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
    let released: bool = blocker
        .query_one("SELECT pg_advisory_unlock($1)", &[&build_key])
        .unwrap()
        .get(0);
    assert!(released);
    service.shutdown();
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();

    // A retracted value still counts: recovered has-values? probes history
    // AEVT, not the current relation.
    let service = common::start_service(&connection, &database_id);
    let populated = common::transact(
        &service,
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
    );
    let historical_entity = populated.tempids["historical"];
    let retracted = common::transact(
        &service,
        "retract-unindexed",
        populated.basis_t,
        &[TxOp::Retract {
            entity: EntityRef::Id(historical_entity),
            attribute: HISTORICAL_ID,
            value: Some(TxValue::Scalar(Value::String("gone".into()))),
        }],
        3_000,
    );
    let mut historical_unique = retracted
        .db_after
        .schema()
        .attribute(HISTORICAL_ID)
        .unwrap()
        .clone();
    historical_unique.unique = Some(Unique::Identity);
    let history_error = common::try_transact(
        &service,
        "unique-historical-unindexed",
        retracted.basis_t,
        &[TxOp::AlterAttribute(historical_unique)],
        4_000,
    )
    .unwrap_err();
    assert_eq!(history_error.code, "schema/unique-requires-avet");
    service.shutdown();
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();

    // Hold the database build fence so the service's schema-triggered index
    // job cannot publish between the two writer requests.
    blocker
        .query_one("SELECT pg_advisory_lock($1)", &[&build_key])
        .unwrap();

    let service = common::start_service(&connection, &database_id);
    let mut indexed = retracted
        .db_after
        .schema()
        .attribute(PENDING_ID)
        .unwrap()
        .clone();
    indexed.indexed = true;
    let index_enabled = common::transact(
        &service,
        "enable-pending-index",
        retracted.basis_t,
        &[TxOp::AlterAttribute(indexed)],
        5_000,
    );

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
    let pending_error = common::try_transact(
        &service,
        "unique-before-backfill",
        index_enabled.basis_t,
        &[TxOp::AlterAttribute(pending_unique.clone())],
        6_000,
    )
    .unwrap_err();
    assert_eq!(pending_error.code, "schema/unique-requires-avet");

    let released: bool = blocker
        .query_one("SELECT pg_advisory_unlock($1)", &[&build_key])
        .unwrap()
        .get(0);
    assert!(released);
    let deadline = Instant::now() + Duration::from_secs(10);
    while service.background_indexing_stats().published_basis_t < index_enabled.basis_t {
        assert!(
            Instant::now() < deadline,
            "schema-triggered AVET publication did not complete"
        );
        std::thread::sleep(Duration::from_millis(10));
    }

    let unique_enabled = common::transact(
        &service,
        "unique-after-backfill",
        index_enabled.basis_t,
        &[TxOp::AlterAttribute(pending_unique)],
        7_000,
    );
    assert_eq!(
        unique_enabled
            .db_after
            .schema()
            .attribute(PENDING_ID)
            .unwrap()
            .unique,
        Some(Unique::Identity)
    );
    service.shutdown();
}
