mod common;

use atomic_core::{
    Attribute, AttributeRef, CallableRef, Cardinality, DB_FN, DB_IDENT, Database, DatabaseValue,
    EntityMap, EntityRef, IndexPrefix, Instruction, Keyword, MapValue, Program, ProgramCall,
    ProgramKind, RuntimeValue, Schema, TransactionRequest, TxCall, TxForm, TxFunctions, TxOp,
    TxValue, Unique, Value, ValueType,
};
use std::collections::{BTreeMap, BTreeSet};
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::Duration;

const TAG: u32 = 1_000;
const LABEL: u32 = 1_001;
const CHILD: u32 = 1_002;
const KEY: u32 = 1_003;

fn reserved(index: usize) -> String {
    format!("__map/{index:020}")
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            TAG,
            Keyword::new("identity", "tag"),
            ValueType::String,
            Cardinality::Many,
        ),
        Attribute::new(
            LABEL,
            Keyword::new("identity", "label"),
            ValueType::String,
            Cardinality::One,
        ),
        Attribute::new(
            CHILD,
            Keyword::new("identity", "child"),
            ValueType::Ref,
            Cardinality::Many,
        )
        .component(),
        Attribute::new(
            KEY,
            Keyword::new("identity", "key"),
            ValueType::String,
            Cardinality::One,
        )
        .unique(Unique::Identity),
    ] {
        schema.install(attribute).unwrap();
    }
    schema
}

fn entity_map(id: Option<String>, attribute: u32, label: &str) -> EntityMap {
    EntityMap {
        id: id.map(EntityRef::Temp),
        attributes: vec![(
            AttributeRef::Id(attribute),
            MapValue::Value(Value::String(label.into()).into()),
        )],
    }
}

fn map(id: Option<String>, attribute: u32, label: &str) -> TxForm {
    TxForm::EntityMap(entity_map(id, attribute, label))
}

fn observed_labels(db: &DatabaseValue, attribute: u32) -> BTreeMap<String, u64> {
    db.datoms_with_prefix(&IndexPrefix::Aevt {
        attribute,
        entity: None,
        value: None,
    })
    .unwrap()
    .into_iter()
    .map(|datom| {
        let Value::String(label) = datom.value else {
            panic!("expected string")
        };
        (label, datom.entity)
    })
    .collect()
}

fn assert_distinct(db: &DatabaseValue, attribute: u32, count: usize) {
    let labels = observed_labels(db, attribute);
    assert_eq!(labels.len(), count);
    assert_eq!(
        labels.values().copied().collect::<BTreeSet<_>>().len(),
        count
    );
}

#[test]
fn anonymous_maps_do_not_alias_explicit_names_for_either_cardinality_or_engine() {
    let db = Database::new(schema()).unwrap();
    for attribute in [TAG, LABEL] {
        let forms = vec![
            map(Some(reserved(0)), attribute, "explicit"),
            map(None, attribute, "anonymous"),
        ];
        let eager = db.with_forms(&forms, &TxFunctions::new(), 10).unwrap();
        let exact = db.database_value().with_forms(&forms, 10).unwrap();
        assert_distinct(&eager.db_after.database_value(), attribute, 2);
        assert_distinct(&exact.db_after, attribute, 2);
        assert_eq!(eager.tx_data, exact.tx_data);
        assert_eq!(eager.tempids, exact.tempids);
        assert_ne!(exact.tempids[&reserved(0)], exact.tempids[&reserved(1)]);
        assert_eq!(
            observed_labels(&exact.db_after, attribute)["explicit"],
            exact.tempids[&reserved(0)]
        );
        assert!(observed_labels(&db.database_value(), attribute).is_empty());
    }
}

#[test]
fn allocation_skips_every_explicit_name_and_is_declarative_under_reordering() {
    let db = Database::new(schema()).unwrap();
    let mut forms = vec![
        map(None, LABEL, "anonymous-a"),
        map(Some(reserved(0)), LABEL, "explicit-zero"),
        map(Some(reserved(1)), LABEL, "explicit-one"),
        map(None, LABEL, "anonymous-b"),
        map(Some(reserved(3)), LABEL, "explicit-three"),
    ];
    let expected = db.with_forms(&forms, &TxFunctions::new(), 10).unwrap();
    assert_distinct(&expected.db_after.database_value(), LABEL, 5);
    for _ in 0..forms.len() {
        forms.rotate_left(1);
        for input in [forms.clone(), forms.iter().cloned().rev().collect()] {
            let actual = db.database_value().with_forms(&input, 10).unwrap();
            assert_eq!(expected.tx_data, actual.tx_data);
            assert_eq!(expected.tempids, actual.tempids);
        }
    }
    assert_eq!(
        expected.tempids.keys().cloned().collect::<Vec<_>>(),
        (0..5).map(reserved).collect::<Vec<_>>()
    );

    // Existing receipts for ordinary, noncolliding maps retain their names.
    let ordinary = db
        .database_value()
        .with_forms(&[map(None, LABEL, "a"), map(None, LABEL, "b")], 10)
        .unwrap();
    assert_eq!(
        ordinary.tempids.keys().cloned().collect::<Vec<_>>(),
        vec![reserved(0), reserved(1)]
    );
}

