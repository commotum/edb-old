# Stage 3 concurrency and fault validation

Date: 2026-08-27

## Result

Stage 3 passed a bounded, fresh-JVM concurrency and transport-failure matrix
against disposable PostgreSQL 16.15.  Every candidate JVM used the exact
535-entry classpath re-audited by the Stage 2 dry-run gate.  The recovered
source artifact was the only Peer/core2 implementation; the licensed
transactor was a separately fingerprinted external fixture.

The latest hardened adversarial final successful run is
`/tmp/datomic-stage3-adversarial-final-v2`. Its summary records
`stage.complete=true`, `services.stopped=true`, and canonical peer-state
SHA-256
`abe3a8e000587079b64965cb99d468ef355d14ef11bd0855aeefab4eba394510`.
The directory is local, ephemeral run evidence rather than durable repository
content.

## Candidate and fixture boundary

The aggregate runner first invokes the complete Stage 2 `--dry-run` gate. It
verifies that gate's 19-file evidence manifest, copies every manifested file,
reverifies the copy, and confirms that the exhaustive 45-file Stage 2 harness
manifest still matches the shared scripts classpath before loading any Stage 3
probe. It also rehashes the exact four-class Hot Rod stub tree after that
preflight and retains the independent recheck as
`inputs/infinispan-stubs-recheck.tsv`. The successful run recorded:

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
- recovery on attempt 47 after 46 bounded unavailable retries;
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

Two earlier development attempts are diagnostic rather than product failures:

- the first reached PostgreSQL start but the filesystem/network sandbox denied local
  socket creation; cleanup completed and no transactor was started;
- the next demonstrated the successful query assertions followed by the
  process-global-thread hang described above, then the aggregate timeout trap
  stopped both fixtures.

## Evidence hashes

The successful directory contains 88 hashed evidence files. The additional
file relative to the preceding run is the retained Hot Rod stub recheck named
above. Top-level hashes for the hardened adversarial final run are:

| Record | SHA-256 |
|---|---|
| `config.properties` | `f6d19ef47c1027dd2835257ee8e4f867f23084bbe0664c2285020ac8cea55ab0` |
| `candidate-classpath.tsv` | `ebd63377a1f3ebe799e0f8a1ff405351c58e5dab4aa482bc3d9a3ec07c6ee5ac` |
| `stage-3-summary.properties` | `1d41a632788e515bea16d71c663140eb9d97defcc411a1436ee8b97092a35c06` |
| `run-status.properties` | `8afd73fa0018d4ecbe106aedaef90ea5032fe216f27077282822d751f0b8cbff` |
| `evidence.sha256` | `8f69d3fac2ab0d7d59f449db2c2e3489b292d24a123522a023754a94b01984f6` |
| Stage 3 harness manifest | `814c71bbc321fd19beca87bc33c2025f3805702097c4392e7f3103b138a748e6` |
| Copied Stage 2 preflight `evidence.sha256` (19 files) | `dc20a8ea052444170ddd7792c1cafef6bbf473917cd9e1c8a725fcaa83b12bcb` |
| Stage 2 harness recheck (45 files) | `9d7a01374465990c17cc83c214395591a653cf572439220b66e4d1e6384e17b1` |
| Hot Rod stub recheck (4 classes) | `5bb9a3440c4fe00f3b299a7e48fbf2aa6ef5f3ef2208fe389af13304eb267e02` |

The pre- and post-transport audit result files are byte-identical, both with
SHA-256
`8c434b3f37258153bf701c39adb26ae6405f509fb0c10c130298241649d01d1a`.
The 88-file manifest currently verifies in full, but the raw `/tmp` directory
is a local ephemeral record, not a durable evidence archive.

## Reproduce

First create a hash-verifiable Stage 2 dry-run root, then pass that root and a
new empty work root to the non-SQL runner:

```bash
DATOMIC_HOME=${DATOMIC_HOME:-../../datomic/datomic-pro-1.0.7277}
ARTIFACT=/tmp/datomic-stage-1/build-a/datomic-rev-peer-1.0.7277-source.jar
POSTGRES_ROOT=${POSTGRES_ROOT:?set this to a PostgreSQL 16 installation root}
STAGE2_DRY_RUN_ROOT=/tmp/datomic-stage2-dry-run
STAGE3_LOCAL_ROOT=/tmp/datomic-stage3-local

scripts/stage2/validate-postgresql.sh \
  --datomic-home "$DATOMIC_HOME" \
  --artifact "$ARTIFACT" \
  --postgres-root "$POSTGRES_ROOT" \
  --work-root "$STAGE2_DRY_RUN_ROOT" \
  --dry-run

scripts/stage3/validate-local.sh \
  "$STAGE2_DRY_RUN_ROOT" "$STAGE3_LOCAL_ROOT"
```

Run the complete disposable PostgreSQL gate:

```bash
STAGE3_PG_PORT=${STAGE3_PG_PORT:-55437}
STAGE3_TRANSACTOR_PORT=${STAGE3_TRANSACTOR_PORT:-54341}

scripts/stage3/validate-postgresql.sh \
  --datomic-home "$DATOMIC_HOME" \
  --artifact "$ARTIFACT" \
  --postgres-root "$POSTGRES_ROOT" \
  --pg-port "$STAGE3_PG_PORT" \
  --transactor-port "$STAGE3_TRANSACTOR_PORT" \
  --confirm-disposable DATOMIC_STAGE3_DISPOSABLE
```

All explicit work roots above must be absent or empty and use the required
`datomic-stage2-` or `datomic-stage3-` basename. Omitting the aggregate
`--work-root` creates a fresh `mktemp` directory. The gate requires Linux,
readable `/proc`, GNU `find`, `readlink`, and `timeout`, plus `SIGSTOP` and
`SIGCONT`. `POSTGRES_ROOT` is user supplied. Confirm that both aggregate ports
are unused, and use no production catalog or service.

## Claim boundary

This is a bounded resilience suite, not a proof over all schedules, loads,
backends, or distributed failures.  The contention schedule is deliberately
small and repeatable.  The transport test pauses one co-located external
transactor and does not model packet reordering, multi-transactor failover,
security faults, or a long-duration partition.  Those cases remain outside the
demonstrated boundary. The paused licensed Transactor is an external fixture;
this matrix neither recovers it nor completes the separate educational
Transactor target.
