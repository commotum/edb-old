//! Deterministic authority/CAS witnesses against fresh opaque PostgreSQL schemas.
use super::*;
use crate::storage::root::Block;
use crate::{IndexOrder, Schema};
use std::cell::RefCell;
use std::rc::Rc;
use std::sync::{Arc, Barrier};

type AfterPublishHook = Box<dyn FnOnce() -> Result<(), SemanticError>>;

thread_local! {
    static BEFORE:RefCell<Option<Box<dyn FnOnce()>>> = RefCell::new(None);
    static AFTER:RefCell<Option<AfterPublishHook>> = RefCell::new(None);
    static BEFORE_RENEW: RefCell<Option<Box<dyn FnOnce()>>> = RefCell::new(None);
}
pub(super) fn before_renew() {
    if let Some(hook) = BEFORE_RENEW.with(|slot| slot.borrow_mut().take()) {
        hook();
    }
}
pub(super) fn before_publish() {
    let hook = BEFORE.with(|slot| slot.borrow_mut().take());
    if let Some(hook) = hook {
        hook();
    }
}
pub(super) fn after_publish() -> Result<(), SemanticError> {
    let hook = AFTER.with(|slot| slot.borrow_mut().take());
    hook.map_or(Ok(()), |hook| hook())
}
struct Hooks;
impl Drop for Hooks {
    fn drop(&mut self) {
        BEFORE.with(|slot| {
            slot.borrow_mut().take();
        });
        AFTER.with(|slot| {
            slot.borrow_mut().take();
        });
        BEFORE_RENEW.with(|slot| {
            slot.borrow_mut().take();
        });
    }
}
fn before(hook: impl FnOnce() + 'static) -> Hooks {
    BEFORE.with(|slot| {
        assert!(slot.borrow_mut().replace(Box::new(hook)).is_none());
    });
    Hooks
}
fn after(hook: impl FnOnce() -> Result<(), SemanticError> + 'static) -> Hooks {
    AFTER.with(|slot| {
        assert!(slot.borrow_mut().replace(Box::new(hook)).is_none());
    });
    Hooks
}
fn before_renewal(hook: impl FnOnce() + 'static) -> Hooks {
    BEFORE_RENEW.with(|slot| {
        assert!(slot.borrow_mut().replace(Box::new(hook)).is_none());
    });
    Hooks
}

struct Fixture {
    admin: postgres::Client,
    schema: String,
    config: PostgresConnectionConfig,
    database: BlockDatabase,
}
impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}
fn fixture() -> Option<Fixture> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block engine PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("block_engine_fence_{:032x}", crate::uuid_v7().unwrap());
    let mut admin = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let scoped = if connection.starts_with("postgres://") || connection.starts_with("postgresql://")
    {
        format!(
            "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
            if connection.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{connection} options='-csearch_path={schema},pg_catalog'")
    };
    let config = PostgresConnectionConfig::plaintext(scoped);
    PgBlockStore::install(&config).unwrap();
    let database = BlockDatabase::create(&config, "fencing", Schema::new()).unwrap();
    Some(Fixture {
        admin,
        schema,
        config,
        database,
    })
}
fn applied(outcome: crate::storage::CasOutcome) -> Reference {
    match outcome {
        crate::storage::CasOutcome::Applied(reference) => reference,
        _ => panic!("fixture CAS conflicted"),
    }
}
fn root(store: &mut PgBlockStore, database: &BlockDatabase) -> (Reference, DatabaseRoot) {
    let reference = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let id = object_id(reference.value.as_deref().unwrap()).unwrap();
    let value =
        DatabaseRoot::decode_for_identity(&id, &required(store, id).unwrap(), &database.identity)
            .unwrap();
    (reference, value)
}
fn expire(config: &PostgresConnectionConfig, database: &BlockDatabase) {
    let mut store = PgBlockStore::connect(config).unwrap();
    let reference = store.read_ref(&database.lease_key()).unwrap().unwrap();
    let mut lease = Lease::decode(reference.value.as_deref().unwrap()).unwrap();
    lease.expires_ms = 0;
    applied(
        store
            .compare_exchange(
                &database.lease_key(),
                Some(reference.revision),
                Some(&lease.encode()),
            )
            .unwrap(),
    );
}
fn request(key: &str, instant: i64) -> TransactionRequest {
    TransactionRequest::new(key, Vec::new()).with_tx_instant(instant)
}
fn assert_same_receipt(a: &ServiceTransactionReport, b: &ServiceTransactionReport) {
    assert_eq!(a.basis_t, b.basis_t);
    assert_eq!(a.tx_hash, b.tx_hash);
    assert_eq!(a.tx_data, b.tx_data);
    assert_eq!(a.tempids, b.tempids);
    for (left, right) in [(&a.db_before, &b.db_before), (&a.db_after, &b.db_after)] {
        assert_eq!(left.basis_t(), right.basis_t());
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(left.datoms(order).unwrap(), right.datoms(order).unwrap());
            assert_eq!(
                left.clone().history().datoms(order).unwrap(),
                right.clone().history().datoms(order).unwrap()
            );
        }
    }
}

