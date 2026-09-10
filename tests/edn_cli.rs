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
fn data_only_query_accepts_general_sources_without_database_configuration() {
    let mut sources = tempfile::NamedTempFile::new().unwrap();
    sources
        .write_all(
            br#"{$rows [["Alice" nil \A #app/state :ready {:priority 1} #{:blue :green} 7]]}"#,
        )
        .unwrap();
    let mut command = atomic();
    // An intentionally unusable setting proves data-only execution does not
    // even validate PostgreSQL configuration, let alone establish a connection.
    command.env("ATOMIC_POSTGRES_URL", "not-a-postgresql-connection");
    let mut child = command
        .args(["query", "--file", "-", "--sources"])
        .arg(sources.path())
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
        .unwrap();
    child.stdin.take().unwrap().write_all(br#"[:find ?name ?map ?tag ?set :in $rows :where [$rows ?name nil \A ?tag ?map ?set 7]]"#).unwrap();
    let output = success(child.wait_with_output().unwrap());
    assert_eq!(
        read_edn(&output).unwrap(),
        read_edn(r#"#{["Alice" {:priority 1} #app/state :ready #{:green :blue}]}"#).unwrap()
    );
}

#[test]
fn configured_partition_cli_preview_commit_and_changed_default_exact_retry() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED default partition CLI: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let started = Instant::now();
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
    transact(
        &fixture.peer_url,
        &endpoint,
        "schema",
        include_str!("../examples/edn/schema.edn"),
    );
    transact(
        &fixture.peer_url,
        &endpoint,
        "partition",
        r#"[{:db/ident :app.part/people :db.install/_partition :db.part/db}]"#,
    );
    let named = data(
        &fixture.peer_url,
        &["query", "--database", "edn", "--file", "-"],
        "[:find ?e :where [?e :db/ident :app.part/people]]",
    );
    let EdnValue::Set(rows) = read_edn(&named).unwrap() else {
        panic!("relation")
    };
    let EdnValue::Vector(row) = &rows[0] else {
        panic!("row")
    };
    let atomic_core::Value::Ref(partition) = atomic_core::edn_value::edn_to_value(&row[0]).unwrap()
    else {
        panic!("partition id")
    };
    server.stop();
    let mut command = atomic();
    configured(&mut command, &fixture.writer_url);
    command
        .args([
            "transactor",
            "--database",
            "edn",
            "--default-partition",
            ":app.part/people",
            "--endpoint",
        ])
        .arg(&endpoint);
    let mut server = Server::spawn(command);
    let intent = r#"[{:db/id "default" :person/name "Configured" :person/email "configured@example.com"}
        {:db/id "forced" :person/name "Forced"}
        {:db/force-partition {"forced" :db.part/user}}]"#;
    let preview = data(
        &fixture.peer_url,
        &[
            "with",
            "--database",
            "edn",
            "--file",
            "-",
            "--default-partition",
            ":app.part/people",
        ],
        intent,
    );
    assert_eq!(field(&preview, "committed"), EdnValue::Bool(false));
    let committed = transact(&fixture.peer_url, &endpoint, "placed", intent);
    let check_placement = |report: &str| {
        let EdnValue::Map(ids) = field(report, "tempids") else {
            panic!("tempids")
        };
        for (key, bits) in [
            ("default", partition as u32),
            ("forced", atomic_core::USER_PARTITION),
        ] {
            let (_, EdnValue::Long(id)) = ids
                .iter()
                .find(|(k, _)| *k == EdnValue::String(key.into()))
                .unwrap()
            else {
                panic!("id")
            };
            assert_eq!(atomic_core::eid_to_part(*id as u64).unwrap(), bits);
        }
    };
    check_placement(&preview);
    check_placement(&committed);
    server.stop();
    // Even an unresolved new default cannot obstruct the original saved receipt.
    let mut command = atomic();
    configured(&mut command, &fixture.writer_url);
    command
        .args([
            "transactor",
            "--database",
            "edn",
            "--default-partition",
            ":missing/partition",
            "--endpoint",
        ])
        .arg(&endpoint);
    let mut server = Server::spawn(command);
    let replay = transact(&fixture.peer_url, &endpoint, "placed", intent);
    assert_eq!(field(&replay, "replayed"), EdnValue::Bool(true));
    for name in [
        "basis-t",
        "tx-hash",
        "tempids",
        "tx-data",
        "db-before-t",
        "db-after-t",
    ] {
        assert_eq!(field(&committed, name), field(&replay, name), "{name}");
    }
    server.stop();
    // Default placement is writer configuration, never a caller override on transact.
    let mut command = atomic();
    command.env_remove("ATOMIC_POSTGRES_URL").args([
        "transact",
        "--database",
        "edn",
        "--file",
        "-",
        "--endpoint",
        "/none",
        "--request-key",
        "none",
        "--default-partition",
        ":app.part/people",
    ]);
    let rejected = command.output().unwrap();
    assert!(!rejected.status.success());
    assert!(String::from_utf8_lossy(&rejected.stderr).contains("cli/edn-usage"));
    println!(
        "DEFAULT_PARTITION_CLI_OK preview=true commit=true explicit_override=true changed_config_exact_retry=true restricted_roles={} complete_elapsed_us={}",
        fixture.roles.is_some(),
        started.elapsed().as_micros()
    );
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
    let schema = include_str!("../examples/edn/schema.edn");
    assert_eq!(
        field(
            &transact(&fixture.peer_url, &endpoint, "schema", schema),
            "committed"
        ),
        EdnValue::Bool(true)
    );
    let alice = include_str!("../examples/edn/people.edn");
    let first = transact(&fixture.peer_url, &endpoint, "alice", alice);
    assert_eq!(field(&first, "replayed"), EdnValue::Bool(false));
    let query = include_str!("../examples/edn/names-query.edn");
    let result = data(
        &fixture.peer_url,
        &["query", "--database", "edn", "--file", "-"],
        query,
    );
    assert!(result.contains("Alice") && result.contains("Bob"));
    let pull = include_str!("../examples/edn/person-pull.edn");
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
    // Multiple immutable bases and raw rows can be authored in a sources file.
    let EdnValue::Long(first_t) = field(&first, "basis-t") else {
        panic!("fixture basis must fit a Long")
    };
    let mut sources_file = tempfile::NamedTempFile::new().unwrap();
    write!(sources_file, "{{$then {{:database \"edn\" :as-of {first_t}}} $now {{:database \"edn\"}} $extra [[\"alice@example.com\" \"external\"]]}}").unwrap();
    let compared = data(
        &fixture.peer_url,
        &[
            "query",
            "--database",
            "edn",
            "--file",
            "-",
            "--sources",
            sources_file.path().to_str().unwrap(),
        ],
        "[:find ?before ?after ?extra :in $then $now $extra :where [$then ?e :person/name ?before] [$now ?e :person/name ?after] [$now ?e :person/email ?email] [$extra ?email ?extra]]",
    );
    assert_eq!(
        read_edn(&compared).unwrap(),
        read_edn(r#"#{["Alice" "Alicia" "external"]}"#).unwrap()
    );
    let mut general_sources = tempfile::NamedTempFile::new().unwrap();
    general_sources.write_all(br#"{$now {:database "edn"} $extra [["alice@example.com" nil \A #app/flag :good {:context "external"} #{:blue :green} 7]]}"#).unwrap();
    let general = data(
        &fixture.peer_url,
        &[
            "query",
            "--file",
            "-",
            "--sources",
            general_sources.path().to_str().unwrap(),
        ],
        "[:find ?name ?context ?tag ?labels :in $now $extra :where [$now ?e :person/name ?name] [$now ?e :person/email ?email] [$extra ?email nil _ ?tag ?context ?labels 7]]",
    );
    assert_eq!(
        read_edn(&general).unwrap(),
        read_edn(r#"#{["Alicia" {:context "external"} #app/flag :good #{:blue :green}]}"#).unwrap()
    );
    let mut rules_file = tempfile::NamedTempFile::new().unwrap();
    rules_file
        .write_all(b"[[[(named ?e ?name) [?e :person/name ?name]]]]")
        .unwrap();
    let return_maps = data(
        &fixture.peer_url,
        &[
            "query",
            "--database",
            "edn",
            "--file",
            "-",
            "--inputs",
            rules_file.path().to_str().unwrap(),
        ],
        "[:find ?name :keys name :in $ % :where (named ?e ?name)]",
    );
    assert!(
        return_maps.contains(":name")
            && return_maps.contains("Alicia")
            && return_maps.contains("Bob")
    );
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
        "EDN_APPLICATION_OK schema=true maps=true nested=true lookup=true omitted_preserved=true preview_isolated=true history=true multiple_sources=true general_relations=true rules=true return_maps=true independent_reads=true restart_exact_retry=true restricted_roles={}",
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
        let prepared = atomic_core::TransactionRequest::from_edn("cost-only", &text).unwrap();
        let prepare_us = start.elapsed().as_micros();
        drop(prepared);
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
            "EDN_COST entities={count} input_bytes={} formatted_bytes={} parse_us={parse_us} print_us={print_us} parse_and_canonical_request_us={prepare_us} complete_transact_process_us={commit_us} complete_query_process_us={query_us} complete_pull_process_us={pull_us} query_rows={} query_result_bytes={} pull_result_bytes={}",
            text.len(),
            printed.len(),
            rows.len(),
            results.len(),
            pulled.len()
        );
    }
    server.stop();
}
