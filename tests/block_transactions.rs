//! Actual Rust-owned publication and exact outcomes on fresh opaque storage.
mod common;

use atomic_core::index::tree::{TreeConfig, build_tree};
use atomic_core::storage::receipts::{ExactReceipt, RequestIndex, scoped_request_key};
use atomic_core::storage::root::DatabaseRoot;
use atomic_core::storage::{BlockDatabase, BlockReader, CasOutcome, IndexDescriptor, PgBlockStore};
use atomic_core::{
    Attribute, CallableRef, Cardinality, Database, DatabaseValue, EntityRef, IndexOrder,
    IndexTransaction, Instruction, Keyword, NativeRegistry, OperationContext, OperationKind,
    PostgresConnectionConfig, Program, ProgramCall, ProgramKind, RuntimeValue, Schema,
    ServiceTransactionReport, Symbol, TransactionDefaults, TransactionRequest, TxForm, TxOp,
    Unique, Value, ValueType, View, encode_program, native_deployment_attribute,
};
use atomic_core::{BlockTransactor, BlockWriterOptions};
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use std::time::{Duration, Instant};

const KEY: u32 = 1000;
const SCORE: u32 = 1001;
const LABEL: u32 = 1002;
const MARKER: u32 = 1003;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            KEY,
            Keyword::new("item", "key"),
            ValueType::String,
            Cardinality::One,
        )
        .unique(Unique::Identity),
        Attribute::new(
            SCORE,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ),
        Attribute::new(
            LABEL,
            Keyword::new("item", "label"),
            ValueType::String,
            Cardinality::One,
        ),
        native_deployment_attribute(MARKER),
    ] {
        schema.install(attribute).unwrap();
    }
    schema
}

fn fixture(
    label: &str,
) -> Option<(
    common::PostgresFixture,
    PostgresConnectionConfig,
    BlockDatabase,
)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let database = BlockDatabase::create(&config, label, schema()).unwrap();
    assert_eq!(BlockDatabase::resolve(&config, label).unwrap(), database);
    Some((fixture, config, database))
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}
fn score(db: &DatabaseValue, entity: u64) -> Vec<Value> {
    db.values(entity, SCORE).unwrap()
}
fn same_receipt(actual: &ServiceTransactionReport, expected: &ServiceTransactionReport) {
    assert!(actual.replayed);
    assert_eq!(actual.basis_t, expected.basis_t);
    assert_eq!(actual.tx_hash, expected.tx_hash);
    assert_eq!(actual.tempids, expected.tempids);
    assert_eq!(actual.tx_data, expected.tx_data);
    assert_eq!(actual.db_before.basis_t(), expected.db_before.basis_t());
    assert_eq!(actual.db_after.basis_t(), expected.db_after.basis_t());
}

#[test]
fn create_preserves_initial_schema_transaction_and_empty_genesis_contract() {
    let Some((_fixture, config, nonempty)) = fixture("block_create_contract") else {
        return;
    };
    let empty = BlockDatabase::create(&config, "empty", Schema::new()).unwrap();
    let reader = BlockReader::connect(&config, Default::default()).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    for (database, oracle, expected_basis) in [
        (nonempty, Database::new(schema()).unwrap(), 1),
        (empty, Database::new(Schema::new()).unwrap(), 0),
    ] {
        assert_eq!(oracle.basis_t(), expected_basis);
        let snapshot = reader.capture(&database.reference_key()).unwrap();
        let value = snapshot.database_value();
        common::assert_same_information(&value, &oracle);
        assert_eq!(
            value.last_tx_instant(),
            oracle.database_value().last_tx_instant()
        );
        let publication = store.read_ref(&database.reference_key()).unwrap().unwrap();
        let id = publication
            .value
            .as_ref()
            .unwrap()
            .as_slice()
            .try_into()
            .unwrap();
        let root = DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
        let indexes = root.indexes.unwrap();
        assert_eq!(
            IndexDescriptor::decode(&indexes, &store.get(indexes).unwrap().unwrap())
                .unwrap()
                .basis,
            expected_basis
        );
        let log = snapshot.log();
        assert_eq!(log.basis_t(), expected_basis);
        assert_eq!(log.tx_data(IndexTransaction::T(0)).unwrap(), None);
        let transactions = log
            .tx_range(None, None)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert_eq!(transactions.len() as u64, expected_basis);
        let history = oracle.datoms(View::History, IndexOrder::Eavt);
        for transaction in transactions {
            let expected = history
                .iter()
                .filter(|d| atomic_core::tx_to_t(d.tx).unwrap() == transaction.t)
                .cloned()
                .collect::<Vec<_>>();
            assert_eq!(transaction.data, expected);
        }
        if expected_basis == 0 {
            assert!(root.log.is_none());
        } else {
            assert!(root.log.is_some());
            assert_eq!(value.entid(&Keyword::new("item", "key")), Some(KEY as u64));
        }
        drop((log, value));
        drop(snapshot);
    }
}

