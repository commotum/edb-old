use atomic_core::sql_io::{
    GenericClient, OperationContext, OperationKind, SqlCallKind, SqlClient, SqlIoReport,
    process_sql_stats,
};
use postgres::fallible_iterator::FallibleIterator;
use std::sync::{Arc, Barrier, Mutex};

fn connection() -> Option<String> {
    let connection = std::env::var("ATOMIC_POSTGRES_URL").ok();
    if connection.is_none() {
        eprintln!("SKIPPED SQL attribution PostgreSQL witness: ATOMIC_POSTGRES_URL is not set");
    }
    connection
}

fn failed_connect() {
    let result = SqlClient::connect_with(|| {
        Err::<postgres::Client, _>("synthetic-private-connection-diagnostic")
    });
    assert!(result.is_err());
}

#[test]
fn named_nested_contexts_group_inclusively_without_repeated_name_double_count() {
    use atomic_core::Keyword;
    let outer =
        OperationContext::named(OperationKind::Application, Keyword::new("app", "report")).unwrap();
    let a = outer
        .child_named(OperationKind::Query, Keyword::new("app", "lookup"))
        .unwrap();
    let repeated = a
        .child_named(OperationKind::Query, Keyword::new("app", "lookup"))
        .unwrap();
    let b = outer
        .child_named(OperationKind::Query, Keyword::new("app", "other"))
        .unwrap();
    let handles = [repeated, b]
        .into_iter()
        .map(|child| {
            std::thread::spawn(move || {
                let _scope = child.enter();
                for _ in 0..5 {
                    failed_connect();
                }
            })
        })
        .collect::<Vec<_>>();
    for handle in handles {
        handle.join().unwrap();
    }
    let report = outer.report();
    assert_eq!(report.stats.calls, 10);
    assert_eq!(report.nested[&Keyword::new("app", "lookup")].calls, 5);
    assert_eq!(report.nested[&Keyword::new("app", "other")].calls, 5);
    assert_eq!(a.report().stats.calls, 5);
    assert!(!report.nested_truncated);
    let (result, measured) = outer.measure(|| Err::<(), _>("unchanged error"));
    assert_eq!(result, Err("unchanged error"));
    assert_eq!(measured.stats, report.stats);
    assert!(
        OperationContext::named(
            OperationKind::Query,
            Keyword {
                namespace: None,
                name: "unqualified".into()
            }
        )
        .is_err()
    );
}

#[test]
fn diagnostic_detail_cap_never_changes_total_or_result() {
    let parent = OperationContext::diagnostic(OperationKind::Application);
    for index in 0..140 {
        let child = parent
            .child_named(
                OperationKind::Query,
                atomic_core::Keyword::new("app", format!("phase-{index}")),
            )
            .unwrap();
        child.record_payload_read(1);
    }
    let report = parent.report();
    assert_eq!(report.nested.len(), 128);
    assert!(report.nested_truncated);
    assert_eq!(report.stats.known_payload_read_bytes, 140);
}

#[test]
fn nested_scopes_restore_attribution_without_sibling_leakage() {
    let parent = OperationContext::new(OperationKind::Application);
    let child = parent.child(OperationKind::Query);
    let sibling = parent.child(OperationKind::Administration);
    let outer = parent.enter();
    failed_connect();
    {
        let _child = child.enter();
        failed_connect();
        child.record_payload_read(7);
    }
    {
        let _sibling = sibling.enter();
        failed_connect();
        sibling.record_payload_write(11);
    }
    failed_connect();
    drop(outer);
    assert!(OperationContext::current().is_none());
    assert_eq!(parent.snapshot().calls, 4);
    assert_eq!(parent.snapshot().errors, 4);
    assert_eq!(child.snapshot().calls, 1);
    assert_eq!(child.snapshot().known_payload_read_bytes, 7);
    assert_eq!(child.snapshot().known_payload_write_bytes, 0);
    assert_eq!(sibling.snapshot().calls, 1);
    assert_eq!(sibling.snapshot().known_payload_write_bytes, 11);
    assert_eq!(parent.snapshot().known_payload_read_bytes, 7);
    assert_eq!(parent.snapshot().known_payload_write_bytes, 11);
    assert_eq!(parent.snapshot().sql_calls, 0);
    assert!(!format!("{:?}", parent.snapshot()).contains("private"));
}

