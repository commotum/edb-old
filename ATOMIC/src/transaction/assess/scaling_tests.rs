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
fn fixed_delta_complete_schema_validation_and_dependency_work() {
    for width in [32, 256, 1_024] {
        let mut schema = wide_schema(width);
        for pair in 0..width / 2 {
            schema
                .install(
                    Attribute::new(
                        1_000 + width + pair,
                        Keyword::new("wide", format!("tuple-{pair}")),
                        ValueType::Tuple,
                        Cardinality::One,
                    )
                    .tuple(TupleSpec::Composite(vec![
                        1_000 + pair * 2,
                        1_001 + pair * 2,
                    ])),
                )
                .unwrap();
        }
        let setup = std::time::Instant::now();
        let database = Database::new(schema).unwrap();
        let value = DatabaseValue::eager(Arc::new(database));
        let attributes = value.schema().attributes().count() as u64;
        let setup_elapsed = setup.elapsed();
        let started = std::time::Instant::now();
        let report = assess_tiered(
            &value,
            &[TxOp::Add {
                entity: EntityRef::Temp("fixed".into()),
                attribute: 1_000,
                value: Value::Long(7).into(),
            }],
            10,
        )
        .unwrap();
        let work = report.read_work;
        assert_eq!(work.schema_transition_attributes, 0);
        assert_eq!(
            work.schema_validation,
            crate::model::schema::SchemaValidationWork::default()
        );
        assert_eq!(work.schema_projection_attributes, 0);
        assert_eq!(work.schema_reuses, 1);
        assert_eq!(work.dependency_lookups, 1);
        assert_eq!(work.dependency_edges, 1);
        assert_eq!(work.composite_candidates, 1);
        assert!(
            report
                .tx_data
                .iter()
                .any(|datom| datom.attribute == 1_000 + width
                    && datom.value == Value::Tuple(vec![Some(Value::Long(7)), None]))
        );
        drop(report);
        eprintln!(
            "fixed schema width={width} attributes={attributes} setup={setup_elapsed:?} full_assess_and_drop={:?} work={work:?}",
            started.elapsed()
        );
    }
}

#[test]
fn general_idents_reuse_schema_but_attribute_alias_repurposing_does_not() {
    let value = Database::new(wide_schema(8)).unwrap().database_value();
    let resident = value.schema_arc();
    let name = Keyword::new("status", "queued");
    let first = assess_tiered(
        &value,
        &[TxOp::Add {
            entity: EntityRef::Temp("enum".into()),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(name.clone()).into(),
        }],
        10,
    )
    .unwrap();
    let enum_id = first.tempids["enum"];
    assert_eq!(first.db_after.entid(&name), Some(enum_id));
    assert!(Arc::ptr_eq(&resident, &first.successor_schema));
    assert_eq!(
        first.read_work.schema_validation,
        crate::model::schema::SchemaValidationWork::default()
    );
    assert_eq!(first.read_work.schema_reuses, 1);
    let renamed = Keyword::new("status", "waiting");
    let second = assess_tiered(
        &first.db_after,
        &[TxOp::Add {
            entity: EntityRef::Id(enum_id),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(renamed.clone()).into(),
        }],
        11,
    )
    .unwrap();
    assert!(Arc::ptr_eq(&resident, &second.successor_schema));
    assert_eq!(second.db_after.entid(&name), Some(enum_id));
    assert_eq!(second.db_after.entid(&renamed), Some(enum_id));
    assert_eq!(first.db_after.entid(&renamed), None);

    let old_attribute_name = value.schema().attribute(1_000).unwrap().ident.clone();
    let new_attribute_name = Keyword::new("wide", "renamed");
    let rename = assess_tiered(
        &value,
        &[TxOp::Add {
            entity: EntityRef::Id(1_000),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(new_attribute_name.clone()).into(),
        }],
        10,
    )
    .unwrap();
    assert!(!Arc::ptr_eq(&resident, &rename.successor_schema));
    assert!(rename.read_work.schema_validation.attributes > 0);
    assert_eq!(
        rename.successor_schema.resolve_ident(&old_attribute_name),
        Some(1_000)
    );
    let repurpose = assess_tiered(
        &rename.db_after,
        &[TxOp::Add {
            entity: EntityRef::Temp("enum".into()),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(old_attribute_name.clone()).into(),
        }],
        11,
    )
    .unwrap();
    assert!(!Arc::ptr_eq(
        &rename.successor_schema,
        &repurpose.successor_schema
    ));
    assert_eq!(
        repurpose
            .successor_schema
            .resolve_ident(&old_attribute_name),
        None
    );
    assert_eq!(
        repurpose
            .successor_schema
            .resolve_ident(&new_attribute_name),
        Some(1_000)
    );
    assert_eq!(
        repurpose.db_after.entid(&old_attribute_name),
        Some(repurpose.tempids["enum"])
    );
    assert!(resident.datom_may_change_schema(
        DB_PART_DB,
        DB_IDENT as u32,
        &Value::Keyword(name),
        true
    ));
}

