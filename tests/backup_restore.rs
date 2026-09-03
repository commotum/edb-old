use atomic_core::{
    Attribute, BackupFault, CallableRef, Cardinality, DB_FN, DB_IDENT, EntityRef, ErrorCategory,
    IndexOrder, Instruction, Keyword, Peer, PortableBackup, PostgresIndexer, PostgresStore,
    Program, ProgramCall, ProgramKind, RestoreFault, Schema, TransactionRequest, TxOp, TxValue,
    USER_PARTITION, Value, ValueType, View, make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::fs;
use std::path::PathBuf;
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

fn omit_first_program_from_manifest(path: &std::path::Path) {
    let mut bytes = fs::read(path).unwrap();
    let body_len = u64::from_be_bytes(bytes[6..14].try_into().unwrap()) as usize;
    let checksum_at = 14 + body_len;
    bytes.truncate(checksum_at);
    let mut at = 14;
    let string_len = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
    at += 4 + string_len;
    let lineage_len = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
    at += 4 + lineage_len + 8 + 32;
    let transaction_count = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
    at += 4 + transaction_count * 32;
    let state_count = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
    at += 4 + state_count * 32;
    let request_count = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
    at += 4;
    for _ in 0..request_count {
        let key_len = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
        at += 4 + key_len + 32 + 8 + 32;
    }
    let program_count = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap());
    assert!(program_count > 0);
    bytes[at..at + 4].copy_from_slice(&(program_count - 1).to_be_bytes());
    bytes.drain(at + 4..at + 4 + 36);
    let new_body_len = body_len - 36;
    bytes[6..14].copy_from_slice(&(new_body_len as u64).to_be_bytes());
    let checksum = sha256(&bytes);
    bytes.extend_from_slice(&checksum);
    fs::write(path, bytes).unwrap();
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

fn assert_same_information(left: &atomic_core::Database, right: &atomic_core::Database) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.eidx_frontier(), right.eidx_frontier());
    assert_eq!(left.schema(), right.schema());
    assert_eq!(
        left.datoms(View::Current, IndexOrder::Eavt),
        right.datoms(View::Current, IndexOrder::Eavt)
    );
    assert_eq!(
        left.datoms(View::History, IndexOrder::Eavt),
        right.datoms(View::History, IndexOrder::Eavt)
    );
}

#[test]
fn live_incremental_backup_deep_verify_and_point_restore_are_exact() {
    let Some(connection) = connection() else {
        return;
    };
    let source = unique("backup_source");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&source, schema()).unwrap();
    let service = common::start_service(&connection, &source);
    let basis1 =
        common::transact(&service, "one", created.basis_t(), &[add("one")], 1_000).db_after;
    let program = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(42)),
            Instruction::Return,
        ],
    };
    store
        .deploy_program(&source, "answer", 1, &program)
        .unwrap();
    store.activate_program(&source, "answer", None, 1).unwrap();
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
    assert_eq!(
        PortableBackup::list_backups(&directory).unwrap(),
        vec![basis1.basis_t(), basis2]
    );

    let mut physical_catalog = Client::connect(&connection, NoTls).unwrap();
    let root_hash: Vec<u8> = physical_catalog
        .query_one(
            "SELECT r.root_hash FROM atomic_tree_publications p \
             JOIN atomic_tree_manifest_roots r ON r.manifest_hash = p.manifest_hash \
             WHERE p.database_id = $1 ORDER BY p.publication_revision DESC LIMIT 1",
            &[&source],
        )
        .unwrap()
        .get(0);
    let root_hash: [u8; 32] = root_hash.try_into().unwrap();
    let root_object = directory.join("objects").join(hex_digest(&root_hash));
    let root_bytes = fs::read(&root_object).unwrap();
    let mut damaged_root = root_bytes.clone();
    damaged_root[0] ^= 1;
    fs::write(&root_object, &damaged_root).unwrap();
    PortableBackup::verify_backup_presence(&directory, basis2).unwrap();
    let error = PortableBackup::verify_backup(&directory, basis2, true).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Fault);
    fs::write(&root_object, root_bytes).unwrap();

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
    let mut target1_store = PostgresStore::connect(&target1_connection).unwrap();
    let (_, _, restored_program) = target1_store
        .resolve_active_program(&target1, "answer")
        .unwrap();
    assert_eq!(restored_program, program);

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
    let restored_peer = Peer::connect(&target2_connection, &target2, 8).unwrap();
    assert_eq!(restored_peer.basis_t(), basis2);
    assert_eq!(restored_peer.durable_base_t(), basis2);
    assert_eq!(restored_peer.durable_base_revision(), 1);
    assert_eq!(
        restored_peer.db().values(user(42), ITEM_VALUE),
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
    target_service.shutdown();

    fs::remove_dir_all(&directory).unwrap();
    service.shutdown();
}

