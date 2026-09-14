mod common;
use atomic_core::{
    Attribute, CallableRef, CapacityLimits, Cardinality, DB_ATTR_PREDS, DB_FN, DB_IDENT, EntityRef,
    ErrorCategory, IndexPrefix, Instruction, Keyword, Program, ProgramCall, ProgramKind,
    ProgramLimits, QueryPattern, QueryTemplate, QueryTerm, RuntimeValue, Schema, Symbol,
    TransactionRequest, TransactionService, TransactionServiceConfig, TransactionStandby, TxOp,
    Value, ValueType,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const BALANCE: u32 = 1_000;
const OBSERVED_BALANCE: u32 = 1_001;
const BATCH_VALUE: u32 = 1_002;
const QUERY_MATCH: u32 = 1_003;

fn connection() -> Option<common::PostgresFixture> {
    std::env::var("ATOMIC_POSTGRES_URL")
        .ok()
        .map(|url| common::PostgresFixture::new(&url, "current_semantics"))
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

fn config(connection: &str, database_id: &str, holder: &str) -> TransactionServiceConfig {
    config_with_limits(connection, database_id, holder, CapacityLimits::default())
}

fn config_with_limits(
    connection: &str,
    database_id: &str,
    holder: &str,
    capacity_limits: CapacityLimits,
) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: atomic_core::PostgresConnectionConfig::plaintext(connection),
        database_id: database_id.into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 4,
        capacity_limits,
    }
}

fn empty_transaction() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![Instruction::Return],
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

fn schema() -> Schema {
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
}

fn stage_four_witness_schema() -> Schema {
    let mut schema = schema();
    schema
        .install(Attribute::new(
            OBSERVED_BALANCE,
            Keyword::new("account", "observed-balance"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            BATCH_VALUE,
            Keyword::new("account", "batch-value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            QUERY_MATCH,
            Keyword::new("account", "query-match"),
            ValueType::Boolean,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn observe_db_before() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::Duplicate,
            Instruction::LoadOne(BALANCE),
            Instruction::EmitAdd(OBSERVED_BALANCE),
            Instruction::Return,
        ],
    }
}

fn replace_then_observe() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushArgument(1),
            Instruction::EmitAdd(BALANCE),
            Instruction::PushArgument(0),
            Instruction::EmitCall {
                function: CallableRef::Database(EntityRef::Ident(Keyword::new(
                    "stage4",
                    "observe-db-before",
                ))),
                argument_count: 1,
            },
            Instruction::Return,
        ],
    }
}

fn branch_collect_and_query() -> Program {
    let same_balance = QueryTemplate::new(
        vec![1],
        vec![
            QueryPattern::new(QueryTerm::Input(2), BALANCE, QueryTerm::Variable(0)),
            QueryPattern::new(QueryTerm::Variable(1), BALANCE, QueryTerm::Variable(0)),
        ],
    )
    .unwrap();
    Program {
        kind: ProgramKind::Transaction,
        arity: 3,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Keyword(Keyword::new("request", "apply"))),
            Instruction::ContainsKey,
            Instruction::If {
                then_branch: vec![
                    Instruction::PushArgument(1),
                    Instruction::ForEach {
                        body: vec![Instruction::Unpack(2), Instruction::EmitAdd(BATCH_VALUE)],
                    },
                    Instruction::Query(same_balance),
                    Instruction::ForEach {
                        body: vec![
                            Instruction::Unpack(1),
                            Instruction::PushConstant(Value::Bool(true)),
                            Instruction::EmitAdd(QUERY_MATCH),
                        ],
                    },
                ],
                else_branch: vec![
                    Instruction::PushConstant(Value::Bool(false)),
                    Instruction::PushArgument(0),
                    Instruction::RequireAnomaly,
                ],
            },
            Instruction::Return,
        ],
    }
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

fn emit_two_maps() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::EmitEntityMap,
            Instruction::PushArgument(1),
            Instruction::EmitEntityMap,
            Instruction::Return,
        ],
    }
}

