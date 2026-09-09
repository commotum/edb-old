# Goal 2 — Native Connections and Ordered Observation

## Objective

Complete Goal 0 Stage 2: applications use independent native peers, optionally
attach to a separately owned single writer, submit declarative transactions,
and observe immutable values and complete ordered reports. Reads survive writer
unavailability and replacement. Preserve successful durable outcomes regardless
of the order in which callers wait for transaction results.

## Constraints and current evidence

- Inherit Goal 0's information model, Rust/PostgreSQL deployment, bounded native
  reads, and proportionate verification. No new child hierarchy or JVM protocol.
- Docs `07_peer_api/00_clojure/00_datomic_api.md` sync/report sections and
  `04_transactions/03_processing_transactions.md` define coordination, complete
  cross-peer reports, opt-in unbounded queue lifetime, and unknown timeouts.
  `1.0.7705/peer/src-clj/datomic/peer.clj` notify-data installs the value before
  delivering the transaction result; notify-index advances physical roots.
- Goal 1 fixed lineage opening, forward boundary routing, and catch-up reports;
  its real PostgreSQL witnesses pass. Retain those repairs and the executable
  `native_workflow` example. Cursor/time support is partly implemented, not new.
- Those initial defects are now substantially repaired: independent read opens,
  non-owning writer attachment/replacement, ordered local commit observation,
  background catch-up and specialized sync exist with live PostgreSQL checks.
  Independent-process submission now uses a versioned same-user Unix socket;
  the broader production/load acceptance remains Goal 0 Stage 6.
- Independent read peers can observe the authoritative PostgreSQL log without
  a writer transport. Select a concrete usable delivery design for independent
  submission too; do not confuse an in-process writer handle with remote access.
- No ordinary whole-database recovery. Expose compatibility/admin entry points
  explicitly; resource limits and unavailable prerequisites must be truthful.

## Stages

### 1. Separate connection lifetime from writer ownership

**Status:** Complete, including independently owned native socket submission.

**Outcome:** Strict native read connections work without starting a service;
writer attachment and embedded convenience have explicit lifetimes and identity.

**Focus:** Connection and peer construction, native root requirements, lifetime
management, and a concrete independent deployment/delivery boundary.

**Completion signal:** Two peer connections open a database without competing
for a writer lease; reads remain usable after writer shutdown; wrong-lineage
attachment fails. The normal path reports zero eager materializations.

### 2. Make observation ordered and independent of result consumption

**Status:** Complete with live ordering, stall, restart, and multi-excision evidence.

**Outcome:** Own and external commits advance connections in order without
waiting for tickets; success remains success and unknown outcomes remain honest.

**Focus:** Background advancement, complete opt-in reports, result ordering,
reconnect, writer replacement, and bounded state when queues are disabled.

**Completion signal:** Concurrent/out-of-order/dropped tickets, external writes,
retries and failover preserve native old values and exactly-once ordered report
observation. Observation failures are surfaced separately from durable success.

### 3. Complete coordination and native traversal

**Status:** Complete again; scaled boundary loading and finite background
scheduling are repaired and verified below. Preserve earlier native coordination.

**Outcome:** db/sync and transaction/index/schema/excision coordination expose
truthful native values; time views and raw traversal retain exact semantics.

**Focus:** Relevant logical/physical frontiers, lazy access, background changes,
T/Tx/instant fixtures, and documented eager diagnostic access.

**Completion signal:** Focused live PostgreSQL witnesses distinguish each sync
frontier and timeout. Cursor/time differentials and measured lazy accesses pass.

### 4. Integrate and return to Goal 0

**Status:** Complete again. Existing independent-process/connection acceptance
and renewed live indexing regressions pass. Return to Goal0 and resume Goal6;
Goals3–5 remain complete.

**Outcome:** A documented application/deployment workflow exercises the completed
connection model, and Goal 0 accurately selects the next unfinished stage.

**Focus:** Independent peers and writer, restart/reconnect, retained snapshots,
workflow updates, and actual executed evidence rather than skipped test counts.

