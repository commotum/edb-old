use super::*;
use crate::backup::repository;

fn put(directory: &Path, bytes: &[u8]) -> ObjectId {
    repository::put_object(directory, bytes).unwrap().0
}
fn genesis_point(directory: &Path, wrong_tree: bool, wrong_frontier: bool) -> ReadPoint {
    repository::admit(directory, true).unwrap();
    let identity = [7; 16];
    repository::claim(directory, identity).unwrap();
    let database = Database::bootstrap().unwrap();
    let mut trees = Vec::new();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let built = crate::index::tree::build_tree(
                order,
                history,
                database.datoms(
                    if history {
                        View::History
                    } else {
                        View::Current
                    },
                    order,
                ),
                &Default::default(),
            )
            .unwrap();
            for (_, bytes) in built.nodes.iter() {
                put(directory, bytes);
            }
            trees.push(built.descriptor);
        }
    }
    let mut index = IndexDescriptor {
        identity,
        basis: 0,
        generation: 0,
        trees,
        pending_avet: vec![],
        avet_work: vec![],
        fulltext: None,
    };
    let metadata = SnapshotMetadata {
        identity,
        basis: 0,
        generation: 0,
        eidx_frontier: database.eidx_frontier() + u64::from(wrong_frontier),
        reserved_frontier: database.reserved_allocation().unwrap().frontier(),
        last_tx_instant: None,
        excision: None,
    };
    let mut publication = DatabaseRoot {
        identity,
        basis: 0,
        writer_epoch: 0,
        log: None,
        indexes: Some(put(directory, &index.encode().unwrap())),
        receipts: None,
        metadata: Some(put(directory, &metadata.encode().unwrap())),
        read_authorization: None,
    };
    // Encode the current one-entry immutable registry without creating a
    // live store merely for a file-only verification fixture.
    let initial = DatabaseValueRoot::from(&publication);
    let initial_id = put(directory, &initial.encode().unwrap());
    let index_id = publication.indexes.unwrap();
    let mut witness_payload = identity.to_vec();
    witness_payload.extend_from_slice(&1u64.to_be_bytes());
    let witness = put(
        directory,
        &Block {
            kind: crate::storage::read_authorization::INDEX_AUTHORIZATION_KIND,
            links: vec![index_id],
            payload: witness_payload,
        }
        .encode()
        .unwrap(),
    );
    let registry = put(
        directory,
        &Block {
            kind: crate::storage::receipts::REQUEST_LEAF_KIND,
            links: vec![witness],
            payload: crate::storage::read_authorization::index_key(&identity, index_id).to_vec(),
        }
        .encode()
        .unwrap(),
    );
    let mut authorization_payload = identity.to_vec();
    authorization_payload.extend_from_slice(&0u64.to_be_bytes());
    publication.read_authorization = Some(put(
        directory,
        &Block {
            kind: crate::storage::read_authorization::READ_AUTHORIZATION_KIND,
            links: vec![initial_id, registry],
            payload: authorization_payload,
        }
        .encode()
        .unwrap(),
    ));
    if wrong_tree {
        // Corrupt a covering projection that is not used to derive schema
        // during ordinary open; the deep semantic comparator must catch it.
        let mut missing = database.datoms(View::Current, IndexOrder::Vaet);
        assert!(missing.pop().is_some());
        let built =
            crate::index::tree::build_tree(IndexOrder::Vaet, false, missing, &Default::default())
                .unwrap();
        for (_, bytes) in built.nodes.iter() {
            put(directory, bytes);
        }
        index.trees[3] = built.descriptor;
        publication.indexes = Some(put(directory, &index.encode().unwrap()));
    }
    let publication_id = put(directory, &publication.encode().unwrap());
    let mut payload = identity.to_vec();
    payload.extend_from_slice(&0u64.to_be_bytes());
    payload.extend_from_slice(&0u64.to_be_bytes());
    let manifest = Block {
        kind: crate::backup::POINT_KIND,
        links: vec![publication_id],
        payload,
    }
    .encode()
    .unwrap();
    put(directory, &manifest);
    repository::publish_point(directory, 0, 0, &manifest).unwrap();
    crate::backup::load_selected(directory, Some(0), 0).unwrap()
}

