use atomic_core::{
    Attribute, AttributeRef, Cardinality, Database, EntityMap, EntityRef, ErrorCategory,
    IndexOrder, IndexPrefix, Keyword, MapValue, Schema, SchemaChange, TxCall, TxForm, TxFunctions,
    TxOp, TxValue, Unique, Value, ValueType, View,
};

const TX_INSTANT: u32 = 1;
const EMAIL: u32 = 10;
const NAME: u32 = 11;
const ALIAS: u32 = 12;
const LINK: u32 = 13;
const BALANCE: u32 = 14;

fn attr(
    id: u32,
    namespace: &str,
    name: &str,
    value_type: ValueType,
    cardinality: Cardinality,
) -> Attribute {
    Attribute::new(id, Keyword::new(namespace, name), value_type, cardinality)
}

fn database() -> Database {
    let mut schema = Schema::new();
    schema
        .install(attr(
            TX_INSTANT,
            "db",
            "txInstant",
            ValueType::Instant,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            attr(
                EMAIL,
                "person",
                "email",
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    let mut name = attr(NAME, "person", "name", ValueType::String, Cardinality::One);
    name.indexed = true;
    schema.install(name).unwrap();
    schema
        .install(attr(
            ALIAS,
            "person",
            "alias",
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(attr(
            LINK,
            "person",
            "link",
            ValueType::Ref,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(attr(
            BALANCE,
            "account",
            "balance",
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    Database::new(schema).unwrap()
}

fn add(entity: EntityRef, attribute: u32, value: TxValue) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value,
    }
}

fn scalar(value: Value) -> TxValue {
    TxValue::Scalar(value)
}

#[test]
fn immutable_indexes_have_documented_membership_and_prefix_access() {
    let before = database();
    let report = before
        .with(
            &[
                add(
                    EntityRef::Id(100),
                    EMAIL,
                    scalar(Value::String("a@example.test".into())),
                ),
                add(
                    EntityRef::Id(100),
                    NAME,
                    scalar(Value::String("Ada".into())),
                ),
                add(EntityRef::Id(100), ALIAS, scalar(Value::String("A".into()))),
                add(
                    EntityRef::Id(100),
                    LINK,
                    TxValue::Entity(EntityRef::Id(200)),
                ),
            ],
            1_000,
        )
        .unwrap();

    let db = &report.db_after;
    assert_eq!(db.datoms(View::Current, IndexOrder::Eavt).len(), 5);
    assert_eq!(db.datoms(View::Current, IndexOrder::Aevt).len(), 5);
    let avet = db.datoms(View::Current, IndexOrder::Avet);
    assert_eq!(avet.len(), 2);
    assert!(
        avet.iter()
            .all(|d| d.attribute == EMAIL || d.attribute == NAME)
    );
    let vaet = db.datoms(View::Current, IndexOrder::Vaet);
    assert_eq!(vaet.len(), 1);
    assert_eq!(vaet[0].value, Value::Ref(200));

    let person = db
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity: 100,
            attribute: None,
            value: None,
        })
        .unwrap();
    assert_eq!(person.len(), 4);
    assert_eq!(
        db.datoms_with_prefix(&IndexPrefix::Avet {
            attribute: EMAIL,
            value: Some(Value::String("a@example.test".into())),
            entity: None,
        })
        .unwrap()[0]
            .entity,
        100
    );
    assert_eq!(
        db.avet_range(
            NAME,
            Some(&Value::String("A".into())),
            Some(&Value::String("B".into()))
        )
        .unwrap()
        .len(),
        1
    );
    assert_eq!(
        db.avet_range(ALIAS, None, None).unwrap_err().code,
        "index/attribute-not-in-avet"
    );

    // Producing a successor cannot alter any root observed from db-before.
    assert!(before.datoms(View::Current, IndexOrder::Eavt).is_empty());
}

#[test]
fn history_roots_keep_assertions_and_retractions_while_old_snapshots_stay_valid() {
    let first = database()
        .with(
            &[add(
                EntityRef::Id(100),
                NAME,
                scalar(Value::String("Ada".into())),
            )],
            1_000,
        )
        .unwrap();
    let second = first
        .db_after
        .with(
            &[add(
                EntityRef::Id(100),
                NAME,
                scalar(Value::String("Augusta".into())),
            )],
            2_000,
        )
        .unwrap();

    assert_eq!(
        first.db_after.values(100, NAME),
        vec![&Value::String("Ada".into())]
    );
    assert_eq!(
        second.db_after.values(100, NAME),
        vec![&Value::String("Augusta".into())]
    );
    let name_history = second
        .db_after
        .history_with_prefix(&IndexPrefix::Eavt {
            entity: 100,
            attribute: Some(NAME),
            value: None,
        })
        .unwrap();
    assert_eq!(name_history.len(), 3);
    assert_eq!(name_history.iter().filter(|d| !d.added).count(), 1);
}

#[test]
fn schema_changes_are_atomic_synchronous_and_use_db_before_for_domain_data() {
    let db = database();
    let age = attr(20, "person", "age", ValueType::Long, Cardinality::One);
    let failure = db
        .with(
            &[
                TxOp::InstallAttribute(age.clone()),
                add(EntityRef::Id(100), 20, scalar(Value::Long(36))),
            ],
            1_000,
        )
        .unwrap_err();
    assert_eq!(failure.code, "schema/unknown-attribute");
    assert_eq!(db.basis_t(), 0);

    let installed = db
        .with(&[TxOp::InstallAttribute(age.clone())], 1_000)
        .unwrap();
    assert_eq!(
        installed.schema_changes,
        vec![SchemaChange::Install(age.clone())]
    );
    assert_eq!(
        installed
            .db_after
            .schema_changes()
            .map(|(tx, change)| (tx, change.clone()))
            .collect::<Vec<_>>(),
        vec![(1, SchemaChange::Install(age.clone()))]
    );
    assert_eq!(
        installed
            .db_after
            .schema()
            .resolve_ident(&Keyword::new("person", "age")),
        Some(20)
    );
    installed
        .db_after
        .with(
            &[add(EntityRef::Id(100), 20, scalar(Value::Long(36)))],
            2_000,
        )
        .unwrap();

    let mut renamed = age;
    renamed.ident = Keyword::new("person", "years");
    let altered = installed
        .db_after
        .with(&[TxOp::AlterAttribute(renamed)], 2_000)
        .unwrap();
    assert_eq!(
        altered
            .db_after
            .schema()
            .resolve_ident(&Keyword::new("person", "age")),
        Some(20)
    );
    assert_eq!(
        altered
            .db_after
            .schema()
            .resolve_ident(&Keyword::new("person", "years")),
        Some(20)
    );
}

#[test]
fn map_forms_normalize_to_primitive_forms_including_many_nested_and_reverse() {
    let map = TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Id(100)),
        attributes: vec![
            (
                AttributeRef::Ident(Keyword::new("person", "name")),
                MapValue::Value(scalar(Value::String("Ada".into()))),
            ),
            (
                AttributeRef::Id(ALIAS),
                MapValue::Many(vec![
                    MapValue::Value(scalar(Value::String("A".into()))),
                    MapValue::Value(scalar(Value::String("Countess".into()))),
                ]),
            ),
            (
                AttributeRef::ReverseId(LINK),
                MapValue::Value(TxValue::Entity(EntityRef::Id(200))),
            ),
        ],
    });
    let mapped = database()
        .with_forms(&[map], &TxFunctions::new(), 1_000)
        .unwrap();
    let primitive = database()
        .with(
            &[
                add(
                    EntityRef::Id(100),
                    NAME,
                    scalar(Value::String("Ada".into())),
                ),
                add(EntityRef::Id(100), ALIAS, scalar(Value::String("A".into()))),
                add(
                    EntityRef::Id(100),
                    ALIAS,
                    scalar(Value::String("Countess".into())),
                ),
                add(
                    EntityRef::Id(200),
                    LINK,
                    TxValue::Entity(EntityRef::Id(100)),
                ),
            ],
            1_000,
        )
        .unwrap();
    assert_eq!(mapped.tx_data, primitive.tx_data);
}

#[test]
fn nested_maps_enforce_ownership_and_functions_all_observe_db_before() {
    let orphan = TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Id(100)),
        attributes: vec![(
            AttributeRef::Id(LINK),
            MapValue::Nested(Box::new(EntityMap {
                id: None,
                attributes: vec![(
                    AttributeRef::Id(NAME),
                    MapValue::Value(scalar(Value::String("child".into()))),
                )],
            })),
        )],
    });
    assert_eq!(
        database()
            .with_forms(&[orphan], &TxFunctions::new(), 1_000)
            .unwrap_err()
            .code,
        "transaction/orphan-nested-map"
    );

    let seeded = database()
        .with(
            &[add(EntityRef::Id(7), BALANCE, scalar(Value::Long(10)))],
            1_000,
        )
        .unwrap();
    let mut functions = TxFunctions::new();
    functions.register("increment", |db, args| {
        let [TxValue::Entity(entity)] = args else {
            return Err(atomic_core::SemanticError::incorrect(
                "test/args",
                "expected entity",
            ));
        };
        let EntityRef::Id(entity) = entity else {
            return Err(atomic_core::SemanticError::incorrect(
                "test/args",
                "expected id",
            ));
        };
        let Some(Value::Long(value)) = db.values(*entity, BALANCE).first().copied() else {
            return Err(atomic_core::SemanticError::incorrect(
                "test/missing",
                "missing balance",
            ));
        };
        Ok(vec![TxForm::Op(add(
            EntityRef::Id(*entity),
            BALANCE,
            scalar(Value::Long(value + 1)),
        ))])
    });
    let call = TxForm::Call(TxCall {
        function: "increment".into(),
        arguments: vec![TxValue::Entity(EntityRef::Id(7))],
    });
    let result = seeded
        .db_after
        .with_forms(&[call.clone(), call], &functions, 2_000)
        .unwrap();
    assert_eq!(result.db_after.values(7, BALANCE), vec![&Value::Long(11)]);
}

