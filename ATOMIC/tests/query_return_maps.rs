use atomic_core::{
    Attribute, Cardinality, Clause, DataPattern, Database, EntityRef, ErrorCategory, FindElement,
    FindSpec, Keyword, Query, QueryControl, QueryResult, QueryValue, ReturnMapShape, Schema,
    Symbol, Term, TxOp, TxValue, Value, ValueType, Variable,
};

fn keyword(name: &str) -> Value {
    Value::Keyword(Keyword::unqualified(name))
}

fn string(value: &str) -> QueryValue {
    QueryValue::Scalar(Value::String(value.into()))
}

#[test]
fn typed_keys_and_positional_access_preserve_exact_nested_values() {
    let keys = vec![
        keyword("name"),
        Value::String("name".into()),
        Value::Symbol(Symbol::unqualified("name")),
        Value::Keyword(Keyword::new("person", "name")),
    ];
    let values = vec![
        string("Alice"),
        QueryValue::Nil,
        QueryValue::Map(vec![(
            QueryValue::Scalar(keyword("nested")),
            QueryValue::Collection(vec![string("Bob"), QueryValue::Nil]),
        )]),
        QueryValue::Tuple(vec![string("Ada"), string("Lovelace")]),
    ];
    let maps = QueryResult::Relation(vec![values.clone(), values.clone()])
        .into_return_maps(keys.clone())
        .unwrap();
    assert_eq!(maps.shape(), ReturnMapShape::Relation);
    assert_eq!(maps.len(), 2);
    assert!(!maps.is_empty());
    assert_eq!(maps.keys(), keys);
    assert_eq!(maps.rows().len(), maps.iter().count());

    for row in &maps {
        assert_eq!(row.len(), keys.len());
        assert!(!row.is_empty());
        assert_eq!(row.keys(), keys);
        assert_eq!(row.values(), values);
        for (index, (key, value)) in row.iter().enumerate() {
            assert_eq!(key, &keys[index]);
            assert_eq!(value, &values[index]);
            assert_eq!(row.get(key), Some(value));
            assert_eq!(&row[index], value);
        }
        assert_eq!(row.iter().next_back(), Some((&keys[3], &values[3])));
        assert_eq!(row.get(&keyword("absent")), None);
        assert_eq!(row.get(&Value::Long(0)), None);
        let [_, nil, _, _] = row.values() else {
            panic!("return maps must support positional slice destructuring")
        };
        assert_eq!(nil, &QueryValue::Nil);
    }
    // The immutable schema is shared, not cloned for every row.
    assert!(std::ptr::eq(maps.keys().as_ptr(), maps[0].keys().as_ptr()));
    assert!(std::ptr::eq(
        maps[0].keys().as_ptr(),
        maps[1].keys().as_ptr()
    ));
    let rows = maps.clone().into_rows();
    assert_eq!(maps.into_iter().collect::<Vec<_>>(), rows);
}

#[test]
fn tuple_relation_and_empty_shapes_remain_distinct() {
    let keys = vec![keyword("value")];
    let tuple = QueryResult::Tuple(Some(vec![string("one")]))
        .into_return_maps(keys.clone())
        .unwrap();
    assert_eq!(tuple.shape(), ReturnMapShape::Tuple);
    assert_eq!(tuple.len(), 1);
    assert_eq!(tuple[0][0], string("one"));

    for (result, shape) in [
        (QueryResult::Tuple(None), ReturnMapShape::Tuple),
        (QueryResult::Relation(vec![]), ReturnMapShape::Relation),
    ] {
        let empty = result
            .clone()
            .into_return_maps_with_arity(keys.clone(), 1)
            .unwrap();
        assert_eq!(empty.shape(), shape);
        assert!(empty.is_empty());
        assert_eq!(empty.keys(), keys);
        assert_eq!(empty.iter().count(), 0);
        assert_eq!(
            empty,
            result.clone().into_return_maps(keys.clone()).unwrap()
        );
        let error = result
            .into_return_maps_with_arity(keys.clone(), 2)
            .unwrap_err();
        assert_eq!(error.code, "query/return-map-arity");
        assert_eq!(error.details["keys"], "1");
        assert_eq!(error.details["find_arity"], "2");
    }

    let zero_width = QueryResult::Tuple(Some(vec![]))
        .into_return_maps_with_arity(vec![], 0)
        .unwrap();
    assert_eq!(zero_width.len(), 1);
    assert!(zero_width[0].is_empty());
}

