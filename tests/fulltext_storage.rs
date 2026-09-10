mod common;
use atomic_core::*;
use postgres::{Client, NoTls};
use std::time::Duration;

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED fulltext storage: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    Some(fixture)
}
fn records() -> impl Iterator<Item = Result<FulltextRecord, SemanticError>> {
    (0u32..600).rev().map(|n| {
        Ok(FulltextRecord {
            key: format!("term/{n:06}").into_bytes(),
            value: vec![(n % 251) as u8; 64],
        })
    })
}
fn projection_source(url: &str, name: &str) -> TreeManifestRecord {
    let mut trees = PostgresTreeStore::connect(url).unwrap();
    let revision = trees.current_publication_revision(name).unwrap();
    trees.load_manifest(name, revision).unwrap().unwrap()
}
fn gc_retry<T>(mut action: impl FnMut() -> Result<T, SemanticError>) -> T {
    let deadline = std::time::Instant::now() + Duration::from_secs(10);
    loop {
        match action() {
            Ok(value) => return value,
            Err(error)
                if error.category == ErrorCategory::Busy
                    && std::time::Instant::now() < deadline =>
            {
                std::thread::sleep(Duration::from_millis(10))
            }
            Err(error) => panic!("bounded isolated GC failed: {error:?}"),
        }
    }
}

