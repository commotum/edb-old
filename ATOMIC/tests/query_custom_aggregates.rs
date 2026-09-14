use atomic_core::*;
use num_bigint::BigInt;
use std::sync::{
    Arc,
    atomic::{AtomicBool, AtomicUsize, Ordering},
};
use std::time::Instant;

fn n(value: i64) -> QueryValue {
    QueryValue::Scalar(Value::Long(value))
}
fn var(name: &str) -> AggregateArg {
    AggregateArg::Variable(name.into())
}
fn custom(name: &str, args: Vec<AggregateArg>) -> FindElement {
    FindElement::CustomAggregate(Box::new(AggregateCall::new(name, args)))
}
fn relation(find: FindSpec, names: &[&str]) -> Query {
    let mut query = Query::new(find, vec![]);
    query.inputs = vec![InputSpec::Relation(
        names.iter().map(|name| Some((*name).into())).collect(),
    )];
    query
}
fn input(rows: Vec<Vec<i64>>) -> QueryInput {
    QueryInput::Relation(
        rows.into_iter()
            .map(|row| row.into_iter().map(Value::Long).collect())
            .collect(),
    )
}
fn long(value: AggregateValue<'_>) -> i64 {
    match value {
        AggregateValue::Stored(Value::Long(value))
        | AggregateValue::Query(QueryValue::Scalar(Value::Long(value))) => *value,
        other => panic!("unexpected integer cell {other:?}"),
    }
}
fn run(query: &Query, inputs: &[QueryInput], extensions: &QueryExtensions) -> QueryOutcome {
    QueryEngine::execute_sources_with_extensions(
        query,
        &[],
        inputs,
        &QueryControl::default(),
        Some(extensions),
    )
    .unwrap()
}
fn weighted_registry() -> QueryExtensions {
    let mut extensions = QueryExtensions::new();
    extensions.register_aggregate("app/weighted", |group, control| {
        assert!(!control.force_scan || control.max_work > 0);
        let Some(QueryValue::Scalar(Value::Long(scale))) = group.constant(2) else {
            panic!("scale")
        };
        let mut weighted = 0;
        let mut weight = 0;
        for row in 0..group.len() {
            group.check(1)?;
            let value = long(group.value(row, 0).unwrap());
            let current_weight = long(group.value(row, 1).unwrap());
            weighted += value * current_weight;
            weight += current_weight;
        }
        Ok(if weight == 0 {
            QueryValue::Nil
        } else {
            n(weighted * scale / weight)
        })
    });
    extensions
}

#[test]
fn aligned_weighted_arguments_group_and_preserve_with_bags() {
    let aggregate = custom(
        "app/weighted",
        vec![var("price"), var("weight"), AggregateArg::Constant(n(100))],
    );
    let mut query = relation(
        FindSpec::Relation(vec![FindElement::Variable("category".into()), aggregate]),
        &["id", "category", "price", "weight"],
    );
    let rows = vec![
        vec![0, 1, 10, 1],
        vec![1, 1, 10, 1],
        vec![2, 1, 40, 2],
        vec![3, 2, 8, 3],
    ];
    let registry = weighted_registry();
    assert_eq!(
        run(&query, &[input(rows.clone())], &registry).result,
        QueryResult::Relation(vec![vec![n(1), n(3000)], vec![n(2), n(800)]])
    );
    query.with = vec!["id".into()];
    assert_eq!(
        run(&query, &[input(rows)], &registry).result,
        QueryResult::Relation(vec![vec![n(1), n(2500)], vec![n(2), n(800)]])
    );
}

#[test]
fn sources_are_explicit_borrowed_and_revalidated_per_prepared_execution() {
    let db = Database::new(Schema::new()).unwrap().database_value();
    let source_rows = vec![vec![Value::Long(7)]];
    let mut query = relation(
        FindSpec::Scalar(custom(
            "app/sources",
            vec![
                var("x"),
                AggregateArg::Source("$db".into()),
                AggregateArg::Source("$scale".into()),
            ],
        )),
        &["x"],
    );
    let mut registry = QueryExtensions::new();
    let expected_basis = db.basis_t();
    registry.register_aggregate("app/sources", move |group, _| {
        let Some(AggregateSource::Database(database)) = group.source(1) else {
            panic!("database source")
        };
        assert_eq!(database.basis_t(), expected_basis);
        let Some(AggregateSource::Tuples(rows)) = group.source(2) else {
            panic!("tuple source")
        };
        assert_eq!(rows, &[vec![Value::Long(7)]]);
        assert!(group.value(0, 1).is_none());
        assert!(group.column(1).is_none());
        Ok(n(long(group.value(0, 0).unwrap()) * 7))
    });
    let prepared = PreparedQuery::new(&query).unwrap();
    let sources = [
        QueryDataSource::database("$db", db),
        QueryDataSource::tuples("$scale", source_rows),
    ];
    assert_eq!(
        prepared
            .execute_with_extensions(
                &sources,
                &[input(vec![vec![3]])],
                &QueryControl::default(),
                &registry
            )
            .unwrap()
            .result,
        QueryResult::Scalar(Some(n(21)))
    );
    assert_eq!(
        prepared
            .execute_with_extensions(
                &sources[..1],
                &[input(vec![])],
                &QueryControl::default(),
                &registry
            )
            .unwrap_err()
            .code,
        "query/unknown-source"
    );
    assert_eq!(
        prepared
            .execute(&sources, &[input(vec![])], &QueryControl::default())
            .unwrap_err()
            .code,
        "query/unknown-aggregate"
    );
    query.find = FindSpec::Scalar(custom(
        "app/weighted",
        vec![var("x"), var("x"), AggregateArg::Constant(n(1))],
    ));
    assert_eq!(
        run(&query, &[input(vec![vec![3]])], &weighted_registry()).result,
        QueryResult::Scalar(Some(n(3)))
    );
}

