use atomic_core::{
    Attribute, Cardinality, DB_ENSURE, DB_ENTITY_ATTRS, DB_ENTITY_PREDS, DB_IDENT, Database,
    EntityRef, ErrorCategory, IndexOrder, Keyword, Schema, Symbol, TupleSpec, TxForm, TxFunctions,
    TxOp, TxValue, USER_PARTITION, Unique, Value, ValueType, View, make_eid, t_to_tx,
};
use std::sync::Arc;
use std::sync::atomic::{AtomicUsize, Ordering as AtomicOrdering};

const DB_TX_INSTANT: u32 = 50;
const EMAIL: u32 = 1_000;
const NAME: u32 = 1_001;
const ALIAS: u32 = 1_002;
const MANAGES: u32 = 1_003;
const PART_A: u32 = 1_004;
const PART_B: u32 = 1_005;
const COMPOSITE: u32 = 1_006;

fn keyword(namespace: &str, name: &str) -> Keyword {
    Keyword::new(namespace, name)
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

fn attribute(
    id: u32,
    namespace: &str,
    name: &str,
    value_type: ValueType,
    cardinality: Cardinality,
) -> Attribute {
    Attribute::new(id, keyword(namespace, name), value_type, cardinality)
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            attribute(
                EMAIL,
                "person",
                "email",
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(attribute(
            NAME,
            "person",
            "name",
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(attribute(
            ALIAS,
            "person",
            "alias",
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(
            attribute(MANAGES, "org", "manages", ValueType::Ref, Cardinality::Many).component(),
        )
        .unwrap();
    schema
        .install(attribute(
            PART_A,
            "item",
            "part-a",
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(attribute(
            PART_B,
            "item",
            "part-b",
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            attribute(
                COMPOSITE,
                "item",
                "parts",
                ValueType::Tuple,
                Cardinality::One,
            )
            .tuple(TupleSpec::Composite(vec![PART_A, PART_B]))
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
}

fn empty_db() -> Database {
    Database::new(schema()).unwrap()
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: TxValue::Scalar(value),
    }
}

fn retract(entity: EntityRef, attribute: u32, value: Option<Value>) -> TxOp {
    TxOp::Retract {
        entity,
        attribute,
        value: value.map(TxValue::Scalar),
    }
}

#[test]
fn database_values_are_immutable_and_every_transaction_is_reified() {
    let before = empty_db();
    let report = before
        .with(
            &[add(
                EntityRef::Temp("p".into()),
                NAME,
                Value::String("Ada".into()),
            )],
            1_000,
        )
        .unwrap();

    assert_eq!(before.basis_t(), 1);
    assert!(
        before
            .datoms(View::Current, IndexOrder::Eavt)
            .iter()
            .all(|datom| datom.attribute != NAME)
    );
    assert_eq!(report.db_after.basis_t(), 2);
    assert!(report.tx_data.iter().any(|datom| {
        datom.entity == t_to_tx(2).unwrap()
            && datom.attribute == DB_TX_INSTANT
            && datom.value == Value::Instant(1_000)
    }));
}

#[test]
fn fresh_tempids_with_the_same_identity_unify_independent_of_input_order() {
    let ops = vec![
        add(
            EntityRef::Temp("left".into()),
            EMAIL,
            Value::String("ada@example.test".into()),
        ),
        add(
            EntityRef::Temp("right".into()),
            EMAIL,
            Value::String("ada@example.test".into()),
        ),
        add(
            EntityRef::Temp("right".into()),
            NAME,
            Value::String("Ada".into()),
        ),
    ];
    let mut reversed = ops.clone();
    reversed.reverse();

    let first = empty_db().with(&ops, 1_000).unwrap();
    let second = empty_db().with(&reversed, 1_000).unwrap();

    assert_eq!(first.tempids["left"], first.tempids["right"]);
    assert_eq!(first.tempids, second.tempids);
    assert_eq!(first.tx_data, second.tx_data);
}

#[test]
fn identity_upsert_merges_all_tempid_facts_into_db_before_entity() {
    let initial = empty_db()
        .with(
            &[add(
                EntityRef::Temp("original".into()),
                EMAIL,
                Value::String("ada@example.test".into()),
            )],
            1_000,
        )
        .unwrap();
    let existing = initial.tempids["original"];

    let upsert = initial
        .db_after
        .with(
            &[
                add(
                    EntityRef::Temp("upsert".into()),
                    EMAIL,
                    Value::String("ada@example.test".into()),
                ),
                add(
                    EntityRef::Temp("upsert".into()),
                    NAME,
                    Value::String("Ada".into()),
                ),
            ],
            2_000,
        )
        .unwrap();

    assert_eq!(upsert.tempids["upsert"], existing);
    assert_eq!(
        upsert.db_after.values(existing, NAME),
        vec![&Value::String("Ada".into())]
    );
}

#[test]
fn lookup_refs_see_db_before_but_not_entities_created_in_the_same_transaction() {
    let result = empty_db().with(
        &[
            add(
                EntityRef::Temp("new".into()),
                EMAIL,
                Value::String("new@example.test".into()),
            ),
            add(
                EntityRef::Lookup {
                    attribute: EMAIL,
                    value: Value::String("new@example.test".into()),
                },
                NAME,
                Value::String("New".into()),
            ),
        ],
        1_000,
    );

    let error = result.unwrap_err();
    assert_eq!(error.category, ErrorCategory::Incorrect);
    assert_eq!(error.code, "transaction/lookup-not-found");
}

#[test]
fn same_eav_add_and_retract_conflict_atomically() {
    let before = empty_db();
    let result = before.with(
        &[
            add(EntityRef::Id(42), NAME, Value::String("Ada".into())),
            retract(EntityRef::Id(42), NAME, Some(Value::String("Ada".into()))),
        ],
        1_000,
    );

    let error = result.unwrap_err();
    assert_eq!(error.category, ErrorCategory::Conflict);
    assert_eq!(error.code, "transaction/datoms-conflict");
    assert_eq!(before.basis_t(), 1);
}

#[test]
fn two_different_cardinality_one_additions_conflict() {
    let error = empty_db()
        .with(
            &[
                add(EntityRef::Id(42), NAME, Value::String("Ada".into())),
                add(EntityRef::Id(42), NAME, Value::String("Grace".into())),
            ],
            1_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/cardinality-one-conflict");
}

#[test]
fn a_cardinality_one_replacement_records_retraction_and_assertion_together() {
    let first = empty_db()
        .with(
            &[add(EntityRef::Id(42), NAME, Value::String("Ada".into()))],
            1_000,
        )
        .unwrap();
    let second = first
        .db_after
        .with(
            &[add(
                EntityRef::Id(42),
                NAME,
                Value::String("Augusta Ada".into()),
            )],
            2_000,
        )
        .unwrap();

    assert!(second.tx_data.iter().any(|datom| {
        datom.entity == 42
            && datom.attribute == NAME
            && datom.value == Value::String("Ada".into())
            && !datom.added
    }));
    assert_eq!(
        second.db_after.values(42, NAME),
        vec![&Value::String("Augusta Ada".into())]
    );
}

#[test]
fn value_less_retract_expands_only_against_db_before() {
    let first = empty_db()
        .with(
            &[add(EntityRef::Id(42), NAME, Value::String("Ada".into()))],
            1_000,
        )
        .unwrap();
    let second = first
        .db_after
        .with(
            &[
                retract(EntityRef::Id(42), NAME, None),
                add(EntityRef::Id(42), NAME, Value::String("Grace".into())),
            ],
            2_000,
        )
        .unwrap();
    assert_eq!(
        second.db_after.values(42, NAME),
        vec![&Value::String("Grace".into())]
    );
}

#[test]
fn cas_observes_db_before() {
    let first = empty_db()
        .with(
            &[add(EntityRef::Id(42), NAME, Value::String("Ada".into()))],
            1_000,
        )
        .unwrap();
    let failure = first.db_after.with(
        &[TxOp::Cas {
            entity: EntityRef::Id(42),
            attribute: NAME,
            old: Some(Value::String("Grace".into()).into()),
            new: Value::String("Augusta".into()).into(),
        }],
        2_000,
    );
    assert_eq!(failure.unwrap_err().code, "transaction/cas-failed");

    let success = first
        .db_after
        .with(
            &[TxOp::Cas {
                entity: EntityRef::Id(42),
                attribute: NAME,
                old: Some(Value::String("Ada".into()).into()),
                new: Value::String("Augusta".into()).into(),
            }],
            2_000,
        )
        .unwrap();
    assert_eq!(
        success.db_after.values(42, NAME),
        vec![&Value::String("Augusta".into())]
    );
}

#[test]
fn entity_ensure_validates_db_after() {
    let spec_ident = keyword("person", "validation");
    let spec = empty_db()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(spec_ident.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_ENTITY_ATTRS as u32,
                    value: Value::Keyword(keyword("person", "name")).into(),
                },
            ],
            1_000,
        )
        .unwrap();

    let report = spec
        .db_after
        .with(
            &[
                add(EntityRef::Id(42), NAME, Value::String("Ada".into())),
                TxOp::Add {
                    entity: EntityRef::Id(42),
                    attribute: DB_ENSURE as u32,
                    value: TxValue::Entity(EntityRef::Ident(spec_ident.clone())),
                },
            ],
            2_000,
        )
        .unwrap();
    assert_eq!(report.db_after.basis_t(), 3);
    assert!(
        report
            .db_after
            .datoms(View::History, IndexOrder::Eavt)
            .iter()
            .all(|datom| datom.attribute != DB_ENSURE as u32)
    );

    let error = spec
        .db_after
        .with(
            &[TxOp::Ensure {
                entity: EntityRef::Id(42),
                spec: EntityRef::Ident(spec_ident),
            }],
            2_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/entity-spec");
}

#[test]
fn entity_predicate_reads_the_complete_db_after() {
    let spec_ident = keyword("person", "name-length");
    let spec = empty_db()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(spec_ident.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_ENTITY_ATTRS as u32,
                    value: Value::Keyword(keyword("person", "name")).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_ENTITY_ATTRS as u32,
                    value: Value::Keyword(keyword("item", "part-a")).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_ENTITY_PREDS as u32,
                    value: Value::Symbol(Symbol::new("person", "name-length-matches?")).into(),
                },
            ],
            1_000,
        )
        .unwrap();

    let calls = Arc::new(AtomicUsize::new(0));
    let predicate_calls = Arc::clone(&calls);
    let mut functions = TxFunctions::new();
    functions.register_entity_predicate("person/name-length-matches?", move |db, entity| {
        predicate_calls.fetch_add(1, AtomicOrdering::SeqCst);
        let name = match db.values(entity, NAME).as_slice() {
            [Value::String(name)] => name,
            _ => return Ok(false),
        };
        let length = match db.values(entity, PART_A).as_slice() {
            [Value::Long(length)] => *length,
            _ => return Ok(false),
        };
        Ok(i64::try_from(name.len()).ok() == Some(length))
    });
    let ensure = |length| {
        vec![
            TxForm::Op(add(EntityRef::Id(42), NAME, Value::String("Ada".into()))),
            TxForm::Op(add(EntityRef::Id(42), PART_A, Value::Long(length))),
            TxForm::Op(TxOp::Ensure {
                entity: EntityRef::Id(42),
                spec: EntityRef::Ident(spec_ident.clone()),
            }),
        ]
    };

    let error = spec
        .db_after
        .with_forms(&ensure(4), &functions, 2_000)
        .unwrap_err();
    assert_eq!(error.code, "transaction/entity-predicate");
    assert_eq!(calls.load(AtomicOrdering::SeqCst), 1);

    let mut duplicate_ensure = ensure(3);
    duplicate_ensure.push(TxForm::Op(TxOp::Ensure {
        entity: EntityRef::Id(42),
        spec: EntityRef::Ident(spec_ident.clone()),
    }));
    let report = spec
        .db_after
        .with_forms(&duplicate_ensure, &functions, 2_000)
        .unwrap();
    assert_eq!(report.db_after.values(42, PART_A), vec![&Value::Long(3)]);
    assert_eq!(calls.load(AtomicOrdering::SeqCst), 2);
}

#[test]
fn entity_predicate_names_must_be_fully_qualified_at_the_source_transaction() {
    let error = empty_db()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("spec".into()),
                attribute: DB_ENTITY_PREDS as u32,
                value: Value::Symbol(Symbol::unqualified("predicate")).into(),
            }],
            1_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/unqualified-entity-predicate");
}

#[test]
fn retract_entity_recurses_through_components() {
    let initial = empty_db()
        .with(
            &[
                add(
                    EntityRef::Id(user(42)),
                    NAME,
                    Value::String("Parent".into()),
                ),
                TxOp::Add {
                    entity: EntityRef::Id(user(42)),
                    attribute: MANAGES,
                    value: TxValue::Entity(EntityRef::Id(user(43))),
                },
                add(EntityRef::Id(user(43)), NAME, Value::String("Child".into())),
            ],
            1_000,
        )
        .unwrap();
    let retracted = initial
        .db_after
        .with(&[TxOp::RetractEntity(EntityRef::Id(user(42)))], 2_000)
        .unwrap();
    assert!(retracted.db_after.values(user(42), NAME).is_empty());
    assert!(retracted.db_after.values(user(43), NAME).is_empty());
}

#[test]
fn composite_tuples_are_derived_with_nil_slots_and_replaced() {
    let first = empty_db()
        .with(&[add(EntityRef::Id(42), PART_A, Value::Long(7))], 1_000)
        .unwrap();
    assert_eq!(
        first.db_after.values(42, COMPOSITE),
        vec![&Value::Tuple(vec![Some(Value::Long(7)), None])]
    );

    let second = first
        .db_after
        .with(
            &[add(EntityRef::Id(42), PART_B, Value::String("x".into()))],
            2_000,
        )
        .unwrap();
    assert_eq!(
        second.db_after.values(42, COMPOSITE),
        vec![&Value::Tuple(vec![
            Some(Value::Long(7)),
            Some(Value::String("x".into()))
        ])]
    );
}

#[test]
fn explicit_composite_value_is_an_upsert_hint_not_a_direct_write() {
    let first = empty_db()
        .with(
            &[
                add(EntityRef::Id(42), PART_A, Value::Long(7)),
                add(EntityRef::Id(42), PART_B, Value::String("x".into())),
            ],
            1_000,
        )
        .unwrap();
    let tuple = Value::Tuple(vec![Some(Value::Long(7)), Some(Value::String("x".into()))]);
    let upsert = first
        .db_after
        .with(
            &[
                add(EntityRef::Temp("item".into()), COMPOSITE, tuple),
                add(
                    EntityRef::Temp("item".into()),
                    NAME,
                    Value::String("merged".into()),
                ),
            ],
            2_000,
        )
        .unwrap();

    assert_eq!(upsert.tempids["item"], 42);
    assert_eq!(
        upsert.db_after.values(42, NAME),
        vec![&Value::String("merged".into())]
    );
    assert!(
        !upsert
            .tx_data
            .iter()
            .any(|datom| datom.attribute == COMPOSITE)
    );
}

#[test]
fn derived_composite_identity_does_not_retroactively_resolve_a_tempid() {
    let first = empty_db()
        .with(
            &[
                add(EntityRef::Id(42), PART_A, Value::Long(7)),
                add(EntityRef::Id(42), PART_B, Value::String("x".into())),
            ],
            1_000,
        )
        .unwrap();
    let error = first
        .db_after
        .with(
            &[
                add(EntityRef::Temp("item".into()), PART_A, Value::Long(7)),
                add(
                    EntityRef::Temp("item".into()),
                    PART_B,
                    Value::String("x".into()),
                ),
            ],
            2_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/unique-conflict");
}

#[test]
fn time_views_distinguish_current_as_of_since_and_history() {
    let first = empty_db()
        .with(
            &[add(EntityRef::Id(42), NAME, Value::String("Ada".into()))],
            1_000,
        )
        .unwrap();
    let second = first
        .db_after
        .with(
            &[add(
                EntityRef::Id(42),
                NAME,
                Value::String("Augusta".into()),
            )],
            2_000,
        )
        .unwrap();

    let current = second.db_after.datoms(View::Current, IndexOrder::Eavt);
    let as_of = second.db_after.datoms(View::AsOf(2), IndexOrder::Eavt);
    let since = second.db_after.datoms(View::Since(2), IndexOrder::Eavt);
    let history = second.db_after.datoms(View::History, IndexOrder::Eavt);

    assert!(
        current
            .iter()
            .any(|d| d.value == Value::String("Augusta".into()))
    );
    assert!(as_of.iter().any(|d| d.value == Value::String("Ada".into())));
    assert!(
        since
            .iter()
            .any(|d| d.value == Value::String("Augusta".into()))
    );
    assert!(
        history
            .iter()
            .any(|d| !d.added && d.value == Value::String("Ada".into()))
    );
}

#[test]
fn explicit_transaction_instants_are_monotonic() {
    let first = empty_db().with(&[], 2_000).unwrap();
    let error = first.db_after.with(&[], 1_999).unwrap_err();
    assert_eq!(error.code, "transaction/non-monotonic-instant");
}

#[test]
fn schema_installation_and_rename_preserve_core_invariants() {
    let mut schema = Schema::new();
    let invalid = attribute(
        90,
        "bad",
        "many-unique",
        ValueType::String,
        Cardinality::Many,
    )
    .unique(Unique::Identity);
    assert_eq!(
        schema.install(invalid).unwrap_err().code,
        "schema/unique-must-be-cardinality-one"
    );

    schema
        .install(attribute(
            91,
            "person",
            "old-name",
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    let old = keyword("person", "old-name");
    let new = keyword("person", "new-name");
    schema.rename(91, new.clone()).unwrap();
    assert_eq!(schema.resolve_ident(&old), Some(91));
    assert_eq!(schema.resolve_ident(&new), Some(91));
    assert_eq!(schema.attribute(91).unwrap().ident, new);
}

#[test]
fn schema_change_validation_uses_current_facts_and_immutable_fields() {
    let populated = empty_db()
        .with(
            &[
                add(EntityRef::Id(42), ALIAS, Value::String("Ada".into())),
                add(
                    EntityRef::Id(42),
                    ALIAS,
                    Value::String("Enchantress".into()),
                ),
            ],
            1_000,
        )
        .unwrap();
    let mut to_one = populated
        .db_after
        .schema()
        .attribute(ALIAS)
        .unwrap()
        .clone();
    to_one.cardinality = Cardinality::One;
    assert_eq!(
        populated
            .db_after
            .validate_schema_change(&to_one)
            .unwrap_err()
            .code,
        "schema/cardinality-change-conflict"
    );

    let mut changed_type = populated.db_after.schema().attribute(NAME).unwrap().clone();
    changed_type.value_type = ValueType::Long;
    assert_eq!(
        populated
            .db_after
            .validate_schema_change(&changed_type)
            .unwrap_err()
            .code,
        "schema/value-type-immutable"
    );
}

#[test]
fn newly_declared_but_uninstalled_attributes_are_not_visible_in_same_transaction() {
    let error = empty_db()
        .with(
            &[add(
                EntityRef::Temp("entity".into()),
                99_999,
                Value::String("not installed".into()),
            )],
            1_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "schema/unknown-attribute");
}

#[test]
fn error_selection_is_stable_across_transaction_permutations() {
    let ops = vec![
        add(EntityRef::Id(42), NAME, Value::String("Ada".into())),
        add(EntityRef::Id(42), NAME, Value::String("Grace".into())),
        add(EntityRef::Id(42), 99_999, Value::String("unknown".into())),
    ];
    let mut reversed = ops.clone();
    reversed.reverse();
    let first = empty_db().with(&ops, 1_000).unwrap_err();
    let second = empty_db().with(&reversed, 1_000).unwrap_err();
    assert_eq!((first.category, first.code), (second.category, second.code));
}
