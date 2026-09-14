//! Existing async and advisory APIs on fresh opaque storage, with no relational
//! migration, server restart, or mutation of another fixture.
mod common;

use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::os::unix::fs::PermissionsExt;
use std::path::{Path, PathBuf};
use std::sync::{
    Arc,
    atomic::{AtomicBool, AtomicUsize, Ordering},
};
use std::time::{Duration, Instant};

const SCORE: u32 = 1000;
const WAIT: Duration = Duration::from_secs(20);

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            SCORE,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}
fn fixture(label: &str) -> Option<(common::PostgresFixture, PostgresConnectionConfig)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block async/hints PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    BlockDatabase::create(&config, "workflow", schema()).unwrap();
    Some((fixture, config))
}
fn service_config(connection: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: PostgresConnectionConfig::plaintext(connection),
        database_id: "workflow".into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(30),
        renew_interval: Duration::from_secs(5),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    }
}
fn add(entity: EntityRef, value: i64) -> TxOp {
    TxOp::Add {
        entity,
        attribute: SCORE,
        value: Value::Long(value).into(),
    }
}
fn request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::new(key, vec![add(EntityRef::Temp("item".into()), value)])
        .with_tx_instant(1000 + value)
}
fn query() -> Query {
    Query::new(
        FindSpec::Collection(FindElement::Variable("n".into())),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::var("e"),
            Term::Constant(Value::Keyword(Keyword::new("item", "score"))),
            Term::var("n"),
        )))],
    )
}
fn expected(values: &[i64]) -> QueryResult {
    QueryResult::Collection(
        values
            .iter()
            .map(|v| QueryValue::Scalar(Value::Long(*v)))
            .collect(),
    )
}
async fn until(mut condition: impl FnMut() -> bool) {
    tokio::time::timeout(WAIT, async {
        while !condition() {
            tokio::time::sleep(Duration::from_millis(1)).await;
        }
    })
    .await
    .expect("bounded async progress");
}
struct Release(Arc<AtomicBool>);
impl Drop for Release {
    fn drop(&mut self) {
        self.0.store(true, Ordering::Release);
    }
}

