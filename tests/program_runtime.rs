use atomic_core::{
    Attribute, AttributeRef, CallableRef, Cardinality, Database, EntityRef, ErrorCategory,
    Instruction, Keyword, MapValue, Program, ProgramBudget, ProgramControl, ProgramKind,
    ProgramOutput, ProgramRuntime, QueryPattern, QueryTemplate, QueryTerm, RuntimeValue, TxForm,
    TxFunctions, TxOp, TxValue, USER_PARTITION, Value, ValueType, decode_program, encode_program,
    encode_program_output, is_exact_true, make_eid, program_hash, require_exact_true,
};
use std::sync::atomic::AtomicBool;

const BALANCE: u32 = 1_000;
const MEMBERS: u32 = 1_001;
const NAME: u32 = 1_002;
const LABELS: u32 = 1_003;

fn database() -> Database {
    let mut schema = atomic_core::Schema::new();
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
            LABELS,
            Keyword::new("team", "labels"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            MEMBERS,
            Keyword::new("team", "members"),
            ValueType::Ref,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            NAME,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    Database::new(schema)
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(user(42)),
                    attribute: BALANCE,
                    value: TxValue::Scalar(Value::Long(40)),
                },
                TxOp::Add {
                    entity: EntityRef::Id(user(90)),
                    attribute: MEMBERS,
                    value: TxValue::Entity(EntityRef::Id(user(42))),
                },
                TxOp::Add {
                    entity: EntityRef::Id(user(90)),
                    attribute: MEMBERS,
                    value: TxValue::Entity(EntityRef::Id(user(43))),
                },
                TxOp::Add {
                    entity: EntityRef::Id(user(42)),
                    attribute: NAME,
                    value: TxValue::Scalar(Value::String("Ada".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Id(user(43)),
                    attribute: NAME,
                    value: TxValue::Scalar(Value::String("Grace".into())),
                },
            ],
            1_000,
        )
        .unwrap()
        .db_after
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

fn increment() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::Duplicate,
            Instruction::LoadOne(BALANCE),
            Instruction::PushConstant(Value::Long(2)),
            Instruction::Add,
            Instruction::EmitAdd(BALANCE),
            Instruction::Return,
        ],
    }
}

#[test]
fn program_encoding_is_canonical_hashed_and_checked() {
    let program = increment();
    let bytes = encode_program(&program).unwrap();
    assert_eq!(
        &bytes[16..18],
        &atomic_core::PROGRAM_ABI_VERSION.to_be_bytes()
    );
    assert_eq!(decode_program(&bytes).unwrap(), program);
    assert_eq!(program_hash(&program).unwrap(), atomic_core::sha256(&bytes));
    assert_eq!(
        hex(&program_hash(&program).unwrap()),
        "2051644d6ff2455a4811006acee063e99b20bb9279b63e43feb6eac6604f00f2"
    );

    let mut corrupt = bytes;
    corrupt[20] ^= 1;
    let error = decode_program(&corrupt).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Fault);
    assert_eq!(error.code, "encoding/checksum-mismatch");

    let mut unsupported = encode_program(&increment()).unwrap();
    unsupported[16..18].copy_from_slice(&99u16.to_be_bytes());
    let checksum_at = unsupported.len() - 32;
    let checksum = atomic_core::sha256(&unsupported[..checksum_at]);
    unsupported[checksum_at..].copy_from_slice(&checksum);
    let error = decode_program(&unsupported).unwrap_err();
    assert_eq!(error.code, "encoding/unsupported-program-abi");
}

#[test]
fn transaction_program_reads_one_db_before_and_emits_ordinary_ops() {
    let db = database();
    let output = ProgramRuntime
        .execute(
            &increment(),
            &db,
            &[Value::Ref(user(42))],
            ProgramControl::default(),
        )
        .unwrap();
    let ProgramOutput::Transaction(forms) = output else {
        panic!("wrong output kind");
    };
    let report = db.with_forms(&forms, &TxFunctions::new(), 2_000).unwrap();
    assert_eq!(
        report.db_after.values(user(42), BALANCE),
        vec![&Value::Long(42)]
    );
}

#[test]
fn predicate_and_query_results_are_typed_and_deterministic() {
    let predicate = Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(0)),
            Instruction::GreaterThan,
            Instruction::Return,
        ],
    };
    let query = Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(BALANCE),
            Instruction::PushConstant(Value::Long(2)),
            Instruction::Multiply,
            Instruction::Return,
        ],
    };
    let db = database();
    let runtime = ProgramRuntime;
    assert!(matches!(
        runtime
            .execute(
                &predicate,
                &db,
                &[Value::Long(1)],
                ProgramControl::default()
            )
            .unwrap(),
        ProgramOutput::AttributePredicate(RuntimeValue::Scalar(Value::Bool(true)))
    ));
    let first = runtime
        .execute(
            &query,
            &db,
            &[Value::Ref(user(42))],
            ProgramControl::default(),
        )
        .unwrap();
    let second = runtime
        .execute(
            &query,
            &db,
            &[Value::Ref(user(42))],
            ProgramControl::default(),
        )
        .unwrap();
    assert_eq!(format!("{first:?}"), format!("{second:?}"));
    assert!(matches!(first, ProgramOutput::Query(rows) if rows == vec![vec![Value::Long(80)]]));
}

