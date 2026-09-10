use atomic_core::edn::{read_edn, write_edn};
use atomic_core::edn_value::{edn_to_value, query_result_to_edn, value_to_edn};
use atomic_core::{Keyword, QueryResult, QueryValue, Symbol, Value};

#[test]
fn stored_types_roundtrip_without_rewriting_representations() {
    let values = vec![
        Value::Bool(true),
        Value::Long(i64::MIN),
        Value::Long(i64::MAX),
        Value::BigInt("9223372036854775808000000001".parse().unwrap()),
        Value::BigDec("1.2300".parse().unwrap()),
        Value::BigDec("1e-9223372036854775807".parse().unwrap()),
        Value::Double(-0.0),
        Value::Double(1.2345678901234567),
        Value::Double(f64::from_bits(0x7ff8_0000_0000_0001)),
        Value::Double(f64::INFINITY),
        Value::Float(1.25),
        Value::Float(-0.0),
        Value::Float(f32::from_bits(0x7fc0_0001)),
        Value::String("hello \"世界\"\n".into()),
        Value::Keyword(Keyword::new("person", "email")),
        Value::Symbol(Symbol::new("app", "name")),
        Value::Ref(atomic_core::MAX_EID),
        Value::Bytes(vec![0, 1, 127, 128, 255]),
        Value::Function([0xab; 32]),
        Value::Uri("https://example.com/é".into()),
        Value::Uuid(0xf81d4fae7dec11d0a76500a0c91e6bf6),
        Value::Instant(-1),
        Value::Instant(0),
        Value::Instant(1),
        Value::Instant(i64::MIN),
        Value::Instant(i64::MAX),
        Value::Tuple(vec![Some(Value::Long(1)), None, Some(Value::Ref(42))]),
    ];
    for value in values {
        let text = write_edn(&value_to_edn(&value).unwrap()).unwrap();
        let recovered = edn_to_value(&read_edn(&text).unwrap()).unwrap();
        match (&value, &recovered) {
            (Value::Double(a), Value::Double(b)) => assert_eq!(a.to_bits(), b.to_bits()),
            (Value::Float(a), Value::Float(b)) => assert_eq!(a.to_bits(), b.to_bits()),
            (Value::BigDec(a), Value::BigDec(b)) => {
                assert_eq!(a.as_bigint_and_exponent(), b.as_bigint_and_exponent())
            }
            _ => assert_eq!(value, recovered, "{text}"),
        }
        assert_eq!(
            std::mem::discriminant(&value),
            std::mem::discriminant(&recovered)
        );
    }
}

#[test]
fn reading_data_does_not_silently_expand_the_stored_value_domain() {
    let distinct_edn_keys = read_edn("{1 :long 1N :bigint}").unwrap();
    assert_eq!(
        atomic_core::edn_value::edn_to_query_value(&distinct_edn_keys)
            .unwrap_err()
            .code,
        "edn/query-key-collision"
    );
    for text in ["nil", "\\a", "{:a 1}", "#{1 2}", "#custom/item 1", "[]"] {
        let parsed = read_edn(text).unwrap();
        assert!(edn_to_value(&parsed).is_err(), "{text}");
    }
    for text in [
        "#inst \"1970-01-01T00:00:00.0001Z\"",
        "#inst \"2016-12-31T23:59:60Z\"",
    ] {
        assert_eq!(
            edn_to_value(&read_edn(text).unwrap()).unwrap_err().code,
            "edn/instant-precision"
        );
    }
    for (text, expected) in [
        ("#inst \"1970-01-01T01:00:00.001000+01:00\"", 1),
        ("#inst \"1969-12-31T23:59:59.999Z\"", -1),
        ("#inst \"2000-02-29T00:00:00Z\"", 951782400000),
    ] {
        assert_eq!(
            edn_to_value(&read_edn(text).unwrap()).unwrap(),
            Value::Instant(expected)
        );
    }
}

#[test]
fn exact_number_format_costs_depend_on_coefficients_not_virtual_scale() {
    for digits in [64, 256, 1024, 4096] {
        for scale in [0i64, 1_000_000, i64::MAX] {
            let text = format!("{}e-{scale}M", "7".repeat(digits));
            let start = std::time::Instant::now();
            let parsed = read_edn(&text).unwrap();
            let parse_us = start.elapsed().as_micros();
            let start = std::time::Instant::now();
            let value = edn_to_value(&parsed).unwrap();
            let printed = write_edn(&value_to_edn(&value).unwrap()).unwrap();
            let convert_print_us = start.elapsed().as_micros();
            let recovered = edn_to_value(&read_edn(&printed).unwrap()).unwrap();
            let (Value::BigDec(before), Value::BigDec(after)) = (&value, recovered) else {
                panic!("exact decimal type lost")
            };
            assert_eq!(
                before.as_bigint_and_exponent(),
                after.as_bigint_and_exponent()
            );
            assert!(
                printed.len() <= digits + 30,
                "virtual decimal scale was expanded"
            );
            println!(
                "EDN_NUMERIC_COST coefficient_digits={digits} scale={scale} input_bytes={} output_bytes={} parse_us={parse_us} convert_print_us={convert_print_us}",
                text.len(),
                printed.len()
            );
        }
    }
}

#[test]
fn result_shapes_and_nested_pull_maps_are_readable_data() {
    let map = QueryValue::Map(vec![(
        QueryValue::Scalar(Value::Keyword(Keyword::new("person", "name"))),
        QueryValue::Scalar(Value::String("Alice".into())),
    )]);
    for result in [
        QueryResult::Relation(vec![vec![map.clone()]]),
        QueryResult::Tuple(Some(vec![map.clone()])),
        QueryResult::Collection(vec![map.clone()]),
        QueryResult::Scalar(Some(map)),
        QueryResult::Tuple(None),
        QueryResult::Scalar(None),
    ] {
        let value = query_result_to_edn(&result).unwrap();
        let text = write_edn(&value).unwrap();
        assert_eq!(read_edn(&text).unwrap(), value);
        if matches!(result, QueryResult::Relation(_)) {
            assert!(text.starts_with("#{"));
        }
    }
}
