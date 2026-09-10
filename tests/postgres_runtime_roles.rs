use atomic_core::{PostgresMigrator, PostgresStore, Schema};
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::time::{SystemTime, UNIX_EPOCH};

const TABLE_PRIVILEGES: &[&str] = &[
    "SELECT",
    "INSERT",
    "UPDATE",
    "DELETE",
    "TRUNCATE",
    "REFERENCES",
    "TRIGGER",
];
const COLUMN_PRIVILEGES: &[&str] = &["SELECT", "INSERT", "UPDATE", "REFERENCES"];
const PEER_TABLES: &[&str] = &[
    "atomic_database_identities",
    "atomic_database_names",
    "atomic_change_checkpoints",
    "atomic_schema_migrations",
    "atomic_databases",
    "atomic_heads",
    "atomic_transactions",
    "atomic_requests",
    "atomic_index_segments",
    "atomic_index_manifests",
    "atomic_programs",
    "atomic_database_generations",
    "atomic_log_generations",
    "atomic_transaction_contents",
    "atomic_generation_transactions",
    "atomic_generation_requests",
    "atomic_generation_request_bases",
    "atomic_request_base_archives",
    "atomic_request_base_archive_roots",
    "atomic_request_base_archive_completions",
    "atomic_log_generation_activations",
    "atomic_completed_excision_requests",
    "atomic_log_generation_completions",
    "atomic_log_generation_retirements",
    "atomic_index_publications",
    "atomic_tree_nodes",
    "atomic_tree_node_blocks",
    "atomic_fulltext_blocks",
    "atomic_fulltext_projections",
    "atomic_fulltext_pages",
    "atomic_fulltext_page_roots",
    "atomic_tree_manifests",
    "atomic_tree_manifest_roots",
    "atomic_tree_publications",
    "atomic_tree_publication_states",
    "atomic_tree_live_sets",
    "atomic_tree_live_nodes",
    "atomic_tree_retirements",
    "atomic_tree_retired_nodes",
    "atomic_tree_retirement_progress",
    "atomic_semantic_commitment_nodes",
    "atomic_semantic_commitment_roots",
];
const WRITER_SELECT_TABLES: &[&str] = &[
    "atomic_fulltext_page_edges",
    "atomic_fulltext_page_builds",
    "atomic_remote_writer_endpoints",
    "atomic_transactor_leases",
    "atomic_log_generation_checkpoints",
    // The existing invoker semantic-root trigger reads these GC claims to
    // reject publication into a generation whose collection has begun.
    "atomic_log_generation_collection_progress",
    "atomic_log_generation_abandonment_progress",
    "atomic_tree_build_intents",
    "atomic_tree_build_intent_nodes",
    "atomic_tree_delta_headers",
    "atomic_tree_delta_nodes",
    "atomic_generation_request_tempids",
    "atomic_generation_request_bases",
    "atomic_program_generation_refs",
    "atomic_semantic_commitment_nodes",
    "atomic_semantic_commitment_roots",
];
const WRITER_INSERT_TABLES: &[&str] = &[
    "atomic_fulltext_pages",
    "atomic_fulltext_page_edges",
    "atomic_fulltext_page_roots",
    "atomic_fulltext_page_builds",
    "atomic_remote_writer_endpoints",
    "atomic_transactions",
    "atomic_requests",
    "atomic_transactor_leases",
    "atomic_tree_nodes",
    "atomic_tree_node_blocks",
    "atomic_fulltext_blocks",
    "atomic_fulltext_projections",
    "atomic_tree_manifests",
    "atomic_tree_manifest_roots",
    "atomic_programs",
    "atomic_tree_delta_headers",
    "atomic_tree_delta_nodes",
    "atomic_tree_build_intents",
    "atomic_tree_build_intent_nodes",
    "atomic_transaction_contents",
    "atomic_generation_transactions",
    "atomic_generation_requests",
    "atomic_generation_request_bases",
    "atomic_generation_request_tempids",
    "atomic_program_generation_refs",
    "atomic_semantic_commitment_nodes",
    "atomic_semantic_commitment_roots",
];
const WRITER_UPDATE_TABLES: &[&str] = &[
    "atomic_remote_writer_endpoints",
    "atomic_databases",
    "atomic_heads",
    "atomic_transactor_leases",
    "atomic_tree_build_intents",
    "atomic_tree_delta_headers",
];

