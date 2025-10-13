# EDB Indexing Strategy (SQLite/Postgres)

Goals
- Preserve Datomic semantics (fast lookups, time travel) on ubiquitous stores.
- Keep write path simple; allow background merges/compaction.

Borrowed defaults from Datomic
- Accumulate‑only semantics: immutable log and index segments; changes accumulate rather than update in place.
- Live view = durable segment trees + in‑memory delta (“memory index”).
- Background jobs periodically merge the memory index into durable segment trees with a wide branching factor to keep job time sublinear in total size.
- Maintain four covering indexes (EAVT/AVET/AEVT/VAET); enable AVET for attributes with :db/index true or uniqueness to support fast value/range queries.

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

Background Indexing
- Keep a fast, bounded in‑memory delta of recent datoms for immediate visibility.
- Run periodic background merges that:
  - Flush the memory index into sorted segments
  - Merge with existing trees using a wide branching factor (≈1000) for sublinear work
  - Write segments in batches; adopt new roots atomically (conditional put)
- When merges lag, throttle writes to allow indexing to catch up.

Full‑text
- SQLite: FTS5 side tables referencing `e/a/tx`; Postgres: `tsvector` + GIN.

Uniqueness & Identities
- Unique identity attributes backed by unique indexes on `(a, v)` and a constraint that `op=true` is current.
- Enforce via tx check + retry on conflict; for P2P, resolve at merge.

Query pushdown notes
- Range predicates (=, !=, <=, <, >, >=) should be planned against AVET; ensure AVET is maintained for attributes you intend to range over.
- Prefer range predicates to custom functions for value ranges; they leverage index order and minimize scans.

Compaction/Merge
- Background job to coalesce segments and prune retracted current values.
- Maintain tombstones for history if needed (or move to history table).

Tuple/Composite Value Encoding & Indexing
- Encoding
  - Type tag `tuple`, arity byte, per-slot type tags, and per-slot canonical encodings; overall byte sequence is lexicographically sortable.
  - Example encodings: RGBA `[r g b a]` as four `uint8` bytes, geo `[lon lat]` as two doubles, range `[start end]` as two longs.
- Comparison rule
  - Lexicographic by slot: compare slot 0, then slot 1, etc.
- SQL mapping
  - Unified table: store encoded tuple in `v BLOB`; equality via bytes; ordering via byte order.
  - Typed columns (optional): expose generated columns per hot tuple attribute
    - Postgres: `GENERATED ALWAYS AS (...) STORED` with functional indexes.
    - SQLite: `VIRTUAL`/computed columns where available; otherwise views.
- Indexes
  - EAVT/AVET/AEVT/VAET remain applicable; no extra composite indexes required for MVP.
- Query pushdown
  - Optimizer can rewrite `(tuple/slot ?v i ?x)` into SQL over generated columns when present.
- Tradeoffs
  - Generated columns speed slot filters/sorts at storage cost; default to raw BLOB unless a tuple attr is hot for slot queries.
 - MVP constraint
   - Homogeneous tuples only (all slots same scalar type). This simplifies validation and optional generated columns (e.g., four `uint8` columns for RGBA) while keeping raw BLOB as the default representation.

<!-- TODO(tuple): Add a small diagram of the encoded layout and example generated column definitions. -->

Open Questions
- Value encoding: canonical format for composite ordering across types?
- History retention policy: per‑attr noHistory; archiving strategy.
- Index maintenance on multi‑writer P2P: per‑replica queues vs. centralized service.
- Tuple arity upper bound; byte layout versioning; cross‑runtime conformance.
