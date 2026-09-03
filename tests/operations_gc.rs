use atomic_core::{
    Attribute, Cardinality, DB_FN, DB_IDENT, Digest, EntityRef, IndexBuildFault, IndexOrder,
    IndexSegment, Instruction, Keyword, Peer, PersistentTreeManifest, PostgresIndexer,
    PostgresOperator, PostgresStore, PostgresTreeStore, Program, ProgramKind,
    RECOMMENDED_GARBAGE_COLLECTION_AGE, Schema, TreeManifestRecord, TreePublicationDelta,
    TreePublishOutcome, TxOp, TxValue, USER_PARTITION, Value, ValueType, View,
    encode_index_segment, encode_program, make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::sync::{Arc, Barrier};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

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

fn unique_long() -> i64 {
    (SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos()
        % (i64::MAX as u128 - 1)) as i64
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add(value: i64) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(user(42)),
        attribute: ITEM_VALUE,
        value: TxValue::Scalar(Value::Long(value)),
    }
}

fn republish_same_basis(connection: &str, database_id: &str) -> TreeManifestRecord {
    let mut trees = PostgresTreeStore::connect(connection).unwrap();
    let revision = trees.current_publication_revision(database_id).unwrap();
    let current = trees.load_manifest(database_id, revision).unwrap().unwrap();
    let mut envelope = PersistentTreeManifest::decode(&current.payload).unwrap();
    envelope.publication_revision += 1;
    let payload = envelope.encode().unwrap();
    let predecessor_manifest_hash = current.manifest_hash;
    let successor = TreeManifestRecord {
        publication_revision: envelope.publication_revision,
        manifest_hash: sha256(&payload),
        payload,
        ..current
    };
    let upload_hashes = BTreeSet::new();
    trees
        .begin_build_intent(
            database_id,
            revision,
            successor.manifest_hash,
            &upload_hashes,
        )
        .unwrap();
    assert_eq!(
        trees
            .publish_manifest_with_delta(
                &successor,
                revision,
                &TreePublicationDelta::Incremental {
                    predecessor_manifest_hash,
                    added_nodes: BTreeSet::new(),
                    retired_nodes: BTreeSet::new(),
                },
            )
            .unwrap(),
        TreePublishOutcome::Published
    );
    trees.release_build_intent().unwrap();
    successor
}

fn republish_with_forged_old_timestamp(connection: &str, database_id: &str) -> TreeManifestRecord {
    let mut trees = PostgresTreeStore::connect(connection).unwrap();
    let revision = trees.current_publication_revision(database_id).unwrap();
    let current = trees.load_manifest(database_id, revision).unwrap().unwrap();
    let mut envelope = PersistentTreeManifest::decode(&current.payload).unwrap();
    envelope.publication_revision += 1;
    let payload = envelope.encode().unwrap();
    let successor = TreeManifestRecord {
        publication_revision: envelope.publication_revision,
        manifest_hash: sha256(&payload),
        payload,
        ..current.clone()
    };
    drop(trees);

    let mut client = Client::connect(connection, NoTls).unwrap();
    let mut transaction = client.transaction().unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_manifests \
               (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, 4, $8, $9)",
            &[
                &successor.database_id,
                &(successor.publication_revision as i64),
                &(successor.basis_t as i64),
                &&successor.tx_hash[..],
                &&successor.state_hash[..],
                &(successor.excision_generation as i64),
                &(successor.eidx_frontier as i64),
                &&successor.manifest_hash[..],
                &&successor.payload[..],
            ],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_manifest_roots \
                   (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
             SELECT $1, index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $2",
            &[&&successor.manifest_hash[..], &&current.manifest_hash[..]],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_build_intents \
                   (manifest_hash, database_id, expected_revision) \
             VALUES ($1, $2, $3)",
            &[
                &&successor.manifest_hash[..],
                &successor.database_id,
                &(revision as i64),
            ],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_delta_headers \
                   (manifest_hash, predecessor_manifest_hash, delta_mode) \
             VALUES ($1, $2, 2)",
            &[&&successor.manifest_hash[..], &&current.manifest_hash[..]],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_publications \
                   (database_id, publication_revision, basis_t, tx_hash, manifest_hash, published_at) \
             VALUES ($1, $2, $3, $4, $5, clock_timestamp() - interval '31 days')",
            &[
                &successor.database_id,
                &(successor.publication_revision as i64),
                &(successor.basis_t as i64),
                &&successor.tx_hash[..],
                &&successor.manifest_hash[..],
            ],
        )
        .unwrap();
    transaction.commit().unwrap();
    successor
}

