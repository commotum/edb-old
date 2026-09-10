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
        extension::{BasicConstraints, KeyUsage, SubjectAlternativeName},
    },
};
use std::io::Write;
use std::net::{SocketAddr, TcpStream};
use std::time::{Duration, Instant};

const WAIT: Duration = Duration::from_secs(10);
fn credentials() -> (Identity, Vec<u8>) {
    let key = PKey::from_rsa(Rsa::generate(2048).unwrap()).unwrap();
    let mut name = X509NameBuilder::new().unwrap();
    name.append_entry_by_text("CN", "localhost").unwrap();
    let name = name.build();
    let mut cert = X509::builder().unwrap();
    cert.set_version(2).unwrap();
    let serial = openssl::bn::BigNum::from_u32(1)
        .unwrap()
        .to_asn1_integer()
        .unwrap();
    cert.set_serial_number(&serial).unwrap();
    cert.set_subject_name(&name).unwrap();
    cert.set_issuer_name(&name).unwrap();
    cert.set_pubkey(&key).unwrap();
    cert.set_not_before(&Asn1Time::days_from_now(0).unwrap())
        .unwrap();
    cert.set_not_after(&Asn1Time::days_from_now(1).unwrap())
        .unwrap();
    cert.append_extension(BasicConstraints::new().critical().ca().build().unwrap())
        .unwrap();
    cert.append_extension(
        KeyUsage::new()
            .digital_signature()
            .key_encipherment()
            .key_cert_sign()
            .build()
            .unwrap(),
    )
    .unwrap();
    let san = SubjectAlternativeName::new()
        .dns("localhost")
        .ip("127.0.0.1")
        .build(&cert.x509v3_context(None, None))
        .unwrap();
    cert.append_extension(san).unwrap();
    cert.sign(&key, MessageDigest::sha256()).unwrap();
    let pem = cert.build().to_pem().unwrap();
    (
        Identity::from_pkcs8(&pem, &key.private_key_to_pem_pkcs8().unwrap()).unwrap(),
        pem,
    )
}
fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED real TLS transport: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    PostgresStore::connect(&fixture.connection)
        .unwrap()
        .create_database("source", schema)
        .unwrap();
    Some(fixture)
}
fn request(key: &str, n: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1000,
            value: Value::Long(n).into(),
        }],
    )
}
fn bind(
    identity: Identity,
    address: SocketAddr,
    service: &TransactionService,
    config: RemoteTransportConfig,
    token: RemoteAuthToken,
    pg: &PostgresConnectionConfig,
) -> (RemoteTransactionServer, RemoteWriterEndpoint) {
    let endpoint = RemoteTransactionEndpoint::bind(address, identity).unwrap();
    let server = endpoint.start(service.client(), config, token).unwrap();
    let published = server
        .publish(pg, server.local_addr(), "localhost")
        .unwrap();
    (server, published)
}

