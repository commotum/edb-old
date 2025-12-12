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
- Tuple/Composite value (Spec Only; MVP target): a fixed‑arity, typed record stored as a single value `V` with engine‑level validation and canonical encoding; e.g., `:style/color` as `[r g b a]`.
- Transaction: declarative set of datoms accrued atomically; produces a new db value and t.
- Time travel: as‑of, since, history; queries run against a db value.
- New scalar types evolve the system: e.g., `:db.type/uint8` (unsigned 8‑bit integer, 0..255) with 1‑byte canonical encoding and numeric sort; external EDN/JSON as integers 0..255.

## Functional Requirements

Schema & Catalog
- Define attributes as data: ident, doc, valueType, cardinality, uniqueness (identity/value), isComponent, noHistory, indexed, fulltext.
- Support aliases and deprecation metadata; never remove or reuse names (growth‑only).
- Provide a catalog API to list attributes, schema versions, and annotations.

Schema defaults and identity patterns (borrowed)
- Growth‑only: never remove or reuse names; introduce aliases and deprecations instead. Prefer adding new attrs/types over mutating semantics.
- Identity spectrum:
  - Opaque numeric entity ids
  - Idents (keywords) for schema and enumerations
  - Unique identities (:db.unique/identity) for domain keys with idempotent upsert
  - Unique values (:db.unique/value) for single‑holder keys
  - Lookup refs [attr value] on unique attrs accepted wherever entity identifiers are accepted
  - Prefer squuids/UUIDv7 for time‑ordered identifiers
- Components and lifecycle: :db/isComponent models owned sub‑entities; retracting a parent cascades to components; Pull expands component maps by default.
- Validation as data: attribute predicates (:db.attr/preds), entity predicates (:db.entity/preds), and entity specs (:db.entity/attrs, :db.entity/preds) enforced via :db/ensure with structured anomalies on failure.

Value Types (overview)
- See docs/research/value-types.md for the canonical scalar set, external representations (EDN/JSON), and internal encoding/ordering notes.
- Growth-only: add new value types when they make domain sense (e.g., `:db.type/uint8` for color channels) rather than overloading existing types.

Type limitations and caveats
- :db.type/bytes is equality‑only; it cannot be unique or used in lookup refs.
- Floating NaN cannot participate in upsert comparisons; retract then assert a new value.
- For JSON interop, integers outside JS safe range (|n| > 2^53−1) must be strings (or BigInt in typed APIs).

Philosophy
- The database evolves by adding sensible data types and structs (tuples) rather than forcing attributes to fit ill‑suited value types. Prefer adding new scalars like `:db.type/uint8` when appropriate.

Tuple/Composite Attributes (First-class)
- `:db/valueType :db.type/tuple`
- MVP constraint: homogeneous tuples only — all slots share the same scalar type. Future phases may allow heterogeneous per‑slot types.
- Canonical schema options:
  - Explicit: `:db/tupleTypes  [:db.type/uint8 :db.type/uint8 :db.type/uint8 :db.type/uint8]` (per‑slot types)
  - MVP sugar: `:db/tupleElemType :db.type/uint8` + `:db/tupleArity 4` (engine normalizes to `:db/tupleTypes` by repeating the element type `arity` times). Do not specify both `:db/tupleTypes` and `:db/tupleElemType`.
- `:db/tupleArity  4` (derived from `:db/tupleTypes`, or provided explicitly; stored for clarity)
- `:db/tupleLabels [:r :g :b :a]` (optional; influences Pull shape)
- `:db/default     [0 0 0 255]` (optional default value)
- Works with existing knobs: `:db/cardinality`, `:db/noHistory`, `:db/unique`, `:db/doc`, `:db/alias`, `:db/indexed`, `:db/fulltext=false`.
- Validation: arity and slot type enforced at transact; for MVP, enforce that every slot value conforms to the single `:db/tupleElemType` (or repeated `:db/tupleTypes`). Retractions behave like scalar values.
- Growth-only: can append labels, add aliases/docs; cannot change arity or slot type in place — use new attribute + alias for migrations.

Example (schema & data)
```clojure
;; Schema
{:db/ident :style/color
 :db/valueType :db.type/tuple
 :db/cardinality :db.cardinality/one
 :db/tupleTypes  [:db.type/uint8 :db.type/uint8 :db.type/uint8 :db.type/uint8]
 :db/tupleLabels [:r :g :b :a]
 :db/default     [0 0 0 255]
 :db/doc "RGBA; each channel 0..255 (00–FF); a defaults to 255"}

;; Transact data (EDN)
[{:db/id -1
  :doc/title "Welcome"
  :style/color [120 40 255 128]}]
```

MVP sugar equivalent
```clojure
{:db/ident :style/color
 :db/valueType :db.type/tuple
 :db/cardinality :db.cardinality/one
 :db/tupleElemType :db.type/uint8
 :db/tupleArity 4
 :db/tupleLabels [:r :g :b :a]
 :db/default [0 0 0 255]
 :db/doc "RGBA; each channel 0..255 (00–FF); a defaults to 255"}
```

JSON wire example (API)
```json
{"tx":[{"db/id":"temp-1","doc/title":"Welcome","style/color":[120,40,255,128]}]}
```

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

