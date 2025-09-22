# EDB Requirements (Draft)

North Star
- What if creating a database were as easy as creating a struct or class — and Datomic’s time‑traveling, replayable model ran everywhere on everyday defaults (WASM, SQLite/Postgres, HTTP/gRPC), with first‑class peer‑to‑peer sync and effortless, evolvable schemas for arbitrary relations and consumers?

## Scope
- Provide a time‑traveling, append‑only, relational store with Datalog + Pull, on ubiquitous runtimes and storage (WASM, SQLite, Postgres).
- Support offline‑first operation and first‑class peer‑to‑peer sync.
- Make schema definition and evolution safe and ergonomic (growth‑only by default).

Non‑Goals (MVP)
- Fine‑grained authorization/ACLs (beyond DB‑level access).
- Automatic sharding/partition rebalancing across servers.
- Custom pluggable query languages beyond Datalog subset.

## Terminology & Model
- Datom: ⟨E, A, V, Tx, Op⟩ where Op ∈ {add, retract}.
- Entity: associative view over datoms sharing E at a point in time.
- Attribute schema: value type, cardinality, uniqueness, isComponent, noHistory, doc, ident, alias.
- Transaction: declarative set of datoms accrued atomically; produces a new db value and t.
- Time travel: as‑of, since, history; queries run against a db value.

## Functional Requirements

Schema & Catalog
- Define attributes as data: ident, doc, valueType, cardinality, uniqueness (identity/value), isComponent, noHistory, indexed, fulltext.
- Support aliases and deprecation metadata; never remove or reuse names (growth‑only).
- Provide a catalog API to list attributes, schema versions, and annotations.

Transactions & Log
- Append‑only transaction log; each tx envelope includes: author key, tx‑instant, parents (hashes), and body (adds/retracts), signed (Ed25519).
- Tempid resolution and lookup refs; deterministic validation (types, cardinality, uniqueness, refs, noHistory).
- Tx functions: supported via sandboxed WASM; deterministic and side‑effect‑free.
- Tx‑report API: tempid→entid map, t, tx entity id, touched attrs, stats; subscribers can consume.

Time Travel
- Expose `as‑of`, `since`, and `history` views; entities are point‑in‑time; history includes retractions.

Query (Datalog Subset)
- Parse → algebrize → plan to SQL for SQLite/Postgres.
- Minimum: find (scalar, tuple, rel, coll), `:in` inputs, predicates, basic aggregates, order/limit, simple rules (optional post‑MVP).
- Result shapes consistent with Datomic; parameter binding; explain plans.

Pull
- Support pull patterns (forward/reverse, nesting, wildcard, recursion with limits, :as/:default/:limit/:xform options).
- Normalize and cache patterns; efficient join strategy for nested pulls; usable standalone and in find specs.

Indexing
- Maintain EAVT/AVET/AEVT/VAET via covering indexes; typed ordering across values.
- Background merge/compaction; snapshots/checkpoints to accelerate reads.
- Full‑text support: SQLite FTS5; Postgres tsvector + GIN.
- Uniqueness enforcement on `(a, v)` for identity/value uniques; idempotent upserts via lookup refs.

Storage Backends
- SQLite (local) and Postgres (server) as first‑class targets; optional KV (RocksDB/FDB) follow‑up.
- CAS semantics for segment persistence; DDL/migration management; constraints and error mapping (e.g., SQLState 23 for unique violations).

Serialization & Transport
- External API payloads in EDN and JSON; log envelopes in CBOR/JSON with detached signatures; efficient internal typed encoding for values.
- HTTP/gRPC endpoints for tx, query, pull, subscribe; streaming tx‑reports (server‑sent events or WebSocket/gRPC stream).

P2P Sync
- Signed DAG of txs; advertise heads; push/pull missing txs; verify signatures; apply with validation.
- Conflict detection/resolution for uniqueness at merge; retries with backoff; snapshots for fast catch‑up.
- Device identity: per‑device keys; optional user binding; key rotation.

Peers, Caches, Observers
- Local peer caches for attributes/segments; eviction strategies; invalidation via tx‑reports.
- Observer API for tx‑report subscriptions and reactive apps.

Admin & Maintenance
- Catalog and db listing; schema migration helpers; backup/restore; compaction/vacuum; index health and metrics.

Developer Experience
- CLI to inspect schema, run queries/pulls, transact, view tx‑reports, and render diagrams.
- SDKs/FFI (C baseline; optional WASM); guides mirroring the reference structure (model → architecture → APIs).

## Non‑Functional Requirements

Performance Targets (MVP guidelines)
- Single‑process SQLite: 2–5k datoms/sec sustained writes; p50 query < 50ms for common patterns on 100k–1M datoms.
- Postgres: 5–10k datoms/sec per writer with background indexing; p95 query < 150ms for indexed lookups.

Consistency & ACID
- Local ACID per store (SQLite/Postgres) across tx application.
- Indexes eventually consistent within bounded merge delay.
- P2P: eventual consistency; uniqueness enforced at merge with deterministic outcome.

Security
- TLS for client/server; optional at‑rest encryption (SQLCipher for SQLite, Postgres native).
- Signed tx envelopes; input validation; deterministic WASM sandbox for tx functions.

Portability
- Transactor logic portable via WASM/wasi; peers polyglot; storage adapters constrained to ubiquitous systems.

Observability
- Metrics: index write batches/bytes, merge latency, query latencies, sync status, cache hits/misses.
- Logs with correlation IDs per tx; structured logs for analysis.

## MVP Cut (Phase 1)
- SQLite + Postgres storage; EAVT/AVET/AEVT/VAET on SQLite, subset on Postgres with covering indexes.
- Datalog subset + Pull subset; as‑of/since; tx envelope (signed), tempids/lookup refs; tx‑reports.
- HTTP/gRPC endpoints; streaming tx‑report; CLI; basic SDK/FFI.
- P2P: push/pull by heads, signatures, uniqueness merge; snapshots; offline‑first.
- Basic full‑text (SQLite FTS5).

## Phase 2 and Beyond
- Advanced aggregates and rules; query planner improvements.
- Partitions/shards; multi‑writer coordination for server mode.
- Fine‑grained ACLs; attribute‑level security; audit tooling.
- Additional backends (RocksDB/FDB), cloud packaging, and managed services.

## Open Questions
- Global t: hybrid logical timestamps vs. server sequencing; merge rules.
- Partitions/shards model and writer assignment.
- Large value/blob handling (content‑addressed store and references).
- Access control in P2P scenarios (capabilities/ACL distribution).
- Pull recursion defaults and performance tuning on SQLite/Postgres.
- Tx function sandbox limits (determinism, resource caps, IO prohibition).
