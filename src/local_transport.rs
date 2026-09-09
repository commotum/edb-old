//! Same-host independent-process transaction delivery. The server creates a
//! private temporary directory and a mode-0600 Unix socket. There is no TCP
//! listener, implicit trust of a shared writable pathname, or durable mailbox
//! retaining transaction inputs. PostgreSQL remains the sole durable authority.
use crate::encoding::{
    WireOutcome, decode_submission, decode_submission_outcome, encode_submission,
    encode_submission_outcome,
};
use crate::{
    Connection, Digest, ErrorCategory, SemanticError, ServiceTransactionReport, TransactionClient,
    TransactionRequest,
};
use std::io::{self, Read, Write};
use std::os::unix::fs::PermissionsExt;
use std::os::unix::net::{UnixListener, UnixStream};
use std::path::{Path, PathBuf};
use std::sync::{
    Arc,
    atomic::{AtomicBool, Ordering},
};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

const MAX_FRAME: usize = 64 * 1024 * 1024 + 48;

/// Explicit deployment policies, not limits on Datalog or database size.
#[derive(Clone, Copy, Debug)]
pub struct LocalTransportConfig {
    pub max_in_flight: usize,
    /// Total deadline for one request/response/pin handoff, including clients
    /// that trickle bytes. Expiry after admission has an unknown outcome.
    pub request_timeout: Duration,
    pub max_frame_bytes: usize,
}

impl Default for LocalTransportConfig {
    fn default() -> Self {
        Self {
            max_in_flight: 4,
            request_timeout: Duration::from_secs(30),
            max_frame_bytes: MAX_FRAME,
        }
    }
}

pub struct LocalTransactionServer {
    endpoint: PathBuf,
    stop: Arc<AtomicBool>,
    worker: Option<JoinHandle<()>>,
    // Drop after the listener/workers: only our own ephemeral socket is removed.
    _directory: tempfile::TempDir,
}

impl LocalTransactionServer {
    /// Serve an existing fenced writer without owning its lease or lifetime.
    /// Share the returned endpoint only with trusted processes running as the
    /// same OS user. Restarting this adapter creates a new endpoint; reconnect
    /// clients explicitly rather than silently retargeting an old pathname.
    pub fn start(
        client: TransactionClient,
        config: LocalTransportConfig,
    ) -> Result<Self, SemanticError> {
        if config.max_in_flight == 0
            || config.max_in_flight > 256
            || config.request_timeout.is_zero()
            || config.max_frame_bytes < 48
            || config.max_frame_bytes > MAX_FRAME
            || Instant::now().checked_add(config.request_timeout).is_none()
        {
            return Err(SemanticError::incorrect(
                "transport/config",
                "invalid bounded transport settings",
            ));
        }
        let directory = tempfile::Builder::new()
            .prefix("atomic-writer-")
            .tempdir()
            .map_err(unavailable)?;
        std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700))
            .map_err(unavailable)?;
        let endpoint = directory.path().join("writer.sock");
        let listener = UnixListener::bind(&endpoint).map_err(unavailable)?;
        std::fs::set_permissions(&endpoint, std::fs::Permissions::from_mode(0o600))
            .map_err(unavailable)?;
        listener.set_nonblocking(true).map_err(unavailable)?;
        let stop = Arc::new(AtomicBool::new(false));
        let stopped = Arc::clone(&stop);
        let worker = thread::Builder::new()
            .name("atomic-local-listener".into())
            .spawn(move || {
                let mut workers: Vec<JoinHandle<()>> = Vec::new();
                while !stopped.load(Ordering::Acquire) {
                    let mut index = 0;
                    while index < workers.len() {
                        if workers[index].is_finished() {
                            let _ = workers.swap_remove(index).join();
                        } else {
                            index += 1;
                        }
                    }
                    match listener.accept() {
                        Ok((mut stream, _)) => {
                            if workers.len() >= config.max_in_flight {
                                let rejection = Err(SemanticError::new(
                                    ErrorCategory::Busy,
                                    "transport/full",
                                    "local transaction endpoint is at its in-flight limit",
                                ));
                                if let Ok(bytes) = encode_submission_outcome(&rejection) {
                                    let _ = write_frame(
                                        &mut stream,
                                        &bytes,
                                        Instant::now() + Duration::from_millis(10),
                                    );
                                }
                                continue;
                            }
                            let client = client.clone();
                            if let Ok(worker) = thread::Builder::new()
                                .name("atomic-local-request".into())
                                .spawn(move || {
                                    let _ = serve(stream, client, config);
                                })
                            {
                                workers.push(worker);
                            }
                        }
                        Err(error) if error.kind() == io::ErrorKind::WouldBlock => {
                            thread::sleep(Duration::from_millis(5))
                        }
                        Err(_) => break,
                    }
                }
                for worker in workers {
                    let _ = worker.join();
                }
            })
            .map_err(unavailable)?;
        Ok(Self {
            endpoint,
            stop,
            worker: Some(worker),
            _directory: directory,
        })
    }

    pub fn endpoint(&self) -> &Path {
        &self.endpoint
    }
}

