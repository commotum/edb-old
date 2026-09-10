use atomic_core::persistent_tree::{TreeConfig, build_tree};
use atomic_core::sql_io::{OperationContext, OperationKind};
use atomic_core::{
    Datom, Digest, IndexOrder, NodeUploadLimits, PostgresConnectionConfig, PostgresIoPolicy,
    PostgresMigrator, PostgresTreeStore, USER_PARTITION, Value, make_eid, t_to_tx,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

fn connection() -> Option<String> {
    let connection = std::env::var("ATOMIC_POSTGRES_URL").ok();
    if connection.is_none() {
        eprintln!("SKIPPED node-upload PostgreSQL witness: ATOMIC_POSTGRES_URL is not set");
    }
    connection
}

fn nodes(count: usize) -> Vec<(Digest, Vec<u8>)> {
    nodes_with_padding(count, 0)
}

fn nodes_with_padding(count: usize, padding: usize) -> Vec<(Digest, Vec<u8>)> {
    let salt = format!(
        "{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    let datoms = (0..count).map(|index| Datom {
        entity: make_eid(USER_PARTITION, index as u64 + 1).unwrap(),
        attribute: 1000,
        value: Value::String(format!(
            "upload-witness-{salt}-{index}{}",
            "x".repeat(padding)
        )),
        tx: t_to_tx(1).unwrap(),
        added: true,
    });
    let build = build_tree(
        IndexOrder::Eavt,
        false,
        datoms,
        &TreeConfig {
            max_leaf_datoms: 1,
            ..TreeConfig::default()
        },
    )
    .unwrap();
    build
        .nodes
        .iter()
        .map(|(hash, bytes)| (*hash, bytes.to_vec()))
        .collect()
}

fn store(connection: &str) -> PostgresTreeStore {
    PostgresMigrator::connect(connection)
        .unwrap()
        .migrate()
        .unwrap();
    PostgresTreeStore::connect(connection)
        .unwrap()
        .with_compressed_node_blocks(false)
}

#[test]
fn postgres_single_node_upload_baseline_counts_actual_driver_calls() {
    let Some(connection) = connection() else {
        return;
    };
    let mut store = store(&connection);
    let nodes = nodes(64);
    for pass in 0..2 {
        let context = OperationContext::new(OperationKind::Indexing);
        let started = Instant::now();
        {
            let _scope = context.enter();
            for (hash, bytes) in &nodes {
                store.insert_node(*hash, bytes).unwrap();
            }
        }
        let stats = context.snapshot();
        assert_eq!(stats.calls, nodes.len() as u64 * 2);
        assert_eq!(stats.sql_calls, stats.calls);
        assert_eq!(stats.errors, 0);
        assert_eq!(
            stats.result_cell_bytes,
            nodes.iter().map(|(_, bytes)| bytes.len() as u64).sum()
        );
        eprintln!(
            "PostgreSQL single-node baseline pass={pass} nodes={} driver_calls={} result_cell_bytes={} elapsed_us={}",
            nodes.len(),
            stats.calls,
            stats.result_cell_bytes,
            started.elapsed().as_micros()
        );
    }
    assert_eq!(store.stats().node_writes, nodes.len() as u64);
    assert_eq!(store.stats().node_reuses, nodes.len() as u64);
}