#[test]
fn verified_tls_authentication_discovery_hints_loss_and_writer_replacement() {
    let Some(fixture) = fixture("remote_transport") else {
        return;
    };
    let url = &fixture.connection;
    let pg = PostgresConnectionConfig::plaintext(url);
    let service = common::start_service(url, "source");
    let peer = Connection::connect(url, "source", 128).unwrap();
    let (identity, root) = credentials();
    let token = RemoteAuthToken::from_bytes([0x73; 32]);
    let config = RemoteClientConfig::new(token.clone())
        .unwrap()
        .with_root_certificate_pem(&root)
        .unwrap();
    assert!(!format!("{config:?}").contains(&"73".repeat(32)));
    assert_eq!(
        RemoteAuthToken::from_hex("not-a-token").unwrap_err().code,
        "remote/auth-token"
    );
    let (server, endpoint) = bind(
        identity,
        "127.0.0.1:0".parse().unwrap(),
        &service,
        RemoteTransportConfig::default(),
        token.clone(),
        &pg,
    );
    assert_eq!(peer.discover_remote_writer(&pg).unwrap(), endpoint);
    let untrusted = RemoteClientConfig::new(token.clone()).unwrap();
    assert_eq!(
        peer.transact_remote(&endpoint, &untrusted, request("untrusted", 0), WAIT)
            .unwrap_err()
            .code,
        "remote/tls"
    );
    let bad_token = RemoteClientConfig::new(RemoteAuthToken::from_bytes([0x91; 32]))
        .unwrap()
        .with_root_certificate_pem(&root)
        .unwrap();
    assert_eq!(
        peer.transact_remote(&endpoint, &bad_token, request("bad-auth", 0), WAIT)
            .unwrap_err()
            .category,
        ErrorCategory::Forbidden
    );
    let wrong_name = server
        .publish(&pg, server.local_addr(), "wrong.invalid")
        .unwrap();
    assert_eq!(
        peer.transact_remote(&wrong_name, &config, request("bad-name", 0), WAIT)
            .unwrap_err()
            .code,
        "remote/tls"
    );
    server
        .publish(&pg, server.local_addr(), "localhost")
        .unwrap();
    PostgresStore::connect(url)
        .unwrap()
        .create_database("other", Schema::new())
        .unwrap();
    PostgresIndexer::connect(url, "other")
        .unwrap()
        .consolidate()
        .unwrap();
    let other = Connection::connect(url, "other", 32).unwrap();
    assert_eq!(
        other
            .transact_remote(&endpoint, &config, request("wrong-lineage", 0), WAIT)
            .unwrap_err()
            .code,
        "remote/database-identity"
    );
    let first = peer
        .transact_remote(&endpoint, &config, request("first", 1), WAIT)
        .unwrap();
    let first_basis = first.basis_t;
    let first_report = first.report.unwrap();
    let entity = first_report.tempids["item"];
    assert_eq!(
        first_report.db_after.values(entity, 1000).unwrap(),
        vec![Value::Long(1)]
    );
    let hints = TransactionHints::from_reads(
        first_report.db_after.snapshot_reference().unwrap(),
        [ReadHint {
            history: false,
            prefix: IndexPrefix::Eavt {
                entity,
                attribute: Some(1000),
                value: None,
            },
        }],
        HintLimits::default(),
    )
    .unwrap();
    let hinted = peer
        .transact_remote_with_hints(
            &endpoint,
            &config,
            request("hinted", 2),
            hints.clone(),
            WAIT,
        )
        .unwrap();
    assert!(!hinted.replayed);
    let hinted_hash = hinted.tx_hash;
    let altered = TransactionHints::from_reads(
        hints.origin().clone(),
        [ReadHint {
            history: true,
            prefix: IndexPrefix::Aevt {
                attribute: 1000,
                entity: None,
                value: None,
            },
        }],
        HintLimits::default(),
    )
    .unwrap();
    let replay = peer
        .transact_remote_with_hints(&endpoint, &config, request("hinted", 2), altered, WAIT)
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, hinted_hash);
    let foreign = TransactionHints::from_reads(
        other.db().snapshot_reference().unwrap(),
        hints.reads().iter().cloned(),
        HintLimits::default(),
    )
    .unwrap();
    let replay = peer
        .transact_remote_with_hints(&endpoint, &config, request("hinted", 2), foreign, WAIT)
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, hinted_hash);
    server.inject_lost_next_committed_response();
    let lost = peer
        .transact_remote(&endpoint, &config, request("lost", 3), WAIT)
        .unwrap_err();
    assert_eq!(lost.category, ErrorCategory::UnknownOutcome);
    let retry = peer
        .transact_remote(&endpoint, &config, request("lost", 3), WAIT)
        .unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, first_basis + 2);
    let lost_hash = retry.tx_hash;
    let address = server.local_addr();
    service.shutdown();
    assert!(!server.is_available());
    assert_eq!(
        peer.discover_remote_writer(&pg).unwrap_err().code,
        "remote/no-writer"
    );
    assert!(server.publish(&pg, address, "localhost").is_err());
    drop(server);
    let replacement = common::start_service(url, "source");
    let (identity, replacement_root) = credentials();
    let replacement_config = RemoteClientConfig::new(token.clone())
        .unwrap()
        .with_root_certificate_pem(&replacement_root)
        .unwrap();
    let (new_server, new_endpoint) = bind(
        identity,
        address,
        &replacement,
        RemoteTransportConfig::default(),
        token,
        &pg,
    );
    assert!(new_endpoint.lease_epoch() > endpoint.lease_epoch());
    assert_ne!(new_endpoint.instance_id(), endpoint.instance_id());
    assert_eq!(
        peer.transact_remote(&endpoint, &replacement_config, request("stale", 4), WAIT)
            .unwrap_err()
            .code,
        "remote/stale-endpoint"
    );
    assert_eq!(peer.discover_remote_writer(&pg).unwrap(), new_endpoint);
    let retry = peer
        .transact_remote(&new_endpoint, &replacement_config, request("lost", 3), WAIT)
        .unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.tx_hash, lost_hash);
    let fresh = peer
        .transact_remote(
            &new_endpoint,
            &replacement_config,
            request("fresh", 4),
            WAIT,
        )
        .unwrap();
    assert_eq!(fresh.basis_t, first_basis + 3);
    drop(new_server);
    replacement.shutdown();
    println!(
        "REMOTE_TLS_OK verified_certificate=true verified_name=true token_rejection=true lineage_rejection=true live_lease_discovery=true changed_hints_same_receipt=true unknown_response_same_key_retry=true replacement_rediscovery=true stale_instance_rejected=true"
    );
}

