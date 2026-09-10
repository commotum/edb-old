mod common;
use atomic_core::*;
use postgres::{Client, NoTls};
use std::time::{Duration, Instant};

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP fulltext incremental lifecycle: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    Some(fixture)
}

fn source(url: &str, name: &str) -> TreeManifestRecord {
    let mut trees = PostgresTreeStore::connect(url).unwrap();
    let revision = trees.current_publication_revision(name).unwrap();
    trees.load_manifest(name, revision).unwrap().unwrap()
}

fn seed_empty(url: &str) {
    PostgresStore::connect(url)
        .unwrap()
        .create_database("source", Schema::new())
        .unwrap();
    let writer = common::start_service(url, "source");
    drop(
        writer
            .client()
            .transact(
                TransactionRequest::new("seed", vec![]).with_tx_instant(1),
                Duration::from_secs(30),
            )
            .unwrap(),
    );
    writer.shutdown();
    PostgresIndexer::connect(url, "source")
        .unwrap()
        .consolidate()
        .unwrap();
}

fn gc<T>(mut action: impl FnMut() -> Result<T, SemanticError>) -> T {
    let deadline = Instant::now() + Duration::from_secs(10);
    loop {
        match action() {
            Ok(value) => return value,
            Err(error) if error.category == ErrorCategory::Busy && Instant::now() < deadline => {
                std::thread::sleep(Duration::from_millis(10));
            }
            Err(error) => panic!("isolated fulltext GC: {error:?}"),
        }
    }
}

