#![cfg(unix)]
mod common;
use atomic_core::edn::{EdnValue, read_edn};
use common::product_support::*;
use std::io::Write;
use std::os::unix::fs::PermissionsExt;
use std::path::Path;
use std::process::Stdio;
use std::time::Instant;

fn data(connection: &str, arguments: &[&str], text: &str) -> String {
    let mut command = atomic();
    configured(&mut command, connection);
    let mut child = command
        .args(arguments)
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
        .unwrap();
    child
        .stdin
        .take()
        .unwrap()
        .write_all(text.as_bytes())
        .unwrap();
    let result = success(child.wait_with_output().unwrap());
    read_edn(&result).unwrap();
    result
}
fn field(text: &str, name: &str) -> EdnValue {
    let EdnValue::Map(fields) = read_edn(text).unwrap() else {
        panic!("expected map")
    };
    fields
        .iter()
        .find_map(|(key, value)| {
            matches!(key, EdnValue::Keyword(key) if key.name == name).then(|| value.clone())
        })
        .unwrap_or_else(|| panic!("missing field {name}"))
}
fn transact(connection: &str, endpoint: &Path, request: &str, text: &str) -> String {
    data(
        connection,
        &[
            "transact",
            "--database",
            "edn",
            "--endpoint",
            endpoint.to_str().unwrap(),
            "--request-key",
            request,
            "--file",
            "-",
        ],
        text,
    )
}

#[test]
fn malformed_edn_is_rejected_before_database_access_without_echoing_values() {
    let mut command = atomic();
    command.env_remove("ATOMIC_POSTGRES_URL").args([
        "query",
        "--database",
        "absent",
        "--file",
        "-",
    ]);
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
        .write_all(b"[:find \"private-input-marker\"] trailing")
        .unwrap();
    let result = child.wait_with_output().unwrap();
    assert!(!result.status.success());
    let error = String::from_utf8_lossy(&result.stderr);
    assert!(error.contains("edn/"), "{error}");
    assert!(!error.contains("private-input-marker"));
    assert!(result.stdout.is_empty());
}