#[test]
fn reference_only_tempids_cannot_capture_an_anonymous_map() {
    let db = Database::new(schema()).unwrap();
    let forms = vec![
        map(None, LABEL, "anonymous"),
        map(Some("parent".into()), LABEL, "parent"),
        TxForm::Op(TxOp::Add {
            entity: EntityRef::Temp("parent".into()),
            attribute: CHILD,
            value: TxValue::Entity(EntityRef::Temp(reserved(0))),
        }),
    ];
    assert_eq!(
        db.with_forms(&forms, &TxFunctions::new(), 10)
            .unwrap_err()
            .code,
        "transaction/tempid-not-an-entity"
    );
    assert_eq!(
        db.database_value().with_forms(&forms, 10).unwrap_err().code,
        "transaction/tempid-not-an-entity"
    );
}

#[test]
fn nested_maps_reserve_names_before_allocating_any_parent_or_child() {
    let db = Database::new(schema()).unwrap();
    let mut parent = entity_map(None, LABEL, "parent");
    parent.attributes.push((
        AttributeRef::Id(CHILD),
        MapValue::Many(vec![
            MapValue::Nested(Box::new(entity_map(None, LABEL, "anonymous-child"))),
            MapValue::Nested(Box::new(entity_map(
                Some(reserved(0)),
                LABEL,
                "explicit-child",
            ))),
        ]),
    ));
    let forms = vec![TxForm::EntityMap(parent.clone())];
    let eager = db.with_forms(&forms, &TxFunctions::new(), 10).unwrap();
    let exact = db.database_value().with_forms(&forms, 10).unwrap();
    assert_distinct(&exact.db_after, LABEL, 3);
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(eager.tempids, exact.tempids);
    let labels = observed_labels(&exact.db_after, LABEL);
    assert_eq!(labels["explicit-child"], exact.tempids[&reserved(0)]);
    assert_eq!(
        exact
            .db_after
            .values(labels["parent"], CHILD)
            .unwrap()
            .into_iter()
            .map(|value| match value {
                Value::Ref(entity) => entity,
                _ => panic!("expected component reference"),
            })
            .collect::<BTreeSet<_>>(),
        [labels["anonymous-child"], labels["explicit-child"]]
            .into_iter()
            .collect()
    );
    if let MapValue::Many(children) = &mut parent.attributes[1].1 {
        children.reverse();
    }
    parent.attributes.reverse();
    let reordered = db
        .database_value()
        .with_forms(&[TxForm::EntityMap(parent)], 10)
        .unwrap();
    assert_eq!(exact.tx_data, reordered.tx_data);
    assert_eq!(exact.tempids, reordered.tempids);
}

#[test]
fn local_recursive_expansion_reserves_generated_explicit_names_and_runs_once() {
    let db = Database::new(schema()).unwrap();
    let invocations = Arc::new(AtomicUsize::new(0));
    let mut functions = TxFunctions::new();
    for (name, body) in [
        (
            "outer",
            vec![
                map(None, LABEL, "outer-anonymous"),
                TxForm::Call(TxCall {
                    function: "inner".into(),
                    arguments: Vec::new(),
                }),
            ],
        ),
        (
            "inner",
            vec![map(Some(reserved(0)), LABEL, "generated-explicit")],
        ),
    ] {
        let invocations = Arc::clone(&invocations);
        let basis = db.basis_t();
        functions.register(name, move |before, _| {
            assert_eq!(before.basis_t(), basis);
            assert!(observed_labels(before, LABEL).is_empty());
            invocations.fetch_add(1, Ordering::Relaxed);
            Ok(body.clone())
        });
    }
    let mut forms = vec![
        map(None, LABEL, "top-anonymous"),
        TxForm::Call(TxCall {
            function: "outer".into(),
            arguments: Vec::new(),
        }),
    ];
    let result = db.with_forms(&forms, &functions, 10).unwrap();
    assert_distinct(&result.db_after.database_value(), LABEL, 3);
    assert_eq!(invocations.load(Ordering::Relaxed), 2);
    assert_eq!(
        observed_labels(&result.db_after.database_value(), LABEL)["generated-explicit"],
        result.tempids[&reserved(0)]
    );
    forms.reverse();
    let reordered = db.with_forms(&forms, &functions, 10).unwrap();
    assert_eq!(invocations.load(Ordering::Relaxed), 4);
    assert_eq!(result.tx_data, reordered.tx_data);
    assert_eq!(result.tempids, reordered.tempids);
}

