//! Pure regressions for the exact native preloader/merge composition. A full
//! old node set can hide an omitted boundary read, so merge sees only bytes
//! admitted by select_merge_leaves, as the PostgreSQL native path does.
use super::*;
use crate::Value;
use crate::persistent_tree::{
    TreeDescriptor, merge_tree, merge_tree_with_boundary_loader, range_tree, validate_tree,
};

fn preload_nodes(
    descriptor: &TreeDescriptor,
    all_nodes: &TreeNodeSet,
    edits: &TreeMergeEdits,
) -> Result<TreeNodeSet, SemanticError> {
    let root_bytes = all_nodes.get(&descriptor.root_hash).unwrap();
    let TreeNode::Root(root) = decode_tree_node(&descriptor.root_hash, root_bytes)? else {
        panic!("root expected");
    };
    let mut points = edits
        .removals
        .iter()
        .chain(&edits.insertions)
        .cloned()
        .collect::<Vec<_>>();
    sort_dedup_datoms(&mut points, descriptor.order);
    let selected = select_merge_leaves(&root, &points, |reference| {
        let TreeNode::Directory(directory) =
            decode_tree_node(&reference.hash, all_nodes.get(&reference.hash).unwrap())?
        else {
            panic!("directory expected");
        };
        Ok(directory)
    })?;
    let mut partial = TreeNodeSet::default();
    partial.insert_known(descriptor.root_hash, root_bytes.to_vec())?;
    for index in selected.directories.keys() {
        let hash = root.directories[*index].hash;
        partial.insert_known(hash, all_nodes.get(&hash).unwrap().to_vec())?;
    }
    for (directory, leaf) in selected.leaves {
        let hash = selected.directories[&directory].leaves[leaf].hash;
        partial.insert_known(hash, all_nodes.get(&hash).unwrap().to_vec())?;
    }
    Ok(partial)
}

#[test]
fn native_preload_includes_interior_leaf_exposed_by_directory_split() {
    let config = TreeConfig {
        max_leaf_datoms: 2,
        max_leaves_per_directory: 8,
        ..TreeConfig::default()
    };
    let datoms = (0..64)
        .map(|index| Datom {
            entity: crate::make_eid(crate::USER_PARTITION, index * 2 + 1).unwrap(),
            attribute: 1_000,
            value: Value::Ref(crate::make_eid(crate::USER_PARTITION, index * 2 + 1).unwrap()),
            tx: crate::t_to_tx(1).unwrap(),
            added: true,
        })
        .collect::<Vec<_>>();
    for order in all_index_orders() {
        for history in [false, true] {
            let built = build_tree(order, history, datoms.clone(), &config).unwrap();
            for directory_index in [0, 1, 3] {
                for leaf_index in [0, 2, 4] {
                    let position = directory_index * 16 + leaf_index * 2;
                    let entity = crate::make_eid(crate::USER_PARTITION, position * 2 + 2).unwrap();
                    let inserted = Datom {
                        entity,
                        value: Value::Ref(entity),
                        tx: crate::t_to_tx(2).unwrap(),
                        ..datoms[0].clone()
                    };
                    let edits = TreeMergeEdits {
                        insertions: vec![inserted.clone()],
                        ..TreeMergeEdits::default()
                    };
                    let full =
                        merge_tree(&built.descriptor, &built.nodes, &edits, &config).unwrap();
                    // Old directory [L0,L1,...,L7] becomes [L0a,L0b,L1,...,L6]
                    // followed by [L7]. Root separator repair needs L6's final
                    // datom, outside the original local preload selection.
                    let preloaded = preload_nodes(&built.descriptor, &built.nodes, &edits).unwrap();
                    assert_eq!(
                        merge_tree(&built.descriptor, &preloaded, &edits, &config)
                            .unwrap_err()
                            .code,
                        "tree/missing-node",
                        "strict old preloading must reproduce the actual omission"
                    );
                    let mut fetched = Vec::new();
                    let partial = merge_tree_with_boundary_loader(
                        &built.descriptor,
                        &preloaded,
                        &edits,
                        &config,
                        &mut |hash| {
                            fetched.push(*hash);
                            Ok(built
                                .nodes
                                .get(hash)
                                .expect("the node exists in durable content")
                                .to_vec())
                        },
                    )
                    .unwrap();
                    assert_eq!(
                        fetched.len(),
                        1,
                        "{order:?}, history={history}, directory={directory_index}, leaf={leaf_index}"
                    );
                    assert!(preloaded.get(&fetched[0]).is_none());
                    assert!(
                        !partial.retired_nodes.contains(&fetched[0]),
                        "boundary reads confer no retirement ownership"
                    );
                    assert!(
                        partial.new_nodes.get(&fetched[0]).is_none(),
                        "old boundary content is not a new upload"
                    );
                    assert_eq!(partial.descriptor, full.descriptor);
                    assert_eq!(
                        partial.new_nodes.iter().collect::<Vec<_>>(),
                        full.new_nodes.iter().collect::<Vec<_>>()
                    );
                    assert_eq!(partial.retired_nodes, full.retired_nodes);
                    let mut nodes = built.nodes.clone();
                    for (hash, payload) in partial.new_nodes.iter() {
                        nodes.insert_known(*hash, payload.to_vec()).unwrap();
                    }
                    validate_tree(&partial.descriptor, &nodes).unwrap();
                    let mut expected = datoms.clone();
                    expected.push(inserted);
                    expected.sort_by(|left, right| left.cmp_in(right, order));
                    assert_eq!(
                        range_tree(&partial.descriptor, &nodes, None, None)
                            .unwrap()
                            .datoms,
                        expected
                    );

                    // A fallback is an authenticated storage read, never a
                    // license to invent or silently omit unavailable bytes.
                    let absent = merge_tree_with_boundary_loader(
                        &built.descriptor,
                        &preloaded,
                        &edits,
                        &config,
                        &mut |_| {
                            Err(SemanticError::new(
                                ErrorCategory::Fault,
                                "test/missing-boundary",
                                "deliberate missing immutable node",
                            ))
                        },
                    )
                    .unwrap_err();
                    assert_eq!(absent.code, "test/missing-boundary");
                    let corrupt = merge_tree_with_boundary_loader(
                        &built.descriptor,
                        &preloaded,
                        &edits,
                        &config,
                        &mut |_| Ok(vec![0]),
                    )
                    .unwrap_err();
                    assert_eq!(corrupt.code, "tree/content-hash-mismatch");
                }
            }
        }
    }
}

