use super::*;
use crate::database_value::TransactionReadContext;
use crate::{Attribute, EntityRef, Schema, TxOp};
use std::sync::atomic::AtomicUsize;

const MANY: u32 = 1_000;

fn fixture(count: usize) -> (DatabaseValue, u64) {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            MANY,
            Keyword::new("bounded", "many"),
            ValueType::Long,
            Cardinality::Many,
        ))
        .unwrap();
    let operations = (0..count)
        .map(|value| TxOp::Add {
            entity: EntityRef::Temp("root".into()),
            attribute: MANY,
            value: Value::Long(value as i64).into(),
        })
        .collect::<Vec<_>>();
    let report = Database::new(schema).unwrap().with(&operations, 1).unwrap();
    (report.db_after.database_value(), report.tempids["root"])
}

fn pattern() -> PullPattern {
    let mut selector = PullAttribute::forward(AttributeName::Id(MANY));
    selector.limit = PullLimit::Limit(1);
    PullPattern::attributes(vec![selector])
}

fn budget(work: usize, bytes: usize) -> QueryPullBudget<'static> {
    QueryPullBudget::new(Arc::new(AtomicBool::new(false)), None, work, 0)
        .with_value_budget(0, bytes)
}

fn assert_single(value: &QueryValue, expected: i64) {
    assert_eq!(
        value,
        &QueryValue::Map(vec![(
            keyword_key(Keyword::new("bounded", "many")),
            QueryValue::Collection(vec![QueryValue::Scalar(Value::Long(expected))]),
        )])
    );
}

#[test]
fn limits_bound_shared_work_and_owned_value_allocations() {
    let (database, root) = fixture(1_000);
    let candidates = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&candidates);
    let view = database.filter(move |_, datom| {
        if datom.entity == root && datom.attribute == MANY {
            observed.fetch_add(1, Ordering::Relaxed);
        }
        true
    });
    let mut budget = budget(32, 2_048);
    let result = view.pull_for_query(&pattern(), root, &mut budget).unwrap();
    assert_single(&result, 0);
    drop(result);
    assert_eq!(candidates.load(Ordering::Relaxed), 1);
    assert!(budget.work() <= 32);
    assert!(budget.value_bytes() <= 2_048);
    eprintln!(
        "bounded_query_pull candidates=1 work={} allocated_payload_bytes={}",
        budget.work(),
        budget.value_bytes()
    );
}

#[test]
fn rejected_view_candidates_are_interruptible_and_budgeted() {
    let (database, root) = fixture(1_000);
    let candidates = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&candidates);
    let view = database.filter(move |_, datom| {
        if datom.entity == root && datom.attribute == MANY {
            observed.fetch_add(1, Ordering::Relaxed);
        }
        false
    });
    let error = view
        .pull_for_query(&pattern(), root, &mut budget(12, 4_096))
        .unwrap_err();
    assert_eq!(error.code, "query/work-limit");
    let consumed = candidates.load(Ordering::Relaxed);
    assert!(consumed > 0 && consumed < 12, "consumed={consumed}");
    assert_eq!(
        view.pull_for_query(&pattern(), root, &mut budget(10_000, usize::MAX))
            .unwrap(),
        QueryValue::Map(Vec::new())
    );
}

#[test]
fn speculative_removed_prefix_is_polled_before_a_visible_item() {
    let (database, root) = fixture(1_000);
    let removals = (0..999)
        .map(|value| TxOp::Retract {
            entity: EntityRef::Id(root),
            attribute: MANY,
            value: Some(Value::Long(value).into()),
        })
        .collect::<Vec<_>>();
    let overlay = database.with(&removals, 2).unwrap().db_after;
    assert_eq!(
        overlay
            .pull_for_query(&pattern(), root, &mut budget(16, 4_096))
            .unwrap_err()
            .code,
        "query/work-limit"
    );
    assert_single(
        &overlay
            .pull_for_query(&pattern(), root, &mut budget(10_000, usize::MAX))
            .unwrap(),
        999,
    );
    assert_eq!(database.values(root, MANY).unwrap().len(), 1_000);
}

