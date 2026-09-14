use atomic_core::{
    Attribute, AttributeName, Cardinality, Database, DatabaseValue, EntityRef, IndexBoundary,
    IndexComponents, IndexPullOptions, IndexTransaction, Keyword, PullAttribute, PullControl,
    PullPattern, QueryValue, Schema, TxOp, TxValue, Value, ValueType,
};
use std::sync::{
    Arc,
    atomic::{AtomicBool, AtomicUsize, Ordering},
};
use std::time::Duration;

mod common;

const NAME: u32 = 1_000;
const SCORE: u32 = 1_001;
const LINKS: u32 = 1_002;
const TAG: u32 = 1_003;
const UNINDEXED: u32 = 1_004;
const EMPTY: u32 = 1_005;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, name, kind, cardinality, indexed) in [
        (NAME, "name", ValueType::String, Cardinality::One, false),
        (SCORE, "score", ValueType::Long, Cardinality::One, true),
        (LINKS, "links", ValueType::Ref, Cardinality::Many, true),
        (TAG, "tag", ValueType::String, Cardinality::Many, true),
        (
            UNINDEXED,
            "unindexed",
            ValueType::Long,
            Cardinality::One,
            false,
        ),
        (EMPTY, "empty", ValueType::Long, Cardinality::One, true),
    ] {
        let mut attribute = Attribute::new(id, Keyword::new("item", name), kind, cardinality);
        attribute.indexed = indexed;
        schema.install(attribute).unwrap();
    }
    schema
}

fn add(entity: EntityRef, attribute: u32, value: impl Into<TxValue>) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn entity(index: usize) -> EntityRef {
    EntityRef::Temp(format!("item-{index:04}"))
}

fn seed(count: usize) -> Vec<TxOp> {
    let mut operations = Vec::new();
    for index in 0..count {
        operations.push(add(
            entity(index),
            NAME,
            Value::String(format!("item-{index:04}")),
        ));
        operations.push(add(entity(index), SCORE, Value::Long(index as i64)));
    }
    operations.extend([
        add(entity(0), LINKS, TxValue::Entity(entity(3))),
        add(entity(0), LINKS, TxValue::Entity(entity(4))),
        add(entity(1), LINKS, TxValue::Entity(entity(3))),
        add(entity(2), LINKS, TxValue::Entity(entity(5))),
        add(entity(0), TAG, Value::String("blue".into())),
        add(entity(0), TAG, Value::String("red".into())),
        add(entity(1), TAG, Value::String("red".into())),
    ]);
    operations
}

fn database() -> atomic_core::TxReport {
    Database::new(schema())
        .unwrap()
        .with(&seed(10), 10)
        .unwrap()
}

fn selector() -> PullPattern {
    PullPattern::attributes(vec![
        PullAttribute::forward(AttributeName::Id(NAME)),
        PullAttribute::forward(AttributeName::Id(SCORE)),
    ])
}

fn options(start: IndexBoundary) -> IndexPullOptions {
    IndexPullOptions::new(start, selector())
}

fn score_start(value: Option<i64>) -> IndexBoundary {
    IndexBoundary::Avet(match value {
        None => IndexComponents::One(SCORE),
        Some(value) => IndexComponents::Two(SCORE, Value::Long(value)),
    })
}

fn name(value: &QueryValue) -> String {
    let QueryValue::Map(fields) = value else {
        panic!("expected pull map")
    };
    for (key, value) in fields {
        if *key == QueryValue::Scalar(Value::Keyword(Keyword::new("item", "name"))) {
            let QueryValue::Scalar(Value::String(name)) = value else {
                panic!("expected name")
            };
            return name.clone();
        }
    }
    panic!("missing name: {value:?}")
}

fn names(database: &DatabaseValue, options: IndexPullOptions) -> Vec<String> {
    database
        .index_pull(options)
        .unwrap()
        .map(|value| name(&value.unwrap()))
        .collect()
}

