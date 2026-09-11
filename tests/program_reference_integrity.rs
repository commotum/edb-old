//! Program reachability is derived evidence. Current-schema repair authenticates
//! retained log values and fixed code dependencies before allowing reclamation.
mod common;

use atomic_core::{
    Attribute, CallableRef, Cardinality, DB_EXCISE, Digest, EntityRef, Instruction, Keyword,
    POSTGRES_SCHEMA_VERSION, PostgresMigrator, PostgresOperator, PostgresStore, Program,
    ProgramKind, Schema, TransactionRequest, TxOp, TxValue, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::sync::{Mutex, MutexGuard};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const PROGRAMS: u32 = 1_000;
const NUMBER: u32 = 1_001;
const HIDDEN_CODE: u32 = 1_002;
static TEST_GATE: Mutex<()> = Mutex::new(());

fn unique(label: &str) -> String {
    format!(
        "refs_{label}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn decode_hex(text: &str) -> Vec<u8> {
    assert!(text.len().is_multiple_of(2));
    text.as_bytes()
        .chunks_exact(2)
        .map(|pair| {
            let digit = |byte: u8| match byte {
                b'0'..=b'9' => byte - b'0',
                b'a'..=b'f' => byte - b'a' + 10,
                _ => panic!("fixture contains invalid hexadecimal"),
            };
            (digit(pair[0]) << 4) | digit(pair[1])
        })
        .collect()
}

fn fixture_query_field(field: &str) -> Vec<u8> {
    let line = include_str!("fixtures/program_query_abi7.hex")
        .lines()
        .find_map(|line| line.strip_prefix(&format!("{field} ")))
        .expect("fixed program fixture field");
    decode_hex(line)
}

#[test]
fn abi7_query_bytes_hash_and_observation_are_stable() {
    let bytes = fixture_query_field("program");
    let program = atomic_core::decode_program(&bytes).unwrap();
    assert_eq!(atomic_core::encode_program(&program).unwrap(), bytes);
    assert_eq!(&bytes[16..18], &7u16.to_be_bytes());
    assert_eq!(
        atomic_core::program_hash(&program).unwrap().as_slice(),
        fixture_query_field("hash")
    );
    let output = atomic_core::ProgramRuntime
        .execute_runtime(
            &program,
            &atomic_core::Database::new(Schema::new()).unwrap(),
            &[atomic_core::RuntimeValue::Vector(
                (1..=4)
                    .map(|value| atomic_core::RuntimeValue::Scalar(Value::Long(value)))
                    .collect(),
            )],
            Default::default(),
        )
        .unwrap();
    assert!(
        matches!(&output, atomic_core::ProgramOutput::Query(rows) if rows == &vec![vec![Value::Long(7)]])
    );
    assert_eq!(
        atomic_core::encode_program_output(&output).unwrap(),
        fixture_query_field("output")
    );
}

struct RestoreArtifacts {
    admin: Client,
    schema: String,
    directory: std::path::PathBuf,
}

impl Drop for RestoreArtifacts {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA IF EXISTS {} CASCADE", self.schema));
        let _ = std::fs::remove_dir_all(&self.directory);
    }
}

struct Fixture {
    _guard: MutexGuard<'static, ()>,
    admin: Client,
    schema_name: String,
    connection: String,
    database: String,
    roots: Vec<Digest>,
    dependencies: Vec<Digest>,
    unused: Digest,
    noise: u64,
}

impl Fixture {
    fn create(label: &str) -> Option<Self> {
        let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
            eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
            return None;
        };
        // Operator semantic-GC admission uses a catalog-wide advisory key.
        // These schemas isolate data, but intentionally share that key.
        let guard = TEST_GATE.lock().unwrap_or_else(|error| error.into_inner());
        let schema_name = unique(label);
        let mut admin = Client::connect(&connection, NoTls).unwrap();
        admin
            .batch_execute(&format!("CREATE SCHEMA {schema_name}"))
            .unwrap();
        let connection =
            if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
                format!(
                    "{connection}{}options=-csearch_path%3D{schema_name}",
                    if connection.contains('?') { '&' } else { '?' }
                )
            } else {
                format!("{connection} options='-csearch_path={schema_name}'")
            };
        let mut admin = Client::connect(&connection, NoTls).unwrap();
        PostgresMigrator::connect(&connection)
            .unwrap()
            .migrate()
            .unwrap();
        let database = "reference-integrity".to_owned();
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                PROGRAMS,
                Keyword::new("item", "programs"),
                ValueType::Function,
                Cardinality::Many,
            ))
            .unwrap();
        schema
            .install(Attribute::new(
                NUMBER,
                Keyword::new("item", "number"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        let mut hidden = Attribute::new(
            HIDDEN_CODE,
            Keyword::new("item", "hidden-code"),
            ValueType::Function,
            Cardinality::One,
        );
        hidden.no_history = true;
        schema.install(hidden).unwrap();
        let mut store = PostgresStore::connect(&connection).unwrap();
        store.create_database(&database, schema).unwrap();
        let mut dependencies = Vec::new();
        for index in 0..4 {
            dependencies.push(
                store
                    .deploy_program_blob(&Program {
                        kind: ProgramKind::Transaction,
                        arity: 0,
                        instructions: vec![
                            Instruction::PushConstant(Value::Long(index)),
                            Instruction::Pop,
                            Instruction::Return,
                        ],
                    })
                    .unwrap(),
            );
        }
        let unused = store
            .deploy_program_blob(&Program {
                kind: ProgramKind::Transaction,
                arity: 0,
                instructions: vec![
                    Instruction::PushConstant(Value::Long(999)),
                    Instruction::Pop,
                    Instruction::Return,
                ],
            })
            .unwrap();
        let roots = vec![
            store
                .deploy_program_blob(&Program {
                    kind: ProgramKind::Transaction,
                    arity: 0,
                    instructions: vec![
                        Instruction::PushEntity(EntityRef::LookupInput {
                            attribute: NUMBER,
                            value: Box::new(TxValue::Tuple(vec![
                                Some(TxValue::Entity(EntityRef::LookupInput {
                                    attribute: NUMBER,
                                    value: Box::new(Value::Function(dependencies[0]).into()),
                                })),
                                None,
                            ])),
                        }),
                        Instruction::Pop,
                        Instruction::Return,
                    ],
                })
                .unwrap(),
            store
                .deploy_program_blob(&Program {
                    kind: ProgramKind::Transaction,
                    arity: 0,
                    instructions: vec![
                        Instruction::EmitCall {
                            function: CallableRef::Database(EntityRef::Lookup {
                                attribute: NUMBER,
                                value: Value::Function(dependencies[1]),
                            }),
                            argument_count: 0,
                        },
                        Instruction::Return,
                    ],
                })
                .unwrap(),
            store
                .deploy_program_blob(&Program {
                    kind: ProgramKind::DualPredicate,
                    arity: 1,
                    instructions: vec![
                        Instruction::PredicateDispatch {
                            attribute: vec![
                                Instruction::PushConstant(Value::Function(dependencies[2])),
                                Instruction::Pop,
                                Instruction::PushConstant(Value::Bool(true)),
                            ],
                            entity: vec![
                                Instruction::PushConstant(Value::Function(dependencies[3])),
                                Instruction::Pop,
                                Instruction::PushConstant(Value::Bool(true)),
                            ],
                        },
                        Instruction::Return,
                    ],
                })
                .unwrap(),
        ];
        let writer = common::start_service(&connection, &database);
        let mut operations = roots
            .iter()
            .map(|hash| TxOp::Add {
                entity: EntityRef::Temp("roots".into()),
                attribute: PROGRAMS,
                value: Value::Function(*hash).into(),
            })
            .collect::<Vec<_>>();
        operations.push(TxOp::Add {
            entity: EntityRef::Temp("noise".into()),
            attribute: NUMBER,
            value: Value::Long(10).into(),
        });
        let report = writer
            .client()
            .transact(
                TransactionRequest::new("programs", operations).with_tx_instant(1),
                Duration::from_secs(30),
            )
            .unwrap();
        let noise = report.tempids["noise"];
        drop(report);
        let report = writer
            .client()
            .transact(
                TransactionRequest::new(
                    "later",
                    vec![TxOp::Add {
                        entity: EntityRef::Id(noise),
                        attribute: NUMBER,
                        value: Value::Long(11).into(),
                    }],
                )
                .with_tx_instant(2),
                Duration::from_secs(30),
            )
            .unwrap();
        drop(report);
        writer.shutdown();
        admin.execute("UPDATE atomic_program_gc_candidates SET candidate_at = clock_timestamp() - interval '40 days'", &[]).unwrap();
        Some(Self {
            _guard: guard,
            admin,
            schema_name,
            connection,
            database,
            roots,
            dependencies,
            unused,
            noise,
        })
    }

    fn expected(&self) -> BTreeSet<Vec<u8>> {
        self.roots
            .iter()
            .chain(&self.dependencies)
            .map(|hash| hash.to_vec())
            .collect()
    }

    fn references(&mut self, generation: i64) -> BTreeSet<Vec<u8>> {
        self.admin.query("SELECT program_hash FROM atomic_program_generation_refs WHERE database_id=$1 AND log_generation=$2", &[&self.database, &generation]).unwrap().into_iter().map(|row| row.get(0)).collect()
    }

    fn remove_dependency_references(&mut self) {
        let mut transaction = self.admin.transaction().unwrap();
        transaction
            .batch_execute("SET LOCAL session_replication_role = replica")
            .unwrap();
        for hash in &self.dependencies {
            transaction
                .execute(
                    "DELETE FROM atomic_program_generation_refs WHERE program_hash = $1",
                    &[&&hash[..]],
                )
                .unwrap();
        }
        transaction.commit().unwrap();
    }

    fn obsolete(&mut self) {
        self.remove_dependency_references();
        self.admin.batch_execute("UPDATE atomic_program_reference_state SET complete=true, problem_code=NULL, walker_version=0").unwrap();
    }

    fn canonical_fingerprint(&mut self) -> Digest {
        let mut bytes = Vec::new();
        for row in self.admin.query("SELECT t.tx_hash,t.content_hash,c.payload FROM atomic_generation_transactions t JOIN atomic_transaction_contents c USING(content_hash) WHERE t.database_id=$1 ORDER BY t.generation,t.basis_t", &[&self.database]).unwrap() {
            for column in 0..3 {
                let value: Vec<u8> = row.get(column);
                bytes.extend_from_slice(&(value.len() as u64).to_be_bytes());
                bytes.extend_from_slice(&value);
            }
        }
        atomic_core::sha256(&bytes)
    }

    fn marker(&mut self) -> (bool, Option<String>, i64, String) {
        let row = self.admin.query_one("SELECT complete, problem_code, walker_version, updated_at::text FROM atomic_program_reference_state WHERE singleton", &[]).unwrap();
        (row.get(0), row.get(1), row.get(2), row.get(3))
    }
}

impl Drop for Fixture {
    fn drop(&mut self) {
        // Only this fixture's generated schema is removed, including corruption
        // cases. No shared catalog, server, or unrelated logical DB is altered.
        let _ = self.admin.batch_execute(&format!(
            "DROP SCHEMA IF EXISTS {} CASCADE",
            self.schema_name
        ));
    }
}

#[test]
fn current_schema_reference_repair_preserves_canonical_values_and_exact_retries() {
    let Some(mut fixture) = Fixture::create("repair") else {
        return;
    };
    let canonical = fixture.canonical_fingerprint();
    let baseline = fixture
        .admin
        .query_one(
            "SELECT version, checksum FROM atomic_schema_migrations",
            &[],
        )
        .unwrap();
    let version: i64 = baseline.get(0);
    let checksum: Vec<u8> = baseline.get(1);
    assert_eq!(version, POSTGRES_SCHEMA_VERSION);
    fixture.obsolete();
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    assert!(fixture.marker().0);
    assert_eq!(fixture.marker().2, 2);
    assert_eq!(fixture.references(1), fixture.expected());
    assert_eq!(fixture.canonical_fingerprint(), canonical);
    let after = fixture
        .admin
        .query_one(
            "SELECT version, checksum FROM atomic_schema_migrations",
            &[],
        )
        .unwrap();
    assert_eq!(after.get::<_, i64>(0), version);
    assert_eq!(after.get::<_, Vec<u8>>(1), checksum);
    let collected = fixture
        .admin
        .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, Vec<u8>>(0))
        .collect::<Vec<_>>();
    assert_eq!(collected, vec![fixture.unused.to_vec()]);
    assert_eq!(fixture.references(1), fixture.expected());
    // Exercise supported query and predicate programs after repair, then
    // retry an existing request and verify its exact snapshot pair.
    let mut store = PostgresStore::connect(&fixture.connection).unwrap();
    let query_bytes = fixture_query_field("program");
    let query = atomic_core::decode_program(&query_bytes).unwrap();
    let query_hash = store.deploy_program_blob(&query).unwrap();
    assert_eq!(query_hash.as_slice(), fixture_query_field("hash"));
    let predicate = store.resolve_program(fixture.roots[2]).unwrap();
    assert_eq!(
        atomic_core::program_hash(&predicate).unwrap(),
        fixture.roots[2]
    );
    let writer = common::start_service(&fixture.connection, &fixture.database);
    let installed = common::transact(
        &writer,
        "code-bindings",
        3,
        &[
            TxOp::Add {
                entity: EntityRef::Temp("query".into()),
                attribute: atomic_core::DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("app", "query")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("query".into()),
                attribute: atomic_core::DB_FN as u32,
                value: Value::Function(query_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("predicate".into()),
                attribute: atomic_core::DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("app", "predicate")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("predicate".into()),
                attribute: atomic_core::DB_FN as u32,
                value: Value::Function(fixture.roots[2]).into(),
            },
        ],
        3,
    );
    let peer = atomic_core::Connection::connect(&fixture.connection, &fixture.database, 8).unwrap();
    let value = peer.db();
    let output = value
        .invoke(
            Keyword::new("app", "query"),
            &[atomic_core::RuntimeValue::Vector(
                (1..=4)
                    .map(|value| atomic_core::RuntimeValue::Scalar(Value::Long(value)))
                    .collect(),
            )],
            Default::default(),
        )
        .unwrap();
    assert_eq!(
        atomic_core::encode_program_output(&output).unwrap(),
        fixture_query_field("output")
    );
    for (role, argument) in [
        (atomic_core::InvokeRole::AttributePredicate, Value::Long(42)),
        (
            atomic_core::InvokeRole::EntityPredicate,
            Value::Ref(fixture.noise),
        ),
    ] {
        let output = value
            .invoke(
                Keyword::new("app", "predicate"),
                &[atomic_core::RuntimeValue::Scalar(argument)],
                atomic_core::InvokeControl {
                    role,
                    ..Default::default()
                },
            )
            .unwrap();
        match output {
            atomic_core::ProgramOutput::AttributePredicate(value)
            | atomic_core::ProgramOutput::EntityPredicate(value) => {
                assert!(atomic_core::is_exact_true(&value))
            }
            other => panic!("predicate changed role: {other:?}"),
        }
    }
    let request = || {
        TransactionRequest::new(
            "later",
            vec![TxOp::Add {
                entity: EntityRef::Id(fixture.noise),
                attribute: NUMBER,
                value: Value::Long(11).into(),
            }],
        )
        .with_tx_instant(2)
    };
    let retry = writer
        .client()
        .transact(request(), Duration::from_secs(30))
        .unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, 3);
    assert_eq!(
        retry.db_before.values(fixture.noise, NUMBER).unwrap(),
        vec![Value::Long(10)]
    );
    assert_eq!(
        retry.db_after.values(fixture.noise, NUMBER).unwrap(),
        vec![Value::Long(11)]
    );
    writer.shutdown();
    let restarted = common::start_service(&fixture.connection, &fixture.database);
    let again = restarted
        .client()
        .transact(request(), Duration::from_secs(30))
        .unwrap();
    assert!(again.replayed);
    assert_eq!(again.basis_t, retry.basis_t);
    assert_eq!(again.tempids, retry.tempids);
    assert_eq!(again.tx_data, retry.tx_data);
    assert_eq!(
        store.recover(&fixture.database).unwrap().basis_t(),
        installed.basis_t
    );
    restarted.shutdown();
    eprintln!(
        "reference repair preserves program execution and receipt retries retain exact snapshots across restart"
    );
}

