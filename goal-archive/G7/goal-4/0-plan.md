# Goal 4 — Incremental indexes and live applications

## Objective

Execute Stage 4 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: background
indexing and application readers use the Rust-owned immutable block engine while
transactions continue. Preserve the current query/fulltext/time/program/EDN and
local/remote/async workflows; no legacy SQL publication or hidden fallback.

Local `datomic_pro_docs/06_indexes/{01_index_model,02_background_indexing}.md`
govern index membership, amortized maintenance and peer-local reads. Recovered
`1.0.7705` index/peer code is algorithmic evidence, not a byte/runtime contract.
Reuse the proven Rust tree/cache/recent/fulltext/query algorithms. Fresh formats
are allowed; preserve current correctness and exact retries, not old layouts.

## Internal stages

### 1. Resident advancement and protected incremental indexing

Status: Complete.

Outcome: Small writes advance persistent recent state without reopening their
whole tail; background jobs merge affected paths and safely publish indexes.

Focus: Extract existing loader-driven merge policy, consistent cross-index
noHistory handling, resumable AVET projection/readiness and fulltext attachment.
Use a protected frozen source and an independent bounded I/O worker. The serialized
writer adopts only on its latest root, preserving newer log/receipts/metadata.
Fresh-work backpressure follows receipt lookup and precedes callbacks; account
transaction/datom/byte limits, not only one convenient threshold. Index failures
must not silently turn into successful no-ops or disable a feature.

Completion: Real service writes continue during preparation, old values/retries
remain valid, stale/cancelled candidates cannot publish, and work/memory/bytes
demonstrate affected-path updates with actual automatic consolidation.

### 2. Shared live readers and application paths

Status: Complete.

Outcome: Peers, fulltext, synchronization, async, remote applications and durable
change consumers read the same immutable roots and recent log, without relational
catalog or publication dependencies.

Focus: Reuse existing public interfaces and application support; captured values
remain pinned/stable while live connections advance. Preserve selective fulltext,
temporal/program reads, snapshot references, independent prefetch/cache lanes,
backpressure/cancellation, notifications with durable catch-up and endpoint/failover
behavior. No eager full-database materialization to shortcut the port.

Completion: Separate actual applications query/navigate/read fulltext and receive
changes through writes, index adoption and writer replacement. Warm resident
queries avoid storage reads; interrupted consumption catches up from durable log.

### 3. Integrated costs and removal of displaced paths

Status: Complete.

Outcome: One tested live indexing/read architecture remains for this stage.

Focus: Port useful regression/failure cases; remove superseded SQL workers,
publication adapters and unsupported native-reader branches as consumers transfer.
Measure complete write/index/read/sync paths on data larger than caches, including
root capture and cleanup. Reconcile lifecycle/backup hooks with owning later stages.

Completion: Current PostgreSQL/application checks pass, costs and remaining limits
are recorded here and in Goal 0, and the parent proceeds to Stage 5. This child is
not a substitute for retention, backup/restore or full product acceptance.

## Original Stage 4 closeout

The following records the original stage acceptance. Subsequent integration
repairs and their verified results are recorded separately below.

Resident writer advancement path-copies only new recent datoms. Protected indexing
uses the existing tree algorithms, one independent I/O worker, bounded parallel
CPU preparation, resumable AVET work and fulltext attachments. Adoption validates
the frozen prefix against the latest publication and preserves newer transactions
and receipts. Receipt lookup precedes fresh-work admission and callbacks.

Actual PostgreSQL checks passed: three indexing tests (5.99 s) covering resumable
AVET/toggles, cancellation/competing preparation and concurrent-tail/noHistory
adoption; two deterministic service tests (1.55 s) covering concurrent writes and
receipt-first backpressure/callback-once behavior. Subsequent public-peer,
cache and reference checks are recorded below.

