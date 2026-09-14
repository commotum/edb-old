# Goal 10 — Integrated product acceptance and recovery

## Objective and ownership

The accepted capabilities form one usable native product with current-version data integrity, reliable recovery and clear fresh-database operational guidance.

This is Stage 9 of [Goal 0](../goal-0/0-plan.md), owning all required stages; AO-C02 disposition (mixed-version upgrades out of scope).
Status: complete (2026-09-10); all three internal stages and parent integrated
acceptance are verified. Goals1–9 are complete.
Preserve completed EDN and all other child work.
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
Continuation: return to Goal0 and close the required parent objective. No active
child or required implementation gap remains. Optional integrations retain the
parent's explicit deferred/out-of-scope dispositions; do not manufacture a new
corrective goal or compatibility campaign.

## Reconciled interactions

- Current-format backups and exact restored receipts passed Goal9. The additional
  native-function workflow passed1/1: speculation/commit, absent/rebound runtime
  deployments, two restarts and backup/deep proof/restore/exact retry (whole2.809s;
  commit/check15.24ms), with no callback execution during restore or exact replay.
- Integrated checks exercise new CLI prefetch/index settings with the existing
  application, new maintenance pacing through operator commands, and changed
  service/log/tree paths through stock crash/takeover. Existing Goal6 deletion,
  Goal7 excision and Goal9 cache-generation isolation evidence is retained.
- Unsupported backup formats fail with an explicit fresh-current-version message;
  no automatic reset or historical executable comparison is required.

## Final acceptance — actual results

Release checks used freshly created isolated fixtures through the configured
`ATOMIC_POSTGRES_URL`, PostgreSQL15 with fsync/synchronous_commit enabled.
`CARGO_INCREMENTAL=0 cargo test --release --offline -j4 --no-run` built the
selected targets; their test binaries ran with `--nocapture --test-threads=1`.
No PostgreSQL early return/skip is counted as a pass. Reuse the child's focused
evidence for unaffected invariants; no full historical matrix was rerun.

- `product_cli`:3/3,5.71s. Separate application processes with restricted roles,
  stock writer restart, exact receipts/references and held/as-of/history values;
  general seven-column/nested data, weighted computation, async reads, diagnostics,
  speculative planning, partitions and native fulltext all pass. Runs use CLI
  hint width3/index preparation2. Recovery from a deliberately removed derived
  publication leaves the authoritative head unchanged. Warm queries perform0SQL.
  Application baseline328/243ms and planning160/127ms are individual workload
  observations, not general performance promises.
- `remote_product::stock_auto_contender_health_and_cached_route_survive_process_crash`:
  1/1,4.03s. Real stock TLS active/standby processes, restricted roles, crash,
  exact retry, cached-route refresh, automatic excision, held reads and health
  probes pass. Cancellation52ms, takeover2083ms, measured full scenario3920ms.
- `admin_cli`:2/2,4.54s. Two disposable PostgreSQL databases; repeated backup,
  offline verification, target/permission guards, restore preview, actual
  SIGTERM at node write followed by exact-selection retry, deep inspection,
  fulltext repair and GC preview/apply. Maintenance flags are exercised for
  backup/restore/GC;3GC batches record3pauses/3ms pacing (223ms wholeGCcommand).
  Three bounded GC calls do not claim global quiescence. The first run exposed
  a stale absent-name error assertion after Goal6's catalog API; corrected to
  `catalog/name-not-found` and reran the full target, rather than weakening the
  product error or treating that first run as a pass.
- `native_transaction_functions::postgres_native_host_matches_speculation_and_retries_without_old_deployment_after_restart`:
  1/1,3.00s; detailed rebinding/backup/retry evidence above.
- Goal9's final current-format semantic/copy and canceled-restore checks remain
  valid. Goal6 lifecycle isolation and Goal7 recovery/excision evidence remain
  valid. This integration did not require changing the engine or reopening a
  child implementation; test setup/error expectations were reconciled.
- Final `cargo check --all-targets --offline -j4` passes (14.62s).
  Lib/bin Clippy passes with15existing library warnings and1existing CLI warning;
  none from this stage. `git diff --check` passes. This is not warning-free or
  commercial deployment certification.

Required audit IDs have verified/native-equivalent dispositions in
`goal-0/1-audit.md`; optional/platform cases remain explicitly deferred/out of
scope. No migration of shared old data, old-executable comparison, automatic
reset, JVM runtime, alternate store or optional new service was introduced.
