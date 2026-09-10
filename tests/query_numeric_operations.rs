//! Public query arithmetic contract: exact domains stay exact until an
//! explicitly approximate operation requests Double. :with retains input bags.
use atomic_core::*;
use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use std::mem::discriminant;
use std::sync::{Arc, atomic::AtomicBool};

fn decimal(text: &str) -> Value {
    Value::BigDec(text.parse().unwrap())
}

fn integer(value: impl Into<BigInt>) -> Value {
    Value::BigInt(value.into())
}

fn operation(function: Function) -> PreparedQuery {
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
    PreparedQuery::new(&query).unwrap()
}

fn scalar(outcome: QueryOutcome) -> Value {
    let QueryResult::Scalar(Some(QueryValue::Scalar(value))) = &outcome.result else {
        panic!("expected scalar value, got {:?}", outcome.result)
    };
    value.clone()
}

fn execute_operation(
    prepared: &PreparedQuery,
    left: Value,
    right: Value,
    control: &QueryControl,
) -> Result<QueryOutcome, SemanticError> {
    prepared.execute(
        &[],
        &[QueryInput::Scalar(left), QueryInput::Scalar(right)],
        control,
    )
}

fn assert_value(actual: Value, expected: Value) {
    // Value::eq alone deliberately equates Long, Ref, decimal, and float.
    assert_eq!(
        discriminant(&actual),
        discriminant(&expected),
        "wrong result domain: {actual:?}; expected {expected:?}"
    );
    assert_eq!(actual, expected);
}

fn assert_operation(function: Function, left: Value, right: Value, expected: Value) {
    let actual =
        execute_operation(&operation(function), left, right, &QueryControl::default()).unwrap();
    assert_value(scalar(actual), expected);
}

fn assert_arithmetic_error(function: Function, left: Value, right: Value) {
    let error =
        execute_operation(&operation(function), left, right, &QueryControl::default()).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Incorrect, "{error:?}");
    assert!(error.code.starts_with("query/"), "{error:?}");
}

#[test]
fn checked_longs_and_arbitrary_precision_integral_operations_keep_their_domains() {
    for (function, left, right, expected) in [
        (Function::Add, 7, 2, 9),
        (Function::Subtract, 7, 2, 5),
        (Function::Multiply, -7, 2, -14),
        (Function::Divide, 7, 2, 3),
        (Function::Divide, -7, 2, -3),
        (Function::Divide, 7, -2, -3),
    ] {
        assert_operation(
            function,
            Value::Long(left),
            Value::Long(right),
            Value::Long(expected),
        );
    }
    for (function, left, right) in [
        (Function::Add, i64::MAX, 1),
        (Function::Subtract, i64::MIN, 1),
        (Function::Multiply, i64::MAX, 2),
        (Function::Divide, i64::MIN, -1),
        (Function::Divide, 1, 0),
    ] {
        assert_arithmetic_error(function, Value::Long(left), Value::Long(right));
    }

    let huge = BigInt::from(1u8) << 200usize;
    assert_operation(
        Function::Add,
        integer(huge.clone()),
        Value::Long(7),
        integer(&huge + 7),
    );
    assert_operation(
        Function::Subtract,
        Value::Long(7),
        integer(huge.clone()),
        integer(BigInt::from(7) - &huge),
    );
    assert_operation(
        Function::Multiply,
        integer(huge.clone()),
        Value::Long(-3),
        integer(&huge * -3),
    );
    assert_operation(Function::Divide, integer(-7), integer(2), integer(-3));
    assert_operation(Function::Divide, Value::Long(7), integer(2), integer(3));
    assert_operation(
        Function::Add,
        Value::Long(i64::MAX),
        integer(1),
        integer(BigInt::from(i64::MAX) + 1),
    );
    assert_arithmetic_error(Function::Divide, integer(1), integer(0));
}

