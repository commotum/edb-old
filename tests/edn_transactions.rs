mod common;

use atomic_core::edn_transaction::read_edn_transaction;
use atomic_core::{
    Attribute, AttributeRef, Cardinality, Database, DatabaseValue, EntityMap, EntityRef,
    IndexOrder, Keyword, MapValue, Schema, TransactionRequest, TupleSpec, TxForm, TxFunctions,
    TxOp, Unique, Value, ValueType, submission_request_digest,
};
use std::time::Duration;

const NAME: u32 = 1_000;
const EMAIL: u32 = 1_001;
const TAGS: u32 = 1_002;
const CHILD: u32 = 1_003;
const FRIEND: u32 = 1_004;
const PAIR: u32 = 1_005;
const PAIRS: u32 = 1_006;
const COUNT: u32 = 1_007;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            NAME,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ),
        Attribute::new(
            EMAIL,
            Keyword::new("person", "email"),
            ValueType::String,
            Cardinality::One,
        )
        .unique(Unique::Identity),
        Attribute::new(
            TAGS,
            Keyword::new("person", "tags"),
            ValueType::String,
            Cardinality::Many,
        ),
        Attribute::new(
            CHILD,
            Keyword::new("person", "children"),
            ValueType::Ref,
            Cardinality::Many,
        )
        .component(),
        Attribute::new(
            FRIEND,
            Keyword::new("person", "friend"),
            ValueType::Ref,
            Cardinality::One,
        ),
        Attribute::new(
            PAIR,
            Keyword::new("person", "pair"),
            ValueType::Tuple,
            Cardinality::One,
        )
        .tuple(TupleSpec::Heterogeneous(vec![
            ValueType::Ref,
            ValueType::Long,
        ]))
        .unique(Unique::Identity),
        Attribute::new(
            PAIRS,
            Keyword::new("person", "pairs"),
            ValueType::Tuple,
            Cardinality::Many,
        )
        .tuple(TupleSpec::Homogeneous(ValueType::Ref)),
        Attribute::new(
            COUNT,
            Keyword::new("person", "count"),
            ValueType::Long,
            Cardinality::One,
        ),
    ] {
        schema.install(attribute).unwrap();
    }
    schema
}

fn checked_preview(before: &Database, text: &str, instant: i64) -> atomic_core::TxReport {
    let eager = before.with_edn(text, instant).unwrap();
    let native = before.database_value().with_edn(text, instant).unwrap();
    assert_eq!(eager.tempids, native.tempids);
    assert_eq!(eager.tx_data, native.tx_data);
    common::assert_same_information(&eager.db_after, &native.db_after);
    eager
}

fn values(db: &DatabaseValue, entity: u64, attribute: u32) -> Vec<Value> {
    db.values(entity, attribute).unwrap()
}

#[test]
fn schema_maps_use_the_existing_system_allocator_and_db_before_rules() {
    let empty = Database::bootstrap().unwrap();
    let text = r#"[{:db/ident :person/name :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}
                  {:db/id "email" :db/ident :person/email :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}]"#;
    let schema = checked_preview(&empty, text, 10);
    let name = schema
        .db_after
        .entid(&Keyword::new("person", "name"))
        .unwrap();
    assert_eq!(
        atomic_core::eid_to_part(name).unwrap(),
        atomic_core::DB_PARTITION
    );
    assert!(u32::try_from(name).is_ok());
    let alice = checked_preview(
        &schema.db_after,
        r#"[{:person/name "Alice" :person/email "alice@example.com"}]"#,
        20,
    );
    let email = schema
        .db_after
        .entid(&Keyword::new("person", "email"))
        .unwrap() as u32;
    let alice_id = alice
        .db_after
        .database_value()
        .lookup(email, &Value::String("alice@example.com".into()))
        .unwrap()
        .unwrap();
    assert_eq!(
        alice.db_after.values(alice_id, name as u32),
        vec![&Value::String("Alice".into())]
    );
    assert_eq!(empty.basis_t(), 0);
    assert!(
        empty
            .with_edn(
                &(text.trim_end_matches(']').to_owned() + " {:person/name \"too-early\"}]"),
                10
            )
            .is_err()
    );

    // Existing typed EntityMap has the same allocation behavior; EDN does not
    // hide a parallel schema installer or add a new partition directive.
    let typed = TxForm::EntityMap(EntityMap {
        id: None,
        attributes: vec![
            (
                AttributeRef::Id(atomic_core::DB_IDENT as u32),
                MapValue::Value(Value::Keyword(Keyword::new("test", "value")).into()),
            ),
            (
                AttributeRef::Id(atomic_core::DB_VALUE_TYPE as u32),
                MapValue::Value(atomic_core::TxValue::Entity(EntityRef::Ident(
                    Keyword::new("db.type", "long"),
                ))),
            ),
            (
                AttributeRef::Id(atomic_core::DB_CARDINALITY as u32),
                MapValue::Value(atomic_core::TxValue::Entity(EntityRef::Ident(
                    Keyword::new("db.cardinality", "one"),
                ))),
            ),
        ],
    });
    let report = empty.with_forms(&[typed], &TxFunctions::new(), 10).unwrap();
    assert!(
        report
            .db_after
            .schema()
            .attribute(
                report
                    .db_after
                    .entid(&Keyword::new("test", "value"))
                    .unwrap() as u32
            )
            .is_ok()
    );
}

