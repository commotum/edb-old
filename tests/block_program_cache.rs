//! Actual source/service cache use; exact retries do not resolve executable code.
mod common;
use atomic_core::storage::{BlockDatabase, BlockReadConfig, BlockReader, PgBlockStore};
use atomic_core::{
    CallableRef, DatabaseCatalog, EntityRef, Instruction, Keyword, PostgresConnectionConfig,
    Program, ProgramCall, ProgramKind, ProgramOutput, Schema, TransactionRequest, TxOp, Value,
};
use std::time::{Duration, Instant};

const WAIT: Duration = Duration::from_secs(10);

fn bind(name: &str, hash: atomic_core::ProgramHash) -> Vec<TxOp> {
    [
        (
            atomic_core::DB_IDENT as u32,
            Value::Keyword(Keyword::new("fn", name)),
        ),
        (atomic_core::DB_FN as u32, Value::Function(hash)),
    ]
    .into_iter()
    .map(|(attribute, value)| TxOp::Add {
        entity: EntityRef::Temp(name.into()),
        attribute,
        value: value.into(),
    })
    .collect()
}

#[test]
fn shared_decoded_cache_is_bounded_and_never_supplies_open_or_retry_authority() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block program cache PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "block_program_cache");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let database = BlockDatabase::create(&config, "program-cache", Schema::new()).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let query = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(7)),
            Instruction::Return,
        ],
    };
    let transaction = Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![Instruction::Return],
    };
    let query_hash = store
        .put(&atomic_core::encode_program(&query).unwrap())
        .unwrap();
    let transaction_hash = store
        .put(&atomic_core::encode_program(&transaction).unwrap())
        .unwrap();
    let service = common::start_service(&fixture.connection, "program-cache");
    let mut bindings = bind("query", query_hash);
    bindings.extend(bind("transaction", transaction_hash));
    service
        .client()
        .transact(TransactionRequest::new("bindings", bindings), WAIT)
        .unwrap();
    let before_calls = service.program_cache_stats();
    assert_eq!(before_calls.decodes, 2);
    let request = |key: &str| {
        TransactionRequest::new(key, vec![]).calling(ProgramCall {
            function: CallableRef::Database(EntityRef::Ident(Keyword::new("fn", "transaction"))),
            arguments: vec![],
        })
    };
    let committed = service.client().transact(request("first"), WAIT).unwrap();
    service.client().transact(request("second"), WAIT).unwrap();
    let after_calls = service.program_cache_stats();
    assert_eq!(after_calls.decodes, before_calls.decodes);
    assert_eq!(after_calls.validations, before_calls.validations);
    assert!(after_calls.hits > before_calls.hits);

    let reader = BlockReader::connect(&config, BlockReadConfig::default()).unwrap();
    let captured = reader
        .capture(&database.reference_key())
        .unwrap()
        .database_value();
    let started = Instant::now();
    for _ in 0..8 {
        assert!(
            matches!(captured.invoke(Keyword::new("fn", "query"), &[], Default::default()).unwrap(),
            ProgramOutput::Query(rows) if rows == vec![vec![Value::Long(7)]])
        );
    }
    let stats = reader.program_cache_stats();
    assert_eq!(
        (stats.misses, stats.decodes, stats.validations, stats.hits),
        (1, 1, 1, 7)
    );
    assert_eq!(stats.current_entries, 1);
    eprintln!(
        "BLOCK_PROGRAM_CACHE invocations=8 elapsed_us={} stats={stats:?}",
        started.elapsed().as_micros()
    );
    reader.set_program_cache_limits(1, 4096);
    captured
        .invoke(Keyword::new("fn", "transaction"), &[], Default::default())
        .unwrap();
    assert_eq!(reader.program_cache_stats().current_entries, 1);
    assert_eq!(reader.program_cache_stats().evictions, 1);
    captured
        .invoke(Keyword::new("fn", "query"), &[], Default::default())
        .unwrap();
    assert_eq!(reader.program_cache_stats().decodes, 3);
    assert!(reader.program_cache_stats().current_bytes <= 4096);

    // An authenticated cached value is discardable local information; disabling
    // admission forces a cold read and detects corruption instead of caching it.
    service.shutdown();
    let mut admin = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
    let original: Vec<u8> = admin
        .query_one(
            "SELECT payload FROM atomic_objects WHERE id=$1",
            &[&&transaction_hash[..]],
        )
        .unwrap()
        .get(0);
    admin
        .execute(
            "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
            &[&&transaction_hash[..], &vec![0u8]],
        )
        .unwrap();
    let restarted = common::start_service(&fixture.connection, "program-cache");
    let replay = restarted.client().transact(request("first"), WAIT).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, committed.tx_hash);
    assert_eq!(restarted.program_cache_stats(), Default::default());
    assert!(restarted.client().transact(request("fresh"), WAIT).is_err());
    assert_eq!(restarted.program_cache_stats().decodes, 0);
    assert_eq!(restarted.program_cache_stats().current_entries, 0);
    admin
        .execute(
            "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
            &[&&transaction_hash[..], &original],
        )
        .unwrap();
    restarted.shutdown();

    // A warm program does not reopen a retired database. The previously held
    // immutable value remains readable within its GC grace period.
    DatabaseCatalog::connect_configured(&config)
        .unwrap()
        .retire("program-cache")
        .unwrap();
    assert_eq!(
        reader.capture(&database.reference_key()).unwrap_err().code,
        "catalog/database-retired"
    );
    captured
        .invoke(Keyword::new("fn", "query"), &[], Default::default())
        .unwrap();
    reader.set_program_cache_limits(0, 0);
    assert_eq!(reader.program_cache_stats().current_entries, 0);
    captured
        .invoke(Keyword::new("fn", "query"), &[], Default::default())
        .unwrap();
    assert_eq!(reader.program_cache_stats().current_entries, 0);
}
