//! Optional compressed transfer representations over authoritative raw nodes.
//!
//! No publication, archive, backup, or GC proof depends on this table. Server
//! hashing deliberately touches canonical content even on a compressed hit;
//! transfer savings do not imply PostgreSQL disk or server-CPU savings.
use crate::block_codec::{MAX_CANONICAL_BLOCK_BYTES, decode_block, encode_block};
use crate::postgres::postgres_error;
use crate::sql_io::GenericClient;
use crate::{Digest, ErrorCategory, SemanticError, sha256};
use std::time::Instant;

pub(crate) const MAX_BATCH_NODES: usize = 256;
pub(crate) const MAX_BATCH_BYTES: usize = 64 * 1024 * 1024;

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct NodeBlockWriteStats {
    pub considered: u64,
    pub skipped_not_smaller: u64,
    pub inserted: u64,
    pub reused: u64,
    /// Extra retained payload beyond the unchanged canonical rows.
    pub inserted_physical_bytes: u64,
    /// Payload submitted, including idempotent attempts; excludes SQL framing.
    pub physical_write_bytes: u64,
    /// Compression plus local decode/authentication wall time, not CPU time.
    pub encode_elapsed_nanos: u64,
}

#[cfg(test)]
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
enum NodeBlockSource {
    Canonical,
    Compressed,
    CorruptProjectionFallback,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct NodeBlockReadStats {
    pub compressed_hits: u64,
    pub canonical_reads: u64,
    pub corrupt_projections: u64,
    pub canonical_bytes: u64,
    /// All returned payloads, including a rejected projection before fallback.
    /// Metadata cells and wire framing are excluded; SQL counters include cells.
    pub physical_read_bytes: u64,
    pub decode_elapsed_nanos: u64,
}

pub(crate) struct LoadedNodeBlock {
    pub(crate) canonical: Vec<u8>,
    #[cfg(test)]
    source: NodeBlockSource,
    pub(crate) stats: NodeBlockReadStats,
}

/// Owned, bounded physical bytes with no connection, cursor or database state.
/// Preparation may overlap canonical I/O; insertion still follows verification.
pub(crate) struct PreparedNodeBlocks {
    hashes: Vec<Vec<u8>>,
    lengths: Vec<i64>,
    fingerprints: Vec<Vec<u8>>,
    payloads: Vec<Vec<u8>>,
    stats: NodeBlockWriteStats,
}

/// Call only after canonical insertion/verification has committed. This
/// optional, bounded statement never changes canonical bytes or their hashes.
/// A caller may record a projection failure and continue canonical publication.
pub(crate) fn store_node_blocks<C: GenericClient>(
    client: &mut C,
    nodes: &[(Digest, &[u8])],
) -> Result<NodeBlockWriteStats, SemanticError> {
    let prepared = prepare_node_blocks(nodes)?;
    store_prepared_node_blocks(client, prepared)
}

/// Pure codec work. Returns owned bytes and performs no SQL or callbacks.
/// The caller can time its scoped worker interval independently of SQL.
pub(crate) fn prepare_node_blocks(
    nodes: &[(Digest, &[u8])],
) -> Result<PreparedNodeBlocks, SemanticError> {
    let total = nodes
        .iter()
        .try_fold(0_usize, |total, (_, bytes)| total.checked_add(bytes.len()));
    if nodes.len() > MAX_BATCH_NODES || total.is_none_or(|total| total > MAX_BATCH_BYTES) {
        return Err(SemanticError::new(
            ErrorCategory::Busy,
            "tree/block-batch-limit",
            "compressed node batch exceeds its bounded input limits",
        ));
    }
    let mut stats = NodeBlockWriteStats::default();
    let mut hashes = Vec::new();
    let mut lengths = Vec::new();
    let mut fingerprints = Vec::new();
    let mut payloads = Vec::new();
    for (hash, canonical) in nodes {
        validate_canonical(hash, canonical)?;
        stats.considered += 1;
        if canonical.len() > MAX_CANONICAL_BLOCK_BYTES {
            stats.skipped_not_smaller += 1;
            continue;
        }
        let started = Instant::now();
        let physical = encode_block(canonical)?;
        if physical.len() >= canonical.len() {
            stats.encode_elapsed_nanos = stats.encode_elapsed_nanos.saturating_add(nanos(started));
            stats.skipped_not_smaller += 1;
            continue;
        }
        // Authenticate the exact physical representation before any write.
        if decode_block(hash, &physical)?.as_slice() != *canonical {
            return Err(fault(
                "tree/block-encoding",
                "compressed representation changed canonical content",
            ));
        }
        stats.encode_elapsed_nanos = stats.encode_elapsed_nanos.saturating_add(nanos(started));
        stats.physical_write_bytes = stats
            .physical_write_bytes
            .saturating_add(physical.len() as u64);
        hashes.push(hash.to_vec());
        lengths.push(canonical.len() as i64);
        fingerprints.push(sha256(&physical).to_vec());
        payloads.push(physical);
    }
    Ok(PreparedNodeBlocks {
        hashes,
        lengths,
        fingerprints,
        payloads,
        stats,
    })
}

/// Insert a prepared optional representation only after canonical verification.
pub(crate) fn store_prepared_node_blocks<C: GenericClient>(
    client: &mut C,
    prepared: PreparedNodeBlocks,
) -> Result<NodeBlockWriteStats, SemanticError> {
    let PreparedNodeBlocks {
        hashes,
        lengths,
        fingerprints,
        payloads,
        mut stats,
    } = prepared;
    if hashes.is_empty() {
        return Ok(stats);
    }
    crate::OperationContext::current_or_process().record_payload_write(stats.physical_write_bytes);
    let rows = client.query(
        "INSERT INTO atomic_tree_node_blocks (node_hash,canonical_bytes,physical_hash,physical_payload) \
         SELECT * FROM unnest($1::bytea[],$2::bigint[],$3::bytea[],$4::bytea[]) \
         ON CONFLICT (node_hash) DO NOTHING RETURNING octet_length(physical_payload)::bigint",
        &[&hashes, &lengths, &fingerprints, &payloads],
    ).map_err(|error| postgres_error("tree/block-insert", error))?;
    stats.inserted = rows.len() as u64;
    stats.reused = hashes.len() as u64 - stats.inserted;
    for row in rows {
        stats.inserted_physical_bytes = stats
            .inserted_physical_bytes
            .saturating_add(row.get::<_, i64>(0) as u64);
    }
    Ok(stats)
}

/// Load one optional representation without transferring raw payload on a
/// valid compressed hit. The server hashes authoritative raw bytes; Rust then
/// authenticates both the physical fingerprint and decoded canonical content.
/// Invalid optional data falls back, but canonical corruption always fails.
pub(crate) fn load_node_block<C: GenericClient>(
    client: &mut C,
    expected_hash: Digest,
) -> Result<Option<LoadedNodeBlock>, SemanticError> {
    let row = client
        .query_opt(
            "SELECT octet_length(n.payload)::bigint, \
                CASE WHEN b.node_hash IS NOT NULL THEN pg_catalog.sha256(n.payload) END, \
                b.canonical_bytes,b.physical_hash,b.physical_payload, \
                CASE WHEN b.node_hash IS NULL THEN n.payload END \
         FROM atomic_tree_nodes n LEFT JOIN atomic_tree_node_blocks b USING(node_hash) \
         WHERE n.node_hash=$1",
            &[&&expected_hash[..]],
        )
        .map_err(|error| postgres_error("tree/block-read", error))?;
    let Some(row) = row else { return Ok(None) };
    let canonical_length: i64 = row.get(0);
    let canonical_hash: Option<Vec<u8>> = row.get(1);
    let physical: Option<Vec<u8>> = row.get(4);
    let mut stats = NodeBlockReadStats::default();
    if let Some(physical) = physical {
        crate::OperationContext::current_or_process().record_payload_read(physical.len() as u64);
        stats.physical_read_bytes = physical.len() as u64;
        let started = Instant::now();
        let projection_length: Option<i64> = row.get(2);
        let fingerprint: Option<Vec<u8>> = row.get(3);
        let decoded = if canonical_hash.as_deref() == Some(expected_hash.as_slice())
            && canonical_length > 0
            && projection_length == Some(canonical_length)
            && fingerprint.as_deref() == Some(sha256(&physical).as_slice())
            && (physical.len() as i64) < canonical_length
        {
            decode_block(&expected_hash, &physical)
                .ok()
                .filter(|canonical| canonical.len() as i64 == canonical_length)
        } else {
            None
        };
        stats.decode_elapsed_nanos = nanos(started);
        if let Some(canonical) = decoded {
            stats.compressed_hits = 1;
            stats.canonical_bytes = canonical.len() as u64;
            return Ok(Some(LoadedNodeBlock {
                canonical,
                #[cfg(test)]
                source: NodeBlockSource::Compressed,
                stats,
            }));
        }
        stats.corrupt_projections = 1;
    } else if let Some(canonical) = row.get::<_, Option<Vec<u8>>>(5) {
        crate::OperationContext::current_or_process().record_payload_read(canonical.len() as u64);
        validate_canonical(&expected_hash, &canonical)?;
        stats.canonical_reads = 1;
        stats.canonical_bytes = canonical.len() as u64;
        stats.physical_read_bytes = canonical.len() as u64;
        return Ok(Some(LoadedNodeBlock {
            canonical,
            #[cfg(test)]
            source: NodeBlockSource::Canonical,
            stats,
        }));
    }
    let row = client
        .query_opt(
            "SELECT payload FROM atomic_tree_nodes WHERE node_hash=$1",
            &[&&expected_hash[..]],
        )
        .map_err(|error| postgres_error("tree/block-canonical-fallback", error))?;
    let Some(row) = row else { return Ok(None) };
    let canonical: Vec<u8> = row.get(0);
    crate::OperationContext::current_or_process().record_payload_read(canonical.len() as u64);
    validate_canonical(&expected_hash, &canonical)?;
    stats.canonical_reads = 1;
    stats.canonical_bytes = canonical.len() as u64;
    stats.physical_read_bytes = stats
        .physical_read_bytes
        .saturating_add(canonical.len() as u64);
    #[cfg(test)]
    let source = if stats.corrupt_projections == 0 {
        NodeBlockSource::Canonical
    } else {
        NodeBlockSource::CorruptProjectionFallback
    };
    Ok(Some(LoadedNodeBlock {
        canonical,
        #[cfg(test)]
        source,
        stats,
    }))
}

fn validate_canonical(hash: &Digest, bytes: &[u8]) -> Result<(), SemanticError> {
    if bytes.is_empty() || &sha256(bytes) != hash {
        return Err(fault(
            "tree/node-content-corrupt",
            "canonical tree node content does not match its hash",
        ));
    }
    Ok(())
}

fn nanos(started: Instant) -> u64 {
    started.elapsed().as_nanos().min(u64::MAX as u128) as u64
}
fn fault(code: &'static str, message: &'static str) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::PostgresMigrator;
    use postgres::{Client, NoTls};
    use std::time::{SystemTime, UNIX_EPOCH};

