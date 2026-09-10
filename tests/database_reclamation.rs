mod common;
#[path = "support/database_reclamation_concurrency.rs"]
mod concurrency;
use atomic_core::*;
use std::time::{Duration, Instant};

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP retired database reclamation: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let f = common::PostgresFixture::new(&url, label);
    PostgresMigrator::connect(&f.connection)
        .unwrap()
        .migrate()
        .unwrap();
    Some(f)
}
fn schema() -> Schema {
    let mut s = Schema::new();
    s.install(Attribute::new(
        1000,
        Keyword::new("item", "value"),
        ValueType::Long,
        Cardinality::Many,
    ))
    .unwrap();
    s.install(
        Attribute::new(
            1001,
            Keyword::new("item", "text"),
            ValueType::String,
            Cardinality::One,
        )
        .fulltext(),
    )
    .unwrap();
    s
}
fn seed(url: &str, name: &str, n: usize) -> DatabaseCatalogEntry {
    let entry = DatabaseCatalog::connect(url)
        .unwrap()
        .create_if_absent(name, schema())
        .unwrap()
        .database;
    let hash = PostgresStore::connect(url)
        .unwrap()
        .deploy_program_blob(&Program {
            kind: ProgramKind::Query,
            arity: 0,
            instructions: vec![
                Instruction::PushConstant(Value::Long(17)),
                Instruction::Return,
            ],
        })
        .unwrap();
    let writer = common::start_service(url, name);
    let mut ops = (0..n)
        .map(|i| TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1000,
            value: Value::Long(i as i64).into(),
        })
        .collect::<Vec<_>>();
    ops.push(TxOp::Add {
        entity: EntityRef::Temp("item".into()),
        attribute: 1001,
        value: Value::String("shared amber document".into()).into(),
    });
    ops.push(TxOp::Add {
        entity: EntityRef::Temp("program".into()),
        attribute: DB_IDENT as u32,
        value: Value::Keyword(Keyword::new("item", "query")).into(),
    });
    ops.push(TxOp::Add {
        entity: EntityRef::Temp("program".into()),
        attribute: DB_FN as u32,
        value: Value::Function(hash).into(),
    });
    drop(
        writer
            .client()
            .transact(
                TransactionRequest::new("seed", ops).with_tx_instant(1),
                Duration::from_secs(30),
            )
            .unwrap(),
    );
    writer.shutdown();
    PostgresIndexer::connect(url, name)
        .unwrap()
        .consolidate()
        .unwrap();
    entry
}
fn complete(url: &str, entry: &DatabaseCatalogEntry) -> (u64, u64) {
    let start = Instant::now();
    let mut batches = 0;
    let mut removed = 0;
    let mut inserted = 0;
    let mut updated = 0;
    let mut objects = 0;
    let mut pins = 0;
    let mut operator = PostgresOperator::connect(url).unwrap();
    // Reconnect at repeatable checkpoints: no in-memory progress or advisory
    // lock is relied on across operator invocations/process interruption.
    loop {
        if batches == 3 || (batches > 0 && batches % 32 == 0) {
            operator = PostgresOperator::connect(url).unwrap();
        }
        let before = retry(|| {
            operator.preview_retired_database_reclamation(
                &entry.database_id,
                &entry.lineage_id,
                Duration::ZERO,
            )
        });
        let after = retry(|| {
            operator.reclaim_retired_database(&entry.database_id, &entry.lineage_id, Duration::ZERO)
        });
        assert_eq!(
            (before.phase, before.rows_selected, before.complete),
            (after.phase, after.rows_selected, after.complete)
        );
        assert!(after.rows_selected <= MAX_DATABASE_RECLAMATION_ROWS as u64);
        assert!(
            after.rows_removed + after.rows_inserted + after.rows_updated
                <= MAX_DATABASE_RECLAMATION_ROWS as u64
        );
        removed += after.rows_removed;
        inserted += after.rows_inserted;
        updated += after.rows_updated;
        objects += after.objects_read;
        pins += after.pins_checked;
        batches += 1;
        if after.complete {
            break;
        }
        assert!(batches < 10000, "reclamation did not progress: {after:?}");
    }
    eprintln!(
        "complete reclaimed storage/check/drop: batches={batches} removed={removed} inserted={inserted} updated={updated} objects={objects} pins={pins} elapsed={:?}",
        start.elapsed()
    );
    (batches, removed)
}
fn retry<T>(mut f: impl FnMut() -> Result<T, SemanticError>) -> T {
    let deadline = Instant::now() + Duration::from_secs(10);
    loop {
        match f() {
            Ok(x) => return x,
            Err(e) if e.category == ErrorCategory::Busy && Instant::now() < deadline => {
                std::thread::sleep(Duration::from_millis(10))
            }
            Err(e) => panic!("reclamation: {e:?}"),
        }
    }
}

