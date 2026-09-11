//! Backup capture within retirement grace and same-lineage restoration on a fresh route.
mod common;
use atomic_core::storage::ownership::{BlockCollector, CollectionPhase};
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::time::Duration;

fn collect(config: &PostgresConnectionConfig) {
    let mut collector = BlockCollector::connect(config).unwrap();
    for _ in 0..128 {
        if collector
            .advance(RECOMMENDED_GARBAGE_COLLECTION_AGE, 4096)
            .unwrap()
            .phase
            == CollectionPhase::Complete
        {
            return;
        }
    }
    panic!("bounded fixture collection did not finish");
}

#[test]
fn backup_capture_survives_retirement_within_grace_and_restore_uses_a_fresh_route() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "backup_retirement");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::Many,
        ))
        .unwrap();
    BlockDatabase::create(&config, "source", schema).unwrap();
    let service = common::start_service(&fixture.connection, "source");
    let first = service
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                (0..4)
                    .map(|n| TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: 1000,
                        value: Value::Long(n).into(),
                    })
                    .collect(),
            ),
            Duration::from_secs(30),
        )
        .unwrap();
    service.shutdown();
    let mut catalog = DatabaseCatalog::connect_configured(&config).unwrap();
    let old = catalog.resolve("source").unwrap();
    let peer = Peer::connect(&fixture.connection, "source", 0).unwrap();
    let captured = peer.database_value();
    let reference = captured.snapshot_reference().unwrap();
    let expected = captured.clone().history().datoms(IndexOrder::Eavt).unwrap();
    let directory = common::private_directory();
    let point = PortableBackup::connect_configured(&config)
        .unwrap()
        .backup_database_with_capture_probe("source", directory.path(), || {
            catalog.retire_checked("source", &old.lineage_id).unwrap();
            collect(&config);
            assert_eq!(
                captured.clone().history().datoms(IndexOrder::Eavt).unwrap(),
                expected
            );
        })
        .unwrap();
    assert_eq!(point.lineage_id, old.lineage_id);
    assert!(
        PortableBackup::connect_configured(&config)
            .unwrap()
            .backup_database("source", directory.path())
            .is_err()
    );
    // No reclamation prerequisite: held old values and the restored route may
    // safely share canonical objects while route authority remains distinct.
    let restored = PortableBackup::connect_configured(&config)
        .unwrap()
        .restore_backup(directory.path(), point.basis_t, "source")
        .unwrap();
    assert_eq!(restored.point.basis_t, point.basis_t);
    let fresh = catalog.resolve("source").unwrap();
    assert_ne!(fresh.database_id, old.database_id);
    assert_eq!(fresh.lineage_id, old.lineage_id);
    assert!(
        reference.open(&config, 0, 0).is_err(),
        "old serialized route must not open restored lineage"
    );
    let value = Peer::connect(&fixture.connection, "source", 0)
        .unwrap()
        .database_value();
    assert_eq!(
        value.clone().history().datoms(IndexOrder::Eavt).unwrap(),
        expected
    );
    assert_eq!(
        value.snapshot_reference().unwrap().database_id(),
        fresh.database_id
    );
    assert_eq!(
        captured.clone().history().datoms(IndexOrder::Eavt).unwrap(),
        expected
    );
    let mut operator = PostgresOperator::connect_configured(&config).unwrap();
    let mut complete = false;
    for _ in 0..128 {
        if operator
            .reclaim_retired_database(&old.database_id, &old.lineage_id, Duration::ZERO)
            .unwrap()
            .cycle_complete
        {
            complete = true;
            break;
        }
    }
    assert!(complete);
    assert_eq!(value.values(first.tempids["item"], 1000).unwrap().len(), 4);
    assert_eq!(
        captured.values(first.tempids["item"], 1000).unwrap().len(),
        4
    );
}
