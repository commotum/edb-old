use super::*;

#[test]
fn transaction_prefix_memo_admits_only_complete_results_and_replays_with_charge() {
    let database = Database::bootstrap().unwrap();
    let entity = make_eid(USER_PARTITION, 42).unwrap();
    let report = database
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("memo", "entity")).into(),
            }],
            1_234,
        )
        .unwrap();
    let context = Arc::new(TransactionReadContext::new(8, u64::MAX));
    let value = report
        .db_after
        .database_value()
        .with_transaction_read_context(Arc::clone(&context));
    let prefix = IndexPrefix::Eavt {
        entity,
        attribute: Some(DB_IDENT as u32),
        value: None,
    };

    let mut partial = value.current_prefix_cursor(&prefix).unwrap();
    assert!(partial.next().transpose().unwrap().is_some());
    drop(partial);
    let partial_work = context.snapshot().unwrap();
    assert_eq!(partial_work.memo_admissions, 0);
    assert_eq!(partial_work.memo_rejections, 1);
    assert_eq!(partial_work.prefix_misses, 1);

    let mut complete_cursor = value.current_prefix_cursor(&prefix).unwrap();
    let complete = complete_cursor
        .by_ref()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert!(
        complete_cursor
            .memo_source
            .as_ref()
            .is_some_and(|source| source.completed && source.key.is_none()),
        "successful admission must move the owned key into the memo"
    );
    assert_eq!(complete.len(), 1);
    let admitted = context.snapshot().unwrap();
    assert_eq!(admitted.memo_admissions, 1);
    assert_eq!(admitted.source_datoms, 2);

    let replayed = value
        .current_prefix_cursor(&prefix)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_eq!(replayed, complete);
    let replay_work = context.snapshot().unwrap();
    assert_eq!(replay_work.prefix_hits, 1);
    assert_eq!(replay_work.source_datoms, admitted.source_datoms);
    assert_eq!(replay_work.logical_datoms, admitted.logical_datoms + 1);

    // History is a separate view coordinate over the same immutable
    // value identity, so independently derived history values can share.
    let history = value.clone().history();
    let first_history = history.datoms_with_prefix(&prefix).unwrap();
    let source_after_history = context.snapshot().unwrap().source_datoms;
    assert_eq!(
        value.clone().history().datoms_with_prefix(&prefix).unwrap(),
        first_history
    );
    assert_eq!(
        context.snapshot().unwrap().source_datoms,
        source_after_history
    );

    assert!(
        value
            .clone()
            .without_transaction_read_context()
            .transaction_read_context()
            .is_none()
    );
}

#[test]
fn empty_prefix_memo_entries_obey_the_entry_bound() {
    let context = Arc::new(TransactionReadContext::new(2, u64::MAX));
    let value = Database::bootstrap()
        .unwrap()
        .database_value()
        .with_transaction_read_context(Arc::clone(&context));
    for entity in 10_000..10_003 {
        assert!(
            value
                .datoms_with_prefix(&IndexPrefix::Eavt {
                    entity,
                    attribute: None,
                    value: None,
                })
                .unwrap()
                .is_empty()
        );
    }
    let work = context.snapshot().unwrap();
    assert_eq!(work.memo_admissions, 2);
    assert_eq!(work.memo_rejections, 1);
    assert_eq!(work.memo_peak_entries, 2);
    assert_eq!(
        work.memo_peak_retained_bytes,
        2 * std::mem::size_of::<PrefixMemoKey>() as u64
    );
}

#[test]
fn oversized_empty_prefix_key_is_not_retained_by_the_memo() {
    let context = Arc::new(TransactionReadContext::new(8, 1_024));
    let value = Database::bootstrap()
        .unwrap()
        .database_value()
        .with_transaction_read_context(Arc::clone(&context));
    let prefix = IndexPrefix::Eavt {
        entity: 10_000,
        attribute: Some(DB_IDENT as u32),
        value: Some(Value::String("x".repeat(4_096))),
    };

    for _ in 0..2 {
        assert!(value.datoms_with_prefix(&prefix).unwrap().is_empty());
    }

    let work = context.snapshot().unwrap();
    assert_eq!(work.prefix_misses, 2);
    assert_eq!(work.prefix_hits, 0);
    assert_eq!(work.memo_admissions, 0);
    assert_eq!(work.memo_rejections, 2);
    assert_eq!(work.memo_peak_entries, 0);
    assert_eq!(work.memo_peak_retained_bytes, 0);
}

