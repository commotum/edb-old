//! Numeric logical identity is separate from canonical stored representation.
use atomic_core::{
    Clause, DataPattern, Datom, DurableTransaction, EntityRef, FindElement, FindSpec,
    INITIAL_EIDX_FRONTIER, InputSpec, ProgramOutput, Query, QueryControl, QueryDataSource,
    QueryEngine, QueryInput, QueryResult, QueryValue, Term, TxForm, TxOp, USER_PARTITION, Value,
    decode_transaction, encode_program_output, encode_transaction, make_eid, sha256,
    submission_request_digest, t_to_tx,
};
use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};
use std::str::FromStr;

fn decimal(text: &str) -> Value {
    Value::BigDec(BigDecimal::from_str(text).unwrap())
}

fn canonical_values() -> Vec<Value> {
    vec![
        Value::Long(i64::MIN),
        Value::Long(1),
        Value::Ref(make_eid(USER_PARTITION, 42).unwrap()),
        Value::BigInt(BigInt::from(1u8) << 80),
        decimal("-12.3400"),
        decimal("1.0"),
        decimal("1.00"),
        Value::Float(0.1),
        Value::Double(0.1),
        Value::Float(-0.0),
        Value::Double(-0.0),
        Value::Float(f32::INFINITY),
        Value::Double(f64::NEG_INFINITY),
        Value::Float(f32::from_bits(0x7fc0_0123)),
        Value::Double(f64::from_bits(0xfff8_0000_0000_0456)),
        Value::Tuple(vec![Some(decimal("1.00")), None, Some(Value::Long(1))]),
    ]
}

fn hex(bytes: &[u8]) -> String {
    bytes.iter().map(|byte| format!("{byte:02x}")).collect()
}

