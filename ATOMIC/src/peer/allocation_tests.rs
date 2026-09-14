//! Allocation checkpoints belong to exact immutable values, not live peers.
//! The fixture exercises the peer facade over the shared immutable read engine.
use crate::storage::{BlockDatabase, PgBlockStore};
use crate::{Attribute, Cardinality, EntityRef, Keyword, Peer, Schema, TxOp, Value, ValueType};
use std::time::Duration;

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
fn allocation_checkpoint_stays_exact_across_successor_indexing_and_reopen() {
    let Some(fixture) = fixture() else {
        return;
    };
    let config = crate::PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("allocation", "n"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    BlockDatabase::create(&config, "memo", schema).unwrap();
    let peer = Peer::connect(&fixture.connection, "memo", 8).unwrap();
    let before = peer.db();
    let allocation = before.reserved_allocation().unwrap();
    assert!(allocation.is_some());
    let context = crate::OperationContext::new(crate::OperationKind::Application);
    {
        let _scope = context.enter();
        for _ in 0..32 {
            assert_eq!(before.clone().reserved_allocation().unwrap(), allocation);
        }
    }
    assert_eq!(context.snapshot().sql_calls, 0);
    let service = crate::TransactionService::start(crate::TransactionServiceConfig {
        connection: config.clone(),
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
            .comparing_basis(before.basis_t())
            .with_tx_instant(1000),
            Duration::from_secs(10),
        )
        .unwrap();
    let after = peer
        .sync_to(report.basis_t, Duration::from_secs(10))
        .unwrap();
    let successor = after.reserved_allocation().unwrap().unwrap();
    assert!(successor.frontier() > allocation.unwrap().frontier());
    assert_eq!(successor.frontier(), report.tempids["claim"] + 1);
    assert_eq!(before.reserved_allocation().unwrap(), allocation);
    assert_eq!(
        report.db_after.reserved_allocation().unwrap(),
        Some(successor)
    );
    let reopened = Peer::connect(&fixture.connection, "memo", 8).unwrap();
    assert_eq!(
        reopened.db().reserved_allocation().unwrap(),
        Some(successor)
    );
    let target = service.client().request_index().unwrap().target_t;
    let published = peer.sync_index(target, Duration::from_secs(10)).unwrap();
    assert_eq!(published.basis_t(), after.basis_t());
    assert_eq!(published.reserved_allocation().unwrap(), Some(successor));
    assert_eq!(after.reserved_allocation().unwrap(), Some(successor));
    assert_eq!(before.reserved_allocation().unwrap(), allocation);
    service.shutdown();
}