#[test]
fn postgres_batch_upload_counts_calls_bytes_and_verified_reuse() {
    let Some(connection) = connection() else {
        return;
    };
    let mut store = store(&connection);
    for max_nodes in [1, 16, 128] {
        let nodes = nodes(64);
        let bytes = nodes
            .iter()
            .map(|(_, bytes)| bytes.len() as u64)
            .sum::<u64>();
        let batches = nodes.len().div_ceil(max_nodes) as u64;
        store.reset_stats();
        for pass in 0..2 {
            let context = OperationContext::new(OperationKind::Indexing);
            let started = Instant::now();
            {
                let _scope = context.enter();
                store
                    .insert_nodes(
                        nodes.iter().map(|(hash, bytes)| (*hash, bytes.as_slice())),
                        NodeUploadLimits {
                            max_nodes,
                            ..NodeUploadLimits::default()
                        },
                    )
                    .unwrap();
            }
            let stats = context.snapshot();
            assert_eq!(stats.calls, batches * 2);
            assert_eq!(stats.completed_calls, stats.calls);
            assert_eq!(stats.control_calls, 0);
            assert_eq!(stats.errors, 0);
            assert_eq!(stats.known_payload_read_bytes, bytes);
            assert_eq!(stats.known_payload_write_bytes, bytes);
            let hash_cells = nodes.len() as u64 * if pass == 0 { 64 } else { 32 };
            assert_eq!(stats.result_cell_bytes, bytes + hash_cells);
            eprintln!(
                "PostgreSQL batch upload pass={pass} max_nodes={max_nodes} nodes={} batches={batches} driver_calls={} canonical_bytes={bytes} result_cell_bytes={} elapsed_us={}",
                nodes.len(),
                stats.calls,
                stats.result_cell_bytes,
                started.elapsed().as_micros()
            );
        }
        assert_eq!(store.stats().node_writes, nodes.len() as u64);
        assert_eq!(store.stats().node_reuses, nodes.len() as u64);
        assert_eq!(store.stats().node_upload_batches, 2 * batches);
        assert_eq!(
            store.stats().node_upload_peak_nodes,
            max_nodes.min(nodes.len()) as u64
        );
    }
}

#[test]
fn postgres_batch_duplicates_and_soft_byte_budget_preserve_valid_nodes() {
    let Some(connection) = connection() else {
        return;
    };
    let mut store = store(&connection);
    let nodes = nodes(4);
    let limits = NodeUploadLimits {
        max_nodes: 3,
        max_bytes: usize::MAX,
    };
    store = store.with_node_upload_limits(limits).unwrap();
    store.reconnect().unwrap();
    assert_eq!(
        store.node_upload_limits(),
        limits,
        "reconnect discarded configured upload policy"
    );
    store
        .insert_nodes(
            nodes
                .iter()
                .flat_map(|(hash, bytes)| [(*hash, bytes.as_slice()); 2]),
            store.node_upload_limits(),
        )
        .unwrap();
    assert_eq!(store.stats().node_writes, nodes.len() as u64);
    assert_eq!(store.stats().node_reuses, nodes.len() as u64);
    assert_eq!(store.stats().node_insert_attempts, nodes.len() as u64 * 2);
    store.reset_stats();
    store
        .insert_nodes(
            nodes.iter().map(|(hash, bytes)| (*hash, bytes.as_slice())),
            NodeUploadLimits {
                max_nodes: 128,
                max_bytes: 1,
            },
        )
        .unwrap();
    assert_eq!(store.stats().node_upload_batches, nodes.len() as u64);
    assert_eq!(store.stats().node_upload_peak_nodes, 1);
    assert_eq!(
        store.stats().node_upload_peak_bytes,
        nodes
            .iter()
            .map(|(_, bytes)| bytes.len() as u64)
            .max()
            .unwrap()
    );
    assert_eq!(store.stats().node_reuses, nodes.len() as u64);
    for (hash, bytes) in nodes {
        assert_eq!(store.load_node(hash).unwrap().unwrap(), bytes);
    }
    eprintln!(
        "PostgreSQL duplicate/reuse and oversized-single-node soft-budget witnesses executed"
    );
}

