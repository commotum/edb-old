use atomic_core::persistent_tree::{TreeConfig, build_tree};
use atomic_core::{
    Attribute, CapacityLimits, Cardinality, EntityRef, ErrorCategory, IndexOrder, Keyword,
    ManifestTree, PersistentTreeManifest, PostgresIndexer, PostgresOperator, PostgresStore,
    PostgresTreeStore, Schema, TreeManifestRecord, TreePublicationDelta, TreePublishOutcome,
    TreeRootBinding, TxOp, TxValue, USER_PARTITION, Value, ValueType, View, make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::collections::BTreeMap;
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

const ITEM_VALUE: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_VALUE,
            Keyword::new("item", "value"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn indexed_schema() -> Schema {
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        ITEM_VALUE,
        Keyword::new("item", "value"),
        ValueType::String,
        Cardinality::One,
    );
    attribute.indexed = true;
    schema.install(attribute).unwrap();
    schema
}

fn add(value: &str) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(user(42)),
        attribute: ITEM_VALUE,
        value: TxValue::Scalar(Value::String(value.into())),
    }
}

fn replace_immutable_transaction_content(client: &mut Client, content_hash: &[u8], payload: &[u8]) {
    // Corruption tests must bypass the immutable-value guard, but must never
    // leave that guard disabled for another session. PostgreSQL makes ALTER
    // TABLE transactional, so the payload replacement and trigger restoration
    // become visible together or not at all.
    let mut transaction = client.transaction().unwrap();
    transaction
        .batch_execute(
            "ALTER TABLE atomic_transaction_contents \
             DISABLE TRIGGER atomic_transaction_contents_immutable",
        )
        .unwrap();
    assert_eq!(
        transaction
            .execute(
                "UPDATE atomic_transaction_contents SET payload = $2 \
                 WHERE content_hash = $1",
                &[&content_hash, &payload],
            )
            .unwrap(),
        1
    );
    transaction
        .batch_execute(
            "ALTER TABLE atomic_transaction_contents \
             ENABLE TRIGGER atomic_transaction_contents_immutable",
        )
        .unwrap();
    transaction.commit().unwrap();
}

#[test]
fn inspector_crosschecks_authoritative_and_derived_state_and_metrics() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("inspect");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let first = common::transact(&service, "one", created.basis_t(), &[add("one")], 1_000);
    let second = common::transact(&service, "two", first.basis_t, &[add("two")], 2_000);
    service.shutdown();
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let report = operator.inspect_database(&database_id, true).unwrap();
    assert!(report.healthy(), "{:?}", report.problems);
    assert_eq!(report.metrics.basis_t, second.basis_t);
    assert_eq!(report.metrics.transactions, second.basis_t);
    assert_eq!(report.metrics.requests, second.basis_t);
    assert_eq!(report.metrics.index_lag, 0);
    assert!(report.metrics.history_datoms >= 4);
    assert!(report.metrics.transaction_bytes > 0);
    assert!(report.metrics.tree_publications > 0);
    assert!(report.metrics.tree_nodes > 0);

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let corrupt_basis = i64::try_from(first.basis_t).unwrap();
    let authoritative = client
        .query_one(
            "SELECT h.log_generation, t.content_hash, c.payload, \
                    (SELECT count(*) FROM atomic_generation_transactions referenced \
                      WHERE referenced.content_hash = t.content_hash) \
               FROM atomic_heads h \
               JOIN atomic_generation_transactions t \
                 ON t.database_id = h.database_id \
                AND t.generation = h.log_generation AND t.basis_t = $2 \
               JOIN atomic_transaction_contents c ON c.content_hash = t.content_hash \
              WHERE h.database_id = $1 AND h.log_generation > 0",
            &[&database_id, &corrupt_basis],
        )
        .unwrap();
    let generation: i64 = authoritative.get(0);
    let content_hash: Vec<u8> = authoritative.get(1);
    let original: Vec<u8> = authoritative.get(2);
    let references: i64 = authoritative.get(3);
    assert!(
        generation > 0,
        "the current log must use native generations"
    );
    assert_eq!(
        references, 1,
        "the test must not corrupt ATLC content shared by another generation"
    );
    let mut damaged = original.clone();
    damaged[16] ^= 1;
    replace_immutable_transaction_content(&mut client, &content_hash, &damaged);

    // Capture the result, restore the immutable value, and prove recovery
    // before asserting on the expected failure. A changed diagnostic cannot
    // strand corrupt shared storage in the test cluster.
    let corrupt = operator.inspect_database(&database_id, true);
    replace_immutable_transaction_content(&mut client, &content_hash, &original);
    let restored = operator.inspect_database(&database_id, true);

    let corrupt = corrupt.unwrap();
    assert!(!corrupt.healthy());
    assert!(
        corrupt
            .problems
            .iter()
            .any(|problem| problem.code.contains("hash") || problem.code.contains("checksum"))
    );
    let restored = restored.unwrap();
    assert!(restored.healthy(), "{:?}", restored.problems);
}