#[test]
fn deep_repository_verification_rejects_hash_valid_wrong_tree_and_frontier() {
    for (wrong_tree, wrong_frontier, code) in [
        (false, false, None),
        (true, false, Some("backup/tree-semantic-mismatch")),
        (false, true, Some("backup/metadata-semantic-mismatch")),
    ] {
        let temporary = tempfile::tempdir().unwrap();
        let directory = temporary.path().join("repository");
        let point = genesis_point(&directory, wrong_tree, wrong_frontier);
        assert!(
            walk_repository(
                &directory,
                &[point.point.manifest_hash],
                &Default::default(),
                false
            )
            .unwrap()
                > 0
        );
        let result = verify_read_point(&directory, point, true, &Default::default());
        match code {
            Some(code) => assert_eq!(result.unwrap_err().code, code),
            None => assert_eq!(result.unwrap().database.basis_t(), 0),
        }
    }
}

#[test]
fn repository_walk_checks_program_edge_type_and_cancellation() {
    let temporary = tempfile::tempdir().unwrap();
    let directory = temporary.path().join("repository");
    let point = genesis_point(&directory, false, false);
    let invalid = crate::Datom {
        entity: crate::t_to_tx(1).unwrap(),
        attribute: crate::DB_FN as u32,
        value: crate::Value::Function(point.publication),
        tx: crate::t_to_tx(1).unwrap(),
        added: true,
    };
    let tree =
        crate::index::tree::build_tree(IndexOrder::Eavt, false, vec![invalid], &Default::default())
            .unwrap();
    for (_, bytes) in tree.nodes.iter() {
        put(&directory, bytes);
    }
    assert!(
        walk_repository(
            &directory,
            &[tree.descriptor.root_hash],
            &Default::default(),
            false
        )
        .is_err()
    );
    let control = MaintenanceControl::default();
    control.cancel();
    assert_eq!(
        walk_repository(&directory, &[point.point.manifest_hash], &control, true)
            .unwrap_err()
            .category,
        ErrorCategory::Interrupted
    );
}

fn transaction_object(directory: &Path, report: &crate::TxReport) -> ObjectId {
    let mut datoms = report.tx_data.iter().collect::<Vec<_>>();
    datoms.sort_by(|a, b| a.cmp_in(b, IndexOrder::Eavt));
    let mut content = Vec::new();
    for datom in datoms {
        content.extend_from_slice(&crate::encoding::canonical_datom_bytes(datom).unwrap());
    }
    let mut payload = Vec::new();
    for number in [
        report.db_after.basis_t(),
        report.db_after.eidx_frontier(),
        report.db_after.reserved_allocation().unwrap().frontier(),
        report.tx_data.len() as u64,
        content.len() as u64,
    ] {
        payload.extend_from_slice(&number.to_be_bytes());
    }
    payload.extend_from_slice(&content);
    put(
        directory,
        &Block {
            kind: crate::storage::log::LOG_ENTRY_KIND,
            links: vec![],
            payload,
        }
        .encode()
        .unwrap(),
    )
}
fn short_log(directory: &Path, entries: Vec<ObjectId>, database: &Database) -> ObjectId {
    let mut payload = Vec::new();
    for number in [
        0,
        database.eidx_frontier(),
        database.reserved_allocation().unwrap().frontier(),
    ] {
        payload.extend_from_slice(&number.to_be_bytes());
    }
    payload.extend_from_slice(&(entries.len() as u16).to_be_bytes());
    payload.extend_from_slice(&[0, 0]);
    put(
        directory,
        &Block {
            kind: crate::storage::log::LOG_PAGE_KIND,
            links: entries,
            payload,
        }
        .encode()
        .unwrap(),
    )
}
fn retained_value(
    directory: &Path,
    database: &Database,
    log: ObjectId,
    wrong: bool,
) -> DatabaseValueRoot {
    let mut trees = Vec::new();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let mut datoms = database.datoms(
                if history {
                    View::History
                } else {
                    View::Current
                },
                order,
            );
            if wrong && !history && order == IndexOrder::Eavt {
                datoms.pop();
            }
            let tree = crate::index::tree::build_tree(order, history, datoms, &Default::default())
                .unwrap();
            for (_, bytes) in tree.nodes.iter() {
                put(directory, bytes);
            }
            trees.push(tree.descriptor);
        }
    }
    let indexes = IndexDescriptor {
        identity: [7; 16],
        basis: database.basis_t(),
        generation: 0,
        trees,
        pending_avet: vec![],
        avet_work: vec![],
        fulltext: None,
    };
    let metadata = SnapshotMetadata {
        identity: [7; 16],
        basis: database.basis_t(),
        generation: 0,
        eidx_frontier: database.eidx_frontier(),
        reserved_frontier: database.reserved_allocation().unwrap().frontier(),
        last_tx_instant: database.last_tx_instant(),
        excision: None,
    };
    DatabaseValueRoot {
        identity: [7; 16],
        basis: database.basis_t(),
        log: Some(log),
        indexes: Some(put(directory, &indexes.encode().unwrap())),
        metadata: Some(put(directory, &metadata.encode().unwrap())),
    }
}

