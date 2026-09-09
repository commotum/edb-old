use atomic_core::{
    Attribute, Cardinality, Connection, EntityRef, Keyword, PostgresMigrator, PostgresStore,
    Schema, TransactionRequest, TxOp, Value, ValueType,
};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

mod common;

const COUNT: u32 = 1_000;
const TIMEOUT: Duration = Duration::from_secs(10);

fn setup() -> Option<(String, String)> {
    let postgres = std::env::var("ATOMIC_POSTGRES_URL").ok()?;
    let id = format!(
        "native-connection-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    PostgresMigrator::connect(&postgres)
        .unwrap()
        .migrate()
        .unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    PostgresStore::connect(&postgres)
        .unwrap()
        .create_database(&id, schema)
        .unwrap();
    Some((postgres, id))
}

fn request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: COUNT,
            value: Value::Long(value).into(),
        }],
    )
}

fn await_background(connection: &Connection, basis: u64) {
    let deadline = Instant::now() + TIMEOUT;
    while connection.db().basis_t() < basis {
        assert!(
            Instant::now() < deadline,
            "background did not advance: {:?}",
            connection.observation_error()
        );
        std::thread::sleep(Duration::from_millis(10));
    }
}

#[test]
fn independent_peers_observe_unwaited_and_external_commits_in_order() {
    let Some((postgres, id)) = setup() else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    let attached = Connection::attach(&postgres, writer.client(), 4).unwrap();
    let reader = Connection::connect(&postgres, &id, 4).unwrap();
    assert!(reader.submit(request("read-only", 0)).is_err());
    let old = reader.db();
    let initial = old.basis_t();
    attached.enable_transaction_reports();
    reader.enable_transaction_reports();

    let first = attached.submit(request("first", 1)).unwrap();
    let second = attached.submit(request("second", 2)).unwrap();
    // Waiting in reverse order must not turn an acknowledged commit into a
    // report-gap error. Dropping a ticket must not suppress adoption either.
    let second_report = second.wait(TIMEOUT).unwrap();
    assert_eq!(attached.db().basis_t(), second_report.basis_t);
    let first_report = first.wait(TIMEOUT).unwrap();
    drop(attached.submit(request("dropped", 3)).unwrap());
    let external = writer
        .client()
        .transact(request("external", 4), TIMEOUT)
        .unwrap();
    await_background(&reader, external.basis_t);
    assert_eq!(attached.db().basis_t(), external.basis_t);
    for connection in [&attached, &reader] {
        let mut before = old.clone();
        for basis in initial + 1..=external.basis_t {
            let report = connection
                .next_transaction_report(TIMEOUT)
                .unwrap()
                .unwrap();
            assert_eq!(report.basis_t, basis);
            common::assert_same_information(&report.db_before, &before);
            assert_eq!(report.db_after.basis_t(), basis);
            assert!(!report.tx_data.is_empty());
            assert!(report.tempids.contains_key("item"));
            before = report.db_after;
        }
        assert!(connection.try_next_transaction_report().is_none());
        assert_eq!(connection.load_stats().compatibility_materializations, 0);
        assert!(connection.observation_error().is_none());
    }
    let replay = attached.transact(request("first", 1), TIMEOUT).unwrap();
    assert!(replay.replayed);
    common::assert_same_information(&first_report.db_after, &replay.db_after);
    assert!(attached.try_next_transaction_report().is_none());
    assert_eq!(old.basis_t(), initial);
    assert!(
        old.values(first_report.tempids["item"], COUNT)
            .unwrap()
            .is_empty()
    );
    writer.shutdown();
    assert_eq!(
        reader.db().values(external.tempids["item"], COUNT).unwrap(),
        vec![Value::Long(4)]
    );
    assert_eq!(attached.db().basis_t(), external.basis_t);
}