#[test]
fn shared_successor_survives_predecessor_discard_and_retirement() {
    let Some(fixture) = fixture("fulltext_shared_lifecycle") else {
        return;
    };
    let url = &fixture.connection;
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1000,
                Keyword::new("doc", "body"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            1001,
            Keyword::new("doc", "number"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    PostgresStore::connect(url)
        .unwrap()
        .create_database("source", schema)
        .unwrap();
    let writer = common::start_service(url, "source");
    let report = writer
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                (0..128)
                    .map(|n| TxOp::Add {
                        entity: EntityRef::Temp(format!("doc{n}")),
                        attribute: 1000,
                        value: Value::String(format!("shared amber document {n}")).into(),
                    })
                    .collect(),
            )
            .with_tx_instant(1),
            Duration::from_secs(30),
        )
        .unwrap();
    let entity = report.tempids["doc0"];
    drop(report);
    let mut indexer = PostgresIndexer::connect(url, "source").unwrap();
    indexer.consolidate().unwrap();
    assert_eq!(indexer.fulltext_build_error(), None);
    let predecessor = source(url, "source");
    let old = Connection::connect(url, "source", 0).unwrap();
    let old_reader = old.db().native_fulltext_reader().unwrap().unwrap();
    let old_root = old_reader.projection().root_hash;
    drop(old_reader);
    drop(
        writer
            .client()
            .transact(
                TransactionRequest::new(
                    "non-text",
                    vec![TxOp::Add {
                        entity: EntityRef::Id(entity),
                        attribute: 1001,
                        value: Value::Long(42).into(),
                    }],
                )
                .with_tx_instant(2),
                Duration::from_secs(30),
            )
            .unwrap(),
    );
    writer.shutdown();
    indexer.consolidate().unwrap();
    assert_eq!(indexer.fulltext_build_error(), None);
    let successor = source(url, "source");
    assert_ne!(predecessor.manifest_hash, successor.manifest_hash);
    let current = Connection::connect(url, "source", 0).unwrap();
    let reader = current.db().native_fulltext_reader().unwrap().unwrap();
    assert_eq!(
        reader.projection().root_hash,
        old_root,
        "non-text change must reuse the complete search root"
    );
    drop(reader);
    let mut sql = Client::connect(url, NoTls).unwrap();
    let count = |sql: &mut Client| -> i64 {
        sql.query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
            .unwrap()
            .get(0)
    };
    let pages = count(&mut sql);
    assert!(pages > 1);
    let retained: i64 = sql.query_one(
        "WITH RECURSIVE reachable(hash) AS (SELECT $1::bytea UNION SELECT e.child_hash FROM atomic_fulltext_page_edges e JOIN reachable r ON e.parent_hash=r.hash) SELECT count(*) FROM reachable",
        &[&&old_root[..]],
    ).unwrap().get(0);
    let mut store = FulltextStore::connect(&PostgresConnectionConfig::plaintext(url)).unwrap();
    let mut discarded = false;
    for _ in 0..=pages {
        let before = count(&mut sql);
        discarded = store
            .discard_projection(predecessor.manifest_hash, 1)
            .unwrap();
        assert!(before - count(&mut sql) <= 1);
        assert!(
            count(&mut sql) >= retained,
            "discard removed shared successor pages"
        );
        if discarded {
            break;
        }
    }
    assert!(discarded);
    assert!(store.open(predecessor.manifest_hash, 1).unwrap().is_none());
    drop(old);
    drop(indexer);
    let mut operator = PostgresOperator::connect(url).unwrap();
    let mut retired = false;
    for _ in 0..128 {
        let preview = gc(|| operator.garbage_inventory(Duration::ZERO));
        let applied = gc(|| operator.collect_garbage(Duration::ZERO));
        assert_eq!(preview.fulltext_blocks, applied.fulltext_blocks);
        assert!(applied.fulltext_blocks <= 4096);
        retired = !sql
            .query_one(
                "SELECT EXISTS(SELECT 1 FROM atomic_tree_manifests WHERE manifest_hash=$1)",
                &[&&predecessor.manifest_hash[..]],
            )
            .unwrap()
            .get::<_, bool>(0);
        if retired {
            break;
        }
    }
    assert!(retired, "predecessor canonical manifest did not retire");
    assert!(count(&mut sql) >= retained);
    drop(current);
    let cold = Connection::connect(url, "source", 0).unwrap();
    assert_eq!(
        cold.db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        128
    );
    drop(cold);
    let mut complete = false;
    for _ in 0..32 {
        let before = count(&mut sql);
        complete = store
            .discard_projection(successor.manifest_hash, 1)
            .unwrap();
        assert!(before - count(&mut sql) <= 1);
        if complete {
            break;
        }
    }
    assert!(complete);
    for _ in 0..32 {
        gc(|| operator.collect_garbage(Duration::ZERO));
        if count(&mut sql) == 0 {
            break;
        }
    }
    assert_eq!(
        count(&mut sql),
        0,
        "last-root deletion did not reclaim the shared DAG"
    );
    let unavailable = Connection::connect(url, "source", 0).unwrap();
    assert_eq!(
        unavailable
            .db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap_err()
            .code,
        "fulltext/index-unavailable"
    );
    let mut repair = PostgresIndexer::connect(url, "source").unwrap();
    repair.rebuild_fulltext().unwrap().unwrap();
    assert_eq!(
        unavailable
            .db()
            .fulltext(1000, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        128
    );
    eprintln!(
        "shared fulltext lifecycle pages={pages} predecessor_retired=true cold_successor_hits=128 final_shared_pages=0 rebuilt_hits=128"
    );
}

#[test]
fn frozen_legacy_sidecar_remains_readable_without_shared_membership() {
    let Some(fixture) = fixture("fulltext_legacy_pages") else {
        return;
    };
    let url = &fixture.connection;
    seed_empty(url);
    let source = source(url, "source");
    // Frozen ATFP v1 leaf, independent of the current page encoder: one
    // record with key "legacy" and value "kept".
    let page = b"ATFP\0\0\0\x01\0\0\0\0\x01\0\0\0\x06legacy\0\0\0\x04kept";
    let root = sha256(page);
    let mut header = b"ATFH\0\0\0\x01\0\0\0\x01".to_vec();
    header.extend_from_slice(&source.manifest_hash);
    header.extend_from_slice(&source.basis_t.to_be_bytes());
    header.extend_from_slice(&source.excision_generation.to_be_bytes());
    header.extend_from_slice(&root);
    for n in [1_u64, page.len() as u64, 1] {
        header.extend_from_slice(&n.to_be_bytes())
    }
    let mut sql = Client::connect(url, NoTls).unwrap();
    sql.execute(
        "INSERT INTO atomic_fulltext_blocks(manifest_hash,block_hash,payload) VALUES($1,$2,$3)",
        &[&&source.manifest_hash[..], &&root[..], &&page[..]],
    )
    .unwrap();
    sql.execute("INSERT INTO atomic_fulltext_projections(manifest_hash,analyzer_version,root_hash,header_hash,header) VALUES($1,1,$2,$3,$4)", &[&&source.manifest_hash[..], &&root[..], &&sha256(&header)[..], &header]).unwrap();
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    let peer = Connection::connect(url, "source", 0).unwrap();
    let reader = peer.db().native_fulltext_reader().unwrap().unwrap();
    assert_eq!(
        reader.get(b"legacy").unwrap(),
        Some(FulltextRecord {
            key: b"legacy".to_vec(),
            value: b"kept".to_vec()
        })
    );
    assert!(reader.get(b"absent").unwrap().is_none());
}

#[test]
fn interrupted_build_guard_and_shared_fence_make_gc_fail_safe() {
    let Some(fixture) = fixture("fulltext_build_guard") else {
        return;
    };
    let url = &fixture.connection;
    seed_empty(url);
    let source = source(url, "source");
    let mut store = FulltextStore::connect(&PostgresConnectionConfig::plaintext(url)).unwrap();
    let records = || {
        (0..100).map(|n| {
            Ok(FulltextRecord {
                key: format!("record-{n:03}").into_bytes(),
                value: vec![7; 32],
            })
        })
    };
    assert_eq!(
        store
            .build_with_fault(
                source.manifest_hash,
                source.basis_t,
                source.excision_generation,
                1,
                records(),
                &FulltextBuildLimits::default(),
                FulltextBuildFault::BeforePublication
            )
            .unwrap_err()
            .code,
        "fulltext/injected-before-publication"
    );
    let mut sql = Client::connect(url, NoTls).unwrap();
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_fulltext_page_builds", &[])
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    let pages: i64 = sql
        .query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
        .unwrap()
        .get(0);
    // A crashed retry can reactivate a source whose orphan root was already
    // queued. Candidate admission must recheck the guard, not trust the queue.
    sql.execute(
        "DELETE FROM atomic_fulltext_page_builds WHERE manifest_hash=$1",
        &[&&source.manifest_hash[..]],
    )
    .unwrap();
    let queued = sql
        .query_one(
            "SELECT * FROM atomic_enqueue_fulltext_source_pages($1)",
            &[&&source.manifest_hash[..]],
        )
        .unwrap();
    assert_eq!(queued.get::<_, i64>(0), pages);
    assert!(queued.get::<_, i64>(1) > 0);
    sql.execute(
        "INSERT INTO atomic_fulltext_page_builds VALUES($1)",
        &[&&source.manifest_hash[..]],
    )
    .unwrap();
    let mut operator = PostgresOperator::connect(url).unwrap();
    assert_eq!(
        gc(|| operator.garbage_inventory(Duration::ZERO)).fulltext_blocks,
        0
    );
    assert_eq!(
        gc(|| operator.collect_garbage(Duration::ZERO)).fulltext_blocks,
        0
    );
    sql.query_one(
        "SELECT pg_advisory_lock_shared(atomic_fulltext_gc_pin_key())",
        &[],
    )
    .unwrap();
    let other = self::fixture("fulltext_independent_gc").unwrap();
    let other_key: i64 = Client::connect(&other.connection, NoTls)
        .unwrap()
        .query_one("SELECT atomic_fulltext_gc_pin_key()", &[])
        .unwrap()
        .get(0);
    let key: i64 = sql
        .query_one("SELECT atomic_fulltext_gc_pin_key()", &[])
        .unwrap()
        .get(0);
    assert_ne!(key, other_key, "independent installations share a GC fence");
    let mut other_operator = PostgresOperator::connect(&other.connection).unwrap();
    gc(|| other_operator.garbage_inventory(Duration::ZERO));
    // Observe this exact fulltext fence independently of the operator's other
    // (installation-independent) semantic-GC coordination.
    let mut probe = Client::connect(url, NoTls).unwrap();
    assert_eq!(
        probe
            .query("SELECT * FROM atomic_fulltext_garbage_candidates(1)", &[])
            .unwrap_err()
            .as_db_error()
            .unwrap()
            .code()
            .code(),
        "55P03"
    );
    assert_eq!(
        operator
            .garbage_inventory(Duration::ZERO)
            .unwrap_err()
            .category,
        ErrorCategory::Busy
    );
    assert_eq!(
        operator
            .collect_garbage(Duration::ZERO)
            .unwrap_err()
            .category,
        ErrorCategory::Busy
    );
    assert_eq!(
        store
            .discard_projection(source.manifest_hash, 1)
            .unwrap_err()
            .category,
        ErrorCategory::Busy
    );
    sql.query_one(
        "SELECT pg_advisory_unlock_shared(atomic_fulltext_gc_pin_key())",
        &[],
    )
    .unwrap();
    store
        .build(
            source.manifest_hash,
            source.basis_t,
            source.excision_generation,
            1,
            records(),
            &FulltextBuildLimits::default(),
        )
        .unwrap();
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_fulltext_page_builds", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
            .unwrap()
            .get::<_, i64>(0),
        pages
    );
    let mut complete = false;
    for _ in 0..16 {
        complete = store.discard_projection(source.manifest_hash, 1).unwrap();
        if complete {
            break;
        }
    }
    assert!(complete);
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
}

