//! Local Pull docs: transforms receive the whole pulled value (including nil),
//! after limits/nesting and before defaults. Native callbacks replace JVM lookup.
mod common;

use atomic_core::{
    Attribute, AttributeName, Cardinality, Clause, DataPattern, Database, EntityRef, EntityValue,
    ErrorCategory, FindElement, FindSpec, Keyword, PullAttribute, PullControl, PullLimit,
    PullNested, PullPattern, PullTransform, Query, QueryControl, QueryValue, Schema, SemanticError,
    Symbol, Term, TransactionRequest, TxOp, TxValue, Value, ValueType, Variable,
};
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const TEXT: u32 = 1_000;
const NUMBER: u32 = 1_001;
const CHILD: u32 = 1_002;
const MANY: u32 = 1_003;
const TAG: u32 = 1_004;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, name, kind, many) in [
        (TEXT, "text", ValueType::String, false),
        (NUMBER, "number", ValueType::Long, false),
        (CHILD, "child", ValueType::Ref, false),
        (MANY, "many", ValueType::Long, true),
        (TAG, "tag", ValueType::Keyword, false),
    ] {
        let mut attribute = Attribute::new(
            id,
            Keyword::new("item", name),
            kind,
            if many {
                Cardinality::Many
            } else {
                Cardinality::One
            },
        );
        attribute.component = id == CHILD;
        schema.install(attribute).unwrap();
    }
    schema
}

fn add(entity: &str, attribute: u32, value: impl Into<TxValue>) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(entity.into()),
        attribute,
        value: value.into(),
    }
}

fn ops() -> Vec<TxOp> {
    vec![
        add("root", TEXT, Value::String("people/alice".into())),
        add("root", NUMBER, Value::Long(42)),
        add("root", TAG, Value::Keyword(Keyword::new("people", "alice"))),
        add(
            "root",
            CHILD,
            TxValue::Entity(EntityRef::Temp("kid".into())),
        ),
        add("kid", TEXT, Value::String("Kid".into())),
        add("root", MANY, Value::Long(1)),
        add("root", MANY, Value::Long(2)),
        add("root", MANY, Value::Long(3)),
    ]
}

fn database() -> (Database, u64) {
    let report = Database::new(schema()).unwrap().with(&ops(), 1).unwrap();
    (report.db_after, report.tempids["root"])
}

fn attribute(id: u32, transform: PullTransform) -> PullAttribute {
    let mut selector = PullAttribute::forward(AttributeName::Id(id));
    selector.transform = Some(transform);
    selector
}

fn field<'a>(value: &'a QueryValue, name: &str) -> Option<&'a QueryValue> {
    let QueryValue::Map(fields) = value else {
        panic!("expected map")
    };
    fields.iter().find_map(|(key, value)| {
        matches!(key, QueryValue::Scalar(Value::Keyword(key)) if key.namespace.as_deref() == Some("item") && key.name == name).then_some(value)
    })
}

#[test]
fn transforms_see_nil_and_defaults_are_only_applied_afterward() {
    let (database, root) = database();
    let calls = Arc::new(AtomicUsize::new(0));
    let seen = calls.clone();
    let mut missing = PullAttribute::forward(AttributeName::Ident(Keyword::new("item", "missing")));
    missing.default = Some(QueryValue::Scalar(Value::Long(99)));
    missing.transform = Some(PullTransform::new("nil-replacement", move |value| {
        assert!(matches!(value, QueryValue::Nil));
        seen.fetch_add(1, Ordering::Relaxed);
        Ok(QueryValue::Scalar(Value::String("from-nil".into())))
    }));
    let mut number = attribute(
        NUMBER,
        PullTransform::new("nil-result", |value| {
            assert_eq!(value, &QueryValue::Scalar(Value::Long(42)));
            Ok(QueryValue::Nil)
        }),
    );
    number.default = Some(QueryValue::Scalar(Value::String(
        "untransformed default".into(),
    )));
    let result = database
        .pull(&PullPattern::attributes(vec![missing, number]), root)
        .unwrap();
    assert_eq!(
        field(&result, "missing"),
        Some(&QueryValue::Scalar(Value::String("from-nil".into())))
    );
    assert_eq!(
        field(&result, "number"),
        Some(&QueryValue::Scalar(Value::String(
            "untransformed default".into()
        )))
    );
    assert_eq!(calls.load(Ordering::Relaxed), 1);

    let filtered = database
        .database_value()
        .filter(|_, datom| datom.attribute != NUMBER);
    let result = filtered
        .pull(
            &PullPattern::attributes(vec![attribute(NUMBER, PullTransform::String)]),
            root,
        )
        .unwrap();
    assert_eq!(
        field(&result, "number"),
        Some(&QueryValue::Scalar(Value::String(String::new())))
    );

    let absent = attribute(NUMBER, PullTransform::new("omit", |_| Ok(QueryValue::Nil)));
    assert!(
        field(
            &database
                .pull(&PullPattern::attributes(vec![absent]), root)
                .unwrap(),
            "number"
        )
        .is_none()
    );
}

