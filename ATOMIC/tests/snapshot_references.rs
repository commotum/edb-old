mod common;

use atomic_core::{
    Attribute, Cardinality, DB_EXCISE, Database, EntityRef, ErrorCategory, IndexOrder, Keyword,
    OperationContext, OperationKind, Peer, PostgresConnectionConfig, PostgresOperator, Schema,
    SnapshotReference, TxOp, Value, ValueType,
};
use std::time::Instant;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, name, no_history) in [(1000, "value", false), (1001, "ephemeral", true)] {
        let mut attribute = Attribute::new(
            id,
            Keyword::new("item", name),
            ValueType::Long,
            Cardinality::One,
        );
        attribute.no_history = no_history;
        schema.install(attribute).unwrap();
    }
    schema
}
fn add(entity: EntityRef, attribute: u32, number: i64) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: Value::Long(number).into(),
    }
}
fn config(url: &str) -> PostgresConnectionConfig {
    PostgresConnectionConfig::plaintext(url)
}

#[test]
fn eager_fixtures_do_not_invent_a_committed_identity() {
    let db = Database::new(schema()).unwrap().database_value();
    assert_eq!(
        db.snapshot_key().unwrap_err().category,
        ErrorCategory::Unsupported
    );
    assert_eq!(
        db.snapshot_reference().unwrap_err().category,
        ErrorCategory::Unsupported
    );
}

// Forge a logical endpoint or a covering-index claim using the current codec,
// then recompute the public checksum. Decoding isn't authorization: each
// well-formed forged reference must reach and fail storage validation.
fn altered(reference: &SnapshotReference, witness: bool) -> SnapshotReference {
    let mut bytes = reference.encode().unwrap();
    assert_eq!(&bytes[..6], b"ATSN\0\x03");
    let mut at = 6;
    for _ in 0..2 {
        let size = u32::from_be_bytes(bytes[at..at + 4].try_into().unwrap()) as usize;
        at += 4 + size;
    }
    if witness {
        at += 24 + 64;
        let length = u16::from_be_bytes(bytes[at..at + 2].try_into().unwrap()) as usize;
        at += 2;
        let value_bytes = &bytes[at..at + length];
        let mut value = atomic_core::storage::root::DatabaseValueRoot::decode(
            &atomic_core::sha256(value_bytes),
            value_bytes,
        )
        .unwrap();
        let mut index = value.indexes.expect("fixture has a covering index");
        index[0] ^= 0x80;
        value.indexes = Some(index);
        let forged = value.encode().unwrap();
        assert_eq!(forged.len(), length);
        bytes[at..at + length].copy_from_slice(&forged);
    } else {
        // generation/basis/frontier, transaction hash, then logical state hash.
        at += 24 + 32;
        bytes[at] ^= 0x80;
    }
    let end = bytes.len() - 32;
    let hash = atomic_core::sha256(&bytes[..end]);
    bytes[end..].copy_from_slice(&hash);
    SnapshotReference::decode(&bytes).unwrap()
}

