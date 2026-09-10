mod common;

use atomic_core::edn::{EdnValue, read_edn};
use atomic_core::edn_pull::{
    EdnAdapterLimits, EdnPullTransforms, parse_pull_edn, parse_pull_edn_with_transforms,
    pull_pattern_from_edn_with_limits,
};
use atomic_core::edn_query::{EdnQueryArgument, parse_query_edn};
use atomic_core::{
    Attribute, AttributeName, Binding, Cardinality, Clause, DataPattern, Database, DatabaseValue,
    EntityRef, FindElement, FindSpec, Function, InputSpec, Keyword, Predicate, PullAttribute,
    PullLimit, PullNested, PullPattern, PullTransform, Query, QueryControl, QueryEngine,
    QueryExtensions, QueryInput, QueryResult, QuerySourceValue, QueryValue, Schema, Term, TxOp,
    TxValue, Value, ValueType,
};

fn data(text: &str) -> EdnQueryArgument {
    EdnQueryArgument::Data(read_edn(text).unwrap())
}
fn execute(text: &str, args: &[EdnQueryArgument]) -> QueryResult {
    parse_query_edn(text)
        .unwrap()
        .bind(args)
        .unwrap()
        .execute(&QueryControl::default(), None)
        .unwrap()
        .result
}
fn long(value: i64) -> QueryValue {
    QueryValue::Scalar(Value::Long(value))
}
fn v(name: &str) -> Term {
    Term::var(name)
}
fn f(name: &str) -> FindElement {
    FindElement::Variable(name.into())
}

#[test]
fn vector_list_and_map_queries_preserve_native_ast_bindings_and_find_shapes() {
    let forms = [
        "[:find ?x ?y :in [?x ...] ?minimum :where [(+ ?x 1) ?y] [(> ?y ?minimum)]]",
        "(:find ?x ?y :in [?x ...] ?minimum :where [(+ ?x 1) ?y] [(> ?y ?minimum)])",
        "{:where [[(+ ?x 1) ?y] [(> ?y ?minimum)]] :in [[?x ...] ?minimum] :find [?x ?y]}",
    ];
    let mut typed = Query::new(
        FindSpec::Relation(vec![f("?x"), f("?y")]),
        vec![
            Clause::Function {
                function: Function::Add,
                source: "$".into(),
                args: vec![v("?x"), Term::Constant(Value::Long(1))],
                binding: Binding::Scalar("?y".into()),
            },
            Clause::Predicate {
                predicate: Predicate::Greater,
                source: "$".into(),
                args: vec![v("?y"), v("?minimum")],
            },
        ],
    );
    typed.inputs = vec![
        InputSpec::Collection("?x".into()),
        InputSpec::Scalar("?minimum".into()),
    ];
    let expected = QueryEngine::execute_sources(
        &typed,
        &[],
        &[
            QueryInput::Collection(vec![Value::Long(1), Value::Long(3)]),
            QueryInput::Scalar(Value::Long(2)),
        ],
        &QueryControl::default(),
    )
    .unwrap()
    .result;
    assert_eq!(
        expected,
        QueryResult::Relation(vec![vec![long(3), long(4)]])
    );
    for text in forms {
        let bound = parse_query_edn(text)
            .unwrap()
            .bind(&[data("[1 3]"), data("2")])
            .unwrap();
        assert_eq!(bound.query(), &typed);
        assert_eq!(
            bound
                .execute(&QueryControl::default(), None)
                .unwrap()
                .result,
            expected
        );
    }
    assert_eq!(
        execute("[:find [?x ...] :in [?x ...]]", &[data("[3 1 3]")]),
        QueryResult::Collection(vec![long(3), long(1)])
    );
    assert_eq!(
        execute("[:find [?x ?y] :in [?x ?y]]", &[data("[3 4]")]),
        QueryResult::Tuple(Some(vec![long(3), long(4)]))
    );
    assert_eq!(
        execute("[:find (?x ?y) :in (?x ?y)]", &[data("(3 4)")]),
        QueryResult::Tuple(Some(vec![long(3), long(4)]))
    );
    assert_eq!(
        execute("[:find (?x ...) :in (?x ...)]", &[data("(3 4)")]),
        QueryResult::Collection(vec![long(3), long(4)])
    );
    assert_eq!(
        execute("[:find ?x . :in [[?x _]]]", &[data("[[3 4]]")]),
        QueryResult::Scalar(Some(long(3)))
    );
}

