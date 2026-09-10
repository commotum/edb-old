//! SC-01/SC-02: explicit native contracts, not inferred JVM behavior.
//! Both assessors execute the same independently expected user workflows.
mod common;

use atomic_core::{
    Attribute, Cardinality, Connection, Database, DatabaseValue, Datom, EntityRef, IndexPrefix,
    Keyword, PostgresIndexer, PostgresMigrator, PostgresStore, Schema, SemanticError, TupleSpec,
    TxOp, Unique, Value, ValueType,
};
use std::collections::BTreeMap;
use std::time::Instant;

const YEAR: u32 = 1000;
const SEASON: u32 = 1001;
const IDENTITY: u32 = 1002;
const LABEL: u32 = 1003;
const FLOAT: u32 = 1004;
const DOUBLE: u32 = 1005;
const FLOAT_ID: u32 = 1006;
const FLOAT_UNIQUE: u32 = 1007;
const DOUBLE_ID: u32 = 1008;
const DOUBLE_UNIQUE: u32 = 1009;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, name, kind) in [
        (YEAR, "year", ValueType::Long),
        (SEASON, "season", ValueType::String),
        (LABEL, "label", ValueType::String),
        (FLOAT, "float", ValueType::Float),
        (DOUBLE, "double", ValueType::Double),
    ] {
        schema
            .install(Attribute::new(
                id,
                Keyword::new("item", name),
                kind,
                Cardinality::One,
            ))
            .unwrap();
    }
    schema
        .install(
            Attribute::new(
                IDENTITY,
                Keyword::new("item", "year-season"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Composite(vec![YEAR, SEASON]))
            .unique(Unique::Identity),
        )
        .unwrap();
    for (id, name, kind, unique) in [
        (FLOAT_ID, "float-id", ValueType::Float, Unique::Identity),
        (
            FLOAT_UNIQUE,
            "float-unique",
            ValueType::Float,
            Unique::Value,
        ),
        (DOUBLE_ID, "double-id", ValueType::Double, Unique::Identity),
        (
            DOUBLE_UNIQUE,
            "double-unique",
            ValueType::Double,
            Unique::Value,
        ),
    ] {
        schema
            .install(
                Attribute::new(id, Keyword::new("item", name), kind, Cardinality::One)
                    .unique(unique),
            )
            .unwrap();
    }
    schema
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn temp(name: &str) -> EntityRef {
    EntityRef::Temp(name.into())
}

fn composite(year: i64) -> Value {
    Value::Tuple(vec![
        Some(Value::Long(year)),
        Some(Value::String("fall".into())),
    ])
}

struct Report {
    database: DatabaseValue,
    datoms: Vec<Datom>,
    tempids: BTreeMap<String, u64>,
}

fn history(database: &DatabaseValue, entity: u64, attribute: u32) -> Vec<Datom> {
    database
        .clone()
        .history()
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity,
            attribute: Some(attribute),
            value: None,
        })
        .unwrap()
}

