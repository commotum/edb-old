mod common;

use atomic_core::edn::read_edn;
use atomic_core::edn_pull::EdnPullTransforms;
use atomic_core::edn_query::{EdnQueryArgument, parse_query_edn};
use atomic_core::*;
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::{Duration, Instant};

const SCORE: u32 = 1000;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            SCORE,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn fixture(count: usize) -> (DatabaseValue, Vec<u64>) {
    let report = Database::new(schema())
        .unwrap()
        .with(
            &(0..count)
                .map(|index| TxOp::Add {
                    entity: EntityRef::Temp(format!("item{index}")),
                    attribute: SCORE,
                    value: Value::Long(index as i64).into(),
                })
                .collect::<Vec<_>>(),
            1_000,
        )
        .unwrap();
    let entities = (0..count)
        .map(|index| report.tempids[&format!("item{index}")])
        .collect();
    (report.db_after.database_value(), entities)
}

fn projection(transform: Option<PullTransform>) -> FindElement {
    let mut attribute = PullAttribute::forward(AttributeName::Id(SCORE));
    attribute.transform = transform;
    FindElement::Pull {
        source: "$db".into(),
        variable: "?e".into(),
        pattern: Box::new(PullPattern::attributes(vec![attribute])),
    }
}

fn query(transform: Option<PullTransform>) -> Query {
    let mut pattern = DataPattern::new(
        Term::var("?e"),
        Term::Constant(Value::Keyword(Keyword::new("item", "score"))),
        Term::var("?n"),
    );
    pattern.source = "$db".into();
    let mut labels = DataPattern::new(Term::var("?n"), Term::var("?label"), Term::Blank);
    labels.source = "$rows".into();
    Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable("?e".into()),
            projection(transform),
            FindElement::Variable("?label".into()),
        ]),
        vec![
            Clause::Pattern(Box::new(pattern)),
            Clause::Pattern(Box::new(labels)),
        ],
    )
}

fn sources(database: DatabaseValue, count: usize) -> Vec<QueryDataSource> {
    vec![
        QueryDataSource::database("$db", database),
        QueryDataSource::tuples(
            "$rows",
            (0..count)
                .map(|index| {
                    vec![
                        Value::Long(index as i64),
                        Value::String(format!("label{index}")),
                    ]
                })
                .collect(),
        ),
    ]
}

fn rows(result: QueryResult) -> Vec<Vec<QueryValue>> {
    match result {
        QueryResult::Relation(rows) => rows,
        QueryResult::Tuple(row) => row.into_iter().collect(),
        QueryResult::Collection(values) => values.into_iter().map(|v| vec![v]).collect(),
        QueryResult::Scalar(value) => value.into_iter().map(|v| vec![v]).collect(),
    }
}

#[test]
fn mixed_database_and_raw_sources_defer_projection_and_own_their_exact_values() {
    let (before, entities) = fixture(4);
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = calls.clone();
    let q = query(Some(PullTransform::new("observe", move |value| {
        observed.fetch_add(1, Ordering::Relaxed);
        Ok(value.clone())
    })));
    let inputs = sources(before.clone(), 4);
    let mut seq = QueryEngine::sequence_sources(&q, &inputs, &[], &Default::default()).unwrap();
    let allocated = seq.stats().allocated_value_bytes;
    assert_eq!(seq.remaining_rows(), 4);
    assert_eq!(seq.size_hint(), (0, Some(4)));
    assert_eq!(seq.stats().rows_produced, 0);
    assert_eq!(calls.load(Ordering::Relaxed), 0);
    // Capture a new view and discard every caller-owned old source. Deferred
    // Pull must still see the captured score, never the successor's99.
    let successor = before
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entities[0]),
                attribute: SCORE,
                value: Value::Long(99).into(),
            }],
            2_000,
        )
        .unwrap()
        .db_after;
    drop((q, inputs, before));
    let first = seq.next().unwrap().unwrap();
    assert_eq!(first[0], QueryValue::Scalar(Value::Ref(entities[0])));
    let QueryValue::Map(entries) = &first[1] else {
        panic!("map")
    };
    assert!(
        entries
            .iter()
            .any(|(_, v)| v == &QueryValue::Scalar(Value::Long(0)))
    );
    assert_eq!(
        successor.values(entities[0], SCORE).unwrap(),
        vec![Value::Long(99)]
    );
    assert_eq!(seq.remaining_rows(), 3);
    assert_eq!(seq.stats().rows_produced, 1);
    assert!(
        seq.stats().allocated_value_bytes > allocated,
        "deferred projection contributes to actual accounting"
    );
    drop(seq);
    assert_eq!(
        calls.load(Ordering::Relaxed),
        1,
        "dropping unconsumed rows does not invoke transforms"
    );
}