#[test]
fn repeated_context_scopes_support_out_of_order_drop() {
    let one = OperationContext::new(OperationKind::Query);
    let two = OperationContext::new(OperationKind::Indexing);
    let outer = one.enter();
    let middle = two.enter();
    let inner = one.enter();
    drop(outer);
    failed_connect();
    drop(inner);
    failed_connect();
    drop(middle);
    assert!(OperationContext::current().is_none());
    assert_eq!(one.snapshot().calls, 1);
    assert_eq!(two.snapshot().calls, 1);
}

#[test]
fn concurrent_contexts_attribute_once_to_each_parent() {
    let parent = OperationContext::new(OperationKind::Application);
    let children = (0..4)
        .map(|_| parent.child(OperationKind::Query))
        .collect::<Vec<_>>();
    let barrier = Arc::new(Barrier::new(children.len()));
    let workers = children
        .iter()
        .map(|child| {
            let child = child.clone();
            let barrier = Arc::clone(&barrier);
            std::thread::spawn(move || {
                let _scope = child.enter();
                barrier.wait();
                for _ in 0..8 {
                    failed_connect();
                }
            })
        })
        .collect::<Vec<_>>();
    for worker in workers {
        worker.join().unwrap();
    }
    for child in children {
        assert_eq!(child.snapshot().calls, 8);
    }
    assert_eq!(parent.snapshot().calls, 32);
    assert_eq!(
        parent.snapshot().by_operation[&OperationKind::Query].calls,
        32
    );
}

#[test]
fn blocked_calls_are_visible_before_they_complete() {
    let context = OperationContext::new(OperationKind::Recovery);
    let worker_context = context.clone();
    let (entered, wait_entered) = std::sync::mpsc::channel();
    let (release, wait_release) = std::sync::mpsc::channel();
    let worker = std::thread::spawn(move || {
        let _scope = worker_context.enter();
        let result = SqlClient::connect_with(|| {
            entered.send(()).unwrap();
            wait_release.recv().unwrap();
            Err::<postgres::Client, _>("synthetic blocked connection")
        });
        assert!(result.is_err());
    });
    wait_entered
        .recv_timeout(std::time::Duration::from_secs(2))
        .unwrap();
    let during = context.snapshot();
    assert_eq!(during.calls, 1);
    assert_eq!(during.completed_calls, 0);
    assert_eq!(during.errors, 0);
    release.send(()).unwrap();
    worker.join().unwrap();
    assert_eq!(context.snapshot().calls, 1);
    assert_eq!(context.snapshot().completed_calls, 1);
    assert_eq!(context.snapshot().errors, 1);
}

#[test]
fn callbacks_are_explicit_reentrant_and_panic_contained() {
    let slot: Arc<Mutex<Option<OperationContext>>> = Arc::new(Mutex::new(None));
    let reports = Arc::new(Mutex::new(Vec::<SqlIoReport>::new()));
    let callback_slot = Arc::clone(&slot);
    let callback_reports = Arc::clone(&reports);
    let context = OperationContext::with_callback(
        OperationKind::Query,
        Arc::new(move |report| {
            // Snapshotting the same context proves publish released its stats lock.
            let context = callback_slot.lock().unwrap().as_ref().unwrap().clone();
            assert_eq!(context.snapshot().calls, report.stats.calls);
            let _ = process_sql_stats();
            callback_reports.lock().unwrap().push(report);
        }),
    );
    *slot.lock().unwrap() = Some(context.clone());
    {
        let _scope = context.enter();
        failed_connect();
    }
    assert!(
        reports.lock().unwrap().is_empty(),
        "scope/SQL calls must not invoke user callbacks under caller locks"
    );
    assert!(context.publish());
    assert_eq!(reports.lock().unwrap().len(), 1);
    slot.lock().unwrap().take();
    let panicking = OperationContext::with_callback(
        OperationKind::Query,
        Arc::new(|_| panic!("test callback")),
    );
    assert!(!panicking.publish());
    panicking.record_payload_read(1);
    assert_eq!(panicking.snapshot().known_payload_read_bytes, 1);
}

