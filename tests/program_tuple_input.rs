mod common;

use atomic_core::{
    Attribute, CallableRef, Cardinality, Database, EntityRef, Instruction, Keyword, Program,
    ProgramBudget, ProgramCall, ProgramControl, ProgramKind, ProgramOutput, ProgramRuntime,
    RuntimeValue, Schema, TupleSpec, TxForm, TxFunctions, TxOp, TxValue, Unique, Value, ValueType,
};
use std::time::Duration;

const KEY: u32 = 1_000;
const TUPLE: u32 = 1_001;
const NOTE: u32 = 1_002;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                KEY,
                Keyword::new("target", "key"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(
            Attribute::new(
                TUPLE,
                Keyword::new("owner", "tuple"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Heterogeneous(vec![
                ValueType::Ref,
                ValueType::Long,
                ValueType::Ref,
            ]))
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            NOTE,
            Keyword::new("owner", "note"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn emitter(instruction: Instruction, arity: u8) -> Program {
    let mut instructions = (0..arity)
        .map(Instruction::PushArgument)
        .collect::<Vec<_>>();
    instructions.push(instruction);
    instructions.push(Instruction::Return);
    Program {
        kind: ProgramKind::Transaction,
        arity,
        instructions,
    }
}

fn tuple(reference: EntityRef, number: i64) -> RuntimeValue {
    RuntimeValue::Vector(vec![
        reference.into(),
        Value::Long(number).into(),
        RuntimeValue::Null,
    ])
}

fn forms(database: &Database, program: &Program, arguments: &[RuntimeValue]) -> Vec<TxForm> {
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute_runtime(program, database, arguments, ProgramControl::default())
        .unwrap()
    else {
        panic!("expected transaction output")
    };
    forms
}

fn target_ops() -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Temp("target".into()),
            attribute: KEY,
            value: Value::String("target".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("target".into()),
            attribute: atomic_core::DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("target", "ident")).into(),
        },
    ]
}

#[test]
fn runtime_vectors_emit_add_cas_and_retract_with_reference_slots_and_nil() {
    let database = Database::new(schema()).unwrap();
    let mut first = target_ops().into_iter().map(TxForm::Op).collect::<Vec<_>>();
    first.extend(forms(
        &database,
        &emitter(Instruction::EmitAdd(TUPLE), 2),
        &[
            EntityRef::Temp("owner".into()).into(),
            tuple(EntityRef::Temp("target".into()), 7),
        ],
    ));
    assert!(
        matches!(&first[2], TxForm::Op(TxOp::Add { value: TxValue::Tuple(slots), .. }) if slots.len() == 3 && slots[2].is_none())
    );
    let added = database
        .with_forms(&first, &TxFunctions::new(), 10)
        .unwrap();
    let target = added.tempids["target"];
    let owner = added.tempids["owner"];
    assert_eq!(
        added.db_after.values(owner, TUPLE),
        vec![&Value::Tuple(vec![
            Some(Value::Ref(target)),
            Some(Value::Long(7)),
            None
        ])]
    );
    let cas = forms(
        &added.db_after,
        &emitter(Instruction::EmitCas(TUPLE), 3),
        &[
            EntityRef::Id(owner).into(),
            tuple(EntityRef::Ident(Keyword::new("target", "ident")), 7),
            tuple(
                EntityRef::Lookup {
                    attribute: KEY,
                    value: Value::String("target".into()),
                },
                8,
            ),
        ],
    );
    let changed = added
        .db_after
        .with_forms(&cas, &TxFunctions::new(), 11)
        .unwrap();
    let retract = forms(
        &changed.db_after,
        &emitter(Instruction::EmitRetract(TUPLE), 2),
        &[EntityRef::Id(owner).into(), tuple(EntityRef::Id(target), 8)],
    );
    let removed = changed
        .db_after
        .with_forms(&retract, &TxFunctions::new(), 12)
        .unwrap();
    assert!(removed.db_after.values(owner, TUPLE).is_empty());
    assert_eq!(
        added.db_after.values(owner, TUPLE),
        vec![&Value::Tuple(vec![
            Some(Value::Ref(target)),
            Some(Value::Long(7)),
            None
        ])]
    );
}

#[test]
fn emitted_tuple_shape_and_both_output_and_runtime_bytes_are_bounded() {
    let database = Database::new(schema()).unwrap();
    let program = emitter(Instruction::EmitAdd(TUPLE), 2);
    for value in [
        RuntimeValue::Vector(vec![RuntimeValue::Null]),
        RuntimeValue::Vector(vec![RuntimeValue::Null; 9]),
        RuntimeValue::Vector(vec![RuntimeValue::Vector(vec![]), RuntimeValue::Null]),
        RuntimeValue::Vector(vec![
            Value::Tuple(vec![None, None]).into(),
            RuntimeValue::Null,
        ]),
        RuntimeValue::Vector(vec![RuntimeValue::map(vec![]).unwrap(), RuntimeValue::Null]),
    ] {
        assert_eq!(
            ProgramRuntime
                .execute_runtime(
                    &program,
                    &database,
                    &[EntityRef::Temp("owner".into()).into(), value],
                    ProgramControl::default()
                )
                .unwrap_err()
                .code,
            "program/invalid-input-tuple"
        );
    }
    let arguments = [
        EntityRef::Temp("owner".into()).into(),
        tuple(EntityRef::Temp("target".repeat(50)), 7),
    ];
    let mut budget = ProgramBudget::new(ProgramControl::default()).unwrap();
    ProgramRuntime
        .execute_runtime_with_budget(&program, &database, &arguments, &mut budget)
        .unwrap();
    assert!(budget.emitted_bytes() > 300);
    assert!(budget.value_bytes() > budget.emitted_bytes());
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &program,
                &database,
                &arguments,
                ProgramControl {
                    max_output: budget.emitted_bytes() - 1,
                    ..ProgramControl::default()
                }
            )
            .unwrap_err()
            .code,
        "program/output-limit"
    );
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &program,
                &database,
                &arguments,
                ProgramControl {
                    max_value_bytes: budget.value_bytes() - 1,
                    ..ProgramControl::default()
                }
            )
            .unwrap_err()
            .code,
        "program/value-byte-limit"
    );
}

