use crate::postgres::{CommitFault, MIGRATIONS, PostgresStore};
use crate::state_commitment::checkpoint_state_hash;
use crate::{
    Attribute, Cardinality, Database, DurableTransaction, EntityRef, ErrorCategory, IndexOrder,
    Keyword, Schema, TxOp, TxValue, Unique, Value, ValueType, View, encode_genesis,
    encode_transaction, request_digest, sha256, transaction_hash,
};
use postgres::{Client, NoTls};
use std::collections::BTreeMap;
use std::process::Command;
use std::time::{SystemTime, UNIX_EPOCH};

const ITEM_NAME: u32 = 1_000;
const ITEM_COUNT: u32 = 1_001;

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

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                ITEM_NAME,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            ITEM_COUNT,
            Keyword::new("item", "quantity"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add_item(name: &str, count: i64) -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM_NAME,
            value: TxValue::Scalar(Value::String(name.into())),
        },
        TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(count)),
        },
    ]
}

fn migrated_store(connection: &str) -> PostgresStore {
    let mut migrator = crate::PostgresMigrator::connect(connection).unwrap();
    migrator.migrate().unwrap();
    PostgresStore::connect(connection).unwrap()
}

fn assert_database_eq(left: &Database, right: &Database) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.eidx_frontier(), right.eidx_frontier());
    assert_eq!(left.schema(), right.schema());
    for view in [View::Current, View::History] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(left.datoms(view, order), right.datoms(view, order));
        }
    }
}

fn assert_value_eq_database(left: &crate::DatabaseValue, right: &Database) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.eidx_frontier(), right.eidx_frontier());
    assert_eq!(left.schema(), right.schema());
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            left.datoms(order).unwrap(),
            right.datoms(View::Current, order)
        );
        assert_eq!(
            left.clone().history().datoms(order).unwrap(),
            right.datoms(View::History, order)
        );
    }
}

fn assert_database_values_eq(left: &crate::DatabaseValue, right: &crate::DatabaseValue) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.eidx_frontier(), right.eidx_frontier());
    assert_eq!(left.schema(), right.schema());
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(left.datoms(order).unwrap(), right.datoms(order).unwrap());
        assert_eq!(
            left.clone().history().datoms(order).unwrap(),
            right.clone().history().datoms(order).unwrap()
        );
    }
}

fn publish_native_base(connection: &str, database_id: &str) {
    crate::PostgresIndexer::connect(connection, database_id)
        .unwrap()
        .consolidate()
        .unwrap();
}

fn create_isolated_schema(connection: &str, prefix: &str) -> String {
    let schema = unique(prefix);
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("CREATE SCHEMA \"{schema}\""))
        .unwrap();
    schema
}

fn client_in_schema(connection: &str, schema: &str) -> Client {
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("SET search_path TO \"{schema}\""))
        .unwrap();
    client
}

fn drop_isolated_schema(connection: &str, schema: &str) {
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("DROP SCHEMA \"{schema}\" CASCADE"))
        .unwrap();
}

fn install_migration_prefix(client: &mut Client, through: i64) {
    for (version, sql) in MIGRATIONS.iter().filter(|(version, _)| *version <= through) {
        client.batch_execute(sql).unwrap();
        let checksum = crate::sha256(sql.as_bytes());
        client
            .execute(
                "INSERT INTO atomic_schema_migrations (version, checksum) VALUES ($1, $2)",
                &[version, &&checksum[..]],
            )
            .unwrap();
    }
}