#[test]
fn predicates_preserve_the_actual_non_true_value() {
    let db = database();
    let predicate = Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![Instruction::PushArgument(0), Instruction::Return],
    };
    let returned = Value::String("truthy-is-not-true".into());
    let output = ProgramRuntime
        .execute(
            &predicate,
            &db,
            std::slice::from_ref(&returned),
            ProgramControl::default(),
        )
        .unwrap();
    let ProgramOutput::AttributePredicate(actual) = output else {
        panic!("wrong output kind");
    };
    assert_eq!(actual, RuntimeValue::Scalar(returned));
    assert!(!is_exact_true(&actual));
    let error = require_exact_true(&actual, "example/nonzero").unwrap_err();
    assert_eq!(error.code, "program/predicate-failed");
    assert_eq!(error.details["predicate"], "example/nonzero");
    assert_eq!(
        error.details["result"],
        "Scalar(String(\"truthy-is-not-true\"))"
    );
    assert!(is_exact_true(&RuntimeValue::Scalar(Value::Bool(true))));
    assert!(require_exact_true(&RuntimeValue::Scalar(Value::Bool(true)), "example/true").is_ok());

    let null_predicate = Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![Instruction::PushNull, Instruction::Return],
    };
    let ProgramOutput::AttributePredicate(null) = ProgramRuntime
        .execute(
            &null_predicate,
            &db,
            &[Value::Long(1)],
            ProgramControl::default(),
        )
        .unwrap()
    else {
        panic!("wrong output kind");
    };
    assert_eq!(null, RuntimeValue::Null);
    assert_eq!(
        require_exact_true(&null, "example/null")
            .unwrap_err()
            .details["result"],
        "Null"
    );

    let collection_predicate = Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushConstant(Value::Long(1)),
            Instruction::MakeVector(1),
            Instruction::Return,
        ],
    };
    let ProgramOutput::AttributePredicate(collection) = ProgramRuntime
        .execute(
            &collection_predicate,
            &db,
            &[Value::Long(1)],
            ProgramControl::default(),
        )
        .unwrap()
    else {
        panic!("wrong output kind");
    };
    assert_eq!(
        collection,
        RuntimeValue::Vector(vec![RuntimeValue::Scalar(Value::Long(1))])
    );
    assert!(require_exact_true(&collection, "example/vector").is_err());
}

#[test]
fn string_length_matches_jvm_utf16_code_units() {
    let db = database();
    let predicate = Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::Length,
            Instruction::PushConstant(Value::Long(4)),
            Instruction::Equal,
            Instruction::Return,
        ],
    };
    let output = ProgramRuntime
        .execute(
            &predicate,
            &db,
            &[Value::String("a💡é".into())],
            ProgramControl::default(),
        )
        .unwrap();
    assert!(matches!(
        output,
        ProgramOutput::AttributePredicate(RuntimeValue::Scalar(Value::Bool(true)))
    ));
}

#[test]
fn transaction_program_can_return_a_typed_nested_call_form() {
    let db = database();
    let hash = [0x5a; 32];
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushArgument(1),
            Instruction::EmitCall {
                function: CallableRef::ExactHash(hash),
                argument_count: 2,
            },
            Instruction::Return,
        ],
    };
    assert_eq!(
        decode_program(&encode_program(&program).unwrap()).unwrap(),
        program
    );
    let output = ProgramRuntime
        .execute(
            &program,
            &db,
            &[Value::Ref(user(42)), Value::Long(7)],
            ProgramControl::default(),
        )
        .unwrap();
    let ProgramOutput::Transaction(forms) = output else {
        panic!("wrong output kind");
    };
    assert!(matches!(
        forms.as_slice(),
        [TxForm::ProgramCall(call)]
            if call.function == CallableRef::ExactHash(hash)
                && call.arguments == [
                    RuntimeValue::Scalar(Value::Ref(user(42))),
                    RuntimeValue::Scalar(Value::Long(7)),
                ]
    ));
}