#[test]
fn writer_replacement_preserves_connection_identity_and_captured_values() {
    let Some((postgres, id)) = setup() else {
        return;
    };
    let first_writer = common::start_service(&postgres, &id);
    let connection = Connection::attach(&postgres, first_writer.client(), 4).unwrap();
    let report = connection.transact(request("before", 1), TIMEOUT).unwrap();
    let old = connection.db();
    first_writer.shutdown();
    assert_eq!(
        connection
            .submit(request("unavailable", 2))
            .err()
            .unwrap()
            .code,
        "service/unavailable"
    );
    let second_writer = common::start_service(&postgres, &id);
    connection.attach_writer(second_writer.client()).unwrap();
    let next = connection.transact(request("after", 3), TIMEOUT).unwrap();
    assert_eq!(next.basis_t, report.basis_t + 1);
    assert_eq!(
        old.values(report.tempids["item"], COUNT).unwrap(),
        vec![Value::Long(1)]
    );
    let other = format!("{id}-other");
    PostgresStore::connect(&postgres)
        .unwrap()
        .create_database(&other, Schema::new())
        .unwrap();
    let wrong_writer = common::start_service(&postgres, &other);
    assert_eq!(
        connection
            .attach_writer(wrong_writer.client())
            .unwrap_err()
            .code,
        "connection/database-identity-mismatch"
    );
    wrong_writer.shutdown();
    second_writer.shutdown();
    assert_eq!(connection.sync().unwrap().basis_t(), next.basis_t);
}

#[test]
fn physical_index_and_excision_sync_require_their_own_completion() {
    let Some((postgres, id)) = setup() else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    let connection = Connection::attach(&postgres, writer.client(), 4).unwrap();
    let inserted = connection.transact(request("seed", 1), TIMEOUT).unwrap();
    // Logical adoption alone does not promise a physical publication.
    atomic_core::PostgresIndexer::connect(&postgres, &id)
        .unwrap()
        .consolidate()
        .unwrap();
    assert_eq!(
        connection
            .sync_index(inserted.basis_t, TIMEOUT)
            .unwrap()
            .basis_t(),
        inserted.basis_t
    );
    assert_eq!(
        connection
            .sync_schema(inserted.basis_t, TIMEOUT)
            .unwrap()
            .basis_t(),
        inserted.basis_t
    );
    assert!(connection.sync_excise(inserted.basis_t, TIMEOUT).is_ok());
    assert_eq!(
        connection
            .sync_index(inserted.basis_t + 10, Duration::from_millis(20))
            .err()
            .unwrap()
            .code,
        "peer/sync-index-timeout"
    );
    let requested = connection
        .transact(
            TransactionRequest::new(
                "excise",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("excision".into()),
                    attribute: atomic_core::DB_EXCISE as u32,
                    value: Value::Ref(inserted.tempids["item"]).into(),
                }],
            ),
            TIMEOUT,
        )
        .unwrap();
    assert_eq!(
        connection
            .sync_excise(requested.basis_t, Duration::from_millis(20))
            .err()
            .unwrap()
            .code,
        "peer/sync-excise-timeout"
    );
    writer.shutdown();
    let mut operator = atomic_core::PostgresOperator::connect(&postgres).unwrap();
    let error = operator
        .process_excision_requests_with_fault(&id, atomic_core::ExcisionFault::AfterActivation)
        .unwrap_err();
    assert!(error.code.contains("fault"), "{error}");
    assert_eq!(
        connection
            .sync_excise(requested.basis_t, Duration::from_millis(20))
            .err()
            .unwrap()
            .code,
        "peer/sync-excise-timeout"
    );
    operator.process_excision_requests(&id).unwrap();
    let excised = connection.sync_excise(requested.basis_t, TIMEOUT).unwrap();
    assert!(
        excised
            .values(inserted.tempids["item"], COUNT)
            .unwrap()
            .is_empty()
    );
    assert_eq!(
        inserted
            .db_after
            .values(inserted.tempids["item"], COUNT)
            .unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(connection.load_stats().compatibility_materializations, 0);
}

