//! Real PostgreSQL checks for opaque objects and conditional references.
//! Each test owns a fresh schema and installs only the thin storage tables.
mod common;

use atomic_core::storage::{
    BatchOutcome, CasOutcome, Guarded, ObjectId, PgBlockStore, RefChange, RefCondition, Reference,
};
use atomic_core::{OperationContext, OperationKind, PostgresConnectionConfig, sha256};
use postgres::{Client, NoTls};
use std::sync::{Arc, Barrier};
use std::time::Instant;

fn fixture(label: &str) -> Option<(common::PostgresFixture, PostgresConnectionConfig)> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL: set ATOMIC_POSTGRES_URL");
        return None;
    };
    let fixture = common::PostgresFixture::new(&connection, label);
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    Some((fixture, config))
}

fn applied(outcome: CasOutcome) -> Reference {
    match outcome {
        CasOutcome::Applied(reference) => reference,
        CasOutcome::Conflict(_) => panic!("conditional reference update unexpectedly conflicted"),
    }
}

fn assert_reference(actual: &Reference, revision: u64, value: Option<&[u8]>) {
    assert_eq!(actual.revision, revision);
    assert_eq!(actual.value.as_deref(), value);
}

fn condition(key: &str, expected: Option<u64>) -> RefCondition {
    RefCondition {
        key: key.to_owned(),
        expected,
    }
}

fn change(key: &str, value: Option<&[u8]>) -> RefChange {
    RefChange {
        key: key.to_owned(),
        value: value.map(<[u8]>::to_vec),
    }
}

#[test]
fn fresh_install_is_two_opaque_tables_and_reinstall_preserves_contents() {
    let Some((fixture, config)) = fixture("block_install_fresh") else {
        return;
    };
    assert!(PgBlockStore::connect(&config).is_err());
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let payload = (0_u8..=255).cycle().take(4_096).collect::<Vec<_>>();
    let id = store.put(&payload).unwrap();
    let reference = applied(
        store
            .compare_exchange("arbitrary/λ", None, Some(&payload))
            .unwrap(),
    );
    PgBlockStore::install(&config).unwrap();
    drop(store);
    let mut reopened = PgBlockStore::connect(&config).unwrap();
    assert_eq!(reopened.get(id).unwrap(), Some(payload.clone()));
    assert_reference(
        &reopened.read_ref("arbitrary/λ").unwrap().unwrap(),
        reference.revision,
        Some(&payload),
    );
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    let counts = sql
        .query_one(
            "SELECT \
             (SELECT count(*) FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n \
                ON n.oid=c.relnamespace WHERE n.nspname=current_schema() AND c.relkind IN ('r','p')), \
             (SELECT count(*) FROM pg_catalog.pg_proc p JOIN pg_catalog.pg_namespace n \
                ON n.oid=p.pronamespace WHERE n.nspname=current_schema()), \
             (SELECT count(*) FROM pg_catalog.pg_trigger t JOIN pg_catalog.pg_class c ON c.oid=t.tgrelid \
                JOIN pg_catalog.pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname=current_schema())",
            &[],
        )
        .unwrap();
    assert_eq!(
        counts.get::<_, i64>(0),
        2,
        "only generic object/reference tables"
    );
    assert_eq!(
        counts.get::<_, i64>(1),
        0,
        "engine policy must not be installed as SQL routines"
    );
    assert_eq!(counts.get::<_, i64>(2), 0, "no trigger state machines");
}

#[test]
fn immutable_objects_roundtrip_batch_order_duplicates_and_missing_values() {
    let Some((_fixture, config)) = fixture("block_immutable") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let binary = vec![0, 255, 128, 0, 1, 254];
    let first = store.put(&binary).unwrap();
    assert_eq!(first, sha256(&binary));
    let empty = store.put(&[]).unwrap();
    assert_eq!(store.put(&binary).unwrap(), first);
    assert_ne!(first, empty);
    let missing = [0_u8; 32];
    assert_ne!(first, missing);
    assert_ne!(empty, missing);
    assert!(store.get(missing).unwrap().is_none());
    assert!(store.get_many(&[]).unwrap().is_empty());
    drop(store);
    let mut reopened = PgBlockStore::connect(&config).unwrap();
    assert_eq!(reopened.get(first).unwrap(), Some(binary.clone()));
    assert_eq!(
        reopened
            .get_many(&[empty, missing, first, empty, first])
            .unwrap(),
        vec![
            Some(vec![]),
            None,
            Some(binary.clone()),
            Some(vec![]),
            Some(binary)
        ]
    );
    assert_eq!(reopened.list_objects(None, 10).unwrap().len(), 2);
}

