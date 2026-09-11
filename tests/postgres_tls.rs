mod common;
use atomic_core::{
    Attribute, Cardinality, ChangeConsumer, ChangeConsumerConfig, Connection, EntityRef,
    ErrorCategory, Keyword, Peer, PostgresConnectionConfig, Schema, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp, TxValue, Value, ValueType,
};
use std::fs;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const ITEM_VALUE: u32 = 1_000;

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
            ITEM_VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

#[test]
fn tls_configuration_fails_closed_and_redacts_connection_secrets() {
    let secret = "not-for-diagnostics";
    let tls = PostgresConnectionConfig::require_tls(format!(
        "host=/tmp/atomic.sock user=atomic password={secret}"
    ));
    let debug = format!("{tls:?}");
    assert!(!debug.contains(secret));
    assert!(!debug.contains("atomic.sock"));
    assert!(debug.contains("verified-tls"));

    let error = match tls.connect() {
        Ok(_) => panic!("TLS over a Unix-domain socket must be rejected before I/O"),
        Err(error) => error,
    };
    assert_eq!(error.category, ErrorCategory::Incorrect);
    assert_eq!(error.code, "postgres/tls-requires-tcp");
    assert!(!error.to_string().contains(secret));

    let error =
        match PostgresConnectionConfig::require_tls("invalid-option=yes password=also-secret")
            .connect()
        {
            Ok(_) => panic!("invalid PostgreSQL parameters must be rejected"),
            Err(error) => error,
        };
    assert_eq!(error.category, ErrorCategory::Incorrect);
    assert_eq!(error.code, "postgres/invalid-connection-config");
    assert!(!error.to_string().contains("also-secret"));

    let error = PostgresConnectionConfig::require_tls("host=localhost")
        .with_root_certificate_pem(b"not a PEM certificate")
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Incorrect);
    assert_eq!(error.code, "postgres/invalid-tls-root-certificate");

    let error = PostgresConnectionConfig::plaintext("host=localhost")
        .with_root_certificate_pem(b"not a PEM certificate")
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Incorrect);
    assert_eq!(error.code, "postgres/tls-not-enabled");
}

