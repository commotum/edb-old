# Goal 5 — Establish Operational Integrity and Recovery

## Objective

Complete Goal 0 Stage 5: protect, recover, upgrade, inspect, reclaim and excise
the native Rust/PostgreSQL database under an explicit, tested authority and
retention model. Preserve completed Goals 1–4 and the existing lifecycle tools.

## Constraints and starting evidence

- Goal 0 owns the objective. This child is reopened and is the only active
  child; Goal6 is paused on an observed integrity-reporting/lifecycle gap.
  No nested goals or corrective parent. Archived operations goals
  are evidence, not instructions.
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

**Status:** Reopened. The large restore, full native fingerprints and exact retry
pass, but inspection misclassifies valid deferred publication folding as corrupt.
Repair pending-membership authentication/reporting and finish restore-owned
bounded publication work, preserving the earlier capture/replay/GC repairs.

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

**Status:** Pending the reopened publication/inspection repair. Earlier schema25
lifecycle, upgrade, authority and focused integrity evidence remains valid.

**Outcome:** Goal 0 can proceed to realistic integrated load/failure acceptance.

**Focus:** Re-run the native application workflow through relevant operational
transitions, reconcile code/docs/tests and preserve any measured limitations.

**Completion signal:** No known core integrity/recovery gap is hidden, this
plan and Goal 0 record verified outcomes, and Goal 6 is scaffolded/resumed and
executed. This child is not the parent's finish line.

## Continuation

Goal5 is the only active child. The large operations parent441700/session11723
exited1 at deep inspection: `integrity/tree-live-membership-mismatch`, after
more than40minutes of inspection. Restore itself passed3367.648s/2200884KiB
peak RSS; full native current/history fingerprints and original-request retry
at1002 passed. The source and retained backup are unchanged; no large crash or
GC ran. Do not repeat the successful import or semantic restore to repair a
derived bookkeeping/reporting defect.

Exact target on dedicated55434: `atomic_goal6_restore.goal6_acceptance_20260909_b`,
database `scale-workflow-361523-1788975974408163506`, head1002/generation1.
Publication revision1/basis991 has manifest
`aea88e93d6a6c3f8e60351c4754ce78fa727e80f0dbfc189f5ef779d1af87864`.
Its sealed1430-node intent equals the disjoint union of1024 live nodes and406
pending additions, with zero missing/extra nodes or absent payloads. The complete
live-set row is not present yet. Restore folds one512-node batch; the short retry
writer apparently folded another before shutdown. This is legitimate protected
maintenance state, not lost facts. Inspection must authenticate the pending
transition rather than require instantaneous complete folding. Restore should
also finish its finite owned publication work, including existing-root retry.

Scope decision: prove authenticated safe/resumable node sets, not forensic
equivalence to a particular sorted SQL batch history. A review model accepted
31419 reachable states across16596 small set/batch cases; arbitrary safe consumed
subsets can also resume correctly. They do not require a new ordering constraint
to preserve facts or GC safety. Pending metrics distinguish remaining node work
from final header sealing. Missing live/retired/pending witnesses and inconsistent
commitments still reject. This follows the documented separation of atomic
information from background indexing/reclamation; it does not make ordinary
publication or the background worker synchronously unbounded.

Next: verify the repair with large-node-count small-log PostgreSQL fixtures and
corrupt pending witnesses, resume the retained target through the normal bounded
maintenance API, rerun the repaired inspection and unchanged-source proof, then
return to Goal6 for actual crash and GC. The crash driver now has16 full streamed
current/history scans (build/clippy/fmt pass, live execution still pending).
Preserve schema25, applied migration checksums, source/backup and all earlier
repairs; never migrate the damaged historical version23 catalog.

## Renewed closure — 2026-09-09

Copy/deep semantics, exact stored-value indexed helpers, schema grouping,
endpoint audits and private restore proof reuse now have actualPG/pure evidence
below. Modern restore retains one deep source proof and one persistent-coordinate
pass, not repeated target reconstruction. GC defers its broad semantic sweep to
the final phase without weakening reachability/authority. Latest22GC/integrity/
lifecycle/phase and17backup cases pass; broader configured library276passes
precede the final narrowly tested backup/GC changes.