#[test]
fn postgres_batch_rejects_bad_input_before_sql_and_does_not_overpull_stream() {
    let Some(connection) = connection() else {
        return;
    };
    let mut store = store(&connection);
    let nodes = nodes(2);
    let context = OperationContext::new(OperationKind::Indexing);
    {
        let _scope = context.enter();
        for limits in [
            NodeUploadLimits {
                max_nodes: 0,
                max_bytes: 1,
            },
            NodeUploadLimits {
                max_nodes: 1,
                max_bytes: 0,
            },
        ] {
            let never_pull = std::iter::from_fn(|| -> Option<(Digest, &[u8])> {
                panic!("invalid limits pulled the input stream")
            });
            assert_eq!(
                store.insert_nodes(never_pull, limits).unwrap_err().code,
                "tree/invalid-upload-limits"
            );
        }
        assert_eq!(
            store
                .insert_nodes(
                    [(nodes[0].0, &b"not-the-canonical-bytes"[..])],
                    NodeUploadLimits::default()
                )
                .unwrap_err()
                .code,
            "tree/node-hash-mismatch"
        );
        store
            .insert_nodes(std::iter::empty(), NodeUploadLimits::default())
            .unwrap();
    }
    assert_eq!(context.snapshot().calls, 0);

    // With a one-node budget, completing the preceding upload must happen
    // before the iterator is asked to produce the next node.
    let observer = OperationContext::new(OperationKind::Indexing);
    {
        let _scope = observer.enter();
        let stream = nodes.iter().enumerate().map(|(index, (hash, bytes))| {
            assert_eq!(observer.snapshot().calls, index as u64 * 2);
            (*hash, bytes.as_slice())
        });
        store
            .insert_nodes(
                stream,
                NodeUploadLimits {
                    max_nodes: 1,
                    max_bytes: usize::MAX,
                },
            )
            .unwrap();
    }
    eprintln!("PostgreSQL invalid-input/no-SQL and bounded-stream witnesses executed");
}

#[test]
fn postgres_batch_corrupt_existing_row_fails_closed_and_leaves_only_safe_orphans() {
    let Some(connection) = connection() else {
        return;
    };
    let mut store = store(&connection);
    let nodes = nodes(3);
    let bad = &nodes[0];
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    raw.execute(
        "INSERT INTO atomic_tree_nodes(node_hash, payload) VALUES($1, $2)",
        &[&&bad.0[..], &&b"corrupt-fixture"[..]],
    )
    .unwrap();
    let context = OperationContext::new(OperationKind::Indexing);
    let error = {
        let _scope = context.enter();
        store
            .insert_nodes(
                nodes.iter().map(|(hash, bytes)| (*hash, bytes.as_slice())),
                NodeUploadLimits::default(),
            )
            .unwrap_err()
    };
    assert_eq!(error.code, "tree/node-hash-conflict");
    assert_eq!(context.snapshot().calls, 2);
    assert_eq!(
        store.stats().node_writes,
        0,
        "failed verification claimed verified writes"
    );
    assert_eq!(
        store.load_node(bad.0).unwrap_err().code,
        "tree/node-content-corrupt"
    );
    for (hash, bytes) in &nodes[1..] {
        assert_eq!(
            store.load_node(*hash).unwrap().as_deref(),
            Some(bytes.as_slice())
        );
    }
    let hashes: Vec<&[u8]> = nodes.iter().map(|(hash, _)| hash.as_slice()).collect();
    let published: i64 = raw.query_one(
        "SELECT count(*) FROM atomic_tree_publications p JOIN atomic_tree_manifest_roots r USING(manifest_hash) WHERE r.root_hash=ANY($1::bytea[])",
        &[&hashes],
    ).unwrap().get(0);
    assert_eq!(published, 0);
    eprintln!(
        "PostgreSQL corrupt-existing-node verification failure retained safe orphan bytes without publication"
    );
}

#[test]
fn postgres_interrupted_batch_insert_is_atomic_and_retry_is_verified() {
    let Some(connection) = connection() else {
        return;
    };
    let _migrated = store(&connection);
    let configured = PostgresConnectionConfig::plaintext(&connection)
        .with_io_policy(PostgresIoPolicy {
            lock_timeout: Some(Duration::from_millis(100)),
            statement_timeout: Some(Duration::from_secs(5)),
            ..PostgresIoPolicy::default()
        })
        .unwrap();
    let mut store = PostgresTreeStore::connect_configured(&configured)
        .unwrap()
        .with_compressed_node_blocks(false);
    let nodes = nodes(3);
    let blocked = nodes.last().unwrap();
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let mut blocker = raw.transaction().unwrap();
    blocker
        .execute(
            "INSERT INTO atomic_tree_nodes(node_hash, payload) VALUES($1, $2)",
            &[&&blocked.0[..], &blocked.1],
        )
        .unwrap();
    let context = OperationContext::new(OperationKind::Indexing);
    let error = {
        let _scope = context.enter();
        store
            .insert_nodes(
                nodes.iter().map(|(hash, bytes)| (*hash, bytes.as_slice())),
                NodeUploadLimits::default(),
            )
            .unwrap_err()
    };
    assert_eq!(error.code, "tree/node-batch-insert");
    assert_eq!(context.snapshot().calls, 1);
    assert_eq!(context.snapshot().errors, 1);
    assert_eq!(store.stats().node_writes, 0);
    blocker.rollback().unwrap();
    let hashes: Vec<&[u8]> = nodes.iter().map(|(hash, _)| hash.as_slice()).collect();
    let remaining: i64 = raw
        .query_one(
            "SELECT count(*) FROM atomic_tree_nodes WHERE node_hash=ANY($1::bytea[])",
            &[&hashes],
        )
        .unwrap()
        .get(0);
    assert_eq!(remaining, 0, "interrupted INSERT left a partial batch");
    store
        .insert_nodes(
            nodes.iter().map(|(hash, bytes)| (*hash, bytes.as_slice())),
            NodeUploadLimits::default(),
        )
        .unwrap();
    assert_eq!(store.stats().node_writes, nodes.len() as u64);
    for (hash, bytes) in nodes {
        assert_eq!(store.load_node(hash).unwrap().unwrap(), bytes);
    }
    eprintln!(
        "PostgreSQL real lock-timeout interrupted batch, rolled back the INSERT, and verified the complete retry"
    );
}

