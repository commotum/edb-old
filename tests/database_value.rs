use atomic_core::{
    Attribute, AttributeName, Cardinality, DB_IDENT, Database, DatabaseValue, EntityIdentifier,
    EntityRef, EntityValue, IndexOrder, IndexPrefix, Keyword, Schema, TupleSpec, TxOp, TxValue,
    Unique, Value, ValueType, View,
};
use bigdecimal::BigDecimal;
use std::str::FromStr;
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};

const EMAIL: u32 = 1_000;
const NAME: u32 = 1_001;
const AMOUNT: u32 = 1_000;
const AMOUNT_TUPLE: u32 = 1_001;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                EMAIL,
                Keyword::new("person", "email"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            NAME,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn changing_database() -> (Database, u64) {
    let database = Database::new(schema()).unwrap();
    let first = database
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("ada".into()),
                    attribute: EMAIL,
                    value: TxValue::Scalar(Value::String("ada@example.test".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("ada".into()),
                    attribute: NAME,
                    value: TxValue::Scalar(Value::String("Ada".into())),
                },
            ],
            2_000,
        )
        .unwrap();
    let entity = first.tempids["ada"];
    let second = first
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: NAME,
                value: TxValue::Scalar(Value::String("wrong".into())),
            }],
            3_000,
        )
        .unwrap();
    (second.db_after, entity)
}

fn name_prefix(entity: u64) -> IndexPrefix {
    IndexPrefix::Eavt {
        entity,
        attribute: Some(NAME),
        value: None,
    }
}

fn representation_schema() -> Schema {
    let mut schema = Schema::new();
    let mut amount = Attribute::new(
        AMOUNT,
        Keyword::new("measurement", "amount"),
        ValueType::BigDec,
        Cardinality::Many,
    );
    amount.indexed = true;
    schema.install(amount).unwrap();

    let mut tuple = Attribute::new(
        AMOUNT_TUPLE,
        Keyword::new("measurement", "amount-tuple"),
        ValueType::Tuple,
        Cardinality::Many,
    )
    .tuple(TupleSpec::Homogeneous(ValueType::BigDec));
    tuple.indexed = true;
    schema.install(tuple).unwrap();
    schema
}

fn decimal(value: &str) -> Value {
    Value::BigDec(BigDecimal::from_str(value).unwrap())
}

fn decimal_tuple(value: &str) -> Value {
    Value::Tuple(vec![Some(decimal(value)), None])
}

fn decimal_scales(values: Vec<Value>) -> Vec<i64> {
    let mut scales = values
        .into_iter()
        .map(|value| match value {
            Value::BigDec(value) => value.fractional_digit_count(),
            other => panic!("expected BigDecimal, got {other:?}"),
        })
        .collect::<Vec<_>>();
    scales.sort_unstable();
    scales
}

fn tuple_decimal_scales(values: Vec<Value>) -> Vec<i64> {
    let mut scales = values
        .into_iter()
        .map(|value| match value {
            Value::Tuple(values) => match values.as_slice() {
                [Some(Value::BigDec(value)), None] => value.fractional_digit_count(),
                _ => panic!("expected a BigDecimal/nil tuple"),
            },
            other => panic!("expected tuple, got {other:?}"),
        })
        .collect::<Vec<_>>();
    scales.sort_unstable();
    scales
}

#[test]
fn temporal_and_custom_windows_are_part_of_one_database_value() {
    let (database, entity) = changing_database();
    assert_eq!(database.basis_t(), 3);
    let current = database.database_value();

    assert_eq!(
        current.values(entity, NAME).unwrap(),
        vec![Value::String("wrong".into())]
    );
    assert_eq!(
        current.clone().as_of(2).values(entity, NAME).unwrap(),
        vec![Value::String("Ada".into())]
    );
    assert_eq!(
        current.clone().since(2).values(entity, NAME).unwrap(),
        vec![Value::String("wrong".into())]
    );

    // Removing the entire bad transaction reveals the prior assertion. The
    // predicate runs before point-in-time retraction collapse.
    let corrected = current
        .clone()
        .filter(|_, datom| atomic_core::tx_to_t(datom.tx).unwrap() != 3);
    assert_eq!(
        corrected.values(entity, NAME).unwrap(),
        vec![Value::String("Ada".into())]
    );

    let history = current.clone().history().as_of(3);
    let events = history.datoms_with_prefix(&name_prefix(entity)).unwrap();
    assert_eq!(events.len(), 3);
    assert_eq!(events.iter().filter(|datom| datom.added).count(), 2);
    assert_eq!(events.iter().filter(|datom| !datom.added).count(), 1);

    // as-of and since coexist, while repeating one kind replaces that bound
    // as the recovered Db record does.
    let interval = current.clone().since(1).as_of(2);
    assert_eq!(
        interval.values(entity, NAME).unwrap(),
        vec![Value::String("Ada".into())]
    );
    assert_eq!(current.clone().as_of(1).as_of(2).as_of_t(), Some(2));
    assert_eq!(current.clone().since(2).since(1).since_t(), Some(1));
}

