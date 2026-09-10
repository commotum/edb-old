use atomic_core::{
    Aggregate, Clause, FindElement, FindSpec, PreparedQuery, Query, QueryControl, QueryDataSource,
    QueryResult, QueryValue, RelationPattern, Symbol, Term, Value,
};
use std::cmp::Ordering;
use std::time::Instant;

fn n(value: i64) -> QueryValue {
    QueryValue::Scalar(Value::Long(value))
}
fn tag(value: QueryValue) -> QueryValue {
    QueryValue::Tagged(Symbol::new("example", "inert"), Box::new(value))
}

#[test]
fn sets_are_unordered_and_deduplicated_without_changing_old_shapes() {
    let set = QueryValue::Set(vec![n(2), n(1), QueryValue::Scalar(Value::Double(1.0))]);
    assert_eq!(set, QueryValue::Set(vec![n(1), n(2)]));
    assert_ne!(set, QueryValue::Collection(vec![n(1), n(2)]));
    assert_ne!(
        QueryValue::Collection(vec![n(1), n(2)]),
        QueryValue::Collection(vec![n(2), n(1)])
    );
    assert_ne!(
        QueryValue::Tuple(vec![n(1)]),
        QueryValue::Collection(vec![n(1)])
    );
    assert_ne!(
        QueryValue::Tuple(vec![n(1)]),
        QueryValue::Scalar(Value::Tuple(vec![Some(Value::Long(1))]))
    );
    assert_eq!(QueryValue::Char('λ'), QueryValue::Char('λ'));
    assert_ne!(
        QueryValue::Char('λ'),
        QueryValue::Scalar(Value::String("λ".into()))
    );
    assert_eq!(tag(set.clone()), tag(QueryValue::Set(vec![n(2), n(1)])));
    assert_ne!(
        tag(set),
        QueryValue::Tagged(
            Symbol::new("other", "inert"),
            Box::new(QueryValue::Set(vec![n(1), n(2)]))
        )
    );
}

#[test]
fn arbitrary_unordered_keys_and_values_have_a_total_order() {
    let a = QueryValue::Map(vec![
        (
            QueryValue::Set(vec![n(2), n(1)]),
            tag(QueryValue::Char('a')),
        ),
        (QueryValue::Nil, QueryValue::Collection(vec![n(3)])),
    ]);
    let b = QueryValue::Map(vec![
        (QueryValue::Nil, QueryValue::Collection(vec![n(3)])),
        (
            QueryValue::Set(vec![n(1), n(2), n(1)]),
            tag(QueryValue::Char('a')),
        ),
    ]);
    assert_eq!(a, b);
    let values = vec![
        QueryValue::Nil,
        n(0),
        QueryValue::Tuple(vec![]),
        QueryValue::Collection(vec![]),
        a,
        b,
        QueryValue::Set(vec![n(2)]),
        QueryValue::Set(vec![n(1), n(2)]),
        QueryValue::Char('a'),
        tag(n(0)),
    ];
    for a in &values {
        for b in &values {
            assert_eq!(a.canonical_cmp(b), b.canonical_cmp(a).reverse());
            assert_eq!(a == b, a.canonical_cmp(b) == Ordering::Equal);
            for c in &values {
                if !a.canonical_cmp(b).is_gt() && !b.canonical_cmp(c).is_gt() {
                    assert!(!a.canonical_cmp(c).is_gt());
                }
            }
        }
    }
}

#[test]
fn deep_general_values_clone_compare_debug_and_drop_on_a_small_stack() {
    std::thread::Builder::new()
        .stack_size(96 * 1024)
        .spawn(|| {
            let mut value = QueryValue::Char('x');
            for level in 0..12_000 {
                value = match level % 4 {
                    0 => tag(value),
                    1 => QueryValue::Set(vec![value]),
                    2 => QueryValue::Map(vec![(QueryValue::Nil, value)]),
                    _ => QueryValue::Tuple(vec![value]),
                };
            }
            let cloned = value.clone();
            assert_eq!(value, cloned);
            let diagnostic = format!("{value:?}");
            assert!(diagnostic.contains("Char('x')"));
            drop(diagnostic);
            drop(cloned);
            drop(value);
        })
        .unwrap()
        .join()
        .unwrap();
}