#[test]
fn structured_branch_inspects_a_real_runtime_map() {
    let db = database();
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 3,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Keyword(Keyword::unqualified("name"))),
            Instruction::ContainsKey,
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Keyword(Keyword::unqualified("email"))),
            Instruction::ContainsKey,
            Instruction::And,
            Instruction::If {
                then_branch: vec![
                    Instruction::PushArgument(1),
                    Instruction::PushArgument(2),
                    Instruction::EmitAdd(NAME),
                ],
                else_branch: vec![
                    Instruction::PushConstant(Value::Bool(false)),
                    Instruction::Require {
                        category: ErrorCategory::Incorrect,
                        message: "user map requires name and email".into(),
                    },
                ],
            },
            Instruction::Return,
        ],
    };
    assert_eq!(
        decode_program(&encode_program(&program).unwrap()).unwrap(),
        program
    );

    let complete = RuntimeValue::map(vec![
        (
            Value::Keyword(Keyword::unqualified("name")),
            RuntimeValue::Scalar(Value::String("Ada".into())),
        ),
        (
            Value::Keyword(Keyword::unqualified("email")),
            RuntimeValue::Scalar(Value::String("ada@example.test".into())),
        ),
    ])
    .unwrap();
    let output = ProgramRuntime
        .execute_runtime(
            &program,
            &db,
            &[
                complete,
                RuntimeValue::Entity(EntityRef::Id(user(44))),
                RuntimeValue::Scalar(Value::String("Ada".into())),
            ],
            ProgramControl::default(),
        )
        .unwrap();
    assert!(matches!(
        output,
        ProgramOutput::Transaction(forms)
            if matches!(
                forms.as_slice(),
                [TxForm::Op(TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: NAME,
                    value: TxValue::Scalar(Value::String(name)),
                })] if *entity == user(44) && name == "Ada"
            )
    ));

    let incomplete = RuntimeValue::map(vec![(
        Value::Keyword(Keyword::unqualified("name")),
        RuntimeValue::Scalar(Value::String("Ada".into())),
    )])
    .unwrap();
    let error = ProgramRuntime
        .execute_runtime(
            &program,
            &db,
            &[
                incomplete,
                RuntimeValue::Entity(EntityRef::Id(user(44))),
                RuntimeValue::Scalar(Value::String("Ada".into())),
            ],
            ProgramControl::default(),
        )
        .unwrap_err();
    assert_eq!(error.code, "program/rejected");
}

#[test]
fn bounded_foreach_transforms_a_finite_collection_into_tx_data() {
    let db = database();
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::ForEach {
                body: vec![Instruction::Unpack(2), Instruction::EmitAdd(BALANCE)],
            },
            Instruction::Return,
        ],
    };
    let items = RuntimeValue::Vector(vec![
        RuntimeValue::Vector(vec![
            RuntimeValue::Entity(EntityRef::Id(user(50))),
            RuntimeValue::Scalar(Value::Long(10)),
        ]),
        RuntimeValue::Vector(vec![
            RuntimeValue::Entity(EntityRef::Id(user(51))),
            RuntimeValue::Scalar(Value::Long(20)),
        ]),
    ]);
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute_runtime(&program, &db, &[items], ProgramControl::default())
        .unwrap()
    else {
        panic!("wrong output kind");
    };
    assert_eq!(forms.len(), 2);
    let report = db.with_forms(&forms, &TxFunctions::new(), 2_000).unwrap();
    assert_eq!(
        report.db_after.values(user(50), BALANCE),
        vec![&Value::Long(10)]
    );
    assert_eq!(
        report.db_after.values(user(51), BALANCE),
        vec![&Value::Long(20)]
    );

    let empty = ProgramRuntime
        .execute_runtime(
            &program,
            &db,
            &[RuntimeValue::Vector(Vec::new())],
            ProgramControl::default(),
        )
        .unwrap();
    assert!(matches!(empty, ProgramOutput::Transaction(forms) if forms.is_empty()));
}

#[test]
fn load_many_and_foreach_perform_an_indexed_local_join() {
    let db = database();
    let query = Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadMany(MEMBERS),
            Instruction::ForEach {
                body: vec![
                    Instruction::Duplicate,
                    Instruction::LoadOne(NAME),
                    Instruction::EmitRow(2),
                ],
            },
            Instruction::Return,
        ],
    };
    let output = ProgramRuntime
        .execute(
            &query,
            &db,
            &[Value::Ref(user(90))],
            ProgramControl::default(),
        )
        .unwrap();
    assert!(matches!(
        output,
        ProgramOutput::Query(rows)
            if rows == vec![
                vec![Value::Ref(user(42)), Value::String("Ada".into())],
                vec![Value::Ref(user(43)), Value::String("Grace".into())],
            ]
    ));

    // The same indexed operators are transaction-function capabilities, not
    // merely a standalone query-program feature. Every iteration can still
    // address the outer call arguments while its item stack stays isolated.
    let copy_member_names = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadMany(MEMBERS),
            Instruction::ForEach {
                body: vec![
                    Instruction::LoadOne(NAME),
                    Instruction::PushArgument(0),
                    Instruction::Swap,
                    Instruction::EmitAdd(LABELS),
                ],
            },
            Instruction::Return,
        ],
    };
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute(
            &copy_member_names,
            &db,
            &[Value::Ref(user(90))],
            ProgramControl::default(),
        )
        .unwrap()
    else {
        panic!("wrong output kind");
    };
    let report = db.with_forms(&forms, &TxFunctions::new(), 2_000).unwrap();
    assert_eq!(
        report.db_after.values(user(90), LABELS),
        vec![&Value::String("Ada".into()), &Value::String("Grace".into())]
    );
}

