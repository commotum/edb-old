# EDB Indexing Strategy (SQLite/Postgres)

Goals
- Preserve Datomic semantics (fast lookups, time travel) on ubiquitous stores.
- Keep write path simple; allow background merges/compaction.

Schema Options
1) Unified datoms table
- Table: `datoms(e BIGINT, a BIGINT, v BLOB/TEXT/NUMERIC, tx BIGINT, op BOOLEAN)`
- Composite indexes:
  - `CREATE INDEX eavt ON datoms(e, a, v, tx);`
  - `CREATE INDEX avet ON datoms(a, v, e, tx);`
  - `CREATE INDEX aevt ON datoms(a, e, v, tx);`
  - `CREATE INDEX vaet ON datoms(v, a, e, tx);`
- Pros: simple; mirrors Datomic. Cons: value typing/ordering needs compact encoding.

2) Typed value columns
- Table: `datoms(e, a, tx, op, v_type, v_long, v_double, v_text, v_ref, v_uuid, ...)`
- Partial indexes per type to keep trees small; encode ordering per type.
- Pros: efficient comparisons; reduces value coercions. Cons: wider rows.

3) Partitioned tables (optional)
- Per‑partition datoms tables to parallelize writes/compaction.
- Pros: concurrent writers; Cons: cross‑partition queries need UNION/VIEW.

Time Travel
- `as-of`, `since`, `history` supported by filtering on `tx` and `op`.
- Snapshot acceleration: periodic checkpoints (materialized views) to skip long scans.

Full‑text
- SQLite: FTS5 side tables referencing `e/a/tx`; Postgres: `tsvector` + GIN.

Uniqueness & Identities
- Unique identity attributes backed by unique indexes on `(a, v)` and a constraint that `op=true` is current.
- Enforce via tx check + retry on conflict; for P2P, resolve at merge.

Compaction/Merge
- Background job to coalesce segments and prune retracted current values.
- Maintain tombstones for history if needed (or move to history table).

Open Questions
- Value encoding: canonical format for composite ordering across types?
- History retention policy: per‑attr noHistory; archiving strategy.
- Index maintenance on multi‑writer P2P: per‑replica queues vs. centralized service.