    struct Fixture {
        admin: Client,
        schema: String,
        client: Client,
    }

    impl Fixture {
        fn new(connection: &str) -> Self {
            let schema = format!(
                "blocks_{}_{}",
                std::process::id(),
                SystemTime::now()
                    .duration_since(UNIX_EPOCH)
                    .unwrap()
                    .as_nanos()
            );
            let mut admin = Client::connect(connection, NoTls).unwrap();
            admin
                .batch_execute(&format!("CREATE SCHEMA {schema}"))
                .unwrap();
            let connection = if connection.starts_with("postgres://")
                || connection.starts_with("postgresql://")
            {
                format!(
                    "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
                    if connection.contains('?') { "&" } else { "?" }
                )
            } else {
                format!("{connection} options='-csearch_path={schema},pg_catalog'")
            };
            let fixture = Self {
                admin,
                schema,
                client: Client::connect(&connection, NoTls).unwrap(),
            };
            PostgresMigrator::connect(&connection)
                .unwrap()
                .migrate()
                .unwrap();
            fixture
        }
    }

    impl Drop for Fixture {
        fn drop(&mut self) {
            let _ = self
                .admin
                .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
        }
    }

    #[test]
    fn optional_projection_authenticates_both_representations_and_falls_back() {
        let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
            eprintln!(
                "SKIPPED compressed projection PostgreSQL check: ATOMIC_POSTGRES_URL is unset"
            );
            return;
        };
        let mut fixture = Fixture::new(&connection);
        let client = &mut fixture.client;
        let canonical = vec![b'x'; 128 * 1024];
        let hash = sha256(&canonical);
        client
            .execute(
                "INSERT INTO atomic_tree_nodes(node_hash,payload) VALUES($1,$2)",
                &[&&hash[..], &canonical],
            )
            .unwrap();
        let raw = load_node_block(client, hash).unwrap().unwrap();
        assert_eq!(raw.source, NodeBlockSource::Canonical);
        assert_eq!(raw.canonical, canonical);
        let write = store_node_blocks(client, &[(hash, &canonical)]).unwrap();
        assert_eq!(write.inserted, 1);
        assert!(write.inserted_physical_bytes < canonical.len() as u64);
        assert_eq!(
            store_node_blocks(client, &[(hash, &canonical)])
                .unwrap()
                .reused,
            1
        );
        let loaded = load_node_block(client, hash).unwrap().unwrap();
        assert_eq!(loaded.source, NodeBlockSource::Compressed);
        assert_eq!(loaded.canonical, canonical);
        assert!(loaded.stats.physical_read_bytes < loaded.stats.canonical_bytes);
        assert!(store_node_blocks(client, &[([0; 32], &canonical)]).is_err());

