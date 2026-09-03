use atomic_core::{
    Attribute, BackgroundIndexingConfig, BackgroundIndexingStats, Cardinality, EntityRef,
    ErrorCategory, Keyword, PostgresStore, Schema, TransactionRequest, TransactionService,
    TransactionServiceConfig, TxOp, TxValue, USER_PARTITION, Value, ValueType, decode_transaction,
    make_eid, sha256,
};
use postgres::{Client, GenericClient, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const ITEM_COUNT: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn setup(connection: &str, database_id: &str) -> u64 {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let mut migrator = atomic_core::PostgresMigrator::connect(connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(connection).unwrap();
    store
        .create_database(database_id, schema)
        .unwrap()
        .basis_t()
}

fn setup_empty(connection: &str, database_id: &str) {
    let mut migrator = atomic_core::PostgresMigrator::connect(connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(connection).unwrap();
    assert_eq!(
        store
            .create_database(database_id, Schema::new())
            .unwrap()
            .basis_t(),
        0
    );
}

fn corrupt_latest_v4_manifest_payload(
    client: &mut impl GenericClient,
    database_id: &str,
) -> Vec<u8> {
    let mut payload: Vec<u8> = client
        .query_one(
            "SELECT m.payload FROM atomic_tree_publications p \
             JOIN atomic_tree_manifests m \
               ON m.database_id = p.database_id \
              AND m.publication_revision = p.publication_revision \
              AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
              AND m.manifest_hash = p.manifest_hash \
             WHERE p.database_id = $1 \
             ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(payload.get(..4), Some(b"ATIM".as_slice()));
    assert_eq!(
        u16::from_be_bytes(payload[4..6].try_into().unwrap()),
        4,
        "poison fixture must track the current canonical manifest version"
    );
    let checksum_byte = payload.len() - 1;
    payload[checksum_byte] ^= 1;
    payload
}

fn publish_corrupt_v4_manifest(client: &mut Client, database_id: &str) -> (u64, u64) {
    let mut transaction = client.transaction().unwrap();
    let authoritative = transaction
        .query_one(
            "SELECT p.publication_revision, m.basis_t, m.tx_hash, m.state_hash, \
                    m.excision_generation, m.eidx_frontier, m.manifest_hash \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m \
                 ON m.database_id = p.database_id \
                AND m.publication_revision = p.publication_revision \
                AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
                AND m.manifest_hash = p.manifest_hash \
              WHERE p.database_id = $1 \
              ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap();
    let current_revision: i64 = authoritative.get(0);
    let poison_revision = current_revision.checked_add(1).unwrap();
    let basis_t: i64 = authoritative.get(1);
    let tx_hash: Vec<u8> = authoritative.get(2);
    let state_hash: Vec<u8> = authoritative.get(3);
    let generation: i64 = authoritative.get(4);
    let eidx_frontier: i64 = authoritative.get(5);
    let source_manifest_hash: Vec<u8> = authoritative.get(6);
    let poison_payload = corrupt_latest_v4_manifest_payload(&mut transaction, database_id);
    let poison_hash = sha256(&poison_payload);
    transaction
        .execute(
            "INSERT INTO atomic_tree_manifests \
               (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, 4, $8, $9)",
            &[
                &database_id,
                &poison_revision,
                &basis_t,
                &tx_hash,
                &state_hash,
                &generation,
                &eidx_frontier,
                &&poison_hash[..],
                &poison_payload,
            ],
        )
        .unwrap();
    let copied = transaction
        .execute(
            "INSERT INTO atomic_tree_manifest_roots \
               (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
             SELECT $1, index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $2",
            &[&&poison_hash[..], &source_manifest_hash],
        )
        .unwrap();
    assert_eq!(copied, 8);
    transaction
        .execute(
            "INSERT INTO atomic_tree_delta_headers \
                   (manifest_hash, predecessor_manifest_hash, delta_mode) \
             VALUES ($1, $2, 0)",
            &[&&poison_hash[..], &source_manifest_hash],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_tree_publications \
               (database_id, publication_revision, basis_t, tx_hash, manifest_hash) \
             VALUES ($1, $2, $3, $4, $5)",
            &[
                &database_id,
                &poison_revision,
                &basis_t,
                &tx_hash,
                &&poison_hash[..],
            ],
        )
        .unwrap();
    transaction.commit().unwrap();
    (
        u64::try_from(basis_t).unwrap(),
        u64::try_from(poison_revision).unwrap(),
    )
}

fn service_config(connection: &str, database_id: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: holder.to_owned(),
        lease_duration: Duration::from_secs(4),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 2,
        capacity_limits: atomic_core::CapacityLimits::default(),
    }
}

fn indexing_config(max: u64) -> BackgroundIndexingConfig {
    BackgroundIndexingConfig {
        memory_index_threshold_bytes: 1,
        memory_index_max_bytes: max,
    }
}

fn request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Id(make_eid(USER_PARTITION, 42).unwrap()),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(value)),
        }],
    )
}

