//! Generic bounded upload preserves authentication, epoch protection and rollback.
mod common;
use atomic_core::storage::{CasOutcome, Guarded, ObjectWriter, PgBlockStore, RefCondition};
use atomic_core::{
    OperationContext, OperationKind, PostgresConnectionConfig, PostgresIoPolicy, sha256,
};
use std::time::{Duration, Instant};

fn fixture(label: &str) -> Option<(common::PostgresFixture, PostgresConnectionConfig)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block upload PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let f = common::PostgresFixture::new(&url, label);
    let config = PostgresConnectionConfig::plaintext(&f.connection);
    PgBlockStore::install(&config).unwrap();
    Some((f, config))
}

#[test]
fn bounded_batch_uses_fewer_driver_calls_and_authenticates_duplicate_reuse() {
    let Some((_f, config)) = fixture("block_upload_cost") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let payloads = (0u8..66).map(|i| vec![i; 2048]).collect::<Vec<_>>();
    let refs = payloads.iter().map(Vec::as_slice).collect::<Vec<_>>();
    let individual = OperationContext::new(OperationKind::Indexing);
    {
        let _scope = individual.enter();
        for bytes in &refs {
            store.put(bytes).unwrap();
        }
    }
    let bulk = OperationContext::new(OperationKind::Indexing);
    let started = Instant::now();
    let mut duplicate = refs.clone();
    duplicate.extend_from_slice(&refs[..2]);
    let ids = {
        let _scope = bulk.enter();
        ObjectWriter::put_objects(&mut store, &duplicate).unwrap()
    };
    assert_eq!(
        ids,
        duplicate
            .iter()
            .map(|bytes| sha256(bytes))
            .collect::<Vec<_>>()
    );
    assert!(bulk.snapshot().calls < individual.snapshot().calls);
    assert_eq!(store.list_objects(None, 128).unwrap().len(), 66);
    assert!(store.object_read_stats().compressed_hits >= 66);
    for (id, bytes) in ids[..66].iter().zip(&payloads) {
        assert_eq!(store.get(*id).unwrap().as_ref(), Some(bytes));
    }
    eprintln!(
        "BLOCK_UPLOAD nodes=66 repeated=2 individual_calls={} batch_calls={} batch_us={} read_bytes={} write_bytes={}",
        individual.snapshot().calls,
        bulk.snapshot().calls,
        started.elapsed().as_micros(),
        bulk.snapshot().known_payload_read_bytes,
        bulk.snapshot().known_payload_write_bytes
    );
    let invalid = OperationContext::new(OperationKind::Indexing);
    {
        let _scope = invalid.enter();
        assert_eq!(
            store.put_many(&vec![refs[0]; 129]).unwrap_err().code,
            "storage/write-batch-limit"
        );
        let eight_mib = vec![0; 8 * 1024 * 1024];
        assert_eq!(
            store.put_many(&[eight_mib.as_slice(); 9]).unwrap_err().code,
            "storage/write-batch-limit"
        );
        assert!(store.put_many(&[]).unwrap().is_empty());
    }
    assert_eq!(invalid.snapshot().calls, 0);
}

#[test]
fn stale_guards_corrupt_reuse_and_interrupted_insert_cannot_partially_succeed() {
    let Some((f, config)) = fixture("block_upload_atomic") else {
        return;
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let old = match store
        .compare_exchange("guards/build", None, Some(b"first"))
        .unwrap()
    {
        CasOutcome::Applied(value) => value,
        _ => panic!("fresh guard"),
    };
    let guards = [RefCondition {
        key: "guards/build".into(),
        expected: Some(old.revision),
    }];
    store
        .compare_exchange("guards/build", Some(old.revision), Some(b"second"))
        .unwrap();
    assert!(matches!(
        store
            .put_many_protected(&[b"unpublished"], 7, &guards)
            .unwrap(),
        Guarded::Conflict(_)
    ));
    assert!(store.get(sha256(b"unpublished")).unwrap().is_none());
    assert!(matches!(
        store.put_many_protected(&[], 7, &guards).unwrap(),
        Guarded::Conflict(_)
    ));

    let original = b"original-program-like-content";
    let id = store.put(original).unwrap();
    let mut sql = postgres::Client::connect(&f.connection, postgres::NoTls).unwrap();
    let physical: Vec<u8> = sql
        .query_one(
            "SELECT payload FROM atomic_objects WHERE id=$1",
            &[&&id[..]],
        )
        .unwrap()
        .get(0);
    sql.execute(
        "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
        &[&&id[..], &vec![0u8]],
    )
    .unwrap();
    assert!(
        store
            .put_many_protected(&[b"new-before-corruption", original], 7, &[])
            .is_err()
    );
    assert!(
        store
            .get(sha256(b"new-before-corruption"))
            .unwrap()
            .is_none()
    );
    assert_eq!(
        sql.query_one(
            "SELECT protected_epoch FROM atomic_objects WHERE id=$1",
            &[&&id[..]]
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    sql.execute(
        "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
        &[&&id[..], &physical],
    )
    .unwrap();

    let limited = config
        .clone()
        .with_io_policy(PostgresIoPolicy {
            lock_timeout: Some(Duration::from_millis(50)),
            ..Default::default()
        })
        .unwrap();
    let mut waiter = PgBlockStore::connect(&limited).unwrap();
    let mut holding = sql.transaction().unwrap();
    holding
        .query_one(
            "SELECT id FROM atomic_objects WHERE id=$1 FOR UPDATE",
            &[&&id[..]],
        )
        .unwrap();
    let error = waiter
        .put_many_protected(&[b"new-before-interruption", original], 9, &[])
        .unwrap_err();
    assert_eq!(
        error.category,
        atomic_core::ErrorCategory::Busy,
        "{error:?}"
    );
    holding.rollback().unwrap();
    assert!(
        store
            .get(sha256(b"new-before-interruption"))
            .unwrap()
            .is_none()
    );
    assert!(matches!(
        waiter
            .put_many_protected(&[b"new-before-interruption", original], 9, &[])
            .unwrap(),
        Guarded::Applied(_)
    ));
    assert_eq!(
        sql.query_one(
            "SELECT protected_epoch FROM atomic_objects WHERE id=$1",
            &[&&id[..]]
        )
        .unwrap()
        .get::<_, i64>(0),
        9
    );
}