#[test]
fn typed_edn_identity_and_exact_receipts_survive_new_writes_and_writer_reopen() {
    let Some((_fixture, config, database)) = fixture("block_transactions") else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    let initial_epoch = writer.writer_epoch();
    let initial_basis = writer.db().unwrap().basis_t();
    assert_eq!(initial_basis, 1);
    let first_request = TransactionRequest::new(
        "typed",
        vec![
            add(
                EntityRef::Temp("one".into()),
                KEY,
                Value::String("one".into()),
            ),
            add(EntityRef::Temp("one".into()), SCORE, Value::Long(1)),
            add(
                EntityRef::Temp("one".into()),
                LABEL,
                Value::String("first".into()),
            ),
        ],
    )
    .comparing_basis(initial_basis)
    .with_tx_instant(1000);
    let first = writer.transact(&first_request).unwrap();
    let entity = first.tempids["one"];
    assert!(!first.replayed);
    assert_eq!(first.basis_t, initial_basis + 1);
    let request = TransactionRequest::from_edn(
        "edn",
        r#"[{:db/id "upsert" :item/key "one" :item/score 2 :item/label "second"}]"#,
    )
    .unwrap()
    .comparing_basis(first.basis_t)
    .with_tx_instant(2000);
    let second = writer.transact(&request).unwrap();
    assert_eq!(
        second.tempids["upsert"], entity,
        "unique identity upserts preserve the permanent entity"
    );
    let canonical = TransactionRequest::from_edn(
        "edn",
        r#"[ ; same request, different text/map ordering
        {:item/label "second", :item/score 2 :item/key "one" :db/id "upsert"}]"#,
    )
    .unwrap()
    .comparing_basis(first.basis_t)
    .with_tx_instant(2000);
    same_receipt(&writer.transact(&canonical).unwrap(), &second);
    let changed = TransactionRequest::from_edn(
        "edn",
        r#"[{:db/id "upsert" :item/key "one" :item/score 99 :item/label "second"}]"#,
    )
    .unwrap()
    .comparing_basis(first.basis_t)
    .with_tx_instant(2000);
    assert_eq!(
        writer.transact(&changed).unwrap_err().code,
        "postgres/idempotency-key-reused"
    );
    let third = writer
        .transact(
            &TransactionRequest::new(
                "third",
                vec![TxOp::Cas {
                    entity: EntityRef::Id(entity),
                    attribute: SCORE,
                    old: Some(Value::Long(2).into()),
                    new: Value::Long(3).into(),
                }],
            )
            .comparing_basis(second.basis_t)
            .with_tx_instant(3000),
        )
        .unwrap();
    assert_eq!(third.basis_t, initial_basis + 3);
    let rejected = TransactionRequest::new(
        "stale-value",
        vec![TxOp::Cas {
            entity: EntityRef::Id(entity),
            attribute: SCORE,
            old: Some(Value::Long(2).into()),
            new: Value::Long(4).into(),
        }],
    )
    .with_tx_instant(3500);
    assert_eq!(
        writer.transact(&rejected).unwrap_err().code,
        "transaction/cas-failed"
    );
    assert_eq!(writer.db().unwrap().basis_t(), third.basis_t);
    assert!(score(&first.db_before, entity).is_empty());
    assert_eq!(score(&first.db_after, entity), vec![Value::Long(1)]);
    assert_eq!(score(&second.db_before, entity), vec![Value::Long(1)]);
    assert_eq!(score(&second.db_after, entity), vec![Value::Long(2)]);
    writer.renew().unwrap();
    writer.release().unwrap();

    let mut writer =
        BlockTransactor::claim(&config, database, BlockWriterOptions::default()).unwrap();
    assert!(writer.writer_epoch() > initial_epoch);
    let replay = writer.transact(&canonical).unwrap();
    same_receipt(&replay, &second);
    assert_eq!(score(&replay.db_before, entity), vec![Value::Long(1)]);
    assert_eq!(score(&replay.db_after, entity), vec![Value::Long(2)]);
    assert_eq!(score(&writer.db().unwrap(), entity), vec![Value::Long(3)]);
    let installed = writer
        .transact(
            &TransactionRequest::new(
                "schema",
                vec![TxOp::InstallAttribute(Attribute::new(
                    1004,
                    Keyword::new("item", "active"),
                    ValueType::Boolean,
                    Cardinality::One,
                ))],
            )
            .with_tx_instant(4000),
        )
        .unwrap();
    assert_eq!(
        installed.db_after.entid(&Keyword::new("item", "active")),
        Some(1004)
    );
    assert_eq!(first.db_after.entid(&Keyword::new("item", "active")), None);
    writer.release().unwrap();
    assert_eq!(
        score(&first.db_after, entity),
        vec![Value::Long(1)],
        "retained receipt values outlive both writers"
    );
}

