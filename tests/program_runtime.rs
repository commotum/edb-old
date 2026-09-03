use atomic_core::{
    Attribute, Cardinality, Database, ErrorCategory, Instruction, Keyword, Program, ProgramControl,
    ProgramKind, ProgramOutput, ProgramRuntime, TxOp, TxValue, Value, ValueType, decode_program,
    encode_program, encode_program_output, program_hash,
};
use std::sync::atomic::AtomicBool;

fn database() -> Database {
    let mut schema = atomic_core::Schema::new();
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
            Keyword::new("account", "balance"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: atomic_core::EntityRef::Id(1_000),
                attribute: 10,
                value: TxValue::Scalar(Value::Long(40)),
            }],
            1_000,
        )
        .unwrap()
        .db_after
}

fn increment() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::Duplicate,
            Instruction::LoadOne(10),
            Instruction::PushConstant(Value::Long(2)),
            Instruction::Add,
            Instruction::EmitAdd(10),
            Instruction::Return,
        ],
    }
}

#[test]
fn program_encoding_is_canonical_hashed_and_checked() {
    let program = increment();
    let bytes = encode_program(&program).unwrap();
    assert_eq!(decode_program(&bytes).unwrap(), program);
    assert_eq!(program_hash(&program).unwrap(), atomic_core::sha256(&bytes));
    assert_eq!(
        hex(&program_hash(&program).unwrap()),
        "472b8ea6727a55059871640fb16c191e72d9477a3a0b7eb7efbc98f03576854d"
    );

    let mut corrupt = bytes;
    corrupt[20] ^= 1;
    let error = decode_program(&corrupt).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Fault);
    assert_eq!(error.code, "encoding/checksum-mismatch");
}

#[test]
fn transaction_program_reads_one_db_before_and_emits_ordinary_ops() {
    let db = database();
    let output = ProgramRuntime
        .execute(
            &increment(),
            &db,
            &[Value::Ref(1_000)],
            ProgramControl::default(),
        )
        .unwrap();
    let ProgramOutput::Transaction(ops) = output else {
        panic!("wrong output kind");
    };
    let report = db.with(&ops, 2_000).unwrap();
    assert_eq!(report.db_after.values(1_000, 10), vec![&Value::Long(42)]);
}

#[test]
fn predicate_and_query_results_are_typed_and_deterministic() {
    let predicate = Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(0)),
            Instruction::GreaterThan,
            Instruction::Return,
        ],
    };
    let query = Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(10),
            Instruction::PushConstant(Value::Long(2)),
            Instruction::Multiply,
            Instruction::Return,
        ],
    };
    let db = database();
    let runtime = ProgramRuntime;
    assert!(matches!(
        runtime
            .execute(
                &predicate,
                &db,
                &[Value::Long(1)],
                ProgramControl::default()
            )
            .unwrap(),
        ProgramOutput::AttributePredicate(true)
    ));
    let first = runtime
        .execute(&query, &db, &[Value::Ref(1_000)], ProgramControl::default())
        .unwrap();
    let second = runtime
        .execute(&query, &db, &[Value::Ref(1_000)], ProgramControl::default())
        .unwrap();
    assert_eq!(format!("{first:?}"), format!("{second:?}"));
    assert!(matches!(first, ProgramOutput::Query(rows) if rows == vec![vec![Value::Long(80)]]));
}

#[test]
fn validation_and_all_runtime_limits_fail_closed() {
    let db = database();
    let invalid = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![Instruction::Pop, Instruction::Return],
    };
    assert_eq!(
        invalid.validate().unwrap_err().code,
        "program/static-stack-underflow"
    );

    let query = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(1)),
            Instruction::PushConstant(Value::Long(2)),
            Instruction::Return,
        ],
    };
    let runtime = ProgramRuntime;
    let error = runtime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                fuel: 1,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Busy, "program/fuel-exhausted")
    );

    let error = runtime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                max_stack: 1,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/stack-limit");

    let cancelled = AtomicBool::new(true);
    let error = runtime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                cancelled: Some(&cancelled),
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Interrupted, "program/cancelled")
    );

    let error = runtime
        .execute(
            &increment(),
            &db,
            &[Value::Long(1_000)],
            ProgramControl::default(),
        )
        .unwrap_err();
    assert_eq!(error.code, "program/type");
}

#[test]
fn independent_decodes_produce_byte_identical_observations() {
    let db = database();
    let bytes = encode_program(&increment()).unwrap();
    let left_program = decode_program(&bytes).unwrap();
    let right_program = decode_program(&bytes).unwrap();
    let left = ProgramRuntime
        .execute(
            &left_program,
            &db,
            &[Value::Ref(1_000)],
            ProgramControl::default(),
        )
        .unwrap();
    let right = ProgramRuntime
        .execute(
            &right_program,
            &db.clone(),
            &[Value::Ref(1_000)],
            ProgramControl::default(),
        )
        .unwrap();
    assert_eq!(
        encode_program_output(&left).unwrap(),
        encode_program_output(&right).unwrap()
    );
}

fn hex(bytes: &[u8]) -> String {
    bytes.iter().map(|byte| format!("{byte:02x}")).collect()
}
