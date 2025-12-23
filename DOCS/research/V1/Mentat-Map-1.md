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



MORE
----

› looking at all of the subfolders in the REFERENCE/mentat/ subfolder can you help me understand kind of a mental model of what each one is? what are the traits folders? which
  folders have a README.md and which don't? Why is that? What's the common link there? is my understanding correct that the most important subfolders are db, edn, query-algebrizer,
  query-projector, sql, and transaction, but maybe I'm missing something?

    List mentat

• Ran python - << 'PY'


• Explored
  └ Search ^\[package\]|^name\s*=|^description\s*= in Cargo.toml
    Read README.md
    List public-traits
    List core-traits
    Read README.md, lib.rs, errors.rs
    List core
    Read lib.rs
    Read Cargo.toml
    Search ^(pub )?mod |^pub fn|struct |enum  in transaction
    Read README.md, lib.rs, errors.rs
    List automation
    Search . in automation
    List sdks
    List tools
    List cli

─ Worked for 2m 59s ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────

• Here’s a concise map of the Mentat workspace and how the pieces fit.

  Mental Model

  - src (REFERENCE/mentat/src): Main “mentat” crate that glues everything together into a public API (store, connection, transactions, queries).
  - core (REFERENCE/mentat/core): Core types and schema mechanics shared across the project (schema maps, SQL/typed value bridging).
  - db (REFERENCE/mentat/db): The storage engine on top of SQLite: bootstraps schema, transacts data, maintains caches, timelines, and watchers.
  - transaction (REFERENCE/mentat/transaction): The in-progress transaction and query execution context; builders, prepared queries, and planning.
  - edn (REFERENCE/mentat/edn): EDN parser and conversion into transactable forms (Mentat uses EDN for data and queries).
  - query-algebrizer (REFERENCE/mentat/query-algebrizer): Turns parsed Datalog/EDN queries into an algebrized form bound to the current schema and facts.
  - query-sql (REFERENCE/mentat/query-sql): Mentat’s structured representation of a SQL query (bridges the algebrizer output to SQL-building).
  - sql (REFERENCE/mentat/sql): A small SQL builder that turns the query representation into SQL text and bound parameters.
  - query-projector (REFERENCE/mentat/query-projector): Maps SQL row results into Datalog projections (find results) and shapes them for the caller.
  - query-pull (REFERENCE/mentat/query-pull): Implements Datomic-style pull expressions for fetching nested/graph-shaped entity data.
  - ffi (REFERENCE/mentat/ffi): FFI boundary (C ABI) used by Android/iOS SDKs.
  - tolstoy (REFERENCE/mentat/tolstoy): Sync prototype: log-based reconciliation of local and remote timelines.
  - Support/infrastructure:
      - core-traits, db-traits, public-traits, query-*-traits, sql-traits, tolstoy-traits: Lightweight interface/error crates used for decoupling (see below).
      - docs: Jekyll docs site; sdks: platform SDKs; tools/cli: CLI; automation, build, scripts: CI/Docker/build helpers; fixtures, tests: test data.

  Traits Crates

  - Purpose: Separate minimal interfaces and shared error types out of heavy crates to avoid cyclic deps, speed compile times, and allow feature-gated builds.
  - Examples:
      - core-traits (REFERENCE/mentat/core-traits): Fundamental shared types (e.g., value typing) used across crates.
      - db-traits (REFERENCE/mentat/db-traits): DB-layer errors and small interfaces.
      - query-algebrizer-traits, query-projector-traits, query-pull-traits (REFERENCE/mentat/query-*-traits): Error/result types and small interfaces for each query stage.
      - sql-traits (REFERENCE/mentat/sql-traits): SQL builder error types.
      - public-traits (REFERENCE/mentat/public-traits): Public Mentat error surface aggregating errors from all the above.
      - tolstoy-traits (REFERENCE/mentat/tolstoy-traits): Sync-related errors and interfaces.

  Which Subfolders Have README.md

  - Have README.md:
      - REFERENCE/mentat/README.md (top-level overview)
      - REFERENCE/mentat/db/README.md
      - REFERENCE/mentat/docs/README.md
      - REFERENCE/mentat/edn/README.md
      - REFERENCE/mentat/ffi/README.md
      - REFERENCE/mentat/query-algebrizer/README.md
      - REFERENCE/mentat/query-projector/README.md
      - REFERENCE/mentat/sql/README.md
      - REFERENCE/mentat/tolstoy/README.md
  - Do not have README.md:
      - Core/internal crates: core, transaction, query-sql, query-pull

  Why that pattern? READMEs appear where external orientation is most helpful: public-facing or conceptually big crates (db, edn, query-{algebrizer, projector}, sql builder, ffi,
  sync) and the top-level workspace. Traits crates and many internal building blocks are intentionally minimal and rely on inline docs and the top-level README’s “What are all of
  these crates?” section.

  Are These The Most Important?


