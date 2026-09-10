# Goal 7 — Integrated Operating Acceptance

**Status:** Complete (2026-09-09). Returned to Goal0; all seven parent stages
and integrated acceptance are established. No child remains active.

## Objective and constraints

Finish Stage7 of `/home/jake/Developer/atomic/goal-0/0-plan.md` and establish the
complete native Rust/PostgreSQL product's measured operating envelope. Goal0
owns every capability and invariant; Goals1–6 are complete and remain evidence,
not replaceable implementations. Run this child alone, with no nested
goals. Local datomic_pro_docs is semantic authority and1.0.7705 architectural
evidence. Preserve all data/format/request/program meaning and integrity checks.

Use existing native workloads, SQL/phase counters, application and replay/fault
support. Actual PostgreSQL and isolated failures are required, not skipped-test
totals. Driver calls, returned bytes, cache accounting, process CPU/RSS and wall
times are distinct. State build/settings/fixture sizes and noisy shared-host
limits. Reuse valid branch/join/hint/compression/index evidence from earlier
children; do not pretend G3 baselines are new results or promise linear scaling.

## Stages

### 1. Remove measured operating bottlenecks

**Status:** Complete. Bounded commitment reads, batched restore uploads and
shared authenticated inspection replay are implemented. Actual measurements
show fewer calls and faster multi-publication inspection; restore wall time
did not materially improve. Integrity checks and30maintenance regressions pass.

**Outcome:** Material commitment encoding and restore/inspection costs are
understood and improved without reducing authentication or recovery correctness.

**Focus:** Goal2's1108–1109SQL calls in four256-operation commitment phases and
G3's100k-record restore3367.648s/inspection2595.920s identify the starting points.
Measure current costs, remove repeated work with bounded locality/batching or
single-pass reuse, retain exact canonical hashes and every corruption check.
Do not introduce more transaction pipeline stages without supporting evidence.

**Completion signal:** Repeatable before/after phase and maintenance measurements
show the actual changed work. Independent semantic/corruption/rollback/retry
checks pass. Broad operations remain honestly data-sized, not constant-work.

### 2. Measure independent peers and mixed workloads

**Status:** Complete. The new independent-process campaign ran1/2/4readers,
cold opens and mixed scans/writes at2048and4096records on actual PostgreSQL.
Detailed commands, latency/SQL/CPU/RSS tables and cache cliffs are in
`docs/read-load.md`; results are workload-specific, not linear-scale claims.

**Outcome:** Operators can see the tested read/write/storage resource envelope.

**Focus:** Increasing reader counts, native warm selective queries, cold-start
bursts and scan-heavy analytics alongside ordinary traffic. Reuse current
fixtures/counters; distinguish independent peer computation from shared storage,
foreground from background, cache allowances from total memory and cold peer
caches from cold PostgreSQL/OS caches.

**Completion signal:** Publish repeatable throughput, latency tails, cache/memory,
process CPU and total observed PostgreSQL costs for multiple reader counts and
mixed traffic. Measured results, not a prescribed scaling ratio, determine the
claim. Repairs needed for correctness or material bottlenecks are implemented.

### 3. Prove integrated lifecycle and close the product

**Status:** Complete. Lifecycle/upgrade/crash witnesses, final rebuilt local/
remote/operator application, saved fault replay and generated differential pass.

**Outcome:** Supported executable/SDK workflows, failure/recovery and upgrades
compose, with concise reproducible instructions and no hidden feature gaps.

**Focus:** Extend/replay existing seeded transaction/retry/reconnect/index fault
schedules and controlled failure reduction. Exercise relevant real PostgreSQL
restart, old-format upgrades and changed-boundary regressions; preserve recent
two-network application, consumer and operator evidence. Fold actual findings
into this plan, Goal0 and user-facing operating instructions.

**Completion signal:** All assigned product capabilities and integrated checks
are established. Exact values/history/identity/receipts survive the changed
boundaries; seeded traces replay/reduce. Report untested environments and costs
plainly. An actual gap reopens its owning child; only then close this child and
Goal0, not merely the scaffold or one benchmark.

