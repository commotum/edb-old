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
  - Filters: filtered DB views via a `FilteredDB` wrapper.
- Performance helpers: JIT-friendly defrecords/defn+, array-backed collections and tight loops.

Modules

- REFERENCE/datascript/src/datascript/db.cljc: Datom/DB definitions, indices, schema, transact and DB view operations.
- REFERENCE/datascript/src/datascript/util.cljc: Small utilities (logging, collections, predicates).
- REFERENCE/datascript/src/datascript/lru.cljc: Small LRU cache (used by query/pull parsing).

Details

- Types and printing
  - `Datom` (fields: `e`, `a`, `v`, `tx`, plus internal `idx`/`_hash`). `datom` constructor and `datom?` predicate.
  - Hashing/equality/lookup/indexed implemented for both CLJ/CLJS; `datom-tx` returns absolute tx, `datom-added` true when addition.
  - Readers/printers: `datom-from-reader` for `#datascript/Datom [...]`; DB printers/readers (`db-from-reader`).
- Comparators and indices
  - Comparators: `cmp-datoms-eavt|aevt|avet` and faster `*-quick` variants; used by B-tree indices.
  - Index API: `-datoms`, `-seek-datoms`, `-rseek-datoms`, `-index-range` via `IIndexAccess`; `find-datom` helper.
  - `:avet` requires `:db/index` (or implied by `:db/unique`/ref/tuple); validated by `validate-indexed`.
- DB structure
  - `DB` record: `schema`, `rschema` (derived properties), `eavt`, `aevt`, `avet`, `max-eid`, `max-tx`, LRU caches (`pull-patterns`, `pull-attrs`), hash atom.
  - Builders: `empty-db` (fresh sorted sets), `init-db` (from trusted datoms; sorts arrays to sets; computes `max-eid`/`max-tx`), `restore-db`.
  - Transient/persistent helpers for indices; hash caching and `clojure.data/Diff` support.
- Filtered DB
  - `FilteredDB` wraps a base DB and proxies reads (`ISearch`, `IIndexAccess`, `IDB`), applying a predicate to results; cannot be mutated.
- Schema model and validation
  - `rschema` derives sets: `:db/unique`, `:db.unique/{identity,value}`, `:db/index`, `:db.cardinality/many`, `:db.type/ref`, `:db/isComponent`, `:db.type/tuple`; plus `:db/attrTuples` mapping tuple sources to tuples.
  - Validation:
    - `:db/isComponent true` requires `:db/valueType :db.type/ref`.
    - `:db/unique` ∈ `{ :db.unique/value, :db.unique/identity }`.
    - `:db/valueType` supports `:db.type/ref` and `:db.type/tuple` here.
    - `:db/cardinality` ∈ `{ :db.cardinality/one, :db.cardinality/many }`.
    - `:db/tupleAttrs` must be a non-empty sequential; cannot reference tuple attrs or cardinality many attrs; tuple attrs imply `:db/index`.
- Entity id resolution
  - `entid` resolves: positive numbers, lookup refs `[unique-attr value]` (including tuple attrs), and keywords via `:db/ident`.
  - `entid-strict` throws on missing; `numeric-eid-exists?` helper.
- Transactions pipeline
  - `TxReport` fields: `:db-before`, `:db-after`, `:tx-data`, `:tempids`, `:tx-meta`.
  - Tempids: negative ints, strings, and `AutoTempid`; tracked in `:tempids` (plus reverse mapping for upsert conflict checks).
  - Prepass: `assoc-auto-tempids` injects `:db/id` or nested tempids; supports reverse attrs and nested entity maps.
  - Operations:
    - `:db/add` adds or overwrites attribute (multi-valued adds accumulate); uniqueness enforced by `validate-datom` on adds.
    - `:db/retract` removes a specific datom; `:db.fn/retractAttribute` removes all datoms for an attr; `:db.fn/retractEntity` removes entity and inbound refs.
    - `:db.fn/cas`/`:db/cas` compare-and-set against current value(s) (multi-valued and single cases differ).
    - Tuple attrs cannot be directly modified unless the full tuple matches current DB values; tuples are queued and flushed via `::queued-tuples` and `flush-tuples`.
    - Component refs (`:db/isComponent`) trigger cascading retracts.
    - Transaction id use (`:db/current-tx` alias) auto-allocates the current tx entity id when used as `e` or `v` in refs.
  - Upserts: identity attrs may route an entity to an existing eid; `resolve-upserts`/`validate-upserts` enforce consistency or raise conflicts.
  - Value tempids: detects tempids used only as values and errors unless resolved by added datoms.
  - Finalization: sets `:db/current-tx` in `:tempids`, increments `:max-tx`, strips `AutoTempid` keys from `:tempids`.
