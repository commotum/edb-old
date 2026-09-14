#![cfg(target_os = "linux")]
mod common;
#[path = "common/network.rs"]
mod network;
use common::product_support::*;
use std::os::unix::fs::PermissionsExt;
use std::path::{Path, PathBuf};
use std::process::Command;
use std::time::{Duration, Instant};

struct Credentials {
    _directory: tempfile::TempDir,
    certificate: PathBuf,
    key: PathBuf,
    token: PathBuf,
    wrong_token: PathBuf,
}
impl Credentials {
    fn new() -> Self {
        let directory = tempfile::tempdir().unwrap();
        let certificate = directory.path().join("certificate.pem");
        let key = directory.path().join("private.pem");
        let token = directory.path().join("token.hex");
        let wrong_token = directory.path().join("wrong-token.hex");
        success(
            Command::new("openssl")
                .args([
                    "req",
                    "-x509",
                    "-newkey",
                    "rsa:2048",
                    "-sha256",
                    "-days",
                    "1",
                    "-nodes",
                    "-subj",
                    "/CN=atomic.test",
                    "-addext",
                    "subjectAltName=DNS:atomic.test",
                    "-keyout",
                ])
                .arg(&key)
                .arg("-out")
                .arg(&certificate)
                .output()
                .unwrap(),
        );
        for path in [&token, &wrong_token] {
            success(
                Command::new("openssl")
                    .args(["rand", "-hex", "-out"])
                    .arg(path)
                    .arg("32")
                    .output()
                    .unwrap(),
            );
            std::fs::set_permissions(path, std::fs::Permissions::from_mode(0o600)).unwrap();
        }
        std::fs::set_permissions(&key, std::fs::Permissions::from_mode(0o600)).unwrap();
        Self {
            _directory: directory,
            certificate,
            key,
            token,
            wrong_token,
        }
    }
    fn client(&self, command: &mut Command) {
        command
            .env("ATOMIC_REMOTE_TLS_ROOT", &self.certificate)
            .env("ATOMIC_REMOTE_TOKEN_FILE", &self.token);
    }
    fn server(&self, command: &mut Command) {
        command
            .env("ATOMIC_REMOTE_TLS_CERT", &self.certificate)
            .env("ATOMIC_REMOTE_TLS_KEY", &self.key)
            .env("ATOMIC_REMOTE_TOKEN_FILE", &self.token);
    }
}
fn writer(fixture: &Fixture, credentials: &Credentials) -> Server {
    let mut command = atomic();
    configured(&mut command, &fixture.writer_url);
    credentials.server(&mut command);
    command.args([
        "transactor",
        "--database",
        "remote-application",
        "--listen",
        "192.0.2.1:0",
        "--advertise",
        "192.0.2.1:0",
        "--tls-server-name",
        "atomic.test",
        "--index-threshold-bytes",
        "1",
        "--lease-ms",
        "3000",
        "--renew-ms",
        "500",
    ]);
    Server::spawn(command)
}
fn app(lab: &network::NetworkLab, fixture: &Fixture, credentials: &Credentials) -> Command {
    let mut command = lab.peer_command(application_binary());
    configured(&mut command, &fixture.peer_url);
    credentials.client(&mut command);
    command.args(["--database", "remote-application"]);
    command
}
fn reference(
    lab: &network::NetworkLab,
    fixture: &Fixture,
    credentials: &Credentials,
    path: &Path,
) -> String {
    success(
        app(lab, fixture, credentials)
            .arg("--reference-in")
            .arg(path)
            .output()
            .unwrap(),
    )
}

