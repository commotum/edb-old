# Plan: Full Datalog Pipeline (Mentat/Datomic EDN)

Goal: Support full EDN Datalog queries (Datomic/Mentat syntax) and execute them via EDB’s index stack (EAVT/AVET/AEVT/VAET), including joins, predicates, aggregates, or/not, rules, and pull in `:find`.

This plan is updated with Datomic reference requirements from:
- query grammar and clauses
- query execution APIs and result shapes
- pull syntax and semantics
- EDN data types
- entity semantics (for result shaping)
- index model (EAVT/AEVT/AVET/VAET)
- time filters (as-of/since/history)

## Scope

In scope:
- EDN query parsing and validation.
- Full Datalog planning and execution over index scans.
- Pull in `:find` with nested specs.
- Return map specs (`:keys`, `:strs`, `:syms`).
- Rules and rule inputs.
- Time filters (as-of/since/history) in query context.

Out of scope (covered by other plans):
- EDN transactions (map form and `:db/add` vectors).
- Schema/vocabulary tooling beyond query needs.

## Constraints and Compatibility

- Semantics should match Datomic/Mentat where possible.
- Query planner must be Rust/WASM-friendly (no JVM/Clojure dependencies).
- Prefer index scans over SQL to align with EDB indexes and future non-SQL backends.
- Deviations from Datomic/Mentat must be documented and tested.

## Datomic Query Grammar Coverage (Parity Checklist)

Target full support for:

- `:find` shapes
  - relation (`:find ?a ?b`)
  - collection (`:find [?a ...]`)
  - tuple (`:find [?a ?b]`)
  - scalar (`:find ?a .`)
  - pull expressions (`(pull ?e pattern)`)
  - aggregates (`(count ?e)`, `(max ?v)`, etc)
- `:with` clause to control duplicate collapse (bag semantics for aggregates).
- `:in` clause
  - `$` src-var
  - default `:in $` when omitted
  - scalar/tuple/coll/rel bindings
  - pattern name for pull pattern inputs
  - rules var `%`
- `:where` clauses
  - data patterns `[src? e a v tx? added?]` with blanks `_`
  - predicate expressions `[(< ?x 10)]` (range predicates, 2-arg only)
  - function expressions `[(+ ?x 1) ?y]` with binding forms
  - rule expressions `[rule-name ?x ?y]`
  - `or`, `or-join`, `and` (inside or)
  - `not`, `not-join`
- rules
  - rule head + body clauses
  - multiple rule heads (logical OR)
  - required bindings (fail if not bound)
  - rule scoping by `src-var`

- list vs map query forms
  - list form with positional sections
  - map form with explicit `:find`/`:in`/`:where` keys

## EDN Data Type Semantics (Query Literals)

EDN parsing must match Datomic expectations:

- bigint: `123N`
- bigdec: `123.45M` (requires EDN parser support)
- double/float: numeric literals (no suffix)
- instant: `#inst "..."` (RFC 3339)
- uuid: `#uuid "..."`
- symbols and keywords are first-class (vars `?x`, attrs `:ns/attr`).
- commas treated as whitespace.

Bytes policy: EDN string literals are treated as raw bytes for `ValueType::Bytes` (no base64 tag yet).

## Pipeline Stages

1) Parse EDN query
   - Parse EDN into Mentat query AST (`edn::query::FindQuery`).
   - Support list and map form query representations.
2) Algebrize
   - Bind to schema, derive types, validate inputs, find spec, with/limit/order.
3) Plan
   - Compile algebraic query into an index-scan execution plan (no SQL).
4) Execute
   - Run scan/join pipeline over EAVT/AVET/AEVT/VAET.
5) Project
   - Shape results per `:find` spec; apply pull if requested.

## Index Planning (From Datomic Index Model)

- EAVT: entity-centric scans and `?e`-bound patterns.
- AEVT: attribute-centric scans and `has`/attribute existence.
- AVET: attribute+value scans and range predicates (`=`, `!=`, `<`, `<=`, `>`, `>=`).
- VAET: reverse ref scans for ref joins and reverse navigation.

Planning rules to align with Datomic behavior:

- Range predicates must use AVET (2-arg only).
- Missing/exists should prefer AEVT.
- Data patterns can elide trailing positions; treat omitted as implicit blanks.
- Use `t` and `added` positions for history queries.

## Pull in `:find` (Datomic Semantics)

