use atomic_core::storage::{BlockDatabase, BlockReadConfig, BlockReader, CasOutcome, PgBlockStore};
use atomic_core::*;
use atomic_core::{BlockTransactor, BlockWriterOptions};
use std::sync::{Arc, Barrier};
mod common;

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL catalog: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PgBlockStore::install(&PostgresConnectionConfig::plaintext(&fixture.connection)).unwrap();
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
    assert_ne!(results[0].database.database_id, "shared");
    let mut catalog = DatabaseCatalog::connect(&fixture.connection).unwrap();
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
    let context = OperationContext::new(OperationKind::Transaction);
    let guard = context.enter();
    let existing = catalog.create_if_absent("shared", invalid_initial).unwrap();
    assert!(!existing.created);
    assert_eq!(existing.database, results[0].database);
    assert!(context.snapshot().sql_calls <= 16);
    assert_eq!(context.snapshot().known_payload_write_bytes, 0);
    drop(guard);
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
fn checked_rename_retire_and_name_reuse_fence_writers_without_rewriting_held_values() {
    let Some(fixture) = fixture("catalog_identity") else {
        return;
    };
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    let mut catalog = DatabaseCatalog::connect_configured(&config).unwrap();
    let old = catalog
        .create_if_absent("original", Schema::new())
        .unwrap()
        .database;
    let database = BlockDatabase::resolve(&config, "original").unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    writer
        .transact(&TransactionRequest::new("first", vec![]))
        .unwrap();
    let reader = BlockReader::connect(
        &config,
        BlockReadConfig {
            cache_entries: 0,
            cache_bytes: 0,
            ..Default::default()
        },
    )
    .unwrap();
    let retained = reader.capture(&database.reference_key()).unwrap();
    let before_root = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let before_id: [u8; 32] = before_root.value.as_deref().unwrap().try_into().unwrap();
    let before_bytes = store.get(before_id).unwrap().unwrap();
    assert_eq!(
        catalog
            .rename_checked("original", "renamed", "wrong")
            .unwrap_err()
            .code,
        "catalog/identity-mismatch"
    );
    let renamed = catalog
        .rename_checked("original", "renamed", &old.lineage_id)
        .unwrap();
    assert_eq!(renamed.database_id, old.database_id);
    assert_eq!(
        catalog.resolve("original").unwrap_err().code,
        "catalog/name-not-found"
    );
    assert_eq!(
        catalog.require_active_id(&old.database_id).unwrap(),
        renamed
    );
    assert_eq!(
        store.read_ref(&database.reference_key()).unwrap(),
        Some(before_root)
    );
    writer
        .transact(&TransactionRequest::new("after-rename", vec![]))
        .unwrap();
    let replacement = catalog
        .create_if_absent("original", Schema::new())
        .unwrap()
        .database;
    assert_ne!(replacement.database_id, old.database_id);
    assert_eq!(
        catalog
            .retire_checked("original", &old.lineage_id)
            .unwrap_err()
            .code,
        "catalog/identity-mismatch"
    );
    let stale = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let retired = catalog
        .retire_checked("renamed", &old.lineage_id)
        .unwrap()
        .unwrap();
    assert!(retired.retired && retired.name.is_none());
    assert_eq!(
        catalog
            .require_active_id(&old.database_id)
            .unwrap_err()
            .code,
        "catalog/database-retired"
    );
    assert!(catalog.retire("renamed").unwrap().is_none());
    assert_eq!(catalog.list_retired(None, 10).unwrap(), vec![retired]);
    assert!(
        catalog
            .list_retired(Some(&old.database_id), 10)
            .unwrap()
            .is_empty()
    );
    assert!(
        writer
            .transact(&TransactionRequest::new("after-retire", vec![]))
            .is_err()
    );
    assert!(
        matches!(
            store
                .compare_exchange(
                    &database.reference_key(),
                    Some(stale.revision),
                    stale.value.as_deref()
                )
                .unwrap(),
            CasOutcome::Conflict(_)
        ),
        "a prepared publisher cannot resurrect a retired root"
    );
    assert!(reader.capture(&database.reference_key()).is_err());
    let context = OperationContext::new(OperationKind::Query);
    let guard = context.enter();
    assert!(
        !retained
            .database_value()
            .datoms(IndexOrder::Eavt)
            .unwrap()
            .is_empty()
    );
    assert!(
        context.snapshot().known_payload_read_bytes > 0,
        "held value performs a cold immutable read after retirement"
    );
    drop(guard);
    assert_eq!(store.get(before_id).unwrap().unwrap(), before_bytes);
    assert_eq!(catalog.resolve("original").unwrap(), replacement);
    assert_eq!(
        store
            .list_live_refs("catalog/identities/", None, 128)
            .unwrap()
            .len(),
        2
    );
}

