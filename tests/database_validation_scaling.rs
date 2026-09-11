//! Public-kernel checks for broad validation/replay optimizations.
//! Semantic authority: schema_data_reference.md notes BigDecimal scale and
//! NaN limitations; recovered common/equals-with-strict-scale distinguishes
//! top-level decimal storage equality from index/identity comparison.
use atomic_core::{
    Attribute, Cardinality, DB_ALTER_ATTRIBUTE, DB_PART_DB, Database, Datom, EntityRef,
    ErrorCategory, IndexOrder, Keyword, Schema, TxOp, USER_PARTITION, Unique, Value, ValueType,
    View, canonical_datom_bytes, make_eid, t_to_tx,
};
use bigdecimal::BigDecimal;
use std::collections::BTreeMap;
use std::str::FromStr;
use std::time::Instant;

const DEC: u32 = 1000;
const DOUBLE: u32 = 1001;
const ONE_DEC: u32 = 1002;
const UNIQUE_DEC: u32 = 1003;
const UNIQUE_DOUBLE: u32 = 1004;
const COUNTER: u32 = 1005;

fn entity(index: u64) -> u64 {
    make_eid(USER_PARTITION, index).unwrap()
}
fn decimal(value: &str) -> Value {
    Value::BigDec(BigDecimal::from_str(value).unwrap())
}
fn add(entity: u64, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(entity),
        attribute,
        value: value.into(),
    }
}
fn retract(entity: u64, attribute: u32, value: Value) -> TxOp {
    TxOp::Retract {
        entity: EntityRef::Id(entity),
        attribute,
        value: Some(value.into()),
    }
}
fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, value_type, cardinality, unique) in [
        (DEC, ValueType::BigDec, Cardinality::Many, None),
        (DOUBLE, ValueType::Double, Cardinality::Many, None),
        (ONE_DEC, ValueType::BigDec, Cardinality::One, None),
        (
            UNIQUE_DEC,
            ValueType::BigDec,
            Cardinality::One,
            Some(Unique::Value),
        ),
        (
            UNIQUE_DOUBLE,
            ValueType::Double,
            Cardinality::One,
            Some(Unique::Value),
        ),
        (COUNTER, ValueType::Long, Cardinality::One, None),
    ] {
        let mut attribute = Attribute::new(
            id,
            Keyword::new("validation", format!("value-{id}")),
            value_type,
            cardinality,
        );
        attribute.indexed = id != COUNTER;
        attribute.unique = unique;
        schema.install(attribute).unwrap();
    }
    schema
}

fn encoded(mut datoms: Vec<Datom>, order: IndexOrder, _history: bool) -> Vec<u8> {
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    // Frame exact canonical datoms in current native index order.
    let mut observation = Vec::new();
    for datom in datoms {
        let bytes = canonical_datom_bytes(&datom).unwrap();
        observation.extend_from_slice(&(bytes.len() as u64).to_le_bytes());
        observation.extend_from_slice(&bytes);
    }
    observation
}

fn observation(database: &Database) -> Vec<Vec<u8>> {
    [false, true]
        .into_iter()
        .flat_map(|history| {
            [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ]
            .map(|order| {
                encoded(
                    database.datoms(
                        if history {
                            View::History
                        } else {
                            View::Current
                        },
                        order,
                    ),
                    order,
                    history,
                )
            })
        })
        .collect()
}

fn assert_exact_replay(database: &Database) {
    database.validate_invariants().unwrap();
    assert_eq!(
        observation(database),
        observation(&database.rebuild_derived_caches().unwrap())
    );
    // Independent byte-keyed replay: NaN payloads/zero signs canonicalize,
    // decimal scale does not. This avoids Value::PartialEq hiding scale loss.
    let mut history = database.datoms(View::History, IndexOrder::Eavt);
    history.sort_by_key(|datom| datom.tx);
    let mut facts = BTreeMap::new();
    for datom in history {
        let value_key = encoded(
            vec![Datom {
                entity: 42,
                attribute: DEC,
                value: datom.value.clone(),
                tx: t_to_tx(1).unwrap(),
                added: true,
            }],
            IndexOrder::Eavt,
            false,
        );
        let key = (datom.entity, datom.attribute, value_key);
        if datom.added {
            if datom.attribute == DB_ALTER_ATTRIBUTE as u32 {
                facts.insert(key, datom);
            } else {
                facts.entry(key).or_insert(datom);
            }
        } else {
            facts.remove(&key);
        }
    }
    assert_eq!(
        encoded(facts.into_values().collect(), IndexOrder::Eavt, false),
        encoded(
            database.datoms(View::Current, IndexOrder::Eavt),
            IndexOrder::Eavt,
            false
        )
    );
}

