use atomic_core::{
    Aggregate, Attribute, Binding, Cardinality, Clause, DataPattern, Database, EntityRef,
    FindElement, FindSpec, Function, InputSpec, Keyword, Peer, PostgresIndexer, PostgresMigrator,
    PostgresStore, Predicate, PullPattern, Query, QueryControl, QueryEngine, QueryInput,
    QueryResult, QuerySource, QueryValue, Schema, Term, TransactionRequest, TupleSpec, TxOp, Value,
    ValueType, Variable,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

fn variable(name: &str) -> Variable {
    name.into()
}

fn find(name: &str) -> FindElement {
    FindElement::Variable(variable(name))
}

fn long(value: i64) -> QueryValue {
    QueryValue::Scalar(Value::Long(value))
}

fn tuple(values: &[i64]) -> Value {
    Value::Tuple(
        values
            .iter()
            .map(|value| Some(Value::Long(*value)))
            .collect(),
    )
}

fn function(query: Query, args: Vec<Term>, binding: Binding) -> Clause {
    Clause::Function {
        function: Function::Query(Box::new(query)),
        source: "$".into(),
        args,
        binding,
    }
}

fn collection_query(find: FindSpec) -> Query {
    let mut query = Query::new(find, vec![]);
    query.inputs = vec![InputSpec::Collection(variable("x"))];
    query
}

fn execute(query: &Query) -> QueryResult {
    Database::new(Schema::new())
        .unwrap()
        .query(query, &[], &QueryControl::default())
        .unwrap()
        .result
}

#[test]
fn collection_inputs_and_outputs_are_destructured_exactly_once() {
    let inner = collection_query(FindSpec::Collection(find("x")));
    let query = Query::new(
        FindSpec::Collection(find("item")),
        vec![function(
            inner.clone(),
            vec![Term::Constant(tuple(&[3, 1, 3, 2]))],
            Binding::Collection(variable("item")),
        )],
    );
    assert_eq!(
        execute(&query),
        QueryResult::Collection(vec![long(3), long(1), long(2)])
    );

    let scalar = Query::new(
        FindSpec::Scalar(find("all")),
        vec![function(
            inner,
            vec![Term::Constant(tuple(&[3, 1, 3, 2]))],
            Binding::Scalar(variable("all")),
        )],
    );
    assert_eq!(
        execute(&scalar),
        QueryResult::Scalar(Some(QueryValue::Collection(vec![
            long(3),
            long(1),
            long(2)
        ])))
    );

    // Nil elements are values, not a missing outer result or a shape error.
    let query = Query::new(
        FindSpec::Collection(find("item")),
        vec![function(
            collection_query(FindSpec::Collection(find("x"))),
            vec![Term::Constant(Value::Tuple(vec![
                None,
                Some(Value::Long(2)),
            ]))],
            Binding::Collection(variable("item")),
        )],
    );
    assert_eq!(
        execute(&query),
        QueryResult::Collection(vec![QueryValue::Nil, long(2)])
    );
}

#[test]
fn relation_and_tuple_results_retain_shape_until_the_outer_binding() {
    let inner = collection_query(FindSpec::Relation(vec![find("x"), find("x")]));
    let query = Query::new(
        FindSpec::Relation(vec![find("a"), find("b")]),
        vec![function(
            inner.clone(),
            vec![Term::Constant(tuple(&[1, 2]))],
            Binding::Relation(vec![Some(variable("a")), Some(variable("b"))]),
        )],
    );
    assert_eq!(
        execute(&query),
        QueryResult::Relation(vec![vec![long(1), long(1)], vec![long(2), long(2)]])
    );
    let whole = Query::new(
        FindSpec::Scalar(find("all")),
        vec![function(
            inner,
            vec![Term::Constant(tuple(&[1, 2]))],
            Binding::Scalar(variable("all")),
        )],
    );
    assert_eq!(
        execute(&whole),
        QueryResult::Scalar(Some(QueryValue::Collection(vec![
            QueryValue::Tuple(vec![long(1), long(1)]),
            QueryValue::Tuple(vec![long(2), long(2)]),
        ])))
    );

    let mut inner = Query::new(FindSpec::Tuple(vec![find("a"), find("b")]), vec![]);
    inner.inputs = vec![InputSpec::Tuple(vec![
        Some(variable("a")),
        Some(variable("b")),
    ])];
    for binding in [
        Binding::Scalar(variable("tuple")),
        Binding::Tuple(vec![Some(variable("first")), None]),
    ] {
        let (find_spec, expected) = if matches!(binding, Binding::Scalar(_)) {
            (
                FindSpec::Scalar(find("tuple")),
                QueryResult::Scalar(Some(QueryValue::Scalar(tuple(&[7, 8])))),
            )
        } else {
            (
                FindSpec::Scalar(find("first")),
                QueryResult::Scalar(Some(long(7))),
            )
        };
        assert_eq!(
            execute(&Query::new(
                find_spec,
                vec![function(
                    inner.clone(),
                    vec![Term::Constant(tuple(&[7, 8]))],
                    binding
                )],
            )),
            expected
        );
    }
}

#[test]
fn nested_container_results_can_be_used_as_collection_relation_and_scalar_inputs() {
    let first = collection_query(FindSpec::Relation(vec![find("x"), find("x")]));
    let mut second = Query::new(FindSpec::Collection(find("b")), vec![]);
    second.inputs = vec![InputSpec::Relation(vec![None, Some(variable("b"))])];
    let third = collection_query(FindSpec::Scalar(FindElement::Aggregate {
        function: Aggregate::Sum,
        variable: variable("x"),
    }));
    let query = Query::new(
        FindSpec::Scalar(find("sum")),
        vec![
            function(
                first,
                vec![Term::Constant(tuple(&[1, 2, 3]))],
                Binding::Scalar(variable("relation")),
            ),
            function(
                second,
                vec![Term::var("relation")],
                Binding::Scalar(variable("collection")),
            ),
            function(
                third,
                vec![Term::var("collection")],
                Binding::Scalar(variable("sum")),
            ),
        ],
    );
    assert_eq!(execute(&query), QueryResult::Scalar(Some(long(6))));

    // A collection-valued aggregate is not a scalar leaf, but is a valid
    // scalar function value and can be destructured by the outer binding.
    let query = Query::new(
        FindSpec::Collection(find("distinct")),
        vec![function(
            collection_query(FindSpec::Scalar(FindElement::Aggregate {
                function: Aggregate::Distinct,
                variable: variable("x"),
            })),
            vec![Term::Constant(tuple(&[2, 1, 2]))],
            Binding::Collection(variable("distinct")),
        )],
    );
    assert_eq!(
        execute(&query),
        QueryResult::Collection(vec![long(1), long(2)])
    );
}

#[test]
fn empty_query_results_preserve_nil_or_empty_container_as_values() {
    for (inner_find, expected) in [
        (FindSpec::Scalar(find("x")), QueryValue::Nil),
        (FindSpec::Tuple(vec![find("x")]), QueryValue::Nil),
        (
            FindSpec::Collection(find("x")),
            QueryValue::Collection(vec![]),
        ),
        (
            FindSpec::Relation(vec![find("x")]),
            QueryValue::Collection(vec![]),
        ),
    ] {
        let query = Query::new(
            FindSpec::Scalar(find("value")),
            vec![function(
                collection_query(inner_find),
                vec![Term::Constant(tuple(&[]))],
                Binding::Scalar(variable("value")),
            )],
        );
        assert_eq!(execute(&query), QueryResult::Scalar(Some(expected)));
    }
    let query = Query::new(
        FindSpec::Collection(find("value")),
        vec![function(
            collection_query(FindSpec::Collection(find("x"))),
            vec![Term::Constant(tuple(&[]))],
            Binding::Collection(variable("value")),
        )],
    );
    assert_eq!(execute(&query), QueryResult::Collection(vec![]));
}

#[test]
fn nested_pull_maps_use_exact_sources_and_survive_tuple_round_trip() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let report = Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: 1000,
                value: Value::String("old".into()).into(),
            }],
            1000,
        )
        .unwrap();
    let entity = report.tempids["item"];
    let old = report.db_after;
    let new = old
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: 1000,
                value: Value::String("new".into()).into(),
            }],
            2000,
        )
        .unwrap()
        .db_after;
    let mut inner = Query::new(
        FindSpec::Scalar(FindElement::Pull {
            source: "$".into(),
            variable: variable("e"),
            pattern: Box::new(PullPattern::wildcard()),
        }),
        vec![],
    );
    inner.inputs = vec![InputSpec::Scalar(variable("e"))];
    let mut nested = function(
        inner,
        vec![Term::Constant(Value::Ref(entity))],
        Binding::Scalar(variable("map")),
    );
    if let Clause::Function { source, .. } = &mut nested {
        *source = "$old".into();
    }
    let query = Query::new(
        FindSpec::Scalar(find("roundtrip")),
        vec![
            nested,
            Clause::Function {
                function: Function::Tuple,
                source: "$".into(),
                args: vec![Term::var("map"), Term::Nil],
                binding: Binding::Scalar(variable("tuple")),
            },
            Clause::Function {
                function: Function::Untuple,
                source: "$".into(),
                args: vec![Term::var("tuple")],
                binding: Binding::Tuple(vec![Some(variable("roundtrip")), None]),
            },
            Clause::Predicate {
                predicate: Predicate::Eq,
                source: "$".into(),
                args: vec![Term::var("map"), Term::var("roundtrip")],
            },
        ],
    );
    let sources = [
        QuerySource {
            name: "$".into(),
            database: new.database_value(),
        },
        QuerySource {
            name: "$old".into(),
            database: new.database_value().as_of(old.basis_t()),
        },
    ];
    let outcome = QueryEngine::execute(&query, &sources, &[], &QueryControl::default()).unwrap();
    let QueryResult::Scalar(Some(map)) = outcome.result else {
        panic!("expected nested map")
    };
    assert!(
        matches!(&map, QueryValue::Map(entries) if entries.iter().any(|(key, value)|
            key == &QueryValue::Scalar(Value::Keyword(Keyword::new("item", "name"))) &&
            value == &QueryValue::Scalar(Value::String("old".into()))
        ))
    );

    // Containers remain query values, never an unbound wildcard in an index.
    let mut no_index_match = query.clone();
    no_index_match
        .clauses
        .push(Clause::Pattern(Box::new(DataPattern::new(
            Term::var("map"),
            Term::Constant(Value::Long(1000)),
            Term::var("name"),
        ))));
    assert_eq!(
        QueryEngine::execute(&no_index_match, &sources, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(None)
    );
}

