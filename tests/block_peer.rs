//! The stock live peer, connection, reports and caches use opaque authority.
mod common;

use atomic_core::storage::{BlockDatabase, BlockReadConfig, BlockReader, PgBlockStore};
use atomic_core::{
    Attribute, Cardinality, Connection, IndexOrder, IndexPrefix, Keyword, Peer,
    PostgresConnectionConfig, Schema, SsdCacheConfig, SsdCacheLimits, TransactionRequest,
    TransactionService, TransactionServiceConfig, Value, ValueType,
};
use std::os::unix::fs::PermissionsExt;
use std::time::{Duration, Instant};

const VALUE: u32 = 1000;
const WAIT: Duration = Duration::from_secs(20);

#[test]
fn live_peer_reports_index_adoption_and_restart_cache_use_the_same_block_values() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "block_peer_live");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        VALUE,
        Keyword::new("item", "value"),
        ValueType::Long,
        Cardinality::One,
    );
    attribute.indexed = true;
    schema.install(attribute).unwrap();
    BlockDatabase::create(&config, "items", schema).unwrap();
    let started = Instant::now();
    let service = TransactionService::start(TransactionServiceConfig {
        connection: fixture.connection.clone(),
        database_id: "items".into(),
        holder_id: "peer-test".into(),
        lease_duration: Duration::from_secs(10),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: Default::default(),
    })
    .unwrap();
    let peer = Peer::connect_configured(&config, "items", 32).unwrap();
    let attached = Connection::attach_configured(config.clone(), service.client(), 32).unwrap();
    let independent = Connection::connect_configured(config.clone(), "items", 32).unwrap();
    assert_eq!(peer.identity(), service.identity());
    assert_eq!(peer.basis_t(), 1);
    assert!(peer.enable_tx_reports());
    let request = |name: &str, number: i64| {
        TransactionRequest::from_edn(
            name,
            &format!(r#"[{{:db/id "item" :item/value {number}}}]"#),
        )
        .unwrap()
        .with_tx_instant(1000 + number)
    };
    let first_request = request("first", 11);
    let first = attached
        .submit(first_request.clone())
        .unwrap()
        .wait(WAIT)
        .unwrap();
    let held = first.db_after.clone();
    let reference = held.snapshot_reference().unwrap();
    let second = service
        .client()
        .transact(request("second", 22), WAIT)
        .unwrap();
    assert_eq!(
        independent
            .sync_to(second.basis_t, WAIT)
            .unwrap_or_else(|error| panic!(
                "independent wait: {error}; observation={:?}",
                independent.observation_error()
            ))
            .basis_t(),
        second.basis_t
    );
    let observed = peer.sync_to(second.basis_t, WAIT).unwrap();
    assert_eq!(
        observed.snapshot_key().unwrap(),
        second.db_after.snapshot_key().unwrap()
    );
    let reports = peer.take_tx_reports();
    assert_eq!(reports.len(), 2);
    for (observed, committed) in reports.iter().zip([&first, &second]) {
        assert_eq!(observed.tempids, committed.tempids);
        assert_eq!(observed.tx_data, committed.tx_data);
        assert_eq!(observed.tx_hash, committed.tx_hash);
        assert_eq!(
            observed.db_before.snapshot_key().unwrap(),
            committed.db_before.snapshot_key().unwrap()
        );
        assert_eq!(
            observed.db_after.snapshot_key().unwrap(),
            committed.db_after.snapshot_key().unwrap()
        );
        assert!(!observed.replayed);
    }
    peer.sync().unwrap();
    assert!(peer.take_tx_reports().is_empty());
    let replay = attached.submit(first_request).unwrap().wait(WAIT).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tempids, first.tempids);
    attached.sync_to(second.basis_t, WAIT).unwrap();
    assert_eq!(attached.db().basis_t(), second.basis_t);
    service.client().request_index().unwrap();
    peer.sync_index(second.basis_t, WAIT).unwrap();
    assert_eq!(peer.recent_stats().transactions, 0);
    assert_eq!(peer.durable_base_t(), second.basis_t);
    assert_eq!(
        held.values(first.tempids["item"], VALUE).unwrap(),
        [Value::Long(11)]
    );
    let reopened = peer.reopen_snapshot(&reference).unwrap();
    assert_eq!(
        reopened.snapshot_key().unwrap(),
        held.snapshot_key().unwrap()
    );
    assert_eq!(
        reopened.values(first.tempids["item"], VALUE).unwrap(),
        [Value::Long(11)]
    );
    let snapshot = peer.snapshot();
    let prefix = IndexPrefix::Avet {
        attribute: VALUE,
        value: None,
        entity: None,
    };
    let rows = snapshot.datoms_with_prefix(false, &prefix).unwrap().datoms;
    assert_eq!(rows.len(), 2);
    assert_eq!(
        rows.iter().map(|d| d.value.clone()).collect::<Vec<_>>(),
        [Value::Long(11), Value::Long(22)]
    );
    assert_eq!(
        snapshot
            .range(false, IndexOrder::Avet, Some(&rows[0]), Some(&rows[1]))
            .unwrap()
            .datoms,
        rows[..1]
    );
    assert_eq!(
        snapshot
            .seek(false, IndexOrder::Avet, &rows[1])
            .unwrap()
            .datom,
        Some(rows[1].clone())
    );
    let mut absent_key = rows[0].clone();
    absent_key.attribute = VALUE - 1;
    assert_eq!(
        snapshot
            .seek(false, IndexOrder::Avet, &absent_key)
            .unwrap()
            .datom,
        Some(rows[0].clone()),
        "raw range positions need not name an indexed attribute"
    );
    let before = peer.load_stats();
    snapshot.datoms_with_prefix(false, &prefix).unwrap();
    let after = peer.load_stats();
    assert!(after.cursor_ranges > before.cursor_ranges);
    assert!(after.cursor_cache_hits > before.cursor_cache_hits);
    assert_eq!(after.compatibility_materializations, 0);

    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let cached = config.clone().with_ssd_cache(SsdCacheConfig {
        directory: directory.path().to_path_buf(),
        limits: SsdCacheLimits::default(),
    });
    let cold = Peer::connect_configured_with_cache_limits(&cached, "items", 0, 0).unwrap();
    let cold_rows = cold
        .snapshot()
        .datoms_with_prefix(false, &prefix)
        .unwrap()
        .datoms;
    let cold_stats = cold.ssd_cache_stats();
    assert!(cold_stats.puts > 0);
    drop(cold);
    let warm = Peer::connect_configured_with_cache_limits(&cached, "items", 0, 0).unwrap();
    assert_eq!(
        warm.snapshot()
            .datoms_with_prefix(false, &prefix)
            .unwrap()
            .datoms,
        cold_rows
    );
    let warm_stats = warm.ssd_cache_stats();
    assert!(
        warm_stats.hits > 0,
        "restart must reuse authenticated local objects"
    );
    let reads = warm.node_block_read_stats();
    drop(warm);
    drop(snapshot);
    drop(reopened);
    drop(observed);
    drop(held);
    drop(reports);
    drop(independent);
    drop(attached);
    drop(peer);
    service.shutdown();
    eprintln!(
        "BLOCK_PEER complete_us={} report_count=2 indexed_transactions=2 cursor_ranges={} cache_hits={} cold_ssd_puts={} restart_ssd_hits={} provider_physical_bytes={}",
        started.elapsed().as_micros(),
        after.cursor_ranges,
        after.cursor_cache_hits,
        cold_stats.puts,
        warm_stats.hits,
        reads.physical_read_bytes
    );
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let tables: Vec<String> = sql.query("SELECT tablename::text FROM pg_catalog.pg_tables WHERE schemaname=current_schema() ORDER BY tablename", &[]).unwrap().into_iter().map(|row| row.get(0)).collect();
    assert_eq!(tables, ["atomic_objects", "atomic_refs"]);
}

