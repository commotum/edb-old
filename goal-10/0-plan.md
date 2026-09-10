# Goal 10 — Integrated product acceptance and recovery

## Objective and ownership

The accepted capabilities form one usable native product with current-version data integrity, reliable recovery and clear fresh-database operational guidance.

This is Stage 9 of [Goal 0](../goal-0/0-plan.md), owning all required stages; AO-C02 disposition (mixed-version upgrades out of scope).
Status: scaffolded; not active. Preserve completed EDN and all other child work.
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

Exercise an evolving end-user scenario through public Rust and EDN interfaces, freshly created PostgreSQL databases and stock services. Verify current-version receipts/programs/snapshots, exact retries after rebinding, restart/crash recovery, same-version failover, current-version backup/restore, deletion/excision safety, cancellation/isolation and measured complete paths. Verify clear rejection of unsupported formats and document when a schema/format change requires fresh database creation. AO-C02 mixed-version rolling upgrades are out of scope under the parent's decision; no historical version fixtures or migrations are required for acceptance.

## Internal stages

### 1. Reconcile accepted capabilities and remaining interactions

**Outcome:** A small set of cross-feature checks not already established by the children.
**Focus:** Reuse their accepted evidence and existing application. Inspect source
only for unresolved semantics or subsequent changes affecting an invariant. Do not
repeat the corpus audit, all fault/size matrices, or every child benchmark.
**Completion signal:** Remaining integration risks and their checks are identified.

### 2. Exercise the integrated product and repair exposed gaps

**Outcome:** The accepted capabilities form one usable native product with current-version data integrity, reliable recovery and clear fresh-database operational guidance.
**Focus:** Exercise an evolving end-user scenario through public Rust and EDN interfaces, freshly created PostgreSQL databases and stock services. Verify current-version receipts/programs/snapshots, exact retries after rebinding, restart/crash recovery, same-version failover, current-version backup/restore, deletion/excision safety, cancellation/isolation and measured complete paths. Verify clear rejection of unsupported formats and document when a schema/format change requires fresh database creation. AO-C02 mixed-version rolling upgrades are out of scope under the parent's decision; no historical version fixtures or migrations are required for acceptance.
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
**Completion signal:** Required capabilities and representative integrated workflows
pass, reusing unaffected child evidence. No skipped PostgreSQL claims or hidden
required gaps. Audit IDs have verified/native-equivalent or explicit deferred/out-of-
scope dispositions; optional unapproved features are not blockers. Reopen owning
children only for actual gaps, not another acceptance ceremony.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: wait for prior required stages; reconcile source before activation.
