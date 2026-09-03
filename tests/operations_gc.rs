use atomic_core::{
    Attribute, Cardinality, DB_FN, DB_IDENT, EntityRef, IndexOrder, IndexSegment, Instruction,
    Keyword, Peer, PostgresIndexer, PostgresOperator, PostgresStore, Program, ProgramKind, Schema,
    TxOp, TxValue, USER_PARTITION, Value, ValueType, View, encode_index_segment, encode_program,
    make_eid, sha256,
};
use postgres::{Client, NoTls};
use std::sync::{Arc, Barrier};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const ITEM_VALUE: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add(value: i64) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(user(42)),
        attribute: ITEM_VALUE,
        value: TxValue::Scalar(Value::Long(value)),
    }
}

#[test]
fn dry_run_and_apply_delete_only_unreachable_aged_content() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let db = common::transact(&service, "one", created.basis_t(), &[add(1)], 1_000).db_after;
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();

    let mut orphan_datoms = db.datoms(View::History, IndexOrder::Eavt);
    orphan_datoms[0].entity = user(999);
    orphan_datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
    let orphan_segment = IndexSegment {
        order: IndexOrder::Eavt,
        history: true,
        datoms: orphan_datoms,
    };
    let segment_bytes = encode_index_segment(&orphan_segment).unwrap();
    let segment_hash = sha256(&segment_bytes);
    let orphan_program = Program {
        kind: ProgramKind::Query,
        arity: 0,
        instructions: vec![
            Instruction::PushConstant(Value::Long(
                SystemTime::now()
                    .duration_since(UNIX_EPOCH)
                    .unwrap()
                    .as_nanos() as i64,
            )),
            Instruction::Return,
        ],
    };
    let program_bytes = encode_program(&orphan_program).unwrap();
    let program_hash = sha256(&program_bytes);
    let mut client = Client::connect(&connection, NoTls).unwrap();
    client
        .execute(
            "INSERT INTO atomic_index_segments (segment_hash, payload) VALUES ($1, $2) \
             ON CONFLICT DO NOTHING",
            &[&&segment_hash[..], &&segment_bytes[..]],
        )
        .unwrap();
    client
        .execute(
            "INSERT INTO atomic_programs (program_hash, kind, arity, payload) \
             VALUES ($1, 2, 0, $2) ON CONFLICT DO NOTHING",
            &[&&program_hash[..], &&program_bytes[..]],
        )
        .unwrap();
    let _ = db; // retain an immutable pre-GC value while storage is reclaimed

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let dry = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(!dry.applied);
    assert!(dry.segment_hashes.contains(&segment_hash));
    assert!(dry.program_hashes.contains(&program_hash));
    assert_eq!(
        client
            .query_one(
                "SELECT count(*) FROM atomic_index_segments WHERE segment_hash = $1",
                &[&&segment_hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    let applied = operator.collect_garbage(Duration::ZERO).unwrap();
    assert!(applied.applied);
    assert!(applied.segment_hashes.contains(&segment_hash));
    assert!(applied.program_hashes.contains(&program_hash));
    assert_eq!(
        client
            .query_one(
                "SELECT count(*) FROM atomic_index_segments WHERE segment_hash = $1",
                &[&&segment_hash[..]],
            )
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    let peer = Peer::connect(&connection, &database_id, 1).unwrap();
    assert_eq!(
        peer.db().values(user(42), ITEM_VALUE),
        vec![&Value::Long(1)]
    );
    service.shutdown();
}

#[test]
fn concurrent_consolidation_and_gc_never_remove_published_segments() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_race");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let mut basis = created.basis_t();
    for value in 1..=4 {
        basis = common::transact(
            &service,
            &format!("request-{value}"),
            basis,
            &[add(value)],
            999 + value,
        )
        .basis_t;
    }
    let barrier = Arc::new(Barrier::new(3));
    let index_handle = {
        let connection = connection.clone();
        let database_id = database_id.clone();
        let barrier = barrier.clone();
        std::thread::spawn(move || {
            let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
            barrier.wait();
            indexer.consolidate().unwrap();
        })
    };
    let gc_handle = {
        let connection = connection.clone();
        let barrier = barrier.clone();
        std::thread::spawn(move || {
            let mut operator = PostgresOperator::connect(&connection).unwrap();
            barrier.wait();
            operator.collect_garbage(Duration::ZERO).unwrap();
        })
    };
    barrier.wait();
    index_handle.join().unwrap();
    gc_handle.join().unwrap();
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let report = operator.inspect_database(&database_id, true).unwrap();
    assert!(report.healthy(), "{:?}", report.problems);
    assert_eq!(report.metrics.index_lag, 0);
    let peer = Peer::connect(&connection, &database_id, 1).unwrap();
    assert_eq!(
        peer.db().values(user(42), ITEM_VALUE),
        vec![&Value::Long(4)]
    );
    service.shutdown();
}

#[test]
fn gc_retains_current_and_historical_temporal_function_blobs() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("gc_temporal_functions");
    let mut store = PostgresStore::connect(&connection).unwrap();
    store.migrate().unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let program = |value| Program {
        kind: ProgramKind::Transaction,
        arity: 0,
        instructions: vec![
            Instruction::PushEntity(EntityRef::Id(user(42))),
            Instruction::PushConstant(Value::Long(value)),
            Instruction::EmitAdd(ITEM_VALUE),
            Instruction::Return,
        ],
    };
    let old_hash = store.deploy_program_blob(&program(11)).unwrap();
    let current_hash = store.deploy_program_blob(&program(22)).unwrap();
    let service = common::start_service(&connection, &database_id);
    let installed = common::transact(
        &service,
        "install-temporal-function",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("function".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("gc", "temporal-function")).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("function".into()),
                attribute: DB_FN as u32,
                value: Value::Function(old_hash).into(),
            },
        ],
        1_000,
    );
    let function = installed.tempids["function"];
    let rebound = common::transact(
        &service,
        "rebind-temporal-function",
        installed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(function),
            attribute: DB_FN as u32,
            value: Value::Function(current_hash).into(),
        }],
        2_000,
    );
    assert_eq!(
        rebound.db_after.values(function, DB_FN as u32),
        vec![&Value::Function(current_hash)]
    );

    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let inventory = operator.collect_garbage(Duration::ZERO).unwrap();
    assert!(!inventory.program_hashes.contains(&old_hash));
    assert!(!inventory.program_hashes.contains(&current_hash));
    let mut client = Client::connect(&connection, NoTls).unwrap();
    for hash in [old_hash, current_hash] {
        assert_eq!(
            client
                .query_one(
                    "SELECT count(*) FROM atomic_programs WHERE program_hash = $1",
                    &[&&hash[..]],
                )
                .unwrap()
                .get::<_, i64>(0),
            1
        );
    }
    service.shutdown();
}
