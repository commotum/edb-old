//! Same-host independent-process transaction delivery. The server uses a
//! private directory and a mode-0600 Unix socket. There is no TCP
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
use std::fs::{File, Metadata, OpenOptions, TryLockError};
use std::io::{self, Read, Seek, Write};
use std::os::unix::fs::{FileTypeExt, MetadataExt, OpenOptionsExt, PermissionsExt};
use std::os::unix::net::{UnixListener, UnixStream};
use std::path::{Component, Path, PathBuf};
use std::sync::{
    Arc,
    atomic::{AtomicBool, Ordering},
};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

const MAX_FRAME: usize = 64 * 1024 * 1024 + 48;
const ENDPOINT_LOCK_HEADER: &str = "ATOMIC-LOCAL-ENDPOINT 1\n";

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
struct FileIdentity {
    device: u64,
    inode: u64,
}

impl FileIdentity {
    fn of(metadata: &Metadata) -> Self {
        Self {
            device: metadata.dev(),
            inode: metadata.ino(),
        }
    }

    fn matches(self, path: &Path) -> bool {
        std::fs::symlink_metadata(path).is_ok_and(|metadata| Self::of(&metadata) == self)
    }
}

/// The lock pathname is permanent: unlinking a locked file would let another
/// process lock a different inode at the same name. The record identifies the
/// only stale socket this adapter is allowed to reclaim after a process crash.
struct EndpointOwner {
    endpoint: PathBuf,
    directory: PathBuf,
    directory_identity: FileIdentity,
    lock_path: PathBuf,
    lock: File,
    lock_identity: FileIdentity,
    socket_identity: Option<FileIdentity>,
    _temporary: Option<tempfile::TempDir>,
}

