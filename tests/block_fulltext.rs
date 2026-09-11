//! Fulltext uses the opaque object provider and the actual Rust index worker.
mod common;

use atomic_core::persistent_tree::{TreeConfig, build_tree};
use atomic_core::storage::fulltext::{build_for_descriptor, build_for_descriptor_with_control};
use atomic_core::storage::log::{LogEntry, LogRoot};
use atomic_core::storage::root::{Block, DatabaseValueRoot};
use atomic_core::storage::{
    BlockReader, CasOutcome, IndexDescriptor, IndexInput, PgBlockStore, SnapshotMetadata,
};
use atomic_core::*;
use std::collections::BTreeSet;
use std::time::Instant;

const TEXT: u32 = 1000;
const PRIVATE: u32 = 1001;
const NUMBER: u32 = 1002;
const IDENTITY: [u8; 16] = [71; 16];
const ROOT: &str = "tests/fulltext";

fn fixture(label: &str) -> Option<(common::PostgresFixture, PostgresConnectionConfig)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    Some((fixture, config))
}

fn schema() -> TxReport {
    let text = Attribute::new(
        TEXT,
        Keyword::new("document", "text"),
        ValueType::String,
        Cardinality::One,
    )
    .fulltext();
    let mut private = Attribute::new(
        PRIVATE,
        Keyword::new("document", "private"),
        ValueType::String,
        Cardinality::One,
    )
    .fulltext();
    private.no_history = true;
    Database::bootstrap()
        .unwrap()
        .with(
            &[
                TxOp::InstallAttribute(text),
                TxOp::InstallAttribute(private),
                TxOp::InstallAttribute(Attribute::new(
                    NUMBER,
                    Keyword::new("document", "number"),
                    ValueType::Long,
                    Cardinality::One,
                )),
            ],
            10,
        )
        .unwrap()
}
fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}
fn text(entity: EntityRef, attribute: u32, value: &str) -> TxOp {
    add(entity, attribute, Value::String(value.into()))
}
fn append(store: &mut PgBlockStore, log: &LogRoot, report: &TxReport) -> LogRoot {
    log.append(
        store,
        &LogEntry {
            basis_t: report.db_after.basis_t(),
            eidx_frontier: report.db_after.eidx_frontier(),
            reserved_frontier: 1003.min(report.db_after.eidx_frontier()),
            tx_data: report.tx_data.clone(),
        },
    )
    .unwrap()
}
fn initial_index(store: &mut PgBlockStore, db: &Database) -> IndexDescriptor {
    let mut trees = Vec::new();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let built = build_tree(
                order,
                history,
                db.datoms(
                    if history {
                        View::History
                    } else {
                        View::Current
                    },
                    order,
                ),
                &TreeConfig::default(),
            )
            .unwrap();
            for (id, bytes) in built.nodes.iter() {
                assert_eq!(store.put(bytes).unwrap(), *id);
            }
            trees.push(built.descriptor);
        }
    }
    let mut descriptor = IndexDescriptor {
        identity: IDENTITY,
        basis: db.basis_t(),
        generation: 0,
        trees,
        pending_avet: vec![],
        avet_work: vec![],
        fulltext: None,
    };
    descriptor.fulltext = build_for_descriptor(
        store,
        &descriptor,
        db.schema(),
        None,
        &FulltextBuildLimits::default(),
    )
    .unwrap()
    .map(|p| p.0);
    descriptor
}
fn publish(store: &mut PgBlockStore, db: &Database, log: &LogRoot, descriptor: &IndexDescriptor) {
    let metadata = SnapshotMetadata {
        identity: IDENTITY,
        basis: db.basis_t(),
        generation: 0,
        eidx_frontier: db.eidx_frontier(),
        reserved_frontier: 1003.min(db.eidx_frontier()),
        last_tx_instant: db.database_value().last_tx_instant().unwrap(),
        excision: None,
    };
    let metadata = store.put(&metadata.encode().unwrap()).unwrap();
    let indexes = store.put(&descriptor.encode().unwrap()).unwrap();
    let root = DatabaseValueRoot {
        identity: IDENTITY,
        basis: db.basis_t(),
        log: log.head(),
        indexes: Some(indexes),
        metadata: Some(metadata),
    };
    let id = store.put(&root.encode().unwrap()).unwrap();
    let revision = store.read_ref(ROOT).unwrap().map(|r| r.revision);
    assert!(matches!(
        store.compare_exchange(ROOT, revision, Some(&id)).unwrap(),
        CasOutcome::Applied(_)
    ));
}
fn prepare(
    store: &mut PgBlockStore,
    reader: &BlockReader,
) -> (IndexDescriptor, FulltextBuildStats) {
    let snapshot = reader.capture(ROOT).unwrap();
    let prepared = IndexInput::new(snapshot)
        .unwrap()
        .prepare(
            store,
            &TreeConfig {
                max_leaf_datoms: 64,
                target_leaf_bytes: 8 * 1024,
                ..Default::default()
            },
        )
        .unwrap();
    (
        prepared.descriptor().clone(),
        prepared.fulltext_stats().unwrap(),
    )
}
fn ids(db: &DatabaseValue, attribute: u32, query: &str) -> BTreeSet<u64> {
    db.fulltext(attribute, query, &FulltextOptions::default())
        .unwrap()
        .hits
        .into_iter()
        .map(|h| h.entity)
        .collect()
}

