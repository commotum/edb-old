//! Persisted dual predicate roles on the current opaque store.
mod common;
use atomic_core::*;
const BALANCE: u32 = 1_000;

fn predicate_schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            BALANCE,
            Keyword::new("account", "balance"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn shared_predicate() -> Program {
    Program {
        kind: ProgramKind::DualPredicate,
        arity: 1,
        instructions: vec![
            Instruction::PredicateDispatch {
                attribute: vec![
                    Instruction::PushArgument(0),
                    Instruction::PushConstant(Value::Long(0)),
                    Instruction::GreaterThan,
                ],
                entity: vec![
                    Instruction::PushArgument(0),
                    Instruction::LoadOne(BALANCE),
                    Instruction::PushConstant(Value::Long(7)),
                    Instruction::Equal,
                ],
            },
            Instruction::Return,
        ],
    }
}

#[test]
fn persisted_symbol_serves_both_predicate_roles_after_install_and_restart() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "dual_predicate");
    let connection = fixture.connection.clone();
    let database_id = "dual-predicate";
    common::blocks::install(&connection).unwrap();
    let mut store = common::blocks::TestStore::connect(&connection).unwrap();
    let created = store
        .create_database(database_id, predicate_schema())
        .unwrap();

    // The dual representation is not permission widening: a database read in
    // its attribute body is rejected before content can be deployed.
    let illegal = Program {
        kind: ProgramKind::DualPredicate,
        arity: 1,
        instructions: vec![
            Instruction::PredicateDispatch {
                attribute: vec![Instruction::PushArgument(0), Instruction::LoadOne(BALANCE)],
                entity: vec![Instruction::PushConstant(Value::Bool(true))],
            },
            Instruction::Return,
        ],
    };
    assert_eq!(
        store.deploy_program_blob(&illegal).unwrap_err().code,
        "program/predicate-database-read"
    );
    let hash = store.deploy_program_blob(&shared_predicate()).unwrap();
    drop(store);

    let service = common::start_service(&connection, database_id);
    let symbol = Symbol::new("test.predicates", "shared");
    let guard_ident = Keyword::new("test", "guard");
    let installed = common::transact(
        &service,
        "install-dual-predicate",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("function".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("test.predicates", "shared")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("function".into()),
                attribute: DB_FN as u32,
                value: Value::Function(hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(u64::from(BALANCE)),
                attribute: DB_ATTR_PREDS as u32,
                value: Value::Symbol(symbol.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(guard_ident).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_ENTITY_PREDS as u32,
                value: Value::Symbol(symbol).into(),
            },
            // Both new declarations are deliberately false for this data.
            // They start with the next transaction, as in recovered db.clj.
            TxOp::Add {
                entity: EntityRef::Temp("account".into()),
                attribute: BALANCE,
                value: Value::Long(-1).into(),
            },
            TxOp::Ensure {
                entity: EntityRef::Temp("account".into()),
                spec: EntityRef::Temp("guard".into()),
            },
        ],
        1_000,
    );
    let guard = installed.tempids["guard"];
    let account = installed.tempids["account"];

    let accepted = common::transact(
        &service,
        "dual-both-pass",
        installed.basis_t,
        &[
            TxOp::Add {
                entity: EntityRef::Id(account),
                attribute: BALANCE,
                value: Value::Long(7).into(),
            },
            TxOp::Ensure {
                entity: EntityRef::Id(account),
                spec: EntityRef::Id(guard),
            },
        ],
        2_000,
    );
    assert_eq!(
        accepted.db_after.values(account, BALANCE).unwrap(),
        vec![Value::Long(7)]
    );

    let attribute_error = common::try_transact(
        &service,
        "dual-attribute-fails",
        accepted.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(account),
            attribute: BALANCE,
            value: Value::Long(-2).into(),
        }],
        3_000,
    )
    .unwrap_err();
    assert_eq!(attribute_error.code, "transaction/attribute-predicate");

    let entity_error = common::try_transact(
        &service,
        "dual-entity-fails",
        accepted.basis_t,
        &[
            TxOp::Add {
                entity: EntityRef::Id(account),
                attribute: BALANCE,
                value: Value::Long(8).into(),
            },
            TxOp::Ensure {
                entity: EntityRef::Id(account),
                spec: EntityRef::Id(guard),
            },
        ],
        4_000,
    )
    .unwrap_err();
    assert_eq!(entity_error.code, "transaction/entity-predicate");
    service.shutdown();

    let restarted = common::start_service(&connection, database_id);
    let after_restart = common::transact(
        &restarted,
        "dual-after-restart",
        accepted.basis_t,
        &[
            TxOp::Add {
                entity: EntityRef::Temp("other-account".into()),
                attribute: BALANCE,
                value: TxValue::Scalar(Value::Long(7)),
            },
            TxOp::Ensure {
                entity: EntityRef::Temp("other-account".into()),
                spec: EntityRef::Id(guard),
            },
        ],
        5_000,
    );
    assert_eq!(
        after_restart
            .db_after
            .values(after_restart.tempids["other-account"], BALANCE)
            .unwrap(),
        vec![Value::Long(7)]
    );
    restarted.shutdown();
}