#[test]
fn database_functions_have_a_real_conjunctive_datalog_host() {
    let db = database();

    // `datomic.function/compile-clojure` deliberately imports `d/q` and `db`,
    // and the transaction model promises declarative Datalog over the valid
    // immutable db value. Binding an unknown entity from a value is therefore
    // a query operation, not entity-first collection traversal.
    let by_name = QueryTemplate::new(
        vec![0],
        vec![QueryPattern::new(
            QueryTerm::Variable(0),
            NAME,
            QueryTerm::Constant(Value::String("Ada".into())),
        )],
    )
    .unwrap();
    let query = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::Query(by_name),
            Instruction::ForEach {
                body: vec![Instruction::Unpack(1), Instruction::EmitRow(1)],
            },
            Instruction::Return,
        ],
    };
    assert!(matches!(
        ProgramRuntime
            .execute(&query, &db, &[], ProgramControl::default())
            .unwrap(),
        ProgramOutput::Query(rows) if rows == vec![vec![Value::Ref(user(42))]]
    ));

    // A cardinality-many membership pattern joins a second pattern inside a
    // transaction function. Only the returned data is later applied.
    let member_names = QueryTemplate::new(
        vec![1],
        vec![
            QueryPattern::new(QueryTerm::Variable(0), NAME, QueryTerm::Variable(1)),
            QueryPattern::new(QueryTerm::Input(0), MEMBERS, QueryTerm::Variable(0)),
        ],
    )
    .unwrap();
    let copy_member_names = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::Query(member_names),
            Instruction::ForEach {
                body: vec![
                    Instruction::Unpack(1),
                    Instruction::PushArgument(0),
                    Instruction::Swap,
                    Instruction::EmitAdd(LABELS),
                ],
            },
            Instruction::Return,
        ],
    };
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute(
            &copy_member_names,
            &db,
            &[Value::Ref(user(90))],
            ProgramControl::default(),
        )
        .unwrap()
    else {
        panic!("wrong output kind");
    };
    let report = db.with_forms(&forms, &TxFunctions::new(), 2_000).unwrap();
    assert_eq!(
        report.db_after.values(user(90), LABELS),
        vec![&Value::String("Ada".into()), &Value::String("Grace".into())]
    );
}

#[test]
fn query_templates_are_canonical_versioned_and_checked() {
    let first = QueryPattern::new(QueryTerm::Input(0), MEMBERS, QueryTerm::Variable(0));
    let second = QueryPattern::new(QueryTerm::Variable(0), NAME, QueryTerm::Variable(1));
    let forward = QueryTemplate::new(vec![0, 1], vec![first.clone(), second.clone()]).unwrap();
    let reverse = QueryTemplate::new(vec![0, 1], vec![second, first.clone()]).unwrap();
    assert_eq!(forward, reverse);
    assert_eq!(forward.version(), atomic_core::QUERY_TEMPLATE_VERSION);
    assert_eq!(
        QueryTemplate::new(vec![0], vec![first.clone(), first])
            .unwrap_err()
            .code,
        "program/noncanonical-query"
    );

    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::Query(forward),
            Instruction::Pop,
            Instruction::Return,
        ],
    };
    let bytes = encode_program(&program).unwrap();
    assert_eq!(decode_program(&bytes).unwrap(), program);

    // Header (16), ABI/kind/arity (4), top-level count (4), instruction tag
    // (1), then the query-template version.
    let mut unsupported = bytes;
    assert_eq!(unsupported[24], 37);
    unsupported[25..27].copy_from_slice(&99u16.to_be_bytes());
    let checksum_at = unsupported.len() - 32;
    let checksum = atomic_core::sha256(&unsupported[..checksum_at]);
    unsupported[checksum_at..].copy_from_slice(&checksum);
    let error = decode_program(&unsupported).unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (
            ErrorCategory::Unsupported,
            "encoding/unsupported-query-template-version"
        )
    );
}

#[test]
fn query_work_is_bounded_and_a_failed_query_does_not_reset_shared_budget() {
    let db = database();
    let all_named_entities = QueryTemplate::new(
        vec![0],
        vec![QueryPattern::new(
            QueryTerm::Variable(0),
            NAME,
            QueryTerm::Variable(1),
        )],
    )
    .unwrap();
    let query = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::Query(all_named_entities),
            Instruction::Pop,
            Instruction::Return,
        ],
    };
    let mut budget = ProgramBudget::new(ProgramControl {
        fuel: 10_000,
        max_collection_items: 1,
        ..ProgramControl::default()
    })
    .unwrap();
    let before = budget.remaining_fuel();
    let error = ProgramRuntime
        .execute_runtime_with_budget(&query, &db, &[], &mut budget)
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Busy, "program/query-intermediate-limit")
    );
    let after_failure = budget.remaining_fuel();
    assert!(after_failure < before);

    let empty = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![Instruction::Return],
    };
    ProgramRuntime
        .execute_runtime_with_budget(&empty, &db, &[], &mut budget)
        .unwrap();
    assert!(budget.remaining_fuel() < after_failure);
    assert_eq!(budget.calls(), 2);

    let error = ProgramRuntime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                fuel: 3,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Busy, "program/fuel-exhausted")
    );
}

