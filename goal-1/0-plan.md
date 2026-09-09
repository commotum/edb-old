# Goal 1 — Restore a Trustworthy Runnable Baseline

## Objective

Establish a working, evidence-backed baseline for the existing Rust/PostgreSQL
database: restore compilation, verify the implemented core against real
PostgreSQL, and provide a small repeatable application workflow. Identify
remaining defects precisely so the next stages can finish the native database
without restarting completed work or mistaking skipped tests for verification.

This realizes Stage 1 of [Goal 0](../goal-0/0-plan.md). Goal 0 remains the
authority for the full database objective and subsequent child goals.

## Constraints

- Inherit Goal 0's Rust/PostgreSQL scope, Datomic information model, integrity
  guarantees, and proportionate verification requirements.
- Use `datomic_pro_docs/` as semantic authority and relevant `1.0.7705/` code
  as design evidence. Consult `goal-archive/` selectively as historical context;
  archived continuation prompts and completion labels do not govern this work.
- Preserve existing implementation, data, applied migrations, and unrelated
  changes. Repair actual failures with the smallest coherent implementation
  changes; do not replace partial features merely because their plans are stale.
- Use disposable PostgreSQL fixtures for integration and failure checks. Prove
  which tests executed, and distinguish skipped prerequisites from passes.
- Exercise the supported public application path. Do not hide an API failure
  behind direct SQL, private test helpers, or eager oracle materialization.
  Explicit fixture provisioning is separate from application behavior.
- Fix defects that prevent the baseline workflow or invalidate its guarantees.
  Record broader connection, transaction, query, and operational work under the
  owning parent stage. Do not delete or weaken failing tests to claim a pass,
  and do not require every future feature to finish this baseline milestone.
- Keep evidence and continuation notes concise. No global decompiler proof,
  mandatory broad benchmark campaign, or additional child-goal hierarchy is
  required for this goal.

## Known context — 2026-09-08

**Status:** Scaffolded; execution has not begun.

- The review at code commit `19b799d` attempted
  `cargo test --offline --all-targets --no-fail-fast`. Compilation failed in
  `src/peer.rs` because one `TieredReadCore` initializer omitted `lineage_id`;
  no tests executed. Recheck this against the current tree before repairing it.
- Persistent indexes, recent tiers, a tiered writer, local query/pull, programs,
  durable publication, and operational tooling already exist. Archived Goals
  10–16 record focused PostgreSQL/fault evidence, not a fresh passing HEAD.
- The former Goal 17 added native time points, raw-boundary normalization,
  reverse cursors, a connection facade, and report types beyond its written
  status. Existing fixtures should be reused and checked.
- Static review identified missing catch-up reports, report-gap errors after
  committed submissions when tickets are waited out of order, service-owned
  connection startup, eager peer fallbacks, and missing background/specialized
  synchronization. These are hypotheses to confirm against current code and
  tests; parent Stage 2 owns full connection/observation closure.
- The eventual baseline workflow covers schema installation, ordinary
  transactions, query/pull, history, immutable old values, and reopen. It should
  remain useful as a regression check during later children.

## Stages

### 1. Restore the build and reconcile partial implementation

**Status:** Pending.

**Outcome:** The current library, tests, and relevant application targets build,
with partial features and the first actual failures understood.

**Focus:** Inspect the current worktree and lineage propagation, repair the
missing initializer or its current equivalent, and resolve related compilation
failures. Check existing pure semantic and native cursor/time fixtures without
assuming historical completion labels still apply.

**Completion signal:** Relevant targets compile and the pure baseline checks
pass. Any separate feature failures are recorded with exact evidence and the
owning parent stage; the record does not imply the whole suite is green.

### 2. Establish actual PostgreSQL baseline evidence

**Status:** Pending.

**Outcome:** The implemented durable and native read paths have reproducible
integration evidence, and concrete regressions are distinguished from missing
configuration or fixture interference.

**Focus:** Provision or reuse an explicitly disposable, migrated PostgreSQL
fixture; run relevant existing durability, native peer/cursor, and service
checks. Investigate connection/report findings with focused witnesses. Repair
baseline regressions and preserve unresolved later-stage witnesses with clear
ownership. Use failure/restart checks where the repaired boundary warrants them.

**Completion signal:** Selected baseline integration checks actually execute
and pass on a recorded PostgreSQL configuration. Results identify executed,
failed, skipped, and unrun checks honestly. Remaining feature failures are
reproducible or explicitly bounded static findings, not silently excluded.

### 3. Establish the continuing application workflow

**Status:** Pending.

**Outcome:** A small executable workflow demonstrates the existing database
through its public Rust API and remains available for later integration work.

**Focus:** Schema as data, ordinary transaction submission, native database
capture, local query and pull, historical information, immutable old values,
and reconnect/reopen of committed state. Use a simple supported workload;
repair prerequisites without completing every Stage 2 feature. Make setup and
execution easy to repeat and keep the example separate from privileged fixtures.

**Completion signal:** The workflow runs against disposable PostgreSQL and
demonstrates expected facts, identities, temporal results, and persistence after
reopen. It exposes any temporary API limitations explicitly and does not use an
eager or privileged bypass to manufacture success.

### 4. Reconcile the baseline and return to Goal 0

**Status:** Pending.

**Outcome:** Goal 0 has an accurate runnable starting point, preserved evidence,
and a concrete next child to execute.

**Focus:** Confirm the affected checks and workflow, record material changes
and limitations, and assign remaining issues to Goal 0's existing stages.
Update parent Stage 1 only when its completion signal is met; preserve known
unresolved tests and avoid broad completion claims.

**Completion signal:** Build, selected baseline checks, and the public workflow
pass; the parent records what was verified and what remains. Return to Goal 0
to scaffold or resume Goal 2 and continue the parent loop.

## Exit and next action

Goal 1 completes when the current implementation builds, the relevant baseline
tests and public workflow run successfully on real PostgreSQL, and remaining
gaps have concrete evidence and ownership in Goal 0. This does not establish
production readiness or completion of later features.

**Next action:** Inspect the current `TieredReadCore` initialization paths and
recheck compilation; repair the observed failure while preserving database
lineage identity, then run the relevant existing checks.