#[test]
fn lagging_peer_adopts_indexed_prefix_larger_than_its_recent_limit() {
    let Some((postgres, id)) = setup() else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    let mut indexer = atomic_core::PostgresIndexer::connect(&postgres, &id).unwrap();
    indexer.consolidate().unwrap();
    let peer = atomic_core::Peer::connect_with_limits(
        &postgres,
        &id,
        0,
        0,
        atomic_core::recent::RecentLimits {
            soft_datoms: 2,
            hard_datoms: 5,
            soft_bytes: 512 * 1024,
            hard_bytes: 1024 * 1024,
        },
    )
    .unwrap();
    let old = peer.db();
    let mut latest = None;
    for value in 0..8 {
        latest = Some(
            writer
                .client()
                .transact(request(&format!("lag-{value}"), value), TIMEOUT)
                .unwrap(),
        );
    }
    let latest = latest.unwrap();
    indexer.consolidate().unwrap();
    let advanced = peer.sync().unwrap();
    assert_eq!(advanced.basis_t(), latest.basis_t);
    assert_eq!(peer.durable_base_t(), latest.basis_t);
    assert_eq!(peer.recent_stats().datoms, 0);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    assert!(
        old.values(latest.tempids["item"], COUNT)
            .unwrap()
            .is_empty()
    );
    assert_eq!(
        advanced.values(latest.tempids["item"], COUNT).unwrap(),
        vec![Value::Long(7)]
    );
    writer.shutdown();
}