#[test]
fn native_builtins_and_whole_collection_nested_alias_transforms() {
    let (database, root) = database();
    for (id, name, transform, expected) in [
        (
            NUMBER,
            "number",
            PullTransform::String,
            Value::String("42".into()),
        ),
        (
            TEXT,
            "text",
            PullTransform::Keyword,
            Value::Keyword(Keyword::new("people", "alice")),
        ),
        (
            TEXT,
            "text",
            PullTransform::Symbol,
            Value::Symbol(Symbol::new("people", "alice")),
        ),
        (
            TAG,
            "tag",
            PullTransform::Name,
            Value::String("alice".into()),
        ),
        (
            TAG,
            "tag",
            PullTransform::Namespace,
            Value::String("people".into()),
        ),
        (
            MANY,
            "many",
            PullTransform::String,
            Value::String("[1 2 3]".into()),
        ),
    ] {
        let result = database
            .pull(
                &PullPattern::attributes(vec![attribute(id, transform)]),
                root,
            )
            .unwrap();
        assert_eq!(field(&result, name), Some(&QueryValue::Scalar(expected)));
    }
    let mut many = attribute(
        MANY,
        PullTransform::new("count", |value| {
            let QueryValue::Collection(values) = value else {
                panic!("whole many collection")
            };
            Ok(QueryValue::Scalar(Value::Long(values.len() as i64)))
        }),
    );
    many.limit = PullLimit::Limit(2);
    let mut child = attribute(
        CHILD,
        PullTransform::new("nested-name", |value| {
            Ok(field(value, "text").unwrap().clone())
        }),
    );
    child.nested = Some(PullNested::Pattern(Box::new(PullPattern::attributes(
        vec![PullAttribute::forward(AttributeName::Id(TEXT))],
    ))));
    child.alias = Some(QueryValue::Scalar(Value::String("child-name".into())));
    let result = database
        .pull(&PullPattern::attributes(vec![many, child]), root)
        .unwrap();
    assert_eq!(
        field(&result, "many"),
        Some(&QueryValue::Scalar(Value::Long(2)))
    );
    let QueryValue::Map(entries) = &result else {
        unreachable!()
    };
    assert!(entries.iter().any(|(key, value)| key
        == &QueryValue::Scalar(Value::String("child-name".into()))
        && value == &QueryValue::Scalar(Value::String("Kid".into()))));
}

fn pull_query(pattern: PullPattern) -> Query {
    let entity = Variable::new("entity").unwrap();
    Query::new(
        FindSpec::Scalar(FindElement::Pull {
            source: "$".into(),
            variable: entity.clone(),
            pattern: Box::new(pattern),
        }),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::Variable(entity),
            Term::Constant(Value::Keyword(Keyword::new("item", "number"))),
            Term::Constant(Value::Long(42)),
        )))],
    )
}

#[test]
fn transform_errors_panics_and_cancellation_do_not_escape_controls() {
    let (database, root) = database();
    let fail = attribute(
        NUMBER,
        PullTransform::new("cancel", |_| {
            Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "test/canceled",
                "canceled by callback",
            ))
        }),
    );
    assert_eq!(
        database
            .pull(&PullPattern::attributes(vec![fail]), root)
            .unwrap_err()
            .code,
        "test/canceled"
    );
    let invalid = attribute(NUMBER, PullTransform::Name);
    assert_eq!(
        database
            .pull(&PullPattern::attributes(vec![invalid]), root)
            .unwrap_err()
            .code,
        "pull/transform-type"
    );
    let panic = attribute(
        NUMBER,
        PullTransform::new("panic", |_| panic!("transform fixture")),
    );
    assert_eq!(
        database
            .pull(&PullPattern::attributes(vec![panic]), root)
            .unwrap_err()
            .code,
        "pull/transform-panicked"
    );

    let control = PullControl::default();
    let cancel = control.cancel.clone();
    let selector = attribute(
        NUMBER,
        PullTransform::new("set-cancel", move |value| {
            cancel.store(true, Ordering::Relaxed);
            Ok(value.clone())
        }),
    );
    assert_eq!(
        database
            .pull_with_control(&PullPattern::attributes(vec![selector]), root, &control)
            .unwrap_err()
            .code,
        "pull/canceled"
    );

    let control = QueryControl::default();
    let cancel = control.cancel.clone();
    let selector = attribute(
        NUMBER,
        PullTransform::new("set-query-cancel", move |value| {
            cancel.store(true, Ordering::Relaxed);
            Ok(value.clone())
        }),
    );
    let query = pull_query(PullPattern::attributes(vec![selector]));
    assert_eq!(
        database.query(&query, &[], &control).unwrap_err().code,
        "query/canceled"
    );
}

