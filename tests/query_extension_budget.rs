//! Persisted read-side extensions participate in the enclosing query's work,
//! deadline, and cancellation; each sibling invocation gets only what remains.
mod common;

use atomic_core::{
    Attribute, Binding, Cardinality, Clause, Database, EntityRef, FindElement, FindSpec, Function,
    InputSpec, Instruction, Keyword, Program, ProgramBudget, ProgramControl, ProgramKind,
    ProgramRuntime, Query, QueryControl, QueryExtensions, QueryInput, Schema, Term,
    TransactionRequest, TxOp, Value, ValueType, Variable,
};
use std::sync::{
    Arc, Mutex,
    atomic::{AtomicUsize, Ordering},
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const NUMBER: u32 = 1_000;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            NUMBER,
            Keyword::new("item", "number"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn database() -> (Database, u64) {
    let report = Database::new(schema())
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("root".into()),
                attribute: NUMBER,
                value: Value::Long(42).into(),
            }],
            1,
        )
        .unwrap();
    (report.db_after, report.tempids["root"])
}

fn extension_query(name: &str) -> Query {
    let mut query = Query::new(
        FindSpec::Collection(FindElement::Variable(Variable::from("result"))),
        vec![Clause::Function {
            function: Function::Extension(name.into()),
            source: "$".into(),
            args: vec![Term::var("input")],
            binding: Binding::Scalar(Variable::from("result")),
        }],
    );
    query.inputs = vec![InputSpec::Collection(Variable::from("input"))];
    query
}

fn inputs() -> Vec<QueryInput> {
    vec![QueryInput::Collection(vec![
        Value::Long(1),
        Value::Long(2),
        Value::Long(3),
    ])]
}

fn program() -> Program {
    Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::PushConstant(Value::String("charged work".repeat(32))),
            Instruction::Pop,
            Instruction::PushArgument(0),
            Instruction::EmitRow(1),
            Instruction::Return,
        ],
    }
}

#[test]
fn persisted_extensions_charge_actual_fuel_and_do_not_reset_sibling_work() {
    let (database, _) = database();
    let program = program();
    let mut budget = ProgramBudget::new(ProgramControl::default()).unwrap();
    let initial = budget.remaining_fuel();
    ProgramRuntime
        .execute_query_with_budget(
            &program,
            &database.database_value(),
            &[Value::Long(1)],
            &mut budget,
        )
        .unwrap();
    let cost = initial - budget.remaining_fuel();
    assert!(cost > 100);
    let mut shared = ProgramBudget::new(ProgramControl {
        fuel: cost * 2,
        ..ProgramControl::default()
    })
    .unwrap();
    for _ in 0..2 {
        ProgramRuntime
            .execute_query_with_budget(
                &program,
                &database.database_value(),
                &[Value::Long(1)],
                &mut shared,
            )
            .unwrap();
    }
    assert_eq!(shared.remaining_fuel(), 0);
    assert_eq!(
        ProgramRuntime
            .execute_query_with_budget(
                &program,
                &database.database_value(),
                &[Value::Long(1)],
                &mut shared
            )
            .unwrap_err()
            .code,
        "program/fuel-exhausted"
    );

    let mut extensions = QueryExtensions::new();
    extensions.register_local("local", |_, args, _| Ok(vec![vec![args[0].clone()]]));
    extensions
        .register_program(
            "stored",
            atomic_core::program_hash(&program).unwrap(),
            program,
        )
        .unwrap();
    let local = database
        .query_with_extensions(
            &extension_query("local"),
            &inputs(),
            &QueryControl::default(),
            &extensions,
        )
        .unwrap();
    let stored = database
        .query_with_extensions(
            &extension_query("stored"),
            &inputs(),
            &QueryControl::default(),
            &extensions,
        )
        .unwrap();
    assert_eq!(stored.result, local.result);
    assert_eq!(stored.stats.work - local.stats.work, cost * 3);
    let control = QueryControl {
        max_work: (local.stats.work + cost * 2) as usize,
        ..QueryControl::default()
    };
    assert_eq!(
        database
            .query_with_extensions(&extension_query("stored"), &inputs(), &control, &extensions)
            .unwrap_err()
            .code,
        "query/work-limit"
    );
    let exact = QueryControl {
        max_work: stored.stats.work as usize,
        ..QueryControl::default()
    };
    assert_eq!(
        database
            .query_with_extensions(&extension_query("stored"), &inputs(), &exact, &extensions)
            .unwrap()
            .result,
        stored.result
    );
}

