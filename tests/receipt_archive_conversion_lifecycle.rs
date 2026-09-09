//! Real PostgreSQL lifecycle boundaries of ordinary exact-receipt conversion.
//! Each case owns separate SQL schemas; no existing catalog is upgraded/dropped.
use atomic_core::{
    Attribute, Cardinality, DB_EXCISE, DatabaseValue, Digest, EntityRef, GarbageInventory,
    IndexOrder, Keyword, Peer, PortableBackup, PostgresIndexer, PostgresMigrator, PostgresOperator,
    PostgresStore, PostgresTreeStore, Schema, TransactionRequest, TxOp, TxValue, USER_PARTITION,
    Value, ValueType, make_eid,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

mod common;

const VALUE: u32 = 1000;
const WAIT: Duration = Duration::from_secs(30);
const MAX_STEPS: usize = 256;

fn gc(operator: &mut PostgresOperator, preview: bool) -> GarbageInventory {
    // Installation-scoped fixtures share PostgreSQL advisory-lock space with
    // concurrent tests. Retry only this exact admission Busy, never corruption,
    // preview divergence or a failed ownership handoff.
    let started = Instant::now();
    loop {
        let result = if preview {
            operator.garbage_inventory(Duration::ZERO)
        } else {
            operator.collect_garbage(Duration::ZERO)
        };
        match result {
            Err(error)
                if error.code == "operations/semantic-gc-pinned"
                    && started.elapsed() < Duration::from_secs(5) =>
            {
                std::thread::sleep(Duration::from_millis(5));
            }
            result => return result.unwrap(),
        }
    }
}

fn unique(label: &str) -> String {
    format!(
        "{label}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn isolated(base: &str, label: &str) -> String {
    let schema = unique(label);
    Client::connect(base, NoTls)
        .unwrap()
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let connection = if base.trim_start().starts_with("postgres://")
        || base.trim_start().starts_with("postgresql://")
    {
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
    connection
}

fn entity(index: u64) -> u64 {
    make_eid(USER_PARTITION, index).unwrap()
}

fn add(index: u64, value: i64) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(entity(index)),
        attribute: VALUE,
        value: Value::Long(value).into(),
    }
}

fn excise(index: u64) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp("excise".into()),
        attribute: DB_EXCISE as u32,
        value: TxValue::Entity(EntityRef::Id(entity(index))),
    }
}

fn fold(connection: &str, database: &str) {
    let rows = Client::connect(connection, NoTls).unwrap().query(
        "SELECT p.manifest_hash FROM atomic_tree_publications p JOIN atomic_tree_delta_headers h USING(manifest_hash) WHERE p.database_id=$1 AND h.delta_state=2 ORDER BY p.publication_revision",
        &[&database],
    ).unwrap();
    let mut trees = PostgresTreeStore::connect(connection).unwrap();
    for row in rows {
        let hash: Digest = row.get::<_, Vec<u8>>(0).try_into().unwrap();
        let complete = (0..MAX_STEPS).any(|_| trees.advance_publication_work(hash).unwrap());
        assert!(
            complete,
            "fixture publication did not finish its bounded fold"
        );
    }
}

struct Fixture {
    base: String,
    connection: String,
    database: String,
    request: TransactionRequest,
    basis: u64,
    generation: u64,
    manifest: Digest,
    revision: u64,
}