#[test]
fn obsolete_markers_block_both_inventory_and_sql_gc_and_healthy_migrate_is_read_only() {
    let Some(mut fixture) = Fixture::create("marker") else {
        return;
    };
    fixture.obsolete();
    assert!(
        PostgresOperator::connect(&fixture.connection)
            .unwrap()
            .garbage_inventory(Duration::ZERO)
            .unwrap()
            .program_hashes
            .is_empty()
    );
    assert!(
        fixture
            .admin
            .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
            .unwrap()
            .is_empty()
    );
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    assert_eq!(fixture.references(1), fixture.expected());
    let before = fixture.marker();
    fixture.admin.batch_execute("CREATE FUNCTION reject_reference_rewrite() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'healthy migration rewrote reference evidence'; END $$; CREATE TRIGGER reject_marker_rewrite BEFORE INSERT OR UPDATE OR DELETE ON atomic_program_reference_state FOR EACH STATEMENT EXECUTE FUNCTION reject_reference_rewrite(); CREATE TRIGGER reject_refs_rewrite BEFORE INSERT OR UPDATE OR DELETE ON atomic_program_generation_refs FOR EACH STATEMENT EXECUTE FUNCTION reject_reference_rewrite()").unwrap();
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    assert_eq!(before, fixture.marker());
    fixture.admin.batch_execute("DROP TRIGGER reject_marker_rewrite ON atomic_program_reference_state; DROP TRIGGER reject_refs_rewrite ON atomic_program_generation_refs; DELETE FROM atomic_program_reference_state").unwrap();
    assert!(
        fixture
            .admin
            .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
            .unwrap()
            .is_empty()
    );
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    assert!(fixture.marker().0);
    assert_eq!(fixture.references(1), fixture.expected());
}