#[test]
fn same_sources_and_find_shapes_agree_with_eager_results_including_aggregates() {
    let (database, _) = fixture(4);
    let input = sources(database, 4);
    let base = query(None);
    let element = projection(None);
    for find in [
        base.find.clone(),
        FindSpec::Tuple(vec![element.clone()]),
        FindSpec::Collection(element.clone()),
        FindSpec::Scalar(element),
        FindSpec::Scalar(FindElement::Aggregate {
            function: Aggregate::Sum,
            variable: "?n".into(),
        }),
    ] {
        let mut q = base.clone();
        q.find = find;
        let expected = rows(
            QueryEngine::execute_sources(&q, &input, &[], &Default::default())
                .unwrap()
                .result,
        );
        let seq = QueryEngine::sequence_sources(&q, &input, &[], &Default::default()).unwrap();
        assert_eq!(seq.remaining_rows(), expected.len());
        assert_eq!(seq.collect::<Result<Vec<_>, _>>().unwrap(), expected);
    }
}

#[test]
fn sequence_shares_work_deadline_cancellation_and_fuses_after_projection_errors() {
    let (database, _) = fixture(4);
    let input = sources(database, 4);
    let q = query(None);
    let prepared = QueryEngine::sequence_sources(&q, &input, &[], &Default::default())
        .unwrap()
        .stats()
        .work as usize;
    let work = QueryControl {
        max_work: prepared,
        ..Default::default()
    };
    let mut seq = QueryEngine::sequence_sources(&q, &input, &[], &work).unwrap();
    assert_eq!(seq.next().unwrap().unwrap_err().code, "query/work-limit");
    assert_eq!(seq.remaining_rows(), 0);
    assert!(seq.next().is_none());
    let control = QueryControl::default();
    let mut seq = QueryEngine::sequence_sources(&q, &input, &[], &control).unwrap();
    seq.next().unwrap().unwrap();
    control.cancel.store(true, Ordering::Relaxed);
    assert_eq!(seq.next().unwrap().unwrap_err().code, "query/canceled");
    assert!(seq.next().is_none());
    assert!(
        QueryEngine::sequence_sources(
            &q,
            &input,
            &[],
            &QueryControl {
                timeout: Some(Duration::ZERO),
                ..Default::default()
            }
        )
        .is_err()
    );
    let timed = QueryControl {
        timeout: Some(Duration::from_millis(250)),
        ..Default::default()
    };
    let mut seq = QueryEngine::sequence_sources(&q, &input, &[], &timed).unwrap();
    std::thread::sleep(Duration::from_millis(300));
    assert_eq!(seq.next().unwrap().unwrap_err().code, "query/timeout");
    assert!(seq.next().is_none());
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = calls.clone();
    let q = query(Some(PullTransform::new("fail-second", move |value| {
        if observed.fetch_add(1, Ordering::Relaxed) == 1 {
            Err(SemanticError::incorrect(
                "test/transform",
                "second row fails",
            ))
        } else {
            Ok(value.clone())
        }
    })));
    let mut seq = QueryEngine::sequence_sources(&q, &input, &[], &Default::default()).unwrap();
    seq.next().unwrap().unwrap();
    assert_eq!(seq.next().unwrap().unwrap_err().code, "test/transform");
    assert_eq!(seq.stats().rows_produced, 1);
    assert_eq!(seq.remaining_rows(), 0);
    assert!(seq.next().is_none());
    assert_eq!(calls.load(Ordering::Relaxed), 2);
}

