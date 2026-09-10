use atomic_core::*;
use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use std::str::FromStr;
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::{Duration, Instant};

const NUMBER: u32 = 1_000;

fn fixture(values: &[Value], value_type: ValueType, indexed: bool) -> Database {
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        NUMBER,
        Keyword::new("range", "number"),
        value_type,
        Cardinality::Many,
    );
    attribute.indexed = indexed;
    schema.install(attribute).unwrap();
    let ops: Vec<_> = values
        .iter()
        .enumerate()
        .map(|(n, value)| TxOp::Add {
            entity: EntityRef::Temp(format!("row-{n}")),
            attribute: NUMBER,
            value: value.clone().into(),
        })
        .collect();
    Database::new(schema)
        .unwrap()
        .with(&ops, 1_000)
        .unwrap()
        .db_after
}

fn pattern() -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(
        Term::var("e"),
        Term::Constant(Value::Long(NUMBER.into())),
        Term::var("v"),
    )))
}

fn comparison(predicate: Predicate, left: Term, right: Term) -> Clause {
    Clause::Predicate {
        predicate,
        source: "$".into(),
        args: vec![left, right],
    }
}

fn query(predicates: Vec<Clause>) -> Query {
    Query::new(
        FindSpec::Collection(FindElement::Variable("v".into())),
        std::iter::once(pattern()).chain(predicates).collect(),
    )
}

fn run(database: &DatabaseValue, query: &Query, inputs: &[QueryInput], scan: bool) -> QueryOutcome {
    QueryEngine::execute_sources(
        query,
        &[QueryDataSource::database("$", database.clone())],
        inputs,
        &QueryControl {
            force_scan: scan,
            ..QueryControl::default()
        },
    )
    .unwrap()
}

#[test]
fn selective_indexed_ranges_do_not_enumerate_unrelated_values() {
    let query = query(vec![
        comparison(
            Predicate::GreaterOrEqual,
            Term::var("v"),
            Term::Constant(Value::Long(5)),
        ),
        comparison(
            Predicate::Less,
            Term::var("v"),
            Term::Constant(Value::BigDec(BigDecimal::from(9))),
        ),
    ]);
    for count in [128, 512, 2_048] {
        let setup = Instant::now();
        let database = fixture(
            &(0..count).map(Value::Long).collect::<Vec<_>>(),
            ValueType::Long,
            true,
        )
        .database_value();
        let setup_elapsed = setup.elapsed();
        let actual = run(&database, &query, &[], false);
        let scan = run(&database, &query, &[], true);
        assert_eq!(
            actual.result,
            QueryResult::Collection((5..9).map(|n| QueryValue::Scalar(Value::Long(n))).collect())
        );
        assert_eq!(actual.result, scan.result);
        // Complete warm-query calls include source/control construction,
        // execution, independent result checking and result/plan destruction.
        // Fixture construction, the initial preparation and forced-scan oracle
        // above are deliberately outside this timer. Timings are diagnostic;
        // the actual consumption counters below are the scaling assertions.
        const REPEATS: u32 = 16;
        let mut complete_elapsed = Duration::ZERO;
        for _ in 0..REPEATS {
            let started = Instant::now();
            let measured = run(&database, &query, &[], false);
            assert_eq!(measured.result, actual.result);
            drop(measured);
            complete_elapsed += started.elapsed();
        }
        eprintln!(
            "query range count={count} indexed={:?} forced={:?} plan={:?} complete_warm_query_check_drop_repeats={REPEATS} complete_elapsed={complete_elapsed:?} setup={setup_elapsed:?}",
            actual.stats, scan.stats, actual.plan,
        );
        assert!(
            actual.stats.datoms_examined <= 8,
            "selective range scanned {} datoms",
            actual.stats.datoms_examined
        );
    }
}

fn expected(values: impl IntoIterator<Item = Value>) -> QueryResult {
    let mut values: Vec<_> = values.into_iter().collect();
    values.sort_by(Value::index_cmp);
    values.dedup_by(|left, right| left.index_cmp(right).is_eq());
    QueryResult::Collection(values.into_iter().map(QueryValue::Scalar).collect())
}

