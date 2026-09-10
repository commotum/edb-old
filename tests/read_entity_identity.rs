mod common;

use atomic_core::{
    Attribute, Cardinality, Database, Entity, EntityRef, Keyword, OperationContext, OperationKind,
    Peer, PostgresIndexer, PostgresMigrator, PostgresStore, Schema, TxOp, Value, ValueType,
};
use std::collections::HashSet;
use std::hash::{DefaultHasher, Hash, Hasher};
use std::time::Instant;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add(entity: EntityRef, name: &str) -> TxOp {
    TxOp::Add {
        entity,
        attribute: 1000,
        value: Value::String(name.into()).into(),
    }
}

fn hash(entity: &Entity) -> u64 {
    let mut hash = DefaultHasher::new();
    entity.hash(&mut hash);
    hash.finish()
}

#[test]
fn identity_survives_values_and_cache_but_not_independent_memory_databases() {
    let initial = Database::new(schema()).unwrap();
    let first = initial
        .with(&[add(EntityRef::Temp("item".into()), "first")], 1000)
        .unwrap();
    let id = first.tempids["item"];
    let second = first
        .db_after
        .with(&[add(EntityRef::Id(id), "second")], 2000)
        .unwrap();
    let before = first.db_after.database_value().entity(id).unwrap().unwrap();
    let after = second
        .db_after
        .database_value()
        .entity(id)
        .unwrap()
        .unwrap();
    assert_eq!(before, after);
    assert_eq!(hash(&before), hash(&after));
    assert_eq!(before.identity(), after.identity());
    assert_eq!(before.identity().lineage_id(), None);
    assert_eq!(before.identity().entity_id(), id);
    let before_hash = hash(&before);
    assert!(before.get(1000).unwrap().is_some());
    assert_eq!(before_hash, hash(&before));
    assert_eq!(before, after); // Lazy cache contents are not entity identity.

    let past = second
        .db_after
        .database_value()
        .as_of(first.db_after.basis_t())
        .entity(id)
        .unwrap()
        .unwrap();
    assert_eq!(past, before);
    let branch = second
        .db_after
        .with(&[add(EntityRef::Id(id), "what if")], 3000)
        .unwrap();
    assert_eq!(
        branch
            .db_after
            .database_value()
            .entity(id)
            .unwrap()
            .unwrap(),
        before
    );
    let overlay = second
        .db_after
        .database_value()
        .with(&[add(EntityRef::Id(id), "overlay")], 3000)
        .unwrap();
    assert_eq!(overlay.db_after.entity(id).unwrap().unwrap(), before);
    let filtered = second
        .db_after
        .database_value()
        .filter(|_, _| true)
        .entity(id)
        .unwrap()
        .unwrap();
    assert_eq!(filtered, before);
    let another = Database::new(schema())
        .unwrap()
        .database_value()
        .entity(id)
        .unwrap()
        .unwrap();
    assert_ne!(another, before);
    let keys: HashSet<_> = [before.identity(), after.identity(), another.identity()]
        .into_iter()
        .collect();
    assert_eq!(keys.len(), 2);
}

#[test]
fn postgres_entity_identity_survives_connections_recovery_and_writer_shutdown_without_io() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED PostgreSQL entity identity witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "entity_identity");
    let url = &fixture.connection;
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    let mut store = PostgresStore::connect(url).unwrap();
    let created = store.create_database("identity", schema()).unwrap();
    store.create_database("other", schema()).unwrap();
    PostgresIndexer::connect(url, "other")
        .unwrap()
        .consolidate()
        .unwrap();
    let service = common::start_service(url, "identity");
    let first = common::transact(
        &service,
        "first",
        created.basis_t(),
        &[add(EntityRef::Temp("item".into()), "first")],
        1000,
    );
    let id = first.tempids["item"];
    let second = common::transact(
        &service,
        "second",
        first.basis_t,
        &[add(EntityRef::Id(id), "second")],
        2000,
    );
    service.shutdown();
    let before = first.db_after.entity(id).unwrap().unwrap();
    let after = second.db_after.entity(id).unwrap().unwrap();
    let peer = Peer::connect(url, "identity", 8).unwrap();
    let reopened = peer.database_value().entity(id).unwrap().unwrap();
    let recovered = store
        .recover("identity")
        .unwrap()
        .database_value()
        .entity(id)
        .unwrap()
        .unwrap();
    let other = Peer::connect(url, "other", 8)
        .unwrap()
        .database_value()
        .entity(id)
        .unwrap()
        .unwrap();
    assert_ne!(before, other);
    assert_eq!(before, after);
    assert_eq!(before, reopened);
    assert_eq!(before, recovered);
    let speculative = second
        .db_after
        .with(&[add(EntityRef::Id(id), "not committed")], 3000)
        .unwrap();
    assert_eq!(speculative.db_after.entity(id).unwrap().unwrap(), before);
    assert!(before.identity().lineage_id().is_some());
    assert_eq!(
        created.database_value().entity(1000).unwrap().unwrap(),
        first.db_after.entity(1000).unwrap().unwrap()
    );

    let context = OperationContext::new(OperationKind::Application);
    let start = Instant::now();
    {
        let _scope = context.enter();
        for _ in 0..10_000 {
            assert_eq!(
                std::hint::black_box(&before),
                std::hint::black_box(&reopened)
            );
            assert_eq!(hash(&before), hash(&reopened));
            assert_eq!(before.identity(), reopened.identity());
        }
    }
    assert_eq!(context.snapshot().sql_calls, 0);
    eprintln!(
        "entity identity 10000 compare/hash/token iterations: {}us, zero SQL",
        start.elapsed().as_micros()
    );
}
