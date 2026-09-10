//! Independently expected R1/R3 regressions. See the query reference's Not and
//! Or binding sections and recovered datalog/eval-not-join's completed subquery.
use atomic_core::{
    Binding, Clause, DataPattern, FindElement, FindSpec, Function, InputSpec, Predicate,
    PreparedQuery, Query, QueryControl, QueryDataSource, QueryEngine, QueryInput, QueryOutcome,
    QueryResult, QueryValue, Rule, SemanticError, Term, Value,
};
use std::collections::BTreeSet;
use std::sync::{Arc, atomic::AtomicBool};

fn v(name: &str) -> Term {
    Term::var(name)
}
fn c(value: Value) -> Term {
    Term::Constant(value)
}
fn pattern(entity: Term, attribute: &str, value: Term) -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(
        entity,
        c(Value::String(attribute.into())),
        value,
    )))
}
fn marked(variable: &str, attribute: &str) -> Clause {
    pattern(v(variable), attribute, c(Value::Bool(true)))
}
fn call(name: &str, args: Vec<Term>) -> Clause {
    Clause::Rule {
        source: "$".into(),
        name: name.into(),
        args,
    }
}
fn not(variables: &[&str], clauses: Vec<Clause>) -> Clause {
    Clause::Not {
        join: Some(variables.iter().map(|name| (*name).into()).collect()),
        clauses,
    }
}
fn rule(name: &str, head: &[&str], required: &[usize], clauses: Vec<Clause>) -> Rule {
    Rule {
        name: name.into(),
        head: head.iter().map(|name| (*name).into()).collect(),
        required: required.iter().copied().collect(),
        clauses,
    }
}
fn query(clauses: Vec<Clause>, rules: Vec<Rule>) -> Query {
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("e".into())]),
        clauses,
    );
    query.rules = rules;
    query
}
fn source(name: &str, rows: &[(i64, &str, Value)]) -> QueryDataSource {
    QueryDataSource::tuples(
        name,
        rows.iter()
            .map(|(e, a, value)| vec![Value::Long(*e), Value::String((*a).into()), value.clone()])
            .collect(),
    )
}
fn fixture() -> Vec<QueryDataSource> {
    vec![source(
        "$",
        &[
            (1, "person", Value::Bool(true)),
            (1, "blocked", Value::Bool(true)),
            (2, "person", Value::Bool(true)),
            (3, "person", Value::Bool(true)),
            (3, "blocked", Value::Bool(true)),
        ],
    )]
}
fn execute(query: &Query, sources: &[QueryDataSource]) -> Result<QueryOutcome, SemanticError> {
    QueryEngine::execute_sources(query, sources, &[], &QueryControl::default())
}
fn assert_entities(outcome: QueryOutcome, expected: &[i64]) {
    let QueryResult::Relation(rows) = outcome.result else {
        panic!("expected relation")
    };
    let values = rows
        .into_iter()
        .map(|row| match row.as_slice() {
            [QueryValue::Scalar(Value::Long(value))] => *value,
            other => panic!("unexpected row {other:?}"),
        })
        .collect::<BTreeSet<_>>();
    assert_eq!(values, expected.iter().copied().collect());
}

#[test]
fn acyclic_negated_rule_matches_direct_set_difference_in_all_orders() {
    let sources = fixture();
    let direct = query(
        vec![
            marked("e", "person"),
            not(&["e"], vec![marked("e", "blocked")]),
        ],
        vec![],
    );
    assert_entities(execute(&direct, &sources).unwrap(), &[2]);
    for body_reversed in [false, true] {
        for rules_reversed in [false, true] {
            let mut body = vec![
                marked("e", "person"),
                not(&["e"], vec![call("blocked", vec![v("e")])]),
            ];
            if body_reversed {
                body.reverse();
            }
            let mut rules = vec![
                rule("eligible", &["e"], &[], body),
                rule("blocked", &["e"], &[0], vec![marked("e", "blocked")]),
            ];
            if rules_reversed {
                rules.reverse();
            }
            let query = query(vec![call("eligible", vec![v("e")])], rules);
            let prepared = PreparedQuery::new(&query).unwrap();
            assert_entities(
                prepared
                    .execute(&sources, &[], &QueryControl::default())
                    .unwrap(),
                &[2],
            );
        }
    }
}

