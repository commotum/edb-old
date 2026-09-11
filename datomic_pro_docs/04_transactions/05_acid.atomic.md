# Atomic implementation trace — log, roots and exact outcomes

Development companion to [ACID](05_acid.md), not recovered Datomic prose.
This component trace covers storage publication, log checkpoints, receipt
preservation and serialized service ownership. It does not claim wire-format parity.
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
[authority/mod.rs](../../src/transactor/authority/mod.rs)::`transact_captured`; index adoption
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

At the baseline, `page_for` (517) seeks backwards from the captured endpoint
in O(log P) page reads, and `LogRange::next_record` (559) repeats that seek at
each 64-entry boundary. This can cost O(Q log P) navigation reads over Q pages.
S-LOG's `LogTxIter.next` instead retains root/directory/segment positions and
advances to adjacent segments/directories after its initial seek.

**Implemented Stage 2 adaptation:** the Rust range cursor now retains a stack
of greater-index ancestor pages. At a boundary it pops the nearest ancestor,
descends its backward skips to the target, and keeps encountered parents for
later forward steps. Each fetched page is yielded once or retained as a future
ancestor: O(Q + log P) page fetches and O(log P) retained pages, without a global
cache or eager history scan. Page skip arrays vary with log size, so this is not
a constant-byte memory claim. Only the captured tail may be partial; loaded
pages still undergo the same hash, shape, coordinate and sealed-page checks.
An error fuses the iterator. Deep global skip-lineage validation remains the
separate `validate_structure` operation, not an added cursor guarantee.

New inline tests compare the old repeated-`page_for` oracle with full, partial,
empty and early-stopped ranges at 8/13/32/65/128 pages, including non-power-of-two
shapes and a partial final page.
They check returned values, per-page read counts and retained ancestor bounds;
the error test checks both bad coordinates and an unsealed predecessor, including
a failure after two emitted pages. The sample durations measure in-memory
object traversal, not PostgreSQL latency. These tests passed in the Stage 2
448-test library run with PostgreSQL configured. The existing [block_log.rs](../../tests/block_log.rs)
PostgreSQL navigation fixture also checks its complete 1,091-entry, 18-page
range against independently constructed entries. Its separate sample reports
elapsed collection time, driver SQL calls and payload bytes, asserting 1,108
calls: one per inline entry plus 17 pages, excluding the already captured tail.
This reuses the existing connection/fixture with uncontrolled PostgreSQL/OS
cache warmth, not a cold-read or throughput benchmark. The live range check
passed at 1,108 calls, 135,550 payload bytes and 719,247 microseconds; all five
block_log cases passed. The on-disk format and random-seek method are unchanged.

**Disposition — retain/adapt:** retain Rust's immutable endpoint/page scheme
and this stateful cursor, without importing the compatibility pod layout or
Datomic's read-ahead policy. This changes forward navigation, not transaction
writes. Source batching/checkpoint amortization still does not establish Rust's
write parity; the complete-path amplification below remains unresolved.

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
  (now [storage/protection.rs](../../src/storage/protection.rs)). Its ordinary object writes then enter
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

