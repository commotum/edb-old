use atomic_core::{
    Aggregate, Attribute, Binding, Cardinality, Clause, DataPattern, Database, EntityRef,
    FindElement, FindSpec, Function, IndexOrder, Keyword, Predicate, Query, QueryControl,
    QueryEngine, QueryResult, QuerySource, QueryValue, Rule, Schema, Term, TxOp, TxValue, Unique,
    Value, ValueType, Variable, View,
};
use std::collections::BTreeSet;
use std::sync::atomic::Ordering;

fn var(name: &str) -> Variable {
    Variable::new(name).unwrap()
}

fn v(name: &str) -> Term {
    Term::Variable(var(name))
}

fn kw(namespace: &str, name: &str) -> Term {
    Term::Constant(Value::Keyword(Keyword::new(namespace, name)))
}

fn val(value: Value) -> Term {
    Term::Constant(value)
}

fn pattern(entity: Term, attribute: Term, value: Term) -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(entity, attribute, value)))
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1,
            Keyword::new("db", "txInstant"),
            ValueType::Instant,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                10,
                Keyword::new("person", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    let mut age = Attribute::new(
        11,
        Keyword::new("person", "age"),
        ValueType::Long,
        Cardinality::One,
    );
    age.indexed = true;
    schema.install(age).unwrap();
    let mut friend = Attribute::new(
        12,
        Keyword::new("person", "friend"),
        ValueType::Ref,
        Cardinality::Many,
    );
    friend.indexed = true;
    schema.install(friend).unwrap();
    schema
}

fn database() -> (Database, Database, [u64; 4]) {
    let db = Database::new(schema()).unwrap();
    let report = db
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("alice".into()),
                    attribute: 10,
                    value: TxValue::Scalar(Value::String("Alice".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("alice".into()),
                    attribute: 11,
                    value: TxValue::Scalar(Value::Long(40)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("alice".into()),
                    attribute: 12,
                    value: TxValue::Entity(EntityRef::Temp("bob".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("bob".into()),
                    attribute: 10,
                    value: TxValue::Scalar(Value::String("Bob".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("bob".into()),
                    attribute: 11,
                    value: TxValue::Scalar(Value::Long(30)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("bob".into()),
                    attribute: 12,
                    value: TxValue::Entity(EntityRef::Temp("cara".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("cara".into()),
                    attribute: 10,
                    value: TxValue::Scalar(Value::String("Cara".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("cara".into()),
                    attribute: 11,
                    value: TxValue::Scalar(Value::Long(30)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("cara".into()),
                    attribute: 12,
                    value: TxValue::Entity(EntityRef::Temp("alice".into())),
                },
            ],
            1_000,
        )
        .unwrap();
    let ids = [
        report.tempids["alice"],
        report.tempids["bob"],
        report.tempids["cara"],
        1_003,
    ];
    let db1 = report.db_after;
    let db2 = db1
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(ids[1]),
                    attribute: 11,
                    value: TxValue::Scalar(Value::Long(31)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("dave".into()),
                    attribute: 10,
                    value: TxValue::Scalar(Value::String("Dave".into())),
                },
            ],
            2_000,
        )
        .unwrap()
        .db_after;
    (db1, db2, ids)
}

fn relation_values(result: QueryResult) -> Vec<Vec<QueryValue>> {
    match result {
        QueryResult::Relation(rows) => rows,
        other => panic!("expected relation, got {other:?}"),
    }
}

#[test]
fn data_patterns_join_shape_and_use_selective_indexes() {
    let (_, db, ids) = database();
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("friend-name"))]),
        vec![
            pattern(v("friend"), kw("person", "name"), v("friend-name")),
            pattern(v("person"), kw("person", "friend"), v("friend")),
            pattern(
                v("person"),
                kw("person", "name"),
                val(Value::String("Alice".into())),
            ),
        ],
    );
    let outcome = db.query(&query, &[], &QueryControl::default()).unwrap();
    let reference = db
        .query(
            &query,
            &[],
            &QueryControl {
                force_scan: true,
                ..QueryControl::default()
            },
        )
        .unwrap();
    assert_eq!(outcome.result, reference.result);
    assert!(outcome.stats.datoms_examined < reference.stats.datoms_examined);
    assert_eq!(
        relation_values(outcome.result),
        vec![vec![QueryValue::Scalar(Value::String("Bob".into()))]]
    );
    assert!(outcome.stats.index_seeks >= 3);
    assert!(outcome.plan.iter().any(|step| step.access == "AVET seek"));

    let entity_query = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("name"))),
        vec![pattern(
            val(Value::Ref(ids[2])),
            kw("person", "name"),
            v("name"),
        )],
    );
    assert_eq!(
        db.query(&entity_query, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::String("Cara".into()))))
    );
}