#[test]
fn validation_reuse_requires_the_validated_allocation_and_keeps_genesis_guard() {
    let value = Database::new(wide_schema(8)).unwrap().database_value();
    let candidate = value.schema().clone();
    let mut reader = Reader::new(&value, AssessmentLimits::unbounded());
    validate_schema_transition(&mut reader, &candidate, &[]).unwrap();
    assert_eq!(reader.work.schema_reuses, 0);
    assert_eq!(
        reader.work.schema_transition_attributes,
        candidate.attribute_count() as u64 * 2
    );
    assert_eq!(
        reader.work.schema_validation.attributes,
        candidate.attribute_count() as u64
    );
    let forbidden = crate::canonical_genesis_datoms()
        .into_iter()
        .next()
        .unwrap();
    let retraction = LogicalDatom {
        entity: forbidden.entity,
        attribute: forbidden.attribute,
        value: forbidden.value,
        added: false,
    };
    assert_eq!(
        validate_schema_transition(&mut reader, value.schema(), &[retraction])
            .unwrap_err()
            .code,
        "schema/native-information-immutable"
    );

    let mut invalid = wide_schema(2);
    invalid
        .install(
            Attribute::new(
                1_002,
                Keyword::new("bad", "tuple"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Composite(vec![1_000, 9_999])),
        )
        .unwrap();
    assert_eq!(
        Database::new(invalid).unwrap_err().code,
        "schema/invalid-tuple-attributes"
    );
}

#[test]
fn unchanged_schema_reuse_preserves_data_predicates_and_changed_tuple_validation() {
    let mut schema = wide_schema(2);
    let mut positive = schema.attribute(1_000).unwrap().clone();
    positive.predicates.push("test/positive".into());
    schema.alter(positive).unwrap();
    schema
        .install(
            Attribute::new(
                1_002,
                Keyword::new("wide", "pair"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Composite(vec![1_000, 1_001])),
        )
        .unwrap();
    let value = Database::new(schema).unwrap().database_value();
    let mut functions = TxFunctions::default();
    functions.register_attribute_predicate("test/positive", |value| {
        Ok(matches!(value, Value::Long(n) if *n > 0))
    });
    let negative = assess_tiered(
        &value,
        &[TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1_000,
            value: Value::Long(-1).into(),
        }],
        10,
    )
    .unwrap();
    assert_eq!(negative.read_work.schema_reuses, 1);
    assert_eq!(
        negative.validate_exact(Some(&functions)).unwrap_err().code,
        "transaction/attribute-predicate"
    );
    assert_eq!(value.basis_t(), 1);
    let mut constituent = value.schema().attribute(1_000).unwrap().clone();
    constituent.cardinality = Cardinality::Many;
    assert_eq!(
        assess_tiered(&value, &[TxOp::AlterAttribute(constituent.clone())], 10)
            .unwrap_err()
            .code,
        "schema/invalid-tuple-attributes"
    );
    let mut tuple = value.schema().attribute(1_002).unwrap().clone();
    tuple.tuple_discontinued = true;
    let discontinued = assess_tiered(&value, &[TxOp::AlterAttribute(tuple)], 10).unwrap();
    assert!(discontinued.read_work.schema_validation.attributes > 0);
    assert!(
        discontinued
            .successor_schema
            .composites_for_constituent(1_000)
            .next()
            .is_none()
    );
    let widened = assess_tiered(
        &discontinued.db_after,
        &[TxOp::AlterAttribute(constituent)],
        11,
    )
    .unwrap();
    assert_eq!(
        widened
            .successor_schema
            .attribute(1_000)
            .unwrap()
            .cardinality,
        Cardinality::Many
    );
    assert_eq!(
        value.schema().attribute(1_000).unwrap().cardinality,
        Cardinality::One
    );
}

#[test]
fn cached_attribute_predicate_dependencies_track_all_owners_and_failed_changes() {
    let mut schema = wide_schema(3);
    for id in [1_000, 1_001] {
        let mut attribute = schema.attribute(id).unwrap().clone();
        attribute.predicates = vec!["test/shared".into(), format!("test/p{id}")];
        schema.alter(attribute).unwrap();
    }
    assert_eq!(
        schema.attribute_predicate_names().collect::<Vec<_>>(),
        vec!["test/p1000", "test/p1001", "test/shared"]
    );
    let retained = schema.clone();
    let mut first = schema.attribute(1_000).unwrap().clone();
    first.predicates.clear();
    schema.alter(first).unwrap();
    assert!(schema.has_attribute_predicate("test/shared"));
    assert!(!schema.has_attribute_predicate("test/p1000"));
    let mut second = schema.attribute(1_001).unwrap().clone();
    second.predicates = vec!["unqualified".into()];
    assert!(schema.alter(second).is_err());
    assert!(schema.has_attribute_predicate("test/shared"));
    let mut second = schema.attribute(1_001).unwrap().clone();
    second.predicates.clear();
    schema.alter(second).unwrap();
    assert_eq!(schema.attribute_predicate_names().count(), 0);
    assert!(retained.has_attribute_predicate("test/shared"));

    let database = Database::new(retained).unwrap();
    assert_eq!(
        database
            .schema()
            .attribute_predicate_names()
            .collect::<Vec<_>>(),
        vec!["test/p1000", "test/p1001", "test/shared"]
    );
    let expected = database
        .schema()
        .attributes()
        .flat_map(|attribute| attribute.predicates.iter().map(String::as_str))
        .collect::<BTreeSet<_>>();
    assert_eq!(
        database
            .schema()
            .attribute_predicate_names()
            .collect::<BTreeSet<_>>(),
        expected
    );
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

/// The previous parent-walk mechanism, retained only as a counted test oracle.
/// It deliberately has neither path compression nor union-by-size.
struct UncompressedUnionFind {
    parent: Vec<usize>,
    parent_hops: usize,
}

impl UncompressedUnionFind {
    fn new(size: usize) -> Self {
        Self {
            parent: (0..size).collect(),
            parent_hops: 0,
        }
    }

    fn root(&mut self, mut index: usize) -> usize {
        while self.parent[index] != index {
            self.parent_hops += 1;
            index = self.parent[index];
        }
        index
    }

    fn join(&mut self, left: usize, right: usize) {
        let left = self.root(left);
        let right = self.root(right);
        self.parent[left.max(right)] = left.min(right);
    }
}

#[test]
fn identity_parent_compression_preserves_minimum_roots_and_bounds_chain_walks() {
    for size in [2, 32, 128, 512, 2_048] {
        let mut compressed = UnionFind::new(size + 1);
        let mut previous = UncompressedUnionFind::new(size + 1);
        for left in (0..size - 1).rev() {
            compressed.join(left, left + 1);
            previous.join(left, left + 1);
        }
        // Sorted identity grouping can create this exact chain. The extra
        // element remains disconnected and must not acquire another root.
        assert_eq!(compressed.parent, previous.parent);
        for _ in 0..2 {
            for index in 0..size {
                assert_eq!(compressed.root(index), previous.root(index));
                assert_eq!(compressed.parent[index], 0);
            }
            assert_eq!(compressed.root(size), size);
            assert_eq!(previous.root(size), size);
        }
        assert_eq!(previous.parent_hops, size * (size - 1));
        // Includes both the search and rewrite passes, not just comparisons.
        // This bound is for this adversarial sequence, not arbitrary unions.
        assert!(compressed.parent_hops <= 6 * size);
        eprintln!(
            "identity parent chain n={size} old_hops={} compressed_hops={}",
            previous.parent_hops, compressed.parent_hops,
        );
    }
}

#[test]
fn connected_identity_chain_assessment_preserves_allocation_and_conflicts() {
    for size in [32_usize, 128, 512] {
        let setup = std::time::Instant::now();
        let mut schema = Schema::new();
        let mut ops = Vec::new();
        for edge in 0..size - 1 {
            let attribute = 1_000 + u32::try_from(edge).unwrap();
            schema
                .install(
                    Attribute::new(
                        attribute,
                        Keyword::new("chain", format!("edge-{edge:04}")),
                        ValueType::Long,
                        Cardinality::One,
                    )
                    .unique(Unique::Identity),
                )
                .unwrap();
            // Increasing attribute keys connect decreasing tempid positions:
            // each connected entity has one value per unique attribute.
            for index in [size - 2 - edge, size - 1 - edge] {
                ops.push(TxOp::Add {
                    entity: EntityRef::Temp(format!("entity-{index:04}")),
                    attribute,
                    value: Value::Long(7).into(),
                });
            }
        }
        let base = Database::new(schema).unwrap().database_value();
        let expected_index = base.eidx_frontier().max(base.basis_t() + 2);
        let expected_entity = make_eid(USER_PARTITION, expected_index).unwrap();
        let setup_elapsed = setup.elapsed();
        let started = std::time::Instant::now();
        let assessed = assess_tiered(&base, &ops, 10).unwrap();
        let mut assessment_elapsed = started.elapsed();
        let hops = assessed.read_work.identity_parent_hops;
        let read_work = assessed.read_work;
        assert_eq!(assessed.tempids.len(), size);
        assert!(assessed.tempids.values().all(|id| *id == expected_entity));
        assert_eq!(assessed.eidx_frontier, expected_index + 1);
        // Independent expected facts, not another entry to the same assessor.
        let facts = assessed
            .tx_data
            .iter()
            .filter(|datom| datom.entity == expected_entity)
            .map(|datom| (datom.attribute, datom.value.clone(), datom.added))
            .collect::<Vec<_>>();
        assert_eq!(
            facts,
            (0..size - 1)
                .map(|edge| (1_000 + u32::try_from(edge).unwrap(), Value::Long(7), true))
                .collect::<Vec<_>>()
        );
        assert!(
            hops <= 6 * size,
            "actual assessment followed {hops} parent edges"
        );
        drop(facts);
        let drop_started = std::time::Instant::now();
        drop(assessed);
        assessment_elapsed += drop_started.elapsed();
        let mut previous = UncompressedUnionFind::new(size);
        for left in (0..size - 1).rev() {
            previous.join(left, left + 1);
        }
        for _ in 0..2 {
            for index in 0..size {
                assert_eq!(previous.root(index), 0);
            }
        }
        assert_eq!(previous.parent_hops, size * (size - 1));
        eprintln!(
            "connected identity assessment n={size} ops={} setup={setup_elapsed:?} assess_and_drop={assessment_elapsed:?} old_parent_hops={} compressed_parent_hops={hops} reads={read_work:?}",
            ops.len(),
            previous.parent_hops,
        );

        if size == 32 {
            // The same connected component must still reject two existing
            // owners, and conflicting partition directives before allocation.
            let mut split = ops.clone();
            split.push(TxOp::ForcePartition {
                tempid: "entity-0000".into(),
                partition: EntityRef::Id(crate::DB_PART_USER),
            });
            split.push(TxOp::ForcePartition {
                tempid: format!("entity-{:04}", size - 1),
                partition: EntityRef::Id(crate::DB_PART_TX),
            });
            assert_eq!(
                assess_tiered(&base, &split, 10).unwrap_err().code,
                "transaction/partition-conflict"
            );
            let existing = assess_tiered(
                &base,
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("left".into()),
                        attribute: 1_000,
                        value: Value::Long(7).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("right".into()),
                        attribute: 1_000 + u32::try_from(size - 2).unwrap(),
                        value: Value::Long(7).into(),
                    },
                ],
                10,
            )
            .unwrap();
            let error = assess_tiered(&existing.db_after, &ops, 11).unwrap_err();
            assert_eq!(error.code, "transaction/upsert-conflict");
            assert_eq!(existing.db_after.basis_t(), base.basis_t() + 1);
        }
    }
}

