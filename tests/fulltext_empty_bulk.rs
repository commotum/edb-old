mod common;

use atomic_core::*;
use std::time::Duration;

const TEXT: u32 = 1000;
const OTHER_TEXT: u32 = 1001;
const MARKER: u32 = 1002;

fn records(value: &DatabaseValue) -> Vec<FulltextRecord> {
    value
        .native_fulltext_reader()
        .unwrap()
        .unwrap()
        .prefix(b"", FulltextReadLimits::default())
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap()
}

#[test]
fn empty_corpus_bulk_loading_preserves_delta_admission_and_empty_documents() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED fulltext empty bulk PG: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "fulltext_empty_bulk");
    let url = &fixture.connection;
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    for empty_document in [false, true] {
        let database = format!("empty_bulk_{empty_document}");
        let mut schema = Schema::new();
        for (id, name) in [(TEXT, "text"), (OTHER_TEXT, "other")] {
            schema
                .install(
                    Attribute::new(
                        id,
                        Keyword::new("empty_bulk", name),
                        ValueType::String,
                        Cardinality::One,
                    )
                    .fulltext(),
                )
                .unwrap();
        }
        schema
            .install(Attribute::new(
                MARKER,
                Keyword::new("empty_bulk", "marker"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        PostgresStore::connect(url)
            .unwrap()
            .create_database(&database, schema)
            .unwrap();
        let writer = common::start_service(url, &database);
        let client = writer.client();
        let mut indexer = PostgresIndexer::connect(url, &database).unwrap();
        client
            .transact(
                TransactionRequest::new(
                    "empty-nontext",
                    vec![TxOp::Add {
                        entity: EntityRef::Temp("marker".into()),
                        attribute: MARKER,
                        value: Value::Long(1).into(),
                    }],
                ),
                Duration::from_secs(30),
            )
            .unwrap();
        indexer.consolidate().unwrap();
        assert_eq!(indexer.fulltext_build_error(), None);
        let unchanged = indexer.fulltext_build_stats();
        assert_eq!(unchanged.input_records, 0);
        assert_eq!(unchanged.blocks, 0);
        assert_eq!(unchanged.empty_corpus_bulk_builds, 0);
        assert_eq!(unchanged.reused_projections, 1);
        if empty_document {
            client
                .transact(
                    TransactionRequest::new(
                        "empty-document",
                        vec![TxOp::Add {
                            entity: EntityRef::Temp("empty".into()),
                            attribute: TEXT,
                            value: Value::String(String::new()).into(),
                        }],
                    ),
                    Duration::from_secs(30),
                )
                .unwrap();
            indexer.consolidate().unwrap();
        }
        let held_peer = Connection::connect(url, &database, 32).unwrap();
        let held = held_peer.db();
        let before = records(&held);
        assert_eq!(before.len(), 2 + usize::from(empty_document));
        indexer = indexer.with_fulltext_build_limits(FulltextBuildLimits {
            max_records: 5,
            ..Default::default()
        });
        let report = client
            .transact(
                TransactionRequest::new(
                    "first-searchable-document",
                    vec![TxOp::Add {
                        entity: EntityRef::Temp("text".into()),
                        attribute: TEXT,
                        value: Value::String("quartz amber cobalt".into()).into(),
                    }],
                ),
                Duration::from_secs(30),
            )
            .unwrap();
        indexer.consolidate().unwrap();
        assert_eq!(indexer.fulltext_build_error(), None);
        let stats = indexer.fulltext_build_stats();
        // One document, three postings and the changed attribute's statistics
        // are admitted. The untouched zero-stat attribute is retained without
        // charging it as new input, although a full target build has six records.
        assert_eq!(stats.input_records, 5);
        assert_eq!(stats.documents_added, 1);
        assert_eq!(stats.empty_corpus_bulk_builds, u64::from(!empty_document));
        let peer = Connection::connect(url, &database, 32).unwrap();
        let current = peer.db();
        assert_eq!(current.basis_t(), report.basis_t);
        let expected = records(&current);
        assert_eq!(expected.len(), 6 + usize::from(empty_document));
        assert_eq!(records(&held), before);
        let hits = current
            .fulltext(TEXT, "quartz", &FulltextOptions::default())
            .unwrap();
        assert_eq!(hits.hits.len(), 1);
        assert_eq!(hits.hits[0].entity, report.tempids["text"]);
        let source = current
            .native_fulltext_reader()
            .unwrap()
            .unwrap()
            .projection()
            .source_manifest;
        let mut store = FulltextStore::connect(&PostgresConnectionConfig::plaintext(url)).unwrap();
        while !store.discard_projection(source, 4096).unwrap() {}
        indexer = indexer.with_fulltext_build_limits(FulltextBuildLimits::default());
        indexer.rebuild_fulltext().unwrap().unwrap();
        let rebuilt = Connection::connect(url, &database, 32).unwrap();
        assert_eq!(records(&rebuilt.db()), expected);
        eprintln!("FULLTEXT_EMPTY_BULK empty_document={empty_document} stats={stats:?}");
        writer.shutdown();
    }
}