fn wait_for_stats(
    service: &TransactionService,
    predicate: impl Fn(&BackgroundIndexingStats) -> bool,
) -> BackgroundIndexingStats {
    let deadline = Instant::now() + Duration::from_secs(10);
    loop {
        let stats = service.background_indexing_stats();
        if predicate(&stats) {
            return stats;
        }
        assert!(
            Instant::now() < deadline,
            "background index condition timed out: {stats:?}"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
}

#[test]
fn default_service_start_creates_the_initial_native_publication() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_default");
    let initial_basis = setup(&connection, &database_id);
    let service = TransactionService::start(service_config(
        &connection,
        &database_id,
        "background-default",
    ))
    .unwrap();
    let indexed = wait_for_stats(&service, |stats| {
        stats.published_basis_t == initial_basis && stats.jobs_completed == 1
    });
    assert_eq!(indexed.total_bytes, 0);
    assert_eq!(indexed.jobs_failed, 0);
    service.shutdown();
}

#[test]
fn empty_database_waits_for_first_commit_before_initial_publication() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_empty");
    setup_empty(&connection, &database_id);
    let service = TransactionService::start(service_config(
        &connection,
        &database_id,
        "background-empty",
    ))
    .unwrap();

    // Give the worker enough time to expose the old failure mode: attempting
    // to consolidate basis zero used to terminate indexing and close writes.
    let deadline = Instant::now() + Duration::from_millis(250);
    while Instant::now() < deadline {
        let stats = service.background_indexing_stats();
        assert_eq!(stats.jobs_started, 0);
        assert_eq!(stats.jobs_failed, 0);
        assert!(stats.last_failure.is_none());
        std::thread::sleep(Duration::from_millis(10));
    }
    assert!(service.client().is_available());

    let first = service
        .client()
        .transact(
            TransactionRequest::new("empty-first-positive", Vec::new()),
            Duration::from_secs(2),
        )
        .unwrap();
    let indexed = wait_for_stats(&service, |stats| {
        stats.published_basis_t == first.basis_t
            && stats.jobs_completed == 1
            && stats.total_bytes == 0
    });
    assert_eq!(first.basis_t, 1);
    assert_eq!(indexed.jobs_started, 1);
    assert_eq!(indexed.jobs_failed, 0);
    service.shutdown();
}

