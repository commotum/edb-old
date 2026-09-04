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
fn ordinary_assessment_shares_schema_and_real_schema_edits_replace_it() {
    let database = Database::new(wide_schema(64)).unwrap();
    let value = DatabaseValue::eager(Arc::new(database));
    let resident = value.schema_arc();
    let ops = [TxOp::Add {
        entity: EntityRef::Temp("ordinary".into()),
        attribute: 1_063,
        value: Value::Long(7).into(),
    }];

    let ordinary = assess_tiered(&value, &ops, 10).unwrap();
    assert!(
        Arc::ptr_eq(&resident, &ordinary.successor_schema),
        "a data transaction must retain the resident schema allocation"
    );
    assert!(
        Arc::ptr_eq(&resident, &ordinary.db_after.schema_arc()),
        "the transaction overlay must carry the same immutable projection"
    );

    let mut altered = value.schema().attribute(1_063).unwrap().clone();
    altered.no_history = true;
    let schema_edit = assess_tiered(&value, &[TxOp::AlterAttribute(altered)], 10).unwrap();
    assert!(
        !Arc::ptr_eq(&resident, &schema_edit.successor_schema),
        "a material schema edit must publish a new immutable projection"
    );
    assert!(
        schema_edit
            .successor_schema
            .attribute(1_063)
            .unwrap()
            .no_history
    );
}

