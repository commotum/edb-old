use atomic_core::{
    Attribute, Cardinality, EntityRef, ErrorCategory, Keyword, POSTGRES_SCHEMA_VERSION, Peer,
    PostgresIndexer, PostgresMigrator, PostgresStore, PostgresTreeStore, Schema,
    TransactionService, TransactionServiceConfig, TxOp, TxValue, Unique, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

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

fn with_connection_parameter(connection: &str, key: &str, value: &str) -> String {
    if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
        let separator = if connection.ends_with('?') || connection.ends_with('&') {
            ""
        } else if connection.contains('?') {
            "&"
        } else {
            "?"
        };
        format!("{connection}{separator}{key}={value}")
    } else {
        // Values used by this harness are generated from ASCII identifiers and
        // contain neither quotes nor backslashes. Keep the assertion adjacent
        // to interpolation so this helper cannot quietly become SQL/libpq
        // escaping infrastructure.
        assert!(
            !value.contains('\'')
                && !value.contains('\\')
                && !value.chars().any(char::is_whitespace)
        );
        format!("{connection} {key}='{value}'")
    }
}

fn as_role(connection: &str, role: &str, password: &str) -> String {
    let connection = with_connection_parameter(connection, "user", role);
    with_connection_parameter(&connection, "password", password)
}

struct IsolatedSchema {
    admin: Client,
    name: String,
}

impl IsolatedSchema {
    fn create(connection: &str, name: String) -> (Self, String) {
        assert!(
            name.chars()
                .all(|character| character.is_ascii_alphanumeric() || character == '_'),
            "generated schema name must be an unquoted PostgreSQL identifier"
        );
        let mut admin = Client::connect(connection, NoTls).unwrap();
        admin
            .batch_execute(&format!("CREATE SCHEMA \"{name}\""))
            .unwrap();
        let options = format!("-csearch_path={name},pg_catalog");
        let scoped = with_connection_parameter(connection, "options", &options);
        (Self { admin, name }, scoped)
    }
}

impl Drop for IsolatedSchema {
    fn drop(&mut self) {
        // The unique schema contains only this fixture. Isolation makes the
        // destructive future-version row harmless to the shared harness, and
        // this best-effort cleanup also runs during panic unwinding.
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA IF EXISTS \"{}\" CASCADE", self.name));
    }
}

#[test]
fn failed_fresh_install_rolls_back_every_baseline_object() {
    let Some(connection) = connection() else {
        return;
    };
    let (_schema, connection) = IsolatedSchema::create(&connection, unique("baseline_rollback"));
    let mut admin = Client::connect(&connection, NoTls).unwrap();
    // A late CREATE conflict exercises transactional DDL, not a synthetic
    // checksum rejection before the baseline executes. This is our own
    // disposable fixture; the installer must leave its existing data intact.
    admin
        .batch_execute(
            "CREATE TABLE atomic_tree_nodes (fixture_sentinel TEXT NOT NULL); \
             INSERT INTO atomic_tree_nodes VALUES ('keep-existing-data')",
        )
        .unwrap();
    let tables = |admin: &mut Client| {
        admin
            .query(
                "SELECT tablename FROM pg_tables WHERE schemaname=current_schema() ORDER BY tablename",
                &[],
            )
            .unwrap()
            .into_iter()
            .map(|row| row.get::<_, String>(0))
            .collect::<Vec<_>>()
    };
    let before = tables(&mut admin);
    let mut migrator = PostgresMigrator::connect(&connection).unwrap();
    let error = migrator.migrate().unwrap_err();
    assert_eq!(error.code, "postgres/migration-ddl");
    assert_eq!(
        error.details.get("postgres_sqlstate").map(String::as_str),
        Some("42P07")
    );
    assert_eq!(tables(&mut admin), before);
    assert_eq!(
        admin
            .query_one("SELECT fixture_sentinel FROM atomic_tree_nodes", &[])
            .unwrap()
            .get::<_, String>(0),
        "keep-existing-data"
    );
    assert!(
        admin
            .query_one(
                "SELECT to_regclass('atomic_schema_migrations') IS NULL",
                &[]
            )
            .unwrap()
            .get::<_, bool>(0)
    );
    // Removing only our conflicting fixture object permits a clean retry.
    admin.batch_execute("DROP TABLE atomic_tree_nodes").unwrap();
    migrator.migrate().unwrap();
    assert_eq!(
        admin
            .query_one("SELECT count(*) FROM atomic_schema_migrations", &[])
            .unwrap()
            .get::<_, i64>(0),
        1
    );
}

