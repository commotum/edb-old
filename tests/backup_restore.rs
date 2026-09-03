use atomic_core::{
    Attribute, CallableRef, Cardinality, DB_FN, DB_IDENT, EntityRef, ErrorCategory, IndexOrder,
    Instruction, Keyword, PortableBackup, PostgresStore, Program, ProgramCall, ProgramKind, Schema,
    TransactionRequest, TxOp, TxValue, USER_PARTITION, Value, ValueType, View, make_eid, sha256,
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

fn omit_first_program_from_manifest(path: &std::path::Path) {
    let mut bytes = fs::read(path).unwrap();
    let body_len = u64::from_be_bytes(bytes[6..14].try_into().unwrap()) as usize;
    let checksum_at = 14 + body_len;
    bytes.truncate(checksum_at);
    let mut at = 14;
    let string_len = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
    at += 4 + string_len + 8 + 32;
    let transaction_count = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
    at += 4 + transaction_count * 32;
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
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
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

    let mut backup = PortableBackup::connect(&connection).unwrap();
    let first = backup.backup_database(&source, &directory).unwrap();
    assert_eq!(first.basis_t, basis1.basis_t());
    assert!(first.objects_written >= 3);
    let basis2 = common::transact(&service, "two", basis1.basis_t(), &[add("two")], 2_000).basis_t;
    let second = backup.backup_database(&source, &directory).unwrap();
    assert_eq!(second.basis_t, basis2);
    assert!(second.objects_reused >= 3);
    assert_eq!(
        PortableBackup::list_backups(&directory).unwrap(),
        vec![basis1.basis_t(), basis2]
    );

    let verified1 = PortableBackup::verify_backup(&directory, basis1.basis_t(), true).unwrap();
    let verified2 = PortableBackup::verify_backup(&directory, basis2, true).unwrap();
    assert_same_information(&basis1, &verified1.database);
    assert_eq!(
        verified2.database.values(user(42), ITEM_VALUE),
        vec![&Value::String("two".into())]
    );

    let target1 = unique("restore_one");
    let restored1 = backup
        .restore_backup(&directory, basis1.basis_t(), &target1)
        .unwrap();
    assert_same_information(&verified1.database, &restored1);
    let (_, _, restored_program) = store.resolve_active_program(&target1, "answer").unwrap();
    assert_eq!(restored_program, program);
    let target2 = unique("restore_two");
    let restored2 = backup.restore_backup(&directory, basis2, &target2).unwrap();
    assert_same_information(&verified2.database, &restored2);
    let error = backup
        .restore_backup(&directory, basis2, &target2)
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Conflict, "backup/target-exists")
    );

    fs::remove_dir_all(&directory).unwrap();
    service.shutdown();
}

#[test]
fn corrupted_external_object_fails_deep_verification() {
    let Some(connection) = connection() else {
        return;
    };
    let source = unique("backup_corrupt");
    let directory = backup_directory();
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
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
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
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
