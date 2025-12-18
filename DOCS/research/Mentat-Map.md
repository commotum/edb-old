Mentat Repository Map
======================

Purpose: Provide a clear map of this repository – what each crate/module does, how the pieces fit together, and the reasoning behind the major design choices. Mentat is an unmaintained Rust project that implements a persistent, embedded knowledge base inspired by Datomic/DataScript. The project favors strong separation of concerns via multiple sub‑crates, typed EDN values, and SQLite as the storage engine.



Big Picture
-----------

- Mentat models facts and queries with Datalog semantics, stores data in SQLite, and exposes a convenient API for transactions and query (including pull). 
- The codebase is organized as a Rust workspace of many small crates to speed up builds, enforce encapsulation, and enable optional features (e.g., sync). 
- EDN is used as a rich interchange format for both data and queries. 
- The query engine is staged: parse EDN → algebrize against schema → translate/project → emit SQL → execute → project results back to rich types.



Design Principles & Rationale
-----------------------------

- Separate crates for clearer boundaries and faster iteration
  - Incremental builds: change isolation dramatically improves compile times on a large codebase.
  - Encapsulation: depending on a crate boundary is a stronger signal than `mod use`; it stabilizes interfaces and discourages tight coupling.
  - Feature gating: optional pieces (e.g., syncing) compile only when needed, reducing binary size and dependencies.
  - Reuse: foundational libraries (e.g., EDN parser) are useful standalone.

- EDN everywhere
  - Richer than JSON for Datomic/Datalog concepts (keywords, sets, instants, UUIDs), aligning with the domain model and query shape.
  - Minimizes lossy conversions between external representation and internal types.

- SQLite as the storage engine
  - Ubiquitous, embeddable, and reliable with advanced features (partial indexes, FTS4) needed for performance.
  - Bundled by default to ensure required SQLite capabilities and consistent behavior across platforms; supports linking to system SQLite and SQLCipher via features.

- Datalog semantics, SQL execution
  - Queries are written against domain concepts; algebrization and translation shoulder the work of mapping to the storage schema.
  - This keeps queries stable even as storage evolves.

- Event‑oriented store
  - Emphasis on transaction/time semantics and change observation outside the DB (observers vs. triggers), making integrations and sync easier.

- Sync as an optional log‑based component (“Tolstoy”)
  - Inspired by log‑centric architectures to support timelines and offline/device sync without entangling the core database.



Crate Map (What each crate does)
---------------------------------

Foundations
- edn/: EDN parsing and value representation. PEG grammar (`edn.rustpeg`), keyword/symbol handling, query EDN utilities. Provides the typed external format that Mentat consumes and produces.
- core/ (mentat_core): Core types/utilities used across the system (e.g., `TypedValue`, `ValueType`, SQL type mapping, tx reports, small shared utilities).
- core-traits/: Domain traits and value sets shared across crates without incurring heavier dependencies.
- sql-traits/: Lightweight SQL‑related errors/traits used by multiple crates without pulling in SQL emitters or rusqlite.

Storage Layer
- db/ (mentat_db): Implements the storage engine on top of SQLite. Responsibilities:
  - Bootstrapping a new DB and maintaining on‑disk schema.
  - Transacting EDN‑shaped data into datoms.
  - Attribute cache and in‑memory schema views for performance.
  - Observers to surface changes to embedding applications.
  Rationale: centralizes all SQLite touchpoints and schema evolution in one place.
- db-traits/: Errors and interfaces around DB concerns, allowing upper layers to depend on traits rather than concrete rusqlite/
  storage details.
- sql/ (mentat_sql): Emits SQL text and bindings from an abstract SQL representation. Keeps SQL emission concerns decoupled from both the DB crate and the query pipeline.

Query Engine (staged)
- query-algebrizer/: The heart of the query pipeline. Combines a parsed Datalog query with current schema/data to produce an `AlgebraicQuery` describing how the query can be satisfied as joins/constraints over Mentat’s SQL schema.
  Reasoning: isolates the domain‑level reasoning (types, attributes, cardinalities, uniqueness) from the storage/SQL layer.
- query-sql/: An abstract, typed representation of SQL queries (columns, expressions, grouping, etc.), bridging Mentat types and SQL constructs.
- query-projector/: Projects SQL rows into the Datalog result shapes (tuples, scalars, relations, aggregates, and pull). Decouples row decoding from query planning/translation.
- query-pull/: Implements Datomic‑style pull against the store using the algebrized plan and projector machinery.
- query-*-traits/: Trait and error crates dividing responsibilities among algebrization, projection, and pull; encourage layering and reduce cross‑crate dependencies.

Transactions & API Surface
- transaction/ (mentat_transaction): Transaction builder/helpers, query helpers, and metadata management used by the top‑level crate. Keeps the high‑level transaction experience consistent and testable.
- public-traits/: Public error and trait types meant for consumers (CLI/FFI/SDKs) without binding to internal details.
- mentat/ (root crate): Assembles the pieces into a usable library:
  - Connection/store management, in‑progress transactions.
  - Vocabulary management and entity builder.
  - Re‑exports of public types.
  - Small macros to ergonomically construct keywords/variables (`kw!`, `var!`).
  - Feature flags: `bundled_sqlite3` (default), `sqlcipher`, `syncable` (pulls in Tolstoy).

