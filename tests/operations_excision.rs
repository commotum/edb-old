use atomic_core::{
    Attribute, Cardinality, DB_EXCISE, EntityRef, ErrorCategory, ExcisionFault, IndexOrder,
    Keyword, Peer, PostgresOperator, PostgresStore, Schema, TxOp, TxValue, USER_PARTITION, Value,
    ValueType, View, make_eid,
};
use postgres::NoTls;
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

const SECRET: u32 = 1_000;
const RETAINED: u32 = 1_001;
const RELATED: u32 = 1_002;

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
            SECRET,
            Keyword::new("person", "secret"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            RETAINED,
            Keyword::new("person", "retained"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            RELATED,
            Keyword::new("person", "related"),
            ValueType::Ref,
            Cardinality::Many,
        ))
        .unwrap();
    schema
}

fn add(entity: u64, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(entity),
        attribute,
        value: TxValue::Scalar(value),
    }
}

#[test]
fn transactional_a15_cow_activation_resumes_and_preserves_old_peer_value() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("a15_cow");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let seeded = common::transact(
        &service,
        "seed-private-person",
        created.basis_t(),
        &[
            add(user(42), SECRET, Value::String("erase-this-secret".into())),
            add(user(42), RETAINED, Value::Long(7)),
            add(user(43), RELATED, Value::Ref(user(42))),
        ],
        1_000,
    );
    let requested = common::transact(
        &service,
        "ordinary-a15-request",
        seeded.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("privacy-request".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(user(42))),
        }],
        2_000,
    );
    let request_entity = requested.tempids["privacy-request"];
    service.shutdown();

    let peer = Peer::connect(&connection, &database_id, 8).unwrap();
    let old_value = peer.db();
    assert_eq!(
        old_value.values(user(42), SECRET),
        vec![&Value::String("erase-this-secret".into())]
    );
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    assert!(
        !operator
            .sync_excise(&database_id, requested.basis_t)
            .unwrap()
    );

    let captured = operator
        .process_excision_requests_with_fault(&database_id, ExcisionFault::AfterCapture)
        .unwrap_err();
    assert_eq!(
        (captured.category, captured.code),
        (ErrorCategory::Interrupted, "excision/injected-fault")
    );
    let catchup_service = common::start_service(&connection, &database_id);
    let catchup = common::transact(
        &catchup_service,
        "transaction-during-excision-build",
        requested.basis_t,
        &[add(user(44), RETAINED, Value::Long(9))],
        3_000,
    );
    catchup_service.shutdown();

    let staged = operator
        .process_excision_requests_with_fault(&database_id, ExcisionFault::AfterCandidateStaged)
        .unwrap_err();
    assert_eq!(
        (staged.category, staged.code),
        (ErrorCategory::Interrupted, "excision/injected-fault")
    );
    assert!(
        !store
            .recover(&database_id)
            .unwrap()
            .values(user(42), SECRET)
            .is_empty()
    );

    let interrupted = operator
        .process_excision_requests_with_fault(&database_id, ExcisionFault::AfterActivation)
        .unwrap_err();
    assert_eq!(
        (interrupted.category, interrupted.code),
        (ErrorCategory::Interrupted, "excision/injected-fault")
    );
    let mut evidence = postgres::Client::connect(&connection, NoTls).unwrap();
    let generation_count: i64 = evidence
        .query_one(
            "SELECT count(*) FROM atomic_log_generations WHERE database_id=$1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        generation_count, 2,
        "retry must resume, not leak a candidate"
    );
    // Head activation is authoritative, but sync-excise remains false until
    // the separate root-last completion marker is durable.
    let switched = store.recover(&database_id).unwrap();
    assert!(switched.values(user(42), SECRET).is_empty());
    assert!(
        !operator
            .sync_excise(&database_id, requested.basis_t)
            .unwrap()
    );

    let receipt = operator.process_excision_requests(&database_id).unwrap();
    assert!(receipt.resumed);
    assert_eq!(receipt.request_count, 1);
    assert!(receipt.removed_datoms >= 3);
    assert_ne!(receipt.source_generation, receipt.generation);
    assert_eq!(receipt.old_head_hash, catchup.tx_hash);
    let reused_content: i64 = evidence
        .query_one(
            "SELECT count(*) FROM atomic_generation_transactions old \
               JOIN atomic_generation_transactions new \
                 ON new.content_hash=old.content_hash \
              WHERE old.database_id=$1 AND old.generation=$2 \
                AND new.database_id=$1 AND new.generation=$3",
            &[
                &database_id,
                &(receipt.source_generation as i64),
                &(receipt.generation as i64),
            ],
        )
        .unwrap()
        .get(0);
    assert!(
        reused_content >= 2,
        "unaffected immutable content should be shared"
    );
    assert!(
        operator
            .sync_excise(&database_id, requested.basis_t)
            .unwrap()
    );
    let integrity = operator.inspect_database(&database_id, true).unwrap();
    assert!(integrity.healthy(), "{:?}", integrity.problems);

    // Existing immutable peer values retain their old branch. Refresh chooses
    // the new generation and no query-visible history contains the secret.
    assert_eq!(
        old_value.values(user(42), SECRET),
        vec![&Value::String("erase-this-secret".into())]
    );
    let refreshed = peer.sync().unwrap();
    assert!(refreshed.values(user(42), SECRET).is_empty());
    assert!(refreshed.values(user(42), RETAINED).is_empty());
    assert!(refreshed.values(user(43), RELATED).is_empty());
    assert_eq!(refreshed.values(user(44), RETAINED), vec![&Value::Long(9)]);
    assert!(
        !refreshed
            .datoms(View::History, IndexOrder::Eavt)
            .iter()
            .any(|datom| datom.value == Value::String("erase-this-secret".into()))
    );
    // The ordinary A=15 assertion remains the permanent semantic audit fact.
    assert!(
        refreshed
            .datoms(View::History, IndexOrder::Eavt)
            .iter()
            .any(|datom| datom.entity == request_entity
                && datom.attribute == DB_EXCISE as u32
                && datom.value == Value::Ref(user(42)))
    );

    let restarted = PostgresStore::connect(&connection)
        .unwrap()
        .recover(&database_id)
        .unwrap();
    assert_eq!(restarted.basis_t(), refreshed.basis_t());
    assert_eq!(
        restarted.datoms(View::History, IndexOrder::Eavt),
        refreshed.datoms(View::History, IndexOrder::Eavt)
    );
}
