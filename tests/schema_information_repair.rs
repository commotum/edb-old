use atomic_core::{
    Attribute, Cardinality, DB_ALTER_ATTRIBUTE, DB_CARDINALITY, DB_CARDINALITY_ONE, DB_IDENT,
    DB_INDEX, DB_TX_INSTANT, DB_TYPE_LONG, DB_UNIQUE, DB_UNIQUE_IDENTITY, DB_VALUE_TYPE, Database,
    EntityRef, IndexOrder, Keyword, Schema, TupleSpec, TxOp, TxValue, USER_PARTITION, Unique,
    Value, ValueType, View, canonical_genesis_datoms, make_eid,
};

const DB_TYPE_INSTANT: u64 = 25;

#[test]
fn bootstrap_schema_is_ordinary_queryable_information() {
    let database = Database::bootstrap().unwrap();
    let datoms = database.datoms(View::Current, IndexOrder::Eavt);

    assert_eq!(datoms, canonical_genesis_datoms());
    assert_eq!(database.basis_t(), 0);

    assert!(datoms.iter().any(|datom| {
        datom.entity == DB_IDENT
            && datom.attribute == DB_IDENT as u32
            && datom.value == Value::Keyword(Keyword::new("db", "ident"))
            && datom.added
    }));
    assert!(datoms.iter().any(|datom| {
        datom.entity == DB_TX_INSTANT
            && datom.attribute == DB_VALUE_TYPE as u32
            && datom.value == Value::Ref(DB_TYPE_INSTANT)
            && datom.added
    }));
}

#[test]
fn application_schema_is_a_positive_transaction_not_genesis() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("item", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let database = Database::new(schema).unwrap();

    assert_eq!(database.basis_t(), 1);
    assert!(
        database
            .datoms(View::AsOf(0), IndexOrder::Eavt)
            .iter()
            .all(|datom| datom.entity != 1_000)
    );
    assert!(
        database
            .datoms(View::History, IndexOrder::Eavt)
            .iter()
            .any(|datom| datom.entity == 1_000
                && datom.attribute == DB_IDENT as u32
                && datom.added)
    );
}

#[test]
fn typed_schema_and_ident_caches_rebuild_from_information_alone() {
    let mut input = Schema::new();
    input
        .install(Attribute::new(
            1_000,
            Keyword::new("item", "kind"),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();
    let database = Database::new(input).unwrap();
    let rebuilt = database.rebuild_derived_caches().unwrap();

    assert_eq!(rebuilt.entid(&Keyword::new("db", "ident")), Some(10));
    assert_eq!(rebuilt.entid(&Keyword::new("item", "kind")), Some(1_000));
    assert_eq!(
        rebuilt.datoms(View::Current, IndexOrder::Eavt),
        database.datoms(View::Current, IndexOrder::Eavt)
    );
    assert_eq!(
        rebuilt.schema().attribute(1_000).unwrap(),
        database.schema().attribute(1_000).unwrap()
    );
}

#[test]
fn general_ident_rename_alias_and_repurpose_follow_assertion_history() {
    let mut input = Schema::new();
    input
        .install(Attribute::new(
            1_000,
            Keyword::new("item", "kind"),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();
    let database = Database::new(input).unwrap();
    let old = Keyword::new("kind", "old");
    let new = Keyword::new("kind", "new");

    let created = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("enum".into()),
                attribute: DB_IDENT as u32,
                value: TxValue::Scalar(Value::Keyword(old.clone())),
            }],
            1_000,
        )
        .unwrap();
    let enum_id = created.tempids["enum"];

    let holder = make_eid(USER_PARTITION, 500).unwrap();
    let renamed = created
        .db_after
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(enum_id),
                    attribute: DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(new.clone())),
                },
                TxOp::Add {
                    entity: EntityRef::Id(holder),
                    attribute: 1_000,
                    value: TxValue::Entity(EntityRef::Ident(old.clone())),
                },
            ],
            2_000,
        )
        .unwrap();
    assert_eq!(renamed.db_after.entid(&old), Some(enum_id));
    assert_eq!(renamed.db_after.entid(&new), Some(enum_id));
    assert_eq!(renamed.db_after.ident(enum_id), Some(&new));
    assert_eq!(
        renamed.db_after.values(holder, 1_000),
        vec![&Value::Ref(enum_id)]
    );

    let repurposed = renamed
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("replacement".into()),
                attribute: DB_IDENT as u32,
                value: TxValue::Scalar(Value::Keyword(old.clone())),
            }],
            3_000,
        )
        .unwrap();
    let replacement = repurposed.tempids["replacement"];
    assert_ne!(replacement, enum_id);
    assert_eq!(repurposed.db_after.entid(&old), Some(replacement));
    assert_eq!(repurposed.db_after.entid(&new), Some(enum_id));
    assert_eq!(
        repurposed
            .db_after
            .rebuild_derived_caches()
            .unwrap()
            .entid(&old),
        Some(replacement)
    );
}

