mod common;
use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, HintLimits, HintPrefetchOptions, IndexPrefix,
    Keyword, OperationContext, OperationKind, Peer, ReadHint, Schema, SpeculationLimits,
    TransactionHints, TransactionRequest, TxForm, TxOp, Value, ValueType,
};
use std::sync::{Arc, atomic::AtomicBool};
use std::time::{Duration, Instant};

// Test-only bounded observation. Production acknowledgement never
// waits for advisory completion or synchronous driver interruption.
fn completed(execution: &atomic_core::HintExecution) -> atomic_core::HintPrefetchStats {
    let deadline = Instant::now() + Duration::from_secs(5);
    loop {
        let stats = execution.snapshot();
        if stats.active_workers == 0 {
            return stats;
        }
        assert!(
            Instant::now() < deadline,
            "hint worker remains in flight: {stats:?}"
        );
        std::thread::sleep(Duration::from_millis(2));
    }
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}
fn add(entity: EntityRef, value: i64) -> TxOp {
    TxOp::Add {
        entity,
        attribute: 1000,
        value: Value::Long(value).into(),
    }
}

#[test]
fn hint_trace_is_bounded_and_does_not_change_pure_with_or_leak_across_calls() {
    let value = Database::new(schema()).unwrap().database_value();
    let forms = vec![TxForm::Op(add(EntityRef::Temp("item".into()), 1))];
    let actual = value
        .with_forms_with_hints(
            &forms,
            1000,
            SpeculationLimits::default(),
            HintLimits {
                max_prefixes: 1,
                max_bytes: 1024,
            },
        )
        .unwrap();
    let expected = value.with_forms(&forms, 1000).unwrap();
    assert_eq!(actual.report.tx_data, expected.tx_data);
    common::assert_same_information(&actual.report.db_after, &expected.db_after);
    assert!(
        actual.hints.is_none(),
        "eager origin has no portable committed coordinate"
    );
    assert!(actual.stats.admitted_prefixes <= 1);
    assert!(actual.stats.retained_bytes <= 1024);
    assert!(actual.stats.prefix_requests > 0);
    let empty = value
        .with_forms_with_hints(
            &forms,
            1000,
            SpeculationLimits::default(),
            HintLimits {
                max_prefixes: 0,
                max_bytes: 0,
            },
        )
        .unwrap();
    assert_eq!(empty.stats.admitted_prefixes, 0);
    assert!(empty.stats.omitted_requests > 0);
    assert_eq!(expected.tx_data, empty.report.tx_data);
}

