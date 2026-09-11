//! Actual backup/offline-query/restore commands against isolated schemas.
mod common;
use atomic_core::*;
use postgres::{Client, NoTls};
use std::io::Write;
use std::process::{Command, Output, Stdio};
use std::time::{Duration, Instant};

fn command(connection: Option<&str>) -> Command {
    let mut command = Command::new(env!("CARGO_BIN_EXE_atomic"));
    for name in [
        "ATOMIC_POSTGRES_URL",
        "ATOMIC_POSTGRES_TRANSPORT",
        "ATOMIC_POSTGRES_TLS_ROOT",
        "ATOMIC_CONNECT_TIMEOUT_MS",
        "ATOMIC_STATEMENT_TIMEOUT_MS",
        "ATOMIC_LOCK_TIMEOUT_MS",
        "ATOMIC_SSD_CACHE_DIR",
        "ATOMIC_SSD_CACHE_ENTRIES",
        "ATOMIC_SSD_CACHE_BYTES",
    ] {
        command.env_remove(name);
    }
    if let Some(connection) = connection {
        command.env(
            "ATOMIC_POSTGRES_URL",
            common::plaintext_connection(connection),
        );
    }
    command
}
fn checked(output: Output) -> String {
    assert!(
        output.status.success(),
        "{}\n{}",
        String::from_utf8_lossy(&output.stdout),
        String::from_utf8_lossy(&output.stderr)
    );
    String::from_utf8(output.stdout).unwrap()
}
fn success(connection: Option<&str>, args: &[&str]) -> String {
    checked(command(connection).args(args).output().unwrap())
}
fn rejected(connection: Option<&str>, args: &[&str], code: &str) {
    let output = command(connection).args(args).output().unwrap();
    assert!(!output.status.success());
    assert!(
        String::from_utf8_lossy(&output.stderr).contains(code),
        "{}",
        String::from_utf8_lossy(&output.stderr)
    );
}
fn parameter(connection: &str, key: &str, value: &str) -> String {
    if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
        let encoded: String = value.bytes().map(|byte| format!("%{byte:02X}")).collect();
        format!(
            "{connection}{}{key}={encoded}",
            if connection.contains('?') { "&" } else { "?" }
        )
    } else {
        format!(
            "{connection} {key}='{}'",
            value.replace('\\', "\\\\").replace('\'', "\\'")
        )
    }
}