#[test]
fn catalog_pages_preserve_utf8_order_and_unbounded_name_length() {
    let Some(fixture) = fixture("catalog_pages") else {
        return;
    };
    let mut catalog = DatabaseCatalog::connect(&fixture.connection).unwrap();
    let mut names = vec![
        "Alpha".to_owned(),
        "a".to_owned(),
        "a/%_".to_owned(),
        "z".to_owned(),
        "é".to_owned(),
        "数据库".to_owned(),
        "a".repeat(1500),
        "β".repeat(800),
        "z".repeat(1500),
    ];
    for name in &names {
        catalog.create_if_absent(name, Schema::new()).unwrap();
    }
    names.sort();
    let mut found = Vec::new();
    let mut after = None;
    loop {
        let page = catalog.list(after.as_deref(), 2).unwrap();
        if page.is_empty() {
            break;
        }
        assert!(page.len() <= 2);
        after = page.last().unwrap().name.clone();
        found.extend(page.into_iter().map(|entry| entry.name.unwrap()));
    }
    assert_eq!(found, names);
    let long = "q".repeat(2000);
    let renamed = catalog.rename("Alpha", &long).unwrap();
    assert_eq!(catalog.resolve(&long).unwrap(), renamed);
    let back = catalog.rename(&long, "short-again").unwrap();
    assert_eq!(back.database_id, renamed.database_id);
    assert!(catalog.resolve(&long).is_err());
    assert_eq!(
        catalog.rename("a", "z").unwrap_err().code,
        "catalog/name-exists"
    );
    assert_eq!(
        catalog
            .create_if_absent("", Schema::new())
            .unwrap_err()
            .code,
        "catalog/invalid-name"
    );
    assert_eq!(
        catalog
            .create_if_absent("nul\0name", Schema::new())
            .unwrap_err()
            .code,
        "catalog/invalid-name"
    );
}

#[test]
fn retirement_fences_and_releases_an_interrupted_excision_owner() {
    let Some(fixture) = fixture("catalog_excision_retire") else {
        return;
    };
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    let database = BlockDatabase::create(&config, "cancel-work", Schema::new()).unwrap();
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    let first = writer
        .transact(&TransactionRequest::new(
            "data",
            vec![TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: DB_DOC as u32,
                value: Value::String("private subject".into()).into(),
            }],
        ))
        .unwrap();
    writer
        .transact(&TransactionRequest::new(
            "excise",
            vec![TxOp::Add {
                entity: EntityRef::Temp("erase".into()),
                attribute: DB_EXCISE as u32,
                value: Value::Ref(first.tempids["item"]).into(),
            }],
        ))
        .unwrap();
    writer.release().unwrap();
    let mut catalog = DatabaseCatalog::connect_configured(&config).unwrap();
    let entry = catalog.resolve("cancel-work").unwrap();
    let mut operator = PostgresOperator::connect_configured(&config).unwrap();
    assert_eq!(
        operator
            .process_excision_requests_with_fault(&entry.database_id, ExcisionFault::AfterCapture)
            .unwrap_err()
            .code,
        "excision/injected-fault"
    );
    let key = format!("excision/{}", entry.database_id);
    let mut store = PgBlockStore::connect(&config).unwrap();
    let pending = store.read_ref(&key).unwrap().unwrap();
    assert!(pending.value.is_some());
    catalog.retire("cancel-work").unwrap();
    assert!(
        store.read_ref(&key).unwrap().unwrap().value.is_none(),
        "retired database work must not retain its candidate forever"
    );
    assert!(matches!(
        store
            .compare_exchange(&key, Some(pending.revision), pending.value.as_deref())
            .unwrap(),
        CasOutcome::Conflict(_)
    ));
}
