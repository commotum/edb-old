//! Opt-in protocol witness, not a production transport or network benchmark.
//! Only Sync/Query/ReadyForQuery message counts are retained. SQL, parameters,
//! authentication contents, and message payloads are never logged or retained.

use super::*;
use crate::persistent_tree::{TreeConfig, build_tree};
use crate::sql_io::{OperationContext, OperationKind};
use crate::{Datom, PostgresMigrator, USER_PARTITION, Value, make_eid, t_to_tx};
use postgres::config::{Host, SslMode};
use postgres::{Config, NoTls};
use std::io::{Cursor, Read, Write};
use std::net::Shutdown;
use std::os::unix::fs::PermissionsExt;
use std::os::unix::net::{UnixListener, UnixStream};
use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};
use std::sync::{Arc, Mutex};
use std::thread::{self, JoinHandle};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const MAX_STARTUP_BYTES: usize = 64 * 1024;
const MAX_MESSAGE_BYTES: usize = 128 * 1024 * 1024;

#[derive(Default)]
struct Counters {
    sync: AtomicU64,
    query: AtomicU64,
    ready: AtomicU64,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
struct Counts {
    sync: u64,
    query: u64,
    ready: u64,
}

impl Counters {
    fn snapshot(&self) -> Counts {
        Counts {
            sync: self.sync.load(Ordering::SeqCst),
            query: self.query.load(Ordering::SeqCst),
            ready: self.ready.load(Ordering::SeqCst),
        }
    }
}

impl Counts {
    fn since_before_trailing_fence(self, before: Self) -> Self {
        Self {
            sync: self.sync.checked_sub(before.sync).unwrap(),
            query: self.query.checked_sub(before.query + 1).unwrap(),
            ready: self.ready.checked_sub(before.ready + 1).unwrap(),
        }
    }
}

fn framing_error() -> io::Error {
    io::Error::new(
        io::ErrorKind::InvalidData,
        "invalid test-relay protocol framing",
    )
}

fn copy_exact(
    reader: &mut impl Read,
    writer: &mut impl Write,
    mut remaining: usize,
) -> io::Result<()> {
    let mut buffer = [0_u8; 8192];
    while remaining > 0 {
        let amount = remaining.min(buffer.len());
        reader.read_exact(&mut buffer[..amount])?;
        writer.write_all(&buffer[..amount])?;
        remaining -= amount;
    }
    Ok(())
}

fn read_message_header(reader: &mut impl Read) -> io::Result<Option<([u8; 5], usize)>> {
    let mut header = [0_u8; 5];
    match reader.read(&mut header[..1])? {
        0 => return Ok(None),
        1 => {}
        _ => unreachable!(),
    }
    reader.read_exact(&mut header[1..])?;
    let length = u32::from_be_bytes(header[1..].try_into().unwrap()) as usize;
    if !(4..=MAX_MESSAGE_BYTES).contains(&length) {
        return Err(framing_error());
    }
    Ok(Some((header, length - 4)))
}

fn frontend(
    reader: &mut impl Read,
    writer: &mut impl Write,
    counters: &Counters,
) -> io::Result<()> {
    let mut startup = [0_u8; 8];
    reader.read_exact(&mut startup)?;
    let length = u32::from_be_bytes(startup[..4].try_into().unwrap()) as usize;
    let version = u32::from_be_bytes(startup[4..].try_into().unwrap());
    // Accept only a plaintext protocol-3 startup, never SSL/GSS negotiation or
    // cancellation packets. No startup fields (including credentials) decoded.
    if !(8..=MAX_STARTUP_BYTES).contains(&length) || version != 196_608 {
        return Err(framing_error());
    }
    writer.write_all(&startup)?;
    copy_exact(reader, writer, length - 8)?;
    while let Some((header, payload)) = read_message_header(reader)? {
        match header[0] {
            b'S' if payload == 0 => {
                counters.sync.fetch_add(1, Ordering::SeqCst);
            }
            b'S' => return Err(framing_error()),
            b'Q' => {
                counters.query.fetch_add(1, Ordering::SeqCst);
            }
            _ => {}
        }
        writer.write_all(&header)?;
        copy_exact(reader, writer, payload)?;
        if header[0] == b'X' {
            return Ok(());
        }
    }
    Ok(())
}

fn backend(reader: &mut impl Read, writer: &mut impl Write, counters: &Counters) -> io::Result<()> {
    while let Some((header, payload)) = read_message_header(reader)? {
        if header[0] == b'Z' {
            if payload != 1 {
                return Err(framing_error());
            }
            let mut status = [0_u8; 1];
            reader.read_exact(&mut status)?;
            // Count the complete Ready before forwarding it. A completed fence
            // therefore provides a precise counter boundary without polling.
            counters.ready.fetch_add(1, Ordering::SeqCst);
            writer.write_all(&header)?;
            writer.write_all(&status)?;
        } else {
            writer.write_all(&header)?;
            copy_exact(reader, writer, payload)?;
        }
    }
    Ok(())
}

struct Relay {
    directory: tempfile::TempDir,
    stop: Arc<AtomicBool>,
    shutdown: Arc<Mutex<Vec<UnixStream>>>,
    counters: Arc<Counters>,
    worker: Option<JoinHandle<io::Result<()>>>,
}

impl Relay {
    fn start(target: PathBuf, port: u16) -> io::Result<Self> {
        let directory = tempfile::tempdir()?;
        std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700))?;
        let listener = UnixListener::bind(directory.path().join(format!(".s.PGSQL.{port}")))?;
        listener.set_nonblocking(true)?;
        let stop = Arc::new(AtomicBool::new(false));
        let shutdown = Arc::new(Mutex::new(Vec::new()));
        let counters = Arc::new(Counters::default());
        let relay_stop = Arc::clone(&stop);
        let relay_shutdown = Arc::clone(&shutdown);
        let relay_counters = Arc::clone(&counters);
        let worker = thread::spawn(move || {
            let mut downstream = loop {
                if relay_stop.load(Ordering::SeqCst) {
                    return Ok(());
                }
                match listener.accept() {
                    Ok((stream, _)) => break stream,
                    Err(error) if error.kind() == io::ErrorKind::WouldBlock => {
                        thread::sleep(Duration::from_millis(2))
                    }
                    Err(error) => return Err(error),
                }
            };
            let mut upstream = UnixStream::connect(target)?;
            for stream in [&downstream, &upstream] {
                stream.set_read_timeout(Some(Duration::from_secs(10)))?;
                stream.set_write_timeout(Some(Duration::from_secs(10)))?;
                relay_shutdown.lock().unwrap().push(stream.try_clone()?);
            }
            let mut frontend_reader = downstream.try_clone()?;
            let mut frontend_writer = upstream.try_clone()?;
            let frontend_counters = Arc::clone(&relay_counters);
            let front = thread::spawn(move || {
                let result = frontend(
                    &mut frontend_reader,
                    &mut frontend_writer,
                    &frontend_counters,
                );
                let _ = frontend_writer.shutdown(Shutdown::Write);
                result
            });
            let result = backend(&mut upstream, &mut downstream, &relay_counters);
            let _ = upstream.shutdown(Shutdown::Both);
            let _ = downstream.shutdown(Shutdown::Both);
            let front = front.join().map_err(|_| framing_error())?;
            result.and(front)
        });
        Ok(Self {
            directory,
            stop,
            shutdown,
            counters,
            worker: Some(worker),
        })
    }

    fn finish(mut self) -> io::Result<()> {
        self.worker
            .take()
            .unwrap()
            .join()
            .map_err(|_| framing_error())?
    }
}

