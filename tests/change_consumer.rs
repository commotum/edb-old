mod common;
use atomic_core::*;
use std::sync::atomic::AtomicBool;
use std::time::{Duration, Instant};

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    PostgresStore::connect(&fixture.connection)
        .unwrap()
        .create_database("changes", Schema::new())
        .unwrap();
    Some(fixture)
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
    let mut admin = postgres::Client::connect(url, postgres::NoTls).unwrap();
    admin
        .execute(
            "UPDATE atomic_change_checkpoints SET commit_hash=$1 WHERE consumer_name='billing'",
            &[&&[99_u8; 32][..]],
        )
        .unwrap();
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
    let channel: String = admin
        .query_one(
            "SELECT 'atomic_n_' || md5($1 || ':changes')",
            &[&fixture.schema],
        )
        .unwrap()
        .get(0);
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
    let channel: String = admin
        .query_one(
            "SELECT 'atomic_n_' || md5($1 || ':changes')",
            &[&fixture.schema],
        )
        .unwrap()
        .get(0);
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
fn excision_does_not_silently_retarget_a_checkpoint_or_pin_old_history() {
    let Some(fixture) = fixture("change_generation") else {
        return;
    };
    let url = &fixture.connection;
    let writer = common::start_service(url, "changes");
    let seeded = writer
        .client()
        .transact(
            TransactionRequest::new(
                "seed",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("person".into()),
                    attribute: DB_DOC as u32,
                    value: Value::String("remove me".into()).into(),
                }],
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    let removed = seeded.tempids["person"];
    drop(seeded);
    let mut consumer = ChangeConsumer::connect(
        url,
        "changes",
        "pre-excision",
        ChangeConsumerConfig::default(),
    )
    .unwrap();
    let pending = consumer.next(Duration::ZERO).unwrap().unwrap();
    let old_generation = pending.checkpoint.generation();
    writer
        .client()
        .transact(
            TransactionRequest::new(
                "excise",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(removed)),
                }],
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    writer.shutdown();
    PostgresIndexer::connect(url, "changes")
        .unwrap()
        .consolidate()
        .unwrap();
    let receipt = PostgresOperator::connect(url)
        .unwrap()
        .process_excision_requests("changes")
        .unwrap();
    assert!(receipt.generation > old_generation);
    assert_eq!(
        consumer.acknowledge(&pending.checkpoint).unwrap_err().code,
        "consumer/generation-changed"
    );
    assert_eq!(
        ChangeConsumer::connect(
            url,
            "changes",
            "pre-excision",
            ChangeConsumerConfig::default()
        )
        .err()
        .unwrap()
        .code,
        "consumer/generation-changed"
    );
    let fresh = ChangeConsumer::connect(
        url,
        "changes",
        "post-excision",
        ChangeConsumerConfig::default(),
    )
    .unwrap();
    assert_eq!(fresh.checkpoint().generation(), receipt.generation);
    // The still-live old consumer owns no lifetime snapshot/generation pin.
    let mut admin = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let key: i64 = admin
        .query_one(
            "SELECT atomic_log_generation_pin_key($1,$2)",
            &[&"changes", &(old_generation as i64)],
        )
        .unwrap()
        .get(0);
    assert!(
        admin
            .query_one("SELECT pg_try_advisory_lock($1)", &[&key])
            .unwrap()
            .get::<_, bool>(0)
    );
    assert!(
        admin
            .query_one("SELECT pg_advisory_unlock($1)", &[&key])
            .unwrap()
            .get::<_, bool>(0)
    );
}

#[test]
fn restricted_consumers_can_checkpoint_only_their_own_rows_not_canonical_data() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required");
        return;
    };
    let fixture = common::product_support::Fixture::new(&url);
    let Some((writer_role, peer_role)) = &fixture.roles else {
        eprintln!("SKIP: fixture role creation unavailable");
        return;
    };
    let mut migrator = PostgresMigrator::connect(&fixture.admin_url).unwrap();
    migrator.migrate().unwrap();
    migrator
        .grant_runtime_privileges(writer_role, peer_role)
        .unwrap();
    PostgresStore::connect(&fixture.admin_url)
        .unwrap()
        .create_database("changes", Schema::new())
        .unwrap();
    let writer = common::start_service(&fixture.writer_url, "changes");
    writer
        .client()
        .transact(
            TransactionRequest::new("one", vec![]),
            Duration::from_secs(10),
        )
        .unwrap();
    let mut consumer = ChangeConsumer::connect(
        &fixture.peer_url,
        "changes",
        "same-name",
        ChangeConsumerConfig::default(),
    )
    .unwrap();
    let event = consumer.next(Duration::ZERO).unwrap().unwrap();
    consumer.acknowledge(&event.checkpoint).unwrap();
    let other = ChangeConsumer::connect(
        &fixture.writer_url,
        "changes",
        "same-name",
        ChangeConsumerConfig::default(),
    )
    .unwrap();
    assert_eq!(
        other.checkpoint().last_t(),
        0,
        "checkpoint names crossed SQL login boundary"
    );
    let mut peer = postgres::Client::connect(&fixture.peer_url, postgres::NoTls).unwrap();
    assert_eq!(
        peer.execute(
            "UPDATE atomic_change_checkpoints SET revision=99 WHERE checkpoint_owner<>current_user",
            &[]
        )
        .unwrap(),
        0
    );
    assert!(
        peer.execute("DELETE FROM atomic_change_checkpoints", &[])
            .is_err()
    );
    assert!(
        peer.execute("UPDATE atomic_heads SET basis_t=basis_t", &[])
            .is_err()
    );
    assert_eq!(
        peer.query_one("SELECT count(*) FROM atomic_change_checkpoints", &[])
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    drop(other);
    drop(consumer);
    drop(peer);
    writer.shutdown();
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
