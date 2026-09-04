mod common;

use atomic_core::persistent_tree::TreeDescriptor;
use atomic_core::{
    Attribute, Cardinality, IndexOrder, Keyword, ManifestTree, PersistentTreeManifest,
    PostgresStore, PostgresTreeStore, Schema, TreeManifestRecord, TreePublishOutcome,
    TreeRootBinding, ValueType, sha256,
};
use postgres::{Client, NoTls};
use std::time::{SystemTime, UNIX_EPOCH};

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
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

fn digest(bytes: Vec<u8>) -> [u8; 32] {
    bytes.try_into().unwrap()
}

fn stage_unknown_delta(
    client: &mut Client,
    manifest_hash: [u8; 32],
    predecessor_manifest_hash: Option<[u8; 32]>,
) {
    let mut empty_set = b"atomic/tree-build-node-set/v1\0".to_vec();
    empty_set.extend_from_slice(&0_u64.to_be_bytes());
    let empty_set_hash = sha256(&empty_set);
    let mut delta_set = b"atomic/tree-publication-delta/v1\0".to_vec();
    delta_set.extend_from_slice(&0_i16.to_be_bytes());
    delta_set.extend_from_slice(&0_u64.to_be_bytes());
    let delta_set_hash = sha256(&delta_set);
    let predecessor = predecessor_manifest_hash
        .as_ref()
        .map(|hash| hash.as_slice());
    client
        .execute(
            "INSERT INTO atomic_tree_delta_headers \
               (manifest_hash, predecessor_manifest_hash, delta_mode, \
                expected_node_count, staged_node_count, delta_set_hash, \
                added_node_count, added_set_hash, delta_state) \
             VALUES ($1, $2, 0, 0, 0, $3, 0, $4, 1)",
            &[
                &&manifest_hash[..],
                &predecessor,
                &&delta_set_hash[..],
                &&empty_set_hash[..],
            ],
        )
        .unwrap();
}

#[allow(clippy::too_many_arguments)]
fn manifest_payload(
    database_id: &str,
    publication_revision: u64,
    basis_t: u64,
    tx_hash: [u8; 32],
    state_hash: [u8; 32],
    excision_generation: u64,
    eidx_frontier: u64,
    roots: &[TreeRootBinding],
) -> Vec<u8> {
    PersistentTreeManifest {
        database_id: database_id.to_owned(),
        publication_revision,
        basis_t,
        tx_hash,
        state_hash,
        excision_generation,
        eidx_frontier,
        trees: roots
            .iter()
            .map(|root| ManifestTree {
                descriptor: TreeDescriptor {
                    root_hash: root.root_hash,
                    order: root.order,
                    history: root.history,
                    count: root.datom_count,
                    first_hash: None,
                    last_hash: None,
                },
                root_bytes: root.encoded_bytes,
            })
            .collect(),
        pending_avet: Vec::new(),
    }
    .encode()
    .unwrap()
}

