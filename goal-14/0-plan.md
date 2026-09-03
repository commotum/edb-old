# Goal 14 — Exact Database-Value Query, Pull, and Entity Semantics

## Objective

Make every query, pull, lookup-ref, extension, and entity operation observe one
exact immutable database value, then correct the already-claimed Datalog and
pull surface to match `datomic_pro_docs` and the sound recovered behavior of
Datomic Pro 1.0.7705. The same semantic access path must work over the eager
`Database` oracle and Goal 13's lazy `PeerSnapshot` without materializing the
whole database.

## Constraints

- `datomic_pro_docs` is the semantic authority. Recovered `Db`, `windowed`,
  query scheduling/rules, extensions, aggregation, pull, and entity paths in
  `1.0.7705` are the default implementation blueprint.
- Rust throughout and PostgreSQL only. A concrete eager/native database-value
  enum is acceptable; do not invent a generic storage-backend framework.
- A database value owns its basis, as-of/since/history/filter state, schema and
  ident interpretation, and index access. No operation may silently fall back
  to the current bare database or live connection.
- Current schema remains operative for time views, while datom visibility is
  governed by the exact value. Filters apply before retraction collapse and
  receive the same temporal/history value with only custom filtering removed.
- Keep `Database::with` pure and preserve the eager implementation as the
  independent semantic oracle. Native peer execution must use lazy tree/recent
  cursors and keep compatibility-materialization counters at zero.
- Query and pull results are unordered unless the API explicitly says
  otherwise. Resource controls bound work; they must not become artificial
  semantic limits such as a fixed rule-iteration count.
- Do not pull Goal 15 lifecycle repair or Goal 16 writer conversion into this
  child. Leave their current red witnesses visible.

## Known context

- `QuerySource { &Database, View }` can express only four simple views. Only
  data patterns honor that `View`; `missing?`, `get-else`, `get-some`, lookup
  refs, extensions, entity/pull, and find-pull use the bare current `Database`.
- `DatabaseView` can compose filters and time bounds but cannot be supplied as
  a query source, passes the wrong value to filter predicates, and scans/rebuilds
  globally. `Peer::{query,pull,entity}` forces the eager compatibility oracle.
- `get-some` returns a keyword rather than numeric attribute id and fails to
  require cardinality one. `ground` has no constant/binding-shape semantics;
  `untuple` turns nil into an empty string; `min/max n` deduplicate bag values.
- Rules are globally precomputed from an empty row, ignore invocation source
  and required bindings, and impose a semantic 128-round ceiling.
- Pull lacks explicit source selection, arbitrary alias keys, duplicate
  selector validation, correct unknown-attribute/empty-nested behavior, and
  selector-local recursion. `Entity` retains only an eager current database.
- Goal 13 supplies immutable native snapshots, resident schema/idents, lazy
  prefix/range cursors, bounded caches, and exact old-snapshot behavior.

## Stages

### 1. One exact immutable database value

**Status:** Complete 2026-09-03. `src/database_value.rs` now owns eager/native
snapshot, basis, window, schema/ident, lookup, and prefix-read state. The eight
`database_value` witnesses cover composed time/filter values, filter-before-
collapse, exact stored retractions, lookup refs, point guards, and prefix/full
equivalence.

**Outcome:** A concrete owned `DatabaseValue` represents eager or native
snapshots together with composable as-of, since, history, and filter state.

**Focus:** Source-backed `Db`/`windowed` contract; exact basis and schema/ident
access; prefix-first temporal filtering; retraction collapse; lookup refs;
history restrictions; red fixtures proving every current-source leak.

**Completion signal:** Eager fixtures show patterns, values, filters, lookup
refs, and nested filter reads all observe the same composed value, with no
full-index rebuild required for a selective prefix.

### 2. Uniform query-source propagation

**Status:** Complete 2026-09-03. `QuerySource` owns a `DatabaseValue`; every
database-consuming clause, rule body, extension, and find-pull follows its
selected source. Static validation requires only actual consumers, including
empty-result pull, while pure clauses and pure rules need no phantom `$`.
Tuple lookup refs resolve through the exact selected value.

**Outcome:** Query planning, clauses, expressions, extensions, aggregates, and
source-qualified find-pull carry the selected `DatabaseValue` end to end.

**Focus:** Remove the magic mandatory `$`; validate only referenced sources;
propagate exact values through `missing?`, `get-else`, `get-some`, local and
persisted extensions, lookup resolution, result shaping, and explicit pull
sources.

**Completion signal:** Multi-source/as-of/since/filter/history fixtures cannot
observe future/current facts through any non-pattern path, and a query using
only a named source succeeds.

### 3. Pull and lazy entity conformance

**Status:** Complete 2026-09-03. Entity and pull retain the exact value and
support eid/ident/lookup identity, reverse/component navigation, arbitrary
aliases, defaults, limits, local recursion, and history guards. Unresolved
ident/lookup pulls preserve a requested `:db/id nil`; many/reverse entity
navigation is logically set-valued even when a filter exposes repeated stored
representations.

**Outcome:** Pull and entity navigation are lazy associative views over their
captured exact database value and match documented selector semantics.

