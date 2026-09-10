//! Public lazy-Pull regression witnesses with view predicates counting actual
//! candidate consumption rather than only the size of the returned value.
use atomic_core::{
    Attribute, AttributeName, Cardinality, Database, DatabaseValue, EntityRef, Keyword,
    PullAttribute, PullControl, PullLimit, PullNested, PullPattern, PullTransform, QueryValue,
    Schema, TxOp, TxValue, Value, ValueType,
};
use std::sync::{
    Arc, OnceLock,
    atomic::{AtomicBool, AtomicUsize, Ordering},
};
use std::time::Instant;

const MANY: u32 = 1_000;
const REFS: u32 = 1_001;
const BACK: u32 = 1_002;
// The scalar-only fixture and graph fixture have independent issued frontiers.
const MARKER: u32 = 1_001;

fn many_values(count: usize) -> (DatabaseValue, u64) {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            MANY,
            Keyword::new("bounded", "many"),
            ValueType::Long,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            MARKER,
            Keyword::new("bounded", "marker"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let mut operations = (0..count)
        .map(|value| TxOp::Add {
            entity: EntityRef::Temp("root".into()),
            attribute: MANY,
            value: Value::Long(value as i64).into(),
        })
        .collect::<Vec<_>>();
    operations.push(TxOp::Add {
        entity: EntityRef::Temp("root".into()),
        attribute: MARKER,
        value: Value::Long(99).into(),
    });
    let report = Database::new(schema).unwrap().with(&operations, 1).unwrap();
    (report.db_after.database_value(), report.tempids["root"])
}

fn thousand_values() -> (DatabaseValue, u64) {
    static DATABASE: OnceLock<(DatabaseValue, u64)> = OnceLock::new();
    DATABASE.get_or_init(|| many_values(1_000)).clone()
}

fn limited(attribute: u32, limit: usize) -> PullAttribute {
    let mut selector = PullAttribute::forward(AttributeName::Id(attribute));
    selector.limit = PullLimit::Limit(limit);
    selector
}

fn numbers(values: impl IntoIterator<Item = i64>) -> QueryValue {
    QueryValue::Collection(
        values
            .into_iter()
            .map(|value| QueryValue::Scalar(Value::Long(value)))
            .collect(),
    )
}

fn field<'a>(value: &'a QueryValue, name: &str) -> Option<&'a QueryValue> {
    let QueryValue::Map(fields) = value else {
        panic!("expected Pull map")
    };
    fields.iter().find_map(|(key, value)| {
        matches!(key, QueryValue::Scalar(Value::Keyword(keyword))
            if keyword.namespace.as_deref() == Some("bounded") && keyword.name == name)
        .then_some(value)
    })
}

#[test]
fn forward_limit_one_consumes_a_bounded_prefix_of_one_thousand_values() {
    let (database, root) = thousand_values();
    let candidates = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&candidates);
    let view = database.filter(move |_, datom| {
        if datom.entity == root && datom.attribute == MANY {
            observed.fetch_add(1, Ordering::Relaxed);
        }
        true
    });
    let mut selector = PullAttribute::forward(AttributeName::Id(MANY));
    selector.limit = PullLimit::Limit(1);
    let started = Instant::now();
    let pulled = view
        .pull(&PullPattern::attributes(vec![selector]), root)
        .unwrap();
    assert_eq!(
        field(&pulled, "many"),
        Some(&QueryValue::Collection(vec![QueryValue::Scalar(
            Value::Long(0)
        )]))
    );
    drop(pulled);
    let visited = candidates.load(Ordering::Relaxed);
    eprintln!(
        "PULL_LIMIT_ONE fanout=1000 visible_candidates={visited} complete_pull_drop_us={}",
        started.elapsed().as_micros()
    );
    assert!(
        visited <= 4,
        "limit 1 must stop candidate consumption, allowing only bounded existence/lookahead overhead; visited {visited} of 1000"
    );
}

