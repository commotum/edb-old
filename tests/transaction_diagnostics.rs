use atomic_core::*;
use std::sync::{Arc, Mutex};
use std::time::{Duration, Instant};
mod common;

const ID: u32 = 1000;
const YEAR: u32 = 1001;
const REGION: u32 = 1002;
const COMPOSITE: u32 = 1003;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                ID,
                Keyword::new("item", "id"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            YEAR,
            Keyword::new("item", "year"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            REGION,
            Keyword::new("item", "region"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                COMPOSITE,
                Keyword::new("item", "tuple"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Composite(vec![YEAR, REGION])),
        )
        .unwrap();
    schema
}
fn add(attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp("subject-secret-tempid".into()),
        attribute,
        value: value.into(),
    }
}
fn seed() -> Vec<TxOp> {
    vec![
        add(ID, Value::String("subject-secret-value".into())),
        add(YEAR, Value::Long(2020)),
        add(REGION, Value::Long(7)),
    ]
}
fn update() -> Vec<TxOp> {
    vec![
        add(ID, Value::String("subject-secret-value".into())),
        add(YEAR, Value::Long(2021)),
        add(YEAR, Value::Long(2021)),
        add(REGION, Value::Long(7)),
    ]
}
fn seeded() -> Database {
    Database::new(schema())
        .unwrap()
        .with(&seed(), 1)
        .unwrap()
        .db_after
}
fn assert_update(work: TransactionWorkStats, produced: usize) {
    assert_eq!(work.assessments, 1);
    assert_eq!(work.input_operations, 4);
    assert_eq!(work.identity_claims, 1);
    assert_eq!(work.identity_lookups, 1);
    assert_eq!(work.upsert_resolutions, 1);
    assert_eq!(work.duplicate_datoms, 1);
    assert_eq!(work.composite_candidates, 1);
    assert_eq!(work.composite_datoms, 2);
    assert!(work.uniqueness_checks >= 1);
    assert!(work.redundant_datoms >= 2);
    assert_eq!(work.produced_datoms, produced as u64);
    assert!(work.redundancy_checks >= work.produced_datoms + work.redundant_datoms);
}

#[test]
fn eager_and_indexed_assessors_report_actual_semantic_work() {
    let database = seeded();
    let context = OperationContext::diagnostic(OperationKind::Transaction);
    let (eager, io) = context.measure(|| database.with(&update(), 2).unwrap());
    assert_update(io.stats.transaction, eager.tx_data.len());
    let context =
        OperationContext::named(OperationKind::Transaction, Keyword::new("app", "upsert")).unwrap();
    let (native, io) = context.measure(|| database.database_value().with(&update(), 2).unwrap());
    assert_update(io.stats.transaction, native.tx_data.len());
    assert_eq!(eager.tx_data, native.tx_data);
    assert_eq!(eager.tempids, native.tempids);
    let printed = format!("{io:?}");
    assert!(!printed.contains("subject-secret"));
}

#[test]
fn disabled_work_is_zero_and_concurrent_contexts_do_not_mix() {
    let database = seeded();
    let expected = database.with(&update(), 2).unwrap().tx_data;
    for enabled in [false, true] {
        let context = if enabled {
            OperationContext::diagnostic(OperationKind::Transaction)
        } else {
            OperationContext::new(OperationKind::Transaction)
        };
        let started = Instant::now();
        let (_, io) = context.measure(|| {
            for _ in 0..16 {
                let result = database.with(&update(), 2).unwrap();
                assert_eq!(result.tx_data, expected);
                drop(result);
            }
        });
        assert_eq!(
            io.stats.transaction.assessments,
            if enabled { 16 } else { 0 }
        );
        if !enabled {
            assert_eq!(io.stats.transaction, TransactionWorkStats::default());
        }
        eprintln!(
            "transaction diagnostics enabled={enabled}:16 complete with/check/drop calls {:?}",
            started.elapsed()
        );
    }
    let handles = [3, 7].map(|count| {
        let database = database.clone();
        std::thread::spawn(move || {
            let context = OperationContext::diagnostic(OperationKind::Transaction);
            let (_, io) = context.measure(|| {
                for _ in 0..count {
                    drop(database.with(&update(), 2).unwrap());
                }
            });
            assert_eq!(io.stats.transaction.assessments, count);
            assert_eq!(io.stats.transaction.input_operations, count * 4);
        })
    });
    for handle in handles {
        handle.join().unwrap();
    }
}

fn registry(replaced: bool) -> NativeRegistry {
    let mut registry = NativeRegistry::builder();
    registry
        .transaction(Symbol::new("app.v1", "update"), move |_, _, control| {
            control.check(1)?;
            if replaced {
                return Err(SemanticError::conflict("test/replaced", "replaced"));
            }
            Ok(update().into_iter().map(TxForm::Op).collect())
        })
        .unwrap();
    registry.build()
}
fn start(
    connection: &str,
    native: NativeRegistry,
    telemetry: Option<TelemetryEmitter>,
) -> TransactionService {
    TransactionService::start_configured_with_options(
        TransactionServiceConfig {
            connection: String::new(),
            database_id: "diagnostics".into(),
            holder_id: "diagnostic-writer".into(),
            lease_duration: Duration::from_secs(5),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 8,
            capacity_limits: Default::default(),
        },
        PostgresConnectionConfig::plaintext(connection),
        ServiceOptions {
            execution: TransactionExecutionOptions {
                native,
                ..Default::default()
            },
            telemetry,
            ..Default::default()
        },
    )
    .unwrap()
}

