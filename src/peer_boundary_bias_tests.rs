//! Private strict query starts reuse ordinary index ordering without sentinel
//! entities or persisted key changes. Oracles compare the intended value/attr
//! relation independently of the virtual-boundary comparator and tree routing.
use super::*;
use crate::{Attribute, Cardinality, Schema, Value, ValueType};
use bigdecimal::BigDecimal;
use std::str::FromStr;

fn decimal(text: &str) -> Value {
    Value::BigDec(BigDecimal::from_str(text).unwrap())
}

fn strict(attribute: u32, value: Value) -> NormalizedIndexBoundary {
    IndexBoundary::Avet(IndexComponents::Two(attribute, value))
        .normalized()
        .unwrap()
        .after_prefix()
}

fn datom(entity: u64, attribute: u32, value: Value) -> Datom {
    Datom {
        entity: crate::make_eid(crate::USER_PARTITION, entity).unwrap(),
        attribute,
        value,
        tx: crate::t_to_tx(1).unwrap(),
        added: true,
    }
}

fn value_cases() -> Vec<(Value, Value, Vec<Value>, Value)> {
    vec![
        (
            Value::Long(1),
            Value::Long(0),
            vec![Value::Long(1), decimal("1.0"), decimal("1.00")],
            Value::Long(2),
        ),
        (
            decimal("1.000"),
            decimal("0"),
            vec![decimal("1"), decimal("1.00"), decimal("1.00000")],
            decimal("2"),
        ),
        (
            Value::Tuple(vec![Some(Value::Long(1)), None]),
            Value::Tuple(vec![Some(Value::Long(0)), None]),
            vec![
                Value::Tuple(vec![Some(Value::Long(1)), None]),
                Value::Tuple(vec![Some(decimal("1.00")), None]),
            ],
            Value::Tuple(vec![Some(Value::Long(2)), None]),
        ),
    ]
}

#[test]
fn after_prefix_bias_keeps_public_ties_but_skips_all_private_numeric_tuple_ties() {
    for (lower, below, ties, above) in value_cases() {
        let public = IndexBoundary::Avet(IndexComponents::Two(1_000, lower.clone()))
            .normalized()
            .unwrap();
        let biased = public.clone().after_prefix();
        assert_eq!(biased.clone().after_prefix(), biased, "bias is idempotent");
        assert_eq!(biased.order(), public.order());
        for value in ties {
            let candidate = datom(1, 1_000, value);
            assert!(public.compare_datom(&candidate).is_eq());
            assert!(biased.compare_datom(&candidate).is_lt());
            assert!(biased.compare_current_group_start(&candidate).is_lt());
        }
        assert!(
            biased
                .compare_datom(&datom(2, 1_000, below.clone()))
                .is_lt()
        );
        assert!(biased.compare_datom(&datom(2, 1_000, above)).is_gt());
        assert!(biased.compare_datom(&datom(2, 1_001, below)).is_gt());
    }

    let empty = IndexBoundary::Avet(IndexComponents::Empty)
        .normalized()
        .unwrap();
    let candidate = datom(1, 1_000, Value::Long(1));
    assert!(empty.compare_datom(&candidate).is_eq());
    assert!(empty.after_prefix().compare_datom(&candidate).is_lt());
    let attribute_end = IndexBoundary::Avet(IndexComponents::One(1_000))
        .normalized()
        .unwrap()
        .after_prefix();
    assert!(attribute_end.compare_datom(&candidate).is_lt());
    assert!(
        attribute_end
            .compare_datom(&datom(1, 1_001, Value::Long(0)))
            .is_gt()
    );
}