#[test]
fn missing_and_corrupt_dependencies_abort_reference_repair_atomically() {
    for corrupt in [false, true] {
        let label = if corrupt { "corrupt" } else { "missing" };
        let Some(mut fixture) = Fixture::create(label) else {
            return;
        };
        fixture.obsolete();
        let before = fixture.marker();
        let refs = fixture.references(1);
        let mut transaction = fixture.admin.transaction().unwrap();
        transaction
            .batch_execute("SET LOCAL session_replication_role = replica")
            .unwrap();
        let hash = fixture.dependencies[0];
        if corrupt {
            transaction.execute("UPDATE atomic_programs SET payload = payload || decode('00','hex') WHERE program_hash=$1", &[&&hash[..]]).unwrap();
        } else {
            transaction
                .execute(
                    "DELETE FROM atomic_programs WHERE program_hash=$1",
                    &[&&hash[..]],
                )
                .unwrap();
        }
        transaction.commit().unwrap();
        let error = PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap_err();
        assert_eq!(
            error.code,
            if corrupt {
                "postgres/program-ref-hash"
            } else {
                "postgres/program-ref-missing-program"
            }
        );
        assert_eq!(fixture.marker(), before);
        assert_eq!(fixture.references(1), refs);
        assert!(
            fixture
                .admin
                .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
                .unwrap()
                .is_empty()
        );
    }
}