fn agrees(
    database: &DatabaseValue,
    query: &Query,
    inputs: &[QueryInput],
    expected: QueryResult,
) -> QueryOutcome {
    let mut indexed = run(database, query, inputs, false);
    // Collection results have set membership, not a promised index order.
    indexed.result = canonical(indexed.result);
    assert_eq!(indexed.result, expected);
    assert_eq!(
        indexed.result,
        canonical(run(database, query, inputs, true).result)
    );
    indexed
}

fn canonical(mut result: QueryResult) -> QueryResult {
    if let QueryResult::Collection(values) = &mut result {
        values.sort_by(QueryValue::canonical_cmp);
    }
    result
}

#[test]
fn all_six_comparisons_preserve_mixed_numeric_boundaries_and_operand_order() {
    let integers: Vec<_> = (-3..=3).map(Value::Long).collect();
    let decimals: Vec<_> = ["-1", "0", "1.0", "1.00", "1.1", "2"]
        .map(|text| Value::BigDec(BigDecimal::from_str(text).unwrap()))
        .into();
    let floats = vec![
        Value::Double(f64::NEG_INFINITY),
        Value::Double(-1.0),
        Value::Double(-0.0),
        Value::Double(0.0),
        Value::Double(0.1),
        Value::Double(1.0),
        Value::Double(f64::INFINITY),
        Value::Double(f64::NAN),
    ];
    for (values, value_type) in [
        (integers, ValueType::Long),
        (decimals, ValueType::BigDec),
        (floats, ValueType::Double),
    ] {
        let database = fixture(&values, value_type, true).database_value();
        for endpoint in [
            Value::Long(0),
            Value::Ref(1),
            Value::Double(0.1),
            Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
            Value::BigInt(BigInt::from(2)),
            Value::Double(f64::NAN),
        ] {
            for predicate in [
                Predicate::Eq,
                Predicate::NotEq,
                Predicate::Less,
                Predicate::LessOrEqual,
                Predicate::Greater,
                Predicate::GreaterOrEqual,
            ] {
                for reversed in [false, true] {
                    let want = expected(
                        values
                            .iter()
                            .filter(|value| {
                                let cmp = if reversed {
                                    endpoint.index_cmp(value)
                                } else {
                                    value.index_cmp(&endpoint)
                                };
                                match predicate {
                                    Predicate::Eq => cmp.is_eq(),
                                    Predicate::NotEq => cmp.is_ne(),
                                    Predicate::Less => cmp.is_lt(),
                                    Predicate::LessOrEqual => !cmp.is_gt(),
                                    Predicate::Greater => cmp.is_gt(),
                                    Predicate::GreaterOrEqual => !cmp.is_lt(),
                                    _ => unreachable!(),
                                }
                            })
                            .cloned(),
                    );
                    let (left, right) = if reversed {
                        (Term::Constant(endpoint.clone()), Term::var("v"))
                    } else {
                        (Term::var("v"), Term::Constant(endpoint.clone()))
                    };
                    let query = query(vec![comparison(predicate, left, right)]);
                    agrees(&database, &query, &[], want);
                }
            }
        }
    }
}

#[test]
fn strict_and_not_equal_bounds_skip_growing_equal_value_fanout() {
    for count in [128, 512, 2_048] {
        let mut data = vec![Value::Long(5); count];
        data.extend((6..9).map(Value::Long));
        let database = fixture(&data, ValueType::Long, true).database_value();
        for predicate in [Predicate::Greater, Predicate::NotEq] {
            let query = query(vec![comparison(
                predicate,
                Term::var("v"),
                Term::Constant(Value::BigDec(BigDecimal::from_str("5.00").unwrap())),
            )]);
            let actual = agrees(&database, &query, &[], expected((6..9).map(Value::Long)));
            eprintln!(
                "equal fanout={count} predicate={predicate:?} stats={:?}",
                actual.stats
            );
            assert!(
                actual.stats.datoms_examined <= 5,
                "equal boundary fanout was scanned"
            );
        }
    }
}