#[test]
fn every_native_aggregate_and_with_is_reachable_from_edn() {
    let cases = [
        ("count", long(3)),
        ("count-distinct", long(2)),
        ("sum", long(5)),
        ("min", long(1)),
        ("max", long(2)),
        ("median", long(2)),
    ];
    for (name, expected) in cases {
        assert_eq!(
            execute(
                &format!("[:find ({name} ?v) . :with ?id :in [[?id ?v]]]"),
                &[data("[[1 1] [2 2] [3 2]]")]
            ),
            QueryResult::Scalar(Some(expected))
        );
    }
    for name in [
        "avg", "variance", "stddev", "distinct", "min 2", "max 2", "rand 2", "sample 2",
    ] {
        let result = execute(
            &format!("[:find ({name} ?v) . :in [?v ...]]"),
            &[data("[1 2 3]")],
        );
        assert!(matches!(result, QueryResult::Scalar(Some(_))), "{name}");
    }
}

#[test]
fn source_order_raw_relations_rules_negation_and_or_dependencies_are_preserved() {
    let tuples = data("[[1 :kind :person] [2 :kind :person] [3 :kind :person] [2 :blocked true]]");
    let rules = data("[[(eligible [?e]) (not [?e :blocked true])]]");
    assert_eq!(
        execute(
            "[:find [?e ...] :in $ % :where [?e :kind :person] [eligible ?e]]",
            &[tuples.clone(), rules.clone()]
        ),
        QueryResult::Collection(vec![long(1), long(3)])
    );
    assert_eq!(
        execute(
            "[:find [?e ...] :in $ % :where [?e :kind :person] (eligible ?e)]",
            &[tuples, rules]
        ),
        QueryResult::Collection(vec![long(1), long(3)])
    );
    assert_eq!(
        execute(
            "[:find [?e ...] :in $left $right :where (or-join [?e ?n] (and [(> ?n 2)] [$right ?e :ok true]) [(< ?n 0)]) [$left ?e :score ?n]]",
            &[
                data("[[1 :score 3] [2 :score 1] [3 :score -1]]"),
                data("[[1 :ok true]]")
            ]
        ),
        QueryResult::Collection(vec![long(1), long(3)])
    );
    let recursive = data(
        "[[(path ?a ?b) [?a :edge ?b]] [(path ?a ?b) [?a :edge ?x] (path ?x ?b)] [(safe [?a]) (not-join [?a] (path ?a 3))]]",
    );
    assert_eq!(
        execute(
            "[:find [?a ...] :in $ % :where [?a :kind :person] (safe ?a)]",
            &[
                data(
                    "[[1 :kind :person] [2 :kind :person] [4 :kind :person] [1 :edge 2] [2 :edge 3]]"
                ),
                recursive
            ]
        ),
        QueryResult::Collection(vec![long(4)])
    );
}

#[test]
fn nested_q_binds_find_shapes_and_named_sources_without_connection_access() {
    assert_eq!(
        execute(
            "[:find [?x ...] :in :where [(q [:find [?v ...] :in [?v ...]] [3 1 3]) [?x ...]]]",
            &[]
        ),
        QueryResult::Collection(vec![long(3), long(1)])
    );
    assert_eq!(
        execute(
            "[:find ?n . :in $outer :where [(q [:find (count ?e) . :in $inner :where [$inner ?e :kind :person]] $outer) ?n]]",
            &[data("[[1 :kind :person] [2 :kind :person]]")]
        ),
        QueryResult::Scalar(Some(long(2)))
    );
    assert_eq!(
        execute(
            "[:find [?x ...] :in $outer % :where [(q [:find [?e ...] :in $ % :where (selected ?e)] $outer %) [?x ...]]]",
            &[
                data("[[1 :kind :person] [2 :kind :animal]]"),
                data("[[(selected ?e) [?e :kind :person]]]")
            ]
        ),
        QueryResult::Collection(vec![long(1)])
    );
}

