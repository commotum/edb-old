# Goal 5 — Local Datalog, Pull, and Native Rust API

## Objective

Build the application-facing Rust layer over Goal 4 peer snapshots: a native,
local Datalog engine with Datomic set semantics, rules and recursion,
negation/disjunction, predicates, functions, aggregates and `:with`; pull and
entity navigation; and one coherent peer API for immutable reads,
synchronization, and transaction submission. Query evaluation must remain a
peer-local read operation over an explicitly supplied immutable database value.

This goal realizes Stage 5 of `goal-0/0-plan.md`. It does not add persisted
custom code, a network service, HA leadership, backup, garbage collection, or
other operational lifecycle machinery.

## Constraints

- Use Rust throughout and PostgreSQL only through the already-established
  store/peer boundaries. Do not introduce a generic storage or query-source
  backend merely for portability.
- Treat `datomic_pro_docs/05_query_and_pull`, the peer API reference, and index
  pull documentation as semantic authority. Trace `1.0.7705` `datomic.datalog`,
  `datomic.query`, `datomic.query.support`, `datomic.pull`, and Java/Clojure API
  shapes as the default blueprint before fixing representations or algorithms.
- Preserve data patterns, query inputs, relation/set semantics, variable
  unification, rules and fixed-point recursion, `not`/`not-join`, `or`/
  `or-join`, predicates/functions, aggregates, `:with`, collection/tuple/
  relation/scalar result shapes, pull recursion/components, missing behavior,
  and unspecified ordering.
- Keep every query and pull operation read-only and local to one immutable
  snapshot. Never consult a newer connection value midway through evaluation.
- Use Goal 2's straightforward database/index behavior as the source oracle
  and build a deliberately simple evaluator first. Add planning/index
  selection only behind differential tests against that evaluator.
- Provide structured parse, binding, semantic, timeout, cancellation, and
  resource-limit errors. Do not panic on application input.
- Native Rust query structures are the primary API. A small EDN-like textual
  form may be added only when it materially improves compatibility/testing; no
  JVM/Clojure API or result-type compatibility is required.
- Preserve unordered result semantics. Deterministic internal order is useful
  for testing but must not become an accidental public ordering promise.
- Keep built-ins in this goal finite and explicit. Persisted/custom function
  deployment and sandboxing belong to Goal 6.

## Known context

- Goal 4 exposes complete current/history/time-view index data on immutable
  `Arc<Database>` snapshots, plus peer synchronization and transaction reports.
- The kernel's four covering index orders permit local clause access without
  PostgreSQL round trips. Query code should choose the most selective bound
  prefix while retaining a scan/reference evaluator as an executable oracle.
- Existing `Value` equality and index comparison intentionally differ for a
  few stored representations. Query equality, grouping, deduplication, and
  ordering must make that boundary explicit rather than assuming Rust `Eq`.
- Goal 2 has native transaction forms and process-local transaction functions;
  Goal 5 should expose them coherently but not redesign transaction semantics.

## Stages

### 1. Recovered query/pull contract and native surface

**Status:** Complete. `QUERY_CONTRACT.md` maps the documented grammar and the
recovered relation/scheduler/query/pull boundaries to typed Rust requests,
snapshot-local evaluation, set/bag rules, explicit resource controls, pull
frames, structured errors, and a finite supported built-in surface.

**Outcome:** Supported syntax, value/binding semantics, result shapes, errors,
and snapshot ownership are fixed before optimizer choices become observable.

**Focus:** Map documented query grammar and pull patterns to recovered parser,
context, relation, clause, rule, aggregate, and pull boundaries; define the
typed Rust AST and public request/result forms; list intentionally deferred
features and justified representation deviations.

**Completion signal:** A concise contract plus executable construction/parse
fixtures distinguish all supported forms and reject malformed or ambiguous
forms with stable errors.

### 2. Reference relations, inputs, and data clauses

**Status:** Complete. `query.rs` implements typed scalar/tuple/collection/
relation bindings, named immutable sources, all four find shapes, 3–5 component
patterns, set projection and `with` bags. Pure tests cover current, history,
`as-of`, multiple sources, blanks/constants, and shape errors without storage.

**Outcome:** A simple local evaluator executes `:find`, `:in`, `:where` data
patterns over immutable current and temporal database values.

**Focus:** Query values and variables, scalar/tuple/collection/relation inputs,
source variables, constants, blanks, relation headers, unification, set
deduplication, result shaping, and straightforward scan evaluation.

**Completion signal:** Fixtures cover bound/unbound permutations, multiple
sources and input shapes, duplicate elimination, missing attributes, temporal
views, and collection/tuple/relation/scalar results without PostgreSQL reads.

### 3. Joins and index-aware execution

**Status:** Complete. A binding-aware scheduler selects ready/selective clauses
and uses EAVT/AEVT/AVET/VAET prefixes from bound components. Every request
returns native plan/stats data; `QueryControl::force_scan` is the EAVT oracle.
Differential tests match it and a 500-entity workload examines one AVET match
instead of more than 1,000 datoms.

**Outcome:** Multi-clause queries use safe join planning and the four local
covering indexes without changing reference results.

**Focus:** Hash/merge/index joins where warranted, clause ordering from bound
variables and simple cardinality estimates, AVET/VAET eligibility, reusable
plans independent of input values, cancellation checkpoints, row/intermediate
limits, and explain data.

