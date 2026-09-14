use super::*;
use crate::{
    Attribute, Cardinality, EntityRef, Keyword, PostgresConnectionConfig, Schema,
    TransactionService, TransactionServiceConfig, TxOp, Value, ValueType,
};
use std::time::{SystemTime, UNIX_EPOCH};

fn endpoint_directory() -> tempfile::TempDir {
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    directory
}

#[test]
fn configured_endpoint_lock_and_record_bound_restart_and_stale_recovery() {
    let directory = endpoint_directory();
    let endpoint = directory.path().join("writer.sock");
    let (listener, mut owner) = EndpointOwner::bind(&endpoint).unwrap();
    assert_eq!(std::fs::metadata(&endpoint).unwrap().mode() & 0o777, 0o600);
    assert_eq!(
        std::fs::metadata(endpoint.with_extension("sock.lock"))
            .unwrap()
            .mode()
            & 0o777,
        0o600
    );
    assert_eq!(
        EndpointOwner::bind(&endpoint).err().unwrap().code,
        "transport/endpoint-in-use"
    );
    // Model process death without running the guard's cleanup. Closing the
    // listener and lock leaves exactly the record/socket recovered after SIGKILL.
    owner.socket_identity = None;
    drop(listener);
    drop(owner);
    assert!(endpoint.exists());
    let (listener, owner) = EndpointOwner::bind(&endpoint).unwrap();
    drop(listener);
    drop(owner);
    assert!(!endpoint.exists());
    assert!(directory.path().is_dir());
    assert!(directory.path().join("writer.sock.lock").is_file());
    let (listener, owner) = EndpointOwner::bind(&endpoint).unwrap();
    drop(listener);
    drop(owner);
    assert!(!endpoint.exists());
}

#[test]
fn configured_endpoint_concurrent_first_starts_have_exactly_one_owner() {
    let directory = endpoint_directory();
    let endpoint = directory.path().join("writer.sock");
    let barrier = Arc::new(std::sync::Barrier::new(2));
    let starts: Vec<_> = (0..2)
        .map(|_| {
            let barrier = Arc::clone(&barrier);
            let endpoint = endpoint.clone();
            thread::spawn(move || {
                barrier.wait();
                EndpointOwner::bind(&endpoint)
            })
        })
        .collect();
    let results: Vec<_> = starts
        .into_iter()
        .map(|worker| worker.join().unwrap())
        .collect();
    assert_eq!(results.iter().filter(|result| result.is_ok()).count(), 1);
    assert_eq!(
        results
            .iter()
            .find_map(|result| result.as_ref().err())
            .unwrap()
            .code,
        "transport/endpoint-in-use"
    );
    drop(results);
    assert!(!endpoint.exists());
}

#[test]
fn configured_endpoint_rejects_unsafe_directories_and_unrecognized_targets() {
    use std::os::unix::fs::symlink;
    let directory = endpoint_directory();
    let endpoint = directory.path().join("writer.sock");
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o755)).unwrap();
    assert_eq!(
        EndpointOwner::bind(&endpoint).err().unwrap().code,
        "transport/unsafe-endpoint"
    );
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let alias = directory.path().join("alias");
    symlink(directory.path(), &alias).unwrap();
    assert_eq!(
        EndpointOwner::bind(&alias.join("other.sock"))
            .err()
            .unwrap()
            .code,
        "transport/unsafe-endpoint"
    );
    let unexpected = directory.path().join("user-file");
    std::fs::write(&unexpected, b"do not replace").unwrap();
    std::fs::write(&endpoint, b"do not replace").unwrap();
    assert_eq!(
        EndpointOwner::bind(&endpoint).err().unwrap().code,
        "transport/unsafe-endpoint"
    );
    assert_eq!(std::fs::read(&endpoint).unwrap(), b"do not replace");
    std::fs::remove_file(&endpoint).unwrap();
    symlink(&unexpected, &endpoint).unwrap();
    assert_eq!(
        EndpointOwner::bind(&endpoint).err().unwrap().code,
        "transport/unsafe-endpoint"
    );
    assert_eq!(std::fs::read(&unexpected).unwrap(), b"do not replace");
    std::fs::remove_file(&endpoint).unwrap();
    let foreign = UnixListener::bind(&endpoint).unwrap();
    std::fs::set_permissions(&endpoint, std::fs::Permissions::from_mode(0o600)).unwrap();
    assert_eq!(
        EndpointOwner::bind(&endpoint).err().unwrap().code,
        "transport/unsafe-endpoint"
    );
    assert!(UnixStream::connect(&endpoint).is_ok());
    drop(foreign);
    // Even a dead socket cannot be reclaimed without a matching record.
    assert_eq!(
        EndpointOwner::bind(&endpoint).err().unwrap().code,
        "transport/unsafe-endpoint"
    );
    std::fs::remove_file(&endpoint).unwrap();
    let lock = directory.path().join("writer.sock.lock");
    std::fs::remove_file(&lock).unwrap();
    symlink(&unexpected, &lock).unwrap();
    assert_eq!(
        EndpointOwner::bind(&endpoint).err().unwrap().code,
        "transport/unsafe-endpoint"
    );
    assert_eq!(std::fs::read(&unexpected).unwrap(), b"do not replace");
    std::fs::remove_file(&lock).unwrap();
    std::fs::write(&lock, b"not Atomic").unwrap();
    std::fs::set_permissions(&lock, std::fs::Permissions::from_mode(0o600)).unwrap();
    assert_eq!(
        EndpointOwner::bind(&endpoint).err().unwrap().code,
        "transport/unsafe-endpoint"
    );
    assert_eq!(std::fs::read(&lock).unwrap(), b"not Atomic");
}

