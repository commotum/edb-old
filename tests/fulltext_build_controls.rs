//! The public service/operator policy reaches real current-engine search builds.
mod common;
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::time::{Duration, Instant};

const NAME: &str = "build-controls";
const TEXT: u32 = 1000;
const WAIT: Duration = Duration::from_secs(30);

fn service_config() -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: String::new(),
        database_id: NAME.into(),
        holder_id: "fulltext-controls".into(),
        lease_duration: Duration::from_secs(10),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    }
}
fn tiny() -> FulltextBuildLimits {
    FulltextBuildLimits {
        max_records: 1,
        ..Default::default()
    }
}
fn fixture() -> Option<(
    common::PostgresFixture,
    PostgresConnectionConfig,
    BlockDatabase,
)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP fulltext build controls: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, "fulltext_build_controls");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                TEXT,
                Keyword::new("doc", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let database = BlockDatabase::create(&config, NAME, schema).unwrap();
    Some((fixture, config, database))
}
fn options(limits: FulltextBuildLimits, automatic: bool) -> ServiceOptions {
    ServiceOptions {
        fulltext_build_limits: limits,
        indexing: BackgroundIndexingConfig {
            memory_index_threshold_bytes: if automatic { 1 } else { 1 << 28 },
            memory_index_max_bytes: 1 << 29,
        },
        excision: ExcisionConfig {
            enabled: false,
            ..Default::default()
        },
        ..Default::default()
    }
}
fn seed(service: &TransactionService) -> ServiceTransactionReport {
    service
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                (0..3)
                    .map(|i| TxOp::Add {
                        entity: EntityRef::Temp(format!("doc{i}")),
                        attribute: TEXT,
                        value: Value::String(format!("needle document value{i}")).into(),
                    })
                    .collect(),
            ),
            WAIT,
        )
        .unwrap()
}
fn wait(mut ready: impl FnMut() -> bool) {
    let start = Instant::now();
    while !ready() {
        assert!(
            start.elapsed() < WAIT,
            "fulltext control result not observed"
        );
        std::thread::sleep(Duration::from_millis(5));
    }
}

#[test]
fn invalid_search_policy_rejects_before_direct_or_standby_connection() {
    let invalid = FulltextBuildLimits {
        max_records: 0,
        ..Default::default()
    };
    let config = PostgresConnectionConfig::plaintext("not a PostgreSQL configuration");
    assert_eq!(
        TransactionService::start_configured_with_options(
            service_config(),
            config.clone(),
            options(invalid.clone(), true)
        )
        .err()
        .unwrap()
        .code,
        "fulltext/build-limits"
    );
    assert_eq!(
        TransactionStandby::start_configured_with_options(
            service_config(),
            config,
            options(invalid, true),
            Duration::from_millis(10)
        )
        .err()
        .unwrap()
        .code,
        "fulltext/build-limits"
    );
}

#[test]
fn background_policy_failure_is_visible_and_restart_with_larger_policy_recovers() {
    let Some((fixture, config, _)) = fixture() else {
        return;
    };
    let service = TransactionService::start_configured_with_options(
        service_config(),
        config.clone(),
        options(tiny(), true),
    )
    .unwrap();
    let report = seed(&service);
    wait(|| {
        service
            .background_indexing_stats()
            .fulltext
            .last_failure
            .is_some()
    });
    let stats = service.background_indexing_stats();
    assert_eq!(
        stats.fulltext.last_failure.unwrap().code,
        "fulltext/build-limit"
    );
    assert!(
        stats
            .fulltext
            .checked_basis_t
            .is_none_or(|basis| basis < report.basis_t)
    );
    service.shutdown();
    let service = TransactionService::start_configured_with_options(
        service_config(),
        config,
        options(Default::default(), true),
    )
    .unwrap();
    wait(|| {
        service
            .background_indexing_stats()
            .fulltext
            .checked_basis_t
            .is_some_and(|t| t >= report.basis_t)
    });
    let peer = Peer::connect(&fixture.connection, NAME, 8).unwrap();
    assert_eq!(
        peer.database_value()
            .fulltext(TEXT, "needle", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        3
    );
    assert!(
        seed(&service).replayed,
        "changed build policy must not change receipts"
    );
    service.shutdown();
}

#[test]
fn operator_limits_cover_consolidation_rebuild_excision_and_explicit_recovery() {
    let Some((fixture, config, database)) = fixture() else {
        return;
    };
    let service = TransactionService::start_configured_with_options(
        service_config(),
        config.clone(),
        options(Default::default(), false),
    )
    .unwrap();
    let report = seed(&service);
    let request = service
        .client()
        .transact(
            TransactionRequest::new(
                "excise",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE as u32,
                    value: Value::Ref(report.tempids["doc0"]).into(),
                }],
            ),
            WAIT,
        )
        .unwrap();
    service.shutdown();
    let route = DatabaseCatalog::connect(&fixture.connection)
        .unwrap()
        .resolve(NAME)
        .unwrap()
        .database_id;
    let mut restricted = PostgresOperator::connect_configured(&config)
        .unwrap()
        .with_fulltext_build_limits(tiny())
        .unwrap();
    let mut normal = PostgresOperator::connect_configured(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let before = store.read_ref(&database.reference_key()).unwrap();
    assert_eq!(
        restricted.consolidate_database(&route).unwrap_err().code,
        "fulltext/build-limit"
    );
    assert_eq!(store.read_ref(&database.reference_key()).unwrap(), before);
    normal.consolidate_database(&route).unwrap();
    let before = store.read_ref(&database.reference_key()).unwrap();
    assert_eq!(
        restricted.rebuild_fulltext(&route, None).unwrap_err().code,
        "fulltext/build-limit"
    );
    assert_eq!(store.read_ref(&database.reference_key()).unwrap(), before);
    normal.rebuild_fulltext(&route, None).unwrap();
    assert_eq!(
        restricted
            .process_excision_requests(&route)
            .unwrap_err()
            .code,
        "fulltext/build-limit"
    );
    assert!(!normal.sync_excise(&route, request.basis_t).unwrap());
    let excised = normal.process_excision_requests(&route).unwrap();
    assert!(excised.resumed);
    assert!(normal.sync_excise(&route, request.basis_t).unwrap());
    let peer = Peer::connect(&fixture.connection, NAME, 8).unwrap();
    assert_eq!(
        peer.database_value()
            .fulltext(TEXT, "needle", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        2
    );
    drop(peer);

    // Only this disposable fixture's current derived descriptor is damaged.
    // Failed recovery must leave its publication untouched, not erase history.
    let before = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let root_id = before.value.as_deref().unwrap().try_into().unwrap();
    let root = atomic_core::storage::root::DatabaseRoot::decode(
        &root_id,
        &store.get(root_id).unwrap().unwrap(),
    )
    .unwrap();
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    sql.execute(
        "DELETE FROM atomic_objects WHERE id=$1",
        &[&root.indexes.unwrap().as_slice()],
    )
    .unwrap();
    assert_eq!(
        restricted.consolidate_database(&route).unwrap_err().code,
        "fulltext/build-limit"
    );
    assert_eq!(
        store.read_ref(&database.reference_key()).unwrap(),
        Some(before)
    );
    assert!(normal.consolidate_database(&route).unwrap().recovered);
    let peer = Peer::connect(&fixture.connection, NAME, 8).unwrap();
    assert_eq!(
        peer.database_value()
            .fulltext(TEXT, "needle", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        2
    );
}