#[test]
fn actual_edn_commands_install_transact_preview_query_pull_history_and_retry() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED actual EDN CLI: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = Fixture::new(&connection);
    if let Some((writer, peer)) = &fixture.roles {
        cli(
            &fixture.admin_url,
            &["migrate", "--writer-role", writer, "--peer-role", peer],
        );
    } else {
        cli(&fixture.admin_url, &["migrate"]);
    }
    cli(&fixture.admin_url, &["create", "--database", "edn"]);
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("transactor.sock");
    let mut server = Server::start(&fixture.writer_url, "edn", &endpoint);
    let schema = r#"[{:db/ident :person/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
      {:db/ident :person/email :db/valueType :db.type/string :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
      {:db/ident :person/aliases :db/valueType :db.type/string :db/cardinality :db.cardinality/many}
      {:db/ident :person/friends :db/valueType :db.type/ref :db/cardinality :db.cardinality/many} ]"#;
    assert_eq!(
        field(
            &transact(&fixture.peer_url, &endpoint, "schema", schema),
            "committed"
        ),
        EdnValue::Bool(true)
    );
    let alice = r#"[{:person/name "Alice" :person/email "alice@example.com" :person/aliases ["Al" "A"]
      :person/friends [{:person/name "Bob" :person/email "bob@example.com"}]}]"#;
    let first = transact(&fixture.peer_url, &endpoint, "alice", alice);
    assert_eq!(field(&first, "replayed"), EdnValue::Bool(false));
    let query = "[:find ?e ?name :where [?e :person/name ?name]]";
    let result = data(
        &fixture.peer_url,
        &["query", "--database", "edn", "--file", "-"],
        query,
    );
    assert!(result.contains("Alice") && result.contains("Bob"));
    let pull = "[:person/name :person/aliases {:person/friends [:person/name]}]";
    let pulled = data(
        &fixture.peer_url,
        &[
            "pull",
            "--database",
            "edn",
            "--file",
            "-",
            "--entity",
            "[:person/email \"alice@example.com\"]",
        ],
        pull,
    );
    assert!(pulled.contains("Alice") && pulled.contains("Bob") && pulled.contains("Al"));
    let preview = data(
        &fixture.peer_url,
        &["with", "--database", "edn", "--file", "-"],
        r#"[{:db/id [:person/email "alice@example.com"] :person/name "Never committed"}]"#,
    );
    assert_eq!(field(&preview, "committed"), EdnValue::Bool(false));
    assert!(
        !data(
            &fixture.peer_url,
            &["query", "--database", "edn", "--file", "-"],
            query
        )
        .contains("Never committed")
    );
    transact(
        &fixture.peer_url,
        &endpoint,
        "rename",
        r#"[{:db/id [:person/email "alice@example.com"] :person/name "Alicia"}]"#,
    );
    let updated = data(
        &fixture.peer_url,
        &[
            "pull",
            "--database",
            "edn",
            "--file",
            "-",
            "--entity",
            "[:person/email \"alice@example.com\"]",
        ],
        pull,
    );
    assert!(updated.contains("Alicia") && updated.contains("Bob") && updated.contains("Al"));
    let history = data(
        &fixture.peer_url,
        &["query", "--database", "edn", "--file", "-", "--history"],
        "[:find ?name ?added :where [?e :person/name ?name ?tx ?added]]",
    );
    assert!(history.contains("Alice") && history.contains("Alicia") && history.contains("false"));
    // Both file input and explicit :in data are exercised without Rust AST construction.
    let mut input_file = tempfile::NamedTempFile::new().unwrap();
    input_file.write_all(br#"["Alicia"]"#).unwrap();
    let filtered = data(
        &fixture.peer_url,
        &[
            "query",
            "--database",
            "edn",
            "--file",
            "-",
            "--inputs",
            input_file.path().to_str().unwrap(),
        ],
        "[:find ?e :in $ ?name :where [?e :person/name ?name]]",
    );
    assert!(matches!(read_edn(&filtered).unwrap(), EdnValue::Set(rows) if rows.len() == 1));
    server.stop();
    // A peer query is independent of writer availability.
    assert!(
        data(
            &fixture.peer_url,
            &["query", "--database", "edn", "--file", "-"],
            query
        )
        .contains("Alicia")
    );
    let mut server = Server::start(&fixture.writer_url, "edn", &endpoint);
    let replay = transact(&fixture.peer_url, &endpoint, "alice", alice);
    for name in [
        "basis-t",
        "tx-hash",
        "tx-data",
        "tempids",
        "db-before-t",
        "db-after-t",
    ] {
        assert_eq!(field(&first, name), field(&replay, name), "{name}");
    }
    assert_eq!(field(&replay, "replayed"), EdnValue::Bool(true));
    assert!(
        data(
            &fixture.peer_url,
            &["query", "--database", "edn", "--file", "-"],
            query
        )
        .contains("Alicia")
    );
    println!(
        "EDN_APPLICATION_OK schema=true maps=true nested=true lookup=true omitted_preserved=true preview_isolated=true history=true independent_reads=true restart_exact_retry=true restricted_roles={}",
        fixture.roles.is_some()
    );

    for count in [32, 128, 512] {
        let text = format!("[{}]", (0..count).map(|index| format!("{{:person/name \"Measure-{count}-{index}\" :person/email \"{count}-{index}@example.com\"}} ")).collect::<String>());
        let start = Instant::now();
        let parsed = read_edn(&text).unwrap();
        let parse_us = start.elapsed().as_micros();
        let start = Instant::now();
        let printed = atomic_core::edn::write_edn(&parsed).unwrap();
        let print_us = start.elapsed().as_micros();
        let start = Instant::now();
        let committed = transact(
            &fixture.peer_url,
            &endpoint,
            &format!("measure-{count}"),
            &text,
        );
        let commit_us = start.elapsed().as_micros();
        assert_eq!(field(&committed, "committed"), EdnValue::Bool(true));
        let start = Instant::now();
        let results = data(
            &fixture.peer_url,
            &["query", "--database", "edn", "--file", "-"],
            query,
        );
        let query_us = start.elapsed().as_micros();
        let entity = format!("[:person/email \"{count}-0@example.com\"]");
        let start = Instant::now();
        let pulled = data(
            &fixture.peer_url,
            &[
                "pull",
                "--database",
                "edn",
                "--file",
                "-",
                "--entity",
                &entity,
            ],
            "[:person/name :person/email]",
        );
        let pull_us = start.elapsed().as_micros();
        let EdnValue::Set(rows) = read_edn(&results).unwrap() else {
            panic!("relation shape lost")
        };
        println!(
            "EDN_COST entities={count} input_bytes={} formatted_bytes={} parse_us={parse_us} print_us={print_us} complete_transact_process_us={commit_us} complete_query_process_us={query_us} complete_pull_process_us={pull_us} query_rows={} query_result_bytes={} pull_result_bytes={}",
            text.len(),
            printed.len(),
            rows.len(),
            results.len(),
            pulled.len()
        );
    }
    server.stop();
}
