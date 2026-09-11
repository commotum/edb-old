mod common;
use atomic_core::*;
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
    mpsc,
};
use std::time::{Duration, Instant};

fn config() -> TelemetryConfig {
    TelemetryConfig {
        enabled: true,
        interval: Duration::from_secs(60),
        ..Default::default()
    }
}
fn snapshot() -> TelemetrySnapshot {
    TelemetrySnapshot::capture(None, TelemetryPhase::Waiting, false)
}
fn wait_for(mut condition: impl FnMut() -> bool) {
    let until = Instant::now() + Duration::from_secs(3);
    while !condition() {
        assert!(
            Instant::now() < until,
            "telemetry worker did not reach expected state"
        );
        std::thread::sleep(Duration::from_millis(1));
    }
}

#[test]
fn disabled_and_not_due_do_not_capture_or_publish() {
    let publisher =
        TelemetryPublisher::start(TelemetryConfig::default(), |_| panic!("disabled sink")).unwrap();
    let emitter = publisher.emitter();
    let started = Instant::now();
    for _ in 0..10_000 {
        assert!(!emitter.publish_if_due(|| panic!("disabled capture")));
    }
    eprintln!(
        "10,000 disabled publication calls {:?}, zero captures",
        started.elapsed()
    );
    assert_eq!(emitter.stats(), TelemetryStats::default());
    assert!(publisher.shutdown(Duration::ZERO));

    let (sent, received) = mpsc::channel();
    let publisher = TelemetryPublisher::start(config(), move |line| {
        sent.send(line.to_owned()).unwrap();
        Ok(())
    })
    .unwrap();
    let emitter = publisher.emitter();
    assert!(emitter.publish_if_due(snapshot));
    assert!(!emitter.publish_if_due(|| panic!("not due")));
    let line = received.recv_timeout(Duration::from_secs(3)).unwrap();
    assert!(line.contains("\"event\":\"atomic.metrics\""));
    assert!(line.contains("\"phase\":\"waiting\""));
    assert!(line.ends_with("}\n"));
    assert!(publisher.shutdown(Duration::from_secs(3)));
}

#[test]
fn bounded_queue_and_stalled_sink_shutdown_have_exact_drop_accounting() {
    let (entered, entry) = mpsc::sync_channel(1);
    let (release, gate) = mpsc::sync_channel(1);
    let publisher = TelemetryPublisher::start(
        TelemetryConfig {
            queue_capacity: 1,
            ..config()
        },
        move |_| {
            entered.send(()).unwrap();
            gate.recv().unwrap();
            Ok(())
        },
    )
    .unwrap();
    let emitter = publisher.emitter();
    assert_eq!(emitter.try_publish(snapshot()), 1);
    entry.recv_timeout(Duration::from_secs(3)).unwrap();
    assert_eq!(emitter.try_publish(snapshot()), 1);
    for _ in 0..128 {
        assert_eq!(emitter.try_publish(snapshot()), 0);
    }
    assert_eq!(emitter.stats().accepted, 2);
    assert_eq!(emitter.stats().dropped_full, 128);
    assert!(
        !publisher.shutdown(Duration::from_millis(10)),
        "blocked callback must not be joined indefinitely"
    );
    let report = OperationContext::new(OperationKind::Query).report();
    assert!(!emitter.publish_operation(&report));
    assert_eq!(emitter.stats().dropped_stopped, 1);
    release.send(()).unwrap();
    wait_for(|| emitter.stats().dropped_shutdown == 1);
    assert_eq!(emitter.stats().delivered, 1);
}

#[test]
fn event_admission_is_exact_and_callback_errors_do_not_kill_publisher() {
    let publisher = TelemetryPublisher::start(
        TelemetryConfig {
            max_event_bytes: 32,
            ..config()
        },
        |_| panic!("oversize sink"),
    )
    .unwrap();
    assert_eq!(publisher.emitter().try_publish(snapshot()), 0);
    assert_eq!(publisher.stats().dropped_oversize, 1);
    assert!(publisher.shutdown(Duration::from_secs(3)));

    let calls = Arc::new(AtomicUsize::new(0));
    let captured = calls.clone();
    let publisher = TelemetryPublisher::start(config(), move |_| {
        match captured.fetch_add(1, Ordering::Relaxed) {
            0 => Err(std::io::Error::other("deliberate sink error")),
            1 => panic!("deliberate sink panic"),
            _ => Ok(()),
        }
    })
    .unwrap();
    let emitter = publisher.emitter();
    for _ in 0..3 {
        assert_eq!(emitter.try_publish(snapshot()), 1);
    }
    wait_for(|| emitter.stats().delivered == 1);
    assert_eq!(emitter.stats().sink_errors, 1);
    assert_eq!(emitter.stats().sink_panics, 1);
    assert!(publisher.shutdown(Duration::from_secs(3)));
}

