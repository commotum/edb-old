mod common;

use atomic_core::{
    Attribute, CallableRef, Cardinality, DB_ATTR_PREDS, DB_FN, DB_IDENT, EntityRef, Instruction,
    Keyword, PostgresStore, Program, ProgramCall, ProgramKind, Schema, Symbol, TransactionRequest,
    TxOp, USER_PARTITION, Value, ValueType, encode_program, make_eid,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const BALANCE: u32 = 1_000;
const CACHE_ENTRY_OVERHEAD: usize = 1024 + std::mem::size_of::<[u8; 32]>();

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

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

fn schema() -> Schema {
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

fn setter() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushArgument(1),
            Instruction::EmitAdd(BALANCE),
            Instruction::Return,
        ],
    }
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

fn query_constant(value: i64) -> Program {
    Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(value)),
            Instruction::Return,
        ],
    }
}

#[test]
fn authoritative_ident_calls_and_predicates_share_decoded_programs() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("authoritative_program_cache");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut setup = PostgresStore::connect(&connection).unwrap();
    let created = setup.create_database(&database_id, schema()).unwrap();
    let setter_hash = setup.deploy_program_blob(&setter()).unwrap();
    let positive_hash = setup.deploy_program_blob(&positive()).unwrap();
    drop(setup);

    let setter_ident = Keyword::new("account", "set-balance");
    let positive_ident = Keyword::new("atomic.predicates", "positive");
    let service = common::start_service(&connection, &database_id);
    assert_eq!(service.program_cache_stats().decodes, 0);

    let installed = common::transact(
        &service,
        "install-cache-functions",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("setter".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(setter_ident.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("setter".into()),
                attribute: DB_FN as u32,
                value: Value::Function(setter_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("positive".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(positive_ident).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("positive".into()),
                attribute: DB_FN as u32,
                value: Value::Function(positive_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(u64::from(BALANCE)),
                attribute: DB_ATTR_PREDS as u32,
                value: Value::Symbol(Symbol::new("atomic.predicates", "positive")).into(),
            },
        ],
        1_000,
    );
    let after_install = service.program_cache_stats();
    assert_eq!(after_install.misses, 2);
    assert_eq!(after_install.decodes, 2);
    assert_eq!(after_install.validations, 2);
    assert_eq!(after_install.current_entries, 2);

    let call = |request_key: &str, basis_t: u64, value: i64, instant: i64| {
        service
            .client()
            .transact(
                TransactionRequest::new(request_key, vec![])
                    .calling(ProgramCall {
                        function: CallableRef::Database(EntityRef::Ident(setter_ident.clone())),
                        arguments: vec![Value::Ref(user(42)).into(), Value::Long(value).into()],
                    })
                    .comparing_basis(basis_t)
                    .with_tx_instant(instant),
                Duration::from_secs(5),
            )
            .unwrap()
    };
    let first = call("cached-call-one", installed.basis_t, 10, 2_000);
    let second = call("cached-call-two", first.basis_t, 20, 3_000);
    assert_eq!(
        second.db_after.values(user(42), BALANCE),
        vec![&Value::Long(20)]
    );

    let after_calls = service.program_cache_stats();
    assert_eq!(after_calls.decodes, after_install.decodes);
    assert_eq!(after_calls.validations, after_install.validations);
    assert_eq!(after_calls.misses, after_install.misses);
    assert_eq!(after_calls.hits - after_install.hits, 4);
    assert_eq!(after_calls.current_entries, 2);
    service.shutdown();
}

#[test]
fn cache_evicts_by_canonical_payload_bytes_and_preserves_lru_order() {
    let Some(connection) = connection() else {
        return;
    };
    let seed = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos() as i64;
    let one_program = query_constant(seed);
    let two_program = query_constant(seed.wrapping_add(1));
    let one_bytes = encode_program(&one_program).unwrap().len() + CACHE_ENTRY_OVERHEAD;
    let two_bytes = encode_program(&two_program).unwrap().len() + CACHE_ENTRY_OVERHEAD;

    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut deployer = PostgresStore::connect(&connection).unwrap();
    let one = deployer.deploy_program_blob(&one_program).unwrap();
    let two = deployer.deploy_program_blob(&two_program).unwrap();
    drop(deployer);

    let mut resolver = PostgresStore::connect(&connection).unwrap();
    resolver.set_program_cache_limits(8, one_bytes.max(two_bytes));
    assert_eq!(resolver.resolve_program(one).unwrap(), one_program);
    assert_eq!(resolver.resolve_program(two).unwrap(), two_program);
    let after_fill = resolver.program_cache_stats();
    assert_eq!(after_fill.misses, 2);
    assert_eq!(after_fill.decodes, 2);
    assert_eq!(after_fill.validations, 2);
    assert_eq!(after_fill.evictions, 1);
    assert_eq!(after_fill.current_entries, 1);
    assert_eq!(after_fill.current_bytes, two_bytes);

    // The resident second program is a hit; loading the first again evicts
    // the least-recently-used second entry and performs exactly one decode.
    resolver.resolve_program(two).unwrap();
    resolver.resolve_program(one).unwrap();
    let after_reload = resolver.program_cache_stats();
    assert_eq!(after_reload.hits, 1);
    assert_eq!(after_reload.misses, 3);
    assert_eq!(after_reload.decodes, 3);
    assert_eq!(after_reload.validations, 3);
    assert_eq!(after_reload.evictions, 2);
    assert_eq!(after_reload.current_entries, 1);
    assert_eq!(after_reload.current_bytes, one_bytes);
}
