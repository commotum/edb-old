use atomic_core::{
    Attribute, Cardinality, EntityRef, ErrorCategory, IndexOrder, Keyword, PersistentTreeManifest,
    PortableBackup, PostgresIndexer, PostgresMigrator, PostgresStore, PostgresTreeStore, Schema,
    TxOp, Value, ValueType, persistent_tree, sha256,
};
use postgres::{Client, NoTls};
use std::fs;
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

fn unique(label: &str) -> String {
    format!(
        "{label}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn isolated_catalog(connection: &str) -> common::PostgresFixture {
    let fixture = common::PostgresFixture::new(connection, "backup_copy_boundary");
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    fixture
}

fn hex(hash: &[u8]) -> String {
    hash.iter().map(|byte| format!("{byte:02x}")).collect()
}

fn private_directory() -> tempfile::TempDir {
    let directory = tempfile::tempdir().unwrap();
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt as _;
        fs::set_permissions(directory.path(), fs::Permissions::from_mode(0o700)).unwrap();
    }
    directory
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

/// The source corruption is intentionally isolated from every other live
/// fixture. Copy is an authentication boundary, not a semantic replay claim.
#[test]
fn initial_copy_defers_semantic_replay_but_keeps_content_and_receipt_authentication() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = isolated_catalog(&connection);
    let connection = fixture.connection.clone();
    let source = unique("copy_source");
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let report = common::transact(
        &service,
        "copy-one",
        created.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1000,
            value: Value::Long(42).into(),
        }],
        1000,
    );
    let basis = report.basis_t;
    service.shutdown();
    drop(report);
    drop(created);
    drop(store);

    let mut catalog = Client::connect(&connection, NoTls).unwrap();
    let row = catalog
        .query_one(
            "SELECT t.generation, t.basis_t, t.tx_hash, t.state_hash, t.content_hash \
               FROM atomic_heads h JOIN atomic_generation_transactions t \
                 ON t.database_id = h.database_id AND t.generation = h.log_generation \
                AND t.basis_t = h.basis_t WHERE h.database_id = $1",
            &[&source],
        )
        .unwrap();
    let generation: i64 = row.get(0);
    let basis_sql: i64 = row.get(1);
    let original_tx_hash: Vec<u8> = row.get(2);
    let original_state_hash: Vec<u8> = row.get(3);
    let content_hash: Vec<u8> = row.get(4);
    assert!(generation > 0);
    let pristine = private_directory();
    let mut backup = PortableBackup::connect(&connection).unwrap();
    backup.backup_database(&source, pristine.path()).unwrap();
    PortableBackup::verify_backup(pristine.path(), basis, true).unwrap();

    // Rewrite only the claimed state in the canonical membership preimage,
    // then consistently update its SQL membership, request and head hashes.
    // Datoms, content hashes and request-base provenance remain untouched.
    let original_membership =
        fs::read(pristine.path().join("objects").join(hex(&original_tx_hash))).unwrap();
    assert_eq!(sha256(&original_membership).as_slice(), original_tx_hash);
    let state_at = original_membership.len() - 32 - 8;
    assert_eq!(
        &original_membership[state_at..state_at + 32],
        original_state_hash
    );
    let mut false_membership = original_membership.clone();
    let false_state_hash = sha256(b"hash-valid but false semantic state");
    false_membership[state_at..state_at + 32].copy_from_slice(&false_state_hash);
    let false_tx_hash = sha256(&false_membership);
    let set_coordinate = |catalog: &mut Client, tx_hash: &[u8], state_hash: &[u8]| {
        common::with_replica_triggers_disabled(catalog, |catalog| {
            let mut transaction = catalog.transaction()?;
            assert_eq!(
                transaction.execute(
                    "UPDATE atomic_generation_transactions SET tx_hash = $4, state_hash = $5 \
                     WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                    &[&source, &generation, &basis_sql, &tx_hash, &state_hash],
                )?,
                1
            );
            assert_eq!(
                transaction.execute(
                    "UPDATE atomic_generation_requests SET tx_hash = $4 \
                     WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                    &[&source, &generation, &basis_sql, &tx_hash],
                )?,
                1
            );
            assert_eq!(
                transaction.execute(
                    "UPDATE atomic_heads SET tx_hash = $2 WHERE database_id = $1",
                    &[&source, &tx_hash],
                )?,
                1
            );
            transaction.commit()
        })
        .unwrap();
    };
    set_coordinate(&mut catalog, &false_tx_hash, &false_state_hash);

    let copied = private_directory();
    let point = backup.backup_database(&source, copied.path()).unwrap();
    assert_eq!(point.basis_t, basis);
    assert_eq!(
        fs::read(copied.path().join("objects").join(hex(&false_tx_hash))).unwrap(),
        false_membership,
        "capture must preserve the authenticated source claim, not repair it"
    );
    let object_count = fs::read_dir(copied.path().join("objects")).unwrap().count();
    let reused = backup.backup_database(&source, copied.path()).unwrap();
    assert_eq!(reused.manifest_hash, point.manifest_hash);
    assert_eq!(reused.objects_written, 0);
    assert_eq!(
        fs::read_dir(copied.path().join("objects")).unwrap().count(),
        object_count,
        "unchanged retry must authenticate and retain the same object graph"
    );
    PortableBackup::verify_backup_presence(copied.path(), basis).unwrap();
    let verify_error = PortableBackup::verify_backup(copied.path(), basis, true).unwrap_err();
    // The new exact read tree exposes the false endpoint commitment before
    // full log replay. This is still a hard failure before restore staging.
    assert_eq!(
        (verify_error.category, verify_error.code),
        (ErrorCategory::Fault, "backup/tree-binding")
    );
    let target = unique("reject_semantic_copy");
    let restore_error = backup
        .restore_backup(copied.path(), basis, &target)
        .unwrap_err();
    assert_eq!(
        (restore_error.category, restore_error.code),
        (ErrorCategory::Fault, "backup/tree-binding")
    );
    assert_eq!(
        catalog
            .query_one(
                "SELECT count(*) FROM atomic_databases WHERE database_id = $1",
                &[&target],
            )
            .unwrap()
            .get::<_, i64>(0),
        0,
        "restore must reject semantic corruption before staging or activation"
    );
    set_coordinate(&mut catalog, &original_tx_hash, &original_state_hash);
    let point_conflict = backup.backup_database(&source, copied.path()).unwrap_err();
    assert_eq!(
        (point_conflict.category, point_conflict.code),
        (ErrorCategory::Conflict, "backup/point-conflict")
    );
    let root = copied.path().join("snapshots").join(format!(
        "{:020}-g{:020}.atbk",
        point.basis_t, point.log_generation
    ));
    assert_eq!(sha256(&fs::read(root).unwrap()), point.manifest_hash);

    // Content authentication is still mandatory during capture itself.
    let content: Vec<u8> = catalog
        .query_one(
            "SELECT payload FROM atomic_transaction_contents WHERE content_hash = $1",
            &[&content_hash],
        )
        .unwrap()
        .get(0);
    let mut corrupt_content = content.clone();
    *corrupt_content.last_mut().unwrap() ^= 1;
    common::with_replica_triggers_disabled(&mut catalog, |catalog| {
        catalog.execute(
            "UPDATE atomic_transaction_contents SET payload = $2 WHERE content_hash = $1",
            &[&content_hash, &corrupt_content],
        )
    })
    .unwrap();
    let corrupt_directory = private_directory();
    let content_error = backup
        .backup_database(&source, corrupt_directory.path())
        .unwrap_err();
    assert_eq!(content_error.category, ErrorCategory::Fault);
    assert_eq!(content_error.code, "generation/content-checksum");
    assert!(
        PortableBackup::list_backups(corrupt_directory.path())
            .unwrap()
            .is_empty()
    );
    common::with_replica_triggers_disabled(&mut catalog, |catalog| {
        catalog.execute(
            "UPDATE atomic_transaction_contents SET payload = $2 WHERE content_hash = $1",
            &[&content_hash, &content],
        )
    })
    .unwrap();

    // Nor can an intact log silently lose its immutable request provenance.
    common::with_replica_triggers_disabled(&mut catalog, |catalog| {
        catalog.execute(
            "UPDATE atomic_generation_requests SET tx_hash = $4 \
             WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
            &[&source, &generation, &basis_sql, &&false_tx_hash[..]],
        )
    })
    .unwrap();
    let detached_directory = private_directory();
    let receipt_error = backup
        .backup_database(&source, detached_directory.path())
        .unwrap_err();
    assert_eq!(
        (receipt_error.category, receipt_error.code),
        (ErrorCategory::Fault, "backup/invalid-chain")
    );
    assert!(
        PortableBackup::list_backups(detached_directory.path())
            .unwrap()
            .is_empty()
    );
    set_coordinate(&mut catalog, &original_tx_hash, &original_state_hash);
    let repaired = private_directory();
    backup.backup_database(&source, repaired.path()).unwrap();
    PortableBackup::verify_backup(repaired.path(), basis, true).unwrap();
}

