use atomic_core::{
    Attribute, AttributeName, Cardinality, Clause, DataPattern, Database, Entity, EntityRef,
    FindElement, FindSpec, Keyword, PullAttribute, PullControl, PullLimit, PullNested, PullPattern,
    Query, QueryControl, QueryResult, QueryValue, Schema, Term, TxOp, TxValue, Unique, Value,
    ValueType, Variable,
};
use std::sync::Arc;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1,
            Keyword::new("db", "txInstant"),
            ValueType::Instant,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                10,
                Keyword::new("person", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            11,
            Keyword::new("person", "age"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let mut friend = Attribute::new(
        12,
        Keyword::new("person", "friend"),
        ValueType::Ref,
        Cardinality::Many,
    );
    friend.indexed = true;
    schema.install(friend).unwrap();
    let mut children = Attribute::new(
        13,
        Keyword::new("person", "children"),
        ValueType::Ref,
        Cardinality::Many,
    );
    children.indexed = true;
    children.component = true;
    schema.install(children).unwrap();
    schema
}

fn database() -> (Database, [u64; 4]) {
    let db = Database::new(schema()).unwrap();
    let report = db
        .with(
            &[
                add("alice", 10, TxValue::Scalar(Value::String("Alice".into()))),
                add("alice", 11, TxValue::Scalar(Value::Long(40))),
                add("alice", 12, TxValue::Entity(EntityRef::Temp("bob".into()))),
                add("alice", 13, TxValue::Entity(EntityRef::Temp("cara".into()))),
                add("bob", 10, TxValue::Scalar(Value::String("Bob".into()))),
                add("bob", 12, TxValue::Entity(EntityRef::Temp("alice".into()))),
                add("cara", 10, TxValue::Scalar(Value::String("Cara".into()))),
                add("dave", 10, TxValue::Scalar(Value::String("Dave".into()))),
            ],
            1_000,
        )
        .unwrap();
    let ids = [
        report.tempids["alice"],
        report.tempids["bob"],
        report.tempids["cara"],
        report.tempids["dave"],
    ];
    (report.db_after, ids)
}

fn add(entity: &str, attribute: u32, value: TxValue) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(entity.into()),
        attribute,
        value,
    }
}

fn field<'a>(value: &'a QueryValue, key: &Keyword) -> Option<&'a QueryValue> {
    let QueryValue::Map(fields) = value else {
        panic!("expected map")
    };
    fields
        .iter()
        .find_map(|(candidate, value)| (candidate == key).then_some(value))
}

#[test]
fn pull_defaults_aliases_nested_refs_reverse_and_limits() {
    let (db, ids) = database();
    let mut name = PullAttribute::forward(AttributeName::Id(10));
    name.alias = Some(Keyword::new("display", "name"));
    let mut age = PullAttribute::forward(AttributeName::Id(11));
    age.default = Some(QueryValue::Scalar(Value::String("N/A".into())));
    let mut friend = PullAttribute::forward(AttributeName::Id(12));
    friend.limit = PullLimit::Limit(1);
    friend.nested = Some(PullNested::Pattern(Box::new(PullPattern::attributes(
        vec![PullAttribute::forward(AttributeName::Id(10))],
    ))));
    let reverse = PullAttribute::reverse(AttributeName::Id(12));
    let pattern = PullPattern::attributes(vec![name, age, friend, reverse]);
    let pulled = db.pull(&pattern, ids[1]).unwrap();
    assert_eq!(
        field(&pulled, &Keyword::new("display", "name")),
        Some(&QueryValue::Scalar(Value::String("Bob".into())))
    );
    assert_eq!(
        field(&pulled, &Keyword::new("person", "age")),
        Some(&QueryValue::Scalar(Value::String("N/A".into())))
    );
    let reverse = field(&pulled, &Keyword::new("person", "_friend")).unwrap();
    assert_eq!(
        reverse,
        &QueryValue::Collection(vec![QueryValue::Map(vec![(
            Keyword::new("db", "id"),
            QueryValue::Scalar(Value::Ref(ids[0]))
        )])])
    );
}

