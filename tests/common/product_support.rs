//! Shared scoped roles and executable lifecycle for local/remote product checks.
use postgres::{Client, NoTls};
use std::io::{BufRead, BufReader};
use std::path::{Path, PathBuf};
use std::process::{Child, Command, Output, Stdio};
use std::sync::mpsc;
use std::thread;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

pub fn atomic() -> Command {
    let mut command = Command::new(env!("CARGO_BIN_EXE_atomic"));
    for name in [
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
    command
}

pub fn configured(command: &mut Command, connection: &str) {
    for name in [
        "ATOMIC_POSTGRES_TLS_ROOT",
        "ATOMIC_TCP_USER_TIMEOUT_MS",
        "ATOMIC_KEEPALIVES",
        "ATOMIC_KEEPALIVES_IDLE_SECONDS",
        "ATOMIC_KEEPALIVES_INTERVAL_SECONDS",
        "ATOMIC_KEEPALIVES_RETRIES",
        "ATOMIC_SSD_CACHE_DIR",
        "ATOMIC_SSD_CACHE_ENTRIES",
        "ATOMIC_SSD_CACHE_BYTES",
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

pub fn success(output: Output) -> String {
    assert!(
        output.status.success(),
        "product command failed: {}\ncompleted fixture checks:\n{}",
        String::from_utf8_lossy(&output.stderr),
        String::from_utf8_lossy(&output.stdout)
    );
    String::from_utf8(output.stdout).unwrap()
}

pub fn cli(connection: &str, arguments: &[&str]) -> String {
    let mut command = atomic();
    configured(&mut command, connection);
    success(command.args(arguments).output().unwrap())
}

pub fn rejected_transactor(connection: &str, database: &str, endpoint: &Path) -> String {
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

/// This fixture owns only its unique schema and optional dedicated runtime roles.
pub struct Fixture {
    admin: Client,
    pub schema: String,
    pub admin_url: String,
    pub writer_url: String,
    pub peer_url: String,
    pub roles: Option<(String, String)>,
    pub can_inject_object_fault: bool,
}

impl Fixture {
    pub fn new(connection: &str) -> Self {
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
            can_inject_object_fault: true,
        };
        let privileges = fixture
            .admin
            .query_one(
                "SELECT rolsuper, rolsuper OR rolcreaterole FROM pg_roles WHERE rolname = current_user",
                &[],
            )
            .unwrap();
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

    fn current_root(&self, database: &str) -> atomic_core::storage::root::DatabaseRoot {
        let config = atomic_core::PostgresConnectionConfig::plaintext(&self.admin_url);
        let entry = atomic_core::DatabaseCatalog::connect_configured(&config)
            .unwrap()
            .resolve(database)
            .unwrap();
        let mut store = atomic_core::storage::PgBlockStore::connect(&config).unwrap();
        let reference = store
            .read_ref(&format!("databases/{}", entry.database_id))
            .unwrap()
            .unwrap();
        let id = reference.value.as_deref().unwrap().try_into().unwrap();
        atomic_core::storage::root::DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap())
            .unwrap()
    }

    pub fn remove_derived_publication(&self, database: &str) {
        // Explicit corruption of one current index object in this test-owned
        // schema. Canonical publication/log/receipts remain unchanged; ordinary
        // startup fails until the operator repairs the selected current index.
        let index = self.current_root(database).indexes.unwrap();
        assert_eq!(
            Client::connect(&self.admin_url, NoTls)
                .unwrap()
                .execute("DELETE FROM atomic_objects WHERE id=$1", &[&&index[..]])
                .unwrap(),
            1
        );
    }

    /// Zero or one readable current index, not a historical publication count.
    pub fn publication_count(&self, database: &str) -> i64 {
        let config = atomic_core::PostgresConnectionConfig::plaintext(&self.admin_url);
        let root = self.current_root(database);
        let Some(index) = root.indexes else {
            return 0;
        };
        let Some(bytes) = atomic_core::storage::PgBlockStore::connect(&config)
            .unwrap()
            .get(index)
            .unwrap()
        else {
            return 0;
        };
        i64::from(atomic_core::storage::IndexDescriptor::decode(&index, &bytes).is_ok())
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

pub fn parameter(connection: &str, name: &str, value: &str) -> String {
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

pub struct Server {
    pub child: Child,
    output: Option<thread::JoinHandle<()>>,
}

impl Server {
    pub fn start(connection: &str, database: &str, endpoint: &Path) -> Self {
        let mut command = atomic();
        configured(&mut command, connection);
        command
            .args([
                "transactor",
                "--index-threshold-bytes",
                "1",
                "--database",
                database,
                "--endpoint",
            ])
            .arg(endpoint);
        let server = Self::spawn(command);
        assert!(
            endpoint.exists(),
            "READY was announced before endpoint existed"
        );
        server
    }
    pub fn spawn(command: Command) -> Self {
        let (server, receiver) = Self::spawn_observed(command);
        let deadline = Instant::now() + Duration::from_secs(30);
        loop {
            let line = receiver
                .recv_timeout(deadline.saturating_duration_since(Instant::now()))
                .expect("transactor did not announce READY");
            if line.starts_with("READY ") {
                break;
            }
        }
        server
    }

    /// Observe startup/standby/health and later READY without blocking spawn.
    /// Dropping the observer discards later lines; the owned reader keeps pipes drained.
    pub fn spawn_observed(mut command: Command) -> (Self, mpsc::Receiver<String>) {
        let mut child = command
            .stdout(Stdio::piped())
            .stderr(Stdio::inherit())
            .spawn()
            .unwrap();
        let stdout = child.stdout.take().unwrap();
        let (sender, receiver) = mpsc::channel();
        let output = thread::spawn(move || {
            for line in BufReader::new(stdout).lines() {
                let Ok(line) = line else { break };
                let _ = sender.send(line);
            }
        });
        let server = Self {
            child,
            output: Some(output),
        };
        (server, receiver)
    }

    pub fn stop(&mut self) {
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

pub fn application_binary() -> PathBuf {
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
