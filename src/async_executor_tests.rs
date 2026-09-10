use super::*;

fn runtime() -> tokio::runtime::Runtime {
    tokio::runtime::Builder::new_current_thread()
        .enable_time()
        .build()
        .unwrap()
}
async fn until(mut predicate: impl FnMut() -> bool) {
    tokio::time::timeout(Duration::from_secs(5), async {
        while !predicate() {
            tokio::time::sleep(Duration::from_millis(1)).await;
        }
    })
    .await
    .expect("worker reached the expected state");
}
struct SlowDrop {
    entered: Arc<AtomicBool>,
    release: Arc<AtomicBool>,
    thread: Arc<Mutex<Option<String>>>,
}
impl Drop for SlowDrop {
    fn drop(&mut self) {
        *self.thread.lock().unwrap() = std::thread::current().name().map(str::to_owned);
        self.entered.store(true, Ordering::Release);
        let deadline = Instant::now() + Duration::from_secs(5);
        while !self.release.load(Ordering::Acquire) && Instant::now() < deadline {
            std::thread::sleep(Duration::from_millis(1));
        }
    }
}

#[test]
fn abandoned_completed_results_and_resources_drop_only_on_workers() {
    let executor = AsyncExecutor::new(AsyncConfig {
        workers: 1,
        max_operations: 1,
        max_resources: 1,
    })
    .unwrap();
    for resource in [false, true] {
        let entered = Arc::new(AtomicBool::new(false));
        let release = Arc::new(AtomicBool::new(false));
        let thread = Arc::new(Mutex::new(None));
        let probe = SlowDrop {
            entered: entered.clone(),
            release: release.clone(),
            thread: thread.clone(),
        };
        runtime().block_on(async {
            if resource {
                let retained = executor.retain(|| probe).unwrap();
                drop(retained);
            } else {
                let operation = executor
                    .reserve()
                    .unwrap()
                    .run(None, false, move || Ok(probe));
                until(|| executor.stats().completed >= 1).await;
                assert!(
                    executor.reserve().is_err(),
                    "unconsumed completion owns its slot"
                );
                drop(operation);
            }
            until(|| entered.load(Ordering::Acquire)).await;
            // The sole worker is in SlowDrop. A timer on this sole executor
            // thread must still advance before that Drop is allowed to finish.
            tokio::time::sleep(Duration::from_millis(10)).await;
            assert!(!release.load(Ordering::Acquire));
            assert!(
                thread
                    .lock()
                    .unwrap()
                    .as_ref()
                    .unwrap()
                    .starts_with("atomic-async-")
            );
            release.store(true, Ordering::Release);
            until(|| executor.stats().operations == 0 && executor.stats().resources == 0).await;
        });
    }
    assert_eq!(executor.stats().peak_running, 1);
}

#[test]
fn callback_registration_races_are_once_only_and_panics_do_not_kill_workers() {
    let executor = AsyncExecutor::new(AsyncConfig {
        workers: 2,
        max_operations: 8,
        max_resources: 1,
    })
    .unwrap();
    let calls = Arc::new(AtomicUsize::new(0));
    runtime().block_on(async {
        for index in 0..128 {
            let operation = executor
                .reserve()
                .unwrap()
                .run(None, false, move || Ok(index));
            if index % 2 == 0 {
                until(|| executor.stats().completed > index).await;
            }
            let calls = calls.clone();
            operation.on_complete(move |result| {
                assert!(
                    std::thread::current()
                        .name()
                        .unwrap()
                        .starts_with("atomic-async-")
                );
                assert_eq!(result.unwrap(), index);
                calls.fetch_add(1, Ordering::AcqRel);
            });
            until(|| executor.stats().operations == 0).await;
        }
        assert_eq!(calls.load(Ordering::Acquire), 128);
        let completed_before = executor.stats().completed;
        let callback_panicked = executor.reserve().unwrap().run(None, false, || Ok(()));
        callback_panicked.on_complete(|_| panic!("isolated async callback panic"));
        until(|| executor.stats().operations == 0 && executor.stats().worker_panics == 1).await;
        assert_eq!(executor.stats().completed, completed_before + 1);
        assert_eq!(executor.stats().worker_panics, 1);
        let failed = executor
            .reserve()
            .unwrap()
            .run::<usize>(None, false, || panic!("isolated async operation panic"));
        assert_eq!(failed.await.unwrap_err().code, "async/worker-panic");
        let value = executor
            .reserve()
            .unwrap()
            .run(None, false, || Ok(42))
            .await
            .unwrap();
        assert_eq!(value, 42);
    });
}

