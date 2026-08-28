# Stage 2 PostgreSQL and recovery validation

Date: `2026-08-27` (`America/Los_Angeles`).

Status: **PASS**.

The canonical source-built peer completed one safety-gated run covering a
disposable PostgreSQL lifecycle, full and incremental backup/restore, failed
root publication, missing and unreadable segments, restore rejection,
interrupted restore, incremental retry, exact logical recovery, and
post-recovery writes.

The complete retained post-Stage-4 run is
`/tmp/datomic-stage2-stage4-final-v1`. Its summary
records `stage.complete=true`, `fault-injection.included=true`, and
`services.stopped=true`.

## Candidate and fixture boundary

| Property | Value |
|---|---|
| Candidate artifact | `datomic-rev-peer-1.0.7277-source.jar` |
| Candidate SHA-256 | `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe` |
| Candidate classpath | 535 entries: artifact, test harness, four-class Hot Rod stub tree, 532 dependencies |
| Dependency manifest SHA-256 | `76a02d5dfd78cd63451944856cdf472c8802a63032c86e021d1f1c9450dc2058` |
| Original Peer on candidate classpath | false |
| Original core2 on candidate classpath | false |
| Transactor implementation on candidate classpath | false |
| Java | Ubuntu OpenJDK `21.0.12+8-1-24.04-Ubuntu` |
| PostgreSQL | `16.15`, bound to `127.0.0.1` |
| PostgreSQL binary SHA-256 | `201d8daff9a5bd9df70b820471b965c39f5a776d677f395232c477d051270722` |
| Licensed transactor JAR SHA-256 | `d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692` |
| Transactor role | separate external fixture only |

Every candidate JVM used the recovered artifact as its only Datomic peer/core2
implementation. The licensed transactor ran in a separate process and never
entered the candidate classpath. It supplied the external transaction service
required by the SQL peer protocol; this is evidence about the recovered peer,
not evidence of a recovered transactor.

The run copied and hash-recorded all origin-gate inputs and all nine executed
harness files. Before touching a database it reran the artifact-origin gate and
the test-storage self-test. The candidate origin, dependency set, Java runtime,
PostgreSQL binaries, transactor fixture, ports, catalog names, and mutable paths
are recorded in `config.properties` and `candidate-classpath.tsv`.

## Normal lifecycle

Five PostgreSQL catalogs began with zero `datomic_kvs` rows. The source and
restore workloads then produced these results:

| Case | Evidence |
|---|---|
| Seed | `t1=1001`, 64 rows, logical SHA-256 `2795bae60836639c1202a80a9ed0f0a6f521119cad7f013001eb50218e4a307d` |
| Full backup | 32 copied, 0 skipped; 32 reachable segments; clean verification; root `[1001]` |
| Augment | `t2=1066`, 96 rows, logical SHA-256 `c1960691a114d43de53d1c8511d3174dbb79b6b1bed89c9b1938656e66663e57` |
| Incremental backup | 0 copied, 2 skipped; clean verification; roots `[1066 1001]` |
| Full t1 restore | 32 copied; exact t1 logical, datom, history, row, and database identity state |
| t1 writability | one sentinel only; basis `1001 -> 1066`; rows `64 -> 65` |
| Incremental target base | full t1 restore copied 32 |
| Incremental t2 restore | 2 skipped/reused; exact t2 state |
| t2 writability | one sentinel only; basis `1066 -> 1099`; rows `96 -> 97` |

The zero-copy t2 backup is expected content-addressed behavior: it reused two
existing immutable values and published exactly one new physical restore-point
root. The distinct basis and exact logical hash prove that the t2 restore point
is not the t1 state.

## Fault and recovery matrix

The source was advanced to a third guarded state before fault injection:

| Property | t3 value |
|---|---|
| Basis | `1099` |
| Rows | 128 |
| Logical SHA-256 | `de1debf98a63e10fa775591c7a57d557d559e7af1c42a4d676f10882cb0ca684` |
| Current-datoms SHA-256 | `1ab64140dc6f63b2b24cb38ef7d4be04b9e6ad3f111dd25a79230f0bc02f27f7` |
| History SHA-256 | `8193e8a556ea8416f703ef778acf8aef8195f3f03d7b2db4fd812019f2290951` |
| Rows SHA-256 | `9aa43c62c8a3b502ebe7749c6d7f6a80953ced6a3b2aa196f50a13236182d1fa` |

### Failed root publication and retry

The candidate requested and awaited an index through t3, copied 43 new
immutable values, skipped 9 existing values, and then injected exactly one
failure while storing the new root. The value count grew from 32 to 75, but
both the logical description and physical root inventory remained exactly
`[1066 1001]`. The captured cause chain contains
`:injected-root-store-failure`; no false t3 restore point was published.

A normal incremental retry then copied 43, skipped 9, published exactly root
`1099`, and verified 52 reachable segments with roots `[1099 1066 1001]`.

### Missing and unreadable leaves

The harness deterministically selected the topology-independent leaf
`values/00/6a90c08b-4392-46f8-a963-5dbceb33ed00`. Two complete disposable
backup copies were made. The selected leaf was moved out of one and replaced
with an empty file in the other.

