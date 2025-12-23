# Plan: Full Datalog Pipeline (Mentat/Datomic EDN)

Goal: Support core EDN Datalog queries (Datomic/Mentat syntax) and execute them via EDB's index stack (EAVT/AVET/AEVT/VAET), including joins, predicates, aggregates, or/not, and rules.

This plan is updated with Datomic reference requirements from:
- query grammar and clauses
- EDN data types
- index model (EAVT/AEVT/AVET/VAET)

## Scope

In scope:
- EDN query parsing and validation.
- Full Datalog planning and execution over index scans.
- Joins, predicates, aggregates, `or`/`not`, and rules.
- Executor returns raw bindings for later projection.

Out of scope (covered by other plans):
- Pull and result shaping helpers (`pull`, return maps, entity views).
- Time filters (as-of/since/history) and history-specific patterns.
- Db/Conn API, caching, and public query endpoints.
- EDN transactions (map form and `:db/add` vectors).
- Schema/vocabulary tooling beyond query needs.

## Constraints and Compatibility

- Semantics should match Datomic/Mentat where possible.
- Query planner must be Rust/WASM-friendly (no JVM/Clojure dependencies).
- Prefer index scans over SQL to align with EDB indexes and future non-SQL backends.
- Clause ordering should be optimizer-driven (do not require user ordering).
- Deviations from Datomic/Mentat must be documented and tested.

## Datomic Query Grammar Coverage (Parity Checklist)

Target full support for:

- `:find` shapes
  - relation (`:find ?a ?b`)
  - collection (`:find [?a ...]`)
  - tuple (`:find [?a ?b]`)
  - scalar (`:find ?a .`)
  - aggregates (`(count ?e)`, `(max ?v)`, etc)
- `:with` clause to control duplicate collapse (bag semantics for aggregates).
- `:in` clause
  - `$` src-var
  - default `:in $` when omitted
  - scalar/tuple/coll/rel bindings
  - rules var `%`
- `:where` clauses
  - data patterns `[src? e a v]` with blanks `_`
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
   - Run scan/join pipeline over EAVT/AVET/AEVT/VAET and return raw bindings.

## Index Planning (From Datomic Index Model)

- EAVT: entity-centric scans and `?e`-bound patterns.
- AEVT: attribute-centric scans and `has`/attribute existence.
- AVET: attribute+value scans and range predicates (`=`, `!=`, `<`, `<=`, `>`, `>=`).
- VAET: reverse ref scans for ref joins and reverse navigation.

Planning rules to align with Datomic behavior:

- Range predicates must use AVET (2-arg only).
- Missing/exists should prefer AEVT.
- Data patterns can elide trailing positions; treat omitted as implicit blanks.

## Current Core Inventory (by Plan Stage)

- M1 (EDN parsing): implemented in `CORE/edb-edn` with list/map form parsing, `:in` bindings, rules var `%`, rule expressions, `:order`, `:limit`, and decimal `M` literals. Tests in `CORE/edb-edn/tests/parse.rs`.
- M3 (algebrize): not implemented; only the minimal `/q` planner in `CORE/edb-server/src/main.rs` and a prototype EDN executor in `CORE/edb-query` (conjunctive patterns, basic predicates, `or`/`not`, no rules or where-fn bindings).
- M4 (plan compiler): not implemented; index scan APIs exist in `CORE/edb-index`.
- M5 (executor): not implemented; `/q` runs a small SQL/index-based path with a single ref-var join.
- M9 (tests/bench): parsing tests exist; no end-to-end Datalog pipeline tests yet.

## Milestones and Deliverables

### M1: EDN Query Parsing

- Parse EDN query strings into the EDB query AST (list + map forms).
- Support full `:in` binding forms (scalar/coll/tuple/rel) and rules var `%`.
- Parse rule expressions in `:where`.
- Parse `:with`, `:order` (asc/desc), and `:limit` (including `nil`).
- Tests for EDN literals (including decimal `M`) and query forms.

Deliverable:
- `edb-edn` query parser module.

### M3: Algebrize Queries

- Use Mentat algebrizer to validate and type-check.
- Support `:find`, `:with`, `:in`, `:where`, `:order`, `:limit`.
- Support rules (`%`).
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

### M9: Tests and Benchmarks

- Coverage for joins, predicates, aggregates, or/not, rules.
- Compare results vs Mentat on shared fixtures.

## Open Questions

1) Whether to support tagged bytes literals (e.g., `#bytes`/base64) beyond string literals.
2) Built-in function coverage (ground, missing?, get-else, get-some, tuple/untuple).
3) Default join strategy for EDB's data sizes.

## Immediate Next Steps

1) Add rules and where-fn bindings in `edb-query` or shift those features into the algebrizer.
2) Prototype the AVET/EAVT plan/executor around the new algebraic query layer.


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

SQL stack (if any translation is reused or referenced):
- `REFERENCE/mentat/query-sql/src/lib.rs`
- `REFERENCE/mentat/sql/src/lib.rs`
