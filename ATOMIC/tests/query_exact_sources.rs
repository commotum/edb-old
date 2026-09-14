use atomic_core::{
    Aggregate, Attribute, AttributeName, Binding, Cardinality, Clause, DataPattern, Database,
    EntityRef, FindElement, FindSpec, Function, Instruction, Keyword, Predicate, Program,
    ProgramKind, PullAttribute, PullPattern, Query, QueryControl, QueryEngine, QueryExtensions,
    QueryInput, QueryResult, QuerySource, QueryValue, Schema, Term, TxOp, TxValue, Unique, Value,
    ValueType, Variable,
};

const NAME: u32 = 1_000;
const AGE: u32 = 1_001;
const NICKNAME: u32 = 1_002;

fn var(name: &str) -> Variable {
    Variable::new(name).unwrap()
}

fn v(name: &str) -> Term {
    Term::Variable(var(name))
}

fn val(value: Value) -> Term {
    Term::Constant(value)
}

fn lookup(attribute: Keyword, value: Value) -> Term {
    val(Value::Tuple(vec![
        Some(Value::Keyword(attribute)),
        Some(value),
    ]))
}

fn kw(namespace: &str, name: &str) -> Term {
    val(Value::Keyword(Keyword::new(namespace, name)))
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
    schema
        .install(Attribute::new(
            NICKNAME,
            Keyword::new("person", "nickname"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn changing_person() -> (Database, u64, u64) {
    let first = Database::new(schema())
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("ada".into()),
                    attribute: NAME,
                    value: TxValue::Scalar(Value::String("Ada".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("ada".into()),
                    attribute: AGE,
                    value: TxValue::Scalar(Value::Long(30)),
                },
            ],
            1_000,
        )
        .unwrap();
    let entity = first.tempids["ada"];
    let old_t = first.db_after.basis_t();
    let second = first
        .db_after
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AGE,
                    value: TxValue::Scalar(Value::Long(31)),
                },
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: NICKNAME,
                    value: TxValue::Scalar(Value::String("Amazing Grace".into())),
                },
            ],
            2_000,
        )
        .unwrap();
    (second.db_after, entity, old_t)
}

#[test]
fn named_as_of_and_since_sources_govern_patterns_missing_and_functions() {
    let (current, entity, old_t) = changing_person();
    let old_lookup = lookup(Keyword::new("person", "name"), Value::String("Ada".into()));
    let mut old_age = DataPattern::new(old_lookup.clone(), kw("person", "age"), v("old-age"));
    old_age.source = "$old".into();
    let mut new_age = DataPattern::new(val(Value::Ref(entity)), kw("person", "age"), v("new-age"));
    new_age.source = "$since".into();
    let query = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Variable(var("old-age")),
            FindElement::Variable(var("new-age")),
            FindElement::Variable(var("nickname")),
            FindElement::Variable(var("found-attribute")),
            FindElement::Variable(var("found-value")),
        ]),
        vec![
            Clause::Pattern(Box::new(old_age)),
            Clause::Pattern(Box::new(new_age)),
            Clause::Predicate {
                predicate: Predicate::Missing,
                source: "$old".into(),
                args: vec![old_lookup.clone(), kw("person", "nickname")],
            },
            Clause::Function {
                function: Function::GetElse,
                source: "$old".into(),
                args: vec![
                    old_lookup.clone(),
                    kw("person", "nickname"),
                    val(Value::String("fallback".into())),
                ],
                binding: Binding::Scalar(var("nickname")),
            },
            Clause::Function {
                function: Function::GetSome,
                source: "$old".into(),
                args: vec![old_lookup, kw("person", "nickname"), kw("person", "name")],
                binding: Binding::Tuple(vec![
                    Some(var("found-attribute")),
                    Some(var("found-value")),
                ]),
            },
        ],
    );
    let sources = [
        QuerySource {
            name: "$old".into(),
            database: current.database_value().as_of(old_t),
        },
        QuerySource {
            name: "$since".into(),
            database: current.database_value().since(old_t),
        },
    ];
    let expected = QueryResult::Tuple(Some(vec![
        QueryValue::Scalar(Value::Long(30)),
        QueryValue::Scalar(Value::Long(31)),
        QueryValue::Scalar(Value::String("fallback".into())),
        QueryValue::Scalar(Value::Ref(u64::from(NAME))),
        QueryValue::Scalar(Value::String("Ada".into())),
    ]));
    assert_eq!(
        QueryEngine::execute(&query, &sources, &[], &QueryControl::default())
            .unwrap()
            .result,
        expected
    );
    assert_eq!(
        QueryEngine::execute(
            &query,
            &sources,
            &[],
            &QueryControl {
                force_scan: true,
                ..QueryControl::default()
            },
        )
        .unwrap()
        .result,
        expected
    );
}