#[test]
fn canonical_numeric_golden_contract() {
    let values = canonical_values();
    let tx = t_to_tx(1).unwrap();
    let transaction = DurableTransaction {
        database_id: "numeric-contract".into(),
        basis_t: 1,
        previous_hash: [7; 32],
        eidx_frontier: INITIAL_EIDX_FRONTIER.max(100),
        tempids: BTreeMap::new(),
        tx_data: values
            .iter()
            .enumerate()
            .map(|(i, value)| Datom {
                entity: make_eid(USER_PARTITION, 42 + i as u64).unwrap(),
                attribute: 1_000,
                value: value.clone(),
                tx,
                added: true,
            })
            .collect(),
    };
    let forms: Vec<_> = values
        .iter()
        .enumerate()
        .map(|(i, value)| {
            TxForm::Op(TxOp::Add {
                entity: EntityRef::Temp(format!("number-{i}")),
                attribute: 1_000,
                value: value.clone().into(),
            })
        })
        .collect();
    let encoded = encode_transaction(&transaction).unwrap();
    // Current durable ATMC numeric bytes are distinct from the deliberately
    // private and replaceable prepared-query cache-key representation.
    assert_eq!(encoded.len(), 634);
    assert_eq!(
        hex(&sha256(&encoded)),
        "d597bce13f95f90bff95b00b01d7b0d5ec2e9e1d7aeb2f64c5a8fbf2e8706a60"
    );
    assert_eq!(
        hex(&submission_request_digest(&forms, Some(7), Some(123_456)).unwrap()),
        "1a3d58bd67a52e264ef738ccf53cd340834b1201aafc5cbb2afa28ea3d21a0cf"
    );
    assert_eq!(
        encode_transaction(&decode_transaction(&encoded).unwrap()).unwrap(),
        encoded
    );
    let expected = [
        "41544d4308000300000000000000001602000000010000000d000000010880000000000000009dc721b207c9f380214bb7947150642da8cac079fae6eee80d19e63d121e0820",
        "41544d4308000300000000000000001602000000010000000d0000000108000000000000000103a67e869edd107e2b76927e34225b97e761e75cfba4ded352e0d47e443c795c",
        "41544d4308000300000000000000001602000000010000000d0000000109000010000000002a1019fbad990c0a5052de3bbf92f6961f3a19f11b583906f8a0c43ade22bdf53f",
        "41544d4308000300000000000000001d02000000010000001400000001010000000b010000000000000000000007fe85a2692b4dc55b06013eda7cfe93da9290163d4bd2b299750fd08a54df44",
        "41544d4308000300000000000000001d0200000001000000140000000100000000000000000400000003fe1df88b7a7864167003bf5fba161075506d8cd8d8028f6344d9c617f0f61a5097e53d",
        "41544d4308000300000000000000001b02000000010000001200000001000000000000000001000000010a9d9e8aa54610378e5aa61e042e42c59cc2fb5e6f43d3a1c3b45f891d78ff852d",
        "41544d4308000300000000000000001b020000000100000012000000010000000000000000020000000164ab4f31d540f7aef588e4e2e3125e473354825d0b4b7de631ab9e616dad9a9bc1",
        "41544d4308000300000000000000001202000000010000000900000001053dcccccddeb4d5c7d60beedda84342ed70938e88b82f66a58b3016e9c6b9f1544ee75d23",
        "41544d4308000300000000000000001602000000010000000d00000001043fb999999999999ac584266d73658452d3a30271513654ee6b4362800bc3e565b167255ffeba875a",
        "41544d430800030000000000000000120200000001000000090000000105000000008423121377091591c0279c349ff8028b20610163a8bb2dc754bd0a77d22a979a",
        "41544d4308000300000000000000001602000000010000000d00000001040000000000000000c3b853e608764b312819577e7d643e1404b2cf2e37896ccc3a99227933d10699",
        "41544d4308000300000000000000001202000000010000000900000001057f800000bc02fb3f2278511149bcb48a5a144467212074e0cfa8e00999ecb8af7a454a0c",
        "41544d4308000300000000000000001602000000010000000d0000000104fff00000000000007eef49f141e5cc274ba9d6ef81c0d9e8c25d0c18eaa18a8d8067b84932450856",
        "41544d4308000300000000000000001202000000010000000900000001057fc000001f372f02b0a4e66f729774c840938159d6fdc66bb64d1efcbf4b528a7b82898e",
        "41544d4308000300000000000000001602000000010000000d00000001047ff8000000000000485cb2c7a98ecb2d190b2dde80e19e1589bfa1b7e7c038063361608b2c8f6e88",
        "41544d4308000300000000000000002c020000000100000023000000010c00000003010000000000000000020000000164000108000000000000000146a0d98260cb9e04906e8a9ccf487c191ca610e26dfac6b77624d08351d93773",
    ];
    assert_eq!(values.len(), expected.len());
    for (value, expected) in values.into_iter().zip(expected) {
        let bytes = encode_program_output(&ProgramOutput::Query(vec![vec![value]])).unwrap();
        assert_eq!(hex(&bytes), expected);
    }
}

