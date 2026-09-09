use atomic_core::{
    Attribute, Cardinality, EntityRef, Keyword, Peer, PostgresIndexer, PostgresStore, Schema,
    TransactionRequest, TransactionServiceConfig, TransactionStandby, TxOp, TxValue,
    USER_PARTITION, Value, ValueType, make_eid,
};
use postgres::{Client, NoTls};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const ITEM_COUNT: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

#[test]
fn standby_takes_over_abandoned_lease_while_peer_reads_remain_available() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("failover_db");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut setup = PostgresStore::connect(&connection).unwrap();
    let created = setup.create_database(&database_id, schema()).unwrap();
    let initial_basis = created.basis_t();
    drop(setup);
    // Publish the initial native basis before simulating an unavailable
    // writer. Ordinary peer reads never silently reconstruct an eager index.
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let mut observer = Client::connect(&connection, NoTls).unwrap();
    observer
        .execute(
            "INSERT INTO atomic_transactor_leases \
             (lease_scope, holder_id, epoch, expires_at) \
             VALUES ($1, 'dead-process', 1, \
                     clock_timestamp() + 150::bigint * interval '1 millisecond')",
            &[&database_id],
        )
        .unwrap();

    let standby = TransactionStandby::start(
        TransactionServiceConfig {
            connection: connection.clone(),
            database_id: database_id.clone(),
            holder_id: "standby".into(),
            lease_duration: Duration::from_secs(1),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 4,
            capacity_limits: atomic_core::CapacityLimits::default(),
        },
        Duration::from_millis(20),
    )
    .unwrap();
    let peer = Peer::connect(&connection, &database_id, 2).unwrap();
    let old = peer.database_value();
    assert_eq!(old.basis_t(), initial_basis);
    assert_eq!(
        peer.database_value().basis_t(),
        initial_basis,
        "reads do not require a live writer"
    );

    let active = standby.await_active(Duration::from_secs(2)).unwrap();
    let report = active
        .client()
        .transact(
            TransactionRequest::new(
                "after-failover",
                vec![TxOp::Add {
                    entity: EntityRef::Id(user(42)),
                    attribute: ITEM_COUNT,
                    value: TxValue::Scalar(Value::Long(1)),
                }],
            ),
            Duration::from_secs(1),
        )
        .unwrap();
    assert_eq!(report.basis_t, initial_basis + 1);
    assert_eq!(old.basis_t(), initial_basis);
    assert_eq!(
        peer.sync_to(report.basis_t, Duration::from_secs(1))
            .unwrap()
            .basis_t(),
        report.basis_t
    );
    assert!(old.values(user(42), ITEM_COUNT).unwrap().is_empty());
    assert_eq!(
        peer.database_value().values(user(42), ITEM_COUNT).unwrap(),
        [Value::Long(1)]
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

    let lease = observer
        .query_one(
            "SELECT holder_id, epoch FROM atomic_transactor_leases WHERE lease_scope = $1",
            &[&database_id],
        )
        .unwrap();
    assert_eq!(lease.get::<_, String>(0), "standby");
    assert_eq!(lease.get::<_, i64>(1), 2);
    assert_eq!(
        PostgresStore::connect(&connection)
            .unwrap()
            .recover(&database_id)
            .unwrap()
            .basis_t(),
        report.basis_t
    );
    active.shutdown();
}