#[test]
fn temporal_sources_inputs_set_and_with_bag_semantics_are_explicit() {
    let (db1, db2, _) = database();
    let age_query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("age"))]),
        vec![pattern(Term::Blank, kw("person", "age"), v("age"))],
    );
    let current = relation_values(
        db2.query(&age_query, &[], &QueryControl::default())
            .unwrap()
            .result,
    );
    assert_eq!(current.len(), 3);
    let temporal = QueryEngine::execute(
        &age_query,
        &[QuerySource {
            name: "$".into(),
            database: &db2,
            view: View::AsOf(1),
        }],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    assert_eq!(relation_values(temporal.result).len(), 2);
    assert_eq!(
        db1.datoms(View::Current, IndexOrder::Eavt),
        db2.datoms(View::AsOf(1), IndexOrder::Eavt)
    );

    let mut bag_query = age_query.clone();
    bag_query.with.push(var("e"));
    bag_query.clauses = vec![pattern(v("e"), kw("person", "age"), v("age"))];
    assert_eq!(
        relation_values(
            db1.query(&bag_query, &[], &QueryControl::default())
                .unwrap()
                .result
        )
        .len(),
        3
    );

    let mut input_query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("name"))]),
        vec![
            pattern(v("e"), kw("person", "age"), v("wanted")),
            pattern(v("e"), kw("person", "name"), v("name")),
        ],
    );
    input_query
        .inputs
        .push(atomic_core::InputSpec::Collection(var("wanted")));
    let result = db2
        .query(
            &input_query,
            &[atomic_core::QueryInput::Collection(vec![
                Value::Long(31),
                Value::Long(40),
            ])],
            &QueryControl::default(),
        )
        .unwrap();
    assert_eq!(relation_values(result.result).len(), 2);
}

#[test]
fn predicates_functions_aggregates_and_with_observe_documented_boundaries() {
    let (db, _, _) = database();
    let query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(var("name")),
            FindElement::Variable(var("next")),
        ]),
        vec![
            pattern(v("e"), kw("person", "name"), v("name")),
            pattern(v("e"), kw("person", "age"), v("age")),
            Clause::Predicate {
                predicate: Predicate::Greater,
                source: "$".into(),
                args: vec![v("age"), val(Value::Long(30))],
            },
            Clause::Function {
                function: Function::Add,
                source: "$".into(),
                args: vec![v("age"), val(Value::Long(1))],
                binding: Binding::Scalar(var("next")),
            },
        ],
    );
    assert_eq!(
        relation_values(
            db.query(&query, &[], &QueryControl::default())
                .unwrap()
                .result
        ),
        vec![vec![
            QueryValue::Scalar(Value::String("Alice".into())),
            QueryValue::Scalar(Value::Long(41)),
        ]]
    );

    let mut aggregate = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Aggregate {
                function: Aggregate::Count,
                variable: var("age"),
            },
            FindElement::Aggregate {
                function: Aggregate::CountDistinct,
                variable: var("age"),
            },
            FindElement::Aggregate {
                function: Aggregate::Average,
                variable: var("age"),
            },
            FindElement::Aggregate {
                function: Aggregate::Median,
                variable: var("age"),
            },
            FindElement::Aggregate {
                function: Aggregate::Variance,
                variable: var("age"),
            },
        ]),
        vec![pattern(v("e"), kw("person", "age"), v("age"))],
    );
    aggregate.with.push(var("e"));
    assert_eq!(
        db.query(&aggregate, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Tuple(Some(vec![
            QueryValue::Scalar(Value::Long(3)),
            QueryValue::Scalar(Value::Long(2)),
            QueryValue::Scalar(Value::Double(100.0 / 3.0)),
            QueryValue::Scalar(Value::Long(30)),
            QueryValue::Scalar(Value::Double(22.222_222_222_222_225)),
        ]))
    );
}

