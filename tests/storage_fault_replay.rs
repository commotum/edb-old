//! Seeded current-storage schedule: abandoned prepared indexes, exact outcomes,
//! immutable values and durable consumer restart. This does not simulate a crash;
//! the separately opted-in restart suite exercises PostgreSQL WAL recovery.
mod common;
use atomic_core::persistent_tree::TreeConfig;
use atomic_core::storage::{BlockDatabase, BlockReader, IndexInput, PgBlockStore};
use atomic_core::*;
use std::collections::BTreeMap;
use std::time::Duration;

const WAIT: Duration = Duration::from_secs(30);

#[test]
fn seeded_block_schedule_preserves_publication_receipts_and_consumer_checkpoints() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "block_fault_schedule");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let database = BlockDatabase::create(&config, "schedule", schema.clone()).unwrap();
    let mut expected = Database::new(schema).unwrap();
    let mut peer = Peer::connect(&fixture.connection, "schedule", 0).unwrap();
    let retained = peer.database_value();
    let retained_expected = expected.clone();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let mut entities = BTreeMap::new();
    let mut events = BTreeMap::from([(
        1,
        expected
            .datoms(View::History, IndexOrder::Eavt)
            .into_iter()
            .filter(|d| d.tx == t_to_tx(1).unwrap())
            .collect::<Vec<_>>(),
    )]);
    let seed = std::env::var("ATOMIC_BLOCK_FAULT_SEED")
        .ok()
        .map(|s| s.parse::<u64>().unwrap())
        .unwrap_or(0x39dc_55b0_63e7_a412);
    assert_ne!(seed, 0);
    let mut random = seed;
    for step in 0..8 {
        random ^= random << 13;
        random ^= random >> 7;
        random ^= random << 17;
        let slot = (random % 4) as usize;
        let name = format!("slot-{slot}");
        let entity = entities
            .get(&slot)
            .copied()
            .map(EntityRef::Id)
            .unwrap_or_else(|| EntityRef::Temp(name.clone()));
        let operations = vec![TxOp::Add {
            entity,
            attribute: 1000,
            value: Value::Long((random % 2001) as i64 - 1000).into(),
        }];
        let instant = 1000 + step;
        let pure = expected.with(&operations, instant).unwrap();
        let request = TransactionRequest::new(format!("seed-{seed}-step-{step}"), operations)
            .comparing_basis(expected.basis_t())
            .with_tx_instant(instant);
        let writer = common::start_service(&fixture.connection, "schedule");
        let report = writer.client().transact(request.clone(), WAIT).unwrap();
        assert_eq!(report.tempids, pure.tempids, "seed={seed} step={step}");
        if let Some(entity) = report.tempids.get(&name) {
            entities.insert(slot, *entity);
        }
        common::assert_same_information(&report.db_before, &expected);
        common::assert_same_information(&report.db_after, &pure.db_after);
        expected = pure.db_after;
        events.insert(report.basis_t, report.tx_data.clone());
        writer.shutdown();

        // Preparing and dropping a complete candidate is an explicit interruption
        // before publication: uploaded immutable objects cannot change the root.
        let before = store.read_ref(&database.reference_key()).unwrap();
        let reader = BlockReader::connect(&config, Default::default()).unwrap();
        let input = IndexInput::new(reader.capture(&database.reference_key()).unwrap()).unwrap();
        let prepared = input.prepare(&mut store, &TreeConfig::default()).unwrap();
        assert_eq!(prepared.through_basis(), expected.basis_t());
        drop(prepared);
        assert_eq!(store.read_ref(&database.reference_key()).unwrap(), before);
        common::assert_same_information(&peer.sync().unwrap(), &expected);
        common::blocks::consolidate(&fixture.connection, "schedule").unwrap();
        peer = Peer::connect(
            &fixture.connection,
            "schedule",
            if step % 2 == 0 { 0 } else { 16 },
        )
        .unwrap();
        common::assert_same_information(&peer.database_value(), &expected);
        common::assert_same_information(&retained, &retained_expected);

        let writer = common::start_service(&fixture.connection, "schedule");
        let replay = writer.client().transact(request, WAIT).unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, report.tx_hash);
        assert_eq!(replay.tempids, report.tempids);
        common::assert_same_information(&replay.db_before, &report.db_before);
        common::assert_same_information(&replay.db_after, &report.db_after);
        writer.shutdown();

        let open = || {
            ChangeConsumer::connect(
                &fixture.connection,
                "schedule",
                "consumer",
                Default::default(),
            )
            .unwrap()
        };
        let mut consumer = open();
        while let Some(event) = consumer.next(Duration::ZERO).unwrap() {
            assert_eq!(
                events.get(&event.transaction.t),
                Some(&event.transaction.data)
            );
            drop(consumer);
            consumer = open();
            let retried = consumer.next(Duration::ZERO).unwrap().unwrap();
            assert_eq!(retried, event);
            consumer.acknowledge(&retried.checkpoint).unwrap();
        }
        assert_eq!(consumer.checkpoint().last_t(), expected.basis_t());
        assert_eq!(peer.load_stats().compatibility_materializations, 0);
    }
    eprintln!(
        "BLOCK_FAULT_SCHEDULE seed={seed} steps=8 prepared-but-unpublished=8 exact-retries=8"
    );
}