#[test]
fn invalid_destructuring_and_nested_input_budgets_fail_explicitly() {
    let database = Database::new(Schema::new()).unwrap();
    let mut scalar = Query::new(FindSpec::Scalar(find("x")), vec![]);
    scalar.inputs = vec![InputSpec::Scalar(variable("x"))];
    let wrong = Query::new(
        FindSpec::Collection(find("out")),
        vec![function(
            scalar,
            vec![Term::Constant(Value::Long(1))],
            Binding::Collection(variable("out")),
        )],
    );
    assert_eq!(
        database
            .query(&wrong, &[], &QueryControl::default())
            .unwrap_err()
            .code,
        "query/function-binding"
    );

    let mut query = Query::new(
        FindSpec::Collection(find("out")),
        vec![function(
            collection_query(FindSpec::Collection(find("x"))),
            vec![Term::var("values")],
            Binding::Collection(variable("out")),
        )],
    );
    query.inputs = vec![InputSpec::Scalar(variable("values"))];
    let inputs = [QueryInput::Scalar(tuple(&(0..100).collect::<Vec<_>>()))];
    let expected = database
        .query(&query, &inputs, &QueryControl::default())
        .unwrap();
    assert_eq!(
        database
            .query(
                &query,
                &inputs,
                &QueryControl {
                    max_intermediate_rows: 99,
                    ..QueryControl::default()
                }
            )
            .unwrap_err()
            .code,
        "query/intermediate-limit"
    );
    assert_eq!(
        database
            .query(
                &query,
                &inputs,
                &QueryControl {
                    max_work: expected.stats.work as usize - 1,
                    ..QueryControl::default()
                }
            )
            .unwrap_err()
            .code,
        "query/work-limit"
    );
}

