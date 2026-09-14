//! Partition policies affect fresh entity placement, not identity or access.
use atomic_core::{
    Attribute, AttributeRef, Cardinality, DB_IDENT, DB_INSTALL_PARTITION, DB_PART_DB, DB_PART_TX,
    Database, DatabaseValue, EntityMap, EntityRef, IndexOrder, Keyword, MapValue, Peer, Schema,
    TransactionRequest, TxForm, TxFunctions, TxOp, TxValue, USER_PARTITION, Unique, Value,
    ValueType, eid_to_eidx, eid_to_part, implicit_part, implicit_part_id, make_eid, partition_eid,
    t_to_tx,
};
use std::collections::BTreeMap;
use std::time::Duration;

mod common;

const KEY: u32 = 1_000;
const PAYLOAD: u32 = 1_001;
const CHILDREN: u32 = 1_002;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                KEY,
                Keyword::new("item", "key"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            PAYLOAD,
            Keyword::new("item", "payload"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                CHILDREN,
                Keyword::new("item", "children"),
                ValueType::Ref,
                Cardinality::Many,
            )
            .component(),
        )
        .unwrap();
    schema
}

fn named_ident() -> Keyword {
    Keyword::new("app.part", "orders")
}

fn add(tempid: &str, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(tempid.into()),
        attribute,
        value: value.into(),
    }
}

fn force(tempid: &str, partition: EntityRef) -> TxOp {
    TxOp::ForcePartition {
        tempid: tempid.into(),
        partition,
    }
}

fn affinity(tempid: &str, entity: EntityRef) -> TxOp {
    TxOp::MatchPartition {
        tempid: tempid.into(),
        entity,
    }
}

fn named_install() -> Vec<TxForm> {
    vec![TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Temp("orders-partition".into())),
        attributes: vec![
            (
                AttributeRef::Ident(Keyword::new("db", "ident")),
                TxValue::Scalar(Value::Keyword(named_ident())).into(),
            ),
            (
                AttributeRef::ReverseIdent(Keyword::new("db.install", "partition")),
                TxValue::Entity(EntityRef::Id(DB_PART_DB)).into(),
            ),
        ],
    })]
}

fn allocations() -> Vec<TxForm> {
    let mut forms: Vec<_> = [
        add("customer", KEY, Value::String("customer-key".into())),
        add("order", PAYLOAD, Value::Long(10)),
        add("line-one", PAYLOAD, Value::Long(11)),
        add("line-two", PAYLOAD, Value::Long(12)),
        add("default", PAYLOAD, Value::Long(13)),
        add("transaction-metadata", PAYLOAD, Value::Long(30)),
        force("transaction-metadata", EntityRef::Id(DB_PART_TX)),
        force("customer", EntityRef::Id(implicit_part(500).unwrap())),
        force("order", EntityRef::Ident(named_ident())),
        // Explicit force wins over affinity, as recovered getPart specifies.
        affinity("order", EntityRef::Temp("customer".into())),
        affinity("line-one", EntityRef::Temp("order".into())),
        affinity("line-two", EntityRef::Temp("line-one".into())),
        force("cart", EntityRef::Id(implicit_part(777).unwrap())),
    ]
    .into_iter()
    .map(TxForm::Op)
    .collect();
    forms.push(TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Temp("cart".into())),
        attributes: vec![
            (
                AttributeRef::Id(PAYLOAD),
                TxValue::Scalar(Value::Long(20)).into(),
            ),
            (
                AttributeRef::Id(CHILDREN),
                MapValue::Nested(Box::new(EntityMap {
                    id: None,
                    attributes: vec![
                        (
                            AttributeRef::Id(PAYLOAD),
                            TxValue::Scalar(Value::Long(21)).into(),
                        ),
                        (
                            AttributeRef::Id(CHILDREN),
                            MapValue::Nested(Box::new(EntityMap {
                                id: None,
                                attributes: vec![(
                                    AttributeRef::Id(PAYLOAD),
                                    TxValue::Scalar(Value::Long(22)).into(),
                                )],
                            })),
                        ),
                    ],
                })),
            ),
        ],
    }));
    forms
}

