//! Deterministic interleavings at the derived publication boundary. Hooks are
//! test-only and run real writer operations on an independent connection.
use super::*;
use crate::storage::{BlockDatabase, BlockTransactor, BlockWriterOptions, PreparedIndex};
use crate::{
    Attribute, Cardinality, EntityRef, Keyword, PostgresConnectionConfig, Schema,
    TransactionRequest, TxOp, Value, ValueType,
};
use std::cell::RefCell;
use std::rc::Rc;

thread_local! {
    static AFTER_STAGING: RefCell<Option<Box<dyn FnOnce()>>> = RefCell::new(None);
}

pub(super) fn after_staging() {
    let hook = AFTER_STAGING.with(|slot| slot.borrow_mut().take());
    if let Some(hook) = hook {
        hook();
    }
}

struct Hook;
impl Drop for Hook {
    fn drop(&mut self) {
        AFTER_STAGING.with(|slot| {
            slot.borrow_mut().take();
        });
    }
}
fn staged(hook: impl FnOnce() + 'static) -> Hook {
    AFTER_STAGING.with(|slot| assert!(slot.borrow_mut().replace(Box::new(hook)).is_none()));
    Hook
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
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP index publication PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("index_publication_{:032x}", crate::uuid_v7().unwrap());
    let mut admin = postgres::Client::connect(&url, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let scoped = if url.starts_with("postgres://") || url.starts_with("postgresql://") {
        format!(
            "{url}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
            if url.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{url} options='-csearch_path={schema},pg_catalog'")
    };
    let config = PostgresConnectionConfig::plaintext(scoped);
    PgBlockStore::install(&config).unwrap();
    let mut application = Schema::new();
    let mut attribute = Attribute::new(
        1000,
        Keyword::new("item", "value"),
        ValueType::Long,
        Cardinality::One,
    );
    attribute.indexed = true;
    application.install(attribute).unwrap();
    let database = BlockDatabase::create(&config, "publication", application).unwrap();
    Some(Fixture {
        admin,
        schema,
        config,
        database,
    })
}
fn request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 1000,
            value: Value::Long(value).into(),
        }],
    )
}
fn candidate(prepared: &PreparedIndex) -> IndexCandidate<'_> {
    IndexCandidate {
        source_indexes: Some(prepared.source_indexes),
        source_index_basis: prepared.snapshot.indexed_basis_t(),
        descriptor: &prepared.descriptor,
        descriptor_id: prepared.descriptor_id,
        endpoint_entry: prepared.endpoint_entry,
        protection: &prepared.protection,
    }
}
fn current(store: &mut PgBlockStore, database: &BlockDatabase) -> DatabaseRoot {
    let reference = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let id = reference.value.as_deref().unwrap().try_into().unwrap();
    DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap()
}