#[test]
fn successor_schema_validation_early_reduces_ordered_base_ranges() {
    const ATTRIBUTE: u32 = 1_000;
    const BASE_FACTS: usize = 512;
    const SMALL_READ_CAP: u64 = 64;

    let mut many_schema = Schema::new();
    many_schema
        .install(Attribute::new(
            ATTRIBUTE,
            Keyword::new("stream", "many"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    let mut many_ops = Vec::with_capacity(BASE_FACTS + 1);
    many_ops.push(TxOp::Add {
        entity: EntityRef::Temp("e-0000".into()),
        attribute: ATTRIBUTE,
        value: Value::String("a".into()).into(),
    });
    many_ops.push(TxOp::Add {
        entity: EntityRef::Temp("e-0000".into()),
        attribute: ATTRIBUTE,
        value: Value::String("b".into()).into(),
    });
    for ordinal in 1..BASE_FACTS {
        many_ops.push(TxOp::Add {
            entity: EntityRef::Temp(format!("e-{ordinal:04}")),
            attribute: ATTRIBUTE,
            value: Value::String(format!("v-{ordinal:04}")).into(),
        });
    }
    let many = Database::new(many_schema)
        .unwrap()
        .with(&many_ops, 10)
        .unwrap()
        .db_after;
    let mut cardinality_one = many.schema().attribute(ATTRIBUTE).unwrap().clone();
    cardinality_one.cardinality = Cardinality::One;
    let alter_cardinality = [TxOp::AlterAttribute(cardinality_one)];
    let eager = many.with(&alter_cardinality, 11).unwrap_err();
    let tiered = assess_tiered_with_limits(
        &DatabaseValue::eager(Arc::new(many)),
        &alter_cardinality,
        11,
        AssessmentLimits {
            max_read_datoms: SMALL_READ_CAP,
            max_read_bytes: u64::MAX,
        },
    )
    .unwrap_err();
    assert_eq!(eager.code, "schema/cardinality-change-conflict");
    assert_eq!(tiered.category, eager.category);
    assert_eq!(tiered.code, eager.code);

    // AVET's stored order keeps different BigDecimal scales adjacent under
    // the logical comparator. The first two values conflict, so recovered
    // source-order reduction reaches the semantic error without reading the
    // hundreds of later values or exhausting the explicit admission cap.
    let mut decimal = Attribute::new(
        ATTRIBUTE,
        Keyword::new("stream", "decimal"),
        ValueType::BigDec,
        Cardinality::One,
    );
    decimal.indexed = true;
    let mut decimal_schema = Schema::new();
    decimal_schema.install(decimal).unwrap();
    let mut decimal_ops = Vec::with_capacity(BASE_FACTS);
    decimal_ops.push(TxOp::Add {
        entity: EntityRef::Temp("e-0000".into()),
        attribute: ATTRIBUTE,
        value: Value::BigDec(BigDecimal::from_str("0.0").unwrap()).into(),
    });
    decimal_ops.push(TxOp::Add {
        entity: EntityRef::Temp("e-0001".into()),
        attribute: ATTRIBUTE,
        value: Value::BigDec(BigDecimal::from_str("0.00").unwrap()).into(),
    });
    for ordinal in 2..BASE_FACTS {
        decimal_ops.push(TxOp::Add {
            entity: EntityRef::Temp(format!("e-{ordinal:04}")),
            attribute: ATTRIBUTE,
            value: Value::BigDec(BigDecimal::from(i64::try_from(ordinal).unwrap())).into(),
        });
    }
    let decimals = Database::new(decimal_schema)
        .unwrap()
        .with(&decimal_ops, 10)
        .unwrap()
        .db_after;
    let mut unique = decimals.schema().attribute(ATTRIBUTE).unwrap().clone();
    unique.unique = Some(Unique::Value);
    let alter_unique = [TxOp::AlterAttribute(unique)];
    let eager = decimals.with(&alter_unique, 11).unwrap_err();
    let tiered = assess_tiered_with_limits(
        &DatabaseValue::eager(Arc::new(decimals)),
        &alter_unique,
        11,
        AssessmentLimits {
            max_read_datoms: SMALL_READ_CAP,
            max_read_bytes: u64::MAX,
        },
    )
    .unwrap_err();
    assert_eq!(eager.code, "schema/unique-change-conflict");
    assert_eq!(tiered.category, eager.category);
    assert_eq!(tiered.code, eager.code);
}

#[test]
fn successor_schema_stream_charges_each_base_datom_and_retains_the_read_cap() {
    const ATTRIBUTE: u32 = 1_000;
    const BASE_FACTS: usize = 128;
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ATTRIBUTE,
            Keyword::new("stream", "bounded"),
            ValueType::Long,
            Cardinality::Many,
        ))
        .unwrap();
    let ops = (0..BASE_FACTS)
        .map(|ordinal| TxOp::Add {
            entity: EntityRef::Temp(format!("e-{ordinal:04}")),
            attribute: ATTRIBUTE,
            value: Value::Long(i64::try_from(ordinal).unwrap()).into(),
        })
        .collect::<Vec<_>>();
    let database = Database::new(schema)
        .unwrap()
        .with(&ops, 10)
        .unwrap()
        .db_after;
    let value = DatabaseValue::eager(Arc::new(database.clone()));
    let mut reader = Reader::new(
        &value,
        AssessmentLimits {
            max_read_datoms: u64::MAX,
            max_read_bytes: u64::MAX,
        },
    );
    validate_many_to_one_successor(&mut reader, &[], ATTRIBUTE).unwrap();
    assert_eq!(reader.work.prefixes, 1);
    assert_eq!(reader.work.prefix_hits, 0);
    assert_eq!(reader.work.datoms, BASE_FACTS as u64);

    let mut one = database.schema().attribute(ATTRIBUTE).unwrap().clone();
    one.cardinality = Cardinality::One;
    let error = assess_tiered_with_limits(
        &DatabaseValue::eager(Arc::new(database)),
        &[TxOp::AlterAttribute(one)],
        11,
        AssessmentLimits {
            max_read_datoms: 32,
            max_read_bytes: u64::MAX,
        },
    )
    .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Busy);
    assert_eq!(error.code, "transaction/read-capacity");
}

