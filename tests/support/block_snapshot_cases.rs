//! Exact source semantics across the indexed base/recent-tail boundary.
use super::*;
use atomic_core::AttributeName;
use atomic_core::persistent_tree::{TreeNode, decode_tree_node};

#[test]
fn tail_schema_enablement_keeps_avet_pending_until_a_new_index_is_captured() {
    let Some((_fixture, config)) = fixture("block_pending_avet") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let mut reports = reports(24, 4);
    let (old_index, _, _) = upload_index(&mut store, &reports[1].db_after);
    let original_log = upload_log(&mut store, &reports);
    publish(&mut store, &reports[1].db_after, &original_log, old_index);
    let before = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let before_value = before.database_value();
    assert!(!before_value.schema().attribute(NAME).unwrap().indexed);

    let mut attribute = reports[1]
        .db_after
        .schema()
        .attribute(NAME)
        .unwrap()
        .clone();
    attribute.indexed = true;
    let enabled = reports[1]
        .db_after
        .with(&[TxOp::AlterAttribute(attribute)], 30)
        .unwrap();
    reports.push(enabled);
    let log = upload_log(&mut store, &reports);
    let expected = &reports[2].db_after;
    publish(&mut store, expected, &log, old_index);
    let pending = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let value = pending.database_value();
    assert!(value.schema().attribute(NAME).unwrap().indexed);
    assert!(!value.has_avet(&AttributeName::Id(NAME)).unwrap());
    let error = value
        .avet_boundary(IndexComponents::One(AttributeName::Id(NAME)))
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Unavailable);
    assert_eq!(error.code, "index/avet-not-ready");
    assert!(
        value
            .datoms(IndexOrder::Avet)
            .unwrap()
            .iter()
            .all(|d| d.attribute != NAME),
        "an incomplete projection must not expose a partial AVET answer"
    );
    // Datalog still answers through an available index, including a constant
    // predicate on the newly indexed attribute's preexisting base information.
    let text = "[:find ?e :where [?e :person/name \"Person 7 xxxx\"]]";
    assert_eq!(
        query(&value, text, &[]),
        query(&expected.database_value(), text, &[])
    );
    assert_eq!(
        name_query(&value, 7),
        name_query(&expected.database_value(), 7)
    );

    // A physical publication may change readiness at the same logical basis.
    let (ready_index, _, _) = upload_index(&mut store, expected);
    publish(&mut store, expected, &log, ready_index);
    let ready = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let ready_value = ready.database_value();
    assert_eq!(ready_value.basis_t(), value.basis_t());
    assert!(ready_value.has_avet(&AttributeName::Id(NAME)).unwrap());
    assert_eq!(
        ready_value.datoms(IndexOrder::Avet).unwrap(),
        expected.datoms(View::Current, IndexOrder::Avet)
    );
    assert!(!value.has_avet(&AttributeName::Id(NAME)).unwrap());
    assert!(!before_value.schema().attribute(NAME).unwrap().indexed);
    drop((before_value, value, ready_value));
    before.release().unwrap();
    pending.release().unwrap();
    ready.release().unwrap();
}