fn lookup_input(reference: EntityRef, number: i64) -> EntityRef {
    EntityRef::LookupInput {
        attribute: TUPLE,
        value: Box::new(TxValue::Tuple(vec![
            Some(TxValue::Entity(reference)),
            Some(Value::Long(number).into()),
            None,
        ])),
    }
}

#[test]
fn runtime_lookup_keys_resolve_db_before_and_are_structurally_bounded_and_ordered() {
    let mut operations = target_ops();
    operations.extend([
        TxOp::Add {
            entity: EntityRef::Temp("owner".into()),
            attribute: TUPLE,
            value: TxValue::Tuple(vec![
                Some(TxValue::Entity(EntityRef::Temp("target".into()))),
                Some(Value::Long(7).into()),
                None,
            ]),
        },
        TxOp::Add {
            entity: EntityRef::Temp("owner".into()),
            attribute: NOTE,
            value: Value::String("found".into()).into(),
        },
    ]);
    let database = Database::new(schema())
        .unwrap()
        .with(&operations, 10)
        .unwrap()
        .db_after;
    let query = Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(NOTE),
            Instruction::Return,
        ],
    };
    let reference = lookup_input(EntityRef::Ident(Keyword::new("target", "ident")), 7);
    let ProgramOutput::Query(rows) = ProgramRuntime
        .execute_runtime(
            &query,
            &database,
            &[reference.clone().into()],
            ProgramControl::default(),
        )
        .unwrap()
    else {
        panic!("expected query output")
    };
    assert_eq!(rows, vec![vec![Value::String("found".into())]]);
    let next = RuntimeValue::Entity(lookup_input(
        EntityRef::Ident(Keyword::new("target", "ident")),
        8,
    ));
    assert_eq!(
        RuntimeValue::Entity(reference).canonical_cmp(&next),
        std::cmp::Ordering::Less
    );
    let mut too_deep = EntityRef::Id(1);
    for _ in 0..20 {
        too_deep = EntityRef::LookupInput {
            attribute: KEY,
            value: Box::new(TxValue::Entity(too_deep)),
        };
    }
    let unused = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![Instruction::Return],
    };
    let deep_literal = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushEntity(too_deep.clone()),
            Instruction::Pop,
            Instruction::Return,
        ],
    };
    assert_eq!(
        deep_literal.validate().unwrap_err().code,
        "program/value-depth"
    );
    let wide_key = EntityRef::LookupInput {
        attribute: KEY,
        value: Box::new(Value::String("x".repeat(1_024)).into()),
    };
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &unused,
                &database,
                &[wide_key.clone().into()],
                ProgramControl {
                    max_value_bytes: 512,
                    ..ProgramControl::default()
                }
            )
            .unwrap_err()
            .code,
        "program/value-byte-limit"
    );
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &emitter(Instruction::EmitAdd(NOTE), 2),
                &database,
                &[wide_key.into(), Value::String("note".into()).into()],
                ProgramControl {
                    max_output: 512,
                    ..ProgramControl::default()
                }
            )
            .unwrap_err()
            .code,
        "program/output-limit"
    );
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &unused,
                &database,
                &[too_deep.into()],
                ProgramControl::default()
            )
            .unwrap_err()
            .code,
        "program/value-depth"
    );
}