#[test]
fn isolated_network_application_retry_replacement_and_reference_handoff() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED real isolated-network application: ATOMIC_POSTGRES_URL unset");
        return;
    };
    if network::enter("isolated_network_application_retry_replacement_and_reference_handoff") {
        return;
    }
    let lab = network::NetworkLab::new();
    let fixture = Fixture::new(&url);
    let (writer_role, peer_role) = fixture
        .roles
        .as_ref()
        .expect("network security witness requires disposable restricted roles");
    cli(
        &fixture.admin_url,
        &[
            "install",
            "--writer-role",
            writer_role,
            "--peer-role",
            peer_role,
        ],
    );
    cli(
        &fixture.admin_url,
        &["create", "--database", "remote-application"],
    );
    let credentials = Credentials::new();
    let directory = tempfile::tempdir().unwrap();
    let reference_path = directory.path().join("snapshot.reference");
    let mut server = writer(&fixture, &credentials);
    let started = Instant::now();
    let output = success(
        app(&lab, &fixture, &credentials)
            .args(["--remote", "--reference-out"])
            .arg(&reference_path)
            .output()
            .unwrap(),
    );
    for marker in [
        "APPLICATION_OK",
        "PLANNING_OK",
        "PARTITIONS_OK",
        "FULLTEXT_OK",
        "REFERENCE_WRITTEN",
    ] {
        assert!(output.contains(marker), "missing {marker}: {output}");
    }
    assert!(output.contains("basis_t=11"));
    assert!(output.contains("QUERY_SQL sql_calls=0"));
    assert!(
        reference(&lab, &fixture, &credentials, &reference_path).contains("REFERENCE_OK basis_t=2")
    );
    let bad = app(&lab, &fixture, &credentials)
        .env("ATOMIC_REMOTE_TOKEN_FILE", &credentials.wrong_token)
        .arg("--remote")
        .output()
        .unwrap();
    assert!(
        !bad.status.success(),
        "wrong token admitted application transactions"
    );
    let unauthorized = app(&lab, &fixture, &credentials)
        .env(
            "ATOMIC_POSTGRES_URL",
            parameter(&fixture.peer_url, "user", "atomic-no-such-fixture-role"),
        )
        .arg("--reference-in")
        .arg(&reference_path)
        .output()
        .unwrap();
    assert!(
        !unauthorized.status.success(),
        "reference was treated as an access capability"
    );
    let process_before = server.child.id();
    server.child.kill().unwrap();
    server.child.wait().unwrap();
    drop(server);
    // Wait beyond the deliberately short lease; no authoritative row is edited
    // to manufacture takeover. Discovery of the replacement uses a new port.
    std::thread::sleep(Duration::from_millis(3300));
    let mut replacement = writer(&fixture, &credentials);
    assert_ne!(process_before, replacement.child.id());
    let replay = success(
        app(&lab, &fixture, &credentials)
            .arg("--remote")
            .output()
            .unwrap(),
    );
    assert!(replay.contains("seed_replayed=true") && replay.contains("FULLTEXT_OK"));
    assert!(reference(&lab, &fixture, &credentials, &reference_path).contains("exact_key=true"));
    let pg = atomic_core::PostgresConnectionConfig::plaintext(&fixture.peer_url);
    let peer =
        atomic_core::Connection::connect_configured(pg.clone(), "remote-application", 128).unwrap();
    let held_reference =
        atomic_core::SnapshotReference::decode(&std::fs::read(&reference_path).unwrap()).unwrap();
    let held = held_reference.open(&pg, 128, 16 * 1024 * 1024).unwrap();
    let alpha = peer
        .db()
        .lookup(1000, &atomic_core::Value::String("alpha".into()))
        .unwrap()
        .unwrap();
    let token = atomic_core::RemoteAuthToken::from_hex(
        std::fs::read_to_string(&credentials.token).unwrap().trim(),
    )
    .unwrap();
    let tls = atomic_core::RemoteClientConfig::new(token)
        .unwrap()
        .with_root_certificate_pem(&std::fs::read(&credentials.certificate).unwrap())
        .unwrap();
    let endpoint = peer.discover_remote_writer(&pg).unwrap();
    peer.transact_remote(
        &endpoint,
        &tls,
        atomic_core::TransactionRequest::new(
            "remote-fixture/excise-alpha",
            vec![atomic_core::TxOp::Add {
                entity: atomic_core::EntityRef::Temp("redaction".into()),
                attribute: atomic_core::DB_EXCISE as u32,
                value: atomic_core::Value::Ref(alpha).into(),
            }],
        ),
        Duration::from_secs(20),
    )
    .unwrap()
    .report
    .unwrap();
    replacement.stop();
    // Held values remain independent of transactor uptime. Automatic excision
    // may already have activated; do not assume a new reference can still open.
    assert_eq!(held.basis_t(), held_reference.key().basis_t());
    let route_id = atomic_core::DatabaseCatalog::connect(&fixture.admin_url)
        .unwrap()
        .resolve("remote-application")
        .unwrap()
        .database_id;
    atomic_core::PostgresOperator::connect(&fixture.admin_url)
        .unwrap()
        .process_excision_requests(&route_id)
        .unwrap();
    let denied = app(&lab, &fixture, &credentials)
        .arg("--reference-in")
        .arg(&reference_path)
        .output()
        .unwrap();
    assert!(!denied.status.success());
    assert!(String::from_utf8_lossy(&denied.stderr).contains("snapshot/generation-not-current"));
    assert_eq!(
        held.values(alpha, 1001).unwrap(),
        vec![atomic_core::Value::Long(3)],
        "already-held value policy changed"
    );
    println!(
        "REMOTE_PRODUCT_OK isolated_networks=2 veth=true separate_application=true verified_tls=true restricted_roles=true invalid_token_rejected=true reference_authorized=true original_basis=2 later_basis=11 writer_crash_replacement=true exact_retry=true pre_excision_reference_rejected=true old_held_value_exact=true total_ms={}",
        started.elapsed().as_millis()
    );
}

