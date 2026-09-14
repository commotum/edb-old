use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, IndexOrder, IndexPrefix, Keyword, Schema, TxOp,
    TxValue, USER_PARTITION, Value, ValueType, make_eid,
};
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};

const TAG: u32 = 1_000;

fn long_history() -> (Database, u64) {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            TAG,
            Keyword::new("stream", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    let mut database = Database::new(schema).unwrap();
    let entity = make_eid(USER_PARTITION, 42).unwrap();
    for ordinal in 0..97 {
        let operation = if ordinal % 2 == 0 {
            TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: TAG,
                value: TxValue::Scalar(Value::String("constant".into())),
            }
        } else {
            TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: TAG,
                value: Some(TxValue::Scalar(Value::String("constant".into()))),
            }
        };
        database = database
            .with(&[operation], i64::from(ordinal) + 1)
            .unwrap()
            .db_after;
    }
    (database, entity)
}

#[test]
fn temporal_prefix_cursor_does_no_filter_work_until_consumed_and_stops_after_one_yield() {
    let (database, entity) = long_history();
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&calls);
    let value = database
        .database_value()
        .as_of(database.basis_t())
        .filter(move |_, _| {
            observed.fetch_add(1, Ordering::Relaxed);
            true
        });
    let prefix = IndexPrefix::Eavt {
        entity,
        attribute: Some(TAG),
        value: Some(Value::String("constant".into())),
    };

    let mut cursor = value.prefix_cursor(&prefix).unwrap();
    assert_eq!(calls.load(Ordering::Relaxed), 0);
    let first = cursor.next().unwrap().unwrap();
    assert!(first.added);
    assert_eq!(calls.load(Ordering::Relaxed), 1);
    drop(cursor);
    assert_eq!(
        calls.load(Ordering::Relaxed),
        1,
        "dropping a partially consumed cursor must not drain its history source"
    );
}

#[test]
fn filtered_history_scan_borrows_the_eager_index_and_evaluates_one_item_at_a_time() {
    let (database, _) = long_history();
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&calls);
    let value = database.database_value().history().filter(move |_, _| {
        observed.fetch_add(1, Ordering::Relaxed);
        true
    });

    let mut cursor = value.scan_cursor(IndexOrder::Eavt).unwrap();
    assert_eq!(calls.load(Ordering::Relaxed), 0);
    assert!(cursor.next().unwrap().is_ok());
    assert_eq!(calls.load(Ordering::Relaxed), 1);
    drop(cursor);
    assert_eq!(calls.load(Ordering::Relaxed), 1);
}

#[test]
fn collecting_names_are_exact_conveniences_over_streaming_results() {
    let (database, entity) = long_history();
    let value = database.database_value().since(32).as_of(80);
    let prefix = IndexPrefix::Eavt {
        entity,
        attribute: Some(TAG),
        value: None,
    };
    let streamed = value
        .prefix_cursor(&prefix)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_eq!(streamed, value.collect_datoms_with_prefix(&prefix).unwrap());
}