fn assert_allocations(database: &DatabaseValue, tempids: &BTreeMap<String, u64>) {
    let named = database.entid(&named_ident()).unwrap();
    assert_eq!(eid_to_part(named).unwrap(), 0);
    assert!(
        database
            .values(DB_PART_DB, DB_INSTALL_PARTITION as u32)
            .unwrap()
            .contains(&Value::Ref(named))
    );
    for name in ["order", "line-one", "line-two"] {
        assert_eq!(partition_eid(tempids[name]).unwrap(), named);
    }
    assert_eq!(
        partition_eid(tempids["customer"]).unwrap(),
        implicit_part(500).unwrap()
    );
    assert_eq!(eid_to_part(tempids["default"]).unwrap(), USER_PARTITION);
    assert_eq!(
        partition_eid(tempids["transaction-metadata"]).unwrap(),
        DB_PART_TX
    );
    assert_eq!(
        database
            .values(tempids["transaction-metadata"], PAYLOAD)
            .unwrap(),
        vec![Value::Long(30)]
    );
    let mut entity = tempids["cart"];
    for expected_payload in [20, 21, 22] {
        assert_eq!(partition_eid(entity).unwrap(), implicit_part(777).unwrap());
        assert_eq!(
            database.values(entity, PAYLOAD).unwrap(),
            vec![Value::Long(expected_payload)]
        );
        if expected_payload != 22 {
            let children = database.values(entity, CHILDREN).unwrap();
            assert_eq!(children.len(), 1);
            let Value::Ref(child) = children[0] else {
                panic!("component must be a ref")
            };
            entity = child;
        }
    }
}

fn upsert_existing() -> Vec<TxOp> {
    vec![
        add("same-customer", KEY, Value::String("customer-key".into())),
        add("same-customer", PAYLOAD, Value::Long(99)),
        force("same-customer", EntityRef::Id(implicit_part(888).unwrap())),
        add("follower", PAYLOAD, Value::Long(100)),
        affinity("follower", EntityRef::Temp("same-customer".into())),
        add("lookup-follower", PAYLOAD, Value::Long(101)),
        affinity(
            "lookup-follower",
            EntityRef::Lookup {
                attribute: KEY,
                value: Value::String("customer-key".into()),
            },
        ),
    ]
}

fn rejected_policies() -> Vec<(&'static str, Vec<TxOp>)> {
    let x = || add("x", PAYLOAD, Value::Long(1));
    let y = || add("y", PAYLOAD, Value::Long(2));
    vec![
        (
            "unknown named partition",
            vec![
                x(),
                force("x", EntityRef::Ident(Keyword::new("app.part", "missing"))),
            ],
        ),
        (
            "ordinary entity is not a partition",
            vec![
                x(),
                force("x", EntityRef::Id(make_eid(USER_PARTITION, 42).unwrap())),
            ],
        ),
        (
            "implicit partition nonzero eidx",
            vec![
                x(),
                force("x", EntityRef::Id(implicit_part(10).unwrap() + 1)),
            ],
        ),
        (
            "malformed permanent id",
            vec![x(), force("x", EntityRef::Id(u64::MAX))],
        ),
        (
            "contradictory force",
            vec![
                x(),
                force("x", EntityRef::Id(implicit_part(1).unwrap())),
                force("x", EntityRef::Id(implicit_part(2).unwrap())),
            ],
        ),
        (
            "contradictory affinity",
            vec![
                x(),
                y(),
                add("z", PAYLOAD, Value::Long(3)),
                affinity("x", EntityRef::Temp("y".into())),
                affinity("x", EntityRef::Temp("z".into())),
            ],
        ),
        (
            "unified identities request distinct partitions",
            vec![
                add("x", KEY, Value::String("same-new-identity".into())),
                add("y", KEY, Value::String("same-new-identity".into())),
                force("x", EntityRef::Id(implicit_part(1).unwrap())),
                force("y", EntityRef::Id(implicit_part(2).unwrap())),
            ],
        ),
        (
            "policy does not create an otherwise unused tempid",
            vec![force("unused", EntityRef::Id(implicit_part(1).unwrap()))],
        ),
        (
            "unanchored cycle",
            vec![
                x(),
                y(),
                affinity("x", EntityRef::Temp("y".into())),
                affinity("y", EntityRef::Temp("x".into())),
            ],
        ),
    ]
}

