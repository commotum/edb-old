use atomic_core::*;
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::Instant;

fn n(value: i64) -> QueryValue {
    QueryValue::Scalar(Value::Long(value))
}
fn find(name: &str) -> FindElement {
    FindElement::Variable(name.into())
}
fn execute(
    query: &Query,
    sources: &[QueryDataSource],
    inputs: &[QueryInput],
    control: &QueryControl,
) -> QueryOutcome {
    QueryEngine::execute_sources(query, sources, inputs, control).unwrap()
}
fn call(name: &str, args: Vec<Term>, binding: Binding) -> Clause {
    Clause::Function {
        function: Function::Extension(name.into()),
        source: "$absent".into(),
        args,
        binding,
    }
}
fn key(id: i64, reverse: bool) -> QueryValue {
    let mut entries = vec![
        (QueryValue::Char('k'), n(id)),
        (QueryValue::Nil, QueryValue::Set(vec![n(2), n(1)])),
    ];
    if reverse {
        entries.reverse();
    }
    QueryValue::Map(entries)
}

#[test]
fn wide_general_relations_use_logical_borrowed_keys_and_match_scan_oracle() {
    for width in [32_i64, 128, 1024] {
        let rows: Vec<_> = (0..width)
            .map(|id| {
                vec![
                    n(id),
                    QueryValue::Nil,
                    QueryValue::Char('λ'),
                    QueryValue::Collection(vec![]),
                    n(4),
                    key(id, false),
                    QueryValue::Tagged(Symbol::new("app", "id"), Box::new(n(id))),
                ]
            })
            .collect();
        let sources = [QueryDataSource::relation("$r", rows)];
        let mut pattern = RelationPattern::new(vec![
            Term::var("id"),
            Term::Blank,
            Term::Blank,
            Term::Blank,
            Term::Blank,
            Term::var("key"),
            Term::var("tag"),
        ]);
        pattern.source = "$r".into();
        let mut query = Query::new(
            FindSpec::Relation(vec![find("id"), find("tag")]),
            vec![Clause::RelationPattern(Box::new(pattern))],
        );
        query.inputs = vec![InputSpec::Scalar("key".into())];
        let input = [QueryInput::General(key(width - 2, true))];
        let scan = execute(
            &query,
            &sources,
            &input,
            &QueryControl {
                force_scan: true,
                max_value_bytes: usize::MAX,
                ..Default::default()
            },
        );
        let started = Instant::now();
        let optimized = execute(
            &query,
            &sources,
            &input,
            &QueryControl {
                max_join_bytes: 4096,
                max_value_bytes: usize::MAX,
                ..Default::default()
            },
        );
        assert_eq!(optimized.result, scan.result);
        assert_eq!(
            optimized.result,
            QueryResult::Relation(vec![vec![
                n(width - 2),
                QueryValue::Tagged(Symbol::new("app", "id"), Box::new(n(width - 2)))
            ]])
        );
        // One scalar input binding plus one selected source-row candidate.
        assert_eq!(optimized.stats.join_candidates, 2);
        assert_eq!(optimized.stats.hash_join_build_rows, width as u64);
        assert_eq!(optimized.stats.datoms_examined, width as u64 + 1);
        assert!(optimized.stats.peak_join_bytes <= 4096);
        let work = optimized.stats.work;
        drop(optimized);
        eprintln!(
            "general relation rows={width} columns=7 complete query/check/drop={:?} work={work} candidates=2 (input+source)",
            started.elapsed()
        );
    }
}

#[test]
fn old_typed_relations_and_general_relations_share_arbitrary_width_matching() {
    let rows: Vec<_> = (0..32_i64)
        .map(|id| {
            (0..12)
                .map(|column| Value::Long(id + column))
                .collect::<Vec<_>>()
        })
        .collect();
    let general = rows
        .iter()
        .map(|row| row.iter().cloned().map(QueryValue::Scalar).collect())
        .collect();
    let mut terms = vec![Term::Blank; 12];
    terms[0] = Term::var("id");
    terms[11] = Term::QueryConstant(n(20));
    let query = Query::new(
        FindSpec::Collection(find("id")),
        vec![Clause::RelationPattern(Box::new(RelationPattern::new(
            terms,
        )))],
    );
    for source in [
        QueryDataSource::tuples("$", rows),
        QueryDataSource::relation("$", general),
    ] {
        assert_eq!(
            execute(&query, &[source], &[], &QueryControl::default()).result,
            QueryResult::Collection(vec![n(9)])
        );
    }
}