fn repeated_read_program(root: u64) -> Program {
    let mut body = vec![
        Instruction::PushEntity(EntityRef::Id(root)),
        Instruction::LoadOne(NUMBER),
        Instruction::Pop,
    ];
    // 4^5 = 1024 reads without the deadline/cancellation bridge.
    for _ in 0..5 {
        let mut outer = vec![Instruction::PushNull; 4];
        outer.push(Instruction::MakeVector(4));
        let mut iteration = vec![Instruction::Pop];
        iteration.extend(body);
        outer.push(Instruction::ForEach { body: iteration });
        body = outer;
    }
    body.extend([
        Instruction::PushArgument(0),
        Instruction::EmitRow(1),
        Instruction::Return,
    ]);
    Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: body,
    }
}

#[test]
fn query_deadline_interrupts_persisted_interpreter_before_program_finishes() {
    let (database, root) = database();
    let reads = Arc::new(AtomicUsize::new(0));
    let observed = reads.clone();
    let filtered = database.database_value().filter(move |_, datom| {
        if datom.attribute == NUMBER {
            observed.fetch_add(1, Ordering::Relaxed);
            std::thread::sleep(Duration::from_millis(1));
        }
        true
    });
    let program = repeated_read_program(root);
    let mut extensions = QueryExtensions::new();
    extensions
        .register_program(
            "read",
            atomic_core::program_hash(&program).unwrap(),
            program,
        )
        .unwrap();
    let control = QueryControl {
        timeout: Some(Duration::from_millis(100)),
        ..QueryControl::default()
    };
    let error = filtered
        .query_with_extensions(
            &extension_query("read"),
            &[QueryInput::Collection(vec![Value::Long(1)])],
            &control,
            &extensions,
        )
        .unwrap_err();
    assert_eq!(error.code, "query/timeout");
    let reads = reads.load(Ordering::Relaxed);
    assert!(
        reads > 0 && reads < 1_024,
        "interpreter must stop before all 1024 reads; observed {reads}"
    );
}

#[test]
fn persisted_interpreter_preserves_parent_cancellation() {
    let (database, root) = database();
    let control = QueryControl::default();
    let cancel = control.cancel.clone();
    let reads = Arc::new(AtomicUsize::new(0));
    let observed = reads.clone();
    let filtered = database.database_value().filter(move |_, datom| {
        if datom.attribute == NUMBER && observed.fetch_add(1, Ordering::Relaxed) >= 4 {
            cancel.store(true, Ordering::Relaxed);
        }
        true
    });
    let program = repeated_read_program(root);
    let mut extensions = QueryExtensions::new();
    extensions
        .register_program(
            "read",
            atomic_core::program_hash(&program).unwrap(),
            program,
        )
        .unwrap();
    assert_eq!(
        filtered
            .query_with_extensions(
                &extension_query("read"),
                &[QueryInput::Collection(vec![Value::Long(1)])],
                &control,
                &extensions
            )
            .unwrap_err()
            .code,
        "query/canceled"
    );
    assert_eq!(reads.load(Ordering::Relaxed), 5);
}

