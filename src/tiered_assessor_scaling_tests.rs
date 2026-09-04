use super::*;
use crate::{Attribute, Database, Keyword};
use bigdecimal::BigDecimal;
use std::str::FromStr;

fn wide_schema(attribute_count: u32) -> Schema {
    let mut schema = Schema::new();
    for ordinal in 0..attribute_count {
        let id = 1_000 + ordinal;
        schema
            .install(Attribute::new(
                id,
                Keyword::new("wide", format!("a-{ordinal:05}")),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
    }
    schema
}

#[test]
fn localized_schema_alter_reads_only_local_information() {
    const ATTRIBUTE_COUNT: u32 = 512;
    const TARGET: u32 = 1_000 + ATTRIBUTE_COUNT - 1;
    let database = Database::new(wide_schema(ATTRIBUTE_COUNT)).unwrap();
    let mut altered = database.schema().attribute(TARGET).unwrap().clone();
    altered.no_history = true;
    let ops = [TxOp::AlterAttribute(altered)];
    let eager = database.with(&ops, 10).unwrap();

    // Before the resident-schema repair, derive_successor_schema performed an
    // EAVT read for all 512 attributes and exhausted this budget. All reads
    // below are now for the one edited entity and fixed transaction metadata.
    let assessed = assess_tiered_with_limits(
        &DatabaseValue::eager(Arc::new(database)),
        &ops,
        10,
        AssessmentLimits {
            max_read_datoms: 32,
            max_read_bytes: u64::MAX,
        },
    )
    .unwrap();
    assert_eq!(assessed.tx_data, eager.tx_data);
    assert_eq!(assessed.successor_schema.as_ref(), eager.db_after.schema());
    assert!(
        assessed.read_work.datoms <= 16,
        "localized alter read {} datoms",
        assessed.read_work.datoms
    );
}

#[test]
fn localized_projection_preserves_raw_schema_and_tuple_ident_semantics() {
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
                Keyword::new("part", "pair"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Composite(vec![1_000, 1_001])),
        )
        .unwrap();
    let database = Database::new(schema).unwrap();
    let retarget = vec![
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
    ];
    let eager = database.with(&retarget, 10).unwrap_err();
    let tiered =
        assess_tiered(&DatabaseValue::eager(Arc::new(database)), &retarget, 10).unwrap_err();
    assert_eq!(tiered.category, eager.category);
    assert_eq!(tiered.code, eager.code);
    assert_eq!(tiered.code, "schema/tuple-definition-immutable");
}

#[test]
fn identity_grouping_is_n_log_n_and_unions_transitively() {
    const GROUPS: usize = 4_096;
    let mut identities = Vec::with_capacity(GROUPS * 2);
    for group in (0..GROUPS).rev() {
        let attribute = 1_000 + u32::try_from(group % 7).unwrap();
        let key = if group % 2 == 0 {
            UpsertIdentityValue::Resolved(Value::String(format!("key-{group:05}")))
        } else {
            UpsertIdentityValue::TempRef(format!("ref-{group:05}"))
        };
        identities.push((group * 2, attribute, key.clone(), Unique::Identity));
        identities.push((group * 2 + 1, attribute, key, Unique::Identity));
    }
    let mut union = UnionFind::new(GROUPS * 2);
    let comparisons = union_identity_assertions(&mut union, &identities);
    for group in 0..GROUPS {
        assert_eq!(union.root(group * 2), union.root(group * 2 + 1));
        assert_eq!(union.root(group * 2), group * 2);
    }
    assert!(
        comparisons < GROUPS * 2 * 32,
        "identity grouping used {comparisons} comparisons"
    );

    // Equal keys on different unique attributes are distinct groups; a value
    // uniqueness assertion never participates in tempid upsert union.
    let mut edge_union = UnionFind::new(4);
    let edge = vec![
        (
            0,
            1_000,
            UpsertIdentityValue::Resolved(Value::Long(1)),
            Unique::Identity,
        ),
        (
            1,
            1_001,
            UpsertIdentityValue::Resolved(Value::Long(1)),
            Unique::Identity,
        ),
        (
            2,
            1_000,
            UpsertIdentityValue::Resolved(Value::Long(1)),
            Unique::Value,
        ),
        (
            3,
            1_000,
            UpsertIdentityValue::Resolved(Value::Long(1)),
            Unique::Identity,
        ),
    ];
    union_identity_assertions(&mut edge_union, &edge);
    assert_eq!(edge_union.root(0), edge_union.root(3));
    assert_ne!(edge_union.root(0), edge_union.root(1));
    assert_eq!(edge_union.root(2), 2);
}

#[test]
fn sorted_uniqueness_validation_preserves_index_and_storage_semantics() {
    const FACTS: usize = 8_192;
    let facts = (0..FACTS)
        .rev()
        .map(|ordinal| Datom {
            entity: u64::try_from(ordinal + 1).unwrap(),
            attribute: 1_000,
            value: Value::Long(i64::try_from(ordinal).unwrap()),
            tx: 1,
            added: true,
        })
        .collect::<Vec<_>>();
    let comparisons = validate_attribute_uniqueness_with_work(&facts).unwrap();
    assert!(
        comparisons < FACTS * 32,
        "uniqueness validation used {comparisons} comparisons"
    );

    let decimal = |entity, spelling: &str| Datom {
        entity,
        attribute: 1_000,
        value: Value::BigDec(BigDecimal::from_str(spelling).unwrap()),
        tx: 1,
        added: true,
    };
    let conflict =
        validate_attribute_uniqueness(&[decimal(1, "1.0"), decimal(2, "1.00")]).unwrap_err();
    assert_eq!(conflict.code, "schema/unique-change-conflict");

    // The physical representations remain distinct facts, but one owner does
    // not violate uniqueness under the logical index comparator.
    validate_attribute_uniqueness(&[decimal(1, "1.0"), decimal(1, "1.00")]).unwrap();
    let nan = validate_attribute_uniqueness(&[Datom {
        entity: 1,
        attribute: 1_000,
        value: Value::Double(f64::NAN),
        tx: 1,
        added: true,
    }])
    .unwrap_err();
    assert_eq!(nan.code, "transaction/nan-cannot-identify");

    // Preserve the old source-shaped left-to-right error precedence without
    // preserving its all-pairs implementation.
    let duplicate_before_nan = validate_attribute_uniqueness(&[
        decimal(1, "2.0"),
        Datom {
            entity: 9,
            attribute: 1_000,
            value: Value::Double(f64::NAN),
            tx: 1,
            added: true,
        },
        decimal(2, "2.00"),
    ])
    .unwrap_err();
    assert_eq!(duplicate_before_nan.code, "schema/unique-change-conflict");
    let nan_before_duplicate = validate_attribute_uniqueness(&[
        Datom {
            entity: 9,
            attribute: 1_000,
            value: Value::Double(f64::NAN),
            tx: 1,
            added: true,
        },
        decimal(1, "2.0"),
        decimal(2, "2.00"),
    ])
    .unwrap_err();
    assert_eq!(nan_before_duplicate.code, "transaction/nan-cannot-identify");
}

#[test]
fn sorted_identity_union_matches_eager_transitive_upsert_resolution() {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1_000,
                Keyword::new("identity", "left"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(
            Attribute::new(
                1_001,
                Keyword::new("identity", "right"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    let database = Database::new(schema).unwrap();
    let ops = vec![
        TxOp::Add {
            entity: EntityRef::Temp("a".into()),
            attribute: 1_000,
            value: Value::String("shared-left".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("b".into()),
            attribute: 1_000,
            value: Value::String("shared-left".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("b".into()),
            attribute: 1_001,
            value: Value::String("shared-right".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("c".into()),
            attribute: 1_001,
            value: Value::String("shared-right".into()).into(),
        },
    ];
    let eager = database.with(&ops, 10).unwrap();
    let assessed = assess_tiered(&DatabaseValue::eager(Arc::new(database)), &ops, 10).unwrap();
    assert_eq!(assessed.tempids, eager.tempids);
    assert_eq!(assessed.tx_data, eager.tx_data);
    assert_eq!(assessed.tempids["a"], assessed.tempids["b"]);
    assert_eq!(assessed.tempids["b"], assessed.tempids["c"]);
}