#[test]
fn excision_rebuild_uses_only_the_successor_generation() {
    let Some(fixture) = fixture("fulltext_excision") else {
        return;
    };
    let url = &fixture.connection;
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1000,
                Keyword::new("document", "body"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let initial = PostgresStore::connect(url)
        .unwrap()
        .create_database("source", schema)
        .unwrap();
    let service = common::start_service(url, "source");
    let seeded = common::transact(
        &service,
        "documents",
        initial.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("remove".into()),
                attribute: 1000,
                value: Value::String("confidential amber".into()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("keep".into()),
                attribute: 1000,
                value: Value::String("public blue".into()).into(),
            },
        ],
        1000,
    );
    let removed = seeded.tempids["remove"];
    let basis = seeded.basis_t;
    drop(seeded);
    let requested = common::transact(
        &service,
        "excise",
        basis,
        &[TxOp::Add {
            entity: EntityRef::Temp("request".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(removed)),
        }],
        2000,
    );
    drop(requested);
    service.shutdown();
    let mut indexer = PostgresIndexer::connect(url, "source").unwrap();
    indexer.consolidate().unwrap();
    assert_eq!(indexer.fulltext_build_error(), None);
    drop(indexer);
    let old = Connection::connect(url, "source", 128).unwrap();
    let held = old.db();
    let old_generation = held
        .native_fulltext_reader()
        .unwrap()
        .unwrap()
        .source_generation();
    assert_eq!(
        held.fulltext(1000, "confidential", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    let receipt = PostgresOperator::connect(url)
        .unwrap()
        .process_excision_requests("source")
        .unwrap();
    assert!(receipt.generation > old_generation);
    assert!(receipt.removed_datoms > 0);
    let current = Connection::connect(url, "source", 128).unwrap();
    match current
        .db()
        .fulltext(1000, "confidential", &FulltextOptions::default())
    {
        Ok(report) => assert!(report.hits.is_empty()),
        Err(error) => assert_eq!(error.code, "fulltext/index-unavailable"),
    }
    let mut rebuilder = PostgresIndexer::connect(url, "source").unwrap();
    let projection = rebuilder.rebuild_fulltext().unwrap().unwrap();
    assert_eq!(projection.source_generation, receipt.generation);
    assert!(
        current
            .db()
            .fulltext(1000, "confidential", &FulltextOptions::default())
            .unwrap()
            .hits
            .is_empty()
    );
    assert!(
        current
            .db()
            .history()
            .fulltext(1000, "confidential", &FulltextOptions::default())
            .unwrap()
            .hits
            .is_empty()
    );
    assert_eq!(
        current
            .db()
            .fulltext(1000, "blue", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    // Already retained values stay exact. Neither excision nor cache invalidation
    // can erase application-held strings; physical reclamation follows pins.
    assert_eq!(
        held.fulltext(1000, "confidential", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    println!(
        "FULLTEXT_EXCISION source_generation={old_generation} successor_generation={} current_and_history_exclude_removed=true unrelated_document_preserved=true retained_value_exact=true",
        receipt.generation
    );
}

#[test]
fn retained_search_source_pins_protect_blocks_until_bounded_gc() {
    let Some(fixture) = fixture("fulltext_gc") else {
        return;
    };
    let url = &fixture.connection;
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1000,
                Keyword::new("document", "body"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let initial = PostgresStore::connect(url)
        .unwrap()
        .create_database("source", schema)
        .unwrap();
    let service = common::start_service(url, "source");
    let report = common::transact(
        &service,
        "first",
        initial.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Temp("doc".into()),
            attribute: 1000,
            value: Value::String("retained amber".into()).into(),
        }],
        1000,
    );
    let entity = report.tempids["doc"];
    let basis = report.basis_t;
    drop(report);
    let mut indexer = PostgresIndexer::connect(url, "source").unwrap();
    indexer.consolidate().unwrap();
    assert_eq!(indexer.fulltext_build_error(), None);
    let old = Connection::connect(url, "source", 128).unwrap();
    let held = old.db();
    let source = projection_source(url, "source");
    assert_eq!(
        held.fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    drop(common::transact(
        &service,
        "second",
        basis,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: 1000,
            value: Value::String("current blue".into()).into(),
        }],
        2000,
    ));
    service.shutdown();
    indexer.consolidate().unwrap();
    assert_eq!(indexer.fulltext_build_error(), None);
    drop(indexer);
    let current = Connection::connect(url, "source", 128).unwrap();
    assert!(
        current
            .db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .is_empty()
    );
    let mut operator = PostgresOperator::connect(url).unwrap();
    let mut sql = Client::connect(url, NoTls).unwrap();
    for _ in 0..8 {
        gc_retry(|| operator.collect_garbage(Duration::ZERO));
    }
    let blocks = |sql: &mut Client| -> i64 {
        sql.query_one(
            "SELECT (SELECT count(*) FROM atomic_fulltext_blocks WHERE manifest_hash=$1) \
                  + (SELECT count(*) FROM atomic_fulltext_pages WHERE created_for=$1)",
            &[&&source.manifest_hash[..]],
        )
        .unwrap()
        .get(0)
    };
    assert!(blocks(&mut sql) > 0);
    assert_eq!(
        held.fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    drop(held);
    drop(old);
    let retired = |sql: &mut Client| -> bool {
        sql.query_one(
            "SELECT NOT EXISTS(SELECT 1 FROM atomic_fulltext_projections WHERE manifest_hash=$1) \
                AND NOT EXISTS(SELECT 1 FROM atomic_fulltext_page_roots WHERE manifest_hash=$1) \
                AND NOT EXISTS(SELECT 1 FROM atomic_fulltext_blocks WHERE manifest_hash=$1) \
                AND NOT EXISTS(SELECT 1 FROM atomic_fulltext_pages p WHERE p.created_for=$1 \
                  AND NOT EXISTS(SELECT 1 FROM atomic_fulltext_page_roots r WHERE r.root_hash=p.block_hash) \
                  AND NOT EXISTS(SELECT 1 FROM atomic_fulltext_page_edges e WHERE e.child_hash=p.block_hash) \
                  AND NOT EXISTS(SELECT 1 FROM atomic_fulltext_page_builds b WHERE b.manifest_hash=p.created_for))",
            &[&&source.manifest_hash[..]],
        ).unwrap().get(0)
    };
    let mut removed = 0;
    for _ in 0..128 {
        let before = blocks(&mut sql);
        let preview = gc_retry(|| operator.garbage_inventory(Duration::ZERO));
        assert_eq!(blocks(&mut sql), before, "dry-run mutated search blocks");
        let applied = gc_retry(|| operator.collect_garbage(Duration::ZERO));
        assert_eq!(applied.fulltext_blocks, preview.fulltext_blocks);
        assert!(applied.fulltext_blocks <= 4096);
        removed += applied.fulltext_blocks;
        if retired(&mut sql) {
            break;
        }
    }
    assert!(retired(&mut sql), "released search source did not retire");
    assert!(removed > 0);
    assert_eq!(
        current
            .db()
            .fulltext(1000, "blue", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    println!(
        "FULLTEXT_GC held_source_protected=true dry_run_nonmutating=true released_blocks_removed={removed} max_batch=4096 current_search_preserved=true"
    );
}
#[test]
fn immutable_publication_interruption_merkle_reads_and_explicit_repair() {
    let Some(fixture) = fixture("fulltext_storage") else {
        return;
    };
    let url = &fixture.connection;
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let initial = PostgresStore::connect(url)
        .unwrap()
        .create_database("source", schema)
        .unwrap();
    let service = common::start_service(url, "source");
    drop(common::transact(
        &service,
        "seed",
        initial.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Temp("one".into()),
            attribute: 1000,
            value: Value::Long(1).into(),
        }],
        1000,
    ));
    service.shutdown();
    PostgresIndexer::connect(url, "source")
        .unwrap()
        .consolidate()
        .unwrap();
    let connection = Connection::connect(url, "source", 128).unwrap();
    let db = connection.db();
    let source = projection_source(url, "source");
    let config = PostgresConnectionConfig::plaintext(url);
    let mut store = FulltextStore::connect(&config).unwrap();
    let limits = FulltextBuildLimits {
        sort_memory_bytes: 1024,
        page_bytes: 1024,
        ..Default::default()
    };
    let error = store
        .build_with_fault(
            source.manifest_hash,
            source.basis_t,
            source.excision_generation,
            1,
            records(),
            &limits,
            FulltextBuildFault::BeforePublication,
        )
        .unwrap_err();
    assert_eq!(error.code, "fulltext/injected-before-publication");
    assert!(store.open(source.manifest_hash, 1).unwrap().is_none());
    assert_eq!(
        db.native_fulltext_reader().err().unwrap().code,
        "fulltext/index-unavailable"
    );
    let mut sql = Client::connect(url, NoTls).unwrap();
    let unpublished: i64 = sql
        .query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
        .unwrap()
        .get(0);
    assert!(unpublished > 1);
    let (projection, stats) = store
        .build(
            source.manifest_hash,
            source.basis_t,
            source.excision_generation,
            1,
            records(),
            &limits,
        )
        .unwrap();
    assert_eq!(projection.record_count, 600);
    assert!(stats.spill_bytes > 0);
    assert!(stats.peak_buffer_bytes < 1200);
    let count: i64 = sql
        .query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
        .unwrap()
        .get(0);
    assert_eq!(count, unpublished, "retry duplicated immutable pages");
    let reader = db.native_fulltext_reader().unwrap().unwrap();
    let mut cold = reader
        .prefix(b"term/00001", FulltextReadLimits::default())
        .unwrap();
    assert_eq!(
        cold.by_ref().collect::<Result<Vec<_>, _>>().unwrap().len(),
        10
    );
    let cold_stats = cold.stats();
    assert!(cold_stats.blocks_read > 0);
    assert!(cold_stats.blocks_read < projection.block_count);
    let context = OperationContext::new(OperationKind::Query);
    let _guard = context.enter();
    let mut warm = db
        .native_fulltext_reader()
        .unwrap()
        .unwrap()
        .prefix(b"term/00001", FulltextReadLimits::default())
        .unwrap();
    assert_eq!(
        warm.by_ref().collect::<Result<Vec<_>, _>>().unwrap().len(),
        10
    );
    assert_eq!(context.snapshot().sql_calls, 0);
    assert_eq!(warm.stats().block_bytes, 0);
    assert!(warm.stats().visited_bytes > 0);
    drop(_guard);
    assert!(reader.get(b"term/000010x").unwrap().is_none());
    assert!(reader.get(b"term/999999").unwrap().is_none());
    // Missing non-root page is a hard integrity error, never an empty result.
    let leaf:Vec<u8>=sql.query_one("SELECT block_hash FROM atomic_fulltext_pages WHERE created_for=$1 AND block_hash<>$2 AND get_byte(payload,8)=0 LIMIT 1",&[&&source.manifest_hash[..],&&projection.root_hash[..]]).unwrap().get(0);
    // Owner-only fault injection deliberately removes the page and its
    // incoming retention edges. Ordinary GC may never delete a referenced page.
    let mut corrupt = sql.transaction().unwrap();
    corrupt
        .execute(
            "DELETE FROM atomic_fulltext_page_edges WHERE child_hash=$1",
            &[&leaf],
        )
        .unwrap();
    corrupt
        .execute(
            "DELETE FROM atomic_fulltext_pages WHERE block_hash=$1",
            &[&leaf],
        )
        .unwrap();
    corrupt.commit().unwrap();
    let cold_connection = Connection::connect(url, "source", 128).unwrap();
    let cold_reader = cold_connection
        .db()
        .native_fulltext_reader()
        .unwrap()
        .unwrap();
    assert_eq!(
        cold_reader
            .prefix(b"", FulltextReadLimits::default())
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap_err()
            .code,
        "fulltext/missing-block"
    );
    assert!(!store.discard_projection(source.manifest_hash, 1).unwrap());
    // Shared DAG reclamation releases direct children into the next frontier;
    // a large batch does not recursively cascade through newly orphaned pages.
    let mut complete = false;
    for _ in 0..32 {
        let before: i64 = sql
            .query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
            .unwrap()
            .get(0);
        complete = store
            .discard_projection(source.manifest_hash, 4096)
            .unwrap();
        let after: i64 = sql
            .query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
            .unwrap()
            .get(0);
        assert!(before - after <= 4096);
        if complete {
            break;
        }
    }
    assert!(complete);
    assert!(store.open(source.manifest_hash, 1).unwrap().is_none());
    store
        .build(
            source.manifest_hash,
            source.basis_t,
            source.excision_generation,
            1,
            records(),
            &limits,
        )
        .unwrap();
    let reopened = Connection::connect(url, "source", 128).unwrap();
    assert_eq!(
        reopened
            .db()
            .native_fulltext_reader()
            .unwrap()
            .unwrap()
            .prefix(b"", FulltextReadLimits::default())
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap()
            .len(),
        600
    );
    assert_eq!(
        projection_source(url, "source").manifest_hash,
        source.manifest_hash
    );
    println!(
        "FULLTEXT_STORAGE records=600 blocks={} encoded_bytes={} spill_bytes={} sort_peak_bytes={} cold_blocks={} cold_bytes={} warm_sql=0 partial_publication_rejected=true explicit_repair=true",
        stats.blocks,
        stats.encoded_bytes,
        stats.spill_bytes,
        stats.peak_buffer_bytes,
        cold_stats.blocks_read,
        cold_stats.block_bytes
    );
}

#[test]
fn native_search_survives_restart_and_reconstructs_after_portable_restore() {
    let Some(fixture) = fixture("fulltext_restore_source") else {
        return;
    };
    let url = &fixture.connection;
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1000,
                Keyword::new("document", "body"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let initial = PostgresStore::connect(url)
        .unwrap()
        .create_database("source", schema)
        .unwrap();
    let mut genesis_indexer = PostgresIndexer::connect(url, "source").unwrap();
    genesis_indexer.consolidate().unwrap();
    assert_eq!(genesis_indexer.fulltext_build_error(), None);
    let empty = Connection::connect(url, "source", 128).unwrap();
    assert!(
        empty
            .db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .is_empty()
    );
    drop(empty);
    drop(genesis_indexer);
    let service = common::start_service(url, "source");
    let report = common::transact(
        &service,
        "documents",
        initial.basis_t(),
        &(0..16)
            .map(|n| TxOp::Add {
                entity: EntityRef::Temp(format!("doc{n}")),
                attribute: 1000,
                value: Value::String(format!("durable amber document {n}")).into(),
            })
            .collect::<Vec<_>>(),
        1000,
    );
    let expected = report
        .db_after
        .clone()
        .history()
        .collect_datoms(IndexOrder::Eavt)
        .unwrap();
    drop(report);
    service.shutdown();
    let mut indexer = PostgresIndexer::connect(url, "source").unwrap();
    indexer.consolidate().unwrap();
    assert_eq!(indexer.fulltext_build_error(), None);
    let connection = Connection::connect(url, "source", 128).unwrap();
    assert_eq!(
        connection
            .db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        16
    );
    drop(connection);
    drop(indexer);
    let reopened = Connection::connect(url, "source", 128).unwrap();
    assert_eq!(
        reopened
            .db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        16
    );
    let directory = tempfile::tempdir().unwrap();
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    }
    let point = PortableBackup::connect(url)
        .unwrap()
        .backup_database("source", directory.path())
        .unwrap();
    let target = common::PostgresFixture::new(
        &std::env::var("ATOMIC_POSTGRES_URL").unwrap(),
        "fulltext_restore_target",
    );
    PostgresMigrator::connect(&target.connection)
        .unwrap()
        .migrate()
        .unwrap();
    PortableBackup::connect(&target.connection)
        .unwrap()
        .restore_backup(directory.path(), point.basis_t, "restored")
        .unwrap();
    let restored = Connection::connect(&target.connection, "restored", 128).unwrap();
    assert_eq!(
        restored
            .db()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    assert_eq!(
        restored
            .db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap_err()
            .code,
        "fulltext/index-unavailable"
    );
    let mut rebuilt = PostgresIndexer::connect(&target.connection, "restored").unwrap();
    assert!(rebuilt.rebuild_fulltext().unwrap().is_some());
    assert_eq!(
        restored
            .db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        16
    );
    // Explicit capacity failure remains observable while canonical indexing succeeds.
    let source = projection_source(&target.connection, "restored");
    let mut sidecar =
        FulltextStore::connect(&PostgresConnectionConfig::plaintext(&target.connection)).unwrap();
    assert!(
        sidecar
            .discard_projection(source.manifest_hash, 4096)
            .unwrap()
    );
    let mut limited = PostgresIndexer::connect(&target.connection, "restored")
        .unwrap()
        .with_fulltext_build_limits(FulltextBuildLimits {
            max_records: 1,
            ..Default::default()
        });
    limited.consolidate().unwrap();
    assert_eq!(
        limited.fulltext_build_error().unwrap().code,
        "fulltext/build-limit"
    );
    assert_eq!(
        limited.rebuild_fulltext().unwrap_err().code,
        "fulltext/build-limit"
    );
    println!(
        "FULLTEXT_RESTORE retained_history_exact=true restored_search_explicit_lag=true rebuilt_hits=16 optional_build_failure_preserves_canonical=true"
    );
}
