//! Missing/damaged physical indexes must not prevent a canonical backup.
mod common;
use atomic_core::*;
use postgres::{Client, NoTls};
use std::io::Write;
use std::path::PathBuf;
use std::process::{Command, Stdio};
use std::time::{Instant, SystemTime, UNIX_EPOCH};

const SCORE: u32 = 1000;
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
}
fn new_fixture() -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP backup fallback: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, "backup_capture_fallback");
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    Some(fixture)
}
fn directory(label: &str) -> PathBuf {
    std::env::temp_dir().join(format!(
        "atomic_backup_fallback_{label}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    ))
}
fn publications(sql: &mut Client, database: &str) -> i64 {
    sql.query_one(
        "SELECT count(*) FROM atomic_tree_publications WHERE database_id=$1",
        &[&database],
    )
    .unwrap()
    .get(0)
}
fn information(db: &DatabaseValue) -> Vec<Vec<Datom>> {
    [false, true]
        .into_iter()
        .flat_map(|history| {
            let db = if history {
                db.clone().history()
            } else {
                db.clone()
            };
            [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ]
            .into_iter()
            .map(move |order| db.datoms(order).unwrap())
        })
        .collect()
}

#[test]
fn stock_create_then_backup_and_offline_query_need_no_service_or_consolidation() {
    let Some(fixture) = new_fixture() else {
        return;
    };
    let directory = directory("stock");
    for args in [
        vec!["create", "--database", "stock"],
        vec![
            "backup",
            "--database",
            "stock",
            "--repository",
            directory.to_str().unwrap(),
        ],
    ] {
        let result = Command::new(env!("CARGO_BIN_EXE_atomic"))
            .args(args)
            .env("ATOMIC_POSTGRES_URL", &fixture.connection)
            .env("ATOMIC_POSTGRES_TRANSPORT", "plaintext")
            .output()
            .unwrap();
        assert!(
            result.status.success(),
            "{}",
            String::from_utf8_lossy(&result.stderr)
        );
    }
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    let identity = DatabaseCatalog::connect(&fixture.connection)
        .unwrap()
        .resolve("stock")
        .unwrap();
    assert_eq!(publications(&mut sql, &identity.database_id), 0);
    let point = PortableBackup::list_backup_points(&directory)
        .unwrap()
        .pop()
        .unwrap();
    assert_eq!(point.basis_t, 0);
    let mut query = Command::new(env!("CARGO_BIN_EXE_atomic"))
        .args(["query", "--repository"])
        .arg(&directory)
        .args(["--file", "-"])
        .env_remove("ATOMIC_POSTGRES_URL")
        .env_remove("ATOMIC_POSTGRES_TRANSPORT")
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
        .unwrap();
    query
        .stdin
        .take()
        .unwrap()
        .write_all(b"[:find ?ident :where [_ :db/ident ?ident]]")
        .unwrap();
    let result = query.wait_with_output().unwrap();
    assert!(
        result.status.success(),
        "{}",
        String::from_utf8_lossy(&result.stderr)
    );
    assert!(String::from_utf8_lossy(&result.stdout).contains(":db/ident"));
    PortableBackup::verify_backup_point(&directory, point.basis_t, point.log_generation, true)
        .unwrap();
    std::fs::remove_dir_all(directory).unwrap();
}

#[test]
fn create_then_backup_without_starting_service_supports_genesis_and_application_schema() {
    let Some(fixture) = new_fixture() else {
        return;
    };
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    let mut store = PostgresStore::connect(&fixture.connection).unwrap();
    for (name, schema, expected_basis) in [("genesis", Schema::new(), 0), ("schema", schema(), 1)] {
        // Use the same public idempotent catalog route as stock `atomic create`.
        let entry = DatabaseCatalog::connect(&fixture.connection)
            .unwrap()
            .create_if_absent(name, schema)
            .unwrap();
        assert!(entry.created);
        assert_eq!(publications(&mut sql, &entry.database.database_id), 0);
        let expected = store.recover(&entry.database.database_id).unwrap();
        assert_eq!(expected.basis_t(), expected_basis);
        let directory = directory(name);
        let started = Instant::now();
        let point = PortableBackup::connect(&fixture.connection)
            .unwrap()
            .backup_database(name, &directory)
            .unwrap();
        assert_eq!(
            publications(&mut sql, &entry.database.database_id),
            0,
            "capture must not repair or write source indexes"
        );
        let offline = BackupConnection::open_point(&directory, &point).unwrap();
        assert_eq!(
            information(&offline.db()),
            information(&expected.database_value())
        );
        assert_eq!(
            offline.log().tx_range(None, None).unwrap().count(),
            expected_basis as usize
        );
        // Allocation metadata supports a subsequent speculative schema install
        // without a transactor, including the special basis-zero checkpoint.
        let next = if expected_basis == 0 {
            SCORE
        } else {
            SCORE + 1
        };
        let speculative = offline
            .db()
            .with(
                &[TxOp::InstallAttribute(Attribute::new(
                    next,
                    Keyword::new("item", "next"),
                    ValueType::Long,
                    Cardinality::One,
                ))],
                1000,
            )
            .unwrap();
        assert_eq!(
            speculative.db_after.entid(&Keyword::new("item", "next")),
            Some(u64::from(next))
        );
        let verified = PortableBackup::verify_backup_point(
            &directory,
            point.basis_t,
            point.log_generation,
            true,
        )
        .unwrap();
        assert_eq!(
            information(&verified.database.database_value()),
            information(&expected.database_value())
        );
        drop((offline, speculative, verified));
        eprintln!(
            "backup fallback {name}: complete capture/open/read/speculate/deep-verify/drop {:?}",
            started.elapsed()
        );
        std::fs::remove_dir_all(directory).unwrap();
    }
}

#[test]
fn nonempty_missing_publication_and_damaged_data_leaf_recover_only_at_capture() {
    let Some(fixture) = new_fixture() else {
        return;
    };
    let mut store = PostgresStore::connect(&fixture.connection).unwrap();
    let created = store.create_database("data", schema()).unwrap();
    let service = common::start_service(&fixture.connection, "data");
    let ops = (0..256)
        .map(|i| TxOp::Add {
            entity: EntityRef::Temp(format!("row{i}")),
            attribute: SCORE,
            value: Value::Long(i).into(),
        })
        .collect::<Vec<_>>();
    let report = common::transact(&service, "seed", created.basis_t(), &ops, 1000);
    let target = report.tempids["row128"];
    let expected = information(&report.db_after);
    let basis = report.basis_t;
    service.shutdown();
    drop(report);
    let mut indexer = PostgresIndexer::connect(&fixture.connection, "data")
        .unwrap()
        .with_segment_datoms(32)
        .unwrap();
    indexer.consolidate().unwrap();
    drop(indexer);
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    let source_count = publications(&mut sql, "data");
    assert!(source_count > 0);
    let mut bad = None;
    for row in sql
        .query("SELECT node_hash,payload FROM atomic_tree_nodes", &[])
        .unwrap()
    {
        let hash: Vec<u8> = row.get(0);
        let bytes: Vec<u8> = row.get(1);
        let hash: Digest = hash.try_into().unwrap();
        if let Ok(persistent_tree::TreeNode::Leaf(leaf)) =
            persistent_tree::decode_tree_node(&hash, &bytes)
            && leaf.order == IndexOrder::Eavt
            && !leaf.history
            && leaf.len() <= 32
            && (0..leaf.len()).any(|i| leaf.datom(i).unwrap().entity == target)
        {
            bad = Some((hash, bytes));
            break;
        }
    }
    let (hash, bytes) = bad.expect("published user-data leaf");
    common::with_replica_triggers_disabled(&mut sql,|sql|sql.execute(
        "UPDATE atomic_tree_nodes SET payload=set_byte(payload,16,get_byte(payload,16)#1) WHERE node_hash=$1",&[&&hash[..]])).unwrap();
    let damaged_dir = directory("damaged");
    let damaged = PortableBackup::connect(&fixture.connection)
        .unwrap()
        .backup_database("data", &damaged_dir)
        .unwrap();
    assert_eq!(publications(&mut sql, "data"), source_count);
    let retained: Vec<u8> = sql
        .query_one(
            "SELECT payload FROM atomic_tree_nodes WHERE node_hash=$1",
            &[&&hash[..]],
        )
        .unwrap()
        .get(0);
    assert_ne!(
        retained, bytes,
        "backup must not overwrite the damaged source"
    );
    assert_eq!(
        information(
            &BackupConnection::open_point(&damaged_dir, &damaged)
                .unwrap()
                .db()
        ),
        expected
    );
    PortableBackup::verify_backup_point(&damaged_dir, basis, damaged.log_generation, true).unwrap();
    common::with_replica_triggers_disabled(&mut sql, |sql| {
        sql.execute(
            "UPDATE atomic_tree_nodes SET payload=$2 WHERE node_hash=$1",
            &[&&hash[..], &&bytes[..]],
        )
    })
    .unwrap();
    // Normal restore retains exact receipt bases as completed archives, not
    // normal publication variants. Losing the normal index publication then
    // leaves all receipt metadata valid: this isolates index loss from broken
    // canonical receipt references.
    let restored_fixture = new_fixture().unwrap();
    PortableBackup::connect(&restored_fixture.connection)
        .unwrap()
        .restore_backup_point(&damaged_dir, basis, damaged.log_generation, "data")
        .unwrap();
    let mut sql = Client::connect(&restored_fixture.connection, NoTls).unwrap();
    assert!(publications(&mut sql, "data") > 0);
    common::with_replica_triggers_disabled(&mut sql, |sql| {
        sql.execute(
            "DELETE FROM atomic_tree_publications WHERE database_id='data'",
            &[],
        )
    })
    .unwrap();
    assert_eq!(publications(&mut sql, "data"), 0);
    let missing_dir = directory("missing");
    let missing = PortableBackup::connect(&restored_fixture.connection)
        .unwrap()
        .backup_database("data", &missing_dir)
        .unwrap();
    assert_eq!(publications(&mut sql, "data"), 0);
    assert_eq!(
        information(
            &BackupConnection::open_point(&missing_dir, &missing)
                .unwrap()
                .db()
        ),
        expected
    );
    PortableBackup::verify_backup_point(&missing_dir, basis, missing.log_generation, true).unwrap();
    assert_eq!(store.recover("data").unwrap().basis_t(), basis);
    // Canonical corruption still fails closed and publishes no backup point.
    common::with_replica_triggers_disabled(&mut sql,|sql|sql.execute(
        "UPDATE atomic_transaction_contents SET payload=set_byte(payload,16,get_byte(payload,16)#1) WHERE lineage_id=(SELECT lineage_id FROM atomic_databases WHERE database_id='data') AND basis_t=$1",&[&(basis as i64)])).unwrap();
    let rejected_dir = directory("canonical_corrupt");
    assert!(
        PortableBackup::connect(&restored_fixture.connection)
            .unwrap()
            .backup_database("data", &rejected_dir)
            .is_err()
    );
    assert!(
        PortableBackup::list_backup_points(&rejected_dir)
            .unwrap()
            .is_empty()
    );
    for directory in [damaged_dir, missing_dir, rejected_dir] {
        std::fs::remove_dir_all(directory).unwrap();
    }
}
