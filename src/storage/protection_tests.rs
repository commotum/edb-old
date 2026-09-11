//! Current publication guards, read-only capture and immutable observation.
use super::*;
use crate::storage::{CasOutcome, Guarded, WriteProtection};
use std::cell::RefCell;

struct ReferenceReadHook {
    key: String,
    remaining: usize,
    action: Box<dyn FnMut()>,
}
thread_local! {
    static AFTER_REFERENCE_READ: RefCell<Option<ReferenceReadHook>> = const { RefCell::new(None) };
}
pub(super) fn after_reference_read(key: &str) {
    let hook = AFTER_REFERENCE_READ.with(|slot| {
        let mut slot = slot.borrow_mut();
        if slot.as_ref().is_some_and(|hook| hook.key == key) {
            slot.take()
        } else {
            None
        }
    });
    if let Some(mut hook) = hook {
        hook.remaining -= 1;
        (hook.action)();
        if hook.remaining != 0 {
            AFTER_REFERENCE_READ.with(|slot| *slot.borrow_mut() = Some(hook));
        }
    }
}
struct ReferenceHook;
impl Drop for ReferenceHook {
    fn drop(&mut self) {
        AFTER_REFERENCE_READ.with(|slot| slot.borrow_mut().take());
    }
}
fn after_read(key: &str, remaining: usize, action: impl FnMut() + 'static) -> ReferenceHook {
    assert!(remaining > 0);
    AFTER_REFERENCE_READ.with(|slot| {
        assert!(
            slot.borrow_mut()
                .replace(ReferenceReadHook {
                    key: key.into(),
                    remaining,
                    action: Box::new(action),
                })
                .is_none()
        );
    });
    ReferenceHook
}

pub(super) struct Fixture {
    pub(super) admin: postgres::Client,
    pub(super) schema: String,
    pub(super) config: PostgresConnectionConfig,
}
impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}
pub(super) fn fixture() -> Option<Fixture> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block protection PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("block_protection_{:032x}", crate::uuid_v7().unwrap());
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
    let fixture = Fixture {
        admin,
        schema,
        config: PostgresConnectionConfig::plaintext(scoped),
    };
    PgBlockStore::install(&fixture.config).unwrap();
    Some(fixture)
}
fn guard(key: &str, expected: Option<u64>) -> RefCondition {
    RefCondition {
        key: key.into(),
        expected,
    }
}
fn revision(outcome: CasOutcome) -> u64 {
    match outcome {
        CasOutcome::Applied(reference) => reference.revision,
        _ => panic!("fixture CAS conflict"),
    }
}

#[test]
fn ordinary_capture_fails_closed_when_a_published_covering_root_is_missing() {
    let Some(fixture) = fixture() else {
        return;
    };
    let database = crate::storage::BlockDatabase::create(
        &fixture.config,
        "missing-covering-root",
        Schema::new(),
    )
    .unwrap();
    let mut store = PgBlockStore::connect(&fixture.config).unwrap();
    let publication = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let id = publication.value.unwrap().try_into().unwrap();
    let root = DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
    let id = root.indexes.unwrap();
    let indexes = IndexDescriptor::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
    assert_eq!(
        store.remove_objects(&[indexes.trees[0].root_hash]).unwrap(),
        1
    );
    let reader = BlockReader::connect(&fixture.config, BlockReadConfig::default()).unwrap();
    let error = reader.capture(&database.reference_key()).unwrap_err();
    assert_eq!(error.code, "storage/missing-object");
    assert_eq!(error.category, ErrorCategory::Fault);
}

#[test]
fn one_reader_shares_one_connection_and_cache_across_eighty_captured_values() {
    let Some(fixture) = fixture() else {
        return;
    };
    let database =
        crate::storage::BlockDatabase::create(&fixture.config, "shared-captures", Schema::new())
            .unwrap();
    let operation = crate::OperationContext::new(crate::OperationKind::Application);
    let _scope = operation.enter();
    let reader = BlockReader::connect(&fixture.config, BlockReadConfig::default()).unwrap();
    let captured = (0..80)
        .map(|_| reader.capture(&database.reference_key()).unwrap())
        .collect::<Vec<_>>();
    assert_eq!(operation.snapshot().connect_calls, 1);
    let first = captured[0].database_value();
    let last = captured[79].database_value();
    assert_eq!(first.snapshot_key().unwrap(), last.snapshot_key().unwrap());
    assert_eq!(
        first.collect_datoms(IndexOrder::Eavt).unwrap(),
        last.collect_datoms(IndexOrder::Eavt).unwrap()
    );
    assert!(captured[79].cache_stats().hits > 0);
    drop((first, last, reader));
    for snapshot in captured {
        drop(snapshot);
    }
}