Sync (optional, via `syncable`)
- tolstoy/ (mentat_tolstoy): Experimental log‑based sync engine with timeline support. Talks to a remote service (via hyper), maps local txs to a shared log, and merges remote changes.
  Reasoning: log‑centric approach improves reasoning about ordering and causality across devices without contaminating the storage core.
- tolstoy-traits/: Traits/errors for sync components so consumers can compile without pulling network code when sync is disabled.

FFI, Tools, SDKs
- ffi/: C/FFI surface (including Android glue) for embedding Mentat in non‑Rust environments.
- tools/cli/ (mentat_cli): A small CLI + REPL for interacting with the store; serves as a reference consumer and debugging tool.
- tools/mentatweb/: Prototype web server wrapper (Nickel) demonstrating a thin HTTP surface.
- sdks/android, sdks/swift/: Mobile wrappers/scaffolding oriented at embedding Mentat in apps.

Documentation, Tests, Fixtures
- docs/: Jekyll site for user/developer docs. Includes versioned API docs (`apis/0.7`, `latest` symlink).
- tests/: Integration tests covering API, cache behavior, pull, query, and sync.
- fixtures/: Sample EDN, schema scripts, and prebuilt SQLite DBs used in tests and migrations.

Automation & Development Tools
- automation/: Taskcluster and Docker configs for CI and release workflows.
- .travis.yml, .taskcluster.yml: CI definitions.
- build/version.rs: Enforces minimum rustc version at build time.
- .cargo/config: Handy aliases for running the CLI (`cargo cli`, `cargo debugcli`).
- scripts/: Build helpers for Android/iOS and docs.
- .vscode/: Workspace settings, tasks, and test runner configuration for contributors.



How the Pieces Fit (data and control flow)
------------------------------------------

1) Input and data model
   - Application code prepares EDN data and/or EDN‑shaped queries. Keywords and variables are constructed ergonomically (`kw!`, `var!`).

2) Transactions
   - The top‑level `mentat` crate exposes `Store` and `InProgress` to transact EDN values. 
   - The `mentat_transaction` crate helps map structured input to datoms. 
   - The `mentat_db` crate receives these and writes to SQLite, maintaining caches and schema views.

3) Queries
   - EDN queries are algebrized by `query-algebrizer` using the current schema into an algebraic plan.
   - The plan is translated to a `query-sql` structure, rendered to SQL by `sql`, and executed via rusqlite in `mentat_db`.
   - Result rows are turned into Datalog result shapes by `query-projector`; pull queries use `query-pull`.

4) Sync (optional)
   - If enabled, `tolstoy` tracks local transactions, communicates with a remote log, and merges remote updates back into the local store.



Dependency Highlights (local path dependencies)
-----------------------------------------------

The workspace uses path dependencies to maintain a layered architecture. A simplified build order (dependencies first):

```
edn -> sql_traits -> core_traits -> db_traits -> mentat_core -> query_algebrizer_traits -> query_pull_traits -> tolstoy_traits -> mentat_sql -> mentat_query_algebrizer -> mentat_db -> mentat_query_sql -> mentat_query_pull -> query_projector_traits -> mentat_query_projector -> public_traits -> mentat_transaction -> mentat_tolstoy -> mentat -> mentat_cli -> mentat_ffi
```

Key relationships:
- Most crates depend on `core_traits` and `edn` to share the same typed vocabulary.
- The `mentat_db` crate is the only place that touches SQLite directly; query crates produce/consume abstract SQL and values.
- Trait crates (`*_traits`, `public-traits`, `sql-traits`, `db-traits`, `tolstoy-traits`) define interfaces and errors to reduce coupling and allow optional compilation of heavy features.



Why these choices?
------------------

- Rust rewrite (from earlier Clojure/JS Datomish) 
  - Goals: smaller binaries, better performance, stronger type guarantees, better tooling, and mobile/Firefox embedding ease.

- Abstract query vs. storage schema
  - Datalog favors expressing intent at the domain level. By translating to SQL internally, Mentat shields queries from storage refactors.

- Open‑world schema and attribute vocabulary
  - Attributes (with types, cardinality, uniqueness, indexing) express domain rules. Some constraints are validated in code to keep the storage flexible and evolvable.

- Observers instead of DB triggers
  - Events are surfaced outside the DB to enable cleaner integrations, and to support sync/timeline features.

- Optional sync
  - Keeps core lean for apps that don’t need sync, while enabling a log‑based model for those that do.



Working in this Repo
--------------------

- Build everything: `cargo build`
- Run all tests: `cargo test --all`
- Work on a single crate: `cargo test -p mentat_query_algebrizer` (or `cargo build -p …`)
- CLI shortcuts: `.cargo/config` defines `cargo cli` and `cargo debugcli`.


At a Glance
-----------

- Use `mentat` for the API surface.
- Storage lives in `mentat_db`; EDN parsing in `edn`.
- Query is staged across `query-algebrizer` → `query-sql`/`sql` → `query-projector` and `query-pull`.
- Sync is optional (`tolstoy`).
- Traits crates define the interfaces that keep layers decoupled.


Status
------

- This project is archived/unmaintained. Expect to fork for new development, update dependencies, and make local decisions about optional features (e.g., whether to keep sync or the CLI).