#[test]
fn decimals_promote_exactly_and_division_never_silently_rounds() {
    for (function, left, right, expected) in [
        (
            Function::Add,
            decimal("1.25"),
            decimal("2.75"),
            decimal("4"),
        ),
        (
            Function::Subtract,
            decimal("1.25"),
            Value::Long(2),
            decimal("-0.75"),
        ),
        (
            Function::Add,
            Value::Long(2),
            decimal("0.5"),
            decimal("2.5"),
        ),
        (
            Function::Multiply,
            decimal("1.20"),
            decimal("3.00"),
            decimal("3.6"),
        ),
        (
            Function::Divide,
            decimal("1.0"),
            Value::Long(8),
            decimal("0.125"),
        ),
        (Function::Divide, decimal("6"), integer(40), decimal("0.15")),
        (
            Function::Divide,
            integer(-3),
            decimal("8"),
            decimal("-0.375"),
        ),
    ] {
        assert_operation(function, left, right, expected);
    }
    let huge = BigInt::from(1u8) << 200usize;
    assert_operation(
        Function::Add,
        integer(huge.clone()),
        decimal("0.25"),
        Value::BigDec(BigDecimal::new(huge * 100 + 25, 2)),
    );
    assert_arithmetic_error(Function::Divide, decimal("1"), decimal("3"));
    assert_arithmetic_error(Function::Divide, Value::Long(1), decimal("0"));
    assert_arithmetic_error(Function::Divide, decimal("0"), decimal("0"));
}

#[test]
fn explicit_float_inputs_choose_double_and_refs_are_not_arithmetic_numbers() {
    assert_operation(
        Function::Add,
        Value::Float(0.5),
        Value::Long(1),
        Value::Double(1.5),
    );
    assert_operation(
        Function::Multiply,
        decimal("1.25"),
        Value::Float(2.0),
        Value::Double(2.5),
    );
    assert_operation(
        Function::Divide,
        Value::Float(3.0),
        Value::Float(2.0),
        Value::Double(1.5),
    );
    assert_operation(
        Function::Subtract,
        Value::Double(2.75),
        integer(2),
        Value::Double(0.75),
    );
    assert_arithmetic_error(
        Function::Add,
        integer(BigInt::from(10u8).pow(400)),
        Value::Double(1.0),
    );
    assert_arithmetic_error(Function::Multiply, decimal("1e400"), Value::Float(0.0));
    for function in [
        Function::Add,
        Function::Subtract,
        Function::Multiply,
        Function::Divide,
    ] {
        assert_arithmetic_error(function.clone(), Value::Ref(1), Value::Long(2));
        assert_arithmetic_error(function, Value::Long(2), Value::Ref(1));
    }
}

fn aggregate_query(function: Aggregate) -> PreparedQuery {
    let mut query = Query::new(
        FindSpec::Scalar(FindElement::Aggregate {
            function,
            variable: "number".into(),
        }),
        vec![],
    );
    query.inputs = vec![InputSpec::Relation(vec![
        Some("number".into()),
        Some("ordinal".into()),
    ])];
    // Public documented bag semantics: equal values from distinct rows count.
    query.with = vec!["ordinal".into()];
    PreparedQuery::new(&query).unwrap()
}

fn aggregate_input(values: &[Value]) -> [QueryInput; 1] {
    [QueryInput::Relation(
        values
            .iter()
            .enumerate()
            .map(|(ordinal, value)| vec![value.clone(), Value::Long(ordinal as i64)])
            .collect(),
    )]
}

fn execute_aggregate(function: Aggregate, values: &[Value]) -> Result<QueryOutcome, SemanticError> {
    aggregate_query(function).execute(&[], &aggregate_input(values), &QueryControl::default())
}

fn assert_aggregate(function: Aggregate, values: &[Value], expected: Value) {
    assert_value(
        scalar(execute_aggregate(function, values).unwrap()),
        expected,
    );
}

fn permutations_three(values: &[Value; 3]) -> Vec<Vec<Value>> {
    [
        [0, 1, 2],
        [0, 2, 1],
        [1, 0, 2],
        [1, 2, 0],
        [2, 0, 1],
        [2, 1, 0],
    ]
    .into_iter()
    .map(|order| order.map(|index| values[index].clone()).to_vec())
    .collect()
}