#[test]
fn conjunctions_subtract_multiple_exclusions_and_preserve_empty_intervals() {
    let database = fixture(
        &(0..=10).map(Value::Long).collect::<Vec<_>>(),
        ValueType::Long,
        true,
    )
    .database_value();
    let mut clauses = vec![
        comparison(
            Predicate::GreaterOrEqual,
            Term::var("v"),
            Term::Constant(Value::Long(2)),
        ),
        comparison(
            Predicate::LessOrEqual,
            Term::var("v"),
            Term::Constant(Value::Long(8)),
        ),
        comparison(
            Predicate::NotEq,
            Term::var("v"),
            Term::Constant(Value::Long(2)),
        ),
        comparison(
            Predicate::NotEq,
            Term::var("v"),
            Term::Constant(Value::Long(5)),
        ),
        comparison(
            Predicate::NotEq,
            Term::var("v"),
            Term::Constant(Value::Double(5.0)),
        ),
        comparison(
            Predicate::NotEq,
            Term::var("v"),
            Term::Constant(Value::Long(8)),
        ),
        comparison(
            Predicate::NotEq,
            Term::var("v"),
            Term::Constant(Value::Long(99)),
        ),
    ];
    for _ in 0..clauses.len() {
        agrees(
            &database,
            &query(clauses.clone()),
            &[],
            expected([3, 4, 6, 7].map(Value::Long)),
        );
        clauses.rotate_left(1);
    }
    for pair in [
        (Predicate::Greater, Predicate::LessOrEqual),
        (Predicate::GreaterOrEqual, Predicate::Less),
        (Predicate::Eq, Predicate::NotEq),
    ] {
        let query = query(vec![
            comparison(pair.0, Term::var("v"), Term::Constant(Value::Long(5))),
            comparison(pair.1, Term::var("v"), Term::Constant(Value::Long(5))),
        ]);
        let actual = agrees(&database, &query, &[], expected([]));
        assert_eq!(actual.stats.datoms_examined, 0);
        assert_eq!(actual.stats.index_seeks, 0);
    }
}

#[test]
fn resolved_input_rows_have_distinct_range_probe_keys_and_binding_orders() {
    let database = fixture(
        &(0..=20).map(Value::Long).collect::<Vec<_>>(),
        ValueType::Long,
        true,
    )
    .database_value();
    let mut query = query(vec![
        comparison(Predicate::GreaterOrEqual, Term::var("v"), Term::var("low")),
        comparison(Predicate::Less, Term::var("v"), Term::var("high")),
    ]);
    query.inputs = vec![InputSpec::Relation(vec![
        Some("low".into()),
        Some("high".into()),
    ])];
    let inputs = [QueryInput::Relation(vec![
        vec![Value::Long(2), Value::Long(4)],
        vec![Value::Long(10), Value::Long(12)],
        vec![Value::Long(2), Value::Long(4)],
    ])];
    for _ in 0..query.clauses.len() {
        let actual = agrees(
            &database,
            &query,
            &inputs,
            expected([2, 3, 10, 11].map(Value::Long)),
        );
        assert!(actual.stats.datoms_examined <= 6);
        query.clauses.rotate_left(1);
    }
    query.inputs.clear();
    query.clauses.extend([
        Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![Term::Constant(Value::Long(2))],
            binding: Binding::Scalar("low".into()),
        },
        Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![Term::Constant(Value::Long(4))],
            binding: Binding::Scalar("high".into()),
        },
    ]);
    agrees(&database, &query, &[], expected([2, 3].map(Value::Long)));
}

