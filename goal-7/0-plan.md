# Goal 7 — Integrated Operating Acceptance

## Objective and constraints

Finish Stage7 of `/home/jake/Developer/atomic/goal-0/0-plan.md` and establish the
complete native Rust/PostgreSQL product's measured operating envelope. Goal0
owns every capability and invariant; Goals1–6 are complete and remain evidence,
not replaceable implementations. This is the sole active child, with no nested
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

**Status:** Active.

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

**Status:** Pending; independent workload work can proceed alongside Stage1.

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

**Status:** Pending; extend existing seeded coverage while measurements run.

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

## Results so far

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

Started2026-09-09. Immediately profile commitment and maintenance paths, implement
the independent-reader campaign and extend existing lifecycle traces. The main
isolated PostgreSQL15.11 fixture is socket-only `/tmp/atomic-product-pg.jJPI2p`
port55439; global GC/restart needs a separate disposable target. Goals1–6 contain
the retained semantic, format, fault and measured-cost evidence.