#[test]
fn source_errors_are_not_hidden_by_empty_relations_and_legacy_wrappers_still_work() {
    let (database, _) = fixture(0);
    let mut input = sources(database.clone(), 0);
    let q = query(None);
    input.push(input[0].clone());
    assert_eq!(
        QueryEngine::sequence_sources(&q, &input, &[], &Default::default())
            .unwrap_err()
            .code,
        "query/duplicate-source"
    );
    input.pop();
    input[0] = QueryDataSource::tuples("$db", vec![]);
    assert_eq!(
        QueryEngine::sequence_sources(&q, &input, &[], &Default::default())
            .unwrap_err()
            .code,
        "query/source-kind"
    );
    input.remove(0);
    assert_eq!(
        QueryEngine::sequence_sources(&q, &input, &[], &Default::default())
            .unwrap_err()
            .code,
        "query/unknown-source"
    );
    let mut legacy = q.clone();
    legacy.clauses.pop();
    legacy.find = FindSpec::Collection(projection(None));
    assert_eq!(
        QueryEngine::sequence(
            &legacy,
            &[QuerySource {
                name: "$db".into(),
                database
            }],
            &[],
            &Default::default()
        )
        .unwrap()
        .remaining_rows(),
        0
    );
}

#[test]
fn bound_edn_sequence_runs_relational_callbacks_eagerly_and_pull_callbacks_lazily() {
    let (database, _) = fixture(3);
    let pulls = Arc::new(AtomicUsize::new(0));
    let observed = pulls.clone();
    let mut transforms = EdnPullTransforms::new();
    transforms.register(
        "native/observe",
        PullTransform::new("observe", move |v| {
            observed.fetch_add(1, Ordering::Relaxed);
            Ok(v.clone())
        }),
    );
    let functions = Arc::new(AtomicUsize::new(0));
    let observed = functions.clone();
    let mut extensions = QueryExtensions::new();
    extensions.register_local("native/pass", move |_, args, _| {
        observed.fetch_add(1, Ordering::Relaxed);
        Ok(vec![args.to_vec()])
    });
    let bound=parse_query_edn("[:find (pull $db ?e [[:item/score :xform native/observe]]) ?label :in $db $rows :where [$db ?e :item/score ?n] [$rows ?n ?label] [(native/pass $db ?n) ?copy]]").unwrap().bind_with_transforms(&[
        EdnQueryArgument::Source(QuerySourceValue::Database(database)),EdnQueryArgument::Data(read_edn("[[0 \"a\"] [1 \"b\"] [2 \"c\"]]").unwrap())],&transforms).unwrap();
    let mut seq = bound
        .sequence(&Default::default(), Some(&extensions))
        .unwrap();
    assert_eq!(seq.remaining_rows(), 3);
    assert_eq!(functions.load(Ordering::Relaxed), 3);
    assert_eq!(pulls.load(Ordering::Relaxed), 0);
    drop((bound, extensions, transforms));
    seq.next().unwrap().unwrap();
    assert_eq!(pulls.load(Ordering::Relaxed), 1);
}

