use atomic_core::{
    Attribute, Cardinality, EntityRef, Keyword, PostgresIndexer, PostgresStore, Schema,
    TransactionRequest, TransactionService, TransactionServiceConfig, TxOp, TxValue,
    USER_PARTITION, Value, ValueType, make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::process::Command;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const ITEM_COUNT: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
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

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
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

fn config(connection: &str, database_id: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.into(),
        database_id: database_id.into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: atomic_core::CapacityLimits::default(),
    }
}

fn set(value: i64) -> TransactionRequest {
    TransactionRequest::new(
        format!("set-{value}"),
        vec![TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(value)),
        }],
    )
}

fn assert_same_information(
    left: &impl common::InformationSource,
    right: &impl common::InformationSource,
) {
    common::assert_same_information(left, right);
}

#[test]
fn transactor_adopts_verified_base_and_replays_only_the_exact_tail() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("service_recovery");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut setup = PostgresStore::connect(&connection).unwrap();
    setup.create_database(&database_id, schema()).unwrap();
    drop(setup);

    let first = TransactionService::start(config(&connection, &database_id, "first")).unwrap();
    let first_client = first.client();
    let mut expected = None;
    for value in 1..=4 {
        expected = Some(
            first_client
                .transact(set(value), Duration::from_secs(2))
                .unwrap()
                .db_after,
        );
    }
    first.shutdown();

    let base = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let warm = TransactionService::start(config(&connection, &database_id, "warm")).unwrap();
    assert_eq!(warm.recovery_stats().base_t, base.basis_t);
    assert_eq!(warm.recovery_stats().target_t, base.basis_t);
    assert_eq!(warm.recovery_stats().tail_transactions, 0);
    let warm_client = warm.client();
    for value in 5..=7 {
        expected = Some(
            warm_client
                .transact(set(value), Duration::from_secs(2))
                .unwrap()
                .db_after,
        );
    }
    warm.shutdown();
    let expected = expected.unwrap();

    // A newest corrupt derived root is ignored; the older verified root and
    // authoritative tail remain sufficient. The row is intentionally
    // self-inconsistent without mutating any already-published immutable row.
    let mut sql = Client::connect(&connection, NoTls).unwrap();
    let published = sql
        .query_one(
            "SELECT p.publication_revision, m.basis_t, m.index_basis_t, m.tx_hash, m.state_hash, \
                    m.excision_generation, m.eidx_frontier, m.manifest_version, \
                    m.log_generation, m.lineage_id \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m \
                 ON m.database_id = p.database_id \
                AND m.publication_revision = p.publication_revision \
                AND m.manifest_hash = p.manifest_hash \
              WHERE p.database_id = $1 \
              ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .unwrap();
    let publication_revision = published.get::<_, i64>(0) + 1;
    let basis_t: i64 = published.get(1);
    let index_basis_t: i64 = published.get(2);
    let tx_hash: Vec<u8> = published.get(3);
    let state_hash: Vec<u8> = published.get(4);
    let excision_generation: i64 = published.get(5);
    let eidx_frontier: i64 = published.get(6);
    let manifest_version: i16 = published.get(7);
    let log_generation: i64 = published.get(8);
    let lineage_id: Option<String> = published.get(9);
    let bogus_payload = [0x5A_u8; 48];
    let bogus_hash = sha256(&bogus_payload);
    common::with_replica_triggers_disabled(&mut sql, |sql| {
        sql.execute(
            "INSERT INTO atomic_tree_manifests \
                 (database_id, publication_revision, basis_t, index_basis_t, tx_hash, state_hash, \
                  excision_generation, eidx_frontier, manifest_version, manifest_hash, \
                  payload, log_generation, lineage_id) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)",
            &[
                &database_id,
                &publication_revision,
                &basis_t,
                &index_basis_t,
                &tx_hash,
                &state_hash,
                &excision_generation,
                &eidx_frontier,
                &manifest_version,
                &&bogus_hash[..],
                &&bogus_payload[..],
                &log_generation,
                &lineage_id,
            ],
        )?;
        sql.execute(
            "INSERT INTO atomic_tree_publications \
                 (database_id, publication_revision, basis_t, tx_hash, manifest_hash, \
                  log_generation) \
             VALUES ($1, $2, $3, $4, $5, $6)",
            &[
                &database_id,
                &publication_revision,
                &basis_t,
                &tx_hash,
                &&bogus_hash[..],
                &log_generation,
            ],
        )?;
        Ok(())
    })
    .unwrap();
    drop(sql);

    let restarted =
        TransactionService::start(config(&connection, &database_id, "restart")).unwrap();
    let stats = restarted.recovery_stats();
    assert_eq!(stats.base_t, base.basis_t);
    assert_eq!(stats.target_t, expected.basis_t());
    assert_eq!(stats.tail_transactions, expected.basis_t() - base.basis_t);
    assert_eq!(stats.rejected_manifests, 1);
    let after_fallback = restarted
        .client()
        .transact(set(8), Duration::from_secs(2))
        .unwrap();
    assert_same_information(&after_fallback.db_before, &expected);
    let expected_after_restart = after_fallback.db_after;
    restarted.shutdown();

    if let (Ok(pg_ctl), Ok(data_dir)) = (
        std::env::var("ATOMIC_POSTGRES_CTL"),
        std::env::var("ATOMIC_POSTGRES_DATA"),
    ) {
        assert!(
            Command::new(pg_ctl)
                .args(["-D", &data_dir, "-m", "fast", "-w", "restart"])
                .status()
                .unwrap()
                .success()
        );
        let after_server_restart =
            TransactionService::start(config(&connection, &database_id, "after-pg-restart"))
                .unwrap();
        assert_eq!(
            after_server_restart.recovery_stats().tail_transactions,
            expected.basis_t() + 1 - base.basis_t
        );
        // A counter alone is not recovery evidence. Force the restarted
        // service to assess against its recovered value and compare every
        // current/history index with the pre-restart committed value.
        let after_restart = after_server_restart
            .client()
            .transact(set(9), Duration::from_secs(2))
            .unwrap();
        assert_same_information(&after_restart.db_before, &expected_after_restart);
        after_server_restart.shutdown();
    }
}
