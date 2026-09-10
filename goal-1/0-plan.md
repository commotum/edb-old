# Goal 1 — Deliver a Runnable Application and Reusable Test Baseline

## Objective

Deliver a supported local transactor, the minimal explicit setup and status
commands needed to use it, and a separate application over the existing
Rust/PostgreSQL database. Establish a small repeatable workload and reusable
checks that subsequent engine changes can use immediately.

This is Stage 1 of `/home/jake/Developer/atomic/goal-0/0-plan.md`. The parent
owns the complete product; this child delivers its first runnable application.

## Constraints and known context

- Use `/home/jake/Developer/atomic/datomic_pro_docs` as semantic authority and
  `/home/jake/Developer/atomic/1.0.7705` as architectural evidence. Archived
  G1/G2/G3 results are evidence, not active instructions or new test results.
- Build on the working service, connection, operator and pure database APIs.
  Preserve immutable values, serialized transactions, exact retry, schema/history,
  peer-local reads, durable formats and migration checksums. Do not wrap a
  self-provisioning acceptance harness as the supported product.
- Runtime roles remain restricted and never migrate on startup. Keep explicit
  administrative targets and authority separate; expose existing PostgreSQL
  TLS/I/O policy, require explicit plaintext development configuration, and
  redact credentials and subject data. Avoid unimplemented feature flags.
- Establish a stable logical local endpoint/restart contract with restrictive
  permissions, compatible with later network discovery. Preserve known commits
  and unknown outcomes; a failed local observation must not undo a known commit.
- Actual PostgreSQL and separate-executable acceptance are required. Isolate
  failure fixtures; self-skipped tests and eager compatibility reads are not
  evidence. Local `db()` capture does not prove storage-independent querying;
  existing report queues remain lossless live observation, not durable consumers.
- Goal 2 owns storage/I/O work plus `db_stats` and `request_index`; Goal 4 owns
  partition/UUID helpers; Goal 6 owns networking and the full administrative CLI.
  Other later capabilities remain in the parent plan and are not closing gates
  here. Reuse and grow this child's test support in each subsequent child.

## Stages

### 1. Expose the local runtime and minimal explicit setup

**Status:** Complete (2026-09-09). Real restricted-role CLI lifecycle, safe endpoint
restart, peer-as-writer rejection and diagnosed index recovery passed.

**Outcome:** Operators can explicitly initialize a database and run a configured
local transactor independently of application processes.

**Focus:** Supported executable entry points for the existing fenced service and
indexer lifecycle, explicit migration/create, lightweight status and documented
shutdown/restart behavior. Provide explicit consolidation recovery for a diagnosed
missing native publication; it is not routine startup work. Keep errors actionable
and configuration/endpoint contracts reusable by later deployment stages.

**Completion signal:** Real commands initialize an explicit PostgreSQL fixture;
the restricted runtime starts, exposes its endpoint/status, stops and restarts.
Invalid configuration or authority fails clearly without hidden provisioning.
The diagnosed missing-publication condition has an explicit recovery path.

### 2. Deliver the separate application workflow

**Status:** Complete (2026-09-09). Two separate application processes passed across
a graceful writer restart using restricted writer/peer roles and public native APIs.

**Outcome:** A separate Rust application uses the supported runtime and public
APIs; its calculation works against both native and fabricated database values.

**Focus:** Transact/retry, query/Pull and entity navigation, old/history values,
restart/reopen and reproducible build/run instructions. Capture one native value
for a function accepting `&DatabaseValue`; run the same function against a complete
in-memory fixture using existing `Database::new`, `with` and `database_value`.
This uses no new store or raw-tuple adapter. Pure `with` already exists; preview
success does not guarantee a later commit against a changed database.

**Completion signal:** The actual separate process submits and retries exactly,
queries/navigates and reopens durably after restart under documented permissions.
The calculation gives expected native/fixture results and gives the same result
when rerun on the retained native value after later commits. No ordinary eager
compatibility materialization substitutes for the native workflow.

