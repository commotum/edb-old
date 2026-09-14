mod common;
use atomic_core::storage::{BlockDatabase, PgBlockStore};
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
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    BlockDatabase::create(&config, "source", schema).unwrap();
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
    BlockDatabase::create(&pg, "other", Schema::new()).unwrap();
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
            max_hint_bytes: 0,
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
    let report = peer
        .transact_remote(&endpoint, &client, request("after-timeout", 2), WAIT)
        .unwrap()
        .report
        .unwrap();
    let hints = TransactionHints::from_reads(
        report.db_after.snapshot_reference().unwrap(),
        [ReadHint {
            history: false,
            prefix: IndexPrefix::Eavt {
                entity: report.tempids["item"],
                attribute: Some(1000),
                value: None,
            },
        }],
        HintLimits::default(),
    )
    .unwrap();
    let ignored = peer
        .transact_remote_with_hints(
            &endpoint,
            &client,
            request("over-hint-policy", 3),
            hints,
            WAIT,
        )
        .unwrap();
    assert_eq!(ignored.basis_t, report.basis_t + 1);
    assert_eq!(server.stats().ignored_hints, 1);
    println!(
        "REMOTE_BOUNDS_OK max_active={} rejected_full={} cumulative_tls_deadline_ms=500 trickle_did_not_extend_deadline=true",
        server.stats().max_active,
        server.stats().rejected_full
    );
    drop(server);
    service.shutdown();
}

#[test]
fn connection_owned_route_refreshes_before_submission_without_replaying_unknown_outcomes() {
    let Some(fixture) = fixture("remote_owned_route") else {
        return;
    };
    let url = &fixture.connection;
    let pg = PostgresConnectionConfig::plaintext(url);
    let service = common::start_service(url, "source");
    let peer = Connection::connect(url, "source", 32).unwrap();
    let captured = peer.db();
    let (identity, root) = credentials();
    let token = RemoteAuthToken::from_bytes([0x24; 32]);
    let tls = RemoteClientConfig::new(token.clone())
        .unwrap()
        .with_root_certificate_pem(&root)
        .unwrap();
    let writer = peer.remote_writer(pg.clone(), tls);
    assert_eq!(writer.identity(), peer.identity());
    assert!(
        writer.cached_endpoint().is_none(),
        "construction must not discover"
    );
    assert_eq!(
        writer.refresh(Duration::ZERO).unwrap_err().code,
        "remote/timeout"
    );
    let (server, endpoint) = bind(
        identity.clone(),
        "127.0.0.1:0".parse().unwrap(),
        &service,
        RemoteTransportConfig::default(),
        token.clone(),
        &pg,
    );
    let started = Instant::now();
    let initial = writer.transact(request("owned-first", 1), WAIT).unwrap();
    assert_eq!(writer.cached_endpoint(), Some(endpoint.clone()));
    let first = initial.report.unwrap();
    assert_eq!(captured.basis_t() + 1, first.basis_t);
    server.inject_lost_next_committed_response();
    let unknown = writer.transact(request("owned-lost", 2), WAIT).unwrap_err();
    assert_eq!(
        unknown.category,
        ErrorCategory::UnknownOutcome,
        "the facade must not conceal ambiguity with an automatic exact retry"
    );
    assert!(writer.cached_endpoint().is_none());
    let exact = writer.transact(request("owned-lost", 2), WAIT).unwrap();
    assert!(exact.replayed);
    assert_eq!(exact.basis_t, first.basis_t + 1);
    let exact_hash = exact.tx_hash;

    // The same trusted certificate and socket now serve a new lease/instance.
    // The cached TLS hello is rejected before any request bytes are written;
    // the facade discovers the successor and submits the same owned data once.
    let address = server.local_addr();
    drop(server);
    service.shutdown();
    let replacement = common::start_service(url, "source");
    let (server, replacement_endpoint) = bind(
        identity,
        address,
        &replacement,
        RemoteTransportConfig::default(),
        token,
        &pg,
    );
    assert!(replacement_endpoint.lease_epoch() > endpoint.lease_epoch());
    let fresh = writer
        .transact(request("owned-after-takeover", 3), WAIT)
        .unwrap();
    assert!(!fresh.replayed);
    assert_eq!(fresh.basis_t, first.basis_t + 2);
    assert_eq!(writer.cached_endpoint(), Some(replacement_endpoint));
    let exact = writer.transact(request("owned-lost", 2), WAIT).unwrap();
    assert!(exact.replayed);
    assert_eq!(exact.tx_hash, exact_hash);

    let executor = AsyncExecutor::new(AsyncConfig {
        workers: 2,
        ..Default::default()
    })
    .unwrap();
    let async_client = AsyncClient::new(&peer, &executor).unwrap();
    let runtime = tokio::runtime::Builder::new_current_thread()
        .enable_all()
        .build()
        .unwrap();
    runtime.block_on(async {
        let endpoint = async_client
            .refresh_route(&writer, WAIT)
            .unwrap()
            .await
            .unwrap();
        assert_eq!(Some(endpoint), writer.cached_endpoint());
        let retried = async_client
            .transact_routed(&writer, request("owned-lost", 2), WAIT)
            .unwrap()
            .await
            .unwrap();
        assert!(retried.replayed);
        assert_eq!(retried.tx_hash, exact_hash);
        assert_eq!(retried.report.unwrap().basis_t, first.basis_t + 1);
    });

    let mut catalog = DatabaseCatalog::connect(url).unwrap();
    catalog.rename("source", "renamed").unwrap();
    let reused = catalog.create_if_absent("source", Schema::new()).unwrap();
    assert_ne!(reused.database.lineage_id, writer.identity().lineage_id());
    let after_rename = writer
        .transact(request("owned-after-rename", 4), WAIT)
        .unwrap();
    assert_eq!(after_rename.basis_t, first.basis_t + 3);
    assert_eq!(
        after_rename
            .report
            .unwrap()
            .db_after
            .snapshot_reference()
            .unwrap()
            .key()
            .lineage_id(),
        writer.identity().lineage_id()
    );
    assert_eq!(captured.basis_t(), first.basis_t - 1);
    let retired = catalog.retire("renamed").unwrap().unwrap();
    assert_eq!(retired.lineage_id, writer.identity().lineage_id());
    assert_eq!(
        writer.refresh(WAIT).unwrap_err().code,
        "catalog/database-retired"
    );
    assert!(
        writer
            .transact(request("owned-after-retire", 5), WAIT)
            .is_err()
    );
    assert_eq!(
        PostgresOperator::connect(url)
            .unwrap()
            .inspect_database(&reused.database.database_id, false)
            .unwrap()
            .metrics
            .basis_t,
        0
    );
    drop(server);
    replacement.shutdown();
    eprintln!(
        "REMOTE_OWNED_ROUTE_OK cached=true takeover=true unknown_not_replayed=true exact_retry=true async_routed=true rename_identity=true complete_ms={}",
        started.elapsed().as_millis()
    );
}