#[test]
fn return_map_keys_preserve_key_domain_column_order_and_empty_shape() {
    for (kind, key) in [("keys", ":x"), ("strs", "\"x\""), ("syms", "x")] {
        let bound = parse_query_edn(&format!("[:find [?x ?y] :{kind} x y :in [?x ?y]]"))
            .unwrap()
            .bind(&[data("[1 2]")])
            .unwrap();
        let outcome = bound.execute(&QueryControl::default(), None).unwrap();
        let result = bound.result_to_edn(&outcome.result).unwrap();
        let EdnValue::Map(entries) = result else {
            panic!("tuple return map must remain one map")
        };
        assert_eq!(entries[0], (read_edn(key).unwrap(), EdnValue::Long(1)));
        assert_eq!(entries.len(), 2);
    }
    let bound = parse_query_edn("[:find ?x :keys x :in [?x ...]]")
        .unwrap()
        .bind(&[data("[]")])
        .unwrap();
    assert_eq!(
        bound
            .result_to_edn(
                &bound
                    .execute(&QueryControl::default(), None)
                    .unwrap()
                    .result
            )
            .unwrap(),
        EdnValue::Set(vec![])
    );
}

fn fixture() -> (DatabaseValue, DatabaseValue, u64, u64) {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            1000,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ),
        Attribute::new(
            1001,
            Keyword::new("person", "age"),
            ValueType::Long,
            Cardinality::One,
        ),
        Attribute::new(
            1002,
            Keyword::new("person", "friend"),
            ValueType::Ref,
            Cardinality::Many,
        ),
    ] {
        schema.install(attribute).unwrap();
    }
    let before = Database::new(schema)
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("a".into()),
                    attribute: 1000,
                    value: TxValue::Scalar(Value::String("Alice".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("a".into()),
                    attribute: 1001,
                    value: TxValue::Scalar(Value::Long(30)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("b".into()),
                    attribute: 1000,
                    value: TxValue::Scalar(Value::String("Bob".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("a".into()),
                    attribute: 1002,
                    value: TxValue::Entity(EntityRef::Temp("b".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("b".into()),
                    attribute: 1002,
                    value: TxValue::Entity(EntityRef::Temp("a".into())),
                },
            ],
            1000,
        )
        .unwrap();
    let a = before.tempids["a"];
    let b = before.tempids["b"];
    let after = before
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(a),
                attribute: 1001,
                value: TxValue::Scalar(Value::Long(31)),
            }],
            2000,
        )
        .unwrap();
    (
        before.db_after.database_value(),
        after.db_after.database_value(),
        a,
        b,
    )
}

#[test]
fn query_edn_uses_the_supplied_current_history_asof_since_and_speculative_value() {
    let (before, after, a, _) = fixture();
    let preview = before
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(a),
                attribute: 1001,
                value: Value::Long(99).into(),
            }],
            3_000,
        )
        .unwrap()
        .db_after;
    let typed = Query::new(
        FindSpec::Relation(vec![f("?age")]),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::Constant(Value::Long(a as i64)),
            Term::Constant(Value::Keyword(Keyword::new("person", "age"))),
            v("?age"),
        )))],
    );
    let text = format!("[:find ?age :where [{a} :person/age ?age]]");
    for view in [
        before.clone(),
        after.clone(),
        after.clone().history(),
        after.clone().as_of(before.basis_t()),
        after.clone().since(before.basis_t()),
        preview,
        after.clone().filter(move |_, datom| datom.entity != a),
    ] {
        let source = EdnQueryArgument::Source(QuerySourceValue::Database(view.clone()));
        let result = execute(&text, &[source]);
        let expected = QueryEngine::execute_sources(
            &typed,
            &[atomic_core::QueryDataSource::database("$", view)],
            &[],
            &QueryControl::default(),
        )
        .unwrap()
        .result;
        assert_eq!(result, expected);
    }
    assert_eq!(
        execute(
            &text,
            &[EdnQueryArgument::Source(QuerySourceValue::Database(before))]
        ),
        QueryResult::Relation(vec![vec![long(30)]])
    );
    assert_eq!(
        execute(
            &text,
            &[EdnQueryArgument::Source(QuerySourceValue::Database(after))]
        ),
        QueryResult::Relation(vec![vec![long(31)]])
    );
}