#[test]
fn zero_recent_datom_limit_cannot_publish_a_value_that_cannot_reopen() {
    let Some(fixture) = fixture() else {
        return;
    };
    let mut options = BlockWriterOptions::default();
    options.reads.max_recent_datoms = 0;
    let mut writer =
        BlockTransactor::claim(&fixture.config, fixture.database.clone(), options).unwrap();
    assert!(writer.transact(&request("zero-cap", 100)).is_err());
    let mut store = PgBlockStore::connect(&fixture.config).unwrap();
    assert_eq!(root(&mut store, &fixture.database).1.basis, 0);
    writer.release().unwrap();
}

#[test]
fn resident_successors_extend_only_new_datoms_and_receipts_bypass_index_gate() {
    let Some(fixture) = fixture() else {
        return;
    };
    let options = BlockWriterOptions {
        lease_duration: Duration::from_secs(60),
        ..Default::default()
    };
    let mut writer =
        BlockTransactor::claim(&fixture.config, fixture.database.clone(), options).unwrap();
    let first_request = request("resident-first", 100);
    let first = writer.transact(&first_request).unwrap();
    let old = writer.current.clone().unwrap();
    let schema = old.schema_arc();
    for n in 1..24 {
        let report = writer
            .transact(&request(&format!("resident-{n}"), 100 + n))
            .unwrap();
        let current = writer.current.as_ref().unwrap();
        let work = current.recent_tier().last_work();
        assert_eq!(
            work.bulk_rebuild_datoms, 0,
            "ordinary advancement must not reconstruct its prior tail"
        );
        assert_eq!(work.authenticated_datoms, report.tx_data.len() as u64);
        assert!(
            Arc::ptr_eq(&schema, &current.schema_arc()),
            "ordinary writes must share schema projections"
        );
        assert_eq!(old.basis_t(), first.basis_t);
    }
    let replay = writer
        .transact_with_fresh_gate(&first_request, || {
            panic!("matching receipt entered fresh gate")
        })
        .unwrap();
    assert_same_receipt(&replay, &first);
    let error = writer
        .transact_with_fresh_gate(&request("park", 200), || {
            Err(SemanticError::new(
                ErrorCategory::Busy,
                "service/index-backpressure",
                "test gate",
            ))
        })
        .unwrap_err();
    assert_eq!(error.code, "service/index-backpressure");
    assert_eq!(writer.current.as_ref().unwrap().basis_t(), 24);
    writer.release().unwrap();
}