fn composite_contract(
    transact: &mut impl FnMut(&[TxOp]) -> Result<Report, SemanticError>,
) -> DatabaseValue {
    let seed = transact(&[
        add(temp("semester"), YEAR, Value::Long(2026)),
        add(temp("semester"), SEASON, Value::String("fall".into())),
    ])
    .unwrap();
    let entity = seed.tempids["semester"];
    assert_eq!(
        seed.database.values(entity, IDENTITY).unwrap(),
        vec![composite(2026)]
    );
    assert_eq!(
        seed.database.lookup(IDENTITY, &composite(2026)).unwrap(),
        Some(entity)
    );

    // Derived uniqueness is checked after tempids are resolved. It does not
    // retroactively turn a constituent-only fresh entity into an upsert.
    let error = transact(&[
        add(temp("duplicate"), YEAR, Value::Long(2026)),
        add(temp("duplicate"), SEASON, Value::String("fall".into())),
    ])
    .err()
    .expect("constituent-only duplicate must fail atomically");
    assert_eq!(error.code, "transaction/unique-conflict");

    let hint = transact(&[
        add(temp("hint"), IDENTITY, composite(2026)),
        add(temp("hint"), LABEL, Value::String("same semester".into())),
    ])
    .unwrap();
    assert_eq!(hint.tempids["hint"], entity);
    assert_eq!(
        hint.database.values(entity, LABEL).unwrap(),
        vec![Value::String("same semester".into())]
    );
    assert!(!hint.datoms.iter().any(|d| d.attribute == IDENTITY));
    assert!(seed.database.values(entity, LABEL).unwrap().is_empty());

    // Use the old identity to select an existing entity, then let the changed
    // constituent derive its new key. The hint is not a competing direct write.
    let changed = transact(&[
        add(temp("changed"), IDENTITY, composite(2026)),
        add(temp("changed"), YEAR, Value::Long(2027)),
    ])
    .unwrap();
    assert_eq!(changed.tempids["changed"], entity);
    assert_eq!(
        changed.database.values(entity, IDENTITY).unwrap(),
        vec![composite(2027)]
    );
    assert_eq!(
        changed.database.lookup(IDENTITY, &composite(2026)).unwrap(),
        None
    );
    assert_eq!(
        changed.database.lookup(IDENTITY, &composite(2027)).unwrap(),
        Some(entity)
    );
    let tuple_changes: Vec<_> = changed
        .datoms
        .iter()
        .filter(|d| d.attribute == IDENTITY)
        .collect();
    assert_eq!(tuple_changes.len(), 2);
    assert!(
        tuple_changes
            .iter()
            .any(|d| !d.added && d.value == composite(2026))
    );
    assert!(
        tuple_changes
            .iter()
            .any(|d| d.added && d.value == composite(2027))
    );
    assert_eq!(
        seed.database.values(entity, IDENTITY).unwrap(),
        vec![composite(2026)]
    );

    // A nonexistent key cannot fabricate a derived tuple or its constituents.
    let absent = transact(&[
        add(temp("absent"), IDENTITY, composite(2040)),
        add(
            temp("absent"),
            LABEL,
            Value::String("no constituents".into()),
        ),
    ])
    .unwrap();
    let absent_id = absent.tempids["absent"];
    for attribute in [YEAR, SEASON, IDENTITY] {
        assert!(
            absent
                .database
                .values(absent_id, attribute)
                .unwrap()
                .is_empty()
        );
    }
    assert!(!absent.datoms.iter().any(|d| d.attribute == IDENTITY));
    absent.database
}

fn nan_contract(
    transact: &mut impl FnMut(&[TxOp]) -> Result<Report, SemanticError>,
    attribute: u32,
    unique_attributes: [u32; 2],
    nan: Value,
    other_nan: Value,
    finite: Value,
) -> DatabaseValue {
    let first = transact(&[add(temp("measurement"), attribute, nan.clone())]).unwrap();
    let entity = first.tempids["measurement"];
    let first_history = history(&first.database, entity, attribute);
    assert_eq!(first_history.len(), 1);
    assert!(first_history[0].added && first_history[0].value.is_nan());
    let redundant = transact(&[add(EntityRef::Id(entity), attribute, other_nan.clone())]).unwrap();
    assert!(!redundant.datoms.iter().any(|d| d.attribute == attribute));
    assert_eq!(
        history(&redundant.database, entity, attribute),
        first_history
    );

    // A single cardinality-one assertion retracts NaN and asserts the finite
    // successor in the same transaction; no preparatory transaction is needed.
    let replaced = transact(&[add(EntityRef::Id(entity), attribute, finite.clone())]).unwrap();
    assert_eq!(
        replaced.database.values(entity, attribute).unwrap(),
        vec![finite.clone()]
    );
    let changes: Vec<_> = replaced
        .datoms
        .iter()
        .filter(|d| d.attribute == attribute)
        .collect();
    assert_eq!(changes.len(), 2);
    assert!(changes.iter().any(|d| !d.added && d.value.is_nan()));
    assert!(changes.iter().any(|d| d.added && d.value == finite));
    assert_eq!(changes[0].tx, changes[1].tx);
    assert_eq!(history(&replaced.database, entity, attribute).len(), 3);
    assert!(first.database.values(entity, attribute).unwrap()[0].is_nan());
    assert!(
        replaced
            .database
            .clone()
            .as_of(first.database.basis_t())
            .values(entity, attribute)
            .unwrap()[0]
            .is_nan()
    );

    let finite_redundant =
        transact(&[add(EntityRef::Id(entity), attribute, finite.clone())]).unwrap();
    assert!(
        !finite_redundant
            .datoms
            .iter()
            .any(|d| d.attribute == attribute)
    );
    let restored = transact(&[add(EntityRef::Id(entity), attribute, other_nan.clone())]).unwrap();
    assert!(restored.database.values(entity, attribute).unwrap()[0].is_nan());
    assert_eq!(history(&restored.database, entity, attribute).len(), 5);
    // Retraction uses logical NaN equality, not the caller's payload bits.
    let removed = transact(&[TxOp::Retract {
        entity: EntityRef::Id(entity),
        attribute,
        value: Some(nan.clone().into()),
    }])
    .unwrap();
    assert!(
        removed
            .database
            .values(entity, attribute)
            .unwrap()
            .is_empty()
    );
    assert_eq!(history(&removed.database, entity, attribute).len(), 6);

    let mut latest = removed.database;
    for unique_attribute in unique_attributes {
        let error = transact(&[add(temp("invalid"), unique_attribute, nan.clone())])
            .err()
            .expect("NaN must not identify even a new entity");
        assert_eq!(error.code, "transaction/nan-cannot-identify");
        let valid = transact(&[add(temp("unique"), unique_attribute, finite.clone())]).unwrap();
        let unique_entity = valid.tempids["unique"];
        let error = transact(&[add(
            EntityRef::Id(unique_entity),
            unique_attribute,
            other_nan.clone(),
        )])
        .err()
        .expect("NaN must not replace a unique key");
        assert_eq!(error.code, "transaction/nan-cannot-identify");
        assert_eq!(
            valid
                .database
                .values(unique_entity, unique_attribute)
                .unwrap(),
            vec![finite.clone()]
        );
        assert_eq!(
            valid.database.lookup(unique_attribute, &finite).unwrap(),
            Some(unique_entity)
        );
        latest = valid.database;
    }
    latest
}