#[test]
fn postgres_receipt_stats_and_bounded_sink_correlate_without_replaying_functions() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP transaction diagnostics PG: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "transaction_diagnostics");
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    PostgresStore::connect(&fixture.connection)
        .unwrap()
        .create_database("diagnostics", schema())
        .unwrap();
    let service = start(&fixture.connection, registry(false), None);
    let seeded = service
        .client()
        .transact(
            TransactionRequest::new("secret-seed-key", seed()).with_tx_instant(1),
            Duration::from_secs(10),
        )
        .unwrap();
    assert!(seeded.diagnostics.is_none());
    service.shutdown();
    let lines = Arc::new(Mutex::new(Vec::new()));
    let emitted = lines.clone();
    let publisher = TelemetryPublisher::start(
        TelemetryConfig {
            enabled: true,
            ..Default::default()
        },
        move |line| {
            emitted.lock().unwrap().push(line.to_owned());
            Ok(())
        },
    )
    .unwrap();
    let service = start(
        &fixture.connection,
        registry(false),
        Some(publisher.emitter()),
    );
    let request = TransactionRequest::from_forms(
        "subject-secret-request-key",
        vec![TxForm::ProgramCall(ProgramCall {
            function: CallableRef::Local(Symbol::new("app.v1", "update")),
            arguments: vec![],
        })],
    )
    .with_tx_instant(2);
    let started = Instant::now();
    let label = Keyword::new("billing", "update-account");
    let context = OperationContext::named(OperationKind::Application, label.clone()).unwrap();
    let committed = {
        let _scope = context.enter();
        service
            .client()
            .transact(request.clone(), Duration::from_secs(10))
            .unwrap()
    };
    let diagnostics = committed.diagnostics.as_ref().unwrap();
    assert_eq!(diagnostics.report.context.as_ref(), Some(&label));
    assert_eq!(diagnostics.basis_t, committed.basis_t);
    assert!(!diagnostics.replayed);
    assert_update(
        diagnostics.report.stats.transaction,
        committed.tx_data.len(),
    );
    assert_eq!(diagnostics.report.stats.transaction.function_calls, 1);
    assert!(
        diagnostics
            .report
            .stats
            .phases
            .contains_key(&OperationKind::TransactionExpansion)
    );
    assert!(
        diagnostics
            .report
            .stats
            .phases
            .contains_key(&OperationKind::TransactionAssessment)
    );
    service.shutdown();
    let service = start(
        &fixture.connection,
        registry(true),
        Some(publisher.emitter()),
    );
    let replayed = service
        .client()
        .transact(request, Duration::from_secs(10))
        .unwrap();
    assert!(replayed.replayed);
    assert_eq!(replayed.tx_hash, committed.tx_hash);
    assert_eq!(replayed.tx_data, committed.tx_data);
    let diagnostics = replayed.diagnostics.as_ref().unwrap();
    assert_eq!(diagnostics.basis_t, committed.basis_t);
    assert!(diagnostics.replayed);
    assert_eq!(
        diagnostics.report.stats.transaction,
        TransactionWorkStats::default()
    );
    let context = OperationContext::diagnostic(OperationKind::Transaction);
    let (failed, io) = context.measure(|| {
        service.client().transact(
            TransactionRequest::from_forms(
                "new-replaced-call",
                vec![TxForm::ProgramCall(ProgramCall {
                    function: CallableRef::Local(Symbol::new("app.v1", "update")),
                    arguments: vec![],
                })],
            ),
            Duration::from_secs(10),
        )
    });
    assert_eq!(failed.unwrap_err().code, "test/replaced");
    assert_eq!(io.stats.transaction.function_calls, 1);
    assert_eq!(io.stats.transaction.assessments, 0);
    service.shutdown();
    let deadline = Instant::now() + Duration::from_secs(5);
    loop {
        if lines.lock().unwrap().len() >= 2 {
            break;
        }
        assert!(
            Instant::now() < deadline,
            "transaction events were not delivered"
        );
        std::thread::sleep(Duration::from_millis(5));
    }
    assert!(publisher.shutdown(Duration::from_secs(2)));
    let text = lines.lock().unwrap().join("");
    assert!(text.contains("atomic.transaction"));
    assert!(text.contains("\"context\":\":billing/update-account\""));
    assert!(text.contains(&format!("\"basis_t\":{}", committed.basis_t)));
    assert!(text.contains("\"replayed\":true"));
    assert!(!text.contains("subject-secret"));
    assert!(!text.contains("secret-seed-key"));
    drop((committed, replayed, seeded));
    eprintln!(
        "transaction diagnostic commit/restart/replay/check/drop {:?}",
        started.elapsed()
    );
}
