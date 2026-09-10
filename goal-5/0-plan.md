# Goal 5 — Native application computation

## Objective and ownership

Applications use grouped custom aggregates, practical standard data functions and explicitly deployed Rust transaction functions/predicates in ordinary production workflows.

This is Stage 4 of [Goal 0](../goal-0/0-plan.md), owning Q01, Q05, ST-02.
Status: complete; all three internal stages and application/PostgreSQL acceptance verified.
Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Build grouped aggregate and row-function contracts on the accepted query-value domain. Provide the portable functions actually needed by documented examples; no Clojure interpreter/reflection. Separate trusted process-local registrations from content-addressed persisted programs, with explicit deployment/version identity, db-before/db-after phases and receipt-first retry behavior.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Applications use grouped custom aggregates, practical standard data functions and explicitly deployed Rust transaction functions/predicates in ordinary production workflows.
**Focus:** Build grouped aggregate and row-function contracts on the accepted query-value domain. Provide the portable functions actually needed by documented examples; no Clojure interpreter/reflection. Separate trusted process-local registrations from content-addressed persisted programs, with explicit deployment/version identity, db-before/db-after phases and receipt-first retry behavior.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Weighted multi-argument aggregation, database-free helpers, native transaction transformations and predicates work through public/EDN interfaces. Speculation and committed execution agree; rebinding/restart never reinterprets retained receipts. Callback cooperation/limits and deployment requirements are documented and tested.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: return to Goal 0 and activate Goal 6. Preserve this implementation;
reopen this child if later integrated acceptance exposes a computation gap.

## Reconciled implementation contract (2026-09-10)

Q01 is a genuine extension seam: the closed Aggregate enum has all existing
built-ins but cannot express a grouped weighted reduction of two variables and
a constant. The query-reference aggregate section permits variables, constants
and sources. Recovered query.clj process-aggregates/group-fv supplies one projected
column to a partially applied function; copying that narrow mechanism would not
deliver the requested weighted workflow. Add a separate grouped aggregate call
and registry, borrowing aligned rows/columns and explicit source/constant arguments.
Preserve grouping and :with multiplicity, source-free execution, exact values,
prepared-plan reuse and shared controls. Local callbacks are not durable code;
persisting an unregistered/ambient Rust closure must still fail explicitly.

Q05's bounded native library will cover count, quot, subs, str, starts-with?,
ends-with? and includes?, including documented predicate/function positions.
Keep the existing arithmetic and built-in aggregates. Native string count/subs
use Unicode scalar positions, never byte offsets or split UTF-8; this intentional
non-JVM choice must be explicit and tested with non-ASCII/supplementary characters.
Do not change the older persisted VM Length instruction's UTF-16 meaning.
quot needs truncation toward zero for exact/floating numeric inputs, not an alias
for current division. New portable persisted query operators need an additive
version; old encodings/results remain unchanged. No Clojure reader or reflection.

ST-02 is production integration over existing exact transaction expansion and
predicate phases. Introduce an explicit immutable native deployment registry
for a Rust transactor host, with qualified/versioned deployment names, shared
cooperative controls and panic/error containment. Reuse the same registry for
exact speculative execution. Functions observe db-before; ensured entity
predicates observe complete proposed db-after. Database-stored programs retain
their temporal/content-addressed meaning and distinct resolution path. Resolve
native names only after the stored-receipt check: restart, removal or replacement
of external code must never reinterpret an accepted retry. Never materialize the
whole database to satisfy an eager callback API; retain that old API separately.
The deployment model is a compiled custom Rust host, not dynamic loading or an
arbitrary-code feature in the stock executable. Ordinary EDN symbol calls must
reach registered native functions through the existing authoritative service.

Acceptance will evolve the application with weighted planning, native text/data
helpers, a real Rust transaction transformation and predicates. Exercise grouped
and empty/bag behavior, controls/errors, old-code compatibility, exact speculation,
actual PostgreSQL/restart and receipt-first retries with the registry absent or
changed. Report callback cooperation honestly rather than claiming preemption.

## Implementation and current verification

The additive public aggregate interface is AggregateCall/AggregateArg and
QueryExtensions::register_aggregate. AggregateGroup borrows aligned cells,
constants and exact sources, with shared check(work). Built-ins, :with bags,
empty global/grouped behavior, prepared/nested/rule/lazy execution and per-run
registry validation are retained. Custom native callbacks remain explicitly
nonportable in stored program encodings.

