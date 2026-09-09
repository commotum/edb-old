use atomic_core::{
    Aggregate, Attribute, AttributeName, Binding, Cardinality, Clause, DataPattern, Database,
    EntityRef, FindElement, FindSpec, Function, InputSpec, Keyword, PullAttribute, PullPattern,
    PullTransform, Query, QueryControl, QueryEngine, QueryInput, QueryResult, QuerySource,
    QueryValue, Schema, Term, TxOp, Value, ValueType, Variable,
};
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};

const SCORE: u32 = 1000;
fn variable(name: &str) -> Variable {
    name.into()
}
fn fixture() -> (Database, Vec<u64>) {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            SCORE,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let report = Database::new(schema)
        .unwrap()
        .with(
            &(0..3)
                .map(|i| TxOp::Add {
                    entity: EntityRef::Temp(format!("e{i}")),
                    attribute: SCORE,
                    value: Value::Long(10 + i).into(),
                })
                .collect::<Vec<_>>(),
            1000,
        )
        .unwrap();
    let entities = (0..3).map(|i| report.tempids[&format!("e{i}")]).collect();
    (report.db_after, entities)
}
fn pattern() -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(
        Term::var("e"),
        Term::Constant(Value::Long(SCORE.into())),
        Term::var("score"),
    )))
}

#[test]
fn nested_min_uses_exact_named_source_and_can_join_outer_patterns() {
    let (old, entities) = fixture();
    let newer = old
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entities[0]),
                attribute: SCORE,
                value: Value::Long(50).into(),
            }],
            2000,
        )
        .unwrap()
        .db_after;
    let inner = Query::new(
        FindSpec::Scalar(FindElement::Aggregate {
            function: Aggregate::Min,
            variable: variable("score"),
        }),
        vec![pattern()],
    );
    let outer = Query::new(
        FindSpec::Relation(vec![FindElement::Variable(variable("score"))]),
        vec![Clause::Function {
            function: Function::Query(Box::new(inner)),
            source: "$selected".into(),
            args: vec![],
            binding: Binding::Scalar(variable("score")),
        }],
    );
    for (selected, expected) in [
        (old.database_value(), 10),
        (newer.database_value(), 11),
        (newer.database_value().as_of(old.basis_t()), 10),
    ] {
        let outcome = QueryEngine::execute(
            &outer,
            &[
                QuerySource {
                    name: "$".into(),
                    database: newer.database_value(),
                },
                QuerySource {
                    name: "$selected".into(),
                    database: selected,
                },
            ],
            &[],
            &QueryControl::default(),
        )
        .unwrap();
        assert_eq!(
            outcome.result,
            QueryResult::Relation(vec![vec![QueryValue::Scalar(Value::Long(expected))]])
        );
        assert!(
            outcome
                .plan
                .iter()
                .any(|step| step.clause.starts_with("nested/"))
        );
    }
    let mut joined = outer.clone();
    joined.clauses.push(pattern());
    let outcome = QueryEngine::execute(
        &joined,
        &[
            QuerySource {
                name: "$".into(),
                database: old.database_value(),
            },
            QuerySource {
                name: "$selected".into(),
                database: old.database_value(),
            },
        ],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    assert_eq!(
        outcome.result,
        QueryResult::Relation(vec![vec![QueryValue::Scalar(Value::Long(10))]])
    );
}

#[test]
fn nested_calls_share_work_and_initial_inputs_obey_explicit_limits() {
    let database = Database::new(Schema::new()).unwrap();
    let mut inner = Query::new(
        FindSpec::Scalar(FindElement::Variable(variable("x"))),
        vec![],
    );
    inner.inputs = vec![InputSpec::Scalar(variable("x"))];
    let mut query = Query::new(
        FindSpec::Collection(FindElement::Variable(variable("y"))),
        vec![Clause::Function {
            function: Function::Query(Box::new(inner)),
            source: "$".into(),
            args: vec![Term::var("x")],
            binding: Binding::Scalar(variable("y")),
        }],
    );
    query.inputs = vec![InputSpec::Collection(variable("x"))];
    let inputs = [QueryInput::Collection((0..100).map(Value::Long).collect())];
    let result = database
        .query(&query, &inputs, &QueryControl::default())
        .unwrap();
    assert!(result.stats.work > 500);
    let limit = QueryControl {
        max_work: result.stats.work as usize - 1,
        ..QueryControl::default()
    };
    assert_eq!(
        database.query(&query, &inputs, &limit).unwrap_err().code,
        "query/work-limit"
    );
    let limit = QueryControl {
        max_intermediate_rows: 99,
        ..QueryControl::default()
    };
    assert_eq!(
        database.query(&query, &inputs, &limit).unwrap_err().code,
        "query/intermediate-limit"
    );
    let limit = QueryControl {
        max_work: 99,
        ..QueryControl::default()
    };
    assert_eq!(
        database.query(&query, &inputs, &limit).unwrap_err().code,
        "query/work-limit"
    );
}

