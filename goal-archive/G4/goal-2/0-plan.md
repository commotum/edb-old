# Goal 2 — Efficient Immutable Storage and Native Reads

## Objective and constraints

Deliver Stage2 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: measurable,
independently cached native reads, efficient authenticated immutable block/index
I/O, snapshot statistics and finite asynchronous index requests. The parent owns
all capabilities, invariants and integrated acceptance. Use local Datomic docs as
semantic authority and 1.0.7705 as architectural
evidence. Status: complete on 2026-09-09; Goal3 is now the sole active child.
Preserve the engine, formats/migration checksums, exact retries,
restricted roles, retained values, authenticated root-last publication and all
Goal1 repairs. PostgreSQL remains authoritative; SSD data is disposable.

Reuse the supported CLI/application, existing pure/native oracles and fault hooks.
No alternative store, replacement test framework, recursive children or hidden
scope exclusions. Account for metadata/pin/health/background SQL, not just node
reads. Scans may scale with data; measurements, not arbitrary timing gates,
determine cost claims. Deeper transaction pipelining is the only conditional
implementation: decide it from measured phases.

## Stages

### 1. Observe operations and decouple cached reads

**Status:** Implemented and verified. Native cache/pin separation, coalesced
misses, total observed SQL and transaction-phase propagation pass actual
PostgreSQL checks; final integration remains part of Stage4.

**Outcome:** Total SQL and transaction-phase costs are attributable; immutable
cache hits do not wait for storage health or unrelated cold misses.

**Focus:** Nested/concurrent operation contexts, metric callbacks, foreground/
background attribution, cache/pin separation, bounded coalesced storage misses.
Retained-value authentication and GC safety remain explicit when sessions fail.

**Completion signal:** Warm native queries issue no foreground SQL and work
during temporary storage loss within supported retention; unavailable cold data
fails safely. Slow misses do not block unrelated hits. Pin loss/reacquisition,
GC and restart preserve exact old values. Repeat the application with declared
total-I/O/phase measurements before claiming improvements.

### 2. Version physical blocks and add bounded SSD reuse

**Status:** Implemented and verified on real PostgreSQL and private filesystem
fixtures. Versioned compression, SSD runtime configuration and application reuse
are integrated. Final measurement/regression closure remains in Stage4.

**Outcome:** Compression reduces suitable physical payloads while preserving
canonical content identity; optional bounded SSD caching safely reuses blocks.

**Focus:** Old/new format decoding, authenticated canonical hashes, bounded
decompression, access/format separation and cache corruption/eviction/fallback.
Explicit restart, retention and excision policy; no new durable authority.

**Completion signal:** Old and compressed blocks survive reopen/backup/restore;
malformed or oversized payloads fail safely. SSD disabled/enabled, corruption,
eviction and restart reuse pass. Measure CPU, memory and transferred/cache/stored
bytes on compressible and poorly compressible inputs; distinguish native payload
size from PostgreSQL disk allocation.

### 3. Batch indexing and complete maintenance APIs

**Status:** Implemented and verified. Statistics, finite/coalesced requests,
bounded verified uploads and scoped codec/upload overlap pass live checks and
have comparative cost evidence below.

**Outcome:** Verified bounded uploads and useful overlap reduce index I/O; users
obtain snapshot statistics and request finite asynchronous indexing.

**Focus:** Lazy finite-target indexing, durable blocks before published roots,
bounded concurrency, db_stats using indexes and request_index/sync_index semantics.
Retain cancellation and report time/filtered-view scan costs.

**Completion signal:** Interrupted/conflicting uploads cannot publish missing
blocks; backup/restore/GC remain correct. Measure round trips, throughput and
peak memory. Real PostgreSQL checks establish history/per-attribute statistics
and finite index targets while newer transactions arrive.

### 4. Verify fault schedules and settle the pipeline decision

**Status:** Complete. Generated/replayed storage faults and controlled reduction,
stable failure signatures, concurrent cache initialization and malformed-header
fallback pass. Final product CLI/application2/2 passes(5.59s). Measured decision:
retain the ordered transaction path.

**Outcome:** Storage changes have generated/replayable/reducible failure evidence
and a measured ordered-transactor pipeline decision.

**Focus:** Grow existing trace and fault support alongside risky changes. Add
automatic reduction; a controlled failing fixture may prove reduction. Use phase
measurements to implement justified pipeline overlap or retain the current path.