#[test]
fn implicit_partition_boundaries_round_trip_without_allocating_entities() {
    for index in [0, 1, 500, 524_287] {
        let partition = implicit_part(index).unwrap();
        assert_eq!(implicit_part_id(partition).unwrap(), Some(index));
        assert_eq!(eid_to_eidx(partition).unwrap(), 0);
        assert_eq!(partition_eid(partition).unwrap(), partition);
    }
    assert!(implicit_part(524_288).is_err());
    assert_eq!(implicit_part_id(DB_PART_DB).unwrap(), None);
}

#[test]
fn pure_partition_policies_share_branches_and_preserve_existing_identity() {
    let initial = Database::new(schema()).unwrap();
    let named = initial
        .with_forms(&named_install(), &TxFunctions::new(), 1_000)
        .unwrap();
    let installed = initial
        .database_value()
        .with_forms(&named_install(), 1_000)
        .unwrap();
    common::assert_same_information(&installed.db_after, &named.db_after);
    assert_eq!(installed.tempids, named.tempids);
    assert!(initial.entid(&named_ident()).is_none());
    let forms = allocations();
    let expected = named
        .db_after
        .with_forms(&forms, &TxFunctions::new(), 2_000)
        .unwrap();
    let actual = installed.db_after.with_forms(&forms, 2_000).unwrap();
    assert_eq!(actual.tempids, expected.tempids);
    assert_eq!(actual.tx_data, expected.tx_data);
    common::assert_same_information(&actual.db_after, &expected.db_after);
    assert_allocations(&actual.db_after, &actual.tempids);
    assert_eq!(
        actual.tempids["transaction-metadata"],
        t_to_tx(actual.db_after.basis_t()).unwrap()
    );
    let mut reversed = forms;
    reversed.reverse();
    let sibling = installed.db_after.with_forms(&reversed, 2_000).unwrap();
    assert_eq!(sibling.tempids, actual.tempids);
    assert_eq!(sibling.tx_data, actual.tx_data);
    // These speculative siblings share an issued frontier, not reservations.
    assert_eq!(installed.db_after.basis_t(), named.db_after.basis_t());

    let upsert = actual.db_after.with(&upsert_existing(), 3_000).unwrap();
    let pure_upsert = expected.db_after.with(&upsert_existing(), 3_000).unwrap();
    common::assert_same_information(&upsert.db_after, &pure_upsert.db_after);
    assert_eq!(upsert.tempids["same-customer"], actual.tempids["customer"]);
    for name in ["same-customer", "lookup-follower"] {
        assert_eq!(
            partition_eid(upsert.tempids[name]).unwrap(),
            implicit_part(500).unwrap()
        );
    }
    // Affinity to a transaction tempid follows its allocation policy before
    // upsert, not the resolved existing ID. Use an existing ID/lookup target
    // when actual existing placement is the desired policy.
    assert_eq!(
        partition_eid(upsert.tempids["follower"]).unwrap(),
        implicit_part(888).unwrap()
    );
    assert!(
        actual
            .db_after
            .values(actual.tempids["customer"], PAYLOAD)
            .unwrap()
            .is_empty()
    );
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            upsert
                .db_after
                .clone()
                .as_of(actual.db_after.basis_t())
                .collect_datoms(order)
                .unwrap(),
            actual.db_after.collect_datoms(order).unwrap()
        );
    }

    for (label, ops) in rejected_policies() {
        let eager_error = expected.db_after.with(&ops, 3_000).expect_err(label);
        let native_error = actual.db_after.with(&ops, 3_000).expect_err(label);
        assert_eq!(
            (native_error.category, native_error.code),
            (eager_error.category, eager_error.code),
            "{label}"
        );
    }
    let anchored_cycle = [
        add("x", PAYLOAD, Value::Long(1)),
        add("y", PAYLOAD, Value::Long(2)),
        force("x", EntityRef::Id(implicit_part(1).unwrap())),
        affinity("x", EntityRef::Temp("y".into())),
        affinity("y", EntityRef::Temp("x".into())),
    ];
    let anchored = actual.db_after.with(&anchored_cycle, 3_000).unwrap();
    assert_eq!(
        partition_eid(anchored.tempids["x"]).unwrap(),
        implicit_part(1).unwrap()
    );
    assert_eq!(
        partition_eid(anchored.tempids["y"]).unwrap(),
        implicit_part(1).unwrap()
    );
    let unified = actual
        .db_after
        .with(
            &[
                add("x", KEY, Value::String("same-new-identity".into())),
                add("y", KEY, Value::String("same-new-identity".into())),
                force("x", EntityRef::Id(implicit_part(1).unwrap())),
            ],
            3_000,
        )
        .unwrap();
    assert_eq!(unified.tempids["x"], unified.tempids["y"]);
    assert_eq!(
        partition_eid(unified.tempids["x"]).unwrap(),
        implicit_part(1).unwrap()
    );
}