Pull grammar support:

- attribute names, reverse attrs (`:attr/_rev`), wildcard `*`.
- map specs for nested pulls `{ :attr [..] }`.
- attr options: `:as`, `:limit`, `:default`, `:xform` (xform may be deferred or limited to built-ins).
- recursion limits: number or `...` with cycle safety.

Semantics:

- Default limit for cardinality-many is 1000; `:limit nil` returns all.
- Missing attrs omitted (unless `:default`).
- Wildcard pulls all attributes and recurses through component refs.
- Non-component refs default to `:db/id` only.

## Time Filters (as-of / since / history)

- `as-of` and `since` are filters on a database value; queries should accept multiple db inputs.
- `history` exposes both assertions and retractions; data patterns use `tx` and `added` slots.
- History queries should be supported via explicit db input (e.g., `$history`) and should not produce entity views.

## Execution API Requirements

- `q` equivalent: execute query and return realized results.
- `qseq` equivalent: lazy/streamed results (useful for pull-heavy queries).
- Support parameterized queries with stable cache keys.
- Optional timeout handling.
- Clause ordering should be optimizer-driven (do not require user ordering).
- Query cache keyed by normalized EDN (avoid dynamic query churn).

## Current Core Inventory (by Plan Stage)

- M1 (EDN parsing): implemented in `CORE/edb-edn` with list/map form parsing, `:in` bindings, rules var `%`, rule expressions, return maps, pull pattern inputs, `:order`, `:limit`, and decimal `M` literals. Tests in `CORE/edb-edn/tests/parse.rs`.
- M2 (schema adapter): core schema types live in `CORE/edb-schema`; SQLite loader/alias resolution in `CORE/edb-schema/src/sqlite.rs` (feature `sqlite`) with tests in `CORE/edb-schema/tests/schema_load.rs`. Mentat-style `HasSchema` trait exists, but algebrizer-specific type inference is still missing.
- M3 (algebrize): not implemented; only the minimal `/q` planner in `CORE/edb-server/src/main.rs` and a prototype EDN conjunctive-pattern executor in `CORE/edb-query` (variables/placeholders, no rules/predicates).
- M4 (plan compiler): not implemented; index scan APIs exist in `CORE/edb-index`.
- M5 (executor): not implemented; `/q` runs a small SQL/index-based path with a single ref-var join.
- M6 (projection + pull): `CORE/edb-pull` implements pull against `current` and indexes, but no query result integration yet.
- M7 (time-aware): `CORE/edb-transactor` supports `db`/`as_of`/`since`/`history`, but query pipeline doesn’t consume those views yet.
- M8 (API/server): `/q` exists for JSON queries; EDN query endpoint and `Conn::q`/`Db::q` are not yet implemented. `/transact-edn` is live.
- M9 (tests/bench): parsing tests exist; no end-to-end Datalog pipeline tests yet.

## Milestones and Deliverables

### M1: EDN Query Parsing

- Parse EDN query strings into the EDB query AST (list + map forms).
- Support full `:in` binding forms (scalar/coll/tuple/rel), rules var `%`, and pattern-name inputs.
- Parse rule expressions in `:where`.
- Support return maps (`:keys`, `:strs`, `:syms`) and pull pattern inputs in `:find`.
- Parse `:with`, `:order` (asc/desc), and `:limit` (including `nil`).
- Tests for EDN literals (including decimal `M`) and query forms.
- Note: `:keys` uses symbols (Datomic-style), e.g. `:keys [e name]`.

Deliverable:
- `edb-edn` query parser module.

### M2: Schema Adapter for Algebrizer

- Map EDB `attrs` rows to Mentat schema/attribute structures.
- Provide `HasSchema` and type/tag metadata.
- Unit tests on minimal schema.

Deliverable:
- `edb-schema::sqlite` loader + algebrizer-facing schema adapter (name TBD).

### M3: Algebrize Queries

- Use Mentat algebrizer to validate and type-check.
- Support `:find`, `:with`, `:in`, `:where`, `:order`, `:limit`.
- Support rules (`%`) and pattern inputs.
 - Validate required bindings for not/or rules and `or-join`/`not-join`.
 - Respect `:limit` variable binding (when provided in `:in`).

Deliverable:
- `edb-query-algebrize` wrapper returning algebraic queries.

### M4: Index Plan Compiler

