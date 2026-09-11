//! One complete-path sample, not a scale benchmark. This target deliberately
//! has one test so process-wide background I/O deltas have no sibling workload.
//! Payload counters measure known transferred bytes, not disk traffic or RSS.
mod common;

use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::{
    Attribute, BackgroundIndexingConfig, BackgroundIndexingStats, CapacityLimits, Cardinality,
    EntityRef, Keyword, OperationContext, OperationKind, Peer, PostgresConnectionConfig, Schema,
    ServiceOptions, SqlIoStats, TransactionClient, TransactionRequest, TransactionService,
    TransactionServiceConfig, TxOp, Value, ValueType, process_sql_stats,
};
use std::time::{Duration, Instant};

const SCORE: u32 = 1000;
const PAYLOAD: u32 = 1001;
const ENTITIES: usize = 2048;
const PAYLOAD_BYTES: usize = 768;
const CACHE_BYTES: usize = 1024 * 1024;
const SMALL_WRITES: usize = 24;
const WAIT: Duration = Duration::from_secs(30);

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn payload(entity: usize) -> String {
    // Deterministic mixed text avoids making the larger-than-cache claim rely
    // on one repeated string while keeping preparation inexpensive.
    let mut result = String::with_capacity(PAYLOAD_BYTES);
    let mut state = entity as u64 + 1;
    for _ in 0..PAYLOAD_BYTES {
        state = state
            .wrapping_mul(6364136223846793005)
            .wrapping_add(1442695040888963407);
        result.push((b'!' + ((state >> 32) % 90) as u8) as char);
    }
    result
}

fn wait_index(client: &TransactionClient, through: u64) -> BackgroundIndexingStats {
    let deadline = Instant::now() + WAIT;
    loop {
        let stats = client.background_indexing_stats();
        assert!(
            client.is_available(),
            "background indexing stopped: {stats:?}"
        );
        assert_eq!(
            stats.jobs_failed, 0,
            "background indexing failed: {stats:?}"
        );
        if stats.published_basis_t >= through && !stats.job_in_flight {
            return stats;
        }
        assert!(
            Instant::now() < deadline,
            "automatic indexing did not drain: {stats:?}"
        );
        std::thread::sleep(Duration::from_millis(5));
    }
}

fn delta(after: u64, before: u64) -> u64 {
    after.checked_sub(before).unwrap()
}
fn lane(stats: &SqlIoStats, kind: OperationKind) -> (u64, u64) {
    stats
        .by_operation
        .get(&kind)
        .map_or((0, 0), |s| (s.calls, s.elapsed_nanos))
}