#[test]
fn current_thread_async_capture_cancellation_abandoned_waiter_and_exact_retry() {
    let Some((fixture, _)) = fixture("block_async_workflow") else {
        return;
    };
    let service =
        TransactionService::start(service_config(&fixture.connection, "async-first")).unwrap();
    let connection = Connection::attach(&fixture.connection, service.client(), 0).unwrap();
    let independent = Peer::connect(&fixture.connection, "workflow", 0).unwrap();
    let executor = AsyncExecutor::new(AsyncConfig {
        workers: 1,
        max_operations: 16,
        max_resources: 8,
    })
    .unwrap();
    let client = AsyncClient::new(&connection, &executor).unwrap();
    let runtime = tokio::runtime::Builder::new_current_thread()
        .enable_time()
        .build()
        .unwrap();
    let started = Instant::now();
    let first = runtime
        .block_on(client.transact(request("one", 1), WAIT).unwrap())
        .unwrap();
    let first_t = first.basis_t;
    let first_entity = first.tempids["item"];
    drop(first); // Native results are explicitly disposed outside the runtime.

    let entered = Arc::new(AtomicBool::new(false));
    let release = Arc::new(AtomicBool::new(false));
    let _release_on_failure = Release(release.clone());
    let mut extensions = QueryExtensions::default();
    let (seen, gate) = (entered.clone(), release.clone());
    extensions.register_pure("hold", move |_, _| {
        seen.store(true, Ordering::Release);
        let deadline = Instant::now() + WAIT;
        while !gate.load(Ordering::Acquire) {
            assert!(
                Instant::now() < deadline,
                "test query gate was not released"
            );
            std::thread::sleep(Duration::from_millis(1));
        }
        Ok(QueryValue::Scalar(Value::Long(7)))
    });
    let blocker = Query::new(
        FindSpec::Scalar(FindElement::Variable("v".into())),
        vec![Clause::Function {
            function: Function::Extension("hold".into()),
            source: "$unused".into(),
            args: vec![],
            binding: Binding::Scalar("v".into()),
        }],
    );
    let (slow, captured_query, captured_db, mut captured_log) = runtime.block_on(async {
        let slow = executor
            .query_sources(blocker, &[], vec![], Default::default(), Some(extensions))
            .unwrap();
        until(|| entered.load(Ordering::Acquire)).await;
        let query = client.query(query(), vec![], Default::default()).unwrap();
        let db = client.db().unwrap();
        let log = client
            .tx_range(
                None,
                None,
                AsyncStreamOptions {
                    chunk_rows: 1,
                    timeout: Some(WAIT),
                },
            )
            .unwrap();
        tokio::time::sleep(Duration::from_millis(15)).await;
        assert!(
            !release.load(Ordering::Acquire),
            "single-thread timer ran while worker was blocked"
        );
        (slow, query, db, log)
    });
    // The independent blocking API advances only outside the runtime; jobs
    // admitted above must retain the previous immutable value even when queued.
    let second = connection.transact(request("two", 2), WAIT).unwrap();
    let second_t = second.basis_t;
    drop(second);
    assert_eq!(independent.db().basis_t(), first_t - 1);
    let independently_read = independent.sync_to(second_t, WAIT).unwrap();
    assert_eq!(
        independently_read
            .query(&query(), &[], &QueryControl::default())
            .unwrap()
            .result,
        expected(&[1, 2])
    );
    release.store(true, Ordering::Release);
    let retry_request = request("abandoned", 3);
    let callback_calls = Arc::new(AtomicUsize::new(0));
    let (native_values, replay) = runtime.block_on(async {
        slow.await.unwrap();
        assert_eq!(captured_query.await.unwrap().result, expected(&[1]));
        let captured = captured_db.await.unwrap();
        assert_eq!(captured.basis_t(), first_t);
        let mut last_t = 0;
        while let Some(transaction) = captured_log.next().await {
            last_t = transaction.unwrap().t;
        }
        assert_eq!(last_t, first_t);
        assert!(captured_log.next().await.is_none());

        let control = QueryControl::default();
        control.cancel.store(true, Ordering::Release);
        let canceled = client
            .query(query(), vec![], control)
            .unwrap()
            .await
            .unwrap_err();
        assert_eq!(canceled.category, ErrorCategory::Interrupted);
        let control = QueryControl::default();
        let cancel = control.cancel.clone();
        let mut relation = query();
        relation.find = FindSpec::Relation(vec![FindElement::Variable("n".into())]);
        let mut stream = client
            .query_sequence(relation, vec![], control, None, Default::default())
            .unwrap();
        cancel.store(true, Ordering::Release);
        assert_eq!(
            stream.next().await.unwrap().unwrap_err().category,
            ErrorCategory::Interrupted
        );
        assert!(stream.next().await.is_none());
        drop(stream);

        let abandoned = client.transact(retry_request.clone(), WAIT).unwrap();
        drop(abandoned); // Submission survives abandoning its Future.
        let advanced = client.sync_to(second_t + 1, WAIT).unwrap().await.unwrap();
        let replay = client
            .transact(retry_request.clone(), WAIT)
            .unwrap()
            .await
            .unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.basis_t, second_t + 1);
        assert_eq!(replay.db_before.basis_t(), second_t);
        let callbacks = callback_calls.clone();
        client
            .transact(retry_request.clone(), WAIT)
            .unwrap()
            .on_complete(move |result| {
                assert!(
                    std::thread::current()
                        .name()
                        .unwrap()
                        .starts_with("atomic-async-")
                );
                assert!(result.unwrap().replayed);
                callbacks.fetch_add(1, Ordering::SeqCst);
            });
        until(|| callback_calls.load(Ordering::SeqCst) == 1).await;
        assert_eq!(
            client
                .query(query(), vec![], Default::default())
                .unwrap()
                .await
                .unwrap()
                .result,
            expected(&[1, 2, 3])
        );
        let pattern =
            PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Id(SCORE))]);
        let pull = client
            .pull(pattern, first_entity, Default::default(), Some(WAIT))
            .unwrap()
            .await
            .unwrap();
        assert_eq!(
            pull,
            QueryValue::Map(vec![(
                QueryValue::Scalar(Value::Keyword(Keyword::new("item", "score"))),
                QueryValue::Scalar(Value::Long(1))
            )])
        );
        (vec![captured, advanced], replay)
    });
    let retry_t = replay.basis_t;
    let retry_hash = replay.tx_hash;
    drop((native_values, replay, independently_read));
    service.shutdown();
    let restarted =
        TransactionService::start(service_config(&fixture.connection, "async-restart")).unwrap();
    connection.attach_writer(restarted.client()).unwrap();
    let replay = runtime
        .block_on(client.transact(retry_request, WAIT).unwrap())
        .unwrap();
    assert!(replay.replayed);
    assert_eq!((replay.basis_t, replay.tx_hash), (retry_t, retry_hash));
    drop(replay);
    drop(captured_log);
    drop(connection);
    runtime.block_on(async {
        drop(client);
        until(|| executor.stats().operations == 0 && executor.stats().resources == 0).await;
    });
    assert_eq!(callback_calls.load(Ordering::SeqCst), 1);
    restarted.shutdown();
    eprintln!(
        "BLOCK_ASYNC captured_basis={first_t} final_basis={retry_t} callback_count=1 canceled_query=true fused_stream=true dropped_waiter_replayed=true cleanup=true complete_us={}",
        started.elapsed().as_micros()
    );
}