#[test]
fn after_prefix_routes_past_ties_spanning_persistent_directories_and_leaves() {
    let config = TreeConfig {
        max_leaf_datoms: 4,
        target_leaf_bytes: 512,
        max_leaf_bytes: 2_048,
        max_leaves_per_directory: 4,
        max_directory_bytes: 4_096,
        max_directories_per_root: 1_024,
        max_root_bytes: 1024 * 1024,
    };
    for (case, (lower, below, ties, above)) in value_cases().into_iter().enumerate() {
        for width in [32, 256, 1_024] {
            let setup = Instant::now();
            let mut datoms = vec![datom(1, 1_000, below.clone())];
            datoms.extend((0..width).map(|ordinal| {
                datom(
                    ordinal as u64 + 2,
                    1_000,
                    ties[ordinal % ties.len()].clone(),
                )
            }));
            datoms.push(datom(width as u64 + 2, 1_000, above.clone()));
            datoms.push(datom(width as u64 + 3, 1_001, below.clone()));
            datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Avet));
            let build = build_tree(IndexOrder::Avet, false, datoms.clone(), &config).unwrap();
            let TreeNode::Root(root) = decode_tree_node(
                &build.descriptor.root_hash,
                build.nodes.get(&build.descriptor.root_hash).unwrap(),
            )
            .unwrap() else {
                panic!("root node")
            };
            assert!(root.directories.len() > 1);
            let setup_elapsed = setup.elapsed();
            // The resident-root routing operation decodes only its selected
            // directory and leaf, not each equal-valued child in the prefix.
            let boundary = TreeBoundary::new(strict(1_000, lower.clone()));
            let started = Instant::now();
            let directory_index = boundary_start_child(&root.directories, &boundary);
            let directory_ref = &root.directories[directory_index];
            let TreeNode::Directory(directory) = decode_tree_node(
                &directory_ref.hash,
                build.nodes.get(&directory_ref.hash).unwrap(),
            )
            .unwrap() else {
                panic!("directory node")
            };
            let leaf_index = boundary_start_child(&directory.leaves, &boundary);
            let leaf_ref = &directory.leaves[leaf_index];
            let TreeNode::Leaf(leaf) =
                decode_tree_node(&leaf_ref.hash, build.nodes.get(&leaf_ref.hash).unwrap()).unwrap()
            else {
                panic!("leaf node")
            };
            let position = leaf_boundary_lower_bound(&leaf, &boundary);
            let route_elapsed = started.elapsed();

            let global_position = root.directories[..directory_index]
                .iter()
                .map(|child| child.count as usize)
                .sum::<usize>()
                + directory.leaves[..leaf_index]
                    .iter()
                    .map(|child| child.count as usize)
                    .sum::<usize>()
                + position;
            let expected_position = datoms
                .iter()
                .position(|datom| {
                    datom.attribute > 1_000
                        || (datom.attribute == 1_000 && datom.value.index_cmp(&lower).is_gt())
                })
                .unwrap();
            assert_eq!(global_position, expected_position);
            assert_eq!(datoms[global_position].value, above);
            // Independent exact endpoint cases: after the last value is the
            // next attribute; after the final attribute is the index end.
            for end in [
                strict(1_000, above.clone()),
                IndexBoundary::Avet(IndexComponents::One(1_001))
                    .normalized()
                    .unwrap()
                    .after_prefix(),
                IndexBoundary::Avet(IndexComponents::Empty)
                    .normalized()
                    .unwrap()
                    .after_prefix(),
            ] {
                let expected = if end.unbiased() == strict(1_000, above.clone()).unbiased() {
                    datoms.len() - 1
                } else {
                    datoms.len()
                };
                assert_eq!(
                    datoms.partition_point(|datom| end.compare_datom(datom).is_lt()),
                    expected
                );
            }
            eprintln!(
                "STRICT_PREFIX case={case} equal_values={width} directories={} decoded_directory_nodes=1 decoded_leaf_nodes=1 route_decode_us={} setup_us={}",
                root.directories.len(),
                route_elapsed.as_micros(),
                setup_elapsed.as_micros()
            );
        }
    }
}

#[test]
fn after_prefix_recent_cursor_does_not_consume_excluded_scale_ties() {
    let mut schema = Schema::new();
    for (attribute, value_type) in [(1_000, ValueType::BigDec), (1_001, ValueType::Long)] {
        let mut descriptor = Attribute::new(
            attribute,
            crate::Keyword::new("strict", format!("a{attribute}")),
            value_type,
            Cardinality::Many,
        );
        descriptor.indexed = true;
        schema.install(descriptor).unwrap();
    }
    for width in [32, 256, 1_024] {
        let mut tx_data = (0..width)
            .map(|ordinal| {
                datom(
                    ordinal as u64 + 1,
                    1_000,
                    decimal(if ordinal % 2 == 0 { "1" } else { "1.00" }),
                )
            })
            .collect::<Vec<_>>();
        tx_data.push(datom(width as u64 + 1, 1_000, decimal("2")));
        tx_data.push(datom(width as u64 + 2, 1_001, Value::Long(0)));
        tx_data.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        let base_hash = [0x53; 32];
        let transaction = DurableTransaction {
            database_id: "strict-recent".into(),
            basis_t: 1,
            previous_hash: base_hash,
            eidx_frontier: crate::INITIAL_EIDX_FRONTIER.max(width as u64 + 3),
            tempids: BTreeMap::new(),
            tx_data,
        };
        let tier = RecentTier::new(
            "strict-recent",
            0,
            base_hash,
            [transaction],
            EndpointProjection::new(schema.clone()),
            RecentLimits::default(),
        )
        .unwrap();
        for history in [false, true] {
            let mut cursor = tier.boundary_cursor(history, &strict(1_000, decimal("1.000")));
            let first = cursor.next().unwrap();
            assert_eq!(first.value, decimal("2"));
            assert!(cursor.stats().datoms_examined <= 3, "{:?}", cursor.stats());
            let second = cursor.next().unwrap();
            assert_eq!(second.attribute, 1_001);
            assert!(cursor.next().is_none());
            assert_eq!(cursor.stats().datoms_yielded, 2);
            eprintln!(
                "STRICT_RECENT equal_values={width} history={history} stats={:?}",
                cursor.stats()
            );
        }
    }
}