/// Provision the alias-bound generation-zero representation implemented by
/// the v9-v11 catalog. Upgrade fixtures must write that historical shape
/// directly; routing them through the current store would test v14 creation
/// SQL against columns and generation tables that intentionally do not exist.
fn provision_legacy_generation_zero_database(
    client: &mut Client,
    database_id: &str,
    schema: Schema,
) -> Database {
    let schema_ops = schema
        .attributes()
        .cloned()
        .map(TxOp::InstallAttribute)
        .collect::<Vec<_>>();
    let database = Database::new(schema).unwrap();
    let bootstrap = Database::bootstrap().unwrap();
    let genesis = encode_genesis(bootstrap.genesis_datoms()).unwrap();
    let genesis_hash = sha256(&genesis);
    let initial = if schema_ops.is_empty() {
        None
    } else {
        assert_eq!(database.basis_t(), 1);
        let tx = crate::t_to_tx(1).unwrap();
        let envelope = DurableTransaction {
            database_id: database_id.to_owned(),
            basis_t: 1,
            previous_hash: genesis_hash,
            eidx_frontier: database.eidx_frontier(),
            tempids: BTreeMap::new(),
            tx_data: database
                .datoms(View::History, IndexOrder::Eavt)
                .into_iter()
                .filter(|datom| datom.tx == tx)
                .collect(),
        };
        let payload = encode_transaction(&envelope).unwrap();
        let tx_hash = transaction_hash(&payload);
        let state_hash = checkpoint_state_hash(&database).unwrap();
        let request_hash = request_digest(&schema_ops, 0, 0).unwrap();
        Some((payload, tx_hash, state_hash, request_hash))
    };

    let mut transaction = client.transaction().unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_databases (database_id, genesis, genesis_hash) \
             VALUES ($1, $2, $3)",
            &[&database_id, &&genesis[..], &&genesis_hash[..]],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_heads (database_id, basis_t, tx_hash) VALUES ($1, 0, $2)",
            &[&database_id, &&genesis_hash[..]],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_database_generations (database_id, excision_generation) \
             VALUES ($1, 0)",
            &[&database_id],
        )
        .unwrap();
    if let Some((payload, tx_hash, state_hash, request_hash)) = initial {
        transaction
            .execute(
                "INSERT INTO atomic_transactions \
                     (database_id, basis_t, previous_hash, tx_hash, payload, state_hash) \
                 VALUES ($1, 1, $2, $3, $4, $5)",
                &[
                    &database_id,
                    &&genesis_hash[..],
                    &&tx_hash[..],
                    &payload,
                    &&state_hash[..],
                ],
            )
            .unwrap();
        transaction
            .execute(
                "INSERT INTO atomic_requests \
                     (database_id, request_key, request_digest, basis_t, tx_hash) \
                 VALUES ($1, '__atomic/create-schema/v1', $2, 1, $3)",
                &[&database_id, &&request_hash[..], &&tx_hash[..]],
            )
            .unwrap();
        transaction
            .execute(
                "UPDATE atomic_heads SET basis_t = 1, tx_hash = $2 WHERE database_id = $1",
                &[&database_id, &&tx_hash[..]],
            )
            .unwrap();
    }
    transaction.commit().unwrap();
    database
}

fn authoritative_rows(client: &mut Client, database_id: &str) -> Vec<(i64, Vec<u8>, Vec<u8>)> {
    client
        .query(
            "SELECT basis_t, tx_hash, payload FROM atomic_transactions \
             WHERE database_id = $1 ORDER BY basis_t",
            &[&database_id],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get(0), row.get(1), row.get(2)))
        .collect()
}

