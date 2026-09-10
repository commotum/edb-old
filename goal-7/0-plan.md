# Goal 7 — Reliable stock services and clients

## Objective and ownership

Stock executables support persistent active/standby operation, bounded automatic excision, recoverable routing, usable asynchronous clients and clear health/readiness.

This is Stage 6 of [Goal 0](../goal-0/0-plan.md), owning AO04, AO05, AO-C01, P04, AO07; AO-C03 disposition.
Status: active (2026-09-10); internal Stage1 reconciled, implementation next.
Goal6 final acceptance passed; preserve completed EDN and all prior child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Reuse fenced service/standby/excision mechanisms and existing native transports. Retain configured indexing/listener/TLS settings across takeover. Add Rust Future/stream-facing adapters without blocking executor threads; distinguish dropping a waiter from cancelling an already durable transaction. Define liveness versus write readiness. Evaluate PID/multi-database packaging by actual supervisor needs.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Stock executables support persistent active/standby operation, bounded automatic excision, recoverable routing, usable asynchronous clients and clear health/readiness.
**Focus:** Reuse fenced service/standby/excision mechanisms and existing native transports. Retain configured indexing/listener/TLS settings across takeover. Add Rust Future/stream-facing adapters without blocking executor threads; distinguish dropping a waiter from cancelling an already durable transaction. Define liveness versus write readiness. Evaluate PID/multi-database packaging by actual supervisor needs.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Actual separate processes show standby takeover, exact retries through route change, read continuity, committed excision progress under runtime roles, shutdown and failure recovery. Single-thread executor checks prove nonblocking public adapters; health endpoints reflect actual availability without leaking credentials. Packaging differences receive explicit user-oriented dispositions.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: implement the source-backed service/client contract below,
then exercise the complete separate-process and asynchronous application workflows.

## Reconciled native contract

The HA/deployment docs require persistent contenders, takeover and independent
peer reads; recovered lifecycle code serves only after acquiring authority.
Preserve resolve-once storage identity, configured indexing, native deployments,
partition defaults and verified transport settings across takeover. Add nonblocking
standby status/try-active and interruptible polling without rewriting service queues.
Stock automatic-election mode may be additive to existing fail-fast startup.
Health means the process/supervisor is functioning; readiness additionally needs
active writer authority and its configured listener. No credentials or subject data
belong in probes. Foreground supervisor identity and one database per process are
native deployment defaults; assess documented composition instead of copying PID files.

Automatic excision belongs to indexing and processes committed requests, including
restart recovery, with one resumable job and bounded admission/steps. The excision
doc explicitly permits whole-database work and reduced write availability: do not
invent constant-cost requirements. Reuse existing authenticated generation rewrite,
pins/checkpoints/root-last completion; account admitted eager phases honestly and
check cooperative cancellation. Narrow lease/epoch-checked runtime authority to
committed-request jobs, never broad restore/GC/activation grants. A concrete source
gap exists in writer tree adoption after generation replacement; repair it while
preserving receipt-first assessment and intentional predates-excision tombstones.

An additive bounded Rust Future/Stream facade performs blocking driver/evaluator
work off executor threads and owns cleanup of abandoned jobs/results. Capture read
bases at admission, include queue time in deadlines, demand-drive stream chunks,
fuse terminal errors and distinguish dropped waiters from transaction cancellation.
Native values retain their existing synchronous method/last-drop contract unless
explicitly wrapped; do not silently claim otherwise. Completion callbacks are
once-only native closures, not ported Java Executor machinery.

Connection-bound remote routing captures stable identity and verified TLS policy,
refreshes stale endpoints, and preserves known commit versus report-open failure.
Route recovery is not permission to replay arbitrary transactions or regenerate
request keys. Ambiguous outcomes must remain explicit and exact-retry safe.

Accepted pre-stage schema34 binary: /tmp/atomic-pre-services.vJBL8z/atomic,
SHA256 ac98c9c660dbdcb4b6b1d1226735e6e4b98053aba0ff5cf137b1a8867091a7d3.
Use it for genuine compatibility checks before any new migration is considered done.
