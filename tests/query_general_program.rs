use atomic_core::*;
use std::sync::atomic::AtomicBool;
mod common;

fn database() -> DatabaseValue {
    Database::new(Schema::new()).unwrap().database_value()
}

fn scalar(value: i64) -> QueryValue {
    QueryValue::Scalar(Value::Long(value))
}

fn mixed() -> QueryValue {
    QueryValue::Map(vec![
        (
            QueryValue::Nil,
            QueryValue::Set(vec![scalar(3), QueryValue::Char('λ')]),
        ),
        (
            QueryValue::Tuple(vec![scalar(1), QueryValue::Nil]),
            QueryValue::Tagged(
                Symbol::new("app", "record"),
                Box::new(QueryValue::Collection(vec![
                    QueryValue::Char('界'),
                    scalar(7),
                ])),
            ),
        ),
    ])
}

fn projection(
    query: Query,
    arguments: Vec<u8>,
    sources: Vec<QueryTemplateSource>,
    arity: u8,
    width: u8,
) -> Program {
    Program {
        kind: ProgramKind::Query,
        arity,
        instructions: vec![
            Instruction::Query(QueryTemplate::native(query, arguments, sources).unwrap()),
            Instruction::ForEach {
                body: vec![Instruction::Unpack(width), Instruction::EmitRow(width)],
            },
            Instruction::Return,
        ],
    }
}

fn ground(value: Term) -> Query {
    Query::new(
        FindSpec::Relation(vec![FindElement::Variable("out".into())]),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![value],
            binding: Binding::Scalar("out".into()),
        }],
    )
}

fn output_rows(output: ProgramOutput) -> Vec<Vec<QueryValue>> {
    match output {
        ProgramOutput::GeneralQuery(rows) => rows,
        ProgramOutput::Query(rows) => rows
            .into_iter()
            .map(|row| row.into_iter().map(QueryValue::Scalar).collect())
            .collect(),
        other => panic!("not query output: {other:?}"),
    }
}

fn resign(bytes: &mut [u8]) {
    let checksum_at = bytes.len() - 32;
    let checksum = sha256(&bytes[..checksum_at]);
    bytes[checksum_at..].copy_from_slice(&checksum);
}

#[test]
fn new_literals_select_only_new_template_and_abi_preserving_exact_general_output() {
    let value = mixed();
    let program = projection(
        ground(Term::QueryConstant(value.clone())),
        vec![],
        vec![],
        0,
        1,
    );
    let bytes = encode_program(&program).unwrap();
    assert_eq!(&bytes[16..18], &10u16.to_be_bytes());
    assert_eq!(&bytes[25..27], &3u16.to_be_bytes());
    let decoded = decode_program(&bytes).unwrap();
    assert_eq!(encode_program(&decoded).unwrap(), bytes);
    let result = ProgramRuntime
        .execute_query_general(&decoded, &database(), &[], ProgramControl::default())
        .unwrap();
    let encoded_output = encode_program_output(&result).unwrap();
    assert_eq!(encoded_output[16], 4);
    assert_eq!(output_rows(result), vec![vec![value]]);

    let old = projection(ground(Term::Constant(Value::Long(7))), vec![], vec![], 0, 1);
    let old_bytes = encode_program(&old).unwrap();
    assert_eq!(&old_bytes[16..18], &7u16.to_be_bytes());
    assert_eq!(&old_bytes[25..27], &2u16.to_be_bytes());
    assert_eq!(
        encode_program(&decode_program(&old_bytes).unwrap()).unwrap(),
        old_bytes
    );
    assert!(
        matches!(ProgramRuntime.execute_query(&old, &database(), &[], ProgramControl::default()).unwrap(), ProgramOutput::Query(rows) if rows == vec![vec![Value::Long(7)]])
    );
}

#[test]
fn new_set_encoding_is_unordered_and_chooses_stable_numeric_duplicate_representations() {
    let values = vec![
        scalar(2),
        QueryValue::Scalar(Value::Ref(1)),
        scalar(1),
        scalar(2),
    ];
    let mut reversed = values.clone();
    reversed.reverse();
    let left = projection(
        ground(Term::QueryConstant(QueryValue::Set(values))),
        vec![],
        vec![],
        0,
        1,
    );
    let right = projection(
        ground(Term::QueryConstant(QueryValue::Set(reversed))),
        vec![],
        vec![],
        0,
        1,
    );
    let bytes = encode_program(&left).unwrap();
    assert_eq!(bytes, encode_program(&right).unwrap());
    assert_eq!(
        encode_program(&decode_program(&bytes).unwrap()).unwrap(),
        bytes
    );
}