#[test]
fn fresh_administrative_install_is_complete_and_idempotent() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_fresh");
    let client = client_in_schema(&connection, &isolated);
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    migrator.migrate().unwrap();

    let mut client = client_in_schema(&connection, &isolated);
    let versions: i64 = client
        .query_one("SELECT count(*) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    assert_eq!(versions, crate::POSTGRES_SCHEMA_VERSION);
    drop(client);
    drop(migrator);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn already_current_migrate_does_not_lock_live_log_tables_for_repair() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_current_concurrent");
    let client = client_in_schema(&connection, &isolated);
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    drop(migrator);

    // These are the relation lock modes held by ordinary legacy/native log
    // writers. Before the current-schema fast path became conditional, the
    // program-reference repair requested SHARE in the opposite order and a
    // harmless startup migrate either blocked or deadlocked the writer.
    let mut writer = client_in_schema(&connection, &isolated);
    let mut writer_transaction = writer.transaction().unwrap();
    writer_transaction
        .batch_execute(
            "LOCK TABLE atomic_log_generations IN ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_generation_transactions IN ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_transactions IN ROW EXCLUSIVE MODE",
        )
        .unwrap();

    let mut client = client_in_schema(&connection, &isolated);
    client.batch_execute("SET lock_timeout TO '500ms'").unwrap();
    let mut concurrent_migrator = crate::PostgresMigrator::from_client(client);
    concurrent_migrator.migrate().unwrap();
    drop(concurrent_migrator);
    writer_transaction.rollback().unwrap();
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn populated_pre_v6_catalog_is_rejected_before_any_schema_mutation() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_pre_v6");
    let mut client = client_in_schema(&connection, &isolated);
    install_migration_prefix(&mut client, 5);
    client
        .execute(
            "INSERT INTO atomic_databases \
             (database_id, bootstrap_schema, bootstrap_hash) VALUES ($1, $2, $3)",
            &[&"old", &vec![1_u8, 0, 0, 0], &&[0_u8; 32][..]],
        )
        .unwrap();
    client
        .execute(
            "INSERT INTO atomic_heads (database_id, basis_t, tx_hash) VALUES ($1, 0, $2)",
            &[&"old", &&[0_u8; 32][..]],
        )
        .unwrap();
    let mut migrator = crate::PostgresMigrator::from_client(client);
    let error = migrator.migrate().unwrap_err();
    assert_eq!(error.category, ErrorCategory::Unsupported);
    assert_eq!(error.code, "postgres/upgrade-rebuild-required");

    let mut client = client_in_schema(&connection, &isolated);
    let latest: i64 = client
        .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    let old_column_remains: bool = client
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM information_schema.columns \
             WHERE table_schema = current_schema() AND table_name = 'atomic_databases' \
               AND column_name = 'bootstrap_schema')",
            &[],
        )
        .unwrap()
        .get(0);
    assert_eq!(latest, 5);
    assert!(old_column_remains);
    drop(client);
    drop(migrator);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn populated_v6_log_is_replayed_for_commitments_and_upgrades_without_loss() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_v6");
    let database_id = "canonical-v6";
    let mut client = client_in_schema(&connection, &isolated);
    // Construct canonical v3 logical content using the first schema that has
    // the commitment column, then faithfully remove migrations 7--9. This is
    // the exact v6 authoritative catalog; migrations 7/8 add no log fields.
    install_migration_prefix(&mut client, 9);
    let before = provision_legacy_generation_zero_database(&mut client, database_id, schema());

    let mut client = client_in_schema(&connection, &isolated);
    let rows_before = authoritative_rows(&mut client, database_id);
    client
        .batch_execute(
            "CREATE OR REPLACE FUNCTION atomic_validate_transaction_insert() \
             RETURNS trigger LANGUAGE plpgsql AS $$ \
             DECLARE current_basis bigint; current_hash bytea; BEGIN \
               SELECT basis_t, tx_hash INTO current_basis, current_hash \
                 FROM atomic_heads WHERE database_id = NEW.database_id; \
               IF NOT FOUND THEN RAISE EXCEPTION 'Atomic database head does not exist' \
                 USING ERRCODE = '23503'; END IF; \
               IF NEW.basis_t <> current_basis + 1 OR NEW.previous_hash <> current_hash THEN \
                 RAISE EXCEPTION 'Atomic transaction does not extend the current head' \
                 USING ERRCODE = '40001'; END IF; RETURN NEW; END; $$; \
             DROP TRIGGER IF EXISTS atomic_index_publications_validate_insert \
               ON atomic_index_publications; \
             DROP FUNCTION IF EXISTS atomic_validate_index_publication(); \
             DROP TABLE atomic_index_publications; \
             ALTER TABLE atomic_transactions DROP COLUMN state_hash; \
             ALTER TABLE atomic_programs DROP CONSTRAINT atomic_programs_kind_check; \
             ALTER TABLE atomic_programs ADD CONSTRAINT atomic_programs_kind_check \
               CHECK (kind BETWEEN 0 AND 2); \
             DELETE FROM atomic_schema_migrations WHERE version >= 7",
        )
        .unwrap();
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    drop(migrator);

    let mut client = client_in_schema(&connection, &isolated);
    assert_eq!(authoritative_rows(&mut client, database_id), rows_before);
    let committed: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_transactions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    let authenticated: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_transactions WHERE database_id = $1 \
             AND state_hash <> decode(repeat('00', 32), 'hex')",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert!(committed > 0);
    assert_eq!(authenticated, committed);
    let mut store = PostgresStore::from_client(client);
    assert_database_eq(&store.recover(database_id).unwrap(), &before);
    drop(store);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn populated_v11_to_v12_preserves_authoritative_bytes_and_exact_state() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_v11");
    let database_id = "canonical-v11";
    let mut client = client_in_schema(&connection, &isolated);
    install_migration_prefix(&mut client, 11);
    let before = provision_legacy_generation_zero_database(&mut client, database_id, schema());
    let mut client = client_in_schema(&connection, &isolated);
    let rows_before = authoritative_rows(&mut client, database_id);
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    drop(migrator);

    let mut client = client_in_schema(&connection, &isolated);
    assert_eq!(authoritative_rows(&mut client, database_id), rows_before);
    let latest: i64 = client
        .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    assert_eq!(latest, crate::POSTGRES_SCHEMA_VERSION);
    let mut store = PostgresStore::from_client(client);
    assert_database_eq(&store.recover(database_id).unwrap(), &before);
    drop(store);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn already_current_catalog_repairs_legacy_zero_state_commitments() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_current_zero");
    let database_id = "current-with-zero-commitments";
    let mut client = client_in_schema(&connection, &isolated);
    install_migration_prefix(&mut client, crate::POSTGRES_SCHEMA_VERSION);
    let mut store = PostgresStore::from_client(client);
    let expected = store.create_database(database_id, schema()).unwrap();
    drop(store);

    let mut client = client_in_schema(&connection, &isolated);
    client
        .batch_execute(
            "ALTER TABLE atomic_transactions DISABLE TRIGGER atomic_transactions_immutable; \
             UPDATE atomic_transactions \
                SET state_hash = decode(repeat('00', 32), 'hex') \
              WHERE database_id = 'current-with-zero-commitments'; \
             ALTER TABLE atomic_transactions ENABLE TRIGGER atomic_transactions_immutable",
        )
        .unwrap();
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    drop(migrator);

    let mut client = client_in_schema(&connection, &isolated);
    let remaining: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_transactions \
             WHERE database_id = $1 \
               AND state_hash = decode(repeat('00', 32), 'hex')",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(remaining, 0);
    let mut store = PostgresStore::from_client(client);
    assert_database_eq(&store.recover(database_id).unwrap(), &expected);
    drop(store);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn private_publication_faults_are_invisible_and_unknown_outcome_resolves_once() {
    let Some(connection) = connection() else {
        return;
    };
    for fault in [
        CommitFault::BeforeTransactionInsert,
        CommitFault::AfterTransactionInsert,
        CommitFault::AfterHeadUpdate,
    ] {
        let database_id = unique("private_rollback");
        let mut store = migrated_store(&connection);
        store.create_database(&database_id, schema()).unwrap();
        publish_native_base(&connection, &database_id);
        let error = store
            .transact_with_fault(
                &database_id,
                "request-1",
                1,
                &add_item("rolled-back", 1),
                1_000,
                fault,
            )
            .unwrap_err();
        assert_eq!(error.code, "postgres/injected-failure");
        assert_eq!(store.recover(&database_id).unwrap().basis_t(), 1);
        assert!(
            store
                .resolve_request_outcome(&database_id, "request-1")
                .unwrap()
                .is_none()
        );
    }

    let database_id = unique("private_unknown");
    let mut store = migrated_store(&connection);
    let db_before = store.create_database(&database_id, schema()).unwrap();
    publish_native_base(&connection, &database_id);
    let error = store
        .transact_with_fault(
            &database_id,
            "request-unknown",
            1,
            &add_item("committed", 1),
            1_000,
            CommitFault::AfterCommitBeforeResponse,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::UnknownOutcome);
    let resolved = store
        .resolve_request_outcome(&database_id, "request-unknown")
        .unwrap()
        .expect("the committed request has a durable outcome");
    assert!(resolved.replayed);
    assert_eq!(resolved.basis_t, 2);
    assert_value_eq_database(&resolved.db_before, &db_before);
    assert_eq!(resolved.database.basis_t(), 2);
    let resolved_again = store
        .resolve_request_outcome(&database_id, "request-unknown")
        .unwrap()
        .expect("outcome lookup remains stable");
    assert_eq!(resolved_again.tx_hash, resolved.tx_hash);
    assert_eq!(resolved_again.tempids, resolved.tempids);
    assert_eq!(resolved_again.tx_data, resolved.tx_data);
    assert_database_values_eq(&resolved_again.db_before, &resolved.db_before);
    assert_database_values_eq(&resolved_again.database, &resolved.database);

    // Retrying through the authoritative writer reconstructs exactly the
    // bound old root plus one authenticated transaction, then installs that
    // immutable value without retaining an eager database.
    let replayed = store
        .transact_with_fault(
            &database_id,
            "request-unknown",
            1,
            &add_item("committed", 1),
            1_000,
            CommitFault::None,
        )
        .unwrap();
    assert!(replayed.replayed);
    assert_database_values_eq(&replayed.db_before, &resolved.db_before);
    assert_database_values_eq(&replayed.database, &resolved.database);
    let residency = store.writer_residency_stats(&database_id);
    assert_eq!(residency.eager_database_values, 0);
    assert_eq!(residency.eager_current_facts, 0);
    assert_eq!(residency.eager_history_datoms, 0);

    let fresh = store
        .transact_with_fault(
            &database_id,
            "request-after-replay",
            2,
            &add_item("second", 2),
            2_000,
            CommitFault::None,
        )
        .unwrap();
    assert_eq!(fresh.basis_t, 3);
    let residency = store.writer_residency_stats(&database_id);
    assert_eq!(residency.eager_database_values, 0);
    assert!(residency.publication_revision > 0);
    assert!(residency.recent_datoms > 0);
    assert!(residency.last_transaction_read_datoms > 0);
    assert!(residency.last_transaction_read_bytes > 0);
    assert!(residency.last_commitment_node_visits > 0);
    assert!(residency.last_commitment_node_hashes > 0);
    assert!(residency.last_commitment_leaf_changes > 0);

    let mut verifier = Client::connect(&connection, NoTls).unwrap();
    let bound: (i64, i64) = verifier
        .query_one(
            "SELECT count(*) FILTER (WHERE request_kind = 2), \
                    (SELECT count(*) FROM atomic_generation_request_bases b \
                      WHERE b.database_id = $1) \
               FROM atomic_generation_requests r WHERE r.database_id = $1",
            &[&database_id],
        )
        .map(|row| (row.get(0), row.get(1)))
        .unwrap();
    assert_eq!(bound, (2, 2));
}

