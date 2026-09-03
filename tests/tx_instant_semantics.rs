use atomic_core::{
    DB_TX_INSTANT, Database, EntityRef, ErrorCategory, PostgresStore, Schema, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp, TxValue, Value, t_to_tx,
};
use postgres::{Client, NoTls};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

fn explicit_tx_instant(entity: EntityRef, instant: i64) -> TxOp {
    TxOp::Add {
        entity,
        attribute: DB_TX_INSTANT as u32,
        value: TxValue::Scalar(Value::Instant(instant)),
    }
}

fn report_tx_instant(tx_data: &[atomic_core::Datom]) -> i64 {
    let instants: Vec<_> = tx_data
        .iter()
        .filter_map(|datom| {
            (datom.attribute == DB_TX_INSTANT as u32 && datom.added).then_some(&datom.value)
        })
        .collect();
    match instants.as_slice() {
        [Value::Instant(instant)] => *instant,
        values => panic!("expected one :db/txInstant assertion, got {values:?}"),
    }
}

#[test]
fn kernel_accepts_one_explicit_current_tx_instant_and_rejects_duplicates() {
    let before = Database::bootstrap().unwrap();
    let explicit = explicit_tx_instant(EntityRef::Tx, 1_000);
    let accepted = before.with(std::slice::from_ref(&explicit), 1_000).unwrap();

    assert_eq!(report_tx_instant(&accepted.tx_data), 1_000);

    let error = before
        .with(&[explicit.clone(), explicit], 1_000)
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Incorrect, "transaction/multiple-tx-instants")
    );
    assert_eq!(
        before.basis_t(),
        0,
        "the rejected pure transition is invisible"
    );
}

#[test]
fn kernel_rejects_adding_retracting_or_resetting_an_old_tx_instant() {
    let first = Database::bootstrap().unwrap().with(&[], 1_000).unwrap();
    let old_tx = EntityRef::Id(t_to_tx(first.db_after.basis_t()).unwrap());

    let add_error = first
        .db_after
        .with(&[explicit_tx_instant(old_tx.clone(), 1_001)], 1_001)
        .unwrap_err();
    assert_eq!(
        (add_error.category, add_error.code),
        (ErrorCategory::Incorrect, "transaction/reset-tx-instant")
    );

    let retract_error = first
        .db_after
        .with(
            &[TxOp::Retract {
                entity: old_tx.clone(),
                attribute: DB_TX_INSTANT as u32,
                value: Some(TxValue::Scalar(Value::Instant(1_000))),
            }],
            1_001,
        )
        .unwrap_err();
    assert_eq!(
        (retract_error.category, retract_error.code),
        (ErrorCategory::Incorrect, "transaction/reset-tx-instant")
    );

    let cas_error = first
        .db_after
        .with(
            &[TxOp::Cas {
                entity: old_tx,
                attribute: DB_TX_INSTANT as u32,
                old: Some(TxValue::Scalar(Value::Instant(1_000))),
                new: TxValue::Scalar(Value::Instant(1_001)),
            }],
            1_001,
        )
        .unwrap_err();
    assert_eq!(
        (cas_error.category, cas_error.code),
        (ErrorCategory::Incorrect, "transaction/reset-tx-instant")
    );
    assert_eq!(first.db_after.basis_t(), 1);
    assert_eq!(report_tx_instant(&first.tx_data), 1_000);
}

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

fn postgres_now(client: &mut Client) -> i64 {
    client
        .query_one(
            "SELECT floor(extract(epoch FROM clock_timestamp()) * 1000)::bigint",
            &[],
        )
        .unwrap()
        .get(0)
}

fn head_basis(client: &mut Client, database_id: &str) -> u64 {
    let basis: i64 = client
        .query_one(
            "SELECT basis_t FROM atomic_heads WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    basis.try_into().unwrap()
}

fn service_config(connection: &str, database_id: String) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id,
        holder_id: unique("tx_instant_holder"),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: atomic_core::CapacityLimits::default(),
    }
}

#[test]
fn service_owns_time_and_enforces_inclusive_tx_instant_bounds_atomically() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("tx_instant_bounds");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    assert_eq!(
        store
            .create_database(&database_id, Schema::new())
            .unwrap()
            .basis_t(),
        0
    );
    drop(store);

    let service =
        TransactionService::start(service_config(&connection, database_id.clone())).unwrap();
    let client = service.client();
    let mut observer = Client::connect(&connection, NoTls).unwrap();

    let server_before = postgres_now(&mut observer);
    let first = client
        .transact(
            TransactionRequest::new("default-one", Vec::new()),
            Duration::from_secs(2),
        )
        .unwrap();
    let server_after = postgres_now(&mut observer);
    let first_instant = report_tx_instant(&first.tx_data);
    assert!(
        (server_before..=server_after).contains(&first_instant),
        "default instant {first_instant} was outside server interval {server_before}..={server_after}"
    );

    let second = client
        .transact(
            TransactionRequest::new("default-two", Vec::new()),
            Duration::from_secs(2),
        )
        .unwrap();
    let second_instant = report_tx_instant(&second.tx_data);
    assert!(second_instant >= first_instant);
    let committed_basis = second.basis_t;
    assert_eq!(head_basis(&mut observer, &database_id), committed_basis);

    let past = second_instant - 1;
    let past_error = client
        .transact(
            TransactionRequest::new(
                "explicit-past",
                vec![explicit_tx_instant(EntityRef::Tx, past)],
            ),
            Duration::from_secs(2),
        )
        .unwrap_err();
    assert_eq!(
        (past_error.category, past_error.code),
        (ErrorCategory::Incorrect, "transaction/past-tx-instant")
    );
    assert_eq!(head_basis(&mut observer, &database_id), committed_basis);

    let future = postgres_now(&mut observer) + 60_000;
    let future_error = client
        .transact(
            TransactionRequest::new(
                "explicit-future",
                vec![explicit_tx_instant(EntityRef::Tx, future)],
            ),
            Duration::from_secs(2),
        )
        .unwrap_err();
    assert_eq!(
        (future_error.category, future_error.code),
        (ErrorCategory::Incorrect, "transaction/future-tx-instant")
    );
    assert_eq!(head_basis(&mut observer, &database_id), committed_basis);

    let lower_bound = client
        .transact(
            TransactionRequest::new(
                "explicit-lower-bound",
                vec![explicit_tx_instant(EntityRef::Tx, second_instant)],
            ),
            Duration::from_secs(2),
        )
        .unwrap();
    assert_eq!(report_tx_instant(&lower_bound.tx_data), second_instant);
    assert_eq!(lower_bound.basis_t, committed_basis + 1);
    assert_eq!(head_basis(&mut observer, &database_id), committed_basis + 1);

    service.shutdown();
}
