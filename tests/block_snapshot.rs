//! Application-library acceptance on fresh opaque PostgreSQL storage.
//! The eager kernel builds expected fixture information, never the read path.
#[path = "support/block_snapshot_cases.rs"]
mod cases;
mod common;

use atomic_core::edn::read_edn;
use atomic_core::edn_pull::parse_pull_edn;
use atomic_core::edn_query::{EdnQueryArgument, parse_query_edn};
use atomic_core::persistent_tree::{TreeConfig, build_tree};
use atomic_core::storage::log::{LogEntry, LogRoot};
use atomic_core::storage::root::DatabaseValueRoot;
use atomic_core::storage::{
    BlockReadConfig, BlockReader, BlockSnapshot, CasOutcome, IndexDescriptor, ObjectId,
    PgBlockStore, SnapshotMetadata,
};
use atomic_core::{
    Attribute, Cardinality, Database, DatabaseValue, EntityRef, ErrorCategory, IndexBoundary,
    IndexComponents, IndexOrder, Keyword, OperationContext, OperationKind,
    PostgresConnectionConfig, QueryControl, QueryResult, QuerySourceValue, TxOp, TxReport, TxValue,
    Unique, Value, ValueType, View,
};
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::Instant;

const SCORE: u32 = 1000;
const NAME: u32 = 1001;
const FRIEND: u32 = 1002;
const RESERVED: u64 = 1003;
const IDENTITY: [u8; 16] = [19; 16];
const ROOT: &str = "databases/application";
const LARGE_READ_BATCH: usize = 1024;

fn fixture(label: &str) -> Option<(common::PostgresFixture, PostgresConnectionConfig)> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return None;
    };
    let fixture = common::PostgresFixture::new(&connection, label);
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    Some((fixture, config))
}

fn open_snapshot(
    config: &PostgresConnectionConfig,
    key: &str,
    limits: BlockReadConfig,
) -> Result<BlockSnapshot, atomic_core::SemanticError> {
    BlockReader::connect(config, limits)?.capture(key)
}

fn reports(rows: usize, width: usize) -> Vec<TxReport> {
    let schema = Database::bootstrap()
        .unwrap()
        .with(
            &[
                TxOp::InstallAttribute(
                    Attribute::new(
                        SCORE,
                        Keyword::new("person", "score"),
                        ValueType::Long,
                        Cardinality::One,
                    )
                    .unique(Unique::Identity),
                ),
                TxOp::InstallAttribute(Attribute::new(
                    NAME,
                    Keyword::new("person", "name"),
                    ValueType::String,
                    Cardinality::One,
                )),
                TxOp::InstallAttribute(Attribute::new(
                    FRIEND,
                    Keyword::new("person", "friend"),
                    ValueType::Ref,
                    Cardinality::One,
                )),
            ],
            10,
        )
        .unwrap();
    let mut ops = Vec::new();
    for i in 0..rows {
        let entity = EntityRef::Temp(format!("person-{i}"));
        ops.push(TxOp::Add {
            entity: entity.clone(),
            attribute: SCORE,
            value: TxValue::Scalar(Value::Long(i as i64)),
        });
        ops.push(TxOp::Add {
            entity: entity.clone(),
            attribute: NAME,
            value: TxValue::Scalar(Value::String(format!("Person {i} {}", "x".repeat(width)))),
        });
        if i > 0 {
            ops.push(TxOp::Add {
                entity,
                attribute: FRIEND,
                value: TxValue::Entity(EntityRef::Temp(format!("person-{}", i - 1))),
            });
        }
    }
    let data = schema.db_after.with(&ops, 20).unwrap();
    vec![schema, data]
}

fn upload_log(store: &mut PgBlockStore, reports: &[TxReport]) -> LogRoot {
    let mut log = LogRoot::empty();
    for report in reports {
        log = log
            .append(
                store,
                &LogEntry {
                    basis_t: report.db_after.basis_t(),
                    eidx_frontier: report.db_after.eidx_frontier(),
                    reserved_frontier: RESERVED,
                    tx_data: report.tx_data.clone(),
                },
            )
            .unwrap();
    }
    log
}