#[test]
fn history_and_filter_sources_do_not_fall_back_to_current_data() {
    let (current, entity, _) = changing_person();
    let mut history_age = DataPattern::new(val(Value::Ref(entity)), kw("person", "age"), v("age"));
    history_age.source = "$history".into();
    history_age.transaction = Some(v("tx"));
    history_age.added = Some(v("added"));
    let history_query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(var("age")),
            FindElement::Variable(var("tx")),
            FindElement::Variable(var("added")),
        ]),
        vec![Clause::Pattern(Box::new(history_age))],
    );
    let history = QueryEngine::execute(
        &history_query,
        &[QuerySource {
            name: "$history".into(),
            database: current.database_value().history(),
        }],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    let QueryResult::Relation(history_rows) = history.result else {
        panic!("expected history relation")
    };
    assert_eq!(history_rows.len(), 3);
    assert_eq!(
        history_rows
            .iter()
            .filter(|row| row[2] == QueryValue::Scalar(Value::Bool(false)))
            .count(),
        1
    );

    let filtered = current
        .database_value()
        .filter(|_, datom| datom.attribute != AGE);
    let mut filtered_age = DataPattern::new(val(Value::Ref(entity)), kw("person", "age"), v("age"));
    filtered_age.source = "$filtered".into();
    let pattern_query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(var("age"))]),
        vec![Clause::Pattern(Box::new(filtered_age))],
    );
    assert_eq!(
        QueryEngine::execute(
            &pattern_query,
            &[QuerySource {
                name: "$filtered".into(),
                database: filtered.clone(),
            }],
            &[],
            &QueryControl::default(),
        )
        .unwrap()
        .result,
        QueryResult::Relation(Vec::new())
    );

    let mut extensions = QueryExtensions::new();
    extensions.register_local("read-age", |database, args, _| {
        let [Value::Ref(entity)] = args else {
            panic!("test supplies one entity ref")
        };
        Ok(vec![vec![
            database
                .values(*entity, AGE)?
                .first()
                .cloned()
                .unwrap_or(Value::Long(-1)),
        ]])
    });
    let filtered_functions = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Variable(var("fallback")),
            FindElement::Variable(var("extension")),
        ]),
        vec![
            Clause::Predicate {
                predicate: Predicate::Missing,
                source: "$filtered".into(),
                args: vec![val(Value::Ref(entity)), kw("person", "age")],
            },
            Clause::Function {
                function: Function::GetElse,
                source: "$filtered".into(),
                args: vec![
                    val(Value::Ref(entity)),
                    kw("person", "age"),
                    val(Value::Long(99)),
                ],
                binding: Binding::Scalar(var("fallback")),
            },
            Clause::Function {
                function: Function::Extension("read-age".into()),
                source: "$filtered".into(),
                args: vec![val(Value::Ref(entity))],
                binding: Binding::Scalar(var("extension")),
            },
        ],
    );
    assert_eq!(
        QueryEngine::execute_with_extensions(
            &filtered_functions,
            &[QuerySource {
                name: "$filtered".into(),
                database: filtered,
            }],
            &[],
            &QueryControl::default(),
            Some(&extensions),
        )
        .unwrap()
        .result,
        QueryResult::Tuple(Some(vec![
            QueryValue::Scalar(Value::Long(99)),
            QueryValue::Scalar(Value::Long(-1)),
        ]))
    );
}

#[test]
fn persisted_extension_and_find_pull_use_the_selected_as_of_value() {
    let (current, entity, old_t) = changing_person();
    let old = current.database_value().as_of(old_t);
    let mut extensions = QueryExtensions::new();
    extensions
        .register_program(
            "read-age",
            [0xAB; 32],
            Program {
                kind: ProgramKind::Query,
                arity: 1,
                instructions: vec![
                    Instruction::PushArgument(0),
                    Instruction::LoadOne(AGE),
                    Instruction::EmitRow(1),
                    Instruction::Return,
                ],
            },
        )
        .unwrap();
    let extension_query = Query::new(
        FindSpec::Scalar(FindElement::Variable(var("age"))),
        vec![Clause::Function {
            function: Function::Extension("read-age".into()),
            source: "$".into(),
            args: vec![val(Value::Ref(entity))],
            binding: Binding::Scalar(var("age")),
        }],
    );
    assert_eq!(
        old.query_with_extensions(&extension_query, &[], &QueryControl::default(), &extensions,)
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(30))))
    );

    let mut person = DataPattern::new(
        v("entity"),
        kw("person", "name"),
        val(Value::String("Ada".into())),
    );
    person.source = "$old".into();
    let pull_query = Query::new(
        FindSpec::Scalar(FindElement::Pull {
            source: "$old".into(),
            variable: var("entity"),
            pattern: Box::new(PullPattern::attributes(vec![PullAttribute::forward(
                AttributeName::Id(AGE),
            )])),
        }),
        vec![Clause::Pattern(Box::new(person))],
    );
    assert_eq!(
        QueryEngine::execute(
            &pull_query,
            &[QuerySource {
                name: "$old".into(),
                database: old,
            }],
            &[],
            &QueryControl::default(),
        )
        .unwrap()
        .result,
        QueryResult::Scalar(Some(QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Keyword(Keyword::new("person", "age"))),
            QueryValue::Scalar(Value::Long(30)),
        )])))
    );
}

