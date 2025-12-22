Core Model

- Datoms: Both model facts as E/A/V/Tx with assertions and retractions. Mentat tracks adds/retracts per transaction in timelined_transactions with an added flag (db/src/db.rs:202).
- Attributes/schema: Both use attribute-level schema (type, cardinality, uniqueness, indexing). Mentat mirrors this and materializes schema/idents in views (db/src/db.rs:252).
- History retention: Datomic keeps immutable history by design. Mentat supports per-attribute no-history via :db/noHistory so some history can be dropped (db/src/entids.rs:24, db/src/metadata.rs:75).

Indexes & Access Patterns

- Datomic’s EAVT/AEVT/AVET/VAET are immutable persistent indexes. Mentat uses SQLite tables with always-on EAVT/AEVT, opt‑in AVET/VAET via partial indexes, plus optional FTS (db/src/db.rs:176).
- Mentat updates a “current state” datoms table in place, while appending to the tx log for audit/timeline purposes (db/src/db.rs:640, db/src/db.rs:662).

Query & Pull

- Both use Datalog and Pull. Mentat compiles Datalog to an abstract SQL plan, then to SQLite SQL, and projects results via a staged pipeline across crates (README.md:200, query-algebrizer/src/lib.rs:1, query-
  projector/src/lib.rs:1, query-sql/src/lib.rs:1, sql/src/lib.rs:1).
- Datomic has first‑class time filtering (“as of”). Mentat exposes tx‑log APIs in query (tx-ids ...), (tx-data ...) and :db/txInstant, but doesn’t present an “as‑of database” as a primitive in the same way
  (query-algebrizer/src/clauses/tx_log_api.rs:1, tests/query.rs:1179, db/src/entids.rs:20).

Architecture & Topology

- Datomic: distributed system (transactor + storage + peers), immutable segments, multi-level caches.
- Mentat: embedded Rust library over SQLite; a single process owns the connection; uses SQLite WAL and pragmas but not a distributed topology (README.md:1, db/src/db.rs:138).
- Mentat’s transaction log exists inside SQLite (timelined_transactions) with timeline manipulation to support sync workflows, not as a separate durable system service (db/src/db.rs:202, db/src/
  timelines.rs:1).

Scaling & Concurrency

- Reads: Datomic scales reads with immutable caches and many peers. Mentat is embedded; read scaling is bounded by SQLite and process architecture; no peer tier or distributed caching.
- Writes: Datomic serializes writes through the transactor; Mentat’s writes go through SQLite’s single-writer model (WAL used for concurrency) (db/src/db.rs:138).

Auditability & History

- Datomic: complete immutable history + “as of.”
- Mentat: tx log + :db/txInstant give audit trails and tx-range queries; current-state datoms is updated in place; attributes can opt out of history (db/src/db.rs:202, db/src/entids.rs:20, db/src/
  metadata.rs:75).

Distribution & Sync

- Datomic Pro/Cloud: built-in distributed deployments.
- Mentat: optional sync (“Tolstoy”) as a separate crate using a log-centric approach; proof-of-concept and feature-gated behind syncable (Cargo.toml:15, tolstoy/src/lib.rs:1, tolstoy/README.md:1).

Operations & Embedding

- Datomic: JVM services, storage backends, caches, cluster ops.
- Mentat: small, embeddable, Rust-first library with FFI and mobile SDK scaffolding; default bundles SQLite for predictable features (ffi/src/lib.rs:1, sdks/android/README.md:1, Cargo.toml:17).

Data Types & Large Values

- Datomic defers large binaries to external stores.
- Mentat supports a bytes value type and stores values as SQLite BLOBs; still leaves content-addressing/size tradeoffs to the app (db/src/db.rs:170, db/src/entids.rs:31).

Key Mentat Choices (different from Datomic)

- Embedded SQLite engine instead of a distributed log/index service, to make app bundling (esp. mobile/desktop) simple (README.md:1, Cargo.toml:1).
- Staged Datalog→SQL pipeline targeting SQLite rather than bespoke immutable index structures (query-algebrizer/src/lib.rs:1, query-sql/src/lib.rs:1, sql/src/lib.rs:1).
- Materialized “current” datoms with an append-only timelined_transactions log, plus per-attribute opt-out of history (db/src/db.rs:202, db/src/db.rs:640, db/src/metadata.rs:75).
- Optional sync as a separate, feature-gated component rather than core cluster architecture (Cargo.toml:15, tolstoy/src/lib.rs:1).
- Many small crates/traits to keep layers decoupled and builds fast; EDN as the lingua franca throughout (Cargo.toml:1, edn/src/lib.rs:1).

Mapping Datomic’s Benefits to Mentat

- Trivial read scaling: Not a goal; Mentat is single-process embedded, no peer tier.
- Database in your app: Yes—Mentat is a library with in-process query/tx (src/lib.rs:1).
- Auditability: Partial—tx log present; current state is updated; per-attribute history can be disabled (db/src/db.rs:202, db/src/metadata.rs:75).
- History/as-of: Partial—tx log APIs and :db/txInstant; no first-class as-of DB value.
- Powerful query: Yes—Datalog + Pull with a typed, staged engine (query-algebrizer/src/lib.rs:1, query-projector/src/lib.rs:1).
- Impedance-free modeling/flexibility: Yes—attribute-centric schema, open-world entities (src/vocabulary.rs:1, db/src/schema.rs:1).
- Efficient access patterns: Yes via SQL indexes (EAVT/AEVT always, AVET/VAET opt-in, FTS) (db/src/db.rs:176).

Mapping Datomic’s Tradeoffs to Mentat

- No per-db write scaling: Similar practical effect—SQLite serializes writes; Mentat uses WAL but not multi-writer clustering (db/src/db.rs:138).
- Not for high-churn telemetry/logs: Similar—Mentat’s design targets app data-of-record; high-ingest streams aren’t a primary fit.
- No structural “types”: Same—Mentat relies on attribute schema; structural constraints enforced at transaction time (db/src/metadata.rs:339).

If you want, I can:

- Expand this into a side-by-side comparison table.
- Extract and add code links for (tx-ids), (tx-data), and pull examples.
- Note specific gaps where Mentat planned features but didn’t complete them.