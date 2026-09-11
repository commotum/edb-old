mod common;
use atomic_core::storage::root::{Block, DatabaseRoot};
use atomic_core::storage::{
    BlockDatabase, CasOutcome, IndexDescriptor, PgBlockStore, SnapshotMetadata,
};
use atomic_core::*;
use std::sync::atomic::AtomicBool;
use std::time::{Duration, Instant};

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    BlockDatabase::create(&config, "changes", Schema::new()).unwrap();
    Some(fixture)
}

fn channel(fixture: &common::PostgresFixture) -> String {
    let database = BlockDatabase::resolve(
        &PostgresConnectionConfig::plaintext(&fixture.connection),
        "changes",
    )
    .unwrap();
    let mut bytes = fixture.schema.as_bytes().to_vec();
    bytes.push(0);
    bytes.extend_from_slice(database.reference_key().as_bytes());
    format!(
        "atomic_r_{}",
        sha256(&bytes)[..24]
            .iter()
            .map(|b| format!("{b:02x}"))
            .collect::<String>()
    )
}

fn checkpoint_key(fixture: &common::PostgresFixture, name: &str) -> String {
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let principal: String = sql
        .query_one("SELECT current_user::text", &[])
        .unwrap()
        .get(0);
    let database = BlockDatabase::resolve(
        &PostgresConnectionConfig::plaintext(&fixture.connection),
        "changes",
    )
    .unwrap();
    let mut bytes = b"atomic/change-consumer/v1".to_vec();
    for value in [principal.as_str(), name] {
        bytes.extend_from_slice(&(value.len() as u64).to_be_bytes());
        bytes.extend_from_slice(value.as_bytes());
    }
    format!(
        "consumers/{}/{}",
        database.reference_key().strip_prefix("databases/").unwrap(),
        sha256(&bytes)
            .iter()
            .map(|b| format!("{b:02x}"))
            .collect::<String>()
    )
}

#[test]
fn replay_is_contiguous_bounded_and_checkpointed_after_processing() {
    let Some(fixture) = fixture("change_replay") else {
        return;
    };
    let url = &fixture.connection;
    let writer = common::start_service(url, "changes");
    let mut entity = EntityRef::Temp("document".into());
    let mut expected = Vec::new();
    for t in 1..=3 {
        let operation = if t < 3 {
            TxOp::Add {
                entity: entity.clone(),
                attribute: DB_DOC as u32,
                value: Value::String(format!("version {t}")).into(),
            }
        } else {
            TxOp::Retract {
                entity: entity.clone(),
                attribute: DB_DOC as u32,
                value: None,
            }
        };
        let report = writer
            .client()
            .transact(
                TransactionRequest::new(format!("tx{t}"), vec![operation]),
                Duration::from_secs(10),
            )
            .unwrap();
        if t == 1 {
            entity = EntityRef::Id(report.tempids["document"]);
        }
        expected.push(report.tx_data);
    }
    let config = ChangeConsumerConfig::default();
    let mut consumer = ChangeConsumer::connect(url, "changes", "billing", config).unwrap();
    assert_eq!(consumer.checkpoint().last_t(), 0);
    let initial = consumer.checkpoint().clone();
    assert_eq!(
        consumer.acknowledge(&initial).unwrap_err().code,
        "consumer/not-delivered"
    );
    let first = consumer.next(Duration::ZERO).unwrap().unwrap();
    assert_eq!(first.transaction.t, 1);
    assert_eq!(first.transaction.data, expected[0]);
    assert_eq!(
        consumer.next(Duration::ZERO).unwrap_err().code,
        "consumer/unacknowledged"
    );
    assert_eq!(consumer.checkpoint().last_t(), 0);
    drop(consumer); // Crash after the external effect but before checkpointing.
    let mut resumed = ChangeConsumer::connect(url, "changes", "billing", config).unwrap();
    let replay = resumed.next(Duration::ZERO).unwrap().unwrap();
    assert_eq!(first, replay);
    let mut competing = ChangeConsumer::connect(url, "changes", "billing", config).unwrap();
    let duplicate = competing.next(Duration::ZERO).unwrap().unwrap();
    resumed.acknowledge(&replay.checkpoint).unwrap();
    assert_eq!(
        competing
            .acknowledge(&duplicate.checkpoint)
            .unwrap_err()
            .code,
        "consumer/checkpoint-conflict"
    );
    assert_eq!(
        resumed.acknowledge(&first.checkpoint).unwrap_err().code,
        "consumer/not-delivered"
    );
    for t in 2..=3 {
        let event = resumed.next(Duration::ZERO).unwrap().unwrap();
        assert_eq!(event.transaction.t, t);
        assert_eq!(event.transaction.data, expected[t as usize - 1]);
        resumed.acknowledge(&event.checkpoint).unwrap();
    }
    assert!(resumed.next(Duration::ZERO).unwrap().is_none());
    assert_eq!(
        resumed
            .next_with_cancel(Duration::from_secs(1), &AtomicBool::new(true))
            .unwrap_err()
            .code,
        "consumer/cancelled"
    );
    let mut small = ChangeConsumer::connect(
        url,
        "changes",
        "small",
        ChangeConsumerConfig {
            max_event_bytes: 1,
            ..config
        },
    )
    .unwrap();
    assert_eq!(
        small.next(Duration::ZERO).unwrap_err().code,
        "consumer/event-too-large"
    );
    assert_eq!(small.checkpoint().last_t(), 0);
    // A checkpoint is not trusted merely because an application login wrote
    // it. Reopening verifies the referenced canonical transaction.
    let mut store = PgBlockStore::connect(&PostgresConnectionConfig::plaintext(url)).unwrap();
    let key = checkpoint_key(&fixture, "billing");
    let reference = store.read_ref(&key).unwrap().unwrap();
    let mut payload = reference.value.unwrap();
    payload[40..72].fill(99);
    assert!(matches!(
        store
            .compare_exchange(&key, Some(reference.revision), Some(&payload))
            .unwrap(),
        CasOutcome::Applied(_)
    ));
    assert_eq!(
        ChangeConsumer::connect(url, "changes", "billing", config)
            .err()
            .unwrap()
            .code,
        "consumer/checkpoint-hash"
    );
    writer.shutdown();
}