#[test]
fn a_filter_receives_the_same_temporal_value_with_only_filters_removed() {
    let (database, entity) = changing_database();
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&calls);
    let filtered = database.database_value().as_of(2).filter(move |source, _| {
        assert_eq!(source.basis_t(), 3);
        assert_eq!(source.as_of_t(), Some(2));
        assert_eq!(source.since_t(), None);
        assert!(!source.is_history());
        assert!(!source.is_filtered());
        assert_eq!(
            source.values(entity, NAME).unwrap(),
            vec![Value::String("Ada".into())]
        );
        observed.fetch_add(1, Ordering::Relaxed);
        true
    });

    assert_eq!(
        filtered.values(entity, NAME).unwrap(),
        vec![Value::String("Ada".into())]
    );
    assert!(calls.load(Ordering::Relaxed) > 0);
}

#[test]
fn windowed_prefix_reads_match_the_eager_oracle_in_every_index_order() {
    let (database, _) = changing_database();
    let value = database.database_value().as_of(2);
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            value.datoms(order).unwrap(),
            database.datoms(View::AsOf(2), order),
            "{order:?} window diverged"
        );
    }
}

#[test]
fn lookup_refs_resolve_through_the_exact_window_but_idents_use_the_basis_cache() {
    let (database, entity) = changing_database();
    let current = DatabaseValue::from(&database);
    let email = Value::String("ada@example.test".into());
    let lookup = EntityIdentifier::Lookup {
        attribute: AttributeName::Ident(Keyword::new("person", "email")),
        value: email.clone(),
    };

    assert_eq!(
        current.resolve_entity_identifier(&lookup).unwrap(),
        Some(entity)
    );
    assert_eq!(current.lookup(EMAIL, &email).unwrap(), Some(entity));
    assert_eq!(
        current.clone().as_of(1).lookup(EMAIL, &email).unwrap(),
        None
    );
    assert_eq!(
        current.clone().since(2).lookup(EMAIL, &email).unwrap(),
        None
    );

    // `:db/ident` is the recovered dictionary fast path rather than an AVET
    // lookup filtered out by a since view.
    let attribute_ident = Keyword::new("person", "email");
    assert_eq!(
        current
            .clone()
            .since(current.basis_t())
            .lookup(DB_IDENT as u32, &Value::Keyword(attribute_ident.clone()))
            .unwrap(),
        Some(u64::from(EMAIL))
    );
    assert_eq!(current.entid(&attribute_ident), Some(u64::from(EMAIL)));
    assert_eq!(current.ident(u64::from(EMAIL)), Some(&attribute_ident));
}

#[test]
fn history_has_an_explicit_point_in_time_guard_and_values_are_immutable() {
    let (database, entity) = changing_database();
    let old = database.database_value().as_of(2);
    let history = old.clone().history();
    let error = history.require_point_in_time("entity").unwrap_err();
    assert_eq!(error.code, "database/history-not-point-in-time");
    assert!(old.require_point_in_time("entity").is_ok());

    let successor = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: NAME,
                value: TxValue::Scalar(Value::String("later".into())),
            }],
            4_000,
        )
        .unwrap()
        .db_after;
    assert_eq!(
        old.values(entity, NAME).unwrap(),
        vec![Value::String("Ada".into())]
    );
    assert_eq!(
        successor.database_value().values(entity, NAME).unwrap(),
        vec![Value::String("later".into())]
    );
}

#[test]
fn filtered_collapse_keeps_every_assertion_until_an_exact_retraction() {
    let before = Database::new(representation_schema()).unwrap();
    let first = before
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("measurement".into()),
                attribute: AMOUNT,
                value: TxValue::Scalar(decimal("2.0")),
            }],
            2_000,
        )
        .unwrap();
    let entity = first.tempids["measurement"];
    let retracted = first
        .db_after
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: AMOUNT,
                value: Some(TxValue::Scalar(decimal("2.0"))),
            }],
            3_000,
        )
        .unwrap();
    let asserted_again = retracted
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: AMOUNT,
                value: TxValue::Scalar(decimal("2.0")),
            }],
            4_000,
        )
        .unwrap();

    // Removing the intervening retraction exposes two assertion events.
    // Recovered filter-retractions yields both; it does not reduce a logical
    // E/A/V group to only its newest assertion.
    let filtered = asserted_again
        .db_after
        .database_value()
        .filter(|_, datom| atomic_core::tx_to_t(datom.tx).unwrap() != 3);
    let datoms = filtered
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity,
            attribute: Some(AMOUNT),
            value: Some(decimal("2.0")),
        })
        .unwrap();
    assert_eq!(datoms.len(), 2);
    assert!(datoms.iter().all(|datom| datom.added));
    assert_eq!(
        datoms
            .iter()
            .map(|datom| atomic_core::tx_to_t(datom.tx).unwrap())
            .collect::<Vec<_>>(),
        vec![4, 2]
    );
}

