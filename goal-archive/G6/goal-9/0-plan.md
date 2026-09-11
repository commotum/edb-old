# Goal 9 — Bounded large-data reads and maintenance

## Objective and ownership

Selective offline backup reads, repeated log reads and maintenance use bounded resources with useful, measured operating controls.

This is Stage 8 of [Goal 0](../goal-0/0-plan.md), owning P05, AO09, ST-04, AO10, AO11.
Status: complete (2026-09-10); all three internal stages and the parent-stage
signal are verified. Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Goal 0's fresh-database
policy governs acceptance: schema/format changes may require a newly created
database. Cross-version database/format migrations, old-binary fixtures and
mixed-version rolling upgrades are not required. Reject unsupported formats
clearly; never silently reinterpret them or reset a database automatically.
Preserve current-version data integrity, exact identity/schema/values, immutable
history, restart/crash recovery, same-version failover, current-version backup/
restore and receipt-first exact retries, including after function rebinding in a
supported database. Retain cancellation, isolation and complete-path performance
requirements. This changes database-version support, not the existing Rust/EDN API
scope, and does not create a cleanup task to remove working compatibility code.
No JVM evaluation, replacement engine, alternate durable store or recursive goals.
Parent decisions govern optional scope.

Apply Goal0's proportionate-implementation policy: reuse unaffected evidence and
the existing application, use focused regressions and relevant integration, and
measure only useful complete paths. No duplicate build/transport/failure matrices,
invented absolute bounds or literal component parity. Required capabilities and
current-version safety remain; unapproved optional work is deferred, not a blocker.

Reuse immutable backup/log/index representations and caches. Add lazy backup access without restore/full materialization, authenticated log SSD reuse, and only effective configurable prefetch/index/backup/GC concurrency or pacing. PostgreSQL remains the durable product store; a backup reader is not a second writer backend.

Here SSD reuse means authenticated persistent local cache reuse; it is not a
hardware requirement. Bound admission, cache/queue residency and meaningful work
chunks, not all intermediate computation or a universal RSS/time ceiling. Choose
prefetch, concurrency or pacing at demonstrated bottlenecks; do not add a knob
for every phase or benchmark every setting combination. Selective offline reads
must actually avoid full backup materialization; documentation alone cannot waive
that requested capability.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Reproduce uncertain behavior; use existing evidence for obvious API/integration
gaps. Record only consequential decisions here.
**Completion signal:** Required behavior, native design, API and current-version integrity risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Selective offline backup reads, repeated log reads and maintenance use bounded resources with useful, measured operating controls.
**Focus:** Reuse immutable backup/log/index representations and caches. Add lazy backup access without restore/full materialization, authenticated log SSD reuse, and only effective configurable prefetch/index/backup/GC concurrency or pacing. PostgreSQL remains the durable product store; a backup reader is not a second writer backend.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified current-version operation and recovery.
**Focus:** Evolve application examples and EDN paths where relevant; use freshly
created PostgreSQL databases and ordinary service/peer boundaries. Verify exact
retries, immutable history, restart/crash recovery, same-version failover and
current-version backup/restore where relevant; measure complete costs. Historical
format and old-binary upgrade fixtures are not acceptance gates. Reopen
implementation for integration gaps.
**Completion signal:** Increasing datasets show selective backup reads and restart-hot log reads with measured I/O/memory attribution; corruption/unavailability fail safely. Concurrency/pacing checks demonstrate bounded admission, cancellation and interference under real workloads. No speedup is asserted merely because a setting exists.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Continuation: return to Goal0 and execute Goal10's cross-feature acceptance.
No unfinished required work remains here; reopen only for a concrete integrated
gap. No historical upgrades, prior-child matrices or optional integration work.

## Material implementation decisions and evidence

- P05: `BackupConnection` supplies the existing `DatabaseValue`/`LogValue`, sharing
  native authenticated tree traversal, schema derivation and bounded decoded-node
  cache. Capture publishes exact read indexes plus a sparse transaction lookup;
  opening/querying does not restore or replay the backup. If the source index
  lags, capture makes a streaming full-index pass, emitting completed nodes rather
  than retaining all encoded output. This capture cost must remain explicit.
