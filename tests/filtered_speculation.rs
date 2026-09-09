//! Filters window the result of `with`; they do not branch historical state.
//! Semantic authority: datomic_pro_docs/02_core_concepts/02_database_filters.md,
//! "as-of Is Not a Branch". Controlled calls deliberately follow that native,
//! docs-first rule too, rather than reproducing recovered filtered-call quirks.
mod common;

use atomic_core::{
    Attribute, CallableRef, Cardinality, Database, DatabaseValue, EntityRef, ErrorCategory,
    Instruction, Keyword, Program, ProgramCall, ProgramKind, Schema, TransactionRequest, TxForm,
    TxOp, Unique, Value, ValueType,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const COUNT: u32 = 1_000;
const KEY: u32 = 1_001;
const TRANSIENT: u32 = 1_002;
const LATE: u32 = 1_003;

fn entity() -> u64 {
    atomic_core::make_eid(atomic_core::USER_PARTITION, 42).unwrap()
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                KEY,
                Keyword::new("item", "key"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Value),
        )
        .unwrap();
    let mut transient = Attribute::new(
        TRANSIENT,
        Keyword::new("item", "transient"),
        ValueType::String,
        Cardinality::One,
    );
    transient.no_history = true;
    schema.install(transient).unwrap();
    schema
}

fn first_ops() -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Id(entity()),
            attribute: COUNT,
            value: Value::Long(1).into(),
        },
        TxOp::Add {
            entity: EntityRef::Id(entity()),
            attribute: KEY,
            value: Value::String("taken".into()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Id(entity()),
            attribute: TRANSIENT,
            value: Value::String("old".into()).into(),
        },
    ]
}

fn second_ops(entity: u64) -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: COUNT,
            value: Value::Long(2).into(),
        },
        TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: TRANSIENT,
            value: Value::String("new".into()).into(),
        },
        TxOp::InstallAttribute(Attribute::new(
            LATE,
            Keyword::new("item", "late"),
            ValueType::String,
            Cardinality::One,
        )),
    ]
}

fn cas(entity: u64, old: i64, new: i64) -> TxOp {
    TxOp::Cas {
        entity: EntityRef::Id(entity),
        attribute: COUNT,
        old: Some(Value::Long(old).into()),
        new: Value::Long(new).into(),
    }
}

fn exercise_filters(base: DatabaseValue, entity: u64, first_t: u64) {
    let past = base.clone().as_of(first_t);
    assert_eq!(past.values(entity, COUNT).unwrap(), vec![Value::Long(1)]);
    // noHistory reclamation is asynchronous. Preserve exactly the history
    // still present in this captured basis; `with` must not invent more.
    let transient_before = past.values(entity, TRANSIENT).unwrap();
    assert!(
        past.schema().attribute(LATE).is_ok(),
        "windowing retains basis schema metadata"
    );
    let wrong_past_cas = past.with(&[cas(entity, 1, 3)], 3).unwrap_err();
    assert_eq!(
        (wrong_past_cas.category, wrong_past_cas.code),
        (ErrorCategory::Conflict, "transaction/cas-failed")
    );
    assert_eq!(
        past.with(&[], 1).unwrap_err().code,
        "transaction/non-monotonic-instant",
        "time remains monotonic against full db-before"
    );
    let ops = [
        cas(entity, 2, 3),
        TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: LATE,
            value: Value::String("late schema is current".into()).into(),
        },
    ];
    let plain = base.with(&ops, 3).unwrap();
    let filtered = past.with(&ops, 3).unwrap();
    assert_eq!(filtered.tx_data, plain.tx_data);
    assert_eq!(filtered.tempids, plain.tempids);
    assert_eq!(filtered.db_after.basis_t(), base.basis_t() + 1);
    assert_eq!(filtered.db_before.as_of_t(), Some(first_t));
    assert_eq!(filtered.db_after.as_of_t(), Some(first_t));
    assert_eq!(
        filtered.db_after.values(entity, COUNT).unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(
        filtered.db_after.values(entity, TRANSIENT).unwrap(),
        transient_before
    );
    assert!(filtered.db_after.values(entity, LATE).unwrap().is_empty());
    common::assert_same_information(&filtered.db_after, &plain.db_after.clone().as_of(first_t));
    let exposed = filtered.db_after.clone().as_of(plain.db_after.basis_t());
    assert_eq!(exposed.values(entity, COUNT).unwrap(), vec![Value::Long(3)]);
    assert_eq!(
        exposed.values(entity, LATE).unwrap(),
        vec![Value::String("late schema is current".into())]
    );
    let chained = filtered.db_after.with(&[cas(entity, 3, 4)], 4).unwrap();
    assert_eq!(chained.db_after.as_of_t(), Some(first_t));
    assert_eq!(
        chained.db_after.values(entity, COUNT).unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(
        chained
            .db_after
            .clone()
            .as_of(chained.db_after.basis_t())
            .values(entity, COUNT)
            .unwrap(),
        vec![Value::Long(4)]
    );

    let since = base.clone().since(first_t).with(&ops, 3).unwrap();
    assert_eq!(since.db_after.since_t(), Some(first_t));
    assert!(since.db_after.values(entity, KEY).unwrap().is_empty());
    assert_eq!(
        since.db_after.values(entity, COUNT).unwrap(),
        vec![Value::Long(3)]
    );
    common::assert_same_information(&since.db_after, &plain.db_after.clone().since(first_t));

    let hidden = base.clone().filter(|_, datom| {
        datom.attribute != KEY && (datom.attribute != COUNT || datom.value == Value::Long(1))
    });
    assert!(hidden.values(entity, KEY).unwrap().is_empty());
    let conflict = hidden
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("other".into()),
                attribute: KEY,
                value: Value::String("taken".into()).into(),
            }],
            3,
        )
        .unwrap_err();
    assert_eq!(
        (conflict.category, conflict.code),
        (ErrorCategory::Conflict, "transaction/unique-value-conflict")
    );
    let hidden_after = hidden.with(&ops, 3).unwrap();
    assert!(hidden_after.db_after.is_filtered());
    assert!(
        hidden_after
            .db_after
            .values(entity, KEY)
            .unwrap()
            .is_empty()
    );
    assert_eq!(hidden_after.tx_data, plain.tx_data);
    let expected_hidden = plain.db_after.clone().filter(|_, datom| {
        datom.attribute != KEY && (datom.attribute != COUNT || datom.value == Value::Long(1))
    });
    common::assert_same_information(&hidden_after.db_after, &expected_hidden);
    assert_eq!(
        base.clone().history().with(&[], 3).unwrap_err().code,
        "transaction/history-with"
    );
    assert_eq!(base.values(entity, COUNT).unwrap(), vec![Value::Long(2)]);
}