#[test]
fn supplied_views_filter_before_collapse_but_stop_before_the_unrelated_suffix() {
    let database = fixture(
        &(0..128).map(Value::Long).collect::<Vec<_>>(),
        ValueType::Long,
        true,
    );
    let before = database.database_value();
    let entity = before
        .datoms_with_prefix(&IndexPrefix::Avet {
            attribute: NUMBER,
            value: Some(Value::Long(5)),
            entity: None,
        })
        .unwrap()[0]
        .entity;
    let report = database
        .with(
            &[
                TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: NUMBER,
                    value: Some(Value::Long(5).into()),
                },
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: NUMBER,
                    value: Value::Long(7).into(),
                },
            ],
            2_000,
        )
        .unwrap();
    let after = report.db_after.database_value();
    let query = query(vec![
        comparison(
            Predicate::GreaterOrEqual,
            Term::var("v"),
            Term::Constant(Value::Long(5)),
        ),
        comparison(
            Predicate::Less,
            Term::var("v"),
            Term::Constant(Value::Long(9)),
        ),
    ]);
    for (view, want) in [
        (before.clone(), vec![5, 6, 7, 8]),
        (after.clone(), vec![6, 7, 8]),
        (after.clone().as_of(before.basis_t()), vec![5, 6, 7, 8]),
        (after.clone().since(before.basis_t()), vec![7]),
        (after.clone().history(), vec![5, 6, 7, 8]),
        // Filtering retractions before collapse deliberately reveals old5.
        (
            after.clone().filter(|_, datom| datom.added),
            vec![5, 6, 7, 8],
        ),
    ] {
        agrees(
            &view,
            &query,
            &[],
            expected(want.into_iter().map(Value::Long)),
        );
    }
    let inspected = Arc::new(AtomicUsize::new(0));
    let visits = Arc::clone(&inspected);
    let hidden = after.filter(move |_, _| {
        visits.fetch_add(1, Ordering::Relaxed);
        false
    });
    let actual = run(&hidden, &query, &[], false);
    assert_eq!(actual.result, expected([]));
    assert!(actual.stats.datoms_examined <= 7);
    assert!(inspected.load(Ordering::Relaxed) <= 6);
}

