use atomic_core::*;
use std::time::{Duration, Instant};
mod common;

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP automatic excision: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    Some(fixture)
}

#[test]
fn committed_request_is_processed_by_the_first_background_index_job() {
    let Some(fixture) = fixture("automatic_excision") else {
        return;
    };
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("person", "secret"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    PostgresStore::connect(&fixture.connection)
        .unwrap()
        .create_database("people", schema)
        .unwrap();
    let service = common::start_service(&fixture.connection, "people");
    let seeded = service
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("person".into()),
                    attribute: 1000,
                    value: Value::String("private".into()).into(),
                }],
            )
            .with_tx_instant(1),
            Duration::from_secs(30),
        )
        .unwrap();
    let entity = seeded.tempids["person"];
    let requested = service
        .client()
        .transact(
            TransactionRequest::new(
                "excise",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE as u32,
                    value: Value::Ref(entity).into(),
                }],
            )
            .with_tx_instant(2),
            Duration::from_secs(30),
        )
        .unwrap();
    service.client().request_index().unwrap();
    let mut operator = PostgresOperator::connect(&fixture.connection).unwrap();
    let started = Instant::now();
    let completed = loop {
        if operator.sync_excise("people", requested.basis_t).unwrap() {
            break true;
        }
        if started.elapsed() > Duration::from_secs(5) {
            break false;
        }
        assert!(service.background_indexing_stats().last_failure.is_none());
        std::thread::sleep(Duration::from_millis(20));
    };
    service.shutdown();
    assert!(
        completed,
        "first ordinary indexing job left the committed excision pending"
    );
}