#[test]
fn exact_read_reopening_retains_observed_publication_without_reader_coordination() {
    enum Observation {
        Report,
        Reference,
        Inspection,
        Backup,
        Outcome,
    }
    for observation in [
        Observation::Report,
        Observation::Reference,
        Observation::Inspection,
        Observation::Backup,
        Observation::Outcome,
    ] {
        let Some(fixture) = fixture() else {
            return;
        };
        let database = crate::storage::BlockDatabase::create(
            &fixture.config,
            "exact-read-race",
            Schema::new(),
        )
        .unwrap();
        let mut writer = crate::storage::BlockTransactor::claim(
            &fixture.config,
            database.clone(),
            crate::storage::BlockWriterOptions::default(),
        )
        .unwrap();
        let request = crate::TransactionRequest::new(
            "retained-report",
            vec![crate::TxOp::Add {
                entity: crate::EntityRef::Temp("item".into()),
                attribute: crate::DB_DOC as u32,
                value: crate::Value::String("exact original value".into()).into(),
            }],
        );
        let report = writer.transact(&request).unwrap();
        let reader = BlockReader::connect(&fixture.config, BlockReadConfig::default()).unwrap();
        let view = report.db_after.clone().history().as_of(report.basis_t);
        let reference =
            crate::SnapshotReference::decode(&view.snapshot_reference().unwrap().encode().unwrap())
                .unwrap();
        let expected_facts = view.collect_datoms(IndexOrder::Eavt).unwrap();
        let source_indexes = report
            .db_after
            .committed_block_parts()
            .unwrap()
            .0
            .captured_root()
            .indexes;
        let expected_basis = report.basis_t;
        let config = fixture.config.clone();
        let route = crate::storage::engine::identity_string(database.route);
        let hook_route = route.clone();
        let advanced = std::rc::Rc::new(std::cell::Cell::new(false));
        let advanced_hook = advanced.clone();
        let _hook = after_read(&database.reference_key(), 1, move || {
            // Publish real covering trees at the same logical basis between
            // the reader's current-ref lookup and opening that immutable value.
            let receipt = crate::PostgresOperator::connect_configured(&config)
                .unwrap()
                .consolidate_database(&hook_route)
                .unwrap();
            assert_eq!(receipt.basis_t, expected_basis);
            assert_ne!(Some(receipt.descriptor_id), source_indexes);
            advanced_hook.set(true);
        });
        let assert_report = |reopened: crate::ServiceTransactionReport| {
            assert_eq!(reopened.basis_t, report.basis_t);
            assert_eq!(reopened.tx_hash, report.tx_hash);
            assert_eq!(reopened.tempids, report.tempids);
            assert_eq!(reopened.tx_data, report.tx_data);
            assert_eq!(
                reopened.db_before.snapshot_key().unwrap(),
                report.db_before.snapshot_key().unwrap()
            );
            assert_eq!(
                reopened.db_after.snapshot_key().unwrap(),
                report.db_after.snapshot_key().unwrap()
            );
        };
        match observation {
            Observation::Report => assert_report(
                reader
                    .exact_report(&database.reference_key(), report.basis_t)
                    .unwrap(),
            ),
            Observation::Reference => {
                let reopened = reference.open_with_reader(&reader).unwrap();
                assert_eq!(
                    reopened.snapshot_key().unwrap(),
                    view.snapshot_key().unwrap()
                );
                assert_eq!(
                    reopened.collect_datoms(IndexOrder::Eavt).unwrap(),
                    expected_facts
                );
            }
            Observation::Inspection => {
                let inspected = crate::PostgresOperator::connect_configured(&fixture.config)
                    .unwrap()
                    .inspect_database(&route, false)
                    .unwrap();
                assert!(inspected.problems.is_empty());
                assert_eq!(inspected.database_id, route);
                assert_eq!(inspected.metrics.basis_t, report.basis_t);
                assert!(inspected.metrics.index_basis_t <= report.basis_t);
                assert_eq!(
                    inspected.metrics.index_lag,
                    report.basis_t - inspected.metrics.index_basis_t
                );
            }
            Observation::Backup => {
                let directory = tempfile::tempdir().unwrap();
                let repository = directory.path().join("repository");
                let point = crate::PortableBackup::connect_configured(&fixture.config)
                    .unwrap()
                    .backup_database(&database.name, &repository)
                    .unwrap();
                assert_eq!(point.basis_t, report.basis_t);
                assert_eq!(
                    point.lineage_id,
                    crate::storage::engine::identity_string(database.identity)
                );
                assert_eq!(point.log_generation, 0);
                let reopened = crate::BackupConnection::open(&repository).unwrap().db();
                // Offline values do not issue live portable references. Their
                // published point supplies lineage/generation authority.
                assert_eq!(reopened.basis_t(), report.basis_t);
                assert_eq!(
                    reopened.history().collect_datoms(IndexOrder::Eavt).unwrap(),
                    expected_facts
                );
            }
            Observation::Outcome => {
                let (digest, _) = crate::encoding::canonical_submission_request(
                    &request.forms,
                    request.compare_basis_t,
                    request.tx_instant_override,
                    crate::storage::log::MAX_LOG_TRANSACTION_BYTES,
                )
                .unwrap();
                let reopened = writer
                    .resolve_request_outcome(&request.request_key, digest)
                    .unwrap()
                    .unwrap();
                assert!(reopened.replayed);
                assert_report(reopened);
            }
        }
        assert!(advanced.get());
        writer.release().unwrap();
    }
}

