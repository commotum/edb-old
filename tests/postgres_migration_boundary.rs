use atomic_core::{
    Attribute, Cardinality, EntityRef, ErrorCategory, Keyword, POSTGRES_SCHEMA_VERSION, Peer,
    PostgresIndexer, PostgresMigrator, PostgresStore, PostgresTreeStore, Schema,
    TransactionService, TransactionServiceConfig, TxOp, TxValue, Unique, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

#[allow(dead_code)]
mod common;

const ITEM_NAME: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique(prefix: &str) -> String {
    let nanos = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    format!("{prefix}_{}_{}", std::process::id(), nanos)
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
}

// The explicit PostgreSQL harness uses libpq keyword parameters. Appending a
// parameter intentionally replaces any earlier user/password for this test.
fn as_role(connection: &str, role: &str, password: &str) -> String {
    assert!(
        !connection.starts_with("postgres://") && !connection.starts_with("postgresql://"),
        "role witness requires an ATOMIC_POSTGRES_URL in libpq keyword form"
    );
    format!("{connection} user={role} password={password}")
}

#[test]
fn future_schema_fails_before_peer_or_service_reads_database_state() {
    let Some(connection) = connection() else {
        return;
    };
    let mut migrator = PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let future = POSTGRES_SCHEMA_VERSION + 1;
    let mut admin = Client::connect(&connection, NoTls).unwrap();
    admin
        .execute(
            "INSERT INTO atomic_schema_migrations (version, checksum) VALUES ($1, $2)",
            &[&future, &&[0_u8; 32][..]],
        )
        .unwrap();

    let migration_error = migrator.migrate().unwrap_err();
    let peer_error = match Peer::connect(&connection, "does-not-exist", 1) {
        Ok(_) => panic!("future schema unexpectedly admitted a peer"),
        Err(error) => error,
    };
    let indexer_error = match PostgresIndexer::connect(&connection, "does-not-exist") {
        Ok(_) => panic!("future schema unexpectedly admitted an indexer"),
        Err(error) => error,
    };
    let tree_error = match PostgresTreeStore::connect(&connection) {
        Ok(_) => panic!("future schema unexpectedly admitted a tree writer"),
        Err(error) => error,
    };
    let service_error = match TransactionService::start(TransactionServiceConfig {
        connection: connection.clone(),
        database_id: "does-not-exist".into(),
        holder_id: unique("future_holder"),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 1,
        capacity_limits: Default::default(),
    }) {
        Ok(service) => {
            service.shutdown();
            panic!("future schema unexpectedly admitted a transaction service")
        }
        Err(error) => error,
    };

    admin
        .execute(
            "DELETE FROM atomic_schema_migrations WHERE version = $1",
            &[&future],
        )
        .unwrap();
    for error in [
        migration_error,
        peer_error,
        indexer_error,
        tree_error,
        service_error,
    ] {
        assert_eq!(error.code, "postgres/schema-too-new");
        assert_eq!(error.category, ErrorCategory::Unavailable);
    }
}

#[test]
fn granted_runtime_roles_start_and_operate_without_ddl_or_history_mutation() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("runtime_roles");
    let writer_role = unique("atomic_writer");
    let peer_role = unique("atomic_peer");
    let password = unique("role_password");

    let mut migrator = PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.create_database(&database_id, schema()).unwrap();
    let mut admin = Client::connect(&connection, NoTls).unwrap();
    admin
        .batch_execute(&format!(
            "CREATE ROLE \"{writer_role}\" LOGIN PASSWORD '{password}'; \
             CREATE ROLE \"{peer_role}\" LOGIN PASSWORD '{password}'"
        ))
        .unwrap();
    migrator
        .grant_runtime_privileges(&writer_role, &peer_role)
        .unwrap();

    let writer_connection = as_role(&connection, &writer_role, &password);
    let peer_connection = as_role(&connection, &peer_role, &password);
    let service = TransactionService::start(TransactionServiceConfig {
        connection: writer_connection.clone(),
        database_id: database_id.clone(),
        holder_id: unique("least_privilege_holder"),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 4,
        capacity_limits: Default::default(),
    })
    .unwrap();
    let report = common::transact(
        &service,
        "role-request",
        1,
        &[TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM_NAME,
            value: TxValue::Scalar(Value::String("secured".into())),
        }],
        1_000,
    );
    assert_eq!(report.basis_t, 2);
    let mut indexer = PostgresIndexer::connect(&writer_connection, &database_id).unwrap();
    let publication = indexer.consolidate().unwrap();
    assert_eq!(publication.basis_t, 2);
    let peer = Peer::connect(&peer_connection, &database_id, 4).unwrap();
    assert_eq!(peer.db().basis_t(), 2);

    let mut writer = Client::connect(&writer_connection, NoTls).unwrap();
    let ddl_error = writer
        .batch_execute("CREATE TABLE atomic_runtime_must_not_create (id integer)")
        .unwrap_err();
    assert_eq!(ddl_error.as_db_error().unwrap().code().code(), "42501");
    let mutation_error = writer
        .execute(
            "DELETE FROM atomic_transactions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(mutation_error.as_db_error().unwrap().code().code(), "42501");
    let catalog_mutation_error = writer
        .execute(
            "UPDATE atomic_databases SET database_id = database_id WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(
        catalog_mutation_error.as_db_error().unwrap().code().code(),
        "55000"
    );
    let mut peer_client = Client::connect(&peer_connection, NoTls).unwrap();
    let peer_write_error = peer_client
        .execute(
            "UPDATE atomic_heads SET basis_t = basis_t WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(
        peer_write_error.as_db_error().unwrap().code().code(),
        "42501"
    );

    drop(peer);
    drop(peer_client);
    drop(writer);
    drop(indexer);
    service.shutdown();
    let released: bool = admin
        .query_one(
            "SELECT expires_at <= clock_timestamp() FROM atomic_transactor_leases \
             WHERE lease_scope = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert!(released, "service shutdown must release its writer lease");
    admin
        .batch_execute(&format!(
            "DROP OWNED BY \"{writer_role}\"; DROP OWNED BY \"{peer_role}\"; \
             DROP ROLE \"{writer_role}\"; DROP ROLE \"{peer_role}\""
        ))
        .unwrap();
}
