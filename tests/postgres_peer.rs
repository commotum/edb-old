use atomic_core::persistent_tree::{TreeNode, decode_tree_node};
use atomic_core::{
    Attribute, AttributeName, Binding, Cardinality, Clause, DataPattern, EntityRef, EntityValue,
    FindElement, FindSpec, Function, IndexBuildFault, IndexOrder, IndexPrefix, InputSpec,
    Instruction, Keyword, Peer, PostgresIndexer, PostgresStore, Program, ProgramKind,
    PullAttribute, PullPattern, Query, QueryControl, QueryEngine, QueryExtensions, QueryInput,
    QueryOutcome, QueryResult, QuerySource, QueryValue, Schema, Term, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp, TxValue, Unique, Value, ValueType,
    Variable, View, decode_index_manifest, encode_index_manifest, sha256,
};
use postgres::{Client, NoTls};
use std::process::Command;
use std::sync::{Arc, Barrier};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;
use common::InformationSource;

const ITEM_NAME: u32 = 1_000;
const ITEM_COUNT: u32 = 1_001;
const ITEM_PARENT: u32 = 1_002;

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

fn schema(no_history: bool) -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                ITEM_NAME,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    let mut count = Attribute::new(
        ITEM_COUNT,
        Keyword::new("item", "count"),
        ValueType::Long,
        Cardinality::One,
    );
    count.indexed = true;
    count.no_history = no_history;
    schema.install(count).unwrap();
    let mut parent = Attribute::new(
        ITEM_PARENT,
        Keyword::new("item", "parent"),
        ValueType::Ref,
        Cardinality::One,
    );
    parent.indexed = true;
    schema.install(parent).unwrap();
    schema
}

fn assert_current_eq(left: &impl InformationSource, right: &impl InformationSource) {
    assert_eq!(left.test_basis_t(), right.test_basis_t());
    assert_eq!(left.test_eidx_frontier(), right.test_eidx_frontier());
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            left.test_datoms(View::Current, order),
            right.test_datoms(View::Current, order)
        );
    }
}

fn assert_eager_native_query_differential(
    label: &str,
    query: &Query,
    eager: atomic_core::DatabaseValue,
    native: atomic_core::DatabaseValue,
) -> Vec<QueryOutcome> {
    let execute = |database, force_scan| {
        QueryEngine::execute(
            query,
            &[QuerySource {
                name: "$".into(),
                database,
            }],
            &[],
            &QueryControl {
                force_scan,
                ..QueryControl::default()
            },
        )
        .unwrap_or_else(|error| panic!("{label} failed (force_scan={force_scan}): {error}"))
    };
    let outcomes = vec![
        execute(eager.clone(), false),
        execute(eager, true),
        execute(native.clone(), false),
        execute(native, true),
    ];
    for outcome in &outcomes[1..] {
        assert_eq!(
            outcome.result, outcomes[0].result,
            "{label} diverged between eager/native or optimized/scan evaluation"
        );
    }
    assert!(
        outcomes[0].stats.datoms_examined <= outcomes[1].stats.datoms_examined,
        "{label} eager optimized path examined more datoms than force-scan"
    );
    assert!(
        outcomes[2].stats.datoms_examined <= outcomes[3].stats.datoms_examined,
        "{label} native optimized path examined more datoms than force-scan"
    );
    outcomes
}

fn populated(
    connection: &str,
    database_id: &str,
    no_history: bool,
    updates: i64,
) -> (PostgresStore, u64) {
    let mut migrator = atomic_core::PostgresMigrator::connect(connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(connection).unwrap();
    let created = store
        .create_database(database_id, schema(no_history))
        .unwrap();
    let service = common::start_service(connection, database_id);
    let first = common::transact(
        &service,
        "create",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ITEM_NAME,
                value: TxValue::Scalar(Value::String(database_id.into())),
            },
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long(0)),
            },
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ITEM_PARENT,
                value: TxValue::Entity(EntityRef::Temp("item".into())),
            },
        ],
        1_000,
    );
    let entity = first.tempids["item"];
    let mut basis = first.basis_t;
    for value in 1..=updates {
        let receipt = common::transact(
            &service,
            &format!("update-{value}"),
            basis,
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long(value)),
            }],
            1_000 + value,
        );
        basis = receipt.basis_t;
    }
    service.shutdown();
    (store, entity)
}

#[test]
fn native_database_value_prefix_cursor_matches_eager_with_bounded_tree_reads() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("database_value_prefix");
    let (mut store, entity) = populated(&connection, &database_id, false, 32);
    let eager = store.recover(&database_id).unwrap().database_value();

    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(1)
        .unwrap();
    let publication = indexer.consolidate().unwrap();
    assert_eq!(publication.basis_t, eager.basis_t());

    // A zero-sized decoded-node cache makes the delta below actual durable
    // path work. It cannot be an accidental hit left by resident metadata.
    let peer = Peer::connect_with_cache_limits(&connection, &database_id, 0, 0).unwrap();
    let native = peer.database_value();
    assert_eq!(native.basis_t(), eager.basis_t());
    assert_eq!(native.eidx_frontier(), eager.eidx_frontier());
    assert_eq!(
        native.last_tx_instant().unwrap(),
        eager.last_tx_instant().unwrap()
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

    let prefix = IndexPrefix::Eavt {
        entity,
        attribute: Some(ITEM_COUNT),
        value: None,
    };
    let expected = eager
        .current_prefix_cursor(&prefix)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    let before = peer.load_stats();
    let actual = native
        .current_prefix_cursor(&prefix)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    let after = peer.load_stats();

    assert_eq!(actual, expected);
    assert_eq!(actual.len(), 1);
    assert_eq!(after.compatibility_materializations, 0);
    let path_reads = after
        .directory_reads
        .saturating_sub(before.directory_reads)
        .saturating_add(after.leaf_reads.saturating_sub(before.leaf_reads));
    assert!(path_reads > 0);
    assert!(
        path_reads <= 4,
        "one exact EAVT prefix used {path_reads} durable tree nodes"
    );
    assert!(
        path_reads < publication.segment_count as u64,
        "one exact prefix read the whole {}-segment publication",
        publication.segment_count
    );
}

