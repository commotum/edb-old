use atomic_core::{
    Attribute, Cardinality, DB_PART_USER, DB_PARTITION, DB_TX_INSTANT, Database, EntityIdentifier,
    IndexOrder, Keyword, MAX_PARTITION, Schema, TimePoint, USER_PARTITION, ValueType, eid_to_eidx,
    eid_to_part, make_eid, t_to_tx, tx_to_t,
};

fn database_with_duplicate_instants() -> Database {
    let one = Database::bootstrap()
        .unwrap()
        .with(&[], 100)
        .unwrap()
        .db_after;
    let two = one.with(&[], 100).unwrap().db_after;
    two.with(&[], 200).unwrap().db_after
}

fn visible_transaction_times(value: atomic_core::DatabaseValue) -> Vec<u64> {
    value
        .datoms(IndexOrder::Avet)
        .unwrap()
        .into_iter()
        .filter(|datom| datom.attribute == DB_TX_INSTANT as u32)
        .map(|datom| tx_to_t(datom.tx).unwrap())
        .collect()
}

#[test]
fn explicit_t_and_tx_are_exact_while_duplicate_instants_follow_recovered_first_match() {
    let database = database_with_duplicate_instants();
    let value = database.database_value();

    assert_eq!(value.resolve_time_point(TimePoint::T(2)).unwrap(), 2);
    assert_eq!(
        value
            .resolve_time_point(TimePoint::Tx(t_to_tx(2).unwrap()))
            .unwrap(),
        2
    );
    assert_eq!(value.resolve_time_point(TimePoint::Instant(99)).unwrap(), 0);
    assert_eq!(
        value.resolve_time_point(TimePoint::Instant(100)).unwrap(),
        1,
        "an exact duplicate millisecond resolves to the earliest matching transaction"
    );
    assert_eq!(
        value.resolve_time_point(TimePoint::Instant(150)).unwrap(),
        2
    );
    assert_eq!(
        value.resolve_time_point(TimePoint::Instant(200)).unwrap(),
        3
    );
    assert_eq!(
        value.resolve_time_point(TimePoint::Instant(201)).unwrap(),
        4,
        "an instant after the database resolves to next-T"
    );

    let invalid_tx = make_eid(USER_PARTITION, 2).unwrap();
    assert_eq!(
        value
            .resolve_time_point(TimePoint::Tx(invalid_tx))
            .unwrap_err()
            .code,
        "identity/not-a-transaction-id"
    );
}

#[test]
fn as_of_is_inclusive_and_since_is_exclusive_of_the_same_resolved_boundary() {
    let database = database_with_duplicate_instants();
    let value = database.database_value();

    let as_of_duplicate = value
        .clone()
        .as_of_time_point(TimePoint::Instant(100))
        .unwrap();
    let since_duplicate = value
        .clone()
        .since_time_point(TimePoint::Instant(100))
        .unwrap();
    assert_eq!(as_of_duplicate.as_of_t(), Some(1));
    assert_eq!(since_duplicate.since_t(), Some(1));
    assert_eq!(visible_transaction_times(as_of_duplicate), vec![1]);
    assert_eq!(visible_transaction_times(since_duplicate), vec![2, 3]);

    let as_of_t = value.clone().as_of_time_point(TimePoint::T(2)).unwrap();
    let since_tx = value
        .clone()
        .since_time_point(TimePoint::Tx(t_to_tx(2).unwrap()))
        .unwrap();
    assert_eq!(visible_transaction_times(as_of_t), vec![1, 2]);
    assert_eq!(visible_transaction_times(since_tx), vec![3]);

    // Time filters window datoms but retain the current basis' metadata.
    let past = value
        .clone()
        .as_of_time_point(TimePoint::Instant(99))
        .unwrap();
    assert_eq!(past.basis_t(), 3);
    assert_eq!(
        past.entid(&Keyword::new("db.part", "user")),
        Some(DB_PART_USER)
    );
    assert!(visible_transaction_times(past).is_empty());
}