- Full read verification of the missing copy returned exactly that one segment
  in `missing-segment-ids` and no unreadable segments.
- Full read verification of the corrupt copy returned exactly that one segment
  in `unreadable-segment-ids` and no missing segments.
- Restore from each copy exited 1 with the normalized
  `:restore-probe-failed` boundary and no success marker.
- The shared fresh rejection target remained at zero SQL rows after each
  attempt because verification failed before `restore-db` was entered.

### Interrupted restore and recovery

The interruption probe selected a different topology-independent leaf and
withheld it only after observing copied progress. The first full restore copied
48 values, injected the miss exactly once after copied count 5, and failed with
`:restore/read-failed`. It did not return success.

After worker progress quiesced, a clean incremental retry copied 4 values,
reused 28, and returned `:succeeded`. A separate transactor was then started on
that recovered catalog. The candidate observed the exact t3 database identity,
logical, datom, history, row count, and hashes before writing one sentinel. The
write advanced basis `1099 -> 1132` and rows `128 -> 129` without altering the
128 pre-existing workload rows.

## Source repairs exposed by Stage 2

Three recovered-source defects were fixed only after the original bytecode
uniquely established their intended forms:

- `datomic.future/filling-promise`: a decompiler-created inner self-name caused
  recursion and `StackOverflowError`; original bytecode invokes the captured
  outer callable, so the inner function is now anonymous.
- `datomic.memory-size`: the object-array branch discarded its loop result;
  original bytecode returns the boxed long, which the recovered branch now
  does.
- `datomic.common/pfuture`: reflective submission selected
  `submit(Runnable)` and discarded results. Original bytecode casts to
  `Callable` and invokes `submit(Callable)`, so that exact hint was restored.

Focused success/failure regressions cover all three. Historically, the
`Callable` repair reduced the reflection-warning inventory from 306 to 305.
The final Stage 1 artifact also includes the separately documented Stage 4
bytecode-proven repairs, leaving an exact 150/6/1 warning inventory. Its full
two-build gate retained all 142 namespace loads and surfaces, the exact
47/108/279 Java surface, and in-memory parity. This complete Stage 2 matrix was
then rerun against that final artifact, rather than relying on the earlier
pre-Stage-4 PostgreSQL result.

## Test-only storage boundary

The recovered artifact does not contain `datomic.fsbackup`; that implementation
exists only in the licensed distribution. To keep candidate JVMs clean, this
stage directly exercised recovered `datomic.backup` through
`scripts/stage2/storage.clj`, a synchronous test-only implementation of
`datomic.backup/Storage` registered under `stage2-file:`. It is not packaged in
the artifact and is not a production storage implementation.

The adapter self-test covers ByteBuffer duplication, atomic writes, concurrent
directory creation, sorted full-key listings, path traversal rejection,
symlink rejection, URI registration, and fault wrappers. This stage therefore
validates the recovered backup engine and its storage-protocol behavior, but
not Datomic's proprietary `file:` adapter or public backup CLI.

## Reproduce

Run the complete gate in a new work directory with unused loopback ports:

```bash
scripts/stage2/validate-postgresql.sh \
  --datomic-home /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  --artifact /tmp/datomic-stage1-stage4-final-v1/build-a/datomic-rev-peer-1.0.7277-source.jar \
  --postgres-root /tmp/datomic-postgres-16-root \
  --pg-port 55436 \
  --transactor-port 54340 \
  --work-root /tmp/datomic-stage2-stage4-final-v1 \
  --confirm-disposable DATOMIC_STAGE2_DISPOSABLE
```

The script refuses non-loopback PostgreSQL, occupied ports, unsafe or nonempty
work roots, non-disposable catalog names, reused mutable paths, mismatched
runtime/tool hashes, wildcard classpaths, and any original peer/core2/transactor
implementation on a candidate classpath. An exit trap stops the exact verified
transactor process and exact new PostgreSQL data directory on every path.

Key retained evidence hashes are:

| File | SHA-256 |
|---|---|
| `config.properties` | `1500e1a6e4b42d33a9e5114bbe1c3d67d4c5d8279f0f9345b29c27dd9272aabe` |
| `candidate-classpath.tsv` | `b375395d6fd6d0bb896e55c5e4eeab294cb154f84477fa77a8286bafb74853b8` |
| `stage-2-summary.properties` | `5e50f0be223d508711e3058b820ae62275a34c716312132f4ebd16d86a71ff31` |
| `run-status.properties` | `4d4a1c7ca0fdcc5ce75513c355a015bea61547b8358f50fdbf3424041dd49c71` |
| `evidence.sha256` | `78ba14d01454f90d650174c7f92f9fa1bf80244686d95faa6029770ba831dca5` |

`evidence.sha256` accounts for 118 retained input, log, result, configuration,
classpath, status, and summary files. The final result summary separately
records a SHA-256 for every normalized lifecycle and fault result marker.

## Remaining boundary

Stage 2 does not prove every storage backend, proprietary filesystem adapter,
security configuration, distributed failure, or concurrency schedule. Those
claims remain outside the demonstrated result. The separate completed Stage 3
matrix records bounded concurrency and resource-race coverage.