## Verified results

- Final saved V2trace replay after both repairs passes1/1(7.96s), retaining exact
  current/history and old values across8writes,3interrupted uploads,1peer reopen,
  3writer restarts,3exact retries and4consumer resumes/8events. Replay output:
  `/tmp/atomic-storage-fault-HAdXqw.trace`. Seed42/48-step production-versus-pure
  differential passes36accepted/12rejected cases with one writer restart;
  generated trace plus three trace-safety tests pass4/4(7.63s), artifact
  `/tmp/atomic-transactor-differential-hyvsSi.trace`. Final supported binary/app
  build, all-target compile, read-load example unit1/1, cargo fmt check and
  git diff whitespace check all pass. No core gap emerged requiring reopening.
- Maintenance shares one authenticated log replay across every retained native
  publication and reuses immutable decoded blocks; it does not skip older or
  same-basis candidates, state commitments, schema/noHistory/pending-AVET checks.
  Restore uses existing bounded128-node/8MiB verified uploads without changing
  canonical content. All30focused actual PostgreSQL regressions pass, including
  a forged earlier publication hidden behind a valid later same-basis root.
  On the identical8192-record/65-transaction source with9publications, inspection
  improved10.591→6.784s, processCPU9.517→5.269s, SQL901→657, result-cell
  bytes92.57→58.32MB and peakRSS114208→104700KiB. Combined Goal7 restore changes
  reduced SQL152963→39198 but wall19.315→19.247s did not improve materially;
  result-cell bytes137.0→160.2MB and peakRSS120988→121452KiB increased. Restored
  target inspection(onepublication)5.849→6.060s also did not improve. Current/
  history/query fingerprints agree. This is not a rerun or extrapolation of G3's
  100k-record campaign; broad restore still has substantial replay/validation
  costs. Reproduction and retained before/after artifacts are in docs/operations.md.
- Final rebuilt application/operations/security regression batch passed on the
  separate `atomic_goal7_final_integrated` PostgreSQL database: admin2/2(14.84s),
  consumers6/6(13.26s), runtime roles1/1(3.21s), local product2/2(8.97s), isolated
  network product1/1(15.43s), remote transport3/3(13.35s). The app reachesbasis11
  with safe planning, partitions, persisted search/programs, exact restart retry
  and18SSD hits on replay; warmed calculations make0foregroundSQL. Live consumer
  whole-process idle500ms makes0SQL; observation77.117msconsumer/77.121mspeer.
  The administrative fixture interrupts restore at an observed node-write lock
  with SIGTERM, then retries identical intent and checks inspection/search/GC.
  All-target compile passes. Prior phase rustfmt drift was corrected mechanically;
  workspace formatting now passes without changing data or durable meaning.
- Independent optimized reader processes execute real four-key Datalog queries,
  capturing a database value per query. At2048records, a1MiBcache holds the
  measured854498-byte hot set:1/2/4readers achieve79569/104453/155234queries/s,
  with zero foregroundSQL. Mixed ordinary readers retain zero foregroundSQL
  while another process scans beyond its cache and a writer commits paced
  transactions. Whole-process background SQL, CPU, RSS and result bytes are
  reported, not hidden behind query-only counters. Doubling to4096records with
  the same cache causes thrashing/eightSQL per query and122/237/497queries/s.
  Earlier256KiB oversized-node bypass and768KiB hot-set thrashing runs are
  retained. No cache-size-independent performance or universal capacity claim.
