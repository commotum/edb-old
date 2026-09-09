use atomic_core::{
    Attribute, Cardinality, Database, DatabaseValue, EntityRef, IndexBoundary, IndexComponents,
    IndexOrder, Keyword, Schema, TxOp, Value, ValueType,
};

mod common;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn exercise(mut eager: Database, mut native: DatabaseValue) {
    let original = native.clone();
    let first = eager
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: 1_000,
                value: Value::Long(1).into(),
            }],
            1,
        )
        .unwrap();
    let speculative = native
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: 1_000,
                value: Value::Long(1).into(),
            }],
            1,
        )
        .unwrap();
    assert_eq!(speculative.tempids, first.tempids);
    let entity = first.tempids["item"];
    eager = first.db_after;
    native = speculative.db_after;
    for step in 2..15 {
        let mut ops = if step % 3 == 0 {
            vec![TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: 1_000,
                value: None,
            }]
        } else {
            vec![TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: 1_000,
                value: Value::Long(step % 2).into(),
            }]
        };
        if [4, 8, 10, 11].contains(&step) {
            let mut attribute = eager.schema().attribute(1_000).unwrap().clone();
            if step == 11 {
                attribute.no_history = true;
            } else {
                attribute.indexed = step != 8;
            }
            ops.push(TxOp::AlterAttribute(attribute));
        }
        if [5, 6, 7].contains(&step) {
            ops.push(TxOp::Add {
                entity: if step == 7 {
                    EntityRef::Temp("alias-replacement".into())
                } else {
                    EntityRef::Id(entity)
                },
                attribute: atomic_core::DB_IDENT as u32,
                value: Value::Keyword(Keyword::new(
                    "item",
                    if step == 6 { "renamed" } else { "original" },
                ))
                .into(),
            });
        }
        let expected = eager.with(&ops, step).unwrap();
        let actual = native.with(&ops, step).unwrap();
        assert_eq!(actual.tx_data, expected.tx_data);
        assert_eq!(actual.tempids, expected.tempids);
        assert_eq!(actual.db_after.schema(), expected.db_after.schema());
        assert_eq!(
            actual.db_after.ident(entity),
            expected.db_after.ident(entity)
        );
        for ident in ["original", "renamed"] {
            assert_eq!(
                actual.db_after.entid(&Keyword::new("item", ident)),
                expected.db_after.entid(&Keyword::new("item", ident))
            );
        }
        assert_eq!(actual.db_before.basis_t(), native.basis_t());
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(
                actual.db_after.collect_datoms(order).unwrap(),
                expected
                    .db_after
                    .database_value()
                    .collect_datoms(order)
                    .unwrap()
            );
            assert_eq!(
                actual
                    .db_after
                    .clone()
                    .history()
                    .collect_datoms(order)
                    .unwrap(),
                expected
                    .db_after
                    .database_value()
                    .history()
                    .collect_datoms(order)
                    .unwrap()
            );
        }
        eager = expected.db_after;
        native = actual.db_after;
    }
    assert!(original.values(entity, 1_000).unwrap().is_empty());
    let expected = eager.database_value();
    for cutoff in [2, 5, 9] {
        common::assert_same_information(
            &native.clone().as_of(cutoff),
            &expected.clone().as_of(cutoff),
        );
        common::assert_same_information(
            &native.clone().since(cutoff),
            &expected.clone().since(cutoff),
        );
    }
    for point in [0, 1, 2, 5, 9, 14, 15] {
        assert_eq!(
            native
                .resolve_time_point(atomic_core::TimePoint::Instant(point))
                .unwrap(),
            expected
                .resolve_time_point(atomic_core::TimePoint::Instant(point))
                .unwrap()
        );
    }
    for boundary in [
        IndexBoundary::Eavt(IndexComponents::One(entity)),
        IndexBoundary::Aevt(IndexComponents::One(1_000)),
        IndexBoundary::Avet(IndexComponents::One(atomic_core::DB_TX_INSTANT as u32)),
        IndexBoundary::Vaet(IndexComponents::One(Value::Ref(entity))),
    ] {
        for (actual, expected) in [
            (native.clone(), expected.clone()),
            (native.clone().history(), expected.clone().history()),
            (native.clone().as_of(5), expected.clone().as_of(5)),
            (native.clone().since(5), expected.clone().since(5)),
        ] {
            assert_eq!(
                actual
                    .seek_cursor(&boundary)
                    .unwrap()
                    .collect::<Result<Vec<_>, _>>()
                    .unwrap(),
                expected
                    .seek_cursor(&boundary)
                    .unwrap()
                    .collect::<Result<Vec<_>, _>>()
                    .unwrap()
            );
            assert_eq!(
                actual
                    .reverse_seek_cursor(&boundary)
                    .unwrap()
                    .collect::<Result<Vec<_>, _>>()
                    .unwrap(),
                expected
                    .reverse_seek_cursor(&boundary)
                    .unwrap()
                    .collect::<Result<Vec<_>, _>>()
                    .unwrap()
            );
        }
    }
}