#[test]
fn operator_staging_tolerates_same_writer_renewal_but_owned_lease_stays_exact() {
    for operator in [true, false] {
        let Some(f) = fixture() else {
            return;
        };
        let writer = Rc::new(RefCell::new(
            BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
                .unwrap(),
        ));
        let seed_request = request("seed", 7);
        let seed = writer.borrow_mut().transact(&seed_request).unwrap();
        let mut store = PgBlockStore::connect(&f.config).unwrap();
        let prepared = writer
            .borrow_mut()
            .index_input()
            .unwrap()
            .prepare(&mut store, &crate::persistent_tree::TreeConfig::default())
            .unwrap();
        let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
        let capture = reader
            .capture_reference(&f.database.reference_key())
            .unwrap();
        let before = current(&mut store, &f.database);
        let lease_key = f.database.lease_key();
        let lease = store.read_ref(&lease_key).unwrap().unwrap();
        let epoch = writer.borrow().writer_epoch();
        let renewing = Rc::clone(&writer);
        let hook = staged(move || renewing.borrow_mut().renew().unwrap());
        let result = if operator {
            publish_index_as_operator(
                &mut store,
                &reader,
                &capture,
                &lease_key,
                epoch,
                candidate(&prepared),
            )
        } else {
            publish_index(
                &mut store,
                &reader,
                &capture,
                RefCondition {
                    key: lease_key.clone(),
                    expected: Some(lease.revision),
                },
                epoch,
                candidate(&prepared),
            )
        };
        drop(hook);
        assert!(store.read_ref(&lease_key).unwrap().unwrap().revision > lease.revision);
        let after = current(&mut store, &f.database);
        if operator {
            let published = result.unwrap();
            assert_eq!(published.snapshot.indexed_basis_t(), seed.basis_t);
            assert_eq!(after.indexes, Some(prepared.descriptor_id));
        } else {
            assert_eq!(
                result.err().unwrap().category,
                crate::ErrorCategory::Conflict
            );
            assert_eq!(after.indexes, before.indexes);
        }
        assert_eq!(after.basis, before.basis);
        assert_eq!(after.log, before.log);
        assert_eq!(after.receipts, before.receipts);
        assert_eq!(after.metadata, before.metadata);
        assert_eq!(after.writer_epoch, epoch);
        assert!(
            writer
                .borrow_mut()
                .transact(&seed_request)
                .unwrap()
                .replayed
        );
        assert_eq!(
            writer
                .borrow_mut()
                .transact(&request("next", 8))
                .unwrap()
                .basis_t,
            seed.basis_t + 1
        );
        drop(capture);
        let writer = Rc::try_unwrap(writer).ok().unwrap().into_inner();
        writer.release().unwrap();
    }
}

#[test]
fn moving_publication_fences_staged_root_and_retry_preserves_the_newer_tail() {
    let Some(f) = fixture() else {
        return;
    };
    let writer = Rc::new(RefCell::new(
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap(),
    ));
    let seed = writer.borrow_mut().transact(&request("seed", 10)).unwrap();
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let prepared = writer
        .borrow_mut()
        .index_input()
        .unwrap()
        .prepare(&mut store, &crate::persistent_tree::TreeConfig::default())
        .unwrap();
    let reader = BlockReader::connect(&f.config, Default::default()).unwrap();
    let capture = reader
        .capture_reference(&f.database.reference_key())
        .unwrap();
    let before = current(&mut store, &f.database);
    let epoch = writer.borrow().writer_epoch();
    let commit = Rc::clone(&writer);
    let newer = Rc::new(RefCell::new(None));
    let committed = Rc::clone(&newer);
    let hook = staged(move || {
        *committed.borrow_mut() = Some(
            commit
                .borrow_mut()
                .transact(&request("during-staging", 11))
                .unwrap(),
        );
    });
    let error = publish_index_as_operator(
        &mut store,
        &reader,
        &capture,
        &f.database.lease_key(),
        epoch,
        candidate(&prepared),
    )
    .err()
    .unwrap();
    drop(hook);
    assert_eq!(error.category, crate::ErrorCategory::Conflict);
    let newer = newer.borrow_mut().take().unwrap();
    assert_eq!(newer.basis_t, seed.basis_t + 1);
    let retained = current(&mut store, &f.database);
    assert_eq!(retained.basis, newer.basis_t);
    assert_eq!(
        retained.indexes, before.indexes,
        "stale staging did not publish"
    );
    let latest = reader
        .capture_reference(&f.database.reference_key())
        .unwrap();
    let publication = publish_index_as_operator(
        &mut store,
        &reader,
        &latest,
        &f.database.lease_key(),
        epoch,
        candidate(&prepared),
    )
    .unwrap();
    let after = current(&mut store, &f.database);
    assert_eq!(after.basis, retained.basis);
    assert_eq!(after.log, retained.log);
    assert_eq!(after.receipts, retained.receipts);
    assert_eq!(after.metadata, retained.metadata);
    assert_eq!(publication.snapshot.indexed_basis_t(), seed.basis_t);
    assert_eq!(
        publication
            .snapshot
            .database_value()
            .values(newer.tempids["item"], 1000)
            .unwrap(),
        vec![Value::Long(11)]
    );
    assert!(
        writer
            .borrow_mut()
            .transact(&request("during-staging", 11))
            .unwrap()
            .replayed
    );
    let writer = Rc::try_unwrap(writer).ok().unwrap().into_inner();
    writer.release().unwrap();
}
