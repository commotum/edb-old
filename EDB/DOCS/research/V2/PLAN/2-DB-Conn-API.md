# Plan: Db/Conn API + caching layer (snapshot reads, attribute caches)

## Scope

- Embedded Db/Conn API with immutable db values and snapshot semantics.
- Derived db values: as-of, since, history (db-value creation and caching; query semantics in Stage 4).
- Attribute cache and query cache integration for repeated reads.
- Support multiple db inputs bound to src-vars in query execution.

## Execution API Requirements

- `q` equivalent: execute query and return realized results.
- `qseq` equivalent: lazy/streamed results (useful for pull-heavy queries).
- Support parameterized queries with stable cache keys.
- Optional timeout handling.
- Query cache keyed by normalized EDN (avoid dynamic query churn).
- Rules input `%` and rule evaluation (subset; recursion TBD).

## Public API and Server Integration (M8)

- Expose `Conn::q` / `Db::q` for in-process execution.
- Update `/q` endpoint to accept EDN and return result shapes.
- Optional streaming API for `qseq`-style execution.

Deliverable:
- `edb-server` EDN query support.

## Current Core Inventory

- `/q` exists for JSON queries; EDN query endpoint and `Conn::q`/`Db::q` are not yet implemented.
- `/transact-edn` is live.

## Open Questions

1) Plan caching strategy (compiled query cache keyed by normalized EDN).
2) Define canonical `/q` payload shape, error format, and streaming semantics.
3) Confirm rules subset (no rules vs non-recursive vs recursive).

## Datomic Reference Files

- REFERENCE/datomic-reference/time_in_datomic.md — snapshot semantics, as-of/since/history grounding.
- REFERENCE/datomic-reference/operation/tutorial/4_read.md — how db values are used for reads in practice.
- REFERENCE/datomic-reference/operation/tutorial/6_read_revisited.md — patterns around db values and repeated reads.
- REFERENCE/datomic-reference/operation/tutorial/8_history.md — history db usage from the app perspective.
- REFERENCE/datomic-reference/indexes/2_index_model.md — why immutable indexes make caching/snapshot reads fast.
- REFERENCE/datomic-reference/best_practices.md — performance/caching guidance and read patterns.

## Mentat Reference Files to Reuse

- `REFERENCE/mentat/transaction/src/query.rs`