#[test]
fn structured_emitters_preserve_cas_ensure_and_transaction_local_entities() {
    let db = database();
    let spec = Keyword::new("account", "spec");
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushEntity(EntityRef::Id(user(42))),
            Instruction::PushConstant(Value::Long(40)),
            Instruction::PushConstant(Value::Long(41)),
            Instruction::EmitCas(BALANCE),
            Instruction::PushEntity(EntityRef::Id(user(43))),
            Instruction::PushNull,
            Instruction::PushConstant(Value::Long(1)),
            Instruction::EmitCas(BALANCE),
            Instruction::PushEntity(EntityRef::Temp("fresh".into())),
            Instruction::PushConstant(Value::Long(9)),
            Instruction::EmitAdd(BALANCE),
            Instruction::PushEntity(EntityRef::Tx),
            Instruction::PushConstant(Value::String("audit".into())),
            Instruction::EmitAdd(NAME),
            Instruction::PushEntity(EntityRef::Id(user(42))),
            Instruction::PushEntity(EntityRef::Ident(spec.clone())),
            Instruction::EmitEnsure,
            Instruction::PushEntity(EntityRef::Id(user(99))),
            Instruction::EmitRetractEntity,
            Instruction::Return,
        ],
    };
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute(&program, &db, &[], ProgramControl::default())
        .unwrap()
    else {
        panic!("wrong output kind");
    };
    assert!(matches!(
        forms[0],
        TxForm::Op(TxOp::Cas {
            entity: EntityRef::Id(entity),
            attribute: BALANCE,
            old: Some(TxValue::Scalar(Value::Long(40))),
            new: TxValue::Scalar(Value::Long(41)),
        }) if entity == user(42)
    ));
    assert!(matches!(
        forms[1],
        TxForm::Op(TxOp::Cas {
            entity: EntityRef::Id(entity),
            attribute: BALANCE,
            old: None,
            new: TxValue::Scalar(Value::Long(1)),
        }) if entity == user(43)
    ));
    assert!(matches!(
        forms[2],
        TxForm::Op(TxOp::Add {
            entity: EntityRef::Temp(ref temp),
            attribute: BALANCE,
            ..
        }) if temp == "fresh"
    ));
    assert!(matches!(
        forms[3],
        TxForm::Op(TxOp::Add {
            entity: EntityRef::Tx,
            attribute: NAME,
            ..
        })
    ));
    assert!(matches!(
        forms[4],
        TxForm::Op(TxOp::Ensure {
            entity: EntityRef::Id(entity),
            spec: EntityRef::Ident(ref actual),
        }) if entity == user(42) && actual == &spec
    ));
    assert!(matches!(
        forms[5],
        TxForm::Op(TxOp::RetractEntity(EntityRef::Id(entity))) if entity == user(99)
    ));
}

#[test]
fn persisted_runtime_emits_value_less_retracts_and_full_entity_maps() {
    let db = database();
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::EmitRetractAll(LABELS),
            Instruction::PushArgument(1),
            Instruction::EmitEntityMap,
            Instruction::Return,
        ],
    };
    assert_eq!(
        decode_program(&encode_program(&program).unwrap()).unwrap(),
        program
    );

    let child = RuntimeValue::map(vec![
        (
            Value::Keyword(Keyword::new("db", "id")),
            RuntimeValue::Scalar(Value::String("child".into())),
        ),
        (
            Value::Keyword(Keyword::new("person", "name")),
            RuntimeValue::Scalar(Value::String("Grace".into())),
        ),
    ])
    .unwrap();
    let map = RuntimeValue::map(vec![
        (
            Value::Keyword(Keyword::new("db", "id")),
            RuntimeValue::Entity(EntityRef::Temp("parent".into())),
        ),
        (
            Value::Keyword(Keyword::new("person", "name")),
            RuntimeValue::Scalar(Value::String("Ada".into())),
        ),
        (
            Value::Keyword(Keyword::new("team", "labels")),
            RuntimeValue::Vector(vec![
                RuntimeValue::Scalar(Value::String("mathematician".into())),
                RuntimeValue::Scalar(Value::String("programmer".into())),
            ]),
        ),
        (
            Value::Keyword(Keyword::new("team", "members")),
            RuntimeValue::Vector(vec![child]),
        ),
        (
            Value::Keyword(Keyword::new("team", "_members")),
            RuntimeValue::Entity(EntityRef::Id(user(91))),
        ),
    ])
    .unwrap();
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute_runtime(
            &program,
            &db,
            &[RuntimeValue::Entity(EntityRef::Id(user(90))), map],
            ProgramControl::default(),
        )
        .unwrap()
    else {
        panic!("wrong output kind");
    };

    assert!(matches!(
        &forms[0],
        TxForm::Op(TxOp::Retract {
            entity: EntityRef::Id(entity),
            attribute: LABELS,
            value: None,
        }) if *entity == user(90)
    ));
    let TxForm::EntityMap(entity_map) = &forms[1] else {
        panic!("runtime map was not preserved as transaction map data");
    };
    assert!(matches!(
        &entity_map.id,
        Some(EntityRef::Temp(tempid)) if tempid == "parent"
    ));
    assert!(entity_map.attributes.iter().any(|(attribute, value)| {
        matches!(
            (attribute, value),
            (
                AttributeRef::Ident(ident),
                MapValue::Many(values),
            ) if ident == &Keyword::new("team", "labels") && values.len() == 2
        )
    }));
    assert!(entity_map.attributes.iter().any(|(attribute, value)| {
        matches!(
            (attribute, value),
            (
                AttributeRef::Ident(ident),
                MapValue::Many(values),
            ) if ident == &Keyword::new("team", "members")
                && matches!(values.as_slice(), [MapValue::Nested(child)]
                    if matches!(&child.id, Some(EntityRef::Temp(tempid)) if tempid == "child"))
        )
    }));
    assert!(entity_map.attributes.iter().any(|(attribute, value)| {
        matches!(
            (attribute, value),
            (
                AttributeRef::ReverseIdent(ident),
                MapValue::Value(TxValue::Entity(EntityRef::Id(entity))),
            ) if ident == &Keyword::new("team", "members") && *entity == user(91)
        )
    }));
}

