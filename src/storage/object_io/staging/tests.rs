use super::*;
use std::cell::RefCell;
use std::rc::Rc;

// A byte sink that records the actual boundary calls, not a SQL/GC simulator.
#[derive(Default)]
struct Recorded {
    values: BTreeMap<ObjectId, Vec<u8>>,
    batches: Vec<Vec<Vec<u8>>>,
    reads: Vec<ObjectId>,
    flushes: usize,
    fail_batch: Option<usize>,
    fail_flush: Option<usize>,
    wrong_ids: bool,
}

struct Objects(Rc<RefCell<Recorded>>);

impl ObjectReader for Objects {
    fn read_object(&mut self, id: ObjectId) -> Result<Vec<u8>, SemanticError> {
        let mut record = self.0.borrow_mut();
        record.reads.push(id);
        record.values.get(&id).cloned().ok_or_else(rejected)
    }
}

impl ObjectWriter for Objects {
    fn put_object(&mut self, bytes: &[u8]) -> Result<ObjectId, SemanticError> {
        Ok(self.put_objects(&[bytes])?[0])
    }

    fn put_objects(&mut self, bytes: &[&[u8]]) -> Result<Vec<ObjectId>, SemanticError> {
        validate_write_batch(bytes)?;
        let mut record = self.0.borrow_mut();
        record
            .batches
            .push(bytes.iter().map(|b| b.to_vec()).collect());
        if record.fail_batch == Some(record.batches.len()) {
            return Err(rejected());
        }
        let ids = bytes.iter().map(|b| sha256(b)).collect::<Vec<_>>();
        for (&id, &bytes) in ids.iter().zip(bytes) {
            record.values.insert(id, bytes.to_vec());
        }
        if record.wrong_ids {
            return Ok(vec![[0; 32]; ids.len()]);
        }
        Ok(ids)
    }

    fn flush_objects(&mut self) -> Result<(), SemanticError> {
        let mut record = self.0.borrow_mut();
        record.flushes += 1;
        if record.fail_flush == Some(record.flushes) {
            return Err(rejected());
        }
        Ok(())
    }
}

fn rejected() -> SemanticError {
    SemanticError::conflict("test/upload-rejected", "Injected object failure")
}

fn fixture() -> (Objects, Rc<RefCell<Recorded>>) {
    let record = Rc::new(RefCell::new(Recorded::default()));
    (Objects(Rc::clone(&record)), record)
}

#[test]
fn pending_reads_and_duplicate_ids_do_not_bypass_the_final_upload() {
    let (mut store, record) = fixture();
    let older = sha256(b"older");
    record.borrow_mut().values.insert(older, b"older".to_vec());
    let id = with_buffered_objects(&mut store, 128, 1024, |objects| {
        let id = objects.put_object(b"pending")?;
        assert_eq!(id, sha256(b"pending"));
        assert_eq!(objects.read_object(id)?, b"pending");
        assert_eq!(objects.put_object(b"pending")?, id);
        assert_eq!(objects.put_objects(&[b"pending", b"pending"])?, [id, id]);
        assert_eq!(objects.read_object(older)?, b"older");
        assert!(record.borrow().batches.is_empty());
        assert_eq!(record.borrow().reads, [older]);
        Ok(id)
    })
    .unwrap();
    let record = record.borrow();
    assert_eq!(record.values[&id], b"pending");
    assert_eq!(record.batches, [vec![b"pending".to_vec()]]);
    assert_eq!(record.flushes, 1);
}

#[test]
fn object_limit_flushes_complete_groups_and_releases_read_your_writes_to_provider() {
    let (mut store, record) = fixture();
    with_buffered_objects(&mut store, 2, 1024, |objects| {
        for bytes in [b"a", b"b", b"c", b"d", b"e"] {
            objects.put_object(bytes)?;
        }
        assert_eq!(record.borrow().batches.len(), 2);
        assert_eq!(objects.read_object(sha256(b"a"))?, b"a");
        assert_eq!(objects.read_object(sha256(b"e"))?, b"e");
        assert_eq!(record.borrow().reads, [sha256(b"a")]);
        Ok(())
    })
    .unwrap();
    let record = record.borrow();
    assert_eq!(
        record.batches.iter().map(Vec::len).collect::<Vec<_>>(),
        [2, 2, 1]
    );
    assert_eq!(record.flushes, 3);
    assert_eq!(record.values.len(), 5);
}

#[test]
fn production_object_bound_splits_129_distinct_objects_into_128_and_one() {
    let (mut store, record) = fixture();
    with_buffered_objects(
        &mut store,
        MAX_WRITE_BATCH_OBJECTS,
        TARGET_PENDING_BYTES,
        |objects| {
            for value in 0u16..129 {
                objects.put_object(&value.to_be_bytes())?;
            }
            Ok(())
        },
    )
    .unwrap();
    assert_eq!(
        record
            .borrow()
            .batches
            .iter()
            .map(Vec::len)
            .collect::<Vec<_>>(),
        [128, 1]
    );
    assert_eq!(record.borrow().values.len(), 129);
}

