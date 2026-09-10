#![cfg(unix)]

use postgres::{Client, NoTls};
use std::io::{BufRead, BufReader};
use std::os::unix::fs::PermissionsExt;
use std::path::{Path, PathBuf};
use std::process::{Child, Command, Output, Stdio};
use std::sync::mpsc;
use std::thread;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

fn atomic() -> Command {
    let mut command = Command::new(env!("CARGO_BIN_EXE_atomic"));
    for name in [
        "ATOMIC_POSTGRES_TLS_ROOT",
        "ATOMIC_CONNECT_TIMEOUT_MS",
        "ATOMIC_STATEMENT_TIMEOUT_MS",
        "ATOMIC_LOCK_TIMEOUT_MS",
    ] {
        command.env_remove(name);
    }
    command
}

fn configured(command: &mut Command, connection: &str) {
    for name in [
        "ATOMIC_POSTGRES_TLS_ROOT",
        "ATOMIC_TCP_USER_TIMEOUT_MS",
        "ATOMIC_KEEPALIVES",
        "ATOMIC_KEEPALIVES_IDLE_SECONDS",
        "ATOMIC_KEEPALIVES_INTERVAL_SECONDS",
        "ATOMIC_KEEPALIVES_RETRIES",
    ] {
        command.env_remove(name);
    }
    command
        .env("ATOMIC_POSTGRES_URL", connection)
        .env("ATOMIC_POSTGRES_TRANSPORT", "plaintext")
        .env("ATOMIC_CONNECT_TIMEOUT_MS", "5000")
        .env("ATOMIC_STATEMENT_TIMEOUT_MS", "30000")
        .env("ATOMIC_LOCK_TIMEOUT_MS", "10000");
}

fn success(output: Output) -> String {
    assert!(
        output.status.success(),
        "product command failed: {}",
        String::from_utf8_lossy(&output.stderr)
    );
    String::from_utf8(output.stdout).unwrap()
}

fn cli(connection: &str, arguments: &[&str]) -> String {
    let mut command = atomic();
    configured(&mut command, connection);
    success(command.args(arguments).output().unwrap())
}

fn rejected_transactor(connection: &str, database: &str, endpoint: &Path) -> String {
    let mut command = atomic();
    configured(&mut command, connection);
    let mut child = command
        .args(["transactor", "--database", database, "--endpoint"])
        .arg(endpoint)
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
        .unwrap();
    let deadline = Instant::now() + Duration::from_secs(15);
    while child.try_wait().unwrap().is_none() {
        if Instant::now() >= deadline {
            let _ = child.kill();
            let _ = child.wait();
            panic!("expected transactor startup rejection, but the process kept running");
        }
        thread::sleep(Duration::from_millis(10));
    }
    let output = child.wait_with_output().unwrap();
    assert!(!output.status.success());
    assert!(!String::from_utf8_lossy(&output.stdout).contains("READY"));
    assert!(!endpoint.exists());
    String::from_utf8(output.stderr).unwrap()
}

#[test]
fn product_configuration_is_explicit_and_redacts_connection_values() {
    const SECRET: &str = "product-private-password-marker";
    let parameters = format!("host=127.0.0.1 password={SECRET} invalid_setting={SECRET}");
    for transport in [None, Some("plaintext")] {
        let mut command = atomic();
        command
            .args(["status", "--database", "configuration-check"])
            .env("ATOMIC_POSTGRES_URL", &parameters);
        if let Some(transport) = transport {
            command.env("ATOMIC_POSTGRES_TRANSPORT", transport);
        } else {
            command.env_remove("ATOMIC_POSTGRES_TRANSPORT");
        }
        let output = command.output().unwrap();
        assert!(!output.status.success());
        let stderr = String::from_utf8_lossy(&output.stderr);
        assert!(!stderr.trim().is_empty());
        assert!(!stderr.contains(SECRET));
        assert!(!String::from_utf8_lossy(&output.stdout).contains(SECRET));
    }
    let mut command = atomic();
    configured(&mut command, &parameters);
    assert!(
        !command
            .args([
                "transactor",
                "--database",
                "configuration-check",
                "--unknown"
            ])
            .output()
            .unwrap()
            .status
            .success()
    );
}

/// This fixture owns only its unique schema and optional dedicated runtime roles.
struct Fixture {
    admin: Client,
    schema: String,
    admin_url: String,
    writer_url: String,
    peer_url: String,
    roles: Option<(String, String)>,
    can_inject_replica_fault: bool,
}