#[test]
fn expired_lease_has_one_concurrent_claimant_and_stale_writer_can_only_replay() {
    let Some(f) = fixture() else {
        return;
    };
    let mut original =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let mut inspect = PgBlockStore::connect(&f.config).unwrap();
    let claimed = root(&mut inspect, &f.database).0;
    assert_eq!(
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .err()
            .unwrap()
            .code,
        "storage/writer-active"
    );
    assert_eq!(root(&mut inspect, &f.database).0, claimed);
    let intent = request("first", 10);
    let committed = original.transact(&intent).unwrap();
    expire(&f.config, &f.database);
    let gate = Arc::new(Barrier::new(2));
    let outcomes = std::thread::scope(|scope| {
        let mut workers = Vec::new();
        for _ in 0..2 {
            let config = f.config.clone();
            let database = f.database.clone();
            let gate = gate.clone();
            workers.push(scope.spawn(move || {
                gate.wait();
                BlockTransactor::claim(&config, database, BlockWriterOptions::default())
            }));
        }
        workers
            .into_iter()
            .map(|worker| worker.join().unwrap())
            .collect::<Vec<_>>()
    });
    let mut winners = Vec::new();
    for outcome in outcomes {
        match outcome {
            Ok(writer) => winners.push(writer),
            Err(error) => assert!(
                matches!(
                    error.category,
                    ErrorCategory::Busy | ErrorCategory::Conflict
                ),
                "{error}"
            ),
        }
    }
    assert_eq!(winners.len(), 1);
    let mut winner = winners.pop().unwrap();
    assert_eq!(winner.writer_epoch(), original.writer_epoch() + 1);
    let active = root(&mut inspect, &f.database);
    assert_eq!(active.1.basis, 1);
    assert_eq!(original.renew().unwrap_err().code, "storage/writer-fenced");
    let replay = original.transact(&intent).unwrap();
    assert!(replay.replayed);
    assert_same_receipt(&committed, &replay);
    assert_eq!(
        original
            .transact(&request("new-stale", 20))
            .unwrap_err()
            .code,
        "storage/writer-fenced"
    );
    assert_eq!(root(&mut inspect, &f.database).0, active.0);
    let lease = inspect.read_ref(&f.database.lease_key()).unwrap();
    assert_eq!(
        original.release().unwrap_err().code,
        "storage/writer-fenced"
    );
    assert_eq!(inspect.read_ref(&f.database.lease_key()).unwrap(), lease);
    let next = winner.transact(&request("winner", 20)).unwrap();
    assert_eq!(next.basis_t, 2);
    winner.release().unwrap();
}

#[test]
fn renewal_recaptures_root_only_changes_but_rejects_a_replacement_token() {
    let Some(f) = fixture() else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let config = f.config.clone();
    let database = f.database.clone();
    let hook = before_renewal(move || {
        let mut store = PgBlockStore::connect(&config).unwrap();
        let (reference, root) = root(&mut store, &database);
        let lease = store.read_ref(&database.lease_key()).unwrap();
        let result = crate::storage::ownership::publish_refs(
            &mut store,
            &[RefCondition {
                key: database.reference_key(),
                expected: Some(reference.revision),
            }],
            &[RefChange {
                key: database.reference_key(),
                value: reference.value,
            }],
        )
        .unwrap();
        assert!(matches!(result, BatchOutcome::Applied(_)));
        assert_eq!(store.read_ref(&database.lease_key()).unwrap(), lease);
        assert_eq!(root.basis, 0);
    });
    writer.renew().unwrap();
    drop(hook);
    assert_eq!(
        writer
            .transact(&request("after-root-only-renewal", 10))
            .unwrap()
            .basis_t,
        1
    );

    let replacement = Rc::new(RefCell::new(None));
    let destination = replacement.clone();
    let config = f.config.clone();
    let database = f.database.clone();
    let hook = before_renewal(move || {
        expire(&config, &database);
        *destination.borrow_mut() =
            Some(BlockTransactor::claim(&config, database, BlockWriterOptions::default()).unwrap());
    });
    assert_eq!(writer.renew().unwrap_err().code, "storage/writer-fenced");
    drop(hook);
    replacement.borrow_mut().take().unwrap().release().unwrap();
}