#[test]
fn unscoped_calls_are_in_process_totals() {
    assert!(OperationContext::current().is_none());
    let before = process_sql_stats();
    failed_connect();
    let after = process_sql_stats();
    assert!(after.connect_calls > before.connect_calls);
    assert!(
        after.by_operation[&OperationKind::Unscoped].calls
            > before
                .by_operation
                .get(&OperationKind::Unscoped)
                .map_or(0, |stats| stats.calls)
    );
}

#[test]
fn phase_wall_timing_is_separate_from_sql_time_and_records_on_drop() {
    let parent = OperationContext::new(OperationKind::Transaction);
    {
        let _scope = parent.enter();
        let phase =
            OperationContext::current_or_process().phase(OperationKind::TransactionAssessment);
        let phase_context = phase.context();
        failed_connect();
        assert!(
            parent.snapshot().phases.is_empty(),
            "open phases have not finished"
        );
        drop(phase);
        assert_eq!(
            phase_context.snapshot().phases[&OperationKind::TransactionAssessment].invocations,
            1
        );
        assert_eq!(phase_context.snapshot().calls, 1);
    }
    assert_eq!(
        parent.snapshot().phases[&OperationKind::TransactionAssessment].invocations,
        1
    );
    assert_eq!(parent.snapshot().sql_calls, 0);
    assert_eq!(parent.snapshot().connect_calls, 1);
    assert!(OperationContext::current().is_none());
}

#[test]
fn postgres_sql_calls_rows_failures_and_drop_rollback_are_counted() {
    let Some(connection) = connection() else {
        return;
    };
    let context = OperationContext::new(OperationKind::Application);
    let _scope = context.enter();
    let mut client = SqlClient::connect(&connection, postgres::NoTls).unwrap();
    client
        .batch_execute("CREATE TEMP TABLE attribution_fixture (payload bytea)")
        .unwrap();
    let mut transaction = client
        .build_transaction()
        .isolation_level(postgres::IsolationLevel::Serializable)
        .read_only(false)
        .deferrable(false)
        .start()
        .unwrap();
    transaction
        .execute(
            "INSERT INTO attribution_fixture VALUES ($1)",
            &[&vec![1_u8, 2, 3]],
        )
        .unwrap();
    transaction.commit().unwrap();
    assert_eq!(
        client
            .query_one("SELECT payload, 42::int4 FROM attribution_fixture", &[])
            .unwrap()
            .get::<_, i32>(1),
        42
    );
    assert!(
        client
            .query_opt("SELECT payload FROM attribution_fixture WHERE false", &[])
            .unwrap()
            .is_none()
    );
    assert!(client.query_one("SELECT 1 / 0", &[]).is_err());
    let statement = client.prepare("SELECT 7::int4").unwrap();
    client.query_one(&statement, &[]).unwrap();
    {
        let mut transaction = client.transaction().unwrap();
        transaction
            .execute(
                "INSERT INTO attribution_fixture VALUES ($1)",
                &[&vec![4_u8, 5]],
            )
            .unwrap();
        // Explicit wrapper Drop invokes and records exactly one rollback.
    }
    assert_eq!(
        client
            .query_one("SELECT count(*) FROM attribution_fixture", &[])
            .unwrap()
            .get::<_, i64>(0),
        1
    );
    client.close().unwrap();
    let stats = context.snapshot();
    assert_eq!(stats.connect_calls, 1);
    assert_eq!(stats.calls, 15);
    assert_eq!(stats.sql_calls, 13);
    assert_eq!(stats.control_calls, 5); // two begins, commit, rollback, close
    assert_eq!(stats.errors, 1);
    assert_eq!(stats.rows, 3);
    assert_eq!(stats.result_cell_bytes, 7 + 4 + 8);
    assert_eq!(stats.by_kind[&SqlCallKind::Rollback].calls, 1);
    assert_eq!(stats.by_kind[&SqlCallKind::QueryOne].errors, 1);
    assert_eq!(
        stats.known_payload_write_bytes, 0,
        "parameters are not re-encoded for metrics"
    );
    eprintln!("PostgreSQL SQL-attribution calls/rows/rollback witness executed");
}

