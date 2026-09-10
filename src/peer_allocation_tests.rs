//! Exact endpoint memo lifetime: old values cannot inherit a successor's
//! cursor, and physical publication changes cannot invalidate an old proof.
use super::*;
use crate::{Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, Value, ValueType};

struct Fixture {
    connection: String,
    schema: String,
    admin: postgres::Client,
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
        eprintln!("SKIPPED allocation-state PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!(
        "allocation_memo_{}_{}",
        std::process::id(),
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    let mut admin = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let connection =
        if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
            format!(
                "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
                if connection.contains('?') { "&" } else { "?" }
            )
        } else {
            format!("{connection} options='-csearch_path={schema},pg_catalog'")
        };
    Some(Fixture {
        connection,
        schema,
        admin,
    })
}

#[test]
fn allocation_memo_is_exact_rebase_shared_successor_distinct_and_errors_retryable() {
    let Some(fixture) = fixture() else {
        return;
    };
    crate::PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("allocation", "n"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let mut store = crate::PostgresStore::connect(&fixture.connection).unwrap();
    let created = store.create_database("memo", schema).unwrap();
    PostgresIndexer::connect(&fixture.connection, "memo")
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(&fixture.connection, "memo", 8).unwrap();
    let before = peer.tiered_snapshot();
    assert!(
        before.endpoint().generation > 0,
        "new databases use native allocation-capable generation"
    );
    assert!(before.state.reserved_allocation.get().is_none());
    let allocation = before.reserved_allocation().unwrap();
    assert_eq!(allocation, created.reserved_allocation());
    assert!(allocation.is_some());
    let context = crate::OperationContext::new(crate::OperationKind::Application);
    {
        let _scope = context.enter();
        for _ in 0..32 {
            assert_eq!(before.clone().reserved_allocation().unwrap(), allocation);
        }
    }
    assert_eq!(context.snapshot().sql_calls, 0);
    // Keep the eager adapter cached so advancement exercises authenticated
    // v2 replay, not only a fresh full recovery at the successor endpoint.
    let eager_before = peer.try_db_compatibility().unwrap();
    assert_eq!(eager_before.reserved_allocation(), allocation);
    let (rebased, _) = before.rebase_exact(None).unwrap();
    assert!(Arc::ptr_eq(
        &before.state.reserved_allocation,
        &rebased.state.reserved_allocation
    ));

    // A proof failure leaves no memo entry, even when multiple calls use the
    // same retained value. Corrupt only this private endpoint, never storage.
    let mut forged_state = (*before.state).clone();
    forged_state.current_hash[0] ^= 1;
    forged_state.reserved_allocation = Arc::new(OnceLock::new());
    let forged = TieredSnapshot {
        core: Arc::clone(&before.core),
        state: Arc::new(forged_state),
    };
    for _ in 0..2 {
        assert!(forged.reserved_allocation().is_err());
        assert!(forged.state.reserved_allocation.get().is_none());
    }

    let service = crate::TransactionService::start(crate::TransactionServiceConfig {
        connection: fixture.connection.clone(),
        database_id: "memo".into(),
        holder_id: "allocation-memo".into(),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: crate::CapacityLimits::default(),
    })
    .unwrap();
    let report = service
        .client()
        .transact(
            crate::TransactionRequest::new(
                "reserved",
                vec![
                    TxOp::ForcePartition {
                        tempid: "claim".into(),
                        partition: EntityRef::Id(crate::DB_PART_DB),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("claim".into()),
                        attribute: 1000,
                        value: Value::Long(1).into(),
                    },
                ],
            )
            .comparing_basis(created.basis_t())
            .with_tx_instant(1000),
            Duration::from_secs(10),
        )
        .unwrap();
    service.shutdown();
    peer.sync_compatibility().unwrap();
    let after = peer.tiered_snapshot();
    assert!(!Arc::ptr_eq(
        &before.state.reserved_allocation,
        &after.state.reserved_allocation
    ));
    let successor = after.reserved_allocation().unwrap().unwrap();
    assert!(successor.frontier() > allocation.unwrap().frontier());
    assert_eq!(successor.frontier(), report.tempids["claim"] + 1);
    assert_eq!(before.reserved_allocation().unwrap(), allocation);
    assert_eq!(
        report.db_after.reserved_allocation().unwrap(),
        Some(successor)
    );
    let eager_after = peer.try_db_compatibility().unwrap();
    assert_eq!(eager_after.reserved_allocation(), Some(successor));
    assert_eq!(eager_before.reserved_allocation(), allocation);
    let reopened = Peer::connect(&fixture.connection, "memo", 8).unwrap();
    assert_eq!(
        reopened
            .try_db_compatibility()
            .unwrap()
            .reserved_allocation(),
        Some(successor)
    );
    PostgresIndexer::connect(&fixture.connection, "memo")
        .unwrap()
        .consolidate()
        .unwrap();
    peer.refresh_index().unwrap();
    let published = peer.tiered_snapshot();
    assert_eq!(published.endpoint(), after.endpoint());
    assert!(Arc::ptr_eq(
        &published.state.reserved_allocation,
        &after.state.reserved_allocation
    ));
    assert_eq!(published.reserved_allocation().unwrap(), Some(successor));
}

#[test]
fn legacy_generation_zero_retains_identity_and_legacy_allocation_on_reads_and_replay() {
    let Some(fixture) = fixture() else {
        return;
    };
    // Genuine populated v11 authority followed by ordinary migration, not a
    // new generation relabeled zero. The legacy base needs a positive basis:
    // gen0 never supplied an authenticated genesis-tree coordinate.
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    crate::postgres_internal_tests::install_migration_prefix(&mut sql, 11);
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("legacy", "n"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let initial = crate::postgres_internal_tests::provision_legacy_generation_zero_database(
        &mut sql, "legacy", schema,
    );
    crate::PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    PostgresIndexer::connect(&fixture.connection, "legacy")
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(&fixture.connection, "legacy", 8).unwrap();
    let before = peer.database_value();
    assert_eq!(peer.tiered_snapshot().endpoint().generation, 0);
    assert_eq!(before.reserved_allocation().unwrap(), None);
    let eager_before = peer.try_db_compatibility().unwrap();
    assert_eq!(eager_before.reserved_allocation(), None);
    let entity_before = before.entity(crate::DB_IDENT).unwrap().unwrap();
    assert!(entity_before.identity().lineage_id().is_some());
    let original_payload: Vec<u8> = sql
        .query_one(
            "SELECT payload FROM atomic_transactions WHERE database_id = 'legacy' AND basis_t = 1",
            &[],
        )
        .unwrap()
        .get(0);
    // Generation zero remains readable, but new durable writes require an
    // explicit native-generation upgrade. Physical maintenance is not one.
    PostgresIndexer::connect(&fixture.connection, "legacy")
        .unwrap()
        .consolidate()
        .unwrap();
    peer.refresh_index().unwrap();
    let after = peer.database_value();
    assert_eq!(after.basis_t(), initial.basis_t());
    assert_eq!(after.reserved_allocation().unwrap(), None);
    assert_eq!(
        peer.try_db_compatibility().unwrap().reserved_allocation(),
        None
    );
    assert_eq!(eager_before.reserved_allocation(), None);
    let recovered = crate::PostgresStore::connect(&fixture.connection)
        .unwrap()
        .recover("legacy")
        .unwrap();
    assert_eq!(recovered.reserved_allocation(), None);
    let reopened = Peer::connect(&fixture.connection, "legacy", 8).unwrap();
    for value in [
        after,
        recovered.database_value(),
        eager_before.database_value(),
        reopened.database_value(),
    ] {
        assert_eq!(
            value.entity(crate::DB_IDENT).unwrap().unwrap(),
            entity_before
        );
    }
    assert_eq!(before.reserved_allocation().unwrap(), None);
    let retained_payload: Vec<u8> = sql
        .query_one(
            "SELECT payload FROM atomic_transactions WHERE database_id = 'legacy' AND basis_t = 1",
            &[],
        )
        .unwrap()
        .get(0);
    assert_eq!(retained_payload, original_payload);
}