fn balance_map(value: i64) -> RuntimeValue {
    RuntimeValue::map(vec![(
        Value::Keyword(Keyword::new("account", "balance")),
        RuntimeValue::Scalar(Value::Long(value)),
    )])
    .unwrap()
}

fn call(request_key: &str, left: i64, right: i64) -> TransactionRequest {
    TransactionRequest::from_forms(request_key, Vec::new()).calling(ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(Keyword::new("account", "emit-two"))),
        arguments: vec![balance_map(left), balance_map(right)],
    })
}

fn install_functions(
    service: &TransactionService,
    positive_hash: [u8; 32],
    emitter_hash: [u8; 32],
) -> atomic_core::ServiceTransactionReport {
    service
        .client()
        .transact(
            TransactionRequest::new(
                "install-functions",
                vec![
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
                        entity: EntityRef::Temp("emitter".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("account", "emit-two")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("emitter".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(emitter_hash).into(),
                    },
                ],
            ),
            Duration::from_secs(3),
        )
        .unwrap()
}

#[test]
fn functions_maps_and_predicates_survive_base_recovery_and_standby_takeover() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("program_base_failover");
    common::install(&connection).unwrap();
    let mut store = common::TestStore::connect(&connection).unwrap();
    store.create_database(&database_id, schema()).unwrap();
    let positive_hash = store.deploy_program_blob(&positive()).unwrap();
    let emitter_hash = store.deploy_program_blob(&emit_two_maps()).unwrap();
    drop(store);

    let first = TransactionService::start(config(&connection, &database_id, "first")).unwrap();
    let installed = install_functions(&first, positive_hash, emitter_hash);
    let emitted = first
        .client()
        .transact(call("first-maps", 11, 11), Duration::from_secs(3))
        .unwrap();
    assert_eq!(emitted.basis_t, installed.basis_t + 1);
    let anonymous: Vec<_> = emitted
        .tempids
        .iter()
        .filter(|(name, _)| name.starts_with("__map/"))
        .map(|(_, entity)| *entity)
        .collect();
    assert_eq!(anonymous.len(), 2, "both identical map forms must survive");
    assert_ne!(anonymous[0], anonymous[1]);
    assert!(anonymous.iter().all(|entity| {
        emitted.db_after.values(*entity, BALANCE).unwrap() == vec![Value::Long(11)]
    }));
    first.shutdown();

    let base = common::consolidate(&connection, &database_id).unwrap();
    assert_eq!(base.basis_t, emitted.basis_t);

    let warm = TransactionService::start(config(&connection, &database_id, "warm")).unwrap();
    assert_eq!(warm.recovery_stats().base_t, base.basis_t);
    assert_eq!(warm.recovery_stats().tail_transactions, 0);
    let warm_report = warm
        .client()
        .transact(call("after-base", 20, 21), Duration::from_secs(3))
        .unwrap();
    assert_eq!(warm_report.basis_t, base.basis_t + 1);
    warm.shutdown();

    // Model a writer that died without releasing its still-live lease. The
    // standby must wait for expiry, recover base+tail, and use the same
    // temporal functions and predicate boundary before publishing.
    let block_config = atomic_core::PostgresConnectionConfig::plaintext(&connection);
    let database =
        atomic_core::storage::BlockDatabase::resolve(&block_config, &database_id).unwrap();
    let abandoned = atomic_core::BlockTransactor::claim(
        &block_config,
        database,
        atomic_core::BlockWriterOptions {
            lease_duration: Duration::from_millis(150),
            ..Default::default()
        },
    )
    .unwrap();
    // Dropping this direct transactor without release leaves its real opaque
    // lease to expire, as after a process failure; no SQL lease forgery.
    drop(abandoned);
    let standby = TransactionStandby::start(
        config(&connection, &database_id, "standby"),
        Duration::from_millis(20),
    )
    .unwrap();
    let active = standby.await_active(Duration::from_secs(3)).unwrap();
    assert_eq!(active.recovery_stats().base_t, base.basis_t);
    assert_eq!(active.recovery_stats().tail_transactions, 1);

    let before_rejection = active.recovery_stats().target_t;
    let error = active
        .client()
        .transact(
            call("negative-after-failover", -1, -2),
            Duration::from_secs(3),
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Incorrect, "transaction/attribute-predicate")
    );
    let committed = active
        .client()
        .transact(
            call("positive-after-failover", 30, 31),
            Duration::from_secs(3),
        )
        .unwrap();
    assert_eq!(committed.basis_t, before_rejection + 1);
    assert_eq!(
        committed
            .db_after
            .datoms_with_prefix(&IndexPrefix::Aevt {
                attribute: BALANCE,
                entity: None,
                value: None,
            })
            .unwrap()
            .len(),
        6
    );
    active.shutdown();
}