#[test]
fn restart_repairs_corrupt_latest_manifest_at_the_same_idle_basis() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_corrupt_seed");
    let initial_basis = setup(&connection, &database_id);

    let first_service = TransactionService::start(service_config(
        &connection,
        &database_id,
        "background-corrupt-seed-one",
    ))
    .unwrap();
    let first = wait_for_stats(&first_service, |stats| {
        stats.published_basis_t == initial_basis && stats.jobs_completed == 1
    });
    assert_eq!(first.published_revision, first.newest_observed_revision);
    first_service.shutdown();

    let mut sql = Client::connect(&connection, NoTls).unwrap();
    let (poison_basis, poison_revision) = publish_corrupt_v4_manifest(&mut sql, &database_id);
    assert_eq!(poison_basis, initial_basis);
    assert_eq!(poison_revision, first.published_revision + 1);
    let head_before: i64 = sql
        .query_one(
            "SELECT basis_t FROM atomic_heads WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);

    // No transaction or novelty is required to obtain a fresh physical root
    // coordinate. A one-byte hard limit would have deadlocked the old design;
    // the revision-aware service repairs immediately at the same basis.
    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "background-corrupt-seed-two"),
        indexing_config(1),
    )
    .unwrap();
    let repaired = wait_for_stats(&service, |stats| {
        stats.published_basis_t == initial_basis
            && stats.published_revision > poison_revision
            && stats.published_revision == stats.newest_observed_revision
            && stats.jobs_completed == 1
            && stats.total_transactions == 0
            && stats.total_bytes == 0
    });
    assert_eq!(repaired.published_revision, poison_revision + 1);
    assert_eq!(repaired.target_basis_t, initial_basis);
    assert_eq!(repaired.jobs_started, 1);
    assert_eq!(repaired.jobs_failed, 0);
    assert!(repaired.last_failure.is_none());
    assert!(service.client().is_available());
    service.shutdown();

    let head_after: i64 = sql
        .query_one(
            "SELECT basis_t FROM atomic_heads WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    let latest = sql
        .query_one(
            "SELECT publication_revision, basis_t FROM atomic_tree_publications \
             WHERE database_id = $1 ORDER BY publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap();
    let latest_revision: i64 = latest.get(0);
    let latest_basis: i64 = latest.get(1);
    assert_eq!(head_after, head_before);
    assert_eq!(
        u64::try_from(latest_revision).unwrap(),
        repaired.published_revision
    );
    assert_eq!(u64::try_from(latest_basis).unwrap(), initial_basis);
}

#[test]
fn background_publication_bounds_novelty_without_hiding_committed_replays() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_pressure");
    let initial_basis = setup(&connection, &database_id);
    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "background-pressure"),
        indexing_config(1),
    )
    .unwrap();
    let client = service.client();

    let initial = wait_for_stats(&service, |stats| {
        stats.published_basis_t == initial_basis
            && stats.jobs_completed == 1
            && stats.total_bytes == 0
    });
    assert_eq!(initial.jobs_started, 1);

    // Hold publication, not transaction processing. The index worker must be
    // independently blocked while the fenced writer can commit one bounded
    // transaction and then expose pressure.
    let mut blocker = Client::connect(&connection, NoTls).unwrap();
    let mut lock = blocker.transaction().unwrap();
    lock.batch_execute("LOCK TABLE atomic_tree_manifests IN ACCESS EXCLUSIVE MODE")
        .unwrap();
    let committed = client
        .transact(request("pressure-commit", 1), Duration::from_secs(2))
        .unwrap();
    let pressured = wait_for_stats(&service, |stats| {
        stats.job_in_flight && stats.total_bytes >= 1
    });
    assert_eq!(pressured.target_basis_t, committed.basis_t);
    assert_eq!(pressured.total_transactions, 1);
    assert_eq!(pressured.total_datoms, committed.tx_data.len() as u64);
    assert_eq!(pressured.indexing_transactions, 1);
    assert_eq!(pressured.memory_index_transactions, 0);

    // A durable retry is priority/reconciliation work, not new novelty, and
    // remains available even while the hard pressure gate is closed.
    let replay = client
        .transact(request("pressure-commit", 1), Duration::from_secs(2))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    let busy = client.submit(request("pressure-new", 2)).unwrap_err();
    assert_eq!(
        (busy.category, busy.code),
        (ErrorCategory::Busy, "service/index-backpressure")
    );
    assert_eq!(
        client.background_indexing_stats().backpressure_rejections,
        1
    );

    lock.commit().unwrap();
    let caught_up = wait_for_stats(&service, |stats| {
        stats.published_basis_t >= committed.basis_t
            && stats.jobs_completed >= 2
            && stats.total_bytes == 0
            && !stats.job_in_flight
    });
    assert_eq!(caught_up.jobs_failed, 0);

    let resumed = client
        .transact(request("pressure-resumed", 2), Duration::from_secs(2))
        .unwrap();
    wait_for_stats(&service, |stats| {
        stats.published_basis_t >= resumed.basis_t && stats.total_bytes == 0
    });
    service.shutdown();

    let mut sql = Client::connect(&connection, NoTls).unwrap();
    let published: i64 = sql
        .query_one(
            "SELECT max(basis_t) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(published as u64, resumed.basis_t);
}