#[test]
fn unindexed_raw_tuple_and_nonstored_endpoint_fallbacks_keep_query_semantics() {
    let database = fixture(
        &(0..10).map(Value::Long).collect::<Vec<_>>(),
        ValueType::Long,
        false,
    )
    .database_value();
    let query = query(vec![comparison(
        Predicate::Greater,
        Term::var("v"),
        Term::Constant(Value::Long(7)),
    )]);
    let result = agrees(&database, &query, &[], expected([8, 9].map(Value::Long)));
    assert!(!result.plan.iter().any(|step| step.access == "AVET range"));
    let raw = QueryEngine::execute_sources(
        &query,
        &[QueryDataSource::tuples(
            "$",
            (0..10)
                .map(|n| vec![Value::Long(n), Value::Long(NUMBER.into()), Value::Long(n)])
                .collect(),
        )],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    assert_eq!(raw.result, expected([8, 9].map(Value::Long)));
    let nil = super_query(Predicate::Greater, Term::Nil);
    agrees(&database, &nil, &[], expected((0..10).map(Value::Long)));
}

fn super_query(predicate: Predicate, endpoint: Term) -> Query {
    query(vec![comparison(predicate, Term::var("v"), endpoint)])
}

#[test]
fn malformed_range_arity_is_not_hidden_by_a_contradictory_hint() {
    let database = fixture(&[Value::Long(1)], ValueType::Long, true).database_value();
    let mut query = super_query(Predicate::Less, Term::Constant(Value::Long(0)));
    query.clauses.push(Clause::Predicate {
        predicate: Predicate::Greater,
        source: "$".into(),
        args: vec![Term::var("v")],
    });
    for force_scan in [false, true] {
        let error = QueryEngine::execute_sources(
            &query,
            &[QueryDataSource::database("$", database.clone())],
            &[],
            &QueryControl {
                force_scan,
                ..QueryControl::default()
            },
        )
        .unwrap_err();
        assert_eq!(error.code, "query/predicate-arity");
    }
}

#[test]
fn local_conjunction_hints_do_not_escape_or_not_or_named_rule_scopes() {
    let database = fixture(
        &(0..10).map(Value::Long).collect::<Vec<_>>(),
        ValueType::Long,
        true,
    )
    .database_value();
    let mut q = query(vec![Clause::Not {
        join: Some(vec!["v".into()]),
        clauses: vec![
            comparison(
                Predicate::GreaterOrEqual,
                Term::var("v"),
                Term::Constant(Value::Long(3)),
            ),
            comparison(
                Predicate::Less,
                Term::var("v"),
                Term::Constant(Value::Long(7)),
            ),
        ],
    }]);
    agrees(
        &database,
        &q,
        &[],
        expected([0, 1, 2, 7, 8, 9].map(Value::Long)),
    );
    q.clauses = vec![Clause::Or {
        join: None,
        branches: vec![
            vec![
                pattern(),
                comparison(
                    Predicate::Less,
                    Term::var("v"),
                    Term::Constant(Value::Long(3)),
                ),
            ],
            vec![
                pattern(),
                comparison(
                    Predicate::Greater,
                    Term::var("v"),
                    Term::Constant(Value::Long(7)),
                ),
            ],
        ],
    }];
    agrees(
        &database,
        &q,
        &[],
        expected([0, 1, 2, 8, 9].map(Value::Long)),
    );
    q.clauses = vec![Clause::Rule {
        source: "$left".into(),
        name: "picked".into(),
        args: vec![Term::var("e"), Term::var("v")],
    }];
    q.rules = vec![Rule {
        name: "picked".into(),
        head: vec!["e".into(), "v".into()],
        required: Default::default(),
        clauses: vec![
            pattern(),
            comparison(
                Predicate::GreaterOrEqual,
                Term::var("v"),
                Term::Constant(Value::Long(2)),
            ),
            comparison(
                Predicate::Less,
                Term::var("v"),
                Term::Constant(Value::Long(5)),
            ),
        ],
    }];
    let other = fixture(
        &(100..110).map(Value::Long).collect::<Vec<_>>(),
        ValueType::Long,
        true,
    )
    .database_value();
    for force_scan in [false, true] {
        let outcome = QueryEngine::execute_sources(
            &q,
            &[
                QueryDataSource::database("$left", database.clone()),
                QueryDataSource::database("$right", other.clone()),
            ],
            &[],
            &QueryControl {
                force_scan,
                ..QueryControl::default()
            },
        )
        .unwrap();
        assert_eq!(
            canonical(outcome.result),
            expected([2, 3, 4].map(Value::Long))
        );
    }
}

#[test]
fn compact_extreme_decimal_boundaries_remain_exact_without_exponent_expansion() {
    for scale in [i64::MIN, i64::MAX - 1] {
        let values: Vec<_> = (1..=4)
            .map(|n| Value::BigDec(BigDecimal::new(BigInt::from(n), scale)))
            .collect();
        let database = fixture(&values, ValueType::BigDec, true).database_value();
        let equal_two = Value::BigDec(BigDecimal::new(BigInt::from(20), scale + 1));
        let q = query(vec![
            comparison(
                Predicate::Greater,
                Term::var("v"),
                Term::Constant(equal_two),
            ),
            comparison(
                Predicate::LessOrEqual,
                Term::var("v"),
                Term::Constant(values[2].clone()),
            ),
        ]);
        let actual = agrees(&database, &q, &[], expected([values[2].clone()]));
        assert!(actual.stats.datoms_examined <= 2);
    }
}

#[test]
fn query_range_raw_filter_loops_obey_work_and_cancellation_admission() {
    use std::sync::atomic::AtomicBool;
    let database = fixture(
        &(0..512).map(Value::Long).collect::<Vec<_>>(),
        ValueType::Long,
        true,
    )
    .database_value();
    let q = super_query(Predicate::GreaterOrEqual, Term::Constant(Value::Long(0)));
    let hidden = database.clone().filter(|_, _| false);
    let error = QueryEngine::execute_sources(
        &q,
        &[QueryDataSource::database("$", hidden)],
        &[],
        &QueryControl {
            max_work: 100,
            ..QueryControl::default()
        },
    )
    .unwrap_err();
    assert_eq!(error.code, "query/work-limit");
    let cancel = Arc::new(AtomicBool::new(false));
    let signal = Arc::clone(&cancel);
    let visits = Arc::new(AtomicUsize::new(0));
    let counter = Arc::clone(&visits);
    let hidden = database.filter(move |_, _| {
        if counter.fetch_add(1, Ordering::Relaxed) == 2 {
            signal.store(true, Ordering::Relaxed);
        }
        false
    });
    let error = QueryEngine::execute_sources(
        &q,
        &[QueryDataSource::database("$", hidden)],
        &[],
        &QueryControl {
            cancel,
            ..QueryControl::default()
        },
    )
    .unwrap_err();
    assert_eq!(error.code, "query/canceled");
    assert_eq!(visits.load(Ordering::Relaxed), 3);
}
