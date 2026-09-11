use super::canonical::{compare_entity_map, compare_tx_form, compare_tx_op, compare_value};
use super::pipeline::{assess_durable_forms, assess_forms, select_tx_instant};
use super::*;
use crate::{
    Attribute, CallableRef, Cardinality, Database, Keyword, ProgramCall, RuntimeValue, Unique,
    Value, ValueType,
};
use bigdecimal::BigDecimal;
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};
use std::str::FromStr;
use std::sync::Arc;

const KEY: u32 = 1_000;
const CHILD: u32 = 1_001;

fn scalar(value: Value) -> MapValue {
    MapValue::Value(TxValue::Scalar(value))
}

#[test]
fn durable_clock_policy_preserves_explicit_errors_and_monotone_default() {
    let base = Database::bootstrap()
        .unwrap()
        .with(&[], 100)
        .unwrap()
        .db_after
        .database_value();
    let add = |instant| TxOp::Add {
        entity: EntityRef::Tx,
        attribute: crate::DB_TX_INSTANT as u32,
        value: Value::Instant(instant).into(),
    };
    assert_eq!(select_tx_instant(&base, 90, None, &[]).unwrap(), 100);
    assert_eq!(select_tx_instant(&base, 200, None, &[]).unwrap(), 200);
    assert_eq!(
        select_tx_instant(&base, 200, None, &[add(150)]).unwrap(),
        150
    );
    assert_eq!(
        select_tx_instant(&base, 200, Some(150), &[add(150)]).unwrap(),
        150
    );
    assert_eq!(select_tx_instant(&base, 200, Some(100), &[]).unwrap(), 100);
    for (now, option, ops, code) in [
        (
            200,
            None,
            vec![add(150), add(150)],
            "transaction/multiple-tx-instants",
        ),
        (
            200,
            Some(160),
            vec![add(150)],
            "transaction/tx-instant-mismatch",
        ),
        (200, None, vec![add(201)], "transaction/future-tx-instant"),
        (200, Some(99), vec![], "transaction/past-tx-instant"),
    ] {
        assert_eq!(
            select_tx_instant(&base, now, option, &ops)
                .unwrap_err()
                .code,
            code
        );
    }
}

#[test]
fn durable_clock_selects_normalized_maps_without_changing_speculation() {
    let base = Database::bootstrap().unwrap().database_value();
    let forms = [TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Tx),
        attributes: vec![(
            AttributeRef::Ident(Keyword::new("db", "txInstant")),
            scalar(Value::Instant(150)),
        )],
    })];
    let options = crate::TransactionExecutionOptions::default();
    let assessed = assess_durable_forms(
        &base,
        &forms,
        200,
        None,
        SpeculationLimits::default(),
        &options,
    )
    .unwrap();
    assert_eq!(assessed.db_after.last_tx_instant().unwrap(), Some(150));
    assert!(
        assessed
            .tx_data
            .iter()
            .any(|d| d.attribute == crate::DB_TX_INSTANT as u32 && d.value == Value::Instant(150))
    );
    let error = assess_forms(&base, &forms, 200, SpeculationLimits::default(), &options)
        .err()
        .unwrap();
    assert_eq!(error.code, "transaction/tx-instant-mismatch");
    assert_eq!(
        assess_forms(&base, &forms, 150, SpeculationLimits::default(), &options)
            .unwrap()
            .tx_data,
        assessed.tx_data
    );
}

#[test]
fn durable_clock_expands_generated_instant_once_even_when_rejected() {
    use std::sync::atomic::{AtomicUsize, Ordering};
    let invoked = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&invoked);
    let name = crate::Symbol::new("test.clock.v1", "select");
    let mut registry = crate::NativeRegistry::builder();
    registry
        .transaction(name.clone(), move |_, _, control| {
            control.check(1)?;
            observed.fetch_add(1, Ordering::SeqCst);
            Ok(vec![TxForm::EntityMap(EntityMap {
                id: Some(EntityRef::Tx),
                attributes: vec![(
                    AttributeRef::Ident(Keyword::new("db", "txInstant")),
                    scalar(Value::Instant(150)),
                )],
            })])
        })
        .unwrap();
    let options = crate::TransactionExecutionOptions {
        native: registry.build(),
        ..Default::default()
    };
    let base = Database::bootstrap().unwrap().database_value();
    let forms = [TxForm::ProgramCall(ProgramCall {
        function: CallableRef::Local(name),
        arguments: vec![],
    })];
    let assessed = assess_durable_forms(
        &base,
        &forms,
        200,
        None,
        SpeculationLimits::default(),
        &options,
    )
    .unwrap();
    assert_eq!(assessed.db_after.last_tx_instant().unwrap(), Some(150));
    assert_eq!(invoked.load(Ordering::SeqCst), 1);
    let error = assess_durable_forms(
        &base,
        &forms,
        200,
        Some(160),
        SpeculationLimits::default(),
        &options,
    )
    .err()
    .unwrap();
    assert_eq!(error.code, "transaction/tx-instant-mismatch");
    assert_eq!(invoked.load(Ordering::SeqCst), 2);
}

