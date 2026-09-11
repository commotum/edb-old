use super::*;
use crate::storage::{BlockDatabase, BlockTransactor, BlockWriterOptions};
use crate::{
    Attribute, Cardinality, Keyword, PostgresConnectionConfig, Schema, TransactionRequest,
    ValueType,
};

struct Fixture {
    admin: postgres::Client,
    schema: String,
    config: PostgresConnectionConfig,
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
        eprintln!("SKIP read authorization PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("read_authorization_{:032x}", crate::uuid_v7().unwrap());
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
    Some(Fixture {
        admin,
        schema,
        config,
    })
}
fn root(store: &mut PgBlockStore, database: &BlockDatabase) -> DatabaseRoot {
    let reference = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let id = reference.value.unwrap().try_into().unwrap();
    DatabaseRoot::decode(&id, &required(store, id).unwrap()).unwrap()
}
fn report_value(report: &crate::ServiceTransactionReport) -> DatabaseValueRoot {
    report
        .db_after
        .committed_block_parts()
        .unwrap()
        .0
        .captured_root()
        .clone()
}
fn commit(
    writer: &mut BlockTransactor,
    key: &str,
    instant: i64,
) -> crate::ServiceTransactionReport {
    writer
        .transact(&TransactionRequest::new(key, vec![]).with_tx_instant(instant))
        .unwrap()
}

#[test]
fn authorization_codec_is_fixed_bounded_and_domain_separated() {
    let descriptor = ReadAuthorization {
        identity: [7; 16],
        initial_basis: 1,
        initial_value: [8; 32],
        indexes: [9; 32],
    };
    let bytes = descriptor.encode().unwrap();
    let id = crate::sha256(&bytes);
    assert_eq!(bytes.len(), ENCODED_BYTES);
    assert_eq!(ReadAuthorization::decode(&id, &bytes).unwrap(), descriptor);
    let block = Block::decode(&id, &bytes).unwrap();
    assert_eq!(block.links, vec![[8; 32], [9; 32]]);
    for n in 0..bytes.len() {
        assert!(ReadAuthorization::decode(&crate::sha256(&bytes[..n]), &bytes[..n]).is_err());
    }
    let mut corrupt = bytes.clone();
    corrupt[20] ^= 1;
    assert_eq!(
        ReadAuthorization::decode(&id, &corrupt).unwrap_err().code,
        "storage/block-hash"
    );
    let mut wrong = block;
    wrong.kind = super::super::receipts::RECEIPT_KIND;
    let bytes = wrong.encode().unwrap();
    assert!(ReadAuthorization::decode(&crate::sha256(&bytes), &bytes).is_err());
    assert_ne!(index_key(&[7; 16], [9; 32]), index_key(&[8; 16], [9; 32]));
    assert_ne!(index_key(&[7; 16], [9; 32]), basis_receipt_key(&[7; 16], 9));

    let witness = IndexAuthorization {
        identity: [7; 16],
        index: [9; 32],
        published_ms: 17,
    };
    let bytes = witness.encode().unwrap();
    let id = crate::sha256(&bytes);
    assert_eq!(bytes.len(), 76);
    assert_eq!(IndexAuthorization::decode(&id, &bytes).unwrap(), witness);
    assert!(ReadAuthorization::decode(&id, &bytes).is_err());
    for n in 0..bytes.len() {
        assert!(IndexAuthorization::decode(&crate::sha256(&bytes[..n]), &bytes[..n]).is_err());
    }
    let mut wrong = Block::decode(&id, &bytes).unwrap();
    wrong.kind = READ_AUTHORIZATION_KIND;
    let bytes = wrong.encode().unwrap();
    assert!(IndexAuthorization::decode(&crate::sha256(&bytes), &bytes).is_err());
}

#[test]
fn selective_registry_remove_preserves_prior_roots_and_collapses_singletons() {
    let Some(f) = fixture() else {
        return;
    };
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let keys = [0u8, 1, 127, 128, 255].map(|n| [n; 32]);
    let mut registry = RequestIndex::empty();
    for key in keys {
        registry = registry.insert(&mut store, key, key).unwrap();
    }
    let original = registry;
    assert_eq!(
        registry.remove(&mut store, [3; 32]).unwrap().root(),
        registry.root()
    );
    for (i, key) in keys.into_iter().enumerate() {
        registry = registry.remove(&mut store, key).unwrap();
        assert_eq!(registry.lookup(&mut store, key).unwrap(), None);
        assert_eq!(original.lookup(&mut store, key).unwrap(), Some(key));
        assert_eq!(registry.scan(&mut store, None, 10).unwrap().len(), 4 - i);
    }
    assert_eq!(registry.root(), None);
}

#[test]
fn registry_batch_matches_sequential_updates_without_intermediate_writes() {
    let Some(f) = fixture() else {
        return;
    };
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let entries = (0u8..64)
        .map(|n| ([n; 32], Some([n.wrapping_add(1); 32])))
        .collect::<Vec<_>>();
    let original = RequestIndex::empty()
        .apply_batch(&mut store, &entries)
        .unwrap();
    let changes = (0u8..96)
        .filter(|n| n % 3 != 2)
        .map(|n| ([n; 32], (n % 3 != 0).then_some([n.wrapping_add(17); 32])))
        .collect::<Vec<_>>();
    let sequential_context = crate::OperationContext::new(crate::OperationKind::Transaction);
    let sequential = {
        let _scope = sequential_context.enter();
        let mut result = original;
        for &(key, value) in &changes {
            result = match value {
                Some(value) => result.upsert(&mut store, key, value).unwrap(),
                None => result.remove(&mut store, key).unwrap(),
            };
        }
        result
    };
    let batch_context = crate::OperationContext::new(crate::OperationKind::Transaction);
    let batch = {
        let _scope = batch_context.enter();
        original.apply_batch(&mut store, &changes).unwrap()
    };
    assert_eq!(
        batch.root(),
        sequential.root(),
        "canonical root is independent of batching"
    );
    for (key, value) in entries {
        assert_eq!(
            original.lookup(&mut store, key).unwrap(),
            value,
            "old root remains exact"
        );
    }
    let repeated_context = crate::OperationContext::new(crate::OperationKind::Transaction);
    {
        let _scope = repeated_context.enter();
        assert_eq!(
            batch.apply_batch(&mut store, &changes).unwrap().root(),
            batch.root()
        );
    }
    assert_eq!(repeated_context.snapshot().known_payload_write_bytes, 0);
    let sequential_io = sequential_context.snapshot();
    let batch_io = batch_context.snapshot();
    assert!(batch_io.known_payload_write_bytes < sequential_io.known_payload_write_bytes);
    eprintln!(
        "REQUEST_INDEX_BATCH entries=64 changes={} sequential_write_bytes={} batch_write_bytes={} sequential_sql={} batch_sql={}",
        changes.len(),
        sequential_io.known_payload_write_bytes,
        batch_io.known_payload_write_bytes,
        sequential_io.sql_calls,
        batch_io.sql_calls
    );
    assert_eq!(original.apply_batch(&mut store, &[]).unwrap(), original);
    for invalid in [
        vec![([1; 32], None), ([1; 32], None)],
        vec![([2; 32], None), ([1; 32], None)],
        vec![([1; 32], None); 4097],
    ] {
        assert_eq!(
            original.apply_batch(&mut store, &invalid).unwrap_err().code,
            "storage/request-index-batch"
        );
    }
    let erase = batch
        .scan(&mut store, None, 4096)
        .unwrap()
        .into_iter()
        .map(|(key, _)| (key, None))
        .collect::<Vec<_>>();
    assert_eq!(batch.apply_batch(&mut store, &erase).unwrap().root(), None);
}

#[test]
fn pruning_respects_age_shared_owners_and_atomic_new_owner_guards() {
    use crate::storage::ownership::{BlockCollector, CollectionPhase, publish_refs, settled_count};
    use crate::storage::{BatchOutcome, RefChange};
    use std::time::Duration;

    let Some(f) = fixture() else {
        return;
    };
    let database = BlockDatabase::create(&f.config, "prune", Schema::new()).unwrap();
    let key = database.reference_key();
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let mut publication = root(&mut store, &database);
    let initial_index = publication.indexes.unwrap();
    // A structural checkpoint is a distinct covering descriptor. Its actual
    // trees are retained from the fixture; this test exercises provenance
    // ownership, not the separate AVET projection algorithm.
    let mut checkpoint = IndexDescriptor::decode(
        &initial_index,
        &required(&mut store, initial_index).unwrap(),
    )
    .unwrap();
    checkpoint.pending_avet = vec![1000];
    let unused_index = store.put(&checkpoint.encode().unwrap()).unwrap();
    let authorization = ReadAuthorization::retain_index(
        &mut store,
        publication.read_authorization.unwrap(),
        &publication.identity,
        unused_index,
    )
    .unwrap();
    assert_eq!(
        ReadAuthorization::retain_index(
            &mut store,
            authorization,
            &publication.identity,
            unused_index
        )
        .unwrap(),
        authorization,
        "repeated membership must not restart its publication age"
    );
    publication.read_authorization = Some(authorization);
    let reference = store.read_ref(&key).unwrap().unwrap();
    let root_id = store.put(&publication.encode().unwrap()).unwrap();
    assert!(matches!(
        publish_refs(
            &mut store,
            &[RefCondition {
                key: key.clone(),
                expected: Some(reference.revision)
            }],
            &[RefChange {
                key: key.clone(),
                value: Some(root_id.to_vec())
            }]
        )
        .unwrap(),
        BatchOutcome::Applied(_)
    ));

    let captured = DatabaseValueRoot {
        indexes: Some(unused_index),
        ..DatabaseValueRoot::from(&publication)
    };
    let captured_id = store.put(&captured.encode().unwrap()).unwrap();
    // Restore/publication roots own objects; merely reading a value does not.
    let owner = format!("restores/test/{:032x}", crate::uuid_v7().unwrap());
    let owner_change = |value| RefChange {
        key: owner.clone(),
        value,
    };
    assert!(matches!(
        publish_refs(
            &mut store,
            &[RefCondition {
                key: owner.clone(),
                expected: None
            }],
            &[owner_change(Some(captured_id.to_vec()))]
        )
        .unwrap(),
        BatchOutcome::Applied(_)
    ));
    let mut collector = BlockCollector::connect(&f.config).unwrap();
    let settle = |collector: &mut BlockCollector| {
        for _ in 0..64 {
            if collector.advance(Duration::ZERO, 4096).unwrap().phase == CollectionPhase::Complete {
                return;
            }
        }
        panic!("small ownership fixture did not settle");
    };
    settle(&mut collector);
    assert_eq!(
        settled_count(&mut store, unused_index).unwrap().unwrap().0,
        2
    );
    // Cursor-only pages survive a new collector/transport each time. A long
    // age keeps this pass read-only with respect to database publications.
    let mut examined = 0;
    for calls in 1..=5 {
        collector = BlockCollector::connect(&f.config).unwrap();
        let page = collector
            .prune_read_authorizations(Duration::from_secs(86_400), 1)
            .unwrap();
        assert!(page.examined <= 1);
        assert_eq!(page.removed, 0);
        assert!(!page.unsettled);
        examined += page.examined;
        if page.complete {
            break;
        }
        assert!(
            calls < 5,
            "persisted cursor must advance beyond both entries"
        );
    }
    assert_eq!(examined, 2);
    let condition = RefCondition {
        key: key.clone(),
        expected: Some(store.read_ref(&key).unwrap().unwrap().revision),
    };
    let held = ReadAuthorization::prune_indexes(
        &mut store,
        &publication,
        condition.clone(),
        u64::MAX,
        None,
        32,
    )
    .unwrap();
    assert_eq!(
        held.removed, 0,
        "an explicitly published owner retains its covering index"
    );
    let owner_revision = store.read_ref(&owner).unwrap().unwrap().revision;
    publish_refs(
        &mut store,
        &[RefCondition {
            key: owner.clone(),
            expected: Some(owner_revision),
        }],
        &[owner_change(None)],
    )
    .unwrap();
    settle(&mut collector);
    assert_eq!(
        settled_count(&mut store, unused_index).unwrap().unwrap().0,
        1
    );
    let young =
        ReadAuthorization::prune_indexes(&mut store, &publication, condition.clone(), 0, None, 1)
            .unwrap();
    assert_eq!(young.examined, 1);
    assert_eq!(young.removed, 0, "publication grace precedes eligibility");
    let candidate = ReadAuthorization::prune_indexes(
        &mut store,
        &publication,
        condition.clone(),
        u64::MAX,
        None,
        32,
    )
    .unwrap();
    assert_eq!(candidate.removed, 1);
    assert!(!candidate.unsettled);
    // An explicit restore owner participates in publication ownership. Once
    // released, its value wrapper may be collected. Re-stage the wrapper
    // under root/GC protection before publishing a new owner.
    let protection =
        super::super::engine::protection(&mut store, std::slice::from_ref(&condition)).unwrap();
    let mut owner_guards = protection.conditions.clone();
    store.set_write_protection(Some(protection)).unwrap();
    assert_eq!(store.put(&captured.encode().unwrap()).unwrap(), captured_id);
    store.set_write_protection(None).unwrap();
    let owner_revision = store.read_ref(&owner).unwrap().unwrap().revision;
    owner_guards.push(RefCondition {
        key: owner.clone(),
        expected: Some(owner_revision),
    });
    publish_refs(
        &mut store,
        &owner_guards,
        &[owner_change(Some(captured_id.to_vec()))],
    )
    .unwrap();
    assert!(
        matches!(
            publish_refs(
                &mut store,
                &candidate.guards,
                &[RefChange {
                    key: key.clone(),
                    value: Some(root_id.to_vec())
                }]
            )
            .unwrap(),
            BatchOutcome::Conflict(_)
        ),
        "a concurrent new owner invalidates the prune proof at publication"
    );
    let owner_revision = store.read_ref(&owner).unwrap().unwrap().revision;
    publish_refs(
        &mut store,
        &[RefCondition {
            key: owner.clone(),
            expected: Some(owner_revision),
        }],
        &[owner_change(None)],
    )
    .unwrap();
    settle(&mut collector);
    let maintained = collector
        .prune_read_authorizations(Duration::ZERO, 32)
        .unwrap();
    assert_eq!(
        maintained.removed, 1,
        "public maintenance publishes the guarded prune"
    );
    publication = root(&mut store, &database);
    let auth = ReadAuthorization::load(
        &mut store,
        publication.read_authorization.unwrap(),
        &publication.identity,
    )
    .unwrap();
    let registry = RequestIndex::from_root(Some(auth.indexes));
    assert_eq!(
        registry
            .lookup(&mut store, index_key(&publication.identity, unused_index))
            .unwrap(),
        None
    );
    assert!(
        registry
            .lookup(&mut store, index_key(&publication.identity, initial_index))
            .unwrap()
            .is_some()
    );
    assert!(
        store.get(unused_index).unwrap().is_some(),
        "logical pruning is not immediate physical erasure"
    );
}

#[test]
fn collector_checkpoints_folded_owners_before_destructive_phases() {
    use crate::storage::RefChange;
    use crate::storage::ownership::{BlockCollector, CollectionPhase, publish_refs};
    use std::time::Duration;

    let Some(f) = fixture() else {
        return;
    };
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let id = store
        .put(
            &Block {
                kind: 55,
                links: vec![],
                payload: b"retired in the same sealed epoch".to_vec(),
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let key = format!("restores/test/{:032x}", crate::uuid_v7().unwrap());
    publish_refs(
        &mut store,
        &[RefCondition {
            key: key.clone(),
            expected: None,
        }],
        &[RefChange {
            key: key.clone(),
            value: Some(id.to_vec()),
        }],
    )
    .unwrap();
    let revision = store.read_ref(&key).unwrap().unwrap().revision;
    publish_refs(
        &mut store,
        &[RefCondition {
            key: key.clone(),
            expected: Some(revision),
        }],
        &[RefChange { key, value: None }],
    )
    .unwrap();
    assert_eq!(
        store
            .list_live_refs("ownership/events/", None, 128)
            .unwrap()
            .len(),
        2
    );
    let mut collector = BlockCollector::connect(&f.config).unwrap();
    assert_eq!(
        collector.advance(Duration::ZERO, 1).unwrap().phase,
        CollectionPhase::Adding
    );
    assert_eq!(
        collector.advance(Duration::ZERO, 2).unwrap().phase,
        CollectionPhase::Adding
    );
    drop(collector); // Restart with the newly added owner folded, removal still pending.
    let mut collector = BlockCollector::connect(&f.config).unwrap();
    let mut observer = BlockCollector::connect(&f.config).unwrap();
    let mut saw_delete = false;
    for _ in 0..16 {
        let result = collector.advance_with_control(Duration::ZERO, 4096, &mut || {
            if !store.object_exists(id)? {
                assert_eq!(
                    observer.status()?.unwrap().phase,
                    CollectionPhase::Sweeping,
                    "folded counts must be durably published before deleting their source objects"
                );
                saw_delete = true;
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "test/after-sweep-delete",
                    "interrupt after physical deletion",
                ));
            }
            Ok(())
        });
        if let Err(error) = result {
            assert_eq!(error.code, "test/after-sweep-delete");
            break;
        }
    }
    assert!(saw_delete);
    drop(collector);
    let mut collector = BlockCollector::connect(&f.config).unwrap();
    let mut saw_forget = false;
    for _ in 0..16 {
        let result = collector.advance_with_control(Duration::ZERO, 4096, &mut || {
            if store.list_live_refs("ownership/events/", None, 128)?.len() < 2 {
                assert_eq!(
                    observer.status()?.unwrap().phase,
                    CollectionPhase::ClearingEvents,
                    "sealed event deletion needs a durable post-fold checkpoint"
                );
                saw_forget = true;
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "test/after-event-forget",
                    "interrupt after event removal",
                ));
            }
            Ok(())
        });
        if let Err(error) = result {
            assert_eq!(error.code, "test/after-event-forget");
            break;
        }
    }
    assert!(saw_forget);
    drop(collector);
    let mut collector = BlockCollector::connect(&f.config).unwrap();
    let mut complete = false;
    for _ in 0..16 {
        if collector.advance(Duration::ZERO, 4096).unwrap().phase == CollectionPhase::Complete {
            complete = true;
            break;
        }
    }
    assert!(complete);
    assert!(store.get(id).unwrap().is_none());
    assert!(
        store
            .list_live_refs("ownership/events/", None, 128)
            .unwrap()
            .is_empty()
    );
}

