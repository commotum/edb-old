mod common;
use atomic_core::{
    Connection, LocalTransactionEndpoint, LocalTransactionServer, LocalTransportConfig, Schema,
    TransactionRequest, TransactionService, TransactionServiceConfig,
};
use std::io::{BufRead, BufReader, Write};
use std::os::unix::fs::PermissionsExt;
use std::process::{Child, Command, Stdio};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const WAIT: Duration = Duration::from_secs(10);

fn database(postgres: &str) -> String {
    let id = format!(
        "configured-endpoint-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    common::install(postgres).unwrap();
    common::TestStore::connect(postgres)
        .unwrap()
        .create_database(&id, Schema::new())
        .unwrap();
    id
}

fn writer(postgres: &str, database: &str) -> TransactionService {
    TransactionService::start(TransactionServiceConfig {
        connection: atomic_core::PostgresConnectionConfig::plaintext(postgres),
        database_id: database.to_owned(),
        holder_id: format!("endpoint-test-{}", std::process::id()),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 4,
        capacity_limits: Default::default(),
    })
    .unwrap()
}

fn endpoint_directory() -> tempfile::TempDir {
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    directory
}

#[test]
fn configured_endpoint_can_be_reserved_and_rolled_back_without_a_writer() {
    let directory = endpoint_directory();
    let endpoint = directory.path().join("writer.sock");
    let prepared = LocalTransactionEndpoint::bind_at(&endpoint).unwrap();
    assert_eq!(prepared.endpoint(), endpoint);
    assert_eq!(
        LocalTransactionEndpoint::bind_at(&endpoint)
            .err()
            .unwrap()
            .code,
        "transport/endpoint-in-use"
    );
    drop(prepared);
    assert!(!endpoint.exists());
    assert!(directory.path().join("writer.sock.lock").is_file());
    let prepared = LocalTransactionEndpoint::bind_at(&endpoint).unwrap();
    drop(prepared);
    assert!(!endpoint.exists());
    assert_eq!(
        LocalTransportConfig {
            max_in_flight: 0,
            ..Default::default()
        }
        .validate()
        .unwrap_err()
        .code,
        "transport/config"
    );
}

#[test]
fn configured_endpoint_restarts_and_preserves_exact_retry() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL is required for configured-endpoint integration");
        return;
    };
    let database = database(&postgres);
    let writer = writer(&postgres, &database);
    let peer = Connection::connect(&postgres, &database, 4).unwrap();
    let directory = endpoint_directory();
    let endpoint = directory.path().join("writer.sock");
    let config = LocalTransportConfig {
        request_timeout: WAIT,
        ..Default::default()
    };
    let prepared = LocalTransactionEndpoint::bind_at(&endpoint).unwrap();
    assert_eq!(
        prepared
            .start(
                writer.client(),
                LocalTransportConfig {
                    max_in_flight: 0,
                    ..config
                },
            )
            .err()
            .unwrap()
            .code,
        "transport/config"
    );
    assert!(
        !endpoint.exists(),
        "invalid config must roll back the socket"
    );
    let prepared = LocalTransactionEndpoint::bind_at(&endpoint).unwrap();
    std::fs::remove_file(&endpoint).unwrap();
    std::fs::write(&endpoint, b"caller replacement").unwrap();
    assert_eq!(
        prepared.start(writer.client(), config).err().unwrap().code,
        "transport/unsafe-endpoint"
    );
    assert_eq!(std::fs::read(&endpoint).unwrap(), b"caller replacement");
    std::fs::remove_file(&endpoint).unwrap();
    let server = LocalTransactionEndpoint::bind_at(&endpoint)
        .unwrap()
        .start(writer.client(), config)
        .unwrap();
    let request = TransactionRequest::new("stable-request", vec![]);
    let first = peer
        .transact_socket(&endpoint, request.clone(), WAIT)
        .unwrap();
    let before = first.report.as_ref().unwrap().db_before.clone();
    assert!(!first.replayed);
    assert_eq!(
        LocalTransactionServer::start_at(writer.client(), config, &endpoint)
            .err()
            .unwrap()
            .code,
        "transport/endpoint-in-use"
    );
    drop(server);
    assert!(!endpoint.exists());
    let restarted = LocalTransactionServer::start_at(writer.client(), config, &endpoint).unwrap();
    assert_eq!(restarted.endpoint(), endpoint);
    let replay = peer.transact_socket(&endpoint, request, WAIT).unwrap();
    assert!(replay.replayed);
    assert_eq!(
        (replay.basis_t, replay.tx_hash),
        (first.basis_t, first.tx_hash)
    );
    assert_eq!(replay.report.unwrap().db_before.basis_t(), before.basis_t());
    assert_eq!(before.basis_t() + 1, first.basis_t);
    drop(restarted);
    writer.shutdown();
    assert!(directory.path().is_dir());
    assert!(directory.path().join("writer.sock.lock").is_file());
}

