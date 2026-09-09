# Goal 6 — Demonstrate the Integrated Native Database

## Objective

Establish Goal 0's original outcome as one usable Rust/PostgreSQL system:
independent peers, serialized declarative writes, exact immutable information,
local query/navigation and recoverable operations under a measured workload.
Complete and verify the existing implementation; this is the last child, not
a new parent or a reason to create another goal hierarchy.

## Constraints and starting context

- Goals 1–5 are complete with focused live evidence. Goal5 was reopened and has
  now repaired ordinary receipt/GC liveness on schema25. Preserve those repairs
  and reopen the owning child for any actual core gap discovered here.
- Local Datomic documentation is semantic authority; recovered 1.0.7705 is
  architectural/algorithmic evidence. Native Rust, PostgreSQL and same-host
  Unix submission are deliberate choices, not JVM/wire equivalence.
- Capacity/caching docs distinguish bounded recent information and immutable
  object caches from application memory. Recovered `datomic/indexer.clj`
  accounts frozen indexing plus new recent data together. Measure these and
  actual process RSS; representation counters are not allocator measurements.
- Choose explicit resource sizes and a representative system-of-record workload
  exceeding configured caches. Report records/facts/history/bytes, batching and
  concurrency. Do not claim a database exceeds physical RAM merely because it
  exceeds a cache, or infer universal SLAs from one local machine.
- Backup/deep verification/excision may be broad and eager. Measure their costs
  separately from ordinary reads/writes; do not silently convert hot paths into
  full reconstruction or call broad scans constant-work.
- PostgreSQL checks must actually execute. Restart/kill only dedicated disposable
  fixtures. Keep request identity on ambiguous retry and independently verify
  acknowledged facts, retained snapshots and history after failure.
- Maintain one active child. Finish with integrated acceptance and current run
  instructions, not more scaffolds or passing test totals alone.

## Stages

### 1. Exercise and measure the deployed application

**Status:** Paused for owning Goal2. The new100k run failed athead982/98100records
with an indexer missing-node error; scheduling/preload repairs are in progress.

**Outcome:** A reproducible independent writer/peer workload demonstrates useful
transactions, query/Pull, identity/programs/history and bounded ordinary state
on data substantially larger than configured caches.

**Focus:** Public API deployment, explicit cache/recent policy, concurrent batch
and localized writes, selective cold/warm reads, background consolidation,
retained values and actual memory/latency/throughput/I/O observations.

**Completion signal:** The application passes at declared scaled checkpoints,
eager compatibility counters stay zero on ordinary paths, configured cache and
recent-state behavior is observed, and measurements identify the actual envelope
and any bottlenecks without substituting timing assertions for correctness.

### 2. Recover the same information through failure and maintenance

**Status:** Pending.

**Outcome:** The measured database survives abrupt writer/server interruption,
replacement and operational recovery without forks, missing commits or changed
old values.

**Focus:** Fenced replacement, exact retry/report recovery, lazy reopen and tail
recovery, dedicated PostgreSQL restart, backup/deep verification/restore and
retention/GC with meaningful costs. Reuse established fault controls and tests
where they establish a boundary; repair newly exposed defects directly.

**Completion signal:** Process/server failures actually occur, acknowledged and
ambiguous requests reconcile correctly, relevant old/current/history values
remain exact, and a scaled portable backup restores to a independently verified
native application state. Broad operation costs and tested failure limits are
recorded separately from ordinary workload metrics.

### 3. Establish integrated acceptance and return to Goal 0

**Status:** Pending.

**Outcome:** A coherent database, current public API/runbook and evidence-backed
operating envelope satisfy the parent objective without hidden core gaps.

**Focus:** Risk-appropriate final regressions, semantic/source decisions, clean
handoff instructions, measured limitations and a final comparison to Goal 0.

**Completion signal:** Integrated workflow and relevant live regressions pass;
current docs reproduce them and describe supported guarantees and intentional
differences. All parent stages are reconciled with no known unfinished core
failure. Only then mark Goal 0 complete.

## Continuation — 2026-09-09

