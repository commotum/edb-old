//! Public immutable log/query adapters over the opaque provider, not SQL logs.
mod common;

use atomic_core::edn_query::{EdnQueryArgument, parse_query_edn};
use atomic_core::persistent_tree::{TreeConfig, build_tree};
use atomic_core::storage::log::{LogEntry, LogRoot};
use atomic_core::storage::root::DatabaseValueRoot;
use atomic_core::storage::{
    BlockReadConfig, BlockReader, CasOutcome, IndexDescriptor, PgBlockStore, SnapshotMetadata,
};
use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, IndexOrder, IndexTransaction, Keyword,
    OperationContext, OperationKind, PostgresConnectionConfig, QueryControl, QueryResult,
    QuerySourceValue, QueryValue, TimePoint, TxOp, TxReport, Value, ValueType, View,
};
use postgres::{Client, NoTls};

const COUNT: u32 = 1_000;
const RESERVED: u64 = 1_001;
const IDENTITY: [u8; 16] = [29; 16];
const ROOT: &str = "databases/log-adapter";

fn reports() -> Vec<TxReport> {
    let mut attribute = Attribute::new(
        COUNT,
        Keyword::new("item", "count"),
        ValueType::Long,
        Cardinality::One,
    );
    attribute.no_history = true;
    let schema = Database::bootstrap()
        .unwrap()
        .with(&[TxOp::InstallAttribute(attribute)], 10)
        .unwrap();
    let first = schema
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: COUNT,
                value: Value::Long(10).into(),
            }],
            1_000,
        )
        .unwrap();
    let item = first.tempids["item"];
    let mut reports = vec![schema, first];
    for (count, instant) in [(20, 1_000), (30, 2_000)] {
        let report = reports
            .last()
            .unwrap()
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(item),
                    attribute: COUNT,
                    value: Value::Long(count).into(),
                }],
                instant,
            )
            .unwrap();
        reports.push(report);
    }
    reports
}

fn append(store: &mut PgBlockStore, log: &LogRoot, report: &TxReport) -> LogRoot {
    log.append(
        store,
        &LogEntry {
            basis_t: report.db_after.basis_t(),
            eidx_frontier: report.db_after.eidx_frontier(),
            reserved_frontier: RESERVED,
            tx_data: report.tx_data.clone(),
        },
    )
    .unwrap()
}