#[test]
fn filtered_limits_count_visible_values_without_scanning_unrelated_suffixes() {
    for width in [32, 128, 1_000] {
        let (database, root) = if width == 1_000 {
            thousand_values()
        } else {
            many_values(width)
        };
        let candidates = Arc::new(AtomicUsize::new(0));
        let observed = Arc::clone(&candidates);
        let view = database.filter(move |_, datom| {
            if datom.entity == root && datom.attribute == MANY {
                observed.fetch_add(1, Ordering::Relaxed);
                return matches!(datom.value, Value::Long(value) if value % 7 == 3);
            }
            true
        });
        // Independent arithmetic oracle, not another Pull implementation.
        let expected = (0..width as i64).filter(|value| value % 7 == 3).take(2);
        let result = view
            .pull(&PullPattern::attributes(vec![limited(MANY, 2)]), root)
            .unwrap();
        assert_eq!(field(&result, "many"), Some(&numbers(expected)));
        drop(result);
        let visited = candidates.load(Ordering::Relaxed);
        assert!(
            (11..=13).contains(&visited),
            "width={width}, visits={visited}"
        );
        eprintln!("PULL_FILTER fanout={width} limit=2 candidate_visits={visited}");
    }
}

fn graph() -> (DatabaseValue, u64, Vec<(u64, i64)>) {
    let mut schema = Schema::new();
    for (id, name, kind) in [
        (MANY, "many", ValueType::Long),
        (REFS, "refs", ValueType::Ref),
        (BACK, "back", ValueType::Ref),
    ] {
        schema
            .install(Attribute::new(
                id,
                Keyword::new("bounded", name),
                kind,
                Cardinality::Many,
            ))
            .unwrap();
    }
    let mut operations = Vec::new();
    for child in 0..32 {
        let name = format!("child-{child:04}");
        for offset in 0..16 {
            operations.push(TxOp::Add {
                entity: EntityRef::Temp(name.clone()),
                attribute: MANY,
                value: Value::Long(child * 100 + offset).into(),
            });
        }
        for (entity, attribute, target) in [
            ("root".to_owned(), REFS, name.clone()),
            (name, BACK, "root".to_owned()),
        ] {
            operations.push(TxOp::Add {
                entity: EntityRef::Temp(entity),
                attribute,
                value: TxValue::Entity(EntityRef::Temp(target)),
            });
        }
    }
    let report = Database::new(schema).unwrap().with(&operations, 1).unwrap();
    let mut children = (0..32)
        .map(|child| (report.tempids[&format!("child-{child:04}")], child * 100))
        .collect::<Vec<_>>();
    children.sort_by_key(|(entity, _)| *entity);
    (
        report.db_after.database_value(),
        report.tempids["root"],
        children,
    )
}

#[test]
fn forward_reverse_and_nested_limits_do_not_expand_unselected_siblings() {
    let (database, root, children) = graph();
    for reverse in [false, true] {
        let edge_visits = Arc::new(AtomicUsize::new(0));
        let value_visits = Arc::new(AtomicUsize::new(0));
        let edges = Arc::clone(&edge_visits);
        let values = Arc::clone(&value_visits);
        let view = database.clone().filter(move |_, datom| {
            if datom.attribute == if reverse { BACK } else { REFS } {
                edges.fetch_add(1, Ordering::Relaxed);
            }
            if datom.attribute == MANY {
                values.fetch_add(1, Ordering::Relaxed);
            }
            true
        });
        let mut selector = if reverse {
            PullAttribute::reverse(AttributeName::Id(BACK))
        } else {
            PullAttribute::forward(AttributeName::Id(REFS))
        };
        selector.limit = PullLimit::Limit(2);
        selector.nested = Some(PullNested::Pattern(Box::new(PullPattern::attributes(
            vec![limited(MANY, 1)],
        ))));
        let result = view
            .pull(&PullPattern::attributes(vec![selector]), root)
            .unwrap();
        let expected = QueryValue::Collection(
            children
                .iter()
                .take(2)
                .map(|(_, first)| {
                    QueryValue::Map(vec![(
                        QueryValue::Scalar(Value::Keyword(Keyword::new("bounded", "many"))),
                        numbers([*first]),
                    )])
                })
                .collect(),
        );
        assert_eq!(
            field(&result, if reverse { "_back" } else { "refs" }),
            Some(&expected)
        );
        drop(result);
        let edges = edge_visits.load(Ordering::Relaxed);
        let values = value_visits.load(Ordering::Relaxed);
        assert!((2..=4).contains(&edges), "reverse={reverse}, edges={edges}");
        assert!(
            (2..=4).contains(&values),
            "reverse={reverse}, values={values}"
        );
        eprintln!(
            "PULL_NESTED reverse={reverse} selected=2 edge_visits={edges} child_value_visits={values}"
        );
    }
}

