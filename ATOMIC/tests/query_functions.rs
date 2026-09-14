//! Source-free public contracts for the deliberately small portable library.
use atomic_core::{
    Binding, Clause, FindElement, FindSpec, Function, InputSpec, Keyword, PreparedQuery, Query,
    QueryControl, QueryInput, QueryOutcome, QueryResult, QueryValue, SemanticError, Symbol, Term,
    Value,
};
use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use std::mem::discriminant;
use std::sync::atomic::Ordering;
use std::time::Instant;

fn scalar(value: Value) -> QueryValue {
    QueryValue::Scalar(value)
}
fn long(value: i64) -> QueryValue {
    scalar(Value::Long(value))
}
fn text(value: &str) -> QueryValue {
    scalar(Value::String(value.into()))
}
fn query(function: Function, arity: usize) -> Query {
    let mut query = Query::new(
        FindSpec::Scalar(FindElement::Variable("result".into())),
        vec![Clause::Function {
            function,
            source: "$deliberately-absent".into(),
            args: (0..arity)
                .map(|i| Term::var(format!("arg{i}").as_str()))
                .collect(),
            binding: Binding::Scalar("result".into()),
        }],
    );
    query.inputs = (0..arity)
        .map(|i| InputSpec::Scalar(format!("arg{i}").as_str().into()))
        .collect();
    query
}
fn execute(
    function: Function,
    args: Vec<QueryValue>,
    control: &QueryControl,
) -> Result<QueryOutcome, SemanticError> {
    PreparedQuery::new(&query(function, args.len()))?.execute(
        &[],
        &args
            .into_iter()
            .map(QueryInput::General)
            .collect::<Vec<_>>(),
        control,
    )
}
fn output(outcome: &QueryOutcome) -> &Value {
    let QueryResult::Scalar(Some(QueryValue::Scalar(value))) = &outcome.result else {
        panic!("expected scalar: {:?}", outcome.result);
    };
    value
}
fn assert_function(function: Function, args: Vec<QueryValue>, expected: Value) {
    let outcome = execute(function, args, &QueryControl::default()).unwrap();
    assert_eq!(discriminant(output(&outcome)), discriminant(&expected));
    assert_eq!(output(&outcome), &expected);
}

#[test]
fn count_handles_general_collections_and_unicode_scalar_strings_without_a_database() {
    for (value, count) in [
        (QueryValue::Nil, 0),
        (text(""), 0),
        (text("aλ😀e\u{301}"), 5), // combining mark is its own scalar, not a grapheme
        (QueryValue::Tuple(vec![long(1)]), 1),
        (
            QueryValue::Collection(vec![long(1), long(1), QueryValue::Nil]),
            3,
        ),
        (
            QueryValue::Map(vec![
                (QueryValue::Nil, text("nil")),
                (QueryValue::Char('x'), long(1)),
            ]),
            2,
        ),
        (
            QueryValue::Set(vec![long(2), long(1), scalar(Value::Double(1.0)), long(2)]),
            2,
        ),
        (scalar(Value::Bytes(vec![0, 1, 2, 3])), 4),
        (scalar(Value::Tuple(vec![None, Some(Value::Long(1))])), 2),
    ] {
        assert_function(Function::Count, vec![value], Value::Long(count));
    }
    for value in [
        long(3),
        QueryValue::Char('x'),
        QueryValue::Tagged(Symbol::unqualified("x"), Box::new(QueryValue::Nil)),
    ] {
        assert_eq!(
            execute(Function::Count, vec![value], &QueryControl::default())
                .unwrap_err()
                .code,
            "query/function-type"
        );
    }
}