#[test]
fn corrupt_object_is_rejected_by_single_bulk_and_repeated_put_reads() {
    let Some((fixture, config)) = fixture("block_corruption") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let original = b"immutable-original";
    let corrupt = b"immutable-corrupt!";
    let id = store.put(original).unwrap();
    let other = store.put(b"unaffected").unwrap();
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    // Owner-only corruption in this test's unique schema bypasses the Rust
    // storage interface; immutable identity is authenticated on every read.
    assert_eq!(
        sql.execute(
            "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
            &[&&id[..], &&corrupt[..]],
        )
        .unwrap(),
        1
    );
    assert_eq!(store.get(id).unwrap_err().code, "storage/object-corrupt");
    assert_eq!(
        store.get_many(&[other, id, other]).unwrap_err().code,
        "storage/object-corrupt"
    );
    assert_eq!(
        store.put(original).unwrap_err().code,
        "storage/object-corrupt"
    );
    assert_eq!(
        sql.query_one(
            "SELECT payload FROM atomic_objects WHERE id=$1",
            &[&&id[..]]
        )
        .unwrap()
        .get::<_, Vec<u8>>(0),
        corrupt,
        "repeated put must not silently repair a conflicting immutable object"
    );
    assert_eq!(store.get(other).unwrap(), Some(b"unaffected".to_vec()));
    sql.execute(
        "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
        &[&&id[..], &&original[..]],
    )
    .unwrap();
    drop(store);
    let mut reopened = PgBlockStore::connect(&config).unwrap();
    assert_eq!(reopened.get(id).unwrap(), Some(original.to_vec()));
}

#[test]
fn installation_refuses_an_existing_catalog_without_changing_its_contents() {
    let Some((fixture, config)) = fixture("block_install_existing") else {
        return;
    };
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    // An unrelated table proves installation refuses ownership without
    // changing pre-existing application contents.
    sql.batch_execute(
        "CREATE TABLE application_marker (id BIGINT PRIMARY KEY, payload BYTEA NOT NULL); \
         INSERT INTO application_marker VALUES (36, decode('123456', 'hex'))",
    )
    .unwrap();
    assert_eq!(
        PgBlockStore::install(&config).unwrap_err().code,
        "storage/schema-not-empty"
    );
    assert!(PgBlockStore::connect(&config).is_err());
    let rows = sql
        .query("SELECT id, payload FROM application_marker", &[])
        .unwrap();
    assert_eq!(rows.len(), 1);
    assert_eq!(rows[0].get::<_, i64>(0), 36);
    assert_eq!(rows[0].get::<_, Vec<u8>>(1), [0x12, 0x34, 0x56]);
    let tables = sql
        .query(
            "SELECT tablename::text FROM pg_catalog.pg_tables \
             WHERE schemaname = current_schema() ORDER BY tablename",
            &[],
        )
        .unwrap()
        .into_iter()
        .map(|row| row.get::<_, String>(0))
        .collect::<Vec<_>>();
    assert_eq!(tables, ["application_marker"]);
}

fn competing_updates(
    config: &PostgresConnectionConfig,
    key: &str,
    expected: Option<u64>,
) -> Reference {
    let gate = Arc::new(Barrier::new(3));
    let mut workers = Vec::new();
    for payload in [b"contender-a", b"contender-b"] {
        let config = config.clone();
        let gate = Arc::clone(&gate);
        let key = key.to_owned();
        workers.push(std::thread::spawn(move || {
            let mut store = PgBlockStore::connect(&config).unwrap();
            gate.wait();
            store
                .compare_exchange(&key, expected, Some(payload))
                .unwrap()
        }));
    }
    gate.wait();
    let outcomes = workers
        .into_iter()
        .map(|worker| worker.join().unwrap())
        .collect::<Vec<_>>();
    let mut winner = None;
    let mut conflict = None;
    for outcome in outcomes {
        match outcome {
            CasOutcome::Applied(reference) => {
                assert!(winner.replace(reference).is_none(), "both CAS writers won");
            }
            CasOutcome::Conflict(reference) => {
                assert!(
                    conflict.replace(reference).is_none(),
                    "both CAS writers lost"
                );
            }
        }
    }
    let winner = winner.expect("exactly one conditional writer wins");
    let conflict = conflict
        .expect("exactly one conditional writer conflicts")
        .expect("the winner's reference is visible to the conflicting writer");
    assert_reference(&conflict, winner.revision, winner.value.as_deref());
    winner
}

