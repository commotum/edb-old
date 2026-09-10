mod common;

use atomic_core::*;
use std::time::{Duration, Instant};

const TEXT: u32 = 1000;
const MARKER: u32 = 1001;
const WAIT: Duration = Duration::from_secs(30);

fn retry_busy<T>(mut operation: impl FnMut() -> Result<T, SemanticError>) -> T {
    let deadline = Instant::now() + WAIT;
    loop {
        match operation() {
            Ok(value) => return value,
            Err(error) if error.category == ErrorCategory::Busy && Instant::now() < deadline => {
                std::thread::sleep(Duration::from_millis(10));
            }
            Err(error) => panic!("bounded fulltext maintenance failed: {error:?}"),
        }
    }
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                TEXT,
                Keyword::new("incremental", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            MARKER,
            Keyword::new("incremental", "marker"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn records(db: &DatabaseValue) -> Vec<FulltextRecord> {
    db.native_fulltext_reader()
        .unwrap()
        .unwrap()
        .prefix(b"", FulltextReadLimits::default())
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap()
}

fn rebuilt_records(
    url: &str,
    database: &str,
    indexer: &mut PostgresIndexer,
    db: &DatabaseValue,
) -> Vec<FulltextRecord> {
    let source = db
        .native_fulltext_reader()
        .unwrap()
        .unwrap()
        .projection()
        .source_manifest;
    let mut store = FulltextStore::connect(&PostgresConnectionConfig::plaintext(url)).unwrap();
    while !retry_busy(|| store.discard_projection(source, 4096)) {}
    indexer.rebuild_fulltext().unwrap().unwrap();
    let after_peer = Connection::connect(url, database, 128).unwrap();
    records(&after_peer.db())
}

#[test]
fn fixed_nontext_and_text_changes_do_not_rebuild_growing_search_corpora() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED incremental fulltext PG: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "fulltext_incremental");
    let url = &fixture.connection;
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    // Default leaf capacity exercises ordinary deployment behavior. The second
    // ladder crosses many small canonical leaves without requiring a huge seed;
    // it makes the hash-pruned page cost observable, not a default-size claim.
    for (size, canonical_leaf_datoms) in [
        (64, 4096),
        (256, 4096),
        (1024, 4096),
        (64, 64),
        (256, 64),
        (1024, 64),
    ] {
        let database = format!("fulltext_incremental_{size}_{canonical_leaf_datoms}");
        PostgresStore::connect(url)
            .unwrap()
            .create_database(&database, schema())
            .unwrap();
        let writer = common::start_service(url, &database);
        let client = writer.client();
        let setup = Instant::now();
        let seed_request = TransactionRequest::new(
            "seed",
            (0..size)
                .map(|n| {
                    add(
                        EntityRef::Temp(format!("d{n}")),
                        TEXT,
                        Value::String(format!("archive quartz unique{n:05}")),
                    )
                })
                .collect(),
        );
        let seed = client.transact(seed_request.clone(), WAIT).unwrap();
        let entity = seed.tempids["d0"];
        let mut indexer = PostgresIndexer::connect(url, &database)
            .unwrap()
            .with_segment_datoms(canonical_leaf_datoms)
            .unwrap()
            .with_fulltext_build_limits(FulltextBuildLimits {
                sort_memory_bytes: 16 * 1024,
                page_bytes: 2048,
                ..Default::default()
            });
        indexer.consolidate().unwrap();
        assert_eq!(indexer.fulltext_build_error(), None);
        let initial_stats = indexer.fulltext_build_stats();
        let held_peer = Connection::connect(url, &database, 128).unwrap();
        let held = held_peer.db();
        let initial_projection = held
            .native_fulltext_reader()
            .unwrap()
            .unwrap()
            .projection()
            .clone();
        eprintln!(
            "FULLTEXT_DELTA_SETUP size={size} canonical_leaf_datoms={canonical_leaf_datoms} search_page_bytes=2048 sort_bytes=16384 records={} pages={} bytes={} setup_us={}",
            initial_stats.input_records,
            initial_projection.block_count,
            initial_projection.encoded_bytes,
            setup.elapsed().as_micros(),
        );

        for (kind, operation) in [
            (
                "nontext",
                add(EntityRef::Id(entity), MARKER, Value::Long(1)),
            ),
            (
                "text",
                add(
                    EntityRef::Id(entity),
                    TEXT,
                    Value::String("replacement jade newtoken".into()),
                ),
            ),
        ] {
            let context = OperationContext::new(OperationKind::Query);
            let started = Instant::now();
            let stats;
            let calls;
            let sql_calls;
            let result_cell_bytes;
            {
                let _guard = context.enter();
                let report = client
                    .transact(TransactionRequest::new(kind, vec![operation]), WAIT)
                    .unwrap();
                indexer.consolidate().unwrap();
                assert_eq!(indexer.fulltext_build_error(), None);
                stats = indexer.fulltext_build_stats();
                let fresh_peer = Connection::connect(url, &database, 128).unwrap();
                let current = fresh_peer.db();
                assert_eq!(current.basis_t(), report.basis_t);
                let term = if kind == "text" {
                    "newtoken"
                } else {
                    "unique00000"
                };
                let found = current
                    .fulltext(TEXT, term, &FulltextOptions::default())
                    .unwrap();
                assert_eq!(found.hits.len(), 1);
                assert_eq!(found.hits[0].entity, entity);
                drop(found);
                drop(current);
                drop(fresh_peer);
                drop(report);
                let io = context.snapshot();
                calls = io.calls;
                sql_calls = io.sql_calls;
                result_cell_bytes = io.result_cell_bytes;
            }
            let elapsed = started.elapsed();
            eprintln!(
                "FULLTEXT_DELTA size={size} canonical_leaf_datoms={canonical_leaf_datoms} kind={kind} input_records={} output_records={} written_pages={} written_bytes={} spill_bytes={} sort_peak={} foreground_driver_calls={calls} foreground_sql_calls={sql_calls} result_cell_bytes={result_cell_bytes} transact_consolidate_reopen_search_check_drop_us={}",
                stats.input_records,
                stats.records,
                stats.blocks,
                stats.encoded_bytes,
                stats.spill_bytes,
                stats.peak_buffer_bytes,
                elapsed.as_micros(),
            );
            eprintln!(
                "FULLTEXT_DELTA_DETAIL size={size} canonical_leaf_datoms={canonical_leaf_datoms} kind={kind} maintenance={stats:?}"
            );
            assert!(
                stats.input_records <= if kind == "nontext" { 0 } else { 8 },
                "fixed {kind} change rebuilt the historical corpus: {stats:?}"
            );
            // Oracle/view reads use a fresh value outside the complete measured
            // operation above, which included release of its temporary reader.
            let check_peer = Connection::connect(url, &database, 128).unwrap();
            let current = check_peer.db();
            if kind == "nontext" {
                assert_eq!(stats.blocks, 0, "unchanged search content rewrote pages");
                assert_eq!(stats.spill_bytes, 0);
                let projection = current.native_fulltext_reader().unwrap().unwrap();
                assert_eq!(
                    projection.projection().root_hash,
                    initial_projection.root_hash
                );
            } else {
                assert!(
                    stats.blocks <= 32,
                    "fixed text edit rewrote too many pages: {stats:?}"
                );
                assert!(
                    current
                        .fulltext(TEXT, "unique00000", &FulltextOptions::default())
                        .unwrap()
                        .hits
                        .is_empty()
                );
                for view in [
                    held.clone(),
                    current.clone().history(),
                    current.clone().as_of(held.basis_t()),
                ] {
                    assert_eq!(
                        view.fulltext(TEXT, "unique00000", &FulltextOptions::default())
                            .unwrap()
                            .hits
                            .len(),
                        1
                    );
                }
            }
        }

        // Full rebuild is an independent maintenance oracle, outside delta timing.
        let before_peer = Connection::connect(url, &database, 128).unwrap();
        let before = before_peer.db();
        let expected = records(&before);
        assert_eq!(
            rebuilt_records(url, &database, &mut indexer, &before),
            expected
        );
        if size == 64 {
            // Input admission is per maintenance operation, not a newly imposed
            // maximum corpus size. A failed search build never rolls back an
            // acknowledged canonical transaction or publishes a partial header.
            indexer = indexer.with_fulltext_build_limits(FulltextBuildLimits {
                max_records: 1,
                ..Default::default()
            });
            let unchanged = client
                .transact(
                    TransactionRequest::new(
                        "limited-nontext",
                        vec![add(EntityRef::Id(entity), MARKER, Value::Long(2))],
                    ),
                    WAIT,
                )
                .unwrap();
            indexer.consolidate().unwrap();
            assert_eq!(indexer.fulltext_build_error(), None);
            assert_eq!(indexer.fulltext_build_stats().input_records, 0);
            let limited_request = TransactionRequest::new(
                "limited-text",
                vec![add(
                    EntityRef::Id(entity),
                    TEXT,
                    Value::String("limited admission violet".into()),
                )],
            );
            let accepted = client.transact(limited_request.clone(), WAIT).unwrap();
            indexer.consolidate().unwrap();
            assert!(indexer.fulltext_build_error().is_some());
            assert!(accepted.basis_t > unchanged.basis_t);
            let missing = Connection::connect(url, &database, 128).unwrap();
            assert_eq!(
                missing.db().native_fulltext_reader().err().unwrap().code,
                "fulltext/index-unavailable"
            );
            let replayed = client.transact(limited_request, WAIT).unwrap();
            assert!(replayed.replayed);
            assert_eq!(replayed.tx_hash, accepted.tx_hash);
            indexer = indexer.with_fulltext_build_limits(FulltextBuildLimits::default());
            indexer.rebuild_fulltext().unwrap().unwrap();
            let repaired = Connection::connect(url, &database, 128).unwrap();
            assert_eq!(
                repaired
                    .db()
                    .fulltext(TEXT, "violet", &FulltextOptions::default())
                    .unwrap()
                    .hits
                    .len(),
                1
            );
        }
        writer.shutdown();
        let restarted = common::start_service(url, &database);
        let replayed = restarted.client().transact(seed_request, WAIT).unwrap();
        assert!(replayed.replayed);
        assert_eq!(replayed.tx_hash, seed.tx_hash);
        assert_eq!(replayed.basis_t, seed.basis_t);
        assert_eq!(replayed.tempids, seed.tempids);
        restarted.shutdown();
    }
}

#[test]
fn incremental_search_records_match_rebuild_through_nohistory_retractions_and_schema_growth() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED incremental fulltext views PG: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "fulltext_incremental_views");
    let url = &fixture.connection;
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    for (no_history, size) in [(false, 128), (true, 64), (true, 1024)] {
        let database = format!("fulltext_views_{no_history}_{size}");
        let mut initial_schema = Schema::new();
        let mut attribute = Attribute::new(
            TEXT,
            Keyword::new("incremental", "text"),
            ValueType::String,
            Cardinality::One,
        )
        .fulltext();
        attribute.no_history = no_history;
        initial_schema.install(attribute).unwrap();
        PostgresStore::connect(url)
            .unwrap()
            .create_database(&database, initial_schema)
            .unwrap();
        let writer = common::start_service(url, &database);
        let client = writer.client();
        let seed = client
            .transact(
                TransactionRequest::new(
                    "seed",
                    (0..size)
                        .map(|n| {
                            add(
                                EntityRef::Temp(format!("d{n}")),
                                TEXT,
                                Value::String(format!("original cobalt item{n:04}")),
                            )
                        })
                        .collect(),
                ),
                WAIT,
            )
            .unwrap();
        let entity = seed.tempids["d0"];
        let mut indexer = PostgresIndexer::connect(url, &database)
            .unwrap()
            .with_fulltext_build_limits(FulltextBuildLimits {
                page_bytes: 1024,
                sort_memory_bytes: 8 * 1024,
                ..Default::default()
            });
        indexer.consolidate().unwrap();
        assert_eq!(indexer.fulltext_build_error(), None);
        let retained_peer = Connection::connect(url, &database, 128).unwrap();
        let retained = retained_peer.db();
        for (ordinal, operation) in [
            add(
                EntityRef::Id(entity),
                TEXT,
                Value::String("replacement amber target".into()),
            ),
            TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: TEXT,
                value: Some(Value::String("replacement amber target".into()).into()),
            },
            add(
                EntityRef::Id(entity),
                TEXT,
                Value::String("original cobalt item0000".into()),
            ),
        ]
        .into_iter()
        .enumerate()
        {
            let changed = client
                .transact(
                    TransactionRequest::new(format!("change{ordinal}"), vec![operation]),
                    WAIT,
                )
                .unwrap();
            indexer.consolidate().unwrap();
            assert_eq!(indexer.fulltext_build_error(), None);
            let stats = indexer.fulltext_build_stats();
            let peer = Connection::connect(url, &database, 128).unwrap();
            let current = peer.db();
            let actual = records(&current);
            let found = current
                .fulltext(TEXT, "item0000", &FulltextOptions::default())
                .unwrap();
            assert_eq!(found.hits.len(), usize::from(ordinal == 2));
            if ordinal == 2 {
                assert_eq!(found.hits[0].tx, t_to_tx(changed.basis_t).unwrap());
            }
            assert_eq!(
                retained
                    .fulltext(TEXT, "item0000", &FulltextOptions::default())
                    .unwrap()
                    .hits
                    .len(),
                1
            );
            assert!(
                current
                    .clone()
                    .filter(move |_, datom| datom.entity != entity)
                    .fulltext(TEXT, "item0000", &FulltextOptions::default())
                    .unwrap()
                    .hits
                    .is_empty()
            );
            eprintln!(
                "FULLTEXT_DELTA_VIEW no_history={no_history} size={size} operation={ordinal} input_records={} pages={} bytes={} independent_rebuild_agrees=true maintenance={stats:?}",
                stats.input_records, stats.blocks, stats.encoded_bytes,
            );
            assert!(
                stats.input_records <= 16,
                "fixed edit rebuilt a corpus: {stats:?}"
            );
            // Explicit repair invalidates this exact source's cached header.
            // Finish its reads first; the independent oracle opens a fresh peer.
            assert_eq!(
                rebuilt_records(url, &database, &mut indexer, &current),
                actual
            );
        }
        let peer = Connection::connect(url, &database, 128).unwrap();
        let new_attribute = u32::try_from(peer.db().eidx_frontier()).unwrap();
        // Use the established two-transaction native schema workflow. Resolving
        // a not-yet-installed numeric attribute in the same TxOp vector is not
        // an incremental-search operation and is not introduced by this repair.
        client
            .transact(
                TransactionRequest::new(
                    "new-search-attribute",
                    vec![TxOp::InstallAttribute(
                        Attribute::new(
                            new_attribute,
                            Keyword::new("incremental", "secondary"),
                            ValueType::String,
                            Cardinality::Many,
                        )
                        .fulltext(),
                    )],
                ),
                WAIT,
            )
            .unwrap();
        let installed = client
            .transact(
                TransactionRequest::new(
                    "new-search-value",
                    vec![add(
                        EntityRef::Id(entity),
                        new_attribute,
                        Value::String("secondary jasper".into()),
                    )],
                ),
                WAIT,
            )
            .unwrap();
        indexer.consolidate().unwrap();
        assert_eq!(indexer.fulltext_build_error(), None);
        let current_peer = Connection::connect(url, &database, 128).unwrap();
        let current = current_peer.db();
        let expected = records(&current);
        let hit = current
            .fulltext(new_attribute, "jasper", &FulltextOptions::default())
            .unwrap();
        assert_eq!(hit.hits.len(), 1);
        assert_eq!(hit.hits[0].entity, entity);
        assert_eq!(hit.hits[0].tx, t_to_tx(installed.basis_t).unwrap());
        assert_eq!(
            rebuilt_records(url, &database, &mut indexer, &current),
            expected
        );
        writer.shutdown();
    }
}
