use atomic_core::{
    Attribute, BackgroundIndexingConfig, BackgroundIndexingStats, Cardinality, EntityRef,
    ErrorCategory, Keyword, PostgresStore, Schema, TransactionRequest, TransactionService,
    TransactionServiceConfig, TxOp, TxValue, USER_PARTITION, Value, ValueType, make_eid, sha256,
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

fn stage_zero_delta_publication_work(
    client: &mut impl GenericClient,
    database_id: &str,
    manifest_hash: &[u8; 32],
    predecessor_manifest_hash: &[u8],
    log_generation: i64,
    expected_revision: i64,
) {
    let empty_set_hash = [0_u8; 32];
    client
        .execute(
            "INSERT INTO atomic_tree_build_intents \
               (manifest_hash, database_id, log_generation, expected_revision, \
                expected_node_count, node_set_hash, intent_state) \
             VALUES ($1, $2, $3, $4, 0, $5, 1)",
            &[
                &&manifest_hash[..],
                &database_id,
                &log_generation,
                &expected_revision,
                &&empty_set_hash[..],
            ],
        )
        .unwrap();
    client
        .execute(
            "INSERT INTO atomic_tree_delta_headers \
               (manifest_hash, predecessor_manifest_hash, delta_mode, \
                expected_node_count, staged_node_count, delta_set_hash, \
                added_node_count, added_set_hash, delta_state) \
             VALUES ($1, $2, 2, 0, 0, $3, 0, $3, 1)",
            &[
                &&manifest_hash[..],
                &predecessor_manifest_hash,
                &&empty_set_hash[..],
            ],
        )
        .unwrap();
}

fn finish_zero_delta_publication_work(client: &mut impl GenericClient, manifest_hash: &[u8; 32]) {
    let complete: bool = client
        .query_one(
            "SELECT atomic_apply_tree_publication_work($1, 4096)",
            &[&&manifest_hash[..]],
        )
        .unwrap()
        .get(0);
    assert!(
        complete,
        "zero-delta publication work must seal in one batch"
    );
}

fn publish_corrupt_v4_manifest(client: &mut Client, database_id: &str) -> (u64, u64) {
    let mut transaction = client.transaction().unwrap();
    let authoritative = transaction
        .query_one(
            "SELECT p.publication_revision, m.basis_t, m.tx_hash, m.state_hash, \
                    m.excision_generation, m.eidx_frontier, m.manifest_hash, \
                    m.log_generation, m.lineage_id \
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
    let log_generation: i64 = authoritative.get(7);
    let lineage_id: Option<String> = authoritative.get(8);
    let poison_payload = corrupt_latest_v4_manifest_payload(&mut transaction, database_id);
    let poison_hash = sha256(&poison_payload);
    transaction
        .execute(
            "INSERT INTO atomic_tree_manifests \
               (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                log_generation, lineage_id) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, 4, $8, $9, $10, $11)",
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
                &log_generation,
                &lineage_id,
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
    stage_zero_delta_publication_work(
        &mut transaction,
        database_id,
        &poison_hash,
        &source_manifest_hash,
        log_generation,
        current_revision,
    );
    transaction
        .execute(
            "INSERT INTO atomic_tree_publications \
               (database_id, publication_revision, basis_t, tx_hash, manifest_hash, \
                log_generation) \
             VALUES ($1, $2, $3, $4, $5, $6)",
            &[
                &database_id,
                &poison_revision,
                &basis_t,
                &tx_hash,
                &&poison_hash[..],
                &log_generation,
            ],
        )
        .unwrap();
    finish_zero_delta_publication_work(&mut transaction, &poison_hash);
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
fn default_service_adopts_the_creation_publication_without_rebuilding_it() {
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
        stats.published_basis_t == initial_basis && stats.jobs_completed == 0
    });
    assert_eq!(indexed.total_bytes, 0);
    assert_eq!(indexed.jobs_failed, 0);
    service.shutdown();
}

#[test]
fn basis_zero_is_published_at_creation_and_first_novelty_advances_it() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_empty");
    setup_empty(&connection, &database_id);
    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "background-empty"),
        indexing_config(1024 * 1024),
    )
    .unwrap();

    // Creation publishes the canonical basis-zero value directly. Starting a
    // writer adopts that exact authority and must not manufacture another
    // physical revision merely because no positive transaction exists yet.
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
fn restart_repairs_over_a_corrupt_latest_manifest_from_an_older_valid_base() {
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
        stats.published_basis_t == initial_basis && stats.jobs_completed == 0
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

    // Hold only publication writes. Plain reads (including exact activation
    // and request-base validation) continue, while the background repair
    // cannot race past the seed observation below.
    let mut blocker = Client::connect(&connection, NoTls).unwrap();
    let mut publication_lock = blocker.transaction().unwrap();
    publication_lock
        .batch_execute("LOCK TABLE atomic_tree_publications IN SHARE MODE")
        .unwrap();

    // A corrupt derived revision is not treated as authoritative data and is
    // not mistaken for absence. The older authenticated immutable base plus
    // authoritative log endpoint remains sufficient to rebuild a newer valid
    // revision at the same logical basis. If every candidate were corrupt,
    // strict activation would fail instead.
    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "background-corrupt-seed-two"),
        indexing_config(1),
    )
    .unwrap();
    let pinned_seed = wait_for_stats(&service, |stats| stats.job_in_flight);
    assert_eq!(pinned_seed.published_revision, first.published_revision);
    assert_eq!(pinned_seed.newest_observed_revision, poison_revision);
    assert_eq!(pinned_seed.published_basis_t, initial_basis);
    assert_eq!(pinned_seed.target_basis_t, initial_basis);
    publication_lock.commit().unwrap();
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
            && stats.jobs_completed == 0
            && stats.total_bytes == 0
    });
    assert_eq!(initial.jobs_started, 0);

    // Hold publication, not transaction processing. The index worker must be
    // independently blocked while the fenced writer can commit one bounded
    // transaction and then expose pressure.
    let mut blocker = Client::connect(&connection, NoTls).unwrap();
    let mut lock = blocker.transaction().unwrap();
    lock.batch_execute("LOCK TABLE atomic_tree_publications IN SHARE MODE")
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
    let parked = client.submit(request("pressure-new", 2)).unwrap();
    wait_for_stats(&service, |stats| stats.backpressure_stalls == 1);
    let queued_one = client.submit(request("pressure-queued-one", 3)).unwrap();
    let queued_two = client.submit(request("pressure-queued-two", 4)).unwrap();
    let busy = client
        .submit(request("pressure-over-capacity", 5))
        .unwrap_err();
    assert_eq!(
        (busy.category, busy.code),
        (ErrorCategory::Busy, "service/queue-full")
    );
    assert_eq!(
        client.background_indexing_stats().backpressure_rejections,
        1
    );

    lock.commit().unwrap();
    let parked = parked.wait(Duration::from_secs(5)).unwrap();
    let queued_one = queued_one.wait(Duration::from_secs(5)).unwrap();
    let queued_two = queued_two.wait(Duration::from_secs(5)).unwrap();
    assert_eq!(parked.basis_t, committed.basis_t + 1);
    assert_eq!(queued_one.basis_t, parked.basis_t + 1);
    assert_eq!(queued_two.basis_t, queued_one.basis_t + 1);
    let caught_up = wait_for_stats(&service, |stats| {
        stats.published_basis_t >= queued_two.basis_t
            && stats.jobs_completed >= 2
            && stats.total_bytes == 0
            && !stats.job_in_flight
    });
    assert_eq!(caught_up.jobs_failed, 0);

    let resumed = client
        .transact(request("pressure-resumed", 6), Duration::from_secs(2))
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
        stats.published_basis_t == initial_basis && stats.jobs_completed == 0
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
            "SELECT tx_hash, state_hash, eidx_frontier \
               FROM atomic_generation_transactions \
              WHERE database_id = $1 AND basis_t = $2 \
                AND generation = (SELECT log_generation FROM atomic_heads \
                                   WHERE database_id = $1)",
            &[&database_id, &(committed.basis_t as i64)],
        )
        .unwrap();
    let tx_hash: Vec<u8> = row.get(0);
    let state_hash: Vec<u8> = row.get(1);
    let eidx_frontier: i64 = row.get(2);
    let source = poison
        .query_one(
            "SELECT p.publication_revision, p.manifest_hash, \
                    m.log_generation, m.lineage_id \
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
    let current_revision: i64 = source.get(0);
    let poison_revision = current_revision.checked_add(1).unwrap();
    let source_manifest_hash: Vec<u8> = source.get(1);
    let log_generation: i64 = source.get(2);
    let lineage_id: Option<String> = source.get(3);
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
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                log_generation, lineage_id) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, 4, $8, $9, $10, $11)",
            &[
                &database_id,
                &poison_revision,
                &(committed.basis_t as i64),
                &tx_hash,
                &state_hash,
                &generation,
                &eidx_frontier,
                &&poison_hash[..],
                &poison_payload,
                &log_generation,
                &lineage_id,
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
    stage_zero_delta_publication_work(
        &mut poison,
        &database_id,
        &poison_hash,
        &source_manifest_hash,
        log_generation,
        current_revision,
    );
    poison
        .execute(
            "INSERT INTO atomic_tree_publications \
               (database_id, publication_revision, basis_t, tx_hash, manifest_hash, \
                log_generation) \
             VALUES ($1, $2, $3, $4, $5, $6)",
            &[
                &database_id,
                &poison_revision,
                &(committed.basis_t as i64),
                &tx_hash,
                &&poison_hash[..],
                &log_generation,
            ],
        )
        .unwrap();
    finish_zero_delta_publication_work(&mut poison, &poison_hash);
    poison.commit().unwrap();

    let repaired = wait_for_stats(&service, |stats| {
            stats.published_basis_t == committed.basis_t
            && stats.published_revision > u64::try_from(poison_revision).unwrap()
            && stats.published_revision == stats.newest_observed_revision
            && stats.jobs_completed == 1
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