#[test]
fn no_history_is_not_read_time_erasure_and_reverse_tail_seeks_match_the_oracle() {
    let Some((_fixture, config)) = fixture("block_no_history") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let mut reports = reports(24, 4);
    let entity = reports[1].tempids["person-7"];
    let old_name = Value::String("Person 7 xxxx".into());
    let (index, _, _) = upload_index(&mut store, &reports[1].db_after);
    let mut attribute = reports[1]
        .db_after
        .schema()
        .attribute(NAME)
        .unwrap()
        .clone();
    attribute.no_history = true;
    let changed = reports[1]
        .db_after
        .with(
            &[
                TxOp::AlterAttribute(attribute),
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: NAME,
                    value: Value::String("Intermediate".into()).into(),
                },
            ],
            30,
        )
        .unwrap();
    reports.push(changed);
    let changed_again = reports[2]
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: NAME,
                value: Value::String("Final".into()).into(),
            }],
            40,
        )
        .unwrap();
    reports.push(changed_again);
    let log = upload_log(&mut store, &reports);
    let expected = reports[3].db_after.database_value();
    publish(&mut store, &reports[3].db_after, &log, index);
    let snapshot = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let value = snapshot.database_value();
    assert!(value.schema().attribute(NAME).unwrap().no_history);
    let history = value.clone().history().datoms(IndexOrder::Eavt).unwrap();
    assert!(
        history
            .iter()
            .any(|d| d.entity == entity && d.attribute == NAME && d.value == old_name && d.added)
    );
    assert!(
        history
            .iter()
            .any(|d| d.entity == entity && d.attribute == NAME && d.value == old_name && !d.added)
    );
    for (actual, oracle) in [
        (value.clone(), expected.clone()),
        (value.clone().history(), expected.clone().history()),
        (value.clone().as_of(2), expected.clone().as_of(2)),
        (value.clone().since(2), expected.clone().since(2)),
    ] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(actual.datoms(order).unwrap(), oracle.datoms(order).unwrap());
        }
        for boundary in [
            IndexBoundary::Eavt(IndexComponents::Two(entity, NAME)),
            IndexBoundary::Eavt(IndexComponents::Four(
                entity,
                NAME,
                old_name.clone(),
                atomic_core::IndexTransaction::Tx(atomic_core::t_to_tx(3).unwrap()),
            )),
            IndexBoundary::Aevt(IndexComponents::Two(NAME, entity)),
        ] {
            let actual = actual
                .reverse_seek_cursor(&boundary)
                .unwrap()
                .take(12)
                .collect::<Result<Vec<_>, _>>()
                .unwrap();
            let oracle = oracle
                .reverse_seek_cursor(&boundary)
                .unwrap()
                .take(12)
                .collect::<Result<Vec<_>, _>>()
                .unwrap();
            assert_eq!(actual, oracle);
        }
    }
    drop(value);
    snapshot.release().unwrap();
}

#[test]
fn cold_missing_or_corrupt_leaf_returns_a_typed_error_and_stops_the_cursor() {
    let Some((fixture, config)) = fixture("block_leaf_fault") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reports = reports(24, 4);
    let log = upload_log(&mut store, &reports);
    let (index, _, _) = upload_index(&mut store, &reports[1].db_after);
    publish(&mut store, &reports[1].db_after, &log, index);
    let descriptor = IndexDescriptor::decode(&index, &store.get(index).unwrap().unwrap()).unwrap();
    let tree = descriptor
        .trees
        .iter()
        .find(|tree| !tree.history && tree.order == IndexOrder::Avet)
        .unwrap();
    let root = decode_tree_node(
        &tree.root_hash,
        &store.get(tree.root_hash).unwrap().unwrap(),
    )
    .unwrap();
    let TreeNode::Root(root) = root else {
        panic!("fixture root kind")
    };
    let directory_id = root.directories[0].hash;
    let directory =
        decode_tree_node(&directory_id, &store.get(directory_id).unwrap().unwrap()).unwrap();
    let TreeNode::Directory(directory) = directory else {
        panic!("fixture directory kind")
    };
    let leaf_id = directory.leaves[0].hash;
    let original = store.get(leaf_id).unwrap().unwrap();
    let mut admin = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    for corrupt in [false, true] {
        let snapshot = open_snapshot(
            &config,
            ROOT,
            BlockReadConfig {
                cache_entries: 0,
                cache_bytes: 0,
                ..BlockReadConfig::default()
            },
        )
        .unwrap();
        let value = snapshot.database_value();
        if corrupt {
            let mut bytes = original.clone();
            *bytes.last_mut().unwrap() ^= 1;
            assert_eq!(
                admin
                    .execute(
                        "UPDATE atomic_objects SET payload=$1 WHERE id=$2",
                        &[&bytes, &&leaf_id[..]]
                    )
                    .unwrap(),
                1
            );
        } else {
            assert_eq!(store.remove_objects(&[leaf_id]).unwrap(), 1);
        }
        let mut cursor = value.scan_cursor(IndexOrder::Avet).unwrap();
        let error = cursor
            .by_ref()
            .find_map(Result::err)
            .expect("cold leaf load must fail");
        assert_eq!(error.category, ErrorCategory::Fault);
        assert_eq!(
            error.code,
            if corrupt {
                "storage/object-corrupt"
            } else {
                "storage/missing-object"
            }
        );
        assert!(
            cursor.next().is_none(),
            "failed cursor must not resume or panic"
        );
        drop(cursor);
        drop(value);
        snapshot.release().unwrap();
        if corrupt {
            assert_eq!(
                admin
                    .execute(
                        "UPDATE atomic_objects SET payload=$1 WHERE id=$2",
                        &[&original, &&leaf_id[..]]
                    )
                    .unwrap(),
                1
            );
        } else {
            assert_eq!(store.put(&original).unwrap(), leaf_id);
        }
    }
}
