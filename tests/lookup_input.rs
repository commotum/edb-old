use atomic_core::{
    Attribute, AttributeRef, Cardinality, Database, EntityMap, EntityRef, ErrorCategory, Keyword,
    MapValue, Schema, TupleSpec, TxForm, TxFunctions, TxOp, TxValue, Unique, Value, ValueType,
};
use std::time::Duration;

mod common;

const KEY: u32 = 1_000;
const OWNER_TARGET: u32 = 1_001;
const PAIR: u32 = 1_002;
const LABEL: u32 = 1_003;
const LINKS: u32 = 1_004;
const PRIMARY: u32 = 1_005;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            KEY,
            Keyword::new("target", "key"),
            ValueType::String,
            Cardinality::One,
        )
        .unique(Unique::Identity),
        Attribute::new(
            OWNER_TARGET,
            Keyword::new("owner", "target"),
            ValueType::Ref,
            Cardinality::One,
        )
        .unique(Unique::Identity),
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
        Attribute::new(
            LABEL,
            Keyword::new("owner", "label"),
            ValueType::String,
            Cardinality::One,
        ),
        Attribute::new(
            LINKS,
            Keyword::new("owner", "links"),
            ValueType::Ref,
            Cardinality::Many,
        ),
        Attribute::new(
            PRIMARY,
            Keyword::new("owner", "primary"),
            ValueType::Ref,
            Cardinality::One,
        ),
    ] {
        schema.install(attribute).unwrap();
    }
    schema
}

fn lookup(attribute: u32, value: TxValue) -> EntityRef {
    EntityRef::LookupInput {
        attribute,
        value: Box::new(value),
    }
}

fn key(name: &str) -> EntityRef {
    lookup(KEY, Value::String(name.into()).into())
}

fn target_ident() -> EntityRef {
    EntityRef::Ident(Keyword::new("target", "one"))
}

fn ref_owner(reference: EntityRef) -> EntityRef {
    lookup(OWNER_TARGET, TxValue::Entity(reference))
}

fn tuple_key(reference: Option<EntityRef>) -> TxValue {
    TxValue::Tuple(vec![
        reference.map(TxValue::Entity),
        Some(Value::Long(7).into()),
    ])
}

fn tuple_owner(reference: Option<EntityRef>) -> EntityRef {
    lookup(PAIR, tuple_key(reference))
}

fn add(entity: EntityRef, attribute: u32, value: impl Into<TxValue>) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn seed_ops() -> Vec<TxOp> {
    let target = EntityRef::Temp("target".into());
    let owner = EntityRef::Temp("owner".into());
    vec![
        add(target.clone(), KEY, Value::String("one".into())),
        add(
            target.clone(),
            atomic_core::DB_IDENT as u32,
            Value::Keyword(Keyword::new("target", "one")),
        ),
        add(owner.clone(), OWNER_TARGET, TxValue::Entity(target.clone())),
        add(owner.clone(), PAIR, tuple_key(Some(target.clone()))),
        add(owner.clone(), LABEL, Value::String("original".into())),
        add(owner.clone(), LINKS, TxValue::Entity(target.clone())),
        add(owner, PRIMARY, TxValue::Entity(target)),
        add(EntityRef::Temp("nil-owner".into()), PAIR, tuple_key(None)),
        add(
            EntityRef::Temp("nil-owner".into()),
            LABEL,
            Value::String("nil".into()),
        ),
        add(
            EntityRef::Temp("destination".into()),
            KEY,
            Value::String("destination".into()),
        ),
    ]
}

fn changes() -> Vec<TxOp> {
    vec![
        TxOp::Cas {
            entity: ref_owner(target_ident()),
            attribute: LABEL,
            old: Some(Value::String("original".into()).into()),
            new: Value::String("changed".into()).into(),
        },
        TxOp::Cas {
            entity: tuple_owner(Some(EntityRef::Lookup {
                attribute: KEY,
                value: Value::String("one".into()),
            })),
            attribute: PRIMARY,
            old: Some(TxValue::Entity(key("one"))),
            new: TxValue::Entity(key("destination")),
        },
        add(
            key("destination"),
            LINKS,
            TxValue::Entity(ref_owner(EntityRef::Lookup {
                attribute: KEY,
                value: Value::String("one".into()),
            })),
        ),
        add(
            tuple_owner(None),
            LABEL,
            Value::String("nil-changed".into()),
        ),
        TxOp::Retract {
            entity: ref_owner(key("one")),
            attribute: LINKS,
            value: Some(TxValue::Entity(key("one"))),
        },
    ]
}