#[test]
fn peers_use_verified_base_tail_and_keep_old_snapshots() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer");
    let (mut store, entity) = populated(&connection, &database_id, false, 8);
    let expected = store.recover(&database_id).unwrap();

    let before_index = Peer::connect(&connection, &database_id, 2).unwrap();
    let old = before_index.db();
    // The transactor owns a background indexer, so startup may already have
    // published an early immutable base. What matters is that this captured
    // value is older than the explicit consolidation below and remains exact.
    assert!(before_index.durable_base_t() < expected.basis_t());
    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(2)
        .unwrap();
    let built = indexer.consolidate().unwrap();
    assert_eq!(built.basis_t, expected.basis_t());
    assert!(built.segment_count > 8);
    assert!(indexer.consolidate().unwrap().reused);

    assert!(before_index.refresh_index().unwrap());
    assert_eq!(before_index.durable_base_t(), expected.basis_t());
    assert_eq!(old.values(entity, ITEM_COUNT), vec![&Value::Long(8)]);

    let cache_peer = Peer::connect(&connection, &database_id, built.segment_count + 1).unwrap();
    let cold = cache_peer.load_stats();
    assert_eq!(cold.root_reads, 8);
    assert!(cold.directory_reads > 0);
    assert!(cold.leaf_reads > 0);
    assert_eq!(cold.compatibility_materializations, 0);
    assert!(!cache_peer.refresh_index().unwrap());
    let refreshed = cache_peer.load_stats();
    assert_eq!(refreshed.directory_reads, cold.directory_reads);
    assert_eq!(refreshed.leaf_reads, cold.leaf_reads);
    assert_eq!(refreshed.compatibility_materializations, 0);

    let peer = Peer::connect(&connection, &database_id, 2).unwrap();
    assert_eq!(peer.durable_base_t(), expected.basis_t());
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    assert_current_eq(&peer.db(), &expected);
    assert_eq!(peer.load_stats().compatibility_materializations, 1);

    // Opening the peer loaded the eight small roots plus only the authenticated
    // schema/ident paths. The first application seek performs at most one
    // directory and one leaf read (and may reuse a metadata path); the
    // identical seek is then entirely cache-backed.
    // Full Database construction above was an explicit, observable
    // compatibility request, not peer startup work.
    let snapshot = peer.snapshot();
    assert_eq!(snapshot.durable_base_t(), Some(expected.basis_t()));
    let key = expected
        .datoms(View::Current, IndexOrder::Eavt)
        .into_iter()
        .find(|datom| datom.entity == entity && datom.attribute == ITEM_COUNT)
        .unwrap();
    let expected_from_key = expected
        .datoms(View::Current, IndexOrder::Eavt)
        .into_iter()
        .filter(|datom| !datom.cmp_in(&key, IndexOrder::Eavt).is_lt())
        .collect::<Vec<_>>();
    let mut cursor = snapshot
        .range_cursor(false, IndexOrder::Eavt, Some(&key), None)
        .unwrap();
    assert_eq!(cursor.stats().tree.directory_reads, 0);
    assert_eq!(cursor.stats().tree.leaf_reads, 0);
    assert_eq!(cursor.next().transpose().unwrap(), Some(key.clone()));
    assert_eq!(cursor.stats().tree.root_reads, 0);
    assert!(cursor.stats().tree.directory_reads <= 1);
    assert!(cursor.stats().tree.leaf_reads <= 1);

    let cached_seek = snapshot.seek(false, IndexOrder::Eavt, &key).unwrap();
    assert_eq!(cached_seek.datom, Some(key.clone()));
    assert_eq!(cached_seek.stats.directory_reads, 0);
    assert_eq!(cached_seek.stats.leaf_reads, 0);

    // The cursor owns immutable node handles. Force the tiny shared cache to
    // visit unrelated trees, then prove eviction cannot invalidate the open
    // range or alter its remaining order.
    for order in [IndexOrder::Aevt, IndexOrder::Avet, IndexOrder::Vaet] {
        let other = expected
            .datoms(View::Current, order)
            .last()
            .cloned()
            .unwrap();
        snapshot.seek(false, order, &other).unwrap();
    }
    let mut streamed = vec![key.clone()];
    streamed.extend(cursor.collect::<Result<Vec<_>, _>>().unwrap());
    assert_eq!(streamed, expected_from_key);
    let tree_prefix = snapshot
        .datoms_with_prefix(
            false,
            &IndexPrefix::Eavt {
                entity,
                attribute: Some(ITEM_COUNT),
                value: None,
            },
        )
        .unwrap();
    assert_eq!(
        tree_prefix.datoms,
        expected
            .datoms_with_prefix(&IndexPrefix::Eavt {
                entity,
                attribute: Some(ITEM_COUNT),
                value: None,
            })
            .unwrap()
    );
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(
                snapshot.datoms(history, order).unwrap().datoms,
                expected.datoms(
                    if history {
                        View::History
                    } else {
                        View::Current
                    },
                    order,
                )
            );
        }
    }
    assert!(peer.cache_stats().current_bytes <= 2 * 512 * 1024);

    assert!(peer.enable_tx_reports());
    let service = common::start_service(&connection, &database_id);
    let committed = common::transact(
        &service,
        "after-index",
        expected.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(99)),
        }],
        2_000,
    );
    assert_eq!(old.basis_t(), expected.basis_t());
    assert_eq!(old.values(entity, ITEM_COUNT), vec![&Value::Long(8)]);
    assert_eq!(
        snapshot
            .datoms_with_prefix(
                false,
                &IndexPrefix::Eavt {
                    entity,
                    attribute: Some(ITEM_COUNT),
                    value: None,
                },
            )
            .unwrap()
            .datoms
            .iter()
            .map(|datom| &datom.value)
            .collect::<Vec<_>>(),
        vec![&Value::Long(8)]
    );
    let advanced = peer
        .sync_to(committed.basis_t, Duration::from_secs(1))
        .unwrap();
    assert_current_eq(&advanced, &committed.db_after);
    let live = peer.snapshot();
    assert_eq!(live.durable_base_t(), Some(expected.basis_t()));
    assert_eq!(peer.recent_stats().transactions, 1);
    let lazy_advanced = cache_peer
        .sync_to_snapshot(committed.basis_t, Duration::from_secs(1))
        .unwrap();
    assert_eq!(lazy_advanced.basis_t(), committed.basis_t);
    assert_eq!(cache_peer.load_stats().compatibility_materializations, 0);
    let live_key = committed
        .db_after
        .datoms(IndexOrder::Eavt)
        .unwrap()
        .into_iter()
        .find(|datom| datom.entity == entity && datom.attribute == ITEM_COUNT)
        .unwrap();
    let point = lazy_advanced
        .seek(false, IndexOrder::Eavt, &live_key)
        .unwrap();
    assert_eq!(point.datom, Some(live_key));
    assert!(point.stats.leaf_reads <= 2);
    assert_eq!(
        lazy_advanced
            .datoms_with_prefix(
                false,
                &IndexPrefix::Eavt {
                    entity,
                    attribute: Some(ITEM_COUNT),
                    value: None,
                },
            )
            .unwrap()
            .datoms
            .iter()
            .map(|datom| &datom.value)
            .collect::<Vec<_>>(),
        vec![&Value::Long(99)]
    );
    assert_eq!(
        live.datoms_with_prefix(
            false,
            &IndexPrefix::Eavt {
                entity,
                attribute: Some(ITEM_COUNT),
                value: None,
            },
        )
        .unwrap()
        .datoms
        .iter()
        .map(|datom| &datom.value)
        .collect::<Vec<_>>(),
        vec![&Value::Long(99)]
    );
    assert_eq!(
        peer.take_tx_reports().last().unwrap().basis_t,
        committed.basis_t
    );
    assert_current_eq(&before_index.sync().unwrap(), &committed.db_after);
    service.shutdown();

    let prefix = IndexPrefix::Eavt {
        entity,
        attribute: Some(ITEM_COUNT),
        value: None,
    };
    let matching = advanced
        .datoms_with_prefix(&prefix)
        .unwrap()
        .iter()
        .rev()
        .cloned()
        .collect::<Vec<_>>();
    assert!(
        advanced
            .reverse_seek_datoms(&prefix)
            .unwrap()
            .starts_with(&matching)
    );
}

