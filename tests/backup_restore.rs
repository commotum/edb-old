use atomic_core::{
    Attribute, BackupFault, CallableRef, Cardinality, DB_EXCISE, DB_FN, DB_IDENT, EntityRef,
    ErrorCategory, Instruction, Keyword, Peer, PersistentTreeManifest, PortableBackup,
    PostgresIndexer, PostgresOperator, PostgresStore, PostgresTreeStore, Program, ProgramCall,
    ProgramKind, RestoreFault, Schema, TransactionRequest, TreeManifestRecord, TxOp, TxValue,
    USER_PARTITION, Value, ValueType, make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::fs;
use std::path::PathBuf;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const ITEM_VALUE: u32 = 1_000;

fn connection() -> Option<common::PostgresFixture> {
    let connection = std::env::var("ATOMIC_POSTGRES_URL").ok()?;
    // Corruption and interrupted restore fixtures must not make another
    // test's migrate discover exceptional offline catalog-repair work.
    Some(common::PostgresFixture::new(&connection, "backup_restore"))
}

fn retry_semantic_fence<T>(
    mut operation: impl FnMut() -> Result<T, atomic_core::SemanticError>,
) -> Result<T, atomic_core::SemanticError> {
    let deadline = std::time::Instant::now() + Duration::from_secs(5);
    loop {
        match operation() {
            Err(error)
                if error.category == ErrorCategory::Busy
                    && error.code == "operations/semantic-gc-pinned"
                    && std::time::Instant::now() < deadline =>
            {
                std::thread::sleep(Duration::from_millis(10));
            }
            result => return result,
        }
    }
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

fn backup_directory() -> PathBuf {
    std::env::temp_dir().join(unique("atomic_backup"))
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

fn hex_digest(hash: &[u8; 32]) -> String {
    hash.iter().map(|byte| format!("{byte:02x}")).collect()
}

fn current_manifest_root_shape(path: &std::path::Path) -> (usize, bool) {
    let bytes = fs::read(path).unwrap();
    assert_eq!(u16::from_be_bytes(bytes[4..6].try_into().unwrap()), 5);
    let mut at = 14;
    let lineage_len = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
    at += 4 + lineage_len + 8 + 8 + 32;
    at += 32 + 32 + 32 + 32;
    let has_tree = match bytes[at] {
        0 => false,
        1 => true,
        value => panic!("invalid tree tag {value}"),
    };
    (bytes.len(), has_tree)
}

fn snapshot_path(directory: &std::path::Path, point: &atomic_core::BackupPoint) -> PathBuf {
    directory.join("snapshots").join(format!(
        "{:020}-g{:020}.atbk",
        point.basis_t, point.log_generation
    ))
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

fn add(value: &str) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(user(42)),
        attribute: ITEM_VALUE,
        value: TxValue::Scalar(Value::String(value.into())),
    }
}

fn assert_same_information(
    left: &impl common::InformationSource,
    right: &impl common::InformationSource,
) {
    common::assert_same_information(left, right);
}

#[test]
fn restored_native_receipts_survive_backup_restore_backup_and_exact_retry() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_request_base_source");
    let first_target = unique("backup_request_base_first_target");
    let second_target = unique("backup_request_base_second_target");
    let first_directory = backup_directory();
    let second_directory = backup_directory();
    let request_key = "portable-exact-request-base";
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let seed_ops = (0..128)
        .map(|ordinal| TxOp::Add {
            entity: EntityRef::Temp(format!("seed-{ordinal}")),
            attribute: ITEM_VALUE,
            value: TxValue::Scalar(Value::String(format!("seed-{ordinal}"))),
        })
        .collect::<Vec<_>>();
    let seed = common::transact(&service, "portable-seed", created.basis_t(), &seed_ops, 500);
    service.shutdown();
    let mut indexer = PostgresIndexer::connect(&connection, &source)
        .unwrap()
        .with_segment_datoms(1)
        .unwrap();
    indexer.consolidate().unwrap();
    let service = common::start_service(&connection, &source);
    let committed = common::transact(
        &service,
        request_key,
        seed.basis_t,
        &[add("portable")],
        1_000,
    );
    service.shutdown();

    let mut source_backup = PortableBackup::connect(&connection).unwrap();
    source_backup
        .backup_database(&source, &first_directory)
        .unwrap();
    PortableBackup::verify_backup(&first_directory, committed.basis_t, true).unwrap();

    let first_connection = isolated_catalog(&connection, "request_base_first_catalog");
    let mut first_restore = PortableBackup::connect(&first_connection).unwrap();
    let first_restored = first_restore
        .restore_backup(&first_directory, committed.basis_t, &first_target)
        .unwrap();
    common::assert_same_information(&first_restored, &committed.db_after);
    let mut first_catalog = Client::connect(&first_connection, NoTls).unwrap();
    let first_archive_count: i64 = first_catalog
        .query_one(
            "SELECT count(*) FROM atomic_request_base_archives archive \
              JOIN atomic_heads head ON head.database_id = archive.database_id \
                                    AND head.log_generation = archive.generation \
              JOIN atomic_request_base_archive_completions complete \
                ON complete.manifest_hash = archive.manifest_hash \
             WHERE archive.database_id = $1",
            &[&first_target],
        )
        .unwrap()
        .get(0);
    assert!(first_archive_count > 0);

    // A fresh directory prevents the logical-point reuse fast path from
    // hiding whether capture can resolve an archive-backed request base.
    let mut second_backup = PortableBackup::connect(&first_connection).unwrap();
    let rebound_point = second_backup
        .backup_database(&first_target, &second_directory)
        .unwrap();
    PortableBackup::verify_backup(&second_directory, rebound_point.basis_t, true).unwrap();

    let second_connection = isolated_catalog(&connection, "request_base_second_catalog");
    let mut second_restore = PortableBackup::connect(&second_connection).unwrap();
    let second_restored = second_restore
        .restore_backup(&second_directory, rebound_point.basis_t, &second_target)
        .unwrap();
    common::assert_same_information(&second_restored, &committed.db_after);
    let mut second_catalog = Client::connect(&second_connection, NoTls).unwrap();
    let archive_node_count: i64 = second_catalog
        .query_one(
            "SELECT max(expected_node_count) \
               FROM atomic_request_base_archives archive \
               JOIN atomic_request_base_archive_completions complete \
                 ON complete.manifest_hash = archive.manifest_hash \
              WHERE archive.database_id = $1",
            &[&second_target],
        )
        .unwrap()
        .get::<_, Option<i64>>(0)
        .expect("restored request receipts retained a completed archive");
    assert!(archive_node_count > 8);
    let second_service = common::start_service(&second_connection, &second_target);
    let before_retry = second_service.writer_residency_stats();
    let replay = second_service
        .client()
        .transact(
            TransactionRequest::new(request_key, vec![add("portable")])
                .comparing_basis(seed.basis_t)
                .with_tx_instant(1_000),
            Duration::from_secs(5),
        )
        .unwrap();
    let after_retry = second_service.writer_residency_stats();
    let retry_node_reads = after_retry
        .native_root_reads
        .saturating_sub(before_retry.native_root_reads)
        .saturating_add(
            after_retry
                .native_directory_reads
                .saturating_sub(before_retry.native_directory_reads),
        )
        .saturating_add(
            after_retry
                .native_leaf_reads
                .saturating_sub(before_retry.native_leaf_reads),
        );
    assert!(
        retry_node_reads < archive_node_count as u64,
        "exact retry eagerly read {retry_node_reads} nodes from a {archive_node_count}-node archive"
    );
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    common::assert_same_information(&replay.db_before, &seed.db_after);
    common::assert_same_information(&replay.db_after, &committed.db_after);
    second_service.shutdown();

    fs::remove_dir_all(first_directory).unwrap();
    fs::remove_dir_all(second_directory).unwrap();
}

#[test]
fn live_incremental_backup_deep_verify_and_point_restore_are_exact() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_source");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let basis1 =
        common::transact(&service, "one", created.basis_t(), &[add("one")], 1_000).db_after;
    let mut indexer = PostgresIndexer::connect(&connection, &source).unwrap();
    indexer.consolidate().unwrap();

    let mut backup = PortableBackup::connect(&connection).unwrap();
    let first = backup.backup_database(&source, &directory).unwrap();
    assert_eq!(first.basis_t, basis1.basis_t());
    assert!(first.objects_written >= 3);
    let basis2 = common::transact(&service, "two", basis1.basis_t(), &[add("two")], 2_000).basis_t;
    indexer.consolidate().unwrap();
    let second = backup.backup_database(&source, &directory).unwrap();
    assert_eq!(second.basis_t, basis2);
    assert!(second.objects_reused >= 3);
    let first_root_shape = current_manifest_root_shape(&snapshot_path(&directory, &first));
    let second_root_shape = current_manifest_root_shape(&snapshot_path(&directory, &second));
    assert_eq!(first_root_shape, second_root_shape);
    // Envelope 5 adds an exact read-tree and sparse log-index digest. Root
    // bytes are still fixed-size rather than proportional to database history.
    assert_eq!(second_root_shape, (328, true));
    assert_eq!(
        PortableBackup::list_backups(&directory).unwrap(),
        vec![basis1.basis_t(), basis2]
    );

    let alias_retry = backup.backup_database(&source, &directory).unwrap();
    assert_eq!(alias_retry.manifest_hash, second.manifest_hash);
    assert_eq!(alias_retry.objects_written, 0);

    // A physical repair can append a new root revision without inventing a
    // logical transaction. Retrying backup at that same (lineage, t) must
    // retain the already-published backup root, not conflict with or silently
    // replace it merely because the live cache envelope changed.
    let object_count_before = fs::read_dir(directory.join("objects")).unwrap().count();
    let mut live_trees = PostgresTreeStore::connect(&connection).unwrap();
    let current_revision = live_trees.current_publication_revision(&source).unwrap();
    let current = live_trees
        .load_manifest(&source, current_revision)
        .unwrap()
        .unwrap();
    let mut successor_payload = PersistentTreeManifest::decode(&current.payload).unwrap();
    successor_payload.publication_revision = current_revision + 1;
    let successor_payload = successor_payload.encode().unwrap();
    let successor = TreeManifestRecord {
        publication_revision: current_revision + 1,
        manifest_hash: sha256(&successor_payload),
        payload: successor_payload,
        ..current
    };
    live_trees
        .publish_manifest(&successor, current_revision)
        .unwrap();
    let repaired_retry = backup.backup_database(&source, &directory).unwrap();
    assert_eq!(repaired_retry.manifest_hash, second.manifest_hash);
    assert_eq!(repaired_retry.objects_written, 0);
    assert_eq!(
        fs::read_dir(directory.join("objects")).unwrap().count(),
        object_count_before
    );

    let mut physical_catalog = Client::connect(&connection, NoTls).unwrap();
    let leaf_hash: Vec<u8> = physical_catalog
        .query_one(
            "SELECT n.node_hash FROM atomic_tree_live_nodes l \
             JOIN atomic_tree_nodes n ON n.node_hash = l.node_hash \
             WHERE l.database_id = $1 AND get_byte(n.payload, 6) = 1 LIMIT 1",
            &[&source],
        )
        .unwrap()
        .get(0);
    let leaf_hash: [u8; 32] = leaf_hash.try_into().unwrap();
    let leaf_object = directory.join("objects").join(hex_digest(&leaf_hash));
    let leaf_bytes = fs::read(&leaf_object).unwrap();
    let mut damaged_leaf = leaf_bytes.clone();
    damaged_leaf[0] ^= 1;
    fs::write(&leaf_object, &damaged_leaf).unwrap();
    // Shallow verification reads routing nodes but only checks leaf presence;
    // deep verification authenticates every leaf payload.
    PortableBackup::verify_backup_presence(&directory, basis2).unwrap();
    let error = PortableBackup::verify_backup(&directory, basis2, true).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Fault);
    fs::write(&leaf_object, leaf_bytes).unwrap();

    let verified1 = PortableBackup::verify_backup(&directory, basis1.basis_t(), true).unwrap();
    let verified2 = PortableBackup::verify_backup(&directory, basis2, true).unwrap();
    assert_same_information(&basis1, &verified1.database);
    assert_eq!(
        verified2.database.values(user(42), ITEM_VALUE),
        vec![&Value::String("two".into())]
    );

    let duplicate_name = unique("restore_same_catalog");
    let identity_error = backup
        .restore_backup(&directory, basis2, &duplicate_name)
        .unwrap_err();
    assert_eq!(
        (identity_error.category, identity_error.code),
        (ErrorCategory::Conflict, "backup/lineage-exists")
    );

    let target1_connection = isolated_catalog(&connection, "restore_catalog_one");
    let target1 = unique("restore_one");
    let mut target1_restore = PortableBackup::connect(&target1_connection).unwrap();
    let restored1 = target1_restore
        .restore_backup(&directory, basis1.basis_t(), &target1)
        .unwrap();
    assert_same_information(&verified1.database, &restored1);

    let target2_connection = isolated_catalog(&connection, "restore_catalog_two");
    let target2 = unique("restore_two");
    let mut target2_restore = PortableBackup::connect(&target2_connection).unwrap();
    let restored2 = target2_restore
        .restore_backup(&directory, basis2, &target2)
        .unwrap();
    assert_same_information(&verified2.database, &restored2);
    let mut catalog = Client::connect(&target2_connection, NoTls).unwrap();
    let restored_tree_count: i64 = catalog
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&target2],
        )
        .unwrap()
        .get(0);
    assert_eq!(restored_tree_count, 1);
    let source_lineage: String = physical_catalog
        .query_one(
            "SELECT lineage_id FROM atomic_databases WHERE database_id = $1",
            &[&source],
        )
        .unwrap()
        .get(0);
    let target_lineage: String = catalog
        .query_one(
            "SELECT lineage_id FROM atomic_databases WHERE database_id = $1",
            &[&target2],
        )
        .unwrap()
        .get(0);
    assert_eq!(target_lineage, source_lineage);
    let renamed_retry = target2_restore
        .backup_database(&target2, &directory)
        .unwrap();
    assert_eq!(renamed_retry.manifest_hash, second.manifest_hash);
    assert_eq!(renamed_retry.objects_written, 0);
    let restored_peer = Peer::connect(&target2_connection, &target2, 8).unwrap();
    assert_eq!(restored_peer.basis_t(), basis2);
    assert_eq!(restored_peer.durable_base_t(), basis2);
    assert_eq!(restored_peer.durable_base_revision(), 1);
    assert_eq!(
        restored_peer
            .db_compatibility()
            .values(user(42), ITEM_VALUE),
        vec![&Value::String("two".into())]
    );
    let source_states: Vec<Vec<u8>> = physical_catalog
        .query(
            "SELECT state_hash FROM atomic_transactions \
             WHERE database_id = $1 AND basis_t <= $2 ORDER BY basis_t",
            &[&source, &(basis2 as i64)],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get(0))
        .collect();
    let target_states: Vec<Vec<u8>> = catalog
        .query(
            "SELECT state_hash FROM atomic_transactions \
             WHERE database_id = $1 ORDER BY basis_t",
            &[&target2],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get(0))
        .collect();
    assert_eq!(target_states, source_states);
    let source_requests: Vec<(String, Vec<u8>, i64)> = physical_catalog
        .query(
            "SELECT request_key, request_digest, basis_t FROM atomic_requests \
             WHERE database_id = $1 AND basis_t <= $2 ORDER BY basis_t",
            &[&source, &(basis2 as i64)],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get(0), row.get(1), row.get(2)))
        .collect();
    let target_requests: Vec<(String, Vec<u8>, i64)> = catalog
        .query(
            "SELECT request_key, request_digest, basis_t FROM atomic_requests \
             WHERE database_id = $1 ORDER BY basis_t",
            &[&target2],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get(0), row.get(1), row.get(2)))
        .collect();
    assert_eq!(target_requests, source_requests);
    let replayed_restore = target2_restore
        .restore_backup(&directory, basis2, &target2)
        .unwrap();
    assert_same_information(&verified2.database, &replayed_restore);
    let target_service = common::start_service(&target2_connection, &target2);
    let replayed_request = common::try_transact(
        &target_service,
        "two",
        basis1.basis_t(),
        &[add("two")],
        2_000,
    )
    .unwrap();
    assert!(replayed_request.replayed);
    assert_eq!(replayed_request.basis_t, basis2);
    let continued = common::transact(
        &target_service,
        "after-rename",
        basis2,
        &[add("three")],
        3_000,
    );
    let continued_backup = target2_restore
        .backup_database(&target2, &directory)
        .unwrap();
    assert_eq!(continued_backup.basis_t, continued.basis_t);
    // A true incremental successor references the authenticated parent root;
    // it need not reopen/copy prefix objects merely to increment a reuse
    // counter. Only the new tail has to be published.
    assert!(continued_backup.objects_written > 0);
    let continued_verified =
        PortableBackup::verify_backup(&directory, continued.basis_t, true).unwrap();
    assert_same_information(&continued.db_after, &continued_verified.database);
    target_service.shutdown();

    fs::remove_dir_all(&directory).unwrap();
    service.shutdown();
}

