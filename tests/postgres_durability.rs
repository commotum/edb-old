use atomic_core::{
    Attribute, Cardinality, CommitFault, Database, EntityRef, ErrorCategory, IndexOrder, Keyword,
    PostgresStore, Schema, TxOp, TxValue, Unique, Value, ValueType, View,
};
use postgres::{Client, NoTls};
use std::process::Command;
use std::sync::{Arc, Barrier};
use std::time::{SystemTime, UNIX_EPOCH};

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1,
            Keyword::new("db", "txInstant"),
            ValueType::Instant,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                10,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            11,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema.rename(11, Keyword::new("item", "quantity")).unwrap();
    schema
}

fn add_item(name: &str, count: i64) -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 10,
            value: TxValue::Scalar(Value::String(name.into())),
        },
        TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: 11,
            value: TxValue::Scalar(Value::Long(count)),
        },
    ]
}

fn unique(prefix: &str) -> String {
    let nanos = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    format!("{prefix}_{}_{}", std::process::id(), nanos)
}

fn migrated_store(connection: &str) -> PostgresStore {
    let mut store = PostgresStore::connect(connection).unwrap();
    store.migrate().unwrap();
    store
}

fn assert_database_eq(left: &Database, right: &Database) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.next_eid(), right.next_eid());
    assert_eq!(
        left.schema().attributes().collect::<Vec<_>>(),
        right.schema().attributes().collect::<Vec<_>>()
    );
    assert_eq!(
        left.schema_changes().collect::<Vec<_>>(),
        right.schema_changes().collect::<Vec<_>>()
    );
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
    left.validate_invariants().unwrap();
    right.validate_invariants().unwrap();
}

#[test]
fn migration_commit_recovery_and_idempotency_are_exact() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("exact");
    let mut store = migrated_store(&connection);
    store.migrate().unwrap();
    let empty = store.create_database(&database_id, schema()).unwrap();
    assert_database_eq(&empty, &store.recover(&database_id).unwrap());

    let ops = add_item("one", 1);
    let committed = store
        .transact(&database_id, "request-1", 0, &ops, 1_000)
        .unwrap();
    assert!(!committed.replayed);
    assert_eq!(committed.basis_t, 1);
    let recovered = store.recover(&database_id).unwrap();
    assert_database_eq(&committed.database, &recovered);
    assert_eq!(
        recovered
            .schema()
            .resolve_ident(&Keyword::new("item", "count")),
        Some(11)
    );

    let schema_commit = store
        .transact(
            &database_id,
            "request-schema",
            1,
            &[TxOp::InstallAttribute(Attribute::new(
                12,
                Keyword::new("item", "tag"),
                ValueType::String,
                Cardinality::Many,
            ))],
            1_001,
        )
        .unwrap();
    let entity = committed.tempids["item"];
    let mut latest = store
        .transact(
            &database_id,
            "request-tag",
            2,
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: 12,
                value: TxValue::Scalar(Value::String("durable-schema".into())),
            }],
            1_002,
        )
        .unwrap();
    assert_eq!(schema_commit.schema_changes.len(), 1);

    for value in 2..=20 {
        latest = store
            .transact(
                &database_id,
                &format!("request-count-{value}"),
                latest.basis_t,
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 11,
                    value: TxValue::Scalar(Value::Long(value)),
                }],
                1_001 + value,
            )
            .unwrap();
    }
    assert_database_eq(&latest.database, &store.recover(&database_id).unwrap());

    let retry = store
        .transact(&database_id, "request-1", 0, &ops, 1_000)
        .unwrap();
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, committed.basis_t);
    assert_eq!(retry.tx_hash, committed.tx_hash);
    assert_eq!(retry.tempids, committed.tempids);
    assert_database_eq(&retry.database, &committed.database);

    let mut reopened = PostgresStore::connect(&connection).unwrap();
    assert_database_eq(&latest.database, &reopened.recover(&database_id).unwrap());
    let next = reopened
        .transact(
            &database_id,
            "request-next-entity",
            latest.basis_t,
            &add_item("two", 2),
            1_022,
        )
        .unwrap();
    assert_eq!(next.tempids["item"], 1_001);
    assert_database_eq(&next.database, &reopened.recover(&database_id).unwrap());

    let mismatch = store
        .transact(&database_id, "request-1", 0, &add_item("other", 2), 1_000)
        .unwrap_err();
    assert_eq!(mismatch.code, "postgres/idempotency-key-reused");
}

