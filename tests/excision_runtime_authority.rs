//! The runtime boundary grants a trusted Rust writer only the checked excision
//! lifecycle, not owner restore/activation/GC authority.
mod common;
use atomic_core::*;
use postgres::{Client, NoTls};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

struct Roles {
    admin: Client,
    writer: String,
    peer: String,
}
impl Roles {
    fn new(connection: &str) -> Self {
        let suffix = format!(
            "{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let writer = format!("exc_writer_{suffix}");
        let peer = format!("exc_peer_{suffix}");
        let mut admin = Client::connect(connection, NoTls).unwrap();
        admin.batch_execute(&format!("CREATE ROLE {writer} LOGIN PASSWORD 'isolated-excision'; CREATE ROLE {peer} LOGIN PASSWORD 'isolated-excision'" )).unwrap();
        Self {
            admin,
            writer,
            peer,
        }
    }
}
impl Drop for Roles {
    fn drop(&mut self) {
        let _ = self.admin.batch_execute(&format!(
            "DROP OWNED BY {}; DROP OWNED BY {}; DROP ROLE {}; DROP ROLE {}",
            self.writer, self.peer, self.writer, self.peer
        ));
    }
}

fn service(connection: &str) -> TransactionService {
    TransactionService::start_configured_with_options(
        TransactionServiceConfig {
            connection: String::new(),
            database_id: "items".into(),
            holder_id: "runtime-owner".into(),
            lease_duration: Duration::from_secs(30),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 8,
            capacity_limits: CapacityLimits::default(),
        },
        PostgresConnectionConfig::plaintext(connection),
        ServiceOptions {
            excision: ExcisionConfig {
                enabled: false,
                ..Default::default()
            },
            ..Default::default()
        },
    )
    .unwrap()
}

fn assert_worker(client: &mut Client, epoch: i64) -> Result<postgres::Row, postgres::Error> {
    client.query_one(
        "SELECT atomic_assert_excision_worker('items','runtime-owner',$1)",
        &[&epoch],
    )
}
fn stage(
    client: &mut Client,
    generation: i64,
    epoch: i64,
) -> Result<postgres::Row, postgres::Error> {
    client.query_one("SELECT * FROM atomic_runtime_excision_step('items',$1,'runtime-owner',$2,'stage',32,0,NULL,NULL,NULL)", &[&generation,&epoch])
}

#[test]
fn runtime_wrappers_check_exact_lease_and_plan_without_exposing_owner_capabilities() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP excision runtime authority: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "excision_authority");
    let mut migrator = PostgresMigrator::connect(&fixture.connection).unwrap();
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
    DatabaseCatalog::connect(&fixture.connection)
        .unwrap()
        .create_if_absent("items", schema)
        .unwrap();
    let writer = service(&fixture.connection);
    let entity = make_eid(USER_PARTITION, 42).unwrap();
    writer
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                vec![TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1000,
                    value: Value::Long(7).into(),
                }],
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    writer
        .client()
        .transact(
            TransactionRequest::new(
                "request",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("excision".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(entity)),
                }],
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    let fault = PostgresOperator::connect(&fixture.connection)
        .unwrap()
        .process_excision_requests_with_fault("items", ExcisionFault::AfterCapture)
        .unwrap_err();
    assert_eq!(fault.code, "excision/injected-fault");

    let mut roles = Roles::new(&fixture.connection);
    migrator
        .grant_runtime_privileges(&roles.writer, &roles.peer)
        .unwrap();
    let epoch: i64 = roles
        .admin
        .query_one(
            "SELECT epoch FROM atomic_transactor_leases WHERE lease_scope='items'",
            &[],
        )
        .unwrap()
        .get(0);
    let candidate: i64 = roles.admin.query_one("SELECT generation FROM atomic_log_generations WHERE database_id='items' AND build_kind=1",&[]).unwrap().get(0);
    let base: i64 = roles
        .admin
        .query_one(
            "SELECT log_generation FROM atomic_heads WHERE database_id='items'",
            &[],
        )
        .unwrap()
        .get(0);
    let mut runtime_config: postgres::Config = fixture.connection.parse().unwrap();
    runtime_config
        .user(&roles.writer)
        .password("isolated-excision");
    let mut runtime = runtime_config.connect(NoTls).unwrap();
    assert_eq!(
        runtime
            .query_one("SELECT current_user", &[])
            .unwrap()
            .get::<_, String>(0),
        roles.writer
    );
    assert_worker(&mut runtime, epoch).unwrap();
    assert_eq!(
        assert_worker(&mut runtime, epoch + 1)
            .unwrap_err()
            .code()
            .unwrap()
            .code(),
        "55000"
    );
    assert_eq!(
        stage(&mut runtime, base, epoch)
            .unwrap_err()
            .code()
            .unwrap()
            .code(),
        "55000",
        "bootstrap generation is not an excision candidate"
    );

    for function in [
        "atomic_activate_log_generation(text,bigint,bigint,bytea,bytea,bytea)",
        "atomic_stage_excision_completions(text,bigint,bigint)",
        "atomic_complete_excision_generation(text,bigint,bytea)",
        "atomic_cleanup_log_generation_build(text,bigint,bigint)",
        "atomic_prepare_database_reclamation(text,text,bigint,boolean)",
    ] {
        assert!(
            !runtime
                .query_one(
                    "SELECT has_function_privilege(current_user,$1,'EXECUTE')",
                    &[&function]
                )
                .unwrap()
                .get::<_, bool>(0),
            "runtime must not execute {function}"
        );
    }
    assert!(!roles.admin.query_one("SELECT has_function_privilege($1,'atomic_assert_excision_worker(text,text,bigint)','EXECUTE')", &[&roles.peer]).unwrap().get::<_,bool>(0));
    assert_eq!(
        runtime
            .execute(
                "DELETE FROM atomic_log_generation_builds WHERE database_id='items'",
                &[]
            )
            .unwrap_err()
            .code()
            .unwrap()
            .code(),
        "42501"
    );

    // A real restricted LOGIN owns these temporary shadows. The definer must
    // still use its exact installation schema, including its internal helper.
    runtime.batch_execute("CREATE TEMP TABLE atomic_transactor_leases (lease_scope text,holder_id text,epoch bigint,expires_at timestamptz); CREATE TEMP TABLE atomic_database_identities (database_id text,retired_at timestamptz); INSERT INTO atomic_database_identities VALUES('items',NULL);").unwrap();
    runtime.execute("INSERT INTO atomic_transactor_leases VALUES('items','runtime-owner',$1,clock_timestamp()+interval '1 hour')",&[&(epoch+1)]).unwrap();
    assert_eq!(
        assert_worker(&mut runtime, epoch + 1)
            .unwrap_err()
            .code()
            .unwrap()
            .code(),
        "55000",
        "fake temporary lease must not authorize a stale epoch"
    );
    assert_worker(&mut runtime, epoch).unwrap();
    let mut sealed = false;
    for _ in 0..8 {
        let stage_result = stage(&mut runtime, candidate, epoch).unwrap();
        assert!(stage_result.get::<_, i64>(0) <= 32);
        if stage_result.get::<_, bool>(1) {
            sealed = true;
            break;
        }
    }
    assert!(
        sealed,
        "genuine frozen request set seals in bounded runtime steps"
    );
    assert_eq!(
        runtime
            .query_one("SELECT current_user", &[])
            .unwrap()
            .get::<_, String>(0),
        roles.writer
    );
    let rows: i64 = roles.admin.query_one("SELECT count(*) FROM atomic_log_generation_completion_stages WHERE database_id='items' AND generation=$1 AND phase=2",&[&candidate]).unwrap().get(0);
    assert_eq!(rows, 1);

    // Expiry/holder changes invalidate the exact same call, even while forged
    // temp metadata remains. The privileged helper never relies on session GUCs.
    writer.shutdown();
    assert_eq!(
        assert_worker(&mut runtime, epoch)
            .unwrap_err()
            .code()
            .unwrap()
            .code(),
        "55000"
    );
    let successor = service(&fixture.connection);
    assert_eq!(
        assert_worker(&mut runtime, epoch)
            .unwrap_err()
            .code()
            .unwrap()
            .code(),
        "55000"
    );
    successor.shutdown();
    drop(runtime);
}
