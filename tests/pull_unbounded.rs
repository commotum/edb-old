//! Pull ellipsis has no language depth ceiling; explicit caller controls and
//! the documented default cardinality-many limit remain independent policies.
mod common;

use atomic_core::{
    Attribute, AttributeName, Cardinality, Database, DatabaseValue, EntityRef, Keyword,
    PullAttribute, PullControl, PullLimit, PullNested, PullPattern, QueryValue, Schema,
    TransactionRequest, TxOp, Value, ValueType,
};
use std::sync::atomic::Ordering;
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const NUMBER: u32 = 1_000;
const NEXT: u32 = 1_001;
const CHILD: u32 = 1_002;
const MANY: u32 = 1_003;

fn schema(two_components: bool) -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            NUMBER,
            Keyword::new("node", "number"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    for (id, name, component, cardinality) in [
        (NEXT, "next", two_components, Cardinality::One),
        (CHILD, "child", true, Cardinality::One),
        (MANY, "many", false, Cardinality::Many),
    ] {
        let mut attribute =
            Attribute::new(id, Keyword::new("node", name), ValueType::Ref, cardinality);
        attribute.component = component;
        schema.install(attribute).unwrap();
    }
    schema
}

fn chain_ops(length: usize, cycle: bool) -> Vec<TxOp> {
    let mut ops = Vec::new();
    for index in 0..length {
        let entity = EntityRef::Temp(format!("node-{index}"));
        ops.push(TxOp::Add {
            entity: entity.clone(),
            attribute: NUMBER,
            value: Value::Long(index as i64).into(),
        });
        let next = if index + 1 < length {
            Some(index + 1)
        } else {
            cycle.then_some(0)
        };
        if let Some(next) = next {
            for attribute in [NEXT, CHILD] {
                ops.push(TxOp::Add {
                    entity: entity.clone(),
                    attribute,
                    value: atomic_core::TxValue::Entity(EntityRef::Temp(format!("node-{next}"))),
                });
            }
        }
    }
    ops
}

fn chain(length: usize, cycle: bool, two_components: bool) -> (DatabaseValue, Vec<u64>) {
    let report = Database::new(schema(two_components))
        .unwrap()
        .with(&chain_ops(length, cycle), 1)
        .unwrap();
    let ids = (0..length)
        .map(|index| report.tempids[&format!("node-{index}")])
        .collect();
    (report.db_after.database_value(), ids)
}

fn recursive_pattern(limit: Option<usize>) -> PullPattern {
    let mut next = PullAttribute::forward(AttributeName::Id(NEXT));
    next.nested = Some(PullNested::Recursion(limit));
    PullPattern::attributes(vec![
        PullAttribute::forward(AttributeName::Id(NUMBER)),
        next,
    ])
}

fn field<'a>(value: &'a QueryValue, namespace: &str, name: &str) -> Option<&'a QueryValue> {
    let QueryValue::Map(fields) = value else {
        panic!("expected pull map")
    };
    fields.iter().find_map(|(key, value)| {
        matches!(key, QueryValue::Scalar(Value::Keyword(keyword)) if keyword.namespace.as_deref() == Some(namespace) && keyword.name == name).then_some(value)
    })
}

fn inspect_chain(result: &QueryValue, attribute: &str, length: usize, terminal: Option<u64>) {
    let mut cursor = result;
    for index in 0..length {
        assert!(
            matches!(field(cursor, "node", "number"), Some(QueryValue::Scalar(Value::Long(number))) if *number == index as i64)
        );
        if index + 1 < length {
            cursor = field(cursor, "node", attribute).expect("chain continues");
        }
    }
    match terminal {
        Some(entity) => {
            let terminal =
                field(cursor, "node", attribute).expect("cycle or recursion limit returns id");
            assert!(matches!(terminal, QueryValue::Map(fields) if fields.len() == 1));
            assert!(
                matches!(field(terminal, "db", "id"), Some(QueryValue::Scalar(Value::Ref(id))) if *id == entity)
            );
        }
        None => assert!(field(cursor, "node", attribute).is_none()),
    }
}

#[test]
fn unlimited_pull_and_component_cycles_cross_old_depth_limit_on_small_stack() {
    let length = 2_048;
    let (database, ids) = chain(length, true, false);
    let basis = database.basis_t();
    let original = database.clone();
    std::thread::Builder::new()
        .stack_size(256 * 1_024)
        .spawn(move || {
            let started = Instant::now();
            let recursive = database.pull(&recursive_pattern(None), ids[0]).unwrap();
            inspect_chain(&recursive, "next", length, Some(ids[0]));
            // Clone, comparison, and ordinary destruction must be stack-safe too.
            let clone = recursive.clone();
            assert!(recursive.canonical_cmp(&clone).is_eq());
            assert!(recursive == clone);
            drop(clone);
            drop(recursive);
            let wildcard = database.pull(&PullPattern::wildcard(), ids[0]).unwrap();
            inspect_chain(&wildcard, "child", length, Some(ids[0]));
            drop(wildcard);
            eprintln!(
                "pull deep navigation: {length} nodes, 256 KiB stack, {:?}",
                started.elapsed()
            );
        })
        .unwrap()
        .join()
        .unwrap();
    assert_eq!(original.basis_t(), basis);
}