#[test]
fn same_basis_generations_are_exact_points_and_restore_remains_lineage_local() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_generation_source");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let seeded = common::transact(
        &service,
        "generation-secret",
        created.basis_t(),
        &[add("erase-me")],
        10_000,
    );
    let requested = common::transact(
        &service,
        "generation-a15",
        seeded.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("privacy-request".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(user(42))),
        }],
        11_000,
    );
    let request_entity = requested.tempids["privacy-request"];
    service.shutdown();

    let mut backup = PortableBackup::connect(&connection).unwrap();
    let before = backup.backup_database(&source, &directory).unwrap();
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let excision = operator.process_excision_requests(&source).unwrap();
    assert_eq!(excision.basis_t, before.basis_t);
    assert_eq!(excision.generation, before.log_generation + 1);
    let after = backup.backup_database(&source, &directory).unwrap();
    assert_eq!(after.basis_t, before.basis_t);
    assert_eq!(after.log_generation, excision.generation);
    assert_ne!(after.manifest_hash, before.manifest_hash);
    assert!(after.objects_reused >= 2);

    let points = PortableBackup::list_backup_points(&directory).unwrap();
    assert_eq!(
        points
            .iter()
            .map(|point| (point.basis_t, point.log_generation))
            .collect::<Vec<_>>(),
        vec![
            (before.basis_t, before.log_generation),
            (after.basis_t, after.log_generation),
        ]
    );
    assert_eq!(
        PortableBackup::list_backups(&directory).unwrap(),
        vec![before.basis_t]
    );
    let exact_before = PortableBackup::verify_backup_point(
        &directory,
        before.basis_t,
        before.log_generation,
        true,
    )
    .unwrap();
    assert_eq!(
        exact_before.database.values(user(42), ITEM_VALUE),
        vec![&Value::String("erase-me".into())]
    );
    let latest = PortableBackup::verify_backup(&directory, before.basis_t, true).unwrap();
    assert_eq!(latest.point.log_generation, after.log_generation);
    assert!(latest.database.values(user(42), ITEM_VALUE).is_empty());

    // Unrelated databases make the destination catalog busy but cannot
    // perturb this lineage's generation coordinate. Initial positive restore
    // preserves the archive generation and therefore its exact backup root.
    let target_connection = isolated_catalog(&connection, "restore_generation_catalog");
    let target = unique("restore_generation_target");
    let mut target_store = PostgresStore::connect(&target_connection).unwrap();
    for ordinal in 0..4 {
        target_store
            .create_database(&unique(&format!("restore_noise_{ordinal}")), Schema::new())
            .unwrap();
    }
    let mut target_restore = PortableBackup::connect(&target_connection).unwrap();
    let restored_after = target_restore
        .restore_backup_point(&directory, after.basis_t, after.log_generation, &target)
        .unwrap();
    assert_same_information(&latest.database, &restored_after);
    let mut target_catalog = Client::connect(&target_connection, NoTls).unwrap();
    let restored_generation: i64 = target_catalog
        .query_one(
            "SELECT log_generation FROM atomic_heads WHERE database_id = $1",
            &[&target],
        )
        .unwrap()
        .get(0);
    assert_eq!(restored_generation as u64, after.log_generation);
    let renamed = target_restore.backup_database(&target, &directory).unwrap();
    assert_eq!(renamed.manifest_hash, after.manifest_hash);
    assert_eq!(renamed.objects_written, 0);

    // Point restore is a new local information publication. Rewinding to the
    // pre-excision generation hides the future completion set, and ordinary
    // tree consolidation must work at that older logical t on the new local
    // generation.
    let restored_before = target_restore
        .restore_backup_point(&directory, before.basis_t, before.log_generation, &target)
        .unwrap();
    assert_same_information(&exact_before.database, &restored_before);
    let rewind_generation: i64 = target_catalog
        .query_one(
            "SELECT log_generation FROM atomic_heads WHERE database_id = $1",
            &[&target],
        )
        .unwrap()
        .get(0);
    assert_eq!(rewind_generation as u64, after.log_generation + 1);
    let mut target_operator = PostgresOperator::connect(&target_connection).unwrap();
    assert!(
        !target_operator
            .sync_excise(&target, before.basis_t)
            .unwrap()
    );
    let mut indexer = PostgresIndexer::connect(&target_connection, &target).unwrap();
    let consolidated = indexer.consolidate().unwrap();
    assert_eq!(consolidated.basis_t, before.basis_t);
    let consolidated_generation: i64 = target_catalog
        .query_one(
            "SELECT excision_generation FROM atomic_tree_manifests WHERE manifest_hash = $1",
            &[&&consolidated.manifest_hash[..]],
        )
        .unwrap()
        .get(0);
    assert_eq!(consolidated_generation, rewind_generation);
    drop(indexer);

    let restored_latest = target_restore
        .restore_backup_point(&directory, after.basis_t, after.log_generation, &target)
        .unwrap();
    assert_same_information(&latest.database, &restored_latest);
    assert!(target_operator.sync_excise(&target, after.basis_t).unwrap());
    let completion_rows: Vec<(i64, i64)> = target_catalog
        .query(
            "SELECT c.request_t, c.request_entity \
               FROM atomic_heads h JOIN atomic_completed_excision_requests c \
                 ON c.database_id = h.database_id AND c.generation = h.log_generation \
              WHERE h.database_id = $1 ORDER BY c.request_t, c.request_entity",
            &[&target],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get(0), row.get(1)))
        .collect();
    assert_eq!(
        completion_rows,
        vec![(before.basis_t as i64, request_entity as i64)]
    );
    fs::remove_dir_all(&directory).unwrap();
}

