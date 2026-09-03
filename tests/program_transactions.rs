use atomic_core::{
    Attribute, Cardinality, EntityRef, ErrorCategory, Instruction, Keyword, PostgresStore, Program,
    ProgramControl, ProgramInvocation, ProgramKind, Schema, TxOp, TxValue, Value, ValueType, View,
};
use postgres::{Client, NoTls};
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
        .install(
            Attribute::new(
                10,
                Keyword::new("account", "balance"),
                ValueType::Long,
                Cardinality::One,
            )
            .predicate("positive"),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            11,
            Keyword::new("account", "snapshot"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn positive() -> Program {
    Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(0)),
            Instruction::GreaterThan,
            Instruction::Return,
        ],
    }
}

fn set_balance() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushArgument(1),
            Instruction::EmitAdd(10),
            Instruction::Return,
        ],
    }
}

fn snapshot_balance() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::Duplicate,
            Instruction::LoadOne(10),
            Instruction::EmitAdd(11),
            Instruction::Return,
        ],
    }
}

#[test]
fn persisted_functions_compose_on_db_before_and_predicates_guard_commit() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("program_tx");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    store.create_database(&database_id, schema()).unwrap();
    store
        .deploy_program(&database_id, "positive", 1, &positive())
        .unwrap();
    store
        .activate_program(&database_id, "positive", None, 1)
        .unwrap();
    let initial = store
        .transact_with_persisted_predicates(
            &database_id,
            "initial",
            0,
            &[TxOp::Add {
                entity: EntityRef::Id(1_000),
                attribute: 10,
                value: TxValue::Scalar(Value::Long(10)),
            }],
            1_000,
        )
        .unwrap();
    assert_eq!(initial.database.values(1_000, 10), vec![&Value::Long(10)]);

    let setter = store
        .deploy_program(&database_id, "set", 1, &set_balance())
        .unwrap();
    let snapshot = store
        .deploy_program(&database_id, "snapshot", 1, &snapshot_balance())
        .unwrap();
    let composed = store
        .transact_programs(
            &database_id,
            "composed",
            1,
            &[
                ProgramInvocation {
                    hash: setter,
                    arguments: vec![Value::Ref(1_000), Value::Long(99)],
                },
                ProgramInvocation {
                    hash: snapshot,
                    arguments: vec![Value::Ref(1_000)],
                },
            ],
            2_000,
            ProgramControl::default(),
        )
        .unwrap();
    assert_eq!(composed.database.values(1_000, 10), vec![&Value::Long(99)]);
    assert_eq!(composed.database.values(1_000, 11), vec![&Value::Long(10)]);

    let error = store
        .transact_program_hash(
            &database_id,
            setter,
            "invalid",
            2,
            &[Value::Ref(1_000), Value::Long(-1)],
            3_000,
            ProgramControl::default(),
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/attribute-predicate");
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 2);
}

#[test]
fn exact_hash_retry_survives_activation_change_and_history_needs_no_code() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("program_retry");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    store.create_database(&database_id, schema()).unwrap();
    store
        .deploy_program(&database_id, "positive", 1, &positive())
        .unwrap();
    store
        .activate_program(&database_id, "positive", None, 1)
        .unwrap();
    let v1 = store
        .deploy_program(&database_id, "set", 1, &set_balance())
        .unwrap();
    store
        .activate_program(&database_id, "set", None, 1)
        .unwrap();
    let first = store
        .transact_program(
            &database_id,
            "set",
            "retry-key",
            0,
            &[Value::Ref(1_000), Value::Long(5)],
            1_000,
            ProgramControl::default(),
        )
        .unwrap();
    assert_eq!(first.program_hash, v1);

    let v2_program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(7)),
            Instruction::EmitAdd(10),
            Instruction::Return,
        ],
    };
    let v2 = store
        .deploy_program(&database_id, "set", 2, &v2_program)
        .unwrap();
    store
        .activate_program(&database_id, "set", Some(1), 2)
        .unwrap();
    let replay = store
        .transact_program_hash(
            &database_id,
            v1,
            "retry-key",
            0,
            &[Value::Ref(1_000), Value::Long(5)],
            1_000,
            ProgramControl::default(),
        )
        .unwrap();
    assert!(replay.commit.replayed);
    let error = store
        .transact_program(
            &database_id,
            "set",
            "retry-key",
            0,
            &[Value::Ref(1_000)],
            1_000,
            ProgramControl::default(),
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Conflict, "postgres/idempotency-key-reused")
    );

    let original = atomic_core::encode_program(&v2_program).unwrap();
    drop(store);
    let mut client = Client::connect(&connection, NoTls).unwrap();
    client
        .execute(
            "UPDATE atomic_programs SET payload = decode('00', 'hex') WHERE program_hash = $1",
            &[&&v2[..]],
        )
        .unwrap();
    let mut restarted = PostgresStore::connect(&connection).unwrap();
    let recovered = restarted.recover(&database_id).unwrap();
    assert_eq!(recovered.basis_t(), 1);
    assert_eq!(recovered.values(1_000, 10), vec![&Value::Long(5)]);
    assert!(
        !recovered
            .datoms(View::History, atomic_core::IndexOrder::Eavt)
            .is_empty()
    );
    client
        .execute(
            "UPDATE atomic_programs SET payload = $2 WHERE program_hash = $1",
            &[&&v2[..], &&original[..]],
        )
        .unwrap();
}
