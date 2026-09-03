use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, ErrorCategory, IndexOrder, Keyword, Schema,
    TupleSpec, TxOp, TxValue, Unique, Value, ValueType, View,
};

const TX_INSTANT: u32 = 1;
const EMAIL: u32 = 10;
const NAME: u32 = 11;
const ALIAS: u32 = 12;
const MANAGES: u32 = 13;
const PART_A: u32 = 14;
const PART_B: u32 = 15;
const COMPOSITE: u32 = 16;

fn keyword(namespace: &str, name: &str) -> Keyword {
    Keyword::new(namespace, name)
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
        .install(attribute(
            TX_INSTANT,
            "db",
            "txInstant",
            ValueType::Instant,
            Cardinality::One,
        ))
        .unwrap();
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

    assert_eq!(before.basis_t(), 0);
    assert!(before.datoms(View::Current, IndexOrder::Eavt).is_empty());
    assert_eq!(report.db_after.basis_t(), 1);
    assert!(report.tx_data.iter().any(|datom| {
        datom.entity == 1 && datom.attribute == TX_INSTANT && datom.value == Value::Instant(1_000)
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
    assert_eq!(before.basis_t(), 0);
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
    let report = empty_db()
        .with(
            &[
                add(EntityRef::Id(42), NAME, Value::String("Ada".into())),
                TxOp::Ensure {
                    entity: EntityRef::Id(42),
                    required: vec![NAME],
                },
            ],
            1_000,
        )
        .unwrap();
    assert_eq!(report.db_after.basis_t(), 1);

    let error = empty_db()
        .with(
            &[TxOp::Ensure {
                entity: EntityRef::Id(42),
                required: vec![NAME],
            }],
            1_000,
        )
        .unwrap_err();
    assert_eq!(error.code, "transaction/entity-spec");
}

#[test]
fn retract_entity_recurses_through_components() {
    let initial = empty_db()
        .with(
            &[
                add(EntityRef::Id(1_000), NAME, Value::String("Parent".into())),
                TxOp::Add {
                    entity: EntityRef::Id(1_000),
                    attribute: MANAGES,
                    value: TxValue::Entity(EntityRef::Id(1_001)),
                },
                add(EntityRef::Id(1_001), NAME, Value::String("Child".into())),
            ],
            1_000,
        )
        .unwrap();
    let retracted = initial
        .db_after
        .with(&[TxOp::RetractEntity(EntityRef::Id(1_000))], 2_000)
        .unwrap();
    assert!(retracted.db_after.values(1_000, NAME).is_empty());
    assert!(retracted.db_after.values(1_001, NAME).is_empty());
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
    let as_of = second.db_after.datoms(View::AsOf(1), IndexOrder::Eavt);
    let since = second.db_after.datoms(View::Since(1), IndexOrder::Eavt);
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
