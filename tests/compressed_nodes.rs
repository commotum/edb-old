use atomic_core::{
    Attribute, BackgroundIndexingConfig, Cardinality, Connection, EntityRef, IndexOrder, Keyword,
    PortableBackup, PostgresMigrator, PostgresStore, PostgresTreeStore, Schema, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp, Value, ValueType, sha256,
};
use postgres::{Client, NoTls};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

struct Fixture {
    admin: Client,
    schema: String,
    connection: String,
}

impl Fixture {
    fn new(connection: &str) -> Self {
        let schema = format!(
            "block_archive_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let mut admin = Client::connect(connection, NoTls).unwrap();
        admin
            .batch_execute(&format!("CREATE SCHEMA {schema}"))
            .unwrap();
        let connection =
            if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
                format!(
                    "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
                    if connection.contains('?') { "&" } else { "?" }
                )
            } else {
                format!("{connection} options='-csearch_path={schema},pg_catalog'")
            };
        let fixture = Self {
            admin,
            schema,
            connection,
        };
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap();
        fixture
    }
}

impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}

#[test]
fn compression_keeps_publication_and_backup_canonical_and_restores_without_sidecars() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED compressed node publication/backup check: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let fixture = Fixture::new(&connection);
    let connection = &fixture.connection;
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("document", "body"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    PostgresStore::connect(connection)
        .unwrap()
        .create_database("source", schema)
        .unwrap();
    let service = TransactionService::start_with_indexing(
        TransactionServiceConfig {
            connection: connection.clone(),
            database_id: "source".into(),
            holder_id: "block-backup".into(),
            lease_duration: Duration::from_secs(5),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 8,
            capacity_limits: Default::default(),
        },
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let peer = Connection::attach(connection, service.client(), 8).unwrap();
    let report = peer
        .transact(
            TransactionRequest::new(
                "documents",
                (0..12)
                    .map(|index| TxOp::Add {
                        entity: EntityRef::Temp(format!("document-{index}")),
                        attribute: 1_000,
                        value: Value::String("compressible document body ".repeat(160)).into(),
                    })
                    .collect(),
            ),
            Duration::from_secs(15),
        )
        .unwrap();
    let index = peer.request_index().unwrap();
    peer.sync_index(index.target_t, Duration::from_secs(15))
        .unwrap();
    let expected = report
        .db_after
        .clone()
        .history()
        .collect_datoms(IndexOrder::Eavt)
        .unwrap();
    service.shutdown();
    let mut sql = Client::connect(connection, NoTls).unwrap();
    let projected: i64 = sql
        .query_one("SELECT count(*) FROM atomic_tree_node_blocks", &[])
        .unwrap()
        .get(0);
    assert!(
        projected > 0,
        "normal tree uploads did not generate optional compressed representations"
    );
    let row = sql.query_one(
        "SELECT n.node_hash,n.payload,b.physical_payload FROM atomic_tree_live_nodes live \
         JOIN atomic_tree_nodes n USING(node_hash) JOIN atomic_tree_node_blocks b USING(node_hash) \
         WHERE live.database_id=$1 AND get_byte(n.payload,6)=1 ORDER BY octet_length(n.payload) DESC LIMIT 1",
        &[&"source"],
    ).unwrap();
    let leaf_hash: [u8; 32] = row.get::<_, Vec<u8>>(0).try_into().unwrap();
    let canonical_leaf: Vec<u8> = row.get(1);
    let physical_leaf: Vec<u8> = row.get(2);
    assert_eq!(sha256(&canonical_leaf), leaf_hash);
    assert!(physical_leaf.len() < canonical_leaf.len());
    let mut trees = PostgresTreeStore::connect(connection).unwrap();
    assert_eq!(trees.load_node(leaf_hash).unwrap().unwrap(), canonical_leaf);
    let revision = trees.current_publication_revision("source").unwrap();
    let manifest = trees.load_manifest("source", revision).unwrap().unwrap();
    assert_eq!(sha256(&manifest.payload), manifest.manifest_hash);
    assert_eq!(manifest.tx_hash, report.tx_hash);
    for root in &manifest.roots {
        let raw: Vec<u8> = sql
            .query_one(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash=$1",
                &[&&root.root_hash[..]],
            )
            .unwrap()
            .get(0);
        assert_eq!(
            raw.len() as u64,
            root.encoded_bytes,
            "publication length changed to physical compressed bytes"
        );
        assert_eq!(sha256(&raw), root.root_hash);
    }

    // Optional sidecars are intentionally absent from the backup source.
    sql.execute("DELETE FROM atomic_tree_node_blocks", &[])
        .unwrap();
    let reopened = Connection::connect(connection, "source", 8).unwrap();
    assert_eq!(
        reopened
            .db()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    let directory = tempfile::tempdir().unwrap();
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    }
    let mut backup = PortableBackup::connect(connection).unwrap();
    let point = backup.backup_database("source", directory.path()).unwrap();
    let name: String = leaf_hash.iter().map(|byte| format!("{byte:02x}")).collect();
    let backup_leaf = std::fs::read(directory.path().join("objects").join(name)).unwrap();
    assert_eq!(
        backup_leaf, canonical_leaf,
        "backup replaced canonical content with a compressed envelope"
    );
    PortableBackup::verify_backup(directory.path(), point.basis_t, true).unwrap();
    // A lineage has one catalog name; restore into a separate empty catalog.
    let target = Fixture::new(connection);
    let mut target_backup = PortableBackup::connect(&target.connection).unwrap();
    target_backup
        .restore_backup(directory.path(), point.basis_t, "restored")
        .unwrap();
    let mut target_sql = Client::connect(&target.connection, NoTls).unwrap();
    let regenerated: i64 = target_sql
        .query_one("SELECT count(*) FROM atomic_tree_node_blocks", &[])
        .unwrap()
        .get(0);
    assert!(
        regenerated > 0,
        "restoring canonical nodes did not regenerate their optional representations"
    );
    let restored = Connection::connect(&target.connection, "restored", 8).unwrap();
    assert_eq!(
        restored
            .db()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    let unchanged = trees.load_manifest("source", revision).unwrap().unwrap();
    assert_eq!(unchanged.manifest_hash, manifest.manifest_hash);
    assert_eq!(unchanged.payload, manifest.payload);
    let canonical_after: Vec<u8> = sql
        .query_one(
            "SELECT payload FROM atomic_tree_nodes WHERE node_hash=$1",
            &[&&leaf_hash[..]],
        )
        .unwrap()
        .get(0);
    assert_eq!(canonical_after, canonical_leaf);
    println!(
        "COMPRESSED_BACKUP_OK raw_leaf_bytes={} physical_leaf_bytes={} extra_stored_payload_bytes={} canonical_publication_preserved=true absent_projection_reopen=true canonical_backup=true regenerated_projections={regenerated}",
        canonical_leaf.len(),
        physical_leaf.len(),
        physical_leaf.len()
    );
}
