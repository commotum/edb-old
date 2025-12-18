Here’s the end-to-end flow for DataScript: from a Datalog query or pull to results, and from transactions to a new immutable DB value. This mirrors the Mentat map style and points to concrete namespaces/files in the vendored reference.

Query Flow

- Entrypoint
  - Public API: `datascript.core/q` delegates to the query engine: REFERENCE/datascript/src/datascript/core.cljc
  - Engine implementation: `datascript.query/q` (v2) and `datascript.query_v3/q` (new engine):
    - REFERENCE/datascript/src/datascript/query.cljc
    - REFERENCE/datascript/src/datascript/query_v3.cljc
- Parse → query IR
  - `datascript.parser/parse-query` parses EDN query forms (`:find/:in/:where/:with`, rules, pulls) into a structured map: REFERENCE/datascript/src/datascript/parser.cljc
- Build context and resolve sources/clauses
  - Each DB source (`$`, `$1`, …) and input variable gets resolved to in-memory relations: REFERENCE/datascript/src/datascript/query.cljc
  - Clauses `:where` are walked and resolved into Relations, joined by shared variables.
- Execute and collect
  - The engine evaluates clauses, joins relations, substitutes constants, and collects tuples for `:find` symbols (plus `:with`): REFERENCE/datascript/src/datascript/query_v3.cljc
- Post-process results
  - Aggregates: group and apply built-ins (min, max, count, distinct, etc.): REFERENCE/datascript/src/datascript/query.cljc
  - Result shapes: `FindRel`, `FindColl`, `FindScalar`, `FindTuple` are shaped accordingly.
  - Pull in :find: any `(pull ?e pattern)` is applied against the DB with `datascript.pull-api/pull-impl`: REFERENCE/datascript/src/datascript/pull_api.cljc

Pull Flow

- Entrypoint
  - Public API: `datascript.core/pull`, `datascript.core/pull-many`: REFERENCE/datascript/src/datascript/core.cljc
  - Implementation: REFERENCE/datascript/src/datascript/pull_api.cljc
- Parse pattern
  - `pull_parser` parses patterns into a PullPattern with attrs, reverse attrs, wildcard, recursion limits, aliases, and map-specs: REFERENCE/datascript/src/datascript/pull_parser.cljc
- Execute pull
  - Builds attribute lookups and walks the DB, returning nested plain maps (not entity views). Handles reverse refs (`:_ref`), components, recursion limits, wildcards, and aliases.

Transaction Flow

- Entrypoints
  - Immutable apply: `datascript.conn/with` and `datascript.conn/db-with` (pure, on DB value): REFERENCE/datascript/src/datascript/conn.cljc
  - Mutable apply to a connection: `datascript.conn/transact!` (updates an atom holding DB), `reset-conn!` (replace DB): REFERENCE/datascript/src/datascript/conn.cljc
- Apply tx-data
  - `db/transact-tx-data` validates tx-data, enforces schema (cardinality, uniqueness, refs, components), derives tempid allocations, produces new datoms/retirements and TxReport: REFERENCE/datascript/src/datascript/db.cljc
  - Schema is a plain map on `:schema` in DB, with flags like `:db/unique`, `:db/cardinality`, `:db/isComponent`, etc.
- Update indexes
  - Datoms are inserted/retracted across three persistent B-tree indices: EAVT, AEVT, AVET, maintained as `me.tonsky.persistent-sorted-set` structures on DB.
  - DB is immutable; a new DB value is returned with updated indexes and `:max-eid`/`:max-tx` increments.

Where Each Stage Lives

- Public API: REFERENCE/datascript/src/datascript/core.cljc
- Connections and transacting: REFERENCE/datascript/src/datascript/conn.cljc
- DB internals (datoms, indexes, schema, transact, as-of/since/filter): REFERENCE/datascript/src/datascript/db.cljc
- Query parsing: REFERENCE/datascript/src/datascript/parser.cljc
- Query engine(s): REFERENCE/datascript/src/datascript/query_v3.cljc, REFERENCE/datascript/src/datascript/query.cljc
- Pull: REFERENCE/datascript/src/datascript/pull_parser.cljc, REFERENCE/datascript/src/datascript/pull_api.cljc
- Entities (lazy entity views): REFERENCE/datascript/src/datascript/impl/entity.cljc
- Built-ins (query predicates/functions): REFERENCE/datascript/src/datascript/built_ins.cljc
- LRU cache: REFERENCE/datascript/src/datascript/lru.cljc
- Serialization/persistence (optional): REFERENCE/datascript/src/datascript/serialize.cljc, REFERENCE/datascript/src/datascript/storage.clj, REFERENCE/datascript/src/datascript/storage.cljs
- JS interop (exported API for CLJS consumers): REFERENCE/datascript/src/datascript/js.cljs