fn change_forms() -> Vec<TxForm> {
    let mut forms: Vec<_> = changes().into_iter().map(TxForm::Op).collect();
    forms[2] = TxForm::EntityMap(EntityMap {
        id: Some(key("destination")),
        attributes: vec![(
            AttributeRef::Ident(Keyword::new("owner", "links")),
            MapValue::Value(TxValue::Entity(tuple_owner(Some(target_ident())))),
        )],
    });
    forms[3] = TxForm::EntityMap(EntityMap {
        id: Some(tuple_owner(None)),
        attributes: vec![(
            AttributeRef::Id(LABEL),
            MapValue::Value(Value::String("nil-changed".into()).into()),
        )],
    });
    forms
}

#[test]
fn reference_shaped_lookup_keys_resolve_in_all_transaction_positions() {
    // Identity docs restrict transaction lookup refs to db-before. Recovered
    // resolve-lookup-ref (db.clj:1117) uses its unique attribute index; nested
    // identifiers and ref tuple slots are resolved before probing that key.
    let seeded = Database::new(schema())
        .unwrap()
        .with(&seed_ops(), 10)
        .unwrap();
    let owner = seeded.tempids["owner"];
    let target = seeded.tempids["target"];
    let destination = seeded.tempids["destination"];
    let expected = seeded.db_after.with(&changes(), 11).unwrap();
    let native = seeded
        .db_after
        .database_value()
        .with(&changes(), 11)
        .unwrap();
    assert_eq!(native.tx_data, expected.tx_data);
    common::assert_same_information(&native.db_after, &expected.db_after);
    assert_eq!(
        native.db_after.values(owner, LABEL).unwrap(),
        [Value::String("changed".into())]
    );
    assert_eq!(
        native.db_after.values(owner, PRIMARY).unwrap(),
        [Value::Ref(destination)]
    );
    assert_eq!(
        native.db_after.values(destination, LINKS).unwrap(),
        [Value::Ref(owner)]
    );
    assert!(native.db_after.values(owner, LINKS).unwrap().is_empty());
    assert_eq!(
        seeded.db_after.values(owner, PRIMARY),
        [&Value::Ref(target)]
    );

    let eager_map = seeded
        .db_after
        .with_forms(&change_forms(), &TxFunctions::new(), 11)
        .unwrap();
    let native_map = seeded
        .db_after
        .database_value()
        .with_forms(&change_forms(), 11)
        .unwrap();
    common::assert_same_information(&eager_map.db_after, &expected.db_after);
    common::assert_same_information(&native_map.db_after, &expected.db_after);
    let retracted = native_map
        .db_after
        .with(&[TxOp::RetractEntity(tuple_owner(None))], 12)
        .unwrap();
    assert!(
        retracted
            .db_after
            .values(seeded.tempids["nil-owner"], PAIR)
            .unwrap()
            .is_empty()
    );
    assert!(
        !native_map
            .db_after
            .values(seeded.tempids["nil-owner"], PAIR)
            .unwrap()
            .is_empty()
    );
}

fn invalid_references() -> Vec<EntityRef> {
    vec![
        ref_owner(EntityRef::Temp("new-target".into())),
        ref_owner(EntityRef::Tx),
        tuple_owner(Some(EntityRef::Temp("new-target".into()))),
        tuple_owner(Some(EntityRef::Tx)),
        ref_owner(EntityRef::Ident(Keyword::new("target", "unknown"))),
        ref_owner(EntityRef::Lookup {
            attribute: KEY,
            value: Value::String("missing".into()),
        }),
        lookup(OWNER_TARGET, Value::String("not-a-ref".into()).into()),
        lookup(
            PAIR,
            TxValue::Tuple(vec![Some(Value::Long(9).into()), None]),
        ),
        lookup(LABEL, Value::String("original".into()).into()),
        key("missing"),
        lookup(
            OWNER_TARGET,
            TxValue::Entity(lookup(LABEL, Value::String("original".into()).into())),
        ),
    ]
}

#[test]
fn lookup_keys_reject_invalid_or_transaction_local_entities_atomically() {
    let seeded = Database::new(schema())
        .unwrap()
        .with(&seed_ops(), 10)
        .unwrap();
    let before = seeded.db_after.database_value();
    for (index, reference) in invalid_references().into_iter().enumerate() {
        let operations = [
            add(
                EntityRef::Temp("new-target".into()),
                KEY,
                Value::String("new".into()),
            ),
            add(reference, LABEL, Value::String("must-not-commit".into())),
        ];
        let eager_error = seeded.db_after.with(&operations, 11).unwrap_err();
        let native_error = before.with(&operations, 11).unwrap_err();
        assert_eq!(
            (native_error.category, native_error.code),
            (eager_error.category, eager_error.code),
            "case {index}"
        );
        if index < 4 {
            assert_eq!(eager_error.code, "transaction/lookup-local-entity");
        }
        common::assert_same_information(&before, &seeded.db_after);
    }
}