#[test]
fn lookup_input_literals_select_new_program_abi_without_reinterpreting_old_abis() {
    let plain = atomic_core::encode_program(&emitter(Instruction::EmitAdd(TUPLE), 2)).unwrap();
    assert_eq!(&plain[16..18], &4u16.to_be_bytes());
    let literal = lookup_input(EntityRef::Ident(Keyword::new("target", "ident")), 7);
    let programs = [
        Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![
                Instruction::PushConstant(Value::Bool(true)),
                Instruction::If {
                    then_branch: vec![
                        Instruction::PushEntity(literal.clone()),
                        Instruction::EmitRetractEntity,
                    ],
                    else_branch: vec![],
                },
                Instruction::Return,
            ],
        },
        Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![
                Instruction::EmitCall {
                    function: CallableRef::Database(literal),
                    argument_count: 0,
                },
                Instruction::Return,
            ],
        },
    ];
    for program in programs {
        let bytes = atomic_core::encode_program(&program).unwrap();
        assert_eq!(&bytes[16..18], &6u16.to_be_bytes());
        assert_eq!(atomic_core::decode_program(&bytes).unwrap(), program);
        for old_abi in [4u16, 5] {
            let mut downgraded = bytes.clone();
            downgraded[16..18].copy_from_slice(&old_abi.to_be_bytes());
            let checksum_at = downgraded.len() - 32;
            let checksum = atomic_core::sha256(&downgraded[..checksum_at]);
            downgraded[checksum_at..].copy_from_slice(&checksum);
            assert_eq!(
                atomic_core::decode_program(&downgraded).unwrap_err().code,
                "encoding/noncanonical-program"
            );
        }
    }
}

#[cfg(unix)]
#[test]
fn persisted_nested_function_tuple_emission_survives_socket_retry_and_writer_recovery() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let id = format!(
        "program-tuple-{}-{}",
        std::process::id(),
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    atomic_core::PostgresMigrator::connect(&postgres)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = atomic_core::PostgresStore::connect(&postgres).unwrap();
    store.create_database(&id, schema()).unwrap();
    let child = store
        .deploy_program_blob(&emitter(Instruction::EmitAdd(TUPLE), 2))
        .unwrap();
    let parent = store
        .deploy_program_blob(&emitter(
            Instruction::EmitCall {
                function: CallableRef::ExactHash(child),
                argument_count: 2,
            },
            2,
        ))
        .unwrap();
    let reader = store
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 1,
            instructions: vec![
                Instruction::PushArgument(0),
                Instruction::Duplicate,
                Instruction::LoadOne(TUPLE),
                Instruction::EmitAdd(TUPLE),
                Instruction::Return,
            ],
        })
        .unwrap();
    let writer = common::start_service(&postgres, &id);
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let peer = atomic_core::Connection::connect(&postgres, &id, 4).unwrap();
    let request = atomic_core::TransactionRequest::new("tuple-function", target_ops())
        .calling(ProgramCall {
            function: CallableRef::ExactHash(parent),
            arguments: vec![
                EntityRef::Temp("owner".into()).into(),
                tuple(EntityRef::Temp("target".into()), 7),
            ],
        })
        .with_tx_instant(10);
    let committed = peer
        .transact_socket(server.endpoint(), request.clone(), Duration::from_secs(15))
        .unwrap();
    let report = committed.report.unwrap();
    assert_eq!(
        report
            .db_after
            .values(report.tempids["owner"], TUPLE)
            .unwrap(),
        vec![Value::Tuple(vec![
            Some(Value::Ref(report.tempids["target"])),
            Some(Value::Long(7)),
            None
        ])]
    );
    let owner_key = lookup_input(EntityRef::Ident(Keyword::new("target", "ident")), 7);
    let read_request = atomic_core::TransactionRequest::new(
        "tuple-function-read",
        vec![TxOp::Add {
            entity: owner_key.clone(),
            attribute: NOTE,
            value: Value::String("read through tuple key".into()).into(),
        }],
    )
    .calling(ProgramCall {
        function: CallableRef::ExactHash(reader),
        arguments: vec![owner_key.into()],
    })
    .with_tx_instant(11);
    let read = peer
        .transact_socket(server.endpoint(), read_request, Duration::from_secs(15))
        .unwrap()
        .report
        .unwrap();
    assert!(read.tx_data.iter().all(|datom| datom.attribute != TUPLE));
    assert_eq!(
        read.db_after.values(report.tempids["owner"], NOTE).unwrap(),
        vec![Value::String("read through tuple key".into())]
    );
    assert!(
        report
            .db_after
            .values(report.tempids["owner"], NOTE)
            .unwrap()
            .is_empty()
    );
    drop(server);
    writer.shutdown();
    common::assert_same_information(&read.db_after, &store.recover(&id).unwrap());
    let writer = common::start_service(&postgres, &id);
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let replay = peer
        .transact_socket(server.endpoint(), request, Duration::from_secs(15))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    assert_eq!(replay.report.unwrap().tempids, report.tempids);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    drop(server);
    writer.shutdown();
}