#[test]
fn invalid_key_types_duplicate_keys_and_every_malformed_row_are_rejected() {
    for key in [
        Value::Long(1),
        Value::Ref(1),
        Value::Bool(true),
        Value::Bytes(vec![1]),
        Value::Tuple(vec![]),
        Value::Function([0; 32]),
    ] {
        let error = QueryResult::Relation(vec![])
            .into_return_maps(vec![key])
            .unwrap_err();
        assert_eq!(error.category, ErrorCategory::Incorrect);
        assert_eq!(error.code, "query/return-map-key-type");
    }
    for key in [
        keyword("x"),
        Value::String("x".into()),
        Value::Symbol(Symbol::new("some", "x")),
    ] {
        let error = QueryResult::Tuple(None)
            .into_return_maps(vec![key.clone(), key])
            .unwrap_err();
        assert_eq!(error.code, "query/return-map-duplicate-key");
        assert_eq!(error.details["position"], "1");
    }
    for result in [
        QueryResult::Tuple(Some(vec![string("one")])),
        QueryResult::Relation(vec![vec![string("one")]]),
        // Checking only the first row would silently truncate a later row.
        QueryResult::Relation(vec![
            vec![string("one"), string("two")],
            vec![string("three")],
        ]),
    ] {
        let error = result
            .into_return_maps(vec![keyword("x"), keyword("y")])
            .unwrap_err();
        assert_eq!(error.code, "query/return-map-arity");
    }
    let error = QueryResult::Tuple(Some(vec![string("one")]))
        .into_return_maps_with_arity(vec![keyword("x")], 2)
        .unwrap_err();
    assert_eq!(error.code, "query/return-map-arity");
}

#[test]
fn scalar_and_collection_results_are_never_silently_reshaped() {
    for result in [
        QueryResult::Scalar(None),
        QueryResult::Scalar(Some(string("one"))),
        QueryResult::Collection(vec![]),
        QueryResult::Collection(vec![string("one")]),
    ] {
        let error = result.into_return_maps(vec![keyword("value")]).unwrap_err();
        assert_eq!(error.category, ErrorCategory::Incorrect);
        assert_eq!(error.code, "query/return-map-shape");
    }
}

#[test]
fn application_query_results_support_key_and_positional_destructuring() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("artist", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let empty = Database::new(schema).unwrap();
    let database = empty
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("a".into()),
                    attribute: 1000,
                    value: TxValue::Scalar(Value::String("Alice".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("b".into()),
                    attribute: 1000,
                    value: TxValue::Scalar(Value::String("Bob".into())),
                },
            ],
            1000,
        )
        .unwrap()
        .db_after;
    let mut query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(Variable::from("name")),
            FindElement::Variable(Variable::from("entity")),
        ]),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::var("entity"),
            Term::Constant(Value::Keyword(Keyword::new("artist", "name"))),
            Term::var("name"),
        )))],
    );
    let keys = vec![keyword("artist"), keyword("id")];
    let maps = database
        .query(&query, &[], &QueryControl::default())
        .unwrap()
        .result
        .into_return_maps_with_arity(keys.clone(), 2)
        .unwrap();
    assert_eq!(maps.len(), 2);
    for row in &maps {
        let [name, id] = row.values() else {
            panic!("expected the two find columns")
        };
        assert_eq!(row.get(&keys[0]), Some(name));
        assert_eq!(row.get(&keys[1]), Some(id));
        assert!(matches!(id, QueryValue::Scalar(Value::Ref(_))));
    }
    assert_eq!(maps[0][0], string("Alice"));
    assert_eq!(maps[1][0], string("Bob"));

    query.find = FindSpec::Tuple(vec![FindElement::Variable(Variable::from("name"))]);
    let tuple = database
        .query(&query, &[], &QueryControl::default())
        .unwrap()
        .result
        .into_return_maps_with_arity(vec![keyword("artist")], 1)
        .unwrap();
    assert_eq!(tuple.shape(), ReturnMapShape::Tuple);
    assert_eq!(tuple.len(), 1);

    let no_match = empty
        .query(&query, &[], &QueryControl::default())
        .unwrap()
        .result
        .into_return_maps_with_arity(vec![keyword("artist")], 1)
        .unwrap();
    assert_eq!(no_match.shape(), ReturnMapShape::Tuple);
    assert!(no_match.is_empty());
}
