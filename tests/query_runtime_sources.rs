mod common;

use atomic_core::sql_io::{OperationContext, OperationKind};
use atomic_core::*;
use std::sync::{Arc, atomic::AtomicBool};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const SCORE: u32 = 1000;
fn v(name: &str) -> Variable {
    name.into()
}
fn c(value: Value) -> Term {
    Term::Constant(value)
}
fn pattern(source: &str, entity: Term, attribute: Term, value: Term) -> Clause {
    Clause::Pattern(Box::new(DataPattern {
        source: source.into(),
        ..DataPattern::new(entity, attribute, value)
    }))
}
fn schema() -> Schema {
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        SCORE,
        Keyword::new("item", "score"),
        ValueType::Long,
        Cardinality::One,
    );
    attribute.indexed = true;
    schema.install(attribute).unwrap();
    schema
}
fn database(count: i64) -> Database {
    Database::new(schema())
        .unwrap()
        .with(
            &(0..count)
                .map(|n| TxOp::Add {
                    entity: EntityRef::Temp(format!("e{n}")),
                    attribute: SCORE,
                    value: Value::Long(n).into(),
                })
                .collect::<Vec<_>>(),
            1000,
        )
        .unwrap()
        .db_after
}
fn all_scores() -> Query {
    Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(v("e")),
            FindElement::Variable(v("score")),
        ]),
        vec![pattern(
            "$",
            Term::var("e"),
            c(Value::Long(SCORE.into())),
            Term::var("score"),
        )],
    )
}
fn canonical_relation(result: QueryResult) -> Vec<QueryValue> {
    let QueryResult::Relation(rows) = result else {
        panic!("relation expected");
    };
    let mut rows: Vec<_> = rows.into_iter().map(QueryValue::Tuple).collect();
    rows.sort_by(QueryValue::canonical_cmp);
    rows
}

#[test]
fn identical_patterns_run_over_native_database_and_raw_datoms() {
    let database = database(30).database_value();
    let query = all_scores();
    let native = QueryEngine::execute_sources(
        &query,
        &[QueryDataSource::database("$", database.clone())],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    let raw = QueryEngine::execute_sources(
        &query,
        &[QueryDataSource::datoms(
            "$",
            database.collect_datoms(IndexOrder::Eavt).unwrap(),
        )],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    assert_eq!(native.result, raw.result);
    assert!(raw.stats.hash_join_build_rows > 0);
    let keyword_query = Query::new(
        FindSpec::Collection(FindElement::Variable(v("e"))),
        vec![pattern(
            "$",
            Term::var("e"),
            c(Value::Keyword(Keyword::new("item", "score"))),
            Term::Blank,
        )],
    );
    let rows = vec![vec![
        Value::String("fred".into()),
        Value::Keyword(Keyword::new("item", "score")),
        Value::Long(7),
    ]];
    assert_eq!(
        QueryEngine::execute_sources(
            &keyword_query,
            &[QueryDataSource::tuples("$", rows)],
            &[],
            &QueryControl::default()
        )
        .unwrap()
        .result,
        QueryResult::Collection(vec![QueryValue::Scalar(Value::String("fred".into()))])
    );
}

fn relation_query() -> Query {
    let mut query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(v("key")),
            FindElement::Variable(v("left")),
            FindElement::Variable(v("right")),
        ]),
        vec![],
    );
    query.inputs = vec![
        InputSpec::Relation(vec![Some(v("key")), Some(v("left"))]),
        InputSpec::Relation(vec![Some(v("key")), Some(v("right"))]),
    ];
    query
}
fn relation_inputs(count: i64) -> Vec<QueryInput> {
    vec![
        QueryInput::Relation(
            (0..count)
                .map(|n| vec![Value::Long(n), Value::Long(n + 1)])
                .collect(),
        ),
        QueryInput::Relation(
            (0..count)
                .map(|n| {
                    vec![
                        Value::BigDec(format!("{n}.000").parse().unwrap()),
                        Value::Long(n + 2),
                    ]
                })
                .collect(),
        ),
    ]
}

