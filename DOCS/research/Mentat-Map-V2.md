Here’s the end-to-end flow from an EDN Datalog query to results, plus how pull works.

Find Query Flow

- Entrypoint
    - Call q_once/q_prepare from the public API (re-exported): REFERENCE/mentat/src/
    lib.rs:1
    - Implementation lives in mentat_transaction::query:
        - q_once: REFERENCE/mentat/transaction/src/query.rs:366
        - q_prepare: REFERENCE/mentat/transaction/src/query.rs:391
- Parse EDN into a FindQuery
    - parse_find_string uses mentat_core::parse_query to parse EDN text: REFERENCE/
    mentat/query-algebrizer/src/lib.rs:399
- Algebrize (bind to schema, check inputs)
    - algebrize_with_inputs turns the parsed query into an AlgebraicQuery by walking
    where-clauses, deriving types, pruning, and validating order/limit: REFERENCE/
    mentat/query-algebrizer/src/lib.rs:274
    - AlgebraicQuery carries the FindSpec, ConjoiningClauses, order/limit, and can
    short-circuit emptiness: REFERENCE/mentat/query-algebrizer/src/lib.rs:340
- Early exit cases
    - Known-empty queries skip SQL entirely and return an empty QueryOutput: REFERENCE/
    mentat/transaction/src/query.rs:335
    - Known-constant queries project without rows: REFERENCE/mentat/transaction/src/
    query.rs:342
- Translate to a SQL-shaped plan + projector
    - query_to_select(schema, algebrized) yields either:
        - ProjectedSelect::Constant(ConstantProjector), or
        - ProjectedSelect::Query { query: SelectQuery, projector }
    - Implementation (SQL shape + projector choice) is in the projector translator:
        - REFERENCE/mentat/query-projector/src/translate.rs:495
        - Enum ProjectedSelect: REFERENCE/mentat/query-projector/src/translate.rs:253
- Build SQL text + bound args
    - SelectQuery::to_sql_query() uses the SQL builder to emit a SQLQuery { sql,
    args }:
        - REFERENCE/mentat/query-sql/src/lib.rs:647
    - The builder (SQLiteQueryBuilder) applies SQLite-specific quoting, parameter
    names, and value serialization: REFERENCE/mentat/sql/src/lib.rs:86, REFERENCE/
    mentat/sql/src/lib.rs:101, REFERENCE/mentat/sql/src/lib.rs:151
- Execute with rusqlite
    - Prepare statement, bind args, and step rows:
        - q_once path: REFERENCE/mentat/transaction/src/query.rs:351
        - q_prepare path stores statement, args, projector: REFERENCE/mentat/
        transaction/src/query.rs:421
- Project rows into Datalog results
    - The Projector (chosen earlier) reads Rows, converts SQLite values to TypedValue
    (via schema + mentat_db conversions), and shapes them per FindSpec:
        - Trait method: REFERENCE/mentat/query-projector/src/projectors/mod.rs:23
    - Return QueryOutput { spec, results }, then helpers convert to scalar/tuple/coll/
    rel forms:
        - IntoResult helpers on QueryExecutionResult: REFERENCE/mentat/transaction/src/
        query.rs:312
- Notes on caching and inputs
    - Known { schema, cache } provides an optional attribute cache that the algebrizer/
    translator consult to pruning or avoid SQL in some cases: REFERENCE/mentat/query-
    algebrizer/src/lib.rs:316 and REFERENCE/mentat/query-algebrizer/src/lib.rs:274

Pull Flow

- Embedded pull in :find
    - If the query uses (pull …), the translator selects a two-stage projector (fetch
    rows, then pull nested data) such as ScalarTwoStagePullProjector,
    RelTwoStagePullProjector, etc.: REFERENCE/mentat/query-projector/src/projectors/
    mod.rs:41
    - The first stage is a regular SQL query; the second stage uses the pull engine to
    fetch attributes for the bound entity IDs.
- Direct pull API
    - Prepare a Puller and fetch a map of entity → structured map:
        - Puller::prepare: REFERENCE/mentat/query-pull/src/lib.rs:156
        - pull_attributes_for_entities: REFERENCE/mentat/query-pull/src/lib.rs:127
    - The puller iteratively fetches attribute layers using the schema and DB
    connection, shaping results into nested maps keyed by attribute keywords.

Where Each Stage Lives

- Parsing: mentat_core::parse_query (re-export), invoked from query-algebrizer parse
wrapper: REFERENCE/mentat/query-algebrizer/src/lib.rs:399
- Algebrizer: REFERENCE/mentat/query-algebrizer/src/lib.rs:274
- Translator (to SQL shape + projector): REFERENCE/mentat/query-projector/src/
translate.rs:495
- SQL representation: REFERENCE/mentat/query-sql/src/lib.rs:1
- SQL builder (text + bindings): REFERENCE/mentat/sql/src/lib.rs:46
- Execution wrapper + prepared queries: REFERENCE/mentat/transaction/src/query.rs:366
- Projectors (row → Datalog): REFERENCE/mentat/query-projector/src/projectors/mod.rs:23
- Pull engine: REFERENCE/mentat/query-pull/src/lib.rs:141

Typical Call Snippets

- One-shot query: q_once(conn, Known::new(&schema, cache_opt), "[:find ?e :where [?
e :person/name \"Alice\"]]", None) → QueryOutput (see REFERENCE/mentat/transaction/
src/query.rs:366)
- Prepared query: q_prepare(conn, Known::new(&schema, cache_opt), q, inputs)?.run(None)
(see REFERENCE/mentat/transaction/src/query.rs:391)
- Pull: Puller::prepare(&schema, attrs)?.pull(&schema, &conn, entities) (see REFERENCE/
mentat/query-pull/src/lib.rs:156)

If you want, I can annotate a concrete example query end-to-end and show the generated
SQL and the projector shape produced.

--------------------------------------------------------------------------------

Section 1:
 
  ├── src/
  │   ├── conn.rs
  │   ├── lib.rs
  │   ├── query_builder.rs
  │   ├── store.rs
  │   ├── sync.rs
  │   └── vocabulary.rs