impl Drop for LocalTransactionServer {
    fn drop(&mut self) {
        self.stop.store(true, Ordering::Release);
        if let Some(worker) = self.worker.take() {
            let _ = worker.join();
        }
    }
}

/// A confirmed durable success, even when opening its local native values
/// fails. Do not resubmit on `report: Err` as if the transaction were rejected;
/// use the stable request key to retrieve the same receipt after recovery.
#[derive(Debug)]
pub struct CommittedTransaction {
    pub basis_t: u64,
    pub tx_hash: Digest,
    pub replayed: bool,
    pub report: Result<ServiceTransactionReport, SemanticError>,
}

impl Connection {
    /// Submit to a separately running same-host writer. Before an authenticated
    /// response, a lost connection/timeout is UnknownOutcome. After success,
    /// local value-opening failures cannot turn that commit into a rejection.
    /// Remote semantic errors preserve category, details, and structured
    /// anomaly; their original code is in `details["remote_code"]`.
    pub fn transact_socket(
        &self,
        endpoint: impl AsRef<Path>,
        request: TransactionRequest,
        timeout: Duration,
    ) -> Result<CommittedTransaction, SemanticError> {
        let deadline = Instant::now()
            .checked_add(timeout)
            .filter(|_| !timeout.is_zero())
            .ok_or_else(|| {
                SemanticError::incorrect(
                    "transport/timeout",
                    "timeout must be positive and representable",
                )
            })?;
        let bytes = encode_submission(self.identity(), &request)?;
        if bytes.len() > MAX_FRAME {
            return Err(SemanticError::incorrect(
                "transport/frame-limit",
                "request exceeds transport frame policy",
            ));
        }
        let socket = socket2::Socket::new(socket2::Domain::UNIX, socket2::Type::STREAM, None)
            .map_err(unavailable)?;
        let address = socket2::SockAddr::unix(endpoint).map_err(unavailable)?;
        socket
            .connect_timeout(&address, remaining(deadline).map_err(unavailable)?)
            .map_err(unavailable)?;
        let mut stream: UnixStream = socket.into();
        // From the first attempted write onward, delivery/admission may have
        // happened. Never classify an ambiguous transport error as a rollback.
        write_frame(&mut stream, &bytes, deadline).map_err(unknown)?;
        let bytes = read_frame(&mut stream, MAX_FRAME, deadline).map_err(unknown)?;
        let outcome = decode_submission_outcome(&bytes).map_err(|error| {
            SemanticError::new(
                ErrorCategory::UnknownOutcome,
                "transport/invalid-outcome",
                "received no usable authenticated transaction outcome",
            )
            .detail("cause", error.to_string())
        })?;
        match outcome {
            WireOutcome::Rejected(error) => Err(error),
            WireOutcome::Committed(wire) => {
                let basis_t = wire.after.basis_t;
                let tx_hash = wire.after.tx_hash;
                let replayed = wire.replayed;
                let report = self.open_socket_report(*wire);
                // The server retains both source root/generation pins until
                // this handoff or its deadline. Successfully opened values now
                // own peer-local pins. ACK loss cannot undo the known commit.
                let _ = transfer(&mut stream, &mut [1], deadline, true);
                Ok(CommittedTransaction {
                    basis_t,
                    tx_hash,
                    replayed,
                    report,
                })
            }
        }
    }
}

fn serve(
    mut stream: UnixStream,
    client: TransactionClient,
    config: LocalTransportConfig,
) -> io::Result<()> {
    let deadline = Instant::now() + config.request_timeout;
    let bytes = read_frame(&mut stream, config.max_frame_bytes, deadline)?;
    let result = decode_submission(&bytes).and_then(|(identity, request)| {
        if identity != client.identity() {
            return Err(SemanticError::new(
                ErrorCategory::Conflict,
                "transport/database-identity",
                "endpoint and peer do not address the same database lineage",
            ));
        }
        client.transact(request, remaining(deadline).map_err(unavailable)?)
    });
    let bytes = encode_submission_outcome(&result).map_err(io::Error::other)?;
    if bytes.len() > config.max_frame_bytes {
        return Err(io::Error::other("response exceeds configured frame policy"));
    }
    write_frame(&mut stream, &bytes, deadline)?;
    if result.is_ok() {
        let mut ack = [0];
        let _ = transfer(&mut stream, &mut ack, deadline, false);
    }
    // Keep the exact report values (and their pins) alive through the ACK.
    drop(result);
    Ok(())
}

