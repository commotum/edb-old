# ATOMIC-NOTE: index structures, shared values and cache ownership

Development commentary for [Index Model](01_index_model.md), separate from the
reference text. This Stage 2 companion traces the selected sorted-collection,
index-tree and cache boundaries. It does not establish full query, indexing,
numeric-comparison or partition-policy conformance.

Source and reference passage coordinates below use unannotated baseline
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`. Added comments move current source
lines; named symbols remain the locator. `git show BASELINE:path` recovers the
recorded rendering. Rust links identify the current component owners, including
the private collection ownership change; this document records no test execution.

## Source locators

| ID | Artifact-qualified source, symbol and baseline line |
| --- | --- |
| S-ORDER | [transactor/datomic/db.clj](../../1.0.7705/transactor/src-clj/datomic/db.clj): comparator roots `eavt-cmp` 885, `avet-cmp` 908, `aevt-cmp` 931, `raet-cmp` 954; `Db.acceptDataCheck` 4748; `Db.seekEAVT` 5065 and adjacent tier-seek methods. |
| S-BT | [transactor/datomic/btset.clj](../../1.0.7705/transactor/src-clj/datomic/btset.clj): `BTSetIter` 88, `BTSetBranch` 163, `BTSetLeaf` 286, `IDataSet` 441, `BTSet` 452. [Peer counterpart](../../1.0.7705/peer/src-clj/datomic/btset.clj) has the same inspected bodies after normalizing generated numeric local names. |
| S-INDEX | [transactor/datomic/index.clj](../../1.0.7705/transactor/src-clj/datomic/index.clj): `RootNode` 235, `DirNode` 268, `get-dir-node` 302, `Index` 1353, `merge-data` 2677, `aligned-dedup` 3209, `start-dirs-pipeline` 3376. The [Peer counterpart](../../1.0.7705/peer/src-clj/datomic/index.clj) matches the selected seek/merge/cache bodies modulo generated names; whole-file identity is not asserted. |
| S-UPDATE | [transactor/datomic/update.clj](../../1.0.7705/transactor/src-clj/datomic/update.clj): `process-request-index` 800, `process-new-index` 693, `writer` 1658, `block-notifier` 1785. |
| S-LOG | [transactor/datomic/log.clj](../../1.0.7705/transactor/src-clj/datomic/log.clj): `write-tail-descriptor` 547, `Log.append` 1188. |
| S-PEER | [peer/datomic/peer.clj](../../1.0.7705/peer/src-clj/datomic/peer.clj): `accept-new-data` 430, `Connection.notify-index` 550, `Connection.notify-data` 633. |

## IM-ORDER: four access orders and their examples

Passages: [Indexes](01_index_model.md#indexes), baseline 12–18; the EAVT, AEVT,
AVET and VAET explanations/tables at 21–75; Usage Notes bullets 1–3.

**Documented contract:** E/A/V ascend and transaction time descends. EAVT and
AEVT contain all datoms; Pro AVET selects indexed or unique attributes; VAET
selects references. The example rows demonstrate contiguous entity, attribute,
attribute/value and reverse-reference groups, respectively.

**Observed source:** S-ORDER comparators order those components, with an
additional assertion-policy tie break. `acceptDataCheck` always adds EAVT/AEVT,
tests `needsAVET` for AVET and reference value type for RAET, the implementation
name for the public VAET order. The `seek*` methods merge cursors in that same
order across available tiers.

**Rust owner/disposition:** retain [Datom::cmp_in](../../src/datom.rs) and
[`recent::index_member`](../../src/recent.rs), which implement these order and
membership choices. `Datom::cmp_in` also retains an exact stored-value tie break;
the numeric-equivalence boundary requires its own contract comparison. The EAVT
paragraph about colocating master/detail IDs concerns partition allocation,
not tree balancing; it is outside this collection change and is not proved by
these comparator traces. Cloud's all-attribute AVET policy is not the Pro target.

## IM-VALUES: immutable segments and structural sharing

Passage: [Accumulate Only](01_index_model.md#accumulate-only), baseline 81–85.
The reference distinguishes accumulated information from append-only physical
storage. New retraction facts and new roots can coexist with old values.

**Observed source:** S-BT insertion copies a changed leaf and its ancestor path,
shares untouched children and returns the original node on comparator duplicates.
Cursor positions are mutable, separate from the set. S-INDEX's directory reuse
passes an existing ID onward when `aligned-dedup` finds matching entries;
`start-dirs-pipeline` writes only results that carry new directory entries.
Root/directory weak-reference cache arrays can change without changing child IDs.

**Rust owner/disposition:** retain [`RecentBtSet`](../../src/recent_btset.rs),
whose `Arc` nodes share unchanged children and preserve prior roots; retain
[`persistent_tree::merge_tree_with_boundary_loader`](../../src/persistent_tree.rs),
which reuses unaffected directory/leaf hashes. Retain the path-copied AVL
[`SharedMap`](../../src/collections/persistent_map.rs) used by
[`overlay_index`](../../src/overlay_index.rs) and
[`database_value`](../../src/database_value.rs). AVL nodes and the recent B-tree
serve different payload/access patterns; no Clojure namespace implies a Rust
crate or requires replacing every persistent map with the same tree shape.

**Observed adaptation:** Datomic's branch width 16 limits the interleaved child/
separator array, normally at most eight children. Rust uses up to sixteen child
references plus separate separators. Rust also splits an overflowing leaf near
the middle; Datomic has a special full-leaf append that reuses the full left leaf.
These retain ordering and old-root sharing but differ in allocation, height and
occupancy costs. This trace does not claim measured parity or mandate a rewrite.

## IM-TIERS: efficient accumulation and durable publication

Passage: [Efficient Accumulation](01_index_model.md#efficient-accumulation),
baseline 89–97, including all seven implementation bullets.

| Documented mechanism | Observed source route | Current Rust route and limits |
| --- | --- | --- |
| Shallow trees of stored segments | S-INDEX `RootNode` → `DirNode` → segment; `Index.seek` loads the selected children. | Retain `persistent_tree::{RootNode, DirectoryNode, LeafSegment}` and canonical object I/O. PostgreSQL stores opaque objects; Rust owns ordering and tree policy. |
| Periodic background indexing | S-UPDATE `process-request-index` runs `index/merge-db`; completion returns through `process-new-index`. | Retain [block_service.rs](../../src/block_service.rs) indexing jobs and [storage/indexing.rs](../../src/storage/indexing.rs); adopting a completed index must preserve later novelty. This companion does not revalidate all scheduling/failure cases. |
| Adaptive work and segment reuse | S-INDEX `merge-data` consumes presorted streams; `aligned-dedup`/`start-dirs-pipeline` reuse directory IDs. | `persistent_tree` reuses unaffected child hashes and copies affected ranges. That local mechanism does not prove sublinear cost for every workload; background-indexing documentation separately lists excision, AVET installation and fulltext exceptions. |
| Recent-plus-stored sorted view | S-ORDER `seek*` uses S-BT `IDataSet` and `iter/merge-iters`. | Retain [recent.rs](../../src/recent.rs) and [tree_cursor.rs](../../src/tree_cursor.rs) ordered cursors and merging, rather than materializing all datoms for every read. |
| Every transaction reaches the log | S-UPDATE `writer` → S-LOG `Log.append` → conditional tail publication; notification waits for `:logged`. | [storage/engine.rs](../../src/storage/engine.rs) publishes log/receipt/root objects before returning a fresh report; Atomic also retains its exact-retry receipt contract. |
| O(1) storage-write tuning | S-UPDATE `writer` batches encoded transactions into a tail-pod append. | The native pipeline currently performs per-request assessment/publication with immutable log and receipt trees. A constant storage-write count or equivalent batching benefit is not established by this source trace. |

## IM-READ: seek, caching and the real-time diagram

Passages: [Efficient Query](01_index_model.md#efficient-query), baseline 101;
[Real-Time Query](01_index_model.md#real-time-query), 105–107.

**Observed source:** S-INDEX `Index.seek` uses separator searches to choose a
directory and segment, then a lower-bound search and `TreeIter`; a segment
already contains datoms. `get-dir-node` and segment lookup reuse weak references
or resolve the immutable ID. S-PEER transaction/index notifications change the
connection's current value; they do not mutate previously returned values.

**Rust owner/disposition:** retain `persistent_tree::seek_tree` and
`tree_cursor::{DurableTreeCursor, MergeCursor}` traversal, plus
[`storage/snapshot.rs`](../../src/storage/snapshot.rs) captured roots and
[`connection.rs`](../../src/connection.rs) observation. The diagram's recent
plus cached durable tiers carry over. Its DynamoDB, optional Memcached and JVM
Peer Server packaging are not native PostgreSQL deployment requirements.

The reference's 1–2 storage-read and memory-speed statements explain a shallow
cached path, not an unconditional end-to-end Atomic bound. Root/reference
acquisition, authentication, cold cache loads and result size also contribute.
See the [caching companion](../08_operations/02_observability_and_tuning/01_memory_and_caching.atomic.md)
for mutable recency versus immutable payload ownership.

## IM-GUIDANCE: operational claims and remaining evidence

Passage: [Usage Notes](01_index_model.md#usage-notes), baseline 111–118.
The first three bullets follow the order/membership trace above. Transparent
caching and workload-specific residence are handled by the caching companion.
The comparative latency, millisecond fetch examples, fully cached small-database
case and storage-performance advice are operational guidance, not benchmark
results reproduced by this change. The final capacity requirement routes to
`block_service` backpressure and indexing; scheduling acceptance remains with
that component. No test result, RSS reduction, latency improvement or complete
indexing-policy equivalence is asserted here.