/// Real evidence requires a TCP PostgreSQL server configured with:
///
/// - a private server certificate valid for the URL host;
/// - `hostssl` access for the test user and no matching plaintext `host` rule;
/// - `ATOMIC_POSTGRES_TLS_URL` containing `sslmode=disable` (the secure config
///   must override it); and
/// - `ATOMIC_POSTGRES_TLS_ROOT_CERT` naming the private CA/server PEM file.
///
/// With either environment variable absent this is a skip, not TLS evidence.
#[test]
fn required_tls_covers_store_writer_indexer_peer_listener_and_consumer() {
    let (Ok(connection), Ok(root_path)) = (
        std::env::var("ATOMIC_POSTGRES_TLS_URL"),
        std::env::var("ATOMIC_POSTGRES_TLS_ROOT_CERT"),
    ) else {
        eprintln!("SKIP: ATOMIC_POSTGRES_TLS_URL and ATOMIC_POSTGRES_TLS_ROOT_CERT required");
        return;
    };
    let root_pem = fs::read(root_path).unwrap();
    let tls = PostgresConnectionConfig::require_tls(connection.clone())
        .with_root_certificate_pem(root_pem)
        .unwrap();

    // Prove the actual transport, not merely that PostgreSQL accepted a
    // connection while TLS happened to be configured on the server.
    let mut probe = tls.connect().unwrap();
    let transport = probe
        .query_one(
            "SELECT ssl, version FROM pg_stat_ssl WHERE pid = pg_backend_pid()",
            &[],
        )
        .unwrap();
    let encrypted: bool = transport.get(0);
    let tls_version: String = transport.get(1);
    assert!(encrypted);
    assert!(matches!(tls_version.as_str(), "TLSv1.2" | "TLSv1.3"));

    // The fixture certificate is deliberately private. Omitting its trust
    // root must fail with the stable TLS boundary rather than leaking the
    // driver's diagnostic (which can include connection parameters).
    let untrusted = PostgresConnectionConfig::require_tls(connection.clone());
    let error = match untrusted.connect() {
        Ok(_) => panic!("private test certificate unexpectedly used a platform trust root"),
        Err(error) => error,
    };
    assert_eq!(error.category, ErrorCategory::Unavailable);
    assert_eq!(error.code, "postgres/tls-connect");

    // The same URL explicitly requests sslmode=disable. The hostssl-only
    // fixture must reject it, while `require_tls` above overrides the string.
    assert!(
        PostgresConnectionConfig::plaintext(connection.clone())
            .connect()
            .is_err()
    );

    atomic_core::storage::PgBlockStore::install(&tls).unwrap();
    let database_id = unique("tls_runtime");
    let created = common::TestStore::connect_configured(&tls)
        .unwrap()
        .create_database(&database_id, schema())
        .unwrap();
    let service = TransactionService::start_configured(
        TransactionServiceConfig {
            // The configured entry point must never fall back to this legacy
            // field for seed, lease, worker, cleanup, or index connections.
            connection: "invalid-option=must-not-be-used".into(),
            database_id: database_id.clone(),
            holder_id: unique("tls-writer"),
            lease_duration: Duration::from_secs(2),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 4,
            capacity_limits: atomic_core::CapacityLimits::default(),
        },
        tls.clone(),
    )
    .unwrap();
    let observer = Connection::connect_configured(tls.clone(), &database_id, 8).unwrap();
    let mut consumer = ChangeConsumer::connect_configured(
        tls.clone(),
        &database_id,
        "tls-observer",
        ChangeConsumerConfig::default(),
    )
    .unwrap();
    // Consume creation's ordinary schema transaction before blocking for the
    // next commit. Checkpoint reads/writes use the same verified configuration.
    while consumer.checkpoint().last_t() < created.basis_t() {
        let event = consumer.next(Duration::ZERO).unwrap().unwrap();
        consumer.acknowledge(&event.checkpoint).unwrap();
    }
    let entry = atomic_core::DatabaseCatalog::connect_configured(&tls)
        .unwrap()
        .resolve(&database_id)
        .unwrap();
    let namespace = atomic_core::storage::PgBlockStore::connect(&tls)
        .unwrap()
        .namespace()
        .to_owned();
    let mut channel_input = namespace.into_bytes();
    channel_input.push(0);
    channel_input.extend_from_slice(format!("databases/{}", entry.database_id).as_bytes());
    let channel = format!(
        "atomic_r_{}",
        atomic_core::sha256(&channel_input)[..24]
            .iter()
            .map(|byte| format!("{byte:02x}"))
            .collect::<String>()
    );
    let listen = format!("LISTEN {channel}");
    let deadline = Instant::now() + Duration::from_secs(5);
    let listener_versions = loop {
        let rows=probe.query("SELECT s.ssl,s.version FROM pg_stat_activity a JOIN pg_stat_ssl s USING(pid) WHERE a.datname=current_database() AND a.query=$1", &[&listen]).unwrap();
        if rows.len() >= 2 {
            let versions = rows
                .iter()
                .map(|row| {
                    assert!(
                        row.get::<_, bool>(0),
                        "asynchronous listener connected without TLS"
                    );
                    let version: String = row.get(1);
                    assert!(matches!(version.as_str(), "TLSv1.2" | "TLSv1.3"));
                    version
                })
                .collect::<Vec<_>>();
            break versions;
        }
        assert!(
            Instant::now() < deadline,
            "connection and consumer TLS listeners did not register"
        );
        std::thread::sleep(Duration::from_millis(10));
    };
    for error in [
        Connection::connect_configured(untrusted.clone(), &database_id, 8)
            .err()
            .unwrap(),
        ChangeConsumer::connect_configured(
            untrusted.clone(),
            &database_id,
            "untrusted",
            ChangeConsumerConfig::default(),
        )
        .err()
        .unwrap(),
    ] {
        assert_eq!(error.category, ErrorCategory::Unavailable);
        assert_eq!(error.code, "postgres/tls-connect");
    }
    let waiter = std::thread::spawn(move || {
        let event = consumer
            .next(Duration::from_secs(5))
            .unwrap()
            .expect("verified TLS listener did not wake the consumer");
        (consumer, event)
    });
    std::thread::sleep(Duration::from_millis(100));
    let began = Instant::now();
    let report = service
        .client()
        .transact(
            TransactionRequest::new(
                "tls-transaction",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: ITEM_VALUE,
                    value: TxValue::Scalar(Value::Long(42)),
                }],
            ),
            Duration::from_secs(5),
        )
        .unwrap();
    let (mut consumer, event) = waiter.join().unwrap();
    assert_eq!(event.transaction.t, report.basis_t);
    assert_eq!(event.transaction.data, report.tx_data);
    assert!(consumer.stats().notices > 0);
    consumer.acknowledge(&event.checkpoint).unwrap();
    let target = service.client().request_index().unwrap().target_t;
    assert!(
        observer
            .sync_index(target, Duration::from_secs(10))
            .unwrap()
            .basis_t()
            >= report.basis_t
    );
    let deadline = Instant::now() + Duration::from_secs(3);
    while observer.db().basis_t() < report.basis_t {
        assert!(
            Instant::now() < deadline,
            "verified TLS peer listener did not observe commit"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
    let elapsed = began.elapsed();
    drop(consumer);
    let resumed = ChangeConsumer::connect_configured(
        tls.clone(),
        &database_id,
        "tls-observer",
        ChangeConsumerConfig::default(),
    )
    .unwrap();
    assert_eq!(resumed.checkpoint().last_t(), report.basis_t);
    eprintln!(
        "postgres_tls async_listeners={} versions={listener_versions:?} consumer_basis={} commit_to_observation_us={} untrusted_root=rejected plaintext=rejected",
        listener_versions.len(),
        resumed.checkpoint().last_t(),
        elapsed.as_micros()
    );
    service.shutdown();

    let peer = Peer::connect_configured(&tls, &database_id, 8).unwrap();
    assert_eq!(peer.basis_t(), report.basis_t);
    assert_eq!(
        peer.database_value()
            .values(report.tempids["item"], ITEM_VALUE)
            .unwrap(),
        vec![Value::Long(42)]
    );
    assert!(created.basis_t() < peer.basis_t());
}
