# Plan: History-aware querying

## Scope

- History db queries (t/added + history_db() queryable).
- Query semantics for as-of/since/history db values.
- Data patterns that include `tx` and `added` slots.

## Time Filters (as-of / since / history)

- `as-of` and `since` are filters on a database value; queries can accept multiple db inputs.
- `history` exposes both assertions and retractions; data patterns use `tx` and `added` slots.
- History queries should be supported via explicit db input (e.g., `$history`) and should not produce entity views.

## Planning Notes

- Use `t` and `added` positions for history queries.

## Time-Aware Queries (M7)

- Wire `as-of`, `since`, and `history` db inputs to filters over indexes.
- Expose `t`/`added` in data pattern matching.
- Add query forms that join `:db/txInstant` via log if needed.

Deliverable:
- Query context and filtered db support.

## Current Core Inventory

- `CORE/edb-transactor` supports `db`/`as_of`/`since`/`history`, but query pipeline does not consume those views yet.

## Tests

- Time filter queries (as-of/since/history) with `t`/`added`.

## Open Questions

1) Built-in history function coverage (tx-ids/tx-data).

## Datomic Reference Files

- REFERENCE/datomic-reference/time_in_datomic.md — canonical meaning of t/as-of/since/history.
- REFERENCE/datomic-reference/operation/tutorial/8_history.md — how history db is queried by users.
- REFERENCE/datomic-reference/transactions/2_transaction_model.md — tx metadata model.
- REFERENCE/datomic-reference/transactions/3_transaction_data.md — t/added semantics and tx entities.
- REFERENCE/datomic-reference/query_and_pull/3_query.md — how to express history queries over datoms.