fn reachability_rules() -> Vec<Rule> {
    vec![
        rule(
            "reachable",
            &["from", "to"],
            &[0],
            vec![pattern(v("from"), "edge", v("to"))],
        ),
        rule(
            "reachable",
            &["from", "to"],
            &[0],
            vec![
                pattern(v("from"), "edge", v("middle")),
                call("reachable", vec![v("middle"), v("to")]),
            ],
        ),
    ]
}

#[test]
fn negation_waits_for_recursive_positive_closure_and_keeps_local_variables_private() {
    let sources = vec![source(
        "$",
        &[
            (1, "person", Value::Bool(true)),
            (2, "person", Value::Bool(true)),
            (3, "person", Value::Bool(true)),
            (1, "edge", Value::Long(4)),
            (4, "edge", Value::Long(5)),
            (5, "edge", Value::Long(4)),
            (5, "blocked", Value::Bool(true)),
            (3, "edge", Value::Long(6)),
        ],
    )];
    let mut rules = reachability_rules();
    rules.push(rule(
        "eligible",
        &["e"],
        &[],
        vec![
            marked("e", "person"),
            not(
                &["e"],
                vec![
                    call("reachable", vec![v("e"), v("end")]),
                    marked("end", "blocked"),
                ],
            ),
        ],
    ));
    let mut query = query(vec![call("eligible", vec![v("e")])], rules);
    // Same spelling outside the not-join must not bind its private ?end.
    query.inputs.push(InputSpec::Scalar("end".into()));
    let outcome = QueryEngine::execute_sources(
        &query,
        &sources,
        &[QueryInput::Scalar(Value::Long(999))],
        &QueryControl::default(),
    )
    .unwrap();
    assert!(outcome.stats.rule_iterations > 1);
    assert_entities(outcome, &[2, 3]);
}

#[test]
fn nested_negation_uses_completed_strata_and_preserves_source_scoping() {
    let sources = vec![
        source(
            "$left",
            &[
                (1, "person", Value::Bool(true)),
                (1, "blocked", Value::Bool(true)),
                (2, "person", Value::Bool(true)),
            ],
        ),
        source(
            "$right",
            &[
                (1, "person", Value::Bool(true)),
                (2, "person", Value::Bool(true)),
                (2, "blocked", Value::Bool(true)),
            ],
        ),
    ];
    let rules = vec![
        rule("blocked", &["e"], &[0], vec![marked("e", "blocked")]),
        rule(
            "eligible",
            &["e"],
            &[],
            vec![
                marked("e", "person"),
                not(&["e"], vec![call("blocked", vec![v("e")])]),
            ],
        ),
        rule(
            "ineligible",
            &["e"],
            &[],
            vec![
                marked("e", "person"),
                not(&["e"], vec![call("eligible", vec![v("e")])]),
            ],
        ),
    ];
    for (name, expected) in [("$left", 1), ("$right", 2)] {
        let query = query(
            vec![Clause::Rule {
                source: name.into(),
                name: "ineligible".into(),
                args: vec![v("e")],
            }],
            rules.clone(),
        );
        assert_entities(execute(&query, &sources).unwrap(), &[expected]);
    }
    // Explicit source on a negative rule overrides its enclosing inherited source.
    let query = query(
        vec![Clause::Rule {
            source: "$left".into(),
            name: "cross".into(),
            args: vec![v("e")],
        }],
        vec![
            rules[0].clone(),
            rule(
                "cross",
                &["e"],
                &[],
                vec![
                    marked("e", "person"),
                    not(
                        &["e"],
                        vec![Clause::Rule {
                            source: "$right".into(),
                            name: "blocked".into(),
                            args: vec![v("e")],
                        }],
                    ),
                ],
            ),
        ],
    );
    assert_entities(execute(&query, &sources).unwrap(), &[1]);
}