#[test]
fn empty_global_group_invokes_once_but_empty_grouped_query_does_not() {
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&calls);
    let mut registry = QueryExtensions::new();
    registry.register_aggregate("app/empty", move |group, _| {
        observed.fetch_add(1, Ordering::Relaxed);
        assert!(group.is_empty());
        assert_eq!(group.constant(1), Some(&QueryValue::Nil));
        assert_eq!(group.column(0).unwrap().len(), 0);
        Ok(n(42))
    });
    let aggregate = custom(
        "app/empty",
        vec![var("x"), AggregateArg::Constant(QueryValue::Nil)],
    );
    let mut query = relation(FindSpec::Scalar(aggregate.clone()), &["x", "group"]);
    assert_eq!(
        run(&query, &[input(vec![])], &registry).result,
        QueryResult::Scalar(Some(n(42)))
    );
    query.find = FindSpec::Relation(vec![FindElement::Variable("group".into()), aggregate]);
    assert_eq!(
        run(&query, &[input(vec![])], &registry).result,
        QueryResult::Relation(vec![])
    );
    assert_eq!(calls.load(Ordering::Relaxed), 1);
}

#[test]
fn exact_general_cells_and_constants_remain_borrowed_without_narrowing() {
    let exact = QueryValue::Map(vec![(
        QueryValue::Char('λ'),
        QueryValue::Tagged(
            Symbol::new("app", "exact"),
            Box::new(QueryValue::Set(vec![QueryValue::Nil, n(4)])),
        ),
    )]);
    let constant = QueryValue::Scalar(Value::BigInt(BigInt::from(1_u64) << 512_usize));
    let mut registry = QueryExtensions::new();
    let expected = exact.clone();
    let expected_constant = constant.clone();
    registry.register_aggregate("app/exact", move |group, _| {
        assert_eq!(group.args().len(), 2);
        assert_eq!(group.constant(1), Some(&expected_constant));
        let AggregateValue::Query(value) = group.value(0, 0).unwrap() else {
            panic!("general map")
        };
        let AggregateValue::Query(column_value) = group.column(0).unwrap().next().unwrap() else {
            panic!("general map column")
        };
        assert!(std::ptr::eq(value, column_value));
        assert_eq!(value, &expected);
        group.check(1)?;
        Ok(value.clone())
    });
    let mut query = Query::new(
        FindSpec::Scalar(custom(
            "app/exact",
            vec![var("x"), AggregateArg::Constant(constant)],
        )),
        vec![],
    );
    query.inputs = vec![InputSpec::Scalar("x".into())];
    assert_eq!(
        run(&query, &[QueryInput::General(exact.clone())], &registry).result,
        QueryResult::Scalar(Some(exact))
    );
}

