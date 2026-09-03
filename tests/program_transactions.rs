mod common;

use atomic_core::{
    Attribute, CallableRef, Cardinality, DB_ATTR_PREDS, DB_ENSURE, DB_ENTITY_ATTRS,
    DB_ENTITY_PREDS, DB_FN, DB_IDENT, EntityRef, ErrorCategory, IndexOrder, Instruction, Keyword,
    PostgresStore, Program, ProgramCall, ProgramHash, ProgramKind, Schema, SemanticError,
    ServiceTransactionReport, Symbol, TransactionRequest, TransactionService, TxForm, TxOp,
    TxValue, USER_PARTITION, Value, ValueType, View, make_eid,
};
use postgres::{Client, NoTls};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const BALANCE: u32 = 1_000;
const SNAPSHOT: u32 = 1_001;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn schema_without_predicates() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            BALANCE,
            Keyword::new("account", "balance"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            SNAPSHOT,
            Keyword::new("account", "snapshot"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn function_binding(tempid: &str, ident: Keyword, hash: ProgramHash) -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Temp(tempid.into()),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(ident).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp(tempid.into()),
            attribute: DB_FN as u32,
            value: Value::Function(hash).into(),
        },
    ]
}

fn database_call(ident: Keyword, arguments: Vec<Value>) -> ProgramCall {
    ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(ident)),
        arguments: arguments.into_iter().map(Into::into).collect(),
    }
}

fn transact_calls(
    service: &TransactionService,
    request_key: &str,
    compare_basis_t: u64,
    operations: Vec<TxOp>,
    calls: Vec<ProgramCall>,
    tx_instant: i64,
) -> Result<ServiceTransactionReport, SemanticError> {
    service.client().transact(
        TransactionRequest::new(request_key, operations)
            .calling_all(calls)
            .comparing_basis(compare_basis_t)
            .with_tx_instant(tx_instant),
        Duration::from_secs(5),
    )
}

fn positive() -> Program {
    Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(0)),
            Instruction::GreaterThan,
            Instruction::Return,
        ],
    }
}

fn set_balance() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushArgument(1),
            Instruction::EmitAdd(BALANCE),
            Instruction::Return,
        ],
    }
}

fn snapshot_balance() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::Duplicate,
            Instruction::LoadOne(BALANCE),
            Instruction::EmitAdd(SNAPSHOT),
            Instruction::Return,
        ],
    }
}

fn ordered_balance_predicate() -> Program {
    Program {
        kind: ProgramKind::EntityPredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(BALANCE),
            Instruction::PushArgument(0),
            Instruction::LoadOne(SNAPSHOT),
            Instruction::GreaterThan,
            Instruction::Not,
            Instruction::Return,
        ],
    }
}