#[test]
fn pull_edn_covers_options_reverse_nesting_recursion_wildcard_and_native_transforms() {
    let (before, _, a, b) = fixture();
    let mut name = PullAttribute::forward(AttributeName::Ident(Keyword::new("person", "name")));
    name.alias = Some(QueryValue::Scalar(Value::String("name".into())));
    let mut friends =
        PullAttribute::reverse(AttributeName::Ident(Keyword::new("person", "friend")));
    friends.limit = PullLimit::Unlimited;
    friends.nested = Some(PullNested::Pattern(Box::new(PullPattern {
        wildcard: false,
        attributes: vec![PullAttribute::forward(AttributeName::Ident(Keyword::new(
            "person", "name",
        )))],
    })));
    let expected = PullPattern {
        wildcard: true,
        attributes: vec![name, friends],
    };
    let parsed = parse_pull_edn(
        "[* [:person/name :as \"name\"] {[:person/_friend :limit nil] [:person/name]}]",
    )
    .unwrap();
    assert_eq!(parsed, expected);
    assert_eq!(
        before.pull(&parsed, a).unwrap(),
        before.pull(&expected, a).unwrap()
    );
    assert_eq!(
        parse_pull_edn("[(limit :person/friend 2) (default :person/age 99)]").unwrap(),
        parse_pull_edn("[[:person/friend :limit 2] [:person/age :default 99]]").unwrap()
    );
    for text in [
        "[:person/name {:person/friend ...}]",
        "[:person/name {:person/friend 3}]",
    ] {
        let pattern = parse_pull_edn(text).unwrap();
        assert!(matches!(
            before.pull(&pattern, a).unwrap(),
            QueryValue::Map(_)
        ));
    }
    let mut registry = EdnPullTransforms::new();
    registry.register(
        "native/inc",
        PullTransform::new("native/inc", |value| match value {
            QueryValue::Scalar(Value::Long(n)) => Ok(long(n + 1)),
            QueryValue::Nil => Ok(QueryValue::Nil),
            _ => panic!("fixture"),
        }),
    );
    let transformed =
        parse_pull_edn_with_transforms("[[:person/age :xform native/inc :default 100]]", &registry)
            .unwrap();
    let pulled = before.pull(&transformed, a).unwrap();
    assert_eq!(
        pulled,
        QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Keyword(Keyword::new("person", "age"))),
            long(31)
        )])
    );
    assert_eq!(
        before.pull(&transformed, b).unwrap(),
        QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Keyword(Keyword::new("person", "age"))),
            long(100)
        )])
    );
    assert!(parse_pull_edn("[[:person/age :xform arbitrary/run]]").is_err());
}

