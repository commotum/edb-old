mod common;
#[path = "common/current_refs.rs"]
mod current;
use atomic_core::{
    Attribute, CapacityLimits, Cardinality, EntityRef, ErrorCategory, Keyword,
    PostgresConnectionConfig, PostgresIoPolicy, Schema, TransactionRequest, TransactionService,
    TransactionServiceConfig, TxOp, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const ITEM: u32 = 1_000;

fn parameter(connection: &str, name: &str, value: &str) -> String {
    if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
        format!(
            "{connection}{}{name}={value}",
            if connection.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{connection} {name}={value}")
    }
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
            ITEM,
            Keyword::new("item", "value"),
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
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM,
            value: Value::Long(value).into(),
        }],
    )
    .with_tx_instant(value + 10)
}

fn setup(postgres: &str, prefix: &str, policy: PostgresIoPolicy) -> (String, TransactionService) {
    common::install(postgres).unwrap();
    let id = unique(prefix);
    common::TestStore::connect(postgres)
        .unwrap()
        .create_database(&id, schema())
        .unwrap();
    let connection =
        PostgresConnectionConfig::plaintext(parameter(postgres, "application_name", &id))
            .with_io_policy(policy)
            .unwrap();
    let service = TransactionService::start_configured(
        TransactionServiceConfig {
            connection: "invalid-option=legacy-must-not-be-used".into(),
            database_id: id.clone(),
            holder_id: unique("io_writer"),
            lease_duration: Duration::from_secs(15),
            renew_interval: Duration::from_secs(5),
            queue_capacity: 8,
            capacity_limits: CapacityLimits::default(),
        },
        connection,
    )
    .unwrap();
    (id, service)
}

fn head(connection: &str, database: &str) -> (u64, Option<[u8; 32]>) {
    let root = current::root(connection, database);
    (root.basis, root.log)
}

#[test]
fn server_statement_and_lock_timeouts_fire_and_the_connection_remains_usable() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let connection = PostgresConnectionConfig::plaintext(parameter(
        &postgres,
        "application_name",
        "io-policy-options",
    ))
    .with_io_policy(PostgresIoPolicy {
        connect_timeout: Some(Duration::from_secs(1)),
        statement_timeout: Some(Duration::from_millis(150)),
        lock_timeout: Some(Duration::from_millis(75)),
        ..PostgresIoPolicy::default()
    })
    .unwrap();
    let mut client = connection.connect().unwrap();
    let settings = client.query_one("SELECT current_setting('application_name'), current_setting('statement_timeout'), current_setting('lock_timeout')", &[]).unwrap();
    assert_eq!(settings.get::<_, String>(0), "io-policy-options");
    assert_eq!(settings.get::<_, String>(1), "150ms");
    assert_eq!(settings.get::<_, String>(2), "75ms");
    let began = Instant::now();
    let error = client.query_one("SELECT pg_sleep(2)", &[]).unwrap_err();
    let statement_elapsed = began.elapsed();
    assert_eq!(error.code().unwrap().code(), "57014");
    assert!(
        statement_elapsed >= Duration::from_millis(100)
            && statement_elapsed < Duration::from_secs(2),
        "statement elapsed {statement_elapsed:?}"
    );
    assert_eq!(
        client.query_one("SELECT 1", &[]).unwrap().get::<_, i32>(0),
        1
    );

    // The blocked read is against a test-owned table, so no other logical
    // database or migration is held behind an ACCESS EXCLUSIVE lock.
    let table = unique("io_policy_probe");
    let mut blocker = Client::connect(&postgres, NoTls).unwrap();
    blocker
        .batch_execute(&format!("CREATE TABLE {table} (value integer)"))
        .unwrap();
    let mut held = blocker.transaction().unwrap();
    held.batch_execute(&format!("LOCK TABLE {table} IN ACCESS EXCLUSIVE MODE"))
        .unwrap();
    let began = Instant::now();
    let error = client
        .query(&format!("SELECT value FROM {table}"), &[])
        .unwrap_err();
    let lock_elapsed = began.elapsed();
    assert_eq!(error.code().unwrap().code(), "55P03");
    assert!(
        lock_elapsed >= Duration::from_millis(40) && lock_elapsed < Duration::from_secs(2),
        "lock elapsed {lock_elapsed:?}"
    );
    held.rollback().unwrap();
    assert!(
        client
            .query(&format!("SELECT value FROM {table}"), &[])
            .unwrap()
            .is_empty()
    );
    blocker
        .batch_execute(&format!("DROP TABLE {table}"))
        .unwrap();
    eprintln!(
        "I/O policy: statement150ms observed {statement_elapsed:?}; lock75ms observed {lock_elapsed:?}; connection reusable"
    );
}