fn ref_value(entity: u64) -> TxValue {
    TxValue::Entity(EntityRef::Id(entity))
}

#[test]
fn ordinary_schema_datoms_use_hooks_and_validate_the_complete_successor() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("item", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    let database = Database::new(schema).unwrap();
    let entity = make_eid(USER_PARTITION, 42).unwrap();
    let seeded = database
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: Value::String("a".into()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: Value::String("b".into()).into(),
                },
            ],
            1_000,
        )
        .unwrap()
        .db_after;

    // The data repair and schema narrowing are judged as one proposed value.
    let narrowed = seeded
        .with(
            &[
                TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: Some(Value::String("b".into()).into()),
                },
                TxOp::Add {
                    entity: EntityRef::Id(1_000),
                    attribute: DB_CARDINALITY as u32,
                    value: ref_value(DB_CARDINALITY_ONE),
                },
            ],
            2_000,
        )
        .unwrap();
    assert_eq!(
        narrowed
            .db_after
            .schema()
            .attribute(1_000)
            .unwrap()
            .cardinality,
        Cardinality::One
    );
    assert!(narrowed.tx_data.iter().any(|datom| {
        datom.entity == 0
            && datom.attribute == DB_ALTER_ATTRIBUTE as u32
            && datom.value == Value::Ref(1_000)
            && datom.added
    }));

    // A raw immutable change is rejected by the same transition validator as
    // typed schema sugar.
    let error = narrowed
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(1_000),
                attribute: DB_VALUE_TYPE as u32,
                value: ref_value(DB_TYPE_LONG),
            }],
            3_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "schema/value-type-immutable");
}

#[test]
fn explicit_index_fact_is_distinct_from_unique_derived_avet_membership() {
    let unique_only = Attribute::new(
        1_000,
        Keyword::new("item", "unique-only"),
        ValueType::String,
        Cardinality::One,
    )
    .unique(Unique::Identity);
    let mut explicit = Attribute::new(
        1_001,
        Keyword::new("item", "explicit"),
        ValueType::String,
        Cardinality::One,
    )
    .unique(Unique::Identity);
    explicit.indexed = true;
    let mut schema = Schema::new();
    schema.install(unique_only).unwrap();
    schema.install(explicit).unwrap();
    let database = Database::new(schema).unwrap();

    let altered = database
        .with(
            &[
                TxOp::Retract {
                    entity: EntityRef::Id(1_000),
                    attribute: DB_UNIQUE as u32,
                    value: Some(ref_value(DB_UNIQUE_IDENTITY)),
                },
                TxOp::Retract {
                    entity: EntityRef::Id(1_001),
                    attribute: DB_UNIQUE as u32,
                    value: Some(ref_value(DB_UNIQUE_IDENTITY)),
                },
            ],
            1_000,
        )
        .unwrap()
        .db_after;
    assert!(!altered.schema().attribute(1_000).unwrap().indexed);
    assert!(altered.schema().attribute(1_001).unwrap().indexed);
    assert_eq!(
        altered.values(1_001, DB_INDEX as u32),
        vec![&Value::Bool(true)]
    );
}

#[test]
fn active_composite_constituent_ident_cannot_be_retargeted_until_discontinued() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("part", "a"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            1_001,
            Keyword::new("part", "b"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                1_002,
                Keyword::new("item", "pair"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Composite(vec![1_000, 1_001])),
        )
        .unwrap();
    let database = Database::new(schema).unwrap();
    let retarget = || {
        vec![
            TxOp::Add {
                entity: EntityRef::Id(1_000),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("part", "renamed-a")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(1_001),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("part", "a")).into(),
            },
        ]
    };
    assert_eq!(
        database.with(&retarget(), 1_000).unwrap_err().code,
        "schema/tuple-definition-immutable"
    );

    let mut composite = database.schema().attribute(1_002).unwrap().clone();
    composite.tuple_discontinued = true;
    let discontinued = database
        .with(&[TxOp::AlterAttribute(composite)], 1_000)
        .unwrap()
        .db_after;
    let retargeted = discontinued.with(&retarget(), 2_000).unwrap().db_after;
    assert_eq!(retargeted.entid(&Keyword::new("part", "a")), Some(1_001));
    assert_eq!(
        retargeted.entid(&Keyword::new("part", "renamed-a")),
        Some(1_000)
    );
    assert!(
        retargeted
            .schema()
            .attribute(1_002)
            .unwrap()
            .tuple_discontinued
    );
}

#[test]
fn native_schema_and_idents_are_immutable() {
    let database = Database::bootstrap().unwrap();
    let error = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(DB_TX_INSTANT),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("user", "clock")).into(),
            }],
            1_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "schema/native-information-immutable");

    let error = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(DB_TX_INSTANT),
                attribute: DB_INDEX as u32,
                value: Value::Bool(false).into(),
            }],
            1_000,
        )
        .unwrap_err();
    assert!(matches!(
        error.code,
        "schema/native-information-immutable" | "schema/native-attribute-immutable"
    ));
}
