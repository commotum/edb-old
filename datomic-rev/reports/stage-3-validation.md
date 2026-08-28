# Stage 3 concurrency and fault validation

Date: 2026-08-27

## Result

Stage 3 passed a bounded, fresh-JVM concurrency and transport-failure matrix
against disposable PostgreSQL 16.15.  Every candidate JVM used the exact
535-entry classpath re-audited by the Stage 2 dry-run gate.  The recovered
source artifact was the only Peer/core2 implementation; the licensed
transactor was a separately fingerprinted external fixture.

The retained post-Stage-4 successful run is
`/tmp/datomic-stage3-stage4-final-v1`. Its summary records
`stage.complete=true`, `services.stopped=true`, and canonical peer-state
SHA-256
`abe3a8e000587079b64965cb99d468ef355d14ef11bd0855aeefab4eba394510`.

## Candidate and fixture boundary

The aggregate runner first invokes the complete Stage 2 `--dry-run` gate.  It
then copies and hashes that gate's candidate classpath and origin result before
loading any Stage 3 probe.  The successful run recorded:

- recovered artifact SHA-256
  `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`;
- 535 candidate entries, with original Peer, core2, and transactor classes all
  absent;
- licensed transactor JAR SHA-256
  `d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692`;
- PostgreSQL binary SHA-256
  `201d8daff9a5bd9df70b820471b965c39f5a776d677f395232c477d051270722`;
- fixed candidate settings: connection TTL 10,000 ms, transaction timeout
  10,000 ms, and query-pool size 2.

The external fixtures bound only to `127.0.0.1` on dedicated ports.  The
runner requires a visibly disposable catalog/work-root and an exact execution
confirmation token.  Its exit trap resumes a paused owned transactor before
any stop attempt and refuses to signal a PID whose command line no longer
matches the owned launcher and properties file.

## Matrix

Each row below ran in a separate candidate JVM with an outer deadline and one
machine-checkable `STAGE3-RESULT` marker.

| Case | Observable result |
|---|---|
| Promise listeners and cleanup | Deterministic delivery, failure, cancellation, listener order, and manual phantom-reference dequeue passed. |
| core2 async/IOC | `aderef`, timeout, IOC retry, completion, and `put-all!` paths passed; owned channels were closed. |
| Pool rejection | Saturated discard/rejection and recovered `pfuture` value, failure, and blocked-release behavior passed; owned executors terminated. |
| Local query timeout | The recovered `qsqr` scheduler, dynamic cancel binding, exact timeout message, `TimeoutException`, and binding restoration passed. |
| SQL query controls | Explicit `d/cancel` retained its conflict category, cancellation flag, stable reason/message; a real query timed out at 30 ms with exact `Query canceled: timeout elapsed`; a following write succeeded. |
| Connection lifecycle race | Four immutable snapshots survived release, four synchronized late users received `:db.error/connection-released`, and reconnect returned a distinct usable object. |
| SQL CAS contention | Eight rounds × eight simultaneous workers produced exactly 8 commits and 56 verified CAS conflicts; every loser retained conflict/cancel metadata. Counter history contained exactly one transition per round. |
| Canonical audit | Baseline rows, query/lifecycle markers, counter value/history, and eight contention events produced the fixed canonical SHA above. |
| Transport interruption | A verified transactor PID entered stopped state under `SIGSTOP`; zero-argument sync reported exact unavailable; the pause lasted at least 10 seconds; `SIGCONT` recovered sync; one sentinel transaction committed and read back. |
| Post-transport audit | A new JVM reproduced the exact pre-fault canonical SHA, proving the fault/recovery path did not change the bounded workload dataset. |

The local query probe intentionally replaces only `eval-query` with a bounded
barrier after the recovered timeout machinery has scheduled cancellation.  It
does not claim to be an expensive-query benchmark.  The SQL query-control case
provides the integration coverage.

## Transport evidence