impl Drop for Relay {
    fn drop(&mut self) {
        self.stop.store(true, Ordering::SeqCst);
        for stream in self.shutdown.lock().unwrap().iter() {
            let _ = stream.shutdown(Shutdown::Both);
        }
        if let Some(worker) = self.worker.take() {
            let _ = worker.join();
        }
    }
}

fn nodes(label: &str) -> Vec<(Digest, Vec<u8>)> {
    let salt = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    let datoms = (0..64).map(|index| Datom {
        entity: make_eid(USER_PARTITION, index + 1).unwrap(),
        attribute: 1000,
        value: Value::String(format!(
            "protocol-{label}-{salt}-{index}{}",
            "x".repeat(1024)
        )),
        tx: t_to_tx(1).unwrap(),
        added: true,
    });
    let tree = build_tree(
        IndexOrder::Eavt,
        false,
        datoms,
        &TreeConfig {
            max_leaf_datoms: 1,
            ..TreeConfig::default()
        },
    )
    .unwrap();
    tree.nodes
        .iter()
        .map(|(hash, bytes)| (*hash, bytes.to_vec()))
        .collect()
}

fn measure(store: &mut PostgresTreeStore, relay: &Relay, label: &str, per_node: bool) -> Counts {
    let nodes = nodes(label);
    // The simple-query fences drain startup, preceding extended-query work,
    // and statement-close traffic. Subtract exactly the trailing Q/Z pair.
    store.client.simple_query("SELECT 1").unwrap();
    let before = relay.counters.snapshot();
    let context = OperationContext::new(OperationKind::Indexing);
    {
        let _scope = context.enter();
        if per_node {
            for (hash, payload) in &nodes {
                store.insert_node(*hash, payload).unwrap();
            }
        } else {
            store
                .insert_nodes(
                    nodes
                        .iter()
                        .map(|(hash, payload)| (*hash, payload.as_slice())),
                    NodeUploadLimits::default(),
                )
                .unwrap();
        }
    }
    store.client.simple_query("SELECT 1").unwrap();
    let counts = relay
        .counters
        .snapshot()
        .since_before_trailing_fence(before);
    assert_eq!(
        counts.query, 0,
        "upload unexpectedly used the simple-query protocol"
    );
    assert_eq!(
        counts.ready,
        counts.sync + counts.query,
        "request/ready cycles did not balance after the fence"
    );
    assert!(counts.ready > 0);
    eprintln!(
        "POSTGRES_PROTOCOL_CYCLES case={label} nodes={} driver_api_calls={} frontend_sync={} frontend_query={} backend_ready={} completed_request_ready_cycles={} (not packets or measured TCP RTT)",
        nodes.len(),
        context.snapshot().calls,
        counts.sync,
        counts.query,
        counts.ready,
        counts.ready
    );
    counts
}

