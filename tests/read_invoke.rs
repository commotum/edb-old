mod common;

use atomic_core::{
    Attribute, AttributeName, CallableRef, Cardinality, Connection, DB_FN, DB_IDENT, Database,
    EntityIdentifier, EntityRef, ErrorCategory, Instruction, InvokeControl, InvokeRole, Keyword,
    PostgresConnectionConfig, PostgresMigrator, PostgresStore, Program, ProgramControl,
    ProgramKind, ProgramOutput, QueryPattern, QueryTemplate, QueryTerm, RuntimeValue, Schema,
    TransactionRequest, TransactionService, TxForm, TxOp, Unique, Value, ValueType,
};
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, AtomicUsize, Ordering};
use std::time::{Duration, Instant};

const SCORE: u32 = 1_000;
const TAG: u32 = 1_001;
const KEY: u32 = 1_002;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            SCORE,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ),
        Attribute::new(
            TAG,
            Keyword::new("item", "tags"),
            ValueType::Long,
            Cardinality::Many,
        ),
        Attribute::new(
            KEY,
            Keyword::new("item", "key"),
            ValueType::String,
            Cardinality::One,
        )
        .unique(Unique::Identity),
    ] {
        schema.install(attribute).unwrap();
    }
    schema
}

fn program(kind: ProgramKind, arity: u8, instructions: Vec<Instruction>) -> Program {
    Program {
        kind,
        arity,
        instructions,
    }
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn binding(name: &str, hash: atomic_core::ProgramHash) -> Vec<TxOp> {
    vec![
        add(
            EntityRef::Temp(name.into()),
            DB_IDENT as u32,
            Value::Keyword(Keyword::new("fn", name)),
        ),
        add(
            EntityRef::Temp(name.into()),
            DB_FN as u32,
            Value::Function(hash),
        ),
        add(
            EntityRef::Temp(name.into()),
            KEY,
            Value::String(name.into()),
        ),
    ]
}

fn transact(
    writer: &TransactionService,
    key: &str,
    ops: Vec<TxOp>,
) -> atomic_core::ServiceTransactionReport {
    writer
        .client()
        .transact(TransactionRequest::new(key, ops), Duration::from_secs(10))
        .unwrap()
}

fn fixture(label: &str) -> Option<(common::PostgresFixture, PostgresStore)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED actual PostgreSQL invoke witness: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&fixture.connection).unwrap();
    store.create_database("invoke", schema()).unwrap();
    Some((fixture, store))
}

fn rows(output: ProgramOutput) -> Vec<Vec<Value>> {
    match output {
        ProgramOutput::Query(rows) => rows,
        other => panic!("expected rows, received {other:?}"),
    }
}

#[test]
fn unknown_functions_and_invalid_controls_fail_without_program_or_writer_authority() {
    let db = Database::new(schema()).unwrap().database_value();
    assert_eq!(
        db.invoke(Keyword::new("fn", "missing"), &[], Default::default())
            .unwrap_err()
            .code,
        "program/function-not-found"
    );
    assert_eq!(
        db.invoke(DB_IDENT, &[], Default::default())
            .unwrap_err()
            .code,
        "program/not-a-database-function"
    );
    let cancelled = AtomicBool::new(true);
    let control = InvokeControl {
        program: ProgramControl {
            cancelled: Some(&cancelled),
            ..Default::default()
        },
        ..Default::default()
    };
    assert_eq!(
        db.invoke(DB_IDENT, &[], control).unwrap_err().category,
        ErrorCategory::Interrupted
    );
    let control = InvokeControl {
        program: ProgramControl {
            max_stack: 0,
            ..Default::default()
        },
        ..Default::default()
    };
    assert_eq!(
        db.invoke(DB_IDENT, &[], control).unwrap_err().code,
        "program/resource-limit"
    );
}