#[test]
fn transaction_reports_are_opt_in_and_removable_connection_state() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer_reports_opt_in");
    let (mut store, entity) = populated(&connection, &database_id, false, 0);
    let basis = store.recover(&database_id).unwrap().basis_t();
    drop(store);
    let peer = Peer::connect(&connection, &database_id, 8).unwrap();
    assert!(!peer.tx_reports_enabled());

    let service = common::start_service(&connection, &database_id);
    let unobserved = common::transact(
        &service,
        "report-disabled",
        basis,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(1)),
        }],
        2_000,
    );
    peer.sync_to_snapshot(unobserved.basis_t, Duration::from_secs(1))
        .unwrap();
    assert!(peer.take_tx_reports().is_empty());

    assert!(peer.enable_tx_reports());
    assert!(!peer.enable_tx_reports());
    let observed = common::transact(
        &service,
        "report-enabled",
        unobserved.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(2)),
        }],
        2_001,
    );
    peer.sync_to_snapshot(observed.basis_t, Duration::from_secs(1))
        .unwrap();
    assert_eq!(
        peer.take_tx_reports()
            .into_iter()
            .map(|report| report.basis_t)
            .collect::<Vec<_>>(),
        vec![observed.basis_t]
    );

    assert!(peer.remove_tx_reports());
    assert!(!peer.remove_tx_reports());
    let removed = common::transact(
        &service,
        "report-removed",
        observed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(3)),
        }],
        2_002,
    );
    peer.sync_to_snapshot(removed.basis_t, Duration::from_secs(1))
        .unwrap();
    assert!(peer.take_tx_reports().is_empty());
    service.shutdown();
}

#[test]
fn corrupt_current_root_is_repaired_at_the_same_basis_and_peer_adopts_revision() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer_same_basis_root_repair");
    let (mut store, entity) = populated(&connection, &database_id, false, 3);
    let expected = store.recover(&database_id).unwrap();
    drop(store);

    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(2)
        .unwrap();
    let current = indexer.consolidate().unwrap();
    let peer = Peer::connect(&connection, &database_id, 4).unwrap();
    assert_eq!(peer.durable_base_revision(), current.publication_revision);
    let old = peer.snapshot();
    assert_eq!(old.basis_t(), expected.basis_t());

    let mut client = Client::connect(&connection, NoTls).unwrap();
    client
        .batch_execute("ALTER TABLE atomic_tree_manifests DISABLE TRIGGER USER")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_tree_manifests \
                SET payload = set_byte(payload, 16, get_byte(payload, 16) # 1) \
              WHERE manifest_hash = $1",
            &[&&current.manifest_hash[..]],
        )
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_tree_manifests ENABLE TRIGGER USER")
        .unwrap();

    // The corrupt derived value still consumed its physical revision. The
    // builder falls back to an older authenticated root (or the log), then
    // publishes the repair at the next revision without inventing a tx basis.
    let repaired = indexer.consolidate().unwrap();
    assert_eq!(repaired.basis_t, current.basis_t);
    assert_eq!(
        repaired.publication_revision,
        current.publication_revision + 1
    );
    assert_ne!(repaired.manifest_hash, current.manifest_hash);

    assert_eq!(peer.durable_base_revision(), current.publication_revision);
    assert!(peer.refresh_index().unwrap());
    assert_eq!(peer.durable_base_revision(), repaired.publication_revision);
    assert_eq!(peer.durable_base_t(), repaired.basis_t);
    assert_current_eq(&peer.db(), &expected);

    // Captured values pin their old root and remain usable even after the live
    // connection swaps a newer physical root at the same logical basis.
    assert_eq!(
        old.datoms_with_prefix(
            false,
            &IndexPrefix::Eavt {
                entity,
                attribute: Some(ITEM_COUNT),
                value: None,
            },
        )
        .unwrap()
        .datoms
        .iter()
        .map(|datom| &datom.value)
        .collect::<Vec<_>>(),
        vec![&Value::Long(3)]
    );
    let reopened = Peer::connect(&connection, &database_id, 4).unwrap();
    assert_eq!(
        reopened.durable_base_revision(),
        repaired.publication_revision
    );
    assert_current_eq(&reopened.db(), &expected);
}

#[test]
fn native_metadata_rebuild_and_recent_tail_preserve_ident_alias_lifecycle() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer_ident_projection");
    let (mut store, _) = populated(&connection, &database_id, false, 1);
    let initial = store.recover(&database_id).unwrap();
    let old = Keyword::new("status", "pending");
    let new = Keyword::new("status", "awaiting");
    let service = common::start_service(&connection, &database_id);
    let introduced = common::transact(
        &service,
        "introduce-ident",
        initial.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Temp("original".into()),
            attribute: atomic_core::DB_IDENT as u32,
            value: Value::Keyword(old.clone()).into(),
        }],
        2_000,
    );
    let original = introduced.tempids["original"];

    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(2)
        .unwrap();
    indexer.consolidate().unwrap();
    let peer = Peer::connect(&connection, &database_id, 16).unwrap();
    let based = peer.snapshot();
    assert_eq!(based.entid(&old), Some(original));
    assert_eq!(based.ident(original), Some(&old));
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

    let renamed = common::transact(
        &service,
        "rename-ident",
        introduced.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(original),
            attribute: atomic_core::DB_IDENT as u32,
            value: Value::Keyword(new.clone()).into(),
        }],
        2_001,
    );
    let repurposed = common::transact(
        &service,
        "repurpose-ident",
        renamed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("replacement".into()),
            attribute: atomic_core::DB_IDENT as u32,
            value: Value::Keyword(old.clone()).into(),
        }],
        2_002,
    );
    let replacement = repurposed.tempids["replacement"];

    // The unconsolidated authenticated tail updates the same discardable
    // maps as a recovered base: the old name is repurposed while the renamed
    // entity keeps its latest canonical name.
    let tailed = peer.sync_snapshot().unwrap();
    assert_eq!(tailed.basis_t(), repurposed.basis_t);
    assert_eq!(tailed.entid(&old), Some(replacement));
    assert_eq!(tailed.entid(&new), Some(original));
    assert_eq!(tailed.ident(original), Some(&new));
    assert_eq!(tailed.ident(replacement), Some(&old));
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

    // Rebuilding a durable tree base from historical AEVT must produce the
    // identical observation without serializing the alias history in ATIM.
    indexer.consolidate().unwrap();
    assert!(peer.refresh_index().unwrap());
    let consolidated = peer.snapshot();
    assert_eq!(consolidated.entid(&old), Some(replacement));
    assert_eq!(consolidated.entid(&new), Some(original));
    assert_eq!(consolidated.ident(original), Some(&new));
    assert_eq!(consolidated.ident(replacement), Some(&old));
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

    service.shutdown();
    drop(store);
    let restarted = Peer::connect(&connection, &database_id, 16).unwrap();
    let snapshot = restarted.snapshot();
    assert_eq!(snapshot.entid(&old), Some(replacement));
    assert_eq!(snapshot.entid(&new), Some(original));
    assert_eq!(snapshot.ident(original), Some(&new));
    assert_eq!(snapshot.ident(replacement), Some(&old));
    assert_eq!(restarted.load_stats().compatibility_materializations, 0);
}