#[test]
fn interrupted_root_publication_never_exposes_a_partial_point_and_retry_converges() {
    let Some(connection) = connection() else {
        return;
    };
    let source = unique("backup_root_faults");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let database = store.create_database(&source, schema()).unwrap();
    let mut backup = PortableBackup::connect(&connection).unwrap();

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
    fs::remove_dir_all(directory).unwrap();
}

#[test]
fn restore_faults_are_atomic_and_ambiguous_commit_retry_is_idempotent() {
    let Some(connection) = connection() else {
        return;
    };
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
            .is_none()
    );
    let restored = before_restore
        .restore_backup(&directory, committed.basis_t, &before_commit)
        .unwrap();
    assert_same_information(&committed.db_after, &restored);

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
    let replay = after_restore
        .restore_backup(&directory, committed.basis_t, &after_commit)
        .unwrap();
    assert_same_information(&committed.db_after, &replay);
    fs::remove_dir_all(directory).unwrap();
}

#[test]
fn corrupted_external_object_fails_deep_verification() {
    let Some(connection) = connection() else {
        return;
    };
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
fn backup_restores_every_temporal_function_version_without_legacy_aliases() {
    let Some(connection) = connection() else {
        return;
    };
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
    let current_hash = store.deploy_program_blob(&writer("current")).unwrap();
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
    // Presence verification does not read content, while deep verification
    // authenticates every referenced program object.
    PortableBackup::verify_backup_presence(&directory, point.basis_t).unwrap();
    PortableBackup::verify_backup(&directory, point.basis_t, false).unwrap();
    let error = PortableBackup::verify_backup(&directory, point.basis_t, true).unwrap_err();
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
        for hash in [old_hash, current_hash] {
            client.execute(
                "DELETE FROM atomic_programs WHERE program_hash = $1",
                &[&&hash[..]],
            )?;
        }
        Ok(())
    })
    .unwrap();
    let restored = backup
        .restore_backup(&directory, rebound.basis_t, &target)
        .unwrap();
    assert_eq!(
        restored.values(function, DB_FN as u32),
        vec![&Value::Function(current_hash)]
    );
    let mut restored_store = PostgresStore::connect(&connection).unwrap();
    assert_eq!(
        restored_store.resolve_program(old_hash).unwrap(),
        writer("old")
    );
    assert_eq!(
        restored_store.resolve_program(current_hash).unwrap(),
        writer("current")
    );

    let target_service = common::start_service(&connection, &target);
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
        report.db_after.values(user(42), ITEM_VALUE),
        vec![&Value::String("current".into())]
    );
    target_service.shutdown();
    omit_first_program_from_manifest(
        &directory
            .join("snapshots")
            .join(format!("{:020}.atbk", point.basis_t)),
    );
    let error = PortableBackup::verify_backup(&directory, point.basis_t, false).unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Fault, "backup/missing-program")
    );
    fs::remove_dir_all(&directory).unwrap();
}