#[test]
fn precommit_failures_are_invisible_and_unknown_outcome_resolves_once() {
    let Some(connection) = connection() else {
        return;
    };
    for fault in [
        CommitFault::BeforeTransactionInsert,
        CommitFault::AfterTransactionInsert,
        CommitFault::AfterHeadUpdate,
    ] {
        let database_id = unique("rollback");
        let mut store = migrated_store(&connection);
        store.create_database(&database_id, schema()).unwrap();
        let error = store
            .transact_with_fault(
                &database_id,
                "request-1",
                0,
                &add_item("rolled-back", 1),
                1_000,
                fault,
            )
            .unwrap_err();
        assert_eq!(error.code, "postgres/injected-failure");
        assert_eq!(store.recover(&database_id).unwrap().basis_t(), 0);
        let committed = store
            .transact(
                &database_id,
                "request-1",
                0,
                &add_item("rolled-back", 1),
                1_000,
            )
            .unwrap();
        assert_eq!(committed.basis_t, 1);
    }

    let database_id = unique("unknown");
    let mut store = migrated_store(&connection);
    store.create_database(&database_id, schema()).unwrap();
    let ops = add_item("committed", 1);
    let error = store
        .transact_with_fault(
            &database_id,
            "request-unknown",
            0,
            &ops,
            1_000,
            CommitFault::AfterCommitBeforeResponse,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::UnknownOutcome);
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 1);
    let resolved = store
        .transact(&database_id, "request-unknown", 0, &ops, 1_000)
        .unwrap();
    assert!(resolved.replayed);

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
fn concurrent_expected_basis_writers_cannot_fork() {
    let Some(connection) = connection() else {
        return;
    };
    let migration_barrier = Arc::new(Barrier::new(3));
    let mut migration_handles = Vec::new();
    for _ in 0..2 {
        let connection = connection.clone();
        let barrier = Arc::clone(&migration_barrier);
        migration_handles.push(std::thread::spawn(move || {
            let mut store = PostgresStore::connect(&connection).unwrap();
            barrier.wait();
            store.migrate()
        }));
    }
    migration_barrier.wait();
    for handle in migration_handles {
        handle.join().unwrap().unwrap();
    }

    let database_id = unique("concurrent");
    let mut creator = PostgresStore::connect(&connection).unwrap();
    creator.create_database(&database_id, schema()).unwrap();
    drop(creator);

    let barrier = Arc::new(Barrier::new(3));
    let mut handles = Vec::new();
    for writer in 0..2 {
        let connection = connection.clone();
        let database_id = database_id.clone();
        let barrier = Arc::clone(&barrier);
        handles.push(std::thread::spawn(move || {
            let mut store = PostgresStore::connect(&connection).unwrap();
            barrier.wait();
            store.transact(
                &database_id,
                &format!("writer-{writer}"),
                0,
                &add_item(&format!("item-{writer}"), writer),
                1_000,
            )
        }));
    }
    barrier.wait();
    let results: Vec<_> = handles
        .into_iter()
        .map(|handle| handle.join().unwrap())
        .collect();
    assert_eq!(results.iter().filter(|result| result.is_ok()).count(), 1);
    let loser = results
        .iter()
        .find_map(|result| result.as_ref().err())
        .unwrap();
    assert_eq!(loser.code, "postgres/stale-basis");

    let mut store = PostgresStore::connect(&connection).unwrap();
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 1);
}