#[test]
fn growing_numeric_relations_use_bounded_hash_joins_and_preserve_reference_results() {
    for count in [32, 128, 512] {
        let inputs = relation_inputs(count);
        let start = Instant::now();
        let fast =
            QueryEngine::execute_sources(&relation_query(), &[], &inputs, &QueryControl::default())
                .unwrap();
        let elapsed = start.elapsed();
        let slow = QueryEngine::execute_sources(
            &relation_query(),
            &[],
            &inputs,
            &QueryControl {
                force_scan: true,
                ..Default::default()
            },
        )
        .unwrap();
        assert_eq!(fast.result, slow.result);
        assert_eq!(fast.stats.rows_produced, count as u64);
        assert_eq!(fast.stats.hash_join_build_rows, count as u64);
        assert_eq!(fast.stats.hash_join_probes, count as u64);
        assert_eq!(fast.stats.join_candidates, count as u64 * 2);
        assert_eq!(
            slow.stats.join_candidates,
            count as u64 + (count * count) as u64
        );
        eprintln!(
            "query_join rows={count} fast_work={} reference_work={} candidates={}/{} table_peak={} accounted_allocations={} wall_us={}",
            fast.stats.work,
            slow.stats.work,
            fast.stats.join_candidates,
            slow.stats.join_candidates,
            fast.stats.peak_join_bytes,
            fast.stats.allocated_value_bytes,
            elapsed.as_micros()
        );
    }
    let small = QueryEngine::execute_sources(
        &relation_query(),
        &[],
        &relation_inputs(65),
        &QueryControl {
            max_join_bytes: 128 * 8,
            ..Default::default()
        },
    )
    .unwrap();
    assert!(small.stats.peak_join_bytes <= 128 * 8);
    assert_eq!(small.stats.rows_produced, 65);
    let no_table = QueryEngine::execute_sources(
        &relation_query(),
        &[],
        &relation_inputs(20),
        &QueryControl {
            max_join_bytes: 0,
            ..Default::default()
        },
    )
    .unwrap();
    assert_eq!(no_table.stats.hash_join_build_rows, 0);
    assert_eq!(no_table.stats.rows_produced, 20);
}

#[test]
fn logical_numeric_hash_handles_floats_refs_decimal_scales_infinities_and_tuples() {
    let left = vec![
        Value::Long(0),
        Value::Long(1),
        Value::Double(0.5),
        Value::Double(f64::NAN),
        Value::Double(f64::INFINITY),
        Value::Tuple(vec![Some(Value::Long(3)), None]),
    ];
    let right = vec![
        Value::Double(-0.0),
        Value::Ref(1),
        Value::BigDec("0.500".parse().unwrap()),
        Value::Float(f32::NAN),
        Value::Float(f32::INFINITY),
        Value::Tuple(vec![Some(Value::BigDec("3.0".parse().unwrap())), None]),
    ];
    let rows = |values: Vec<Value>| {
        QueryInput::Relation(
            values
                .into_iter()
                .enumerate()
                .map(|(n, key)| vec![key, Value::Long(n as i64)])
                .collect(),
        )
    };
    let mut repeated = right.clone();
    repeated.extend(right.clone());
    let inputs = [rows(left), rows(repeated)];
    let mut query = relation_query();
    query.find = FindSpec::Scalar(FindElement::Aggregate {
        function: Aggregate::Count,
        variable: v("key"),
    });
    // Duplicate right tuples differ in the retained projection. :with controls
    // aggregate multiplicity; hashing must not collapse those rows prematurely.
    query.with = vec![v("right")];
    let fast =
        QueryEngine::execute_sources(&query, &[], &inputs, &QueryControl::default()).unwrap();
    let reference = QueryEngine::execute_sources(
        &query,
        &[],
        &inputs,
        &QueryControl {
            force_scan: true,
            ..Default::default()
        },
    )
    .unwrap();
    assert_eq!(fast.result, reference.result);
    assert_eq!(
        fast.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(12))))
    );
}

#[test]
fn grouped_database_probes_keep_selective_seeks_and_duplicate_multiplicity() {
    let database = database(10);
    let mut query = all_scores();
    query.inputs = vec![InputSpec::Relation(vec![
        Some(v("score")),
        Some(v("request")),
    ])];
    query.find = FindSpec::Relation(vec![
        FindElement::Variable(v("e")),
        FindElement::Variable(v("request")),
    ]);
    let inputs = [QueryInput::Relation(
        (0..80)
            .map(|n| vec![Value::Long(n % 2), Value::Long(n)])
            .collect(),
    )];
    let fast = database
        .query(&query, &inputs, &QueryControl::default())
        .unwrap();
    let reference = database
        .query(
            &query,
            &inputs,
            &QueryControl {
                force_scan: true,
                ..Default::default()
            },
        )
        .unwrap();
    // Query relations have set semantics, not an ORDER BY contract. Grouping
    // may enumerate equal rows differently from the row-at-a-time reference.
    assert_eq!(
        canonical_relation(fast.result),
        canonical_relation(reference.result)
    );
    assert_eq!(fast.stats.index_seeks, 2);
    assert_eq!(fast.stats.grouped_probes_saved, 78);
    assert_eq!(fast.stats.rows_produced, 80);
    assert!(fast.stats.datoms_examined < reference.stats.datoms_examined);
    assert!(fast.plan.iter().any(|step| step.access == "AVET seek"));
}