- Commitment encoding now fetches authenticated update-local subtrees with
  adaptive depth1–4 (maximum31rows); coordinate-only checks remain point reads.
  The115-byte transfer cap rejects oversized canonical nodes; hashes, normalized
  columns, counts and BST/heap checks remain mandatory. No format/schema change.
  Final actual PostgreSQL commitment tests10/10 and phase test1/1 pass, alongside
  9independent pure tests. Four256-operation transactions reduced encoding
  SQL1109→243 and totalSQL1221→355. Sequential/queued release wall times were
  258.27→238.80ms /225.57→194.39ms; processCPU164.90→141.21ms /
  145.02→122.51ms. Returned result-cell bytes increased402510→447454 /
  370862→415806. Small1/64-operation samples did not consistently improve
  latency, despite81→33 /333→72encoding SQL. Shared-host timings are not an SLA.
  A rollback-controlled661-member oracle comparison preserved exact roots,
  visits and writes:256replacements526→106SQL,81986→83954canonical read bytes,
  peak map key/value bytes1913040→1916496 (excludes allocator/SQL buffers/RSS).
  This removes measured calls, not evidence for deeper transaction pipelining.
- Goal6 rebuilt application/network/operator/consumer acceptance is retained.
  Additional actual PostgreSQL TLS timeout check configured120ms/observed121.03ms
  preserved connection usability; the disposable TLS cluster was stopped.
- All library tests run as the real user:341passed/1ignored(187.22s). PostgreSQL
  was deliberately unset in that broad run, so early-return PG tests are not
  counted as PostgreSQL evidence; targeted configured suites supply it.
- Existing storage fault trace now writes V2 with graceful writer replacement,
  exact old receipt retry and consumer acknowledged/unacknowledged restart;
  V1 explicit actions still decode with unchanged meaning. Actual suite4/4
  passed35.21s. Default12actions:2writes,1interrupted upload,2peer reopens,
  2writer restarts,2exact retries,3consumer resumes/3events (schema included).
  Saved `/tmp/atomic-storage-fault-KcrEu4.trace` replayed on a fresh fixture in
  4.36s. Controlled failure reduced9→3 in9replays and passes without the injected
  assertion. The reducer also found a new test-oracle omission of ordinary
  schema transaction t=1; oracle corrected, no engine defect claimed.
  Seed42/24actions also passed7.63s:8writes,3interrupted uploads,1peer reopen,
  3writer restarts,3exact retries,4consumer resumes/8events. Saved trace:
  `/tmp/atomic-storage-fault-Wj0KUK.trace`.
- Separate socket-only PostgreSQL live-handle/standby restart1/1 passed3.99s.
  Current application populated a separate crash fixture through basis11;
  immediate-stop/WAL recovery passed: durable settings on, exact acknowledged
  receipt retry, complete current/history fingerprints at11/12/13, retained old
  log/value and independent native reopen. PostgreSQL restart419ms, replacement
  start2759ms, exact tail1transaction/1range read; native eager materializations0.
  Fixture `/tmp/atomic-goal7-restart.gxkNvp`, not the shared application server.
- Fresh pre-partition and pre-fulltext executables created exact old genesis
  databases in new isolated schemas. Explicit migration26→29 plus administrative
  genesis-index publication preceded one-time tests. Partition1/1(1.44s) and
  fulltext1/1(1.24s) preserve4588/4818-byte old genesis, historical values, explicit
  vocabulary transactions, restart/indexed recovery and exact receipts. Initial
  fixture opening correctly failed without native publication; no implicit
  recovery was added. Earlier preserved binaries and hashes are in Goals4–5.

## Continuation

Complete2026-09-09; returned to Goal0 and reconciled every parent stage. No active
child or known failing required check remains. Product usage/verification starts
in docs/application.md and docs/acceptance.md; read-load and operations guides
record the measured envelope and unmeasured environments/costs. Keep a single
owning child if a future integrated gap is found, not a corrective parent.
The main socket-only PostgreSQL15.11 fixture is `/tmp/atomic-product-pg.jJPI2p`
port55439; final differential data uses `atomic_goal7_final_integrated`.
Crash/TLS fixtures were stopped; global GC/restart always needs a separate
disposable target. `/tmp` traces/binaries are retained local evidence, not
permanent repository assets. No additional implementation or test gate is queued.