#[test]
fn runtime_finish_is_narrow_and_ignores_temporary_relation_shadows() {
    let Some(fixture) = fixture("fulltext_runtime_guard") else {
        return;
    };
    let url = &fixture.connection;
    seed_empty(url);
    let source = source(url, "source");
    struct Roles {
        admin: Client,
        writer: String,
        peer: String,
    }
    impl Drop for Roles {
        fn drop(&mut self) {
            let _ = self.admin.batch_execute(&format!(
                "DROP OWNED BY {}, {}; DROP ROLE {}; DROP ROLE {}",
                self.writer, self.peer, self.writer, self.peer,
            ));
        }
    }
    let suffix = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    let writer = format!("ft_writer_{}_{suffix}", std::process::id());
    let peer = format!("ft_peer_{}_{suffix}", std::process::id());
    let mut roles = Roles {
        admin: Client::connect(url, NoTls).unwrap(),
        writer,
        peer,
    };
    roles
        .admin
        .batch_execute(&format!(
            "CREATE ROLE {} LOGIN PASSWORD '{suffix}'; CREATE ROLE {}",
            roles.writer, roles.peer
        ))
        .unwrap();
    PostgresMigrator::connect(url)
        .unwrap()
        .grant_runtime_privileges(&roles.writer, &roles.peer)
        .unwrap();
    let mut store = FulltextStore::connect(&PostgresConnectionConfig::plaintext(url)).unwrap();
    let records = || {
        [Ok(FulltextRecord {
            key: b"key".to_vec(),
            value: b"value".to_vec(),
        })]
        .into_iter()
    };
    assert!(
        store
            .build_with_fault(
                source.manifest_hash,
                source.basis_t,
                source.excision_generation,
                1,
                records(),
                &FulltextBuildLimits::default(),
                FulltextBuildFault::BeforePublication
            )
            .is_err()
    );
    let mut runtime = Client::connect(url, NoTls).unwrap();
    runtime
        .batch_execute(&format!("SET ROLE {}", roles.writer))
        .unwrap();
    runtime
        .batch_execute(
            "CREATE TEMP TABLE atomic_fulltext_pages(block_hash bytea); \
         CREATE TEMP TABLE atomic_fulltext_projections(manifest_hash bytea,root_hash bytea); \
         CREATE TEMP TABLE atomic_fulltext_page_roots(manifest_hash bytea,root_hash bytea); \
         CREATE TEMP TABLE atomic_fulltext_page_builds(manifest_hash bytea)",
        )
        .unwrap();
    for table in ["atomic_fulltext_projections", "atomic_fulltext_page_roots"] {
        runtime
            .execute(
                &format!("INSERT INTO pg_temp.{table} VALUES($1,$1)"),
                &[&&source.manifest_hash[..]],
            )
            .unwrap();
    }
    runtime
        .execute(
            "INSERT INTO pg_temp.atomic_fulltext_page_builds VALUES($1)",
            &[&&source.manifest_hash[..]],
        )
        .unwrap();
    let owner_key: i64 = roles
        .admin
        .query_one("SELECT atomic_fulltext_gc_pin_key()", &[])
        .unwrap()
        .get(0);
    let runtime_key: i64 = runtime
        .query_one("SELECT atomic_fulltext_gc_pin_key()", &[])
        .unwrap()
        .get(0);
    assert_eq!(
        owner_key, runtime_key,
        "temporary page table changed the installation fence"
    );
    let error = runtime
        .query_one(
            "SELECT * FROM atomic_finish_fulltext_build($1)",
            &[&&source.manifest_hash[..]],
        )
        .unwrap_err();
    assert_eq!(
        error.code().unwrap().code(),
        "23503",
        "fake root authorized guard removal"
    );
    assert_eq!(
        roles
            .admin
            .query_one("SELECT count(*) FROM atomic_fulltext_page_builds", &[])
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    store
        .build(
            source.manifest_hash,
            source.basis_t,
            source.excision_generation,
            1,
            records(),
            &FulltextBuildLimits::default(),
        )
        .unwrap();
    runtime
        .execute(
            &format!(
                "INSERT INTO {}.atomic_fulltext_page_builds VALUES($1)",
                fixture.schema
            ),
            &[&&source.manifest_hash[..]],
        )
        .unwrap();
    let row = runtime
        .query_one(
            "SELECT * FROM atomic_finish_fulltext_build($1)",
            &[&&source.manifest_hash[..]],
        )
        .unwrap();
    assert_eq!(row.get::<_, i64>(0), 1);
    assert_eq!(row.get::<_, i64>(1), 0);
    assert_eq!(
        roles
            .admin
            .query_one("SELECT count(*) FROM atomic_fulltext_page_builds", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    assert_eq!(
        runtime
            .query_one(
                "SELECT count(*) FROM pg_temp.atomic_fulltext_page_builds",
                &[]
            )
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    let error = runtime
        .execute(
            &format!("DELETE FROM {}.atomic_fulltext_pages", fixture.schema),
            &[],
        )
        .unwrap_err();
    assert_eq!(error.code().unwrap().code(), "42501");
    runtime
        .batch_execute(&format!("RESET ROLE; SET ROLE {}", roles.peer))
        .unwrap();
    let error = runtime
        .query_one(
            "SELECT * FROM atomic_finish_fulltext_build($1)",
            &[&&source.manifest_hash[..]],
        )
        .unwrap_err();
    assert_eq!(error.code().unwrap().code(), "42501");
    drop(runtime);
    assert!(
        store
            .discard_projection(source.manifest_hash, 4096)
            .unwrap()
    );
    // Exercise the complete uploader under actual restricted credentials too,
    // including edge/root triggers, not just a SET ROLE call to finish.
    let writer_url = if url.starts_with("postgres://") || url.starts_with("postgresql://") {
        format!("{url}&user={}&password={suffix}", roles.writer)
    } else {
        format!("{url} user={} password={suffix}", roles.writer)
    };
    let mut uploader =
        FulltextStore::connect(&PostgresConnectionConfig::plaintext(&writer_url)).unwrap();
    let (_, stats) = uploader
        .build(
            source.manifest_hash,
            source.basis_t,
            source.excision_generation,
            1,
            (0..100).map(|n| {
                Ok(FulltextRecord {
                    key: format!("key-{n:03}").into_bytes(),
                    value: vec![7; 32],
                })
            }),
            &FulltextBuildLimits {
                page_bytes: 1024,
                ..Default::default()
            },
        )
        .unwrap();
    assert!(stats.blocks_inserted > 1);
    assert!(stats.edges_inserted > 0);
    assert_eq!(stats.roots_inserted, 1);
    assert_eq!(stats.retention_pages_examined, stats.blocks_inserted);
    assert_eq!(stats.retention_candidates_added, 0);
}
