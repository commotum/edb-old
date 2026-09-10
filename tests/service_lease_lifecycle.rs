//! A live transaction owns its lease row continuously, even past wall expiry.
//! Hold the subsequent head lock to test this without workload/timing guesses.
mod common;

use atomic_core::{
    Attribute, CapacityLimits, Cardinality, EntityRef, ErrorCategory, IndexOrder, Keyword,
    PostgresMigrator, PostgresStore, Schema, SemanticError, ServiceTransactionReport,
    TransactionClient, TransactionRequest, TransactionService, TransactionServiceConfig, TxOp,
    Value, ValueType,
};
use postgres::{Client, NoTls};
use std::thread;
use std::time::{Duration, Instant};

const COUNT: u32 = 1_000;
const WAIT: Duration = Duration::from_secs(10);

fn config(connection: &str, database: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.into(),
        database_id: database.into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(1),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    }
}

fn setup(connection: &str, database: &str) -> u64 {
    PostgresMigrator::connect(connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            COUNT,
            Keyword::new("lease", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    PostgresStore::connect(connection)
        .unwrap()
        .create_database(database, schema)
        .unwrap()
        .basis_t()
}

fn request(key: &str, entity: EntityRef, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity,
            attribute: COUNT,
            value: Value::Long(value).into(),
        }],
    )
}

fn wait_blocked(observer: &mut Client, blocker: i32, query_fragment: &str) -> i32 {
    let deadline = Instant::now() + WAIT;
    loop {
        if let Some(row) = observer
            .query_opt(
                "SELECT pid FROM pg_stat_activity \
                 WHERE $1 = ANY(pg_blocking_pids(pid)) \
                   AND position($2 in query) > 0 LIMIT 1",
                &[&blocker, &query_fragment],
            )
            .unwrap()
        {
            return row.get(0);
        }
        assert!(
            Instant::now() < deadline,
            "expected blocked {query_fragment}"
        );
        thread::sleep(Duration::from_millis(5));
    }
}

fn lease(observer: &mut Client, database: &str) -> (String, i64, bool) {
    let row = observer
        .query_one(
            "SELECT holder_id, epoch, expires_at > clock_timestamp() \
             FROM atomic_transactor_leases WHERE lease_scope = $1",
            &[&database],
        )
        .unwrap();
    (row.get(0), row.get(1), row.get(2))
}

fn delayed_request(
    connection: &str,
    database: &str,
    client: &TransactionClient,
    request: TransactionRequest,
    competing_candidate: bool,
) -> Result<ServiceTransactionReport, SemanticError> {
    let mut blocker = Client::connect(connection, NoTls).unwrap();
    let blocker_pid: i32 = blocker
        .query_one("SELECT pg_backend_pid()", &[])
        .unwrap()
        .get(0);
    let mut held = blocker.transaction().unwrap();
    held.query_one(
        "SELECT basis_t FROM atomic_heads WHERE database_id = $1 FOR UPDATE",
        &[&database],
    )
    .unwrap();
    let ticket = client.submit(request).unwrap();
    let mut observer = Client::connect(connection, NoTls).unwrap();
    // Head locking follows successful live-lease validation in the same SQL
    // transaction. This wait proves the writer owns the fence before expiry.
    let worker_pid = wait_blocked(&mut observer, blocker_pid, "FOR UPDATE OF h");
    let deadline = Instant::now() + WAIT;
    while lease(&mut observer, database).2 {
        assert!(
            Instant::now() < deadline,
            "blocked transaction lease never expired"
        );
        thread::sleep(Duration::from_millis(5));
    }
    let candidate = competing_candidate.then(|| {
        let config = config(connection, database, "candidate");
        let candidate = thread::spawn(move || TransactionService::start(config));
        // The timestamp is expired, but a takeover must still wait behind the
        // continuously held lease row. It must observe the renewed epoch later.
        wait_blocked(&mut observer, worker_pid, "atomic_transactor_leases");
        candidate
    });
    held.commit().unwrap();
    let result = ticket.wait(WAIT);
    if let Some(candidate) = candidate {
        match candidate.join().unwrap() {
            Ok(service) => {
                service.shutdown();
                panic!("candidate stole a continuously locked, successful writer epoch");
            }
            Err(error) => assert_eq!(error.code, "postgres/lease-held"),
        }
    }
    result
}

