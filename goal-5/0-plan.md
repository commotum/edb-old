# Goal 5 — Establish Operational Integrity and Recovery

## Objective

Complete Goal 0 Stage 5: protect, recover, upgrade, inspect, reclaim and excise
the native Rust/PostgreSQL database under an explicit, tested authority and
retention model. Preserve completed Goals 1–4 and the existing lifecycle tools.

## Constraints and starting evidence

- Goal 0 owns the objective. This is the only active child; no nested goals or
  corrective parent. Archived operations goals are evidence, not instructions.
- Local Datomic administration/backup/excision docs govern intended guarantees;
  recovered 1.0.7705 code guides design. No JVM/wire or other-store compatibility.
- Keep ordinary trusted-root adoption lazy. Deep verification may replay and
  compare the whole database; do not add that cost to ordinary reads or commits.
- Preserve migration checksums, acknowledged commits, unknown outcomes, retries,
  exact captured values and generation pins. Use isolated disposable PostgreSQL
  for destructive/corruption/restart tests; never restart a shared active fixture.
- Static review found backup tree validation checks hash/shape/claimed state
  coordinates but does not compare actual main or request-base archive datoms
  with authoritative log states. Existing live semantic-projection checks are a
  reusable model, including legitimate noHistory/pending-index differences.
- The dependency walker was repaired in Goal 3, but older per-generation marks
  can still claim complete=true. Existing authenticated backfill machinery must
  be versioned and invoked before old marks are trusted by GC.
- Connection TLS, role separation, fencing, backup/restore, excision and GC
  already exist. Inspect and test actual gaps, not rewrite working subsystems.
  SQL/network failure policy was explicitly deferred from Goal 2.
- The broad unconfigured Rust regression launched during Goal 4 completed with
  exit 0 (library 251/1 ignored); final nested additions passed separately live.
  No unconfigured PG case counts as integration evidence.

## Stages

### 1. Bind derived integrity evidence to authoritative information

**Status:** Complete. Deep semantic backup/restore and versioned reference repair
pass actual PostgreSQL corruption, upgrade and interrupted-owner fixtures.

**Outcome:** Deep backup/restore verification detects internally consistent
false trees; GC cannot trust obsolete or incomplete program-dependency marks.

**Focus:** Main and request-base archive semantic comparison, exact log points,
current/history/index projections, versioned authenticated reference repair,
and migration/runtime/GC fail-closed behavior. Reuse existing replay and walkers.

**Completion signal:** Forged validly hashed main/archive trees reject in deep
verification and restore; valid temporal/noHistory/projection backups still
restore. Real PostgreSQL upgrade fixtures repair formerly omitted dependencies,
preserve hashes/checksums and prevent reclaim while completeness is unproven.

### 2. Establish failure, transport and authority boundaries

**Status:** Complete. Typed SQL/TCP policy, ambiguity/retry, verified TLS,
least-privilege roles and actual server restart have direct live evidence.

**Outcome:** Deployment has an explicit SQL/network failure envelope and
least-privilege transport/role behavior that preserves transaction outcomes.

**Focus:** Connection policy, blocked reads/locks, interpreter versus SQL
deadlines, receipt ambiguity/retry, verified TLS, same-user socket authority,
writer fencing/failover and supported recovery procedures. Use existing controls
where sufficient; do not claim forced cancellation of arbitrary native code.

**Completion signal:** Actual blocked/failing PostgreSQL and role/transport
witnesses establish bounded configured failure behavior and honest committed/
unknown/rejected outcomes; secure deployment instructions match tested APIs.

### 3. Verify lifecycle operations as one recoverable workflow

**Status:** Complete after reopening. Ordinary receipt-preserving publication
conversion, interrupted lifecycle recovery and actual reclamation now pass.

**Outcome:** Backup, restore, upgrade, inspection, retention/GC and excision work
together without losing acknowledged history, receipts or live captured values.

**Focus:** Relevant existing failure/corruption/interruption/restart fixtures,
pin and generation lifetimes, request archives, deterministic retry, excision
completion, and external-copy retention limits. Record costs of inherently broad
administrative work without demanding constant work for whole-database tasks.

