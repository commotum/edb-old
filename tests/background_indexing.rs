use atomic_core::{
    Attribute, BackgroundIndexingConfig, BackgroundIndexingStats, Cardinality, EntityRef,
    ErrorCategory, Keyword, PostgresIndexer, PostgresStore, Schema, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp, TxValue, USER_PARTITION, Value, ValueType,
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

fn corrupt_latest_manifest_payload(client: &mut impl GenericClient, database_id: &str) -> Vec<u8> {
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
        6,
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

fn publish_corrupt_manifest(client: &mut Client, database_id: &str) -> (u64, u64, [u8; 32]) {
    let mut transaction = client.transaction().unwrap();
    let authoritative = transaction
        .query_one(
            "SELECT p.publication_revision, m.basis_t, m.index_basis_t, m.tx_hash, m.state_hash, \
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
    let index_basis_t: i64 = authoritative.get(2);
    let tx_hash: Vec<u8> = authoritative.get(3);
    let state_hash: Vec<u8> = authoritative.get(4);
    let generation: i64 = authoritative.get(5);
    let eidx_frontier: i64 = authoritative.get(6);
    let source_manifest_hash: Vec<u8> = authoritative.get(7);
    let log_generation: i64 = authoritative.get(8);
    let lineage_id: Option<String> = authoritative.get(9);
    let poison_payload = corrupt_latest_manifest_payload(&mut transaction, database_id);
    let poison_hash = sha256(&poison_payload);
    transaction
        .execute(
            "INSERT INTO atomic_tree_manifests \
               (database_id, publication_revision, basis_t, index_basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                log_generation, lineage_id) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 6, $9, $10, $11, $12)",
            &[
                &database_id,
                &poison_revision,
                &basis_t,
                &index_basis_t,
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
        poison_hash,
    )
}

fn assert_incremental_publication(
    client: &mut Client,
    database_id: &str,
    publication_revision: u64,
    predecessor_manifest_hash: [u8; 32],
) {
    let row = client
        .query_one(
            "SELECT state.delta_mode, state.predecessor_manifest_hash \
               FROM atomic_tree_publications publication \
               JOIN atomic_tree_publication_states state \
                 ON state.manifest_hash = publication.manifest_hash \
              WHERE publication.database_id = $1 \
                AND publication.publication_revision = $2",
            &[&database_id, &(publication_revision as i64)],
        )
        .unwrap();
    assert_eq!(row.get::<_, i16>(0), 2, "background work rebuilt in full");
    assert_eq!(
        row.get::<_, Option<Vec<u8>>>(1).as_deref(),
        Some(predecessor_manifest_hash.as_slice()),
        "background delta did not name the immediate physical predecessor"
    );
}

fn corrupt_latest_manifest_in_place(client: &mut Client, database_id: &str) -> (u64, [u8; 32]) {
    let row = client
        .query_one(
            "SELECT p.publication_revision, p.manifest_hash, m.payload \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m ON m.manifest_hash = p.manifest_hash \
              WHERE p.database_id = $1 \
              ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap();
    let revision = u64::try_from(row.get::<_, i64>(0)).unwrap();
    let manifest_hash: [u8; 32] = row.get::<_, Vec<u8>>(1).try_into().unwrap();
    let mut payload: Vec<u8> = row.get(2);
    let last = payload.len() - 1;
    payload[last] ^= 1;
    client
        .batch_execute("SET session_replication_role = replica")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_tree_manifests SET payload = $1 WHERE manifest_hash = $2",
            &[&payload, &&manifest_hash[..]],
        )
        .unwrap();
    client
        .batch_execute("SET session_replication_role = origin")
        .unwrap();
    (revision, manifest_hash)
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
            && stats.published_revision == stats.newest_observed_revision
            && stats.pending_avet_projections == 0
            && stats.jobs_completed >= 1
            && !stats.job_in_flight
            && stats.total_bytes == 0
    });
    assert_eq!(first.basis_t, 1);
    assert_eq!(indexed.jobs_started, indexed.jobs_completed);
    assert_eq!(indexed.jobs_failed, 0);
    service.shutdown();
}

#[test]
fn incomplete_live_fold_is_finished_before_the_bounded_tail_merge() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_incomplete_fold");
    setup(&connection, &database_id);

    // Build enough immutable paths that the publication's fixed 512-node
    // post-commit fold cannot finish in the publishing call itself.
    let seed_service = TransactionService::start(service_config(
        &connection,
        &database_id,
        "background-incomplete-seed",
    ))
    .unwrap();
    let operations = (0..1_024)
        .map(|ordinal| TxOp::Add {
            entity: EntityRef::Temp(format!("seed-{ordinal}")),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(ordinal)),
        })
        .collect::<Vec<_>>();
    seed_service
        .client()
        .transact(
            TransactionRequest::new("background-incomplete-populate", operations),
            Duration::from_secs(10),
        )
        .unwrap();
    seed_service.shutdown();

    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(8)
        .unwrap();
    let large = indexer.consolidate().unwrap();
    assert!(large.segment_count > 500, "witness tree is not large");
    let mut sql = Client::connect(&connection, NoTls).unwrap();
    let fold = sql
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_tree_delta_headers \
                             WHERE manifest_hash = $1 AND delta_state = 2), \
                    EXISTS (SELECT 1 FROM atomic_tree_live_sets \
                             WHERE database_id = $2 AND manifest_hash = $1 AND complete)",
            &[&&large.manifest_hash[..], &database_id],
        )
        .unwrap();
    assert!(fold.get::<_, bool>(0), "large fold unexpectedly finished");
    assert!(!fold.get::<_, bool>(1));

    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "background-incomplete-merge"),
        indexing_config(1024 * 1024),
    )
    .unwrap();
    let committed = service
        .client()
        .transact(
            request("background-incomplete-tail", 9),
            Duration::from_secs(5),
        )
        .unwrap();
    let indexed = wait_for_stats(&service, |stats| {
        stats.published_basis_t == committed.basis_t
            && stats.published_revision > large.publication_revision
            && stats.jobs_completed >= 2
            && !stats.job_in_flight
            && stats.total_bytes == 0
    });
    assert_eq!(indexed.published_revision, large.publication_revision + 1);
    assert_eq!(indexed.jobs_failed, 0);
    assert_incremental_publication(
        &mut sql,
        &database_id,
        indexed.published_revision,
        large.manifest_hash,
    );
    let old_work: i64 = sql
        .query_one(
            "SELECT count(*) FROM atomic_tree_delta_headers WHERE manifest_hash = $1",
            &[&&large.manifest_hash[..]],
        )
        .unwrap()
        .get(0);
    assert_eq!(old_work, 0, "predecessor fold was not sealed");
    assert!(service.client().is_available());
    service.shutdown();
}