#[test]
fn unchanged_retry_authenticates_leaf_bytes_and_requires_the_complete_tree() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = isolated_catalog(&connection);
    let connection = fixture.connection.clone();
    let source = unique("copy_tree_source");
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let operations = (0..40)
        .map(|value| TxOp::Add {
            entity: EntityRef::Temp(format!("item-{value}")),
            attribute: 1000,
            value: Value::Long(value).into(),
        })
        .collect::<Vec<_>>();
    let report = common::transact(&service, "copy-tree", created.basis_t(), &operations, 1000);
    let basis = report.basis_t;
    let mut indexer = PostgresIndexer::connect(&connection, &source).unwrap();
    indexer.consolidate().unwrap();
    let mut trees = PostgresTreeStore::connect(&connection).unwrap();
    let revision = trees.current_publication_revision(&source).unwrap();
    let live = trees.load_manifest(&source, revision).unwrap().unwrap();
    let live = PersistentTreeManifest::decode(&live.payload).unwrap();
    assert_eq!(live.basis_t, basis);
    service.shutdown();
    drop(report);
    drop(created);
    drop(store);

    let directory = private_directory();
    let mut backup = PortableBackup::connect(&connection).unwrap();
    let point = backup.backup_database(&source, directory.path()).unwrap();
    let root = live.tree(IndexOrder::Eavt, false).unwrap();
    let mut leaf_hash = None;
    persistent_tree::validate_tree_streaming(&root.descriptor, |hash| {
        let bytes = fs::read(directory.path().join("objects").join(hex(hash))).unwrap();
        if let persistent_tree::TreeNode::Leaf(leaf) =
            persistent_tree::decode_tree_node(hash, &bytes)?
            && (0..leaf.len()).any(|index| leaf.datom(index).unwrap().attribute == 1000)
        {
            leaf_hash = Some(*hash);
        }
        Ok(bytes)
    })
    .unwrap();
    // This leaf contains the new transaction's business facts, so it is not
    // in that transaction's earlier db-before archive. Retry must inspect the
    // retained main tree itself instead of relying on receipt-base copying.
    let leaf_path = directory
        .path()
        .join("objects")
        .join(hex(&leaf_hash.expect("main tree contains business facts")));
    let original = fs::read(&leaf_path).unwrap();
    let mut corrupted = original.clone();
    *corrupted.last_mut().unwrap() ^= 1;
    fs::write(&leaf_path, &corrupted).unwrap();
    PortableBackup::verify_backup_presence(directory.path(), basis).unwrap();
    let damaged = backup
        .backup_database(&source, directory.path())
        .unwrap_err();
    assert_eq!(
        (damaged.category, damaged.code),
        (ErrorCategory::Fault, "backup/object-corrupt")
    );
    fs::write(&leaf_path, &original).unwrap();

    let hidden = leaf_path.with_extension("temporarily-missing");
    fs::rename(&leaf_path, &hidden).unwrap();
    let missing = backup
        .backup_database(&source, directory.path())
        .unwrap_err();
    assert_eq!(
        (missing.category, missing.code),
        (ErrorCategory::Unavailable, "backup/object-type")
    );
    fs::rename(hidden, leaf_path).unwrap();
    let count = fs::read_dir(directory.path().join("objects"))
        .unwrap()
        .count();
    let repaired = backup.backup_database(&source, directory.path()).unwrap();
    assert_eq!(repaired.manifest_hash, point.manifest_hash);
    assert_eq!(repaired.objects_written, 0);
    assert_eq!(
        fs::read_dir(directory.path().join("objects"))
            .unwrap()
            .count(),
        count
    );
    PortableBackup::verify_backup(directory.path(), basis, true).unwrap();
}
