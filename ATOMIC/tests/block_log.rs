mod common;

use atomic_core::storage::PgBlockStore;
use atomic_core::storage::log::{LOG_PAGE_ENTRIES, LogEntry, LogRoot};
use atomic_core::storage::root::{Block, MAX_BLOCK_BYTES};
use atomic_core::{
    Datom, DurableTransaction, OperationContext, OperationKind, PostgresConnectionConfig,
    SqlCallKind, Value, encode_transaction, make_eid, t_to_tx,
};
use postgres::{Client, NoTls};
use std::collections::BTreeMap;
use std::time::Instant;

fn fixture(label: &str) -> Option<(common::PostgresFixture, PgBlockStore)> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return None;
    };
    let scope = common::PostgresFixture::new(&connection, label);
    let config = PostgresConnectionConfig::plaintext(&scope.connection);
    PgBlockStore::install(&config).unwrap();
    let store = PgBlockStore::connect(&config).unwrap();
    Some((scope, store))
}

fn entry(basis_t: u64) -> LogEntry {
    LogEntry {
        basis_t,
        eidx_frontier: 1_003,
        reserved_frontier: 1_003,
        tx_data: vec![Datom {
            entity: make_eid(atomic_core::USER_PARTITION, 1_001).unwrap(),
            attribute: 1_000,
            value: Value::Long(basis_t as i64),
            tx: t_to_tx(basis_t).unwrap(),
            added: true,
        }],
    }
}

#[test]
fn append_has_constant_object_writes_and_selective_authenticated_navigation() {
    let Some((scope, mut store)) = fixture("block_log_navigation") else {
        return;
    };
    let started = Instant::now();
    let total = LOG_PAGE_ENTRIES as u64 * 17 + 3;
    let mut root = LogRoot::empty();
    let mut captured = None;
    let mut writes = 0;
    for basis_t in 1..=total {
        let context = OperationContext::new(OperationKind::Transaction);
        let guard = context.enter();
        root = root.append(&mut store, &entry(basis_t)).unwrap();
        drop(guard);
        let sql = context.snapshot();
        assert_eq!(sql.by_kind[&SqlCallKind::Execute].calls, 2);
        writes += sql.by_kind[&SqlCallKind::Execute].calls;
        if basis_t == 66 {
            captured = Some(root.clone());
        }
    }
    let append_elapsed = started.elapsed();
    let head = root.head().unwrap();
    drop(store);
    let config = PostgresConnectionConfig::plaintext(&scope.connection);
    let mut store = PgBlockStore::connect(&config).unwrap();
    let context = OperationContext::new(OperationKind::Query);
    let guard = context.enter();
    let root = LogRoot::open(&mut store, head).unwrap();
    assert_eq!(root.basis_t(), total);
    assert_eq!(
        context.snapshot().sql_calls,
        2,
        "open reads one tail and its last entry metadata"
    );
    drop(guard);
    let mut max_read_calls = 0;
    for basis_t in [1, 64, 65, 127, 128, 129, total - 1, total] {
        let context = OperationContext::new(OperationKind::Query);
        let guard = context.enter();
        assert_eq!(
            root.read(&mut store, basis_t).unwrap(),
            Some(entry(basis_t))
        );
        drop(guard);
        let calls = context.snapshot().sql_calls;
        assert!(calls <= 6, "selective log lookup read {calls} objects");
        max_read_calls = max_read_calls.max(calls);
    }
    let range = root
        .range(&mut store, 61, 70)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_eq!(range, (61..70).map(entry).collect::<Vec<_>>());
    let range_context = OperationContext::new(OperationKind::Query);
    let range_started = Instant::now();
    let complete_range = {
        let _guard = range_context.enter();
        root.range(&mut store, 1, total + 1)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap()
    };
    let range_elapsed = range_started.elapsed();
    let range_io = range_context.snapshot();
    assert_eq!(complete_range, (1..=total).map(entry).collect::<Vec<_>>());
    let pages = total.div_ceil(LOG_PAGE_ENTRIES as u64);
    assert_eq!(
        range_io.sql_calls,
        total + pages - 1,
        "one inline entry read per record and one read per page except the captured tail"
    );
    assert!(range_io.known_payload_read_bytes > 0);
    eprintln!(
        "BLOCK_LOG_RANGE_SAMPLE transactions={total} pages={pages} sql_calls={} payload_read_bytes={} complete_us={}",
        range_io.sql_calls,
        range_io.known_payload_read_bytes,
        range_elapsed.as_micros()
    );
    assert!(root.read(&mut store, 0).unwrap().is_none());
    assert!(root.read(&mut store, total + 1).unwrap().is_none());
    let captured = captured.unwrap();
    let prefix_work = OperationContext::new(OperationKind::Query);
    {
        let _guard = prefix_work.enter();
        assert!(root.extends_prefix(&mut store, &captured).unwrap());
    }
    assert!(prefix_work.snapshot().sql_calls <= 5);
    assert!(prefix_work.snapshot().known_payload_read_bytes < 64 * 1024);
    assert!(!captured.extends_prefix(&mut store, &root).unwrap());
    assert_eq!(captured.read(&mut store, 66).unwrap(), Some(entry(66)));
    assert!(captured.read(&mut store, 67).unwrap().is_none());
    let mut changed = entry(67);
    changed.tx_data[0].value = Value::Long(-67);
    let branch = captured.append(&mut store, &changed).unwrap();
    assert_eq!(branch.read(&mut store, 67).unwrap(), Some(changed));
    assert_eq!(root.read(&mut store, 67).unwrap(), Some(entry(67)));
    assert_eq!(
        store.list_refs(None, 10).unwrap().len(),
        1,
        "log appends never publish mutable refs"
    );
    eprintln!(
        "BLOCK_LOG_SAMPLE transactions={total} immutable_writes={writes} append_complete_ms={} open_object_reads=2 max_lookup_object_reads={max_read_calls}",
        append_elapsed.as_millis()
    );
}

