use atomic_core::{
    Attribute, AttributeName, Cardinality, DB_IDENT, Database, EntityIdentifier, EntityRef,
    Keyword, PullAttribute, PullPattern, QueryValue, Schema, TxOp, TxValue, USER_PARTITION, Unique,
    Value, ValueType, make_eid,
};

const ITEM_KEY: u32 = 1_000;
const ITEM_VALUE: u32 = 1_001;

fn database() -> Database {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                ITEM_KEY,
                Keyword::new("item", "key"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            ITEM_VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    Database::new(schema).unwrap()
}

#[test]
fn pull_and_entity_resolve_eids_aliases_and_lookup_refs_on_one_database_value() {
    let old_entity_ident = Keyword::new("item", "old");
    let new_entity_ident = Keyword::new("item", "new");
    let old_attribute_ident = Keyword::new("item", "key");
    let new_attribute_ident = Keyword::new("item", "lookup-key");

    let created = database()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(old_entity_ident.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: ITEM_KEY,
                    value: Value::String("widget".into()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: ITEM_VALUE,
                    value: Value::Long(42).into(),
                },
            ],
            1_000,
        )
        .unwrap();
    let entity = created.tempids["item"];
    let before_rename = created.db_after;
    let after_rename = before_rename
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(new_entity_ident.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Id(u64::from(ITEM_KEY)),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(new_attribute_ident.clone()).into(),
                },
            ],
            2_000,
        )
        .unwrap()
        .db_after;

    let pattern =
        PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(ITEM_VALUE))]);
    let identifiers = vec![
        EntityIdentifier::Id(entity),
        EntityIdentifier::Ident(old_entity_ident.clone()),
        EntityIdentifier::Ident(new_entity_ident.clone()),
        EntityIdentifier::Lookup {
            attribute: AttributeName::Id(ITEM_KEY),
            value: Value::String("widget".into()),
        },
        EntityIdentifier::Lookup {
            attribute: AttributeName::Ident(old_attribute_ident.clone()),
            value: Value::String("widget".into()),
        },
        EntityIdentifier::Lookup {
            attribute: AttributeName::Ident(new_attribute_ident),
            value: Value::String("widget".into()),
        },
    ];

    let expected = after_rename.pull(&pattern, entity).unwrap();
    for identifier in &identifiers {
        assert_eq!(
            after_rename.resolve_entity_identifier(identifier).unwrap(),
            Some(entity)
        );
        assert_eq!(
            after_rename
                .entity(identifier.clone())
                .unwrap()
                .unwrap()
                .id(),
            entity
        );
        assert_eq!(
            after_rename.pull(&pattern, identifier.clone()).unwrap(),
            expected
        );
    }
    assert_eq!(
        after_rename.pull_many(&pattern, identifiers).unwrap(),
        vec![expected; 6]
    );

    let unknown = EntityIdentifier::Ident(Keyword::new("item", "missing"));
    assert!(after_rename.entity(unknown.clone()).unwrap().is_none());
    assert_eq!(
        after_rename.pull(&pattern, unknown).unwrap(),
        QueryValue::Map(Vec::new())
    );

    let non_unique = after_rename
        .resolve_entity_identifier(&EntityIdentifier::Lookup {
            attribute: AttributeName::Id(ITEM_VALUE),
            value: Value::Long(42),
        })
        .unwrap_err();
    assert_eq!(non_unique.code, "transaction/lookup-non-unique");
    let wrong_type = after_rename
        .resolve_entity_identifier(&EntityIdentifier::Lookup {
            attribute: AttributeName::Id(ITEM_KEY),
            value: Value::Long(42),
        })
        .unwrap_err();
    assert_eq!(wrong_type.code, "transaction/value-type");

    // Numeric identifiers mirror recovered resolve-id: a valid numeric eid
    // can name a lazy entity even when that entity currently has no facts.
    let empty = make_eid(USER_PARTITION, 999).unwrap();
    assert_eq!(after_rename.entity(empty).unwrap().unwrap().id(), empty);

    // Ident resolution belongs to the exact immutable database value. A
    // later repurpose changes only the successor's alias map.
    let repurposed = after_rename
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("replacement".into()),
                attribute: DB_IDENT as u32,
                value: TxValue::Scalar(Value::Keyword(old_entity_ident.clone())),
            }],
            3_000,
        )
        .unwrap();
    assert_eq!(
        after_rename
            .resolve_entity_identifier(&EntityIdentifier::from(old_entity_ident.clone()))
            .unwrap(),
        Some(entity)
    );
    assert_eq!(
        repurposed
            .db_after
            .resolve_entity_identifier(&EntityIdentifier::from(old_entity_ident))
            .unwrap(),
        Some(repurposed.tempids["replacement"])
    );
}