#[test]
fn sql_constraints_immutability_and_corruption_checks_fail_closed() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("constraints");
    let mut store = migrated_store(&connection);
    store.create_database(&database_id, schema()).unwrap();

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let constraint_count: i64 = client
        .query_one(
            "SELECT count(*) FROM pg_constraint \
             WHERE conrelid IN ('atomic_transactions'::regclass, \
                                 'atomic_requests'::regclass, \
                                 'atomic_heads'::regclass)",
            &[],
        )
        .unwrap()
        .get(0);
    assert!(constraint_count >= 15);
    let trigger_count: i64 = client
        .query_one(
            "SELECT count(*) FROM pg_trigger \
             WHERE NOT tgisinternal AND tgrelid IN \
             ('atomic_databases'::regclass, 'atomic_transactions'::regclass, \
              'atomic_requests'::regclass, 'atomic_heads'::regclass)",
            &[],
        )
        .unwrap()
        .get(0);
    assert_eq!(trigger_count, 7);
    let mut malformed = client.transaction().unwrap();
    let bytes = [0_u8; 48];
    let hash = [1_u8; 32];
    let error = malformed
        .execute(
            "INSERT INTO atomic_transactions \
             (database_id, basis_t, previous_hash, tx_hash, payload) \
             VALUES ($1, 2, $2, $3, $4)",
            &[&database_id, &&[0_u8; 32][..], &&hash[..], &&bytes[..]],
        )
        .unwrap_err();
    assert_eq!(error.as_db_error().unwrap().code().code(), "40001");
    drop(malformed);

    store
        .transact(
            &database_id,
            "request-1",
            0,
            &add_item("immutable", 1),
            1_000,
        )
        .unwrap();
    let error = client
        .execute(
            "UPDATE atomic_transactions SET payload = payload WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(error.as_db_error().unwrap().code().code(), "55000");

    let role = unique("atomic_runtime");
    client
        .batch_execute(&format!(
            "CREATE ROLE {role}; \
             GRANT SELECT, INSERT ON atomic_transactions, atomic_requests TO {role}; \
             GRANT SELECT, UPDATE ON atomic_heads TO {role}; \
             GRANT SELECT ON atomic_databases TO {role}"
        ))
        .unwrap();
    let mut runtime = client.transaction().unwrap();
    runtime
        .batch_execute(&format!("SET LOCAL ROLE {role}"))
        .unwrap();
    let error = runtime
        .execute(
            "DELETE FROM atomic_transactions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(error.as_db_error().unwrap().code().code(), "42501");
    drop(runtime);
    client
        .batch_execute(&format!("DROP OWNED BY {role}; DROP ROLE {role}"))
        .unwrap();

    client
        .batch_execute("ALTER TABLE atomic_transactions DISABLE TRIGGER USER")
        .unwrap();
    client
        .execute(
            "UPDATE atomic_transactions SET payload = set_byte(payload, 16, \
             get_byte(payload, 16) # 1) WHERE database_id = $1 AND basis_t = 1",
            &[&database_id],
        )
        .unwrap();
    client
        .batch_execute("ALTER TABLE atomic_transactions ENABLE TRIGGER USER")
        .unwrap();
    let error = store.recover(&database_id).unwrap_err();
    assert_eq!(error.code, "recovery/transaction-checksum-mismatch");

    let missing_id = unique("missing");
    store.create_database(&missing_id, schema()).unwrap();
    store
        .transact(&missing_id, "request-1", 0, &add_item("missing", 1), 1_000)
        .unwrap();
    client
        .batch_execute(
            "ALTER TABLE atomic_requests DISABLE TRIGGER USER; \
             ALTER TABLE atomic_transactions DISABLE TRIGGER USER",
        )
        .unwrap();
    client
        .execute(
            "DELETE FROM atomic_requests WHERE database_id = $1",
            &[&missing_id],
        )
        .unwrap();
    client
        .execute(
            "DELETE FROM atomic_transactions WHERE database_id = $1",
            &[&missing_id],
        )
        .unwrap();
    client
        .batch_execute(
            "ALTER TABLE atomic_requests ENABLE TRIGGER USER; \
             ALTER TABLE atomic_transactions ENABLE TRIGGER USER",
        )
        .unwrap();
    let error = store.recover(&missing_id).unwrap_err();
    assert_eq!(error.code, "recovery/missing-transaction");
}

#[test]
fn acknowledged_commit_survives_postgres_restart() {
    let (Some(connection), Ok(pg_ctl), Ok(data_dir)) = (
        connection(),
        std::env::var("ATOMIC_POSTGRES_CTL"),
        std::env::var("ATOMIC_POSTGRES_DATA"),
    ) else {
        return;
    };
    let database_id = unique("restart");
    let expected = {
        let mut store = migrated_store(&connection);
        store.create_database(&database_id, schema()).unwrap();
        store
            .transact(&database_id, "request-1", 0, &add_item("durable", 1), 1_000)
            .unwrap()
            .database
    };
    let status = Command::new(pg_ctl)
        .args(["-D", &data_dir, "-m", "fast", "-w", "restart"])
        .status()
        .unwrap();
    assert!(status.success());
    let mut store = PostgresStore::connect(&connection).unwrap();
    assert_database_eq(&expected, &store.recover(&database_id).unwrap());
}

#[test]
fn client_process_death_before_commit_is_invisible() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("process_death");
    let mut store = migrated_store(&connection);
    store.create_database(&database_id, schema()).unwrap();
    drop(store);

    let status = Command::new(std::env::current_exe().unwrap())
        .args(["--ignored", "--exact", "postgres_crash_worker"])
        .env("ATOMIC_POSTGRES_URL", &connection)
        .env("ATOMIC_CRASH_DATABASE", &database_id)
        .status()
        .unwrap();
    assert!(!status.success());

    let mut store = PostgresStore::connect(&connection).unwrap();
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 0);
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_transactions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(count, 0);
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
        0,
        &add_item("never-visible", 1),
        1_000,
        CommitFault::AfterHeadUpdateProcessAbort,
    );
    unreachable!();
}