#[test]
fn persisted_wide_named_relation_retains_nil_arbitrary_keys_sets_and_tags() {
    let mut terms = (0..8)
        .map(|column| Term::Variable(Variable::new(format!("c{column}")).unwrap()))
        .collect::<Vec<_>>();
    terms[1] = Term::QueryConstant(mixed());
    terms[7] = Term::var("c0");
    let query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable("c0".into()),
            FindElement::Variable("c6".into()),
        ]),
        vec![Clause::RelationPattern(Box::new(RelationPattern {
            source: "$rows".into(),
            terms,
        }))],
    );
    let program = projection(
        query,
        vec![],
        vec![QueryTemplateSource::relation("$rows", 0)],
        1,
        2,
    );
    let decoded = decode_program(&encode_program(&program).unwrap()).unwrap();
    let mut row = vec![
        scalar(11),
        mixed(),
        QueryValue::Nil,
        scalar(4),
        scalar(5),
        scalar(6),
        mixed(),
        scalar(11),
    ];
    let accepted = row.clone();
    row[7] = scalar(12);
    let rows = QueryValue::Collection(vec![QueryValue::Tuple(row), QueryValue::Tuple(accepted)]);
    let result = ProgramRuntime
        .execute_query_general(&decoded, &database(), &[rows], ProgramControl::default())
        .unwrap();
    assert_eq!(output_rows(result), vec![vec![scalar(11), mixed()]]);
}

#[test]
fn existing_native_input_template_accepts_general_values_without_changing_its_bytes() {
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("x".into())]),
        vec![],
    );
    query.inputs = vec![InputSpec::Scalar("x".into())];
    let program = projection(query.clone(), vec![0], vec![], 1, 1);
    let before = encode_program(&program).unwrap();
    assert_eq!(&before[16..18], &7u16.to_be_bytes());
    for (value, expected) in [
        (QueryValue::Nil, QueryValue::Nil),
        (mixed(), mixed()),
        (
            QueryValue::Collection(vec![scalar(1), scalar(2)]),
            QueryValue::Collection(vec![scalar(1), scalar(2)]),
        ),
        // Legal native tuples retain the existing query result normalization.
        // Query-only collections/maps/sets/tags above remain lossless carriers.
        (
            QueryValue::Tuple(vec![scalar(1)]),
            QueryValue::Scalar(Value::Tuple(vec![Some(Value::Long(1))])),
        ),
    ] {
        let direct = QueryEngine::execute_sources(
            &query,
            &[],
            &[QueryInput::General(value.clone())],
            &QueryControl::default(),
        )
        .unwrap();
        assert_eq!(
            direct.result,
            QueryResult::Relation(vec![vec![expected.clone()]])
        );
        let result = ProgramRuntime
            .execute_query_general(
                &program,
                &database(),
                std::slice::from_ref(&value),
                ProgramControl::default(),
            )
            .unwrap();
        assert_eq!(output_rows(result), vec![vec![expected]]);
    }
    assert_eq!(encode_program(&program).unwrap(), before);
}

#[test]
fn new_templates_reject_downgrades_invalid_characters_and_unbounded_shapes() {
    let program = projection(
        ground(Term::QueryConstant(QueryValue::Char('\u{10ffff}'))),
        vec![],
        vec![],
        0,
        1,
    );
    let bytes = encode_program(&program).unwrap();
    for (offset, version) in [(16, 7u16), (25, 2u16)] {
        let mut changed = bytes.clone();
        changed[offset..offset + 2].copy_from_slice(&version.to_be_bytes());
        resign(&mut changed);
        assert!(decode_program(&changed).is_err());
    }
    let mut changed = bytes;
    let at = changed
        .windows(5)
        .position(|bytes| bytes == [6, 0, 0x10, 0xff, 0xff])
        .unwrap();
    changed[at + 1..at + 5].copy_from_slice(&0xd800u32.to_be_bytes());
    resign(&mut changed);
    assert_eq!(
        decode_program(&changed).unwrap_err().code,
        "encoding/query-character"
    );
    let mut deep = QueryValue::Nil;
    for _ in 0..64 {
        deep = QueryValue::Tuple(vec![deep]);
    }
    assert!(QueryTemplate::native(ground(Term::QueryConstant(deep)), vec![], vec![]).is_err());
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("x".into())]),
        vec![Clause::RelationPattern(Box::new(RelationPattern {
            source: "$rows".into(),
            terms: vec![Term::var("x"); 4097],
        }))],
    );
    assert!(
        QueryTemplate::native(
            query,
            vec![],
            vec![QueryTemplateSource::relation("$rows", 0)]
        )
        .is_err()
    );
}

