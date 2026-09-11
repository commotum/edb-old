# Atomic implementation trace — log, roots and exact outcomes

Development companion to [ACID](05_acid.md), not recovered Datomic prose.
This selected Stage 2 slice covers storage publication, log checkpoints and
receipt preservation. It does not establish whole-chapter or wire-format parity.
Coordinates below refer to unannotated revision
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`; use named symbols after comments move
the lines. This revision identifies recovered renderings, not runnable upstream
source. Only the artifact-qualified passages below were compared.

## Source and current Rust owners

| Locator | Selected source symbols at the baseline |
| --- | --- |
| S-LOG | [transactor log.clj](../../1.0.7705/transactor/src-clj/datomic/log.clj): namespace doc (6), `fressianed-tx` (427), `write-tail-descriptor` (547), `Tail` (686), `LogImpl.adopt-root` (1179), `LogImpl.seek-tx-impl` (1264), `root-id` (2052), `create-leaves*` (2121), `extend-tree` (2263). |
| S-CLUSTER | [transactor cluster.clj](../../1.0.7705/transactor/src-clj/datomic/cluster.clj): `ClusteredStore` (143), `update-pod` (437). |
| S-POD | [transactor kv_cluster.clj](../../1.0.7705/transactor/src-clj/datomic/kv_cluster.clj): `KVCluster.update-pod*` (250), `get-pod` (432), `get-pod-meta` (569). |
| S-WRITER | [transactor update.clj](../../1.0.7705/transactor/src-clj/datomic/update.clj): `writer-process :adopt-tree` (1619), `writer` (1658), `extend-tree` (1899). |
| S-INDEX | [transactor index.clj](../../1.0.7705/transactor/src-clj/datomic/index.clj): `init-index` (2004), `find-index-root-id` (2019), conditional index-root `set-ref` (6436). |

The logical Rust mechanisms currently live in [storage/log.rs](../../src/storage/log.rs),
[root.rs](../../src/storage/root.rs), [descriptors.rs](../../src/storage/descriptors.rs)
and [receipts.rs](../../src/storage/receipts.rs). Transaction publication is
[engine.rs](../../src/storage/engine.rs)::`transact_captured` (790); index adoption
is [index_publication.rs](../../src/storage/index_publication.rs)::`publish` (73).
These are database-engine structures, not PostgreSQL payload interpretations.
The [provider trace](../08_operations/00_architecture_and_storage/00_storage_services.atomic.md)
covers the lower opaque-object/reference boundary separately.

## ACID-PUBLISH: staging versus the atomic visibility point

Passages: [Atomicity](05_acid.md#atomicity), paragraph at line 17;
[Isolation](05_acid.md#isolation), write-CAS bullet at 42;
[Durability](05_acid.md#durability), paragraph at 48.

**Observed source:** S-LOG appends encoded transactions using the prior pod
descriptor and its next revision. S-POD first writes the immutable tail chunk,
then conditionally installs its pointer and metadata in the pod row. Thus
prepublication values may exist without being part of the database. S-WRITER
awaits `log/append` before delivering its messages' `:logged` promises. S-CLUSTER
documents rev/etag requirements; its wrapper validates metadata keys and
delegates the actual conditional update.

**Rust trace:** `transact_captured` stages a log entry/page, metadata, immutable
before/after value descriptors, receipt, request-index paths and publication
root. Only `ownership::publish_refs` makes the root and renewed lease visible
together, under captured source/lease/collection guards. An unsuccessful final
guard leaves no newly published transaction or receipt; staged objects may
still require collection. A lost acknowledgment can leave a committed outcome.

**Disposition — retain/adapt:** retain immutable staging followed by guarded
publication and acknowledgment. Atomic's guarded multi-reference batch is a
native adaptation for its lease and collection contract, not evidence that
Datomic uses the same guards. The doc's “single atomic write” describes the
atomic database transition; it is not evidence that Rust issues one physical
write or one SQL statement per transaction.

## ACID-LOG: append representation and checkpoint costs

Passages: [How It Works](05_acid.md#how-it-works), log/index bullets at 54–55,
immutable-values paragraph at 57, batching explanation at 67.

**Observed source:** the log is a tree plus a tail pod. Its namespace doc
explicitly attributes the subtle pod arrangement to compatibility, not best
practice. S-LOG `fressianed-tx` selects `:id`, `:t`, `:data` and resets Fressian's
cache context per transaction. `Tail` holds decoded transactions and matching
encoded buffers. Append concatenates only the new batch's buffers. S-POD stores
that batch as one immutable `:prev`-linked chunk, then conditionally advances
the pod row. `get-pod` captures the row and reads the complete linked tail:
one read per chunk plus O(tail bytes) copy/storage for its joined buffer.
`get-pod-meta` instead reads only the row.

S-LOG `create-leaves*` groups encoded bytes by target weight and merges a short
last group; the target is not a hard maximum. `extend-tree` writes the new
segments, replacement tail directory and replacement root, optionally coalescing
directories. It waits for all creates before returning the candidate. This
copies segment bytes and encodes directory/root arrays, not constant work per
checkpoint. S-WRITER builds from a captured tail in the background, then queues
adoption on the serialized writer. `adopt-root` keeps the current tail's
transactions newer than the checkpoint and resets the pod to the new root plus
that remainder. The candidate alone cannot replace newer appends.

**Rust trace:** `LogRoot::append_encoded` (254) uses immutable pages of at most
64 entry IDs, with authenticated backwards skip links. Ordinary append copies
the current page's bounded entry array and skip array, writes the entry and a
new page; large entries additionally use payload chunks. Opening a new page
constructs skips with O(log P) page reads for P pages. The skip array itself
also grows O(log P); “bounded page” does not mean every byte is a fixed constant.
No separate tail-pod segmentation process exists here.

`page_for` (517) seeks backwards from the captured endpoint in O(log P) page
reads. `LogRange::next_record` (559) caches one page but re-seeks from that
endpoint when crossing each 64-entry boundary. A forward range spanning Q
pages can therefore perform O(Q log P) navigation reads, before entry/payload
reads, absent lower-layer caching. S-LOG's iterator instead advances through
adjacent segments/directories after its initial seek. These are concrete cost
differences, not measured latency claims.

**Disposition — retain, with a bounded adaptation candidate:** retain Rust's
immutable endpoint/page scheme; do not import a compatibility pod protocol merely
because it appears in the source. Evaluate forward-range navigation separately
if workloads justify it. Source batching/checkpoint amortization does not prove
Rust matches the documented average of one or fewer writes per transaction.
The complete-path measurements below leave write amplification unresolved.

## ACID-COST: complete-path amplification remains open

The root agent's [recorded application campaign](../../goal-0/0-plan.md) used
2,048 records, 512-byte payloads and 500-ms phases, in debug with uncontrolled
warm PostgreSQL/OS caches. Seed took 13.175 s and 4,648 driver SQL calls. Mixed
writer phases recorded 710 calls for three writes and 1,195 for five writes,
about 237–239 calls per completed write and about 100 ms median latency.
Counts include maintenance; they are not an isolated per-transaction profile.
The root agent additionally reports `TransactionEncoding` accounted for 1,000
of 1,186 calls in the earlier 512-record campaign. This subtask did not rerun
either campaign. These results demonstrate working paths, not write parity.

**Observed call-graph multiplier at the baseline:**

- `transact_captured` installs source, lease and GC guards via `protection`
  (engine.rs:1344). Its ordinary object writes then enter
  [PgBlockStore::put](../../src/storage/postgres.rs) → `put_protected` →
  `guarded_refs`. Each object gets a separate transaction: begin, one advisory
  lock query and one `SELECT ... FOR UPDATE` per guard, one
  `INSERT ... RETURNING`, and commit. Three guards mean nine observed driver
  calls per protected object in the uncontended path; [sql_io.rs](../../src/sql_io.rs)
  counts begin/commit too. Deduplication still protects and authenticates the
  existing payload; it does not skip this sequence.
- Even an inline transaction has seven non-trie `put` calls: log entry, log
  page, snapshot metadata, before/after value descriptors, receipt and database
  root. Retained programs and large payload chunks add more. This is a call
  count, not a claim that all seven contents are new or that seven is the whole
  transaction's SQL cost.
- `RequestIndex::insert_inner` (receipts.rs:526) writes a leaf/split branch and
  each copied ancestor. The two sequential fresh-key inserts in the engine
  persist both paths, including an intermediate index root and any shared
  ancestors rebuilt again. Each node write pays the protected-write multiplier;
  traversal and the initial replay lookup also fetch objects. Logarithmic
  depth does not make this complete path cheap.
- `capture_successor` validates and extends the resident recent indexes; it
  does not publish another durable root here. The later
  [ownership::publish_refs](../../src/storage/ownership.rs) does reread GC/root
  references and an ownership clock, adds an ownership event and clock update,
  and invokes the guarded database-root/lease batch. Lease refresh, schema
  work and campaign maintenance add further costs.

**Inference:** repeated per-object guard transactions and two independently
persisted receipt paths are plausible major contributors to the measured
encoding/publication amplification. The code establishes the multiplier, not
an attribution of every measured call or millisecond.

**Open Stage 3 disposition — measurable repair or concrete justification:**
retaining exact receipts, immutable endpoints and collection protection does
not justify this implementation's whole-path cost automatically. Evaluate
bounded object-write batching under one unchanged protection guard set and
coalescing both insert-only receipt edits before persisting shared paths.
Any change must preserve reused-object protection, occupied-key rejection,
final source/lease/GC fencing and exact lost-ack replay, and report the same
complete-path counters/latencies before and after. A generic writer trait or
green correctness/read-cache tests alone do not close this issue.

## ACID-ROOTS: domain refs versus a coherent descriptor

Passages: [How It Works](05_acid.md#how-it-works), durable refs at 59;
[Implications](05_acid.md#implications), conditional log/index-root bullets at
78–79 and missing-immutable-node paragraph at 81.

**Observed source:** the log tree pointer is `:d/r` in its tail pod metadata;
S-INDEX uses a separate revisioned index-root ref. A log-tree checkpoint and
an index checkpoint are different operations. References require stronger
consistency than immutable payload fetches. S-POD throws if a captured tail
link's bytes are missing; it does not silently skip the transaction.

**Rust trace:** `DatabaseRoot` (root.rs:114) coherently names log, indexes,
receipts, metadata and read authorization, with a writer epoch distinct from
transaction basis. `IndexDescriptor` (descriptors.rs:16) contains current/history
tree roots, basis/generation, pending AVET work and optional fulltext attachment;
`SnapshotMetadata` (241) captures allocation/time/excision coordinates.
`DatabaseRoot::adopt_indexes` (259) retains the other root fields. The adoption
caller authenticates the candidate's current log prefix and index lineage,
then conditionally publishes against the current capture; a stale capture is
rejected. Retrying from a new capture can preserve transactions newer than the
candidate's indexed basis.

**Disposition — retain/move:** keep the coherent publication descriptor and
separate value descriptor, rather than splitting root fields merely to match
Datomic's domain refs. Keep codecs, traversal and descriptor checks in the shared
database engine. Keep source selection, lease/epoch admission and publication
guards in orchestration. Reads already use `ObjectReader`; these log/receipt
write helpers still accept concrete `PgBlockStore` at this baseline. Moving
their opaque-write dependency behind `ObjectWriter` is an ownership adaptation,
not permission to move database interpretation into provider SQL.

## ACID-RECEIPTS: preserved Atomic behavior, not inferred parity

Passage: [Durability](05_acid.md#durability), storage acknowledgment at 48.
This passage does not promise Atomic's exact caller-key retry contract.

S-POD recognizes a retried storage update by rereading the same proposed
revision/tail ID. S-LOG also stores transaction `:id`; identities are not absent
from this source. Neither selected mechanism demonstrates a durable request-key
index returning exact before/after values and original tempid names.

Atomic deliberately has that stronger representation: `ExactReceipt`
(receipts.rs:59) stores the request digest, transaction ID, before/after value
IDs and tempids. `DatabaseValueRoot` (root.rs:288) omits receipts and writer
authority, avoiding a content-addressed cycle back through its own receipt.
`RequestIndex` (receipts.rs:352) is an immutable compressed binary trie: lookup
and path-copy insertion visit at most 256 branching bits, with expected
logarithmic paths for hashed keys, rather than loading all receipts. Fresh
publication inserts both caller-key and domain-separated basis addresses.
This adds receipt payloads, two index-update paths, and retained value links.

`transact_captured` checks receipts before fresh-admission, epoch and lease
gates. **Disposition — retain:** preserve that order, exact replay and occupied
key checks. Index adoption retains the latest receipt root. Trimming/excision
and long-term receipt-retention policy are outside this selected audit; keeping
a receipt link is not by itself proof of every privacy/retention requirement.

## Verification and remaining scope

Existing checks inspected, not rerun for this documentation task:

- [engine_tests.rs](../../src/storage/engine_tests.rs),
  `gc_revision_change_at_final_cas_publishes_neither_transaction_nor_receipt`
  (412) and `injected_lost_ack_is_resolved_from_the_durable_receipt_after_reclaim`
  (499): final-guard rejection and exact replay after a lost acknowledgment.
- [index_publication_tests.rs](../../src/storage/index_publication_tests.rs),
  `moving_publication_fences_staged_root_and_retry_preserves_the_newer_tail`
  (204): stale publication fails; retry retains current log, basis, receipts
  and metadata while adopting the prepared index.

PostgreSQL-dependent checks need a configured fixture; early-return skips are
not runtime evidence. This trace does not audit the whole recovered log
namespace, old-format normalization, excision, collector completeness, all
failure schedules, domain validation, peer monotonicity or cross-peer `sync`.
The transactor artifact was selected explicitly; duplicate peer artifacts and
generated local-name differences do not establish whole-file equivalence.