#[test]
fn competing_reference_creation_and_replacement_have_one_durable_winner() {
    let Some((_fixture, config)) = fixture("block_cas_race") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let created = competing_updates(&config, "root", None);
    assert_eq!(created.revision, 1);
    let replaced = competing_updates(&config, "root", Some(created.revision));
    assert_eq!(replaced.revision, created.revision + 1);
    let mut reopened = PgBlockStore::connect(&config).unwrap();
    assert_reference(
        &reopened.read_ref("root").unwrap().unwrap(),
        replaced.revision,
        replaced.value.as_deref(),
    );
}

#[test]
fn tombstones_preserve_revision_and_prevent_recreation_aba() {
    let Some((_fixture, config)) = fixture("block_ref_tombstone") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    assert!(store.read_ref("root").unwrap().is_none());
    assert!(matches!(
        store
            .compare_exchange("root", Some(1), Some(b"absent"))
            .unwrap(),
        CasOutcome::Conflict(None)
    ));
    let original = applied(store.compare_exchange("root", None, Some(b"A")).unwrap());
    let deleted = applied(
        store
            .compare_exchange("root", Some(original.revision), None)
            .unwrap(),
    );
    assert_reference(&deleted, original.revision + 1, None);
    drop(store);
    let mut reopened = PgBlockStore::connect(&config).unwrap();
    assert_reference(
        &reopened.read_ref("root").unwrap().unwrap(),
        deleted.revision,
        None,
    );
    for expected in [None, Some(original.revision)] {
        let CasOutcome::Conflict(Some(current)) = reopened
            .compare_exchange("root", expected, Some(b"stale"))
            .unwrap()
        else {
            panic!("a tombstone must reject create-only and stale-revision writes");
        };
        assert_reference(&current, deleted.revision, None);
    }
    let restored = applied(
        reopened
            .compare_exchange("root", Some(deleted.revision), Some(b"A"))
            .unwrap(),
    );
    assert_reference(&restored, deleted.revision + 1, Some(b"A"));
    let CasOutcome::Conflict(Some(current)) = reopened
        .compare_exchange("root", Some(original.revision), Some(b"stale"))
        .unwrap()
    else {
        panic!("returning to the same bytes must not permit a stale revision");
    };
    assert_reference(&current, restored.revision, Some(b"A"));
}

#[test]
fn guarded_reference_batch_is_atomic_and_leaves_read_only_guards_unchanged() {
    let Some((_fixture, config)) = fixture("block_guarded_batch") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let root = applied(
        store
            .compare_exchange("root", None, Some(b"before"))
            .unwrap(),
    );
    let authority = applied(
        store
            .compare_exchange("authority", None, Some(b"old-owner"))
            .unwrap(),
    );
    let next_authority = applied(
        store
            .compare_exchange("authority", Some(authority.revision), Some(b"new-owner"))
            .unwrap(),
    );
    let changes = [
        change("root", Some(b"after")),
        change("work", Some(b"before")),
    ];
    let stale = [
        condition("root", Some(root.revision)),
        condition("work", None),
        condition("authority", Some(authority.revision)),
    ];
    let BatchOutcome::Conflict(observed) = store.compare_exchange_many(&stale, &changes).unwrap()
    else {
        panic!("a changed authority must reject the entire reference batch");
    };
    let observed = observed
        .into_iter()
        .collect::<std::collections::BTreeMap<_, _>>();
    assert_eq!(
        observed.get("authority"),
        Some(&Some(next_authority.clone()))
    );
    assert_eq!(observed.get("root"), Some(&Some(root.clone())));
    assert_eq!(observed.get("work"), Some(&None));
    assert_eq!(store.read_ref("root").unwrap(), Some(root.clone()));
    assert!(store.read_ref("work").unwrap().is_none());
    let current = [
        condition("root", Some(root.revision)),
        condition("work", None),
        condition("authority", Some(next_authority.revision)),
    ];
    let BatchOutcome::Applied(updated) = store.compare_exchange_many(&current, &changes).unwrap()
    else {
        panic!("matching guards must publish the complete reference batch");
    };
    assert_eq!(updated.len(), 2);
    drop(store);
    let mut reopened = PgBlockStore::connect(&config).unwrap();
    assert_reference(
        &reopened.read_ref("root").unwrap().unwrap(),
        root.revision + 1,
        Some(b"after"),
    );
    assert_reference(
        &reopened.read_ref("work").unwrap().unwrap(),
        1,
        Some(b"before"),
    );
    assert_eq!(
        reopened.read_ref("authority").unwrap(),
        Some(next_authority)
    );
}