**Completion signal:** Connection acceptance and workflow pass without eager
recovery or writer ownership; parent Stage2 updated, then the first unfinished
parent stage resumed (currently6; do not recreate completed Goals3–5).

## Reopened by scaled integration — 2026-09-09

Goal6 paused while this child owned the repair. The schema25
100k attempt reached acknowledged head982 (98100 imported records), hash
`212ac700ad123000e9cfd50f2a760c0e1de87074be6174c71308600a8de8bdac`, before the
index worker reported `tree/missing-node` and closed admission. Latest index
publication621 coversbasis979. The reported immutable hash
`33afd4eb7f552207520938e197a69fdc0b85e92d0c34310b7646012f6315cded` exists in
PostgreSQL with2160payload bytes: investigate missing in-memory merge inputs,
not assume durable loss or rebuild the whole database. Preserve the failed
fixture `scale-workflow-277036-1788941894497441026` in `atomic_goal6_scale`.

An independent diagnosed scheduler defect kept the publication-maintenance flag
as unconditional demand. Under sustained writes it repeatedly merged new small
tails below the2MiB threshold. The partial repair now retains a finite demanded
basis through multi-batch live-set/AVET completion and preserves newer forced/
schema demand; focused live multi-batch/subthreshold/restart/threshold fixture
passes, with existing regressions in progress. Preserve this work.

The strict old preloader was reproduced failing on a64-datom pure fixture:
splitting a full directory makes an old interior leaf a new routing boundary.
That leaf is neither touched nor an old directory endpoint. Native merge now
loads missing boundary objects on demand through the PostgreSQL node store,
authenticates their hash/type/order/count/routing, and caches each once for the
merge. The pure strict API remains strict; loaded old nodes do not become new
payloads or retirement candidates merely because they were fetched. No whole-
directory/tree preload or weaker authentication was introduced.

Actual retained98100-record repair: old optimized diagnostic fails in730ms with
the same missing node; new debug diagnostic passes that point and reaches the
intended AfterSegments fault. Normal consolidation then succeeds in16.396s,
revision622/basis982, with transaction head/hash unchanged. It reads156nodes /
11571464bytes, reuses4462subtrees and74previously staged nodes, and seals the
4538-node/166386560-byte live index closure. Debug/release timings are not a
speedup comparison; previously staged output explains zero new writes here.

Finite scheduling now keeps the demanded publication basis through bounded
live-set/AVET maintenance instead of treating each publication as unconditional
demand for a newer subthreshold tail. Newer forced/schema demand remains
monotonic. This follows `datomic_pro_docs/06_indexes/02_background_indexing.md`,
capacity-planning threshold/backpressure semantics and recovered
`1.0.7705/transactor/src-clj/datomic/indexer.clj`'s separate recent/indexing usage.

Renewed checks:2pure tests cover72split cases (exactly one authenticated deferred
read each) and48retraction cases (none), exact output/retirement ownership and
missing/corrupt providers; pass0.93s. Current actual PostgreSQL: background9/9,
incremental consolidation3/3, AVET2/2, stored-value2/2, decimal decode1/1 pass
(15 live fixtures and2pure tests). The background suite includes >512-node
multi-batch maintenance, subthreshold writes/restart and threshold resumption.
Focused clippy passes5.53s. Goal6 must still finish a fresh100k deployment and
its portable operations/crash/GC; neither earlier failed run is acceptance.

## Verified changes — 2026-09-08

- `Connection::connect[_configured]` opens a strict independent native reader;
  `attach[_configured]` borrows an existing writer, `attach_writer` replaces it,
  and `start` remains the explicitly embedded convenience. Wrong identity is
  rejected before attachment. Dropping an attached connection does not own the
  writer lease or lifetime. `db()` remains local immutable capture.