Goal5's receipt-archive handoff and actual PostgreSQL checks now pass. Resume
the implemented scale/deployment workflow with current25 optimized binaries
and use the successful workload for portable operations,
dedicated crash recovery and reclamation measurements. Small driver smokes and
the failed initial100k attempt below are not scaled acceptance.
Historical main catalog `postgres` on port55432 intentionally remains version23;
do not migrate it. Main test socket is `/tmp/atomic-goal-pg.MWicgR/socket`.
The separate restart/TLS server at `/tmp/atomic-goal5-tls.J4Rk1G/data`, port55434,
is available after coordinating users; its explicit config is
`/tmp/atomic-goal5-tls.J4Rk1G/goal5-postgresql.conf`. Preserve all existing work.

## Integration checkpoint — 2026-09-09

- Fresh main-port catalogs: `atomic_goal6_scale`, `atomic_goal6_restore`,
  `atomic_goal6_regression`. Dedicated server55434 also has a separate fresh
  `atomic_goal6_restore`, reserved for scaled restore followed by crash restart.
- `Connection::connect_with_cache_limits` and its configured counterpart expose
  the existing peer entry/byte policy; cache/recent statistics are forwarded.
  Existing constructor defaults and writer ownership are unchanged. Actual new
  fixture passes1/1 (6.26s): 128×1KiB payloads,77evictions, one retained entry,
  peak297030 accounted bytes under512KiB, zero eager fallbacks; zero/one-byte
  policies bypass caching. Old cursors/values survive eviction/write/shutdown.
- Live service backlog1/1 (11.50s), failover1/1 (1.61s), stress1/1 (37.38s overall)
  pass. Stress measures240 transactions in8.195s,12 producers, queue4 plus one
  handoff;218396 rejected-full admissions show the fixture's aggressive retry
  loop, not desirable application behavior. This small test is not the scale run.
  Failover fixture now explicitly provisions its native root and uses native
  reads, retaining epoch2 takeover/old-value assertions; no fallback was added.
- Live bounded writer-state1/1 (17.78s), transaction/kernel differential2/2
  (18.64s), incremental consolidation3/3 (224.62s) pass. Localized successor
  reuses840 subtrees and reads86/writes35 nodes for3 changed datoms; this is a
  focused small-tree witness, not a universal constant-I/O guarantee.
- Final role audit found a stale expected SELECT allowlist for two existing
  GC-progress tables required by the invoker semantic-root validator since
  migration18. Only the fixture was synchronized; exact ACL/no-delete checks
  pass1/1 (1.61s), with no added product privileges.
- Scale, portable-operations and dedicated immediate-PG-restart examples are
  being implemented. No large-workload, crash-recovery or broad operation cost
  acceptance is claimed yet.
- First optimized100k attempt failed after128.73s atbasis82 (~8100records),
  before scale acceptance. Background pressure reached8.48MB and parked writes;
  default30s socket-handler timeout produced UnknownOutcome. The driver now
  retries the identical request within explicit15-minute reconciliation and
  two-hour import bounds. Actual eight-record5ms loss probe produced one unknown
  and one replay per peer with exact receipts/no duplicateappend; clippy passes.
- Same-input index profiling found8.165s current-EAV lookup and71.784s path
  preloading before merging: cached whole leaves were repeatedly authenticated/
  decoded per datom. Unioning touched paths before loading is being repaired
  without weakening parent/child/separator/noHistory checks.
- Ordinary GC smoke showed permanent active-receipt base ownership blocks the
  entire publication retirement prefix even after all reader pins release.
  This reopens Goal5 Stage3, now the only active child. Resume this workflow
  after its bounded archive-ownership handoff is implemented and verified.
- Commitment write batching is verified: max256 child-first nodes per SQL
  INSERT plus full row collision authentication; loaded-node SQL is prepared
  once per update. Stored hash/format/authority is unchanged. Actual PG suite8/8
  (4.54s), writer differentials2/2 (17.00s), bounded residency1/1 (9.46s) pass.
  A651-node single-transaction fixture now uses3 INSERT statements, previously651;
  debug wall times0.661s versus0.953s are local fixture observations, not a
  release throughput claim or an independent isolated performance comparison.
