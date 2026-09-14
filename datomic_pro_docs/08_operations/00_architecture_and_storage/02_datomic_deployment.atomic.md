# Atomic trace — immutable capture and peer observation

This companion traces only “Getting Connected” and “Peer Failover” in
[Datomic Deployment](02_datomic_deployment.md), the related
[Connection API](../../../1.0.7705/peer/src-java/datomic/Connection.java), and
the immutable-value promise in [The Datomic Data Model](../../02_core_concepts/00_datomic_data_model.md).
“Use a Consistent Database Value” in [Best Practices](../../02_core_concepts/04_best_practices.md)
means retaining one value across related reads, not repeatedly capturing a
possibly advancing connection. Time windows/history are traced separately in
[Database Filters](../../02_core_concepts/02_database_filters.atomic.md).

Named source symbols below are artifact-qualified; numeric locators refer to
unannotated revision `cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`.

## Capture is not synchronization

**Documented:** during transactor unavailability `db` returns the latest locally
available consistent value; zero-argument `sync` supplies a transactor barrier.
This does not promise that uncached lazy reads work without storage.

**Observed:** [peer.clj](../../../1.0.7705/peer/src-clj/datomic/peer.clj)::
`Connection.db` (793) checks release state and dereferences `db_ref` once.
It neither refreshes storage nor sends a transaction. The
[Db record](../../../1.0.7705/peer/src-clj/datomic/db.clj) (4724) retains persistent
index layers, memory log and basis/next/index/view coordinates, with no pointer
back to the connection. `acceptDataCheck` derives a new record and indexes.
Thus adoption changes the connection's next capture, not previously returned
values. Capturing the handle is cheap; traversing its absent index blocks is
still lazy storage work. A held value is not a reclamation pin: see the
[retention boundary](00_storage_services.atomic.md#atomic-note-lifecycle-reclamation-boundary).

**Native ownership:** [peer/live.rs](../../../src/peer/live.rs) owns `Peer`'s
`Core` and the advancing `State`. `db` clones the captured handle under a short
read lock; an update mutex serializes observation, not arbitrary snapshot reads.
[peer/snapshot.rs](../../../src/peer/snapshot.rs) is its read facade.
Shared immutable values, log ranges, durable-view references and advisory hints
belong to `database_value`, not the observer runtime. Pure `DatabaseIdentity`
belongs to `model::identity`.

[application/connection.rs](../../../src/application/connection.rs) composes an
advancing peer with an optional transaction client and, for embedded startup,
an owned `TransactionService` lifetime. This last responsibility is a native
adaptation, not part of the source remote peer Connection. An independent read
connection starts no writer. The typed root API remains the intentional facade;
there is no second read engine or hidden generic lifetime owner.

## Startup, tail acceptance and index adoption

**Observed:** [db_io.clj](../../../1.0.7705/peer/src-clj/datomic/db_io.clj)::
`load-db-from-basis` (143) captures index and log heads separately, constructs the
indexed Db, keeps the greater transaction basis via `most-current-db` (124),
and calls `log/catchup`. Equal bases retain the first argument; this is not a
physical index-revision selector or atomic multi-reference read.
[log.clj](../../../1.0.7705/peer/src-clj/datomic/log.clj)::`catchup` seeks from
`Db.nextT`, avoiding replay of the already captured prefix.

`create-connection-state` (peer:847) loads before creating the notifier, loads
again from that basis before delivery starts, then installs the updater and
notifier. The inferred purpose is bridging startup's load/subscription window;
this bounded trace does not prove every transport schedule. `accept-new-data`
(430) discards notification datoms older than `nextT` and applies the remaining
formed datoms with `acceptDataCheck(false)`. It does not expand transaction
forms, and that `false` alone is not a proof that every gap is impossible.

`notify-index` (550) loads the completed index off the notifier path, then
[adopter.clj](../../../1.0.7705/peer/src-clj/datomic/adopter.clj)::`adopt-index`
(13) replays the memory-log suffix after its index basis and CASes the local Db
pointer. A failed CAS replays only transactions after the candidate basis.
`memlog-txes-since` (db:8535) binary-searches the vector for this suffix;
`trim-log` (4534) copies the retained suffix to avoid holding its discarded
prefix. The cost is replaying missing recent transactions, not materializing
the whole stored database. This local CAS is not a durable publication CAS.
The initial index-basis rejection is not repeated in every retry; concurrent
index-notification scheduling remains outside this proof.

**Native:** `BlockReader::capture_changed` authenticates a new root and log
extension, derives schema/ident/AVET readiness from the missing canonical suffix,
and extends the shared recent tier. A changed physical index or generation uses
a full open of the current format. The observer validates the complete report
batch before publishing its new state. These checks and root descriptors are
native mechanisms, not a literal port of separate Datomic index/log references.

## Observation, reports and target-specific waits

**Observed:** source `notify-data` (peer:633) captures before, adopts the datoms,
then constructs a report only for a pending submitter or installed report queue.
It delivers the submitting future before adding to the single unbounded queue,
then releases transaction-basis waiters. `notify-db` (613) replaces a loaded Db
and releases waiters without inventing reports for the skipped prefix.
`StorageOnlyConnection` (1119) instead owns a fixed Db, no transaction/report
lane, and only satisfies explicit-t sync already covered by that value.
`LocalConnection` is another implementation; remote notification ordering is
not a statement about every packaged Connection implementation.

Source `TWatcherImpl` (333) keeps target-ordered futures. The transaction watcher
uses `basisT`; the index watcher uses `indexBasisT`. Zero-argument `Connection.sync`
sends `:sync` through the updater and `notify-sync` (713) resolves it with the
then-current Db. `sync(t)` registers local demand without sending that barrier.
For `syncSchema/Excise`, if the transaction basis covers `t`, `sync-background-t`
uses `db/ts-needing-index` (3587) to choose only the relevant work through `t`;
it need not wait for every transaction through `t` to be indexed.

**Source uncertainty:** waiter registration captures `db` before its lock and
checks that same lexical value again inside. The lock alone does not establish
lost-wakeup immunity if a notification drains before late registration. No JVM
schedule test was run, so this is an explicit proof gap, not a reproduced bug.

**Native adaptations:** direct typed, nonblocking writer notices are only wakeup
hints; the independent storage observer reconstructs ordered exact reports from
durable receipts, including retained generation handoffs. Dropping a transaction
ticket does not suppress adoption. Atomic's keyed exact retry and receipt-first
unknown-outcome handling are stronger native contracts; the source chapter
explicitly declines to retry arbitrary transactions automatically. See the
[ACID trace](../../04_transactions/05_acid.atomic.md).

Native `Connection::sync()` refreshes the visible durable head without requiring
a live writer; it is not the same barrier as source zero-argument `sync()`.
`Connection::sync_to(t)` waits for local adoption on a condition variable while
the background observer performs I/O. Its predicate is read under the same mutex
used by publication, rather than copying the source watcher's stale-value shape.

**Target-contract repair:** at Stage 4 entry, native `sync_schema(t)` waited for
*all current* `avet_unready` entries, and `sync_index(t)` also required the entire
current AVET work set to be empty. Work after `t` could extend these waits or
cause a timeout despite the requested target being complete. The implemented
`peer/live.rs::avet_ready_through` now inspects existing schema history only for
pending attributes. `avet_transition_after` folds `:db/index` and `:db/unique`
facts transaction-at-a-time to identify effective AVET membership changes.

Native descriptors can reach a basis with partial AVET checkpoints, unlike the
source's completed `indexBasisT`: merely deleting the emptiness check would be
unsound. The helper checks work at the descriptor basis separately from newer
tail enables, so later disable/re-enable cycles cannot hide unfinished old work.
Index sync also checks removals; schema sync concerns additions. Excision's
existing completion test already filters requests through the target and is
unchanged. No new durable tracker or format is introduced. The real-PostgreSQL
[readiness regression](../../../src/peer/live/readiness_tests.rs) uses manual
canonical index steps and zero-duration waits to distinguish old, later, partial,
and removal work, plus uniqueness admission and atomic flag transfer. Uniqueness
over historical data without ready AVET remains rejected; this repair does not
introduce an asynchronous unique-only backfill. The focused actual PostgreSQL
readiness test passed in 2.80 seconds in the main integration run.

## Complete-path cost and evidence boundary

At component entry, `Peer::collect_reports` called `exact_report_from_capture`
per missing transaction. Each receipt reopened both exact endpoints from their
index basis. For N reports after L unindexed transactions, with unchanged physical
index/generation, endpoint replay visits sum to `2*L*N + N*N` transactions,
apart from ordinary sync catch-up. Tree caching does not eliminate this repeated
decode/reindex work. This is a static call-path count, not measured latency/RSS.
The implemented `BlockReader::exact_reports_from_capture` reuses authenticated
reader suffix extension for adjacent exact endpoints, not receipt-provided schema
or the writer's `capture_successor`. It retains publication-log membership,
receipt coordinates/frontiers and exact root/route checks. The single-report
receipt-first path remains unchanged. [The catch-up regression](../../../tests/block_peer.rs)
uses one 80-write fixture, missing-report counts 16/32/64 and already-held prefixes
0/16 with no SSD or tree cache. It measures complete sync/drain/verification/value
disposal separately from seeding, with total driver SQL calls, report-phase calls,
known payload bytes and elapsed time. Its call bounds reject multiplying the
held prefix by every missing report. These are not SQL-statement, network-byte
or allocator measurements.

The actual PostgreSQL integration run passed all six catch-up samples:

| Held prefix | Missing reports | Total driver SQL calls | Report-phase calls | Known payload bytes read | Complete µs |
|---:|---:|---:|---:|---:|---:|
| 0 | 16 | 353 | 266 | 90,245 | 300,022 |
| 0 | 32 | 738 | 571 | 207,752 | 597,407 |
| 16 | 16 | 376 | 289 | 110,366 | 217,409 |
| 16 | 32 | 746 | 579 | 242,519 | 593,748 |
| 0 | 64 | 1,586 | 1,257 | 523,264 | 1,159,930 |
| 16 | 64 | 1,573 | 1,244 | 524,362 | 1,232,269 |

Increasing the held prefix by 16 changed report calls by +23/+8/−13, not by a
replay of that prefix per report. Receipt membership and prefix/root checks
still incur per-report work; this is not a one-read-per-report claim. These
debug samples overlapped other integration/build work, so elapsed values are
observations rather than a controlled speedup or Datomic latency comparison.
The same run passed the existing seven native connection cases, including the
multi-generation excision handoff. The final three-case `block_peer` rerun passed
in 9.61 seconds, including its added physical-index boundary witness: first-after
and second-before keys are logically equal but their exact SnapshotReferences
differ, and every observed endpoint reference matches its writer receipt.

Separately, the public native log cursor initially performed a point lookup per
transaction and did not inherit the shared forward `LogRange` traversal. Its
implemented forward-navigation repair and authenticated SSD payload-cache checks
are recorded in the [Log API trace](../../07_peer_api/02_shared_reference/00_log_api.atomic.md),
with actual public-path measurements rather than inference from the low-level
log benchmark.

Existing actual-PostgreSQL test bodies independently assert:

- [native_connection.rs](../../../tests/native_connection.rs): ordered external,
  unwaited and dropped-ticket reports; saved-value immutability; writer replacement
  with identity rejection; reconnect after killing the actual observer SQL
  session; and lagged reports across two excision generations within grace.
- [block_peer.rs](../../../tests/block_peer.rs): reports match writer before/after
  keys/references, tempids, datoms and hash; repeated sync emits none; index adoption
  preserves held values; exact references reopen; restart reuses authenticated
  SSD objects.

The test run above was performed by the main integration owner, who records
live-environment commands/results.
No query/fulltext algorithm, complete namespace, all failure schedules or deployment equivalence
is claimed. The entire adopter/db_io and Java Connection/Database interfaces were
read; peer/Db coverage is the named capture/time/adoption/notification paths and
their actual callers. Selected Db mechanisms and the Db record normalize equally
across the recovered artifacts; full `db.clj` does not have a whole-file equality
claim. Packaging duplication is not a second logical engine.