// Each group denotes one mathematical value, in ascending order. Expected
// identity/order is deliberately not computed by Value's comparison or hash.
fn ordered_numeric_groups() -> Vec<Vec<Value>> {
    vec![
        vec![
            Value::Float(f32::NEG_INFINITY),
            Value::Double(f64::NEG_INFINITY),
        ],
        vec![decimal("-1e100")],
        vec![
            Value::BigInt(-(BigInt::from(1u8) << 80usize)),
            Value::Float(-2f32.powi(80)),
            Value::Double(-2f64.powi(80)),
        ],
        vec![
            Value::Long(i64::MIN),
            Value::BigInt(i64::MIN.into()),
            decimal("-9223372036854775808.00"),
            Value::Float(-2f32.powi(63)),
            Value::Double(-2f64.powi(63)),
        ],
        vec![
            Value::Long(-1),
            Value::BigInt((-1).into()),
            decimal("-1.000"),
            Value::Float(-1.0),
            Value::Double(-1.0),
        ],
        vec![decimal("-0.50"), Value::Float(-0.5), Value::Double(-0.5)],
        vec![decimal("-1e-100")],
        vec![
            Value::Long(0),
            Value::Ref(0),
            Value::BigInt(0.into()),
            decimal("0.000"),
            decimal("0e100"),
            Value::Float(0.0),
            Value::Float(-0.0),
            Value::Double(0.0),
            Value::Double(-0.0),
        ],
        vec![decimal("1e-100")],
        vec![decimal("0.1"), decimal("0.1000")],
        // Binary 0.1 is not decimal 0.1, and f32/f64 round differently.
        vec![Value::Double(0.1)],
        vec![Value::Float(0.1)],
        vec![
            decimal("0.5"),
            decimal("0.5000"),
            Value::Float(0.5),
            Value::Double(0.5),
        ],
        vec![
            Value::Long(1),
            Value::Ref(1),
            Value::BigInt(1.into()),
            decimal("1.0"),
            decimal("1.00"),
            Value::Float(1.0),
            Value::Double(1.0),
        ],
        vec![
            Value::Long(9_007_199_254_740_992),
            Value::Ref(9_007_199_254_740_992),
            Value::BigInt(9_007_199_254_740_992u64.into()),
            decimal("9007199254740992.0"),
            Value::Double(9_007_199_254_740_992.0),
        ],
        vec![
            Value::Long(9_007_199_254_740_993),
            Value::Ref(9_007_199_254_740_993),
            Value::BigInt(9_007_199_254_740_993u64.into()),
            decimal("9007199254740993.0"),
        ],
        vec![
            Value::Long(i64::MAX),
            Value::Ref(i64::MAX as u64),
            Value::BigInt(i64::MAX.into()),
            decimal("9223372036854775807.00"),
        ],
        vec![
            Value::Ref(1u64 << 63),
            Value::BigInt(BigInt::from(1u8) << 63),
            decimal("9223372036854775808.00"),
            Value::Float(2f32.powi(63)),
            Value::Double(2f64.powi(63)),
        ],
        vec![
            Value::Ref(u64::MAX),
            Value::BigInt(u64::MAX.into()),
            decimal("18446744073709551615.00"),
        ],
        vec![
            Value::BigInt(BigInt::from(1u8) << 64),
            Value::Float(2f32.powi(64)),
            Value::Double(2f64.powi(64)),
        ],
        vec![
            Value::BigInt(BigInt::from(1u8) << 80),
            decimal("1208925819614629174706176.0"),
            Value::Float(2f32.powi(80)),
            Value::Double(2f64.powi(80)),
        ],
        vec![decimal("1e100")],
        vec![Value::Float(f32::INFINITY), Value::Double(f64::INFINITY)],
        vec![
            Value::Float(f32::NAN),
            Value::Float(f32::from_bits(0xffc0_1234)),
            Value::Double(f64::NAN),
            Value::Double(f64::from_bits(0xfff8_0000_0000_0456)),
        ],
    ]
}

#[test]
fn cross_representation_order_obeys_independent_ordered_equivalence_classes() {
    let groups = ordered_numeric_groups();
    for (left_group, left_values) in groups.iter().enumerate() {
        for (right_group, right_values) in groups.iter().enumerate() {
            for left in left_values {
                for right in right_values {
                    let expected = left_group.cmp(&right_group);
                    assert_eq!(left.index_cmp(right), expected, "{left:?} versus {right:?}");
                    assert_eq!(
                        left == right,
                        expected == Ordering::Equal,
                        "{left:?} versus {right:?}"
                    );
                }
            }
        }
    }
    // Exhaustive ordered classes above check equality substitution and
    // transitivity, including the >2^53 and >i64::MAX precision boundaries.
}