#[test]
fn physical_reclamation_is_bounded_resumable_and_keeps_shared_content_and_tombstones() {
    let Some(f) = fixture("terminal_reclamation") else {
        return;
    };
    let url = &f.connection;
    let old = seed(url, "old", 64);
    let shared = seed(url, "shared", 64);
    let mut sql = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let shared_nodes:i64=sql.query_one("SELECT count(*) FROM atomic_tree_live_nodes a JOIN atomic_tree_live_nodes b USING(node_hash) WHERE a.database_id=$1 AND b.database_id=$2",&[&old.database_id,&shared.database_id]).unwrap().get(0);
    assert!(
        shared_nodes > 0,
        "fixture must genuinely share canonical nodes"
    );
    let shared_pages:i64=sql.query_one("SELECT count(*) FROM atomic_fulltext_page_roots a JOIN atomic_fulltext_page_roots b USING(root_hash) JOIN atomic_tree_manifests ma ON ma.manifest_hash=a.manifest_hash JOIN atomic_tree_manifests mb ON mb.manifest_hash=b.manifest_hash WHERE ma.database_id=$1 AND mb.database_id=$2",&[&old.database_id,&shared.database_id]).unwrap().get(0);
    assert!(shared_pages > 0, "fixture must share fulltext page roots");
    let mut catalog = DatabaseCatalog::connect(url).unwrap();
    assert!(
        PostgresOperator::connect(url)
            .unwrap()
            .reclaim_retired_database(&old.database_id, &old.lineage_id, Duration::ZERO)
            .is_err()
    );
    catalog.retire_checked("old", &old.lineage_id).unwrap();
    let mut op = PostgresOperator::connect(url).unwrap();
    assert!(
        op.preview_retired_database_reclamation(&old.database_id, "wrong", Duration::ZERO)
            .is_err()
    );
    assert!(
        op.preview_retired_database_reclamation(
            &old.database_id,
            &old.lineage_id,
            Duration::from_secs(3600)
        )
        .is_err()
    );
    let first = op
        .preview_retired_database_reclamation(&old.database_id, &old.lineage_id, Duration::ZERO)
        .unwrap();
    assert!(!first.applied);
    assert_eq!(
        sql.query_one(
            "SELECT count(*) FROM atomic_database_reclamation_progress",
            &[]
        )
        .unwrap()
        .get::<_, i64>(0),
        0,
        "preview must not seed progress"
    );
    complete(url, &old);
    assert_eq!(
        sql.query_one(
            "SELECT count(*) FROM atomic_databases WHERE database_id=$1",
            &[&old.database_id]
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    assert_eq!(
        sql.query_one(
            "SELECT count(*) FROM atomic_transaction_contents WHERE lineage_id=$1",
            &[&old.lineage_id]
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    assert!(sql.query_one("SELECT retired_at IS NOT NULL AND reclaimed_at IS NOT NULL FROM atomic_database_identities WHERE database_id=$1",&[&old.database_id]).unwrap().get::<_,bool>(0));
    assert_eq!(
        sql.query_one(
            "SELECT count(*) FROM atomic_database_reclamation_objects WHERE database_id=$1",
            &[&old.database_id]
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    let value = Peer::connect(url, "shared", 0).unwrap().database_value();
    assert_eq!(
        value
            .datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .filter(|d| d.attribute == 1000)
            .count(),
        64
    );
    assert_eq!(
        value
            .fulltext(1001, "amber", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_programs", &[])
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    drop(value);
    catalog
        .retire_checked("shared", &shared.lineage_id)
        .unwrap();
    complete(url, &shared);
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_fulltext_pages", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    assert_eq!(
        sql.query_one("SELECT count(*) FROM atomic_programs", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    // Pre-existing global orphans are the ordinary collector's domain; a
    // completed target frontier must not leave a permanent GC barrier.
    retry(|| op.collect_garbage(Duration::ZERO));
    let replacement = catalog.create_if_absent("old", schema()).unwrap().database;
    assert_ne!(replacement.database_id, old.database_id);
    assert_ne!(replacement.lineage_id, old.lineage_id);
    assert!(
        op.reclaim_retired_database(&old.database_id, &old.lineage_id, Duration::ZERO)
            .unwrap()
            .complete
    );
}

#[test]
fn receipt_only_allocations_and_unuploaded_build_intent_cross_real_batch_boundary() {
    let Some(f) = fixture("terminal_large") else {
        return;
    };
    let url = &f.connection;
    let entry = DatabaseCatalog::connect(url)
        .unwrap()
        .create_if_absent("large", schema())
        .unwrap()
        .database;
    let writer = common::start_service(url, "large");
    let ops = (0..600)
        .map(|i| TxOp::Retract {
            entity: EntityRef::Temp(format!("unused{i}")),
            attribute: 1000,
            value: Some(Value::Long(0).into()),
        })
        .collect();
    let receipt = writer
        .client()
        .transact(
            TransactionRequest::new("receipt-only", ops).with_tx_instant(1),
            Duration::from_secs(30),
        )
        .unwrap();
    assert_eq!(receipt.tempids.len(), 600);
    drop(receipt);
    writer.shutdown();
    let mut sql = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let generation = sql
        .query_one(
            "SELECT log_generation FROM atomic_heads WHERE database_id=$1",
            &[&entry.database_id],
        )
        .unwrap()
        .get::<_, i64>(0) as u64;
    let hashes = (0..600)
        .map(|i| sha256(format!("never-uploaded-{i}").as_bytes()))
        .collect();
    let intent = sha256(b"unpublished-large-intent");
    let mut trees = PostgresTreeStore::connect(url).unwrap();
    trees
        .begin_build_intent(&entry.database_id, generation, 0, intent, &hashes)
        .unwrap();
    trees.release_build_intent().unwrap();
    drop(trees);
    assert_eq!(
        sql.query_one(
            "SELECT count(*) FROM atomic_tree_build_intent_nodes WHERE manifest_hash=$1",
            &[&&intent[..]]
        )
        .unwrap()
        .get::<_, i64>(0),
        600
    );
    assert_eq!(
        sql.query_one(
            "SELECT count(*) FROM atomic_generation_request_tempids WHERE database_id=$1",
            &[&entry.database_id]
        )
        .unwrap()
        .get::<_, i64>(0),
        600
    );
    DatabaseCatalog::connect(url)
        .unwrap()
        .retire_checked("large", &entry.lineage_id)
        .unwrap();
    let mut operator = PostgresOperator::connect(url).unwrap();
    let mut saw_full = false;
    let start = Instant::now();
    let mut steps = 0;
    loop {
        let report = retry(|| {
            operator.reclaim_retired_database(&entry.database_id, &entry.lineage_id, Duration::ZERO)
        });
        saw_full |= report.rows_removed == 512;
        assert!(report.rows_removed + report.rows_inserted + report.rows_updated <= 512);
        steps += 1;
        if report.complete {
            break;
        }
        assert!(steps < 10000);
    }
    assert!(
        saw_full,
        "must actually delete a512-row batch, not infer a bound from SQL LIMIT"
    );
    assert_eq!(
        sql.query_one(
            "SELECT count(*) FROM atomic_tree_build_intents WHERE database_id=$1",
            &[&entry.database_id]
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    eprintln!(
        "large600receipt+600unuploaded-intent complete: batches={steps} elapsed={:?}",
        start.elapsed()
    );
}

#[test]
fn captured_value_and_inflight_backup_pin_retired_storage_and_completed_backup_restores_fresh_route()
 {
    let Some(f) = fixture("terminal_backup") else {
        return;
    };
    let url = &f.connection;
    let old = seed(url, "source", 4);
    let peer = Peer::connect(url, "source", 0).unwrap();
    let captured = peer.database_value();
    let reference = captured.snapshot_reference().unwrap();
    let directory = std::env::temp_dir().join(format!("atomic-retired-backup-{}", f.schema));
    let point = PortableBackup::connect(url)
        .unwrap()
        .backup_database_with_pin_probe("source", &directory, || {
            DatabaseCatalog::connect(url)
                .unwrap()
                .retire_checked("source", &old.lineage_id)
                .unwrap();
            let error = PostgresOperator::connect(url)
                .unwrap()
                .reclaim_retired_database(&old.database_id, &old.lineage_id, Duration::ZERO)
                .unwrap_err();
            assert_eq!(error.category, ErrorCategory::Busy);
        })
        .unwrap();
    assert_eq!(point.lineage_id, old.lineage_id);
    assert_eq!(
        captured
            .datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .filter(|d| d.attribute == 1000)
            .count(),
        4
    );
    assert!(
        PortableBackup::connect(url)
            .unwrap()
            .backup_database("source", &directory)
            .is_err()
    );
    let blocked = PortableBackup::connect(url)
        .unwrap()
        .restore_backup(&directory, point.basis_t, "source")
        .unwrap_err();
    assert_eq!(blocked.code, "backup/retired-lineage-requires-reclamation");
    drop(captured);
    drop(peer);
    complete(url, &old);
    let restored = PortableBackup::connect(url)
        .unwrap()
        .restore_backup(&directory, point.basis_t, "source")
        .unwrap();
    assert_eq!(restored.basis_t(), point.basis_t);
    let fresh = DatabaseCatalog::connect(url)
        .unwrap()
        .resolve("source")
        .unwrap();
    assert_ne!(fresh.database_id, old.database_id);
    assert_eq!(fresh.lineage_id, old.lineage_id);
    assert!(
        reference
            .open(&PostgresConnectionConfig::plaintext(url), 0, 0)
            .is_err(),
        "old route must not reopen restored same-lineage storage"
    );
    let value = Peer::connect(url, "source", 0).unwrap().database_value();
    assert_eq!(
        value
            .datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .filter(|d| d.attribute == 1000)
            .count(),
        4
    );
    std::fs::remove_dir_all(directory).unwrap();
}