#[cfg(unix)]
fn interrupt_at_object_write(connection: &str, args: &[&str]) {
    use std::os::unix::process::ExitStatusExt;
    struct Child(std::process::Child);
    impl Drop for Child {
        fn drop(&mut self) {
            if self.0.try_wait().ok().flatten().is_none() {
                let _ = self.0.kill();
            }
            let _ = self.0.wait();
        }
    }
    let mut blocker = Client::connect(connection, NoTls).unwrap();
    blocker
        .batch_execute("BEGIN; LOCK TABLE atomic_objects IN SHARE MODE")
        .unwrap();
    let mut observer = Client::connect(connection, NoTls).unwrap();
    let application = format!("restore-crash-{}", std::process::id());
    let tagged = parameter(connection, "application_name", &application);
    let mut child = Child(
        command(Some(&tagged))
            .env("ATOMIC_LOCK_TIMEOUT_MS", "30000")
            .env("ATOMIC_STATEMENT_TIMEOUT_MS", "30000")
            .args(args)
            .stdout(Stdio::piped())
            .stderr(Stdio::piped())
            .spawn()
            .unwrap(),
    );
    let deadline = Instant::now() + Duration::from_secs(20);
    loop {
        let blocked: bool = observer.query_one("SELECT EXISTS(SELECT 1 FROM pg_catalog.pg_stat_activity WHERE application_name=$1 AND wait_event_type='Lock' AND query LIKE '%atomic_objects%')", &[&application]).unwrap().get(0);
        if blocked {
            break;
        }
        assert!(
            Instant::now() < deadline,
            "restore did not reach object write"
        );
        assert!(
            child.0.try_wait().unwrap().is_none(),
            "restore exited before staged write"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
    // Only this test-owned process and namespace are touched.
    assert_eq!(unsafe { libc::kill(child.0.id() as i32, libc::SIGTERM) }, 0);
    let status = child.0.wait().unwrap();
    assert_eq!(status.signal(), Some(libc::SIGTERM));
    blocker.batch_execute("ROLLBACK").unwrap();
}

#[test]
fn stock_backup_offline_stdin_query_verify_restore_and_inspect_use_current_blocks() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let source = common::PostgresFixture::new(&url, "cli_backup_source");
    let target = common::PostgresFixture::new(&url, "cli_backup_target");
    success(Some(&source.connection), &["install"]);
    success(Some(&target.connection), &["install"]);
    success(
        Some(&source.connection),
        &["create", "--database", "source"],
    );
    let writer = common::start_service(&source.connection, "source");
    let report = writer
        .client()
        .transact(
            TransactionRequest::new(
                "document",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: DB_DOC as u32,
                    value: Value::String("portable-cli-value".into()).into(),
                }],
            )
            .with_tx_instant(1000),
            Duration::from_secs(30),
        )
        .unwrap();
    writer.shutdown();
    let directory = common::private_directory();
    let repository = directory.path().to_str().unwrap();
    let backup = ["backup", "--database", "source", "--repository", repository];
    assert!(success(Some(&source.connection), &backup).contains("BACKED_UP"));
    assert!(success(Some(&source.connection), &backup).contains("objects_written=0"));
    let point = PortableBackup::list_backup_points(directory.path())
        .unwrap()
        .pop()
        .unwrap();
    let basis = point.basis_t.to_string();
    let generation = point.log_generation.to_string();
    assert!(
        success(None, &["list-backups", "--repository", repository]).contains("BACKUPS count=1")
    );
    let verify = [
        "verify-backup",
        "--repository",
        repository,
        "--basis",
        &basis,
        "--generation",
        &generation,
    ];
    assert!(success(None, &verify).contains("VERIFIED_DEEP"));
    let mut presence = verify.to_vec();
    presence.push("--presence-only");
    assert!(success(None, &presence).contains("VERIFIED_PRESENCE"));
    let mut child = command(None)
        .args(["query", "--repository", repository, "--file", "-"])
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
        .unwrap();
    child
        .stdin
        .take()
        .unwrap()
        .write_all(br#"[:find ?e :where [?e :db/doc "portable-cli-value"]]"#)
        .unwrap();
    let query = checked(child.wait_with_output().unwrap());
    assert!(
        query.contains(&report.tempids["item"].to_string()),
        "{query}"
    );
    let mut sql = Client::connect(&target.connection, NoTls).unwrap();
    let postgres_database: String = sql
        .query_one("SELECT current_database()", &[])
        .unwrap()
        .get(0);
    let restore = [
        "restore",
        "--repository",
        repository,
        "--basis",
        &basis,
        "--generation",
        &generation,
        "--target-database",
        "restored",
        "--postgres-database",
        &postgres_database,
        "--catalog-schema",
        &target.schema,
    ];
    assert!(success(Some(&target.connection), &restore).contains("RESTORE_PREVIEW"));
    assert!(
        DatabaseCatalog::connect(&target.connection)
            .unwrap()
            .list(None, 10)
            .unwrap()
            .is_empty()
    );
    let mut wrong = restore.to_vec();
    *wrong.last_mut().unwrap() = &source.schema;
    wrong.push("--apply");
    rejected(Some(&target.connection), &wrong, "cli/target-mismatch");
    let mut apply = restore.to_vec();
    apply.push("--apply");
    #[cfg(unix)]
    interrupt_at_object_write(&target.connection, &apply);
    assert!(success(Some(&target.connection), &apply).contains("RESTORED"));
    assert!(success(Some(&target.connection), &apply).contains("RESTORED"));
    assert!(
        success(
            Some(&target.connection),
            &["inspect", "--database", "restored"]
        )
        .contains("INSPECT healthy=true")
    );
    let value = Peer::connect(&target.connection, "restored", 0)
        .unwrap()
        .database_value();
    assert_eq!(
        value.values(report.tempids["item"], DB_DOC as u32).unwrap(),
        vec![Value::String("portable-cli-value".into())]
    );
    let retry_writer = common::start_service(&target.connection, "restored");
    let replay = retry_writer
        .client()
        .transact(
            TransactionRequest::new(
                "document",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: DB_DOC as u32,
                    value: Value::String("portable-cli-value".into()).into(),
                }],
            )
            .with_tx_instant(1000),
            Duration::from_secs(30),
        )
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tempids, report.tempids);
    assert_eq!(replay.tx_hash, report.tx_hash);
    retry_writer.shutdown();
}

#[test]
fn backup_administration_rejects_incomplete_options_before_credentials_or_files() {
    let directory = common::private_directory();
    let repository = directory.path().join("must-not-create");
    let repository = repository.to_str().unwrap();
    rejected(None, &["backup", "--repository", repository], "cli/usage");
    rejected(
        None,
        &["verify-backup", "--repository", repository, "--basis", "0"],
        "cli/usage",
    );
    rejected(
        None,
        &[
            "restore",
            "--repository",
            repository,
            "--basis",
            "0",
            "--generation",
            "0",
            "--target-database",
            "target",
            "--apply",
        ],
        "cli/usage",
    );
    assert!(!std::path::Path::new(repository).exists());
}
