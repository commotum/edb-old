use atomic_core::{
    Database, FindElement, FindSpec, InputSpec, Query, QueryControl, QueryEngine, QueryInput,
    QueryResult, QuerySource, QueryValue, Schema, Value, Variable,
};

#[test]
fn deep_results_clone_compare_and_drop_on_a_small_stack() {
    std::thread::Builder::new()
        .stack_size(256 * 1024)
        .spawn(|| {
            let mut result = QueryValue::Nil;
            for _ in 0..10_000 {
                result = QueryValue::Map(vec![(
                    QueryValue::Scalar(Value::String("next".into())),
                    QueryValue::Collection(vec![result]),
                )]);
            }
            let clone = result.clone();
            assert!(result == clone);
            drop(result);
            drop(clone);
        })
        .unwrap()
        .join()
        .unwrap();
}

#[test]
fn default_query_accepts_more_than_the_former_result_ceiling() {
    let value = Variable::new("value").unwrap();
    let mut query = Query::new(
        FindSpec::Collection(FindElement::Variable(value.clone())),
        vec![],
    );
    query.inputs = vec![InputSpec::Collection(value)];
    let database = Database::new(Schema::new()).unwrap().database_value();
    let sources = [QuerySource {
        name: "$".into(),
        database,
    }];
    let inputs = [QueryInput::Collection(
        (0..100_005).map(Value::Long).collect(),
    )];
    let start = std::time::Instant::now();
    let outcome =
        QueryEngine::execute(&query, &sources, &inputs, &QueryControl::default()).unwrap();
    let QueryResult::Collection(values) = outcome.result else {
        panic!("collection expected")
    };
    assert_eq!(values.len(), 100_005);
    eprintln!(
        "query default: {} projected input rows in {:?}, {} rows produced",
        values.len(),
        start.elapsed(),
        outcome.stats.rows_produced
    );
    let explicit = QueryControl {
        max_result_rows: 100_000,
        ..QueryControl::default()
    };
    let error = QueryEngine::execute(&query, &sources, &inputs, &explicit).unwrap_err();
    assert_eq!(error.category, atomic_core::ErrorCategory::Busy);
    assert_eq!(QueryControl::default().max_work, usize::MAX);
    assert_eq!(QueryControl::default().max_intermediate_rows, usize::MAX);
}