#[test]
fn readiness_backpressure_and_named_operation_events_are_explicit() {
    let (sent, received) = mpsc::channel();
    let publisher = TelemetryPublisher::start(config(), move |line| {
        sent.send(line.to_owned()).unwrap();
        Ok(())
    })
    .unwrap();
    let emitter = publisher.emitter();
    let mut sample = snapshot();
    sample.phase = TelemetryPhase::Active;
    sample.service = Some(OperationalServiceStats {
        accepting: false,
        queued: 0,
        max_queued: 1,
        processed: 2,
        rejected_full: 1,
    });
    assert_eq!(emitter.try_publish(sample.clone()), 4);
    let lines = (0..4)
        .map(|_| received.recv_timeout(Duration::from_secs(3)).unwrap())
        .collect::<Vec<_>>();
    for name in [
        "atomic.metrics",
        "atomic.warning.writer_unavailable",
        "atomic.warning.not_ready",
        "atomic.warning.backpressure",
    ] {
        assert!(
            lines.iter().any(|line| line.contains(name)),
            "missing {name}"
        );
    }
    sample.phase = TelemetryPhase::Stopping;
    assert_eq!(
        emitter.try_publish(sample),
        1,
        "old rejected counter must not repeat as new pressure"
    );
    received.recv_timeout(Duration::from_secs(3)).unwrap();
    let context =
        OperationContext::named(OperationKind::Query, Keyword::new("app", "lookup")).unwrap();
    let inner = context
        .child_named(OperationKind::Query, Keyword::new("app", "inner"))
        .unwrap();
    inner.measure(|| inner.record_payload_read(1));
    assert!(emitter.publish_operation(&context.report()));
    let line = received.recv_timeout(Duration::from_secs(3)).unwrap();
    assert!(line.contains("\"context\":\":app/lookup\""));
    assert!(line.contains("\"context\":\":app/inner\""));
    assert!(line.contains("\"transaction.identity_lookups\":0"));
    assert!(publisher.shutdown(Duration::from_secs(3)));
}

#[test]
fn invalid_configuration_is_rejected_before_starting_worker() {
    for invalid in [
        TelemetryConfig {
            interval: Duration::ZERO,
            ..config()
        },
        TelemetryConfig {
            queue_capacity: 0,
            ..config()
        },
        TelemetryConfig {
            max_event_bytes: 0,
            ..config()
        },
        TelemetryConfig {
            queue_capacity: usize::MAX,
            max_event_bytes: 2,
            ..config()
        },
    ] {
        assert!(
            matches!(TelemetryPublisher::start(invalid, |_| Ok(())), Err(error) if error.code == "telemetry/invalid-config")
        );
    }
}

#[test]
fn edn_and_json_share_exact_counts_and_escape_explicit_labels() {
    let mut report = OperationContext::new(OperationKind::Query).report();
    report.stats.calls = u64::MAX;
    report.context = Some(Keyword::new("app", "lookup"));
    report.stats.reads.cache.insert(
        CacheTier::DecodedNode,
        CacheIoStats {
            accesses: 2,
            hits: 1,
            misses: 1,
            ..Default::default()
        },
    );
    let edn = io_report_to_edn(&report);
    let text = atomic_core::edn::write_edn(&edn).unwrap();
    assert!(text.contains("18446744073709551615N"));
    assert!(text.contains("cache.DecodedNode"));
    assert_eq!(atomic_core::edn::read_edn(&text).unwrap(), edn);
    // Keywords with escaped characters are not readable EDN labels; the public
    // formatter preserves the AST exactly and the standard writer rejects it.
    // JSON remains valid for such caller-constructed metadata.
    report.context = Some(Keyword::new("app", "quote\"line\nλ"));
    let (sent, received) = mpsc::channel();
    let publisher = TelemetryPublisher::start(config(), move |line| {
        sent.send(line.to_owned()).unwrap();
        Ok(())
    })
    .unwrap();
    assert!(publisher.emitter().publish_operation(&report));
    let line = received.recv_timeout(Duration::from_secs(3)).unwrap();
    assert!(line.contains("\"sql.calls\":18446744073709551615"));
    assert!(line.contains("quote\\\"line\\nλ"));
    assert_eq!(line.lines().count(), 1);
    assert!(publisher.shutdown(Duration::from_secs(3)));
}

