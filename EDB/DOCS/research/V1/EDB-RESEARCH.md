# EDB Research Log & Checklist

North Star
- What if creating a database were as easy as creating a struct or class — and Datomic’s time‑traveling, replayable model ran everywhere on everyday defaults (WASM, SQLite/Postgres, HTTP/gRPC), with first‑class peer‑to‑peer sync and effortless, evolvable schemas for arbitrary relations and consumers?

How to review each item
- Purpose: what is this for?
- Key insights: invariants, algorithms, APIs relevant to EDB
- Dependencies: libraries, storage, runtime
- Constraints: performance, consistency, single‑writer, CAS, indexing behavior
- Risks/gaps: limitations or unclear parts
- Actions: concrete next steps (prototype/spec/change/open question)
- Produce: 1‑paragraph summary + bullet insights + actions

Meta
- Related docs: insights.md, p2p-sync-mvp.md, indexing-strategy.md

## Repo Survey & Tooling
- [x] Root purpose and tooling (`README.md`, `main.py`, `pyproject.toml`)
  - Summary
    - Root overview names the project “EDB” (README.md:1). Python entrypoint prints a greeting (main.py:1) and minimal Python packaging is present (pyproject.toml:1) requiring Python 3.10 (.python-version:1).
    - Repo content centers on Datomic research and prototypes: reverse engineering (datomic-reverse-engineer), reference docs (datomic-reference), Mentat (Rust), and Postgres K/V setup scripts.
    - Tooling mix: Python (basic), Rust (Mentat workspace), SQL, Bash, Graphviz DOT diagrams, Java/Clojure artifacts for decompiled Datomic code, and a Clojure CLI installer script.
  - Key insights
    - This repo is a research workspace rather than a single build target; multiple languages and ecosystems coexist.
    - Python is currently non-essential; Rust and documentation are the primary assets; Java/Clojure pieces are artifacts for study, not to be built.
    - Graphviz can render `.dot` architecture diagrams; scripts for rendering could improve DX.
  - Actions
    - Define a minimal dev environment doc (Rust stable, Python 3.10, Graphviz, Java JRE for browsing decompiled code, optional Clojure CLI if needed).
    - Add simple Makefile/scripts to render DOT to PNG/SVG and to open docs (optional).
    - Keep Python lightweight unless an analysis/CLI emerges; avoid unnecessary Python deps.

## PostgreSQL Storage Setup Scripts
Files: `datomic-postgresql-storage-setup-scripts/`
- [x] `postgres-user.sql` — role creation
  - Summary: Creates login role `nemo` with password `nemo`.
  - Insights: Example only; insecure default credentials.
  - Actions: Parameterize user/secret; remove public grants; follow least‑privilege.
- [x] `postgres-db.sql` — DB creation
  - Summary: Creates `mycloud` DB (UTF‑8, template0), owner postgres.
  - Insights: Baseline settings fine; owner should be dedicated role.
  - Actions: Use app role as owner; configure connection limits per env.
- [x] `postgres-table.sql` — K/V table
  - Summary: `mycloud_kvs(id text primary key, rev integer, map text, val bytea)`; grants ALL to postgres and public.
  - Insights: Simple K/V layout matching Datomic SQL adapter expectations; permissive grants.
  - Actions: Revoke public ALL; grant needed privileges to app role; consider indexes on `rev` depending on CAS usage; migration path.