#[test]
fn interrupted_build_is_invisible_and_corrupt_derived_data_falls_back_to_log() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("index_fault");
    let (mut store, _) = populated(&connection, &database_id, false, 3);
    let expected = store.recover(&database_id).unwrap();
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let publications_before: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(1)
        .unwrap();
    assert_eq!(
        indexer
            .consolidate_with_fault(IndexBuildFault::AfterSegments)
            .unwrap_err()
            .code,
        "index/injected-failure"
    );
    let count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    // The transactor may already have published an earlier immutable base.
    // Fault injection must leave the publication coordinate unchanged; it
    // need not be absent.
    assert_eq!(count, publications_before);
    indexer.consolidate().unwrap();

    let corrupt_hash = client
        .query("SELECT node_hash, payload FROM atomic_tree_nodes", &[])
        .unwrap()
        .into_iter()
        .find_map(|row| {
            let hash: Vec<u8> = row.get(0);
            let hash: [u8; 32] = hash.try_into().unwrap();
            let payload: Vec<u8> = row.get(1);
            match decode_tree_node(&hash, &payload).ok()? {
                TreeNode::Leaf(leaf)
                    if !leaf.history
                        && leaf.order == IndexOrder::Eavt
                        && (0..leaf.len()).any(|index| {
                            leaf.datom(index).is_some_and(|datom| {
                                datom.value == Value::String(database_id.clone())
                            })
                        }) =>
                {
                    Some(hash)
                }
                _ => None,
            }
        })
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_tree_nodes DISABLE TRIGGER USER")
        .unwrap();
    client.execute(
        "UPDATE atomic_tree_nodes SET payload = set_byte(payload, 16, get_byte(payload, 16) # 1) \
         WHERE node_hash = $1", &[&&corrupt_hash[..]],
    ).unwrap();
    client
        .batch_execute("ALTER TABLE atomic_tree_nodes ENABLE TRIGGER USER")
        .unwrap();
    let peer = Peer::connect(&connection, &database_id, 32).unwrap();
    // Startup reads only roots and resident metadata paths, not arbitrary
    // application leaves. Corruption is detected as soon as the affected path
    // is demanded, while explicit full compatibility recovery remains
    // available from the authoritative log.
    assert_eq!(peer.durable_base_t(), expected.basis_t());
    assert!(peer.load_stats().leaf_reads > 0);
    assert_eq!(
        peer.snapshot()
            .datoms_with_prefix(
                false,
                &IndexPrefix::Eavt {
                    entity: expected
                        .datoms(View::Current, IndexOrder::Eavt)
                        .into_iter()
                        .find(|datom| datom.value == Value::String(database_id.clone()))
                        .unwrap()
                        .entity,
                    attribute: Some(ITEM_NAME),
                    value: None,
                },
            )
            .unwrap_err()
            .code,
        "tree/content-hash-mismatch"
    );
    assert_current_eq(&peer.db(), &expected);
}

#[test]
fn self_consistent_legacy_manifest_falls_back_but_invalid_native_authority_fails_closed() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("manifest_log_fault");
    let (mut store, _) = populated(&connection, &database_id, false, 2);
    let expected = store.recover(&database_id).unwrap();
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let Some(flat_row) = client
        .query_opt(
            "SELECT payload FROM atomic_index_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
    else {
        client
            .batch_execute("ALTER TABLE atomic_tree_manifests DISABLE TRIGGER USER")
            .unwrap();
        client
            .execute(
                "UPDATE atomic_tree_manifests \
                 SET payload = set_byte(payload, 16, get_byte(payload, 16) # 1) \
                 WHERE database_id = $1",
                &[&database_id],
            )
            .unwrap();
        client
            .batch_execute("ALTER TABLE atomic_tree_manifests ENABLE TRIGGER USER")
            .unwrap();
        let error = Peer::connect(&connection, &database_id, 32)
            .err()
            .expect("corrupt native authority must fail closed");
        assert_eq!(error.code, "peer/all-native-publications-invalid");
        return;
    };
    let payload: Vec<u8> = flat_row.get(0);
    let mut manifest = decode_index_manifest(&payload).unwrap();
    manifest.tx_hash[0] ^= 1;
    let rewritten = encode_index_manifest(&manifest).unwrap();
    let rewritten_hash = sha256(&rewritten);
    client
        .batch_execute("ALTER TABLE atomic_index_manifests DISABLE TRIGGER ALL")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_index_manifests \
             SET tx_hash = $1, manifest_hash = $2, payload = $3 \
             WHERE database_id = $4",
            &[
                &&manifest.tx_hash[..],
                &&rewritten_hash[..],
                &&rewritten[..],
                &database_id,
            ],
        )
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_index_manifests ENABLE TRIGGER ALL")
        .unwrap();

    let peer = Peer::connect(&connection, &database_id, 32).unwrap();
    assert_eq!(peer.durable_base_t(), 0);
    assert_current_eq(&peer.db(), &expected);
}

#[test]
fn no_history_consolidation_forgets_old_values_without_changing_current_state() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("nohistory");
    let (mut store, entity) = populated(&connection, &database_id, true, 5);
    let expected = store.recover(&database_id).unwrap();
    let authoritative_old: Vec<_> = expected
        .datoms(View::History, IndexOrder::Eavt)
        .into_iter()
        .filter(|d| d.entity == entity && d.attribute == ITEM_COUNT)
        .collect();
    assert!(authoritative_old.len() > 1);
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(2)
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(&connection, &database_id, 8).unwrap();
    assert_current_eq(&peer.db(), &expected);
    let retained: Vec<_> = peer
        .snapshot()
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|d| d.entity == entity && d.attribute == ITEM_COUNT)
        .collect();
    assert_eq!(retained.len(), 1);
    assert!(retained[0].added);
    assert_eq!(retained[0].value, Value::Long(5));
}

#[test]
fn enabling_no_history_changes_only_future_indexing_jobs() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("nohistory_preserves_prior");
    let (mut store, entity) = populated(&connection, &database_id, false, 2);
    // Establish an exact pre-toggle durable base. Changing :db/noHistory does
    // not rewrite that base or alter ordinary history reads by itself.
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let before = store.recover(&database_id).unwrap();
    let mut count = before.schema().attribute(ITEM_COUNT).unwrap().clone();
    count.no_history = true;
    let service = common::start_service(&connection, &database_id);
    let altered = common::transact(
        &service,
        "stop-retaining-future-history",
        before.basis_t(),
        &[TxOp::AlterAttribute(count)],
        2_000,
    );
    let third = common::transact(
        &service,
        "forgotten-three",
        altered.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(3)),
        }],
        2_001,
    );
    let fourth = common::transact(
        &service,
        "retained-current-four",
        third.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(4)),
        }],
        2_002,
    );
    service.shutdown();

    let before_index_job = Peer::connect(&connection, &database_id, 16).unwrap();
    assert!(before_index_job.durable_base_t() < fourth.basis_t);
    let unindexed_history = before_index_job
        .snapshot()
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.entity == entity && datom.attribute == ITEM_COUNT)
        .collect::<Vec<_>>();
    for value in 0..=4 {
        assert!(
            unindexed_history
                .iter()
                .any(|datom| datom.value == Value::Long(value)),
            "changing :db/noHistory immediately changed history: {unindexed_history:?}"
        );
    }
    let expected_current = store.recover(&database_id).unwrap();

    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(&connection, &database_id, 16).unwrap();
    assert_eq!(peer.durable_base_t(), fourth.basis_t);
    assert_current_eq(&peer.db(), &expected_current);
    let retained = peer
        .snapshot()
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.entity == entity && datom.attribute == ITEM_COUNT)
        .collect::<Vec<_>>();
    // The future indexing job merges affected old and new segment data before
    // applying endpoint noHistory. It may therefore forget an adjacent pair
    // straddling the toggle/base boundary; there is no pre-toggle cutoff.
    assert!(!retained.iter().any(|datom| datom.value == Value::Long(2)));
    assert!(!retained.iter().any(|datom| datom.value == Value::Long(3)));
    assert!(
        retained
            .iter()
            .any(|datom| datom.value == Value::Long(4) && datom.added)
    );
}

