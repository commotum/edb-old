use atomic_core::{
    Attribute, CapacityLimits, Cardinality, EntityRef, ErrorCategory, Keyword, PostgresStore,
    Schema, TransactionRequest, TransactionService, TransactionServiceConfig, TxOp, TxValue,
    USER_PARTITION, Value, ValueType, make_eid,
};
use postgres::{Client, NoTls};
use std::sync::{Arc, Barrier};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const ITEM_COUNT: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
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
            ITEM_COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn set(value: i64) -> Vec<TxOp> {
    vec![TxOp::Add {
        entity: EntityRef::Id(user(42)),
        attribute: ITEM_COUNT,
        value: TxValue::Scalar(Value::Long(value)),
    }]
}

fn config(connection: &str, database_id: &str, holder_id: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: holder_id.to_owned(),
        lease_duration: Duration::from_secs(1),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    }
}

fn lease_row(client: &mut Client, database_id: &str) -> (String, u64) {
    let row = client
        .query_one(
            "SELECT holder_id, epoch FROM atomic_transactor_leases WHERE lease_scope = $1",
            &[&database_id],
        )
        .unwrap();
    let epoch: i64 = row.get(1);
    (row.get(0), epoch.try_into().unwrap())
}

#[test]
fn service_owns_the_lease_and_takeover_advances_its_epoch() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("fenced_db");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut setup = PostgresStore::connect(&connection).unwrap();
    let initial_basis = setup
        .create_database(&database_id, schema())
        .unwrap()
        .basis_t();
    drop(setup);

    let first = TransactionService::start(config(&connection, &database_id, "first")).unwrap();
    let first_client = first.client();
    let mut observer = Client::connect(&connection, NoTls).unwrap();
    assert_eq!(lease_row(&mut observer, &database_id), ("first".into(), 1));

    let duplicate = match TransactionService::start(config(&connection, &database_id, "first")) {
        Ok(service) => {
            service.shutdown();
            panic!("a second service acquired an already-live database lease")
        }
        Err(error) => error,
    };
    assert_eq!(
        (duplicate.category, duplicate.code),
        (ErrorCategory::Unavailable, "postgres/lease-held")
    );
    let held = match TransactionService::start(config(&connection, &database_id, "second")) {
        Ok(service) => {
            service.shutdown();
            panic!("a competing service acquired an already-live database lease")
        }
        Err(error) => error,
    };
    assert_eq!(
        (held.category, held.code),
        (ErrorCategory::Unavailable, "postgres/lease-held")
    );

    let one = first_client
        .transact(
            TransactionRequest::new("one", set(1)),
            Duration::from_secs(2),
        )
        .unwrap();
    assert_eq!(one.basis_t, initial_basis + 1);
    first.shutdown();
    assert_eq!(
        first_client
            .submit(TransactionRequest::new("stale", set(2)))
            .unwrap_err()
            .code,
        "service/unavailable"
    );

    let second = TransactionService::start(config(&connection, &database_id, "second")).unwrap();
    assert_eq!(lease_row(&mut observer, &database_id), ("second".into(), 2));
    let two = second
        .client()
        .transact(
            TransactionRequest::new("two", set(2)),
            Duration::from_secs(2),
        )
        .unwrap();
    assert_eq!(two.basis_t, initial_basis + 2);
    assert_eq!(
        two.db_after.values(user(42), ITEM_COUNT).unwrap(),
        vec![Value::Long(2)]
    );
    second.shutdown();
}

#[test]
fn a_database_bound_service_cannot_redirect_a_request_to_another_database() {
    let Some(connection) = connection() else {
        return;
    };
    let database_a = unique("lease_bound_a");
    let database_b = unique("lease_bound_b");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let initial_a = store
        .create_database(&database_a, schema())
        .unwrap()
        .basis_t();
    let initial_b = store
        .create_database(&database_b, schema())
        .unwrap()
        .basis_t();

    // TransactionRequest deliberately has no database selector. The only
    // mutation target is the database captured when this service starts.
    let service = TransactionService::start(config(&connection, &database_a, "leader-a")).unwrap();
    let committed = service
        .client()
        .transact(
            TransactionRequest::new("bound-request", set(7)),
            Duration::from_secs(2),
        )
        .unwrap();
    assert_eq!(committed.basis_t, initial_a + 1);
    assert_eq!(store.recover(&database_a).unwrap().basis_t(), initial_a + 1);
    assert_eq!(store.recover(&database_b).unwrap().basis_t(), initial_b);
    service.shutdown();
}

#[test]
fn concurrent_candidates_have_exactly_one_higher_epoch_winner() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("lease_race");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut setup = PostgresStore::connect(&connection).unwrap();
    setup.create_database(&database_id, schema()).unwrap();
    let mut observer = Client::connect(&connection, NoTls).unwrap();
    observer
        .execute(
            "INSERT INTO atomic_transactor_leases \
             (lease_scope, holder_id, epoch, expires_at) \
             VALUES ($1, 'expired', 1, clock_timestamp() - interval '1 second')",
            &[&database_id],
        )
        .unwrap();

    let barrier = Arc::new(Barrier::new(3));
    let mut handles = Vec::new();
    for holder in ["candidate-a", "candidate-b"] {
        let connection = connection.clone();
        let database_id = database_id.clone();
        let barrier = barrier.clone();
        handles.push(std::thread::spawn(move || {
            barrier.wait();
            TransactionService::start(config(&connection, &database_id, holder))
        }));
    }
    barrier.wait();
    let results: Vec<_> = handles
        .into_iter()
        .map(|handle| handle.join().unwrap())
        .collect();
    assert_eq!(results.iter().filter(|result| result.is_ok()).count(), 1);
    assert_eq!(
        results
            .iter()
            .filter_map(|result| result.as_ref().err())
            .next()
            .unwrap()
            .code,
        "postgres/lease-held"
    );
    let (holder, epoch) = lease_row(&mut observer, &database_id);
    assert!(["candidate-a", "candidate-b"].contains(&holder.as_str()));
    assert_eq!(epoch, 2);
    for service in results.into_iter().flatten() {
        service.shutdown();
    }
}
