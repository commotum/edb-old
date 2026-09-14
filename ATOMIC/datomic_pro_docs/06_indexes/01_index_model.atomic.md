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
| S-ROUTING | Same index artifacts: `strdiff` 2751, `vecdiff` 2776, `mindiff` 2800 and `sparse-datom` 2809. `mindiff` specializes strings/vectors only; all other runtime values return the complete upper value. |
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

**Rust owner/disposition:** retain [Datom::cmp_in](../../src/model/datom.rs) and
[`index::recent::index_member`](../../src/index/recent/mod.rs), which implement these order and
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

**Rust owner/disposition:** retain [`RecentBtSet`](../../src/index/recent/btset.rs),
whose `Arc` nodes share unchanged children and preserve prior roots; retain
[`index::tree::merge_tree_with_boundary_loader`](../../src/index/tree/mod.rs),
which reuses unaffected directory/leaf hashes. Retain the path-copied AVL
[`SharedMap`](../../src/collections/persistent_map.rs) used by
[`overlay_index`](../../src/index/overlay.rs) and
[`database_value`](../../src/database_value/overlay.rs). AVL nodes and the recent B-tree
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
| Shallow trees of stored segments | S-INDEX `RootNode` → `DirNode` → segment; `Index.seek` loads the selected children. | Retain `index::tree::{RootNode, DirectoryNode, LeafSegment}` and canonical object I/O. PostgreSQL stores opaque objects; Rust owns ordering and tree policy. |
| Periodic background indexing | S-UPDATE `process-request-index` runs `index/merge-db`; completion returns through `process-new-index`. | Retain [transactor/service.rs](../../src/transactor/service.rs) indexing jobs and [storage/indexing.rs](../../src/storage/indexing.rs); adopting a completed index must preserve later novelty. This companion does not revalidate all scheduling/failure cases. |
| Adaptive work and segment reuse | S-INDEX `merge-data` consumes presorted streams; `aligned-dedup`/`start-dirs-pipeline` reuse directory IDs. | `persistent_tree` reuses unaffected child hashes and copies affected ranges. That local mechanism does not prove sublinear cost for every workload; background-indexing documentation separately lists excision, AVET installation and fulltext exceptions. |
| Recent-plus-stored sorted view | S-ORDER `seek*` uses S-BT `IDataSet` and `iter/merge-iters`. | Retain [index/recent/mod.rs](../../src/index/recent/mod.rs) and [index/cursor.rs](../../src/index/cursor.rs) ordered cursors and merging, rather than materializing all datoms for every read. |
| Every transaction reaches the log | S-UPDATE `writer` → S-LOG `Log.append` → conditional tail publication; notification waits for `:logged`. | [transactor/authority/mod.rs](../../src/transactor/authority/mod.rs) publishes log/receipt/root objects before returning a fresh report; Atomic also retains its exact-retry receipt contract. |
| O(1) storage-write tuning | S-UPDATE `writer` batches encoded transactions into a tail-pod append. | The native pipeline currently performs per-request assessment/publication with immutable log and receipt trees. A constant storage-write count or equivalent batching benefit is not established by this source trace. |

## IM-READ: seek, caching and the real-time diagram

