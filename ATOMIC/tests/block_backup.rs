//! Current block backup acceptance; every PostgreSQL target is an isolated schema.
mod common;
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::time::{Duration, Instant};

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block backup: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let f = common::PostgresFixture::new(&url, label);
    PgBlockStore::install(&PostgresConnectionConfig::plaintext(&f.connection)).unwrap();
    Some(f)
}
fn schema() -> Schema {
    let mut schema = Schema::new();
    let mut number = Attribute::new(
        1000,
        Keyword::new("item", "number"),
        ValueType::Long,
        Cardinality::One,
    );
    number.indexed = true;
    schema.install(number).unwrap();
    schema
        .install(
            Attribute::new(
                1001,
                Keyword::new("item", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    schema
}
fn objects(url: &str) -> i64 {
    postgres::Client::connect(url, postgres::NoTls)
        .unwrap()
        .query_one("SELECT count(*) FROM atomic_objects", &[])
        .unwrap()
        .get(0)
}
#[test]
fn capture_is_differential_file_only_and_offline_reads_share_current_semantics() {
    let Some(f) = fixture("block_backup") else {
        return;
    };
    let config = PostgresConnectionConfig::plaintext(&f.connection);
    BlockDatabase::create(&config, "source", schema()).unwrap();
    let writer = common::start_service(&f.connection, "source");
    let ops = (0..512)
        .flat_map(|n| {
            [
                TxOp::Add {
                    entity: EntityRef::Temp(format!("e{n}")),
                    attribute: 1000,
                    value: Value::Long(n).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp(format!("e{n}")),
                    attribute: 1001,
                    value: Value::String(format!("amber item {n}")).into(),
                },
            ]
        })
        .collect();
    let first = writer
        .client()
        .transact(
            TransactionRequest::new("seed", ops).with_tx_instant(1000),
            Duration::from_secs(60),
        )
        .unwrap();
    let target = first.tempids["e256"];
    writer.shutdown();
    let directory = common::private_directory();
    let mut backup = PortableBackup::connect(&f.connection).unwrap();
    let prior_objects = objects(&f.connection);
    let started = Instant::now();
    let context = OperationContext::new(OperationKind::Transaction);
    let guard = context.enter();
    let point = backup.backup_database("source", directory.path()).unwrap();
    drop(guard);
    let capture_elapsed = started.elapsed();
    assert_eq!(
        objects(&f.connection),
        prior_objects,
        "Backup must not write source objects to prepare its read index"
    );
    let offline = BackupConnection::open_configured(
        directory.path(),
        Some(&point),
        BackupReadConfig {
            cache_entries: 16,
            cache_bytes: 16 * 1024,
        },
    )
    .unwrap();
    assert_eq!(offline.db().basis_t(), first.basis_t);
    assert_eq!(
        offline.db().values(target, 1000).unwrap(),
        vec![Value::Long(256)]
    );
    assert_eq!(offline.log().basis_t(), first.basis_t);
    assert!(
        offline.db().snapshot_reference().is_err(),
        "File values must not manufacture live storage authority"
    );
    let before = offline.db();
    let rerun = backup.backup_database("source", directory.path()).unwrap();
    assert_eq!(rerun.manifest_hash, point.manifest_hash);
    assert_eq!(rerun.objects_written, 0);
    assert!(rerun.objects_reused > 0);
    let deep_started = Instant::now();
    let verified = PortableBackup::verify_backup_point(
        directory.path(),
        point.basis_t,
        point.log_generation,
        true,
    )
    .unwrap();
    assert_eq!(verified.database.basis_t(), point.basis_t);
    assert_eq!(before.values(target, 1000).unwrap(), vec![Value::Long(256)]);
    eprintln!(
        "BLOCK_BACKUP capture_ms={} verify_ms={} objects_written={} objects_reused={} offline_read_stats={:?} source_objects_unchanged=true diagnostics={:?}",
        capture_elapsed.as_millis(),
        deep_started.elapsed().as_millis(),
        point.objects_written,
        rerun.objects_reused,
        offline.read_stats(),
        context.snapshot()
    );
}

#[test]
fn capture_faults_never_publish_partial_points_and_retry_resolves_published_outcome() {
    let Some(f) = fixture("block_backup_faults") else {
        return;
    };
    BlockDatabase::create(
        &PostgresConnectionConfig::plaintext(&f.connection),
        "empty",
        Schema::new(),
    )
    .unwrap();
    let mut backup = PortableBackup::connect(&f.connection).unwrap();
    for fault in [
        BackupFault::AfterPublicationCaptured,
        BackupFault::AfterFirstObjectStaged,
        BackupFault::AfterObjects,
        BackupFault::AfterManifestStaged,
        BackupFault::AfterManifestPublished,
    ] {
        let directory = common::private_directory();
        assert!(
            backup
                .backup_database_with_fault("empty", directory.path(), fault)
                .is_err()
        );
        let points = PortableBackup::list_backup_points(directory.path()).unwrap();
        assert_eq!(
            points.len(),
            usize::from(fault == BackupFault::AfterManifestPublished)
        );
        let point = backup.backup_database("empty", directory.path()).unwrap();
        assert_eq!(point.basis_t, 0);
        let verified = PortableBackup::verify_backup(directory.path(), 0, true).unwrap();
        assert_eq!(verified.database.basis_t(), 0);
        assert_eq!(
            BackupConnection::open(directory.path())
                .unwrap()
                .db()
                .basis_t(),
            0
        );
    }
}

#[test]
fn exact_numeric_values_and_earlier_receipts_survive_restore_and_rebackup() {
    use std::str::FromStr;
    let Some(source) = fixture("backup_exact_source") else {
        return;
    };
    let target = fixture("backup_exact_target").unwrap();
    let mut schema = Schema::new();
    for (id, name, ty) in [
        (1000, "decimal", ValueType::BigDec),
        (1001, "double", ValueType::Double),
        (1002, "float", ValueType::Float),
    ] {
        schema
            .install(Attribute::new(
                id,
                Keyword::new("exact", name),
                ty,
                Cardinality::One,
            ))
            .unwrap();
    }
    BlockDatabase::create(
        &PostgresConnectionConfig::plaintext(&source.connection),
        "source",
        schema,
    )
    .unwrap();
    let writer = common::start_service(&source.connection, "source");
    let add = |entity: &str, attribute, value: Value| TxOp::Add {
        entity: EntityRef::Temp(entity.into()),
        attribute,
        value: value.into(),
    };
    let request = TransactionRequest::new(
        "exact-numeric",
        vec![
            add(
                "negative",
                1000,
                Value::BigDec(bigdecimal::BigDecimal::from_str("1.00").unwrap()),
            ),
            add(
                "positive",
                1000,
                Value::BigDec(bigdecimal::BigDecimal::from_str("1.0").unwrap()),
            ),
            add("negative", 1001, Value::Double(-0.0)),
            add("positive", 1001, Value::Double(0.0)),
            add("negative", 1002, Value::Float(-0.0)),
            add("positive", 1002, Value::Float(0.0)),
            add("nan", 1001, Value::Double(f64::NAN)),
            add("nan", 1002, Value::Float(f32::NAN)),
        ],
    )
    .with_tx_instant(1000);
    let report = writer
        .client()
        .transact(request.clone(), Duration::from_secs(30))
        .unwrap();
    // Retain a receipt base older than the exported point.
    writer
        .client()
        .transact(
            TransactionRequest::new("later", vec![add("later", 1001, Value::Double(37.0))])
                .with_tx_instant(2000),
            Duration::from_secs(30),
        )
        .unwrap();
    writer.shutdown();
    let directory = common::private_directory();
    let point = PortableBackup::connect(&source.connection)
        .unwrap()
        .backup_database("source", directory.path())
        .unwrap();
    let started = Instant::now();
    let mut restore = PortableBackup::connect(&target.connection).unwrap();
    let restored = restore
        .restore_backup(directory.path(), point.basis_t, "target")
        .unwrap();
    let negative = report.tempids["negative"];
    let positive = report.tempids["positive"];
    let check = |value: &DatabaseValue| {
        for (entity, expected) in [(negative, "1.00"), (positive, "1.0")] {
            let Value::BigDec(decimal) = &value.values(entity, 1000).unwrap()[0] else {
                panic!("decimal type lost");
            };
            assert_eq!(decimal.to_string(), expected);
        }
        // Preserve the source's canonical representation: the existing value
        // codec canonicalizes signed zero and NaNs, but not decimal scale.
        for entity in [negative, positive] {
            let Value::Double(double) = report.db_after.values(entity, 1001).unwrap()[0] else {
                unreachable!()
            };
            let Value::Float(float) = report.db_after.values(entity, 1002).unwrap()[0] else {
                unreachable!()
            };
            let Value::Double(actual) = value.values(entity, 1001).unwrap()[0] else {
                panic!("double type lost");
            };
            assert_eq!(actual.to_bits(), double.to_bits());
            let Value::Float(actual) = value.values(entity, 1002).unwrap()[0] else {
                panic!("float type lost");
            };
            assert_eq!(actual.to_bits(), float.to_bits());
        }
        assert!(
            matches!(value.values(report.tempids["nan"], 1001).unwrap()[0], Value::Double(n) if n.is_nan())
        );
    };
    assert_eq!(restored.point.manifest_hash, point.manifest_hash);
    check(
        &Peer::connect(&target.connection, "target", 8)
            .unwrap()
            .database_value(),
    );
    let writer = common::start_service(&target.connection, "target");
    let replay = writer
        .client()
        .transact(request, Duration::from_secs(30))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tempids, report.tempids);
    assert_eq!(replay.tx_hash, report.tx_hash);
    assert_eq!(
        replay.db_before.snapshot_key(),
        report.db_before.snapshot_key()
    );
    assert_eq!(
        replay.db_after.snapshot_key(),
        report.db_after.snapshot_key()
    );
    check(&replay.db_after);
    writer.shutdown();
    let second = common::private_directory();
    PortableBackup::connect(&target.connection)
        .unwrap()
        .backup_database("target", second.path())
        .unwrap();
    check(&BackupConnection::open(second.path()).unwrap().db());
    eprintln!(
        "EXACT_BACKUP restore+receipt+rebackup_ms={}",
        started.elapsed().as_millis()
    );
}

#[test]
fn repeated_capture_rejects_missing_and_corrupt_objects_in_completed_points() {
    use atomic_core::storage::root::DatabaseRoot;
    let Some(source) = fixture("backup_retry_integrity") else {
        return;
    };
    let config = PostgresConnectionConfig::plaintext(&source.connection);
    let database = BlockDatabase::create(&config, "source", schema()).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let root: Digest = store
        .read_ref(&database.reference_key())
        .unwrap()
        .unwrap()
        .value
        .unwrap()
        .try_into()
        .unwrap();
    let publication = DatabaseRoot::decode(&root, &store.get(root).unwrap().unwrap()).unwrap();
    let metadata = publication.metadata.unwrap();
    let directory = common::private_directory();
    let mut backup = PortableBackup::connect(&source.connection).unwrap();
    let point = backup.backup_database("source", directory.path()).unwrap();
    let path = directory.path().join("objects").join(
        metadata
            .iter()
            .map(|b| format!("{b:02x}"))
            .collect::<String>(),
    );
    let original = std::fs::read(&path).unwrap();
    let hidden = directory.path().join("removed-test-object");
    std::fs::rename(&path, &hidden).unwrap();
    assert!(backup.backup_database("source", directory.path()).is_err());
    assert!(PortableBackup::verify_backup_presence(directory.path(), point.basis_t).is_err());
    std::fs::rename(&hidden, &path).unwrap();
    let mut corrupt = original.clone();
    let last = corrupt.len() - 1;
    corrupt[last] ^= 1;
    std::fs::write(&path, corrupt).unwrap();
    assert!(backup.backup_database("source", directory.path()).is_err());
    assert!(PortableBackup::verify_backup_presence(directory.path(), point.basis_t).is_err());
    std::fs::write(&path, original).unwrap();
    assert_eq!(
        backup
            .backup_database("source", directory.path())
            .unwrap()
            .manifest_hash,
        point.manifest_hash
    );
}
