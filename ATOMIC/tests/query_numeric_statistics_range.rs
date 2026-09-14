//! Exact statistical inputs may have an unrepresentable intermediate delta even
//! when the requested approximate output is finite. Retain repeated observations
//! with :with; otherwise a set-valued query would change the sample weights.
use atomic_core::*;

fn decimal(text: &str) -> Value {
    Value::BigDec(text.parse().unwrap())
}

fn aggregate(function: Aggregate, values: Vec<Value>) -> Result<f64, SemanticError> {
    let mut query = Query::new(
        FindSpec::Scalar(FindElement::Aggregate {
            function,
            variable: "value".into(),
        }),
        vec![],
    );
    query.inputs = vec![InputSpec::Relation(vec![
        Some("observation".into()),
        Some("value".into()),
    ])];
    query.with = vec!["observation".into()];
    let input = QueryInput::Relation(
        values
            .into_iter()
            .enumerate()
            .map(|(index, value)| vec![Value::Long(index as i64), value])
            .collect(),
    );
    let outcome =
        PreparedQuery::new(&query)
            .unwrap()
            .execute(&[], &[input], &QueryControl::default())?;
    match outcome.result {
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Double(value)))) => Ok(value),
        other => panic!("approximate statistical Double expected, got {other:?}"),
    }
}

fn close(actual: f64, expected: f64) {
    assert!(actual.is_finite(), "nonfinite result {actual}");
    assert!(
        (actual / expected - 1.0).abs() <= 8.0 * f64::EPSILON,
        "actual={actual:e}, expected={expected:e}"
    );
}

#[test]
fn standard_deviation_survives_an_unrepresentable_exact_centered_delta() {
    for observations in [
        vec![decimal("-1e308"), decimal("1e308")],
        vec![decimal("3e308"), decimal("5e308")],
    ] {
        let result = aggregate(Aggregate::StandardDeviation, observations);
        eprintln!("exact pair expected stddev=1e308; actual={result:?}");
        close(result.unwrap(), 1e308);
    }
}

#[test]
fn standard_deviation_weights_an_out_of_range_exact_outlier_before_conversion() {
    let mut observations = vec![decimal("0"); 999];
    observations.push(decimal("1e309"));
    // Population stddev for N-1 zeroes and one x is |x| * sqrt(N-1) / N.
    // Evaluate the independently known result without overflowing the test.
    let expected = 999.0_f64.sqrt() * 1e306;
    let result = aggregate(Aggregate::StandardDeviation, observations);
    eprintln!("1000 observations expected stddev={expected:e}; actual={result:?}");
    close(result.unwrap(), expected);
}

#[test]
fn genuinely_out_of_range_variance_is_still_a_checked_error() {
    let error = aggregate(
        Aggregate::Variance,
        vec![decimal("-1e308"), decimal("1e308")],
    )
    .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Incorrect);
    assert_eq!(error.code, "query/arithmetic-range");
}

#[test]
fn tiny_normalized_contributions_do_not_invalidate_a_representable_statistic() {
    for (largest, factor) in [("1", 1.0), ("1e308", 1e308)] {
        // 1e-400 cannot itself be a nonzero f64, but its contribution is far
        // below the precision of this explicitly approximate result.
        let observations = vec![decimal("0"), decimal("1e-400"), decimal(largest)];
        close(
            aggregate(Aggregate::StandardDeviation, observations).unwrap(),
            (2.0_f64.sqrt() / 3.0) * factor,
        );
    }
    close(
        aggregate(
            Aggregate::Variance,
            vec![decimal("0"), decimal("1e-400"), decimal("1")],
        )
        .unwrap(),
        2.0 / 9.0,
    );
}

#[test]
fn final_underflow_and_explicit_float_conversion_remain_checked() {
    for observations in [
        vec![decimal("0"), decimal("1e-400")],
        // Explicit floating inputs still choose checked floating conversion;
        // internal exact-input normalization is not a change to that contract.
        vec![Value::Double(0.0), decimal("1e-400"), Value::Double(1.0)],
    ] {
        let error = aggregate(Aggregate::StandardDeviation, observations).unwrap_err();
        assert_eq!(error.category, ErrorCategory::Incorrect);
        assert_eq!(error.code, "query/arithmetic-range");
    }
}
