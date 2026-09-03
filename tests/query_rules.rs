//! Rule regressions mapped to `datomic.datalog/eval-query`, `eval-rule`, and
//! `eval-clause` in the recovered 1.0.7705 peer, plus the documented Required
//! Bindings and Rule Database Scoping contracts.

use atomic_core::{
    Attribute, Cardinality, Clause, DataPattern, Database, EntityRef, FindElement, FindSpec,
    InputSpec, Keyword, Query, QueryControl, QueryEngine, QueryInput, QueryResult, QuerySource,
    QueryValue, Rule, Schema, Term, TxOp, TxValue, Value, ValueType, Variable, View,
};
use std::collections::BTreeSet;

const AGE: u32 = 1_000;
const FRIEND: u32 = 1_001;

fn var(name: &str) -> Variable {
    Variable::new(name).unwrap()
}

fn v(name: &str) -> Term {
    Term::Variable(var(name))
}

fn val(value: Value) -> Term {
    Term::Constant(value)
}

fn kw(namespace: &str, name: &str) -> Term {
    val(Value::Keyword(Keyword::new(namespace, name)))
}

fn pattern(entity: Term, attribute: Term, value: Term) -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(entity, attribute, value)))
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    let mut age = Attribute::new(
        AGE,
        Keyword::new("person", "age"),
        ValueType::Long,
        Cardinality::One,
    );
    age.indexed = true;
    schema.install(age).unwrap();
    let mut friend = Attribute::new(
        FRIEND,
        Keyword::new("person", "friend"),
        ValueType::Ref,
        Cardinality::Many,
    );
    friend.indexed = true;
    schema.install(friend).unwrap();
    schema
}

fn required(indices: &[usize]) -> BTreeSet<usize> {
    indices.iter().copied().collect()
}

#[test]
fn rule_invocation_source_is_inherited_and_partitions_the_memo() {
    let first = Database::new(schema())
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("alice".into()),
                attribute: AGE,
                value: TxValue::Scalar(Value::Long(30)),
            }],
            1_000,
        )
        .unwrap();
    let alice = first.tempids["alice"];
    let old = first.db_after;
    let current = old
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(alice),
                attribute: AGE,
                value: TxValue::Scalar(Value::Long(31)),
            }],
            2_000,
        )
        .unwrap()
        .db_after;

    // The body uses `$`, which is the invocation source inside a rule. If the
    // memo were keyed only by rule name, the second call could reuse age 30.
    let age_rule = Rule {
        name: "age-of".into(),
        head: vec![var("entity"), var("age")],
        required: required(&[0]),
        clauses: vec![pattern(v("entity"), kw("person", "age"), v("age"))],
    };
    let mut query = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Variable(var("old-age")),
            FindElement::Variable(var("new-age")),
        ]),
        vec![
            Clause::Rule {
                source: "$old".into(),
                name: "age-of".into(),
                args: vec![val(Value::Ref(alice)), v("old-age")],
            },
            Clause::Rule {
                source: "$new".into(),
                name: "age-of".into(),
                args: vec![val(Value::Ref(alice)), v("new-age")],
            },
        ],
    );
    query.rules.push(age_rule);
    let outcome = QueryEngine::execute(
        &query,
        &[
            QuerySource {
                name: "$".into(),
                database: &current,
                view: View::Current,
            },
            QuerySource {
                name: "$old".into(),
                database: &old,
                view: View::Current,
            },
            QuerySource {
                name: "$new".into(),
                database: &current,
                view: View::Current,
            },
        ],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    assert_eq!(
        outcome.result,
        QueryResult::Tuple(Some(vec![
            QueryValue::Scalar(Value::Long(30)),
            QueryValue::Scalar(Value::Long(31)),
        ]))
    );
}