--------------------------------------------------------------------------------

Executive Summary

- Provides a high‑level Rust API for a SQLite‑backed, Datomic‑style store with schema,
queries, transactions, caching, pull, vocabulary management, and optional sync.
- Conn holds thread‑safe store metadata (schema, partition map, attribute cache) and
exposes query, pull, transactions, caching, and transaction observers.
- Store wraps a single SQLite connection plus Conn for convenience, implements query/
pull traits, and adds helpers for caching, observers, and optional encryption/sync.
- QueryBuilder offers an ergonomic way to build queries with typed inputs and fetch
results as scalar/coll/tuple/rel without manual QueryInputs plumbing.
- vocabulary enables defining, checking, installing, and upgrading named vocabularies
with versioned attributes, including pre/post migration hooks and multi‑vocabulary
orchestration.
- Optional features integrate syncing (tolstoy) and SQLCipher encryption, keeping the
core decoupled from these concerns.

Modules

- REFERENCE/mentat/src/lib.rs: Crate entry; re‑exports core types/traits, defines var!
and kw! macros, exposes Conn, Store, QueryBuilder, and Syncable/SyncReport behind
features.
- REFERENCE/mentat/src/conn.rs: Conn encapsulates Metadata and TxObservationService.
Methods for:
    - Queries (q_once, q_uncached, q_prepare, q_explain) using current schema and
    cache.
    - Pull (pull_attributes_for_entities / entity) via query‑pull.
    - Attribute cache registration/deregistration with forward/reverse/both directions.
    - Transaction lifecycles (begin_read, begin_uncached_read, begin_transaction,
    transact) with correct SQLite behaviors (Deferred/Immediate).
    - Observer registration and event dispatching; extensive tests cover tempid
    allocation, rollback/commit, caching performance, and error handling.
- REFERENCE/mentat/src/store.rs: Convenience wrapper around a single SQLite connection
+ Conn.
    - Open/change‑key (feature‑gated), begin read/tx, wrapper transact, optional sync
    loop with full‑sync followups.
    - Implements Queryable/Pullable by delegating to Conn.
    - Cache helpers, observer register/unregister, last_tx_id.
    - Tests verify prepared queries with cache, cache mutation semantics across
    rollback/commit, and observer notification on registered attrs.
- REFERENCE/mentat/src/query_builder.rs: Builder for typed query execution.
    - Bind var values, refs (by entid or via kw → entid), types, instants/datetimes.
    - Execute to QueryOutput and typed views: scalar, coll, tuple, rel.
    - Internally constructs QueryInputs, uses a read transaction from Store.
    - Tests cover multiple result shapes and value extraction patterns.
- REFERENCE/mentat/src/sync.rs: Feature‑gated (syncable); implements Syncable for
InProgress by constructing a RemoteClient and delegating to tolstoy::Syncer::sync.
- REFERENCE/mentat/src/vocabulary.rs: Programmatic vocabulary management.
    - Definition (name, version, attribute list, pre/post hooks) → transactable terms
    via TermBuilder.
    - Vocabulary represents the installed state (entids + attributes).
    - HasVocabularies reads all vocabularies or by name; VersionedStore checks/ensures/
    upgrades with conflict detection and missing‑attribute handling.
    - VocabularySource and SimpleVocabularySource handle multi‑vocabulary ensure with
    pre/post orchestration.
    - Validates presence/version of core schema; tests ensure core vocabulary existence
    and reading.

Key Behaviors

- Caching: Attribute‑level forward/reverse caches live in metadata; updated within
InProgress, rolled back or committed atomically, with copy‑on‑write semantics to
isolate in‑flight changes.
- Transactions: Use SQLite Deferred for reads and Immediate for writes; metadata guard
via mutex; observers receive per‑tx change sets on commit.
- Queries: Prepared/explained queries leverage schema + cache; direct lookup helpers
for single/multi‑valued attributes.
- Vocabularies: Install/upgrade declaratively; pre hook can repair data before schema
tightening; conflicts in attribute definitions are detected and rejected.

Features

- syncable: Adds sync integration (Syncable, SyncReport) via mentat_tolstoy.
- sqlcipher: Adds open_with_key and change_encryption_key for encrypted SQLite
databases.

--------------------------------------------------------------------------------
 
Section 2:

  ├── core/
  │   ├── Cargo.toml
  │   └── src/
  │       ├── cache.rs
  │       ├── counter.rs
  │       ├── lib.rs
  │       ├── sql_types.rs
  │       ├── tx_report.rs
  │       ├── types.rs
  │       └── util.rs
  ├── core-traits/
  │   ├── Cargo.toml
  │   ├── lib.rs
  │   ├── value_type_set.rs
  │   └── values.rs

--------------------------------------------------------------------------------

Executive Summary

- Core crate (REFERENCE/mentat/core)
    - REFERENCE/mentat/core/src/lib.rs
        - Defines the central Schema model: IdentMap (ident→entid), EntidMap
        (entid→ident), and AttributeMap (entid→Attribute), plus component_attributes.
        Implements HasSchema for lookups and attribute checks, to_edn_value() for
        serializing schema to EDN, and keeps component_attributes up to date.
        - Re-exports helpers/macros and SQL type helpers (SQLTypeAffinity,
        SQLValueType, SQLValueTypeSet), and includes interpose! macros for efficient
        formatting/iteration.
        - Tests ensure microsecond-precision DateTime handling and stable EDN output
        for schema.
    - REFERENCE/mentat/core/src/sql_types.rs
        - Maps logical ValueType to storage representation: per-type SQL tag
        (ValueTypeTag) and optional SQLite affinity to disambiguate overlapping tags
        (e.g., Long vs Double).
        - SQLValueType and SQLValueTypeSet traits provide tag derivation, uniqueness
        checks, and set-to-tags mapping; includes integer accommodation checks for
        types.
    - REFERENCE/mentat/core/src/cache.rs
        - Declares cache traits only: CachedAttributes (forward/reverse checks and
        lookups) and UpdateableCache<E> for applying retractions/assertions to the
        cache. No implementation here; acts as integration surface.
    - REFERENCE/mentat/core/src/tx_report.rs
        - TxReport struct encapsulating commit metadata: tx_id, tx_instant, and
        resolved tempid mapping.
    - REFERENCE/mentat/core/src/types.rs
        - Alias ValueTypeTag = i32 shared with SQL typing.
    - REFERENCE/mentat/core/src/counter.rs
        - RcCounter: simple shared counter with next() for predictable test-friendly
        increments.
    - REFERENCE/mentat/core/src/util.rs
        - Side-effect chaining traits ResultEffect and OptionEffect (when_ok/when_err/
        when_none/when_some), and small Either enum with map helpers.
    - Cargo and deps
        - REFERENCE/mentat/core/Cargo.toml wires to core_traits and edn, with serde-
        enabled chrono/uuid/ordered-float.
