#![cfg(unix)]

mod common;

use atomic_core::sql_io::{OperationContext, OperationKind};
use atomic_core::*;
use common::product_support::{Fixture, Server, cli};
use postgres::{Client, NoTls};
use std::os::unix::fs::PermissionsExt;
use std::sync::Arc;
use std::sync::atomic::{AtomicBool, AtomicUsize, Ordering};
use std::time::{Duration, Instant};

const VALUES: u32 = 1_000;
const PARENT: u32 = 1_001;
const SCORE: u32 = 1_002;
const UNINDEXED: u32 = 1_003;
const NAME: u32 = 1_004;
const TIES: u32 = 1_005;
const WAIT: Duration = Duration::from_secs(60);

fn add(entity: EntityRef, attribute: u32, value: TxValue) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value,
    }
}

fn range_query(attribute: u32) -> Query {
    Query::new(
        FindSpec::Collection(FindElement::Variable("value".into())),
        vec![
            Clause::Pattern(Box::new(DataPattern::new(
                Term::var("entity"),
                Term::Constant(Value::Ref(u64::from(attribute))),
                Term::var("value"),
            ))),
            Clause::Predicate {
                predicate: Predicate::LessOrEqual,
                source: "$".into(),
                args: vec![Term::Constant(Value::Long(5)), Term::var("value")],
            },
            Clause::Predicate {
                predicate: Predicate::Less,
                source: "$".into(),
                args: vec![
                    Term::var("value"),
                    Term::Constant(Value::BigDec("9.0".parse().unwrap())),
                ],
            },
        ],
    )
}

fn field<'a>(value: &'a QueryValue, name: &str) -> &'a QueryValue {
    let QueryValue::Map(fields) = value else {
        panic!("expected map: {value:?}")
    };
    let key = QueryValue::Scalar(Value::Keyword(Keyword::new("reads", name)));
    &fields
        .iter()
        .find(|(candidate, _)| candidate == &key)
        .expect("missing field")
        .1
}

fn canonical_collection(mut result: QueryResult) -> QueryResult {
    // Collection results are unordered. Compare membership without turning a
    // different valid index traversal order into a false semantic regression.
    let QueryResult::Collection(values) = &mut result else {
        panic!("collection expected")
    };
    values.sort_by(QueryValue::canonical_cmp);
    result
}

struct ReadCost {
    visits: u64,
    work: Option<u64>,
    driver_calls: u64,
    elapsed: Duration,
}

fn print_cost(
    label: &str,
    size: usize,
    warm: bool,
    before: PeerLoadStats,
    after: PeerLoadStats,
    cost: ReadCost,
) {
    let ReadCost {
        visits,
        work,
        driver_calls,
        elapsed,
    } = cost;
    let work = work.map_or_else(|| "n/a".to_owned(), |work| work.to_string());
    eprintln!(
        "PEER_READ_PG operation={label} size={size} warm={warm} visited={visits} work={work} driver_calls={driver_calls} ranges={} cursor_sql_reads={} cursor_sql_bytes={} root_reads={} directories={} leaves={} complete_check_drop_us={}",
        after.cursor_ranges - before.cursor_ranges,
        after.cursor_sql_reads - before.cursor_sql_reads,
        after.cursor_sql_read_bytes - before.cursor_sql_read_bytes,
        after.cursor_root_reads - before.cursor_root_reads,
        after.cursor_directory_reads - before.cursor_directory_reads,
        after.cursor_leaf_reads - before.cursor_leaf_reads,
        elapsed.as_micros(),
    );
    assert_eq!(
        after.compatibility_materializations,
        before.compatibility_materializations
    );
}