#[test]
fn prepared_queries_rebind_sources_inputs_and_validate_new_schema_even_when_empty() {
    let mut query = all_scores();
    query.inputs = vec![InputSpec::Scalar(v("score"))];
    let mut cache = PreparedQueryCache::new(2, 128 * 1024);
    let prepared = cache.prepare(&query).unwrap();
    cache.prepare(&query.clone()).unwrap();
    assert_eq!(cache.stats().hits, 1);
    let database = database(2).database_value();
    let input = |n| [QueryInput::Scalar(Value::Long(n))];
    let first = prepared
        .execute(
            &[QueryDataSource::database("$", database.clone())],
            &input(0),
            &QueryControl::default(),
        )
        .unwrap();
    let second = prepared
        .execute(
            &[QueryDataSource::database("$", database)],
            &input(1),
            &QueryControl::default(),
        )
        .unwrap();
    assert_ne!(first.result, second.result);
    assert!(
        prepared
            .execute(
                &[QueryDataSource::database(
                    "$",
                    Database::new(Schema::new()).unwrap().database_value()
                )],
                &input(0),
                &QueryControl::default()
            )
            .is_err()
    );
    assert_eq!(
        prepared
            .execute(&[], &input(0), &QueryControl::default())
            .unwrap_err()
            .code,
        "query/unknown-source"
    );
    let mut empty_query = query.clone();
    empty_query.inputs = vec![InputSpec::Collection(v("score"))];
    let empty_prepared = cache.prepare(&empty_query).unwrap();
    assert!(
        empty_prepared
            .execute(
                &[QueryDataSource::database(
                    "$",
                    Database::new(Schema::new()).unwrap().database_value()
                )],
                &[QueryInput::Collection(vec![])],
                &QueryControl::default()
            )
            .is_err(),
        "cached preparation must validate source schema even for empty inputs"
    );
    for name in ["a", "b", "c"] {
        let mut renamed = query.clone();
        renamed.with = vec![v(name)];
        cache.prepare(&renamed).unwrap();
    }
    assert!(cache.stats().entries <= 2 && cache.stats().evictions >= 2);
    assert!(cache.stats().retained_weight <= 128 * 1024);
}

#[test]
fn prepared_keys_do_not_conflate_numeric_representation_or_capture_extensions() {
    let make = |value| {
        Query::new(
            FindSpec::Scalar(FindElement::Variable(v("value"))),
            vec![Clause::Function {
                function: Function::Ground,
                source: "$".into(),
                args: vec![c(value)],
                binding: Binding::Scalar(v("value")),
            }],
        )
    };
    let mut cache = PreparedQueryCache::new(4, 64 * 1024);
    let integer = cache.prepare(&make(Value::Long(1))).unwrap();
    let reference = cache.prepare(&make(Value::Ref(1))).unwrap();
    assert_eq!(cache.stats().hits, 0);
    assert!(matches!(
        integer
            .execute(&[], &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(1))))
    ));
    assert!(matches!(
        reference
            .execute(&[], &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Ref(1))))
    ));
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable(v("value"))),
        vec![Clause::Function {
            function: Function::Extension("runtime".into()),
            source: "$".into(),
            args: vec![],
            binding: Binding::Scalar(v("value")),
        }],
    );
    let prepared = cache.prepare(&query).unwrap();
    let sources = [QueryDataSource::database("$", database(1).database_value())];
    for expected in [10, 20] {
        let mut extensions = QueryExtensions::default();
        extensions.register_local("runtime", move |_, _, _| {
            Ok(vec![vec![Value::Long(expected)]])
        });
        assert_eq!(
            prepared
                .execute_with_extensions(&sources, &[], &QueryControl::default(), &extensions)
                .unwrap()
                .result,
            QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(expected))))
        );
    }
}