#[test]
fn saved_outcomes_precede_current_basis_defaults_and_capacity_admission() {
    let Some((_fixture, config, database)) = fixture("block_receipt_first") else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    let installed = writer
        .transact(
            &TransactionRequest::from_edn(
                "partition",
                r#"[{:db/id "orders" :db/ident :part/orders :db.install/_partition :db.part/db}]"#,
            )
            .unwrap()
            .with_tx_instant(1000),
        )
        .unwrap();
    let partition = installed
        .db_after
        .entid(&Keyword::new("part", "orders"))
        .unwrap();
    writer.release().unwrap();
    let mut options = BlockWriterOptions::default();
    options.execution.defaults =
        TransactionDefaults::default().with_default_partition(Keyword::new("part", "orders"));
    let mut writer = BlockTransactor::claim(&config, database.clone(), options).unwrap();
    let request = TransactionRequest::from_edn(
        "ordered",
        r#"[{:db/id "order" :item/key "order" :item/score 7}]"#,
    )
    .unwrap()
    .comparing_basis(installed.basis_t)
    .with_tx_instant(2000);
    let committed = writer.transact(&request).unwrap();
    assert_eq!(
        atomic_core::eid_to_part(committed.tempids["order"]).unwrap() as u64,
        atomic_core::eid_to_eidx(partition).unwrap()
    );
    writer
        .transact(
            &TransactionRequest::new(
                "later",
                vec![add(
                    EntityRef::Id(committed.tempids["order"]),
                    SCORE,
                    Value::Long(8),
                )],
            )
            .with_tx_instant(3000),
        )
        .unwrap();
    writer.release().unwrap();

    let mut denied = BlockWriterOptions::default();
    denied.capacity.max_transaction_ops = 0;
    denied.capacity.max_transaction_bytes = 0;
    denied.capacity.max_transaction_read_datoms = 0;
    denied.capacity.max_transaction_read_bytes = 0;
    denied.capacity.max_history_transactions = 0;
    denied.execution.defaults =
        TransactionDefaults::default().with_default_partition(Keyword::new("part", "missing"));
    let mut writer = BlockTransactor::claim(&config, database.clone(), denied).unwrap();
    let replay = writer.transact(&request).unwrap();
    same_receipt(&replay, &committed);
    assert_eq!(
        score(&replay.db_after, committed.tempids["order"]),
        vec![Value::Long(7)]
    );
    let mut changed = request.clone();
    changed.compare_basis_t = Some(0);
    assert_eq!(
        writer.transact(&changed).unwrap_err().code,
        "postgres/idempotency-key-reused",
        "digest mismatch also precedes current capacity/default validation"
    );
    let fresh = TransactionRequest::from_edn("fresh", r#"[{:item/score 9}]"#).unwrap();
    assert_eq!(
        writer.transact(&fresh).unwrap_err().code,
        "storage/transaction-capacity"
    );
    writer.release().unwrap();
    let mut options = BlockWriterOptions::default();
    options.execution.defaults =
        TransactionDefaults::default().with_default_partition(Keyword::new("part", "missing"));
    let mut writer = BlockTransactor::claim(&config, database, options).unwrap();
    same_receipt(&writer.transact(&request).unwrap(), &committed);
    assert_eq!(
        writer.transact(&fresh).unwrap_err().code,
        "transaction/default-partition-not-found"
    );
    writer.release().unwrap();
}

#[test]
fn authenticated_receipt_tempids_must_be_strictly_below_the_exact_log_frontier() {
    let Some((_fixture, config, database)) = fixture("block_receipt_frontier") else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    let request = TransactionRequest::new(
        "boundary",
        vec![add(EntityRef::Temp("item".into()), SCORE, Value::Long(7))],
    )
    .with_tx_instant(1000);
    let committed = writer.transact(&request).unwrap();
    let frontier = committed.db_after.eidx_frontier();
    let key = scoped_request_key(&database.identity, &request.request_key).unwrap();
    let reference_key = database.reference_key();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let current = store.read_ref(&reference_key).unwrap().unwrap();
    let root_id = current
        .value
        .as_ref()
        .unwrap()
        .as_slice()
        .try_into()
        .unwrap();
    let mut root = DatabaseRoot::decode(&root_id, &store.get(root_id).unwrap().unwrap()).unwrap();
    let receipt_id = RequestIndex::from_root(root.receipts)
        .lookup(&mut store, key)
        .unwrap()
        .unwrap();
    let original = ExactReceipt::load(&mut store, receipt_id).unwrap();
    let metadata_id = root.metadata.unwrap();
    let reserved = atomic_core::storage::SnapshotMetadata::decode(
        &metadata_id,
        &store.get(metadata_id).unwrap().unwrap(),
    )
    .unwrap()
    .reserved_frontier;
    assert!(
        reserved < frontier,
        "Fixture distinguishes reserved and ordinary issuance"
    );
    for (partition, eidx, accepted) in [
        (atomic_core::USER_PARTITION, frontier - 1, true),
        (atomic_core::USER_PARTITION, frontier, false),
        (atomic_core::DB_PARTITION, reserved - 1, true),
        (atomic_core::DB_PARTITION, reserved, false),
        (atomic_core::DB_PARTITION, frontier, false),
        (
            atomic_core::eid_to_part(atomic_core::implicit_part(0).unwrap()).unwrap(),
            frontier,
            false,
        ),
    ] {
        let mut forged = original.clone();
        let entity = atomic_core::make_eid(partition, eidx).unwrap();
        forged.tempids.insert("boundary-alias".into(), entity);
        // This is valid receipt syntax with an authenticated object hash. Only
        // the referenced log's exact frontier can reject its semantic claim.
        let forged_id = forged.put(&mut store).unwrap();
        root.receipts = RequestIndex::empty()
            .insert(&mut store, key, forged_id)
            .unwrap()
            .root();
        let next = store.put(&root.encode().unwrap()).unwrap();
        let revision = store.read_ref(&reference_key).unwrap().unwrap().revision;
        assert!(matches!(
            store
                .compare_exchange(&reference_key, Some(revision), Some(&next))
                .unwrap(),
            CasOutcome::Applied(_)
        ));
        if accepted {
            let replay = writer.transact(&request).unwrap();
            assert!(replay.replayed);
            assert_eq!(replay.tempids["boundary-alias"], entity);
            assert_eq!(replay.tx_hash, committed.tx_hash);
        } else {
            assert_eq!(
                writer.transact(&request).unwrap_err().code,
                "storage/receipt-tempids"
            );
        }
    }
    writer.release().unwrap();
}

#[test]
fn persisted_and_native_programs_keep_binding_validation_and_receipt_first_execution() {
    let Some((_fixture, config, database)) = fixture("block_transaction_programs") else {
        return;
    };
    let calls = Arc::new(AtomicUsize::new(0));
    let observed = calls.clone();
    let mut registry = NativeRegistry::builder();
    registry
        .transaction(
            Symbol::new("test.tx.v1", "increment"),
            move |db, args, budget| {
                budget.check(1)?;
                observed.fetch_add(1, Ordering::SeqCst);
                let [RuntimeValue::Scalar(Value::Ref(entity))] = args else {
                    panic!("expected entity")
                };
                let values = db.values(*entity, SCORE)?;
                let [Value::Long(old)] = values.as_slice() else {
                    panic!("expected score")
                };
                Ok(vec![TxForm::Op(add(
                    EntityRef::Id(*entity),
                    SCORE,
                    Value::Long(old + 1),
                ))])
            },
        )
        .unwrap();
    registry
        .attribute_predicate(Symbol::new("test.tx.v1", "positive"), |value, budget| {
            budget.check(1)?;
            Ok(RuntimeValue::Scalar(Value::Bool(
                matches!(value, Value::Long(n) if *n > 0),
            )))
        })
        .unwrap();
    let mut options = BlockWriterOptions::default();
    options.execution.native = registry.build();
    let mut writer = BlockTransactor::claim(&config, database.clone(), options).unwrap();
    let program = |text: &str| Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::String(text.into())),
            Instruction::EmitAdd(LABEL),
            Instruction::Return,
        ],
    };
    let mut store = PgBlockStore::connect(&config).unwrap();
    let original = store
        .put(&encode_program(&program("original")).unwrap())
        .unwrap();
    let replacement = store
        .put(&encode_program(&program("replacement")).unwrap())
        .unwrap();
    let mut score_attribute = schema().attribute(SCORE).unwrap().clone();
    score_attribute.predicates = vec!["checks/positive".into()];
    let binding = writer
        .transact(
            &TransactionRequest::new(
                "bindings",
                vec![
                    add(EntityRef::Temp("item".into()), SCORE, Value::Long(7)),
                    add(
                        EntityRef::Temp("function".into()),
                        atomic_core::DB_IDENT as u32,
                        Value::Keyword(Keyword::new("test", "rename")),
                    ),
                    add(
                        EntityRef::Temp("function".into()),
                        atomic_core::DB_FN as u32,
                        Value::Function(original),
                    ),
                    add(
                        EntityRef::Temp("predicate".into()),
                        atomic_core::DB_IDENT as u32,
                        Value::Keyword(Keyword::new("checks", "positive")),
                    ),
                    add(
                        EntityRef::Temp("predicate".into()),
                        MARKER,
                        Value::Symbol(Symbol::new("test.tx.v1", "positive")),
                    ),
                    TxOp::AlterAttribute(score_attribute),
                ],
            )
            .with_tx_instant(1000),
        )
        .unwrap();
    let entity = binding.tempids["item"];
    let request = TransactionRequest::from_edn(
        "persisted",
        &format!("[[:test/rename #atomic/ref {entity}]]"),
    )
    .unwrap()
    .with_tx_instant(2000);
    let original_report = writer.transact(&request).unwrap();
    assert_eq!(
        original_report.db_after.values(entity, LABEL).unwrap(),
        vec![Value::String("original".into())]
    );
    writer
        .transact(
            &TransactionRequest::new(
                "rebind",
                vec![add(
                    EntityRef::Id(binding.tempids["function"]),
                    atomic_core::DB_FN as u32,
                    Value::Function(replacement),
                )],
            )
            .with_tx_instant(3000),
        )
        .unwrap();
    same_receipt(&writer.transact(&request).unwrap(), &original_report);
    let mut new_call = request.clone();
    new_call.request_key = "new-persisted".into();
    new_call.tx_instant_override = Some(4000);
    assert_eq!(
        writer
            .transact(&new_call)
            .unwrap()
            .db_after
            .values(entity, LABEL)
            .unwrap(),
        vec![Value::String("replacement".into())]
    );
    let native = TransactionRequest::from_forms(
        "native",
        vec![TxForm::ProgramCall(ProgramCall {
            function: CallableRef::Local(Symbol::new("test.tx.v1", "increment")),
            arguments: vec![RuntimeValue::Scalar(Value::Ref(entity))],
        })],
    )
    .with_tx_instant(5000);
    let native_report = writer.transact(&native).unwrap();
    assert_eq!(
        score(&native_report.db_before, entity),
        vec![Value::Long(7)]
    );
    assert_eq!(score(&native_report.db_after, entity), vec![Value::Long(8)]);
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    same_receipt(&writer.transact(&native).unwrap(), &native_report);
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    let invalid = TransactionRequest::new(
        "invalid",
        vec![add(EntityRef::Id(entity), SCORE, Value::Long(-1))],
    )
    .with_tx_instant(6000);
    assert_eq!(
        writer.transact(&invalid).unwrap_err().code,
        "transaction/attribute-predicate"
    );
    assert_eq!(writer.db().unwrap().basis_t(), native_report.basis_t);
    writer.release().unwrap();
    let mut writer =
        BlockTransactor::claim(&config, database, BlockWriterOptions::default()).unwrap();
    same_receipt(&writer.transact(&native).unwrap(), &native_report);
    same_receipt(&writer.transact(&request).unwrap(), &original_report);
    let mut fresh = native.clone();
    fresh.request_key = "missing-deployment".into();
    fresh.tx_instant_override = Some(7000);
    assert_eq!(
        writer.transact(&fresh).unwrap_err().code,
        "native/deployment-not-found"
    );
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    writer.release().unwrap();
}

