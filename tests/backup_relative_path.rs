#![cfg(unix)]
mod common;

use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::{
    Attribute, BackupConnection, BackupReadConfig, Cardinality, EntityRef, ErrorCategory,
    IndexTransaction, Keyword, PortableBackup, PostgresConnectionConfig, Schema, TimePoint, TxOp,
    Value, ValueType,
};
use std::os::unix::fs::{PermissionsExt, symlink};
use std::process::Command;
use std::time::Instant;

const CHILD: &str = "ATOMIC_BACKUP_RELATIVE_CHILD";
const ENTITY: &str = "ATOMIC_BACKUP_RELATIVE_ENTITY";
const BASIS: &str = "ATOMIC_BACKUP_RELATIVE_BASIS";
const TEST: &str = "relative_backup_cold_reads_survive_working_directory_change";

fn child_reads() {
    assert!(std::env::var_os("ATOMIC_POSTGRES_URL").is_none());
    let entity: u64 = std::env::var(ENTITY).unwrap().parse().unwrap();
    let basis: u64 = std::env::var(BASIS).unwrap().parse().unwrap();

    // Anchoring must not canonicalize away a forbidden final symlink or
    // bypass operator-private directory admission.
    symlink("repository", "repository-link").unwrap();
    let error = BackupConnection::open("repository-link").err().unwrap();
    assert_eq!(error.category, ErrorCategory::Fault);
    assert_eq!(error.code, "backup/directory-type");
    std::fs::create_dir("public-repository").unwrap();
    std::fs::set_permissions("public-repository", std::fs::Permissions::from_mode(0o777)).unwrap();
    let error = BackupConnection::open("public-repository").err().unwrap();
    assert_eq!(error.category, ErrorCategory::Fault);
    assert_eq!(error.code, "backup/directory-permissions");

    let connection = BackupConnection::open_configured(
        "repository",
        None,
        BackupReadConfig {
            cache_entries: 0,
            cache_bytes: 0,
        },
    )
    .unwrap();
    let before = connection.read_stats();
    let db = connection.db();
    let log = connection.log();
    std::fs::create_dir("other-working-directory").unwrap();
    // Only this isolated, single-test subprocess changes its CWD. The test
    // parent and all other integration tests retain their original CWD.
    std::env::set_current_dir("other-working-directory").unwrap();
    assert!(!std::path::Path::new("repository").exists());
    assert_eq!(db.basis_t(), basis);
    assert_eq!(db.values(entity, 1000).unwrap(), vec![Value::Long(37)]);
    let after_db = connection.read_stats();
    assert!(
        after_db.object_reads > before.object_reads,
        "database read must be cold"
    );
    let transactions = log
        .tx_range(Some(TimePoint::T(basis)), None)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_eq!(transactions.len(), 1);
    assert_eq!(transactions[0].t, basis);
    assert!(
        transactions[0]
            .data
            .iter()
            .any(|datom| datom.entity == entity
                && datom.attribute == 1000
                && datom.value == Value::Long(37)
                && datom.added)
    );
    assert!(
        connection.read_stats().object_reads > after_db.object_reads,
        "log read must be cold"
    );
    assert_eq!(connection.cache_stats().current_bytes, 0);
    drop(connection);
    assert_eq!(
        log.tx_data(IndexTransaction::T(basis)).unwrap(),
        Some(transactions[0].data.clone())
    );
    assert_eq!(db.values(entity, 1000).unwrap(), vec![Value::Long(37)]);
}

#[test]
fn relative_backup_cold_reads_survive_working_directory_change() {
    if std::env::var_os(CHILD).is_some() {
        child_reads();
        return;
    }
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP relative backup path: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let start = Instant::now();
    let fixture = common::PostgresFixture::new(&url, "backup_relative_path");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let created = BlockDatabase::create(&config, "relative", schema).unwrap();
    let service = common::start_service(&fixture.connection, "relative");
    let report = common::transact(
        &service,
        "seed",
        1,
        &[TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1000,
            value: Value::Long(37).into(),
        }],
        1000,
    );
    let entity = report.tempids["item"];
    let basis = report.basis_t;
    service.shutdown();
    let directory = tempfile::tempdir().unwrap();
    let repository = directory.path().join("repository");
    PortableBackup::connect(&fixture.connection)
        .unwrap()
        .backup_database("relative", &repository)
        .unwrap();
    drop((report, created));
    // Remove all source relations before launching the offline reader.
    drop(fixture);

    let output = Command::new(std::env::current_exe().unwrap())
        .args(["--exact", TEST, "--nocapture", "--test-threads=1"])
        .current_dir(directory.path())
        .env(CHILD, "1")
        .env(ENTITY, entity.to_string())
        .env(BASIS, basis.to_string())
        .env_remove("ATOMIC_POSTGRES_URL")
        .env_remove("ATOMIC_POSTGRES_TRANSPORT")
        .output()
        .unwrap();
    assert!(
        output.status.success(),
        "subprocess failed:\n{}\n{}",
        String::from_utf8_lossy(&output.stdout),
        String::from_utf8_lossy(&output.stderr)
    );
    drop(directory);
    eprintln!(
        "relative backup open + changed-CWD cold db/log reads, source removed: {:?}",
        start.elapsed()
    );
}