#[test]
fn public_publication_lock_rejection_and_shorter_unknown_wait_preserve_retry_outcomes() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let (id, service) = setup(
        &postgres,
        "io_outcomes",
        PostgresIoPolicy {
            statement_timeout: Some(Duration::from_secs(1)),
            lock_timeout: Some(Duration::from_millis(300)),
            ..PostgresIoPolicy::default()
        },
    );
    let client = service.client();
    let reports = client.subscribe_reports();
    let initial = head(&postgres, &id);
    let mut blocker = Client::connect(&postgres, NoTls).unwrap();
    let mut held = blocker.transaction().unwrap();
    held.query_one(
        "SELECT revision FROM atomic_refs WHERE key=$1 FOR UPDATE",
        &[&current::root_key(&postgres, &id)],
    )
    .unwrap();
    let known_request = request("private-known-lock-key", 1);
    let began = Instant::now();
    let rejected = client
        .transact(known_request.clone(), Duration::from_secs(3))
        .unwrap_err();
    let rejected_elapsed = began.elapsed();
    assert_eq!(rejected.category, ErrorCategory::Busy);
    assert_eq!(rejected.details["postgres_sqlstate"], "55P03");
    assert!(!rejected.details.contains_key("postgres_transport"));
    assert!(!format!("{rejected:?}").contains("private-known-lock-key"));
    assert!(
        rejected_elapsed >= Duration::from_millis(200) && rejected_elapsed < Duration::from_secs(3)
    );
    assert_eq!(head(&postgres, &id), initial);
    held.rollback().unwrap();
    let committed = client
        .transact(known_request.clone(), Duration::from_secs(3))
        .unwrap();
    assert!(!committed.replayed);
    assert_eq!(committed.basis_t, initial.0 as u64 + 1);
    let replay = client
        .transact(known_request, Duration::from_secs(3))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, committed.tx_hash);
    assert_eq!(replay.tempids, committed.tempids);

    let mut held = blocker.transaction().unwrap();
    held.query_one(
        "SELECT revision FROM atomic_refs WHERE key=$1 FOR UPDATE",
        &[&current::root_key(&postgres, &id)],
    )
    .unwrap();
    let unknown_request = request("private-unknown-wait-key", 2);
    let ticket = client.submit(unknown_request.clone()).unwrap();
    let began = Instant::now();
    let unknown = ticket.wait(Duration::from_millis(20)).unwrap_err();
    let unknown_elapsed = began.elapsed();
    assert_eq!(unknown.category, ErrorCategory::UnknownOutcome);
    assert!(!format!("{unknown:?}").contains("private-unknown-wait-key"));
    assert_eq!(unknown.details["request_key_hash"].len(), 64);
    held.rollback().unwrap();
    // Timing out the caller does not cancel an accepted transaction. FIFO
    // same-key retry establishes its authoritative receipt, not a guess.
    let reconciled = client
        .transact(unknown_request, Duration::from_secs(3))
        .unwrap();
    assert!(reconciled.replayed);
    assert_eq!(reconciled.basis_t, committed.basis_t + 1);
    assert_eq!(
        reports
            .recv_timeout(Duration::from_secs(1))
            .unwrap()
            .tx_hash,
        committed.tx_hash
    );
    assert_eq!(
        reports
            .recv_timeout(Duration::from_secs(1))
            .unwrap()
            .tx_hash,
        reconciled.tx_hash
    );
    assert_eq!(reports.pending_reports(), 0);
    assert_eq!(head(&postgres, &id).0 as u64, reconciled.basis_t);
    assert_eq!(
        committed
            .db_after
            .values(committed.tempids["item"], ITEM)
            .unwrap(),
        [Value::Long(1)]
    );
    service.shutdown();
    assert_eq!(
        common::TestStore::connect(&postgres)
            .unwrap()
            .recover(&id)
            .unwrap()
            .basis_t(),
        reconciled.basis_t
    );
    eprintln!(
        "I/O outcomes: known300ms lock rejection {rejected_elapsed:?}; caller20ms unknown {unknown_elapsed:?}; retry returned committed t{} without duplicate report",
        reconciled.basis_t
    );
}