#[test]
fn wildcard_expands_components_and_recursion_is_cycle_safe() {
    let (db, ids) = database();
    let wildcard = db.pull(&PullPattern::wildcard(), ids[0]).unwrap();
    let children = field(&wildcard, &Keyword::new("person", "children")).unwrap();
    let QueryValue::Collection(children) = children else {
        panic!("children should be cardinality many")
    };
    assert_eq!(
        field(&children[0], &Keyword::new("person", "name")),
        Some(&QueryValue::Scalar(Value::String("Cara".into())))
    );

    let mut recurse = PullAttribute::forward(AttributeName::Id(12));
    recurse.nested = Some(PullNested::Recursion(None));
    let recursive =
        PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(10)), recurse]);
    let result = db.pull(&recursive, ids[0]).unwrap();
    let alice_again = match field(&result, &Keyword::new("person", "friend")).unwrap() {
        QueryValue::Collection(friends) => {
            match field(&friends[0], &Keyword::new("person", "friend")).unwrap() {
                QueryValue::Collection(friends) => &friends[0],
                other => panic!("expected nested collection, got {other:?}"),
            }
        }
        other => panic!("expected friend collection, got {other:?}"),
    };
    assert_eq!(
        alice_again,
        &QueryValue::Map(vec![(
            Keyword::new("db", "id"),
            QueryValue::Scalar(Value::Ref(ids[0]))
        )])
    );
}

#[test]
fn entity_values_retain_their_immutable_database_snapshot() {
    let (db1, ids) = database();
    let entity = Entity::new(Arc::new(db1.clone()), ids[0]);
    let db2 = db1
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(ids[0]),
                attribute: 11,
                value: TxValue::Scalar(Value::Long(41)),
            }],
            2_000,
        )
        .unwrap()
        .db_after;
    assert_eq!(
        entity.get(11).unwrap(),
        Some(QueryValue::Scalar(Value::Long(40)))
    );
    assert_eq!(db2.values(ids[0], 11), vec![&Value::Long(41)]);
    assert_eq!(entity.reverse(12).unwrap()[0].id(), ids[1]);
}

#[test]
fn query_pull_expression_projects_from_the_same_snapshot() {
    let (db, _) = database();
    let entity = Variable::new("entity").unwrap();
    let query = Query::new(
        FindSpec::Scalar(FindElement::Pull {
            variable: entity.clone(),
            pattern: Box::new(PullPattern::attributes(vec![PullAttribute::forward(
                AttributeName::Id(10),
            )])),
        }),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::Variable(entity),
            Term::Constant(Value::Keyword(Keyword::new("person", "name"))),
            Term::Constant(Value::String("Alice".into())),
        )))],
    );
    assert_eq!(
        db.query(&query, &[], &QueryControl::default())
            .unwrap()
            .result,
        QueryResult::Scalar(Some(QueryValue::Map(vec![(
            Keyword::new("person", "name"),
            QueryValue::Scalar(Value::String("Alice".into()))
        )])))
    );
}

#[test]
fn pull_entity_and_cancellation_limits_bound_recursive_work() {
    let (db, ids) = database();
    let mut recurse = PullAttribute::forward(AttributeName::Id(12));
    recurse.nested = Some(PullNested::Recursion(None));
    let pattern = PullPattern::attributes(vec![recurse]);
    let control = PullControl {
        max_entities: 1,
        ..PullControl::default()
    };
    assert_eq!(
        db.pull_with_control(&pattern, ids[0], &control)
            .unwrap_err()
            .code,
        "pull/entity-limit"
    );
    let control = PullControl::default();
    control
        .cancel
        .store(true, std::sync::atomic::Ordering::Relaxed);
    assert_eq!(
        db.pull_with_control(&pattern, ids[0], &control)
            .unwrap_err()
            .code,
        "pull/canceled"
    );
}
