use atomic_core::storage::{BlockDatabase, BlockTransactor, BlockWriterOptions};
use atomic_core::{
    Attribute, AttributeName, Cardinality, DB_IDENT, Database, DatabaseValue, EntityIdentifier,
    EntityRef, IndexBoundary, IndexComponents, IndexOrder, Keyword, Peer, RawIndexValue, Schema,
    TupleSpec, TxOp, Unique, Value, ValueType,
};
use atomic_core::{PostgresConnectionConfig, TransactionRequest};
use std::cmp::Ordering;

mod common;

const PAIR: u32 = 1000;
const REF_PAIR: u32 = 1001;
const HOMOGENEOUS: u32 = 1002;
const LEFT: u32 = 1003;
const RIGHT: u32 = 1004;
const COMPOSITE: u32 = 1005;
const PLAIN: u32 = 1006;
const UNIQUE: u32 = 1007;

fn name(local: &str) -> Keyword {
    Keyword::new("read-index", local)
}

fn schema(pair_indexed: bool) -> Schema {
    let mut schema = Schema::new();
    for (id, local, tuple) in [
        (
            PAIR,
            "pair",
            TupleSpec::Heterogeneous(vec![ValueType::Long, ValueType::String]),
        ),
        (
            REF_PAIR,
            "ref-pair",
            TupleSpec::Heterogeneous(vec![ValueType::Ref, ValueType::Long]),
        ),
        (
            HOMOGENEOUS,
            "homogeneous",
            TupleSpec::Homogeneous(ValueType::Ref),
        ),
    ] {
        let mut attribute =
            Attribute::new(id, name(local), ValueType::Tuple, Cardinality::One).tuple(tuple);
        attribute.indexed = id != PAIR || pair_indexed;
        schema.install(attribute).unwrap();
    }
    for (id, local, value_type) in [
        (LEFT, "left", ValueType::Long),
        (RIGHT, "right", ValueType::String),
        (PLAIN, "plain", ValueType::Long),
    ] {
        schema
            .install(Attribute::new(
                id,
                name(local),
                value_type,
                Cardinality::One,
            ))
            .unwrap();
    }
    let mut composite = Attribute::new(
        COMPOSITE,
        name("composite"),
        ValueType::Tuple,
        Cardinality::One,
    )
    .tuple(TupleSpec::Composite(vec![LEFT, RIGHT]));
    composite.indexed = true;
    schema.install(composite).unwrap();
    schema
        .install(
            Attribute::new(UNIQUE, name("id"), ValueType::String, Cardinality::One)
                .unique(Unique::Identity),
        )
        .unwrap();
    schema
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn seed() -> Vec<TxOp> {
    let mut ops = Vec::new();
    for (row, first, second) in [
        ("nil", None, "nil"),
        ("one_a", Some(1), "a"),
        ("one_z", Some(1), "z"),
        ("two", Some(2), "b"),
        ("three", Some(3), "c"),
    ] {
        ops.push(add(
            EntityRef::Temp(row.into()),
            PAIR,
            Value::Tuple(vec![
                first.map(Value::Long),
                Some(Value::String(second.into())),
            ]),
        ));
    }
    for local in ["alice", "bob"] {
        ops.push(add(
            EntityRef::Temp(local.into()),
            DB_IDENT as u32,
            Value::Keyword(name(local)),
        ));
        ops.push(add(
            EntityRef::Temp(local.into()),
            UNIQUE,
            Value::String(local.into()),
        ));
    }
    for (attribute, value) in [(LEFT, Value::Long(2)), (RIGHT, Value::String("b".into()))] {
        ops.push(add(EntityRef::Temp("composite".into()), attribute, value));
    }
    ops
}

fn fixture() -> (DatabaseValue, DatabaseValue, u64) {
    let first = Database::new(schema(true))
        .unwrap()
        .with(&seed(), 1000)
        .unwrap();
    let alice = first.tempids["alice"];
    let bob = first.tempids["bob"];
    let changed = first
        .db_after
        .with(
            &[
                add(
                    EntityRef::Id(first.tempids["one_a"]),
                    PAIR,
                    Value::Tuple(vec![Some(Value::Long(4)), Some(Value::String("d".into()))]),
                ),
                add(
                    EntityRef::Id(alice),
                    REF_PAIR,
                    Value::Tuple(vec![Some(Value::Ref(bob)), Some(Value::Long(7))]),
                ),
                add(
                    EntityRef::Id(alice),
                    HOMOGENEOUS,
                    Value::Tuple(vec![Some(Value::Ref(bob)), None]),
                ),
            ],
            2000,
        )
        .unwrap();
    (
        first.db_after.database_value(),
        changed.db_after.database_value(),
        alice,
    )
}

/// Independent full-order oracle, not another call to a boundary comparator.
/// Compare every datom's A/V against the literal virtual tuple, retaining
/// adjacent attributes because seeks intentionally continue past the range.
fn assert_avet_seeks(database: &DatabaseValue, attribute: u32, tuple: Vec<Option<Value>>) {
    let raw = RawIndexValue::Stored(Value::Tuple(tuple.clone()));
    let authored = database
        .avet_boundary(IndexComponents::Two(AttributeName::Id(attribute), raw))
        .unwrap();
    let literal = Value::Tuple(tuple);
    assert_eq!(
        authored,
        IndexBoundary::Avet(IndexComponents::Two(attribute, literal.clone()))
    );
    let all = database.datoms(IndexOrder::Avet).unwrap();
    let compare = |datom: &&atomic_core::Datom| {
        datom
            .attribute
            .cmp(&attribute)
            .then_with(|| datom.value.index_cmp(&literal))
    };
    let forward: Vec<_> = all
        .iter()
        .filter(|d| compare(d) != Ordering::Less)
        .cloned()
        .collect();
    let reverse: Vec<_> = all
        .iter()
        .rev()
        .filter(|d| compare(d) != Ordering::Greater)
        .cloned()
        .collect();
    assert_eq!(database.collect_seek_datoms(&authored).unwrap(), forward);
    assert_eq!(
        database.collect_reverse_seek_datoms(&authored).unwrap(),
        reverse
    );
}

#[test]
fn virtual_tuple_seeks_keep_exact_current_temporal_and_filtered_order() {
    let (old, current, _) = fixture();
    let views = [
        old.clone(),
        current.clone(),
        current.clone().as_of(old.basis_t()),
        current.clone().history(),
        current.clone().since(old.basis_t()),
        current.filter(|_, datom| datom.attribute != REF_PAIR),
    ];
    for database in views {
        for prefix in [
            vec![],
            vec![None],
            vec![Some(Value::Long(1))],
            vec![Some(Value::Long(1)), None],
            vec![Some(Value::Long(1)), Some(Value::String("a".into()))],
            vec![Some(Value::Long(2))],
            vec![Some(Value::Long(99))],
        ] {
            assert_avet_seeks(&database, PAIR, prefix.clone());
            assert_avet_seeks(&database, COMPOSITE, prefix);
        }
    }
}

#[test]
fn tuple_authoring_normalizes_ref_prefixes_and_all_schema_kinds() {
    let (_, database, alice) = fixture();
    let bob = database.entid(&name("bob")).unwrap();
    for attribute in [REF_PAIR, HOMOGENEOUS] {
        for reference in [
            EntityIdentifier::Id(bob),
            EntityIdentifier::Ident(name("bob")),
            EntityIdentifier::Lookup {
                attribute: AttributeName::Ident(name("id")),
                value: Value::String("bob".into()),
            },
        ] {
            let raw = RawIndexValue::Tuple(vec![Some(RawIndexValue::Entity(reference))]);
            let literal = Value::Tuple(vec![Some(Value::Ref(bob))]);
            assert_eq!(
                database
                    .avet_boundary(IndexComponents::Two(
                        AttributeName::Id(attribute),
                        raw.clone()
                    ))
                    .unwrap(),
                IndexBoundary::Avet(IndexComponents::Two(attribute, literal.clone()))
            );
            assert_eq!(
                database
                    .eavt_boundary(IndexComponents::Three(
                        EntityIdentifier::Id(alice),
                        AttributeName::Id(attribute),
                        raw.clone()
                    ))
                    .unwrap(),
                IndexBoundary::Eavt(IndexComponents::Three(alice, attribute, literal.clone()))
            );
            assert_eq!(
                database
                    .aevt_boundary(IndexComponents::Three(
                        AttributeName::Id(attribute),
                        EntityIdentifier::Id(alice),
                        raw
                    ))
                    .unwrap(),
                IndexBoundary::Aevt(IndexComponents::Three(attribute, alice, literal))
            );
        }
        for prefix in [vec![], vec![None], vec![Some(Value::Ref(bob))]] {
            assert_avet_seeks(&database, attribute, prefix);
        }
        let nil = RawIndexValue::Tuple(vec![None]);
        assert_eq!(
            database
                .avet_boundary(IndexComponents::Two(AttributeName::Id(attribute), nil))
                .unwrap(),
            IndexBoundary::Avet(IndexComponents::Two(attribute, Value::Tuple(vec![None])))
        );
    }
    assert_eq!(
        database
            .avet_boundary(IndexComponents::Two(
                AttributeName::Id(COMPOSITE),
                RawIndexValue::Tuple(vec![Some(Value::Long(2).into())])
            ))
            .unwrap(),
        IndexBoundary::Avet(IndexComponents::Two(
            COMPOSITE,
            Value::Tuple(vec![Some(Value::Long(2))])
        ))
    );
}

#[test]
fn virtual_bounds_do_not_relax_stored_tuple_shape_or_slot_validation() {
    let (_, database, _) = fixture();
    for (attribute, prefix, code) in [
        (PAIR, vec![None; 3], "transaction/invalid-tuple-length"),
        (COMPOSITE, vec![None; 3], "transaction/invalid-tuple-length"),
        (
            HOMOGENEOUS,
            vec![None; 9],
            "transaction/invalid-tuple-length",
        ),
        (
            PAIR,
            vec![Some(Value::String("wrong".into()))],
            "transaction/invalid-tuple-element",
        ),
        (
            REF_PAIR,
            vec![Some(Value::Long(1))],
            "transaction/invalid-tuple-element",
        ),
        (
            PAIR,
            vec![None, Some(Value::String("x".repeat(257)))],
            "transaction/tuple-string-too-large",
        ),
    ] {
        for raw in [
            RawIndexValue::Stored(Value::Tuple(prefix.clone())),
            RawIndexValue::Tuple(
                prefix
                    .into_iter()
                    .map(|v| v.map(RawIndexValue::Stored))
                    .collect(),
            ),
        ] {
            assert_eq!(
                database
                    .avet_boundary(IndexComponents::Two(AttributeName::Id(attribute), raw))
                    .unwrap_err()
                    .code,
                code
            );
        }
    }
    assert_eq!(
        database
            .avet_boundary(IndexComponents::Two(
                AttributeName::Id(REF_PAIR),
                RawIndexValue::Tuple(vec![Some(RawIndexValue::Entity(EntityIdentifier::Ident(
                    name("missing")
                )))])
            ))
            .unwrap_err()
            .code,
        "index/unresolved-entity"
    );
    for raw in [
        RawIndexValue::Stored(Value::Tuple(vec![Some(Value::Ref(u64::MAX))])),
        RawIndexValue::Tuple(vec![Some(RawIndexValue::Entity(EntityIdentifier::Id(
            u64::MAX,
        )))]),
        RawIndexValue::Tuple(vec![Some(RawIndexValue::Tuple(vec![]))]),
    ] {
        assert!(
            database
                .avet_boundary(IndexComponents::Two(AttributeName::Id(REF_PAIR), raw))
                .is_err()
        );
    }
    // The same short tuple that is a valid seek position remains an invalid
    // stored value. Keep the underlying validator and transaction path intact.
    let eager = Database::new(schema(true)).unwrap();
    for attribute in [PAIR, HOMOGENEOUS, COMPOSITE] {
        for tuple in [vec![], vec![None]] {
            let value = Value::Tuple(tuple);
            assert_eq!(
                eager
                    .schema()
                    .validate_value(eager.schema().attribute(attribute).unwrap(), &value)
                    .unwrap_err()
                    .code,
                "transaction/invalid-tuple-length"
            );
            if attribute != COMPOSITE {
                assert_eq!(
                    eager
                        .with(
                            &[add(EntityRef::Temp("invalid".into()), attribute, value)],
                            1000
                        )
                        .unwrap_err()
                        .code,
                    "transaction/invalid-tuple-length"
                );
            }
        }
    }
}

#[test]
fn has_avet_resolves_exact_attribute_identity_and_distinguishes_configuration() {
    let (_, database, _) = fixture();
    for attribute in [
        AttributeName::Id(PAIR),
        AttributeName::Ident(name("pair")),
        AttributeName::Id(UNIQUE),
    ] {
        assert!(database.has_avet(&attribute).unwrap());
    }
    assert!(!database.schema().attribute(UNIQUE).unwrap().indexed);
    assert!(!database.has_avet(&AttributeName::Id(PLAIN)).unwrap());
    for attribute in [
        AttributeName::Id(u32::MAX),
        AttributeName::Ident(name("missing")),
        AttributeName::Ident(name("alice")),
    ] {
        let error = database.has_avet(&attribute).unwrap_err();
        assert_eq!(error.code, "database/unknown-attribute");
    }
    let unindexed = Database::new(schema(false))
        .unwrap()
        .with(&seed(), 1000)
        .unwrap()
        .db_after
        .database_value();
    let mut enabled = unindexed.schema().attribute(PAIR).unwrap().clone();
    enabled.indexed = true;
    let speculation = unindexed
        .with(&[TxOp::AlterAttribute(enabled)], 2000)
        .unwrap();
    assert!(!unindexed.has_avet(&AttributeName::Id(PAIR)).unwrap());
    assert!(
        speculation
            .db_after
            .has_avet(&AttributeName::Id(PAIR))
            .unwrap()
    );
    assert_avet_seeks(&speculation.db_after, PAIR, vec![Some(Value::Long(2))]);
}

#[test]
fn postgres_retained_readiness_and_tuple_bounds_survive_physical_backfill() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("read_index_authoring PostgreSQL case requires ATOMIC_POSTGRES_URL");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "read_index_authoring");
    let connection = &fixture.connection;
    let database_id = "read_index_authoring";
    common::install(connection).unwrap();
    let mut store = common::TestStore::connect(connection).unwrap();
    let created = store.create_database(database_id, schema(false)).unwrap();
    let service = common::start_service(connection, database_id);
    let seeded = common::transact(&service, "seed", created.basis_t(), &seed(), 1000);
    service.shutdown();
    common::consolidate(connection, database_id).unwrap();
    let before = Peer::connect(connection, database_id, 16)
        .unwrap()
        .database_value();
    assert!(!before.has_avet(&AttributeName::Id(PAIR)).unwrap());

    let config = PostgresConnectionConfig::plaintext(connection);
    let mut writer = BlockTransactor::claim(
        &config,
        BlockDatabase::resolve(&config, database_id).unwrap(),
        BlockWriterOptions::default(),
    )
    .unwrap();
    let mut attribute = seeded.db_after.schema().attribute(PAIR).unwrap().clone();
    attribute.indexed = true;
    let enabled = writer
        .transact(
            &TransactionRequest::new("enable", vec![TxOp::AlterAttribute(attribute)])
                .comparing_basis(seeded.basis_t)
                .with_tx_instant(2000),
        )
        .unwrap();
    let pending = Peer::connect(connection, database_id, 16)
        .unwrap()
        .database_value();
    assert_eq!(pending.basis_t(), enabled.basis_t);
    assert!(pending.schema().attribute(PAIR).unwrap().indexed);
    assert!(
        !pending
            .has_avet(&AttributeName::Ident(name("pair")))
            .unwrap()
    );
    assert!(
        !pending
            .clone()
            .as_of(before.basis_t())
            .has_avet(&AttributeName::Id(PAIR))
            .unwrap()
    );
    assert_eq!(
        pending
            .avet_boundary(IndexComponents::Two(
                AttributeName::Id(PAIR),
                RawIndexValue::Tuple(vec![Some(Value::Long(1).into())])
            ))
            .unwrap_err()
            .code,
        "index/avet-not-ready"
    );
    // EAVT is independent of AVET readiness, including virtual tuple starts.
    pending
        .eavt_boundary(IndexComponents::Three(
            EntityIdentifier::Id(seeded.tempids["two"]),
            AttributeName::Id(PAIR),
            RawIndexValue::Tuple(vec![Some(Value::Long(2).into())]),
        ))
        .unwrap();

    writer.release().unwrap();
    common::consolidate(connection, database_id).unwrap();
    let service = common::start_service(connection, database_id);
    let ready = Peer::connect(connection, database_id, 16)
        .unwrap()
        .database_value();
    assert!(ready.has_avet(&AttributeName::Id(PAIR)).unwrap());
    assert_eq!(ready.basis_t(), pending.basis_t());
    assert!(!pending.has_avet(&AttributeName::Id(PAIR)).unwrap());
    assert!(!before.has_avet(&AttributeName::Id(PAIR)).unwrap());
    let replaced = common::transact(
        &service,
        "replace-tuple",
        enabled.basis_t,
        &[add(
            EntityRef::Id(seeded.tempids["one_a"]),
            PAIR,
            Value::Tuple(vec![Some(Value::Long(4)), Some(Value::String("d".into()))]),
        )],
        3000,
    );
    let latest = Peer::connect(connection, database_id, 16)
        .unwrap()
        .database_value();
    for database in [
        ready.clone(),
        latest.clone(),
        latest.clone().as_of(seeded.basis_t),
        latest.clone().history(),
        latest.clone().since(enabled.basis_t),
    ] {
        assert!(database.has_avet(&AttributeName::Id(PAIR)).unwrap());
        for prefix in [
            vec![],
            vec![None],
            vec![Some(Value::Long(1))],
            vec![Some(Value::Long(2))],
            vec![Some(Value::Long(99))],
        ] {
            assert_avet_seeks(&database, PAIR, prefix);
        }
    }
    let invalid = common::try_transact(
        &service,
        "invalid-short-tuple",
        replaced.basis_t,
        &[add(
            EntityRef::Temp("bad".into()),
            PAIR,
            Value::Tuple(vec![Some(Value::Long(2))]),
        )],
        4000,
    )
    .unwrap_err();
    assert_eq!(invalid.code, "transaction/invalid-tuple-length");
    service.shutdown();
}