#[test]
fn persisted_entity_spec_predicate_validates_complete_db_after_via_service() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("entity_spec_predicate");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store
        .create_database(&database_id, schema_without_predicates())
        .unwrap();
    let positive_hash = store.deploy_program_blob(&positive()).unwrap();
    let ordered_hash = store
        .deploy_program_blob(&ordered_balance_predicate())
        .unwrap();
    drop(store);

    let service = common::start_service(&connection, &database_id);
    let guard_ident = Keyword::new("account", "ordered");
    let spec = common::transact(
        &service,
        "install-entity-spec",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("positive-function".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("atomic.predicates", "positive")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("positive-function".into()),
                attribute: DB_FN as u32,
                value: Value::Function(positive_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(u64::from(BALANCE)),
                attribute: DB_ATTR_PREDS as u32,
                value: Value::Symbol(Symbol::new("atomic.predicates", "positive")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("entity-predicate".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("account", "balance-ordered?")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("entity-predicate".into()),
                attribute: DB_FN as u32,
                value: Value::Function(ordered_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(guard_ident.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_ENTITY_ATTRS as u32,
                value: Value::Keyword(Keyword::new("account", "balance")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_ENTITY_ATTRS as u32,
                value: Value::Keyword(Keyword::new("account", "snapshot")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_ENTITY_PREDS as u32,
                value: Value::Symbol(Symbol::new("account", "balance-ordered?")).into(),
            },
        ],
        1_000,
    );
    let ensured = |low, high| {
        vec![
            TxOp::Add {
                entity: EntityRef::Id(user(42)),
                attribute: BALANCE,
                value: Value::Long(low).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(user(42)),
                attribute: SNAPSHOT,
                value: Value::Long(high).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(user(42)),
                attribute: DB_ENSURE as u32,
                value: TxValue::Entity(EntityRef::Ident(guard_ident.clone())),
            },
        ]
    };
    let error = common::try_transact(
        &service,
        "invalid-entity",
        spec.basis_t,
        &ensured(100, 20),
        2_000,
    )
    .unwrap_err();
    assert_eq!(error.code, "transaction/entity-predicate");
    let valid = common::transact(
        &service,
        "valid-entity",
        spec.basis_t,
        &ensured(20, 100),
        2_000,
    );
    assert!(
        valid
            .db_after
            .datoms(View::History, IndexOrder::Eavt)
            .iter()
            .all(|datom| datom.attribute != DB_ENSURE as u32)
    );
    service.shutdown();

    // A fresh transactor process boundary resolves both predicate functions
    // from the recovered temporal :db/fn bindings and validates db-after.
    let restarted = common::start_service(&connection, &database_id);
    let error = common::try_transact(
        &restarted,
        "invalid-after-restart",
        valid.basis_t,
        &ensured(200, 10),
        3_000,
    )
    .unwrap_err();
    assert_eq!(error.code, "transaction/entity-predicate");
    restarted.shutdown();
}

#[test]
fn persisted_functions_compose_on_db_before_and_predicates_guard_commit() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("program_tx");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store
        .create_database(&database_id, schema_without_predicates())
        .unwrap();
    let positive_hash = store.deploy_program_blob(&positive()).unwrap();
    let setter_hash = store.deploy_program_blob(&set_balance()).unwrap();
    let snapshot_hash = store.deploy_program_blob(&snapshot_balance()).unwrap();
    drop(store);

    let positive_ident = Keyword::new("atomic.predicates", "positive");
    let setter_ident = Keyword::new("account", "set-balance");
    let snapshot_ident = Keyword::new("account", "snapshot-balance");
    let mut bindings = function_binding("positive-function", positive_ident.clone(), positive_hash);
    bindings.extend(function_binding(
        "setter-function",
        setter_ident.clone(),
        setter_hash,
    ));
    bindings.extend(function_binding(
        "snapshot-function",
        snapshot_ident.clone(),
        snapshot_hash,
    ));
    bindings.push(TxOp::Add {
        entity: EntityRef::Id(u64::from(BALANCE)),
        attribute: DB_ATTR_PREDS as u32,
        value: Value::Symbol(Symbol::new("atomic.predicates", "positive")).into(),
    });

    let service = common::start_service(&connection, &database_id);
    let installed = common::transact(
        &service,
        "install-functions",
        created.basis_t(),
        &bindings,
        1_000,
    );
    let initial = common::transact(
        &service,
        "initial",
        installed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: BALANCE,
            value: Value::Long(10).into(),
        }],
        2_000,
    );
    assert_eq!(
        initial.db_after.values(user(42), BALANCE),
        vec![&Value::Long(10)]
    );

    let composed = transact_calls(
        &service,
        "composed",
        initial.basis_t,
        vec![],
        vec![
            database_call(
                setter_ident.clone(),
                vec![Value::Ref(user(42)), Value::Long(99)],
            ),
            database_call(snapshot_ident, vec![Value::Ref(user(42))]),
        ],
        3_000,
    )
    .unwrap();
    assert_eq!(
        composed.db_after.values(user(42), BALANCE),
        vec![&Value::Long(99)]
    );
    assert_eq!(
        composed.db_after.values(user(42), SNAPSHOT),
        vec![&Value::Long(10)]
    );

    let error = transact_calls(
        &service,
        "invalid",
        composed.basis_t,
        vec![],
        vec![database_call(
            setter_ident,
            vec![Value::Ref(user(42)), Value::Long(-1)],
        )],
        4_000,
    )
    .unwrap_err();
    assert_eq!(error.code, "transaction/attribute-predicate");
    assert_eq!(error.details["entity"], user(42).to_string());
    assert_eq!(error.details["attribute"], "account/balance");
    assert_eq!(error.details["value"], "Long(-1)");
    assert_eq!(error.details["predicate"], "atomic.predicates/positive");
    assert_eq!(error.details["pred_return"], "Scalar(Bool(false))");
    service.shutdown();

    let mut restarted = PostgresStore::connect(&connection).unwrap();
    assert_eq!(
        restarted.recover(&database_id).unwrap().basis_t(),
        composed.basis_t
    );
}

#[test]
fn temporal_function_rebinding_uses_db_before_and_exact_retry_is_stable() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("program_retry");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store
        .create_database(&database_id, schema_without_predicates())
        .unwrap();
    let positive_hash = store.deploy_program_blob(&positive()).unwrap();
    let v1 = store.deploy_program_blob(&set_balance()).unwrap();

    let v2_program = Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(7)),
            Instruction::EmitAdd(BALANCE),
            Instruction::Return,
        ],
    };
    let v2 = store.deploy_program_blob(&v2_program).unwrap();
    drop(store);

    let set_ident = Keyword::new("account", "set-balance");
    let mut bindings = function_binding(
        "positive-function",
        Keyword::new("atomic.predicates", "positive"),
        positive_hash,
    );
    bindings.extend(function_binding("set-function", set_ident.clone(), v1));
    bindings.push(TxOp::Add {
        entity: EntityRef::Id(u64::from(BALANCE)),
        attribute: DB_ATTR_PREDS as u32,
        value: Value::Symbol(Symbol::new("atomic.predicates", "positive")).into(),
    });

    let service = common::start_service(&connection, &database_id);
    let installed = common::transact(
        &service,
        "install-functions",
        created.basis_t(),
        &bindings,
        1_000,
    );
    let function_eid = installed.tempids["set-function"];

    // Durable identity covers the caller's logical ident-based form, not the
    // hash selected later from the locked db-before. A replay must therefore
    // return this exact decision even after the ident is rebound.
    let retry_request = || {
        TransactionRequest::new("retry-key", vec![])
            .calling(database_call(
                set_ident.clone(),
                vec![Value::Ref(user(42)), Value::Long(5)],
            ))
            .comparing_basis(installed.basis_t)
            .with_tx_instant(2_000)
    };
    let first = service
        .client()
        .transact(retry_request(), Duration::from_secs(5))
        .unwrap();
    assert_eq!(
        first.db_after.values(user(42), BALANCE),
        vec![&Value::Long(5)]
    );

    // Datomic's Db.getFn boundary reads the immutable db-before. Therefore a
    // transaction that replaces :db/fn still invokes v1; only the next
    // transaction sees v2. v1 writes its argument, while v2 always writes 7,
    // making the observed version unambiguous.
    let rebound = transact_calls(
        &service,
        "rebind-and-call",
        first.basis_t,
        vec![TxOp::Add {
            entity: EntityRef::Id(function_eid),
            attribute: DB_FN as u32,
            value: Value::Function(v2).into(),
        }],
        vec![database_call(
            set_ident.clone(),
            vec![Value::Ref(user(42)), Value::Long(6)],
        )],
        3_000,
    )
    .unwrap();
    assert_eq!(
        rebound.db_before.values(function_eid, DB_FN as u32),
        vec![&Value::Function(v1)]
    );
    assert_eq!(
        rebound.db_after.values(function_eid, DB_FN as u32),
        vec![&Value::Function(v2)]
    );
    assert_eq!(
        rebound.db_after.values(user(42), BALANCE),
        vec![&Value::Long(6)]
    );

    let after_rebind = transact_calls(
        &service,
        "call-after-rebind",
        rebound.basis_t,
        vec![],
        vec![database_call(
            set_ident.clone(),
            vec![Value::Ref(user(42)), Value::Long(99)],
        )],
        4_000,
    )
    .unwrap();
    assert_eq!(
        after_rebind.db_after.values(user(42), BALANCE),
        vec![&Value::Long(7)]
    );

    let v1_as_of_install = after_rebind
        .db_after
        .datoms(View::AsOf(installed.basis_t), IndexOrder::Eavt);
    assert!(v1_as_of_install.iter().any(|datom| {
        datom.entity == function_eid
            && datom.attribute == DB_FN as u32
            && datom.value == Value::Function(v1)
    }));
    assert!(!v1_as_of_install.iter().any(|datom| {
        datom.entity == function_eid
            && datom.attribute == DB_FN as u32
            && datom.value == Value::Function(v2)
    }));

    let replay = service
        .client()
        .transact(retry_request(), Duration::from_secs(5))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, first.basis_t);
    assert_eq!(
        replay.db_after.values(user(42), BALANCE),
        vec![&Value::Long(5)]
    );
    let error = transact_calls(
        &service,
        "retry-key",
        installed.basis_t,
        vec![],
        vec![database_call(
            Keyword::new("account", "set-balance"),
            vec![Value::Ref(user(42)), Value::Long(8)],
        )],
        2_000,
    )
    .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Conflict, "postgres/idempotency-key-reused")
    );

    let original = atomic_core::encode_program(&v2_program).unwrap();
    service.shutdown();
    let mut historical_store = PostgresStore::connect(&connection).unwrap();
    let as_of_install = historical_store
        .recover_basis(&database_id, installed.basis_t)
        .unwrap();
    let selected = match as_of_install.values(function_eid, DB_FN as u32).as_slice() {
        [Value::Function(hash)] => *hash,
        value => panic!("unexpected historical function binding: {value:?}"),
    };
    assert_eq!(selected, v1);
    let historical_program = historical_store.resolve_program(selected).unwrap();
    let atomic_core::ProgramOutput::Transaction(forms) = atomic_core::ProgramRuntime
        .execute(
            &historical_program,
            &as_of_install,
            &[Value::Ref(user(42)), Value::Long(33)],
            atomic_core::ProgramControl::default(),
        )
        .unwrap()
    else {
        panic!("historical :db/fn did not resolve to a transaction function");
    };
    assert!(matches!(
        forms.as_slice(),
        [TxForm::Op(TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: BALANCE,
            value: TxValue::Scalar(Value::Long(33)),
        })] if *entity == user(42)
    ));
    let mut client = Client::connect(&connection, NoTls).unwrap();
    common::with_replica_triggers_disabled(&mut client, |client| {
        client.execute(
            "UPDATE atomic_programs SET payload = decode('00', 'hex') WHERE program_hash = $1",
            &[&&v2[..]],
        )
    })
    .unwrap();
    let mut restarted = PostgresStore::connect(&connection).unwrap();
    let recovered = restarted.recover(&database_id).unwrap();
    assert_eq!(recovered.basis_t(), after_rebind.basis_t);
    assert_eq!(recovered.values(user(42), BALANCE), vec![&Value::Long(7)]);
    assert_eq!(
        recovered.values(function_eid, DB_FN as u32),
        vec![&Value::Function(v2)]
    );
    assert!(!recovered.datoms(View::History, IndexOrder::Eavt).is_empty());
    common::with_replica_triggers_disabled(&mut client, |client| {
        client.execute(
            "UPDATE atomic_programs SET payload = $2 WHERE program_hash = $1",
            &[&&v2[..], &&original[..]],
        )
    })
    .unwrap();
}

