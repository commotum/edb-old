//! Runtime-independent async facade exercised on a single-thread executor.
mod common;
use atomic_core::*;
use std::sync::{
    Arc,
    atomic::{AtomicBool, AtomicUsize, Ordering},
};
use std::time::{Duration, Instant};

fn runtime() -> tokio::runtime::Runtime {
    tokio::runtime::Builder::new_current_thread()
        .enable_time()
        .build()
        .unwrap()
}
fn executor(workers: usize, operations: usize, resources: usize) -> AsyncExecutor {
    AsyncExecutor::new(AsyncConfig {
        workers,
        max_operations: operations,
        max_resources: resources,
    })
    .unwrap()
}
async fn until(mut predicate: impl FnMut() -> bool) {
    tokio::time::timeout(Duration::from_secs(10), async {
        while !predicate() {
            tokio::time::sleep(Duration::from_millis(1)).await;
        }
    })
    .await
    .expect("bounded worker progress");
}
fn extension_query() -> Query {
    Query::new(
        FindSpec::Scalar(FindElement::Variable("v".into())),
        vec![Clause::Function {
            function: Function::Extension("hold".into()),
            source: "$absent".into(),
            args: vec![],
            binding: Binding::Scalar("v".into()),
        }],
    )
}
fn blocking_extension(entered: Arc<AtomicBool>, release: Arc<AtomicBool>) -> QueryExtensions {
    let mut extensions = QueryExtensions::default();
    extensions.register_pure("hold", move |_, _| {
        assert!(
            std::thread::current()
                .name()
                .unwrap()
                .starts_with("atomic-async-")
        );
        entered.store(true, Ordering::Release);
        let deadline = Instant::now() + Duration::from_secs(10);
        while !release.load(Ordering::Acquire) && Instant::now() < deadline {
            std::thread::sleep(Duration::from_millis(1));
        }
        Ok(QueryValue::Scalar(Value::Long(17)))
    });
    extensions
}
fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}
fn request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1000,
            value: Value::Long(value).into(),
        }],
    )
}
fn score_query() -> Query {
    Query::new(
        FindSpec::Collection(FindElement::Variable("n".into())),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::var("e"),
            Term::Constant(Value::Keyword(Keyword::new("item", "score"))),
            Term::var("n"),
        )))],
    )
}
fn expected(values: &[i64]) -> QueryResult {
    QueryResult::Collection(
        values
            .iter()
            .map(|n| QueryValue::Scalar(Value::Long(*n)))
            .collect(),
    )
}

#[test]
fn database_free_callbacks_do_not_block_and_queued_deadlines_count_waiting() {
    let executor = executor(1, 2, 1);
    let entered = Arc::new(AtomicBool::new(false));
    let release = Arc::new(AtomicBool::new(false));
    runtime().block_on(async {
        let slow = executor
            .query_sources(
                extension_query(),
                &[],
                vec![],
                Default::default(),
                Some(blocking_extension(entered.clone(), release.clone())),
            )
            .unwrap();
        until(|| entered.load(Ordering::Acquire)).await;
        let expired = executor
            .query_sources(
                extension_query(),
                &[],
                vec![],
                QueryControl {
                    timeout: Some(Duration::from_millis(5)),
                    ..Default::default()
                },
                None,
            )
            .unwrap();
        assert_eq!(
            executor
                .query_sources(extension_query(), &[], vec![], Default::default(), None)
                .unwrap_err()
                .code,
            "async/operation-capacity"
        );
        tokio::time::sleep(Duration::from_millis(20)).await;
        assert!(
            !release.load(Ordering::Acquire),
            "single-thread timer advanced during blocking callback"
        );
        release.store(true, Ordering::Release);
        assert_eq!(
            slow.await.unwrap().result,
            QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(17))))
        );
        assert_eq!(expired.await.unwrap_err().code, "async/deadline");
        until(|| executor.stats().operations == 0).await;
    });
    assert_eq!(executor.stats().peak_running, 1);
}

