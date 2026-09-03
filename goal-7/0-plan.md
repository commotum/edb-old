# Goal 7 — Available Fenced Transaction Service

## Objective

Turn the durable `PostgresStore` write owner into a bounded production-shaped
Rust transaction service that preserves Datomic's single serialized
transactor, actionable anomaly categories, explicit unknown outcomes, live
transaction reports, and standby failover. PostgreSQL is the sole coordinator:
leadership has monotonic epochs and leases, and every publication is fenced by
the epoch held by the service that assessed it. Reads remain local and
available at peers throughout writer replacement.

This realizes Stage 7 of `goal-0/0-plan.md`. It does not add backup, garbage
collection, excision, full observability infrastructure, deployment packaging,
or the other Goal 8 lifecycle work.

## Constraints

- Rust and PostgreSQL only; no generic coordinator/storage layer and no JVM,
  Clojure, Artemis/HornetQ, or application-server emulation.
- Preserve the documented model: transactions enter a bounded queue, execute
  serially, complete only after durable storage acknowledgement, and may have
  unknown outcome after caller timeout/disconnection. Applications choose
  semantic retry policy using durable idempotency keys.
- Treat the existing pure kernel, PostgreSQL durability/idempotency, peer, and
  program contracts as fixed lower boundaries. Do not move semantic assessment
  into request handlers or weaken expected-basis publication.
- Model the recovered `ConnectionState`, 128-slot unsent queue,
  `await-tx-result`, pending failure, transaction report queue, endpoint
  discovery/reconnection, active/standby heartbeat, and anomaly conversion by
  their intent, using idiomatic bounded Rust channels and typed results.
- PostgreSQL leadership rows own a monotonically increasing epoch, holder id,
  and expiry. Acquisition/renewal/release are conditional; transaction
  publication must check the held epoch in the same SQL transaction as the
  database-head update. A paused or replaced owner can never publish.
- Use database/server time for leases; never rely on process wall clocks for
  safety. Lease duration affects availability, not correctness.
- Backpressure is admission control, not hidden unbounded buffering. Shutdown
  and leadership loss deterministically resolve queued/pending requests.
- Keep the service boundary transport-neutral and small. A network protocol,
  authentication, HTTP framework, and deployment control plane are not needed
  to prove this stage.

## Known context

- Goal 3 already serializes writers with `FOR UPDATE`, expected basis/hash CAS,
  immutable transactions, durable request keys, and explicit `UnknownOutcome`.
- Goal 4 peers can read immutable snapshots, synchronize monotonically, and
  keep reading while no writer is active.
- Goal 6 adds exact program identity but does not alter the publication rule.
- The docs require serial transaction processing, durable acknowledgement,
  timeout-as-unknown, per-connection reports, monotonic peer views, and manual
  application retry. HA uses one active plus standby; takeover follows missed
  heartbeats and reads remain available.
- Recovered `peer.clj` uses bounded 128-entry unsent/lucene queues, promises for
  pending transactions, reconnection and pending failure; `cluster.clj` uses
  bounded asynchronous queues and barriers. The native mapping can omit the
  recovered messaging stack while preserving boundedness and control flow.

## Stages

### 1. Service and leadership contract

**Status:** Complete. `SERVICE_CONTRACT.md` maps the documented queue,
timeout-as-unknown, report and HA semantics plus recovered `ConnectionState`,
pending promise, 128-slot queue, reconnect and active-master lifecycle into a
bounded Rust worker and PostgreSQL epoch fence. It fixes request states,
shutdown, subscriber overflow, server-time lease, publication, retry, and
peer-read invariants while explicitly excluding a transport stack.

**Outcome:** Safety/liveness, request lifecycle, anomaly/retry, queue, report,
lease, fencing, timeout, and shutdown semantics are explicit and traced.

**Completion signal:** A concise contract maps recovered boundaries to Rust
and PostgreSQL, distinguishes rejected/failed/unknown outcomes, and states the
invariants every subsequent test must enforce.

### 2. PostgreSQL epoch lease and publication fence

**Status:** Complete. Migration 4 adds the sole coordination row with holder,
positive monotonic epoch and server-time expiry. `PostgresStore` implements
advisory-serialized acquire, live-only renew, conditional release, and
`transact_fenced`; fence verification locks the lease inside the same SQL
transaction before the database head. Real PostgreSQL tests prove competing
holders, expiry, renewal refusal after expiry, concurrent takeover with one
higher-epoch winner, valid fenced commits, and rejection of a stale holder
whose expected database basis is otherwise current.

**Outcome:** One holder owns a renewable lease at a monotonic epoch, and stale
epochs cannot publish even if their process resumes with an open connection.

**Completion signal:** Real PostgreSQL tests cover acquire, renew, expiry,
takeover, release, concurrent candidates, server-time behavior, and a stale
owner attempting publication after replacement.

### 3. Bounded serialized transaction worker

**Outcome:** A Rust service owns one `PostgresStore`, admits requests through a
fixed queue, processes them in order, returns typed outcomes, and fails pending
work cleanly on shutdown or leadership loss.

**Completion signal:** Queue saturation is `Busy`; accepted requests never run
concurrently; overload is bounded; cancellation/timeout semantics are explicit;
and graceful/abrupt worker tests leave no hidden pending work.

### 4. Durable retry, reports, and peer-facing integration

**Outcome:** Sync and async submission expose request keys, expected basis,
transaction reports and unknown outcomes without automatic semantic retry.

**Completion signal:** Tests prove duplicate requests resolve once, reused keys
conflict, reports include immutable before/after/data/tempids, slow/timeout
calls can be reconciled, observers can unsubscribe without leaks, and peer
reads/sync remain monotonic.

### 5. Standby takeover and failure injection

**Outcome:** A standby catches up, acquires a higher epoch after lease expiry,
and serves writes without fork while the old owner is fenced.

**Completion signal:** Multi-process or independent-service tests inject pause,
process death, connection loss, acknowledgement loss, and concurrent takeover;
exactly one epoch publishes, history is contiguous, and reads remain usable.

### 6. Stress and integrated verification

**Outcome:** The transaction service remains bounded, diagnosable, and correct
under sustained concurrency and the full prior feature set.

**Completion signal:** Measured stress establishes configured queue bounds and
serial outcomes; all format/lint/unit/integration/restart/fault suites pass on
real PostgreSQL; deliberate deviations and remaining Goal 8 work are recorded.

## Availability exit condition

Goal 7 is complete when the production-shaped Rust write service provides one
fenced serialized owner, bounded admission, actionable/unknown outcomes,
durable idempotent retry, transaction reports, and automatic standby takeover;
stale owners cannot fork history under injected failure, and peer reads remain
available. Lifecycle/backup/privacy/operational completion remains Goal 8.
