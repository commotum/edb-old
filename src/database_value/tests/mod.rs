use super::*;
use crate::{
    Attribute, AttributeName, Cardinality, Clause, DB_IDENT, DataPattern, Database, Datom,
    EntityIdentifier, EntityRef, ErrorCategory, FindElement, FindSpec, IndexBoundary,
    IndexComponents, IndexOrder, IndexPrefix, IndexTransaction, Keyword, Query, QueryControl,
    Schema, Term, TupleSpec, TxOp, TxReport, TxValue, USER_PARTITION, Unique, Value, ValueType,
    Variable, make_eid, t_to_tx, tx_to_t,
};
use bigdecimal::BigDecimal;
use std::str::FromStr;

const AMOUNT: u32 = 1_000;
const LINK: u32 = 1_001;
const BOUNDARY_EMAIL: u32 = 1_000;
const BOUNDARY_TEXT: u32 = 1_001;
const BOUNDARY_LINK: u32 = 1_002;
const BOUNDARY_HOMOGENEOUS_REFS: u32 = 1_003;
const BOUNDARY_HETEROGENEOUS: u32 = 1_004;
use super::read_context::{LogicalReadObserver, PrefixMemoKey, TransactionReadContext};
use super::value::ReadBasis;
use crate::index::compare_prefix;
use crate::index::overlay::OverlayIndexes;
use std::cmp::Ordering;
use std::sync::Arc;

fn decimal(value: &str) -> Value {
    Value::BigDec(BigDecimal::from_str(value).unwrap())
}

fn overlay_for(report: &TxReport, tx_instant: i64) -> DatabaseValue {
    DatabaseValue::transaction_overlay(
        report.db_before.database_value(),
        Arc::from(report.tx_data.clone()),
        Arc::new(report.db_after.schema().clone()),
        report.db_after.basis_t(),
        report.db_after.eidx_frontier(),
        tx_instant,
    )
    .unwrap()
}

fn indexed(mut attribute: Attribute) -> Attribute {
    attribute.indexed = true;
    attribute
}

