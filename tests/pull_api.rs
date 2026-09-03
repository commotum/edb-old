use atomic_core::{
    Attribute, AttributeName, Cardinality, Clause, DataPattern, Database, Entity, EntityRef,
    EntityValue, FindElement, FindSpec, Keyword, PullAttribute, PullControl, PullDirection,
    PullLimit, PullNested, PullPattern, Query, QueryControl, QueryResult, QueryValue, Schema, Term,
    TxOp, TxValue, Unique, Value, ValueType, Variable,
};
use std::sync::Arc;

const NAME: u32 = 1_000;
const AGE: u32 = 1_001;
const FRIEND: u32 = 1_002;
const CHILDREN: u32 = 1_003;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                NAME,
                Keyword::new("person", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            AGE,
            Keyword::new("person", "age"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    let mut friend = Attribute::new(
        FRIEND,
        Keyword::new("person", "friend"),
        ValueType::Ref,
        Cardinality::Many,
    );
    friend.indexed = true;
    schema.install(friend).unwrap();
    let mut children = Attribute::new(
        CHILDREN,
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
                add(
                    "alice",
                    NAME,
                    TxValue::Scalar(Value::String("Alice".into())),
                ),
                add("alice", AGE, TxValue::Scalar(Value::Long(40))),
                add(
                    "alice",
                    FRIEND,
                    TxValue::Entity(EntityRef::Temp("bob".into())),
                ),
                add(
                    "alice",
                    CHILDREN,
                    TxValue::Entity(EntityRef::Temp("cara".into())),
                ),
                add("bob", NAME, TxValue::Scalar(Value::String("Bob".into()))),
                add(
                    "bob",
                    FRIEND,
                    TxValue::Entity(EntityRef::Temp("alice".into())),
                ),
                add("cara", NAME, TxValue::Scalar(Value::String("Cara".into()))),
                add("dave", NAME, TxValue::Scalar(Value::String("Dave".into()))),
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

fn keyword_key(keyword: Keyword) -> QueryValue {
    QueryValue::Scalar(Value::Keyword(keyword))
}

fn map_field<'a>(value: &'a QueryValue, key: &QueryValue) -> Option<&'a QueryValue> {
    let QueryValue::Map(fields) = value else {
        panic!("expected map")
    };
    fields
        .iter()
        .find_map(|(candidate, value)| candidate.canonical_cmp(key).is_eq().then_some(value))
}

fn field<'a>(value: &'a QueryValue, key: &Keyword) -> Option<&'a QueryValue> {
    map_field(value, &keyword_key(key.clone()))
}

#[test]
fn pull_defaults_aliases_nested_refs_reverse_and_limits() {
    let (db, ids) = database();
    let mut name = PullAttribute::forward(AttributeName::Id(NAME));
    name.alias = Some(keyword_key(Keyword::new("display", "name")));
    let mut age = PullAttribute::forward(AttributeName::Id(AGE));
    age.default = Some(QueryValue::Scalar(Value::String("N/A".into())));
    let mut friend = PullAttribute::forward(AttributeName::Id(FRIEND));
    friend.limit = PullLimit::Limit(1);
    friend.nested = Some(PullNested::Pattern(Box::new(PullPattern::attributes(
        vec![PullAttribute::forward(AttributeName::Id(NAME))],
    ))));
    let reverse = PullAttribute::reverse(AttributeName::Id(FRIEND));
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
            keyword_key(Keyword::new("db", "id")),
            QueryValue::Scalar(Value::Ref(ids[0]))
        )])])
    );
}