#[test]
fn prefix_proof_rejects_changed_earlier_entry_despite_equal_endpoint_id() {
    let Some((_scope, mut store)) = fixture("block_log_prefix") else {
        return;
    };
    let first = LogRoot::empty().append(&mut store, &entry(1)).unwrap();
    let before = first.append(&mut store, &entry(2)).unwrap();
    let after = before.append(&mut store, &entry(3)).unwrap();
    assert!(after.extends_prefix(&mut store, &before).unwrap());
    assert!(after.extends_prefix(&mut store, &LogRoot::empty()).unwrap());
    let mut changed = entry(1);
    changed.tx_data[0].value = Value::Long(-1);
    let fork = LogRoot::empty().append(&mut store, &changed).unwrap();
    let changed_id = fork.read_record(&mut store, 1).unwrap().unwrap().id;
    let id = after.head().unwrap();
    let mut page = Block::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
    page.links[0] = changed_id;
    let forged_id = store.put(&page.encode().unwrap()).unwrap();
    let forged = LogRoot::open(&mut store, forged_id).unwrap();
    assert_eq!(
        forged.read_record(&mut store, 2).unwrap().unwrap().id,
        before.read_record(&mut store, 2).unwrap().unwrap().id
    );
    assert!(!forged.extends_prefix(&mut store, &before).unwrap());
}

#[test]
fn missing_entry_fails_the_stream_and_wrong_skip_coordinates_are_rejected() {
    let Some((_scope, mut store)) = fixture("block_log_integrity") else {
        return;
    };
    let mut root = LogRoot::empty();
    for basis_t in 1..=130 {
        root = root.append(&mut store, &entry(basis_t)).unwrap();
    }
    let missing = root.read_record(&mut store, 63).unwrap().unwrap().id;
    assert_eq!(store.remove_objects(&[missing]).unwrap(), 1);
    let mut range = root.range(&mut store, 62, 66).unwrap();
    assert_eq!(range.next().unwrap().unwrap(), entry(62));
    assert_eq!(
        range.next().unwrap().unwrap_err().code,
        "storage/missing-object"
    );
    assert!(
        range.next().is_none(),
        "a failed authenticated stream remains failed"
    );
    drop(range);
    let head = root.head().unwrap();
    let bytes = store.get(head).unwrap().unwrap();
    let mut bad_page = Block::decode(&head, &bytes).unwrap();
    // Tail page2 has two entry IDs then links to page1 and page0. Substituting
    // the wrong valid object preserves its hash but violates navigation.
    assert_eq!(bad_page.links.len(), 4);
    bad_page.links[3] = bad_page.links[2];
    let bad_head = store.put(&bad_page.encode().unwrap()).unwrap();
    let invalid = LogRoot::open(&mut store, bad_head).unwrap();
    assert_eq!(
        invalid.read(&mut store, 1).unwrap_err().code,
        "storage/log-skip-coordinate"
    );
    let mut bad_entry_page = Block::decode(&head, &bytes).unwrap();
    bad_entry_page.links[0] = bad_entry_page.links[1];
    let bad_head = store.put(&bad_entry_page.encode().unwrap()).unwrap();
    let invalid = LogRoot::open(&mut store, bad_head).unwrap();
    assert_eq!(
        invalid.read(&mut store, 129).unwrap_err().code,
        "storage/log-entry-coordinate"
    );
}