#[test]
fn configured_endpoint_cleanup_preserves_replacement_paths() {
    let directory = endpoint_directory();
    let endpoint = directory.path().join("writer.sock");
    let (listener, owner) = EndpointOwner::bind(&endpoint).unwrap();
    std::fs::remove_file(&endpoint).unwrap();
    std::fs::write(&endpoint, b"replacement").unwrap();
    drop(listener);
    drop(owner);
    assert_eq!(std::fs::read(&endpoint).unwrap(), b"replacement");
    std::fs::remove_file(&endpoint).unwrap();
    let (listener, owner) = EndpointOwner::bind(&endpoint).unwrap();
    let lock_path = directory.path().join("writer.sock.lock");
    std::fs::remove_file(&lock_path).unwrap();
    std::fs::write(&lock_path, b"replacement lock").unwrap();
    drop(listener);
    drop(owner);
    assert_eq!(std::fs::read(&lock_path).unwrap(), b"replacement lock");
    assert!(
        std::fs::symlink_metadata(&endpoint)
            .unwrap()
            .file_type()
            .is_socket()
    );
}

#[test]
fn configured_endpoint_reports_listener_exit() {
    let Some((_postgres, _database, writer, _peer)) = fixture() else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL is required for listener-health integration");
        return;
    };
    let directory = endpoint_directory();
    let endpoint = directory.path().join("writer.sock");
    let (listener, owner) = EndpointOwner::bind(&endpoint).unwrap();
    let server = LocalTransactionServer::start_bound(
        writer.client(),
        LocalTransportConfig::default(),
        listener,
        owner,
    )
    .unwrap();
    assert!(server.is_available());
    // Terminate the real listener loop while retaining the server value.
    // Health must reflect worker completion, not merely owner/lease lifetime.
    server.stop.store(true, Ordering::Release);
    let deadline = Instant::now() + Duration::from_secs(2);
    while server.is_available() && Instant::now() < deadline {
        thread::sleep(Duration::from_millis(5));
    }
    assert!(!server.is_available());
    drop(server);
    assert!(!endpoint.exists());
    writer.shutdown();
}