/// Construct the large read fixture through the same pure transaction oracle,
/// but bound each assessment instead of asking its pairwise request validator
/// to assess 30,000 forms at once. Only the current eager database survives
/// each batch; the authenticated log is uploaded before dropping the report.
fn upload_batched_read_fixture(
    store: &mut PgBlockStore,
    rows: usize,
    width: usize,
) -> (Database, LogRoot) {
    assert!(rows > 0);
    let initial = reports(rows.min(LARGE_READ_BATCH), width);
    let mut log = upload_log(store, &initial);
    let mut current = initial.into_iter().last().unwrap().db_after;
    for start in (LARGE_READ_BATCH..rows).step_by(LARGE_READ_BATCH) {
        let previous = current
            .lookup(SCORE, &Value::Long((start - 1) as i64))
            .unwrap()
            .unwrap();
        let end = (start + LARGE_READ_BATCH).min(rows);
        let mut operations = Vec::with_capacity((end - start) * 3);
        for i in start..end {
            let entity = EntityRef::Temp(format!("person-{i}"));
            operations.push(TxOp::Add {
                entity: entity.clone(),
                attribute: SCORE,
                value: Value::Long(i as i64).into(),
            });
            operations.push(TxOp::Add {
                entity: entity.clone(),
                attribute: NAME,
                value: Value::String(format!("Person {i} {}", "x".repeat(width))).into(),
            });
            operations.push(TxOp::Add {
                entity,
                attribute: FRIEND,
                value: TxValue::Entity(if i == start {
                    EntityRef::Id(previous)
                } else {
                    EntityRef::Temp(format!("person-{}", i - 1))
                }),
            });
        }
        let report = current.with(&operations, 20).unwrap();
        log = log
            .append(
                store,
                &LogEntry {
                    basis_t: report.db_after.basis_t(),
                    eidx_frontier: report.db_after.eidx_frontier(),
                    reserved_frontier: RESERVED,
                    tx_data: report.tx_data,
                },
            )
            .unwrap();
        current = report.db_after;
    }
    current.validate_invariants().unwrap();
    (current, log)
}

fn upload_index(store: &mut PgBlockStore, db: &Database) -> (ObjectId, u64, Vec<ObjectId>) {
    let mut trees = Vec::new();
    let mut encoded = 0;
    let mut nodes = Vec::new();
    let config = TreeConfig {
        max_leaf_datoms: 128,
        target_leaf_bytes: 16 * 1024,
        ..TreeConfig::default()
    };
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let built = build_tree(
                order,
                history,
                db.datoms(
                    if history {
                        View::History
                    } else {
                        View::Current
                    },
                    order,
                ),
                &config,
            )
            .unwrap();
            for (id, bytes) in built.nodes.iter() {
                assert_eq!(store.put(bytes).unwrap(), *id);
                encoded += bytes.len() as u64;
                nodes.push(*id);
            }
            trees.push(built.descriptor);
        }
    }
    let bytes = IndexDescriptor {
        identity: IDENTITY,
        basis: db.basis_t(),
        generation: 0,
        trees,
        pending_avet: vec![],
        avet_work: vec![],
        fulltext: None,
    }
    .encode()
    .unwrap();
    (store.put(&bytes).unwrap(), encoded, nodes)
}

fn publish(store: &mut PgBlockStore, db: &Database, log: &LogRoot, index: ObjectId) -> ObjectId {
    let metadata = SnapshotMetadata {
        identity: IDENTITY,
        basis: db.basis_t(),
        generation: 0,
        eidx_frontier: db.eidx_frontier(),
        reserved_frontier: RESERVED.min(db.eidx_frontier()),
        last_tx_instant: db.database_value().last_tx_instant().unwrap(),
        excision: None,
    };
    let metadata = store.put(&metadata.encode().unwrap()).unwrap();
    let root = DatabaseValueRoot {
        identity: IDENTITY,
        basis: db.basis_t(),
        log: log.head(),
        indexes: Some(index),
        metadata: Some(metadata),
    };
    let id = store.put(&root.encode().unwrap()).unwrap();
    let expected = store.read_ref(ROOT).unwrap().map(|r| r.revision);
    assert!(matches!(
        store.compare_exchange(ROOT, expected, Some(&id)).unwrap(),
        CasOutcome::Applied(_)
    ));
    id
}

fn query(value: &DatabaseValue, text: &str, data: &[&str]) -> QueryResult {
    let mut args = vec![EdnQueryArgument::Source(QuerySourceValue::Database(
        value.clone(),
    ))];
    args.extend(
        data.iter()
            .map(|text| EdnQueryArgument::Data(read_edn(text).unwrap())),
    );
    parse_query_edn(text)
        .unwrap()
        .bind(&args)
        .unwrap()
        .execute(&QueryControl::default(), None)
        .unwrap()
        .result
}

fn name_query(value: &DatabaseValue, score: usize) -> QueryResult {
    query(
        value,
        "[:find ?name . :in $ ?score :where [?e :person/score ?score] [?e :person/name ?name]]",
        &[&score.to_string()],
    )
}

