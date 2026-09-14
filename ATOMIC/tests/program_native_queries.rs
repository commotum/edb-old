//! Durable native queries use the same AST and evaluator as ordinary queries.
mod common;

use atomic_core::*;
use std::{collections::BTreeSet, sync::atomic::AtomicBool, time::Duration};

const EDGE: u32 = 1_000;
const SCORE: u32 = 1_001;
const BLOCKED: u32 = 1_002;
const CHOSEN: u32 = 1_003;
const OTHER_CHOSEN: u32 = 1_004;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, name, ty, cardinality) in [
        (EDGE, "edge", ValueType::Ref, Cardinality::Many),
        (SCORE, "score", ValueType::Long, Cardinality::One),
        (BLOCKED, "blocked", ValueType::Boolean, Cardinality::One),
        (CHOSEN, "chosen", ValueType::Long, Cardinality::Many),
        (
            OTHER_CHOSEN,
            "other-chosen",
            ValueType::Long,
            Cardinality::Many,
        ),
    ] {
        schema
            .install(Attribute::new(
                id,
                Keyword::new("graph", name),
                ty,
                cardinality,
            ))
            .unwrap();
    }
    schema
}

fn seed() -> Vec<TxOp> {
    let mut ops = Vec::new();
    for (from, to) in [("root", "middle"), ("middle", "leaf"), ("root", "low")] {
        ops.push(TxOp::Add {
            entity: EntityRef::Temp(from.into()),
            attribute: EDGE,
            value: TxValue::Entity(EntityRef::Temp(to.into())),
        });
    }
    for (name, score) in [("middle", 10), ("leaf", 30), ("low", 4)] {
        ops.push(TxOp::Add {
            entity: EntityRef::Temp(name.into()),
            attribute: SCORE,
            value: Value::Long(score).into(),
        });
    }
    ops
}

fn changes(ids: &std::collections::BTreeMap<String, u64>) -> Vec<TxOp> {
    let mut ops = Vec::new();
    for (name, score) in [("middle", 20), ("leaf", 40), ("low", 5)] {
        ops.push(TxOp::Add {
            entity: EntityRef::Id(ids[name]),
            attribute: SCORE,
            value: Value::Long(score).into(),
        });
    }
    ops.push(TxOp::Add {
        entity: EntityRef::Id(ids["middle"]),
        attribute: BLOCKED,
        value: Value::Bool(true).into(),
    });
    ops
}

fn var(name: &str) -> Term {
    Term::var(name)
}
fn attr(id: u32) -> Term {
    Term::Constant(Value::Ref(u64::from(id)))
}
fn pattern(entity: Term, attribute: Term, value: Term) -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(entity, attribute, value)))
}

// All five persisted capabilities contribute to this result: the selected
// leaf needs recursion; middle is negated; low fails the predicate; the
// attribute is an input; and the result comes from matching two past views.
fn selector_template() -> QueryTemplate {
    let mut historical = DataPattern::new(var("entity"), var("attribute"), var("old"));
    historical.source = "$history".into();
    historical.transaction = Some(var("tx"));
    historical.added = Some(Term::Constant(Value::Bool(false)));
    let mut past = DataPattern::new(var("entity"), var("attribute"), var("old"));
    past.source = "$then".into();
    let mut query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable("entity".into()),
            FindElement::Variable("old".into()),
        ]),
        vec![
            Clause::Rule {
                source: "$".into(),
                name: "reachable".into(),
                args: vec![var("root"), var("entity")],
            },
            pattern(var("entity"), var("attribute"), var("score")),
            Clause::Predicate {
                predicate: Predicate::GreaterOrEqual,
                source: "$".into(),
                args: vec![var("score"), var("minimum")],
            },
            Clause::Not {
                join: Some(vec!["entity".into()]),
                clauses: vec![pattern(
                    var("entity"),
                    attr(BLOCKED),
                    Term::Constant(Value::Bool(true)),
                )],
            },
            Clause::Pattern(Box::new(historical)),
            Clause::Pattern(Box::new(past)),
        ],
    );
    query.inputs = vec![
        InputSpec::Scalar("root".into()),
        InputSpec::Scalar("minimum".into()),
        InputSpec::Scalar("attribute".into()),
    ];
    query.rules = vec![
        Rule {
            name: "reachable".into(),
            head: vec!["from".into(), "to".into()],
            required: BTreeSet::new(),
            clauses: vec![pattern(var("from"), attr(EDGE), var("to"))],
        },
        Rule {
            name: "reachable".into(),
            head: vec!["from".into(), "to".into()],
            required: BTreeSet::new(),
            clauses: vec![
                pattern(var("from"), attr(EDGE), var("middle")),
                Clause::Rule {
                    source: "$".into(),
                    name: "reachable".into(),
                    args: vec![var("middle"), var("to")],
                },
            ],
        },
    ];
    QueryTemplate::native(
        query,
        vec![0, 1, 2],
        vec![
            QueryTemplateSource::current("$"),
            QueryTemplateSource::history("$history"),
            QueryTemplateSource::current("$then").as_of(QueryTemplateTime::Argument(3)),
        ],
    )
    .unwrap()
}