#[test]
fn block_fulltext_is_selective_incremental_temporal_and_no_history_safe() {
    let Some((_fixture, config)) = fixture("block_fulltext_views") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reader = BlockReader::connect(&config, Default::default()).unwrap();
    let initial = schema();
    let mut log = append(&mut store, &LogRoot::empty(), &initial);
    let base = initial_index(&mut store, &initial.db_after);
    publish(&mut store, &initial.db_after, &log, &base);
    assert!(
        ids(
            &reader.capture(ROOT).unwrap().database_value(),
            TEXT,
            "absent"
        )
        .is_empty()
    );
    let mut ops = vec![
        text(EntityRef::Temp("a".into()), TEXT, "Jane's blue river"),
        text(EntityRef::Temp("b".into()), TEXT, "Jane blue mountain"),
        text(EntityRef::Temp("a".into()), PRIVATE, "hiddenold"),
    ];
    for n in 0..512 {
        ops.push(text(
            EntityRef::Temp(format!("doc{n}")),
            TEXT,
            &format!("Archive document token{n:04} ordinary material"),
        ));
    }
    let seeded = initial.db_after.with(&ops, 20).unwrap();
    log = append(&mut store, &log, &seeded);
    publish(&mut store, &seeded.db_after, &log, &base);
    let recent = reader.capture(ROOT).unwrap().database_value();
    let (a, b) = (seeded.tempids["a"], seeded.tempids["b"]);
    assert_eq!(ids(&recent, TEXT, "jane"), BTreeSet::from([a, b]));
    assert_eq!(
        ids(&recent, TEXT, "token0317"),
        BTreeSet::from([seeded.tempids["doc317"]])
    );
    let (indexed, build) = prepare(&mut store, &reader);
    assert_eq!(build.documents_added, 515);
    assert_eq!(build.empty_corpus_bulk_builds, 1);
    publish(&mut store, &seeded.db_after, &log, &indexed);
    let old = reader.capture(ROOT).unwrap().database_value();
    let context = OperationContext::new(OperationKind::Query);
    let start = Instant::now();
    let cold = {
        let _guard = context.enter();
        old.fulltext(TEXT, "token0317", &Default::default())
            .unwrap()
    };
    let cold_us = start.elapsed().as_micros();
    assert_eq!(cold.hits.len(), 1);
    assert_eq!(cold.hits[0].entity, seeded.tempids["doc317"]);
    let projection = old
        .native_fulltext_reader()
        .unwrap()
        .unwrap()
        .projection()
        .clone();
    assert!(cold.stats.read_bytes > 0 && cold.stats.read_bytes < projection.encoded_bytes);
    let warm = OperationContext::new(OperationKind::Query);
    {
        let _guard = warm.enter();
        assert_eq!(
            ids(&old, TEXT, "token0317"),
            BTreeSet::from([seeded.tempids["doc317"]])
        );
    }
    assert_eq!(warm.snapshot().sql_calls, 0);
    // Include a fresh reader's capture, query and result consumption. Connection
    // destructor cleanup is not part of this synchronous measurement.
    let whole = OperationContext::new(OperationKind::Query);
    let begin = Instant::now();
    let complete_bytes = {
        let _guard = whole.enter();
        let cold_reader = BlockReader::connect(&config, Default::default()).unwrap();
        let captured = cold_reader.capture(ROOT).unwrap();
        let result = captured
            .database_value()
            .fulltext(TEXT, "token0317", &Default::default())
            .unwrap();
        assert_eq!(result.hits.len(), 1);
        assert_eq!(result.hits[0].entity, seeded.tempids["doc317"]);
        let bytes = result.stats.read_bytes;
        drop(result);
        drop(captured);
        bytes
    };
    assert!(complete_bytes > 0 && complete_bytes < projection.encoded_bytes);
    eprintln!(
        "BLOCK_FULLTEXT_CAPTURE_READ elapsed_us={} sql_calls={} canonical_read_bytes={} search_bytes={complete_bytes} corpus_bytes={} deferred_connection_cleanup_excluded=true",
        begin.elapsed().as_micros(),
        whole.snapshot().sql_calls,
        whole.snapshot().known_payload_read_bytes,
        projection.encoded_bytes
    );
    eprintln!(
        "BLOCK_FULLTEXT docs=515 cold_query_us={cold_us} complete_sql={} read_bytes={} corpus_bytes={} build={build:?}",
        context.snapshot().sql_calls,
        cold.stats.read_bytes,
        projection.encoded_bytes
    );
    assert_eq!(ids(&old, TEXT, "JANE"), BTreeSet::from([a, b]));
    assert_eq!(ids(&old, TEXT, "\"jane blue river\""), BTreeSet::from([a]));
    assert_eq!(
        ids(&old, TEXT, "jan* AND NOT mountain"),
        BTreeSet::from([a])
    );
    let changes = [
        text(EntityRef::Id(a), TEXT, "Juliet green meadow"),
        text(EntityRef::Id(a), PRIVATE, "hiddennew"),
    ];
    let preview = old.with(&changes, 30).unwrap().db_after;
    assert_eq!(ids(&preview, TEXT, "juliet"), BTreeSet::from([a]));
    assert_eq!(ids(&preview, TEXT, "jane"), BTreeSet::from([b]));
    let changed = seeded.db_after.with(&changes, 30).unwrap();
    log = append(&mut store, &log, &changed);
    publish(&mut store, &changed.db_after, &log, &indexed);
    let lagging = reader.capture(ROOT).unwrap().database_value();
    assert_eq!(ids(&lagging, TEXT, "jane"), BTreeSet::from([b]));
    assert_eq!(ids(&lagging, TEXT, "juliet"), BTreeSet::from([a]));
    assert_eq!(
        ids(&lagging.clone().as_of(old.basis_t()), TEXT, "jane"),
        BTreeSet::from([a, b])
    );
    assert_eq!(
        ids(&lagging.clone().history(), TEXT, "jane"),
        BTreeSet::from([a, b])
    );
    assert!(ids(&lagging.clone().since(old.basis_t()), TEXT, "jane").is_empty());
    assert_eq!(ids(&lagging, PRIVATE, "hiddennew"), BTreeSet::from([a]));
    assert!(ids(&lagging, PRIVATE, "hiddenold").is_empty());
    let local = lagging
        .with(&[text(EntityRef::Id(b), TEXT, "Cobalt cliffs")], 40)
        .unwrap()
        .db_after;
    assert_eq!(ids(&local, TEXT, "juliet"), BTreeSet::from([a]));
    assert_eq!(ids(&local, TEXT, "cobalt"), BTreeSet::from([b]));
    assert!(ids(&local, TEXT, "jane").is_empty());
    assert_eq!(ids(&recent, TEXT, "jane"), BTreeSet::from([a, b]));
    let work = OperationContext::new(OperationKind::Query);
    let started = Instant::now();
    let (updated, delta) = {
        let _guard = work.enter();
        prepare(&mut store, &reader)
    };
    assert_eq!(delta.documents_added, 2);
    assert_eq!(
        delta.documents_removed, 1,
        "noHistory physically removes its old assertion"
    );
    assert!(
        delta.source_datoms_examined < 256,
        "unchanged corpus was decoded: {delta:?}"
    );
    assert!(delta.input_records < 20);
    eprintln!(
        "BLOCK_FULLTEXT_INCREMENTAL elapsed_us={} complete_sql={} {delta:?}",
        started.elapsed().as_micros(),
        work.snapshot().sql_calls
    );
    publish(&mut store, &changed.db_after, &log, &updated);
    let current = reader.capture(ROOT).unwrap().database_value();
    assert_eq!(ids(&current, TEXT, "juliet"), BTreeSet::from([a]));
    assert_eq!(
        ids(&current.clone().as_of(old.basis_t()), TEXT, "jane"),
        BTreeSet::from([a, b])
    );
    assert_eq!(
        ids(&current.clone().history(), TEXT, "jane"),
        BTreeSet::from([a, b])
    );
    assert!(ids(&current.clone().since(old.basis_t()), TEXT, "jane").is_empty());
    assert!(
        ids(
            &current.clone().history().filter(|_, d| !d.added),
            TEXT,
            "jane"
        )
        .is_empty()
    );
    assert_eq!(ids(&old, PRIVATE, "hiddenold"), BTreeSet::from([a]));
    assert!(ids(&current.clone().history(), PRIVATE, "hiddenold").is_empty());
    assert_eq!(ids(&current, PRIVATE, "hiddennew"), BTreeSet::from([a]));
    // A nontext transaction does not tokenize or rewrite the existing corpus.
    let nontext = changed
        .db_after
        .with(&[add(EntityRef::Id(a), NUMBER, Value::Long(9))], 40)
        .unwrap();
    log = append(&mut store, &log, &nontext);
    publish(&mut store, &nontext.db_after, &log, &updated);
    let (_, reused) = prepare(&mut store, &reader);
    assert_eq!(reused.input_records, 0);
    assert_eq!(reused.tokenized_bytes, 0);
    assert_eq!(reused.reused_projections, 1);
}

