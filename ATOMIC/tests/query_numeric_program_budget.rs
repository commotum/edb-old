//! A persisted extension's native query must inherit the caller's numeric cap,
//! including through nested query ASTs and repeated interpreter host calls.
use atomic_core::*;
use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use std::mem::discriminant;

fn arithmetic_query(function: Function) -> Query {
    let mut query = Query::new(
        FindSpec::Scalar(FindElement::Variable("result".into())),
        vec![Clause::Function {
            function,
            source: "$".into(),
            args: vec![Term::var("left"), Term::var("right")],
            binding: Binding::Scalar("result".into()),
        }],
    );
    query.inputs = vec![
        InputSpec::Scalar("left".into()),
        InputSpec::Scalar("right".into()),
    ];
    query
}

fn nested_query(query: Query) -> Query {
    arithmetic_query(Function::Query(Box::new(query)))
}

fn program(query: Query, host_calls: usize) -> Program {
    program_queries(&vec![query; host_calls])
}

fn program_queries(queries: &[Query]) -> Program {
    let mut instructions = Vec::new();
    for (call, query) in queries.iter().enumerate() {
        instructions.push(Instruction::Query(
            QueryTemplate::native(
                query.clone(),
                vec![0, 1],
                vec![QueryTemplateSource::current("$")],
            )
            .unwrap(),
        ));
        instructions.push(if call + 1 == queries.len() {
            Instruction::EmitRow(1)
        } else {
            Instruction::Pop
        });
    }
    instructions.push(Instruction::Return);
    Program {
        kind: ProgramKind::Query,
        arity: 2,
        instructions,
    }
}

fn register(program: Program) -> QueryExtensions {
    // Exercise the actual persisted-program representation, with no live PG
    // fixture or trusted local callback substituted for the interpreter.
    let encoded = encode_program(&program).unwrap();
    let restored = decode_program(&encoded).unwrap();
    assert_eq!(encode_program(&restored).unwrap(), encoded);
    let mut extensions = QueryExtensions::new();
    extensions
        .register_program("numeric", program_hash(&restored).unwrap(), restored)
        .unwrap();
    extensions
}

fn sources() -> [QueryDataSource; 1] {
    [QueryDataSource::database(
        "$",
        Database::new(Schema::new()).unwrap().database_value(),
    )]
}

fn inputs(left: &Value, right: &Value) -> [QueryInput; 2] {
    [
        QueryInput::Scalar(left.clone()),
        QueryInput::Scalar(right.clone()),
    ]
}

fn assert_result(outcome: &QueryOutcome, expected: &Value) {
    let QueryResult::Scalar(Some(QueryValue::Scalar(actual))) = &outcome.result else {
        panic!("expected one numeric result, got {:?}", outcome.result)
    };
    assert_eq!(discriminant(actual), discriminant(expected));
    assert_eq!(actual, expected);
}

fn assert_capacity(error: SemanticError) {
    assert_eq!(error.category, ErrorCategory::Busy, "{error:?}");
    assert_eq!(error.code, "query/numeric-capacity");
}

fn cases() -> Vec<(Function, Value, Value, Value)> {
    vec![
        (
            Function::Add,
            Value::BigDec(BigDecimal::new(1.into(), 1_000)),
            Value::Long(1),
            Value::BigDec(BigDecimal::new(BigInt::from(10u8).pow(1_000) + 1, 1_000)),
        ),
        (
            Function::Multiply,
            Value::BigInt(BigInt::from(1u8) << 2_048usize),
            Value::BigInt(BigInt::from(1u8) << 2_048usize),
            Value::BigInt(BigInt::from(1u8) << 4_096usize),
        ),
    ]
}

#[test]
fn persisted_query_program_preserves_numeric_cap_and_reuses_after_failure() {
    let sources = sources();
    let prepared =
        PreparedQuery::new(&arithmetic_query(Function::Extension("numeric".into()))).unwrap();
    let small = QueryControl {
        max_numeric_bytes: 1_024,
        ..Default::default()
    };
    let large = QueryControl {
        max_numeric_bytes: 64 * 1_024,
        ..Default::default()
    };
    for (function, left, right, expected) in cases() {
        for nesting in 0..=2 {
            let mut query = arithmetic_query(function.clone());
            for _ in 0..nesting {
                query = nested_query(query);
            }
            let extensions = register(program(query, 1));
            let inputs = inputs(&left, &right);
            // Repeat the failure/success sequence on the same prepared query:
            // neither a stale result nor a previous larger budget may survive.
            for _ in 0..2 {
                assert_capacity(
                    prepared
                        .execute_with_extensions(&sources, &inputs, &small, &extensions)
                        .unwrap_err(),
                );
                let outcome = prepared
                    .execute_with_extensions(&sources, &inputs, &large, &extensions)
                    .unwrap();
                assert_result(&outcome, &expected);
            }
        }
    }
}