#[test]
fn guarded_work_publication_serializes_with_root_transition() {
    let Some((_fixture, config)) = fixture("block_capture_race") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    for attempt in 0..4 {
        let root_key = format!("root/{attempt}");
        let work_key = format!("work/{attempt}");
        let root = applied(
            store
                .compare_exchange(&root_key, None, Some(b"captured-root"))
                .unwrap(),
        );
        let gate = Arc::new(Barrier::new(3));
        let capture = {
            let config = config.clone();
            let gate = Arc::clone(&gate);
            let root_key = root_key.clone();
            let work_key = work_key.clone();
            std::thread::spawn(move || {
                let mut worker = PgBlockStore::connect(&config).unwrap();
                gate.wait();
                worker
                    .compare_exchange_many(
                        &[
                            condition(&root_key, Some(root.revision)),
                            condition(&work_key, None),
                        ],
                        &[change(&work_key, Some(b"captured-root"))],
                    )
                    .unwrap()
            })
        };
        let transition = {
            let config = config.clone();
            let gate = Arc::clone(&gate);
            let root_key = root_key.clone();
            std::thread::spawn(move || {
                let mut writer = PgBlockStore::connect(&config).unwrap();
                gate.wait();
                applied(
                    writer
                        .compare_exchange(&root_key, Some(root.revision), Some(b"new-root"))
                        .unwrap(),
                )
            })
        };
        gate.wait();
        let capture = capture.join().unwrap();
        let published = transition.join().unwrap();
        assert_reference(&published, root.revision + 1, Some(b"new-root"));
        match capture {
            BatchOutcome::Applied(references) => {
                assert_eq!(references.len(), 1);
                assert_eq!(references[0].0, work_key);
                assert_reference(&references[0].1, 1, Some(b"captured-root"));
                assert_eq!(
                    store.read_ref(&work_key).unwrap(),
                    Some(references[0].1.clone())
                );
            }
            BatchOutcome::Conflict(observed) => {
                assert!(
                    observed
                        .iter()
                        .any(|(key, value)| key == &root_key && value.as_ref() == Some(&published))
                );
                assert!(store.read_ref(&work_key).unwrap().is_none());
            }
        }
        // The post-transition ordering must always reject new work based on
        // the old root, regardless of which contender won the first race.
        let stale_work = format!("stale/{attempt}");
        assert!(matches!(
            store
                .compare_exchange_many(
                    &[
                        condition(&root_key, Some(root.revision)),
                        condition(&stale_work, None)
                    ],
                    &[change(&stale_work, Some(b"captured-root"))],
                )
                .unwrap(),
            BatchOutcome::Conflict(_)
        ));
        assert!(store.read_ref(&stale_work).unwrap().is_none());
    }
}