#[test]
fn pull_aliases_accept_string_numeric_and_collection_keys() {
    let (db, ids) = database();
    let string_key = QueryValue::Scalar(Value::String("display name".into()));
    let numeric_key = QueryValue::Scalar(Value::Long(7));
    let collection_key = QueryValue::Collection(vec![
        QueryValue::Scalar(Value::Keyword(Keyword::new("display", "identity"))),
        QueryValue::Scalar(Value::Long(1)),
    ]);

    let mut name = PullAttribute::forward(AttributeName::Id(NAME));
    name.alias = Some(string_key.clone());
    let mut age = PullAttribute::forward(AttributeName::Id(AGE));
    age.alias = Some(numeric_key.clone());
    let mut id = PullAttribute::forward(AttributeName::Ident(Keyword::new("db", "id")));
    id.alias = Some(collection_key.clone());

    let pulled = db
        .pull(&PullPattern::attributes(vec![name, age, id]), ids[0])
        .unwrap();
    assert_eq!(
        map_field(&pulled, &string_key),
        Some(&QueryValue::Scalar(Value::String("Alice".into())))
    );
    assert_eq!(
        map_field(&pulled, &numeric_key),
        Some(&QueryValue::Scalar(Value::Long(40)))
    );
    assert_eq!(
        map_field(&pulled, &collection_key),
        Some(&QueryValue::Scalar(Value::Ref(ids[0])))
    );

    let QueryValue::Map(entries) = pulled else {
        unreachable!("pull always returns a map")
    };
    assert!(
        entries
            .windows(2)
            .all(|entries| entries[0].0.canonical_cmp(&entries[1].0).is_lt())
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

    let mut recurse = PullAttribute::forward(AttributeName::Id(FRIEND));
    recurse.nested = Some(PullNested::Recursion(None));
    let recursive = PullPattern::attributes(vec![
        PullAttribute::forward(AttributeName::Id(NAME)),
        recurse,
    ]);
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
            keyword_key(Keyword::new("db", "id")),
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
                attribute: AGE,
                value: TxValue::Scalar(Value::Long(41)),
            }],
            2_000,
        )
        .unwrap()
        .db_after;
    assert!(matches!(
        entity.get(AGE).unwrap(),
        Some(EntityValue::Scalar(Value::Long(40)))
    ));
    assert_eq!(db2.values(ids[0], AGE), vec![&Value::Long(41)]);
    assert_eq!(entity.reverse(FRIEND).unwrap()[0].id(), ids[1]);
}

#[test]
fn pull_explicit_id_unknown_attributes_and_nested_empty_results() {
    let (db, ids) = database();
    let db_id = Keyword::new("db", "id");
    let missing = Keyword::new("person", "penguins");

    let pulled = db
        .pull(
            &PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Ident(
                db_id.clone(),
            ))]),
            ids[0],
        )
        .unwrap();
    assert_eq!(
        field(&pulled, &db_id),
        Some(&QueryValue::Scalar(Value::Ref(ids[0])))
    );

    let unknown = PullAttribute::forward(AttributeName::Ident(missing.clone()));
    assert_eq!(
        db.pull(&PullPattern::attributes(vec![unknown.clone()]), ids[0])
            .unwrap(),
        QueryValue::Map(Vec::new())
    );
    let mut unknown_with_default = unknown.clone();
    unknown_with_default.default = Some(QueryValue::Scalar(Value::String("none".into())));
    assert_eq!(
        field(
            &db.pull(&PullPattern::attributes(vec![unknown_with_default]), ids[0])
                .unwrap(),
            &missing,
        ),
        Some(&QueryValue::Scalar(Value::String("none".into())))
    );

    let mut friends = PullAttribute::forward(AttributeName::Id(FRIEND));
    friends.nested = Some(PullNested::Pattern(Box::new(PullPattern::attributes(
        vec![unknown],
    ))));
    let pulled = db
        .pull(&PullPattern::attributes(vec![friends]), ids[0])
        .unwrap();
    assert_eq!(
        field(&pulled, &Keyword::new("person", "friend")),
        Some(&QueryValue::Collection(Vec::new()))
    );
}

#[test]
fn pull_rejects_duplicate_selectors_and_non_positive_limits() {
    let (db, ids) = database();
    let duplicate = PullPattern::attributes(vec![
        PullAttribute::forward(AttributeName::Id(NAME)),
        PullAttribute::forward(AttributeName::Ident(Keyword::new("person", "name"))),
    ]);
    assert_eq!(
        db.pull(&duplicate, ids[0]).unwrap_err().code,
        "pull/duplicate-attribute"
    );

    let mut zero_limit = PullAttribute::forward(AttributeName::Id(FRIEND));
    zero_limit.limit = PullLimit::Limit(0);
    assert_eq!(
        db.pull(&PullPattern::attributes(vec![zero_limit]), ids[0])
            .unwrap_err()
            .code,
        "db.error/invalid-limit"
    );

    let mut empty_input_limit = PullAttribute::forward(AttributeName::Id(FRIEND));
    empty_input_limit.limit = PullLimit::Limit(0);
    assert_eq!(
        db.pull_many::<Vec<u64>, u64>(
            &PullPattern::attributes(vec![empty_input_limit]),
            Vec::new()
        )
        .unwrap_err()
        .code,
        "db.error/invalid-limit"
    );

    let mut zero_depth = PullAttribute::forward(AttributeName::Id(FRIEND));
    zero_depth.nested = Some(PullNested::Recursion(Some(0)));
    assert_eq!(
        db.pull(&PullPattern::attributes(vec![zero_depth]), ids[0])
            .unwrap_err()
            .code,
        "db.error/invalid-recur-limit"
    );
}