fn selector(kind: ProgramKind, output_attribute: u32) -> Program {
    Program {
        kind,
        arity: 4,
        instructions: vec![
            Instruction::Query(selector_template()),
            Instruction::ForEach {
                body: vec![
                    Instruction::Unpack(2),
                    if kind == ProgramKind::Query {
                        Instruction::EmitRow(2)
                    } else {
                        Instruction::EmitAdd(output_attribute)
                    },
                ],
            },
            Instruction::Return,
        ],
    }
}

fn arguments(root: u64, seed_basis: u64) -> Vec<Value> {
    vec![
        Value::Ref(root),
        Value::Long(10),
        Value::Ref(u64::from(SCORE)),
        Value::Long(seed_basis as i64),
    ]
}

fn rows(program: &Program, database: &DatabaseValue, args: &[Value]) -> Vec<Vec<Value>> {
    let ProgramOutput::Query(rows) = ProgramRuntime
        .execute_query(program, database, args, ProgramControl::default())
        .unwrap()
    else {
        panic!("expected query rows")
    };
    rows
}

#[test]
fn compact_queries_share_native_semantics_and_preserve_canonical_row_order() {
    let seeded = Database::new(schema())
        .unwrap()
        .with(&seed(), 1_000)
        .unwrap();
    let database = seeded.db_after.database_value();
    let compact = QueryTemplate::new(
        vec![1, 0],
        vec![QueryPattern::new(
            QueryTerm::Variable(0),
            SCORE,
            QueryTerm::Variable(1),
        )],
    )
    .unwrap();
    let native = QueryTemplate::native(compact.native_query().clone(), vec![], vec![]).unwrap();
    let program = |template| Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::Query(template),
            Instruction::ForEach {
                body: vec![Instruction::Unpack(2), Instruction::EmitRow(2)],
            },
            Instruction::Return,
        ],
    };
    let compact = program(compact);
    let native = program(native);
    let expected = vec![
        vec![Value::Long(4), Value::Ref(seeded.tempids["low"])],
        vec![Value::Long(10), Value::Ref(seeded.tempids["middle"])],
        vec![Value::Long(30), Value::Ref(seeded.tempids["leaf"])],
    ];
    assert_eq!(rows(&compact, &database, &[]), expected);
    let native_rows = rows(&native, &database, &[]);
    assert_eq!(native_rows.len(), expected.len());
    assert!(expected.iter().all(|row| native_rows.contains(row)));
    for program in [compact, native] {
        let bytes = encode_program(&program).unwrap();
        assert_eq!(decode_program(&bytes).unwrap(), program);
        assert_eq!(
            rows(&decode_program(&bytes).unwrap(), &database, &[]),
            rows(&program, &database, &[])
        );
    }
}