**Focus:** Explicit `:db/id`; eid/ident/lookup identifiers; ref-valued entity
navigation and reverse attributes; arbitrary alias values; duplicate selector
rejection; positive limits/depth; unknown attributes and nested-empty results;
selector-local recursion/cycle behavior; reject point entity/pull on history.

**Completion signal:** Independent fixtures cover selector edge cases and old
entities remain pinned to their exact database value while the live peer
advances.

### 4. Correct claimed functions and aggregate bags

**Status:** Complete 2026-09-03. `get-some`, `ground`, tuple/untuple nil, and
bounded min/max bag behavior have direct fixtures. Documentation authority
wins over the recovered variadic implementation for zero-argument `tuple`,
which is now rejected because the query reference requires one or more inputs.

**Outcome:** Existing query functions and aggregates preserve documented
constant/binding, nil, cardinality, identity, and bag semantics.

**Focus:** `get-some` numeric attribute ids and cardinality-one validation;
`ground` constant shape binding; query-only nil in tuple/untuple; `min/max n`
duplicate retention; stable incorrect/anomaly boundaries.

**Completion signal:** Direct source-witness fixtures and generated bag/value
cases distinguish every repaired behavior without adding nil to stored datoms.

### 5. Invocation-scoped rule evaluation

**Status:** Complete 2026-09-03. Invocation/source/binding-keyed memo relations
replace global precomputation and converge until no relation grows. Required,
recursive, mutually recursive, multi-source, greater-than-128-depth, and
explicit resource-limit fixtures pass without a semantic round cap.

**Outcome:** Rules honor invocation source and required bindings and converge
to the real finite fixed point under ordinary resource accounting.

**Focus:** Recovered adornment/required-binding scheduling; source-keyed memo
relations; recursive and mutually recursive rules; deduplication; termination
by no-new-tuples; cancellation/work/row limits separate from semantics.

**Completion signal:** Bound and multi-source recursive fixtures agree with an
independent reference evaluator, recursion beyond 128 derivation depths can
succeed when within resource limits, and unbounded/infinite work fails only at
an explicit resource control.

### 6. Native peer execution and differential closure

**Status:** Complete 2026-09-03. Peer, snapshot, program-query, pull, and entity
entry points use native values directly. Find-pull shares the enclosing query's
absolute deadline, cancellation flag, and work budget. A deterministic
PostgreSQL differential compares eager/native and optimized/force-scan results
over selective queries, a three-clause join, composed temporal filtering, and
raw history.

**Outcome:** Query, pull, and entity APIs execute directly over Goal 13 native
snapshots and agree with the eager oracle at the identical database value.

**Focus:** Route peer APIs through `DatabaseValue`; lazy selective access;
old-snapshot isolation; cache/cursor metrics; generated optimized-vs-reference
and eager-vs-native differential cases; source/deviation ledger fold-back.

**Completion signal:** Real PostgreSQL witnesses show identical results across
eager and native values, zero compatibility materializations, bounded path
reads for selective work, exact old snapshots after live advancement, and all
format/Clippy/pure/focused integration gates pass.

## Exit condition

Goal 14 completes only when all supported query, pull, lookup, extension, and
entity behavior consumes one exact immutable database value; known primitive,
aggregate, selector, and rule defects are repaired with source-backed fixtures;
and native peer execution is lazy, bounded, and differentially equal to the
eager reference model.

**Status:** Achieved 2026-09-03.

## Source and verification closure

- Exact values follow recovered `datomic.db.Db` state and `windowed`
  (`1.0.7705/peer/src-clj/datomic/db.clj:4719-4744,1810-1831`) and the docs'
  immutable/filter contracts
  (`datomic_pro_docs/02_core_concepts/00_datomic_data_model.md:16` and
  `02_core_concepts/02_database_filters.md`).
- Query source propagation and helpers follow recovered `pull-fv`, `dbrel`,
  `resolve-id`, and extensions
  (`query.clj:1354-1394`, `datalog.clj:558-585`, `db.clj:1197-1218`,
  `extensions.clj:131-215`). Rules follow `sched-in-order`, `eval-rule`, and
  `qsqr` (`datalog.clj:1766-1895,2974-3041,3245-3265`). Aggregation bag
  behavior follows `aggregation.clj:28-59`.
- Pull/entity edge behavior follows `pull.clj:651-747` and the documented
  entity-identifier, set-valued navigation, selector, and timeout contracts in
  `05_query_and_pull/` and `02_core_concepts/03_entities.md`.
- `cargo fmt --check`, all-target warning-denying Clippy, the full pure suite,
  and all focused semantic suites pass. Against a live PostgreSQL 15.11
  fixture, all 18 `postgres_peer` tests pass serially, including a real server
  restart, exact old snapshots, bounded lazy reads, zero compatibility
  materializations, and the generated four-way differential. The persisted
  extension PostgreSQL witness also passes.

Deferred APIs remain explicit rather than silently claimed: fulltext, nested
`q`, random aggregates, pull transforms, `qseq`, and JVM/EDN coercion are not
part of Goal 14's implemented surface. Goal 17 must classify them as
non-core omissions or reopen the owning semantic milestone; they cannot be
used as evidence for functionality that does not exist.