#[test]
fn general_runtime_admission_cancellation_and_transaction_boundary_are_explicit() {
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("x".into())]),
        vec![],
    );
    query.inputs = vec![InputSpec::Scalar("x".into())];
    let program = projection(query, vec![0], vec![], 1, 1);
    let error = ProgramRuntime
        .execute_query_general(
            &program,
            &database(),
            &[mixed()],
            ProgramControl {
                max_value_bytes: 64,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/value-byte-limit");
    let canceled = AtomicBool::new(true);
    assert_eq!(
        ProgramRuntime
            .execute_query_general(
                &program,
                &database(),
                &[mixed()],
                ProgramControl {
                    cancelled: Some(&canceled),
                    ..ProgramControl::default()
                }
            )
            .unwrap_err()
            .code,
        "program/cancelled"
    );
    assert_eq!(
        output_rows(
            ProgramRuntime
                .execute_query_general(&program, &database(), &[mixed()], ProgramControl::default())
                .unwrap()
        ),
        vec![vec![mixed()]]
    );
    let tx = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::EmitCall {
                function: CallableRef::ExactHash([7; 32]),
                argument_count: 1,
            },
            Instruction::Return,
        ],
    };
    let output = ProgramRuntime
        .execute_runtime(
            &tx,
            &Database::new(Schema::new()).unwrap(),
            &[RuntimeValue::Query(mixed())],
            ProgramControl::default(),
        )
        .unwrap();
    assert_eq!(
        encode_program_output(&output).unwrap_err().code,
        "program/query-only-value"
    );
}

fn joined_program() -> Program {
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("data".into())]),
        vec![
            Clause::RelationPattern(Box::new(RelationPattern {
                source: "$rows".into(),
                terms: vec![
                    Term::var("entity"),
                    Term::var("data"),
                    Term::Blank,
                    Term::Blank,
                    Term::Blank,
                    Term::Blank,
                    Term::var("score"),
                    Term::QueryConstant(QueryValue::Char('λ')),
                ],
            })),
            Clause::Pattern(Box::new(DataPattern::new(
                Term::var("entity"),
                Term::Constant(Value::Ref(1000)),
                Term::var("score"),
            ))),
        ],
    );
    projection(
        query,
        vec![],
        vec![
            QueryTemplateSource::current("$"),
            QueryTemplateSource::relation("$rows", 0),
        ],
        1,
        1,
    )
}