fn pin_application_name(database_id: &str) -> String {
    let digest = sha256(database_id.as_bytes());
    let suffix: String = digest[..16]
        .iter()
        .map(|byte| format!("{byte:02x}"))
        .collect();
    format!("atomic-pin-{suffix}")
}

#[test]
fn gc_reclaims_proven_unreferenced_values_but_retains_untracked_legacy_segments() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let db = common::transact(&service, "one", created.basis_t(), &[add(1)], 1_000).db_after;
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();

    let mut orphan_datoms = db.datoms(View::History, IndexOrder::Eavt);
    orphan_datoms[0].entity = user(999);
    orphan_datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
    let orphan_segment = IndexSegment {
        order: IndexOrder::Eavt,
        history: true,
        datoms: orphan_datoms,
    };
    let segment_bytes = encode_index_segment(&orphan_segment).unwrap();
    let segment_hash = sha256(&segment_bytes);
    let orphan_program = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(
                SystemTime::now()
                    .duration_since(UNIX_EPOCH)
                    .unwrap()
                    .as_nanos() as i64,
            )),
            Instruction::Return,
        ],
    };
    let program_bytes = encode_program(&orphan_program).unwrap();
    let program_hash = sha256(&program_bytes);
    // A pre-ledger crash orphan has no durable intent. Once every retained
    // native publication has exact membership provenance, the conservative
    // age sweep can prove this detached value is not live anywhere.
    let orphan_node_bytes = b"pre-ledger-content-first-crash-orphan".to_vec();
    let orphan_node_hash = sha256(&orphan_node_bytes);
    let mut client = Client::connect(&connection, NoTls).unwrap();
    client
        .execute(
            "INSERT INTO atomic_index_segments (segment_hash, payload, created_at) \
             VALUES ($1, $2, clock_timestamp() - interval '1 second') \
             ON CONFLICT DO NOTHING",
            &[&&segment_hash[..], &&segment_bytes[..]],
        )
        .unwrap();
    client
        .execute(
            "INSERT INTO atomic_programs (program_hash, kind, arity, payload, created_at) \
             VALUES ($1, 2, 0, $2, clock_timestamp() - interval '1 second') \
             ON CONFLICT DO NOTHING",
            &[&&program_hash[..], &&program_bytes[..]],
        )
        .unwrap();
    client
        .execute(
            "INSERT INTO atomic_tree_nodes (node_hash, payload, created_at) \
             VALUES ($1, $2, clock_timestamp() - interval '1 second') \
             ON CONFLICT DO NOTHING",
            &[&&orphan_node_hash[..], &&orphan_node_bytes[..]],
        )
        .unwrap();
    let _ = db; // retain an immutable pre-GC value while storage is reclaimed

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    // The month is guidance, not a semantic gate. A deliberate zero-age run
    // is useful after a controlled import and still obeys exact liveness.
    let dry = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(!dry.applied);
    assert!(dry.segment_hashes.is_empty());
    assert!(dry.program_hashes.contains(&program_hash));
    assert!(dry.tree_node_hashes.contains(&orphan_node_hash));
    let mut expected = dry.clone();
    expected.applied = true;
    assert_eq!(
        client
            .query_one(
                "SELECT count(*) FROM atomic_index_segments WHERE segment_hash = $1",
                &[&&segment_hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    assert_eq!(
        client
            .query_one(
                "SELECT count(*) FROM atomic_programs WHERE program_hash = $1",
                &[&&program_hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    let applied = operator
        .collect_garbage(Duration::ZERO)
        .unwrap();
    assert_eq!(applied, expected);
    assert!(applied.applied);
    assert!(applied.segment_hashes.is_empty());
    assert!(applied.program_hashes.contains(&program_hash));
    assert!(applied.tree_node_hashes.contains(&orphan_node_hash));
    assert_eq!(
        client
            .query_one(
                "SELECT count(*) FROM atomic_index_segments WHERE segment_hash = $1",
                &[&&segment_hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    assert_eq!(
        client
            .query_one(
                "SELECT count(*) FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&orphan_node_hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    assert_eq!(
        client
            .query_one(
                "SELECT count(*) FROM atomic_programs WHERE program_hash = $1",
                &[&&program_hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    let peer = Peer::connect(&connection, &database_id, 1).unwrap();
    assert_eq!(
        peer.db().values(user(42), ITEM_VALUE),
        vec![&Value::Long(1)]
    );
    service.shutdown();
}

#[test]
fn forged_publication_time_cannot_backdate_the_retirement_mark() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_retirement_clock");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    common::transact(
        &service,
        "retirement-clock",
        created.basis_t(),
        &[add(1)],
        1_000,
    );
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();
    let forged = republish_with_forged_old_timestamp(&connection, &database_id);

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let row = client
        .query_one(
            "SELECT p.published_at < clock_timestamp() - interval '30 days', \
                    r.retired_at > clock_timestamp() - interval '1 minute' \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_retirements r \
                 ON r.database_id = p.database_id \
                AND r.publication_revision = p.publication_revision - 1 \
              WHERE p.database_id = $1 AND p.publication_revision = $2",
            &[&database_id, &(forged.publication_revision as i64)],
        )
        .unwrap();
    let publication_is_old: bool = row.get(0);
    let retirement_is_recent: bool = row.get(1);
    assert!(publication_is_old);
    assert!(retirement_is_recent);
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    assert!(
        operator
            .garbage_inventory(RECOMMENDED_GARBAGE_COLLECTION_AGE)
            .unwrap()
            .tree_publications
            .is_empty()
    );
    service.shutdown();
}

#[test]
fn concurrent_consolidation_and_gc_never_remove_published_segments() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_race");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let mut basis = created.basis_t();
    for value in 1..=4 {
        basis = common::transact(
            &service,
            &format!("request-{value}"),
            basis,
            &[add(value)],
            999 + value,
        )
        .basis_t;
    }
    let barrier = Arc::new(Barrier::new(3));
    let index_handle = {
        let connection = connection.clone();
        let database_id = database_id.clone();
        let barrier = barrier.clone();
        std::thread::spawn(move || {
            let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
            barrier.wait();
            indexer.consolidate().unwrap();
        })
    };
    let gc_handle = {
        let connection = connection.clone();
        let barrier = barrier.clone();
        std::thread::spawn(move || {
            let mut operator = PostgresOperator::connect(&connection).unwrap();
            barrier.wait();
            operator
                .collect_garbage(RECOMMENDED_GARBAGE_COLLECTION_AGE)
                .unwrap();
        })
    };
    barrier.wait();
    index_handle.join().unwrap();
    gc_handle.join().unwrap();
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let report = operator.inspect_database(&database_id, true).unwrap();
    assert!(report.healthy(), "{:?}", report.problems);
    assert_eq!(report.metrics.index_lag, 0);
    let peer = Peer::connect(&connection, &database_id, 1).unwrap();
    assert_eq!(
        peer.db().values(user(42), ITEM_VALUE),
        vec![&Value::Long(4)]
    );
    service.shutdown();
}

#[test]
fn abandoned_content_first_build_is_exactly_collected_and_can_be_retried() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_abandoned_build");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let value = unique_long();
    common::transact(
        &service,
        "abandoned-build",
        created.basis_t(),
        &[add(value)],
        1_000,
    );

    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    let interrupted = indexer
        .consolidate_with_fault(IndexBuildFault::AfterSegments)
        .unwrap_err();
    assert_eq!(interrupted.code, "index/injected-failure");

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let intent = raw
        .query_one(
            "SELECT manifest_hash FROM atomic_tree_build_intents WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap();
    let manifest_hash = <Digest>::try_from(intent.get::<_, Vec<u8>>(0)).unwrap();
    let uploaded = raw
        .query(
            "SELECT node_hash FROM atomic_tree_build_intent_nodes WHERE manifest_hash = $1",
            &[&&manifest_hash[..]],
        )
        .unwrap()
        .into_iter()
        .map(|row| <Digest>::try_from(row.get::<_, Vec<u8>>(0)).unwrap())
        .collect::<BTreeSet<_>>();
    assert!(!uploaded.is_empty());
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let dry = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(dry.tree_build_intents.iter().any(|candidate| {
        candidate.database_id == database_id && candidate.manifest_hash == manifest_hash
    }));
    let predicted = dry
        .tree_node_hashes
        .iter()
        .copied()
        .filter(|hash| uploaded.contains(hash))
        .collect::<BTreeSet<_>>();
    assert!(!predicted.is_empty());
    let mut expected = dry.clone();
    expected.applied = true;
    let collected = operator.collect_garbage(Duration::ZERO).unwrap();
    assert_eq!(collected, expected);
    assert!(collected.tree_build_intents.iter().any(|candidate| {
        candidate.database_id == database_id && candidate.manifest_hash == manifest_hash
    }));
    let deleted = collected
        .tree_node_hashes
        .iter()
        .copied()
        .filter(|hash| uploaded.contains(hash))
        .collect::<BTreeSet<_>>();
    assert_eq!(deleted, predicted);
    for hash in &deleted {
        assert_eq!(
            raw.query_one(
                "SELECT count(*) FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
            0
        );
    }

    // Rebuilding the same deterministic candidate after GC must simply
    // re-upload its immutable values and publish revision one once.
    let retried = indexer.consolidate().unwrap();
    assert_eq!(retried.publication_revision, 1);
    assert_eq!(retried.manifest_hash, manifest_hash);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );
    let peer = Peer::connect(&connection, &database_id, 1).unwrap();
    assert_eq!(
        peer.db().values(user(42), ITEM_VALUE),
        vec![&Value::Long(value)]
    );
    service.shutdown();
}

#[test]
fn gc_retains_current_and_historical_temporal_function_blobs() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_temporal_functions");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let program = |value| Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushEntity(EntityRef::Id(user(42))),
            Instruction::PushConstant(Value::Long(value)),
            Instruction::EmitAdd(ITEM_VALUE),
            Instruction::Return,
        ],
    };
    let old_hash = store.deploy_program_blob(&program(11)).unwrap();
    let current_hash = store.deploy_program_blob(&program(22)).unwrap();
    let service = common::start_service(&connection, &database_id);
    let installed = common::transact(
        &service,
        "install-temporal-function",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("function".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("gc", "temporal-function")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("function".into()),
                attribute: DB_FN as u32,
                value: Value::Function(old_hash).into(),
            },
        ],
        1_000,
    );
    let function = installed.tempids["function"];
    let rebound = common::transact(
        &service,
        "rebind-temporal-function",
        installed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(function),
            attribute: DB_FN as u32,
            value: Value::Function(current_hash).into(),
        }],
        2_000,
    );
    assert_eq!(
        rebound.db_after.values(function, DB_FN as u32),
        vec![&Value::Function(current_hash)]
    );

    // Make both values old enough to be candidates absent the exact temporal
    // scan. Immutable triggers are bypassed only by this owner-level fixture.
    let mut client = Client::connect(&connection, NoTls).unwrap();
    client
        .batch_execute("SET session_replication_role = replica")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_programs SET created_at = clock_timestamp() - interval '31 days' \
              WHERE program_hash = $1 OR program_hash = $2",
            &[&&old_hash[..], &&current_hash[..]],
        )
        .unwrap();
    client
        .execute(
            "UPDATE atomic_program_gc_candidates \
                SET candidate_at = clock_timestamp() - interval '31 days' \
              WHERE program_hash = $1 OR program_hash = $2",
            &[&&old_hash[..], &&current_hash[..]],
        )
        .unwrap();
    client
        .batch_execute("SET session_replication_role = origin")
        .unwrap();

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let inventory = operator
        .collect_garbage(RECOMMENDED_GARBAGE_COLLECTION_AGE)
        .unwrap();
    assert!(!inventory.program_hashes.contains(&old_hash));
    assert!(!inventory.program_hashes.contains(&current_hash));
    for hash in [old_hash, current_hash] {
        assert_eq!(
            client
                .query_one(
                    "SELECT count(*) FROM atomic_programs WHERE program_hash = $1",
                    &[&&hash[..]],
                )
                .unwrap()
                .get::<_, i64>(0),
            1
        );
    }
    service.shutdown();
}

#[test]
fn native_root_retirement_honors_snapshot_pins_and_reclaims_released_values() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_native_roots");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let first_value = unique_long();
    let second_value = first_value + 1;
    let first = common::transact(
        &service,
        "native-first",
        created.basis_t(),
        &[add(first_value)],
        1_000,
    );
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    let publication_one = indexer.consolidate().unwrap();

    // Zero cache capacity ensures the retained snapshot must still be able to
    // fetch directories/leaves after GC, not merely return a cached answer.
    let peer = Peer::connect_with_cache_limits(&connection, &database_id, 0, 0).unwrap();
    let old_snapshot = peer.snapshot();
    let snapshot_clones = (0..128).map(|_| old_snapshot.clone()).collect::<Vec<_>>();
    let publication_two = republish_same_basis(&connection, &database_id);
    assert_eq!(publication_two.basis_t, publication_one.basis_t);
    assert_eq!(
        publication_two.publication_revision,
        publication_one.publication_revision + 1
    );

    let second = common::transact(
        &service,
        "native-second",
        first.basis_t,
        &[add(second_value)],
        2_000,
    );
    let publication_three = indexer.consolidate().unwrap();
    assert_eq!(
        publication_three.publication_revision,
        publication_two.publication_revision + 1
    );
    assert_eq!(publication_three.basis_t, second.basis_t);
    peer.sync_to_snapshot(second.basis_t, Duration::from_secs(1))
        .unwrap();
    assert!(peer.refresh_index().unwrap());
    assert_eq!(
        peer.durable_base_revision(),
        publication_three.publication_revision
    );

    // One PeerCore uses exactly one pin backend even while two physical roots
    // and many snapshot handles remain live.
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let pin_backends: i64 = raw
        .query_one(
            "SELECT count(*) FROM pg_stat_activity WHERE application_name = $1",
            &[&pin_application_name(&database_id)],
        )
        .unwrap()
        .get(0);
    assert_eq!(pin_backends, 1);
    assert_eq!(peer.pinned_manifest_hashes().len(), 2);

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let dry_pinned = operator.garbage_inventory(Duration::ZERO).unwrap();
    let pinned_candidates = dry_pinned
        .tree_publications
        .iter()
        .filter(|candidate| candidate.database_id == database_id)
        .map(|candidate| candidate.publication_revision)
        .collect::<BTreeSet<_>>();
    assert!(pinned_candidates.is_empty());
    // Prefix retirement is intentional: a pin on revision one also blocks
    // revision two, so no later root can be reclaimed out of order.
    let mut expected_pinned = dry_pinned.clone();
    expected_pinned.applied = true;
    let applied_pinned = operator
        .collect_garbage(Duration::ZERO)
        .unwrap();
    assert_eq!(applied_pinned, expected_pinned);
    assert!(
        applied_pinned
            .tree_publications
            .iter()
            .all(|candidate| candidate.database_id != database_id)
    );

    // The old immutable database value remains a real lazy reader while its
    // publication has been superseded. Even a deliberately zero-age run did
    // not invalidate the pinned value or any of its lazily loaded nodes.
    assert_eq!(
        old_snapshot
            .database_value()
            .values(user(42), ITEM_VALUE)
            .unwrap(),
        vec![Value::Long(first_value)]
    );
    drop(snapshot_clones);
    drop(old_snapshot);
    assert_eq!(peer.pinned_manifest_hashes().len(), 1);

    let dry_released = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert_eq!(
        dry_released
            .tree_publications
            .iter()
            .filter(|candidate| candidate.database_id == database_id)
            .map(|candidate| candidate.publication_revision)
            .collect::<Vec<_>>(),
        vec![publication_one.publication_revision]
    );
    let mut expected_released = dry_released.clone();
    expected_released.applied = true;
    let released = operator.collect_garbage(Duration::ZERO).unwrap();
    assert_eq!(released, expected_released);
    assert!(
        released.tree_publications.iter().any(|candidate| {
            candidate.database_id == database_id
                && candidate.publication_revision == publication_one.publication_revision
        })
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        2
    );

    let dry_changed = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert_eq!(
        dry_changed
            .tree_publications
            .iter()
            .filter(|candidate| candidate.database_id == database_id)
            .map(|candidate| candidate.publication_revision)
            .collect::<Vec<_>>(),
        vec![publication_two.publication_revision]
    );
    let retired_nodes = raw
        .query(
            "SELECT node_hash FROM atomic_tree_retired_nodes \
              WHERE database_id = $1 AND publication_revision = $2",
            &[&database_id, &(publication_two.publication_revision as i64)],
        )
        .unwrap()
        .into_iter()
        .map(|row| <Digest>::try_from(row.get::<_, Vec<u8>>(0)).unwrap())
        .collect::<BTreeSet<_>>();
    let deleted_nodes = dry_changed
        .tree_node_hashes
        .iter()
        .copied()
        .filter(|hash| retired_nodes.contains(hash))
        .collect::<BTreeSet<_>>();
    assert!(
        !deleted_nodes.is_empty(),
        "a changed successor must leave at least one old physical tree value"
    );
    let mut expected_changed = dry_changed.clone();
    expected_changed.applied = true;
    let changed = operator.collect_garbage(Duration::ZERO).unwrap();
    assert_eq!(changed, expected_changed);
    assert!(changed.tree_publications.iter().any(|candidate| {
        candidate.database_id == database_id
            && candidate.publication_revision == publication_two.publication_revision
    }));
    for hash in deleted_nodes {
        assert_eq!(
            raw.query_one(
                "SELECT count(*) FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
            0
        );
    }
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );
    assert_eq!(
        peer.database_value().values(user(42), ITEM_VALUE).unwrap(),
        vec![Value::Long(second_value)]
    );
    let report = operator.inspect_database(&database_id, true).unwrap();
    assert!(report.healthy(), "{:?}", report.problems);

    // Reopening both operator and peer is the process-restart boundary: the
    // surviving newest publication reconstructs the exact current value.
    drop(operator);
    drop(peer);
    let restarted_peer = Peer::connect(&connection, &database_id, 8).unwrap();
    assert_eq!(
        restarted_peer
            .database_value()
            .values(user(42), ITEM_VALUE)
            .unwrap(),
        vec![Value::Long(second_value)]
    );
    service.shutdown();
}