#[test]
fn source_validation_tracks_semantic_consumers_not_placeholder_fields() {
    let pure = Query {
        find: FindSpec::Scalar(FindElement::Variable(var("input"))),
        with: Vec::new(),
        inputs: vec![atomic_core::InputSpec::Scalar(var("input"))],
        clauses: vec![Clause::Predicate {
            predicate: Predicate::Eq,
            source: "$not-consumed".into(),
            args: vec![val(Value::Long(1)), val(Value::Long(1))],
        }],
        rules: Vec::new(),
    };
    assert_eq!(
        QueryEngine::execute(
            &pure,
            &[],
            &[QueryInput::Scalar(Value::Long(7))],
            &QueryControl::default(),
        )
        .unwrap()
        .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(7))))
    );

    let pure_rule = Query {
        find: FindSpec::Scalar(FindElement::Variable(var("answer"))),
        with: Vec::new(),
        inputs: Vec::new(),
        clauses: vec![Clause::Rule {
            source: "$not-consumed".into(),
            name: "constant-answer".into(),
            args: vec![v("answer")],
        }],
        rules: vec![atomic_core::Rule {
            name: "constant-answer".into(),
            head: vec![var("answer")],
            required: Default::default(),
            clauses: vec![Clause::Function {
                function: Function::Ground,
                source: "$also-not-consumed".into(),
                args: vec![val(Value::Long(42))],
                binding: Binding::Scalar(var("answer")),
            }],
        }],
    };
    assert_eq!(
        QueryEngine::execute(&pure_rule, &[], &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(42))))
    );

    let missing_pull_source = Query {
        find: FindSpec::Scalar(FindElement::Pull {
            source: "$missing".into(),
            variable: var("entity"),
            pattern: Box::new(PullPattern::attributes(Vec::new())),
        }),
        with: Vec::new(),
        inputs: vec![atomic_core::InputSpec::Scalar(var("entity"))],
        clauses: vec![Clause::Predicate {
            predicate: Predicate::Eq,
            source: "$not-consumed".into(),
            args: vec![val(Value::Long(1)), val(Value::Long(2))],
        }],
        rules: Vec::new(),
    };
    assert_eq!(
        QueryEngine::execute(
            &missing_pull_source,
            &[],
            &[QueryInput::Scalar(Value::Ref(1))],
            &QueryControl::default(),
        )
        .unwrap_err()
        .code,
        "query/unknown-source"
    );

    let (current, _, _) = changing_person();
    assert_eq!(
        QueryEngine::execute(
            &missing_pull_source,
            &[QuerySource {
                name: "$missing".into(),
                database: current.database_value().history(),
            }],
            &[QueryInput::Scalar(Value::Ref(1))],
            &QueryControl::default(),
        )
        .unwrap_err()
        .code,
        "database/history-not-point-in-time"
    );

    let empty_min_max = Query::new(
        FindSpec::Tuple(vec![
            FindElement::Aggregate {
                function: Aggregate::Min,
                variable: var("value"),
            },
            FindElement::Aggregate {
                function: Aggregate::Max,
                variable: var("value"),
            },
        ]),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$not-consumed".into(),
            args: vec![val(Value::Tuple(Vec::new()))],
            binding: Binding::Collection(var("value")),
        }],
    );
    assert_eq!(
        QueryEngine::execute(&empty_min_max, &[], &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Tuple(Some(vec![QueryValue::Nil, QueryValue::Nil]))
    );
}

#[test]
fn find_pull_shares_the_enclosing_query_cancel_deadline_and_work_budget() {
    let (current, entity, _) = changing_person();
    let query = Query {
        find: FindSpec::Scalar(FindElement::Pull {
            source: "$".into(),
            variable: var("entity"),
            pattern: Box::new(PullPattern::attributes(vec![PullAttribute::forward(
                AttributeName::Id(AGE),
            )])),
        }),
        with: Vec::new(),
        inputs: vec![atomic_core::InputSpec::Scalar(var("entity"))],
        clauses: Vec::new(),
        rules: Vec::new(),
    };
    let sources = [QuerySource {
        name: "$".into(),
        database: current.database_value(),
    }];
    let inputs = [QueryInput::Scalar(Value::Ref(entity))];

    let canceled = QueryControl::default();
    canceled
        .cancel
        .store(true, std::sync::atomic::Ordering::Relaxed);
    assert_eq!(
        QueryEngine::execute(&query, &sources, &inputs, &canceled)
            .unwrap_err()
            .code,
        "query/canceled"
    );

    let timed_out = QueryControl {
        timeout: Some(std::time::Duration::ZERO),
        ..QueryControl::default()
    };
    assert_eq!(
        QueryEngine::execute(&query, &sources, &inputs, &timed_out)
            .unwrap_err()
            .code,
        "query/timeout"
    );

    let exhausted = QueryControl {
        max_work: 0,
        ..QueryControl::default()
    };
    assert_eq!(
        QueryEngine::execute(&query, &sources, &inputs, &exhausted)
            .unwrap_err()
            .code,
        "query/work-limit"
    );
}