fn check_reads(url: &str, database: &str, size: usize, root: u64) {
    let mut forward = PullAttribute::forward(AttributeName::Id(VALUES));
    forward.limit = PullLimit::Limit(1);
    let mut reverse = PullAttribute::reverse(AttributeName::Id(PARENT));
    reverse.limit = PullLimit::Limit(1);
    let mut nested = reverse.clone();
    nested.nested = Some(PullNested::Pattern(Box::new(PullPattern::attributes(
        vec![PullAttribute::forward(AttributeName::Id(SCORE))],
    ))));
    for (label, pattern, name) in [
        (
            "forward",
            PullPattern::attributes(vec![forward.clone()]),
            "values",
        ),
        ("reverse", PullPattern::attributes(vec![reverse]), "_parent"),
        ("nested", PullPattern::attributes(vec![nested]), "_parent"),
        (
            "wildcard",
            PullPattern {
                wildcard: true,
                attributes: vec![forward],
            },
            "values",
        ),
    ] {
        // New peer = cold application working set. Bootstrap metadata I/O is
        // outside the operation and may incidentally cache colocated data; the
        // second identical operation reuses the resulting decoded-node cache.
        let peer = Connection::connect(url, database, 128).unwrap();
        for warm in [false, true] {
            let visited = Arc::new(AtomicUsize::new(0));
            let observed = Arc::clone(&visited);
            // Diagnostic always-true filter counts candidates in the actual
            // supplied-view path. It never changes the visible relation.
            let db = peer.db().filter(move |_, datom| {
                if datom.attribute >= VALUES {
                    observed.fetch_add(1, Ordering::Relaxed);
                }
                true
            });
            let before = peer.load_stats();
            let started = Instant::now();
            let io = OperationContext::new(OperationKind::Query);
            let scope = io.enter();
            let result = db.pull(&pattern, root).unwrap();
            let QueryValue::Collection(values) = field(&result, name) else {
                panic!("collection expected")
            };
            assert_eq!(values.len(), 1);
            if name == "values" {
                assert_eq!(values[0], QueryValue::Scalar(Value::Long(0)));
            }
            if label == "nested" {
                assert_eq!(
                    field(&values[0], "score"),
                    &QueryValue::Scalar(Value::Long(0))
                );
            }
            drop(result);
            drop(db);
            drop(scope);
            let sql = io.snapshot();
            let elapsed = started.elapsed();
            let count = visited.load(Ordering::Relaxed) as u64;
            print_cost(
                label,
                size,
                warm,
                before,
                peer.load_stats(),
                ReadCost {
                    visits: count,
                    work: None,
                    driver_calls: sql.calls,
                    elapsed,
                },
            );
            assert!(
                count <= 32,
                "{label} limit1 walked {count} candidates at fanout{size}"
            );
        }
    }
    let mut strict = range_query(TIES);
    strict.clauses[1] = Clause::Predicate {
        predicate: Predicate::Greater,
        source: "$".into(),
        args: vec![Term::var("value"), Term::Constant(Value::Long(5))],
    };
    let mut unequal = range_query(TIES);
    unequal.clauses[1] = Clause::Predicate {
        predicate: Predicate::NotEq,
        source: "$".into(),
        args: vec![
            Term::var("value"),
            Term::Constant(Value::BigDec("5.00".parse().unwrap())),
        ],
    };
    for (attribute, label, query, expected_range) in [
        (SCORE, "indexed-range", range_query(SCORE), 5..9),
        (UNINDEXED, "fallback-range", range_query(UNINDEXED), 5..9),
        (TIES, "strict-tie-range", strict, 6..9),
        (TIES, "unequal-tie-range", unequal, 6..9),
    ] {
        let peer = Connection::connect(url, database, 128).unwrap();
        let db = peer.db();
        let expected = QueryResult::Collection(
            expected_range
                .map(|n| QueryValue::Scalar(Value::Long(n)))
                .collect(),
        );
        let prepared = PreparedQuery::new(&query).unwrap();
        for warm in [false, true] {
            let before = peer.load_stats();
            let started = Instant::now();
            let io = OperationContext::new(OperationKind::Query);
            let scope = io.enter();
            let result = prepared
                .execute(
                    &[QueryDataSource::database("$", db.clone())],
                    &[],
                    &QueryControl::default(),
                )
                .unwrap();
            assert_eq!(canonical_collection(result.result.clone()), expected);
            let visits = result.stats.datoms_examined;
            let work = result.stats.work;
            drop(result);
            drop(scope);
            let sql = io.snapshot();
            let elapsed = started.elapsed();
            print_cost(
                label,
                size,
                warm,
                before,
                peer.load_stats(),
                ReadCost {
                    visits,
                    work: Some(work),
                    driver_calls: sql.calls,
                    elapsed,
                },
            );
            if attribute != UNINDEXED {
                assert!(visits <= 8, "indexed range enumerated {visits}/{size}");
            } else {
                assert!(
                    visits >= size as u64,
                    "unindexed fallback must remain accounted"
                );
            }
        }
        // Independent scan outside the timed/cold operation.
        let scanned = db
            .query(
                &query,
                &[],
                &QueryControl {
                    force_scan: true,
                    ..QueryControl::default()
                },
            )
            .unwrap();
        assert_eq!(canonical_collection(scanned.result), expected);
    }
}