#[test]
fn successor_delta_retractions_make_schema_changes_valid_over_an_overlay() {
    const TAG: u32 = 1_000;
    const EXTERNAL_ID: u32 = 1_001;
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            TAG,
            Keyword::new("stream", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    let mut external_id = Attribute::new(
        EXTERNAL_ID,
        Keyword::new("stream", "external-id"),
        ValueType::BigDec,
        Cardinality::One,
    );
    external_id.indexed = true;
    schema.install(external_id).unwrap();
    let initial = Database::new(schema).unwrap();
    let seed_ops = vec![
        TxOp::Add {
            entity: EntityRef::Temp("left".into()),
            attribute: TAG,
            value: Value::String("red".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("left".into()),
            attribute: TAG,
            value: Value::String("blue".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("left".into()),
            attribute: EXTERNAL_ID,
            value: Value::BigDec(BigDecimal::from_str("1.0").unwrap()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("right".into()),
            attribute: EXTERNAL_ID,
            value: Value::BigDec(BigDecimal::from_str("1.00").unwrap()).into(),
        },
    ];
    let eager_seed = initial.with(&seed_ops, 10).unwrap();
    let tiered_seed =
        assess_tiered(&DatabaseValue::eager(Arc::new(initial)), &seed_ops, 10).unwrap();
    assert_eq!(tiered_seed.tx_data, eager_seed.tx_data);
    let left = eager_seed.tempids["left"];
    let right = eager_seed.tempids["right"];
    let mut tag_one = eager_seed.db_after.schema().attribute(TAG).unwrap().clone();
    tag_one.cardinality = Cardinality::One;
    let mut unique = eager_seed
        .db_after
        .schema()
        .attribute(EXTERNAL_ID)
        .unwrap()
        .clone();
    unique.unique = Some(Unique::Value);
    let repair = vec![
        TxOp::Retract {
            entity: EntityRef::Id(left),
            attribute: TAG,
            value: Some(Value::String("blue".into()).into()),
        },
        TxOp::Retract {
            entity: EntityRef::Id(right),
            attribute: EXTERNAL_ID,
            value: Some(Value::BigDec(BigDecimal::from_str("1.00").unwrap()).into()),
        },
        TxOp::AlterAttribute(tag_one),
        TxOp::AlterAttribute(unique),
    ];
    let eager = eager_seed.db_after.with(&repair, 11).unwrap();
    assert_eq!(
        eager
            .db_after
            .values(left, TAG)
            .into_iter()
            .cloned()
            .collect::<Vec<_>>(),
        vec![Value::String("red".into())]
    );

    // `tiered_seed.db_after` is a bounded transaction overlay. This second
    // validation therefore exercises the production overlay prefix cursor,
    // then merges the new repair delta without recovering an eager Database.
    // A completed assessment cannot itself publish another assessment overlay;
    // the committed writer normally replaces it with a fresh native value.
    let logical = vec![
        LogicalDatom {
            entity: left,
            attribute: TAG,
            value: Value::String("blue".into()),
            added: false,
        },
        LogicalDatom {
            entity: right,
            attribute: EXTERNAL_ID,
            value: Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
            added: false,
        },
    ];
    let mut reader = Reader::new(
        &tiered_seed.db_after,
        AssessmentLimits {
            max_read_datoms: u64::MAX,
            max_read_bytes: u64::MAX,
        },
    );
    validate_many_to_one_successor(&mut reader, &logical, TAG).unwrap();
    validate_unique_successor(&mut reader, &logical, EXTERNAL_ID).unwrap();
    assert_eq!(reader.work.prefixes, 2);
}

#[test]
fn successor_unique_stream_preserves_nan_rejection() {
    const ATTRIBUTE: u32 = 1_000;
    let mut score = Attribute::new(
        ATTRIBUTE,
        Keyword::new("stream", "score"),
        ValueType::Double,
        Cardinality::One,
    );
    score.indexed = true;
    let mut schema = Schema::new();
    schema.install(score).unwrap();
    let database = Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("score".into()),
                attribute: ATTRIBUTE,
                value: Value::Double(f64::NAN).into(),
            }],
            10,
        )
        .unwrap()
        .db_after;
    let mut unique = database.schema().attribute(ATTRIBUTE).unwrap().clone();
    unique.unique = Some(Unique::Value);
    let ops = [TxOp::AlterAttribute(unique)];
    let eager = database.with(&ops, 11).unwrap_err();
    let tiered = assess_tiered(&DatabaseValue::eager(Arc::new(database)), &ops, 11).unwrap_err();
    assert_eq!(eager.code, "transaction/nan-cannot-identify");
    assert_eq!(tiered.category, eager.category);
    assert_eq!(tiered.code, eager.code);

    // A real uniqueness conflict is the schema-transition error even when a
    // later AVET value is NaN. This locks the ordered streaming path to the
    // eager oracle's phase/error precedence.
    let mut score = Attribute::new(
        ATTRIBUTE,
        Keyword::new("stream", "mixed-score"),
        ValueType::Double,
        Cardinality::One,
    );
    score.indexed = true;
    let mut schema = Schema::new();
    schema.install(score).unwrap();
    let mixed = Database::new(schema)
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("a-nan".into()),
                    attribute: ATTRIBUTE,
                    value: Value::Double(f64::NAN).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("b-duplicate-left".into()),
                    attribute: ATTRIBUTE,
                    value: Value::Double(1.0).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("c-duplicate-right".into()),
                    attribute: ATTRIBUTE,
                    value: Value::Double(1.0).into(),
                },
            ],
            10,
        )
        .unwrap()
        .db_after;
    let mut unique = mixed.schema().attribute(ATTRIBUTE).unwrap().clone();
    unique.unique = Some(Unique::Value);
    let ops = [TxOp::AlterAttribute(unique)];
    let eager = mixed.with(&ops, 11).unwrap_err();
    let tiered = assess_tiered(&DatabaseValue::eager(Arc::new(mixed)), &ops, 11).unwrap_err();
    assert_eq!(eager.code, "schema/unique-change-conflict");
    assert_eq!(tiered.category, eager.category);
    assert_eq!(tiered.code, eager.code);
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