fn raw_boundary_fixture() -> (DatabaseValue, u64, u64, Keyword, Keyword) {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                BOUNDARY_EMAIL,
                Keyword::new("boundary", "email"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            BOUNDARY_TEXT,
            Keyword::new("boundary", "text"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(indexed(Attribute::new(
            BOUNDARY_LINK,
            Keyword::new("boundary", "link"),
            ValueType::Ref,
            Cardinality::Many,
        )))
        .unwrap();
    schema
        .install(indexed(
            Attribute::new(
                BOUNDARY_HOMOGENEOUS_REFS,
                Keyword::new("boundary", "homogeneous-refs"),
                ValueType::Tuple,
                Cardinality::Many,
            )
            .tuple(TupleSpec::Homogeneous(ValueType::Ref)),
        ))
        .unwrap();
    schema
        .install(indexed(
            Attribute::new(
                BOUNDARY_HETEROGENEOUS,
                Keyword::new("boundary", "heterogeneous"),
                ValueType::Tuple,
                Cardinality::Many,
            )
            .tuple(TupleSpec::Heterogeneous(vec![
                ValueType::Ref,
                ValueType::String,
                ValueType::Ref,
            ])),
        ))
        .unwrap();

    let alice_ident = Keyword::new("boundary", "alice");
    let bob_ident = Keyword::new("boundary", "bob");
    let report = Database::new(schema)
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("alice".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(alice_ident.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("alice".into()),
                    attribute: BOUNDARY_EMAIL,
                    value: Value::String("alice@example.test".into()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("bob".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(bob_ident.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("bob".into()),
                    attribute: BOUNDARY_EMAIL,
                    value: Value::String("bob@example.test".into()).into(),
                },
            ],
            1_000,
        )
        .unwrap();
    (
        report.db_after.database_value(),
        report.tempids["alice"],
        report.tempids["bob"],
        alice_ident,
        bob_ident,
    )
}

fn assert_same_stored_datoms(actual: &[Datom], expected: &[Datom]) {
    assert_eq!(actual.len(), expected.len(), "different datom counts");
    for (actual, expected) in actual.iter().zip(expected) {
        assert_eq!(actual.entity, expected.entity);
        assert_eq!(actual.attribute, expected.attribute);
        assert_eq!(actual.tx, expected.tx);
        assert_eq!(actual.added, expected.added);
        assert!(
            actual.value.stored_eq(&expected.value),
            "different stored values: {:?} and {:?}",
            actual.value,
            expected.value
        );
    }
}

fn expected_seek(database: &DatabaseValue, boundary: &IndexBoundary, reverse: bool) -> Vec<Datom> {
    let normalized = boundary.normalized().unwrap();
    let mut datoms = database
        .datoms(boundary.order())
        .unwrap()
        .into_iter()
        .filter(|datom| {
            let comparison = normalized.compare_datom(datom);
            if reverse {
                !comparison.is_gt()
            } else {
                !comparison.is_lt()
            }
        })
        .collect::<Vec<_>>();
    if reverse {
        datoms.reverse();
    }
    datoms
}

fn assert_bidirectional_seek(database: &DatabaseValue, boundary: &IndexBoundary) {
    let forward = database
        .seek_cursor(boundary)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    let reverse = database
        .reverse_seek_cursor(boundary)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_same_stored_datoms(&forward, &expected_seek(database, boundary, false));
    assert_same_stored_datoms(&reverse, &expected_seek(database, boundary, true));
    assert_same_stored_datoms(&database.collect_seek_datoms(boundary).unwrap(), &forward);
    assert_same_stored_datoms(
        &database.collect_reverse_seek_datoms(boundary).unwrap(),
        &reverse,
    );
}

fn assert_prefix_matches_eager(
    overlay: &DatabaseValue,
    eager: &DatabaseValue,
    prefix: &IndexPrefix,
) {
    let overlay_current = overlay.datoms_with_prefix(prefix).unwrap();
    let eager_current = eager.datoms_with_prefix(prefix).unwrap();
    assert_same_stored_datoms(&overlay_current, &eager_current);

    let cursor_current = overlay
        .current_prefix_cursor(prefix)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap();
    assert_same_stored_datoms(&cursor_current, &eager_current);

    let overlay_history = overlay
        .clone()
        .history()
        .datoms_with_prefix(prefix)
        .unwrap();
    let eager_history = eager.clone().history().datoms_with_prefix(prefix).unwrap();
    assert_same_stored_datoms(&overlay_history, &eager_history);
}

fn overlay_source_prefix(overlay: &DatabaseValue, prefix: &IndexPrefix) -> IndexPrefix {
    match &overlay.basis {
        ReadBasis::TransactionOverlay(overlay) => overlay.source_prefix(prefix),
        _ => panic!("expected a transaction overlay"),
    }
}

fn overlay_fixture() -> (TxReport, DatabaseValue, u64, u64, Keyword, Keyword, Keyword) {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            AMOUNT,
            Keyword::new("measurement", "amount"),
            ValueType::BigDec,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            LINK,
            Keyword::new("measurement", "link"),
            ValueType::Ref,
            Cardinality::Many,
        ))
        .unwrap();

    let old = Keyword::new("overlay", "old");
    let new = Keyword::new("overlay", "new");
    let spare = Keyword::new("overlay", "spare");
    let database = Database::new(schema).unwrap();
    let seeded = database
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("first".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(old.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("first".into()),
                    attribute: AMOUNT,
                    value: decimal("1.0").into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("first".into()),
                    attribute: AMOUNT,
                    value: decimal("1.00").into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("first".into()),
                    attribute: LINK,
                    value: TxValue::Entity(EntityRef::Temp("second".into())),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("second".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(spare.clone()).into(),
                },
            ],
            2_000,
        )
        .unwrap();
    let first = seeded.tempids["first"];
    let second = seeded.tempids["second"];
    let renamed = seeded
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(first),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(new.clone()).into(),
            }],
            3_000,
        )
        .unwrap();

    let mut indexed = renamed.db_after.schema().attribute(AMOUNT).unwrap().clone();
    indexed.indexed = true;
    let report = renamed
        .db_after
        .with(
            &[
                TxOp::AlterAttribute(indexed),
                TxOp::Retract {
                    entity: EntityRef::Id(first),
                    attribute: AMOUNT,
                    value: Some(decimal("1.0").into()),
                },
                TxOp::Add {
                    entity: EntityRef::Id(second),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(old.clone()).into(),
                },
            ],
            4_000,
        )
        .unwrap();
    let overlay = overlay_for(&report, 4_000);
    (report, overlay, first, second, old, new, spare)
}

mod boundaries;
mod overlay;
mod read_context;
mod views;
