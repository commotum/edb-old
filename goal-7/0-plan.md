# Goal 7 — Reliable stock services and clients

## Objective and ownership

Stock executables support persistent active/standby operation, bounded automatic excision, recoverable routing, usable asynchronous clients and clear health/readiness.

This is Stage 6 of [Goal 0](../goal-0/0-plan.md), owning AO04, AO05, AO-C01, P04, AO07; AO-C03 disposition.
Status: scaffolded; not active. Preserve completed EDN and all other child work.
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
Current next action: wait for prior required stages; reconcile source before activation.