const WRITER_FUNCTIONS: &[&str] = &[
    "atomic_finish_fulltext_build(bytea)",
    "atomic_discover_remote_writer(text,text)",
    "atomic_apply_tree_publication_work(bytea,bigint)",
    "atomic_finish_tree_build(bytea)",
    "atomic_heartbeat_tree_build(bytea)",
    "atomic_log_generation_pin_key(text,bigint)",
    "atomic_publish_tree(text,bigint,bigint,bytea,bytea)",
    "atomic_request_base_archive_build_live(text,bigint)",
    "atomic_tree_database_build_pin_key(text)",
];
const PEER_FUNCTIONS: &[&str] = &[
    "atomic_log_generation_pin_key(text,bigint)",
    "atomic_discover_remote_writer(text,text)",
];

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

fn quote_identifier(identifier: &str) -> String {
    format!("\"{}\"", identifier.replace('"', "\"\""))
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
        assert!(
            !value.contains('\'')
                && !value.contains('\\')
                && !value.chars().any(char::is_whitespace)
        );
        format!("{connection} {key}='{value}'")
    }
}

struct IsolatedSchema {
    admin: Client,
    name: String,
}

impl IsolatedSchema {
    fn create(connection: &str, name: String) -> (Self, String) {
        assert!(
            name.chars()
                .all(|character| character.is_ascii_alphanumeric() || character == '_')
        );
        let mut admin = Client::connect(connection, NoTls).unwrap();
        admin
            .batch_execute(&format!("CREATE SCHEMA {}", quote_identifier(&name)))
            .unwrap();
        let options = format!("-csearch_path={name},pg_catalog");
        let scoped = with_connection_parameter(connection, "options", &options);
        (Self { admin, name }, scoped)
    }
}

impl Drop for IsolatedSchema {
    fn drop(&mut self) {
        let _ = self.admin.batch_execute(&format!(
            "DROP SCHEMA IF EXISTS {} CASCADE",
            quote_identifier(&self.name)
        ));
    }
}

struct RuntimeRoles {
    admin: Client,
    writer: String,
    peer: String,
    extra_databases: Vec<String>,
    extra_schemas: Vec<String>,
}

impl RuntimeRoles {
    fn create(connection: &str, writer: String, peer: String, password: &str) -> Self {
        let mut admin = Client::connect(connection, NoTls).unwrap();
        admin
            .batch_execute(&format!(
                "CREATE ROLE {} LOGIN PASSWORD '{}'; \
                 CREATE ROLE {} LOGIN PASSWORD '{}'",
                quote_identifier(&writer),
                password,
                quote_identifier(&peer),
                password,
            ))
            .unwrap();
        Self {
            admin,
            writer,
            peer,
            extra_databases: Vec::new(),
            extra_schemas: Vec::new(),
        }
    }

    fn remember_database(&mut self, database: String) {
        self.extra_databases.push(database);
    }

    fn remember_schema(&mut self, schema: String) {
        self.extra_schemas.push(schema);
    }
}

