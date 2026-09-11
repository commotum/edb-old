mod common;

use atomic_core::{
    Attribute, Cardinality, EntityRef, IndexTransaction, Keyword, LogCursorStats, LogTransaction,
    OperationContext, OperationKind, Peer, PostgresConnectionConfig, PostgresOperator, Schema,
    SsdCacheConfig, SsdCacheLimits, TimePoint, TxOp, Value, ValueType,
};
use std::io::{Read, Seek, Write};
use std::os::unix::fs::PermissionsExt;
use std::time::Instant;

fn schema() -> Schema {
    let mut schema = Schema::new();
    let mut text = Attribute::new(
        1000,
        Keyword::new("item", "text"),
        ValueType::String,
        Cardinality::One,
    );
    text.no_history = true;
    schema.install(text).unwrap();
    schema
}

fn cache_config(url: &str, directory: &std::path::Path) -> PostgresConnectionConfig {
    PostgresConnectionConfig::plaintext(url).with_ssd_cache(SsdCacheConfig {
        directory: directory.to_owned(),
        limits: SsdCacheLimits {
            max_entries: 128,
            max_bytes: 8 * 1024 * 1024,
        },
    })
}

fn scan(
    peer: &Peer,
) -> (
    Vec<LogTransaction>,
    LogCursorStats,
    atomic_core::SqlIoStats,
    u128,
) {
    let started = Instant::now();
    let context = OperationContext::diagnostic(OperationKind::Query);
    let _scope = context.enter();
    let log = peer.log();
    // Initial application schema occupies T=1; exercise submitted updates.
    let mut cursor = log.tx_range(Some(TimePoint::T(2)), None).unwrap();
    assert_eq!(cursor.stats().range_reads, 0);
    let rows = cursor.by_ref().collect::<Result<Vec<_>, _>>().unwrap();
    let stats = cursor.stats();
    assert!(cursor.next().is_none());
    drop(cursor);
    drop(log);
    (
        rows,
        stats,
        context.snapshot(),
        started.elapsed().as_micros(),
    )
}

#[test]
fn native_log_payload_cache_is_restart_hot_bounded_and_not_membership_authority() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP native log cache: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let started = Instant::now();
    let fixture = common::PostgresFixture::new(&url, "native_log_cache");
    let url = &fixture.connection;
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    common::install(url).unwrap();
    common::TestStore::connect(url)
        .unwrap()
        .create_database("log", schema())
        .unwrap();
    let service = common::start_service(url, "log");
    let mut expected = Vec::new();
    let mut entity = EntityRef::Temp("item".into());
    for i in 0..12 {
        let report = common::transact(
            &service,
            &format!("write-{i}"),
            i + 1,
            &[TxOp::Add {
                entity: entity.clone(),
                attribute: 1000,
                value: Value::String(format!("{i}:{}", "log-payload-".repeat(4096))).into(),
            }],
            1000 + i as i64,
        );
        if i == 0 {
            entity = EntityRef::Id(report.tempids["item"]);
        }
        expected.push(LogTransaction {
            t: report.basis_t,
            data: report.tx_data.clone(),
        });
    }
    service.shutdown();
    common::consolidate(url, "log").unwrap();
    let config = cache_config(url, directory.path());
    let peer = Peer::connect_configured_with_cache_limits(&config, "log", 0, 0).unwrap();
    let (cold, cold_stats, cold_sql, cold_us) = scan(&peer);
    assert_eq!(cold, expected);
    assert_eq!(cold_stats.cache_hits, 0);
    assert!(cold_stats.postgres_payload_bytes_read >= cold_stats.payload_bytes_read);
    assert_eq!(cold_stats.peak_buffered_transactions, 1);
    let (warm, warm_stats, warm_sql, warm_us) = scan(&peer);
    assert_eq!(warm, expected);
    assert_eq!(warm_stats.cache_hits, 12);
    assert_eq!(warm_stats.postgres_payload_bytes_read, 0);
    assert_eq!(
        warm_sql.sql_calls, 0,
        "captured root pin already owns the authenticated cached closure"
    );
    assert!(warm_sql.result_cell_bytes < cold_sql.result_cell_bytes / 10);
    let limits = peer.ssd_cache_stats();
    assert!(limits.current_entries <= 128 && limits.current_bytes <= 8 * 1024 * 1024);
    drop(peer);

    let reopen_started = Instant::now();
    let peer = Peer::connect_configured_with_cache_limits(&config, "log", 0, 0).unwrap();
    let (reopened, restart_stats, restart_sql, _) = scan(&peer);
    assert_eq!(reopened, expected);
    assert_eq!(restart_stats.cache_hits, 12);
    assert_eq!(restart_stats.postgres_payload_bytes_read, 0);
    assert!(restart_sql.result_cell_bytes < cold_sql.result_cell_bytes / 10);
    assert_eq!(
        peer.log().tx_data(IndexTransaction::T(7)).unwrap(),
        Some(expected[5].data.clone())
    );
    let restart_us = reopen_started.elapsed().as_micros();

    // The captured immutable log root supplies membership authority. A valid
    // content-addressed cache hit needs no mutable SQL membership row.

    // Corrupt only private disposable files: authentication falls back to SQL.
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
            byte[0] ^= 1;
            file.seek(std::io::SeekFrom::End(-1)).unwrap();
            file.write_all(&byte).unwrap();
        }
    }
    let (repaired, repaired_stats, _, _) = scan(&peer);
    assert_eq!(repaired, expected);
    assert_eq!(repaired_stats.cache_hits, 0);
    assert!(repaired_stats.postgres_payload_bytes_read >= repaired_stats.payload_bytes_read);
    assert!(peer.ssd_cache_stats().corruptions >= 12);

    let disabled = Peer::connect_configured_with_cache_limits(
        &PostgresConnectionConfig::plaintext(url),
        "log",
        0,
        0,
    )
    .unwrap();
    let (uncached, disabled_stats, _, _) = scan(&disabled);
    assert_eq!(uncached, expected);
    assert_eq!(disabled_stats.cache_hits, 0);
    assert!(disabled_stats.postgres_payload_bytes_read >= disabled_stats.payload_bytes_read);
    drop(disabled);
    drop(peer);
    drop(expected);
    drop(cold);
    drop(warm);
    drop(reopened);
    drop(repaired);
    drop(uncached);
    drop(directory);
    drop(fixture);
    eprintln!(
        "native log cache 12 transactions: cold={cold_us}us warm={warm_us}us reopen+scan={restart_us}us canonical={} cold_sql_cells={} warm_sql_cells={} restart_sql_cells={} complete={}ms; cache capacity 128/8MiB, RAM node cache disabled",
        cold_stats.payload_bytes_read,
        cold_sql.result_cell_bytes,
        warm_sql.result_cell_bytes,
        restart_sql.result_cell_bytes,
        started.elapsed().as_millis()
    );
}