#[test]
fn native_callbacks_receive_remaining_control_and_are_checked_after_return() {
    let (database, _) = database();
    let observations = Arc::new(Mutex::new(Vec::new()));
    let seen = observations.clone();
    let mut extensions = QueryExtensions::new();
    extensions.register_local("observe", move |_, args, control| {
        seen.lock()
            .unwrap()
            .push((control.max_work, control.timeout.unwrap()));
        std::thread::sleep(Duration::from_millis(2));
        Ok(vec![vec![args[0].clone()]])
    });
    let control = QueryControl {
        max_work: 10_000,
        timeout: Some(Duration::from_secs(10)),
        ..QueryControl::default()
    };
    database
        .query_with_extensions(
            &extension_query("observe"),
            &inputs(),
            &control,
            &extensions,
        )
        .unwrap();
    let observations = observations.lock().unwrap();
    assert_eq!(observations.len(), 3);
    assert!(observations[0].0 < control.max_work && observations[0].1 < control.timeout.unwrap());
    for pair in observations.windows(2) {
        assert!(pair[1].0 < pair[0].0 && pair[1].1 < pair[0].1);
    }
    drop(observations);
    let cancel = control.cancel.clone();
    extensions.register_local("cancel", move |_, args, _| {
        cancel.store(true, Ordering::Relaxed);
        Ok(vec![vec![args[0].clone()]])
    });
    assert_eq!(
        database
            .query_with_extensions(&extension_query("cancel"), &inputs(), &control, &extensions)
            .unwrap_err()
            .code,
        "query/canceled"
    );
}

#[test]
fn native_postgres_deployed_extensions_share_query_work_at_captured_basis() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return;
    };
    let id = format!(
        "query-extension-budget-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    atomic_core::PostgresMigrator::connect(&postgres)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = atomic_core::PostgresStore::connect(&postgres).unwrap();
    store.create_database(&id, schema()).unwrap();
    let writer = common::start_service(&postgres, &id);
    let report = writer
        .client()
        .transact(
            TransactionRequest::new(
                "initial",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("root".into()),
                    attribute: NUMBER,
                    value: Value::Long(42).into(),
                }],
            )
            .with_tx_instant(1),
            Duration::from_secs(30),
        )
        .unwrap();
    let root = report.tempids["root"];
    let mut program = program();
    program.instructions.splice(
        0..0,
        [
            Instruction::PushEntity(EntityRef::Id(root)),
            Instruction::LoadOne(NUMBER),
            Instruction::PushConstant(Value::Long(42)),
            Instruction::Equal,
            Instruction::Require {
                category: atomic_core::ErrorCategory::Incorrect,
                message: "exact captured basis required".into(),
            },
        ],
    );
    let hash = store.deploy_program_blob(&program).unwrap();
    drop(store);
    let resolved = atomic_core::PostgresStore::connect(&postgres)
        .unwrap()
        .resolve_program(hash)
        .unwrap();
    let peer = atomic_core::Connection::connect(&postgres, &id, 16).unwrap();
    let captured = peer.db();
    writer
        .client()
        .transact(
            TransactionRequest::new(
                "later",
                vec![TxOp::Add {
                    entity: EntityRef::Id(root),
                    attribute: NUMBER,
                    value: Value::Long(43).into(),
                }],
            )
            .with_tx_instant(2),
            Duration::from_secs(30),
        )
        .unwrap();
    writer.shutdown();
    let mut extensions = QueryExtensions::new();
    extensions
        .register_program("stored", hash, resolved)
        .unwrap();
    let query = extension_query("stored");
    let outcome = captured
        .query_with_extensions(&query, &inputs(), &QueryControl::default(), &extensions)
        .unwrap();
    assert!(outcome.stats.work > 1_000);
    let control = QueryControl {
        max_work: outcome.stats.work as usize / 2,
        ..QueryControl::default()
    };
    assert_eq!(
        captured
            .query_with_extensions(&query, &inputs(), &control, &extensions)
            .unwrap_err()
            .code,
        "query/work-limit"
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
}