The strengthened GC driver passes a fresh20record actualPG workflow3.813s/
VmHWM18352KiB:12full current/history streamed fingerprint scans match, both
windows quiescent(8/7batches), blockedprefixes0, exactindependentreopen,0eager/
0cache. Old234/236datoms become236/238 afteritsmarker;2obsolete publication/
manifest and3intent records removed,2receiptarchivescreated, no treepayload
deleted because all remainedreachable. Fixture
`atomic_goal6_regression.goal6_gc_full_fingerprint_20260909_r1`, database
`scale-workflow-457292-1788981810054362885`, finalbasis7, is retained. Approval
initially declined, then succeeded after read-only single-fixture/fixed-schema
proof; there is no remaining permission blocker. This is small lifecycle
acceptance, not the still-running large Goal6 result.

## Scaled backup/replay reopening — 2026-09-09

Current100k source:`atomic_goal6_scale.goal6_acceptance_20260909_b`, database
`scale-workflow-361523-1788975974408163506`, head1002/hash
`48554c0fade3c85f93f6fc73e606c64f918183b9d2b76c4b594600e2d87d53bf`.
Native full current/history fingerprints pass1.083s/13144KiB,0eager. First
backup was deliberately interrupted after520seconds, CPU99.5%, RSS248864KiB,
before completing its first phase. This is an incomplete baseline/lower bound,
not a completed time or timeout SLA. Owned child382911 receivedSIGTERM; parent
382869 exited1 and the partial private repository`/tmp/atomic-operations-QRpT5i`
is retained. No source rows were changed or backup files deleted.

Concrete causes: first capture sets `reconstructed=Some(genesis)` while ordinary
incremental capture does not replay. `Database::apply_committed` reconstructs
indexes/history and invokes full invariants for every transaction. Cardinality,
uniqueness, logical application and history replay use repeated linear/pairwise
scans; invariant validation also rebuilds commitments/indexes. Merely removing
capture replay would leave deep verification and restore with the same problem.

Repair direction: ordinary backup copies authenticated immutable content,
preserving canonical bytes, chain/membership/frontier/endpoint, exact receipt
and program closures, pins and children-before-root publication. Explicit deep
verification/restore retain semantic log/tree comparisons. This intentionally
moves semantic-log rejection from first copy to verification/restore, matching
the local backup docs and recovered`peer/src-clj/datomic/backup.clj` copy design;
test and document this boundary, not claim an unchanged guarantee. Replace
quadratic eager validation/replay with indexed/sorted equivalents preserving
stored-vs-index equality, alteration-hook timestamps and all invariants.

Initial capture copy-boundary regression passes on actual PostgreSQL3.50s:
hash-valid false semantic state copies/presence-checks but deep verification
and restore reject before target activation; damaged content/request digests
still reject during copy. All11backup units pass, including legacy alias/
tempid normalization and locally valid but semantically false frontier rejection.
Review also found unchanged-root retry automatically deep-replays the backup;
replace that overstrict repeated proof with full physical/content/structural
authentication and exact captured logical-point equality, retaining explicit
semantic verification and restore checks. This follow-up is implemented and
passes2actualPG copy-boundary tests6.46s plus11backup units0.42s. Retry reads and
authenticates the full reachable tree structure and exact logical coordinates;
hash-valid false semantics can be copied/reused but explicit deep/restore reject.

Indexed eager helpers preserve stored/index equality (including nontransitive
mixed decimal/numeric comparison), first-match behavior and alteration hooks.
All new committed datoms retain type/entity/ref/frontier/transaction checks;
schema transitions validate retained values. The unchanged independent full
invariant audit now runs at whole-recovery, verification/restore, compatibility
tail, legacy backfill and preactivation excision endpoints, not every prefix.
19database tests pass;2kfacts/20transactions debug replay with every-prefix
audits14.538s versus endpoint audit3.206s preserves every state hash and exact
final information. This is a focused comparison, not a100k operational result;
per-prefix index construction and current-state scans remain. Current actualPG
recovery/backup/excision regressions and the release100k workflow are next.

