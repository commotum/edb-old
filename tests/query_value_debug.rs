use atomic_core::{QueryValue, Value};
use std::fmt;

#[test]
fn query_value_debug_preserves_familiar_variants_and_map_insertion_order() {
    for (value, expected) in [
        (QueryValue::Nil, "Nil"),
        (QueryValue::Scalar(Value::Long(7)), "Scalar(Long(7))"),
        (
            QueryValue::Collection(vec![QueryValue::Nil, QueryValue::Scalar(Value::Long(1))]),
            "Collection([Nil, Scalar(Long(1))])",
        ),
        (
            QueryValue::Tuple(vec![QueryValue::Nil, QueryValue::Collection(vec![])]),
            "Tuple([Nil, Collection([])])",
        ),
        (
            QueryValue::Map(vec![
                (QueryValue::Scalar(Value::Long(2)), QueryValue::Nil),
                (
                    QueryValue::Scalar(Value::Long(1)),
                    QueryValue::Tuple(vec![]),
                ),
            ]),
            "Map([(Scalar(Long(2)), Nil), (Scalar(Long(1)), Tuple([]))])",
        ),
    ] {
        assert_eq!(format!("{value:?}"), expected);
        // Formatting is intentionally compact even when a caller asks for
        // alternate debug; diagnostics do not need a recursive pretty printer.
        assert_eq!(format!("{value:#?}"), expected);
    }
}

#[test]
fn ten_thousand_nested_result_containers_format_and_drop_on_a_small_stack() {
    for kind in ["map", "collection", "tuple", "mixed"] {
        std::thread::Builder::new()
            .name(format!("query-debug-{kind}"))
            .stack_size(256 * 1024)
            .spawn(move || {
                let mut value = QueryValue::Scalar(Value::Long(42));
                for index in 0..10_000 {
                    value = match kind {
                        "map" => QueryValue::Map(vec![(QueryValue::Nil, value)]),
                        "collection" => QueryValue::Collection(vec![value]),
                        "tuple" => QueryValue::Tuple(vec![value]),
                        _ if index % 2 == 0 => QueryValue::Map(vec![(value, QueryValue::Nil)]),
                        _ => QueryValue::Collection(vec![value]),
                    };
                }
                let compact = format!("{value:?}");
                assert!(compact.contains("Scalar(Long(42))"));
                assert!(compact.len() > 90_000);
                let alternate = format!("{value:#?}");
                assert_eq!(compact, alternate);
                // Both rendered strings and the actual deep QueryValue are
                // destroyed normally inside this small-stack thread.
            })
            .unwrap()
            .join()
            .unwrap();
    }
}

#[test]
fn formatting_propagates_writer_errors_without_visiting_the_remaining_tree() {
    struct LimitedWriter {
        remaining_calls: usize,
        calls: usize,
    }
    impl fmt::Write for LimitedWriter {
        fn write_str(&mut self, _: &str) -> fmt::Result {
            self.calls += 1;
            if self.remaining_calls == 0 {
                return Err(fmt::Error);
            }
            self.remaining_calls -= 1;
            Ok(())
        }
    }
    let value = QueryValue::Map(vec![
        (
            QueryValue::Collection(vec![QueryValue::Nil]),
            QueryValue::Tuple(vec![QueryValue::Nil]),
        ),
        (
            QueryValue::Nil,
            QueryValue::Collection(vec![QueryValue::Nil; 1_000]),
        ),
    ]);
    let mut writer = LimitedWriter {
        remaining_calls: 2,
        calls: 0,
    };
    assert!(fmt::write(&mut writer, format_args!("{value:?}")).is_err());
    assert_eq!(writer.calls, 3);
}

