//! A guarded publication must re-read its conditions after waiting for locks,
//! even when the connection's configured transaction default is Repeatable Read.
mod common;

use atomic_core::storage::{BatchOutcome, CasOutcome, PgBlockStore, RefChange, RefCondition};
use atomic_core::{PostgresConnectionConfig, sha256};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant};

fn repeatable_read_connection(connection: &str, schema: &str, application: &str) -> String {
    // The PostgreSQL startup option parser needs an escaped space in this GUC
    // value. Escape it again for a keyword DSN, or percent-encode it for a URI.
    let options = format!(
        "-csearch_path={schema},pg_catalog \
         -cdefault_transaction_isolation=repeatable\\ read \
         -cstatement_timeout=10000 -clock_timeout=10000"
    );
    if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
        let encoded: String = options.bytes().map(|byte| format!("%{byte:02X}")).collect();
        format!(
            "{connection}{}options={encoded}&application_name={application}",
            if connection.contains('?') { '&' } else { '?' }
        )
    } else {
        let escaped = options.replace('\\', "\\\\").replace('\'', "\\'");
        format!("{connection} options='{escaped}' application_name='{application}'")
    }
}

#[test]
fn guarded_cas_rechecks_condition_after_wait_with_repeatable_read_default() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "block_isolation");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let guard_key = "root/authority";
    let child_key = "root/child";
    let original = match store
        .compare_exchange(guard_key, None, Some(b"old-authority"))
        .unwrap()
    {
        CasOutcome::Applied(reference) => reference,
        outcome => panic!("initial reference creation failed: {outcome:?}"),
    };

    // Unique schema names also fit PostgreSQL's application-name length bound.
    // Only the one worker retains this name while the lock wait is observed.
    let application = fixture.schema.clone();
    let worker_config = PostgresConnectionConfig::plaintext(repeatable_read_connection(
        &fixture.connection,
        &fixture.schema,
        &application,
    ));
    {
        let mut probe = worker_config.connect().unwrap();
        let default: String = probe
            .query_one("SHOW default_transaction_isolation", &[])
            .unwrap()
            .get(0);
        assert_eq!(default, "repeatable read");
    }

    // Match the storage primitive's namespace-qualified advisory key exactly.
    let digest = sha256(format!("atomic-storage\0{}\0{guard_key}", fixture.schema).as_bytes());
    let lock = i64::from_be_bytes(digest[..8].try_into().unwrap());
    let high = ((lock as u64) >> 32) as i64;
    let low = ((lock as u64) & u32::MAX as u64) as i64;
    let mut holder = Client::connect(&fixture.connection, NoTls).unwrap();
    let mut observer = Client::connect(&fixture.connection, NoTls).unwrap();
    observer
        .batch_execute("SET statement_timeout='2s'")
        .unwrap();

    std::thread::scope(|scope| {
        // Declared inside the scope: any assertion/query failure drops the
        // transaction and releases its lock before the scope joins its worker.
        let mut holding = holder.transaction().unwrap();
        holding
            .query_one("SELECT pg_catalog.pg_advisory_xact_lock($1)", &[&lock])
            .unwrap();
        let expected = original.revision;
        let worker = scope.spawn(move || {
            let mut store = PgBlockStore::connect(&worker_config)?;
            store.compare_exchange_many(
                &[
                    RefCondition {
                        key: guard_key.to_owned(),
                        expected: Some(expected),
                    },
                    RefCondition {
                        key: child_key.to_owned(),
                        expected: None,
                    },
                ],
                &[RefChange {
                    key: child_key.to_owned(),
                    value: Some(b"must-not-publish".to_vec()),
                }],
            )
        });

        let deadline = Instant::now() + Duration::from_secs(5);
        let observed_wait = loop {
            let waiting: bool = observer
                .query_one(
                    "SELECT EXISTS (SELECT 1 FROM pg_catalog.pg_locks l \
                     JOIN pg_catalog.pg_stat_activity a ON a.pid=l.pid \
                     WHERE a.datname=current_database() AND a.application_name=$1 \
                     AND l.locktype='advisory' AND NOT l.granted \
                     AND l.classid::bigint=$2 AND l.objid::bigint=$3 AND l.objsubid=1)",
                    &[&application, &high, &low],
                )
                .unwrap()
                .get(0);
            if waiting {
                break true;
            }
            if worker.is_finished() || Instant::now() >= deadline {
                break false;
            }
            std::thread::sleep(Duration::from_millis(10));
        };

        // Change only the condition reference. Under a stale RR snapshot, the
        // child's insertion has no write/write conflict to rescue the guard.
        if observed_wait {
            assert_eq!(
                holding
                    .execute(
                        "UPDATE atomic_refs SET revision=revision+1,value=$2 WHERE key=$1",
                        &[&guard_key, &&b"replacement-authority"[..]],
                    )
                    .unwrap(),
                1
            );
        }
        holding.commit().unwrap();
        let result = worker.join().expect("guarded CAS worker panicked");
        assert!(
            observed_wait,
            "worker never waited on the exact authority guard lock"
        );
        let outcome = result.expect("guarded CAS must report a conflict, not an SQL error");
        let BatchOutcome::Conflict(current) = outcome else {
            panic!("worker published through a stale condition: {outcome:?}");
        };
        let current_guard = current
            .iter()
            .find(|(key, _)| key == guard_key)
            .unwrap()
            .1
            .as_ref()
            .unwrap();
        assert_eq!(current_guard.revision, original.revision + 1);
        assert_eq!(
            current_guard.value.as_deref(),
            Some(&b"replacement-authority"[..])
        );
    });

    assert!(
        store.read_ref(child_key).unwrap().is_none(),
        "stale worker must not create the child reference"
    );
    let guard = store.read_ref(guard_key).unwrap().unwrap();
    assert_eq!(guard.revision, original.revision + 1);
    assert_eq!(guard.value.as_deref(), Some(&b"replacement-authority"[..]));
}