#[test]
fn capacity_failed_prefix_is_never_admitted() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            AMOUNT,
            Keyword::new("memo", "amount"),
            ValueType::BigDec,
            Cardinality::Many,
        ))
        .unwrap();
    let entity = make_eid(USER_PARTITION, 1).unwrap();
    let database = Database::new(schema)
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: decimal("1.0").into(),
                },
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: AMOUNT,
                    value: decimal("2.0").into(),
                },
            ],
            1_000,
        )
        .unwrap()
        .db_after;
    let context = Arc::new(TransactionReadContext::new(1, u64::MAX));
    let value = database
        .database_value()
        .with_transaction_read_context(Arc::clone(&context));
    let error = value
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity,
            attribute: Some(AMOUNT),
            value: None,
        })
        .unwrap_err();
    assert_eq!(error.code, "transaction/read-capacity");
    let work = context.snapshot().unwrap();
    assert_eq!(work.logical_datoms, 1);
    assert_eq!(work.memo_admissions, 0);
    assert_eq!(work.memo_rejections, 1);
}

#[test]
fn temporal_prefix_stream_charges_only_yields_and_does_not_memoize_raw_history() {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            AMOUNT,
            Keyword::new("window", "amount"),
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
                value: decimal("2.0").into(),
            }],
            1_000,
        )
        .unwrap();
    let retracted = first
        .db_after
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: AMOUNT,
                value: Some(decimal("2.0").into()),
            }],
            2_000,
        )
        .unwrap();
    let latest = retracted
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: AMOUNT,
                value: decimal("2.0").into(),
            }],
            3_000,
        )
        .unwrap()
        .db_after;
    let context = Arc::new(TransactionReadContext::new(1, u64::MAX));
    let value = latest
        .database_value()
        .with_transaction_read_context(Arc::clone(&context))
        .filter(|_, datom| tx_to_t(datom.tx).unwrap() != 3);
    let prefix = IndexPrefix::Eavt {
        entity,
        attribute: Some(AMOUNT),
        value: Some(decimal("2.0")),
    };

    let mut cursor = value.prefix_cursor(&prefix).unwrap();
    assert_eq!(context.snapshot().unwrap().logical_datoms, 0);
    assert!(cursor.next().unwrap().is_ok());
    let after_one = context.snapshot().unwrap();
    assert_eq!(after_one.logical_datoms, 1);
    assert_eq!(after_one.memo_admissions, 0);
    assert_eq!(after_one.memo_rejections, 0);
    let error = cursor.next().unwrap().unwrap_err();
    assert_eq!(error.code, "transaction/read-capacity");
    assert_eq!(context.snapshot().unwrap().logical_datoms, 1);
}

#[test]
fn prefix_cursor_rejects_non_current_database_values() {
    let value = Database::bootstrap().unwrap().database_value().history();
    assert_eq!(
        value
            .current_prefix_cursor(&IndexPrefix::Aevt {
                attribute: DB_IDENT as u32,
                entity: None,
                value: None,
            })
            .err()
            .expect("history values must reject the point-current cursor")
            .code,
        "database/prefix-cursor-requires-current"
    );
}

#[test]
fn observed_overlay_prefix_stops_at_the_logical_read_cap() {
    const TAG: u32 = 1_000;
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            TAG,
            Keyword::new("read-cap", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    let database = Database::new(schema).unwrap();
    let entity = make_eid(USER_PARTITION, 1).unwrap();
    let ops = (0..256)
        .map(|value| TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: TAG,
            value: Value::String(format!("tag-{value:03}")).into(),
        })
        .collect::<Vec<_>>();
    let seeded = database.with(&ops, 1_000).unwrap().db_after;
    let proposed = seeded.with(&[], 2_000).unwrap();
    let observer = Arc::new(LogicalReadObserver::new(2, u64::MAX));
    let overlay = overlay_for(&proposed, 2_000).with_read_observer(Arc::clone(&observer));

    let error = overlay
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity,
            attribute: Some(TAG),
            value: None,
        })
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Busy, "transaction/read-capacity")
    );
    assert_eq!(
        observer.snapshot().unwrap().datoms,
        2,
        "the cursor must charge successful yields one at a time; a late whole-prefix charge leaves this at zero"
    );
}
