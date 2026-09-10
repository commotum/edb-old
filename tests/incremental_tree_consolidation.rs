use atomic_core::{
    Attribute, Cardinality, DB_ALTER_ATTRIBUTE, Digest, EntityRef, IndexBuildFault,
    IndexBuildReceipt, IndexOrder, IndexPrefix, Keyword, Peer, PersistentTreeManifest,
    PostgresIndexer, PostgresStore, PostgresTreeStore, Schema, TxOp, TxValue, Value, ValueType,
    View, t_to_tx,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

mod common;
use common::InformationSource;

const ITEM_COUNT: u32 = 1_000;
// More than four fixed 512-datom source chunks forces five initial runs and a
// genuine second merge level at fan-in four.
const AVET_BACKFILL_DATOMS: u32 = 2_100;

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

fn await_background_publication(
    service: &atomic_core::TransactionService,
    basis_t: u64,
) -> atomic_core::BackgroundIndexingStats {
    // A deliberately multi-level external AVET sort advances through several
    // separately committed same-basis publications and live-set folds.
    let deadline = Instant::now() + Duration::from_secs(120);
    loop {
        let stats = service.background_indexing_stats();
        if stats.published_basis_t >= basis_t && stats.pending_avet_projections == 0 {
            return stats;
        }
        assert!(
            Instant::now() < deadline,
            "schema-triggered physical publication did not complete: {stats:?}"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
}

fn latest_avet_counts(connection: &str, database_id: &str) -> (i64, i64) {
    let mut client = Client::connect(connection, NoTls).unwrap();
    let row = client
        .query_one(
            "SELECT COALESCE(sum(root.datom_count) FILTER (WHERE NOT root.history), 0)::bigint, \
                    COALESCE(sum(root.datom_count) FILTER (WHERE root.history), 0)::bigint \
               FROM atomic_tree_manifest_roots root \
              WHERE root.index_order = 2 \
                AND root.manifest_hash = ( \
                    SELECT publication.manifest_hash \
                      FROM atomic_tree_publications publication \
                     WHERE publication.database_id = $1 \
                     ORDER BY publication.publication_revision DESC LIMIT 1)",
            &[&database_id],
        )
        .unwrap();
    (row.get(0), row.get(1))
}

fn finish_publication_work(connection: &str, manifest_hash: Digest) {
    let mut tree_store = PostgresTreeStore::connect(connection).unwrap();
    for _ in 0..10_000 {
        if tree_store.advance_publication_work(manifest_hash).unwrap() {
            return;
        }
    }
    panic!("native publication work did not finish within its bounded test fence");
}

fn schema(no_history: bool, indexed: bool) -> Schema {
    let mut schema = Schema::new();
    let mut count = Attribute::new(
        ITEM_COUNT,
        Keyword::new("item", "count"),
        ValueType::Long,
        Cardinality::One,
    );
    count.indexed = indexed;
    count.no_history = no_history;
    schema.install(count).unwrap();
    schema
}

fn manifest(client: &mut Client, database_id: &str, basis_t: u64) -> PersistentTreeManifest {
    let payload: Vec<u8> = client
        .query_one(
            "SELECT m.payload FROM atomic_tree_publications p \
             JOIN atomic_tree_manifests m \
               ON m.database_id = p.database_id \
              AND m.publication_revision = p.publication_revision \
              AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
              AND m.manifest_hash = p.manifest_hash \
             WHERE p.database_id = $1 AND p.basis_t = $2 \
             ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id, &(basis_t as i64)],
        )
        .unwrap()
        .get(0);
    PersistentTreeManifest::decode(&payload).unwrap()
}

fn manifests_at_basis(
    connection: &str,
    database_id: &str,
    basis_t: u64,
) -> Vec<PersistentTreeManifest> {
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .query(
            "SELECT manifest.payload FROM atomic_tree_publications publication \
             JOIN atomic_tree_manifests manifest \
               ON manifest.manifest_hash = publication.manifest_hash \
              AND manifest.database_id = publication.database_id \
              AND manifest.publication_revision = publication.publication_revision \
              AND manifest.basis_t = publication.basis_t \
              AND manifest.tx_hash = publication.tx_hash \
              AND manifest.log_generation = publication.log_generation \
             WHERE publication.database_id = $1 AND publication.basis_t = $2 \
             ORDER BY publication.publication_revision",
            &[&database_id, &(basis_t as i64)],
        )
        .unwrap()
        .into_iter()
        .map(|row| PersistentTreeManifest::decode(&row.get::<_, Vec<u8>>(0)).unwrap())
        .collect()
}

fn assert_snapshot_matches(
    snapshot: &atomic_core::PeerSnapshot,
    expected: &impl InformationSource,
) {
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(
                snapshot.datoms(history, order).unwrap().datoms,
                expected.test_datoms(
                    if history {
                        View::History
                    } else {
                        View::Current
                    },
                    order,
                ),
                "tree/oracle mismatch for {order:?} history={history}"
            );
        }
    }
}

