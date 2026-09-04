use atomic_core::{
    Attribute, Cardinality, DB_ALTER_ATTRIBUTE, Digest, EntityRef, IndexBuildFault, IndexOrder,
    Keyword, Peer, PersistentTreeManifest, PostgresIndexer, PostgresStore, PostgresTreeStore,
    Schema, TxOp, TxValue, Value, ValueType, View, t_to_tx,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

mod common;
use common::InformationSource;

const ITEM_COUNT: u32 = 1_000;
const AVET_BACKFILL_DATOMS: u32 = 1_200;

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
    let deadline = Instant::now() + Duration::from_secs(30);
    loop {
        let stats = service.background_indexing_stats();
        if stats.published_basis_t >= basis_t && stats.pending_avet_projections == 0 {
            return stats;
        }
        assert!(
            Instant::now() < deadline,
            "schema-triggered physical publication did not complete"
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
    let database_id = unique("incremental_tree");
    let salt = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos() as i64
        & 0x3fff_ffff_ffff_ffff;
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store
        .create_database(&database_id, schema(false, true))
        .unwrap();

    let service = common::start_service(&connection, &database_id);
    let operations = (0..1_024)
        .map(|index| TxOp::Add {
            entity: EntityRef::Temp(format!("item-{index}")),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(salt + index)),
        })
        .collect::<Vec<_>>();
    let populated = common::transact(
        &service,
        "populate-large-tree",
        created.basis_t(),
        &operations,
        1_000,
    );
    let entity = populated.tempids["item-512"];
    service.shutdown();

    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
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
    finish_publication_work(&connection, initial.manifest_hash);

    let service = common::start_service(&connection, &database_id);
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
    eprintln!("initial={initial:?}\nsuccessor={successor:?}");
    assert_eq!(successor.tail_datoms, updated.tx_data.len() as u64);
    assert_eq!(successor.input_datoms, successor.tail_datoms);
    assert!(successor.node_reads * 10 < initial.segment_count as u64);
    assert!(successor.segment_count * 10 < initial.segment_count);
    assert!(successor.reused_subtrees > 100);
    finish_publication_work(&connection, successor.manifest_hash);

    let mut client = Client::connect(&connection, NoTls).unwrap();
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
        &Peer::connect(&connection, &database_id, 64)
            .unwrap()
            .snapshot(),
        &updated.db_after,
    );

    let service = common::start_service(&connection, &database_id);
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
        &Peer::connect(&connection, &database_id, 64)
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
            value: TxValue::Scalar(Value::Long(
                salt + i64::from(AVET_BACKFILL_DATOMS - index),
            )),
        })
        .collect::<Vec<_>>();
    let populated = common::transact(
        &service,
        "populate-unindexed-attribute",
        created.basis_t(),
        &operations,
        1_000,
    );
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
    let enabled_physical_avet = latest_avet_counts(&connection, &database_id);
    assert!(
        enabled_physical_avet.0 - baseline_physical_avet.0
            >= i64::from(AVET_BACKFILL_DATOMS)
    );
    assert!(
        enabled_physical_avet.1 - baseline_physical_avet.1
            >= i64::from(AVET_BACKFILL_DATOMS)
    );
    assert!(
        enabled_physical_avet.0 + enabled_physical_avet.1 > enabled.tx_data.len() as i64,
        "physical AVET roots did not expose the historical attribute-range backfill"
    );
    let refreshed = Peer::connect(&connection, &database_id, 64)
        .unwrap()
        .snapshot();
    assert_eq!(refreshed.basis_t(), enabled.db_after.basis_t());
    assert_eq!(refreshed.schema(), enabled.db_after.schema());
    assert_eq!(
        enabled.db_after.datoms(IndexOrder::Avet).unwrap_err().code,
        "peer/avet-not-ready",
        "the immutable db-after must not retroactively gain a physical AVET"
    );
    let enabled_avet = refreshed
        .datoms(false, IndexOrder::Avet)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.attribute == ITEM_COUNT)
        .count();
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