#[test]
fn wildcard_limit_override_still_discovers_later_attributes_without_full_values() {
    let (database, root) = thousand_values();
    let candidates = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&candidates);
    let view = database.filter(move |_, datom| {
        if datom.entity == root && datom.attribute == MANY {
            observed.fetch_add(1, Ordering::Relaxed);
        }
        true
    });
    let pattern = PullPattern {
        wildcard: true,
        attributes: vec![limited(MANY, 1)],
    };
    let result = view.pull(&pattern, root).unwrap();
    assert_eq!(field(&result, "many"), Some(&numbers([0])));
    assert_eq!(
        field(&result, "marker"),
        Some(&QueryValue::Scalar(Value::Long(99)))
    );
    drop(result);
    let visited = candidates.load(Ordering::Relaxed);
    assert!(
        visited <= 4,
        "wildcard attribute discovery consumed {visited} values"
    );
    eprintln!("PULL_WILDCARD fanout=1000 limit=1 candidate_visits={visited}");
}

#[test]
fn default_and_unlimited_limits_preserve_the_documented_result_prefixes() {
    let (database, root) = many_values(1_050);
    for (limit, expected_count) in [
        (PullLimit::Limit(3), 3),
        (PullLimit::Default, 1_000),
        (PullLimit::Unlimited, 1_050),
    ] {
        let candidates = Arc::new(AtomicUsize::new(0));
        let observed = Arc::clone(&candidates);
        let view = database.clone().filter(move |_, datom| {
            if datom.entity == root && datom.attribute == MANY {
                observed.fetch_add(1, Ordering::Relaxed);
            }
            true
        });
        let mut selector = PullAttribute::forward(AttributeName::Id(MANY));
        selector.limit = limit;
        let result = view
            .pull(&PullPattern::attributes(vec![selector]), root)
            .unwrap();
        assert_eq!(
            field(&result, "many"),
            Some(&numbers(0..expected_count as i64))
        );
        drop(result);
        let visited = candidates.load(Ordering::Relaxed);
        assert!(
            visited >= expected_count && visited <= expected_count + 2,
            "limit={limit:?}, visits={visited}"
        );
    }
}

