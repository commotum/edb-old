# Goal 8 — Actionable diagnostics and operational signals

## Objective and ownership

Users can explain expensive queries/transactions and operate the product using correlated, named metrics and structured events.

This is Stage 7 of [Goal 0](../goal-0/0-plan.md), owning D01, D02, D03, AO06, Q06.
Status: scaffolded; not active. Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Extend the existing attribution system with per-operation cache/index work, stable clause/binding/phase identities and transaction-correlated semantic counters. Add bounded configurable publication/logging and meaningful alarms. Retain the native optimizer by default; explain actual scheduling rather than copy Clojure tuning rituals.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Users can explain expensive queries/transactions and operate the product using correlated, named metrics and structured events.
**Focus:** Extend the existing attribution system with per-operation cache/index work, stable clause/binding/phase identities and transaction-correlated semantic counters. Add bounded configurable publication/logging and meaningful alarms. Retain the native optimizer by default; explain actual scheduling rather than copy Clojure tuning rituals.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Concurrent cached/uncached reads, nested queries, upserts/program transactions and indexing/failover produce attributable reports without cross-operation contamination or payload/secret leakage. Stock service emits verified events/counters; disabled instrumentation has measured costs and no semantic effects.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: wait for prior required stages; reconcile source before activation.
