use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, ErrorCategory, Keyword, Peer,
    PersistentTreeManifest, PortableBackup, PostgresIndexer, PostgresMigrator, PostgresStore,
    PostgresTreeStore, RestoreFault, Schema, TransactionRequest, TxOp, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const ATTRIBUTE: u32 = 1000;

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

struct Fixture {
    directory: tempfile::TempDir,
    expected: Database,
    request: TransactionRequest,
    entity: u64,
}

impl Fixture {
    fn isolated_connection() -> Option<String> {
        let connection = std::env::var("ATOMIC_POSTGRES_URL").ok()?;
        let schema_name = unique("restore_publication");
        Client::connect(&connection, NoTls)
            .unwrap()
            .batch_execute(&format!("CREATE SCHEMA {schema_name}"))
            .unwrap();
        let connection =
            if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
                let separator = if connection.contains('?') { '&' } else { '?' };
                format!("{connection}{separator}options=-csearch_path%3D{schema_name}")
            } else {
                format!("{connection} options='-c search_path={schema_name}'")
            };
        PostgresMigrator::connect(&connection)
            .unwrap()
            .migrate()
            .unwrap();
        Some(connection)
    }

    fn new() -> Option<Self> {
        let connection = Self::isolated_connection()?;
        let source = unique("source");
        let mut schema = Schema::new();
        let mut attribute = Attribute::new(
            ATTRIBUTE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        );
        attribute.indexed = true;
        schema.install(attribute).unwrap();
        let mut store = PostgresStore::connect(&connection).unwrap();
        let created = store.create_database(&source, schema).unwrap();
        let service = common::start_service(&connection, &source);
        let seed = common::transact(
            &service,
            "seed",
            created.basis_t(),
            &(0..160)
                .map(|value| TxOp::Add {
                    entity: EntityRef::Temp(format!("item-{value}")),
                    attribute: ATTRIBUTE,
                    value: Value::Long(value).into(),
                })
                .collect::<Vec<_>>(),
            1000,
        );
        let entity = seed.tempids["item-0"];
        let request = TransactionRequest::new(
            "acknowledged-update",
            vec![TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: ATTRIBUTE,
                value: Value::Long(999).into(),
            }],
        )
        .comparing_basis(seed.basis_t)
        .with_tx_instant(2000);
        let committed = service
            .client()
            .transact(request.clone(), Duration::from_secs(30))
            .unwrap();
        service.shutdown();
        PostgresIndexer::connect(&connection, &source)
            .unwrap()
            .with_segment_datoms(1)
            .unwrap()
            .consolidate()
            .unwrap();
        let expected = store.recover(&source).unwrap();
        assert_eq!(expected.basis_t(), committed.basis_t);
        let directory = tempfile::tempdir().unwrap();
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700))
                .unwrap();
        }
        PortableBackup::connect(&connection)
            .unwrap()
            .backup_database(&source, directory.path())
            .unwrap();
        Some(Self {
            directory,
            expected,
            request,
            entity,
        })
    }

    fn restore(
        &self,
        connection: &str,
        target: &str,
        fault: RestoreFault,
    ) -> Result<Database, atomic_core::SemanticError> {
        PortableBackup::connect(connection)
            .unwrap()
            .restore_backup_with_fault(
                self.directory.path(),
                self.expected.basis_t(),
                target,
                fault,
            )
    }

    fn assert_native_and_retry(&self, connection: &str, target: &str) {
        let peer = Peer::connect_with_cache_limits(connection, target, 0, 0).unwrap();
        let value = peer.database_value();
        common::assert_same_information(&value, &self.expected);
        assert_eq!(
            value.values(self.entity, ATTRIBUTE).unwrap(),
            vec![Value::Long(999)]
        );
        assert_eq!(peer.load_stats().compatibility_materializations, 0);
        let writer = common::start_service(connection, target);
        let replay = writer
            .client()
            .transact(self.request.clone(), Duration::from_secs(30))
            .unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.basis_t, self.expected.basis_t());
        assert_eq!(
            replay.db_before.values(self.entity, ATTRIBUTE).unwrap(),
            vec![Value::Long(0)]
        );
        assert_eq!(
            replay.db_after.values(self.entity, ATTRIBUTE).unwrap(),
            vec![Value::Long(999)]
        );
        writer.shutdown();
        assert_eq!(
            Peer::connect(connection, target, 0).unwrap().basis_t(),
            self.expected.basis_t()
        );
    }

    fn assert_complete(&self, connection: &str, target: &str) -> (i64, Vec<u8>) {
        let mut sql = Client::connect(connection, NoTls).unwrap();
        let row = sql
            .query_one(
                "SELECT p.publication_revision, p.manifest_hash, l.complete, l.problem_code \
                 FROM atomic_tree_publications p JOIN atomic_tree_live_sets l \
                   ON l.database_id=p.database_id AND l.manifest_hash=p.manifest_hash \
                 WHERE p.database_id=$1 ORDER BY p.publication_revision DESC LIMIT 1",
                &[&target],
            )
            .unwrap();
        let revision: i64 = row.get(0);
        let hash: Vec<u8> = row.get(1);
        assert!(row.get::<_, bool>(2));
        assert_eq!(row.get::<_, Option<String>>(3), None);
        let pending: i64 = sql
            .query_one(
                "SELECT count(*) FROM atomic_tree_delta_headers WHERE manifest_hash=$1",
                &[&hash],
            )
            .unwrap()
            .get(0);
        assert_eq!(pending, 0);
        let mut tree = PostgresTreeStore::connect(connection).unwrap();
        let record = tree
            .load_manifest(target, revision as u64)
            .unwrap()
            .unwrap();
        let manifest = PersistentTreeManifest::decode(&record.payload).unwrap();
        let mut expected = BTreeSet::new();
        for root in &manifest.trees {
            let validated =
                atomic_core::persistent_tree::validate_tree_streaming(&root.descriptor, |hash| {
                    Ok(tree.load_node(*hash)?.expect("restored node exists"))
                })
                .unwrap();
            expected.extend(validated.node_hashes);
        }
        assert!(
            expected.len() > 512,
            "fixture must require multiple fold batches"
        );
        let actual = sql
            .query(
                "SELECT node_hash FROM atomic_tree_live_nodes WHERE database_id=$1",
                &[&target],
            )
            .unwrap()
            .into_iter()
            .map(|row| <[u8; 32]>::try_from(row.get::<_, Vec<u8>>(0)).unwrap())
            .collect::<BTreeSet<_>>();
        assert_eq!(actual, expected);
        (revision, hash)
    }

    fn assert_partial(&self, connection: &str, target: &str) {
        let row = Client::connect(connection, NoTls)
            .unwrap()
            .query_one(
                "SELECT h.expected_node_count, \
                        (SELECT count(*) FROM atomic_tree_live_nodes WHERE database_id=$1), \
                        (SELECT count(*) FROM atomic_tree_delta_nodes WHERE manifest_hash=h.manifest_hash) \
                 FROM atomic_tree_publications p JOIN atomic_tree_delta_headers h USING (manifest_hash) \
                 WHERE p.database_id=$1 AND h.delta_state=2",
                &[&target],
            )
            .unwrap();
        let expected: i64 = row.get(0);
        let live: i64 = row.get(1);
        let pending: i64 = row.get(2);
        assert!(expected > 512);
        assert_eq!(live, 512);
        assert_eq!(pending, expected - live);
    }
}