struct ChildGuard(Child);

impl Drop for ChildGuard {
    fn drop(&mut self) {
        let _ = self.0.kill();
        let _ = self.0.wait();
    }
}

#[test]
fn configured_endpoint_recovers_a_sigkilled_adapter() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL is required for configured-endpoint integration");
        return;
    };
    let child_database = database(&postgres);
    let directory = endpoint_directory();
    let endpoint = directory.path().join("writer.sock");
    let mut child = ChildGuard(
        Command::new(std::env::current_exe().unwrap())
            .args([
                "--exact",
                "local_endpoint_child",
                "--nocapture",
                "--test-threads=1",
            ])
            .env("ATOMIC_ENDPOINT_CHILD_DATABASE", &child_database)
            .env("ATOMIC_ENDPOINT_CHILD_PATH", &endpoint)
            .stdout(Stdio::piped())
            .stderr(Stdio::inherit())
            .spawn()
            .unwrap(),
    );
    let stdout = child.0.stdout.take().unwrap();
    let (ready_tx, ready_rx) = std::sync::mpsc::sync_channel(1);
    let output = std::thread::spawn(move || {
        for line in BufReader::new(stdout).lines() {
            let line = line.unwrap();
            if line.contains("ENDPOINT_READY") {
                let _ = ready_tx.send(());
                break;
            }
        }
    });
    ready_rx
        .recv_timeout(WAIT)
        .expect("child adapter did not become ready");
    output.join().unwrap();
    child.0.kill().unwrap();
    assert!(!child.0.wait().unwrap().success());
    assert!(endpoint.exists(), "SIGKILL must leave a real stale socket");
    // The endpoint guard protects adapter ownership independently of the
    // writer's lease. A separately provisioned writer avoids turning this
    // focused adapter check into another lease-expiry test.
    let replacement_database = database(&postgres);
    let replacement = writer(&postgres, &replacement_database);
    let server = LocalTransactionServer::start_at(
        replacement.client(),
        LocalTransportConfig::default(),
        &endpoint,
    )
    .unwrap();
    let peer = Connection::connect(&postgres, &replacement_database, 4).unwrap();
    assert!(
        peer.transact_socket(
            &endpoint,
            TransactionRequest::new("after-crash", vec![]),
            WAIT
        )
        .unwrap()
        .report
        .is_ok()
    );
    // The filesystem endpoint is not database identity: a client for the old
    // lineage must still be rejected by the unchanged submission protocol.
    let old_peer = Connection::connect(&postgres, &child_database, 4).unwrap();
    let error = old_peer
        .transact_socket(
            &endpoint,
            TransactionRequest::new("wrong-lineage", vec![]),
            WAIT,
        )
        .unwrap_err();
    assert_eq!(
        error.details.get("remote_code").map(String::as_str),
        Some("transport/database-identity")
    );
    drop(server);
    replacement.shutdown();
}

#[test]
fn local_endpoint_child() {
    let Ok(database) = std::env::var("ATOMIC_ENDPOINT_CHILD_DATABASE") else {
        return;
    };
    let postgres = std::env::var("ATOMIC_POSTGRES_URL").unwrap();
    let endpoint = std::env::var("ATOMIC_ENDPOINT_CHILD_PATH").unwrap();
    let writer = writer(&postgres, &database);
    let _server =
        LocalTransactionServer::start_at(writer.client(), Default::default(), endpoint).unwrap();
    println!("ENDPOINT_READY");
    std::io::stdout().flush().unwrap();
    // Parent SIGKILL is the assertion's deliberate fault, not normal shutdown.
    loop {
        std::thread::park();
    }
}