#[test]
fn stock_auto_contender_health_and_cached_route_survive_process_crash() {
    use atomic_core::*;
    use std::io::{Read, Write};
    use std::net::{SocketAddr, TcpStream};
    use std::sync::mpsc::Receiver;

    fn observe(receiver: &Receiver<String>, prefix: &str) -> String {
        let deadline = Instant::now() + Duration::from_secs(30);
        loop {
            let line = receiver
                .recv_timeout(deadline.saturating_duration_since(Instant::now()))
                .expect("stock service did not reach expected state");
            if line.starts_with(prefix) {
                return line;
            }
        }
    }
    fn health(receiver: &Receiver<String>) -> SocketAddr {
        observe(receiver, "HEALTH ")
            .split_whitespace()
            .find_map(|part| part.strip_prefix("address="))
            .unwrap()
            .parse()
            .unwrap()
    }
    fn metric(receiver: &Receiver<String>, phase: &str) -> String {
        let deadline = Instant::now() + Duration::from_secs(30);
        loop {
            let line = receiver
                .recv_timeout(deadline.saturating_duration_since(Instant::now()))
                .unwrap();
            if line.contains("\"event\":\"atomic.metrics\"")
                && line.contains(&format!("\"phase\":\"{phase}\""))
            {
                return line;
            }
            assert!(
                !line.starts_with("READY "),
                "waiting contender unexpectedly activated"
            );
        }
    }
    fn probe(address: SocketAddr, path: &str, code: u16, phase: &str) {
        let mut socket = TcpStream::connect_timeout(&address, Duration::from_secs(2)).unwrap();
        socket
            .set_read_timeout(Some(Duration::from_secs(2)))
            .unwrap();
        write!(socket, "GET {path} HTTP/1.1\r\nHost: localhost\r\n\r\n").unwrap();
        let mut response = String::new();
        socket.read_to_string(&mut response).unwrap();
        assert!(
            response.starts_with(&format!("HTTP/1.1 {code} ")),
            "{response}"
        );
        assert!(
            response.contains(&format!("\"state\":\"{phase}\"")),
            "{response}"
        );
        assert!(!response.contains("password") && !response.contains("token"));
    }
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED stock standby product: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let started = Instant::now();
    let fixture = Fixture::new(&connection);
    let (writer, peer_role) = fixture
        .roles
        .as_ref()
        .expect("acceptance needs restricted roles");
    cli(
        &fixture.admin_url,
        &["install", "--writer-role", writer, "--peer-role", peer_role],
    );
    cli(&fixture.admin_url, &["create", "--database", "automatic"]);
    let credentials = Credentials::new();
    let spawn = |poll: &str| {
        let mut command = atomic();
        configured(&mut command, &fixture.writer_url);
        credentials.server(&mut command);
        command.args([
            "transactor",
            "--database",
            "automatic",
            "--mode",
            "auto",
            "--standby-poll-ms",
            poll,
            "--telemetry-ms",
            "25",
            "--lease-ms",
            "1000",
            "--renew-ms",
            "100",
            "--listen",
            "127.0.0.1:0",
            "--advertise",
            "127.0.0.1:0",
            "--tls-server-name",
            "atomic.test",
            "--health-listen",
            "127.0.0.1:0",
            "--index-threshold-bytes",
            "1",
            "--default-partition",
            ":db.part/user",
        ]);
        Server::spawn_observed(command)
    };
    let (mut first, first_lines) = spawn("25");
    let first_health = health(&first_lines);
    observe(&first_lines, "READY ");
    probe(first_health, "/health", 200, "active");
    probe(first_health, "/ready", 200, "active");

    let pg = PostgresConnectionConfig::plaintext(&fixture.peer_url);
    let peer = Connection::connect_configured(pg.clone(), "automatic", 128).unwrap();
    let empty = peer.db();
    let token =
        RemoteAuthToken::from_hex(std::fs::read_to_string(&credentials.token).unwrap().trim())
            .unwrap();
    let tls = RemoteClientConfig::new(token)
        .unwrap()
        .with_root_certificate_pem(&std::fs::read(&credentials.certificate).unwrap())
        .unwrap();
    let route = peer.remote_writer(pg, tls);
    route
        .transact(
            TransactionRequest::from_edn(
                "install-value",
                r#"[
      {:db/ident :item/value :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
    ]"#,
            )
            .unwrap(),
            Duration::from_secs(20),
        )
        .unwrap()
        .report
        .unwrap();
    let request =
        TransactionRequest::from_edn("stable-request", r#"[{:db/id "item" :item/value 42}]"#)
            .unwrap();
    let original = route
        .transact(request.clone(), Duration::from_secs(20))
        .unwrap();
    let original = original.report.unwrap();
    let original_route = route.cached_endpoint().unwrap();
    let active_event = metric(&first_lines, "active");
    assert!(active_event.contains(&format!("\"lease_epoch\":{}", original_route.lease_epoch())));
    assert!(active_event.contains("\"writer_available\":true"));
    assert!(active_event.contains("\"listener_ready\":true"));
    let attribute = original
        .db_after
        .entid(&Keyword::new("item", "value"))
        .unwrap() as u32;
    let entity = original.tempids["item"];
    assert_eq!(
        original.db_after.values(entity, attribute).unwrap(),
        [Value::Long(42)]
    );
    let (mut second, second_lines) = spawn("25");
    let second_health = health(&second_lines);
    observe(&second_lines, "STANDBY ");
    probe(second_health, "/health", 200, "standby");
    probe(second_health, "/ready", 503, "standby");
    metric(&second_lines, "waiting");

    // Cancellation must wake a persistent contender, not wait out its poll.
    let (mut cancelled, cancelled_lines) = spawn("20000");
    let cancelled_health = health(&cancelled_lines);
    observe(&cancelled_lines, "STANDBY ");
    probe(cancelled_health, "/ready", 503, "standby");
    let cancel_started = Instant::now();
    cancelled.stop();
    let cancel_elapsed = cancel_started.elapsed();
    assert!(cancel_elapsed < Duration::from_secs(3));

    // Actual SIGKILL: no graceful lease release or automatic client replay.
    let takeover = Instant::now();
    first.child.kill().unwrap();
    first.child.wait().unwrap();
    assert_eq!(
        original.db_after.values(entity, attribute).unwrap(),
        [Value::Long(42)]
    );
    assert_eq!(empty.basis_t(), 0);
    observe(&second_lines, "READY ");
    probe(second_health, "/ready", 200, "active");
    // One explicit exact retry through the cached dead route, recovered before
    // any request bytes are sent to its replacement.
    let retry = route.transact(request, Duration::from_secs(20)).unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, original.basis_t);
    assert_eq!(retry.tx_hash, original.tx_hash);
    assert_eq!(retry.report.unwrap().tempids, original.tempids);
    assert_ne!(route.cached_endpoint().unwrap(), original_route);
    let active_event = metric(&second_lines, "active");
    assert!(active_event.contains(&format!(
        "\"lease_epoch\":{}",
        route.cached_endpoint().unwrap().lease_epoch()
    )));
    assert!(active_event.contains("\"writer_available\":true"));
    assert!(active_event.contains("\"listener_ready\":true"));
    let fresh = route
        .transact(
            TransactionRequest::from_edn(
                "after-takeover",
                r#"[{:db/id "another" :item/value 99}]"#,
            )
            .unwrap(),
            Duration::from_secs(20),
        )
        .unwrap()
        .report
        .unwrap();
    assert_eq!(fresh.basis_t, original.basis_t + 1);
    assert_eq!(
        original.db_after.values(entity, attribute).unwrap(),
        [Value::Long(42)]
    );
    let active_id = DatabaseCatalog::connect(&fixture.admin_url)
        .unwrap()
        .resolve("automatic")
        .unwrap()
        .database_id;
    let mut operator = PostgresOperator::connect(&fixture.admin_url).unwrap();
    let deadline = Instant::now() + Duration::from_secs(10);
    loop {
        let basis = operator
            .inspect_database(&active_id, false)
            .unwrap()
            .metrics
            .index_basis_t;
        if basis >= fresh.basis_t {
            break;
        }
        assert!(
            Instant::now() < deadline,
            "takeover lost automatic indexing options"
        );
        std::thread::sleep(Duration::from_millis(20));
    }
    // Committed excision is stock background work, not an owner-only command.
    let excision = route
        .transact(
            TransactionRequest::new(
                "automatic-excision",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("redaction".into()),
                    attribute: DB_EXCISE as u32,
                    value: Value::Ref(entity).into(),
                }],
            ),
            Duration::from_secs(20),
        )
        .unwrap();
    let excised = peer
        .sync_excise(excision.basis_t, Duration::from_secs(30))
        .unwrap();
    assert!(excised.values(entity, attribute).unwrap().is_empty());
    let post_excision = route
        .transact(
            TransactionRequest::from_edn("healthy-after-excision", r#"[{:item/value 123}]"#)
                .unwrap(),
            Duration::from_secs(20),
        )
        .unwrap();
    assert!(!post_excision.replayed);
    probe(second_health, "/ready", 200, "active");
    second.stop();
    assert_eq!(
        fresh.db_after.values(entity, attribute).unwrap(),
        [Value::Long(42)]
    );
    println!(
        "STOCK_STANDBY_OK tls=true restricted_roles=true crash=true exact_retry=true cached_route=true automatic_excision=true held_reads=true health=true cancel_ms={} takeover_ms={} complete_ms={}",
        cancel_elapsed.as_millis(),
        takeover.elapsed().as_millis(),
        started.elapsed().as_millis()
    );
}