**Completion signal:** Real PostgreSQL lifecycle and failure checks pass on
isolated fixtures; important costs and irreducible external-retention limitations
are measured or explicitly unmeasured, with actionable operator procedures.

### 4. Return operational acceptance to the parent

**Status:** Complete. The repaired public GC workflow and current25 lifecycle,
upgrade, authority and integrity checks establish renewed operational closure.

**Outcome:** Goal 0 can proceed to realistic integrated load/failure acceptance.

**Focus:** Re-run the native application workflow through relevant operational
transitions, reconcile code/docs/tests and preserve any measured limitations.

**Completion signal:** No known core integrity/recovery gap is hidden, this
plan and Goal 0 record verified outcomes, and Goal 6 is scaffolded/resumed and
executed. This child is not the parent's finish line.

## Continuation

Return to Goal0 and resume existing Goal6 with current25 optimized binaries.
Measure the cache-exceeding workload and its backup/recovery/GC costs; this
child's small operational witnesses do not complete the parent. Preserve all
repairs and use fresh SQL catalogs, not the damaged historical version23 catalog.

## Integration checkpoint — 2026-09-09

The chronological notes below include superseded in-progress states. The
earlier closure evidence covers the schema24 repairs; the later reopening
section is authoritative for the still-active receipt/GC repair.

- Migration 24/program walker version 1 and backup semantic checks are being
  implemented. The applied migration 1–23 files are unchanged. Partial claimed
  GC/build owners need conservative authenticated retained-row references;
  readable generations still require complete replay. Tests are in progress.
- Explicit PostgresIoPolicy is implemented: per-statement/lock settings,
  minimum configured/policy/caller socket-connect cap and optional TCP liveness
  controls. Defaults preserve existing behavior. Tests remain in progress;
  these are not total DNS/TLS/startup or complete transaction deadlines.
- Actual PostgreSQL 15.11 checks on schema 24: integrity 3/3 in 10.24s,
  interrupted excision/old native value 1/1 in 10.05s, inspection scope/snapshot
  2/2 in 11.05s, migration authority/least privilege 2/2 in 3.02s. These suites
  use separate schemas inside `atomic_goal5_ops`.
- Separate TLS server verifies real encryption, refusal of plaintext and an
  untrusted private certificate, and configured migrator/writer/indexer/peer:
  TLS suite 2/2 in 2.45s. New typed I/O policy over TLS remains to be checked.
- Fresh main-port SQL catalogs: `atomic_goal5_backup`, `atomic_goal5_refs`,
  `atomic_goal5_io`, `atomic_goal5_ops`, at
  `host=/tmp/atomic-goal-pg.MWicgR/socket port=55432 user=jake`.
  Shared historical `postgres` catalog intentionally remains old version 23.
- Isolated TLS/restart fixture: `/tmp/atomic-goal5-tls.J4Rk1G/data`, port 55434,
  socket `/tmp/atomic-goal5-tls.J4Rk1G/socket`, server log `server.log`, startup
  option `-c config_file=/tmp/atomic-goal5-tls.J4Rk1G/goal5-postgresql.conf`.
  Private certificate `server.crt` is the test trust root. Do not restart while
  policy tests use it; main-port lifecycle tests are independent.
- All 15 operations_gc tests pass on actual PG in 60.40s (`atomic_goal5_ops`,
  search_path `goal5_gc`), including temporal code, request archives, old native
  snapshot pins, abandonment and phased generation reclamation.
- Backup semantic suite 4/4 passes live (33.13s): 16 forged main-index cases,
  eight forged earlier archive indexes, noHistory/receipt restore and pending
  AVET phases. Existing backup/restore suite passes 10/10 serially in 113.96s.
  The initial parallel run had one expected GC-fence Busy rejected by a fixture's
  unconditional unwrap; the fixture now retries only that exact contention code
  within five seconds, and the normal parallel rerun passed 10/10 in 47.44s. Tiny witness: basis
  3, 149 current/151 log-history datoms, 88 counted objects, 342ms deep proof.
  This does not establish large-database backup costs.
