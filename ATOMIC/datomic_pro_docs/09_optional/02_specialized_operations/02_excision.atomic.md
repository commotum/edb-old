# Atomic implementation trace — excision lifecycle

Development companion to [Excision](02_excision.md), not recovered documentation.
This slice concerns scheduling, durable rewriting, publication and reclamation;
it is not a full audit of predicate admission or all privacy requirements.
Source coordinates use baseline `cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`.

## A transactional request is not a completed removal

Passages: “Implementation,” “Excision and Reference Attributes,” and “Tracking
Excisions.” [excise.clj](../../../1.0.7705/transactor/src-clj/datomic/excise.clj)::
`pred-and-extent` (174) bounds removal strictly before the request entity's time,
and additionally before an optional earlier cutoff. `component-es-set` (48)
uses history as of the request; `keeper?` (141) preserves protected datoms.
The request remains evidence that forgetting was requested; later assertions
are not indefinitely removed by an old request.

Rust's [excision.rs](../../../src/excision.rs)::`ExcisionPlan` owns the pure
predicate/extent model. [storage/excision.rs](../../../src/storage/excision.rs)::
`ExcisionJob` owns resumable work refs/checkpoints and staged generations.
Keep that direction: storage drives the predicate; predicate computation must
not acquire leases, publish roots or delete objects. The source/metadata/plan
checks on resumed work prevent silently reusing work from a different generation.

[transactor/excision_operator.rs](../../../src/transactor/excision_operator.rs)::
`process_requests` owns the administrative claim/renew/admit/adopt/release loop;
`ExcisionJob` remains shared checkpoint/rewrite/candidate machinery, and
`sync_requests` is read-only. The operator consumes small immutable generation
and request-count summaries rather than moving writer authority into shared
storage or the pure predicate model.

## Index publication, log rewriting and adoption are distinct

Passages: “How it Works” and “Performance.” Source
[update.clj](../../../1.0.7705/transactor/src-clj/datomic/update.clj)::`process-request-index`
(800) freezes indexing state, ensures log segmentation, then calls
[index.clj](../../../1.0.7705/transactor/src-clj/datomic/index.clj)::`merge-db`
(6346). `merge-db` itself conditionally installs the durable index ref. When it
returns excision predicates, the worker calls `log/excise` (1706), queues
`:excise-root`, and waits for completion before queuing `:new-index`.
`process-new-index` (693) adopts the completed result in the process's current
database and releases its indexing permit; it is not the earlier storage CAS.
These separate source publications must not be described as one atomic combined
index/log replacement.

The overlap handling is explicit.
[db.clj](../../../1.0.7705/transactor/src-clj/datomic/db.clj)::`prepare-for-indexing`
(5599) moves existing
novelty into `:indexing` and starts a fresh `:memidx`; `complete-indexing` (5619)
requires a newer revision at the frozen `indexingNextT`, retaining subsequent
memory-index additions. `log/excise-root` (1730) receives the log-treeifier's
current root. If treeification replaced a directory while excision was running,
it relocates the affected segment and patches its replacements into that current
directory. `update/writer-process :adopt-tree` (1619) then calls `log/adopt-root`
on the writer's current log, keeping the tail newer than the tree checkpoint.
Only after that conditional publication does it mark superseded IDs and deliver
the completion promise. A treeifier atom reset is not the durability boundary.

Source `excise-ts`, `excise-dir-map` and `write-excised-log` (1588, 1608, 1629)
select affected transactions/segments and retain unaffected segment references.
This is not Atomic's full-log scan. Predicate discovery and index rewriting have
their own costs; selection alone does not establish a bound for the whole job.

Rust deliberately constructs one replacement generation: `rewrite_step` filters
canonical log entries while preserving basis/allocation coordinates;
`index_step` uses the same index/fulltext preparation path; `receipt_step` replaces
old caller-key outcomes with occupancy tombstones and creates sanitized basis
receipts. `BlockTransactor::adopt_excision` validates the current source, exactly
next generation and unchanged allocation/time coordinates, then atomically
publishes the combined root, clears work and restore checkpoints, and establishes
a bounded report handoff. It does not create another history transaction.
The source does not establish Atomic's exact-receipt/tombstone protocol.

## Explicit differences and costs

In [transactor/excision_lane.rs](../../../src/transactor/excision_lane.rs)::`ExcisionLane::poll`, successful
writer admission starts the fresh-write pause before rewrite/index/receipt work;
the serialized service continues lease renewal and receipt lookup, parking fresh
requests behind their post-receipt gate. Discovery/admission happens off-thread
before that pause. This is a potentially whole-database pause, not merely an
atomic activation pause; source background indexing continues alongside processing
unless its memory pressure stops fresh work. Preserve the native frozen-source
invariant unless a separately demonstrated incremental catch-up design replaces
it. Bounded work per checkpoint does not establish bounded total pause duration.

The demonstrated per-entry reseek in `rewrite_step` is now replaced by the same
forward `LogRange` used by ordinary range readers. A crate-private
`next_record_bounded` uses the existing encoded-entry check before chunk fetching
and datom decoding; its public unbounded entry method delegates to the same code.
Source reads use the job's already-owned authenticated `BlockReader`, allowing
guarded writes to continue on the work connection without a second traversal
implementation. Source IDs stay frozen; the cursor does not recapture the live
root. Existing per-entry and cumulative checkpoint admission calculations remain.

The new actual-path test
`rewrite_checkpoint_uses_forward_source_reads_and_retries_admission_without_progress`
rejects a too-small entry budget without moving the checkpoint, restores the
budget, then rewrites 130 transactions across three pages. It expects 130 source
entry reads plus two sealed-page reads, preserved basis order, and no activation
of the candidate during rewrite. The shared cursor's independent bound test
proves an oversized chunked entry fails before the missing chunk is fetched and
fuses the iterator. The integrated library run passed the actual-path regression:
`EXCISION_FORWARD basis=130 source_object_reads=132 totalSqlCalls=14896`.
Those total driver calls include guarded candidate preparation, not just source
navigation. Index rebuilding and receipt rewriting still add whole-history work;
the read-shape improvement is not a claim of cheap excision or bounded pause.

The reference's “Limitations” excludes fulltext excision and disclaims complete
noHistory removal. Rust rebuilds its native fulltext attachment from sanitized
data, and walks its canonical log rather than importing those source limitations.
This is a deliberate capability difference, not evidence of Lucene/source parity.
Captured old values and external backups are not rewritten in place. Old storage
can also survive through report handoff/retirement grace; completion is not proof
that every plaintext copy has already disappeared from disk or memory.

## Reclamation and verification boundary

After logical removal, source `garbage/gc` (555) deletes superseded IDs recorded
before a supplied timestamp. Native collection follows published ownership and
epoch protection; see the [provider lifecycle note](../../08_operations/00_architecture_and_storage/00_storage_services.atomic.md#atomic-note-lifecycle-reclamation-boundary).
Retain that separate collector owner; an excision predicate is not authority to
delete a content-addressed object still owned by another root.

Permanent checks in [storage/excision_tests.rs](../../../src/storage/excision_tests.rs)
cover automatic sanitization/reopen versus held values, checkpoint resume,
failed-admission write availability, shared operator checkpoints, and later
plaintext reclamation after grace. The coordinated run passed the existing
automatic/restart/excision/GC integration checks; this source-review subtask did
not independently rerun them. It does not
audit arbitrary backups, operating-system caches, all concurrent mutation schedules
or legal retention policy.