#[test]
fn lookup_keys_do_not_see_assertions_or_ident_changes_in_the_same_transaction() {
    let seeded = Database::new(schema())
        .unwrap()
        .with(&seed_ops(), 10)
        .unwrap();
    let target = seeded.tempids["target"];
    let owner = seeded.tempids["owner"];
    let moved_ident = [
        TxOp::Retract {
            entity: EntityRef::Id(target),
            attribute: atomic_core::DB_IDENT as u32,
            value: Some(Value::Keyword(Keyword::new("target", "one")).into()),
        },
        add(
            EntityRef::Id(seeded.tempids["destination"]),
            atomic_core::DB_IDENT as u32,
            Value::Keyword(Keyword::new("target", "one")),
        ),
        add(
            ref_owner(target_ident()),
            LABEL,
            Value::String("resolved-before-ident-move".into()),
        ),
    ];
    let expected = seeded.db_after.with(&moved_ident, 11).unwrap();
    let native = seeded
        .db_after
        .database_value()
        .with(&moved_ident, 11)
        .unwrap();
    common::assert_same_information(&native.db_after, &expected.db_after);
    assert_eq!(
        native.db_after.values(owner, LABEL).unwrap(),
        [Value::String("resolved-before-ident-move".into())]
    );
    let fresh = [
        add(
            EntityRef::Temp("fresh".into()),
            KEY,
            Value::String("fresh".into()),
        ),
        add(
            key("fresh"),
            LABEL,
            Value::String("not-visible-in-before".into()),
        ),
    ];
    let expected = seeded.db_after.with(&fresh, 11).unwrap_err();
    let native = seeded
        .db_after
        .database_value()
        .with(&fresh, 11)
        .unwrap_err();
    assert_eq!(
        (native.category, native.code),
        (expected.category, expected.code)
    );
}

fn deep_reference() -> EntityRef {
    let mut reference = target_ident();
    for _ in 0..40 {
        reference = ref_owner(reference);
    }
    reference
}

#[test]
fn deeply_nested_reference_input_is_rejected_by_admission_before_resolution() {
    let seeded = Database::new(schema())
        .unwrap()
        .with(&seed_ops(), 10)
        .unwrap();
    let cases = [
        TxForm::Op(add(deep_reference(), LABEL, Value::String("deep".into()))),
        TxForm::Op(add(
            EntityRef::Id(seeded.tempids["owner"]),
            LINKS,
            TxValue::Entity(deep_reference()),
        )),
        TxForm::EntityMap(EntityMap {
            id: Some(deep_reference()),
            attributes: vec![(
                AttributeRef::Id(LABEL),
                MapValue::Value(Value::String("deep".into()).into()),
            )],
        }),
        TxForm::Op(TxOp::Cas {
            entity: EntityRef::Id(seeded.tempids["owner"]),
            attribute: PRIMARY,
            old: Some(TxValue::Entity(deep_reference())),
            new: TxValue::Entity(target_ident()),
        }),
    ];
    for form in cases {
        let expected = seeded
            .db_after
            .with_forms(std::slice::from_ref(&form), &TxFunctions::new(), 11)
            .unwrap_err();
        let native = seeded
            .db_after
            .database_value()
            .with_forms(std::slice::from_ref(&form), 11)
            .unwrap_err();
        assert_eq!(
            (expected.category, expected.code),
            (ErrorCategory::Busy, "transaction/input-depth")
        );
        assert_eq!(
            (native.category, native.code),
            (expected.category, expected.code)
        );
        if let TxForm::Op(operation) = form {
            let native = seeded
                .db_after
                .database_value()
                .with(&[operation], 11)
                .unwrap_err();
            assert_eq!(native.code, "transaction/input-depth");
        }
    }
}