- Index path-decoding repair now passes the pure union witness, actual
  incremental/locality/noHistory/AVET3/3 (19.00s versus the earlier224.62s suite),
  stored-value2/2 (2.13s), background recovery/backpressure8/8 (5.52s), and a new
  cross-leaf BigDecimal exact-retraction fixture1/1 (4.66s). Clippy passes.
  Same-input component times: lookup8.165→0.126s, noHistory preload71.784→0.025s,
  EAV preload71.920→0.007s; actual merge0.072s unchanged. Fresh build/upload1.285s.
  The old diagnostic was interrupted after>159CPU-seconds before publication,
  so there is no completed whole-job baseline or exact total speedup claim.
- Normal consolidation of the failed run completed1.154s (staged nodes reused),
  publication revision5/index basis82 versus previous23; input23659datoms,
  node reads3227672bytes, encoded output12107970bytes, HWM71812KiB. Transaction
  basis82/hash and schema24 stayed unchanged. Failed data is retained at
  `scale-workflow-225999-1788939302299420169`, catalog`atomic_goal6_scale`.
- Operations smoke on the earlier20-record scaled-schema fixture passes:
  backup271ms, unchanged repeat99ms (0written/70reused), deepverify31ms,
  independent restore1056ms, coldnative36ms, persisted original retry211ms,
  deepinspect90ms, total1.906s. Current234/history236 fingerprints match;
  head remains6, no eager compatibility, repository89985bytes/73files at
  `/tmp/atomic-operations-SUPTne`. This remains a small functionality witness.
- Guarded `restart_workflow` now passes a real immediate stop/start of the
  dedicatedserver55434 on that20-record restored schema: logs show interruption,
  WAL redo and readiness, not a clean-shutdown-only simulation. Storage restart
  517ms; replacement2542ms including leaseexpiry; nativebase1→target7,6tailtx/
  onerange, HWM10688KiB. Oldvalues/log, acknowledged replay and nextcommit pass;
  targetbasis8/schema24, sourceunchangedbasis6. Server is available/released.
  Stable schema24 binaries are retained under `/tmp/atomic-goal6-stable.1P1qKz`
  for fixture reproducibility; do not use them after migrating a target to25.

## Current execution — 2026-09-09

Goal5 reclosed after the ordinary GC workflow reclaimed actual publications/
nodes with receipts intact; schema25 conversion, interrupted kind1-generation
lifecycle, backup/restore, integrity, upgrade and authority suites pass. Current
optimized examples built successfully. This is now the only active child.

The new100k run is executing in main `atomic_goal6_scale`, database
`scale-workflow-277036-1788941894497441026`, process parent277036/writer277055/
peers277073 and277075. Do not run GC or restart its server during import. First
quarter checkpoint: each peer12500records, basis250/251,123.8/124.6seconds,
peerRSS21.6/21.9MB and cache peaks below1MiB, eager compatibility0. Writer remains
below~54MB sampledRSS with no failed jobs or backpressure stalls atbasis260.
This is in-progress evidence, not a successful100k result. Frequent tiny index
jobs are being diagnosed read-only while the run continues.

That run subsequently failed: actualexit1, head982/hash
`212ac700ad123000e9cfd50f2a760c0e1de87074be6174c71308600a8de8bdac`, index
revision621/basis979. Reported missinghash33afd4eb7f55 exists in PostgreSQL;
Goal2 now owns native merge-preload investigation plus the diagnosed finite-
scheduling repair. The writer/peer processes stopped; preserve their database.
Goal2 is the only active child until this core indexing gap is verified closed.

After successful import/fenced-writer-kill acceptance, use this exact database
for independent backup/deepverify/restore on dedicatedserver55434, then guarded
immediate PostgreSQL crash recovery. Run source GC only after portable workflow
checks its unchanged fingerprint. GC is global to the source installation,
which also retains the earlier failed8100-record run and small smokes: disclose
those extra fixtures in administrative totals instead of calling them100k-only.
Preserve all data; no source reset or cleanup is required.