#[test]
fn alice_maps_match_typed_facts_and_upsert_does_not_erase_omitted_attributes() {
    let before = Database::new(schema()).unwrap();
    let text = r#"[{:db/id "alice" :person/name "Alice" :person/email "alice@example.com"
                   :person/tags #{"rust" "data"}}]"#;
    let edn = checked_preview(&before, text, 10);
    let typed = before
        .with_forms(
            &[TxForm::EntityMap(EntityMap {
                id: Some(EntityRef::Temp("alice".into())),
                attributes: vec![
                    (
                        AttributeRef::Id(NAME),
                        MapValue::Value(Value::String("Alice".into()).into()),
                    ),
                    (
                        AttributeRef::Id(EMAIL),
                        MapValue::Value(Value::String("alice@example.com".into()).into()),
                    ),
                    (
                        AttributeRef::Id(TAGS),
                        MapValue::Many(vec![
                            MapValue::Value(Value::String("rust".into()).into()),
                            MapValue::Value(Value::String("data".into()).into()),
                        ]),
                    ),
                ],
            })],
            &TxFunctions::new(),
            10,
        )
        .unwrap();
    assert_eq!(edn.tx_data, typed.tx_data);
    assert_eq!(edn.tempids, typed.tempids);
    let alice = edn.tempids["alice"];
    let updated = checked_preview(
        &edn.db_after,
        r#"[{:person/email "alice@example.com" :person/tags ["systems"]}]"#,
        20,
    );
    assert_eq!(
        updated.db_after.values(alice, NAME),
        vec![&Value::String("Alice".into())]
    );
    assert_eq!(updated.db_after.values(alice, TAGS).len(), 3);
    assert!(before.values(alice, NAME).is_empty());
}

#[test]
fn nested_reverse_and_anonymous_identities_share_the_typed_normalizer() {
    let before = Database::new(schema()).unwrap();
    let report = checked_preview(
        &before,
        r#"[
      {:db/id "__map/00000000000000000000" :person/name "explicit"}
      {:db/id nil :person/name "anonymous"}
      {:db/id "parent" :person/name "Parent" :person/children [{:person/name "Child"}]}
      {:db/id "friend" :person/email "friend@example.com" :person/name "Friend"}
      {:person/name "Owner" :person/friend "friend"}
      {:db/id "reverse-target" :person/name "Target" :person/_friend [{:person/email "owner@example.com"}]}]"#,
        10,
    );
    let native = report.db_after.database_value();
    let explicit = report.tempids["__map/00000000000000000000"];
    assert_eq!(
        values(&native, explicit, NAME),
        vec![Value::String("explicit".into())]
    );
    assert_eq!(
        native
            .datoms_with_prefix(&atomic_core::IndexPrefix::Aevt {
                attribute: NAME,
                entity: None,
                value: None
            })
            .unwrap()
            .len(),
        7
    );
    let parent = report.tempids["parent"];
    let children = values(&native, parent, CHILD);
    let [Value::Ref(child)] = children.as_slice() else {
        panic!("one child")
    };
    assert_eq!(
        values(&native, *child, NAME),
        vec![Value::String("Child".into())]
    );
    let owner = native
        .lookup(EMAIL, &Value::String("owner@example.com".into()))
        .unwrap()
        .unwrap();
    assert_eq!(
        values(&native, owner, FRIEND),
        vec![Value::Ref(report.tempids["reverse-target"])]
    );
    let before_facts = before.datoms(atomic_core::View::History, IndexOrder::Eavt);
    assert!(
        before
            .with_edn(r#"[{:person/friend {:person/name "unowned"}}]"#, 10)
            .is_err()
    );
    assert_eq!(
        before.datoms(atomic_core::View::History, IndexOrder::Eavt),
        before_facts
    );
}