Passages: [Efficient Query](01_index_model.md#efficient-query), baseline 101;
[Real-Time Query](01_index_model.md#real-time-query), 105–107.

**Observed source:** S-INDEX `Index.seek` uses separator searches to choose a
directory and segment, then a lower-bound search and `TreeIter`; a segment
already contains datoms. `get-dir-node` and segment lookup reuse weak references
or resolve the immutable ID. S-PEER transaction/index notifications change the
connection's current value; they do not mutate previously returned values.

**Rust owner/disposition:** retain `index::tree::seek_tree` and
`index::cursor::{DurableTreeCursor, MergeCursor}` traversal, plus
[`storage/snapshot.rs`](../../src/storage/snapshot.rs) captured roots and
[`application/connection.rs`](../../src/application/connection.rs) observation. The diagram's recent
plus cached durable tiers carry over. Its DynamoDB, optional Memcached and JVM
Peer Server packaging are not native PostgreSQL deployment requirements.

The reference's 1–2 storage-read and memory-speed statements explain a shallow
cached path, not an unconditional end-to-end Atomic bound. Root/reference
acquisition, authentication, cold cache loads and result size also contribute.
See the [caching companion](../08_operations/02_observability_and_tuning/01_memory_and_caching.atomic.md)
for mutable recency versus immutable payload ownership.

## IM-ROUTING: sparse keys must obey the value comparator

**Observed source:** S-ROUTING `strdiff` takes the upper string through its first
different UTF-16 unit. `vecdiff` retains the preceding vector elements and calls
`mindiff` on the first unequal element. The default `mindiff` branch retains the
whole upper value, including URI values reached recursively inside vectors.
These are routing bounds, not new user-visible datoms.

**Consequential native discrepancy and repair:** the earlier Rust URI routing
variant shortened raw UTF-16 strings, but URI values now compare components.
For consecutive values `http://x:9/a` and `http://x:10/a`, a raw prefix
`http://x:1` is below the prior span, violating the separator requirement
`prior last < separator <= next first`. A format bump alone cannot repair this
algorithm. [`index::tree::minimum_difference`](../../src/index/tree/mod.rs)
now keeps exact URI values, recursively through tuple differences; the unsafe
raw-URI routing variant/tag is removed. String, byte and named-value compression
remain. Format 5 has no historical raw-URI tree reader.

Exact URI separators can require more parent bytes than raw prefixes. Parent
capacity remains explicit `TreeConfig` policy; this repair does not promise
compact URI keys or unlimited fixed-depth capacity. Multi-leaf/two-directory
unit regressions cover numeric ports and host case, ordinary/nested-tuple URI
values, equivalent-spelling seeks, copy-on-write merge, preserved old roots and
exact large-URI parent bytes. These test bodies exist; this review records no
new executed test result.

## IM-OWNERS: dependency-based module ownership

The inspected paths do not justify separate crates or one generic tree for all
payloads. The implemented same-crate [index module](../../src/index/mod.rs)
contains the following owners. Supported tree/recent APIs are now
`atomic_core::index::{tree,recent}`; obsolete root modules are removed without
forwarding aliases. Root-level type re-exports remain the intentional API facade.

| Owner | Current symbols and actual consumers | Retained boundary |
| --- | --- | --- |
| `index/boundary` | [boundary.rs](../../src/index/boundary.rs) `IndexBoundary`, `NormalizedIndexBoundary`, `IndexPrefix`; consumed by recent, durable and speculative cursors and public reads. | Boundary validation and prefix bias remain independent of tree shape. Full `Datom::cmp_in` and logical/stored `Value` equality remain model rules; omitted public components do not acquire fabricated sentinel values. |
| `index/eager` | [eager.rs](../../src/index/eager.rs) `IndexRoots::build` and sorted `Arc<[Datom]>` arrays are consumed by [Database](../../src/database.rs), not the native snapshot cursor. | The explicit eager oracle is separate from public boundaries. Native incremental indexes are not replaced with its whole-array rebuilds. |
| `index/recent/{mod,btset}` | [RecentTier](../../src/index/recent/mod.rs) authenticates contiguous log entries, tracks endpoint membership and memory limits, then inserts `RecentDatomRef` locators through [RecentBtSet](../../src/index/recent/btset.rs). Snapshot capture/advancement, indexing and service backlog accounting consume it. | Retain chunk-shared log plus path-copied B-tree. The locator depends on `DurableTransaction`; it is not presently a generic collection. Keep authentication, noHistory policy and pressure limits out of a generic set API. |
| `index/overlay` | [OverlayIndexes](../../src/index/overlay.rs) owns speculative current/history maps, exact-EAV removals and ident projections; [DatabaseValue overlay](../../src/database_value/overlay.rs) owns base-plus-overlay merging and exceptional AVET backfill, composed by shared [cursors](../../src/database_value/cursor.rs). | Retain AVL `SharedMap` sharing and one `Arc<Datom>` payload across orders. It intentionally records all four raw orders; schema filtering occurs in `OverlayDeltaCursor`, so blindly applying recent-tier membership at insertion would change schema-transition behavior. |
| `index/tree` | [tree/mod.rs](../../src/index/tree/mod.rs) owns node values, sparse routing, canonical codec, build/merge and validation. `storage::indexing`, recovery, snapshot loading, backup verification and fulltext structural comparison use these values. | Root→directory→columnar-leaf shape and immutable hash reuse remain. Sparse bounds are not ordinary user values; PostgreSQL publication stays outside tree construction. |
| `index/cursor` | [cursor.rs](../../src/index/cursor.rs) owns `DurableTreeSource`, loaded-node views, `DurableTreeCursor` and `MergeCursor`. `BlockSnapshot` supplies both source traits; fulltext `HistoryPages` supplies durable-only access. Portable backups reuse `BlockSnapshot`. | Generic source loading, bounded loaded-leaf ownership and mutable iterator positions remain together; cache policy and shared navigation helpers are separate. |
| `index/tree/{navigation,cache}` | [navigation.rs](../../src/index/tree/navigation.rs) owns `TreeBoundary`, routing searches and child-edge checks consumed by cursors/preparation/metadata. [cache.rs](../../src/index/tree/cache.rs) owns `TreeNodeCache` mutation, weights and counters; its moved tests keep private ownership access. | Edit preparation no longer imports routing helpers from cursor state. Cache eviction drops only its `Arc`, not an active cursor's immutable payload. |
| `index/prepare` and `index/metadata` | [prepare.rs](../../src/index/prepare.rs) contains edit planning, selected-path loading and `IndexEavLookup`; [metadata.rs](../../src/index/metadata.rs) owns projections, reconstruction, readiness and pure schema membership/delta helpers. [storage/indexing.rs](../../src/storage/indexing.rs) uses preparation, while [storage/snapshot.rs](../../src/storage/snapshot.rs) uses metadata derivation/advancement. | Dependency runs from preparation to immutable metadata policy, not the reverse. Preparation does not publish; storage protection and descriptor adoption remain in storage. |

The main flows observed are:

- `DatabaseValue` → speculative overlay maps → `SharedMap`; its base can be an
  eager `Database` or native `BlockSnapshot`.
- `BlockSnapshot::{prefix_cursor,normalized_cursor}` → `DurableTreeCursor` and
  `RecentCursor` → `MergeCursor`. Current reads suppress durable E/A/V groups
  touched by recent events; both tiers honor endpoint membership/AVET readiness.
- `IndexInput::prepare_with_parallelism` → current/history edit planning and
  selected-path preload → `merge_tree_with_boundary_loader` → immutable
  `PreparedIndex`; `BlockTransactor::adopt_index` owns later publication.

[`index/io.rs`](../../src/index/io.rs) contains `NodeBlockReadStats`, not traversal.
Preparation-parallelism admission moved to `index::prepare`. Related recent,
cache, metadata and preparation tests moved with their private owners; the
recent-log subprocess test's exact module name was updated too. This organization
change retains algorithms and does not itself establish a new behavioral result.

**Retained cost limits and unreviewed scope:** merge reuses unchanged content
but scans the root's directory references and each affected directory's leaf
references. It is not uniformly `O(log n + changed datoms)`. Current recent
reads buffer a logical E/A/V event group to select stored-value winners; cursor
memory is not always one datom. Recent BTSet fanout/split differences noted
above remain deliberate retain decisions pending workload evidence. This
review inspected the named production bodies/callers and selected source
counterparts, not every codec branch, reverse-boundary case, noHistory case,
excision/fulltext algorithm, GC/protection race or scheduler failure path.

## IM-GUIDANCE: operational claims and remaining evidence

Passage: [Usage Notes](01_index_model.md#usage-notes), baseline 111–118.
The first three bullets follow the order/membership trace above. Transparent
caching and workload-specific residence are handled by the caching companion.
The comparative latency, millisecond fetch examples, fully cached small-database
case and storage-performance advice are operational guidance, not benchmark
results reproduced by this change. The final capacity requirement routes to
`transactor::service` backpressure and indexing; scheduling acceptance remains with
that component. No test result, RSS reduction, latency improvement or complete
indexing-policy equivalence is asserted here.