fn choice() -> Clause {
    Clause::Or {
        join: None,
        branches: [1, 2]
            .into_iter()
            .map(|value| {
                vec![Clause::Predicate {
                    predicate: Predicate::Eq,
                    source: "$".into(),
                    args: vec![v("e"), c(Value::Long(value))],
                }]
            })
            .collect(),
    }
}

#[test]
fn predicate_only_or_waits_for_inputs_in_every_outer_clause_order() {
    for reverse in [false, true] {
        let mut clauses = vec![pattern(v("e"), "person", v("value")), choice()];
        if reverse {
            clauses.reverse();
        }
        assert_entities(
            execute(&query(clauses, vec![]), &fixture()).unwrap(),
            &[1, 2],
        );
    }
    let error = execute(&query(vec![choice()], vec![]), &fixture()).unwrap_err();
    assert_eq!(error.code, "query/insufficient-binding");
}

#[test]
fn or_join_can_bind_outputs_and_schedule_branch_functions_and_required_rules() {
    let choose = Clause::Or {
        join: Some(vec!["e".into()]),
        branches: vec![
            vec![
                Clause::Predicate {
                    predicate: Predicate::Eq,
                    source: "$".into(),
                    args: vec![v("copy"), c(Value::Long(2))],
                },
                Clause::Function {
                    function: Function::Add,
                    source: "$".into(),
                    args: vec![v("e"), c(Value::Long(0))],
                    binding: Binding::Scalar("copy".into()),
                },
                pattern(v("e"), "person", v("local")),
            ],
            vec![call("chosen", vec![v("e")])],
        ],
    };
    let mut query = query(
        vec![pattern(v("e"), "person", v("value")), choose],
        vec![rule(
            "chosen",
            &["e"],
            &[0],
            vec![Clause::Predicate {
                predicate: Predicate::Eq,
                source: "$".into(),
                args: vec![v("e"), c(Value::Long(1))],
            }],
        )],
    );
    query.inputs.push(InputSpec::Scalar("local".into()));
    // Branch-private ?local must not inherit this conflicting caller value.
    assert_entities(
        QueryEngine::execute_sources(
            &query,
            &fixture(),
            &[QueryInput::Scalar(Value::String("not a Boolean".into()))],
            &QueryControl::default(),
        )
        .unwrap(),
        &[1, 2],
    );

    let output_or = Clause::Or {
        join: Some(vec!["e".into()]),
        branches: vec![
            vec![marked("e", "blocked")],
            vec![Clause::Function {
                function: Function::Ground,
                source: "$".into(),
                args: vec![c(Value::Long(7))],
                binding: Binding::Scalar("e".into()),
            }],
        ],
    };
    assert_entities(
        execute(&self::query(vec![output_or], vec![]), &fixture()).unwrap(),
        &[1, 3, 7],
    );
}

#[test]
fn nested_join_scopes_do_not_leak_private_variables_into_plain_or() {
    let branches = vec![
        vec![
            pattern(v("e"), "person", v("value")),
            not(&["e"], vec![pattern(v("e"), "blocked", v("private"))]),
        ],
        vec![
            pattern(v("e"), "person", v("value")),
            Clause::Or {
                join: Some(vec!["e".into()]),
                branches: vec![vec![pattern(v("e"), "blocked", v("different-private"))]],
            },
        ],
    ];
    let query = query(
        vec![Clause::Or {
            join: None,
            branches,
        }],
        vec![],
    );
    assert_entities(execute(&query, &fixture()).unwrap(), &[1, 2, 3]);
}

