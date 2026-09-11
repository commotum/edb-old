//! General and schema ident aliases survive current-block reopen and exact retry.
mod common;
use atomic_core::*;
const ITEM_NAME: u32 = 1000;
const ITEM_COUNT: u32 = 1001;
const ITEM_KIND: u32 = 1002;
fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                ITEM_NAME,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            ITEM_COUNT,
            Keyword::new("item", "quantity"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

#[test]
fn general_idents_aliases_and_schema_recover_from_only_the_ordinary_log() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "ident_restart");
    let connection = fixture.connection.clone();
    let database_id = "ident-restart";
    let old_holder = Keyword::new("holder", "old");
    let new_holder = Keyword::new("holder", "new");
    let old_enum = Keyword::new("kind", "old");
    let new_enum = Keyword::new("kind", "new");
    let old_attribute = Keyword::new("item", "kind");
    let new_attribute = Keyword::new("item", "category");
    let mut app_schema = schema();
    app_schema
        .install(Attribute::new(
            ITEM_KIND,
            old_attribute.clone(),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();

    common::blocks::install(&connection).unwrap();
    let mut store = common::blocks::TestStore::connect(&connection).unwrap();
    let created = store.create_database(database_id, app_schema).unwrap();
    let service = common::start_service(&connection, database_id);
    let populated = common::transact(
        &service,
        "idents-create",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("enum".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(old_enum.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("holder".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(old_holder.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("holder".into()),
                attribute: ITEM_NAME,
                value: Value::String("durable holder".into()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("holder".into()),
                attribute: ITEM_KIND,
                value: TxValue::Entity(EntityRef::Temp("enum".into())),
            },
        ],
        1_000,
    );
    let holder = populated.tempids["holder"];
    let enum_id = populated.tempids["enum"];
    let rename_ops = vec![
        TxOp::Add {
            entity: EntityRef::Id(holder),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(new_holder.clone()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Id(enum_id),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(new_enum.clone()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Id(u64::from(ITEM_KIND)),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(new_attribute.clone()).into(),
        },
    ];
    let renamed = common::transact(
        &service,
        "idents-rename",
        populated.basis_t,
        &rename_ops,
        2_000,
    );
    assert!(renamed.tx_data.iter().all(|datom| {
        datom.attribute != atomic_core::DB_INSTALL_ATTRIBUTE as u32
            && datom.attribute != atomic_core::DB_ALTER_ATTRIBUTE as u32
    }));
    let advanced = common::transact(
        &service,
        "idents-advance",
        renamed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(holder),
            attribute: ITEM_COUNT,
            value: Value::Long(7).into(),
        }],
        3_000,
    );
    let retry = common::transact(
        &service,
        "idents-rename",
        populated.basis_t,
        &rename_ops,
        2_000,
    );
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, renamed.basis_t);
    assert_eq!(retry.tx_hash, renamed.tx_hash);
    assert_eq!(retry.tempids, renamed.tempids);
    assert_eq!(retry.tx_data, renamed.tx_data);
    common::assert_same_information(&retry.db_after, &renamed.db_after);

    service.shutdown();
    drop(store);
    let mut reopened = common::blocks::TestStore::connect(&connection).unwrap();
    let recovered = reopened.recover(database_id).unwrap();
    common::assert_same_information(&recovered, &advanced.db_after);
    assert_eq!(recovered.entid(&old_holder), Some(holder));
    assert_eq!(recovered.entid(&new_holder), Some(holder));
    assert_eq!(recovered.entid(&old_enum), Some(enum_id));
    assert_eq!(recovered.entid(&new_enum), Some(enum_id));
    assert_eq!(recovered.entid(&old_attribute), Some(u64::from(ITEM_KIND)));
    assert_eq!(recovered.entid(&new_attribute), Some(u64::from(ITEM_KIND)));

    let pattern = PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Ident(
        old_attribute,
    ))]);
    assert_eq!(
        recovered
            .pull(&pattern, EntityIdentifier::Ident(old_holder))
            .unwrap(),
        recovered.pull(&pattern, holder).unwrap()
    );
}