- Native commit callbacks now enqueue an Arc report into a bounded one-slot
  hint channel without peer locks or SQL on the serialized writer. Peer opening
  occurs outside the service's observer registry lock; durable catch-up covers
  registration/hint gaps. Tickets wait for local observation only within their
  remaining deadline, never turning a known commit into a rejection.
  `Connection::observation_error()` exposes observation failures separately.
- Connections poll the authoritative log and physical publications every 100ms.
  Their worker has explicit shutdown and no connection-owning reference cycle.
  Dropping a reader requests stop without joining a stalled read; the bounded
  worker/pins remain alive until that in-flight read returns, then release.
  Queue retention remains opt-in. Catch-up reconstructs original tempid names
  from generation receipts; ATLC payloads intentionally do not contain them.
- Ordinary peer opening and post-excision root adoption reject missing native
  roots instead of reconstructing an eager database. Explicit
  `Peer::connect_compatibility[_configured]` retains the admin/legacy adapter.
  `Peer::db/sync/sync_to` now default to native values too. Eager access is named
  `db_compatibility`, `try_db_compatibility`, `sync_compatibility`, and
  `sync_to_compatibility`; older eager differential fixtures use those names.
- A lagging peer without a report queue adopts a newer authenticated checkpoint
  and only its tail. It does not replay all indexed history. Writer admission
  limits are not retroactive validity rules for already committed facts.
- `sync_index` checks authenticated `index_basis_t`, including projection work;
  `sync_schema` checks adopted schema and positive AVET readiness (it can also
  wait for newer concurrently adopted schema changes); `sync_excise` scans
  native AEVT request history and checks the active generation's root-last
  completion and exact request identities, without eager log recovery. During
  activation-before-root-publication, specialized waits retain the old value
  and wait/timeout rather than bypassing the missing native root.
- `LocalTransactionServer` and `Connection::transact_socket` support independently
  running writer/peer processes. Full TxForm/schema/map/program grammar uses a
  versioned, bounded native frame, not persisted request or Datomic wire bytes.
  Compound framing supports the maximum legal scalar without changing existing
  accepted encodings or hashes. Native errors retain categories/details/anomaly;
  original remote code is in `details["remote_code"]`.
- Socket admission/workers and frame I/O deadlines are bounded; private 0700
  directory / 0600 socket define the same-host, same-OS-user trust boundary.
  Adapter restart creates a fresh endpoint; replacement is explicit. PostgreSQL
  uses its independent existing transport/role policy. This is not a TCP or
  cross-host writer protocol claim.
- Lost responses after delivery are UnknownOutcome; same-key retry returns the
  durable receipt. Confirmed success returns `CommittedTransaction`, whose
  separately fallible native report opening uses the read peer's permissions
  without acquiring a writer head-row lock. The writer retains source pins
  until acknowledgement/deadline; local values then own their pins.
- Excision refresh originally skipped unseen transaction reports. It now walks
  immutable activation ancestry, reconstructs original reports in retained
  generations, and stages all reports before publishing the new state/queue.
  Multiple rewrites plus the newest ordinary tail preserve order/tempid names.
  Missing retained ancestry fails closed, never silently skipping reports;
  this does not invent indefinite retention through operator GC or server loss.
- Low-level basis waits now retry root-pin transport recovery across restart.
  Physical frontier waits retry transport interruptions as well as root-last
  publication gaps. `Connection::sync_to` waits solely on background observation
  and respects its local deadline even when storage/update is stalled.
  Low-level synchronous PostgreSQL calls and socket receipt opening still use
  the configured storage I/O policy; their polling/delivery deadlines do not
  cancel an arbitrary already-blocked SQL call. Operational network policy and
  failure-envelope measurement remain required by parent Stages 5–6.
- `native_workflow` covers the in-process attachment, while `process_workflow`
  launches a writer and two submitting peers as separate processes, plus an
  observing parent. Both verify native old values and reopen after writer stop.

## Executed evidence and limits

Disposable PostgreSQL 15.11, main socket port 55432 and separate restart-socket
port 55433 under `/tmp/atomic-goal-pg.MWicgR`, user jake/database postgres:

