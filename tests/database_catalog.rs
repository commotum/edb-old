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
    // A publisher can already hold an old snapshot when retirement commits.
    // The guard must lock the current identity, not trust a stale active bit.
    let mut late_publisher =
        postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let mut stale = late_publisher
        .build_transaction()
        .isolation_level(postgres::IsolationLevel::RepeatableRead)
        .start()
        .unwrap();
    assert!(
        stale
            .query_one(
                "SELECT retired_at IS NULL FROM atomic_database_identities WHERE database_id=$1",
                &[&old.database_id],
            )
            .unwrap()
            .get::<_, bool>(0)
    );
    let retired = catalog
        .retire_checked("renamed", &old.lineage_id)
        .unwrap()
        .unwrap();
    let rejected = stale
        .execute(
            "UPDATE atomic_heads SET basis_t=basis_t WHERE database_id=$1",
            &[&old.database_id],
        )
        .unwrap_err();
    assert_eq!(rejected.code().unwrap().code(), "40001");
    stale.rollback().unwrap();
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

#[test]
fn headless_restore_reservations_remain_visible_and_retirement_fences_publication() {
    let Some(source) = fixture("catalog_restore_source") else {
        return;
    };
    let target = fixture("catalog_restore_target").unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("test", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    DatabaseCatalog::connect(&source.connection)
        .unwrap()
        .create_if_absent("source", schema)
        .unwrap();
    let directory = tempfile::tempdir().unwrap();
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    }
    let point = PortableBackup::connect(&source.connection)
        .unwrap()
        .backup_database("source", directory.path())
        .unwrap();
    let mut restore = PortableBackup::connect(&target.connection).unwrap();
    let error = restore
        .restore_backup_with_fault(
            directory.path(),
            point.basis_t,
            "pending",
            RestoreFault::BeforeCommit,
        )
        .unwrap_err();
    assert_eq!(error.code, "backup/restore-before-activation");
    let mut catalog = DatabaseCatalog::connect(&target.connection).unwrap();
    let pending = catalog.resolve("pending").unwrap();
    assert_eq!(pending.lineage_id, point.lineage_id);
    assert_eq!(catalog.list(None, 10).unwrap(), vec![pending.clone()]);
    let existing = catalog.create_if_absent("pending", Schema::new()).unwrap();
    assert!(!existing.created);
    assert_eq!(existing.database, pending);
    assert!(Peer::connect(&target.connection, "pending", 0).is_err());
    let mut sql = postgres::Client::connect(&target.connection, postgres::NoTls).unwrap();
    let head_count = |sql: &mut postgres::Client| {
        sql.query_one(
            "SELECT count(*) FROM atomic_heads WHERE database_id=$1",
            &[&pending.database_id],
        )
        .unwrap()
        .get::<_, i64>(0)
    };
    assert_eq!(head_count(&mut sql), 0);

    // Resume a real staged restore, then retire after it has captured its
    // stable target but before its initial head INSERT. No synthetic head or
    // bypassed storage guard supplies the fixture.
    let mut probed = false;
    let error = restore
        .restore_backup_with_activation_probe(directory.path(), point.basis_t, "pending", || {
            probed = true;
            assert!(
                catalog
                    .retire_checked("pending", &pending.lineage_id)
                    .unwrap()
                    .unwrap()
                    .retired
            );
        })
        .unwrap_err();
    assert!(probed);
    assert_eq!(error.code, "backup/restore-initial-activate");
    assert_eq!(
        error.details.get("postgres_sqlstate").map(String::as_str),
        Some("55000")
    );
    assert_eq!(head_count(&mut sql), 0);
    let replacement = catalog.create_if_absent("pending", Schema::new()).unwrap();
    assert!(replacement.created);
    assert_ne!(replacement.database.database_id, pending.database_id);
    assert_ne!(replacement.database.lineage_id, pending.lineage_id);
    assert_eq!(head_count(&mut sql), 0);
}

#[test]
fn independent_publication_guard_does_not_take_the_writers_head_lock() {
    let Some(fixture) = fixture("catalog_publication_lock_order") else {
        return;
    };
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("test", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let entry = DatabaseCatalog::connect(&fixture.connection)
        .unwrap()
        .create_if_absent("database", schema)
        .unwrap()
        .database;
    PostgresIndexer::connect(&fixture.connection, "database")
        .unwrap()
        .consolidate()
        .unwrap();
    let mut writer = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let mut writer = writer.transaction().unwrap();
    writer
        .query_one(
            "SELECT basis_t FROM atomic_heads WHERE database_id=$1 FOR UPDATE",
            &[&entry.database_id],
        )
        .unwrap();
    let mut publisher = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let mut publisher = publisher.transaction().unwrap();
    // PostgreSQL executes same-event BEFORE triggers in name order. Prove the
    // active-identity guard runs first, before index-basis/delta/revision
    // validation can reject this deliberately duplicated publication.
    let first_before_insert: String = publisher
        .query_one(
            "SELECT tgname::text FROM pg_trigger \
             WHERE tgrelid='atomic_tree_publications'::regclass \
             AND (tgtype::int & 7)=7 AND tgenabled='O' \
             ORDER BY tgname LIMIT 1",
            &[],
        )
        .unwrap()
        .get(0);
    assert_eq!(first_before_insert, "atomic_tree_publications_active");
    publisher
        .query_one(
            "SELECT database_id FROM atomic_databases WHERE database_id=$1 FOR UPDATE",
            &[&entry.database_id],
        )
        .unwrap();
    publisher
        .batch_execute("SET LOCAL lock_timeout='200ms'")
        .unwrap();
    // Exercise the real publication trigger on an authenticated old row.
    // Its already-consumed upload delta must reject this duplicate (P0002);
    // retirement admission must not wait for the independent writer's already
    // held head lock (55P03).
    let rejected = publisher
        .execute(
            "INSERT INTO atomic_tree_publications \
             (database_id,publication_revision,basis_t,tx_hash,manifest_hash,log_generation) \
             SELECT database_id,publication_revision,basis_t,tx_hash,manifest_hash,log_generation \
             FROM atomic_tree_publications WHERE database_id=$1 \
             ORDER BY publication_revision DESC LIMIT 1",
            &[&entry.database_id],
        )
        .unwrap_err();
    assert_eq!(rejected.code().unwrap().code(), "P0002", "{rejected:?}");
    publisher.rollback().unwrap();
    writer.rollback().unwrap();
}