**Completion signal:** Seeded storage failures replay and reduce, relevant
regressions and evolving application pass on actual PostgreSQL. If pipeline
changes, dependent failure, ordering, acknowledgement and unknown-outcome checks
pass; otherwise retain the measured rationale. Fold results into Goal0 and
continue its first unfinished child, not another parent or audit phase.

## Continuation

Active on 2026-09-09 after Goal1 acceptance. Four live cache tests (3.70s) prove
zero observed driver calls for warm Query/range/cache stats with terminated data
and pin sessions and held I/O locks; eight identical concurrent misses share one
fetch/Arc; lazy cursor SQL stays with its creation context; pin reacquisition
restores fences and collector-claimed cold roots fail before node reads.
These are bounded retention witnesses, not indefinite offline guarantees.

Statistics/index-request tests passed3/3 on PostgreSQL (2.64s): captured target3
remains finite while commits reach7, history count150, zero eager materialization.
The deterministic coalescing/interleaving unit test also passed. db_stats streams
the view's history AEVT, with explicit scan/cancellation costs. request_index
currently uses an attached writer, reusing its worker; no new local/network
control transport is claimed.

Observed SQL counts started/completed driver API calls, including connect/control,
pin/health/metadata, lazy streams and background work; not network round trips.
Nested/concurrent contexts, callback and failure attribution pass 11 focused
checks, including four PostgreSQL witnesses. Submitted work/reconciliation retain
the caller context. Background observation, indexing and writer maintenance have
independent contexts. Callbacks publish explicitly, outside worker locks.
First small transaction phase witness: 44 SQL calls, 38.40ms transaction wall,
0.034ms expansion, 1.95ms assessment, 11.51ms encoding, 19.80ms publication/commit
across two disjoint spans, 0.013ms report; replay runs only its actual phases.
These are debug-host samples, not throughput or a universal pipeline conclusion.

Migration26 adds optional compressed node projections; migrations1–25 and raw
canonical rows/hashes/SQL checks are unchanged. Compressed hits authenticate raw
server hash/length and bounded decoded canonical content; corrupt projections
fall back, corrupt authority fails. This duplicates PostgreSQL storage and adds
encoding/server hashing; it does not claim disk savings. Canonical backup/restore
remains compatible and recreates projections. A 57,517-byte leaf used 1,800 extra
projection bytes; separate-catalog restore regenerated14 projections (5.27s test).

Codec tests3/3 and SSD filesystem tests6/6 pass. Debug 256KiB codec samples:
repeated input1260 physical bytes, encode4.38ms/decode10.84ms thread CPU; noisy
input retained262144 raw bytes, encode43.72ms/decode8.06ms. Native SSD test passed
(7.93s): 500573 canonical bytes /7820 compressed payload bytes; cold6SQL versus
reopened3SQL (retention health remains), three SSD hits/8087 cache bytes. Damaged
owned cache files refetch safely; disable/purge and bounded accounting pass.
Access/trust+lineage+generation+format namespacing is not authorization. New peers
still authorize through PostgreSQL. Cache purge is best effort, not secure erase
or automatic excision of all copies. Runtime env/API and policy are documented.

Canonical batch comparison, same local debug run, 66nodes/12383bytes:
per-node132 driver calls/70.18ms fresh; max128 batch2calls/5.29ms;
max16 batch10calls/9.67ms. Max1 was slower89.53ms; no universal improvement claim.
Eight live upload checks cover verified reuse/conflicts, interrupted atomic
INSERT/retry, streaming bounds and best-effort projection failure. A compressible
18-node batch added one projection INSERT: 77039 canonical +4778 extra projection
bytes, three total calls. Separate measured overlap/peak-RSS evidence remains due.

Post-compression four cache isolation tests pass again(10.69s). Root-retirement/
pin GC passed in its own disposable database(2.50s). Parallel backup suite first
hit publication deadlock/global semantic-GC busy; serial rerun in a separate empty
database passed all10(77.60s). Do not run global zero-age GC against concurrent
shared fixtures or conceal the initial contention. Restricted v26 ACL check passes.
Supported CLI/application with SSD passed2/2(5.53s), restricted roles and recovery:
second process18 SSD hits/zero payload fetches, warm20 calculations zeroSQL;
864/740ms application wall and16776/16404KiB RSS. Costs are small local fixtures,
not scale evidence. A caught legacy cursor-byte misattribution of SSD decoding
has been corrected and gets an explicit regression assertion.

