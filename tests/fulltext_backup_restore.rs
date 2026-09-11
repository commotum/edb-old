//! Fulltext and transitive native programs remain usable through staged restore.
mod common;
use atomic_core::storage::ownership::{BlockCollector, CollectionPhase};
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::time::{Duration, Instant};
const WAIT: Duration = Duration::from_secs(30);

fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let url = std::env::var("ATOMIC_POSTGRES_URL").ok()?;
    let fixture = common::PostgresFixture::new(&url, label);
    PgBlockStore::install(&PostgresConnectionConfig::plaintext(&fixture.connection)).unwrap();
    Some(fixture)
}
fn hits(value: &DatabaseValue, attribute: u32, text: &str) -> usize {
    value
        .fulltext(attribute, text, &FulltextOptions::default())
        .unwrap()
        .hits
        .len()
}
fn collect(config: &PostgresConnectionConfig) {
    let mut collector = BlockCollector::connect(config).unwrap();
    for _ in 0..128 {
        if collector.advance(Duration::ZERO, 4096).unwrap().phase == CollectionPhase::Complete {
            return;
        }
    }
    panic!("bounded fixture collection did not finish");
}
fn same_receipt(actual: &ServiceTransactionReport, expected: &ServiceTransactionReport) {
    assert!(actual.replayed);
    assert_eq!(actual.basis_t, expected.basis_t);
    assert_eq!(actual.tx_hash, expected.tx_hash);
    assert_eq!(actual.tempids, expected.tempids);
    assert_eq!(actual.tx_data, expected.tx_data);
    assert_eq!(
        actual.db_before.snapshot_key(),
        expected.db_before.snapshot_key()
    );
    assert_eq!(
        actual.db_after.snapshot_key(),
        expected.db_after.snapshot_key()
    );
    for (actual, expected) in [
        (&actual.db_before, &expected.db_before),
        (&actual.db_after, &expected.db_after),
    ] {
        assert_eq!(
            actual.clone().history().datoms(IndexOrder::Eavt).unwrap(),
            expected.clone().history().datoms(IndexOrder::Eavt).unwrap()
        );
    }
}

