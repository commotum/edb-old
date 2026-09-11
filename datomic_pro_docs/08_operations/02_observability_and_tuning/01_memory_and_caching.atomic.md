# ATOMIC-NOTE: immutable cached data and mutable cache policy

Development commentary for [Memory and Caching](01_memory_and_caching.md).
The original text and source URL remain authoritative reference material. This
Stage 2 trace covers cache/value ownership and the selected shared-recency
change; it classifies surrounding lifecycle and optional-integration passages
without claiming full implementation coverage or fresh test execution.

Reference/source baseline: `cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`.
Coordinates name the unannotated rendering; use `git show BASELINE:path` when
current comments have shifted lines. Rust links name current component owners.

## Source locators

| ID | Artifact-qualified source, symbol and baseline line |
| --- | --- |
| S-CACHE | [peer/datomic/domain.clj](../../../1.0.7705/peer/src-clj/datomic/domain.clj): `create-object-cache` 115; `system-cache` 177; `lookup-with-object-cache` 311. [peer/datomic/cache/caffeine.clj](../../../1.0.7705/peer/src-clj/datomic/cache/caffeine.clj): `WrappedCCache` 98; `create-weight-limited` 220; `create-scaled-weight-limited` 244. |
| S-INDEX | [peer/datomic/index.clj](../../../1.0.7705/peer/src-clj/datomic/index.clj): `RootNode` 235, `DirNode` 268, `get-dir-node` 302, `Index.seek` in `Index` 1353. |
| S-LRU | [transactor/clojure/core/cache.clj](../../../1.0.7705/transactor/src-clj/clojure/core/cache.clj): `LRUCache` 206; `lru-cache-factory` 600. [transactor/clojure/data/priority_map.clj](../../../1.0.7705/transactor/src-clj/clojure/data/priority_map.clj): `PersistentPriorityMap` 260, including `assoc`, `peek` and `pop`. These are bundled libraries, now annotated derivatives of the baseline archive text. |
| S-MEMO | [transactor/clojure/core/memoize.clj](../../../1.0.7705/transactor/src-clj/clojure/core/memoize.clj): `memoizer` 242; `lru` 393. It wraps the persistent cache in an atom; this is a dependency caller, not proof that the Datomic object cache uses `LRUCache`. |
| S-LIFECYCLE | [transactor/datomic/update.clj](../../../1.0.7705/transactor/src-clj/datomic/update.clj): `Master.internal-start-database` 2745, `process-transaction` 1102, `writer` 1658, `process-new-index` 693. [transactor/datomic/db.clj](../../../1.0.7705/transactor/src-clj/datomic/db.clj): `complete-indexing` 5619. |
| S-PEER | [peer/datomic/db_io.clj](../../../1.0.7705/peer/src-clj/datomic/db_io.clj): `load-db-from-basis` 143. [peer/datomic/peer.clj](../../../1.0.7705/peer/src-clj/datomic/peer.clj): `accept-new-data` 430; `Connection.notify-index` 550 and `notify-data` 633. |
| S-ENTITY | [peer/datomic/query.clj](../../../1.0.7705/peer/src-clj/datomic/query.clj): `EntityMap` 203, `valAt` and `touch`; `emap` 353. |
| S-MEMCACHED | [peer/datomic/memcached.clj](../../../1.0.7705/peer/src-clj/datomic/memcached.clj): `configure-auto-discovery` 220, `factory*` 249, `create-cache` 534, `start-memcached-from-config` 681. |

## MC-VALUES: transparency and validity