#[test]
fn controlled_range_stop_never_admits_an_incomplete_transaction_prefix() {
    let (database, root) = fixture(32);
    let context = Arc::new(TransactionReadContext::new(1_000, u64::MAX));
    let database = database.with_transaction_read_context(Arc::clone(&context));
    let prefix = IndexPrefix::Eavt {
        entity: root,
        attribute: Some(MANY),
        value: None,
    };
    let mut cursor = database.prefix_cursor(&prefix).unwrap();
    let mut control =
        |datom: Option<&Datom>| Ok(datom.is_none_or(|datom| datom.value == Value::Long(0)));
    assert_eq!(
        cursor
            .next_with_control(&mut control)
            .transpose()
            .unwrap()
            .unwrap()
            .value,
        Value::Long(0)
    );
    assert!(cursor.next_with_control(&mut control).is_none());
    assert!(cursor.next().is_none());
    drop(cursor);
    assert_eq!(context.snapshot().unwrap().memo_admissions, 0);
    assert_eq!(context.snapshot().unwrap().memo_rejections, 1);
    assert_eq!(database.datoms_with_prefix(&prefix).unwrap().len(), 32);
    assert_eq!(context.snapshot().unwrap().memo_admissions, 1);
    assert_eq!(database.datoms_with_prefix(&prefix).unwrap().len(), 32);
    assert_eq!(context.snapshot().unwrap().prefix_hits, 1);
}

#[test]
fn range_stop_is_applied_before_rejecting_view_predicates() {
    let (database, root) = fixture(32);
    let visits = Arc::new(AtomicUsize::new(0));
    let observed = Arc::clone(&visits);
    let view = database.filter(move |_, _| {
        observed.fetch_add(1, Ordering::Relaxed);
        false
    });
    let mut cursor = view
        .seek_cursor(&IndexBoundary::Eavt(IndexComponents::Two(root, MANY)))
        .unwrap();
    let mut examined = 0;
    assert!(
        cursor
            .next_with_control(&mut |datom| {
                if let Some(datom) = datom {
                    examined += 1;
                    return Ok(matches!(datom.value, Value::Long(value) if value < 5));
                }
                Ok(true)
            })
            .is_none()
    );
    assert_eq!(examined, 6); // Five rejected values and one upper-bound sentinel.
    assert_eq!(visits.load(Ordering::Relaxed), 5);
    assert!(cursor.next().is_none());
}

#[test]
fn lookup_identifier_checks_cancellation_inside_a_rejecting_view() {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                MANY,
                Keyword::new("bounded", "key"),
                ValueType::Long,
                Cardinality::One,
            )
            .unique(crate::Unique::Identity),
        )
        .unwrap();
    let report = Database::new(schema)
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("root".into()),
                attribute: MANY,
                value: Value::Long(7).into(),
            }],
            1,
        )
        .unwrap();
    let control = PullControl::default();
    let cancel = Arc::clone(&control.cancel);
    let armed = Arc::new(AtomicBool::new(true));
    let view = report.db_after.database_value().filter(move |_, datom| {
        if datom.attribute == MANY && armed.swap(false, Ordering::Relaxed) {
            cancel.store(true, Ordering::Relaxed);
            false
        } else {
            true
        }
    });
    let identifier = EntityIdentifier::Lookup {
        attribute: AttributeName::Id(MANY),
        value: Value::Long(7),
    };
    assert_eq!(
        view.pull_with_control(&pattern(), identifier.clone(), &control)
            .unwrap_err()
            .code,
        "pull/canceled"
    );
    control.cancel.store(false, Ordering::Relaxed);
    assert_eq!(
        view.pull_with_control(&pattern(), identifier, &control)
            .unwrap(),
        QueryValue::Map(vec![(
            keyword_key(Keyword::new("bounded", "key")),
            QueryValue::Scalar(Value::Long(7)),
        )])
    );
}

#[test]
fn ordinary_and_controlled_cursor_complete_consumption_costs() {
    let (database, root) = fixture(1_000);
    let prefix = IndexPrefix::Eavt {
        entity: root,
        attribute: Some(MANY),
        value: None,
    };
    let repeats = 32;
    for controlled in [false, true] {
        let started = Instant::now();
        let mut visits = 0;
        for _ in 0..repeats {
            let mut cursor = database.prefix_cursor(&prefix).unwrap();
            let mut values = Vec::new();
            loop {
                let next = if controlled {
                    cursor.next_with_control(&mut |datom| {
                        visits += usize::from(datom.is_some());
                        Ok(true)
                    })
                } else {
                    cursor.next()
                };
                let Some(datom) = next else { break };
                values.push(datom.unwrap().value);
            }
            assert_eq!(values.len(), 1_000);
            assert_eq!(values.first(), Some(&Value::Long(0)));
            assert_eq!(values.last(), Some(&Value::Long(999)));
            drop(values);
            drop(cursor);
        }
        assert_eq!(visits, if controlled { repeats * 1_000 } else { 0 });
        eprintln!(
            "complete_eager_prefix controlled={controlled} repeats={repeats} datoms_per_repeat=1000 elapsed={:?}",
            started.elapsed()
        );
    }
}