#[test]
fn staged_program_dependencies_and_fulltext_survive_gc_restore_and_receipt_first_retries() {
    let Some(source) = fixture("fulltext_backup_source") else {
        return;
    };
    let target = fixture("fulltext_backup_target").unwrap();
    let source_config = PostgresConnectionConfig::plaintext(&source.connection);
    let target_config = PostgresConnectionConfig::plaintext(&target.connection);
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1000,
                Keyword::new("document", "body"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let mut private = Attribute::new(
        1001,
        Keyword::new("document", "private"),
        ValueType::String,
        Cardinality::One,
    )
    .fulltext();
    private.no_history = true;
    schema.install(private).unwrap();
    BlockDatabase::create(&source_config, "source", schema).unwrap();
    let mut source_store = PgBlockStore::connect(&source_config).unwrap();
    let leaf = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(17)),
            Instruction::EmitRow(1),
            Instruction::Return,
        ],
    };
    let leaf_bytes = encode_program(&leaf).unwrap();
    let leaf_id = source_store.put(&leaf_bytes).unwrap();
    let program = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Function(leaf_id)),
            Instruction::Pop,
            Instruction::PushConstant(Value::Long(17)),
            Instruction::EmitRow(1),
            Instruction::Return,
        ],
    };
    let program_bytes = encode_program(&program).unwrap();
    let program_id = source_store.put(&program_bytes).unwrap();
    let transaction = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::PushConstant(Value::String("callback-result".into())),
            Instruction::EmitAdd(DB_DOC as u32),
            Instruction::Return,
        ],
    };
    let transaction_id = source_store
        .put(&encode_program(&transaction).unwrap())
        .unwrap();
    let disabled = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushConstant(Value::Bool(false)),
            Instruction::Require {
                category: ErrorCategory::Incorrect,
                message: "callback disabled".into(),
            },
            Instruction::Return,
        ],
    };
    let disabled_id = source_store
        .put(&encode_program(&disabled).unwrap())
        .unwrap();
    let service = common::start_service(&source.connection, "source");
    let mut ops = (0..16)
        .flat_map(|n| {
            [
                TxOp::Add {
                    entity: EntityRef::Temp(format!("doc{n}")),
                    attribute: 1000,
                    value: Value::String(format!("durable amber document {n}")).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp(format!("doc{n}")),
                    attribute: 1001,
                    value: Value::String("secretold".into()).into(),
                },
            ]
        })
        .collect::<Vec<_>>();
    ops.extend([
        TxOp::Add {
            entity: EntityRef::Temp("program".into()),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("document", "query")).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("program".into()),
            attribute: DB_FN as u32,
            value: Value::Function(program_id).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("transaction".into()),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("document", "touch")).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("transaction".into()),
            attribute: DB_FN as u32,
            value: Value::Function(transaction_id).into(),
        },
    ]);
    let request = TransactionRequest::new("documents", ops).with_tx_instant(1000);
    let report = service.client().transact(request.clone(), WAIT).unwrap();
    service
        .client()
        .transact(
            TransactionRequest::new(
                "edit",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Id(report.tempids["doc0"]),
                        attribute: 1000,
                        value: Value::String("durable azure document".into()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Id(report.tempids["doc0"]),
                        attribute: 1001,
                        value: Value::String("secretnew".into()).into(),
                    },
                ],
            )
            .with_tx_instant(2000),
            WAIT,
        )
        .unwrap();
    let callback = TransactionRequest::from_edn(
        "callback",
        &format!("[[:document/touch #atomic/ref {}]]", report.tempids["doc0"]),
    )
    .unwrap()
    .with_tx_instant(3000);
    let callback_report = service.client().transact(callback.clone(), WAIT).unwrap();
    service
        .client()
        .transact(
            TransactionRequest::new(
                "disable-callback",
                vec![TxOp::Add {
                    entity: EntityRef::Id(report.tempids["transaction"]),
                    attribute: DB_FN as u32,
                    value: Value::Function(disabled_id).into(),
                }],
            )
            .with_tx_instant(4000),
            WAIT,
        )
        .unwrap();
    let connection = Connection::attach(&source.connection, service.client(), 0).unwrap();
    let index = connection.request_index().unwrap();
    connection.sync_index(index.target_t, WAIT).unwrap();
    let current = connection.db();
    assert_eq!(hits(&current, 1000, "amber"), 15);
    assert_eq!(hits(&current.clone().history(), 1000, "amber"), 16);
    assert_eq!(
        hits(&current.clone().as_of(report.basis_t), 1000, "amber"),
        16
    );
    assert_eq!(hits(&current.clone().history(), 1001, "secretold"), 15);
    let expected = current.clone().history().datoms(IndexOrder::Eavt).unwrap();
    drop(connection);
    service.shutdown();
    let directory = common::private_directory();
    let point = PortableBackup::connect_configured(&source_config)
        .unwrap()
        .backup_database("source", directory.path())
        .unwrap();
    let mut restore = PortableBackup::connect_configured(&target_config).unwrap();
    let start = Instant::now();
    let operation = OperationContext::new(OperationKind::Administration);
    let observed = operation.enter();
    assert_eq!(
        restore
            .restore_backup_with_fault(
                directory.path(),
                point.basis_t,
                "restored",
                RestoreFault::BeforeCommit
            )
            .unwrap_err()
            .code,
        "backup/restore-before-activation"
    );
    drop(observed);
    eprintln!(
        "RESTORE_STAGE elapsed_ms={} sql_calls={} read_bytes={} write_bytes={}",
        start.elapsed().as_millis(),
        operation.snapshot().sql_calls,
        operation.snapshot().known_payload_read_bytes,
        operation.snapshot().known_payload_write_bytes
    );
    collect(&target_config);
    let mut target_store = PgBlockStore::connect(&target_config).unwrap();
    assert_eq!(
        target_store.get(program_id).unwrap().unwrap(),
        program_bytes
    );
    assert_eq!(
        target_store.get(leaf_id).unwrap().unwrap(),
        leaf_bytes,
        "transitive program dependency is a completed owned child"
    );
    assert_eq!(
        restore
            .restore_backup_with_fault(
                directory.path(),
                point.basis_t,
                "restored",
                RestoreFault::AfterCommitBeforeResponse
            )
            .unwrap_err()
            .code,
        "backup/restore-after-activation"
    );
    let restored = Peer::connect(&target.connection, "restored", 0)
        .unwrap()
        .database_value();
    assert_eq!(
        restored.clone().history().datoms(IndexOrder::Eavt).unwrap(),
        expected
    );
    assert_eq!(hits(&restored, 1000, "amber"), 15);
    assert_eq!(hits(&restored, 1000, "azure"), 1);
    assert_eq!(hits(&restored.clone().history(), 1000, "amber"), 16);
    assert_eq!(hits(&restored.clone().history(), 1001, "secretold"), 15);
    assert!(matches!(
        ProgramRuntime
            .execute_query(
                &decode_program(&target_store.get(program_id).unwrap().unwrap()).unwrap(),
                &restored,
                &[],
                ProgramControl::default()
            )
            .unwrap(),
        ProgramOutput::Query(rows) if rows == vec![vec![Value::Long(17)]]
    ));
    let writer = common::start_service(&target.connection, "restored");
    let mut invoked = false;
    let again = restore
        .restore_backup_with_activation_probe(directory.path(), point.basis_t, "restored", || {
            invoked = true
        })
        .unwrap();
    assert!(
        !invoked,
        "completed restore resolves before active-writer admission or callbacks"
    );
    assert_eq!(again.basis_t(), point.basis_t);
    same_receipt(
        &writer.client().transact(request.clone(), WAIT).unwrap(),
        &report,
    );
    let changed = TransactionRequest::new("documents", vec![]).with_tx_instant(1000);
    assert_eq!(
        writer.client().transact(changed, WAIT).unwrap_err().code,
        "postgres/idempotency-key-reused"
    );
    same_receipt(
        &writer.client().transact(callback.clone(), WAIT).unwrap(),
        &callback_report,
    );
    let mut conflicting_callback = callback.clone();
    conflicting_callback.tx_instant_override = Some(5000);
    assert_eq!(
        writer
            .client()
            .transact(conflicting_callback, WAIT)
            .unwrap_err()
            .code,
        "postgres/idempotency-key-reused"
    );
    let mut new_callback = callback;
    new_callback.request_key = "new-callback".into();
    new_callback.tx_instant_override = Some(5000);
    assert_eq!(
        writer
            .client()
            .transact(new_callback, WAIT)
            .unwrap_err()
            .category,
        ErrorCategory::Incorrect,
        "new requests really reach the disabled callback"
    );
    writer.shutdown();
    collect(&target_config);
    let reopened = Peer::connect(&target.connection, "restored", 0)
        .unwrap()
        .database_value();
    assert_eq!(hits(&reopened, 1000, "amber"), 15);
    assert!(
        PostgresOperator::connect_configured(&target_config)
            .unwrap()
            .inspect_database(
                &DatabaseCatalog::connect_configured(&target_config)
                    .unwrap()
                    .resolve("restored")
                    .unwrap()
                    .database_id,
                true,
            )
            .unwrap()
            .healthy()
    );
}
