mod common;

use atomic_core::{
    Attribute, Cardinality, DB_ATTR_PREDS, DB_ENTITY_PREDS, DB_FN, DB_IDENT, EntityRef,
    ErrorCategory, Instruction, Keyword, PostgresStore, Program, ProgramKind, Schema, Symbol, TxOp,
    TxValue, USER_PARTITION, Value, ValueType, make_eid,
};
use postgres::{Client, NoTls};
use std::time::{SystemTime, UNIX_EPOCH};

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn constant(value: i64) -> Program {
    Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(value)),
            Instruction::Return,
        ],
    }
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

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

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
fn immutable_blob_deploy_restart_and_bounded_cache() {
    let Some(connection) = connection() else {
        return;
    };
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let one = store.deploy_program_blob(&constant(101)).unwrap();
    assert_eq!(store.deploy_program_blob(&constant(101)).unwrap(), one);
    let two = store.deploy_program_blob(&constant(102)).unwrap();
    drop(store);
    let mut restarted = PostgresStore::connect(&connection).unwrap();
    assert_eq!(restarted.resolve_program(one).unwrap(), constant(101));
    restarted.set_program_cache_capacity(1);
    restarted.resolve_program(one).unwrap();
    restarted.resolve_program(two).unwrap();
    assert_eq!(restarted.cached_programs(), 1);
}

#[test]
fn program_content_rows_reject_normal_mutation_and_deploy_replays() {
    let Some(connection) = connection() else {
        return;
    };
    let program = constant(
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos() as i64,
    );
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let hash = store.deploy_program_blob(&program).unwrap();
    assert_eq!(store.deploy_program_blob(&program).unwrap(), hash);

    let mut client = Client::connect(&connection, NoTls).unwrap();
    for statement in [
        "UPDATE atomic_programs SET program_hash = program_hash WHERE program_hash = $1",
        "UPDATE atomic_programs SET kind = kind WHERE program_hash = $1",
        "UPDATE atomic_programs SET arity = arity WHERE program_hash = $1",
        "UPDATE atomic_programs SET payload = payload WHERE program_hash = $1",
        "DELETE FROM atomic_programs WHERE program_hash = $1",
    ] {
        let error = client.execute(statement, &[&&hash[..]]).unwrap_err();
        assert_eq!(error.as_db_error().unwrap().code().code(), "55000");
    }
    let truncate = client
        .batch_execute("TRUNCATE TABLE atomic_programs")
        .unwrap_err();
    // PostgreSQL may reject a referenced table before reaching its explicit
    // truncate trigger; either route preserves the physical invariant.
    assert!(matches!(
        truncate.as_db_error().map(|error| error.code().code()),
        Some("55000" | "0A000")
    ));
    let trigger: String = client
        .query_one(
            "SELECT pg_get_triggerdef(oid) FROM pg_trigger \
             WHERE tgrelid = 'atomic_programs'::regclass \
               AND tgname = 'atomic_programs_reject_truncate' \
               AND NOT tgisinternal",
            &[],
        )
        .unwrap()
        .get(0);
    assert!(trigger.contains("BEFORE TRUNCATE"));
    assert_eq!(
        client
            .query_one(
                "SELECT count(*) FROM atomic_programs WHERE program_hash = $1",
                &[&&hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
        1
    );
}

#[test]
fn corrupt_persisted_program_fails_closed() {
    let Some(connection) = connection() else {
        return;
    };
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let program = constant(
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos() as i64,
    );
    let original = atomic_core::encode_program(&program).unwrap();
    let hash = store.deploy_program_blob(&program).unwrap();
    drop(store);

    let mut client = Client::connect(&connection, NoTls).unwrap();
    common::with_replica_triggers_disabled(&mut client, |client| {
        client.execute(
            "UPDATE atomic_programs SET payload = payload || decode('00', 'hex') \
             WHERE program_hash = $1",
            &[&&hash[..]],
        )
    })
    .unwrap();
    let mut restarted = PostgresStore::connect(&connection).unwrap();
    let error = restarted.resolve_program(hash).unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Fault, "postgres/program-hash-mismatch")
    );
    common::with_replica_triggers_disabled(&mut client, |client| {
        client.execute(
            "UPDATE atomic_programs SET payload = $2 WHERE program_hash = $1",
            &[&&hash[..], &&original[..]],
        )
    })
    .unwrap();
}

#[test]
fn persisted_symbol_serves_both_predicate_roles_after_install_and_restart() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("dual_predicate");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store
        .create_database(&database_id, predicate_schema())
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

    let service = common::start_service(&connection, &database_id);
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
                entity: EntityRef::Id(user(42)),
                attribute: BALANCE,
                value: Value::Long(-1).into(),
            },
            TxOp::Ensure {
                entity: EntityRef::Id(user(42)),
                spec: EntityRef::Temp("guard".into()),
            },
        ],
        1_000,
    );
    let guard = installed.tempids["guard"];

    let accepted = common::transact(
        &service,
        "dual-both-pass",
        installed.basis_t,
        &[
            TxOp::Add {
                entity: EntityRef::Id(user(42)),
                attribute: BALANCE,
                value: Value::Long(7).into(),
            },
            TxOp::Ensure {
                entity: EntityRef::Id(user(42)),
                spec: EntityRef::Id(guard),
            },
        ],
        2_000,
    );
    assert_eq!(
        accepted.db_after.values(user(42), BALANCE).unwrap(),
        vec![Value::Long(7)]
    );

    let attribute_error = common::try_transact(
        &service,
        "dual-attribute-fails",
        accepted.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
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
                entity: EntityRef::Id(user(42)),
                attribute: BALANCE,
                value: Value::Long(8).into(),
            },
            TxOp::Ensure {
                entity: EntityRef::Id(user(42)),
                spec: EntityRef::Id(guard),
            },
        ],
        4_000,
    )
    .unwrap_err();
    assert_eq!(entity_error.code, "transaction/entity-predicate");
    service.shutdown();

    let restarted = common::start_service(&connection, &database_id);
    let after_restart = common::transact(
        &restarted,
        "dual-after-restart",
        accepted.basis_t,
        &[
            TxOp::Add {
                entity: EntityRef::Id(user(43)),
                attribute: BALANCE,
                value: TxValue::Scalar(Value::Long(7)),
            },
            TxOp::Ensure {
                entity: EntityRef::Id(user(43)),
                spec: EntityRef::Id(guard),
            },
        ],
        5_000,
    );
    assert_eq!(
        after_restart.db_after.values(user(43), BALANCE).unwrap(),
        vec![Value::Long(7)]
    );
    restarted.shutdown();
}