impl Fixture {
    fn new(connection: &str) -> Self {
        let mut admin = Client::connect(connection, NoTls).unwrap();
        let unique = format!(
            "product_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        admin
            .batch_execute(&format!("CREATE SCHEMA {unique}"))
            .unwrap();
        let scoped = parameter(
            connection,
            "options",
            &format!("-csearch_path={unique},pg_catalog"),
        );
        let mut fixture = Self {
            admin,
            schema: unique.clone(),
            admin_url: scoped.clone(),
            writer_url: scoped.clone(),
            peer_url: scoped,
            roles: None,
            can_inject_replica_fault: false,
        };
        let privileges = fixture
            .admin
            .query_one(
                "SELECT rolsuper, rolsuper OR rolcreaterole FROM pg_roles WHERE rolname = current_user",
                &[],
            )
            .unwrap();
        fixture.can_inject_replica_fault = privileges.get(0);
        let can_create_roles: bool = privileges.get(1);
        if can_create_roles {
            let writer = format!("{unique}_writer");
            let peer = format!("{unique}_peer");
            // Generated identifiers/password contain only ASCII letters, digits and underscores.
            fixture.admin.batch_execute(&format!(
                "CREATE ROLE {writer} LOGIN PASSWORD '{unique}'; CREATE ROLE {peer} LOGIN PASSWORD '{unique}'"
            )).unwrap();
            fixture.writer_url = parameter(
                &parameter(&fixture.admin_url, "user", &writer),
                "password",
                &unique,
            );
            fixture.peer_url = parameter(
                &parameter(&fixture.admin_url, "user", &peer),
                "password",
                &unique,
            );
            fixture.roles = Some((writer, peer));
        }
        fixture
    }

    fn remove_derived_publication(&self, database: &str) {
        // Deliberate fault, confined to this fixture's schema and logical database.
        // Replica mode bypasses immutability/FK triggers; no log/head is changed.
        let mut sql = Client::connect(&self.admin_url, NoTls).unwrap();
        let mut transaction = sql.transaction().unwrap();
        transaction
            .batch_execute("SET LOCAL session_replication_role = replica")
            .unwrap();
        assert!(
            transaction
                .execute(
                    "DELETE FROM atomic_tree_publications WHERE database_id = $1",
                    &[&database],
                )
                .unwrap()
                > 0
        );
        transaction
            .execute(
                "DELETE FROM atomic_tree_manifests WHERE database_id = $1",
                &[&database],
            )
            .unwrap();
        transaction.commit().unwrap();
    }

    fn publication_count(&self, database: &str) -> i64 {
        Client::connect(&self.admin_url, NoTls)
            .unwrap()
            .query_one(
                "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
                &[&database],
            )
            .unwrap()
            .get(0)
    }
}

impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
        if let Some((writer, peer)) = &self.roles {
            // These fixture-created roles never own application objects; remove their grants.
            let _ = self.admin.batch_execute(&format!(
                "DROP OWNED BY {writer}, {peer}; DROP ROLE {writer}; DROP ROLE {peer}"
            ));
        }
    }
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
        let escaped = value.replace('\\', "\\\\").replace('\'', "\\'");
        format!("{connection} {name}='{escaped}'")
    }
}

struct Server {
    child: Child,
    output: Option<thread::JoinHandle<()>>,
}

impl Server {
    fn start(connection: &str, database: &str, endpoint: &Path) -> Self {
        let mut command = atomic();
        configured(&mut command, connection);
        let mut child = command
            .args(["transactor", "--database", database, "--endpoint"])
            .arg(endpoint)
            .stdout(Stdio::piped())
            .stderr(Stdio::inherit())
            .spawn()
            .unwrap();
        let stdout = child.stdout.take().unwrap();
        let (sender, receiver) = mpsc::channel();
        let output = thread::spawn(move || {
            for line in BufReader::new(stdout).lines() {
                let Ok(line) = line else { break };
                if line.starts_with("READY ") {
                    let _ = sender.send(line);
                }
            }
        });
        let server = Self {
            child,
            output: Some(output),
        };
        receiver
            .recv_timeout(Duration::from_secs(30))
            .expect("transactor did not announce READY");
        assert!(
            endpoint.exists(),
            "READY was announced before the endpoint existed"
        );
        server
    }

    fn stop(&mut self) {
        assert!(
            Command::new("/bin/kill")
                .args(["-TERM", &self.child.id().to_string()])
                .status()
                .unwrap()
                .success()
        );
        let deadline = Instant::now() + Duration::from_secs(15);
        loop {
            if let Some(status) = self.child.try_wait().unwrap() {
                assert!(status.success(), "SIGTERM shutdown failed: {status}");
                break;
            }
            assert!(
                Instant::now() < deadline,
                "transactor did not stop after SIGTERM"
            );
            thread::sleep(Duration::from_millis(10));
        }
        if let Some(output) = self.output.take() {
            output.join().unwrap();
        }
    }
}

