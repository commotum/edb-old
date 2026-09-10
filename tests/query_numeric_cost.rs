//! Complete query + result-drop samples. Input construction is outside the
//! measured application operation; counts are semantic work, not process RSS.
use atomic_core::*;
use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use std::time::Instant;

#[test]
fn compact_exponents_do_not_expand_during_complete_hash_join_queries() {
    let mut query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable("left".into()),
            FindElement::Variable("right".into()),
        ]),
        vec![],
    );
    query.inputs = vec![
        InputSpec::Relation(vec![Some("key".into()), Some("left".into())]),
        InputSpec::Relation(vec![Some("key".into()), Some("right".into())]),
    ];
    let prepared = PreparedQuery::new(&query).unwrap();
    for count in [32_i64, 128, 512] {
        let mut expected_work = None;
        for scale in [0_i64, 1_000, 100_000, i64::MAX - 1, i64::MIN] {
            let inputs: Vec<_> = [false, true]
                .into_iter()
                .map(|alternate| {
                    QueryInput::Relation(
                        (0..count)
                            .map(|n| {
                                let coefficient = (n + 1) * if alternate { 10 } else { 1 };
                                vec![
                                    Value::BigDec(BigDecimal::new(
                                        BigInt::from(coefficient),
                                        scale + i64::from(alternate),
                                    )),
                                    Value::Long(n),
                                ]
                            })
                            .collect(),
                    )
                })
                .collect();
            let started = Instant::now();
            let outcome = prepared
                .execute(&[], &inputs, &QueryControl::default())
                .unwrap();
            let QueryResult::Relation(rows) = &outcome.result else {
                panic!("relation expected")
            };
            assert_eq!(rows.len(), count as usize);
            for (n, row) in rows.iter().enumerate() {
                assert_eq!(row, &vec![QueryValue::Scalar(Value::Long(n as i64)); 2]);
            }
            assert_eq!(outcome.stats.hash_join_build_rows, count as u64);
            assert_eq!(outcome.stats.hash_join_probes, count as u64);
            assert_eq!(outcome.stats.join_candidates, count as u64 * 2);
            let work = outcome.stats.work;
            let bytes = outcome.stats.allocated_value_bytes;
            if let Some(expected) = expected_work {
                assert_eq!(work, expected);
            }
            expected_work = Some(work);
            drop(outcome);
            let elapsed = started.elapsed();
            eprintln!(
                "NUMERIC_QUERY_COST rows={count} coefficient_digits<=4 scale={scale} work={work} accounted_allocations={bytes} query_check_drop_us={}",
                elapsed.as_micros()
            );
        }
    }
}