#[test]
fn persisted_stage_four_control_flow_is_atomic_and_db_before_consistent() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("program_stage_four_witness");
    common::install(&connection).unwrap();
    let mut store = common::TestStore::connect(&connection).unwrap();
    store
        .create_database(&database_id, stage_four_witness_schema())
        .unwrap();
    let observer_hash = store.deploy_program_blob(&observe_db_before()).unwrap();
    let replacer_hash = store.deploy_program_blob(&replace_then_observe()).unwrap();
    let workflow_hash = store
        .deploy_program_blob(&branch_collect_and_query())
        .unwrap();
    drop(store);

    let service =
        TransactionService::start(config(&connection, &database_id, "stage-four")).unwrap();
    let installed = service
        .client()
        .transact(
            TransactionRequest::new(
                "install-stage-four-programs",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("observer".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("stage4", "observe-db-before")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("observer".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(observer_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("replacer".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("stage4", "replace-then-observe"))
                            .into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("replacer".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(replacer_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("workflow".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("stage4", "workflow")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("workflow".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(workflow_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("source".into()),
                        attribute: BALANCE,
                        value: Value::Long(5).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("same-balance".into()),
                        attribute: BALANCE,
                        value: Value::Long(5).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("other-balance".into()),
                        attribute: BALANCE,
                        value: Value::Long(6).into(),
                    },
                ],
            ),
            Duration::from_secs(3),
        )
        .unwrap();
    let source = installed.tempids["source"];
    let same_balance = installed.tempids["same-balance"];
    let other_balance = installed.tempids["other-balance"];

    let apply = RuntimeValue::map(vec![(
        Value::Keyword(Keyword::new("request", "apply")),
        RuntimeValue::Scalar(Value::Bool(true)),
    )])
    .unwrap();
    let finite_batch = RuntimeValue::Vector(vec![
        RuntimeValue::Vector(vec![
            RuntimeValue::Entity(EntityRef::Id(source)),
            RuntimeValue::Scalar(Value::Long(101)),
        ]),
        RuntimeValue::Vector(vec![
            RuntimeValue::Entity(EntityRef::Id(other_balance)),
            RuntimeValue::Scalar(Value::Long(202)),
        ]),
    ]);
    let workflow_call = |options, batch| ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(Keyword::new("stage4", "workflow"))),
        arguments: vec![options, batch, RuntimeValue::Entity(EntityRef::Id(source))],
    };
    let replace_call = |value| ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(Keyword::new(
            "stage4",
            "replace-then-observe",
        ))),
        arguments: vec![
            RuntimeValue::Entity(EntityRef::Id(source)),
            RuntimeValue::Scalar(Value::Long(value)),
        ],
    };

    let applied = service
        .client()
        .transact(
            TransactionRequest::from_forms("stage-four-success", Vec::new())
                .calling_all([workflow_call(apply, finite_batch.clone()), replace_call(9)])
                .comparing_basis(installed.basis_t),
            Duration::from_secs(3),
        )
        .unwrap();

    // The outer call changes balance to 9, but both its nested observer and
    // the sibling two-pattern query saw the exact immutable db-before where
    // source and same-balance both had value 5.
    assert_eq!(
        applied.db_after.values(source, BALANCE).unwrap(),
        vec![Value::Long(9)]
    );
    assert_eq!(
        applied.db_after.values(source, OBSERVED_BALANCE).unwrap(),
        vec![Value::Long(5)]
    );
    assert_eq!(
        applied.db_after.values(source, BATCH_VALUE).unwrap(),
        vec![Value::Long(101)]
    );
    assert_eq!(
        applied.db_after.values(other_balance, BATCH_VALUE).unwrap(),
        vec![Value::Long(202)]
    );
    assert_eq!(
        applied.db_after.values(source, QUERY_MATCH).unwrap(),
        vec![Value::Bool(true)]
    );
    assert_eq!(
        applied.db_after.values(same_balance, QUERY_MATCH).unwrap(),
        vec![Value::Bool(true)]
    );
    assert!(
        applied
            .db_after
            .values(other_balance, QUERY_MATCH)
            .unwrap()
            .is_empty()
    );

    let cancel = RuntimeValue::map(vec![
        (
            Value::Keyword(Keyword::new("cognitect.anomalies", "category")),
            RuntimeValue::Scalar(Value::Keyword(Keyword::new(
                "cognitect.anomalies",
                "conflict",
            ))),
        ),
        (
            Value::Keyword(Keyword::new("cognitect.anomalies", "message")),
            RuntimeValue::Scalar(Value::String("stage four cancellation".into())),
        ),
        (
            Value::Keyword(Keyword::new("stage4", "reason")),
            RuntimeValue::Scalar(Value::String("map omitted :request/apply".into())),
        ),
    ])
    .unwrap();
    let mut expected_cancel_entries = match cancel.clone() {
        RuntimeValue::Map(entries) => entries,
        _ => unreachable!("constructed a runtime map"),
    };
    expected_cancel_entries.push((
        Value::Keyword(Keyword::new("datomic", "cancelled")),
        RuntimeValue::Scalar(Value::Bool(true)),
    ));
    let expected_cancel = RuntimeValue::map(expected_cancel_entries).unwrap();
    let error = service
        .client()
        .transact(
            TransactionRequest::from_forms("stage-four-cancel", Vec::new())
                .calling_all([replace_call(99), workflow_call(cancel, finite_batch)])
                .comparing_basis(applied.basis_t),
            Duration::from_secs(3),
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code, error.message.as_str()),
        (
            ErrorCategory::Conflict,
            "program/rejected",
            "stage four cancellation"
        )
    );
    assert_eq!(error.details["datomic/cancelled"], "true");
    assert_eq!(error.details["stage4/reason"], "map omitted :request/apply");
    assert_eq!(error.anomaly, Some(Box::new(expected_cancel)));

    let recovered = common::TestStore::connect(&connection)
        .unwrap()
        .recover(&database_id)
        .unwrap();
    assert_eq!(recovered.basis_t(), applied.basis_t);
    assert_eq!(
        recovered.values(source, BALANCE).unwrap(),
        vec![Value::Long(9)]
    );
    assert_eq!(
        recovered.values(source, OBSERVED_BALANCE).unwrap(),
        vec![Value::Long(5)]
    );
    service.shutdown();
}

