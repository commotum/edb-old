use atomic_core::{
    Attribute, AttributeRef, Cardinality, EntityMap, EntityRef, ErrorCategory, Keyword, MapValue,
    PostgresStore, Schema, TransactionRequest, TransactionService, TransactionServiceConfig,
    TxForm, TxOp, TxValue, USER_PARTITION, Value, ValueType, make_eid, sha256,
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

fn pin_application_name(database_id: &str) -> String {
    let digest = sha256(database_id.as_bytes());
    let suffix: String = digest[..16]
        .iter()
        .map(|byte| format!("{byte:02x}"))
        .collect();
    format!("atomic-pin-{suffix}")
}

fn pin_backend_count(client: &mut Client, database_id: &str) -> i64 {
    client
        .query_one(
            "SELECT count(*) FROM pg_stat_activity WHERE application_name = $1",
            &[&pin_application_name(database_id)],
        )
        .unwrap()
        .get(0)
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

fn request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(value)),
        }],
    )
}

fn config(
    connection: &str,
    database_id: String,
    holder: &str,
    capacity: usize,
) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.into(),
        database_id,
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: capacity,
        capacity_limits: atomic_core::CapacityLimits::default(),
    }
}

fn setup(connection: &str, database_id: &str) -> u64 {
    let mut migrator = atomic_core::PostgresMigrator::connect(connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(connection).unwrap();
    store
        .create_database(database_id, schema())
        .unwrap()
        .basis_t()
}

#[test]
fn concurrent_ordinary_requests_choose_successive_db_before_values() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_ordinary_concurrent");
    let initial_basis = setup(&connection, &database_id);
    let service = TransactionService::start(config(
        &connection,
        database_id.clone(),
        "ordinary-leader",
        4,
    ))
    .unwrap();
    let barrier = Arc::new(Barrier::new(3));
    let handles: Vec<_> = [("ordinary-one", 1), ("ordinary-two", 2)]
        .into_iter()
        .map(|(key, value)| {
            let client = service.client();
            let barrier = barrier.clone();
            std::thread::spawn(move || {
                barrier.wait();
                client.transact(request(key, value), Duration::from_secs(2))
            })
        })
        .collect();
    barrier.wait();

    let mut reports: Vec<_> = handles
        .into_iter()
        .map(|handle| handle.join().unwrap().unwrap())
        .collect();
    reports.sort_by_key(|report| report.basis_t);
    assert_eq!(
        reports
            .iter()
            .map(|report| (report.db_before.basis_t(), report.basis_t))
            .collect::<Vec<_>>(),
        vec![
            (initial_basis, initial_basis + 1),
            (initial_basis + 1, initial_basis + 2),
        ]
    );
    assert_eq!(
        reports.last().unwrap().db_after.basis_t(),
        initial_basis + 2
    );
    service.shutdown();
}

#[test]
fn authoritative_request_accepts_entity_maps_as_transaction_data() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_entity_map");
    let initial_basis = setup(&connection, &database_id);
    let service =
        TransactionService::start(config(&connection, database_id, "entity-map-leader", 2))
            .unwrap();
    let request = TransactionRequest::from_forms(
        "entity-map",
        vec![TxForm::EntityMap(EntityMap {
            id: Some(EntityRef::Temp("mapped".into())),
            attributes: vec![(
                AttributeRef::Ident(Keyword::new("item", "count")),
                MapValue::Value(TxValue::Scalar(Value::Long(41))),
            )],
        })],
    );
    let report = service
        .client()
        .transact(request, Duration::from_secs(2))
        .unwrap();
    assert_eq!(report.basis_t, initial_basis + 1);
    let mapped = report.tempids["mapped"];
    assert_eq!(
        report.db_after.values(mapped, ITEM_COUNT).unwrap(),
        vec![Value::Long(41)]
    );
    service.shutdown();
}