#[test]
fn captured_binding_data_lookup_and_restart_never_refresh_or_write_the_head() {
    let Some((fixture, mut store)) = fixture("read_invoke_basis") else {
        return;
    };
    let first_program = program(
        ProgramKind::Query,
        1,
        vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(SCORE),
            Instruction::Return,
        ],
    );
    let mut second_program = first_program.clone();
    second_program.instructions.splice(
        2..2,
        [Instruction::PushConstant(Value::Long(1)), Instruction::Add],
    );
    let first_hash = store.deploy_program_blob(&first_program).unwrap();
    let second_hash = store.deploy_program_blob(&second_program).unwrap();
    let writer = common::start_service(&fixture.connection, "invoke");
    let mut ops = binding("score", first_hash);
    ops.push(add(EntityRef::Temp("item".into()), SCORE, Value::Long(7)));
    let first = transact(&writer, "first", ops);
    let entity = first.tempids["item"];
    let function = first.tempids["score"];
    let peer = Connection::connect(&fixture.connection, "invoke", 8).unwrap();
    let old = peer.db();
    let reference = old.snapshot_reference().unwrap();
    let args = [RuntimeValue::Scalar(Value::Ref(entity))];
    let start = Instant::now();
    for identifier in [
        EntityIdentifier::Id(function),
        EntityIdentifier::Ident(Keyword::new("fn", "score")),
        EntityIdentifier::Lookup {
            attribute: AttributeName::Ident(Keyword::new("item", "key")),
            value: Value::String("score".into()),
        },
    ] {
        assert_eq!(
            rows(old.invoke(identifier, &args, Default::default()).unwrap()),
            vec![vec![Value::Long(7)]]
        );
    }
    eprintln!(
        "invoke initial eid/ident/lookup: 3 complete call/check/drop cycles in {:?}",
        start.elapsed()
    );
    let second = transact(
        &writer,
        "second",
        vec![
            add(
                EntityRef::Id(function),
                DB_FN as u32,
                Value::Function(second_hash),
            ),
            add(EntityRef::Id(entity), SCORE, Value::Long(21)),
        ],
    );
    writer.shutdown();
    peer.sync().unwrap();
    let current = peer.db();
    for (value, expected) in [
        (&old, 7),
        (&current, 22),
        (&current.clone().as_of(first.basis_t), 7),
    ] {
        assert_eq!(
            rows(
                value
                    .invoke(Keyword::new("fn", "score"), &args, Default::default())
                    .unwrap()
            ),
            vec![vec![Value::Long(expected)]]
        );
    }
    let hidden = old.clone().filter(|_, datom| datom.attribute != SCORE);
    assert_eq!(
        hidden
            .invoke(function, &args, Default::default())
            .unwrap_err()
            .code,
        "program/missing-value"
    );
    // Drop all peers before opening the portable exact reference. The function
    // is still the historical content hash, not the currently bound program.
    drop(peer);
    drop(old);
    drop(current);
    let reopened = reference
        .open(
            &PostgresConnectionConfig::plaintext(&fixture.connection),
            8,
            1024 * 1024,
        )
        .unwrap();
    assert_eq!(
        rows(
            reopened
                .invoke(function, &args, Default::default())
                .unwrap()
        ),
        vec![vec![Value::Long(7)]]
    );
    let fresh = Connection::connect(&fixture.connection, "invoke", 8).unwrap();
    assert_eq!(fresh.db().basis_t(), second.basis_t);
    assert_eq!(reopened.basis_t(), first.basis_t);
    eprintln!(
        "invoke exact-basis: 7 checked outputs plus 1 expected filtered error, including rebind/reconnect; elapsed={:?}; head={}",
        start.elapsed(),
        fresh.db().basis_t()
    );
}