#[test]
fn missing_nested_source_is_rejected_even_when_outer_inputs_have_no_rows() {
    let database = Database::new(Schema::new()).unwrap();
    let mut pattern = DataPattern::new(Term::var("e"), Term::Blank, Term::Blank);
    pattern.source = "$absent".into();
    let inner = Query::new(
        FindSpec::Collection(find("e")),
        vec![Clause::Pattern(Box::new(pattern))],
    );
    let mut outer = Query::new(
        FindSpec::Scalar(find("all")),
        vec![function(inner, vec![], Binding::Scalar(variable("all")))],
    );
    outer.inputs = vec![InputSpec::Collection(variable("none"))];
    assert_eq!(
        database
            .query(
                &outer,
                &[QueryInput::Collection(vec![])],
                &QueryControl::default()
            )
            .unwrap_err()
            .code,
        "query/unknown-source"
    );
}

#[test]
fn postgres_captured_nested_tuple_join_and_maps_work_after_new_writes_and_writer_stop() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL nested shapes: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&postgres, "query_nested_shapes");
    let postgres = fixture.connection.clone();
    const NAME: u32 = 1000;
    const PAIR: u32 = 1001;
    const LEFT: u32 = 1002;
    const RIGHT: u32 = 1003;
    let timeout = Duration::from_secs(30);
    let id = format!(
        "nested_shapes_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    PostgresMigrator::connect(&postgres)
        .unwrap()
        .migrate()
        .unwrap();
    let mut schema = Schema::new();
    for (attribute, name, kind) in [
        (NAME, "name", ValueType::String),
        (LEFT, "left", ValueType::Long),
        (RIGHT, "right", ValueType::Long),
    ] {
        schema
            .install(Attribute::new(
                attribute,
                Keyword::new("item", name),
                kind,
                Cardinality::One,
            ))
            .unwrap();
    }
    let mut pair = Attribute::new(
        PAIR,
        Keyword::new("item", "pair"),
        ValueType::Tuple,
        Cardinality::One,
    )
    .tuple(TupleSpec::Homogeneous(ValueType::Long));
    pair.indexed = true;
    schema.install(pair).unwrap();
    PostgresStore::connect(&postgres)
        .unwrap()
        .create_database(&id, schema)
        .unwrap();
    let writer = common::start_service(&postgres, &id);
    let peer = Peer::connect(&postgres, &id, 4).unwrap();
    let operations = |entity: EntityRef, left: i64, right: i64, name: &str| {
        [
            (NAME, Value::String(name.into())),
            (PAIR, tuple(&[left, right])),
            (LEFT, Value::Long(left)),
            (RIGHT, Value::Long(right)),
        ]
        .into_iter()
        .map(|(attribute, value)| TxOp::Add {
            entity: entity.clone(),
            attribute,
            value: value.into(),
        })
        .collect()
    };
    let first = writer
        .client()
        .transact(
            TransactionRequest::new(
                "first",
                operations(EntityRef::Temp("item".into()), 7, 8, "old"),
            )
            .with_tx_instant(1000),
            timeout,
        )
        .unwrap();
    let entity = first.tempids["item"];
    PostgresIndexer::connect(&postgres, &id)
        .unwrap()
        .consolidate()
        .unwrap();
    peer.sync_index(first.basis_t, timeout).unwrap();
    let old = peer.db();
    let second = writer
        .client()
        .transact(
            TransactionRequest::new("second", operations(EntityRef::Id(entity), 9, 10, "new"))
                .with_tx_instant(2000),
            timeout,
        )
        .unwrap();
    let current = peer.sync_to(second.basis_t, timeout).unwrap();
    writer.shutdown();

    let mut inner = Query::new(
        FindSpec::Tuple(vec![find("left"), find("right")]),
        vec![
            Clause::Pattern(Box::new(DataPattern::new(
                Term::var("e"),
                Term::Constant(Value::Long(LEFT.into())),
                Term::var("left"),
            ))),
            Clause::Pattern(Box::new(DataPattern::new(
                Term::var("e"),
                Term::Constant(Value::Long(RIGHT.into())),
                Term::var("right"),
            ))),
        ],
    );
    inner.inputs = vec![InputSpec::Scalar(variable("e"))];
    let nested_tuple = Clause::Function {
        function: Function::Query(Box::new(inner)),
        source: "$selected".into(),
        args: vec![Term::Constant(Value::Ref(entity))],
        binding: Binding::Scalar(variable("key")),
    };
    let mut join = DataPattern::new(
        Term::var("matched"),
        Term::Constant(Value::Long(PAIR.into())),
        Term::var("key"),
    );
    join.source = "$selected".into();
    let mut pull = Query::new(
        FindSpec::Scalar(FindElement::Pull {
            source: "$".into(),
            variable: variable("matched"),
            pattern: Box::new(PullPattern::wildcard()),
        }),
        vec![],
    );
    pull.inputs = vec![InputSpec::Scalar(variable("matched"))];
    let query = Query::new(
        FindSpec::Tuple(vec![find("key"), find("map")]),
        vec![
            nested_tuple,
            Clause::Pattern(Box::new(join)),
            Clause::Function {
                function: Function::Query(Box::new(pull)),
                source: "$selected".into(),
                args: vec![Term::var("matched")],
                binding: Binding::Scalar(variable("map")),
            },
        ],
    );
    for (selected, expected_pair, expected_name) in [
        (old, tuple(&[7, 8]), "old"),
        (current.clone(), tuple(&[9, 10]), "new"),
    ] {
        let outcome = QueryEngine::execute(
            &query,
            &[
                QuerySource {
                    name: "$".into(),
                    database: current.clone(),
                },
                QuerySource {
                    name: "$selected".into(),
                    database: selected,
                },
            ],
            &[],
            &QueryControl::default(),
        )
        .unwrap();
        let QueryResult::Tuple(Some(rows)) = outcome.result else {
            panic!("expected one joined tuple and map")
        };
        assert_eq!(rows[0], QueryValue::Scalar(expected_pair));
        assert!(
            matches!(&rows[1], QueryValue::Map(entries) if entries.iter().any(|(key, value)| key == &QueryValue::Scalar(Value::Keyword(Keyword::new("item", "name"))) && value == &QueryValue::Scalar(Value::String(expected_name.into()))))
        );
        assert!(outcome.stats.index_seeks >= 3);
        assert!(outcome.stats.datoms_examined <= 4);
    }
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
}