Tuples in queries (Spec Only; MVP target)
- Tuples bind as single values; equality/joins compare entire tuples.
- Tuple accessors in predicates: `(tuple/get ?c :r ?r)` or `(tuple/slot ?c 0 ?r)` yield a scalar per slot.
- Aggregation treats tuples as atomic unless an accessor is used.
 - Lowering: labeled accessors expand to positional using `:db/tupleLabels`; error if a label is used and labels are not defined.

Pull
- Support pull patterns (forward/reverse, nesting, wildcard, recursion with limits, :as/:default/:limit/:xform options).
- Normalize and cache patterns; efficient join strategy for nested pulls; usable standalone and in find specs.
- Tuples: return vectors by default, or labeled maps if `:db/tupleLabels` exist.
- Example Pull
```clojure
[:doc/title {:style/color [:r :g :b :a]}]
;; → {:style/color {:r 120 :g 40 :b 255 :a 128}}
```

Indexing
- Maintain EAVT/AVET/AEVT/VAET via covering indexes; typed ordering across values.
- Background merge/compaction; snapshots/checkpoints to accelerate reads.
- Full‑text support: SQLite FTS5; Postgres tsvector + GIN.
- Uniqueness enforcement on `(a, v)` for identity/value uniques; idempotent upserts via lookup refs.
- Tuples: compact, lexicographically sortable internal encoding; comparisons are lexicographic across slots. See docs/research/indexing-strategy.md.
 - Range predicates (=, !=, <=, <, >, >=) push down to AVET; ensure attrs needed for range have :db/index true or uniqueness enabled.
 - Maintain a fast in‑memory delta (“memory index”) merged with durable segment trees by background jobs (wide branching factor ⇒ sublinear job times).

Storage Backends
- SQLite (local) and Postgres (server) as first‑class targets; optional KV (RocksDB/FDB) follow‑up.
- CAS semantics for segment persistence; DDL/migration management; constraints and error mapping (e.g., SQLState 23 for unique violations).

Serialization & Transport
- External API payloads in EDN and JSON; log envelopes in CBOR/JSON with detached signatures; efficient internal typed encoding for values.
- HTTP/gRPC endpoints for tx, query, pull, subscribe; streaming tx‑reports (server‑sent events or WebSocket/gRPC stream).
- Tuples & new scalars (e.g., `uint8`): define canonical encodings for internal storage; EDN/JSON remain numeric vectors (or labeled maps) externally.

Writer, isolation, and sync
- Single writer composes transactions by set‑union of primitive datoms returned by tx functions — no read/modify/write updates.
- Strong serializable writes via conditional puts of log/index roots; per‑peer operations are monotonic; cross‑peer reads are serializable.
- Provide sync(t): given a basis t, return a db value that includes at least t to coordinate read‑your‑writes across processes.

Transaction hints (latency)
- Provide a with‑like API to compute :hints on the peer (dry‑run). Pass :hints to transact so the writer can prefetch; semantics remain unchanged.

Query & Pull (details)
- Range predicates (=, !=, <=, <, >, >=) push down to AVET; prefer them over custom predicates for performance.
- Built‑ins: get‑else, get‑some, ground, missing?, tuple/untuple; user functions must be pure. Support qseq for lazy streaming and rules for reusable logic.
- Query ergonomics: structural query caching; parameterize inputs; order clauses by selectivity; support timeouts.
- Pull patterns: forward/reverse attrs (:rel/child, :rel/_child); :as/:default/:limit/:xform; wildcards; recursion limits; component defaults vs non‑component ids. Reverse lookup caveat: avoid leading underscore in the name portion of idents you want to reverse‑navigate.

P2P Sync
- Signed DAG of txs; advertise heads; push/pull missing txs; verify signatures; apply with validation.
- Conflict detection/resolution for uniqueness at merge; retries with backoff; snapshots for fast catch‑up.
- Device identity: per‑device keys; optional user binding; key rotation.
 - Basis coordination: include basis t in out‑of‑band communications and support sync(t) to align readers across peers/services.

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

Limitations
- :db.type/bytes: not unique, not for lookup refs; equality‑only indexing.
- NaN upsert: retract before assert when changing NaN values.

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
- Tuple indexing helpers (slot-wise expression pushdown to SQL)?
- Arity: no hard cap in spec; initial implementations optimize/testing for common small composites (e.g., RGBA/geo/quaternion/range).
- `noHistory` semantics on large tuple churn.
- Full‑text is not applicable to tuples.
- Homogeneous-only constraint: when and how to lift to heterogeneous tuples; planner/encoding implications; feature flag negotiation when lifting.

## Rationale (Tuples)
- Compact, indexed, and queryable way to model small fixed-arity records (RGBA, geo points, ranges) without the overhead of component entities. Improves Pull ergonomics and keeps EAVT/AVET lean.

## Risks (Tuples)
- SQL pushdown for slot predicates may be limited on SQLite without generated columns; cross-store canonical encoding must be stable across runtimes.

## Next Steps (Tuples)
- Specify canonical encoding, tuple accessor grammar; prototype generated columns for hot tuple attributes on Postgres; add cross‑runtime conformance tests; feature negotiation for `tuple`, `tuple-enc:v1`, and `uint8`.
- Document and implement `:db/tupleElemType`/`:db/tupleArity` sugar (normalize to `:db/tupleTypes`); enforce homogeneous tuples for MVP.