#[test]
fn tree_content_is_idempotent_and_publication_is_root_last() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("tree_store");
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("tree-test", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut kernel = PostgresStore::connect(&connection).unwrap();
    kernel.create_database(&database_id, schema).unwrap();
    drop(kernel);

    // Begin above the smallest positive basis so the same fixture can prove
    // that physical publication rejects a later logical-basis regression.
    let service = common::start_service(&connection, &database_id);
    let tx_instant = i64::try_from(
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_millis(),
    )
    .unwrap();
    let report = common::transact(&service, "tree-store-empty-successor", 1, &[], tx_instant);
    assert_eq!(report.basis_t, 2);
    let database = report.db_after;
    service.shutdown();

    let mut metadata = Client::connect(&connection, NoTls).unwrap();
    let row = metadata
        .query_one(
            "SELECT h.basis_t, h.tx_hash, t.state_hash, h.log_generation \
               FROM atomic_heads h \
               JOIN atomic_generation_transactions t \
                 ON t.database_id = h.database_id AND t.generation = h.log_generation \
                AND t.basis_t = h.basis_t AND t.tx_hash = h.tx_hash \
              WHERE h.database_id = $1",
            &[&database_id],
        )
        .unwrap();
    let basis_t = u64::try_from(row.get::<_, i64>(0)).unwrap();
    let tx_hash = digest(row.get(1));
    let state_hash = digest(row.get(2));
    let generation = u64::try_from(row.get::<_, i64>(3)).unwrap();
    drop(metadata);

    let mut store = PostgresTreeStore::connect(&connection).unwrap();
    let expected_revision = store.current_publication_revision(&database_id).unwrap();
    let mut roots = Vec::new();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            // The structural codec owns real ATIX bytes. These opaque values
            // isolate and exercise the PostgreSQL content/root contract.
            let payload =
                format!("ATIX-store-fixture-{database_id}-{history}-{order:?}").into_bytes();
            let root_hash = sha256(&payload);
            store.insert_node(root_hash, &payload).unwrap();
            roots.push(TreeRootBinding {
                order,
                history,
                root_hash,
                datom_count: 0,
                encoded_bytes: payload.len() as u64,
            });
        }
    }

    let mut observer = Client::connect(&connection, NoTls).unwrap();
    let before_publication: i64 = observer
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        u64::try_from(before_publication).unwrap(),
        expected_revision
    );
    drop(observer);

    let publication_revision = expected_revision + 1;
    let payload = manifest_payload(
        &database_id,
        publication_revision,
        basis_t,
        tx_hash,
        state_hash,
        generation,
        database.eidx_frontier(),
        &roots,
    );
    let manifest = TreeManifestRecord {
        database_id: database_id.clone(),
        publication_revision,
        basis_t,
        tx_hash,
        state_hash,
        excision_generation: generation,
        eidx_frontier: database.eidx_frontier(),
        manifest_hash: sha256(&payload),
        payload,
        roots,
    };
    assert_eq!(
        store
            .publish_manifest(&manifest, expected_revision)
            .unwrap(),
        TreePublishOutcome::Published
    );
    assert_eq!(
        store
            .publish_manifest(&manifest, expected_revision)
            .unwrap(),
        TreePublishOutcome::AlreadyPublished
    );

    // A physical successor need not invent a logical transaction. Revision is
    // part of the authenticated content identity, so the two immutable
    // manifests can safely describe the same basis and roots.
    let successor_revision = publication_revision + 1;
    let successor_payload = manifest_payload(
        &database_id,
        successor_revision,
        basis_t,
        tx_hash,
        state_hash,
        generation,
        database.eidx_frontier(),
        &manifest.roots,
    );
    let successor = TreeManifestRecord {
        publication_revision: successor_revision,
        manifest_hash: sha256(&successor_payload),
        payload: successor_payload,
        ..manifest.clone()
    };
    assert_eq!(
        store
            .publish_manifest(&successor, publication_revision)
            .unwrap(),
        TreePublishOutcome::Published
    );
    // Exact ambiguous retry remains successful after a newer revision exists.
    assert_eq!(
        store
            .publish_manifest(&manifest, expected_revision)
            .unwrap(),
        TreePublishOutcome::AlreadyPublished
    );

    assert_eq!(
        store.current_publication_revision(&database_id).unwrap(),
        successor_revision
    );
    assert_eq!(
        store
            .latest_published_revision(&database_id, basis_t)
            .unwrap(),
        Some(successor_revision)
    );
    assert_eq!(
        store
            .load_manifest(&database_id, publication_revision)
            .unwrap(),
        Some(manifest.clone())
    );
    assert_eq!(
        store
            .load_manifest(&database_id, successor_revision)
            .unwrap(),
        Some(successor.clone())
    );

    let mut old = Client::connect(&connection, NoTls).unwrap();
    let old_row = old
        .query_one(
            "SELECT tx_hash, state_hash FROM atomic_generation_transactions \
             WHERE database_id = $1 AND generation = $2 AND basis_t = 1",
            &[&database_id, &i64::try_from(generation).unwrap()],
        )
        .unwrap();
    let old_tx_hash = digest(old_row.get(0));
    let old_state_hash = digest(old_row.get(1));
    let old_payload = manifest_payload(
        &database_id,
        successor_revision + 1,
        1,
        old_tx_hash,
        old_state_hash,
        generation,
        database.eidx_frontier(),
        &successor.roots,
    );
    let regressing = TreeManifestRecord {
        publication_revision: successor_revision + 1,
        basis_t: 1,
        tx_hash: old_tx_hash,
        state_hash: old_state_hash,
        manifest_hash: sha256(&old_payload),
        payload: old_payload,
        ..successor.clone()
    };
    drop(old);
    let regression = store
        .publish_manifest(&regressing, successor_revision)
        .unwrap_err();
    assert_eq!(regression.code, "tree/publication-basis-regression");

    let mut gap = successor.clone();
    gap.publication_revision = successor_revision + 2;
    let gap_payload = manifest_payload(
        &database_id,
        successor_revision + 2,
        successor.basis_t,
        successor.tx_hash,
        successor.state_hash,
        successor.excision_generation,
        successor.eidx_frontier,
        &successor.roots,
    );
    gap.manifest_hash = sha256(&gap_payload);
    gap.payload = gap_payload;
    let gap_error = store
        .publish_manifest(&gap, successor_revision)
        .unwrap_err();
    assert_eq!(gap_error.code, "tree/publication-revision-mismatch");

    let stats = store.stats();
    assert_eq!(stats.node_writes, 8);
    assert_eq!(stats.manifest_writes, 2);
    assert_eq!(stats.root_binding_writes, 16);
    assert_eq!(stats.publication_writes, 2);

    for root in &manifest.roots {
        // Rebuilding identical content records measurable structural reuse.
        let bytes = store.load_node(root.root_hash).unwrap().unwrap();
        store.insert_node(root.root_hash, &bytes).unwrap();
    }
    assert_eq!(store.stats().node_reuses, 8);

    let error = store.insert_node([0; 32], b"wrong hash").unwrap_err();
    assert_eq!(error.code, "tree/node-hash-mismatch");

    let mut raw = store.into_client();
    assert!(
        raw.execute(
            "UPDATE atomic_tree_nodes SET payload = $1 WHERE node_hash = $2",
            &[
                &b"replacement".as_slice(),
                &&manifest.roots[0].root_hash[..]
            ],
        )
        .is_err()
    );
}

