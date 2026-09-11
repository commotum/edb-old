#![cfg(unix)]

mod common;
use common::product_support::*;
use std::os::unix::fs::PermissionsExt;
use std::process::Command;

#[test]
fn data_only_cli_diagnostics_are_opt_in_and_preserve_readable_results() {
    use atomic_core::edn::{EdnValue, read_edn};
    let dir = tempfile::tempdir().unwrap();
    let query = dir.path().join("query.edn");
    let sources = dir.path().join("sources.edn");
    std::fs::write(&query, "[:find ?x :in $ :where [?x :item/value ?v]]").unwrap();
    std::fs::write(&sources, "{$ [[1 :item/value 2] [3 :item/value 4]]}").unwrap();
    let run = |flags: &[&str]| {
        let mut cmd = atomic();
        cmd.env_remove("ATOMIC_POSTGRES_URL")
            .args(["query", "--file"])
            .arg(&query)
            .arg("--sources")
            .arg(&sources)
            .args(flags);
        read_edn(&success(cmd.output().unwrap())).unwrap()
    };
    let plain = run(&[]);
    let EdnValue::Map(fields) = run(&["--query-stats", "--io-context", ":app/list-items"]) else {
        panic!("diagnostics require wrapper");
    };
    let field = |name| {
        fields
            .iter()
            .find(|(key, _)| *key == EdnValue::Keyword(atomic_core::Keyword::new("atomic", name)))
            .map(|(_, value)| value)
            .unwrap()
    };
    assert_eq!(field("ret"), &plain);
    assert!(matches!(field("query-stats"), EdnValue::Map(_)));
    assert!(matches!(field("io-stats"), EdnValue::Map(_)));
}

#[test]
fn product_configuration_is_explicit_and_redacts_connection_values() {
    const SECRET: &str = "product-private-password-marker";
    let parameters = format!("host=127.0.0.1 password={SECRET} invalid_setting={SECRET}");
    for transport in [None, Some("plaintext")] {
        let mut command = atomic();
        command
            .args(["status", "--database", "configuration-check"])
            .env("ATOMIC_POSTGRES_URL", &parameters);
        if let Some(transport) = transport {
            command.env("ATOMIC_POSTGRES_TRANSPORT", transport);
        } else {
            command.env_remove("ATOMIC_POSTGRES_TRANSPORT");
        }
        let output = command.output().unwrap();
        assert!(!output.status.success());
        let stderr = String::from_utf8_lossy(&output.stderr);
        assert!(!stderr.trim().is_empty());
        assert!(!stderr.contains(SECRET));
        assert!(!String::from_utf8_lossy(&output.stdout).contains(SECRET));
    }
    let mut command = atomic();
    configured(&mut command, &parameters);
    assert!(
        !command
            .args([
                "transactor",
                "--database",
                "configuration-check",
                "--unknown"
            ])
            .output()
            .unwrap()
            .status
            .success()
    );
}