#[test]
fn rules_inputs_mixed_sources_and_prepared_queries_preserve_native_semantics() {
    let db = Database::new(schema())
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("one".into()),
                    attribute: 1000,
                    value: Value::Long(1).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("two".into()),
                    attribute: 1000,
                    value: Value::Long(2).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("three".into()),
                    attribute: 1000,
                    value: Value::Long(3).into(),
                },
            ],
            1000,
        )
        .unwrap()
        .db_after
        .database_value();
    let template = edn_query::parse_query_edn(
        "[:find ?n ?label :in $ $labels ?floor % :where (eligible ?e ?n) [$labels ?n ?label]]",
    )
    .unwrap();
    let bound = template
        .bind(&[
            edn_query::EdnQueryArgument::Source(QuerySourceValue::Database(db)),
            edn_query::EdnQueryArgument::Source(
                QueryDataSource::tuples(
                    "$labels",
                    vec![
                        vec![Value::Long(2), Value::String("two".into())],
                        vec![Value::Long(3), Value::String("three".into())],
                    ],
                )
                .value,
            ),
            edn_query::EdnQueryArgument::Data(edn::read_edn("2").unwrap()),
            edn_query::EdnQueryArgument::Data(
                edn::read_edn("[[(eligible ?e ?n) [?e :item/score ?n] [(>= ?n 2)]]]").unwrap(),
            ),
        ])
        .unwrap();
    let executor = executor(2, 4, 4);
    runtime().block_on(async {
        let prepared = PreparedQuery::new(bound.query()).unwrap();
        for force_scan in [false, true] {
            let outcome = executor
                .query_prepared(
                    prepared.clone(),
                    &bound.sources,
                    bound.inputs.clone(),
                    QueryControl {
                        force_scan,
                        ..Default::default()
                    },
                    None,
                )
                .unwrap()
                .await
                .unwrap();
            let QueryResult::Relation(rows) = outcome.result else {
                panic!("relation expected");
            };
            let expected = vec![
                vec![
                    QueryValue::Scalar(Value::Long(2)),
                    QueryValue::Scalar(Value::String("two".into())),
                ],
                vec![
                    QueryValue::Scalar(Value::Long(3)),
                    QueryValue::Scalar(Value::String("three".into())),
                ],
            ];
            assert_eq!(rows.len(), expected.len());
            for row in expected {
                assert!(rows.contains(&row));
            }
        }
    });
}

#[test]
fn query_streams_demand_drive_pull_fuse_errors_and_wait_for_worker_admission() {
    let db = Database::new(schema())
        .unwrap()
        .with(
            &(0..8)
                .map(|n| TxOp::Add {
                    entity: EntityRef::Temp(format!("e{n}")),
                    attribute: 1000,
                    value: Value::Long(n).into(),
                })
                .collect::<Vec<_>>(),
            1000,
        )
        .unwrap()
        .db_after
        .database_value();
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = calls.clone();
    let mut attribute = PullAttribute::forward(AttributeName::Id(1000));
    attribute.transform = Some(PullTransform::new("observe", move |value| {
        let count = observed.fetch_add(1, Ordering::AcqRel);
        if count == 3 {
            Err(SemanticError::incorrect(
                "test/fourth-row",
                "fused projection failure",
            ))
        } else {
            Ok(value.clone())
        }
    }));
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Pull {
            source: "$".into(),
            variable: "e".into(),
            pattern: Box::new(PullPattern::attributes(vec![attribute])),
        }]),
        score_query().clauses,
    );
    let executor = executor(1, 1, 1);
    let sources = [QueryDataSource::database("$", db)];
    let mut stream = executor
        .query_sequence_sources(
            query.clone(),
            &sources,
            vec![],
            Default::default(),
            None,
            AsyncStreamOptions {
                chunk_rows: 2,
                timeout: None,
            },
        )
        .unwrap();
    assert_eq!(calls.load(Ordering::Acquire), 0);
    assert_eq!(
        executor
            .query_sequence_sources(
                query,
                &sources,
                vec![],
                Default::default(),
                None,
                Default::default()
            )
            .unwrap_err()
            .code,
        "async/resource-capacity"
    );
    runtime().block_on(async {
        stream.next().await.unwrap().unwrap();
        assert_eq!(calls.load(Ordering::Acquire), 2);
        tokio::time::sleep(Duration::from_millis(10)).await;
        assert_eq!(
            calls.load(Ordering::Acquire),
            2,
            "no producer prefetch without next demand"
        );
        stream.next().await.unwrap().unwrap();
        stream.next().await.unwrap().unwrap();
        assert_eq!(
            stream.next().await.unwrap().unwrap_err().code,
            "test/fourth-row"
        );
        assert!(stream.next().await.is_none());
        assert!(stream.next().await.is_none());
        assert!(stream.is_terminated());
        until(|| executor.stats().resources == 0).await;
    });
    assert_eq!(calls.load(Ordering::Acquire), 4);
}

