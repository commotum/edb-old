mod common;

use atomic_core::storage::PgBlockStore;
use atomic_core::storage::log::{LogEntry, LogRoot};
use atomic_core::storage::receipts::{
    ExactReceipt, MAX_RECEIPT_TEMPID_BYTES, RECEIPT_KIND, REQUEST_BRANCH_KIND, RequestIndex,
    scoped_request_key,
};
use atomic_core::storage::root::{Block, DatabaseValueRoot};
use atomic_core::{
    Datom, DurableTransaction, OperationContext, OperationKind, PostgresConnectionConfig,
    SqlCallKind, Value, encode_transaction, make_eid, sha256, t_to_tx,
};
use postgres::{Client, NoTls};
use std::collections::BTreeMap;
use std::time::Instant;

const IDENTITY: [u8; 16] = [41; 16];

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

fn example(store: &mut PgBlockStore) -> ExactReceipt {
    let before = store
        .put(
            &DatabaseValueRoot {
                identity: IDENTITY,
                basis: 0,
                log: None,
                indexes: None,
                metadata: None,
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let log = LogRoot::empty()
        .append(
            store,
            &LogEntry {
                basis_t: 1,
                eidx_frontier: 1003,
                reserved_frontier: 1001,
                tx_data: vec![Datom {
                    entity: make_eid(atomic_core::USER_PARTITION, 1001).unwrap(),
                    attribute: 1000,
                    value: Value::Long(7),
                    tx: t_to_tx(1).unwrap(),
                    added: true,
                }],
            },
        )
        .unwrap();
    let after = store
        .put(
            &DatabaseValueRoot {
                identity: IDENTITY,
                basis: 1,
                log: log.head(),
                indexes: None,
                metadata: None,
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    ExactReceipt {
        identity: IDENTITY,
        request_digest: sha256(b"canonical request"),
        basis: 1,
        transaction: log.read_record(store, 1).unwrap().unwrap().id,
        before,
        after,
        tempids: BTreeMap::from([
            (
                "item".into(),
                make_eid(atomic_core::USER_PARTITION, 1001).unwrap(),
            ),
            ("transaction".into(), t_to_tx(1).unwrap()),
        ]),
    }
}

fn writes(context: &OperationContext) -> u64 {
    context
        .snapshot()
        .by_kind
        .get(&SqlCallKind::Execute)
        .map_or(0, |s| s.calls)
}

#[test]
fn exact_receipts_preserve_links_tempids_and_immutable_request_bindings() {
    let Some((scope, mut store)) = fixture("block_receipt_exact") else {
        return;
    };
    let receipt = example(&mut store);
    let id = receipt.put(&mut store).unwrap();
    assert_eq!(receipt.put(&mut store).unwrap(), id);
    let encoded = store.get(id).unwrap().unwrap();
    let block = Block::decode(&id, &encoded).unwrap();
    assert_eq!(block.kind, RECEIPT_KIND);
    assert_eq!(
        block.links,
        vec![receipt.before, receipt.after, receipt.transaction]
    );
    assert_eq!(ExactReceipt::load(&mut store, id).unwrap(), receipt);
    let key = scoped_request_key(&IDENTITY, "one").unwrap();
    assert_ne!(key, scoped_request_key(&[42; 16], "one").unwrap());
    assert_ne!(key, scoped_request_key(&IDENTITY, "two").unwrap());
    assert!(scoped_request_key(&IDENTITY, "").is_err());
    let empty = RequestIndex::empty();
    let index = empty.insert(&mut store, key, id).unwrap();
    let changed = ExactReceipt {
        request_digest: sha256(b"changed request"),
        ..receipt.clone()
    };
    let changed_id = changed.put(&mut store).unwrap();
    let context = OperationContext::new(OperationKind::Transaction);
    let guard = context.enter();
    assert_eq!(index.insert(&mut store, key, id).unwrap(), index);
    assert_eq!(
        index.insert(&mut store, key, changed_id).unwrap_err().code,
        "storage/request-key-exists"
    );
    assert_eq!(
        writes(&context),
        0,
        "matching/conflicting retries never change the index"
    );
    drop(guard);
    assert_eq!(empty.lookup(&mut store, key).unwrap(), None);
    drop(store);
    let config = PostgresConnectionConfig::plaintext(&scope.connection);
    let mut reopened = PgBlockStore::connect(&config).unwrap();
    let index = RequestIndex::from_root(index.root());
    assert_eq!(index.lookup(&mut reopened, key).unwrap(), Some(id));
    assert_eq!(ExactReceipt::load(&mut reopened, id).unwrap(), receipt);
    assert_eq!(
        reopened.list_refs(None, 10).unwrap().len(),
        1,
        "only the provider format ref exists; receipt writes never publish rows/refs"
    );
}

#[test]
fn receipt_index_is_selective_path_copy_and_insertion_order_independent() {
    let Some((_scope, mut store)) = fixture("block_receipt_scale") else {
        return;
    };
    let mut receipt = example(&mut store);
    let mut index = RequestIndex::empty();
    let mut entries = Vec::new();
    let mut retained = None;
    const RECEIPTS: u64 = 128;
    for n in 0..RECEIPTS {
        let key = scoped_request_key(&IDENTITY, &format!("request-{n}")).unwrap();
        receipt.request_digest = sha256(&n.to_be_bytes());
        let context = OperationContext::new(OperationKind::Transaction);
        let guard = context.enter();
        let id = receipt.put(&mut store).unwrap();
        index = index.insert(&mut store, key, id).unwrap();
        assert!(
            writes(&context) <= 24,
            "hashed-key updates must remain path-sized, not receipt-count-sized"
        );
        drop(guard);
        entries.push((key, id));
        if n == 63 {
            retained = Some(index);
        }
        if [63, RECEIPTS - 1].contains(&n) {
            let context = OperationContext::new(OperationKind::Query);
            let guard = context.enter();
            let (key, id) = entries[n as usize / 2];
            assert_eq!(index.lookup(&mut store, key).unwrap(), Some(id));
            assert_eq!(ExactReceipt::load(&mut store, id).unwrap().basis, 1);
            assert!(
                context.snapshot().sql_calls <= 20,
                "lookup should touch a bounded trie path and one receipt"
            );
            drop(guard);
        }
    }
    let retained = retained.unwrap();
    assert_eq!(
        retained.lookup(&mut store, entries[63].0).unwrap(),
        Some(entries[63].1)
    );
    assert_eq!(retained.lookup(&mut store, entries[64].0).unwrap(), None);
    let mut reversed = RequestIndex::empty();
    for (key, id) in entries[..64].iter().rev() {
        reversed = reversed.insert(&mut store, *key, *id).unwrap();
    }
    assert_eq!(
        reversed.root(),
        retained.root(),
        "canonical radix structure does not depend on insertion order"
    );
    let context = OperationContext::new(OperationKind::Query);
    let guard = context.enter();
    assert_eq!(
        index
            .lookup(&mut store, scoped_request_key(&IDENTITY, "absent").unwrap())
            .unwrap(),
        None
    );
    assert!(context.snapshot().sql_calls <= 20);
    drop(guard);

    entries.sort_unstable();
    let mut after = None;
    let mut enumerated = Vec::new();
    let mut pages = 0;
    loop {
        let context = OperationContext::new(OperationKind::Query);
        let guard = context.enter();
        let page = index.scan(&mut store, after, 73).unwrap();
        assert!(
            context.snapshot().sql_calls <= 180,
            "continuation must prune the already-enumerated prefix"
        );
        drop(guard);
        if page.is_empty() {
            break;
        }
        pages += 1;
        after = page.last().map(|(key, _)| *key);
        enumerated.extend(page);
    }
    assert_eq!(enumerated, entries);
    assert_eq!(pages, 2);
    assert!(
        index
            .scan(&mut store, Some([0xff; 32]), 1)
            .unwrap()
            .is_empty()
    );
    assert_eq!(
        index.scan(&mut store, None, 0).unwrap_err().code,
        "storage/request-index-limit"
    );
    let middle = entries.len() / 2;
    let (key, previous) = entries[middle];
    let occupied = store
        .put(b"privacy-safe occupied request decision")
        .unwrap();
    let context = OperationContext::new(OperationKind::Transaction);
    let guard = context.enter();
    let replaced = index.upsert(&mut store, key, occupied).unwrap();
    assert!(context.snapshot().sql_calls <= 40);
    assert!(writes(&context) <= 20);
    drop(guard);
    assert_eq!(index.lookup(&mut store, key).unwrap(), Some(previous));
    assert_eq!(replaced.lookup(&mut store, key).unwrap(), Some(occupied));
    assert_eq!(
        replaced.upsert(&mut store, key, occupied).unwrap(),
        replaced
    );
    assert_eq!(
        replaced.insert(&mut store, key, previous).unwrap_err().code,
        "storage/request-key-exists",
        "administrative replacement keeps the request key occupied"
    );
    assert_eq!(
        replaced
            .scan(&mut store, Some(entries[middle - 1].0), 1)
            .unwrap(),
        vec![(key, occupied)]
    );
}

#[test]
fn receipts_reject_missing_corrupt_and_noncanonical_reachable_content() {
    let Some((scope, mut store)) = fixture("block_receipt_integrity") else {
        return;
    };
    let receipt = example(&mut store);
    let id = receipt.put(&mut store).unwrap();
    let block = Block::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
    let mut invalid = block.clone();
    invalid.payload[56..64].copy_from_slice(&u64::MAX.to_be_bytes());
    let malformed = store.put(&invalid.encode().unwrap()).unwrap();
    assert_eq!(
        ExactReceipt::load(&mut store, malformed).unwrap_err().code,
        "storage/receipt-size"
    );
    let mut invalid = block.clone();
    invalid.payload[64..68].copy_from_slice(&u32::MAX.to_be_bytes());
    let malformed = store.put(&invalid.encode().unwrap()).unwrap();
    assert_eq!(
        ExactReceipt::load(&mut store, malformed).unwrap_err().code,
        "storage/receipt-tempids"
    );
    let mut invalid = block.clone();
    invalid.payload[72] = 0xff; // First UTF-8 tempid name, after count and length.
    let malformed = store.put(&invalid.encode().unwrap()).unwrap();
    assert_eq!(
        ExactReceipt::load(&mut store, malformed).unwrap_err().code,
        "storage/receipt-tempids"
    );
    let mut invalid = block.clone();
    // "transaction" follows "item"; changing it to "aransaction" makes
    // otherwise valid UTF-8 map entries out of canonical key order.
    invalid.payload[88] = b'a';
    let malformed = store.put(&invalid.encode().unwrap()).unwrap();
    assert_eq!(
        ExactReceipt::load(&mut store, malformed).unwrap_err().code,
        "storage/receipt-tempids"
    );
    for invalid_t in [0, receipt.basis + 1] {
        let mut invalid = block.clone();
        // The first mapping's entity follows the four-byte "item" name.
        invalid.payload[76..84].copy_from_slice(&t_to_tx(invalid_t).unwrap().to_be_bytes());
        let malformed = store.put(&invalid.encode().unwrap()).unwrap();
        assert_eq!(
            ExactReceipt::load(&mut store, malformed).unwrap_err().code,
            "storage/receipt-tempids",
            "receipt TX tempids must stay within 1..=basis"
        );
    }
    let mut invalid_receipt = receipt.clone();
    invalid_receipt.tempids.insert("invalid".into(), u64::MAX);
    let context = OperationContext::new(OperationKind::Transaction);
    let guard = context.enter();
    assert_eq!(
        invalid_receipt.put(&mut store).unwrap_err().code,
        "storage/receipt-tempids"
    );
    invalid_receipt.tempids.clear();
    invalid_receipt.basis = 0;
    assert_eq!(
        invalid_receipt.put(&mut store).unwrap_err().code,
        "storage/receipt-coordinate"
    );
    assert_eq!(writes(&context), 0, "invalid inputs fail before staging");
    drop(guard);

    let key_a = [0; 32];
    let key_b = [0x80; 32];
    let index = RequestIndex::empty()
        .insert(&mut store, key_a, id)
        .unwrap()
        .insert(&mut store, key_b, id)
        .unwrap();
    let root = index.root().unwrap();
    let mut branch = Block::decode(&root, &store.get(root).unwrap().unwrap()).unwrap();
    assert_eq!(branch.kind, REQUEST_BRANCH_KIND);
    branch.links.swap(0, 1);
    let malformed = store.put(&branch.encode().unwrap()).unwrap();
    assert_eq!(
        RequestIndex::from_root(Some(malformed))
            .lookup(&mut store, key_a)
            .unwrap_err()
            .code,
        "storage/request-index"
    );
    assert_eq!(store.remove_objects(&[branch.links[0]]).unwrap(), 1);
    assert_eq!(
        index.lookup(&mut store, key_b).unwrap_err().code,
        "storage/missing-object"
    );
    assert_eq!(
        index.lookup(&mut store, key_a).unwrap(),
        Some(id),
        "unrelated branches are not fetched"
    );

    let mut admin = Client::connect(&scope.connection, NoTls).unwrap();
    admin
        .execute(
            "UPDATE atomic_objects SET payload = payload || decode('00', 'hex') WHERE id=$1",
            &[&&id[..]],
        )
        .unwrap();
    assert_eq!(
        ExactReceipt::load(&mut store, id).unwrap_err().code,
        "storage/object-corrupt"
    );
    assert_eq!(store.remove_objects(&[id]).unwrap(), 1);
    assert_eq!(
        ExactReceipt::load(&mut store, id).unwrap_err().code,
        "storage/missing-object"
    );
}

#[test]
fn current_maximum_request_tempid_payload_is_chunked_without_a_lower_admission_cap() {
    let Some((_scope, mut store)) = fixture("block_receipt_chunks") else {
        return;
    };
    let mut receipt = example(&mut store);
    let entity = make_eid(atomic_core::USER_PARTITION, 1001).unwrap();
    receipt.tempids = (0..4)
        .map(|n| {
            let mut name = String::with_capacity(16 * 1024 * 1024 - 1024);
            name.push(char::from(b'a' + n));
            name.extend(std::iter::repeat_n('x', 16 * 1024 * 1024 - 1025));
            (name, entity)
        })
        .collect();
    // The current canonical transaction codec admits this payload. A receipt
    // must not introduce a smaller effective request limit during the cutover.
    let transaction = DurableTransaction {
        database_id: "boundary".into(),
        basis_t: 1,
        previous_hash: [0; 32],
        eidx_frontier: 1003,
        tempids: std::mem::take(&mut receipt.tempids),
        tx_data: vec![],
    };
    let encoded = encode_transaction(&transaction).unwrap();
    assert!(encoded.len() > MAX_RECEIPT_TEMPID_BYTES - 8192);
    drop(encoded);
    receipt.tempids = transaction.tempids;
    let started = Instant::now();
    let context = OperationContext::new(OperationKind::Transaction);
    let guard = context.enter();
    let id = receipt.put(&mut store).unwrap();
    assert_eq!(
        writes(&context),
        17,
        "sixteen chunks plus one receipt descriptor"
    );
    drop(guard);
    let block = Block::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
    assert_eq!(block.payload.len(), 64);
    assert_eq!(
        block.links.len(),
        19,
        "three exact basis/log links and sixteen chunks"
    );
    assert_eq!(ExactReceipt::load(&mut store, id).unwrap(), receipt);
    eprintln!(
        "BLOCK_RECEIPT_LARGE_SAMPLE tempids=4 chunks=16 complete_put_load_ms={}",
        started.elapsed().as_millis()
    );
    let mut wrong = block.clone();
    wrong.links[3] = receipt.before;
    let wrong = store.put(&wrong.encode().unwrap()).unwrap();
    assert_eq!(
        ExactReceipt::load(&mut store, wrong).unwrap_err().code,
        "storage/receipt-chunk"
    );
    assert_eq!(store.remove_objects(&[block.links[3]]).unwrap(), 1);
    assert_eq!(
        ExactReceipt::load(&mut store, id).unwrap_err().code,
        "storage/missing-object"
    );
}