#[test]
fn postgres_optional_compression_has_separate_calls_bytes_and_cpu_cost() {
    let Some(connection) = connection() else {
        return;
    };
    let mut store = store(&connection)
        .with_compressed_node_blocks(true)
        .with_node_block_encoding_overlap(None);
    let nodes = nodes_with_padding(16, 4096);
    let canonical_bytes = nodes
        .iter()
        .map(|(_, bytes)| bytes.len() as u64)
        .sum::<u64>();
    let context = OperationContext::new(OperationKind::Indexing);
    let started = Instant::now();
    {
        let _scope = context.enter();
        store
            .insert_nodes(
                nodes.iter().map(|(hash, bytes)| (*hash, bytes.as_slice())),
                NodeUploadLimits::default(),
            )
            .unwrap();
    }
    let stats = store.stats();
    let sql = context.snapshot();
    assert_eq!(stats.node_writes, nodes.len() as u64);
    assert_eq!(stats.node_block_writes.considered, nodes.len() as u64);
    assert!(stats.node_block_writes.inserted > 0);
    assert_eq!(stats.node_block_write_failures, 0);
    assert_eq!(
        sql.calls, 3,
        "canonical INSERT+verify and one optional compressed INSERT"
    );
    assert_eq!(sql.by_operation[&OperationKind::NodeCompression].calls, 1);
    assert_eq!(sql.phases[&OperationKind::NodeCompression].invocations, 1);
    assert_eq!(
        sql.known_payload_write_bytes,
        canonical_bytes + stats.node_block_writes.physical_write_bytes
    );
    assert!(stats.node_block_writes.inserted_physical_bytes < canonical_bytes);
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    for (hash, canonical) in &nodes {
        let stored: Vec<u8> = raw
            .query_one(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash=$1",
                &[&&hash[..]],
            )
            .unwrap()
            .get(0);
        assert_eq!(
            &stored, canonical,
            "compression rewrote canonical PostgreSQL bytes"
        );
    }
    let reads = OperationContext::new(OperationKind::Query);
    {
        let _scope = reads.enter();
        for (hash, canonical) in &nodes {
            assert_eq!(
                store.load_node(*hash).unwrap().as_deref(),
                Some(canonical.as_slice())
            );
        }
    }
    assert_eq!(reads.snapshot().calls, nodes.len() as u64);
    let transfer = store.stats().node_block_reads;
    assert_eq!(transfer.compressed_hits, stats.node_block_writes.inserted);
    assert_eq!(transfer.canonical_bytes, canonical_bytes);
    assert!(transfer.physical_read_bytes < canonical_bytes);
    assert_eq!(
        reads.snapshot().known_payload_read_bytes,
        transfer.physical_read_bytes
    );
    eprintln!(
        "PostgreSQL compressed node reads driver_calls={} canonical_bytes={canonical_bytes} physical_payload_bytes={} decode_nanos={}",
        reads.snapshot().calls,
        transfer.physical_read_bytes,
        transfer.decode_elapsed_nanos
    );
    eprintln!(
        "PostgreSQL optional compression nodes={} driver_calls={} canonical_bytes={canonical_bytes} extra_retained_compressed_bytes={} physical_payload_submitted={} encode_nanos={} phase_nanos={} elapsed_us={}",
        nodes.len(),
        sql.calls,
        stats.node_block_writes.inserted_physical_bytes,
        sql.known_payload_write_bytes,
        stats.node_block_writes.encode_elapsed_nanos,
        sql.phases[&OperationKind::NodeCompression].elapsed_nanos,
        started.elapsed().as_micros()
    );
}