#[test]
fn finishing_a_multibatch_publication_does_not_index_a_new_subthreshold_tail() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_finite_demand");
    setup(&connection, &database_id);
    let seed = TransactionService::start(service_config(
        &connection,
        &database_id,
        "finite-demand-seed",
    ))
    .unwrap();
    seed.client()
        .transact(
            TransactionRequest::new(
                "finite-demand-populate",
                (0..1_024)
                    .map(|ordinal| TxOp::Add {
                        entity: EntityRef::Temp(format!("seed-{ordinal}")),
                        attribute: ITEM_COUNT,
                        value: Value::Long(ordinal).into(),
                    })
                    .collect(),
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    seed.shutdown();
    let large = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(8)
        .unwrap()
        .consolidate()
        .unwrap();
    assert!(large.segment_count > 512);

    let mut sql = Client::connect(&connection, NoTls).unwrap();
    let mut pause = sql.transaction().unwrap();
    let pending_nodes: i64 = pause
        .query_one(
            "SELECT count(*) FROM atomic_tree_delta_nodes WHERE manifest_hash = $1",
            &[&&large.manifest_hash[..]],
        )
        .unwrap()
        .get(0);
    assert!(
        pending_nodes > 0,
        "publishing consumed the first 512-node fold batch"
    );
    // Hold just this publication's continuation row. Native activation and
    // commits remain available, but the worker cannot finish its next batch
    // until a below-threshold transaction has committed after selection.
    pause
        .query_one(
            "SELECT delta_state FROM atomic_tree_delta_headers \
             WHERE manifest_hash = $1 AND delta_state = 2 FOR UPDATE",
            &[&&large.manifest_hash[..]],
        )
        .unwrap();
    let limits = BackgroundIndexingConfig {
        memory_index_threshold_bytes: 16 * 1024,
        memory_index_max_bytes: 1024 * 1024,
    };
    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "finite-demand-resume"),
        limits,
    )
    .unwrap();
    wait_for_stats(&service, |stats| stats.job_in_flight);
    let tail = service
        .client()
        .transact(
            request("finite-demand-small-tail", 9),
            Duration::from_secs(5),
        )
        .unwrap();
    assert!(tail.basis_t > large.basis_t);
    let during_fold = service.background_indexing_stats();
    assert!(during_fold.total_bytes > 0);
    assert!(during_fold.total_bytes < limits.memory_index_threshold_bytes);
    pause.commit().unwrap();
    let finished = wait_for_stats(&service, |stats| {
        stats.jobs_completed >= 1 && !stats.job_in_flight
    });
    assert_eq!(finished.jobs_started, 1);
    assert_eq!(finished.jobs_completed, 1);
    assert_eq!(finished.published_revision, large.publication_revision);
    assert_eq!(finished.published_basis_t, large.basis_t);
    assert_eq!(finished.target_basis_t, tail.basis_t);
    assert_eq!(finished.total_transactions, 1);
    assert_eq!(finished.jobs_failed, 0);
    let sealed: bool = sql
        .query_one(
            "SELECT complete FROM atomic_tree_live_sets \
             WHERE database_id = $1 AND manifest_hash = $2",
            &[&database_id, &&large.manifest_hash[..]],
        )
        .unwrap()
        .get(0);
    assert!(sealed);
    for value in 10..13 {
        service
            .client()
            .transact(
                request(&format!("finite-demand-small-{value}"), value),
                Duration::from_secs(5),
            )
            .unwrap();
    }
    assert_eq!(service.background_indexing_stats().jobs_started, 1);
    service.shutdown();

    // Restart must neither forget retained novelty nor reinterpret a sealed
    // publication as an unconditional request to merge that small tail.
    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "finite-demand-second-resume"),
        limits,
    )
    .unwrap();
    let restarted = service.background_indexing_stats();
    assert_eq!(restarted.total_transactions, 4);
    assert_eq!(restarted.jobs_started, 0);
    assert_eq!(restarted.published_revision, large.publication_revision);
    let crossing = service
        .client()
        .transact(
            TransactionRequest::new(
                "finite-demand-cross-threshold",
                (0..128)
                    .map(|ordinal| TxOp::Add {
                        entity: EntityRef::Temp(format!("cross-{ordinal}")),
                        attribute: ITEM_COUNT,
                        value: Value::Long(ordinal).into(),
                    })
                    .collect(),
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    let indexed = wait_for_stats(&service, |stats| {
        stats.published_basis_t == crossing.basis_t && !stats.job_in_flight
    });
    assert_eq!(indexed.published_revision, large.publication_revision + 1);
    assert_eq!(indexed.total_bytes, 0);
    assert_eq!(indexed.jobs_failed, 0);
    assert_eq!(
        PostgresStore::connect(&connection)
            .unwrap()
            .recover(&database_id)
            .unwrap()
            .basis_t(),
        crossing.basis_t
    );
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
    let (poison_basis, poison_revision, poison_hash) =
        publish_corrupt_manifest(&mut sql, &database_id);
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
            && stats.pending_avet_projections == 0
            && stats.jobs_completed >= 1
            && !stats.job_in_flight
            && stats.total_transactions == 0
            && stats.total_bytes == 0
    });
    assert_eq!(repaired.published_revision, poison_revision + 1);
    assert_eq!(repaired.target_basis_t, initial_basis);
    assert_eq!(repaired.jobs_started, repaired.jobs_completed);
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
    assert_incremental_publication(
        &mut sql,
        &database_id,
        repaired.published_revision,
        poison_hash,
    );
}