#[test]
fn postgres_advisory_hints_preserve_identity_retries_staleness_alteration_and_cancel() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED transaction hint PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "hint_semantics");
    let url = &fixture.connection;
    common::install(url).unwrap();
    let created = common::TestStore::connect(url)
        .unwrap()
        .create_database("hints", schema())
        .unwrap();
    let service = common::start_service(url, "hints");
    let first = common::transact(
        &service,
        "seed",
        created.basis_t(),
        &[add(EntityRef::Temp("item".into()), 1)],
        1000,
    );
    let eid = first.tempids["item"];
    service.shutdown();
    common::consolidate(url, "hints").unwrap();
    let peer = Peer::connect(url, "hints", 8).unwrap();
    let before = peer.db();
    let origin = before.snapshot_reference().unwrap();
    let ops = vec![add(EntityRef::Id(eid), 2)];
    let forms = ops.iter().cloned().map(TxForm::Op).collect::<Vec<_>>();
    let generation = OperationContext::new(OperationKind::Application);
    let preview = {
        let _scope = generation.enter();
        before
            .with_forms_with_hints(
                &forms,
                2000,
                SpeculationLimits::default(),
                HintLimits::default(),
            )
            .unwrap()
    };
    let hints = preview.hints.clone().unwrap();
    assert!(!hints.reads().is_empty());
    assert!(hints.retained_bytes() <= HintLimits::default().max_bytes);
    assert_eq!(before.values(eid, 1000).unwrap(), vec![Value::Long(1)]);
    let service = common::start_service(url, "hints");
    let request = TransactionRequest::new("hinted", ops.clone())
        .comparing_basis(before.basis_t())
        .with_tx_instant(2000);
    let phase = OperationContext::new(OperationKind::Application);
    let (ticket, execution) = {
        let _scope = phase.enter();
        service
            .client()
            .submit_with_hints(
                request.clone(),
                hints.clone(),
                HintPrefetchOptions::default(),
            )
            .unwrap()
    };
    let committed = ticket.wait(Duration::from_secs(10)).unwrap();
    completed(&execution);
    assert_eq!(committed.tx_data, preview.report.tx_data);
    common::assert_same_information(&committed.db_after, &preview.report.db_after);
    assert_eq!(execution.snapshot().attempts, 1);
    let replay = service
        .client()
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, committed.tx_hash);

    let altered = TransactionHints::from_reads(
        origin.clone(),
        [
            ReadHint {
                history: false,
                prefix: IndexPrefix::Eavt {
                    entity: eid,
                    attribute: Some(1000),
                    value: Some(Value::Long(-999)),
                },
            },
            ReadHint {
                history: false,
                prefix: IndexPrefix::Aevt {
                    attribute: 123456,
                    entity: None,
                    value: None,
                },
            },
        ],
        HintLimits::default(),
    )
    .unwrap();
    let (ticket, _) = service
        .client()
        .submit_with_hints(
            request.clone(),
            altered.clone(),
            HintPrefetchOptions::default(),
        )
        .unwrap();
    assert_eq!(
        ticket.wait(Duration::from_secs(10)).unwrap().tx_hash,
        committed.tx_hash
    );
    let canceled = HintPrefetchOptions {
        cancel: Arc::new(AtomicBool::new(true)),
        ..Default::default()
    };
    let (ticket, cancel_stats) = service
        .client()
        .submit_with_hints(request, hints.clone(), canceled)
        .unwrap();
    assert_eq!(
        ticket.wait(Duration::from_secs(10)).unwrap().tx_hash,
        committed.tx_hash
    );
    assert_eq!(cancel_stats.snapshot().datoms, 0);
    assert!(cancel_stats.snapshot().canceled_or_limited);

    // Stale basis and inaccurate reads are advisory, never implicit CAS.
    let (ticket, stale) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("stale-hints", vec![add(EntityRef::Id(eid), 3)])
                .with_tx_instant(3000),
            hints,
            HintPrefetchOptions::default(),
        )
        .unwrap();
    let next = ticket.wait(Duration::from_secs(10)).unwrap();
    assert_eq!(
        next.db_after.values(eid, 1000).unwrap(),
        vec![Value::Long(3)]
    );
    assert!(!completed(&stale).ignored_origin);
    let (ticket, _) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("altered-new", vec![add(EntityRef::Id(eid), 4)])
                .with_tx_instant(4000),
            altered,
            HintPrefetchOptions::default(),
        )
        .unwrap();
    let latest = ticket.wait(Duration::from_secs(10)).unwrap();
    assert_eq!(
        latest.db_after.values(eid, 1000).unwrap(),
        vec![Value::Long(4)]
    );
    service.shutdown();
    assert_eq!(
        Peer::connect(url, "hints", 8).unwrap().basis_t(),
        latest.basis_t
    );

    common::TestStore::connect(url)
        .unwrap()
        .create_database("other", schema())
        .unwrap();
    let other = common::start_service(url, "other");
    let foreign = TransactionHints::from_reads(
        origin,
        [ReadHint {
            history: false,
            prefix: IndexPrefix::Aevt {
                attribute: 1000,
                entity: None,
                value: None,
            },
        }],
        HintLimits::default(),
    )
    .unwrap();
    let (ticket, foreign_stats) = other
        .client()
        .submit_with_hints(
            TransactionRequest::new("foreign", vec![add(EntityRef::Temp("new".into()), 9)])
                .with_tx_instant(1000),
            foreign,
            HintPrefetchOptions::default(),
        )
        .unwrap();
    ticket.wait(Duration::from_secs(10)).unwrap();
    assert!(completed(&foreign_stats).ignored_origin);
    other.shutdown();
    eprintln!(
        "hints generated={:?} peer_sql={} execution={:?} transaction_total_sql={} known_retry=true stale=true altered=true canceled=true foreign_ignored=true",
        preview.stats,
        generation.snapshot().sql_calls,
        execution.snapshot(),
        phase.snapshot().sql_calls
    );
}

