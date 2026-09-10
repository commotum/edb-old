# Goal 2 — Stack-safe graph traversal and recent-log ownership

## Objective and constraints

Complete Stage2 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: repair R6
(recursive stored-component retraction in both assessors) and R12 (recursive
last-owner destruction of the recent-log predecessor chain). Preserve all Goal1
repairs, immutable values, exact transaction meaning, retained snapshots and
durable request receipts. Status: **complete (2026-09-10)**. Goal0 now owns Goal3.

Use `/home/jake/Developer/atomic/datomic_pro_docs` as semantic authority and
`/home/jake/Developer/atomic/1.0.7705` as architectural evidence. The recovered
`datomic/builtins/component-es-set` uses a worklist; Clojure's GC does not provide
Rust Arc-chain drop guarantees. Preserve native Rust/PostgreSQL and existing
resource policies. No arbitrary stored-graph depth cap, database rewrite, JVM
parity or new product feature. Do not change canonical transaction/index ordering.

Starting points: `expand_retract_entity` in `database.rs` and `tiered_assessor.rs`
recursively follow component refs. Their visited sets stop cycles, not deep paths.
`recent.rs` stores predecessor `Arc<LogChunk>` links; ordinary last-owner release
can recurse once per chunk. Reconcile actual code before editing. Goal4 will own
maintained counts/statistics; avoid implementing those performance repairs here.

## Ordered work

### 1. Establish permanent depth and sharing regressions

- **Outcome:** Small-stack checks expose call-depth dependence without crashing
  the main runner, with independently expected facts/closure and shared lifetimes.
- **Focus:** Flat-built deep acyclic graphs, cycles, shared components and incoming
  noncomponent refs; long recent tails with sole/shared owners, concurrent release
  and consolidation. Reuse existing fixtures and native/value comparison support.
- **Completion signal:** Subprocess/small-stack witnesses are permanent and specify
  meaningful depths, retained old values, exact retraction closure and failure
  behavior. Distinguish reproduced failures from static risks; avoid running a
  known stack overflow in the ordinary harness process.
- **Status:** Complete. Permanent isolated small-stack graph and ownership tests.

### 2. Repair traversal and release with explicit ownership

- **Outcome:** Graph and tail depth live in explicit work/ownership structures,
  not the Rust call stack; sharing and deterministic semantics remain intact.
- **Focus:** Iterative component closure in both assessors with normal cancellation/
  admission checks. Iterative predecessor release must stop at shared nodes and
  handle racing last owners safely; unchanged snapshots must retain needed data.
- **Completion signal:** Deep small-stack checks pass, cycles terminate, incoming
  references retract correctly, shared snapshots survive and cleanup releases all
  unreferenced storage. Budget failure leaves db-before and durable state intact.
  Count traversal/release work and measure complete execution including drop;
  counters must not perform their own full traversal on the measured path.
- **Status:** Complete. Explicit DFS tasks preserve emission/read order; atomic
  last-owner extraction releases predecessor chains iteratively.

### 3. Verify real application and PostgreSQL lifecycle

- **Outcome:** Repairs work through the product with durable history and exact
  retry, not only handcrafted ownership or eager tests.
- **Focus:** Disposable PG flat graph commits, deep retraction, restart/retry,
  captured before/after values, indexing/consolidation and shared tail retention.
  Re-run relevant transaction/recent/peer regressions plus Goal1 combined checks;
  reuse the CLI/application support rather than creating another harness.
- **Completion signal:** Configured PG actually executes; exact expected closure,
  preserved old snapshots/history and receipts survive restart. Relevant suites,
  formatting and all-target compilation pass without unexplained regressions.
  Record commands, fixture conditions, measured complete-path costs and remaining
  uncertainty. Mark parent Stage2 complete, return to Goal0 and execute Stage3.
- **Status:** Complete. Actual CLI/PG retraction, shared-tail consolidation,
  retained history and restart/exact retry passed with default5s writer lease.

## Evidence and continuation