#[test]
fn nested_paths_and_recursive_selectors_have_local_cycle_and_depth_state() {
    let (db, ids) = database();

    // Ordinary nesting may revisit an ancestor. It is not recursive traversal
    // and therefore must not be collapsed to only :db/id.
    let mut back_to_alice = PullAttribute::forward(AttributeName::Id(FRIEND));
    back_to_alice.nested = Some(PullNested::Pattern(Box::new(PullPattern::attributes(
        vec![PullAttribute::forward(AttributeName::Id(NAME))],
    ))));
    let mut through_bob = PullAttribute::forward(AttributeName::Id(FRIEND));
    through_bob.nested = Some(PullNested::Pattern(Box::new(PullPattern::attributes(
        vec![back_to_alice],
    ))));
    let pulled = db
        .pull(&PullPattern::attributes(vec![through_bob]), ids[0])
        .unwrap();
    let QueryValue::Collection(bobs) = field(&pulled, &Keyword::new("person", "friend")).unwrap()
    else {
        panic!("friends must be a collection")
    };
    let QueryValue::Collection(alices) =
        field(&bobs[0], &Keyword::new("person", "friend")).unwrap()
    else {
        panic!("friends must be a collection")
    };
    assert_eq!(
        field(&alices[0], &Keyword::new("person", "name")),
        Some(&QueryValue::Scalar(Value::String("Alice".into())))
    );

    // A recursion limit is relative to its selector, not to unrelated outer
    // nesting. One friend edge below :person/children still expands Dave.
    let db = db
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Id(ids[2]),
                    attribute: FRIEND,
                    value: TxValue::Entity(EntityRef::Id(ids[3])),
                },
                TxOp::Add {
                    entity: EntityRef::Id(ids[3]),
                    attribute: FRIEND,
                    value: TxValue::Entity(EntityRef::Id(ids[0])),
                },
            ],
            2_000,
        )
        .unwrap()
        .db_after;
    let mut recurse_friend = PullAttribute::forward(AttributeName::Id(FRIEND));
    recurse_friend.nested = Some(PullNested::Recursion(Some(1)));
    let child_pattern = PullPattern::attributes(vec![
        PullAttribute::forward(AttributeName::Id(NAME)),
        recurse_friend,
    ]);
    let mut children = PullAttribute::forward(AttributeName::Id(CHILDREN));
    children.nested = Some(PullNested::Pattern(Box::new(child_pattern)));
    let pulled = db
        .pull(&PullPattern::attributes(vec![children]), ids[0])
        .unwrap();
    let QueryValue::Collection(children) =
        field(&pulled, &Keyword::new("person", "children")).unwrap()
    else {
        panic!("children must be a collection")
    };
    let QueryValue::Collection(friends) =
        field(&children[0], &Keyword::new("person", "friend")).unwrap()
    else {
        panic!("friends must be a collection")
    };
    assert_eq!(
        field(&friends[0], &Keyword::new("person", "name")),
        Some(&QueryValue::Scalar(Value::String("Dave".into())))
    );
    let QueryValue::Collection(next) =
        field(&friends[0], &Keyword::new("person", "friend")).unwrap()
    else {
        panic!("friends must be a collection")
    };
    assert_eq!(
        next[0],
        QueryValue::Map(vec![(
            keyword_key(Keyword::new("db", "id")),
            QueryValue::Scalar(Value::Ref(ids[0]))
        )])
    );
}