**Stage 3 disposition — repair the measured multiplier:** bounded object uploads
now share the unchanged protection guard set as described below. Both insert-only
receipt edits reuse pending nodes through read-your-writes. They still produce
intermediate immutable nodes; no claim of minimal path-copy output is made.
Keep their occupied-key contract rather than substituting the existing general
replacement/deletion batch API. Reused-content protection, source/lease/GC fencing
and exact lost-ack replay remain unchanged. The complete service-phase check
measured 114 driver calls before and 32 after; replay stayed at 17. Local debug
total/encoding/commit samples were 46.86/36.03/8.18 ms before and
15.40/7.06/6.51 ms after. Concurrent activity and warm-cache conditions differed:
these are samples, not a controlled latency ratio or production throughput.

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
guards in orchestration. At the baseline these log/receipt builders accepted
concrete `PgBlockStore`. The Stage 2 repair now accepts the existing
`ObjectWriter` and calls `put_object`; read-only batch editing needs only
`ObjectReader`. No new backend or publication policy was introduced.
At that ownership-repair boundary, `PgBlockStore`'s trait adapter delegated each
write to protected `put`; the repair alone did not batch writes. The subsequent
[transaction-local upload barrier](#atomic-note-transaction-local-upload-barrier)
below now groups transaction preparation while preserving encodings, chunk limits,
guard authority and reference publication. Measured complete-path cost remains a
separate acceptance question.

A new test-only byte collection exercises
`receipts::object_writer_tests::opaque_writer_preserves_log_receipt_and_request_index_structures`:
log page rollover and ordered range reads, chunked receipt roundtrip, request
index edits against a `BTreeMap` oracle, retained prior roots, occupied-key
rejection without writes, and unchanged error propagation after a staged entry.
It implements object I/O only, not a fake PostgreSQL/GC protocol. It passed in
the Stage 2 full library run, followed by all four real block_receipts cases.

## ACID-PRIMITIVES: small provider boundary and its safety conditions

Follow-up review of the current-version bodies supports the following narrow
boundary; it does not prove every collector schedule or call site correct.

| Primitive to retain | Mechanism and responsibility |
| --- | --- |
| Immutable object read/write | Return authenticated canonical bytes, preserving ordered bulk results and duplicates. Hash identity is independent of the current raw/compressed physical representation. Re-put must not overwrite an existing payload and must reject corrupt existing content. This content-addressed guarantee is an Atomic adaptation; source log builders use generated value keys. |
| Revisioned reference read/CAS | Opaque bytes plus a monotone revision; absence differs from a revisioned tombstone. Every changed key has a guard, and a batch publishes all changes or none. The provider does not decide whether a key denotes a database, lease, receipt or ownership clock. |
| Guarded protection/removal | Caller supplies authority guards and numeric epochs; the provider atomically checks them with monotone protection or exclusive-threshold deletion. The engine, not the provider, chooses epochs, proves unreachability and applies retention age. |

**Observed correctness mechanisms:** `protocol::validate_batch` rejects duplicate
guards/changes and unguarded writes before I/O. PostgreSQL transactions explicitly
use READ COMMITTED, acquire namespace-qualified advisory locks in sorted order
(including absent keys), then lock/read guarded rows. This matters after waiting:
the guard must reflect current committed state, even if the connection default
is REPEATABLE READ. Only matching revisions reach the writes. Tombstones retain
revisions across delete/recreate and reject a stale observer even when bytes
return to an earlier value. Protocol participants must use this guarded path;
administrative SQL can bypass it.

`put_protected` checks the same guards as its mutation, authenticates the returned
existing/new payload before commit, and raises `protected_epoch` with `GREATEST`.
Thus deduplicating a previously abandoned object does not leave it eligible for
an older sweep. `protect_existing` stamps caller-selected objects but does not
authenticate their graph; its engine callers must already have done so.
`remove_unprotected` combines the collector's guards with
`protected_epoch < before_epoch`. A displaced collector changes nothing; a
current protected stamp wins over an older delete threshold. Future batching
must retain these atomic properties, not simply perform an early guard read.

**Lost-ack caveat:** authenticated `get(id)` establishes content presence, not
that a particular guarded put refreshed protection. An old deduplicated object
may predate the failed attempt. The corrected `PgBlockStore::put` documentation
now distinguishes content presence from protection; repeat the guarded operation
or establish its applied protection. Current transaction callers
propagate these errors. By contrast, S-POD's retry witness checks the installed
revision and tail identity; payload existence alone is not its success test.

**Readers versus collection:** `BlockReader::capture_reference` (snapshot.rs:465)
observes the reference once; `capture_immutable` (955) follows that captured
immutable value without registering a reader or changing reference state.
Another publication need not make the captured value fail or switch endpoints.
The `capture` documentation and read_authorization module header were corrected
to match these current bodies. Reopening an untrusted embedded value authenticates
its canonical log/metadata and published covering index against retained
authority; merely possessing a valid content hash is insufficient.

Durable root/receipt/index-authority edges and retirement grace protect storage,
not the lifetime of each in-process Arc. `ownership::publish_refs` records root
additions/removals atomically with publication. The collector seals an epoch,
folds additions before removals, checkpoints folded counts before sweeping,
and retains retired objects for the supplied age. S-POD only exposes a callback
marking replaced tail candidates after reset; its selected body does not establish
the same epoch, ownership or grace algorithm. Missing required objects fail
closed. Indefinite availability of a removed generation or unowned old index
does not follow from immutable bytes alone.

**Retain/adapt recommendation:** keep object/opaque-CAS/protection primitives
small and separate; keep root classification, reachability, retention and
publication decisions in Rust orchestration. Read-only consumers must not need
write protection merely to read. Preserve protection when adapting codecs to
`ObjectWriter` or coalescing writes. This is a concrete current-contract boundary,
not justification for all existing collector complexity or the open write cost.

**Relevant checks inspected, not rerun in this review:**

- [block_storage.rs](../../tests/block_storage.rs):
  `tombstones_preserve_revision_and_prevent_recreation_aba` (293),
  `guarded_reference_batch_is_atomic_and_leaves_read_only_guards_unchanged` (345),
  `protected_reuse_survives_sweep_and_stale_collector_cannot_delete` (510).
- [block_storage_isolation.rs](../../tests/block_storage_isolation.rs):
  `guarded_cas_rechecks_condition_after_wait_with_repeatable_read_default` (31).
- [compression_tests.rs](../../src/storage/compression_tests.rs):
  `postgres_compressed_objects_reput_canonical_bytes_and_reject_corruption` (104)
  also checks that corrupt-object rejection rolls back the protection stamp.
- [protection_tests.rs](../../src/storage/protection_tests.rs): missing covering
  root fails closed (106); exact reopening retains its observed publication
  across a racing index adoption without reader coordination (162).
- [block_backup_roles.rs](../../tests/block_backup_roles.rs): SELECT-only capture
  survives retirement within configured grace (69). This is not evidence of an
  unbounded reader pin. [block_gc.rs](../../tests/block_gc.rs) (72) checks live
  peer values and exact receipts across collection/reopen.

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

- [authority/tests.rs](../../src/transactor/authority/tests.rs),
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

## ATOMIC-NOTE: transaction-local upload barrier

The bounded implementation below addresses the per-object guard multiplier in
ACID-COST without changing transaction assessment, receipt semantics or ownership
publication. The coordinated service check measured fewer driver calls as above;
this is not a claim of pipeline equivalence.

**Current call graph:** [authority/mod.rs](../../src/transactor/authority/mod.rs)::`transact_captured`
first looks up the exact request receipt, before fresh gates, epoch/lease checks
and assessment. Only an absent request enters immutable preparation. The engine
then writes retained programs, log entry/chunks/page, metadata, before/after value
descriptors, the exact receipt, two request-index paths and the publication root.
Previously the [ObjectWriter](../../src/storage/object_io.rs) adapter invoked
one protected put per object. [put_many_protected](../../src/storage/object_batch.rs)
authenticates and protects a bounded group in one SQL transaction;
`guarded_refs` in [postgres.rs](../../src/storage/postgres.rs) repeats its ordered
advisory locks and guarded-row reads once per group instead of once per object.
Publication/reference reads and existing Patricia-path reads remain; pending
new Patricia nodes are now read from the transaction-local buffer.

**Implemented API:** the shared `ObjectWriter` contract is explicitly refined:
`put_object`/`put_objects` accept canonical bytes and provide their content IDs
with read-your-writes; a required `flush_objects` method is the persistence and
existing-content authentication barrier. This is an explicit change from the
previous per-put persistence promise. `PgBlockStore`, `backup::RepositoryCopy`
and the test byte collection explicitly implement an immediate/no-pending barrier.
The log/receipt headers document caller-owned flushing; indexing and fulltext
statistics describe accepted objects/bytes, not SQL calls or a durability proof.
The live index-preparation path still takes an immediate `PgBlockStore`; repository
puts still sync new files/directories and authenticate existing bytes. No parallel
log/receipt algorithms or additional provider were introduced.

A private [transaction-local writer](../../src/storage/object_io/staging.rs) owns
pending canonical bytes. Its only production construction/escape path is
`with_staged_transaction(store, protection, |objects| -> fields)`. This first
checks that the supplied `WriteProtection` exactly matches the installed provider
context, then exclusively borrows the provider through the object-only interface:
the builder cannot change guards or publish refs.
The helper runs the existing builders, flushes successfully, then returns their
fields. On builder or flush error it returns no fields; drop performs no I/O or
publication. Pending reads use the local bytes; older reads use the provider.
This supports the second insert reading the first insert's new Patricia path.
The two insert-only calls are retained: `RequestIndex::apply_batch` supports
replacement/deletion, so substituting it would lose occupied-key rejection
without a separate proven insert-only change.

The implemented sequence is:

`receipt lookup → fresh checks/assessment → guarded staging + bounded flushes
→ capture_successor → ownership::publish_refs → report`

All objects, including receipt/request-index/publication-root objects, finish
flushing before `capture_successor`. Its current contract requires durable log
and root bytes; it validates coordinates and extends recent indexes, with its
schema callback reading only `db-before`. Moving this call after the helper
avoids exposing an unflushed `BlockSnapshot`. A later capture error can leave
unpublished objects, as other prepublication failures already can. The root,
lease, ownership event and clock remain atomically published by
[ownership::publish_refs](../../src/storage/ownership.rs); an upload batch does
not replace that operation.

**Necessary safety and cost limits:**

- Every flush uses the same captured source/lease/GC conditions and epoch;
  never refresh stale guards mid-attempt. Final publication checks them again.
  A GC change between flushes or before publication rejects the attempt, even
  when some earlier objects are durable. Those objects are not a transaction.
- Do not skip a provider write because an ID is cached or already exists.
  Newly referenced retained programs and deduplicated orphan content still need
  guarded epoch protection. The provider's `GREATEST` protection touch, returned
  payload authentication and rollback on corrupt reuse remain mandatory.
- Buffer whole objects into groups no larger than 128 objects/64 MiB canonical
  bytes, with an implemented 4 MiB target for pending groups. One larger admitted
  object is held alone, still bounded by the 64 MiB object ceiling. A transaction's
  total encoded objects can exceed one group; do not introduce a 64 MiB
  whole-transaction staging rejection or split opaque object bytes arbitrarily.
  Release successful groups and bound pending storage; this bounds owned bytes,
  not process RSS, serializer copies, driver buffers or assessment memory.
- A lost flush acknowledgment is not resolved by cached/local bytes or `get(id)`:
  those do not prove refreshed protection. Propagate uncertainty or repeat the
  guarded upload. A lost publication acknowledgment is resolved through the
  existing exact receipt path before fresh admission, without reassessment.

**Source comparison, separately:** S-WRITER `fressianer` (baseline 1358) queues
encoded transactions; `writer` (1658) drains ready transactions up to its byte
threshold and passes the ordered batch to S-LOG `LogImpl.append` (1188).
[io.clj](../../1.0.7705/transactor/src-clj/datomic/io.clj)::`unchunk` (452) copies
those buffers without advancing their inputs. Conditional `write-tail-descriptor`
completion precedes delivery of each `:logged` promise; `block-notifier` (1785)
waits for that promise. Separately, `zip-and-create` (600) returns asynchronous
create results and `extend-tree` (2263) dereferences all of them before returning
the candidate checkpoint. These are distinct acknowledgment boundaries.
Transaction-local SQL upload grouping recreates neither the overlapping worker
pipeline nor batching several transactions into one log-tail publication.

The buffer validates returned provider identities and preserves the first flush
error. Later reads, puts and flushes return that error, even when a builder catches
it and returns `Ok`: the helper's mandatory final barrier still rejects the attempt.

**Focused checks:** all nine tests in
[staging/tests.rs](../../src/storage/object_io/staging/tests.rs) checks read-your-writes,
duplicate ordered IDs, actual 128+1 splitting, byte-target splitting, larger-object
isolation, closure errors/unwind without Drop I/O, prefix-only persistence after
later failures, caught-error poisoning, underlying barrier failure and wrong
provider IDs pass. The added
`transaction_upload_barrier_precedes_publication_and_exposes_both_receipt_paths`
in [authority/tests.rs](../../src/transactor/authority/tests.rs) uses an independent
PostgreSQL connection at the existing prepublication hook: the old published root
must remain unchanged while the entire new candidate graph, both receipt addresses,
exact before/after coordinates and log entry are already readable. Its first
run exposed a test-only assumption that every stored object was ATOB; tree nodes
have ATIX encodings. The witness now filters candidates by the root family and
traverses authenticated children with the actual canonical-family dispatcher.
That corrected witness passed in the coordinated 463-test library run with
real PostgreSQL. The same run passes final-CAS rejection, stale-writer fencing,
lost-ack replay and stalled local observation. A separate application-level
5 MiB transaction spans two canonical log chunks and multiple upload groups,
then releases/reclaims the writer and replays the exact receipt; it passes in
[block_transactions.rs](../../tests/block_transactions.rs). Its complete
commit/reopen/retry sample took 6.89 s in debug alongside other checks. The
repeated-text fixture is not a compression, throughput or RSS benchmark.

The real [block_upload_batch.rs](../../tests/block_upload_batch.rs) tests pass:
corrupt reuse, stale guards and interrupted insertion cannot partially succeed.
Existing engine final-GC-CAS, replacement-writer, receipt-before-gate, lost-ack
and reopen cases pass. The service-phase check also proves callback invocation
once, prepublication rejection and unknown-outcome reconciliation. Buffer tests
bound recorded group bytes/counts and inject multigroup failures; they do not
measure process RSS. Final-tree integrated acceptance remains pending.

## ACID-SCHEDULING: retain one fresh transaction authority, expose the tradeoff

Source `process-transaction` advances its internal `db-ref` before logging;
`fressianer`, `writer` and notification encoders overlap successive transactions.
`writer` drains ready work and fails the process on append error; peers only
receive results after `:logged`. This is real multicore and commit-amortization
leverage, not an accidental language detail.

Native disposition: retain one fresh transaction through assessment, upload and
publication, while index/excision preparation, prefetch and application reads
remain independent. This deliberately avoids a second queue of assessed but
uncommitted successors and the dependent failure/discard states for those values.
The service's one `Ambiguous` intent must be reconciled before another fresh
callback or maintenance result is consumed. That is a concrete state/ownership
simplification for exact native retry; Rust does not make pipelining unnecessary.

The cost is equally explicit: fresh assessments do not overlap another
transaction's log I/O, each transaction publishes its own receipt/root, and one
writer limits write throughput. The upload repair removes avoidable per-object
coordination without promising concurrent fresh writes or source-equivalent
throughput. Keep complete-path costs visible and exercise concurrent submissions,
index pressure and ambiguous outcomes; do not infer capacity from a small empty
transaction sample. A future pipeline would need measured workload justification
and the same durable outcome contract, not just additional threads.

The isolated complete-path `block_live_costs` run passes after the cutover:
2,048 entities/1.5 MiB scalar payload with 1 MiB configured caches, 24 small
writes, three automatic indexing jobs, sync, warm reads and shutdown. Startup
through shutdown takes 13.73 s/1,889 driver calls; the 24 writes take 0.543 s,
or 4.74 s including automatic indexing completion. Seed commit takes 2.10 s,
or 8.24 s including its index. Thirty-two warm reads use zero SQL. No other
tests or builds ran during this sample, but OS/PostgreSQL caches remain warm
and uncontrolled; this debug run is not production throughput or an A/B ratio.

## ACID-OWNERS: data transfer and publication authority

[transactor/authority](../../src/transactor/authority/mod.rs) owns the lease,
receipt-first fresh-write path and guarded adoption. The shared
[storage/catalog](../../src/storage/catalog/database.rs) owns names, identity
and genesis; [storage/protection](../../src/storage/protection.rs) assembles
opaque collection guards. Reading a lease to validate endpoint discovery or an
administrative restore guard is deliberate composition, not starting a writer.
Normal snapshot, index and log reads do not depend on a running authority.

[writer.rs](../../src/transactor/writer.rs) orders uncertain-outcome resolution,
renewal, prepared results and admitted requests. Independent index and excision
lanes prepare candidates, not publish authority. Requests, client admission,
observations, activation and standby have separate modules with explicit imports;
the former root service/runtime/engine paths are removed, not forwarding aliases.

S-WRITER `block-notifier` waits for the logged promise and sends encoded data;
it does not invoke application callbacks on the authoritative processor.
Atomic's only local commit observer previously wrapped `try_send` in a generic
closure. The native registration now stores bounded `SyncSender<Arc<Report>>`
values directly. Full hint slots may drop hints because peer catch-up uses the
durable log; they cannot block the writer by running application code. This is
distinct from the explicitly lossless transaction-report queue. Registration
and removal retain their prior locking and lifetime behavior. The existing
`connection::tests::stalled_local_observation_does_not_stop_the_authoritative_writer`
regression exercises a paused peer and full hint slot; current cutover execution
is tracked in Goal 0 rather than inferred from compilation.
