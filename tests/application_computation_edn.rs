use atomic_core::edn::{EdnValue, read_edn};
use atomic_core::edn_query::{EdnQueryArgument, parse_query_edn};
use atomic_core::*;
mod common;

fn data(text: &str) -> EdnQueryArgument {
    EdnQueryArgument::Data(read_edn(text).unwrap())
}

fn run(query: &str, inputs: &[EdnQueryArgument], extensions: Option<&QueryExtensions>) -> EdnValue {
    let query = parse_query_edn(query).unwrap().bind(inputs).unwrap();
    let result = query.execute(&QueryControl::default(), extensions).unwrap();
    query.result_to_edn(&result.result).unwrap()
}

fn weighted_registry() -> QueryExtensions {
    let mut extensions = QueryExtensions::new();
    extensions.register_aggregate("app/weighted", |group, _| {
        let Some(QueryValue::Scalar(Value::Long(scale))) = group.constant(2) else {
            return Err(SemanticError::incorrect(
                "app/scale",
                "expected integer scale",
            ));
        };
        let mut result = 0i64;
        for row in 0..group.len() {
            group.check(1)?;
            let long = |index| match group.value(row, index) {
                Some(AggregateValue::Stored(Value::Long(value)))
                | Some(AggregateValue::Query(QueryValue::Scalar(Value::Long(value)))) => Ok(*value),
                _ => Err(SemanticError::incorrect(
                    "app/weight",
                    "expected integer amount/weight",
                )),
            };
            result += long(0)? * long(1)? * scale;
        }
        Ok(QueryValue::Scalar(Value::Long(result)))
    });
    extensions
}

#[test]
fn edn_custom_aggregate_keeps_aligned_arguments_grouping_and_with_bags() {
    let registry = weighted_registry();
    let rows = data(r#"[["team" 3 2 :a] ["team" 3 2 :b] ["team" 4 5 :c] ["other" 2 7 :d]]"#);
    let query = r#"[:find ?team (app/weighted ?hours ?weight 10) :with ?ticket
                    :in $rows :where [$rows ?team ?hours ?weight ?ticket]]"#;
    assert_eq!(
        run(query, std::slice::from_ref(&rows), Some(&registry)),
        read_edn(r#"#{["team" 320] ["other" 140]}"#).unwrap()
    );
    let query = r#"[:find ?team (app/weighted ?hours ?weight 10)
                    :in $rows :where [$rows ?team ?hours ?weight ?ticket]]"#;
    assert_eq!(
        run(query, &[rows], Some(&registry)),
        read_edn(r#"#{["team" 260] ["other" 140]}"#).unwrap()
    );
    assert_eq!(
        run(
            "[:find (app/weighted ?hours ?weight 10) . :in [[?hours ?weight]]]",
            &[data("[]")],
            Some(&registry)
        ),
        EdnValue::Long(0)
    );
}

#[test]
fn edn_standard_data_functions_work_without_a_database_or_jvm() {
    let query = r#"[:find ?prefix ?length ?minutes ?label
        :in [?name ...]
        :where [(clojure.string/starts-with? ?name "a")]
               [(includes? ?name "🦀")]
               [(ends-with? ?name "界")]
               [(subs ?name 1 2) ?prefix]
               [(clojure.core/count ?name) ?length]
               [(quot -125000 60000) ?minutes]
               [(str ?prefix ":" ?minutes nil) ?label]]"#;
    assert_eq!(
        run(query, &[data(r#"["a🦀界" "other"]"#)], None),
        read_edn(r#"#{["🦀" 3 -2 "🦀:-2"]}"#).unwrap()
    );
    for (value, count) in [
        ("nil", 0),
        ("#{1 2}", 2),
        ("{:a 1 :b 2}", 2),
        ("[1 nil 2]", 3),
    ] {
        assert_eq!(
            run(
                "[:find ?n . :in ?value :where [(count ?value) ?n]]",
                &[data(value)],
                None
            ),
            EdnValue::Long(count)
        );
    }
}

fn count_program(function: Function, sources: Vec<QueryTemplateSource>) -> Program {
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("out".into())]),
        vec![Clause::Function {
            function,
            source: "$".into(),
            args: vec![Term::Variable("input".into())],
            binding: Binding::Scalar("out".into()),
        }],
    );
    query.inputs = vec![InputSpec::Scalar("input".into())];
    Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::Query(QueryTemplate::native(query, vec![0], sources).unwrap()),
            Instruction::ForEach {
                body: vec![Instruction::Unpack(1), Instruction::EmitRow(1)],
            },
            Instruction::Return,
        ],
    }
}