#[test]
fn sql_rejects_stale_generation_and_incomplete_root_publication() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("tree_store_adversarial");
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("tree-test", "guard"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut kernel = PostgresStore::connect(&connection).unwrap();
    let database = kernel.create_database(&database_id, schema).unwrap();
    drop(kernel);

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let row = client
        .query_one(
            "SELECT h.basis_t, h.tx_hash, t.state_hash, h.log_generation, d.lineage_id \
               FROM atomic_heads h \
               JOIN atomic_databases d USING (database_id) \
               JOIN atomic_generation_transactions t \
                 ON t.database_id = h.database_id AND t.generation = h.log_generation \
                AND t.basis_t = h.basis_t AND t.tx_hash = h.tx_hash \
              WHERE h.database_id = $1",
            &[&database_id],
        )
        .unwrap();
    let basis: i64 = row.get(0);
    let tx_hash: Vec<u8> = row.get(1);
    let state_hash: Vec<u8> = row.get(2);
    let generation: i64 = row.get(3);
    let lineage_id: String = row.get(4);
    let frontier = i64::try_from(database.eidx_frontier()).unwrap();
    let node_payload = format!("ATIX-adversarial-root-{database_id}").into_bytes();
    let node_hash = sha256(&node_payload);
    client
        .execute(
            "INSERT INTO atomic_tree_nodes (node_hash, payload) VALUES ($1, $2)",
            &[&&node_hash[..], &&node_payload[..]],
        )
        .unwrap();

    let legacy_payload = format!("ATIM-v3-rejected-{database_id}").into_bytes();
    let legacy_hash = sha256(&legacy_payload);
    let legacy = client.execute(
        "INSERT INTO atomic_tree_manifests \
           (database_id, publication_revision, basis_t, tx_hash, state_hash, \
            excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
            log_generation, lineage_id) \
         VALUES ($1, 1, $2, $3, $4, $5, $6, 3, $7, $8, $5, $9)",
        &[
            &database_id,
            &basis,
            &tx_hash,
            &state_hash,
            &generation,
            &frontier,
            &&legacy_hash[..],
            &&legacy_payload[..],
            &lineage_id,
        ],
    );
    assert!(legacy.is_err());

    let stale_payload = format!("ATIX-stale-manifest-{database_id}").into_bytes();
    let stale_hash = sha256(&stale_payload);
    let stale = client.execute(
        "INSERT INTO atomic_tree_manifests \
           (database_id, publication_revision, basis_t, tx_hash, state_hash, \
            excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
            log_generation, lineage_id) \
         VALUES ($1, 1, $2, $3, $4, $5, $6, 5, $7, $8, $5, $9)",
        &[
            &database_id,
            &basis,
            &tx_hash,
            &state_hash,
            &(generation + 1),
            &frontier,
            &&stale_hash[..],
            &&stale_payload[..],
            &lineage_id,
        ],
    );
    assert!(stale.is_err());

    let payload = format!("ATIX-incomplete-manifest-{database_id}").into_bytes();
    let manifest_hash = sha256(&payload);
    client
        .execute(
            "INSERT INTO atomic_tree_manifests \
               (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                log_generation, lineage_id) \
             VALUES ($1, 1, $2, $3, $4, $5, $6, 5, $7, $8, $5, $9)",
            &[
                &database_id,
                &basis,
                &tx_hash,
                &state_hash,
                &generation,
                &frontier,
                &&manifest_hash[..],
                &&payload[..],
                &lineage_id,
            ],
        )
        .unwrap();
    for coordinate in 0_i16..7 {
        let history = coordinate >= 4;
        let order = coordinate % 4;
        client
            .execute(
                "INSERT INTO atomic_tree_manifest_roots \
                   (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
                 VALUES ($1, $2, $3, $4, 0, $5)",
                &[
                    &&manifest_hash[..],
                    &order,
                    &history,
                    &&node_hash[..],
                    &i64::try_from(node_payload.len()).unwrap(),
                ],
            )
            .unwrap();
    }
    stage_unknown_delta(&mut client, manifest_hash, None);
    let publication = client.execute(
        "INSERT INTO atomic_tree_publications \
           (database_id, publication_revision, basis_t, tx_hash, manifest_hash, log_generation) \
         VALUES ($1, 1, $2, $3, $4, $5)",
        &[
            &database_id,
            &basis,
            &tx_hash,
            &&manifest_hash[..],
            &generation,
        ],
    );
    assert!(publication.is_err());
    let visible: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(visible, 0);

    client
        .execute(
            "INSERT INTO atomic_tree_manifest_roots \
               (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
             VALUES ($1, 3, true, $2, 0, $3)",
            &[
                &&manifest_hash[..],
                &&node_hash[..],
                &i64::try_from(node_payload.len()).unwrap(),
            ],
        )
        .unwrap();
    client
        .execute(
            "INSERT INTO atomic_tree_publications \
               (database_id, publication_revision, basis_t, tx_hash, manifest_hash, log_generation) \
             VALUES ($1, 1, $2, $3, $4, $5)",
            &[
                &database_id,
                &basis,
                &tx_hash,
                &&manifest_hash[..],
                &generation,
            ],
        )
        .unwrap();

    // The application checks this before writing candidates, and the SQL
    // boundary independently rejects direct writers that skip a revision.
    let gap_payload = format!("ATIX-gap-manifest-{database_id}").into_bytes();
    let gap_hash = sha256(&gap_payload);
    client
        .execute(
            "INSERT INTO atomic_tree_manifests \
               (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                log_generation, lineage_id) \
             VALUES ($1, 3, $2, $3, $4, $5, $6, 5, $7, $8, $5, $9)",
            &[
                &database_id,
                &basis,
                &tx_hash,
                &state_hash,
                &generation,
                &frontier,
                &&gap_hash[..],
                &&gap_payload[..],
                &lineage_id,
            ],
        )
        .unwrap();
    client
        .execute(
            "INSERT INTO atomic_tree_manifest_roots \
               (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
             SELECT $1, index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $2",
            &[&&gap_hash[..], &&manifest_hash[..]],
        )
        .unwrap();
    stage_unknown_delta(&mut client, gap_hash, Some(manifest_hash));
    let gap_publication = client.execute(
        "INSERT INTO atomic_tree_publications \
           (database_id, publication_revision, basis_t, tx_hash, manifest_hash, log_generation) \
         VALUES ($1, 3, $2, $3, $4, $5)",
        &[&database_id, &basis, &tx_hash, &&gap_hash[..], &generation],
    );
    assert!(gap_publication.is_err());
    let visible: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(visible, 1);
}