#[test]
fn concurrent_conditional_requests_against_one_basis_have_one_winner() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_conditional_concurrent");
    let initial_basis = setup(&connection, &database_id);
    let service = TransactionService::start(config(
        &connection,
        database_id.clone(),
        "conditional-leader",
        4,
    ))
    .unwrap();
    let barrier = Arc::new(Barrier::new(3));
    let handles: Vec<_> = [("conditional-one", 1), ("conditional-two", 2)]
        .into_iter()
        .map(|(key, value)| {
            let client = service.client();
            let barrier = barrier.clone();
            std::thread::spawn(move || {
                barrier.wait();
                client.transact(
                    request(key, value).comparing_basis(initial_basis),
                    Duration::from_secs(2),
                )
            })
        })
        .collect();
    barrier.wait();

    let results: Vec<_> = handles
        .into_iter()
        .map(|handle| handle.join().unwrap())
        .collect();
    let reports: Vec<_> = results
        .iter()
        .filter_map(|result| result.as_ref().ok())
        .collect();
    let errors: Vec<_> = results
        .iter()
        .filter_map(|result| result.as_ref().err())
        .collect();
    assert_eq!(reports.len(), 1);
    assert_eq!(reports[0].basis_t, initial_basis + 1);
    assert_eq!(errors.len(), 1);
    assert_eq!(
        (errors[0].category, errors[0].code),
        (ErrorCategory::Conflict, "postgres/stale-basis")
    );
    service.shutdown();
}

#[test]
fn report_subscription_is_lossless_beyond_the_previous_bounded_buffer() {
    let Some(connection) = connection() else {
        return;
    };
    const REPORTS: u64 = 96;
    let database_id = unique("service_lossless_reports");
    let initial_basis = setup(&connection, &database_id);
    let service =
        TransactionService::start(config(&connection, database_id.clone(), "report-leader", 4))
            .unwrap();
    let client = service.client();
    let reports = client.subscribe_reports();

    for ordinal in 0..REPORTS {
        let report = client
            .transact(
                request(&format!("report-{ordinal}"), ordinal as i64),
                Duration::from_secs(2),
            )
            .unwrap();
        assert_eq!(report.basis_t, initial_basis + ordinal + 1);
    }

    let retained = client.stats();
    assert_eq!(reports.pending_reports(), REPORTS as usize);
    assert_eq!(retained.queued_reports, REPORTS as usize);
    assert_eq!(retained.max_queued_reports, REPORTS as usize);

    for ordinal in 0..REPORTS {
        assert_eq!(
            reports
                .recv_timeout(Duration::from_secs(2))
                .unwrap()
                .basis_t,
            initial_basis + ordinal + 1
        );
    }
    assert!(reports.try_recv().is_err());
    assert_eq!(reports.pending_reports(), 0);
    assert_eq!(client.stats().queued_reports, 0);
    assert_eq!(client.stats().max_queued_reports, REPORTS as usize);

    drop(reports);
    let abandoned = client.subscribe_reports();
    client
        .transact(
            request("report-abandoned", REPORTS as i64),
            Duration::from_secs(2),
        )
        .unwrap();
    assert_eq!(abandoned.pending_reports(), 1);
    assert_eq!(client.stats().queued_reports, 1);
    drop(abandoned);
    assert_eq!(client.stats().queued_reports, 0);
    service.shutdown();
}