// Deliberately slow, bounded pre-repair-style rational oracle. This must never
// receive the compact extreme-scale fixtures below: expanding those scales is
// exactly the production behavior being removed.
fn bounded_rational(value: &Value) -> Option<(BigInt, BigInt)> {
    let one = BigInt::from(1u8);
    match value {
        Value::Long(value) => Some(((*value).into(), one)),
        Value::Ref(value) => Some(((*value).into(), one)),
        Value::BigInt(value) => Some((value.clone(), one)),
        Value::BigDec(value) => {
            let (coefficient, scale) = value.as_bigint_and_scale();
            let coefficient = coefficient.into_owned();
            assert!(scale.unsigned_abs() <= 1_100);
            let power = BigInt::from(10u8).pow(scale.unsigned_abs() as u32);
            Some(if scale >= 0 {
                (coefficient, power)
            } else {
                (coefficient * power, one)
            })
        }
        Value::Float(value) => bounded_rational(&Value::Double(f64::from(*value))),
        Value::Double(value) if value.is_finite() => {
            let bits = value.to_bits();
            let exponent = ((bits >> 52) & 0x7ff) as i32;
            let fraction = bits & ((1u64 << 52) - 1);
            let (significand, exponent) = if exponent == 0 {
                (fraction, -1074)
            } else {
                (fraction | (1u64 << 52), exponent - 1075)
            };
            let signed = BigInt::from(significand) * if value.is_sign_negative() { -1 } else { 1 };
            Some(if exponent >= 0 {
                (signed << exponent as usize, one)
            } else {
                (signed, one << (-exponent) as usize)
            })
        }
        _ => None,
    }
}

#[test]
fn subnormal_and_extreme_finite_floats_match_bounded_rational_reference() {
    let mut values: Vec<_> = ordered_numeric_groups().into_iter().flatten().collect();
    values.extend([
        Value::Double(f64::from_bits(1)),
        Value::Double(-f64::from_bits(1)),
        Value::Double(f64::from_bits(2)),
        Value::Double(f64::from_bits(0x000f_ffff_ffff_ffff)),
        Value::Double(f64::MIN_POSITIVE),
        Value::Double(f64::from_bits(0x0010_0000_0000_0001)),
        Value::Double(f64::MAX),
        Value::Double(-f64::MAX),
        Value::Float(f32::from_bits(1)),
        Value::Float(-f32::from_bits(1)),
        Value::Float(f32::MIN_POSITIVE),
        Value::Float(f32::MAX),
        decimal("1e-324"),
        decimal("4.9406564584124654e-324"),
        decimal("5e-324"),
        decimal("1.401298464324817e-45"),
        decimal("1.1754943508222875e-38"),
        decimal("2.2250738585072014e-308"),
        decimal("3.4028234663852886e38"),
        decimal("1.7976931348623157e308"),
        decimal("1.8e308"),
        // Different coefficient lengths with equal adjusted decimal spans.
        decimal("1.2345678901234567890123456789e-300"),
        decimal("1.234567890123456789012345679e-300"),
        decimal("-1.2345678901234567890123456789e300"),
        decimal("-1.234567890123456789012345679e300"),
    ]);
    let rationals: Vec<_> = values
        .iter()
        .filter_map(|value| bounded_rational(value).map(|rational| (value, rational)))
        .collect();
    for (left, (left_numerator, left_denominator)) in &rationals {
        for (right, (right_numerator, right_denominator)) in &rationals {
            let expected =
                (left_numerator * right_denominator).cmp(&(right_numerator * left_denominator));
            assert_eq!(left.index_cmp(right), expected, "{left:?} versus {right:?}");
        }
    }
}

#[test]
fn compact_extreme_scales_normalize_without_changing_identity() {
    let scaled =
        |coefficient: i64, scale: i64| Value::BigDec(BigDecimal::new(coefficient.into(), scale));
    for scale in [i64::MIN, i64::MIN + 1, i64::MAX - 1] {
        let one = scaled(1, scale);
        let ten = scaled(10, scale + 1);
        assert_eq!(one.index_cmp(&ten), Ordering::Equal);
        assert!(!one.stored_eq(&ten));
        assert_eq!(scaled(11, scale + 1).index_cmp(&one), Ordering::Greater);
        assert_eq!(
            scaled(-11, scale + 1).index_cmp(&scaled(-1, scale)),
            Ordering::Less
        );
        assert_eq!(
            scaled(0, scale).index_cmp(&Value::Double(-0.0)),
            Ordering::Equal
        );
    }
    // Normalizing 100 at MIN carries the internal exponent beyond i64::MAX;
    // the coefficient and public stored scale remain tiny and unchanged.
    assert_eq!(
        scaled(100, i64::MIN).index_cmp(&scaled(10, i64::MIN)),
        Ordering::Greater
    );
    assert_eq!(
        scaled(1, i64::MAX).index_cmp(&Value::Double(f64::from_bits(1))),
        Ordering::Less
    );
    assert_eq!(
        scaled(1, i64::MIN).index_cmp(&Value::Double(f64::MAX)),
        Ordering::Greater
    );
}

