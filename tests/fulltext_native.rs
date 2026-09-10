mod common;
use atomic_core::*;
use std::collections::BTreeSet;
use std::sync::{Arc, atomic::AtomicBool};
use std::time::{Duration, Instant};

const TEXT: u32 = 1000;
fn add(entity: EntityRef, text: &str) -> TxOp {
    TxOp::Add {
        entity,
        attribute: TEXT,
        value: Value::String(text.into()).into(),
    }
}
fn find(db: &DatabaseValue, term: &str) -> FulltextReport {
    db.fulltext(TEXT, term, &FulltextOptions::default())
        .unwrap()
}
fn ids(report: &FulltextReport) -> BTreeSet<u64> {
    report.hits.iter().map(|hit| hit.entity).collect()
}

#[test]
fn indexed_native_search_is_view_safe_lag_visible_and_selective() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED native fulltext: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "fulltext_native");
    let url = &fixture.connection;
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                TEXT,
                Keyword::new("document", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let created = PostgresStore::connect(url)
        .unwrap()
        .create_database("search", schema)
        .unwrap();
    let writer = common::start_service(url, "search");
    let mut ops = vec![
        add(EntityRef::Temp("a".into()), "Jane's blue river"),
        add(EntityRef::Temp("b".into()), "Jane blue mountain"),
    ];
    for n in 0..400 {
        ops.push(add(
            EntityRef::Temp(format!("doc{n}")),
            &format!("Archive document token{n:04} ordinary material"),
        ));
    }
    let seeded = common::transact(&writer, "seed", created.basis_t(), &ops, 1000);
    let a = seeded.tempids["a"];
    let b = seeded.tempids["b"];
    let mut indexer = PostgresIndexer::connect(url, "search")
        .unwrap()
        .with_fulltext_build_limits(FulltextBuildLimits {
            sort_memory_bytes: 16 * 1024,
            page_bytes: 2048,
            ..Default::default()
        });
    indexer.consolidate().unwrap();
    assert!(indexer.fulltext_build_error().is_none());
    let build = indexer.fulltext_build_stats();
    let peer = Connection::connect(url, "search", 512).unwrap();
    let old = peer.db();
    assert_eq!(old.basis_t(), seeded.basis_t);
    let context = OperationContext::new(OperationKind::Query);
    let cold_start = Instant::now();
    let cold = {
        let _guard = context.enter();
        find(&old, "token0317")
    };
    let cold_us = cold_start.elapsed().as_micros();
    let cold_sql = context.snapshot().sql_calls;
    assert_eq!(ids(&cold), BTreeSet::from([seeded.tempids["doc317"]]));
    assert!(cold.stats.read_bytes > 0);
    let projection = old
        .native_fulltext_reader()
        .unwrap()
        .unwrap()
        .projection()
        .clone();
    assert!(
        cold.stats.read_bytes < projection.encoded_bytes,
        "selective query read entire search corpus"
    );
    let warm_context = OperationContext::new(OperationKind::Query);
    let warm_start = Instant::now();
    let warm = {
        let _guard = warm_context.enter();
        find(&old, "token0317")
    };
    let warm_us = warm_start.elapsed().as_micros();
    assert_eq!(warm.hits, cold.hits);
    assert_eq!(
        warm_context.snapshot().sql_calls,
        0,
        "warm search includes all attributed SQL"
    );
    assert_eq!(warm.stats.read_bytes, 0);
    assert!(warm.stats.admitted_bytes > 0);
    let cache = old.native_fulltext_reader().unwrap().unwrap().cache_stats();
    assert!(cache.entries > 0 && cache.entries <= cache.max_entries);
    assert!(cache.retained_bytes > 0 && cache.retained_bytes <= cache.max_bytes);
    assert_eq!(ids(&find(&old, "JANE")), BTreeSet::from([a, b]));
    assert_eq!(ids(&find(&old, "\"jane blue river\"")), BTreeSet::from([a]));
    assert_eq!(
        ids(&find(&old, "jan* AND NOT mountain")),
        BTreeSet::from([a])
    );
    // Warming cannot evade cumulative byte budgets, cancellation or deadlines.
    for options in [
        FulltextOptions {
            max_bytes: 128,
            ..Default::default()
        },
        FulltextOptions {
            max_work: 2,
            ..Default::default()
        },
        FulltextOptions {
            timeout: Some(Duration::ZERO),
            ..Default::default()
        },
        FulltextOptions {
            cancel: Arc::new(AtomicBool::new(true)),
            ..Default::default()
        },
    ] {
        assert!(old.fulltext(TEXT, "token0317", &options).is_err());
    }
    let stopword = find(&old, "the");
    assert!(stopword.hits.is_empty());
    assert!(stopword.stats.admitted_bytes > 0);

    let changes = [add(EntityRef::Id(a), "Juliet green meadow")];
    let preview = old.with(&changes, 2000).unwrap().db_after;
    assert_eq!(ids(&find(&preview, "juliet")), BTreeSet::from([a]));
    assert_eq!(ids(&find(&preview, "jane")), BTreeSet::from([b]));
    let changed = common::transact(&writer, "change", seeded.basis_t, &changes, 2000);
    let lagging_peer = Connection::connect(url, "search", 512).unwrap();
    let lagging = lagging_peer.db();
    assert_eq!(lagging.basis_t(), changed.basis_t);
    assert_eq!(
        ids(&find(&lagging, "jane")),
        BTreeSet::from([b]),
        "stale candidates must not return retracted values"
    );
    let not_yet = find(&lagging, "juliet");
    assert!(not_yet.hits.is_empty());
    assert!(not_yet.stats.index_basis_t < lagging.basis_t());
    indexer.consolidate().unwrap();
    assert!(indexer.fulltext_build_error().is_none());
    let current_peer = Connection::connect(url, "search", 512).unwrap();
    let current = current_peer.db();
    assert_eq!(ids(&find(&current, "juliet")), BTreeSet::from([a]));
    assert_eq!(
        ids(&find(&current.clone().as_of(old.basis_t()), "jane")),
        BTreeSet::from([a, b])
    );
    assert_eq!(
        ids(&find(&current.clone().history(), "jane")),
        BTreeSet::from([a, b])
    );
    assert!(
        find(&current.clone().since(old.basis_t()), "jane")
            .hits
            .is_empty()
    );
    assert!(
        find(&current.clone().history().filter(|_, d| !d.added), "jane")
            .hits
            .is_empty()
    );
    assert_eq!(
        ids(&find(
            &old.clone().filter(move |_, d| d.entity != a),
            "jane"
        )),
        BTreeSet::from([b])
    );
    assert_eq!(ids(&find(&old, "jane")), BTreeSet::from([a, b]));
    // The same original string reasserted after a retraction must return the
    // transaction of the supplied value, not the candidate's older assertion.
    let reasserted = common::transact(
        &writer,
        "reassert",
        changed.basis_t,
        &[add(EntityRef::Id(a), "Jane's blue river")],
        3000,
    );
    let reasserted_peer = Connection::connect(url, "search", 512).unwrap();
    let reasserted_db = reasserted_peer.db();
    let report = find(&reasserted_db, "river");
    assert_eq!(report.hits.len(), 1);
    assert_eq!(report.hits[0].tx, t_to_tx(reasserted.basis_t).unwrap());
    assert!(report.stats.index_basis_t < reasserted_db.basis_t());
    writer.shutdown();
    assert_eq!(
        find(&old, "token0317").hits,
        cold.hits,
        "no transactor query dependency"
    );
    println!(
        "FULLTEXT_NATIVE documents=402 source_records={} source_blocks={} source_bytes={} sort_peak_bytes={} spill_bytes={} cold_sql={} cold_search_bytes={} cold_us={} warm_sql=0 warm_search_bytes=0 warm_us={} cumulative_admitted_bytes={} exact_view=true lag_visible=true reassert_tx_exact=true",
        projection.record_count,
        projection.block_count,
        projection.encoded_bytes,
        build.peak_buffer_bytes,
        build.spill_bytes,
        cold_sql,
        cold.stats.read_bytes,
        cold_us,
        warm_us,
        warm.stats.admitted_bytes
    );
    println!(
        "FULLTEXT_CACHE entries={} retained_bytes={} max_entries={} max_bytes={} includes_positive_headers_and_decoded_pages=true not_process_rss=true",
        cache.entries, cache.retained_bytes, cache.max_entries, cache.max_bytes
    );
}