#[test]
fn native_roles_return_typed_results_and_transaction_forms_remain_inert() {
    let Some((fixture, mut store)) = fixture("read_invoke_roles") else {
        return;
    };
    let mut ops = Vec::new();
    let programs = [
        (
            "attribute",
            program(
                ProgramKind::AttributePredicate,
                1,
                vec![Instruction::PushArgument(0), Instruction::Return],
            ),
        ),
        (
            "dual",
            program(
                ProgramKind::DualPredicate,
                1,
                vec![
                    Instruction::PredicateDispatch {
                        attribute: vec![
                            Instruction::PushArgument(0),
                            Instruction::PushConstant(Value::Long(0)),
                            Instruction::GreaterThan,
                        ],
                        entity: vec![
                            Instruction::PushArgument(0),
                            Instruction::LoadOne(SCORE),
                            Instruction::PushConstant(Value::Long(7)),
                            Instruction::Equal,
                        ],
                    },
                    Instruction::Return,
                ],
            ),
        ),
        (
            "entity",
            program(
                ProgramKind::EntityPredicate,
                1,
                vec![
                    Instruction::PushArgument(0),
                    Instruction::LoadOne(SCORE),
                    Instruction::Return,
                ],
            ),
        ),
        (
            "query",
            program(
                ProgramKind::Query,
                1,
                vec![
                    Instruction::PushArgument(0),
                    Instruction::Length,
                    Instruction::Return,
                ],
            ),
        ),
        (
            "transaction",
            program(
                ProgramKind::Transaction,
                0,
                vec![
                    Instruction::PushEntity(EntityRef::Temp("speculative".into())),
                    Instruction::PushConstant(Value::Long(9)),
                    Instruction::EmitAdd(SCORE),
                    // This missing binding must not be resolved: invoking the outer
                    // function returns declarative nested call data, not a transaction.
                    Instruction::EmitCall {
                        function: CallableRef::Database(EntityRef::Ident(Keyword::new(
                            "fn",
                            "not-installed",
                        ))),
                        argument_count: 0,
                    },
                    Instruction::Return,
                ],
            ),
        ),
    ];
    for (name, program) in programs {
        ops.extend(binding(name, store.deploy_program_blob(&program).unwrap()));
    }
    ops.push(add(EntityRef::Temp("item".into()), SCORE, Value::Long(7)));
    let writer = common::start_service(&fixture.connection, "invoke");
    let report = transact(&writer, "bind", ops);
    writer.shutdown();
    let db = report.db_after;
    let item = RuntimeValue::Scalar(Value::Ref(report.tempids["item"]));
    let text = RuntimeValue::Scalar(Value::String("not coerced".into()));
    match db
        .invoke(
            Keyword::new("fn", "attribute"),
            std::slice::from_ref(&text),
            Default::default(),
        )
        .unwrap()
    {
        ProgramOutput::AttributePredicate(result) => assert_eq!(result, text),
        other => panic!("unexpected {other:?}"),
    }
    match db
        .invoke(
            Keyword::new("fn", "entity"),
            std::slice::from_ref(&item),
            Default::default(),
        )
        .unwrap()
    {
        ProgramOutput::EntityPredicate(result) => {
            assert_eq!(result, RuntimeValue::Scalar(Value::Long(7)))
        }
        other => panic!("unexpected {other:?}"),
    }
    assert_eq!(
        db.invoke(
            Keyword::new("fn", "dual"),
            std::slice::from_ref(&item),
            Default::default()
        )
        .unwrap_err()
        .code,
        "program/predicate-role-required"
    );
    for (role, argument) in [
        (
            InvokeRole::AttributePredicate,
            RuntimeValue::Scalar(Value::Long(1)),
        ),
        (InvokeRole::EntityPredicate, item),
    ] {
        let output = db
            .invoke(
                Keyword::new("fn", "dual"),
                &[argument],
                InvokeControl {
                    role,
                    ..Default::default()
                },
            )
            .unwrap();
        let result = match output {
            ProgramOutput::AttributePredicate(result) | ProgramOutput::EntityPredicate(result) => {
                result
            }
            other => panic!("unexpected {other:?}"),
        };
        assert_eq!(result, RuntimeValue::Scalar(Value::Bool(true)));
    }
    assert_eq!(
        db.invoke(
            Keyword::new("fn", "query"),
            &[RuntimeValue::Scalar(Value::Long(1))],
            InvokeControl {
                role: InvokeRole::AttributePredicate,
                ..Default::default()
            }
        )
        .unwrap_err()
        .code,
        "program/not-attribute-predicate"
    );
    assert_eq!(
        db.invoke(
            Keyword::new("fn", "entity"),
            &[RuntimeValue::Null],
            Default::default()
        )
        .unwrap_err()
        .code,
        "program/predicate-argument"
    );
    assert_eq!(
        db.invoke(Keyword::new("fn", "query"), &[], Default::default())
            .unwrap_err()
            .code,
        "program/arity"
    );
    assert_eq!(
        rows(
            db.invoke(
                Keyword::new("fn", "query"),
                &[RuntimeValue::Vector(vec![RuntimeValue::Null, text])],
                Default::default()
            )
            .unwrap()
        ),
        vec![vec![Value::Long(2)]]
    );
    let ProgramOutput::Transaction(forms) = db
        .invoke(Keyword::new("fn", "transaction"), &[], Default::default())
        .unwrap()
    else {
        panic!("expected inert transaction forms")
    };
    assert_eq!(forms.len(), 2);
    assert!(
        matches!(&forms[0], TxForm::Op(TxOp::Add { entity: EntityRef::Temp(name), attribute: SCORE, .. }) if name == "speculative")
    );
    assert!(
        matches!(&forms[1], TxForm::ProgramCall(call) if call.function == CallableRef::Database(EntityRef::Ident(Keyword::new("fn", "not-installed"))))
    );
    assert_eq!(
        Connection::connect(&fixture.connection, "invoke", 8)
            .unwrap()
            .db()
            .basis_t(),
        report.basis_t
    );
}

