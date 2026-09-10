# Goal 2 — Stack-safe graph traversal and recent-log ownership

## Objective and constraints

Complete Stage2 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: repair R6
(recursive stored-component retraction in both assessors) and R12 (recursive
last-owner destruction of the recent-log predecessor chain). Preserve all Goal1
repairs, immutable values, exact transaction meaning, retained snapshots and
durable request receipts. Status: **in progress**. This is Goal0's only active child.

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
- **Status:** In progress.

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
- **Status:** Not started.

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
- **Status:** Not started.

## Evidence and continuation

Use low-debug/nonincremental Cargo builds as in Goal0. Live disposable PG15.11 is
currently `/tmp/atomic-repair-pg.vA037i/data`, loopback55471; test URL
`host=127.0.0.1 port=55471 user=atomic_repair dbname=atomic_repair` with
`ATOMIC_POSTGRES_TRANSPORT=plaintext`. Normal durability is on. Host/socket tests
need approved execution; all test schemas/data must be isolated. Never operate on
unrelated databases. If the fixture is unavailable, recreate a disposable one
rather than calling a skipped PG test proof.

Continuation (2026-09-10): Goal1 verified; begin R6/R12 in parallel within this
child. Root owns PG/application integration. Keep the parent aligned, and hand
back to its loop after this signal; there are no child-of-child goal folders.
