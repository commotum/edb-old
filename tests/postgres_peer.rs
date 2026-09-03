use atomic_core::{
    Attribute, AttributeName, Cardinality, Clause, DataPattern, Database, EntityRef, FindElement,
    FindSpec, IndexBuildFault, IndexOrder, IndexPrefix, Keyword, Peer, PostgresIndexer,
    PostgresStore, PullAttribute, PullPattern, Query, QueryControl, QueryResult, QueryValue,
    Schema, Term, TxOp, TxValue, Unique, Value, ValueType, Variable, View, decode_index_manifest,
    decode_index_segment,
};
use postgres::{Client, NoTls};
use std::process::Command;
use std::sync::{Arc, Barrier};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

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
        .install(Attribute::new(
            1,
            Keyword::new("db", "txInstant"),
            ValueType::Instant,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                10,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    let mut count = Attribute::new(
        11,
        Keyword::new("item", "count"),
        ValueType::Long,
        Cardinality::One,
    );
    count.indexed = true;
    count.no_history = no_history;
    schema.install(count).unwrap();
    let mut parent = Attribute::new(
        12,
        Keyword::new("item", "parent"),
        ValueType::Ref,
        Cardinality::One,
    );
    parent.indexed = true;
    schema.install(parent).unwrap();
    schema
}

fn assert_current_eq(left: &Database, right: &Database) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.next_eid(), right.next_eid());
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            left.datoms(View::Current, order),
            right.datoms(View::Current, order)
        );
    }
}

fn populated(
    connection: &str,
    database_id: &str,
    no_history: bool,
    updates: i64,
) -> (PostgresStore, u64) {
    let mut store = PostgresStore::connect(connection).unwrap();
    store.migrate().unwrap();
    store
        .create_database(database_id, schema(no_history))
        .unwrap();
    let first = store
        .transact(
            database_id,
            "create",
            0,
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: 10,
                    value: TxValue::Scalar(Value::String(database_id.into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: 11,
                    value: TxValue::Scalar(Value::Long(0)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: 12,
                    value: TxValue::Entity(EntityRef::Temp("item".into())),
                },
            ],
            1_000,
        )
        .unwrap();
    let entity = first.tempids["item"];
    let mut basis = 1;
    for value in 1..=updates {
        let receipt = store
            .transact(
                database_id,
                &format!("update-{value}"),
                basis,
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 11,
                    value: TxValue::Scalar(Value::Long(value)),
                }],
                1_000 + value,
            )
            .unwrap();
        basis = receipt.basis_t;
    }
    (store, entity)
}

