//! Two-build upgrade witness. Compile this test against the pre-repair library,
//! run with MODE=seed, then run against the repaired library with MODE=verify.
//! Use a disposable PostgreSQL database; neither mode deletes the old receipts.
//! Required environment: ATOMIC_POSTGRES_URL, ATOMIC_IDENTITY_UPGRADE_MODE,
//! ATOMIC_IDENTITY_UPGRADE_RECEIPT (a private local file), and optionally
//! ATOMIC_IDENTITY_UPGRADE_DATABASE (defaults to identity_upgrade).

use atomic_core::{
    Attribute, AttributeRef, CallableRef, CapacityLimits, Cardinality, EntityMap, EntityRef,
    IndexOrder, Instruction, Keyword, MapValue, PostgresMigrator, PostgresStore, Program,
    ProgramCall, ProgramKind, RuntimeValue, Schema, ServiceTransactionReport, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxForm, Value, ValueType,
};
use std::collections::BTreeSet;
use std::time::Duration;

const TAG: u32 = 1_000;
const COLLISION: &str = "__map/00000000000000000000";

fn entity_map(id: Option<&str>, tag: &str) -> TxForm {
    TxForm::EntityMap(EntityMap {
        id: id.map(|id| EntityRef::Temp(id.into())),
        attributes: vec![(
            AttributeRef::Id(TAG),
            MapValue::Value(Value::String(tag.into()).into()),
        )],
    })
}

fn receipt(report: &ServiceTransactionReport) -> String {
    // Deliberately includes exact history and current values, not only the hash
    // or entity count. Replay status itself is allowed to differ on retry.
    format!(
        "basis={:?}\nhash={:?}\ntempids={:?}\ntx={:?}\nbefore={:?}\nafter={:?}\nhistory={:?}\n",
        report.basis_t,
        report.tx_hash,
        report.tempids,
        report.tx_data,
        report.db_before.datoms(IndexOrder::Eavt).unwrap(),
        report.db_after.datoms(IndexOrder::Eavt).unwrap(),
        report
            .db_after
            .clone()
            .history()
            .datoms(IndexOrder::Eavt)
            .unwrap(),
    )
}

fn affected_entities(report: &ServiceTransactionReport) -> usize {
    report
        .tx_data
        .iter()
        .filter(|d| d.attribute == TAG && d.added)
        .map(|d| d.entity)
        .collect::<BTreeSet<_>>()
        .len()
}

#[test]
#[ignore = "explicit two-build PostgreSQL upgrade witness; requires seed/verify environment"]
fn old_collided_receipts_remain_exact_but_new_requests_are_repaired() {
    let connection =
        std::env::var("ATOMIC_POSTGRES_URL").expect("live disposable PostgreSQL required");
    let mode = std::env::var("ATOMIC_IDENTITY_UPGRADE_MODE").expect("seed or verify required");
    assert!(mode == "seed" || mode == "verify");
    let seed = mode == "seed";
    let file = std::env::var("ATOMIC_IDENTITY_UPGRADE_RECEIPT").expect("receipt file required");
    let database_id = std::env::var("ATOMIC_IDENTITY_UPGRADE_DATABASE")
        .unwrap_or_else(|_| "identity_upgrade".into());
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    if seed {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                TAG,
                Keyword::new("review", "tag"),
                ValueType::String,
                Cardinality::Many,
            ))
            .unwrap();
        store.create_database(&database_id, schema).unwrap();
    }
    let program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::EmitEntityMap,
            Instruction::Return,
        ],
    };
    let hash = store.deploy_program_blob(&program).unwrap();
    let call = ProgramCall {
        function: CallableRef::ExactHash(hash),
        arguments: vec![
            RuntimeValue::map(vec![(
                Value::Keyword(Keyword::new("review", "tag")),
                RuntimeValue::Scalar(Value::String("from-program".into())),
            )])
            .unwrap(),
        ],
    };
    let requests = [
        TransactionRequest::from_forms(
            "old-map-collision",
            vec![
                entity_map(Some(COLLISION), "explicit"),
                entity_map(None, "anonymous"),
            ],
        )
        .with_tx_instant(10),
        TransactionRequest::from_forms(
            "old-program-collision",
            vec![entity_map(Some(COLLISION), "explicit-program")],
        )
        .calling(call)
        .with_tx_instant(11),
    ];
    let service = TransactionService::start(TransactionServiceConfig {
        connection,
        database_id,
        holder_id: format!("upgrade-{}", std::process::id()),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    })
    .unwrap();
    let mut actual = format!("program={hash:?}\n");
    for request in &requests {
        let report = service
            .client()
            .transact(request.clone(), Duration::from_secs(20))
            .unwrap();
        assert_eq!(report.replayed, !seed);
        assert_eq!(
            affected_entities(&report),
            1,
            "original acknowledged collision must not be split"
        );
        actual.push_str(&receipt(&report));
    }
    if seed {
        // create_new protects against accidentally overwriting the old witness.
        use std::io::Write;
        std::fs::OpenOptions::new()
            .write(true)
            .create_new(true)
            .open(file)
            .unwrap()
            .write_all(actual.as_bytes())
            .unwrap();
    } else {
        assert_eq!(actual, std::fs::read_to_string(file).unwrap());
        let run = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .unwrap()
            .as_nanos();
        for (index, mut request) in requests.into_iter().enumerate() {
            request.request_key = format!("fresh-after-upgrade-{run}-{index}");
            request.tx_instant_override = None;
            let report = service
                .client()
                .transact(request, Duration::from_secs(20))
                .unwrap();
            assert!(!report.replayed);
            assert_eq!(
                affected_entities(&report),
                2,
                "new requests must use collision-free identity"
            );
        }
    }
    service.shutdown();
    eprintln!(
        "PostgreSQL identity upgrade {mode}: both direct-map and persisted-program receipts checked"
    );
}
