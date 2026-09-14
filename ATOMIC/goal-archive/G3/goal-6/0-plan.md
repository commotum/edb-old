# Goal 6 — Demonstrate the Integrated Native Database

## Objective

Establish Goal 0's original outcome as one usable Rust/PostgreSQL system:
independent peers, serialized declarative writes, exact immutable information,
local query/navigation and recoverable operations under a measured workload.
Complete and verify the existing implementation; this is the last child, not
a new parent or a reason to create another goal hierarchy.

## Constraints and starting context

- Goals1–6 and parent integrated acceptance are complete; no child is active.
  Goal5 repaired the
  publication/inspection mismatch on the original large target, ordinary
  receipt/GC liveness, backup/replay/proof reuse and repeated semantic sweeping
  on schema25. Preserve those repairs
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

**Status:** Complete scaled deployment measurement. Goal2 receipt-reuse repair
and focused actual PostgreSQL checks pass; Stage2 also passes on rebuilt binaries.
See exact results below; no unrelated import repeat is needed for this repair.

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

**Status:** Complete. Exact-manifest100k restore, native current/history, original
request retry, repaired deep inspection, post-fold native checks, both source GC
windows and actual dedicated PostgreSQL crash/WAL recovery all pass. Full old,
acknowledged, as-of, successor and independent-reopen fingerprints stay exact.
Source/backups are preserved; operational marker writes are explicitly recorded.

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

**Status:** Complete. The original information model and usable native system
are established with real PostgreSQL/application/failure evidence, measured
limits, current public run instructions and no known unfinished core failure.

**Outcome:** A coherent database, current public API/runbook and evidence-backed
operating envelope satisfy the parent objective without hidden core gaps.

**Focus:** Risk-appropriate final regressions, semantic/source decisions, clean
handoff instructions, measured limitations and a final comparison to Goal 0.

**Completion signal:** Integrated workflow and relevant live regressions pass;
current docs reproduce them and describe supported guarantees and intentional
differences. All parent stages are reconciled with no known unfinished core
failure. Only then mark Goal 0 complete.

## Continuation — 2026-09-09

Goal6 and Goal0 are complete. The100k deployed application, portable recovery/
inspection, source GC and actual dedicated server crash pass. All children are
reconciled; there is no active child or known core failure. Follow the operator
guide for future use; do not restart the completed loop or repeat expensive
acceptance simply because a session resumes. Reopen an owning child only for a
newly demonstrated core gap. The chronological evidence below remains preserved.
Do not repeat the unrelated import merely because an administrative path changed.
Small driver smokes and
the failed initial100k attempt below are not scaled acceptance.
Historical main catalog `postgres` on port55432 intentionally remains version23;
do not migrate it. Main test socket is `/tmp/atomic-goal-pg.MWicgR/socket`.
The separate restart/TLS server at `/tmp/atomic-goal5-tls.J4Rk1G/data`, port55434,
is available after coordinating users; its explicit config is
`/tmp/atomic-goal5-tls.J4Rk1G/goal5-postgresql.conf`. Preserve all existing work.

### Current operational run — 2026-09-09