impl EndpointOwner {
    fn bind(endpoint: &Path) -> Result<(UnixListener, Self), SemanticError> {
        let endpoint = if endpoint.is_absolute() {
            endpoint.to_owned()
        } else {
            std::env::current_dir().map_err(unavailable)?.join(endpoint)
        };
        if endpoint
            .components()
            .any(|component| matches!(component, Component::ParentDir | Component::CurDir))
        {
            return Err(unsafe_endpoint(
                "endpoint must not contain . or .. components",
            ));
        }
        let name = endpoint
            .file_name()
            .ok_or_else(|| unsafe_endpoint("endpoint must name a socket in a private directory"))?;
        let directory = endpoint
            .parent()
            .ok_or_else(|| unsafe_endpoint("endpoint requires a private parent directory"))?
            .to_owned();
        // SAFETY: geteuid takes no arguments and has no memory-safety preconditions.
        let uid = unsafe { libc::geteuid() };
        let mut prefix = PathBuf::new();
        for component in directory.components() {
            prefix.push(component.as_os_str());
            let metadata = std::fs::symlink_metadata(&prefix).map_err(unavailable)?;
            if !metadata.is_dir() || metadata.file_type().is_symlink() {
                return Err(unsafe_endpoint(
                    "endpoint directory components must not be symlinks",
                ));
            }
            // A root-owned sticky /tmp is safe for an owned private child.
            // Untrusted owners or non-sticky writable ancestors can replace it.
            if (metadata.uid() != uid && metadata.uid() != 0)
                || (metadata.mode() & 0o022 != 0 && metadata.mode() & 0o1000 == 0)
            {
                return Err(unsafe_endpoint(
                    "endpoint has an unsafe writable directory ancestor",
                ));
            }
        }
        let directory_file = OpenOptions::new()
            .read(true)
            .custom_flags(libc::O_DIRECTORY | libc::O_NOFOLLOW)
            .open(&directory)
            .map_err(unavailable)?;
        let metadata = directory_file.metadata().map_err(unavailable)?;
        if metadata.uid() != uid || metadata.mode() & 0o7777 != 0o700 {
            return Err(unsafe_endpoint(
                "endpoint parent must be owned by this user with mode 0700",
            ));
        }
        let directory_identity = FileIdentity::of(&metadata);
        if !directory_identity.matches(&directory) {
            return Err(unsafe_endpoint("endpoint directory changed during startup"));
        }
        let mut lock_name = name.to_owned();
        lock_name.push(".lock");
        let lock_path = directory.join(lock_name);
        // Publish an initialized, already-locked file atomically. Publishing an
        // empty file before taking its lock lets a simultaneous first start
        // mistake the initialization window for a broken ownership record.
        let mut candidate = tempfile::Builder::new()
            .prefix(".atomic-endpoint-lock-")
            .tempfile_in(&directory)
            .map_err(unavailable)?;
        candidate
            .write_all(ENDPOINT_LOCK_HEADER.as_bytes())
            .map_err(unavailable)?;
        candidate.as_file().sync_data().map_err(unavailable)?;
        candidate
            .as_file()
            .try_lock()
            .map_err(|error| unavailable(error.into()))?;
        let (mut lock, already_locked) = match candidate.persist_noclobber(&lock_path) {
            Ok(file) => (file, true),
            Err(error) if error.error.kind() == io::ErrorKind::AlreadyExists => {
                drop(error);
                let metadata = std::fs::symlink_metadata(&lock_path).map_err(unavailable)?;
                if !metadata.is_file() || metadata.file_type().is_symlink() {
                    return Err(unsafe_endpoint(
                        "endpoint lock must be a regular, non-symlink file",
                    ));
                }
                let lock = OpenOptions::new()
                    .read(true)
                    .write(true)
                    .custom_flags(libc::O_NOFOLLOW | libc::O_NONBLOCK)
                    .open(&lock_path)
                    .map_err(unavailable)?;
                (lock, false)
            }
            Err(error) => return Err(unavailable(error.error)),
        };
        let metadata = lock.metadata().map_err(unavailable)?;
        if !metadata.is_file()
            || metadata.uid() != uid
            || metadata.mode() & 0o7777 != 0o600
            || metadata.nlink() != 1
        {
            return Err(unsafe_endpoint(
                "endpoint lock must be an owned, unlinked-elsewhere mode-0600 file",
            ));
        }
        let lock_identity = FileIdentity::of(&metadata);
        if !lock_identity.matches(&lock_path) || !directory_identity.matches(&directory) {
            return Err(unsafe_endpoint(
                "endpoint ownership paths changed during startup",
            ));
        }
        if !already_locked {
            match lock.try_lock() {
                Ok(()) => {}
                Err(TryLockError::WouldBlock) => return Err(endpoint_busy()),
                Err(TryLockError::Error(error)) => return Err(unavailable(error)),
            }
        }
        let mut record = String::new();
        lock.rewind().map_err(unavailable)?;
        (&mut lock)
            .take(129)
            .read_to_string(&mut record)
            .map_err(unavailable)?;
        let record = record
            .strip_prefix(ENDPOINT_LOCK_HEADER)
            .filter(|record| record.len() <= 100)
            .ok_or_else(|| {
                unsafe_endpoint("endpoint lock is not a recognized Atomic ownership record")
            })?;
        let previous = if record.is_empty() {
            None
        } else {
            let mut fields = record.split_whitespace();
            let device = fields.next().and_then(|value| value.parse().ok());
            let inode = fields.next().and_then(|value| value.parse().ok());
            match (device, inode, fields.next()) {
                (Some(device), Some(inode), None) => Some(FileIdentity { device, inode }),
                _ => {
                    return Err(unsafe_endpoint(
                        "endpoint lock has an invalid socket identity",
                    ));
                }
            }
        };
        let mut owner = Self {
            endpoint,
            directory,
            directory_identity,
            lock_path,
            lock,
            lock_identity,
            socket_identity: None,
            _temporary: None,
        };
        match std::fs::symlink_metadata(&owner.endpoint) {
            Ok(metadata) => {
                let identity = FileIdentity::of(&metadata);
                if !metadata.file_type().is_socket()
                    || metadata.uid() != uid
                    || metadata.mode() & 0o7777 != 0o600
                    || Some(identity) != previous
                {
                    return Err(unsafe_endpoint(
                        "existing endpoint is not the recorded Atomic socket",
                    ));
                }
                let socket =
                    socket2::Socket::new(socket2::Domain::UNIX, socket2::Type::STREAM, None)
                        .map_err(unavailable)?;
                let address = socket2::SockAddr::unix(&owner.endpoint).map_err(unavailable)?;
                match socket.connect_timeout(&address, Duration::from_millis(100)) {
                    Ok(()) => return Err(endpoint_busy()),
                    Err(error) if error.kind() == io::ErrorKind::ConnectionRefused => {}
                    Err(error) => return Err(unavailable(error)),
                }
                owner.ensure_paths()?;
                if !identity.matches(&owner.endpoint) {
                    return Err(unsafe_endpoint("stale endpoint changed during startup"));
                }
                std::fs::remove_file(&owner.endpoint).map_err(unavailable)?;
            }
            Err(error) if error.kind() == io::ErrorKind::NotFound => {}
            Err(error) => return Err(unavailable(error)),
        }
        owner.ensure_paths()?;
        let listener = UnixListener::bind(&owner.endpoint).map_err(unavailable)?;
        let metadata = std::fs::symlink_metadata(&owner.endpoint).map_err(unavailable)?;
        if !metadata.file_type().is_socket() || metadata.uid() != uid {
            return Err(unsafe_endpoint("bound endpoint changed during startup"));
        }
        owner.socket_identity = Some(FileIdentity::of(&metadata));
        owner.ensure_paths()?;
        std::fs::set_permissions(&owner.endpoint, std::fs::Permissions::from_mode(0o600))
            .map_err(unavailable)?;
        listener.set_nonblocking(true).map_err(unavailable)?;
        let identity = owner.socket_identity.expect("bound socket identity");
        let record = format!(
            "{ENDPOINT_LOCK_HEADER}{} {}\n",
            identity.device, identity.inode
        );
        owner.lock.rewind().map_err(unavailable)?;
        owner
            .lock
            .write_all(record.as_bytes())
            .map_err(unavailable)?;
        owner
            .lock
            .set_len(record.len() as u64)
            .map_err(unavailable)?;
        owner.lock.sync_data().map_err(unavailable)?;
        Ok((listener, owner))
    }