#[test]
fn live_backup_generation_pin_blocks_point_restore_cutover_and_retry_converges() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_pin_source");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let first = common::transact(
        &service,
        "backup-pin-one",
        created.basis_t(),
        &[add("one")],
        20_000,
    );
    // Keep an explicit eager verification value: native transaction reports
    // now correctly pin their immutable source generation until dropped.
    let first_expected = store.recover_basis(&source, first.basis_t).unwrap();
    let mut backup = PortableBackup::connect(&connection).unwrap();
    let old_point = backup.backup_database(&source, &directory).unwrap();
    let second = common::transact(
        &service,
        "backup-pin-two",
        first.basis_t,
        &[add("two")],
        21_000,
    );
    service.shutdown();

    let mut competing_restore = PortableBackup::connect(&connection).unwrap();
    let current_point = backup
        .backup_database_with_pin_probe(&source, &directory, || {
            let error = competing_restore
                .restore_backup_point(
                    &directory,
                    old_point.basis_t,
                    old_point.log_generation,
                    &source,
                )
                .unwrap_err();
            assert_eq!(error.code, "backup/restore-activate");
            assert!(error.message.contains("blocked by a live peer or backup"));
        })
        .unwrap();
    assert_eq!(current_point.basis_t, second.basis_t);
    let still_current = store.recover(&source).unwrap();
    assert_same_information(&second.db_after, &still_current);

    // The failed restore above was caused by the backup pin. Retained native
    // report values are independent legitimate generation pins, so release
    // them before proving that the backup-pin retry itself converges.
    drop(first);
    drop(second);

    let retried = competing_restore
        .restore_backup_point(
            &directory,
            old_point.basis_t,
            old_point.log_generation,
            &source,
        )
        .unwrap();
    assert_same_information(&first_expected, &retried);
    fs::remove_dir_all(&directory).unwrap();
}

