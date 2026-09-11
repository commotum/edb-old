//! Direct, selective backup reads after the PostgreSQL source has disappeared.
mod common;
use atomic_core::storage::{BlockDatabase, BlockTransactor, BlockWriterOptions, PgBlockStore};
use atomic_core::*;
use std::fs;
use std::io::{Read, Write};
use std::path::PathBuf;
use std::process::{Command, Stdio};
use std::time::{Instant, SystemTime, UNIX_EPOCH};

const SCORE: u32 = 1000;
const LABEL: u32 = 1001;
fn schema() -> Schema {
    let mut schema = Schema::new();
    let mut score = Attribute::new(
        SCORE,
        Keyword::new("item", "score"),
        ValueType::Long,
        Cardinality::One,
    );
    score.indexed = true;
    schema.install(score).unwrap();
    schema
        .install(
            Attribute::new(
                LABEL,
                Keyword::new("item", "label"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    schema
}
fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}
fn directory() -> PathBuf {
    std::env::temp_dir().join(format!(
        "atomic_offline_reads_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    ))
}
fn query(start: i64) -> Query {
    Query::new(
        FindSpec::Relation(vec![FindElement::Variable("?v".into())]),
        vec![
            Clause::Pattern(Box::new(DataPattern::new(
                Term::var("?e"),
                Term::Constant(Value::Keyword(Keyword::new("item", "score"))),
                Term::var("?v"),
            ))),
            Clause::Predicate {
                predicate: Predicate::GreaterOrEqual,
                source: "$".into(),
                args: vec![Term::var("?v"), Term::Constant(Value::Long(start))],
            },
            Clause::Predicate {
                predicate: Predicate::Less,
                source: "$".into(),
                args: vec![Term::var("?v"), Term::Constant(Value::Long(start + 3))],
            },
        ],
    )
}

fn offline_cli(
    repository: &std::path::Path,
    arguments: &[&str],
    text: &str,
) -> atomic_core::edn::EdnValue {
    let mut child = Command::new(env!("CARGO_BIN_EXE_atomic"))
        .args(arguments)
        .arg("--repository")
        .arg(repository)
        .args(["--file", "-"])
        .env_remove("ATOMIC_POSTGRES_URL")
        .env_remove("ATOMIC_POSTGRES_TRANSPORT")
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
    let output = child.wait_with_output().unwrap();
    assert!(
        output.status.success(),
        "{}",
        String::from_utf8_lossy(&output.stderr)
    );
    atomic_core::edn::read_edn(std::str::from_utf8(&output.stdout).unwrap()).unwrap()
}
#[test]
fn offline_backup_query_pull_history_speculation_and_log_are_selective_and_source_independent() {
    let Ok(pg) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL is not configured");
        return;
    };
    for size in [64usize, 8192] {
        let setup = Instant::now();
        let fixture = common::PostgresFixture::new(&pg, "backup_reads");
        let connection = fixture.connection.clone();
        let config = PostgresConnectionConfig::plaintext(&connection);
        PgBlockStore::install(&config).unwrap();
        let created = BlockDatabase::create(&config, "offline", schema()).unwrap();
        // No service/index worker runs: backup must prepare its covering read
        // value locally from a genuinely unindexed recent tail.
        let mut writer = BlockTransactor::claim(
            &config,
            created,
            BlockWriterOptions {
                lease_duration: std::time::Duration::from_secs(120),
                ..Default::default()
            },
        )
        .unwrap();
        let mut operations = (0..size)
            .flat_map(|i| {
                [
                    add(
                        EntityRef::Temp(format!("e{i}")),
                        SCORE,
                        Value::Long(i as i64),
                    ),
                    add(
                        EntityRef::Temp(format!("e{i}")),
                        LABEL,
                        Value::String(format!("row-{i}")),
                    ),
                ]
            })
            .collect::<Vec<_>>();
        let program = Program {
            kind: ProgramKind::Query,
            arity: 1,
            instructions: vec![
                Instruction::PushArgument(0),
                Instruction::EmitRow(1),
                Instruction::Return,
            ],
        };
        let program_hash = PgBlockStore::connect(&config)
            .unwrap()
            .put(&encode_program(&program).unwrap())
            .unwrap();
        operations.extend([
            add(
                EntityRef::Temp("function".into()),
                DB_IDENT as u32,
                Value::Keyword(Keyword::new("offline", "identity")),
            ),
            add(
                EntityRef::Temp("function".into()),
                DB_FN as u32,
                Value::Function(program_hash),
            ),
        ]);
        // The large fixture is setup for a read-selectivity check, not a
        // thirty-second transaction-latency assertion in unoptimized builds.
        let first = writer
            .transact(&TransactionRequest::new("seed", operations).with_tx_instant(1000))
            .unwrap();
        let target_index = size / 2;
        let target = first.tempids[&format!("e{target_index}")];
        let backup_dir = directory();
        if size > 1000 {
            let control = MaintenanceControl::new(
                std::time::Duration::from_secs(30),
                std::sync::Arc::new(std::sync::atomic::AtomicBool::new(false)),
            )
            .unwrap();
            let worker_control = control.clone();
            let worker_connection = connection.clone();
            let worker_directory = backup_dir.clone();
            let worker = std::thread::spawn(move || {
                PortableBackup::connect(&worker_connection)
                    .unwrap()
                    .with_maintenance_control(worker_control)
                    .backup_database("offline", &worker_directory)
            });
            let waiting = Instant::now();
            while control.stats().pauses == 0 && !worker.is_finished() {
                assert!(waiting.elapsed() < std::time::Duration::from_secs(120));
                std::thread::sleep(std::time::Duration::from_millis(2));
            }
            assert!(control.stats().pauses > 0);
            control.cancel();
            assert_eq!(
                worker.join().unwrap().unwrap_err().code,
                "maintenance/canceled"
            );
            assert!(
                PortableBackup::list_backup_points(&backup_dir)
                    .unwrap()
                    .is_empty(),
                "cancellation must not publish a partial read root"
            );
            eprintln!(
                "BACKUP_CANCEL_OK copied_batches={} no_published_point=true",
                control.stats().completed_batches
            );
        }
        let mut backup = PortableBackup::connect(&connection).unwrap();
        let first_point = backup.backup_database("offline", &backup_dir).unwrap();
        let second = writer
            .transact(
                &TransactionRequest::new(
                    "edit",
                    vec![add(
                        EntityRef::Id(target),
                        LABEL,
                        Value::String("changed".into()),
                    )],
                )
                .comparing_basis(first.basis_t)
                .with_tx_instant(2000),
            )
            .unwrap();
        let second_point = backup.backup_database("offline", &backup_dir).unwrap();
        let expected_query = second
            .db_after
            .query(&query(target_index as i64), &[], &QueryControl::default())
            .unwrap()
            .result;
        let expected_history = second
            .db_after
            .clone()
            .history()
            .datoms_with_prefix(&IndexPrefix::Eavt {
                entity: target,
                attribute: Some(LABEL),
                value: None,
            })
            .unwrap();
        let expected_logs = [first.tx_data.clone(), second.tx_data.clone()];
        let first_t = first.basis_t;
        let second_t = second.basis_t;
        writer.release().unwrap();
        drop((first, second, backup));
        // All source relations are actually gone. An online reconnect or
        // compatibility materialization cannot satisfy any following read.
        drop(fixture);
        let setup_elapsed = setup.elapsed();

        let complete = Instant::now();
        let offline = BackupConnection::open_point(&backup_dir, &second_point).unwrap();
        let opened = offline.read_stats();
        let db = offline.db();
        assert!(
            db.snapshot_reference().is_err(),
            "file values cannot impersonate live publication authority"
        );
        assert_eq!(db.basis_t(), second_t);
        let result = db
            .query(&query(target_index as i64), &[], &QueryControl::default())
            .unwrap();
        assert_eq!(result.result, expected_query);
        assert!(
            result.stats.datoms_examined <= 4,
            "{}",
            result.stats.datoms_examined
        );
        let selected = offline.read_stats();
        assert!(
            selected.leaf_reads - opened.leaf_reads <= 2,
            "{opened:?} -> {selected:?}"
        );
        if size > 1000 {
            assert!(opened.leaf_reads < 32, "{opened:?}");
        }
        assert!(offline.cache_stats().current_bytes <= BackupReadConfig::default().cache_bytes);
        let warm_before = offline.read_stats();
        assert_eq!(
            db.query(&query(target_index as i64), &[], &QueryControl::default())
                .unwrap()
                .result,
            expected_query
        );
        assert_eq!(offline.read_stats().object_reads, warm_before.object_reads);
        drop(result);
        let cache = offline.cache_stats();
        drop((db, offline));
        let complete_elapsed = complete.elapsed();
        eprintln!(
            "offline size={size} setup={setup_elapsed:?} open+twoqueries+consume+drop={complete_elapsed:?} open={opened:?} selected={selected:?} cache={cache:?}"
        );

        let offline = BackupConnection::open_point(&backup_dir, &second_point).unwrap();
        let db = offline.db();
        let ProgramOutput::Query(program_rows) = db
            .invoke(
                Keyword::new("offline", "identity"),
                &[RuntimeValue::Scalar(Value::Long(37))],
                InvokeControl::default(),
            )
            .unwrap()
        else {
            panic!("native query output expected")
        };
        assert_eq!(program_rows, vec![vec![Value::Long(37)]]);
        let matches = db
            .fulltext(LABEL, "changed", &FulltextOptions::default())
            .unwrap();
        assert_eq!(
            matches
                .hits
                .iter()
                .map(|hit| hit.entity)
                .collect::<Vec<_>>(),
            vec![target]
        );

        if size == 64 {
            let queried = offline_cli(
                &backup_dir,
                &["query"],
                &format!(
                    "[:find ?v :where [?e :item/score ?v] [(>= ?v {target_index})] [(< ?v {})]]",
                    target_index + 3
                ),
            );
            assert!(format!("{queried:?}").contains("32"));
            let pulled = offline_cli(
                &backup_dir,
                &["pull", "--entity", &target.to_string()],
                "[:item/label]",
            );
            assert!(format!("{pulled:?}").contains("changed"));
            let preview = offline_cli(
                &backup_dir,
                &["with", "--tx-instant", "3000"],
                &format!("[{{:db/id {target} :item/label \"offline-preview\"}}]"),
            );
            assert!(format!("{preview:?}").contains("offline-preview"));
            let input = tempfile::NamedTempFile::new().unwrap();
            let mut input_file = input.reopen().unwrap();
            write!(
                input_file,
                "{{$log {{:repository {:?} :basis {} :generation {} :log true}}}}",
                backup_dir.to_str().unwrap(),
                second_point.basis_t,
                second_point.log_generation
            )
            .unwrap();
            let logs = offline_cli(
                &backup_dir,
                &["query", "--sources", input.path().to_str().unwrap()],
                "[:find ?tx :in $log :where [(tx-ids $log 1 3) [?tx ...]]]",
            );
            assert!(format!("{logs:?}").contains("1"));
            eprintln!(
                "OFFLINE_EDN_OK query/pull/with/log file+stdin without PostgreSQL configuration"
            );
        }

        let pattern =
            PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(LABEL))]);
        let pulled = db.pull(&pattern, target).unwrap();
        assert!(format!("{pulled:?}").contains("changed"));
        assert_eq!(
            db.clone()
                .history()
                .datoms_with_prefix(&IndexPrefix::Eavt {
                    entity: target,
                    attribute: Some(LABEL),
                    value: None
                })
                .unwrap(),
            expected_history
        );
        assert_eq!(
            db.clone().as_of(first_t).values(target, LABEL).unwrap(),
            vec![Value::String(format!("row-{target_index}"))]
        );
        assert_eq!(db.last_tx_instant().unwrap(), Some(2000));
        let reverse = db
            .reverse_seek_cursor(&IndexBoundary::Avet(IndexComponents::Two(
                SCORE,
                Value::Long(target_index as i64),
            )))
            .unwrap()
            .take(3)
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert_eq!(
            reverse.iter().map(|d| d.value.clone()).collect::<Vec<_>>(),
            (0..3)
                .map(|i| Value::Long(target_index as i64 - i))
                .collect::<Vec<_>>()
        );
        let speculative = db
            .with(
                &[add(
                    EntityRef::Id(target),
                    LABEL,
                    Value::String("hypothesis".into()),
                )],
                3000,
            )
            .unwrap();
        assert_eq!(
            speculative.db_after.values(target, LABEL).unwrap(),
            vec![Value::String("hypothesis".into())]
        );
        assert_eq!(
            db.values(target, LABEL).unwrap(),
            vec![Value::String("changed".into())]
        );
        let historical = BackupConnection::open_point(&backup_dir, &first_point).unwrap();
        assert_eq!(
            historical.db().values(target, LABEL).unwrap(),
            vec![Value::String(format!("row-{target_index}"))]
        );
        let log_before = offline.read_stats();
        assert_eq!(db.log_value().unwrap().basis_t(), second_t);
        let transactions = offline
            .log()
            .tx_range(Some(TimePoint::T(first_t)), None)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        let mut sampled_log = offline
            .log()
            .tx_range(Some(TimePoint::T(second_t)), None)
            .unwrap();
        assert!(sampled_log.next().unwrap().is_ok());
        assert_eq!(sampled_log.stats().postgres_payload_bytes_read, 0);
        assert!(sampled_log.stats().payload_bytes_read > 0);
        assert_eq!(
            transactions.iter().map(|t| t.t).collect::<Vec<_>>(),
            vec![first_t, second_t]
        );
        assert_eq!(
            transactions
                .iter()
                .map(|t| t.data.clone())
                .collect::<Vec<_>>(),
            expected_logs
        );
        assert_eq!(
            offline
                .log()
                .tx_range(Some(TimePoint::Instant(2000)), None)
                .unwrap()
                .next()
                .unwrap()
                .unwrap()
                .t,
            second_t
        );
        let log_after = offline.read_stats();
        eprintln!("offline size={size} two-log-payloads before={log_before:?} after={log_after:?}");
        let stopped = db.query(
            &query(target_index as i64),
            &[],
            &QueryControl {
                max_work: 1,
                ..Default::default()
            },
        );
        assert!(stopped.is_err());

        if size > 1000 {
            // Locate the selected EAVT leaf outside the measured read path.
            // A failed cold read is fused; previously cached immutable values
            // remain valid and no PostgreSQL fallback can hide the failure.
            let mut selected_leaf = None;
            for entry in fs::read_dir(backup_dir.join("objects")).unwrap() {
                let path = entry.unwrap().path();
                if !path.is_file() {
                    continue;
                }
                let bytes = fs::read(&path).unwrap();
                // Repository files use the provider's physical gzip envelope;
                // locate canonical leaves without changing the public API.
                // Keep the physical bytes below for corruption and restoration.
                let canonical = if bytes.starts_with(b"ATOMICBL") {
                    assert_eq!(&bytes[8..10], &[1, 1]);
                    let expected = u64::from_be_bytes(bytes[10..18].try_into().unwrap());
                    assert!(expected <= 64 * 1024 * 1024);
                    let mut canonical = Vec::new();
                    flate2::read::GzDecoder::new(&bytes[26..])
                        .take(expected + 1)
                        .read_to_end(&mut canonical)
                        .unwrap();
                    assert_eq!(canonical.len() as u64, expected);
                    canonical
                } else {
                    bytes.clone()
                };
                let hash = sha256(&canonical);
                if let Ok(atomic_core::persistent_tree::TreeNode::Leaf(leaf)) =
                    atomic_core::persistent_tree::decode_tree_node(&hash, &canonical)
                    && leaf.order == IndexOrder::Eavt
                    && !leaf.history
                    && (0..leaf.len()).any(|i| {
                        let datom = leaf.datom(i).unwrap();
                        datom.entity == target
                            && datom.attribute == LABEL
                            && datom.value == Value::String("changed".into())
                    })
                {
                    selected_leaf = Some((path, bytes));
                    break;
                }
            }
            let (path, bytes) = selected_leaf.expect("selected data leaf exists");
            let cold = BackupConnection::open_configured(
                &backup_dir,
                Some(&second_point),
                BackupReadConfig {
                    cache_entries: 0,
                    cache_bytes: 0,
                },
            )
            .unwrap();
            let mut corrupted = bytes.clone();
            let last = corrupted.len() - 1;
            corrupted[last] ^= 1;
            fs::write(&path, &corrupted).unwrap();
            let cold_db = cold.db();
            let mut cursor = cold_db
                .prefix_cursor(&IndexPrefix::Eavt {
                    entity: target,
                    attribute: None,
                    value: None,
                })
                .unwrap();
            assert!(cursor.next().unwrap().is_err());
            assert!(cursor.next().is_none());
            assert_eq!(
                db.values(target, LABEL).unwrap(),
                vec![Value::String("changed".into())]
            );
            fs::write(path, bytes).unwrap();
        }
        drop((db, offline, historical, speculative));
        fs::remove_dir_all(&backup_dir).unwrap();
    }
}
