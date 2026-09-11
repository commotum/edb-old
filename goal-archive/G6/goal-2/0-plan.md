# Goal 2 — Composable immutable read APIs

## Objective and ownership

Applications compose captured database, tuple and log sources, navigate and compare entities, inspect index readiness, author virtual tuple boundaries and invoke stored native functions without custom plumbing.

This is Stage 1 of [Goal 0](../goal-0/0-plan.md), owning Q03, P01, P03, ST-05, P06.
Status: complete; all three internal stages and the parent-stage signal verified.
Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Reuse QuerySequence, DatabaseValue, existing index normalizers and authenticated program resolution. Preserve eager joins/aggregation with lazy Pull/transforms; no requirement to stream the whole evaluator.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Applications compose captured database, tuple and log sources, navigate and compare entities, inspect index readiness, author virtual tuple boundaries and invoke stored native functions without custom plumbing.
**Focus:** Reuse QuerySequence, DatabaseValue, existing index normalizers and authenticated program resolution. Preserve eager joins/aggregation with lazy Pull/transforms; no requirement to stream the whole evaluator.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Mixed-source lazy reads agree with eager results and preserve exact bases, cancellation and error fusion; entity identity is stable across time but not unrelated lineages; partial tuple seeks and physical readiness work; direct invocation uses captured bindings and read-only authority. Permanent public-API and real PostgreSQL/application checks pass with bounded complete-path costs.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: return to Goal 0; Goal 3 owns schema/identity allocation.

## Reconciled implementation — 2026-09-10

All stages complete. The partial evidence below records intermediate results;
the final acceptance section records completion and supersedes its pending notes.

- Q03: additive `QueryEngine::sequence_sources[_with_extensions]` and bound EDN
  `sequence`; original wrappers preserved. Relational work remains eager, Pull lazy.
  Shared deadline/cancellation/work and error fusion span both phases.
- P01: `DatabaseValue::has_avet(AttributeName)` exposes existing physical readiness,
  not merely requested indexing. A retained pending value stays pending after publication.
- ST-05: partial tuple starts are virtual values, including empty prefixes; existing
  slot/ref validation is reused. Stored tuple arity is unchanged.
- P03: `Entity: Eq + Hash`, plus a cheap owned `EntityIdentity` token, uses database
  origin and eid, not basis or cache. Durable lineage survives recovery/backup;
  independent memory origins remain distinct and clones/speculation retain origin.
  Identity is non-semantic metadata: no durable codec, checksum or allocation change.
  Local Java Entity docs and recovered `datomic/query.clj:329–340` specify this
  reference-like equality, rather than equality of all attributes.
- P06: `DatabaseValue::invoke` resolves exact-view `:db/fn` using the existing
  authenticated program runtime; query/predicate outputs retain their role and
  transaction output stays inert. No new evaluator, write authority or JVM execution.
  Actual cancellation testing exposed hidden-row scans in interpreter cursors and
  lookup normalization; those shared paths are being made cooperatively controlled.
- An independent review found legacy segmented eager-peer startup bypassed normal
  materialization identity assignment. It is being covered with an authentic legacy
  fixture, not a generation-1 test that never enters that branch.

Partial verified evidence (unoptimized, isolated actual PostgreSQL; no skips):

- `read_sequence_sources`: 7/7; existing `query_composition`: 5/5.
  Complete prepare/consume/drop at 32/128/512 rows: consume one 0.612/1.944/7.510ms;
  consume all 1.260/4.682/18.293ms. Preparation work 262/1030/4102, one projection
  adds 5; all work 422/1670/6662. These are samples/work counters, not RSS or
  throughput claims. Fixture construction measured separately.
- `read_index_authoring`: 5/5 (one PostgreSQL), existing raw-boundary lib tests 4/4.
- `read_entity_identity`: 2/2 (one PostgreSQL), including native speculative values,
  separate connections, recovery and stopped writer. 10,000 compare/hash/token
  iterations took 7.4ms and performed zero SQL; this is a micro-cost, not query scale.
- Initial `read_invoke` 4/4 after cursor repair; runtime lookup/cancellation expansion
  and integrated compatibility remain to be verified.

User workflow evolves `examples/application_workflow.rs` with a database/ordinary
planning-data join, lazy result consumption, readiness inspection and identity
across exact transaction values. `docs/read-values.md` explains contracts and limits.

## Final acceptance — 2026-09-10

- Four new public targets pass 18/18, including actual PostgreSQL: sequence 7,
  index authoring 5, identity 2, invocation 4. The invocation matrix covers
  LoadOne/LoadMany/Exists/v1 query templates and historical Lookup/LookupInput.
  Cancel at candidate 7 now stops at 7; 64 fuel stops the sampled rejecting scan
  at 6/128. Logical visible-read limits are distinct from raw-work fuel.
- Optimized integrated coverage totals 127 passing tests across these targets,
  the five EDN targets (45), product CLI (2), and existing query/program targets.
  One pre-existing opt-in fresh-process benchmark in `query_runtime_sources` was
  ignored, not counted as a pass. No PostgreSQL fixture was skipped.
  The first batch caught the new test's incorrect logical-vs-raw expectation;
  the corrected invocation and sequence targets were rerun and passed.
- Existing `program_tuple_input` adds 5 debug-profile passes. Raw-boundary unit
  tests pass 4/4. Authentic generation-zero segmented compatibility regression
  reproduced the lost-origin failure, then passed with unchanged durable rows
  and manifest bytes after the narrow repair.
- Actual stock transactor/application runs use restricted roles, two application
  processes and two writer restarts. Both print `READ_VALUES_OK`; captured-state
  calculations, planning, named partitions, fulltext and exact retries remain valid.
- Retained pre-repair receipt fixture `repair_old_receipt_c0bc499` passes the
  optimized `identity_upgrade` ignored/explicit test (1/1, 5.63s), with no reseed.
  Receipt file SHA-256 remains
  `e625cb49f32df376c9cf1e5135582347ad502de21dd7ff713f52f210dd123c32`.
- Optimized complete sequence preparation/consumption/drop samples, 32/128/512
  rows: one consumed 0.185/0.303/1.264ms; all consumed 0.282/0.533/2.033ms.
  Work counters remain 262/1030/4102 prepared and 422/1670/6662 fully consumed.
  Identity compare/hash/token 10,000 iterations: 1.464ms, zero SQL. Three complete
  eid/ident/lookup invocations: 2.216ms. These local samples are not throughput/RSS claims.
- `cargo build --offline --release -j4 --bin atomic --example application_workflow`,
  targeted `cargo test --offline --release -j4 ... -- --nocapture`, and
  `cargo clippy --offline -j4 --all-targets` succeeded. Clippy retains 24 existing
  warnings and reported none in the new APIs/tests. Formatting and diff checks pass.

PostgreSQL was the existing isolated `edn_acceptance_20260910` database on the
disposable port-55471 cluster; each new fixture owns a distinct schema. The old
receipt check used its separately retained `atomic_repair` fixture. No shared
server restart, application-data deletion, migration or canonical codec change occurred.

Continuation: this child is closed unless later integration exposes a gap.
Goal 0 remains active and selects Goal 3. Optional parent integrations remain decisions.