#[test]
fn consumer_reconnect_repairs_a_dropped_notice_from_the_log() {
    let Some(fixture) = fixture("change_reconnect") else {
        return;
    };
    let url = &fixture.connection;
    let writer = common::start_service(url, "changes");
    let mut consumer =
        ChangeConsumer::connect(url, "changes", "resume", ChangeConsumerConfig::default()).unwrap();
    let mut admin = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let channel = channel(&fixture);
    let listen = format!("LISTEN {channel}");
    let pid:i32=admin.query_one("SELECT pid FROM pg_stat_activity WHERE datname=current_database() AND query=$1 ORDER BY backend_start DESC LIMIT 1", &[&listen]).unwrap().get(0);
    let waiter = std::thread::spawn(move || {
        let event = consumer.next(Duration::from_secs(5)).unwrap().unwrap();
        (consumer, event)
    });
    std::thread::sleep(Duration::from_millis(100));
    let terminated: bool = admin
        .query_one("SELECT pg_terminate_backend($1)", &[&pid])
        .unwrap()
        .get(0);
    assert!(terminated);
    std::thread::sleep(Duration::from_millis(100));
    writer
        .client()
        .transact(
            TransactionRequest::new("while-disconnected", vec![]),
            Duration::from_secs(10),
        )
        .unwrap();
    // Head replay is sufficient even if this exact notice was lost.
    let (mut consumer, event) = waiter.join().unwrap();
    assert_eq!(event.transaction.t, 1);
    consumer.acknowledge(&event.checkpoint).unwrap();
    assert_eq!(consumer.stats().reconnects, 1);
    writer.shutdown();
}

#[test]
fn spoofed_notice_flood_is_coalesced_without_fabricating_transactions() {
    let Some(fixture) = fixture("change_notice_flood") else {
        return;
    };
    let mut consumer = ChangeConsumer::connect(
        &fixture.connection,
        "changes",
        "flood",
        ChangeConsumerConfig::default(),
    )
    .unwrap();
    let before = notice_listener_stats();
    let mut admin = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let channel = channel(&fixture);
    admin
        .query(
            "SELECT pg_notify($1,repeat('x',100) || n::text) FROM generate_series(1,512) n",
            &[&channel],
        )
        .unwrap();
    assert!(consumer.next(Duration::from_millis(750)).unwrap().is_none());
    let after = notice_listener_stats();
    assert!(after.received - before.received >= 512);
    assert!(after.coalesced > before.coalesced);
    assert!(after.peak_batch <= 64);
    assert_eq!(consumer.checkpoint().last_t(), 0);
    assert_eq!(consumer.stats().delivered, 0);
    eprintln!(
        "notice_flood received={} coalesced={} peak_driver_batch={} gap_checks={}",
        after.received - before.received,
        after.coalesced - before.coalesced,
        after.peak_batch,
        consumer.stats().gap_checks
    );
}