#[test]
fn portable_function_programs_are_additively_versioned_and_match_direct_edn() {
    let program = count_program(Function::Count, vec![]);
    let bytes = encode_program(&program).unwrap();
    assert_eq!(&bytes[16..18], &11u16.to_be_bytes());
    assert_eq!(&bytes[25..27], &4u16.to_be_bytes());
    let decoded = decode_program(&bytes).unwrap();
    assert_eq!(encode_program(&decoded).unwrap(), bytes);
    let database = Database::new(Schema::new()).unwrap().database_value();
    let output = ProgramRuntime
        .execute_query(
            &decoded,
            &database,
            &[Value::String("a🦀界".into())],
            ProgramControl::default(),
        )
        .unwrap();
    assert!(matches!(output, ProgramOutput::Query(rows) if rows == vec![vec![Value::Long(3)]]));
    // A general relation source must not demote an otherwise newer template.
    let wide = count_program(
        Function::Count,
        vec![QueryTemplateSource::relation("$rows", 0)],
    );
    assert_eq!(&encode_program(&wide).unwrap()[25..27], &4u16.to_be_bytes());
    let legacy = count_program(Function::Tuple, vec![]);
    let legacy_bytes = encode_program(&legacy).unwrap();
    assert_eq!(&legacy_bytes[16..18], &7u16.to_be_bytes());
    assert_eq!(&legacy_bytes[25..27], &2u16.to_be_bytes());
    assert_eq!(
        encode_program(&decode_program(&legacy_bytes).unwrap()).unwrap(),
        legacy_bytes
    );
}

#[test]
fn native_aggregate_is_not_misrepresented_as_persisted_code() {
    let bound = parse_query_edn("[:find (app/weighted ?x ?w 10) . :in [[?x ?w]]]")
        .unwrap()
        .bind(&[data("[[1 2]]")])
        .unwrap();
    assert_eq!(
        QueryTemplate::native(bound.query().clone(), vec![0], vec![])
            .unwrap_err()
            .code,
        "program/nonportable-query"
    );
    assert!(bound.execute(&QueryControl::default(), None).is_err());
    assert_eq!(
        run(
            "[:find (clojure.core/count ?x) . :in [?x ...]]",
            &[data("[1 2]")],
            None
        ),
        EdnValue::Long(2)
    );
}

#[test]
fn namespace_aliases_do_not_steal_explicit_extension_names() {
    let mut extensions = QueryExtensions::new();
    extensions.register_pure("clojure.string/count", |_, _| {
        Ok(QueryValue::Scalar(Value::Long(91)))
    });
    extensions.register_pure("clojure.core/includes?", |_, _| {
        Ok(QueryValue::Scalar(Value::Bool(true)))
    });
    assert_eq!(
        run(
            "[:find ?n . :in :where [(clojure.string/count nil) ?n]]",
            &[],
            Some(&extensions)
        ),
        EdnValue::Long(91)
    );
    assert_eq!(
        run(
            "[:find ?result . :in :where [(clojure.core/includes? 123) ?result]]",
            &[],
            Some(&extensions)
        ),
        EdnValue::Bool(true)
    );
}

#[test]
fn portable_helper_program_is_durable_and_invokes_after_reopen() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual portable program: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "portable_function_program");
    common::install(&fixture.connection).unwrap();
    let mut store = common::TestStore::connect(&fixture.connection).unwrap();
    store
        .create_database("portable-functions", Schema::new())
        .unwrap();
    let program = count_program(Function::Count, vec![]);
    let bytes = encode_program(&program).unwrap();
    let hash = store.deploy_program_blob(&program).unwrap();
    drop(store);
    let service = common::start_service(&fixture.connection, "portable-functions");
    let report = service
        .client()
        .transact(
            TransactionRequest::new(
                "install",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("function".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("app", "count")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("function".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(hash).into(),
                    },
                ],
            ),
            std::time::Duration::from_secs(30),
        )
        .unwrap();
    let basis = report.db_after.basis_t();
    service.shutdown();
    let started = std::time::Instant::now();
    let connection = Connection::connect(&fixture.connection, "portable-functions", 8).unwrap();
    let database = connection.db();
    assert_eq!(database.basis_t(), basis);
    let resolved = common::TestStore::connect(&fixture.connection)
        .unwrap()
        .resolve_program(hash)
        .unwrap();
    assert_eq!(encode_program(&resolved).unwrap(), bytes);
    let result = database
        .invoke(
            Keyword::new("app", "count"),
            &[RuntimeValue::Scalar(Value::String("a🦀界".into()))],
            Default::default(),
        )
        .unwrap();
    assert!(matches!(result, ProgramOutput::Query(rows) if rows == vec![vec![Value::Long(3)]]));
    drop(database);
    drop(connection);
    println!(
        "PORTABLE_PROGRAM_OK abi=11 canonical_bytes=true reopen_resolve_execute_check_drop_us={}",
        started.elapsed().as_micros()
    );
}