#[test]
fn process_death_at_private_precommit_kill_point_is_invisible() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("private_process_death");
    let mut store = migrated_store(&connection);
    store.create_database(&database_id, schema()).unwrap();
    publish_native_base(&connection, &database_id);
    drop(store);

    let status = Command::new(std::env::current_exe().unwrap())
        .args([
            "--ignored",
            "--exact",
            "postgres_internal_tests::postgres_crash_worker",
        ])
        .env("ATOMIC_POSTGRES_URL", &connection)
        .env("ATOMIC_CRASH_DATABASE", &database_id)
        .status()
        .unwrap();
    assert!(!status.success());

    let mut store = PostgresStore::connect(&connection).unwrap();
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 1);
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_generation_transactions \
              WHERE database_id = $1 AND basis_t = 2",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(count, 0);
}

#[test]
#[ignore = "subprocess worker for the process-death test"]
fn postgres_crash_worker() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let Ok(database_id) = std::env::var("ATOMIC_CRASH_DATABASE") else {
        return;
    };
    let mut store = PostgresStore::connect(&connection).unwrap();
    let _ = store.transact_with_fault(
        &database_id,
        "crash-request",
        1,
        &add_item("never-visible", 1),
        1_000,
        CommitFault::AfterHeadUpdateProcessAbort,
    );
    unreachable!();
}
