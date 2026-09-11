use atomic_core::{
    Attribute, AttributeRef, Cardinality, Database, EntityMap, EntityRef, IndexOrder, Keyword,
    MapValue, Schema, TupleSpec, TxForm, TxFunctions, TxOp, TxValue, Unique, Value, ValueType,
};
use std::time::Duration;

mod common;

const KEY: u32 = 1_000;
const PAIR: u32 = 1_001;
const REFS: u32 = 1_002;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                KEY,
                Keyword::new("target", "key"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(
            Attribute::new(
                PAIR,
                Keyword::new("owner", "pair"),
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Heterogeneous(vec![
                ValueType::Ref,
                ValueType::Long,
            ]))
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(
            Attribute::new(
                REFS,
                Keyword::new("owner", "refs"),
                ValueType::Tuple,
                Cardinality::Many,
            )
            .tuple(TupleSpec::Homogeneous(ValueType::Ref)),
        )
        .unwrap();
    schema
}

fn pair(reference: EntityRef) -> TxValue {
    pair_at(reference, 7)
}

fn pair_at(reference: EntityRef, number: i64) -> TxValue {
    TxValue::Tuple(vec![
        Some(TxValue::Entity(reference)),
        Some(Value::Long(number).into()),
    ])
}

fn permutations<T: Clone>(values: &[T]) -> Vec<Vec<T>> {
    if values.is_empty() {
        return vec![Vec::new()];
    }
    let mut result = Vec::new();
    for index in 0..values.len() {
        let mut remainder = values.to_vec();
        let first = remainder.remove(index);
        for mut rest in permutations(&remainder) {
            rest.insert(0, first.clone());
            result.push(rest);
        }
    }
    result
}

#[test]
fn tuple_identity_is_independent_of_transaction_form_order() {
    let database = Database::new(schema()).unwrap();
    let expected = database.with(&ops(), 10).unwrap();
    for reordered in permutations(&ops()) {
        let eager = database.with(&reordered, 10).unwrap();
        let native = database.database_value().with(&reordered, 10).unwrap();
        assert_eq!(eager.tempids, expected.tempids);
        assert_eq!(native.tempids, expected.tempids);
        assert_eq!(native.tx_data, expected.tx_data);
        common::assert_same_information(&native.db_after, &expected.db_after);
        common::assert_same_information(&eager.db_after, &expected.db_after);
    }
}

#[test]
fn distinct_symbolic_tuple_keys_cannot_leave_duplicate_stored_identities() {
    let database = Database::new(schema()).unwrap();
    let original_basis = database.basis_t();
    let aliases = [
        TxOp::Add {
            entity: EntityRef::Temp("target-left".into()),
            attribute: KEY,
            value: Value::String("same-target".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("target-right".into()),
            attribute: KEY,
            value: Value::String("same-target".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("owner-left".into()),
            attribute: PAIR,
            value: pair(EntityRef::Temp("target-left".into())),
        },
        TxOp::Add {
            entity: EntityRef::Temp("owner-right".into()),
            attribute: PAIR,
            value: pair(EntityRef::Temp("target-right".into())),
        },
    ];
    // get-ids groups raw keys before ProcessExpander replaces tuple tempids.
    // Separate target aliases can thus converge after owner allocation. Do
    // not invent a recursive identity solver as a compatibility requirement:
    // a deterministic, atomic uniqueness conflict preserves this boundary.
    let expected = database.with(&aliases, 10).unwrap_err();
    assert_eq!(expected.category, atomic_core::ErrorCategory::Conflict);
    for reordered in permutations(&aliases) {
        let eager = database.with(&reordered, 10).unwrap_err();
        let native = database.database_value().with(&reordered, 10).unwrap_err();
        assert_eq!(
            (eager.category, eager.code),
            (expected.category, expected.code)
        );
        assert_eq!(
            (native.category, native.code),
            (expected.category, expected.code)
        );
    }
    assert_eq!(database.basis_t(), original_basis);
}

#[test]
fn tuple_cas_and_retraction_resolve_refs_and_preserve_stored_tuple_inputs() {
    let initial = Database::new(schema()).unwrap().with(&ops(), 10).unwrap();
    let owner = initial.tempids["left"];
    let target = initial.tempids["target"];
    let lookup = EntityRef::Lookup {
        attribute: KEY,
        value: Value::String("target".into()),
    };
    let changes = [
        TxOp::Cas {
            entity: EntityRef::Id(owner),
            attribute: PAIR,
            old: Some(pair(lookup.clone())),
            new: pair_at(EntityRef::Ident(Keyword::new("target", "ident")), 8),
        },
        TxOp::Add {
            entity: EntityRef::Id(owner),
            attribute: REFS,
            value: TxValue::Tuple(vec![Some(TxValue::Entity(lookup.clone())), None]),
        },
        TxOp::Add {
            entity: EntityRef::Id(owner),
            attribute: REFS,
            value: Value::Tuple(vec![None, Some(Value::Ref(target))]).into(),
        },
    ];
    let expected = initial.db_after.with(&changes, 11).unwrap();
    let actual = initial
        .db_after
        .database_value()
        .with(&changes, 11)
        .unwrap();
    common::assert_same_information(&actual.db_after, &expected.db_after);
    assert_eq!(
        actual.db_after.values(owner, PAIR).unwrap(),
        [Value::Tuple(vec![
            Some(Value::Ref(target)),
            Some(Value::Long(8))
        ])]
    );
    let retractions = [
        // The old numeric-only representation and the new symbolic tuple
        // input both continue to address exactly the same stored fact.
        TxOp::Cas {
            entity: EntityRef::Id(owner),
            attribute: PAIR,
            old: Some(Value::Tuple(vec![Some(Value::Ref(target)), Some(Value::Long(8))]).into()),
            new: pair_at(lookup.clone(), 9),
        },
        TxOp::Retract {
            entity: EntityRef::Id(owner),
            attribute: REFS,
            value: Some(TxValue::Tuple(vec![Some(TxValue::Entity(lookup)), None])),
        },
        TxOp::Retract {
            entity: EntityRef::Id(owner),
            attribute: REFS,
            value: Some(Value::Tuple(vec![None, Some(Value::Ref(target))]).into()),
        },
    ];
    let expected = expected.db_after.with(&retractions, 12).unwrap();
    let actual = actual.db_after.with(&retractions, 12).unwrap();
    assert_eq!(actual.tx_data, expected.tx_data);
    common::assert_same_information(&actual.db_after, &expected.db_after);
    assert!(actual.db_after.values(owner, REFS).unwrap().is_empty());
    assert_eq!(initial.db_after.values(owner, PAIR).len(), 1);
    assert_eq!(
        initial.db_after.values(owner, PAIR)[0],
        &Value::Tuple(vec![Some(Value::Ref(target)), Some(Value::Long(7))])
    );

    let wrong_old = [TxOp::Cas {
        entity: EntityRef::Id(owner),
        attribute: PAIR,
        old: Some(pair_at(EntityRef::Id(target), 99)),
        new: pair_at(EntityRef::Id(target), 10),
    }];
    let expected = expected.db_after.with(&wrong_old, 13).unwrap_err();
    let actual = actual.db_after.with(&wrong_old, 13).unwrap_err();
    assert_eq!(expected.code, "transaction/cas-failed");
    assert_eq!(
        (actual.category, actual.code),
        (expected.category, expected.code)
    );
}

#[test]
fn tuple_nil_slots_are_legal_but_bad_types_and_nested_slots_are_not() {
    // schema-reference permits nil in every scalar slot and bounds tuples
    // to 2–8 slots. require-tuple-ids (db.clj:1259) resolves only ref slots.
    let database = Database::new(schema()).unwrap();
    let original_basis = database.basis_t();
    let nils = [
        TxOp::Add {
            entity: EntityRef::Temp("nil-owner".into()),
            attribute: PAIR,
            value: TxValue::Tuple(vec![None, Some(Value::Long(7).into())]),
        },
        TxOp::Add {
            entity: EntityRef::Temp("nil-owner".into()),
            attribute: REFS,
            value: TxValue::Tuple(vec![None, None]),
        },
    ];
    let expected = database.with(&nils, 10).unwrap();
    let actual = database.database_value().with(&nils, 10).unwrap();
    common::assert_same_information(&actual.db_after, &expected.db_after);
    let invalid = [
        (PAIR, TxValue::Tuple(vec![])),
        (PAIR, TxValue::Tuple(vec![None])),
        (REFS, TxValue::Tuple(vec![None; 9])),
        (KEY, TxValue::Tuple(vec![None, None])),
        (
            PAIR,
            TxValue::Tuple(vec![None, Some(TxValue::Entity(EntityRef::Tx))]),
        ),
        (
            PAIR,
            TxValue::Tuple(vec![Some(TxValue::Tuple(vec![None, None])), None]),
        ),
        (
            PAIR,
            TxValue::Tuple(vec![Some(Value::String("not a ref".into()).into()), None]),
        ),
        (
            PAIR,
            pair(EntityRef::Ident(Keyword::new("target", "absent"))),
        ),
        (
            PAIR,
            pair(EntityRef::Lookup {
                attribute: KEY,
                value: Value::String("absent".into()),
            }),
        ),
        (PAIR, pair(EntityRef::Temp("value-only".into()))),
    ];
    for (attribute, value) in invalid {
        let ops = [TxOp::Add {
            entity: EntityRef::Temp("invalid-owner".into()),
            attribute,
            value,
        }];
        let expected = database.with(&ops, 11).unwrap_err();
        let actual = database.database_value().with(&ops, 11).unwrap_err();
        assert_eq!(
            (actual.category, actual.code),
            (expected.category, expected.code)
        );
    }
    assert_eq!(database.basis_t(), original_basis);
}

#[test]
fn conflicting_tuple_upserts_do_not_merge_distinct_existing_entities() {
    let database = Database::new(schema()).unwrap();
    let mut initial = ops();
    initial.extend([
        TxOp::Add {
            entity: EntityRef::Temp("left".into()),
            attribute: KEY,
            value: Value::String("left-owner".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("other".into()),
            attribute: KEY,
            value: Value::String("other-owner".into()).into(),
        },
    ]);
    let database = database.with(&initial, 10).unwrap().db_after;
    let conflicting = [
        TxOp::Add {
            entity: EntityRef::Temp("conflict".into()),
            attribute: PAIR,
            value: pair(EntityRef::Ident(Keyword::new("target", "ident"))),
        },
        TxOp::Add {
            entity: EntityRef::Temp("conflict".into()),
            attribute: KEY,
            value: Value::String("other-owner".into()).into(),
        },
    ];
    for reordered in permutations(&conflicting) {
        let expected = database.with(&reordered, 11).unwrap_err();
        let actual = database.database_value().with(&reordered, 11).unwrap_err();
        assert_eq!(expected.code, "transaction/upsert-conflict");
        assert_eq!(
            (actual.category, actual.code),
            (expected.category, expected.code)
        );
    }
}

fn map_forms() -> Vec<TxForm> {
    vec![
        TxForm::EntityMap(EntityMap {
            id: Some(EntityRef::Temp("target".into())),
            attributes: vec![
                (
                    AttributeRef::Id(KEY),
                    MapValue::Value(Value::String("target".into()).into()),
                ),
                (
                    AttributeRef::Id(atomic_core::DB_IDENT as u32),
                    MapValue::Value(Value::Keyword(Keyword::new("target", "ident")).into()),
                ),
            ],
        }),
        TxForm::EntityMap(EntityMap {
            id: Some(EntityRef::Temp("left".into())),
            attributes: vec![
                (
                    AttributeRef::Ident(Keyword::new("owner", "pair")),
                    MapValue::Value(pair(EntityRef::Temp("target".into()))),
                ),
                (
                    AttributeRef::Id(REFS),
                    MapValue::Many(vec![
                        MapValue::Value(TxValue::Tuple(vec![
                            Some(TxValue::Entity(EntityRef::Temp("target".into()))),
                            None,
                        ])),
                        MapValue::Value(TxValue::Tuple(vec![None, None])),
                    ]),
                ),
            ],
        }),
        TxForm::EntityMap(EntityMap {
            id: Some(EntityRef::Temp("right".into())),
            attributes: vec![(
                AttributeRef::Id(PAIR),
                MapValue::Value(pair(EntityRef::Temp("target".into()))),
            )],
        }),
    ]
}

#[test]
fn tuple_map_values_preserve_identity_and_collection_boundaries() {
    let database = Database::new(schema()).unwrap();
    let expected = database
        .with_forms(&map_forms(), &TxFunctions::new(), 10)
        .unwrap();
    assert_eq!(expected.tempids["left"], expected.tempids["right"]);
    let owner = expected.tempids["left"];
    assert_eq!(expected.db_after.values(owner, REFS).len(), 2);
    for reordered in permutations(&map_forms()) {
        let actual = database
            .with_forms(&reordered, &TxFunctions::new(), 10)
            .unwrap();
        assert_eq!(actual.tempids, expected.tempids);
        common::assert_same_information(&actual.db_after, &expected.db_after);
    }
}

fn ops() -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Temp("target".into()),
            attribute: KEY,
            value: Value::String("target".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("target".into()),
            attribute: atomic_core::DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("target", "ident")).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("left".into()),
            attribute: PAIR,
            value: pair(EntityRef::Temp("target".into())),
        },
        TxOp::Add {
            entity: EntityRef::Temp("right".into()),
            attribute: PAIR,
            value: pair(EntityRef::Temp("target".into())),
        },
    ]
}

#[test]
fn tuple_slots_preserve_tempids_upsert_idents_lookups_and_nils() {
    let database = Database::new(schema()).unwrap();
    let eager = database.with(&ops(), 10).unwrap();
    let native = database.database_value().with(&ops(), 10).unwrap();
    assert_eq!(eager.tempids["left"], eager.tempids["right"]);
    assert_eq!(native.tempids, eager.tempids);
    common::assert_same_information(&native.db_after, &eager.db_after);
    let next = vec![
        TxOp::Add {
            entity: EntityRef::Temp("upsert".into()),
            attribute: PAIR,
            value: pair(EntityRef::Ident(Keyword::new("target", "ident"))),
        },
        TxOp::Add {
            entity: EntityRef::Temp("upsert".into()),
            attribute: REFS,
            value: TxValue::Tuple(vec![
                Some(TxValue::Entity(EntityRef::Ident(Keyword::new(
                    "target", "ident",
                )))),
                Some(TxValue::Entity(EntityRef::Lookup {
                    attribute: KEY,
                    value: Value::String("target".into()),
                })),
                None,
            ]),
        },
    ];
    let expected = eager.db_after.with(&next, 11).unwrap();
    let actual = native.db_after.with(&next, 11).unwrap();
    assert_eq!(actual.tempids["upsert"], eager.tempids["left"]);
    assert_eq!(actual.tx_data, expected.tx_data);
    common::assert_same_information(&actual.db_after, &expected.db_after);
    for bad in [
        pair(EntityRef::Temp("value-only".into())),
        TxValue::Tuple(vec![Some(Value::String("not-a-ref".into()).into()), None]),
        TxValue::Tuple(vec![None]),
    ] {
        let ops = [TxOp::Add {
            entity: EntityRef::Temp("bad-owner".into()),
            attribute: PAIR,
            value: bad,
        }];
        let expected = database.with(&ops, 10).unwrap_err();
        let actual = database.database_value().with(&ops, 10).unwrap_err();
        assert_eq!(
            (actual.category, actual.code),
            (expected.category, expected.code)
        );
    }
}

#[cfg(unix)]
#[test]
fn tuple_inputs_survive_native_socket_retry_and_recovery() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let id = format!(
        "tuple-input-{}-{}",
        std::process::id(),
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    common::install(&postgres).unwrap();
    let mut store = common::TestStore::connect(&postgres).unwrap();
    store.create_database(&id, schema()).unwrap();
    let expected = store.recover(&id).unwrap().with(&ops(), 10).unwrap();
    let writer = common::start_service(&postgres, &id);
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let peer = atomic_core::Connection::connect(&postgres, &id, 4).unwrap();
    let request = atomic_core::TransactionRequest::new("tuple-input", ops()).with_tx_instant(10);
    let result = peer
        .transact_socket(server.endpoint(), request.clone(), Duration::from_secs(10))
        .unwrap();
    let report = result.report.unwrap();
    assert_eq!(report.tempids, expected.tempids);
    common::assert_same_information(&report.db_after, &expected.db_after);
    drop(server);
    writer.shutdown();
    common::assert_same_information(&store.recover(&id).unwrap(), &expected.db_after);
    let writer = common::start_service(&postgres, &id);
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let replay = peer
        .transact_socket(server.endpoint(), request, Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, result.basis_t);
    assert_eq!(replay.report.unwrap().tempids, report.tempids);
    assert!(
        !report
            .db_after
            .collect_datoms(IndexOrder::Eavt)
            .unwrap()
            .is_empty()
    );
}

#[cfg(unix)]
#[test]
fn tuple_maps_cas_and_retraction_cross_native_socket_without_materialization() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let id = format!(
        "tuple-map-input-{}-{}",
        std::process::id(),
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    common::install(&postgres).unwrap();
    let mut store = common::TestStore::connect(&postgres).unwrap();
    store.create_database(&id, schema()).unwrap();
    let before = Database::new(schema()).unwrap();
    let expected = before
        .with_forms(&map_forms(), &TxFunctions::new(), 10)
        .unwrap();
    let writer = common::start_service(&postgres, &id);
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let peer = atomic_core::Connection::connect(&postgres, &id, 4).unwrap();
    let old = peer.db();
    let request = atomic_core::TransactionRequest::from_forms("tuple-map-input", map_forms())
        .with_tx_instant(10);
    let committed = peer
        .transact_socket(server.endpoint(), request.clone(), Duration::from_secs(15))
        .unwrap();
    let mapped = committed.report.unwrap();
    assert_eq!(mapped.tempids, expected.tempids);
    assert_eq!(mapped.tempids["left"], mapped.tempids["right"]);
    common::assert_same_information(&mapped.db_before, &before);
    common::assert_same_information(&mapped.db_after, &expected.db_after);
    common::assert_same_information(&old, &before);
    let owner = mapped.tempids["left"];
    let target = mapped.tempids["target"];
    let changes = vec![
        TxOp::Cas {
            entity: EntityRef::Id(owner),
            attribute: PAIR,
            old: Some(pair(EntityRef::Lookup {
                attribute: KEY,
                value: Value::String("target".into()),
            })),
            new: pair_at(EntityRef::Ident(Keyword::new("target", "ident")), 8),
        },
        TxOp::Retract {
            entity: EntityRef::Id(owner),
            attribute: REFS,
            value: Some(TxValue::Tuple(vec![
                Some(TxValue::Entity(EntityRef::Ident(Keyword::new(
                    "target", "ident",
                )))),
                None,
            ])),
        },
        TxOp::Retract {
            entity: EntityRef::Id(owner),
            attribute: REFS,
            value: Some(Value::Tuple(vec![None, None]).into()),
        },
    ];
    let expected_after = expected.db_after.with(&changes, 11).unwrap();
    let changed = peer
        .transact_socket(
            server.endpoint(),
            atomic_core::TransactionRequest::from_forms(
                "tuple-map-cas",
                changes.into_iter().map(TxForm::Op).collect(),
            )
            .with_tx_instant(11),
            Duration::from_secs(15),
        )
        .unwrap()
        .report
        .unwrap();
    assert_eq!(changed.tx_data, expected_after.tx_data);
    common::assert_same_information(&changed.db_after, &expected_after.db_after);
    assert!(changed.db_after.values(owner, REFS).unwrap().is_empty());
    assert_eq!(
        changed.db_after.values(owner, PAIR).unwrap(),
        [Value::Tuple(vec![
            Some(Value::Ref(target)),
            Some(Value::Long(8))
        ])]
    );
    let invalid_map = vec![TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Id(owner)),
        attributes: vec![(
            AttributeRef::Id(PAIR),
            MapValue::Value(pair(EntityRef::Temp("tuple-value-only".into()))),
        )],
    })];
    let expected_error = expected_after
        .db_after
        .with_forms(&invalid_map, &TxFunctions::new(), 12)
        .unwrap_err();
    let error = peer
        .transact_socket(
            server.endpoint(),
            atomic_core::TransactionRequest::from_forms("tuple-map-invalid", invalid_map)
                .with_tx_instant(12),
            Duration::from_secs(15),
        )
        .unwrap_err();
    assert_eq!(error.category, expected_error.category);
    assert_eq!(error.details["remote_code"], expected_error.code);
    let replay = peer
        .transact_socket(server.endpoint(), request, Duration::from_secs(15))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    let replay = replay.report.unwrap();
    assert_eq!(replay.tx_data, mapped.tx_data);
    assert_eq!(replay.tempids, mapped.tempids);
    common::assert_same_information(&replay.db_after, &expected.db_after);
    drop(server);
    writer.shutdown();
    common::assert_same_information(&store.recover(&id).unwrap(), &expected_after.db_after);
}
