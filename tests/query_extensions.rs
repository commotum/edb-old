use atomic_core::{
    Attribute, Binding, Cardinality, Clause, EntityRef, ErrorCategory, FindElement, FindSpec,
    Function, InputSpec, Instruction, Keyword, PostgresStore, Program, ProgramKind, Query,
    QueryControl, QueryExtensions, QueryInput, QueryResult, QueryValue, Schema, Term, TxOp,
    TxValue, USER_PARTITION, Value, ValueType, Variable, make_eid,
};
use std::sync::Arc;
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

const ITEM_COUNT: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
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
            ITEM_COUNT,
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
    let fixture = common::PostgresFixture::new(&connection, "query_extensions");
    let connection = fixture.connection.clone();
    let database_id = unique("query_program");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let committed = common::transact(
        &service,
        "initial",
        created.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(5)),
        }],
        1_000,
    );
    let program = Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(ITEM_COUNT),
            Instruction::PushConstant(Value::String("old".into())),
            Instruction::EmitRow(2),
            Instruction::PushConstant(Value::Long(6)),
            Instruction::PushConstant(Value::String("literal".into())),
            Instruction::EmitRow(2),
            Instruction::Return,
        ],
    };
    let hash = store.deploy_program_blob(&program).unwrap();
    drop(store);

    let mut restarted = PostgresStore::connect(&connection).unwrap();
    let resolved = restarted.resolve_program(hash).unwrap();
    let old = Arc::new(committed.db_after);
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
                &[QueryInput::Scalar(Value::Ref(user(42)))],
                &QueryControl::default(),
                &extensions,
            )
            .unwrap()
        })
    };
    common::transact(
        &service,
        "advance",
        committed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(99)),
        }],
        2_000,
    );
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
            &[QueryInput::Scalar(Value::Ref(user(42)))],
            &cancelled,
            &extensions,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Interrupted);
    service.shutdown();
}