## Datomic Reference (Ground Truth)
Folder: `datomic-reference/`
- Overviews
  - [x] `overview.md`
    - Summary: Introduction to Datomic’s information model (datoms, schema, entities), architecture (transactor, peers, storage), editions/APIs, and getting started.
    - Key insights: Immutable log and indexes enable time travel and read scale; peer vs client API split; schema-as-data with attribute-level constraints.
    - Actions: Capture invariants in insights.md; align EDB intro structure around model → architecture → APIs.
  - [x] `entities.md`
    - Summary: Entities are lazy associative views over datoms at a point in time; forward/reverse navigation; caching via touch; time-basis behavior.
    - Key insights: Entities are point-in-time; reverse lookup via underscore attrs; component semantics for recursive touch.
    - Actions: Specify entity view semantics for EDB, including reverse navigation and component recursion.
  - [x] `programming_with_data_and_edn.md`
    - Summary: EDN as data exchange; program with data-first mindset; patterns for representing rich types and domain facts.
    - Key insights: EDN/EDB API should be language-neutral; typed values and lossless serialization are core.
    - Actions: Define EDB wire format (EDN and JSON), and typed value mapping.
  - [x] `time_in_datomic.md`
    - Summary: as-of/since/history filters and examples; time t/tx semantics and impacts on queries.
    - Key insights: Time-travel APIs are first-class; entities are not history-spanning; history view includes retractions.
    - Actions: Specify EDB time filters and result semantics; plan index support for time predicates.
  - [x] `best_practices.md`
    - Summary: Growth-not-breakage; one-direction relationships; uniques for external keys; noHistory for churn; aliases; schema annotation.
    - Key insights: Strong defaults for evolvability; name stability; leveraging idents for enums.
    - Actions: Adopt growth-only schema migrations in EDB; add aliasing and deprecation metadata.
- Indexes
  - [x] `indexes/1_indexes_toc.md`
    - Summary: Structure of index docs.
    - Actions: N/A.
  - [x] `indexes/2_index_model.md`
    - Summary: EAVT/AVET/AEVT/VAET roles; sorted, immutable segments; query access patterns.
    - Key insights: Composite ordering drives query plans; values must be comparably ordered across types.
    - Actions: See indexing-strategy.md for SQLite/Postgres index designs.
  - [x] `indexes/3_background_indexing.md`
    - Summary: Background index build/merge from tx log into persistent segments; durability.
    - Key insights: Decouple write path from index maintenance; peers stream segments.
    - Actions: Define EDB background merge job and snapshot strategy.
- Query & Pull
  - [x] `query_and_pull/1_query_toc.md`
    - Summary: TOC for query docs.
  - [x] `query_and_pull/2_executing_queries.md`
    - Summary: Executing Datalog queries; inputs; result shapes; performance notes.
    - Key insights: Find specs, bindings, and inputs shape API ergonomics.
    - Actions: Draft minimal EDB query API surface and result encodings.
  - [x] `query_and_pull/3_query.md`
    - Summary: Datalog clauses, predicates, rules; source vars; aggregation.
    - Key insights: Separation of parse → algebrize → plan is beneficial.
    - Actions: Mirror Mentat’s algebrizer → SQL approach for EDB MVP.
  - [x] `query_and_pull/4_pull.md`
    - Summary: Pull patterns (forward/reverse, wildcard, nesting, recursion, as/default/limit/xform) and grammar.
    - Key insights: Pull is declarative and language-neutral; integrates with query find.
    - Actions: Define a Pull subset for EDB and map to SQL joins efficiently.
- Schema
  - [x] `schema/1_schema.md`
    - Summary: Attribute schema (type, cardinality, doc, refs); schema as data.
    - Key insights: Attribute-level control unlocks evolvability and per-attr constraints.
    - Actions: Specify EDB attribute catalog and migration DDL.
  - [x] `schema/2_changing_schema.md`
    - Summary: Additive changes, renames via aliases, avoiding breaking changes.
    - Key insights: Never remove or reuse names; prefer aliases and annotations.
    - Actions: Provide first-class aliasing and deprecation markers in EDB.
  - [x] `schema/3_data_modeling.md`
    - Summary: Modeling entities/refs/enums; component relationships; cardinality choices.
    - Key insights: Components form ownership trees; enums via idents.
    - Actions: Document modeling playbook for EDB users.
  - [x] `schema/4_identity_and_uniqueness.md`
    - Summary: Unique identity (`db.unique/identity`) and value constraints; lookup refs.
    - Key insights: Uniques enable idempotent upserts and external keys.
    - Actions: Enforce unique indexes and conflict retries in EDB (incl. P2P merge behavior).