#[test]
fn localized_successor_reads_and_writes_paths_not_the_whole_tree() {
    let Some(connection) = connection() else {
        return;
    };
    let costs = [1_024, 4_096].map(|items| localized_successor_case(&connection, items));
    let (small_initial, small_metadata, small_successor) = &costs[0];
    let (large_initial, large_metadata, large_successor) = &costs[1];
    assert!(large_initial.segment_count > small_initial.segment_count * 3);
    // The fixed schema/history has the same authenticated metadata cost at
    // both sizes. The larger business tree may introduce adjacent directory
    // paths: one directory and one boundary leaf on either side of each of
    // the three changed datoms in each of eight indexes. It must not turn a
    // fixed delta into a scan of the larger tree.
    assert_eq!(large_metadata.node_reads, small_metadata.node_reads);
    let adjacent_paths = 4 * 8 * small_successor.tail_datoms;
    assert!(
        large_successor.node_reads <= small_successor.node_reads + adjacent_paths,
        "complete fixed-delta read work grew beyond adjacent paths: {costs:?}"
    );
}

fn localized_successor_case(
    connection: &str,
    items: usize,
) -> (IndexBuildReceipt, IndexBuildReceipt, IndexBuildReceipt) {
    let case_started = Instant::now();
    eprintln!("localized_items={items} phase=population");
    let database_id = unique("incremental_tree");
    // Unique fixed-width values preserve the same ordering/packing shape while
    // keeping business leaves distinct across test runs. Read counters include
    // every loaded node even when its content already exists globally.
    let salt = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos() as i64
        & 0x3fff_ffff_ffff_ffff;
    let mut migrator = atomic_core::PostgresMigrator::connect(connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(connection).unwrap();
    let created = store
        .create_database(&database_id, schema(false, true))
        .unwrap();

    let service = common::start_service(connection, &database_id);
    let operations = (0..items)
        .map(|index| TxOp::Add {
            entity: EntityRef::Temp(format!("item-{index}")),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(salt + index as i64)),
        })
        .collect::<Vec<_>>();
    // The larger fixture is one bulk transaction, not the measured delta.
    // Give its caller a bounded setup allowance without changing the ordinary
    // five-second lease or either localized transaction's five-second wait.
    let populated = service
        .client()
        .transact(
            atomic_core::TransactionRequest::new("populate-large-tree", operations)
                .comparing_basis(created.basis_t())
                .with_tx_instant(1_000),
            Duration::from_secs(30),
        )
        .unwrap();
    eprintln!(
        "localized_items={items} phase=population-complete elapsed={:?}",
        case_started.elapsed()
    );
    let changed_index = items / 2;
    let entity = populated.tempids[&format!("item-{changed_index}")];
    service.shutdown();

    let mut indexer = PostgresIndexer::connect(connection, &database_id)
        .unwrap()
        .with_segment_datoms(8)
        .unwrap();
    let initial = indexer.consolidate().unwrap();
    assert!(
        initial.segment_count > 500,
        "initial witness is not large: {initial:?}"
    );
    // Publication is atomic before derived live-set bookkeeping is complete.
    // This witness drives the indexer manually, so finish those bounded batches
    // before measuring whether the next build chose the incremental path.
    finish_publication_work(connection, initial.manifest_hash);
    // A no-op consolidation still authenticates all roots and reconstructs
    // schema/identity metadata. Keep this measured overhead in the total
    // successor count; do not compare it with an arbitrary percentage of a
    // tiny-page fixture's size. Stats/cache are reset by each consolidation.
    let metadata = indexer.consolidate().unwrap();
    assert!(metadata.reused);
    assert_eq!(metadata.input_datoms, 0);
    assert_eq!(metadata.segment_count, 0);

    let service = common::start_service(connection, &database_id);
    let updated = common::transact(
        &service,
        "localized-update",
        populated.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(salt + 10_000)),
        }],
        2_000,
    );
    service.shutdown();
    let successor = indexer.consolidate().unwrap();
    eprintln!(
        "localized_items={items}\ninitial={initial:?}\nmetadata={metadata:?}\nsuccessor={successor:?}"
    );
    assert_eq!(successor.tail_datoms, 3);
    assert_eq!(successor.tail_datoms, updated.tx_data.len() as u64);
    assert_eq!(successor.input_datoms, successor.tail_datoms);
    // `select_merge_leaves` loads up to three directories, three point-neighbor
    // leaves, two touched-directory boundary leaves and two adjacent-directory
    // boundary leaves per point (10). Allow two additional packing-boundary
    // leaves and the three-level exact EAV lookup per tail datom. All eight
    // root reads are already included in `metadata.node_reads`. This is a
    // conservative fixed-delta path bound, not a whole-tree percentage.
    let path_bound = successor.tail_datoms * (8 * 12 + 3);
    assert!(
        successor.node_reads <= metadata.node_reads + path_bound,
        "complete reads exceeded metadata plus localized paths"
    );
    assert!(successor.segment_count * 10 < initial.segment_count);
    assert!(successor.reused_subtrees > 100);
    finish_publication_work(connection, successor.manifest_hash);

    let mut client = Client::connect(connection, NoTls).unwrap();
    let initial_manifest = manifest(&mut client, &database_id, populated.basis_t);
    let successor_manifest = manifest(&mut client, &database_id, updated.basis_t);
    let unchanged = initial_manifest
        .trees
        .iter()
        .zip(&successor_manifest.trees)
        .filter(|(before, after)| before.descriptor.root_hash == after.descriptor.root_hash)
        .count();
    assert!(unchanged > 0, "localized update reused no complete roots");
    assert_snapshot_matches(
        &Peer::connect(connection, &database_id, 64)
            .unwrap()
            .snapshot(),
        &updated.db_after,
    );
    // Independent application-data oracle: do not prove the physical result
    // correct only by comparing it with another view of the same transaction.
    let snapshot = Peer::connect(connection, &database_id, 64)
        .unwrap()
        .snapshot();
    let actual = snapshot
        .datoms(false, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.attribute == ITEM_COUNT)
        .map(|datom| (datom.entity, datom.value))
        .collect::<std::collections::BTreeMap<_, _>>();
    let expected = (0..items)
        .map(|index| {
            (
                populated.tempids[&format!("item-{index}")],
                Value::Long(if index == changed_index {
                    salt + 10_000
                } else {
                    salt + index as i64
                }),
            )
        })
        .collect::<std::collections::BTreeMap<_, _>>();
    assert_eq!(actual, expected);
    let history = snapshot
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.attribute == ITEM_COUNT)
        .collect::<Vec<_>>();
    assert_eq!(history.len(), items + 2);
    assert_eq!(history.iter().filter(|datom| !datom.added).count(), 1);
    assert!(history.iter().any(|datom| {
        datom.entity == entity
            && !datom.added
            && datom.value == Value::Long(salt + changed_index as i64)
    }));

    let service = common::start_service(connection, &database_id);
    let next = common::transact(
        &service,
        "interrupted-update",
        updated.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(salt + 20_000)),
        }],
        3_000,
    );
    service.shutdown();
    assert_eq!(
        indexer
            .consolidate_with_fault(IndexBuildFault::AfterSegments)
            .unwrap_err()
            .code,
        "index/injected-failure"
    );
    let published: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications \
             WHERE database_id = $1 AND basis_t = $2",
            &[&database_id, &(next.basis_t as i64)],
        )
        .unwrap()
        .get(0);
    assert_eq!(published, 0, "interrupted candidate became visible");
    assert_snapshot_matches(
        &Peer::connect(connection, &database_id, 64)
            .unwrap()
            .snapshot(),
        &next.db_after,
    );
    let retried = indexer.consolidate().unwrap();
    assert!(
        retried.node_reuses > 0,
        "orphaned immutable nodes were not reused"
    );

    let flat_writes: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_index_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        flat_writes, 0,
        "native indexer still dual-wrote flat manifests"
    );
    eprintln!(
        "localized_items={items} phase=complete elapsed={:?}",
        case_started.elapsed()
    );
    (initial, metadata, successor)
}