#[test]
fn avet_walks_one_attribute_in_both_directions_with_directional_offset_limit() {
    let seeded = database();
    let value = seeded.db_after.database_value();
    let normalized = value
        .avet_boundary(IndexComponents::One(AttributeName::Ident(Keyword::new(
            "item", "score",
        ))))
        .unwrap();
    assert_eq!(
        names(&value, options(normalized)),
        (0..10)
            .map(|index| format!("item-{index:04}"))
            .collect::<Vec<_>>()
    );
    let mut forward = options(score_start(Some(3)));
    forward.offset = 2;
    forward.limit = Some(3);
    assert_eq!(
        names(&value, forward),
        ["item-0005", "item-0006", "item-0007"]
    );
    let mut reverse = options(score_start(Some(7)));
    reverse.reverse = true;
    reverse.offset = 2;
    reverse.limit = Some(3);
    assert_eq!(
        names(&value, reverse),
        ["item-0005", "item-0004", "item-0003"]
    );
    let mut reverse_all = options(score_start(None));
    reverse_all.reverse = true;
    assert_eq!(
        names(&value, reverse_all),
        (0..10)
            .rev()
            .map(|index| format!("item-{index:04}"))
            .collect::<Vec<_>>()
    );
    assert!(names(&value, options(score_start(Some(99)))).is_empty());
    let full = IndexBoundary::Avet(IndexComponents::Four(
        SCORE,
        Value::Long(3),
        seeded.tempids["item-0003"],
        IndexTransaction::T(seeded.db_after.basis_t()),
    ));
    assert_eq!(names(&value, options(full.clone()))[0], "item-0003");
    let mut reverse_full = options(full);
    reverse_full.reverse = true;
    assert_eq!(
        names(&value, reverse_full),
        ["item-0003", "item-0002", "item-0001", "item-0000"]
    );
    let mut zero = options(score_start(None));
    zero.limit = Some(0);
    assert!(names(&value, zero).is_empty());
}

#[test]
fn aevt_pulls_reference_values_and_preserves_documented_duplicates() {
    let seeded = database();
    let value = seeded.db_after.database_value();
    assert_eq!(
        names(
            &value,
            options(IndexBoundary::Aevt(IndexComponents::One(LINKS)))
        ),
        ["item-0003", "item-0004", "item-0003", "item-0005"]
    );
    let after_first = IndexBoundary::Aevt(IndexComponents::Two(LINKS, seeded.tempids["item-0001"]));
    assert_eq!(
        names(&value, options(after_first)),
        ["item-0003", "item-0005"]
    );
    let mut reverse = options(IndexBoundary::Aevt(IndexComponents::One(LINKS)));
    reverse.reverse = true;
    assert_eq!(
        names(&value, reverse),
        ["item-0005", "item-0003", "item-0004", "item-0003"]
    );
    // A supplied AVET value chooses only the start, not an equality filter:
    // recovered index-pull continues through the attribute and does not dedup.
    let start = IndexBoundary::Avet(IndexComponents::Two(TAG, Value::String("blue".into())));
    assert_eq!(
        names(&value, options(start)),
        ["item-0000", "item-0000", "item-0001"]
    );
}

#[test]
fn index_pull_checks_index_start_schema_and_selector_even_for_empty_results() {
    let value = database().db_after.database_value();
    for (start, code) in [
        (
            IndexBoundary::Eavt(IndexComponents::Empty),
            "index-pull/invalid-index",
        ),
        (
            IndexBoundary::Vaet(IndexComponents::Empty),
            "index-pull/invalid-index",
        ),
        (
            IndexBoundary::Avet(IndexComponents::Empty),
            "index-pull/missing-attribute",
        ),
        (
            IndexBoundary::Aevt(IndexComponents::Empty),
            "index-pull/missing-attribute",
        ),
        (
            IndexBoundary::Avet(IndexComponents::One(TAG)),
            "index-pull/missing-start-value",
        ),
        (
            IndexBoundary::Aevt(IndexComponents::One(SCORE)),
            "index-pull/aevt-not-ref-many",
        ),
        (
            IndexBoundary::Aevt(IndexComponents::One(TAG)),
            "index-pull/aevt-not-ref-many",
        ),
    ] {
        assert_eq!(value.index_pull(options(start)).unwrap_err().code, code);
    }
    assert!(
        value
            .index_pull(options(IndexBoundary::Avet(IndexComponents::One(
                UNINDEXED
            ))))
            .is_err()
    );
    assert!(
        value
            .index_pull(options(IndexBoundary::Avet(IndexComponents::Two(
                SCORE,
                Value::String("wrong type".into())
            ))))
            .is_err()
    );
    assert_eq!(
        value
            .clone()
            .history()
            .index_pull(options(score_start(None)))
            .unwrap_err()
            .code,
        "database/history-not-point-in-time"
    );
    let mut invalid = options(IndexBoundary::Avet(IndexComponents::One(EMPTY)));
    invalid.limit = Some(0);
    invalid.selector =
        PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(99_999))]);
    assert!(value.index_pull(invalid).is_err());
    assert!(
        names(
            &value,
            options(IndexBoundary::Avet(IndexComponents::One(EMPTY)))
        )
        .is_empty()
    );
}