#[test]
fn fulltext_installed_in_recent_tail_is_searchable_without_scanning_unrelated_facts() {
    let Some((_fixture, config)) = fixture("block_fulltext_recent_schema") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reader = BlockReader::connect(&config, Default::default()).unwrap();
    let initial = Database::bootstrap().unwrap().with(&[], 10).unwrap();
    let mut log = append(&mut store, &LogRoot::empty(), &initial);
    let base = initial_index(&mut store, &initial.db_after);
    assert!(base.fulltext.is_none());
    let installed = initial
        .db_after
        .with(
            &[
                TxOp::InstallAttribute(
                    Attribute::new(
                        TEXT,
                        Keyword::new("document", "text"),
                        ValueType::String,
                        Cardinality::One,
                    )
                    .fulltext(),
                ),
                TxOp::InstallAttribute(Attribute::new(
                    PRIVATE,
                    Keyword::new("document", "private"),
                    ValueType::String,
                    Cardinality::One,
                )),
                TxOp::InstallAttribute(Attribute::new(
                    NUMBER,
                    Keyword::new("document", "number"),
                    ValueType::Long,
                    Cardinality::One,
                )),
            ],
            20,
        )
        .unwrap();
    log = append(&mut store, &log, &installed);
    let seeded = installed
        .db_after
        .with(
            &[text(
                EntityRef::Temp("document".into()),
                TEXT,
                "Violet river",
            )],
            30,
        )
        .unwrap();
    log = append(&mut store, &log, &seeded);
    publish(&mut store, &seeded.db_after, &log, &base);
    let before = reader.capture(ROOT).unwrap().database_value();
    let found = before
        .fulltext(TEXT, "violet", &Default::default())
        .unwrap();
    assert_eq!(found.hits.len(), 1);
    assert_eq!(found.hits[0].entity, seeded.tempids["document"]);
    assert_eq!(
        found.hits[0].tx,
        t_to_tx(seeded.db_after.basis_t()).unwrap()
    );
    assert_eq!(found.stats.index_basis_t, base.basis);
    assert_eq!(found.stats.read_bytes, 0);

    let unrelated = seeded
        .db_after
        .with(
            &(0..512)
                .map(|n| {
                    add(
                        EntityRef::Temp(format!("number{n}")),
                        NUMBER,
                        Value::Long(n),
                    )
                })
                .collect::<Vec<_>>(),
            40,
        )
        .unwrap();
    log = append(&mut store, &log, &unrelated);
    publish(&mut store, &unrelated.db_after, &log, &base);
    let after = reader.capture(ROOT).unwrap().database_value();
    let same = after.fulltext(TEXT, "violet", &Default::default()).unwrap();
    assert_eq!(same.hits, found.hits);
    assert_eq!(same.stats.work, found.stats.work);
    assert_eq!(same.stats.admitted_bytes, found.stats.admitted_bytes);
    for options in [
        FulltextOptions {
            max_work: found.stats.work as usize - 1,
            ..Default::default()
        },
        FulltextOptions {
            max_bytes: found.stats.admitted_bytes as usize - 1,
            ..Default::default()
        },
    ] {
        assert!(after.fulltext(TEXT, "violet", &options).is_err());
    }
    let retracted = unrelated
        .db_after
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(seeded.tempids["document"]),
                attribute: TEXT,
                value: None,
            }],
            50,
        )
        .unwrap();
    log = append(&mut store, &log, &retracted);
    publish(&mut store, &retracted.db_after, &log, &base);
    let current = reader.capture(ROOT).unwrap().database_value();
    assert!(ids(&current, TEXT, "violet").is_empty());
    for retained in [
        before,
        current.clone().as_of(seeded.db_after.basis_t()),
        current.history(),
    ] {
        assert_eq!(
            ids(&retained, TEXT, "violet"),
            BTreeSet::from([seeded.tempids["document"]])
        );
    }
}