    fn ensure_paths(&self) -> Result<(), SemanticError> {
        if self.directory_identity.matches(&self.directory)
            && self.lock_identity.matches(&self.lock_path)
        {
            Ok(())
        } else {
            Err(unsafe_endpoint("endpoint ownership paths changed"))
        }
    }
}

impl Drop for EndpointOwner {
    fn drop(&mut self) {
        // Cooperative instances cannot replace these paths while the file lock
        // is held. If a caller nevertheless replaces a path, leave it alone.
        if self.ensure_paths().is_ok()
            && self.socket_identity.is_some_and(|identity| {
                std::fs::symlink_metadata(&self.endpoint).is_ok_and(|metadata| {
                    metadata.file_type().is_socket() && FileIdentity::of(&metadata) == identity
                })
            })
        {
            let _ = std::fs::remove_file(&self.endpoint);
        }
        // The lock closes after cleanup; its permanent pathname is never removed.
    }
}

fn unsafe_endpoint(message: &'static str) -> SemanticError {
    SemanticError::incorrect("transport/unsafe-endpoint", message)
}

fn endpoint_busy() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Busy,
        "transport/endpoint-in-use",
        "local endpoint is already in use",
    )
}

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

impl LocalTransportConfig {
    /// Validate deployment bounds without starting a writer or binding a socket.
    pub fn validate(&self) -> Result<(), SemanticError> {
        if self.max_in_flight == 0
            || self.max_in_flight > 256
            || self.request_timeout.is_zero()
            || self.max_frame_bytes < 48
            || self.max_frame_bytes > MAX_FRAME
            || Instant::now().checked_add(self.request_timeout).is_none()
        {
            return Err(SemanticError::incorrect(
                "transport/config",
                "invalid bounded transport settings",
            ));
        }
        Ok(())
    }
}