        // Valid SQL fingerprints do not authenticate decompressed content.
        client
            .execute(
                "DELETE FROM atomic_tree_node_blocks WHERE node_hash=$1",
                &[&&hash[..]],
            )
            .unwrap();
        let wrong = encode_block(&vec![b'y'; canonical.len()]).unwrap();
        let wrong_fingerprint = sha256(&wrong);
        client.execute("INSERT INTO atomic_tree_node_blocks(node_hash,canonical_bytes,physical_hash,physical_payload) VALUES($1,$2,$3,$4)", &[&&hash[..], &(canonical.len() as i64), &&wrong_fingerprint[..], &wrong]).unwrap();
        let fallback = load_node_block(client, hash).unwrap().unwrap();
        assert_eq!(fallback.source, NodeBlockSource::CorruptProjectionFallback);
        assert_eq!(fallback.canonical, canonical);
        assert_eq!(fallback.stats.corrupt_projections, 1);
        assert_eq!(
            fallback.stats.physical_read_bytes,
            canonical.len() as u64 + wrong.len() as u64
        );
        assert!(client.execute("UPDATE atomic_tree_node_blocks SET physical_payload=physical_payload WHERE node_hash=$1", &[&&hash[..]]).is_err());
        client
            .execute(
                "DELETE FROM atomic_tree_node_blocks WHERE node_hash=$1",
                &[&&hash[..]],
            )
            .unwrap();
        assert!(client.execute("INSERT INTO atomic_tree_node_blocks(node_hash,canonical_bytes,physical_hash,physical_payload) VALUES($1,$2,$3,$4)", &[&&hash[..], &((canonical.len() + 1) as i64), &&wrong_fingerprint[..], &wrong]).is_err());
        assert!(client.execute("INSERT INTO atomic_tree_node_blocks(node_hash,canonical_bytes,physical_hash,physical_payload) VALUES($1,$2,$3,$4)", &[&&hash[..], &(canonical.len() as i64), &&[0u8; 32][..], &wrong]).is_err());
        store_node_blocks(client, &[(hash, &canonical)]).unwrap();