- Transactions
  - [x] `transactions/1_transactions_toc.md`
    - Summary: TOC for transactions docs.
  - [x] `transactions/2_transaction_model.md`
    - Summary: Declarative, immutable transaction model; d/with vs d/transact; db as value; adds/retracts.
    - Key insights: Order-free validation; tx as information; ledger semantics.
    - Actions: Design EDB tx envelope and with/apply function; add deterministic validation hooks.
  - [ ] `transactions/3_transaction_data.md`
    - Actions: Review and capture data clause shapes for EDB wire format.
  - [x] `transactions/4_processing_transactions.md`
    - Summary: Tx processing stages; tempids; indexing; tx reports.
    - Key insights: Tempid resolution and tx-report contracts for subscribers.
    - Actions: Define EDB tx-report schema and subscription API.
  - [x] `transactions/5_transaction_functions.md`
    - Summary: Tx functions for computed writes; constraints and safety.
    - Key insights: Determinism and sandboxing are critical.
    - Actions: Specify WASM tx functions with deterministic sandbox.
  - [ ] `transactions/6_acid.md`
    - Actions: Capture ACID guarantees target for EDB (local vs distributed).
  - [ ] `transactions/7_synchronization.md`
    - Actions: Align with P2P log model; define sync consistency envelope.
  - [ ] `transactions/8_partitions.md`
    - Actions: Decide on partitions/shards for concurrency and scaling.
  - [ ] `transactions/9_reducing_latency_with_transaction_hints.md`
    - Actions: Evaluate hints analogous to Datomic for EDB.
- Operation
  - [x] `operation/1_storage.md`
    - Summary: Storage backends; configuration; performance considerations.
    - Actions: Map to EDB storage adapters (SQLite/Postgres/KV options).
  - [x] `operation/2_transactor.md`
    - Summary: Transactor config, connectivity, SSL, memcached, AWS notes.
    - Key insights: Single-writer architecture and peer discovery.
    - Actions: For EDB, define transactor/writer role for centralized mode; contrast with P2P.
  - [ ] `operation/tutorial/1_tutorial_toc.md` … `8_history.md`
    - Actions: Skim for examples to reuse in EDB docs.
- Diagrams
  - [x] `datomic.dot` (classic)
    - Summary: JVM transactor/peers, CAS to storage, segment streaming.
  - [x] `overview.dot` (full view)
    - Summary: Information model + indexes + processes in one diagram.
  - [x] `newDB.dot` (new runtimes)
    - Summary: WASM transactor, polyglot peers, gRPC/Web, KV storage options.

For each: produce summary, key insights, constraints, actions, and open questions; feed invariants into insights.md.

## Mentat (Rust)
Folder: `mentat/`
- Top level
  - [x] `README.md` (motivation, comparisons, build/tests)
    - Summary: Unmaintained Rust project inspired by Datomic/DataScript; focuses on persistence and performance over DB-as-value; comparisons to DataScript, Datomic, SQLite; aims to store arbitrary relations on SQLite without upfront storage schema coordination. Datalog for querying; additions/retractions for tx input.
    - Key insights: Clear articulation of the SQL mapping approach (algebrizer → SQL → projector); embeddability and single-file storage are key; tx-as-ledger aligns with EDB goals.
    - Actions: Borrow phrasing for EDB positioning; adopt “relations on ubiquitous stores without upfront storage schema” as part of messaging; ensure we keep Datomic’s time travel and tx log.
  - [x] `Cargo.toml` (features/workspace)
    - Summary: Crate `mentat` v0.11.1; features `bundled_sqlite3`, `sqlcipher`, `syncable`; workspace members `tools/cli`, `ffi`.
    - Key insights: Feature‑gated sync/encryption; good model for EDB modularity via features.
    - Actions: Plan EDB feature flags (e.g., `p2p`, `fts`, `sqlcipher`, `wasm-tx`).
  - [x] `src/lib.rs`, `conn.rs`, `query_builder.rs`, `store.rs`, `vocabulary.rs`
    - Summary: lib.rs re‑exports types/macros; `Conn` wraps schema, attribute cache, tx observer; provides `q_once`, `q_prepare`, pull helpers, caching controls; `Store` convenience composition with SQLite connection; transaction lifecycle with `InProgress`; observers for tx reports.
    - Key insights: `Known { schema, cache }` threading into algebrizer; copy‑on‑write attribute cache for isolation in tx; explicit tx behaviors (Deferred/Immediate) over rusqlite.
    - Actions: Mirror connection + in‑progress patterns in EDB; define tx‑report observer API; design attribute cache story compatible with P2P.
