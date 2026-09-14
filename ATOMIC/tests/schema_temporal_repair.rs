use atomic_core::{
    Attribute, Cardinality, Clause, DB_IDENT, DB_INDEX, DataPattern, Database, EntityRef,
    FindElement, FindSpec, IndexOrder, Keyword, Query, QueryControl, QueryEngine, QueryResult,
    QuerySource, QueryValue, Schema, Term, TxOp, TxValue, USER_PARTITION, Value, ValueType,
    Variable, View, make_eid,
};

const ITEM_TAG: u32 = 1_000;

#[test]
fn time_views_use_the_schema_of_the_database_value_that_created_them() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_TAG,
            Keyword::new("item", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    let database = Database::new(schema).unwrap();
    let entity = make_eid(USER_PARTITION, 42).unwrap();

    let before_index = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: ITEM_TAG,
                value: TxValue::Scalar(Value::String("old fact".into())),
            }],
            1_000,
        )
        .unwrap()
        .db_after;
    assert_eq!(before_index.basis_t(), 2);
    assert!(!before_index.schema().attribute(ITEM_TAG).unwrap().indexed);
    assert!(
        before_index
            .datoms(View::Current, IndexOrder::Avet)
            .iter()
            .all(|datom| datom.attribute != ITEM_TAG)
    );

    let mut indexed_attribute = before_index.schema().attribute(ITEM_TAG).unwrap().clone();
    indexed_attribute.indexed = true;
    let after_index = before_index
        .with(&[TxOp::AlterAttribute(indexed_attribute)], 2_000)
        .unwrap()
        .db_after;

    // The relation is rewound to t=2, before :db/index was asserted...
    assert!(
        after_index
            .datoms(View::AsOf(2), IndexOrder::Eavt)
            .iter()
            .all(|datom| !(datom.entity == u64::from(ITEM_TAG)
                && datom.attribute == DB_INDEX as u32))
    );
    // ...but that relation is interpreted with after_index's current schema,
    // so the old application fact participates in AVET.
    assert!(
        after_index
            .datoms(View::AsOf(2), IndexOrder::Avet)
            .iter()
            .any(|datom| datom.entity == entity && datom.attribute == ITEM_TAG)
    );

    // The independently retained older database value keeps its own schema.
    assert!(!before_index.schema().attribute(ITEM_TAG).unwrap().indexed);
    assert!(
        before_index
            .datoms(View::Current, IndexOrder::Avet)
            .iter()
            .all(|datom| datom.attribute != ITEM_TAG)
    );
}

#[test]
fn temporal_sources_resolve_idents_from_the_database_values_current_cache() {
    let old_ident = Keyword::new("item", "tag");
    let new_ident = Keyword::new("item", "label");
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_TAG,
            old_ident,
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let entity = make_eid(USER_PARTITION, 42).unwrap();
    let before_rename = Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: ITEM_TAG,
                value: Value::String("historical".into()).into(),
            }],
            1_000,
        )
        .unwrap()
        .db_after;
    let after_rename = before_rename
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(u64::from(ITEM_TAG)),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(new_ident.clone()).into(),
            }],
            2_000,
        )
        .unwrap()
        .db_after;

    let found = Variable::new("found").unwrap();
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable(found.clone())),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::Constant(Value::Ref(entity)),
            Term::Constant(Value::Keyword(new_ident.clone())),
            Term::Variable(found),
        )))],
    );
    let outcome = QueryEngine::execute(
        &query,
        &[QuerySource {
            name: "$".into(),
            database: after_rename.database_value().as_of(2),
        }],
        &[],
        &QueryControl::default(),
    )
    .unwrap();
    assert_eq!(
        outcome.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::String("historical".into()))))
    );
    assert_eq!(
        before_rename
            .query(&query, &[], &QueryControl::default())
            .unwrap_err()
            .code,
        "query/unknown-attribute"
    );
}