#[test]
fn protected_reuse_survives_sweep_and_stale_collector_cannot_delete() {
    let Some((fixture, config)) = fixture("block_protection") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let gc = applied(
        store
            .compare_exchange("gc", None, Some(b"collector-a"))
            .unwrap(),
    );
    let guard = [condition("gc", Some(gc.revision))];
    let orphan = store.put(b"reused-orphan").unwrap();
    let garbage = store.put(b"unreachable-garbage").unwrap();
    assert_eq!(
        store.put_protected(b"reused-orphan", 7, &guard).unwrap(),
        Guarded::Applied(orphan)
    );
    // Protection is monotone even if an older but still authorized operation
    // reuses the same bytes. The sweep threshold is exclusive.
    assert_eq!(
        store.put_protected(b"reused-orphan", 3, &guard).unwrap(),
        Guarded::Applied(orphan)
    );
    assert_eq!(
        store
            .remove_unprotected(&[orphan, garbage], 7, &guard)
            .unwrap(),
        Guarded::Applied(1)
    );
    assert_eq!(store.get(orphan).unwrap(), Some(b"reused-orphan".to_vec()));
    assert!(store.get(garbage).unwrap().is_none());
    let next_gc = applied(
        store
            .compare_exchange("gc", Some(gc.revision), Some(b"collector-b"))
            .unwrap(),
    );
    let eligible = store.put(b"eligible-garbage").unwrap();
    let Guarded::Conflict(observed) = store
        .remove_unprotected(&[eligible, orphan], 8, &guard)
        .unwrap()
    else {
        panic!("a displaced collector must not delete any selected object");
    };
    assert_eq!(observed, vec![("gc".to_owned(), Some(next_gc.clone()))]);
    assert_eq!(
        store.get(eligible).unwrap(),
        Some(b"eligible-garbage".to_vec())
    );
    assert_eq!(store.get(orphan).unwrap(), Some(b"reused-orphan".to_vec()));
    let absent = b"stale-upload";
    assert!(matches!(
        store.put_protected(absent, 8, &guard).unwrap(),
        Guarded::Conflict(_)
    ));
    assert!(store.get(sha256(absent)).unwrap().is_none());
    assert!(matches!(
        store.put_protected(b"reused-orphan", 8, &guard).unwrap(),
        Guarded::Conflict(_)
    ));
    let mut sql = Client::connect(&fixture.connection, NoTls).unwrap();
    assert_eq!(
        sql.query_one(
            "SELECT protected_epoch FROM atomic_objects WHERE id=$1",
            &[&&orphan[..]]
        )
        .unwrap()
        .get::<_, i64>(0),
        7,
        "a rejected guarded put must not modify existing protection"
    );
    assert_eq!(
        store
            .remove_unprotected(&[eligible], 8, &[condition("gc", Some(next_gc.revision))])
            .unwrap(),
        Guarded::Applied(1)
    );
    assert!(store.get(eligible).unwrap().is_none());
}

#[test]
fn bounded_enumeration_and_explicit_removal_preserve_other_objects_and_refs() {
    let Some((_fixture, config)) = fixture("block_enumeration") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let format = store.read_ref("system/format").unwrap().unwrap();
    let mut expected = (0_u64..7)
        .map(|value| store.put(&value.to_be_bytes()).unwrap())
        .collect::<Vec<_>>();
    expected.sort();
    let mut listed = Vec::new();
    loop {
        let page = store.list_objects(listed.last().copied(), 2).unwrap();
        assert!(page.len() <= 2);
        if page.is_empty() {
            break;
        }
        assert!(page.iter().all(|id| !listed.contains(id)));
        listed.extend(page);
        assert!(
            listed.len() <= expected.len(),
            "object cursor did not advance"
        );
    }
    assert_eq!(listed, expected);
    for key in ["alpha", "bravo", "charlie", "delta", "echo"] {
        applied(
            store
                .compare_exchange(key, None, Some(key.as_bytes()))
                .unwrap(),
        );
    }
    applied(store.compare_exchange("bravo", Some(1), None).unwrap());
    let mut keys = Vec::new();
    loop {
        let after = keys.last().map(String::as_str);
        let page = store.list_refs(after, 2).unwrap();
        assert!(page.len() <= 2);
        if page.is_empty() {
            break;
        }
        for (key, reference) in page {
            assert!(!keys.contains(&key));
            if key == "system/format" {
                assert_reference(&reference, format.revision, format.value.as_deref());
            } else if key == "bravo" {
                assert_reference(&reference, 2, None);
            } else {
                assert_reference(&reference, 1, Some(key.as_bytes()));
            }
            keys.push(key);
        }
        assert!(keys.len() <= 6, "reference cursor did not advance");
    }
    assert_eq!(
        keys,
        [
            "alpha",
            "bravo",
            "charlie",
            "delta",
            "echo",
            "system/format"
        ]
    );
    let removed = expected[2];
    assert_eq!(
        store
            .remove_objects(&[removed, removed, [0_u8; 32]])
            .unwrap(),
        1
    );
    assert_eq!(store.remove_objects(&[removed]).unwrap(), 0);
    assert!(store.get(removed).unwrap().is_none());
    expected.retain(|id| *id != removed);
    assert_eq!(store.list_objects(None, 20).unwrap(), expected);
    assert_eq!(store.list_refs(None, 20).unwrap().len(), 6);
}