fn numeric_contracts(
    transact: &mut impl FnMut(&[TxOp]) -> Result<Report, SemanticError>,
) -> DatabaseValue {
    nan_contract(
        transact,
        FLOAT,
        [FLOAT_ID, FLOAT_UNIQUE],
        Value::Float(f32::from_bits(0x7fc0_0001)),
        Value::Float(f32::from_bits(0xffc0_0042)),
        Value::Float(6.25),
    );
    nan_contract(
        transact,
        DOUBLE,
        [DOUBLE_ID, DOUBLE_UNIQUE],
        Value::Double(f64::from_bits(0x7ff8_0000_0000_0001)),
        Value::Double(f64::from_bits(0xfff8_0000_0000_0042)),
        Value::Double(6.25),
    )
}

fn eager_transactor(reverse: bool) -> impl FnMut(&[TxOp]) -> Result<Report, SemanticError> {
    let mut database = Database::new(schema()).unwrap();
    let mut attempt = 0;
    move |ops| {
        attempt += 1;
        let mut ops = ops.to_vec();
        if reverse {
            ops.reverse();
        }
        let report = database.with(&ops, attempt * 1000)?;
        database = report.db_after;
        Ok(Report {
            database: database.database_value(),
            datoms: report.tx_data,
            tempids: report.tempids,
        })
    }
}

#[test]
fn composite_upsert_is_an_explicit_hint_not_constituent_only_inference() {
    for reverse in [false, true] {
        composite_contract(&mut eager_transactor(reverse));
    }
}

#[test]
fn float_and_double_nan_replacement_history_redundancy_and_unique_rejection() {
    numeric_contracts(&mut eager_transactor(false));
}

#[test]
fn native_postgres_contracts_survive_consolidation_and_recovery() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED schema identity PostgreSQL contract: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "schema_identity_contracts");
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&fixture.connection).unwrap();
    let created = store.create_database("contracts", schema()).unwrap();
    let service = common::start_service(&fixture.connection, "contracts");
    let start = Instant::now();
    let mut basis = created.basis_t();
    let mut attempt = 0;
    let mut transact = |ops: &[TxOp]| {
        attempt += 1;
        let report = common::try_transact(
            &service,
            &format!("contract-{attempt}"),
            basis,
            ops,
            attempt * 1000,
        )?;
        basis = report.basis_t;
        Ok(Report {
            database: report.db_after,
            datoms: report.tx_data,
            tempids: report.tempids,
        })
    };
    let composite = composite_contract(&mut transact);
    let latest = numeric_contracts(&mut transact);
    service.shutdown();
    PostgresIndexer::connect(&fixture.connection, "contracts")
        .unwrap()
        .consolidate()
        .unwrap();
    let reopened = Connection::connect(&fixture.connection, "contracts", 8).unwrap();
    let recovered = store.recover("contracts").unwrap();
    common::assert_same_information(&latest, &reopened.db());
    common::assert_same_information(&latest, &recovered);
    assert!(composite.basis_t() < latest.basis_t());
    eprintln!(
        "schema identity contracts: {attempt} attempted transactions, complete assertions/consolidation/reopen/recovery in {:?}",
        start.elapsed()
    );
}
