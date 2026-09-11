mod common;

use atomic_core::{
    Attribute, CallableRef, Cardinality, DB_FN, DB_IDENT, ProgramCall, Schema, TransactionRequest,
    ValueType, implicit_part, partition_eid,
};
use atomic_core::{
    AttributeRef, Database, EntityRef, Instruction, Keyword, Program, ProgramControl, ProgramKind,
    ProgramOutput, ProgramRuntime, RuntimeValue, TxForm, TxOp, Value, decode_program,
    encode_program, encode_program_output, sha256,
};
use std::time::Duration;

fn policy_program() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::String("parent".into())),
            Instruction::PushConstant(Value::Keyword(Keyword::new("db.part", "user"))),
            Instruction::EmitForcePartition,
            Instruction::PushEntity(EntityRef::Temp("child".into())),
            Instruction::PushEntity(EntityRef::Temp("parent".into())),
            Instruction::EmitMatchPartition,
            Instruction::Return,
        ],
    }
}

#[test]
fn current_program_format_emits_bounded_partition_policy() {
    let db = Database::bootstrap().unwrap();
    let program = policy_program();
    let bytes = encode_program(&program).unwrap();
    assert_eq!(
        &bytes[16..18],
        &atomic_core::PROGRAM_ABI_VERSION.to_be_bytes()
    );
    assert_eq!(decode_program(&bytes).unwrap(), program);
    let output = ProgramRuntime
        .execute(&program, &db, &[], ProgramControl::default())
        .unwrap();
    let ProgramOutput::Transaction(forms) = &output else {
        panic!("transaction program output")
    };
    assert!(
        matches!(&forms[0], TxForm::Op(TxOp::ForcePartition { tempid, partition: EntityRef::Ident(ident) }) if tempid == "parent" && ident == &Keyword::new("db.part", "user"))
    );
    assert!(
        matches!(&forms[1], TxForm::Op(TxOp::MatchPartition { tempid, entity: EntityRef::Temp(parent) }) if tempid == "child" && parent == "parent")
    );
    assert!(!encode_program_output(&output).unwrap().is_empty());
    let error = ProgramRuntime
        .execute(
            &program,
            &db,
            &[],
            ProgramControl {
                max_forms: 1,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/form-limit");
    for old_abi in 4u16..atomic_core::PROGRAM_ABI_VERSION {
        let mut downgraded = bytes.clone();
        downgraded[16..18].copy_from_slice(&old_abi.to_be_bytes());
        let checksum_at = downgraded.len() - 32;
        let checksum = sha256(&downgraded[..checksum_at]);
        downgraded[checksum_at..].copy_from_slice(&checksum);
        assert!(decode_program(&downgraded).is_err());
    }
    for kind in [ProgramKind::Transaction, ProgramKind::Query] {
        let minimal = Program {
            kind,
            arity: 0,
            instructions: vec![Instruction::Return],
        };
        if minimal.validate().is_ok() {
            assert_eq!(
                &encode_program(&minimal).unwrap()[16..18],
                &atomic_core::PROGRAM_ABI_VERSION.to_be_bytes()
            );
        }
    }
    let mut nested = program;
    nested.instructions = vec![
        Instruction::PushConstant(Value::Bool(true)),
        Instruction::If {
            then_branch: nested.instructions[..nested.instructions.len() - 1].to_vec(),
            else_branch: vec![],
        },
        Instruction::Return,
    ];
    assert_eq!(
        &encode_program(&nested).unwrap()[16..18],
        &atomic_core::PROGRAM_ABI_VERSION.to_be_bytes()
    );
    assert_eq!(
        decode_program(&encode_program(&nested).unwrap()).unwrap(),
        nested
    );
}

#[test]
fn partition_emission_requires_transaction_program_and_tempid_target() {
    let db = Database::bootstrap().unwrap();
    let mut program = policy_program();
    program.kind = ProgramKind::Query;
    assert_eq!(program.validate().unwrap_err().code, "program/output-kind");
    program.kind = ProgramKind::Transaction;
    program.instructions[0] = Instruction::PushEntity(EntityRef::Id(4));
    assert_eq!(
        ProgramRuntime
            .execute(&program, &db, &[], ProgramControl::default())
            .unwrap_err()
            .code,
        "program/partition-tempid"
    );
}

#[test]
fn runtime_partition_install_map_keeps_normal_entity_map_representation() {
    let db = Database::bootstrap().unwrap();
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::EmitEntityMap,
            Instruction::Return,
        ],
    };
    // Entity maps and partition instructions share one current program format.
    assert_eq!(
        &encode_program(&program).unwrap()[16..18],
        &atomic_core::PROGRAM_ABI_VERSION.to_be_bytes()
    );
    let input = RuntimeValue::Map(vec![
        (
            Value::Keyword(Keyword::new("db", "id")),
            RuntimeValue::Scalar(Value::String("partition".into())),
        ),
        (
            Value::Keyword(Keyword::new("db", "ident")),
            RuntimeValue::Scalar(Value::Keyword(Keyword::new("part", "customers"))),
        ),
        (
            Value::Keyword(Keyword::new("db.install", "_partition")),
            RuntimeValue::Scalar(Value::Keyword(Keyword::new("db.part", "db"))),
        ),
    ]);
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute_runtime(&program, &db, &[input], ProgramControl::default())
        .unwrap()
    else {
        panic!("transaction forms")
    };
    let TxForm::EntityMap(map) = &forms[0] else {
        panic!("entity map")
    };
    assert!(map.attributes.iter().any(|(attribute, _)| matches!(attribute, AttributeRef::ReverseIdent(ident) if ident == &Keyword::new("db.install", "partition"))));
}