#[test]
fn input_shapes_preserve_nil_maps_and_only_destructure_sets_as_collections() {
    let mut scalar = Query::new(FindSpec::Scalar(find("x")), vec![]);
    scalar.inputs = vec![InputSpec::Scalar("x".into())];
    let prepared = PreparedQuery::new(&scalar).unwrap();
    for value in [
        QueryValue::Nil,
        key(7, false),
        QueryValue::Char('x'),
        QueryValue::Tuple((0..12).map(n).collect()),
    ] {
        let expected = if matches!(value, QueryValue::Tuple(_)) {
            QueryValue::Scalar(Value::Tuple(
                (0..12).map(|v| Some(Value::Long(v))).collect(),
            ))
        } else {
            value.clone()
        };
        assert_eq!(
            prepared
                .execute(
                    &[],
                    &[QueryInput::General(value.clone())],
                    &QueryControl::default()
                )
                .unwrap()
                .result,
            QueryResult::Scalar(Some(expected))
        );
    }
    let mut collection = Query::new(FindSpec::Collection(find("x")), vec![]);
    collection.inputs = vec![InputSpec::Collection("x".into())];
    let set = QueryValue::Set(vec![QueryValue::Nil, key(1, false), key(1, true)]);
    assert_eq!(
        execute(
            &collection,
            &[],
            &[QueryInput::General(set.clone())],
            &QueryControl::default()
        )
        .result,
        QueryResult::Collection(vec![QueryValue::Nil, key(1, false)])
    );
    collection.inputs = vec![InputSpec::Tuple(vec![Some("x".into()), None, None])];
    assert_eq!(
        QueryEngine::execute_sources(
            &collection,
            &[],
            &[QueryInput::General(set)],
            &QueryControl::default()
        )
        .unwrap_err()
        .code,
        "query/input-shape"
    );
    collection.inputs = vec![InputSpec::Relation(vec![Some("x".into()), None])];
    let relation = QueryValue::Set(vec![QueryValue::Tuple(vec![
        key(2, false),
        QueryValue::Nil,
    ])]);
    assert_eq!(
        execute(
            &collection,
            &[],
            &[QueryInput::General(relation)],
            &QueryControl::default()
        )
        .result,
        QueryResult::Collection(vec![key(2, false)])
    );
    let bad_row = QueryValue::Collection(vec![QueryValue::Set(vec![n(1), n(2)])]);
    assert_eq!(
        QueryEngine::execute_sources(
            &collection,
            &[],
            &[QueryInput::General(bad_row)],
            &QueryControl::default()
        )
        .unwrap_err()
        .code,
        "query/input-shape"
    );
}

#[test]
fn pure_callbacks_bind_all_shapes_without_sources_and_preparation_is_registry_agnostic() {
    let mut registry = QueryExtensions::new();
    registry.register_pure("identity", |args, _| Ok(args[0].clone()));
    let value = key(42, false);
    for (binding, output, expected) in [
        (
            Binding::Scalar("out".into()),
            value.clone(),
            QueryResult::Collection(vec![value.clone()]),
        ),
        (
            Binding::Tuple(vec![Some("out".into()), None]),
            QueryValue::Tuple(vec![value.clone(), n(1)]),
            QueryResult::Collection(vec![value.clone()]),
        ),
        (
            Binding::Collection("out".into()),
            QueryValue::Set(vec![value.clone(), value.clone()]),
            QueryResult::Collection(vec![value.clone()]),
        ),
        (
            Binding::Relation(vec![Some("out".into()), None]),
            QueryValue::Collection(vec![QueryValue::Tuple(vec![
                value.clone(),
                QueryValue::Nil,
            ])]),
            QueryResult::Collection(vec![value.clone()]),
        ),
    ] {
        let query = Query::new(
            FindSpec::Collection(find("out")),
            vec![call("identity", vec![Term::QueryConstant(output)], binding)],
        );
        let prepared = PreparedQuery::new(&query).unwrap();
        assert_eq!(
            prepared
                .execute_with_extensions(&[], &[], &QueryControl::default(), &registry)
                .unwrap()
                .result,
            expected
        );
        let mut bound_registry = QueryExtensions::new();
        bound_registry.register_local("identity", |_, _, _| Ok(vec![]));
        assert_eq!(
            prepared
                .execute_with_extensions(&[], &[], &QueryControl::default(), &bound_registry)
                .unwrap_err()
                .code,
            "query/unknown-source"
        );
        assert_eq!(
            prepared
                .execute_with_extensions(&[], &[], &QueryControl::default(), &registry)
                .unwrap()
                .result,
            expected
        );
    }
}

