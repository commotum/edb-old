//! Derived walker-1 evidence is not sufficient for native query literals.
//! Fault injection is confined to a disposable catalog. This is a marker/DDL
//! upgrade witness, not a claim that the current writer is a historical binary.
mod common;
#[path = "common/schema34_downgrade.rs"]
mod schema34_downgrade;

use atomic_core::*;
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::time::{Duration, Instant};

struct Fixture {
    scope: common::PostgresFixture,
    sql: Client,
    roots: Vec<Digest>,
    leaves: Vec<Digest>,
    unused: Digest,
}

fn query_program(term: Term) -> Program {
    Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::Query(
                QueryTemplate::native(
                    Query::new(
                        FindSpec::Relation(vec![FindElement::Variable("v".into())]),
                        vec![Clause::Function {
                            function: Function::Ground,
                            source: "$".into(),
                            args: vec![term],
                            binding: Binding::Scalar("v".into()),
                        }],
                    ),
                    vec![],
                    vec![],
                )
                .unwrap(),
            ),
            Instruction::Pop,
            Instruction::PushConstant(Value::Long(0)),
            Instruction::EmitRow(1),
            Instruction::Return,
        ],
    }
}

fn leaf(value: i64) -> Program {
    Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(value)),
            Instruction::EmitRow(1),
            Instruction::Return,
        ],
    }
}

impl Fixture {
    fn new() -> Option<Self> {
        let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
            eprintln!("SKIP actual PostgreSQL: ATOMIC_POSTGRES_URL unset");
            return None;
        };
        let scope = common::PostgresFixture::new(&url, "query_reference_walker");
        PostgresMigrator::connect(&scope.connection)
            .unwrap()
            .migrate()
            .unwrap();
        let mut store = PostgresStore::connect(&scope.connection).unwrap();
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1000,
                Keyword::new("app", "programs"),
                ValueType::Function,
                Cardinality::Many,
            ))
            .unwrap();
        drop(store.create_database("query-reference", schema).unwrap());
        let leaves = vec![
            store.deploy_program_blob(&leaf(41)).unwrap(),
            store.deploy_program_blob(&leaf(42)).unwrap(),
        ];
        let unused = store.deploy_program_blob(&leaf(999)).unwrap();
        let programs = [
            query_program(Term::Constant(Value::Function(leaves[0]))),
            query_program(Term::QueryConstant(QueryValue::Tagged(
                Symbol::new("app", "inert"),
                Box::new(QueryValue::Map(vec![(
                    QueryValue::Nil,
                    QueryValue::Set(vec![QueryValue::Scalar(Value::Function(leaves[1]))]),
                )])),
            ))),
        ];
        assert_eq!(
            &encode_program(&programs[0]).unwrap()[16..18],
            &7u16.to_be_bytes()
        );
        assert_eq!(
            &encode_program(&programs[1]).unwrap()[16..18],
            &10u16.to_be_bytes()
        );
        let roots = programs
            .iter()
            .map(|program| store.deploy_program_blob(program).unwrap())
            .collect::<Vec<_>>();
        drop(store);
        let service = common::start_service(&scope.connection, "query-reference");
        let report = service
            .client()
            .transact(
                TransactionRequest::new(
                    "retain-query-programs",
                    roots
                        .iter()
                        .map(|hash| TxOp::Add {
                            entity: EntityRef::Temp("owner".into()),
                            attribute: 1000,
                            value: Value::Function(*hash).into(),
                        })
                        .collect(),
                )
                .with_tx_instant(1000),
                Duration::from_secs(30),
            )
            .unwrap();
        drop(report);
        service.shutdown();
        let mut sql = Client::connect(&scope.connection, NoTls).unwrap();
        sql.batch_execute("UPDATE atomic_program_gc_candidates SET candidate_at=clock_timestamp()-interval '40 days'").unwrap();
        Some(Self {
            scope,
            sql,
            roots,
            leaves,
            unused,
        })
    }

    fn references(&mut self) -> BTreeSet<Vec<u8>> {
        self.sql.query("SELECT program_hash FROM atomic_program_generation_refs WHERE database_id='query-reference' AND log_generation=1", &[]).unwrap().into_iter().map(|row| row.get(0)).collect()
    }

    fn expected(&self) -> BTreeSet<Vec<u8>> {
        self.roots
            .iter()
            .chain(&self.leaves)
            .map(|hash| hash.to_vec())
            .collect()
    }

    fn make_obsolete(&mut self) {
        let mut tx = self.sql.transaction().unwrap();
        tx.batch_execute("SET LOCAL session_replication_role=replica")
            .unwrap();
        for hash in &self.leaves {
            tx.execute(
                "DELETE FROM atomic_program_generation_refs WHERE program_hash=$1",
                &[&&hash[..]],
            )
            .unwrap();
        }
        tx.batch_execute("UPDATE atomic_program_reference_state SET complete=true,problem_code=NULL,walker_version=1").unwrap();
        tx.commit().unwrap();
    }

    fn canonical_bytes(&mut self) -> Vec<(Vec<u8>, Vec<u8>)> {
        self.sql.query("SELECT content_hash,payload FROM atomic_transaction_contents ORDER BY content_hash", &[]).unwrap().into_iter().map(|row| (row.get(0), row.get(1))).collect()
    }
}

