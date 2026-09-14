use atomic_core::*;
use std::sync::atomic::Ordering;

#[test]
fn prepared_rule_analysis_is_reused_only_after_success_and_controls_still_apply() {
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("x".into())]),
        vec![Clause::Rule {
            source: "$".into(),
            name: "answer".into(),
            args: vec![Term::var("x")],
        }],
    );
    query.rules.push(Rule {
        name: "answer".into(),
        head: vec!["x".into()],
        required: Default::default(),
        clauses: vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![Term::Constant(Value::Long(7))],
            binding: Binding::Scalar("x".into()),
        }],
    });
    let prepared = PreparedQuery::new(&query).unwrap();
    let limited = QueryControl {
        max_work: 1,
        ..QueryControl::default()
    };
    assert_eq!(
        prepared.execute(&[], &[], &limited).unwrap_err().code,
        "query/work-limit"
    );
    let cold = prepared
        .execute(&[], &[], &QueryControl::default())
        .unwrap();
    let warm = prepared
        .clone()
        .execute(&[], &[], &QueryControl::default())
        .unwrap();
    assert_eq!(
        cold.result,
        QueryResult::Relation(vec![vec![QueryValue::Scalar(Value::Long(7))]])
    );
    assert_eq!(warm.result, cold.result);
    assert!(
        warm.stats.work < cold.stats.work,
        "warm execution must reuse rule analysis"
    );
    let canceled = QueryControl::default();
    canceled.cancel.store(true, Ordering::Relaxed);
    assert_eq!(
        prepared.execute(&[], &[], &canceled).unwrap_err().code,
        "query/canceled"
    );
}
