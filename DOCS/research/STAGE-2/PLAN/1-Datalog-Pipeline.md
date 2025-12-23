# Plan: Full Datalog Pipeline (Mentat-style)

Goal: Support full EDN Datalog queries (Mentat/Datomic syntax) and execute them via EDB’s index stack (EAVT/AVET/AEVT/VAET), including joins, predicates, aggregates, or/not, and pull in `:find`.

## Scope

In scope:
- EDN query parsing and validation.
- Full Datalog planning and execution over index scans.
- Pull in `:find` with nested specs.
- Query result shaping (scalar/tuple/coll/rel).

Out of scope (covered by other plans):
- EDN transactions (map form and `:db/add` vectors).
- Schema/vocabulary tooling beyond what is needed for querying.

## Constraints and Compatibility

- Semantics should match Mentat/Datomic where possible.
- The query planner must be Rust/WASM-friendly (no non-portable dependencies).
- Query execution should prefer index scans over raw SQL to stay aligned with EDB’s indexes and future non-SQL backends.
- Deviations from Datomic/Mentat must be explicitly documented.

## Target Pipeline Stages

1) Parse EDN query
   - Parse EDN into Mentat query AST (`edn::query::FindQuery`).
2) Algebrize
   - Bind to schema, derive types, validate inputs and `:find`, produce an algebraic query.
3) Plan
   - Compile algebraic query into an index-scan execution plan (no SQL).
4) Execute
   - Run scan/join pipeline over EAVT/AVET/AEVT/VAET.
5) Project
   - Shape results per `:find` spec; apply pull if requested.

## Architecture Approach

### Parsing and Algebrization

- Reuse Mentat’s EDN parser and query AST (`REFERENCE/mentat/edn` and `REFERENCE/mentat/query-algebrizer`) to preserve semantics and validation rules.
- Build an EDB schema adapter that feeds Mentat algebrizer the EDB schema from `attrs` (value types, cardinality, uniqueness, component, etc).

### Index-Plan Compiler (EDB-specific)

Replace Mentat’s SQL translator with an EDB index-plan compiler that targets:

- EAVT for entity-first scans, and `:e` + `:a` constraints.
- AVET for value constraints and range scans.
- AEVT for `has` (attribute existence) scans.
- VAET for reverse ref lookups or ref-joins.

Plan nodes (initial set):
- `ScanEavt`, `ScanAvet`, `ScanAevt`, `ScanVaet`.
- `Join` (hash/nested-loop depending on selectivity).
- `Filter` (predicates, type constraints).
- `Or`, `Not`, `NotJoin` (subplans).
- `Aggregate` (count, min, max, avg, sum).
- `Project` (find spec, distinct, order, limit).

### Execution Model

- Scan nodes return bindings keyed by variables.
- Joins unify variables; predicate evaluation happens after unification.
- Range predicates use ordered bytes from `edb-encoding` via AVET.
- Pull in `:find` triggers a second-stage fetch from `edb-pull`.

## Milestones and Deliverables

### M1: EDN Query Parsing

- Integrate Mentat EDN parser.
- Parse EDN query strings into Mentat AST.
- Tests for query parsing and EDN literals (`#inst`, `#uuid`, bigint).

Deliverables:
- `edb-edn` crate or module for parsing queries.

### M2: Schema Adapter for Algebrizer

- Map EDB `attrs` rows to Mentat schema and attribute structures.
- Implement `HasSchema` and required type metadata.
- Unit tests against a minimal schema.

Deliverables:
- `edb-mentat-schema` adapter layer (name TBD).

### M3: Algebrize Queries

- Use Mentat algebrizer to produce algebraic queries.
- Validate `:find`, `:in`, `:where`, `:order`, `:limit`, and typing rules.
- Capture algebrized form for planner input.

Deliverables:
- `edb-query-algebrize` wrapper that returns algebraic queries.

### M4: Index Plan Compiler

- Compile algebraic queries into index scan plans.
- Choose access paths based on bound variables and constraints.
- Support `or`, `not`, `not-join`, and `ground` forms.

Deliverables:
- `edb-query-plan` module producing an executable plan tree.

### M5: Query Executor

- Execute plan nodes over EAVT/AVET/AEVT/VAET.
- Implement join strategies and predicate evaluation.
- Add ordering, distinct, and limit.

Deliverables:
- `edb-query-exec` module returning raw bindings.

### M6: Projection + Pull

- Implement result shaping per `:find` spec.
- Add two-stage pull expansion for `pull` in `:find`.

Deliverables:
- `edb-query-project` module + integration with `edb-pull`.

### M7: Public API and Server Integration

- Expose `Conn::q` or similar API for in-process query execution.
- Update `/q` endpoint to accept EDN and return result shapes.

Deliverables:
- `edb-server` EDN query support.
- `edb-conn` API surface (if added in a later plan).

### M8: Testing and Benchmarks

- Coverage for joins, predicates, aggregates, or/not, pull in `:find`.
- Compare results with Mentat on shared fixtures where possible.
- Add microbenchmarks for scan selection and join strategies.

## Open Questions

1) Decimal/bytes EDN representation for query literals (tagged vs string).
2) Extent of Mentat algebrizer reuse vs custom EDB algebrizer.
3) Which join strategy is the default (hash vs nested loop) for EDB’s data sizes.
4) How to represent and query history (`t`/`added`) in Datalog (history db).

## Immediate Next Steps

1) Confirm EDN literal policy for decimal/bytes in queries.
2) Stand up a minimal EDN query parser + schema adapter prototype.
3) Build a tiny index-plan prototype for `[:find ?e :where [?e :a 1]]` and verify scan selection.