- Core crates
  - [x] `edn/` (parser, transaction input)
    - Summary: EDN parser; streams values into transaction‑ready representations.
    - Actions: Define EDB’s value model and mapping to EDN/JSON.
  - [x] `core/` (types, SQL mappings, utils)
    - Summary: `ValueType`, `TypedValue`, SQL type linkages, utilities, reusable keywords.
    - Actions: Specify EDB core type set and comparison/ordering rules for indexing.
  - [x] `db/`, `sql/` (storage, abstraction)
    - Summary: SQLite schema and access; SQL abstraction layers.
    - Actions: Compare to EDB indexing‑strategy; confirm feasibility on Postgres.
  - [x] `transaction/` (transact, results)
    - Summary: Tx inputs, tempid resolution, tx reports, query helpers.
    - Actions: Define EDB tx envelope, tempid/lookup ref rules, report format.
  - [x] Traits: `core-traits/`, `db-traits/`, `sql-traits`, `public-traits`
    - Summary: Interface boundaries decoupling crates.
    - Actions: Use similar trait boundaries in EDB spec (even across languages).
- Query engine
  - [x] `query-algebrizer/`
    - Summary: `Known { schema, cache }`; parses → algebrizes into `AlgebraicQuery`; tracks bound vars; supports order/limit; checks fully‑bound queries.
    - Actions: Adopt separate algebrizer stage for EDB with a portable plan to SQL on SQLite/Postgres.
  - [x] `query-sql/`
    - Summary: Abstract SQL model bridging Datalog to SQL.
  - [x] `query-projector/`
    - Summary: Projects SQL rows to Datalog results; integrates Pull via `PullTemplate`/`PullConsumer` and expands bindings.
    - Actions: Design projection layer for EDB to support pull inside queries efficiently.
  - [x] `query-pull/`
    - Summary: Puller prepares and executes pull over a set of entity ids to maps; supports aliasing and recursive fetch.
    - Actions: Implement Pull MVP compatible with EDB schema/catalog and indexes.
- Tooling/interfaces
  - [x] `tools/cli/` (mentat_cli)
    - Summary: REPL/CLI over Mentat; forwards crate features.
    - Actions: Use as reference for an EDB CLI to inspect db, tx, and query.
  - [x] `ffi/` (C FFI)
    - Summary: `mentat_ffi` exposing C interface; builds `lib`, `staticlib`, `cdylib`.
    - Actions: Plan FFI story for EDB (C baseline; consider WASM for broader reach).
  - [x] `sdks/` (Android)
    - Summary: Platform SDK examples.
  - [x] `docs/` (API docs)
    - Summary: Static site with Rust/Swift/Java docs.
    - Actions: EDB docs strategy: API + guides mirrored from datomic-reference structure.

For each: note patterns portable to EDB (even if not Rust), risks from unmaintained status, and DX ideas (CLI/FFI/SDKs).

## Datomic Reverse Engineer (Peer)
Folder: `datomic-reverse-engineer/`
- Orientation
  - [x] `src-java/summary.txt` (CFR report)
    - Summary: Decompile of peer-1.0.7277.jar via CFR 0.152, with numerous unstructured methods flagged.
    - Actions: Use targeted file reads for high-signal classes; avoid relying on broken reconstructions.
