# Goal 4 — General query data and relations

## Objective and ownership

Applications query ordinary data of arbitrary relation width and pass nil, maps, collections and other supported query-only values without forcing them into stored datom types.

This is Stage 3 of [Goal 0](../goal-0/0-plan.md), owning Q02 (six-plus columns), Q04.
Status: complete; all three internal stages and integrated acceptance verified.
Preserve completed EDN and all other child work.
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
Current next action: return to Goal 0 and execute Goal 5. Preserve this capability
and its compatibility evidence; reopen only for a concrete integrated gap.

## Initial reconciliation (2026-09-10)

The documented data-pattern grammar accepts one or more relation columns
(`02_query_reference.md:680`), and arbitrary non-variable literals, data inputs
and pure function clauses are distinct from stored attribute types. Recovered
`datomic/datalog.clj` separates ordinary expression arguments from expressions
that explicitly need a source. Native equivalents should retain that distinction.

Keep stored `Value`, old `DataPattern`, old typed `QueryInput` and raw `Tuples`
interfaces intact. Reuse `QueryValue`/`BoundValue` and the existing evaluator,
prepared plans, joins, rules, lazy projection and budgets. General-value input,
literal and relation interfaces must be additive, with a pure Rust callback ABI
that does not require a dummy database. Preserve current collection/result meanings;
sets need explicit unordered semantics, not a reinterpretation of the existing
ordered `Collection` variant. EDN input sets currently lose that distinction.

The contract is reconciled: additive query-only Set/Char/Tagged representations,
General inputs/literals/relations, borrowed raw relation cells, registry-aware
source dependencies and pure Rust callbacks. Nil inputs remain parameters, not
synthetic ground clauses. Sets are unordered and can collection-destructure;
positional tuples/rows remain sequential. Unknown EDN tags remain inert; known
stored tags retain validation. New persisted representations select template 3 /
ABI 10; unchanged programs retain old bytes. No arbitrary JVM object/code support
is implied by general native data.

Implementation now covers the core and frontend paths; acceptance is not yet
established. Added a genuinely database-free CLI query route, a seven-column
general-data join in the evolving application, and mixed PostgreSQL/EDN source
regressions. General retained values and comparison scratch need bounded admission
before cloning/sorting; this includes discarded duplicate set members. Complete
cost, legacy encoding/receipt, PostgreSQL and application tests remain to run.

An initially proposed 16 MiB library-wide cumulative allocation default failed
the existing 100,005-result unbounded-query regression and a small forced-scan
reference. Reject that new default restriction: preserve native unbounded defaults,
add explicit `max_value_bytes` control, and retain inherited program limits. The
file/stdin CLI chooses a configurable 16 MiB allowance. Accounted cumulative
allocation is not live heap or RSS; do not describe it as such.

### Interim verification

- Public EDN general-data regressions: 6/6 pass, including nil prepared reuse,
  named seven-column rules, source-free nested queries and native pure callbacks.
- Actual PostgreSQL release `edn_cli`: 4/4 pass, 0 skipped, 13.84s. Includes
  restricted roles, map/schema/history/preview/retry, wide general relation joins,
  source descriptors without an implicit database, and data-only CLI execution
  with intentionally invalid PostgreSQL configuration.
- Separate `product_cli`: 2/2 pass, 0 skipped, 8.80s; two application processes /
  two writer restarts, old exact retries, warm peer reads, and new
  `QUERY_DATA_OK width=7 nested_values=true pure_callbacks=true old_basis=true`.
  New general application work reports 0 SQL calls in both warmed runs (sub-ms
  displayed by the current millisecond timer); no cold or throughput claim.
- EDN full-process measurements for 32/128/512 new entities: transact
  105/162/364ms, query 41/42/58ms, Pull 33/44/45ms. These are fixture costs,
  not an arbitrary-scale product claim.

Those checks precede the final general sort/dedup accounting changes; repeat the
affected acceptance on the final source. Pure core/program compatibility,
permanent general-value cost tests and genuine old-program PostgreSQL receipts
are still being verified. This child and parent remain unfinished.

### Integrated retention gap

The fixed program-dependency visitor handled only legacy query `patterns()` and
skipped `native_spec()`. This misses function-content literals in older ABI7
native queries as well as new general nested values. Complete immutable dependency
retention is an acceptance invariant, not optional query functionality. Extend
the existing visitor across native terms, nested queries/rules and Pull defaults,
including general maps/sets/tags; add a versioned reference-evidence rebuild and
old-writer fence without rewriting canonical program/transaction content.
Verify previous completed marks are not blindly trusted and referenced content
survives GC, while missing content fails closed. Goal 4 remains active through this
persisted-domain integration; the earlier Goal 3 program-retention repair is
preserved and extended, not replaced.