#[test]
fn independent_background_observer_reconnects_after_its_sql_session_is_killed() {
    let Some((postgres, id)) = setup() else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    let first = writer
        .client()
        .transact(request("before-disconnect", 1), TIMEOUT)
        .unwrap();
    let application_name = format!("observe-{}", std::process::id());
    let reader_parameters = format!("{postgres} application_name={application_name}");
    let reader = Connection::connect(&reader_parameters, &id, 0).unwrap();
    reader.enable_transaction_reports();
    let old = reader.db();
    let mut control = postgres::Client::connect(&postgres, postgres::NoTls).unwrap();
    let killed = control
        .query(
            "SELECT pg_terminate_backend(pid) FROM pg_stat_activity \
         WHERE application_name = $1 AND pid <> pg_backend_pid()",
            &[&application_name],
        )
        .unwrap();
    assert!(
        !killed.is_empty(),
        "disconnect injection must terminate a real peer session"
    );
    assert!(killed.iter().all(|row| row.get::<_, bool>(0)));
    let next = writer
        .client()
        .transact(request("after-disconnect", 2), TIMEOUT)
        .unwrap();
    await_background(&reader, next.basis_t);
    let observed = reader.next_transaction_report(TIMEOUT).unwrap().unwrap();
    common::assert_same_information(&observed.db_before, &next.db_before);
    common::assert_same_information(&observed.db_after, &next.db_after);
    assert_eq!(observed.tempids, next.tempids);
    assert!(reader.try_next_transaction_report().is_none());
    assert_eq!(
        old.values(first.tempids["item"], COUNT).unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(reader.load_stats().compatibility_materializations, 0);
    writer.shutdown();
}

#[test]
fn native_time_views_resolve_t_tx_and_duplicate_instants_over_postgres() {
    let Some((postgres, id)) = setup() else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    let connection = Connection::attach(&postgres, writer.client(), 0).unwrap();
    let first = connection
        .transact(request("instant-first", 1).with_tx_instant(1_000), TIMEOUT)
        .unwrap();
    let second = connection
        .transact(request("instant-second", 2).with_tx_instant(1_000), TIMEOUT)
        .unwrap();
    let third = connection
        .transact(request("instant-third", 3).with_tx_instant(1_100), TIMEOUT)
        .unwrap();
    atomic_core::PostgresIndexer::connect(&postgres, &id)
        .unwrap()
        .consolidate()
        .unwrap();
    let database = connection.sync_index(third.basis_t, TIMEOUT).unwrap();
    for (point, expected) in [
        (atomic_core::TimePoint::T(second.basis_t), second.basis_t),
        (
            atomic_core::TimePoint::Tx(atomic_core::t_to_tx(second.basis_t).unwrap()),
            second.basis_t,
        ),
        (atomic_core::TimePoint::Instant(1_000), first.basis_t),
        (atomic_core::TimePoint::Instant(1_050), second.basis_t),
        (atomic_core::TimePoint::Instant(1_101), third.basis_t + 1),
    ] {
        assert_eq!(database.resolve_time_point(point).unwrap(), expected);
    }
    let as_of = database
        .clone()
        .as_of_time_point(atomic_core::TimePoint::Instant(1_000))
        .unwrap();
    assert_eq!(
        as_of.values(first.tempids["item"], COUNT).unwrap(),
        vec![Value::Long(1)]
    );
    assert!(
        as_of
            .values(second.tempids["item"], COUNT)
            .unwrap()
            .is_empty()
    );
    let since = database
        .since_time_point(atomic_core::TimePoint::Tx(
            atomic_core::t_to_tx(first.basis_t).unwrap(),
        ))
        .unwrap();
    assert!(
        since
            .values(first.tempids["item"], COUNT)
            .unwrap()
            .is_empty()
    );
    assert_eq!(
        since.values(second.tempids["item"], COUNT).unwrap(),
        vec![Value::Long(2)]
    );
    assert_eq!(connection.load_stats().compatibility_materializations, 0);
    writer.shutdown();
}

#[test]
fn a_lagging_report_queue_does_not_skip_transactions_across_excision() {
    let Some((postgres, id)) = setup() else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    // This manually advanced peer models a connected observer between polls;
    // it retains its original generation while the background rewrite runs.
    let peer = atomic_core::Peer::connect(&postgres, &id, 4).unwrap();
    peer.enable_tx_reports();
    let old = peer.db();
    let inserted = writer
        .client()
        .transact(request("before-excision", 7), TIMEOUT)
        .unwrap();
    let excision = writer
        .client()
        .transact(
            TransactionRequest::new(
                "excise-unobserved",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("excision".into()),
                    attribute: atomic_core::DB_EXCISE as u32,
                    value: Value::Ref(inserted.tempids["item"]).into(),
                }],
            ),
            TIMEOUT,
        )
        .unwrap();
    writer.shutdown();
    atomic_core::PostgresOperator::connect(&postgres)
        .unwrap()
        .process_excision_requests(&id)
        .unwrap();
    // Remain offline through another rewrite, then leave an ordinary tail in
    // the newest generation. No physical activation may hide a logical tx.
    let writer = common::start_service(&postgres, &id);
    let second = writer
        .client()
        .transact(request("second-generation", 8), TIMEOUT)
        .unwrap();
    let second_excision = writer
        .client()
        .transact(
            TransactionRequest::new(
                "excise-second-generation",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("second-excision".into()),
                    attribute: atomic_core::DB_EXCISE as u32,
                    value: Value::Ref(second.tempids["item"]).into(),
                }],
            ),
            TIMEOUT,
        )
        .unwrap();
    writer.shutdown();
    atomic_core::PostgresOperator::connect(&postgres)
        .unwrap()
        .process_excision_requests(&id)
        .unwrap();
    let writer = common::start_service(&postgres, &id);
    let newest = writer
        .client()
        .transact(request("after-two-rewrites", 9), TIMEOUT)
        .unwrap();
    let current = peer.sync().unwrap();
    assert!(
        current
            .values(inserted.tempids["item"], COUNT)
            .unwrap()
            .is_empty()
    );
    let reports = peer.take_tx_reports();
    assert_eq!(
        reports
            .iter()
            .map(|report| report.basis_t)
            .collect::<Vec<_>>(),
        vec![
            inserted.basis_t,
            excision.basis_t,
            second.basis_t,
            second_excision.basis_t,
            newest.basis_t
        ]
    );
    common::assert_same_information(&reports[0].db_before, &old);
    common::assert_same_information(&reports[0].db_after, &inserted.db_after);
    assert_eq!(reports[0].tempids, inserted.tempids);
    assert_eq!(reports[1].tempids, excision.tempids);
    common::assert_same_information(&reports[2].db_after, &second.db_after);
    common::assert_same_information(&reports[4].db_after, &newest.db_after);
    assert_eq!(reports[2].tempids, second.tempids);
    assert_eq!(reports[3].tempids, second_excision.tempids);
    assert_eq!(reports[4].tempids, newest.tempids);
    assert!(peer.sync().is_ok());
    assert!(peer.take_tx_reports().is_empty());
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
}