#[test]
fn source_free_nested_pure_functions_and_rule_scope_are_independent() {
    let mut registry = QueryExtensions::new();
    registry.register_pure("id", |args, _| Ok(args[0].clone()));
    let inner = Query::new(
        FindSpec::Scalar(find("inner")),
        vec![call(
            "id",
            vec![Term::QueryConstant(key(9, false))],
            Binding::Scalar("inner".into()),
        )],
    );
    let query = Query::new(
        FindSpec::Scalar(find("out")),
        vec![Clause::Function {
            function: Function::Query(Box::new(inner)),
            source: "$missing".into(),
            args: vec![],
            binding: Binding::Scalar("out".into()),
        }],
    );
    assert_eq!(
        QueryEngine::execute_sources_with_extensions(
            &query,
            &[],
            &[],
            &QueryControl::default(),
            Some(&registry)
        )
        .unwrap()
        .result,
        QueryResult::Scalar(Some(key(9, false)))
    );
    let mut rule_query = Query::new(
        FindSpec::Scalar(find("out")),
        vec![Clause::Rule {
            source: "$missing".into(),
            name: "wrap".into(),
            args: vec![Term::var("out")],
        }],
    );
    rule_query.rules = vec![Rule {
        name: "wrap".into(),
        head: vec!["value".into()],
        required: Default::default(),
        clauses: vec![call(
            "id",
            vec![Term::QueryConstant(key(3, false))],
            Binding::Scalar("value".into()),
        )],
    }];
    assert_eq!(
        QueryEngine::execute_sources_with_extensions(
            &rule_query,
            &[],
            &[],
            &QueryControl::default(),
            Some(&registry)
        )
        .unwrap()
        .result,
        QueryResult::Scalar(Some(key(3, false)))
    );
}

#[test]
fn general_values_are_admitted_before_callbacks_and_prepared_queries_recover() {
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&calls);
    let mut registry = QueryExtensions::new();
    registry.register_pure("count", move |args, _| {
        observed.fetch_add(1, Ordering::Relaxed);
        Ok(args[0].clone())
    });
    let mut query = Query::new(
        FindSpec::Scalar(find("out")),
        vec![call(
            "count",
            vec![Term::var("input")],
            Binding::Scalar("out".into()),
        )],
    );
    query.inputs = vec![InputSpec::Scalar("input".into())];
    let prepared = PreparedQuery::new(&query).unwrap();
    let large = QueryValue::Map(vec![(
        QueryValue::Char('x'),
        QueryValue::Scalar(Value::String("x".repeat(8192))),
    )]);
    let input = [QueryInput::General(large.clone())];
    let small = QueryControl {
        max_value_bytes: 1024,
        ..Default::default()
    };
    assert_eq!(
        prepared
            .execute_with_extensions(&[], &input, &small, &registry)
            .unwrap_err()
            .code,
        "query/value-byte-limit"
    );
    assert_eq!(calls.load(Ordering::Relaxed), 0);
    assert_eq!(
        prepared
            .execute_with_extensions(&[], &input, &QueryControl::default(), &registry)
            .unwrap()
            .result,
        QueryResult::Scalar(Some(large))
    );
    small.cancel.store(true, Ordering::Relaxed);
    assert_eq!(
        prepared
            .execute_with_extensions(&[], &input, &small, &registry)
            .unwrap_err()
            .code,
        "query/canceled"
    );
    small.cancel.store(false, Ordering::Relaxed);
    let duplicate_keys = QueryValue::Map(vec![
        (n(1), n(2)),
        (QueryValue::Scalar(Value::Double(1.0)), n(3)),
    ]);
    assert_eq!(
        prepared
            .execute_with_extensions(
                &[],
                &[QueryInput::General(duplicate_keys)],
                &QueryControl::default(),
                &registry
            )
            .unwrap_err()
            .code,
        "query/duplicate-map-key"
    );
}

