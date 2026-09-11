use atomic_core::*;
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::{Duration, Instant};
mod common;

const SCORE: u32 = 1000;
const BEFORE: u32 = 1001;
const MARKER: u32 = 1002;
fn name(local: &str) -> Symbol {
    Symbol::new("app.tx.v1", local)
}
fn scalar(value: i64) -> RuntimeValue {
    RuntimeValue::Scalar(Value::Long(value))
}
fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}
fn call(local: &str, entity: u64, next: i64) -> TxForm {
    TxForm::ProgramCall(ProgramCall {
        function: CallableRef::Local(name(local)),
        arguments: vec![RuntimeValue::Scalar(Value::Ref(entity)), scalar(next)],
    })
}
fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            SCORE,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            BEFORE,
            Keyword::new("item", "before"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema.install(native_deployment_attribute(MARKER)).unwrap();
    schema
}
fn arguments(args: &[RuntimeValue]) -> Result<(u64, i64), SemanticError> {
    match args {
        [
            RuntimeValue::Scalar(Value::Ref(entity)),
            RuntimeValue::Scalar(Value::Long(next)),
        ] => Ok((*entity, *next)),
        _ => Err(SemanticError::incorrect(
            "test/arguments",
            "expected entity and next score",
        )),
    }
}
fn registry(calls: Arc<AtomicUsize>) -> NativeRegistry {
    let mut builder = NativeRegistry::builder();
    let observed = calls.clone();
    builder
        .transaction(name("increment"), move |db, args, control| {
            control.check(1)?;
            observed.fetch_add(1, Ordering::SeqCst);
            let (entity, next) = arguments(args)?;
            let old_values = db.values(entity, SCORE)?;
            let [Value::Long(old)] = old_values.as_slice() else {
                panic!("missing score")
            };
            Ok(vec![
                TxForm::Op(add(EntityRef::Id(entity), BEFORE, Value::Long(*old))),
                call("inner", entity, next),
            ])
        })
        .unwrap();
    let observed = calls.clone();
    builder
        .transaction(name("inner"), move |db, args, control| {
            control.check(1)?;
            observed.fetch_add(1, Ordering::SeqCst);
            let (entity, next) = arguments(args)?;
            assert_eq!(
                db.values(entity, BEFORE)?.len(),
                0,
                "nested call must see db-before, not preceding returned forms"
            );
            Ok(vec![
                TxForm::Op(add(EntityRef::Id(entity), SCORE, Value::Long(next))),
                TxForm::Op(TxOp::Ensure {
                    entity: EntityRef::Id(entity),
                    spec: EntityRef::Ident(Keyword::new("checks", "spec")),
                }),
            ])
        })
        .unwrap();
    builder
        .attribute_predicate(name("positive"), |value, control| {
            control.check(1)?;
            Ok(RuntimeValue::Scalar(Value::Bool(
                matches!(value, Value::Long(n) if *n > 0),
            )))
        })
        .unwrap();
    builder.entity_predicate(name("after"), |db, entity, control| {
        control.check(1)?;
        let score = db.values(entity, SCORE)?;
        let old = db.values(entity, BEFORE)?;
        Ok(RuntimeValue::Scalar(Value::Bool(matches!((score.as_slice(),old.as_slice()), ([Value::Long(next)], [Value::Long(old)]) if *next == old + 1))))
    }).unwrap();
    builder
        .transaction(name("panic"), |_, _, _| panic!("contained native callback"))
        .unwrap();
    builder
        .transaction(name("cancel"), |_, _, _| {
            Err(
                SemanticError::conflict("test/cancel", "application canceled")
                    .detail("app/reason", "policy"),
            )
        })
        .unwrap();
    builder
        .transaction(name("large"), |_, args, control| {
            control.reserve(10_000)?;
            let (entity, _) = arguments(args)?;
            Ok(vec![TxForm::Op(add(
                EntityRef::Id(entity),
                SCORE,
                Value::Long(9),
            ))])
        })
        .unwrap();
    builder
        .transaction(name("ignore-work"), |_, _, control| {
            let _ = control.check(control.remaining_work() + 1);
            Ok(vec![])
        })
        .unwrap();
    builder
        .transaction(name("ignore-memory"), |_, _, control| {
            let _ = control.reserve(usize::MAX);
            Ok(vec![])
        })
        .unwrap();
    builder.build()
}
fn options(registry: NativeRegistry) -> TransactionExecutionOptions {
    TransactionExecutionOptions {
        native: registry,
        ..Default::default()
    }
}
fn bindings() -> Vec<TxForm> {
    let mut forms = vec![TxForm::Op(add(
        EntityRef::Temp("item".into()),
        SCORE,
        Value::Long(7),
    ))];
    for (temp, local) in [("positive", "positive"), ("after", "after")] {
        forms.push(TxForm::Op(add(
            EntityRef::Temp(temp.into()),
            DB_IDENT as u32,
            Value::Keyword(Keyword::new("checks", temp)),
        )));
        forms.push(TxForm::Op(add(
            EntityRef::Temp(temp.into()),
            MARKER,
            Value::Symbol(name(local)),
        )));
    }
    forms.push(TxForm::Op(add(
        EntityRef::Temp("spec".into()),
        DB_IDENT as u32,
        Value::Keyword(Keyword::new("checks", "spec")),
    )));
    forms.push(TxForm::Op(add(
        EntityRef::Temp("spec".into()),
        DB_ENTITY_PREDS as u32,
        Value::Symbol(Symbol::new("checks", "after")),
    )));
    let mut attr = schema().attribute(SCORE).unwrap().clone();
    attr.predicates = vec!["checks/positive".into()];
    forms.push(TxForm::Op(TxOp::AlterAttribute(attr)));
    forms
}
fn speculative_fixture() -> (
    DatabaseValue,
    u64,
    TransactionExecutionOptions,
    Arc<AtomicUsize>,
) {
    let calls = Arc::new(AtomicUsize::new(0));
    let options = options(registry(calls.clone()));
    let initial = Database::new(schema()).unwrap().database_value();
    let report = initial
        .with_forms_with_execution_options(&bindings(), 1, Default::default(), &options)
        .unwrap();
    (report.db_after, report.tempids["item"], options, calls)
}