#[test]
fn generation_transition_rejects_old_checkpoints_without_a_lifetime_pin() {
    let Some(fixture) = fixture("change_generation") else {
        return;
    };
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    let writer = common::start_service(&fixture.connection, "changes");
    writer
        .client()
        .transact(
            TransactionRequest::new("seed", vec![]),
            Duration::from_secs(10),
        )
        .unwrap();
    writer.shutdown();
    let mut admin = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let active_pins = |admin: &mut postgres::Client| -> std::collections::BTreeSet<String> {
        admin
            .query(
                "SELECT key FROM atomic_refs WHERE key LIKE 'pins/%' AND value IS NOT NULL",
                &[],
            )
            .unwrap()
            .into_iter()
            .map(|row| row.get(0))
            .collect()
    };
    let pins_before = active_pins(&mut admin);
    let mut consumer =
        ChangeConsumer::connect(&fixture.connection, "changes", "old", Default::default()).unwrap();
    let pending = consumer.next(Duration::ZERO).unwrap().unwrap();
    // Previously released writer pins may finish asynchronous cleanup here.
    // Delivery must add no retained pin of its own, irrespective of those drops.
    assert!(
        active_pins(&mut admin).is_subset(&pins_before),
        "delivery retained a lifetime history pin"
    );
    let mut store = PgBlockStore::connect(&config).unwrap();
    let database = BlockDatabase::resolve(&config, "changes").unwrap();
    let reference = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let id = reference.value.unwrap().as_slice().try_into().unwrap();
    let mut root = DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
    // Fixture-only current publication transition: the Stage-5 excision worker
    // owns redaction, while this regression isolates consumer generation fences.
    let metadata_id = root.metadata.unwrap();
    let mut metadata =
        SnapshotMetadata::decode(&metadata_id, &store.get(metadata_id).unwrap().unwrap()).unwrap();
    metadata.generation += 1;
    root.metadata = Some(store.put(&metadata.encode().unwrap()).unwrap());
    let index_id = root.indexes.unwrap();
    let mut indexes =
        IndexDescriptor::decode(&index_id, &store.get(index_id).unwrap().unwrap()).unwrap();
    indexes.generation = metadata.generation;
    root.indexes = Some(store.put(&indexes.encode().unwrap()).unwrap());
    let next = store.put(&root.encode().unwrap()).unwrap();
    assert!(matches!(
        store
            .compare_exchange(
                &database.reference_key(),
                Some(reference.revision),
                Some(&next)
            )
            .unwrap(),
        CasOutcome::Applied(_)
    ));
    assert_eq!(
        consumer.acknowledge(&pending.checkpoint).unwrap_err().code,
        "consumer/generation-changed"
    );
    assert_eq!(
        ChangeConsumer::connect(&fixture.connection, "changes", "old", Default::default())
            .err()
            .unwrap()
            .code,
        "consumer/generation-changed"
    );
    let fresh =
        ChangeConsumer::connect(&fixture.connection, "changes", "fresh", Default::default())
            .unwrap();
    assert_eq!(fresh.checkpoint().generation(), metadata.generation);
    assert!(active_pins(&mut admin).is_subset(&pins_before));
}