#[test]
fn actual_product_commands_serve_a_separate_application_across_restart() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED real product acceptance: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let application = application_binary();
    let fixture = Fixture::new(&connection);
    if let Some((writer, peer)) = &fixture.roles {
        cli(
            &fixture.admin_url,
            &["migrate", "--writer-role", writer, "--peer-role", peer],
        );
    } else {
        cli(&fixture.admin_url, &["migrate"]);
        eprintln!("restricted-role witness unavailable: fixture account cannot create roles");
    }
    const DATABASE: &str = "application";
    cli(&fixture.admin_url, &["create", "--database", DATABASE]);
    cli(&fixture.admin_url, &["status", "--database", DATABASE]);
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("transactor.sock");
    let cache_directory = directory.path().join("peer-cache");
    std::fs::create_dir(&cache_directory).unwrap();
    std::fs::set_permissions(&cache_directory, std::fs::Permissions::from_mode(0o700)).unwrap();
    if fixture.roles.is_some() {
        let error = rejected_transactor(&fixture.peer_url, DATABASE, &endpoint);
        assert!(
            error.contains("ERROR"),
            "rejection omitted its error category"
        );
        assert_eq!(fixture.publication_count(DATABASE), 0);
        println!("PEER_RUNTIME_REJECTED no_ready=true no_publication=true");
    }
    for round in 0..2 {
        let mut command = atomic();
        configured(&mut command, &fixture.writer_url);
        command
            .args(["transactor", "--database", DATABASE, "--endpoint"])
            .arg(&endpoint)
            .args([
                "--telemetry-ms",
                "25",
                "--index-threshold-bytes",
                "1",
                "--hint-workers",
                "3",
                "--index-workers",
                "2",
            ]);
        let (mut server, events) = Server::spawn_observed(command);
        loop {
            let line = events
                .recv_timeout(std::time::Duration::from_secs(20))
                .unwrap();
            if line.starts_with("READY ") {
                break;
            }
        }
        let mut command = Command::new(&application);
        configured(&mut command, &fixture.peer_url);
        command
            .env("ATOMIC_SSD_CACHE_DIR", &cache_directory)
            .env("ATOMIC_SSD_CACHE_ENTRIES", "128")
            .env("ATOMIC_SSD_CACHE_BYTES", "16777216");
        let output = success(
            command
                .args(["--database", DATABASE, "--endpoint"])
                .arg(&endpoint)
                .output()
                .unwrap(),
        );
        assert!(output.contains("APPLICATION_OK"));
        assert!(output.contains("DIAGNOSTICS_OK named=true reads=true immutable_basis=true"));
        let events = events.try_iter().collect::<Vec<_>>();
        assert!(
            events
                .iter()
                .any(|line| line.contains("\"event\":\"atomic.transaction\"")),
            "missing transaction telemetry: {events:?}"
        );
        assert!(
            events
                .iter()
                .any(|line| line.contains("\"event\":\"atomic.metrics\"")),
            "missing service telemetry: {events:?}"
        );
        assert!(output.contains("ASYNC_READ_OK single_thread=true old_basis=true chunk_rows=1"));
        assert!(output.contains("COMPUTATION_OK weighted=true native_helpers=true old_basis=true"));
        assert!(output.contains("READ_VALUES_OK mixed_sources=true entity_identity=true"));
        assert!(output.contains(
            "QUERY_DATA_OK width=7 nested_values=true pure_callbacks=true old_basis=true"
        ));
        assert!(output.contains("PLANNING_OK discarded=true stale_rejected=true logical_ids_remapped=true exact_report=true exact_reference=true"));
        assert!(output.contains("PARTITIONS_OK named=true component_affinity=true uuid_roundtrip=true retry_exact=true reference_exact=true"));
        assert!(output.contains(
            "FULLTEXT_OK native=true structured_join=true portable_program=true retry_exact=true"
        ));
        assert!(output.contains(if round == 0 {
            "retry_exact=true replayed=false basis_t=11"
        } else {
            "retry_exact=true replayed=true basis_t=11"
        }));
        assert!(output.contains(if round == 0 {
            "reference_exact=true replayed=false basis_t=9"
        } else {
            "reference_exact=true replayed=true basis_t=9"
        }));
        assert!(output.contains("BASELINE"));
        assert!(output.contains("QUERY_SQL sql_calls=0 errors=0 result_cell_bytes=0"));
        assert!(output.contains("SSD_CACHE SsdCacheStats"));
        assert!(output.contains(if round == 0 {
            "seed_replayed=false"
        } else {
            "seed_replayed=true"
        }));
        assert!(output.contains(if round == 0 {
            "update_replayed=false"
        } else {
            "update_replayed=true"
        }));
        print!("round={round} {output}");
        server.stop();
        assert!(!endpoint.exists(), "normal shutdown retained the socket");
    }
    let committed_status = cli(&fixture.admin_url, &["status", "--database", DATABASE]);
    if fixture.can_inject_replica_fault {
        fixture.remove_derived_publication(DATABASE);
        let error = rejected_transactor(&fixture.writer_url, DATABASE, &endpoint);
        assert!(error.contains("service/native-index-required"));
        assert!(error.contains("atomic consolidate"));
        assert_eq!(fixture.publication_count(DATABASE), 0);
        assert_eq!(
            cli(&fixture.admin_url, &["status", "--database", DATABASE]),
            committed_status,
            "failed startup changed authoritative head state"
        );
        let indexed = cli(&fixture.admin_url, &["consolidate", "--database", DATABASE]);
        assert!(indexed.contains("INDEXED basis_t=11"));
        assert!(fixture.publication_count(DATABASE) > 0);
        let mut recovered = Server::start(&fixture.writer_url, DATABASE, &endpoint);
        recovered.stop();
        assert_eq!(
            cli(&fixture.admin_url, &["status", "--database", DATABASE]),
            committed_status,
            "derived recovery changed authoritative head state"
        );
        println!(
            "CLI_RECOVERY_OK diagnosed_missing_publication=true head_unchanged=true restarted=true"
        );
    } else {
        eprintln!(
            "missing-publication recovery witness unavailable: fixture account is not a superuser"
        );
    }
    println!(
        "PRODUCT_ACCEPTANCE_OK restricted_roles={} writer_restarts={} application_processes=2",
        fixture.roles.is_some(),
        1 + usize::from(fixture.can_inject_replica_fault)
    );
}