#[test]
fn entid_at_uses_first_t_at_or_after_instant() {
    let database = database_with_duplicate_instants();
    let value = database.database_value();
    let user = EntityIdentifier::Ident(Keyword::new("db.part", "user"));

    for (time_point, expected_t) in [
        (TimePoint::T(2), 2),
        (TimePoint::Tx(t_to_tx(2).unwrap()), 2),
        (TimePoint::Instant(99), 1),
        (TimePoint::Instant(100), 1),
        (TimePoint::Instant(150), 3),
        (TimePoint::Instant(201), 4),
    ] {
        let boundary = value.entid_at(&user, time_point).unwrap();
        assert_eq!(eid_to_part(boundary).unwrap(), USER_PARTITION);
        assert_eq!(eid_to_eidx(boundary).unwrap(), expected_t);
    }

    assert_eq!(
        value
            .entid_at(
                &EntityIdentifier::Ident(Keyword::new("missing", "partition")),
                TimePoint::T(1),
            )
            .unwrap_err()
            .code,
        "database/unknown-partition"
    );
}

#[test]
fn entid_at_accepts_recovered_named_and_implicit_partition_shapes() {
    let database = database_with_duplicate_instants();
    let value = database.database_value();

    // A partition-zero entity contributes its eidx as the partition bits.
    for partition in [1_u32, 37, MAX_PARTITION] {
        let partition_entity = make_eid(DB_PARTITION, u64::from(partition)).unwrap();
        let boundary = value
            .entid_at(&EntityIdentifier::Id(partition_entity), TimePoint::T(2))
            .unwrap();
        assert_eq!(eid_to_part(boundary).unwrap(), partition);
        assert_eq!(eid_to_eidx(boundary).unwrap(), 2);
    }

    // An implicit/custom partition is already represented by its nonzero
    // partition base eid, whose eidx must be zero.
    let implicit_partition = 524_288 + 42;
    let implicit_base = make_eid(implicit_partition, 0).unwrap();
    let boundary = value
        .entid_at(&EntityIdentifier::Id(implicit_base), TimePoint::T(3))
        .unwrap();
    assert_eq!(eid_to_part(boundary).unwrap(), implicit_partition);
    assert_eq!(eid_to_eidx(boundary).unwrap(), 3);

    // `partbits` is shape-based after ordinary exact-value ident resolution;
    // it does not whitelist the built-in partition idents. Dynamic named
    // partition installation is a separate transaction capability, so an
    // installed attribute ident is the smallest available non-built-in
    // partition-zero witness.
    let named_ident = Keyword::new("partition", "orders");
    let mut named_schema = Schema::new();
    named_schema
        .install(Attribute::new(
            1_000,
            named_ident.clone(),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let named_value = Database::new(named_schema).unwrap().database_value();
    let boundary = named_value
        .entid_at(&EntityIdentifier::Ident(named_ident), TimePoint::T(2))
        .unwrap();
    assert_eq!(eid_to_part(boundary).unwrap(), 1_000);
    assert_eq!(eid_to_eidx(boundary).unwrap(), 2);
}

#[test]
fn entid_at_rejects_ids_that_are_not_partition_shaped() {
    let value = database_with_duplicate_instants().database_value();
    let ordinary_entity = make_eid(37, 1).unwrap();
    assert_eq!(
        value
            .entid_at(&EntityIdentifier::Id(ordinary_entity), TimePoint::T(1))
            .unwrap_err()
            .code,
        "database/not-a-partition"
    );

    let oversized_partition_zero_entity =
        make_eid(DB_PARTITION, u64::from(MAX_PARTITION) + 1).unwrap();
    assert_eq!(
        value
            .entid_at(
                &EntityIdentifier::Id(oversized_partition_zero_entity),
                TimePoint::T(1),
            )
            .unwrap_err()
            .code,
        "database/not-a-partition"
    );
}
