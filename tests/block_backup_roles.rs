//! A backup reads source objects and prepares its covering indexes only in files.
mod common;

use atomic_core::storage::ownership::{BlockCollector, CollectionPhase, CollectionStats};
use atomic_core::storage::root::{Block, DatabaseRoot};
use atomic_core::storage::{
    BlockDatabase, BlockTransactor, BlockWriterOptions, IndexDescriptor, PgBlockStore,
};
use atomic_core::*;
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const SCORE: u32 = 1000;
const TEXT: u32 = 1001;

fn quote(value: &str) -> String {
    format!("\"{}\"", value.replace('"', "\"\""))
}

struct ReaderRole {
    admin: Client,
    schema: String,
    name: String,
}
impl Drop for ReaderRole {
    fn drop(&mut self) {
        let schema = quote(&self.schema);
        let role = quote(&self.name);
        let _ = self.admin.batch_execute(&format!(
            "REVOKE SELECT ON {schema}.atomic_objects FROM {role}; \
             REVOKE SELECT,INSERT,UPDATE,DELETE ON {schema}.atomic_refs FROM {role}; \
             REVOKE USAGE ON SCHEMA {schema} FROM {role}; DROP ROLE {role}"
        ));
    }
}

fn no_reader_pins(store: &mut PgBlockStore) {
    let start = Instant::now();
    while !store
        .list_live_refs("pins/read/", None, 128)
        .unwrap()
        .is_empty()
    {
        assert!(
            start.elapsed() < Duration::from_secs(10),
            "reader cleanup did not finish"
        );
        std::thread::sleep(Duration::from_millis(5));
    }
}

fn collect(config: &PostgresConnectionConfig) -> (usize, CollectionStats) {
    let mut collector = BlockCollector::connect(config).unwrap();
    let mut total = CollectionStats::default();
    for batches in 1..=128 {
        let progress = collector.advance(Duration::ZERO, 512).unwrap();
        // The API reports work for this advance, not cumulative cycle work.
        let stats = progress.stats;
        total.ownership_steps += stats.ownership_steps;
        total.newly_owned_objects += stats.newly_owned_objects;
        total.ownership_payload_bytes += stats.ownership_payload_bytes;
        total.metadata_objects_protected += stats.metadata_objects_protected;
        total.objects_examined += stats.objects_examined;
        total.objects_removed += stats.objects_removed;
        total.stored_bytes_removed += stats.stored_bytes_removed;
        total.events_removed += stats.events_removed;
        total.sessions_revoked += stats.sessions_revoked;
        total.pins_reaped += stats.pins_reaped;
        if progress.phase == CollectionPhase::Complete {
            return (batches, total);
        }
    }
    panic!("bounded fixture collection did not finish");
}

fn objects(admin: &mut Client) -> i64 {
    admin
        .query_one("SELECT count(*) FROM atomic_objects", &[])
        .unwrap()
        .get(0)
}