#[test]
fn query_pull_pattern_arguments_and_native_extensions_are_explicit() {
    let (before, _, a, _) = fixture();
    let args = [
        EdnQueryArgument::Source(QuerySourceValue::Database(before.clone())),
        data(&a.to_string()),
        data("[:person/name {:person/friend [:person/name]}]"),
    ];
    let bound = parse_query_edn("[:find (pull ?e pattern) . :in $ ?e pattern]")
        .unwrap()
        .bind(&args)
        .unwrap();
    let result = bound
        .execute(&QueryControl::default(), None)
        .unwrap()
        .result;
    assert_eq!(
        result,
        QueryResult::Scalar(Some(
            before
                .pull(
                    &parse_pull_edn("[:person/name {:person/friend [:person/name]}]").unwrap(),
                    a,
                )
                .unwrap()
        ))
    );
    let calls = std::sync::Arc::new(std::sync::atomic::AtomicUsize::new(0));
    let mut extensions = QueryExtensions::new();
    let count = calls.clone();
    extensions.register_local("native/even?", move |_, args, _| {
        count.fetch_add(1, std::sync::atomic::Ordering::Relaxed);
        let [Value::Long(n)] = args else {
            panic!("fixture")
        };
        Ok(vec![vec![Value::Bool(n % 2 == 0)]])
    });
    let bound = parse_query_edn("[:find [?n ...] :in $ [?n ...] :where [(native/even? ?n)]]")
        .unwrap()
        .bind(&[
            EdnQueryArgument::Source(QuerySourceValue::Database(before)),
            data("[1 2 3 4]"),
        ])
        .unwrap();
    assert_eq!(calls.load(std::sync::atomic::Ordering::Relaxed), 0);
    assert_eq!(
        bound
            .execute(&QueryControl::default(), Some(&extensions))
            .unwrap()
            .result,
        QueryResult::Collection(vec![long(2), long(4)])
    );
    assert_eq!(calls.load(std::sync::atomic::Ordering::Relaxed), 4);
}

#[test]
fn malformed_valid_edn_is_rejected_by_specific_adapter_errors_and_limits() {
    for text in ["{:find [?x] :wat []}", "[:find ?x :find ?y]", "[]"] {
        assert!(parse_query_edn(text).is_err(), "{text}");
    }
    for text in [
        "[:person/name :person/name]",
        "[[:person/name :limit -1]]",
        "[[:person/name :unknown 1]]",
        "[{:person/friend -1}]",
    ] {
        assert!(parse_pull_edn(text).is_err(), "{text}");
    }
    let value = read_edn("[{:person/friend [:person/name]}]").unwrap();
    assert_eq!(
        pull_pattern_from_edn_with_limits(
            &value,
            &EdnPullTransforms::new(),
            &EdnAdapterLimits {
                max_depth: 1,
                max_nodes: 100,
                ..EdnAdapterLimits::default()
            }
        )
        .unwrap_err()
        .code,
        "edn/adapter-limit"
    );
    let query = parse_query_edn("[:find ?x :in ?x]").unwrap();
    assert_eq!(query.bind(&[]).unwrap_err().code, "edn/query-input-arity");
    assert!(query.bind(&[data("{:not :scalar}")]).is_err());
}

#[test]
fn nil_inputs_keep_binding_shapes_and_prepared_execution_is_reusable() {
    for (text, args, expected) in [
        (
            "[:find ?x . :in ?x]",
            "nil",
            QueryResult::Scalar(Some(QueryValue::Nil)),
        ),
        (
            "[:find [?x ?y] :in [?x ?y]]",
            "[nil 2]",
            QueryResult::Tuple(Some(vec![QueryValue::Nil, long(2)])),
        ),
        (
            "[:find [?x ...] :in [?x ...]]",
            "[nil 2 nil]",
            QueryResult::Collection(vec![QueryValue::Nil, long(2)]),
        ),
        (
            "[:find ?x ?y :in [[?x ?y]]]",
            "[[nil 2] [1 nil]]",
            QueryResult::Relation(vec![
                vec![QueryValue::Nil, long(2)],
                vec![long(1), QueryValue::Nil],
            ]),
        ),
        (
            "[:find ?x . :in :where [(ground nil) ?x]]",
            "",
            QueryResult::Scalar(Some(QueryValue::Nil)),
        ),
    ] {
        let inputs = if args.is_empty() {
            vec![]
        } else {
            vec![data(args)]
        };
        let bound = parse_query_edn(text).unwrap().bind(&inputs).unwrap();
        for _ in 0..5 {
            assert_eq!(
                bound
                    .execute(&QueryControl::default(), None)
                    .unwrap()
                    .result,
                expected
            );
        }
    }
    assert_eq!(
        execute("[:find [?x ?y] :in ?x ?y]", &[data("nil"), data("3")]),
        QueryResult::Tuple(Some(vec![QueryValue::Nil, long(3)]))
    );
    let bound = parse_query_edn("[:find [?x ...] :in [?x ...]]")
        .unwrap()
        .bind(&[data("[1 2 3]")])
        .unwrap();
    for control in [
        QueryControl {
            max_work: 0,
            ..Default::default()
        },
        QueryControl {
            timeout: Some(std::time::Duration::ZERO),
            ..Default::default()
        },
    ] {
        assert!(bound.execute(&control, None).is_err());
    }
    assert_eq!(
        bound
            .execute(&QueryControl::default(), None)
            .unwrap()
            .result,
        QueryResult::Collection(vec![long(1), long(2), long(3)])
    );
}