#[test]
fn schema_disambiguates_tuples_lookups_and_many_reference_collections() {
    let before = Database::new(schema()).unwrap();
    let created = checked_preview(
        &before,
        r#"[
      {:db/id "a" :person/email "a@example.com"}
      {:db/id "b" :person/email "b@example.com"}
      {:db/id "owner" :person/pair ["a" 7] :person/pairs [["a" nil] ["b" "a"]]
       :person/children ["a" "b"]}]"#,
        10,
    );
    let owner = created.tempids["owner"];
    let a = created.tempids["a"];
    let b = created.tempids["b"];
    assert_eq!(
        created.db_after.values(owner, PAIR),
        vec![&Value::Tuple(vec![
            Some(Value::Ref(a)),
            Some(Value::Long(7))
        ])]
    );
    assert_eq!(created.db_after.values(owner, PAIRS).len(), 2);
    assert_eq!(
        created.db_after.values(owner, CHILD),
        vec![&Value::Ref(a), &Value::Ref(b)]
    );
    let updated = checked_preview(
        &created.db_after,
        r#"[[:db/add [:person/pair [[:person/email "a@example.com"] 7]] :person/name "Tuple owner"]
             {:db/id [:person/email "b@example.com"] :person/friend [:person/email "a@example.com"]}]"#,
        20,
    );
    assert_eq!(
        updated.db_after.values(owner, NAME),
        vec![&Value::String("Tuple owner".into())]
    );
    assert_eq!(updated.db_after.values(b, FRIEND), vec![&Value::Ref(a)]);
    let many_lookup = checked_preview(
        &created.db_after,
        r#"[{:db/id "other" :person/children [[:person/email "a@example.com"] [:person/email "b@example.com"]]}]"#,
        20,
    );
    assert_eq!(
        many_lookup
            .db_after
            .values(many_lookup.tempids["other"], CHILD),
        vec![&Value::Ref(a), &Value::Ref(b)]
    );
    assert!(
        created
            .db_after
            .with_edn(
                r#"[{:person/children [:person/email "a@example.com"]}]"#,
                20
            )
            .is_err()
    );
    assert!(
        created
            .db_after
            .with_edn(r#"[{:person/name ["not a scalar"]}]"#, 20)
            .is_err()
    );
}

#[test]
fn builtins_entity_forms_and_transaction_metadata_use_existing_semantics() {
    let before = Database::new(schema()).unwrap();
    let added = checked_preview(
        &before,
        r#"[
      {:db/id "p" :db/ident :person/alice :person/name "Alice" :person/tags ("a" "b")}
      {:db/ident :person/required :db.entity/attrs [:person/name]}
      [ :db/add "datomic.tx" :person/name "import run"]]"#,
        10,
    );
    let alice = added.tempids["p"];
    let updated = checked_preview(
        &added.db_after,
        &format!(
            r#"[[:db/cas :person/alice :person/count nil 1]
                     [:db/retract {alice} :person/tags "a"]
                     [:db/ensure :person/alice :person/required]]"#
        ),
        20,
    );
    assert_eq!(updated.db_after.values(alice, COUNT), vec![&Value::Long(1)]);
    assert_eq!(
        updated.db_after.values(alice, TAGS),
        vec![&Value::String("b".into())]
    );
    let retracted = checked_preview(
        &updated.db_after,
        r#"[[:db/retract :person/alice :person/tags]]"#,
        30,
    );
    assert!(retracted.db_after.values(alice, TAGS).is_empty());
    let removed = checked_preview(
        &retracted.db_after,
        r#"[[:db/retractEntity :person/alice]]"#,
        40,
    );
    assert!(removed.db_after.values(alice, NAME).is_empty());
    for invalid in [
        r#"[[:db/add :person/alice :person/name]]"#,
        r#"[[:db/cas :person/alice :person/count 99 2]]"#,
        r#"[{:person/count "not a number"}]"#,
        r#"[[clojure.core/eval "(+ 1 2)"]]"#,
        r#"[#db/fn {:lang "clojure" :code "..."}]"#,
    ] {
        assert!(
            updated.db_after.with_edn(invalid, 30).is_err(),
            "accepted {invalid}"
        );
    }
    assert!(added.tx_data.iter().any(|datom| datom.entity
        == atomic_core::t_to_tx(added.db_after.basis_t()).unwrap()
        && datom.attribute == NAME
        && datom.value == Value::String("import run".into())));
}