#[test]
fn postgres_optional_projection_failure_does_not_fail_verified_canonical_upload() {
    let Some(connection) = connection() else {
        return;
    };
    let nodes = nodes_with_padding(1, 4096);
    let (hash, payload) = nodes.iter().max_by_key(|(_, bytes)| bytes.len()).unwrap();
    let mut canonical_store = store(&connection);
    canonical_store.insert_node(*hash, payload).unwrap();
    let configured = PostgresConnectionConfig::plaintext(&connection)
        .with_io_policy(PostgresIoPolicy {
            lock_timeout: Some(Duration::from_millis(100)),
            statement_timeout: Some(Duration::from_secs(5)),
            ..PostgresIoPolicy::default()
        })
        .unwrap();
    let mut store = PostgresTreeStore::connect_configured(&configured).unwrap();
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let mut blocker = raw.transaction().unwrap();
    // An uncommitted row holds this one optional key; it is rolled back below
    // and never becomes a visible malformed projection.
    blocker.execute(
        "INSERT INTO atomic_tree_node_blocks(node_hash,canonical_bytes,physical_hash,physical_payload) VALUES($1,$2,$3,$4)",
        &[&&hash[..], &(payload.len() as i64), &&atomic_core::sha256(b"x")[..], &&b"x"[..]],
    ).unwrap();
    let context = OperationContext::new(OperationKind::Indexing);
    {
        let _scope = context.enter();
        store
            .insert_nodes([(*hash, payload.as_slice())], NodeUploadLimits::default())
            .unwrap();
    }
    assert_eq!(store.stats().node_reuses, 1);
    assert_eq!(store.stats().node_block_write_failures, 1);
    assert_eq!(context.snapshot().errors, 1);
    assert_eq!(
        context.snapshot().by_operation[&OperationKind::NodeCompression].errors,
        1
    );
    blocker.rollback().unwrap();
    assert_eq!(
        store.load_node(*hash).unwrap().as_deref(),
        Some(payload.as_slice())
    );
    // The compatibility one-node API participates in the same optional path.
    store.insert_node(*hash, payload).unwrap();
    assert_eq!(store.stats().node_block_writes.inserted, 1);
    assert_eq!(store.stats().node_block_write_failures, 1);
    eprintln!(
        "PostgreSQL optional projection lock timeout was counted; verified canonical upload succeeded and single-node retry created the projection"
    );
}