- Query/Data path
  - [x] `datomic/datalog$eval_query.java`, joins, projections
    - Summary: Datalog evaluation dispatches rules (`eval-rule`), adornment/predicate processing, input pruning; heavy use of Clojure vars/seq processing.
    - Insights: Rule evaluation loop over program rules; input set pruning before scheduling; aligns with staged eval.
    - Actions: Reflect staged query plan in EDB algebrizer/executor; keep rule dispatch explicit.
  - [x] `datomic/pull$*.java`
    - Samples: `pull$normalize_pattern.java`, `pull$dereffed_index_pull.java`, `pull$try_xform.java`, `pull$pull_1$f__19054.java`, `pull$pull$f__19059.java`.
    - Summary: Pull normalizes patterns (string/edn → canonical map), caches normalized patterns, supports dereferenced index pulls, and integrates optional extension transforms via `extension-resolver/resolve-xform!`.
    - Insights: Pattern normalization + caching is a key optimization; supports reverse attrs and xform/default/limit options; pull can run standalone or inside query projections.
    - Actions: EDB Pull: implement normalization cache, extension hook points for transforms, efficient joins for nested patterns, reverse navigation.
- Indexes/Log/IO
  - [x] `datomic/index$*.java`, `index/TransposedData.java`
    - Sample: `index$write_vals.java` logs batch counts, accumulates bytes, delegates to `cluster/write-vals` with `:index` op.
    - Insights: Index writes are clustered; metrics collected; value map batched.
    - Actions: In EDB, implement batched index writes; expose metrics; pluggable backends.
  - [x] `datomic/log$*.java`
    - Sample: `log$write_new_log.java` constructs new log root/tail, writes descriptors, uses `uuid->val-key`; sets rev=0, etag nil, d/r and d/l entries.
    - Insights: Log is append-structured; root/tail objects with descriptors; fressian serialization references.
    - Actions: EDB log: append-only with root/tail descriptors; avoid Fressian (use CBOR/JSON + binary values).
- Storage adapters
  - [x] `datomic/kv_sql*`, `kv_sql_ext*`, `sql-src/datomic/sql$*.java`
    - Sample: `kv_sql_ext$kv_sql.java` bridges cluster conf → JDBC spec → kv-sql connection; `kv_sql$constraint_violation_QMARK_.java` checks SQLState "23"; `sql$execute_commands.java` iterates commands into PreparedStatements.
    - Insights: SQL adapter expects constraint detection via SQLState; commands batched across JDBC.
    - Actions: EDB Postgres adapter: map unique violations, robust error mapping; command batching.
  - [ ] `datomic/ddb*`, `s3*`, `cassandra*`, `h2*`
- Peer/cluster/process
  - [x] `datomic/peer$*.java`, `cluster*`, `process_monitor*`, `extensions*`
    - Samples: `peer$get_catalog.java` (URI parse → local or system cluster catalog), `peer$administer_system.java` (actions like `upgrade-schema`, `release-object-cache`), `cluster$write_vals_STAR_$fn__10660.java` (write path helper), `cluster/QueueingWriter.java` (async write queue with timeouts/promises), `process_monitor$fn__23484.java` (module load registration), `extensions$tx_ids.java` (tx id range from log).
    - Summary: Peer utilities for catalog discovery and admin, clustered write helpers with async queueing + timeouts, process monitoring hooks, and tx id helpers.
    - Insights: Cluster write path abstracts over store with queueing and backpressure; peers maintain local db catalogs; administrative tasks adjust schema/cache; tx ids streamed from log.
    - Actions: EDB: design observer and admin APIs; provide async write batching with bounded queues; expose tx-range APIs; define catalog discovery for P2P and centralized modes.
- Utilities
  - [x] `fressian*`, `crypto*`, `lucene/*`, `datafy*`, `treewalk*`
    - Samples: `fressian$write_handler_lookup.java` (compose custom write handlers), `crypto$random_bytes.java` (SecureRandom bytes), `lucene$field_stream.java` (field binary stream view), `datafy$fn__17266.java` (helpers for data coercion), `treewalk$create_node.java` (node creation via lookup).
    - Summary: Serialization handlers (Fressian), crypto utilities, full‑text interop, datafy/treewalk utilities for value coercion and tree building.
    - Insights: Fressian is central to Datomic wire/storage serialization; Lucene utilities indicate FT integration; helpers provide coercion and tree building around identifiers.
    - Actions: EDB: choose serialization (avoid Fressian; prefer CBOR/JSON + typed encoding); plan FT via SQLite FTS5/Postgres tsvector; implement datafy/tree building helpers for Pull.
  - Java internals
  - [x] `com/datomic/impl/peer/ActiveMQInputStream.java`
    - Summary: Adapts an `ActiveMQBuffer` to a Java `InputStream` (read byte/bytes with readableBytes checks).
    - Actions: If adopting message queues/transports, provide similar adapters; for EDB, prefer gRPC/HTTP streams.