fn output_bytes(value: Value) -> Vec<u8> {
    encode_program_output(&ProgramOutput::Query(vec![vec![value]])).unwrap()
}

#[test]
fn logical_tuple_identity_does_not_erase_stored_scale_or_canonical_variant() {
    let one_scale = decimal("1.0");
    let two_scale = decimal("1.00");
    assert_eq!(one_scale, two_scale);
    assert!(!one_scale.stored_eq(&two_scale));
    assert_eq!(one_scale.stored_cmp(&two_scale), Ordering::Less);
    assert_ne!(
        output_bytes(one_scale.clone()),
        output_bytes(two_scale.clone())
    );
    assert_ne!(
        output_bytes(Value::Long(1)),
        output_bytes(one_scale.clone())
    );
    // Strict scale applies only when both top-level values are decimals.
    assert!(one_scale.stored_eq(&Value::Long(1)));
    assert!(Value::Long(1).stored_eq(&two_scale));
    let tuple_a = Value::Tuple(vec![Some(one_scale), None, Some(Value::Float(-0.0))]);
    let tuple_b = Value::Tuple(vec![Some(two_scale), None, Some(Value::BigInt(0.into()))]);
    assert_eq!(tuple_a, tuple_b);
    assert!(tuple_a.stored_eq(&tuple_b));
    assert_eq!(tuple_a.stored_cmp(&tuple_b), Ordering::Equal);
    assert_ne!(output_bytes(tuple_a.clone()), output_bytes(tuple_b));
    assert_eq!(Value::Tuple(vec![None]).index_cmp(&tuple_a), Ordering::Less);
    assert_eq!(
        Value::Tuple(vec![Some(Value::Long(1))]).index_cmp(&tuple_a),
        Ordering::Less
    );
    assert_eq!(
        output_bytes(Value::Float(-0.0)),
        output_bytes(Value::Float(0.0))
    );
    assert_eq!(
        output_bytes(Value::Double(f64::NAN)),
        output_bytes(Value::Double(f64::from_bits(0xfff8_0000_0000_0456)))
    );
}

fn join_values() -> Vec<(usize, Value)> {
    let mut groups = ordered_numeric_groups();
    groups.extend([
        vec![
            Value::Double(f64::from_bits(1)),
            Value::BigDec(BigDecimal::new(BigInt::from(5u8).pow(1074), 1074)),
        ],
        vec![
            Value::Float(f32::from_bits(1)),
            Value::Double(f64::from(f32::from_bits(1))),
            Value::BigDec(BigDecimal::new(BigInt::from(5u8).pow(149), 149)),
        ],
        vec![
            Value::Double(f64::MAX),
            Value::BigInt(BigInt::from((1u64 << 53) - 1) << 971),
        ],
        vec![
            Value::BigDec(BigDecimal::new(1.into(), i64::MIN)),
            Value::BigDec(BigDecimal::new(10.into(), i64::MIN + 1)),
        ],
        vec![
            Value::BigDec(BigDecimal::new(1.into(), i64::MAX - 1)),
            Value::BigDec(BigDecimal::new(10.into(), i64::MAX)),
        ],
    ]);
    groups.extend([
        vec![
            Value::Tuple(vec![Some(Value::Long(1)), None]),
            Value::Tuple(vec![Some(decimal("1.00")), None]),
            Value::Tuple(vec![Some(Value::Double(1.0)), None]),
        ],
        vec![Value::Tuple(vec![Some(Value::Long(1))])],
        vec![Value::Tuple(vec![None, Some(Value::Long(1))])],
        vec![
            Value::Tuple(vec![Some(Value::Tuple(vec![
                Some(Value::Float(f32::NAN)),
                Some(Value::Double(-0.0)),
            ]))]),
            Value::Tuple(vec![Some(Value::Tuple(vec![
                Some(Value::Double(f64::NAN)),
                Some(decimal("0.00")),
            ]))]),
        ],
    ]);
    groups
        .into_iter()
        .enumerate()
        .flat_map(|(group, values)| values.into_iter().map(move |value| (group, value)))
        .collect()
}