- Typed I/O policy suite passes 4/4 live in 6.86s. Statement150ms measured
  150.729ms; lock75ms measured75.453ms. Public head-lock failures leave the head
  unchanged and retry commits: statement250ms=251.953ms/Interrupted57014,
  lock300ms=303.420ms/Busy55P03. Caller20ms=20.143ms returns UnknownOutcome;
  identical-key reconciliation produces one committed report, not a duplicate.
  Verified TLS1.3 statement120ms=120.671ms, connection remains usable. No TCP
  blackhole/whole-operation deadline claim. Unit policy tests 4/4 pass.
- Live deep inspection now shares the backup pending-AVET provenance guard:
  legitimate stale clearing rows may remain, but invented data cannot hide
  behind a pending marker. Five history/projection helper tests pass.
- Actual stop/start of isolated server55434 passes postgres_restart_resilience
  1/1 in 3.14s: handles reborrow, standby recovery, logical/physical sync and
  retained native values survive restart. Main55432 work was unaffected.
- Public native_workflow passes on schema24 in isolated `goal5_workflow`:
  `workflow-200389-1788937846880874610`, t=3, all transaction/read/speculation/
  immutable-log/index/return-map/deferred-query checks retained.
- Review found that unfinished restore builders can pre-stage code marks before
  their corresponding log rows exist. Repair must preserve/authenticate those
  additional roots for non-collecting unpublished builders and serialize their
  capture with reference writes. A pause/upgrade/resume/GC fixture is being
  added; this is a real remaining Stage 1 safety gap until verified.
- Current parent check: separate writer/two-peer process_workflow on schema24;
  versioned-reference final repair tests/review remain in flight.
- Separate writer/two-peer `process_workflow` passes on schema24:
  `process-workflow-202743-1788938062899869234`, concurrent serialized commits,
  exact retries/reports, immutable values, and writer-offline reopen.
- Paused headless restore preservation now passes its actual PostgreSQL fixture.
  Review found another real integrity gap: readable-generation backfill replayed
  to the last surviving log member, potentially accepting a missing published
  tail. Repair must use the independently published active/retired endpoint and
  reject a truncated log atomically. This remains the active Stage 1 repair.
- Exceptional upgrade/reference repair is explicitly quiesced. Concurrent
  terminal abandonment can create a lock-order deadlock; PostgreSQL safely
  aborts a participant. No online-repair guarantee is made. The operator guide
  records the separate, potentially eager semantic-replay integrity cost.

## Verified closure — 2026-09-09

- Combined actual PostgreSQL `program_reference_upgrade`: 8/8 in 34.17s.
  Active readable endpoints require the exact published head basis/hash;
  retired readable endpoints require their successor's predecessor/basis and
  retained semantic-coordinate hash when available. Legitimate pre-v15 retired
  coordinates may lack that derived hash, never the retirement basis. Truncated
  active/retired terminal membership plus receipt now fails atomically, retains
  prior marks and blocks GC; restoring exact rows permits successful repair.
- The suite also verifies v23→24 checksums/rollback, missing/corrupt/future
  walkers, noHistory roots, partially collected generations, and paused headless
  restore→upgrade→resume→GC with pre-staged code preserved and authenticated.
  Healthy repeated migration does not rewrite completeness ledgers.
- Current library regression: 255 passed, one intentionally ignored process
  worker, 31.94s. Actual operations_integrity rerun after the shared pending-AVET
  guard: 3/3 in 4.67s. All-target clippy and formatting checks pass. Unconfigured
  library PG paths are not counted as live checks.
- `docs/operations.md` is the active runbook. The backup witness is tiny;
  realistic backup/recovery/memory/throughput costs are not yet established.
  Goal 6 is the next and only active child, not optional follow-up work.

## Reopened by integrated acceptance — 2026-09-09

An actual GC smoke found no index-node reclamation after releasing every reader
pin. Investigation proved a core liveness gap: active-generation request-base
bindings exclude the oldest physical publication from retirement; prefix-order
collection then blocks all later roots. A bootstrap receipt can keep this true
indefinitely regardless of retention age. Archive ownership is currently created
only by restore, so ordinary GC cannot transfer that responsibility.

