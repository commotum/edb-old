use atomic_core::persistent_tree::TreeConfig;
use atomic_core::{
    Attribute, Cardinality, EntityRef, IndexOrder, Keyword, MAX_RECEIPT_ARCHIVE_WORK_PER_GC, Peer,
    PostgresIndexer, PostgresMigrator, PostgresOperator, PostgresStore, Schema, TransactionRequest,
    TxOp, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

mod common;

struct Fixture {
    connection: String,
    schema: String,
    sql: Client,
}

impl Fixture {
    fn new() -> Option<Self> {
        let connection = std::env::var("ATOMIC_POSTGRES_URL").ok()?;
        let schema = format!(
            "receipt_conversion_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let mut sql = Client::connect(&connection, NoTls).unwrap();
        sql.batch_execute(&format!("CREATE SCHEMA {schema}"))
            .unwrap();
        let connection =
            if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
                format!(
                    "{connection}{}options=-csearch_path%3D{schema}",
                    if connection.contains('?') { '&' } else { '?' }
                )
            } else {
                format!("{connection} options='-c search_path={schema}'")
            };
        let fixture = Self {
            sql: Client::connect(&connection, NoTls).unwrap(),
            connection,
            schema,
        };
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap();
        Some(fixture)
    }
}

impl Drop for Fixture {
    fn drop(&mut self) {
        // A generated schema owned exclusively by this fixture, never a catalog.
        let _ = self
            .sql
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}

fn marker() -> TransactionRequest {
    TransactionRequest::new(
        "marker",
        vec![TxOp::Add {
            entity: EntityRef::Temp("marker".into()),
            attribute: 1000,
            value: Value::Long(999).into(),
        }],
    )
}

fn gc(operator: &mut PostgresOperator, apply: bool) -> atomic_core::GarbageInventory {
    gc_result(operator, apply).unwrap()
}

fn gc_result(
    operator: &mut PostgresOperator,
    apply: bool,
) -> Result<atomic_core::GarbageInventory, atomic_core::SemanticError> {
    let began = Instant::now();
    loop {
        let result = if apply {
            operator.collect_garbage(Duration::ZERO)
        } else {
            operator.garbage_inventory(Duration::ZERO)
        };
        match result {
            Ok(inventory) => return Ok(inventory),
            Err(error)
                if error.code == "operations/semantic-gc-pinned"
                    && began.elapsed() < Duration::from_secs(30) =>
            {
                // PostgreSQL advisory keys are catalog-wide even across our
                // isolated schemas; an independent operations fixture may own it.
                std::thread::sleep(Duration::from_millis(10));
            }
            Err(error) => return Err(error),
        }
    }
}

#[test]
fn ordinary_receipts_convert_in_bounded_resumable_phases_without_changing_exact_retry() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let began = Instant::now();
    let connection = &fixture.connection;
    let database = "ordinary-receipt-conversion";
    let mut schema = Schema::new();
    let mut value = Attribute::new(
        1000,
        Keyword::new("item", "value"),
        ValueType::Long,
        Cardinality::One,
    );
    value.indexed = true;
    schema.install(value).unwrap();
    PostgresStore::connect(connection)
        .unwrap()
        .create_database(database, schema)
        .unwrap();
    let writer = common::start_service(connection, database);
    drop(
        writer
            .client()
            .transact(
                TransactionRequest::new(
                    "seed",
                    (0..180)
                        .map(|index| TxOp::Add {
                            entity: EntityRef::Temp(format!("item-{index}")),
                            attribute: 1000,
                            value: Value::Long(index).into(),
                        })
                        .collect(),
                ),
                Duration::from_secs(30),
            )
            .unwrap(),
    );
    writer.shutdown();
    let mut indexer = PostgresIndexer::connect(connection, database)
        .unwrap()
        .with_tree_config(TreeConfig {
            max_leaf_datoms: 1,
            max_leaves_per_directory: 16,
            ..TreeConfig::default()
        })
        .unwrap();
    indexer.consolidate().unwrap();
    drop(indexer);
    let old_peer = Peer::connect_with_cache_limits(connection, database, 0, 0).unwrap();
    let old = old_peer.database_value();
    let old_hash = old_peer.pinned_manifest_hashes()[0];
    let expected_before = old.datoms(IndexOrder::Eavt).unwrap();
    let expected_history = old.clone().history().datoms(IndexOrder::Eavt).unwrap();
    let writer = common::start_service(connection, database);
    let report = writer
        .client()
        .transact(marker(), Duration::from_secs(30))
        .unwrap();
    let expected_basis = report.basis_t;
    let expected_hash = report.tx_hash;
    let expected_tx = report.tx_data.clone();
    let expected_tempids = report.tempids.clone();
    assert_eq!(
        report.db_before.datoms(IndexOrder::Eavt).unwrap(),
        expected_before
    );
    drop(report);
    writer.shutdown();
    PostgresIndexer::connect(connection, database)
        .unwrap()
        .consolidate()
        .unwrap();
    let binding: Vec<u8> = fixture.sql.query_one("SELECT base_manifest_hash FROM atomic_generation_request_bases base JOIN atomic_generation_requests request USING(database_id,generation,request_key_hash) WHERE database_id=$1 AND request.basis_t=$2", &[&database, &(expected_basis as i64)]).unwrap().get(0);
    assert_eq!(binding, old_hash);
    let mut operator = PostgresOperator::connect(connection).unwrap();
    for _ in 0..64 {
        let inventory = gc(&mut operator, true);
        assert!(
            inventory
                .receipt_archive_conversions
                .iter()
                .all(|step| step.manifest_hash != old_hash)
        );
    }
    assert_eq!(old.datoms(IndexOrder::Eavt).unwrap(), expected_before);
    assert_eq!(
        old.clone().history().datoms(IndexOrder::Eavt).unwrap(),
        expected_history
    );
    assert_eq!(old_peer.load_stats().compatibility_materializations, 0);
    drop(old);
    drop(old_peer);

    // Damaged relational metadata must not become a completed archive owner.
    common::with_replica_triggers_disabled(&mut fixture.sql, |sql| sql.execute(
        "UPDATE atomic_tree_manifest_roots SET encoded_bytes=encoded_bytes+1 WHERE manifest_hash=$1 AND index_order=0 AND NOT history", &[&&old_hash[..]],
    )).unwrap();
    assert_eq!(
        gc_result(&mut operator, false).unwrap_err().code,
        "operations/receipt-archive-conversion"
    );
    common::with_replica_triggers_disabled(&mut fixture.sql, |sql| sql.execute(
        "UPDATE atomic_tree_manifest_roots SET encoded_bytes=encoded_bytes-1 WHERE manifest_hash=$1 AND index_order=0 AND NOT history", &[&&old_hash[..]],
    )).unwrap();

    let mut batches = 0;
    let mut total_reads = 0;
    let mut total_hashes = 0;
    let mut observed_large_hash_page = false;
    let mut corrupted_frontier = false;
    let mut corrupted_checkpoint = false;
    let mut retried_during_staging = false;
    let mut completed = false;
    while batches < 128 {
        let preview = gc(&mut operator, false);
        let mut expected = preview.clone();
        expected.applied = true;
        let inventory = gc(&mut operator, true);
        assert_eq!(inventory, expected, "same-snapshot phase prediction");
        batches += 1;
        for step in &inventory.receipt_archive_conversions {
            assert!(
                step.node_reads <= MAX_RECEIPT_ARCHIVE_WORK_PER_GC as u64,
                "{step:?}"
            );
            assert!(
                step.hashes_processed <= MAX_RECEIPT_ARCHIVE_WORK_PER_GC as u64,
                "{step:?}"
            );
            assert!(
                step.retired_rows_drained <= MAX_RECEIPT_ARCHIVE_WORK_PER_GC as u64,
                "{step:?}"
            );
            total_reads += step.node_reads;
            total_hashes += step.hashes_processed;
            observed_large_hash_page |=
                step.hashes_processed == MAX_RECEIPT_ARCHIVE_WORK_PER_GC as u64;
        }
        if !corrupted_frontier && let Some(row) = fixture.sql.query_opt("SELECT node_hash,next_child FROM atomic_receipt_archive_frontier WHERE manifest_hash=$1 ORDER BY node_hash LIMIT 1", &[&&old_hash[..]]).unwrap() {
            let node: Vec<u8> = row.get(0);
            let offset: i32 = row.get(1);
            fixture.sql.execute("UPDATE atomic_receipt_archive_frontier SET next_child=2147483647 WHERE manifest_hash=$1 AND node_hash=$2", &[&&old_hash[..], &node]).unwrap();
            assert_eq!(gc_result(&mut operator, false).unwrap_err().code, "operations/receipt-archive-conversion", "invalid frontier must fail closed");
            fixture.sql.execute("UPDATE atomic_receipt_archive_frontier SET next_child=$3 WHERE manifest_hash=$1 AND node_hash=$2", &[&&old_hash[..], &node, &offset]).unwrap();
            corrupted_frontier = true;
        }
        if !corrupted_checkpoint && let Some(row) = fixture.sql.query_opt("SELECT hash_state FROM atomic_receipt_archive_conversions WHERE manifest_hash=$1 AND phase=2 AND hashed_nodes>0", &[&&old_hash[..]]).unwrap() {
            let saved: Vec<u8> = row.get(0);
            fixture.sql.execute("UPDATE atomic_receipt_archive_conversions SET hash_state=$2 WHERE manifest_hash=$1", &[&&old_hash[..], &vec![0_u8]]).unwrap();
            assert_eq!(gc_result(&mut operator, true).unwrap_err().code, "operations/archive-hash-checkpoint", "malformed checkpoint must fail closed");
            fixture.sql.execute("UPDATE atomic_receipt_archive_conversions SET hash_state=$2 WHERE manifest_hash=$1", &[&&old_hash[..], &saved]).unwrap();
            corrupted_checkpoint = true;
        }
        if !retried_during_staging && corrupted_frontier {
            let writer = common::start_service(connection, database);
            let retry = writer
                .client()
                .transact(marker(), Duration::from_secs(30))
                .unwrap();
            assert!(retry.replayed);
            assert_eq!(
                retry.db_before.datoms(IndexOrder::Eavt).unwrap(),
                expected_before
            );
            assert_eq!(retry.tx_hash, expected_hash);
            drop(retry);
            writer.shutdown();
            retried_during_staging = true;
        }
        drop(operator);
        operator = PostgresOperator::connect(connection).unwrap(); // Actual durable resume each batch.
        let row = fixture.sql.query_one("SELECT EXISTS(SELECT 1 FROM atomic_tree_publications WHERE manifest_hash=$1),EXISTS(SELECT 1 FROM atomic_request_base_archive_completions WHERE manifest_hash=$1)", &[&&old_hash[..]]).unwrap();
        let published: bool = row.get(0);
        let archived: bool = row.get(1);
        assert_ne!(
            published, archived,
            "exactly one complete immutable owner per committed state"
        );
        if archived {
            completed = true;
            break;
        }
    }
    assert!(
        completed
            && corrupted_frontier
            && corrupted_checkpoint
            && retried_during_staging
            && observed_large_hash_page,
        "completion={completed} frontier={corrupted_frontier} checkpoint={corrupted_checkpoint} retry={retried_during_staging} large_page={observed_large_hash_page}"
    );
    let writer = common::start_service(connection, database);
    let retry = writer
        .client()
        .transact(marker(), Duration::from_secs(30))
        .unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, expected_basis);
    assert_eq!(retry.tx_hash, expected_hash);
    assert_eq!(retry.tx_data, expected_tx);
    assert_eq!(retry.tempids, expected_tempids);
    assert_eq!(
        retry.db_before.datoms(IndexOrder::Eavt).unwrap(),
        expected_before
    );
    assert_eq!(
        retry
            .db_before
            .clone()
            .history()
            .datoms(IndexOrder::Eavt)
            .unwrap(),
        expected_history
    );
    assert_eq!(
        retry
            .db_after
            .values(expected_tempids["marker"], 1000)
            .unwrap(),
        [Value::Long(999)]
    );
    let preserved: Vec<u8> = fixture.sql.query_one("SELECT base_manifest_hash FROM atomic_generation_request_bases base JOIN atomic_generation_requests request USING(database_id,generation,request_key_hash) WHERE database_id=$1 AND request.basis_t=$2", &[&database, &(expected_basis as i64)]).unwrap().get(0);
    assert_eq!(preserved, old_hash);
    drop(retry);
    writer.shutdown();
    eprintln!(
        "receipt_conversion_live=passed batches={batches} node_reads={total_reads} hashes={total_hashes} elapsed_ms={}",
        began.elapsed().as_millis()
    );
}