#[test]
fn entity_map_emission_rejects_invalid_shapes_and_obeys_output_bytes() {
    let db = database();
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::EmitEntityMap,
            Instruction::Return,
        ],
    };

    let unqualified = RuntimeValue::map(vec![(
        Value::Keyword(Keyword::unqualified("name")),
        RuntimeValue::Scalar(Value::String("Ada".into())),
    )])
    .unwrap();
    assert_eq!(
        ProgramRuntime
            .execute_runtime(&program, &db, &[unqualified], ProgramControl::default(),)
            .unwrap_err()
            .code,
        "program/entity-map-key"
    );

    let nameless_reverse = RuntimeValue::map(vec![(
        Value::Keyword(Keyword::new("team", "_")),
        RuntimeValue::Entity(EntityRef::Id(user(42))),
    )])
    .unwrap();
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &program,
                &db,
                &[nameless_reverse],
                ProgramControl::default(),
            )
            .unwrap_err()
            .code,
        "program/entity-map-key"
    );

    let nil_value = RuntimeValue::map(vec![(
        Value::Keyword(Keyword::new("person", "name")),
        RuntimeValue::Null,
    )])
    .unwrap();
    assert_eq!(
        ProgramRuntime
            .execute_runtime(&program, &db, &[nil_value], ProgramControl::default(),)
            .unwrap_err()
            .code,
        "program/entity-map-value"
    );

    let nested_collection = RuntimeValue::map(vec![(
        Value::Keyword(Keyword::new("team", "labels")),
        RuntimeValue::Vector(vec![RuntimeValue::Vector(vec![])]),
    )])
    .unwrap();
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &program,
                &db,
                &[nested_collection],
                ProgramControl::default(),
            )
            .unwrap_err()
            .code,
        "program/entity-map-collection"
    );

    let large = RuntimeValue::map(vec![(
        Value::Keyword(Keyword::new("person", "name")),
        RuntimeValue::Scalar(Value::String("x".repeat(256))),
    )])
    .unwrap();
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &program,
                &db,
                &[large],
                ProgramControl {
                    max_output: 32,
                    ..ProgramControl::default()
                },
            )
            .unwrap_err()
            .code,
        "program/output-limit"
    );
}

#[test]
fn one_budget_spans_many_invocations_and_emitted_forms() {
    let db = database();
    let empty = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![Instruction::Return],
    };
    let mut budget = ProgramBudget::new(ProgramControl {
        fuel: 2,
        max_calls: 10,
        ..ProgramControl::default()
    })
    .unwrap();
    let runtime = ProgramRuntime;
    runtime
        .execute_with_budget(&empty, &db, &[], &mut budget)
        .unwrap();
    runtime
        .execute_with_budget(&empty, &db, &[], &mut budget)
        .unwrap();
    let error = runtime
        .execute_with_budget(&empty, &db, &[], &mut budget)
        .unwrap_err();
    assert_eq!(error.code, "program/fuel-exhausted");
    assert_eq!(budget.calls(), 3);

    let emitting = Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushArgument(1),
            Instruction::EmitAdd(BALANCE),
            Instruction::Return,
        ],
    };
    let mut budget = ProgramBudget::new(ProgramControl {
        fuel: 10_000,
        max_forms: 1,
        ..ProgramControl::default()
    })
    .unwrap();
    let arguments = [Value::Ref(user(42)), Value::Long(1)];
    runtime
        .execute_with_budget(&emitting, &db, &arguments, &mut budget)
        .unwrap();
    let error = runtime
        .execute_with_budget(&emitting, &db, &arguments, &mut budget)
        .unwrap_err();
    assert_eq!(error.code, "program/form-limit");
    assert_eq!(budget.emitted_forms(), 1);
    assert!(budget.emitted_bytes() > 0);
}

