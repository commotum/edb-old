//! Visible headless restore reservations and route retirement fences.
mod common;
use atomic_core::storage::ownership::{BlockCollector, CollectionPhase};
use atomic_core::storage::{BlockDatabase, BlockTransactor, BlockWriterOptions, PgBlockStore};
use atomic_core::*;
use std::time::Duration;

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let url = std::env::var("ATOMIC_POSTGRES_URL").ok()?;
    let fixture = common::PostgresFixture::new(&url, label);
    PgBlockStore::install(&PostgresConnectionConfig::plaintext(&fixture.connection)).unwrap();
    Some(fixture)
}
fn collect(config: &PostgresConnectionConfig) {
    let mut collector = BlockCollector::connect(config).unwrap();
    for _ in 0..128 {
        if collector.advance(Duration::ZERO, 4096).unwrap().phase == CollectionPhase::Complete {
            return;
        }
    }
    panic!("bounded fixture collection did not finish");
}

#[test]
fn headless_restore_is_visible_idempotent_and_retirement_fences_the_staged_worker() {
    let Some(source) = fixture("catalog_restore_source") else {
        return;
    };
    let target = fixture("catalog_restore_target").unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    DatabaseCatalog::connect(&source.connection)
        .unwrap()
        .create_if_absent("source", schema)
        .unwrap();
    let directory = common::private_directory();
    let point = PortableBackup::connect(&source.connection)
        .unwrap()
        .backup_database("source", directory.path())
        .unwrap();
    let mut restore = PortableBackup::connect(&target.connection).unwrap();
    let config = PostgresConnectionConfig::plaintext(&target.connection);
    assert_eq!(
        restore
            .restore_backup_with_fault(
                directory.path(),
                point.basis_t,
                "pending",
                RestoreFault::AfterFirstContentInserted,
            )
            .unwrap_err()
            .code,
        "backup/restore-after-content"
    );
    // Only one child-closed engine object has been copied, not the complete
    // manifest. Pending stack IDs are weak; this paused graph must survive GC
    // through its completed-subtree ownership map and remain resumable.
    collect(&config);
    assert_eq!(
        restore
            .restore_backup_with_fault(
                directory.path(),
                point.basis_t,
                "pending",
                RestoreFault::BeforeCommit
            )
            .unwrap_err()
            .code,
        "backup/restore-before-activation"
    );
    let mut catalog = DatabaseCatalog::connect(&target.connection).unwrap();
    let pending = catalog.resolve("pending").unwrap();
    assert_eq!(pending.lineage_id, point.lineage_id);
    assert_eq!(catalog.list(None, 10).unwrap(), vec![pending.clone()]);
    let repeated = catalog.create_if_absent("pending", Schema::new()).unwrap();
    assert!(!repeated.created);
    assert_eq!(repeated.database, pending);
    assert_eq!(
        Peer::connect(&target.connection, "pending", 0)
            .err()
            .unwrap()
            .code,
        "backup/database-restoring"
    );
    let mut store = PgBlockStore::connect(&config).unwrap();
    let root_key = format!("databases/{}", pending.database_id);
    let work_key = format!("restores/{}", pending.database_id);
    assert_eq!(store.read_ref(&root_key).unwrap(), None);
    assert!(store.read_ref(&work_key).unwrap().unwrap().value.is_some());
    collect(&config);
    let mut probed = false;
    let error = restore
        .restore_backup_with_activation_probe(directory.path(), point.basis_t, "pending", || {
            probed = true;
            assert!(
                catalog
                    .retire_checked("pending", &pending.lineage_id)
                    .unwrap()
                    .unwrap()
                    .retired
            );
        })
        .unwrap_err();
    assert!(probed);
    assert_eq!(error.category, ErrorCategory::Conflict, "{error:?}");
    assert!(store.read_ref(&root_key).unwrap().unwrap().value.is_none());
    assert!(store.read_ref(&work_key).unwrap().unwrap().value.is_none());
    let replacement = catalog.create_if_absent("pending", Schema::new()).unwrap();
    assert!(replacement.created);
    assert_ne!(replacement.database.database_id, pending.database_id);
    assert_ne!(replacement.database.lineage_id, pending.lineage_id);
    // The retired lineage can immediately acquire a fresh route without waiting
    // for reclamation, but never two simultaneous active aliases.
    restore
        .restore_backup(directory.path(), point.basis_t, "restored")
        .unwrap();
    let fresh = catalog.resolve("restored").unwrap();
    assert_ne!(fresh.database_id, pending.database_id);
    assert_eq!(fresh.lineage_id, pending.lineage_id);
    assert_eq!(
        restore
            .restore_backup(directory.path(), point.basis_t, "alias")
            .unwrap_err()
            .code,
        "backup/lineage-exists"
    );
    assert!(store.read_ref(&root_key).unwrap().unwrap().value.is_none());
}