Passages: opening paragraphs, baseline 9–11; [Object Cache](01_memory_and_caching.md#object-cache),
39–43; Usage Notes bullets 1–2 and 4–5.

**Documented contract:** cached information is immutable, so an entry does not
become stale through an in-place change to its represented value. This does not
mean entries must remain resident forever, caches must preserve their own old
versions, or garbage-collected storage must retain every object forever.

**Observed source:** S-INDEX fills mutable weak-reference arrays beside immutable
child IDs. S-CACHE wraps Caffeine's lookup/put/remove operations and constructs
an object cache weighted by key/value size, with configured headroom. The
reference calls the object policy LRU; the implementation delegates policy to
Caffeine. Neither that description nor S-LRU establishes identity with a simple
exact-LRU implementation or with every Caffeine admission/eviction heuristic.

**Rust owner/disposition:** retain immutable `Arc` payloads and per-process caches
in [index/tree/cache.rs](../../../src/index/tree/cache.rs),
[program_cache.rs](../../../src/program_cache.rs) and
[fulltext_store.rs](../../../src/fulltext_store.rs). Active [tree cursors](../../../src/index/cursor.rs) hold
`LoadedDirectory`/`LoadedLeaf` owners; eviction releases the cache's ownership and
does not invalidate those active values. Persistent data ownership remains in
[`collections/persistent_map.rs`](../../../src/collections/persistent_map.rs),
[`index/recent/btset.rs`](../../../src/index/recent/btset.rs) and
[`index/tree/mod.rs`](../../../src/index/tree/mod.rs), as explained in the
[index-model companion](../../06_indexes/01_index_model.atomic.md).

## MC-RECENCY: selected native adaptation

Relevant passage: [Object Cache](01_memory_and_caching.md#object-cache), first
and third paragraphs; this mechanism change is an implementation decision, not
an extra documented Datomic API.

**Observed dependency mechanism:** S-LRU's lookup does not touch recency. `hit`
creates a new cache with an increasing tick; its priority map stores both
item→priority lookup and priority→item-set ordering. Reprioritization keeps both
views consistent. `peek` finds the minimum priority; ties within its hash set do
not promise FIFO order. The library explicitly trades extra index maintenance
and logarithmic peek for persistence, reprioritization and sorted traversal.
S-MEMO retains a changing cache reference outside those persistent values.

**Native decision:** use the private
[`collections::lru::LruMap`](../../../src/collections/lru.rs) for the three native
caches' keyed recency. The pre-change implementations each combined a map with a
`VecDeque`; a hit removed its key by scanning that deque, including under the
tree-cache mutex. The new mechanism uses a hash index and linked reusable slots:
touch changes only neighboring links and the endpoints, and eviction removes
one live key and frees its slot. Lookup/touch/eviction have expected O(1) work;
new insertion is amortized expected O(1), subject to hash-table behavior.

This adapts a useful dependency principle—keep keyed lookup and eviction order
consistent inside one collection—to existing mutable Rust owners. It does not
port the full priority-map API, make a generic priority queue necessary, or
implement Caffeine. Cache limits, value weights, counters, oversize bypass and
synchronization remain the consumers' policy. Persistent AVL maps remain useful
for immutable database ownership and are not replaced by this mutable cache map.

## MC-ACCOUNTING: cache limits are not process-memory limits

Passages: [Object Cache](01_memory_and_caching.md#object-cache), sizing paragraph
at 41; [Usage Notes](01_memory_and_caching.md#usage-notes), application-memory
competition at 82.

S-CACHE `create-object-cache` weighs keys and values and reserves headroom; it is
not a process-RSS limiter. Native cache consumers likewise enforce their own
entry/payload estimates. `LruMap` retains hash-table and slot-buffer capacity
after eviction; its slot count follows peak concurrent occupancy, not current
occupancy. `Arc` payloads retained by readers may also outlive eviction. Allocator
overhead, code, stacks, other caches and unrelated application memory remain
outside an individual cache's counters. Therefore an entry/byte-limit check or a
bounded touch-operation count is not an RSS or end-to-end latency measurement.

## MC-RECENT: memory-index lifecycle

Passages: [Memory Index](01_memory_and_caching.md#memory-index), 15–19;
[On the Transactor](01_memory_and_caching.md#on-the-transactor), 23–25;
[On the Peer](01_memory_and_caching.md#on-the-peer), 29–31.

**Observed source:** S-LIFECYCLE startup loads the index and catches up its log;
`process-new-index` calls `complete-indexing` to discard only the indexed portion.
S-PEER `load-db-from-basis` loads an index and calls `log/catchup`; subsequent
notifications advance the connection's current database/index. Recent data is
required to represent that basis, unlike an optional cached copy of a durable
segment. The transactor's indexing limits govern its growth; local object-cache
capacity is a separate choice.

**Description-granularity discrepancy:** the reference says the transactor updates
the memory index after writing the log. Recovered `process-transaction` advances
its internal db atom before writer publication; notification waits for `:logged`.
Startup also enables admission before catchup finishes, while serial processing
uses the caught-up value. The public durable-completion boundary is supported;
the prose is not an exact internal scheduling trace. Preserve both pieces of
evidence instead of rewriting the reference or copying an unsafe early reply.

**Rust trace:** [index/recent/mod.rs](../../../src/index/recent/mod.rs) owns recent sorted tiers;
[storage/snapshot.rs](../../../src/storage/snapshot.rs) captures/rebuilds a basis
and detects changed index roots; [transactor/authority/mod.rs](../../../src/transactor/authority/mod.rs)
publishes before replacing the writer's current snapshot.
[block_service.rs](../../../src/block_service.rs) owns index scheduling/adoption
and fresh-work backpressure. This companion leaves full restart, failover and
index-pressure acceptance to those components; exact retries retain their own
receipt-first admission rule.

## MC-ENTITY: handle-local caching

Passage: [Entity Caching](01_memory_and_caching.md#entity-caching), baseline 35.
S-ENTITY `EntityMap.valAt` memoizes fetched values in its mutable cache; `touch`
walks attributes and component references. Native [pull.rs](../../../src/pull.rs)
`Entity` holds a database value and shared mutex-protected lazy cache; `touch`
realizes direct/component data. Retain this handle-local ownership. It is not
one of the three global recency consumers changed here; this trace does not
reassess all entity navigation or identity semantics.

## MC-OPTIONAL: Memcached and operational advice

Passages: [Memcached](01_memory_and_caching.md#memcached), 47–55;
[SASL Auth](01_memory_and_caching.md#sasl-auth), 59;
[CloudWatch metrics](01_memory_and_caching.md#cloudwatch-metrics), 63–67;
[Automatic Node Discovery](01_memory_and_caching.md#automatic-node-discovery),
71–75; Usage Notes bullets 3 and 5.

S-MEMCACHED `factory*` selects consistent hashing, credentials and discovery mode;
`create-cache` supplies cache lookup/put and records tier metrics;
`start-memcached-from-config` composes the optional configured client. These
explain the extra shared tier, authentication and discovery passages. The stated
availability date/version and latency/capacity advice are reference operational
information, not a newly verified performance result.

Memcached, ElastiCache and CloudWatch-specific setup are non-target integrations
for this native PostgreSQL component. No such dependency is introduced or silently
equated with Atomic's optional local caches. Workload-specific cache sizing and
transparent reuse still apply; full vendor configuration and cache-population
paths remain outside this bounded study. The transactor's cardinality/uniqueness
reads use the same immutable indexes as other readers, so native cache policy
must preserve payload identity without taking over transaction semantics.