- Compile algebraic queries into index scan plans.
- Implement plan nodes for or/or-join/not/not-join/ground.
- Enforce AVET usage for range predicates.

Deliverable:
- `edb-query-plan` module producing executable plan trees.

### M5: Query Executor

- Execute plan nodes over EAVT/AVET/AEVT/VAET.
- Join strategies (hash or nested-loop) with selectivity heuristics.
- Apply `:with` bag semantics and aggregates.
- Add ordering, distinct, and limit.

Deliverable:
- `edb-query-exec` module returning raw bindings.

### M6: Projection + Pull

- Implement result shaping per find spec and return maps.
- Two-stage pull expansion for pull expressions in `:find`.

Deliverable:
- `edb-query-project` module + integration with `edb-pull`.

### M7: Time-Aware Queries

- Wire `as-of`, `since`, and `history` db inputs to filters over indexes.
- Expose `t`/`added` in data pattern matching.
- Add query forms that join `:db/txInstant` via log if needed.

Deliverable:
- Query context and filtered db support.

### M8: Public API and Server Integration

- Expose `Conn::q` / `Db::q` for in-process execution.
- Update `/q` endpoint to accept EDN and return result shapes.
- Optional streaming API for `qseq`-style execution.

Deliverable:
- `edb-server` EDN query support.

### M9: Tests and Benchmarks

- Coverage for joins, predicates, aggregates, or/not, rules, and pull.
- Time filter queries (as-of/since/history) with `t/added`.
- Compare results vs Mentat on shared fixtures.

## Open Questions

1) Whether to support tagged bytes literals (e.g., `#bytes`/base64) beyond string literals.
2) Built-in function coverage (ground, missing?, get-else, get-some, tuple/untuple, tx-ids/tx-data).
3) Default join strategy for EDB’s data sizes.
4) Plan caching strategy (compiled query cache keyed by normalized EDN).

## Immediate Next Steps

1) Extend the schema adapter with algebrizer-facing type inference (value type sets, component attr flags).
2) Add query features beyond data-pattern conjunctions (predicates, or/not, rules) in `edb-query` or the forthcoming algebrizer.
3) Prototype the AVET/EAVT plan/executor around the new algebraic query layer.


## Mentat Reference Files to Reuse

These files are the most relevant sources to copy or adapt for a Mentat-style pipeline. The list is derived from `DOCS/research/STAGE-2/REF/Mentat-Map.md`.

EDN parsing and query AST:
- `REFERENCE/mentat/edn/src/edn.rustpeg`
- `REFERENCE/mentat/edn/src/lib.rs`
- `REFERENCE/mentat/edn/src/query.rs`
- `REFERENCE/mentat/edn/src/types.rs`
- `REFERENCE/mentat/edn/src/entities.rs`

Algebrizer and clause processing:
- `REFERENCE/mentat/query-algebrizer/src/lib.rs`
- `REFERENCE/mentat/query-algebrizer/src/types.rs`
- `REFERENCE/mentat/query-algebrizer/src/validate.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/mod.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/pattern.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/inputs.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/or.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/not.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/predicate.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/ground.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/fulltext.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/tx_log_api.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/where_fn.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/resolve.rs`
- `REFERENCE/mentat/query-algebrizer/src/clauses/convert.rs`

Projection and pull integration:
- `REFERENCE/mentat/query-projector/src/translate.rs`
- `REFERENCE/mentat/query-projector/src/project.rs`
- `REFERENCE/mentat/query-projector/src/lib.rs`
- `REFERENCE/mentat/query-projector/src/projectors/mod.rs`
- `REFERENCE/mentat/query-projector/src/projectors/simple.rs`
- `REFERENCE/mentat/query-projector/src/projectors/pull_two_stage.rs`
- `REFERENCE/mentat/query-pull/src/lib.rs`

SQL stack (if any translation is reused or referenced):
- `REFERENCE/mentat/query-sql/src/lib.rs`
- `REFERENCE/mentat/sql/src/lib.rs`

Schema and type system:
- `REFERENCE/mentat/core/src/lib.rs`
- `REFERENCE/mentat/core/src/sql_types.rs`
- `REFERENCE/mentat/core-traits/lib.rs`
- `REFERENCE/mentat/core-traits/value_type_set.rs`
- `REFERENCE/mentat/core-traits/values.rs`

Execution wrappers (query APIs and result shaping):
- `REFERENCE/mentat/transaction/src/query.rs`