#[test]
fn readonly_peer_reopens_embedded_value_after_old_publication_wrapper_is_collected() {
    use crate::storage::ownership::{BlockCollector, CollectionPhase};
    use crate::{EntityRef, IndexOrder, Peer, SnapshotReference, TxOp, Value};
    use std::time::Duration;
    let Some(mut f) = fixture() else {
        return;
    };
    let can_create: bool = f
        .admin
        .query_one(
            "SELECT rolsuper OR rolcreaterole FROM pg_roles WHERE rolname=current_user",
            &[],
        )
        .unwrap()
        .get(0);
    if !can_create {
        eprintln!("SKIP readonly snapshot role: CREATE ROLE unavailable");
        return;
    }
    let role = format!("snapshot_reader_{:032x}", crate::uuid_v7().unwrap());
    struct Role {
        admin: postgres::Client,
        schema: String,
        name: String,
    }
    impl Drop for Role {
        fn drop(&mut self) {
            let _ = self.admin.batch_execute(&format!(
                "REVOKE SELECT ON {0}.atomic_objects FROM {1}; REVOKE SELECT,INSERT,UPDATE,DELETE ON {0}.atomic_refs FROM {1}; REVOKE USAGE ON SCHEMA {0} FROM {1}; DROP ROLE {1}", self.schema, self.name));
        }
    }
    f.admin
        .batch_execute(&format!("CREATE ROLE {role} LOGIN PASSWORD '{role}'"))
        .unwrap();
    let base = std::env::var("ATOMIC_POSTGRES_URL").unwrap();
    let _role = Role {
        admin: postgres::Client::connect(&base, postgres::NoTls).unwrap(),
        schema: f.schema.clone(),
        name: role.clone(),
    };
    f.admin.batch_execute(&format!("GRANT USAGE ON SCHEMA {0} TO {1}; GRANT SELECT ON {0}.atomic_objects TO {1}; GRANT SELECT,INSERT,UPDATE,DELETE ON {0}.atomic_refs TO {1}", f.schema, role)).unwrap();
    let parameters = if base.starts_with("postgres://") || base.starts_with("postgresql://") {
        format!(
            "{base}{}user={role}&password={role}&options=-csearch_path%3D{}%2Cpg_catalog",
            if base.contains('?') { '&' } else { '?' },
            f.schema
        )
    } else {
        format!(
            "{base} user={role} password={role} options='-csearch_path={},pg_catalog'",
            f.schema
        )
    };
    let reader_config = PostgresConnectionConfig::plaintext(parameters);
    let mut probe = reader_config.connect().unwrap();
    assert_eq!(
        probe
            .query_one("SELECT current_user::text", &[])
            .unwrap()
            .get::<_, String>(0),
        role
    );
    assert_eq!(
        probe
            .execute("UPDATE atomic_objects SET payload=payload WHERE false", &[])
            .unwrap_err()
            .code()
            .unwrap()
            .code(),
        "42501"
    );

    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let database = BlockDatabase::create(&f.config, "readonly", schema).unwrap();
    let mut writer =
        BlockTransactor::claim(&f.config, database.clone(), BlockWriterOptions::default()).unwrap();
    let first = writer
        .transact(
            &TransactionRequest::new(
                "one",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: 1000,
                    value: Value::Long(7).into(),
                }],
            )
            .with_tx_instant(10),
        )
        .unwrap();
    let entity = first.tempids["item"];
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let prepared = writer
        .index_input()
        .unwrap()
        .prepare(&mut store, &crate::persistent_tree::TreeConfig::default())
        .unwrap();
    writer.adopt_index(prepared).unwrap();
    let value = writer.hint_database_value().unwrap();
    let snapshot = value.committed_block_parts().unwrap().0;
    let old_wrapper = snapshot.storage_root_id();
    let expected = value.datoms(IndexOrder::Eavt).unwrap();
    let reference =
        SnapshotReference::decode(&value.snapshot_reference().unwrap().encode().unwrap()).unwrap();
    let original_basis = value.basis_t();
    let second = writer
        .transact(
            &TransactionRequest::new(
                "two",
                vec![TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1000,
                    value: Value::Long(8).into(),
                }],
            )
            .with_tx_instant(20),
        )
        .unwrap();
    drop(second);
    drop(first);
    drop(value);
    drop(snapshot);
    writer.release().unwrap();

    let peer =
        Peer::connect_configured_with_cache_limits(&reader_config, "readonly", 0, 0).unwrap();
    let opened = peer.reopen_snapshot(&reference).unwrap();
    assert_eq!(opened.values(entity, 1000).unwrap(), vec![Value::Long(7)]);
    assert_eq!(
        peer.database_value().values(entity, 1000).unwrap(),
        vec![Value::Long(8)]
    );
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
        store.get(old_wrapper).unwrap().is_none(),
        "old complete publication wrapper is no longer owned"
    );
    let before: i64 = f
        .admin
        .query_one(
            &format!("SELECT count(*) FROM {}.atomic_objects", f.schema),
            &[],
        )
        .unwrap()
        .get(0);
    let reopened = reference.open(&reader_config, 0, 0).unwrap();
    assert_eq!(reopened.basis_t(), original_basis);
    assert_eq!(reopened.datoms(IndexOrder::Eavt).unwrap(), expected);
    assert_eq!(opened.datoms(IndexOrder::Eavt).unwrap(), expected);
    assert_eq!(
        SnapshotReference::decode(&reopened.snapshot_reference().unwrap().encode().unwrap())
            .unwrap(),
        reference
    );
    let after: i64 = f
        .admin
        .query_one(
            &format!("SELECT count(*) FROM {}.atomic_objects", f.schema),
            &[],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        after, before,
        "opening and cold reads add no immutable objects"
    );
}

