#![cfg(unix)]
mod common;

use atomic_core::edn::{EdnValue, read_edn};
use common::product_support::*;
use std::io::Write;
use std::os::unix::fs::PermissionsExt;
use std::process::{Command, Stdio};
use std::time::Instant;

fn input(mut command: Command, edn: &str) -> EdnValue {
    let mut child = command
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
        .unwrap();
    child
        .stdin
        .take()
        .unwrap()
        .write_all(edn.as_bytes())
        .unwrap();
    read_edn(&success(child.wait_with_output().unwrap())).unwrap()
}

fn data(connection: &str, args: &[&str], edn: &str) -> EdnValue {
    let mut command = atomic();
    configured(&mut command, connection);
    command.args(args);
    input(command, edn)
}

fn rejected(connection: &str, args: &[&str]) -> String {
    let mut command = atomic();
    configured(&mut command, connection);
    let output = command.args(args).output().unwrap();
    assert!(!output.status.success(), "unexpected command success");
    String::from_utf8(output.stderr).unwrap()
}

fn field<'a>(value: &'a EdnValue, name: &str) -> &'a EdnValue {
    let EdnValue::Map(entries) = value else {
        panic!("expected transaction report")
    };
    entries
        .iter()
        .find_map(|(key, value)| {
            matches!(key, EdnValue::Keyword(key) if key.name == name).then_some(value)
        })
        .expect("report field")
}

fn exact_retry(before: &EdnValue, after: &EdnValue) {
    assert_eq!(field(after, "replayed"), &EdnValue::Bool(true));
    for name in [
        "basis-t",
        "tx-hash",
        "tx-data",
        "tempids",
        "db-before-t",
        "db-after-t",
    ] {
        assert_eq!(
            field(before, name),
            field(after, name),
            "changed receipt {name}"
        );
    }
}

const SCHEMA: &str = r#"[{:db/ident :person/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}]"#;
const PERSON: &str = r#"[{:person/name "Alice"}]"#;
const QUERY: &str = r#"[:find ?name :where [_ :person/name ?name]]"#;