#[test]
fn native_superseded_prefix_is_budgeted_before_its_first_visible_output() {
    use crate::{
        CapacityLimits, Peer, PostgresIndexer, PostgresMigrator, PostgresStore, TransactionRequest,
        TransactionService, TransactionServiceConfig,
    };
    use std::time::{Duration, SystemTime, UNIX_EPOCH};

    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: native merge admission requires ATOMIC_POSTGRES_URL");
        return;
    };
    let database_id = format!(
        "pull-native-skip-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut schema = Schema::new();
    let mut attribute = Attribute::new(
        MANY,
        Keyword::new("bounded", "many"),
        ValueType::Long,
        Cardinality::Many,
    );
    attribute.indexed = true;
    schema.install(attribute).unwrap();
    PostgresStore::connect(&connection)
        .unwrap()
        .create_database(&database_id, schema)
        .unwrap();
    let writer = TransactionService::start(TransactionServiceConfig {
        connection: connection.clone(),
        database_id: database_id.clone(),
        holder_id: format!("{database_id}-writer"),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    })
    .unwrap();
    let count = 512;
    let operations = (0..count)
        .map(|value| TxOp::Add {
            entity: EntityRef::Temp("root".into()),
            attribute: MANY,
            value: Value::Long(value).into(),
        })
        .collect();
    let seeded = writer
        .client()
        .transact(
            TransactionRequest::new("seed", operations).with_tx_instant(1),
            Duration::from_secs(30),
        )
        .unwrap();
    let root = seeded.tempids["root"];
    PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    let before_peer = Peer::connect(&connection, &database_id, 64).unwrap();
    let before = before_peer.db();
    let mut changes = (0..count - 1)
        .map(|value| TxOp::Retract {
            entity: EntityRef::Id(root),
            attribute: MANY,
            value: Some(Value::Long(value).into()),
        })
        .collect::<Vec<_>>();
    changes.push(TxOp::Add {
        entity: EntityRef::Id(root),
        attribute: MANY,
        value: Value::Long(-1).into(),
    });
    writer
        .client()
        .transact(
            TransactionRequest::new("recent-replacements", changes).with_tx_instant(2),
            Duration::from_secs(30),
        )
        .unwrap();
    writer.shutdown();
    let peer = Peer::connect(&connection, &database_id, 64).unwrap();
    let current = peer.db();
    let mut small = budget(32, 4_096);
    let result = current.pull_for_query(&pattern(), root, &mut small);
    assert!(
        matches!(&result, Err(error) if error.code == "query/work-limit"),
        "superseded {count}-fact durable prefix was not admitted: result={result:?}, work={}",
        small.work(),
    );
    let mut large = budget(10_000, usize::MAX);
    assert_single(
        &current
            .pull_for_query(&pattern(), root, &mut large)
            .unwrap(),
        -1,
    );
    assert!(
        large.work() >= count as usize,
        "skipped facts must consume work"
    );
    assert_single(&before.pull(&pattern(), root).unwrap(), 0);

    let mut native = peer
        .tiered_snapshot()
        .prefix_cursor(
            false,
            &IndexPrefix::Eavt {
                entity: root,
                attribute: Some(MANY),
                value: None,
            },
        )
        .unwrap();
    let mut polls = 0;
    assert!(
        native
            .next_with_poll(&mut || {
                polls += 1;
                Ok(polls < 32)
            })
            .is_none()
    );
    assert_eq!(polls, 32);
    assert!(
        native.next().is_none(),
        "a stopped native cursor stays fused"
    );
    drop(native);

    // The durable lookahead is 511, but the recent branch contains -1.
    // An upper fence must inspect the ordered merge, never either lookahead.
    let mut cursor = current.query_avet_start_cursor(MANY, None).unwrap();
    let mut candidates = Vec::new();
    let mut control = |datom: Option<&Datom>| {
        if let Some(datom) = datom {
            candidates.push(datom.value.clone());
            return Ok(datom.attribute == MANY && datom.value.index_cmp(&Value::Long(0)).is_lt());
        }
        Ok(true)
    };
    assert_eq!(
        cursor
            .next_with_control(&mut control)
            .unwrap()
            .unwrap()
            .value,
        Value::Long(-1)
    );
    assert!(cursor.next_with_control(&mut control).is_none());
    drop(cursor);
    assert_eq!(candidates, vec![Value::Long(-1), Value::Long(count - 1)]);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    eprintln!(
        "native merge skip admission width={count}, small_work={}, complete_pull_work={}, ordered_candidates={candidates:?}",
        small.work(),
        large.work()
    );
}