#[test]
fn pure_callback_errors_panics_and_cancellation_are_contained() {
    let query = Query::new(
        FindSpec::Scalar(find("out")),
        vec![call("f", vec![], Binding::Scalar("out".into()))],
    );
    let mut registry = QueryExtensions::new();
    registry.register_pure("f", |_, _| panic!("trusted callback panic witness"));
    assert_eq!(
        QueryEngine::execute_sources_with_extensions(
            &query,
            &[],
            &[],
            &QueryControl::default(),
            Some(&registry)
        )
        .unwrap_err()
        .code,
        "query/local-extension-panicked"
    );
    registry.register_pure("f", |_, control| {
        control.cancel.store(true, Ordering::Relaxed);
        Ok(QueryValue::Nil)
    });
    assert_eq!(
        QueryEngine::execute_sources_with_extensions(
            &query,
            &[],
            &[],
            &QueryControl::default(),
            Some(&registry)
        )
        .unwrap_err()
        .code,
        "query/canceled"
    );
    registry.register_pure("f", |_, _| Ok(QueryValue::Nil));
    assert_eq!(
        QueryEngine::execute_sources_with_extensions(
            &query,
            &[],
            &[],
            &QueryControl::default(),
            Some(&registry)
        )
        .unwrap()
        .result,
        QueryResult::Scalar(Some(QueryValue::Nil))
    );
}

#[test]
fn deep_general_tuples_remain_query_only_on_a_small_execution_stack() {
    const CHILD: &str = "ATOMIC_GENERAL_TUPLE_DEPTH_CHILD";
    if std::env::var_os(CHILD).is_none() {
        let output = std::process::Command::new(std::env::current_exe().unwrap())
            .arg("deep_general_tuples_remain_query_only_on_a_small_execution_stack")
            .arg("--exact")
            .arg("--nocapture")
            .env(CHILD, "1")
            .output()
            .unwrap();
        assert!(
            output.status.success(),
            "isolated small-stack query failed: {}",
            String::from_utf8_lossy(&output.stderr)
        );
        return;
    }
    std::thread::Builder::new()
        .stack_size(256 * 1024)
        .spawn(|| {
            let mut value = QueryValue::Nil;
            for _ in 0..10_000 {
                value = QueryValue::Tuple(vec![QueryValue::Nil, value]);
            }
            let mut query = Query::new(FindSpec::Scalar(find("value")), vec![]);
            query.inputs = vec![InputSpec::Scalar("value".into())];
            let outcome = execute(
                &query,
                &[],
                &[QueryInput::General(value.clone())],
                &QueryControl::default(),
            );
            let QueryResult::Scalar(Some(result)) = outcome.result else {
                panic!("scalar result");
            };
            assert!(
                matches!(result, QueryValue::Tuple(_)),
                "deep data must not become recursively owned stored tuples"
            );
            assert!(result == value);
            drop(result);
            drop(value);
        })
        .unwrap()
        .join()
        .unwrap();
}

