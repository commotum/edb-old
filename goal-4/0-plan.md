# Goal 4 — General query data and relations

## Objective and ownership

Applications query ordinary data of arbitrary relation width and pass nil, maps, collections and other supported query-only values without forcing them into stored datom types.

This is Stage 3 of [Goal 0](../goal-0/0-plan.md), owning Q02 (six-plus columns), Q04.
Status: scaffolded; not active. Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Introduce additive query-only representations and callback contracts while retaining existing typed wrappers and the optimized datom path. Extend EDN adapters and version persisted query/program forms only when new representations are used. Keep equality/hash, shape, resource limits and immutable source identity coherent.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Applications query ordinary data of arbitrary relation width and pass nil, maps, collections and other supported query-only values without forcing them into stored datom types.
**Focus:** Introduce additive query-only representations and callback contracts while retaining existing typed wrappers and the optimized datom path. Extend EDN adapters and version persisted query/program forms only when new representations are used. Keep equality/hash, shape, resource limits and immutable source identity coherent.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Typed and EDN tests cover arbitrary-width named sources, mixed nested general values, constants and database-free callbacks, with prepared reuse, rule/subquery scoping and exact numeric behavior. Old program bytes/receipts remain valid; bounded memory/work and actual PostgreSQL mixed-source workflows pass.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: wait for prior required stages; reconcile source before activation.