#[test]
fn sum_selects_the_complete_group_domain_before_any_intermediate_arithmetic() {
    for values in permutations_three(&[Value::Long(i64::MAX), Value::Long(1), integer(-1)]) {
        assert_aggregate(Aggregate::Sum, &values, integer(i64::MAX));
    }
    for values in permutations_three(&[Value::Long(i64::MAX), integer(1), decimal("-0.5")]) {
        assert_aggregate(Aggregate::Sum, &values, decimal("9223372036854775807.5"));
    }
    assert_aggregate(
        Aggregate::Sum,
        &[decimal("1.20"), decimal("1.20")],
        decimal("2.4"),
    );
    assert_aggregate(
        Aggregate::Sum,
        &[Value::Long(1), Value::Float(0.5), integer(2)],
        Value::Double(3.5),
    );
    let error =
        execute_aggregate(Aggregate::Sum, &[Value::Long(i64::MAX), Value::Long(1)]).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Incorrect);
    for function in [
        Aggregate::Sum,
        Aggregate::Median,
        Aggregate::Average,
        Aggregate::Variance,
        Aggregate::StandardDeviation,
    ] {
        assert_eq!(
            execute_aggregate(function, &[Value::Ref(1)])
                .unwrap_err()
                .category,
            ErrorCategory::Incorrect
        );
    }
}

#[test]
fn median_preserves_exact_selected_values_and_uses_domain_appropriate_midpoints() {
    let huge = BigInt::from(10u8).pow(100);
    assert_aggregate(
        Aggregate::Median,
        &[
            integer(&huge + 2),
            integer(huge.clone()),
            integer(&huge + 1),
        ],
        integer(&huge + 1),
    );
    assert_aggregate(
        Aggregate::Median,
        &[decimal("0.5"), Value::Long(1), integer(2)],
        Value::Long(1),
    );
    assert_aggregate(
        Aggregate::Median,
        &[Value::Long(i64::MAX), Value::Long(i64::MAX)],
        Value::Long(i64::MAX),
    );
    assert_aggregate(
        Aggregate::Median,
        &[Value::Long(-2), Value::Long(-1)],
        Value::Long(-1),
    );
    assert_aggregate(Aggregate::Median, &[integer(-2), integer(-1)], integer(-1));
    assert_aggregate(
        Aggregate::Median,
        &[integer(huge.clone()), integer(&huge + 1)],
        integer(huge),
    );
    assert_aggregate(
        Aggregate::Median,
        &[decimal("1.2"), decimal("1.3")],
        decimal("1.25"),
    );
    assert_aggregate(
        Aggregate::Median,
        &[Value::Float(1.0), Value::Long(2)],
        Value::Double(1.5),
    );
}

fn assert_double_close(actual: Value, expected: f64) {
    let Value::Double(actual) = actual else {
        panic!("expected approximate Double, got {actual:?}")
    };
    assert!(
        actual.is_finite(),
        "unexpected non-finite statistic {actual}"
    );
    assert!(
        (actual - expected).abs() <= expected.abs().max(1.0) * 1e-12,
        "{actual} differs from {expected}"
    );
}

#[test]
fn approximate_statistics_preserve_exact_sum_cancellation_and_centering() {
    assert_aggregate(
        Aggregate::Average,
        &[Value::Long(i64::MAX), Value::Long(i64::MAX)],
        Value::Double(i64::MAX as f64),
    );
    let huge = BigInt::from(10u8).pow(400);
    for values in permutations_three(&[integer(huge.clone()), integer(-&huge), Value::Long(3)]) {
        assert_aggregate(Aggregate::Average, &values, Value::Double(1.0));
    }
    for values in [
        vec![integer(huge.clone()), integer(&huge + 2)],
        vec![Value::Long(i64::MAX - 2), Value::Long(i64::MAX)],
        vec![
            decimal("100000000000000000000.1"),
            decimal("100000000000000000000.3"),
        ],
    ] {
        let expected_variance = if matches!(&values[0], Value::BigDec(_)) {
            0.01
        } else {
            1.0
        };
        assert_double_close(
            scalar(execute_aggregate(Aggregate::Variance, &values).unwrap()),
            expected_variance,
        );
        assert_double_close(
            scalar(execute_aggregate(Aggregate::StandardDeviation, &values).unwrap()),
            expected_variance.sqrt(),
        );
    }
    assert_double_close(
        scalar(
            execute_aggregate(
                Aggregate::Variance,
                &[Value::Long(1), Value::Long(2), Value::Long(3)],
            )
            .unwrap(),
        ),
        2.0 / 3.0,
    );
    assert_eq!(
        execute_aggregate(Aggregate::Average, &[integer(huge)])
            .unwrap_err()
            .category,
        ErrorCategory::Incorrect
    );
}