impl Drop for RuntimeRoles {
    fn drop(&mut self) {
        for database in &self.extra_databases {
            let _ = self.admin.batch_execute(&format!(
                "DROP DATABASE IF EXISTS {}",
                quote_identifier(database)
            ));
        }
        for schema in &self.extra_schemas {
            let _ = self.admin.batch_execute(&format!(
                "DROP SCHEMA IF EXISTS {} CASCADE",
                quote_identifier(schema)
            ));
        }
        let _ = self.admin.batch_execute(&format!(
            "DROP OWNED BY {}; DROP OWNED BY {}; \
             DROP ROLE IF EXISTS {}; DROP ROLE IF EXISTS {}",
            quote_identifier(&self.writer),
            quote_identifier(&self.peer),
            quote_identifier(&self.writer),
            quote_identifier(&self.peer),
        ));
    }
}

fn grant_matrix(
    client: &mut Client,
    role: &str,
    privileges: &[&str],
    any_column: bool,
) -> BTreeSet<(String, String)> {
    let privilege_values = privileges
        .iter()
        .map(|privilege| format!("('{}'::text)", privilege))
        .collect::<Vec<_>>()
        .join(", ");
    let predicate = if any_column {
        "has_any_column_privilege($1::name, c.oid, privilege.name)"
    } else {
        "has_table_privilege($1::name, c.oid, privilege.name)"
    };
    client
        .query(
            &format!(
                "SELECT c.relname::text, privilege.name \
                   FROM pg_class c \
                   JOIN pg_namespace n ON n.oid = c.relnamespace \
                  CROSS JOIN (VALUES {privilege_values}) privilege(name) \
                  WHERE n.nspname = current_schema() \
                    AND c.relkind IN ('r', 'p', 'v') \
                    AND c.relname LIKE 'atomic\\_%' ESCAPE '\\' \
                    AND {predicate} \
                  ORDER BY c.relname, privilege.name"
            ),
            &[&role],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get(0), row.get(1)))
        .collect()
}

fn expected_peer_grants() -> BTreeSet<(String, String)> {
    let mut grants: BTreeSet<_> = PEER_TABLES
        .iter()
        .map(|table| ((*table).to_owned(), "SELECT".to_owned()))
        .collect();
    grants.insert(("atomic_change_checkpoints".into(), "INSERT".into()));
    grants.insert(("atomic_change_checkpoints".into(), "UPDATE".into()));
    grants
}

fn expected_writer_grants() -> BTreeSet<(String, String)> {
    let mut expected = expected_peer_grants();
    for table in WRITER_SELECT_TABLES {
        expected.insert(((*table).to_owned(), "SELECT".to_owned()));
    }
    for table in WRITER_INSERT_TABLES {
        expected.insert(((*table).to_owned(), "INSERT".to_owned()));
    }
    for table in WRITER_UPDATE_TABLES {
        expected.insert(((*table).to_owned(), "UPDATE".to_owned()));
    }
    expected
}

fn expected_column_grants(table_grants: &BTreeSet<(String, String)>) -> BTreeSet<(String, String)> {
    table_grants
        .iter()
        .filter(|(_, privilege)| COLUMN_PRIVILEGES.contains(&privilege.as_str()))
        .cloned()
        .collect()
}

fn writable_schemas(client: &mut Client, role: &str) -> Vec<String> {
    client
        .query(
            "SELECT n.nspname \
               FROM pg_namespace n \
              WHERE n.nspname !~ '^pg_' \
                AND n.nspname <> 'information_schema' \
                AND has_schema_privilege($1::name, n.oid, 'CREATE') \
              ORDER BY n.nspname",
            &[&role],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get(0))
        .collect()
}

fn executable_definer_functions(client: &mut Client, role: &str) -> BTreeSet<String> {
    client
        .query(
            "SELECT p.proname::text || '(' || \
                    replace(oidvectortypes(p.proargtypes), ' ', '') || ')' \
               FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace \
              WHERE n.nspname = current_schema() AND p.prosecdef \
                AND p.proname LIKE 'atomic\\_%' ESCAPE '\\' \
                AND has_function_privilege($1::name, p.oid, 'EXECUTE') \
              ORDER BY 1",
            &[&role],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get(0))
        .collect()
}