Use low-debug/nonincremental Cargo builds as in Goal0. Live disposable PG15.11 is
currently `/tmp/atomic-repair-pg.vA037i/data`, loopback55471; test URL
`host=127.0.0.1 port=55471 user=atomic_repair dbname=atomic_repair` with
`ATOMIC_POSTGRES_TRANSPORT=plaintext`. Normal durability is on. Host/socket tests
need approved execution; all test schemas/data must be isolated. Never operate on
unrelated databases. If the fixture is unavailable, recreate a disposable one
rather than calling a skipped PG test proof.

### Execution evidence and integrated gap (2026-09-10)

- R12 is repaired with iterative `LogChunk::drop` using `Arc::into_inner`;
  no layout, durable format or R7 bookkeeping changes. The old actual chunk type
  overflowed an isolated128KiB stack at1024chunks; repaired32768chunks pass, as do
  retained-predecessor and concurrent-final-owner cases. All23 recent-tier unit
  tests pass. Authenticated32768transaction/1024chunk tail rebuild retains33tx and
  releases old/new payloads only after their final owner. Complete old-tail release
  sampled4.7ms in an unoptimized build; not a throughput claim.
- Both component assessors now use explicit DFS tasks retaining their original
  prefix/emit order. Permanent small-stack/closure/admission tests are in progress.
  Existing read/byte/operation admission is preserved. The API has no separate
  graph cancellation token; client wait expiry is not transaction cancellation.
- The new actual CLI/PG lifecycle test passed128node closure, including a cycle,
  shared child and incoming/outgoing noncomponent refs (259retractions). The2048node
  seed then committed, but the writer exited before retraction: a long request
  outlived the default5s lease while holding its validated rowlock; the subsequent
  between-request renewal rejected expiry. This is an integrated availability gap,
  not an accepted test limitation. Recovery already extends its exact fenced epoch
  while retaining that lock. Apply the same rule before normal and exact-replay
  commits, preserving strict ordinary renewal and stale-owner fencing. Add actual
  slow-commit/replay regressions; do not merely lengthen the default/test lease.

### Verified completion

- `component_retraction_depth`:3/3 pass,32.29s. Isolated256KiB-stack eager1024/2048
  and exact1024/8192-node cases pass. Transaction+assertions+report-drop samples:
  eager2.596/9.021s; exact0.278/2.332s. Exact8192 flat seed1.960s. An additional
  eager8192 diagnostic also passed (123.05s transaction/check/drop); the permanent
  split avoids repeating expensive oracle construction without imposing a depth cap.
  These unoptimized diagnostic samples are not throughput claims.
- Direct reader traversal unit:257entities,514prefixes,769charged datoms (=3N−2)
  and513touched E/A pairs; traversal/worklist-drop5.989ms. Tight admission stops at
  32charged datoms; cycles/shared/incoming refs and read/byte failures preserve db-before.
- `stack_safety_postgres`:1/1 pass,28.12s. Graph128:seed441ms/retract+report339ms;
  graph2048:seed6722ms/retract+report5450ms.65small commits/reports5688ms;
  writer-stop/consolidate/adopt/drop1875ms. Actual restricted-role CLI transactor,
  normal durability, default5s lease. Exact retractions259/4099, retained histories,
  3recent chunks, zero-tail adoption and old receipt replay are asserted.
- Integrated lease fix uses the configured duration privately; successful fresh
  and replay commits extend the same holder/epoch only under the continuously held
  validated rowlock. Ordinary renewal/failed transaction expiry remain strict.
  No API/wire/hash/migration/default changes. `service_lease_lifecycle`2/2,
  `service_leadership`3/3 and `service_failover`1/1 pass on live PG; tests observe
  `pg_blocking_pids` and actual SQL expiry, including contender fencing and failure.
- Existing metadata/recent lifecycle1/1 (3.60s), production-writer/eager-oracle
  restart differential1/1 (2.28s), Goal1 combined CLI1/1 (3.86s) pass on PG.
  All23recent unit tests pass; child-only core dumps disabled for overflow witnesses.
  Formatting/diff checks and all-target Clippy pass with pre-existing warnings;
  lint hygiene remains Stage7. Logs are in the disposable fixture root.

Continuation: Goal2 complete; return to Goal0 and execute Goal3 R4/R11. Preserve
the iterative release when Goal4 maintains counts. Reopen Goal2 for any integrated
ownership/traversal/lease gap; no child-of-child folders or corrective parent.
