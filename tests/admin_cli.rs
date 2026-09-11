//! Actual stock commands against fresh, isolated opaque-storage namespaces.
mod common;

use atomic_core::storage::{CasOutcome, PgBlockStore};
use atomic_core::{DatabaseCatalog, Peer, PostgresConnectionConfig};
use postgres::{Client, NoTls};
use std::process::{Command, Output};

fn command(connection: Option<&str>, args: &[&str]) -> Output {
    let mut command = Command::new(env!("CARGO_BIN_EXE_atomic"));
    for variable in [
        "ATOMIC_POSTGRES_URL",
        "ATOMIC_POSTGRES_TRANSPORT",
        "ATOMIC_POSTGRES_TLS_ROOT",
        "ATOMIC_CONNECT_TIMEOUT_MS",
        "ATOMIC_STATEMENT_TIMEOUT_MS",
        "ATOMIC_LOCK_TIMEOUT_MS",
        "ATOMIC_SSD_CACHE_DIR",
        "ATOMIC_SSD_CACHE_ENTRIES",
        "ATOMIC_SSD_CACHE_BYTES",
    ] {
        command.env_remove(variable);
    }
    if let Some(connection) = connection {
        command
            .env("ATOMIC_POSTGRES_URL", connection)
            .env("ATOMIC_POSTGRES_TRANSPORT", "plaintext");
    }
    command.args(args).output().unwrap()
}
fn success(connection: &str, args: &[&str]) -> String {
    let output = command(Some(connection), args);
    assert!(
        output.status.success(),
        "{args:?}: {}\n{}",
        String::from_utf8_lossy(&output.stdout),
        String::from_utf8_lossy(&output.stderr)
    );
    String::from_utf8(output.stdout).unwrap()
}
fn rejected(connection: Option<&str>, args: &[&str], code: &str) {
    let output = command(connection, args);
    assert!(!output.status.success(), "{args:?} unexpectedly succeeded");
    assert!(
        String::from_utf8_lossy(&output.stderr).contains(code),
        "{args:?}: {}",
        String::from_utf8_lossy(&output.stderr)
    );
}
fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP admin CLI PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    Some(common::PostgresFixture::new(&url, label))
}