#[test]
fn no_history_false_resumes_retention_across_a_pruned_base() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("nohistory_resume");
    let (mut store, entity) = populated(&connection, &database_id, true, 2);

    // Keep a full-log transactor value alive while an independent indexer
    // publishes a legally pair-pruned base. The v2 current-state commitment
    // must be independent of that background retention timing.
    let service = common::start_service(&connection, &database_id);
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let before = store.recover(&database_id).unwrap();
    let mut count = before.schema().attribute(ITEM_COUNT).unwrap().clone();
    count.no_history = false;
    let altered = common::transact(
        &service,
        "retain-history-again",
        before.basis_t(),
        &[TxOp::AlterAttribute(count)],
        2_000,
    );
    let third = common::transact(
        &service,
        "post-nohistory-3",
        altered.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(3)),
        }],
        2_001,
    );
    let fourth = common::transact(
        &service,
        "post-nohistory-4",
        third.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(4)),
        }],
        2_002,
    );
    service.shutdown();

    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(&connection, &database_id, 16).unwrap();
    assert_eq!(peer.durable_base_t(), fourth.basis_t);
    let retained = peer
        .snapshot()
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.entity == entity && datom.attribute == ITEM_COUNT)
        .collect::<Vec<_>>();
    assert!(!retained.iter().any(|datom| datom.value == Value::Long(1)));
    assert!(retained.iter().any(|datom| datom.value == Value::Long(2)));
    assert!(
        retained
            .iter()
            .any(|datom| datom.value == Value::Long(3) && datom.added)
    );
    assert!(
        retained
            .iter()
            .any(|datom| datom.value == Value::Long(3) && !datom.added)
    );
    assert!(
        retained
            .iter()
            .any(|datom| datom.value == Value::Long(4) && datom.added)
    );
}

#[test]
fn concurrent_builders_waiting_peer_and_postgres_restart_converge() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer_concurrency");
    let (mut store, entity) = populated(&connection, &database_id, false, 30);
    let mut basis = store.recover(&database_id).unwrap().basis_t();
    let mut before_client = Client::connect(&connection, NoTls).unwrap();
    let initial_revision: i64 = before_client
        .query_one(
            "SELECT COALESCE(max(publication_revision), 0) \
               FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    drop(before_client);

    // Leave one authoritative transaction beyond the existing base without
    // crossing the ordinary background byte threshold. This makes the two
    // builders race for a known next physical revision even if the population
    // service happened to index all earlier work before shutdown.
    let tail_service = common::start_service(&connection, &database_id);
    let unindexed = common::transact(
        &tail_service,
        "force-unindexed-builder-tail",
        basis,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(30)),
        }],
        4_000,
    );
    basis = unindexed.basis_t;
    tail_service.shutdown();
    let barrier = Arc::new(Barrier::new(3));
    let mut handles = Vec::new();
    for segment_datoms in [5, 7] {
        let connection = connection.clone();
        let database_id = database_id.clone();
        let barrier = Arc::clone(&barrier);
        handles.push(std::thread::spawn(move || {
            let mut indexer = PostgresIndexer::connect(&connection, database_id)
                .unwrap()
                .with_segment_datoms(segment_datoms)
                .unwrap();
            barrier.wait();
            indexer.consolidate()
        }));
    }
    barrier.wait();
    let receipts: Vec<_> = handles
        .into_iter()
        .map(|handle| handle.join().unwrap().unwrap())
        .collect();
    assert_eq!(receipts[0].manifest_hash, receipts[1].manifest_hash);
    assert_eq!(
        receipts[0].publication_revision,
        u64::try_from(initial_revision).unwrap() + 1
    );
    assert_eq!(
        receipts[1].publication_revision,
        receipts[0].publication_revision
    );

    let waiting_peer = Peer::connect(&connection, &database_id, 16).unwrap();
    let synchronizer = waiting_peer.clone();
    let observer = waiting_peer.clone();
    let old = waiting_peer.db();
    let reader_snapshot = Arc::clone(&old);
    let reader = std::thread::spawn(move || {
        for _ in 0..1_000 {
            reader_snapshot.validate_invariants().unwrap();
            assert_eq!(
                reader_snapshot.values(entity, ITEM_COUNT),
                vec![&Value::Long(30)]
            );
        }
    });
    let writer_connection = connection.clone();
    let writer_database = database_id.clone();
    let writer = std::thread::spawn(move || {
        std::thread::sleep(Duration::from_millis(50));
        let service = common::start_service(&writer_connection, &writer_database);
        let committed = common::transact(
            &service,
            "concurrent-tail",
            basis,
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long(500)),
            }],
            5_000,
        );
        service.shutdown();
        committed
    });
    let advanced = synchronizer
        .sync_to(basis + 1, Duration::from_secs(2))
        .unwrap();
    let committed = writer.join().unwrap();
    reader.join().unwrap();
    assert_current_eq(&advanced, &committed.db_after);
    assert_eq!(waiting_peer.basis_t(), committed.basis_t);
    assert_current_eq(&observer.db(), &committed.db_after);
    assert_eq!(old.basis_t(), basis);
    assert_eq!(old.values(entity, ITEM_COUNT), vec![&Value::Long(30)]);

    let pinned_snapshot = waiting_peer.snapshot();
    let pinned_clone = pinned_snapshot.clone();
    assert_eq!(waiting_peer.pinned_manifest_hashes().len(), 1);
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();
    assert!(waiting_peer.refresh_index().unwrap());
    assert_eq!(waiting_peer.pinned_manifest_hashes().len(), 2);
    drop(pinned_snapshot);
    assert_eq!(waiting_peer.pinned_manifest_hashes().len(), 2);
    drop(pinned_clone);
    assert_eq!(waiting_peer.pinned_manifest_hashes().len(), 1);
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let manifest_count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_tree_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    let latest_revision: i64 = client
        .query_one(
            "SELECT max(publication_revision) FROM atomic_tree_publications \
              WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        latest_revision,
        i64::try_from(receipts[0].publication_revision + 1).unwrap()
    );
    assert_eq!(manifest_count, latest_revision);
    drop(client);

    if let (Ok(pg_ctl), Ok(data_dir)) = (
        std::env::var("ATOMIC_POSTGRES_CTL"),
        std::env::var("ATOMIC_POSTGRES_DATA"),
    ) {
        assert!(
            Command::new(pg_ctl)
                .args(["-D", &data_dir, "-m", "fast", "-w", "restart"])
                .status()
                .unwrap()
                .success()
        );
    }
    let restarted = Peer::connect(&connection, &database_id, 16).unwrap();
    assert_eq!(restarted.durable_base_t(), committed.basis_t);
    assert_eq!(restarted.load_stats().root_reads, 8);
    assert!(restarted.load_stats().leaf_reads > 0);
    assert_eq!(restarted.load_stats().compatibility_materializations, 0);
    assert_current_eq(&restarted.db(), &committed.db_after);
}

#[test]
fn waiting_native_sync_does_not_block_lazy_snapshot_reads() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer_waiter_cache_lane");
    let (mut store, entity) = populated(&connection, &database_id, false, 12);
    let basis = store.recover(&database_id).unwrap().basis_t();
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(2)
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(&connection, &database_id, 16).unwrap();
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

    let waiter = peer.clone();
    let waiting =
        std::thread::spawn(move || waiter.sync_to_snapshot(basis + 1, Duration::from_secs(2)));
    std::thread::sleep(Duration::from_millis(50));

    let snapshot = peer.snapshot();
    let (sent, received) = std::sync::mpsc::channel();
    let reader = std::thread::spawn(move || {
        let result = snapshot.datoms_with_prefix(
            false,
            &IndexPrefix::Eavt {
                entity,
                attribute: Some(ITEM_COUNT),
                value: None,
            },
        );
        sent.send(result).unwrap();
    });
    let read = received
        .recv_timeout(Duration::from_millis(500))
        .expect("sync waiter held the peer I/O/cache mutex while sleeping")
        .unwrap();
    assert_eq!(read.datoms.len(), 1);
    reader.join().unwrap();

    let service = common::start_service(&connection, &database_id);
    let committed = common::transact(
        &service,
        "release-native-waiter",
        basis,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(13)),
        }],
        6_000,
    );
    service.shutdown();
    let advanced = waiting.join().unwrap().unwrap();
    assert_eq!(advanced.basis_t(), committed.basis_t);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
}

