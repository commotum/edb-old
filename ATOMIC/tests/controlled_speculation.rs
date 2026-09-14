//! Native d/with-style controlled generation and validation. Semantic anchors:
//! transactions/transaction-functions: every function sees the same db-before;
//! schema/schema-reference: new attribute predicates begin next transaction,
//! and entity predicates receive the complete proposed db-after.
mod common;

use atomic_core::{
    Attribute, CallableRef, Cardinality, DB_ATTR_PREDS, DB_ENSURE, DB_ENTITY_ATTRS,
    DB_ENTITY_PREDS, DB_FN, DB_IDENT, DatabaseValue, EntityRef, ErrorCategory, Instruction,
    Keyword, Peer, Program, ProgramCall, ProgramHash, ProgramKind, Schema, SemanticError,
    ServiceTransactionReport, SpeculationLimits, SpeculativeTransactionReport, Symbol,
    TransactionRequest, TransactionService, TxForm, TxOp, TxValue, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::sync::mpsc;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const BALANCE: u32 = 1_000;
const SNAPSHOT: u32 = 1_001;

fn ident(name: &str) -> Keyword {
    Keyword::new("controlled-speculation", name)
}
fn entity() -> u64 {
    atomic_core::make_eid(atomic_core::USER_PARTITION, 42).unwrap()
}
fn unique() -> String {
    format!(
        "controlled-speculation-{}-{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

struct Fixture {
    connection: String,
    database_id: String,
    store: common::TestStore,
    service: TransactionService,
    peer: Peer,
    _scope: common::PostgresFixture,
}

impl Fixture {
    fn new() -> Option<Self> {
        let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
            eprintln!("SKIP controlled speculation: ATOMIC_POSTGRES_URL is unset");
            return None;
        };
        let scope = common::PostgresFixture::new(&connection, "controlled_speculation");
        let connection = scope.connection.clone();
        let database_id = unique();
        common::install(&connection).unwrap();
        let mut schema = Schema::new();
        for (attribute, name) in [(BALANCE, "balance"), (SNAPSHOT, "snapshot")] {
            schema
                .install(Attribute::new(
                    attribute,
                    ident(name),
                    ValueType::Long,
                    Cardinality::One,
                ))
                .unwrap();
        }
        let mut store = common::TestStore::connect(&connection).unwrap();
        store.create_database(&database_id, schema).unwrap();
        let service = common::start_service(&connection, &database_id);
        let peer = Peer::connect(&connection, &database_id, 8).unwrap();
        Some(Self {
            connection,
            database_id,
            store,
            service,
            peer,
            _scope: scope,
        })
    }

    fn deploy(&mut self, program: &Program) -> ProgramHash {
        self.store.deploy_program_blob(program).unwrap()
    }

    fn transact(
        &self,
        key: &str,
        before: &DatabaseValue,
        forms: &[TxForm],
        instant: i64,
    ) -> Result<ServiceTransactionReport, SemanticError> {
        self.service.client().transact(
            TransactionRequest::from_forms(key, forms.to_vec())
                .comparing_basis(before.basis_t())
                .with_tx_instant(instant),
            Duration::from_secs(30),
        )
    }

    fn paired(&self, key: &str, forms: &[TxForm], instant: i64) -> SpeculativeTransactionReport {
        let before = self.peer.sync().unwrap();
        let pure = before.with_forms(forms, instant).unwrap();
        assert_eq!(
            self.peer.sync().unwrap().basis_t(),
            before.basis_t(),
            "pure evaluation must not publish"
        );
        let durable = self.transact(key, &before, forms, instant).unwrap();
        assert_eq!(pure.tx_data, durable.tx_data);
        assert_eq!(pure.tempids, durable.tempids);
        common::assert_same_information(&pure.db_before, &durable.db_before);
        common::assert_same_information(&pure.db_after, &durable.db_after);
        self.peer
            .sync_to(durable.basis_t, Duration::from_secs(30))
            .unwrap();
        pure
    }

    fn rejected(&self, key: &str, forms: &[TxForm], instant: i64, code: &str) {
        let before = self.peer.sync().unwrap();
        let pure = before.with_forms(forms, instant).unwrap_err();
        let durable = self.transact(key, &before, forms, instant).unwrap_err();
        assert_eq!(pure.code, code, "{pure:?}");
        assert_eq!(durable.code, code, "{durable:?}");
        assert_eq!(pure.category, durable.category);
        assert_eq!(self.peer.sync().unwrap().basis_t(), before.basis_t());
    }
}

fn add(entity: EntityRef, attribute: u32, value: impl Into<TxValue>) -> TxForm {
    TxForm::Op(TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    })
}

fn binding(name: &str, hash: ProgramHash) -> Vec<TxForm> {
    vec![
        add(
            EntityRef::Temp(name.into()),
            DB_IDENT as u32,
            Value::Keyword(ident(name)),
        ),
        add(
            EntityRef::Temp(name.into()),
            DB_FN as u32,
            Value::Function(hash),
        ),
    ]
}

fn call(name: &str, arguments: Vec<Value>) -> TxForm {
    TxForm::ProgramCall(ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(ident(name))),
        arguments: arguments.into_iter().map(Into::into).collect(),
    })
}

