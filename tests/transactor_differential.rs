use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, Keyword, PostgresMigrator, PostgresStore, Schema,
    ServiceTransactionReport, TransactionRequest, TransactionService, TransactionServiceConfig,
    TxOp, TxValue, Unique, Value, ValueType,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const PERSON_EMAIL: u32 = 1_000;
const PERSON_NAME: u32 = 1_001;
const PERSON_AGE: u32 = 1_002;
const PERSON_TAG: u32 = 1_003;
const PERSON_FRIEND: u32 = 1_004;

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

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                PERSON_EMAIL,
                Keyword::new("person", "email"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_NAME,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_AGE,
            Keyword::new("person", "age"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_TAG,
            Keyword::new("person", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_FRIEND,
            Keyword::new("person", "friend"),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn config(connection: &str, database_id: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: unique_name("transactor_differential_holder"),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: atomic_core::CapacityLimits::default(),
    }
}

fn scalar(value: Value) -> TxValue {
    TxValue::Scalar(value)
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: scalar(value),
    }
}

/// Apply one request to both semantic engines and compare every observable
/// part of the transaction report and both immutable database values.
fn transact_both(
    service: &TransactionService,
    oracle: &mut Database,
    request_key: &str,
    operations: Vec<TxOp>,
    instant: i64,
) -> ServiceTransactionReport {
    let expected = oracle.with(&operations, instant).unwrap();
    let actual = service
        .client()
        .transact(
            TransactionRequest::new(request_key, operations)
                .comparing_basis(oracle.basis_t())
                .with_tx_instant(instant),
            Duration::from_secs(5),
        )
        .unwrap();

    assert!(!actual.replayed, "a fresh request was reported as a replay");
    assert_eq!(actual.basis_t, expected.db_after.basis_t());
    assert_eq!(actual.tx_data, expected.tx_data);
    assert_eq!(actual.tempids, expected.tempids);
    common::assert_same_information(&actual.db_before, &expected.db_before);
    common::assert_same_information(&actual.db_after, &expected.db_after);

    *oracle = expected.db_after;
    actual
}

fn reject_both(
    service: &TransactionService,
    oracle: &Database,
    request_key: &str,
    operations: Vec<TxOp>,
    instant: i64,
) {
    let expected = oracle.with(&operations, instant).unwrap_err();
    let actual = service
        .client()
        .transact(
            TransactionRequest::new(request_key, operations)
                .comparing_basis(oracle.basis_t())
                .with_tx_instant(instant),
            Duration::from_secs(5),
        )
        .unwrap_err();
    assert_eq!(actual, expected);
}

#[test]
fn production_writer_matches_the_pure_kernel_across_transactions_and_restart() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique_name("transactor_differential");
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let mut oracle = store.create_database(&database_id, schema()).unwrap();
    drop(store);

    let service = TransactionService::start(config(&connection, &database_id)).unwrap();
    let seeded = transact_both(
        &service,
        &mut oracle,
        "seed-people",
        vec![
            add(
                EntityRef::Temp("alice".into()),
                PERSON_EMAIL,
                Value::String("alice@example.test".into()),
            ),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_NAME,
                Value::String("Ada".into()),
            ),
            add(EntityRef::Temp("alice".into()), PERSON_AGE, Value::Long(36)),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_TAG,
                Value::String("logic".into()),
            ),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_TAG,
                Value::String("math".into()),
            ),
            add(
                EntityRef::Temp("bob".into()),
                PERSON_EMAIL,
                Value::String("bob@example.test".into()),
            ),
            add(
                EntityRef::Temp("bob".into()),
                PERSON_NAME,
                Value::String("Bob".into()),
            ),
            TxOp::Add {
                entity: EntityRef::Temp("bob".into()),
                attribute: PERSON_FRIEND,
                value: TxValue::Entity(EntityRef::Temp("alice".into())),
            },
        ],
        10_000,
    );
    let alice = seeded.tempids["alice"];
    let bob = seeded.tempids["bob"];

    let upserted = transact_both(
        &service,
        &mut oracle,
        "upsert-and-replace",
        vec![
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_EMAIL,
                Value::String("alice@example.test".into()),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_NAME,
                Value::String("Ada Lovelace".into()),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_AGE,
                Value::Long(37),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_TAG,
                Value::String("computing".into()),
            ),
            TxOp::Retract {
                entity: EntityRef::Temp("alice-upsert".into()),
                attribute: PERSON_TAG,
                value: Some(scalar(Value::String("math".into()))),
            },
        ],
        10_001,
    );
    assert_eq!(upserted.tempids["alice-upsert"], alice);

    // Force the next assessment to reconstruct its immutable db-before from
    // the durable root and log, not from the first service's resident value.
    service.shutdown();
    let service = TransactionService::start(config(&connection, &database_id)).unwrap();

    reject_both(
        &service,
        &oracle,
        "stale-cas",
        vec![TxOp::Cas {
            entity: EntityRef::Id(alice),
            attribute: PERSON_AGE,
            old: Some(scalar(Value::Long(36))),
            new: scalar(Value::Long(38)),
        }],
        10_002,
    );

    transact_both(
        &service,
        &mut oracle,
        "cas-and-retract",
        vec![
            TxOp::Cas {
                entity: EntityRef::Id(alice),
                attribute: PERSON_AGE,
                old: Some(scalar(Value::Long(37))),
                new: scalar(Value::Long(38)),
            },
            TxOp::Retract {
                entity: EntityRef::Id(alice),
                attribute: PERSON_TAG,
                value: Some(scalar(Value::String("logic".into()))),
            },
            TxOp::Retract {
                entity: EntityRef::Id(bob),
                attribute: PERSON_NAME,
                value: None,
            },
        ],
        10_002,
    );

    let mut age = oracle.schema().attribute(PERSON_AGE).unwrap().clone();
    age.ident = Keyword::new("person", "years");
    age.no_history = true;
    transact_both(
        &service,
        &mut oracle,
        "alter-age-schema",
        vec![TxOp::AlterAttribute(age)],
        10_003,
    );
    transact_both(
        &service,
        &mut oracle,
        "post-alter-and-retract-entity",
        vec![
            add(EntityRef::Id(alice), PERSON_AGE, Value::Long(39)),
            TxOp::RetractEntity(EntityRef::Id(bob)),
        ],
        10_004,
    );
    service.shutdown();

    let mut store = PostgresStore::connect(&connection).unwrap();
    let recovered = store.recover(&database_id).unwrap();
    common::assert_same_information(&recovered, &oracle);
    assert_eq!(
        recovered
            .schema()
            .resolve_ident(&Keyword::new("person", "age")),
        Some(PERSON_AGE)
    );
    assert_eq!(
        recovered
            .schema()
            .resolve_ident(&Keyword::new("person", "years")),
        Some(PERSON_AGE)
    );
}