fn expected_pairs(values: &[(usize, Value)]) -> BTreeSet<(i64, i64)> {
    values
        .iter()
        .enumerate()
        .flat_map(|(left, (left_group, _))| {
            values
                .iter()
                .enumerate()
                .filter_map(move |(right, (right_group, _))| {
                    (left_group == right_group).then_some((left as i64, right as i64))
                })
        })
        .collect()
}

fn result_pairs(result: QueryResult) -> BTreeSet<(i64, i64)> {
    let QueryResult::Relation(rows) = result else {
        panic!("expected relation")
    };
    let count = rows.len();
    let pairs: BTreeSet<_> = rows
        .into_iter()
        .map(|row| match row.as_slice() {
            [
                QueryValue::Scalar(Value::Long(left)),
                QueryValue::Scalar(Value::Long(right)),
            ] => (*left, *right),
            _ => panic!("expected two ordinal columns, got {row:?}"),
        })
        .collect();
    assert_eq!(
        count,
        pairs.len(),
        "query relation must not duplicate pairs"
    );
    pairs
}

fn pair_projection() -> FindSpec {
    FindSpec::Relation(vec![
        FindElement::Variable("left".into()),
        FindElement::Variable("right".into()),
    ])
}

#[test]
fn raw_source_and_input_hash_joins_match_independent_numeric_identity_matrix() {
    let values = join_values();
    let expected = expected_pairs(&values);
    let pattern = |source: &str, entity: &str| {
        Clause::Pattern(Box::new(DataPattern {
            source: source.into(),
            ..DataPattern::new(
                Term::var(entity),
                Term::Constant(Value::String("key".into())),
                Term::var("key"),
            )
        }))
    };
    let raw_query = Query::new(
        pair_projection(),
        vec![pattern("$left", "left"), pattern("$right", "right")],
    );
    let raw_rows: Vec<_> = values
        .iter()
        .enumerate()
        .map(|(ordinal, (_, key))| {
            vec![
                Value::Long(ordinal as i64),
                Value::String("key".into()),
                key.clone(),
            ]
        })
        .collect();
    let mut right_rows = raw_rows.clone();
    right_rows.reverse();
    let sources = [
        QueryDataSource::tuples("$left", raw_rows),
        QueryDataSource::tuples("$right", right_rows),
    ];

    let mut input_query = Query::new(pair_projection(), vec![]);
    input_query.inputs = vec![
        InputSpec::Relation(vec![Some("key".into()), Some("left".into())]),
        InputSpec::Relation(vec![Some("key".into()), Some("right".into())]),
    ];
    let input_rows: Vec<_> = values
        .iter()
        .enumerate()
        .map(|(ordinal, (_, key))| vec![key.clone(), Value::Long(ordinal as i64)])
        .collect();
    let mut right_rows = input_rows.clone();
    right_rows.reverse();
    let inputs = [
        QueryInput::Relation(input_rows),
        QueryInput::Relation(right_rows),
    ];

    for (query, query_sources, query_inputs) in [
        (&raw_query, sources.as_slice(), &[][..]),
        (&input_query, &[][..], inputs.as_slice()),
    ] {
        for table_bytes in [0, 128 * 7, 4 * 1024 * 1024] {
            let outcome = QueryEngine::execute_sources(
                query,
                query_sources,
                query_inputs,
                &QueryControl {
                    max_join_bytes: table_bytes,
                    ..Default::default()
                },
            )
            .unwrap();
            assert_eq!(
                result_pairs(outcome.result),
                expected,
                "table budget {table_bytes}"
            );
            if table_bytes == 0 {
                assert_eq!(outcome.stats.hash_join_build_rows, 0);
            } else {
                assert!(outcome.stats.hash_join_build_rows > 0);
                assert!(outcome.stats.hash_join_probes > 0);
                assert!(outcome.stats.peak_join_bytes <= table_bytes);
            }
        }
    }
}