#[test]
fn database_function_without_an_ident_is_callable_by_eid() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("eid_function");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store
        .create_database(&database_id, schema_without_predicates())
        .unwrap();
    let setter_hash = store.deploy_program_blob(&set_balance()).unwrap();
    drop(store);

    let service = common::start_service(&connection, &database_id);
    let installed = common::transact(
        &service,
        "install-eid-function",
        created.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Temp("anonymous-function".into()),
            attribute: DB_FN as u32,
            value: Value::Function(setter_hash).into(),
        }],
        1_000,
    );
    let function_eid = installed.tempids["anonymous-function"];
    let called = transact_calls(
        &service,
        "call-eid-function",
        installed.basis_t,
        vec![],
        vec![ProgramCall {
            function: CallableRef::Database(EntityRef::Id(function_eid)),
            arguments: vec![Value::Ref(user(42)).into(), Value::Long(17).into()],
        }],
        2_000,
    )
    .unwrap();
    assert_eq!(
        called.db_after.values(user(42), BALANCE),
        vec![&Value::Long(17)]
    );
    service.shutdown();
}

#[test]
fn persisted_predicates_are_resolved_only_for_assessed_assertions() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("lazy_predicate");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store
        .create_database(&database_id, schema_without_predicates())
        .unwrap();
    // Give the fault-injected blob unique content: the program catalog is
    // global, so deleting the shared `positive()` hash would perturb parallel
    // database tests that legitimately reuse identical content.
    let marker = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos() as i64;
    let lazy_positive = Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushConstant(Value::Long(marker)),
            Instruction::Pop,
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(0)),
            Instruction::GreaterThan,
            Instruction::Return,
        ],
    };
    let positive_hash = store.deploy_program_blob(&lazy_positive).unwrap();
    let lazy_entity = Program {
        kind: ProgramKind::EntityPredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushConstant(Value::Long(marker.wrapping_add(1))),
            Instruction::Pop,
            Instruction::PushConstant(Value::Bool(true)),
            Instruction::Return,
        ],
    };
    let entity_hash = store.deploy_program_blob(&lazy_entity).unwrap();
    drop(store);

    let guard_ident = Keyword::new("account", "lazy-guard");
    let service = common::start_service(&connection, &database_id);
    let installed = common::transact(
        &service,
        "install-lazy-predicate",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("positive".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("atomic.predicates", "positive")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("positive".into()),
                attribute: DB_FN as u32,
                value: Value::Function(positive_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(u64::from(BALANCE)),
                attribute: DB_ATTR_PREDS as u32,
                value: Value::Symbol(Symbol::new("atomic.predicates", "positive")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("entity-predicate".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("atomic.predicates", "lazy-entity")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("entity-predicate".into()),
                attribute: DB_FN as u32,
                value: Value::Function(entity_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(guard_ident.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_ENTITY_PREDS as u32,
                value: Value::Symbol(Symbol::new("atomic.predicates", "lazy-entity")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("guard".into()),
                attribute: DB_ENTITY_ATTRS as u32,
                value: Value::Keyword(Keyword::new("account", "snapshot")).into(),
            },
        ],
        1_000,
    );
    let seeded = common::transact(
        &service,
        "seed-guarded-value",
        installed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: BALANCE,
            value: Value::Long(10).into(),
        }],
        2_000,
    );
    service.shutdown();

    // Model an unavailable/corrupt deployment artifact. The append-only
    // catalog trigger is bypassed only by this fault-injection session.
    let mut client = Client::connect(&connection, NoTls).unwrap();
    common::with_replica_triggers_disabled(&mut client, |client| {
        client.execute(
            "DELETE FROM atomic_programs WHERE program_hash = $1",
            &[&&positive_hash[..]],
        )?;
        client.execute(
            "DELETE FROM atomic_programs WHERE program_hash = $1",
            &[&&entity_hash[..]],
        )
    })
    .unwrap();
    drop(client);

    let restarted = common::start_service(&connection, &database_id);
    let unrelated = common::transact(
        &restarted,
        "unrelated-with-missing-predicate",
        seeded.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: SNAPSHOT,
            value: Value::Long(1).into(),
        }],
        3_000,
    );
    assert_eq!(restarted.program_cache_stats().misses, 0);

    // A raw reassertion is removed by assessment and likewise performs no
    // lookup. A material assertion is the first operation that needs code.
    let redundant = common::transact(
        &restarted,
        "redundant-with-missing-predicate",
        unrelated.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: BALANCE,
            value: Value::Long(10).into(),
        }],
        4_000,
    );
    assert_eq!(restarted.program_cache_stats().misses, 0);
    let missing_attribute = common::try_transact(
        &restarted,
        "ensure-missing-required-attribute",
        redundant.basis_t,
        &[TxOp::Ensure {
            entity: EntityRef::Id(user(43)),
            spec: EntityRef::Ident(guard_ident.clone()),
        }],
        5_000,
    )
    .unwrap_err();
    assert_eq!(missing_attribute.code, "transaction/entity-spec");
    assert_eq!(restarted.program_cache_stats().misses, 0);
    let entity_error = common::try_transact(
        &restarted,
        "ensure-with-missing-predicate",
        redundant.basis_t,
        &[TxOp::Ensure {
            entity: EntityRef::Id(user(42)),
            spec: EntityRef::Ident(guard_ident),
        }],
        5_000,
    )
    .unwrap_err();
    assert_eq!(
        (entity_error.category, entity_error.code),
        (ErrorCategory::NotFound, "postgres/program-not-found")
    );
    let error = common::try_transact(
        &restarted,
        "material-with-missing-predicate",
        redundant.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: BALANCE,
            value: Value::Long(11).into(),
        }],
        5_000,
    )
    .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::NotFound, "postgres/program-not-found")
    );
    restarted.shutdown();
}

