use atomic_core::{
    Attribute, Cardinality, DB_EXCISE, EntityRef, Keyword, PortableBackup, PostgresIndexer,
    PostgresMigrator, PostgresOperator, PostgresStore, RECOMMENDED_GARBAGE_COLLECTION_AGE, Schema,
    TxOp, TxValue, USER_PARTITION, Value, ValueType, make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::fs;
use std::thread;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

mod common;

const ITEM_VALUE: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_VALUE,
            Keyword::new("item", "value"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add(value: &str) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(user(42)),
        attribute: ITEM_VALUE,
        value: TxValue::Scalar(Value::String(value.into())),
    }
}

fn named_connection(connection: &str, application_name: &str) -> String {
    if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
        let separator = if connection.contains('?') { '&' } else { '?' };
        format!("{connection}{separator}application_name={application_name}")
    } else {
        format!("{connection} application_name={application_name}")
    }
}

#[test]
fn inspection_is_one_repeatable_read_snapshot_across_recovery_and_tree_metrics() {
    let Some(connection) = connection() else {
        return;
    };
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let database_id = unique("inspect_snapshot");
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let old = common::transact(
        &service,
        "old-snapshot-value",
        created.basis_t(),
        &[add("old")],
        1_000,
    );
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();
    let baseline = PostgresOperator::connect(&connection)
        .unwrap()
        .inspect_database(&database_id, true)
        .unwrap();
    assert!(baseline.healthy(), "{:?}", baseline.problems);

    // Inspection reaches this relation only after reading the head, complete
    // log, and both derived-root families. Holding it lets the test publish a
    // successor while the inspector is demonstrably mid-report.
    let mut blocker_client = Client::connect(&connection, NoTls).unwrap();
    let mut blocker = blocker_client.transaction().unwrap();
    blocker
        .batch_execute("LOCK TABLE atomic_programs IN ACCESS EXCLUSIVE MODE")
        .unwrap();

    let application_name = unique("atomic_inspector");
    let inspection_connection = named_connection(&connection, &application_name);
    let inspected_database = database_id.clone();
    let inspection = thread::spawn(move || {
        PostgresOperator::connect(&inspection_connection)
            .unwrap()
            .inspect_database(&inspected_database, true)
    });

    let mut observer = Client::connect(&connection, NoTls).unwrap();
    let deadline = Instant::now() + Duration::from_secs(15);
    let observed_mid_report = loop {
        let waiting: bool = observer
            .query_one(
                "SELECT EXISTS (SELECT 1 FROM pg_stat_activity \
                 WHERE application_name = $1 AND wait_event_type = 'Lock')",
                &[&application_name],
            )
            .unwrap()
            .get(0);
        if waiting {
            break true;
        }
        if Instant::now() >= deadline {
            break false;
        }
        thread::sleep(Duration::from_millis(10));
    };

    let new = common::transact(
        &service,
        "new-snapshot-value",
        old.basis_t,
        &[add("new")],
        2_000,
    );
    indexer.consolidate().unwrap();
    blocker.commit().unwrap();
    let report = inspection.join().unwrap().unwrap();
    service.shutdown();

    assert!(
        observed_mid_report,
        "inspector never reached the controlled lock"
    );
    assert!(report.healthy(), "{:?}", report.problems);
    assert_eq!(report.metrics.basis_t, baseline.metrics.basis_t);
    assert_eq!(report.metrics.transactions, baseline.metrics.transactions);
    assert_eq!(report.metrics.requests, baseline.metrics.requests);
    assert_eq!(
        report.metrics.current_datoms,
        baseline.metrics.current_datoms
    );
    assert_eq!(
        report.metrics.history_datoms,
        baseline.metrics.history_datoms
    );
    assert_eq!(
        report.metrics.tree_publication_revision,
        baseline.metrics.tree_publication_revision
    );
    assert_eq!(
        report.metrics.tree_publications,
        baseline.metrics.tree_publications
    );
    assert_eq!(report.metrics.index_lag, baseline.metrics.index_lag);
    assert_eq!(report.metrics.basis_t, old.basis_t);
    assert!(new.basis_t > report.metrics.basis_t);

    let successor = PostgresOperator::connect(&connection)
        .unwrap()
        .inspect_database(&database_id, true)
        .unwrap();
    assert!(successor.healthy(), "{:?}", successor.problems);
    assert_eq!(successor.metrics.basis_t, new.basis_t);
    assert!(successor.metrics.tree_publication_revision > report.metrics.tree_publication_revision);
    assert!(successor.metrics.history_datoms > report.metrics.history_datoms);
}

