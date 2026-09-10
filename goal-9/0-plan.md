# Goal 9 — Bounded large-data reads and maintenance

## Objective and ownership

Selective offline backup reads, repeated log reads and maintenance use bounded resources with useful, measured operating controls.

This is Stage 8 of [Goal 0](../goal-0/0-plan.md), owning P05, AO09, ST-04, AO10, AO11.
Status: scaffolded; not active. Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Reuse immutable backup/log/index representations and caches. Add lazy backup access without restore/full materialization, authenticated log SSD reuse, and only effective configurable prefetch/index/backup/GC concurrency or pacing. PostgreSQL remains the durable product store; a backup reader is not a second writer backend.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Selective offline backup reads, repeated log reads and maintenance use bounded resources with useful, measured operating controls.
**Focus:** Reuse immutable backup/log/index representations and caches. Add lazy backup access without restore/full materialization, authenticated log SSD reuse, and only effective configurable prefetch/index/backup/GC concurrency or pacing. PostgreSQL remains the durable product store; a backup reader is not a second writer backend.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Increasing datasets show selective backup reads and restart-hot log reads with measured I/O/memory attribution; corruption/unavailability fail safely. Concurrency/pacing checks demonstrate bounded admission, cancellation and interference under real workloads. No speedup is asserted merely because a setting exists.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: wait for prior required stages; reconcile source before activation.