#[test]
fn shared_controls_errors_panics_and_result_admission_leave_prepared_plan_reusable() {
    let query = relation(
        FindSpec::Scalar(custom("app/check", vec![var("x")])),
        &["x"],
    );
    let prepared = PreparedQuery::new(&query).unwrap();
    let mut registry = QueryExtensions::new();
    registry.register_aggregate("app/check", |group, _| {
        group.check(500)?;
        Ok(n(1))
    });
    let good = prepared
        .execute_with_extensions(
            &[],
            &[input(vec![vec![1]])],
            &QueryControl::default(),
            &registry,
        )
        .unwrap();
    let low = QueryControl {
        max_work: good.stats.work as usize - 1,
        ..Default::default()
    };
    assert_eq!(
        prepared
            .execute_with_extensions(&[], &[input(vec![vec![1]])], &low, &registry)
            .unwrap_err()
            .code,
        "query/work-limit"
    );
    let cancel = Arc::new(AtomicBool::new(false));
    let flag = Arc::clone(&cancel);
    registry.register_aggregate("app/check", move |group, _| {
        flag.store(true, Ordering::Relaxed);
        group.check(1)?;
        Ok(n(1))
    });
    let control = QueryControl {
        cancel: Arc::clone(&cancel),
        ..Default::default()
    };
    assert_eq!(
        prepared
            .execute_with_extensions(&[], &[input(vec![vec![1]])], &control, &registry)
            .unwrap_err()
            .code,
        "query/canceled"
    );
    cancel.store(false, Ordering::Relaxed);
    registry.register_aggregate("app/check", |_, _| panic!("contained aggregate panic"));
    assert_eq!(
        prepared
            .execute_with_extensions(&[], &[input(vec![])], &control, &registry)
            .unwrap_err()
            .code,
        "query/local-aggregate-panicked"
    );
    registry.register_aggregate("app/check", |_, _| {
        Err(SemanticError::incorrect(
            "app/aggregate-rejected",
            "intentional",
        ))
    });
    assert_eq!(
        prepared
            .execute_with_extensions(&[], &[input(vec![])], &control, &registry)
            .unwrap_err()
            .code,
        "app/aggregate-rejected"
    );
    registry.register_aggregate("app/check", |_, _| {
        Ok(QueryValue::Scalar(Value::String("x".repeat(8192))))
    });
    assert_eq!(
        prepared
            .execute_with_extensions(
                &[],
                &[input(vec![])],
                &QueryControl {
                    max_value_bytes: 4096,
                    ..Default::default()
                },
                &registry
            )
            .unwrap_err()
            .code,
        "query/value-byte-limit"
    );
    registry.register_aggregate("app/check", |_, _| Ok(n(17)));
    assert_eq!(
        prepared
            .execute_with_extensions(&[], &[input(vec![])], &control, &registry)
            .unwrap()
            .result,
        QueryResult::Scalar(Some(n(17)))
    );
}

#[test]
fn nested_rule_and_sequence_execution_use_the_same_aggregate_registry() {
    let registry = weighted_registry();
    let inner = relation(
        FindSpec::Scalar(custom(
            "app/weighted",
            vec![var("x"), var("w"), AggregateArg::Constant(n(1))],
        )),
        &["x", "w"],
    );
    let mut outer = Query::new(
        FindSpec::Scalar(FindElement::Variable("answer".into())),
        vec![Clause::Function {
            function: Function::Query(Box::new(inner.clone())),
            source: "$unused".into(),
            args: vec![Term::var("rows")],
            binding: Binding::Scalar("answer".into()),
        }],
    );
    outer.inputs = vec![InputSpec::Scalar("rows".into())];
    let rows = QueryValue::Collection(vec![
        QueryValue::Tuple(vec![n(10), n(1)]),
        QueryValue::Tuple(vec![n(40), n(2)]),
    ]);
    assert_eq!(
        run(&outer, &[QueryInput::General(rows)], &registry).result,
        QueryResult::Scalar(Some(n(30)))
    );
    let mut sequence = QueryEngine::sequence_sources_with_extensions(
        &inner,
        &[],
        &[input(vec![vec![10, 1], vec![40, 2]])],
        &QueryControl::default(),
        Some(&registry),
    )
    .unwrap();
    assert_eq!(sequence.next().unwrap().unwrap(), vec![n(30)]);
    assert!(sequence.next().is_none());
    let mut pattern = RelationPattern::new(vec![Term::var("x"), Term::var("w")]);
    pattern.source = "$prices".into();
    let mut rule_query = Query::new(
        inner.find.clone(),
        vec![Clause::Rule {
            name: "prices".into(),
            source: "$prices".into(),
            args: vec![Term::var("x"), Term::var("w")],
        }],
    );
    rule_query.rules = vec![Rule {
        name: "prices".into(),
        head: vec!["x".into(), "w".into()],
        required: Default::default(),
        clauses: vec![Clause::RelationPattern(Box::new(pattern))],
    }];
    assert_eq!(
        QueryEngine::execute_sources_with_extensions(
            &rule_query,
            &[QueryDataSource::tuples(
                "$prices",
                vec![
                    vec![Value::Long(10), Value::Long(1)],
                    vec![Value::Long(40), Value::Long(2)]
                ]
            )],
            &[],
            &QueryControl::default(),
            Some(&registry)
        )
        .unwrap()
        .result,
        QueryResult::Scalar(Some(n(30)))
    );
}