The completed work connects stock Peer/Connection, exact observer reports, SSD/RAM caches,
independent bounded hints, fulltext and generic notifications. Exact outcomes also
have a basis-addressed entry in the same immutable receipt trie, so observers retain
real tempid maps without a relational report table. Serialized snapshot references
must prove both canonical log/metadata and published covering-index provenance;
a Rust-owned retained index registry is adopted in the same root CAS. Stage 5
subsequently added bounded retention-aware pruning, not a chain of old publications.
Fulltext preparation failures fail the candidate instead of publishing an index
with silently missing search data. Tree and fulltext RAM caches have separate
bounded budgets; report their combined footprint, not one cache as the total.

The peer/fulltext/compression/reference checks, durable consumers and remote
routing port passed the stage runs below. Pin lifecycle/GC and administration/
backup were subsequently handled in their owning stages. Final whole-product
acceptance remains Goal 7's responsibility; no databases were reset for this port.

Stage closeout run: 42 selected unit tests passed with PostgreSQL configured
(28.91 s), including compression/aggregate admission, publication provenance,
resident advancement and the zero-datom-cap regression. Both fulltext tests passed
(3.12 s), both stock service tests passed (1.85 s), and all seven transaction tests
passed (19.38 s). Stock Peer/Connection/SSD test passed (1.41 s) after fixing a
real indexed-base prefix comparison bug and the test cache's private permissions.
The TLS remote test passed (3.02 s): certificate/name/token rejection, exact hinted
retry, lost response, replacement discovery and stale-instance rejection.

Debug complete-path sample: 2,048 entities / 1,572,864 string bytes with 1 MiB
writer and peer node caches. Startup/capture 116 ms; populate commits 1.79 s,
or 7.71 s through automatic index drain. Twenty-four small writes took 1.97 s,
or 4.60 s through final index drain; three automatic jobs. Complete startup-through-shutdown 12.99 s,
6,872 SQL calls, 19,094,947 known read-payload bytes and 8,209,052 write-payload
bytes. Thirty-two reused immutable reads took 587 us and zero SQL/payload bytes.
Accounted peer cache peak 715,532 B; writer current 567,671 B, not process RSS.
This is one debug workload, not a throughput/scaling SLA; deferred driver teardown
and fixture deletion are outside that timer. Fulltext selective query loaded six
SQL results / 29,453 bytes from a 250,545-byte search corpus; incremental edit
tokenized 74 bytes for two additions/one removal, not the entire corpus.

Further actual PostgreSQL checks passed: async/hints 2 (1.61 s), log 5
(13.81 s, including a 64 MiB transaction), live peer 2 (1.35 s), and consumers 9
(1.87 s). These cover cancellation/abandoned waiter exact retry, independent hint
I/O without SSD acquisition, whole-prefix forgery rejection, preflight admission
before chunk download, principal-scoped checkpoints and cross-process notifications.
The stalled local observer deadline regression also passed (0.66 s).

Default readers now accept valid committed tails without guessed local limits;
explicit reader limits remain enforced. Writer restart under smaller fresh-work
limits can still return an existing exact receipt. The source no longer contains
the legacy live-peer coordinator/eager adapter or obsolete idle fulltext worker.
Actual unified index/search job observations replace the idle worker's counters.
The final 19 service regressions passed (1.46 s), including an actual failed GC
guard adoption followed by a successful coherent search retry. The ported exact
allocation checkpoint test passed (0.86 s), and bounded Pull over 512 superseded
facts passed (1.11 s). The original Stage 4 child closed before Stage 5 began.
The remaining exact-reader/admin/backup adapters and SQL fixtures were transferred
to their owning stages for replacement/removal, not retained as a second engine.

## Stage 7 integration repairs — complete

Current execution: Complete. The final exact-report/reference observation race
is repaired through shared `pin_current_reference` bounded capture retries.
Exact reports, serialized snapshot references, inspection, backup capture and
read-only receipt resolution use this helper without repeating transaction
execution or weakening generation, identity or protection checks. The focused
regressions and affected remote workflow passed; this integration reopen is closed.

