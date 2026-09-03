use atomic_core::{
    Attribute, Cardinality, EntityRef, ErrorCategory, Keyword, PostgresStore, Schema, TxOp,
    TxValue, Value, ValueType,
};
use std::sync::{Arc, Barrier};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

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
        .install(Attribute::new(
            10,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn set(value: i64) -> Vec<TxOp> {
    vec![TxOp::Add {
        entity: EntityRef::Id(1_000),
        attribute: 10,
        value: TxValue::Scalar(Value::Long(value)),
    }]
}

#[test]
fn lease_uses_server_expiry_monotonic_epochs_and_fences_publication() {
    let Some(connection) = connection() else {
        return;
    };
    let scope = unique("lease");
    let database_id = unique("fenced_db");
    let mut first = PostgresStore::connect(&connection).unwrap();
    first.migrate().unwrap();
    first.create_database(&database_id, schema()).unwrap();
    let lease1 = first.acquire_lease(&scope, "first", 1_000).unwrap();
    assert_eq!(lease1.epoch, 1);
    let mut second = PostgresStore::connect(&connection).unwrap();
    let held = second.acquire_lease(&scope, "second", 1_000).unwrap_err();
    assert_eq!(
        (held.category, held.code),
        (ErrorCategory::Unavailable, "postgres/lease-held")
    );
    first.renew_lease(&lease1, 1_000).unwrap();
    first
        .transact_fenced(&lease1, &database_id, "one", 0, &set(1), 1_000)
        .unwrap();
    first.release_lease(&lease1).unwrap();
    let lease2 = second.acquire_lease(&scope, "second", 1_000).unwrap();
    assert_eq!(lease2.epoch, 2);

    let stale = first
        .transact_fenced(&lease1, &database_id, "stale", 1, &set(2), 2_000)
        .unwrap_err();
    assert_eq!(
        (stale.category, stale.code),
        (ErrorCategory::Unavailable, "postgres/leadership-lost")
    );
    let committed = second
        .transact_fenced(&lease2, &database_id, "two", 1, &set(2), 2_000)
        .unwrap();
    assert_eq!(committed.basis_t, 2);
    assert_eq!(committed.database.values(1_000, 10), vec![&Value::Long(2)]);
}

#[test]
fn expired_and_concurrent_candidates_have_exactly_one_higher_epoch_winner() {
    let Some(connection) = connection() else {
        return;
    };
    let scope = unique("lease_race");
    let mut setup = PostgresStore::connect(&connection).unwrap();
    setup.migrate().unwrap();
    let expired = setup.acquire_lease(&scope, "old", 50).unwrap();
    std::thread::sleep(Duration::from_millis(100));
    assert_eq!(
        setup.renew_lease(&expired, 1_000).unwrap_err().code,
        "postgres/leadership-lost"
    );

    let barrier = Arc::new(Barrier::new(3));
    let mut handles = Vec::new();
    for holder in ["candidate-a", "candidate-b"] {
        let connection = connection.clone();
        let scope = scope.clone();
        let barrier = barrier.clone();
        handles.push(std::thread::spawn(move || {
            let mut store = PostgresStore::connect(&connection).unwrap();
            barrier.wait();
            store.acquire_lease(&scope, holder, 1_000)
        }));
    }
    barrier.wait();
    let results: Vec<_> = handles
        .into_iter()
        .map(|handle| handle.join().unwrap())
        .collect();
    assert_eq!(results.iter().filter(|result| result.is_ok()).count(), 1);
    let winner = results.into_iter().find_map(Result::ok).unwrap();
    assert_eq!(winner.epoch, expired.epoch + 1);
}