#[test]
fn postgres_lazy_stream_retains_its_context_and_records_abandonment() {
    let Some(connection) = connection() else {
        return;
    };
    let mut client = SqlClient::connect(&connection, postgres::NoTls).unwrap();
    let parent = OperationContext::new(OperationKind::Application);
    let query = parent.child(OperationKind::Query);
    let unrelated = OperationContext::new(OperationKind::Administration);
    let mut rows = {
        let _scope = query.enter();
        client
            .query_raw(
                "SELECT n::int4 FROM generate_series(1, 3) n",
                std::iter::empty::<&(dyn ToSql + Sync)>(),
            )
            .unwrap()
    };
    {
        let _other = unrelated.enter();
        assert_eq!(rows.next().unwrap().unwrap().get::<_, i32>(0), 1);
        assert_eq!(rows.next().unwrap().unwrap().get::<_, i32>(0), 2);
        assert_eq!(rows.next().unwrap().unwrap().get::<_, i32>(0), 3);
        assert!(rows.next().unwrap().is_none());
    }
    drop(rows);
    assert_eq!(query.snapshot().calls, 1);
    assert_eq!(query.snapshot().stream_polls, 4);
    assert_eq!(query.snapshot().rows, 3);
    assert_eq!(query.snapshot().result_cell_bytes, 12);
    assert_eq!(parent.snapshot().stream_polls, 4);
    assert_eq!(unrelated.snapshot().calls, 0);
    assert_eq!(unrelated.snapshot().rows, 0);
    {
        let _scope = query.enter();
        let mut rows = client
            .query_raw(
                "SELECT n FROM generate_series(1, 3) n",
                std::iter::empty::<&(dyn ToSql + Sync)>(),
            )
            .unwrap();
        rows.next().unwrap();
    }
    assert_eq!(query.snapshot().abandoned_streams, 1);
    eprintln!("PostgreSQL SQL-attribution lazy-stream witness executed");
}

use postgres::types::ToSql;

#[test]
fn postgres_owned_generic_trait_covers_raw_fixture_clients_and_transactions() {
    let Some(connection) = connection() else {
        return;
    };
    fn read<C: GenericClient>(client: &mut C) {
        assert_eq!(
            client
                .query_one("SELECT 9::int4", &[])
                .unwrap()
                .get::<_, i32>(0),
            9
        );
    }
    fn nested<C: GenericClient>(client: &mut C) {
        let mut transaction = client.transaction().unwrap();
        read(&mut transaction);
        transaction.rollback().unwrap();
    }
    let mut raw = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
    let context = OperationContext::new(OperationKind::Query);
    {
        let _scope = context.enter();
        read(&mut raw);
        nested(&mut raw);
    }
    let mut raw_transaction = raw.transaction().unwrap();
    {
        let _scope = context.enter();
        read(&mut raw_transaction);
    }
    raw_transaction.rollback().unwrap();
    assert_eq!(context.snapshot().sql_calls, 5);
    assert_eq!(context.snapshot().rows, 3);
    assert_eq!(context.snapshot().by_kind[&SqlCallKind::QueryOne].calls, 3);
    eprintln!("PostgreSQL SQL-attribution raw generic-helper witness executed");
}

#[test]
fn postgres_stream_errors_are_counted_even_after_query_creation() {
    let Some(connection) = connection() else {
        return;
    };
    let mut client = SqlClient::connect(&connection, postgres::NoTls).unwrap();
    let context = OperationContext::new(OperationKind::Query);
    {
        let _scope = context.enter();
        if let Ok(mut rows) = client.query_raw(
            "SELECT n / (3 - n) FROM generate_series(1, 3) n",
            std::iter::empty::<&(dyn ToSql + Sync)>(),
        ) {
            loop {
                match rows.next() {
                    Ok(Some(_)) => {}
                    Ok(None) => panic!("division by zero must fail"),
                    Err(_) => break,
                }
            }
        }
    }
    let stats = context.snapshot();
    assert_eq!(stats.calls, 1);
    assert_eq!(stats.errors, 1);
    assert_eq!(stats.by_kind[&SqlCallKind::QueryRaw].errors, 1);
    assert_eq!(stats.abandoned_streams, 0);
    eprintln!(
        "PostgreSQL streaming failure counted; stream_errors={}",
        stats.stream_errors
    );
}
