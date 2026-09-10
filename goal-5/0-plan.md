# Goal 5 — Native application computation

## Objective and ownership

Applications use grouped custom aggregates, practical standard data functions and explicitly deployed Rust transaction functions/predicates in ordinary production workflows.

This is Stage 4 of [Goal 0](../goal-0/0-plan.md), owning Q01, Q05, ST-02.
Status: scaffolded; not active. Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Build grouped aggregate and row-function contracts on the accepted query-value domain. Provide the portable functions actually needed by documented examples; no Clojure interpreter/reflection. Separate trusted process-local registrations from content-addressed persisted programs, with explicit deployment/version identity, db-before/db-after phases and receipt-first retry behavior.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Applications use grouped custom aggregates, practical standard data functions and explicitly deployed Rust transaction functions/predicates in ordinary production workflows.
**Focus:** Build grouped aggregate and row-function contracts on the accepted query-value domain. Provide the portable functions actually needed by documented examples; no Clojure interpreter/reflection. Separate trusted process-local registrations from content-addressed persisted programs, with explicit deployment/version identity, db-before/db-after phases and receipt-first retry behavior.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Weighted multi-argument aggregation, database-free helpers, native transaction transformations and predicates work through public/EDN interfaces. Speculation and committed execution agree; rebinding/restart never reinterprets retained receipts. Callback cooperation/limits and deployment requirements are documented and tested.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: wait for prior required stages; reconcile source before activation.