#[test]
fn newer_reference_walker_is_not_reinterpreted_by_an_older_binary() {
    let Some(mut fixture) = Fixture::create("future") else {
        return;
    };
    fixture
        .admin
        .batch_execute("UPDATE atomic_program_reference_state SET walker_version=3")
        .unwrap();
    let before = fixture.marker();
    assert_eq!(
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap_err()
            .code,
        "postgres/program-reference-walker-too-new"
    );
    assert_eq!(fixture.marker(), before);
    assert!(
        fixture
            .admin
            .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
            .unwrap()
            .is_empty()
    );
    fixture
        .admin
        .execute(
            "INSERT INTO atomic_schema_migrations(version,checksum) VALUES($1,$2)",
            &[&(POSTGRES_SCHEMA_VERSION + 1), &&[0_u8; 32][..]],
        )
        .unwrap();
    assert_eq!(
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap_err()
            .code,
        "postgres/schema-too-new"
    );
}

#[test]
fn truncated_published_or_retired_terminal_aborts_and_exact_row_repair_recovers() {
    for retired in [false, true] {
        let Some(mut fixture) = Fixture::create(if retired {
            "retired_tail"
        } else {
            "active_tail"
        }) else {
            return;
        };
        let writer = common::start_service(&fixture.connection, &fixture.database);
        let mut operations = vec![TxOp::Add {
            entity: EntityRef::Temp("terminal-code".into()),
            attribute: PROGRAMS,
            value: Value::Function(fixture.unused).into(),
        }];
        if retired {
            operations.push(TxOp::Add {
                entity: EntityRef::Temp("excise".into()),
                attribute: DB_EXCISE as u32,
                value: TxValue::Entity(EntityRef::Id(fixture.noise)),
            });
        }
        let report = writer
            .client()
            .transact(
                TransactionRequest::new("terminal-code", operations).with_tx_instant(3),
                Duration::from_secs(30),
            )
            .unwrap();
        let code_entity = report.tempids["terminal-code"];
        let terminal_basis = report.basis_t as i64;
        drop(report);
        writer.shutdown();
        if retired {
            PostgresOperator::connect(&fixture.connection)
                .unwrap()
                .process_excision_requests(&fixture.database)
                .unwrap();
        }
        atomic_core::PostgresIndexer::connect(&fixture.connection, &fixture.database)
            .unwrap()
            .consolidate()
            .unwrap();
        let snapshot = atomic_core::Peer::connect(&fixture.connection, &fixture.database, 0)
            .unwrap()
            .database_value();
        assert_eq!(
            snapshot.values(code_entity, PROGRAMS).unwrap(),
            vec![Value::Function(fixture.unused)]
        );
        fixture.obsolete();
        let marker = fixture.marker();
        let references = fixture.references(1);
        assert!(references.contains(fixture.unused.as_slice()));
        // Keep original immutable bytes for a genuine repair-and-retry proof.
        // Remove both membership and receipt: a shorter prefix alone remains
        // internally valid, but cannot satisfy publication authority.
        let mut transaction = fixture.admin.transaction().unwrap();
        transaction
            .batch_execute("SET LOCAL session_replication_role=replica")
            .unwrap();
        transaction.execute("CREATE TEMP TABLE saved_terminal_membership AS SELECT * FROM atomic_generation_transactions WHERE database_id=$1 AND generation=1 AND basis_t=$2", &[&fixture.database, &terminal_basis]).unwrap();
        transaction.execute("CREATE TEMP TABLE saved_terminal_receipt AS SELECT * FROM atomic_generation_requests WHERE database_id=$1 AND generation=1 AND basis_t=$2", &[&fixture.database, &terminal_basis]).unwrap();
        transaction.batch_execute("CREATE TEMP TABLE saved_terminal_tempids AS SELECT t.* FROM atomic_generation_request_tempids t JOIN saved_terminal_receipt r USING(database_id,generation,request_key_hash); DELETE FROM atomic_generation_request_tempids t USING saved_terminal_receipt r WHERE t.database_id=r.database_id AND t.generation=r.generation AND t.request_key_hash=r.request_key_hash; DELETE FROM atomic_generation_requests t USING saved_terminal_receipt r WHERE t.database_id=r.database_id AND t.generation=r.generation AND t.request_key_hash=r.request_key_hash; DELETE FROM atomic_generation_transactions t USING saved_terminal_membership r WHERE t.database_id=r.database_id AND t.generation=r.generation AND t.basis_t=r.basis_t").unwrap();
        transaction.commit().unwrap();
        let error = PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap_err();
        assert_eq!(error.code, "postgres/program-ref-endpoint-mismatch");
        assert_eq!(fixture.marker(), marker);
        assert_eq!(fixture.references(1), references);
        assert!(
            fixture
                .admin
                .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
                .unwrap()
                .is_empty()
        );
        assert_eq!(
            snapshot.values(code_entity, PROGRAMS).unwrap(),
            vec![Value::Function(fixture.unused)]
        );
        let mut transaction = fixture.admin.transaction().unwrap();
        transaction.batch_execute("SET LOCAL session_replication_role=replica; INSERT INTO atomic_generation_transactions SELECT * FROM saved_terminal_membership; INSERT INTO atomic_generation_requests SELECT * FROM saved_terminal_receipt; INSERT INTO atomic_generation_request_tempids SELECT * FROM saved_terminal_tempids").unwrap();
        transaction.commit().unwrap();
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap();
        let mut expected = fixture.expected();
        expected.insert(fixture.unused.to_vec());
        assert_eq!(fixture.references(1), expected);
        if retired {
            assert_eq!(fixture.references(2), expected);
        }
        assert!(
            fixture
                .admin
                .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
                .unwrap()
                .is_empty()
        );
        assert_eq!(
            atomic_core::Peer::connect(&fixture.connection, &fixture.database, 0)
                .unwrap()
                .database_value()
                .values(code_entity, PROGRAMS)
                .unwrap(),
            vec![Value::Function(fixture.unused)]
        );
    }
}