Latest checkpoint: all six children and Goal0 integrated acceptance are complete.
The original pending target passes repaired deep inspection2595.920s/
1566504KiB peak, healthy with1pendingpublication/406nodes. One normal512-node
owner call then finishes the fold: exact1430live/intent nodes, zero headers,
unchanged1002/gen1/headhash. Independent post-fold native verification passes
1.120s/13600KiB, every current/history/query/frontier/basis field exact, eager0.
The enhanced16-full-fingerprint immediatePG crash driver also passes (exit0,
session83234). Dedicated55434 logs at21:50:09–10UTC prove immediate shutdown,
interruption, WAL redo and readiness. Storage restart518ms, replacement start
2616ms including lease expiry; native indexed base991→acknowledged1003 with
12tail transactions in one range. All16 full current/history scans match old1002,
acknowledged1003, recovered as-of values and final1004 independent reopen. Exact
request replay makes no new commit; successor, old log, Pull and marker history
remain correct. VmHWM26164KiB, eager materializations/hits/failures0. BinarySHA256
`4290ef643b30f4837aa7e99b7e2954747e1e575dfcedd49faa274d00fab2b435`.
Final target current401153/history401157 fingerprints:
`f9fdbcbe0d4aca18611fd5580f60d3642ea81329efb27c2a55f3c0bda4d6569a` /
`3b3bba41c6702572603705adb852a84f006adf47facca9d0bc7d70c75c40bf0f`.
Goal5's27 focused live
publication/inspection/backup checks and current small operations workflow pass.
Final source-unchanged full native capture passed1.093s/13412KiB BEFORE
GC's intentional marker1002→1003. Independent source GC now passes397.876s/
38644KiB peak, both windows quiescent (656/14 batches,340.050/48.607s), zero
blocked prefixes and all12 full old/current/history/reopen fingerprints exact.
It retires68 obsolete publications, creates68 receipt archives/48378 memberships,
and preserves all5216 owned tree payloads/448956990bytes. Total archive conversion
reads97394 nodes/11227667529bytes across distinct receipt closures. This ran
concurrently with target inspection; no isolated-latency or payload-reclamation
claim. Log:`/tmp/atomic-large-source-gc.2jGpOJ/run.log`.

Final read-only checks confirm generation1 on both installations, zero live
writer leases and fsync/synchronous_commit/full_page_writes all on. Source1003
hash`ff6524123a621ab8fe31afad13aa90d896f3496cd49a1164222569d0f4e40c17`;
target1004 hash`c65d8c96cec139803665c358bf2ca5574d86a730fb67b0c73aba148c96a0f9f3`.
Only the documented GC/crash markers advance these heads after the1002 backup.
Final fmt, all-target Clippy with warnings denied and diff checks pass. Latest
full configured library276passes precede the final narrow operator repairs;
their27 live PG cases, current small full workflow and large lifecycle runs
establish those changes. Unconfigured PG early returns are never live evidence.
Public README/operator commands match the actual drivers. Expensive eager admin
work, receipt-closure costs, same-host submission, cache-versus-RAM distinction
and the individually documented Goal4 omissions remain explicit limits, not
hidden core failures. The chronology below retains earlier incomplete results,
not additional active work or a claim that failed commands exited successfully.
Final phase output (including the original failed parent) is retained in
`/tmp/atomic-final-acceptance.40v3Bv/`; the full GC log remains at its path above.

#### Earlier checkpoints (superseded by the latest checkpoint above)

Goal5's renewal closes with17livebackup/proof cases,22liveGC/integrity/lifecycle
cases, full/resumed smalloperations and12-full-fingerprintGC smoke. The broad
Rust run's only failure was a sandbox-denied socket write; its exact permitted
rerun passes. Unconfigured PG early returns are not live evidence.

First100k backup129.687s/VmHWM86040KiB,8214newobjects,484539927bytes/8217files;
unchanged repeat137.792s/VmHWM94896KiB,8214reused/0new, exactsame root. Retained
repository`/tmp/atomic-operations-ozvYpB`, manifest
`cf050a943f0d17e0240194c080813ce534119afa73544ed0e0924216b3d645ff`.
Earlier standalone deepverification was deliberately stopped incomplete after
1608s/VmHWM1913552KiB to switch to tested currentrestore; it is not a pass or
completed latency. Initial520s failedcapture and invalidoldbinary repositories
also remain retained; see Goal5 for their exact paths.