#[test]
fn prepared_cache_bypasses_callbacks_and_oversized_keys_without_constant_collisions() {
    let ground = |value| {
        Query::new(
            FindSpec::Scalar(FindElement::Variable(v("value"))),
            vec![Clause::Function {
                function: Function::Ground,
                source: "$".into(),
                args: vec![c(value)],
                binding: Binding::Scalar(v("value")),
            }],
        )
    };
    let mut cache = PreparedQueryCache::new(3, 4096);
    for bits in [0x7ff8_0000_0000_0001u64, 0xfff8_0000_0000_0002] {
        let prepared = cache
            .prepare(&ground(Value::Double(f64::from_bits(bits))))
            .unwrap();
        let QueryResult::Scalar(Some(QueryValue::Scalar(Value::Double(value)))) = prepared
            .execute(&[], &[], &QueryControl::default())
            .unwrap()
            .result
        else {
            panic!("float expected");
        };
        assert_eq!(value.to_bits(), bits);
    }
    assert_eq!(cache.stats().hits, 0);
    let before = cache.stats().entries;
    for suffix in ["left", "right"] {
        let query = ground(Value::String(format!("{}{suffix}", "large".repeat(1000))));
        let prepared = cache.prepare(&query).unwrap();
        assert_eq!(prepared.query(), &query);
    }
    assert_eq!(
        cache.stats().entries,
        before,
        "oversized keys must not cache a common truncated prefix"
    );
    for (literal, scale) in [("1.0", 1), ("1.000", 3)] {
        let prepared = cache
            .prepare(&ground(Value::BigDec(literal.parse().unwrap())))
            .unwrap();
        let QueryResult::Scalar(Some(QueryValue::Scalar(Value::BigDec(ref value)))) = prepared
            .execute(&[], &[], &QueryControl::default())
            .unwrap()
            .result
        else {
            panic!("decimal expected");
        };
        assert_eq!(value.as_bigint_and_exponent().1, scale);
    }
    let database = database(1).database_value();
    for expected in [10, 20] {
        let mut selector = PullAttribute::forward(AttributeName::Id(SCORE));
        selector.transform = Some(PullTransform::new("same-name", move |_| {
            Ok(QueryValue::Scalar(Value::Long(expected)))
        }));
        let query = Query::new(
            FindSpec::Scalar(FindElement::Pull {
                source: "$".into(),
                variable: v("e"),
                pattern: Box::new(PullPattern::attributes(vec![selector])),
            }),
            all_scores().clauses,
        );
        let outcome = QueryEngine::execute_sources(
            &query,
            &[QueryDataSource::database("$", database.clone())],
            &[],
            &QueryControl::default(),
        )
        .unwrap();
        assert_eq!(outcome.stats.prepared_cache_hits, 0);
        let QueryResult::Scalar(Some(QueryValue::Map(ref entries))) = outcome.result else {
            panic!("map expected");
        };
        assert!(
            entries
                .iter()
                .any(|(_, value)| value == &QueryValue::Scalar(Value::Long(expected)))
        );
    }
}