#[test]
fn validation_and_all_runtime_limits_fail_closed() {
    let db = database();
    let invalid = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![Instruction::Pop, Instruction::Return],
    };
    assert_eq!(
        invalid.validate().unwrap_err().code,
        "program/static-stack-underflow"
    );

    let mismatched_branches = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Bool(true)),
            Instruction::If {
                then_branch: vec![Instruction::PushConstant(Value::Long(1))],
                else_branch: Vec::new(),
            },
            Instruction::Return,
        ],
    };
    assert_eq!(
        mismatched_branches.validate().unwrap_err().code,
        "program/branch-stack-shape"
    );
    let leaking_loop = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::ForEach { body: Vec::new() },
            Instruction::Return,
        ],
    };
    assert_eq!(
        leaking_loop.validate().unwrap_err().code,
        "program/foreach-stack-shape"
    );

    let query = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(1)),
            Instruction::PushConstant(Value::Long(2)),
            Instruction::Return,
        ],
    };
    let runtime = ProgramRuntime;
    let error = runtime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                fuel: 1,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Busy, "program/fuel-exhausted")
    );

    let error = runtime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                max_stack: 1,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/stack-limit");

    let cancelled = AtomicBool::new(true);
    let error = runtime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                cancelled: Some(&cancelled),
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Interrupted, "program/cancelled")
    );

    let error = runtime
        .execute(
            &increment(),
            &db,
            &[Value::Long(1_000)],
            ProgramControl::default(),
        )
        .unwrap_err();
    assert_eq!(error.code, "program/type");

    let collection_program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::ForEach {
                body: vec![Instruction::Pop],
            },
            Instruction::Return,
        ],
    };
    let error = runtime
        .execute_runtime(
            &collection_program,
            &db,
            &[RuntimeValue::Vector(vec![
                RuntimeValue::Null,
                RuntimeValue::Null,
            ])],
            ProgramControl {
                max_collection_items: 1,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/collection-limit");

    assert_eq!(
        RuntimeValue::map(vec![
            (Value::Long(1), RuntimeValue::Null),
            (Value::Double(1.0), RuntimeValue::Null),
        ])
        .unwrap_err()
        .code,
        "program/duplicate-map-key"
    );
    let ignores_argument = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![Instruction::Return],
    };
    let noncanonical = RuntimeValue::Map(vec![
        (Value::Long(2), RuntimeValue::Null),
        (Value::Long(1), RuntimeValue::Null),
    ]);
    assert_eq!(
        runtime
            .execute_runtime(
                &ignores_argument,
                &db,
                &[noncanonical],
                ProgramControl::default(),
            )
            .unwrap_err()
            .code,
        "program/noncanonical-map"
    );

    let mut too_deep_runtime = RuntimeValue::Null;
    for _ in 0..18 {
        too_deep_runtime = RuntimeValue::Vector(vec![too_deep_runtime]);
    }
    assert_eq!(
        runtime
            .execute_runtime(
                &ignores_argument,
                &db,
                &[too_deep_runtime],
                ProgramControl::default(),
            )
            .unwrap_err()
            .code,
        "program/value-depth"
    );

    let error = runtime
        .execute_runtime(
            &ignores_argument,
            &db,
            &[RuntimeValue::Vector(vec![
                RuntimeValue::Null,
                RuntimeValue::Null,
            ])],
            ProgramControl {
                max_collection_items: 1,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/collection-limit");

    let allocation = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Bytes(vec![0; 32])),
            Instruction::Return,
        ],
    };
    let error = runtime
        .execute(
            &allocation,
            &db,
            &[],
            ProgramControl {
                max_value_bytes: 8,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/value-byte-limit");
}

#[test]
fn output_and_fuel_limits_account_for_value_bytes_not_only_value_count() {
    let db = database();
    let query = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Bytes(vec![0; 256])),
            Instruction::Return,
        ],
    };

    let output_error = ProgramRuntime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                fuel: 10_000,
                max_output: 64,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(
        (output_error.category, output_error.code),
        (ErrorCategory::Busy, "program/output-limit")
    );

    let fuel_error = ProgramRuntime
        .execute(
            &query,
            &db,
            &[],
            ProgramControl {
                fuel: 32,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(
        (fuel_error.category, fuel_error.code),
        (ErrorCategory::Busy, "program/fuel-exhausted")
    );
}

#[test]
fn cancellation_categories_match_datomic_cancel_and_values_are_depth_bounded() {
    let db = database();
    for category in [ErrorCategory::Incorrect, ErrorCategory::Conflict] {
        let cancel = Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![
                Instruction::PushConstant(Value::Bool(false)),
                Instruction::Require {
                    category,
                    message: "rejected".into(),
                },
                Instruction::Return,
            ],
        };
        let error = ProgramRuntime
            .execute(&cancel, &db, &[], ProgramControl::default())
            .unwrap_err();
        assert_eq!((error.category, error.code), (category, "program/rejected"));
    }

    let forbidden = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Bool(false)),
            Instruction::Require {
                category: ErrorCategory::Forbidden,
                message: "not a Datomic cancel category".into(),
            },
            Instruction::Return,
        ],
    };
    assert_eq!(
        forbidden.validate().unwrap_err().code,
        "program/rejection-category"
    );

    let mut nested = Value::Long(1);
    for _ in 0..18 {
        nested = Value::Tuple(vec![Some(nested)]);
    }
    let too_deep = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![Instruction::PushConstant(nested), Instruction::Return],
    };
    assert_eq!(
        ProgramRuntime
            .execute(&too_deep, &db, &[], ProgramControl::default())
            .unwrap_err()
            .code,
        "program/value-depth"
    );
}