#[test]
fn negative_rule_cycle_is_diagnostic_even_when_nested_in_or() {
    for mutual in [false, true] {
        let target = if mutual { "second" } else { "first" };
        let mut rules = vec![rule(
            "first",
            &["e"],
            &[],
            vec![
                marked("e", "person"),
                Clause::Or {
                    join: Some(vec!["e".into()]),
                    branches: vec![vec![not(&["e"], vec![call(target, vec![v("e")])])]],
                },
            ],
        )];
        if mutual {
            rules.push(rule(
                "second",
                &["e"],
                &[],
                vec![call("first", vec![v("e")])],
            ));
        }
        let query = query(vec![call("first", vec![v("e")])], rules);
        let error = execute(&query, &fixture()).unwrap_err();
        assert_eq!(error.code, "query/unstratified-negation");
        assert!(error.details.contains_key("rule"));
        assert!(error.details.contains_key("dependency"));
    }
}

#[test]
fn unused_negative_cycles_do_not_reject_independent_queries() {
    let query = query(
        vec![marked("e", "person")],
        vec![rule(
            "unused",
            &["e"],
            &[0],
            vec![not(&["e"], vec![call("unused", vec![v("e")])])],
        )],
    );
    assert_entities(execute(&query, &fixture()).unwrap(), &[1, 2, 3]);
}

#[test]
fn negative_subqueries_share_work_and_cancellation_with_their_parent() {
    let mut rows = Vec::new();
    for i in 1..16 {
        rows.push((i, "person", Value::Bool(true)));
        rows.push((i, "edge", Value::Long(i + 1)));
    }
    rows.push((16, "blocked", Value::Bool(true)));
    let sources = vec![source("$", &rows)];
    let mut rules = reachability_rules();
    rules.push(rule(
        "eligible",
        &["e"],
        &[],
        vec![
            marked("e", "person"),
            not(
                &["e"],
                vec![
                    call("reachable", vec![v("e"), v("end")]),
                    marked("end", "blocked"),
                ],
            ),
        ],
    ));
    let query = query(vec![call("eligible", vec![v("e")])], rules);
    let unlimited = execute(&query, &sources).unwrap();
    let work = unlimited.stats.work as usize;
    assert_entities(unlimited, &[]);
    let error = QueryEngine::execute_sources(
        &query,
        &sources,
        &[],
        &QueryControl {
            max_work: work - 1,
            ..QueryControl::default()
        },
    )
    .unwrap_err();
    assert_eq!(error.code, "query/work-limit");
    let error = QueryEngine::execute_sources(
        &query,
        &sources,
        &[],
        &QueryControl {
            cancel: Arc::new(AtomicBool::new(true)),
            ..QueryControl::default()
        },
    )
    .unwrap_err();
    assert_eq!(error.code, "query/canceled");
}

#[test]
fn nested_query_rule_scopes_are_independent_and_receive_cycle_validation() {
    let child = query(
        vec![call("same-name", vec![v("e")])],
        vec![rule("same-name", &["e"], &[], vec![marked("e", "blocked")])],
    );
    let outer = query(
        vec![call("same-name", vec![v("e")])],
        vec![rule(
            "same-name",
            &["e"],
            &[],
            vec![
                marked("e", "person"),
                not(
                    &["e"],
                    vec![Clause::Function {
                        function: Function::Query(Box::new(child)),
                        source: "$".into(),
                        args: vec![],
                        binding: Binding::Relation(vec![Some("e".into())]),
                    }],
                ),
            ],
        )],
    );
    assert_entities(execute(&outer, &fixture()).unwrap(), &[2]);
    let child = query(
        vec![call("bad", vec![v("e")])],
        vec![rule(
            "bad",
            &["e"],
            &[],
            vec![
                marked("e", "person"),
                not(&["e"], vec![call("bad", vec![v("e")])]),
            ],
        )],
    );
    let outer = query(
        vec![Clause::Function {
            function: Function::Query(Box::new(child)),
            source: "$".into(),
            args: vec![],
            binding: Binding::Relation(vec![Some("e".into())]),
        }],
        vec![],
    );
    assert_eq!(
        execute(&outer, &fixture()).unwrap_err().code,
        "query/unstratified-negation"
    );
}