#[test]
fn block_values_support_edn_pull_time_views_and_independent_branches() {
    let Some((_fixture, config)) = fixture("block_values") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let mut reports = reports(96, 8);
    let old = &reports[1].db_after;
    let log = upload_log(&mut store, &reports);
    let (index, _, _) = upload_index(&mut store, old);
    publish(&mut store, old, &log, index);
    let snapshot = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let value = DatabaseValue::block(snapshot.clone());
    let expected = old.database_value();
    assert_eq!(name_query(&value, 42), name_query(&expected, 42));
    let id = reports[1].tempids["person-42"];
    let pattern =
        parse_pull_edn("[:person/name :person/score {:person/friend [:person/name]}]").unwrap();
    assert_eq!(
        value.pull(&pattern, id).unwrap(),
        expected.pull(&pattern, id).unwrap()
    );
    let change = format!("[{{:db/id {id} :person/name \"Changed\"}}]");
    let branch = value.with_edn(&change, 30).unwrap();
    let oracle = old.with_edn(&change, 30).unwrap();
    assert_eq!(branch.tx_data, oracle.tx_data);
    assert_eq!(branch.tempids, oracle.tempids);
    assert_eq!(
        name_query(&branch.db_after, 42),
        name_query(&oracle.db_after.database_value(), 42)
    );
    assert_eq!(name_query(&value, 42), name_query(&expected, 42));
    let alternate = value
        .with_edn(&format!("[{{:db/id {id} :person/name \"Alternate\"}}]"), 30)
        .unwrap();
    assert_ne!(
        name_query(&alternate.db_after, 42),
        name_query(&branch.db_after, 42)
    );
    reports.push(oracle);
    let next = &reports[2].db_after;
    let log = upload_log(&mut store, &reports);
    let (index, _, _) = upload_index(&mut store, next);
    publish(&mut store, next, &log, index);
    let newer = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let newer_value = DatabaseValue::block(newer.clone());
    assert_eq!(name_query(&value, 42), name_query(&expected, 42));
    for (actual, expected) in [
        (newer_value.clone(), next.database_value()),
        (newer_value.clone().as_of(2), next.database_value().as_of(2)),
        (newer_value.clone().since(2), next.database_value().since(2)),
        (
            newer_value.clone().history(),
            next.database_value().history(),
        ),
    ] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(
                actual.datoms(order).unwrap(),
                expected.datoms(order).unwrap()
            );
        }
    }
    drop((newer_value, alternate, branch, value));
    newer.release().unwrap();
    snapshot.release().unwrap();
}

#[test]
fn old_index_plus_authenticated_recent_tail_matches_current_and_history() {
    let Some((_fixture, config)) = fixture("block_recent") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let mut reports = reports(192, 8);
    let id = reports[1].tempids["person-41"];
    let change = reports[1]
        .db_after
        .with_edn(&format!("[{{:db/id {id} :person/name \"New name\"}}]"), 30)
        .unwrap();
    reports.push(change);
    let log = upload_log(&mut store, &reports);
    let (index, _, _) = upload_index(&mut store, &reports[0].db_after);
    let expected = &reports[2].db_after;
    publish(&mut store, expected, &log, index);
    let snapshot = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let value = DatabaseValue::block(snapshot.clone());
    for history in [false, true] {
        let actual = if history {
            value.clone().history()
        } else {
            value.clone()
        };
        let expected = if history {
            expected.database_value().history()
        } else {
            expected.database_value()
        };
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(
                actual.datoms(order).unwrap(),
                expected.datoms(order).unwrap()
            );
        }
    }
    let boundary = IndexBoundary::Avet(IndexComponents::Two(SCORE, Value::Long(41)));
    let reverse = value
        .reverse_seek_cursor(&boundary)
        .unwrap()
        .take(10)
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_eq!(
        reverse,
        expected
            .database_value()
            .reverse_seek_cursor(&boundary)
            .unwrap()
            .take(10)
            .collect::<Result<Vec<_>, _>>()
            .unwrap()
    );
    assert_eq!(
        name_query(&value, 41),
        name_query(&expected.database_value(), 41)
    );
    drop(value);
    snapshot.release().unwrap();
}