#[test]
fn retained_no_history_log_references_are_not_lost_by_materialized_replay() {
    let Some(mut fixture) = Fixture::create("nohistory") else {
        return;
    };
    let old = PostgresStore::connect(&fixture.connection)
        .unwrap()
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![
                Instruction::PushConstant(Value::Long(10042)),
                Instruction::Pop,
                Instruction::Return,
            ],
        })
        .unwrap();
    let writer = common::start_service(&fixture.connection, &fixture.database);
    for (instant, hash) in [(3, old), (4, fixture.unused)] {
        let report = writer
            .client()
            .transact(
                TransactionRequest::new(
                    format!("nohistory-{instant}"),
                    vec![TxOp::Add {
                        entity: EntityRef::Id(fixture.noise),
                        attribute: HIDDEN_CODE,
                        value: Value::Function(hash).into(),
                    }],
                )
                .with_tx_instant(instant),
                Duration::from_secs(30),
            )
            .unwrap();
        drop(report);
    }
    writer.shutdown();
    fixture.obsolete();
    let mut transaction = fixture.admin.transaction().unwrap();
    transaction
        .batch_execute("SET LOCAL session_replication_role = replica")
        .unwrap();
    transaction
        .execute(
            "DELETE FROM atomic_program_generation_refs WHERE program_hash=$1",
            &[&&old[..]],
        )
        .unwrap();
    transaction.commit().unwrap();
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    let refs = fixture.references(1);
    assert!(refs.contains(old.as_slice()));
    assert!(refs.contains(fixture.unused.as_slice()));
}