#[test]
fn general_relation_complete_async_cost_ladder_includes_admission_consume_and_cleanup() {
    let executor = executor(2, 4, 4);
    runtime().block_on(async {
        for width in [32, 128, 512] {
            let source = QueryDataSource::relation("$", (0..width).map(|n| vec![
                QueryValue::Scalar(Value::Long(n)), QueryValue::Map(vec![(QueryValue::Nil, QueryValue::Char('λ'))]),
            ]).collect());
            let query = Query::new(FindSpec::Relation(vec![FindElement::Variable("n".into()), FindElement::Variable("m".into())]),
                vec![Clause::RelationPattern(Box::new(RelationPattern::new(vec![Term::var("n"), Term::var("m")]))) ]);
            let started = Instant::now();
            let prepared = PreparedQuery::new(&query).unwrap();
            let outcome = executor.query_prepared(prepared, &[source], vec![], Default::default(), None).unwrap().await.unwrap();
            let QueryResult::Relation(rows) = &outcome.result else { panic!("relation expected"); };
            assert_eq!(rows.len(), width as usize);
            let ids = rows.iter().map(|row| match row[0] { QueryValue::Scalar(Value::Long(n)) => n, _ => panic!("integer id") })
                .collect::<std::collections::BTreeSet<_>>();
            assert_eq!(ids, (0..width).collect());
            let work = outcome.stats.work;
            let bytes = outcome.stats.allocated_value_bytes;
            drop(outcome);
            until(|| executor.stats().operations == 0 && executor.stats().running == 0).await;
            eprintln!("async relation rows={width} prepare/admit/execute/verify/result+worker-cleanup={:?} query_work={work} admitted_value_bytes={bytes}", started.elapsed());
        }
    });
}

#[test]
fn streams_resume_after_full_admission_and_honor_cancellation() {
    let executor = executor(1, 1, 1);
    let entered = Arc::new(AtomicBool::new(false));
    let release = Arc::new(AtomicBool::new(false));
    let source = QueryDataSource::tuples("$", vec![vec![Value::Long(1)]]);
    let query = Query::new(
        FindSpec::Collection(FindElement::Variable("n".into())),
        vec![Clause::RelationPattern(Box::new(RelationPattern::new(
            vec![Term::var("n")],
        )))],
    );
    runtime().block_on(async {
        let slow = executor
            .query_sources(
                extension_query(),
                &[],
                vec![],
                Default::default(),
                Some(blocking_extension(entered.clone(), release.clone())),
            )
            .unwrap();
        until(|| entered.load(Ordering::Acquire)).await;
        let mut stream = executor
            .query_sequence_sources(
                query.clone(),
                std::slice::from_ref(&source),
                vec![],
                Default::default(),
                None,
                Default::default(),
            )
            .unwrap();
        assert!(
            tokio::time::timeout(Duration::from_millis(10), stream.next())
                .await
                .is_err()
        );
        release.store(true, Ordering::Release);
        slow.await.unwrap();
        assert_eq!(
            stream.next().await.unwrap().unwrap(),
            vec![QueryValue::Scalar(Value::Long(1))]
        );
        assert!(stream.next().await.is_none());
        until(|| executor.stats().resources == 0 && executor.stats().operations == 0).await;
        let control = QueryControl::default();
        let cancel = control.cancel.clone();
        let mut stream = executor
            .query_sequence_sources(query, &[source], vec![], control, None, Default::default())
            .unwrap();
        cancel.store(true, Ordering::Release);
        assert_eq!(
            stream.next().await.unwrap().unwrap_err().category,
            ErrorCategory::Interrupted
        );
        assert!(stream.next().await.is_none());
    });
}