#[test]
fn get_else_accepts_general_non_nil_defaults_and_preserves_found_stored_values() {
    const ATTRIBUTE: u32 = 1000;
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ATTRIBUTE,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let database = Database::new(schema).unwrap();
    let report = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ATTRIBUTE,
                value: TxValue::Scalar(Value::Long(7)),
            }],
            1000,
        )
        .unwrap();
    let entity = report.tempids["item"];
    let mut query = Query::new(
        FindSpec::Scalar(find("out")),
        vec![Clause::Function {
            function: Function::GetElse,
            source: "$".into(),
            args: vec![
                Term::Constant(Value::Ref(entity)),
                Term::Constant(Value::Ref(u64::from(ATTRIBUTE))),
                Term::var("fallback"),
            ],
            binding: Binding::Scalar("out".into()),
        }],
    );
    query.inputs = vec![InputSpec::Scalar("fallback".into())];
    for fallback in [
        key(1, false),
        QueryValue::Char('x'),
        QueryValue::Set(vec![n(1)]),
    ] {
        let inputs = [QueryInput::General(fallback.clone())];
        assert_eq!(
            database
                .query(&query, &inputs, &QueryControl::default())
                .unwrap()
                .result,
            QueryResult::Scalar(Some(fallback))
        );
        assert_eq!(
            report
                .db_after
                .query(&query, &inputs, &QueryControl::default())
                .unwrap()
                .result,
            QueryResult::Scalar(Some(n(7)))
        );
    }
    for database in [&database, &report.db_after] {
        assert_eq!(
            database
                .query(
                    &query,
                    &[QueryInput::General(QueryValue::Nil)],
                    &QueryControl::default()
                )
                .unwrap_err()
                .code,
            "query/get-else-nil-default"
        );
    }
}

#[test]
fn general_predicate_and_existing_binding_comparisons_share_limits() {
    let mut registry = QueryExtensions::new();
    registry.register_pure("identity", |args, _| Ok(args[0].clone()));
    for clause in [
        Clause::Predicate {
            predicate: Predicate::Eq,
            source: "$unused".into(),
            args: vec![Term::var("left"), Term::var("right")],
        },
        call(
            "identity",
            vec![Term::var("right")],
            Binding::Scalar("left".into()),
        ),
    ] {
        let mut query = Query::new(FindSpec::Scalar(find("left")), vec![clause]);
        query.inputs = vec![
            InputSpec::Scalar("left".into()),
            InputSpec::Scalar("right".into()),
        ];
        let prepared = PreparedQuery::new(&query).unwrap();
        let value = QueryValue::Map(
            (0..32)
                .map(|index| (n(index), QueryValue::Set(vec![n(2), n(1), n(index)])))
                .collect(),
        );
        let mut reversed = value.clone().into_map().unwrap();
        reversed.reverse();
        let inputs = [
            QueryInput::General(value.clone()),
            QueryInput::General(QueryValue::Map(reversed)),
        ];
        let complete = prepared
            .execute_with_extensions(&[], &inputs, &QueryControl::default(), &registry)
            .unwrap();
        assert_eq!(complete.result, QueryResult::Scalar(Some(value.clone())));
        for limited in [
            QueryControl {
                max_work: complete.stats.work as usize - 1,
                ..Default::default()
            },
            QueryControl {
                max_value_bytes: complete.stats.allocated_value_bytes - 1,
                ..Default::default()
            },
        ] {
            let error = prepared
                .execute_with_extensions(&[], &inputs, &limited, &registry)
                .unwrap_err();
            assert!(matches!(
                error.code,
                "query/work-limit" | "query/value-byte-limit"
            ));
        }
        assert_eq!(
            prepared
                .execute_with_extensions(&[], &inputs, &QueryControl::default(), &registry)
                .unwrap()
                .result,
            QueryResult::Scalar(Some(value))
        );
    }
}

#[test]
fn legacy_shallow_tuple_find_results_preserve_native_representation_at_any_width() {
    for width in [1, 9] {
        let mut inner = Query::new(
            FindSpec::Tuple((0..width).map(|_| find("value")).collect()),
            vec![],
        );
        inner.inputs = vec![InputSpec::Scalar("value".into())];
        let query = Query::new(
            FindSpec::Scalar(find("out")),
            vec![Clause::Function {
                function: Function::Query(Box::new(inner)),
                source: "$".into(),
                args: vec![Term::Constant(Value::Long(7))],
                binding: Binding::Scalar("out".into()),
            }],
        );
        let database = Database::new(Schema::new()).unwrap();
        assert_eq!(
            database
                .query(&query, &[], &QueryControl::default())
                .unwrap()
                .result,
            QueryResult::Scalar(Some(QueryValue::Scalar(Value::Tuple(vec![
                Some(
                    Value::Long(7)
                );
                width
            ]))))
        );
    }
}