#[test]
fn exact_speculation_shares_db_before_and_checks_complete_db_after() {
    let (db, entity, options, calls) = speculative_fixture();
    let report = db
        .with_forms_with_execution_options(
            &[call("increment", entity, 8)],
            2,
            Default::default(),
            &options,
        )
        .unwrap();
    assert_eq!(calls.load(Ordering::SeqCst), 2);
    assert_eq!(db.values(entity, SCORE).unwrap(), vec![Value::Long(7)]);
    assert_eq!(
        report.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(8)]
    );
    assert_eq!(
        report.db_after.values(entity, BEFORE).unwrap(),
        vec![Value::Long(7)]
    );
    let error = db
        .with_forms_with_execution_options(
            &[call("increment", entity, 10)],
            2,
            Default::default(),
            &options,
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/entity-predicate");
    let error = db
        .with_forms_with_execution_options(
            &[call("increment", entity, -1)],
            2,
            Default::default(),
            &options,
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/attribute-predicate");
}

#[test]
fn deployment_identity_limits_panics_and_application_errors_are_explicit() {
    let mut builder = NativeRegistry::builder();
    assert_eq!(
        builder
            .transaction(Symbol::new("app", "unversioned"), |_, _, _| Ok(vec![]))
            .err()
            .unwrap()
            .code,
        "native/invalid-deployment-name"
    );
    builder
        .transaction(name("once"), |_, _, _| Ok(vec![]))
        .unwrap();
    assert_eq!(
        builder
            .transaction(name("once"), |_, _, _| Ok(vec![]))
            .err()
            .unwrap()
            .code,
        "native/duplicate-deployment"
    );
    let (db, entity, options, _) = speculative_fixture();
    for (local, code) in [
        ("panic", "native/callback-panic"),
        ("cancel", "test/cancel"),
        ("absent", "native/deployment-not-found"),
        ("ignore-work", "program/fuel-exhausted"),
        ("ignore-memory", "program/value-byte-limit"),
    ] {
        let error = db
            .with_forms_with_execution_options(
                &[call(local, entity, 8)],
                2,
                Default::default(),
                &options,
            )
            .unwrap_err();
        assert_eq!(error.code, code);
        if local == "cancel" {
            assert_eq!(error.details["app/reason"], "policy");
        }
    }
    let mut limits = SpeculationLimits::default();
    limits.program.max_calls = 1;
    assert_eq!(
        db.with_forms_with_execution_options(&[call("increment", entity, 8)], 2, limits, &options)
            .unwrap_err()
            .code,
        "program/call-limit"
    );
    limits = SpeculationLimits::default();
    limits.program.max_forms = 1;
    assert_eq!(
        db.with_forms_with_execution_options(&[call("increment", entity, 8)], 2, limits, &options)
            .unwrap_err()
            .code,
        "program/form-limit"
    );
    limits = SpeculationLimits::default();
    limits.program.max_value_bytes = 1000;
    assert_eq!(
        db.with_forms_with_execution_options(&[call("large", entity, 8)], 2, limits, &options)
            .unwrap_err()
            .code,
        "program/value-byte-limit"
    );
    limits = SpeculationLimits::default();
    limits.program.fuel = 1;
    assert_eq!(
        db.with_forms_with_execution_options(&[call("increment", entity, 8)], 2, limits, &options)
            .unwrap_err()
            .code,
        "program/fuel-exhausted"
    );
    assert!(
        db.with_forms_with_execution_options(
            &[call("increment", entity, 8)],
            2,
            Default::default(),
            &options
        )
        .is_ok()
    );
}

#[test]
fn missing_stored_binding_is_not_reinterpreted_as_native_and_marker_is_exclusive() {
    let (db, entity, options, _) = speculative_fixture();
    let remove = TxForm::Op(TxOp::Retract {
        entity: EntityRef::Ident(Keyword::new("checks", "positive")),
        attribute: MARKER,
        value: Some(Value::Symbol(name("positive")).into()),
    });
    let error = db
        .with_forms_with_execution_options(&[remove], 2, Default::default(), &options)
        .unwrap_err();
    assert_eq!(error.code, "program/not-a-database-function");
    let both = TxForm::Op(add(
        EntityRef::Ident(Keyword::new("checks", "positive")),
        DB_FN as u32,
        Value::Function([42; 32]),
    ));
    assert_eq!(
        db.with_forms_with_execution_options(&[both], 2, Default::default(), &options)
            .unwrap_err()
            .code,
        "native/ambiguous-binding"
    );
    assert_eq!(
        db.with_forms(&[call("increment", entity, 8)], 2)
            .unwrap_err()
            .code,
        "native/deployment-not-found"
    );
}

#[test]
fn legacy_ident_collision_is_not_a_global_gate_and_active_marker_schema_is_checked() {
    let mut legacy = Schema::new();
    legacy
        .install(Attribute::new(
            SCORE,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    legacy
        .install(Attribute::new(
            BEFORE,
            Keyword::new("item", "before"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    legacy
        .install(Attribute::new(
            MARKER,
            native_deployment_ident(),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let db = Database::new(legacy).unwrap().database_value();
    let initial = db
        .with(
            &[
                add(
                    EntityRef::Temp("legacy".into()),
                    MARKER,
                    Value::String("ordinary old application data".into()),
                ),
                add(
                    EntityRef::Temp("legacy".into()),
                    DB_IDENT as u32,
                    Value::Keyword(Keyword::new("checks", "legacy")),
                ),
                add(EntityRef::Temp("item".into()), SCORE, Value::Long(1)),
            ],
            1,
        )
        .unwrap();
    let entity = initial.tempids["item"];
    let next = initial
        .db_after
        .with(&[add(EntityRef::Id(entity), SCORE, Value::Long(2))], 2)
        .unwrap();
    assert_eq!(
        next.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(2)]
    );
    let mut attr = next.db_after.schema().attribute(SCORE).unwrap().clone();
    attr.predicates = vec!["checks/legacy".into()];
    assert_eq!(
        next.db_after
            .with(&[TxOp::AlterAttribute(attr)], 3)
            .unwrap_err()
            .code,
        "native/invalid-marker-schema"
    );

    let (db, _, options, _) = speculative_fixture();
    let renamed = db
        .with_forms_with_execution_options(
            &[TxForm::Op(add(
                EntityRef::Id(MARKER.into()),
                DB_IDENT as u32,
                Value::Keyword(Keyword::new("app", "deployments")),
            ))],
            2,
            Default::default(),
            &options,
        )
        .unwrap();
    assert_eq!(
        renamed.db_after.entid(&native_deployment_ident()),
        Some(MARKER.into()),
        "retained ident alias preserves native binding meaning"
    );
    let mut marker = db.schema().attribute(MARKER).unwrap().clone();
    marker.no_history = true;
    assert_eq!(
        db.with_forms_with_execution_options(
            &[TxForm::Op(TxOp::AlterAttribute(marker))],
            2,
            Default::default(),
            &options
        )
        .unwrap_err()
        .code,
        "native/invalid-marker-schema"
    );
}

fn service(connection: &str, native: NativeRegistry) -> TransactionService {
    TransactionService::start_with_execution_options(
        TransactionServiceConfig {
            connection: connection.to_owned(),
            database_id: "native-functions".into(),
            holder_id: "native-host".into(),
            lease_duration: Duration::from_secs(5),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 16,
            capacity_limits: Default::default(),
        },
        options(native),
    )
    .unwrap()
}

#[test]
fn postgres_native_host_matches_speculation_and_retries_without_old_deployment_after_restart() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let started = Instant::now();
    let fixture = common::PostgresFixture::new(&url, "native_functions");
    common::install(&fixture.connection).unwrap();
    let mut store = common::TestStore::connect(&fixture.connection).unwrap();
    store.create_database("native-functions", schema()).unwrap();
    let calls = Arc::new(AtomicUsize::new(0));
    let registry = registry(calls.clone());
    let writer = service(&fixture.connection, registry.clone());
    let install = writer
        .client()
        .transact(
            TransactionRequest::from_forms("bindings", bindings()).with_tx_instant(1),
            Duration::from_secs(10),
        )
        .unwrap();
    let entity = install.tempids["item"];
    let before = install.db_after.clone();
    let forms = vec![call("increment", entity, 8)];
    let expected = before
        .with_forms_with_execution_options(&forms, 2, Default::default(), &options(registry))
        .unwrap();
    let request = TransactionRequest::from_forms("native-call", forms).with_tx_instant(2);
    let operation = Instant::now();
    let committed = writer
        .client()
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    assert_eq!(committed.tx_data, expected.tx_data);
    assert_eq!(committed.tempids, expected.tempids);
    assert_eq!(
        committed.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(8)]
    );
    assert_eq!(calls.load(Ordering::SeqCst), 4);
    let complete_commit = operation.elapsed();
    writer.shutdown();
    let writer = service(&fixture.connection, NativeRegistry::default());
    let replay = writer
        .client()
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_data, committed.tx_data);
    assert_eq!(replay.tempids, committed.tempids);
    assert_eq!(
        replay.db_before.values(entity, SCORE).unwrap(),
        vec![Value::Long(7)]
    );
    assert_eq!(
        replay.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(8)]
    );
    assert_eq!(calls.load(Ordering::SeqCst), 4);
    assert_eq!(
        writer
            .client()
            .transact(
                TransactionRequest::from_forms("new-missing", vec![call("increment", entity, 9)]),
                Duration::from_secs(10)
            )
            .unwrap_err()
            .code,
        "native/deployment-not-found"
    );
    writer.shutdown();
    let mut changed = NativeRegistry::builder();
    changed
        .transaction(name("increment"), |_, _, _| {
            Err(SemanticError::conflict(
                "test/replaced",
                "new host implementation",
            ))
        })
        .unwrap();
    let writer = service(&fixture.connection, changed.build());
    let replay = writer
        .client()
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    assert_eq!(
        writer
            .client()
            .transact(
                TransactionRequest::from_forms("new-replaced", vec![call("increment", entity, 9)]),
                Duration::from_secs(10)
            )
            .unwrap_err()
            .code,
        "test/replaced"
    );
    writer.shutdown();
    let backup_dir = tempfile::tempdir().unwrap();
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        std::fs::set_permissions(backup_dir.path(), std::fs::Permissions::from_mode(0o700))
            .unwrap();
    }
    let point = PortableBackup::connect(&fixture.connection)
        .unwrap()
        .backup_database("native-functions", backup_dir.path())
        .unwrap();
    PortableBackup::verify_backup(backup_dir.path(), point.basis_t, true).unwrap();
    let target = common::PostgresFixture::new(&url, "native_restore");
    common::install(&target.connection).unwrap();
    PortableBackup::connect(&target.connection)
        .unwrap()
        .restore_backup(backup_dir.path(), point.basis_t, "native-functions")
        .unwrap();
    common::consolidate(&target.connection, "native-functions").unwrap();
    let restored = Connection::connect(&target.connection, "native-functions", 8).unwrap();
    let restored_db = restored.db();
    let predicate = restored_db
        .entid(&Keyword::new("checks", "positive"))
        .unwrap();
    assert_eq!(
        restored_db.values(predicate, MARKER).unwrap(),
        vec![Value::Symbol(name("positive"))]
    );
    assert!(
        restored_db
            .values(predicate, DB_FN as u32)
            .unwrap()
            .is_empty()
    );
    let writer = service(&target.connection, NativeRegistry::default());
    let retry = writer
        .client()
        .transact(request, Duration::from_secs(10))
        .unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.tx_data, committed.tx_data);
    assert_eq!(
        retry.db_before.values(entity, SCORE).unwrap(),
        vec![Value::Long(7)]
    );
    assert_eq!(
        retry.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(8)]
    );
    assert_eq!(
        calls.load(Ordering::SeqCst),
        4,
        "backup/deep verification/restore/retry never executes external code"
    );
    writer.shutdown();
    drop((
        retry,
        restored_db,
        restored,
        expected,
        before,
        install,
        replay,
        committed,
        store,
    ));
    eprintln!(
        "native complete commit/check={complete_commit:?}; fixture/install/speculate/commit/two-restarts/backup/deep-verify/restore/retry/check/report-drop={:?}",
        started.elapsed()
    );
}