### 3. Establish a reusable workload and check baseline

**Status:** Complete (2026-09-09). Application baseline and selectable seed/replay
checks ran on PostgreSQL; measurement coverage and failure limits are explicit.

**Outcome:** Later children can rerun a representative application workload and
extend existing semantic/failure checks from a recorded baseline.

**Focus:** Small reusable workload/setup, elapsed/resource measurements and
existing counters with explicit coverage, including omitted PostgreSQL calls.
Extend current pure-oracle/generated tests and fault hooks with selectable seeds
and replayable traces for a bounded relevant operation/failure sequence. Use small
clock/transport seams only where changed code benefits. Later children grow fault
coverage and failure reduction alongside their changes; do not wait until final
acceptance. This child requires no full instrumentation, framework, minimizer,
exhaustive failure campaign or complete performance repair.

**Completion signal:** Retained commands, workload and measurements reproduce the
baseline with declared limits. Relevant PostgreSQL checks actually execute, and
a recorded generated seed/trace replays through existing test support. Document
observed results and remaining costs without claiming unmeasured improvements.

## Verified delivery and costs

- `atomic` exposes explicit migrate/create/status/consolidate and the local
  transactor. Configuration shares verified TLS/I/O policy with the application.
  Endpoint preparation validates/locks before writer startup; listener failure
  stops the daemon. SIGINT/SIGTERM drain local requests and stop the writer;
  underlying I/O policy still governs database stalls. Migration and role grants
  are separate outcomes: `MIGRATED` survives a later grant failure.
- `cargo build --offline --bin atomic --example application_workflow` passed.
  With a fresh isolated PostgreSQL 15.11 fixture, `cargo test --offline --test
  product_cli -- --nocapture` passed 2/2 (5.54s): restricted_roles=true,
  two application processes/one restart, peer writer startup rejected without
  publication, missing-publication diagnosis then actual CLI consolidation to
  basis3 and restart with unchanged head. The fixture removes only its own schema
  and roles. Application query/navigation, old/history values, exact receipt
  replay, native/fixture calculation parity and zero eager materialization passed.
- `local_endpoint` passed 4/4: prepared endpoint rollback, normal restart/exact
  retry and actual SIGKILL stale-socket recovery. `--lib local_transport` passed
  8/8 with PostgreSQL configured, including unknown outcome/confirmed-commit
  regressions, ownership/path safety and listener health. The health unit witness
  deliberately ends the listener loop; it does not simulate OS descriptor exhaustion.
  Runtime configuration's two unit tests and CLI parser test passed.
- Small application baseline: first process 828ms total, 20 calculations
  30,964µs, RSS15,500KiB; replay process 728ms/35,169µs/RSS15,552KiB. Each reports
  cursor reads16/44,648bytes and peak cache8 entries/77,692bytes. These omit
  catalog/pin/lease/background/outcome SQL and are not scalability measurements.
  Goal2 owns total attribution. Reusable commands/contracts: docs/application.md.
- Seed42/36 generated steps and exact saved replay passed: 27 accepted,
  9 rejected, one orderly restart (5.97s/5.48s). Default72 full differential file
  passed5/5: 54 accepted,18 rejected,two restarts (9.59s). Failures here are
  semantic CAS/uniqueness/type rejection, not process-kill generation/reduction.
  Traces `/tmp/atomic-transactor-differential-1HZqi5.trace` and
  `/tmp/atomic-transactor-differential-hi4Dcc.trace` were byte-identical, SHA256
  `7bcfb78e2136eecd9a6d447dd831323e5c0a38a75fa371418f07394e7bd8e950`.
  Test code retains the versioned trace format and safe new-file/replay controls.

## Continuation

Complete; return to Goal0 and execute Goal2. Reuse the CLI/application and
differential driver throughout later changes. Reopen this child if integrated
checks reveal a runnable-path gap; this delivery does not complete the parent.
