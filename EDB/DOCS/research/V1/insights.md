# EDB Insights & Invariants (Living Doc)

North Star
- What if creating a database were as easy as creating a struct or class — and Datomic’s time‑traveling, replayable model ran everywhere on everyday defaults (WASM, SQLite/Postgres, HTTP/gRPC), with first‑class peer‑to‑peer sync and effortless, evolvable schemas for arbitrary relations and consumers?

Keep (from Datomic)
- Append-only tx log with total order (t) and replay
- Immutable datoms ⟨E A V Tx Op⟩ and entity views
- As‑of / since / history time travel
- Attribute schema: value type, cardinality, uniqueness, noHistory
- Datalog + Pull, peer caches, read scalability via immutability
- Background indexing and durable segments (EAVT/AVET/AEVT/VAET)
 - Batched index writes and metrics; segment streaming to peers
 - Tx reports to peers; tempid resolution; catalog of attributes

Borrow (defaults) from Datomic
- Schema growth-only
  - Never remove or reuse names; introduce aliases and deprecations instead.
  - Identity spectrum: entity ids; idents for schema/enums; unique identity attributes for domain keys; lookup refs for upsert/readability; use squuids/UUIDv7 for time‑ordered locality.
  - Components: model owned sub-entities with :db/isComponent; cascade retract; allow “touch” to eagerly materialize component trees in caches.
  - Validation without rigid SQL constraints: attribute predicates, entity predicates, and entity specs enforced via :db/ensure.
- Transactions, time, and ACID
  - Single writer (transactor); tx is union of primitives returned by tx functions — no read/modify/write updates.
  - Strong serializable writes via conditional put of roots; per‑peer monotonic operations; cross‑peer reads are serializable. Provide sync(t) to coordinate read‑your‑writes across processes.
  - Prefer t over txInstant for precision; history view includes retractions; as‑of/since for point‑in‑time queries.
  - Transaction hints: peers can compute :hints (via with‑like API) to reduce latency without changing semantics.
- Indexing strategy
  - Accumulate‑only semantics with immutable log and index trees.
  - Memory index + background jobs that merge to durable segment trees (wide branching factor ⇒ sublinear job times).
  - Maintain four covering indexes (EAVT/AEVT/AVET/VAET); enable AVET for attrs marked :db/index or :db/unique.
- Query and Pull ergonomics
  - Datalog grammar features: :with; :find return maps; built‑in range predicates (=, !=, <=, <, >, >=) that push down to AVET.
  - Built‑ins: get‑else, get‑some, ground, missing?, tuple/untuple; query caching; parameterization; clause ordering; timeouts; qseq for lazy streaming; rules for reusable logic.
  - Pull patterns: forward/reverse attributes, :as/:default/:limit/:xform options, wildcards, recursion limits, component defaults (maps) vs non‑components (ids). “Outer join” patterns via Pull or get‑else.
- Partitions, synchronization, and ops
  - Optional partitions (named/implicit) for locality and “new entity scans”; support partition assignment for tempids.
  - Synchronization via sync(t), not eventual gossip alone.
  - Clear type limits: bytes cannot be unique / used for lookup refs; NaN cannot participate in upsert; reverse lookup naming caveat (leading underscore on attribute name portion prevents reverse lookup).

Evolve (EDB deltas)
- Runtimes: WASM/wasi transactor; polyglot peers (JS/Swift/Rust/etc.)
- Storage: SQLite (local), Postgres (server), option for KV (RocksDB/FDB)
- Transport: HTTP/gRPC; optional P2P transports
- Schema UX: schema‑as‑data with safe defaults and guided growth
- Built‑in P2P sync: signed log replication; offline‑first
- Free‑text: FTS (SQLite FTS5 / Postgres tsvector)
- Serialization: EDN/JSON for API; compact internal encoding for values
- Values: add tuple/composite as first‑class typed values with small, sortable canonical encoding; include `:db.type/uint8` (0..255, 1 byte) for RGBA and similar
 - Tuples (MVP): restrict to homogeneous slot type with schema sugar `:db/tupleElemType` + `:db/tupleArity` (normalized to `:db/tupleTypes`); per-slot labels optional for Pull rendering
- DevEx: Pull renders labeled maps for tuples if `:db/tupleLabels` exist; vectors otherwise
- Modeling guidance: prefer component entities for unbounded/nested structures; use tuples for small, fixed‑arity records (RGBA, geo points, ranges, quaternions)
- Safety: growth‑only — introduce new tuple attributes rather than mutating arity/types

Philosophy
- Evolve by adding sensible value types and structs (e.g., `uint8` for color channels, tuples for RGBA) rather than forcing attributes into ill‑fitting existing types.

Early Decisions (Draft)
- Single‑writer vs. multi‑writer: aim for multi‑writer via signed logs; resolve conflicts at index‑build or read via constraints
- Uniqueness: enforce via per‑attr unique indexes + retry on conflict
- Tx functions: sandboxed WASM; deterministic only
- Identity: Ed25519 keys per device; signed tx envelope
- Values: typed encoding with small, sortable representation (for composite indexes)
 - Indexing: EAVT/AVET/AEVT/VAET via covering indexes; background merge workers per store
 - DevEx: CLI + FFI; schema‑as‑data with guided defaults; Pull and Query as first‑class
 - Error mapping: SQL adapter maps unique violations and constraint errors coherently
 - Avoid Fressian; prefer CBOR/JSON for envelopes; binary/columnar for value storage

Open Questions
- Global t: per‑peer lamport/hybrid time or server‑assigned? merge rules?
- Partitioning: per‑partition writers and merge windows?
- Large values and blobs: external store + content‑addressed refs?
- Access control: per‑attribute/namespace ACLs in a P2P model?
- Index compaction: background merge strategy on SQLite/Postgres?
- How to expose tx functions safely across WASM runtimes (deterministic footprint)?
- Pull recursion limits and performance on SQLite/Postgres schemas?
- Tuple arity cap; Datalog accessor syntax; cost/benefit of SQL pushdown for tuple slots
 - When to lift homogeneous-only to heterogeneous tuples; encoding/planner impacts and migration/feature-gating

Next Actions
- Prototype signed tx envelope and local append‑only log
- Explore SQLite schema for EAVT/AVET with covering indexes
- Define minimal Datalog subset and Pull MVP
- Spike P2P sync over HTTP + local discovery
