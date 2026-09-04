use atomic_core::{
    Attribute, Cardinality, DB_EXCISE, DB_FN, DB_IDENT, Digest, EntityRef, ExcisionFault,
    GarbageInventory, IndexBuildFault, IndexOrder, IndexSegment, Instruction, Keyword,
    MAX_LOG_GENERATION_ROWS_PER_GC, MAX_TREE_BUILD_INTENT_NODES_PER_GC,
    MAX_TREE_RETIREMENT_NODES_PER_GC, Peer, PersistentTreeManifest, PortableBackup,
    PostgresIndexer, PostgresOperator, PostgresStore, PostgresTreeStore, Program, ProgramKind,
    RECOMMENDED_GARBAGE_COLLECTION_AGE, RestoreFault, Schema, TreeManifestRecord,
    TreePublicationDelta, TreePublishOutcome, TreeRootBinding, TxOp, TxValue, USER_PARTITION,
    Value, ValueType, encode_index_segment, encode_program, make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::fs;
use std::sync::{Arc, Barrier};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const ITEM_VALUE: u32 = 1_000;
const MAX_TEST_GC_STEPS: usize = 128;
const MAX_TEST_LOG_GC_STEPS: usize = 128;

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

fn isolated_catalog(connection: &str, label: &str) -> String {
    let schema = unique(label);
    assert!(
        schema
            .bytes()
            .all(|byte| byte.is_ascii_lowercase() || byte.is_ascii_digit() || byte == b'_')
    );
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let scoped = if connection.trim_start().starts_with("postgres://")
        || connection.trim_start().starts_with("postgresql://")
    {
        let separator = if connection.contains('?') { '&' } else { '?' };
        format!("{connection}{separator}options=-csearch_path%3D{schema}")
    } else {
        format!("{connection} options='-c search_path={schema}'")
    };
    let mut migrator = atomic_core::PostgresMigrator::connect(&scoped).unwrap();
    migrator.migrate().unwrap();
    scoped
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

fn assert_same_information(
    left: &impl common::InformationSource,
    right: &impl common::InformationSource,
) {
    common::assert_same_information(left, right);
}

fn provision_generation_zero_database(
    connection: &str,
    store: &mut PostgresStore,
    database_id: &str,
) {
    // New catalogs correctly start in the native positive-generation format.
    // This owner-level fixture recreates the supported upgrade shape: an
    // existing v13 catalog row and basis-zero head that migration 14 leaves as
    // generation zero until its first COW activation.
    let donor = unique("generation_zero_donor");
    store.create_database(&donor, Schema::new()).unwrap();
    let mut raw = Client::connect(connection, NoTls).unwrap();
    raw.execute(
        "INSERT INTO atomic_databases (database_id, genesis, genesis_hash) \
         SELECT $1, genesis, genesis_hash FROM atomic_databases WHERE database_id = $2",
        &[&database_id, &donor],
    )
    .unwrap();
    raw.execute(
        "INSERT INTO atomic_heads (database_id, basis_t, tx_hash, log_generation) \
         SELECT $1, 0, genesis_hash, 0 FROM atomic_databases WHERE database_id = $1",
        &[&database_id],
    )
    .unwrap();
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
            successor.excision_generation,
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
    let manifest_lineage = if successor.excision_generation > 0 {
        Some(
            client
                .query_one(
                    "SELECT lineage_id FROM atomic_databases WHERE database_id = $1",
                    &[&successor.database_id],
                )
                .unwrap()
                .get::<_, String>(0),
        )
    } else {
        None
    };
    let mut transaction = client.transaction().unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_manifests \
               (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                log_generation, lineage_id) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, 4, $8, $9, $10, $11)",
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
                &(successor.excision_generation as i64),
                &manifest_lineage,
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
                   (manifest_hash, database_id, log_generation, expected_revision, \
                    expected_node_count, node_set_hash, intent_state) \
             VALUES ($1, $2, $3, $4, 0, $5, 1)",
            &[
                &&successor.manifest_hash[..],
                &successor.database_id,
                &(successor.excision_generation as i64),
                &(revision as i64),
                &&[0_u8; 32][..],
            ],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_delta_headers \
                   (manifest_hash, predecessor_manifest_hash, delta_mode, \
                    expected_node_count, staged_node_count, delta_set_hash, \
                    added_node_count, added_set_hash, delta_state) \
             VALUES ($1, $2, 2, 0, 0, decode(repeat('00', 32), 'hex'), \
                     0, decode(repeat('00', 32), 'hex'), 1)",
            &[&&successor.manifest_hash[..], &&current.manifest_hash[..]],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_publications \
                   (database_id, publication_revision, basis_t, tx_hash, manifest_hash, \
                    published_at, log_generation) \
             VALUES ($1, $2, $3, $4, $5, clock_timestamp() - interval '31 days', $6)",
            &[
                &successor.database_id,
                &(successor.publication_revision as i64),
                &(successor.basis_t as i64),
                &&successor.tx_hash[..],
                &&successor.manifest_hash[..],
                &(successor.excision_generation as i64),
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

fn inventory_has_work(inventory: &GarbageInventory) -> bool {
    !inventory.segment_hashes.is_empty()
        || !inventory.program_hashes.is_empty()
        || !inventory.tree_publications.is_empty()
        || !inventory.tree_build_intents.is_empty()
        || !inventory.tree_manifest_hashes.is_empty()
        || !inventory.tree_node_hashes.is_empty()
        || !inventory.log_generations.is_empty()
        || !inventory.semantic_commitment_roots.is_empty()
        || !inventory.semantic_commitment_node_hashes.is_empty()
}

fn apply_exact_inventory(operator: &mut PostgresOperator, dry: &GarbageInventory) {
    let mut expected = dry.clone();
    expected.applied = true;
    assert_eq!(operator.collect_garbage(Duration::ZERO).unwrap(), expected);
}

#[test]
fn semantic_node_gc_is_age_gated_and_bounded() {
    let Some(connection) = connection() else {
        return;
    };
    let scoped = isolated_catalog(&connection, "gc_semantic_frontier");
    let database_id = unique("gc_semantic_live");
    let mut store = PostgresStore::connect(&scoped).unwrap();
    store.create_database(&database_id, Schema::new()).unwrap();
    let mut raw = Client::connect(&scoped, NoTls).unwrap();
    let active_root: Option<Vec<u8>> = raw
        .query_one(
            "SELECT current_root FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 ORDER BY basis_t DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    let values = (0..atomic_core::MAX_SEMANTIC_COMMITMENT_NODES_PER_GC + 1)
        .map(|ordinal| format!("orphan-semantic-{database_id}-{ordinal}").into_bytes())
        .map(|payload| (sha256(&payload), payload))
        .collect::<Vec<_>>();
    let hashes = values
        .iter()
        .map(|(hash, _)| hash.to_vec())
        .collect::<Vec<_>>();
    let payloads = values
        .iter()
        .map(|(_, payload)| payload.clone())
        .collect::<Vec<_>>();
    raw.execute(
        "INSERT INTO atomic_semantic_commitment_nodes \
             (node_hash, payload, subtree_count) \
         SELECT hash, payload, 1 FROM unnest($1::bytea[], $2::bytea[]) \
              AS inserted(hash, payload)",
        &[&hashes, &payloads],
    )
    .unwrap();

    let mut operator = PostgresOperator::connect(&scoped).unwrap();
    let metrics = operator
        .inspect_database(&database_id, false)
        .unwrap()
        .metrics;
    assert!(metrics.semantic_commitment_roots > 0);
    assert_eq!(
        metrics.orphan_semantic_commitment_nodes,
        values.len() as u64
    );
    let protected = operator
        .garbage_inventory(RECOMMENDED_GARBAGE_COLLECTION_AGE)
        .unwrap();
    assert!(protected.semantic_commitment_node_hashes.is_empty());

    let first = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert_eq!(
        first.semantic_commitment_node_hashes.len(),
        atomic_core::MAX_SEMANTIC_COMMITMENT_NODES_PER_GC
    );
    apply_exact_inventory(&mut operator, &first);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_nodes \
              WHERE node_hash = ANY($1)",
            &[&hashes],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );

    let second = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert_eq!(second.semantic_commitment_node_hashes.len(), 1);
    apply_exact_inventory(&mut operator, &second);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_nodes \
              WHERE node_hash = ANY($1)",
            &[&hashes],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    if let Some(active_root) = active_root {
        assert!(
            raw.query_opt(
                "SELECT 1 FROM atomic_semantic_commitment_nodes WHERE node_hash = $1",
                &[&active_root],
            )
            .unwrap()
            .is_some(),
            "active semantic root node was collected"
        );
    }
}

fn preview_next_tree_build_intent(
    operator: &mut PostgresOperator,
    database_id: &str,
    manifest_hash: Digest,
) -> GarbageInventory {
    for attempt in 0..MAX_TEST_GC_STEPS {
        let dry = operator.garbage_inventory(Duration::ZERO).unwrap();
        if dry.tree_build_intents.iter().any(|candidate| {
            candidate.database_id == database_id && candidate.manifest_hash == manifest_hash
        }) {
            return dry;
        }
        assert!(
            inventory_has_work(&dry),
            "target build intent is not eligible and no unrelated work can advance it: \
             database={database_id}, manifest={manifest_hash:?}, attempt={attempt}, \
             inventory={dry:?}"
        );
        apply_exact_inventory(operator, &dry);
    }
    panic!(
        "target build intent did not become collectible after {MAX_TEST_GC_STEPS} exact GC steps: \
         database={database_id}, manifest={manifest_hash:?}"
    );
}

fn drain_tree_build_intents_for_database(
    operator: &mut PostgresOperator,
    client: &mut Client,
    database_id: &str,
) {
    for attempt in 0..MAX_TEST_GC_STEPS {
        let remaining: i64 = client
            .query_one(
                "SELECT count(*) FROM atomic_tree_build_intents WHERE database_id = $1",
                &[&database_id],
            )
            .unwrap()
            .get(0);
        if remaining == 0 {
            return;
        }
        let dry = operator.garbage_inventory(Duration::ZERO).unwrap();
        assert!(
            inventory_has_work(&dry),
            "database still has {remaining} build intent(s), but no GC work is eligible: \
             database={database_id}, attempt={attempt}, inventory={dry:?}"
        );
        apply_exact_inventory(operator, &dry);
    }
    let remaining: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_tree_build_intents WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    panic!(
        "database still has {remaining} build intent(s) after {MAX_TEST_GC_STEPS} exact GC steps: \
         database={database_id}"
    );
}

fn preview_next_tree_retirement(
    operator: &mut PostgresOperator,
    database_id: &str,
    publication_revision: u64,
) -> GarbageInventory {
    for _ in 0..MAX_TEST_GC_STEPS {
        let dry = operator.garbage_inventory(Duration::ZERO).unwrap();
        if dry.tree_publications.iter().any(|candidate| {
            candidate.database_id == database_id
                && candidate.publication_revision == publication_revision
        }) {
            return dry;
        }
        assert!(
            inventory_has_work(&dry),
            "target retirement is not eligible and no unrelated work can advance it: \
             database={database_id}, revision={publication_revision}, inventory={dry:?}"
        );
        apply_exact_inventory(operator, &dry);
    }
    panic!("target tree retirement did not become collectible");
}

fn preview_next_log_generation(
    operator: &mut PostgresOperator,
    database_id: &str,
    generation: u64,
) -> GarbageInventory {
    preview_next_log_generation_at_age(operator, database_id, generation, Duration::ZERO)
}

fn preview_next_log_generation_at_age(
    operator: &mut PostgresOperator,
    database_id: &str,
    generation: u64,
    minimum_age: Duration,
) -> GarbageInventory {
    for _ in 0..MAX_TEST_LOG_GC_STEPS {
        let dry = operator.garbage_inventory(minimum_age).unwrap();
        if dry.log_generations.iter().any(|candidate| {
            candidate.database_id == database_id && candidate.generation == generation
        }) {
            return dry;
        }
        assert!(
            inventory_has_work(&dry),
            "target log generation is not eligible and no unrelated work can advance it: \
             database={database_id}, generation={generation}, inventory={dry:?}"
        );
        let mut expected = dry.clone();
        expected.applied = true;
        assert_eq!(operator.collect_garbage(minimum_age).unwrap(), expected);
    }
    panic!("target log generation did not become collectible");
}

fn collect_log_generation_to_completion(
    operator: &mut PostgresOperator,
    database_id: &str,
    generation: u64,
) {
    for _ in 0..128 {
        let dry = preview_next_log_generation(operator, database_id, generation);
        let complete = dry.log_generations.iter().any(|candidate| {
            candidate.database_id == database_id
                && candidate.generation == generation
                && candidate.is_complete
        });
        let mut expected = dry.clone();
        expected.applied = true;
        assert_eq!(operator.collect_garbage(Duration::ZERO).unwrap(), expected);
        if complete {
            return;
        }
    }
    panic!("target log generation collection did not converge");
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

    let mut orphan_datoms = db.history().datoms(IndexOrder::Eavt).unwrap();
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
    // A pre-ledger crash orphan has no durable intent. Age and apparent
    // reachability are not provenance, so the collector must leave it alone.
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
    assert!(!dry.tree_node_hashes.contains(&orphan_node_hash));
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
    let applied = operator.collect_garbage(Duration::ZERO).unwrap();
    assert_eq!(applied, expected);
    assert!(applied.applied);
    assert!(applied.segment_hashes.is_empty());
    assert!(applied.program_hashes.contains(&program_hash));
    assert!(!applied.tree_node_hashes.contains(&orphan_node_hash));
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
fn retired_log_generation_gc_is_pinned_phased_bounded_and_restart_safe() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_log_generation");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    provision_generation_zero_database(&connection, &mut store, &database_id);
    let service = common::start_service(&connection, &database_id);
    let schema_ops = schema()
        .attributes()
        .cloned()
        .map(TxOp::InstallAttribute)
        .collect::<Vec<_>>();
    let installed = common::transact(
        &service,
        "generation-gc-install-schema",
        0,
        &schema_ops,
        500,
    );
    let seeded = common::transact(
        &service,
        "generation-gc-seed",
        installed.basis_t,
        &[add(unique_long())],
        1_000,
    );
    let requested = common::transact(
        &service,
        "generation-gc-excision",
        seeded.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("generation-gc-request".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(user(42))),
        }],
        2_000,
    );
    service.shutdown();

    // This immutable value has no tree root, so its generation session pin is
    // the only thing standing between a lazy reader and retired log bytes.
    let peer = Peer::connect(&connection, &database_id, 0).unwrap();
    let old_snapshot = peer.snapshot();
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let receipt = operator.process_excision_requests(&database_id).unwrap();
    assert_eq!(receipt.source_generation, 0);
    assert_eq!(receipt.basis_t, requested.basis_t);

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let legacy_endpoint = raw
        .query_one(
            "SELECT basis_t, tx_hash FROM atomic_transactions \
              WHERE database_id = $1 ORDER BY basis_t DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap();
    let legacy_basis: i64 = legacy_endpoint.get(0);
    let legacy_tx_hash: Vec<u8> = legacy_endpoint.get(1);
    let legacy_manifest_payload = vec![0x5a; 64];
    let legacy_manifest_hash = sha256(&legacy_manifest_payload);
    raw.execute(
        "INSERT INTO atomic_index_manifests \
                (database_id, basis_t, tx_hash, manifest_hash, payload) \
         VALUES ($1, $2, $3, $4, $5)",
        &[
            &database_id,
            &legacy_basis,
            &legacy_tx_hash,
            &&legacy_manifest_hash[..],
            &legacy_manifest_payload,
        ],
    )
    .unwrap();
    raw.execute(
        "INSERT INTO atomic_index_publications \
                (database_id, basis_t, tx_hash, manifest_hash) \
         VALUES ($1, $2, $3, $4)",
        &[
            &database_id,
            &legacy_basis,
            &legacy_tx_hash,
            &&legacy_manifest_hash[..],
        ],
    )
    .unwrap();

    // A large exact reference ledger exercises the wrapper's fixed batch,
    // without manufacturing hundreds of authoritative transactions.
    let programs = (0..MAX_LOG_GENERATION_ROWS_PER_GC + 1)
        .map(|ordinal| Program {
            kind: ProgramKind::Query,
            arity: 0,
            instructions: vec![
                Instruction::PushConstant(Value::Long(ordinal as i64)),
                Instruction::Return,
            ],
        })
        .map(|program| encode_program(&program).unwrap())
        .map(|payload| (sha256(&payload), payload))
        .collect::<Vec<_>>();
    let program_hashes = programs
        .iter()
        .map(|(hash, _)| hash.to_vec())
        .collect::<Vec<_>>();
    let program_payloads = programs
        .iter()
        .map(|(_, payload)| payload.clone())
        .collect::<Vec<_>>();
    raw.execute(
        "INSERT INTO atomic_programs (program_hash, kind, arity, payload) \
         SELECT program_hash, 2, 0, payload \
           FROM unnest($1::bytea[], $2::bytea[]) AS values(program_hash, payload) \
         ON CONFLICT DO NOTHING",
        &[&program_hashes, &program_payloads],
    )
    .unwrap();
    raw.execute(
        "INSERT INTO atomic_program_generation_refs \
                (database_id, log_generation, program_hash) \
         SELECT $1, 0, program_hash FROM unnest($2::bytea[]) AS program_hash \
         ON CONFLICT DO NOTHING",
        &[&database_id, &program_hashes],
    )
    .unwrap();

    assert!(
        operator
            .garbage_inventory(Duration::ZERO)
            .unwrap()
            .log_generations
            .iter()
            .all(|candidate| candidate.database_id != database_id)
    );
    assert_eq!(
        old_snapshot
            .database_value()
            .values(user(42), ITEM_VALUE)
            .unwrap()
            .len(),
        1
    );
    drop(old_snapshot);
    drop(peer);
    assert!(
        operator
            .garbage_inventory(RECOMMENDED_GARBAGE_COLLECTION_AGE)
            .unwrap()
            .log_generations
            .iter()
            .all(|candidate| candidate.database_id != database_id)
    );

    let first = preview_next_log_generation(&mut operator, &database_id, 0);
    let first_candidate = first
        .log_generations
        .iter()
        .find(|candidate| candidate.database_id == database_id && candidate.generation == 0)
        .unwrap();
    assert_eq!(
        (
            first_candidate.collection_phase,
            first_candidate.rows_removed,
            first_candidate.is_complete,
        ),
        (0, 1, false)
    );
    let mut expected_first = first.clone();
    expected_first.applied = true;
    assert_eq!(
        operator.collect_garbage(Duration::ZERO).unwrap(),
        expected_first
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_index_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_index_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );
    assert!(
        raw.query_one(
            "SELECT count(*) FROM atomic_transactions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0)
            > 0
    );

    // A new operator process resumes from the durable phase row; no
    // in-memory cursor participates in collection correctness. Once the
    // first phase has claimed the generation and closed new readers, changing
    // back to the normal retention policy cannot strand the durable cursor.
    drop(operator);
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let resumed_at_normal_age = preview_next_log_generation_at_age(
        &mut operator,
        &database_id,
        0,
        RECOMMENDED_GARBAGE_COLLECTION_AGE,
    );
    apply_exact_inventory(&mut operator, &resumed_at_normal_age);
    let mut phase_five_batches = Vec::new();
    let mut completed = false;
    for _ in 0..64 {
        let dry = preview_next_log_generation(&mut operator, &database_id, 0);
        let candidate = dry
            .log_generations
            .iter()
            .find(|candidate| candidate.database_id == database_id && candidate.generation == 0)
            .unwrap()
            .clone();
        if candidate.collection_phase < 4
            || (candidate.collection_phase == 4 && candidate.rows_removed == 0)
        {
            assert!(
                raw.query_one(
                    "SELECT count(*) FROM atomic_transactions WHERE database_id = $1",
                    &[&database_id],
                )
                .unwrap()
                .get::<_, i64>(0)
                    > 0,
                "legacy transactions outlived their flat-index prerequisites"
            );
        }
        if candidate.collection_phase == 5 && candidate.rows_removed > 0 {
            phase_five_batches.push(candidate.rows_removed);
        }
        let mut expected = dry.clone();
        expected.applied = true;
        assert_eq!(operator.collect_garbage(Duration::ZERO).unwrap(), expected);
        if candidate.is_complete {
            completed = true;
            break;
        }
    }
    assert!(completed);
    assert_eq!(
        phase_five_batches,
        vec![MAX_LOG_GENERATION_ROWS_PER_GC as u64, 1]
    );
    for table in [
        "atomic_index_publications",
        "atomic_index_manifests",
        "atomic_requests",
        "atomic_transactions",
        "atomic_program_generation_refs",
        "atomic_log_generation_retirements",
    ] {
        let count: i64 = raw
            .query_one(
                &format!("SELECT count(*) FROM {table} WHERE database_id = $1"),
                &[&database_id],
            )
            .unwrap()
            .get(0);
        assert_eq!(count, 0, "{table} retained generation-zero rows");
    }
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        receipt.basis_t
    );
}

#[test]
fn retired_generation_collection_preserves_shared_atlc_content() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_shared_atlc");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let seeded = common::transact(
        &service,
        "shared-atlc-seed",
        created.basis_t(),
        &[
            add(unique_long()),
            TxOp::Add {
                entity: EntityRef::Id(user(43)),
                attribute: ITEM_VALUE,
                value: TxValue::Scalar(Value::Long(unique_long())),
            },
        ],
        1_000,
    );
    let first_request = common::transact(
        &service,
        "shared-atlc-first-excision",
        seeded.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("shared-atlc-first-request".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(user(42))),
        }],
        2_000,
    );
    service.shutdown();
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let first = operator.process_excision_requests(&database_id).unwrap();
    assert_eq!(first.basis_t, first_request.basis_t);

    let service = common::start_service(&connection, &database_id);
    let second_request = common::transact(
        &service,
        "shared-atlc-second-excision",
        first.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("shared-atlc-second-request".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(user(43))),
        }],
        3_000,
    );
    service.shutdown();
    let old_peer = Peer::connect(&connection, &database_id, 0).unwrap();
    let old_snapshot = old_peer.snapshot();
    let second = operator.process_excision_requests(&database_id).unwrap();
    assert_eq!(second.source_generation, first.generation);
    assert_eq!(second.basis_t, second_request.basis_t);

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let shared_hash: Vec<u8> = raw
        .query_one(
            "SELECT old.content_hash \
               FROM atomic_generation_transactions old \
               JOIN atomic_generation_transactions new \
                 ON new.content_hash = old.content_hash \
              WHERE old.database_id = $1 AND old.generation = $2 \
                AND new.database_id = $1 AND new.generation = $3 \
              ORDER BY old.content_hash LIMIT 1",
            &[
                &database_id,
                &(first.generation as i64),
                &(second.generation as i64),
            ],
        )
        .unwrap()
        .get(0);
    assert!(
        operator
            .garbage_inventory(Duration::ZERO)
            .unwrap()
            .log_generations
            .iter()
            .all(|candidate| candidate.database_id != database_id
                || candidate.generation != first.generation),
        "the live old-generation snapshot must block physical membership deletion"
    );
    assert_eq!(
        old_snapshot
            .database_value()
            .values(user(43), ITEM_VALUE)
            .unwrap()
            .len(),
        1
    );
    drop(old_snapshot);
    drop(old_peer);

    // The first native generation must retire before its successor can do so;
    // then collect the generation that shares this exact ATLC value with the
    // active branch.
    collect_log_generation_to_completion(&mut operator, &database_id, first.source_generation);
    collect_log_generation_to_completion(&mut operator, &database_id, first.generation);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_transaction_contents WHERE content_hash = $1",
            &[&shared_hash],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_generation_transactions \
              WHERE database_id = $1 AND generation = $2 AND content_hash = $3",
            &[&database_id, &(second.generation as i64), &shared_hash],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        second.basis_t
    );
}

