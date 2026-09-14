#![cfg(unix)]

mod common;
use atomic_core::*;
use common::product_support::{Fixture, Server, atomic, cli, configured};
use std::collections::BTreeSet;
use std::os::unix::fs::PermissionsExt;
use std::path::Path;
use std::time::{Duration, Instant};

const LABEL: u32 = 1_000;
const COMPONENT: u32 = 1_001;
const LINK: u32 = 1_002;
const WAIT: Duration = Duration::from_secs(60);

fn writer(connection: &str, database: &str, endpoint: &Path) -> Server {
    let mut command = atomic();
    configured(&mut command, connection);
    command
        .args([
            "transactor",
            "--index-threshold-bytes",
            "16777216",
            "--database",
            database,
            "--endpoint",
        ])
        .arg(endpoint);
    Server::spawn(command)
}

fn add(entity: EntityRef, attribute: u32, value: TxValue) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value,
    }
}

fn sorted(mut datoms: Vec<Datom>) -> Vec<Datom> {
    datoms.sort_by(|a, b| a.cmp_in(b, IndexOrder::Eavt));
    datoms
}

#[test]
fn deep_retraction_and_shared_recent_tail_survive_cli_consolidation_and_restart() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED stack-safety PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = Fixture::new(&connection);
    if let Some((writer, peer)) = &fixture.roles {
        cli(
            &fixture.admin_url,
            &["install", "--writer-role", writer, "--peer-role", peer],
        );
    } else {
        cli(&fixture.admin_url, &["install"]);
    }
    const DATABASE: &str = "stack_safety";
    cli(&fixture.admin_url, &["create", "--database", DATABASE]);
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("transactor.sock");
    let mut server = writer(&fixture.writer_url, DATABASE, &endpoint);
    let peer = Connection::connect(&fixture.peer_url, DATABASE, 256).unwrap();
    peer.transact_socket(
        &endpoint,
        TransactionRequest::new(
            "schema",
            vec![
                TxOp::InstallAttribute(Attribute::new(
                    LABEL,
                    Keyword::new("graph", "label"),
                    ValueType::Long,
                    Cardinality::One,
                )),
                TxOp::InstallAttribute(
                    Attribute::new(
                        COMPONENT,
                        Keyword::new("graph", "component"),
                        ValueType::Ref,
                        Cardinality::Many,
                    )
                    .component(),
                ),
                TxOp::InstallAttribute(Attribute::new(
                    LINK,
                    Keyword::new("graph", "link"),
                    ValueType::Ref,
                    Cardinality::Many,
                )),
            ],
        ),
        WAIT,
    )
    .unwrap()
    .report
    .unwrap();
    let mut final_retraction = None;
    let mut survivor = 0;
    for depth in [128, 2_048] {
        let node = |i| EntityRef::Temp(format!("node-{i}"));
        let outside = EntityRef::Temp("outside".into());
        let referenced = EntityRef::Temp("referenced".into());
        // Flat authoring: stored depth is independent of authoring-form depth.
        let mut ops = Vec::new();
        for i in 0..depth {
            ops.push(add(node(i), LABEL, Value::Long(i as i64).into()));
            if i + 1 < depth {
                ops.push(add(node(i), COMPONENT, TxValue::Entity(node(i + 1))));
            }
        }
        ops.extend([
            add(node(depth - 1), COMPONENT, TxValue::Entity(node(0))), // cycle
            add(node(0), COMPONENT, TxValue::Entity(node(depth / 2))), // shared child
            add(outside.clone(), LABEL, Value::Long(-1).into()),
            add(referenced.clone(), LABEL, Value::Long(-2).into()),
            add(outside, LINK, TxValue::Entity(node(depth / 2))), // incoming ref
            add(node(0), LINK, TxValue::Entity(referenced)),      // not a component
        ]);
        let started = Instant::now();
        let seed = peer
            .transact_socket(
                &endpoint,
                TransactionRequest::new(format!("graph-{depth}"), ops),
                WAIT,
            )
            .unwrap()
            .report
            .unwrap();
        let seed_ms = started.elapsed().as_millis();
        let closure: BTreeSet<_> = (0..depth)
            .map(|i| seed.tempids[&format!("node-{i}")])
            .collect();
        let before = seed.db_after.datoms(IndexOrder::Eavt).unwrap();
        let historical = seed
            .db_after
            .clone()
            .history()
            .datoms(IndexOrder::Eavt)
            .unwrap();
        survivor = seed.tempids["outside"];
        let request = TransactionRequest::new(
            format!("retract-{depth}"),
            vec![TxOp::RetractEntity(EntityRef::Id(seed.tempids["node-0"]))],
        );
        let started = Instant::now();
        let committed = peer
            .transact_socket(&endpoint, request.clone(), WAIT)
            .unwrap();
        let hash = committed.tx_hash;
        let report = committed.report.unwrap();
        let retract_ms = started.elapsed().as_millis(); // includes receipt/value opening
        let in_closure = |d: &&Datom| {
            closure.contains(&d.entity)
                || matches!(&d.value, Value::Ref(id) if closure.contains(id))
        };
        let expected = before
            .iter()
            .filter(in_closure)
            .cloned()
            .map(|mut d| {
                d.tx = t_to_tx(report.basis_t).unwrap();
                d.added = false;
                d
            })
            .collect();
        let actual = report
            .tx_data
            .iter()
            .filter(|d| !d.added)
            .cloned()
            .collect();
        assert_eq!(
            sorted(actual),
            sorted(expected),
            "exact documented retraction closure"
        );
        assert!(
            !report
                .db_after
                .datoms(IndexOrder::Eavt)
                .unwrap()
                .iter()
                .any(|d| in_closure(&d))
        );
        assert_eq!(
            report.db_after.values(survivor, LABEL).unwrap(),
            vec![Value::Long(-1)]
        );
        assert_eq!(
            report
                .db_after
                .values(seed.tempids["referenced"], LABEL)
                .unwrap(),
            vec![Value::Long(-2)]
        );
        assert_eq!(seed.db_after.datoms(IndexOrder::Eavt).unwrap(), before);
        assert_eq!(
            seed.db_after
                .clone()
                .history()
                .datoms(IndexOrder::Eavt)
                .unwrap(),
            historical
        );
        eprintln!(
            "COMPONENT_PG depth={depth} closure_entities={} retracted_datoms={} seed_ms={seed_ms} retract_and_report_ms={retract_ms}",
            closure.len(),
            report.tx_data.iter().filter(|d| !d.added).count()
        );
        final_retraction = Some((request, hash, report, before));
    }
    let (request, hash, report, original_before) = final_retraction.unwrap();
    // Establish a known base before measuring chunk growth. Mandatory schema
    // indexing from setup must not race this deliberately unindexed tail.
    server.stop();
    cli(&fixture.admin_url, &["consolidate", "--database", DATABASE]);
    peer.sync_index(report.basis_t, WAIT).unwrap();
    assert_eq!(peer.recent_stats().transactions, 0);
    server = writer(&fixture.writer_url, DATABASE, &endpoint);
    let mut retained = None;
    let started = Instant::now();
    for i in 0..65 {
        peer.transact_socket(
            &endpoint,
            TransactionRequest::new(
                format!("tail-{i}"),
                vec![add(EntityRef::Id(survivor), LABEL, Value::Long(i).into())],
            ),
            WAIT,
        )
        .unwrap()
        .report
        .unwrap(); // release ordinary per-transaction reports
        if i == 31 {
            retained = Some(peer.sync().unwrap());
        }
    }
    let recent = peer.sync().unwrap();
    let stats = peer.recent_stats();
    assert!(
        stats.transactions >= 65 && stats.log_chunks >= 3,
        "fixture did not exercise predecessor sharing: {stats:?}"
    );
    let retained = retained.unwrap();
    assert_eq!(
        retained.values(survivor, LABEL).unwrap(),
        vec![Value::Long(31)]
    );
    assert_eq!(
        recent.values(survivor, LABEL).unwrap(),
        vec![Value::Long(64)]
    );
    eprintln!(
        "RECENT_PG append65_and_reports_ms={} transactions={} chunks={}",
        started.elapsed().as_millis(),
        stats.transactions,
        stats.log_chunks
    );
    let recent_history = recent.clone().history().datoms(IndexOrder::Eavt).unwrap();
    let started = Instant::now();
    server.stop(); // releases writer-side recent ownership
    cli(&fixture.admin_url, &["consolidate", "--database", DATABASE]);
    let indexed = peer.sync_index(recent.basis_t(), WAIT).unwrap();
    assert_eq!(peer.recent_stats().transactions, 0);
    assert_eq!(
        indexed.clone().history().datoms(IndexOrder::Eavt).unwrap(),
        recent_history
    );
    assert_eq!(
        retained.values(survivor, LABEL).unwrap(),
        vec![Value::Long(31)]
    );
    drop(recent);
    eprintln!(
        "RECENT_PG writer_stop_consolidate_adopt_drop_ms={} retained_basis={}",
        started.elapsed().as_millis(),
        retained.basis_t()
    );
    let mut replacement = writer(&fixture.writer_url, DATABASE, &endpoint);
    let reopened = Connection::connect(&fixture.peer_url, DATABASE, 256).unwrap();
    let replay = reopened.transact_socket(&endpoint, request, WAIT).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, hash);
    let replay = replay.report.unwrap();
    assert_eq!(replay.tx_data, report.tx_data);
    assert_eq!(replay.tempids, report.tempids);
    assert_eq!(
        replay.db_before.datoms(IndexOrder::Eavt).unwrap(),
        original_before
    );
    assert_eq!(
        replay
            .db_after
            .clone()
            .history()
            .datoms(IndexOrder::Eavt)
            .unwrap(),
        report
            .db_after
            .clone()
            .history()
            .datoms(IndexOrder::Eavt)
            .unwrap()
    );
    replacement.stop();
    drop(reopened);
    drop(peer);
    assert_eq!(
        retained.values(survivor, LABEL).unwrap(),
        vec![Value::Long(31)]
    );
    drop(retained);
    eprintln!(
        "STACK_SAFETY_PG_OK closure=true retained_history=true shared_recent=true consolidation=true restart_retry=exact"
    );
}