#[test]
fn shared_assessment_strips_attempt_state_and_speculation_reapplies_views() {
    let mut schema = crate::Schema::new();
    schema
        .install(Attribute::new(
            KEY,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let seeded = Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: KEY,
                value: Value::Long(1).into(),
            }],
            10,
        )
        .unwrap();
    let entity = seeded.tempids["item"];
    let base = seeded.db_after.database_value();
    let filtered = base.clone().as_of(0).filter(|_, _| false);
    let forms = [TxForm::Op(TxOp::Add {
        entity: EntityRef::Id(entity),
        attribute: KEY,
        value: Value::Long(2).into(),
    })];
    let options = crate::TransactionExecutionOptions::default();
    let assessed = assess_forms(
        &filtered,
        &forms,
        20,
        SpeculationLimits::default(),
        &options,
    )
    .unwrap();
    assert_eq!(
        assessed.db_before.values(entity, KEY).unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(
        assessed.db_after.values(entity, KEY).unwrap(),
        vec![Value::Long(2)]
    );
    assert!(assessed.db_before.transaction_read_context().is_none());
    assert!(assessed.db_after.transaction_read_context().is_none());
    assert!(!assessed.db_after.is_filtered());
    assert_eq!(assessed.db_after.as_of_t(), None);
    assert!(assessed.read_work.logical_datoms > 0);
    assert!(assessed.read_work.prefix_misses > 0);
    assert!(assessed.assessment_work.prefixes > 0);
    assert_eq!(assessed.basis_t, base.basis_t() + 1);
    assert_eq!(assessed.eidx_frontier, assessed.db_after.eidx_frontier());
    assert!(assessed.retained_programs.is_empty());

    let speculative = filtered
        .with_forms_with_execution_options(&forms, 20, SpeculationLimits::default(), &options)
        .unwrap();
    assert_eq!(speculative.tx_data, assessed.tx_data);
    assert_eq!(speculative.tempids, assessed.tempids);
    assert!(speculative.db_before.is_filtered());
    assert!(speculative.db_after.is_filtered());
    assert_eq!(speculative.db_after.as_of_t(), Some(0));
    assert!(
        speculative
            .db_after
            .datoms(crate::IndexOrder::Eavt)
            .unwrap()
            .is_empty()
    );
    assert_eq!(base.values(entity, KEY).unwrap(), vec![Value::Long(1)]);
    let error = assess_forms(
        &base.history(),
        &forms,
        20,
        SpeculationLimits::default(),
        &options,
    )
    .err()
    .expect("history must remain nontransactable");
    assert_eq!(error.code, "transaction/history-with");
}

#[test]
fn shared_assessment_returns_dormant_program_closure_for_publication_protection() {
    use crate::program::ValidatedProgram;
    use crate::{Instruction, Program, ProgramKind};
    let leaf = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![Instruction::Return],
    };
    let leaf_hash = crate::program_hash(&leaf).unwrap();
    let root = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Function(leaf_hash)),
            Instruction::Pop,
            Instruction::Return,
        ],
    };
    let root_hash = crate::program_hash(&root).unwrap();
    let programs = BTreeMap::from([
        (leaf_hash, Arc::new(ValidatedProgram::from_canonical(leaf))),
        (root_hash, Arc::new(ValidatedProgram::from_canonical(root))),
    ]);
    let base = Database::bootstrap()
        .unwrap()
        .database_value()
        .retain_programs(programs);
    let forms = [
        TxForm::Op(TxOp::Add {
            entity: EntityRef::Temp("code".into()),
            attribute: crate::DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("test", "code")).into(),
        }),
        TxForm::Op(TxOp::Add {
            entity: EntityRef::Temp("code".into()),
            attribute: crate::DB_FN as u32,
            value: Value::Function(root_hash).into(),
        }),
    ];
    let assessed = assess_forms(
        &base,
        &forms,
        10,
        SpeculationLimits::default(),
        &crate::TransactionExecutionOptions::default(),
    )
    .unwrap();
    assert_eq!(
        assessed
            .retained_programs
            .keys()
            .copied()
            .collect::<BTreeSet<_>>(),
        BTreeSet::from([root_hash, leaf_hash])
    );
    for hash in [root_hash, leaf_hash] {
        assert!(Arc::ptr_eq(
            &assessed.retained_programs[&hash],
            &assessed.db_after.resolve_program(hash).unwrap()
        ));
    }
    assert!(assessed.db_after.transaction_read_context().is_none());
}

