# Goal 9 — Bounded large-data reads and maintenance

## Objective and ownership

Selective offline backup reads, repeated log reads and maintenance use bounded resources with useful, measured operating controls.

This is Stage 8 of [Goal 0](../goal-0/0-plan.md), owning P05, AO09, ST-04, AO10, AO11.
Status: active continuation; internal Stage1 reconciliation is next. Goal8 is
complete; no Goal9 implementation/acceptance is claimed. Preserve completed EDN
and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Goal 0's fresh-database
policy governs acceptance: schema/format changes may require a newly created
database. Cross-version database/format migrations, old-binary fixtures and
mixed-version rolling upgrades are not required. Reject unsupported formats
clearly; never silently reinterpret them or reset a database automatically.
Preserve current-version data integrity, exact identity/schema/values, immutable
history, restart/crash recovery, same-version failover, current-version backup/
restore and receipt-first exact retries, including after function rebinding in a
supported database. Retain cancellation, isolation and complete-path performance
requirements. This changes database-version support, not the existing Rust/EDN API
scope, and does not create a cleanup task to remove working compatibility code.
No JVM evaluation, replacement engine, alternate durable store or recursive goals.
Parent decisions govern optional scope.

Apply Goal0's proportionate-implementation policy: reuse unaffected evidence and
the existing application, use focused regressions and relevant integration, and
measure only useful complete paths. No duplicate build/transport/failure matrices,
invented absolute bounds or literal component parity. Required capabilities and
current-version safety remain; unapproved optional work is deferred, not a blocker.

Reuse immutable backup/log/index representations and caches. Add lazy backup access without restore/full materialization, authenticated log SSD reuse, and only effective configurable prefetch/index/backup/GC concurrency or pacing. PostgreSQL remains the durable product store; a backup reader is not a second writer backend.

Here SSD reuse means authenticated persistent local cache reuse; it is not a
hardware requirement. Bound admission, cache/queue residency and meaningful work
chunks, not all intermediate computation or a universal RSS/time ceiling. Choose
prefetch, concurrency or pacing at demonstrated bottlenecks; do not add a knob
for every phase or benchmark every setting combination. Selective offline reads
must actually avoid full backup materialization; documentation alone cannot waive
that requested capability.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Reproduce uncertain behavior; use existing evidence for obvious API/integration
gaps. Record only consequential decisions here.
**Completion signal:** Required behavior, native design, API and current-version integrity risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Selective offline backup reads, repeated log reads and maintenance use bounded resources with useful, measured operating controls.
**Focus:** Reuse immutable backup/log/index representations and caches. Add lazy backup access without restore/full materialization, authenticated log SSD reuse, and only effective configurable prefetch/index/backup/GC concurrency or pacing. PostgreSQL remains the durable product store; a backup reader is not a second writer backend.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified current-version operation and recovery.
**Focus:** Evolve application examples and EDN paths where relevant; use freshly
created PostgreSQL databases and ordinary service/peer boundaries. Verify exact
retries, immutable history, restart/crash recovery, same-version failover and
current-version backup/restore where relevant; measure complete costs. Historical
format and old-binary upgrade fixtures are not acceptance gates. Reopen
implementation for integration gaps.
**Completion signal:** Increasing datasets show selective backup reads and restart-hot log reads with measured I/O/memory attribution; corruption/unavailability fail safely. Concurrency/pacing checks demonstrate bounded admission, cancellation and interference under real workloads. No speedup is asserted merely because a setting exists.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: reconcile the existing backup reader, log cache and maintenance
controls against P05/AO09/ST-04/AO10/AO11. Reuse accepted cache attribution and stock
application support from Goal8. Start with the selective offline-backup user path;
inspect relevant source/docs, then implement the smallest coherent native advance.
No historical upgrades, repeated prior-child matrices or optional integration work.