#[test]
fn restore_refuses_same_log_branches_with_different_request_keys_and_newer_target_heads() {
    let Some(source) = fixture("restore_branch_source") else {
        return;
    };
    let target = fixture("restore_branch_target").unwrap();
    DatabaseCatalog::connect(&source.connection)
        .unwrap()
        .create_if_absent("source", Schema::new())
        .unwrap();
    let directory = common::private_directory();
    let mut backup = PortableBackup::connect(&source.connection).unwrap();
    let initial = backup.backup_database("source", directory.path()).unwrap();
    let mut restore = PortableBackup::connect(&target.connection).unwrap();
    restore
        .restore_backup(directory.path(), initial.basis_t, "target")
        .unwrap();
    let operation = |key: &str| {
        TransactionRequest::new(
            key,
            vec![TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: DB_DOC as u32,
                value: Value::String("identical material history".into()).into(),
            }],
        )
        .with_tx_instant(1000)
    };
    let source_writer = common::start_service(&source.connection, "source");
    let left = source_writer
        .client()
        .transact(operation("source-key"), Duration::from_secs(30))
        .unwrap();
    source_writer.shutdown();
    let target_writer = common::start_service(&target.connection, "target");
    let right = target_writer
        .client()
        .transact(operation("target-key"), Duration::from_secs(30))
        .unwrap();
    target_writer.shutdown();
    assert_eq!(left.tx_data, right.tx_data);
    assert_eq!(
        left.tx_hash, right.tx_hash,
        "log omits request key identity"
    );
    let point = backup.backup_database("source", directory.path()).unwrap();
    let target_entry = DatabaseCatalog::connect(&target.connection)
        .unwrap()
        .resolve("target")
        .unwrap();
    let mut store =
        PgBlockStore::connect(&PostgresConnectionConfig::plaintext(&target.connection)).unwrap();
    let root_key = format!("databases/{}", target_entry.database_id);
    let work_key = format!("restores/{}", target_entry.database_id);
    let before = store.read_ref(&root_key).unwrap();
    let before_work = store.read_ref(&work_key).unwrap();
    assert_eq!(
        restore
            .restore_backup(directory.path(), point.basis_t, "target")
            .unwrap_err()
            .code,
        "backup/restore-target-changed"
    );
    assert_eq!(store.read_ref(&root_key).unwrap(), before);
    assert_eq!(
        store.read_ref(&work_key).unwrap(),
        before_work,
        "incompatible target is rejected before staging"
    );
    let writer = common::start_service(&target.connection, "target");
    let replay = writer
        .client()
        .transact(operation("target-key"), Duration::from_secs(30))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, right.tx_hash);
    let advanced = writer
        .client()
        .transact(
            TransactionRequest::new("advance", vec![]).with_tx_instant(2000),
            Duration::from_secs(30),
        )
        .unwrap();
    writer.shutdown();
    assert!(advanced.basis_t > point.basis_t);
    let before = store.read_ref(&root_key).unwrap();
    assert_eq!(
        restore
            .restore_backup(directory.path(), point.basis_t, "target")
            .unwrap_err()
            .code,
        "backup/restore-target-changed"
    );
    assert_eq!(store.read_ref(&root_key).unwrap(), before);
    assert_eq!(
        Peer::connect(&target.connection, "target", 0)
            .unwrap()
            .database_value()
            .basis_t(),
        advanced.basis_t
    );
}