#[cfg(unix)]
#[test]
fn lookup_inputs_cross_native_socket_and_preserve_receipts_recovery_and_rejected_head() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let id = format!(
        "lookup-input-{}-{}",
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
    let before = store.recover(&id).unwrap();
    let expected_seed = before.with(&seed_ops(), 10).unwrap();
    let writer = common::start_service(&postgres, &id);
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let peer = atomic_core::Connection::connect(&postgres, &id, 4).unwrap();
    // Keep a receipt encoded entirely in the pre-LookupInput grammar, then
    // retry it after committing new input shapes and reopening the writer.
    let seed_request =
        atomic_core::TransactionRequest::new("legacy-seed", seed_ops()).with_tx_instant(10);
    let seeded = peer
        .transact_socket(
            server.endpoint(),
            seed_request.clone(),
            Duration::from_secs(15),
        )
        .unwrap();
    let seed_hash = seeded.tx_hash;
    let seeded = seeded.report.unwrap();
    assert_eq!(seeded.tempids, expected_seed.tempids);
    common::assert_same_information(&seeded.db_after, &expected_seed.db_after);
    let old = seeded.db_after.clone();
    let expected = expected_seed
        .db_after
        .with_forms(&change_forms(), &TxFunctions::new(), 11)
        .unwrap();
    let changed_request =
        atomic_core::TransactionRequest::from_forms("lookup-changes", change_forms())
            .with_tx_instant(11);
    let changed = peer
        .transact_socket(
            server.endpoint(),
            changed_request.clone(),
            Duration::from_secs(15),
        )
        .unwrap();
    let changed_hash = changed.tx_hash;
    let changed = changed.report.unwrap();
    assert_eq!(changed.tx_data, expected.tx_data);
    common::assert_same_information(&changed.db_after, &expected.db_after);
    common::assert_same_information(&old, &expected_seed.db_after);
    let mut sql = postgres::Client::connect(&postgres, postgres::NoTls).unwrap();
    let head = |sql: &mut postgres::Client| {
        let row = sql
            .query_one(
                "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
                &[&id],
            )
            .unwrap();
        (row.get::<_, i64>(0), row.get::<_, Vec<u8>>(1))
    };
    let committed_head = head(&mut sql);
    for (index, reference) in invalid_references().into_iter().enumerate() {
        let forms = vec![
            TxForm::Op(add(
                EntityRef::Temp("new-target".into()),
                KEY,
                Value::String("new".into()),
            )),
            TxForm::EntityMap(EntityMap {
                id: Some(reference),
                attributes: vec![(
                    AttributeRef::Id(LABEL),
                    MapValue::Value(Value::String("must-not-commit".into()).into()),
                )],
            }),
        ];
        let expected_error = expected
            .db_after
            .with_forms(&forms, &TxFunctions::new(), 12)
            .unwrap_err();
        let error = peer
            .transact_socket(
                server.endpoint(),
                atomic_core::TransactionRequest::from_forms(format!("invalid-{index}"), forms)
                    .with_tx_instant(12),
                Duration::from_secs(15),
            )
            .unwrap_err();
        assert_eq!(error.category, expected_error.category, "case {index}");
        assert_eq!(
            error.details["remote_code"], expected_error.code,
            "case {index}"
        );
        assert_eq!(
            head(&mut sql),
            committed_head,
            "rejected case {index} must not advance the durable head"
        );
    }
    let deep_request = atomic_core::TransactionRequest::new(
        "too-deep",
        vec![add(deep_reference(), LABEL, Value::String("deep".into()))],
    );
    let error = writer.client().submit(deep_request.clone()).unwrap_err();
    assert_eq!(error.code, "transaction/input-depth");
    let error = peer
        .transact_socket(server.endpoint(), deep_request, Duration::from_secs(15))
        .unwrap_err();
    assert_eq!(error.code, "transaction/input-depth");
    assert_eq!(head(&mut sql), committed_head);
    common::assert_same_information(&peer.db(), &expected.db_after);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    drop(server);
    writer.shutdown();
    common::assert_same_information(&store.recover(&id).unwrap(), &expected.db_after);
    let writer = common::start_service(&postgres, &id);
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    for (request, hash, original) in [
        (seed_request, seed_hash, seeded),
        (changed_request, changed_hash, changed),
    ] {
        let replayed = peer
            .transact_socket(server.endpoint(), request, Duration::from_secs(15))
            .unwrap();
        assert!(replayed.replayed);
        assert_eq!(replayed.tx_hash, hash);
        let report = replayed.report.unwrap();
        assert_eq!(report.tx_data, original.tx_data);
        assert_eq!(report.tempids, original.tempids);
        common::assert_same_information(&report.db_before, &original.db_before);
        common::assert_same_information(&report.db_after, &original.db_after);
        assert_eq!(head(&mut sql), committed_head);
    }
}