#[test]
fn superseded_inactive_generation_is_abandoned_in_restart_safe_phases() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_abandoned_generation");
    let backup_directory = std::env::temp_dir().join(unique("gc_abandoned_generation_backup"));
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let seeded = common::transact(
        &service,
        "abandoned-generation-seed",
        created.basis_t(),
        &[add(unique_long())],
        1_000,
    );
    service.shutdown();
    let mut backup = PortableBackup::connect(&connection).unwrap();
    let point = backup
        .backup_database(&database_id, &backup_directory)
        .unwrap();

    let service = common::start_service(&connection, &database_id);
    let request = common::transact(
        &service,
        "abandoned-generation-request",
        seeded.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("abandoned-generation-request".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(user(42))),
        }],
        2_000,
    );
    service.shutdown();
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let interrupted = operator
        .process_excision_requests_with_fault(&database_id, ExcisionFault::AfterCandidateStaged)
        .unwrap_err();
    assert_eq!(interrupted.code, "excision/injected-fault");
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let candidate_generation: i64 = raw
        .query_one(
            "SELECT g.generation \
               FROM atomic_log_generations g \
               JOIN atomic_log_generation_builds b \
                 ON b.database_id = g.database_id AND b.generation = g.generation \
              WHERE g.database_id = $1 AND g.build_kind = 1 \
                AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                 WHERE a.database_id = g.database_id \
                                   AND a.generation = g.generation) \
              ORDER BY g.generation DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert!(
        operator
            .garbage_inventory(Duration::ZERO)
            .unwrap()
            .log_generations
            .iter()
            .all(|candidate| candidate.database_id != database_id
                || candidate.generation != candidate_generation as u64),
        "a candidate remains resumable while its captured source is active"
    );

    // A same-lineage point restore publishes a different generation. The old
    // inactive rewrite can no longer win activation and must not leak forever.
    let restored = backup
        .restore_backup_point(
            &backup_directory,
            point.basis_t,
            point.log_generation,
            &database_id,
        )
        .unwrap();
    assert_eq!(restored.basis_t(), point.basis_t);
    assert!(restored.basis_t() < request.basis_t);
    assert!(
        operator
            .garbage_inventory(RECOMMENDED_GARBAGE_COLLECTION_AGE)
            .unwrap()
            .log_generations
            .iter()
            .all(|candidate| candidate.database_id != database_id
                || candidate.generation != candidate_generation as u64)
    );

    let first =
        preview_next_log_generation(&mut operator, &database_id, candidate_generation as u64);
    let first_candidate = first
        .log_generations
        .iter()
        .find(|candidate| {
            candidate.database_id == database_id
                && candidate.generation == candidate_generation as u64
        })
        .unwrap();
    assert!(first_candidate.abandoned);
    let mut expected = first.clone();
    expected.applied = true;
    assert_eq!(operator.collect_garbage(Duration::ZERO).unwrap(), expected);
    drop(operator);
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    collect_log_generation_to_completion(&mut operator, &database_id, candidate_generation as u64);
    for table in [
        "atomic_log_generations",
        "atomic_log_generation_builds",
        "atomic_log_generation_abandonment_progress",
        "atomic_generation_transactions",
        "atomic_generation_requests",
    ] {
        let count: i64 = raw
            .query_one(
                &format!("SELECT count(*) FROM {table} WHERE database_id = $1 AND generation = $2"),
                &[&database_id, &candidate_generation],
            )
            .unwrap()
            .get(0);
        assert_eq!(count, 0, "{table} retained an abandoned generation row");
    }
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        point.basis_t
    );
    fs::remove_dir_all(&backup_directory).unwrap();
}