The exact-manifest resumed release parent441700, tool session11723, exited1
at deep inspection with `integrity/tree-live-membership-mismatch` after more
than40minutes. Read-only proof finds1430 protected nodes =1024 live +406 pending,
with zero missing/extra nodes or missing payloads; no complete live-set row yet.
This is valid deferred publication work misclassified by inspection. Goal5 owns
pending-aware authentication and finite restore completion; no crash or GC ran.
Restore child441737 completed successfully after3367.648s, peak2200884KiB RSS.
Independent target-native verification passed1.114s/13424KiB, with exact
401150 current and401152 history datom fingerprints and zero eager fallback.
Original-request retry passed0.390s/17872KiB, returning basis1002 without a new
commit. Deep integrity and the final unchanged-source phase have not passed.
Final formatting and all-target clippy with warnings denied pass on the combined
tree. BinarySHA256
`c8359d6504c6f8ae998a6064fd903a6804529392e01fcd3f57f10895bb8e48b3`.
Source database`scale-workflow-361523-1788975974408163506` is unchanged at
generation1/basis1002/hash
`48554c0fade3c85f93f6fc73e606c64f918183b9d2b76c4b594600e2d87d53bf`, in
`atomic_goal6_scale.goal6_acceptance_20260909_b` on main55432. Fresh native
fingerprints match1.127s/13336KiB with0eager. Restore targets the same SQLschema
name in dedicatedserver55434's`atomic_goal6_restore`. Mandatory deep verification
occurs inside resumed restore; current run then requires native fingerprints,
persisted retry, deep inspection and source-unchanged checks. Its timing excludes
the earlier completed copy/repeat. Do not overwrite the running release binary.

After portable success, run guarded immediatePGrestart only on dedicated55434,
then rebuild/run the enhancedGC driver against the stopped source with4096batches/
1800s perwindow. GC source preflight:68pubs,1430live/5138stored nodes,443379586
stored payloadbytes,1002receipts covering all68bases,0archives,4,314,635semantic
nodes/1003roots. No activewriters. The final empty semantic sweep measured19.67s;
the Rust phase repair removes repetition during earlier work, not its inherent
broad scan. ~48.5k archive memberships is an estimate. Requiredreceiptpayloads
may remain while metadata retires; do not require deletion of reachable data.
Large restore/crash/GC are still pending. Reopen the owning child for a real gap.

## Historical integration checkpoint — 2026-09-09

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

## Historical execution checkpoints — 2026-09-09

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

Goal2 has now reclosed: the failed98100-record database consolidates normally to
revision622/basis982 in16.396s(debug), unchanged transaction head/hash, sealed
live closure4538nodes/166386560payload bytes. Pure boundary/retraction matrices
and17renewed checks (15actualPG) pass. The scheduler no longer perpetually
indexes subthreshold tails while still finishing finite physical maintenance.

Fresh repaired optimized run: database`scale-workflow-361523-1788975974408163506`
in catalog`atomic_goal6_scale`, isolated SQL schema`goal6_acceptance_20260909_b`,
parent361523/writer361545/peers361565,361567. Actual live library testing then
exposed a separate exact-receipt resource-reuse regression (correct values but
one new read cache/session per retained retry after ambiguous commit/reconnect).
Goal2 owns that repair; this measurement continues without release rebuild or
fixture mutation. Current repair binaries must exercise scaled receipt recovery
afterward; unrelated import progress need not be thrown away.
The isolation preserves all failed fixtures and keeps final GC counts scoped
to this installation alone. Do not GC or restart its source during import.

After successful import/fenced-writer-kill acceptance, use the fresh database
for independent backup/deepverify/restore on dedicatedserver55434, then guarded
immediate PostgreSQL crash recovery. Run source GC only after portable workflow
checks its unchanged fingerprint. GC is global to the source installation schema;
the fresh schema contains only this final workload, not earlier failed runs.
Preserve all data; no source reset or cleanup is required.

## Successful scaled deployment — 2026-09-09