#[test]
fn avet_transition_waits_for_a_covering_native_publication() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer_avet_readiness");
    let mut application = Schema::new();
    application
        .install(
            Attribute::new(
                ITEM_NAME,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    application
        .install(Attribute::new(
            ITEM_COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, application).unwrap();
    let service = common::start_service(&connection, &database_id);
    let inserted = common::transact(
        &service,
        "unindexed-value",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ITEM_NAME,
                value: TxValue::Scalar(Value::String("ready".into())),
            },
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long(7)),
            },
        ],
        1_000,
    );
    let entity = inserted.tempids["item"];
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(&connection, &database_id, 16).unwrap();

    let before = store.recover(&database_id).unwrap();
    let mut count = before.schema().attribute(ITEM_COUNT).unwrap().clone();
    count.indexed = true;
    let altered = common::transact(
        &service,
        "enable-avet",
        before.basis_t(),
        &[TxOp::AlterAttribute(count)],
        1_001,
    );
    let unready = peer
        .sync_to_snapshot(altered.basis_t, Duration::from_secs(1))
        .unwrap();
    let prefix = IndexPrefix::Avet {
        attribute: ITEM_COUNT,
        value: None,
        entity: None,
    };
    assert_eq!(
        unready.datoms_with_prefix(false, &prefix).unwrap_err().code,
        "peer/avet-not-ready"
    );

    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    assert!(peer.refresh_index().unwrap());
    let ready = peer.snapshot().datoms_with_prefix(false, &prefix).unwrap();
    assert_eq!(ready.datoms.len(), 1);
    assert_eq!(ready.datoms[0].entity, entity);
    assert_eq!(ready.datoms[0].value, Value::Long(7));
    service.shutdown();
}

#[test]
fn failed_multi_row_tail_does_not_tear_peer_state_and_can_retry() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer_atomic_tail");
    let (mut store, entity) = populated(&connection, &database_id, false, 1);
    let initial = store.recover(&database_id).unwrap();
    drop(store);
    let peer = Peer::connect(&connection, &database_id, 8).unwrap();
    assert!(peer.take_tx_reports().is_empty());
    assert!(peer.enable_tx_reports());
    let initial_hash_basis = peer.basis_t();
    let service = common::start_service(&connection, &database_id);
    let first = common::transact(
        &service,
        "atomic-tail-first",
        initial.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(2)),
        }],
        2_000,
    );
    let second = common::transact(
        &service,
        "atomic-tail-second",
        first.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(3)),
        }],
        3_000,
    );
    service.shutdown();

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let basis_sql = i64::try_from(second.basis_t).unwrap();
    let original_state: Vec<u8> = client
        .query_one(
            "SELECT state_hash FROM atomic_transactions \
             WHERE database_id = $1 AND basis_t = $2",
            &[&database_id, &basis_sql],
        )
        .unwrap()
        .get(0);
    let mut corrupt_state = original_state.clone();
    corrupt_state[0] ^= 1;
    client
        .batch_execute("ALTER TABLE atomic_transactions DISABLE TRIGGER USER")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_transactions SET state_hash = $1 \
             WHERE database_id = $2 AND basis_t = $3",
            &[&corrupt_state, &database_id, &basis_sql],
        )
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_transactions ENABLE TRIGGER USER")
        .unwrap();

    let rejected = peer
        .sync_to(second.basis_t, Duration::from_secs(1))
        .unwrap_err();
    assert_eq!(rejected.category, atomic_core::ErrorCategory::Fault);
    assert!(matches!(
        rejected.code,
        "peer/tail-state-commitment-mismatch" | "recovery/state-commitment-mismatch"
    ));
    assert_eq!(peer.basis_t(), initial_hash_basis);
    assert_current_eq(&peer.db(), &initial);
    assert!(peer.take_tx_reports().is_empty());

    client
        .batch_execute("ALTER TABLE atomic_transactions DISABLE TRIGGER USER")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_transactions SET state_hash = $1 \
             WHERE database_id = $2 AND basis_t = $3",
            &[&original_state, &database_id, &basis_sql],
        )
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_transactions ENABLE TRIGGER USER")
        .unwrap();

    let retried = peer
        .sync_to(second.basis_t, Duration::from_secs(1))
        .unwrap();
    assert_current_eq(&retried, &second.db_after);
    assert_eq!(peer.take_tx_reports().len(), 2);
}

#[test]
fn peer_native_api_transacts_syncs_queries_navigates_and_pulls_one_basis() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("native_api");
    let (store, entity) = populated(&connection, &database_id, false, 1);
    drop(store);
    let peer = Peer::connect(&connection, &database_id, 8).unwrap();
    let old_entity = peer.entity(entity).unwrap().unwrap();
    let value = Variable::new("value").unwrap();
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable(value.clone())),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::Constant(Value::Ref(entity)),
            Term::Constant(Value::Keyword(Keyword::new("item", "count"))),
            Term::Variable(value),
        )))],
    );
    let old_database = old_entity.database();
    let old_query = query.clone();
    let old_reader = std::thread::spawn(move || {
        for _ in 0..1_000 {
            assert_eq!(
                old_database
                    .query(&old_query, &[], &QueryControl::default())
                    .unwrap()
                    .result,
                QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(1))))
            );
        }
    });
    let service = TransactionService::start(TransactionServiceConfig {
        connection: connection.clone(),
        database_id: database_id.clone(),
        holder_id: "native-api-transactor".into(),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: atomic_core::CapacityLimits::default(),
    })
    .unwrap();
    let receipt = service
        .client()
        .transact(
            TransactionRequest::new(
                "native-update",
                vec![TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: ITEM_COUNT,
                    value: TxValue::Scalar(Value::Long(77)),
                }],
            ),
            Duration::from_secs(2),
        )
        .unwrap();
    peer.sync_to(receipt.basis_t, Duration::from_secs(2))
        .unwrap();
    old_reader.join().unwrap();
    assert_eq!(peer.basis_t(), receipt.basis_t);
    assert!(matches!(
        old_entity.get(ITEM_COUNT).unwrap(),
        Some(EntityValue::Scalar(Value::Long(1)))
    ));
    service.shutdown();

    assert_eq!(
        peer.query(&query, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(77))))
    );
    let pulled = peer
        .pull(
            &PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(ITEM_COUNT))]),
            entity,
        )
        .unwrap();
    assert_eq!(
        pulled,
        QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Keyword(Keyword::new("item", "count"))),
            QueryValue::Scalar(Value::Long(77))
        )])
    );
}