#[test]
fn excision_revokes_pending_restore_and_exact_activation_receipts() {
    let Some(source) = fixture("restore_excision_source") else {
        return;
    };
    let target = fixture("restore_excision_target").unwrap();
    DatabaseCatalog::connect(&source.connection)
        .unwrap()
        .create_if_absent("source", Schema::new())
        .unwrap();
    let source_writer = common::start_service(&source.connection, "source");
    let secret = source_writer
        .client()
        .transact(
            TransactionRequest::new(
                "secret",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("private".into()),
                    attribute: DB_DOC as u32,
                    value: Value::String("erase restore-owned history".into()).into(),
                }],
            )
            .with_tx_instant(1000),
            Duration::from_secs(30),
        )
        .unwrap();
    let entity = secret.tempids["private"];
    source_writer.shutdown();
    let directory = common::private_directory();
    let mut backup = PortableBackup::connect(&source.connection).unwrap();
    let initial = backup.backup_database("source", directory.path()).unwrap();
    let mut restore = PortableBackup::connect(&target.connection).unwrap();
    assert_eq!(
        restore
            .restore_backup_with_fault(
                directory.path(),
                initial.basis_t,
                "target",
                RestoreFault::AfterCommitBeforeResponse,
            )
            .unwrap_err()
            .code,
        "backup/restore-after-activation"
    );

    let source_writer = common::start_service(&source.connection, "source");
    source_writer
        .client()
        .transact(
            TransactionRequest::new("successor", vec![]).with_tx_instant(2000),
            Duration::from_secs(30),
        )
        .unwrap();
    source_writer.shutdown();
    let successor = backup.backup_database("source", directory.path()).unwrap();
    assert_eq!(
        restore
            .restore_backup_with_fault(
                directory.path(),
                successor.basis_t,
                "target",
                RestoreFault::BeforeCommit,
            )
            .unwrap_err()
            .code,
        "backup/restore-before-activation"
    );
    let target_entry = DatabaseCatalog::connect(&target.connection)
        .unwrap()
        .resolve("target")
        .unwrap();
    let config = PostgresConnectionConfig::plaintext(&target.connection);
    let mut store = PgBlockStore::connect(&config).unwrap();
    let owner_keys = [
        format!("restores/{}", target_entry.database_id),
        format!("restores/completed/{}", target_entry.database_id),
    ];
    for key in &owner_keys {
        assert!(store.read_ref(key).unwrap().unwrap().value.is_some());
    }

    // A real writer transition invalidates the pending restore's old guards.
    // Excision must additionally remove both strong owners atomically, not
    // leave erased data alive through abandoned work or receipt-first replay.
    let writer = common::start_service(&target.connection, "target");
    let peer = Peer::connect(&target.connection, "target", 0).unwrap();
    let requested = writer
        .client()
        .transact(
            TransactionRequest::new(
                "erase",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("excision".into()),
                    attribute: DB_EXCISE as u32,
                    value: Value::Ref(entity).into(),
                }],
            )
            .with_tx_instant(2000),
            Duration::from_secs(30),
        )
        .unwrap();
    let current = peer
        .sync_excise(requested.basis_t, Duration::from_secs(60))
        .unwrap();
    assert!(current.values(entity, DB_DOC as u32).unwrap().is_empty());
    writer.shutdown();
    for key in &owner_keys {
        assert!(store.read_ref(key).unwrap().unwrap().value.is_none());
    }
    let root_key = format!("databases/{}", target_entry.database_id);
    let root = store.read_ref(&root_key).unwrap();
    for point in [initial, successor] {
        assert_eq!(
            restore
                .restore_backup_point(
                    directory.path(),
                    point.basis_t,
                    point.log_generation,
                    "target",
                )
                .unwrap_err()
                .code,
            "backup/restore-target-changed"
        );
        assert_eq!(store.read_ref(&root_key).unwrap(), root);
    }
}