#[test]
fn native_partition_forms_survive_retries_retention_index_interruption_and_recovery() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required for partition lifecycle");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "partition_lifecycle");
    let connection = &fixture.connection;
    common::install(connection).unwrap();
    let database_id = "partition-lifecycle";
    let mut store = common::TestStore::connect(connection).unwrap();
    store.create_database(database_id, schema()).unwrap();
    let mut eager = Database::new(schema()).unwrap();
    let service = common::start_service(connection, database_id);
    let peer = Peer::connect(connection, database_id, 32).unwrap();
    let original = peer.database_value();
    let mut retained = vec![(original.clone(), eager.clone())];
    let mut retry_request = None;
    let mut allocation_ids = BTreeMap::new();
    let mut expected_retry = None;
    for (step, forms) in [named_install(), allocations()].into_iter().enumerate() {
        let instant = (step as i64 + 1) * 1_000;
        let request =
            TransactionRequest::from_forms(format!("partition-forms-{step}"), forms.clone())
                .comparing_basis(eager.basis_t())
                .with_tx_instant(instant);
        let expected = eager
            .with_forms(&forms, &TxFunctions::new(), instant)
            .unwrap();
        let speculative = peer.database_value().with_forms(&forms, instant).unwrap();
        let report = service
            .client()
            .transact(request.clone(), Duration::from_secs(5))
            .unwrap();
        assert_eq!(report.tempids, expected.tempids);
        assert_eq!(report.tempids, speculative.tempids);
        assert_eq!(report.tx_data, expected.tx_data);
        common::assert_same_information(&report.db_after, &expected.db_after);
        eager = expected.db_after;
        peer.sync_to(report.basis_t, Duration::from_secs(5))
            .unwrap();
        retained.push((report.db_after.clone(), eager.clone()));
        if step == 1 {
            assert_allocations(&report.db_after, &report.tempids);
            assert_eq!(
                report.tempids["transaction-metadata"],
                t_to_tx(report.basis_t).unwrap()
            );
            allocation_ids = report.tempids.clone();
            retry_request = Some(request);
            expected_retry = Some(report);
        }
    }
    for (ordinal, (label, ops)) in rejected_policies().into_iter().enumerate() {
        let expected = eager.with(&ops, 3_000).expect_err(label);
        let speculative = peer.database_value().with(&ops, 3_000).expect_err(label);
        let actual = common::try_transact(
            &service,
            &format!("invalid-partition-{ordinal}"),
            eager.basis_t(),
            &ops,
            3_000,
        )
        .expect_err(label);
        assert_eq!(
            (actual.category, actual.code),
            (expected.category, expected.code),
            "{label}"
        );
        assert_eq!(
            (speculative.category, speculative.code),
            (expected.category, expected.code),
            "{label}"
        );
        assert_eq!(peer.sync().unwrap().basis_t(), eager.basis_t());
    }
    let expected = eager.with(&upsert_existing(), 3_000).unwrap();
    let report = common::transact(
        &service,
        "partition-upsert",
        eager.basis_t(),
        &upsert_existing(),
        3_000,
    );
    assert_eq!(report.tempids, expected.tempids);
    assert_eq!(report.tempids["same-customer"], allocation_ids["customer"]);
    common::assert_same_information(&report.db_after, &expected.db_after);
    eager = expected.db_after;

    // Installing an existing system entity must use its resident authenticated
    // ident, without requiring the application to reassert that ident.
    let delayed_ident = Keyword::new("app.part", "delayed");
    for (phase, ops) in [
        vec![
            add(
                "delayed-partition",
                DB_IDENT as u32,
                Value::Keyword(delayed_ident.clone()),
            ),
            force("delayed-partition", EntityRef::Id(DB_PART_DB)),
        ],
        vec![TxOp::Add {
            entity: EntityRef::Id(DB_PART_DB),
            attribute: DB_INSTALL_PARTITION as u32,
            value: TxValue::Entity(EntityRef::Ident(delayed_ident.clone())),
        }],
    ]
    .into_iter()
    .enumerate()
    {
        let instant = 4_000 + phase as i64 * 1_000;
        let before = peer.sync().unwrap();
        let expected = eager.with(&ops, instant).unwrap();
        let speculative = before.with(&ops, instant).unwrap();
        let report = common::transact(
            &service,
            &format!("delayed-partition-{phase}"),
            eager.basis_t(),
            &ops,
            instant,
        );
        assert_eq!(report.tempids, expected.tempids);
        assert_eq!(report.tempids, speculative.tempids);
        common::assert_same_information(&report.db_after, &expected.db_after);
        common::assert_same_information(&report.db_after, &speculative.db_after);
        assert_eq!(
            report
                .db_after
                .schema()
                .partitions()
                .any(|(_, ident)| ident == &delayed_ident),
            phase == 1
        );
        eager = expected.db_after;
        retained.push((report.db_after, eager.clone()));
    }
    service.shutdown();

    // Interrupted publication is covered by storage::index_publication::tests.
    // This fixture retains partition semantics and exact values across indexing.
    common::assert_same_information(&store.recover(database_id).unwrap(), &eager);
    common::consolidate(connection, database_id).unwrap();
    let restarted_peer = Peer::connect(connection, database_id, 0).unwrap();
    common::assert_same_information(&restarted_peer.database_value(), &eager);
    assert_allocations(&restarted_peer.database_value(), &allocation_ids);
    let restarted_service = common::start_service(connection, database_id);
    let replay = restarted_service
        .client()
        .transact(retry_request.unwrap(), Duration::from_secs(5))
        .unwrap();
    let expected_retry = expected_retry.unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, expected_retry.tx_hash);
    assert_eq!(replay.tempids, expected_retry.tempids);
    assert_eq!(replay.tx_data, expected_retry.tx_data);
    common::assert_same_information(&replay.db_before, &expected_retry.db_before);
    common::assert_same_information(&replay.db_after, &expected_retry.db_after);
    for (value, expected) in retained {
        common::assert_same_information(&value, &expected);
    }
    assert!(original.entid(&named_ident()).is_none());
    restarted_service.shutdown();
}