#[test]
fn unsupported_development_ledger_is_rejected_without_mutation() {
    let Some(connection) = connection() else {
        return;
    };
    let (_schema, connection) =
        IsolatedSchema::create(&connection, unique("old_baseline_boundary"));
    let mut admin = Client::connect(&connection, NoTls).unwrap();
    admin
        .batch_execute(
            "CREATE TABLE atomic_schema_migrations (version BIGINT PRIMARY KEY, checksum BYTEA NOT NULL); \
             CREATE TABLE atomic_heads (fixture_sentinel TEXT NOT NULL); \
             INSERT INTO atomic_heads VALUES ('old-database-is-untouched')",
        )
        .unwrap();
    let old = POSTGRES_SCHEMA_VERSION - 1;
    let checksum = vec![0x51_u8; 32];
    admin
        .execute(
            "INSERT INTO atomic_schema_migrations VALUES ($1, $2)",
            &[&old, &checksum],
        )
        .unwrap();
    let mut migrator = PostgresMigrator::connect(&connection).unwrap();
    let migration_error = migrator.migrate().unwrap_err();
    let runtime_error = match Peer::connect(&connection, "unused", 1) {
        Ok(_) => panic!("an old development ledger admitted a runtime reader"),
        Err(error) => error,
    };
    for error in [migration_error, runtime_error] {
        assert_eq!(error.code, "postgres/schema-rebuild-required");
        assert_eq!(error.category, ErrorCategory::Unsupported);
    }
    let rows = admin
        .query(
            "SELECT version, checksum FROM atomic_schema_migrations",
            &[],
        )
        .unwrap();
    assert_eq!(rows.len(), 1);
    assert_eq!(rows[0].get::<_, i64>(0), old);
    assert_eq!(rows[0].get::<_, Vec<u8>>(1), checksum);
    assert_eq!(
        admin
            .query_one("SELECT fixture_sentinel FROM atomic_heads", &[])
            .unwrap()
            .get::<_, String>(0),
        "old-database-is-untouched"
    );
    assert!(
        admin
            .query_one("SELECT to_regclass('atomic_databases') IS NULL", &[])
            .unwrap()
            .get::<_, bool>(0)
    );
}

#[test]
fn current_baseline_checksum_mismatch_rejects_installer_and_runtime() {
    let Some(connection) = connection() else {
        return;
    };
    let (_schema, connection) =
        IsolatedSchema::create(&connection, unique("baseline_checksum_boundary"));
    let mut migrator = PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.create_database("checksum-witness", schema()).unwrap();
    let mut admin = Client::connect(&connection, NoTls).unwrap();
    let before = admin
        .query_one("SELECT basis_t, tx_hash FROM atomic_heads", &[])
        .unwrap();
    let before = (before.get::<_, i64>(0), before.get::<_, Vec<u8>>(1));
    admin
        .execute(
            "UPDATE atomic_schema_migrations SET checksum=set_byte(checksum,0,get_byte(checksum,0)#1)",
            &[],
        )
        .unwrap();
    let corrupt: Vec<u8> = admin
        .query_one("SELECT checksum FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    let migration_error = migrator.migrate().unwrap_err();
    let runtime_error = match Peer::connect(&connection, "checksum-witness", 1) {
        Ok(_) => panic!("a mismatched baseline checksum admitted a reader"),
        Err(error) => error,
    };
    for error in [migration_error, runtime_error] {
        assert_eq!(error.code, "postgres/migration-checksum-mismatch");
    }
    assert_eq!(
        admin
            .query_one("SELECT checksum FROM atomic_schema_migrations", &[])
            .unwrap()
            .get::<_, Vec<u8>>(0),
        corrupt
    );
    let after = admin
        .query_one("SELECT basis_t, tx_hash FROM atomic_heads", &[])
        .unwrap();
    assert_eq!((after.get::<_, i64>(0), after.get::<_, Vec<u8>>(1)), before);
}

#[test]
fn future_schema_fails_before_peer_or_service_reads_database_state() {
    let Some(connection) = connection() else {
        return;
    };
    let (_schema, connection) =
        IsolatedSchema::create(&connection, unique("future_schema_boundary"));
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
    let (_schema, connection) = IsolatedSchema::create(&connection, unique("runtime_role_schema"));
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
    assert_eq!(peer.load_stats().compatibility_materializations, 0);

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
    // Writers retain the UPDATE capability needed for catalog row locks, but
    // cannot enter the owner-only terminal-reclamation predicate called by
    // the mutation guard. Assert that denial independently of table grants.
    let privileges = writer
        .query_one(
            "SELECT has_table_privilege(current_user, 'atomic_databases', 'UPDATE'), \
                    has_function_privilege(current_user, 'atomic_database_reclamation_authorized()', 'EXECUTE')",
            &[],
        )
        .unwrap();
    assert!(privileges.get::<_, bool>(0));
    assert!(!privileges.get::<_, bool>(1));
    let catalog_mutation_error = writer
        .execute(
            "UPDATE atomic_databases SET database_id = database_id WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(
        catalog_mutation_error.as_db_error().unwrap().code().code(),
        "42501"
    );
    assert!(
        catalog_mutation_error
            .as_db_error()
            .unwrap()
            .message()
            .contains("atomic_database_reclamation_authorized")
    );
    // The owner can call that predicate, but an ordinary UPDATE must still
    // reach the independent immutable-catalog rejection, not change data.
    let owner_mutation_error = admin
        .execute(
            "UPDATE atomic_databases SET database_id = database_id WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(
        owner_mutation_error.as_db_error().unwrap().code().code(),
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

    #[cfg(unix)]
    {
        let endpoint =
            atomic_core::LocalTransactionServer::start(service.client(), Default::default())
                .unwrap();
        let connection =
            atomic_core::Connection::connect(&peer_connection, &database_id, 4).unwrap();
        let receipt = connection
            .transact_socket(
                endpoint.endpoint(),
                atomic_core::TransactionRequest::new(
                    "read-role-socket-request",
                    vec![TxOp::Add {
                        entity: EntityRef::Temp("remote".into()),
                        attribute: ITEM_NAME,
                        value: Value::String("via-writer".into()).into(),
                    }],
                ),
                Duration::from_secs(10),
            )
            .unwrap()
            .report
            .unwrap();
        assert_eq!(
            receipt
                .db_after
                .values(receipt.tempids["remote"], ITEM_NAME)
                .unwrap(),
            vec![Value::String("via-writer".into())]
        );
        assert_eq!(connection.load_stats().compatibility_materializations, 0);
        drop(receipt);
        drop(connection);
        drop(endpoint);
    }

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