impl Fixture {
    fn new(excision_generation: bool) -> Option<Self> {
        let Ok(base) = std::env::var("ATOMIC_POSTGRES_URL") else {
            eprintln!("SKIP receipt lifecycle: ATOMIC_POSTGRES_URL is unset");
            return None;
        };
        let connection = isolated(&base, "receipt_lifecycle");
        let database = unique("receipt_lifecycle");
        let mut schema = Schema::new();
        let mut attribute = Attribute::new(
            VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        );
        attribute.indexed = true;
        schema.install(attribute).unwrap();
        PostgresStore::connect(&connection)
            .unwrap()
            .create_database(&database, schema)
            .unwrap();
        PostgresIndexer::connect(&connection, &database)
            .unwrap()
            .consolidate()
            .unwrap();
        let writer = common::start_service(&connection, &database);
        drop(
            writer
                .client()
                .transact(
                    TransactionRequest::new("seed", vec![add(42, 10), add(43, 99)]),
                    WAIT,
                )
                .unwrap(),
        );
        writer.shutdown();
        if excision_generation {
            let writer = common::start_service(&connection, &database);
            drop(
                writer
                    .client()
                    .transact(
                        TransactionRequest::new("first-excision", vec![excise(43)]),
                        WAIT,
                    )
                    .unwrap(),
            );
            writer.shutdown();
            let receipt = PostgresOperator::connect(&connection)
                .unwrap()
                .process_excision_requests(&database)
                .unwrap();
            assert_eq!(receipt.generation, 2);
        }
        PostgresIndexer::connect(&connection, &database)
            .unwrap()
            .consolidate()
            .unwrap();
        let request = TransactionRequest::new("exact-update", vec![add(42, 20)]);
        let writer = common::start_service(&connection, &database);
        let report = writer.client().transact(request.clone(), WAIT).unwrap();
        assert_eq!(
            report.db_before.values(entity(42), VALUE).unwrap(),
            vec![Value::Long(10)]
        );
        assert_eq!(
            report.db_after.values(entity(42), VALUE).unwrap(),
            vec![Value::Long(20)]
        );
        let basis = report.basis_t;
        drop(report);
        writer.shutdown();
        PostgresIndexer::connect(&connection, &database)
            .unwrap()
            .consolidate()
            .unwrap();
        fold(&connection, &database);
        let row = Client::connect(&connection, NoTls).unwrap().query_one(
            "SELECT base.generation,base.base_manifest_hash,p.publication_revision FROM atomic_generation_request_bases base JOIN atomic_generation_requests request USING(database_id,generation,request_key_hash) JOIN atomic_tree_publications p ON p.manifest_hash=base.base_manifest_hash WHERE base.database_id=$1 AND request.basis_t=$2",
            &[&database, &(basis as i64)],
        ).unwrap();
        let generation = row.get::<_, i64>(0) as u64;
        assert_eq!(generation, if excision_generation { 2 } else { 1 });
        Some(Self {
            base,
            connection,
            database,
            request,
            basis,
            generation,
            manifest: row.get::<_, Vec<u8>>(1).try_into().unwrap(),
            revision: row.get::<_, i64>(2) as u64,
        })
    }

    fn stage(&self) {
        let mut operator = PostgresOperator::connect(&self.connection).unwrap();
        for _ in 0..MAX_STEPS {
            let applied = gc(&mut operator, false);
            if applied
                .receipt_archive_conversions
                .iter()
                .any(|item| item.manifest_hash == self.manifest && item.phase == 0)
            {
                let row = Client::connect(&self.connection, NoTls).unwrap().query_one(
                    "SELECT work.phase,EXISTS(SELECT 1 FROM atomic_tree_publications WHERE manifest_hash=work.manifest_hash),EXISTS(SELECT 1 FROM atomic_request_base_archive_completions WHERE manifest_hash=work.manifest_hash) FROM atomic_receipt_archive_conversions work WHERE manifest_hash=$1",
                    &[&&self.manifest[..]],
                ).unwrap();
                assert_eq!(row.get::<_, i16>(0), 1);
                assert!(row.get::<_, bool>(1));
                assert!(!row.get::<_, bool>(2));
                return;
            }
        }
        panic!("exact receipt source never entered bounded conversion");
    }