#[test]
fn negation_disjunction_and_recursive_rules_terminate_with_set_results() {
    let (_, db, ids) = database();
    let without_age = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("name"))]),
        vec![
            pattern(v("e"), kw("person", "name"), v("name")),
            Clause::Not {
                join: None,
                clauses: vec![pattern(v("e"), kw("person", "age"), Term::Blank)],
            },
        ],
    );
    assert_eq!(
        relation_values(
            db.query(&without_age, &[], &QueryControl::default())
                .unwrap()
                .result
        ),
        vec![vec![QueryValue::Scalar(Value::String("Dave".into()))]]
    );

    let choice = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("name"))]),
        vec![
            pattern(v("e"), kw("person", "name"), v("name")),
            Clause::Or {
                join: Some(vec![var("e")]),
                branches: vec![
                    vec![pattern(v("e"), kw("person", "age"), val(Value::Long(31)))],
                    vec![pattern(v("e"), kw("person", "age"), val(Value::Long(40)))],
                ],
            },
        ],
    );
    assert_eq!(
        relation_values(
            db.query(&choice, &[], &QueryControl::default())
                .unwrap()
                .result
        )
        .len(),
        2
    );

    let base = Rule {
        name: "reachable".into(),
        head: vec![var("x"), var("y")],
        required: BTreeSet::new(),
        clauses: vec![pattern(v("x"), kw("person", "friend"), v("y"))],
    };
    let recursive = Rule {
        name: "reachable".into(),
        head: vec![var("x"), var("z")],
        required: BTreeSet::new(),
        clauses: vec![
            pattern(v("x"), kw("person", "friend"), v("y")),
            Clause::Rule {
                source: "$".into(),
                name: "reachable".into(),
                args: vec![v("y"), v("z")],
            },
        ],
    };
    let mut reach = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("target"))]),
        vec![Clause::Rule {
            source: "$".into(),
            name: "reachable".into(),
            args: vec![val(Value::Ref(ids[0])), v("target")],
        }],
    );
    reach.rules = vec![base, recursive];
    let targets = relation_values(
        db.query(&reach, &[], &QueryControl::default())
            .unwrap()
            .result,
    );
    assert_eq!(targets.len(), 3);
}

#[test]
fn cancellation_and_work_limits_fail_with_structured_errors() {
    let (_, db, _) = database();
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("e"))]),
        vec![pattern(v("e"), Term::Blank, Term::Blank)],
    );
    let control = QueryControl::default();
    control.cancel.store(true, Ordering::Relaxed);
    assert_eq!(
        db.query(&query, &[], &control).unwrap_err().code,
        "query/canceled"
    );
    let control = QueryControl {
        max_work: 1,
        ..QueryControl::default()
    };
    assert_eq!(
        db.query(&query, &[], &control).unwrap_err().code,
        "query/work-limit"
    );
}

#[test]
fn all_input_and_find_shapes_multiple_sources_and_history_components_work() {
    let (db1, db2, ids) = database();
    let mut shaped = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Variable(var("a")),
            FindElement::Variable(var("b")),
            FindElement::Variable(var("c")),
        ]),
        Vec::new(),
    );
    shaped.inputs = vec![
        atomic_core::InputSpec::Scalar(var("a")),
        atomic_core::InputSpec::Tuple(vec![Some(var("b")), None]),
        atomic_core::InputSpec::Relation(vec![Some(var("c"))]),
    ];
    assert_eq!(
        db2.query(
            &shaped,
            &[
                atomic_core::QueryInput::Scalar(Value::Long(1)),
                atomic_core::QueryInput::Tuple(vec![Value::Long(2), Value::Long(99)]),
                atomic_core::QueryInput::Relation(vec![vec![Value::Long(3)]]),
            ],
            &QueryControl::default(),
        )
        .unwrap()
        .result,
        QueryResult::Tuple(Some(vec![
            QueryValue::Scalar(Value::Long(1)),
            QueryValue::Scalar(Value::Long(2)),
            QueryValue::Scalar(Value::Long(3)),
        ]))
    );

    let ground = Query::new(
        FindSpec::Collection(FindElement::Variable(var("x"))),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![val(Value::Tuple(vec![
                Some(Value::Long(1)),
                Some(Value::Long(2)),
            ]))],
            binding: Binding::Collection(var("x")),
        }],
    );
    assert_eq!(
        db2.query(&ground, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Collection(vec![
            QueryValue::Scalar(Value::Long(1)),
            QueryValue::Scalar(Value::Long(2)),
        ])
    );

    let mut old = DataPattern::new(
        Term::Constant(Value::Ref(ids[1])),
        kw("person", "age"),
        v("old"),
    );
    old.source = "$old".into();
    let mut new = DataPattern::new(
        Term::Constant(Value::Ref(ids[1])),
        kw("person", "age"),
        v("new"),
    );
    new.source = "$new".into();
    let compare = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Variable(var("old")),
            FindElement::Variable(var("new")),
        ]),
        vec![
            Clause::Pattern(Box::new(old)),
            Clause::Pattern(Box::new(new)),
        ],
    );
    assert_eq!(
        QueryEngine::execute(
            &compare,
            &[
                QuerySource {
                    name: "$".into(),
                    database: &db2,
                    view: View::Current,
                },
                QuerySource {
                    name: "$old".into(),
                    database: &db1,
                    view: View::Current,
                },
                QuerySource {
                    name: "$new".into(),
                    database: &db2,
                    view: View::Current,
                },
            ],
            &[],
            &QueryControl::default(),
        )
        .unwrap()
        .result,
        QueryResult::Tuple(Some(vec![
            QueryValue::Scalar(Value::Long(30)),
            QueryValue::Scalar(Value::Long(31)),
        ]))
    );

    let mut historical = DataPattern::new(
        Term::Constant(Value::Ref(ids[1])),
        kw("person", "age"),
        v("age"),
    );
    historical.transaction = Some(v("tx"));
    historical.added = Some(v("added"));
    let history_query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(var("age")),
            FindElement::Variable(var("tx")),
            FindElement::Variable(var("added")),
        ]),
        vec![Clause::Pattern(Box::new(historical))],
    );
    let history = QueryEngine::execute(
        &history_query,
        &[QuerySource {
            name: "$".into(),
            database: &db2,
            view: View::History,
        }],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    assert_eq!(relation_values(history.result).len(), 3);
}