#[test]
fn broken_or_wrong_role_predicate_bindings_fail_their_source_transaction() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("predicate_binding_validation");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store
        .create_database(&database_id, schema_without_predicates())
        .unwrap();
    let transaction_hash = store.deploy_program_blob(&set_balance()).unwrap();
    let positive_hash = store.deploy_program_blob(&positive()).unwrap();
    drop(store);

    let service = common::start_service(&connection, &database_id);
    let missing = common::try_transact(
        &service,
        "missing-predicate-binding",
        created.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Id(u64::from(BALANCE)),
            attribute: DB_ATTR_PREDS as u32,
            value: Value::Symbol(Symbol::new("atomic.predicates", "missing")).into(),
        }],
        1_000,
    )
    .unwrap_err();
    assert_eq!(
        (missing.category, missing.code),
        (ErrorCategory::NotFound, "program/function-not-found")
    );

    let wrong_role = common::try_transact(
        &service,
        "wrong-role-predicate-binding",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("wrong-role".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("atomic.predicates", "wrong-role")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("wrong-role".into()),
                attribute: DB_FN as u32,
                value: Value::Function(transaction_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(u64::from(BALANCE)),
                attribute: DB_ATTR_PREDS as u32,
                value: Value::Symbol(Symbol::new("atomic.predicates", "wrong-role")).into(),
            },
        ],
        1_000,
    )
    .unwrap_err();
    assert_eq!(wrong_role.code, "program/not-attribute-predicate");

    let wrong_entity_role = common::try_transact(
        &service,
        "wrong-role-entity-predicate-binding",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("wrong-entity-role".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("atomic.predicates", "wrong-entity-role"))
                    .into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("wrong-entity-role".into()),
                attribute: DB_FN as u32,
                value: Value::Function(transaction_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("wrong-entity-spec".into()),
                attribute: DB_ENTITY_PREDS as u32,
                value: Value::Symbol(Symbol::new("atomic.predicates", "wrong-entity-role")).into(),
            },
        ],
        1_000,
    )
    .unwrap_err();
    assert_eq!(wrong_entity_role.code, "program/not-entity-predicate");

    let installed = common::transact(
        &service,
        "valid-predicate-binding",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("valid".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("atomic.predicates", "valid")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("valid".into()),
                attribute: DB_FN as u32,
                value: Value::Function(positive_hash).into(),
            },
            TxOp::Add {
                entity: EntityRef::Id(u64::from(BALANCE)),
                attribute: DB_ATTR_PREDS as u32,
                value: Value::Symbol(Symbol::new("atomic.predicates", "valid")).into(),
            },
        ],
        1_000,
    );
    let function_eid = installed.tempids["valid"];
    let wrong_rebind = common::try_transact(
        &service,
        "wrong-role-predicate-rebind",
        installed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(function_eid),
            attribute: DB_FN as u32,
            value: Value::Function(transaction_hash).into(),
        }],
        2_000,
    )
    .unwrap_err();
    assert_eq!(wrong_rebind.code, "program/not-attribute-predicate");

    // Every rejected candidate leaves its expected basis available.
    let accepted = common::transact(
        &service,
        "after-rejected-bindings",
        installed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(user(42)),
            attribute: SNAPSHOT,
            value: Value::Long(1).into(),
        }],
        2_000,
    );
    assert_eq!(accepted.basis_t, installed.basis_t + 1);
    service.shutdown();
}