#[test]
fn install_lifecycle_inspection_and_collection_use_only_the_selected_opaque_namespace() {
    let Some(fixture) = fixture("admin_current") else {
        return;
    };
    let connection = &fixture.connection;
    let installed = success(connection, &["install"]);
    assert!(installed.contains("INSTALLED storage_format=atomic/opaque-storage/1 tables=2"));
    assert_eq!(success(connection, &["migrate"]), installed);
    let mut sql = Client::connect(connection, NoTls).unwrap();
    let tables: Vec<String> = sql.query(
        "SELECT c.relname::text FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname=$1 AND c.relkind='r' ORDER BY c.relname", &[&fixture.schema]
    ).unwrap().iter().map(|r| r.get(0)).collect();
    assert_eq!(tables, ["atomic_objects", "atomic_refs"]);
    let routines: i64 = sql.query_one("SELECT count(*) FROM pg_catalog.pg_proc p JOIN pg_catalog.pg_namespace n ON n.oid=p.pronamespace WHERE n.nspname=$1", &[&fixture.schema]).unwrap().get(0);
    assert_eq!(routines, 0);
    assert!(success(connection, &["create", "--database", "original"]).contains("CREATED"));
    assert!(success(connection, &["create", "--database", "original"]).contains("EXISTS"));
    let mut catalog = DatabaseCatalog::connect(connection).unwrap();
    let original = catalog.resolve("original").unwrap();
    assert!(success(connection, &["status", "--database", "original"]).contains("basis_t=0"));
    assert!(
        success(connection, &["inspect", "--database", "original"])
            .contains("INSPECT healthy=true")
    );
    assert!(
        success(connection, &["list-databases", "--limit", "1"]).contains(&original.database_id)
    );
    assert!(success(connection, &["list-databases", "--after", "original"]).contains("entries=0"));
    assert!(
        success(
            connection,
            &["rename", "--database", "original", "--new-name", "renamed"]
        )
        .contains("applied=false")
    );
    assert_eq!(catalog.resolve("original").unwrap(), original);
    success(
        connection,
        &[
            "rename",
            "--database",
            "original",
            "--new-name",
            "renamed",
            "--lineage",
            &original.lineage_id,
            "--apply",
        ],
    );
    assert_eq!(
        catalog.resolve("renamed").unwrap().database_id,
        original.database_id
    );
    rejected(
        Some(connection),
        &["inspect", "--database", "original"],
        "catalog/name-not-found",
    );
    success(connection, &["create", "--database", "original"]);
    let replacement = catalog.resolve("original").unwrap();
    assert_ne!(replacement.database_id, original.database_id);
    rejected(
        Some(connection),
        &[
            "delete",
            "--database",
            "original",
            "--lineage",
            &original.lineage_id,
            "--apply",
        ],
        "catalog/identity-mismatch",
    );
    assert!(success(connection, &["delete", "--database", "renamed"]).contains("applied=false"));
    success(
        connection,
        &[
            "delete",
            "--database",
            "renamed",
            "--lineage",
            &original.lineage_id,
            "--apply",
        ],
    );
    assert!(success(connection, &["list-databases", "--retired"]).contains(&original.database_id));
    assert_eq!(catalog.resolve("original").unwrap(), replacement);

    let database: String = sql
        .query_one("SELECT current_database()", &[])
        .unwrap()
        .get(0);
    let mut store =
        PgBlockStore::connect(&PostgresConnectionConfig::plaintext(connection)).unwrap();
    let orphan = store.put(b"CLI test orphan").unwrap();
    let before = store.list_refs(None, 128).unwrap();
    assert!(
        before.len() < 128,
        "tiny fixture reference inventory fits one page"
    );
    let gc = [
        "gc",
        "--postgres-database",
        &database,
        "--catalog-schema",
        &fixture.schema,
        "--older-than-seconds",
        "0",
    ];
    assert!(success(connection, &gc).contains("GC_PREVIEW batch=1 applied=false"));
    assert_eq!(
        store.list_refs(None, 128).unwrap(),
        before,
        "preview is read-only"
    );
    let mut wrong = gc.to_vec();
    wrong[4] = "not_the_selected_schema";
    wrong.push("--apply");
    rejected(Some(connection), &wrong, "cli/target-mismatch");
    assert_eq!(store.list_refs(None, 128).unwrap(), before);
    let mut applied = gc.to_vec();
    applied.extend(["--apply", "--batches", "64"]);
    let collected = success(connection, &applied);
    assert!(collected.contains("cycle_complete=true"), "{collected}");
    assert!(collected.contains("global_quiescence=not-established"));
    assert!(store.get(orphan).unwrap().is_none());
    let retired = [
        "gc-deleted",
        "--storage-id",
        &original.database_id,
        "--lineage",
        &original.lineage_id,
        "--postgres-database",
        &database,
        "--catalog-schema",
        &fixture.schema,
        "--older-than-seconds",
        "0",
    ];
    assert!(success(connection, &retired).contains("applied=false"));
    let mut retire_apply = retired.to_vec();
    retire_apply.extend(["--apply", "--batches", "64"]);
    assert!(success(connection, &retire_apply).contains("cycle_complete=true"));
    assert!(
        success(connection, &["inspect", "--database", "original"])
            .contains("INSPECT healthy=true")
    );
}

fn quoted(identifier: &str) -> String {
    format!("\"{}\"", identifier.replace('"', "\"\""))
}
struct Roles {
    sql: Client,
    namespace: String,
    database: String,
    names: Vec<String>,
}
impl Drop for Roles {
    fn drop(&mut self) {
        // Only these fresh roles' exact grants; no DROP OWNED or session termination.
        for role in &self.names {
            let role = quoted(role);
            let schema = quoted(&self.namespace);
            let database = quoted(&self.database);
            let _ = self.sql.batch_execute(&format!(
                "REVOKE ALL ON TABLE {schema}.atomic_objects, {schema}.atomic_refs FROM {role}; REVOKE ALL ON SCHEMA {schema} FROM {role}; REVOKE CONNECT ON DATABASE {database} FROM {role}; DROP ROLE {role}"
            ));
        }
    }
}
fn role_connection(connection: &str, role: &str) -> String {
    if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
        let encoded: String = role.bytes().map(|b| format!("%{b:02X}")).collect();
        format!(
            "{connection}{}user={encoded}",
            if connection.contains('?') { "&" } else { "?" }
        )
    } else {
        format!(
            "{connection} user='{}'",
            role.replace('\\', "\\\\").replace('\'', "\\'")
        )
    }
}