#[test]
fn public_statement_cancellation_before_commit_is_interrupted_and_retryable() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let (id, service) = setup(
        &postgres,
        "io_statement",
        PostgresIoPolicy {
            statement_timeout: Some(Duration::from_millis(250)),
            ..PostgresIoPolicy::default()
        },
    );
    let client = service.client();
    let initial = head(&postgres, &id);
    let mut blocker = Client::connect(&postgres, NoTls).unwrap();
    let mut held = blocker.transaction().unwrap();
    held.query_one(
        "SELECT revision FROM atomic_refs WHERE key=$1 FOR UPDATE",
        &[&current::root_key(&postgres, &id)],
    )
    .unwrap();
    let request = request("statement-timeout", 3);
    let began = Instant::now();
    let error = client
        .transact(request.clone(), Duration::from_secs(3))
        .unwrap_err();
    let elapsed = began.elapsed();
    assert_eq!(error.category, ErrorCategory::Interrupted);
    assert_eq!(error.details["postgres_sqlstate"], "57014");
    assert!(!error.details.contains_key("postgres_transport"));
    assert!(elapsed >= Duration::from_millis(150) && elapsed < Duration::from_secs(3));
    assert_eq!(head(&postgres, &id), initial);
    held.rollback().unwrap();
    let committed = client.transact(request, Duration::from_secs(3)).unwrap();
    assert!(!committed.replayed);
    assert_eq!(committed.basis_t, initial.0 as u64 + 1);
    service.shutdown();
    eprintln!(
        "I/O outcomes: statement250ms canceled blocked publication guard after {elapsed:?}; no head advance until explicit retry"
    );
}

#[test]
fn verified_tls_applies_the_same_sql_timeout_policy() {
    let (Ok(postgres), Ok(root_path)) = (
        std::env::var("ATOMIC_POSTGRES_TLS_URL"),
        std::env::var("ATOMIC_POSTGRES_TLS_ROOT_CERT"),
    ) else {
        return;
    };
    let connection = PostgresConnectionConfig::require_tls(postgres)
        .with_root_certificate_pem(std::fs::read(root_path).unwrap())
        .unwrap()
        .with_io_policy(PostgresIoPolicy {
            connect_timeout: Some(Duration::from_secs(1)),
            statement_timeout: Some(Duration::from_millis(120)),
            lock_timeout: Some(Duration::from_millis(40)),
            ..PostgresIoPolicy::default()
        })
        .unwrap();
    let mut client = connection.connect().unwrap();
    let row = client.query_one("SELECT ssl, version, current_setting('statement_timeout'), current_setting('lock_timeout') FROM pg_stat_ssl WHERE pid=pg_backend_pid()", &[]).unwrap();
    assert!(row.get::<_, bool>(0));
    assert!(matches!(
        row.get::<_, String>(1).as_str(),
        "TLSv1.2" | "TLSv1.3"
    ));
    assert_eq!(row.get::<_, String>(2), "120ms");
    assert_eq!(row.get::<_, String>(3), "40ms");
    let began = Instant::now();
    let error = client.query_one("SELECT pg_sleep(2)", &[]).unwrap_err();
    let elapsed = began.elapsed();
    assert_eq!(error.code().unwrap().code(), "57014");
    assert!(elapsed >= Duration::from_millis(80) && elapsed < Duration::from_secs(2));
    assert_eq!(
        client.query_one("SELECT 1", &[]).unwrap().get::<_, i32>(0),
        1
    );
    eprintln!(
        "TLS I/O policy: verified{}, statement120ms observed {elapsed:?}, connection reusable",
        row.get::<_, String>(1)
    );
}
