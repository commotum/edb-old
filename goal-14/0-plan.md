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

**Status:** Pending.

**Outcome:** A concrete owned `DatabaseValue` represents eager or native
snapshots together with composable as-of, since, history, and filter state.

**Focus:** Source-backed `Db`/`windowed` contract; exact basis and schema/ident
access; prefix-first temporal filtering; retraction collapse; lookup refs;
history restrictions; red fixtures proving every current-source leak.

**Completion signal:** Eager fixtures show patterns, values, filters, lookup
refs, and nested filter reads all observe the same composed value, with no
full-index rebuild required for a selective prefix.

### 2. Uniform query-source propagation

**Status:** Pending.

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

**Status:** Pending.

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

**Status:** Pending.

**Outcome:** Existing query functions and aggregates preserve documented
constant/binding, nil, cardinality, identity, and bag semantics.

**Focus:** `get-some` numeric attribute ids and cardinality-one validation;
`ground` constant shape binding; query-only nil in tuple/untuple; `min/max n`
duplicate retention; stable incorrect/anomaly boundaries.

**Completion signal:** Direct source-witness fixtures and generated bag/value
cases distinguish every repaired behavior without adding nil to stored datoms.

### 5. Invocation-scoped rule evaluation

**Status:** Pending.

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

**Status:** Pending.

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