#[test]
fn same_basis_excision_points_select_exact_generations_and_restore_tombstones() {
    let Some(source) = fixture("backup_excision_points") else {
        return;
    };
    let target = fixture("backup_excision_points_target").unwrap();
    let config = PostgresConnectionConfig::plaintext(&source.connection);
    let database = BlockDatabase::create(&config, "source", Schema::new()).unwrap();
    // Direct writer deliberately leaves the committed excision unprocessed
    // until its pre-excision point is durably exported.
    let mut writer =
        BlockTransactor::claim(&config, database, BlockWriterOptions::default()).unwrap();
    let seed = TransactionRequest::new(
        "secret",
        vec![TxOp::Add {
            entity: EntityRef::Temp("private".into()),
            attribute: DB_DOC as u32,
            value: Value::String("pre-excision point".into()).into(),
        }],
    )
    .with_tx_instant(1000);
    let seeded = writer.transact(&seed).unwrap();
    let entity = seeded.tempids["private"];
    let requested = writer
        .transact(
            &TransactionRequest::new(
                "erase",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE as u32,
                    value: Value::Ref(entity).into(),
                }],
            )
            .with_tx_instant(2000),
        )
        .unwrap();
    writer.release().unwrap();
    let directory = common::private_directory();
    let mut backup = PortableBackup::connect(&source.connection).unwrap();
    let before = backup.backup_database("source", directory.path()).unwrap();
    let peer = Peer::connect(&source.connection, "source", 0).unwrap();
    let service = common::start_service(&source.connection, "source");
    service.client().request_index().unwrap();
    peer.sync_excise(requested.basis_t, Duration::from_secs(60))
        .unwrap();
    service.shutdown();
    let after = backup.backup_database("source", directory.path()).unwrap();
    assert_eq!(before.basis_t, after.basis_t);
    assert_eq!(before.log_generation + 1, after.log_generation);
    assert_ne!(before.manifest_hash, after.manifest_hash);
    let points = PortableBackup::list_backup_points(directory.path()).unwrap();
    assert_eq!(
        points
            .iter()
            .map(|point| point.manifest_hash)
            .collect::<Vec<_>>(),
        vec![before.manifest_hash, after.manifest_hash]
    );
    assert_eq!(
        PortableBackup::list_backups(directory.path()).unwrap(),
        vec![before.basis_t]
    );
    let old = PortableBackup::verify_backup_point(
        directory.path(),
        before.basis_t,
        before.log_generation,
        true,
    )
    .unwrap();
    assert_eq!(
        old.database.values(entity, DB_DOC as u32),
        vec![&Value::String("pre-excision point".into())]
    );
    let latest = PortableBackup::verify_backup(directory.path(), after.basis_t, true).unwrap();
    assert_eq!(latest.point.manifest_hash, after.manifest_hash);
    assert!(latest.database.values(entity, DB_DOC as u32).is_empty());
    let restored = PortableBackup::connect(&target.connection)
        .unwrap()
        .restore_backup_point(
            directory.path(),
            after.basis_t,
            after.log_generation,
            "target",
        )
        .unwrap();
    assert_eq!(restored.point.manifest_hash, after.manifest_hash);
    assert!(
        Peer::connect(&target.connection, "target", 8)
            .unwrap()
            .database_value()
            .values(entity, DB_DOC as u32)
            .unwrap()
            .is_empty()
    );
    let service = common::start_service(&target.connection, "target");
    assert_eq!(
        service
            .client()
            .transact(seed, Duration::from_secs(30))
            .unwrap_err()
            .code,
        "postgres/idempotency-predates-excision"
    );
    service.shutdown();
}

#[test]
fn final_restore_cas_refuses_a_live_head_advance_after_the_completion_proof() {
    let Some(source) = fixture("restore_final_cas_source") else {
        return;
    };
    let target = fixture("restore_final_cas_target").unwrap();
    DatabaseCatalog::connect(&source.connection)
        .unwrap()
        .create_if_absent("source", Schema::new())
        .unwrap();
    let directory = common::private_directory();
    let mut backup = PortableBackup::connect(&source.connection).unwrap();
    let initial = backup.backup_database("source", directory.path()).unwrap();
    let request = |key: &str| {
        TransactionRequest::new(
            key,
            vec![TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: DB_DOC as u32,
                value: Value::String(key.into()).into(),
            }],
        )
        .with_tx_instant(1000)
    };
    let source_writer = common::start_service(&source.connection, "source");
    source_writer
        .client()
        .transact(request("source"), Duration::from_secs(30))
        .unwrap();
    source_writer.shutdown();
    let successor = backup.backup_database("source", directory.path()).unwrap();
    let mut restore = PortableBackup::connect(&target.connection).unwrap();
    restore
        .restore_backup(directory.path(), initial.basis_t, "target")
        .unwrap();
    let route = DatabaseCatalog::connect(&target.connection)
        .unwrap()
        .resolve("target")
        .unwrap()
        .database_id;
    let mut store =
        PgBlockStore::connect(&PostgresConnectionConfig::plaintext(&target.connection)).unwrap();
    let root_key = format!("databases/{route}");
    let mut advanced = None;
    let mut advanced_root = None;
    let error = restore
        .restore_backup_with_completion_probe(directory.path(), successor.basis_t, "target", || {
            let writer = common::start_service(&target.connection, "target");
            advanced = Some(
                writer
                    .client()
                    .transact(request("competing"), Duration::from_secs(30))
                    .unwrap(),
            );
            writer.shutdown();
            advanced_root = store.read_ref(&root_key).unwrap();
        })
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Conflict, "{error:?}");
    assert_eq!(store.read_ref(&root_key).unwrap(), advanced_root);
    let advanced = advanced.expect("completion probe executed");
    let peer = Peer::connect(&target.connection, "target", 0).unwrap();
    assert_eq!(
        peer.database_value()
            .values(advanced.tempids["item"], DB_DOC as u32)
            .unwrap(),
        vec![Value::String("competing".into())]
    );
    let writer = common::start_service(&target.connection, "target");
    let replay = writer
        .client()
        .transact(request("competing"), Duration::from_secs(30))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, advanced.tx_hash);
    writer.shutdown();
}
