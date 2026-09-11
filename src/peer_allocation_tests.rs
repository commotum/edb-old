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