#[test]
fn complete_nested_set_operations_scale_with_input_size() {
    for width in [32, 128, 512] {
        let value = QueryValue::Set(
            (0..width)
                .rev()
                .map(|i| {
                    tag(QueryValue::Map(vec![(
                        n(i),
                        QueryValue::Tuple(vec![n(i), QueryValue::Nil]),
                    )]))
                })
                .collect(),
        );
        let expected = QueryValue::Set(
            (0..width)
                .map(|i| {
                    tag(QueryValue::Map(vec![(
                        n(i),
                        QueryValue::Tuple(vec![n(i), QueryValue::Nil]),
                    )]))
                })
                .collect(),
        );
        let started = Instant::now();
        let actual = value.clone();
        assert_eq!(actual, expected);
        drop(actual);
        eprintln!(
            "general-values width={width} clone+canonical-comparison+drop={:?}",
            started.elapsed()
        );
    }
}

fn relation_key(index: i64, reverse: bool) -> QueryValue {
    let mut members = vec![
        QueryValue::Nil,
        if reverse {
            QueryValue::Scalar(Value::Ref(index as u64))
        } else {
            n(index)
        },
    ];
    let mut entries = vec![
        (
            QueryValue::Char('k'),
            QueryValue::Set(std::mem::take(&mut members)),
        ),
        (
            tag(QueryValue::Nil),
            QueryValue::Tuple(vec![n(index), QueryValue::Char('x')]),
        ),
    ];
    if reverse {
        entries.reverse();
    }
    QueryValue::Map(entries)
}

fn relation_query() -> Query {
    Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable("left".into()),
            FindElement::Variable("right".into()),
            FindElement::Variable("key".into()),
        ]),
        vec![
            Clause::RelationPattern(Box::new(RelationPattern {
                source: "$left".into(),
                terms: vec![
                    Term::var("left"),
                    Term::var("key"),
                    Term::Blank,
                    Term::Blank,
                    Term::Blank,
                    Term::Blank,
                ],
            })),
            Clause::RelationPattern(Box::new(RelationPattern {
                source: "$right".into(),
                terms: vec![
                    Term::Blank,
                    Term::Blank,
                    Term::Blank,
                    Term::Blank,
                    Term::Blank,
                    Term::var("key"),
                    Term::var("right"),
                ],
            })),
        ],
    )
}

fn relation_sources(width: i64) -> Vec<QueryDataSource> {
    vec![
        QueryDataSource::relation(
            "$left",
            (0..width)
                .map(|i| {
                    vec![
                        n(i),
                        relation_key(i, false),
                        QueryValue::Nil,
                        QueryValue::Char('a'),
                        tag(n(i)),
                        QueryValue::Collection(vec![]),
                    ]
                })
                .collect(),
        ),
        QueryDataSource::relation(
            "$right",
            (0..width)
                .rev()
                .map(|i| {
                    vec![
                        QueryValue::Nil,
                        n(0),
                        QueryValue::Char('b'),
                        QueryValue::Nil,
                        QueryValue::Collection(vec![]),
                        relation_key(i, true),
                        n(i + 1000),
                    ]
                })
                .collect(),
        ),
    ]
}

fn verify_relation(result: &QueryResult, width: i64) {
    let QueryResult::Relation(rows) = result else {
        panic!("relation result required")
    };
    assert_eq!(rows.len(), width as usize);
    let mut seen = std::collections::BTreeSet::new();
    for row in rows {
        let QueryValue::Scalar(Value::Long(index)) = row[0] else {
            panic!("left id")
        };
        assert!((0..width).contains(&index));
        assert!(seen.insert(index));
        assert_eq!(row[1], n(index + 1000));
        assert_eq!(row[2], relation_key(index, false));
    }
}

