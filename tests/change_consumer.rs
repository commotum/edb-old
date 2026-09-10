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
    for t in 1..=3 {
        writer
            .client()
            .transact(
                TransactionRequest::new(format!("tx{t}"), vec![]),
                Duration::from_secs(10),
            )
            .unwrap();
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
    let terminated: bool = admin
        .query_one("SELECT pg_terminate_backend($1)", &[&pid])
        .unwrap()
        .get(0);
    assert!(terminated);
    writer
        .client()
        .transact(
            TransactionRequest::new("while-disconnected", vec![]),
            Duration::from_secs(10),
        )
        .unwrap();
    // Head replay is sufficient even if this exact notice was lost.
    consumer.reconnect().unwrap();
    let event = consumer.next(Duration::from_secs(1)).unwrap().unwrap();
    assert_eq!(event.transaction.t, 1);
    consumer.acknowledge(&event.checkpoint).unwrap();
    assert_eq!(consumer.stats().reconnects, 1);
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
    std::thread::sleep(Duration::from_millis(500));
    let idle_calls = observer.observation_sql_stats().sql_calls - idle_before;
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
        "external_notice idle_window_ms=500 idle_observer_sql={idle_calls} consumer_us={consumer_us} peer_us={peer_us} notices={} gap_checks={} observer_sql={}",
        consumer.stats().notices,
        consumer.stats().gap_checks,
        observer.observation_sql_stats().sql_calls
    );
}