#[test]
fn block_fulltext_authenticates_attachment_binding_pages_and_cancellation() {
    let Some((_fixture, config)) = fixture("block_fulltext_auth") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reader = BlockReader::connect(&config, Default::default()).unwrap();
    let initial = schema();
    let log = append(&mut store, &LogRoot::empty(), &initial);
    let descriptor = initial_index(&mut store, &initial.db_after);
    let attachment = descriptor.fulltext.unwrap();
    let original = Block::decode(&attachment, &store.get(attachment).unwrap().unwrap()).unwrap();
    publish(&mut store, &initial.db_after, &log, &descriptor);
    assert!(
        ids(
            &reader.capture(ROOT).unwrap().database_value(),
            TEXT,
            "absent"
        )
        .is_empty()
    );
    // Content hashes are valid: the engine must reject the semantic binding.
    for (offset, replacement, code) in [
        (16 + 8, 99u64, "fulltext/analyzer-version"),
        (16 + 44, 9u64, "fulltext/source-binding"),
        (16 + 92, 99u64, "fulltext/root-count"),
    ] {
        let mut block = original.clone();
        if offset == 24 {
            block.payload[offset..offset + 4].copy_from_slice(&(replacement as u32).to_be_bytes());
        } else {
            block.payload[offset..offset + 8].copy_from_slice(&replacement.to_be_bytes());
        }
        let mut changed = descriptor.clone();
        changed.fulltext = Some(store.put(&block.encode().unwrap()).unwrap());
        publish(&mut store, &initial.db_after, &log, &changed);
        assert_eq!(
            reader
                .capture(ROOT)
                .unwrap()
                .database_value()
                .fulltext(TEXT, "absent", &Default::default())
                .unwrap_err()
                .code,
            code
        );
    }
    let page_id = original.links[1];
    let mut page = Block::decode(&page_id, &store.get(page_id).unwrap().unwrap()).unwrap();
    page.links.push([29; 32]); // Valid hash, but a leaf has no outgoing children.
    let bad_page = store.put(&page.encode().unwrap()).unwrap();
    let mut broken = original.clone();
    broken.links[1] = bad_page;
    broken.payload[16 + 60..16 + 92].copy_from_slice(&bad_page);
    let mut changed = descriptor.clone();
    changed.fulltext = Some(store.put(&broken.encode().unwrap()).unwrap());
    publish(&mut store, &initial.db_after, &log, &changed);
    assert_eq!(
        reader
            .capture(ROOT)
            .unwrap()
            .database_value()
            .fulltext(TEXT, "absent", &Default::default())
            .unwrap_err()
            .code,
        "fulltext/page-links"
    );
    publish(&mut store, &initial.db_after, &log, &descriptor);
    let fresh = BlockReader::connect(&config, Default::default()).unwrap();
    let missing_page = original.links[1];
    assert_eq!(store.remove_objects(&[missing_page]).unwrap(), 1);
    assert_eq!(
        fresh
            .capture(ROOT)
            .unwrap()
            .database_value()
            .fulltext(TEXT, "absent", &Default::default())
            .unwrap_err()
            .code,
        "storage/missing-object"
    );
    let mut controls = 0;
    let error = build_for_descriptor_with_control(
        &mut store,
        &descriptor,
        initial.db_after.schema(),
        None,
        &Default::default(),
        &mut || {
            controls += 1;
            if controls >= 3 {
                Err(SemanticError::new(
                    ErrorCategory::Busy,
                    "test/cancel",
                    "test cancellation",
                ))
            } else {
                Ok(())
            }
        },
    )
    .unwrap_err();
    assert_eq!(error.code, "test/cancel");
    assert_eq!(controls, 3);
}