fn fixture() -> Option<(String, String, TransactionService, Connection)> {
    let postgres = std::env::var("ATOMIC_POSTGRES_URL").ok()?;
    let database = format!(
        "transport-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    let storage = PostgresConnectionConfig::plaintext(&postgres);
    crate::storage::PgBlockStore::install(&storage).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("item", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    crate::storage::BlockDatabase::create(&storage, &database, schema).unwrap();
    let writer = TransactionService::start(TransactionServiceConfig {
        connection: storage,
        database_id: database.clone(),
        holder_id: database.clone(),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 16,
        capacity_limits: Default::default(),
    })
    .unwrap();
    let connection = Connection::connect(&postgres, &database, 4).unwrap();
    Some((postgres, database, writer, connection))
}

fn request() -> TransactionRequest {
    TransactionRequest::new(
        "same-key",
        vec![TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1_000,
            value: Value::String("durable".into()).into(),
        }],
    )
}

#[test]
fn committed_response_closes_without_a_reader_acknowledgment() {
    let Some((_, _, writer, _)) = fixture() else {
        return;
    };
    let server = LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let mut stream = UnixStream::connect(server.endpoint()).unwrap();
    let deadline = Instant::now() + Duration::from_secs(10);
    let bytes = encode_submission(&writer.client().identity(), &request()).unwrap();
    write_frame(&mut stream, &bytes, deadline).unwrap();
    let response = read_frame(&mut stream, MAX_FRAME, deadline).unwrap();
    assert!(matches!(
        decode_submission_outcome(&response).unwrap(),
        WireOutcome::Committed(_)
    ));
    stream
        .set_read_timeout(Some(Duration::from_secs(1)))
        .unwrap();
    assert_eq!(stream.read(&mut [0]).unwrap(), 0);
    drop(server);
    writer.shutdown();
}

#[test]
fn admitted_socket_disconnect_is_unknown_and_retry_returns_the_same_receipt() {
    let Some((_, _, writer, connection)) = fixture() else {
        return;
    };
    let directory = tempfile::tempdir().unwrap();
    let path = directory.path().join("lost-response.sock");
    let listener = UnixListener::bind(&path).unwrap();
    let client = writer.client();
    let fake = thread::spawn(move || {
        let (mut stream, _) = listener.accept().unwrap();
        let bytes = read_frame(
            &mut stream,
            MAX_FRAME,
            Instant::now() + Duration::from_secs(10),
        )
        .unwrap();
        let (_, request) = decode_submission(&bytes).unwrap();
        let committed = client.transact(request, Duration::from_secs(10)).unwrap();
        drop(stream); // Actual COMMIT, deliberately lost response.
        committed
    });
    let error = connection
        .transact_socket(&path, request(), Duration::from_secs(10))
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::UnknownOutcome);
    let committed = fake.join().unwrap();
    let server = LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    assert_eq!(
        std::fs::metadata(server.endpoint())
            .unwrap()
            .permissions()
            .mode()
            & 0o777,
        0o600
    );
    assert_eq!(
        std::fs::metadata(server.endpoint().parent().unwrap())
            .unwrap()
            .permissions()
            .mode()
            & 0o777,
        0o700
    );
    let recovered = connection
        .transact_socket(server.endpoint(), request(), Duration::from_secs(10))
        .unwrap();
    assert!(recovered.replayed);
    assert_eq!(recovered.basis_t, committed.basis_t);
    assert_eq!(recovered.tx_hash, committed.tx_hash);
    let recovered = recovered.report.unwrap();
    assert_eq!(recovered.tempids, committed.tempids);
    assert_eq!(recovered.tx_data, committed.tx_data);
    assert_eq!(
        recovered
            .db_after
            .values(recovered.tempids["item"], 1_000)
            .unwrap(),
        vec![Value::String("durable".into())]
    );
    drop(server);
    writer.shutdown();
}

#[test]
fn confirmed_commit_survives_a_failed_local_native_root_open() {
    let Some((_, _, writer, connection)) = fixture() else {
        return;
    };
    let directory = tempfile::tempdir().unwrap();
    let path = directory.path().join("bad-receipt-root.sock");
    let listener = UnixListener::bind(&path).unwrap();
    let client = writer.client();
    let fake = thread::spawn(move || {
        let (mut stream, _) = listener.accept().unwrap();
        let deadline = Instant::now() + Duration::from_secs(10);
        let (_, request) =
            decode_submission(&read_frame(&mut stream, MAX_FRAME, deadline).unwrap()).unwrap();
        let committed = client.transact(request, Duration::from_secs(10)).unwrap();
        let mut bytes = encode_submission_outcome(&Ok(committed.clone())).unwrap();
        // Valid native response with an unavailable advertised before-root:
        // ATMC header 16, protocol/tag 2, exact endpoint 88, then root hash.
        bytes[16 + 2 + 88] ^= 1;
        let checksum_offset = bytes.len() - 32;
        let checksum = crate::sha256(&bytes[..checksum_offset]);
        bytes[checksum_offset..].copy_from_slice(&checksum);
        write_frame(&mut stream, &bytes, deadline).unwrap();
        committed
    });
    let outcome = connection
        .transact_socket(&path, request(), Duration::from_secs(10))
        .unwrap();
    let committed = fake.join().unwrap();
    assert_eq!(outcome.basis_t, committed.basis_t);
    assert_eq!(outcome.tx_hash, committed.tx_hash);
    let error = outcome.report.unwrap_err();
    assert_eq!(error.code, "storage/missing-object");
    assert_eq!(
        connection
            .sync_to(outcome.basis_t, Duration::from_secs(10))
            .unwrap()
            .basis_t(),
        outcome.basis_t
    );
    writer.shutdown();
}

#[test]
fn framed_reader_has_a_total_deadline_and_rejects_oversized_lengths() {
    let (mut reader, mut writer) = UnixStream::pair().unwrap();
    writer
        .write_all(&(MAX_FRAME as u64 + 1).to_be_bytes())
        .unwrap();
    assert!(
        read_frame(
            &mut reader,
            MAX_FRAME,
            Instant::now() + Duration::from_millis(50)
        )
        .is_err()
    );
    let (mut reader, mut writer) = UnixStream::pair().unwrap();
    writer.write_all(&48_u64.to_be_bytes()).unwrap();
    let started = Instant::now();
    assert!(read_frame(&mut reader, MAX_FRAME, started + Duration::from_millis(30)).is_err());
    assert!(started.elapsed() < Duration::from_secs(1));
}