#[test]
fn configured_budget_is_shared_by_siblings_nested_calls_predicates_and_queries() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("program_service_limits");
    common::install(&connection).unwrap();
    let mut store = common::TestStore::connect(&connection).unwrap();
    store.create_database(&database_id, schema()).unwrap();
    let empty_hash = store.deploy_program_blob(&empty_transaction()).unwrap();
    let setter_hash = store.deploy_program_blob(&set_balance()).unwrap();
    let positive_hash = store.deploy_program_blob(&positive()).unwrap();
    let nested_hash = store
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![
                Instruction::EmitCall {
                    function: CallableRef::ExactHash(empty_hash),
                    argument_count: 0,
                },
                Instruction::Return,
            ],
        })
        .unwrap();
    let scan_hash = store
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![
                Instruction::Query(
                    QueryTemplate::new(
                        vec![0],
                        vec![QueryPattern::new(
                            QueryTerm::Variable(0),
                            BALANCE,
                            QueryTerm::Variable(1),
                        )],
                    )
                    .unwrap(),
                ),
                Instruction::Pop,
                Instruction::Return,
            ],
        })
        .unwrap();
    drop(store);

    let setup = TransactionService::start(config(&connection, &database_id, "setup")).unwrap();
    let installed = setup
        .client()
        .transact(
            TransactionRequest::new(
                "install-limit-programs-and-data",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("empty".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("limits", "empty")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("empty".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(empty_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("setter".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("limits", "set")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("setter".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(setter_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("positive".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("limits", "positive")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("positive".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(positive_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("nested".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("limits", "nested")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("nested".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(nested_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("scan".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("limits", "scan")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("scan".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(scan_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("one".into()),
                        attribute: BALANCE,
                        value: Value::Long(1).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("two".into()),
                        attribute: BALANCE,
                        value: Value::Long(2).into(),
                    },
                ],
            ),
            Duration::from_secs(3),
        )
        .unwrap();
    setup.shutdown();

    let limited = TransactionService::start(config_with_limits(
        &connection,
        &database_id,
        "limited",
        CapacityLimits {
            program: ProgramLimits {
                max_calls: 1,
                max_collection_items: 1,
                ..ProgramLimits::default()
            },
            ..CapacityLimits::default()
        },
    ))
    .unwrap();
    let predicate_installed = limited
        .client()
        .transact(
            TransactionRequest::new(
                "install-limited-predicate",
                vec![TxOp::Add {
                    entity: EntityRef::Id(u64::from(BALANCE)),
                    attribute: DB_ATTR_PREDS as u32,
                    value: Value::Symbol(Symbol::new("limits", "positive")).into(),
                }],
            ),
            Duration::from_secs(3),
        )
        .unwrap();
    let basis = predicate_installed.basis_t;
    let db_call = |name: &str, arguments: Vec<RuntimeValue>| ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(Keyword::new("limits", name))),
        arguments,
    };

    let siblings = TransactionRequest::from_forms("limited-siblings", Vec::new())
        .calling_all([db_call("empty", Vec::new()), db_call("empty", Vec::new())]);
    assert_eq!(
        limited
            .client()
            .transact(siblings, Duration::from_secs(3))
            .unwrap_err()
            .code,
        "program/call-limit"
    );
    assert_eq!(
        limited
            .client()
            .transact(
                TransactionRequest::from_forms("limited-nested", Vec::new())
                    .calling(db_call("nested", Vec::new())),
                Duration::from_secs(3),
            )
            .unwrap_err()
            .code,
        "program/call-limit"
    );
    assert_eq!(
        limited
            .client()
            .transact(
                TransactionRequest::from_forms("limited-predicate", Vec::new()).calling(db_call(
                    "set",
                    vec![
                        RuntimeValue::Entity(EntityRef::Id(installed.tempids["one"])),
                        RuntimeValue::Scalar(Value::Long(3)),
                    ],
                )),
                Duration::from_secs(3),
            )
            .unwrap_err()
            .code,
        "program/call-limit"
    );
    assert_eq!(
        limited
            .client()
            .transact(
                TransactionRequest::from_forms("limited-query", Vec::new())
                    .calling(db_call("scan", Vec::new())),
                Duration::from_secs(3),
            )
            .unwrap_err()
            .code,
        "query/intermediate-limit"
    );

    assert_eq!(
        common::TestStore::connect(&connection)
            .unwrap()
            .recover(&database_id)
            .unwrap()
            .basis_t(),
        basis,
        "every resource-limited attempt must remain invisible"
    );
    let after = limited
        .client()
        .transact(
            TransactionRequest::from_forms("after-limits", Vec::new()),
            Duration::from_secs(3),
        )
        .unwrap();
    assert_eq!(after.basis_t, basis + 1);
    limited.shutdown();
}