#[test]
fn measured_complete_small_writes_on_populated_database_include_publication_and_exact_retry() {
    let Some((_fixture, config, database)) = fixture("block_transaction_cost") else {
        return;
    };
    let mut options = BlockWriterOptions {
        lease_duration: Duration::from_secs(120),
        ..Default::default()
    };
    options.reads.max_recent_transactions = 128;
    let started = Instant::now();
    let mut writer = BlockTransactor::claim(&config, database.clone(), options.clone()).unwrap();
    let mut data = Vec::with_capacity(4096);
    for n in 0..2048 {
        let entity = EntityRef::Temp(format!("item-{n}"));
        data.push(add(entity.clone(), KEY, Value::String(format!("key-{n}"))));
        data.push(add(entity, SCORE, Value::Long(n)));
    }
    let seed = writer
        .transact(&TransactionRequest::new("seed", data).with_tx_instant(1000))
        .unwrap();
    let entity = seed.tempids["item-1024"];
    let populated_ms = started.elapsed().as_millis();
    let mut first = None;
    let mut first_request = None;
    let mut first_half_nanos = 0;
    let mut second_half_nanos = 0;
    let operation = OperationContext::new(OperationKind::Transaction);
    let scope = operation.enter();
    let writes_started = Instant::now();
    for n in 0..100 {
        let request = TransactionRequest::new(
            format!("small-{n}"),
            vec![add(EntityRef::Id(entity), SCORE, Value::Long(10_000 + n))],
        )
        .with_tx_instant(2000 + n);
        let attempt = Instant::now();
        let report = writer.transact(&request).unwrap();
        if n < 50 {
            first_half_nanos += attempt.elapsed().as_nanos();
        } else {
            second_half_nanos += attempt.elapsed().as_nanos();
        }
        assert_eq!(report.basis_t, seed.basis_t + n as u64 + 1);
        assert!(
            report.tx_data.len() <= 3,
            "small writes must not serialize the database"
        );
        if n == 0 {
            first = Some(report);
            first_request = Some(request);
        }
    }
    let write_ms = writes_started.elapsed().as_millis();
    drop(scope);
    let sql = operation.snapshot();
    assert!(sql.sql_calls > 0);
    assert!(sql.known_payload_write_bytes > 0);
    assert_eq!(
        score(&writer.db().unwrap(), entity),
        vec![Value::Long(10_099)]
    );
    writer.release().unwrap();
    let reopen_started = Instant::now();
    let mut writer = BlockTransactor::claim(&config, database, options).unwrap();
    let replay = writer.transact(first_request.as_ref().unwrap()).unwrap();
    same_receipt(&replay, first.as_ref().unwrap());
    assert_eq!(score(&replay.db_after, entity), vec![Value::Long(10_000)]);
    eprintln!(
        "BLOCK_TRANSACTION_SAMPLE entities=2048 small_writes=100 populate_ms={populated_ms} writes_complete_ms={write_ms} first50_ns={first_half_nanos} second50_ns={second_half_nanos} sql_calls={} payload_written_bytes={} reopen_retry_complete_ms={}",
        sql.sql_calls,
        sql.known_payload_write_bytes,
        reopen_started.elapsed().as_millis()
    );
    writer.release().unwrap();
}