#[test]
fn consumer_names_are_principal_scoped_with_read_only_object_privileges() {
    let Some(fixture) = fixture("change_roles") else {
        return;
    };
    let mut admin = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let can_create: bool = admin
        .query_one(
            "SELECT rolsuper OR rolcreaterole FROM pg_roles WHERE rolname=current_user",
            &[],
        )
        .unwrap()
        .get(0);
    if !can_create {
        eprintln!("SKIP: dedicated-role creation unavailable");
        return;
    }
    let names = [
        format!("{}_a", fixture.schema),
        format!("{}_b", fixture.schema),
    ];
    struct Roles {
        admin: postgres::Client,
        schema: String,
        names: [String; 2],
    }
    impl Drop for Roles {
        fn drop(&mut self) {
            for role in &self.names {
                // Exact fixture grants only; never DROP OWNED or broad cleanup.
                let _ = self.admin.batch_execute(&format!(
                    "REVOKE SELECT ON {}.atomic_objects FROM {role}; REVOKE SELECT,INSERT,UPDATE,DELETE ON {}.atomic_refs FROM {role}; REVOKE USAGE ON SCHEMA {} FROM {role}; DROP ROLE {role}",
                    self.schema, self.schema, self.schema));
            }
        }
    }
    for role in &names {
        admin
            .batch_execute(&format!(
                "CREATE ROLE {role} LOGIN PASSWORD '{}'",
                fixture.schema
            ))
            .unwrap();
    }
    let roles = Roles {
        admin,
        schema: fixture.schema.clone(),
        names,
    };
    let mut admin = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    for role in &roles.names {
        admin.batch_execute(&format!("GRANT USAGE ON SCHEMA {} TO {role}; GRANT SELECT ON {}.atomic_objects TO {role}; GRANT SELECT,INSERT,UPDATE,DELETE ON {}.atomic_refs TO {role}", fixture.schema, fixture.schema, fixture.schema)).unwrap();
    }
    let urls = roles
        .names
        .iter()
        .map(|role| {
            common::product_support::parameter(
                &common::product_support::parameter(&fixture.connection, "user", role),
                "password",
                &fixture.schema,
            )
        })
        .collect::<Vec<_>>();
    let writer = common::start_service(&fixture.connection, "changes");
    writer
        .client()
        .transact(
            TransactionRequest::new("one", vec![]),
            Duration::from_secs(10),
        )
        .unwrap();
    let mut consumer =
        ChangeConsumer::connect(&urls[0], "changes", "same-name", Default::default()).unwrap();
    let event = consumer.next(Duration::ZERO).unwrap().unwrap();
    consumer.acknowledge(&event.checkpoint).unwrap();
    let other =
        ChangeConsumer::connect(&urls[1], "changes", "same-name", Default::default()).unwrap();
    assert_eq!(
        other.checkpoint().last_t(),
        0,
        "checkpoint names crossed login namespace"
    );
    let mut peer = postgres::Client::connect(&urls[0], postgres::NoTls).unwrap();
    assert!(
        peer.execute("UPDATE atomic_objects SET payload=payload", &[])
            .is_err()
    );
    // Opaque references intentionally do not claim per-feature SQL row ACLs.
    // Their provider authority is broader than this application consumer API.
    assert_eq!(
        admin
            .query_one(
                "SELECT count(*) FROM atomic_refs WHERE key LIKE 'consumers/%'",
                &[]
            )
            .unwrap()
            .get::<_, i64>(0),
        2
    );
    drop((consumer, other, peer));
    writer.shutdown();
    drop(roles);
}

#[test]
fn checkpoint_identity_survives_rename_and_never_follows_name_reuse() {
    let Some(fixture) = fixture("change_identity") else {
        return;
    };
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    let writer = common::start_service(&fixture.connection, "changes");
    writer
        .client()
        .transact(
            TransactionRequest::new("one", vec![]),
            Duration::from_secs(10),
        )
        .unwrap();
    writer.shutdown();
    let mut consumer =
        ChangeConsumer::connect(&fixture.connection, "changes", "resume", Default::default())
            .unwrap();
    let event = consumer.next(Duration::ZERO).unwrap().unwrap();
    consumer.acknowledge(&event.checkpoint).unwrap();
    let database = BlockDatabase::resolve(&config, "changes").unwrap();
    let mut catalog = atomic_core::DatabaseCatalog::connect_configured(&config).unwrap();
    catalog.rename("changes", "renamed").unwrap();
    let replacement = BlockDatabase::create(&config, "changes", Schema::new()).unwrap();
    assert_ne!(replacement.identity, database.identity);
    consumer.reconnect().unwrap();
    assert_eq!(consumer.checkpoint().last_t(), 1);
    assert!(consumer.next(Duration::ZERO).unwrap().is_none());
    let renamed =
        ChangeConsumer::connect(&fixture.connection, "renamed", "resume", Default::default())
            .unwrap();
    assert_eq!(renamed.checkpoint(), consumer.checkpoint());
    let reused =
        ChangeConsumer::connect(&fixture.connection, "changes", "resume", Default::default())
            .unwrap();
    assert_eq!(reused.checkpoint().last_t(), 0);
    assert_ne!(
        reused.checkpoint().lineage_id(),
        consumer.checkpoint().lineage_id()
    );
    catalog.retire("renamed").unwrap();
    assert_eq!(
        consumer.next(Duration::ZERO).unwrap_err().code,
        "consumer/database-unavailable"
    );
}

