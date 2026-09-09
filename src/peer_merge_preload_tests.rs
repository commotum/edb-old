//! Pure regressions for the exact native preloader/merge composition. A full
//! old node set can hide an omitted boundary read, so merge sees only bytes
//! admitted by select_merge_leaves, as the PostgreSQL native path does.
use super::*;
use crate::persistent_tree::{TreeDescriptor, TreeMerge, range_tree, validate_tree};
use crate::Value;

fn partial_merge(
    descriptor: &TreeDescriptor,
    all_nodes: &TreeNodeSet,
    edits: &TreeMergeEdits,
    config: &TreeConfig,
) -> Result<TreeMerge, SemanticError> {
    let root_bytes = all_nodes.get(&descriptor.root_hash).unwrap();
    let TreeNode::Root(root) = decode_tree_node(&descriptor.root_hash, root_bytes)? else {
        panic!("root expected");
    };
    let mut points = edits.removals.iter().chain(&edits.insertions).cloned().collect::<Vec<_>>();
    sort_dedup_datoms(&mut points, descriptor.order);
    let selected = select_merge_leaves(&root, &points, |reference| {
        let TreeNode::Directory(directory) = decode_tree_node(&reference.hash, all_nodes.get(&reference.hash).unwrap())? else {
            panic!("directory expected");
        };
        Ok(directory)
    })?;
    let mut partial = TreeNodeSet::default();
    partial.insert(root_bytes.to_vec())?;
    for index in selected.directories.keys() {
        partial.insert(all_nodes.get(&root.directories[*index].hash).unwrap().to_vec())?;
    }
    for (directory, leaf) in selected.leaves {
        let hash = selected.directories[&directory].leaves[leaf].hash;
        partial.insert(all_nodes.get(&hash).unwrap().to_vec())?;
    }
    merge_tree(descriptor, &partial, edits, config)
}

#[test]
fn native_preload_includes_interior_leaf_exposed_by_directory_split() {
    let config = TreeConfig {
        max_leaf_datoms: 2,
        max_leaves_per_directory: 8,
        ..TreeConfig::default()
    };
    let datoms = (0..64).map(|index| Datom {
        entity: crate::make_eid(crate::USER_PARTITION, index * 2 + 1).unwrap(),
        attribute: 1_000,
        value: Value::Long(index as i64),
        tx: crate::t_to_tx(1).unwrap(),
        added: true,
    }).collect::<Vec<_>>();
    let built = build_tree(IndexOrder::Eavt, false, datoms.clone(), &config).unwrap();
    let inserted = Datom { entity: crate::make_eid(crate::USER_PARTITION, 2).unwrap(), ..datoms[0].clone() };
    let edits = TreeMergeEdits { insertions: vec![inserted.clone()], ..TreeMergeEdits::default() };
    let full = merge_tree(&built.descriptor, &built.nodes, &edits, &config).unwrap();
    // Old directory [L0,L1,...,L7] becomes [L0a,L0b,L1,...,L6]
    // followed by [L7]. Root separator repair needs L6's final datom even
    // though L6 was neither edited nor adjacent to L0 nor an old endpoint.
    let partial = partial_merge(&built.descriptor, &built.nodes, &edits, &config).unwrap();
    assert_eq!(partial.descriptor, full.descriptor);
    let mut nodes = built.nodes;
    for (_, payload) in partial.new_nodes.iter() {
        nodes.insert(payload.to_vec()).unwrap();
    }
    validate_tree(&partial.descriptor, &nodes).unwrap();
    let mut expected = datoms;
    expected.push(inserted);
    expected.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
    assert_eq!(range_tree(&partial.descriptor, &nodes, None, None).unwrap().datoms, expected);
}