#[test]
fn data_access_functions_and_nested_pattern_parameters_compile_to_native_semantics() {
    let (before, _, a, b) = fixture();
    assert_eq!(
        execute(
            "[:find [?age ?attr ?name ?x ?y] :in $ ?e :where [(get-else $ ?e :person/age 0) ?age] [(get-some $ ?e :person/age :person/name) [?attr ?name]] [(tuple ?age ?name) ?pair] [(untuple ?pair) [?x ?y]] [(missing? $ ?e :person/age)]]",
            &[
                EdnQueryArgument::Source(QuerySourceValue::Database(before.clone())),
                data(&b.to_string())
            ]
        ),
        QueryResult::Tuple(Some(vec![
            long(0),
            QueryValue::Scalar(Value::Ref(1000)),
            QueryValue::Scalar(Value::String("Bob".into())),
            long(0),
            QueryValue::Scalar(Value::String("Bob".into()))
        ]))
    );
    let bound = parse_query_edn("[:find ?result . :in $outer ?e pattern :where [(q [:find (pull $inner ?e selector) . :in $inner ?e selector] $outer ?e pattern) ?result]]").unwrap().bind(&[
        EdnQueryArgument::Source(QuerySourceValue::Database(before.clone())),data(&a.to_string()),data("[:person/name]")]).unwrap();
    assert_eq!(
        bound
            .execute(&QueryControl::default(), None)
            .unwrap()
            .result,
        QueryResult::Scalar(Some(
            before
                .pull(&parse_pull_edn("[:person/name]").unwrap(), a)
                .unwrap()
        ))
    );
}

#[test]
fn conversion_admits_total_inputs_and_repeated_pattern_expansion_before_cloning() {
    let query = parse_query_edn("[:find ?x :in ?x]").unwrap();
    assert_eq!(
        query
            .bind_with_limits(
                &[EdnQueryArgument::Data(EdnValue::String("x".repeat(1024)))],
                &EdnPullTransforms::new(),
                &EdnAdapterLimits {
                    max_bytes: 512,
                    ..Default::default()
                }
            )
            .unwrap_err()
            .code,
        "edn/adapter-limit"
    );
    let variables = (0..8).map(|i| format!("?e{i}")).collect::<Vec<_>>();
    let text = format!(
        "[:find {} :in $ [{}] pattern]",
        variables
            .iter()
            .map(|v| format!("(pull {v} pattern)"))
            .collect::<Vec<_>>()
            .join(" "),
        variables.join(" ")
    );
    let pattern = format!(
        "[{}]",
        (0..20)
            .map(|i| format!(":entity/a{i}"))
            .collect::<Vec<_>>()
            .join(" ")
    );
    let query = parse_query_edn(&text).unwrap();
    let args = [data("[]"), data("[1 2 3 4 5 6 7 8]"), data(&pattern)];
    // Input fits under100nodes; eight compiled pattern copies do not.
    assert_eq!(
        query
            .bind_with_limits(
                &args,
                &EdnPullTransforms::new(),
                &EdnAdapterLimits {
                    max_nodes: 100,
                    ..Default::default()
                }
            )
            .unwrap_err()
            .code,
        "edn/adapter-limit"
    );
    query.bind(&args).unwrap();
    let wide = EdnValue::Vector(vec![EdnValue::Nil; 1024]);
    assert_eq!(
        pull_pattern_from_edn_with_limits(
            &wide,
            &EdnPullTransforms::new(),
            &EdnAdapterLimits {
                max_nodes: 10,
                ..Default::default()
            }
        )
        .unwrap_err()
        .code,
        "edn/adapter-limit"
    );
}