#[test]
fn publication_authorizes_indexes_atomically_and_retains_initial_and_receipt_values() {
    let Some(f) = fixture() else {
        return;
    };
    for initial_basis in [0, 1] {
        let mut schema = Schema::new();
        if initial_basis == 1 {
            schema
                .install(Attribute::new(
                    1000,
                    Keyword::new("item", "value"),
                    ValueType::Long,
                    Cardinality::One,
                ))
                .unwrap();
        }
        let database =
            BlockDatabase::create(&f.config, &format!("initial-{initial_basis}"), schema).unwrap();
        let mut store = PgBlockStore::connect(&f.config).unwrap();
        let initial = root(&mut store, &database);
        assert_eq!(initial.basis, initial_basis);
        let initial_value = DatabaseValueRoot::from(&initial);
        authorize_value(&mut store, &initial, &initial_value).unwrap();
        let mut writer =
            BlockTransactor::claim(&f.config, database.clone(), BlockWriterOptions::default())
                .unwrap();
        let first = report_value(&commit(&mut writer, "first", 10));
        let last = report_value(&commit(&mut writer, "last", 20));
        let before = root(&mut store, &database);
        authorize_value(&mut store, &before, &initial_value).unwrap();
        authorize_value(&mut store, &before, &first).unwrap();
        let prepared = writer
            .index_input()
            .unwrap()
            .prepare(&mut store, &crate::persistent_tree::TreeConfig::default())
            .unwrap();
        let candidate = DatabaseValueRoot {
            indexes: Some(prepared.descriptor_id()),
            ..last.clone()
        };
        assert_eq!(
            authorize_value(&mut store, &before, &candidate)
                .unwrap_err()
                .code,
            "snapshot/unpublished-value",
            "staging is not publication authority"
        );
        writer.adopt_index(prepared).unwrap();
        let published = root(&mut store, &database);
        authorize_value(&mut store, &published, &candidate).unwrap();
        authorize_value(&mut store, &published, &last).unwrap();
        authorize_value(&mut store, &published, &first).unwrap();
        authorize_value(&mut store, &published, &initial_value).unwrap();
        // The new descriptor links the initial value and current trie, never
        // its predecessor authorization or publication object.
        let id = published.read_authorization.unwrap();
        let block = Block::decode(&id, &required(&mut store, id).unwrap()).unwrap();
        assert!(!block.links.contains(&before.read_authorization.unwrap()));
        writer.release().unwrap();
    }
}