The fresh optimized driver exits0/PASS atbasis1002, database
`scale-workflow-361523-1788975974408163506`, source schema
`atomic_goal6_scale.goal6_acceptance_20260909_b`. It imports100000records in
1000transactions from two independent submitting peers:400000business facts,
25600000UTF8 payload bytes,1001log rows/38140841canonical payload bytes before
the final controlled update. Import773.913s =129.213records/s. Per-peer100record
batch latency p50=1.585/1.567s, p95=2.147/2.150s, p99=2.304/2.280s. No import
unknown outcomes, duplicate receipts, full admissions, failed jobs or backpressure
stalls. This is a local workload observation, not a universal SLA.

Writer100ms-sampled RSS peak60301312bytes, recent accounted peak2593158bytes,
combined indexing/recent pressure peak2446572bytes, node-cache peak4158506bytes
under4MiB. Final reported VmHWM is58810368bytes; report the larger observed
periodic RSS as well, without treating either as an allocator or strict peak bound.
Long-lived peer VmHWM22437888/22396928bytes, cache peaks1047625/1048439bytes under
1MiB, thousands of actual evictions. All ordinary eager counters stay0.
Physical publication revision68 coversbasis991; scheduler134completed jobs counts
publication plus maintenance completion, not134different index epochs.
Read-only SQL confirms68physical publications, a complete1430-node live closure
with167521776payload bytes (greater than measured ordinary process memory), and
schema25. Final headhash`48554c0fade3c85f93f6fc73e606c64f918183b9d2b76c4b594600e2d87d53bf`,
generation1/basis1002; no active writer lease remains.

Selective query atbasis1001: cold8.322ms/warm7.332ms, both4SQL node reads and
435017payload bytes. Atbasis1002:7.932/6.720ms, same I/O. The deliberately small
1MiB cache churns this query's combined index working set; **do not claim warm
reads hit cache or improve I/O**. Work is selective, not a full database scan.

Persisted balance update, pure speculation, as-of/history, exact schema/old
values and peer-held early/late values pass. Actual writer361545 SIGKILL is
followed by fenced replacement in5.174s; same request replays once atbasis1002,
no duplicate head advancement. Recovery loads11tail transactions inone range
frombase991 with0eager database/history state; replacement RSS~16.4MB. All peers
and writers then stop and independent native reopen passes.

Environment: PostgreSQL15.11, fsync/synchronous_commit/full_page_writes allon,
shared_buffers128MB; Ryzen Threadripper2950X/16cores32threads, LinuxMemTotal
131773760KiB, localNVMe. Other focused regression/build activity shared the host;
this is not an isolated hardware comparison. Dataset exceeds declared cache/
recent budgets, not physical machine RAM. This driver used verified indexing
repairs but preceded the newly found weak receipt-core repair; current rebuilt
operations/restart checks must verify that independent recovery path on this
same dataset. Backups, server crash and GC are not yet accepted at this scale.

## Historical scaled operations checkpoint — 2026-09-09

Goal2's weak receipt-core repair and7targeted actual PostgreSQL safety checks
pass; current optimized operations/restart/GC examples rebuilt after final
lineage-filter ordering. Operations driver is running on the successful source,
restoring into `atomic_goal6_restore.goal6_acceptance_20260909_b` on dedicated
server55434. Private retained repository:`/tmp/atomic-operations-QRpT5i`.
First source-native phase passes1.083s, open112ms, VmHWM/RSS13144KiB,0eager;
complete current401150 and history401152 fingerprints are
`ee6bfab74da07b3013f9b2a7ba14735a78945b359906415a27e03daf9d7affed` and
`76a6fb1ac1a156188d5db5c419c110de0444a1b33288e28448c9c285daf93d30`.
It reads430cursor SQL nodes/77884081payload bytes with256KiB cache; these are
broad full-current/history scans, not the selective query's one examined datom.
Backup-first was deliberately interrupted after520s, CPU99.5%, RSS248864KiB.
Owned child382911 receivedSIGTERM and parent382869 exited1; partial repository
is retained, source unchanged. Goal5 owns unnecessary initial capture replay
and quadratic eager invariants/history replay. No portable completion is claimed.