#[test]
fn invalid_keys_and_unbounded_enumeration_fail_before_changing_storage() {
    let Some((_fixture, config)) = fixture("block_input_limits") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    for key in ["", "invalid\0key"] {
        assert_eq!(store.read_ref(key).unwrap_err().code, "storage/invalid-key");
        assert_eq!(
            store
                .compare_exchange(key, None, Some(b"value"))
                .unwrap_err()
                .code,
            "storage/invalid-key"
        );
    }
    for limit in [0, usize::MAX] {
        assert_eq!(
            store.list_objects(None, limit).unwrap_err().code,
            "storage/invalid-limit"
        );
        assert_eq!(
            store.list_refs(None, limit).unwrap_err().code,
            "storage/invalid-limit"
        );
    }
    assert!(store.list_objects(None, 10).unwrap().is_empty());
    let references = store.list_refs(None, 10).unwrap();
    assert_eq!(references.len(), 1);
    assert_eq!(references[0].0, "system/format");
}

#[test]
fn measured_objects_and_root_publish_reopen_read_complete_path() {
    let Some((_fixture, config)) = fixture("block_measured_path") else {
        return;
    };
    PgBlockStore::install(&config).unwrap();
    for size in [32_usize, 4_096, 65_536] {
        let payloads = (0_u64..16)
            .map(|index| {
                let mut bytes = vec![0xa5; size];
                bytes[..8].copy_from_slice(&index.to_be_bytes());
                bytes
            })
            .collect::<Vec<_>>();
        let operation = OperationContext::new(OperationKind::Application);
        let scope = operation.enter();
        let started = Instant::now();
        let mut store = PgBlockStore::connect(&config).unwrap();
        let ids = payloads
            .iter()
            .map(|payload| store.put(payload).unwrap())
            .collect::<Vec<_>>();
        let root = ids.iter().flatten().copied().collect::<Vec<_>>();
        let key = format!("sample/{size}");
        applied(store.compare_exchange(&key, None, Some(&root)).unwrap());
        drop(store);
        let put_publish = started.elapsed();
        let read_started = Instant::now();
        let mut reopened = PgBlockStore::connect(&config).unwrap();
        let recovered_root = reopened.read_ref(&key).unwrap().unwrap().value.unwrap();
        let recovered_ids = recovered_root
            .chunks_exact(32)
            .map(|bytes| ObjectId::try_from(bytes).unwrap())
            .collect::<Vec<_>>();
        assert_eq!(recovered_ids, ids);
        let recovered = reopened.get_many(&recovered_ids).unwrap();
        assert_eq!(
            recovered,
            payloads.iter().cloned().map(Some).collect::<Vec<_>>()
        );
        drop(reopened);
        let reopen_get = read_started.elapsed();
        let elapsed = started.elapsed();
        drop(scope);
        let io = operation.snapshot();
        assert!(
            io.sql_calls > 0,
            "complete-path storage I/O must be measured"
        );
        assert_eq!(io.errors, 0);
        eprintln!(
            "BLOCK_STORAGE_SAMPLE objects={} object_bytes={} total_payload_bytes={} put_publish_us={} reopen_get_us={} complete_us={} connect_calls={} sql_calls={} result_cell_bytes={}",
            payloads.len(),
            size,
            payloads.len() * size,
            put_publish.as_micros(),
            reopen_get.as_micros(),
            elapsed.as_micros(),
            io.connect_calls,
            io.sql_calls,
            io.result_cell_bytes,
        );
    }
}
