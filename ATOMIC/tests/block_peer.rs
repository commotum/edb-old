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
fn report_catchup_extends_exact_endpoints_without_replaying_each_prefix() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "block_peer_report_cost");
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
    BlockDatabase::create(&config, "items", schema).unwrap();
    let service = TransactionService::start(TransactionServiceConfig {
        connection: config.clone(),
        database_id: "items".into(),
        holder_id: "report-cost".into(),
        lease_duration: Duration::from_secs(10),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: Default::default(),
    })
    .unwrap();

    struct Case {
        prefix: usize,
        missing: usize,
        peer: Peer,
        held: atomic_core::DatabaseValue,
    }
    let open_cases = |prefix| {
        [16, 32, 64].map(|missing| {
            // No SSD or tree cache can hide repeated provider reads. Opening
            // the held prefix is fixture preparation, outside catch-up cost.
            let peer = Peer::connect_configured_with_cache_limits(&config, "items", 0, 0).unwrap();
            let held = peer.db();
            assert!(peer.enable_tx_reports());
            Case {
                prefix,
                missing,
                peer,
                held,
            }
        })
    };
    let mut cases = Vec::from(open_cases(0));
    let initial_basis = cases[0].held.basis_t();
    let indexed_basis = cases[0].peer.durable_base_t();
    let mut committed = Vec::new();
    let mut samples = Vec::new();
    for number in 1..=80 {
        committed.push(
            service
                .client()
                .transact(
                    TransactionRequest::from_edn(
                        format!("report-{number}"),
                        &format!(r#"[{{:db/id "item" :item/value {number}}}]"#),
                    )
                    .unwrap()
                    .with_tx_instant(1000 + number as i64),
                    WAIT,
                )
                .unwrap(),
        );
        while let Some(position) = cases
            .iter()
            .position(|case| case.prefix + case.missing == number)
        {
            let case = cases.swap_remove(position);
            let operation =
                atomic_core::OperationContext::new(atomic_core::OperationKind::PeerObservation);
            let started = Instant::now();
            {
                let _scope = operation.enter();
                let current = case.peer.sync().unwrap();
                assert_eq!(current.basis_t(), initial_basis + number as u64);
                assert_eq!(case.peer.durable_base_t(), indexed_basis);
                let reports = case.peer.take_tx_reports();
                assert_eq!(reports.len(), case.missing);
                for (offset, (report, expected)) in reports
                    .iter()
                    .zip(&committed[case.prefix..number])
                    .enumerate()
                {
                    assert_eq!(report.basis_t, expected.basis_t);
                    assert_eq!(report.tx_data, expected.tx_data);
                    assert_eq!(report.tempids, expected.tempids);
                    assert_eq!(report.tx_hash, expected.tx_hash);
                    assert!(!report.replayed);
                    assert_eq!(
                        report.db_before.snapshot_key().unwrap(),
                        expected.db_before.snapshot_key().unwrap()
                    );
                    assert_eq!(
                        report.db_after.snapshot_key().unwrap(),
                        expected.db_after.snapshot_key().unwrap()
                    );
                    let entity = report.tempids["item"];
                    assert!(report.db_before.values(entity, VALUE).unwrap().is_empty());
                    assert_eq!(
                        report.db_after.values(entity, VALUE).unwrap(),
                        [Value::Long((case.prefix + offset + 1) as i64)]
                    );
                }
                assert_eq!(case.held.basis_t(), initial_basis + case.prefix as u64);
                assert!(
                    case.held
                        .values(committed[number - 1].tempids["item"], VALUE)
                        .unwrap()
                        .is_empty()
                );
                // Include complete adoption, drain, verification and disposal
                // of the returned values, not only cursor construction.
                drop(reports);
                drop(current);
            }
            let elapsed = started.elapsed();
            let io = operation.snapshot();
            let report_calls =
                io.by_operation[&atomic_core::OperationKind::TransactionReport].calls;
            assert!(io.known_payload_read_bytes > 0);
            // Receipt membership and exact-root/prefix authentication still
            // cost bounded reads per report. Reopening both full recent tails
            // per transaction exceeds this ceiling for the larger cases.
            assert!(
                io.sql_calls <= 32 * case.missing as u64 + 128,
                "prefix={} missing={} repeated replay: {io:?}",
                case.prefix,
                case.missing
            );
            let unchanged =
                atomic_core::OperationContext::new(atomic_core::OperationKind::PeerObservation);
            {
                let _scope = unchanged.enter();
                assert_eq!(
                    case.peer.sync().unwrap().basis_t(),
                    committed.last().unwrap().basis_t
                );
                assert!(case.peer.take_tx_reports().is_empty());
            }
            assert!(
                !unchanged
                    .snapshot()
                    .by_operation
                    .contains_key(&atomic_core::OperationKind::TransactionReport)
            );
            eprintln!(
                "PEER_REPORT_CATCHUP prefix={} missing={} sql_calls={} report_calls={} payload_read_bytes={} complete_us={}",
                case.prefix,
                case.missing,
                io.sql_calls,
                report_calls,
                io.known_payload_read_bytes,
                elapsed.as_micros()
            );
            samples.push((case.prefix, case.missing, report_calls));
        }
        if number == 16 {
            cases.extend(open_cases(16));
        }
    }
    assert!(cases.is_empty());
    for missing in [16, 32, 64] {
        let calls = |prefix| {
            samples
                .iter()
                .find(|sample| sample.0 == prefix && sample.1 == missing)
                .unwrap()
                .2
        };
        assert!(
            calls(16).abs_diff(calls(0)) <= 4 * missing as u64 + 64,
            "report replay must not multiply the already captured prefix"
        );
    }
    service.shutdown();
}

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
        connection: PostgresConnectionConfig::plaintext(&fixture.connection),
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
    // Leave the manual peer behind while a physical index is published
    // between two receipts. Logical endpoint equality must not substitute for
    // the second receipt's exact before-root during one report batch.
    assert_eq!(
        service.client().request_index().unwrap().target_t,
        first.basis_t
    );
    attached.sync_index(first.basis_t, WAIT).unwrap();
    let second = service
        .client()
        .transact(request("second", 22), WAIT)
        .unwrap();
    assert_eq!(
        first.db_after.snapshot_key().unwrap(),
        second.db_before.snapshot_key().unwrap()
    );
    assert_ne!(
        reference,
        second.db_before.snapshot_reference().unwrap(),
        "the fixture must cross a real physical index publication"
    );
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
        assert_eq!(
            observed.db_before.snapshot_reference().unwrap(),
            committed.db_before.snapshot_reference().unwrap()
        );
        assert_eq!(
            observed.db_after.snapshot_reference().unwrap(),
            committed.db_after.snapshot_reference().unwrap()
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
        connection: PostgresConnectionConfig::plaintext(&fixture.connection),
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
    let explicit = atomic_core::index::recent::RecentLimits {
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
    let restarted = TransactionService::start_with_options(
        TransactionServiceConfig {
            connection: config,
            ..settings("smaller", capacity)
        },
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