#[test]
fn explicit_sharing_and_unique_identity_upserts_still_unify_intentionally() {
    let db = Database::new(schema()).unwrap();
    let forms = vec![
        map(Some(reserved(0)), TAG, "first"),
        map(Some(reserved(0)), TAG, "second"),
        map(None, KEY, "same-key"),
        map(Some(reserved(1)), KEY, "same-key"),
    ];
    let result = db.database_value().with_forms(&forms, 10).unwrap();
    assert_eq!(
        result
            .db_after
            .values(result.tempids[&reserved(0)], TAG)
            .unwrap()
            .len(),
        2
    );
    assert_eq!(result.tempids[&reserved(1)], result.tempids[&reserved(2)]);
    assert_ne!(result.tempids[&reserved(0)], result.tempids[&reserved(1)]);
    let later = result
        .db_after
        .with_forms(&[map(None, KEY, "same-key")], 11)
        .unwrap();
    assert_eq!(later.tempids[&reserved(0)], result.tempids[&reserved(1)]);
    let eager = db.with_forms(&forms, &TxFunctions::new(), 10).unwrap();
    assert_eq!(result.tx_data, eager.tx_data);
    assert_eq!(result.tempids, eager.tempids);
}

fn runtime_map(id: Option<String>, label: &str) -> RuntimeValue {
    let mut entries = vec![(
        Value::Keyword(Keyword::new("identity", "label")),
        RuntimeValue::Scalar(Value::String(label.into())),
    )];
    if let Some(id) = id {
        entries.push((
            Value::Keyword(Keyword::new("db", "id")),
            RuntimeValue::Entity(EntityRef::Temp(id)),
        ));
    }
    RuntimeValue::map(entries).unwrap()
}

#[test]
fn postgres_generated_maps_commit_restart_and_retry_without_reexpansion() {
    let Some(connection) = std::env::var("ATOMIC_POSTGRES_URL").ok() else {
        eprintln!("PostgreSQL anonymous identity check skipped: ATOMIC_POSTGRES_URL is not set");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "anonymous_identity");
    common::install(&fixture.connection).unwrap();
    let mut store = common::TestStore::connect(&fixture.connection).unwrap();
    store
        .create_database("anonymous_identity", schema())
        .unwrap();
    let emitter = store
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 2,
            instructions: vec![
                Instruction::PushArgument(0),
                Instruction::EmitEntityMap,
                Instruction::PushArgument(1),
                Instruction::EmitEntityMap,
                Instruction::Return,
            ],
        })
        .unwrap();
    let recursive_emitter = store
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 2,
            instructions: vec![
                Instruction::PushArgument(0),
                Instruction::PushArgument(1),
                Instruction::EmitCall {
                    function: CallableRef::ExactHash(emitter),
                    argument_count: 2,
                },
                Instruction::Return,
            ],
        })
        .unwrap();
    // A changed binding is deliberately incompatible with the original call.
    let changed = store
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![Instruction::Return],
        })
        .unwrap();
    drop(store);
    let service = common::start_service(&fixture.connection, "anonymous_identity");
    let installed = service
        .client()
        .transact(
            TransactionRequest::new(
                "install",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("emitter".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("identity", "emit")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("emitter".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(recursive_emitter).into(),
                    },
                ],
            )
            .with_tx_instant(10),
            Duration::from_secs(5),
        )
        .unwrap();
    let request = TransactionRequest::from_forms(
        "anonymous-request",
        vec![map(None, LABEL, "top-anonymous")],
    )
    .calling(ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(Keyword::new("identity", "emit"))),
        arguments: vec![
            runtime_map(None, "generated-anonymous"),
            runtime_map(Some(reserved(0)), "generated-explicit"),
        ],
    })
    .comparing_basis(installed.basis_t)
    .with_tx_instant(11);
    let speculative = installed.db_after.with_forms(&request.forms, 11).unwrap();
    assert_distinct(&speculative.db_after, LABEL, 3);
    let report = service
        .client()
        .transact(request.clone(), Duration::from_secs(5))
        .unwrap();
    assert!(!report.replayed);
    assert_eq!(report.tx_data, speculative.tx_data);
    assert_eq!(report.tempids, speculative.tempids);
    assert_distinct(&report.db_after, LABEL, 3);
    assert!(observed_labels(&report.db_before, LABEL).is_empty());
    assert_eq!(
        observed_labels(&report.db_after, LABEL)["generated-explicit"],
        report.tempids[&reserved(0)]
    );
    service
        .client()
        .transact(
            TransactionRequest::new(
                "change-binding",
                vec![TxOp::Add {
                    entity: EntityRef::Id(installed.tempids["emitter"]),
                    attribute: DB_FN as u32,
                    value: Value::Function(changed).into(),
                }],
            )
            .with_tx_instant(12),
            Duration::from_secs(5),
        )
        .unwrap();
    service.shutdown();

    let restarted = common::start_service(&fixture.connection, "anonymous_identity");
    let replay = restarted
        .client()
        .transact(request, Duration::from_secs(5))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, report.basis_t);
    assert_eq!(replay.tx_hash, report.tx_hash);
    assert_eq!(replay.tx_data, report.tx_data);
    assert_eq!(replay.tempids, report.tempids);
    assert_eq!(
        observed_labels(&replay.db_after, LABEL),
        observed_labels(&report.db_after, LABEL)
    );
    assert!(observed_labels(&replay.db_before, LABEL).is_empty());
    restarted.shutdown();
}
