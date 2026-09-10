mod common;
use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, HintLimits, HintPrefetchOptions, IndexPrefix,
    Keyword, OperationContext, OperationKind, Peer, PostgresIndexer, PostgresMigrator,
    PostgresStore, ReadHint, Schema, SpeculationLimits, TransactionHints, TransactionRequest,
    TxForm, TxOp, Value, ValueType,
};
use std::sync::{Arc, atomic::AtomicBool};
use std::time::{Duration, Instant};

// Measurement/test-only bounded observation. Production acknowledgement never
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
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    let created = PostgresStore::connect(url)
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
    PostgresIndexer::connect(url, "hints")
        .unwrap()
        .consolidate()
        .unwrap();
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

    PostgresStore::connect(url)
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
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required for blocked advisory witness");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "hint_blocked");
    let url = &fixture.connection;
    PostgresMigrator::connect(url).unwrap().migrate().unwrap();
    let created = PostgresStore::connect(url)
        .unwrap()
        .create_database("blocked", schema())
        .unwrap();
    let service = common::start_service(url, "blocked");
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
    let mut control = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let key: i64 = control
        .query_one(
            "SELECT hashtextextended(current_schema() || '/blocked-hint', 123)",
            &[],
        )
        .unwrap()
        .get(0);
    control
        .query_one("SELECT pg_advisory_lock($1)", &[&key])
        .unwrap();
    // Scoped fixture injection: only the separately timeout-configured hint
    // session blocks during new-pin setup. Existing writer sessions keep their
    // original function behavior. This is not a driver cancellation witness.
    control.batch_execute(&format!(r#"
        ALTER FUNCTION atomic_log_generation_pin_key(text,bigint)
            RENAME TO atomic_test_original_pin_key;
        CREATE FUNCTION atomic_log_generation_pin_key(candidate_database_id text, candidate_generation bigint)
        RETURNS bigint LANGUAGE plpgsql SET search_path FROM CURRENT AS $body$
        BEGIN
            IF current_setting('statement_timeout') = '1s' THEN
                PERFORM pg_advisory_lock({key});
                PERFORM pg_advisory_unlock({key});
            END IF;
            RETURN atomic_test_original_pin_key(candidate_database_id, candidate_generation);
        END $body$;
    "#)).unwrap();
    let mut authority_gate = postgres::Client::connect(url, postgres::NoTls).unwrap();
    let mut gate = authority_gate.transaction().unwrap();
    gate.query_one(
        "SELECT basis_t FROM atomic_heads WHERE database_id='blocked' FOR UPDATE",
        &[],
    )
    .unwrap();
    let options = HintPrefetchOptions {
        timeout: Duration::from_secs(1),
        ..Default::default()
    };
    let (ticket, first) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("while-hint-blocks", vec![add(EntityRef::Id(eid), 2)])
                .with_tx_instant(2000),
            hints.clone(),
            options.clone(),
        )
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
            "hint did not enter the injected independent-pin wait: {:?}",
            first.snapshot()
        );
        std::thread::sleep(Duration::from_millis(2));
    }
    gate.commit().unwrap();
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
    // Test-only authority gate lets prefetch finish before cancellation. A
    // one-byte budget permits one in-flight datom, never another cursor read.
    let mut gate = authority_gate.transaction().unwrap();
    gate.query_one(
        "SELECT basis_t FROM atomic_heads WHERE database_id='blocked' FOR UPDATE",
        &[],
    )
    .unwrap();
    let (ticket, one_byte) = service
        .client()
        .submit_with_hints(
            TransactionRequest::new("one-byte-hint", vec![add(EntityRef::Id(eid), 4)])
                .with_tx_instant(4000),
            hints,
            HintPrefetchOptions {
                max_read_bytes: 1,
                ..options
            },
        )
        .unwrap();
    let wait = Instant::now();
    while one_byte.snapshot().attempts == 0 {
        assert!(wait.elapsed() < Duration::from_secs(3));
        std::thread::sleep(Duration::from_millis(2));
    }
    let byte_stats = completed(&one_byte);
    assert_eq!(byte_stats.datoms, 1);
    assert!(byte_stats.retained_read_bytes > 1);
    assert_eq!(
        byte_stats.retained_read_bytes,
        byte_stats.max_inflight_datom_bytes
    );
    assert!(byte_stats.canceled_or_limited);
    gate.commit().unwrap();
    ticket.wait(Duration::from_secs(3)).unwrap();
    service.shutdown();
    eprintln!(
        "blocked_hint independent_pin_setup=true acknowledged_two_transactions_us={acknowledgements_us} first={finished:?} second={:?} one_byte_budget={byte_stats:?}; no synchronous-call interruption claimed",
        second.snapshot()
    );
}