#[test]
fn general_vm_operations_and_aggregate_input_retention_are_bounded() {
    let length = Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::Length,
            Instruction::EmitRow(1),
            Instruction::Return,
        ],
    };
    let set = QueryValue::Set(vec![
        scalar(1),
        QueryValue::Scalar(Value::Ref(1)),
        scalar(2),
    ]);
    assert_eq!(
        output_rows(
            ProgramRuntime
                .execute_query_general(&length, &database(), &[set], ProgramControl::default())
                .unwrap()
        ),
        vec![vec![scalar(2)]]
    );
    for instruction in [Instruction::Get, Instruction::ContainsKey] {
        let program = Program {
            kind: ProgramKind::Query,
            arity: 2,
            instructions: vec![
                Instruction::PushArgument(0),
                Instruction::PushArgument(1),
                instruction.clone(),
                Instruction::EmitRow(1),
                Instruction::Return,
            ],
        };
        let output = ProgramRuntime
            .execute_query_general(
                &program,
                &database(),
                &[mixed(), QueryValue::Nil],
                ProgramControl::default(),
            )
            .unwrap();
        let expected = if matches!(instruction, Instruction::Get) {
            QueryValue::Set(vec![scalar(3), QueryValue::Char('λ')])
        } else {
            QueryValue::Scalar(Value::Bool(true))
        };
        assert_eq!(output_rows(output), vec![vec![expected]]);
    }
    let unused = Program {
        kind: ProgramKind::Query,
        arity: 2,
        instructions: vec![
            Instruction::PushConstant(Value::Long(0)),
            Instruction::EmitRow(1),
            Instruction::Return,
        ],
    };
    let argument = QueryValue::Scalar(Value::String("x".repeat(1024)));
    let error = ProgramRuntime
        .execute_query_general(
            &unused,
            &database(),
            &[argument.clone(), argument],
            ProgramControl {
                max_value_bytes: 1800,
                ..ProgramControl::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/value-byte-limit");
}

#[test]
fn postgres_general_program_persists_reopens_invokes_and_keeps_speculation_inert() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP actual PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let total = std::time::Instant::now();
    let fixture = common::PostgresFixture::new(&url, "general_program");
    common::install(&fixture.connection).unwrap();
    let mut store = common::TestStore::connect(&fixture.connection).unwrap();
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let created = store.create_database("general-program", schema).unwrap();
    let program = joined_program();
    let expected_bytes = encode_program(&program).unwrap();
    let hash = store.deploy_program_blob(&program).unwrap();
    assert_eq!(hash, sha256(&expected_bytes));
    let writer = common::start_service(&fixture.connection, "general-program");
    let installed = common::transact(
        &writer,
        "install",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: 1000,
                value: Value::Long(7).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("query".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("app", "wide-query")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("query".into()),
                attribute: DB_FN as u32,
                value: Value::Function(hash).into(),
            },
        ],
        1000,
    );
    let entity = installed.tempids["item"];
    let rows = QueryValue::Collection(vec![QueryValue::Tuple(vec![
        QueryValue::Scalar(Value::Ref(entity)),
        mixed(),
        QueryValue::Nil,
        scalar(3),
        scalar(4),
        scalar(5),
        scalar(7),
        QueryValue::Char('λ'),
    ])]);
    let arguments = [RuntimeValue::Query(rows)];
    let peer = Connection::connect(&fixture.connection, "general-program", 8).unwrap();
    let current = peer.db();
    let reference = current.snapshot_reference().unwrap();
    let operation = std::time::Instant::now();
    assert_eq!(
        output_rows(
            current
                .invoke(
                    Keyword::new("app", "wide-query"),
                    &arguments,
                    Default::default()
                )
                .unwrap()
        ),
        vec![vec![mixed()]]
    );
    let initial_invoke = operation.elapsed();
    let preview = current
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: 1000,
                value: Value::Long(8).into(),
            }],
            2000,
        )
        .unwrap();
    assert!(
        output_rows(
            preview
                .db_after
                .invoke(
                    Keyword::new("app", "wide-query"),
                    &arguments,
                    Default::default()
                )
                .unwrap()
        )
        .is_empty()
    );
    assert_eq!(
        output_rows(
            current
                .invoke(
                    Keyword::new("app", "wide-query"),
                    &arguments,
                    Default::default()
                )
                .unwrap()
        ),
        vec![vec![mixed()]]
    );
    let query_call = |arguments| {
        TxForm::ProgramCall(ProgramCall {
            function: CallableRef::Database(EntityRef::Ident(Keyword::new("app", "wide-query"))),
            arguments,
        })
    };
    assert_eq!(
        current
            .with_forms(&[query_call(vec![])], 2000)
            .unwrap_err()
            .code,
        "program/not-transaction-function"
    );
    assert_eq!(
        current
            .with_forms(&[query_call(arguments.to_vec())], 2000)
            .unwrap_err()
            .code,
        "program/query-only-value"
    );
    assert_eq!(
        store.recover("general-program").unwrap().basis_t(),
        installed.basis_t
    );
    writer.shutdown();
    drop(preview);
    drop(current);
    drop(peer);
    drop(installed);
    drop(store);
    let reopened = reference
        .open(
            &PostgresConnectionConfig::plaintext(&fixture.connection),
            8,
            1024 * 1024,
        )
        .unwrap();
    let mut reopened_store = common::TestStore::connect(&fixture.connection).unwrap();
    assert_eq!(
        encode_program(&reopened_store.resolve_program(hash).unwrap()).unwrap(),
        expected_bytes
    );
    let operation = std::time::Instant::now();
    assert_eq!(
        output_rows(
            reopened
                .invoke(
                    Keyword::new("app", "wide-query"),
                    &arguments,
                    Default::default()
                )
                .unwrap()
        ),
        vec![vec![mixed()]]
    );
    eprintln!(
        "general persisted ABI10: initial invoke/check/drop={initial_invoke:?}; reopened invoke/check/drop={:?}; full fixture/install/speculate/reopen={:?}",
        operation.elapsed(),
        total.elapsed()
    );
}