struct PausedCache {
    original: PathBuf,
    parked: PathBuf,
}
impl PausedCache {
    fn new(original: &Path) -> Self {
        let parked = original.with_file_name("parked-cache");
        std::fs::rename(original, &parked).unwrap();
        Self {
            original: original.into(),
            parked,
        }
    }
}
impl Drop for PausedCache {
    fn drop(&mut self) {
        if !self.original.exists() {
            let _ = std::fs::rename(&self.parked, &self.original);
        }
    }
}
fn hint_completed(execution: &HintExecution) -> HintPrefetchStats {
    let deadline = Instant::now() + WAIT;
    loop {
        let stats = execution.snapshot();
        if stats.active_workers == 0 && stats.attempts > 0 {
            return stats;
        }
        assert!(Instant::now() < deadline, "hint did not finish: {stats:?}");
        std::thread::sleep(Duration::from_millis(2));
    }
}

#[test]
fn independent_hints_skip_ssd_and_never_change_native_callback_receipts() {
    let Some((fixture, plain)) = fixture("block_hint_workflow") else {
        return;
    };
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let cache_path = directory.path().to_path_buf();
    let configured = plain.clone().with_ssd_cache(SsdCacheConfig {
        directory: cache_path.clone(),
        limits: SsdCacheLimits::default(),
    });
    let calls = Arc::new(AtomicUsize::new(0));
    let entered = Arc::new(AtomicBool::new(false));
    let release = Arc::new(AtomicBool::new(false));
    let mut registry = NativeRegistry::builder();
    let (count, seen, gate) = (calls.clone(), entered.clone(), release.clone());
    registry
        .transaction(
            Symbol::new("hint.test.v1", "set"),
            move |_, args, control| {
                control.check(1)?;
                count.fetch_add(1, Ordering::SeqCst);
                seen.store(true, Ordering::Release);
                let deadline = Instant::now() + WAIT;
                while !gate.load(Ordering::Acquire) {
                    assert!(
                        Instant::now() < deadline,
                        "native test gate was not released"
                    );
                    std::thread::sleep(Duration::from_millis(1));
                }
                let [RuntimeValue::Scalar(Value::Ref(entity))] = args else {
                    panic!("expected entity");
                };
                Ok(vec![TxForm::Op(add(EntityRef::Id(*entity), 2))])
            },
        )
        .unwrap();
    let mut config = service_config(&fixture.connection, "hints");
    config.connection = configured;
    config.capacity_limits.writer_tree_cache_entries = 0;
    config.capacity_limits.writer_tree_cache_bytes = 0;
    let service = TransactionService::start_with_options(
        config,
        ServiceOptions {
            execution: TransactionExecutionOptions {
                native: registry.build(),
                ..Default::default()
            },
            ..Default::default()
        },
    )
    .unwrap();
    let _release_on_failure = Release(release.clone());
    let client = service.client();
    let seed = client.transact(request("seed", 1), WAIT).unwrap();
    let entity = seed.tempids["item"];
    let target = client.request_index().unwrap().target_t;
    let peer = Peer::connect_configured(&plain, "workflow", 0).unwrap();
    let before = peer.sync_index(target, WAIT).unwrap();
    let preview = before
        .with_forms_with_hints(
            &[TxForm::Op(add(EntityRef::Id(entity), 2))],
            2000,
            SpeculationLimits::default(),
            HintLimits::default(),
        )
        .unwrap();
    assert!(!preview.hints.as_ref().unwrap().reads().is_empty());
    let origin = before.snapshot_reference().unwrap();
    let hints = TransactionHints::from_reads(
        origin.clone(),
        [ReadHint {
            history: true,
            prefix: IndexPrefix::Aevt {
                attribute: SCORE,
                entity: None,
                value: None,
            },
        }],
        HintLimits::default(),
    )
    .unwrap();
    let request = TransactionRequest::from_forms(
        "native-hinted",
        vec![TxForm::ProgramCall(ProgramCall {
            function: CallableRef::Local(Symbol::new("hint.test.v1", "set")),
            arguments: vec![RuntimeValue::Scalar(Value::Ref(entity))],
        })],
    )
    .with_tx_instant(2000);
    assert!(
        std::fs::read_dir(&cache_path).unwrap().any(|entry| entry
            .unwrap()
            .path()
            .extension()
            .is_some_and(|extension| extension == "block")),
        "configured writer really populated SSD files"
    );
    let paused = PausedCache::new(&cache_path);
    let started = Instant::now();
    let (ticket, execution) = client
        .submit_with_hints(
            request.clone(),
            hints.clone(),
            HintPrefetchOptions {
                max_datoms: 1,
                ..Default::default()
            },
        )
        .unwrap();
    let deadline = Instant::now() + WAIT;
    while !entered.load(Ordering::Acquire) {
        assert!(
            Instant::now() < deadline,
            "native authority did not reach its gate"
        );
        std::thread::sleep(Duration::from_millis(1));
    }
    let advisory = hint_completed(&execution);
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    assert!(advisory.completed_workers >= 1);
    assert_eq!(advisory.errors, 0);
    assert_eq!(
        advisory.datoms, 1,
        "hint performed a cold durable prefix read while authority waited"
    );
    assert_eq!(advisory.join_nanos, 0);
    assert!(
        !cache_path.exists(),
        "advisory lane must not open or recreate the writer SSD directory"
    );
    assert_eq!(
        peer.db().values(entity, SCORE).unwrap(),
        vec![Value::Long(1)],
        "independent peer remains readable while native callback waits"
    );
    drop(paused); // Restore our test-owned directory before authoritative reads.
    release.store(true, Ordering::Release);
    let committed = ticket.wait(WAIT).unwrap();
    assert_eq!(committed.tx_data, preview.report.tx_data);
    assert_eq!(
        committed.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(2)]
    );
    assert_eq!(before.values(entity, SCORE).unwrap(), vec![Value::Long(1)]);
    let canceled = HintPrefetchOptions {
        cancel: Arc::new(AtomicBool::new(true)),
        ..Default::default()
    };
    let attached = Connection::attach(&fixture.connection, client.clone(), 0).unwrap();
    let executor = AsyncExecutor::new(AsyncConfig {
        workers: 1,
        max_operations: 4,
        max_resources: 2,
    })
    .unwrap();
    let async_client = AsyncClient::new(&attached, &executor).unwrap();
    let runtime = tokio::runtime::Builder::new_current_thread()
        .enable_time()
        .build()
        .unwrap();
    let (replay, canceled_execution) = runtime
        .block_on(
            async_client
                .transact_with_hints(request.clone(), hints.clone(), canceled, WAIT)
                .unwrap(),
        )
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, committed.tx_hash);
    assert_eq!(hint_completed(&canceled_execution).datoms, 0);
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    drop(replay);
    drop(attached);
    runtime.block_on(async {
        drop(async_client);
        until(|| executor.stats().operations == 0 && executor.stats().resources == 0).await;
    });
    let altered = TransactionHints::from_reads(
        origin,
        [ReadHint {
            history: false,
            prefix: IndexPrefix::Eavt {
                entity,
                attribute: Some(SCORE),
                value: Some(Value::Long(-999)),
            },
        }],
        HintLimits::default(),
    )
    .unwrap();
    let (ticket, altered_execution) = client
        .submit_with_hints(
            TransactionRequest::new("stale-altered", vec![add(EntityRef::Id(entity), 3)])
                .with_tx_instant(3000),
            altered,
            HintPrefetchOptions::default(),
        )
        .unwrap();
    let latest = ticket.wait(WAIT).unwrap();
    hint_completed(&altered_execution);
    assert_eq!(
        latest.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(3)]
    );
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    service.shutdown();
    let mut changed = service_config(&fixture.connection, "hints-restart");
    changed.capacity_limits.max_transaction_bytes = 1;
    let restarted = TransactionService::start(changed).unwrap(); // Native callback is absent.
    let replay = restarted.client().transact(request, WAIT).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, committed.tx_hash);
    assert_eq!(replay.tx_data, committed.tx_data);
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    restarted.shutdown();
    eprintln!(
        "BLOCK_HINTS callback_count=1 cold_hint_datoms={} hint_workers={} hint_errors=0 advisory_ssd_open=false canceled_hint_preserves_receipt=true changed_limits_and_missing_callback_retry=true complete_us={}",
        advisory.datoms,
        advisory.completed_workers,
        started.elapsed().as_micros()
    );
}