#[test]
fn interrupted_root_publication_never_exposes_a_partial_point_and_retry_converges() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_root_faults");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let database = store.create_database(&source, schema()).unwrap();
    let mut backup = PortableBackup::connect(&connection).unwrap();

    let pin_directory = backup_directory();
    let error = backup
        .backup_database_with_fault(&source, &pin_directory, BackupFault::AfterGenerationPinned)
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Interrupted);
    assert_eq!(
        fs::read_dir(pin_directory.join("snapshots"))
            .unwrap()
            .count(),
        0
    );
    let mut lock_probe = Client::connect(&connection, NoTls).unwrap();
    let pin_key: i64 = lock_probe
        .query_one(
            "SELECT atomic_log_generation_pin_key(database_id, log_generation) \
               FROM atomic_heads WHERE database_id = $1",
            &[&source],
        )
        .unwrap()
        .get(0);
    assert!(
        lock_probe
            .query_one("SELECT pg_try_advisory_lock($1)", &[&pin_key])
            .unwrap()
            .get::<_, bool>(0)
    );
    assert!(
        lock_probe
            .query_one("SELECT pg_advisory_unlock($1)", &[&pin_key])
            .unwrap()
            .get::<_, bool>(0)
    );
    backup.backup_database(&source, &pin_directory).unwrap();
    fs::remove_dir_all(pin_directory).unwrap();

    for fault_at in [
        BackupFault::AfterFirstObjectStaged,
        BackupFault::AfterObjects,
        BackupFault::AfterManifestStaged,
    ] {
        let directory = backup_directory();
        let error = backup
            .backup_database_with_fault(&source, &directory, fault_at)
            .unwrap_err();
        assert_eq!(error.category, ErrorCategory::Interrupted);
        assert!(PortableBackup::list_backups(&directory).unwrap().is_empty());
        let point = backup.backup_database(&source, &directory).unwrap();
        assert_eq!(point.basis_t, database.basis_t());
        PortableBackup::verify_backup(&directory, point.basis_t, true).unwrap();
        fs::remove_dir_all(directory).unwrap();
    }

    let directory = backup_directory();
    let error = backup
        .backup_database_with_fault(&source, &directory, BackupFault::AfterManifestPublished)
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Interrupted);
    assert_eq!(
        PortableBackup::list_backups(&directory).unwrap(),
        vec![database.basis_t()]
    );
    let retry = backup.backup_database(&source, &directory).unwrap();
    assert_eq!(retry.basis_t, database.basis_t());
    PortableBackup::verify_backup(&directory, retry.basis_t, true).unwrap();
    let other = unique("backup_claim_intruder");
    store.create_database(&other, schema()).unwrap();
    let claim_error = backup.backup_database(&other, &directory).unwrap_err();
    assert_eq!(
        (claim_error.category, claim_error.code),
        (ErrorCategory::Conflict, "backup/claim-conflict")
    );
    let recreated_connection = isolated_catalog(&connection, "backup_recreated_catalog");
    let mut recreated_store = PostgresStore::connect(&recreated_connection).unwrap();
    recreated_store.create_database(&source, schema()).unwrap();
    let mut recreated_backup = PortableBackup::connect(&recreated_connection).unwrap();
    let recreated_error = recreated_backup
        .backup_database(&source, &directory)
        .unwrap_err();
    assert_eq!(
        (recreated_error.category, recreated_error.code),
        (ErrorCategory::Conflict, "backup/claim-conflict")
    );
    fs::remove_dir_all(directory).unwrap();
}