#[test]
fn explicit_pattern_ownership_and_touched_component_caches_are_stack_safe() {
    let length = 1_025;
    let mut operations = Vec::new();
    for index in 0..length {
        operations.push(add(
            &format!("node-{index}"),
            NUMBER,
            Value::Long(index as i64),
        ));
        operations.push(add(
            &format!("node-{index}"),
            CHILD,
            TxValue::Entity(EntityRef::Temp(format!("node-{}", (index + 1) % length))),
        ));
    }
    let report = Database::new(schema())
        .unwrap()
        .with(&operations, 1)
        .unwrap();
    let database = report.db_after.database_value();
    let first = report.tempids["node-0"];
    std::thread::Builder::new().stack_size(256 * 1_024).spawn(move || {
        let mut pattern = PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(NUMBER))]);
        for _ in 0..length {
            let mut child = PullAttribute::forward(AttributeName::Id(CHILD));
            child.nested = Some(PullNested::Pattern(Box::new(pattern)));
            pattern = PullPattern::attributes(vec![child]);
        }
        let copy = pattern.clone();
        assert!(pattern == copy);
        let result = database.pull(&pattern, first).unwrap();
        let mut cursor = &result;
        for _ in 0..length {
            cursor = field(cursor, "child").expect("explicit nesting keeps following the cycle");
        }
        assert_eq!(field(cursor, "number"), Some(&QueryValue::Scalar(Value::Long(0))));
        drop(result);
        drop(copy);
        drop(pattern);

        let entity = database.entity(first).unwrap().unwrap().touch().unwrap();
        let retained = entity.clone();
        let mut cursor = entity.clone();
        for index in 0..length {
            assert!(matches!(cursor.get(NUMBER).unwrap(), Some(EntityValue::Scalar(Value::Long(value))) if value == index as i64));
            let Some(EntityValue::Entity(next)) = cursor.get(CHILD).unwrap() else { panic!("component") };
            cursor = next;
        }
        assert_eq!(cursor.id(), first);
        drop(cursor);
        drop(entity);
        let other = retained.clone();
        let barrier = Arc::new(std::sync::Barrier::new(2));
        let handles = [retained, other].into_iter().map(|entity| {
            let barrier = barrier.clone();
            std::thread::Builder::new().stack_size(128 * 1_024).spawn(move || {
                barrier.wait();
                drop(entity);
            }).unwrap()
        }).collect::<Vec<_>>();
        for handle in handles {
            handle.join().unwrap();
        }
    }).unwrap().join().unwrap();
}

#[test]
fn native_postgres_transforms_keep_exact_values_and_peer_local_execution() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return;
    };
    let id = format!(
        "pull-transform-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    );
    atomic_core::PostgresMigrator::connect(&postgres)
        .unwrap()
        .migrate()
        .unwrap();
    atomic_core::PostgresStore::connect(&postgres)
        .unwrap()
        .create_database(&id, schema())
        .unwrap();
    let writer = common::start_service(&postgres, &id);
    let report = writer
        .client()
        .transact(
            TransactionRequest::new("initial", ops()).with_tx_instant(1),
            Duration::from_secs(30),
        )
        .unwrap();
    let root = report.tempids["root"];
    let peer = atomic_core::Connection::connect(&postgres, &id, 16).unwrap();
    let captured = peer.db();
    writer
        .client()
        .transact(
            TransactionRequest::new(
                "update",
                vec![TxOp::Add {
                    entity: EntityRef::Id(root),
                    attribute: NUMBER,
                    value: Value::Long(43).into(),
                }],
            )
            .with_tx_instant(2),
            Duration::from_secs(30),
        )
        .unwrap();
    writer.shutdown();
    let selector = attribute(NUMBER, PullTransform::String);
    let pattern = PullPattern::attributes(vec![selector]);
    assert_eq!(
        field(&captured.pull(&pattern, root).unwrap(), "number"),
        Some(&QueryValue::Scalar(Value::String("42".into())))
    );
    let query = pull_query(pattern);
    assert_eq!(
        captured
            .query(&query, &[], &QueryControl::default())
            .unwrap()
            .result,
        atomic_core::QueryResult::Scalar(Some(QueryValue::Map(vec![(
            QueryValue::Scalar(Value::Keyword(Keyword::new("item", "number"))),
            QueryValue::Scalar(Value::String("42".into()))
        )])))
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
}
