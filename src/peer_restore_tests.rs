//! Restore-specific admission and initial output ownership. The configured
//! backup test exercises bounded incremental and multi-step AVET successors.
use super::*;

#[test]
fn restore_builder_requires_exact_genesis_not_a_later_eager_database() {
    let genesis = Database::bootstrap().unwrap();
    let hash = sha256(&encode_genesis(genesis.genesis_datoms()).unwrap());
    let later = genesis.with(&[], 1_000).unwrap().db_after;
    for (candidate, claimed_hash) in [(&genesis, [9; 32]), (&later, hash)] {
        let error = RestoreTreeBuilder::new("restore-admission", 1, claimed_hash, candidate, 1)
            .err()
            .expect("restore must not turn a whole later database into its initial scan");
        assert_eq!(error.code, "backup/restore-tree-genesis");
    }
    assert_eq!(genesis.basis_t(), 0);
}

#[test]
fn restore_builder_initial_output_is_complete_and_requires_durable_acknowledgement() {
    let genesis = Database::bootstrap().unwrap();
    let hash = sha256(&encode_genesis(genesis.genesis_datoms()).unwrap());
    let (builder, built) =
        RestoreTreeBuilder::new("restore-output", 1, hash, &genesis, 41).unwrap();
    assert_eq!(built.manifest.basis_t, 0);
    assert_eq!(built.manifest.index_basis_t, 0);
    assert_eq!(built.manifest.tx_hash, hash);
    assert_eq!(built.manifest.publication_revision, 41);
    assert_eq!(built.manifest.trees.len(), 8);
    assert!(!builder.has_pending_projection());
    assert_eq!(
        builder.ready_for_step().unwrap_err().code,
        "backup/restore-tree-upload-pending"
    );
    for tree in &built.manifest.trees {
        crate::persistent_tree::validate_tree_streaming(&tree.descriptor, |hash| {
            Ok(built
                .nodes
                .get(hash)
                .expect("initial node closure")
                .to_vec())
        })
        .unwrap();
        let view = if tree.descriptor.history {
            View::History
        } else {
            View::Current
        };
        assert_eq!(
            tree.descriptor.count,
            genesis.datoms(view, tree.descriptor.order).len() as u64
        );
    }
    let stats = builder.stats();
    assert_eq!(stats.steps, 1);
    assert_eq!(stats.projection_steps, 0);
    assert_eq!(stats.tail_transactions, 0);
    assert_eq!(stats.tail_datoms, 0);
    assert_eq!(stats.node_outputs, built.nodes.len() as u64);
    assert!(stats.encoded_bytes > 0);
}