#[test]
fn native_entity_and_pull_pin_one_snapshot_without_compatibility_materialization() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("native_entity_pull");
    let (_store, entity) = populated(&connection, &database_id, false, 1);
    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(1)
        .unwrap();
    let built = indexer.consolidate().unwrap();

    // Disable the shared decoded-node cache so the measured reads below are
    // actual tree path reads rather than accidental metadata-cache hits.
    let peer = Peer::connect_with_cache_limits(&connection, &database_id, 0, 0).unwrap();
    let before_reads = peer.load_stats();
    assert_eq!(before_reads.compatibility_materializations, 0);
    let old_basis = peer.basis_t();
    let old_snapshot = peer.snapshot();
    let old_entity = old_snapshot.entity(entity).unwrap().unwrap();
    assert_eq!(old_entity.database().basis_t(), old_basis);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

    let service = common::start_service(&connection, &database_id);
    let updated = common::transact(
        &service,
        "native-entity-pull-update",
        old_basis,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(77)),
        }],
        9_000,
    );
    let current_snapshot = peer
        .sync_to_snapshot(updated.basis_t, Duration::from_secs(2))
        .unwrap();
    assert_eq!(current_snapshot.basis_t(), updated.basis_t);

    // The old entity had not realized this attribute before connection
    // advancement. Its first read must therefore traverse the old roots, not
    // a cache populated before the update.
    assert!(matches!(
        old_entity.get(ITEM_COUNT).unwrap(),
        Some(EntityValue::Scalar(Value::Long(1)))
    ));
    let pattern =
        PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(ITEM_COUNT))]);
    assert_eq!(
        old_snapshot.pull(&pattern, entity).unwrap(),
        QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Keyword(Keyword::new("item", "count"))),
            QueryValue::Scalar(Value::Long(1)),
        )])
    );

    let current_entity = current_snapshot.entity(entity).unwrap().unwrap();
    assert!(matches!(
        current_entity.get(ITEM_COUNT).unwrap(),
        Some(EntityValue::Scalar(Value::Long(77)))
    ));
    assert_eq!(
        peer.pull(&pattern, entity).unwrap(),
        QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Keyword(Keyword::new("item", "count"))),
            QueryValue::Scalar(Value::Long(77)),
        )])
    );

    let after_reads = peer.load_stats();
    assert_eq!(after_reads.compatibility_materializations, 0);
    assert_eq!(after_reads.compatibility_hits, 0);
    let path_reads = after_reads
        .directory_reads
        .saturating_sub(before_reads.directory_reads)
        .saturating_add(
            after_reads
                .leaf_reads
                .saturating_sub(before_reads.leaf_reads),
        );
    assert!(path_reads > 0);
    // Prefix routing may inspect one predecessor directory/leaf before the
    // matching directory/leaf, so four uncached point reads are bounded by
    // sixteen decoded nodes rather than the size of any full tree.
    assert!(
        path_reads <= 16,
        "four point reads used {path_reads} tree nodes"
    );
    assert!(path_reads < built.segment_count as u64);
    service.shutdown();
}

#[test]
fn native_queries_pin_exact_snapshots_and_read_bounded_tree_paths() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("native_query_snapshot");
    let (_store, entity) = populated(&connection, &database_id, false, 1);
    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(1)
        .unwrap();
    let built = indexer.consolidate().unwrap();

    // No decoded-node cache: every count below represents an actual bounded
    // persistent-tree path rather than a process-local cache hit.
    let peer = Peer::connect_with_cache_limits(&connection, &database_id, 0, 0).unwrap();
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    let old_snapshot = peer.snapshot();
    let old_basis = old_snapshot.basis_t();
    let value = Variable::new("value").unwrap();
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable(value.clone())),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::Constant(Value::Ref(entity)),
            Term::Constant(Value::Keyword(Keyword::new("item", "count"))),
            Term::Variable(value),
        )))],
    );

    let service = common::start_service(&connection, &database_id);
    let updated = common::transact(
        &service,
        "native-query-update",
        old_basis,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(77)),
        }],
        9_000,
    );
    let current_snapshot = peer
        .sync_to_snapshot(updated.basis_t, Duration::from_secs(2))
        .unwrap();
    let before_queries = peer.load_stats();
    assert_eq!(before_queries.compatibility_materializations, 0);

    let old = old_snapshot
        .query(&query, &[], &QueryControl::default())
        .unwrap();
    assert_eq!(
        old.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(1))))
    );
    let current = peer.query(&query, &[], &QueryControl::default()).unwrap();
    assert_eq!(
        current.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(77))))
    );

    // Temporal and custom windows are evaluated over the same captured native
    // value; neither path may fall back to the peer's current eager oracle.
    let mut named_as_of_query = query.clone();
    let Clause::Pattern(pattern) = &mut named_as_of_query.clauses[0] else {
        unreachable!("test query has one data pattern")
    };
    pattern.source = "$old".into();
    let as_of = QueryEngine::execute(
        &named_as_of_query,
        &[QuerySource {
            name: "$old".into(),
            database: current_snapshot.database_value().as_of(old_basis),
        }],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    assert_eq!(
        as_of.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(1))))
    );
    let bad_t = updated.basis_t;
    let filtered = current_snapshot
        .database_value()
        .filter(move |_, datom| atomic_core::tx_to_t(datom.tx).unwrap() != bad_t)
        .query(&query, &[], &QueryControl::default())
        .unwrap();
    assert_eq!(
        filtered.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(1))))
    );
    for outcome in [&old, &current, &as_of, &filtered] {
        assert_eq!(outcome.stats.index_seeks, 1);
        assert_eq!(outcome.stats.datoms_examined, 1);
    }

    let after_queries = peer.load_stats();
    assert_eq!(after_queries.compatibility_materializations, 0);
    assert_eq!(after_queries.compatibility_hits, 0);
    let path_reads = after_queries
        .directory_reads
        .saturating_sub(before_queries.directory_reads)
        .saturating_add(
            after_queries
                .leaf_reads
                .saturating_sub(before_queries.leaf_reads),
        );
    assert!(path_reads > 0);
    // Prefix routing can inspect one predecessor directory/leaf. Four uncached
    // selective queries therefore remain bounded by sixteen decoded nodes,
    // independent of the full tree's segment count.
    assert!(
        path_reads <= 16,
        "four selective queries used {path_reads} tree nodes"
    );
    assert!(path_reads < built.segment_count as u64);

    // Persisted extensions are query programs, but their database reads must
    // still use the exact DatabaseValue captured by the caller. This catches
    // a particularly subtle regression where the outer query is native while
    // the extension re-enters through the peer's eager compatibility value.
    let hash = [0x51; 32];
    let mut extensions = QueryExtensions::new();
    extensions
        .register_program(
            "native-count",
            hash,
            Program {
                kind: ProgramKind::Query,
                arity: 1,
                instructions: vec![
                    Instruction::PushArgument(0),
                    Instruction::LoadOne(ITEM_COUNT),
                    Instruction::Return,
                ],
            },
        )
        .unwrap();
    let extension_value = Variable::new("extension-value").unwrap();
    let extension_query = Query {
        find: FindSpec::Scalar(FindElement::Variable(extension_value.clone())),
        with: Vec::new(),
        inputs: vec![InputSpec::Scalar(Variable::from("target"))],
        clauses: vec![Clause::Function {
            function: Function::Extension("native-count".into()),
            source: "$".into(),
            args: vec![Term::var("target")],
            binding: Binding::Scalar(extension_value),
        }],
        rules: Vec::new(),
    };
    let extension_inputs = [QueryInput::Scalar(Value::Ref(entity))];
    let old_extension = old_snapshot
        .query_with_extensions(
            &extension_query,
            &extension_inputs,
            &QueryControl::default(),
            &extensions,
        )
        .unwrap();
    assert_eq!(
        old_extension.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(1))))
    );
    let current_extension = peer
        .query_with_extensions(
            &extension_query,
            &extension_inputs,
            &QueryControl::default(),
            &extensions,
        )
        .unwrap();
    assert_eq!(
        current_extension.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(77))))
    );
    let hash_hex = hash
        .iter()
        .map(|byte| format!("{byte:02x}"))
        .collect::<String>();
    assert!(old_extension.plan[0].access.contains(&hash_hex));
    assert!(current_extension.plan[0].access.contains(&hash_hex));

    let after_extensions = peer.load_stats();
    assert_eq!(after_extensions.root_reads, before_queries.root_reads);
    assert_eq!(after_extensions.compatibility_materializations, 0);
    assert_eq!(after_extensions.compatibility_hits, 0);
    service.shutdown();
}

