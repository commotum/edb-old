use atomic_core::{
    Aggregate, Attribute, Binding, Cardinality, Clause, DataPattern, Database, EntityRef,
    FindElement, FindSpec, Function, InputSpec, Keyword, Query, QueryControl, QueryInput,
    QueryResult, QueryValue, Rule, Schema, Term, TxOp, TxValue, Unique, Value, ValueType, Variable,
};
use std::collections::BTreeSet;

const NAME: u32 = 1_000;
const AGE: u32 = 1_001;
const FRIEND: u32 = 1_002;

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
    schema
        .install(
            Attribute::new(
                NAME,
                Keyword::new("person", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
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

fn people() -> (Database, [u64; 3]) {
    let report = Database::new(schema())
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("alice".into()),
                    attribute: AGE,
                    value: TxValue::Scalar(Value::Long(40)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("alice".into()),
                    attribute: FRIEND,
                    value: TxValue::Entity(EntityRef::Temp("bob".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("bob".into()),
                    attribute: AGE,
                    value: TxValue::Scalar(Value::Long(30)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("cara".into()),
                    attribute: AGE,
                    value: TxValue::Scalar(Value::Long(30)),
                },
            ],
            1_000,
        )
        .unwrap();
    let ids = [
        report.tempids["alice"],
        report.tempids["bob"],
        report.tempids["cara"],
    ];
    (report.db_after, ids)
}

#[test]
fn get_some_returns_attribute_eid_and_validates_every_candidate_cardinality() {
    let (database, ids) = people();
    let query = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Variable(var("attribute")),
            FindElement::Variable(var("value")),
        ]),
        vec![Clause::Function {
            function: Function::GetSome,
            source: "$".into(),
            args: vec![val(Value::Ref(ids[0])), kw("person", "age")],
            binding: Binding::Tuple(vec![Some(var("attribute")), Some(var("value"))]),
        }],
    );
    assert_eq!(
        database
            .query(&query, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Tuple(Some(vec![
            QueryValue::Scalar(Value::Ref(u64::from(AGE))),
            QueryValue::Scalar(Value::Long(40)),
        ]))
    );

    let invalid_later_candidate = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("value"))),
        vec![Clause::Function {
            function: Function::GetSome,
            source: "$".into(),
            args: vec![
                val(Value::Ref(ids[0])),
                kw("person", "age"),
                kw("person", "friend"),
            ],
            binding: Binding::Tuple(vec![None, Some(var("value"))]),
        }],
    );
    assert_eq!(
        database
            .query(&invalid_later_candidate, &[], &QueryControl::default())
            .unwrap_err()
            .code,
        "query/get-some-cardinality"
    );
}

#[test]
fn bounded_min_and_max_preserve_bag_duplicates() {
    let (database, _) = people();
    let mut query = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Aggregate {
                function: Aggregate::MinN(3),
                variable: var("age"),
            },
            FindElement::Aggregate {
                function: Aggregate::MaxN(3),
                variable: var("age"),
            },
        ]),
        vec![pattern(v("entity"), kw("person", "age"), v("age"))],
    );
    query.with.push(var("entity"));
    assert_eq!(
        database
            .query(&query, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Tuple(Some(vec![
            QueryValue::Collection(vec![
                QueryValue::Scalar(Value::Long(30)),
                QueryValue::Scalar(Value::Long(30)),
                QueryValue::Scalar(Value::Long(40)),
            ]),
            QueryValue::Collection(vec![
                QueryValue::Scalar(Value::Long(40)),
                QueryValue::Scalar(Value::Long(30)),
                QueryValue::Scalar(Value::Long(30)),
            ]),
        ]))
    );
}