#[test]
#[ignore = "manual comparative PostgreSQL hint costs; local samples are not an SLA"]
fn postgres_hint_cold_warm_costs() {
    let url = std::env::var("ATOMIC_POSTGRES_URL").expect("explicit fixture URL required");
    eprintln!(
        "hint_cost_scope round0=first_use_peer_writer_process_caches round1=reused_caches; PostgreSQL/OS buffers are not flushed; two local observations per mode, not a speedup or SLA claim"
    );
    for mode in ["absent", "hinted"] {
        let fixture = common::PostgresFixture::new(&url, "hint_costs");
        let url = &fixture.connection;
        PostgresMigrator::connect(url).unwrap().migrate().unwrap();
        let created = PostgresStore::connect(url)
            .unwrap()
            .create_database("costs", schema())
            .unwrap();
        let service = common::start_service(url, "costs");
        let first = common::transact(
            &service,
            "seed",
            created.basis_t(),
            &(0..128)
                .map(|n| add(EntityRef::Temp(format!("item{n}")), n))
                .collect::<Vec<_>>(),
            1000,
        );
        let eids = first.tempids.values().copied().collect::<Vec<_>>();
        service.shutdown();
        PostgresIndexer::connect(url, "costs")
            .unwrap()
            .with_segment_datoms(8)
            .unwrap()
            .consolidate()
            .unwrap();
        let peer = Peer::connect(url, "costs", 256).unwrap();
        let mut value = peer.db();
        let service = common::start_service(url, "costs");
        for round in 0..2 {
            let ops = eids
                .iter()
                .take(32)
                .map(|eid| add(EntityRef::Id(*eid), 500 + round))
                .collect::<Vec<_>>();
            let generation = OperationContext::new(OperationKind::Application);
            let cache_before = peer.cache_stats();
            let preview_started = Instant::now();
            let (preview, hints, trace) = {
                let _scope = generation.enter();
                let forms = ops.iter().cloned().map(TxForm::Op).collect::<Vec<_>>();
                if mode == "hinted" {
                    let traced = value
                        .with_forms_with_hints(
                            &forms,
                            2000 + round,
                            SpeculationLimits::default(),
                            HintLimits::default(),
                        )
                        .unwrap();
                    (traced.report, traced.hints, Some(traced.stats))
                } else {
                    (
                        value
                            .with_forms_with_limits(
                                &forms,
                                2000 + round,
                                SpeculationLimits::default(),
                            )
                            .unwrap(),
                        None,
                        None,
                    )
                }
            };
            let preview_us = preview_started.elapsed().as_micros();
            let request =
                TransactionRequest::new(format!("round{round}"), ops).with_tx_instant(2000 + round);
            let measured = OperationContext::new(OperationKind::Application);
            let start = Instant::now();
            let (ticket, hint_stats) = {
                let _scope = measured.enter();
                if mode == "hinted" {
                    let (ticket, stats) = service
                        .client()
                        .submit_with_hints(request, hints.unwrap(), HintPrefetchOptions::default())
                        .unwrap();
                    (ticket, Some(stats))
                } else {
                    (service.client().submit(request).unwrap(), None)
                }
            };
            let report = ticket.wait(Duration::from_secs(15)).unwrap();
            assert_eq!(report.tx_data, preview.tx_data);
            let wall_us = start.elapsed().as_micros();
            let after_ack = hint_stats.as_ref().map(|stats| stats.snapshot());
            let final_hints = hint_stats.as_ref().map(completed);
            let writer = service.client().writer_residency_stats();
            if mode == "absent" && round == 0 {
                assert!(
                    generation.snapshot().sql_calls > 0,
                    "first-use peer must actually miss its node cache"
                );
                assert!(
                    writer.last_native_sql_reads > 0,
                    "first-use writer must actually miss its node cache"
                );
            }
            eprintln!(
                "hint_cost mode={mode} round={round} items=128 updates=32 leaf_datoms=8 peer_preview_us={preview_us} trace={trace:?} peer_sql={} wall_us={wall_us} transaction_sql={} connect_calls={} phases={:?} hints_at_ack={after_ack:?} hints_complete={final_hints:?} peer_cache_before={cache_before:?} peer_cache_after={:?} writer={writer:?}",
                generation.snapshot().sql_calls,
                measured.snapshot().sql_calls,
                measured.snapshot().connect_calls,
                measured.snapshot().phases,
                peer.cache_stats()
            );
            value = peer
                .sync_to(report.basis_t, Duration::from_secs(5))
                .unwrap();
        }
        service.shutdown();
    }
}