#[test]
fn raw_tuple_rules_inherit_sources_and_history_components_remain_literal() {
    let edge = |left, right| {
        pattern(
            "$",
            Term::var(left),
            c(Value::String("edge".into())),
            Term::var(right),
        )
    };
    let mut query = Query::new(
        FindSpec::Collection(FindElement::Variable(v("to"))),
        vec![Clause::Rule {
            source: "$graph".into(),
            name: "reach".into(),
            args: vec![c(Value::Long(0)), Term::var("to")],
        }],
    );
    query.rules = vec![
        Rule {
            name: "reach".into(),
            head: vec![v("from"), v("to")],
            required: [0].into(),
            clauses: vec![edge("from", "to")],
        },
        Rule {
            name: "reach".into(),
            head: vec![v("from"), v("to")],
            required: [0].into(),
            clauses: vec![
                edge("from", "mid"),
                Clause::Rule {
                    source: "$".into(),
                    name: "reach".into(),
                    args: vec![Term::var("mid"), Term::var("to")],
                },
            ],
        },
    ];
    let sources = [QueryDataSource::tuples(
        "$graph",
        (0..20)
            .map(|n| {
                vec![
                    Value::Long(n),
                    Value::String("edge".into()),
                    Value::Long(n + 1),
                    Value::Long(1),
                    Value::Bool(true),
                ]
            })
            .collect(),
    )];
    let outcome =
        QueryEngine::execute_sources(&query, &sources, &[], &QueryControl::default()).unwrap();
    assert_eq!(outcome.stats.rows_produced, 20);
    assert!(outcome.stats.rule_iterations > 0);
    let mut history = DataPattern::new(Term::var("e"), Term::Blank, Term::var("value"));
    history.source = "$history".into();
    history.transaction = Some(Term::var("tx"));
    history.added = Some(c(Value::Bool(false)));
    let query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(v("value")),
            FindElement::Variable(v("tx")),
        ]),
        vec![Clause::Pattern(Box::new(history))],
    );
    let raw = [QueryDataSource::tuples(
        "$history",
        vec![
            vec![
                Value::Long(1),
                Value::String("a".into()),
                Value::String("old".into()),
                Value::Long(2),
                Value::Bool(false),
            ],
            vec![
                Value::Long(1),
                Value::String("a".into()),
                Value::String("new".into()),
                Value::Long(2),
                Value::Bool(true),
            ],
        ],
    )];
    assert_eq!(
        QueryEngine::execute_sources(&query, &raw, &[], &QueryControl::default())
            .unwrap()
            .stats
            .rows_produced,
        1
    );
}

#[test]
fn chunked_raw_sources_keep_repeated_variables_duplicates_and_control_limits() {
    let query = Query::new(
        FindSpec::Collection(FindElement::Variable(v("same"))),
        vec![pattern(
            "$",
            Term::var("same"),
            Term::Blank,
            Term::var("same"),
        )],
    );
    let tuples = (0..40)
        .flat_map(|n| {
            [
                vec![Value::Long(n), Value::Bool(true), Value::Long(n)],
                vec![Value::Long(n), Value::Bool(false), Value::Long(n + 1)],
            ]
        })
        .collect();
    let sources = [QueryDataSource::tuples("$", tuples)];
    let result =
        QueryEngine::execute_sources(&query, &sources, &[], &QueryControl::default()).unwrap();
    assert_eq!(result.stats.rows_produced, 40);
    for control in [
        QueryControl {
            max_work: 5,
            ..Default::default()
        },
        QueryControl {
            max_intermediate_rows: 5,
            ..Default::default()
        },
        QueryControl {
            cancel: Arc::new(AtomicBool::new(true)),
            ..Default::default()
        },
        QueryControl {
            timeout: Some(Duration::ZERO),
            ..Default::default()
        },
    ] {
        assert!(QueryEngine::execute_sources(&query, &sources, &[], &control).is_err());
    }
}