#[test]
fn deep_integrity_rejects_coherent_native_tree_not_derived_from_log() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("inspect_native_semantic_forgery");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store
        .create_database(&database_id, indexed_schema())
        .unwrap();
    let service = common::start_service(&connection, &database_id);
    let committed = common::transact(
        &service,
        "authentic",
        created.basis_t(),
        &[add("authentic")],
        1_000,
    );
    service.shutdown();
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();

    let authoritative = store.recover(&database_id).unwrap();
    assert_eq!(authoritative.basis_t(), committed.basis_t);
    let config = TreeConfig::default();
    let mut manifest_trees = Vec::new();
    let mut all_nodes = BTreeMap::new();
    let mut changed_roots = Vec::new();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let mut datoms = authoritative.datoms(
                if history {
                    View::History
                } else {
                    View::Current
                },
                order,
            );
            let mut changed = false;
            for datom in &mut datoms {
                if datom.entity == user(42)
                    && datom.attribute == ITEM_VALUE
                    && datom.value.stored_eq(&Value::String("authentic".into()))
                {
                    datom.value = Value::String("forged".into());
                    changed = true;
                }
            }
            if changed {
                changed_roots.push((history, order));
                datoms.sort_by(|left, right| left.cmp_in(right, order));
            }
            let build = build_tree(order, history, datoms, &config).unwrap();
            let root_bytes = build.stats.root_bytes;
            manifest_trees.push(ManifestTree {
                descriptor: build.descriptor,
                root_bytes,
            });
            all_nodes.extend(build.nodes.into_nodes());
        }
    }
    assert_eq!(changed_roots.len(), 6);

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let coordinate = raw
        .query_one(
            "SELECT p.publication_revision, m.basis_t, m.tx_hash, m.state_hash, \
                    m.excision_generation, m.eidx_frontier \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m ON m.manifest_hash = p.manifest_hash \
              WHERE p.database_id = $1 ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap();
    let expected_revision = u64::try_from(coordinate.get::<_, i64>(0)).unwrap();
    let basis_t = u64::try_from(coordinate.get::<_, i64>(1)).unwrap();
    let tx_hash = coordinate
        .get::<_, Vec<u8>>(2)
        .try_into()
        .expect("transaction hash is 32 bytes");
    let state_hash = coordinate
        .get::<_, Vec<u8>>(3)
        .try_into()
        .expect("state hash is 32 bytes");
    let generation = u64::try_from(coordinate.get::<_, i64>(4)).unwrap();
    let eidx_frontier = u64::try_from(coordinate.get::<_, i64>(5)).unwrap();
    assert_eq!(basis_t, authoritative.basis_t());

    let forged_manifest = PersistentTreeManifest {
        database_id: database_id.clone(),
        publication_revision: expected_revision + 1,
        basis_t,
        tx_hash,
        // The trusted indexer can copy the authoritative coordinate. That
        // claim does not prove these independently hashed trees contain it.
        state_hash,
        excision_generation: generation,
        eidx_frontier,
        trees: manifest_trees,
        pending_avet: Vec::new(),
    };
    let payload = forged_manifest.encode().unwrap();
    let manifest_hash = sha256(&payload);
    let roots = forged_manifest
        .trees
        .iter()
        .map(|tree| TreeRootBinding {
            order: tree.descriptor.order,
            history: tree.descriptor.history,
            root_hash: tree.descriptor.root_hash,
            datom_count: tree.descriptor.count,
            encoded_bytes: tree.root_bytes,
        })
        .collect();
    let record = TreeManifestRecord {
        database_id: database_id.clone(),
        publication_revision: forged_manifest.publication_revision,
        basis_t,
        tx_hash,
        state_hash,
        excision_generation: generation,
        eidx_frontier,
        manifest_hash,
        payload,
        roots,
    };
    let live_nodes = all_nodes.keys().copied().collect();
    let mut trees = PostgresTreeStore::connect(&connection).unwrap();
    trees
        .begin_build_intent(
            &database_id,
            generation,
            expected_revision,
            manifest_hash,
            &live_nodes,
        )
        .unwrap();
    for (hash, payload) in &all_nodes {
        trees.insert_node(*hash, payload).unwrap();
    }
    assert_eq!(
        trees
            .publish_manifest_with_delta(
                &record,
                expected_revision,
                &TreePublicationDelta::Replace {
                    live_nodes: live_nodes.clone(),
                },
            )
            .unwrap(),
        TreePublishOutcome::Published
    );
    trees.release_build_intent().unwrap();
    let live = raw
        .query_one(
            "SELECT manifest_hash, complete, problem_code \
               FROM atomic_tree_live_sets WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap();
    assert_eq!(live.get::<_, Vec<u8>>(0), manifest_hash);
    assert!(live.get::<_, bool>(1));
    assert_eq!(live.get::<_, Option<String>>(2), None);

    // Shallow inspection authenticates the PostgreSQL publication envelope,
    // every content hash, and the root graph. It deliberately trusts the
    // conditionally published indexer result at that boundary.
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let shallow = operator.inspect_database(&database_id, false).unwrap();
    assert!(shallow.healthy(), "{:?}", shallow.problems);

    // The explicit broad operation also reconstructs the authoritative log
    // value and compares its semantic information with the physical EAVT.
    let deep = operator.inspect_database(&database_id, true).unwrap();
    assert!(!deep.healthy());
    assert!(deep.problems.iter().any(|problem| {
        problem.code == "integrity/tree-semantic-mismatch"
            && problem.message.contains("current/history information")
    }));
}

#[test]
fn configured_operation_byte_and_history_limits_fail_before_publication() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("capacity");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let initial_basis = created.basis_t();
    let op_limited = common::start_service_with_limits(
        &connection,
        &database_id,
        CapacityLimits {
            max_transaction_ops: 1,
            ..CapacityLimits::default()
        },
    );
    let error = common::try_transact(
        &op_limited,
        "too-many",
        initial_basis,
        &[add("one"), add("two")],
        1_000,
    )
    .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Busy, "postgres/transaction-op-capacity")
    );
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        initial_basis
    );
    op_limited.shutdown();

    let byte_limited = common::start_service_with_limits(
        &connection,
        &database_id,
        CapacityLimits {
            max_transaction_bytes: 1,
            ..CapacityLimits::default()
        },
    );
    assert_eq!(
        common::try_transact(
            &byte_limited,
            "too-large",
            initial_basis,
            &[add("one")],
            1_000,
        )
        .unwrap_err()
        .code,
        "service/request-byte-capacity"
    );
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        initial_basis
    );
    byte_limited.shutdown();

    let history_limited = common::start_service_with_limits(
        &connection,
        &database_id,
        CapacityLimits {
            max_history_transactions: initial_basis + 1,
            ..CapacityLimits::default()
        },
    );
    common::transact(&history_limited, "one", initial_basis, &[add("one")], 1_000);
    assert_eq!(
        common::try_transact(
            &history_limited,
            "two",
            initial_basis + 1,
            &[add("two")],
            2_000,
        )
        .unwrap_err()
        .code,
        "postgres/history-capacity"
    );
    assert!(
        common::transact(&history_limited, "one", initial_basis, &[add("one")], 1_000,).replayed,
        "capacity limits do not hide an already durable outcome"
    );
    history_limited.shutdown();
}