- Errors (ex-info data)
  - Schema: `:schema/validation`.
  - Index access: `:index-access`.
  - Lookup refs and entity ids: `:lookup-ref/syntax`, `:lookup-ref/unique`, `:entity-id`, `:entity-id/syntax`, `:entity-id/missing`.
  - Transact: `:transact/syntax`, `:transact/unique`, `:transact/cas`, `:transact/upsert`.
- Utilities and caches
  - `datascript.util`: `raise`, `log`, `cond+`/`if+`, `squuid`, `squuid-time-millis`, `distinct-by`, `find`, `single`, `concatv`, `zip`, `removem`, `conjv`, `conjs`, `reduce-indexed`.
  - `datascript.lru`: `LRU` map and `ICache` with `cache` for small, hot-path caches (used for pull pattern/attrs caches in `DB`).

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
- Engine v2 (default): `datascript.query/q` is used by `datascript.core/q`; handles rules, aggregates, pull, result shaping, and caching.
- Engine v3 (alternative): `datascript.query_v3/q` is an optimized engine with array-backed relations and fast joins; not used by default.

Modules

- REFERENCE/datascript/src/datascript/parser.cljc: `parse-query`, find spec classes, and AST walkers.
- REFERENCE/datascript/src/datascript/query_v3.cljc: Core evaluation loop, relation joins, collectors, query cache.
- REFERENCE/datascript/src/datascript/query.cljc: Engine with explicit post-processing and pull integration.
- REFERENCE/datascript/src/datascript/built_ins.cljc: Built-in predicates and aggregates dispatch table.

Errors & Behavior

- Parser throws ex-info for malformed queries (invalid forms, duplicate vars, pull misuse). Engine throws for arity/type mismatches.

Details

- Parser IR (parser.cljc)
  - Symbols: `Placeholder` (`_`), `Variable` (`?v`), `SrcVar` (`$db`), `RulesVar` (`%`), `Constant` (non-var), `PlainSymbol` (other symbols).
  - Bindings: `BindIgnore` (`_`), `BindScalar` (`?v`), `BindTuple` (`[b1 b2 ...]`), `BindColl` (`[b ...]`), `BindRel` (`[[...]]`).
  - :find: `FindRel` (one or more), `FindColl` (`[elem ...]`), `FindScalar` (`elem .`), `FindTuple` (`[elem ...]`). Elements: `Variable`, `Pull` (`[pull $ ?e pattern]`), `Aggregate` (built-in or custom via `['aggregate' ?fn arg+]`).
  - Return maps: `:keys`, `:syms`, `:strs` validated against :find arity/shape; not supported for scalar/coll finds.
  - :with: additional vars to include in uniqueness/grouping but not returned unless in :find.
  - :in: list of bindings/sources; supports `src-var` (`$`), `rules-var` (`%`), plain symbols, and binding forms. If any clause uses a source, a default `$` is auto-supplied.
  - :where clauses: data patterns, predicates `[(pred args...)]`, functions with binding `[(fun args...) binding]`, rules `[rule-name args...]`, `not`, `not-join`, `or`, `or-join`, `and`.
  - Rules: branches grouped by name; each has head `[name rule-vars]` and clauses; required/free vars must be consistent across branches; arity validated.
  - Validation highlights: distinctness for :in/:with; `:find ∪ :with ⊆ :where ∪ :in`; only one of `:keys/:syms/:strs`; `or`/`or-join` free var checks; `not`/`not-join` join vars non-empty; missing `%` when rules are present.