#[test]
fn generated_queries_match_eager_native_and_force_scan_references() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("native_query_differential");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema(false)).unwrap();
    let service = common::start_service(&connection, &database_id);

    // Generate enough connected facts to exercise AVET selection, joins, and
    // history without introducing a separate property-test harness. The data
    // and query sequence are fixed so a failure is exactly reproducible.
    const ENTITY_COUNT: usize = 12;
    let mut seed_ops = Vec::with_capacity(ENTITY_COUNT * 3);
    for ordinal in 0..ENTITY_COUNT {
        let entity = format!("node-{ordinal}");
        let parent = format!("node-{}", (ordinal + 1) % ENTITY_COUNT);
        seed_ops.extend([
            TxOp::Add {
                entity: EntityRef::Temp(entity.clone()),
                attribute: ITEM_NAME,
                value: TxValue::Scalar(Value::String(format!("{database_id}-node-{ordinal}"))),
            },
            TxOp::Add {
                entity: EntityRef::Temp(entity.clone()),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long((ordinal % 4) as i64)),
            },
            TxOp::Add {
                entity: EntityRef::Temp(entity),
                attribute: ITEM_PARENT,
                value: TxValue::Entity(EntityRef::Temp(parent)),
            },
        ]);
    }
    let seeded = common::transact(
        &service,
        "differential-seed",
        created.basis_t(),
        &seed_ops,
        10_000,
    );
    let entities = (0..ENTITY_COUNT)
        .map(|ordinal| seeded.tempids[&format!("node-{ordinal}")])
        .collect::<Vec<_>>();

    let updates = (0..ENTITY_COUNT)
        .filter(|ordinal| ordinal % 3 == 0)
        .map(|ordinal| TxOp::Add {
            entity: EntityRef::Id(entities[ordinal]),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(100 + ordinal as i64)),
        })
        .collect::<Vec<_>>();
    let updated = common::transact(
        &service,
        "differential-updates",
        seeded.basis_t,
        &updates,
        10_001,
    );
    let retracted = common::transact(
        &service,
        "differential-retract",
        updated.basis_t,
        &[TxOp::Retract {
            entity: EntityRef::Id(entities[5]),
            attribute: ITEM_COUNT,
            value: None,
        }],
        10_002,
    );
    service.shutdown();

    let eager_database = store.recover(&database_id).unwrap();
    assert_eq!(eager_database.basis_t(), retracted.basis_t);
    let mut indexer = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .with_segment_datoms(4)
        .unwrap();
    let publication = indexer.consolidate().unwrap();
    assert_eq!(publication.basis_t, retracted.basis_t);
    let peer = Peer::connect(&connection, &database_id, 32).unwrap();
    let native = peer.database_value();
    let eager = eager_database.database_value();
    assert_eq!(native.basis_t(), eager.basis_t());
    assert_eq!(
        native.datoms(IndexOrder::Eavt).unwrap(),
        eager.datoms(IndexOrder::Eavt).unwrap()
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

    let variable = |name: &str| Variable::new(name).unwrap();
    let attribute = |name: &str| Term::Constant(Value::Keyword(Keyword::new("item", name)));
    let pattern = |entity, name, value| {
        Clause::Pattern(Box::new(DataPattern::new(entity, attribute(name), value)))
    };

    // Generate several selective cases, including values affected and not
    // affected by cardinality-one replacement and explicit retraction.
    for wanted in 0..4 {
        let entity = variable("entity");
        let query = Query::new(
            FindSpec::Relation(vec![FindElement::Variable(entity.clone())]),
            vec![pattern(
                Term::Variable(entity),
                "count",
                Term::Constant(Value::Long(wanted)),
            )],
        );
        let outcomes = assert_eager_native_query_differential(
            &format!("selective-count-{wanted}"),
            &query,
            eager.clone(),
            native.clone(),
        );
        assert!(outcomes[0].stats.datoms_examined < outcomes[1].stats.datoms_examined);
        assert!(outcomes[2].stats.datoms_examined < outcomes[3].stats.datoms_examined);
    }

    let child = variable("child");
    let parent = variable("parent");
    let parent_name = variable("parent-name");
    let join = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(child.clone()),
            FindElement::Variable(parent_name.clone()),
        ]),
        vec![
            pattern(
                Term::Variable(child.clone()),
                "count",
                Term::Constant(Value::Long(2)),
            ),
            pattern(
                Term::Variable(child),
                "parent",
                Term::Variable(parent.clone()),
            ),
            pattern(Term::Variable(parent), "name", Term::Variable(parent_name)),
        ],
    );
    let join_outcomes = assert_eager_native_query_differential(
        "three-pattern-join",
        &join,
        eager.clone(),
        native.clone(),
    );
    assert!(join_outcomes[0].stats.datoms_examined < join_outcomes[1].stats.datoms_examined);
    assert!(join_outcomes[2].stats.datoms_examined < join_outcomes[3].stats.datoms_examined);

    let entity = variable("entity");
    let count = variable("count");
    let name = variable("name");
    let temporal_filtered = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(entity.clone()),
            FindElement::Variable(count.clone()),
            FindElement::Variable(name.clone()),
        ]),
        vec![
            pattern(
                Term::Variable(entity.clone()),
                "count",
                Term::Variable(count),
            ),
            pattern(Term::Variable(entity), "name", Term::Variable(name)),
        ],
    );
    let excluded = entities[4];
    let eager_temporal = eager
        .clone()
        .as_of(seeded.basis_t)
        .filter(move |_, datom| datom.entity != excluded);
    let native_temporal = native
        .clone()
        .as_of(seeded.basis_t)
        .filter(move |_, datom| datom.entity != excluded);
    let temporal_outcomes = assert_eager_native_query_differential(
        "as-of-plus-filter",
        &temporal_filtered,
        eager_temporal,
        native_temporal,
    );
    let mut expected_temporal_rows = entities
        .iter()
        .enumerate()
        .filter(|(ordinal, _)| *ordinal != 4)
        .map(|(ordinal, entity)| {
            vec![
                QueryValue::Scalar(Value::Ref(*entity)),
                QueryValue::Scalar(Value::Long((ordinal % 4) as i64)),
                QueryValue::Scalar(Value::String(format!("{database_id}-node-{ordinal}"))),
            ]
        })
        .collect::<Vec<_>>();
    expected_temporal_rows.sort_by(|left, right| left[0].canonical_cmp(&right[0]));
    assert_eq!(
        temporal_outcomes[0].result,
        QueryResult::Relation(expected_temporal_rows)
    );

    let historical_count = variable("historical-count");
    let tx = variable("tx");
    let added = variable("added");
    let mut history_pattern = DataPattern::new(
        Term::Blank,
        attribute("count"),
        Term::Variable(historical_count.clone()),
    );
    history_pattern.transaction = Some(Term::Variable(tx.clone()));
    history_pattern.added = Some(Term::Variable(added.clone()));
    let history = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(historical_count),
            FindElement::Variable(tx),
            FindElement::Variable(added),
        ]),
        vec![Clause::Pattern(Box::new(history_pattern))],
    );
    let history_outcomes = assert_eager_native_query_differential(
        "raw-history",
        &history,
        eager.history(),
        native.history(),
    );
    let QueryResult::Relation(history_rows) = &history_outcomes[0].result else {
        panic!("history differential must return a relation")
    };
    assert!(
        history_rows
            .iter()
            .any(|row| { row[2] == QueryValue::Scalar(Value::Bool(false)) })
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    assert_eq!(peer.load_stats().compatibility_hits, 0);
}