#[test]
fn slow_fresh_commit_and_exact_replay_keep_the_configured_writer_epoch_live() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED lease lifecycle PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "lease_lifecycle");
    const DATABASE: &str = "lease_lifecycle";
    let basis = setup(&fixture.connection, DATABASE);
    let service =
        TransactionService::start(config(&fixture.connection, DATABASE, "owner")).unwrap();
    let client = service.client();
    let mut observer = Client::connect(&fixture.connection, NoTls).unwrap();
    let initial_epoch = lease(&mut observer, DATABASE).1;
    let original = request("original", EntityRef::Temp("item".into()), 1).comparing_basis(basis);
    let first = delayed_request(
        &fixture.connection,
        DATABASE,
        &client,
        original.clone(),
        true,
    )
    .unwrap();
    assert!(!first.replayed);
    assert_eq!(
        lease(&mut observer, DATABASE),
        ("owner".into(), initial_epoch, true)
    );
    let entity = first.tempids["item"];
    let second = client
        .transact(request("next", EntityRef::Id(entity), 2), WAIT)
        .unwrap();
    assert_eq!(second.basis_t, first.basis_t + 1);

    // Retry the old request with its now-stale compare-basis after forcing the
    // outcome-read branch itself to outlast the lease duration.
    let replay = delayed_request(&fixture.connection, DATABASE, &client, original, false).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, first.basis_t);
    assert_eq!(replay.tx_hash, first.tx_hash);
    assert_eq!(replay.tempids, first.tempids);
    assert_eq!(replay.tx_data, first.tx_data);
    for (left, right) in [
        (&replay.db_before, &first.db_before),
        (&replay.db_after, &first.db_after),
    ] {
        assert_eq!(
            left.datoms(IndexOrder::Eavt).unwrap(),
            right.datoms(IndexOrder::Eavt).unwrap()
        );
    }
    assert_eq!(
        lease(&mut observer, DATABASE),
        ("owner".into(), initial_epoch, true)
    );
    let third = client
        .transact(request("after-replay", EntityRef::Id(entity), 3), WAIT)
        .unwrap();
    assert_eq!(third.basis_t, second.basis_t + 1);
    assert!(client.is_available());
    service.shutdown();
    assert_eq!(
        client
            .submit(request("stale", EntityRef::Id(entity), 4))
            .unwrap_err()
            .code,
        "service/unavailable"
    );

    let replacement =
        TransactionService::start(config(&fixture.connection, DATABASE, "replacement")).unwrap();
    assert_eq!(
        lease(&mut observer, DATABASE),
        ("replacement".into(), initial_epoch + 1, true)
    );
    let fourth = replacement
        .client()
        .transact(request("replacement", EntityRef::Id(entity), 4), WAIT)
        .unwrap();
    assert_eq!(fourth.basis_t, third.basis_t + 1);
    replacement.shutdown();
    eprintln!(
        "LEASE_LIFECYCLE slow_fresh=true slow_replay=true exact_receipt=true next_request=true fenced_takeover=true lease_ms=1000"
    );
}

#[test]
fn failed_slow_transaction_does_not_revive_an_expired_epoch() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED lease rollback PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "lease_rollback");
    const DATABASE: &str = "lease_rollback";
    setup(&fixture.connection, DATABASE);
    let service =
        TransactionService::start(config(&fixture.connection, DATABASE, "owner")).unwrap();
    let client = service.client();
    let first = client
        .transact(request("seed", EntityRef::Temp("item".into()), 1), WAIT)
        .unwrap();
    let entity = first.tempids["item"];
    let rejected = TransactionRequest::new(
        "rejected",
        vec![TxOp::Cas {
            entity: EntityRef::Id(entity),
            attribute: COUNT,
            old: Some(Value::Long(999).into()),
            new: Value::Long(2).into(),
        }],
    );
    let error =
        delayed_request(&fixture.connection, DATABASE, &client, rejected, false).unwrap_err();
    assert_eq!(error.code, "transaction/cas-failed");
    let deadline = Instant::now() + WAIT;
    while client.is_available() {
        assert!(
            Instant::now() < deadline,
            "failed expired writer remained available"
        );
        thread::sleep(Duration::from_millis(5));
    }
    let mut observer = Client::connect(&fixture.connection, NoTls).unwrap();
    let expired = lease(&mut observer, DATABASE);
    assert!(!expired.2);
    assert_eq!(
        observer
            .query_one(
                "SELECT basis_t FROM atomic_heads WHERE database_id = $1",
                &[&DATABASE]
            )
            .unwrap()
            .get::<_, i64>(0),
        first.basis_t as i64
    );
    service.shutdown();
    let replacement =
        TransactionService::start(config(&fixture.connection, DATABASE, "replacement")).unwrap();
    assert_eq!(lease(&mut observer, DATABASE).1, expired.1 + 1);
    let next = replacement
        .client()
        .transact(request("next", EntityRef::Id(entity), 2), WAIT)
        .unwrap();
    assert_eq!(next.basis_t, first.basis_t + 1);
    assert_eq!(
        next.db_before.values(entity, COUNT).unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(
        client
            .submit(request("stale", EntityRef::Id(entity), 3))
            .unwrap_err()
            .category,
        ErrorCategory::Unavailable
    );
    replacement.shutdown();
}