#[test]
fn projection_is_lazy_and_cancellation_covers_offsets_and_fuses_errors() {
    let value = database().db_after.database_value();
    let projected = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&projected);
    let filtered = value.clone().filter(move |_, datom| {
        if datom.attribute == NAME {
            observed.fetch_add(1, Ordering::Relaxed);
        }
        true
    });
    let mut limited = options(score_start(None));
    limited.offset = 4;
    limited.limit = Some(1);
    let mut cursor = filtered.index_pull(limited).unwrap();
    assert_eq!(projected.load(Ordering::Relaxed), 0);
    assert_eq!(name(&cursor.next().unwrap().unwrap()), "item-0004");
    assert_eq!(projected.load(Ordering::Relaxed), 1);
    assert!(cursor.next().is_none());
    assert_eq!(projected.load(Ordering::Relaxed), 1);

    let cancel = Arc::new(AtomicBool::new(false));
    let triggered = Arc::clone(&cancel);
    let canceled = value.filter(move |_, datom| {
        if datom.attribute == SCORE {
            triggered.store(true, Ordering::Relaxed);
        }
        true
    });
    let control = PullControl {
        cancel,
        ..PullControl::default()
    };
    let mut offset = options(score_start(None));
    offset.offset = 9;
    let mut cursor = canceled.index_pull_with_control(offset, &control).unwrap();
    assert_eq!(cursor.next().unwrap().unwrap_err().code, "pull/canceled");
    assert!(cursor.next().is_none());
    let no_entities = PullControl {
        max_entities: 0,
        ..PullControl::default()
    };
    let mut cursor = filtered
        .index_pull_with_control(options(score_start(None)), &no_entities)
        .unwrap();
    assert_eq!(
        cursor.next().unwrap().unwrap_err().code,
        "pull/entity-limit"
    );
    assert!(cursor.next().is_none());
}

#[test]
fn cancellation_interrupts_rejected_index_candidates_before_exhaustion() {
    let value = database().db_after.database_value();
    let control = PullControl::default();
    let cancel = Arc::clone(&control.cancel);
    let reject = Arc::new(AtomicBool::new(true));
    let active = Arc::clone(&reject);
    let candidates = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&candidates);
    let filtered = value.filter(move |_, datom| {
        if datom.attribute == SCORE && active.load(Ordering::Relaxed) {
            if observed.fetch_add(1, Ordering::Relaxed) + 1 == 3 {
                cancel.store(true, Ordering::Relaxed);
            }
            return false;
        }
        true
    });
    let mut cursor = filtered
        .index_pull_with_control(options(score_start(None)), &control)
        .unwrap();
    assert_eq!(candidates.load(Ordering::Relaxed), 0);
    assert_eq!(cursor.next().unwrap().unwrap_err().code, "pull/canceled");
    assert!((3..=4).contains(&candidates.load(Ordering::Relaxed)));
    assert!(cursor.next().is_none());
    drop(cursor);

    reject.store(false, Ordering::Relaxed);
    control.cancel.store(false, Ordering::Relaxed);
    let mut cursor = filtered
        .index_pull_with_control(options(score_start(None)), &control)
        .unwrap();
    assert_eq!(name(&cursor.next().unwrap().unwrap()), "item-0000");
}