#[test]
fn callback_discovery_preserves_cumulative_primitive_admission() {
    use std::sync::atomic::{AtomicUsize, Ordering};
    let database = Database::bootstrap().unwrap();
    let invoked = Arc::new(AtomicUsize::new(0));
    let mut functions = TxFunctions::new();
    let count = Arc::clone(&invoked);
    functions.register("emit", move |_, _| {
        count.fetch_add(1, Ordering::Relaxed);
        Ok(vec![TxForm::Op(TxOp::Add {
            entity: EntityRef::Temp("some-entity".into()),
            attribute: crate::DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("test", "entity")).into(),
        })])
    });
    functions.register("branch", |_, _| {
        Ok((0..100)
            .map(|_| {
                TxForm::Call(TxCall {
                    function: "emit".into(),
                    arguments: Vec::new(),
                })
            })
            .collect())
    });
    let error = database
        .normalize_forms_with_limit(
            &[TxForm::Call(TxCall {
                function: "branch".into(),
                arguments: Vec::new(),
            })],
            &functions,
            2,
        )
        .unwrap_err();
    assert_eq!(error.code, "postgres/transaction-op-capacity");
    assert_eq!(
        invoked.load(Ordering::Relaxed),
        3,
        "stop at the first over-budget primitive; do not invoke the remaining 97 callbacks"
    );
}

#[test]
fn nested_component_maps_emit_partition_affinity_but_primitive_edges_do_not() {
    let mut schema = crate::Schema::new();
    schema
        .install(Attribute::new(
            KEY,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                CHILD,
                Keyword::new("person", "child"),
                ValueType::Ref,
                Cardinality::One,
            )
            .component(),
        )
        .unwrap();
    let db = Database::new(schema).unwrap();
    let forms = [TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Temp("parent".into())),
        attributes: vec![(
            AttributeRef::Id(CHILD),
            MapValue::Nested(Box::new(EntityMap {
                id: Some(EntityRef::Temp("child".into())),
                attributes: vec![(AttributeRef::Id(KEY), scalar(Value::String("child".into())))],
            })),
        )],
    })];
    let normalized = db.normalize_forms(&forms, &TxFunctions::new()).unwrap();
    assert!(normalized.iter().any(|op| matches!(op, TxOp::MatchPartition { tempid, entity: EntityRef::Temp(parent) } if tempid == "child" && parent == "parent")));
    let exact = db
        .database_value()
        .normalize_persisted_forms_with_limit(&forms, 16)
        .unwrap();
    assert_eq!(normalized.len(), exact.len());
    assert!(
        normalized
            .iter()
            .zip(&exact)
            .all(|(a, b)| compare_tx_op(a, b) == Ordering::Equal)
    );
    let primitives = [TxForm::Op(TxOp::Add {
        entity: EntityRef::Temp("parent".into()),
        attribute: CHILD,
        value: TxValue::Entity(EntityRef::Temp("child".into())),
    })];
    assert_eq!(
        db.normalize_forms(&primitives, &TxFunctions::new())
            .unwrap()
            .len(),
        1
    );
}

