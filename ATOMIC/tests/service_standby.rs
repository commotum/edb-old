//! Persistent contenders retain deployment settings and have an interruptible,
//! non-consuming observation API. PostgreSQL fixtures use isolated catalogs.
mod common;

use atomic_core::*;
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::{Duration, Instant};

const VALUE: u32 = 1000;
const WAIT: Duration = Duration::from_secs(20);

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP service standby: ATOMIC_POSTGRES_URL is unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    common::install(&fixture.connection).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    DatabaseCatalog::connect(&fixture.connection)
        .unwrap()
        .create_if_absent("items", schema)
        .unwrap();
    Some(fixture)
}

fn config(connection: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: PostgresConnectionConfig::plaintext(connection),
        database_id: "items".into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(30),
        renew_interval: Duration::from_millis(50),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    }
}

fn until(mut condition: impl FnMut() -> bool) {
    let deadline = Instant::now() + WAIT;
    while !condition() {
        assert!(Instant::now() < deadline, "condition did not become true");
        std::thread::sleep(Duration::from_millis(5));
    }
}

fn take(standby: &mut TransactionStandby) -> TransactionService {
    let mut active = None;
    until(|| {
        active = standby.try_active().unwrap();
        active.is_some()
    });
    active.unwrap()
}

#[test]
fn takeover_preserves_indexing_native_registry_partition_defaults_and_captured_identity() {
    let Some(fixture) = fixture("standby_options") else {
        return;
    };
    let first = TransactionService::start(config(&fixture.connection, "first")).unwrap();
    let installed = first
        .client()
        .transact(
            TransactionRequest::from_edn(
                "partition",
                r#"[{:db/id "part" :db/ident :part/orders :db.install/_partition :db.part/db}]"#,
            )
            .unwrap(),
            WAIT,
        )
        .unwrap();
    common::consolidate(&fixture.connection, "items").unwrap();
    let partition = eid_to_eidx(
        installed
            .db_after
            .entid(&Keyword::new("part", "orders"))
            .unwrap(),
    )
    .unwrap() as u32;
    let before = installed.db_after.clone();
    let identity = first.identity();
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&calls);
    let mut registry = NativeRegistry::builder();
    registry
        .transaction(
            Symbol::new("standby.v1", "allocate"),
            move |_, _, control| {
                control.check(1)?;
                observed.fetch_add(1, Ordering::SeqCst);
                Ok(vec![TxForm::Op(TxOp::Add {
                    entity: EntityRef::Temp("created".into()),
                    attribute: VALUE,
                    value: Value::Long(42).into(),
                })])
            },
        )
        .unwrap();
    let options = ServiceOptions {
        indexing: BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1,
            memory_index_max_bytes: 1 << 20,
        },
        execution: TransactionExecutionOptions {
            defaults: TransactionDefaults::default()
                .with_default_partition(Keyword::new("part", "orders")),
            native: registry.build(),
        },
        ..Default::default()
    };
    let connection = PostgresConnectionConfig::plaintext(&fixture.connection)
        .with_io_policy(PostgresIoPolicy {
            statement_timeout: Some(Duration::from_secs(10)),
            ..Default::default()
        })
        .unwrap();
    // The connection policy survives every failed lease attempt and activation.
    let mut standby_config = config(&fixture.connection, "second");
    standby_config.connection = connection;
    let mut standby =
        TransactionStandby::start_with_options(standby_config, options, Duration::from_millis(10))
            .unwrap();
    until(|| standby.status() == StandbyStatus::Waiting);
    let polling = OperationContext::new(OperationKind::Application);
    let scope = polling.enter();
    let started = Instant::now();
    for _ in 0..10_000 {
        assert!(matches!(
            standby.status(),
            StandbyStatus::Waiting | StandbyStatus::Activating
        ));
        assert!(standby.try_active().unwrap().is_none());
    }
    let elapsed = started.elapsed();
    drop(scope);
    assert_eq!(
        polling.snapshot().calls,
        0,
        "polling does not call the driver"
    );
    eprintln!("10,000 complete status/try-active polls {elapsed:?}; 0 attributed driver calls");
    let mut catalog = DatabaseCatalog::connect(&fixture.connection).unwrap();
    catalog.rename("items", "renamed").unwrap();
    let replacement = catalog.create_if_absent("items", Schema::new()).unwrap();
    assert_ne!(replacement.database.database_id, identity.database_id());
    let started = Instant::now();
    first.shutdown();
    let active = take(&mut standby);
    assert_eq!(standby.status(), StandbyStatus::Transferred);
    assert_eq!(active.identity(), identity);
    assert_eq!(
        standby.try_active().err().unwrap().code,
        "service/standby-consumed"
    );
    standby.shutdown();
    assert!(
        active.client().is_available(),
        "transferred service is independently owned"
    );
    let request = TransactionRequest::new("native-after-takeover", vec![]).calling(ProgramCall {
        function: CallableRef::Local(Symbol::new("standby.v1", "allocate")),
        arguments: vec![],
    });
    let report = active.client().transact(request.clone(), WAIT).unwrap();
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    assert_eq!(eid_to_part(report.tempids["created"]).unwrap(), partition);
    assert_eq!(
        report
            .db_after
            .values(report.tempids["created"], VALUE)
            .unwrap(),
        [Value::Long(42)]
    );
    assert!(
        before
            .values(report.tempids["created"], VALUE)
            .unwrap()
            .is_empty()
    );
    until(|| active.background_indexing_stats().published_basis_t >= report.basis_t);
    assert!(
        active.background_indexing_stats().jobs_completed > 0,
        "one-byte automatic scheduling survived takeover"
    );
    let retry = active.client().transact(request, WAIT).unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.tempids, report.tempids);
    assert_eq!(
        calls.load(Ordering::SeqCst),
        1,
        "receipt lookup precedes native execution"
    );
    active.shutdown();
    eprintln!(
        "complete takeover/native commit/index/retry/shutdown {:?}",
        started.elapsed()
    );
}