    fn finish(&self) {
        // Each invocation reopens the operator, resuming durable progress rather
        // than relying on an in-process closure or hash accumulator.
        let mut sql = Client::connect(&self.connection, NoTls).unwrap();
        for _ in 0..MAX_STEPS {
            gc(
                &mut PostgresOperator::connect(&self.connection).unwrap(),
                false,
            );
            let row = sql.query_one(
                "SELECT EXISTS(SELECT 1 FROM atomic_tree_publications WHERE manifest_hash=$1),EXISTS(SELECT 1 FROM atomic_request_base_archive_completions WHERE manifest_hash=$1),EXISTS(SELECT 1 FROM atomic_receipt_archive_conversions WHERE manifest_hash=$1)",
                &[&&self.manifest[..]],
            ).unwrap();
            if !row.get::<_, bool>(0) {
                assert!(
                    row.get::<_, bool>(1),
                    "publication vanished before archive completed"
                );
                assert!(!row.get::<_, bool>(2));
                return;
            }
        }
        panic!("receipt archive ownership handoff did not finish");
    }

    fn verify_retry(&self, connection: &str, database: &str) {
        let writer = common::start_service(connection, database);
        let report = writer
            .client()
            .transact(self.request.clone(), WAIT)
            .unwrap();
        assert!(report.replayed);
        assert_eq!(report.basis_t, self.basis);
        assert_eq!(
            report.db_before.values(entity(42), VALUE).unwrap(),
            vec![Value::Long(10)]
        );
        assert_eq!(
            report.db_after.values(entity(42), VALUE).unwrap(),
            vec![Value::Long(20)]
        );
        assert_eq!(report.db_before.basis_t() + 1, report.db_after.basis_t());
        assert_eq!(
            Peer::connect(connection, database, 0)
                .unwrap()
                .database_value()
                .basis_t(),
            self.basis
        );
        drop(report);
        writer.shutdown();
    }
}

fn assert_value(value: &DatabaseValue, expected: Option<i64>) {
    assert_eq!(
        value.values(entity(42), VALUE).unwrap(),
        expected.into_iter().map(Value::Long).collect::<Vec<_>>()
    );
}

#[test]
fn staged_conversion_is_backup_safe_and_restores_exact_retry() {
    let Some(fixture) = Fixture::new(false) else {
        return;
    };
    fixture.stage();
    fixture.verify_retry(&fixture.connection, &fixture.database);
    let mut operator = PostgresOperator::connect(&fixture.connection).unwrap();
    let inspected = operator.inspect_database(&fixture.database, true).unwrap();
    assert!(inspected.healthy(), "{:?}", inspected.problems);
    let directory = tempfile::tempdir().unwrap();
    let repository = directory.path().join("private-backup");
    let mut backup = PortableBackup::connect(&fixture.connection).unwrap();
    let point = backup
        .backup_database(&fixture.database, &repository)
        .unwrap();
    let verified = PortableBackup::verify_backup(&repository, point.basis_t, true).unwrap();
    assert_eq!(verified.database.basis_t(), fixture.basis);
    let target_connection = isolated(&fixture.base, "receipt_lifecycle_restore");
    let target = unique("receipt_restored");
    let restored = PortableBackup::connect(&target_connection)
        .unwrap()
        .restore_backup_point(&repository, point.basis_t, point.log_generation, &target)
        .unwrap();
    common::assert_same_information(&verified.database, &restored);
    fixture.verify_retry(&target_connection, &target);
    fixture.finish();
    let repeated = backup
        .backup_database(&fixture.database, &repository)
        .unwrap();
    assert_eq!(
        repeated.manifest_hash, point.manifest_hash,
        "ownership-only conversion changed portable information"
    );
    assert_eq!(repeated.objects_written, 0);
    fixture.verify_retry(&fixture.connection, &fixture.database);
    let inspected = operator.inspect_database(&fixture.database, true).unwrap();
    assert!(inspected.healthy(), "{:?}", inspected.problems);
}