#[test]
fn entity_many_navigation_collapses_representation_distinct_logical_equals() {
    let report = Database::new(representation_schema())
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("measurement".into()),
                    attribute: AMOUNT,
                    value: TxValue::Scalar(decimal("1.0")),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("measurement".into()),
                    attribute: AMOUNT,
                    value: TxValue::Scalar(decimal("1.00")),
                },
            ],
            2_000,
        )
        .unwrap();
    let measurement = report.tempids["measurement"];

    // The immutable log retains both scale-distinct representations, while
    // associative cardinality-many navigation has Datomic's set semantics.
    assert_eq!(
        report
            .db_after
            .database_value()
            .values(measurement, AMOUNT)
            .unwrap()
            .len(),
        2
    );
    let entity = report.db_after.entity(measurement).unwrap().unwrap();
    let Some(EntityValue::Collection(amounts)) = entity.get(AMOUNT).unwrap() else {
        panic!("cardinality-many values must remain a collection")
    };
    assert_eq!(amounts.len(), 1);
}

#[test]
fn representation_distinct_decimals_do_not_cross_cancel_in_windows() {
    let one_scale = decimal("1.0");
    let two_scale = decimal("1.00");
    assert!(one_scale.index_cmp(&two_scale).is_eq());
    assert!(!one_scale.stored_eq(&two_scale));
    let one_scale_tuple = decimal_tuple("1.0");
    let two_scale_tuple = decimal_tuple("1.00");
    assert!(one_scale_tuple.index_cmp(&two_scale_tuple).is_eq());
    assert!(!one_scale_tuple.stored_eq(&two_scale_tuple));

    let before = Database::new(representation_schema()).unwrap();
    let two_scale_asserted = before
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("measurement".into()),
                    attribute: AMOUNT,
                    value: TxValue::Scalar(decimal("1.00")),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("measurement".into()),
                    attribute: AMOUNT_TUPLE,
                    value: TxValue::Scalar(decimal_tuple("1.00")),
                },
            ],
            2_000,
        )
        .unwrap();
    let entity = two_scale_asserted.tempids["measurement"];
    let one_scale_asserted = two_scale_asserted
        .db_after
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: TxValue::Scalar(decimal("1.0")),
                },
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT_TUPLE,
                    value: TxValue::Scalar(decimal_tuple("1.0")),
                },
            ],
            3_000,
        )
        .unwrap();
    let retracted = one_scale_asserted
        .db_after
        .with(
            &[
                TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: Some(TxValue::Scalar(decimal("1.00"))),
                },
                TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT_TUPLE,
                    value: Some(TxValue::Scalar(decimal_tuple("1.00"))),
                },
            ],
            4_000,
        )
        .unwrap();
    let current = retracted.db_after.database_value();

    assert_eq!(
        decimal_scales(current.clone().as_of(3).values(entity, AMOUNT).unwrap()),
        vec![1, 2]
    );
    assert_eq!(
        tuple_decimal_scales(
            current
                .clone()
                .as_of(3)
                .values(entity, AMOUNT_TUPLE)
                .unwrap()
        ),
        vec![1, 2]
    );

    // In recovered logical order the t=3 assertion of 1.0M sits between the
    // t=4 retraction and t=2 assertion of 1.00M. BigDecimal scale is part of
    // the port's stored fact identity, including recursively inside tuples:
    // the scan must connect the exact retraction to its older assertion
    // without hiding the interleaved equal-magnitude sibling. A window at the
    // current basis must also agree with the unwindowed current index.
    assert_eq!(
        decimal_scales(current.values(entity, AMOUNT).unwrap()),
        vec![1]
    );
    assert_eq!(
        decimal_scales(
            current
                .clone()
                .as_of(current.basis_t())
                .values(entity, AMOUNT)
                .unwrap()
        ),
        vec![1]
    );
    assert_eq!(
        tuple_decimal_scales(current.values(entity, AMOUNT_TUPLE).unwrap()),
        vec![1]
    );
    assert_eq!(
        tuple_decimal_scales(
            current
                .clone()
                .as_of(current.basis_t())
                .values(entity, AMOUNT_TUPLE)
                .unwrap()
        ),
        vec![1]
    );
}
