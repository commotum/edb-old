mod common;
use atomic_core::storage::ownership::{BlockCollector, CollectionPhase};
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::time::Duration;

fn cycle_at(collector: &mut BlockCollector, minimum_age: Duration) {
    for _ in 0..128 {
        if collector.advance(minimum_age, 4096).unwrap().phase == CollectionPhase::Complete {
            return;
        }
    }
    panic!("small deployment fixture did not finish collection");
}

fn cycle(collector: &mut BlockCollector) {
    cycle_at(collector, RECOMMENDED_GARBAGE_COLLECTION_AGE);
}

#[test]
fn staged_program_needs_no_live_handle_and_binding_retains_exact_code() {
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
    let leaf_hash = operator.deploy_program(&leaf).unwrap();
    let root = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::EmitCall {
                function: CallableRef::ExactHash(leaf_hash),
                argument_count: 0,
            },
            Instruction::Return,
        ],
    };

    // Staging handles transient collector contention internally.
    let gc_config = config.clone();
    let collector = std::thread::spawn(move || {
        let mut collector = BlockCollector::connect(&gc_config).unwrap();
        cycle(&mut collector);
    });
    let root_hash = operator.deploy_program(&root).unwrap();
    collector.join().unwrap();
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
    assert_eq!(root_hash, program_hash(&root).unwrap());
    let writer = common::start_service(&fixture.connection, "programs");
    let bound = operator
        .install_program(
            &writer.client(),
            "bind",
            Keyword::new("deployed", "run"),
            &root,
            std::slice::from_ref(&leaf),
            Duration::from_secs(20),
        )
        .unwrap();
    // Exact retries resolve the committed receipt before inspecting optional
    // deployment input. This malformed unused dependency must not be evaluated.
    let invalid_dependency = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![],
    };
    let retry = operator
        .install_program(
            &writer.client(),
            "bind",
            Keyword::new("deployed", "run"),
            &root,
            &[invalid_dependency],
            Duration::from_secs(20),
        )
        .unwrap();
    assert_eq!(retry.basis_t, bound.basis_t);
    assert!(retry.replayed);
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
    let orphan = operator.deploy_program(&unused).unwrap();
    cycle(&mut collector);
    cycle(&mut collector);
    assert!(
        store.get(orphan).unwrap().is_some(),
        "unbound staging respects the grace period"
    );

    cycle_at(&mut collector, Duration::ZERO);
    cycle_at(&mut collector, Duration::ZERO);
    assert!(
        store.get(orphan).unwrap().is_none(),
        "expired unbound staging is collected"
    );
    assert!(
        store
            .list_live_refs("staging/", None, 32)
            .unwrap()
            .is_empty()
    );

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