#[test]
fn postgres_exact_references_preserve_basis_views_retention_and_no_history_exposure() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED snapshot reference PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "snapshot_refs");
    let url = &fixture.connection;
    common::install(url).unwrap();
    let created = common::TestStore::connect(url)
        .unwrap()
        .create_database("snapshots", schema())
        .unwrap();
    let service = common::start_service(url, "snapshots");
    let first = common::transact(
        &service,
        "first",
        created.basis_t(),
        &[
            add(EntityRef::Temp("item".into()), 1000, 1),
            add(EntityRef::Temp("item".into()), 1001, 10),
        ],
        1000,
    );
    let eid = first.tempids["item"];
    let second = common::transact(
        &service,
        "second",
        first.basis_t,
        &[
            add(EntityRef::Id(eid), 1000, 2),
            add(EntityRef::Id(eid), 1001, 20),
        ],
        2000,
    );
    service.shutdown();
    let before_index = second.db_after.clone();
    let before_history = before_index.clone().history();
    let history_facts = before_history.collect_datoms(IndexOrder::Eavt).unwrap();
    let history_ref = before_history.snapshot_reference().unwrap();
    let reference = before_index.snapshot_reference().unwrap();
    let key = before_index.snapshot_key().unwrap();

    let measured = OperationContext::new(OperationKind::Application);
    let start = Instant::now();
    {
        let _scope = measured.enter();
        for _ in 0..10_000 {
            assert_eq!(
                std::hint::black_box(before_index.snapshot_key().unwrap()),
                key
            );
            assert_eq!(std::hint::black_box(key.clone()), key);
        }
    }
    assert_eq!(measured.snapshot().sql_calls, 0);
    let key_micros = start.elapsed().as_micros();
    assert_ne!(key, before_history.snapshot_key().unwrap());
    assert_ne!(
        key,
        before_index
            .clone()
            .as_of(first.basis_t)
            .snapshot_key()
            .unwrap()
    );
    assert_ne!(key, before_index.clone().since(0).snapshot_key().unwrap());
    assert_ne!(
        first.db_after.snapshot_key().unwrap(),
        before_index
            .clone()
            .as_of(first.basis_t)
            .snapshot_key()
            .unwrap()
    );
    assert_eq!(
        before_index
            .clone()
            .filter(|_, _| true)
            .snapshot_key()
            .unwrap_err()
            .category,
        ErrorCategory::Unsupported
    );
    let speculative = before_index
        .with(&[add(EntityRef::Id(eid), 1000, 3)], 3000)
        .unwrap();
    assert_eq!(
        speculative
            .db_after
            .snapshot_reference()
            .unwrap_err()
            .category,
        ErrorCategory::Unsupported
    );
    assert_eq!(
        before_index.values(eid, 1000).unwrap(),
        vec![Value::Long(2)]
    );

    common::consolidate(url, "snapshots").unwrap();
    let peer = Peer::connect(url, "snapshots", 8).unwrap();
    assert_eq!(peer.db().snapshot_key().unwrap(), key);
    // noHistory is a physical retention policy, not a different logical commit.
    assert!(
        peer.db()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap()
            .len()
            < history_facts.len()
    );
    let reopened = peer.reopen_snapshot(&history_ref).unwrap();
    assert_eq!(
        reopened.collect_datoms(IndexOrder::Eavt).unwrap(),
        history_facts
    );
    assert_eq!(reopened.snapshot_key().unwrap(), history_ref.key().clone());
    let decoded = SnapshotReference::decode(&reference.encode().unwrap()).unwrap();
    let direct = decoded.open(&config(url), 8, 1024 * 1024).unwrap();
    common::assert_same_information(&direct, &before_index);
    assert_eq!(peer.basis_t(), second.basis_t);

    // New commits and schema changes do not substitute latest/as-of for exact.
    let service = common::start_service(url, "snapshots");
    let mut attribute = before_index.schema().attribute(1000).unwrap().clone();
    attribute.indexed = true;
    let latest = common::transact(
        &service,
        "later",
        second.basis_t,
        &[
            TxOp::AlterAttribute(attribute),
            add(EntityRef::Id(eid), 1000, 4),
        ],
        4000,
    );
    service.shutdown();
    peer.sync().unwrap();
    assert_eq!(peer.basis_t(), latest.basis_t);
    common::assert_same_information(&peer.reopen_snapshot(&reference).unwrap(), &before_index);
    for view in [
        before_index.clone().as_of(first.basis_t),
        before_index.clone().since(first.basis_t),
        before_index
            .clone()
            .as_of(second.basis_t)
            .since(first.basis_t)
            .history(),
    ] {
        let reopened = view
            .snapshot_reference()
            .unwrap()
            .open(&config(url), 8, 1024 * 1024)
            .unwrap();
        assert_eq!(
            view.snapshot_key().unwrap(),
            reopened.snapshot_key().unwrap()
        );
        assert_eq!(
            view.collect_datoms(IndexOrder::Eavt).unwrap(),
            reopened.collect_datoms(IndexOrder::Eavt).unwrap()
        );
        assert_eq!(view.schema(), reopened.schema());
    }
    assert!(peer.reopen_snapshot(&altered(&reference, false)).is_err());
    assert!(peer.reopen_snapshot(&altered(&reference, true)).is_err());
    common::TestStore::connect(url)
        .unwrap()
        .create_database("different", schema())
        .unwrap();
    common::consolidate(url, "different").unwrap();
    assert_eq!(
        Peer::connect(url, "different", 8)
            .unwrap()
            .reopen_snapshot(&reference)
            .unwrap_err()
            .code,
        "peer/identity-mismatch"
    );
    eprintln!(
        "snapshot_keys iterations=10000 construct_plus_clone_us={key_micros} foreground_sql=0 exact_reopen=true no_history_witness=true schema_preserved=true"
    );
}

#[test]
fn postgres_excision_rejects_old_references_without_revoking_retained_values() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED snapshot excision PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "snapshot_excision");
    let url = &fixture.connection;
    common::install(url).unwrap();
    let created = common::TestStore::connect(url)
        .unwrap()
        .create_database("excise", schema())
        .unwrap();
    let service = common::start_service(url, "excise");
    let report = common::transact(
        &service,
        "seed",
        created.basis_t(),
        &[add(EntityRef::Temp("item".into()), 1000, 9)],
        1000,
    );
    let eid = report.tempids["item"];
    let old = report.db_after.clone();
    let reference = old.snapshot_reference().unwrap();
    common::transact(
        &service,
        "excise",
        report.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("request".into()),
            attribute: DB_EXCISE as u32,
            value: atomic_core::TxValue::Entity(EntityRef::Id(eid)),
        }],
        2000,
    );
    service.shutdown();
    let database = atomic_core::DatabaseCatalog::connect(url)
        .unwrap()
        .resolve("excise")
        .unwrap();
    PostgresOperator::connect(url)
        .unwrap()
        .process_excision_requests(&database.database_id)
        .unwrap();
    let peer = Peer::connect(url, "excise", 8).unwrap();
    assert!(peer.db().values(eid, 1000).unwrap().is_empty());
    assert_ne!(
        peer.db().snapshot_key().unwrap().generation(),
        reference.key().generation()
    );
    assert_eq!(
        peer.reopen_snapshot(&reference).unwrap_err().code,
        "snapshot/generation-not-current"
    );
    assert_eq!(
        reference
            .open(&config(url), 8, 1024 * 1024)
            .unwrap_err()
            .code,
        "snapshot/generation-not-current"
    );
    assert_eq!(old.values(eid, 1000).unwrap(), vec![Value::Long(9)]);
}