#[test]
fn canonical_maps_preserve_unordered_keys_and_duplicate_key_value_ordering() {
    use std::cmp::Ordering;
    // A deliberately recursive, shallow oracle captures the pre-hardening
    // semantics independently of the production arena representation.
    fn oracle(left: &QueryValue, right: &QueryValue) -> Ordering {
        fn rank(value: &QueryValue) -> u8 {
            match value {
                QueryValue::Nil => 0,
                QueryValue::Scalar(_) => 1,
                QueryValue::Tuple(_) => 2,
                QueryValue::Collection(_) => 3,
                QueryValue::Map(_) => 4,
                QueryValue::Set(_) | QueryValue::Char(_) | QueryValue::Tagged(_, _) => {
                    unreachable!("historical oracle covers only the original value variants")
                }
            }
        }
        let order = rank(left).cmp(&rank(right));
        if order != Ordering::Equal {
            return order;
        }
        match (left, right) {
            (QueryValue::Nil, QueryValue::Nil) => Ordering::Equal,
            (QueryValue::Scalar(left), QueryValue::Scalar(right)) => left.index_cmp(right),
            (QueryValue::Tuple(left), QueryValue::Tuple(right))
            | (QueryValue::Collection(left), QueryValue::Collection(right)) => left
                .iter()
                .zip(right)
                .map(|(left, right)| oracle(left, right))
                .find(|order| *order != Ordering::Equal)
                .unwrap_or_else(|| left.len().cmp(&right.len())),
            (QueryValue::Map(left), QueryValue::Map(right)) => {
                let mut left = left.iter().collect::<Vec<_>>();
                let mut right = right.iter().collect::<Vec<_>>();
                let compare =
                    |(left_key, left_value): &&(QueryValue, QueryValue),
                     (right_key, right_value): &&(QueryValue, QueryValue)| {
                        oracle(left_key, right_key).then_with(|| oracle(left_value, right_value))
                    };
                left.sort_by(compare);
                right.sort_by(compare);
                left.iter()
                    .zip(&right)
                    .map(|(left, right)| compare(left, right))
                    .find(|order| *order != Ordering::Equal)
                    .unwrap_or_else(|| left.len().cmp(&right.len()))
            }
            _ => unreachable!(),
        }
    }
    let nested = QueryValue::Map(vec![
        (
            QueryValue::Scalar(Value::Long(2)),
            QueryValue::Collection(vec![QueryValue::Nil]),
        ),
        (QueryValue::Scalar(Value::Long(1)), QueryValue::Nil),
    ]);
    let mut reversed_entries = nested.clone().into_map().unwrap();
    reversed_entries.reverse();
    let reversed = QueryValue::Map(reversed_entries);
    let duplicates = QueryValue::Map(vec![
        (nested.clone(), QueryValue::Scalar(Value::Long(2))),
        (reversed.clone(), QueryValue::Scalar(Value::Long(1))),
        (QueryValue::Scalar(Value::Long(1)), QueryValue::Nil),
        (
            QueryValue::Scalar(Value::Double(1.0)),
            QueryValue::Collection(vec![]),
        ),
    ]);
    let mut reverse_duplicates = duplicates.clone().into_map().unwrap();
    reverse_duplicates.reverse();
    let reverse_duplicates = QueryValue::Map(reverse_duplicates);
    assert_eq!(duplicates, reverse_duplicates);
    let values = [
        QueryValue::Nil,
        QueryValue::Scalar(Value::Long(1)),
        QueryValue::Scalar(Value::Double(1.0)),
        QueryValue::Scalar(Value::Double(f64::NAN)),
        QueryValue::Tuple(vec![QueryValue::Nil]),
        QueryValue::Collection(vec![QueryValue::Nil]),
        QueryValue::Collection(vec![nested.clone(), reversed.clone()]),
        QueryValue::Map(vec![]),
        nested,
        reversed,
        duplicates,
        reverse_duplicates,
    ];
    for left in &values {
        for right in &values {
            assert_eq!(left.canonical_cmp(right), oracle(left, right));
        }
    }
}

#[test]
fn pathological_nested_map_key_comparison_is_stack_safe() {
    const PROBE: &str = "ATOMIC_QUERY_MAP_KEY_STACK_PROBE";
    if std::env::var_os(PROBE).is_some() {
        std::thread::Builder::new()
            .name("query-map-key-comparison".into())
            .stack_size(256 * 1024)
            .spawn(|| {
                for recurse_through_key in [true, false] {
                    let mut value = QueryValue::Map(vec![(QueryValue::Nil, QueryValue::Nil)]);
                    for _ in 0..1_000 {
                        let shallow = QueryValue::Map(vec![(QueryValue::Nil, QueryValue::Nil)]);
                        value = if recurse_through_key {
                            QueryValue::Map(vec![
                                (value, QueryValue::Nil),
                                (shallow, QueryValue::Nil),
                            ])
                        } else {
                            QueryValue::Map(vec![
                                (QueryValue::Nil, value),
                                (QueryValue::Nil, shallow),
                            ])
                        };
                    }
                    assert!(value.canonical_cmp(&QueryValue::Map(vec![])).is_gt());
                    assert_eq!(
                        value.canonical_cmp(&value.clone()),
                        std::cmp::Ordering::Equal
                    );
                }
            })
            .unwrap()
            .join()
            .unwrap();
        return;
    }
    // A comparator regression must fail this test, not abort the complete
    // test process. The child runs exactly this one probe and inherits no
    // special handling of QueryValue destruction.
    let output = std::process::Command::new(std::env::current_exe().unwrap())
        .args([
            "--exact",
            "pathological_nested_map_key_comparison_is_stack_safe",
            "--nocapture",
        ])
        .env(PROBE, "1")
        .output()
        .unwrap();
    assert!(
        output.status.success(),
        "map-key comparison probe failed: {}",
        String::from_utf8_lossy(&output.stderr)
    );
}