#[test]
fn ordinary_nested_queries_observe_the_same_numeric_cap() {
    let sources = sources();
    for (function, left, right, expected) in cases() {
        let query = nested_query(nested_query(arithmetic_query(function)));
        let prepared = PreparedQuery::new(&query).unwrap();
        let inputs = inputs(&left, &right);
        assert_capacity(
            prepared
                .execute(
                    &sources,
                    &inputs,
                    &QueryControl {
                        max_numeric_bytes: 1_024,
                        ..Default::default()
                    },
                )
                .unwrap_err(),
        );
        assert_result(
            &prepared
                .execute(
                    &sources,
                    &inputs,
                    &QueryControl {
                        max_numeric_bytes: 64 * 1_024,
                        ..Default::default()
                    },
                )
                .unwrap(),
            &expected,
        );
    }
}

#[test]
fn sibling_program_host_queries_keep_the_cap_and_share_remaining_work() {
    let sources = sources();
    let query = arithmetic_query(Function::Multiply);
    let single = register(program(query.clone(), 1));
    let siblings = register(program(query, 2));
    let prepared =
        PreparedQuery::new(&arithmetic_query(Function::Extension("numeric".into()))).unwrap();
    let large = Value::BigInt(BigInt::from(1u8) << 8_192usize);
    let inputs = inputs(&large, &large);
    let generous = QueryControl {
        max_numeric_bytes: 128 * 1_024,
        ..Default::default()
    };
    let once = prepared
        .execute_with_extensions(&sources, &inputs, &generous, &single)
        .unwrap();
    let twice = prepared
        .execute_with_extensions(&sources, &inputs, &generous, &siblings)
        .unwrap();
    assert_eq!(once.result, twice.result);
    assert!(twice.stats.work > once.stats.work);
    let shared = QueryControl {
        max_work: once.stats.work as usize,
        ..generous.clone()
    };
    assert_result(
        &prepared
            .execute_with_extensions(&sources, &inputs, &shared, &single)
            .unwrap(),
        &Value::BigInt(BigInt::from(1u8) << 16_384usize),
    );
    let error = prepared
        .execute_with_extensions(&sources, &inputs, &shared, &siblings)
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Busy, "{error:?}");
    assert_eq!(error.code, "query/work-limit");
    assert_capacity(
        prepared
            .execute_with_extensions(
                &sources,
                &inputs,
                &QueryControl {
                    max_numeric_bytes: 1_024,
                    ..generous.clone()
                },
                &siblings,
            )
            .unwrap_err(),
    );
    assert_eq!(
        prepared
            .execute_with_extensions(&sources, &inputs, &generous, &siblings)
            .unwrap()
            .result,
        twice.result
    );

    // The first host query succeeds under the small cap. Only the second one
    // expands a coefficient, so resetting admission after the first call would
    // turn the required numeric-capacity failure into a successful result.
    let mut cheap = arithmetic_query(Function::Add);
    let Clause::Function { args, .. } = &mut cheap.clauses[0] else {
        unreachable!()
    };
    *args = vec![
        Term::Constant(Value::Long(1)),
        Term::Constant(Value::Long(1)),
    ];
    let cheap_only = register(program(cheap.clone(), 1));
    let cheap_then_large = register(program_queries(&[
        cheap,
        arithmetic_query(Function::Multiply),
    ]));
    let small = QueryControl {
        max_numeric_bytes: 1_024,
        ..generous.clone()
    };
    assert_result(
        &prepared
            .execute_with_extensions(&sources, &inputs, &small, &cheap_only)
            .unwrap(),
        &Value::Long(2),
    );
    assert_capacity(
        prepared
            .execute_with_extensions(&sources, &inputs, &small, &cheap_then_large)
            .unwrap_err(),
    );
    assert_eq!(
        prepared
            .execute_with_extensions(&sources, &inputs, &generous, &cheap_then_large)
            .unwrap()
            .result,
        twice.result
    );
}