#[test]
fn log_cache_excision_generation_and_explicit_purge_do_not_resurrect_content() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP native log cache excision: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "log_cache_excision");
    let url = &fixture.connection;
    common::install(url).unwrap();
    common::TestStore::connect(url)
        .unwrap()
        .create_database("log", schema())
        .unwrap();
    let service = common::start_service(url, "log");
    let inserted = common::transact(
        &service,
        "insert",
        1,
        &[TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1000,
            value: Value::String("excised-only-secret".into()).into(),
        }],
        1000,
    );
    let item = inserted.tempids["item"];
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let config = cache_config(url, directory.path());
    let old_peer = Peer::connect_configured_with_cache_limits(&config, "log", 0, 0).unwrap();
    let old_log = old_peer.log();
    assert_eq!(scan(&old_peer).0[0].data, inserted.tx_data);
    assert_eq!(scan(&old_peer).1.cache_hits, 1);
    common::transact(
        &service,
        "excise",
        2,
        &[TxOp::Add {
            entity: EntityRef::Temp("excision".into()),
            attribute: atomic_core::DB_EXCISE as u32,
            value: Value::Ref(item).into(),
        }],
        2000,
    );
    service.shutdown();
    PostgresOperator::connect(url)
        .unwrap()
        .process_excision_requests(
            &atomic_core::DatabaseCatalog::connect(url)
                .unwrap()
                .resolve("log")
                .unwrap()
                .database_id,
        )
        .unwrap();
    let new_peer = Peer::connect_configured_with_cache_limits(&config, "log", 0, 0).unwrap();
    assert!(new_peer.excision_generation() > old_log.generation());
    // Capturing the new recent tier can itself populate that generation's log
    // objects. Clear only its namespace so this scan witnesses a cold new
    // generation while the old generation remains demonstrably hot.
    assert!(new_peer.purge_ssd_generation(new_peer.excision_generation()));
    let (current, stats, _, _) = scan(&new_peer);
    assert_eq!(
        stats.cache_hits, 0,
        "old-generation namespace is not reusable"
    );
    assert!(
        !current
            .iter()
            .flat_map(|tx| &tx.data)
            .any(|d| d.entity == item && d.attribute == 1000)
    );
    assert_eq!(
        old_log
            .tx_data(IndexTransaction::T(inserted.basis_t))
            .unwrap(),
        Some(inserted.tx_data.clone())
    );
    assert!(new_peer.purge_ssd_generation(old_log.generation()));
    assert_eq!(
        scan(&old_peer).1.cache_hits,
        0,
        "explicit purge includes log payloads"
    );
    assert_eq!(
        scan(&new_peer).1.cache_hits,
        2,
        "other generation remains hot"
    );
    drop(new_peer);
    drop(old_log);
    drop(old_peer);
    drop(inserted);
}