#[test]
fn native_query_program_combines_rules_predicate_not_history_and_dynamic_attribute() {
    let first = Database::new(schema())
        .unwrap()
        .with(&seed(), 1_000)
        .unwrap();
    let after = first
        .db_after
        .with(&changes(&first.tempids), 2_000)
        .unwrap()
        .db_after
        .database_value();
    let program = selector(ProgramKind::Query, CHOSEN);
    let bytes = encode_program(&program).unwrap();
    assert_eq!(
        &bytes[16..18],
        &atomic_core::PROGRAM_ABI_VERSION.to_be_bytes()
    );
    assert_eq!(decode_program(&bytes).unwrap(), program);
    let args = arguments(first.tempids["root"], first.db_after.basis_t());
    assert_eq!(
        rows(&program, &after, &args),
        vec![vec![Value::Ref(first.tempids["leaf"]), Value::Long(30)]]
    );
    // Native nested-query execution must carry the same named temporal
    // sources and recursive rule behavior through the durable artifact.
    let inner = selector_template().native_query().clone();
    let mut nested = Query::new(
        inner.find.clone(),
        vec![Clause::Function {
            function: Function::Query(Box::new(inner.clone())),
            source: "$".into(),
            args: vec![var("root"), var("minimum"), var("attribute")],
            binding: Binding::Relation(vec![Some("entity".into()), Some("old".into())]),
        }],
    );
    nested.inputs = inner.inputs;
    let mut nested_program = program.clone();
    nested_program.instructions[0] = Instruction::Query(
        QueryTemplate::native(
            nested,
            vec![0, 1, 2],
            vec![
                QueryTemplateSource::current("$"),
                QueryTemplateSource::history("$history"),
                QueryTemplateSource::current("$then").as_of(QueryTemplateTime::Argument(3)),
            ],
        )
        .unwrap(),
    );
    let nested_program = decode_program(&encode_program(&nested_program).unwrap()).unwrap();
    assert_eq!(
        rows(&nested_program, &after, &args),
        rows(&program, &after, &args)
    );
    // The same artifact on a later speculative value observes that branch,
    // while the original value and its historical source remain unchanged.
    let blocked = after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(first.tempids["leaf"]),
                attribute: BLOCKED,
                value: Value::Bool(true).into(),
            }],
            3_000,
        )
        .unwrap()
        .db_after;
    assert!(rows(&program, &blocked, &args).is_empty());
    assert_eq!(rows(&program, &after, &args).len(), 1);
    for bound in [
        Value::Ref(t_to_tx(first.db_after.basis_t()).unwrap()),
        Value::Instant(1_000),
    ] {
        let mut timed = args.clone();
        timed[3] = bound;
        assert_eq!(
            rows(&program, &after, &timed),
            rows(&program, &after, &args)
        );
    }
    let leaf = first.tempids["leaf"];
    let filtered = after.clone().filter(move |_, datom| {
        !(datom.entity == leaf && datom.attribute == SCORE && !datom.added)
    });
    assert!(
        rows(&program, &filtered, &args).is_empty(),
        "derived history must retain the supplied filter"
    );
    let mut windowed = program.clone();
    windowed.instructions[0] = Instruction::Query(
        QueryTemplate::native(
            selector_template().native_query().clone(),
            vec![0, 1, 2],
            vec![
                QueryTemplateSource::current("$")
                    .as_of(QueryTemplateTime::Literal(TimePoint::Instant(2_000))),
                QueryTemplateSource::history("$history").since(QueryTemplateTime::Literal(
                    TimePoint::T(first.db_after.basis_t()),
                )),
                QueryTemplateSource::current("$then").as_of(QueryTemplateTime::Literal(
                    TimePoint::Tx(t_to_tx(first.db_after.basis_t()).unwrap()),
                )),
            ],
        )
        .unwrap(),
    );
    let windowed = decode_program(&encode_program(&windowed).unwrap()).unwrap();
    assert_eq!(
        rows(&windowed, &after, &args),
        rows(&program, &after, &args)
    );
}