#[test]
fn complete_general_relation_queries_match_scan_and_have_bounded_growth() {
    let mut work = Vec::new();
    for width in [32, 128, 512] {
        let setup = Instant::now();
        let sources = relation_sources(width);
        let query = relation_query();
        let setup_elapsed = setup.elapsed();
        let started = Instant::now();
        let prepared = PreparedQuery::new(&query).unwrap();
        let outcome = prepared
            .execute(
                &sources,
                &[],
                &QueryControl {
                    max_value_bytes: 256 * 1024 * 1024,
                    ..QueryControl::default()
                },
            )
            .unwrap();
        verify_relation(&outcome.result, width);
        let stats = outcome.stats.clone();
        assert!(stats.hash_join_build_rows >= width as u64);
        drop(outcome);
        drop(prepared);
        let complete_elapsed = started.elapsed();
        let reference_started = Instant::now();
        let prepared = PreparedQuery::new(&query).unwrap();
        let scan = prepared
            .execute(
                &sources,
                &[],
                &QueryControl {
                    force_scan: true,
                    // The deliberately quadratic reference pays for every
                    // repeated canonical comparison, unlike the hash path.
                    max_value_bytes: usize::try_from(8_u64 * 1024 * 1024 * 1024)
                        .unwrap_or(usize::MAX),
                    ..QueryControl::default()
                },
            )
            .unwrap();
        verify_relation(&scan.result, width);
        assert_eq!(scan.stats.hash_join_build_rows, 0);
        let scan_work = scan.stats.work;
        let scan_bytes = scan.stats.allocated_value_bytes;
        drop(scan);
        drop(prepared);
        let scan_elapsed = reference_started.elapsed();
        work.push(stats.work);
        eprintln!(
            "general-relation rows={width} caps(hash256MiB/scan8GiB cumulative-accounting) setup={setup_elapsed:?} prepare+execute+verify+result/plan-drop={complete_elapsed:?} work={} candidates={} datoms={} accounted-bytes={} peak-join-bytes={} force-scan-complete={scan_elapsed:?} scan-work={scan_work} scan-bytes={scan_bytes}",
            stats.work,
            stats.join_candidates,
            stats.datoms_examined,
            stats.allocated_value_bytes,
            stats.peak_join_bytes
        );
    }
    assert!(work[1] <= work[0] * 6, "complete work {work:?}");
    assert!(work[2] <= work[1] * 6, "complete work {work:?}");
}

