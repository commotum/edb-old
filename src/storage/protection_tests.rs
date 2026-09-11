//! Scoped actual-store witnesses for attempt protection and receipt-first pins.
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
fn capture_guards_are_bounded_consistent_and_never_gc_only() {
    assert!(normalize_capture_conditions(&[]).is_err());
    assert_eq!(
        normalize_capture_conditions(&[guard("system/gc", None)])
            .unwrap_err()
            .code,
        "storage/capture-source-guard"
    );
    assert_eq!(
        normalize_capture_conditions(&[guard("database", Some(1)), guard("database", Some(2))])
            .unwrap_err()
            .category,
        ErrorCategory::Conflict
    );
    let same = [
        guard("database", Some(1)),
        guard("system/gc", None),
        guard("system/gc", None),
    ];
    assert_eq!(normalize_capture_conditions(&same).unwrap().len(), 2);
    let crowded = (0..127)
        .map(|n| guard(&format!("reference/{n}"), None))
        .collect::<Vec<_>>();
    assert_eq!(
        normalize_capture_conditions(&crowded).unwrap_err().code,
        "storage/capture-guard-limit"
    );
    assert!(normalize_capture_conditions(&[guard("database", Some(0))]).is_err());
}

#[test]
fn exact_read_reopening_retries_index_publication_without_changing_its_requested_value() {
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
            // the reader's current-ref lookup and its guarded pin admission.
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
                assert_eq!(inspected.metrics.index_basis_t, report.basis_t);
                assert_eq!(inspected.metrics.index_lag, 0);
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
fn current_reference_observation_retry_is_bounded_and_explicit_capture_stays_strict() {
    let Some(fixture) = fixture() else {
        return;
    };
    let mut store = PgBlockStore::connect(&fixture.config).unwrap();
    let id = store.put(b"opaque capture: no value decoding yet").unwrap();
    revision(
        store
            .compare_exchange("observation", None, Some(&id))
            .unwrap(),
    );
    let reader = BlockReader::connect(&fixture.config, BlockReadConfig::default()).unwrap();
    for strict in [true, false] {
        let mut publisher = PgBlockStore::connect(&fixture.config).unwrap();
        let attempts = std::rc::Rc::new(std::cell::Cell::new(0));
        let attempts_hook = attempts.clone();
        let _hook = after_read("observation", 3, move || {
            let previous = publisher.read_ref("observation").unwrap().unwrap();
            revision(
                publisher
                    .compare_exchange("observation", Some(previous.revision), Some(&id))
                    .unwrap(),
            );
            attempts_hook.set(attempts_hook.get() + 1);
        });
        let result = if strict {
            reader.pin_reference("observation")
        } else {
            reader.pin_current_reference("observation")
        };
        assert_eq!(result.unwrap_err().code, "storage/capture-conflict");
        assert_eq!(attempts.get(), if strict { 1 } else { 3 });
    }
    assert_eq!(
        reader
            .pin_current_reference("missing-reference")
            .unwrap_err()
            .code,
        "storage/reference-not-found",
        "terminal errors are not converted into capture contention"
    );
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

#[test]
fn root_capture_precedes_decoding_and_exact_values_use_its_stable_pin() {
    let Some(fixture) = fixture() else {
        return;
    };
    let mut store = PgBlockStore::connect(&fixture.config).unwrap();
    let malformed = store.put(b"not a database descriptor").unwrap();
    revision(
        store
            .compare_exchange("database", None, Some(&malformed))
            .unwrap(),
    );
    let reader = BlockReader::connect(
        &fixture.config,
        BlockReadConfig {
            max_recent_transactions: 0,
            ..BlockReadConfig::default()
        },
    )
    .unwrap();
    let captured = reader.pin_reference("database").unwrap();
    assert_eq!(captured.root_id(), malformed);
    assert_eq!(captured.source_condition(), guard("database", Some(1)));
    assert_eq!(captured.source_revision(), 1);
    assert_eq!(
        reader.read_stats().object_reads,
        0,
        "receipt authority capture cannot load current database objects"
    );
    let db = crate::Database::bootstrap().unwrap();
    let identity = [17; 16];
    let mut trees = Vec::new();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let tree = crate::persistent_tree::build_tree(
                order,
                history,
                db.datoms(
                    if history {
                        crate::View::History
                    } else {
                        crate::View::Current
                    },
                    order,
                ),
                &crate::persistent_tree::TreeConfig::default(),
            )
            .unwrap();
            for (hash, bytes) in tree.nodes.iter() {
                assert_eq!(store.put(bytes).unwrap(), *hash);
            }
            trees.push(tree.descriptor);
        }
    }
    let indexes = store
        .put(
            &IndexDescriptor {
                identity,
                basis: 0,
                generation: 0,
                trees,
                pending_avet: vec![],
                avet_work: vec![],
                fulltext: None,
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let metadata = store
        .put(
            &SnapshotMetadata {
                identity,
                basis: 0,
                generation: 0,
                eidx_frontier: db.eidx_frontier(),
                reserved_frontier: crate::INITIAL_EIDX_FRONTIER,
                last_tx_instant: None,
                excision: None,
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let valid = store
        .put(
            &DatabaseValueRoot {
                identity,
                basis: 0,
                log: None,
                indexes: Some(indexes),
                metadata: Some(metadata),
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    revision(
        store
            .compare_exchange("database", Some(1), Some(&valid))
            .unwrap(),
    );
    // The source revision is stale, but the retained pin is stable authority.
    assert!(
        reader.capture_root(&captured).is_err(),
        "malformed original root stays malformed; newer head cannot replace it"
    );
    assert_eq!(
        store
            .read_ref(&captured.condition().key)
            .unwrap()
            .unwrap()
            .revision,
        1
    );
    let valid_capture = reader.pin_reference("database").unwrap();
    revision(
        store
            .compare_exchange("database", Some(2), Some(&malformed))
            .unwrap(),
    );
    let old = reader.capture_root(&valid_capture).unwrap();
    assert_eq!(old.basis_t(), 0);
    valid_capture.release().unwrap();
    assert_eq!(
        old.database_value().datoms(IndexOrder::Eavt).unwrap(),
        db.datoms(crate::View::Current, IndexOrder::Eavt)
    );
    old.release().unwrap();
    let before = store.list_live_refs("pins/read/", None, 128).unwrap();
    revision(
        store
            .compare_exchange("system/gc", None, Some(&1u64.to_be_bytes()))
            .unwrap(),
    );
    let error = reader
        .capture_immutable(valid, &[captured.condition(), guard("system/gc", None)])
        .err()
        .unwrap();
    assert_eq!(error.code, "storage/capture-gc-conflict");
    assert!(reader.capture_immutable(valid, &[]).is_err());
    let after = store.list_live_refs("pins/read/", None, 128).unwrap();
    assert_eq!(
        after.len(),
        before.len(),
        "stale/unguarded captures created no pins"
    );
    let clone = captured.clone();
    assert_eq!(
        captured.release().unwrap_err().category,
        ErrorCategory::Busy
    );
    clone.release().unwrap();
}
