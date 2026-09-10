//! Versioned program reachability is derived evidence. Upgrade authenticates
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

/// Restore only a named historical function, never re-run an entire migration
/// over populated fixture tables. These exact definitions make the fixture a
/// genuine old schema instead of retaining newer behavior under old records.
fn historical_function<'a>(sql: &'a str, name: &str) -> &'a str {
    let start = sql
        .find(&format!("CREATE OR REPLACE FUNCTION {name}("))
        .expect("historical function must exist");
    let end = start
        + sql[start..]
            .find("\n$$;")
            .expect("historical function terminator")
        + 4;
    &sql[start..end]
}

/// Strip the later optional artifacts from this private, stopped fixture before
/// restoring schema23. Canonical log/program/tree values are never rewritten.
fn remove_migrations_26_through_30(transaction: &mut postgres::Transaction<'_>) {
    let installed: i64 = transaction
        .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    if installed <= 25 {
        return;
    }
    assert_eq!(
        installed, 30,
        "add explicit rollback for newer fixture artifacts"
    );
    for relation in [
        "atomic_change_checkpoints",
        "atomic_remote_writer_endpoints",
    ] {
        assert_eq!(
            transaction
                .query_one(&format!("SELECT count(*) FROM {relation}"), &[])
                .unwrap()
                .get::<_, i64>(0),
            0,
            "stopped upgrade fixture must not discard consumer or routing state",
        );
    }
    // Search and compressed blocks are discardable projections. Remove their
    // tables, functions and external trigger, not just migration version rows.
    transaction
        .batch_execute(
            "DROP TRIGGER atomic_tree_manifest_fulltext_garbage ON atomic_tree_manifests;
         DROP TABLE atomic_fulltext_page_edges, atomic_fulltext_page_roots,
                    atomic_fulltext_page_builds, atomic_fulltext_page_garbage,
                    atomic_fulltext_pages, atomic_fulltext_projections,
                    atomic_fulltext_blocks, atomic_fulltext_garbage,
                    atomic_change_checkpoints, atomic_remote_writer_endpoints,
                    atomic_tree_node_blocks;
         DROP FUNCTION atomic_fulltext_gc_pin_key(),
                       atomic_validate_fulltext_page_source(),
                       atomic_validate_fulltext_projection_root(),
                       atomic_enqueue_fulltext_source_pages(BYTEA),
                       atomic_track_fulltext_page_reference(),
                       atomic_retire_fulltext_build(),
                       atomic_finish_fulltext_build(BYTEA),
                       atomic_fulltext_garbage_candidates(BIGINT),
                       atomic_collect_fulltext_garbage(BIGINT),
                       atomic_mark_fulltext_garbage(),
                       atomic_validate_fulltext_block_insert(),
                       atomic_reject_fulltext_mutation(),
                       atomic_validate_remote_writer_endpoint(),
                       atomic_discover_remote_writer(TEXT,TEXT),
                       atomic_validate_tree_node_block_insert(),
                       atomic_reject_tree_node_block_mutation();
         DELETE FROM atomic_schema_migrations WHERE version BETWEEN 26 AND 30;",
        )
        .unwrap();
}

