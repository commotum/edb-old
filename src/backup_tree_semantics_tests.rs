//! A canonical manifest with an internally valid physical graph is not proof
//! that its datoms agree with the state label. Both storage adapters must use
//! the same replay comparison, including when no portable root hash exists.
use super::*;
use crate::{Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, Value, ValueType};

fn database(value: i64) -> Database {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("semantic-proof", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: 1_000,
                value: Value::Long(value).into(),
            }],
            1_000,
        )
        .unwrap()
        .db_after
}

fn tree(database: &Database) -> crate::peer::FullNativeTreeBuild {
    crate::peer::build_full_native_tree(
        "semantic-proof",
        1,
        1,
        [3; 32],
        checkpoint_state_hash(database).unwrap(),
        database,
    )
    .unwrap()
}

fn verify(
    built: &crate::peer::FullNativeTreeBuild,
    expected: &Database,
) -> Result<(), SemanticError> {
    verify_tree_projection_semantics(&built.manifest, expected, &mut |root| {
        read_validated_tree_with_loader(root, &mut |hash| {
            Ok(built.nodes.get(&hash).expect("full built closure").to_vec())
        })
        .map(|(datoms, _)| datoms)
    })
}

#[test]
fn coherent_wrong_tree_does_not_inherit_an_authoritative_state_label() {
    let expected = database(7);
    let alternative = database(8);
    let expected_tree = tree(&expected);
    let mut wrong_tree = tree(&alternative);
    assert_eq!(expected.basis_t(), alternative.basis_t());
    assert_eq!(expected.eidx_frontier(), alternative.eidx_frontier());
    verify(&expected_tree, &expected).unwrap();
    verify(&wrong_tree, &alternative).unwrap();

    // The coordinate envelope is plausible and every descendant hashes and
    // orders correctly. Only agreement with independently replayed facts
    // distinguishes it from the expected checkpoint.
    wrong_tree.manifest.state_hash = expected_tree.manifest.state_hash;
    let payload = wrong_tree.manifest.encode().unwrap();
    assert_eq!(
        PersistentTreeManifest::decode(&payload).unwrap().state_hash,
        expected_tree.manifest.state_hash
    );
    for root in &wrong_tree.manifest.trees {
        persistent_tree::validate_tree_streaming(&root.descriptor, |hash| {
            Ok(wrong_tree.nodes.get(hash).unwrap().to_vec())
        })
        .unwrap();
    }
    assert_eq!(
        verify(&wrong_tree, &expected).unwrap_err().code,
        "backup/tree-semantic-mismatch"
    );
}

#[test]
fn shared_tree_loader_checks_root_byte_binding_before_semantic_comparison() {
    let expected = database(7);
    let mut built = tree(&expected);
    for root in &mut built.manifest.trees {
        root.root_bytes += 1;
    }
    assert_eq!(
        verify(&built, &expected).unwrap_err().code,
        "backup/tree-root-bytes"
    );
}