#[test]
fn native_query_shares_program_work_allocation_and_cancellation_limits() {
    let first = Database::new(schema())
        .unwrap()
        .with(&seed(), 1_000)
        .unwrap();
    let after = first
        .db_after
        .with(&changes(&first.tempids), 2_000)
        .unwrap()
        .db_after
        .database_value();
    let program = selector(ProgramKind::Query, CHOSEN);
    let args = arguments(first.tempids["root"], first.db_after.basis_t());
    // Measure the reusable query after its static rule analysis is cached.
    // A cold invocation may additionally pay the one-time analysis cost.
    rows(&program, &after, &args);
    let mut budget = ProgramBudget::new(ProgramControl::default()).unwrap();
    let before = budget.remaining_fuel();
    ProgramRuntime
        .execute_query_with_budget(&program, &after, &args, &mut budget)
        .unwrap();
    let used = before - budget.remaining_fuel();
    assert!(used > 10, "native scans/rules must debit the enclosing VM");
    let error = ProgramRuntime
        .execute_query(
            &program,
            &after,
            &args,
            ProgramControl {
                fuel: used - 1,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Busy);
    let mut spent = ProgramBudget::new(ProgramControl {
        fuel: used / 2,
        ..ProgramControl::default()
    })
    .unwrap();
    assert!(
        ProgramRuntime
            .execute_query_with_budget(&program, &after, &args, &mut spent)
            .is_err()
    );
    assert_eq!(
        spent.remaining_fuel(),
        0,
        "failed native work is not refunded to a reusable budget"
    );
    let cancelled = AtomicBool::new(true);
    assert_eq!(
        ProgramRuntime
            .execute_query(
                &program,
                &after,
                &args,
                ProgramControl {
                    cancelled: Some(&cancelled),
                    ..ProgramControl::default()
                }
            )
            .unwrap_err()
            .category,
        ErrorCategory::Interrupted
    );
    let error = ProgramRuntime
        .execute_query(
            &program,
            &after,
            &args,
            ProgramControl {
                max_value_bytes: 512,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Busy);
}

fn wrapped(query: Query, arity: u8) -> Program {
    Program {
        kind: ProgramKind::Query,
        arity,
        instructions: vec![
            Instruction::Query(QueryTemplate::native(query, (0..arity).collect(), vec![]).unwrap()),
            Instruction::Return,
        ],
    }
}

#[test]
fn deterministic_native_ast_variants_round_trip_and_nonportable_callbacks_reject() {
    let check = |query: Query, arity| {
        let program = wrapped(query, arity);
        assert_eq!(
            decode_program(&encode_program(&program).unwrap()).unwrap(),
            program
        );
    };
    for function in [
        Aggregate::Count,
        Aggregate::CountDistinct,
        Aggregate::Min,
        Aggregate::Max,
        Aggregate::Sum,
        Aggregate::Average,
        Aggregate::Distinct,
        Aggregate::Median,
        Aggregate::Variance,
        Aggregate::StandardDeviation,
        Aggregate::MinN(3),
        Aggregate::MaxN(3),
    ] {
        let mut query = Query::new(
            FindSpec::Scalar(FindElement::Aggregate {
                function,
                variable: "x".into(),
            }),
            vec![],
        );
        query.inputs = vec![InputSpec::Collection("x".into())];
        check(query, 1);
    }
    let inner = Query::new(
        FindSpec::Scalar(FindElement::Variable("x".into())),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![Term::Constant(Value::Long(3))],
            binding: Binding::Scalar("x".into()),
        }],
    );
    for function in [
        Function::Ground,
        Function::Add,
        Function::Subtract,
        Function::Multiply,
        Function::Divide,
        Function::Tuple,
        Function::Untuple,
        Function::GetElse,
        Function::GetSome,
        Function::Query(Box::new(inner)),
        Function::TxIds,
        Function::TxData,
    ] {
        for binding in [
            Binding::Scalar("x".into()),
            Binding::Collection("x".into()),
            Binding::Tuple(vec![Some("x".into()), None]),
            Binding::Relation(vec![Some("x".into()), None]),
        ] {
            // Runtime arity/source checks remain with the common evaluator;
            // this table witnesses that the durable codec covers every tag.
            check(
                Query::new(
                    FindSpec::Collection(FindElement::Variable("x".into())),
                    vec![Clause::Function {
                        function: function.clone(),
                        source: "$".into(),
                        args: vec![Term::Constant(Value::Long(3))],
                        binding,
                    }],
                ),
                0,
            );
        }
    }
    for predicate in [
        Predicate::Eq,
        Predicate::NotEq,
        Predicate::Less,
        Predicate::LessOrEqual,
        Predicate::Greater,
        Predicate::GreaterOrEqual,
        Predicate::Missing,
    ] {
        check(
            Query::new(
                FindSpec::Tuple(vec![FindElement::Variable("x".into())]),
                vec![
                    Clause::Function {
                        function: Function::Ground,
                        source: "$".into(),
                        args: vec![Term::Constant(Value::Long(3))],
                        binding: Binding::Scalar("x".into()),
                    },
                    Clause::Or {
                        join: Some(vec!["x".into()]),
                        branches: vec![vec![Clause::Predicate {
                            predicate,
                            source: "$".into(),
                            args: vec![var("x"), Term::Nil],
                        }]],
                    },
                    Clause::Not {
                        join: None,
                        clauses: vec![pattern(var("x"), attr(BLOCKED), Term::Blank)],
                    },
                ],
            ),
            0,
        );
    }
    for transform in [
        PullTransform::String,
        PullTransform::Keyword,
        PullTransform::Symbol,
        PullTransform::Name,
        PullTransform::Namespace,
    ] {
        let mut attribute =
            PullAttribute::reverse(AttributeName::Ident(Keyword::new("graph", "edge")));
        attribute.alias = Some(QueryValue::Tuple(vec![
            QueryValue::Nil,
            QueryValue::Scalar(Value::Long(3)),
        ]));
        attribute.default = Some(QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Long(1)),
            QueryValue::Collection(vec![QueryValue::Nil]),
        )]));
        attribute.limit = PullLimit::Unlimited;
        attribute.nested = Some(PullNested::Recursion(Some(2)));
        attribute.transform = Some(transform);
        let mut nested = PullAttribute::forward(AttributeName::Id(SCORE));
        nested.limit = PullLimit::Limit(3);
        nested.nested = Some(PullNested::Pattern(Box::new(PullPattern {
            wildcard: true,
            attributes: vec![],
        })));
        let mut query = Query::new(
            FindSpec::Scalar(FindElement::Pull {
                source: "$".into(),
                variable: "e".into(),
                pattern: Box::new(PullPattern {
                    wildcard: false,
                    attributes: vec![attribute, nested],
                }),
            }),
            vec![],
        );
        query.inputs = vec![InputSpec::Scalar("e".into())];
        check(query, 1);
    }
    for function in [Aggregate::Rand(2), Aggregate::Sample(2)] {
        let mut query = Query::new(
            FindSpec::Scalar(FindElement::Aggregate {
                function,
                variable: "x".into(),
            }),
            vec![],
        );
        query.inputs = vec![InputSpec::Collection("x".into())];
        assert_eq!(
            QueryTemplate::native(query, vec![0], vec![])
                .unwrap_err()
                .code,
            "program/nonportable-query"
        );
    }
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable("x".into())),
        vec![Clause::Function {
            function: Function::Extension("local-closure".into()),
            source: "$".into(),
            args: vec![],
            binding: Binding::Scalar("x".into()),
        }],
    );
    assert_eq!(
        QueryTemplate::native(query, vec![], vec![])
            .unwrap_err()
            .code,
        "program/nonportable-query"
    );
    let mut attribute = PullAttribute::forward(AttributeName::Id(SCORE));
    attribute.transform = Some(PullTransform::new("closure", |value| Ok(value.clone())));
    let mut query = Query::new(
        FindSpec::Scalar(FindElement::Pull {
            source: "$".into(),
            variable: "e".into(),
            pattern: Box::new(PullPattern {
                wildcard: false,
                attributes: vec![attribute],
            }),
        }),
        vec![],
    );
    query.inputs = vec![InputSpec::Scalar("e".into())];
    assert_eq!(
        QueryTemplate::native(query, vec![0], vec![])
            .unwrap_err()
            .code,
        "program/nonportable-query"
    );
}