#[test]
fn stored_query_transaction_program_preview_and_exact_remote_retry_survive_replacement() {
    let Some(fixture) = fixture("remote_stored_program") else {
        return;
    };
    let url = &fixture.connection;
    let pg = PostgresConnectionConfig::plaintext(url);
    let make_program = |output: i64| {
        let mut query = Query::new(
            FindSpec::Relation(vec![FindElement::Variable("entity".into())]),
            vec![Clause::Pattern(Box::new(DataPattern::new(
                Term::var("entity"),
                Term::Constant(Value::Ref(1000)),
                Term::var("selected"),
            )))],
        );
        query.inputs = vec![InputSpec::Scalar("selected".into())];
        Program {
            kind: ProgramKind::Transaction,
            arity: 1,
            instructions: vec![
                Instruction::Query(
                    QueryTemplate::native(query, vec![0], vec![QueryTemplateSource::current("$")])
                        .unwrap(),
                ),
                Instruction::ForEach {
                    body: vec![
                        Instruction::Unpack(1),
                        Instruction::PushConstant(Value::Long(output)),
                        Instruction::EmitAdd(1000),
                    ],
                },
                Instruction::Return,
            ],
        }
    };
    let program = make_program(99);
    let program_bytes = encode_program(&program).unwrap();
    let mut store = PgBlockStore::connect(&pg).unwrap();
    let hash = store.put(&program_bytes).unwrap();
    assert_eq!(hash, sha256(&program_bytes));
    assert_eq!(
        decode_program(&store.get(hash).unwrap().unwrap()).unwrap(),
        program
    );
    let writer = common::start_service(url, "source");
    let peer = Connection::connect(url, "source", 128).unwrap();
    let (identity, root) = credentials();
    let token = RemoteAuthToken::from_bytes([0x52; 32]);
    let client = RemoteClientConfig::new(token.clone())
        .unwrap()
        .with_root_certificate_pem(&root)
        .unwrap();
    let (server, endpoint) = bind(
        identity,
        "127.0.0.1:0".parse().unwrap(),
        &writer,
        RemoteTransportConfig::default(),
        token.clone(),
        &pg,
    );
    let ident = Keyword::new("remote", "select-stored");
    let installed = peer
        .transact_remote(
            &endpoint,
            &client,
            TransactionRequest::new(
                "program-install",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("selector".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(ident.clone()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("selector".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("selected".into()),
                        attribute: 1000,
                        value: Value::Long(1).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("untouched".into()),
                        attribute: 1000,
                        value: Value::Long(2).into(),
                    },
                ],
            )
            .with_tx_instant(1000),
            WAIT,
        )
        .unwrap()
        .report
        .unwrap();
    let captured = installed.db_after.clone();
    let selected = installed.tempids["selected"];
    let untouched = installed.tempids["untouched"];
    let call = |value: i64| ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(ident.clone())),
        arguments: vec![RuntimeValue::Scalar(Value::Long(value))],
    };
    let preview = captured
        .with_forms(&[TxForm::ProgramCall(call(1))], 2000)
        .unwrap();
    assert_eq!(
        preview.db_after.values(selected, 1000).unwrap(),
        vec![Value::Long(99)]
    );
    assert_eq!(
        preview.db_after.values(untouched, 1000).unwrap(),
        vec![Value::Long(2)]
    );
    assert_eq!(
        captured.values(selected, 1000).unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(
        Peer::connect(url, "source", 0)
            .unwrap()
            .database_value()
            .basis_t(),
        installed.basis_t,
        "preview committed durable data"
    );
    let request = TransactionRequest::new("program-execute", vec![])
        .calling(call(1))
        .comparing_basis(installed.basis_t)
        .with_tx_instant(2000);
    let request_hash = submission_request_digest(
        &request.forms,
        request.compare_basis_t,
        request.tx_instant_override,
    )
    .unwrap();
    server.inject_lost_next_committed_response();
    assert_eq!(
        peer.transact_remote(&endpoint, &client, request.clone(), WAIT)
            .unwrap_err()
            .category,
        ErrorCategory::UnknownOutcome
    );
    let committed = peer
        .transact_remote(&endpoint, &client, request.clone(), WAIT)
        .unwrap();
    assert!(committed.replayed);
    let committed_hash = committed.tx_hash;
    let report = committed.report.unwrap();
    assert_eq!(report.tx_data, preview.tx_data);
    assert_eq!(report.tempids, preview.tempids);
    common::assert_same_information(&report.db_after, &preview.db_after);
    let address = server.local_addr();
    drop(server);
    writer.shutdown();
    let replacement = common::start_service(url, "source");
    let (identity, root) = credentials();
    let client = RemoteClientConfig::new(token.clone())
        .unwrap()
        .with_root_certificate_pem(&root)
        .unwrap();
    let (server, endpoint) = bind(
        identity,
        address,
        &replacement,
        RemoteTransportConfig::default(),
        token,
        &pg,
    );
    assert_eq!(peer.discover_remote_writer(&pg).unwrap(), endpoint);
    let next_hash = store
        .put(&encode_program(&make_program(101)).unwrap())
        .unwrap();
    let rebound = peer
        .transact_remote(
            &endpoint,
            &client,
            TransactionRequest::new(
                "program-rebind",
                vec![TxOp::Add {
                    entity: EntityRef::Ident(ident.clone()),
                    attribute: DB_FN as u32,
                    value: Value::Function(next_hash).into(),
                }],
            )
            .with_tx_instant(3000),
            WAIT,
        )
        .unwrap()
        .report
        .unwrap();
    // This exact retry predates the new binding and its compare-basis is stale.
    // It must return the old receipt, not re-run either stored program version.
    let retried = peer
        .transact_remote(&endpoint, &client, request.clone(), WAIT)
        .unwrap();
    assert!(retried.replayed);
    assert_eq!(retried.tx_hash, committed_hash);
    let replay = retried.report.unwrap();
    assert_eq!(replay.tx_data, report.tx_data);
    common::assert_same_information(&replay.db_before, &report.db_before);
    common::assert_same_information(&replay.db_after, &report.db_after);
    assert_eq!(
        submission_request_digest(
            &request.forms,
            request.compare_basis_t,
            request.tx_instant_override
        )
        .unwrap(),
        request_hash
    );
    assert_eq!(store.get(hash).unwrap().unwrap(), program_bytes);
    let fresh = peer
        .transact_remote(
            &endpoint,
            &client,
            TransactionRequest::new("program-execute-new", vec![])
                .calling(call(99))
                .comparing_basis(rebound.basis_t)
                .with_tx_instant(4000),
            WAIT,
        )
        .unwrap()
        .report
        .unwrap();
    assert_eq!(
        fresh.db_after.values(selected, 1000).unwrap(),
        vec![Value::Long(101)]
    );
    assert_eq!(
        fresh.db_after.values(untouched, 1000).unwrap(),
        vec![Value::Long(2)]
    );
    assert_eq!(
        captured
            .with_forms(&[TxForm::ProgramCall(call(1))], 2000)
            .unwrap()
            .tx_data,
        preview.tx_data
    );
    drop(server);
    replacement.shutdown();
    println!(
        "REMOTE_STORED_PROGRAM_OK native_query_body=true preview_matches_commit=true preview_not_durable=true lost_response_same_key_retry=true replacement_rebind_retry_original_receipt=true fresh_call_uses_new_binding=true captured_value_keeps_original_program=true request_and_program_bytes_preserved=true"
    );
}
