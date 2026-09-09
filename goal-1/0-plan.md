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

**Status:** Pending.

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

**Status:** Pending.

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

**Status:** Pending.

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

## Continuation

Scaffold rewritten; implementation has not begun. Reconcile the parent and
current executable surfaces, then implement the supported local transactor
entry point in Stage 1. Keep this the sole active child. Record material results
and the next action; once its signals hold, return to Goal 0's loop and reconcile
the next child. Completing this child does not complete the parent.