#[test]
fn retract_entity_removes_incoming_references_via_vaet() {
    let seeded = database()
        .with(
            &[
                add(
                    EntityRef::Id(100),
                    NAME,
                    scalar(Value::String("target".into())),
                ),
                add(
                    EntityRef::Id(200),
                    LINK,
                    TxValue::Entity(EntityRef::Id(100)),
                ),
            ],
            1_000,
        )
        .unwrap();
    let retracted = seeded
        .db_after
        .with(&[TxOp::RetractEntity(EntityRef::Id(100))], 2_000)
        .unwrap();
    assert!(retracted.db_after.values(100, NAME).is_empty());
    assert!(retracted.db_after.values(200, LINK).is_empty());
}

#[test]
fn malformed_map_input_has_stable_errors() {
    let form = TxForm::EntityMap(EntityMap {
        id: Some(EntityRef::Id(100)),
        attributes: vec![(
            AttributeRef::Id(NAME),
            MapValue::Many(vec![MapValue::Value(scalar(Value::String("Ada".into())))]),
        )],
    });
    let error = database()
        .with_forms(&[form], &TxFunctions::new(), 1_000)
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Incorrect);
    assert_eq!(error.code, "transaction/collection-on-cardinality-one");
}

#[test]
fn custom_and_time_filters_compose_before_retraction_collapse() {
    let first = database()
        .with(
            &[add(
                EntityRef::Id(100),
                NAME,
                scalar(Value::String("Ada".into())),
            )],
            1_000,
        )
        .unwrap();
    let bad = first
        .db_after
        .with(
            &[add(
                EntityRef::Id(100),
                NAME,
                scalar(Value::String("wrong".into())),
            )],
            2_000,
        )
        .unwrap();
    let fixed = bad
        .db_after
        .with(
            &[add(
                EntityRef::Id(100),
                NAME,
                scalar(Value::String("Ada".into())),
            )],
            3_000,
        )
        .unwrap();

    let corrected = fixed
        .db_after
        .view(View::Current)
        .filter(|_, datom| datom.tx != 2)
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity: 100,
            attribute: Some(NAME),
            value: None,
        })
        .unwrap();
    assert_eq!(corrected.len(), 1);
    assert_eq!(corrected[0].value, Value::String("Ada".into()));

    let interval = fixed
        .db_after
        .view(View::Current)
        .since(1)
        .as_of(2)
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity: 100,
            attribute: Some(NAME),
            value: None,
        })
        .unwrap();
    assert_eq!(interval.len(), 1);
    assert_eq!(interval[0].value, Value::String("wrong".into()));

    let raw = fixed
        .db_after
        .view(View::Current)
        .as_of(2)
        .history()
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity: 100,
            attribute: Some(NAME),
            value: None,
        })
        .unwrap();
    assert_eq!(raw.len(), 3);
    assert_eq!(raw.iter().filter(|datom| !datom.added).count(), 1);
}