#[test]
fn stock_lifecycle_preserves_live_writer_and_receipts_across_rename_and_name_reuse() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual lifecycle CLI: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = Fixture::new(&url);
    if let Some((writer, peer)) = &fixture.roles {
        cli(
            &fixture.admin_url,
            &["migrate", "--writer-role", writer, "--peer-role", peer],
        );
    } else {
        cli(&fixture.admin_url, &["migrate"]);
    }
    let started = Instant::now();
    let created = cli(&fixture.admin_url, &["create", "--database", "customers"]);
    assert!(created.starts_with("CREATED "));
    assert!(cli(&fixture.admin_url, &["create", "--database", "customers"]).starts_with("EXISTS "));
    let mut catalog = atomic_core::DatabaseCatalog::connect(&fixture.admin_url).unwrap();
    let original = catalog.resolve("customers").unwrap();
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("writer.sock");
    let endpoint_text = endpoint.to_str().unwrap();
    let mut writer = Server::start(&fixture.writer_url, "customers", &endpoint);
    let submit = |name, key, forms| {
        data(
            &fixture.peer_url,
            &[
                "transact",
                "--database",
                name,
                "--endpoint",
                endpoint_text,
                "--request-key",
                key,
                "--file",
                "-",
            ],
            forms,
        )
    };
    submit("customers", "schema", SCHEMA);
    let before = submit("customers", "alice", PERSON);
    let preview = cli(
        &fixture.admin_url,
        &["rename", "--database", "customers", "--new-name", "people"],
    );
    assert!(preview.contains("applied=false"));
    assert_eq!(catalog.resolve("customers").unwrap(), original);
    assert!(catalog.resolve("people").is_err());
    rejected(
        &fixture.admin_url,
        &[
            "rename",
            "--database",
            "customers",
            "--new-name",
            "people",
            "--lineage",
            "00000000-0000-0000-0000-000000000000",
            "--apply",
        ],
    );
    cli(
        &fixture.admin_url,
        &[
            "rename",
            "--database",
            "customers",
            "--new-name",
            "people",
            "--lineage",
            &original.lineage_id,
            "--apply",
        ],
    );
    let renamed = catalog.resolve("people").unwrap();
    assert_eq!(renamed.database_id, original.database_id);
    assert_eq!(renamed.lineage_id, original.lineage_id);
    assert!(catalog.resolve("customers").is_err());
    exact_retry(&before, &submit("people", "alice", PERSON));
    assert_eq!(
        data(
            &fixture.peer_url,
            &["query", "--database", "people", "--file", "-"],
            QUERY
        ),
        read_edn(r#"#{["Alice"]}"#).unwrap()
    );
    cli(&fixture.admin_url, &["create", "--database", "customers"]);
    let reused = catalog.resolve("customers").unwrap();
    assert_ne!(reused.database_id, original.database_id);
    assert_ne!(reused.lineage_id, original.lineage_id);
    let replacement_endpoint = directory.path().join("replacement.sock");
    let mut replacement = Server::start(&fixture.writer_url, "customers", &replacement_endpoint);
    for (key, forms) in [("schema", SCHEMA), ("bob", r#"[{:person/name "Bob"}]"#)] {
        data(
            &fixture.peer_url,
            &[
                "transact",
                "--database",
                "customers",
                "--endpoint",
                replacement_endpoint.to_str().unwrap(),
                "--request-key",
                key,
                "--file",
                "-",
            ],
            forms,
        );
    }
    let sources = directory.path().join("sources.edn");
    std::fs::write(&sources, r#"{$other {:database "customers"}}"#).unwrap();
    let mixed = data(
        &fixture.peer_url,
        &[
            "query",
            "--database",
            "people",
            "--sources",
            sources.to_str().unwrap(),
            "--file",
            "-",
        ],
        r#"[:find ?old ?new :in $ $other :where [$ _ :person/name ?old] [$other _ :person/name ?new]]"#,
    );
    assert_eq!(
        mixed,
        read_edn(r#"#{["Alice" "Bob"]}"#).unwrap(),
        "a reused source name must not match the primary's old storage-ID spelling"
    );
    replacement.stop();
    assert!(cli(&fixture.peer_url, &["list-databases", "--limit", "1"]).contains("entries=1"));
    if fixture.roles.is_some() {
        rejected(
            &fixture.writer_url,
            &[
                "delete",
                "--database",
                "people",
                "--lineage",
                &original.lineage_id,
                "--apply",
            ],
        );
        rejected(
            &fixture.peer_url,
            &[
                "rename",
                "--database",
                "people",
                "--new-name",
                "stolen",
                "--lineage",
                &original.lineage_id,
                "--apply",
            ],
        );
    }
    writer.stop();
    let writer = Server::start(&fixture.writer_url, "people", &endpoint);
    exact_retry(&before, &submit("people", "alice", PERSON));
    assert!(cli(&fixture.admin_url, &["delete", "--database", "people"]).contains("applied=false"));
    cli(
        &fixture.admin_url,
        &[
            "delete",
            "--database",
            "people",
            "--lineage",
            &original.lineage_id,
            "--apply",
        ],
    );
    assert!(catalog.resolve("people").is_err());
    assert_eq!(catalog.resolve("customers").unwrap(), reused);
    assert!(
        cli(&fixture.peer_url, &["list-databases", "--retired"]).contains(&original.lineage_id)
    );
    rejected(&fixture.peer_url, &["status", "--database", "people"]);
    // Cleanup kills this fixture's process if retirement has not already stopped it.
    drop(writer);
    cli(
        &fixture.admin_url,
        &[
            "delete",
            "--database",
            "customers",
            "--lineage",
            &reused.lineage_id,
            "--apply",
        ],
    );
    println!(
        "DATABASE_LIFECYCLE_OK preview=true rename_preserves_identity=true name_reuse_fresh=true exact_retry=true mixed_sources=true restricted_roles={} complete_ms={}",
        fixture.roles.is_some(),
        started.elapsed().as_millis()
    );
}

#[test]
fn stock_cli_reclaims_only_the_explicit_retired_identity() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual reclamation CLI: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = Fixture::new(&url);
    cli(&fixture.admin_url, &["migrate"]);
    cli(&fixture.admin_url, &["create", "--database", "temporary"]);
    let mut catalog = atomic_core::DatabaseCatalog::connect(&fixture.admin_url).unwrap();
    let retired = catalog.resolve("temporary").unwrap();
    cli(
        &fixture.admin_url,
        &[
            "delete",
            "--database",
            "temporary",
            "--lineage",
            &retired.lineage_id,
            "--apply",
        ],
    );
    cli(&fixture.admin_url, &["create", "--database", "temporary"]);
    let replacement = catalog.resolve("temporary").unwrap();
    let started = Instant::now();
    let physical: String = postgres::Client::connect(&fixture.admin_url, postgres::NoTls)
        .unwrap()
        .query_one("SELECT current_database()", &[])
        .unwrap()
        .get(0);
    let base = [
        "gc-deleted",
        "--storage-id",
        retired.database_id.as_str(),
        "--lineage",
        retired.lineage_id.as_str(),
        "--postgres-database",
        physical.as_str(),
        "--catalog-schema",
        fixture.schema.as_str(),
        "--older-than-seconds",
        "0",
    ];
    let preview = cli(&fixture.admin_url, &base);
    assert!(preview.contains("applied=false"));
    let mut apply = base.to_vec();
    apply.extend(["--apply", "--batches", "200"]);
    assert!(cli(&fixture.admin_url, &apply).contains("complete=true"));
    assert_eq!(catalog.resolve("temporary").unwrap(), replacement);
    assert!(cli(&fixture.admin_url, &["status", "--database", "temporary"]).contains("basis_t=0"));
    println!(
        "RECLAMATION_CLI_OK preview=true reclaim=true reused_name_untouched=true complete_ms={}",
        started.elapsed().as_millis()
    );
}

#[test]
fn genuine_schema_33_upgrade_preserves_old_receipt_through_rename() {
    let (Ok(url), Some(old)) = (
        std::env::var("ATOMIC_POSTGRES_URL"),
        std::env::var_os("ATOMIC_PRE_LIFECYCLE_BIN"),
    ) else {
        eprintln!(
            "SKIP genuine lifecycle upgrade: requires ATOMIC_POSTGRES_URL and ATOMIC_PRE_LIFECYCLE_BIN"
        );
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "lifecycle_upgrade");
    let old_command = |args: &[&str]| {
        let mut command = Command::new(&old);
        configured(&mut command, &fixture.connection);
        command.args(args);
        command
    };
    success(old_command(&["migrate"]).output().unwrap());
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    assert_eq!(
        sql.query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
            .unwrap()
            .get::<_, i64>(0),
        33
    );
    success(
        old_command(&["create", "--database", "old-name"])
            .output()
            .unwrap(),
    );
    let legacy_long_name = "n".repeat(1200);
    success(
        old_command(&["create", "--database", &legacy_long_name])
            .output()
            .unwrap(),
    );
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("writer.sock");
    let endpoint_text = endpoint.to_str().unwrap();
    let mut writer = Server::spawn(old_command(&[
        "transactor",
        "--database",
        "old-name",
        "--endpoint",
        endpoint_text,
    ]));
    input(
        old_command(&[
            "transact",
            "--database",
            "old-name",
            "--endpoint",
            endpoint_text,
            "--request-key",
            "schema",
            "--file",
            "-",
        ]),
        SCHEMA,
    );
    let before = input(
        old_command(&[
            "transact",
            "--database",
            "old-name",
            "--endpoint",
            endpoint_text,
            "--request-key",
            "alice",
            "--file",
            "-",
        ]),
        PERSON,
    );
    writer.stop();
    let genesis: Vec<u8> = sql
        .query_one(
            "SELECT genesis FROM atomic_databases WHERE database_id='old-name'",
            &[],
        )
        .unwrap()
        .get(0);
    cli(&fixture.connection, &["migrate"]);
    assert_eq!(
        atomic_core::DatabaseCatalog::connect(&fixture.connection)
            .unwrap()
            .resolve(&legacy_long_name)
            .unwrap()
            .database_id,
        legacy_long_name
    );
    assert_eq!(
        sql.query_one(
            "SELECT genesis FROM atomic_databases WHERE database_id='old-name'",
            &[]
        )
        .unwrap()
        .get::<_, Vec<u8>>(0),
        genesis
    );
    let old_rejection = old_command(&["status", "--database", "old-name"])
        .output()
        .unwrap();
    assert!(!old_rejection.status.success());
    assert!(String::from_utf8_lossy(&old_rejection.stderr).contains("postgres/schema-too-new"));
    let entry = atomic_core::DatabaseCatalog::connect(&fixture.connection)
        .unwrap()
        .resolve("old-name")
        .unwrap();
    cli(
        &fixture.connection,
        &[
            "rename",
            "--database",
            "old-name",
            "--new-name",
            "new-name",
            "--lineage",
            &entry.lineage_id,
            "--apply",
        ],
    );
    let mut writer = Server::start(&fixture.connection, "new-name", &endpoint);
    let replay = data(
        &fixture.connection,
        &[
            "transact",
            "--database",
            "new-name",
            "--endpoint",
            endpoint_text,
            "--request-key",
            "alice",
            "--file",
            "-",
        ],
        PERSON,
    );
    exact_retry(&before, &replay);
    writer.stop();
    println!(
        "LIFECYCLE_UPGRADE_OK genuine_old_schema=33 old_startup_fenced=true genesis_unchanged=true renamed_receipt_exact=true"
    );
}

#[test]
fn destructive_lifecycle_options_are_checked_before_connecting() {
    for args in [
        vec!["delete", "--database", "customers", "--apply"],
        vec![
            "rename",
            "--database",
            "customers",
            "--new-name",
            "people",
            "--apply",
        ],
        vec!["list-databases", "--limit", "0"],
        vec!["list-databases", "--limit", "4097"],
        vec![
            "gc-deleted",
            "--storage-id",
            "id",
            "--lineage",
            "lineage",
            "--older-than-seconds",
            "0",
        ],
    ] {
        assert!(rejected("host=invalid", &args).contains("cli/usage"));
    }
}
