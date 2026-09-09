//! Semantic frontier discovery is a separate broad phase, not work repeated
//! for every bounded tree/receipt bookkeeping step. No live semantic content
//! or permission check is weakened to make the phase probe pass.
use atomic_core::{
    Attribute, Cardinality, EntityRef, ErrorCategory, GarbageInventory, Instruction, Keyword,
    PostgresConnectionConfig, PostgresIndexer, PostgresIoPolicy, PostgresMigrator,
    PostgresOperator, PostgresStore, Program, ProgramKind, Schema, TransactionRequest, TxOp, Value,
    ValueType, sha256,
};
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

mod common;

struct Fixture {
    connection: String,
    schema: String,
    sql: Client,
}

impl Fixture {
    fn new() -> Option<Self> {
        let Ok(base) = std::env::var("ATOMIC_POSTGRES_URL") else {
            eprintln!("SKIP semantic GC phases: ATOMIC_POSTGRES_URL is unset");
            return None;
        };
        let schema = format!(
            "semantic_gc_phases_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        Client::connect(&base, NoTls)
            .unwrap()
            .batch_execute(&format!("CREATE SCHEMA {schema}"))
            .unwrap();
        let connection = if base.starts_with("postgres://") || base.starts_with("postgresql://") {
            format!(
                "{base}{}options=-csearch_path%3D{schema}",
                if base.contains('?') { '&' } else { '?' }
            )
        } else {
            format!("{base} options='-c search_path={schema}'")
        };
        PostgresMigrator::connect(&connection)
            .unwrap()
            .migrate()
            .unwrap();
        Some(Self {
            sql: Client::connect(&connection, NoTls).unwrap(),
            connection,
            schema,
        })
    }

    fn operator(&self) -> PostgresOperator {
        let config = PostgresConnectionConfig::plaintext(&self.connection)
            .with_io_policy(PostgresIoPolicy {
                statement_timeout: Some(Duration::from_secs(5)),
                lock_timeout: Some(Duration::from_millis(100)),
                ..PostgresIoPolicy::default()
            })
            .unwrap();
        PostgresOperator::connect_configured(&config).unwrap()
    }

    fn orphans(&mut self) -> Vec<Vec<u8>> {
        // Synthetic unreferenced values, matching the existing semantic-GC
        // fixture: no authoritative coordinate or parent names these bytes.
        (0..2)
            .map(|ordinal| {
                let payload = format!("{}-orphan-{ordinal}", self.schema).into_bytes();
                let hash = sha256(&payload).to_vec();
                self.sql
                    .execute(
                        "INSERT INTO atomic_semantic_commitment_nodes \
                         (node_hash,payload,subtree_count) VALUES ($1,$2,1)",
                        &[&hash, &payload],
                    )
                    .unwrap();
                hash
            })
            .collect()
    }

    fn orphan_count(&mut self, hashes: &[Vec<u8>]) -> i64 {
        self.sql
            .query_one(
                "SELECT count(*) FROM atomic_semantic_commitment_nodes WHERE node_hash=ANY($1)",
                &[&hashes],
            )
            .unwrap()
            .get(0)
    }
}

impl Drop for Fixture {
    fn drop(&mut self) {
        // Only this fixture's generated schema, never an existing installation.
        let _ = self
            .sql
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}

fn earlier_work(inventory: &GarbageInventory) -> bool {
    !inventory.segment_hashes.is_empty()
        || !inventory.program_hashes.is_empty()
        || !inventory.tree_publications.is_empty()
        || !inventory.tree_build_intents.is_empty()
        || !inventory.tree_manifest_hashes.is_empty()
        || !inventory.tree_node_hashes.is_empty()
        || !inventory.request_base_archives.is_empty()
        || !inventory.receipt_archive_conversions.is_empty()
        || !inventory.log_generations.is_empty()
        || !inventory.semantic_commitment_roots.is_empty()
}

fn exact_apply(operator: &mut PostgresOperator, mut preview: GarbageInventory) {
    preview.applied = true;
    assert_eq!(operator.collect_garbage(Duration::ZERO).unwrap(), preview);
}

#[test]
fn published_intent_progress_does_not_scan_the_unrelated_semantic_catalog() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let database = "intent-phase";
    let mut store = PostgresStore::connect(&fixture.connection).unwrap();
    let expected = store.create_database(database, Schema::new()).unwrap();
    PostgresIndexer::connect(&fixture.connection, database)
        .unwrap()
        .consolidate()
        .unwrap();
    assert!(
        fixture
            .sql
            .query_one(
                "SELECT count(*) FROM atomic_tree_build_intents WHERE intent_state=2",
                &[]
            )
            .unwrap()
            .get::<_, i64>(0)
            > 0,
        "the probe requires a real published upload ledger"
    );
    let orphans = fixture.orphans();
    let mut operator = fixture.operator();