#[test]
fn complete_live_writes_and_automatic_indexes_exceed_cache_while_warm_values_need_no_storage() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block live costs: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "block_live_costs");
    let connection = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&connection).unwrap();
    let mut schema = Schema::new();
    for (id, name, kind) in [
        (SCORE, "score", ValueType::Long),
        (PAYLOAD, "payload", ValueType::String),
    ] {
        schema
            .install(Attribute::new(
                id,
                Keyword::new("item", name),
                kind,
                Cardinality::One,
            ))
            .unwrap();
    }
    BlockDatabase::create(&connection, "live-costs", schema).unwrap();

    let start_io = process_sql_stats();
    let complete_started = Instant::now();
    let complete_context = OperationContext::new(OperationKind::Application);
    let _complete_scope = complete_context.enter();
    let service = TransactionService::start_with_options(
        TransactionServiceConfig {
            connection: connection.clone(),
            database_id: "live-costs".into(),
            holder_id: "cost-sample".into(),
            lease_duration: Duration::from_secs(60),
            renew_interval: Duration::from_secs(5),
            queue_capacity: 4,
            capacity_limits: CapacityLimits {
                writer_tree_cache_entries: 64,
                writer_tree_cache_bytes: CACHE_BYTES,
                ..CapacityLimits::default()
            },
        },
        ServiceOptions {
            index_preparation_parallelism: 2,
            // Every nonempty novelty triggers the automatic scheduler. There
            // is intentionally no explicit request_index call in this test.
            indexing: BackgroundIndexingConfig {
                memory_index_threshold_bytes: 128,
                memory_index_max_bytes: 32 * 1024 * 1024,
            },
            ..ServiceOptions::default()
        },
    )
    .unwrap();
    let client = service.client();
    let peer =
        Peer::connect_configured_with_cache_limits(&connection, "live-costs", 64, CACHE_BYTES)
            .unwrap();
    let startup_capture_us = complete_started.elapsed().as_micros();
    let mut operations = Vec::with_capacity(ENTITIES * 2);
    for n in 0..ENTITIES {
        let entity = EntityRef::Temp(format!("item-{n}"));
        operations.push(add(entity.clone(), SCORE, Value::Long(n as i64)));
        operations.push(add(entity, PAYLOAD, Value::String(payload(n))));
    }
    let seed_started = Instant::now();
    let seed = client
        .transact(
            TransactionRequest::new("populate", operations).with_tx_instant(1000),
            WAIT,
        )
        .unwrap();
    let seed_commit_us = seed_started.elapsed().as_micros();
    let payload_bytes: usize = seed
        .tx_data
        .iter()
        .filter_map(|d| match &d.value {
            Value::String(value) if d.attribute == PAYLOAD => Some(value.len()),
            _ => None,
        })
        .sum();
    assert_eq!(payload_bytes, ENTITIES * PAYLOAD_BYTES);
    assert!(
        payload_bytes > CACHE_BYTES,
        "current string payload alone must exceed each node cache"
    );
    let entity = seed.tempids["item-1024"];
    let probe = seed.tempids["item-1536"];
    let seeded = wait_index(&client, seed.basis_t);
    let seed_commit_and_index_us = seed_started.elapsed().as_micros();
    assert!(seeded.jobs_completed >= 1);
    assert_eq!(seeded.total_transactions, 0);
    let captured = peer.sync_database_value().unwrap();
    assert_eq!(captured.basis_t(), seed.basis_t);
    assert_eq!(
        captured.values(probe, SCORE).unwrap(),
        vec![Value::Long(1536)]
    );

    let writes = OperationContext::new(OperationKind::Transaction);
    let writes_started = Instant::now();
    let mut first_half_ns = 0;
    let mut second_half_ns = 0;
    let mut last_basis = seed.basis_t;
    {
        let _scope = writes.enter();
        for n in 0..SMALL_WRITES {
            let started = Instant::now();
            let value = 10_000 + n as i64;
            let report = client
                .transact(
                    TransactionRequest::new(
                        format!("small-{n}"),
                        vec![add(EntityRef::Id(entity), SCORE, Value::Long(value))],
                    )
                    .with_tx_instant(2000 + n as i64),
                    WAIT,
                )
                .unwrap();
            assert_eq!(report.basis_t, seed.basis_t + n as u64 + 1);
            assert_eq!(report.tx_data.len(), 3);
            assert_eq!(
                report.db_after.values(entity, SCORE).unwrap(),
                vec![Value::Long(value)]
            );
            last_basis = report.basis_t;
            drop(report);
            if n < SMALL_WRITES / 2 {
                first_half_ns += started.elapsed().as_nanos();
            } else {
                second_half_ns += started.elapsed().as_nanos();
            }
        }
    }
    let writes_complete_us = writes_started.elapsed().as_micros();
    let writes_io = writes.snapshot();
    let indexed = wait_index(&client, last_basis);
    let writes_and_index_us = writes_started.elapsed().as_micros();
    assert_eq!(indexed.total_transactions, 0);
    assert!(indexed.jobs_completed > seeded.jobs_completed);
    let sync_context = OperationContext::new(OperationKind::PeerObservation);
    let sync_started = Instant::now();
    let current = {
        let _scope = sync_context.enter();
        peer.sync_database_value().unwrap()
    };
    let sync_us = sync_started.elapsed().as_micros();
    let sync_io = sync_context.snapshot();
    assert_eq!(current.basis_t(), last_basis);
    assert_eq!(peer.durable_base_t(), last_basis);
    assert_eq!(
        current.values(entity, SCORE).unwrap(),
        vec![Value::Long(10_000 + SMALL_WRITES as i64 - 1)]
    );
    assert_eq!(
        seed.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(1024)]
    );
    assert_eq!(
        captured.values(entity, SCORE).unwrap(),
        vec![Value::Long(1024)]
    );

    // Warm exactly the reused immutable value. No sync, snapshot reopening,
    // or report reconstruction is hidden inside this interval.
    assert_eq!(
        current.values(probe, SCORE).unwrap(),
        vec![Value::Long(1536)]
    );
    let hits_before = peer.cache_stats().hits;
    let warm = OperationContext::new(OperationKind::Query);
    let warm_started = Instant::now();
    {
        let _scope = warm.enter();
        for _ in 0..32 {
            assert_eq!(
                current.values(probe, SCORE).unwrap(),
                vec![Value::Long(1536)]
            );
        }
    }
    let warm_us = warm_started.elapsed().as_micros();
    let warm_io = warm.snapshot();
    assert_eq!(
        warm_io.calls, 0,
        "warm immutable reads must not access storage"
    );
    assert_eq!(warm_io.known_payload_read_bytes, 0);
    assert_eq!(warm_io.known_payload_write_bytes, 0);
    let cache = peer.cache_stats();
    assert!(cache.hits > hits_before);
    assert!(cache.peak_bytes <= CACHE_BYTES);
    let residency = service.writer_residency_stats();
    assert_eq!(residency.eager_database_values, 0);
    assert!(residency.tree_cache_bytes <= CACHE_BYTES);
    assert_eq!(residency.recent_datoms, 0);
    service.shutdown();
    let complete_us = complete_started.elapsed().as_micros();
    let end_io = process_sql_stats();
    let prep_before = lane(&start_io, OperationKind::Indexing);
    let prep_after = lane(&end_io, OperationKind::Indexing);
    let maintenance_before = lane(&start_io, OperationKind::WriterMaintenance);
    let maintenance_after = lane(&end_io, OperationKind::WriterMaintenance);
    eprintln!(
        "BLOCK_LIVE_COST entities={ENTITIES} scalar_payload_bytes={payload_bytes} cache_config_bytes={CACHE_BYTES} peer_cache_peak_accounted_bytes={} writer_cache_current_accounted_bytes={} startup_capture_us={startup_capture_us} seed_commit_us={seed_commit_us} seed_commit_plus_auto_index_us={seed_commit_and_index_us} small_writes={SMALL_WRITES} writes_complete_us={writes_complete_us} writes_plus_auto_index_us={writes_and_index_us} first12_complete_ns={first_half_ns} second12_complete_ns={second_half_ns} write_sql_calls={} write_read_payload_bytes={} write_payload_bytes={} peer_sync_us={sync_us} peer_sync_sql_calls={} peer_sync_payload_bytes={} auto_jobs={} preparation_lane_sql_calls={} preparation_lane_sql_ns={} adoption_and_other_maintenance_sql_calls={} adoption_and_other_maintenance_sql_ns={} warm_reads=32 warm_us={warm_us} warm_sql_calls={} warm_payload_bytes={} complete_startup_through_shutdown_us={complete_us} complete_sql_calls={} complete_read_payload_bytes={} complete_write_payload_bytes={}",
        cache.peak_bytes,
        residency.tree_cache_bytes,
        writes_io.sql_calls,
        writes_io.known_payload_read_bytes,
        writes_io.known_payload_write_bytes,
        sync_io.sql_calls,
        sync_io.known_payload_read_bytes,
        indexed.jobs_completed,
        delta(prep_after.0, prep_before.0),
        delta(prep_after.1, prep_before.1),
        delta(maintenance_after.0, maintenance_before.0),
        delta(maintenance_after.1, maintenance_before.1),
        warm_io.sql_calls,
        warm_io.known_payload_read_bytes,
        delta(end_io.sql_calls, start_io.sql_calls),
        delta(
            end_io.known_payload_read_bytes,
            start_io.known_payload_read_bytes
        ),
        delta(
            end_io.known_payload_write_bytes,
            start_io.known_payload_write_bytes
        ),
    );
    // Timers are reported, not brittle performance thresholds. Cache accounts
    // are not process RSS; retained exact reports/root metadata are separate.
    // Background SQL duration is not preparation/adoption CPU wall duration.
    // Deferred peer-driver destruction and fixture teardown are outside them.
}