#[test]
fn installation_grants_generic_runtime_access_without_peer_object_writes() {
    let Some(fixture) = fixture("admin_roles") else {
        return;
    };
    success(&fixture.connection, &["install"]);
    success(&fixture.connection, &["create", "--database", "readable"]);
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    let database: String = sql
        .query_one("SELECT current_database()", &[])
        .unwrap()
        .get(0);
    let suffix = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    let writer = format!("atomic writer\"{suffix}");
    let peer = format!("atomic peer\"{suffix}");
    let mut roles = Roles {
        sql,
        namespace: fixture.schema.clone(),
        database,
        names: vec![],
    };
    for role in [&writer, &peer] {
        roles
            .sql
            .batch_execute(&format!("CREATE ROLE {} LOGIN", quoted(role)))
            .unwrap();
        roles.names.push(role.clone());
    }
    let granted = success(
        &fixture.connection,
        &["install", "--writer-role", &writer, "--peer-role", &peer],
    );
    assert!(
        granted.contains("GRANTED runtime_roles=true additive=true object_delete=operator_only")
    );
    for (role, expected) in [
        (&writer, [true, true, true, false]),
        (&peer, [true, false, false, false]),
    ] {
        for (privilege, allowed) in ["SELECT", "INSERT", "UPDATE", "DELETE"]
            .into_iter()
            .zip(expected)
        {
            let actual: bool = roles
                .sql
                .query_one(
                    "SELECT has_table_privilege($1, $2, $3)",
                    &[
                        role,
                        &format!("{}.atomic_objects", quoted(&fixture.schema)),
                        &privilege,
                    ],
                )
                .unwrap()
                .get(0);
            assert_eq!(actual, allowed, "object privilege {privilege} for {role}");
            let actual: bool = roles
                .sql
                .query_one(
                    "SELECT has_table_privilege($1, $2, $3)",
                    &[
                        role,
                        &format!("{}.atomic_refs", quoted(&fixture.schema)),
                        &privilege,
                    ],
                )
                .unwrap()
                .get(0);
            assert!(actual, "reference privilege {privilege}");
        }
    }
    let peer_url = role_connection(&fixture.connection, &peer);
    let captured = Peer::connect(&peer_url, "readable", 0).unwrap();
    assert_eq!(captured.database_value().basis_t(), 0);
    drop(captured);
    let mut peer_store =
        PgBlockStore::connect(&PostgresConnectionConfig::plaintext(&peer_url)).unwrap();
    assert!(matches!(
        peer_store
            .compare_exchange("test/peer-reference", None, Some(b"opaque"))
            .unwrap(),
        CasOutcome::Applied(_)
    ));
    assert!(peer_store.put(b"forbidden peer upload").is_err());
    let mut peer_sql = Client::connect(&peer_url, NoTls).unwrap();
    assert_eq!(
        peer_sql
            .execute(
                "DELETE FROM atomic_refs WHERE key='test/peer-reference'",
                &[]
            )
            .unwrap(),
        1
    );
    drop(peer_sql);
    drop(peer_store);
    let mut owner_catalog = DatabaseCatalog::connect(&fixture.connection).unwrap();
    let original = owner_catalog.resolve("readable").unwrap();
    for role in [&writer, &peer] {
        let url = role_connection(&fixture.connection, role);
        let mut restricted = DatabaseCatalog::connect(&url).unwrap();
        for result in [
            restricted.rename("readable", "stolen").map(|_| ()),
            restricted.rename("readable", "readable").map(|_| ()),
            restricted.retire("readable").map(|_| ()),
        ] {
            let error = result.unwrap_err();
            assert_eq!(error.category, atomic_core::ErrorCategory::Forbidden);
            assert_eq!(error.code, "storage/operator-required");
        }
        assert_eq!(restricted.resolve("readable").unwrap(), original);
        assert!(restricted.resolve("stolen").is_err());
    }
    let renamed = owner_catalog.rename("readable", "still-readable").unwrap();
    assert_eq!(renamed.database_id, original.database_id);
    assert!(
        owner_catalog
            .retire("still-readable")
            .unwrap()
            .unwrap()
            .retired
    );
}

#[test]
fn installation_refuses_a_nonempty_namespace_and_arguments_fail_before_connecting() {
    for args in [
        vec!["install", "--writer-role", "writer"],
        vec!["migrate", "--writer-role", "same", "--peer-role", "same"],
        vec!["delete", "--database", "missing", "--apply"],
    ] {
        rejected(None, &args, "cli/usage");
    }
    let Some(fixture) = fixture("admin_nonempty") else {
        return;
    };
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    sql.batch_execute("CREATE TABLE unrelated (value TEXT); INSERT INTO unrelated VALUES ('keep')")
        .unwrap();
    rejected(
        Some(&fixture.connection),
        &["install"],
        "storage/schema-not-empty",
    );
    let value: String = sql
        .query_one("SELECT value FROM unrelated", &[])
        .unwrap()
        .get(0);
    assert_eq!(value, "keep");
    let objects: Option<String> = sql
        .query_one("SELECT to_regclass('atomic_objects')::text", &[])
        .unwrap()
        .get(0);
    assert_eq!(objects, None);
}