#[test]
fn partition_install_maps_lower_to_ordinary_installation_facts() {
    let db = Database::bootstrap().unwrap();
    for attribute in [
        AttributeRef::ReverseId(crate::DB_INSTALL_PARTITION as u32),
        AttributeRef::ReverseIdent(Keyword::new("db.install", "partition")),
        AttributeRef::Ident(Keyword::new("db.install", "_partition")),
    ] {
        let forms = [TxForm::EntityMap(EntityMap {
            id: Some(EntityRef::Temp("partition".into())),
            attributes: vec![
                (
                    AttributeRef::Id(crate::DB_IDENT as u32),
                    scalar(Value::Keyword(Keyword::new("part", "customers"))),
                ),
                (
                    attribute,
                    scalar(Value::Keyword(Keyword::new("db.part", "db"))),
                ),
            ],
        })];
        let ops = db.normalize_forms(&forms, &TxFunctions::new()).unwrap();
        assert_eq!(ops.len(), 2);
        assert!(ops.iter().any(|op| matches!(op, TxOp::Add { entity: EntityRef::Ident(ident), attribute, value: TxValue::Entity(EntityRef::Temp(tempid)) } if ident == &Keyword::new("db.part", "db") && u64::from(*attribute) == crate::DB_INSTALL_PARTITION && tempid == "partition")));
        assert!(!ops.iter().any(|op| matches!(
            op,
            TxOp::ForcePartition { .. } | TxOp::MatchPartition { .. }
        )));
        let exact = db
            .database_value()
            .normalize_persisted_forms_with_limit(&forms, 16)
            .unwrap();
        assert!(
            ops.iter()
                .zip(&exact)
                .all(|(a, b)| compare_tx_op(a, b) == Ordering::Equal)
        );
    }
}

#[test]
fn map_and_many_collections_have_an_order_independent_structural_key() {
    let left = EntityMap {
        id: None,
        attributes: vec![
            (
                AttributeRef::Id(12),
                MapValue::Many(vec![
                    scalar(Value::String("z".into())),
                    scalar(Value::String("a".into())),
                ]),
            ),
            (
                AttributeRef::Ident(Keyword::new("person", "name")),
                scalar(Value::String("Ada".into())),
            ),
        ],
    };
    let right = EntityMap {
        id: None,
        attributes: vec![
            (
                AttributeRef::Ident(Keyword::new("person", "name")),
                scalar(Value::String("Ada".into())),
            ),
            (
                AttributeRef::Id(12),
                MapValue::Many(vec![
                    scalar(Value::String("a".into())),
                    scalar(Value::String("z".into())),
                ]),
            ),
        ],
    };

    assert_eq!(compare_entity_map(&left, &right), Ordering::Equal);
    assert_eq!(compare_entity_map(&right, &left), Ordering::Equal);

    let op = TxForm::Op(TxOp::RetractEntity(EntityRef::Tx));
    let mut first = vec![TxForm::EntityMap(left), op.clone()];
    let mut second = vec![op, TxForm::EntityMap(right)];
    first.sort_by(compare_tx_form);
    second.sort_by(compare_tx_form);
    assert!(
        first
            .iter()
            .zip(&second)
            .all(|(left, right)| compare_tx_form(left, right) == Ordering::Equal)
    );
}

#[test]
fn structural_value_ties_preserve_stored_numeric_representations() {
    let one_scale = Value::BigDec(BigDecimal::from_str("1.0").unwrap());
    let two_scale = Value::BigDec(BigDecimal::from_str("1.00").unwrap());
    assert_eq!(one_scale.index_cmp(&two_scale), Ordering::Equal);
    assert_ne!(compare_value(&one_scale, &two_scale), Ordering::Equal);
    assert_eq!(
        compare_value(&one_scale, &two_scale),
        compare_value(&two_scale, &one_scale).reverse()
    );

    let integer = Value::Long(1);
    assert_eq!(integer.index_cmp(&one_scale), Ordering::Equal);
    assert_ne!(compare_value(&integer, &one_scale), Ordering::Equal);

    let negative_zero = Value::Double(-0.0);
    let positive_zero = Value::Double(0.0);
    assert_eq!(negative_zero.index_cmp(&positive_zero), Ordering::Equal);
    assert_ne!(
        compare_value(&negative_zero, &positive_zero),
        Ordering::Equal
    );
}

#[test]
fn exact_entity_map_normalizer_preserves_nested_identity() {
    let mut schema = crate::Schema::new();
    schema
        .install(
            Attribute::new(
                KEY,
                Keyword::new("person", "key"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            CHILD,
            Keyword::new("person", "child"),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();
    let database = Database::new(schema).unwrap();
    let forms = vec![TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Temp("parent".into())),
        attributes: vec![(
            AttributeRef::Ident(Keyword::new("person", "child")),
            MapValue::Nested(Box::new(EntityMap {
                id: None,
                attributes: vec![(
                    AttributeRef::Ident(Keyword::new("person", "key")),
                    scalar(Value::String("child-key".into())),
                )],
            })),
        )],
    })];

    let exact = database
        .database_value()
        .normalize_persisted_forms_with_limit(&forms, 16)
        .unwrap();
    assert_eq!(exact.len(), 2);
    let child = exact
        .iter()
        .find_map(|op| match op {
            TxOp::Add {
                entity: EntityRef::Temp(child),
                attribute,
                value,
            } if *attribute == KEY
                && matches!(value, TxValue::Scalar(Value::String(value)) if value == "child-key") =>
            {
                Some(child)
            }
            _ => None,
        })
        .expect("nested identity assertion");
    assert!(exact.iter().any(|op| matches!(op,
        TxOp::Add { entity: EntityRef::Temp(parent), attribute, value: TxValue::Entity(EntityRef::Temp(reference)) }
            if parent == "parent" && *attribute == CHILD && reference == child
    )));
}