#[test]
fn postgres_encoding_overlap_is_bounded_attributed_and_joined_before_return() {
    let Some(connection) = connection() else {
        return;
    };
    let mut store = store(&connection).with_compressed_node_blocks(true);
    let tiny = nodes(2);
    store
        .insert_nodes(
            tiny.iter().map(|(hash, bytes)| (*hash, bytes.as_slice())),
            NodeUploadLimits::default(),
        )
        .unwrap();
    assert_eq!(
        store.stats().node_block_encoding_workers,
        0,
        "tiny upload spawned an encoding worker"
    );
    store.reset_stats();
    let nodes = nodes_with_padding(16, 8192);
    let context = OperationContext::new(OperationKind::Indexing);
    {
        let _scope = context.enter();
        store
            .insert_nodes(
                nodes.iter().map(|(hash, bytes)| (*hash, bytes.as_slice())),
                NodeUploadLimits::default(),
            )
            .unwrap();
    }
    let stats = store.stats();
    let sql = context.snapshot();
    assert_eq!(stats.node_block_encoding_workers, 1);
    assert_eq!(stats.node_block_encoding_peak_workers, 1);
    assert!(stats.node_block_encoding_overlap_nanos > 0);
    assert!(stats.node_block_encoding_overlap_nanos <= stats.node_block_preparation_nanos);
    assert!(stats.node_block_encoding_overlap_nanos <= stats.node_block_overlapped_upload_nanos);
    assert_eq!(sql.calls, 3);
    assert_eq!(sql.by_operation[&OperationKind::NodeCompression].calls, 1);
    assert_eq!(
        sql.phases[&OperationKind::NodeCompression].invocations,
        2,
        "worker and upload phases did not both finish"
    );
    assert!(!sql.by_operation.contains_key(&OperationKind::Unscoped));
    eprintln!(
        "PostgreSQL measured encoding/upload overlap_nanos={} preparation_nanos={} canonical_upload_nanos={} peak_workers={}",
        stats.node_block_encoding_overlap_nanos,
        stats.node_block_preparation_nanos,
        stats.node_block_overlapped_upload_nanos,
        stats.node_block_encoding_peak_workers
    );

    // A canonical verification failure still joins its preparation worker,
    // and must never insert projections for the rejected candidate batch.
    store.reset_stats();
    let failed_nodes = nodes_with_padding(16, 8192);
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    raw.execute(
        "INSERT INTO atomic_tree_nodes(node_hash,payload) VALUES($1,$2)",
        &[&&failed_nodes[0].0[..], &&b"overlap-corruption-fixture"[..]],
    )
    .unwrap();
    let failed = OperationContext::new(OperationKind::Indexing);
    {
        let _scope = failed.enter();
        assert_eq!(
            store
                .insert_nodes(
                    failed_nodes
                        .iter()
                        .map(|(hash, bytes)| (*hash, bytes.as_slice())),
                    NodeUploadLimits::default()
                )
                .unwrap_err()
                .code,
            "tree/node-hash-conflict"
        );
    }
    assert_eq!(store.stats().node_block_encoding_workers, 1);
    assert_eq!(store.stats().node_block_writes.inserted, 0);
    assert_eq!(
        failed.snapshot().phases[&OperationKind::NodeCompression].invocations,
        1
    );
    assert_eq!(failed.snapshot().calls, 2);
    let hashes: Vec<&[u8]> = failed_nodes
        .iter()
        .map(|(hash, _)| hash.as_slice())
        .collect();
    let projections: i64 = raw
        .query_one(
            "SELECT count(*) FROM atomic_tree_node_blocks WHERE node_hash=ANY($1::bytea[])",
            &[&hashes],
        )
        .unwrap()
        .get(0);
    assert_eq!(projections, 0);
}

fn process_peak_rss_kib() -> Option<u64> {
    std::fs::read_to_string("/proc/self/status")
        .ok()?
        .lines()
        .find_map(|line| {
            line.strip_prefix("VmHWM:")?
                .split_whitespace()
                .next()?
                .parse()
                .ok()
        })
}

#[cfg(target_os = "linux")]
fn process_resources() -> Option<(u64, u64, u64)> {
    let mut usage = std::mem::MaybeUninit::<libc::rusage>::uninit();
    // RUSAGE_SELF includes all process threads, including a joined codec worker.
    // The OS initializes the entire result on a successful call.
    if unsafe { libc::getrusage(libc::RUSAGE_SELF, usage.as_mut_ptr()) } != 0 {
        return None;
    }
    let usage = unsafe { usage.assume_init() };
    let micros = |time: libc::timeval| {
        (time.tv_sec as u64)
            .saturating_mul(1_000_000)
            .saturating_add(time.tv_usec as u64)
    };
    Some((
        micros(usage.ru_utime),
        micros(usage.ru_stime),
        usage.ru_maxrss as u64,
    ))
}

#[cfg(not(target_os = "linux"))]
fn process_resources() -> Option<(u64, u64, u64)> {
    None
}

