//! Ownership tests use the real private chunk type without building unrelated
//! indexes. Authenticated-tail rebuilding is covered separately below. Each
//! depth witness runs in a subprocess, so a regression cannot abort Cargo's
//! ordinary runner. Timing encloses complete owner release, not an inner loop.
use super::*;
use std::collections::BTreeMap;
use std::process::Command;
use std::sync::{Barrier, Weak};
use std::time::Instant;

const CASE_ENV: &str = "ATOMIC_RECENT_LOG_DROP_CASE";
const SMALL_STACK: usize = 128 * 1024;

fn run_isolated(case: &str) {
    let mut command = Command::new(std::env::current_exe().unwrap());
    command
        .args([
            "--exact",
            "recent::log_drop_tests::isolated_release_case",
            "--nocapture",
            "--test-threads=1",
        ])
        .env(CASE_ENV, case);
    #[cfg(unix)]
    {
        use std::os::unix::process::CommandExt;
        // SAFETY: the child-only hook changes a resource limit without using
        // captured locks or allocating. A regressed child must not write a core
        // file; the parent harness's resource limits are left unchanged.
        unsafe {
            command.pre_exec(|| {
                let limit = libc::rlimit {
                    rlim_cur: 0,
                    rlim_max: 0,
                };
                if libc::setrlimit(libc::RLIMIT_CORE, &limit) == 0 {
                    Ok(())
                } else {
                    Err(std::io::Error::last_os_error())
                }
            });
        }
    }
    let output = command.output().unwrap();
    assert!(
        output.status.success(),
        "case={case}, status={}\nstdout={}\nstderr={}",
        output.status,
        String::from_utf8_lossy(&output.stdout),
        String::from_utf8_lossy(&output.stderr)
    );
    eprint!("{}", String::from_utf8_lossy(&output.stderr));
}

fn entry() -> RecentEntry {
    RecentEntry {
        transaction: Arc::new(DurableTransaction {
            database_id: "ownership-only".into(),
            basis_t: 1,
            previous_hash: [0; 32],
            eidx_frontier: crate::INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::new(),
            tx_data: vec![],
        }),
        hash: [1; 32],
        encoded_bytes: 0,
        accounted_bytes: 0,
    }
}

fn append_chunks(
    mut log: PersistentLog,
    entries: &Arc<[RecentEntry]>,
    count: usize,
) -> PersistentLog {
    for _ in 0..count {
        let total_len = log.len() + entries.len() as u64;
        log.head = Some(Arc::new(LogChunk {
            previous: log.head.take(),
            entries: Arc::clone(entries),
            total_len,
        }));
    }
    log
}

fn ownership_chain(count: usize) -> (PersistentLog, Weak<[RecentEntry]>) {
    // Full-size real chunks share an identical payload only to isolate Arc
    // ownership cost. These synthetic records are not authenticated log data.
    let entries: Arc<[RecentEntry]> = vec![entry(); LOG_CHUNK_SIZE].into();
    let probe = Arc::downgrade(&entries);
    (
        append_chunks(PersistentLog::default(), &entries, count),
        probe,
    )
}

fn sole_owner() {
    for chunks in [1024usize, 8192, 32768] {
        let (log, payload) = ownership_chain(chunks);
        assert_eq!(log.len(), (chunks * LOG_CHUNK_SIZE) as u64);
        let started = Instant::now();
        drop(log);
        let released = started.elapsed();
        // Every node owned this payload. None remaining can be leaked while
        // allowing this weak reference to expire. Verification is outside timing.
        assert!(payload.upgrade().is_none());
        eprintln!(
            "recent-log release=sole chunks={chunks} stack_bytes={SMALL_STACK} complete_us={}",
            released.as_micros()
        );
    }
}

fn retained_predecessor() {
    let entries: Arc<[RecentEntry]> = vec![entry(); LOG_CHUNK_SIZE].into();
    let payload = Arc::downgrade(&entries);
    let predecessor = append_chunks(PersistentLog::default(), &entries, 8192);
    let successor = append_chunks(predecessor.clone(), &entries, 8192);
    drop(entries);
    let started = Instant::now();
    drop(successor);
    let suffix_release = started.elapsed();
    assert!(payload.upgrade().is_some());
    assert_eq!(predecessor.len(), 8192 * LOG_CHUNK_SIZE as u64);
    assert_eq!(predecessor.chunks(), 8192);
    assert_eq!(predecessor.head.as_ref().unwrap().entries[0].hash, [1; 32]);
    let started = Instant::now();
    drop(predecessor);
    let prefix_release = started.elapsed();
    assert!(payload.upgrade().is_none());
    eprintln!(
        "recent-log release=retained chunks=16384 suffix_complete_us={} final_prefix_complete_us={}",
        suffix_release.as_micros(),
        prefix_release.as_micros()
    );
}

