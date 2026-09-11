#![cfg(unix)]
mod common;
use atomic_core::edn::{EdnValue, read_edn};
use common::product_support::*;
use std::io::Write;
use std::os::unix::fs::PermissionsExt;
use std::path::Path;
use std::process::{Command, Stdio};
use std::time::{Duration, Instant};

fn input(connection: &str, args: &[&str], edn: &str) -> EdnValue {
    let mut command = atomic();
    configured(&mut command, connection);
    command.args(args);
    command_input(command, edn)
}

fn command_input(mut command: Command, edn: &str) -> EdnValue {
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

fn field<'a>(value: &'a EdnValue, name: &str) -> &'a EdnValue {
    let EdnValue::Map(entries) = value else {
        panic!("expected report")
    };
    entries
        .iter()
        .find_map(|(key, value)| {
            matches!(key, EdnValue::Keyword(key) if key.name == name).then_some(value)
        })
        .expect("report field")
}

#[test]
fn stock_edn_client_reaches_compiled_rust_host_and_retries_across_restart() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual native host: ATOMIC_POSTGRES_URL unset");
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
    cli(
        &fixture.admin_url,
        &["create", "--database", "native-people"],
    );
    let binary = Path::new(env!("CARGO_BIN_EXE_atomic"))
        .parent()
        .unwrap()
        .join("examples/native_transaction_host");
    assert!(
        binary.is_file(),
        "build --example native_transaction_host before this test"
    );
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("native.sock");
    let endpoint = endpoint.to_str().unwrap();
    let mut original = None;
    let started = Instant::now();
    for round in 0..2 {
        let mut command = Command::new(&binary);
        configured(&mut command, &fixture.writer_url);
        command
            .args(["native-people", endpoint])
            .stdin(Stdio::piped());
        let mut host = Server::spawn(command);
        input(
            &fixture.peer_url,
            &[
                "transact",
                "--database",
                "native-people",
                "--endpoint",
                endpoint,
                "--request-key",
                "schema",
                "--file",
                "-",
            ],
            r#"[{:db/ident :person/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}]"#,
        );
        let report = input(
            &fixture.peer_url,
            &[
                "transact",
                "--database",
                "native-people",
                "--endpoint",
                endpoint,
                "--request-key",
                "person",
                "--file",
                "-",
            ],
            r#"[[demo.people.v1/add-person "person" "  ALICE   Smith  "]]"#,
        );
        assert_eq!(field(&report, "replayed"), &EdnValue::Bool(round != 0));
        if let Some(original) = &original {
            for name in [
                "basis-t",
                "tx-hash",
                "tx-data",
                "tempids",
                "db-before-t",
                "db-after-t",
            ] {
                assert_eq!(
                    field(&report, name),
                    field(original, name),
                    "retry changed {name}"
                );
            }
        } else {
            original = Some(report);
        }
        assert_eq!(
            input(
                &fixture.peer_url,
                &["query", "--database", "native-people", "--file", "-"],
                r#"[:find ?name ?label :where [_ :person/name ?name] [(str "Hello " ?name) ?label]]"#
            ),
            read_edn(r#"#{["alice smith" "Hello alice smith"]}"#).unwrap()
        );
        host.child.stdin.take().unwrap().write_all(b"\n").unwrap();
        let deadline = Instant::now() + Duration::from_secs(20);
        loop {
            if let Some(status) = host.child.try_wait().unwrap() {
                assert!(status.success());
                break;
            }
            assert!(Instant::now() < deadline, "native host shutdown timed out");
            std::thread::sleep(Duration::from_millis(10));
        }
    }
    println!(
        "NATIVE_HOST_OK processes=2 edn=true exact_retry=true restricted_roles={} complete_ms={}",
        fixture.roles.is_some(),
        started.elapsed().as_millis()
    );
}

#[test]
fn stock_cli_data_helpers_need_no_database() {
    let result = input(
        "host=invalid",
        &["query", "--file", "-"],
        r#"[:find ?n ?middle ?quot :in :where [(count "a🦀界") ?n] [(subs "a🦀界" 1 2) ?middle] [(quot 125000 60000) ?quot]]"#,
    );
    assert_eq!(result, read_edn(r#"#{[3 "🦀" 2]}"#).unwrap());
}