#[test]
fn failed_initial_restore_is_collected_before_the_alias_is_reused() {
    let Some(connection) = connection() else {
        return;
    };
    let source = unique("gc_initial_restore_source");
    let target = unique("gc_initial_restore_target");
    let backup_directory = std::env::temp_dir().join(unique("gc_initial_restore_backup"));
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let value = unique_long();
    let committed = common::transact(
        &service,
        "initial-restore-abandonment-seed",
        created.basis_t(),
        &[add(value)],
        1_000,
    );
    service.shutdown();
    let mut backup = PortableBackup::connect(&connection).unwrap();
    backup.backup_database(&source, &backup_directory).unwrap();

    // A separate catalog is the supported renamed-restore shape: lineage IDs
    // are unique inside a catalog, while the portable archive preserves the
    // source lineage in the target catalog.
    let target_connection = isolated_catalog(&connection, "gc_initial_restore_catalog");
    let mut restore = PortableBackup::connect(&target_connection).unwrap();
    let interrupted = restore
        .restore_backup_with_fault(
            &backup_directory,
            committed.basis_t,
            &target,
            RestoreFault::BeforeCommit,
        )
        .unwrap_err();
    assert_eq!(
        interrupted.code, "backup/restore-before-activation",
        "{interrupted:?}"
    );
    let mut raw = Client::connect(&target_connection, NoTls).unwrap();
    let generation: i64 = raw
        .query_one(
            "SELECT g.generation \
               FROM atomic_log_generations g \
               JOIN atomic_log_generation_builds b \
                 ON b.database_id = g.database_id AND b.generation = g.generation \
              WHERE g.database_id = $1 AND g.build_kind = 0 \
                AND b.source_generation IS NULL \
                AND NOT EXISTS (SELECT 1 FROM atomic_heads h \
                                 WHERE h.database_id = g.database_id)",
            &[&target],
        )
        .unwrap()
        .get(0);
    let mut operator = PostgresOperator::connect(&target_connection).unwrap();
    assert!(
        operator
            .garbage_inventory(RECOMMENDED_GARBAGE_COLLECTION_AGE)
            .unwrap()
            .log_generations
            .iter()
            .all(|candidate| candidate.database_id != target),
        "the normal retention age must protect a newly interrupted initial restore"
    );
    let first = preview_next_log_generation(&mut operator, &target, generation as u64);
    assert!(first.log_generations.iter().any(|candidate| {
        candidate.database_id == target
            && candidate.generation == generation as u64
            && candidate.abandoned
    }));
    apply_exact_inventory(&mut operator, &first);
    drop(operator);

    // The durable abandonment cursor survives a process boundary. Its final
    // transaction removes generation ownership first and the unpublished
    // catalog identity last, making the alias genuinely free rather than
    // leaving a poison row that every later restore collides with.
    let mut operator = PostgresOperator::connect(&target_connection).unwrap();
    collect_log_generation_to_completion(&mut operator, &target, generation as u64);
    assert!(
        raw.query_opt(
            "SELECT 1 FROM atomic_databases WHERE database_id = $1",
            &[&target],
        )
        .unwrap()
        .is_none()
    );
    let restored = restore
        .restore_backup(&backup_directory, committed.basis_t, &target)
        .unwrap();
    assert_same_information(&restored, &committed.db_after);
    fs::remove_dir_all(&backup_directory).unwrap();
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
    let baseline_service = common::start_service(&connection, &database_id);
    let baseline_value = unique_long();
    let baseline = common::transact(
        &baseline_service,
        "abandoned-build-baseline",
        created.basis_t(),
        &[add(baseline_value)],
        1_000,
    );
    baseline_service.shutdown();

    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    let baseline_publication = indexer.consolidate().unwrap();
    let mut baseline_tree = PostgresTreeStore::connect(&connection).unwrap();
    let mut baseline_sealed = false;
    for _ in 0..64 {
        if baseline_tree
            .advance_publication_work(baseline_publication.manifest_hash)
            .unwrap()
        {
            baseline_sealed = true;
            break;
        }
    }
    assert!(
        baseline_sealed,
        "baseline publication fold did not converge"
    );
    let value = baseline_value + 1;
    let service = common::start_service(&connection, &database_id);
    common::transact(
        &service,
        "abandoned-build",
        baseline.basis_t,
        &[add(value)],
        2_000,
    );
    // With a current physical root, this tiny commit remains below the
    // background novelty threshold. Stop the service before invoking the
    // direct fault-injected indexer so the test has exactly one builder.
    service.shutdown();
    let interrupted = indexer
        .consolidate_with_fault(IndexBuildFault::AfterSegments)
        .unwrap_err();
    assert_eq!(interrupted.code, "index/injected-failure");

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let intent = raw
        .query_one(
            "SELECT i.manifest_hash FROM atomic_tree_build_intents i \
              WHERE i.database_id = $1 \
                AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications p \
                                 WHERE p.manifest_hash = i.manifest_hash)",
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
        baseline_publication.publication_revision as i64
    );

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let dry = preview_next_tree_build_intent(&mut operator, &database_id, manifest_hash);
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
    // re-upload its immutable values and publish the next revision once.
    let retried = indexer.consolidate().unwrap();
    assert_eq!(
        retried.publication_revision,
        baseline_publication.publication_revision + 1
    );
    assert_eq!(retried.manifest_hash, manifest_hash);
    let publication = raw
        .query_one(
            "SELECT max(publication_revision), \
                    count(*) FILTER (WHERE publication_revision = $2 \
                                      AND manifest_hash = $3) \
               FROM atomic_tree_publications WHERE database_id = $1",
            &[
                &database_id,
                &(retried.publication_revision as i64),
                &&retried.manifest_hash[..],
            ],
        )
        .unwrap();
    assert_eq!(
        publication.get::<_, Option<i64>>(0),
        Some(retried.publication_revision as i64)
    );
    assert_eq!(publication.get::<_, i64>(1), 1);
    let peer = Peer::connect(&connection, &database_id, 1).unwrap();
    assert_eq!(
        peer.db().values(user(42), ITEM_VALUE),
        vec![&Value::Long(value)]
    );
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
        rebound.db_after.values(function, DB_FN as u32).unwrap(),
        vec![Value::Function(current_hash)]
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
    // The administrative store caches an exact writer base after creation.
    // Release that legitimate witness so this test isolates Peer-held pins.
    drop(store);
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    let publication_one = indexer.consolidate().unwrap();
    assert_eq!(publication_one.basis_t, created.basis_t());

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

    let publication_three = republish_same_basis(&connection, &database_id);
    assert_eq!(
        publication_three.publication_revision,
        publication_two.publication_revision + 1
    );
    assert_eq!(publication_three.basis_t, created.basis_t());
    assert!(peer.refresh_index().unwrap());
    assert_eq!(
        peer.durable_base_revision(),
        publication_three.publication_revision
    );
    // Publication is root-first and membership folding is deliberately
    // bounded. Finish both small folds explicitly so an empty GC inventory
    // below can only be caused by the live snapshot pin, not by unfinished
    // retirement bookkeeping.
    let mut tree_work = PostgresTreeStore::connect(&connection).unwrap();
    let mut sealed = false;
    for _ in 0..64 {
        if tree_work
            .advance_publication_work(publication_three.manifest_hash)
            .unwrap()
        {
            sealed = true;
            break;
        }
    }
    assert!(sealed);
    let mut bookkeeping = Client::connect(&connection, NoTls).unwrap();
    assert_eq!(
        bookkeeping
            .query_one(
                "SELECT count(*) FROM atomic_tree_retirements \
                  WHERE database_id = $1 AND bookkeeping_complete \
                    AND publication_revision = ANY($2)",
                &[
                    &database_id,
                    &vec![
                        publication_one.publication_revision as i64,
                        publication_two.publication_revision as i64,
                    ],
                ],
            )
            .unwrap()
            .get::<_, i64>(0),
        2
    );

    // Database creation may already have published a genesis tree. The
    // snapshot opened at publication_one does not read that predecessor, so
    // collect every earlier root before isolating publication_one's pin.
    let earlier_revisions = bookkeeping
        .query(
            "SELECT publication_revision FROM atomic_tree_publications \
              WHERE database_id = $1 AND publication_revision < $2 \
              ORDER BY publication_revision",
            &[&database_id, &(publication_one.publication_revision as i64)],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, i64>(0) as u64)
        .collect::<Vec<_>>();
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    for revision in earlier_revisions {
        let dry = preview_next_tree_retirement(&mut operator, &database_id, revision);
        let mut expected = dry.clone();
        expected.applied = true;
        assert_eq!(operator.collect_garbage(Duration::ZERO).unwrap(), expected);
    }

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

    let dry_pinned = operator.garbage_inventory(Duration::ZERO).unwrap();
    let pinned_candidates = dry_pinned
        .tree_publications
        .iter()
        .filter(|candidate| candidate.database_id == database_id)
        .map(|candidate| candidate.publication_revision)
        .collect::<BTreeSet<_>>();
    if !pinned_candidates.is_empty() {
        let retirement_debug = raw
            .query(
                "SELECT r.publication_revision, encode(r.manifest_hash, 'hex'), \
                        r.bookkeeping_complete, r.garbage_complete, \
                        EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress \
                                 WHERE progress.database_id = r.database_id \
                                   AND progress.publication_revision = r.publication_revision \
                                   AND progress.manifest_hash = r.manifest_hash) \
                   FROM atomic_tree_retirements r \
                  WHERE r.database_id = $1 ORDER BY r.publication_revision",
                &[&database_id],
            )
            .unwrap()
            .into_iter()
            .map(|row| {
                (
                    row.get::<_, i64>(0),
                    row.get::<_, String>(1),
                    row.get::<_, bool>(2),
                    row.get::<_, bool>(3),
                    row.get::<_, bool>(4),
                )
            })
            .collect::<Vec<_>>();
        let advisory_lock_debug = raw
            .query(
                "SELECT pid, mode, granted, classid::bigint, objid::bigint, objsubid \
                   FROM pg_locks WHERE locktype = 'advisory' ORDER BY pid, mode, classid, objid",
                &[],
            )
            .unwrap()
            .into_iter()
            .map(|row| {
                (
                    row.get::<_, i32>(0),
                    row.get::<_, String>(1),
                    row.get::<_, bool>(2),
                    row.get::<_, i64>(3),
                    row.get::<_, i64>(4),
                    row.get::<_, i16>(5),
                )
            })
            .collect::<Vec<_>>();
        panic!(
            "pinned roots became collectible: candidates={pinned_candidates:?}, \
             peer_hashes={:?}, retirements={retirement_debug:?}, \
             advisory_locks={advisory_lock_debug:?}",
            peer.pinned_manifest_hashes()
        );
    }
    // Prefix retirement is intentional: a pin on revision one also blocks
    // revision two, so no later root can be reclaimed out of order.
    let mut expected_pinned = dry_pinned.clone();
    expected_pinned.applied = true;
    let applied_pinned = operator.collect_garbage(Duration::ZERO).unwrap();
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
        Vec::<Value>::new()
    );
    drop(snapshot_clones);
    drop(old_snapshot);
    assert_eq!(peer.pinned_manifest_hashes().len(), 1);

    let dry_released = preview_next_tree_retirement(
        &mut operator,
        &database_id,
        publication_one.publication_revision,
    );
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
    assert!(released.tree_publications.iter().any(|candidate| {
        candidate.database_id == database_id
            && candidate.publication_revision == publication_one.publication_revision
    }));
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        2
    );

    let dry_changed = preview_next_tree_retirement(
        &mut operator,
        &database_id,
        publication_two.publication_revision,
    );
    assert_eq!(
        dry_changed
            .tree_publications
            .iter()
            .filter(|candidate| candidate.database_id == database_id)
            .map(|candidate| candidate.publication_revision)
            .collect::<Vec<_>>(),
        vec![publication_two.publication_revision]
    );
    let mut expected_changed = dry_changed.clone();
    expected_changed.applied = true;
    let changed = operator.collect_garbage(Duration::ZERO).unwrap();
    assert_eq!(changed, expected_changed);
    assert!(changed.tree_publications.iter().any(|candidate| {
        candidate.database_id == database_id
            && candidate.publication_revision == publication_two.publication_revision
    }));
    // These are semantically and physically identical republishes: roots are
    // reclaimable, while their globally shared immutable nodes stay live.
    assert!(changed.tree_node_hashes.is_empty());
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
        Vec::<Value>::new()
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
        Vec::<Value>::new()
    );
}