#[test]
fn walker_two_migration_rebuilds_native_literal_closure_and_sql_fails_closed() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let started = Instant::now();
    assert_eq!(fixture.references(), fixture.expected());
    let bytes = fixture.canonical_bytes();
    fixture.make_obsolete();
    // SQL32 rejects even an incorrectly optimistic complete walker1 marker.
    assert!(
        fixture
            .sql
            .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
            .unwrap()
            .is_empty()
    );
    let old_sql = include_str!("../migrations/0024_versioned_program_references.sql");
    let old_gc = &old_sql[old_sql
        .find("CREATE OR REPLACE FUNCTION atomic_collect_program_garbage(")
        .unwrap()..];
    let mut tx = fixture.sql.transaction().unwrap();
    schema34_downgrade::remove_migration_34(&mut tx);
    tx.batch_execute(old_gc).unwrap();
    tx.batch_execute("DELETE FROM atomic_schema_migrations WHERE version>=32")
        .unwrap();
    tx.commit().unwrap();
    assert_eq!(
        PostgresStore::connect(&fixture.scope.connection)
            .err()
            .unwrap()
            .code,
        "postgres/schema-upgrade-required"
    );
    // Inspect actual additive DDL inside a transaction, then roll it back;
    // the real migrator below must still execute the full31→32 upgrade.
    let mut tx = fixture.sql.transaction().unwrap();
    tx.batch_execute(include_str!(
        "../migrations/0032_query_program_references.sql"
    ))
    .unwrap();
    let row = tx.query_one("SELECT complete,problem_code,walker_version FROM atomic_program_reference_state WHERE singleton", &[]).unwrap();
    assert!(!row.get::<_, bool>(0));
    assert_eq!(
        row.get::<_, Option<String>>(1).as_deref(),
        Some("program/obsolete-reference-walker")
    );
    assert_eq!(row.get::<_, i64>(2), 1);
    assert!(
        tx.query("SELECT atomic_collect_program_garbage(0,4096)", &[])
            .unwrap()
            .is_empty()
    );
    tx.rollback().unwrap();
    PostgresMigrator::connect(&fixture.scope.connection)
        .unwrap()
        .migrate()
        .unwrap();
    schema34_downgrade::assert_restored(&mut fixture.sql);
    assert_eq!(fixture.references(), fixture.expected());
    assert_eq!(fixture.canonical_bytes(), bytes);
    let row = fixture.sql.query_one("SELECT complete,problem_code,walker_version,updated_at::text FROM atomic_program_reference_state WHERE singleton", &[]).unwrap();
    assert!(row.get::<_, bool>(0));
    assert_eq!(row.get::<_, Option<String>>(1), None);
    assert_eq!(row.get::<_, i64>(2), 2);
    let updated: String = row.get(3);
    PostgresMigrator::connect(&fixture.scope.connection)
        .unwrap()
        .migrate()
        .unwrap();
    assert_eq!(
        fixture
            .sql
            .query_one(
                "SELECT updated_at::text FROM atomic_program_reference_state WHERE singleton",
                &[]
            )
            .unwrap()
            .get::<_, String>(0),
        updated
    );
    let collected = fixture
        .sql
        .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, Vec<u8>>(0))
        .collect::<Vec<_>>();
    assert_eq!(collected, vec![fixture.unused.to_vec()]);
    assert_eq!(fixture.references(), fixture.expected());
    let mut store = PostgresStore::connect(&fixture.scope.connection).unwrap();
    for hash in fixture.roots.iter().chain(&fixture.leaves) {
        assert_eq!(
            program_hash(&store.resolve_program(*hash).unwrap()).unwrap(),
            *hash
        );
    }
    eprintln!(
        "isolated SQL31→32 + walker1→2 authenticated rebuild, literal reachability and GC completed in {:?}",
        started.elapsed()
    );
}

#[test]
fn corrupt_literal_dependency_cannot_publish_walker_two_completeness() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    fixture.make_obsolete();
    let hash = fixture.leaves[1];
    let original: Vec<u8> = fixture
        .sql
        .query_one(
            "SELECT payload FROM atomic_programs WHERE program_hash=$1",
            &[&&hash[..]],
        )
        .unwrap()
        .get(0);
    let mut tx = fixture.sql.transaction().unwrap();
    tx.batch_execute("SET LOCAL session_replication_role=replica")
        .unwrap();
    let mut corrupt = original.clone();
    corrupt[0] ^= 1;
    tx.execute(
        "UPDATE atomic_programs SET payload=$2 WHERE program_hash=$1",
        &[&&hash[..], &corrupt],
    )
    .unwrap();
    tx.commit().unwrap();
    let error = PostgresMigrator::connect(&fixture.scope.connection)
        .unwrap()
        .migrate()
        .unwrap_err();
    assert_eq!(error.code, "postgres/program-ref-hash");
    assert_eq!(
        fixture
            .sql
            .query_one(
                "SELECT walker_version FROM atomic_program_reference_state WHERE singleton",
                &[]
            )
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    assert!(
        fixture
            .sql
            .query("SELECT atomic_collect_program_garbage(0,4096)", &[])
            .unwrap()
            .is_empty()
    );
    let mut tx = fixture.sql.transaction().unwrap();
    tx.batch_execute("SET LOCAL session_replication_role=replica")
        .unwrap();
    tx.execute(
        "UPDATE atomic_programs SET payload=$2 WHERE program_hash=$1",
        &[&&hash[..], &original],
    )
    .unwrap();
    tx.commit().unwrap();
    PostgresMigrator::connect(&fixture.scope.connection)
        .unwrap()
        .migrate()
        .unwrap();
    assert_eq!(fixture.references(), fixture.expected());
}