#[test]
fn idents_resolve_as_entities_against_db_before() {
    let report = database()
        .with(
            &[add(
                EntityRef::Ident(Keyword::new("person", "email")),
                NAME,
                scalar(Value::String("attribute entity".into())),
            )],
            1_000,
        )
        .unwrap();
    assert_eq!(
        report.db_after.values(u64::from(EMAIL), NAME),
        vec![&Value::String("attribute entity".into())]
    );
}

#[test]
fn attribute_predicates_start_on_the_transaction_after_installation() {
    let db = database();
    let mut balance = db.schema().attribute(BALANCE).unwrap().clone();
    balance.predicates.push("non-negative".into());
    let installed = db
        .with(
            &[
                TxOp::AlterAttribute(balance),
                add(EntityRef::Id(7), BALANCE, scalar(Value::Long(-1))),
            ],
            1_000,
        )
        .unwrap();
    assert_eq!(
        installed.db_after.values(7, BALANCE),
        vec![&Value::Long(-1)]
    );

    let mut functions = TxFunctions::new();
    functions.register_attribute_predicate("non-negative", |value| {
        Ok(matches!(value, Value::Long(value) if *value >= 0))
    });
    let rejected = installed.db_after.with_forms(
        &[TxForm::Op(add(
            EntityRef::Id(7),
            BALANCE,
            scalar(Value::Long(-2)),
        ))],
        &functions,
        2_000,
    );
    assert_eq!(
        rejected.unwrap_err().code,
        "transaction/attribute-predicate"
    );
    assert_eq!(installed.db_after.basis_t(), 1);

    let accepted = installed
        .db_after
        .with_forms(
            &[TxForm::Op(add(
                EntityRef::Id(7),
                BALANCE,
                scalar(Value::Long(0)),
            ))],
            &functions,
            2_000,
        )
        .unwrap();
    assert_eq!(accepted.db_after.values(7, BALANCE), vec![&Value::Long(0)]);
}