#[test]
fn worker_serializes_reports_and_resolves_durable_retry() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_serial");
    let initial_basis = setup(&connection, &database_id);
    let service =
        TransactionService::start(config(&connection, database_id.clone(), "one", 8)).unwrap();
    let client = service.client();
    let reports = client.subscribe_reports();

    let first = client.submit(request("one", 1)).unwrap();
    let one = first.wait(Duration::from_secs(2)).unwrap();
    let second = client.submit(request("two", 2)).unwrap();
    let two = second.wait(Duration::from_secs(2)).unwrap();
    assert_eq!(
        (one.db_before.basis_t(), one.basis_t),
        (initial_basis, initial_basis + 1)
    );
    assert_eq!(
        (two.db_before.basis_t(), two.basis_t),
        (initial_basis + 1, initial_basis + 2)
    );
    assert_eq!(
        two.db_after.values(user(42), ITEM_COUNT).unwrap(),
        vec![Value::Long(2)]
    );
    assert_eq!(
        reports
            .recv_timeout(Duration::from_secs(1))
            .unwrap()
            .basis_t,
        initial_basis + 1
    );
    assert_eq!(
        reports
            .recv_timeout(Duration::from_secs(1))
            .unwrap()
            .basis_t,
        initial_basis + 2
    );

    let replay = client
        .transact(request("one", 1), Duration::from_secs(2))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, initial_basis + 1);
    let conflict = client
        .transact(request("one", 9), Duration::from_secs(2))
        .unwrap_err();
    assert_eq!(
        (conflict.category, conflict.code),
        (ErrorCategory::Conflict, "postgres/idempotency-key-reused")
    );
    assert!(reports.try_recv().is_err(), "replays are not live commits");
    assert_eq!(client.stats().processed, 3);
    assert_eq!(client.stats().subscribers, 1);
    drop(reports);
    assert_eq!(client.stats().subscribers, 0);
    service.shutdown();
    assert!(!client.is_available());
}

#[test]
fn originating_result_is_enqueued_before_the_subscription_report() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_report_order");
    let initial_basis = setup(&connection, &database_id);
    let service =
        TransactionService::start(config(&connection, database_id, "report-order", 2)).unwrap();
    let client = service.client();
    let reports = client.subscribe_reports();
    let ticket = client.submit(request("ordered", 7)).unwrap();

    // The worker enqueues the originating result before publishing the same
    // commit to report subscribers. Once the subscription can observe it, a
    // zero-duration wait must therefore observe the already-enqueued result.
    let subscribed = reports.recv_timeout(Duration::from_secs(2)).unwrap();
    let originating = ticket.wait(Duration::ZERO).unwrap();
    assert_eq!(originating.db_before.basis_t(), initial_basis);
    assert_eq!(originating.basis_t, initial_basis + 1);
    assert_eq!(subscribed.db_before.basis_t(), initial_basis);
    assert_eq!(subscribed.basis_t, originating.basis_t);
    assert_eq!(subscribed.tx_hash, originating.tx_hash);
    assert_eq!(subscribed.tx_data, originating.tx_data);
    assert_eq!(subscribed.tempids, originating.tempids);
    assert_eq!(
        subscribed.db_after.values(user(42), ITEM_COUNT).unwrap(),
        vec![Value::Long(7)]
    );
    service.shutdown();
}