#[test]
fn aggregate_complete_path_cost_scales_with_rows_and_preserves_alignment() {
    let mut query = relation(
        FindSpec::Scalar(custom("app/sum-products", vec![var("x"), var("w")])),
        &["id", "x", "w"],
    );
    query.with = vec!["id".into()];
    let visits = Arc::new(AtomicUsize::new(0));
    let visited = Arc::clone(&visits);
    let mut registry = QueryExtensions::new();
    registry.register_aggregate("app/sum-products", move |group, _| {
        let mut result = 0;
        for row in 0..group.len() {
            group.check(1)?;
            visited.fetch_add(1, Ordering::Relaxed);
            result += long(group.value(row, 0).unwrap()) * long(group.value(row, 1).unwrap());
        }
        Ok(n(result))
    });
    let mut previous = None;
    for width in [32_i64, 128, 1024, 4096] {
        visits.store(0, Ordering::Relaxed);
        let input = input((0..width).map(|id| vec![id, id + 1, id % 7 + 1]).collect());
        let expected: i64 = (0..width).map(|id| (id + 1) * (id % 7 + 1)).sum();
        let started = Instant::now();
        let prepared = PreparedQuery::new(&query).unwrap();
        let outcome = prepared
            .execute_with_extensions(&[], &[input], &QueryControl::default(), &registry)
            .unwrap();
        assert_eq!(outcome.result, QueryResult::Scalar(Some(n(expected))));
        assert_eq!(visits.load(Ordering::Relaxed), width as usize);
        let work = outcome.stats.work;
        let bytes = outcome.stats.allocated_value_bytes;
        if let Some((old_width, old_work)) = previous {
            assert!(work <= old_work * (width / old_width) as u64 + 32);
        }
        previous = Some((width, work));
        drop(outcome);
        drop(prepared);
        eprintln!(
            "custom aggregate rows={width} complete prepare/query/check/drop={:?} callback_visits={width} work={work} accounted_bytes={bytes}",
            started.elapsed()
        );
    }
}

#[test]
fn custom_and_builtin_aggregates_share_general_group_keys_and_the_same_bag() {
    let key = QueryValue::Set(vec![n(1), n(2)]);
    let mut query = relation(
        FindSpec::Relation(vec![
            FindElement::Variable("key".into()),
            custom(
                "app/weighted",
                vec![var("x"), var("w"), AggregateArg::Constant(n(1))],
            ),
            FindElement::Aggregate {
                function: Aggregate::Count,
                variable: "x".into(),
            },
        ]),
        &["id", "key", "x", "w"],
    );
    query.with = vec!["id".into()];
    let input = QueryInput::General(QueryValue::Collection(vec![
        QueryValue::Tuple(vec![n(0), key.clone(), n(10), n(1)]),
        QueryValue::Tuple(vec![
            n(1),
            QueryValue::Set(vec![n(2), n(1), n(2)]),
            n(10),
            n(1),
        ]),
        QueryValue::Tuple(vec![n(2), key.clone(), n(40), n(2)]),
    ]));
    assert_eq!(
        run(&query, &[input], &weighted_registry()).result,
        QueryResult::Relation(vec![vec![key, n(25), n(3)]])
    );
}

#[test]
fn constant_validation_and_nested_registration_happen_before_empty_execution() {
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&calls);
    let mut registry = QueryExtensions::new();
    registry.register_aggregate("app/check", move |_, _| {
        observed.fetch_add(1, Ordering::Relaxed);
        Ok(QueryValue::Nil)
    });
    let bad = QueryValue::Map(vec![(n(1), n(2)), (n(1), n(3))]);
    let query = relation(
        FindSpec::Scalar(custom(
            "app/check",
            vec![var("x"), AggregateArg::Constant(bad)],
        )),
        &["x"],
    );
    let error = QueryEngine::execute_sources_with_extensions(
        &query,
        &[],
        &[input(vec![])],
        &QueryControl::default(),
        Some(&registry),
    )
    .unwrap_err();
    assert_eq!(error.code, "query/duplicate-map-key");
    assert_eq!(calls.load(Ordering::Relaxed), 0);

    let inner = relation(
        FindSpec::Scalar(custom("app/missing", vec![var("x")])),
        &["x"],
    );
    let mut outer = Query::new(
        FindSpec::Collection(FindElement::Variable("answer".into())),
        vec![Clause::Function {
            function: Function::Query(Box::new(inner)),
            source: "$not-consumed".into(),
            args: vec![Term::var("rows")],
            binding: Binding::Scalar("answer".into()),
        }],
    );
    outer.inputs = vec![InputSpec::Collection("rows".into())];
    assert_eq!(
        QueryEngine::execute_sources_with_extensions(
            &outer,
            &[],
            &[QueryInput::Collection(vec![])],
            &QueryControl::default(),
            Some(&registry)
        )
        .unwrap_err()
        .code,
        "query/unknown-aggregate"
    );
}