#[test]
fn chunked_transaction_crosses_upload_groups_and_reopens_with_exact_receipt() {
    let Some((_fixture, config, database)) = fixture("block_transaction_grouped") else {
        return;
    };
    // Exceed the staging target and one canonical log chunk. Repeated text
    // keeps the fixture cheap; this is not a compression or RSS benchmark.
    let payload = "x".repeat(5 * 1024 * 1024);
    let request = TransactionRequest::new(
        "multiple-upload-groups",
        vec![add(
            EntityRef::Temp("large".into()),
            LABEL,
            Value::String(payload.clone()),
        )],
    )
    .with_tx_instant(1000);
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    let started = Instant::now();
    let report = writer.transact(&request).unwrap();
    let entity = report.tempids["large"];
    assert!(report.db_before.values(entity, LABEL).unwrap().is_empty());
    assert_eq!(
        report.db_after.values(entity, LABEL).unwrap(),
        vec![Value::String(payload.clone())]
    );
    // An independent connection sees all canonical chunks after acknowledgement.
    let mut inspect = PgBlockStore::connect(&config).unwrap();
    let bytes = inspect.get(report.tx_hash).unwrap().unwrap();
    let entry = atomic_core::storage::root::Block::decode(&report.tx_hash, &bytes).unwrap();
    assert_eq!(entry.kind, atomic_core::storage::log::LOG_ENTRY_KIND);
    assert_eq!(entry.links.len(), 2);
    for id in &entry.links {
        let chunk = inspect.get(*id).unwrap().unwrap();
        assert_eq!(
            atomic_core::storage::root::Block::decode(id, &chunk)
                .unwrap()
                .kind,
            atomic_core::storage::log::LOG_CHUNK_KIND
        );
    }
    writer.release().unwrap();
    let mut writer =
        BlockTransactor::claim(&config, database, BlockWriterOptions::default()).unwrap();
    let replay = writer.transact(&request).unwrap();
    same_receipt(&replay, &report);
    assert_eq!(replay.tx_data, report.tx_data);
    assert_eq!(
        replay.db_after.values(entity, LABEL).unwrap(),
        vec![Value::String(payload)]
    );
    assert!(replay.db_before.values(entity, LABEL).unwrap().is_empty());
    eprintln!(
        "GROUPED_TRANSACTION_OK payload_bytes={} chunks=2 commit_reopen_retry_us={}",
        5 * 1024 * 1024,
        started.elapsed().as_micros()
    );
    writer.release().unwrap();
}