- Built-ins (built_ins.cljc)
  - Predicates/functions: arithmetic, comparisons via `db/value-compare`, booleans (`and`, `or`), regex, strings, collections, entity helpers: `-differ?`, `-get-else`, `-get-some`, `missing?`, `ground`, `tuple` (vector), `untuple`.
  - Aggregates: `sum`, `avg`, `median`, `variance`, `stddev`, `distinct` (set), `min`/`max` (optionally top N), `rand` (optionally N), `sample`, `count`, `count-distinct`.

- Engine v2 (query.cljc)
  - Context with `rels`, `sources`, `rules`; parsed queries cached (LRU).
  - Inputs bound via binding forms to relations; sources (`$`) and rules (`%`) resolved.
  - Patterns: lookup-ref resolution (`entid-strict`), constant substitution, DB search or collection match.
  - Predicates/functions: functions from built-ins/context/resolved symbols; tuple-level filter or binding to new vars.
  - Rules: rule expansion with guards (`-differ?`), depth-first solve respecting required/free vars.
  - Logical forms: `and` reduce; `or`/`or-join` union branches; `not`/`not-join` subtract branch results with binding checks.
  - Post: aggregates (group-by non-aggregate elements), `pull` elements via `pull_api`, then shape results per find class and optional return-map.
  - Errors: `:query/inputs`, `:query/where`, `:query/binding` for arity, unknown predicates/functions, insufficient bindings.

- Engine v3 (query_v3.cljc)
  - Array-backed `IRelation` implementations, fast key extraction, hash-join; inputs converted via `bind` (scalars become constants if single-row).
  - Patterns/predicates/logic similar in spirit; rules are not implemented here; returns native set of vectors for `:find`+`:with`.
  - Caches parsed queries in an LRU of size 100.

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

Details

- Pattern grammar (pull_parser.cljc)
  - Pattern: sequence of items: attr-spec, map-spec, or wildcard (`*`, `"*"`, or `:*`).
  - Attr-spec forms:
    - `attr-name`: keyword or string naming an attribute; may be reverse (e.g. `:_friend`).
    - `attr-expr`: `[attr-name :as k :limit n|nil :default v :xform sym-or-fn]+`.
    - Legacy: `['limit attr-name n|nil]`, `['default attr-name v]`.
  - Map-spec: `{attr-spec (pattern | recursion-limit)}`; recursion-limit is positive number or `'...` (unbounded).
  - Attribute metadata derived from schema: `:ref?`, `:component?`, `:multival?`. Reverse names validated to be refs.
  - Defaults:
    - `:limit` defaults to 1000 for multival attrs; only allowed on `:db.cardinality/many` attrs.
    - `:xform` defaults to `identity`; resolved from built-ins or (CLJ) requiring-resolve of namespaced symbol.
    - For ref attrs without explicit subpattern: default is `[:db/id]`; for component refs: default is wildcard of the target entity.
    - When wildcard is present in a pattern and `:db/id` is not explicitly included, `:db/id` is auto-inserted.
  - Pattern normalization: attributes are sorted; `PullPattern` stores `:first-attr`/`:last-attr` for efficient index scans and holds forward attrs and `:reverse-attrs` separately.

- Execution model (pull_api.cljc)
  - Public API: `pull` and `pull-many` accept optional `{:visitor f}`. Visitor receives four args for each touch:
    - `(:db.pull/attr e a nil)` when scanning a forward attr
    - `(:db.pull/wildcard e nil nil)` when encountering wildcard expansion
    - `(:db.pull/reverse nil a v)` when scanning a reverse attr
  - Engine: stack of frames implementing `IFrame`:
    - `AttrsFrame`: walks forward attrs, pairs them with datoms, applies `:default` when missing, and `:xform` on values (including `nil` if no datom).
    - `MultivalAttrFrame`: collects scalar multi-valued attributes with `:limit` enforcement.
    - `MultivalRefAttrFrame`: collects ref multi-valued attributes and spawns nested frames per id.
    - `ReverseAttrsFrame`: walks `:reverse-attrs` after forward pass, scanning inbound references (uses `:avet` on DB when available).
    - `ResultFrame`: carries a completed value plus any pending datoms for merging.
  - Recursion and cycles:
    - For recursive or auto-expanding component refs with wildcard subpattern, the engine tracks `seen` ids to break cycles and `recursion-limits` per attribute.
    - Unbounded recursion via `'...` or bounded via a positive integer; when limit hits 0, traversal for that attr stops.
  - Index use: on `DB`, uses `:eavt` for forward and `:avet` for reverse scans with narrow slices; otherwise falls back to `db/-search`/`-seek-datoms`.
  - Wildcard expansion: for unmatched datoms before the next explicit attr, dynamically parses the datom’s attribute into a `PullAttr` (cached via LRU) and emits it.
  - Merge rules: alias `:as` is the output key; `:default` is used when no datom; `:xform` is applied to values (including `nil`).