#[test]
fn rejecting_filter_cancellation_stops_during_consumption_and_view_is_reusable() {
    let (database, root) = thousand_values();
    let control = PullControl::default();
    let cancel = Arc::clone(&control.cancel);
    let trigger = Arc::new(AtomicBool::new(true));
    let active = Arc::clone(&trigger);
    let candidates = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&candidates);
    let view = database.filter(move |_, datom| {
        if datom.entity == root && datom.attribute == MANY {
            let visited = observed.fetch_add(1, Ordering::Relaxed) + 1;
            if active.load(Ordering::Relaxed) {
                if visited == 7 {
                    cancel.store(true, Ordering::Relaxed);
                }
                return false;
            }
        }
        true
    });
    let pattern = PullPattern::attributes(vec![limited(MANY, 1)]);
    control.cancel.store(true, Ordering::Relaxed);
    assert_eq!(
        view.pull_with_control(&pattern, root, &control)
            .unwrap_err()
            .code,
        "pull/canceled"
    );
    assert_eq!(candidates.load(Ordering::Relaxed), 0);
    control.cancel.store(false, Ordering::Relaxed);
    assert_eq!(
        view.pull_with_control(&pattern, root, &control)
            .unwrap_err()
            .code,
        "pull/canceled"
    );
    let interrupted_at = candidates.load(Ordering::Relaxed);
    assert!(
        (7..=8).contains(&interrupted_at),
        "cancellation consumed {interrupted_at} rejected candidates"
    );
    trigger.store(false, Ordering::Relaxed);
    control.cancel.store(false, Ordering::Relaxed);
    candidates.store(0, Ordering::Relaxed);
    let result = view.pull_with_control(&pattern, root, &control).unwrap();
    assert_eq!(field(&result, "many"), Some(&numbers([0])));
    assert!(candidates.load(Ordering::Relaxed) <= 2);
}

#[test]
fn transforms_receive_only_the_limited_value_and_defaults_follow_nil() {
    let (database, root) = thousand_values();
    for missing in [false, true] {
        let candidates = Arc::new(AtomicUsize::new(0));
        let observed = Arc::clone(&candidates);
        let view = database.clone().filter(move |_, datom| {
            if datom.entity == root && datom.attribute == MANY {
                observed.fetch_add(1, Ordering::Relaxed);
                return !missing;
            }
            true
        });
        let calls = Arc::new(AtomicUsize::new(0));
        let called = Arc::clone(&calls);
        let mut selector = limited(MANY, 2);
        selector.default = Some(QueryValue::Scalar(Value::String("fallback".into())));
        selector.transform = Some(PullTransform::new("bounded-input", move |value| {
            let expected = if missing {
                QueryValue::Nil
            } else {
                numbers([0, 1])
            };
            assert_eq!(value, &expected);
            called.fetch_add(1, Ordering::Relaxed);
            Ok(QueryValue::Nil)
        }));
        let result = view
            .pull(&PullPattern::attributes(vec![selector]), root)
            .unwrap();
        assert_eq!(
            field(&result, "many"),
            Some(&QueryValue::Scalar(Value::String("fallback".into())))
        );
        assert_eq!(calls.load(Ordering::Relaxed), 1);
        let visited = candidates.load(Ordering::Relaxed);
        if missing {
            // Proving absence through an opaque filter requires exhaustion.
            assert!((1_000..=1_002).contains(&visited));
        } else {
            assert!((2..=4).contains(&visited));
        }
    }
}

#[test]
fn bounded_pull_preserves_old_current_temporal_views_and_rejects_history() {
    let (before, root) = thousand_values();
    let after = before
        .with(
            &[
                TxOp::Retract {
                    entity: EntityRef::Id(root),
                    attribute: MANY,
                    value: Some(Value::Long(0).into()),
                },
                TxOp::Add {
                    entity: EntityRef::Id(root),
                    attribute: MANY,
                    value: Value::Long(1_000).into(),
                },
            ],
            2,
        )
        .unwrap()
        .db_after;
    let pattern = PullPattern::attributes(vec![limited(MANY, 1)]);
    for (view, expected) in [
        (before.clone(), 0),
        (after.clone(), 1),
        (after.clone().as_of(before.basis_t()), 0),
        (after.clone().since(before.basis_t()), 1_000),
    ] {
        let result = view.pull(&pattern, root).unwrap();
        assert_eq!(field(&result, "many"), Some(&numbers([expected])));
    }
    assert_eq!(
        after.history().pull(&pattern, root).unwrap_err().code,
        "database/history-not-point-in-time"
    );
}