#[test]
fn readonly_capture_prepares_file_indexes_and_survives_source_retirement_and_collection() {
    let Ok(base) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block backup roles: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let mut permission = Client::connect(&base, NoTls).unwrap();
    let create_role: bool = permission
        .query_one(
            "SELECT rolsuper OR rolcreaterole FROM pg_roles WHERE rolname=current_user",
            &[],
        )
        .unwrap()
        .get(0);
    if !create_role {
        eprintln!("SKIP block backup roles: CREATE ROLE unavailable");
        return;
    }
    let complete = Instant::now();
    let fixture = common::PostgresFixture::new(&base, "block_backup_roles");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let role_name = format!(
        "backup_reader_{}_{:x}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    let quoted_role = quote(&role_name);
    permission
        .batch_execute(&format!(
            "CREATE ROLE {quoted_role} LOGIN PASSWORD '{role_name}'"
        ))
        .unwrap();
    let role = ReaderRole {
        admin: permission,
        schema: fixture.schema.clone(),
        name: role_name.clone(),
    };
    let mut admin = Client::connect(&fixture.connection, NoTls).unwrap();
    admin
        .batch_execute(&format!(
            "GRANT USAGE ON SCHEMA {} TO {quoted_role}; \
         GRANT SELECT ON atomic_objects TO {quoted_role}; \
         GRANT SELECT,INSERT,UPDATE,DELETE ON atomic_refs TO {quoted_role}",
            quote(&fixture.schema)
        ))
        .unwrap();
    let parameters = if base.starts_with("postgres://") || base.starts_with("postgresql://") {
        format!(
            "{base}{}user={role_name}&password={role_name}&options=-csearch_path%3D{}%2Cpg_catalog",
            if base.contains('?') { '&' } else { '?' },
            fixture.schema
        )
    } else {
        format!(
            "{base} user={role_name} password={role_name} options='-csearch_path={},pg_catalog'",
            fixture.schema
        )
    };
    let readonly = PostgresConnectionConfig::plaintext(parameters);
    let mut probe = readonly.connect().unwrap();
    assert_eq!(
        probe
            .query_one("SELECT current_user::text", &[])
            .unwrap()
            .get::<_, String>(0),
        role_name
    );
    for operation in [
        "INSERT INTO atomic_objects SELECT * FROM atomic_objects WHERE false",
        "UPDATE atomic_objects SET payload=payload WHERE false",
        "DELETE FROM atomic_objects WHERE false",
    ] {
        assert_eq!(
            probe
                .execute(operation, &[])
                .unwrap_err()
                .code()
                .unwrap()
                .code(),
            "42501"
        );
    }
    drop(probe);

    let mut schema = Schema::new();
    let mut score = Attribute::new(
        SCORE,
        Keyword::new("item", "score"),
        ValueType::Long,
        Cardinality::One,
    );
    score.indexed = true;
    schema.install(score).unwrap();
    schema
        .install(
            Attribute::new(
                TEXT,
                Keyword::new("item", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let database = BlockDatabase::create(&config, "source", schema).unwrap();
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    let mut operations = Vec::new();
    for index in 0..48 {
        operations.push(TxOp::Add {
            entity: EntityRef::Temp(format!("e{index}")),
            attribute: SCORE,
            value: Value::Long(index).into(),
        });
        operations.push(TxOp::Add {
            entity: EntityRef::Temp(format!("e{index}")),
            attribute: TEXT,
            value: Value::String(format!("violet document {index}")).into(),
        });
    }
    let report = writer
        .transact(&TransactionRequest::new("seed", operations).with_tx_instant(1000))
        .unwrap();
    let basis = report.basis_t;
    let expected = report.db_after.datoms(IndexOrder::Eavt).unwrap();
    drop(report);
    writer.release().unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    no_reader_pins(&mut store);
    let root_id: Digest = store
        .read_ref(&database.reference_key())
        .unwrap()
        .unwrap()
        .value
        .unwrap()
        .try_into()
        .unwrap();
    let root = DatabaseRoot::decode(&root_id, &store.get(root_id).unwrap().unwrap()).unwrap();
    let index_id = root.indexes.unwrap();
    let index = IndexDescriptor::decode(&index_id, &store.get(index_id).unwrap().unwrap()).unwrap();
    assert!(index.basis < basis, "fixture must have an unindexed tail");
    let before_objects = objects(&mut admin);
    let directory = tempfile::tempdir().unwrap();
    let first_path = directory.path().join("readonly");
    let capture_started = Instant::now();
    let mut backup = PortableBackup::connect_configured(&readonly).unwrap();
    let first = backup.backup_database("source", &first_path).unwrap();
    let capture_elapsed = capture_started.elapsed();
    no_reader_pins(&mut store);
    assert_eq!(
        objects(&mut admin),
        before_objects,
        "file preparation wrote a source immutable object"
    );
    assert_eq!(
        store
            .read_ref(&database.reference_key())
            .unwrap()
            .unwrap()
            .value
            .unwrap(),
        root_id
    );

    // Ensure collection actually removes an unowned object while the backup's
    // publication pin alone protects the retired database's required closure.
    let orphan = store
        .put(
            &Block {
                kind: 65000,
                links: vec![],
                payload: b"unowned role fixture".to_vec(),
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let mut collection = None;
    let retired_path = directory.path().join("retired");
    let point = backup
        .backup_database_with_pin_probe("source", &retired_path, || {
            let retired = DatabaseCatalog::connect_configured(&config)
                .unwrap()
                .retire("source")
                .unwrap()
                .unwrap();
            assert_eq!(retired.database_id, first.lineage_id);
            assert!(
                store
                    .read_ref(&database.reference_key())
                    .unwrap()
                    .unwrap()
                    .value
                    .is_none()
            );
            collection = Some(collect(&config));
            assert!(
                store.get(orphan).unwrap().is_none(),
                "probe must exercise physical collection"
            );
            for id in [
                Some(root_id),
                root.log,
                root.indexes,
                root.receipts,
                root.metadata,
            ]
            .into_iter()
            .flatten()
            {
                assert!(
                    store.get(id).unwrap().is_some(),
                    "backup pin lost a required root child"
                );
            }
        })
        .unwrap();
    let (batches, gc) = collection.unwrap();
    assert!(gc.objects_removed > 0);
    assert_eq!(point.basis_t, basis);
    no_reader_pins(&mut store);
    drop((store, admin, backup));
    drop(role);
    drop(fixture); // No source SQL can satisfy the following reads.

    for path in [&first_path, &retired_path] {
        let offline = BackupConnection::open(path).unwrap();
        let db = offline.db();
        assert_eq!(db.datoms(IndexOrder::Eavt).unwrap(), expected);
        let query = Query::new(
            FindSpec::Relation(vec![FindElement::Variable("?v".into())]),
            vec![Clause::Pattern(Box::new(DataPattern::new(
                Term::var("?e"),
                Term::Constant(Value::Keyword(Keyword::new("item", "score"))),
                Term::var("?v"),
            )))],
        );
        let QueryResult::Relation(rows) = db
            .query(&query, &[], &QueryControl::default())
            .unwrap()
            .result
        else {
            panic!("relation result expected")
        };
        let mut scores = rows
            .into_iter()
            .map(|row| match row.as_slice() {
                [QueryValue::Scalar(Value::Long(score))] => *score,
                _ => panic!("one long query cell expected"),
            })
            .collect::<Vec<_>>();
        scores.sort_unstable();
        assert_eq!(scores, (0..48).collect::<Vec<_>>());
        assert_eq!(
            db.fulltext(TEXT, "violet", &FulltextOptions::default())
                .unwrap()
                .hits
                .len(),
            48
        );
        assert_eq!(
            offline
                .log()
                .tx_ids(Some(TimePoint::T(basis)), None)
                .unwrap()
                .collect::<Result<Vec<_>, _>>()
                .unwrap(),
            vec![t_to_tx(basis).unwrap()]
        );
    }
    eprintln!(
        "readonly backup unindexed=48 capture={capture_elapsed:?} gc_batches={batches} gc={gc:?} complete+source-drop+offline-query/fulltext/log={:?}",
        complete.elapsed()
    );
}
