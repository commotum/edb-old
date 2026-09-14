use atomic_core::storage::PgBlockStore;
use atomic_core::*;
use std::time::{Duration, Instant};
mod common;

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP automatic excision: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PgBlockStore::install(&PostgresConnectionConfig::plaintext(&fixture.connection)).unwrap();
    Some(fixture)
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
        PgBlockStore::connect(&PostgresConnectionConfig::plaintext(&fixture.connection))
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
        if fixture.connection.starts_with("postgres://")
            || fixture.connection.starts_with("postgresql://")
        {
            format!(
                "{}{}user={}&password=isolated-auto",
                fixture.connection,
                if fixture.connection.contains('?') {
                    '&'
                } else {
                    '?'
                },
                self.writer
            )
        } else {
            format!(
                "{} user={} password=isolated-auto",
                fixture.connection, self.writer
            )
        }
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
    TransactionService::start_with_options(
        TransactionServiceConfig {
            connection: PostgresConnectionConfig::plaintext(url),
            database_id: name.into(),
            holder_id: format!("writer-{name}"),
            lease_duration: Duration::from_secs(5),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 8,
            capacity_limits: CapacityLimits::default(),
        },
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
    DatabaseCatalog::connect(url)
        .unwrap()
        .create_if_absent(name, schema)
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
    let database_id = DatabaseCatalog::connect(url)
        .unwrap()
        .resolve(name)
        .unwrap()
        .database_id;
    let mut operator = PostgresOperator::connect(url).unwrap();
    let deadline = Instant::now() + Duration::from_secs(30);
    loop {
        let stats = service.background_indexing_stats();
        assert!(stats.last_failure.is_none(), "{stats:?}");
        if operator.sync_excise(&database_id, basis).unwrap() {
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
        let database_id = DatabaseCatalog::connect(&fixture.connection)
            .unwrap()
            .resolve(name)
            .unwrap()
            .database_id;
        let error = PostgresOperator::connect(&fixture.connection)
            .unwrap()
            .process_excision_requests_with_fault(&database_id, fault)
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
        if fault == ExcisionFault::AfterActivation {
            // Current publication includes completion atomically. Restart
            // observes that durable result; it does not execute a second job.
            assert_eq!(stats.excision.steps, 0, "{stats:?}");
        } else {
            assert!(stats.excision.complete, "{stats:?}");
            assert!(stats.excision.steps > 0, "{stats:?}");
        }
        service.shutdown();
        drop((old, requested, later));
        eprintln!(
            "restricted restart {name}: full resume/write/index/check/drop {:?}; {:?}",
            started.elapsed(),
            stats.excision
        );
    }
}
