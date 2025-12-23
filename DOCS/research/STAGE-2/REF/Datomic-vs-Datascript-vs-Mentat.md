DataScript (in‑memory, CLJ/CLJS)

- Objective: Single‑process, immutable Datalog DB for apps and the browser; simple, fast, embeddable. DOCS/research/Datascript-Map.md:1
- Differentiators
    - Pure in‑memory DB value; transactions return a new value (persistent B‑trees EAVT/AEVT/AVET).
    - Works in Clojure and ClojureScript (browser/JS) with small LRU caches and JS interop.
    - Pull returns plain nested maps; entity views exist but pulls don’t depend on them.
    - Optional persistence/serialization; no background indexer or external storage.
- Exclusive tradeoffs
    - No server, transactor, or pluggable storage; durability and concurrency are up to the host.
    - Memory‑bound datasets; no per‑DB write scaling or cross‑process ACID story.
    - Operational features (partitions, sync, background indexing) are out of scope.
- Key selling points
    - Minimal API with full Datalog + Pull semantics in‑process.
    - Deterministic, immutable snapshots; easy to reason about and test.
    - Runs in the browser; great for local state, prototypes, and offline UIs.

Mentat (SQLite‑backed, Rust)

- Objective: Datomic‑style developer experience over SQLite: EDN Datalog + Pull, durable offline store, strong ergonomics. DOCS/research/Mentat-Map-V2.md:1
- Differentiators
    - Compiles Datalog to a structured SQL plan + “projector”, then runs via rusqlite.
    - Attribute‑level caches integrated into planning can skip or prune SQL work.
    - Rich host API: Conn/Store, prepared/explain/uncached queries, observers, vocabulary management, optional sync/encryption, typed QueryBuilder.
    - Two‑stage projectors for (pull …) to fetch rows then nested attributes.
- Exclusive tradeoffs
    - Constrained by SQLite concurrency/locking; no multi‑transactor clustering.
    - Query planning/execution shaped by SQL engine; complexity vs. a pure in‑mem engine.
    - Durability/time‑travel via SQLite + timelines, not immutable index segments.
- Key selling points
    - Portable, embeddable, durable single‑node store with Datomic‑like semantics.
    - Strong ergonomics (prepared queries, observers, vocab/versioning) for app teams.
    - Good offline‑first fit with optional sync and encryption.

Datomic (distributed, immutable indexes)

- Objective: Immutable, append‑only information model with embedded query, ACID transactions, time travel, and trivial read scaling. DOCS/research/Datomic.md:1
- Differentiators
    - Peers embed the query engine; transactor serializes writes; storage is pluggable.
    - Four persistent indexes (EAVT/AEVT/AVET/VAET), background indexing, multi‑level caching.
    - First‑class time travel, audit trail, partitions, squuids, entities, qseq, transaction functions.
    - Read‑your‑own‑writes sync across processes via basis T coordination.
- Exclusive tradeoffs
    - Per‑database writes are totally serialized; no horizontal write scaling.
    - Background indexing can bottleneck writes; eventual consistency in storage layers.
    - No schema time‑travel; bytes can’t be unique/lookup‑ref; tx functions run in the write pipeline.
- Key selling points
    - Proven production system with powerful Datalog + Pull over immutable indexes.
    - Low‑latency reads at peer, trivial read scaling, rich operational model.
    - Full auditability and effortless time travel.

What truly differentiates them

- Runtime and scope
    - DataScript: in‑memory, CLJ/CLJS, zero external deps; simplest mental model.
    - Mentat: embedded SQLite with Rust ergonomics; durable, offline‑first library.
    - Datomic: distributed architecture with transactor + peers + pluggable storage.
- Query execution
    - DataScript: in‑memory Datalog engine over persistent sets.
    - Mentat: Datalog → SQL plan + projector over SQLite.
    - Datomic: embedded engine over immutable on‑disk indexes with background indexing.
- Storage and ACID
    - DataScript: optional serialization; no cross‑process ACID story.
    - Mentat: ACID via SQLite transactions, observers, vocab/versioning.
    - Datomic: ACID via transactor, CAS‑guarded roots, append‑only log.
- Operational features
    - DataScript: none by design (keep it small).
    - Mentat: observers, caching, prepared/explain, optional sync/encryption.
    - Datomic: partitions, sync, background indexing, multi‑level caching, time travel.

When to choose which

- Pick DataScript for in‑process state, CLJ/CLJS apps, or browser‑first UIs needing Datalog + Pull without ops.
- Pick Mentat for an embeddable, durable, offline‑first store with Datomic‑like semantics on a single node.
- Pick Datomic for production systems needing immutable history, time travel, rich ops features, and easy read scaling across services.