#[test]
fn sequence_defers_transforms_preserves_sources_and_fuses_after_cancellation() {
    let (database, _) = fixture();
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = calls.clone();
    let mut attribute = PullAttribute::forward(AttributeName::Id(SCORE));
    attribute.transform = Some(PullTransform::new("observe", move |value| {
        observed.fetch_add(1, Ordering::Relaxed);
        Ok(value.clone())
    }));
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Pull {
            source: "$".into(),
            variable: variable("e"),
            pattern: Box::new(PullPattern::attributes(vec![attribute])),
        }]),
        vec![pattern()],
    );
    let control = QueryControl::default();
    let mut sequence = database.query_sequence(&query, &[], &control).unwrap();
    assert_eq!(sequence.remaining_rows(), 3);
    assert_eq!(calls.load(Ordering::Relaxed), 0);
    let first = sequence.next().unwrap().unwrap();
    assert!(
        matches!(&first[0], QueryValue::Map(entries) if entries.iter().any(|(key, _)| key == &QueryValue::Scalar(Value::Keyword(Keyword::new("item", "score")))))
    );
    assert_eq!(calls.load(Ordering::Relaxed), 1);
    assert_eq!(sequence.remaining_rows(), 2);
    control.cancel.store(true, Ordering::Relaxed);
    assert!(sequence.next().unwrap().is_err());
    assert!(sequence.next().is_none());
    assert_eq!(calls.load(Ordering::Relaxed), 1);
    // One work account spans relation preparation and deferred Pull.
    let prepared_work = database
        .query_sequence(&query, &[], &QueryControl::default())
        .unwrap()
        .stats()
        .work;
    let limit = QueryControl {
        max_work: prepared_work as usize,
        ..QueryControl::default()
    };
    let mut sequence = database.query_sequence(&query, &[], &limit).unwrap();
    assert_eq!(
        sequence.next().unwrap().unwrap_err().code,
        "query/work-limit"
    );
}

#[test]
fn local_random_aggregates_have_documented_cardinality_and_membership() {
    let database = Database::new(Schema::new()).unwrap();
    for (function, expected) in [
        (Aggregate::Rand(20), 20),
        (Aggregate::Sample(20), 3),
        (Aggregate::Rand(0), 0),
        (Aggregate::Sample(0), 0),
    ] {
        let mut query = Query::new(
            FindSpec::Scalar(FindElement::Aggregate {
                function,
                variable: variable("x"),
            }),
            vec![],
        );
        query.inputs = vec![InputSpec::Collection(variable("x"))];
        let outcome = database
            .query(
                &query,
                &[QueryInput::Collection(vec![
                    Value::Long(1),
                    Value::Long(2),
                    Value::Long(2),
                    Value::Long(3),
                ])],
                &QueryControl::default(),
            )
            .unwrap();
        let QueryResult::Scalar(Some(result)) = outcome.result else {
            panic!("scalar result")
        };
        let items = result.into_collection().unwrap();
        assert_eq!(items.len(), expected);
        assert!(
            items
                .iter()
                .all(|item| matches!(item, QueryValue::Scalar(Value::Long(1..=3))))
        );
        if matches!(function, Aggregate::Sample(_)) {
            for (index, item) in items.iter().enumerate() {
                assert!(!items[..index].contains(item));
            }
        }
    }
}

#[test]
fn pull_post_projection_preserves_distinct_entities_even_when_maps_are_equal() {
    let (database, _) = fixture();
    let mut attribute = PullAttribute::forward(AttributeName::Id(SCORE));
    attribute.transform = Some(PullTransform::new("same", |_| {
        Ok(QueryValue::Scalar(Value::Long(1)))
    }));
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Pull {
            source: "$".into(),
            variable: variable("e"),
            pattern: Box::new(PullPattern::attributes(vec![attribute])),
        }]),
        vec![pattern()],
    );
    let result = database
        .query(&query, &[], &QueryControl::default())
        .unwrap()
        .result;
    let QueryResult::Relation(rows) = result else {
        panic!("relation")
    };
    assert_eq!(rows.len(), 3);
    assert_eq!(rows[0], rows[1]);
    let sequence = database
        .query_sequence(&query, &[], &QueryControl::default())
        .unwrap();
    assert_eq!(sequence.remaining_rows(), 3);
    assert_eq!(sequence.collect::<Result<Vec<_>, _>>().unwrap(), rows);
}