fn setter() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 2,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushArgument(1),
            Instruction::EmitAdd(BALANCE),
            Instruction::Return,
        ],
    }
}

fn snapshot() -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::Duplicate,
            Instruction::LoadOne(BALANCE),
            Instruction::EmitAdd(SNAPSHOT),
            Instruction::Return,
        ],
    }
}

fn nested(function: CallableRef, marker: i64) -> Program {
    Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushConstant(Value::Long(marker)),
            Instruction::Pop,
            Instruction::PushArgument(0),
            Instruction::EmitCall {
                function,
                argument_count: 1,
            },
            Instruction::Return,
        ],
    }
}

fn positive() -> Program {
    Program {
        kind: ProgramKind::AttributePredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(0)),
            Instruction::GreaterThan,
            Instruction::Return,
        ],
    }
}

fn ordered() -> Program {
    Program {
        kind: ProgramKind::EntityPredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(BALANCE),
            Instruction::PushArgument(0),
            Instruction::LoadOne(SNAPSHOT),
            Instruction::GreaterThan,
            Instruction::Not,
            Instruction::Return,
        ],
    }
}

fn ensure() -> TxForm {
    add(
        EntityRef::Id(entity()),
        DB_ENSURE as u32,
        TxValue::Entity(EntityRef::Ident(ident("guard"))),
    )
}

