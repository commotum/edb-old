use atomic_core::*;
use std::sync::{Arc, Barrier};
mod common;

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL catalog: ATOMIC_POSTGRES_URL unset");
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
fn concurrent_create_is_idempotent_and_existing_catalog_lookup_never_rebuilds_schema() {
    let Some(fixture) = fixture("catalog_create") else {
        return;
    };
    let barrier = Arc::new(Barrier::new(6));
    let threads = (0..6)
        .map(|_| {
            let barrier = barrier.clone();
            let connection = fixture.connection.clone();
            std::thread::spawn(move || {
                let mut catalog = DatabaseCatalog::connect(&connection).unwrap();
                barrier.wait();
                catalog.create_if_absent("shared", Schema::new()).unwrap()
            })
        })
        .collect::<Vec<_>>();
    let results = threads
        .into_iter()
        .map(|thread| thread.join().unwrap())
        .collect::<Vec<_>>();
    assert_eq!(results.iter().filter(|result| result.created).count(), 1);
    assert!(
        results
            .iter()
            .all(|result| result.database == results[0].database)
    );
    assert_eq!(results[0].database.database_id, "shared");
    let mut catalog = DatabaseCatalog::connect(&fixture.connection).unwrap();
    // This schema cannot initialize a database: it skips the fresh schema ID.
    // Existing-create ignores it, proving the public no-op has no eager rebuild.
    let mut invalid_initial = Schema::new();
    invalid_initial
        .install(Attribute::new(
            2000,
            Keyword::new("test", "gap"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    assert!(Database::new(invalid_initial.clone()).is_err());
    let started = std::time::Instant::now();
    let existing = catalog.create_if_absent("shared", invalid_initial).unwrap();
    assert!(!existing.created);
    assert_eq!(existing.database, results[0].database);
    drop(existing);
    eprintln!(
        "complete existing create/identity-check/drop {:?}; six concurrent creators issued one identity",
        started.elapsed()
    );
    assert_eq!(
        catalog.list(None, 1).unwrap(),
        vec![results[0].database.clone()]
    );
    assert!(catalog.list(Some("shared"), 1).unwrap().is_empty());
    assert_eq!(
        catalog.list(None, 0).unwrap_err().code,
        "catalog/page-limit"
    );
}

#[test]
fn checked_rename_retire_and_name_reuse_preserve_canonical_bytes_and_issued_identity() {
    let Some(fixture) = fixture("catalog_identity") else {
        return;
    };
    let mut catalog = DatabaseCatalog::connect(&fixture.connection).unwrap();
    let old = catalog
        .create_if_absent("original", Schema::new())
        .unwrap()
        .database;
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let original: Vec<u8> = sql
        .query_one(
            "SELECT genesis FROM atomic_databases WHERE database_id=$1",
            &[&old.database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        catalog
            .rename_checked("original", "renamed", "wrong-lineage")
            .unwrap_err()
            .code,
        "catalog/identity-mismatch"
    );
    let renamed = catalog
        .rename_checked("original", "renamed", &old.lineage_id)
        .unwrap();
    assert_eq!(renamed.database_id, old.database_id);
    assert_eq!(renamed.lineage_id, old.lineage_id);
    assert_eq!(
        catalog.resolve("original").unwrap_err().code,
        "catalog/name-not-found"
    );
    assert_eq!(
        catalog.require_active_id(&old.database_id).unwrap(),
        renamed
    );
    let replacement = catalog
        .create_if_absent("original", Schema::new())
        .unwrap()
        .database;
    assert_ne!(replacement.database_id, old.database_id);
    assert_ne!(replacement.lineage_id, old.lineage_id);
    assert_eq!(
        catalog
            .retire_checked("original", &old.lineage_id)
            .unwrap_err()
            .code,
        "catalog/identity-mismatch"
    );
    assert_eq!(catalog.resolve("original").unwrap(), replacement);
    let retired = catalog
        .retire_checked("renamed", &old.lineage_id)
        .unwrap()
        .unwrap();
    assert!(retired.retired);
    assert!(retired.name.is_none());
    assert_eq!(
        catalog
            .require_active_id(&old.database_id)
            .unwrap_err()
            .code,
        "catalog/database-retired"
    );
    assert!(
        catalog
            .retire_checked("renamed", &old.lineage_id)
            .unwrap()
            .is_none()
    );
    assert_eq!(catalog.list_retired(None, 10).unwrap(), vec![retired]);
    let after: Vec<u8> = sql
        .query_one(
            "SELECT genesis FROM atomic_databases WHERE database_id=$1",
            &[&old.database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        after, original,
        "rename and retirement never rewrite canonical bytes"
    );
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_database_identities", &[])
            .unwrap()
            .get::<_, i64>(0),
        2
    );
}
