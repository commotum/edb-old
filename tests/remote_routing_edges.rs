#![cfg(unix)]
mod common;
use atomic_core::*;
use native_tls::Identity;
use openssl::{
    asn1::Asn1Time,
    hash::MessageDigest,
    pkey::PKey,
    rsa::Rsa,
    x509::{
        X509, X509NameBuilder,
        extension::{BasicConstraints, SubjectAlternativeName},
    },
};
use std::time::{Duration, Instant};

fn credentials() -> (Identity, Vec<u8>) {
    let key = PKey::from_rsa(Rsa::generate(2048).unwrap()).unwrap();
    let mut name = X509NameBuilder::new().unwrap();
    name.append_entry_by_text("CN", "localhost").unwrap();
    let name = name.build();
    let mut certificate = X509::builder().unwrap();
    certificate.set_version(2).unwrap();
    certificate
        .set_serial_number(
            &openssl::bn::BigNum::from_u32(1)
                .unwrap()
                .to_asn1_integer()
                .unwrap(),
        )
        .unwrap();
    certificate.set_subject_name(&name).unwrap();
    certificate.set_issuer_name(&name).unwrap();
    certificate.set_pubkey(&key).unwrap();
    certificate
        .set_not_before(&Asn1Time::days_from_now(0).unwrap())
        .unwrap();
    certificate
        .set_not_after(&Asn1Time::days_from_now(1).unwrap())
        .unwrap();
    certificate
        .append_extension(BasicConstraints::new().critical().ca().build().unwrap())
        .unwrap();
    let san = SubjectAlternativeName::new()
        .dns("localhost")
        .build(&certificate.x509v3_context(None, None))
        .unwrap();
    certificate.append_extension(san).unwrap();
    certificate.sign(&key, MessageDigest::sha256()).unwrap();
    let pem = certificate.build().to_pem().unwrap();
    (
        Identity::from_pkcs8(&pem, &key.private_key_to_pem_pkcs8().unwrap()).unwrap(),
        pem,
    )
}

#[test]
fn routed_known_commit_survives_peer_report_read_failure_and_exact_retry() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP routing edge: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::product_support::Fixture::new(&url);
    let Some((writer_role, peer_role)) = &fixture.roles else {
        eprintln!("SKIP routing edge: isolated runtime role creation unavailable");
        return;
    };
    let mut migrator = PostgresMigrator::connect(&fixture.admin_url).unwrap();
    migrator.migrate().unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    DatabaseCatalog::connect(&fixture.admin_url)
        .unwrap()
        .create_if_absent("items", schema)
        .unwrap();
    migrator
        .grant_runtime_privileges(writer_role, peer_role)
        .unwrap();
    let service = TransactionService::start(TransactionServiceConfig {
        connection: fixture.writer_url.clone(),
        database_id: "items".into(),
        holder_id: "route-edge-writer".into(),
        lease_duration: Duration::from_secs(10),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    })
    .unwrap();
    let peer = Connection::connect(&fixture.peer_url, "items", 0).unwrap();
    let before = peer.db();
    let (identity, certificate) = credentials();
    let token = RemoteAuthToken::from_bytes([0x79; 32]);
    let server = RemoteTransactionEndpoint::bind("127.0.0.1:0".parse().unwrap(), identity)
        .unwrap()
        .start(
            service.client(),
            RemoteTransportConfig::default(),
            token.clone(),
        )
        .unwrap();
    let published = server
        .publish(
            &PostgresConnectionConfig::plaintext(&fixture.writer_url),
            server.local_addr(),
            "localhost",
        )
        .unwrap();
    let client = RemoteClientConfig::new(token)
        .unwrap()
        .with_root_certificate_pem(&certificate)
        .unwrap();
    let route = peer.remote_writer(
        PostgresConnectionConfig::plaintext(&fixture.peer_url),
        client,
    );
    assert_eq!(route.refresh(Duration::from_secs(10)).unwrap(), published);

    let mut admin = postgres::Client::connect(&fixture.admin_url, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!(
            "REVOKE SELECT ON atomic_tree_manifests,atomic_generation_transactions FROM {peer_role}"
        ))
        .unwrap();
    let mut denied_peer = postgres::Client::connect(&fixture.peer_url, postgres::NoTls).unwrap();
    let denied = denied_peer
        .query("SELECT 1 FROM atomic_tree_manifests LIMIT 1", &[])
        .unwrap_err();
    assert_eq!(denied.code().map(|code| code.code()), Some("42501"));
    drop(denied_peer);
    let request = TransactionRequest::new(
        "known-commit-report-unreadable",
        vec![TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1000,
            value: Value::Long(7).into(),
        }],
    );
    let started = Instant::now();
    let committed = route
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    assert!(!committed.replayed);
    assert_eq!(committed.basis_t, before.basis_t() + 1);
    let report_error = committed.report.unwrap_err();
    assert_eq!(report_error.category, ErrorCategory::Forbidden);
    assert_eq!(
        report_error
            .details
            .get("postgres_sqlstate")
            .map(String::as_str),
        Some("42501")
    );
    eprintln!("known commit retained after local report failure: {report_error}");
    assert_eq!(
        route.cached_endpoint(),
        Some(published),
        "a local report failure does not invalidate the successful route"
    );
    assert_eq!(
        PostgresStore::connect(&fixture.admin_url)
            .unwrap()
            .database_status("items")
            .unwrap()
            .basis_t,
        committed.basis_t
    );
    admin
        .batch_execute(&format!(
            "GRANT SELECT ON atomic_tree_manifests,atomic_generation_transactions TO {peer_role}"
        ))
        .unwrap();
    let retry = route.transact(request, Duration::from_secs(10)).unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, committed.basis_t);
    assert_eq!(retry.tx_hash, committed.tx_hash);
    let report = retry.report.unwrap();
    assert_eq!(
        report
            .db_after
            .values(report.tempids["item"], 1000)
            .unwrap(),
        [Value::Long(7)]
    );
    assert!(
        before
            .values(report.tempids["item"], 1000)
            .unwrap()
            .is_empty()
    );
    eprintln!(
        "complete routed known-commit/report-denial/restore-read/exact-retry/check {:?}",
        started.elapsed()
    );
    drop(report);
    drop(server);
    service.shutdown();
}