**Completion signal:** Differential/property tests match the reference
evaluator across clause permutations and adversarial bindings; explain output
shows index/scan choices; measured selective queries avoid full-database scans.

### 4. Logical clauses and recursive rules

**Status:** Complete. Set difference/union implement `not`/`not-join` and
`or`/`or-join` with explicit local-variable isolation. Multiple rule heads grow
duplicate-free relations to a bounded fixed point; required bindings,
recursive graph cycles, branch variables, and insufficient bindings have
executable coverage.

**Outcome:** `not`, `not-join`, `or`, `or-join`, rule invocation, and terminating
fixed-point recursion implement the documented variable-scope semantics.

**Focus:** Required bindings, branch variable agreement, rule heads, recursive
deduplication, cycle termination, stratified negation boundary, and stable
errors for unsafe forms.

**Completion signal:** Recursive graph, branch, negation, explicit join, cycle,
and unsafe-binding fixtures pass independently of rule/clause ordering.

### 5. Predicates, functions, aggregates, and `:with`

**Status:** Complete for the surface listed in `SUPPORTED_SURFACE.md`.
Comparison/missing predicates; native arithmetic, ground, tuple/untuple,
get-else/get-some functions; and deterministic documented aggregates are
implemented. Tests establish grouping, numeric behavior, distinctness and
count multiplicity after `with`. Random aggregates and host-language calls are
deliberately excluded.

**Outcome:** The documented core expression and aggregation model works with
correct set/bag boundaries.

**Focus:** Comparison/equality/arithmetic predicates, `ground`, `get-else`,
`get-some`, tuple/collection bindings, pure function clauses, grouping,
aggregate distinctness, `:with` basis expansion, min/max/count/sum/avg and
limit-bearing aggregates, nil/missing/error behavior, and resource accounting.

**Completion signal:** Focused examples and randomized differential fixtures
establish predicate/function bindings, grouping keys, duplicate handling, and
the documented `:with` aggregate behavior.

### 6. Pull, entity navigation, and peer-facing API

**Status:** Complete. `pull.rs` provides wildcard, forward/reverse attributes,
aliases, defaults, cardinality limits, nested/component traversal, recursion
and cycle fallback, cancellation/work limits, immutable `Entity` values,
multiple pulls, and query pull expressions. `Peer` composes transact/sync/
query/pull/entity on one captured basis and the real PostgreSQL end-to-end test
keeps an old query/entity snapshot exact during advancement.

**Outcome:** Applications can navigate entities, pull graphs, query snapshots,
submit transactions, and synchronize through a cohesive native Rust API.

**Focus:** Pull wildcard/attribute/map forms, aliases, defaults, limits,
reverse attributes, components, recursion and cycle control; immutable entity
views; query/pull composition; peer transaction submission with idempotency and
expected basis; cancellation/timeouts and structured results.

**Completion signal:** Pull/entity fixtures cover cycles, components, reverse
refs, missing/default values, recursion, limits, aliases, and old snapshots;
an end-to-end Rust test transacts, syncs, queries, navigates, and pulls without
mixing database bases.

### 7. Semantic, concurrency, and performance verification

**Status:** Complete. Nine query tests, five pull tests, and the real-store peer
suite cover forced-scan differential results, clause-order scheduling,
recursive cycles, explicit-join scope, all result/input forms, malformed and
resource errors, deep/cyclic pull, concurrent old-snapshot queries, selective
index work, and end-to-end PostgreSQL submission/sync.

**Outcome:** The complete native read/API layer is demonstrably correct,
snapshot-local, bounded when requested, and useful on production-shaped data.

**Focus:** Differential corpus across all supported clauses/views; concurrent
queries while peers advance; cancellation and hard intermediate/result limits;
deep/wide pull; recursive cycles; malformed/adversarial requests; stable
unordered results; planner regression measurements; full prior suite.

**Completion signal:** Reference and optimized evaluators agree, old snapshots
remain exact during concurrent advancement, bounds terminate pathological work,
selective workloads use indexes, and all Rust format/lint/test suites pass.

## Query/API exit condition

Goal 5 is complete when Rust applications can submit transactions and obtain
immutable peer snapshots, run the supported Datomic-shaped Datalog semantics
and pull/entity navigation entirely locally, cancel or bound expensive work,
inspect execution choices, and obtain results that differentially match the
simple evaluator across current and temporal views. Unsupported Datomic forms
must be listed explicitly; persisted custom behavior, HA service operation, and
lifecycle features remain unclaimed.

## Completion evidence

Completed 2026-09-02. `SUPPORTED_SURFACE.md` records the implemented grammar
and deliberate host/runtime omissions. The integrated suite passed against
PostgreSQL 15.11: 17 library tests, 13 kernel tests, 6 durability tests (one
intentional worker ignore), 5 peer/index/API tests, 5 pull tests, 9 query tests,
and 20 semantic tests. Both durability and peer suites performed real server
restarts. `cargo clippy --all-targets -- -D warnings`, formatting, and diff
whitespace checks pass.

The main architectural deviation is a typed Rust AST and owned vector
relations rather than EDN/Clojure forms and JVM-specialized relation classes.
The recovered relation, binding scheduler, set algebra, fixed-point, aggregate,
pull-frame, snapshot, and bound-index choices remain recognizable. This does
not claim streaming `qseq` or cached compiled plans, which remain explicit
omissions rather than accidental API promises.