#[test]
fn numeric_storage_retractions_readds_and_alter_events_replay_exactly() {
    let before = Database::new(schema()).unwrap();
    let subject = entity(42);
    let nan_a = f64::from_bits(0x7ff8_0000_0000_0001);
    let nan_b = f64::from_bits(0x7ff8_0000_0000_0042);
    let seed_ops = vec![
        add(subject, DEC, decimal("1.0")),
        add(subject, DEC, decimal("1.00")),
        add(subject, DOUBLE, Value::Double(-0.0)),
        add(subject, DOUBLE, Value::Double(nan_a)),
        add(subject, DOUBLE, Value::Double(f64::NEG_INFINITY)),
        add(subject, DOUBLE, Value::Double(f64::INFINITY)),
        add(subject, ONE_DEC, decimal("3.0")),
        add(subject, UNIQUE_DEC, decimal("7.0")),
    ];
    let seeded = before.with(&seed_ops, 1000).unwrap();
    let mut reverse = seed_ops;
    reverse.reverse();
    assert_eq!(
        observation(&seeded.db_after),
        observation(&before.with(&reverse, 1000).unwrap().db_after)
    );
    let old_observation = observation(&seeded.db_after);
    let changed = seeded
        .db_after
        .with(
            &[
                retract(subject, DEC, decimal("1.0")),
                retract(subject, DOUBLE, Value::Double(0.0)),
                retract(subject, DOUBLE, Value::Double(nan_b)),
                add(subject, ONE_DEC, decimal("3.00")),
                retract(subject, UNIQUE_DEC, decimal("7.0")),
                add(entity(43), UNIQUE_DEC, decimal("7.00")),
            ],
            2000,
        )
        .unwrap();
    assert_eq!(changed.db_after.values(subject, DEC).len(), 1);
    assert!(changed.db_after.values(subject, DEC)[0].stored_eq(&decimal("1.00")));
    assert_eq!(changed.db_after.values(subject, ONE_DEC).len(), 1);
    assert!(changed.db_after.values(subject, ONE_DEC)[0].stored_eq(&decimal("3.00")));
    assert!(changed.db_after.values(subject, UNIQUE_DEC).is_empty());
    let remaining_doubles = changed.db_after.values(subject, DOUBLE);
    assert_eq!(remaining_doubles.len(), 2);
    assert!(
        remaining_doubles
            .iter()
            .all(|value| { matches!(value, Value::Double(number) if number.is_infinite()) })
    );
    assert_eq!(
        changed
            .db_after
            .lookup(UNIQUE_DEC, &decimal("7.000"))
            .unwrap(),
        Some(entity(43))
    );
    let mut indexed = changed
        .db_after
        .schema()
        .attribute(COUNTER)
        .unwrap()
        .clone();
    indexed.indexed = true;
    let readded = changed
        .db_after
        .with(
            &[
                add(subject, DEC, decimal("1.0")),
                add(subject, DOUBLE, Value::Double(0.0)),
                add(subject, DOUBLE, Value::Double(nan_b)),
                TxOp::AlterAttribute(indexed.clone()),
            ],
            3000,
        )
        .unwrap();
    assert_eq!(readded.db_after.values(subject, DOUBLE).len(), 4);
    indexed.indexed = false;
    let altered = readded
        .db_after
        .with(&[TxOp::AlterAttribute(indexed)], 4000)
        .unwrap();
    let redundant = altered
        .db_after
        .with(
            &[
                add(subject, DEC, decimal("1.0")),
                add(subject, DOUBLE, Value::Double(nan_a)),
            ],
            5000,
        )
        .unwrap();
    assert!(
        !redundant
            .tx_data
            .iter()
            .any(|datom| datom.entity == subject)
    );
    let current = redundant.db_after.datoms(View::Current, IndexOrder::Eavt);
    let hook = current
        .iter()
        .find(|datom| {
            datom.entity == DB_PART_DB
                && datom.attribute == DB_ALTER_ATTRIBUTE as u32
                && datom.value == Value::Ref(u64::from(COUNTER))
        })
        .unwrap();
    assert_eq!(hook.tx, t_to_tx(altered.db_after.basis_t()).unwrap());
    let one_scale = current
        .iter()
        .find(|datom| {
            datom.entity == subject
                && datom.attribute == DEC
                && datom.value.stored_eq(&decimal("1.0"))
        })
        .unwrap();
    assert_eq!(one_scale.tx, t_to_tx(readded.db_after.basis_t()).unwrap());
    let other_scale = current
        .iter()
        .find(|datom| {
            datom.entity == subject
                && datom.attribute == DEC
                && datom.value.stored_eq(&decimal("1.00"))
        })
        .unwrap();
    assert_eq!(other_scale.tx, t_to_tx(seeded.db_after.basis_t()).unwrap());
    let hooks = redundant
        .db_after
        .datoms(View::History, IndexOrder::Eavt)
        .into_iter()
        .filter(|datom| {
            datom.entity == DB_PART_DB
                && datom.attribute == DB_ALTER_ATTRIBUTE as u32
                && datom.value == Value::Ref(u64::from(COUNTER))
        })
        .count();
    assert_eq!(hooks, 2);
    for database in [
        &before,
        &seeded.db_after,
        &changed.db_after,
        &readded.db_after,
        &altered.db_after,
        &redundant.db_after,
    ] {
        assert_exact_replay(database);
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let application_facts = |datoms: Vec<Datom>| {
                datoms
                    .into_iter()
                    .filter(|datom| datom.entity == subject || datom.entity == entity(43))
                    .collect()
            };
            assert_eq!(
                encoded(
                    application_facts(database.datoms(View::Current, order)),
                    order,
                    false
                ),
                encoded(
                    application_facts(
                        redundant
                            .db_after
                            .datoms(View::AsOf(database.basis_t()), order)
                    ),
                    order,
                    false
                )
            );
        }
    }
    assert_eq!(old_observation, observation(&seeded.db_after));
}