Final upload suite9/9 passes (28.62s), plus production localized successor /
interrupted-upload/retry witness(10.98s) and earlier persistent/incremental tree
suites5/5. Encoding uses one scoped worker at >=64KiB by default; it joins even
when canonical upload fails. Threshold samples22KiB avoided a worker,74KiB used
one. Public TreeStore/Indexer controls select bounds, compression and overlap.
Release34-node/~1.119MB samples, canonical/serial/overlap wall milliseconds:
repeat31.16/44.94/33.78; random44.43/60.21/31.48. Measured overlap5.57/14.16ms.
Per-upload user+system CPU ms repeat8.13/11.08/11.06, random7.83/18.82/20.01;
process peak RSS KiB repeat13412/13372/13264, random13368/13200/14152.
Random skipped33/34 incompressible projections. Calls2 canonical/3 with projection.
Separate fresh processes and actual getrusage/proc metrics; setup/PG CPU excluded
from upload CPU, process HWM includes setup. Shared-host single samples are noisy;
no claim overlap outperforms canonical-only or achieves general scaling.

Release transaction campaign passed24 measured commits(3.10s test), four requests
per mode at1/64/256 data operations plus dependent CAS and basis checks. Sequential/
queued wall ms:29.24/46.79,103.66/92.51,195.53/216.51. Rust CPU ms11.06/11.23,
50.26/48.60,126.58/138.21; SQL calls194/194,445/443,1220/1221. Expansion0.026–
0.547ms/four requests and report<0.058ms provide little demonstrated overlap.
At256 operations encoding/commitment took145.42/141.97ms, including1108/1109SQL
calls (77.77/70.87ms). This is substantial commitment-tree I/O, not pure encoding.
Retain one ordered authority; extra cross-transaction pipeline complexity is not
justified by these samples. Stage7 must profile this identified point-load cost
alongside its existing operating bottlenecks. PostgreSQL CPU remains unmeasured.

Generated default storage trace passed5.23s; saved replay5.42s and seed0xbeef/18
actions also pass. Controlled reducer9→3 actions in9 candidate replays(32.29s);
reduced trace passes production invariants and only fails the labeled fixture.
Artifacts0600: `/tmp/atomic-storage-fault-25dVby.trace` and
`/tmp/atomic-storage-fault-reduced-G3gznL.trace`. Fault means existing AfterSegments
injection, not process kill. Source trace/replay/reducer and cost commands are
documented in docs/application.md. Stable failure signatures are being tightened
to prevent reduction to an unrelated error.

Post-integration native SSD eviction/byte regression passed7.94s; stats3/3,
migration boundary2/2, SQL attribution11/11 pass. Actual v25 canonical nodes
upgraded without reencoding or changes to migrations1–25 checksums(0.89s).
Statement/lock timeout and known/unknown retry checks3/3 pass(11.19s); separate
TLS fixture was excluded, not counted. Concurrent first cache open uncovered
a real initialization-temp/inventory race; its repair is under focused test.

Final closure: SSD9/9 passes, including concurrent first initialization. A
recognized owned malformed header no longer blocks native reopen: inventory
continues checking all bounded paths, counts corrupt physical occupancy, exposes
incomplete accounting and bypasses writes/purge without touching damaged files.
Unknown files/unsafe permissions/symlinks still reject. Actual native regression
passed9.60s with framing preserved and exact PostgreSQL fallback. Reducer4/4
passed30.46s, matching semantic category/code or PG context/SQLSTATE without
sensitive messages; different failures cannot satisfy reduction.

Literal protocol observation passed3.38s:66 nodes, per-node132 driver calls /
396 Sync+Ready cycles; batched2calls/6cycles; compressed3calls/9cycles. Explicit
SELECT1 fences exclude setup/fence traffic. The bounded test-only Unix relay
counts framing, never logs SQL/parameters/authentication, and is opt-in via
ATOMIC_POSTGRES_PROTOCOL_WITNESS=1 with the fixture URL. Two framing tests pass.
Counts are protocol request/completion cycles, not packets, TCP RTT timings or
an assumption that every cycle waits unpipelined.

Final common application passed2/2 again5.59s after cache repairs. This closes
Goal2; return to Goal0 and execute Goal3. Reopen Goal2 if integration reveals a gap.
Use the isolated test cluster only for failures; preserve existing retained data.