#[test]
fn conversion_resumes_after_excision_and_releases_retired_kind_one_generation() {
    let Some(fixture) = Fixture::new(true) else {
        return;
    };
    fixture.stage();
    let old_peer =
        Peer::connect_with_cache_limits(&fixture.connection, &fixture.database, 0, 0).unwrap();
    let old = old_peer.database_value();
    assert_value(&old, Some(20));
    let history = old.clone().history().datoms(IndexOrder::Eavt).unwrap();
    let writer = common::start_service(&fixture.connection, &fixture.database);
    drop(
        writer
            .client()
            .transact(
                TransactionRequest::new("second-excision", vec![excise(42)]),
                WAIT,
            )
            .unwrap(),
    );
    writer.shutdown();
    let mut operator = PostgresOperator::connect(&fixture.connection).unwrap();
    let excision = operator
        .process_excision_requests(&fixture.database)
        .unwrap();
    assert_eq!(excision.source_generation, fixture.generation);
    assert_eq!(excision.generation, fixture.generation + 1);
    fold(&fixture.connection, &fixture.database);
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    assert_eq!(
        sql.query_one(
            "SELECT build_kind FROM atomic_log_generations WHERE database_id=$1 AND generation=$2",
            &[&fixture.database, &(fixture.generation as i64)]
        )
        .unwrap()
        .get::<_, i16>(0),
        1
    );
    let retired: bool = sql
        .query_one(
            "SELECT atomic_collect_tree_retirement($1,$2,$3,0,512)",
            &[
                &fixture.database,
                &(fixture.revision as i64),
                &&fixture.manifest[..],
            ],
        )
        .unwrap()
        .get(0);
    assert!(
        !retired,
        "ordinary retirement bypassed an interrupted conversion"
    );
    let archive_error = sql
        .query_one(
            "SELECT * FROM atomic_collect_request_base_archive($1,$2,$3,0,512)",
            &[
                &fixture.database,
                &(fixture.generation as i64),
                &&fixture.manifest[..],
            ],
        )
        .unwrap_err();
    assert_eq!(archive_error.code().map(|code| code.code()), Some("55006"));
    let inventory = gc(&mut operator, true);
    assert!(
        !inventory
            .request_base_archives
            .iter()
            .any(|archive| archive.manifest_hash == fixture.manifest)
    );
    assert!(
        !inventory
            .log_generations
            .iter()
            .any(|generation| generation.database_id == fixture.database
                && generation.generation == fixture.generation)
    );
    fixture.finish();
    assert_value(&old, Some(20));
    assert_eq!(
        old.clone().history().datoms(IndexOrder::Eavt).unwrap(),
        history
    );
    let current_peer =
        Peer::connect_with_cache_limits(&fixture.connection, &fixture.database, 0, 0).unwrap();
    let current = current_peer.database_value();
    assert_value(&current, None);
    drop(old);
    drop(old_peer);
    let mut removed = false;
    for _ in 0..MAX_STEPS {
        gc(&mut operator, false);
        let exists: bool = sql.query_one("SELECT EXISTS(SELECT 1 FROM atomic_log_generations WHERE database_id=$1 AND generation=$2)", &[&fixture.database, &(fixture.generation as i64)]).unwrap().get(0);
        if !exists {
            removed = true;
            break;
        }
    }
    assert!(
        removed,
        "retired excision generation remained blocked after conversion and pin release"
    );
    assert!(
        !sql.query_one(
            "SELECT EXISTS(SELECT 1 FROM atomic_request_base_archives WHERE manifest_hash=$1)",
            &[&&fixture.manifest[..]]
        )
        .unwrap()
        .get::<_, bool>(0)
    );
    assert_value(&current, None);
    let inspected = operator.inspect_database(&fixture.database, true).unwrap();
    assert!(inspected.healthy(), "{:?}", inspected.problems);
    assert_eq!(current_peer.load_stats().compatibility_materializations, 0);
}