#[test]
fn restore_faults_are_atomic_and_ambiguous_commit_retry_is_idempotent() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_restore_faults");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let committed = common::transact(
        &service,
        "restore-fault-seed",
        created.basis_t(),
        &[add("durable")],
        4_000,
    );
    service.shutdown();
    let mut backup = PortableBackup::connect(&connection).unwrap();
    backup.backup_database(&source, &directory).unwrap();

    let batch_connection = isolated_catalog(&connection, "restore_batch_catalog");
    let mut batch_restore = PortableBackup::connect(&batch_connection).unwrap();
    let batch_target = unique("restore_batch_atomicity");
    let batch_error = batch_restore
        .restore_backup_with_fault(
            &directory,
            committed.basis_t,
            &batch_target,
            RestoreFault::AfterFirstContentInserted,
        )
        .unwrap_err();
    assert_eq!(batch_error.category, ErrorCategory::Interrupted);
    let mut batch_catalog = Client::connect(&batch_connection, NoTls).unwrap();
    let unowned_contents: i64 = batch_catalog
        .query_one(
            "SELECT count(*) FROM atomic_transaction_contents c \
              WHERE c.lineage_id = (SELECT lineage_id FROM atomic_databases \
                                     WHERE database_id = $1) \
                AND NOT EXISTS (SELECT 1 FROM atomic_generation_transactions t \
                                 WHERE t.content_hash = c.content_hash)",
            &[&batch_target],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        unowned_contents, 0,
        "failed restore leaked an ownerless ATLC value"
    );
    let batch_retried = batch_restore
        .restore_backup(&directory, committed.basis_t, &batch_target)
        .unwrap();
    assert_same_information(&committed.db_after, &batch_retried);

    // Exercise the exact lock-handoff instant without a timing sleep. The
    // restore has dropped its shared builder pin, but still owns the restore
    // serialization lock in the publication transaction. A zero-age GC on a
    // separate session must be unable to install its permanent abandonment
    // claim, and the same transaction must then publish normally.
    let handoff_connection = isolated_catalog(&connection, "restore_handoff_catalog");
    let handoff_target = unique("restore_handoff_target");
    let mut handoff_restore = PortableBackup::connect(&handoff_connection).unwrap();
    let mut handoff_operator = PostgresOperator::connect(&handoff_connection).unwrap();
    let mut probed = false;
    let handoff_restored = handoff_restore
        .restore_backup_with_activation_probe(
            &directory,
            committed.basis_t,
            &handoff_target,
            || {
                probed = true;
                let collection =
                    retry_semantic_fence(|| handoff_operator.collect_garbage(Duration::ZERO))
                        .unwrap();
                assert!(
                    collection
                        .log_generations
                        .iter()
                        .all(|candidate| candidate.database_id != handoff_target),
                    "GC admitted the live restore during its activation handoff"
                );
                let mut catalog = Client::connect(&handoff_connection, NoTls).unwrap();
                let claimed: bool = catalog
                    .query_one(
                        "SELECT EXISTS (SELECT 1 \
                           FROM atomic_log_generation_abandonment_progress \
                          WHERE database_id = $1)",
                        &[&handoff_target],
                    )
                    .unwrap()
                    .get(0);
                assert!(!claimed, "GC permanently claimed the live restore");
            },
        )
        .unwrap();
    assert!(probed);
    assert_same_information(&committed.db_after, &handoff_restored);

    let before_connection = isolated_catalog(&connection, "restore_before_catalog");
    let mut before_restore = PortableBackup::connect(&before_connection).unwrap();
    let before_commit = unique("restore_before_commit");
    let error = before_restore
        .restore_backup_with_fault(
            &directory,
            committed.basis_t,
            &before_commit,
            RestoreFault::BeforeCommit,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Interrupted);
    let mut client = Client::connect(&before_connection, NoTls).unwrap();
    assert!(
        client
            .query_opt(
                "SELECT 1 FROM atomic_databases WHERE database_id = $1",
                &[&before_commit],
            )
            .unwrap()
            .is_some()
    );
    assert!(
        client
            .query_opt(
                "SELECT 1 FROM atomic_heads WHERE database_id = $1",
                &[&before_commit],
            )
            .unwrap()
            .is_none()
    );
    let staged_coordinates: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1",
            &[&before_commit],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        staged_coordinates,
        i64::try_from(committed.basis_t + 1).unwrap(),
        "restore stages every immutable historical coordinate before activation"
    );
    let candidate = client
        .query_one(
            "SELECT generation, through_basis_t, head_hash, state_hash \
               FROM atomic_log_generation_checkpoints \
              WHERE database_id = $1",
            &[&before_commit],
        )
        .unwrap();
    let candidate_generation: i64 = candidate.get(0);
    let candidate_basis: i64 = candidate.get(1);
    let candidate_head: Vec<u8> = candidate.get(2);
    let candidate_state: Vec<u8> = candidate.get(3);
    // BeforeCommit now deliberately leaves every semantic coordinate staged.
    // Remove only the exact endpoint inside a rolled-back fault transaction so
    // this remains a direct witness for the SQL activation fence.
    let mut rootless = client.transaction().unwrap();
    rootless
        .batch_execute(
            "ALTER TABLE atomic_semantic_commitment_roots \
             DISABLE TRIGGER atomic_semantic_commitment_roots_immutable",
        )
        .unwrap();
    assert_eq!(
        rootless
            .execute(
                "DELETE FROM atomic_semantic_commitment_roots \
                  WHERE database_id = $1 AND generation = $2 AND basis_t = $3 \
                    AND tx_hash = $4 AND state_hash = $5",
                &[
                    &before_commit,
                    &candidate_generation,
                    &candidate_basis,
                    &candidate_head,
                    &candidate_state,
                ],
            )
            .unwrap(),
        1
    );
    let activation_without_root = rootless
        .query_one(
            "SELECT atomic_activate_initial_log_generation($1, $2, $3, $4, $5)",
            &[
                &before_commit,
                &candidate_generation,
                &candidate_basis,
                &candidate_head,
                &candidate_state,
            ],
        )
        .unwrap_err();
    assert_eq!(
        activation_without_root.as_db_error().unwrap().code().code(),
        "23503",
        "the SQL publication boundary must reject a coordinate-less generation"
    );
    rootless.rollback().unwrap();
    assert!(
        client
            .query_opt(
                "SELECT 1 FROM atomic_heads WHERE database_id = $1",
                &[&before_commit],
            )
            .unwrap()
            .is_none(),
        "a rejected activation must roll its tentative head insert back"
    );
    let mut unpublished = PostgresStore::connect(&before_connection).unwrap();
    assert!(unpublished.recover(&before_commit).is_err());
    let restored = before_restore
        .restore_backup(&directory, committed.basis_t, &before_commit)
        .unwrap();
    assert_same_information(&committed.db_after, &restored);
    let restored_coordinate: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_heads h \
               JOIN atomic_log_generation_activations a \
                 ON a.database_id = h.database_id \
                AND a.generation = h.log_generation \
                AND a.basis_t = h.basis_t AND a.head_hash = h.tx_hash \
               JOIN atomic_semantic_commitment_roots r \
                 ON r.database_id = h.database_id \
                AND r.generation = h.log_generation \
                AND r.basis_t = h.basis_t AND r.tx_hash = h.tx_hash \
                AND r.state_hash = a.state_hash \
              WHERE h.database_id = $1",
            &[&before_commit],
        )
        .unwrap()
        .get(0);
    assert_eq!(restored_coordinate, 1);
    let restarted_restore = PostgresStore::connect(&before_connection)
        .unwrap()
        .recover(&before_commit)
        .unwrap();
    assert_same_information(&committed.db_after, &restarted_restore);

    // A second faulted first-time restore is deliberately abandoned instead
    // of resumed. The first GC pass installs the permanent claim; restore
    // must then fail Busy, bounded phases drain only generation-owned rows,
    // and the unpublished catalog locator becomes reusable at the end.
    let abandoned_connection = isolated_catalog(&connection, "restore_abandoned_initial_catalog");
    let abandoned_target = unique("restore_abandoned_initial");
    let mut abandoned_restore = PortableBackup::connect(&abandoned_connection).unwrap();
    let error = abandoned_restore
        .restore_backup_with_fault(
            &directory,
            committed.basis_t,
            &abandoned_target,
            RestoreFault::BeforeCommit,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Interrupted);
    let mut abandoned_catalog = Client::connect(&abandoned_connection, NoTls).unwrap();
    let abandoned_identity = abandoned_catalog
        .query_one(
            "SELECT d.lineage_id, g.generation \
               FROM atomic_databases d JOIN atomic_log_generations g \
                 ON g.database_id = d.database_id \
              WHERE d.database_id = $1 AND g.build_kind = 0",
            &[&abandoned_target],
        )
        .unwrap();
    let abandoned_lineage: String = abandoned_identity.get(0);
    let abandoned_generation: i64 = abandoned_identity.get(1);
    let mut abandoned_operator = PostgresOperator::connect(&abandoned_connection).unwrap();
    let first_collection =
        retry_semantic_fence(|| abandoned_operator.collect_garbage(Duration::ZERO)).unwrap();
    assert!(
        first_collection
            .request_base_archives
            .iter()
            .any(|candidate| {
                candidate.database_id == abandoned_target
                    && candidate.generation == abandoned_generation as u64
            })
    );
    let abandonment_claimed: bool = abandoned_catalog
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_log_generation_abandonment_progress \
                              WHERE database_id = $1 AND generation = $2)",
            &[&abandoned_target, &abandoned_generation],
        )
        .unwrap()
        .get(0);
    assert!(
        abandonment_claimed,
        "request-base archive collection did not permanently claim the headless restore"
    );
    let busy = abandoned_restore
        .restore_backup(&directory, committed.basis_t, &abandoned_target)
        .unwrap_err();
    assert_eq!(
        (busy.category, busy.code),
        (ErrorCategory::Busy, "backup/restore-target-abandoning")
    );
    let mut collected = false;
    for _ in 0..32 {
        if abandoned_catalog
            .query_opt(
                "SELECT 1 FROM atomic_databases WHERE database_id = $1",
                &[&abandoned_target],
            )
            .unwrap()
            .is_none()
        {
            collected = true;
            break;
        }
        // Other parallel fixtures can hold the catalog-wide GC fence. That
        // documented Busy response is retryable; corruption and other errors
        // must still fail immediately, and contention must not wait forever.
        retry_semantic_fence(|| abandoned_operator.collect_garbage(Duration::ZERO)).unwrap();
    }
    assert!(
        collected,
        "headless restore was not reclaimed in bounded phases"
    );
    let leaked_contents: i64 = abandoned_catalog
        .query_one(
            "SELECT count(*) FROM atomic_transaction_contents WHERE lineage_id = $1",
            &[&abandoned_lineage],
        )
        .unwrap()
        .get(0);
    assert_eq!(leaked_contents, 0);
    let mut replacement_store = PostgresStore::connect(&abandoned_connection).unwrap();
    replacement_store
        .create_database(&abandoned_target, schema())
        .unwrap();
    let replacement = atomic_core::DatabaseCatalog::connect(&abandoned_connection)
        .unwrap()
        .resolve(&abandoned_target)
        .unwrap();
    // Reclaimed stable storage identities are never reused by a new database
    // with the same public name. Resolve the route before comparing lineage.
    assert_ne!(replacement.database_id, abandoned_target);
    let replacement_lineage = replacement.lineage_id;
    assert_ne!(replacement_lineage, abandoned_lineage);

    let after_connection = isolated_catalog(&connection, "restore_after_catalog");
    let mut after_restore = PortableBackup::connect(&after_connection).unwrap();
    let after_commit = unique("restore_after_commit");
    let error = after_restore
        .restore_backup_with_fault(
            &directory,
            committed.basis_t,
            &after_commit,
            RestoreFault::AfterCommitBeforeResponse,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Interrupted);
    let mut after_catalog = Client::connect(&after_connection, NoTls).unwrap();
    let acknowledged_coordinate: i64 = after_catalog
        .query_one(
            "SELECT count(*) FROM atomic_heads h \
               JOIN atomic_semantic_commitment_roots r \
                 ON r.database_id = h.database_id \
                AND r.generation = h.log_generation \
                AND r.basis_t = h.basis_t AND r.tx_hash = h.tx_hash \
              WHERE h.database_id = $1",
            &[&after_commit],
        )
        .unwrap()
        .get(0);
    assert_eq!(acknowledged_coordinate, 1);
    let replay = after_restore
        .restore_backup(&directory, committed.basis_t, &after_commit)
        .unwrap();
    assert_same_information(&committed.db_after, &replay);
    fs::remove_dir_all(directory).unwrap();
}