#[test]
fn compact_decimal_identities_accept_both_scale_boundaries() {
    for scale in [i64::MIN, i64::MIN + 1, i64::MAX - 1, i64::MAX] {
        let value = Value::BigDec(BigDecimal::new(7.into(), scale));
        for (function, right) in [
            (Function::Add, 0),
            (Function::Subtract, 0),
            (Function::Multiply, 1),
            (Function::Divide, 1),
        ] {
            let outcome = execute_operation(
                &operation(function),
                value.clone(),
                Value::Long(right),
                &QueryControl {
                    max_numeric_bytes: 4_096,
                    ..Default::default()
                },
            )
            .unwrap();
            assert_value(scalar(outcome), value.clone());
        }
    }
}

#[test]
fn arithmetic_limits_fail_explicitly_and_prepared_queries_remain_reusable() {
    let prepared = operation(Function::Add);
    let tiny = decimal("1e-1000000");
    let constrained = QueryControl {
        max_numeric_bytes: 1_024,
        ..Default::default()
    };
    let error = execute_operation(&prepared, tiny, Value::Long(1), &constrained).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Busy, "{error:?}");
    for control in [
        QueryControl {
            max_work: 0,
            ..Default::default()
        },
        QueryControl {
            cancel: Arc::new(AtomicBool::new(true)),
            ..Default::default()
        },
    ] {
        let error = execute_operation(&prepared, decimal("1"), decimal("2"), &control).unwrap_err();
        assert!(
            matches!(
                error.category,
                ErrorCategory::Busy | ErrorCategory::Interrupted
            ),
            "{error:?}"
        );
    }
    for _ in 0..2 {
        assert_value(
            scalar(
                execute_operation(
                    &prepared,
                    decimal("1.25"),
                    decimal("2.75"),
                    &QueryControl::default(),
                )
                .unwrap(),
            ),
            decimal("4"),
        );
    }

    // The same prepared operation and row count need more accounted work for
    // a genuinely large coefficient, not merely for loading more query rows.
    let multiply = operation(Function::Multiply);
    let ordinary =
        execute_operation(&multiply, integer(2), integer(3), &QueryControl::default()).unwrap();
    let large = integer(BigInt::from(1u8) << 8_192usize);
    let limited_work = QueryControl {
        max_work: ordinary.stats.work as usize,
        ..Default::default()
    };
    let error = execute_operation(&multiply, large.clone(), large, &limited_work).unwrap_err();
    assert_eq!(error.category, ErrorCategory::Busy, "{error:?}");
    assert_eq!(error.code, "query/work-limit");
    assert_value(
        scalar(execute_operation(&multiply, integer(2), integer(3), &limited_work).unwrap()),
        integer(6),
    );

    let aggregate = aggregate_query(Aggregate::Sum);
    let error = aggregate
        .execute(
            &[],
            &aggregate_input(&[decimal("1e-1000000"), Value::Long(1)]),
            &constrained,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Busy, "{error:?}");
    for _ in 0..2 {
        assert_value(
            scalar(
                aggregate
                    .execute(
                        &[],
                        &aggregate_input(&[decimal("1.25"), decimal("2.75")]),
                        &QueryControl::default(),
                    )
                    .unwrap(),
            ),
            decimal("4"),
        );
    }
    let division = operation(Function::Divide);
    assert!(
        execute_operation(
            &division,
            decimal("1"),
            decimal("3"),
            &QueryControl::default()
        )
        .is_err()
    );
    assert_value(
        scalar(
            execute_operation(
                &division,
                decimal("1"),
                decimal("8"),
                &QueryControl::default(),
            )
            .unwrap(),
        ),
        decimal("0.125"),
    );
}