#[test]
fn corrupt_reachable_bytes_and_invalid_append_leave_captured_log_unchanged() {
    let Some((scope, mut store)) = fixture("block_log_corruption") else {
        return;
    };
    let root = LogRoot::empty().append(&mut store, &entry(1)).unwrap();
    let head = root.head().unwrap();
    let before = store.list_objects(None, 10).unwrap();
    assert_eq!(
        root.append(&mut store, &entry(3)).unwrap_err().code,
        "storage/log-append-coordinate"
    );
    let mut backward = entry(2);
    backward.reserved_frontier = 1_002;
    assert_eq!(
        root.append(&mut store, &backward).unwrap_err().code,
        "storage/log-append-coordinate"
    );
    assert_eq!(store.list_objects(None, 10).unwrap(), before);
    let record = root.read_record(&mut store, 1).unwrap().unwrap();
    let mut sql = Client::connect(&scope.connection, NoTls).unwrap();
    sql.execute(
        "UPDATE atomic_objects SET payload=payload || decode('00','hex') WHERE id=$1",
        &[&&record.id[..]],
    )
    .unwrap();
    assert_eq!(
        LogRoot::open(&mut store, head).unwrap_err().code,
        "storage/object-corrupt"
    );
    assert_eq!(
        root.read(&mut store, 1).unwrap_err().code,
        "storage/object-corrupt"
    );
    assert_eq!(root.head(), Some(head));
}

#[test]
fn maximum_current_transaction_roundtrips_through_bounded_chunk_objects() {
    let Some((_scope, mut store)) = fixture("block_log_large") else {
        return;
    };
    let mut large = entry(1);
    large.eidx_frontier = 1_004;
    large.reserved_frontier = 1_004;
    // Four byte values fill the current transaction codec's 64 MiB body.
    // Its checked envelope exceeds the storage provider's single-object cap.
    large.tx_data = (0..4)
        .map(|slot| Datom {
            entity: make_eid(atomic_core::USER_PARTITION, 1_001).unwrap(),
            attribute: 1_000 + slot,
            value: Value::Bytes(vec![slot as u8; 16 * 1024 * 1024 - 43]),
            tx: t_to_tx(1).unwrap(),
            added: true,
        })
        .collect();
    let current = DurableTransaction {
        database_id: "boundary".into(),
        basis_t: 1,
        previous_hash: [0; 32],
        eidx_frontier: large.eidx_frontier,
        tempids: BTreeMap::new(),
        tx_data: large.tx_data.clone(),
    };
    let encoded = encode_transaction(&current).unwrap();
    assert!(encoded.len() > MAX_BLOCK_BYTES);
    drop(encoded);
    drop(current);
    let started = Instant::now();
    let operation = OperationContext::new(OperationKind::Transaction);
    let guard = operation.enter();
    let root = LogRoot::empty().append(&mut store, &large).unwrap();
    drop(guard);
    assert_eq!(
        operation.snapshot().by_kind[&SqlCallKind::Execute].calls,
        18
    );
    let reopened = LogRoot::open(&mut store, root.head().unwrap()).unwrap();
    assert_eq!(reopened.read(&mut store, 1).unwrap(), Some(large));
    // Equal chunks may deduplicate; the authenticated entry still has one
    // ordered link for each of its sixteen physical positions.
    let head = root.head().unwrap();
    let tail = Block::decode(&head, &store.get(head).unwrap().unwrap()).unwrap();
    let id = tail.links[0];
    let bytes = store.get(id).unwrap().unwrap();
    assert_eq!(Block::decode(&id, &bytes).unwrap().links.len(), 16);
    eprintln!(
        "BLOCK_LOG_LARGE current_body_bytes={} append_open_read_ms={}",
        64 * 1024 * 1024,
        started.elapsed().as_millis()
    );
}