#[test]
fn stream_drop_defers_buffer_and_live_cursor_cleanup() {
    struct Cursor {
        rows: VecDeque<SlowDrop>,
        _drop: SlowDrop,
    }
    impl Iterator for Cursor {
        type Item = Result<SlowDrop, SemanticError>;
        fn next(&mut self) -> Option<Self::Item> {
            self.rows.pop_front().map(Ok)
        }
    }
    let executor = AsyncExecutor::new(AsyncConfig {
        workers: 1,
        max_operations: 1,
        max_resources: 1,
    })
    .unwrap();
    let entered = Arc::new(AtomicBool::new(false));
    let release = Arc::new(AtomicBool::new(false));
    let thread = Arc::new(Mutex::new(None));
    let probe = || SlowDrop {
        entered: entered.clone(),
        release: release.clone(),
        thread: thread.clone(),
    };
    let cursor = Cursor {
        rows: VecDeque::from([probe(), probe()]),
        _drop: probe(),
    };
    let mut stream = super::super::stream::AsyncStream::new(
        &executor,
        super::super::stream::AsyncStreamOptions {
            chunk_rows: 2,
            timeout: None,
        },
        None,
        || Box::new(move || Ok(Box::new(cursor))),
    )
    .unwrap();
    let yielded_native_value = runtime().block_on(async {
        let first = stream.next().await.unwrap().unwrap();
        assert_eq!(stream.buffered_rows(), 1);
        drop(stream);
        until(|| entered.load(Ordering::Acquire)).await;
        tokio::time::sleep(Duration::from_millis(10)).await;
        assert!(!release.load(Ordering::Acquire));
        assert!(
            thread
                .lock()
                .unwrap()
                .as_ref()
                .unwrap()
                .starts_with("atomic-async-")
        );
        release.store(true, Ordering::Release);
        until(|| executor.stats().resources == 0 && executor.stats().operations == 0).await;
        first
    });
    // A consumed generic item is caller-owned, unlike the abandoned buffer.
    drop(yielded_native_value);
}

#[test]
fn dropped_transaction_waiter_runs_once_and_expired_queued_job_never_attempts() {
    let executor = AsyncExecutor::new(AsyncConfig {
        workers: 1,
        max_operations: 3,
        max_resources: 1,
    })
    .unwrap();
    let entered = Arc::new(AtomicBool::new(false));
    let release = Arc::new(AtomicBool::new(false));
    let calls = Arc::new(AtomicUsize::new(0));
    runtime().block_on(async {
        let block_entered = entered.clone();
        let block_release = release.clone();
        let blocker = executor.reserve().unwrap().run(None, false, move || {
            block_entered.store(true, Ordering::Release);
            while !block_release.load(Ordering::Acquire) {
                std::thread::sleep(Duration::from_millis(1));
            }
            Ok(())
        });
        until(|| entered.load(Ordering::Acquire)).await;
        let observed = calls.clone();
        let transaction = executor.reserve().unwrap().run(None, true, move || {
            observed.fetch_add(1, Ordering::AcqRel);
            Ok(())
        });
        drop(transaction);
        let observed = calls.clone();
        let expired = executor.reserve().unwrap().run(
            Some(Instant::now() + Duration::from_millis(5)),
            true,
            move || {
                observed.fetch_add(100, Ordering::AcqRel);
                Ok(())
            },
        );
        tokio::time::sleep(Duration::from_millis(15)).await;
        release.store(true, Ordering::Release);
        blocker.await.unwrap();
        assert_eq!(expired.await.unwrap_err().code, "async/deadline");
        until(|| executor.stats().operations == 0).await;
        assert_eq!(calls.load(Ordering::Acquire), 1);
    });
}