#[test]
fn stopping_a_waiting_contender_wakes_a_long_poll_and_preserves_the_active_writer() {
    let Some(fixture) = fixture("standby_cancel_wait") else {
        return;
    };
    let active = TransactionService::start(config(&fixture.connection, "active")).unwrap();
    let mut standby = TransactionStandby::start(
        config(&fixture.connection, "waiting"),
        Duration::from_secs(30),
    )
    .unwrap();
    until(|| standby.status() == StandbyStatus::Waiting);
    let started = Instant::now();
    standby.request_stop();
    assert_eq!(
        standby.try_active().err().unwrap().code,
        "service/standby-stopped"
    );
    standby.shutdown();
    assert!(
        started.elapsed() < Duration::from_secs(2),
        "shutdown must not wait for the 30-second poll"
    );
    assert!(active.client().is_available());
    active.shutdown();
}

#[test]
fn stopping_an_unclaimed_ready_activation_releases_its_lease_and_workers() {
    let Some(fixture) = fixture("standby_cancel_ready") else {
        return;
    };
    let mut standby = TransactionStandby::start(
        config(&fixture.connection, "unclaimed"),
        Duration::from_millis(10),
    )
    .unwrap();
    until(|| standby.status() == StandbyStatus::Ready);
    standby.request_stop();
    assert_eq!(
        standby.try_active().err().unwrap().code,
        "service/standby-stopped"
    );
    standby.shutdown();
    // The old 30-second lease would still be held if cleanup merely detached
    // the contender or forgot the service buffered in its result channel.
    let replacement =
        TransactionService::start(config(&fixture.connection, "replacement")).unwrap();
    assert!(replacement.client().is_available());
    replacement.shutdown();
}

