use atomic_core::edn::{
    EdnLimits, EdnReadOptions, EdnReader, EdnUnknownTagPolicy, EdnValue, read_edn,
    read_edn_with_options, write_edn, write_edn_with_limits,
};
use atomic_core::{ErrorCategory, Keyword, Symbol};
use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};

fn round_trip(text: &str) -> EdnValue {
    let value = read_edn(text).unwrap_or_else(|error| panic!("{text}: {error}"));
    let printed = write_edn(&value).unwrap();
    assert_eq!(read_edn(&printed).unwrap(), value, "printed {printed}");
    value
}

#[test]
fn standard_elements_round_trip_without_erasing_collection_or_number_types() {
    for text in [
        "nil",
        "true",
        "false",
        r"\newline",
        r"\return",
        r"\space",
        r"\tab",
        r"\λ",
        r"\🦀",
        r"\u03BB",
        r"\]",
        r"\;",
        r"\\",
        r#""line\nquote\" slash\\ \u03BB\uD83E\uDD80""#,
        ":person/name",
        ":name",
        "foo/bar",
        "/",
        "?e",
        "$source",
        "%",
        "...",
        "a#b:c",
        "0",
        "-0",
        "+23",
        "-9223372036854775808",
        "9223372036854775807",
        "123456789012345678901234567890N",
        "-9N",
        "-0.0",
        "1.23",
        "3e4",
        "-1E-3",
        "1M",
        "1.2500M",
        "1.01e100M",
        "1e-100M",
        "(a 1 [nil])",
        "#{a :a \"a\"}",
        "{[1 :x] (a b), nil false, #{1 2} {:nested true}}",
        r#"#uuid "F81D4FAE-7DEC-11D0-A765-00A0C91E6BF6""#,
        r#"#inst "1985-04-12T23:20:50.12345678901234567890Z""#,
        "#app/person {:name \"Alice\"}",
        "#app/outer #app/inner [1]",
    ] {
        round_trip(text);
    }
    assert!(matches!(round_trip("1N"), EdnValue::BigInt(_)));
    assert!(matches!(round_trip("1M"), EdnValue::BigDec(_)));
    assert!(matches!(round_trip("1.0"), EdnValue::Double(_)));
    assert!(matches!(round_trip("(1)"), EdnValue::List(_)));
    assert!(matches!(round_trip("[1]"), EdnValue::Vector(_)));
    let EdnValue::Double(zero) = round_trip("-0.0") else {
        panic!()
    };
    assert!(zero.is_sign_negative());
    let EdnValue::BigDec(decimal) = round_trip("1.2500M") else {
        panic!()
    };
    assert_eq!(decimal.as_bigint_and_exponent(), (BigInt::from(12500), 4));
}

#[test]
fn comments_commas_discard_and_sequential_streams_are_data_only() {
    let input = "; greeting\n1, #_ :discarded [2 #_#unknown/tag {:a 3} 4] ; tail\n{:x 5}";
    let values = EdnReader::new(input)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_eq!(
        values,
        vec![
            read_edn("1").unwrap(),
            read_edn("[2 4]").unwrap(),
            read_edn("{:x 5}").unwrap()
        ]
    );
    assert_eq!(read_edn("#_ #_ 1 2 3").unwrap(), EdnValue::Long(3));
    assert_eq!(read_edn("[1 #_ 2]").unwrap(), read_edn("[1]").unwrap());
    assert_eq!(read_edn("1 #_ 2 ;end").unwrap(), EdnValue::Long(1));
    assert!(read_edn("1 2").is_err());
    assert!(read_edn("#_ 1").is_err());
    assert!(EdnReader::new("#_1 ;empty").unwrap().next().is_none());
    let mut reader = EdnReader::new("1 [)").unwrap();
    assert_eq!(reader.next().unwrap().unwrap(), EdnValue::Long(1));
    assert!(reader.next().unwrap().is_err());
    assert!(reader.next().is_none());
    assert!(reader.next().is_none());
}

