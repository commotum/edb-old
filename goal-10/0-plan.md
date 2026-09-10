# Goal 10 — Integrated product acceptance and compatibility

## Objective and ownership

The accepted capabilities form one usable native product with preserved data and credible upgrade/operational guidance.

This is Stage 9 of [Goal 0](../goal-0/0-plan.md), owning All required stages; AO-C02.
Status: scaffolded; not active. Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Exercise an evolving end-user scenario through public Rust and EDN interfaces, real PostgreSQL and stock services. Revalidate old receipts/programs/snapshots, restart/failover, deletion/excision safety and measured complete paths. Establish the supported rolling-upgrade envelope from actual version fixtures; do not claim universal mixed-version support.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** The accepted capabilities form one usable native product with preserved data and credible upgrade/operational guidance.
**Focus:** Exercise an evolving end-user scenario through public Rust and EDN interfaces, real PostgreSQL and stock services. Revalidate old receipts/programs/snapshots, restart/failover, deletion/excision safety and measured complete paths. Establish the supported rolling-upgrade envelope from actual version fixtures; do not claim universal mixed-version support.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** All required child signals and integrated workflows pass without skipped PostgreSQL claims or hidden gaps. Every audit ID has a verified implementation, supported native-equivalent disposition or explicit user decision. Reopen owning children for gaps; never mark this parent complete solely because scaffolds or one child exist.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: wait for prior required stages; reconcile source before activation.
