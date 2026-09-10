//! Actual operator processes use disposable, distinct PostgreSQL databases.
//! Catalog-wide GC must never run on the shared ATOMIC_POSTGRES_URL catalog.
mod common;

use atomic_core::*;
use postgres::{Client, NoTls};
use std::path::Path;
use std::process::{Child, Command, Output, Stdio};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

fn command(connection: Option<&str>) -> Command {
    let mut command = Command::new(env!("CARGO_BIN_EXE_atomic"));
    for variable in [
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
        command.env_remove(variable);
    }
    if let Some(connection) = connection {
        command
            .env("ATOMIC_POSTGRES_URL", connection)
            .env("ATOMIC_POSTGRES_TRANSPORT", "plaintext");
    }
    command
}
fn output(connection: Option<&str>, args: &[&str]) -> Output {
    command(connection).args(args).output().unwrap()
}
fn success(connection: Option<&str>, args: &[&str]) -> String {
    let output = output(connection, args);
    assert!(
        output.status.success(),
        "operator command failed: {}\n{}",
        String::from_utf8_lossy(&output.stderr),
        String::from_utf8_lossy(&output.stdout)
    );
    String::from_utf8(output.stdout).unwrap()
}
fn rejected(connection: Option<&str>, args: &[&str], code: &str) {
    let result = output(connection, args);
    assert!(!result.status.success());
    assert!(
        String::from_utf8_lossy(&result.stderr).contains(code),
        "unexpected error: {}",
        String::from_utf8_lossy(&result.stderr)
    );
}
fn parameter(connection: &str, name: &str, value: &str) -> String {
    if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
        let encoded: String = value
            .bytes()
            .map(|byte| {
                if byte.is_ascii_alphanumeric() || b"-._~".contains(&byte) {
                    char::from(byte).to_string()
                } else {
                    format!("%{byte:02X}")
                }
            })
            .collect();
        format!(
            "{connection}{}{name}={encoded}",
            if connection.contains('?') { "&" } else { "?" }
        )
    } else {
        format!(
            "{connection} {name}='{}'",
            value.replace('\\', "\\\\").replace('\'', "\\'")
        )
    }
}
struct Fixture {
    admin: Client,
    source: String,
    target: String,
    source_name: String,
    target_name: String,
    roles: Vec<String>,
}
impl Fixture {
    fn new(url: &str) -> Self {
        let mut admin = Client::connect(url, NoTls).unwrap();
        let suffix = format!(
            "{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let source_name = format!("atomic_cli_{suffix}_source");
        let target_name = format!("atomic_cli_{suffix}_target");
        for name in [&source_name, &target_name] {
            assert!(
                name.len() < 63 && name.bytes().all(|b| b.is_ascii_alphanumeric() || b == b'_')
            );
            admin
                .batch_execute(&format!("CREATE DATABASE {name}"))
                .unwrap();
        }
        Self {
            source: parameter(url, "dbname", &source_name),
            target: parameter(url, "dbname", &target_name),
            source_name,
            target_name,
            admin,
            roles: Vec::new(),
        }
    }
    fn peer(&mut self) -> String {
        let writer = format!("{}_writer", self.source_name);
        let peer = format!("{}_peer", self.source_name);
        for role in [&writer, &peer] {
            self.admin
                .batch_execute(&format!("CREATE ROLE {role} LOGIN"))
                .unwrap();
            self.roles.push(role.clone());
        }
        success(
            Some(&self.source),
            &["migrate", "--writer-role", &writer, "--peer-role", &peer],
        );
        parameter(&self.source, "user", &peer)
    }
}
impl Drop for Fixture {
    fn drop(&mut self) {
        // Every identifier was generated and validated here; never terminate
        // sessions or collect garbage in the supplied/shared maintenance DB.
        for name in [&self.source_name, &self.target_name] {
            let _ = self
                .admin
                .batch_execute(&format!("DROP DATABASE {name} WITH (FORCE)"));
        }
        for role in &self.roles {
            let _ = self.admin.batch_execute(&format!("DROP ROLE {role}"));
        }
    }
}

/// Own only the child created by this test, including panic cleanup. PostgreSQL
/// locks and any staging work are confined to Fixture's disposable destination.
struct RestoreChild(Child);
impl Drop for RestoreChild {
    fn drop(&mut self) {
        if self.0.try_wait().ok().flatten().is_none() {
            let _ = self.0.kill();
        }
        let _ = self.0.wait();
    }
}

#[cfg(unix)]
fn interrupt_restore_at_node_write(fixture: &Fixture, arguments: &[&str]) {
    use std::io::Read;
    use std::os::unix::process::ExitStatusExt;

    let mut blocker = Client::connect(&fixture.target, NoTls).unwrap();
    // SHARE permits native validation reads but stops an actual node INSERT.
    // This is an ordinary PostgreSQL lock, not a production fault hook.
    blocker
        .batch_execute("BEGIN; LOCK TABLE atomic_tree_nodes IN SHARE MODE")
        .unwrap();
    // Observe outside the long lock transaction so pg_stat_activity snapshots
    // are refreshed while new restore connections appear.
    let mut observer = Client::connect(&fixture.target, NoTls).unwrap();
    let application = format!("atomic-admin-restore-interruption-{}", std::process::id());
    let connection = parameter(&fixture.target, "application_name", &application);
    let mut child = RestoreChild(
        command(Some(&connection))
            .env("ATOMIC_LOCK_TIMEOUT_MS", "30000")
            .env("ATOMIC_STATEMENT_TIMEOUT_MS", "30000")
            .args(arguments)
            .stdout(Stdio::piped())
            .stderr(Stdio::null())
            .spawn()
            .unwrap(),
    );
    let deadline = Instant::now() + Duration::from_secs(15);
    loop {
        assert!(
            child.0.try_wait().unwrap().is_none(),
            "restore exited before the intended lock wait"
        );
        let waiting: bool = observer
            .query_one(
                "SELECT EXISTS(SELECT 1 FROM pg_catalog.pg_locks l \
                 JOIN pg_catalog.pg_stat_activity a ON a.pid=l.pid \
                 WHERE a.datname=current_database() AND a.application_name=$1 \
                 AND l.relation='atomic_tree_nodes'::regclass \
                 AND l.mode='RowExclusiveLock' AND NOT l.granted)",
                &[&application],
            )
            .unwrap()
            .get(0);
        if waiting {
            break;
        }
        assert!(
            Instant::now() < deadline,
            "restore never reached its node-write lock wait"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
    // The unreaped live child PID cannot be reused by an unrelated process.
    let pid = i32::try_from(child.0.id()).unwrap();
    assert_eq!(unsafe { libc::kill(pid, libc::SIGTERM) }, 0);
    let deadline = Instant::now() + Duration::from_secs(5);
    let status = loop {
        if let Some(status) = child.0.try_wait().unwrap() {
            break status;
        }
        assert!(
            Instant::now() < deadline,
            "SIGTERM did not terminate the restore process"
        );
        std::thread::sleep(Duration::from_millis(10));
    };
    assert_eq!(status.signal(), Some(libc::SIGTERM));
    let mut progress = String::new();
    child
        .0
        .stdout
        .take()
        .unwrap()
        .read_to_string(&mut progress)
        .unwrap();
    assert!(progress.contains("PROGRESS command=restore phase=verify-stage-activate"));
    blocker.batch_execute("ROLLBACK").unwrap();
    println!(
        "RESTORE_INTERRUPTED observed_node_write_lock=true signal=SIGTERM retry=same-exact-selection"
    );
}

#[test]
fn admin_argument_errors_do_not_need_credentials_or_create_files() {
    let directory = tempfile::tempdir().unwrap();
    let repository = directory.path().join("must-not-create");
    let repo = repository.to_str().unwrap();
    for args in [
        vec!["backup", "--repository", repo],
        vec!["verify-backup", "--repository", repo, "--basis", "0"],
        vec![
            "verify-backup",
            "--repository",
            repo,
            "--basis",
            "-1",
            "--generation",
            "0",
        ],
        vec![
            "restore",
            "--repository",
            repo,
            "--basis",
            "0",
            "--generation",
            "0",
            "--target-database",
            "target",
            "--apply",
        ],
        vec![
            "gc",
            "--postgres-database",
            "x",
            "--catalog-schema",
            "public",
            "--older-than-seconds",
            "0",
            "--batches",
            "0",
            "--apply",
        ],
        vec![
            "gc",
            "--postgres-database",
            "x",
            "--catalog-schema",
            "public",
            "--older-than-seconds",
            "0",
            "--batches",
            "2",
        ],
        vec![
            "fulltext-rebuild",
            "--database",
            "x",
            "--discard-manifest",
            "bad",
            "--apply",
        ],
    ] {
        rejected(None, &args, "cli/usage");
    }
    assert!(!repository.exists());
    let help = success(None, &["--help"]);
    assert!(help.contains("verify-backup") && help.contains("catalog-wide"));
}

#[test]
fn actual_admin_commands_backup_verify_restore_inspect_gc_and_repair() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED actual admin CLI: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let mut fixture = Fixture::new(&url);
    success(Some(&fixture.source), &["migrate"]);
    success(Some(&fixture.target), &["migrate"]);
    success(Some(&fixture.source), &["create", "--database", "source"]);
    rejected(
        Some(&fixture.source),
        &["inspect", "--database", "absent"],
        "ERROR",
    );
    rejected(
        Some(&fixture.source),
        &["fulltext-rebuild", "--database", "absent"],
        "cli/no-native-publication",
    );
    let peer_url = fixture.peer();
    let service = common::start_service(&fixture.source, "source");
    let schema = common::transact(
        &service,
        "schema",
        0,
        &[TxOp::InstallAttribute(
            Attribute::new(
                1000,
                Keyword::new("article", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )],
        1_000,
    );
    drop(schema);
    let seeded = common::transact(
        &service,
        "seed",
        1,
        &[TxOp::Add {
            entity: EntityRef::Temp("article".into()),
            attribute: 1000,
            value: Value::String("durable searchable facts".into()).into(),
        }],
        2_000,
    );
    let entity = seeded.tempids["article"];
    drop(seeded);
    let changed = common::transact(
        &service,
        "change",
        2,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: 1000,
            value: Value::String("durable immutable facts".into()).into(),
        }],
        3_000,
    );
    drop(changed);
    service.shutdown();
    let indexed = success(
        Some(&fixture.source),
        &["consolidate", "--database", "source"],
    );
    assert!(indexed.contains("INDEXED basis_t=3"));
    let status = success(Some(&fixture.source), &["status", "--database", "source"]);
    let inspected = success(Some(&fixture.source), &["inspect", "--database", "source"]);
    assert!(inspected.contains("INSPECT healthy=true deep_derived=true"));
    assert!(
        inspected.contains("pending_tree_publications=")
            && inspected.contains("pending_tree_membership_nodes=")
    );
    let directory = tempfile::tempdir().unwrap();
    let repository = directory.path().join("repository");
    let repo = repository.to_str().unwrap();
    let first = success(
        Some(&fixture.source),
        &["backup", "--database", "source", "--repository", repo],
    );
    let captured = PortableBackup::list_backup_points(&repository).unwrap();
    assert_eq!(captured.len(), 1);
    assert_eq!(captured[0].basis_t, 3);
    let generation = captured[0].log_generation.to_string();
    assert!(
        first.contains("BACKED_UP")
            && first.contains(&format!("basis_t=3 generation={generation}"))
            && first.contains("semantic_verification=false"),
        "{first}"
    );
    let repeated = success(
        Some(&fixture.source),
        &["backup", "--database", "source", "--repository", repo],
    );
    assert!(repeated.contains("objects_written=0"));
    rejected(
        Some(&fixture.source),
        &["backup", "--database", "absent", "--repository", repo],
        "ERROR",
    );
    assert!(success(None, &["list-backups", "--repository", repo]).contains("BACKUPS count=1"));
    let point = [
        "--repository",
        repo,
        "--basis",
        "3",
        "--generation",
        &generation,
    ];
    let mut verify = vec!["verify-backup"];
    verify.extend(point);
    assert!(success(None, &verify).contains("VERIFIED_DEEP"));
    verify.push("--presence-only");
    assert!(success(None, &verify).contains("VERIFICATION semantic=false"));
    rejected(
        None,
        &[
            "verify-backup",
            "--repository",
            repo,
            "--basis",
            "4",
            "--generation",
            &generation,
        ],
        "ERROR",
    );
    let mut restore = vec!["restore"];
    restore.extend(point);
    restore.extend([
        "--target-database",
        "restored",
        "--postgres-database",
        &fixture.target_name,
        "--catalog-schema",
        "public",
    ]);
    assert!(success(Some(&fixture.target), &restore).contains("RESTORE_PREVIEW"));
    assert!(
        PostgresStore::connect(&fixture.target)
            .unwrap()
            .database_status("restored")
            .is_err()
    );
    restore.push("--apply");
    #[cfg(unix)]
    interrupt_restore_at_node_write(&fixture, &restore);
    assert!(
        success(Some(&fixture.target), &restore).contains("RESTORED target_database=\"restored\"")
    );
    assert!(success(Some(&fixture.target), &restore).contains("basis_t=3"));
    assert!(
        success(
            Some(&fixture.target),
            &["inspect", "--database", "restored"]
        )
        .contains("INSPECT healthy=true")
    );
    let restored = Peer::connect(&fixture.target, "restored", 16).unwrap();
    assert_eq!(
        restored.db().values(entity, 1000).unwrap(),
        vec![Value::String("durable immutable facts".into())]
    );
    assert_eq!(
        restored.db().history().values(entity, 1000).unwrap().len(),
        3 // Original assertion, its retraction, and replacement assertion.
    );
    drop(restored);
    assert!(
        success(
            Some(&fixture.target),
            &["fulltext-rebuild", "--database", "restored"]
        )
        .contains("FULLTEXT_REBUILT basis_t=3")
    );
    let peer = Peer::connect(&fixture.target, "restored", 16).unwrap();
    assert_eq!(
        peer.db()
            .fulltext(1000, "immutable", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    let projection = peer
        .db()
        .native_fulltext_reader()
        .unwrap()
        .unwrap()
        .projection()
        .clone();
    drop(peer);
    // Delete one exact derived block only in the disposable target catalog.
    // Cold search must report the damage; canonical facts remain recoverable.
    let mut fault_client = Client::connect(&fixture.target, NoTls).unwrap();
    assert_eq!(
        common::with_replica_triggers_disabled(&mut fault_client, |client| {
            client.execute(
                "DELETE FROM atomic_fulltext_pages p USING atomic_fulltext_page_roots r \
                 WHERE r.manifest_hash=$1 AND r.root_hash=$2 AND p.block_hash=r.root_hash",
                &[&&projection.source_manifest[..], &&projection.root_hash[..]],
            )
        })
        .unwrap(),
        1
    );
    drop(fault_client);
    let damaged = Peer::connect(&fixture.target, "restored", 16).unwrap();
    assert_eq!(
        damaged
            .db()
            .fulltext(1000, "immutable", &FulltextOptions::default())
            .unwrap_err()
            .code,
        "fulltext/missing-block"
    );
    drop(damaged);
    let manifest: String = projection
        .source_manifest
        .iter()
        .map(|b| format!("{b:02x}"))
        .collect();
    rejected(
        Some(&fixture.target),
        &[
            "fulltext-rebuild",
            "--database",
            "restored",
            "--discard-manifest",
            &"0".repeat(64),
            "--apply",
        ],
        "cli/repair-target-mismatch",
    );
    let repaired = success(
        Some(&fixture.target),
        &[
            "fulltext-rebuild",
            "--database",
            "restored",
            "--discard-manifest",
            &manifest,
            "--apply",
            "--batches",
            "2",
        ],
    );
    assert!(repaired.contains("FULLTEXT_DISCARD") && repaired.contains("FULLTEXT_REBUILT"));
    let repaired_peer = Peer::connect(&fixture.target, "restored", 16).unwrap();
    assert_eq!(
        repaired_peer
            .db()
            .fulltext(1000, "immutable", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    drop(repaired_peer);

    // An unrelated target lineage and a wrong physical target both fail safely.
    success(
        Some(&fixture.target),
        &["create", "--database", "unrelated"],
    );
    let unrelated = success(
        Some(&fixture.target),
        &["status", "--database", "unrelated"],
    );
    let mut wrong = restore.clone();
    let at = wrong.iter().position(|s| *s == "restored").unwrap();
    wrong[at] = "unrelated";
    rejected(Some(&fixture.target), &wrong, "ERROR");
    assert_eq!(
        success(
            Some(&fixture.target),
            &["status", "--database", "unrelated"]
        ),
        unrelated
    );
    rejected(Some(&fixture.source), &restore, "cli/target-mismatch");

    let gc = [
        "gc",
        "--postgres-database",
        &fixture.source_name,
        "--catalog-schema",
        "public",
        "--older-than-seconds",
        "0",
    ];
    let before = success(Some(&fixture.source), &gc);
    assert!(before.contains("GC_PREVIEW batch=1 applied=false"));
    assert_eq!(
        success(Some(&fixture.source), &["status", "--database", "source"]),
        status
    );
    let mut denied = gc.to_vec();
    denied.push("--apply");
    rejected(Some(&peer_url), &denied, "ERROR");
    let mut wrong_gc = gc.to_vec();
    wrong_gc[2] = &fixture.target_name;
    wrong_gc.push("--apply");
    rejected(Some(&fixture.source), &wrong_gc, "cli/target-mismatch");
    let mut applied = gc.to_vec();
    applied.extend(["--apply", "--batches", "3"]);
    let collected = success(Some(&fixture.source), &applied);
    println!("{collected}");
    assert!(
        collected.contains("GC_APPLIED batch=3 applied=true")
            && collected.contains("global_quiescence=not-established")
    );
    assert_eq!(
        success(Some(&fixture.source), &["status", "--database", "source"]),
        status
    );
    assert!(
        success(Some(&fixture.source), &["inspect", "--database", "source"])
            .contains("INSPECT healthy=true")
    );
    assert!(success(None, &verify).contains("VERIFIED_PRESENCE"));
    println!(
        "ADMIN_ACCEPTANCE_OK dedicated_pg_databases=2 backup_repeat=true offline_verify=true restore_preview=true restore_retry=true interrupted_restore={} target_rejection=true deep_inspect=true gc_preview_apply=true restricted_gc_rejected=true fulltext_repair=true",
        cfg!(unix)
    );
    assert!(Path::new(repo).is_dir());
}