#[test]
fn blocked_independent_hint_does_not_delay_authority_or_spawn_another_worker() {
    use atomic_core::{
        CallableRef, ErrorCategory, NativeRegistry, PostgresConnectionConfig, ProgramCall,
        SemanticError, ServiceOptions, Symbol, TransactionExecutionOptions, TransactionService,
        TransactionServiceConfig,
    };
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required for blocked advisory witness");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "hint_blocked");
    let url = &fixture.connection;
    common::install(url).unwrap();
    let created = common::TestStore::connect(url)
        .unwrap()
        .create_database("blocked", schema())
        .unwrap();
    // Readers no longer insert session rows. Gate a real reference SELECT
    // instead, using a fixture-only row-security policy. A separate runtime
    // role makes this effective even when the fixture owner is a superuser.
    struct ReadGate {
        control: postgres::Client,
        role: String,
        schema: String,
    }
    impl Drop for ReadGate {
        fn drop(&mut self) {
            let _ = self.control.batch_execute(&format!(
                "REVOKE ALL ON TABLE {schema}.atomic_objects, {schema}.atomic_refs FROM {role}; \
                 REVOKE USAGE ON SCHEMA {schema} FROM {role}; DROP ROLE {role}",
                schema = self.schema,
                role = self.role,
            ));
        }
    }
    let mut control = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let key: i64 = control
        .query_one(
            "SELECT hashtextextended(current_schema() || '/blocked-hint', 123)",
            &[],
        )
        .unwrap()
        .get(0);
    let role = format!("{}_runtime", fixture.schema);
    control
        .batch_execute(&format!(
            r#"
        CREATE ROLE {role};
        GRANT {role} TO CURRENT_USER;
        GRANT USAGE ON SCHEMA {schema} TO {role};
        GRANT SELECT, INSERT, UPDATE, DELETE ON atomic_objects, atomic_refs TO {role};
        CREATE FUNCTION atomic_test_block_hint() RETURNS boolean LANGUAGE plpgsql AS $body$
        BEGIN
            IF current_setting('statement_timeout') = '1s' THEN
                PERFORM pg_advisory_lock({key});
                PERFORM pg_advisory_unlock({key});
            END IF;
            RETURN true;
        END $body$;
        ALTER TABLE atomic_refs ENABLE ROW LEVEL SECURITY;
        CREATE POLICY hint_read ON atomic_refs FOR SELECT USING (atomic_test_block_hint());
        CREATE POLICY runtime_insert ON atomic_refs FOR INSERT WITH CHECK (true);
        CREATE POLICY runtime_update ON atomic_refs FOR UPDATE USING (true) WITH CHECK (true);
        CREATE POLICY runtime_delete ON atomic_refs FOR DELETE USING (true);
    "#,
            schema = fixture.schema,
        ))
        .unwrap();
    let mut gate = ReadGate {
        control,
        role,
        schema: fixture.schema.clone(),
    };
    let runtime_url = if url.starts_with("postgres://") || url.starts_with("postgresql://") {
        format!(
            "{url}&options=-csearch_path%3D{}%2Cpg_catalog%20-crole%3D{}",
            gate.schema, gate.role,
        )
    } else {
        format!(
            "{url} options='-csearch_path={},pg_catalog -crole={}'",
            gate.schema, gate.role,
        )
    };
    let (entered, started_authority) = std::sync::mpsc::sync_channel(1);
    let (release, released) = std::sync::mpsc::sync_channel(1);
    let released = std::sync::Mutex::new(released);
    let function = Symbol::new("hint.fixture.v1", "pause-authority");
    let mut registry = NativeRegistry::builder();
    registry
        .transaction(function.clone(), move |_, _, control| {
            control.check(1)?;
            entered.try_send(()).map_err(|_| {
                SemanticError::new(
                    ErrorCategory::Interrupted,
                    "test/hint-authority-entry",
                    "Fixture authority entry could not be observed",
                )
            })?;
            released
                .lock()
                .unwrap()
                .recv_timeout(Duration::from_secs(10))
                .map_err(|_| {
                    SemanticError::new(
                        ErrorCategory::Interrupted,
                        "test/hint-authority-release",
                        "Fixture authority was not released",
                    )
                })?;
            control.check(1)?;
            Ok(Vec::new())
        })
        .unwrap();
    let service = TransactionService::start_with_options(
        TransactionServiceConfig {
            connection: PostgresConnectionConfig::plaintext(runtime_url),
            database_id: "blocked".into(),
            holder_id: "hint-fixture".into(),
            lease_duration: Duration::from_secs(30),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 32,
            capacity_limits: Default::default(),
        },
        ServiceOptions {
            execution: TransactionExecutionOptions {
                native: registry.build(),
                ..Default::default()
            },
            ..Default::default()
        },
    )
    .unwrap();
    // On failure, disconnect the bounded callback before service Drop joins
    // its worker. No authority row lock may stall pre-dispatch lease renewal.
    let release_authority = release;
    let pause_authority = ProgramCall {
        function: CallableRef::Local(function),
        arguments: vec![],
    };
    let seed = common::transact(
        &service,
        "seed",
        created.basis_t(),
        &[add(EntityRef::Temp("item".into()), 1)],
        1000,
    );
    let eid = seed.tempids["item"];
    let origin = seed.db_after.snapshot_reference().unwrap();
    let hints = TransactionHints::from_reads(
        origin,
        [ReadHint {
            history: true,
            prefix: IndexPrefix::Aevt {
                attribute: 1000,
                entity: None,
                value: None,
            },
        }],
        HintLimits::default(),
    )
    .unwrap();
    let control = &mut gate.control;
    control
        .query_one("SELECT pg_advisory_lock($1)", &[&key])
        .unwrap();
    let options = HintPrefetchOptions {
        timeout: Duration::from_secs(1),
        ..Default::default()
    };
    let (ticket, first) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("while-hint-blocks", vec![add(EntityRef::Id(eid), 2)])
                .with_tx_instant(2000)
                .calling(pause_authority.clone()),
            hints.clone(),
            options.clone(),
        )
        .unwrap();
    started_authority
        .recv_timeout(Duration::from_secs(3))
        .unwrap();
    let wait = Instant::now();
    loop {
        let blocked: bool = control.query_one(
            "SELECT EXISTS (SELECT 1 FROM pg_locks WHERE locktype='advisory' AND NOT granted AND classid::bigint=$1 AND objid::bigint=$2)",
            &[&((key as u64 >> 32) as i64), &((key as u64 & 0xffff_ffff) as i64)]).unwrap().get(0);
        if blocked {
            break;
        }
        assert!(
            wait.elapsed() < Duration::from_millis(800),
            "hint did not enter the injected independent-reader wait: {:?}",
            first.snapshot()
        );
        std::thread::sleep(Duration::from_millis(2));
    }
    release_authority.send(()).unwrap();
    let started = Instant::now();
    let report = ticket.wait(Duration::from_secs(3)).unwrap();
    assert_eq!(
        report.db_after.values(eid, 1000).unwrap(),
        vec![Value::Long(2)]
    );
    assert_eq!(
        first.snapshot().active_workers,
        1,
        "authority must acknowledge while the injected hint call is still blocked"
    );
    assert_eq!(first.snapshot().join_nanos, 0);
    let (ticket, second) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("same-busy-worker", vec![add(EntityRef::Id(eid), 3)])
                .with_tx_instant(3000),
            hints.clone(),
            options.clone(),
        )
        .unwrap();
    ticket.wait(Duration::from_secs(3)).unwrap();
    assert_eq!(second.snapshot().skipped_busy, 1);
    assert_eq!(second.snapshot().active_workers, 0);
    let acknowledgements_us = started.elapsed().as_micros();
    control
        .query_one("SELECT pg_advisory_unlock($1)", &[&key])
        .unwrap();
    let finished = completed(&first);
    assert_eq!(finished.completed_workers, 1);
    assert_eq!(
        finished.datoms, 0,
        "cancellation must prevent reads after blocked setup returns"
    );
    assert!(finished.canceled_or_limited);
    // The callback gate lets prefetch finish before cancellation. A
    // one-byte budget permits one in-flight datom, never another cursor read.
    let (ticket, one_byte) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("one-byte-hint", vec![add(EntityRef::Id(eid), 4)])
                .with_tx_instant(4000)
                .calling(pause_authority),
            hints,
            HintPrefetchOptions {
                max_read_bytes: 1,
                ..options
            },
        )
        .unwrap();
    started_authority
        .recv_timeout(Duration::from_secs(3))
        .unwrap();
    assert_eq!(one_byte.snapshot().attempts, 1);
    let byte_stats = completed(&one_byte);
    assert_eq!(byte_stats.datoms, 1);
    assert!(byte_stats.retained_read_bytes > 1);
    assert_eq!(
        byte_stats.retained_read_bytes,
        byte_stats.max_inflight_datom_bytes
    );
    assert!(byte_stats.canceled_or_limited);
    release_authority.send(()).unwrap();
    ticket.wait(Duration::from_secs(3)).unwrap();
    service.shutdown();
    eprintln!(
        "blocked_hint independent_reference_read=true acknowledged_two_transactions_us={acknowledgements_us} first={finished:?} second={:?} one_byte_budget={byte_stats:?}; no synchronous-call interruption claimed",
        second.snapshot()
    );
}