#[test]
fn competing_corrupt_revision_is_repaired_without_closing_writes() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_race");
    let initial_basis = setup(&connection, &database_id);
    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "background-race"),
        indexing_config(1024 * 1024),
    )
    .unwrap();
    let client = service.client();
    let initial = wait_for_stats(&service, |stats| {
        stats.published_basis_t == initial_basis && stats.jobs_completed == 1
    });

    // Hold node access so the worker selects the old revision but cannot reach
    // publication. A competing corrupt root then wins the exact next physical
    // coordinate. The indexer must reselect and repair it; this is normal CAS
    // contention, not a reason to close the authoritative transaction writer.
    let mut saboteur = Client::connect(&connection, NoTls).unwrap();
    let mut poison = saboteur.transaction().unwrap();
    poison
        .batch_execute("LOCK TABLE atomic_tree_nodes IN ACCESS EXCLUSIVE MODE")
        .unwrap();
    let committed = client
        .transact(request("race-commit", 7), Duration::from_secs(2))
        .unwrap();
    wait_for_stats(&service, |stats| stats.job_in_flight);
    let row = poison
        .query_one(
            "SELECT tx_hash, state_hash, payload FROM atomic_transactions \
              WHERE database_id = $1 AND basis_t = $2",
            &[&database_id, &(committed.basis_t as i64)],
        )
        .unwrap();
    let tx_hash: Vec<u8> = row.get(0);
    let state_hash: Vec<u8> = row.get(1);
    let payload: Vec<u8> = row.get(2);
    let durable = decode_transaction(&payload).unwrap();
    let source = poison
        .query_one(
            "SELECT publication_revision, manifest_hash \
               FROM atomic_tree_publications WHERE database_id = $1 \
               ORDER BY publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap();
    let current_revision: i64 = source.get(0);
    let poison_revision = current_revision.checked_add(1).unwrap();
    let source_manifest_hash: Vec<u8> = source.get(1);
    assert_eq!(
        u64::try_from(current_revision).unwrap(),
        initial.published_revision
    );
    // Preserve the current v4 envelope header and corrupt its checksum. This
    // remains a hash-named immutable value at SQL level while the canonical
    // decoder correctly rejects it.
    let poison_payload = corrupt_latest_v4_manifest_payload(&mut poison, &database_id);
    let poison_hash = sha256(&poison_payload);
    let generation: i64 = poison
        .query_one(
            "SELECT excision_generation FROM atomic_database_generations \
              WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    poison
        .execute(
            "INSERT INTO atomic_tree_manifests \
               (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, 4, $8, $9)",
            &[
                &database_id,
                &poison_revision,
                &(committed.basis_t as i64),
                &tx_hash,
                &state_hash,
                &generation,
                &(durable.eidx_frontier as i64),
                &&poison_hash[..],
                &poison_payload,
            ],
        )
        .unwrap();
    let copied = poison
        .execute(
            "INSERT INTO atomic_tree_manifest_roots \
               (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
             SELECT $1, index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $2",
            &[&&poison_hash[..], &source_manifest_hash],
        )
        .unwrap();
    assert_eq!(copied, 8);
    poison
        .execute(
            "INSERT INTO atomic_tree_delta_headers \
                   (manifest_hash, predecessor_manifest_hash, delta_mode) \
             VALUES ($1, $2, 0)",
            &[&&poison_hash[..], &source_manifest_hash],
        )
        .unwrap();
    poison
        .execute(
            "INSERT INTO atomic_tree_publications \
               (database_id, publication_revision, basis_t, tx_hash, manifest_hash) \
             VALUES ($1, $2, $3, $4, $5)",
            &[
                &database_id,
                &poison_revision,
                &(committed.basis_t as i64),
                &tx_hash,
                &&poison_hash[..],
            ],
        )
        .unwrap();
    poison.commit().unwrap();

    let repaired = wait_for_stats(&service, |stats| {
        stats.published_basis_t == committed.basis_t
            && stats.published_revision > u64::try_from(poison_revision).unwrap()
            && stats.published_revision == stats.newest_observed_revision
            && stats.jobs_completed == 2
            && stats.total_bytes == 0
    });
    assert_eq!(
        repaired.published_revision,
        u64::try_from(poison_revision).unwrap() + 1
    );
    assert_eq!(repaired.jobs_failed, 0);
    assert!(repaired.last_failure.is_none());
    assert!(client.is_available());

    let after_race = client
        .transact(request("race-after", 8), Duration::from_secs(2))
        .unwrap();
    wait_for_stats(&service, |stats| {
        stats.published_basis_t == after_race.basis_t && stats.total_bytes == 0
    });
    service.shutdown();

    let recovered = PostgresStore::connect(&connection)
        .unwrap()
        .recover(&database_id)
        .unwrap();
    assert_eq!(recovered.basis_t(), after_race.basis_t);
}
