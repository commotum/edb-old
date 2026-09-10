use atomic_core::{
    ErrorCategory, Instruction, MAX_PROGRAMS_PER_GC, PostgresMigrator, PostgresOperator, Program,
    ProgramKind, SemanticError, Value, encode_program, sha256,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant};

mod common;

fn gc<T>(mut operation: impl FnMut() -> Result<T, SemanticError>) -> T {
    let deadline = Instant::now() + Duration::from_secs(10);
    loop {
        match operation() {
            Ok(value) => return value,
            Err(error) if error.category == ErrorCategory::Busy && Instant::now() < deadline => {
                std::thread::sleep(Duration::from_millis(10));
            }
            Err(error) => panic!("{error:?}"),
        }
    }
}

#[test]
fn program_gc_compares_membership_without_changing_oldest_first_batch_order() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP program GC ordering: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "program_gc_order");
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut client = Client::connect(&fixture.connection, NoTls).unwrap();
    let mut programs = (0..MAX_PROGRAMS_PER_GC + 2)
        .map(|value| {
            let payload = encode_program(&Program {
                kind: ProgramKind::Query,
                arity: 0,
                instructions: vec![
                    Instruction::PushConstant(Value::Long(value as i64)),
                    Instruction::Return,
                ],
            })
            .unwrap();
            (sha256(&payload), payload)
        })
        .collect::<Vec<_>>();
    // Deliberately opposite to hash order: changing the query's ORDER BY before
    // LIMIT would select a different batch and must not repair the assertion.
    programs.sort_unstable_by(|left, right| right.0.cmp(&left.0));
    let mut transaction = client.transaction().unwrap();
    for (position, (hash, payload)) in programs.iter().enumerate() {
        transaction
            .execute(
                "INSERT INTO atomic_programs (program_hash, kind, arity, payload) \
                 VALUES ($1, 2, 0, $2)",
                &[&&hash[..], &payload],
            )
            .unwrap();
        if position <= MAX_PROGRAMS_PER_GC {
            transaction
                .execute(
                    "UPDATE atomic_program_gc_candidates \
                     SET candidate_at = now() - interval '31 days' \
                         + $2::bigint * interval '1 second' WHERE program_hash = $1",
                    &[&&hash[..], &(position as i64)],
                )
                .unwrap();
        }
    }
    transaction.commit().unwrap();
    let mut operator = PostgresOperator::connect(&fixture.connection).unwrap();
    let horizon = Duration::from_secs(30 * 24 * 60 * 60);
    let mut preview = gc(|| operator.garbage_inventory(horizon));
    let oldest = programs[..MAX_PROGRAMS_PER_GC]
        .iter()
        .map(|(hash, _)| *hash)
        .collect::<Vec<_>>();
    assert_eq!(preview.program_hashes, oldest);
    preview.applied = true;
    assert_eq!(gc(|| operator.collect_garbage(horizon)), preview);
    assert_eq!(
        client
            .query_one("SELECT count(*) FROM atomic_programs", &[])
            .unwrap()
            .get::<_, i64>(0),
        2,
    );
    assert_eq!(
        gc(|| operator.collect_garbage(horizon)).program_hashes,
        vec![programs[MAX_PROGRAMS_PER_GC].0],
    );
    // The unaged orphan remains in storage, and an empty collection is safe.
    assert!(
        gc(|| operator.collect_garbage(horizon))
            .program_hashes
            .is_empty()
    );
    let remaining = client
        .query("SELECT program_hash FROM atomic_programs", &[])
        .unwrap();
    assert_eq!(remaining.len(), 1);
    assert_eq!(
        remaining[0].get::<_, Vec<u8>>(0),
        programs[MAX_PROGRAMS_PER_GC + 1].0
    );
    println!(
        "PROGRAM_GC_ORDER_OK oldest_batch={} aged_remainder=1 young_retained=1",
        MAX_PROGRAMS_PER_GC
    );
}