#[test]
fn permanently_claimed_restore_build_is_never_resumed_or_activated() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_abandoned_restore_source");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let mut backup = PortableBackup::connect(&connection).unwrap();
    backup.backup_database(&source, &directory).unwrap();
    let service = common::start_service(&connection, &source);
    let latest = common::transact(
        &service,
        "abandoned-restore-seed",
        created.basis_t(),
        &[add("latest")],
        4_100,
    );
    service.shutdown();
    backup.backup_database(&source, &directory).unwrap();

    let target_connection = isolated_catalog(&connection, "restore_abandoned_catalog");
    let target = unique("restore_abandoned_target");
    let mut restore = PortableBackup::connect(&target_connection).unwrap();
    restore
        .restore_backup(&directory, latest.basis_t, &target)
        .unwrap();

    // Leave a fully staged rewind candidate, then model the durable claim
    // installed by the abandonment worker after its captured source ceased to
    // be current. A claim is a one-way ownership transfer: restore must skip
    // the old build and allocate a new database-local successor.
    let interrupted = restore
        .restore_backup_with_fault(
            &directory,
            created.basis_t(),
            &target,
            RestoreFault::BeforeCommit,
        )
        .unwrap_err();
    assert_eq!(interrupted.category, ErrorCategory::Interrupted);
    let mut catalog = Client::connect(&target_connection, NoTls).unwrap();
    let claimed_generation: i64 = catalog
        .query_one(
            "SELECT b.generation FROM atomic_log_generation_builds b \
              WHERE b.database_id = $1 \
                AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                 WHERE a.database_id = b.database_id \
                                   AND a.generation = b.generation)",
            &[&target],
        )
        .unwrap()
        .get(0);
    catalog
        .execute(
            "INSERT INTO atomic_log_generation_abandonment_progress \
                 (database_id, generation) VALUES ($1, $2)",
            &[&target, &claimed_generation],
        )
        .unwrap();

    let rewound = restore
        .restore_backup(&directory, created.basis_t(), &target)
        .unwrap();
    assert_same_information(&created, &rewound);
    let active_generation: i64 = catalog
        .query_one(
            "SELECT log_generation FROM atomic_heads WHERE database_id = $1",
            &[&target],
        )
        .unwrap()
        .get(0);
    assert!(active_generation > claimed_generation);
    let claim_state = catalog
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_log_generation_abandonment_progress \
                              WHERE database_id = $1 AND generation = $2), \
                    EXISTS (SELECT 1 FROM atomic_log_generation_activations \
                              WHERE database_id = $1 AND generation = $2)",
            &[&target, &claimed_generation],
        )
        .unwrap();
    assert!(claim_state.get::<_, bool>(0));
    assert!(!claim_state.get::<_, bool>(1));
    fs::remove_dir_all(directory).unwrap();
}