#[test]
fn required_rule_bindings_and_result_limits_fail_stably() {
    let (_, db, _) = database();
    let rule = Rule {
        name: "named".into(),
        head: vec![var("e"), var("name")],
        required: BTreeSet::from([0]),
        clauses: vec![pattern(v("e"), kw("person", "name"), v("name"))],
    };
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("name"))]),
        vec![Clause::Rule {
            source: "$".into(),
            name: "named".into(),
            args: vec![v("e"), v("name")],
        }],
    );
    query.rules.push(rule);
    assert_eq!(
        db.query(&query, &[], &QueryControl::default())
            .unwrap_err()
            .code,
        "query/insufficient-binding"
    );

    let names = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("name"))]),
        vec![pattern(Term::Blank, kw("person", "name"), v("name"))],
    );
    let control = QueryControl {
        max_result_rows: 2,
        ..QueryControl::default()
    };
    assert_eq!(
        db.query(&names, &[], &control).unwrap_err().code,
        "query/result-limit"
    );
}

#[test]
fn explicit_join_clauses_isolate_non_join_variables() {
    let (_, db, _) = database();
    let mut query = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("e"))),
        vec![
            pattern(
                v("e"),
                kw("person", "name"),
                val(Value::String("Alice".into())),
            ),
            Clause::Not {
                join: Some(vec![var("e")]),
                clauses: vec![
                    pattern(v("x"), kw("person", "friend"), v("e")),
                    pattern(v("x"), kw("person", "name"), v("local")),
                ],
            },
        ],
    );
    query
        .inputs
        .push(atomic_core::InputSpec::Scalar(var("local")));
    let result = db
        .query(
            &query,
            &[atomic_core::QueryInput::Scalar(Value::String(
                "Dave".into(),
            ))],
            &QueryControl::default(),
        )
        .unwrap();
    assert_eq!(result.result, QueryResult::Scalar(None));

    let numeric_attribute = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("name"))),
        vec![pattern(
            Term::Blank,
            Term::Constant(Value::Ref(10)),
            v("name"),
        )],
    );
    assert!(matches!(
        db.query(&numeric_attribute, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(_))
    ));
}

#[test]
fn selective_optimized_plan_scales_with_matches_not_database_size() {
    let mut ops = Vec::new();
    for index in 0..500 {
        let temp = format!("person-{index}");
        ops.push(TxOp::Add {
            entity: EntityRef::Temp(temp.clone()),
            attribute: 10,
            value: TxValue::Scalar(Value::String(temp.clone())),
        });
        ops.push(TxOp::Add {
            entity: EntityRef::Temp(temp),
            attribute: 11,
            value: TxValue::Scalar(Value::Long(index)),
        });
    }
    let db = Database::new(schema())
        .unwrap()
        .with(&ops, 1_000)
        .unwrap()
        .db_after;
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("e"))),
        vec![pattern(v("e"), kw("person", "age"), val(Value::Long(499)))],
    );
    let optimized = db.query(&query, &[], &QueryControl::default()).unwrap();
    let reference = db
        .query(
            &query,
            &[],
            &QueryControl {
                force_scan: true,
                ..QueryControl::default()
            },
        )
        .unwrap();
    assert_eq!(optimized.result, reference.result);
    assert_eq!(optimized.stats.datoms_examined, 1);
    assert!(reference.stats.datoms_examined > 1_000);
}