#[test]
fn reclaimed_prepared_index_is_a_gc_conflict_and_can_be_prepared_again() {
    use crate::storage::ownership::{BlockCollector, CollectionPhase};
    let Some(f) = fixture() else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let report = writer.transact(&request("before-index-gc", 10)).unwrap();
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let prepared = writer
        .index_input()
        .unwrap()
        .prepare(&mut store, &crate::index::tree::TreeConfig::default())
        .unwrap();
    let candidate = prepared.descriptor_id();
    let publication = root(&mut store, &f.database);
    assert_ne!(publication.1.indexes, Some(candidate));
    let mut collector = BlockCollector::connect(&f.config).unwrap();
    let mut complete = false;
    for _ in 0..64 {
        if collector.advance(Duration::ZERO, 4096).unwrap().phase == CollectionPhase::Complete {
            complete = true;
            break;
        }
    }
    assert!(complete);
    assert!(
        store.get(candidate).unwrap().is_none(),
        "unpublished output was physically reclaimed"
    );
    let error = writer.adopt_index(prepared).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Conflict);
    assert_eq!(error.code, "storage/index-publication-conflict");
    assert_eq!(root(&mut store, &f.database), publication);
    assert_eq!(report.db_after.basis_t(), 1);
    let prepared = writer
        .index_input()
        .unwrap()
        .prepare(&mut store, &crate::index::tree::TreeConfig::default())
        .unwrap();
    assert_eq!(writer.adopt_index(prepared).unwrap().indexed_basis, 1);
    assert_eq!(
        writer
            .transact(&request("after-index-gc", 20))
            .unwrap()
            .basis_t,
        2
    );
    writer.release().unwrap();
}

#[test]
fn transaction_upload_barrier_precedes_publication_and_exposes_both_receipt_paths() {
    let Some(f) = fixture() else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let mut inspect = PgBlockStore::connect(&f.config).unwrap();
    let original = root(&mut inspect, &f.database);
    let intent = request("flushed-before-publication", 10);
    let request_key = scoped_request_key(&f.database.identity, &intent.request_key).unwrap();
    let digest = crate::encoding::canonical_submission_request(
        &intent.forms,
        intent.compare_basis_t,
        intent.tx_instant_override,
        MAX_LOG_TRANSACTION_BYTES,
    )
    .unwrap()
    .0;
    let config = f.config.clone();
    let database = f.database.clone();
    let observed = Rc::new(RefCell::new(None));
    let witness = Rc::clone(&observed);
    let _hook = before(move || {
        // An independent connection cannot read the transaction-local buffer.
        let mut store = PgBlockStore::connect(&config).unwrap();
        assert_eq!(root(&mut store, &database), original);
        let mut after = None;
        let mut candidates = Vec::new();
        loop {
            let ids = store.list_objects(after, 128).unwrap();
            for &id in &ids {
                let bytes = required(&mut store, id).unwrap();
                // Tree nodes/programs have their own canonical encodings;
                // only the ATOB family can contain a DatabaseRoot.
                if bytes.starts_with(b"ATOB")
                    && Block::decode(&id, &bytes).unwrap().kind
                        == crate::storage::root::DATABASE_ROOT_KIND
                {
                    let candidate = DatabaseRoot::decode(&id, &bytes).unwrap();
                    if candidate.identity == database.identity && candidate.basis == 1 {
                        candidates.push((id, candidate));
                    }
                }
            }
            if ids.len() < 128 {
                break;
            }
            after = ids.last().copied();
        }
        assert_eq!(candidates.len(), 1);
        let (candidate_id, candidate) = candidates.pop().unwrap();
        // Every outgoing object exists before the root reference is installed.
        let mut pending = vec![candidate_id];
        let mut visited = std::collections::BTreeSet::new();
        while let Some(id) = pending.pop() {
            if visited.insert(id) {
                let bytes = required(&mut store, id).unwrap();
                pending.extend(
                    crate::storage::ownership::object_children(&mut store, id, &bytes).unwrap(),
                );
            }
        }
        let requests = RequestIndex::from_root(candidate.receipts);
        let receipt_id = requests.lookup(&mut store, request_key).unwrap().unwrap();
        assert_eq!(
            requests
                .lookup(
                    &mut store,
                    crate::storage::receipts::basis_receipt_key(&database.identity, 1)
                )
                .unwrap(),
            Some(receipt_id)
        );
        let receipt = ExactReceipt::load(&mut store, receipt_id).unwrap();
        assert_eq!(receipt.request_digest, digest);
        assert_eq!(receipt.basis, 1);
        assert!(receipt.tempids.is_empty());
        for (id, expected_basis) in [(receipt.before, 0), (receipt.after, 1)] {
            assert_eq!(
                DatabaseValueRoot::decode(&id, &required(&mut store, id).unwrap())
                    .unwrap()
                    .basis,
                expected_basis
            );
        }
        let log = LogRoot::open(&mut store, candidate.log.unwrap()).unwrap();
        let entry = log.read_record(&mut store, 1).unwrap().unwrap();
        assert_eq!(entry.id, receipt.transaction);
        assert_eq!(entry.entry.basis_t, 1);
        *witness.borrow_mut() = Some((candidate_id, receipt.transaction));
    });
    let report = writer.transact(&intent).unwrap();
    let (candidate, transaction) = observed.borrow().expect("prepublication witness ran");
    assert_eq!(report.tx_hash, transaction);
    assert_eq!(
        root(&mut inspect, &f.database).0.value,
        Some(candidate.to_vec())
    );
    assert!(!report.replayed);
    assert_same_receipt(&report, &writer.transact(&intent).unwrap());
    writer.release().unwrap();
}