- `native_connection`: four tests passed together, then the fifth real SQL
  session-termination/reconnect witness passed separately. Coverage includes
  reverse ticket wait order, dropped tickets, own/external complete reports,
  replay without duplicate reports, writer replacement, immutable old values,
  read reopening, index/excision completion and interrupted activation.
- Lagging-peer witness: eight transactions beyond its captured basis, five-datom
  configured recent setting; after consolidation/catch-up, durable base equals
  head, recent datoms = 0, compatibility materializations = 0, old value intact.
  This is a bounded-state regression, not a realistic scale benchmark.
- `service_startup_index_requirement`: 2/2, including the added strict peer
  rejection and explicitly eager compatibility-open witness.
- `postgres_peer`: 19/19 after connection changes; after the newer-checkpoint
  fast path, all ten matching native-path tests passed again. The enhanced
  AVET readiness fixture then passed separately using `sync_schema` itself.
- `native_time_points` 5/5 and `streaming_time_windows` 3/3 are pure temporal
  evidence, not PostgreSQL tests despite sharing the configured command.
- Library 236 passed / 1 ignored after connection/sync changes; PostgreSQL
  conditional skips in that run are not live evidence. Build, formatting,
  clippy with warnings denied, and whitespace checks pass. The later checkpoint
  path has separate live tests as above; do not imply a final all-target live run.
- Updated `cargo run --offline --example native_workflow` passed on real
  PostgreSQL after separating the writer and adding the second peer.

Additional executed evidence (same disposable PostgreSQL 15.11 fixtures):

- Native connection suite: 7/7, 101.83s after the multi-generation repair.
  Added T/Tx/duplicate-instant native views and two excision rewrites followed
  by a new-generation transaction, preserving all five original reports.
  Final local-only `Connection::sync_to` rerun: 7/7, 123.35s.
- Actual dedicated server restart: 1/1, final extended run 3.13s, preserving native old values,
  recovering root pins, standby takeover, complete reports, zero eager loads.
  An initial fixture run restarted at the wrong socket; only the corrected
  explicit-options run counts. That run exposed the root-pin retry bug, now fixed.
- Writer-stall regression failed before bounded notices (second authoritative
  transaction timed out), then passed after repair; extended to verify dropping
  a connection and the local synchronization deadline while observation is held.
- Native socket tests: 3/3 on PostgreSQL, including actual commit/response loss
  and same-key replay, a confirmed commit with unavailable exact native root,
  and oversized/slow frame rejection. Max-scalar and full grammar codec tests
  pass. Runtime-role test permits socket submission/native receipt reads while
  peer-role SQL UPDATE is denied (42501), compatibility loads zero.
- Final independent-process workflow passed with database
  `process-workflow-79850-1788930549341561928`, peer PIDs 81193/81194.
  Final transport tests 3/3, 49.58s; stalled-observer/drop/local deadline test
  1/1, 1.02s. No scale claim.
- Service notifications now follow authoritative response delivery; the focused
  own/external/unwaited/replay fixture passed again, 1/1, 1.52s. Background log
  polling and report consumption remain asynchronous; do not promise scheduling
  order of arbitrary user threads.
- Library: 243 passed / 1 ignored, 32.46s; conditional PostgreSQL skips are not
  live evidence. Socket framing requires local socket permissions; a prior
  sandbox EPERM run was not a passing suite. Clippy/all-target checks pass.

## Continuation

**Child complete again; parent continues with Goal6.** The later scaled
indexing failures are repaired with direct and live regression evidence above.
The earlier broader `postgres_peer` rerun passed19/19,435.98s. Reopen this child
for an owning later failure, preserving all repairs and completed Goals3–5.
No overall production/scale acceptance is claimed. PostgreSQL restart checks use
the exact isolated startup options documented in README. Long SQL/network
failure-envelope and load measurements remain explicit parent work, not an
unbounded-wait guarantee or production acceptance inferred from local tests.
