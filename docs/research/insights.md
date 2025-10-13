# EDB Insights & Invariants (Living Doc)

North Star
- What if creating a database were as easy as creating a struct or class — and Datomic’s time‑traveling, replayable model ran everywhere on everyday defaults (WASM, SQLite/Postgres, HTTP/gRPC), with first‑class peer‑to‑peer sync and effortless, evolvable schemas for arbitrary relations and consumers?

Keep (from Datomic)
- Append‑only tx log with total order (t) and replay
- Immutable datoms ⟨E A V Tx Op⟩ and entity views
- As‑of / since / history time travel
- Attribute schema: value type, cardinality, uniqueness, noHistory
- Datalog + Pull, peer caches, read scalability via immutability
- Background indexing and durable segments (EAVT/AVET/AEVT/VAET)
 - Batched index writes and metrics; segment streaming to peers
 - Tx reports to peers; tempid resolution; catalog of attributes

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