#[test]
fn corrupt_tree_in_a_is_scoped_and_makes_shared_reclamation_conservative() {
    let Some(connection) = connection() else {
        return;
    };
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let database_a = unique("corrupt_tree_a");
    let database_b = unique("maintain_b");
    let backup_directory = std::env::temp_dir().join(unique("atomic_scope_backup"));
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created_a = store.create_database(&database_a, schema()).unwrap();
    let created_b = store.create_database(&database_b, schema()).unwrap();
    let service_a = common::start_service(&connection, &database_a);
    let service_b = common::start_service(&connection, &database_b);
    let committed_a = common::transact(
        &service_a,
        "derived-a",
        created_a.basis_t(),
        &[add("a")],
        1_000,
    );
    let committed_b = common::transact(
        &service_b,
        "logical-b",
        created_b.basis_t(),
        &[add("forget-b")],
        1_000,
    );
    let requested_b = common::transact(
        &service_b,
        "ordinary-a15-scope-request",
        committed_b.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("scope-excision".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(u64::from(ITEM_VALUE))),
        }],
        2_000,
    );
    service_a.shutdown();
    service_b.shutdown();
    PostgresIndexer::connect(&connection, &database_a)
        .unwrap()
        .consolidate()
        .unwrap();

    let mut backup = PortableBackup::connect(&connection).unwrap();
    backup
        .backup_database(&database_b, &backup_directory)
        .unwrap();
    PortableBackup::verify_backup(&backup_directory, requested_b.basis_t, true).unwrap();

    let mut raw = Client::connect(&connection, NoTls).unwrap();
    let manifest_row = raw
        .query_one(
            "SELECT m.manifest_hash, m.payload FROM atomic_tree_publications p \
             JOIN atomic_tree_manifests m ON m.manifest_hash = p.manifest_hash \
             WHERE p.database_id = $1 ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_a],
        )
        .unwrap();
    let manifest_hash: Vec<u8> = manifest_row.get(0);
    let original_manifest: Vec<u8> = manifest_row.get(1);
    common::with_replica_triggers_disabled(&mut raw, |client| {
        client.execute(
            "UPDATE atomic_tree_manifests \
             SET payload = set_byte(payload, 0, get_byte(payload, 0) # 1) \
             WHERE manifest_hash = $1",
            &[&manifest_hash],
        )?;
        Ok(())
    })
    .unwrap();

    // This orphan proves the database-scoped logical rewrite does not smuggle
    // a global reclamation pass into the same failure boundary.
    let orphan_payload = vec![0x5a; 64];
    let orphan_hash = sha256(&orphan_payload);
    raw.execute(
        "INSERT INTO atomic_index_segments (segment_hash, payload) VALUES ($1, $2) \
         ON CONFLICT DO NOTHING",
        &[&&orphan_hash[..], &&orphan_payload[..]],
    )
    .unwrap();

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let report_b = operator.inspect_database(&database_b, true);
    let report_a = operator.inspect_database(&database_a, true);
    let garbage = operator.garbage_inventory(RECOMMENDED_GARBAGE_COLLECTION_AGE);
    let excision = operator.process_excision_requests(&database_b);
    let orphan_survived: i64 = raw
        .query_one(
            "SELECT count(*) FROM atomic_index_segments WHERE segment_hash = $1",
            &[&&orphan_hash[..]],
        )
        .unwrap()
        .get(0);

    // Restore all injected damage before asserting so one failed assertion
    // cannot poison unrelated live-PostgreSQL fixtures.
    common::with_replica_triggers_disabled(&mut raw, |client| {
        client.execute(
            "UPDATE atomic_tree_manifests SET payload = $1 WHERE manifest_hash = $2",
            &[&&original_manifest[..], &manifest_hash],
        )?;
        client.execute(
            "DELETE FROM atomic_index_segments WHERE segment_hash = $1",
            &[&&orphan_hash[..]],
        )?;
        Ok(())
    })
    .unwrap();
    fs::remove_dir_all(&backup_directory).unwrap();

    let report_b = report_b.unwrap();
    assert!(report_b.healthy(), "{:?}", report_b.problems);
    assert!(report_b.metrics.shared_reachability_uncertain);
    assert_eq!(report_b.metrics.orphan_segments, 0);
    assert_eq!(report_b.metrics.orphan_tree_nodes, 0);
    let report_a = report_a.unwrap();
    assert!(!report_a.healthy());
    assert!(
        report_a
            .problems
            .iter()
            .any(|problem| problem.code == "tree/manifest-magic"),
        "target inspection did not report its unreadable manifest header: {:?}",
        report_a.problems
    );
    let garbage = garbage.unwrap();
    assert!(
        garbage
            .tree_publications
            .iter()
            .all(|candidate| candidate.database_id != database_a),
        "a corrupt live root must never become a reclamation candidate"
    );
    let excision = excision.unwrap();
    assert_eq!(excision.database_id, database_b);
    assert!(excision.removed_datoms > 0);
    assert_eq!(orphan_survived, 1);
    assert_eq!(committed_a.basis_t, report_a.metrics.basis_t);
}