#[test]
fn gc_revision_change_at_final_cas_publishes_neither_transaction_nor_receipt() {
    let Some(f) = fixture() else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let mut inspect = PgBlockStore::connect(&f.config).unwrap();
    let original = root(&mut inspect, &f.database);
    let lease = inspect.read_ref(&f.database.lease_key()).unwrap();
    let config = f.config.clone();
    let _hook = before(move || {
        let mut store = PgBlockStore::connect(&config).unwrap();
        applied(
            store
                .compare_exchange(GC_REFERENCE, None, Some(&1u64.to_be_bytes()))
                .unwrap(),
        );
    });
    let intent = request("gc-fenced", 10);
    assert_eq!(
        writer.transact(&intent).unwrap_err().code,
        "storage/publication-conflict"
    );
    assert_eq!(root(&mut inspect, &f.database), original);
    assert_eq!(inspect.read_ref(&f.database.lease_key()).unwrap(), lease);
    assert!(writer.store.write_protection().is_none());
    assert!(
        RequestIndex::from_root(original.1.receipts)
            .lookup(
                &mut inspect,
                scoped_request_key(&f.database.identity, &intent.request_key).unwrap()
            )
            .unwrap()
            .is_none()
    );
    let committed = writer.transact(&intent).unwrap();
    assert!(!committed.replayed);
    assert_eq!(committed.basis_t, 1);
    let replay = writer.transact(&intent).unwrap();
    assert!(replay.replayed);
    assert_same_receipt(&committed, &replay);
    writer.release().unwrap();
}

#[test]
fn replacement_after_staging_fences_old_final_cas_without_consuming_request_key() {
    let Some(f) = fixture() else {
        return;
    };
    let mut old =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let replacement = Rc::new(RefCell::new(None));
    let destination = replacement.clone();
    let config = f.config.clone();
    let database = f.database.clone();
    let _hook = before(move || {
        expire(&config, &database);
        *destination.borrow_mut() =
            Some(BlockTransactor::claim(&config, database, BlockWriterOptions::default()).unwrap());
    });
    let intent = request("raced", 10);
    assert_eq!(
        old.transact(&intent).unwrap_err().code,
        "storage/writer-fenced"
    );
    let mut winner = replacement
        .borrow_mut()
        .take()
        .expect("replacement ran at final CAS seam");
    let mut inspect = PgBlockStore::connect(&f.config).unwrap();
    let state = root(&mut inspect, &f.database).1;
    assert_eq!(state.basis, 0);
    assert!(state.log.is_none());
    assert!(state.receipts.is_none());
    assert_eq!(state.writer_epoch, winner.writer_epoch());
    let committed = winner.transact(&intent).unwrap();
    assert!(!committed.replayed);
    assert_eq!(committed.basis_t, 1);
    let stale_replay = old.transact(&intent).unwrap();
    assert!(stale_replay.replayed);
    assert_same_receipt(&committed, &stale_replay);
    winner.release().unwrap();
}