    // This published intent's only task is to drop its exact upload ledger.
    // It does not legitimately authenticate a semantic node. An exclusive
    // relation lock therefore detects both an accidental global preview scan
    // and an unnecessary empty-frontier collector call, deterministically.
    let mut blocker = Client::connect(&fixture.connection, NoTls).unwrap();
    let mut blocked = blocker.transaction().unwrap();
    blocked
        .batch_execute("LOCK TABLE atomic_semantic_commitment_nodes IN ACCESS EXCLUSIVE MODE")
        .unwrap();
    let preview = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(!preview.tree_build_intents.is_empty());
    assert!(preview.semantic_commitment_node_hashes.is_empty());
    exact_apply(&mut operator, preview);

    // Once its earlier work is complete, the same lock must be encountered:
    // the repair defers the semantic sweep; it does not silently remove it.
    let started = Instant::now();
    let error = operator.garbage_inventory(Duration::ZERO).unwrap_err();
    blocked.rollback().unwrap();
    assert_eq!(error.category, ErrorCategory::Busy);
    assert_eq!(error.code, "operations/gc-semantic-node-candidates");
    eprintln!(
        "semantic_gc_phase final_sweep_lock_probe={:?}",
        started.elapsed()
    );

    let preview = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(!earlier_work(&preview));
    assert_eq!(preview.semantic_commitment_node_hashes.len(), orphans.len());
    exact_apply(&mut operator, preview);
    assert_eq!(fixture.orphan_count(&orphans), 0);
    let final_preview = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(!earlier_work(&final_preview));
    assert!(final_preview.semantic_commitment_node_hashes.is_empty());
    common::assert_same_information(&expected, &store.recover(database).unwrap());
}

#[test]
fn receipt_conversion_and_program_work_precede_exact_semantic_orphan_collection() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let database = "receipt-phases";
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        1000,
        Keyword::new("phase", "value"),
        ValueType::Long,
        Cardinality::One,
    );
    attribute.indexed = true;
    schema.install(attribute).unwrap();
    let mut store = PostgresStore::connect(&fixture.connection).unwrap();
    store.create_database(database, schema).unwrap();
    store
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![Instruction::Return],
        })
        .unwrap();
    for ordinal in 0..3 {
        let writer = common::start_service(&fixture.connection, database);
        drop(
            writer
                .client()
                .transact(
                    TransactionRequest::new(
                        format!("write-{ordinal}"),
                        vec![TxOp::Add {
                            entity: EntityRef::Temp(format!("item-{ordinal}")),
                            attribute: 1000,
                            value: Value::Long(ordinal).into(),
                        }],
                    ),
                    Duration::from_secs(30),
                )
                .unwrap(),
        );
        writer.shutdown();
        PostgresIndexer::connect(&fixture.connection, database)
            .unwrap()
            .consolidate()
            .unwrap();
    }
    let expected = store.recover(database).unwrap();
    let bindings_before: i64 = fixture
        .sql
        .query_one("SELECT count(*) FROM atomic_generation_request_bases", &[])
        .unwrap()
        .get(0);
    assert!(bindings_before > 0);
    let orphans = fixture.orphans();
    let mut operator = fixture.operator();
    let mut conversion_phases = BTreeSet::new();
    let mut program_steps = 0;
    let mut intent_steps = 0;
    let mut semantic_steps = 0;
    let mut quiescent = false;
    for _ in 0..128 {
        let preview = operator.garbage_inventory(Duration::ZERO).unwrap();
        if !earlier_work(&preview) && preview.semantic_commitment_node_hashes.is_empty() {
            quiescent = true;
            break;
        }
        if earlier_work(&preview) {
            assert!(preview.semantic_commitment_node_hashes.is_empty());
            assert_eq!(fixture.orphan_count(&orphans), orphans.len() as i64);
        } else {
            semantic_steps += 1;
        }
        program_steps += usize::from(!preview.program_hashes.is_empty());
        intent_steps += usize::from(!preview.tree_build_intents.is_empty());
        for conversion in &preview.receipt_archive_conversions {
            conversion_phases.insert(conversion.phase);
        }
        exact_apply(&mut operator, preview);
    }
    assert!(
        quiescent,
        "bounded fixture did not finish all eligible phases"
    );
    assert!(program_steps > 0);
    assert!(intent_steps > 0);
    assert_eq!(conversion_phases, BTreeSet::from([0, 1, 2, 3, 4]));
    assert!(semantic_steps > 0);
    assert_eq!(fixture.orphan_count(&orphans), 0);
    assert_eq!(
        fixture
            .sql
            .query_one("SELECT count(*) FROM atomic_generation_request_bases", &[])
            .unwrap()
            .get::<_, i64>(0),
        bindings_before
    );
    common::assert_same_information(&expected, &store.recover(database).unwrap());
    eprintln!(
        "semantic_gc_phases conversion={conversion_phases:?} intent_steps={intent_steps} program_steps={program_steps} semantic_steps={semantic_steps} bindings_preserved={bindings_before}"
    );
}
