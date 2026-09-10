use atomic_core::{
    Attribute, Cardinality, DB_EXCISE, EntityRef, ErrorCategory, ExcisionFault, IndexOrder,
    Keyword, Peer, PostgresIndexer, PostgresOperator, PostgresStore, Schema, TxOp, TxValue,
    USER_PARTITION, Value, ValueType, View, make_eid,
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
    let mut secret = Attribute::new(
        SECRET,
        Keyword::new("person", "secret"),
        ValueType::String,
        Cardinality::Many,
    );
    secret.no_history = true;
    schema.install(secret).unwrap();
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

// This target deliberately owns each operator fault boundary. Automatic
// service execution has its separate public regression in automatic_excision.
fn manual_service(connection: &str, database_id: &str) -> atomic_core::TransactionService {
    atomic_core::TransactionService::start_configured_with_options(
        atomic_core::TransactionServiceConfig {
            connection: String::new(),
            database_id: database_id.into(),
            holder_id: unique("manual-excision"),
            lease_duration: std::time::Duration::from_secs(5),
            renew_interval: std::time::Duration::from_millis(100),
            queue_capacity: 32,
            capacity_limits: Default::default(),
        },
        atomic_core::PostgresConnectionConfig::plaintext(connection),
        atomic_core::ServiceOptions {
            excision: atomic_core::ExcisionConfig {
                enabled: false,
                ..Default::default()
            },
            ..Default::default()
        },
    )
    .unwrap()
}

#[test]
fn transactional_a15_cow_activation_resumes_and_preserves_old_peer_value() {
    let Some(connection) = connection() else {
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "operator_excision");
    let connection = fixture.connection.clone();
    let database_id = unique("a15_cow");
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = manual_service(&connection, &database_id);
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
    let retracted = common::transact(
        &service,
        "retract-no-history-secret",
        seeded.basis_t,
        &[TxOp::Retract {
            entity: EntityRef::Id(user(42)),
            attribute: SECRET,
            value: Some(TxValue::Scalar(Value::String("erase-this-secret".into()))),
        }],
        1_100,
    );
    let reasserted = common::transact(
        &service,
        "reassert-no-history-secret",
        retracted.basis_t,
        &[add(
            user(42),
            SECRET,
            Value::String("erase-this-secret".into()),
        )],
        1_200,
    );
    service.shutdown();

    // Establish a real persistent base after noHistory has discarded the
    // adjacent historical retraction/assertion pair. Excision must still
    // remove the surviving fact from the authoritative COW log and every
    // newly published current/history tree.
    let indexed = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();
    assert_eq!(indexed.basis_t, reasserted.basis_t);
    let consolidated = Peer::connect(&connection, &database_id, 8).unwrap();
    assert_eq!(consolidated.durable_base_t(), reasserted.basis_t);
    let consolidated_secret = consolidated
        .snapshot()
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.entity == user(42) && datom.attribute == SECRET)
        .collect::<Vec<_>>();
    assert_eq!(consolidated_secret.len(), 1);
    assert!(consolidated_secret[0].added);
    drop(consolidated);

    let request_service = manual_service(&connection, &database_id);
    let requested = common::transact(
        &request_service,
        "ordinary-a15-request",
        reasserted.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("privacy-request".into()),
            attribute: DB_EXCISE as u32,
            value: TxValue::Entity(EntityRef::Id(user(42))),
        }],
        2_000,
    );
    let request_entity = requested.tempids["privacy-request"];
    request_service.shutdown();

    let peer = Peer::connect(&connection, &database_id, 8).unwrap();
    let old_value = peer.db_compatibility();
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
    let catchup_service = manual_service(&connection, &database_id);
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
    let mut evidence = postgres::Client::connect(&connection, NoTls).unwrap();
    let unpublished_coordinates: i64 = evidence
        .query_one(
            "SELECT count(*) FROM atomic_semantic_commitment_roots r \
               JOIN atomic_log_generation_builds b \
                 ON b.database_id = r.database_id AND b.generation = r.generation \
              WHERE r.database_id = $1 \
                AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                 WHERE a.database_id = r.database_id \
                                   AND a.generation = r.generation)",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        unpublished_coordinates, 0,
        "a staged but unpublished excision must not leak a semantic endpoint"
    );

    let interrupted = operator
        .process_excision_requests_with_fault(&database_id, ExcisionFault::AfterActivation)
        .unwrap_err();
    assert_eq!(
        (interrupted.category, interrupted.code),
        (ErrorCategory::Interrupted, "excision/injected-fault")
    );
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
    let active_coordinate_count: i64 = evidence
        .query_one(
            "SELECT count(*) FROM atomic_heads h \
               JOIN atomic_log_generation_activations a \
                 ON a.database_id = h.database_id \
                AND a.generation = h.log_generation \
                AND a.basis_t = h.basis_t AND a.head_hash = h.tx_hash \
               JOIN atomic_semantic_commitment_roots r \
                 ON r.database_id = h.database_id \
                AND r.generation = h.log_generation \
                AND r.basis_t = h.basis_t AND r.tx_hash = h.tx_hash \
                AND r.state_hash = a.state_hash \
              WHERE h.database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(active_coordinate_count, 1);
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
    let refreshed = peer.sync_compatibility().unwrap();
    assert_eq!(peer.durable_base_t(), refreshed.basis_t());
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
    // durable_base_t == basis_t means these direct cursor reads come wholly
    // from the newly published COW tree, with no recent-log overlay.
    let refreshed_snapshot = peer.snapshot();
    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert!(
                !refreshed_snapshot
                    .datoms(history, order)
                    .unwrap()
                    .datoms
                    .iter()
                    .any(|datom| datom.entity == user(42)
                        && datom.attribute == SECRET
                        && datom.value == Value::String("erase-this-secret".into()))
            );
        }
    }
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
    assert!(
        !restarted
            .datoms(View::History, IndexOrder::Eavt)
            .iter()
            .any(|datom| datom.entity == user(42)
                && datom.attribute == SECRET
                && datom.value == Value::String("erase-this-secret".into()))
    );
    assert_eq!(restarted.basis_t(), refreshed.basis_t());
    assert_eq!(
        restarted.datoms(View::History, IndexOrder::Eavt),
        refreshed.datoms(View::History, IndexOrder::Eavt)
    );
}
