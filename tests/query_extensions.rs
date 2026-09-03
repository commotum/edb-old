use atomic_core::{
    Attribute, Binding, Cardinality, Clause, EntityRef, ErrorCategory, FindElement, FindSpec,
    Function, InputSpec, Instruction, Keyword, PostgresStore, Program, ProgramKind, Query,
    QueryControl, QueryExtensions, QueryInput, QueryResult, QueryValue, Schema, Term, TxOp,
    TxValue, Value, ValueType, Variable,
};
use std::sync::Arc;
use std::time::{SystemTime, UNIX_EPOCH};

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
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
        .install(Attribute::new(
            10,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn extension_query(name: &str, binding: Binding, find: FindSpec) -> Query {
    Query {
        find,
        with: Vec::new(),
        inputs: vec![InputSpec::Scalar(Variable::from("x"))],
        clauses: vec![Clause::Function {
            function: Function::Extension(name.into()),
            source: "$".into(),
            args: vec![Term::var("x")],
            binding,
        }],
        rules: Vec::new(),
    }
}

#[test]
fn local_extensions_support_shapes_limits_errors_and_panic_containment() {
    let db = atomic_core::Database::new(schema()).unwrap();
    let mut extensions = QueryExtensions::new();
    extensions.register_local("double", |_, args, _| match args {
        [Value::Long(value)] => Ok(vec![vec![Value::Long(value * 2)]]),
        _ => unreachable!(),
    });
    extensions.register_local("pair", |_, args, _| {
        Ok(vec![
            vec![args[0].clone(), Value::Long(1)],
            vec![args[0].clone(), Value::Long(2)],
        ])
    });
    extensions.register_local("panic", |_, _, _| panic!("contained"));

    let scalar = extension_query(
        "double",
        Binding::Scalar(Variable::from("y")),
        FindSpec::Scalar(FindElement::Variable(Variable::from("y"))),
    );
    assert_eq!(
        db.query_with_extensions(
            &scalar,
            &[QueryInput::Scalar(Value::Long(4))],
            &QueryControl::default(),
            &extensions,
        )
        .unwrap()
        .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Long(8))))
    );

    let relation = extension_query(
        "pair",
        Binding::Relation(vec![Some(Variable::from("a")), Some(Variable::from("b"))]),
        FindSpec::Relation(vec![
            FindElement::Variable(Variable::from("a")),
            FindElement::Variable(Variable::from("b")),
        ]),
    );
    let outcome = db
        .query_with_extensions(
            &relation,
            &[QueryInput::Scalar(Value::Long(9))],
            &QueryControl::default(),
            &extensions,
        )
        .unwrap();
    assert!(matches!(outcome.result, QueryResult::Relation(rows) if rows.len() == 2));
    assert!(outcome.plan[0].access.contains("local extension pair"));

    let limited = QueryControl {
        max_result_rows: 1,
        ..QueryControl::default()
    };
    assert_eq!(
        db.query_with_extensions(
            &relation,
            &[QueryInput::Scalar(Value::Long(9))],
            &limited,
            &extensions,
        )
        .unwrap_err()
        .code,
        "query/extension-output-limit"
    );
    let panic_query = extension_query(
        "panic",
        Binding::Scalar(Variable::from("y")),
        FindSpec::Scalar(FindElement::Variable(Variable::from("y"))),
    );
    let error = db
        .query_with_extensions(
            &panic_query,
            &[QueryInput::Scalar(Value::Long(1))],
            &QueryControl::default(),
            &extensions,
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Fault, "query/local-extension-panicked")
    );
}

#[test]
fn persisted_query_program_is_exact_snapshot_local_and_cancelled_by_parent() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("query_program");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    store.create_database(&database_id, schema()).unwrap();
    let committed = store
        .transact(
            &database_id,
            "initial",
            0,
            &[TxOp::Add {
                entity: EntityRef::Id(1_000),
                attribute: 10,
                value: TxValue::Scalar(Value::Long(5)),
            }],
            1_000,
        )
        .unwrap();
    let program = Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(10),
            Instruction::PushConstant(Value::String("old".into())),
            Instruction::EmitRow(2),
            Instruction::PushConstant(Value::Long(6)),
            Instruction::PushConstant(Value::String("literal".into())),
            Instruction::EmitRow(2),
            Instruction::Return,
        ],
    };
    let hash = store
        .deploy_program(&database_id, "snapshot", 1, &program)
        .unwrap();
    store
        .activate_program(&database_id, "snapshot", None, 1)
        .unwrap();
    drop(store);

    let mut restarted = PostgresStore::connect(&connection).unwrap();
    let (_, resolved_hash, resolved) = restarted
        .resolve_active_program(&database_id, "snapshot")
        .unwrap();
    assert_eq!(resolved_hash, hash);
    let old = Arc::new(committed.database);
    let mut extensions = QueryExtensions::new();
    extensions
        .register_program("snapshot", hash, resolved)
        .unwrap();
    let query = extension_query(
        "snapshot",
        Binding::Relation(vec![
            Some(Variable::from("value")),
            Some(Variable::from("tag")),
        ]),
        FindSpec::Relation(vec![
            FindElement::Variable(Variable::from("value")),
            FindElement::Variable(Variable::from("tag")),
        ]),
    );
    let query_thread = {
        let old = old.clone();
        let extensions = extensions.clone();
        let query = query.clone();
        std::thread::spawn(move || {
            old.query_with_extensions(
                &query,
                &[QueryInput::Scalar(Value::Ref(1_000))],
                &QueryControl::default(),
                &extensions,
            )
            .unwrap()
        })
    };
    restarted
        .transact(
            &database_id,
            "advance",
            1,
            &[TxOp::Add {
                entity: EntityRef::Id(1_000),
                attribute: 10,
                value: TxValue::Scalar(Value::Long(99)),
            }],
            2_000,
        )
        .unwrap();
    let outcome = query_thread.join().unwrap();
    assert!(matches!(
        outcome.result,
        QueryResult::Relation(rows)
            if rows.iter().any(|row| row[0] == QueryValue::Scalar(Value::Long(5)))
                && !rows.iter().any(|row| row[0] == QueryValue::Scalar(Value::Long(99)))
    ));
    assert!(
        outcome.plan[0]
            .access
            .contains(&hash.iter().map(|b| format!("{b:02x}")).collect::<String>())
    );

    let cancelled = QueryControl::default();
    cancelled
        .cancel
        .store(true, std::sync::atomic::Ordering::Relaxed);
    let error = old
        .query_with_extensions(
            &query,
            &[QueryInput::Scalar(Value::Ref(1_000))],
            &cancelled,
            &extensions,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Interrupted);
}