#[test]
fn component_nested_maps_create_owned_entities() {
    let db = database();
    let mut link = db.schema().attribute(LINK).unwrap().clone();
    link.component = true;
    let altered = db.with(&[TxOp::AlterAttribute(link)], 1_000).unwrap();
    let report = altered
        .db_after
        .with_forms(
            &[TxForm::EntityMap(EntityMap {
                id: Some(EntityRef::Id(100)),
                attributes: vec![(
                    AttributeRef::Id(LINK),
                    MapValue::Nested(Box::new(EntityMap {
                        id: None,
                        attributes: vec![(
                            AttributeRef::Id(NAME),
                            MapValue::Value(scalar(Value::String("child".into()))),
                        )],
                    })),
                )],
            })],
            &TxFunctions::new(),
            2_000,
        )
        .unwrap();
    let [Value::Ref(child)] = report.db_after.values(100, LINK).as_slice() else {
        panic!("expected one child reference")
    };
    assert_eq!(
        report.db_after.values(*child, NAME),
        vec![&Value::String("child".into())]
    );
}

#[test]
fn transaction_permutations_and_state_sequences_preserve_kernel_invariants() {
    let mut forward = database();
    let mut reverse = database();
    let mut snapshots = Vec::new();

    for step in 1_u64..=64 {
        let entity = 100 + (step % 7);
        let mut ops = vec![
            add(
                EntityRef::Id(entity),
                NAME,
                scalar(Value::String(format!("name-{step}"))),
            ),
            add(
                EntityRef::Id(entity),
                ALIAS,
                scalar(Value::String(format!("alias-{}", step % 5))),
            ),
            TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: ALIAS,
                value: Some(scalar(Value::String(format!("alias-{}", (step + 2) % 5)))),
            },
        ];
        let report_forward = forward.with(&ops, step as i64 * 1_000).unwrap();
        ops.reverse();
        let report_reverse = reverse.with(&ops, step as i64 * 1_000).unwrap();

        assert_eq!(report_forward.tx_data, report_reverse.tx_data);
        assert_eq!(report_forward.tempids, report_reverse.tempids);
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(
                report_forward.db_after.datoms(View::Current, order),
                report_reverse.db_after.datoms(View::Current, order)
            );
            assert_eq!(
                report_forward.db_after.datoms(View::History, order),
                report_reverse.db_after.datoms(View::History, order)
            );
        }
        report_forward.db_after.validate_invariants().unwrap();
        report_reverse.db_after.validate_invariants().unwrap();
        if step % 8 == 0 {
            snapshots.push((
                report_forward.db_after.clone(),
                report_forward
                    .db_after
                    .datoms(View::Current, IndexOrder::Eavt),
            ));
        }
        forward = report_forward.db_after;
        reverse = report_reverse.db_after;
    }

    for (snapshot, expected) in snapshots {
        assert_eq!(snapshot.datoms(View::Current, IndexOrder::Eavt), expected);
        snapshot.validate_invariants().unwrap();
    }
}

#[test]
fn transaction_instants_resolve_to_stable_time_boundaries() {
    let first = database().with(&[], 1_000).unwrap();
    let second = first.db_after.with(&[], 2_000).unwrap();
    let third = second.db_after.with(&[], 2_000).unwrap();
    let db = third.db_after;

    assert_eq!(db.t_at_or_before_instant(999), 0);
    assert_eq!(db.t_at_or_before_instant(1_500), 1);
    assert_eq!(db.t_at_or_before_instant(2_000), 3);
    assert_eq!(db.t_at_or_after_instant(1_500), 2);
    assert_eq!(db.t_at_or_after_instant(2_001), 4);
    assert_eq!(
        db.as_of_instant(1_500)
            .datoms(IndexOrder::Eavt)
            .iter()
            .filter(|datom| datom.attribute == TX_INSTANT)
            .count(),
        1
    );
}