#[test]
fn postgres_upload_protocol_cycles_are_observed_not_inferred() {
    if std::env::var("ATOMIC_POSTGRES_PROTOCOL_WITNESS").as_deref() != Ok("1") {
        eprintln!(
            "SKIPPED opt-in protocol witness: set ATOMIC_POSTGRES_PROTOCOL_WITNESS=1 with isolated ATOMIC_POSTGRES_URL"
        );
        return;
    }
    let connection = std::env::var("ATOMIC_POSTGRES_URL")
        .expect("protocol witness requires explicit isolated PostgreSQL fixture");
    let original: Config = connection
        .parse()
        .unwrap_or_else(|_| panic!("invalid isolated fixture configuration"));
    let [Host::Unix(directory)] = original.get_hosts() else {
        panic!("protocol witness requires exactly one explicit Unix-socket host")
    };
    assert!(
        original.get_ports().len() <= 1,
        "protocol witness requires one port"
    );
    let port = original.get_ports().first().copied().unwrap_or(5432);
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresTreeStore::connect(&connection)
        .unwrap()
        .with_compressed_node_blocks(false);
    let relay = Relay::start(directory.join(format!(".s.PGSQL.{port}")), port).unwrap();
    let mut proxy = Config::new();
    proxy
        .host_path(relay.directory.path())
        .port(port)
        .ssl_mode(SslMode::Disable)
        .user(
            original
                .get_user()
                .expect("fixture must configure user explicitly"),
        )
        .dbname(
            original
                .get_dbname()
                .expect("fixture must configure dbname explicitly"),
        );
    if let Some(password) = original.get_password() {
        proxy.password(password);
    }
    if let Some(options) = original.get_options() {
        proxy.options(options);
    }
    store.client = Client::from_raw(proxy.connect(NoTls).unwrap());
    let single = measure(&mut store, &relay, "per-node", true);
    let batch = measure(&mut store, &relay, "batch", false);
    store = store.with_compressed_node_blocks(true);
    let compressed = measure(&mut store, &relay, "batch-compressed", false);
    assert!(batch.ready < single.ready);
    assert!(compressed.ready > batch.ready);
    drop(store);
    relay.finish().unwrap();
}

#[test]
fn protocol_counter_forwards_payloads_without_interpreting_them() {
    let mut frontend_bytes = vec![0, 0, 0, 8, 0, 3, 0, 0];
    frontend_bytes.extend_from_slice(&[b'S', 0, 0, 0, 4]);
    frontend_bytes.extend_from_slice(&[b'Q', 0, 0, 0, 7, b'x', b'y', 0]);
    let counters = Counters::default();
    let mut forwarded = Vec::new();
    frontend(&mut Cursor::new(&frontend_bytes), &mut forwarded, &counters).unwrap();
    assert_eq!(forwarded, frontend_bytes);
    let backend_bytes = [b'Z', 0, 0, 0, 5, b'I', b'Z', 0, 0, 0, 5, b'I'];
    forwarded.clear();
    backend(&mut Cursor::new(backend_bytes), &mut forwarded, &counters).unwrap();
    assert_eq!(forwarded, backend_bytes);
    assert_eq!(
        counters.snapshot(),
        Counts {
            sync: 1,
            query: 1,
            ready: 2
        }
    );
}

#[test]
fn protocol_counter_rejects_unsupported_or_unbounded_framing() {
    let counters = Counters::default();
    let mut sink = io::sink();
    let negotiation = [0, 0, 0, 8, 4, 210, 22, 47];
    assert!(frontend(&mut Cursor::new(negotiation), &mut sink, &counters).is_err());
    assert!(
        backend(
            &mut Cursor::new([b'D', 255, 255, 255, 255]),
            &mut sink,
            &counters
        )
        .is_err()
    );
    assert!(backend(&mut Cursor::new([b'Z', 0, 0, 0, 4]), &mut sink, &counters).is_err());
    assert_eq!(counters.snapshot(), Counts::default());
}