#[test]
fn native_controlled_generation_is_pure_same_before_and_validates_exact_after() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let set_hash = fixture.deploy(&setter());
    let snapshot_hash = fixture.deploy(&snapshot());
    let nested_hash = fixture.deploy(&nested(
        CallableRef::Database(EntityRef::Ident(ident("snapshot-function"))),
        1,
    ));
    let ordered_hash = fixture.deploy(&ordered());
    let replacement_hash = fixture.deploy(&Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(999)),
            Instruction::EmitAdd(SNAPSHOT),
            Instruction::Return,
        ],
    });
    let mut installation = binding("setter", set_hash);
    installation.extend(binding("snapshot-function", snapshot_hash));
    installation.extend(binding("nested", nested_hash));
    installation.extend(binding("ordered", ordered_hash));
    installation.extend([
        add(
            EntityRef::Temp("guard".into()),
            DB_IDENT as u32,
            Value::Keyword(ident("guard")),
        ),
        add(
            EntityRef::Temp("guard".into()),
            DB_ENTITY_PREDS as u32,
            Value::Symbol(Symbol::new("controlled-speculation", "ordered")),
        ),
        add(
            EntityRef::Temp("guard".into()),
            DB_ENTITY_ATTRS as u32,
            Value::Keyword(ident("balance")),
        ),
        add(
            EntityRef::Temp("guard".into()),
            DB_ENTITY_ATTRS as u32,
            Value::Keyword(ident("snapshot")),
        ),
        add(EntityRef::Id(entity()), BALANCE, Value::Long(10)),
        add(EntityRef::Id(entity()), SNAPSHOT, Value::Long(10)),
    ]);
    fixture.paired("install", &installation, 1_000);
    let before = fixture.peer.db();
    fixture.rejected(
        "bad-after",
        &[
            call("setter", vec![Value::Ref(entity()), Value::Long(30)]),
            call("nested", vec![Value::Ref(entity())]),
            ensure(),
        ],
        2_000,
        "transaction/entity-predicate",
    );
    let forms = vec![
        call("setter", vec![Value::Ref(entity()), Value::Long(5)]),
        call("nested", vec![Value::Ref(entity())]),
        ensure(),
        add(
            EntityRef::Ident(ident("snapshot-function")),
            DB_FN as u32,
            Value::Function(replacement_hash),
        ),
    ];

    // Setter + wrapper + nested snapshot consume three invocations; the
    // exact db-after predicate is the fourth, on that same attempt budget.
    let error = before
        .with_forms_with_limits(
            &forms,
            2_000,
            SpeculationLimits {
                program: atomic_core::ProgramLimits {
                    max_calls: 3,
                    ..Default::default()
                },
                ..Default::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "program/call-limit");

    // Pure execution must not wait for a writer/head row lock. Always release
    // our lock before checking the worker result, including a failed timeout.
    let mut client = Client::connect(&fixture.connection, NoTls).unwrap();
    let mut transaction = client.transaction().unwrap();
    transaction
        .query_one(
            "SELECT revision FROM atomic_refs WHERE key = $1 FOR UPDATE",
            &[&atomic_core::storage::BlockDatabase::resolve(
                &atomic_core::PostgresConnectionConfig::plaintext(&fixture.connection),
                &fixture.database_id,
            )
            .unwrap()
            .reference_key()],
        )
        .unwrap();
    let (sender, receiver) = mpsc::channel();
    let captured = before.clone();
    let proposed = forms.clone();
    let worker = std::thread::spawn(move || {
        sender.send(captured.with_forms(&proposed, 2_000)).unwrap();
    });
    let result = receiver.recv_timeout(Duration::from_secs(15));
    transaction.rollback().unwrap();
    worker.join().unwrap();
    let pure = result
        .expect("native speculation blocked on a writer lock")
        .unwrap();
    assert_eq!(
        pure.db_after.values(entity(), BALANCE).unwrap(),
        vec![Value::Long(5)]
    );
    assert_eq!(
        pure.db_after.values(entity(), SNAPSHOT).unwrap(),
        vec![Value::Long(10)]
    );
    let mut reversed = forms.clone();
    reversed.reverse();
    let reordered = before.with_forms(&reversed, 2_000).unwrap();
    assert_eq!(pure.tx_data, reordered.tx_data);
    let durable = fixture
        .transact("controlled", &before, &forms, 2_000)
        .unwrap();
    assert_eq!(pure.tx_data, durable.tx_data);
    common::assert_same_information(&pure.db_after, &durable.db_after);
    let next = pure
        .db_after
        .with_forms(&[call("nested", vec![Value::Ref(entity())])], 3_000)
        .unwrap();
    assert_eq!(
        next.db_after.values(entity(), SNAPSHOT).unwrap(),
        vec![Value::Long(999)]
    );
    assert_eq!(
        before.values(entity(), BALANCE).unwrap(),
        vec![Value::Long(10)]
    );
    assert_eq!(fixture.peer.sync().unwrap().basis_t(), durable.basis_t);
}

#[test]
fn native_predicate_activation_and_changed_bindings_match_durable_rules() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let positive_hash = fixture.deploy(&positive());
    let transaction_hash = fixture.deploy(&setter());
    fixture.rejected(
        "missing-unused",
        &binding("unused", [0xa5; 32]),
        1_000,
        "storage/missing-object",
    );
    let mut wrong = binding("wrong", transaction_hash);
    wrong.push(add(
        EntityRef::Id(u64::from(BALANCE)),
        DB_ATTR_PREDS as u32,
        Value::Symbol(Symbol::new("controlled-speculation", "wrong")),
    ));
    fixture.rejected(
        "wrong-unused-predicate",
        &wrong,
        1_000,
        "program/not-attribute-predicate",
    );
    let mut wrong_entity = binding("wrong-entity", transaction_hash);
    wrong_entity.push(add(
        EntityRef::Temp("guard".into()),
        DB_ENTITY_PREDS as u32,
        Value::Symbol(Symbol::new("controlled-speculation", "wrong-entity")),
    ));
    fixture.rejected(
        "wrong-unused-entity-predicate",
        &wrong_entity,
        1_000,
        "program/not-entity-predicate",
    );

    let mut installation = binding("positive", positive_hash);
    installation.extend([
        add(
            EntityRef::Id(u64::from(BALANCE)),
            DB_ATTR_PREDS as u32,
            Value::Symbol(Symbol::new("controlled-speculation", "positive")),
        ),
        add(EntityRef::Id(entity()), BALANCE, Value::Long(-1)),
    ]);
    let installed = fixture.paired("install-predicate", &installation, 1_000);
    assert_eq!(
        installed.db_after.values(entity(), BALANCE).unwrap(),
        vec![Value::Long(-1)]
    );
    fixture.rejected(
        "next-negative",
        &[add(EntityRef::Id(entity()), BALANCE, Value::Long(-2))],
        2_000,
        "transaction/attribute-predicate",
    );
    fixture.paired(
        "next-positive",
        &[add(EntityRef::Id(entity()), BALANCE, Value::Long(2))],
        2_000,
    );
    let renamed = fixture.paired(
        "rename-predicate",
        &[add(
            EntityRef::Ident(ident("positive")),
            DB_IDENT as u32,
            Value::Keyword(ident("renamed-positive")),
        )],
        3_000,
    );
    assert_eq!(
        renamed.db_after.entid(&ident("positive")),
        renamed.db_after.entid(&ident("renamed-positive"))
    );
    fixture.rejected(
        "bad-alias-rebind",
        &[add(
            EntityRef::Ident(ident("renamed-positive")),
            DB_FN as u32,
            Value::Function(transaction_hash),
        )],
        4_000,
        "program/not-attribute-predicate",
    );
    fixture.rejected(
        "remove-active-code",
        &[TxForm::Op(TxOp::Retract {
            entity: EntityRef::Ident(ident("renamed-positive")),
            attribute: DB_FN as u32,
            value: None,
        })],
        4_000,
        "program/not-a-database-function",
    );
}

