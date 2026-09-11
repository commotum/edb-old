mod common;
use atomic_core::storage::ownership::{BlockCollector, CollectionPhase};
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::time::Duration;

fn cycle(collector: &mut BlockCollector) {
    for _ in 0..128 {
        if collector.advance(Duration::ZERO, 4096).unwrap().phase == CollectionPhase::Complete {
            return;
        }
    }
    panic!("small deployment fixture did not finish collection");
}

#[test]
fn protected_deployment_survives_concurrent_collection_then_binding_owns_exact_code() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "program_deployment");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    BlockDatabase::create(&config, "programs", Schema::new()).unwrap();
    let leaf = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![Instruction::Return],
    };
    let mut operator = PostgresOperator::connect_configured(&config).unwrap();
    let leaf_handle = operator.deploy_program(&leaf).unwrap();
    let root = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::EmitCall {
                function: CallableRef::ExactHash(leaf_handle.hash()),
                argument_count: 0,
            },
            Instruction::Return,
        ],
    };

    // The collector is genuinely concurrent with root preparation. A seal may
    // reject an attempt; retrying authenticates and protects the closure again.
    let gc_config = config.clone();
    let collector = std::thread::spawn(move || {
        let mut collector = BlockCollector::connect(&gc_config).unwrap();
        cycle(&mut collector);
    });
    let root_handle = loop {
        match operator.deploy_program(&root) {
            Ok(handle) => break handle,
            Err(error) if error.category == ErrorCategory::Conflict => continue,
            Err(error) => panic!("deployment failed: {error:?}"),
        }
    };
    collector.join().unwrap();
    leaf_handle.release().unwrap();
    let mut collector = BlockCollector::connect(&config).unwrap();
    cycle(&mut collector);
    let mut store = PgBlockStore::connect(&config).unwrap();
    for program in [&root, &leaf] {
        let bytes = encode_program(program).unwrap();
        assert_eq!(
            store.get(program_hash(program).unwrap()).unwrap(),
            Some(bytes.clone())
        );
        assert_eq!(
            encode_program(&decode_program(&bytes).unwrap()).unwrap(),
            bytes
        );
    }
    let hash = root_handle.hash();
    let writer = common::start_service(&fixture.connection, "programs");
    let bound = writer
        .client()
        .transact(
            TransactionRequest::new(
                "bind",
                vec![
                    TxOp::Add {
                        entity: EntityRef::Temp("function".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("deployed", "run")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("function".into()),
                        attribute: DB_FN as u32,
                        value: Value::Function(hash).into(),
                    },
                ],
            ),
            Duration::from_secs(20),
        )
        .unwrap();
    root_handle.release().unwrap();
    cycle(&mut collector);
    writer.shutdown();
    let reopened = common::start_service(&fixture.connection, "programs");
    let called = reopened
        .client()
        .transact(
            TransactionRequest::from_forms(
                "call",
                vec![TxForm::ProgramCall(ProgramCall {
                    function: CallableRef::Database(EntityRef::Ident(Keyword::new(
                        "deployed", "run",
                    ))),
                    arguments: vec![],
                })],
            ),
            Duration::from_secs(20),
        )
        .unwrap();
    assert_eq!(called.basis_t, bound.basis_t + 1);
    reopened.shutdown();

    let unused = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(42)),
            Instruction::Pop,
            Instruction::Return,
        ],
    };
    let unused = operator.deploy_program(&unused).unwrap();
    let orphan = unused.hash();
    unused.release().unwrap();
    cycle(&mut collector);
    cycle(&mut collector);
    assert!(store.get(orphan).unwrap().is_none());

    let missing = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::EmitCall {
                function: CallableRef::ExactHash([0x55; 32]),
                argument_count: 0,
            },
            Instruction::Return,
        ],
    };
    assert_eq!(
        operator.deploy_program(&missing).unwrap_err().code,
        "program/dependency-not-found"
    );
}
