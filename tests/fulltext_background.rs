mod common;

use atomic_core::*;
use postgres::{Client, NoTls};
use std::time::{Duration, Instant};

fn wait_for(
    service: &TransactionService,
    predicate: impl Fn(&BackgroundIndexingStats) -> bool,
) -> BackgroundIndexingStats {
    let deadline = Instant::now() + Duration::from_secs(10);
    loop {
        let stats = service.background_indexing_stats();
        if predicate(&stats) {
            return stats;
        }
        assert!(
            Instant::now() < deadline,
            "background state did not converge: {stats:?}"
        );
        std::thread::sleep(Duration::from_millis(10));
    }
}

#[test]
fn startup_search_failure_is_observable_nonfatal_and_recovers_while_idle() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "fulltext_background");
    let connection = &fixture.connection;
    let config = PostgresConnectionConfig::plaintext(connection);
    PostgresMigrator::connect(connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1000,
                Keyword::new("search", "body"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    PostgresStore::connect(connection)
        .unwrap()
        .create_database("search-background", schema)
        .unwrap();
    let mut indexer = PostgresIndexer::connect(connection, "search-background").unwrap();
    let canonical = indexer.consolidate().unwrap();
    assert!(indexer.fulltext_build_error().is_none());

    let mut competing = Client::connect(connection, NoTls).unwrap();
    let source: Digest = competing.query_one("SELECT manifest_hash FROM atomic_tree_publications WHERE database_id=$1 ORDER BY publication_revision DESC LIMIT 1", &[&"search-background"]).unwrap().get::<_, Vec<u8>>(0).try_into().unwrap();
    assert!(
        FulltextStore::connect(&config)
            .unwrap()
            .discard_projection(source, 4096)
            .unwrap()
    );
    // Exercise the real bounded build/repair exclusion, not a test-only error
    // hook. This key deliberately mirrors the versioned storage protocol.
    let mut key_input = b"atomic/fulltext-projection-lock/v1".to_vec();
    key_input.extend_from_slice(&source);
    let key = i64::from_be_bytes(sha256(&key_input)[..8].try_into().unwrap());
    competing
        .query_one("SELECT pg_advisory_lock($1)", &[&key])
        .unwrap();

    let service = common::start_service(connection, "search-background");
    let failed = wait_for(&service, |stats| stats.fulltext.last_failure.is_some());
    let failure = failed.fulltext.last_failure.as_ref().unwrap();
    assert_eq!(failure.category, ErrorCategory::Busy);
    assert_eq!(failure.code, "fulltext/projection-busy");
    assert!(failed.fulltext.retry_in.is_some());
    assert_eq!(failed.published_basis_t, canonical.basis_t);
    assert_eq!(failed.jobs_failed, 0);
    assert!(failed.last_failure.is_none());
    assert!(service.client().is_available());

    // Search unavailability cannot reject an otherwise ordinary transaction.
    let report = service
        .client()
        .transact(
            TransactionRequest::new(
                "during-search-retry",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("document".into()),
                    attribute: 1000,
                    value: Value::String("Idle retry keeps canonical authority intact".into())
                        .into(),
                }],
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    assert!(report.basis_t > canonical.basis_t);
    assert_eq!(service.background_indexing_stats().jobs_failed, 0);

    let unlocked: bool = competing
        .query_one("SELECT pg_advisory_unlock($1)", &[&key])
        .unwrap()
        .get(0);
    assert!(unlocked);
    // No additional commit, explicit request, or polling read triggers repair:
    // only the existing worker's bounded idle timer does the work.
    let recovered = wait_for(&service, |stats| {
        stats.fulltext.last_failure.is_none() && stats.fulltext.idle_retries > 0
    });
    assert_eq!(recovered.fulltext.checked_basis_t, Some(canonical.basis_t));
    assert!(recovered.fulltext.failures > 0);
    assert!(recovered.fulltext.retry_in.is_none());
    assert!(!recovered.fulltext.retry_exhausted);
    assert_eq!(recovered.jobs_completed, 0);
    assert_eq!(recovered.jobs_failed, 0);
    assert!(service.client().is_available());
    assert!(
        FulltextStore::connect(&config)
            .unwrap()
            .open(source, 1)
            .unwrap()
            .is_some()
    );
    eprintln!(
        "fulltext_idle_retry basis={} attempts={} failures={} idle_retries={} canonical_jobs={}",
        canonical.basis_t,
        recovered.fulltext.attempts,
        recovered.fulltext.failures,
        recovered.fulltext.idle_retries,
        recovered.jobs_completed
    );
    service.shutdown();
}
