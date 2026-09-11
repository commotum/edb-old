use super::*;

struct Fixture {
    admin: postgres::Client,
    schema: String,
    config: PostgresConnectionConfig,
}
impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}
fn fixture() -> Option<Fixture> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP compressed opaque PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("block_compression_{:032x}", crate::uuid_v7().unwrap());
    let mut admin = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let scoped = if connection.starts_with("postgres://") || connection.starts_with("postgresql://")
    {
        format!(
            "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
            if connection.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{connection} options='-csearch_path={schema},pg_catalog'")
    };
    let config = PostgresConnectionConfig::plaintext(scoped);
    PgBlockStore::install(&config).unwrap();
    Some(Fixture {
        admin,
        schema,
        config,
    })
}

#[test]
fn aggregate_expansion_is_admitted_before_any_decompression() {
    let canonical = vec![b'x'; 1024];
    let id = sha256(&canonical);
    let mut physical = crate::storage::codec::encode_block(&canonical).unwrap();
    assert!(crate::storage::codec::is_encoded(&physical));
    // Neither member is actually valid for its advertised length. An eager
    // decoder would fail decompression before ever checking the batch bound.
    physical[10..18].copy_from_slice(&((MAX_BLOCK_BYTES / 2 + 1) as u64).to_be_bytes());
    let mut stats = ObjectReadStats::default();
    assert_eq!(
        decode_objects(
            &[id, id],
            vec![Some(physical.clone()), Some(physical.clone())],
            &mut stats
        )
        .unwrap_err()
        .code,
        "storage/read-budget"
    );
    assert_eq!(stats.physical_bytes, (physical.len() * 2) as u64);
    assert_eq!(stats.canonical_bytes, 0);
    assert_eq!(stats.compressed_hits + stats.raw_reads, 0);
    physical[10..18].copy_from_slice(&((MAX_BLOCK_BYTES + 1) as u64).to_be_bytes());
    assert_eq!(
        decode_objects(&[id], vec![Some(physical)], &mut stats)
            .unwrap_err()
            .code,
        "storage/object-corrupt"
    );
    assert_eq!(stats.canonical_bytes, 0);
}

#[test]
fn exact_aggregate_boundary_preserves_order_duplicates_and_raw_marker_content() {
    let canonical = vec![b'a'; MAX_BLOCK_BYTES / 2];
    let physical = crate::storage::codec::encode_block(&canonical).unwrap();
    let id = sha256(&canonical);
    let mut stats = ObjectReadStats::default();
    let result = decode_objects(
        &[id, [0; 32], id],
        vec![Some(physical.clone()), None, Some(physical)],
        &mut stats,
    )
    .unwrap();
    assert_eq!(result[0].as_deref(), Some(canonical.as_slice()));
    assert!(result[1].is_none());
    assert_eq!(result[2].as_deref(), Some(canonical.as_slice()));
    assert_eq!(stats.canonical_bytes, MAX_BLOCK_BYTES as u64);
    assert_eq!(stats.compressed_hits, 2);
    drop(result);
    let raw = b"ATOMICBL not an envelope but legal opaque raw bytes".to_vec();
    let id = sha256(&raw);
    assert_eq!(
        decode_objects(&[id], vec![Some(raw.clone())], &mut stats).unwrap(),
        vec![Some(raw)]
    );
    assert_eq!(stats.raw_reads, 1);
}

#[test]
fn postgres_compressed_objects_reput_canonical_bytes_and_reject_corruption() {
    let Some(f) = fixture() else {
        return;
    };
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let canonical = vec![b'q'; 256 * 1024];
    let id = store.put(&canonical).unwrap();
    assert_eq!(id, sha256(&canonical));
    let mut sql = f.config.connect_for("test/compression").unwrap();
    let physical: Vec<u8> = sql
        .query_one(
            "SELECT payload FROM atomic_objects WHERE id=$1",
            &[&&id[..]],
        )
        .unwrap()
        .get(0);
    assert!(physical.len() < canonical.len() / 8);
    let before = store.object_read_stats();
    assert_eq!(store.get(id).unwrap(), Some(canonical.clone()));
    let after = store.object_read_stats();
    assert_eq!(
        after.physical_bytes - before.physical_bytes,
        physical.len() as u64
    );
    assert_eq!(
        after.canonical_bytes - before.canonical_bytes,
        canonical.len() as u64
    );
    assert_eq!(after.compressed_hits - before.compressed_hits, 1);
    assert_eq!(after.raw_reads, before.raw_reads);
    assert!(after.decode_nanos >= before.decode_nanos);
    // Raw and gzip are both current physical variants. An existing valid raw
    // object must not be rewritten just because this writer chooses gzip.
    sql.execute(
        "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
        &[&&id[..], &&canonical[..]],
    )
    .unwrap();
    let guards = [RefCondition {
        key: "test/guard".into(),
        expected: None,
    }];
    assert_eq!(store.put(&canonical).unwrap(), id);
    assert!(
        matches!(store.put_protected(&canonical, 7, &guards).unwrap(), Guarded::Applied(got) if got == id)
    );
    let row = sql
        .query_one(
            "SELECT payload,protected_epoch FROM atomic_objects WHERE id=$1",
            &[&&id[..]],
        )
        .unwrap();
    assert_eq!(row.get::<_, Vec<u8>>(0), canonical);
    assert_eq!(row.get::<_, i64>(1), 7);
    // Guarded re-put may touch only after canonical authentication succeeds.
    let mut corrupt = physical.clone();
    let last = corrupt.len() - 1;
    corrupt[last] ^= 1;
    sql.execute(
        "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
        &[&&id[..], &&corrupt[..]],
    )
    .unwrap();
    assert_eq!(store.get(id).unwrap_err().code, "storage/object-corrupt");
    assert_eq!(
        store
            .put_protected(&canonical, 9, &guards)
            .unwrap_err()
            .code,
        "storage/object-corrupt"
    );
    let row = sql
        .query_one(
            "SELECT payload,protected_epoch FROM atomic_objects WHERE id=$1",
            &[&&id[..]],
        )
        .unwrap();
    assert_eq!(row.get::<_, Vec<u8>>(0), corrupt);
    assert_eq!(
        row.get::<_, i64>(1),
        7,
        "corrupt-object rejection rolls back epoch mutation"
    );
    sql.execute(
        "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
        &[&&id[..], &&physical[..]],
    )
    .unwrap();
    assert_eq!(store.get(id).unwrap(), Some(canonical));
}

#[test]
fn postgres_full_size_object_remains_legal_and_multiget_bomb_is_rejected() {
    let Some(f) = fixture() else {
        return;
    };
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let canonical = vec![b'z'; MAX_BLOCK_BYTES];
    let id = store.put(&canonical).unwrap();
    assert_eq!(
        store.get(id).unwrap().as_deref(),
        Some(canonical.as_slice())
    );
    let before = store.object_read_stats();
    assert_eq!(
        store.get_many(&[id, id]).unwrap_err().code,
        "storage/read-budget"
    );
    let after = store.object_read_stats();
    assert!(after.physical_bytes > before.physical_bytes);
    assert_eq!(after.canonical_bytes, before.canonical_bytes);
    assert_eq!(after.compressed_hits, before.compressed_hits);
    assert_eq!(
        store.get_many(&[id, [0; 32]]).unwrap(),
        vec![Some(canonical), None]
    );
}