#[test]
fn general_relation_limits_and_cancellation_do_not_poison_prepared_reuse() {
    let sources = relation_sources(32);
    let prepared = PreparedQuery::new(&relation_query()).unwrap();
    let canceled = QueryControl::default();
    canceled
        .cancel
        .store(true, std::sync::atomic::Ordering::Relaxed);
    assert_eq!(
        prepared.execute(&sources, &[], &canceled).unwrap_err().code,
        "query/canceled"
    );
    let error = prepared
        .execute(
            &sources,
            &[],
            &QueryControl {
                max_work: 16,
                ..QueryControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "query/work-limit");
    let error = prepared
        .execute(
            &sources,
            &[],
            &QueryControl {
                max_value_bytes: 16,
                ..QueryControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "query/value-byte-limit");
    let outcome = prepared
        .execute(&sources, &[], &QueryControl::default())
        .unwrap();
    verify_relation(&outcome.result, 32);
}

fn aggregate_fixture() -> Vec<QueryDataSource> {
    vec![QueryDataSource::relation(
        "$",
        (0..12)
            .map(|i| {
                vec![
                    n(i),
                    relation_key(i % 3, i % 2 == 0),
                    QueryValue::Set(vec![n(i % 4), n(i % 4)]),
                ]
            })
            .collect(),
    )]
}

fn aggregate_query(function: Aggregate, grouped: bool) -> Query {
    let mut query = Query::new(
        if grouped {
            FindSpec::Relation(vec![
                FindElement::Variable("group".into()),
                FindElement::Aggregate {
                    function,
                    variable: "value".into(),
                },
            ])
        } else {
            FindSpec::Scalar(FindElement::Aggregate {
                function,
                variable: "value".into(),
            })
        },
        vec![Clause::RelationPattern(Box::new(RelationPattern::new(
            vec![Term::var("id"), Term::var("group"), Term::var("value")],
        )))],
    );
    query.with.push("id".into());
    query
}

#[test]
fn general_aggregate_groups_and_sorted_results_preserve_values_and_bags() {
    let sources = aggregate_fixture();
    let run = |function, grouped| {
        PreparedQuery::new(&aggregate_query(function, grouped))
            .unwrap()
            .execute(&sources, &[], &QueryControl::default())
            .unwrap()
    };
    let grouped = run(Aggregate::Count, true);
    let QueryResult::Relation(rows) = grouped.result else {
        panic!("grouped relation")
    };
    assert_eq!(rows.len(), 3);
    for index in 0..3 {
        let row = rows
            .iter()
            .find(|row| row[0] == relation_key(index, false))
            .expect("independent group");
        assert_eq!(row[1], n(4));
    }
    let set = |value| QueryValue::Set(vec![n(value)]);
    for (function, expected) in [
        (Aggregate::Count, n(12)),
        (Aggregate::CountDistinct, n(4)),
        (
            Aggregate::Distinct,
            QueryValue::Collection((0..4).map(set).collect()),
        ),
        (Aggregate::Min, set(0)),
        (Aggregate::Max, set(3)),
        (Aggregate::MinN(3), QueryValue::Collection(vec![set(0); 3])),
        (Aggregate::MaxN(3), QueryValue::Collection(vec![set(3); 3])),
    ] {
        assert_eq!(
            run(function, false).result,
            QueryResult::Scalar(Some(expected))
        );
    }
    for (function, count, distinct) in [
        (Aggregate::Rand(9), 9, false),
        (Aggregate::Sample(3), 3, true),
    ] {
        let outcome = run(function, false);
        let QueryResult::Scalar(Some(QueryValue::Collection(ref values))) = outcome.result else {
            panic!("sample collection")
        };
        assert_eq!(values.len(), count);
        for value in values {
            assert!((0..4).any(|n| *value == set(n)));
        }
        if distinct {
            for i in 0..count {
                for j in 0..i {
                    assert_ne!(values[i], values[j]);
                }
            }
        }
    }
}

#[test]
fn general_aggregate_comparisons_are_metered_and_limits_leave_preparation_reusable() {
    let sources = aggregate_fixture();
    let count = PreparedQuery::new(&aggregate_query(Aggregate::Count, false))
        .unwrap()
        .execute(&sources, &[], &QueryControl::default())
        .unwrap();
    let prepared = PreparedQuery::new(&aggregate_query(Aggregate::CountDistinct, false)).unwrap();
    let distinct = prepared
        .execute(&sources, &[], &QueryControl::default())
        .unwrap();
    // Same input/basis and scalar output: the difference is aggregate sorting
    // and uniqueness comparison, formerly outside shared projection admission.
    assert!(distinct.stats.work > count.stats.work + 12);
    assert!(distinct.stats.allocated_value_bytes > count.stats.allocated_value_bytes);
    let limit = QueryControl {
        max_work: distinct.stats.work as usize - 1,
        ..QueryControl::default()
    };
    assert_eq!(
        prepared.execute(&sources, &[], &limit).unwrap_err().code,
        "query/work-limit"
    );
    let limit = QueryControl {
        max_value_bytes: distinct.stats.allocated_value_bytes - 1,
        ..QueryControl::default()
    };
    assert_eq!(
        prepared.execute(&sources, &[], &limit).unwrap_err().code,
        "query/value-byte-limit"
    );
    assert_eq!(
        prepared
            .execute(&sources, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(n(4)))
    );
}
