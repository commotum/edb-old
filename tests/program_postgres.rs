#[allow(dead_code)]
mod common;

use atomic_core::{ErrorCategory, Instruction, PostgresStore, Program, ProgramKind, Value};
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