#[test]
fn substring_uses_checked_unicode_scalar_offsets_and_optional_end() {
    for (start, end, expected) in [
        (0, Some(0), ""),
        (1, Some(3), "λ😀"),
        (3, None, "e\u{301}"),
        (5, None, ""),
        (4, Some(5), "\u{301}"),
    ] {
        let mut args = vec![text("aλ😀e\u{301}"), long(start)];
        if let Some(end) = end {
            args.push(long(end));
        }
        assert_function(Function::Subs, args, Value::String(expected.into()));
    }
    assert_function(
        Function::Subs,
        vec![text("abc"), scalar(Value::BigInt(1.into()))],
        Value::String("bc".into()),
    );
    for args in [
        vec![text("abc"), long(-1)],
        vec![text("abc"), long(4)],
        vec![text("abc"), long(2), long(1)],
        vec![text("abc"), long(0), long(4)],
        vec![text("abc"), scalar(Value::Double(1.0))],
    ] {
        assert_eq!(
            execute(Function::Subs, args, &QueryControl::default())
                .unwrap_err()
                .code,
            "query/function-type"
        );
    }
}

#[test]
fn string_predicates_are_literal_unicode_safe_and_case_sensitive() {
    for (function, haystack, needle, expected) in [
        (Function::StartsWith, "λ😀ABC", "λ😀", true),
        (Function::StartsWith, "λ😀ABC", "😀", false),
        (Function::EndsWith, "ABCλ😀", "λ😀", true),
        (Function::EndsWith, "ABCλ😀", "λ", false),
        (Function::Includes, "abλ😀cd", "λ😀", true),
        (Function::Includes, "abcd", "BC", false),
        (Function::Includes, "ababaabc", "abaabc", true),
        (Function::Includes, "ababaabc", "abaabd", false),
        (Function::Includes, "", "", true),
        (Function::StartsWith, "", "", true),
        (Function::EndsWith, "", "", true),
        (Function::Includes, "abc", "abcd", false),
    ] {
        assert_function(
            function,
            vec![text(haystack), text(needle)],
            Value::Bool(expected),
        );
    }
}

#[test]
fn str_formats_general_data_nil_and_compact_exact_numbers_with_no_execution() {
    assert_function(Function::Str, vec![], Value::String(String::new()));
    assert_function(
        Function::Str,
        vec![
            QueryValue::Nil,
            text("hello"),
            QueryValue::Char('λ'),
            long(42),
        ],
        Value::String("helloλ42".into()),
    );
    let map = QueryValue::Map(vec![
        (
            scalar(Value::Keyword(Keyword::new("app", "z"))),
            QueryValue::Nil,
        ),
        (
            scalar(Value::Keyword(Keyword::new("app", "a"))),
            QueryValue::Tuple(vec![text("a\n\"b"), QueryValue::Nil, QueryValue::Char('λ')]),
        ),
    ]);
    assert_function(
        Function::Str,
        vec![map],
        Value::String("{:app/a [\"a\\n\\\"b\" nil \\λ], :app/z nil}".into()),
    );
    assert_function(
        Function::Str,
        vec![QueryValue::Set(vec![long(2), long(1), long(1)])],
        Value::String("#{1 2}".into()),
    );
    assert_function(
        Function::Str,
        vec![QueryValue::Collection(vec![
            QueryValue::Char(' '),
            QueryValue::Char('\n'),
        ])],
        Value::String("[\\space \\newline]".into()),
    );
    assert_function(
        Function::Str,
        vec![QueryValue::Tagged(
            Symbol::new("app", "inert"),
            Box::new(QueryValue::Nil),
        )],
        Value::String("#app/inert nil".into()),
    );
    for scale in [i64::MIN, -100_000, 100_000, i64::MAX] {
        assert_function(
            Function::Str,
            vec![scalar(Value::BigDec(BigDecimal::new(12.into(), scale)))],
            Value::String(format!("12E{}", -i128::from(scale))),
        );
    }
    assert_function(
        Function::Str,
        vec![scalar(Value::BigDec("2.50".parse().unwrap()))],
        Value::String("2.50".into()),
    );
}