/// Fixture construction only: build from one exact value, then atomically
/// replace only the publication's index pointer. Production indexing is not
/// supplied or emulated by this helper.
fn index_fixture_value(
    store: &mut PgBlockStore,
    database: &BlockDatabase,
    value: &DatabaseValue,
) -> (u64, usize) {
    let reference_key = database.reference_key();
    let current = store.read_ref(&reference_key).unwrap().unwrap();
    let id = current
        .value
        .as_ref()
        .unwrap()
        .as_slice()
        .try_into()
        .unwrap();
    let before = DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap();
    assert_eq!(before.identity, database.identity);
    assert_eq!(before.basis, value.basis_t());
    let old_index = before.indexes.unwrap();
    let generation = IndexDescriptor::decode(&old_index, &store.get(old_index).unwrap().unwrap())
        .unwrap()
        .generation;
    let tree_config = TreeConfig {
        max_leaf_datoms: 128,
        target_leaf_bytes: 16 * 1024,
        ..TreeConfig::default()
    };
    let mut trees = Vec::with_capacity(8);
    let mut bytes = 0u64;
    let mut nodes = 0;
    let mut seen = std::collections::BTreeSet::new();
    for history in [false, true] {
        let source = if history {
            value.clone().history()
        } else {
            value.clone()
        };
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let built = build_tree(
                order,
                history,
                source.collect_datoms(order).unwrap(),
                &tree_config,
            )
            .unwrap();
            for (hash, encoded) in built.nodes.iter() {
                assert_eq!(store.put(encoded).unwrap(), *hash);
                if seen.insert(*hash) {
                    bytes += encoded.len() as u64;
                    nodes += 1;
                }
            }
            trees.push(built.descriptor);
        }
    }
    let indexes = store
        .put(
            &IndexDescriptor {
                identity: database.identity,
                basis: value.basis_t(),
                generation,
                trees,
                pending_avet: vec![],
                avet_work: vec![],
                fulltext: None,
            }
            .encode()
            .unwrap(),
        )
        .unwrap();
    let after = before.adopt_indexes(before.writer_epoch, indexes).unwrap();
    assert_eq!(after.log, before.log);
    assert_eq!(after.receipts, before.receipts);
    assert_eq!(after.metadata, before.metadata);
    assert_eq!(after.basis, before.basis);
    assert_eq!(after.writer_epoch, before.writer_epoch);
    let next = store.put(&after.encode().unwrap()).unwrap();
    assert!(matches!(
        store
            .compare_exchange(&reference_key, Some(current.revision), Some(&next))
            .unwrap(),
        CasOutcome::Applied(_)
    ));
    (bytes, nodes)
}