#[test]
fn byte_target_flushes_before_overflow_but_an_admitted_large_object_stands_alone() {
    let (mut store, record) = fixture();
    with_buffered_objects(&mut store, 128, 5, |objects| {
        for bytes in [b"aa".as_slice(), b"bbb", b"cccc", b"123456", b"d"] {
            objects.put_object(bytes)?;
        }
        Ok(())
    })
    .unwrap();
    let record = record.borrow();
    assert_eq!(
        record
            .batches
            .iter()
            .map(|b| b.iter().map(Vec::len).sum::<usize>())
            .collect::<Vec<_>>(),
        [5, 4, 6, 1]
    );
    assert_eq!(record.batches[2], [b"123456".to_vec()]);
    assert_eq!(record.values.len(), 5);
}

#[test]
fn build_error_and_unwind_never_flush_on_drop() {
    let (mut store, record) = fixture();
    let error = with_buffered_objects(&mut store, 2, 1024, |objects| {
        objects.put_object(b"discard")?;
        Err::<(), _>(rejected())
    })
    .unwrap_err();
    assert_eq!(error, rejected());
    assert!(record.borrow().batches.is_empty());
    assert_eq!(record.borrow().flushes, 0);
    let panic = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        let _ = with_buffered_objects(
            &mut store,
            2,
            1024,
            |objects| -> Result<(), SemanticError> {
                objects.put_object(b"discard-on-unwind")?;
                panic!("builder unwind");
            },
        );
    }));
    assert!(panic.is_err());
    assert!(record.borrow().batches.is_empty());
    assert_eq!(record.borrow().flushes, 0);
}

#[test]
fn build_error_after_one_group_leaves_only_that_group_without_a_final_barrier() {
    let (mut store, record) = fixture();
    assert_eq!(
        with_buffered_objects(&mut store, 1, 1024, |objects| {
            objects.put_object(b"first")?;
            objects.put_object(b"discard")?;
            Err::<(), _>(rejected())
        })
        .unwrap_err(),
        rejected()
    );
    let record = record.borrow();
    assert_eq!(record.batches, [vec![b"first".to_vec()]]);
    assert_eq!(record.flushes, 1);
    assert_eq!(record.values.len(), 1);
    assert!(!record.values.contains_key(&sha256(b"discard")));
}

#[test]
fn caught_multigroup_upload_failure_poisoning_prevents_prepared_success() {
    let (mut store, record) = fixture();
    record.borrow_mut().fail_batch = Some(2);
    let result = with_buffered_objects(&mut store, 1, 1024, |objects| {
        objects.put_object(b"durable-first")?;
        objects.put_object(b"failed-second")?;
        assert_eq!(objects.put_object(b"never-third").unwrap_err(), rejected());
        assert_eq!(
            objects.read_object(sha256(b"durable-first")).unwrap_err(),
            rejected()
        );
        assert_eq!(objects.put_object(b"never-fourth").unwrap_err(), rejected());
        assert_eq!(objects.flush_objects().unwrap_err(), rejected());
        Ok("must-not-escape")
    });
    assert_eq!(result.unwrap_err(), rejected());
    let record = record.borrow();
    assert_eq!(record.batches.len(), 2);
    assert_eq!(record.flushes, 1);
    assert_eq!(
        record.values,
        BTreeMap::from([(sha256(b"durable-first"), b"durable-first".to_vec())])
    );
}

#[test]
fn underlying_barrier_failure_is_sticky_even_after_the_upload_was_accepted() {
    let (mut store, record) = fixture();
    record.borrow_mut().fail_flush = Some(1);
    let result = with_buffered_objects(&mut store, 128, 1024, |objects| {
        objects.put_object(b"accepted-not-proven-durable")?;
        assert_eq!(objects.flush_objects().unwrap_err(), rejected());
        Ok("must-not-escape")
    });
    assert_eq!(result.unwrap_err(), rejected());
    assert_eq!(record.borrow().batches.len(), 1);
    assert_eq!(record.borrow().flushes, 1);
}

#[test]
fn wrong_provider_id_is_not_a_successful_barrier() {
    let (mut store, record) = fixture();
    record.borrow_mut().wrong_ids = true;
    let error = with_buffered_objects(&mut store, 128, 1024, |objects| {
        objects.put_object(b"content")
    })
    .unwrap_err();
    assert_eq!(error.code, "storage/staging-identity");
    assert_eq!(record.borrow().batches.len(), 1);
    assert_eq!(record.borrow().flushes, 0);
}
