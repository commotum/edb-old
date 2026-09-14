//! The public service submits through opaque block authority, not a writer fixture.
mod common;

use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::{
    Attribute, CapacityLimits, Cardinality, ErrorCategory, IndexOrder, Keyword, OperationContext,
    OperationKind, PostgresConnectionConfig, Schema, ServiceOptions, StandbyStatus,
    TransactionDefaults, TransactionRequest, TransactionService, TransactionServiceConfig,
    TransactionStandby, Value, ValueType,
};
use std::time::{Duration, Instant};

const VALUE: u32 = 1000;
const WAIT: Duration = Duration::from_secs(20);

fn fixture(label: &str) -> Option<(common::PostgresFixture, PostgresConnectionConfig)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    let connection = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&connection).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    BlockDatabase::create(&connection, "items", schema).unwrap();
    Some((fixture, connection))
}

fn config(connection: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: PostgresConnectionConfig::plaintext(connection),
        database_id: "items".into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(10),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 4,
        capacity_limits: CapacityLimits::default(),
    }
}

fn request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::from_edn(key, &format!(r#"[{{:db/id "item" :item/value {value}}}]"#))
        .unwrap()
        .with_tx_instant(1000 + value)
}

#[test]
fn queued_public_service_reports_exact_values_and_retries_before_changed_limits() {
    let Some((fixture, connection)) = fixture("block_service_queue") else {
        return;
    };
    let service = TransactionService::start(config(&fixture.connection, "first")).unwrap();
    let initial_basis = service.recovery_stats().target_t;
    assert_eq!(
        initial_basis, 1,
        "nonempty schema installation is a transaction"
    );
    let identity = service.identity();
    let client = service.client();
    let reports = client.subscribe_reports();
    let operation = OperationContext::named(
        OperationKind::Application,
        Keyword::new("test", "block-service"),
    )
    .unwrap();
    let submitted = request("first", 1).comparing_basis(initial_basis);
    let first = {
        let _scope = operation.enter();
        client
            .submit(submitted.clone())
            .unwrap()
            .wait(WAIT)
            .unwrap()
    };
    assert_eq!(first.basis_t, initial_basis + 1);
    assert_eq!(first.db_before.basis_t(), initial_basis);
    let entity = first.tempids["item"];
    assert_eq!(
        first.db_after.values(entity, VALUE).unwrap(),
        vec![Value::Long(1)]
    );
    let diagnostic = first
        .diagnostics
        .as_ref()
        .expect("named caller requests diagnostics");
    assert_eq!(diagnostic.basis_t, first.basis_t);
    assert!(!diagnostic.replayed);
    assert_eq!(reports.recv_timeout(WAIT).unwrap().tx_hash, first.tx_hash);

    let started = Instant::now();
    let tickets: Vec<_> = (2..=4)
        .map(|value| {
            client
                .submit(request(&format!("queued-{value}"), value))
                .unwrap()
        })
        .collect();
    for (ordinal, ticket) in tickets.into_iter().enumerate() {
        let report = ticket.wait(WAIT).unwrap();
        assert_eq!(report.basis_t, initial_basis + ordinal as u64 + 2);
        assert_eq!(reports.recv_timeout(WAIT).unwrap().tx_hash, report.tx_hash);
    }
    let replay = client.transact(submitted.clone(), WAIT).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    assert_eq!(replay.tx_data, first.tx_data);
    assert_eq!(replay.tempids, first.tempids);
    assert!(
        reports.try_recv().is_err(),
        "receipt replay is not a new commit notification"
    );
    let stats = service.background_indexing_stats();
    assert_eq!(stats.published_basis_t, initial_basis);
    assert_eq!(stats.target_basis_t, initial_basis + 4);
    assert_eq!(stats.total_transactions, 4);
    assert!(stats.total_datoms > 0);
    assert_eq!(stats.jobs_completed, 0);
    let requested = client.request_index().unwrap();
    assert_eq!(requested.target_t, initial_basis + 4);
    let deadline = Instant::now() + WAIT;
    while client.background_indexing_stats().published_basis_t < requested.target_t {
        assert!(Instant::now() < deadline, "index request did not complete");
        assert!(
            client.is_available(),
            "index preparation failed: {:?}",
            client.background_indexing_stats().last_failure
        );
        std::thread::sleep(Duration::from_millis(5));
    }
    let indexed = client.background_indexing_stats();
    assert_eq!(indexed.total_transactions, 0);
    assert_eq!(indexed.total_datoms, 0);
    assert!(indexed.jobs_completed >= 1);
    assert_eq!(
        first.db_after.values(entity, VALUE).unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(service.writer_residency_stats().eager_database_values, 0);
    eprintln!(
        "BLOCK_SERVICE queued_commits=3 exact_retries=1 index_complete=true complete_us={} recent_datoms={} recent_bytes={}",
        started.elapsed().as_micros(),
        stats.total_datoms,
        stats.total_bytes
    );
    drop(reports);
    service.shutdown();
    assert!(!client.is_available());

    let mut restart = config(&fixture.connection, "second");
    restart.connection = connection;
    restart.capacity_limits.max_transaction_bytes = 1;
    let options = ServiceOptions {
        execution: atomic_core::TransactionExecutionOptions {
            defaults: TransactionDefaults::default()
                .with_default_partition(Keyword::new("missing", "partition")),
            ..Default::default()
        },
        ..Default::default()
    };
    let reopened = TransactionService::start_with_options(restart, options).unwrap();
    assert_eq!(reopened.identity(), identity);
    assert_eq!(reopened.recovery_stats().target_t, initial_basis + 4);
    let retry_context = OperationContext::diagnostic(OperationKind::Application);
    let retried = {
        let _scope = retry_context.enter();
        reopened.client().transact(submitted, WAIT).unwrap()
    };
    assert!(retried.replayed);
    assert_eq!(retried.tx_hash, first.tx_hash);
    assert_eq!(
        retried.db_after.datoms(IndexOrder::Eavt).unwrap(),
        first.db_after.datoms(IndexOrder::Eavt).unwrap()
    );
    assert_eq!(
        retried
            .diagnostics
            .as_ref()
            .unwrap()
            .report
            .stats
            .transaction
            .assessments,
        0
    );
    let rejected = reopened
        .client()
        .transact(request("new-too-wide", 9), WAIT)
        .unwrap_err();
    assert_eq!(rejected.category, ErrorCategory::Busy);
    assert_eq!(rejected.code, "storage/transaction-capacity");
    reopened.shutdown();
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let tables: Vec<String> = sql.query("SELECT tablename::text FROM pg_catalog.pg_tables WHERE schemaname=current_schema() ORDER BY tablename", &[]).unwrap().into_iter().map(|row| row.get(0)).collect();
    assert_eq!(
        tables,
        ["atomic_objects", "atomic_refs"],
        "service must not install or consult relational authority"
    );
}

#[test]
fn standby_takes_over_the_same_immutable_database_identity() {
    let Some((fixture, connection)) = fixture("block_service_standby") else {
        return;
    };
    let active = TransactionService::start(config(&fixture.connection, "active")).unwrap();
    let identity = active.identity();
    let first = active.client().transact(request("first", 1), WAIT).unwrap();
    let mut standby = TransactionStandby::start_with_options(
        TransactionServiceConfig {
            connection,
            ..config(&fixture.connection, "waiting")
        },
        ServiceOptions::default(),
        Duration::from_millis(10),
    )
    .unwrap();
    let deadline = Instant::now() + WAIT;
    while standby.status() != StandbyStatus::Waiting {
        assert!(
            Instant::now() < deadline,
            "contender did not wait for active writer"
        );
        assert!(standby.try_active().unwrap().is_none());
        std::thread::sleep(Duration::from_millis(5));
    }
    active.shutdown();
    let replacement = loop {
        if let Some(service) = standby.try_active().unwrap() {
            break service;
        }
        assert!(
            Instant::now() < deadline,
            "contender did not acquire released writer"
        );
        std::thread::sleep(Duration::from_millis(5));
    };
    assert_eq!(replacement.identity(), identity);
    let second = replacement
        .client()
        .transact(request("second", 2), WAIT)
        .unwrap();
    assert_eq!(second.db_before.basis_t(), first.basis_t);
    assert_eq!(second.basis_t, first.basis_t + 1);
    assert_eq!(
        first.db_after.values(first.tempids["item"], VALUE).unwrap(),
        vec![Value::Long(1)]
    );
    replacement.shutdown();
}