#[test]
fn corrupted_external_object_fails_deep_verification() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_corrupt");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let basis =
        common::transact(&service, "one", created.basis_t(), &[add("secret")], 1_000).basis_t;
    let mut backup = PortableBackup::connect(&connection).unwrap();
    backup.backup_database(&source, &directory).unwrap();
    let object = fs::read_dir(directory.join("objects"))
        .unwrap()
        .next()
        .unwrap()
        .unwrap()
        .path();
    let original = fs::read(&object).unwrap();
    let mut corrupt = original.clone();
    corrupt[0] ^= 1;
    fs::write(&object, &corrupt).unwrap();
    let error = PortableBackup::verify_backup(&directory, basis, true).unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Fault, "backup/object-corrupt")
    );
    fs::write(&object, &original).unwrap();
    assert!(PortableBackup::verify_backup(&directory, basis, true).is_ok());
    fs::remove_dir_all(&directory).unwrap();
    service.shutdown();
}

#[test]
fn corrupt_derived_roots_fall_back_to_older_tree_then_log_only() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_derived_fallback");
    let tree_directory = backup_directory();
    let log_directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let basis1 = common::transact(&service, "one", created.basis_t(), &[add("one")], 1_000).basis_t;
    let mut indexer = PostgresIndexer::connect(&connection, &source).unwrap();
    indexer.consolidate().unwrap();
    let basis2 = common::transact(&service, "two", basis1, &[add("two")], 2_000).db_after;
    indexer.consolidate().unwrap();

    let mut catalog = Client::connect(&connection, NoTls).unwrap();
    let manifests: Vec<Vec<u8>> = catalog
        .query(
            "SELECT manifest_hash FROM atomic_tree_publications \
             WHERE database_id = $1 ORDER BY publication_revision DESC",
            &[&source],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get(0))
        .collect();
    assert!(manifests.len() >= 2);
    let corrupt_manifest = |catalog: &mut Client, hash: &[u8]| {
        let mut payload: Vec<u8> = catalog
            .query_one(
                "SELECT payload FROM atomic_tree_manifests WHERE manifest_hash = $1",
                &[&hash],
            )
            .unwrap()
            .get(0);
        payload[0] ^= 1;
        common::with_replica_triggers_disabled(catalog, |catalog| {
            catalog.execute(
                "UPDATE atomic_tree_manifests SET payload = $2 WHERE manifest_hash = $1",
                &[&hash, &payload],
            )?;
            Ok(())
        })
        .unwrap();
    };

    // The newest physical revision is damaged, but the previous published
    // tree remains a valid accelerator for the same authoritative log point.
    corrupt_manifest(&mut catalog, &manifests[0]);
    let mut backup = PortableBackup::connect(&connection).unwrap();
    let tree_point = backup.backup_database(&source, &tree_directory).unwrap();
    assert_eq!(tree_point.basis_t, basis2.basis_t());
    PortableBackup::verify_backup(&tree_directory, tree_point.basis_t, true).unwrap();
    let tree_target_connection = isolated_catalog(&connection, "restore_older_tree");
    let tree_target = unique("restore_older_tree");
    let mut tree_restore = PortableBackup::connect(&tree_target_connection).unwrap();
    let restored_from_tree = tree_restore
        .restore_backup(&tree_directory, tree_point.basis_t, &tree_target)
        .unwrap();
    assert_same_information(&basis2, &restored_from_tree);
    let mut tree_catalog = Client::connect(&tree_target_connection, NoTls).unwrap();
    let restored_tree_basis: i64 = tree_catalog
        .query_one(
            "SELECT basis_t FROM atomic_tree_publications WHERE database_id = $1",
            &[&tree_target],
        )
        .unwrap()
        .get(0);
    // The older source tree plus its canonical tail now yields an exact
    // offline read projection at capture, rather than a lagging backup tree.
    assert_eq!(restored_tree_basis as u64, basis2.basis_t());

    // When every derived root is damaged, the immutable transaction log is
    // still sufficient authority. Capture reconstructs from genesis plus
    // transactions and emits a complete exact read index only in the backup.
    for hash in manifests.iter().skip(1) {
        corrupt_manifest(&mut catalog, hash);
    }
    let log_point = backup.backup_database(&source, &log_directory).unwrap();
    PortableBackup::verify_backup(&log_directory, log_point.basis_t, true).unwrap();
    let log_target_connection = isolated_catalog(&connection, "restore_log_only");
    let log_target = unique("restore_log_only");
    let mut log_restore = PortableBackup::connect(&log_target_connection).unwrap();
    let restored_from_log = log_restore
        .restore_backup(&log_directory, log_point.basis_t, &log_target)
        .unwrap();
    assert_same_information(&basis2, &restored_from_log);
    let mut log_catalog = Client::connect(&log_target_connection, NoTls).unwrap();
    let restored_tree_count: i64 = log_catalog
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&log_target],
        )
        .unwrap()
        .get(0);
    assert_eq!(restored_tree_count, 1);
    let restored_basis: i64 = log_catalog
        .query_one(
            "SELECT basis_t FROM atomic_tree_publications WHERE database_id = $1",
            &[&log_target],
        )
        .unwrap()
        .get(0);
    assert_eq!(restored_basis as u64, basis2.basis_t());
    let restored_request_base_count: i64 = log_catalog
        .query_one(
            "SELECT count(*) FROM atomic_request_base_archives archive \
              JOIN atomic_request_base_archive_completions complete \
                ON complete.manifest_hash = archive.manifest_hash \
             WHERE archive.database_id = $1",
            &[&log_target],
        )
        .unwrap()
        .get(0);
    assert!(restored_request_base_count > 0);

    fs::remove_dir_all(tree_directory).unwrap();
    fs::remove_dir_all(log_directory).unwrap();
    service.shutdown();
}