#[test]
fn remote_handshake_deadline_and_inflight_admission_are_bounded() {
    let Some(fixture) = fixture("remote_bounds") else {
        return;
    };
    let url = &fixture.connection;
    let pg = PostgresConnectionConfig::plaintext(url);
    let service = common::start_service(url, "source");
    let peer = Connection::connect(url, "source", 32).unwrap();
    let (identity, root) = credentials();
    let token = RemoteAuthToken::from_bytes([9; 32]);
    let client = RemoteClientConfig::new(token.clone())
        .unwrap()
        .with_root_certificate_pem(&root)
        .unwrap();
    let (server, endpoint) = bind(
        identity,
        "127.0.0.1:0".parse().unwrap(),
        &service,
        RemoteTransportConfig {
            max_in_flight: 1,
            request_timeout: Duration::from_millis(500),
            ..Default::default()
        },
        token,
        &pg,
    );
    let mut slow = TcpStream::connect(server.local_addr()).unwrap();
    slow.write_all(&[0x16, 0x03, 0x03, 0x40, 0x00]).unwrap();
    let start = Instant::now();
    while server.stats().active == 0 && start.elapsed() < Duration::from_secs(2) {
        std::thread::sleep(Duration::from_millis(5));
    }
    assert_eq!(server.stats().active, 1);
    assert!(
        peer.transact_remote(&endpoint, &client, request("saturated", 1), WAIT)
            .is_err()
    );
    for _ in 0..12 {
        std::thread::sleep(Duration::from_millis(50));
        if slow.write_all(&[0]).is_err() {
            break;
        }
    }
    while server.stats().active != 0 && start.elapsed() < Duration::from_secs(2) {
        std::thread::sleep(Duration::from_millis(5));
    }
    assert_eq!(server.stats().active, 0);
    assert_eq!(server.stats().max_active, 1);
    assert!(server.stats().rejected_full >= 1);
    assert!(start.elapsed() < Duration::from_secs(2));
    peer.transact_remote(&endpoint, &client, request("after-timeout", 2), WAIT)
        .unwrap()
        .report
        .unwrap();
    println!(
        "REMOTE_BOUNDS_OK max_active={} rejected_full={} cumulative_tls_deadline_ms=500 trickle_did_not_extend_deadline=true",
        server.stats().max_active,
        server.stats().rejected_full
    );
    drop(server);
    service.shutdown();
}