#[test]
fn native_structured_inputs_functions_and_pull_feed_ordinary_vm_instructions() {
    let db = Database::new(schema()).unwrap();
    for (spec, argument) in [
        (
            InputSpec::Tuple(vec![Some("x".into()), None]),
            RuntimeValue::Vector(vec![
                RuntimeValue::Scalar(Value::Long(7)),
                RuntimeValue::Null,
            ]),
        ),
        (
            InputSpec::Collection("x".into()),
            RuntimeValue::Vector(vec![RuntimeValue::Scalar(Value::Long(7))]),
        ),
        (
            InputSpec::Relation(vec![Some("x".into())]),
            RuntimeValue::Vector(vec![RuntimeValue::Vector(vec![RuntimeValue::Scalar(
                Value::Long(7),
            )])]),
        ),
    ] {
        // QueryInput is a stored-scalar API, so an ignored slot still requires
        // a scalar value; nil is a query literal rather than a stored Value.
        let argument = match argument {
            RuntimeValue::Vector(mut values) if matches!(spec, InputSpec::Tuple(_)) => {
                values[1] = RuntimeValue::Scalar(Value::Long(0));
                RuntimeValue::Vector(values)
            }
            value => value,
        };
        let mut query = Query::new(
            FindSpec::Scalar(FindElement::Variable("sum".into())),
            vec![Clause::Function {
                function: Function::Add,
                source: "$".into(),
                args: vec![var("x"), Term::Constant(Value::Long(2))],
                binding: Binding::Scalar("sum".into()),
            }],
        );
        query.inputs = vec![spec];
        let program = wrapped(query, 1);
        assert_eq!(
            decode_program(&encode_program(&program).unwrap()).unwrap(),
            program
        );
        assert!(
            matches!(ProgramRuntime.execute_runtime(&program, &db, &[argument], ProgramControl::default()).unwrap(),
            ProgramOutput::Query(rows) if rows == vec![vec![Value::Long(9)]])
        );
    }
    let seeded = db.with(&seed(), 1_000).unwrap();
    let mut query = Query::new(
        FindSpec::Scalar(FindElement::Pull {
            source: "$".into(),
            variable: "entity".into(),
            pattern: Box::new(PullPattern {
                wildcard: false,
                attributes: vec![PullAttribute::forward(AttributeName::Id(SCORE))],
            }),
        }),
        vec![],
    );
    query.inputs = vec![InputSpec::Scalar("entity".into())];
    let mut program = wrapped(query, 1);
    program.instructions.insert(1, Instruction::Length);
    assert_eq!(
        rows(
            &program,
            &seeded.db_after.database_value(),
            &[Value::Ref(seeded.tempids["leaf"])]
        ),
        vec![vec![Value::Long(1)]]
    );
}