#[test]
fn fulltext_and_log_edn_use_actual_postgres_sources_and_captured_basis() {
    use atomic_core::{
        Connection, PostgresIndexer, PostgresMigrator, PostgresStore, TransactionRequest,
    };
    use std::time::Duration;
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP EDN fulltext/log PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "edn_query_pull");
    let url = &fixture.connection;
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1000,
                Keyword::new("article", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    PostgresStore::connect(url)
        .unwrap()
        .create_database("edn", schema)
        .unwrap();
    let service = common::start_service(url, "edn");
    let connection = Connection::connect(url, "edn", 8).unwrap();
    let first = service
        .client()
        .transact(
            TransactionRequest::new(
                "one",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("a".into()),
                    attribute: 1000,
                    value: Value::String("blue river".into()).into(),
                }],
            ),
            Duration::from_secs(30),
        )
        .unwrap();
    let entity = first.tempids["a"];
    connection
        .sync_to(first.basis_t, Duration::from_secs(30))
        .unwrap();
    let captured_log = connection.log();
    let second = service
        .client()
        .transact(
            TransactionRequest::new(
                "two",
                vec![TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1000,
                    value: Value::String("green meadow".into()).into(),
                }],
            ),
            Duration::from_secs(30),
        )
        .unwrap();
    connection
        .sync_to(second.basis_t, Duration::from_secs(30))
        .unwrap();
    let mut indexer = PostgresIndexer::connect(url, "edn").unwrap();
    indexer.consolidate().unwrap();
    indexer.rebuild_fulltext().unwrap();
    connection
        .sync_index(second.basis_t, Duration::from_secs(30))
        .unwrap();
    let current = connection.db();
    for (view, search, expected) in [
        (current.clone(), "meadow", 1),
        (current.clone(), "river", 0),
        (current.clone().history(), "river", 1),
        (current.clone().as_of(first.basis_t), "river", 1),
    ] {
        let query = "[:find ?e ?text ?tx ?score :in $ ?search :where [(fulltext $ :article/text ?search) [[?e ?text ?tx ?score]]]]";
        let result = execute(
            query,
            &[
                EdnQueryArgument::Source(QuerySourceValue::Database(view.clone())),
                EdnQueryArgument::Data(EdnValue::String(search.into())),
            ],
        );
        let QueryResult::Relation(rows) = result else {
            panic!("relation")
        };
        assert_eq!(rows.len(), expected);
        for row in rows {
            assert_eq!(row[0], QueryValue::Scalar(Value::Ref(entity)));
        }
        assert_eq!(
            view.fulltext(1000, search, &Default::default())
                .unwrap()
                .hits
                .len(),
            expected
        );
    }
    assert_eq!(
        execute(
            "[:find [?text ...] :in $log :where [(tx-ids $log 0 nil) [?tx ...]] [(tx-data $log ?tx) [[?e ?a ?text ?tx ?added]]] [(= ?a 1000)] [(= ?added true)]]",
            &[EdnQueryArgument::Source(QuerySourceValue::Log(
                captured_log
            ))]
        ),
        QueryResult::Collection(vec![QueryValue::Scalar(Value::String("blue river".into()))])
    );
}