#[test]
fn native_handlers_are_explicit_nested_and_never_run_in_discarded_or_trailing_forms() {
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = calls.clone();
    let mut options = EdnReadOptions {
        unknown_tags: EdnUnknownTagPolicy::Reject,
        ..Default::default()
    };
    options.tag_handlers.insert(
        Symbol::new("test", "count"),
        Arc::new(move |value| {
            observed.fetch_add(1, Ordering::SeqCst);
            Ok(EdnValue::Vector(vec![value]))
        }),
    );
    let value =
        read_edn_with_options("[#_#test/count 1 #test/count #test/count 2]", &options).unwrap();
    assert_eq!(value, read_edn("[[[2]]]").unwrap());
    assert_eq!(calls.load(Ordering::SeqCst), 2);
    assert_eq!(
        read_edn_with_options("#_ #unregistered/x 1 2", &options).unwrap(),
        EdnValue::Long(2)
    );
    assert_eq!(
        read_edn_with_options("#_ #inst 123 2", &options).unwrap(),
        EdnValue::Long(2)
    );
    assert!(read_edn_with_options("1 #test/count 2", &options).is_err());
    assert_eq!(calls.load(Ordering::SeqCst), 2);
    assert_eq!(
        read_edn_with_options("#unregistered/x 1", &options)
            .unwrap_err()
            .code,
        "edn/unknown-tag"
    );
}

#[test]
fn edn_uniqueness_uses_precision_domains_and_unordered_recursive_collection_equality() {
    assert_ne!(read_edn("1").unwrap(), read_edn("1N").unwrap());
    assert_ne!(read_edn("1").unwrap(), read_edn("1.0").unwrap());
    assert_ne!(read_edn("1M").unwrap(), read_edn("1.0").unwrap());
    assert_eq!(read_edn("1.000M").unwrap(), read_edn("1e0M").unwrap());
    assert_eq!(
        read_edn("[1 {:a 2 :b 3}]").unwrap(),
        read_edn("(1 {:b 3 :a 2})").unwrap()
    );
    assert_eq!(
        read_edn("#{1 [2 3]}").unwrap(),
        read_edn("#{(2 3) 1}").unwrap()
    );
    round_trip("#{1 1N 1.0 1M}");
    for text in [
        "{1 :a 1 :b}",
        "#{1 1}",
        "#{[1] (1)}",
        "#{-0.0 0.0}",
        "#{1M 1.00M}",
        "{{:a 1 :b 2} 3 {:b 2 :a 1} 4}",
        "#{#{1 2} #{2 1}}",
        r#"#{#uuid "f81d4fae-7dec-11d0-a765-00a0c91e6bf6" #uuid "F81D4FAE-7DEC-11D0-A765-00A0C91E6BF6"}"#,
        r#"#{#inst "2000-01-01T00:00:00.1000Z" #inst "1999-12-31T19:00:00.1-05:00"}"#,
    ] {
        assert!(
            read_edn(text)
                .unwrap_err()
                .code
                .starts_with("edn/duplicate"),
            "{text}"
        );
    }
    let duplicate = EdnValue::Map(vec![
        (EdnValue::Long(1), EdnValue::Nil),
        (EdnValue::Long(1), EdnValue::Bool(true)),
    ]);
    assert_eq!(write_edn(&duplicate).unwrap_err().code, "edn/duplicate-key");
}