#[test]
fn native_codec_rejects_future_versions_and_checks_bounded_mutated_artifacts() {
    let bytes = encode_program(&selector(ProgramKind::Query, CHOSEN)).unwrap();
    let mut future = bytes.clone();
    future[16..18].copy_from_slice(&99u16.to_be_bytes());
    let checksum_at = future.len() - 32;
    let checksum = sha256(&future[..checksum_at]);
    future[checksum_at..].copy_from_slice(&checksum);
    assert_eq!(
        decode_program(&future).unwrap_err().code,
        "encoding/unsupported-program-abi"
    );
    for at in 24..checksum_at {
        let mut changed = bytes.clone();
        changed[at] ^= 0xff;
        let checksum = sha256(&changed[..checksum_at]);
        changed[checksum_at..].copy_from_slice(&checksum);
        if let Ok(program) = decode_program(&changed) {
            assert_eq!(
                encode_program(&program).unwrap(),
                changed,
                "accepted artifact must remain canonical at byte {at}"
            );
        }
    }
    let mut query = selector_template().native_query().clone();
    for _ in 0..40 {
        query = Query::new(
            FindSpec::Scalar(FindElement::Variable("x".into())),
            vec![Clause::Function {
                function: Function::Query(Box::new(query)),
                source: "$".into(),
                args: vec![],
                binding: Binding::Scalar("x".into()),
            }],
        );
    }
    assert_eq!(
        QueryTemplate::native(query, vec![], vec![])
            .unwrap_err()
            .code,
        "program/native-query-shape-limit"
    );
}