impl Drop for Server {
    fn drop(&mut self) {
        if !matches!(self.child.try_wait(), Ok(Some(_))) {
            let _ = self.child.kill();
            let _ = self.child.wait();
        }
        if let Some(output) = self.output.take() {
            let _ = output.join();
        }
    }
}

fn application_binary() -> PathBuf {
    let path = std::env::var_os("ATOMIC_APPLICATION_BIN")
        .map(PathBuf::from)
        .unwrap_or_else(|| {
            Path::new(env!("CARGO_BIN_EXE_atomic"))
                .parent()
                .unwrap()
                .join("examples/application_workflow")
        });
    assert!(
        path.is_file(),
        "build the application first: cargo build --bin atomic --example application_workflow; or set ATOMIC_APPLICATION_BIN"
    );
    path
}

#[test]
fn actual_product_commands_serve_a_separate_application_across_restart() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED real product acceptance: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let application = application_binary();
    let fixture = Fixture::new(&connection);
    if let Some((writer, peer)) = &fixture.roles {
        cli(
            &fixture.admin_url,
            &["migrate", "--writer-role", writer, "--peer-role", peer],
        );
    } else {
        cli(&fixture.admin_url, &["migrate"]);
        eprintln!("restricted-role witness unavailable: fixture account cannot create roles");
    }
    const DATABASE: &str = "application";
    cli(&fixture.admin_url, &["create", "--database", DATABASE]);
    cli(&fixture.admin_url, &["status", "--database", DATABASE]);
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("transactor.sock");
    if fixture.roles.is_some() {
        let error = rejected_transactor(&fixture.peer_url, DATABASE, &endpoint);
        assert!(
            error.contains("ERROR"),
            "rejection omitted its error category"
        );
        assert_eq!(fixture.publication_count(DATABASE), 0);
        println!("PEER_RUNTIME_REJECTED no_ready=true no_publication=true");
    }
    for round in 0..2 {
        let mut server = Server::start(&fixture.writer_url, DATABASE, &endpoint);
        let mut command = Command::new(&application);
        configured(&mut command, &fixture.peer_url);
        let output = success(
            command
                .args(["--database", DATABASE, "--endpoint"])
                .arg(&endpoint)
                .output()
                .unwrap(),
        );
        assert!(output.contains("APPLICATION_OK"));
        assert!(output.contains("BASELINE"));
        assert!(output.contains(if round == 0 {
            "seed_replayed=false"
        } else {
            "seed_replayed=true"
        }));
        assert!(output.contains(if round == 0 {
            "update_replayed=false"
        } else {
            "update_replayed=true"
        }));
        print!("round={round} {output}");
        server.stop();
        assert!(!endpoint.exists(), "normal shutdown retained the socket");
    }
    let committed_status = cli(&fixture.admin_url, &["status", "--database", DATABASE]);
    if fixture.can_inject_replica_fault {
        fixture.remove_derived_publication(DATABASE);
        let error = rejected_transactor(&fixture.writer_url, DATABASE, &endpoint);
        assert!(error.contains("service/native-index-required"));
        assert!(error.contains("atomic consolidate"));
        assert_eq!(fixture.publication_count(DATABASE), 0);
        assert_eq!(
            cli(&fixture.admin_url, &["status", "--database", DATABASE]),
            committed_status,
            "failed startup changed authoritative head state"
        );
        let indexed = cli(&fixture.admin_url, &["consolidate", "--database", DATABASE]);
        assert!(indexed.contains("INDEXED basis_t=3"));
        assert!(fixture.publication_count(DATABASE) > 0);
        let mut recovered = Server::start(&fixture.writer_url, DATABASE, &endpoint);
        recovered.stop();
        assert_eq!(
            cli(&fixture.admin_url, &["status", "--database", DATABASE]),
            committed_status,
            "derived recovery changed authoritative head state"
        );
        println!(
            "CLI_RECOVERY_OK diagnosed_missing_publication=true head_unchanged=true restarted=true"
        );
    } else {
        eprintln!(
            "missing-publication recovery witness unavailable: fixture account is not a superuser"
        );
    }
    println!(
        "PRODUCT_ACCEPTANCE_OK restricted_roles={} writer_restarts={} application_processes=2",
        fixture.roles.is_some(),
        1 + usize::from(fixture.can_inject_replica_fault)
    );
}