#[test]
fn configured_service_keeps_committing_while_telemetry_sink_is_blocked() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP telemetry service: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "telemetry");
    common::install(&fixture.connection).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    DatabaseCatalog::connect(&fixture.connection)
        .unwrap()
        .create_if_absent("events", schema)
        .unwrap();
    let (sent, received) = mpsc::sync_channel(1);
    let (release, gate) = mpsc::sync_channel(1);
    let publisher = TelemetryPublisher::start(
        TelemetryConfig {
            queue_capacity: 1,
            ..config()
        },
        move |line| {
            sent.send(line.to_owned()).unwrap();
            gate.recv().unwrap();
            Ok(())
        },
    )
    .unwrap();
    let emitter = publisher.emitter();
    let service = TransactionService::start_with_options(
        TransactionServiceConfig {
            connection: PostgresConnectionConfig::plaintext(&fixture.connection),
            database_id: "events".into(),
            holder_id: "telemetry-writer".into(),
            lease_duration: Duration::from_secs(10),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 8,
            capacity_limits: CapacityLimits::default(),
        },
        ServiceOptions {
            telemetry: Some(emitter.clone()),
            ..Default::default()
        },
    )
    .unwrap();
    let client = service.client();
    let request = TransactionRequest::new(
        "private-request-key",
        vec![TxOp::Add {
            entity: EntityRef::Temp("private-tempid".into()),
            attribute: 1000,
            value: Value::String("private-subject-value".into()).into(),
        }],
    );
    let first = client
        .submit(request.clone())
        .unwrap()
        .wait(Duration::from_secs(10))
        .unwrap();
    let line = received.recv_timeout(Duration::from_secs(3)).unwrap();
    for secret in [
        "private-request-key",
        "private-tempid",
        "private-subject-value",
        &fixture.connection,
    ] {
        assert!(!line.contains(secret));
    }
    let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let row = sql
        .query_one(
            "SELECT ($1::text::jsonb->>'event'), ($1::text::jsonb->>'basis_t')::bigint",
            &[&line],
        )
        .unwrap();
    assert_eq!(row.get::<_, String>(0), "atomic.transaction");
    assert_eq!(row.get::<_, i64>(1) as u64, first.db_after.basis_t());
    let replay = client
        .submit(request)
        .unwrap()
        .wait(Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    let final_report = client
        .submit(TransactionRequest::new("another-private-key", vec![]))
        .unwrap()
        .wait(Duration::from_secs(10))
        .unwrap();
    assert_eq!(
        final_report.db_after.basis_t(),
        first.db_after.basis_t() + 1
    );
    assert!(emitter.stats().dropped_full >= 1);
    wait_for(|| client.operational_stats().processed == 3);
    let capture = OperationContext::new(OperationKind::Application);
    let sample = {
        let _scope = capture.enter();
        TelemetrySnapshot::capture(Some(&client), TelemetryPhase::Active, true)
    };
    assert_eq!(capture.snapshot().calls, 0, "sampling must not perform SQL");
    assert_eq!(sample.service.as_ref().unwrap().processed, 3);
    assert!(sample.writer_available);
    let mut warned = sample.clone();
    let index = warned.indexing.as_mut().unwrap();
    let failure = BackgroundIndexingFailure {
        category: ErrorCategory::Busy,
        code: "telemetry/test-pause",
        message: "private-error-subject-payload".into(),
    };
    index.last_failure = Some(failure.clone());
    index.fulltext.last_failure = Some(failure.clone());
    index.excision_failure = Some(failure);
    let (warning_tx, warning_rx) = mpsc::channel();
    let warnings = TelemetryPublisher::start(config(), move |line| {
        warning_tx.send(line.to_owned()).unwrap();
        Ok(())
    })
    .unwrap();
    assert_eq!(warnings.emitter().try_publish(warned), 4);
    let warning_lines = (0..4)
        .map(|_| warning_rx.recv_timeout(Duration::from_secs(3)).unwrap())
        .collect::<Vec<_>>();
    for name in [
        "atomic.warning.indexing_failed",
        "atomic.warning.fulltext_failed",
        "atomic.warning.excision_paused",
    ] {
        assert!(warning_lines.iter().any(|line| line.contains(name)));
    }
    for line in warning_lines {
        assert!(!line.contains("private-error-subject-payload"));
        sql.query_one("SELECT $1::text::jsonb", &[&line]).unwrap();
    }
    assert!(warnings.shutdown(Duration::from_secs(3)));
    drop((first, replay, final_report));
    service.shutdown();
    assert!(!publisher.shutdown(Duration::from_millis(10)));
    release.send(()).unwrap();
    wait_for(|| emitter.stats().dropped_shutdown >= 1);
}