fn call(args: &[Value]) -> ProgramCall {
    ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(Keyword::new("graph", "select"))),
        arguments: args.iter().cloned().map(RuntimeValue::Scalar).collect(),
    }
}

fn log_program(start: u64, end: u64, leaf: u64) -> Program {
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("value".into())]),
        vec![
            Clause::Function {
                function: Function::TxIds,
                source: "$log".into(),
                args: vec![
                    Term::Constant(Value::Long(start as i64)),
                    Term::Constant(Value::Long(end as i64)),
                ],
                binding: Binding::Collection("tx".into()),
            },
            Clause::Function {
                function: Function::TxData,
                source: "$log".into(),
                args: vec![var("tx")],
                binding: Binding::Relation(vec![
                    Some("entity".into()),
                    Some("attribute".into()),
                    Some("value".into()),
                    None,
                    Some("added".into()),
                ]),
            },
            Clause::Predicate {
                predicate: Predicate::Eq,
                source: "$log".into(),
                args: vec![var("entity"), Term::Constant(Value::Ref(leaf))],
            },
            Clause::Predicate {
                predicate: Predicate::Eq,
                source: "$log".into(),
                args: vec![var("attribute"), attr(SCORE)],
            },
            Clause::Predicate {
                predicate: Predicate::Eq,
                source: "$log".into(),
                args: vec![var("added"), Term::Constant(Value::Bool(false))],
            },
        ],
    );
    Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::Query(
                QueryTemplate::native(query, vec![], vec![QueryTemplateSource::log("$log")])
                    .unwrap(),
            ),
            Instruction::ForEach {
                body: vec![Instruction::Unpack(1), Instruction::EmitRow(1)],
            },
            Instruction::Return,
        ],
    }
}