#[test]
fn explicit_pull_controls_and_documented_many_default_remain_effective() {
    assert_eq!(PullControl::default().max_depth, usize::MAX);
    assert_eq!(PullControl::default().max_entities, usize::MAX);
    let (database, ids) = chain(1_201, false, false);
    let pattern = recursive_pattern(None);
    for (control, code) in [
        (
            PullControl {
                max_depth: 8,
                ..Default::default()
            },
            "pull/depth-limit",
        ),
        (
            PullControl {
                max_entities: 8,
                ..Default::default()
            },
            "pull/entity-limit",
        ),
    ] {
        assert_eq!(
            database
                .pull_with_control(&pattern, ids[0], &control)
                .unwrap_err()
                .code,
            code
        );
    }
    let canceled = PullControl::default();
    canceled.cancel.store(true, Ordering::Relaxed);
    assert_eq!(
        database
            .pull_with_control(&pattern, ids[0], &canceled)
            .unwrap_err()
            .code,
        "pull/canceled"
    );
    let bounded = database.pull(&recursive_pattern(Some(2)), ids[0]).unwrap();
    inspect_chain(&bounded, "next", 3, Some(ids[3]));
    let ops = ids
        .iter()
        .map(|target| TxOp::Add {
            entity: EntityRef::Id(ids[0]),
            attribute: MANY,
            value: Value::Ref(*target).into(),
        })
        .collect::<Vec<_>>();
    let database = database.with(&ops, 2).unwrap().db_after;
    let mut selector = PullAttribute::forward(AttributeName::Id(MANY));
    let default = database
        .pull(&PullPattern::attributes(vec![selector.clone()]), ids[0])
        .unwrap();
    assert!(
        matches!(field(&default, "node", "many"), Some(QueryValue::Collection(values)) if values.len() == 1_000)
    );
    selector.limit = PullLimit::Unlimited;
    let unlimited = database
        .pull(&PullPattern::attributes(vec![selector]), ids[0])
        .unwrap();
    assert!(
        matches!(field(&unlimited, "node", "many"), Some(QueryValue::Collection(values)) if values.len() == ids.len())
    );
}

#[test]
fn ordinary_component_pull_crosses_former_entity_visit_ceiling() {
    // Two distinct component edges to each next entity form a small DAG whose
    // pull result is a tree. Each branch must be expanded, not globally deduped.
    let (database, ids) = chain(17, false, true);
    let started = Instant::now();
    let result = database.pull(&PullPattern::wildcard(), ids[0]).unwrap();
    let mut pending = vec![&result];
    let mut maps = 0;
    while let Some(value) = pending.pop() {
        if let QueryValue::Map(fields) = value {
            maps += 1;
            pending.extend(fields.iter().map(|(_, value)| value));
        }
    }
    assert_eq!(maps, (1 << 17) - 1);
    drop(result);
    eprintln!(
        "pull component DAG: 17 stored entities, {maps} expanded maps, {:?}",
        started.elapsed()
    );
}

#[test]
fn native_postgres_pull_is_deep_stack_safe_and_keeps_its_captured_value() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP native deep pull: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let id = format!(
        "pull-unbounded-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    common::install(&postgres).unwrap();
    let mut store = common::TestStore::connect(&postgres).unwrap();
    store.create_database(&id, schema(false)).unwrap();
    let writer = common::start_service(&postgres, &id);
    let length = 768;
    let report = writer
        .client()
        .transact(
            TransactionRequest::new("chain", chain_ops(length, false)).with_tx_instant(1),
            Duration::from_secs(30),
        )
        .unwrap();
    let first = report.tempids["node-0"];
    let peer = atomic_core::Connection::connect(&postgres, &id, 16).unwrap();
    let captured = peer.db();
    writer
        .client()
        .transact(
            TransactionRequest::new(
                "new-value",
                vec![TxOp::Add {
                    entity: EntityRef::Id(first),
                    attribute: NUMBER,
                    value: Value::Long(-1).into(),
                }],
            )
            .with_tx_instant(2),
            Duration::from_secs(30),
        )
        .unwrap();
    writer.shutdown();
    std::thread::Builder::new()
        .stack_size(256 * 1_024)
        .spawn(move || {
            let started = Instant::now();
            let result = captured.pull(&recursive_pattern(None), first).unwrap();
            inspect_chain(&result, "next", length, None);
            let copy = result.clone();
            assert!(result == copy);
            drop(copy);
            drop(result);
            let wildcard = captured.pull(&PullPattern::wildcard(), first).unwrap();
            inspect_chain(&wildcard, "child", length, None);
            drop(wildcard);
            eprintln!(
                "native pull deep navigation: {length} nodes, writer stopped, {:?}",
                started.elapsed()
            );
        })
        .unwrap()
        .join()
        .unwrap();
}