Typical Call Snippets

- Create a DB and transact (immutable and conn)
  - (def db (d/empty-db {:likes {:db/cardinality :db.cardinality/many}}))
  - (def db2 (:db-after (d/with db [[:db/add -1 :name "Ivan"]
                                     [:db/add -1 :likes "pizza"]])))
  - (def conn (d/create-conn {:name {:db/unique :db.unique/identity}}))
  - (d/transact! conn [[:db/add -1 :name "Oleg"]])
- Query
  - (d/q '[:find ?e :where [?e :name "Ivan"]] @conn)
  - (d/q '[:find ?name ?likes :in $ ?x :where [?e :name ?name] [?e :likes ?x] [?e :likes ?likes]] @conn :pizza)
- Pull
  - (d/pull @conn [:db/id :name {:friends [:db/id :name]}] 1)
  - (d/pull-many @conn [:db/id :name] [1 2 3])
-- Filtered DB view (cannot transact)
- Filtered DB view (cannot transact)
  - (def fdb (d/filter @conn (fn [db datom] (= (:a datom) :likes))))
  - (d/q '[:find ?e ?v :where [?e :likes ?v]] fdb)
-- Index lookups
- Index lookups
  - (d/datoms @conn :eavt 1)                ; all datoms for entity 1
  - (d/datoms @conn :aevt :likes)           ; all entities having :likes
  - (d/index-range @conn :age 18 60)        ; AVET slice by value range

Error Flow

- Query/pull parsing and execution
  - Malformed query forms, invalid pull patterns, arity/type mismatches raise ex-info from parser/query/pull layers.
- Transactions and DB rules
  - Schema/uniqueness/cardinality violations, lookup ref errors, and tempid misuse raise ex-info from `db/transact-tx-data`.
- Filtered DB guard
  - Attempting to transact against a filtered DB raises (ex-info "Filtered DB cannot be modified" {:error :transaction/filtered}) in `datascript.conn/with`.

--------------------------------------------------------------------------------

Section 1: Core API (datascript.core)

  ├── src/datascript/
  │   ├── core.cljc
  │   ├── conn.cljc
  │   ├── js.cljs

--------------------------------------------------------------------------------

Executive Summary

- Defines the public API surface for entities, pull, queries, DB creation, and connections.
- Re-exports:
  - Entities: `entity`, `entid`, `touch`, `entity-db` (lazy entity views over DB).
  - Pull: `pull`, `pull-many` (return plain Clojure maps, not entity views).
  - Query: `q` (delegates to query engine).
  - DB creation: `empty-db`, `init-db`, `datom`, `db?`, `datom?`.
  - Connections: `create-conn`, `conn-from-db`, `transact!`, `db-with`, `with`, `reset-conn!`, listeners (`listen!`, `unlisten!`).
- CLJS exports for JS interop: `datascript/js.cljs` provides `q`, `pull`, `pull_many`, etc.

Modules

- REFERENCE/datascript/src/datascript/core.cljc: Docs + thin wrappers to internal namespaces.
- REFERENCE/datascript/src/datascript/conn.cljc: Mutable connection wrapper over immutable DB with transacting and optional storage tail handling.
- REFERENCE/datascript/src/datascript/js.cljs: CLJS exports for consumers (e.g., foreign JS code).

Details

- Entities & Pull
  - `entity`, `entid`, `entity-db`, `touch` (lazy, cached entity views; reverse refs via `:_attr`; `touch` for eager fetch, debug-only).
  - `pull`, `pull-many` return plain maps; recursive selectors, reverse attrs, components, wildcards, aliases.
- Query
  - `q` delegates to query engine; supports `(pull ?e pattern)` in :find.
- DB creation & serialization
  - `empty-db` (opts: `:branching-factor`, `:ref-type`, optional `:storage`).
  - `init-db` (fast-path from trusted datoms; no validation).
  - `datom`, `db?`, `datom?` helpers.
  - `serializable` / `from-serializable` with customizable freeze/thaw fns.
  - JVM caveat: `serializable` holds a global lock; serializations don’t run in parallel.
- Filtered DB
  - `is-filtered`, `filter` create a view that proxies all reads and applies `(pred db datom)` to results.
  - Supports entities, pull, queries, index access; not cached; cannot be used with `with`/`db-with`.
- Changing DB (pure)
  - `with` returns TxReport for an immutable DB; `db-with` returns the new DB value.
  - `with-schema` updates schema without validation; must be compatible change.
- Index access
  - `datoms`, `find-datom`, `seek-datoms`, `rseek-datoms`, `index-range` over `:eavt|:aevt|:avet`.
  - `:avet` contains refs, `:db/unique`, or `:db/index` attrs only.
  - Iterators are lazy and efficient; support `first`, `next`, `reverse`, `seq`.
- Connections (see Section 6 for internals)
  - `conn?`, `create-conn`, `conn-from-db`, `conn-from-datoms`.
  - `transact!`, `reset-conn!`, `reset-schema!`, `listen!`, `unlisten!`.
  - CLJ: `restore-conn` rehydrates from storage (when available) and tracks a tx tail.
- Datomic compatibility
  - `tempid`, `resolve-tempid`, `db` (deref conn), `transact`, `transact-async` (compatibility-first APIs; prefer `@conn` and `transact!`).
- squuid utilities
  - `squuid`, `squuid-time-millis` for time-ordered UUIDs.
- Storage (CLJ only)
  - `storage`, `store`, `restore`, `addresses`, `collect-garbage`, `file-storage`.
- JS interop (CLJS)
  - Exports: `empty_db`, `init_db`, `serializable`, `from_serializable`, `q`, `pull`, `pull_many`, `db_with`, `entity`, `touch`, `entity_db`, `filter`, `is_filtered`, `create_conn`, `conn_from_db`, `conn_from_datoms`, `db`, `transact`, `reset_conn`, `listen`, `unlisten`, `resolve_tempid`, `datoms`, `seek_datoms`, `index_range`, `squuid`, `squuid_time_millis`.
  - Conversions: schema/entities keywordization, `js->Datom`, `tx-report->js`, `pull-result->js`.
  - JS `transact` calls internal `-transact!` then notifies listeners (mirrors CLJ behavior).

--------------------------------------------------------------------------------

Section 2: DB Internals (datascript.db)

  ├── src/datascript/
  │   ├── db.cljc
  │   ├── util.cljc
  │   ├── lru.cljc

--------------------------------------------------------------------------------

Executive Summary

- Core immutable DB value with:
  - Three persistent indices: EAVT, AEVT, AVET implemented via `me.tonsky.persistent-sorted-set`.
  - `Datom` type and accessors; rich comparators and slices for index scans.
  - Schema map with flags: `:db/unique`, `:db.cardinality/*`, `:db/isComponent`, ref types.
  - Transact pipeline (`transact-tx-data`): validates tx-data, applies adds/retracts, resolves lookup refs and tempids, updates indexes, returns TxReport.
  - Time/filters: as-of, since, and filtered DB views.
- Performance helpers: JIT-friendly defrecords/defn+, array-backed collections and tight loops.

Modules

- REFERENCE/datascript/src/datascript/db.cljc: Datom/DB definitions, indices, schema, transact and DB view operations.
- REFERENCE/datascript/src/datascript/util.cljc: Small utilities (logging, collections, predicates).
- REFERENCE/datascript/src/datascript/lru.cljc: Small LRU cache (used by query/pull parsing).

--------------------------------------------------------------------------------

Section 3: Query Parser & Engine

  ├── src/datascript/
  │   ├── parser.cljc
  │   ├── built_ins.cljc
  │   ├── query_v3.cljc
  │   └── query.cljc

--------------------------------------------------------------------------------

Executive Summary

- Parser: `parser.cljc` turns query forms into IR (symbols, sources, clauses, find specs, rules), handling pulls in :find, constants, and validation.
- Built-ins: logical and predicate functions namespace (`=`/`not`/aggregates, etc.).
- Engine v3: Newer, faster engine (`query_v3.cljc`) that builds relations, resolves clauses, and collects results with composable transducers; uses an LRU query cache.
- Engine v2: Legacy engine (`query.cljc`) with similar responsibilities and post-processing steps (aggregates, pull post-shaping).

Modules

- REFERENCE/datascript/src/datascript/parser.cljc: `parse-query`, find spec classes, and AST walkers.
- REFERENCE/datascript/src/datascript/query_v3.cljc: Core evaluation loop, relation joins, collectors, query cache.
- REFERENCE/datascript/src/datascript/query.cljc: Engine with explicit post-processing and pull integration.
- REFERENCE/datascript/src/datascript/built_ins.cljc: Built-in predicates and aggregates dispatch table.

Errors & Behavior

- Parser throws ex-info for malformed queries (invalid forms, duplicate vars, pull misuse). Engine throws for arity/type mismatches.

--------------------------------------------------------------------------------

Section 4: Pull (API, Parser, Execution)

  ├── src/datascript/
  │   ├── pull_api.cljc
  │   └── pull_parser.cljc

--------------------------------------------------------------------------------

Executive Summary

- Parser builds a `PullPattern` with attrs, reverse-attrs, wildcard, recursion and aliasing.
- Execution materializes nested attribute maps for one or many entities; supports component semantics, reverse refs, wildcard, `:db/id` inclusion.

Modules

- REFERENCE/datascript/src/datascript/pull_parser.cljc: `parse-pattern`, attr and map-spec parsing, reverse attrs, defaults.
- REFERENCE/datascript/src/datascript/pull_api.cljc: `pull`, `pull-many`, and internal `pull-impl` used from query engine.

--------------------------------------------------------------------------------

Section 5: Entities (Lazy Views)

  ├── src/datascript/
  │   └── impl/entity.cljc

--------------------------------------------------------------------------------

Executive Summary

- Entities are lazy, map-like views over DB; attributes are fetched on-demand and cached per-entity.
- Reverse refs via `:_attr`/`:ns/_attr`, component refs collapse to a single entity; entity equality is by id.

Modules

- REFERENCE/datascript/src/datascript/impl/entity.cljc

--------------------------------------------------------------------------------

Section 6: Connections & Listeners

  ├── src/datascript/
  │   └── conn.cljc

--------------------------------------------------------------------------------

Executive Summary

- `Conn` is an atom holding a DB and metadata (listeners, optional storage tail).
- `transact!` applies tx-data, updates the DB, notifies registered listeners with a TxReport.
- `db-with`/`with` perform pure transactions on immutable DB values.

Modules

- REFERENCE/datascript/src/datascript/conn.cljc

--------------------------------------------------------------------------------

Section 7: Persistence (Optional)

  ├── src/datascript/
  │   ├── storage.clj / storage.cljs
  │   └── serialize.cljc

--------------------------------------------------------------------------------

Executive Summary

- Optional storage interface for persisting B-tree nodes (`IStorage`), plus DB (de)serialization.
- `serialize.cljc` turns a DB into a compact, serializable representation (attrs catalog, eavt/aevt/avet arrays, keywords table) and back.
- `storage.clj/.cljs` implement adapters to stream B-tree nodes to/from storage, maintain root/tail pointers, and perform tail-compaction.

Modules

- REFERENCE/datascript/src/datascript/storage.clj, REFERENCE/datascript/src/datascript/storage.cljs
- REFERENCE/datascript/src/datascript/serialize.cljc

--------------------------------------------------------------------------------

Section 8: Utilities & Support

  ├── src/datascript/
  │   ├── util.cljc
  │   ├── lru.cljc
  │   └── js.cljs

--------------------------------------------------------------------------------

Executive Summary

- `util.cljc`: helpers (logging, predicate utilities, array helpers).
- `lru.cljc`: small generic LRU cache (used by query/pull caches).
- `js.cljs`: exports DataScript API to JS runtime for CLJS builds (q, pull, pull_many, entity, etc.).

Notes

- DataScript is in-memory by default; persistence is optional and primarily for CLJ.
- DB values are immutable; connections provide a mutable façade for convenience.
- Query/pull parsers and engines are shared between CLJ/CLJS via `.cljc` sources.



---

PROMPT:

I need you to read every file from the following sections in the @REFERENCE/datascript/ folder into the context. After you read the full contents of every file from a section into the context you should provide an executive summary of your understanding. Sound good? Make sure you employ a solid chunking/streaming read process so you can avoid truncation on large files. You need to fold this into DOCS/research/Datascript-Map.md, expand upon what's already there, correct any errors, and make sure it fully covers the contents.

---