struct RuntimeRoles {
    admin: postgres::Client,
    writer: String,
    peer: String,
}
impl RuntimeRoles {
    fn new(fixture: &common::PostgresFixture) -> Self {
        let suffix = format!(
            "{}_{}",
            std::process::id(),
            std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let writer = format!("auto_writer_{suffix}");
        let peer = format!("auto_peer_{suffix}");
        let mut admin = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
        admin.batch_execute(&format!("CREATE ROLE {writer} LOGIN PASSWORD 'isolated-auto'; CREATE ROLE {peer} LOGIN PASSWORD 'isolated-auto'" )).unwrap();
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .grant_runtime_privileges(&writer, &peer)
            .unwrap();
        Self {
            admin,
            writer,
            peer,
        }
    }
    fn writer_url(&self, fixture: &common::PostgresFixture) -> String {
        format!(
            "{} user={} password=isolated-auto",
            fixture.connection, self.writer
        )
    }
}
impl Drop for RuntimeRoles {
    fn drop(&mut self) {
        let _ = self.admin.batch_execute(&format!(
            "DROP OWNED BY {}; DROP OWNED BY {}; DROP ROLE {}; DROP ROLE {}",
            self.writer, self.peer, self.writer, self.peer
        ));
    }
}
fn start(url: &str, name: &str, excision: ExcisionConfig) -> TransactionService {
    TransactionService::start_configured_with_options(
        TransactionServiceConfig {
            connection: String::new(),
            database_id: name.into(),
            holder_id: format!("writer-{name}"),
            lease_duration: Duration::from_secs(5),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 8,
            capacity_limits: CapacityLimits::default(),
        },
        PostgresConnectionConfig::plaintext(url),
        ServiceOptions {
            excision,
            ..Default::default()
        },
    )
    .unwrap()
}
fn create(url: &str, name: &str) {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("person", "secret"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    PostgresStore::connect(url)
        .unwrap()
        .create_database(name, schema)
        .unwrap();
}
fn commit(
    service: &TransactionService,
    key: &str,
    entity: EntityRef,
    attribute: u32,
    value: Value,
) -> ServiceTransactionReport {
    service
        .client()
        .transact(
            TransactionRequest::new(
                key,
                vec![TxOp::Add {
                    entity,
                    attribute,
                    value: value.into(),
                }],
            ),
            Duration::from_secs(30),
        )
        .unwrap()
}
fn wait_index(service: &TransactionService, basis: u64) {
    service.client().request_index().unwrap();
    let deadline = Instant::now() + Duration::from_secs(30);
    loop {
        let stats = service.background_indexing_stats();
        assert!(stats.last_failure.is_none(), "{stats:?}");
        if stats.published_basis_t >= basis {
            break;
        }
        assert!(Instant::now() < deadline, "{stats:?}");
        std::thread::sleep(Duration::from_millis(10));
    }
}
fn wait_excise(url: &str, name: &str, basis: u64, service: &TransactionService) {
    let mut operator = PostgresOperator::connect(url).unwrap();
    let deadline = Instant::now() + Duration::from_secs(30);
    loop {
        let stats = service.background_indexing_stats();
        assert!(stats.last_failure.is_none(), "{stats:?}");
        if operator.sync_excise(name, basis).unwrap() {
            break;
        }
        assert!(Instant::now() < deadline, "{stats:?}");
        std::thread::sleep(Duration::from_millis(10));
    }
}

#[test]
fn restricted_writer_resumes_each_durable_boundary_and_follows_the_new_generation() {
    let Some(fixture) = fixture("automatic_restart") else {
        return;
    };
    let roles = RuntimeRoles::new(&fixture);
    let runtime = roles.writer_url(&fixture);
    assert_eq!(
        postgres::Client::connect(&runtime, postgres::NoTls)
            .unwrap()
            .query_one("SELECT current_user::text", &[])
            .unwrap()
            .get::<_, String>(0),
        roles.writer
    );
    for (name, fault) in [
        ("capture", ExcisionFault::AfterCapture),
        ("candidate", ExcisionFault::AfterCandidateStaged),
        ("activation", ExcisionFault::AfterActivation),
    ] {
        create(&fixture.connection, name);
        let service = start(
            &runtime,
            name,
            ExcisionConfig {
                enabled: false,
                ..Default::default()
            },
        );
        let old = commit(
            &service,
            "seed",
            EntityRef::Temp("person".into()),
            1000,
            Value::String("retained-old-report".into()),
        );
        let entity = old.tempids["person"];
        let requested = commit(
            &service,
            "request",
            EntityRef::Temp("request".into()),
            DB_EXCISE as u32,
            Value::Ref(entity),
        );
        wait_index(&service, requested.basis_t);
        service.shutdown();
        let error = PostgresOperator::connect(&fixture.connection)
            .unwrap()
            .process_excision_requests_with_fault(name, fault)
            .unwrap_err();
        assert_eq!(error.code, "excision/injected-fault");
        let started = Instant::now();
        let service = start(
            &runtime,
            name,
            ExcisionConfig {
                log_batch_transactions: 1,
                ..Default::default()
            },
        );
        wait_excise(&runtime, name, requested.basis_t, &service);
        let later = commit(
            &service,
            "after",
            EntityRef::Temp("new".into()),
            1000,
            Value::String("post-excision".into()),
        );
        wait_index(&service, later.basis_t);
        let prefix = IndexPrefix::Eavt {
            entity,
            attribute: Some(1000),
            value: None,
        };
        assert_eq!(old.db_after.datoms_with_prefix(&prefix).unwrap().len(), 1);
        assert!(
            later
                .db_before
                .datoms_with_prefix(&prefix)
                .unwrap()
                .is_empty()
        );
        let retry = service
            .client()
            .transact(
                TransactionRequest::new(
                    "seed",
                    vec![TxOp::Add {
                        entity: EntityRef::Temp("person".into()),
                        attribute: 1000,
                        value: Value::String("retained-old-report".into()).into(),
                    }],
                ),
                Duration::from_secs(30),
            )
            .unwrap_err();
        assert_eq!(retry.code, "postgres/idempotency-predates-excision");
        let stats = service.background_indexing_stats();
        assert!(stats.last_failure.is_none(), "{stats:?}");
        assert!(stats.excision.complete, "{stats:?}");
        assert!(stats.excision.steps > 0, "{stats:?}");
        service.shutdown();
        drop((old, requested, later));
        eprintln!(
            "restricted restart {name}: full resume/write/index/check/drop {:?}; {:?}",
            started.elapsed(),
            stats.excision
        );
    }
}

#[test]
fn low_admission_pauses_excision_without_closing_writes_and_larger_restart_completes() {
    let Some(fixture) = fixture("automatic_admission") else {
        return;
    };
    create(&fixture.connection, "people");
    let roles = RuntimeRoles::new(&fixture);
    let runtime = roles.writer_url(&fixture);
    let service = start(
        &runtime,
        "people",
        ExcisionConfig {
            max_admitted_bytes: 1,
            ..Default::default()
        },
    );
    let seeded = commit(
        &service,
        "seed",
        EntityRef::Temp("person".into()),
        1000,
        Value::String("private".into()),
    );
    let requested = commit(
        &service,
        "request",
        EntityRef::Temp("request".into()),
        DB_EXCISE as u32,
        Value::Ref(seeded.tempids["person"]),
    );
    service.client().request_index().unwrap();
    let deadline = Instant::now() + Duration::from_secs(30);
    loop {
        let stats = service.background_indexing_stats();
        assert!(stats.last_failure.is_none(), "{stats:?}");
        if let Some(error) = stats.excision_failure {
            assert_eq!(error.code, "excision/admission-capacity");
            break;
        }
        assert!(
            Instant::now() < deadline,
            "maintenance admission was not observed"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
    assert!(
        !PostgresOperator::connect(&runtime)
            .unwrap()
            .sync_excise("people", requested.basis_t)
            .unwrap()
    );
    commit(
        &service,
        "still-writable",
        EntityRef::Temp("later".into()),
        1000,
        Value::String("kept".into()),
    );
    service.shutdown();
    let service = start(&runtime, "people", ExcisionConfig::default());
    wait_excise(&runtime, "people", requested.basis_t, &service);
    service.shutdown();
}

#[test]
fn whole_job_size_ladder_reports_admitted_and_rewritten_work() {
    let Some(fixture) = fixture("automatic_cost") else {
        return;
    };
    let roles = RuntimeRoles::new(&fixture);
    let runtime = roles.writer_url(&fixture);
    let mut last_account = 0;
    for count in [8_u64, 32, 128] {
        let name = format!("size_{count}");
        create(&fixture.connection, &name);
        let service = start(
            &runtime,
            &name,
            ExcisionConfig {
                enabled: false,
                ..Default::default()
            },
        );
        let mut removed = Vec::new();
        let mut retained = Vec::new();
        for i in 0..count {
            let report = commit(
                &service,
                &format!("seed-{i}"),
                EntityRef::Temp("person".into()),
                1000,
                Value::String(format!("value-{i}")),
            );
            if i % 2 == 0 {
                removed.push(report.tempids["person"]);
            } else {
                retained.push((report.tempids["person"], format!("value-{i}")));
            }
        }
        let request = service
            .client()
            .transact(
                TransactionRequest::new(
                    "requests",
                    removed
                        .iter()
                        .enumerate()
                        .map(|(i, &entity)| TxOp::Add {
                            entity: EntityRef::Temp(format!("request-{i}")),
                            attribute: DB_EXCISE as u32,
                            value: Value::Ref(entity).into(),
                        })
                        .collect(),
                ),
                Duration::from_secs(30),
            )
            .unwrap();
        wait_index(&service, request.basis_t);
        service.shutdown();
        let before = process_sql_stats();
        let started = Instant::now();
        let service = start(
            &runtime,
            &name,
            ExcisionConfig {
                log_batch_transactions: 8,
                ..Default::default()
            },
        );
        wait_excise(&runtime, &name, request.basis_t, &service);
        // Completion publication precedes the maintenance thread's final
        // diagnostic handoff by a small, intentional interval.
        let deadline = Instant::now() + Duration::from_secs(10);
        let progress = loop {
            let stats = service.background_indexing_stats();
            assert!(stats.last_failure.is_none(), "{stats:?}");
            if stats.excision.complete {
                break stats.excision;
            }
            assert!(Instant::now() < deadline, "{stats:?}");
            std::thread::sleep(Duration::from_millis(5));
        };
        let connection = Connection::connect(&runtime, &name, 8).unwrap();
        let current = connection.db();
        let prefix = |entity| IndexPrefix::Eavt {
            entity,
            attribute: Some(1000),
            value: None,
        };
        for &entity in &removed {
            assert!(
                current
                    .clone()
                    .history()
                    .datoms_with_prefix(&prefix(entity))
                    .unwrap()
                    .is_empty()
            );
        }
        for (entity, expected) in &retained {
            let datoms = current.datoms_with_prefix(&prefix(*entity)).unwrap();
            assert_eq!(datoms.len(), 1);
            assert_eq!(datoms[0].value, Value::String(expected.clone()));
        }
        assert_eq!(progress.rewritten_transactions, request.basis_t);
        assert!(progress.source_transactions >= 2 * request.basis_t);
        assert!(progress.peak_admitted_bytes > last_account);
        last_account = progress.peak_admitted_bytes;
        service.shutdown();
        drop((current, connection, request, removed, retained));
        let elapsed = started.elapsed();
        let after = process_sql_stats();
        eprintln!(
            "automatic size={count}: complete start/resume/check/shutdown/drop {elapsed:?}; progress={progress:?}; process observed calls={} sql_calls={} result_cell_bytes={}",
            after.calls - before.calls,
            after.sql_calls - before.sql_calls,
            after.result_cell_bytes - before.result_cell_bytes
        );
    }
}