#[test]
fn bounded_pull_and_indexed_ranges_survive_cli_publication_views_and_restart() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED peer read PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
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
    for size in [128_usize, 1_024, 4_096] {
        let database = format!("peer_reads_{size}");
        cli(&fixture.admin_url, &["create", "--database", &database]);
        let directory = tempfile::tempdir().unwrap();
        std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
        let endpoint = directory.path().join("writer.sock");
        let mut server = Server::start(&fixture.writer_url, &database, &endpoint);
        let peer = Connection::connect(&fixture.peer_url, &database, 128).unwrap();
        let schema = [
            (VALUES, "values", ValueType::Long, Cardinality::Many, false),
            (PARENT, "parent", ValueType::Ref, Cardinality::One, false),
            (SCORE, "score", ValueType::Long, Cardinality::One, true),
            (
                UNINDEXED,
                "unindexed",
                ValueType::Long,
                Cardinality::One,
                false,
            ),
            (NAME, "name", ValueType::String, Cardinality::One, false),
            (TIES, "ties", ValueType::Long, Cardinality::One, true),
        ]
        .into_iter()
        .map(|(id, name, value_type, cardinality, indexed)| {
            let mut attribute =
                Attribute::new(id, Keyword::new("reads", name), value_type, cardinality);
            attribute.indexed = indexed;
            TxOp::InstallAttribute(attribute)
        })
        .collect();
        peer.transact_socket(&endpoint, TransactionRequest::new("schema", schema), WAIT)
            .unwrap()
            .report
            .unwrap();
        let mut ops = vec![add(
            EntityRef::Temp("root".into()),
            NAME,
            Value::String("root".into()).into(),
        )];
        for n in 0..size {
            let child = EntityRef::Temp(format!("child-{n:05}"));
            ops.extend([
                add(
                    EntityRef::Temp("root".into()),
                    VALUES,
                    Value::Long(n as i64).into(),
                ),
                add(
                    child.clone(),
                    PARENT,
                    TxValue::Entity(EntityRef::Temp("root".into())),
                ),
                add(child.clone(), SCORE, Value::Long(n as i64).into()),
                add(child.clone(), UNINDEXED, Value::Long(n as i64).into()),
                add(
                    child,
                    TIES,
                    Value::Long(if n < size - 4 {
                        5
                    } else {
                        6 + (n - (size - 4)) as i64
                    })
                    .into(),
                ),
            ]);
        }
        let request = TransactionRequest::new("seed", ops);
        let started = Instant::now();
        let committed = match peer.transact_socket(&endpoint, request.clone(), WAIT) {
            Ok(committed) => committed,
            Err(error) if error.category == ErrorCategory::UnknownOutcome => {
                // The server's ordinary 30s response deadline is not a rollback
                // promise. Resolve ambiguity with exactly the same request key
                // and bytes; never create a replacement transaction identity.
                eprintln!(
                    "PEER_READ_PG_SETUP size={size} unknown_outcome_ms={} code={} retrying_exact_seed",
                    started.elapsed().as_millis(),
                    error.code
                );
                let committed = peer
                    .transact_socket(&endpoint, request.clone(), WAIT)
                    .unwrap();
                assert!(
                    committed.replayed,
                    "this witness expects the lost response's committed receipt"
                );
                eprintln!(
                    "PEER_READ_PG_SETUP size={size} recovered_exact_receipt_ms={}",
                    started.elapsed().as_millis()
                );
                committed
            }
            Err(error) => panic!("seed failed: {error:?}"),
        };
        let seeded = committed.report.unwrap();
        let root = seeded.tempids["root"];
        let changed = seeded.tempids["child-00005"];
        peer.sync_index(seeded.basis_t, WAIT).unwrap();
        eprintln!(
            "PEER_READ_PG_SETUP size={size} seed_and_publication_ms={} (excluded from read costs)",
            started.elapsed().as_millis()
        );
        check_reads(&fixture.peer_url, &database, size, root);

        // Prevent backfill publication while observing a logically indexed but
        // physically incomplete attribute. This is this fixture's existing
        // build-pin lock, not a production delay or altered index policy.
        let mut blocker = Client::connect(&fixture.admin_url, NoTls).unwrap();
        let build_key: i64 = blocker
            .query_one(
                "SELECT atomic_tree_database_build_pin_key($1)",
                &[&database],
            )
            .unwrap()
            .get(0);
        blocker
            .query_one("SELECT pg_advisory_lock($1)", &[&build_key])
            .unwrap();
        let mut indexed = peer.db().schema().attribute(UNINDEXED).unwrap().clone();
        indexed.indexed = true;
        let enabled = peer
            .transact_socket(
                &endpoint,
                TransactionRequest::new("enable-index", vec![TxOp::AlterAttribute(indexed)]),
                WAIT,
            )
            .unwrap()
            .report
            .unwrap();
        let pending_peer = Connection::connect(&fixture.peer_url, &database, 128).unwrap();
        let pending = pending_peer.db();
        assert!(pending.schema().attribute(UNINDEXED).unwrap().indexed);
        let query = range_query(UNINDEXED);
        let fallback = pending
            .query(&query, &[], &QueryControl::default())
            .unwrap();
        assert!(fallback.plan.iter().any(|step| step.access == "AEVT seek"));
        assert!(fallback.stats.datoms_examined >= size as u64);
        assert_eq!(
            canonical_collection(fallback.result),
            QueryResult::Collection((5..9).map(|n| QueryValue::Scalar(Value::Long(n))).collect())
        );
        blocker
            .query_one("SELECT pg_advisory_unlock($1)", &[&build_key])
            .unwrap();
        drop(blocker);
        peer.sync_index(enabled.basis_t, WAIT).unwrap();
        peer.sync_schema(enabled.basis_t, WAIT).unwrap();
        let ready = peer
            .db()
            .query(&query, &[], &QueryControl::default())
            .unwrap();
        assert!(ready.plan.iter().any(|step| step.access == "AVET range"));
        assert!(ready.stats.datoms_examined <= 8);
        let still_pending = pending
            .query(&query, &[], &QueryControl::default())
            .unwrap();
        assert!(still_pending.stats.datoms_examined >= size as u64);
        assert_eq!(
            canonical_collection(ready.result),
            canonical_collection(still_pending.result)
        );

        // Cancellation must remain responsive while the supplied view rejects
        // every candidate, not only when a visible result reaches Pull.
        let cancel = Arc::new(AtomicBool::new(false));
        let canceled = Arc::clone(&cancel);
        let rejected = Arc::new(AtomicUsize::new(0));
        let counted = Arc::clone(&rejected);
        let invisible = peer.db().filter(move |_, datom| {
            if datom.attribute == VALUES {
                if counted.fetch_add(1, Ordering::Relaxed) == 15 {
                    canceled.store(true, Ordering::Relaxed);
                }
                return false;
            }
            true
        });
        let error = invisible
            .pull_with_control(
                &PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(VALUES))]),
                root,
                &PullControl {
                    cancel,
                    ..PullControl::default()
                },
            )
            .unwrap_err();
        assert_eq!(error.code, "pull/canceled");
        assert!(rejected.load(Ordering::Relaxed) <= 16);

        let retained = peer.db();
        let updated = peer
            .transact_socket(
                &endpoint,
                TransactionRequest::new(
                    "change",
                    vec![
                        add(EntityRef::Id(changed), SCORE, Value::Long(6).into()),
                        TxOp::Retract {
                            entity: EntityRef::Id(root),
                            attribute: VALUES,
                            value: Some(Value::Long(0).into()),
                        },
                    ],
                ),
                WAIT,
            )
            .unwrap()
            .report
            .unwrap();
        peer.sync_index(updated.basis_t, WAIT).unwrap();
        let latest = peer.db();
        for view in [
            retained.clone(),
            latest.clone(),
            latest.clone().history(),
            latest.clone().as_of(seeded.basis_t),
            latest.clone().since(seeded.basis_t),
            latest
                .clone()
                .filter(|_, datom| datom.value != Value::Long(7)),
        ] {
            let query = range_query(SCORE);
            let indexed = view.query(&query, &[], &QueryControl::default()).unwrap();
            let scanned = view
                .query(
                    &query,
                    &[],
                    &QueryControl {
                        force_scan: true,
                        ..QueryControl::default()
                    },
                )
                .unwrap();
            assert_eq!(
                canonical_collection(indexed.result),
                canonical_collection(scanned.result)
            );
        }
        let mut selector = PullAttribute::forward(AttributeName::Id(VALUES));
        selector.limit = PullLimit::Limit(1);
        let pattern = PullPattern::attributes(vec![selector]);
        let filtered = latest.clone().filter(|_, datom| {
            datom.attribute != VALUES || !datom.value.index_cmp(&Value::Long(7)).is_lt()
        });
        assert_eq!(
            field(&filtered.pull(&pattern, root).unwrap(), "values"),
            &QueryValue::Collection(vec![QueryValue::Scalar(Value::Long(7))])
        );
        assert_eq!(
            field(&retained.pull(&pattern, root).unwrap(), "values"),
            &QueryValue::Collection(vec![QueryValue::Scalar(Value::Long(0))])
        );
        assert_eq!(
            latest
                .clone()
                .history()
                .pull(&pattern, root)
                .unwrap_err()
                .code,
            "database/history-not-point-in-time"
        );
        server.stop();
        let mut restarted = Server::start(&fixture.writer_url, &database, &endpoint);
        let reopened = Connection::connect(&fixture.peer_url, &database, 128).unwrap();
        let replay = reopened
            .transact_socket(&endpoint, request, WAIT)
            .unwrap()
            .report
            .unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, seeded.tx_hash);
        assert_eq!(replay.basis_t, seeded.basis_t);
        assert_eq!(replay.tempids, seeded.tempids);
        assert_eq!(
            field(&reopened.db().pull(&pattern, root).unwrap(), "values"),
            &QueryValue::Collection(vec![QueryValue::Scalar(Value::Long(1))])
        );
        restarted.stop();
    }
}