--------------------------------------------------------------------------------

Section 5: Entities (Lazy Views)

  ├── src/datascript/
  │   └── impl/entity.cljc

--------------------------------------------------------------------------------

Executive Summary

- Entities are lazy, map-like views over DB; attributes are fetched on-demand and cached per-entity.
- Reverse refs via `:_attr`/`:ns/_attr`, component refs collapse to a single entity; entity equality is by the pair (DB identity, entity id).

Modules

- REFERENCE/datascript/src/datascript/impl/entity.cljc

Details

- Construction and identity
  - `datascript.impl.entity/entity db eid` resolves `eid` (number, lookup-ref, or keyword `:db/ident`) and returns an `Entity` only if the numeric eid exists in DB.
  - Equality and hashing use both DB identity and eid; entities from different DB values or versions are not equal even with the same eid.
- Read-only, map-like interface
  - Implements associative lookup (`(:attr e)` / `(get e :attr not-found)`), `IFn` call syntax `(e :attr)`, `ILookup`, `ISeqable`, `ICounted`.
  - Unsupported mutations: `assoc`, `cons`, `empty` throw.
  - Printing and seq/count expose only cached attributes; `touch` populates cache first.
- Attribute value shapes
  - Cardinality many scalars → set of values.
  - Cardinality many refs → set of Entities.
  - Single ref → Entity; single scalar → value.
  - Reverse refs (`:_attr`/`:ns/_attr`): set of Entities unless `:db/isComponent`, in which case a single Entity.
  - Special key `:db/id` returns eid.
- Caching and `touch`
  - First lookup for a forward attr queries DB and caches the result in the entity-local cache; subsequently returned from cache.
  - `touch` eagerly loads all forward attrs via `db/-search [eid]`, partitions by attr, computes values with the same shape rules, and recursively touches component refs.
  - Reverse refs are computed on demand and are not cached on the entity.
- JS interop (CLJS)
  - Entities behave as JS Maps: `keys()`, `entries()`, `values()`, `has(k)`, `get(k)`; multival results are converted to arrays for JS.
  - `get(":db/id")` returns eid; reverse get uses `"_attr"` naming; iteration yields currently cached attributes (use `touch` to prefill).

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

Details

- Conn representation
  - `Conn` wraps an internal atom map (via `extend-clj.core/deftype-atom`) and derefs to the current `:db` value. Swap/CAS update `:db` in the internal map.
  - `conn?` returns true for derefable values that either contain a DB or are nil.
- Creating connections
  - `create-conn` creates a conn backed by an empty DB (with optional `schema` and `opts` adapted by storage layer); `conn-from-db` wraps an existing DB; `conn-from-datoms` initializes DB via `init-db` and wraps it.
  - When a DB has storage attached, `conn-from-db` persists it immediately and initializes `:tx-tail []` and `:db-last-stored` for incremental persistence.
  - CLJ: `restore-conn` reads a DB and pending tail from storage and builds a conn whose `:db` is `db-with-tail` of the restored pair.
- Pure DB changes
  - `with` executes a transaction against an immutable DB value and returns a TxReport. On `FilteredDB`, throws `(ex-info "Filtered DB cannot be modified" {:error :transaction/filtered})`.
  - `db-with` returns `(:db-after (with db tx-data))`.
