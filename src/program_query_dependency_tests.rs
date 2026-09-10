use super::*;
use crate::{
    Binding, Clause, FindElement, FindSpec, Function, Instruction, Predicate, Program,
    PullAttribute, PullNested, PullPattern, Query, QueryTemplate, QueryValue, RelationPattern,
    Rule, Symbol, Term,
};

fn term(marker: u8) -> Term {
    Term::Constant(Value::Function([marker; 32]))
}
fn general(marker: u8) -> QueryValue {
    QueryValue::Scalar(Value::Function([marker; 32]))
}
fn ground(marker: u8) -> Clause {
    Clause::Function {
        function: Function::Ground,
        source: "$".into(),
        args: vec![term(marker)],
        binding: Binding::Scalar("x".into()),
    }
}
fn program(query: Query) -> Program {
    Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::Query(QueryTemplate::native(query, vec![], vec![]).unwrap()),
            Instruction::Return,
        ],
    }
}

#[test]
fn native_query_walker_visits_every_literal_position_and_nested_shape() {
    let mut nested = PullAttribute::forward(crate::AttributeName::Id(1001));
    nested.default = Some(QueryValue::Scalar(Value::Tuple(vec![
        Some(Value::Function([23; 32])),
        None,
    ])));
    nested.alias = Some(QueryValue::Collection(vec![general(24)]));
    let mut pull = PullAttribute::forward(crate::AttributeName::Id(1000));
    pull.alias = Some(general(22));
    pull.default = Some(QueryValue::Map(vec![(
        general(20),
        QueryValue::Tagged(Symbol::new("tag", "payload"), Box::new(general(21))),
    )]));
    pull.nested = Some(PullNested::Pattern(Box::new(PullPattern {
        wildcard: false,
        attributes: vec![nested],
    })));
    let mut pattern = crate::DataPattern::new(term(1), term(2), term(3));
    pattern.transaction = Some(term(4));
    pattern.added = Some(term(5));
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Pull {
            source: "$".into(),
            variable: "x".into(),
            pattern: Box::new(PullPattern {
                wildcard: false,
                attributes: vec![pull],
            }),
        }]),
        vec![
            Clause::Pattern(Box::new(pattern)),
            Clause::RelationPattern(Box::new(RelationPattern::new(vec![Term::QueryConstant(
                QueryValue::Tuple(vec![
                    general(6),
                    QueryValue::Tagged(
                        Symbol::new("tag", "data"),
                        Box::new(QueryValue::Map(vec![(
                            general(7),
                            QueryValue::Set(vec![general(8)]),
                        )])),
                    ),
                    QueryValue::Scalar(Value::Tuple(vec![Some(Value::Function([9; 32])), None])),
                ]),
            )]))),
            Clause::Predicate {
                predicate: Predicate::Eq,
                source: "$".into(),
                args: vec![
                    term(10),
                    Term::QueryConstant(QueryValue::Tagged(
                        Symbol::new("tag", "predicate"),
                        Box::new(general(11)),
                    )),
                ],
            },
            ground(12),
            Clause::Function {
                function: Function::Query(Box::new(Query::new(
                    FindSpec::Scalar(FindElement::Variable("x".into())),
                    vec![ground(13)],
                ))),
                source: "$".into(),
                args: vec![term(14)],
                binding: Binding::Scalar("x".into()),
            },
            Clause::Not {
                join: None,
                clauses: vec![ground(15)],
            },
            Clause::Or {
                join: None,
                branches: vec![vec![ground(16)], vec![ground(17)]],
            },
            Clause::Rule {
                source: "$".into(),
                name: "rule".into(),
                args: vec![term(18)],
            },
        ],
    );
    query.rules.push(Rule {
        name: "rule".into(),
        head: vec!["x".into()],
        required: BTreeSet::new(),
        clauses: vec![ground(19)],
    });
    // Direct traversal covers dormant but syntactically representable slots;
    // the smaller following test witnesses actual validated persisted blobs.
    let mut hashes = Vec::new();
    collect_native_query_dependencies(&query, &mut hashes);
    assert_eq!(
        hashes.into_iter().collect::<BTreeSet<_>>(),
        (1..=24).map(|marker| [marker; 32]).collect()
    );
}

#[test]
fn validated_abi7_and_abi10_query_closures_fail_closed_on_missing_nested_code() {
    let dependency = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![Instruction::Return],
    };
    let leaf_hash = crate::program_hash(&dependency).unwrap();
    let leaf = Arc::new(ValidatedProgram::from_canonical(dependency));
    for term in [
        Term::Constant(Value::Function(leaf_hash)),
        Term::QueryConstant(QueryValue::Tagged(
            Symbol::new("tag", "reference"),
            Box::new(QueryValue::Map(vec![(
                QueryValue::Nil,
                QueryValue::Set(vec![QueryValue::Scalar(Value::Function(leaf_hash))]),
            )])),
        )),
    ] {
        let query = Query::new(
            FindSpec::Scalar(FindElement::Variable("x".into())),
            vec![Clause::Function {
                function: Function::Ground,
                source: "$".into(),
                args: vec![term],
                binding: Binding::Scalar("x".into()),
            }],
        );
        let code = program(query);
        let bytes = crate::encode_program(&code).unwrap();
        assert!(matches!(
            u16::from_be_bytes(bytes[16..18].try_into().unwrap()),
            7 | 10
        ));
        let root_hash = crate::sha256(&bytes);
        let root = Arc::new(ValidatedProgram::from_canonical(
            crate::decode_program(&bytes).unwrap(),
        ));
        let error = resolve_program_closure(
            &mut |hash| {
                if hash == root_hash {
                    Ok(Arc::clone(&root))
                } else {
                    Err(SemanticError::new(
                        ErrorCategory::NotFound,
                        "test/missing-dependency",
                        "missing nested code",
                    ))
                }
            },
            BTreeSet::from([root_hash]),
        )
        .unwrap_err();
        assert_eq!(error.code, "test/missing-dependency");
        let mut reads = BTreeMap::<Digest, usize>::new();
        let closure = resolve_program_closure(
            &mut |hash| {
                *reads.entry(hash).or_default() += 1;
                if hash == root_hash {
                    Ok(Arc::clone(&root))
                } else {
                    assert_eq!(hash, leaf_hash);
                    Ok(Arc::clone(&leaf))
                }
            },
            BTreeSet::from([root_hash]),
        )
        .unwrap();
        assert_eq!(
            closure.keys().copied().collect::<BTreeSet<_>>(),
            BTreeSet::from([root_hash, leaf_hash])
        );
        assert!(reads.values().all(|count| *count == 1));
    }
}