#[test]
fn persisted_native_query_speculates_commits_rebinds_retries_and_recovers() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "native_program_queries");
    let url = &fixture.connection;
    common::install(url).unwrap();
    let mut store = common::TestStore::connect(url).unwrap();
    let created = store.create_database("native-queries", schema()).unwrap();
    let v1 = store
        .deploy_program_blob(&selector(ProgramKind::Transaction, CHOSEN))
        .unwrap();
    let v2 = store
        .deploy_program_blob(&selector(ProgramKind::Transaction, OTHER_CHOSEN))
        .unwrap();
    let writer = common::start_service(url, "native-queries");
    let mut initial = seed();
    initial.extend([
        TxOp::Add {
            entity: EntityRef::Temp("selector".into()),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("graph", "select")).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("selector".into()),
            attribute: DB_FN as u32,
            value: Value::Function(v1).into(),
        },
    ]);
    let installed = common::transact(&writer, "install", created.basis_t(), &initial, 1_000);
    let changed = common::transact(
        &writer,
        "change",
        installed.basis_t,
        &changes(&installed.tempids),
        2_000,
    );
    let captured = changed.db_after.clone();
    let args = arguments(installed.tempids["root"], installed.basis_t);
    let leaf = installed.tempids["leaf"];
    let preview = captured
        .with_forms(&[TxForm::ProgramCall(call(&args))], 3_000)
        .unwrap();
    assert_eq!(
        preview.db_after.values(leaf, CHOSEN).unwrap(),
        vec![Value::Long(30)]
    );
    assert!(captured.values(leaf, CHOSEN).unwrap().is_empty());
    assert_eq!(
        store.recover("native-queries").unwrap().basis_t(),
        changed.basis_t
    );
    // A log source is the actual captured commit log, not reconstructed from
    // database history. Explicit ranges are independent of database as-of.
    let log_query = log_program(changed.basis_t, changed.basis_t + 1, leaf);
    assert_eq!(
        decode_program(&encode_program(&log_query).unwrap()).unwrap(),
        log_query
    );
    assert_eq!(
        rows(&log_query, &captured, &[]),
        vec![vec![Value::Long(30)]]
    );
    assert_eq!(
        rows(&log_query, &captured.clone().as_of(installed.basis_t), &[]),
        vec![vec![Value::Long(30)]]
    );
    assert_eq!(
        ProgramRuntime
            .execute_query(
                &log_query,
                &preview.db_after,
                &[],
                ProgramControl::default()
            )
            .unwrap_err()
            .category,
        ErrorCategory::Unsupported,
        "speculative data cannot masquerade as committed log transactions"
    );
    let request = || {
        TransactionRequest::new("select", vec![])
            .calling(call(&args))
            .comparing_basis(changed.basis_t)
            .with_tx_instant(3_000)
    };
    let committed = writer
        .client()
        .transact(request(), Duration::from_secs(10))
        .unwrap();
    common::assert_same_information(&preview.db_after, &committed.db_after);
    let rebound = common::transact(
        &writer,
        "rebind",
        committed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(installed.tempids["selector"]),
            attribute: DB_FN as u32,
            value: Value::Function(v2).into(),
        }],
        4_000,
    );
    let old_preview = captured
        .with_forms(&[TxForm::ProgramCall(call(&args))], 5_000)
        .unwrap();
    assert!(
        old_preview
            .db_after
            .values(leaf, OTHER_CHOSEN)
            .unwrap()
            .is_empty()
    );
    let new_preview = rebound
        .db_after
        .with_forms(&[TxForm::ProgramCall(call(&args))], 5_000)
        .unwrap();
    assert_eq!(
        new_preview.db_after.values(leaf, OTHER_CHOSEN).unwrap(),
        vec![Value::Long(30)]
    );
    let replay = writer
        .client()
        .transact(request(), Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    assert!(
        replay
            .db_after
            .values(leaf, OTHER_CHOSEN)
            .unwrap()
            .is_empty()
    );
    writer.shutdown();
    let restarted = common::start_service(url, "native-queries");
    let replay = restarted
        .client()
        .transact(request(), Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    let after_restart = restarted
        .client()
        .transact(
            TransactionRequest::new("new-version", vec![])
                .calling(call(&args))
                .with_tx_instant(5_000),
            Duration::from_secs(10),
        )
        .unwrap();
    common::assert_same_information(&new_preview.db_after, &after_restart.db_after);
    let historical = store
        .recover_basis("native-queries", changed.basis_t)
        .unwrap();
    let retained = store.resolve_program(v1).unwrap();
    assert_eq!(program_hash(&retained).unwrap(), v1);
    let ProgramOutput::Transaction(forms) = historical
        .invoke(
            Keyword::new("graph", "select"),
            &args
                .iter()
                .cloned()
                .map(RuntimeValue::Scalar)
                .collect::<Vec<_>>(),
            atomic_core::InvokeControl::default(),
        )
        .unwrap()
    else {
        panic!("retained version changed kind")
    };
    assert!(
        matches!(&forms[..], [TxForm::Op(TxOp::Add { entity: EntityRef::Id(id), attribute: CHOSEN,
        value: TxValue::Scalar(Value::Long(30)) })] if *id == leaf)
    );
    assert_eq!(
        rows(&selector(ProgramKind::Query, CHOSEN), &captured, &args),
        vec![vec![Value::Ref(leaf), Value::Long(30)]]
    );
    restarted.shutdown();
    eprintln!(
        "native durable query: five capabilities; speculation/commit; retained version; replay before/after restart; recovered bytes verified"
    );
}