Current release capture now completes: first100k backup129.687s,8214newobjects,
484539927repository bytes/8217files, VmHWM86040KiB. Retained private repository
`/tmp/atomic-operations-ozvYpB`, manifest
`cf050a943f0d17e0240194c080813ce534119afa73544ed0e0924216b3d645ff`.
Fresh native fingerprint1.103s/13380KiB matches both prior digests with0eager.
Unchanged reuse/deepverify/restore are still running; this is not whole-operation
acceptance. Release build26.75s and all-targetclippy7.24s pass. Renewed actualPG
backup integrity4/4(21.01s), backup/restore10/10(89.19s), excision1/1(5.89s),
integrity3/3(8.83s), programreferenceupgrade8/8(26.95s), v6/currentcommitment
backfills1each(1.68/1.42s) pass; pure COW3/excision7 also pass.

Read-only attribution found another avoidable eager replay cost: schema
derivation scans the full current set for each property of every installed
attribute. A scoped per-schema-entity grouping and borrowed ident-history
iterator repair is implemented;2schema differential tests pass256mutation cases
and error/input-order witnesses. Debug28attribute schema derivation,5repeat
medians:1kfacts3.823→0.461ms,4k13.299→1.114ms. The running release measurement
predates that optimization; binarySHA256
`ab8c09d96c643a17bbb76d52f8938f0a84a09e6bd1bd0ca8ac31d1fb0cc7d25e`.
Do not silently attribute its results to a later binary. Final current-code
PostgreSQL-configured serial library passes276/0fail/1expected internal worker
ignore in208.76s in`atomic_goal5_refs.goal5_final_lib_1788979074568`. Its passing
process-death parent explicitly launches that worker; PostgreSQL checks did run.
The current-source ordinary native application example is next.

Unchanged100k backup reuse passes137.792s/VmHWM94896KiB,8214reused/0newobjects,
exact same manifest and repository bytes/files. This eliminates writes but the
full structural/content authentication does not demonstrate a latency saving;
do not claim Datomic-style incremental timing from this result. Deep verification
is now the active phase. No large restore/server-crash/GC result exists yet.

The renewed native application workflow also passes on current debug source:
`workflow-423053-1788979481097827270`, schema25/head3, including controlled/tuple
transactions, query/Pull, history/log/index-pull, deferred projections and chained
speculation, immutable old values and reopen.11.61s includes9.74s compilation;
do not use wrapper memory as application RSS.

Restore review identified up to five complete replays: deep backup proof,
persistent-coordinate staging, target-row comparison, main-tree base recovery,
and final returned-head recovery. The last three appear redundant with a private
verified proof and exact target-row/coordinate checks. A bounded repair is in
progress, retaining full deep semantics and the one coordinate-building replay.
Do not replace `content != expected_content` alone: its derived PartialEq uses
logical Value equality and can hide stored BigDecimal scale. Replacement proof
must compare canonical encoded bytes, retain request/archive/completion checks,
carry the verified main-base schema (which can precede head), bind local generation
coordinates, and recheck final head/lineage/state/frontier before returning.
New live stored-byte, schema/base, rebind/retry and final-head-race tests are required.

That restore proof reuse is implemented and compiles. It captures only the main
index's verified schema/coordinate, leaves public BackupVerification unchanged,
compares canonical target content bytes and rechecks a locked final target head.
Legacy main bases preceding head need no invented persistent coordinate; an
existing coordinate must agree. Three redundant target replays are removed;
the explicit deep source proof and coordinate-building semantic pass remain.
Current existing/new actualPG regressions are running. The final276-test library
and ordinary workflow above precede this latest backup-only repair.