#[test]
fn whole_log_and_metadata_are_authenticated_not_only_final_transaction_identity() {
    let Some(f) = fixture() else {
        return;
    };
    let database = BlockDatabase::create(&f.config, "canonical", Schema::new()).unwrap();
    let mut writer =
        BlockTransactor::claim(&f.config, database.clone(), BlockWriterOptions::default()).unwrap();
    commit(&mut writer, "one", 10);
    let last = report_value(&commit(&mut writer, "two", 20));
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let publication = root(&mut store, &database);
    authorize_value(&mut store, &publication, &last).unwrap();
    let log = super::super::log::LogRoot::open(&mut store, last.log.unwrap()).unwrap();
    let mut earlier = log.read_record(&mut store, 1).unwrap().unwrap().entry;
    let final_record = log.read_record(&mut store, 2).unwrap().unwrap();
    let instant = earlier
        .tx_data
        .iter_mut()
        .find(|d| u64::from(d.attribute) == crate::DB_TX_INSTANT)
        .unwrap();
    instant.value = crate::Value::Instant(11);
    let forged = super::super::log::LogRoot::empty()
        .append(&mut store, &earlier)
        .unwrap()
        .append(&mut store, &final_record.entry)
        .unwrap();
    assert_eq!(forged.latest_entry_id(), Some(final_record.id));
    assert_ne!(forged.head(), last.log);
    let candidate = DatabaseValueRoot {
        log: forged.head(),
        ..last.clone()
    };
    assert_eq!(
        authorize_value(&mut store, &publication, &candidate)
            .unwrap_err()
            .code,
        "snapshot/unpublished-value"
    );

    let id = last.metadata.unwrap();
    let mut metadata = SnapshotMetadata::decode(&id, &required(&mut store, id).unwrap()).unwrap();
    metadata.last_tx_instant = Some(21);
    let forged_metadata = store.put(&metadata.encode().unwrap()).unwrap();
    let candidate = DatabaseValueRoot {
        metadata: Some(forged_metadata),
        ..last.clone()
    };
    assert_eq!(
        authorize_value(&mut store, &publication, &candidate)
            .unwrap_err()
            .code,
        "snapshot/unpublished-value"
    );
    let mut missing = publication.clone();
    missing.read_authorization = None;
    assert_eq!(
        authorize_value(&mut store, &missing, &last)
            .unwrap_err()
            .code,
        "snapshot/unpublished-value"
    );
    writer.release().unwrap();
}