#[test]
fn filtered_with_uses_full_basis_and_reapplies_read_windows() {
    let database = Database::new(schema()).unwrap();
    let first = database.with(&first_ops(), 1).unwrap();
    let entity = entity();
    let second = first.db_after.with(&second_ops(entity), 2).unwrap();
    exercise_filters(
        second.db_after.database_value(),
        entity,
        first.db_after.basis_t(),
    );
}

#[test]
fn postgres_filtered_speculation_and_controlled_generation_commute_with_filters() {
    let Ok(postgres) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL filtered speculation: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let id = format!(
        "filtered-speculation-{}-{}",
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
    let mut store = atomic_core::PostgresStore::connect(&postgres).unwrap();
    store.create_database(&id, schema()).unwrap();
    let increment = store
        .deploy_program_blob(&Program {
            kind: ProgramKind::Transaction,
            arity: 1,
            instructions: vec![
                Instruction::PushArgument(0),
                Instruction::Duplicate,
                Instruction::LoadOne(COUNT),
                Instruction::PushConstant(Value::Long(1)),
                Instruction::Add,
                Instruction::EmitAdd(COUNT),
                Instruction::Return,
            ],
        })
        .unwrap();
    let writer = common::start_service(&postgres, &id);
    let peer = atomic_core::Connection::connect(&postgres, &id, 4).unwrap();
    let first = writer
        .client()
        .transact(
            TransactionRequest::new("first", first_ops()).with_tx_instant(1),
            Duration::from_secs(15),
        )
        .unwrap();
    let entity = entity();
    let second = writer
        .client()
        .transact(
            TransactionRequest::new("second", second_ops(entity)).with_tx_instant(2),
            Duration::from_secs(15),
        )
        .unwrap();
    let base = peer
        .sync_to(second.basis_t, Duration::from_secs(15))
        .unwrap();
    exercise_filters(base.clone(), entity, first.basis_t);
    let forms = [TxForm::ProgramCall(ProgramCall {
        function: CallableRef::ExactHash(increment),
        arguments: vec![EntityRef::Id(entity).into()],
    })];
    let plain = base.with_forms(&forms, 3).unwrap();
    assert_eq!(
        plain.db_after.values(entity, COUNT).unwrap(),
        vec![Value::Long(3)]
    );
    let past = base
        .clone()
        .as_of(first.basis_t)
        .with_forms(&forms, 3)
        .unwrap();
    assert_eq!(
        past.tx_data, plain.tx_data,
        "controlled LoadOne reads the full basis, not the old window"
    );
    common::assert_same_information(&past.db_after, &plain.db_after.clone().as_of(first.basis_t));
    assert_eq!(
        past.db_after
            .clone()
            .as_of(past.db_after.basis_t())
            .values(entity, COUNT)
            .unwrap(),
        vec![Value::Long(3)]
    );
    let hidden = base
        .clone()
        .filter(|_, datom| datom.attribute != COUNT)
        .with_forms(&forms, 3)
        .unwrap();
    assert_eq!(hidden.tx_data, plain.tx_data);
    assert!(hidden.db_after.values(entity, COUNT).unwrap().is_empty());
    assert!(hidden.db_after.is_filtered());
    assert_eq!(peer.sync().unwrap().basis_t(), base.basis_t());
    assert_eq!(store.recover(&id).unwrap().basis_t(), base.basis_t());
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    writer.shutdown();
}