- Transacting
  - `transact!` locks on the conn, runs an internal `-transact!` that swaps `:db` to `:db-after` from the TxReport, then notifies all registered listeners with the same report value.
  - TxReport shape: `{:db-before … :db-after … :tx-data […] :tempids {...} :tx-meta tx-meta}`.
  - Storage (CLJ): if a conn has storage, each tx appends datoms to `:tx-tail`. When the tail’s total datom count exceeds the index `:branching-factor`, it flushes the full DB (`store-impl!`), resets `:tx-tail`, and updates `:db-last-stored`; otherwise it writes the incremental tail (`store-tail`).
- Reset and schema changes
  - `reset-conn!` replaces the current DB with a provided DB and emits a TxReport that retracts all old datoms and inserts all new ones. Storage-backed conns persist the new DB and reset the tail.
  - `reset-schema!` applies `db/with-schema`; on CLJ with storage, persists the schema change and clears the tail.
- Listeners API
  - `listen!` registers a callback under a key (if none provided, generates one); idempotent per key. `unlisten!` removes it.
  - Listeners receive TxReports after the conn has been updated. Order follows iteration over the internal map.
- Thread-safety and gotchas
  - `transact!` uses `locking` for serialized updates and callbacks.
  - Avoid transacting on filtered DBs; use `with`/`db-with` on base DBs or filter views for read-only queries.

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

Details

- IStorage protocol (storage.clj)
  - Methods: `-store [[addr data] ...]`, `-restore addr`, `-list-addresses`, `-delete addrs-seq`.
  - Addresses are 64‑bit integers. Two reserved addresses: root (0) and tail (1).
  - Default implementation: `file-storage dir` writes each address to a file; configurable via options:
    - `:freeze-fn` / `:thaw-fn` for values, or low-level `:write-fn` / `:read-fn` over streams
    - `:addr->filename-fn` / `:filename->addr-fn` for path mapping
- Storage adapter and persistence
  - `StorageAdapter` bridges persistent-sorted-set’s `IStorage` to an external `IStorage` by serializing nodes as EDN-like maps:
    - Node data: `{:level n, :keys [[e a v tx] ...], :addresses [...]}` (addresses only for branches)
  - `store-impl!` serializes `:eavt/:aevt/:avet` roots to addresses and writes a root meta:
    - `{:schema, :max-eid, :max-tx, :eavt, :aevt, :avet, :max-addr, <settings>}` and resets tail to an empty vector
  - `store` persists a DB to its existing storage or to a provided one (guards against switching storage mid-life)
  - `store-tail` appends a vector of tx datom groups to the tail address (used for incremental persistence)
  - `restore-impl` loads root and tail, rebuilds indices via `set/restore-by` using the adapter, and returns `[db tail]`
  - `db-with-tail` replays tail groups on the DB (with `db/with-datom`) and bumps `:max-tx` after each group
  - `restore` convenience returns a DB with tail applied
- Addresses and GC
  - `addresses` walks all nodes in all three indices of given DBs and includes root/tail; returns the set of in-use addresses
  - `collect-garbage` loads the current DB (to protect it), discovers all addresses in use (including weakly referenced DBs), and deletes any others via `-delete`
  - Weak references are kept for previously stored DBs to assist GC discovery
- CLJS stubs
  - `storage.cljs` is a placeholder in CLJS (persistence is CLJ‑only)

- Serialization format (serialize.cljc)
  - serializable(db, opts): returns a platform‑neutral data structure with keys:
    - `"count"`, `"tx0"`, `"max-eid"`, `"max-tx"`, `"schema"` (via `:freeze-fn`, default `pr-str`/edn),
      `"attrs"` (attribute keywords table), `"keywords"` (other keywords table),
      `"eavt"` (array of [e a-idx v tx-offset]), `"aevt"`/`"avet"` (arrays of indices pointing into `eavt`), and (CLJ) `"branching-factor"`, `"ref-type"` from settings
  - Values encoding: numbers/strings/booleans inlined; special markers for `##Inf`/`##-Inf`/`##NaN`; keywords use indexes; other types via `:freeze-fn`
  - from-serializable(data, opts): reconstructs datoms and indexes, thaws schema/keywords, and builds a DB via `db/restore-db`; merges optional settings `:branching-factor`/`:ref-type`
  - JVM caveat: serializable uses a global lock to serialize one DB at a time

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