#[test]
fn required_bound_recursion_is_demand_driven_per_invocation() {
    const PATH_LENGTH: usize = 12;
    let mut edges = Vec::new();
    for prefix in ["wanted", "unrelated"] {
        for index in 0..PATH_LENGTH {
            edges.push(TxOp::Add {
                entity: EntityRef::Temp(format!("{prefix}-{index}")),
                attribute: FRIEND,
                value: TxValue::Entity(EntityRef::Temp(format!("{prefix}-{}", index + 1))),
            });
        }
    }
    let report = Database::new(schema())
        .unwrap()
        .with(&edges, 1_000)
        .unwrap();
    let root = report.tempids["wanted-0"];
    let unrelated_last = report.tempids[&format!("unrelated-{PATH_LENGTH}")];

    let direct = Rule {
        name: "descendant".into(),
        head: vec![var("ancestor"), var("descendant")],
        required: required(&[0]),
        clauses: vec![pattern(
            v("ancestor"),
            kw("person", "friend"),
            v("descendant"),
        )],
    };
    let transitive = Rule {
        name: "descendant".into(),
        head: vec![var("ancestor"), var("descendant")],
        required: required(&[0]),
        clauses: vec![
            pattern(v("ancestor"), kw("person", "friend"), v("middle")),
            Clause::Rule {
                source: "$".into(),
                name: "descendant".into(),
                args: vec![v("middle"), v("descendant")],
            },
        ],
    };
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("descendant"))]),
        vec![Clause::Rule {
            source: "$".into(),
            name: "descendant".into(),
            args: vec![v("root"), v("descendant")],
        }],
    );
    query.inputs.push(InputSpec::Scalar(var("root")));
    query.rules = vec![direct, transitive];
    let outcome = report
        .db_after
        .query(
            &query,
            &[QueryInput::Scalar(Value::Ref(root))],
            &QueryControl::default(),
        )
        .unwrap();
    let QueryResult::Relation(rows) = outcome.result else {
        panic!("expected relation")
    };
    assert_eq!(rows.len(), PATH_LENGTH);
    assert!(!rows.contains(&vec![QueryValue::Scalar(Value::Ref(unrelated_last))]));
    assert!(outcome.stats.rule_iterations > 1);
}

#[test]
fn mutually_recursive_required_rules_reach_the_finite_fixed_point() {
    const PATH_LENGTH: usize = 6;
    let mut edges = Vec::new();
    for index in 0..PATH_LENGTH {
        edges.push(TxOp::Add {
            entity: EntityRef::Temp(format!("node-{index}")),
            attribute: FRIEND,
            value: TxValue::Entity(EntityRef::Temp(format!("node-{}", index + 1))),
        });
    }
    let report = Database::new(schema())
        .unwrap()
        .with(&edges, 1_000)
        .unwrap();
    let root = report.tempids["node-0"];
    let expected = [2, 4, 6]
        .into_iter()
        .map(|index| report.tempids[&format!("node-{index}")])
        .collect::<Vec<_>>();

    let odd_direct = Rule {
        name: "odd-distance".into(),
        head: vec![var("from"), var("to")],
        required: required(&[0]),
        clauses: vec![pattern(v("from"), kw("person", "friend"), v("to"))],
    };
    let odd_recursive = Rule {
        name: "odd-distance".into(),
        head: vec![var("from"), var("to")],
        required: required(&[0]),
        clauses: vec![
            pattern(v("from"), kw("person", "friend"), v("next")),
            Clause::Rule {
                source: "$".into(),
                name: "even-distance".into(),
                args: vec![v("next"), v("to")],
            },
        ],
    };
    let even = Rule {
        name: "even-distance".into(),
        head: vec![var("from"), var("to")],
        required: required(&[0]),
        clauses: vec![
            pattern(v("from"), kw("person", "friend"), v("next")),
            Clause::Rule {
                source: "$".into(),
                name: "odd-distance".into(),
                args: vec![v("next"), v("to")],
            },
        ],
    };
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("to"))]),
        vec![Clause::Rule {
            source: "$".into(),
            name: "even-distance".into(),
            args: vec![val(Value::Ref(root)), v("to")],
        }],
    );
    query.rules = vec![odd_direct, odd_recursive, even];
    let outcome = report
        .db_after
        .query(&query, &[], &QueryControl::default())
        .unwrap();
    let QueryResult::Relation(rows) = outcome.result else {
        panic!("expected relation")
    };
    assert_eq!(rows.len(), expected.len());
    for entity in expected {
        assert!(rows.contains(&vec![QueryValue::Scalar(Value::Ref(entity))]));
    }
}

#[test]
fn recursive_rule_work_is_still_bounded_by_query_control() {
    let report = Database::new(schema())
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("left".into()),
                attribute: FRIEND,
                value: TxValue::Entity(EntityRef::Temp("right".into())),
            }],
            1_000,
        )
        .unwrap();
    let root = report.tempids["left"];
    let rule = Rule {
        name: "edge".into(),
        head: vec![var("from"), var("to")],
        required: required(&[0]),
        clauses: vec![pattern(v("from"), kw("person", "friend"), v("to"))],
    };
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("to"))]),
        vec![Clause::Rule {
            source: "$".into(),
            name: "edge".into(),
            args: vec![val(Value::Ref(root)), v("to")],
        }],
    );
    query.rules.push(rule);
    let error = report
        .db_after
        .query(
            &query,
            &[],
            &QueryControl {
                max_work: 1,
                ..QueryControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "query/work-limit");
}