### Final query-domain checks

The optimized pure query batch exercised 86 cases across 14 targets; four
PostgreSQL early returns and one ignored opt-in benchmark are **not** included.
Log: `/tmp/atomic-query-general-final.swTGhs/broad-pure.log`. All 11 general-engine
cases pass, including legacy shallow tuple-find representations (one/nine columns),
10,000-deep query-only tuples on a 256 KiB stack, and general get-else defaults.
The latter follows recovered `datomic/extensions.clj:161–167`: any non-nil default
is returned unchanged; nil remains rejected. Existing stored tuple validation is
not imposed on legacy query-only result representations.

For seven-column sources growing through 32/128/1,024 rows, complete optimized
query/check/drop took 231/710/5,379 microseconds, with work 2,378/8,912/69,896.
The selective join checked two candidates (one input, one source); source scanning
still scales with row count. The existing default 100,005-result query passed in
235ms and exact persisted-program fuel assertions remained unchanged.

EDN general cases now pass 7/7. The old malformed-input test that rejected every
scalar map was replaced with exact general-map acceptance plus an explicit invalid
positional-tuple test; this is the requested capability, not a weakened safety
check. Old EDN format/value/query/transaction pure cases pass; their PostgreSQL
early returns are excluded pending the configured final run.

### Integrated acceptance results

Final optimized configured PostgreSQL/application targets passed: edn_cli 4,
edn_query_pull 15, edn_transactions 9, product_cli 2, query_extension_budget 5,
query_extensions 2, query_nested_shapes 8 and query_runtime_sources 10. These
counts include actually exercised PostgreSQL cases; the last target's one opt-in
benchmark remains ignored and is excluded. General EDN 7 and debug 5 also pass.
Four older query fixtures now use disposable schema isolation. A first attempt
to migrate their shared catalog failed closed with missing referenced program
content before fixture construction; shared content was left untouched, not
deleted or repaired to pass a test.

Final general-program target: 8/8, including actual PostgreSQL, 1.09s. Shallow
tuple normalization agrees with both the original query behavior and direct
query execution; map/set/tag/nil results retain their general representation.
Complete invoke/check/drop was 807us, reopened 857us; full fixture including
install/speculation/reopen was 927ms. Genuine old ABI7 bytes/hash/observation,
historical database invocation/predicates/exact retries, runtime 22 cases and
dependency visitor 2 cases pass. The old artifact was generated with a retained
schema-30 library, not a current encoder round trip.

Schema 32 / program-reference walker 2 acceptance: 2 actual isolated PostgreSQL
cases pass in 2.21s. Old walker marks are rebuilt, native/general literal content
survives GC, unused content is collected, canonical bytes remain unchanged, and
corrupt/missing dependencies fail closed with recovery tested. Rebuild/GC took
120ms excluding setup. Existing quiesced migration protocol and startup version
fence apply; no online mixed-version writer guarantee is inferred.

General-value complete-path benchmark 8/8 passes. For 32/128/512 rows, hash path
prepare/execute/independent validation/drop took 2.78/9.52/42.49ms, work
48,022/234,118/1,101,766 and cumulative accounted bytes
4,665,856/22,687,744/106,571,776. Forced scans took 9.24/129.25/2,034.72ms,
work 202,198/2,829,190/43,135,942. Hash full-path growth includes N log N
canonical projection; it is not a pure linear join-only claim. Explicit benchmark
caps are 256MiB/8GiB cumulative allocation, not RSS.

Final stock application retains both QUERY_DATA_OK markers and warm 0 SQL reads.
Final EDN full-process costs at 32/128/512 entities: transact 86/154/346ms,
query 40/31/56ms, Pull 34/37/43ms. Clippy all targets passes with 15 existing
library warnings; new warnings were corrected. Product semantics, not warning
cleanup or JVM representation copying, determined the changes.

The final EDN input-map comparison fix is verified: optimized edn_value private
tests 6/6, no skips, 0.01s. A 40-set-key map cannot consume only traversal budget
and then sort for free; canonical comparisons and scratch are admitted, failure
is latched, and numeric/unordered-set key collisions remain rejected. This closes
the last reported conversion gap without an API or durable-format change.
