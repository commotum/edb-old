use atomic_core::{
    Attribute, Cardinality, ErrorCategory, Instruction, Keyword, PostgresStore, Program,
    ProgramKind, Schema, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::sync::{Arc, Barrier};
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

#[test]
fn immutable_deploy_activation_restart_and_bounded_cache() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("program_registry");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    store.create_database(&database_id, schema()).unwrap();

    let one = store
        .deploy_program(&database_id, "answer", 1, &constant(101))
        .unwrap();
    assert_eq!(
        store
            .deploy_program(&database_id, "answer", 1, &constant(101))
            .unwrap(),
        one
    );
    let error = store
        .deploy_program(&database_id, "answer", 1, &constant(102))
        .unwrap_err();
    assert_eq!(error.code, "postgres/program-version-immutable");
    assert_eq!(
        store
            .activate_program(&database_id, "answer", None, 1)
            .unwrap(),
        one
    );
    assert_eq!(
        store
            .activate_program(&database_id, "answer", None, 1)
            .unwrap(),
        one
    );

    let two = store
        .deploy_program(&database_id, "answer", 2, &constant(102))
        .unwrap();
    drop(store);
    let mut restarted = PostgresStore::connect(&connection).unwrap();
    let (version, hash, program) = restarted
        .resolve_active_program(&database_id, "answer")
        .unwrap();
    assert_eq!((version, hash, program), (1, one, constant(101)));
    restarted.set_program_cache_capacity(1);
    restarted.resolve_program(one).unwrap();
    restarted.resolve_program(two).unwrap();
    assert_eq!(restarted.cached_programs(), 1);
}

#[test]
fn concurrent_conditional_activation_has_one_winner() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("program_race");
    let mut setup = PostgresStore::connect(&connection).unwrap();
    setup.migrate().unwrap();
    setup.create_database(&database_id, schema()).unwrap();
    for version in 1..=3 {
        setup
            .deploy_program(
                &database_id,
                "choice",
                version,
                &constant(200 + version as i64),
            )
            .unwrap();
    }
    setup
        .activate_program(&database_id, "choice", None, 1)
        .unwrap();

    let barrier = Arc::new(Barrier::new(3));
    let mut handles = Vec::new();
    for version in [2, 3] {
        let connection = connection.clone();
        let database_id = database_id.clone();
        let barrier = barrier.clone();
        handles.push(std::thread::spawn(move || {
            let mut store = PostgresStore::connect(&connection).unwrap();
            barrier.wait();
            store.activate_program(&database_id, "choice", Some(1), version)
        }));
    }
    barrier.wait();
    let results: Vec<_> = handles
        .into_iter()
        .map(|handle| handle.join().unwrap())
        .collect();
    assert_eq!(results.iter().filter(|result| result.is_ok()).count(), 1);
    let error = results.into_iter().find_map(Result::err).unwrap();
    assert_eq!(
        (error.category, error.code),
        (
            ErrorCategory::Conflict,
            "postgres/program-activation-conflict"
        )
    );
}

#[test]
fn corrupt_persisted_program_fails_closed() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("program_corrupt");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    store.create_database(&database_id, schema()).unwrap();
    let program = constant(
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos() as i64,
    );
    let original = atomic_core::encode_program(&program).unwrap();
    let hash = store
        .deploy_program(&database_id, "bad", 1, &program)
        .unwrap();
    store
        .activate_program(&database_id, "bad", None, 1)
        .unwrap();
    drop(store);

    let mut client = Client::connect(&connection, NoTls).unwrap();
    client
        .execute(
            "UPDATE atomic_programs SET payload = payload || decode('00', 'hex') \
             WHERE program_hash = $1",
            &[&&hash[..]],
        )
        .unwrap();
    let mut restarted = PostgresStore::connect(&connection).unwrap();
    let error = restarted
        .resolve_active_program(&database_id, "bad")
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Fault, "postgres/program-hash-mismatch")
    );
    client
        .execute(
            "UPDATE atomic_programs SET payload = $2 WHERE program_hash = $1",
            &[&&hash[..], &&original[..]],
        )
        .unwrap();
}