#[test]
fn native_request_base_pins_active_root_and_releases_before_generation_gc() {
    let Some(base_connection) = connection() else {
        return;
    };
    let connection = isolated_catalog(&base_connection, "gc_request_base_catalog");
    let database_id = unique("gc_request_base");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, Schema::new()).unwrap();
    assert_eq!(created.basis_t(), 0);

    // Every new positive generation has an authenticated semantic root at
    // genesis.  Publish that t0 value so the later request can name its exact
    // db-before base without depending on an ordinary-writer implementation
    // that this migration slice deliberately does not change yet.
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    let genesis_publication = indexer.consolidate().unwrap();
    assert_eq!(genesis_publication.basis_t, 0);

    let service = common::start_service(&connection, &database_id);
    let schema_ops = schema()
        .attributes()
        .cloned()
        .map(TxOp::InstallAttribute)
        .collect::<Vec<_>>();
    let installed = common::transact(&service, "request-base-schema", 0, &schema_ops, 500);
    let committed = common::transact(
        &service,
        "request-base-native",
        installed.basis_t,
        &[add(unique_long())],
        1_000,
    );
    service.shutdown();

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let request = raw
        .query_one(
            "SELECT request.generation, request.request_key_hash, request.request_kind, \
                    base.base_manifest_hash \
               FROM atomic_generation_requests request \
               LEFT JOIN atomic_generation_request_bases base \
                 ON base.database_id = request.database_id \
                AND base.generation = request.generation \
                AND base.request_key_hash = request.request_key_hash \
              WHERE request.database_id = $1 AND request.basis_t = $2",
            &[&database_id, &(committed.basis_t as i64)],
        )
        .unwrap();
    let source_generation: i64 = request.get(0);
    assert_eq!(source_generation, 1);
    assert_eq!(request.get::<_, i16>(2), 2);
    assert_eq!(
        request.get::<_, Option<Vec<u8>>>(3),
        Some(genesis_publication.manifest_hash.to_vec())
    );

    // The native writer publishes the request, its exact db-before base, the
    // transaction, semantic coordinate, and head in one PostgreSQL commit.
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let complete = operator.inspect_database(&database_id, false).unwrap();
    assert!(
        complete
            .problems
            .iter()
            .all(|problem| problem.code != "integrity/invalid-request-base"),
        "native writer produced an invalid request base: {:?}",
        complete.problems
    );

    // Authenticated recovery must treat kind 2 as an ordinary transaction,
    // never as an excision replay marker.
    assert_same_information(&store.recover(&database_id).unwrap(), &committed.db_after);

    let successor_publication = indexer.consolidate().unwrap();
    assert!(successor_publication.basis_t >= committed.basis_t);
    let mut publication_work = PostgresTreeStore::connect(&connection).unwrap();
    let mut folded = false;
    for _ in 0..64 {
        if publication_work
            .advance_publication_work(successor_publication.manifest_hash)
            .unwrap()
        {
            folded = true;
            break;
        }
    }
    assert!(folded);

    // The durable association is a real root pin even without a connected
    // Peer session.  Preview and the owner SQL function agree that the active
    // generation's oldest root cannot be claimed.
    let active_inventory = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(active_inventory.tree_publications.iter().all(|candidate| {
        candidate.database_id != database_id
            || candidate.manifest_hash != genesis_publication.manifest_hash
    }));
    let collected: bool = raw
        .query_one(
            "SELECT atomic_collect_tree_retirement($1, $2, $3, 0, 512)",
            &[
                &database_id,
                &(genesis_publication.publication_revision as i64),
                &&genesis_publication.manifest_hash[..],
            ],
        )
        .unwrap()
        .get(0);
    assert!(!collected);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_retirement_progress \
              WHERE manifest_hash = $1",
            &[&&genesis_publication.manifest_hash[..]],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );

    // A real excision activation retires generation one.  Its kind-2 request
    // is authenticated as ordinary source information and rewritten as a
    // tombstone in the successor; no binding is copied to that tombstone.
    let service = common::start_service(&connection, &database_id);
    let requested = common::transact(
        &service,
        "request-base-excision",
        committed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("request-base-excision".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(user(42))),
        }],
        2_000,
    );
    service.shutdown();
    let activated = operator.process_excision_requests(&database_id).unwrap();
    assert_eq!(activated.source_generation, source_generation as u64);
    assert_eq!(activated.basis_t, requested.basis_t);
    let activated_manifest = raw
        .query_one(
            "SELECT manifest_hash FROM atomic_tree_publications \
              WHERE database_id = $1 AND log_generation = $2 \
              ORDER BY publication_revision DESC LIMIT 1",
            &[&database_id, &(activated.generation as i64)],
        )
        .unwrap()
        .get::<_, Vec<u8>>(0);
    let activated_manifest = <Digest>::try_from(activated_manifest).unwrap();
    let mut folded = false;
    for _ in 0..64 {
        if publication_work
            .advance_publication_work(activated_manifest)
            .unwrap()
        {
            folded = true;
            break;
        }
    }
    assert!(folded);
    drop(publication_work);
    drop(indexer);
    // Transaction reports intentionally hold exact immutable db-before and
    // db-after values. They are live report witnesses, and therefore retain
    // the old manifest/generation until the application releases them.
    let report_pinned = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(report_pinned.log_generations.iter().all(|candidate| {
        candidate.database_id != database_id || candidate.generation != source_generation as u64
    }));
    assert!(report_pinned.semantic_commitment_roots.iter().all(|root| {
        root.database_id != database_id || root.generation != source_generation as u64
    }));
    drop(installed);
    drop(committed);
    drop(requested);

    // While the tree dependency remains, the retired log generation is not a
    // generation-GC candidate.  Root GC releases the now-unresolvable request
    // binding at publication deletion, after which the existing phased log
    // collector can become eligible without a dependency cycle.
    assert!(
        operator
            .garbage_inventory(Duration::ZERO)
            .unwrap()
            .log_generations
            .iter()
            .all(|candidate| candidate.database_id != database_id
                || candidate.generation != source_generation as u64)
    );
    let mut generation_became_collectible = false;
    for _ in 0..MAX_TEST_GC_STEPS {
        let inventory = operator.garbage_inventory(Duration::ZERO).unwrap();
        if inventory.log_generations.iter().any(|candidate| {
            candidate.database_id == database_id && candidate.generation == source_generation as u64
        }) {
            generation_became_collectible = true;
            break;
        }
        assert!(
            inventory_has_work(&inventory),
            "request-base retirement made no progress before generation GC: {inventory:?}"
        );
        apply_exact_inventory(&mut operator, &inventory);
    }
    assert!(generation_became_collectible);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_generation_request_bases \
              WHERE database_id = $1 AND generation = $2",
            &[&database_id, &source_generation],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );

    // The log is now terminal, but its semantic coordinates still protect the
    // old immutable database values.  They are the first bounded generation
    // phase; active and unrelated roots are not database-local garbage.
    let active_generation = activated.generation as i64;
    let old_root_count: i64 = raw
        .query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2",
            &[&database_id, &source_generation],
        )
        .unwrap()
        .get(0);
    assert!(old_root_count > 0);
    let active_root_count: i64 = raw
        .query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2",
            &[&database_id, &active_generation],
        )
        .unwrap()
        .get(0);
    assert!(active_root_count > 0);
    let other_database = unique("gc_semantic_other");
    store
        .create_database(&other_database, Schema::new())
        .unwrap();
    let other_root_count: i64 = raw
        .query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1",
            &[&other_database],
        )
        .unwrap()
        .get(0);
    assert!(other_root_count > 0);

    let old_nodes = raw
        .query(
            "WITH RECURSIVE reachable(node_hash) AS ( \
                 SELECT current_root FROM atomic_semantic_commitment_roots \
                  WHERE database_id = $1 AND generation = $2 \
                    AND current_root IS NOT NULL \
                 UNION \
                 SELECT child.node_hash FROM reachable r \
                   JOIN atomic_semantic_commitment_nodes parent \
                     ON parent.node_hash = r.node_hash \
                   CROSS JOIN LATERAL \
                     (VALUES (parent.left_hash), (parent.right_hash)) child(node_hash) \
                  WHERE child.node_hash IS NOT NULL \
             ) SELECT node_hash FROM reachable",
            &[&database_id, &source_generation],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, Vec<u8>>(0))
        .collect::<BTreeSet<_>>();
    let other_live_nodes = raw
        .query(
            "WITH RECURSIVE reachable(node_hash) AS ( \
                 SELECT current_root FROM atomic_semantic_commitment_roots \
                  WHERE NOT (database_id = $1 AND generation = $2) \
                    AND current_root IS NOT NULL \
                 UNION \
                 SELECT child.node_hash FROM reachable r \
                   JOIN atomic_semantic_commitment_nodes parent \
                     ON parent.node_hash = r.node_hash \
                   CROSS JOIN LATERAL \
                     (VALUES (parent.left_hash), (parent.right_hash)) child(node_hash) \
                  WHERE child.node_hash IS NOT NULL \
             ) SELECT node_hash FROM reachable",
            &[&database_id, &source_generation],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, Vec<u8>>(0))
        .collect::<BTreeSet<_>>();
    let old_only_nodes = old_nodes
        .difference(&other_live_nodes)
        .cloned()
        .collect::<Vec<_>>();
    let shared_nodes = old_nodes
        .intersection(&other_live_nodes)
        .cloned()
        .collect::<Vec<_>>();
    assert!(
        !old_only_nodes.is_empty(),
        "excision produced no old-generation-only semantic nodes"
    );
    assert!(
        !shared_nodes.is_empty(),
        "semantic commitments did not structurally share any retained nodes"
    );

    let semantic_dry =
        preview_next_log_generation(&mut operator, &database_id, source_generation as u64);
    let semantic_phase = semantic_dry
        .log_generations
        .iter()
        .find(|candidate| {
            candidate.database_id == database_id && candidate.generation == source_generation as u64
        })
        .unwrap();
    assert_eq!(semantic_phase.semantic_roots_removed, old_root_count as u64);
    assert_eq!(semantic_phase.rows_removed, old_root_count as u64);
    assert_eq!(
        semantic_dry
            .semantic_commitment_roots
            .iter()
            .filter(|root| {
                root.database_id == database_id && root.generation == source_generation as u64
            })
            .count(),
        old_root_count as usize
    );

    // Even the owner cannot mutate a root outside the guarded collector.
    let immutable = raw
        .execute(
            "DELETE FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2 AND basis_t = ( \
                    SELECT min(basis_t) FROM atomic_semantic_commitment_roots \
                     WHERE database_id = $1 AND generation = $2)",
            &[&database_id, &source_generation],
        )
        .unwrap_err();
    assert_eq!(immutable.code().unwrap().code(), "55000");

    // Claim, deletion, and cursor creation are one transaction. A crash-like
    // rollback restores every root and leaves the exact dry run repeatable.
    let mut rollback = raw.transaction().unwrap();
    let rolled_back = rollback
        .query(
            "SELECT basis_t FROM atomic_collect_semantic_commitment_generation_roots(\
                 $1, $2, 0, 512, false)",
            &[&database_id, &source_generation],
        )
        .unwrap();
    assert_eq!(rolled_back.len(), old_root_count as usize);
    rollback.rollback().unwrap();
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2",
            &[&database_id, &source_generation],
        )
        .unwrap()
        .get::<_, i64>(0),
        old_root_count
    );

    apply_exact_inventory(&mut operator, &semantic_dry);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2",
            &[&database_id, &source_generation],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2",
            &[&database_id, &active_generation],
        )
        .unwrap()
        .get::<_, i64>(0),
        active_root_count
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1",
            &[&other_database],
        )
        .unwrap()
        .get::<_, i64>(0),
        other_root_count
    );

    collect_log_generation_to_completion(&mut operator, &database_id, source_generation as u64);
    for attempt in 0..MAX_TEST_GC_STEPS {
        let remaining: i64 = raw
            .query_one(
                "SELECT count(*) FROM atomic_semantic_commitment_nodes \
                  WHERE node_hash = ANY($1)",
                &[&old_only_nodes],
            )
            .unwrap()
            .get(0);
        if remaining == 0 {
            break;
        }
        let dry = operator.garbage_inventory(Duration::ZERO).unwrap();
        assert!(
            inventory_has_work(&dry),
            "{remaining} old semantic nodes remained without GC work at attempt {attempt}"
        );
        apply_exact_inventory(&mut operator, &dry);
    }
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_nodes \
              WHERE node_hash = ANY($1)",
            &[&old_only_nodes],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_nodes \
              WHERE node_hash = ANY($1)",
            &[&shared_nodes],
        )
        .unwrap()
        .get::<_, i64>(0),
        shared_nodes.len() as i64
    );
}