#[test]
fn optimized_validation_preserves_cardinality_uniqueness_and_nan_rejections() {
    let before = Database::new(schema()).unwrap();
    for (ops, category, code) in [
        (
            vec![
                add(entity(42), ONE_DEC, decimal("4.0")),
                add(entity(42), ONE_DEC, decimal("4.00")),
            ],
            ErrorCategory::Conflict,
            "transaction/cardinality-one-conflict",
        ),
        (
            vec![
                add(entity(42), UNIQUE_DEC, decimal("8.0")),
                add(entity(43), UNIQUE_DEC, decimal("8.00")),
            ],
            ErrorCategory::Conflict,
            "transaction/unique-conflict",
        ),
        (
            vec![
                add(entity(42), UNIQUE_DOUBLE, Value::Double(-0.0)),
                add(entity(43), UNIQUE_DOUBLE, Value::Double(0.0)),
            ],
            ErrorCategory::Conflict,
            "transaction/unique-conflict",
        ),
        (
            vec![add(entity(42), UNIQUE_DOUBLE, Value::Double(f64::NAN))],
            ErrorCategory::Incorrect,
            "transaction/nan-cannot-identify",
        ),
    ] {
        let original = observation(&before);
        for reverse in [false, true] {
            let mut ops = ops.clone();
            if reverse {
                ops.reverse();
            }
            let error = before.with(&ops, 1000).unwrap_err();
            assert_eq!((error.category, error.code), (category, code));
            assert_eq!(observation(&before), original);
        }
    }
}

#[test]
fn validation_scaling_reports_1000_and_4000_facts_without_a_machine_specific_sla() {
    const IDENTIFIER: u32 = 1000;
    let mut measurements = Vec::new();
    for count in [1000_usize, 4000] {
        let mut schema = Schema::new();
        let mut identifier = Attribute::new(
            IDENTIFIER,
            Keyword::new("scaling", "id"),
            ValueType::Long,
            Cardinality::One,
        );
        identifier.unique = Some(Unique::Value);
        schema.install(identifier).unwrap();
        let before = Database::new(schema).unwrap();
        let ops = (0..count)
            .map(|index| TxOp::Add {
                entity: EntityRef::Temp(format!("entity-{index:04}")),
                attribute: IDENTIFIER,
                value: Value::Long(index as i64).into(),
            })
            .collect::<Vec<_>>();
        let build_start = Instant::now();
        let report = before.with(&ops, 1000).unwrap();
        let build_us = build_start.elapsed().as_micros();
        assert_eq!(
            report
                .db_after
                .datoms(View::Current, IndexOrder::Eavt)
                .iter()
                .filter(|datom| datom.attribute == IDENTIFIER)
                .count(),
            count
        );
        let mut validation_us = Vec::new();
        for _ in 0..3 {
            let start = Instant::now();
            std::hint::black_box(&report.db_after)
                .validate_invariants()
                .unwrap();
            validation_us.push(start.elapsed().as_micros());
        }
        validation_us.sort_unstable();
        eprintln!(
            "database_validation_scaling facts={count} history_datoms={} build_us={build_us} validation_us={validation_us:?}",
            report
                .db_after
                .datoms(View::History, IndexOrder::Eavt)
                .len()
        );
        measurements.push(validation_us[1]);
    }
    eprintln!(
        "database_validation_scaling median_ratio_4000_over_1000={:.3}; observational_only=true",
        measurements[1] as f64 / measurements[0].max(1) as f64
    );
}