The runner waits for an explicit fault-ready marker before checking the owned
transactor PID and issuing `SIGSTOP`.  It verifies `/proc/<pid>/status` reports
stopped state, starts a 45-second fail-safe `SIGCONT` watchdog, and only then
allows the candidate to begin its bounded sync.

The successful trace recorded:

- exact `:cognitect.anomalies/unavailable` during the pause;
- normal orchestrator resume before the watchdog fired;
- recovery on attempt 52 after 51 bounded unavailable retries;
- basis T 1053 before the fault and after the recovery sync;
- one sentinel entity, one read result, and one transaction sentinel datom;
- sentinel basis advancement from 1053 to 1055;
- identical pre- and post-transport peer audit results.

All seven transport markers were present exactly once and in order.  The
runner stopped the transactor and PostgreSQL, verified both ports were closed,
and only then wrote `stage.complete=true`.

## Fresh-JVM termination boundary

An early complete run exposed a harness defect: the SQL query-control operation
and its assertions succeeded, but Datomic's process-global query executors kept
the dedicated JVM alive after Peer cleanup.  The final probe performs a bounded
`d/shutdown false`, runs `shutdown-agents`, emits success only after that cleanup
returns, and then explicitly exits its owned fresh JVM.  Cleanup exceptions or
a 30-second cleanup timeout emit `STAGE3-ERROR` and cannot pass.  This boundary
does not hide per-case executor leaks: every executor created by a probe is
separately shut down and awaited before the result is returned.

Two other retained attempts are diagnostic rather than product failures:

- v2 reached PostgreSQL start but the filesystem/network sandbox denied local
  socket creation; cleanup completed and no transactor was started;
- v3 demonstrated the successful query assertions followed by the
  process-global-thread hang described above, then the aggregate timeout trap
  stopped both fixtures.

## Evidence hashes

The successful directory contains 64 hashed evidence files. Top-level hashes
for the final post-Stage-4 run are:

| Record | SHA-256 |
|---|---|
| `config.properties` | `eff2d4d4e9768bdb55abbec61c863539229b9181ac3f9feca05119f3d2e20db5` |
| `stage-3-summary.properties` | `462dee72fdf018ebd9e44355009c0a8004903f04b503419e6faefdb48ce82733` |
| `run-status.properties` | `8afd73fa0018d4ecbe106aedaef90ea5032fe216f27077282822d751f0b8cbff` |
| `evidence.sha256` | `f3033d28744e604e621dcbe9d0596a73f46c6ab8860d4db9ff84a878d9736bc8` |
| Stage 3 harness manifest | `b6ba343b0d0c2c9a610de0b359aeb3fba358a1fa9b468ff2cec030a97a6168d4` |

The pre- and post-transport audit result files are byte-identical, both with
SHA-256
`8c434b3f37258153bf701c39adb26ae6405f509fb0c10c130298241649d01d1a`.

## Reproduce

First run the non-SQL cases against a Stage 2-audited candidate classpath:

```bash
scripts/stage3/validate-local.sh "$CANDIDATE_CLASSPATH" \
  /tmp/datomic-stage3-local
```

Run the complete disposable PostgreSQL gate:

```bash
scripts/stage3/validate-postgresql.sh \
  --datomic-home /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  --artifact /tmp/datomic-stage1-stage4-final-v1/build-a/datomic-rev-peer-1.0.7277-source.jar \
  --postgres-root /tmp/datomic-postgres-16-root \
  --work-root /tmp/datomic-stage3-stage4-final-v1 \
  --pg-port 55437 \
  --transactor-port 54341 \
  --confirm-disposable DATOMIC_STAGE3_DISPOSABLE
```

The work root must be absent or empty and its basename must start with
`datomic-stage3-`.  Use unused loopback ports and no production catalog or
service.

## Claim boundary

This is a bounded resilience suite, not a proof over all schedules, loads,
backends, or distributed failures.  The contention schedule is deliberately
small and repeatable.  The transport test pauses one co-located external
transactor and does not model packet reordering, multi-transactor failover,
security faults, or a long-duration partition.  Those cases remain outside the
demonstrated boundary.