        // A valid old projection must not mask a corrupted authoritative row.
        let mut transaction = client.transaction().unwrap();
        transaction
            .batch_execute("SET LOCAL session_replication_role=replica")
            .unwrap();
        transaction
            .execute(
                "UPDATE atomic_tree_nodes SET payload=$2 WHERE node_hash=$1",
                &[&&hash[..], &vec![b'z'; canonical.len()]],
            )
            .unwrap();
        transaction.commit().unwrap();
        assert_eq!(
            load_node_block(client, hash).err().unwrap().code,
            "tree/node-content-corrupt"
        );
        let mut transaction = client.transaction().unwrap();
        transaction
            .batch_execute("SET LOCAL session_replication_role=replica")
            .unwrap();
        transaction
            .execute(
                "UPDATE atomic_tree_nodes SET payload=$2 WHERE node_hash=$1",
                &[&&hash[..], &canonical],
            )
            .unwrap();
        transaction.commit().unwrap();

        let lengths = client.query_one("SELECT octet_length(n.payload)::bigint,octet_length(b.physical_payload)::bigint FROM atomic_tree_nodes n JOIN atomic_tree_node_blocks b USING(node_hash) WHERE node_hash=$1", &[&&hash[..]]).unwrap();
        let raw_bytes: i64 = lengths.get(0);
        let extra_bytes: i64 = lengths.get(1);
        println!(
            "COMPRESSED_NODE_OK canonical_bytes={raw_bytes} extra_stored_payload_bytes={extra_bytes} transferred_payload_bytes={} encode_verify_ns={} decode_ns={} canonical_storage_retained=true server_hash_cpu_not_separately_measured=true",
            loaded.stats.physical_read_bytes,
            write.encode_elapsed_nanos,
            loaded.stats.decode_elapsed_nanos
        );

        // Test the cascade while bypassing only this fixture's raw immutability
        // trigger; the FK remains enabled, unlike replica-mode deletion.
        client
            .batch_execute(
                "ALTER TABLE atomic_tree_nodes DISABLE TRIGGER atomic_tree_nodes_immutable",
            )
            .unwrap();
        client
            .execute(
                "DELETE FROM atomic_tree_nodes WHERE node_hash=$1",
                &[&&hash[..]],
            )
            .unwrap();
        client
            .batch_execute(
                "ALTER TABLE atomic_tree_nodes ENABLE TRIGGER atomic_tree_nodes_immutable",
            )
            .unwrap();
        assert!(
            client
                .query_opt(
                    "SELECT node_hash FROM atomic_tree_node_blocks WHERE node_hash=$1",
                    &[&&hash[..]]
                )
                .unwrap()
                .is_none()
        );
        assert!(load_node_block(client, hash).unwrap().is_none());
    }
}
