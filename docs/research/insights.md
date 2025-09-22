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

Evolve (EDB deltas)
- Runtimes: WASM/wasi transactor; polyglot peers (JS/Swift/Rust/etc.)
- Storage: SQLite (local), Postgres (server), option for KV (RocksDB/FDB)
- Transport: HTTP/gRPC; optional P2P transports
- Schema UX: schema‑as‑data with safe defaults and guided growth
- Built‑in P2P sync: signed log replication; offline‑first
- Free‑text: FTS (SQLite FTS5 / Postgres tsvector)
- Serialization: EDN/JSON for API; compact internal encoding for values

Early Decisions (Draft)
- Single‑writer vs. multi‑writer: aim for multi‑writer via signed logs; resolve conflicts at index‑build or read via constraints
- Uniqueness: enforce via per‑attr unique indexes + retry on conflict
- Tx functions: sandboxed WASM; deterministic only
- Identity: Ed25519 keys per device; signed tx envelope
- Values: typed encoding with small, sortable representation (for composite indexes)

Open Questions
- Global t: per‑peer lamport/hybrid time or server‑assigned? merge rules?
- Partitioning: per‑partition writers and merge windows?
- Large values and blobs: external store + content‑addressed refs?
- Access control: per‑attribute/namespace ACLs in a P2P model?
- Index compaction: background merge strategy on SQLite/Postgres?

Next Actions
- Prototype signed tx envelope and local append‑only log
- Explore SQLite schema for EAVT/AVET with covering indexes
- Define minimal Datalog subset and Pull MVP
- Spike P2P sync over HTTP + local discovery