#[test]
fn actual_postgres_mixed_log_database_and_raw_sequence_retains_basis_after_writes_and_stop() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP mixed sequence PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "read_sequence_sources");
    let url = &fixture.connection;
    common::install(url).unwrap();
    common::TestStore::connect(url)
        .unwrap()
        .create_database("sequence", schema())
        .unwrap();
    let service = common::start_service(url, "sequence");
    let connection = Connection::connect(url, "sequence", 8).unwrap();
    let report = service
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("a".into()),
                        attribute: SCORE,
                        value: Value::Long(7).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("b".into()),
                        attribute: SCORE,
                        value: Value::Long(8).into(),
                    },
                ],
            ),
            Duration::from_secs(30),
        )
        .unwrap();
    connection
        .sync_to(report.basis_t, Duration::from_secs(30))
        .unwrap();
    let captured = connection.db();
    let log = connection.log();
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = calls.clone();
    let mut transforms = EdnPullTransforms::new();
    transforms.register(
        "native/observe",
        PullTransform::new("observe", move |v| {
            observed.fetch_add(1, Ordering::Relaxed);
            Ok(v.clone())
        }),
    );
    let bound=parse_query_edn("[:find ?e (pull $db ?e [[:item/score :xform native/observe]]) ?label :in $db $log $rows :where [(tx-ids $log 0 nil) [?tx ...]] [(tx-data $log ?tx) [[?e ?a ?n _ ?added]]] [(= ?a 1000)] [(= ?added true)] [$rows ?n ?label]]").unwrap().bind_with_transforms(&[
        EdnQueryArgument::Source(QuerySourceValue::Database(captured.clone())),EdnQueryArgument::Source(QuerySourceValue::Log(log)),EdnQueryArgument::Data(read_edn("[[7 \"seven\"] [8 \"eight\"]]").unwrap())],&transforms).unwrap();
    let mut seq = bound.sequence(&Default::default(), None).unwrap();
    assert_eq!(seq.remaining_rows(), 2);
    assert_eq!(calls.load(Ordering::Relaxed), 0);
    let later = service
        .client()
        .transact(
            TransactionRequest::new(
                "change",
                vec![TxOp::Add {
                    entity: EntityRef::Id(report.tempids["a"]),
                    attribute: SCORE,
                    value: Value::Long(99).into(),
                }],
            ),
            Duration::from_secs(30),
        )
        .unwrap();
    connection
        .sync_to(later.basis_t, Duration::from_secs(30))
        .unwrap();
    assert_eq!(
        connection.db().values(report.tempids["a"], SCORE).unwrap(),
        vec![Value::Long(99)]
    );
    service.shutdown();
    let expected = rows(bound.execute(&Default::default(), None).unwrap().result);
    assert_eq!(expected.len(), 2);
    calls.store(0, Ordering::Relaxed);
    drop((bound, captured, connection, transforms, report, later));
    let mut actual = Vec::new();
    for row in seq.by_ref() {
        actual.push(row.unwrap());
    }
    assert_eq!(actual, expected);
    assert_eq!(seq.remaining_rows(), 0);
    assert_eq!(seq.stats().rows_produced, 2);
    assert_eq!(calls.load(Ordering::Relaxed), 2);
    assert!(seq.next().is_none());
}

#[test]
fn measured_complete_preparation_partial_consumption_and_drop_costs() {
    for count in [32, 128, 512] {
        let setup = Instant::now();
        let (database, _) = fixture(count);
        let input = sources(database, count);
        let setup = setup.elapsed();
        let expected = rows(
            QueryEngine::execute_sources(&query(None), &input, &[], &Default::default())
                .unwrap()
                .result,
        );
        for consumed in [1, count] {
            let calls = Arc::new(AtomicUsize::new(0));
            let observed = calls.clone();
            let q = query(Some(PullTransform::new("measure", move |v| {
                observed.fetch_add(1, Ordering::Relaxed);
                Ok(v.clone())
            })));
            let complete = Instant::now();
            let mut seq =
                QueryEngine::sequence_sources(&q, &input, &[], &Default::default()).unwrap();
            let prepared = complete.elapsed();
            let initial_work = seq.stats().work;
            let initial_bytes = seq.stats().allocated_value_bytes;
            assert_eq!(seq.remaining_rows(), count);
            assert_eq!(calls.load(Ordering::Relaxed), 0);
            for row in expected.iter().take(consumed) {
                assert_eq!(&seq.next().unwrap().unwrap(), row);
            }
            let total_work = seq.stats().work;
            let total_bytes = seq.stats().allocated_value_bytes;
            assert_eq!(calls.load(Ordering::Relaxed), consumed);
            assert_eq!(seq.remaining_rows(), count - consumed);
            drop((seq, q));
            eprintln!(
                "read_sequence rows={count} consumed={consumed} fixture={setup:?} prepare={prepared:?} complete_prepare_consume_drop={:?} work={initial_work}->{total_work} accounted_bytes={initial_bytes}->{total_bytes}",
                complete.elapsed()
            );
        }
    }
}