#[test]
fn eager_entity_navigation_returns_entities_and_keeps_one_snapshot() {
    let (db, ids) = database();
    let entity = db.entity(ids[0]).unwrap().unwrap();

    assert!(matches!(
        entity.get_named(&AttributeName::Ident(Keyword::new("db", "id")))
            .unwrap(),
        Some(EntityValue::Scalar(Value::Ref(id))) if id == ids[0]
    ));
    assert!(
        entity
            .get_named(&AttributeName::Ident(Keyword::new("person", "missing")))
            .unwrap()
            .is_none()
    );
    let Some(EntityValue::Collection(friends)) = entity.get(FRIEND).unwrap() else {
        panic!("cardinality-many ref must return an entity collection")
    };
    let EntityValue::Entity(bob) = &friends[0] else {
        panic!("ref values must navigate as entities")
    };
    assert_eq!(bob.id(), ids[1]);
    assert!(matches!(
        bob.get(NAME).unwrap(),
        Some(EntityValue::Scalar(Value::String(name))) if name == "Bob"
    ));

    let Some(EntityValue::Collection(incoming)) = bob
        .get_direction(&PullDirection::Reverse(AttributeName::Id(FRIEND)))
        .unwrap()
    else {
        panic!("non-component reverse refs must return a collection")
    };
    assert!(matches!(&incoming[0], EntityValue::Entity(alice) if alice.id() == ids[0]));
    assert!(entity.keys().unwrap().contains(&Keyword::new("db", "id")));
    entity.touch().unwrap();
}

#[test]
fn pull_and_entities_retain_the_exact_database_value_and_reject_history() {
    let (before, ids) = database();
    let after = before
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(ids[0]),
                attribute: AGE,
                value: TxValue::Scalar(Value::Long(41)),
            }],
            2_000,
        )
        .unwrap()
        .db_after;
    let age_pattern = PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(AGE))]);

    let old_value = before.database_value();
    let old_entity = old_value.entity(ids[0]).unwrap().unwrap();
    assert!(matches!(
        old_entity.get(AGE).unwrap(),
        Some(EntityValue::Scalar(Value::Long(40)))
    ));
    assert_eq!(
        field(
            &old_value.pull(&age_pattern, ids[0]).unwrap(),
            &Keyword::new("person", "age")
        ),
        Some(&QueryValue::Scalar(Value::Long(40)))
    );

    let as_of_old = after.database_value().as_of(before.basis_t());
    let as_of_entity = as_of_old.entity(ids[0]).unwrap().unwrap();
    assert!(matches!(
        as_of_entity.get(AGE).unwrap(),
        Some(EntityValue::Scalar(Value::Long(40)))
    ));
    assert_eq!(
        field(
            &as_of_old.pull(&age_pattern, ids[0]).unwrap(),
            &Keyword::new("person", "age")
        ),
        Some(&QueryValue::Scalar(Value::Long(40)))
    );

    let filtered = after
        .database_value()
        .filter(|_, datom| datom.attribute != AGE);
    assert!(
        filtered
            .entity(ids[0])
            .unwrap()
            .unwrap()
            .get(AGE)
            .unwrap()
            .is_none()
    );
    assert_eq!(
        filtered.pull(&age_pattern, ids[0]).unwrap(),
        QueryValue::Map(Vec::new())
    );

    let history = after.database_value().history();
    assert_eq!(
        history.entity(ids[0]).unwrap_err().code,
        "database/history-not-point-in-time"
    );
    assert_eq!(
        history.pull(&age_pattern, ids[0]).unwrap_err().code,
        "database/history-not-point-in-time"
    );
    assert_eq!(
        history
            .pull_many::<Vec<u64>, u64>(&age_pattern, Vec::new())
            .unwrap_err()
            .code,
        "database/history-not-point-in-time"
    );
    assert_eq!(
        Entity::from_database_value(history, ids[0])
            .unwrap_err()
            .code,
        "database/history-not-point-in-time"
    );
}

#[test]
fn query_pull_expression_projects_from_the_same_snapshot() {
    let (db, _) = database();
    let entity = Variable::new("entity").unwrap();
    let query = Query::new(
        FindSpec::Scalar(FindElement::Pull {
            variable: entity.clone(),
            pattern: Box::new(PullPattern::attributes(vec![PullAttribute::forward(
                AttributeName::Id(NAME),
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
            keyword_key(Keyword::new("person", "name")),
            QueryValue::Scalar(Value::String("Alice".into()))
        )])))
    );
}

#[test]
fn pull_entity_and_cancellation_limits_bound_recursive_work() {
    let (db, ids) = database();
    let mut recurse = PullAttribute::forward(AttributeName::Id(FRIEND));
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