#[test]
fn larger_than_cache_open_query_branch_and_cancellation_are_selective() {
    let Some((_fixture, config)) = fixture("block_selective") else {
        return;
    };
    let setup = Instant::now();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let (expected, log) = upload_batched_read_fixture(&mut store, 10_000, 192);
    for (attribute, count) in [(SCORE, 10_000), (NAME, 10_000), (FRIEND, 9_999)] {
        assert_eq!(
            expected
                .datoms_with_prefix(&atomic_core::IndexPrefix::Aevt {
                    attribute,
                    entity: None,
                    value: None,
                })
                .unwrap()
                .len(),
            count,
            "batching must preserve the complete large read fixture"
        );
    }
    let predecessor = expected
        .lookup(SCORE, &Value::Long((LARGE_READ_BATCH - 1) as i64))
        .unwrap()
        .unwrap();
    let successor = expected
        .lookup(SCORE, &Value::Long(LARGE_READ_BATCH as i64))
        .unwrap()
        .unwrap();
    assert_eq!(
        expected.values(successor, FRIEND),
        vec![&Value::Ref(predecessor)]
    );
    let (index, total_bytes, _) = upload_index(&mut store, &expected);
    publish(&mut store, &expected, &log, index);
    assert!(total_bytes > 1024 * 1024);
    let id = expected.lookup(SCORE, &Value::Long(777)).unwrap().unwrap();
    eprintln!(
        "BLOCK_SNAPSHOT_SETUP rows=10000 payload_width=192 batch_rows={LARGE_READ_BATCH} basis={} stored_tree_bytes={total_bytes} setup_us={}",
        expected.basis_t(),
        setup.elapsed().as_micros()
    );
    for cache_bytes in [0, 16 * 1024] {
        let operation = OperationContext::new(OperationKind::Application);
        let scope = operation.enter();
        let start = Instant::now();
        let snapshot = open_snapshot(
            &config,
            ROOT,
            BlockReadConfig {
                cache_entries: 16,
                cache_bytes,
                ..BlockReadConfig::default()
            },
        )
        .unwrap();
        let value = DatabaseValue::block(snapshot.clone());
        assert_eq!(
            name_query(&value, 777),
            name_query(&expected.database_value(), 777)
        );
        let branch = value
            .with_edn(&format!("[{{:db/id {id} :person/name \"Preview\"}}]"), 30)
            .unwrap();
        assert_eq!(
            name_query(&branch.db_after, 777),
            QueryResult::Scalar(Some(atomic_core::QueryValue::Scalar(Value::String(
                "Preview".into()
            ))))
        );
        let elapsed = start.elapsed();
        drop(scope);
        let io = operation.snapshot();
        assert!(io.sql_calls > 0);
        assert!(
            io.known_payload_read_bytes < total_bytes / 4,
            "narrow open/query/branch must not load the whole fixture: {} of {} bytes",
            io.known_payload_read_bytes,
            total_bytes
        );
        eprintln!(
            "BLOCK_SNAPSHOT_SAMPLE rows=10000 stored_tree_bytes={total_bytes} cache_bytes={cache_bytes} complete_us={} sql_calls={} payload_read_bytes={} result_cell_bytes={}",
            elapsed.as_micros(),
            io.sql_calls,
            io.known_payload_read_bytes,
            io.result_cell_bytes
        );
        let control = QueryControl::default();
        let cancel = control.cancel.clone();
        let examined = Arc::new(AtomicUsize::new(0));
        let observed = examined.clone();
        let hidden = value.clone().filter(move |_, _| {
            if observed.fetch_add(1, Ordering::Relaxed) >= 32 {
                cancel.store(true, Ordering::Relaxed);
            }
            false
        });
        let bound = parse_query_edn("[:find ?e :where [?e :person/name _]]")
            .unwrap()
            .bind(&[EdnQueryArgument::Source(QuerySourceValue::Database(hidden))])
            .unwrap();
        let error = bound.execute(&control, None).unwrap_err();
        assert_eq!(error.category, ErrorCategory::Interrupted);
        assert!(
            examined.load(Ordering::Relaxed) < 1000,
            "cancellation must interrupt filtered source consumption"
        );
        drop((bound, branch, value));
        snapshot.release().unwrap();
    }
}

#[test]
fn missing_reachable_index_root_fails_instead_of_falling_back_to_legacy_data() {
    let Some((_fixture, config)) = fixture("block_missing") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reports = reports(16, 8);
    let log = upload_log(&mut store, &reports);
    let (index, _, _) = upload_index(&mut store, &reports[1].db_after);
    publish(&mut store, &reports[1].db_after, &log, index);
    let descriptor = IndexDescriptor::decode(&index, &store.get(index).unwrap().unwrap()).unwrap();
    assert_eq!(
        store
            .remove_objects(&[descriptor.trees[0].root_hash])
            .unwrap(),
        1
    );
    assert!(open_snapshot(&config, ROOT, BlockReadConfig::default()).is_err());
}