#[test]
fn quotient_preserves_exact_domains_and_truncates_toward_zero() {
    for (left, right, expected) in [(7, 2, 3), (-7, 2, -3), (7, -2, -3), (-7, -2, 3)] {
        assert_function(
            Function::Quot,
            vec![long(left), long(right)],
            Value::Long(expected),
        );
        assert_function(
            Function::Quot,
            vec![scalar(Value::BigInt(left.into())), long(right)],
            Value::BigInt(expected.into()),
        );
        assert_function(
            Function::Quot,
            vec![
                scalar(Value::Double(
                    left as f64 + if left > 0 { 0.5 } else { -0.5 },
                )),
                long(right),
            ],
            Value::Double(expected as f64),
        );
    }
    let large: BigInt = (BigInt::from(1) << 256usize) + 17;
    assert_function(
        Function::Quot,
        vec![scalar(Value::BigInt(large.clone())), long(-7)],
        Value::BigInt(large / -7),
    );

    // Independent bounded rational oracle, including non-terminating decimal
    // division: quot must truncate 7/3, not request an exact decimal expansion.
    for a in [-17, -7, 0, 7, 17] {
        for b in [-7, -2, 2, 7] {
            for sa in [-3_i64, 0, 3] {
                for sb in [-3_i64, 0, 3] {
                    let shift = sb - sa;
                    let factor = BigInt::from(10).pow(shift.unsigned_abs() as u32);
                    let expected = if shift >= 0 {
                        BigInt::from(a) * factor / b
                    } else {
                        BigInt::from(a) / (BigInt::from(b) * factor)
                    };
                    assert_function(
                        Function::Quot,
                        vec![
                            scalar(Value::BigDec(BigDecimal::new(a.into(), sa))),
                            scalar(Value::BigDec(BigDecimal::new(b.into(), sb))),
                        ],
                        Value::BigDec(BigDecimal::new(expected, 0)),
                    );
                }
            }
        }
    }
}

#[test]
fn quotient_extreme_exponents_zero_divisors_nonfinite_and_capacity_are_explicit() {
    for scale in [i64::MIN, -100_000] {
        assert_function(
            Function::Quot,
            vec![
                scalar(Value::BigDec(BigDecimal::new(1.into(), scale))),
                long(2),
            ],
            Value::BigDec(BigDecimal::new(5.into(), scale + 1)),
        );
    }
    for scale in [100_000, i64::MAX] {
        assert_function(
            Function::Quot,
            vec![
                scalar(Value::BigDec(BigDecimal::new((-1).into(), scale))),
                long(3),
            ],
            Value::BigDec(BigDecimal::new(0.into(), 0)),
        );
    }
    for (left, right) in [
        (Value::Long(i64::MIN), Value::Long(-1)),
        (Value::Long(1), Value::Long(0)),
        (Value::BigInt(1.into()), Value::BigInt(0.into())),
        (Value::BigDec(1.into()), Value::BigDec(0.into())),
        (Value::Double(1.0), Value::Double(-0.0)),
        (Value::Double(f64::INFINITY), Value::Double(2.0)),
        (Value::Double(1.0), Value::Double(f64::NAN)),
        (Value::Double(f64::MAX), Value::Double(f64::MIN_POSITIVE)),
    ] {
        assert!(
            execute(
                Function::Quot,
                vec![scalar(left), scalar(right)],
                &QueryControl::default()
            )
            .unwrap_err()
            .code
            .starts_with("query/arithmetic")
        );
    }
    let control = QueryControl {
        max_numeric_bytes: 1024,
        ..QueryControl::default()
    };
    let error = execute(
        Function::Quot,
        vec![
            scalar(Value::BigDec(BigDecimal::new(1.into(), -100_000))),
            long(3),
        ],
        &control,
    )
    .unwrap_err();
    assert_eq!(error.code, "query/numeric-capacity");
}