#[test]
fn chained_speculation_preserves_current_and_history_against_the_eager_oracle() {
    let eager = Database::new(schema()).unwrap();
    exercise(eager.clone(), eager.database_value());
}

#[test]
fn native_speculation_never_advances_postgres_or_materializes_the_database() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let id = format!(
        "native-speculation-{}-{}",
        std::process::id(),
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    atomic_core::PostgresMigrator::connect(&postgres)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = atomic_core::PostgresStore::connect(&postgres).unwrap();
    store.create_database(&id, schema()).unwrap();
    let writer = common::start_service(&postgres, &id);
    let peer = atomic_core::Connection::connect(&postgres, &id, 4).unwrap();
    let before = peer.db();
    let eager = store.recover(&id).unwrap();
    exercise(eager, before.clone());
    assert_eq!(peer.sync().unwrap().basis_t(), before.basis_t());
    assert_eq!(store.recover(&id).unwrap().basis_t(), before.basis_t());
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    // Schema/index changes in a speculative chain must expose pre-existing
    // unindexed facts as well as the new delta without publishing a tree.
    let first = before
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("indexed".into()),
                attribute: 1_000,
                value: Value::Long(22).into(),
            }],
            20,
        )
        .unwrap();
    let entity = first.tempids["indexed"];
    let mut indexed_attribute = first.db_after.schema().attribute(1_000).unwrap().clone();
    indexed_attribute.indexed = true;
    let indexed = first
        .db_after
        .with(&[TxOp::AlterAttribute(indexed_attribute)], 21)
        .unwrap()
        .db_after;
    let boundary = IndexBoundary::Avet(IndexComponents::Two(1_000, Value::Long(22)));
    assert!(
        indexed
            .seek_cursor(&boundary)
            .unwrap()
            .any(|datom| datom.unwrap().entity == entity)
    );
    assert!(
        indexed
            .reverse_seek_cursor(&boundary)
            .unwrap()
            .any(|datom| datom.unwrap().entity == entity)
    );
    assert_eq!(peer.db().basis_t(), before.basis_t());
    writer.shutdown();
}

#[test]
fn long_speculative_chains_have_flat_iterator_and_drop_depth() {
    // A small stack makes accidental recursive overlay nesting observable.
    std::thread::Builder::new()
        .stack_size(256 * 1024)
        .spawn(|| {
            let database = Database::new(Schema::new()).unwrap();
            let mut value = database.database_value();
            for instant in 1..=2_000 {
                value = value.with(&[], instant).unwrap().db_after;
            }
            assert_eq!(value.basis_t(), 2_000);
            assert_eq!(
                value
                    .resolve_time_point(atomic_core::TimePoint::Instant(1_500))
                    .unwrap(),
                1_500
            );
            assert_eq!(
                value
                    .values(
                        atomic_core::t_to_tx(2_000).unwrap(),
                        atomic_core::DB_TX_INSTANT as u32
                    )
                    .unwrap(),
                vec![Value::Instant(2_000)]
            );
            drop(value);
        })
        .unwrap()
        .join()
        .unwrap();
}