• Explored
  └ Read query.rs
    Search parse_find_string|algebrize_with_inputs|AlgebraicQuery|Known|query_to_select|ProjectedSelect|to_sql_query|SQLQuery|projector\.project in mentat
    Search struct SQLQuery|impl .*SQLQuery|to_sql_query\( in query-sql
    Read lib.rs
    Read lib.rs
    Search mod translate|fn query_to_select|enum ProjectedSelect in query-projector
    Read translate.rs
    Read translate.rs
    Search ^pub fn parse_find_string\(|^pub fn algebrize_with_inputs\( in query-algebrizer
    Read lib.rs
    Read lib.rs
    Read lib.rs
    Search pub enum QueryOutput|pub enum QueryResults|struct ConstantProjector|trait Projector|pub type Result<|project\( in query-projector
    Read mod.rs
    Search struct SQLQuery|struct QueryBuilder|SQLiteQueryBuilder|push_sql\( in sql
    Read lib.rs
    Search pub fn pull_attributes_for_entity\( in lib.rs
    Read lib.rs
    Search pull_attributes_for_entity\( in lib.rs
    Search pub fn pull\(|struct Puller|TwoStagePull|prepare\( in query-pull
    Read lib.rs

─ Worked for 1m 20s ───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────

• Here’s the end-to-end flow from an EDN Datalog query to results, plus how pull works.

  Find Query Flow

  - Entrypoint
      - Call q_once/q_prepare from the public API (re-exported): REFERENCE/mentat/src/lib.rs:1
      - Implementation lives in mentat_transaction::query:
          - q_once: REFERENCE/mentat/transaction/src/query.rs:366
          - q_prepare: REFERENCE/mentat/transaction/src/query.rs:391
  - Parse EDN into a FindQuery
      - parse_find_string uses mentat_core::parse_query to parse EDN text: REFERENCE/mentat/query-algebrizer/src/lib.rs:399
  - Algebrize (bind to schema, check inputs)
      - algebrize_with_inputs turns the parsed query into an AlgebraicQuery by walking where-clauses, deriving types, pruning, and validating order/limit: REFERENCE/mentat/query-
        algebrizer/src/lib.rs:274
      - AlgebraicQuery carries the FindSpec, ConjoiningClauses, order/limit, and can short-circuit emptiness: REFERENCE/mentat/query-algebrizer/src/lib.rs:340
  - Early exit cases
      - Known-empty queries skip SQL entirely and return an empty QueryOutput: REFERENCE/mentat/transaction/src/query.rs:335
      - Known-constant queries project without rows: REFERENCE/mentat/transaction/src/query.rs:342
  - Translate to a SQL-shaped plan + projector
      - query_to_select(schema, algebrized) yields either:
          - ProjectedSelect::Constant(ConstantProjector), or
          - ProjectedSelect::Query { query: SelectQuery, projector }
      - Implementation (SQL shape + projector choice) is in the projector translator:
          - REFERENCE/mentat/query-projector/src/translate.rs:495
          - Enum ProjectedSelect: REFERENCE/mentat/query-projector/src/translate.rs:253
  - Build SQL text + bound args
      - SelectQuery::to_sql_query() uses the SQL builder to emit a SQLQuery { sql, args }:
          - REFERENCE/mentat/query-sql/src/lib.rs:647
      - The builder (SQLiteQueryBuilder) applies SQLite-specific quoting, parameter names, and value serialization: REFERENCE/mentat/sql/src/lib.rs:86, REFERENCE/mentat/sql/src/
        lib.rs:101, REFERENCE/mentat/sql/src/lib.rs:151
  - Execute with rusqlite
      - Prepare statement, bind args, and step rows:
          - q_once path: REFERENCE/mentat/transaction/src/query.rs:351
          - q_prepare path stores statement, args, projector: REFERENCE/mentat/transaction/src/query.rs:421
  - Project rows into Datalog results
      - The Projector (chosen earlier) reads Rows, converts SQLite values to TypedValue (via schema + mentat_db conversions), and shapes them per FindSpec:
          - Trait method: REFERENCE/mentat/query-projector/src/projectors/mod.rs:23
      - Return QueryOutput { spec, results }, then helpers convert to scalar/tuple/coll/rel forms:
          - IntoResult helpers on QueryExecutionResult: REFERENCE/mentat/transaction/src/query.rs:312
  - Notes on caching and inputs
      - Known { schema, cache } provides an optional attribute cache that the algebrizer/translator consult to pruning or avoid SQL in some cases: REFERENCE/mentat/query-algebrizer/
        src/lib.rs:316 and REFERENCE/mentat/query-algebrizer/src/lib.rs:274

  Pull Flow

  - Embedded pull in :find
      - If the query uses (pull …), the translator selects a two-stage projector (fetch rows, then pull nested data) such as ScalarTwoStagePullProjector, RelTwoStagePullProjector,
        etc.: REFERENCE/mentat/query-projector/src/projectors/mod.rs:41
      - The first stage is a regular SQL query; the second stage uses the pull engine to fetch attributes for the bound entity IDs.
  - Direct pull API
      - Prepare a Puller and fetch a map of entity → structured map:
          - Puller::prepare: REFERENCE/mentat/query-pull/src/lib.rs:156
          - pull_attributes_for_entities: REFERENCE/mentat/query-pull/src/lib.rs:127
      - The puller iteratively fetches attribute layers using the schema and DB connection, shaping results into nested maps keyed by attribute keywords.

  Where Each Stage Lives

  - Parsing: mentat_core::parse_query (re-export), invoked from query-algebrizer parse wrapper: REFERENCE/mentat/query-algebrizer/src/lib.rs:399
  - Algebrizer: REFERENCE/mentat/query-algebrizer/src/lib.rs:274
  - Translator (to SQL shape + projector): REFERENCE/mentat/query-projector/src/translate.rs:495
  - SQL representation: REFERENCE/mentat/query-sql/src/lib.rs:1
  - SQL builder (text + bindings): REFERENCE/mentat/sql/src/lib.rs:46
  - Execution wrapper + prepared queries: REFERENCE/mentat/transaction/src/query.rs:366
  - Projectors (row → Datalog): REFERENCE/mentat/query-projector/src/projectors/mod.rs:23
  - Pull engine: REFERENCE/mentat/query-pull/src/lib.rs:141

  Typical Call Snippets

  - One-shot query: q_once(conn, Known::new(&schema, cache_opt), "[:find ?e :where [?e :person/name \"Alice\"]]", None) → QueryOutput (see REFERENCE/mentat/transaction/src/
    query.rs:366)
  - Prepared query: q_prepare(conn, Known::new(&schema, cache_opt), q, inputs)?.run(None) (see REFERENCE/mentat/transaction/src/query.rs:391)
  - Pull: Puller::prepare(&schema, attrs)?.pull(&schema, &conn, entities) (see REFERENCE/mentat/query-pull/src/lib.rs:156)

  If you want, I can annotate a concrete example query end-to-end and show the generated SQL and the projector shape produced.