fn remaining(deadline: Instant) -> io::Result<Duration> {
    let remaining = deadline.saturating_duration_since(Instant::now());
    if remaining.is_zero() {
        Err(io::Error::new(
            io::ErrorKind::TimedOut,
            "native transport deadline elapsed",
        ))
    } else {
        Ok(remaining)
    }
}

fn transfer(
    stream: &mut UnixStream,
    bytes: &mut [u8],
    deadline: Instant,
    write: bool,
) -> io::Result<()> {
    let mut offset = 0;
    while offset < bytes.len() {
        let timeout = Some(remaining(deadline)?);
        let count = if write {
            stream.set_write_timeout(timeout)?;
            stream.write(&bytes[offset..])
        } else {
            stream.set_read_timeout(timeout)?;
            stream.read(&mut bytes[offset..])
        };
        match count {
            Ok(0) => {
                return Err(io::Error::new(
                    io::ErrorKind::UnexpectedEof,
                    "native endpoint disconnected",
                ));
            }
            Ok(count) => offset += count,
            Err(error) if error.kind() == io::ErrorKind::Interrupted => continue,
            Err(error) => return Err(error),
        }
    }
    Ok(())
}

fn write_frame(stream: &mut UnixStream, bytes: &[u8], deadline: Instant) -> io::Result<()> {
    let mut length = (bytes.len() as u64).to_be_bytes();
    transfer(stream, &mut length, deadline, true)?;
    // Bounded frames, but avoid duplicating their allocation during writes.
    let mut offset = 0;
    while offset < bytes.len() {
        stream.set_write_timeout(Some(remaining(deadline)?))?;
        match stream.write(&bytes[offset..]) {
            Ok(0) => {
                return Err(io::Error::new(
                    io::ErrorKind::WriteZero,
                    "native endpoint disconnected",
                ));
            }
            Ok(count) => offset += count,
            Err(error) if error.kind() == io::ErrorKind::Interrupted => continue,
            Err(error) => return Err(error),
        }
    }
    Ok(())
}

fn read_frame(stream: &mut UnixStream, maximum: usize, deadline: Instant) -> io::Result<Vec<u8>> {
    let mut length = [0; 8];
    transfer(stream, &mut length, deadline, false)?;
    let length = usize::try_from(u64::from_be_bytes(length)).map_err(io::Error::other)?;
    if !(48..=maximum).contains(&length) {
        return Err(io::Error::other("invalid native frame size"));
    }
    let mut bytes = vec![0; length];
    transfer(stream, &mut bytes, deadline, false)?;
    Ok(bytes)
}

fn unavailable(error: io::Error) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "transport/unavailable",
        error.to_string(),
    )
}

fn unknown(error: io::Error) -> SemanticError {
    SemanticError::new(
        ErrorCategory::UnknownOutcome,
        "transport/unknown-outcome",
        error.to_string(),
    )
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, EntityRef, Keyword, PostgresMigrator, PostgresStore, Schema,
        TransactionService, TransactionServiceConfig, TxOp, Value, ValueType,
    };
    use std::time::{SystemTime, UNIX_EPOCH};

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
        PostgresMigrator::connect(&postgres)
            .unwrap()
            .migrate()
            .unwrap();
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        PostgresStore::connect(&postgres)
            .unwrap()
            .create_database(&database, schema)
            .unwrap();
        let writer = TransactionService::start(TransactionServiceConfig {
            connection: postgres.clone(),
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
            let mut ack = [0];
            let _ = transfer(&mut stream, &mut ack, deadline, false);
            committed
        });
        let outcome = connection
            .transact_socket(&path, request(), Duration::from_secs(10))
            .unwrap();
        let committed = fake.join().unwrap();
        assert_eq!(outcome.basis_t, committed.basis_t);
        assert_eq!(outcome.tx_hash, committed.tx_hash);
        let error = outcome.report.unwrap_err();
        assert_eq!(error.code, "peer/exact-manifest-absent");
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
}
