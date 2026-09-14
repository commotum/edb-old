# Plan: Entity API + pull/query builder

## Scope

- Entity API with lazy entity views and per-entity caching.
- Pull syntax and semantics for entity shaping and query results.
- Query builder patterns for data-driven APIs.
- Result shaping for `:find` (relation/collection/tuple/scalar) and return maps (`:keys`, `:strs`, `:syms`).

## Pull in `:find` (Datomic Semantics)

Pull grammar support:

- attribute names, reverse attrs (`:attr/_rev`), wildcard `*`.
- map specs for nested pulls `{ :attr [..] }`.
- attr options: `:as`, `:limit`, `:default`, `:xform` (xform may be deferred or limited to built-ins).
- recursion limits: number or `...` with cycle safety.

Phasing:

- Phase 1: wildcard, reverse attrs, map specs, and `:limit`/`:default`.
- Phase 2: recursion and `:xform` expansion.

Semantics:

- Default limit for cardinality-many is 1000; `:limit nil` returns all.
- Missing attrs omitted (unless `:default`).
- Wildcard pulls all attributes and recurses through component refs.
- Non-component refs default to `:db/id` only.

## Projection + Pull (M6)

- Implement result shaping per find spec and return maps.
- Two-stage pull expansion for pull expressions in `:find`.

Deliverable:
- `edb-query-project` module + integration with `edb-pull`.

## Current Core Inventory

- `CORE/edb-pull` implements pull against `current` and indexes, but no query result integration yet.
- `CORE/edb-edn` already parses pull pattern inputs and return map specs.

## Tests

- Coverage for pull expressions and return map output shapes.

## Datomic Reference Files

- REFERENCE/datomic-reference/entities.md — entity semantics, lazy access, caching behavior.
- REFERENCE/datomic-reference/query_and_pull/4_pull.md — pull syntax and semantics for entity shaping.
- REFERENCE/datomic-reference/query_and_pull/2_executing_queries.md — query execution + result shaping patterns.
- REFERENCE/datomic-reference/programming_with_data_and_edn.md — data-driven API patterns that inform a query builder.
- REFERENCE/datomic-reference/operation/tutorial/2_api.md — user-facing API shape to mirror.

## Mentat Reference Files to Reuse

- `REFERENCE/mentat/query-projector/src/translate.rs`
- `REFERENCE/mentat/query-projector/src/project.rs`
- `REFERENCE/mentat/query-projector/src/lib.rs`
- `REFERENCE/mentat/query-projector/src/projectors/mod.rs`
- `REFERENCE/mentat/query-projector/src/projectors/simple.rs`
- `REFERENCE/mentat/query-projector/src/projectors/pull_two_stage.rs`
- `REFERENCE/mentat/query-pull/src/lib.rs`