#[test]
fn log_functions_join_provenance_and_keep_captured_basis_on_actual_postgres() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP query log PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "query_runtime_sources");
    let url = fixture.connection.clone();
    common::install(&url).unwrap();
    let id = format!(
        "query_log_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    let mut log_schema = Schema::new();
    let mut score = Attribute::new(
        SCORE,
        Keyword::new("item", "score"),
        ValueType::Long,
        Cardinality::One,
    );
    score.indexed = true;
    score.no_history = true;
    log_schema.install(score).unwrap();
    common::TestStore::connect(&url)
        .unwrap()
        .create_database(&id, log_schema)
        .unwrap();
    let service = common::start_service(&url, &id);
    let connection = Connection::connect(&url, &id, 4).unwrap();
    let write = |key: &str, entity, score| {
        service
            .client()
            .transact(
                TransactionRequest::new(
                    key,
                    vec![TxOp::Add {
                        entity,
                        attribute: SCORE,
                        value: Value::Long(score).into(),
                    }],
                )
                .with_tx_instant(1000),
                Duration::from_secs(30),
            )
            .unwrap()
    };
    let first = write("one", EntityRef::Temp("item".into()), 10);
    let entity = first.tempids["item"];
    let second = write("two", EntityRef::Id(entity), 20);
    connection
        .sync_to(second.basis_t, Duration::from_secs(30))
        .unwrap();
    let captured = connection.log();
    let third = write("three", EntityRef::Id(entity), 30);
    connection
        .sync_to(third.basis_t, Duration::from_secs(30))
        .unwrap();
    common::consolidate(&url, &id).unwrap();
    connection
        .sync_index(third.basis_t, Duration::from_secs(30))
        .unwrap();
    assert!(
        !connection
            .db()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .any(|datom| datom.attribute == SCORE && datom.value == Value::Long(10)),
        "log query must not substitute pruned indexed history"
    );
    let query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(v("value")),
            FindElement::Variable(v("added")),
            FindElement::Variable(v("tx")),
            FindElement::Variable(v("recorded")),
        ]),
        vec![
            Clause::Function {
                function: Function::TxIds,
                source: "$log".into(),
                args: vec![c(Value::Long(first.basis_t as i64)), Term::Nil],
                binding: Binding::Collection(v("tx")),
            },
            Clause::Function {
                function: Function::TxData,
                source: "$log".into(),
                args: vec![Term::var("tx")],
                binding: Binding::Relation(vec![
                    Some(v("e")),
                    Some(v("a")),
                    Some(v("value")),
                    Some(v("tx")),
                    Some(v("added")),
                ]),
            },
            Clause::Predicate {
                predicate: Predicate::Eq,
                source: "$".into(),
                args: vec![Term::var("a"), c(Value::Long(SCORE.into()))],
            },
            pattern(
                "$metadata",
                Term::var("tx"),
                c(Value::Keyword(Keyword::new("db", "txInstant"))),
                Term::var("recorded"),
            ),
        ],
    );
    let io = OperationContext::new(OperationKind::Query);
    let outcome = {
        let _scope = io.enter();
        QueryEngine::execute_sources(
            &query,
            &[
                QueryDataSource::log("$log", captured),
                QueryDataSource::database("$metadata", connection.db()),
            ],
            &[],
            &QueryControl::default(),
        )
        .unwrap()
    };
    assert_eq!(outcome.stats.rows_produced, 3);
    assert!(
        io.snapshot().sql_calls > 0,
        "actual authenticated native log reads ran"
    );
    let QueryResult::Relation(rows) = outcome.result else {
        panic!("relation expected");
    };
    assert!(
        rows.iter()
            .all(|row| row[3] == QueryValue::Scalar(Value::Instant(1000)))
    );
    assert!(
        rows.iter()
            .any(|row| row[0] == QueryValue::Scalar(Value::Long(10))
                && row[1] == QueryValue::Scalar(Value::Bool(false)))
    );
    assert!(
        !rows
            .iter()
            .any(|row| row[0] == QueryValue::Scalar(Value::Long(30)))
    );
    eprintln!(
        "query log PostgreSQL witness: {} result rows, SQL={:?}",
        rows.len(),
        io.snapshot()
    );
    let peer = Peer::connect(&url, &id, 16).unwrap();
    let captured = peer.db();
    let mut query = all_scores();
    query.inputs = vec![InputSpec::Relation(vec![
        Some(v("score")),
        Some(v("request")),
    ])];
    query.find = FindSpec::Relation(vec![
        FindElement::Variable(v("e")),
        FindElement::Variable(v("request")),
    ]);
    let inputs = |count| {
        [QueryInput::Relation(
            (0..count)
                .map(|n| vec![Value::Long(30), Value::Long(n)])
                .collect(),
        )]
    };
    let cold = OperationContext::new(OperationKind::Query);
    {
        let _scope = cold.enter();
        captured
            .query(&query, &inputs(1), &QueryControl::default())
            .unwrap();
    }
    assert!(
        cold.snapshot().sql_calls > 0,
        "fresh peer actually read a published AVET path"
    );
    for count in [10, 100, 1000] {
        let warm = OperationContext::new(OperationKind::Query);
        let start = Instant::now();
        let outcome = {
            let _scope = warm.enter();
            captured
                .query(&query, &inputs(count), &QueryControl::default())
                .unwrap()
        };
        assert_eq!(outcome.stats.rows_produced, count as u64);
        assert_eq!(outcome.stats.index_seeks, 1);
        assert_eq!(outcome.stats.grouped_probes_saved, count as u64 - 1);
        assert_eq!(
            warm.snapshot().sql_calls,
            0,
            "warm query must not run foreground SQL probes"
        );
        eprintln!(
            "query PostgreSQL grouped requests={count} matches={} seeks={} datoms={} sql_calls=0 wall_us={} cold_sql_calls={}",
            outcome.stats.rows_produced,
            outcome.stats.index_seeks,
            outcome.stats.datoms_examined,
            start.elapsed().as_micros(),
            cold.snapshot().sql_calls
        );
    }
    service.shutdown();
}
