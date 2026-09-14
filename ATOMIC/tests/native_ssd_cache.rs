mod common;

use atomic_core::{
    Attribute, Cardinality, EntityRef, IndexOrder, Keyword, OperationContext, OperationKind, Peer,
    PostgresConnectionConfig, Schema, SsdCacheConfig, SsdCacheLimits, TxOp, Value, ValueType,
};
use std::io::{Read, Seek, Write};
use std::os::unix::fs::PermissionsExt;

#[test]
fn postgres_native_ssd_reopen_corruption_disable_and_purge() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED native SSD PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "native_ssd");
    let url = &fixture.connection;
    let directory = tempfile::Builder::new()
        .prefix("atomic-native-ssd-")
        .tempdir()
        .unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    common::install(url).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "text"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    common::TestStore::connect(url)
        .unwrap()
        .create_database("ssd", schema)
        .unwrap();
    let service = common::start_service(url, "ssd");
    let ops = (0..256)
        .map(|index| TxOp::Add {
            entity: EntityRef::Temp(format!("item-{index}")),
            attribute: 1000,
            value: Value::String(format!("{index}:{}", "repeatable-content-".repeat(100))).into(),
        })
        .collect::<Vec<_>>();
    let report = common::transact(&service, "populate", 1, &ops, 1000);
    let expected = report.db_after.datoms(IndexOrder::Eavt).unwrap();
    drop(report);
    service.shutdown();
    common::consolidate(url, "ssd").unwrap();
    let config = PostgresConnectionConfig::plaintext(url).with_ssd_cache(SsdCacheConfig {
        directory: directory.path().to_owned(),
        limits: SsdCacheLimits {
            max_entries: 128,
            max_bytes: 16 * 1024 * 1024,
        },
    });
    // Disable RAM admission to isolate SSD reuse. Opening still authorizes and
    // authenticates the native root through PostgreSQL before any SSD access.
    let peer = Peer::connect_configured_with_cache_limits(&config, "ssd", 0, 0).unwrap();
    let generation = peer.excision_generation();
    let cold = OperationContext::named(OperationKind::Query, Keyword::new("app", "cold")).unwrap();
    {
        let _scope = cold.enter();
        assert_eq!(
            peer.snapshot()
                .database_value()
                .datoms(IndexOrder::Eavt)
                .unwrap(),
            expected
        );
    }
    let cold_sql = cold.snapshot();
    let cold_blocks = peer.node_block_read_stats();
    let initial = peer.ssd_cache_stats();
    assert!(initial.puts > 0);
    use atomic_core::CacheTier;
    assert!(cold_sql.reads.cache[&CacheTier::DecodedNode].misses > 0);
    assert!(cold_sql.reads.cache[&CacheTier::LocalDisk].misses > 0);
    assert!(cold_sql.reads.cache[&CacheTier::PostgresBlock].hits > 0);
    assert!(cold_sql.reads.indexes[&IndexOrder::Eavt].node_accesses > 0);
    assert!(cold_blocks.compressed_hits > 0);
    assert!(cold_blocks.physical_read_bytes < cold_blocks.canonical_bytes);
    assert!(initial.current_entries <= 128 && initial.current_bytes <= 16 * 1024 * 1024);
    drop(peer);

    let reopened = OperationContext::diagnostic(OperationKind::Query);
    let (peer, reopen_stats) =
        reopened.measure(|| Peer::connect_configured_with_cache_limits(&config, "ssd", 0, 0));
    let peer = peer.unwrap();
    assert!(
        reopen_stats.stats.sql_calls > 0,
        "opening establishes current authority"
    );
    let restart = OperationContext::diagnostic(OperationKind::Query);
    {
        let _scope = restart.enter();
        assert_eq!(
            peer.snapshot()
                .database_value()
                .datoms(IndexOrder::Eavt)
                .unwrap(),
            expected
        );
    }
    let restart_sql = restart.snapshot();
    let restart_cache = peer.ssd_cache_stats();
    assert!(restart_cache.hits > 0);
    assert!(restart_sql.reads.cache[&CacheTier::LocalDisk].hits > 0);
    assert!(
        !restart_sql
            .reads
            .cache
            .contains_key(&CacheTier::PostgresBlock)
    );
    assert_eq!(peer.node_block_read_stats().canonical_bytes, 0);
    assert_eq!(peer.load_stats().cursor_sql_read_bytes, 0);
    assert_eq!(peer.load_stats().cursor_sql_reads, 0);
    assert!(restart_sql.sql_calls < cold_sql.sql_calls);
    assert_eq!(
        restart_sql.sql_calls, 0,
        "the authenticated captured value permits warm reads"
    );
    let warm_peer =
        Peer::connect_configured_with_cache_limits(&config, "ssd", 128, 16 * 1024 * 1024).unwrap();
    let warm_db = warm_peer.snapshot().database_value();
    assert_eq!(warm_db.datoms(IndexOrder::Eavt).unwrap(), expected);
    let warm = OperationContext::diagnostic(OperationKind::Query);
    let (result, warm_stats) = warm.measure(|| warm_db.datoms(IndexOrder::Eavt));
    assert_eq!(result.unwrap(), expected);
    assert!(warm_stats.stats.reads.cache[&CacheTier::DecodedNode].hits > 0);
    assert!(
        !warm_stats
            .stats
            .reads
            .cache
            .contains_key(&CacheTier::LocalDisk)
    );
    assert!(
        !warm_stats
            .stats
            .reads
            .cache
            .contains_key(&CacheTier::PostgresBlock)
    );
    let plain = OperationContext::new(OperationKind::Query);
    let started = std::time::Instant::now();
    let (result, plain_stats) = plain.measure(|| warm_db.datoms(IndexOrder::Eavt));
    let disabled_us = started.elapsed().as_micros();
    assert_eq!(result.unwrap(), expected);
    assert_eq!(plain_stats.stats.reads, Default::default());
    eprintln!(
        "diagnostics warm complete read enabled={}us disabled={}us cold_payload={} disk_payload={}",
        warm_stats.operation_elapsed_nanos / 1000,
        disabled_us,
        cold_sql.reads.cache[&CacheTier::PostgresBlock].physical_bytes,
        restart_sql.reads.cache[&CacheTier::LocalDisk].physical_bytes
    );
    drop(warm_peer);

    // Damage only owned cache entries, preserving framing and permissions.
    // Authentication rejects them and refetches canonical facts from storage.
    let mut damaged = 0;
    for entry in std::fs::read_dir(directory.path()).unwrap() {
        let path = entry.unwrap().path();
        if path
            .extension()
            .is_some_and(|extension| extension == "block")
        {
            let mut file = std::fs::OpenOptions::new()
                .read(true)
                .write(true)
                .open(path)
                .unwrap();
            file.seek(std::io::SeekFrom::End(-1)).unwrap();
            let mut byte = [0];
            file.read_exact(&mut byte).unwrap();
            byte[0] ^= 0xff;
            file.seek(std::io::SeekFrom::End(-1)).unwrap();
            file.write_all(&byte).unwrap();
            damaged += 1;
        }
    }
    assert!(damaged > 0);
    assert_eq!(
        peer.snapshot()
            .database_value()
            .datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    assert!(peer.ssd_cache_stats().corruptions > 0);
    assert!(peer.node_block_read_stats().canonical_bytes > 0);
    drop(peer);

    // A damaged owned .block header is not deletion authority, but must not
    // prevent a fresh native peer from opening and falling back to PostgreSQL.
    let malformed_entries = std::fs::read_dir(directory.path())
        .unwrap()
        .map(|entry| entry.unwrap().path())
        .filter(|path| {
            path.extension()
                .is_some_and(|extension| extension == "block")
        })
        .map(|path| {
            let intact = std::fs::read(&path).unwrap();
            let mut damaged = intact.clone();
            damaged[0] ^= 0xff;
            std::fs::write(&path, &damaged).unwrap();
            (path, intact, damaged)
        })
        .collect::<Vec<_>>();
    assert!(!malformed_entries.is_empty());
    let peer = Peer::connect_configured_with_cache_limits(&config, "ssd", 0, 0).unwrap();
    assert!(!peer.ssd_cache_stats().inventory_complete);
    assert!(peer.ssd_cache_stats().corrupt_entry_bypasses > 0);
    assert_eq!(
        peer.snapshot()
            .database_value()
            .datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    assert!(peer.node_block_read_stats().canonical_bytes > 0);
    assert_eq!(peer.ssd_cache_stats().puts, 0);
    assert!(!peer.purge_ssd_generation(generation));
    for (path, _, damaged) in &malformed_entries {
        assert_eq!(&std::fs::read(path).unwrap(), damaged);
    }
    drop(peer);
    // Explicit fixture repair restores the original owned file; the cache
    // itself never overwrote or deleted the uncertain representation.
    for (path, intact, _) in malformed_entries {
        std::fs::write(path, intact).unwrap();
    }
    let peer = Peer::connect_configured_with_cache_limits(&config, "ssd", 0, 0).unwrap();
    assert!(peer.ssd_cache_stats().inventory_complete);
    assert!(peer.purge_ssd_generation(generation));
    assert_eq!(peer.ssd_cache_stats().current_entries, 0);
    drop(peer);

    let disabled = Peer::connect_with_cache_limits(url, "ssd", 0, 0).unwrap();
    assert_eq!(
        disabled
            .snapshot()
            .database_value()
            .datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    assert_eq!(disabled.ssd_cache_stats().puts, 0);
    assert_eq!(disabled.ssd_cache_stats().physical_read_bytes, 0);
    let eviction_directory = tempfile::Builder::new()
        .prefix("atomic-native-ssd-evict-")
        .tempdir()
        .unwrap();
    std::fs::set_permissions(
        eviction_directory.path(),
        std::fs::Permissions::from_mode(0o700),
    )
    .unwrap();
    let eviction_config = PostgresConnectionConfig::plaintext(url).with_ssd_cache(SsdCacheConfig {
        directory: eviction_directory.path().to_owned(),
        limits: SsdCacheLimits {
            max_entries: 1,
            max_bytes: 16 * 1024 * 1024,
        },
    });
    let evicting =
        Peer::connect_configured_with_cache_limits(&eviction_config, "ssd", 0, 0).unwrap();
    for _ in 0..2 {
        assert_eq!(
            evicting
                .snapshot()
                .database_value()
                .datoms(IndexOrder::Eavt)
                .unwrap(),
            expected
        );
    }
    assert!(evicting.ssd_cache_stats().evictions > 0);
    assert_eq!(evicting.ssd_cache_stats().current_entries, 1);
    eprintln!(
        "NATIVE_SSD_OK cold_sql={} reopen_sql={} hits={} canonical_bytes={} physical_payload_bytes={} cache_bytes={} damaged_entries={damaged} framing_reopen_fallback=true framing_preserved=true",
        cold_sql.sql_calls,
        restart_sql.sql_calls,
        restart_cache.hits,
        cold_blocks.canonical_bytes,
        cold_blocks.physical_read_bytes,
        initial.current_bytes
    );
}