#[test]
fn portable_arity_types_controls_and_prepared_reuse_remain_checked() {
    for (function, args) in [
        (Function::Count, vec![]),
        (Function::Quot, vec![long(1)]),
        (Function::Subs, vec![text("a")]),
        (Function::StartsWith, vec![text("a")]),
        (Function::EndsWith, vec![text("a"), text("a"), text("a")]),
        (Function::Includes, vec![]),
    ] {
        assert_eq!(
            execute(function, args, &QueryControl::default())
                .unwrap_err()
                .code,
            "query/function-arity"
        );
    }
    let prepared = PreparedQuery::new(&query(Function::Includes, 2)).unwrap();
    let inputs = [
        QueryInput::Scalar(Value::String("a".repeat(4096))),
        QueryInput::Scalar(Value::String(format!("{}b", "a".repeat(128)))),
    ];
    let success = prepared
        .execute(&[], &inputs, &QueryControl::default())
        .unwrap();
    assert_eq!(output(&success), &Value::Bool(false));
    for control in [
        QueryControl {
            max_work: success.stats.work as usize - 1,
            ..QueryControl::default()
        },
        QueryControl {
            max_value_bytes: success.stats.allocated_value_bytes - 1,
            ..QueryControl::default()
        },
    ] {
        let error = prepared.execute(&[], &inputs, &control).unwrap_err();
        assert!(matches!(
            error.code,
            "query/work-limit" | "query/value-byte-limit"
        ));
    }
    let control = QueryControl::default();
    control.cancel.store(true, Ordering::Relaxed);
    assert_eq!(
        prepared.execute(&[], &inputs, &control).unwrap_err().code,
        "query/canceled"
    );
    control.cancel.store(false, Ordering::Relaxed);
    assert_eq!(
        output(&prepared.execute(&[], &inputs, &control).unwrap()),
        &Value::Bool(false)
    );
    assert_eq!(
        execute(
            Function::Str,
            vec![text(&"x".repeat(4096)); 4],
            &QueryControl {
                max_value_bytes: 4096,
                ..QueryControl::default()
            }
        )
        .unwrap_err()
        .code,
        "query/value-byte-limit"
    );
}

#[test]
fn source_free_deep_str_is_iterative_and_output_limited() {
    std::thread::Builder::new()
        .stack_size(256 * 1024)
        .spawn(|| {
            let mut value = QueryValue::Nil;
            for _ in 0..4096 {
                value = QueryValue::Tagged(Symbol::unqualified("t"), Box::new(value));
            }
            let outcome = execute(Function::Str, vec![value], &QueryControl::default()).unwrap();
            let Value::String(text) = output(&outcome) else {
                panic!("str type");
            };
            assert_eq!(text.len(), 4096 * 3 + 3);
            assert!(text.ends_with("nil"));
            drop(outcome);
        })
        .unwrap()
        .join()
        .unwrap();
}

#[test]
fn complete_portable_search_cost_is_linear_on_repeated_prefix_adversaries() {
    let mut previous = None;
    for size in [1024, 4096, 16_384] {
        let haystack = format!("{}b", "a".repeat(size));
        let needle = format!("{}b", "a".repeat(size / 2));
        let started = Instant::now();
        let prepared = PreparedQuery::new(&query(Function::Includes, 2)).unwrap();
        let inputs = [
            QueryInput::Scalar(Value::String(haystack)),
            QueryInput::Scalar(Value::String(needle)),
        ];
        let outcome = prepared
            .execute(&[], &inputs, &QueryControl::default())
            .unwrap();
        assert_eq!(output(&outcome), &Value::Bool(true));
        let (work, bytes) = (outcome.stats.work, outcome.stats.allocated_value_bytes);
        drop(outcome);
        drop(inputs);
        drop(prepared);
        eprintln!(
            "portable-includes bytes={size} prepare+execute+check+drop={:?} work={work} accounted_bytes={bytes}",
            started.elapsed()
        );
        if let Some(prior) = previous {
            assert!(work <= prior * 5, "quadratic repeated-prefix work");
        }
        previous = Some(work);
    }
}