fn remove_migration_25(transaction: &mut postgres::Transaction<'_>) {
    remove_migrations_26_through_30(transaction);
    let installed: i64 = transaction
        .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    if installed <= 24 {
        return;
    }
    assert_eq!(
        installed, 25,
        "add explicit fixture rollback for each newer migration; never pretend its artifacts are schema23"
    );
    assert_eq!(
        transaction
            .query_one(
                "SELECT count(*) FROM atomic_receipt_archive_conversions",
                &[]
            )
            .unwrap()
            .get::<_, i64>(0),
        0,
        "upgrade fixture must not discard active receipt conversions"
    );
    transaction.batch_execute("DROP FUNCTION atomic_finish_receipt_archive_conversion(BYTEA,BIGINT); DROP FUNCTION atomic_begin_receipt_archive_conversion(BYTEA,BIGINT); DROP FUNCTION atomic_receipt_archive_conversion_context(BYTEA); DROP FUNCTION atomic_collect_tree_retirement(TEXT,BIGINT,BYTEA,BIGINT,BIGINT); ALTER FUNCTION atomic_collect_tree_retirement_unconverted_v22(TEXT,BIGINT,BYTEA,BIGINT,BIGINT) RENAME TO atomic_collect_tree_retirement; DROP FUNCTION atomic_collect_request_base_archive(TEXT,BIGINT,BYTEA,BIGINT,BIGINT); ALTER FUNCTION atomic_collect_request_base_archive_unconverted_v20(TEXT,BIGINT,BYTEA,BIGINT,BIGINT) RENAME TO atomic_collect_request_base_archive").unwrap();
    let archive_sql = include_str!("../migrations/0020_request_base_archives.sql");
    for name in [
        "atomic_validate_request_base_archive_insert",
        "atomic_reject_request_base_archive_mutation",
        "atomic_collect_request_base_archive",
    ] {
        transaction
            .batch_execute(historical_function(archive_sql, name))
            .unwrap();
    }
    transaction
        .batch_execute(historical_function(
            include_str!("../migrations/0017_request_snapshot_bases.sql"),
            "atomic_release_inactive_request_bases",
        ))
        .unwrap();
    transaction.batch_execute("DROP TABLE atomic_receipt_archive_frontier; DROP TABLE atomic_receipt_archive_conversions; DELETE FROM atomic_schema_migrations WHERE version=25").unwrap();
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
        let database = "reference-upgrade".to_owned();
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

    fn remove_old_omissions(&mut self) {
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
        self.remove_old_omissions();
        self.admin.batch_execute("UPDATE atomic_program_reference_state SET complete=true, problem_code=NULL, walker_version=0").unwrap();
    }

    fn downgrade_to_23(&mut self) {
        self.remove_old_omissions();
        let old = include_str!("../migrations/0014_log_generations.sql");
        let start = old
            .find("CREATE OR REPLACE FUNCTION atomic_collect_program_garbage(")
            .unwrap();
        let end = old[start..]
            .find("CREATE OR REPLACE FUNCTION atomic_publish_tree(")
            .unwrap()
            + start;
        let mut transaction = self.admin.transaction().unwrap();
        remove_migration_25(&mut transaction);
        transaction.batch_execute(&old[start..end]).unwrap();
        transaction.batch_execute("ALTER TABLE atomic_program_reference_state DROP COLUMN walker_version; DELETE FROM atomic_schema_migrations WHERE version=24").unwrap();
        transaction.commit().unwrap();
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
fn schema23_complete_marks_are_rebuilt_with_unchanged_prior_checksums() {
    let Some(mut fixture) = Fixture::create("v23") else {
        return;
    };
    let expected_v24_checksum: Vec<u8> = fixture
        .admin
        .query_one(
            "SELECT checksum FROM atomic_schema_migrations WHERE version=24",
            &[],
        )
        .unwrap()
        .get(0);
    fixture.downgrade_to_23();
    let prior = fixture
        .admin
        .query(
            "SELECT version, checksum FROM atomic_schema_migrations ORDER BY version",
            &[],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get::<_, i64>(0), row.get::<_, Vec<u8>>(1)))
        .collect::<Vec<_>>();
    assert_eq!(prior.len(), 23);
    assert!(
        fixture
            .admin
            .query_one(
                "SELECT complete FROM atomic_program_reference_state WHERE singleton",
                &[]
            )
            .unwrap()
            .get::<_, bool>(0)
    );
    assert_eq!(
        PostgresStore::connect(&fixture.connection)
            .err()
            .unwrap()
            .code,
        "postgres/schema-upgrade-required"
    );
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    assert!(fixture.marker().0);
    assert_eq!(fixture.marker().2, 1);
    assert_eq!(fixture.references(1), fixture.expected());
    let after = fixture.admin.query("SELECT version, checksum FROM atomic_schema_migrations WHERE version<=23 ORDER BY version", &[]).unwrap().into_iter().map(|row| (row.get::<_, i64>(0), row.get::<_, Vec<u8>>(1))).collect::<Vec<_>>();
    assert_eq!(prior, after);
    let restored_v24_checksum: Vec<u8> = fixture
        .admin
        .query_one(
            "SELECT checksum FROM atomic_schema_migrations WHERE version=24",
            &[],
        )
        .unwrap()
        .get(0);
    assert_eq!(restored_v24_checksum, expected_v24_checksum);
    assert_eq!(
        fixture
            .admin
            .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
            .unwrap()
            .get::<_, i64>(0),
        POSTGRES_SCHEMA_VERSION
    );
    let collected = fixture
        .admin
        .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, Vec<u8>>(0))
        .collect::<Vec<_>>();
    assert_eq!(collected, vec![fixture.unused.to_vec()]);
    assert_eq!(fixture.references(1), fixture.expected());
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
        let Some(mut fixture) = Fixture::create(if corrupt { "corrupt" } else { "missing" }) else {
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
        // A failing actual 23->24 upgrade rolls back DDL and its migration
        // record too. New runtime entry points remain closed. Operators must
        // not resume legacy GC after such a rollback; old binaries cannot be
        // retroactively made safe by a migration that did not commit.
        fixture.downgrade_to_23();
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
        assert_eq!(
            fixture
                .admin
                .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
                .unwrap()
                .get::<_, i64>(0),
            23
        );
        assert!(!fixture.admin.query_one("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='atomic_program_reference_state' AND column_name='walker_version')", &[]).unwrap().get::<_, bool>(0));
        assert_eq!(fixture.references(1), refs);
        assert_eq!(
            PostgresStore::connect(&fixture.connection)
                .err()
                .unwrap()
                .code,
            "postgres/schema-upgrade-required"
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
        .batch_execute("UPDATE atomic_program_reference_state SET walker_version=2")
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
fn paused_headless_restore_keeps_prestaged_program_roots_through_upgrade_and_gc() {
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
fn authenticated_partial_generation_gc_can_resume_after_reference_upgrade() {
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
    assert_eq!(fixture.marker().2, 1);
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
    panic!("bounded collection should finish after upgrade");
}
