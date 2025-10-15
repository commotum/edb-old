# EDB + MyCloud — Consolidated Plan (v1)

Status
- Canonical plan distilled from research docs, build order, and Datomic references.
- Scope: EDB (extensible database, EAVT model, universal schema) first. MyCloud DAG/author features later.

Sources
- Repo overview: overview.md:1, BUILD_ORDER.md:1
- Research: docs/research/* (EDB-REQUIREMENTS.md:1, value-types.md:1, tuple-encoding.md:1, indexing-strategy.md:1, insights.md:1, EDB-RESEARCH.md:1, p2p-sync-mvp.md:1, MyCloud-Overview.md:1)
- Datomic references: datomic-reference/** (schema, transactions, time, best practices, entities, overview)

Guiding Principles
- Information model: immutable datoms ⟨E A V Tx Op⟩ with add/retract; databases are values.
- Append-only log and time travel (as-of, since, history). Queries run against a db value.
- Schema-as-data with growth-only evolution (never remove/reuse names; use aliases and new attrs).
- Identity spectrum: numeric entity ids; idents (keywords); unique identities with upsert; lookup refs.
- Components and lifecycle: :db/isComponent implies cascade retract; pull expands components by default.
- Universal schema: attributes themselves are entities with well-defined meta (valueType, cardinality, uniqueness, etc.).

Architecture (EDB)
- Single-writer transactor applies transactions serially; peers/clients read immutable snapshots.
- Storage backends: SQLite (local) and Postgres (server) as first-class.
- Log: durable append-only sequence; db state reconstructable via replay.
- Indexes: EAVT first with in-memory delta + background merges to durable segments; then AVET (+range) and VAET (reverse refs). AEVT optional.
- Catalog: attribute definitions stored and queryable; alias mapping supported.

Schema & Catalog
- Attribute fields (baseline):
  - :db/ident (keyword), :db/valueType, :db/cardinality, :db/unique (None|Identity|Value)
  - :db/isComponent (bool), :db/noHistory (bool), :db/doc (string), :db/alias (additional idents)
- Growth-only rules:
  - Never remove or reuse names. Deprecate via doc and introduce aliases/new attrs instead.
  - Do not change cardinality/uniqueness/valueType in place; migrate via new attribute + alias.
- Uniqueness:
  - Identity → idempotent upsert on unique (a,v), tempids unify to existing e.
  - Value → at most one e may hold (a,v); conflicts reject.
  - Only cardinality one attributes can be unique. :db.type/bytes cannot be unique or used in lookup refs.
- Lookup refs: `[attr value]` accepted wherever an entity id is accepted if `attr` is unique.

Transactions (Grammar & Semantics)
- Grammar (MVP): list and map forms
  - ["add" e a v], ["retract" e a (v?)], ["cas" e a expected|null new]
  - ["retract-entity" e] cascades to component refs
  - ["tx-fn" :ident args…] expands to primitive ops (deterministic functions only)
  - Map form: {"db/id": id|temp|lookup, ":ns/attr": value, ...}
- Tx meta (system scope only):
  - :db/txInstant (instant, one) recorded as a datom on the transaction entity and enforced monotonic; if provided older, bump to last+1.
  - Keep other user-defined meta out of system scope for MVP; domains may add later as ordinary attrs when needed.
- Semantics:
  - Upsert via :db.unique/identity, uniqueness checks for :db.unique/value
  - Cardinality-one implicit retract on change
  - CAS checks expected vs current value
  - Lookup refs resolve against db-before

Values & Encoding
- Scalars (MVP baseline): long, double, boolean, string, keyword, uuid, instant, ref, bytes; include numeric subtypes where implemented (float32, float16, bfloat16) and uint8.
- Extended scalars (present/spec’d): bigint, decimal (canonical string externally; canonical, sortable internal encodings).
- Limitations: bytes are equality-only; cannot be unique/lookup. NaN cannot participate in upsert comparison; change via retract+assert.

Tuples / Composites (MVP)
- First-class `:db.type/tuple` with homogeneous slots only in MVP.
- Schema sugar: `:db/tupleElemType` + `:db/tupleArity` normalized to `:db/tupleTypes` (engine-side normalization).
- Validation: enforce arity and slot type at transact.
- Indexing: tuple values compare lexicographically by slot; equality and range via AVET. Full-text not applicable.
- Pull: optional `:db/tupleLabels` render labeled maps; otherwise vectors.
- Growth-only: cannot change arity/slot types in place; migrate via new attribute + alias.

Indexing Strategy
- EAVT: in-memory delta for recent writes; periodic merge to durable segment trees; CAS root adoption.
- AVET: enabled for attrs with :db/index true or any :db/unique; supports equality and range predicates with typed ordering.
- VAET: reverse refs to support Pull/navigation efficiently.
- Query pushdown: prefer range predicates (=, !=, <=, <, >, >=) to leverage AVET order; consider generated columns for hot tuple attrs (advisory).

Query & Pull
- Query (subset MVP): Datalog with :find, :in, :where; support equality/joins, basic aggregates (count/min/max/sum), and range predicates with AVET pushdown. Basic rules optional.
- Pull: forward/reverse attrs (:rel/_parent), components default to nested maps, non-components as ids; options for :as/:default/:limit; recursion limits.

Observability & Reports
- Tx-reports include primitives, resolved tempids, touched attrs, tx entity, and tx t/txInstant. Peers can subscribe.
- Metrics: index merges (batches/bytes), latencies (tx, query, merge), cache hit rate.

APIs (MVP)
- HTTP/gRPC endpoints: transact, db (basis), q, pull, sync(t), subscribe (tx-reports streaming).
- Serialization: EDN/JSON externally; canonical internal encodings for values (and tuples) used for hashing/ordering.

Backends
- SQLite: local, single-file, with FTS5 for strings; table layout for current values, unique index, and log. Background compaction and snapshots optional.
- Postgres: server mode; AVET/VAET realized by covering indexes; tx atomicity via single transaction.

P2P & MyCloud (Later Phase)
- MyCloud builds on EDB once core is stable and performant.
- Merkle DAG of transactions: signed DAG (Ed25519), author keys, parents, and feature negotiation in envelopes.
- Basis coordination via sync(t). Snapshots/checkpoints for fast catch-up.
- Uniqueness resolution at merge; deterministic outcomes; retry/backoff.
- Tx meta beyond :db/txInstant (e.g., :tx/actor, :source/confidence) to be introduced as domain-defined attributes when needed, not part of EDB system schema.

Build Order (Phased)
1) Core Encoding & Datom Types
   - Define scalar set and canonical order; tuple v1 (homogeneous) spec; golden vectors.
2) Transaction Model & Validation
   - Grammar, tempids, lookup refs, upserts, uniqueness, CAS, retract-entity (component cascade).
3) Append-Only Log & t (SQLite)
   - Durable log with monotonic t; tx entity and :db/txInstant.
4) Schema Catalog & Identity/Lookup & Components
   - Install attributes, enforce bytes limitations; aliases; component semantics.
5) Unique Enforcement (identity/value)
   - Table-backed uniqueness with (a,v)→e mapping; reject conflicts, idempotent upserts.
6) Memory Index + Background Indexer (EAVT)
   - In-memory delta + background merge to durable segments; atomic root adoption.
7) AVET + VAET Indexes
   - AVET for indexed/unique attrs; VAET for reverse refs.
8) Query Engine
   - Parse→algebrize→plan; push range predicates to AVET; aggregates and basic rules.
9) Pull Engine
   - Patterns, reverse attrs, components, tuple labels.
10) API Server (HTTP/gRPC)
   - Endpoints for tx, q, pull, sync(t), subscribe.
11) Observability
   - Metrics/logging/tx-reports.
12) P2P Sync MVP (MyCloud)
   - Signed DAG, heads, push/pull, snapshots, uniqueness at merge.
13) CLI/SDKs
   - Developer tooling and language bindings.

Testing Strategy
- Start with unit/property tests per step; add cross-step integration later.
- Golden vectors in tests/vectors/ for encoding (scalars and tuples), order, and round-trips.
- Tx-model tests for tempids, lookup refs, uniqueness, cardinality-one retractions, CAS.
- Index tests for EAVT/AVET/VAET scans and pushdown behavior.
- Pull tests for components, reverse attrs, tuple label rendering.

Decisions (MVP Clarifications)
- Tx meta: only :db/txInstant is a system attribute in EDB MVP; author/DAG/notes remain out of system schema and will be added by MyCloud/domain later as ordinary attributes.
- :db.type/bytes cannot be unique or used in lookup refs.
- Tuples v1 are homogeneous-only; engine normalizes schema sugar to canonical form.

Open Questions
- Global t strategy for multi-writer/server deployments (HLT vs server sequencing).
- Generated columns for tuple slots (Postgres/SQLite) and planner rewrite heuristics.
- History retention and noHistory semantics for large tuple churn.
- Feature negotiation for new types (tuple, uint8) in P2P contexts.

Next Actions
- Adopt this plan as the baseline in step READMEs/specs; keep per-step acceptance templates in edb/<step>/README.md and link to this file.
- Ensure system attribute :db/txInstant is installed automatically on bootstrap.
- Keep DAG/author/signatures in MyCloud phase; avoid premature system schema expansion.