#[test]
fn large_replacement_publishes_root_before_bounded_membership_fold() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_bounded_publication_fold");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut kernel = PostgresStore::connect(&connection).unwrap();
    let database = kernel.create_database(&database_id, schema()).unwrap();
    drop(kernel);

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let head = raw
        .query_one(
            "SELECT h.basis_t, h.tx_hash, COALESCE(legacy.state_hash, native.state_hash), \
                    h.log_generation \
               FROM atomic_heads h \
               LEFT JOIN atomic_transactions legacy \
                 ON h.log_generation = 0 \
                AND legacy.database_id = h.database_id \
                AND legacy.basis_t = h.basis_t AND legacy.tx_hash = h.tx_hash \
               LEFT JOIN atomic_generation_transactions native \
                 ON h.log_generation > 0 \
                AND native.database_id = h.database_id \
                AND native.generation = h.log_generation \
                AND native.basis_t = h.basis_t AND native.tx_hash = h.tx_hash \
              WHERE h.database_id = $1",
            &[&database_id],
        )
        .unwrap();
    let basis_t = head.get::<_, i64>(0) as u64;
    let tx_hash: Digest = head.get::<_, Vec<u8>>(1).try_into().unwrap();
    let state_hash: Digest = head.get::<_, Vec<u8>>(2).try_into().unwrap();
    let log_generation = head.get::<_, i64>(3) as u64;

    let values = (0..(MAX_TREE_RETIREMENT_NODES_PER_GC * 2 + 1))
        .map(|ordinal| format!("replacement-{database_id}-{ordinal}").into_bytes())
        .map(|payload| (sha256(&payload), payload))
        .collect::<Vec<_>>();
    let live_nodes = values
        .iter()
        .map(|(hash, _)| *hash)
        .collect::<BTreeSet<_>>();
    let mut roots = Vec::new();
    for ((history, order), (root_hash, payload)) in [false, true]
        .into_iter()
        .flat_map(|history| {
            [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ]
            .into_iter()
            .map(move |order| (history, order))
        })
        .zip(values.iter())
    {
        roots.push(TreeRootBinding {
            order,
            history,
            root_hash: *root_hash,
            datom_count: 0,
            encoded_bytes: payload.len() as u64,
        });
    }
    let manifest_payload = format!("bounded-replacement-manifest-{database_id}").into_bytes();
    let manifest = TreeManifestRecord {
        database_id: database_id.clone(),
        publication_revision: 1,
        basis_t,
        tx_hash,
        state_hash,
        excision_generation: log_generation,
        eidx_frontier: database.eidx_frontier(),
        manifest_hash: sha256(&manifest_payload),
        payload: manifest_payload,
        roots,
    };
    let mut trees = PostgresTreeStore::connect(&connection).unwrap();
    trees
        .begin_build_intent(
            &database_id,
            log_generation,
            0,
            manifest.manifest_hash,
            &live_nodes,
        )
        .unwrap();
    for (hash, payload) in &values {
        trees.insert_node(*hash, payload).unwrap();
    }
    assert_eq!(
        trees
            .publish_manifest_with_delta(
                &manifest,
                0,
                &TreePublicationDelta::Replace {
                    live_nodes: live_nodes.clone(),
                },
            )
            .unwrap(),
        TreePublishOutcome::Published
    );

    // Publishing made the immutable root visible and performed only one
    // bounded opportunistic fold; it did not consume the O(database) ledgers
    // while holding the root serialization lock.
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_publications \
              WHERE database_id = $1 AND manifest_hash = $2",
            &[&database_id, &&manifest.manifest_hash[..]],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );
    let remaining_after_root = raw
        .query_one(
            "SELECT count(*) FROM atomic_tree_delta_nodes WHERE manifest_hash = $1",
            &[&&manifest.manifest_hash[..]],
        )
        .unwrap()
        .get::<_, i64>(0);
    assert_eq!(
        remaining_after_root,
        (values.len() - MAX_TREE_RETIREMENT_NODES_PER_GC) as i64
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_build_intent_nodes WHERE manifest_hash = $1",
            &[&&manifest.manifest_hash[..]],
        )
        .unwrap()
        .get::<_, i64>(0),
        values.len() as i64
    );

    let mut previous = remaining_after_root;
    while !trees
        .advance_publication_work(manifest.manifest_hash)
        .unwrap()
    {
        let remaining = raw
            .query_one(
                "SELECT count(*) FROM atomic_tree_delta_nodes WHERE manifest_hash = $1",
                &[&&manifest.manifest_hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0);
        assert!(previous - remaining <= MAX_TREE_RETIREMENT_NODES_PER_GC as i64);
        assert!(remaining < previous);
        previous = remaining;
    }
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_delta_headers WHERE manifest_hash = $1",
            &[&&manifest.manifest_hash[..]],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    let sealed = raw
        .query_one(
            "SELECT l.complete, count(n.node_hash) \
               FROM atomic_tree_live_sets l \
               JOIN atomic_tree_live_nodes n ON n.database_id = l.database_id \
              WHERE l.database_id = $1 AND l.manifest_hash = $2 \
              GROUP BY l.complete",
            &[&database_id, &&manifest.manifest_hash[..]],
        )
        .unwrap();
    assert!(sealed.get::<_, bool>(0));
    assert_eq!(sealed.get::<_, i64>(1), values.len() as i64);
    trees.release_build_intent().unwrap();
}