#[test]
fn local_callbacks_require_an_explicit_registry() {
    let database = Database::bootstrap().unwrap().database_value();
    let error = database
        .normalize_persisted_forms_with_limit(
            &[TxForm::Call(TxCall {
                function: "local/only".into(),
                arguments: Vec::new(),
            })],
            16,
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/missing-function-context");
}

#[test]
fn entity_predicate_uses_one_exact_value_interface() {
    let database = Database::bootstrap().unwrap();
    let mut functions = TxFunctions::new();
    functions.register_entity_value_predicate("test/exists", |database, entity| {
        Ok(RuntimeValue::Scalar(Value::Bool(
            !database.values(entity, crate::DB_IDENT as u32)?.is_empty(),
        )))
    });
    let system_entity = crate::DB_IDENT;

    assert_eq!(
        functions
            .validate_entity_predicate("test/exists", &database.database_value(), system_entity)
            .unwrap(),
        RuntimeValue::Scalar(Value::Bool(true))
    );
    let overlay = database
        .database_value()
        .with_functions(&[], &TxFunctions::new(), 1)
        .unwrap()
        .db_after;
    assert_eq!(
        functions
            .validate_entity_predicate("test/exists", &overlay, system_entity,)
            .unwrap(),
        RuntimeValue::Scalar(Value::Bool(true))
    );
}

#[test]
fn local_callbacks_share_one_assessor_across_memory_and_speculative_values() {
    let mut schema = crate::Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("account", "balance"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let entity = crate::make_eid(crate::USER_PARTITION, 42).unwrap();
    let memory = Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: 1_000,
                value: Value::Long(10).into(),
            }],
            1,
        )
        .unwrap()
        .db_after;
    let mut functions = TxFunctions::new();
    functions.register("account/increment", move |before, _| {
        let values = before.values(entity, 1_000)?;
        let [Value::Long(value)] = values.as_slice() else {
            panic!("one balance")
        };
        Ok(vec![TxForm::Op(TxOp::Cas {
            entity: EntityRef::Id(entity),
            attribute: 1_000,
            old: Some(Value::Long(*value).into()),
            new: Value::Long(value + 1).into(),
        })])
    });
    let forms = [TxForm::Call(TxCall {
        function: "account/increment".into(),
        arguments: vec![],
    })];
    let exact = memory.database_value();
    let memory_report = memory.with_forms(&forms, &functions, 2).unwrap();
    let overlay_report = exact.with_functions(&forms, &functions, 2).unwrap();
    assert_eq!(memory_report.tx_data, overlay_report.tx_data);
    assert_eq!(
        overlay_report.db_after.values(entity, 1_000).unwrap(),
        vec![Value::Long(11)]
    );
    assert_eq!(exact.values(entity, 1_000).unwrap(), vec![Value::Long(10)]);
    assert!(overlay_report.db_after.transaction_read_context().is_none());
    let branch = overlay_report
        .db_after
        .with_functions(&forms, &functions, 3)
        .unwrap();
    assert_eq!(
        branch.db_after.values(entity, 1_000).unwrap(),
        vec![Value::Long(12)]
    );
    assert_eq!(
        overlay_report.db_after.values(entity, 1_000).unwrap(),
        vec![Value::Long(11)]
    );
    let conflict = [TxForm::Op(TxOp::Cas {
        entity: EntityRef::Id(entity),
        attribute: 1_000,
        old: Some(Value::Long(10).into()),
        new: Value::Long(99).into(),
    })];
    assert_eq!(
        memory_report
            .db_after
            .with_forms(&conflict, &functions, 3)
            .unwrap_err()
            .code,
        overlay_report
            .db_after
            .with_functions(&conflict, &functions, 3)
            .unwrap_err()
            .code,
    );
}