#[test]
fn structured_cancel_preserves_dynamic_anomaly_data() {
    let db = database();
    let cancel = Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushArgument(1),
            Instruction::RequireAnomaly,
            Instruction::Return,
        ],
    };
    assert_eq!(
        decode_program(&encode_program(&cancel).unwrap()).unwrap(),
        cancel
    );
    let anomaly = RuntimeValue::map(vec![
        (
            Value::Keyword(Keyword::new("cognitect.anomalies", "category")),
            RuntimeValue::Scalar(Value::Keyword(Keyword::new(
                "cognitect.anomalies",
                "conflict",
            ))),
        ),
        (
            Value::Keyword(Keyword::new("cognitect.anomalies", "message")),
            RuntimeValue::Scalar(Value::String("account changed".into())),
        ),
        (
            Value::Keyword(Keyword::new("account", "expected")),
            RuntimeValue::Scalar(Value::Long(40)),
        ),
        (
            Value::Keyword(Keyword::new("account", "context")),
            RuntimeValue::Vector(vec![
                RuntimeValue::Scalar(Value::Long(40)),
                RuntimeValue::Scalar(Value::Bool(false)),
            ]),
        ),
    ])
    .unwrap();
    let mut expected_entries = match anomaly.clone() {
        RuntimeValue::Map(entries) => entries,
        _ => unreachable!("constructed a runtime map"),
    };
    expected_entries.push((
        Value::Keyword(Keyword::new("datomic", "cancelled")),
        RuntimeValue::Scalar(Value::Bool(true)),
    ));
    let expected_anomaly = RuntimeValue::map(expected_entries).unwrap();
    let error = ProgramRuntime
        .execute_runtime(
            &cancel,
            &db,
            &[RuntimeValue::Scalar(Value::Bool(false)), anomaly.clone()],
            ProgramControl::default(),
        )
        .unwrap_err();
    assert_eq!(
        (error.category, error.code, error.message.as_str()),
        (
            ErrorCategory::Conflict,
            "program/rejected",
            "account changed"
        )
    );
    assert_eq!(error.details["account/expected"], "40");
    assert_eq!(error.details["datomic/cancelled"], "true");
    assert_eq!(error.anomaly, Some(Box::new(expected_anomaly)));

    let success = ProgramRuntime
        .execute_runtime(
            &cancel,
            &db,
            &[RuntimeValue::Scalar(Value::Bool(true)), anomaly],
            ProgramControl::default(),
        )
        .unwrap();
    assert!(matches!(success, ProgramOutput::Transaction(forms) if forms.is_empty()));

    let missing_category = RuntimeValue::map(vec![(
        Value::Keyword(Keyword::new("app", "reason")),
        RuntimeValue::Scalar(Value::String("no".into())),
    )])
    .unwrap();
    assert_eq!(
        ProgramRuntime
            .execute_runtime(
                &cancel,
                &db,
                &[RuntimeValue::Scalar(Value::Bool(false)), missing_category,],
                ProgramControl::default(),
            )
            .unwrap_err()
            .code,
        "program/cancel-category"
    );
}

#[test]
fn canonical_program_payload_has_a_dedicated_finite_limit() {
    const PROGRAM_LIMIT: usize = 4 * 1024 * 1024;
    let oversized = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Bytes(vec![0; PROGRAM_LIMIT])),
            Instruction::Return,
        ],
    };
    assert_eq!(
        encode_program(&oversized).unwrap_err().code,
        "program/payload-limit"
    );
    assert_eq!(
        decode_program(&vec![0; PROGRAM_LIMIT + 1])
            .unwrap_err()
            .code,
        "encoding/program-payload-limit"
    );
}

#[test]
fn independent_decodes_produce_byte_identical_observations() {
    let db = database();
    let bytes = encode_program(&increment()).unwrap();
    let left_program = decode_program(&bytes).unwrap();
    let right_program = decode_program(&bytes).unwrap();
    let left = ProgramRuntime
        .execute(
            &left_program,
            &db,
            &[Value::Ref(user(42))],
            ProgramControl::default(),
        )
        .unwrap();
    let right = ProgramRuntime
        .execute(
            &right_program,
            &db.clone(),
            &[Value::Ref(user(42))],
            ProgramControl::default(),
        )
        .unwrap();
    assert_eq!(
        encode_program_output(&left).unwrap(),
        encode_program_output(&right).unwrap()
    );
}

fn hex(bytes: &[u8]) -> String {
    bytes.iter().map(|byte| format!("{byte:02x}")).collect()
}