#[test]
fn checkpoint_rejects_forged_earlier_prefix_with_unchanged_final_transaction() {
    use atomic_core::storage::log::LogRoot;
    let Some(fixture) = fixture("change_prefix") else {
        return;
    };
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    let writer = common::start_service(&fixture.connection, "changes");
    for n in 1..=2 {
        writer
            .client()
            .transact(
                TransactionRequest::new(format!("tx{n}"), vec![]),
                Duration::from_secs(10),
            )
            .unwrap();
    }
    writer.shutdown();
    let mut consumer =
        ChangeConsumer::connect(&fixture.connection, "changes", "saved", Default::default())
            .unwrap();
    for _ in 0..2 {
        let event = consumer.next(Duration::ZERO).unwrap().unwrap();
        consumer.acknowledge(&event.checkpoint).unwrap();
    }
    let saved = consumer.checkpoint().clone();
    let mut pending = ChangeConsumer::connect(
        &fixture.connection,
        "changes",
        "pending",
        Default::default(),
    )
    .unwrap();
    let first = pending.next(Duration::ZERO).unwrap().unwrap();
    pending.acknowledge(&first.checkpoint).unwrap();
    let second = pending.next(Duration::ZERO).unwrap().unwrap();
    let database = BlockDatabase::resolve(&config, "changes").unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let reference = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let id = reference.value.unwrap().as_slice().try_into().unwrap();
    let mut root = DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
    let log_id = root.log.unwrap();
    let log = LogRoot::open(&mut store, log_id).unwrap();
    let mut changed = log.read(&mut store, 1).unwrap().unwrap();
    changed
        .tx_data
        .iter_mut()
        .find(|d| d.attribute == DB_TX_INSTANT as u32)
        .unwrap()
        .value = Value::Instant(-123);
    let fork = LogRoot::empty().append(&mut store, &changed).unwrap();
    let mut page = Block::decode(&log_id, &store.get(log_id).unwrap().unwrap()).unwrap();
    page.links[0] = fork.read_record(&mut store, 1).unwrap().unwrap().id;
    let forged = store.put(&page.encode().unwrap()).unwrap();
    assert_eq!(
        LogRoot::open(&mut store, forged)
            .unwrap()
            .read_record(&mut store, 2)
            .unwrap()
            .unwrap()
            .id,
        saved.commit_hash()
    );
    root.log = Some(forged);
    let forged_root = store.put(&root.encode().unwrap()).unwrap();
    assert!(matches!(
        store
            .compare_exchange(
                &database.reference_key(),
                Some(reference.revision),
                Some(&forged_root)
            )
            .unwrap(),
        CasOutcome::Applied(_)
    ));
    assert_eq!(
        consumer.next(Duration::ZERO).unwrap_err().code,
        "consumer/checkpoint-prefix"
    );
    assert_eq!(consumer.checkpoint(), &saved);
    assert_eq!(
        ChangeConsumer::connect(&fixture.connection, "changes", "saved", Default::default())
            .err()
            .unwrap()
            .code,
        "consumer/checkpoint-prefix"
    );
    assert_eq!(
        pending.acknowledge(&second.checkpoint).unwrap_err().code,
        "consumer/checkpoint-prefix"
    );
}

#[test]
fn oversized_chunked_event_is_admitted_before_chunk_transfer_and_can_be_reopened() {
    let Some(fixture) = fixture("change_admission") else {
        return;
    };
    let writer = common::start_service(&fixture.connection, "changes");
    writer
        .client()
        .transact(
            TransactionRequest::new(
                "large",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("large".into()),
                    attribute: DB_DOC as u32,
                    value: Value::String("x".repeat(2 * 1024 * 1024)).into(),
                }],
            ),
            Duration::from_secs(15),
        )
        .unwrap();
    writer.shutdown();
    let mut consumer = ChangeConsumer::connect(
        &fixture.connection,
        "changes",
        "bounded",
        ChangeConsumerConfig {
            max_event_bytes: 16 * 1024,
            ..Default::default()
        },
    )
    .unwrap();
    let context = OperationContext::new(OperationKind::Application);
    {
        let _guard = context.enter();
        assert_eq!(
            consumer.next(Duration::ZERO).unwrap_err().code,
            "consumer/event-too-large"
        );
    }
    assert_eq!(consumer.checkpoint().last_t(), 0);
    assert!(
        context.snapshot().known_payload_read_bytes < 16 * 1024,
        "oversize rejection fetched transaction chunks: {:?}",
        context.snapshot()
    );
    drop(consumer);
    let mut resumed = ChangeConsumer::connect(
        &fixture.connection,
        "changes",
        "bounded",
        Default::default(),
    )
    .unwrap();
    let event = resumed.next(Duration::ZERO).unwrap().unwrap();
    assert!(event.accounted_bytes >= 4 * 1024 * 1024);
    resumed.acknowledge(&event.checkpoint).unwrap();
    assert_eq!(resumed.checkpoint().last_t(), 1);
}