#[test]
fn request_identity_is_schema_independent_and_formatting_insensitive() {
    let left = read_edn_transaction(
        r#"[{:db/id "p" :person/name "Alice" :person/tags #{"x" "y"}}
                                        [:db/add "p" :person/count 1]]"#,
    )
    .unwrap();
    let right = read_edn_transaction(
        r#"(; harmless comment
        [:db/add "p" :person/count 1],
        {:person/tags #{"y" "x"}, :person/name "Alice", :db/id "p"})"#,
    )
    .unwrap();
    assert_eq!(
        submission_request_digest(&left, None, None).unwrap(),
        submission_request_digest(&right, None, None).unwrap()
    );
    let changed = read_edn_transaction(
        r#"[{:db/id "p" :person/name "Bob" :person/tags #{"x" "y"}}
                                          [:db/add "p" :person/count 1]]"#,
    )
    .unwrap();
    assert_ne!(
        submission_request_digest(&left, None, None).unwrap(),
        submission_request_digest(&changed, None, None).unwrap()
    );
    // Reading/digesting unknown attrs and native calls is allowed: authoritative
    // schema/program selection happens only after checking an existing receipt.
    assert!(
        TransactionRequest::from_edn("unresolved", "[[:missing/function :missing/entity]]").is_ok()
    );
}

#[test]
fn ref_unique_schema_tuples_partition_directives_and_admission_reuse_existing_paths() {
    let mut schema = schema();
    schema
        .install(
            Attribute::new(
                1_008,
                Keyword::new("person", "primary"),
                ValueType::Ref,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    let before = Database::new(schema).unwrap();
    let first = checked_preview(
        &before,
        r#"[
        {:db/force-partition {"target" :db.part/user}}
        {:db/match-partition {"owner" "target"}}
        {:db/id "target" :person/email "target@example.com"}
        {:db/id "owner" :person/primary "target" :person/name "Owner"}]"#,
        10,
    );
    let owner = first.tempids["owner"];
    let next = checked_preview(
        &first.db_after,
        r#"[
        {:person/primary [:person/email "target@example.com"] :person/count 7}
        [:db/add [:person/primary [:person/email "target@example.com"]] :person/tags "matched"]]"#,
        20,
    );
    assert_eq!(next.db_after.values(owner, COUNT), vec![&Value::Long(7)]);
    assert_eq!(
        next.db_after.values(owner, TAGS),
        vec![&Value::String("matched".into())]
    );

    let db = before.database_value();
    let input = r#"[{:person/name "Alice" :person/email "alice@example.com"}]"#;
    let limits = atomic_core::SpeculationLimits {
        max_operations: 1,
        ..Default::default()
    };
    assert_eq!(
        db.with_edn_with_limits(input, 10, limits)
            .unwrap_err()
            .category,
        atomic_core::ErrorCategory::Busy
    );
    assert!(db.with_edn(input, 10).is_ok());
    assert_eq!(db.basis_t(), before.basis_t());

    let empty = Database::bootstrap().unwrap();
    let installed = checked_preview(
        &empty,
        r#"[{:db/ident :point/position :db/valueType :db.type/tuple
        :db/cardinality :db.cardinality/one :db/tupleTypes [:db.type/long :db.type/string]}]"#,
        10,
    );
    let tuple = installed
        .db_after
        .entid(&Keyword::new("point", "position"))
        .unwrap() as u32;
    let positioned = checked_preview(
        &installed.db_after,
        r#"[{:db/id "point" :point/position [7 "north"]}]"#,
        20,
    );
    assert_eq!(
        positioned
            .db_after
            .values(positioned.tempids["point"], tuple),
        vec![&Value::Tuple(vec![
            Some(Value::Long(7)),
            Some(Value::String("north".into()))
        ])]
    );
}