#[test]
fn unread_report_owns_native_pins_after_service_shutdown_until_it_is_dropped() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_report_pin");
    let initial_basis = setup(&connection, &database_id);
    let service =
        TransactionService::start(config(&connection, database_id.clone(), "report-pin", 2))
            .unwrap();
    let client = service.client();
    let reports = client.subscribe_reports();
    let direct = client
        .transact(request("report-pin", 17), Duration::from_secs(2))
        .unwrap();
    assert_eq!(direct.basis_t, initial_basis + 1);
    drop(direct);
    assert_eq!(reports.pending_reports(), 1);
    assert_eq!(client.stats().queued_reports, 1);
    assert_eq!(client.stats().max_queued_reports, 1);

    // Once the service and client are gone, only the unread report owns its
    // immutable db-before/db-after values and therefore their shared native
    // root/generation pin manager.
    service.shutdown();
    drop(client);
    let mut observer = Client::connect(&connection, NoTls).unwrap();
    assert_eq!(pin_backend_count(&mut observer, &database_id), 1);

    let queued = reports.recv_timeout(Duration::ZERO).unwrap();
    assert_eq!(reports.pending_reports(), 0);
    assert_eq!(queued.db_before.basis_t(), initial_basis);
    assert_eq!(queued.db_after.basis_t(), initial_basis + 1);
    assert_eq!(
        queued.db_after.values(user(42), ITEM_COUNT).unwrap(),
        vec![Value::Long(17)]
    );
    assert_eq!(pin_backend_count(&mut observer, &database_id), 1);

    drop(queued);
    drop(reports);
    let deadline = std::time::Instant::now() + Duration::from_secs(2);
    while pin_backend_count(&mut observer, &database_id) != 0 {
        assert!(
            std::time::Instant::now() < deadline,
            "dropping the last queued report did not release its pin session"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
}

#[test]
fn queue_is_bounded_and_timeout_is_unknown_then_reconcilable() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_bound");
    let initial_basis = setup(&connection, &database_id);
    let service =
        TransactionService::start(config(&connection, database_id.clone(), "one", 1)).unwrap();
    let client = service.client();
    let slow_reports = client.subscribe_reports();

    let mut blocker = Client::connect(&connection, NoTls).unwrap();
    let mut lock = blocker.transaction().unwrap();
    lock.query_one(
        "SELECT basis_t FROM atomic_heads WHERE database_id = $1 FOR UPDATE",
        &[&database_id],
    )
    .unwrap();
    let first = client.submit(request("one", 1)).unwrap();
    for _ in 0..100 {
        if client.stats().queued == 0 {
            break;
        }
        std::thread::sleep(Duration::from_millis(2));
    }
    assert_eq!(
        client.stats().queued,
        0,
        "worker did not begin first request"
    );
    let second = client.submit(request("two", 2)).unwrap();
    let full = client.submit(request("three", 3)).unwrap_err();
    assert_eq!(
        (full.category, full.code),
        (ErrorCategory::Busy, "service/queue-full")
    );
    let unknown = first.wait(Duration::from_millis(20)).unwrap_err();
    assert_eq!(unknown.category, ErrorCategory::UnknownOutcome);
    assert!(!unknown.details.contains_key("request_key"));
    assert_eq!(unknown.details["request_key_hash"].len(), 64);
    assert_ne!(unknown.details["request_key_hash"], "one");
    lock.commit().unwrap();

    let second_report = second.wait(Duration::from_secs(2)).unwrap();
    assert_eq!(second_report.basis_t, initial_basis + 2);
    let replay = client
        .transact(request("one", 1), Duration::from_secs(2))
        .unwrap();
    assert!(replay.replayed);
    let stats = client.stats();
    assert_eq!(stats.rejected_full, 1);
    assert!(stats.max_queued <= 1);
    assert_eq!(
        slow_reports
            .recv_timeout(Duration::from_secs(1))
            .unwrap()
            .basis_t,
        initial_basis + 1
    );
    assert_eq!(
        slow_reports
            .recv_timeout(Duration::from_secs(1))
            .unwrap()
            .basis_t,
        initial_basis + 2
    );
    service.shutdown();
}

#[test]
fn graceful_shutdown_settles_admitted_and_queued_requests() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_shutdown");
    let initial_basis = setup(&connection, &database_id);
    let service =
        TransactionService::start(config(&connection, database_id.clone(), "one", 2)).unwrap();
    let client = service.client();
    let mut blocker = Client::connect(&connection, NoTls).unwrap();
    let mut lock = blocker.transaction().unwrap();
    lock.query_one(
        "SELECT basis_t FROM atomic_heads WHERE database_id = $1 FOR UPDATE",
        &[&database_id],
    )
    .unwrap();
    let admitted = client.submit(request("admitted", 1)).unwrap();
    for _ in 0..100 {
        if client.stats().queued == 0 {
            break;
        }
        std::thread::sleep(Duration::from_millis(2));
    }
    let queued = client.submit(request("queued", 2)).unwrap();
    let shutdown = std::thread::spawn(move || service.shutdown());
    for _ in 0..100 {
        if !client.is_available() {
            break;
        }
        std::thread::sleep(Duration::from_millis(2));
    }
    assert!(!client.is_available());
    lock.commit().unwrap();
    assert_eq!(
        admitted.wait(Duration::from_secs(2)).unwrap().basis_t,
        initial_basis + 1,
        "the already executing fenced transaction may finish"
    );
    let error = queued.wait(Duration::from_secs(2)).unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Unavailable, "service/unavailable")
    );
    shutdown.join().unwrap();
    assert_eq!(
        PostgresStore::connect(&connection)
            .unwrap()
            .recover(&database_id)
            .unwrap()
            .basis_t(),
        initial_basis + 1
    );
}