Stage3 must add a bounded resumable archive-ownership handoff for required exact
receipt bases, retaining their request/hash/content and snapshot protection while
allowing ordinary publication retirement to progress. Incomplete staging must
remain protected and fail closed; readers, backup and deep inspection must never
see ambiguous or absent authoritative root ownership. Do not delete receipts,
skip prefix safety or impose a whole-database fold per GC call. Migration25 may
extend the protocol without changing applied migration1–24 bytes. This is the
only active child until the repair's live completion signal is established.

While that repair is in progress, the new guarded crash driver passes actual
immediate PostgreSQL shutdown/WAL recovery on the20-record restored fixture:
storage restart517ms, replacement2542ms including lease expiry, native recovery
base1→target7 with6tail transactions/one range, HWM10688KiB. Exact acknowledged
retry, held old values/log, successor commit and offline reopen pass; target
basis6→8, sourceunchanged6. Dedicatedserver55434 is available again. This is
small recovery evidence, not the remaining scaled acceptance.

## Receipt conversion verification checkpoint — 2026-09-09

- Migration25 implements bounded source-closure traversal, resumable existing
  v1 SHA-256 node-set digest, retired-ledger drain and atomic same-hash archive
  handoff. Source metadata/root lengths/routing floors are authenticated;
  readers fence before source discovery and ignore incomplete archive headers.
  Collector wrappers acquire the global fence before testing conversion owners,
  including when the source generation retired after staging. Runtime roles
  receive no new authority; SQL1–24 bytes remain unchanged.
- Live ordinary conversion fixture passes18.35s:19 batches,3717 authenticated
  node reads,1852 sorted hashes; tiny leaves force full512-node/hash batches.
  Every batch reopens the operator. Exact retry before/during/after, old snapshot
  pins, malformed frontier/checkpoint and corrupt root-length rejection pass.
- New live lifecycle suite2/2 passes11.69s. Backup during phase1 deep-verifies and
  independently restores exact before/after retry; finishing conversion leaves
  the portable manifest unchanged and repeated backup writes0new objects.
  A paused conversion in an excision-built generation survives another excision,
  preserves held old current/history values, then releases its archive and old
  generation after pins drop. Current information remains healthy.
- That lifecycle exposed another real liveness defect: archive phases suppressed
  semantic-node preview but still applied a fresh semantic frontier, causing
  repeated rollback when unrelated orphans existed. Apply now matches the
  preview's phase exclusion; exact comparison remains enforced, and the live
  retired-generation cleanup passes.
- Current25 upgrade8/8 passes29.98s; exact runtime-role audit1/1 passes2.04s.
  Historical23/24 checksum/rollback and versioned-reference repair coverage is
  preserved. Older archives/GC/backup regressions and public GC smoke are running;
  this child remains active until those establish renewed operational closure.

## Renewed operational closure — 2026-09-09

- Actual public GC workflow now passes6.296s/HWM17824KiB on the small installation
  that exposed the defect. Publications8→2, retirements6→0, stored nodes138→120
  (284074→253068 payload bytes), blocked prefixes2→0. Five completed archives
  retain120 membership rows; immutable receipt bindings remain. Held current/
  history samples stay exact and eager compatibility remains0. The released old
  manifest is unpublished while its receipt binding remains1. This establishes
  actual prefix/node reclamation, not deletion of still-required archive data.
- The released-pin window used7phases/985ms, maximum batch91ms,56node reads/
  106106bytes,24hashes and18retirement rows. Total conversion remains proportional
  to distinct retained physical closures; realistic maintenance cost is Goal6.
- Current25 live operations_gc15/15 passes101.22s and operations_integrity3/3
  passes10.48s in a fresh `atomic_goal5_ops` schema. Current25 backup semantic
  integrity4/4 passes43.05s; existing backup_restore10/10 passes162.64s. New conversion
  backup/restore/excision lifecycle2/2 already passes as recorded above.
- Current library261passed/1intentionallyignored in32.58s; all-target clippy and
  formatting pass. The initial sandbox-only library run was denied Unix-socket
  creation in one transport fixture; its permitted library rerun passes. PG-free
  test returns are not counted as live evidence. Broad all-target pure/integration
  harness execution continues under Goal6, which must reopen this child for any
  actual operational defect it reveals.

Goal5 is complete again; Goal6 is now the only active child. The original parent
objective remains open until its integrated scaled acceptance is established.
