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
        assert_eq!(bound.query, typed);
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
        EdnValue::Vector(vec![])
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