/// A stable endpoint reserved before starting its writer service. Binding
/// performs the same ownership checks as [`LocalTransactionServer::start_at`]
/// and holds the listener and exclusive lock until it is started or dropped.
/// No requests are processed before `start`; dropping an unused endpoint
/// removes only its own socket, retaining the caller's directory and lock file.
pub struct LocalTransactionEndpoint {
    listener: UnixListener,
    owner: EndpointOwner,
}

impl LocalTransactionEndpoint {
    pub fn bind_at(endpoint: impl AsRef<Path>) -> Result<Self, SemanticError> {
        let (listener, owner) = EndpointOwner::bind(endpoint.as_ref())?;
        Ok(Self { listener, owner })
    }

    pub fn endpoint(&self) -> &Path {
        &self.owner.endpoint
    }

    pub fn start(
        self,
        client: TransactionClient,
        config: LocalTransportConfig,
    ) -> Result<LocalTransactionServer, SemanticError> {
        config.validate()?;
        self.owner.ensure_paths()?;
        if !self.owner.socket_identity.is_some_and(|identity| {
            std::fs::symlink_metadata(&self.owner.endpoint).is_ok_and(|metadata| {
                metadata.file_type().is_socket() && FileIdentity::of(&metadata) == identity
            })
        }) {
            return Err(unsafe_endpoint("prepared endpoint changed before startup"));
        }
        LocalTransactionServer::start_bound(client, config, self.listener, self.owner)
    }
}

pub struct LocalTransactionServer {
    endpoint: PathBuf,
    stop: Arc<AtomicBool>,
    worker: Option<JoinHandle<()>>,
    // Drop after the listener/workers; retain the exclusive lock through cleanup.
    _owner: EndpointOwner,
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
        config.validate()?;
        let directory = tempfile::Builder::new()
            .prefix("atomic-writer-")
            .tempdir()
            .map_err(unavailable)?;
        std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700))
            .map_err(unavailable)?;
        let endpoint = directory.path().join("writer.sock");
        let (listener, mut owner) = EndpointOwner::bind(&endpoint)?;
        owner._temporary = Some(directory);
        Self::start_bound(client, config, listener, owner)
    }

    /// Serve at a stable same-user endpoint in an existing owned mode-0700
    /// directory. Directory components cannot be symlinks or replaceable by
    /// other users. The socket is mode 0600; `<endpoint>.lock` is a permanent
    /// mode-0600 ownership record and must not be removed or replaced while the
    /// adapter is running. Startup fails if another adapter holds its lock.
    /// After a crash, only its recorded, no-longer-listening socket is reclaimed.
    /// Unexpected files/sockets are rejected without replacement. Dropping the
    /// adapter removes its own socket, leaving the caller's directory and lock.
    /// The writer lease/lifetime remains owned by the caller, as with `start`.
    pub fn start_at(
        client: TransactionClient,
        config: LocalTransportConfig,
        endpoint: impl AsRef<Path>,
    ) -> Result<Self, SemanticError> {
        config.validate()?;
        LocalTransactionEndpoint::bind_at(endpoint)?.start(client, config)
    }

    fn start_bound(
        client: TransactionClient,
        config: LocalTransportConfig,
        listener: UnixListener,
        owner: EndpointOwner,
    ) -> Result<Self, SemanticError> {
        let endpoint = owner.endpoint.clone();
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
            _owner: owner,
        })
    }

    pub fn endpoint(&self) -> &Path {
        &self.endpoint
    }

    /// Whether the listener worker is still running. This does not check the
    /// writer lease; a daemon must also supervise its transaction service.
    pub fn is_available(&self) -> bool {
        self.worker
            .as_ref()
            .is_some_and(|worker| !worker.is_finished())
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
}