fn concurrent_owners() {
    for branch_heads in [false, true] {
        let started = Instant::now();
        let mut complete_release_us = 0;
        for _ in 0..16 {
            let (original, payload) = ownership_chain(8192);
            let (left, right) = if branch_heads {
                let entries = Arc::clone(&original.head.as_ref().unwrap().entries);
                (
                    append_chunks(original.clone(), &entries, 1),
                    append_chunks(original, &entries, 1),
                )
            } else {
                (original.clone(), original)
            };
            let barrier = Arc::new(Barrier::new(3));
            let workers: Vec<_> = [left, right]
                .into_iter()
                .map(|log| {
                    let barrier = Arc::clone(&barrier);
                    std::thread::Builder::new()
                        .stack_size(SMALL_STACK)
                        .spawn(move || {
                            barrier.wait();
                            drop(log);
                        })
                        .unwrap()
                })
                .collect();
            let release = Instant::now();
            barrier.wait();
            for worker in workers {
                worker.join().unwrap();
            }
            complete_release_us += release.elapsed().as_micros();
            assert!(payload.upgrade().is_none());
        }
        eprintln!(
            "recent-log release=concurrent branch_heads={branch_heads} rounds=16 chunks_per_round=8192 complete_us={complete_release_us} whole_fixture_us={}",
            started.elapsed().as_micros()
        );
    }
}

fn authenticated_rebuild() {
    // Reuse the public authentication/reconstruction path used when a peer
    // adopts a newer covering base. Empty transaction payloads isolate the log
    // lifetime from index-tree cost; actual PG datoms are exercised separately.
    const TRANSACTIONS: u64 = 32 * 1024;
    const RETAINED_TAIL: u64 = 33;
    let base_hash = [7; 32];
    let projection = EndpointProjection::new(Schema::new());
    let transactions = (1..=TRANSACTIONS).scan(base_hash, |previous, basis_t| {
        let transaction = DurableTransaction {
            database_id: "authenticated-drop".into(),
            basis_t,
            previous_hash: *previous,
            eidx_frontier: crate::INITIAL_EIDX_FRONTIER,
            tempids: BTreeMap::new(),
            tx_data: Vec::new(),
        };
        *previous = transaction_hash(&encode_transaction(&transaction).unwrap());
        Some(transaction)
    });
    let construction = Instant::now();
    let current = RecentTier::new(
        "authenticated-drop",
        0,
        base_hash,
        transactions,
        projection.clone(),
        RecentLimits::default(),
    )
    .unwrap();
    let construction_us = construction.elapsed().as_micros();
    assert_eq!(current.stats().transactions, TRANSACTIONS);
    assert_eq!(current.log.chunks(), TRANSACTIONS / LOG_CHUNK_SIZE as u64);
    let retained = current.clone();
    let entries = current.entries();
    assert_eq!(entries[0].transaction.basis_t, 1);
    assert_eq!(entries.last().unwrap().transaction.basis_t, TRANSACTIONS);
    let old_payload = Arc::downgrade(&entries[0].transaction);
    let new_base = (TRANSACTIONS - RETAINED_TAIL) as usize;
    let new_hash = entries[new_base - 1].hash;
    let tail = entries[new_base..]
        .iter()
        .map(|entry| entry.transaction.as_ref().clone())
        .collect::<Vec<_>>();
    let final_hash = entries.last().unwrap().hash;
    drop(entries);

    let adoption = Instant::now();
    let rebuilt = RecentTier::new(
        "authenticated-drop",
        new_base as u64,
        new_hash,
        tail,
        projection,
        RecentLimits::default(),
    )
    .unwrap();
    drop(current);
    let adoption_us = adoption.elapsed().as_micros();
    assert!(
        old_payload.upgrade().is_some(),
        "retained database value still owns its complete old tail"
    );
    assert_eq!(retained.stats().end_hash, final_hash);
    assert_eq!(retained.stats().transactions, TRANSACTIONS);
    assert_eq!(rebuilt.stats().base_t, new_base as u64);
    assert_eq!(rebuilt.stats().transactions, RETAINED_TAIL);
    assert_eq!(rebuilt.stats().end_hash, final_hash);

    let release = Instant::now();
    drop(retained);
    let old_release_us = release.elapsed().as_micros();
    assert!(old_payload.upgrade().is_none());
    let remaining = rebuilt.entries();
    assert_eq!(remaining[0].transaction.basis_t, new_base as u64 + 1);
    assert_eq!(remaining.last().unwrap().transaction.basis_t, TRANSACTIONS);
    let tail_payload = Arc::downgrade(&remaining[0].transaction);
    drop(remaining);
    let release = Instant::now();
    drop(rebuilt);
    let tail_release_us = release.elapsed().as_micros();
    assert!(tail_payload.upgrade().is_none());
    eprintln!(
        "recent-log release=authenticated-rebuild transactions={TRANSACTIONS} chunks=1024 retained_tail={RETAINED_TAIL} construction_us={construction_us} complete_adoption_us={adoption_us} complete_old_release_us={old_release_us} complete_tail_release_us={tail_release_us}"
    );
}

#[test]
fn recent_log_sole_owner_release_is_stack_safe() {
    run_isolated("sole");
}

#[test]
fn recent_log_retained_predecessor_survives_successor_release() {
    run_isolated("retained");
}

#[test]
fn recent_log_concurrent_final_owners_release_all_chunks() {
    run_isolated("concurrent");
}

#[test]
fn authenticated_recent_tail_rebuild_preserves_old_snapshot_until_final_release() {
    run_isolated("authenticated");
}

#[test]
fn isolated_release_case() {
    let Ok(case) = std::env::var(CASE_ENV) else {
        return;
    };
    std::thread::Builder::new()
        .stack_size(SMALL_STACK)
        .spawn(move || match case.as_str() {
            "sole" => sole_owner(),
            "retained" => retained_predecessor(),
            "concurrent" => concurrent_owners(),
            "authenticated" => authenticated_rebuild(),
            other => panic!("unknown isolated case {other}"),
        })
        .unwrap()
        .join()
        .unwrap();
}