#[test]
fn indexed_small_writes_and_reopened_receipts_are_selective_after_index_only_publication() {
    let Some((_fixture, config, database)) = fixture("block_indexed_transactions") else {
        return;
    };
    let mut options = BlockWriterOptions {
        lease_duration: Duration::from_secs(120),
        ..Default::default()
    };
    options.reads.max_recent_transactions = 16;
    let mut writer = BlockTransactor::claim(&config, database.clone(), options.clone()).unwrap();
    let mut data = Vec::with_capacity(8192);
    for n in 0..4096 {
        let entity = EntityRef::Temp(format!("indexed-{n}"));
        data.push(add(
            entity.clone(),
            KEY,
            Value::String(format!("key-{n:04}")),
        ));
        data.push(add(entity, SCORE, Value::Long(n)));
    }
    let request = TransactionRequest::new("indexed-seed", data).with_tx_instant(1000);
    let original = writer.transact(&request).unwrap();
    let entity = original.tempids["indexed-2048"];
    let probe_entity = original.tempids["indexed-1234"];
    writer.release().unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let (index_bytes, index_nodes) = index_fixture_value(&mut store, &database, &original.db_after);
    assert!(index_bytes > 512 * 1024);
    assert!(
        index_nodes > 100,
        "fixture must contain many selective leaves, not eight single-leaf roots"
    );
    assert!(score(&original.db_before, entity).is_empty());
    assert_eq!(score(&original.db_after, entity), vec![Value::Long(2048)]);

    for (round, cache_bytes) in [64 * 1024, 256 * 1024].into_iter().enumerate() {
        options.reads.cache_bytes = cache_bytes;
        options.reads.cache_entries = 64;
        let operation = OperationContext::new(OperationKind::Transaction);
        let scope = operation.enter();
        let started = Instant::now();
        let mut writer =
            BlockTransactor::claim(&config, database.clone(), options.clone()).unwrap();
        let update = TransactionRequest::new(
            format!("indexed-small-{round}"),
            vec![add(
                EntityRef::Id(entity),
                SCORE,
                Value::Long(9000 + round as i64),
            )],
        )
        .with_tx_instant(2000 + round as i64);
        let committed = writer.transact(&update).unwrap();
        assert_eq!(committed.tx_data.len(), 3);
        let append_ms = started.elapsed().as_millis();
        drop(scope);
        let append_io = operation.snapshot();
        assert!(
            append_io.known_payload_read_bytes < index_bytes / 2,
            "small append read {} bytes out of {index_bytes} indexed bytes",
            append_io.known_payload_read_bytes
        );
        assert!(
            append_io.known_payload_write_bytes < 64 * 1024,
            "small append writes touched objects, not the indexed database"
        );
        writer.release().unwrap();

        let operation = OperationContext::new(OperationKind::Recovery);
        let scope = operation.enter();
        let started = Instant::now();
        let mut reopened =
            BlockTransactor::claim(&config, database.clone(), options.clone()).unwrap();
        let replay = reopened.transact(&update).unwrap();
        same_receipt(&replay, &committed);
        assert_eq!(
            score(&replay.db_before, entity),
            vec![Value::Long(if round == 0 { 2048 } else { 9000 })]
        );
        assert_eq!(
            score(&replay.db_after, entity),
            vec![Value::Long(9000 + round as i64)]
        );
        let reopen_retry_ms = started.elapsed().as_millis();
        drop(scope);
        let retry_io = operation.snapshot();
        assert!(
            retry_io.known_payload_read_bytes < index_bytes / 2,
            "reopen plus exact retry must not scan the whole index"
        );
        if round == 0 {
            // The old receipt deliberately retains its original before/after
            // roots, even though the live publication now uses a newer index.
            let old = reopened.transact(&request).unwrap();
            same_receipt(&old, &original);
            assert!(score(&old.db_before, entity).is_empty());
            assert_eq!(score(&old.db_after, entity), vec![Value::Long(2048)]);
        }
        reopened.release().unwrap();

        let reader = BlockReader::connect(&config, options.reads.clone()).unwrap();
        let snapshot = reader.capture(&database.reference_key()).unwrap();
        assert_eq!(
            score(&snapshot.database_value(), probe_entity),
            vec![Value::Long(1234)]
        );
        let before_hits = reader.cache_stats().hits;
        assert_eq!(
            score(&snapshot.database_value(), probe_entity),
            vec![Value::Long(1234)]
        );
        let cache = reader.cache_stats();
        assert!(
            cache.hits > before_hits,
            "a repeated selective read uses the configured node cache"
        );
        assert!(cache.peak_bytes <= cache_bytes);
        drop(snapshot);
        eprintln!(
            "BLOCK_INDEXED_TRANSACTION_SAMPLE entities=4096 indexed_nodes={index_nodes} indexed_bytes={index_bytes} cache_limit_bytes={cache_bytes} append_complete_ms={append_ms} append_sql_calls={} append_read_bytes={} append_write_bytes={} reopen_retry_complete_ms={reopen_retry_ms} retry_sql_calls={} retry_read_bytes={} probe_cache_peak_bytes={} probe_cache_hits={}",
            append_io.sql_calls,
            append_io.known_payload_read_bytes,
            append_io.known_payload_write_bytes,
            retry_io.sql_calls,
            retry_io.known_payload_read_bytes,
            cache.peak_bytes,
            cache.hits
        );
    }
    assert_eq!(score(&original.db_after, entity), vec![Value::Long(2048)]);
}