#[test]
fn peers_use_verified_base_tail_and_keep_old_snapshots() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer");
    let (mut store, entity) = populated(&connection, &database_id, false, 8);
    let expected = store.recover(&database_id).unwrap();

    let mut before_index = Peer::connect(&connection, &database_id, 2).unwrap();
    let old = before_index.db();
    assert_eq!(before_index.durable_base_t(), 0);
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
    assert_eq!(old.values(entity, 11), vec![&Value::Long(8)]);

    let mut cache_peer = Peer::connect(&connection, &database_id, built.segment_count + 1).unwrap();
    let before_refresh = cache_peer.cache_stats();
    assert!(!cache_peer.refresh_index().unwrap());
    assert!(cache_peer.cache_stats().hits > before_refresh.hits);

    let mut peer = Peer::connect(&connection, &database_id, 2).unwrap();
    assert_eq!(peer.durable_base_t(), expected.basis_t());
    assert_current_eq(&peer.db(), &expected);
    assert!(peer.cache_stats().misses > 0);
    assert!(peer.cache_stats().evictions > 0);

    let committed = store
        .transact(
            &database_id,
            "after-index",
            expected.basis_t(),
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: 11,
                value: TxValue::Scalar(Value::Long(99)),
            }],
            2_000,
        )
        .unwrap();
    assert_eq!(old.basis_t(), expected.basis_t());
    assert_eq!(old.values(entity, 11), vec![&Value::Long(8)]);
    let advanced = peer
        .sync_to(committed.basis_t, Duration::from_secs(1))
        .unwrap();
    assert_current_eq(&advanced, &committed.database);
    assert_eq!(
        peer.take_tx_reports().last().unwrap().basis_t,
        committed.basis_t
    );
    assert_current_eq(&before_index.sync().unwrap(), &committed.database);

    let prefix = IndexPrefix::Eavt {
        entity,
        attribute: Some(11),
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
fn interrupted_build_is_invisible_and_corrupt_derived_data_falls_back_to_log() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("index_fault");
    let (mut store, _) = populated(&connection, &database_id, false, 3);
    let expected = store.recover(&database_id).unwrap();
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
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_index_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(count, 0);
    indexer.consolidate().unwrap();

    let manifest_payload: Vec<u8> = client
        .query_one(
            "SELECT payload FROM atomic_index_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    let manifest = decode_index_manifest(&manifest_payload).unwrap();
    let corrupt_hash = manifest
        .segments
        .iter()
        .find_map(|reference| {
            let payload: Vec<u8> = client
                .query_one(
                    "SELECT payload FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&reference.hash[..]],
                )
                .unwrap()
                .get(0);
            decode_index_segment(&payload)
                .unwrap()
                .datoms
                .iter()
                .any(|datom| datom.value == Value::String(database_id.clone()))
                .then_some(reference.hash)
        })
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_index_segments DISABLE TRIGGER USER")
        .unwrap();
    client.execute(
        "UPDATE atomic_index_segments SET payload = set_byte(payload, 16, get_byte(payload, 16) # 1) \
         WHERE segment_hash = $1", &[&&corrupt_hash[..]],
    ).unwrap();
    client
        .batch_execute("ALTER TABLE atomic_index_segments ENABLE TRIGGER USER")
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
        .filter(|d| d.entity == entity && d.attribute == 11)
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
        .db()
        .datoms(View::History, IndexOrder::Eavt)
        .into_iter()
        .filter(|d| d.entity == entity && d.attribute == 11)
        .collect();
    assert_eq!(retained.len(), 1);
    assert!(retained[0].added);
    assert_eq!(retained[0].value, Value::Long(5));
}

#[test]
fn concurrent_builders_waiting_peer_and_postgres_restart_converge() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("peer_concurrency");
    let (mut store, entity) = populated(&connection, &database_id, false, 30);
    let basis = store.recover(&database_id).unwrap().basis_t();
    let barrier = Arc::new(Barrier::new(3));
    let mut handles = Vec::new();
    for _ in 0..2 {
        let connection = connection.clone();
        let database_id = database_id.clone();
        let barrier = Arc::clone(&barrier);
        handles.push(std::thread::spawn(move || {
            let mut indexer = PostgresIndexer::connect(&connection, database_id)
                .unwrap()
                .with_segment_datoms(7)
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

    let mut waiting_peer = Peer::connect(&connection, &database_id, 16).unwrap();
    let old = waiting_peer.db();
    let reader_snapshot = Arc::clone(&old);
    let reader = std::thread::spawn(move || {
        for _ in 0..1_000 {
            reader_snapshot.validate_invariants().unwrap();
            assert_eq!(reader_snapshot.values(entity, 11), vec![&Value::Long(30)]);
        }
    });
    let writer_connection = connection.clone();
    let writer_database = database_id.clone();
    let writer = std::thread::spawn(move || {
        std::thread::sleep(Duration::from_millis(50));
        let mut writer = PostgresStore::connect(&writer_connection).unwrap();
        writer
            .transact(
                &writer_database,
                "concurrent-tail",
                basis,
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 11,
                    value: TxValue::Scalar(Value::Long(500)),
                }],
                5_000,
            )
            .unwrap()
    });
    let advanced = waiting_peer
        .sync_to(basis + 1, Duration::from_secs(2))
        .unwrap();
    let committed = writer.join().unwrap();
    reader.join().unwrap();
    assert_current_eq(&advanced, &committed.database);
    assert_eq!(old.basis_t(), basis);
    assert_eq!(old.values(entity, 11), vec![&Value::Long(30)]);

    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let manifest_count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_index_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(manifest_count, 2);
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
    assert_current_eq(&restarted.db(), &committed.database);
}

#[test]
fn peer_native_api_transacts_syncs_queries_navigates_and_pulls_one_basis() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("native_api");
    let (store, entity) = populated(&connection, &database_id, false, 1);
    drop(store);
    let mut peer = Peer::connect(&connection, &database_id, 8).unwrap();
    let old_entity = peer.entity(entity);
    let before = peer.basis_t();
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
    let receipt = peer
        .transact(
            "native-update",
            before,
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: 11,
                value: TxValue::Scalar(Value::Long(77)),
            }],
            9_000,
        )
        .unwrap();
    old_reader.join().unwrap();
    assert_eq!(peer.basis_t(), receipt.basis_t);
    assert_eq!(
        old_entity.get(11).unwrap(),
        Some(QueryValue::Scalar(Value::Long(1)))
    );

    assert_eq!(
        peer.query(&query, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(77))))
    );
    let pulled = peer
        .pull(
            &PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(11))]),
            entity,
        )
        .unwrap();
    assert_eq!(
        pulled,
        QueryValue::Map(vec![(
            Keyword::new("item", "count"),
            QueryValue::Scalar(Value::Long(77))
        )])
    );
}