#[test]
fn boundary_retractions_preserve_locality_and_exact_retirement_ownership() {
    let config = TreeConfig {
        max_leaf_datoms: 2,
        max_leaves_per_directory: 8,
        ..TreeConfig::default()
    };
    let datoms = (0..64)
        .map(|index| {
            let entity = crate::make_eid(crate::USER_PARTITION, index + 1).unwrap();
            Datom {
                entity,
                attribute: 1_000,
                value: Value::Ref(entity),
                tx: crate::t_to_tx(1).unwrap(),
                added: true,
            }
        })
        .collect::<Vec<_>>();
    for order in all_index_orders() {
        let built = build_tree(order, false, datoms.clone(), &config).unwrap();
        for directory in 0..4 {
            for (start, count) in [(0, 2), (14, 2), (0, 16)] {
                let range = directory * 16 + start..directory * 16 + start + count;
                let edits = TreeMergeEdits {
                    removals: datoms[range.clone()].to_vec(),
                    ..TreeMergeEdits::default()
                };
                let full = merge_tree(&built.descriptor, &built.nodes, &edits, &config).unwrap();
                let preloaded = preload_nodes(&built.descriptor, &built.nodes, &edits).unwrap();
                let partial = merge_tree_with_boundary_loader(&built.descriptor, &preloaded, &edits, &config, &mut |_| {
                    panic!("endpoint/directory retraction already has its complete boundary witnesses")
                }).unwrap();
                assert_eq!(partial.descriptor, full.descriptor);
                assert_eq!(partial.retired_nodes, full.retired_nodes);
                assert_eq!(
                    partial.new_nodes.iter().collect::<Vec<_>>(),
                    full.new_nodes.iter().collect::<Vec<_>>()
                );
                let mut nodes = built.nodes.clone();
                for (hash, payload) in partial.new_nodes.iter() {
                    nodes.insert_known(*hash, payload.to_vec()).unwrap();
                }
                validate_tree(&partial.descriptor, &nodes).unwrap();
                let expected = datoms
                    .iter()
                    .enumerate()
                    .filter(|(index, _)| !range.contains(index))
                    .map(|(_, datom)| datom.clone())
                    .collect::<Vec<_>>();
                assert_eq!(
                    range_tree(&partial.descriptor, &nodes, None, None)
                        .unwrap()
                        .datoms,
                    expected
                );
            }
        }
    }
}
