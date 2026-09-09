use atomic_core::{
    Attribute, Cardinality, Connection, EntityRef, IndexBoundary, IndexComponents, Keyword,
    PostgresConnectionConfig, PostgresIndexer, PostgresIoPolicy, PostgresMigrator, PostgresStore,
    Schema, TransactionRequest, TxOp, Value, ValueType,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const PAYLOAD: u32 = 1_000;
const ITEMS: usize = 128;
const TIMEOUT: Duration = Duration::from_secs(15);

fn payload(index: usize) -> Value {
    Value::String(format!("item-{index:04}-{}", "x".repeat(1_024)))
}

fn add(entity: EntityRef, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute: PAYLOAD,
        value: value.into(),
    }
}

#[test]
fn explicit_entry_and_byte_caches_preserve_old_values_and_do_not_own_attached_writer() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let id = format!(
        "connection-cache-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    PostgresMigrator::connect(&postgres)
        .unwrap()
        .migrate()
        .unwrap();
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        PAYLOAD,
        Keyword::new("item", "payload"),
        ValueType::String,
        Cardinality::One,
    );
    attribute.indexed = true;
    schema.install(attribute).unwrap();
    PostgresStore::connect(&postgres)
        .unwrap()
        .create_database(&id, schema)
        .unwrap();
    let writer = common::start_service(&postgres, &id);
    let seeded = writer
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                (0..ITEMS)
                    .map(|index| add(EntityRef::Temp(format!("item-{index}")), payload(index)))
                    .collect(),
            ),
            TIMEOUT,
        )
        .unwrap();
    PostgresIndexer::connect(&postgres, &id)
        .unwrap()
        .consolidate()
        .unwrap();

    // The configured constructor owns the same explicit PostgreSQL policy as
    // existing Connection::connect_configured, while cache entries and bytes
    // are now independent. One entry provably evicts during cross-index reads.
    let connection = Connection::connect_configured_with_cache_limits(
        PostgresConnectionConfig::plaintext(&postgres)
            .with_io_policy(PostgresIoPolicy {
                connect_timeout: Some(Duration::from_secs(1)),
                statement_timeout: Some(TIMEOUT),
                ..PostgresIoPolicy::default()
            })
            .unwrap(),
        &id,
        1,
        512 * 1024,
    )
    .unwrap();
    let clone = connection.clone();
    assert_eq!(connection.recent_stats().datoms, 0);
    let old = connection.db();
    let old_basis = old.basis_t();
    let boundary = IndexBoundary::Aevt(IndexComponents::One(PAYLOAD));
    let mut retained_cursor = old.seek_cursor(&boundary).unwrap();
    let first = retained_cursor.next().unwrap().unwrap();
    let before = connection.load_stats();
    for index in [0, 32, 64, 96, 127] {
        assert_eq!(
            old.values(seeded.tempids[&format!("item-{index}")], PAYLOAD)
                .unwrap(),
            [payload(index)]
        );
        let datom = old
            .seek_cursor(&IndexBoundary::Avet(IndexComponents::Two(
                PAYLOAD,
                payload(index),
            )))
            .unwrap()
            .next()
            .unwrap()
            .unwrap();
        assert_eq!(datom.entity, seeded.tempids[&format!("item-{index}")]);
    }
    let mut count = 1;
    for datom in retained_cursor {
        let datom = datom.unwrap();
        assert_eq!(datom.attribute, PAYLOAD);
        count += 1;
    }
    assert_eq!(count, ITEMS);
    assert_eq!(first.attribute, PAYLOAD);
    let cache = clone.cache_stats();
    assert!(cache.evictions > 0, "{cache:?}");
    assert!(cache.current_entries <= 1 && cache.peak_entries <= 1);
    assert!(cache.current_bytes <= 512 * 1024 && cache.peak_bytes <= 512 * 1024);
    let after = connection.load_stats();
    assert!(after.cursor_sql_reads > before.cursor_sql_reads);
    assert!(after.cursor_sql_read_bytes > before.cursor_sql_read_bytes);
    assert_eq!(after.compatibility_materializations, 0);

    // A nonzero entry budget must not override a zero or undersized byte
    // budget. Cache bypass changes I/O, never the immutable information.
    for bytes in [0, 1] {
        let uncached = Connection::connect_with_cache_limits(&postgres, &id, 32, bytes).unwrap();
        assert_eq!(
            uncached
                .db()
                .values(seeded.tempids["item-0"], PAYLOAD)
                .unwrap(),
            [payload(0)]
        );
        let stats = uncached.cache_stats();
        assert_eq!(stats.current_entries, 0);
        assert_eq!(stats.current_bytes, 0);
        assert!(stats.oversized_bypasses > 0);
        assert_eq!(uncached.load_stats().compatibility_materializations, 0);
    }

    connection.attach_writer(writer.client()).unwrap();
    let updated = connection
        .transact(
            TransactionRequest::new(
                "update",
                vec![add(
                    EntityRef::Id(seeded.tempids["item-0"]),
                    Value::String("updated".into()),
                )],
            ),
            TIMEOUT,
        )
        .unwrap();
    assert_eq!(
        clone.sync_to(updated.basis_t, TIMEOUT).unwrap().basis_t(),
        updated.basis_t
    );
    assert_eq!(old.basis_t(), old_basis);
    assert_eq!(
        old.values(seeded.tempids["item-0"], PAYLOAD).unwrap(),
        [payload(0)]
    );
    let current = clone.db();
    assert_eq!(
        current.values(seeded.tempids["item-0"], PAYLOAD).unwrap(),
        [Value::String("updated".into())]
    );
    assert_eq!(clone.recent_stats().end_t, updated.basis_t);
    assert_eq!(clone.load_stats().compatibility_materializations, 0);
    drop(connection);
    drop(clone);
    // Attaching the submission endpoint never grants ownership of the writer.
    let continued = writer
        .client()
        .transact(
            TransactionRequest::new(
                "after-readers-dropped",
                vec![add(
                    EntityRef::Temp("late".into()),
                    Value::String("late".into()),
                )],
            ),
            TIMEOUT,
        )
        .unwrap();
    assert_eq!(continued.basis_t, updated.basis_t + 1);
    writer.shutdown();
    assert_eq!(
        old.values(seeded.tempids["item-0"], PAYLOAD).unwrap(),
        [payload(0)]
    );
    assert_eq!(
        current.values(seeded.tempids["item-0"], PAYLOAD).unwrap(),
        [Value::String("updated".into())]
    );
    eprintln!(
        "Connection cache limits: {ITEMS} x 1KiB payloads; one-entry cache {cache:?}; selective cursor reads {}, bytes {}; zero/one-byte budgets bypassed, old cursor/value and non-owning writer survived",
        after.cursor_sql_reads - before.cursor_sql_reads,
        after.cursor_sql_read_bytes - before.cursor_sql_read_bytes,
    );
}