#[cfg(unix)]
#[test]
fn postgres_socket_schema_maps_and_receipt_replay_precede_ident_resolution() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "edn_receipt");
    common::install(&fixture.connection).unwrap();
    let mut store = common::TestStore::connect(&fixture.connection).unwrap();
    store.create_database("edn", Schema::new()).unwrap();
    let writer = common::start_service(&fixture.connection, "edn");
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let peer = atomic_core::Connection::connect(&fixture.connection, "edn", 8).unwrap();
    let timeout = Duration::from_secs(10);
    let installed = peer.transact_socket(server.endpoint(), TransactionRequest::from_edn("schema", r#"[
      {:db/ident :person/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
      {:db/ident :person/email :db/valueType :db.type/string :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}]"#).unwrap(), timeout).unwrap();
    let attribute = installed
        .report
        .as_ref()
        .unwrap()
        .db_after
        .entid(&Keyword::new("person", "name"))
        .unwrap();
    let request = TransactionRequest::from_edn(
        "alice",
        r#"[{:db/id "alice" :person/name "Alice" :person/email "alice@example.com"}]"#,
    )
    .unwrap();
    let committed = peer
        .transact_socket(server.endpoint(), request.clone(), timeout)
        .unwrap();
    let receipt = committed.report.as_ref().unwrap();
    let alice = receipt.tempids["alice"];
    assert_eq!(
        receipt.db_after.values(alice, attribute as u32).unwrap(),
        vec![Value::String("Alice".into())]
    );
    // Rename then repurpose the submitted keyword onto a non-attribute entity.
    // Old schema names can remain aliases; repurposing makes reevaluation fail
    // unambiguously, rather than accidentally succeeding through that alias.
    peer.transact_socket(
        server.endpoint(),
        TransactionRequest::new(
            "forget-old-ident",
            vec![
                TxOp::Retract {
                    entity: EntityRef::Id(attribute),
                    attribute: atomic_core::DB_IDENT as u32,
                    value: Some(Value::Keyword(Keyword::new("person", "name")).into()),
                },
                TxOp::Add {
                    entity: EntityRef::Id(attribute),
                    attribute: atomic_core::DB_IDENT as u32,
                    value: Value::Keyword(Keyword::new("person", "label")).into(),
                },
            ],
        ),
        timeout,
    )
    .unwrap();
    let repurposed = peer
        .transact_socket(
            server.endpoint(),
            TransactionRequest::new(
                "repurpose-old-ident",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("ordinary".into()),
                    attribute: atomic_core::DB_IDENT as u32,
                    value: Value::Keyword(Keyword::new("person", "name")).into(),
                }],
            ),
            timeout,
        )
        .unwrap();
    assert_ne!(
        repurposed
            .report
            .as_ref()
            .unwrap()
            .db_after
            .entid(&Keyword::new("person", "name")),
        Some(attribute)
    );
    drop(server);
    writer.shutdown();
    let writer = common::start_service(&fixture.connection, "edn");
    let server =
        atomic_core::LocalTransactionServer::start(writer.client(), Default::default()).unwrap();
    let replay = peer
        .transact_socket(server.endpoint(), request, timeout)
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    let replay = replay.report.unwrap();
    assert_eq!(replay.tx_data, receipt.tx_data);
    assert_eq!(replay.tempids, receipt.tempids);
    let reevaluated =
        TransactionRequest::from_edn("new-after-repurpose", r#"[{:person/name "Alice"}]"#).unwrap();
    assert!(
        peer.transact_socket(server.endpoint(), reevaluated, timeout)
            .is_err(),
        "a new request must resolve the repurposed keyword against authoritative db-before"
    );
    let changed = TransactionRequest::from_edn(
        "alice",
        r#"[{:db/id "alice" :person/name "Bob" :person/email "alice@example.com"}]"#,
    )
    .unwrap();
    let rejected = peer
        .transact_socket(server.endpoint(), changed, timeout)
        .unwrap_err();
    assert_eq!(rejected.category, atomic_core::ErrorCategory::Conflict);
    assert_eq!(
        rejected.details.get("remote_code").map(String::as_str),
        Some("postgres/idempotency-key-reused")
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    drop(server);
    writer.shutdown();
}

#[test]
fn postgres_edn_program_rebinding_cannot_change_a_retained_receipt() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "edn_program");
    common::install(&fixture.connection).unwrap();
    let mut store = common::TestStore::connect(&fixture.connection).unwrap();
    store.create_database("edn", schema()).unwrap();
    let program = |count| atomic_core::Program {
        kind: atomic_core::ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            atomic_core::Instruction::PushArgument(0),
            atomic_core::Instruction::PushConstant(Value::Long(count)),
            atomic_core::Instruction::EmitAdd(COUNT),
            atomic_core::Instruction::Return,
        ],
    };
    let first_hash = store.deploy_program_blob(&program(1)).unwrap();
    let second_hash = store.deploy_program_blob(&program(2)).unwrap();
    let emitter = store
        .deploy_program_blob(&atomic_core::Program {
            kind: atomic_core::ProgramKind::Transaction,
            arity: 1,
            instructions: vec![
                atomic_core::Instruction::PushArgument(0),
                atomic_core::Instruction::EmitEntityMap,
                atomic_core::Instruction::Return,
            ],
        })
        .unwrap();
    let writer = common::start_service(&fixture.connection, "edn");
    let timeout = Duration::from_secs(10);
    let installed = writer
        .client()
        .transact(
            TransactionRequest::new(
                "install",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("fn".into()),
                        attribute: atomic_core::DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("test", "set-count")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("fn".into()),
                        attribute: atomic_core::DB_FN as u32,
                        value: Value::Function(first_hash).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("person".into()),
                        attribute: NAME,
                        value: Value::String("Alice".into()).into(),
                    },
                ],
            ),
            timeout,
        )
        .unwrap();
    let person = installed.tempids["person"];
    let request =
        TransactionRequest::from_edn("call", &format!("[[:test/set-count #atomic/ref {person}]]"))
            .unwrap();
    let first = writer.client().transact(request.clone(), timeout).unwrap();
    assert_eq!(
        first.db_after.values(person, COUNT).unwrap(),
        vec![Value::Long(1)]
    );
    writer
        .client()
        .transact(
            TransactionRequest::new(
                "rebind",
                vec![TxOp::Add {
                    entity: EntityRef::Id(installed.tempids["fn"]),
                    attribute: atomic_core::DB_FN as u32,
                    value: Value::Function(second_hash).into(),
                }],
            ),
            timeout,
        )
        .unwrap();
    writer.shutdown();
    let writer = common::start_service(&fixture.connection, "edn");
    let replay = writer.client().transact(request, timeout).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, first.basis_t);
    assert_eq!(replay.tx_data, first.tx_data);
    let next = writer
        .client()
        .transact(
            TransactionRequest::from_edn(
                "call-new",
                &format!("[[:test/set-count #atomic/ref {person}]]"),
            )
            .unwrap(),
            timeout,
        )
        .unwrap();
    assert_eq!(
        next.db_after.values(person, COUNT).unwrap(),
        vec![Value::Long(2)]
    );
    let hash: String = emitter.iter().map(|byte| format!("{byte:02x}")).collect();
    let generated = writer.client().transact(TransactionRequest::from_edn("generated-map", &format!(r#"[
        {{:person/name "anonymous"}}
        [#atomic/function "{hash}" {{:db/id "__map/00000000000000000000" :person/name "generated"}}]]"#)).unwrap(), timeout).unwrap();
    let generated_id = generated.tempids["__map/00000000000000000000"];
    assert_eq!(
        generated.db_after.values(generated_id, NAME).unwrap(),
        vec![Value::String("generated".into())]
    );
    assert_eq!(
        generated
            .db_after
            .datoms_with_prefix(&atomic_core::IndexPrefix::Aevt {
                attribute: NAME,
                entity: None,
                value: None
            })
            .unwrap()
            .len(),
        3
    );
    writer.shutdown();
}