fn publish(store: &mut PgBlockStore, report: &TxReport, log: &LogRoot) {
    let db = &report.db_after;
    let mut trees = Vec::new();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let mut datoms = db.datoms(
                if history {
                    View::History
                } else {
                    View::Current
                },
                order,
            );
            // Model a consolidated noHistory index. The transaction log must
            // still return the original additions/retractions independently.
            if history {
                datoms.retain(|d| {
                    d.attribute != COUNT
                        || (d.added && d.tx == atomic_core::t_to_tx(db.basis_t()).unwrap())
                });
            }
            let built = build_tree(order, history, datoms, &TreeConfig::default()).unwrap();
            for (id, bytes) in built.nodes.iter() {
                assert_eq!(store.put(bytes).unwrap(), *id);
            }
            trees.push(built.descriptor);
        }
    }
    let indexes = store
        .put(
            &IndexDescriptor {
                identity: IDENTITY,
                basis: db.basis_t(),
                generation: 0,
                trees,
                pending_avet: vec![],
                avet_work: vec![],
                fulltext: None,
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let metadata = store
        .put(
            &SnapshotMetadata {
                identity: IDENTITY,
                basis: db.basis_t(),
                generation: 0,
                eidx_frontier: db.eidx_frontier(),
                reserved_frontier: RESERVED,
                last_tx_instant: db.database_value().last_tx_instant().unwrap(),
                excision: None,
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let root = store
        .put(
            &DatabaseValueRoot {
                identity: IDENTITY,
                basis: db.basis_t(),
                log: log.head(),
                indexes: Some(indexes),
                metadata: Some(metadata),
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let expected = store.read_ref(ROOT).unwrap().map(|r| r.revision);
    assert!(matches!(
        store.compare_exchange(ROOT, expected, Some(&root)).unwrap(),
        CasOutcome::Applied(_)
    ));
}

#[test]
fn block_log_supports_public_queries_exact_time_bounds_and_lazy_authenticated_cursors() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "block_native_log");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reports = reports();
    let mut root = LogRoot::empty();
    for report in &reports[..3] {
        root = append(&mut store, &root, report);
    }
    publish(&mut store, &reports[2], &root);
    let reader = BlockReader::connect(&config, BlockReadConfig::default()).unwrap();
    let captured = reader.capture(ROOT).unwrap();
    let old_log = captured.log();
    root = append(&mut store, &root, &reports[3]);
    publish(&mut store, &reports[3], &root);
    let latest = reader.capture(ROOT).unwrap();
    let log = latest.log();
    assert_eq!(old_log.basis_t(), 3);
    assert_eq!(log.basis_t(), 4);
    assert_eq!(log.generation(), 0);
    assert_eq!(old_log.tx_data(IndexTransaction::T(4)).unwrap(), None);
    assert_eq!(log.tx_data(IndexTransaction::T(0)).unwrap(), None);
    assert_eq!(log.tx_data(IndexTransaction::T(5)).unwrap(), None);
    assert_eq!(
        log.tx_data(IndexTransaction::T(2)).unwrap(),
        Some(reports[1].tx_data.clone())
    );
    assert_eq!(
        log.tx_data(IndexTransaction::Tx(atomic_core::t_to_tx(3).unwrap()))
            .unwrap(),
        Some(reports[2].tx_data.clone())
    );
    assert!(
        latest
            .database_value()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .all(|d| d.attribute != COUNT || d.value != Value::Long(10))
    );

    let txs = |start, end| {
        log.tx_range(start, end)
            .unwrap()
            .map(|tx| tx.unwrap().t)
            .collect::<Vec<_>>()
    };
    assert_eq!(
        txs(
            Some(TimePoint::Instant(1_000)),
            Some(TimePoint::Instant(2_000))
        ),
        vec![2, 3]
    );
    assert_eq!(txs(Some(TimePoint::Instant(1_001)), None), vec![4]);
    assert!(txs(Some(TimePoint::Instant(2_001)), None).is_empty());
    assert!(txs(Some(TimePoint::T(4)), Some(TimePoint::T(2))).is_empty());
    assert_eq!(
        log.tx_ids(
            Some(TimePoint::Tx(atomic_core::t_to_tx(2).unwrap())),
            Some(TimePoint::T(4))
        )
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap(),
        vec![
            atomic_core::t_to_tx(2).unwrap(),
            atomic_core::t_to_tx(3).unwrap()
        ]
    );
    let result = parse_query_edn("[:find [?value ...] :in $log :where [(tx-ids $log 0 nil) [?tx ...]] [(tx-data $log ?tx) [[?e ?a ?value ?tx ?added]]] [(= ?a 1000)] [(= ?added true)]]")
        .unwrap()
        .bind(&[EdnQueryArgument::Source(QuerySourceValue::Log(log.clone()))])
        .unwrap()
        .execute(&QueryControl::default(), None)
        .unwrap()
        .result;
    assert_eq!(
        result,
        QueryResult::Collection(vec![
            QueryValue::Scalar(Value::Long(10)),
            QueryValue::Scalar(Value::Long(20)),
            QueryValue::Scalar(Value::Long(30)),
        ])
    );

    let context = OperationContext::new(OperationKind::Query);
    let scope = context.enter();
    let mut cursor = log
        .tx_range(Some(TimePoint::T(2)), Some(TimePoint::T(4)))
        .unwrap();
    assert_eq!(
        context.snapshot().sql_calls,
        0,
        "integer bounds must remain lazy"
    );
    drop(scope);
    assert_eq!(cursor.next().unwrap().unwrap().t, 2);
    assert_eq!(cursor.stats().transactions_read, 1);
    assert_eq!(cursor.stats().peak_buffered_transactions, 1);
    assert!(cursor.stats().postgres_payload_bytes_read > 0);
    assert!(
        context.snapshot().sql_calls > 0,
        "cursor retains operation attribution after creation scope"
    );
    drop(cursor);

    let record = root.read_record(&mut store, 3).unwrap().unwrap();
    let mut admin = Client::connect(&fixture.connection, NoTls).unwrap();
    admin
        .execute(
            "UPDATE atomic_objects SET payload = payload || decode('00', 'hex') WHERE id = $1",
            &[&&record.id[..]],
        )
        .unwrap();
    let mut failed = log.tx_range(Some(TimePoint::T(2)), None).unwrap();
    assert_eq!(failed.next().unwrap().unwrap().t, 2);
    assert_eq!(
        failed.next().unwrap().unwrap_err().code,
        "storage/object-corrupt"
    );
    assert!(failed.next().is_none());
    assert_eq!(failed.stats().range_reads, 2);
    drop(failed);
    drop(log);
    drop(old_log);
    latest.release().unwrap();
    captured.release().unwrap();
}