#[test]
fn fresh_and_idempotent_restore_finish_every_selected_publication_batch() {
    let Some(fixture) = Fixture::new() else {
        return;
    };
    let target = unique("fresh_target");
    let connection = Fixture::isolated_connection().unwrap();
    let restored = fixture
        .restore(&connection, &target, RestoreFault::None)
        .unwrap();
    common::assert_same_information(&restored, &fixture.expected);
    let publication = fixture.assert_complete(&connection, &target);
    fixture
        .restore(&connection, &target, RestoreFault::None)
        .unwrap();
    assert_eq!(fixture.assert_complete(&connection, &target), publication);
    fixture.assert_native_and_retry(&connection, &target);
}

#[test]
fn interrupted_fold_and_owner_error_propagate_then_resume_the_same_root() {
    let Some(fixture) = Fixture::new() else {
        return;
    };
    let target = unique("interrupted_target");
    let connection = Fixture::isolated_connection().unwrap();
    let error = fixture
        .restore(&connection, &target, RestoreFault::AfterTreePublication)
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Interrupted);
    assert_eq!(error.code, "backup/restore-after-tree-publication");
    fixture.assert_partial(&connection, &target);
    // The published value remains complete and readable while metadata folds.
    let old = Peer::connect_with_cache_limits(&connection, &target, 0, 0).unwrap();
    common::assert_same_information(&old.database_value(), &fixture.expected);
    // A forged non-published header is not equivalent to consumed work.
    let mut sql = Client::connect(&connection, NoTls).unwrap();
    common::with_replica_triggers_disabled(&mut sql, |client| {
        client.execute("UPDATE atomic_tree_delta_headers SET delta_state=1", &[])
    })
    .unwrap();
    assert_eq!(
        fixture
            .restore(&connection, &target, RestoreFault::None)
            .unwrap_err()
            .code,
        "backup/restore-tree-work-state"
    );
    common::with_replica_triggers_disabled(&mut sql, |client| {
        client.execute("UPDATE atomic_tree_delta_headers SET delta_state=2", &[])
    })
    .unwrap();
    fixture
        .restore(&connection, &target, RestoreFault::None)
        .unwrap();
    let complete = fixture.assert_complete(&connection, &target);
    assert_eq!(
        complete.0, 1,
        "resume must not manufacture another physical root"
    );
    fixture.assert_native_and_retry(&connection, &target);

    let failing_target = unique("owner_error_target");
    let failing_connection = Fixture::isolated_connection().unwrap();
    let mut sql = Client::connect(&failing_connection, NoTls).unwrap();
    // Fail the second bounded owner transaction, after the first 512 nodes
    // committed. This scoped fixture trigger never targets another database.
    sql.batch_execute(&format!(
        "CREATE FUNCTION reject_fixture_second_fold() RETURNS trigger LANGUAGE plpgsql AS $$ \
         BEGIN IF NEW.database_id='{failing_target}' AND \
           (SELECT count(*) FROM atomic_tree_live_nodes WHERE database_id=NEW.database_id)>=512 \
         THEN RAISE EXCEPTION 'fixture bounded-fold interruption' USING ERRCODE='40001'; \
         END IF; RETURN NEW; END $$; \
         CREATE TRIGGER reject_fixture_second_fold BEFORE INSERT ON atomic_tree_live_nodes \
         FOR EACH ROW EXECUTE FUNCTION reject_fixture_second_fold();"
    ))
    .unwrap();
    let error = fixture
        .restore(&failing_connection, &failing_target, RestoreFault::None)
        .unwrap_err();
    assert_eq!(error.code, "tree/publication-work");
    fixture.assert_partial(&failing_connection, &failing_target);
    sql.batch_execute("DROP TRIGGER reject_fixture_second_fold ON atomic_tree_live_nodes; DROP FUNCTION reject_fixture_second_fold();").unwrap();
    fixture
        .restore(&failing_connection, &failing_target, RestoreFault::None)
        .unwrap();
    assert_eq!(
        fixture
            .assert_complete(&failing_connection, &failing_target)
            .0,
        1
    );
    fixture.assert_native_and_retry(&failing_connection, &failing_target);
}