#[test]
fn cancellation_during_blocked_activation_cleans_up_after_the_driver_returns() {
    let Some(fixture) = fixture("standby_cancel_start") else {
        return;
    };
    let mut observer = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    observer
        .batch_execute("SET statement_timeout='2s'")
        .unwrap();
    let mut blocker = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let database = DatabaseCatalog::connect(&fixture.connection)
        .unwrap()
        .resolve("items")
        .unwrap();
    let lease_key = format!("writers/{}", database.database_id);
    // Match the generic reference guard used even when the lease is absent.
    // Catalog resolution reads immutable objects synchronously before start()
    // returns; a table lock would block the caller before it can request stop.
    let digest = sha256(format!("atomic-storage\0{}\0{lease_key}", fixture.schema).as_bytes());
    let guard = i64::from_be_bytes(digest[..8].try_into().unwrap());
    let high = ((guard as u64) >> 32) as i64;
    let low = ((guard as u64) & u32::MAX as u64) as i64;
    let connection = PostgresConnectionConfig::plaintext(&fixture.connection)
        .with_io_policy(PostgresIoPolicy {
            statement_timeout: Some(Duration::from_secs(10)),
            lock_timeout: Some(Duration::from_secs(10)),
            ..Default::default()
        })
        .unwrap();
    // On assertion unwind, drop the blocking transaction before joining the
    // contender; its bounded driver call can then finish and clean up normally.
    let mut standby;
    let mut blocked = blocker.transaction().unwrap();
    let blocker_pid: i32 = blocked
        .query_one("SELECT pg_backend_pid()", &[])
        .unwrap()
        .get(0);
    blocked
        .query_one("SELECT pg_catalog.pg_advisory_xact_lock($1)", &[&guard])
        .unwrap();
    standby = TransactionStandby::start(
        TransactionServiceConfig {
            connection,
            ..config(&fixture.connection, "starting")
        },
        Duration::from_millis(10),
    )
    .unwrap();
    // The real activation driver is blocked on its own fixture's writer guard.
    // Cancellation cannot interrupt a synchronous SQL call; it must discard
    // and clean up any completed activation once the driver returns.
    until(|| {
        observer.query_one(
            "SELECT EXISTS(SELECT 1 FROM pg_catalog.pg_locks l WHERE l.locktype='advisory' AND NOT l.granted AND l.classid::bigint=$2 AND l.objid::bigint=$3 AND l.objsubid=1 AND $1=ANY(pg_catalog.pg_blocking_pids(l.pid)))",
            &[&blocker_pid, &high, &low],
        ).unwrap().get::<_,bool>(0)
    });
    assert_eq!(standby.status(), StandbyStatus::Activating);
    standby.request_stop();
    assert_eq!(
        standby.try_active().err().unwrap().code,
        "service/standby-stopped"
    );
    blocked.rollback().unwrap();
    standby.shutdown();
    let replacement =
        TransactionService::start(config(&fixture.connection, "replacement")).unwrap();
    replacement.shutdown();
}

#[test]
fn invalid_standby_settings_fail_before_connecting() {
    let mut invalid = config("host=/no-such-standby-socket", "holder");
    invalid.queue_capacity = 0;
    assert_eq!(
        TransactionStandby::start(invalid, Duration::from_secs(1))
            .err()
            .unwrap()
            .code,
        "service/invalid-config"
    );
    assert_eq!(
        TransactionStandby::start(
            config("host=/no-such-standby-socket", "holder"),
            Duration::ZERO
        )
        .err()
        .unwrap()
        .code,
        "service/standby-poll"
    );
    let invalid = ServiceOptions {
        indexing: BackgroundIndexingConfig {
            memory_index_threshold_bytes: 0,
            memory_index_max_bytes: 1,
        },
        ..Default::default()
    };
    assert_eq!(
        TransactionStandby::start_with_options(
            config("host=/no-such-standby-socket", "holder"),
            invalid,
            Duration::from_secs(1)
        )
        .err()
        .unwrap()
        .code,
        "service/invalid-indexing-config"
    );
}