#[test]
fn deep_verification_checks_earlier_receipt_trees_and_map_coordinates() {
    for (wrong_before_tree, wrong_map, wrong_before_basis, error) in [
        (false, false, false, None),
        (true, false, false, Some("backup/tree-semantic-mismatch")),
        (false, true, false, Some("backup/receipt-address")),
        (false, false, true, Some("backup/receipt-coordinate")),
    ] {
        let temporary = tempfile::tempdir().unwrap();
        let directory = temporary.path().join("repository");
        let genesis = genesis_point(&directory, false, false);
        let original = load_publication(&directory, &genesis).unwrap();
        let first = Database::bootstrap().unwrap().with(&[], 10).unwrap();
        let second = first.db_after.with(&[], 20).unwrap();
        let one = transaction_object(&directory, &first);
        let two = transaction_object(&directory, &second);
        let log_one = short_log(&directory, vec![one], &first.db_after);
        let log_two = short_log(&directory, vec![one, two], &second.db_after);
        let before = if wrong_before_basis {
            genesis.value.clone()
        } else {
            retained_value(&directory, &first.db_after, log_one, wrong_before_tree)
        };
        let after = retained_value(&directory, &second.db_after, log_two, false);
        let before_id = put(&directory, &before.encode().unwrap());
        let after_id = put(&directory, &after.encode().unwrap());
        let mut payload = [7; 16].to_vec();
        payload.extend_from_slice(&[5; 32]);
        payload.extend_from_slice(&2u64.to_be_bytes());
        payload.extend_from_slice(&4u64.to_be_bytes());
        payload.extend_from_slice(&0u32.to_be_bytes());
        let receipt = put(
            &directory,
            &Block {
                kind: RECEIPT_KIND,
                links: vec![before_id, after_id, two],
                payload,
            }
            .encode()
            .unwrap(),
        );
        let key = basis_receipt_key(&[7; 16], if wrong_map { 3 } else { 2 });
        let receipts = put(
            &directory,
            &Block {
                kind: crate::storage::receipts::REQUEST_LEAF_KIND,
                links: vec![receipt],
                payload: key.to_vec(),
            }
            .encode()
            .unwrap(),
        );
        let publication = DatabaseRoot {
            identity: [7; 16],
            basis: 2,
            writer_epoch: 0,
            log: after.log,
            indexes: after.indexes,
            receipts: Some(receipts),
            metadata: after.metadata,
            read_authorization: original.read_authorization,
        };
        let publication_id = put(&directory, &publication.encode().unwrap());
        let mut payload = [7; 16].to_vec();
        payload.extend_from_slice(&0u64.to_be_bytes());
        payload.extend_from_slice(&2u64.to_be_bytes());
        let manifest = Block {
            kind: crate::backup::POINT_KIND,
            links: vec![publication_id],
            payload,
        }
        .encode()
        .unwrap();
        put(&directory, &manifest);
        repository::publish_point(&directory, 0, 2, &manifest).unwrap();
        let point = crate::backup::load_selected(&directory, Some(0), 2).unwrap();
        let result = verify_read_point(&directory, point, true, &Default::default());
        match error {
            None => assert_eq!(result.unwrap().database.basis_t(), 2),
            Some(code) => assert_eq!(result.unwrap_err().code, code),
        }
    }
}