#[test]
fn paused_headless_restore_keeps_prestaged_program_roots_through_repair_and_gc() {
    let Some(fixture) = Fixture::create("paused_restore") else {
        return;
    };
    let schema = unique("restore_target");
    let base = std::env::var("ATOMIC_POSTGRES_URL").unwrap();
    let mut admin = Client::connect(&base, NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let target = if base.starts_with("postgres://") || base.starts_with("postgresql://") {
        format!(
            "{base}{}options=-csearch_path%3D{schema}",
            if base.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{base} options='-csearch_path={schema}'")
    };
    let directory = std::env::temp_dir().join(unique("restore_backup"));
    let _artifacts = RestoreArtifacts {
        admin,
        schema,
        directory: directory.clone(),
    };
    let point = atomic_core::PortableBackup::connect(&fixture.connection)
        .unwrap()
        .backup_database(&fixture.database, &directory)
        .unwrap();
    PostgresMigrator::connect(&target)
        .unwrap()
        .migrate()
        .unwrap();
    let mut restore = atomic_core::PortableBackup::connect(&target).unwrap();
    let error = restore
        .restore_backup_with_fault(
            &directory,
            point.basis_t,
            "headless-target",
            atomic_core::RestoreFault::AfterFirstContentInserted,
        )
        .unwrap_err();
    assert_eq!(error.category, atomic_core::ErrorCategory::Interrupted);
    let mut raw = Client::connect(&target, NoTls).unwrap();
    assert_eq!(
        raw.query_one("SELECT count(*) FROM atomic_heads", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    assert_eq!(
        raw.query_one("SELECT count(*) FROM atomic_generation_transactions", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    let mut transaction = raw.transaction().unwrap();
    transaction.batch_execute("SET LOCAL session_replication_role=replica; UPDATE atomic_program_reference_state SET walker_version=0, complete=true, problem_code=NULL").unwrap();
    for hash in &fixture.dependencies {
        transaction
            .execute(
                "DELETE FROM atomic_program_generation_refs WHERE program_hash=$1",
                &[&&hash[..]],
            )
            .unwrap();
    }
    transaction.commit().unwrap();
    PostgresMigrator::connect(&target)
        .unwrap()
        .migrate()
        .unwrap();
    let marks = raw
        .query(
            "SELECT program_hash FROM atomic_program_generation_refs",
            &[],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, Vec<u8>>(0))
        .collect::<BTreeSet<_>>();
    assert_eq!(
        marks,
        fixture.expected(),
        "future log code must remain rooted while the restore is paused"
    );
    assert!(
        raw.query("SELECT atomic_collect_program_garbage(0,4096)", &[])
            .unwrap()
            .is_empty()
    );
    let restored = restore
        .restore_backup(&directory, point.basis_t, "headless-target")
        .unwrap();
    assert_eq!(restored.basis_t(), point.basis_t);
    assert_eq!(
        raw.query_one("SELECT count(*) FROM atomic_program_generation_refs", &[])
            .unwrap()
            .get::<_, i64>(0),
        fixture.expected().len() as i64
    );
    assert!(
        raw.query("SELECT atomic_collect_program_garbage(0,4096)", &[])
            .unwrap()
            .is_empty()
    );
}

#[test]
fn authenticated_partial_generation_gc_can_resume_after_reference_repair() {
    let Some(mut fixture) = Fixture::create("partial_gc") else {
        return;
    };
    let writer = common::start_service(&fixture.connection, &fixture.database);
    let report = writer
        .client()
        .transact(
            TransactionRequest::new(
                "excise-noise",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("excise".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(fixture.noise)),
                }],
            )
            .with_tx_instant(3),
            Duration::from_secs(30),
        )
        .unwrap();
    drop(report);
    writer.shutdown();
    let mut operator = PostgresOperator::connect(&fixture.connection).unwrap();
    let receipt = operator
        .process_excision_requests(&fixture.database)
        .unwrap();
    assert_eq!(receipt.source_generation, 1);
    let mut eligible = false;
    for _ in 0..128 {
        let inventory = operator.garbage_inventory(Duration::ZERO).unwrap();
        if inventory.log_generations.iter().any(|generation| {
            generation.database_id == fixture.database && generation.generation == 1
        }) {
            eligible = true;
            break;
        }
        operator.collect_garbage(Duration::ZERO).unwrap();
    }
    assert!(eligible, "retired generation should become collectible");
    fixture
        .admin
        .query(
            "SELECT * FROM atomic_collect_semantic_commitment_generation_roots($1,1,0,4096,false)",
            &[&fixture.database],
        )
        .unwrap();
    let mut prefix_removed = false;
    for _ in 0..128 {
        fixture
            .admin
            .query(
                "SELECT * FROM atomic_collect_log_generation($1,1,0,1)",
                &[&fixture.database],
            )
            .unwrap();
        let minimum: Option<i64> = fixture.admin.query_one("SELECT min(basis_t) FROM atomic_generation_transactions WHERE database_id=$1 AND generation=1", &[&fixture.database]).unwrap().get(0);
        if minimum.is_some_and(|basis| basis > 1) {
            prefix_removed = true;
            break;
        }
    }
    assert!(
        prefix_removed,
        "bounded collector must leave a retained suffix"
    );
    fixture.obsolete();
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    assert_eq!(fixture.references(1), fixture.expected());
    assert_eq!(
        fixture.references(receipt.generation as i64),
        fixture.expected()
    );
    assert_eq!(fixture.marker().2, 2);
    for _ in 0..128 {
        let result = fixture
            .admin
            .query_one(
                "SELECT * FROM atomic_collect_log_generation($1,1,0,1)",
                &[&fixture.database],
            )
            .unwrap();
        if result.get::<_, bool>(2) {
            return;
        }
    }
    panic!("bounded collection should finish after reference repair");
}