#[test]
fn abandoned_intent_ledger_is_staged_and_collected_in_bounded_batches() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_bounded_intent");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.create_database(&database_id, schema()).unwrap();
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let log_generation = raw
        .query_one(
            "SELECT log_generation FROM atomic_heads WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0) as u64;
    let values = (0..MAX_TREE_BUILD_INTENT_NODES_PER_GC + 1)
        .map(|ordinal| format!("abandoned-{database_id}-{ordinal}").into_bytes())
        .map(|payload| (sha256(&payload), payload))
        .collect::<Vec<_>>();
    let hashes = values
        .iter()
        .map(|(hash, _)| *hash)
        .collect::<BTreeSet<_>>();
    let manifest_hash = sha256(format!("manifest-{database_id}").as_bytes());
    let mut trees = PostgresTreeStore::connect(&connection).unwrap();
    trees
        .begin_build_intent(&database_id, log_generation, 0, manifest_hash, &hashes)
        .unwrap();
    assert_eq!(trees.stats().build_intent_node_batches, 2);
    assert_eq!(trees.stats().build_intent_node_writes, values.len() as u64);
    for (hash, payload) in &values {
        trees.insert_node(*hash, payload).unwrap();
    }
    trees.release_build_intent().unwrap();
    let staged = raw
        .query_one(
            "SELECT intent_state, staged_node_count, expected_node_count \
               FROM atomic_tree_build_intents WHERE manifest_hash = $1",
            &[&&manifest_hash[..]],
        )
        .unwrap();
    assert_eq!(staged.get::<_, i16>(0), 1);
    assert_eq!(staged.get::<_, i64>(1), values.len() as i64);
    assert_eq!(staged.get::<_, i64>(2), values.len() as i64);

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let dry_first = preview_next_tree_build_intent(&mut operator, &database_id, manifest_hash);
    assert!(dry_first.tree_build_intents.iter().any(|intent| {
        intent.database_id == database_id
            && intent.manifest_hash == manifest_hash
            && intent.abandoned
    }));
    assert_eq!(
        dry_first
            .tree_node_hashes
            .iter()
            .filter(|hash| hashes.contains(*hash))
            .count(),
        MAX_TREE_BUILD_INTENT_NODES_PER_GC
    );
    let mut expected_first = dry_first.clone();
    expected_first.applied = true;
    assert_eq!(
        operator.collect_garbage(Duration::ZERO).unwrap(),
        expected_first
    );
    let after_first = raw
        .query_one(
            "SELECT intent_state, \
                    (SELECT count(*) FROM atomic_tree_build_intent_nodes n \
                      WHERE n.manifest_hash = i.manifest_hash) \
               FROM atomic_tree_build_intents i WHERE manifest_hash = $1",
            &[&&manifest_hash[..]],
        )
        .unwrap();
    assert_eq!(after_first.get::<_, i16>(0), 3);
    assert_eq!(after_first.get::<_, i64>(1), 1);

    let dry_second = preview_next_tree_build_intent(&mut operator, &database_id, manifest_hash);
    assert_eq!(
        dry_second
            .tree_node_hashes
            .iter()
            .filter(|hash| hashes.contains(*hash))
            .count(),
        1
    );
    let mut expected_second = dry_second.clone();
    expected_second.applied = true;
    assert_eq!(
        operator.collect_garbage(Duration::ZERO).unwrap(),
        expected_second
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_build_intents WHERE manifest_hash = $1",
            &[&&manifest_hash[..]],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    let hash_bytes = hashes.iter().map(|hash| hash.to_vec()).collect::<Vec<_>>();
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_nodes WHERE node_hash = ANY($1)",
            &[&hash_bytes],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
}

#[test]
fn claimed_retirement_ledger_is_unobservable_and_drains_in_bounded_batches() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_bounded_retirement");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    // The administrative store caches an exact writer base after creation.
    // Release that legitimate witness so this test isolates Peer-held pins.
    drop(store);
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    let publication_one = indexer.consolidate().unwrap();
    assert_eq!(publication_one.basis_t, created.basis_t());
    let peer = Peer::connect_with_cache_limits(&connection, &database_id, 0, 0).unwrap();
    let old_snapshot = peer.snapshot();
    republish_same_basis(&connection, &database_id);

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    // Successful intent ledgers are bookkeeping, not garbage. Drain them
    // while the old root is pinned so this test isolates retirement work.
    drain_tree_build_intents_for_database(&mut operator, &mut raw, &database_id);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_build_intents WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );

    // Inject a large exact old-minus-new witness onto the otherwise empty
    // same-basis delta. This is owner-level fixture setup; the collector must
    // still honor the ordinary immutable guards and its configured batch.
    let values = (0..MAX_TREE_RETIREMENT_NODES_PER_GC + 1)
        .map(|ordinal| format!("retired-{database_id}-{ordinal}").into_bytes())
        .map(|payload| (sha256(&payload), payload))
        .collect::<Vec<_>>();
    let hash_bytes = values
        .iter()
        .map(|(hash, _)| hash.to_vec())
        .collect::<Vec<_>>();
    let payloads = values
        .iter()
        .map(|(_, payload)| payload.clone())
        .collect::<Vec<_>>();
    let retired_set = values
        .iter()
        .map(|(hash, _)| *hash)
        .collect::<BTreeSet<_>>();
    let mut fixture = raw.transaction().unwrap();
    fixture
        .execute(
            "INSERT INTO atomic_tree_nodes (node_hash, payload) \
             SELECT * FROM unnest($1::bytea[], $2::bytea[])",
            &[&hash_bytes, &payloads],
        )
        .unwrap();
    fixture
        .execute(
            "INSERT INTO atomic_tree_retired_nodes \
                    (database_id, publication_revision, node_hash) \
             SELECT $1, $2, node_hash FROM unnest($3::bytea[]) AS node_hash",
            &[
                &database_id,
                &(publication_one.publication_revision as i64),
                &hash_bytes,
            ],
        )
        .unwrap();
    fixture.commit().unwrap();
    drop(old_snapshot);
    drop(peer);

    let dry_first = preview_next_tree_retirement(
        &mut operator,
        &database_id,
        publication_one.publication_revision,
    );
    assert!(dry_first.tree_publications.iter().any(|publication| {
        publication.database_id == database_id
            && publication.publication_revision == publication_one.publication_revision
    }));
    assert!(
        !dry_first
            .tree_manifest_hashes
            .contains(&publication_one.manifest_hash)
    );
    assert_eq!(
        dry_first
            .tree_node_hashes
            .iter()
            .filter(|hash| retired_set.contains(*hash))
            .count(),
        MAX_TREE_RETIREMENT_NODES_PER_GC
    );
    let mut expected_first = dry_first.clone();
    expected_first.applied = true;
    assert_eq!(
        operator.collect_garbage(Duration::ZERO).unwrap(),
        expected_first
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_retired_nodes \
              WHERE database_id = $1 AND publication_revision = $2",
            &[&database_id, &(publication_one.publication_revision as i64),],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_retirement_progress \
              WHERE database_id = $1 AND publication_revision = $2",
            &[&database_id, &(publication_one.publication_revision as i64),],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );

    let dry_second = preview_next_tree_retirement(
        &mut operator,
        &database_id,
        publication_one.publication_revision,
    );
    assert!(
        dry_second
            .tree_manifest_hashes
            .contains(&publication_one.manifest_hash)
    );
    assert_eq!(
        dry_second
            .tree_node_hashes
            .iter()
            .filter(|hash| retired_set.contains(*hash))
            .count(),
        1
    );
    let mut expected_second = dry_second.clone();
    expected_second.applied = true;
    assert_eq!(
        operator.collect_garbage(Duration::ZERO).unwrap(),
        expected_second
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_publications \
              WHERE database_id = $1 AND publication_revision = $2",
            &[&database_id, &(publication_one.publication_revision as i64),],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_tree_nodes WHERE node_hash = ANY($1)",
            &[&hash_bytes],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
}