#[test]
fn measured_edn_query_complete_path_scales_with_input_not_decimal_exponent() {
    use std::time::Instant;
    let query_text = "[:find ?e ?value :in [[?e ?value]] ?needle :where [(= ?value ?needle)]]";
    for count in [32, 128, 512] {
        for scale in [0, 1_000, 100_000] {
            let needle = format!("7E-{scale}M");
            let input = format!(
                "[{}]",
                (0..count)
                    .map(|e| format!("[{e} {needle}]"))
                    .collect::<Vec<_>>()
                    .join(" ")
            );
            let complete = Instant::now();
            let query = parse_query_edn(query_text).unwrap();
            let args = [data(&input), data(&needle)];
            let read = complete.elapsed();
            let at = Instant::now();
            let bound = query.bind(&args).unwrap();
            let bind = at.elapsed();
            let at = Instant::now();
            let outcome = bound.execute(&QueryControl::default(), None).unwrap();
            let QueryResult::Relation(rows) = &outcome.result else {
                panic!("relation")
            };
            assert_eq!(rows.len(), count);
            assert!(
                rows.iter()
                    .enumerate()
                    .all(|(index, row)| row[0] == long(index as i64))
            );
            let rendered =
                atomic_core::edn::write_edn(&bound.result_to_edn(&outcome.result).unwrap())
                    .unwrap();
            let bytes = rendered.len();
            let work = outcome.stats.work;
            drop((rendered, outcome, bound, args, query));
            eprintln!(
                "EDN query rows={count} scale={scale} input_bytes={} output_bytes={bytes} read={read:?} bind={bind:?} execute_print_drop={:?} complete={:?} native_work={work}",
                input.len(),
                at.elapsed(),
                complete.elapsed()
            );
        }
    }
}

#[test]
fn short_raw_relations_match_only_referenced_columns_in_scan_and_hash_paths() {
    // More than10rows selects the native hash join when a key is bound. Both
    // short and long rows participate; no invented filler value may escape.
    let one = format!(
        "[{}]",
        (0..32)
            .map(|i| format!("[{i}]"))
            .collect::<Vec<_>>()
            .join(" ")
    );
    let two = format!(
        "[{}]",
        (0..32)
            .map(|i| format!("[{i} \"v{i}\"]"))
            .collect::<Vec<_>>()
            .join(" ")
    );
    for (pattern, source, expected, hashed) in [
        (
            "[$raw ?key]",
            one.as_str(),
            QueryResult::Scalar(Some(long(7))),
            true,
        ),
        (
            "[$raw ?key _ _]",
            one.as_str(),
            QueryResult::Scalar(Some(long(7))),
            true,
        ),
        (
            "[$raw ?key ?value]",
            two.as_str(),
            QueryResult::Scalar(Some(QueryValue::Scalar(Value::String("v7".into())))),
            true,
        ),
        (
            "[$raw ?key ?value _ _ _]",
            two.as_str(),
            QueryResult::Scalar(Some(QueryValue::Scalar(Value::String("v7".into())))),
            true,
        ),
        (
            "[$raw ?key _ ?value]",
            two.as_str(),
            QueryResult::Scalar(None),
            false,
        ),
        (
            "[$raw ?key ?value]",
            one.as_str(),
            QueryResult::Scalar(None),
            false,
        ),
    ] {
        let find = if pattern.contains("?value") {
            "?value"
        } else {
            "?key"
        };
        let query =
            parse_query_edn(&format!("[:find {find} . :in $raw ?key :where {pattern}]")).unwrap();
        let bound = query.bind(&[data(source), data("7")]).unwrap();
        for force_scan in [false, true] {
            let outcome = bound
                .execute(
                    &QueryControl {
                        force_scan,
                        ..Default::default()
                    },
                    None,
                )
                .unwrap();
            assert_eq!(
                outcome.result, expected,
                "{pattern} force_scan={force_scan}"
            );
            if !force_scan && hashed {
                assert!(outcome.stats.hash_join_build_rows >= 32);
            }
            if force_scan {
                assert_eq!(outcome.stats.hash_join_build_rows, 0);
            }
        }
    }
    let (before, after, _, _) = fixture();
    assert_eq!(
        execute(
            "[:find ?before ?after ?extra :in $then $now $extra :where [$then ?e :person/age ?before] [$now ?e :person/age ?after] [$now ?e :person/name ?name] [$extra ?name ?extra]]",
            &[
                EdnQueryArgument::Source(QuerySourceValue::Database(before)),
                EdnQueryArgument::Source(QuerySourceValue::Database(after)),
                data("[[\"Alice\" \"external\"]]")
            ]
        ),
        QueryResult::Relation(vec![vec![
            long(30),
            long(31),
            QueryValue::Scalar(Value::String("external".into()))
        ]])
    );
}