#[test]
fn no_history_pair_wholly_inside_tail_is_safe_and_job_time_only() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("incremental_nohistory_tail");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store
        .create_database(&database_id, schema(true, true))
        .unwrap();
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();

    let service = common::start_service(&connection, &database_id);
    let asserted = common::transact(
        &service,
        "assert-in-tail",
        created.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Temp("short-lived".into()),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(7)),
        }],
        1_000,
    );
    let entity = asserted.tempids["short-lived"];
    let retracted = common::transact(
        &service,
        "retract-in-tail",
        asserted.basis_t,
        &[TxOp::Retract {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: Some(TxValue::Scalar(Value::Long(7))),
        }],
        2_000,
    );
    service.shutdown();

    let receipt = indexer.consolidate().unwrap();
    assert_eq!(receipt.basis_t, retracted.basis_t);
    let snapshot = Peer::connect(&connection, &database_id, 32)
        .unwrap()
        .snapshot();
    let current = snapshot.datoms(false, IndexOrder::Eavt).unwrap().datoms;
    assert!(!current.iter().any(|datom| {
        datom.entity == entity && datom.attribute == ITEM_COUNT && datom.value == Value::Long(7)
    }));
    let history = snapshot.datoms(true, IndexOrder::Eavt).unwrap().datoms;
    assert!(!history.iter().any(|datom| {
        datom.entity == entity && datom.attribute == ITEM_COUNT && datom.value == Value::Long(7)
    }));
}