The older deep-check baseline was deliberately stopped after1608s (SIGTERM only
child417278); parent410436 exited1 and both are gone. It did not complete and
is not acceptance. Last CPU99.9%,RSS1800528KiB,VmHWM1913552KiB; at1390s kernel
rchar4.535GB/read_bytes0 (cached reads). Source t1002/gen1/hash is unchanged;
the complete backup repository is retained. Move to the tested current restore,
which retains mandatory deep verification, rather than keep testing the older
implementation. This is an incomplete cost baseline, not a completed latency.

Restore proof reuse passes17actualPG tests (10existing82.04s,4semantic19.92s,
3new17.29s),11backup units0.42s andclippy. A real same-basis-generation failure
was repaired by checking canonical log identity before archive closure; mismatched
legitimate generations remain replaceable, without skipping corruption checks.
The operational driver now supports exact-manifest resume after completed copy/
repeat. It binds source lineage/generation/basis before target writes; mandatory
restore deep proof, native equality, retry, inspection and source-unchanged checks
remain. Small current-code full driver passes4.057s and resumed driver1.582s at
schema25/head3; bad manifest rejects before restore. Its retained repository is
`/tmp/atomic-operations-wTbtlN`. Resumed elapsed excludes earlier completed
copy/repeat; a previous standalone deep check need not have completed.

Read-only100k GC preflight found another avoidable cost: the no-garbage semantic
node candidate query scans4,314,635stored nodes/1003roots and takes19.67s to
return0rows (first5sprobe canceled). Current code repeats it during every earlier
tree/conversion phase and reselects even an empty frontier. A Rust-only phase
split is being implemented; do not add a migration or remove root/reachability
authentication. Skip semantic sweeping during earlier work and skip its delete
function when the authenticated preview is empty. All current source hashes
remain unchanged by this read-only preflight; actualGC is still pending.

Current run: releasebuild25.11s succeeds, all-targetclippy0.41s passes. Binary
SHA256`c8359d6504c6f8ae998a6064fd903a6804529392e01fcd3f57f10895bb8e48b3`.
Resumed operations parent441700/session11723, restore child441737. Exact source
fingerprints pass again1.127s/13336KiB/0eager atbasis1002, and the retained manifest
passes the resume identity guard. Restore (including its own mandatory deep
verification) is running on dedicatedserver55434's isolated target schema.
No successful large restore, server crash or sourceGC result exists yet. Keep
the running release executable intact; GC phase edits are separate source work
and its example must be rebuilt after this parent finishes.

GC phase repair is verified on22actualPG cases: GC15/15(61.61s), integrity3/3
(9.23s), receipt archive lifecycle2/2(17.87s), new phase tests2/2(8.12s).
The deterministic relation-lock witness proves earlier intent work proceeds,
then the final semantic sweep still occurs; exact orphan deletion follows lock
release. Conversion phases0–4, program work and receipt bindings remain valid.
No migration/ACL change. Whole-catalog semantic search remains broad (~19.67s);
only its needless repetition was removed. All1002scale receipts retain all68
physical bases, so required payloads may remain even after publication metadata
is reclaimed. Driver budgets4096batches/1800s perwindow are estimates until run.

Broad current Rust pure/build regression finished: all integration targets and
7example harnesses pass. Library275pass/1expectedworkerignore/1sandbox EPERM
on a socket write made the original all-target command exit101; exact fixture
passes0.03s with local-socket permission, no code change. PostgreSQL variables
were unset; those early-return cases are not live evidence. Actual pure results
include validation3/3(53.42s), identity5/5(308.36s), kernel14/14(23.41s), semantic22,
programruntime22, DatabaseValue8, Pull13 and query suites. Live configured and
focused evidence is recorded separately above.

One attempted fresh measurement was invalid: a release build collided with
in-progress eager-helper declarations and failed; the command then inadvertently
started the older binary. It was stopped promptly (owned PID392372), and no
performance or correctness result is counted. Its partial private repository
`/tmp/atomic-copy-measurement.bqP2eC` is retained. Build success must gate the
next run; source head/hash remain unchanged. Both incomplete repositories remain
available for diagnosis; no timing from the invalid run is used.

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