#[test]
fn empty_database_and_recent_admission_use_explicit_current_metadata() {
    let Some((_fixture, config)) = fixture("block_empty") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let empty = Database::bootstrap().unwrap();
    let (index, _, _) = upload_index(&mut store, &empty);
    publish(&mut store, &empty, &LogRoot::empty(), index);
    let snapshot = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let value = snapshot.database_value();
    assert_eq!(value.basis_t(), 0);
    assert_eq!(
        value.datoms(IndexOrder::Eavt).unwrap(),
        empty.datoms(View::Current, IndexOrder::Eavt)
    );
    assert_eq!(value.last_tx_instant().unwrap(), None);
    drop(value);
    snapshot.release().unwrap();

    let reports = reports(8, 4);
    let log = upload_log(&mut store, &reports);
    publish(&mut store, &reports[1].db_after, &log, index);
    for limits in [
        BlockReadConfig {
            max_recent_transactions: 1,
            ..BlockReadConfig::default()
        },
        BlockReadConfig {
            max_recent_datoms: 1,
            ..BlockReadConfig::default()
        },
        BlockReadConfig {
            max_recent_bytes: 1,
            ..BlockReadConfig::default()
        },
    ] {
        let error = open_snapshot(&config, ROOT, limits).unwrap_err();
        assert_eq!(error.category, ErrorCategory::Busy);
        assert_eq!(error.code, "storage/recent-window-limit");
    }
}

#[test]
fn one_reader_shares_its_connection_and_cache_across_many_captured_values() {
    let Some((_fixture, config)) = fixture("block_shared_reader") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reports = reports(8, 4);
    let log = upload_log(&mut store, &reports);
    let (index, _, _) = upload_index(&mut store, &reports[1].db_after);
    publish(&mut store, &reports[1].db_after, &log, index);
    let operation = OperationContext::new(OperationKind::Application);
    let scope = operation.enter();
    let reader = BlockReader::connect(&config, BlockReadConfig::default()).unwrap();
    let mut captured = Vec::new();
    for _ in 0..80 {
        captured.push(reader.capture(ROOT).unwrap());
    }
    assert_eq!(operation.snapshot().connect_calls, 1);
    let first = captured[0].database_value();
    let last = captured[79].database_value();
    assert_eq!(name_query(&first, 2), name_query(&last, 2));
    assert!(captured[79].cache_stats().hits > 0);
    drop((first, last, reader));
    for snapshot in captured {
        snapshot.release().unwrap();
    }
    drop(scope);
}

#[test]
fn captured_native_programs_invoke_and_speculate_without_sql_program_catalog() {
    use atomic_core::{
        Instruction, InvokeControl, Program, ProgramKind, RuntimeValue, encode_program,
    };
    let Some((_fixture, config)) = fixture("block_program") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let mut reports = reports(8, 4);
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::String("From native program".into())),
            Instruction::EmitAdd(NAME),
            Instruction::Return,
        ],
    };
    let program_id = store.put(&encode_program(&program).unwrap()).unwrap();
    let binding = reports[1]
        .db_after
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("function".into()),
                    attribute: atomic_core::DB_IDENT as u32,
                    value: Value::Keyword(Keyword::new("test", "rename")).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("function".into()),
                    attribute: atomic_core::DB_FN as u32,
                    value: Value::Function(program_id).into(),
                },
            ],
            30,
        )
        .unwrap();
    reports.push(binding);
    let log = upload_log(&mut store, &reports);
    let (index, _, _) = upload_index(&mut store, &reports[2].db_after);
    publish(&mut store, &reports[2].db_after, &log, index);
    let snapshot = open_snapshot(&config, ROOT, BlockReadConfig::default()).unwrap();
    let value = snapshot.database_value();
    let person = reports[1].tempids["person-2"];
    let output = value
        .invoke(
            Keyword::new("test", "rename"),
            &[RuntimeValue::Scalar(Value::Ref(person))],
            InvokeControl::default(),
        )
        .unwrap();
    let atomic_core::ProgramOutput::Transaction(forms) = output else {
        panic!("expected inert transaction forms")
    };
    let branch = value.with_forms(&forms, 40).unwrap();
    let called = value
        .with_edn(&format!("[[:test/rename #atomic/ref {person}]]"), 40)
        .unwrap();
    assert_eq!(branch.tx_data, called.tx_data);
    assert_eq!(
        name_query(&branch.db_after, 2),
        QueryResult::Scalar(Some(atomic_core::QueryValue::Scalar(Value::String(
            "From native program".into()
        ))))
    );
    assert_eq!(
        name_query(&value, 2),
        name_query(&reports[2].db_after.database_value(), 2)
    );
    drop((called, branch, value));
    snapshot.release().unwrap();
}