#[test]
fn pending_avet_requires_real_history_and_the_exact_copy_prefix() {
    use crate::{Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, Value, ValueType};
    let temporary = tempfile::tempdir().unwrap();
    let directory = temporary.path().join("repository");
    repository::admit(&directory, true).unwrap();
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        1000,
        Keyword::new("pending", "value"),
        ValueType::Long,
        Cardinality::One,
    );
    attribute.indexed = true;
    schema.install(attribute).unwrap();
    let database = Database::new(schema)
        .unwrap()
        .with(
            &(0..3)
                .map(|n| TxOp::Add {
                    entity: EntityRef::Temp(format!("e{n}")),
                    attribute: 1000,
                    value: Value::Long(n).into(),
                })
                .collect::<Vec<_>>(),
            1000,
        )
        .unwrap()
        .db_after;
    let value = retained_value(&directory, &database, [1; 32], false);
    let id = value.indexes.unwrap();
    let original = IndexDescriptor::decode(&id, &read_object(&directory, id).unwrap()).unwrap();
    let control = MaintenanceControl::default();
    let mut reader = Reader {
        directory: &directory,
        control: &control,
    };
    let replayed = database.datoms(View::History, IndexOrder::Eavt);
    for history in [false, true] {
        let mut index = original.clone();
        let source = index.trees[usize::from(history) * 4 + 1].clone();
        let mut position = None;
        let TreeNode::Root(root) = crate::index::tree::decode_tree_node(
            &source.root_hash,
            &reader.read_object(source.root_hash).unwrap(),
        )
        .unwrap() else {
            unreachable!()
        };
        for (directory_index, child) in root.directories.iter().enumerate() {
            let TreeNode::Directory(dir) = crate::index::tree::decode_tree_node(
                &child.hash,
                &reader.read_object(child.hash).unwrap(),
            )
            .unwrap() else {
                unreachable!()
            };
            for (leaf_index, child) in dir.leaves.iter().enumerate() {
                let TreeNode::Leaf(leaf) = crate::index::tree::decode_tree_node(
                    &child.hash,
                    &reader.read_object(child.hash).unwrap(),
                )
                .unwrap() else {
                    unreachable!()
                };
                for slot in 0..leaf.len() {
                    let datom = leaf.datom(slot).unwrap();
                    if datom.attribute == 1000 && datom.value == Value::Long(1) {
                        position = Some((directory_index as u32, leaf_index as u32, slot as u32));
                    }
                }
            }
        }
        let (directory_index, leaf, slot) = position.unwrap();
        index.pending_avet = vec![1000];
        index.avet_work = vec![crate::storage::BlockAvetWork {
            attribute: 1000,
            adding: true,
            history,
            clearing: true,
            directory: directory_index,
            leaf,
            slot,
            source,
        }];
        let actual = read_tree(&mut reader, &index.trees[usize::from(history) * 4 + 2]).unwrap();
        verify_pending(&mut reader, &index, &database, &actual, &replayed, history).unwrap();
        let mut forged = actual.clone();
        forged
            .iter_mut()
            .find(|d| d.attribute == 1000)
            .unwrap()
            .value = Value::Long(99);
        assert_eq!(
            verify_pending(&mut reader, &index, &database, &forged, &replayed, history)
                .unwrap_err()
                .code,
            "backup/avet-fabricated"
        );
        index.avet_work[0].clearing = false;
        let prefix = projection_prefix(&mut reader, &index.avet_work[0]).unwrap();
        assert_eq!(prefix.len(), 1);
        verify_pending(&mut reader, &index, &database, &prefix, &replayed, history).unwrap();
        index.avet_work[0].slot = 0;
        assert_eq!(
            verify_pending(&mut reader, &index, &database, &prefix, &replayed, history)
                .unwrap_err()
                .code,
            "backup/avet-prefix"
        );
    }
}