fn assert_error_code(error: atomic_core::SemanticError, expected: &str) {
    assert_eq!(
        error.code, expected,
        "unexpected PostgreSQL boundary error: {}",
        error.message
    );
}

#[test]
fn runtime_grants_reject_ambient_authority_and_match_the_effective_acl() {
    let Some(connection) = connection() else {
        return;
    };
    let (_schema, connection) = IsolatedSchema::create(&connection, unique("runtime_acl_schema"));
    let mut migrator = PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let shadow_database = unique("runtime_acl_trigger_target");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store
        .create_database(&shadow_database, Schema::new())
        .unwrap();
    drop(store);

    let writer = unique("atomic_acl_writer");
    let peer = unique("atomic_acl_peer");
    let password = unique("atomic_acl_password");
    let mut roles = RuntimeRoles::create(&connection, writer.clone(), peer.clone(), &password);
    let database: String = roles
        .admin
        .query_one("SELECT current_database()", &[])
        .unwrap()
        .get(0);

    roles
        .admin
        .batch_execute(&format!(
            "GRANT CREATE ON DATABASE {} TO {}",
            quote_identifier(&database),
            quote_identifier(&writer),
        ))
        .unwrap();
    let error = migrator
        .grant_runtime_privileges(&writer, &peer)
        .unwrap_err();
    assert_error_code(error, "postgres/runtime-role-not-least-privilege");
    roles
        .admin
        .batch_execute(&format!(
            "REVOKE CREATE ON DATABASE {} FROM {}",
            quote_identifier(&database),
            quote_identifier(&writer),
        ))
        .unwrap();

    let owned_database = unique("runtime_acl_owned_database");
    roles.remember_database(owned_database.clone());
    roles
        .admin
        .batch_execute(&format!(
            "CREATE DATABASE {} OWNER {}",
            quote_identifier(&owned_database),
            quote_identifier(&writer),
        ))
        .unwrap();
    let error = migrator
        .grant_runtime_privileges(&writer, &peer)
        .unwrap_err();
    assert_error_code(error, "postgres/runtime-role-not-least-privilege");
    roles
        .admin
        .batch_execute(&format!(
            "DROP DATABASE {}",
            quote_identifier(&owned_database)
        ))
        .unwrap();

    let shadow_schema = unique("runtime_acl_shadow");
    roles
        .admin
        .batch_execute(&format!(
            "CREATE SCHEMA {}; GRANT CREATE ON SCHEMA {} TO {}",
            quote_identifier(&shadow_schema),
            quote_identifier(&shadow_schema),
            quote_identifier(&writer),
        ))
        .unwrap();
    roles.remember_schema(shadow_schema.clone());
    let error = migrator
        .grant_runtime_privileges(&writer, &peer)
        .unwrap_err();
    assert_error_code(error, "postgres/runtime-role-not-least-privilege");
    roles
        .admin
        .batch_execute(&format!(
            "REVOKE CREATE ON SCHEMA {} FROM {}",
            quote_identifier(&shadow_schema),
            quote_identifier(&writer),
        ))
        .unwrap();

    // A runtime role that owns a routine can replace its body after
    // provisioning; owning a SECURITY DEFINER routine would be an immediate
    // privilege escalation. Reject retained routine ownership independently
    // of the role's current CREATE grants.
    roles
        .admin
        .batch_execute(&format!(
            "GRANT CREATE ON SCHEMA {} TO {}; \
             CREATE FUNCTION {}.runtime_owned_probe() RETURNS BOOLEAN \
                 LANGUAGE SQL AS 'SELECT true'; \
             ALTER FUNCTION {}.runtime_owned_probe() OWNER TO {}; \
             REVOKE CREATE ON SCHEMA {} FROM {}",
            quote_identifier(&shadow_schema),
            quote_identifier(&writer),
            quote_identifier(&shadow_schema),
            quote_identifier(&shadow_schema),
            quote_identifier(&writer),
            quote_identifier(&shadow_schema),
            quote_identifier(&writer),
        ))
        .unwrap();
    let error = migrator
        .grant_runtime_privileges(&writer, &peer)
        .unwrap_err();
    assert_error_code(error, "postgres/runtime-role-not-least-privilege");
    roles
        .admin
        .batch_execute(&format!(
            "DROP FUNCTION {}.runtime_owned_probe()",
            quote_identifier(&shadow_schema)
        ))
        .unwrap();

    roles
        .admin
        .batch_execute(&format!(
            "GRANT UPDATE (basis_t) ON atomic_heads TO {}",
            quote_identifier(&writer)
        ))
        .unwrap();
    let error = migrator
        .grant_runtime_privileges(&writer, &peer)
        .unwrap_err();
    assert_error_code(error, "postgres/runtime-role-not-least-privilege");
    roles
        .admin
        .batch_execute(&format!(
            "REVOKE UPDATE (basis_t) ON atomic_heads FROM {}",
            quote_identifier(&writer)
        ))
        .unwrap();

    roles
        .admin
        .batch_execute("GRANT DELETE ON atomic_transactions TO PUBLIC")
        .unwrap();
    let error = migrator
        .grant_runtime_privileges(&writer, &peer)
        .unwrap_err();
    assert_error_code(error, "postgres/runtime-relations-public-privileges");
    roles
        .admin
        .batch_execute("REVOKE DELETE ON atomic_transactions FROM PUBLIC")
        .unwrap();

    roles
        .admin
        .batch_execute("GRANT UPDATE (basis_t) ON atomic_heads TO PUBLIC")
        .unwrap();
    let error = migrator
        .grant_runtime_privileges(&writer, &peer)
        .unwrap_err();
    assert_error_code(error, "postgres/runtime-relations-public-privileges");
    roles
        .admin
        .batch_execute("REVOKE UPDATE (basis_t) ON atomic_heads FROM PUBLIC")
        .unwrap();

    // Direct grants outside the positive runtime whitelist must be cleared as
    // well. Exercise both a present administrative table and an otherwise
    // unknown `atomic_*` table, standing in for a future migration.
    roles
        .admin
        .batch_execute(&format!(
            "CREATE TABLE atomic_future_extension (id BIGINT PRIMARY KEY); \
             GRANT DELETE ON atomic_log_generation_collection_progress, \
                             atomic_future_extension TO {}",
            quote_identifier(&writer)
        ))
        .unwrap();
    migrator.grant_runtime_privileges(&writer, &peer).unwrap();

    // Runtime roles deliberately retain the database's ordinary TEMP
    // capability. Every Atomic routine must therefore capture the trusted
    // schema first and spell pg_temp explicitly last. This protects both
    // owner functions and SECURITY INVOKER validation triggers, which inherit
    // the writer's lookup path.
    let writer_connection = with_connection_parameter(
        &with_connection_parameter(&connection, "user", &writer),
        "password",
        &password,
    );
    let mut runtime_writer = Client::connect(&writer_connection, NoTls).unwrap();
    runtime_writer
        .batch_execute(
            "CREATE TEMP TABLE atomic_databases \
                 (database_id TEXT PRIMARY KEY, lineage_id TEXT NOT NULL); \
             CREATE TEMP TABLE atomic_generation_transactions \
                 (database_id TEXT NOT NULL, generation BIGINT NOT NULL, \
                  basis_t BIGINT NOT NULL, tx_hash BYTEA NOT NULL, \
                  state_hash BYTEA NOT NULL); \
             CREATE TEMP TABLE atomic_heads \
                 (database_id TEXT PRIMARY KEY, log_generation BIGINT NOT NULL, \
                  basis_t BIGINT NOT NULL)",
        )
        .unwrap();
    runtime_writer
        .execute(
            "INSERT INTO atomic_databases VALUES \
                 ('temp-shadow', '00000000-0000-4000-8000-000000000000')",
            &[],
        )
        .unwrap();
    let shadow_error = runtime_writer
        .query_one(
            "SELECT atomic_tree_database_build_pin_key('temp-shadow')",
            &[],
        )
        .unwrap_err();
    assert_eq!(
        shadow_error
            .as_db_error()
            .expect("owner function returned a PostgreSQL error")
            .code()
            .code(),
        "23503",
        "SECURITY DEFINER lookup was redirected through pg_temp"
    );

    // The manifest table deliberately relies on an invoker trigger for the
    // generation/state binding that PostgreSQL cannot express as a foreign
    // key. Populate a complete counterfeit view in pg_temp. An unpinned
    // trigger would accept this direct insert; the repaired trigger must read
    // the real empty generation and reject it.
    let durable_lineage: String = roles
        .admin
        .query_one(
            "SELECT lineage_id FROM atomic_databases WHERE database_id = $1",
            &[&shadow_database],
        )
        .unwrap()
        .get(0);
    let forged_tx = vec![0x41_u8; 32];
    let forged_state = vec![0x42_u8; 32];
    runtime_writer
        .execute(
            "INSERT INTO atomic_databases VALUES ($1, $2)",
            &[&shadow_database, &durable_lineage],
        )
        .unwrap();
    runtime_writer
        .execute(
            "INSERT INTO atomic_generation_transactions \
                 (database_id, generation, basis_t, tx_hash, state_hash) \
             VALUES ($1, 1, 1, $2, $3)",
            &[&shadow_database, &forged_tx, &forged_state],
        )
        .unwrap();
    runtime_writer
        .execute(
            "INSERT INTO atomic_heads (database_id, log_generation, basis_t) \
             VALUES ($1, 1, 1)",
            &[&shadow_database],
        )
        .unwrap();
    let forged_manifest = vec![0x43_u8; 32];
    let forged_payload = vec![0x01_u8];
    let trigger_error = runtime_writer
        .execute(
            "INSERT INTO atomic_tree_manifests \
                 (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                  excision_generation, eidx_frontier, manifest_version, manifest_hash, \
                  payload, log_generation, lineage_id) \
             VALUES ($1, 1, 1, $2, $3, 1, 1000, 4, $4, $5, 1, $6)",
            &[
                &shadow_database,
                &forged_tx,
                &forged_state,
                &forged_manifest,
                &forged_payload,
                &durable_lineage,
            ],
        )
        .unwrap_err();
    let trigger_database_error = trigger_error
        .as_db_error()
        .expect("invoker validation trigger returned a PostgreSQL error");
    assert_eq!(trigger_database_error.code().code(), "23503");
    assert!(
        trigger_database_error
            .message()
            .contains("complete lineage build"),
        "invoker trigger did not validate against the durable Atomic schema"
    );

    for relation in [
        "atomic_log_generation_collection_progress",
        "atomic_future_extension",
    ] {
        let retained: bool = roles
            .admin
            .query_one(
                "SELECT has_table_privilege($1::name, $2::text, 'DELETE')",
                &[&writer, &relation],
            )
            .unwrap()
            .get(0);
        assert!(!retained, "residual DELETE survived on {relation}");
    }

    let peer_expected = expected_peer_grants();
    let writer_expected = expected_writer_grants();
    assert_eq!(
        grant_matrix(&mut roles.admin, &peer, TABLE_PRIVILEGES, false),
        peer_expected
    );
    assert_eq!(
        grant_matrix(&mut roles.admin, &writer, TABLE_PRIVILEGES, false),
        writer_expected
    );
    assert_eq!(
        grant_matrix(&mut roles.admin, &peer, COLUMN_PRIVILEGES, true),
        expected_column_grants(&peer_expected)
    );
    assert_eq!(
        grant_matrix(&mut roles.admin, &writer, COLUMN_PRIVILEGES, true),
        expected_column_grants(&writer_expected)
    );
    assert_eq!(
        executable_definer_functions(&mut roles.admin, &peer),
        PEER_FUNCTIONS
            .iter()
            .map(|name| (*name).to_owned())
            .collect()
    );
    assert_eq!(
        executable_definer_functions(&mut roles.admin, &writer),
        WRITER_FUNCTIONS
            .iter()
            .map(|name| (*name).to_owned())
            .collect()
    );
    for relation in [
        "atomic_request_base_archives",
        "atomic_request_base_archive_nodes",
        "atomic_request_base_archive_roots",
        "atomic_request_base_archive_completions",
        "atomic_receipt_archive_conversions",
        "atomic_receipt_archive_frontier",
    ] {
        let may_stage_archive: bool = roles
            .admin
            .query_one(
                "SELECT has_table_privilege($1::name, $2::text, 'INSERT')",
                &[&writer, &relation],
            )
            .unwrap()
            .get(0);
        assert!(
            !may_stage_archive,
            "ordinary writer may stage operator-only archive relation {relation}"
        );
    }
    for function in [
        "atomic_complete_request_base_archive(text,bigint,bytea)",
        "atomic_collect_request_base_archive(text,bigint,bytea,bigint,bigint)",
        "atomic_receipt_archive_conversion_context(bytea)",
        "atomic_begin_receipt_archive_conversion(bytea,bigint)",
        "atomic_finish_receipt_archive_conversion(bytea,bigint)",
        "atomic_collect_tree_retirement_unconverted_v22(text,bigint,bytea,bigint,bigint)",
        "atomic_collect_request_base_archive_unconverted_v20(text,bigint,bytea,bigint,bigint)",
    ] {
        let may_manage_archive: bool = roles
            .admin
            .query_one(
                "SELECT has_function_privilege($1::name, $2::text, 'EXECUTE')",
                &[&writer, &function],
            )
            .unwrap()
            .get(0);
        assert!(
            !may_manage_archive,
            "ordinary writer may execute restore/operator-only function {function}"
        );
    }
    let expected_path = format!("{}, pg_catalog, pg_temp", _schema.name);
    let definer_paths = roles
        .admin
        .query(
            "SELECT p.proname::text, \
                    (SELECT option_value FROM pg_options_to_table(p.proconfig) \
                      WHERE option_name = 'search_path') \
               FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace \
              WHERE n.nspname = current_schema() AND p.prosecdef \
                AND p.proname LIKE 'atomic\\_%' ESCAPE '\\'",
            &[],
        )
        .unwrap();
    assert!(!definer_paths.is_empty());
    assert!(
        definer_paths
            .iter()
            .all(|row| row.get::<_, Option<String>>(1).as_deref() == Some(&expected_path))
    );
    let routine_paths = roles
        .admin
        .query(
            "SELECT p.prosecdef, \
                    (SELECT option_value FROM pg_options_to_table(p.proconfig) \
                      WHERE option_name = 'search_path') \
               FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace \
              WHERE n.nspname = current_schema() \
                AND p.proname LIKE 'atomic\\_%' ESCAPE '\\'",
            &[],
        )
        .unwrap();
    assert!(!routine_paths.is_empty());
    assert!(
        routine_paths.iter().any(|row| !row.get::<_, bool>(0)),
        "fixture must include SECURITY INVOKER routines"
    );
    assert!(
        routine_paths
            .iter()
            .all(|row| row.get::<_, Option<String>>(1).as_deref() == Some(&expected_path))
    );
    assert!(writable_schemas(&mut roles.admin, &peer).is_empty());
    assert!(writable_schemas(&mut roles.admin, &writer).is_empty());
    for role in [&peer, &writer] {
        let row = roles
            .admin
            .query_one(
                "SELECT has_schema_privilege($1::name, current_schema(), 'USAGE'), \
                        has_schema_privilege($1::name, current_schema(), 'CREATE'), \
                        has_database_privilege($1::name, current_database(), 'CONNECT'), \
                        has_database_privilege($1::name, current_database(), 'CREATE')",
                &[role],
            )
            .unwrap();
        let usage: bool = row.get(0);
        let create: bool = row.get(1);
        let connect: bool = row.get(2);
        let create_schema: bool = row.get(3);
        assert!(usage);
        assert!(!create);
        assert!(connect);
        assert!(!create_schema);
    }
    // Exercise trigger bodies and exact-source pins with actual runtime login
    // roles, not only ACL introspection. Empty fulltext still publishes a
    // corpus-statistics page and a source-bound immutable header.
    let mut search_schema = Schema::new();
    search_schema
        .install(
            atomic_core::Attribute::new(
                1000,
                atomic_core::Keyword::new("document", "body"),
                atomic_core::ValueType::String,
                atomic_core::Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    PostgresStore::connect(&connection)
        .unwrap()
        .create_database("fulltext_runtime", search_schema)
        .unwrap();
    let mut indexer =
        atomic_core::PostgresIndexer::connect(&writer_connection, "fulltext_runtime").unwrap();
    indexer.consolidate().unwrap();
    assert_eq!(indexer.fulltext_build_error(), None);
    let projection = indexer.rebuild_fulltext().unwrap().unwrap();
    let peer_connection = with_connection_parameter(
        &with_connection_parameter(&connection, "user", &peer),
        "password",
        &password,
    );
    let reader =
        atomic_core::Connection::connect(&peer_connection, "fulltext_runtime", 32).unwrap();
    assert!(
        reader
            .db()
            .fulltext(1000, "amber", &atomic_core::FulltextOptions::default())
            .unwrap()
            .hits
            .is_empty()
    );
    let mut runtime_store = atomic_core::FulltextStore::connect(
        &atomic_core::PostgresConnectionConfig::plaintext(&writer_connection),
    )
    .unwrap();
    assert_error_code(
        runtime_store
            .discard_projection(projection.source_manifest, 1)
            .unwrap_err(),
        "fulltext/operator-required",
    );
    let service = atomic_core::TransactionService::start(atomic_core::TransactionServiceConfig {
        connection: writer_connection.clone(),
        database_id: "fulltext_runtime".into(),
        holder_id: "remote-acl-writer".into(),
        lease_duration: std::time::Duration::from_secs(5),
        renew_interval: std::time::Duration::from_millis(100),
        queue_capacity: 4,
        capacity_limits: Default::default(),
    })
    .unwrap();
    let lease=roles.admin.query_one("SELECT holder_id,epoch FROM atomic_transactor_leases WHERE lease_scope='fulltext_runtime'",&[]).unwrap();
    let holder: String = lease.get(0);
    let epoch: i64 = lease.get(1);
    let instance = vec![19u8; 32];
    runtime_writer.execute("INSERT INTO atomic_remote_writer_endpoints(database_id,lineage_id,holder_id,lease_epoch,instance_id,network_address,tls_server_name,protocol_version) VALUES($1,$2,$3,$4,$5,'127.0.0.1:1','localhost',1)",&[&"fulltext_runtime",&reader.identity().lineage_id(),&holder,&epoch,&instance]).unwrap();
    let discovered = reader
        .discover_remote_writer(&atomic_core::PostgresConnectionConfig::plaintext(
            &peer_connection,
        ))
        .unwrap();
    assert_eq!(discovered.lease_epoch(), epoch as u64);
    assert!(runtime_writer.execute("UPDATE atomic_remote_writer_endpoints SET lease_epoch=lease_epoch+1 WHERE database_id='fulltext_runtime'",&[]).is_err());
    service.shutdown();
    assert_eq!(
        reader
            .discover_remote_writer(&atomic_core::PostgresConnectionConfig::plaintext(
                &peer_connection
            ))
            .unwrap_err()
            .code,
        "remote/no-writer"
    );
}
