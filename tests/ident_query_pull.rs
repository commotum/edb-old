use atomic_core::{
    Attribute, AttributeName, Cardinality, Clause, DataPattern, Database, EntityRef, FindElement,
    FindSpec, Keyword, PullAttribute, PullPattern, Query, QueryControl, QueryResult, QueryValue,
    Schema, Term, TxOp, TxValue, Value, ValueType, Variable, t_to_tx,
};

const DB_IDENT: u32 = 10;
const ITEM_KIND: u32 = 1_000;

#[derive(Clone)]
struct Idents {
    holder_old: Keyword,
    holder_new: Keyword,
    attribute_old: Keyword,
    attribute_new: Keyword,
    enum_old: Keyword,
    enum_new: Keyword,
}

fn renamed_ident_database() -> (Database, u64, u64, Idents) {
    let idents = Idents {
        holder_old: Keyword::new("holder", "old"),
        holder_new: Keyword::new("holder", "new"),
        attribute_old: Keyword::new("item", "kind"),
        attribute_new: Keyword::new("item", "category"),
        enum_old: Keyword::new("kind", "old"),
        enum_new: Keyword::new("kind", "new"),
    };
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_KIND,
            idents.attribute_old.clone(),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();

    let created = Database::new(schema)
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("enum".into()),
                    attribute: DB_IDENT,
                    value: TxValue::Scalar(Value::Keyword(idents.enum_old.clone())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("holder".into()),
                    attribute: DB_IDENT,
                    value: TxValue::Scalar(Value::Keyword(idents.holder_old.clone())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("holder".into()),
                    attribute: ITEM_KIND,
                    value: TxValue::Entity(EntityRef::Temp("enum".into())),
                },
            ],
            1_000,
        )
        .unwrap();
    let holder = created.tempids["holder"];
    let enum_id = created.tempids["enum"];
    let mut attribute = created
        .db_after
        .schema()
        .attribute(ITEM_KIND)
        .unwrap()
        .clone();
    attribute.ident = idents.attribute_new.clone();
    let renamed = created
        .db_after
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(enum_id),
                    attribute: DB_IDENT,
                    value: TxValue::Scalar(Value::Keyword(idents.enum_new.clone())),
                },
                TxOp::Add {
                    entity: EntityRef::Id(holder),
                    attribute: DB_IDENT,
                    value: TxValue::Scalar(Value::Keyword(idents.holder_new.clone())),
                },
                TxOp::AlterAttribute(attribute),
            ],
            2_000,
        )
        .unwrap();

    (renamed.db_after, holder, enum_id, idents)
}

fn keyword(keyword: &Keyword) -> Term {
    Term::Constant(Value::Keyword(keyword.clone()))
}

fn scalar_tx_query(pattern: DataPattern) -> Query {
    Query::new(
        FindSpec::Scalar(FindElement::Variable(Variable::from("tx"))),
        vec![Clause::Pattern(Box::new(pattern))],
    )
}

#[test]
fn query_resolves_historical_aliases_in_entity_attribute_and_ref_value_positions() {
    let (database, _, _, idents) = renamed_ident_database();
    let mut pattern = DataPattern::new(
        keyword(&idents.holder_old),
        keyword(&idents.attribute_old),
        keyword(&idents.enum_old),
    );
    pattern.transaction = Some(Term::Variable(Variable::from("tx")));
    let query = scalar_tx_query(pattern);
    let expected = QueryResult::Scalar(Some(QueryValue::Scalar(Value::Ref(t_to_tx(2).unwrap()))));

    for force_scan in [false, true] {
        assert_eq!(
            database
                .query(
                    &query,
                    &[],
                    &QueryControl {
                        force_scan,
                        ..QueryControl::default()
                    },
                )
                .unwrap()
                .result,
            expected
        );
    }
}

#[test]
fn keyword_values_remain_literals_when_the_attribute_is_not_ref_typed() {
    let (database, _, _, idents) = renamed_ident_database();
    let mut pattern = DataPattern::new(
        keyword(&idents.enum_old),
        Term::Constant(Value::Keyword(Keyword::new("db", "ident"))),
        keyword(&idents.enum_new),
    );
    pattern.transaction = Some(Term::Variable(Variable::from("tx")));

    assert_eq!(
        database
            .query(&scalar_tx_query(pattern), &[], &QueryControl::default(),)
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Ref(t_to_tx(3).unwrap()))))
    );
}

#[test]
fn pull_resolves_attribute_aliases_and_retains_the_requested_key() {
    let (database, holder, enum_id, idents) = renamed_ident_database();
    let result = database
        .pull(
            &PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Ident(
                idents.attribute_old.clone(),
            ))]),
            holder,
        )
        .unwrap();
    let QueryValue::Map(entries) = result else {
        panic!("pull must return a map");
    };

    assert_eq!(
        entries,
        vec![(
            QueryValue::Scalar(Value::Keyword(idents.attribute_old)),
            QueryValue::Map(vec![(
                QueryValue::Scalar(Value::Keyword(Keyword::new("db", "id"))),
                QueryValue::Scalar(Value::Ref(enum_id)),
            )]),
        )]
    );
    assert!(entries.iter().all(|(key, _)| {
        key != &QueryValue::Scalar(Value::Keyword(idents.attribute_new.clone()))
    }));
}