Integration found real gaps in decoded-program reuse, log cache/provider
accounting, batched node uploads, preserved fulltext controls and report continuity
across excision. These are now implemented and covered by the actual configured
PostgreSQL runs below. All timings are unoptimized local samples, not throughput
or scaling promises. `all1` and `focus3` also contained other failures; only their
specifically passing targets are credited here.

- `all1`: decoded-program cache 1/1 (1.08 s), log cache 2/2 (11.92 s), batched
  uploads 2/2 (0.38 s), fulltext 2/2 (3.00 s), and fulltext controls 3/3 (4.33 s).
  Eight cached program invocations took 1.566 ms with one decode/validation and
  seven hits, retaining 1,147 accounted bytes. The 66-node duplicate-upload probe
  used 3 driver calls versus 132 individual calls (73.296 ms batch path), while
  stale guards/corrupt reuse/interruption still failed atomically. With RAM node
  caching disabled, the 12-transaction log probe took 178.646 ms cold and 77.838 ms
  warm; the warm scan performed zero SQL. Cache contents never supply membership,
  generation, program-deployment or receipt authority. Fulltext limits now govern
  background jobs and operator consolidation/rebuild/excision/current-index repair.
- `focus3`: native connection 7/7 (28.29 s), SSD cache 1/1 (11.44 s), and snapshot
  references 3/3 (4.11 s). Connected observers retain separate generation-interest
  tokens; excision atomically publishes reclaimable original-report handoffs.
  Neither ordinary captured values nor current roots retain a prior-head chain.
  Two skipped generations yielded all five exact original reports; after the last
  lagging observer dropped, handoffs were released over two GC cycles (5.911 s)
  while held report values remained readable. New serialized old-generation opens
  remain forbidden. A restore without original handoffs fails queue catchup with
  `peer/report-history-unavailable`, without silently advancing past missing reports.
- `publication`: both deterministic index-publication tests passed (2.62 s).
  Operator staging tolerates renewal by the same writer; explicit writer-owned
  lease guards remain exact. A concurrent commit fences a staged old root, and
  recapture/adoption preserves the newer log, receipts and metadata. Capture/CAS
  retries remain bounded and retain source/epoch/GC guards.
- `focus4`: standby 5/5 (3.78 s), worker 9/9 (17.59 s), and transaction hints 3/3
  (3.33 s; one manual cost benchmark intentionally ignored). The configured
  takeover/native commit/index/retry/shutdown path took 592.999 ms; 10,000
  status/try-active polls took 3.279 ms with zero driver calls. These passes close
  the earlier consolidation/capture and test-coordination failures while retaining
  queue continuity and the advisory-only hint contract.
- The separate immediate-crash WAL run passed 1/1 (32.81 s): PostgreSQL restart
  took 518 ms, acknowledged receipt retry and held value/log reads survived, and
  an uncommitted reference update rolled back. It ran separately from other PG
  fixtures rather than disrupting the combined test run.

Evidence logs: `/tmp/atomic-goal7-all1.log`, `/tmp/atomic-goal7-focus3.log`,
`/tmp/atomic-goal7-publication.log`, `/tmp/atomic-goal7-focus4.log`, and
`/tmp/atomic-goal7-wal.log`. Permanent regressions live in the corresponding
`tests/` targets and `src/storage/index_publication_tests.rs`.

Final evidence: the full library run passed 438 tests in 324.76 s. Two later
observation regressions were then added and verified in a five-test protection
selection (4.45 s, `/tmp/atomic-goal7-observers3.log`); this is not a claim of a
440-test full-library run. The affected integration selection exited successfully:
nine targets / 20 tests (`/tmp/atomic-goal7-observers-integration.log`), including
remote product 2/2. Isolated TLS took 17.852 s; stock automatic failover measured
62 ms cancellation, 4,452 ms takeover and 6,159 ms complete workflow.

Goal 4 and its integration repairs are complete. Return to the completed Goal 0/7
integrated acceptance record; no child work remains pending here.