#[test]
fn temporal_and_custom_views_use_the_same_snapshot_for_walk_and_projection() {
    let seeded = database();
    let old = seeded.db_after.database_value();
    let item = seeded.tempids["item-0000"];
    let changed = old
        .with(
            &[
                add(EntityRef::Id(item), SCORE, Value::Long(50)),
                add(EntityRef::Id(item), NAME, Value::String("changed".into())),
                TxOp::Retract {
                    entity: EntityRef::Id(seeded.tempids["item-0001"]),
                    attribute: SCORE,
                    value: None,
                },
            ],
            11,
        )
        .unwrap()
        .db_after;
    assert_eq!(names(&old, options(score_start(Some(0))))[0], "item-0000");
    assert_eq!(
        names(
            &changed.clone().as_of(old.basis_t()),
            options(score_start(Some(0)))
        )[0],
        "item-0000"
    );
    assert_eq!(
        names(
            &changed.clone().since(old.basis_t()),
            options(score_start(None))
        ),
        ["changed"]
    );
    let hidden = seeded.tempids["item-0004"];
    let filtered = changed.filter(move |_, datom| datom.entity != hidden);
    let actual = names(&filtered, options(score_start(None)));
    assert!(
        !actual
            .iter()
            .any(|name| name == "item-0004" || name == "item-0001")
    );
    assert_eq!(actual.last().unwrap(), "changed");
    let mut reverse = options(score_start(Some(99)));
    reverse.reverse = true;
    let mut expected = actual;
    expected.reverse();
    assert_eq!(names(&filtered, reverse), expected);
}

#[test]
fn postgres_native_index_pull_is_lazy_and_retains_old_and_temporal_values() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let id = format!(
        "index-pull-{}-{}",
        std::process::id(),
        std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    common::install(&postgres).unwrap();
    common::TestStore::connect(&postgres)
        .unwrap()
        .create_database(&id, schema())
        .unwrap();
    let writer = common::start_service(&postgres, &id);
    let seeded = writer
        .client()
        .transact(
            atomic_core::TransactionRequest::new("seed", seed(512)).with_tx_instant(10),
            Duration::from_secs(20),
        )
        .unwrap();
    common::consolidate(&postgres, &id).unwrap();
    let peer = atomic_core::Peer::connect(&postgres, &id, 0).unwrap();
    let database = peer.db();
    let projected = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&projected);
    let filtered = database.clone().filter(move |_, datom| {
        if datom.attribute == NAME {
            observed.fetch_add(1, Ordering::Relaxed);
        }
        true
    });
    let baseline = peer.load_stats();
    let mut selected = options(score_start(Some(256)));
    selected.offset = 3;
    selected.limit = Some(2);
    let mut cursor = filtered.index_pull(selected.clone()).unwrap();
    assert_eq!(
        peer.load_stats(),
        baseline,
        "construction must not visit a tree child"
    );
    assert_eq!(projected.load(Ordering::Relaxed), 0);
    assert_eq!(
        cursor
            .by_ref()
            .map(|value| name(&value.unwrap()))
            .collect::<Vec<_>>(),
        ["item-0259", "item-0260"]
    );
    assert_eq!(projected.load(Ordering::Relaxed), 2);
    drop(cursor);
    let after = peer.load_stats();
    eprintln!(
        "native index-pull: 512 entities, offset3/limit2, projected2, cursor SQL reads={}, bytes={}, leaves={}",
        after.cursor_sql_reads - baseline.cursor_sql_reads,
        after.cursor_sql_read_bytes - baseline.cursor_sql_read_bytes,
        after.cursor_leaf_reads - baseline.cursor_leaf_reads
    );
    assert_eq!(
        names(
            &database,
            options(IndexBoundary::Aevt(IndexComponents::One(LINKS)))
        ),
        ["item-0003", "item-0004", "item-0003", "item-0005"]
    );
    let changed = writer
        .client()
        .transact(
            atomic_core::TransactionRequest::new(
                "change",
                vec![
                    add(
                        EntityRef::Id(seeded.tempids["item-0259"]),
                        SCORE,
                        Value::Long(9_999),
                    ),
                    add(
                        EntityRef::Id(seeded.tempids["item-0259"]),
                        NAME,
                        Value::String("changed".into()),
                    ),
                ],
            )
            .with_tx_instant(11),
            Duration::from_secs(20),
        )
        .unwrap();
    writer.shutdown();
    assert_eq!(
        names(&database, selected.clone()),
        ["item-0259", "item-0260"]
    );
    let current = peer.sync().unwrap();
    assert_eq!(current.basis_t(), changed.basis_t);
    assert_eq!(
        names(&current, selected.clone()),
        ["item-0260", "item-0261"]
    );
    assert_eq!(
        names(&current.clone().as_of(seeded.basis_t), selected),
        ["item-0259", "item-0260"]
    );
    assert_eq!(
        names(&current.since(seeded.basis_t), options(score_start(None))),
        ["changed"]
    );
}
