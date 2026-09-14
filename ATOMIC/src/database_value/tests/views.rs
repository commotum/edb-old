use super::*;

#[test]
fn eager_raw_seek_streams_both_directions_across_indexes_and_views() {
    let (report, _, first, second, ..) = overlay_fixture();
    let value = report.db_after.database_value();
    let boundaries = [
        IndexBoundary::Eavt(IndexComponents::Four(
            first,
            AMOUNT,
            decimal("1.0"),
            IndexTransaction::T(2),
        )),
        IndexBoundary::Aevt(IndexComponents::Three(AMOUNT, first, decimal("1.0"))),
        IndexBoundary::Avet(IndexComponents::Two(AMOUNT, decimal("1.0"))),
        IndexBoundary::Vaet(IndexComponents::Three(Value::Ref(second), LINK, first)),
    ];

    for database in [
        value.clone(),
        value.clone().history(),
        value.clone().as_of(2),
        value.clone().since(1).as_of(3),
    ] {
        for boundary in &boundaries {
            assert_bidirectional_seek(&database, boundary);
        }
    }

    for boundary in [
        IndexBoundary::Eavt(IndexComponents::Empty),
        IndexBoundary::Aevt(IndexComponents::Empty),
        IndexBoundary::Avet(IndexComponents::Empty),
        IndexBoundary::Vaet(IndexComponents::Empty),
    ] {
        assert_bidirectional_seek(&value, &boundary);
        assert_bidirectional_seek(&value.clone().history(), &boundary);
    }
}

#[test]
fn reverse_filtered_seek_is_lazy_and_collapses_after_filtering() {
    use std::sync::atomic::{AtomicUsize, Ordering as AtomicOrdering};

    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            AMOUNT,
            Keyword::new("seek", "amount"),
            ValueType::BigDec,
            Cardinality::Many,
        ))
        .unwrap();
    let entity = make_eid(USER_PARTITION, 1).unwrap();
    let first = Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: AMOUNT,
                value: decimal("1.0").into(),
            }],
            1_000,
        )
        .unwrap()
        .db_after;
    let second = first
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: AMOUNT,
                value: Some(decimal("1.0").into()),
            }],
            2_000,
        )
        .unwrap()
        .db_after;
    let latest = second
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: AMOUNT,
                value: decimal("1.0").into(),
            }],
            3_000,
        )
        .unwrap()
        .db_after;
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&calls);
    let excluded_t = latest.basis_t();
    let filtered = latest.database_value().filter(move |_, datom| {
        // Raw reverse seek continues into earlier entities/attributes.
        // Isolate this history and use its actual T: schema installation
        // already consumed the initial transaction in Database::new.
        if datom.entity != entity || datom.attribute != AMOUNT {
            return false;
        }
        observed.fetch_add(1, AtomicOrdering::Relaxed);
        tx_to_t(datom.tx).unwrap() != excluded_t
    });
    let boundary = IndexBoundary::Eavt(IndexComponents::Three(entity, AMOUNT, decimal("1.0")));

    let mut cursor = filtered.reverse_seek_cursor(&boundary).unwrap();
    assert_eq!(calls.load(AtomicOrdering::Relaxed), 0);
    assert!(
        cursor.next().is_none(),
        "after filtering the newest assertion, the visible retraction hides the older assertion"
    );
    assert_eq!(calls.load(AtomicOrdering::Relaxed), 3);

    let history_calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&history_calls);
    let history = latest.database_value().history().filter(move |_, _| {
        observed.fetch_add(1, AtomicOrdering::Relaxed);
        true
    });
    let mut cursor = history.reverse_seek_cursor(&boundary).unwrap();
    assert_eq!(history_calls.load(AtomicOrdering::Relaxed), 0);
    assert!(cursor.next().unwrap().is_ok());
    assert_eq!(history_calls.load(AtomicOrdering::Relaxed), 1);
    drop(cursor);
    assert_eq!(history_calls.load(AtomicOrdering::Relaxed), 1);
}

#[test]
fn transaction_instant_is_resident_basis_metadata_across_clones_and_views() {
    let database = Database::bootstrap().unwrap();
    assert_eq!(database.database_value().last_tx_instant(), None);
    let first = database.with(&[], 1_234).unwrap();
    let value = first.db_after.database_value();
    let held = value.clone();
    let derived = value
        .clone()
        .as_of(0)
        .since(0)
        .history()
        .filter(|_, _| true)
        .with_read_observer(Arc::new(LogicalReadObserver::new(u64::MAX, u64::MAX)));
    assert_eq!(value.last_tx_instant(), Some(1_234));
    assert_eq!(held.last_tx_instant(), Some(1_234));
    assert_eq!(derived.last_tx_instant(), Some(1_234));
    assert_eq!(derived.basis_t(), value.basis_t());

    let next = first.db_after.with(&[], 2_345).unwrap();
    let overlay = overlay_for(&next, 2_345);
    assert_eq!(
        next.db_after.database_value().last_tx_instant(),
        Some(2_345)
    );
    assert_eq!(overlay.last_tx_instant(), Some(2_345));
    assert_eq!(
        overlay.clone().as_of(value.basis_t()).last_tx_instant(),
        Some(2_345)
    );
    assert_eq!(held.last_tx_instant(), Some(1_234));
    assert_eq!(held.basis_t(), value.basis_t());
}

#[test]
fn point_current_metadata_and_prefix_cursor_match_the_eager_oracle() {
    let database = Database::bootstrap().unwrap();
    let genesis = database.database_value();
    assert_eq!(genesis.eidx_frontier(), database.eidx_frontier());
    assert_eq!(genesis.last_tx_instant(), None);

    let entity = make_eid(USER_PARTITION, 42).unwrap();
    let report = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: DB_IDENT as u32,
                value: TxValue::Scalar(Value::Keyword(Keyword::new("cursor", "entity"))),
            }],
            1_234,
        )
        .unwrap();
    let value = report.db_after.database_value();
    assert_eq!(value.eidx_frontier(), report.db_after.eidx_frontier());
    assert_eq!(value.last_tx_instant(), Some(1_234));

    let prefix = IndexPrefix::Eavt {
        entity,
        attribute: None,
        value: None,
    };
    let lazy = value
        .current_prefix_cursor(&prefix)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_eq!(lazy, value.datoms_with_prefix(&prefix).unwrap());
    assert!(lazy.iter().all(|datom| datom.entity == entity));
}