#[test]
fn invocation_budgets_include_binding_reads_and_reuse_after_failure() {
    let Some((fixture, mut store)) = fixture("read_invoke_limits") else {
        return;
    };
    let code = program(
        ProgramKind::Query,
        1,
        vec![
            Instruction::PushArgument(0),
            Instruction::LoadMany(TAG),
            Instruction::Length,
            Instruction::Return,
        ],
    );
    let hash = store.deploy_program_blob(&code).unwrap();
    let mut ops = binding("count", hash);
    for (name, instruction) in [
        ("one", Instruction::LoadOne(TAG)),
        ("exists", Instruction::Exists(TAG)),
    ] {
        let code = program(
            ProgramKind::Query,
            1,
            vec![
                Instruction::PushArgument(0),
                instruction,
                Instruction::Return,
            ],
        );
        ops.extend(binding(name, store.deploy_program_blob(&code).unwrap()));
    }
    let template = QueryTemplate::new(
        vec![0],
        vec![QueryPattern::new(
            QueryTerm::Input(0),
            TAG,
            QueryTerm::Variable(0),
        )],
    )
    .unwrap();
    let code = program(
        ProgramKind::Query,
        1,
        vec![
            Instruction::Query(template),
            Instruction::Length,
            Instruction::Return,
        ],
    );
    ops.extend(binding(
        "pattern",
        store.deploy_program_blob(&code).unwrap(),
    ));
    ops.extend((0..128).map(|n| add(EntityRef::Temp("item".into()), TAG, Value::Long(n))));
    ops.push(add(
        EntityRef::Temp("item".into()),
        KEY,
        Value::String("lookup-key".into()),
    ));
    let writer = common::start_service(&fixture.connection, "invoke");
    let report = transact(&writer, "bind", ops);
    let db = report.db_after;
    let args = [RuntimeValue::Scalar(Value::Ref(report.tempids["item"]))];
    let function = report.tempids["count"];
    for (control, expected) in [
        (
            InvokeControl {
                max_read_datoms: 0,
                ..Default::default()
            },
            "transaction/read-capacity",
        ),
        (
            InvokeControl {
                max_read_bytes: 1,
                ..Default::default()
            },
            "transaction/read-capacity",
        ),
        (
            InvokeControl {
                max_read_datoms: 8,
                ..Default::default()
            },
            "transaction/read-capacity",
        ),
        (
            InvokeControl {
                program: ProgramControl {
                    fuel: 0,
                    ..Default::default()
                },
                ..Default::default()
            },
            "program/fuel-exhausted",
        ),
        (
            InvokeControl {
                program: ProgramControl {
                    max_collection_items: 3,
                    ..Default::default()
                },
                ..Default::default()
            },
            "program/collection-limit",
        ),
        (
            InvokeControl {
                program: ProgramControl {
                    max_output: 1,
                    ..Default::default()
                },
                ..Default::default()
            },
            "program/output-limit",
        ),
        (
            InvokeControl {
                program: ProgramControl {
                    max_value_bytes: 1,
                    ..Default::default()
                },
                ..Default::default()
            },
            "program/value-byte-limit",
        ),
        (
            InvokeControl {
                deadline: Some(Instant::now() - Duration::from_secs(1)),
                ..Default::default()
            },
            "program/timeout",
        ),
    ] {
        let error = db.invoke(function, &args, control).unwrap_err();
        assert_eq!(error.code, expected, "{error:?}");
        assert_eq!(
            rows(db.invoke(function, &args, Default::default()).unwrap()),
            vec![vec![Value::Long(128)]]
        );
    }
    let visits = Arc::new(AtomicUsize::new(0));
    let cancelled = Arc::new(AtomicBool::new(false));
    let filtered = db.clone().filter({
        let visits = Arc::clone(&visits);
        let cancelled = Arc::clone(&cancelled);
        move |_, datom| {
            if datom.attribute == TAG {
                if visits.fetch_add(1, Ordering::Relaxed) + 1 == 7 {
                    cancelled.store(true, Ordering::Relaxed);
                }
                false
            } else {
                true
            }
        }
    });
    let control = InvokeControl {
        program: ProgramControl {
            cancelled: Some(&cancelled),
            ..Default::default()
        },
        ..Default::default()
    };
    for name in ["count", "one", "exists", "pattern"] {
        visits.store(0, Ordering::Relaxed);
        cancelled.store(false, Ordering::Relaxed);
        assert_eq!(
            filtered
                .invoke(report.tempids[name], &args, control)
                .unwrap_err()
                .code,
            "program/cancelled"
        );
        assert!(
            visits.load(Ordering::Relaxed) <= 8,
            "{name}: rejecting filter consumed {} candidates after cancellation",
            visits.load(Ordering::Relaxed)
        );
    }
    cancelled.store(false, Ordering::Relaxed);
    assert_eq!(
        rows(db.invoke(function, &args, control).unwrap()),
        vec![vec![Value::Long(128)]]
    );
    let visits = Arc::new(AtomicUsize::new(0));
    let rejected = db.clone().filter({
        let visits = Arc::clone(&visits);
        move |_, datom| {
            if datom.attribute == TAG {
                visits.fetch_add(1, Ordering::Relaxed);
                false
            } else {
                true
            }
        }
    });
    let start = Instant::now();
    for name in ["count", "one", "exists", "pattern"] {
        visits.store(0, Ordering::Relaxed);
        assert_eq!(
            rejected
                .invoke(
                    report.tempids[name],
                    &args,
                    InvokeControl {
                        program: ProgramControl {
                            fuel: 64,
                            ..Default::default()
                        },
                        ..Default::default()
                    }
                )
                .unwrap_err()
                .code,
            "program/fuel-exhausted"
        );
        assert!(
            visits.load(Ordering::Relaxed) < 32,
            "{name}: fuel must admit rejected raw candidates"
        );
    }
    let fuel_visits = visits.load(Ordering::Relaxed);
    assert!(
        fuel_visits < 32,
        "fuel admission examined {fuel_visits} of 128 rejected values"
    );
    eprintln!(
        "invoke rejecting-filter complete failure/check/drop: 64 fuel, {fuel_visits}/128 candidate visits, {:?}",
        start.elapsed()
    );
    visits.store(0, Ordering::Relaxed);
    // This existing observer is a logical-result budget, not a physical-row
    // counter. The binding consumes one datom and the rejected body none;
    // the separate fuel bound above still admits all raw candidate work.
    assert_eq!(
        rows(
            rejected
                .invoke(
                    function,
                    &args,
                    InvokeControl {
                        max_read_datoms: 8,
                        ..Default::default()
                    }
                )
                .unwrap()
        ),
        vec![vec![Value::Long(0)]]
    );
    assert_eq!(visits.load(Ordering::Relaxed), 128);

    // Unique lookup key assertions/retractions across time must obey controls
    // during operand resolution, before the body opens its own prefix cursor.
    let mut latest = db.clone();
    for step in 0..16 {
        let key = if step % 2 == 0 {
            "other-key"
        } else {
            "lookup-key"
        };
        latest = transact(
            &writer,
            &format!("lookup-history-{step}"),
            vec![add(
                EntityRef::Id(report.tempids["item"]),
                KEY,
                Value::String(key.into()),
            )],
        )
        .db_after;
    }
    writer.shutdown();
    let visits = Arc::new(AtomicUsize::new(0));
    let cancelled = Arc::new(AtomicBool::new(false));
    let rejected = latest.history().filter({
        let visits = Arc::clone(&visits);
        let cancelled = Arc::clone(&cancelled);
        move |_, datom| {
            if datom.attribute == KEY {
                if visits.fetch_add(1, Ordering::Relaxed) + 1 == 7 {
                    cancelled.store(true, Ordering::Relaxed);
                }
                false
            } else {
                true
            }
        }
    });
    for name in ["count", "one", "exists", "pattern"] {
        for reference in [
            EntityRef::Lookup {
                attribute: KEY,
                value: Value::String("lookup-key".into()),
            },
            EntityRef::LookupInput {
                attribute: KEY,
                value: Box::new(Value::String("lookup-key".into()).into()),
            },
        ] {
            visits.store(0, Ordering::Relaxed);
            cancelled.store(false, Ordering::Relaxed);
            let control = InvokeControl {
                program: ProgramControl {
                    cancelled: Some(&cancelled),
                    ..Default::default()
                },
                ..Default::default()
            };
            assert_eq!(
                rejected
                    .invoke(
                        report.tempids[name],
                        &[RuntimeValue::Entity(reference)],
                        control
                    )
                    .unwrap_err()
                    .code,
                "program/cancelled"
            );
            assert_eq!(
                visits.load(Ordering::Relaxed),
                7,
                "{name}: lookup normalization must stop at cancellation"
            );
        }
    }
}