#[test]
fn native_missing_transitive_code_and_resource_rejections_do_not_publish() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let missing_root = fixture.deploy(&nested(CallableRef::ExactHash([0xb6; 32]), 2));
    let before = fixture.peer.db();
    let forms = binding("missing-child", missing_root);
    let pure = before.with_forms(&forms, 1_000).unwrap_err();
    assert_eq!(pure.code, "storage/missing-object");
    let durable = fixture
        .transact("missing-child", &before, &forms, 1_000)
        .unwrap_err();
    assert_eq!(durable.code, "storage/missing-object");
    assert_eq!(fixture.peer.sync().unwrap().basis_t(), before.basis_t());

    let child_hash = fixture.deploy(&snapshot());
    let nested_hash = fixture.deploy(&nested(CallableRef::ExactHash(child_hash), 3));
    let mut installation = binding("nested-budget", nested_hash);
    installation.push(add(EntityRef::Id(entity()), BALANCE, Value::Long(7)));
    for limits in [
        SpeculationLimits {
            max_program_dependencies: 1,
            ..Default::default()
        },
        SpeculationLimits {
            max_program_bytes: 1,
            ..Default::default()
        },
    ] {
        let error = before
            .with_forms_with_limits(&installation, 1_000, limits)
            .unwrap_err();
        assert_eq!(
            (error.category, error.code),
            (
                ErrorCategory::Busy,
                "transaction/program-dependency-capacity"
            )
        );
        assert_eq!(fixture.peer.sync().unwrap().basis_t(), before.basis_t());
    }
    let bounded = before
        .with_forms_with_limits(
            &installation,
            1_000,
            SpeculationLimits {
                max_program_dependencies: 2,
                max_program_bytes: 16 * 1024,
                ..Default::default()
            },
        )
        .unwrap();
    assert!(bounded.db_after.entid(&ident("nested-budget")).is_some());
    fixture.paired("install-budget", &installation, 1_000);
    let before = fixture.peer.db();
    let forms = [call("nested-budget", vec![Value::Ref(entity())])];
    for (limits, code) in [
        (
            SpeculationLimits {
                program: atomic_core::ProgramLimits {
                    fuel: 1,
                    ..Default::default()
                },
                ..Default::default()
            },
            "program/fuel-exhausted",
        ),
        (
            SpeculationLimits {
                program: atomic_core::ProgramLimits {
                    max_calls: 1,
                    ..Default::default()
                },
                ..Default::default()
            },
            "program/call-limit",
        ),
        (
            SpeculationLimits {
                max_read_datoms: 1,
                ..Default::default()
            },
            "transaction/read-capacity",
        ),
        (
            SpeculationLimits {
                max_read_bytes: 1,
                ..Default::default()
            },
            "transaction/read-capacity",
        ),
    ] {
        let error = before
            .with_forms_with_limits(&forms, 2_000, limits)
            .unwrap_err();
        assert_eq!(
            (error.category, error.code),
            (ErrorCategory::Busy, code),
            "{error:?}"
        );
    }
    let error = before
        .with_forms_with_limits(
            &[
                add(EntityRef::Id(entity()), BALANCE, Value::Long(8)),
                add(EntityRef::Id(entity()), SNAPSHOT, Value::Long(8)),
            ],
            2_000,
            SpeculationLimits {
                max_operations: 1,
                ..Default::default()
            },
        )
        .unwrap_err();
    assert_eq!(error.code, "postgres/transaction-op-capacity");
    let successful = before
        .with_forms_with_limits(
            &forms,
            2_000,
            SpeculationLimits {
                max_read_datoms: 32,
                ..Default::default()
            },
        )
        .unwrap();
    assert_eq!(
        successful.db_after.values(entity(), SNAPSHOT).unwrap(),
        vec![Value::Long(7)]
    );
    assert!(
        successful
            .db_after
            .collect_datoms(atomic_core::IndexOrder::Eavt)
            .unwrap()
            .len()
            > 32,
        "a returned database value must not retain the attempt's consumed read budget"
    );
    assert_eq!(fixture.peer.sync().unwrap().basis_t(), before.basis_t());
    assert!(before.values(entity(), SNAPSHOT).unwrap().is_empty());
}