#[test]
fn ground_honors_binding_shape_and_tuple_nil_remains_query_only() {
    let database = Database::new(schema()).unwrap();
    let tuple = Value::Tuple(vec![Some(Value::Long(1)), None]);

    let empty_tuple = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("tuple"))),
        vec![Clause::Function {
            function: Function::Tuple,
            source: "$unused".into(),
            args: Vec::new(),
            binding: Binding::Scalar(var("tuple")),
        }],
    );
    assert_eq!(
        atomic_core::QueryEngine::execute(&empty_tuple, &[], &[], &QueryControl::default())
            .unwrap_err()
            .code,
        "query/function-arity"
    );

    let scalar_nil = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("nil"))),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$unused".into(),
            args: vec![Term::Nil],
            binding: Binding::Scalar(var("nil")),
        }],
    );
    assert_eq!(
        atomic_core::QueryEngine::execute(&scalar_nil, &[], &[], &QueryControl::default(),)
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Nil))
    );

    let direct_tuple_nil = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("tuple"))),
        vec![Clause::Function {
            function: Function::Tuple,
            source: "$unused".into(),
            args: vec![val(Value::Long(1)), Term::Nil],
            binding: Binding::Scalar(var("tuple")),
        }],
    );
    assert_eq!(
        atomic_core::QueryEngine::execute(&direct_tuple_nil, &[], &[], &QueryControl::default(),)
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(tuple.clone())))
    );

    let scalar = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("value"))),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![val(tuple.clone())],
            binding: Binding::Scalar(var("value")),
        }],
    );
    assert_eq!(
        database
            .query(&scalar, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(tuple.clone())))
    );

    let tuple_binding = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Variable(var("left")),
            FindElement::Variable(var("right")),
        ]),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![val(tuple.clone())],
            binding: Binding::Tuple(vec![Some(var("left")), Some(var("right"))]),
        }],
    );
    assert_eq!(
        database
            .query(&tuple_binding, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Tuple(Some(vec![
            QueryValue::Scalar(Value::Long(1)),
            QueryValue::Nil,
        ]))
    );

    let collection = Query::new(
        FindSpec::Collection(FindElement::Variable(var("item"))),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![val(Value::Tuple(vec![
                Some(Value::Long(1)),
                None,
                Some(Value::Long(2)),
            ]))],
            binding: Binding::Collection(var("item")),
        }],
    );
    assert_eq!(
        database
            .query(&collection, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Collection(vec![
            QueryValue::Scalar(Value::Long(1)),
            QueryValue::Nil,
            QueryValue::Scalar(Value::Long(2)),
        ])
    );

    let relation = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(var("left")),
            FindElement::Variable(var("right")),
        ]),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![val(Value::Tuple(vec![
                Some(Value::Tuple(vec![Some(Value::Long(1)), None])),
                Some(Value::Tuple(vec![
                    Some(Value::Long(2)),
                    Some(Value::Long(3)),
                ])),
            ]))],
            binding: Binding::Relation(vec![Some(var("left")), Some(var("right"))]),
        }],
    );
    assert_eq!(
        database
            .query(&relation, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Relation(vec![
            vec![QueryValue::Scalar(Value::Long(1)), QueryValue::Nil],
            vec![
                QueryValue::Scalar(Value::Long(2)),
                QueryValue::Scalar(Value::Long(3)),
            ],
        ])
    );

    let nil_round_trip = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Variable(var("nil")),
            FindElement::Variable(var("rebuilt")),
        ]),
        vec![
            Clause::Function {
                function: Function::Ground,
                source: "$".into(),
                args: vec![val(tuple.clone())],
                binding: Binding::Scalar(var("tuple")),
            },
            Clause::Function {
                function: Function::Untuple,
                source: "$".into(),
                args: vec![v("tuple")],
                binding: Binding::Tuple(vec![Some(var("one")), Some(var("nil"))]),
            },
            Clause::Function {
                function: Function::Tuple,
                source: "$".into(),
                args: vec![v("one"), v("nil")],
                binding: Binding::Scalar(var("rebuilt")),
            },
        ],
    );
    assert_eq!(
        database
            .query(&nil_round_trip, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Tuple(Some(vec![QueryValue::Nil, QueryValue::Scalar(tuple)]))
    );

    let mut variable_ground = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("result"))),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![v("input")],
            binding: Binding::Scalar(var("result")),
        }],
    );
    variable_ground.inputs.push(InputSpec::Scalar(var("input")));
    assert_eq!(
        database
            .query(
                &variable_ground,
                &[QueryInput::Scalar(Value::Long(1))],
                &QueryControl::default(),
            )
            .unwrap_err()
            .code,
        "query/ground-not-constant"
    );
}

#[test]
fn pattern_nil_matches_no_stored_value_and_empty_min_max_are_nil() {
    let database = Database::new(schema()).unwrap();
    let no_nil_fact = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("entity"))]),
        vec![pattern(v("entity"), kw("person", "age"), Term::Nil)],
    );
    assert_eq!(
        database
            .query(&no_nil_fact, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Relation(Vec::new())
    );

    let empty_extrema = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Aggregate {
                function: Aggregate::Min,
                variable: var("age"),
            },
            FindElement::Aggregate {
                function: Aggregate::Max,
                variable: var("age"),
            },
        ]),
        vec![pattern(Term::Blank, kw("person", "age"), v("age"))],
    );
    assert_eq!(
        database
            .query(&empty_extrema, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Tuple(Some(vec![QueryValue::Nil, QueryValue::Nil]))
    );
}

#[test]
fn finite_rules_can_converge_beyond_128_derivation_rounds() {
    const EDGE_COUNT: usize = 140;
    let mut edges = Vec::with_capacity(EDGE_COUNT);
    for index in 0..EDGE_COUNT {
        edges.push(TxOp::Add {
            entity: EntityRef::Temp(format!("node-{index}")),
            attribute: FRIEND,
            value: TxValue::Entity(EntityRef::Temp(format!("node-{}", index + 1))),
        });
    }
    // The terminal tempid must name an entity in E position as well as being
    // referenced from V. Datomic does not allocate a tempid used only as a
    // transaction value.
    edges.push(TxOp::Add {
        entity: EntityRef::Temp(format!("node-{EDGE_COUNT}")),
        attribute: AGE,
        value: TxValue::Scalar(Value::Long(0)),
    });
    let report = Database::new(schema())
        .unwrap()
        .with(&edges, 1_000)
        .unwrap();
    let root = report.tempids["node-0"];
    let last = report.tempids[&format!("node-{EDGE_COUNT}")];
    let base = Rule {
        name: "from-root".into(),
        head: vec![var("target")],
        required: BTreeSet::new(),
        clauses: vec![pattern(
            val(Value::Ref(root)),
            kw("person", "friend"),
            v("target"),
        )],
    };
    let recursive = Rule {
        name: "from-root".into(),
        head: vec![var("target")],
        required: BTreeSet::new(),
        clauses: vec![
            Clause::Rule {
                source: "$".into(),
                name: "from-root".into(),
                args: vec![v("middle")],
            },
            pattern(v("middle"), kw("person", "friend"), v("target")),
        ],
    };
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("target"))]),
        vec![Clause::Rule {
            source: "$".into(),
            name: "from-root".into(),
            args: vec![v("target")],
        }],
    );
    query.rules = vec![base, recursive];
    let outcome = report
        .db_after
        .query(
            &query,
            &[],
            &QueryControl {
                max_work: 2_000_000,
                ..QueryControl::default()
            },
        )
        .unwrap();
    let QueryResult::Relation(targets) = outcome.result else {
        panic!("expected relation")
    };
    assert_eq!(targets.len(), EDGE_COUNT);
    assert!(targets.contains(&vec![QueryValue::Scalar(Value::Ref(last))]));
    assert!(outcome.stats.rule_iterations > 128);
}