fn postgres_fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual PostgreSQL async facade: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    common::install(&fixture.connection).unwrap();
    drop(
        common::TestStore::connect(&fixture.connection)
            .unwrap()
            .create_database("async", schema())
            .unwrap(),
    );
    Some(fixture)
}

#[test]
fn postgres_captured_reads_dropped_transaction_retry_restart_and_cleanup() {
    let Some(fixture) = postgres_fixture("async_native") else {
        return;
    };
    let service = common::start_service(&fixture.connection, "async");
    let connection = Connection::attach(&fixture.connection, service.client(), 0).unwrap();
    let executor = executor(1, 16, 8);
    let client = AsyncClient::new(&connection, &executor).unwrap();
    let rt = runtime();
    let first = rt
        .block_on(
            client
                .transact(request("one", 1), Duration::from_secs(20))
                .unwrap(),
        )
        .unwrap();
    let first_t = first.basis_t;
    let first_entity = first.tempids["item"];
    drop(first);
    let entered = Arc::new(AtomicBool::new(false));
    let release = Arc::new(AtomicBool::new(false));
    let (slow, captured_query, captured_value, mut captured_log) = rt.block_on(async {
        let slow = executor
            .query_sources(
                extension_query(),
                &[],
                vec![],
                Default::default(),
                Some(blocking_extension(entered.clone(), release.clone())),
            )
            .unwrap();
        until(|| entered.load(Ordering::Acquire)).await;
        let query = client
            .query(score_query(), vec![], Default::default())
            .unwrap();
        let database = client.db().unwrap();
        let log = client
            .tx_range(
                None,
                None,
                AsyncStreamOptions {
                    chunk_rows: 1,
                    timeout: None,
                },
            )
            .unwrap();
        (slow, query, database, log)
    });
    // Deliberately exercise the old blocking API outside the async executor:
    // the read tasks above must not move to this successor before starting.
    let second = connection
        .transact(request("two", 2), Duration::from_secs(20))
        .unwrap();
    let second_t = second.basis_t;
    drop(second);
    let pattern = PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(1000))]);
    let pull_oracle = connection.db().pull(&pattern, first_entity).unwrap();
    release.store(true, Ordering::Release);
    let retry_request = request("dropped-waiter", 3);
    let native_results = rt.block_on(async {
        let mut native_values = Vec::new();
        slow.await.unwrap();
        assert_eq!(captured_query.await.unwrap().result, expected(&[1]));
        let captured = captured_value.await.unwrap();
        assert_eq!(captured.basis_t(), first_t);
        native_values.push(captured);
        let mut transactions = Vec::new();
        while let Some(transaction) = captured_log.next().await {
            transactions.push(transaction.unwrap());
        }
        assert_eq!(transactions.last().unwrap().t, first_t);
        assert!(
            transactions
                .iter()
                .flat_map(|tx| &tx.data)
                .any(|d| d.attribute == 1000 && d.value == Value::Long(1))
        );
        assert!(!transactions.iter().any(|tx| tx.t > first_t));
        let synced = client
            .sync_to(second_t, Duration::from_secs(20))
            .unwrap()
            .await
            .unwrap();
        assert_eq!(synced.basis_t(), second_t);
        native_values.push(synced);
        assert_eq!(
            client
                .query(score_query(), vec![], Default::default())
                .unwrap()
                .await
                .unwrap()
                .result,
            expected(&[1, 2])
        );
        let pulled = client
            .pull(
                pattern.clone(),
                first_entity,
                Default::default(),
                Some(Duration::from_secs(20)),
            )
            .unwrap()
            .await
            .unwrap();
        assert_eq!(pulled, pull_oracle);
        let ticket = client
            .transact(retry_request.clone(), Duration::from_secs(20))
            .unwrap();
        assert_eq!(ticket.request_key(), "dropped-waiter");
        drop(ticket);
        let after = client
            .sync_to(second_t + 1, Duration::from_secs(20))
            .unwrap()
            .await
            .unwrap();
        assert_eq!(after.basis_t(), second_t + 1);
        native_values.push(after);
        let retry = client
            .transact(retry_request.clone(), Duration::from_secs(20))
            .unwrap()
            .await
            .unwrap();
        assert!(retry.replayed);
        assert_eq!(retry.basis_t, second_t + 1);
        assert_eq!(retry.db_before.basis_t(), second_t);
        let target = client.request_index().unwrap().await.unwrap().target_t;
        let indexed = client
            .sync_index(target, Duration::from_secs(20))
            .unwrap()
            .await
            .unwrap();
        assert!(indexed.basis_t() >= target);
        native_values.push(indexed);
        let schema = client
            .sync_schema(target, Duration::from_secs(20))
            .unwrap()
            .await
            .unwrap();
        assert!(schema.basis_t() >= target);
        native_values.push(schema);
        let excised = client
            .sync_excise(target, Duration::from_secs(20))
            .unwrap()
            .await
            .unwrap();
        assert!(excised.basis_t() >= target);
        native_values.push(excised);
        let synced = client
            .sync(Some(Duration::from_secs(20)))
            .unwrap()
            .await
            .unwrap();
        assert!(synced.basis_t() >= target);
        native_values.push(synced);
        let read_before = connection.load_stats();
        let result = client
            .query(score_query(), vec![], Default::default())
            .unwrap()
            .await
            .unwrap();
        assert_eq!(result.result, expected(&[1, 2, 3]));
        assert!(
            connection.load_stats().cursor_sql_reads > read_before.cursor_sql_reads,
            "zero-cache indexed query performs actual worker PostgreSQL reads"
        );
        // Leave a completed immutable value unconsumed, then abandon it. Its
        // operation slot is released only after worker-side destruction.
        let abandoned = client.db().unwrap();
        until(|| executor.stats().queued == 0 && executor.stats().running == 0).await;
        drop(abandoned);
        until(|| executor.stats().operations == 0).await;
        (native_values, retry)
    });
    // Consuming a native result intentionally transfers its native last-drop
    // contract. Dispose these outside Tokio; abandoned facade values above
    // are instead destroyed by the async workers without caller intervention.
    drop(native_results);
    service.shutdown();
    assert_eq!(
        rt.block_on(
            client
                .query(score_query(), vec![], Default::default())
                .unwrap()
        )
        .unwrap()
        .result,
        expected(&[1, 2, 3])
    );
    let service = common::start_service(&fixture.connection, "async");
    connection.attach_writer(service.client()).unwrap();
    let retry = rt
        .block_on(
            client
                .transact(retry_request, Duration::from_secs(20))
                .unwrap(),
        )
        .unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, second_t + 1);
    drop(retry);
    drop(captured_log);
    drop(connection);
    rt.block_on(async {
        drop(client);
        tokio::time::sleep(Duration::from_millis(5)).await;
        until(|| executor.stats().resources == 0 && executor.stats().operations == 0).await;
    });
    service.shutdown();
}