#[test]
fn speculative_code_closure_survives_chaining_cache_eviction_and_scoped_reclamation() {
    let Some(mut fixture) = Fixture::new() else {
        return;
    };
    let marker = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos() as i64;
    let leaf_hash = fixture.deploy(&Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushConstant(Value::Long(marker)),
            Instruction::Pop,
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::Long(7)),
            Instruction::EmitAdd(BALANCE),
            Instruction::Return,
        ],
    });
    let root_hash = fixture.deploy(&nested(
        CallableRef::ExactHash(leaf_hash),
        marker.wrapping_add(1),
    ));
    let original = fixture.peer.db();
    let first = original
        .with_forms(&binding("retained-root", root_hash), 1_000)
        .unwrap();
    let mut current = first.db_after.clone();
    // The ordinary content cache holds 64 entries. Populate distinct content
    // through successive native branches; the speculative closure must not
    // depend on that evictable cache. The post-delete miss below proves eviction.
    for index in 0..80 {
        let hash = fixture.deploy(&Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions: vec![
                Instruction::PushConstant(Value::Long(marker.wrapping_add(index + 2))),
                Instruction::Pop,
                Instruction::Return,
            ],
        });
        current = current
            .with_forms(&binding(&format!("filler-{index}"), hash), 1_001 + index)
            .unwrap()
            .db_after;
    }
    // Deliberate deletion of only this fixture's never-committed program roots.
    // The speculative value's retained closure must not depend on provider I/O.
    let config = atomic_core::PostgresConnectionConfig::plaintext(&fixture.connection);
    assert_eq!(
        atomic_core::storage::PgBlockStore::connect(&config)
            .unwrap()
            .remove_objects(&[root_hash, leaf_hash])
            .unwrap(),
        2
    );
    for hash in [root_hash, leaf_hash] {
        let uncached = original
            .with_forms(
                &[TxForm::ProgramCall(ProgramCall {
                    function: CallableRef::ExactHash(hash),
                    arguments: vec![Value::Ref(entity()).into()],
                })],
                2_000,
            )
            .unwrap_err();
        assert_eq!(
            uncached.code, "storage/missing-object",
            "shared-cache eviction must be observed: {uncached:?}"
        );
    }
    let called = current
        .with_forms(&[call("retained-root", vec![Value::Ref(entity())])], 2_000)
        .unwrap();
    assert_eq!(
        called.db_after.values(entity(), BALANCE).unwrap(),
        vec![Value::Long(7)]
    );
    let called_again = called
        .db_after
        .with_forms(&[call("retained-root", vec![Value::Ref(entity())])], 3_000)
        .unwrap();
    assert_eq!(
        called_again.db_after.values(entity(), BALANCE).unwrap(),
        vec![Value::Long(7)]
    );
    assert!(first.db_after.values(entity(), BALANCE).unwrap().is_empty());
    assert!(original.entid(&ident("retained-root")).is_none());
    assert_eq!(fixture.peer.sync().unwrap().basis_t(), original.basis_t());
}