- Core-traits crate (REFERENCE/mentat/core-traits)
    - REFERENCE/mentat/core-traits/lib.rs
        - Foundational data model and conversions used across the project.
        - Identity/types:
            - Entid = i64, KnownEntid wrapper with conversions into entity/attribute/
            value “places”.
            - ValueType enumeration (Ref, Boolean, Instant, Long, Double, String,
            Keyword, Uuid) with keyword/EDN conversion, EnumSet plumbing, and
            fmt::Display.
        - Attributes:
            - Attribute with flags (index, fulltext, unique, multival, component,
            no_history) and flags() → bitfield (AttributeBitFlags) for search tables;
            to_edn_value() emits standardized EDN maps using values.rs constants.
            attribute::Unique converts to typed EDN keywords.
        - Values:
            - TypedValue (Ref, Boolean, Long, Double(OrderedFloat),
            Instant(DateTime<Utc>), String(ValueRc), Keyword(ValueRc), Uuid). Rich
            From implementations, microsecond precision for instants, and ergonomic
            typed_... constructors.
            - Extensive accessors and FFI-friendly conversions to C strings for string/
            keyword/uuid.
        - Query/pull bindings:
            - Binding enum (Scalar, Vec, Map) and StructuredMap for pull results.
            Includes type/congruence checks and conversions/accessors.
        - Utilities:
            - now() returns a microsecond-precise UTC timestamp; bail! macro; tests for
            attribute flags and typed value congruence.
    - REFERENCE/mentat/core-traits/value_type_set.rs
        - ValueTypeSet over EnumSet<ValueType> with constructors (any, none, of_one,
        numeric-only variants), set operations (union/intersection/difference),
        membership/introspection (contains, exemplar, is_unit, is_only_numeric), and
        iteration support.
    - REFERENCE/mentat/core-traits/values.rs
        - Lazy edn::Value constants for common DB keywords (e.g., :db/ident, :db/
        valueType, :db.cardinality/one, :db.type/*, :db.unique/*). Used to generate
        canonical EDN for attributes.
    - Cargo and deps
        - REFERENCE/mentat/core-traits/Cargo.toml provides serde + edn wiring with rc
        support; uses lazy_static to materialize keyword constants.

Notable Behaviors

- Microsecond precision normalization for instants across conversions and “now()”.
- SQL encoding strategy: type tags with optional SQLite affinity to differentiate
overlapping types.
- Attribute flags provide a compact, typed bridge from schema semantics to search-table
columns.
- Rich typed-value conversions (including C FFI string exports) and congruence checks
support ergonomic APIs and safe bindings.
- Cache traits expose forward/reverse lookups and update application but defer actual
storage/strategies to implementers.

--------------------------------------------------------------------------------

Section 3:

  ├── db/
  │   ├── README.md
  │   ├── Cargo.toml
  │   ├── src/
  │   │   ├── add_retract_alter_set.rs
  │   │   ├── bootstrap.rs
  │   │   ├── debug.rs
  │   │   ├── entids.rs
  │   │   ├── internal_types.rs
  │   │   ├── lib.rs
  │   │   ├── metadata.rs
  │   │   ├── schema.rs
  │   │   ├── timelines.rs
  │   │   ├── tx.rs
  │   │   ├── tx_checking.rs
  │   │   ├── tx_observer.rs
  │   │   ├── types.rs
  │   │   ├── upsert_resolution.rs
  │   │   └── watcher.rs
  │   └── tests/
  │       └── value_tests.rs

--------------------------------------------------------------------------------

Executive Summary — Section 3 (db)

- Purpose
    - Implements the SQLite-backed storage layer and transaction engine for Mentat:
    schema install/migration, EDN→SQL translation, upsert resolution, metadata/
    materialized views maintenance, attribute caching, timeline operations, and
    observer hooks.
- SQLite Layer
    - Connection setup: opens DB or in-memory with PRAGMAs (WAL, temp_store=2, journal
    sizing); optional SQLCipher support via feature flag for encryption keys.
    - Schema install/migration: V1 schema DDL (tables, indices, FTS4 virtual table,
    materialized views: idents, schema, known_parts, fulltext_datoms, all_datoms).
    - Versioning: uses PRAGMA user_version with CURRENT_VERSION=1; ensures/creates
    current DB and bootstraps initial data and views.
    - Materialized views: idents and schema derived from datoms; re-populated when
    metadata changes.
- Core Data Model and Types
    - Typed SQL values: precise mapping between TypedValue and SQLite values/type tags
    for round-trip (booleans, longs/doubles, instants via micros, strings, UUID
    blobs, keywords).
    - Partitions and DB: Partition/PartitionMap manage entid allocation; DB bundles
    partition_map + schema.
    - Entids and metadata attributes: constant ids for :db/* namespace; helpers to
    detect when an attribute impacts metadata or schema; precomputed SQL lists for
    metadata-related attributes.
- Transactions Pipeline
    - Stage 1: Entities → Terms
        - Parses EDN entities (maps/vectors) into typed Term variants, handling:
            - Type coercion via schema (e.g., keywords→ref, etc.).
            - Reverse-attr notation (:attr/_reversed), list explosions for cardinality-
            many, and nested map explosion with dangling protection (requires
            component or unique identity).
            - (transaction-tx) function expansion for self-referential tx metadata;
            (lookup-ref ...) captured as symbolic lookup refs.
            - :db/id stripping/normalization for map notation; internal tempids
            (Mentat-scoped, not exposed in TxReport).
    - Stage 2: Lookup Ref Resolution
        - Bulk-resolves all (a,v) pairs that refer to unique attributes against SQLite
        using indexed lookups, producing terms with only tempids (no lookup refs).
    - Stage 3: Upsert Resolution
        - Evolves tempid populations across generations. Simple upserts
        (tempid, :db.unique/identity, value) drive resolution first; complex upserts
        or dependent upserts evolve via union-find clustering. Conflicting upserts
        and multi-resolution scenarios are detected and reported deterministically.
    - Stage 4: Application + Constraints
        - Builds an AEV trie, enforces:
            - Type correctness (attribute value type matches) and cardinality
            constraints (no multi-values for one; no add+retract same datom).
        - Splits inserts into non-FTS/FTS and one/many buckets; writes through temp
        tables to datoms and transactions (materialize + optional commit).
        - Computes/ensures txInstant and notifies a watcher per datom; returns TxReport
        with tx id/instant/tempid map.
    - SQL execution path
        - Creates temp tables (exact_searches, inexact_searches, search_results),
        executes search join to existing datoms, applies retractions and additions,
        populates timelined_transactions (timeline-aware tx log), updates datoms
        flags (avet/vaet/fulltext/unique).
        - For FTS values, inserts text into fulltext_values_view with upsert-ish
        triggers, then links datoms to FTS rowid.
- Schema/Metadata Management
    - Bootstrap: ships core schema/idents as a symbolic EDN schema; converts to
    assertions and transacts at init; installs partitions (:db.part/db, :db.part/
    user, :db.part/tx).
    - Attribute builder/validation:
        - Validates combinations (e.g., fulltext implies index and string; component
        implies ref; unique requires index).
        - Supports install and alter validations, and a mutate path that records
        applied alterations (Index, Unique, Cardinality, NoHistory, IsComponent).
    - Metadata updates:
        - Interprets raw EAVs to maintain AttributeMap, including handling safe schema
        retractions (all defining attributes + :db/ident must be retracted together).
        - Applies changes to materialized views (idents/schema) after each tx; flips
        datoms index flags (avet/unique) and checks safety when switching
        cardinalities.
- Attribute Caching
    - Caches current attribute state in memory for fast lookups:
        - Forward caches: single/multi value maps of e→v(s).
        - Reverse caches: unique (v→e) and non-unique (v→set<e>) maps.
        - Tracks which attributes are cached forward/reverse; populates from SQLite
        aevt scans, differentiates fulltext vs normal.
    - Copy-on-write overlay for in-progress transactions:
        - InProgressSQLiteAttributeCache overlays a stable SQLiteAttributeCache;
        supports per-attribute register/unregister; merges overlay on commit; handles
        retractions via None sentinels; isolates readers during tx.
        - Accumulates per-attribute updates from a datom watcher and updates caches
        post-commit.
    - Query helpers to get entids for values and values for entids only when the
    attribute is marked cached.
- Timelines
    - Moves transactions off main timeline to another timeline:
        - Validates destination is empty, collects tx range, rewinds state by applying
        inverse terms (using Materialize action), cleans up txInstant artifacts from
        datoms, remaps transactions’ timeline, and returns updated schema/
        partition_map as needed.
        - Test coverage demonstrates moving ranges and preventing timeline mixing.
- Observers and Watchers
    - TransactWatcher: simple hook to see each datom and a done() with pre-tx schema;
    used by caching and observers.
    - TxObserver service: allows registering observers keyed by string with an
    attribute-set filter; collects attributes per tx and asynchronously notifies
    observers via a background command executor thread after commit.
- Debug/Test Utilities
    - TestConn holds sqlite/partition_map/schema and provides convenient transact
    methods; checks materialized views stay consistent.
    - Dump helpers to fetch datoms, transactions, fulltext values; macros
    assert_transact and assert_matches to parse/compare EDN patterns.
    - Extensive tests cover add/retract semantics, cardinality, uniqueness, fulltext,
    lookup refs, nested maps, reversed notation, schema install/alter/retract,
    upserts, timelines, type checking, and SQLCipher gating.
- Error Handling
    - Rich DbErrorKind coverage for input errors, type and cardinality constraints,
    schema assertion/alteration failures, conflicting upserts, cache update failures,
    timeline misuse, and SQL execution contexts via failure::ResultExt for precise
    provenance.

Notable Interactions

- Relies on core-traits for TypedValue, Attribute, ValueType, and time helpers; and
core for Schema, maps, and TxReport.
- Implements MentatStoring for rusqlite::Connection, encapsulating the storage
operations consumed by the transaction pipeline.

--------------------------------------------------------------------------------

Section 4:

  ├── transaction/
  │   ├── Cargo.toml
  │   └── src/
  │       ├── entity_builder.rs
  │       ├── lib.rs
  │       ├── metadata.rs
  │       └── query.rs

--------------------------------------------------------------------------------

- Purpose: High‑level transaction and query layer for Mentat, wrapping rusqlite and the
lower db crate with builders, caching, observers, and prepared/explain capabilities.
- Crate wiring: Depends on edn, core/core‑traits, db/db‑traits, query‑algebrizer/
projector/pull/sql, rusqlite. Optional sqlcipher feature forwards to rusqlite.
- In‑progress state: InProgress holds open SQLite transaction, generation guard,
PartitionMap, Schema, attribute cache, and observer watcher. InProgressRead provides
read‑only wrapper.
- Transactions:
    - Build terms programmatically via entity_builder (TermBuilder, EntityBuilder,
    BuildTerms) with named tempids, lookup refs, and tx functions.
    - Execute with InProgress::{transact_entities, transact_terms, transact, import};
    uses mentat_db::transact(_terms) pipeline and a combined watcher
    (InProgressTransactWatcher) to feed both cache and observers.
    - commit updates Metadata (generation, partitions, schema), commits cache overlay,
    and notifies observers; rollback and savepoint helpers included.
- Caching controls:
    - use_caching toggles query cache usage.
    - cache() registers/deregisters forward/reverse/both attribute caches by ident;
    cache state lives in InProgressSQLiteAttributeCache and SQLiteAttributeCache.
- Query API:
    - Traits Queryable and Pullable implemented for both InProgress and InProgressRead.
    - Query execution: q_once, q_uncached, q_prepare (validated no unbound vars), and
    q_explain (returns SQLQuery + EQP steps). Uses algebrizer to transform EDN
    queries to SQL, projector to map rows to TypedValue structures.
    - Lookup helpers: lookup_value(_for_attribute) and lookup_values(_for_attribute)
    return cached values when attribute is registered, otherwise run minimal queries.
- Metadata: Metadata aggregates generation, PartitionMap, shared Schema, and persistent
SQLiteAttributeCache; constructed via private new() to constrain initialization.

--------------------------------------------------------------------------------

Section 5:

  ├── edn/
  │   ├── README.md
  │   ├── build.rs
  │   ├── Cargo.toml
  │   ├── src/
  │   │   ├── edn.rustpeg
  │   │   ├── entities.rs
  │   │   ├── intern_set.rs
  │   │   ├── lib.rs
  │   │   ├── matcher.rs
  │   │   ├── namespaceable_name.rs
  │   │   ├── pretty_print.rs
  │   │   ├── query.rs
  │   │   ├── symbols.rs
  │   │   ├── types.rs
  │   │   ├── utils.rs
  │   │   └── value_rc.rs
  │   └── tests/
  │       ├── query_tests.rs
  │       ├── serde_support.rs
  │       └── tests.rs

--------------------------------------------------------------------------------

- Purpose and Build
    - Implements EDN parsing and related data structures for Mentat; also parses
    Mentat-specific transaction entities and Datalog-like queries.
    - REFERENCE/mentat/edn/build.rs compiles the PEG grammar REFERENCE/mentat/edn/src/
    edn.rustpeg into parse module, included in REFERENCE/mentat/edn/src/lib.rs.
    Features optional serde_support.
- Core Types and Utilities
    - Value/SpannedValue/ValueAndSpan in REFERENCE/mentat/edn/src/types.rs represent
    EDN values (atoms and collections). Macros generate comprehensive helpers:
        - is*/as*/into* helpers for all variants, conversions from/to floats, bigints,
        symbols, keywords.
        - Display prints valid EDN (including special #f NaN, #f +/-Infinity, #uuid,
        #inst).
        - Ordering defined across heterogenous types; collection comparison is
        lexicographic.
        - Time helpers: FromMicros, FromMillis, ToMicros, ToMillis.
    - utils::merge in REFERENCE/mentat/edn/src/utils.rs merges two Value::Maps (right
    overwrites left).
    - value_rc (REFERENCE/mentat/edn/src/value_rc.rs) defines ValueRc<T> = Arc<T> plus
    FromRc/Cloned traits to convert between Rc/Arc/Box and clone values
    ergonomically.
    - intern_set (REFERENCE/mentat/edn/src/intern_set.rs) provides an InternSet<T> that
    interns large values and returns shared ValueRc<T> handles.
- Symbols, Keywords, Names
    - namespaceable_name (REFERENCE/mentat/edn/src/namespaceable_name.rs) stores
    namespace/name as a single String with a boundary index, ensuring invariants;
    supports forward/backward name toggling (prefix _), ordering (namespace then
    name), and serde serialization via a friendly struct form ({ namespace, name }).
    - symbols (REFERENCE/mentat/edn/src/symbols.rs) builds on NamespaceableName:
        - PlainSymbol, NamespacedSymbol, Keyword with EDN Display.
        - Introspection (is_var_symbol for ?x, is_src_symbol for $), is_backward/
        to_reversed keyword helpers, and ns_keyword! macro.
- Parsing (PEG grammar) and Query/Transaction AST
    - Grammar in REFERENCE/mentat/edn/src/edn.rustpeg:
        - EDN atoms: nil, booleans, integers (including octal/hex/based), bigints,
        floats (including exponents), strings (with escapes), #uuid, instants (#inst,
        #instmillis, #instmicros), symbols, keywords, and collections (list, vector,
        set, map). Commas treated as whitespace; line comments ;....
        - Transaction entities: :db/add / :db/retract, entity/value/attribute places
        including entids, idents (keywords), lookup-refs, tx-functions, vectors, map
        notation. Supports reversed attributes (:_foo/bar) and normalizes them at
        parse time.
        - Query language: parses :find (scalar/coll/rel/
        tuple), :in, :where, :limit, :order, pull, (the ?var), predicates and where
        functions, joins (or, or-join, and in or arms), negation (not, not-join), and
        type annotations.
    - Query AST in REFERENCE/mentat/edn/src/query.rs:
        - Variable, QueryFunction, FnArg (variables, src vars, ints/entids, keywords,
        constants, nested vectors).
        - Pattern places: PatternNonValuePlace (placeholder/var/entid/ident) and
        PatternValuePlace (placeholder/var/entid-or-int/ident-or-kw/constant) with
        conversions from EDN values.
        - Pattern normalizes reversed attributes (swapping e/v and reversing
        attribute).
        - Pull specs: PullConcreteAttribute, NamedPullAttribute, PullAttributeSpec,
        Pull.
        - Projections: Element (variables, aggregates, corresponding (the ?x), pulls),
        FindSpec, Limit, Order.
        - Binding forms: VariableOrPlaceholder, Binding (scalar/coll/rel/tuple) with
        validation helpers.
        - Where clauses: Predicate, WhereFn, OrJoin/OrWhereClause, NotJoin,
        TypeAnnotation, and WhereClause sum type.
        - Parsed query (ParsedQuery) holds all parts; ContainsVariables trait collects
        mentioned variables and supports checks like OrJoin::is_fully_unified.
        Distinct handling via FindSpec::requires_distinct.
- Pretty Printing and Pattern Matching
    - pretty_print (REFERENCE/mentat/edn/src/pretty_print.rs): pretty-printer for Value
    using the pretty crate, with controlled width and minimal whitespace expansion
    tailored for EDN/query readability.
    - matcher (REFERENCE/mentat/edn/src/matcher.rs): pattern matching for Value with
    default rules:
        - _ matches anything; ?name is a placeholder that must match the same sub-EDN
        where repeated.
        - Supports recursive matching of vectors, lists, sets, and maps; includes
        substantial unit tests documenting behavior and corner cases (e.g., set/map
        cardinality vs. placeholder uniqueness).
- Transactions Entities Model
    - entities (REFERENCE/mentat/edn/src/entities.rs) provides generic building blocks
    for transactable data:
        - TempId (External string or Internal i64), EntidOrIdent (i64 or keyword).
        - LookupRef, TxFunction, ValuePlace<T>, EntityPlace<T>, AttributePlace.
        - Entity<T> variants for vector form and map notation; OpType for add/retract.
        - TransactableValueMarker ensures the right Into/From relations when embedding
        values.
        - Uses ValueRc for efficient sharing.
- Tests Coverage
    - REFERENCE/mentat/edn/tests/tests.rs: Extensive validation of EDN parsing for
    atoms, collections, special tags (#uuid, #inst*), printing round-trips, ordering,
    helpers (is/as/into), whitespace/comments/commas handling, and utilities (merge).
    - REFERENCE/mentat/edn/tests/query_tests.rs: Validates parsing of queries including
    predicates, or/or-join/and nesting, order, limit, placeholders/variables, and
    UUID constants in patterns.
    - REFERENCE/mentat/edn/tests/serde_support.rs: When serde_support enabled, tests
    serialization/deserialization shape for Keyword via NamespaceableName
    representation.

--------------------------------------------------------------------------------

Section 6:

  ├── query-algebrizer/
  │   ├── README.md
  │   ├── Cargo.toml
  │   ├── src/
  │   │   ├── lib.rs
  │   │   ├── types.rs
  │   │   ├── validate.rs
  │   │   └── clauses/
  │   │       ├── convert.rs
  │   │       ├── fulltext.rs
  │   │       ├── ground.rs
  │   │       ├── inputs.rs
  │   │       ├── mod.rs
  │   │       ├── not.rs
  │   │       ├── or.rs
  │   │       ├── predicate.rs
  │   │       ├── resolve.rs
  │   │       ├── tx_log_api.rs
  │   │       └── where_fn.rs
  │   └── tests/
  │       ├── fulltext.rs
  │       ├── ground.rs
  │       ├── predicate.rs
  │       ├── type_reqs.rs
  │       └── utils/
  │           └── mod.rs

--------------------------------------------------------------------------------

- Purpose: Transforms parsed Datalog (EDN) queries into an algebraic form (AlgebraicQuery) suitable for deterministic SQL generation over Mentat’s schema. Central entry is src/
lib.rs.
- Core types: src/types.rs defines SQL-facing concepts (DatomsTable, Column, QualifiedAlias), logical constraints (ColumnConstraint, ColumnIntersection/ColumnAlternation),
comparison ops (Inequality), computed sources (ComputedTable: Subquery, Union, NamedValues), and evolved query shapes (Evolved*).
- ConjoiningClauses engine: src/clauses/mod.rs holds the main state machine for algebrization. It:
    - Tracks FROM sources, computed tables, WHERE intersections/alternations, variable→column bindings, value bindings, known/extracted types, and required type sets.
    - Aliases tables, binds columns/values, narrows/broadens ValueTypeSet, processes type requirements into HasTypes constraints, expands inter-column equalities, prunes extracted
    types.
    - Orchestrates clause application order and pattern evolution and marks query “known empty” (EmptyBecause) for impossible constraints.
- Pattern handling: src/clauses/pattern.rs converts EDN patterns to EvolvedPattern using schema/caches, chooses tables (Datoms/AllDatoms/Fulltext*) based on attribute/value/
fulltext, binds appropriate columns, and enforces type congruence.
- Predicates and type annotations: src/clauses/predicate.rs resolves inequality predicates (<, <=, >, >=, !=) with type inference and intersection of supported types, plus
TypeAnnotation constraints (type ?v …).
- Logical operators:
    - OR/OR-join: src/clauses/or.rs detects “simple” ORs (same shape/table) and compiles to alternations over one alias, or else builds UNIONs of sub-CCs with consistent projection
    and optional per-arm type-tag projection.
    - NOT/NOT-join: src/clauses/not.rs builds NOT EXISTS subqueries, enforcing that join vars are bound in the outer CC (or error if unbound).
    - Validation of unify vars is in src/validate.rs.
- Data grounding and functions:
    - ground: src/clauses/ground.rs supports scalar/tuple/collection/relation groundings; materializes NamedValues computed tables, propagates/validates types, and filters
    impossible rows.
    - fulltext: src/clauses/fulltext.rs builds FTS join (FulltextValues + Datoms) matching text, binding entity/value/tx/score with appropriate type constraints.
    - tx-log API: src/clauses/tx_log_api.rs implements (tx-ids) and (tx-data) via Transactions table, exposing e/a/v/tx/added and range constraints.
    - where-fn dispatch in src/clauses/where_fn.rs; argument resolvers (numeric/instant/ref) in src/clauses/resolve.rs; FnArg→TypedValue conversion in src/clauses/convert.rs.
    - Query inputs: src/clauses/inputs.rs models input variable types/values with validation (QueryInputs).
- Algebrization flow: src/lib.rs parses/validates find query, seeds CC with inputs, derives types from find spec (e.g., pull vars→Ref), applies clauses, expands binding equalities,
prunes/extracts types, processes required types, validates/expands ordering needs (OrderBy + type tags), finalizes AlgebraicQuery and simplifies variable limits.
- Caching integration: Known (src/lib.rs) abstracts schema + CachedAttributes for reverse/forward lookups, enabling pattern specialization and early failure during algebrization.
- Tests: cover fulltext, ground variations (including heterogeneous and placeholder cases), predicate/typing behavior (numeric vs instant), OR/NOT validation and semantics, type
requirement propagation, and tx-log APIs. Utilities in tests/utils/mod.rs help build schemas and run algebrization.

--------------------------------------------------------------------------------

Section 7:

  ├── query-sql/
  │   ├── Cargo.toml
  │   └── src/
  │       └── lib.rs

--------------------------------------------------------------------------------

- Purpose: query-sql renders SQL from the algebrized query representation. It turns Mentat’s query IR (from query-algebrizer) into parameterized SQLite SQL using the mentat_sql
builder and shared types from core, core-traits, sql-traits, and edn.
- Core data model:
    - ColumnOrExpression, Expression: Uniform representation of SQL “things” that can appear in projections/constraints, including columns (QualifiedAlias), constants (Entid,
    TypedValue, ints), and unary expressions.
    - Projection: Columns(Vec<ProjectedColumn>), Star, One to shape SELECT lists, with aliasing via sanitized identifiers.
    - Constraint: Logical/relational predicates (Infix, And, Or, In, IsNull, IsNotNull, NotExists, TypeCheck) mapped to SQL with correct grouping and operator placement.
    - Source model: TableOrSubquery covers physical tables (SourceAlias), Union of queries, nested Subquery, and inline Values relations (both Unnamed and Named forms).
    - From/joins: TableList is shorthand for comma-separated inner joins; Join with JoinOp::Inner is present (constraints-on-join noted as TODO).
    - Query shape: SelectQuery gathers distinct, projection, from, constraints, group_by, order (using OrderBy + Direction + VariableColumn), and limit (Limit::None|Fixed|Variable).
- SQL generation:
    - Implementations of QueryFragment for all the above pieces push SQL fragments into a QueryBuilder with proper identifier quoting and parameter binding.
    - Helpers: qualified_alias_push_sql and source_alias_push_sql render qualified names and aliased sources. format_select_var produces stable, SQL-safe names for variables used in
    projections and bind parameters (e.g., ?foo-1 → ifoo_1).
    - Type affinity checks (TypeCheck) emit typeof(value) = '…' comparisons using SQLTypeAffinity derived from Mentat’s ValueType.
    - SelectQuery::to_sql_query() produces a final SQLQuery with SQL text and bound args via SQLiteQueryBuilder.
- Tests (in-module):
    - Validate rendering for IN, AND parenthesization, VALUES (unnamed and named with headers), full-text MATCHES, equality against datoms columns, and end-to-end SELECT with
    DISTINCT, multiple tables, and constraints.
- Dependencies (Cargo):
    - Bridges to edn, core-traits, mentat_core, mentat_sql, sql-traits, and mentat_query_algebrizer, reflecting its role as the SQL emission layer downstream of algebrization.

--------------------------------------------------------------------------------

Section 8:

  ├── sql/
  │   ├── README.md
  │   ├── Cargo.toml
  │   └── src/
  │       └── lib.rs

--------------------------------------------------------------------------------

- Purpose: Tiny SQLite-focused SQL builder that safely composes SQL strings and named parameters; distilled from Diesel’s QueryBuilder.
- Main files: REFERENCE/mentat/sql/Cargo.toml, REFERENCE/mentat/sql/README.md, REFERENCE/mentat/sql/src/lib.rs.
- Core types:
    - SQLQuery: final SQL plus ordered (name, Rc<Value>) args.
    - QueryBuilder trait: push raw SQL, identifiers, TypedValues, named bind params, then finish.
    - QueryFragment trait: composable pieces write themselves into a QueryBuilder.
    - SQLiteQueryBuilder: concrete builder with backtick-escaped identifiers and generated $vN params.
- Value handling (core_traits::TypedValue):
    - Int/bool inline; doubles formatted in scientific notation to avoid integer coercion in SQLite.
    - Instants via ToMicros; UUIDs, strings, keywords become parameters or static args.
    - Strings/blobs deduped using internal maps; consolidated at finish.
- Parameter rules:
    - Named parameters only; push_bind_param validates names and forbids collisions with generated $vN.
    - finish merges static + deduped args and sorts by name for deterministic order.
- Safety/correctness:
    - Identifiers quoted with backticks; errors via SQLError/BuildQueryResult.
    - Unit tests validate SQL composition, quoting, value formatting, dedupe, and arg ordering.
- Dependencies: rusqlite (with limits, optional sqlcipher), ordered-float, plus Mentat crates core, core-traits, sql-traits.

--------------------------------------------------------------------------------

Section 9:

  ├── query-projector/
  │   ├── README.md
  │   ├── Cargo.toml
  │   └── src/
  │       ├── binding_tuple.rs
  │       ├── lib.rs
  │       ├── project.rs
  │       ├── pull.rs
  │       ├── relresult.rs
  │       ├── translate.rs
  │       └── projectors/
  │           ├── constant.rs
  │           ├── mod.rs
  │           ├── pull_two_stage.rs
  │           └── simple.rs

--------------------------------------------------------------------------------

- Purpose
    - Bridges algebrized queries to executable SQL plus Datalog result shaping. Produces both the SQL projection (what columns and how) and a Datalog “projector” that converts
    SQLite rows into Mentat bindings.
- Core Types
    - QueryOutput and QueryResults (src/lib.rs): Encapsulate a query’s FindSpec and its results in one of four shapes: Scalar, Tuple, Coll, Rel. Conversions enforce expected shapes
    with clear errors.
    - BindingTuple (src/binding_tuple.rs): Converts tuple results into Rust tuples (1–6) or Vec; validates expected width.
    - CombinedProjection (src/lib.rs): Holds SQL projection, optional pre-aggregate projection (for nested queries), group-by columns, and a Datalog projector; flips DISTINCT off
    when LIMIT 1.
    - ProjectedElements (src/project.rs): Intermediate build product with inner/outer SQL projections, templates (TypedIndex), pulls, and GROUP BY info.
    - TypedIndex (src/lib.rs): Knows where in a row to read a value and whether a type tag column is needed (Known vs Unknown).
- Projection & Aggregation
    - project_elements (src/project.rs): Walks FindSpec elements, producing inner SQL columns, outer references, typed templates, pulls, and tracking aggregates. Handles:
        - Variable vs Corresponding (the ?x) semantics (min/max disambiguation).
        - Type-tag projection when variables are not of uniquely known type.
        - :with variables, ORDER BY named projection, and grouping.
    - query_projection (src/lib.rs): Returns either a ConstantProjector for known-empty or fully unit-bound queries, or a CombinedProjection with the right projector. Aggregates
    trigger a two-layer (inner distinct, outer aggregate) plan.
- Translation to SQL
    - translate.rs: Converts ConjoiningClauses to query-sql’s SelectQuery. Key pieces:
        - Constraint building from column intersections/alternations and column constraints.
        - Type constraints via value_type_tag and SQL type affinities (possible_affinities).
        - cc_to_select_query constructs the SelectQuery with projection, from, constraints, group_by, order, and limit.
        - re_project builds nested queries to apply DISTINCT correctly relative to aggregates and LIMIT/ORDER semantics.
        - cc_to_exists yields a minimal “exists” query for CCs.
- Projectors
    - Simple projectors (src/projectors/simple.rs):
        - ScalarProjector, TupleProjector, RelProjector, CollProjector.
        - Decode row values via TypedIndex to Mentat Bindings, assembling the target shape.
        - Distinct is avoided when already implied (e.g., aggregate inner or all columns are unit).
    - Two-stage pull projectors (src/projectors/pull_two_stage.rs):
        - ScalarTwoStagePullProjector, TupleTwoStagePullProjector, RelTwoStagePullProjector, CollTwoStagePullProjector.
        - Stage 1: Collect entity IDs from rows. Stage 2: Use Puller (mentat_query_pull) to expand requested attributes and splice pulled maps into bindings.
- Pull Support
    - pull.rs: PullOperation (pattern), PullTemplate (indices + operation), PullConsumer collects eids from rows, executes pull, and expands/produces results.
- Utilities & Results
    - RelResult (src/relresult.rs): Strided container for relational results; supports reference access, iteration, and ownership conversion; includes tests for correctness.
- Error Handling
    - ProjectorError covers invalid projections (duplicates, conflicting (the ?x) usage), tuple width mismatches, unexpected result type conversions, and “not yet implemented” for
    complex aggregates.
- Dependencies & Integration
    - Integrates with algebrizer (query structure), query-sql (SQL AST), db layer (TypedValue conversions), and query-pull (pull execution). Uses rusqlite, indexmap, and core Mentat
    crates.

--------------------------------------------------------------------------------

Section 10:

  ├── query-pull/
  │   ├── Cargo.toml
  │   └── src/
  │       └── lib.rs

--------------------------------------------------------------------------------

- Purpose
    - Implements “pull” evaluation: given a schema, SQLite connection, a set of entity IDs, and a pull spec (attributes and nested patterns), produce structured maps for each entity
    with requested attributes, including nested and multi-valued attributes.
- Key Types
    - Puller: Core engine. Built via Puller::prepare(schema, attrs), which resolves attribute specs and aliases; internally derives an AttributeSpec for caching and optional :db/id
    aliasing.
    - PullResults: BTreeMap<Entid, ValueRc<StructuredMap>> mapping each entity to its result map.
    - PullAttributeSpec / NamedPullAttribute / PullConcreteAttribute: Pull spec AST nodes imported from edn::query.
- API Entry Points
    - pull_attributes_for_entity(schema, db, entity, attributes) -> Result<StructuredMap>: Convenience to pull a given list of attributes for a single entity.
    - pull_attributes_for_entities(schema, db, entities, attributes) -> Result<PullResults>: Bulk version for multiple entities.
    - Puller::prepare(schema, attrs) -> Result<Puller>: Convert specs (idents or entids, optional aliasing, wildcard) into a ready-to-run puller.
    - Puller::pull(schema, db, entities) -> Result<PullResults>: Executes the pull over a set of entities.
- How Pull Works
    - Preparation:
        - Resolves attribute identifiers to entids via the schema, handles optional aliasing; supports wildcard [*] to include all attributes.
        - Special-cases :db/id (ensures specified at most once) and supports aliasing of :db/id.
        - Builds a cache::AttributeSpec describing requested attributes for efficient fetching.
    - Execution:
        - Materializes a set of entity IDs, constructs AttributeCaches via mentat_db::cache::make_cache_for_entities_and_attributes.
        - Initializes result maps, seeding :db/id (aliased if requested).
        - Iterates through requested attributes, looks up cached values per entity, and inserts Binding values into each entity’s StructuredMap.
- Data Representation
    - Values are Bindings, wrapping TypedValue or nested Map (ValueRc<StructuredMap>) to support sharing (pulled maps may appear multiple times or recursively).
    - Uses ValueRc to allow interior mutation during assembly while retaining shared references for output.
- Errors & Validation
    - PullError for repeated :db/id, invalid attributes, and general failures.
    - Preparation phase validates attribute existence; wildcard enumerates all attributes from the schema’s attribute map.
- Integration & Dependencies
    - Relies on:
        - mentat_db::cache for constructing attribute caches from SQLite.
        - mentat_core for schema, keywords, and ValueRc.
        - edn::query for pull AST types.
        - core_traits for typed values and entity IDs.
        - rusqlite for DB access.
    - Serves the query-projector’s two-stage pull projectors: projector collects entity IDs from SQL rows, then uses Puller to expand attributes and splice results into query
    bindings.
- Notes
    - Current implementation focuses on flat attribute fetching; nested/recursive pull is architected to be done in stages, but recursion limits and deeper nesting behaviors are
    stubbed in design comments.
    - Wildcard pulls use schema’s attribute map to include all attributes; aliases respected where provided.