#[test]
fn avet_schema_transition_is_an_explicit_attribute_range_job() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("incremental_avet_transition");
    let salt = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos() as i64
        & 0x3fff_ffff_ffff_ffff;
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store
        .create_database(&database_id, schema(false, false))
        .unwrap();
    let service = common::start_service(&connection, &database_id);
    // AEVT entity order deliberately opposes value order. More than two
    // fixed 512-datom source chunks forces multiple spill runs and a real
    // four-way external merge before bounded same-basis projection copies.
    let operations = (0..AVET_BACKFILL_DATOMS)
        .map(|index| TxOp::Add {
            entity: EntityRef::Temp(format!("item-{index}")),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(salt + i64::from(AVET_BACKFILL_DATOMS - index))),
        })
        .collect::<Vec<_>>();
    // Keep each writer request comfortably below the generic five-second test
    // client deadline. The aggregate AEVT range still exceeds four 512-datom
    // sort runs, which is the behavior this witness needs to exercise.
    let mut basis_t = created.basis_t();
    let mut populated = None;
    for (batch, operations) in operations.chunks(350).enumerate() {
        let report = common::transact(
            &service,
            &format!("populate-unindexed-attribute-{batch}"),
            basis_t,
            operations,
            1_000 + batch as i64,
        );
        basis_t = report.basis_t;
        populated = Some(report);
    }
    let populated = populated.expect("AVET backfill fixture has at least one batch");
    service.shutdown();
    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(8)
        .unwrap();
    indexer.consolidate().unwrap();
    let baseline_physical_avet = latest_avet_counts(&connection, &database_id);

    let mut indexed = populated
        .db_after
        .schema()
        .attribute(ITEM_COUNT)
        .unwrap()
        .clone();
    indexed.indexed = true;
    let service = common::start_service(&connection, &database_id);
    let enabled = common::transact(
        &service,
        "enable-avet",
        populated.basis_t,
        &[TxOp::AlterAttribute(indexed)],
        2_000,
    );
    let enabled_hook = enabled
        .tx_data
        .iter()
        .find(|datom| {
            datom.entity == 0
                && u64::from(datom.attribute) == DB_ALTER_ATTRIBUTE
                && datom.value == Value::Ref(u64::from(ITEM_COUNT))
                && datom.added
        })
        .expect("enable transaction omitted its alter-attribute event");
    assert_eq!(enabled_hook.tx, t_to_tx(enabled.basis_t).unwrap());
    let enabled_stats = await_background_publication(&service, enabled.basis_t);
    assert!(enabled_stats.jobs_completed > 0);
    service.shutdown();
    let enabled_build = indexer.consolidate().unwrap();
    assert!(enabled_build.reused);
    assert!(enabled.tx_data.len() < 10);
    let transition = manifests_at_basis(&connection, &database_id, enabled.basis_t);
    let pending = transition
        .iter()
        .filter(|manifest| !manifest.pending_avet.is_empty())
        .collect::<Vec<_>>();
    assert!(
        !pending.is_empty(),
        "multi-chunk AVET backfill must publish an observable incomplete root"
    );
    assert!(pending.iter().all(|manifest| {
        manifest.index_basis_t == populated.basis_t && manifest.index_basis_t < manifest.basis_t
    }));
    let completed = transition.last().expect("AVET transition has a final root");
    assert!(completed.pending_avet.is_empty());
    assert_eq!(completed.index_basis_t, enabled.basis_t);
    let enabled_physical_avet = latest_avet_counts(&connection, &database_id);
    assert!(enabled_physical_avet.0 - baseline_physical_avet.0 >= i64::from(AVET_BACKFILL_DATOMS));
    assert!(enabled_physical_avet.1 - baseline_physical_avet.1 >= i64::from(AVET_BACKFILL_DATOMS));
    assert!(
        enabled_physical_avet.0 + enabled_physical_avet.1 > enabled.tx_data.len() as i64,
        "physical AVET roots did not expose the historical attribute-range backfill"
    );
    let refreshed = Peer::connect(&connection, &database_id, 64)
        .unwrap()
        .snapshot();
    assert_eq!(refreshed.basis_t(), enabled.db_after.basis_t());
    assert_eq!(refreshed.schema(), enabled.db_after.schema());
    let pending_avet = IndexPrefix::Avet {
        attribute: ITEM_COUNT,
        value: None,
        entity: None,
    };
    assert_eq!(
        enabled
            .db_after
            .datoms(IndexOrder::Avet)
            .unwrap()
            .into_iter()
            .filter(|datom| datom.attribute == ITEM_COUNT)
            .count(),
        0,
        "an unqualified raw AVET scan must expose only the physically ready projection"
    );
    assert_eq!(
        enabled
            .db_after
            .datoms_with_prefix(&pending_avet)
            .unwrap_err()
            .code,
        "peer/avet-not-ready",
        "an attribute-qualified raw AVET seek must not silently return a partial result"
    );
    assert_eq!(
        enabled
            .db_after
            .datoms_with_prefix(&IndexPrefix::Aevt {
                attribute: ITEM_COUNT,
                entity: None,
                value: None,
            })
            .unwrap()
            .len(),
        AVET_BACKFILL_DATOMS as usize,
        "the immutable logical value must remain complete through AEVT while AVET is pending"
    );
    let enabled_avet = refreshed
        .datoms_with_prefix(false, &pending_avet)
        .unwrap()
        .datoms
        .len();
    assert_eq!(enabled_avet, AVET_BACKFILL_DATOMS as usize);

    let mut unindexed = enabled
        .db_after
        .schema()
        .attribute(ITEM_COUNT)
        .unwrap()
        .clone();
    unindexed.indexed = false;
    let service = common::start_service(&connection, &database_id);
    let disabled = common::transact(
        &service,
        "disable-avet",
        enabled.basis_t,
        &[TxOp::AlterAttribute(unindexed)],
        3_000,
    );
    let disabled_hook = disabled
        .tx_data
        .iter()
        .find(|datom| {
            datom.entity == 0
                && u64::from(datom.attribute) == DB_ALTER_ATTRIBUTE
                && datom.value == Value::Ref(u64::from(ITEM_COUNT))
                && datom.added
        })
        .expect("disable transaction omitted its repeated alter-attribute event");
    assert_eq!(disabled_hook.tx, t_to_tx(disabled.basis_t).unwrap());
    let disabled_stats = await_background_publication(&service, disabled.basis_t);
    assert!(disabled_stats.jobs_completed > 0);
    service.shutdown();
    let disabled_build = indexer.consolidate().unwrap();
    assert!(disabled_build.reused);
    assert!(disabled.tx_data.len() < 10);
    let disabled_physical_avet = latest_avet_counts(&connection, &database_id);
    let schema_tail_noise = disabled.tx_data.len() as i64;
    assert!(
        enabled_physical_avet.0 - disabled_physical_avet.0
            >= i64::from(AVET_BACKFILL_DATOMS) - schema_tail_noise
    );
    assert!(
        enabled_physical_avet.1 - disabled_physical_avet.1
            >= i64::from(AVET_BACKFILL_DATOMS) - schema_tail_noise
    );
    let final_snapshot = Peer::connect(&connection, &database_id, 64)
        .unwrap()
        .snapshot();
    assert_snapshot_matches(&final_snapshot, &disabled.db_after);
    let current_hook = final_snapshot
        .datoms(false, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .find(|datom| {
            datom.entity == 0
                && u64::from(datom.attribute) == DB_ALTER_ATTRIBUTE
                && datom.value == Value::Ref(u64::from(ITEM_COUNT))
                && datom.added
        })
        .expect("published current tree omitted the alter-attribute event");
    assert_eq!(current_hook.tx, t_to_tx(disabled.basis_t).unwrap());
    let hook_history = final_snapshot
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| {
            datom.entity == 0
                && u64::from(datom.attribute) == DB_ALTER_ATTRIBUTE
                && datom.value == Value::Ref(u64::from(ITEM_COUNT))
                && datom.added
        })
        .map(|datom| datom.tx)
        .collect::<Vec<_>>();
    assert_eq!(
        hook_history,
        vec![
            t_to_tx(disabled.basis_t).unwrap(),
            t_to_tx(enabled.basis_t).unwrap(),
        ]
    );
    assert_eq!(
        disabled
            .db_after
            .datoms(IndexOrder::Avet)
            .unwrap()
            .into_iter()
            .filter(|datom| datom.attribute == ITEM_COUNT)
            .count(),
        0
    );
}