#[test]
fn backup_restores_every_temporal_function_version_without_legacy_aliases() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let source = unique("backup_temporal_functions");
    let target = unique("restore_temporal_functions");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let writer = |value: &str| Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushEntity(EntityRef::Id(user(42))),
            Instruction::PushConstant(Value::String(value.into())),
            Instruction::EmitAdd(ITEM_VALUE),
            Instruction::Return,
        ],
    };
    let old_hash = store.deploy_program_blob(&writer("old")).unwrap();
    let nested_hash = store.deploy_program_blob(&writer("current")).unwrap();
    let current_wrapper = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::EmitCall {
                function: CallableRef::ExactHash(nested_hash),
                argument_count: 0,
            },
            Instruction::Return,
        ],
    };
    let current_hash = store.deploy_program_blob(&current_wrapper).unwrap();
    let function_ident = Keyword::new("backup", "writer");
    let service = common::start_service(&connection, &source);
    let installed = common::transact(
        &service,
        "install-temporal-writer",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("writer".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(function_ident.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("writer".into()),
                attribute: DB_FN as u32,
                value: Value::Function(old_hash).into(),
            },
        ],
        1_000,
    );
    let function = installed.tempids["writer"];
    let rebound = common::transact(
        &service,
        "rebind-temporal-writer",
        installed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(function),
            attribute: DB_FN as u32,
            value: Value::Function(current_hash).into(),
        }],
        2_000,
    );
    let mut backup = PortableBackup::connect(&connection).unwrap();
    let point = backup.backup_database(&source, &directory).unwrap();
    PortableBackup::verify_backup_presence(&directory, point.basis_t).unwrap();
    let old_program_object = directory.join("objects").join(hex_digest(&old_hash));
    let old_program_bytes = fs::read(&old_program_object).unwrap();
    let mut damaged_program = old_program_bytes.clone();
    damaged_program[0] ^= 1;
    fs::write(&old_program_object, &damaged_program).unwrap();
    // Linked program values must be read even by presence verification: their
    // immutable payloads are the routing metadata for transitive dependencies.
    let error = PortableBackup::verify_backup_presence(&directory, point.basis_t).unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Fault, "backup/object-corrupt")
    );
    fs::write(&old_program_object, old_program_bytes).unwrap();
    PortableBackup::verify_backup(&directory, point.basis_t, true).unwrap();
    service.shutdown();

    // No legacy program-version row points at either blob. Removing the live
    // catalog copies makes restoration depend on the backup object graph.
    let mut client = Client::connect(&connection, NoTls).unwrap();
    common::with_replica_triggers_disabled(&mut client, |client| {
        for hash in [old_hash, current_hash, nested_hash] {
            client.execute(
                "DELETE FROM atomic_programs WHERE program_hash = $1",
                &[&&hash[..]],
            )?;
        }
        Ok(())
    })
    .unwrap();
    let target_connection = isolated_catalog(&connection, "restore_temporal_catalog");
    let mut target_restore = PortableBackup::connect(&target_connection).unwrap();
    let restored = target_restore
        .restore_backup(&directory, rebound.basis_t, &target)
        .unwrap();
    assert_eq!(
        restored.values(function, DB_FN as u32),
        vec![&Value::Function(current_hash)]
    );
    let mut target_catalog = Client::connect(&target_connection, NoTls).unwrap();
    let restored_program_marks = target_catalog
        .query(
            "SELECT r.program_hash FROM atomic_heads h \
               JOIN atomic_program_generation_refs r \
                 ON r.database_id = h.database_id \
                AND r.log_generation = h.log_generation \
              WHERE h.database_id = $1 ORDER BY r.program_hash",
            &[&target],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, Vec<u8>>(0))
        .collect::<BTreeSet<_>>();
    assert_eq!(
        restored_program_marks,
        [old_hash, current_hash, nested_hash]
            .into_iter()
            .map(Vec::from)
            .collect()
    );
    // Newly restored blobs are immediately old enough at a zero threshold.
    // The exact active-generation marks, not a mutable alias, must keep the
    // whole temporal/transitive graph alive.
    let mut target_operator = PostgresOperator::connect(&target_connection).unwrap();
    retry_semantic_fence(|| target_operator.collect_garbage(Duration::ZERO)).unwrap();
    let mut restored_store = PostgresStore::connect(&target_connection).unwrap();
    assert_eq!(
        restored_store.resolve_program(old_hash).unwrap(),
        writer("old")
    );
    assert_eq!(
        restored_store.resolve_program(current_hash).unwrap(),
        current_wrapper
    );
    assert_eq!(
        restored_store.resolve_program(nested_hash).unwrap(),
        writer("current")
    );

    let target_service = common::start_service(&target_connection, &target);
    let report = target_service
        .client()
        .transact(
            TransactionRequest::new("invoke-restored-writer", vec![]).calling(ProgramCall {
                function: CallableRef::Database(EntityRef::Ident(function_ident)),
                arguments: vec![],
            }),
            Duration::from_secs(5),
        )
        .unwrap();
    assert_eq!(
        report.db_after.values(user(42), ITEM_VALUE).unwrap(),
        vec![Value::String("current".into())]
    );
    target_service.shutdown();

    // V4 roots name only the linked log heads. Program reachability is derived
    // transitively from immutable datoms and program payloads, so removing a
    // reachable object must fail even though no flattened program list exists.
    let missing_program_object = old_program_object.with_extension("temporarily-missing");
    fs::rename(&old_program_object, &missing_program_object).unwrap();
    let error = PortableBackup::verify_backup_presence(&directory, point.basis_t).unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Unavailable, "backup/object-type")
    );
    fs::rename(missing_program_object, old_program_object).unwrap();
    // The source corruption above is deliberate, but this suite shares its
    // installation schema. Put the immutable values back so a later explicit
    // migration/repair is not correctly forced to fail closed on our debris.
    let mut repair = PostgresStore::connect(&connection).unwrap();
    assert_eq!(
        repair.deploy_program_blob(&writer("old")).unwrap(),
        old_hash
    );
    assert_eq!(
        repair.deploy_program_blob(&current_wrapper).unwrap(),
        current_hash
    );
    assert_eq!(
        repair.deploy_program_blob(&writer("current")).unwrap(),
        nested_hash
    );
    fs::remove_dir_all(&directory).unwrap();
}