#[test]
fn composite_work_uses_constituent_reverse_links_not_schema_width() {
    const ATTRIBUTE_COUNT: u32 = 512;
    const COMPOSITE: u32 = 1_000 + ATTRIBUTE_COUNT;
    let mut schema = wide_schema(ATTRIBUTE_COUNT);
    schema
        .install(
            Attribute::new(
                COMPOSITE,
                Keyword::new("wide", "pair"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Composite(vec![1_000, 1_001])),
        )
        .unwrap();
    schema.validate_tuple_definitions().unwrap();
    assert_eq!(
        schema.composites_for_constituent(1_000).collect::<Vec<_>>(),
        vec![COMPOSITE]
    );
    assert!(schema.composites_for_constituent(1_002).next().is_none());

    let database = Database::new(schema.clone()).unwrap();
    let value = DatabaseValue::eager(Arc::new(database));
    let unrelated = assess_tiered(
        &value,
        &[TxOp::Add {
            entity: EntityRef::Temp("unrelated".into()),
            attribute: 1_000 + ATTRIBUTE_COUNT - 1,
            value: Value::Long(7).into(),
        }],
        10,
    )
    .unwrap();
    assert_eq!(unrelated.read_work.composite_candidates, 0);

    let constituent = assess_tiered(
        &value,
        &[TxOp::Add {
            entity: EntityRef::Temp("constituent".into()),
            attribute: 1_000,
            value: Value::Long(8).into(),
        }],
        10,
    )
    .unwrap();
    assert_eq!(constituent.read_work.composite_candidates, 1);
    assert!(
        constituent
            .tx_data
            .iter()
            .any(|datom| datom.attribute == COMPOSITE && datom.added)
    );

    let mut discontinued = schema.attribute(COMPOSITE).unwrap().clone();
    discontinued.tuple_discontinued = true;
    schema.alter(discontinued).unwrap();
    assert!(
        schema.composites_for_constituent(1_000).next().is_none(),
        "discontinuation must remove recovered constituent reverse links"
    );
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

fn quadratic_dedupe(datoms: &mut Vec<LogicalDatom>) {
    let mut result = Vec::new();
    for datom in datoms.drain(..) {
        if !result.iter().any(|existing| same_logical(existing, &datom)) {
            result.push(datom);
        }
    }
    result.sort_by(compare_logical);
    *datoms = result;
}

fn quadratic_same_transaction(
    schema: &Schema,
    datoms: &[LogicalDatom],
) -> Result<(), SemanticError> {
    for (index, left) in datoms.iter().enumerate() {
        let attribute = schema.attribute(left.attribute)?;
        for right in &datoms[index + 1..] {
            if left.entity == right.entity
                && left.attribute == right.attribute
                && left.value.stored_eq(&right.value)
                && left.added != right.added
            {
                return Err(SemanticError::conflict(
                    "transaction/datoms-conflict",
                    "addition and retraction of the same E/A/V conflict",
                ));
            }
            if left.entity == right.entity
                && left.attribute == right.attribute
                && left.added
                && right.added
                && attribute.cardinality == Cardinality::One
                && left.value.index_cmp(&right.value).is_ne()
            {
                return Err(SemanticError::conflict(
                    "transaction/cardinality-one-conflict",
                    "two values for one cardinality-one E/A conflict",
                ));
            }
            if left.attribute == right.attribute
                && left.added
                && right.added
                && attribute.unique.is_some()
                && left.value.index_cmp(&right.value).is_eq()
                && left.entity != right.entity
            {
                return Err(SemanticError::conflict(
                    "transaction/unique-conflict",
                    "two entities assert the same unique A/V",
                ));
            }
        }
    }
    Ok(())
}

fn same_logical_slice(left: &[LogicalDatom], right: &[LogicalDatom]) -> bool {
    left.len() == right.len()
        && left
            .iter()
            .zip(right)
            .all(|(left, right)| same_logical(left, right))
}

#[test]
fn dedupe_is_n_log_n_and_retains_stored_numeric_representations() {
    const DISTINCT: usize = 8_192;
    let mut datoms = Vec::with_capacity(DISTINCT * 2 + 2);
    for ordinal in (0..DISTINCT).rev() {
        let datom = LogicalDatom {
            entity: u64::try_from(ordinal % 257).unwrap(),
            attribute: 1_000 + u32::try_from(ordinal % 5).unwrap(),
            value: Value::Long(i64::try_from(ordinal).unwrap()),
            added: ordinal % 3 != 0,
        };
        datoms.push(datom.clone());
        datoms.push(datom);
    }
    datoms.push(LogicalDatom {
        entity: 99_999,
        attribute: 1_000,
        value: Value::BigDec(BigDecimal::from_str("1.0").unwrap()),
        added: true,
    });
    datoms.push(LogicalDatom {
        entity: 99_999,
        attribute: 1_000,
        value: Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
        added: true,
    });
    let mut expected = datoms.clone();
    quadratic_dedupe(&mut expected);
    let input_len = datoms.len();
    let comparisons = dedupe_with_work(&mut datoms);
    assert!(same_logical_slice(&datoms, &expected));
    assert_eq!(datoms.len(), DISTINCT + 2);
    assert!(
        comparisons < input_len * 40,
        "dedupe used {comparisons} comparisons"
    );
    assert!(
        datoms
            .windows(2)
            .all(|pair| compare_logical(&pair[0], &pair[1]).is_lt())
    );
}

fn validation_schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("validation", "many"),
            ValueType::Long,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            1_001,
            Keyword::new("validation", "one"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                1_002,
                Keyword::new("validation", "identity"),
                ValueType::BigDec,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
}

#[test]
fn keyed_same_transaction_validation_scales_and_matches_quadratic_precedence() {
    const DATOMS: usize = 8_192;
    let schema = validation_schema();
    let valid = (0..DATOMS)
        .rev()
        .map(|ordinal| LogicalDatom {
            entity: u64::try_from(ordinal + 1).unwrap(),
            attribute: 1_002,
            value: Value::BigDec(
                BigDecimal::from_str(&format!("{ordinal}.{:02}", ordinal % 97)).unwrap(),
            ),
            added: true,
        })
        .collect::<Vec<_>>();
    let comparisons = validate_same_transaction_with_work(&schema, &valid).unwrap();
    assert!(
        comparisons < DATOMS * 80,
        "same-transaction validation used {comparisons} comparisons"
    );

    // Compare the replacement to its exact former implementation over a
    // deterministic mix of operation, cardinality, uniqueness, unknown-attr,
    // and logical-equal/stored-distinct numeric cases. This also protects the
    // former left-index/right-index failure precedence.
    let mut state = 0x9e37_79b9_7f4a_7c15_u64;
    for _case in 0..256 {
        let mut datoms = Vec::new();
        for index in 0..32 {
            state ^= state << 7;
            state ^= state >> 9;
            state ^= state << 8;
            let attribute = match state % 11 {
                0 => 9_999,
                1..=3 => 1_001,
                4..=7 => 1_002,
                _ => 1_000,
            };
            let value = if attribute == 1_002 {
                let whole = state % 5;
                let scale = if state & 1 == 0 { "0" } else { "00" };
                Value::BigDec(BigDecimal::from_str(&format!("{whole}.{scale}")).unwrap())
            } else {
                Value::Long(i64::try_from(state % 7).unwrap())
            };
            datoms.push(LogicalDatom {
                entity: 1 + state % 6,
                attribute,
                value,
                added: (state ^ u64::try_from(index).unwrap()) & 1 == 0,
            });
        }
        let old = quadratic_same_transaction(&schema, &datoms);
        let new = validate_same_transaction(&schema, &datoms);
        match (old, new) {
            (Ok(()), Ok(())) => {}
            (Err(old), Err(new)) => {
                assert_eq!(new.category, old.category);
                assert_eq!(new.code, old.code);
            }
            outcomes => panic!("same-transaction validator divergence: {outcomes:?}"),
        }
    }
}

#[test]
fn unique_delta_grouping_scales_by_logical_av_key() {
    const DATOMS: usize = 8_192;
    let logical = (0..DATOMS)
        .rev()
        .map(|ordinal| LogicalDatom {
            entity: u64::try_from(ordinal + 1).unwrap(),
            attribute: 1_002,
            value: Value::BigDec(
                BigDecimal::from_str(&format!("{ordinal}.{:02}", ordinal % 97)).unwrap(),
            ),
            added: true,
        })
        .collect::<Vec<_>>();
    let (groups, comparisons) = group_unique_deltas(&logical, (0..logical.len()).collect());
    assert_eq!(groups.len(), DATOMS);
    assert!(
        comparisons < DATOMS * 40,
        "A/V grouping used {comparisons} comparisons"
    );

    let scales = vec![
        LogicalDatom {
            entity: 1,
            attribute: 1_002,
            value: Value::BigDec(BigDecimal::from_str("1.0").unwrap()),
            added: false,
        },
        LogicalDatom {
            entity: 2,
            attribute: 1_002,
            value: Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
            added: true,
        },
    ];
    let (groups, _) = group_unique_deltas(&scales, vec![0, 1]);
    assert_eq!(groups.len(), 1);
    assert_eq!(groups[0].deltas.len(), 2);
}

#[test]
fn logical_equal_stored_distinct_unique_ownership_transfers_match_eager() {
    const DECIMAL: u32 = 1_000;
    const LABEL: u32 = 1_001;
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                DECIMAL,
                Keyword::new("item", "decimal"),
                ValueType::BigDec,
                Cardinality::One,
            )
            .unique(Unique::Value),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            LABEL,
            Keyword::new("item", "label"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let initial = Database::new(schema).unwrap();
    let decimal =
        |spelling: &str| TxValue::Scalar(Value::BigDec(BigDecimal::from_str(spelling).unwrap()));
    let seeded = initial
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("holder".into()),
                    attribute: DECIMAL,
                    value: decimal("1.0"),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("recipient".into()),
                    attribute: LABEL,
                    value: Value::String("recipient".into()).into(),
                },
            ],
            10,
        )
        .unwrap();
    let holder = seeded.tempids["holder"];
    let recipient = seeded.tempids["recipient"];
    let ops = vec![
        TxOp::Retract {
            entity: EntityRef::Id(holder),
            attribute: DECIMAL,
            value: Some(decimal("1.0")),
        },
        TxOp::Add {
            entity: EntityRef::Id(recipient),
            attribute: DECIMAL,
            value: decimal("1.00"),
        },
    ];
    let eager = seeded.db_after.with(&ops, 11).unwrap();
    let assessed =
        assess_tiered(&DatabaseValue::eager(Arc::new(seeded.db_after)), &ops, 11).unwrap();
    assert_eq!(assessed.tx_data, eager.tx_data);
    assert_eq!(
        assessed.db_after.values(recipient, DECIMAL).unwrap(),
        eager
            .db_after
            .values(recipient, DECIMAL)
            .into_iter()
            .cloned()
            .collect::<Vec<_>>()
    );
}