#[test]
fn committed_windows_survive_changed_writer_policy_but_explicit_reader_limits_apply() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "block_peer_read_policy");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let database = BlockDatabase::create(&config, "items", schema).unwrap();
    let settings = |holder: &str, capacity_limits| TransactionServiceConfig {
        connection: fixture.connection.clone(),
        database_id: "items".into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(10),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits,
    };
    let service = TransactionService::start(settings("original", Default::default())).unwrap();
    let forms = (0..32)
        .map(|n| format!(r#"{{:db/id "item-{n}" :item/value {n}}}"#))
        .collect::<Vec<_>>()
        .join(" ");
    let request = TransactionRequest::from_edn("preserved", &format!("[{forms}]"))
        .unwrap()
        .with_tx_instant(1000);
    let first = service.client().transact(request.clone(), WAIT).unwrap();
    assert_eq!(
        service
            .client()
            .background_indexing_stats()
            .total_transactions,
        1
    );
    service.shutdown();
    let limits = BlockReadConfig::default();
    assert_eq!(limits.max_recent_transactions, usize::MAX);
    assert_eq!(limits.max_recent_datoms, usize::MAX);
    assert_eq!(limits.max_recent_bytes, usize::MAX);
    let bounded = BlockReader::connect(
        &config,
        BlockReadConfig {
            max_recent_datoms: 1,
            ..limits
        },
    )
    .unwrap();
    assert_eq!(
        bounded.capture(&database.reference_key()).unwrap_err().code,
        "storage/recent-window-limit"
    );
    let explicit = atomic_core::recent::RecentLimits {
        soft_bytes: 1,
        hard_bytes: 1,
        soft_datoms: 1,
        hard_datoms: 1,
    };
    assert!(Peer::connect_configured_with_limits(&config, "items", 8, 4096, explicit).is_err());
    let peer = Peer::connect_configured(&config, "items", 8).unwrap();
    assert_eq!(peer.basis_t(), first.basis_t);
    let reference = first.db_after.snapshot_reference().unwrap();
    let exact = reference.open(&config, 0, 0).unwrap();
    assert_eq!(
        exact.snapshot_key().unwrap(),
        first.db_after.snapshot_key().unwrap()
    );
    assert_eq!(
        exact.datoms(IndexOrder::Eavt).unwrap(),
        first.db_after.datoms(IndexOrder::Eavt).unwrap()
    );
    let capacity = atomic_core::CapacityLimits {
        max_transaction_ops: 1,
        max_transaction_bytes: 1,
        ..Default::default()
    };
    let restarted = TransactionService::start_configured_with_options(
        settings("smaller", capacity),
        config,
        atomic_core::ServiceOptions {
            indexing: atomic_core::BackgroundIndexingConfig {
                memory_index_threshold_bytes: 1,
                memory_index_max_bytes: 1,
            },
            ..Default::default()
        },
    )
    .unwrap();
    let retry = restarted.client().transact(request, WAIT).unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, first.basis_t);
    assert_eq!(retry.tx_hash, first.tx_hash);
    assert_eq!(retry.tempids, first.tempids);
    restarted.shutdown();
}