- Tools
  - [ ] `tools/tools.decompiler/*`

For each: extract invariants (tx ordering, CAS rules, index semantics), and portability deltas (JVM → WASM/HTTP, serialization alternatives). 

## P2P & Sync
- [x] Define log model (append‑only, signed, t/causality, replay)
- [ ] Device identity & trust (keys, signatures, rotation)
- [x] Replication flows (peer↔peer, peer↔service, conflicts)
- [ ] Consistency targets (eventual vs. partitions, per‑attr unique)
- [ ] Security/encryption (at rest/in transit)
- [ ] Offline‑first behavior (queues, backpressure)
- [ ] Observers/events (tx‑reports, subscriptions)

Output: p2p-sync-mvp.md with an MVP plan and open questions.

## Tuple/Composite Values (Spec Only; MVP target)
- Purpose
  - Compact representation for small fixed-shape records (color, geo, ranges) as single values to keep `EAVT/AVET/AEVT/VAET` lean.
- Key insights
  - Lexicographic canonical encoding for tuples; optional generated columns for hot attributes; Pull label mapping; Datalog tuple accessors; evolve by adding new scalars like `:db.type/uint8`.
- Dependencies
  - Value encoding layer; algebrizer predicate support; SQL adapters; JSON/EDN serializers.
- Constraints
  - Growth-only schema; stable canonicalization for P2P; no hard cap in spec — initial focus on common small composites.
- Risks/gaps
  - Slot pushdown on SQLite; migration from component-entity patterns; unknown-type handling in older peers.
- Actions
  - Draft encoding spec; add query accessor grammar; feature flags (`tuple`,`tuple-enc:v1`,`uint8`); author examples.
- Produce
  - 1-page spec + tests + examples (RGBA, geo point, range).

Review checklist (tuples & new types)
- [ ] Arity and per-slot types deterministic
- [ ] Canonical encoding documented (endianness, varint, text normalization)
- [ ] Datalog accessor grammar specified (labeled + positional lowering)
- [ ] Pull rendering (labels vs vector) specified
- [ ] SQL pushdown plan captured for Postgres/SQLite
- [ ] P2P feature flags and interop policy stated (tuple, tuple-enc:v1, uint8)
- [ ] Growth-only migration guidance written

## Deliverables (Each Section)
- [ ] 1‑paragraph summary
- [ ] Key insights (bullets)
- [ ] Decisions/requirements for EDB
- [ ] Risks/open questions
- [ ] Proposed next actions
- [ ] File references

## EDB Requirements (Draft)
- Runtime & Transport
  - WASM/wasi transactor runtime; polyglot peers; HTTP/gRPC baseline; optional P2P transports.
- Storage
  - SQLite (local), Postgres (server), KV options (RocksDB/FDB); CAS semantics at segment level.
- Schema & Catalog
  - Attribute catalog with type, cardinality, uniqueness, noHistory, isComponent; aliasing/deprecation metadata; schema‑as‑data.
- Transactions & Log
  - Append‑only signed tx log; deterministic tx functions (WASM); tempid resolution; tx reports; time filters (as‑of/since/history).
- Query & Pull
  - Datalog subset MVP; algebrize → SQL plan; Pull patterns (forward/reverse/nesting/limits/defaults) with efficient joins.
- Indexing
  - EAVT/AVET/AEVT/VAET via covering indexes; background merge; full‑text via FTS5/tsvector.
- P2P Sync
  - Signed DAG of txs; head advertisement; push/pull; conflict handling (uniques on merge); snapshots/checkpoints.
- DevEx
  - CLI, FFI (C), SDKs, docs mirroring model→architecture→API; DOT diagram tooling.

Open Risks & Questions
- Global t coordination and merge; partitions/shards; ACLs; large values; index compaction at scale; tx function safety across runtimes; Pull recursion performance.