The seven portable Function variants are implemented with bounded Unicode
operations, exact quotient and iterative readable formatting. EDN maps only
the explicit core/string aliases; unmatched qualified names remain extensions.
Template4/ABI11 is selected only for new portable operators, including nested
queries; relation-source version3 cannot demote it. Existing tags/ABI bytes are
unchanged. Migration33 is an operator compatibility fence only (no table/data/
program rewrite); old schema32 binary is retained at
`/tmp/atomic-pre-computation.kCrlcS/atomic`, SHA256
`bc22053b1ed6351dcda674e77c8ba4884e2b4c8f13b9157f142b1c3b057014bd`.
Its genuine schema32-to33 upgrade/receipt test passed on the final release build.

NativeRegistry is an immutable snapshot built with version-qualified symbols
(`demo.people.v1/add-person`); TransactionExecutionOptions combines it with
existing transaction defaults. NativeCallContext cooperatively shares program
work/bytes/call/form controls. Explicit schema-data predicate bindings use
`:atomic.native/deployment` Symbol alongside :db/ident, mutually exclusive with
:db/fn. No missing-program-to-native fallback and no closure serialization.
Retained program closure still follows actual Function-valued facts; the marker
adds no content hash or walker-version change. Marker checks were narrowed
to affected/called bindings so unrelated legacy uses of that ident are not a
global write gate. Ignored native check/reserve errors remain sticky even
when a callback returns no forms; permanent regressions cover this repaired boundary.

Optimized pure acceptance so far: 57 exercised tests across seven targets,
two PostgreSQL early returns explicitly excluded. Custom aggregate 9/9;
portable functions 9/9; existing numeric/query/nested/fuel behavior passes.
For 32/128/1024/4096 rows, complete aggregate prepare/execute/check/drop took
236/734/3827/15273us, exactly N callback visits and work 167/647/5127/20487.
Cumulative accounted bytes 21728/86624/692320/2768992 are not RSS.
Portable includes at 1024/4096/16384 bytes took 142/122/615us and work
3084/12300/49164; these single-fixture times are not throughput certification.

Initial frontend/program regressions passed 4/4 in debug: weighted :with bags,
empty groups, database-free helpers, Unicode and old/new versioned program
round trips. Alias isolation, actual ABI11 invocation, native compiled-host/stock
EDN client, application/restart and genuine old-binary upgrade tests subsequently
passed final execution below. The runnable native host and
docs/application-computation.md show how end users deploy ordinary Rust code.

## Final acceptance (2026-09-10)

Final stock atomic, application_workflow and native_transaction_host release builds
passed. Configured release acceptance passed 36/36, zero skipped/ignored, across
application_computation_edn (6), edn_query_pull (15), native_computation_cli (3),
native_transaction_functions (5), product_cli (2), query_extension_budget (5).
Actual PostgreSQL used isolated schemas on the retained local fsync/synchronous-
commit-enabled server; no shared catalog repair or fixture reseeding was used.

The stock EDN client reached a compiled Rust host over two real process lifetimes
with restricted runtime roles and exact receipts (complete fixture 1267ms).
Native speculation, both predicate phases, two restarts, backup/deep verification,
restore and absent/changed-registry retries passed: commit/check 9.44ms; complete
fixture through report drop 2.567s (cleanup included in 2.78s test-target runtime).
The intentional callback panic was caught, not a test failure. ABI11 canonical
program bytes survived reopen and native invocation (complete reopen/resolve/
execute/check/drop 41.72ms). A genuine schema32 binary created the old database
and receipt; migration33 fenced that old binary's startup and preserved the receipt.

The evolving application passed twice across restart, with weighted calculation
698/611us and zero SQL in that calculation, old bases stable and all prior planning,
partition, fulltext, SSD cache and recovery markers intact. These are sampled
complete paths, not general throughput or memory certification. Existing optimized
pure and numeric tests above remain separate evidence, not double-counted here.
All-target checking and Clippy completed; unrelated existing warnings remain.

Continuation: Goal 5 is complete. Goal 6 owns lifecycle next; later child and parent
acceptance remain unfinished. Cooperative trusted callbacks are not preemptible
sandboxes; native deployment identity and that limitation are explicit user docs.
