use atomic_core::edn::{EdnValue, read_edn, write_edn};
use atomic_core::edn_query::{EdnQueryArgument, parse_query_edn};
use atomic_core::{
    Aggregate, Attribute, AttributeName, Cardinality, Clause, DataPattern, Database, EntityRef,
    FindElement, FindSpec, InputSpec, Keyword, PullAttribute, PullPattern, Query, QueryControl,
    QueryEngine, QueryInput, QueryResult, QuerySourceValue, QueryValue, Schema, Term, TxOp,
    TxValue, Value, ValueType,
};

fn long(value: i64) -> QueryValue {
    QueryValue::Scalar(Value::Long(value))
}

fn aggregate(function: Aggregate) -> FindElement {
    FindElement::Aggregate {
        function,
        variable: "value".into(),
    }
}

#[test]
fn distinct_is_a_set_and_with_preserves_only_distinct_basis_rows_in_each_group() {
    let mut query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable("group".into()),
            aggregate(Aggregate::Count),
            aggregate(Aggregate::CountDistinct),
            aggregate(Aggregate::Distinct),
        ]),
        vec![],
    );
    query.inputs = vec![InputSpec::Relation(vec![
        Some("group".into()),
        Some("id".into()),
        Some("value".into()),
    ])];
    let inputs = [QueryInput::Relation(
        [
            [1, 10, 7],
            [1, 10, 7], // An identical input tuple never adds to the basis set.
            [1, 11, 7], // A different :with identity does preserve this duplicate value.
            [1, 12, 8],
            [2, 20, 7],
            [2, 21, 7],
            [2, 21, 7],
        ]
        .into_iter()
        .map(|row| row.into_iter().map(Value::Long).collect())
        .collect(),
    )];
    for (with, expected_counts) in [(false, [2, 1]), (true, [3, 2])] {
        query.with = if with { vec!["id".into()] } else { vec![] };
        let result = QueryEngine::execute_sources(&query, &[], &inputs, &QueryControl::default())
            .unwrap()
            .result;
        let QueryResult::Relation(rows) = result else {
            panic!("grouped aggregate must return a relation")
        };
        assert_eq!(rows.len(), 2);
        for (group, count, members) in [
            (1, expected_counts[0], vec![long(7), long(8)]),
            (2, expected_counts[1], vec![long(7)]),
        ] {
            let row = rows.iter().find(|row| row[0] == long(group)).unwrap();
            assert_eq!(row[1], long(count));
            assert_eq!(row[2], long(members.len() as i64));
            assert!(matches!(row[3], QueryValue::Set(_)));
            assert_eq!(row[3], QueryValue::Set(members));
        }
    }
}

#[test]
fn edn_distinct_result_is_readable_set_data() {
    let bound = parse_query_edn("[:find (distinct ?value) . :in [?value ...]]")
        .unwrap()
        .bind(&[EdnQueryArgument::Data(read_edn("[3 1 3 2 1]").unwrap())])
        .unwrap();
    let result = bound
        .execute(&QueryControl::default(), None)
        .unwrap()
        .result;
    assert_eq!(
        result,
        QueryResult::Scalar(Some(QueryValue::Set(vec![long(1), long(2), long(3)])))
    );
    let edn = bound.result_to_edn(&result).unwrap();
    assert!(matches!(edn, EdnValue::Set(_)));
    let text = write_edn(&edn).unwrap();
    assert!(
        text.starts_with("#{"),
        "distinct must render as an EDN set: {text}"
    );
    let EdnValue::Set(mut values) = read_edn(&text).unwrap() else {
        panic!("readable distinct result must round-trip as a set")
    };
    values.sort_by_key(|value| match value {
        EdnValue::Long(value) => *value,
        other => panic!("unexpected set member: {other:?}"),
    });
    assert_eq!(
        values,
        vec![EdnValue::Long(1), EdnValue::Long(2), EdnValue::Long(3)]
    );
}

#[test]
fn typed_and_edn_lazy_pull_keep_the_value_byte_limit_after_relational_execution() {
    let mut schema = Schema::new();
    for (id, name, value_type) in [
        (1000, "id", ValueType::Long),
        (1001, "text", ValueType::String),
    ] {
        schema
            .install(Attribute::new(
                id,
                Keyword::new("item", name),
                value_type,
                Cardinality::One,
            ))
            .unwrap();
    }
    let payload = "x".repeat(16 * 1024);
    let database = Database::new(schema)
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: 1000,
                    value: TxValue::Scalar(Value::Long(1)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: 1001,
                    value: TxValue::Scalar(Value::String(payload.clone())),
                },
            ],
            1000,
        )
        .unwrap()
        .db_after;
    let query = Query::new(
        FindSpec::Relation(vec![FindElement::Pull {
            source: "$".into(),
            variable: "entity".into(),
            pattern: Box::new(PullPattern::attributes(vec![PullAttribute::forward(
                AttributeName::Id(1001),
            )])),
        }]),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::var("entity"),
            Term::Constant(Value::Keyword(Keyword::new("item", "id"))),
            Term::Blank,
        )))],
    );
    let bound =
        parse_query_edn("[:find (pull ?entity [:item/text]) :in $ :where [?entity :item/id _]]")
            .unwrap()
            .bind(&[EdnQueryArgument::Source(QuerySourceValue::Database(
                database.database_value(),
            ))])
            .unwrap();
    let control = QueryControl {
        max_value_bytes: 4096,
        ..QueryControl::default()
    };
    for mut sequence in [
        database.query_sequence(&query, &[], &control).unwrap(),
        bound.sequence(&control, None).unwrap(),
    ] {
        assert_eq!(sequence.remaining_rows(), 1);
        assert_eq!(sequence.stats().rows_produced, 0);
        assert!(sequence.stats().allocated_value_bytes < control.max_value_bytes);
        assert_eq!(
            sequence.next().unwrap().unwrap_err().code,
            "query/value-byte-limit"
        );
        assert_eq!(sequence.stats().rows_produced, 0);
        assert_eq!(sequence.remaining_rows(), 0);
        assert!(sequence.next().is_none());
    }
    // Failing lazy projection must leave the immutable database and bound query reusable.
    for mut sequence in [
        database
            .query_sequence(&query, &[], &QueryControl::default())
            .unwrap(),
        bound.sequence(&QueryControl::default(), None).unwrap(),
    ] {
        assert_eq!(
            sequence.next().unwrap().unwrap(),
            vec![QueryValue::Map(vec![(
                QueryValue::Scalar(Value::Keyword(Keyword::new("item", "text"))),
                QueryValue::Scalar(Value::String(payload.clone())),
            )])]
        );
        assert_eq!(sequence.stats().rows_produced, 1);
        assert!(sequence.stats().allocated_value_bytes > control.max_value_bytes);
        assert!(sequence.next().is_none());
    }
}
