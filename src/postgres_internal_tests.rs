use crate::postgres::{CommitFault, PostgresStore};
use crate::{
    Attribute, Cardinality, Database, EntityRef, ErrorCategory, IndexOrder, Keyword, Schema, TxOp,
    TxValue, Unique, Value, ValueType, View,
};
use postgres::{Client, NoTls};
use std::process::Command;
use std::time::{SystemTime, UNIX_EPOCH};

const ITEM_NAME: u32 = 1_000;
const ITEM_COUNT: u32 = 1_001;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
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
        .install(
            Attribute::new(
                ITEM_NAME,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            ITEM_COUNT,
            Keyword::new("item", "quantity"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add_item(name: &str, count: i64) -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM_NAME,
            value: TxValue::Scalar(Value::String(name.into())),
        },
        TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(count)),
        },
    ]
}

fn migrated_store(connection: &str) -> PostgresStore {
    let mut store = PostgresStore::connect(connection).unwrap();
    store.migrate().unwrap();
    store
}

fn assert_database_eq(left: &Database, right: &Database) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.eidx_frontier(), right.eidx_frontier());
    assert_eq!(left.schema(), right.schema());
    for view in [View::Current, View::History] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(left.datoms(view, order), right.datoms(view, order));
        }
    }
}

#[test]
fn private_publication_faults_are_invisible_and_unknown_outcome_resolves_once() {
    let Some(connection) = connection() else {
        return;
    };
    for fault in [
        CommitFault::BeforeTransactionInsert,
        CommitFault::AfterTransactionInsert,
        CommitFault::AfterHeadUpdate,
    ] {
        let database_id = unique("private_rollback");
        let mut store = migrated_store(&connection);
        store.create_database(&database_id, schema()).unwrap();
        let error = store
            .transact_with_fault(
                &database_id,
                "request-1",
                1,
                &add_item("rolled-back", 1),
                1_000,
                fault,
            )
            .unwrap_err();
        assert_eq!(error.code, "postgres/injected-failure");
        assert_eq!(store.recover(&database_id).unwrap().basis_t(), 1);
        assert!(
            store
                .resolve_request_outcome(&database_id, "request-1")
                .unwrap()
                .is_none()
        );
    }

    let database_id = unique("private_unknown");
    let mut store = migrated_store(&connection);
    let db_before = store.create_database(&database_id, schema()).unwrap();
    let error = store
        .transact_with_fault(
            &database_id,
            "request-unknown",
            1,
            &add_item("committed", 1),
            1_000,
            CommitFault::AfterCommitBeforeResponse,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::UnknownOutcome);
    let resolved = store
        .resolve_request_outcome(&database_id, "request-unknown")
        .unwrap()
        .expect("the committed request has a durable outcome");
    assert!(resolved.replayed);
    assert_eq!(resolved.basis_t, 2);
    assert_database_eq(&resolved.db_before, &db_before);
    assert_eq!(resolved.database.basis_t(), 2);
    let resolved_again = store
        .resolve_request_outcome(&database_id, "request-unknown")
        .unwrap()
        .expect("outcome lookup remains stable");
    assert_eq!(resolved_again.tx_hash, resolved.tx_hash);
    assert_eq!(resolved_again.tempids, resolved.tempids);
    assert_eq!(resolved_again.tx_data, resolved.tx_data);
    assert_database_eq(&resolved_again.db_before, &resolved.db_before);
    assert_database_eq(&resolved_again.database, &resolved.database);
}

#[test]
fn process_death_at_private_precommit_kill_point_is_invisible() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("private_process_death");
    let mut store = migrated_store(&connection);
    store.create_database(&database_id, schema()).unwrap();
    drop(store);

    let status = Command::new(std::env::current_exe().unwrap())
        .args([
            "--ignored",
            "--exact",
            "postgres_internal_tests::postgres_crash_worker",
        ])
        .env("ATOMIC_POSTGRES_URL", &connection)
        .env("ATOMIC_CRASH_DATABASE", &database_id)
        .status()
        .unwrap();
    assert!(!status.success());

    let mut store = PostgresStore::connect(&connection).unwrap();
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 1);
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_transactions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(count, 1);
}

#[test]
#[ignore = "subprocess worker for the process-death test"]
fn postgres_crash_worker() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let Ok(database_id) = std::env::var("ATOMIC_CRASH_DATABASE") else {
        return;
    };
    let mut store = PostgresStore::connect(&connection).unwrap();
    let _ = store.transact_with_fault(
        &database_id,
        "crash-request",
        1,
        &add_item("never-visible", 1),
        1_000,
        CommitFault::AfterHeadUpdateProcessAbort,
    );
    unreachable!();
}