#[test]
fn divergent_corrupt_root_requires_explicit_administrative_rebuild() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_divergent_corrupt");
    setup(&connection, &database_id);
    let seed_service = TransactionService::start(service_config(
        &connection,
        &database_id,
        "background-divergent-seed",
    ))
    .unwrap();
    seed_service
        .client()
        .transact(
            request("background-divergent-value", 41),
            Duration::from_secs(5),
        )
        .unwrap();
    seed_service.shutdown();

    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    let divergent = indexer.consolidate().unwrap();
    let mut sql = Client::connect(&connection, NoTls).unwrap();
    let (corrupt_revision, _corrupt_hash) =
        corrupt_latest_manifest_in_place(&mut sql, &database_id);
    assert_eq!(corrupt_revision, divergent.publication_revision);

    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "background-divergent-bounded"),
        indexing_config(1024 * 1024),
    )
    .unwrap();
    let failed = wait_for_stats(&service, |stats| stats.jobs_failed == 1);
    let failure = failed
        .last_failure
        .expect("bounded background refusal was not observable");
    assert_eq!(failure.category, ErrorCategory::Unavailable);
    assert_eq!(failure.code, "index/background-rebuild-required");
    assert!(!service.client().is_available());
    let latest_after_failure: i64 = sql
        .query_one(
            "SELECT max(publication_revision) FROM atomic_tree_publications \
              WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        u64::try_from(latest_after_failure).unwrap(),
        corrupt_revision
    );
    service.shutdown();

    // The broad recovery path remains an explicit operator decision.
    let repaired = indexer.consolidate().unwrap();
    assert_eq!(repaired.publication_revision, corrupt_revision + 1);
    let mode: i16 = sql
        .query_one(
            "SELECT state.delta_mode FROM atomic_tree_publications publication \
               JOIN atomic_tree_publication_states state \
                 ON state.manifest_hash = publication.manifest_hash \
              WHERE publication.database_id = $1 \
                AND publication.publication_revision = $2",
            &[&database_id, &(repaired.publication_revision as i64)],
        )
        .unwrap()
        .get(0);
    assert_eq!(mode, 1, "administrative repair was not a replacement");
    let restarted = TransactionService::start(service_config(
        &connection,
        &database_id,
        "background-divergent-restarted",
    ))
    .unwrap();
    assert!(restarted.client().is_available());
    restarted.shutdown();
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
fn lease_loss_settles_hard_limit_parked_and_queued_work_as_definitely_unavailable() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("background_index_lease_loss");
    setup(&connection, &database_id);
    let service = TransactionService::start_with_indexing(
        service_config(&connection, &database_id, "background-lease-loss"),
        indexing_config(1),
    )
    .unwrap();
    let client = service.client();

    // Freeze publication so one committed transaction crosses the strict hard
    // limit and the next request is retained locally without assessment.
    let mut blocker = Client::connect(&connection, NoTls).unwrap();
    let mut publication_lock = blocker.transaction().unwrap();
    publication_lock
        .batch_execute("LOCK TABLE atomic_tree_publications IN SHARE MODE")
        .unwrap();
    let committed = client
        .transact(request("lease-loss-committed", 1), Duration::from_secs(2))
        .unwrap();
    wait_for_stats(&service, |stats| {
        stats.job_in_flight && stats.total_bytes > 1
    });
    let parked = client.submit(request("lease-loss-parked", 2)).unwrap();
    wait_for_stats(&service, |stats| stats.backpressure_stalls == 1);
    let queued = client.submit(request("lease-loss-queued", 3)).unwrap();

    // Fence the active epoch before its next renewal. Neither retained request
    // has reached Database::with or the log, so UnknownOutcome would be false.
    let mut fence = Client::connect(&connection, NoTls).unwrap();
    assert_eq!(
        fence
            .execute(
                "UPDATE atomic_transactor_leases \
                    SET holder_id = 'fenced-by-test', epoch = epoch + 1, \
                        expires_at = clock_timestamp() \
                  WHERE lease_scope = $1 AND holder_id = 'background-lease-loss'",
                &[&database_id],
            )
            .unwrap(),
        1
    );

    for ticket in [parked, queued] {
        let error = ticket.wait(Duration::from_secs(2)).unwrap_err();
        assert_eq!(
            (error.category, error.code),
            (ErrorCategory::Unavailable, "service/unavailable")
        );
    }
    assert!(!client.is_available());
    assert_eq!(client.stats().queued, 0);

    publication_lock.commit().unwrap();
    service.shutdown();
    assert_eq!(
        PostgresStore::connect(&connection)
            .unwrap()
            .recover(&database_id)
            .unwrap()
            .basis_t(),
        committed.basis_t,
        "parked work must remain absent from the authoritative log"
    );
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
                    m.log_generation, m.lineage_id, m.index_basis_t \
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
    let source_index_basis_t: i64 = source.get(4);
    assert_eq!(
        u64::try_from(current_revision).unwrap(),
        initial.published_revision
    );
    // Preserve the current v6 envelope header and corrupt its checksum. This
    // remains a hash-named immutable value at SQL level while the canonical
    // decoder correctly rejects it.
    let poison_payload = corrupt_latest_manifest_payload(&mut poison, &database_id);
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
               (database_id, publication_revision, basis_t, index_basis_t, tx_hash, state_hash, \
                excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                log_generation, lineage_id) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, 6, $9, $10, $11, $12)",
            &[
                &database_id,
                &poison_revision,
                &(committed.basis_t as i64),
                &source_index_basis_t,
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
    let mut evidence = Client::connect(&connection, NoTls).unwrap();
    assert_incremental_publication(
        &mut evidence,
        &database_id,
        repaired.published_revision,
        poison_hash,
    );

    let after_race = client
        .transact(request("race-after", 8), Duration::from_secs(2))
        .unwrap();
    assert_eq!(
        client.writer_residency_stats().publication_revision,
        repaired.published_revision,
        "the writer did not adopt the authenticated repaired root before assessing new work"
    );
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