#[test]
fn injected_lost_ack_is_resolved_from_the_durable_receipt_after_reclaim() {
    let Some(f) = fixture() else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let _hook = after(|| {
        Err(SemanticError::new(
            ErrorCategory::UnknownOutcome,
            "test/lost-ack",
            "Injected caller acknowledgement loss after successful publication",
        ))
    });
    let intent = request("lost-ack", 10);
    let prior_lease_revision = writer.lease_revision;
    let error = writer.transact(&intent).unwrap_err();
    assert_eq!(error.category, ErrorCategory::UnknownOutcome);
    assert_eq!(
        writer.lease_revision, prior_lease_revision,
        "fault precedes local publication acknowledgement"
    );
    let mut inspect = PgBlockStore::connect(&f.config).unwrap();
    let committed = root(&mut inspect, &f.database).1;
    assert_eq!(committed.basis, 1);
    assert!(committed.receipts.is_some());
    assert!(writer.store.write_protection().is_none());
    let exact = writer.transact(&intent).unwrap();
    assert!(exact.replayed);
    assert_eq!(exact.basis_t, 1);
    let next = writer.transact(&request("after-lost-ack", 20)).unwrap();
    assert!(!next.replayed);
    assert_eq!(
        next.basis_t, 2,
        "fresh request refreshes only the still-owned lease revision"
    );
    writer.release().unwrap();
    let mut recovered =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let before = root(&mut inspect, &f.database).0;
    let replay = recovered.transact(&intent).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, 1);
    assert_same_receipt(&exact, &replay);
    assert_eq!(
        root(&mut inspect, &f.database).0,
        before,
        "receipt retry does not republish the transaction"
    );
    assert_eq!(
        recovered
            .transact(&request("lost-ack", 11))
            .unwrap_err()
            .code,
        "postgres/idempotency-key-reused"
    );
    assert_eq!(root(&mut inspect, &f.database).0, before);
    recovered.release().unwrap();
}

#[test]
fn concurrent_name_creation_never_replaces_an_active_winning_identity() {
    let Some(f) = fixture() else {
        return;
    };
    let gate = Arc::new(Barrier::new(2));
    let outcomes = std::thread::scope(|scope| {
        let mut workers = Vec::new();
        for _ in 0..2 {
            let config = f.config.clone();
            let gate = gate.clone();
            workers.push(scope.spawn(move || {
                gate.wait();
                BlockDatabase::create(&config, "same-name", Schema::new())
            }));
        }
        workers
            .into_iter()
            .map(|worker| worker.join().unwrap())
            .collect::<Vec<_>>()
    });
    let resolved = BlockDatabase::resolve(&f.config, "same-name").unwrap();
    let mut successes = 0;
    for outcome in outcomes {
        match outcome {
            Ok(database) => {
                successes += 1;
                assert_eq!(
                    database, resolved,
                    "every successful creator must observe the same active identity"
                );
            }
            Err(error) => assert_eq!(error.category, ErrorCategory::Conflict, "{error}"),
        }
    }
    assert!(successes >= 1);
    let mut inspect = PgBlockStore::connect(&f.config).unwrap();
    let published = root(&mut inspect, &resolved);
    assert_eq!(published.1.basis, 0);
    assert_eq!(
        BlockDatabase::create(&f.config, "same-name", Schema::new()).unwrap(),
        resolved
    );
    assert_eq!(
        root(&mut inspect, &resolved),
        published,
        "idempotent create cannot reset an existing root"
    );
}