- Backup envelope5 adds the authenticated log lookup. Older development backup
  migration is not required; unsupported versions fail explicitly. PostgreSQL
  canonical data is not rewritten or reset. Existing legacy handling can remain.
- AO09 actual PostgreSQL passed2/2, zero skips: twelve noHistory updates retained
  1,132,927 canonical payload bytes; cold/warm/reopened SQL result cells were
  1,136,343/3,416/3,416 bytes. Warm/reopened log scans transferred zero PostgreSQL
  payload bytes and hit12 cached transactions while rechecking authority.
  Cold/warm scans39.761/17.409ms; reopen+scan+checks72.577ms; whole fixture including
  seed, validation, drop and cleanup1.905s. Corrupt files refetched, forged warm
  membership rejected/fused, and excision/purge isolation passed. These are one
  local workload's measurements, not a general speedup guarantee.
- ST04/AO11: configurable bounded hint lanes and grouped CPU edit preparation;
  SQL/tree merge/publication remains deterministic and serialized. Defaults1;
  hints allow0(disabled)..8, index preparation1..8. No literal JVM queues/stores.
- AO10: cooperative cancellation and pacing at copied/committed batches, with
  actual counters. No sleep inside GC/restore write transactions. Backup retains
  its read-only consistent snapshot and generation pin while pacing.

## Final focused acceptance

All PostgreSQL checks below actually ran against isolated disposable schemas on
the configured local PostgreSQL15 server (fsync/synchronous_commit on), zero
skips. Release builds use `CARGO_INCREMENTAL=0 cargo test --release --offline -j4`;
run the named test target with `ATOMIC_POSTGRES_URL` set. Unchanged earlier passes
are retained; this is not a second whole-repository acceptance campaign.

- `backup_reads`:1/1 with64/8192 entities,9.96s total. Source schemas were deleted
  before offline query/Pull/history/as-of/seek/with/log use. Stock EDN query, Pull,
  with and named-log sources passed with file/stdin and no PostgreSQL configuration.
  Open+two queries+consume/drop2.138/11.191ms; open16objects, selected query2more;
  bytes39,063/571,060 and cache126,958/1,985,947B. Both examined4datoms for3results;
  warm repetition added no reads. Setup/capture/cleanup1.475/8.119s. Corrupt leaf
  fails/fuses, held authenticated values survive; cancellation retains copied
  objects but publishes no point, and retry completes.
- `maintenance_controls`:original3/3 plus final restore/GC regression1/1.
  Three actual PG hint lanes share a2-datom limit, and never join before commit.
  Same4096-input index edit preparation2.556ms atwidth1 versus3.807ms atwidth4:
  no speedup, default1 retained. Canonical roots agree. Five upload batches pace,
  cancellation retains reusable staging, and retry publishes correctly. Initial
  restore cancellation leaves durable rows but **no head**, so runtime open fails
  closed; retry restores exact information, then paced GC succeeds (whole2.597s).
- `native_log_cache`:2/2; cold/warm/reopen and corruption/authority/excision
  evidence above remains applicable.
- Current-format `backup_semantic_integrity`:4/4 (9.06s), including forged main
  and receipt-base trees, pending AVET and exact noHistory receipt retry.
  `backup_copy_boundary`:2/2 (3.59s), including content/request authentication and
  missing descendants. The new exact tree detects a forged endpoint at
  `backup/tree-binding` before replay, instead of the former state-commitment gate.
- Pure streaming-tree equivalence/error regression1/1; current manifest roundtrip
  and explicit unsupported-version/fresh-database error1/1. New backup version5
  does not reinterpret development version4. Clippy lib/bin succeeds with15+1
  existing warnings and no new warnings; `git diff --check` is clean.

Intermediate failures were fixture issues, not counted as passes: large debug
seed timeout (optimized setup then passed), old semantic fixture migrating a
shared development catalog (now isolated), and restore test permissions/head
assumptions (corrected to the actual private-repository/no-head contract). No
shared catalog reset or corruption repair was performed.

User guidance: `docs/backup-reads.md`, `docs/native-log-cache.md`,
`docs/maintenance-controls.md` and existing CLI/admin guides. Capture can require
a full streaming index pass; restore/deep verification remain broad operations.
These are explicit costs, not selective-read exceptions or performance claims.