#[test]
fn instant_precision_offsets_calendar_and_leap_seconds_are_not_collapsed() {
    let precise = r#"#inst "1969-12-31T23:59:59.999999999999999999Z""#;
    assert_eq!(write_edn(&round_trip(precise)).unwrap(), precise);
    assert_eq!(
        read_edn(r#"#inst "2000-02-29T01:00:00+01:00""#).unwrap(),
        read_edn(r#"#inst "2000-02-29T00:00:00Z""#).unwrap()
    );
    assert_ne!(
        read_edn(r#"#inst "2016-12-31T23:59:60Z""#).unwrap(),
        read_edn(r#"#inst "2017-01-01T00:00:00Z""#).unwrap()
    );
    assert_eq!(
        read_edn(r#"#inst "2017-01-01T00:59:60+01:00""#).unwrap(),
        read_edn(r#"#inst "2016-12-31T23:59:60Z""#).unwrap()
    );
    for text in [
        "1900-02-29T00:00:00Z",
        "2000-13-01T00:00:00Z",
        "2000-01-00T00:00:00Z",
        "2000-01-01T24:00:00Z",
        "2000-01-01T00:00:60Z",
        "2016-12-30T23:59:60Z",
        "2000-01-01T00:00:00+24:00",
        "2000-01-01T00:00:00.Z",
        "2000-01-01",
        "",
    ] {
        assert_eq!(
            read_edn(&format!("#inst {text:?}")).unwrap_err().code,
            "edn/instant",
            "{text}"
        );
    }
}

#[test]
fn malformed_truncated_and_clojure_only_syntax_is_rejected_with_locations() {
    for text in [
        "",
        " ",
        "[",
        "(]",
        "{1}",
        "#{",
        "#",
        "#_",
        "#tag",
        "[1 #_]",
        "{1 2 3}",
        "01",
        "-01",
        "1.",
        ".1",
        "1e",
        "1e+",
        "1.0N",
        "1/2",
        "0x10",
        "2r10",
        "1_000",
        "1e999",
        "1e-999",
        "9223372036854775808",
        "::foo",
        ":/",
        ":x/1",
        "a/b/c",
        "a/",
        "/a",
        "foo//bar",
        "'x",
        "`x",
        "~x",
        "@x",
        "^:x y",
        "#=(evil)",
        "#(x)",
        "##NaN",
        "#:ns{:a 1}",
        r#""\uD800""#,
        r#""\uD800\u0041""#,
        r#""\uDC00""#,
        r#""\q""#,
        r#""unfinished"#,
        r"\uD800",
        r"\uZZZZ",
        r"\unknown",
        "\\ ",
        r#"#uuid "nope""#,
        "#uuid 1",
        "#inst nil",
    ] {
        let error = read_edn(text).unwrap_err();
        assert!(error.code.starts_with("edn/"), "{text}: {error}");
        assert!(
            error.details.contains_key("byte_offset"),
            "{text}: {error:?}"
        );
    }
    let error = read_edn("[1\n 2 }").unwrap_err();
    assert_eq!(error.details["line"], "2");
    assert!(!error.message.contains("[1"));
}

#[test]
fn exact_decimal_extreme_scale_printing_is_compact_and_preserves_stored_scale() {
    for scale in [i64::MIN, -1000, -1, 0, 1, 1000, i64::MAX] {
        let value = EdnValue::BigDec(BigDecimal::new(BigInt::from(-12300), scale));
        let text = write_edn(&value).unwrap();
        assert!(text.len() < 32, "{text}");
        let EdnValue::BigDec(reparsed) = read_edn(&text).unwrap() else {
            panic!()
        };
        assert_eq!(
            reparsed.as_bigint_and_exponent(),
            (BigInt::from(-12300), scale)
        );
    }
    assert_eq!(
        read_edn("1e9223372036854775808M").unwrap(),
        EdnValue::BigDec(BigDecimal::new(BigInt::from(1), i64::MIN))
    );
    assert!(read_edn("1e9223372036854775809M").is_err());
}

#[test]
fn finite_double_writer_round_trips_exact_bits_including_subnormals_and_signed_zero() {
    let mut bits = 0x9e3779b97f4a7c15_u64;
    let mut values = vec![
        0.0,
        -0.0,
        f64::MAX,
        f64::MIN,
        f64::MIN_POSITIVE,
        f64::from_bits(1),
        -f64::from_bits(1),
    ];
    for _ in 0..512 {
        bits = bits
            .wrapping_mul(6364136223846793005)
            .wrapping_add(1442695040888963407);
        let value = f64::from_bits(bits);
        if value.is_finite() {
            values.push(value);
        }
    }
    for expected in values {
        let text = write_edn(&EdnValue::Double(expected)).unwrap();
        let EdnValue::Double(actual) = read_edn(&text).unwrap() else {
            panic!("{text}")
        };
        assert_eq!(actual.to_bits(), expected.to_bits(), "{text}");
    }
}

#[test]
fn input_output_token_node_depth_and_stream_work_admission_fail_safely() {
    let opts = |limits| EdnReadOptions {
        limits,
        ..Default::default()
    };
    let small = EdnLimits {
        max_input_bytes: 3,
        ..Default::default()
    };
    assert_eq!(
        read_edn_with_options("true", &opts(small))
            .unwrap_err()
            .category,
        ErrorCategory::Busy
    );
    let small = EdnLimits {
        max_token_bytes: 3,
        ..Default::default()
    };
    assert_eq!(
        read_edn_with_options("1", &opts(small)).unwrap(),
        EdnValue::Long(1)
    );
    assert_eq!(
        write_edn_with_limits(&EdnValue::Long(1), small).unwrap(),
        "1"
    );
    assert!(read_edn_with_options("\"abcd\"", &opts(small)).is_err());
    assert!(read_edn_with_options("abcd", &opts(small)).is_err());
    let small = EdnLimits {
        max_nodes: 2,
        ..Default::default()
    };
    assert!(read_edn_with_options("[1 2]", &opts(small)).is_err());
    let small = EdnLimits {
        max_depth: 2,
        ..Default::default()
    };
    assert!(read_edn_with_options("[[[1]]]", &opts(small)).is_err());
    let deep = format!("{}0{}", "[".repeat(10000), "]".repeat(10000));
    assert_eq!(read_edn(&deep).unwrap_err().code, "edn/capacity");
    assert!(read_edn(&"#_".repeat(10000)).is_err());
    assert!(read_edn("nil").is_ok());
    let value = read_edn("[1 2]").unwrap();
    assert!(
        write_edn_with_limits(
            &value,
            EdnLimits {
                max_output_bytes: 4,
                ..Default::default()
            }
        )
        .is_err()
    );
    assert!(
        write_edn_with_limits(
            &value,
            EdnLimits {
                max_nodes: 2,
                ..Default::default()
            }
        )
        .is_err()
    );
    let mut reader = EdnReader::with_options(
        "1 2 3",
        &opts(EdnLimits {
            max_nodes: 2,
            ..Default::default()
        }),
    )
    .unwrap();
    assert!(reader.next().unwrap().is_ok());
    assert!(reader.next().unwrap().is_ok());
    assert!(reader.next().unwrap().is_err());
    assert!(reader.next().is_none());
    assert!(
        read_edn_with_options(
            ";long comment\n1",
            &opts(EdnLimits {
                max_work: 4,
                ..Default::default()
            })
        )
        .is_err()
    );
}

#[test]
fn writer_rejects_invalid_constructed_values_and_keeps_map_order() {
    for value in [
        EdnValue::Double(f64::NAN),
        EdnValue::Double(f64::INFINITY),
        EdnValue::Keyword(Keyword::unqualified(":")),
        EdnValue::Symbol(Symbol::unqualified("nil")),
        EdnValue::Tagged(Symbol::unqualified("1bad"), Box::new(EdnValue::Nil)),
        EdnValue::Tagged(Symbol::unqualified("inst"), Box::new(EdnValue::Long(0))),
    ] {
        assert!(write_edn(&value).is_err());
    }
    assert_eq!(
        write_edn(&read_edn("{:z 1 :a 2}").unwrap()).unwrap(),
        "{:z 1 :a 2}"
    );
}

#[test]
fn handler_results_are_revalidated_and_deep_failure_cleanup_is_iterative() {
    let mut options = EdnReadOptions::default();
    options.tag_handlers.insert(
        Symbol::new("test", "deep"),
        Arc::new(|_| {
            let mut value = EdnValue::Nil;
            for _ in 0..20000 {
                value = EdnValue::Vector(vec![value]);
            }
            Ok(value)
        }),
    );
    assert_eq!(
        read_edn_with_options("#test/deep nil", &options)
            .unwrap_err()
            .code,
        "edn/capacity"
    );
    options.tag_handlers.insert(
        Symbol::new("test", "duplicate"),
        Arc::new(|_| Ok(EdnValue::Set(vec![EdnValue::Long(1), EdnValue::Long(1)]))),
    );
    assert_eq!(
        read_edn_with_options("#test/duplicate nil", &options)
            .unwrap_err()
            .code,
        "edn/duplicate-element"
    );
}

#[test]
fn large_text_escaped_text_and_handler_payloads_use_text_not_numeric_limits() {
    let value = EdnValue::String("x".repeat(1024 * 1024));
    let output = write_edn(&value).unwrap();
    assert_eq!(read_edn(&output).unwrap(), value);
    let escaped = EdnValue::String("\0".repeat(64 * 1024));
    let output = write_edn(&escaped).unwrap();
    assert_eq!(read_edn(&output).unwrap(), escaped);
    let limits = EdnLimits {
        max_numeric_bytes: 4,
        ..Default::default()
    };
    assert!(write_edn_with_limits(&EdnValue::BigInt(BigInt::from(12345)), limits).is_err());
    assert!(
        read_edn_with_options(
            "12345N",
            &EdnReadOptions {
                limits,
                ..Default::default()
            }
        )
        .is_err()
    );
    let options = EdnReadOptions {
        limits: EdnLimits {
            max_token_bytes: 8,
            ..Default::default()
        },
        ..Default::default()
    };
    let mut handlers = options.clone();
    handlers.tag_handlers.insert(
        Symbol::new("t", "s"),
        Arc::new(|_| Ok(EdnValue::String("x".repeat(9)))),
    );
    assert_eq!(
        read_edn_with_options("#t/s nil", &handlers)
            .unwrap_err()
            .code,
        "edn/capacity"
    );
    let builtin = EdnValue::Tagged(
        Symbol::unqualified("uuid"),
        Box::new(EdnValue::String(
            "f81d4fae-7dec-11d0-a765-00a0c91e6bf6".into(),
        )),
    );
    assert!(
        write_edn_with_limits(
            &builtin,
            EdnLimits {
                max_nodes: 1,
                ..Default::default()
            }
        )
        .is_err()
    );
}

#[test]
fn arbitrary_key_sorting_is_work_bounded_even_for_long_common_prefixes() {
    let prefix = "x".repeat(1024);
    let map = EdnValue::Map(
        (0..64)
            .rev()
            .map(|i| {
                (
                    EdnValue::String(format!("{prefix}{i:04}")),
                    EdnValue::Long(i),
                )
            })
            .collect(),
    );
    let input = write_edn(&map).unwrap();
    let limited = EdnReadOptions {
        limits: EdnLimits {
            max_work: 512 * 1024,
            ..Default::default()
        },
        ..Default::default()
    };
    assert_eq!(
        read_edn_with_options(&input, &limited).unwrap_err().code,
        "edn/capacity"
    );
    assert_eq!(
        write_edn_with_limits(&map, limited.limits)
            .unwrap_err()
            .code,
        "edn/capacity"
    );
    let EdnValue::Map(reparsed) = read_edn(&input).unwrap() else {
        panic!()
    };
    assert_eq!(reparsed.len(), 64);
    for (ordinal, (key, value)) in reparsed.iter().enumerate() {
        let expected = 63 - ordinal;
        assert_eq!(key, &EdnValue::String(format!("{prefix}{expected:04}")));
        assert_eq!(value, &EdnValue::Long(expected as i64));
    }
}