/// Run this in a fresh test process for each mode so VmHWM isn't inherited
/// from another case. CPU deltas use process-wide getrusage around the upload;
/// `/usr/bin/time` can additionally show whole-process setup/fixture costs.
#[test]
#[ignore = "isolated process memory/CPU benchmark; select explicitly with live PostgreSQL"]
fn postgres_upload_measurement_child() {
    let Some(connection) = connection() else {
        return;
    };
    let mode = std::env::var("ATOMIC_UPLOAD_BENCH_MODE").unwrap_or_else(|_| "overlap".into());
    let size: usize = std::env::var("ATOMIC_UPLOAD_BENCH_SIZE")
        .ok()
        .map(|text| text.parse().unwrap())
        .unwrap_or(32_768);
    let entropy = std::env::var("ATOMIC_UPLOAD_BENCH_ENTROPY").unwrap_or_else(|_| "repeat".into());
    assert!(
        size <= 1_048_576,
        "benchmark fixture size, not a database value limit"
    );
    assert!(matches!(entropy.as_str(), "repeat" | "random"));
    let mut store = match mode.as_str() {
        "canonical" => store(&connection),
        "serial" => store(&connection)
            .with_compressed_node_blocks(true)
            .with_node_block_encoding_overlap(None),
        "overlap" => store(&connection).with_compressed_node_blocks(true),
        "forced-overlap" => store(&connection)
            .with_compressed_node_blocks(true)
            .with_node_block_encoding_overlap(Some(0)),
        _ => panic!("unrecognized benchmark mode"),
    };
    let salt = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos() as u64;
    let datoms = (0..32).map(|index| {
        let mut state = salt.wrapping_add(index + 1);
        let value = (0..size)
            .map(|_| {
                state ^= state << 13;
                state ^= state >> 7;
                state ^= state << 17;
                if entropy == "repeat" {
                    b'x'
                } else {
                    state as u8
                }
            })
            .collect::<Vec<_>>();
        Datom {
            entity: make_eid(USER_PARTITION, salt % 1_000_000_000 + index + 1).unwrap(),
            attribute: 1000,
            value: Value::Bytes(value),
            tx: t_to_tx(1).unwrap(),
            added: true,
        }
    });
    let build = build_tree(
        IndexOrder::Eavt,
        false,
        datoms,
        &TreeConfig {
            max_leaf_datoms: 1,
            ..TreeConfig::default()
        },
    )
    .unwrap();
    let canonical_bytes: u64 = build
        .nodes
        .iter()
        .map(|(_, bytes)| bytes.len() as u64)
        .sum();
    let before = process_peak_rss_kib();
    let resources_before = process_resources();
    let context = OperationContext::new(OperationKind::Indexing);
    let started = Instant::now();
    {
        let _scope = context.enter();
        store
            .insert_nodes(
                build.nodes.iter().map(|(hash, bytes)| (*hash, bytes)),
                NodeUploadLimits::default(),
            )
            .unwrap();
    }
    let elapsed = started.elapsed().as_micros();
    let resources_after = process_resources();
    let after = process_peak_rss_kib();
    let stats = store.stats();
    let resources = resources_before
        .zip(resources_after)
        .map(|(before, after)| {
            (
                after.0.saturating_sub(before.0),
                after.1.saturating_sub(before.1),
                after.2,
            )
        });
    assert_eq!(stats.node_block_write_failures, 0);
    assert_eq!(
        stats.node_writes,
        build.nodes.iter().count() as u64,
        "benchmark unexpectedly reused previously stored nodes"
    );
    eprintln!(
        "UPLOAD_MEASUREMENT mode={mode} entropy={entropy} bytes_per_value={size} nodes={} canonical_bytes={canonical_bytes} upload_us={elapsed} upload_cpu_user_us_system_us_process_maxrss_kib={resources:?} driver_calls={} extra_compressed_bytes={} compressed_nodes={} skipped_not_smaller={} encode_nanos={} preparation_nanos={} canonical_upload_nanos={} overlap_nanos={} workers={} batch_input_peak_bytes={} process_hwm_before_kib={before:?} process_hwm_after_kib={after:?}",
        build.nodes.iter().count(),
        context.snapshot().calls,
        stats.node_block_writes.inserted_physical_bytes,
        stats.node_block_writes.inserted,
        stats.node_block_writes.skipped_not_smaller,
        stats.node_block_writes.encode_elapsed_nanos,
        stats.node_block_preparation_nanos,
        stats.node_block_overlapped_upload_nanos,
        stats.node_block_encoding_overlap_nanos,
        stats.node_block_encoding_workers,
        stats.node_upload_peak_bytes
    );
}