#[test]
fn stored_partition_program_speculates_commits_retries_and_recovers_in_postgres() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "partition_program");
    common::install(&fixture.connection).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let mut store = common::TestStore::connect(&fixture.connection).unwrap();
    let created = store.create_database("partition-program", schema).unwrap();
    let partition = implicit_part(17).unwrap();
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushEntity(EntityRef::Temp("parent".into())),
            Instruction::PushConstant(Value::Ref(partition)),
            Instruction::EmitForcePartition,
            Instruction::PushEntity(EntityRef::Temp("child".into())),
            Instruction::PushEntity(EntityRef::Temp("parent".into())),
            Instruction::EmitMatchPartition,
            Instruction::PushEntity(EntityRef::Temp("parent".into())),
            Instruction::PushArgument(0),
            Instruction::EmitAdd(1000),
            Instruction::PushEntity(EntityRef::Temp("child".into())),
            Instruction::PushConstant(Value::String("child".into())),
            Instruction::EmitAdd(1000),
            Instruction::Return,
        ],
    };
    let hash = store.deploy_program_blob(&program).unwrap();
    assert_eq!(store.resolve_program(hash).unwrap(), program);
    let writer = common::start_service(&fixture.connection, "partition-program");
    let installed = common::transact(
        &writer,
        "install",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("function".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("person", "create-family")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("function".into()),
                attribute: DB_FN as u32,
                value: Value::Function(hash).into(),
            },
        ],
        1000,
    );
    let form = TxForm::ProgramCall(ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(Keyword::new("person", "create-family"))),
        arguments: vec![RuntimeValue::Scalar(Value::String("Ada".into()))],
    });
    let preview = installed
        .db_after
        .with_forms(std::slice::from_ref(&form), 2000)
        .unwrap();
    assert_eq!(partition_eid(preview.tempids["parent"]).unwrap(), partition);
    assert_eq!(partition_eid(preview.tempids["child"]).unwrap(), partition);
    assert_eq!(
        store.recover("partition-program").unwrap().basis_t(),
        installed.basis_t
    );
    let request = TransactionRequest {
        request_key: "family".into(),
        forms: vec![form],
        compare_basis_t: Some(installed.basis_t),
        tx_instant_override: Some(2000),
    };
    let committed = writer
        .client()
        .transact(request.clone(), Duration::from_secs(5))
        .unwrap();
    for name in ["parent", "child"] {
        assert_eq!(partition_eid(committed.tempids[name]).unwrap(), partition);
    }
    // Only actual name assertions and transaction metadata persist; policies
    // never become phantom domain facts.
    assert_eq!(
        committed
            .tx_data
            .iter()
            .filter(|datom| datom.attribute == 1000)
            .count(),
        2
    );
    let retry = writer
        .client()
        .transact(request.clone(), Duration::from_secs(5))
        .unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.tempids, committed.tempids);
    assert_eq!(retry.basis_t, committed.basis_t);
    writer.shutdown();
    let recovered = store.recover("partition-program").unwrap();
    assert_eq!(recovered.basis_t(), committed.basis_t);
    let restarted = common::start_service(&fixture.connection, "partition-program");
    let recovered_retry = restarted
        .client()
        .transact(request, Duration::from_secs(5))
        .unwrap();
    assert!(recovered_retry.replayed);
    assert_eq!(recovered_retry.tempids, committed.tempids);
    assert_eq!(
        recovered_retry
            .db_after
            .values(committed.tempids["parent"], 1000)
            .unwrap(),
        vec![Value::String("Ada".into())]
    );
    restarted.shutdown();
    eprintln!(
        "actual PostgreSQL stored partition program: speculation, commit, receipt retry, restart replay verified"
    );
}