#[cfg(unix)]
#[test]
fn postgres_real_socket_transactions_keep_exact_committed_receipts() {
    let Some(fixture) = postgres_fixture("async_socket") else {
        return;
    };
    let service = common::start_service(&fixture.connection, "async");
    let server = LocalTransactionServer::start(service.client(), Default::default()).unwrap();
    let connection = Connection::connect(&fixture.connection, "async", 0).unwrap();
    let executor = executor(2, 4, 4);
    let client = AsyncClient::new(&connection, &executor).unwrap();
    runtime().block_on(async {
        let request = request("socket", 41);
        let outcome = client
            .transact_socket(server.endpoint(), request.clone(), Duration::from_secs(20))
            .unwrap()
            .await
            .unwrap();
        assert!(!outcome.replayed);
        let basis = outcome.basis_t;
        let report = outcome.report.unwrap();
        let hash = report.tx_hash;
        drop(report);
        let replay = client
            .transact_socket(server.endpoint(), request, Duration::from_secs(20))
            .unwrap()
            .await
            .unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.basis_t, basis);
        assert_eq!(replay.tx_hash, hash);
        assert_eq!(
            client
                .query(score_query(), vec![], Default::default())
                .unwrap()
                .await
                .unwrap()
                .result,
            expected(&[41])
        );
        drop(replay);
        drop(client);
        until(|| executor.stats().operations == 0 && executor.stats().resources == 0).await;
    });
    drop(server);
    service.shutdown();
}