#[test]
fn configured_put_refreshes_reused_objects_and_lost_guards_insert_nothing() {
    let Some(fixture) = fixture() else {
        return;
    };
    let mut store = PgBlockStore::connect(&fixture.config).unwrap();
    let existing = store.put(b"preexisting orphan").unwrap();
    let writer = revision(
        store
            .compare_exchange("writer", None, Some(b"opaque writer"))
            .unwrap(),
    );
    let protection = WriteProtection {
        epoch: 7,
        conditions: vec![guard("writer", Some(writer)), guard("system/gc", None)],
    };
    store
        .set_write_protection(Some(protection.clone()))
        .unwrap();
    assert_eq!(store.put(b"preexisting orphan").unwrap(), existing);
    let fresh = store.put(b"fresh protected").unwrap();
    assert_eq!(
        store
            .remove_unprotected(&[existing, fresh], 7, &[guard("writer", Some(writer))])
            .unwrap(),
        Guarded::Applied(0)
    );
    for invalid in [
        WriteProtection {
            epoch: u64::MAX,
            conditions: protection.conditions.clone(),
        },
        WriteProtection {
            epoch: 1,
            conditions: vec![],
        },
        WriteProtection {
            epoch: 1,
            conditions: vec![guard("writer", Some(writer)), guard("writer", Some(writer))],
        },
    ] {
        assert!(store.set_write_protection(Some(invalid)).is_err());
        assert_eq!(
            store.write_protection(),
            Some(&protection),
            "failed replacement must preserve the old authority"
        );
    }
    revision(
        store
            .compare_exchange("system/gc", None, Some(b"opaque GC epoch"))
            .unwrap(),
    );
    for payload in [
        b"must not be inserted".as_slice(),
        b"preexisting orphan".as_slice(),
    ] {
        let error = store.put(payload).unwrap_err();
        assert_eq!(error.category, ErrorCategory::Conflict);
        assert_eq!(error.code, "storage/write-protection-conflict");
    }
    assert!(
        store
            .get(crate::sha256(b"must not be inserted"))
            .unwrap()
            .is_none()
    );
    let current = WriteProtection {
        epoch: 8,
        conditions: vec![guard("writer", Some(writer)), guard("system/gc", Some(1))],
    };
    store.set_write_protection(Some(current)).unwrap();
    revision(
        store
            .compare_exchange("writer", Some(writer), Some(b"replacement writer"))
            .unwrap(),
    );
    assert_eq!(
        store.put(b"stale writer").unwrap_err().category,
        ErrorCategory::Conflict
    );
    assert!(store.get(crate::sha256(b"stale writer")).unwrap().is_none());
    store.set_write_protection(None).unwrap();
    assert_eq!(
        store.put(b"explicit fixture write").unwrap(),
        crate::sha256(b"explicit fixture write")
    );
    assert_eq!(
        store
            .remove_unprotected(&[existing, fresh], 8, &[guard("writer", Some(writer + 1))])
            .unwrap(),
        Guarded::Applied(2),
        "failed protected writes did not raise the stored epoch"
    );
}