#[cfg(unix)]
#[test]
fn separate_writer_wakes_idle_peer_and_blocked_consumer_without_polling_sql() {
    use std::io::{BufRead, BufReader};
    use std::os::unix::fs::PermissionsExt;
    use std::process::{Command, Stdio};
    let Some(fixture) = fixture("change_process") else {
        return;
    };
    let url = &fixture.connection;
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("writer.sock");
    struct Child(std::process::Child);
    impl Drop for Child {
        fn drop(&mut self) {
            let _ = self.0.kill();
            let _ = self.0.wait();
        }
    }
    let mut child = Child(
        Command::new(env!("CARGO_BIN_EXE_atomic"))
            .args(["transactor", "--database", "changes", "--endpoint"])
            .arg(&endpoint)
            .env("ATOMIC_POSTGRES_URL", url)
            .env("ATOMIC_POSTGRES_TRANSPORT", "plaintext")
            .env_remove("ATOMIC_SSD_CACHE_DIR")
            .stdout(Stdio::piped())
            .stderr(Stdio::inherit())
            .spawn()
            .unwrap(),
    );
    let (ready_tx, ready_rx) = std::sync::mpsc::channel();
    let stdout = child.0.stdout.take().unwrap();
    std::thread::spawn(move || {
        let line = BufReader::new(stdout).lines().next().unwrap().unwrap();
        ready_tx.send(line).unwrap();
    });
    assert!(
        ready_rx
            .recv_timeout(Duration::from_secs(15))
            .unwrap()
            .starts_with("READY")
    );
    let observer = Connection::connect(url, "changes", 8).unwrap();
    let submitter = Connection::connect(url, "changes", 8).unwrap();
    let consumer =
        ChangeConsumer::connect(url, "changes", "process", ChangeConsumerConfig::default())
            .unwrap();
    std::thread::sleep(Duration::from_millis(250));
    let idle_before = observer.observation_sql_stats().sql_calls;
    let process_idle_before = process_sql_stats().sql_calls;
    std::thread::sleep(Duration::from_millis(500));
    let idle_calls = observer.observation_sql_stats().sql_calls - idle_before;
    let process_idle_calls = process_sql_stats().sql_calls - process_idle_before;
    assert_eq!(idle_calls, 0, "idle observer still polls PostgreSQL");
    let waiter = std::thread::spawn(move || {
        let mut consumer = consumer;
        let event = consumer.next(Duration::from_secs(5)).unwrap().unwrap();
        (consumer, event)
    });
    std::thread::sleep(Duration::from_millis(100));
    let start = Instant::now();
    let committed = submitter
        .transact_socket(
            &endpoint,
            TransactionRequest::new("process-commit", vec![]),
            Duration::from_secs(10),
        )
        .unwrap();
    let (mut consumer, event) = waiter.join().unwrap();
    let consumer_us = start.elapsed().as_micros();
    assert_eq!(event.transaction.t, committed.basis_t);
    assert!(
        consumer.stats().notices > 0,
        "consumer did not wake from external notification"
    );
    let deadline = Instant::now() + Duration::from_secs(2);
    while observer.db().basis_t() < committed.basis_t {
        assert!(
            Instant::now() < deadline,
            "peer did not observe external commit"
        );
        std::thread::sleep(Duration::from_millis(5));
    }
    let peer_us = start.elapsed().as_micros();
    consumer.acknowledge(&event.checkpoint).unwrap();
    eprintln!(
        "external_notice idle_window_ms=500 idle_observer_sql={idle_calls} reader_process_idle_sql={process_idle_calls} consumer_us={consumer_us} peer_us={peer_us} notices={} gap_checks={} observer_sql={}",
        consumer.stats().notices,
        consumer.stats().gap_checks,
        observer.observation_sql_stats().sql_calls
    );
}
