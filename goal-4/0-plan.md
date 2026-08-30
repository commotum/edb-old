# Goal 4 — Finish a Coherent, Study-Quality Datomic Reconstruction

## Objective

Finish the educational recovery and explanation of Datomic Pro 1.0.7277 as a
coherent, runnable PostgreSQL-backed system. The recovered Peer and complete
recovered Transactor must work together across transaction submission, durable
storage, log publication, indexing, transport, acknowledgement, failover, and
normal operation without original implementation fallback. The finished system
must be clear enough to study from source and exercise through meaningful
runtime scenarios.

Goal 4 replaces exhaustive equivalence as the finish line with **strong
coherence**. Completion does not require proving that every dormant branch in
all 117 Peer/Transactor overlaps is universally equivalent. It requires strong
surface and architectural consistency, green critical paths, no known
contradiction on supported paths, focused regressions for confirmed defects,
and an honest account of residual risk and out-of-scope behavior.

Goal 2 and Goal 3 are historical strategy and evidence inputs. This file is the
authoritative strategy and status record for Goal 4.

## Strong-coherence boundary

The normal acceptance boundary is:

1. Candidate ownership and isolation: no original Peer or Transactor
   implementation classes enter a recovered runtime.
2. Expected namespaces and major callable, protocol, class, and subsystem
   surfaces are present and internally consistent.
3. Peer, transport, Transactor, storage, log, index, coordination, and HA
   contracts compose into one intelligible architecture.
4. Critical PostgreSQL-backed success, rejection, recovery, acknowledgement,
   and failover paths pass focused runtime checks.
5. No known unexplained contradiction remains on supported or exercised paths.
6. Confirmed recovery defects are repaired generically and retain focused
   regressions.
7. Dormant, optional, or unsupported behavior is identified as residual risk or
   scope rather than subjected automatically to exhaustive proof.

The 117-row overlap ledger is a diagnostic risk register, not a completion
gate. Investigate a row when evidence is surfaced that could falsify shared
design: a surface or ABI mismatch, an unexplained executable difference on a
relevant path, a differential/runtime failure, a contradictory subsystem
contract, or an explicit user request. Exact-AOT, global graph matching, and
broad compiler-family comparison are escalation tools only; they are never
default prerequisites.

## Material constraints

- Work in `/home/jake/Developer/atomic/datomic-rev` and preserve the recovered
  Peer as the reference boundary.
- Keep `/home/jake/Developer/atomic/goal-1` untouched and uninspected.
- Preserve existing worktree changes unless their ownership and disposition are
  established; do not erase retained recovery work to obtain a clean tree.
- Keep licensed originals isolated as fingerprinted evidence or behavioral
  oracles, never as candidate implementation dependencies.
- Use PostgreSQL as the primary authoritative backend. Optional interfaces and
  alternative stores must not displace the core system objective.
- Keep one outcome-bearing executable or explanatory boundary active at a time.
- Prefer retained evidence and targeted checks. Do not create review-of-review,
  namespace-by-namespace proof campaigns, or comparator projects without a
  concrete falsification trigger.
- Match claims to observation and record genuine uncertainty plainly.

## Execution strategy

Stages 2–4 remain ordered acceptance gates, but they are not a rigid execution
waterfall. Finish the active Stage 2 HA boundary first. After Stage 2 closes,
execute Stages 3 and 4 as evidence-driven subsystem vertical slices: validate
one high-value operational behavior, then immediately record its source map,
contracts, state transitions, and cited runtime evidence before selecting the
next behavior. The teaching narrative must grow with the executable proof,
not wait behind a broad operational campaign.

Stage 5 is a rolling scope ledger throughout that work. Optional facilities
encountered along the way are classified as validated, coherent but
unexercised, partial, or out of scope; classification alone does not create an
implementation obligation. Final release signoff remains last, after the
PostgreSQL core and architectural narrative are coherent.

## Known inherited state

- The complete 247-source Transactor corpus is recovered; 272/272 effective
  namespaces load and all 247 callable/root/class shapes agree under the
  established bounded surface.
- Recovered Peer and Transactor runtimes exclude original implementations and
  cross the primary PostgreSQL write, log, index, restart, transport,
  acknowledgement, and failover paths.
- The in-flight transaction-during-takeover v8 boundary passes: A's pending
  descriptor CAS made the prepared tail authoritative before B claimed and
  caught up that referenced lineage; the original Future reports unavailable,
  the same Peer continues through B, a fresh Peer and SQL root agree, and stale
  A self-fences. This is descriptor-CAS and catchup evidence, not startup
  discovery of an unreferenced immutable row.
- Concurrent accepted submissions during takeover now pass in both relevant
  descriptor-CAS schedules. In v6 stale A published all four logical writes
  before fencing (four adopted, zero resubmitted). In deterministic v7 B first
  claimed and caught up the unchanged baseline while A remained frozen, after
  which the Peer resubmitted exactly the four absent logical writes (zero
  adopted, four resubmitted). Both runs finish at one strict order through
  basis 1009, with unavailable original Futures, exact-once logical effects,
  stale-A fencing, fresh-Peer agreement, verified evidence, and clean shutdown.
- The advisory overlap ledger currently contains 13 fully resolved rows, nine
  bounded-partial rows, and 95 open rows. These counts describe certainty, not
  a known defect count or Goal 4 completion percentage.
- The accepted credential-scoped asymmetric storage-reachability result is now
  retained at `/tmp/datomic-recovered-pair-ha-partition-v6`. Broader packet
  loss/reordering and arbitrary multi-node topologies remain unproved by design.
  The recovered architecture still needs a consolidated source map and teaching
  narrative.
- The credential-scoped asymmetric PostgreSQL partition/heal v4 execution is
  behaviorally green but is **not accepted evidence**. It demonstrated B's
  promotion while A remained live and storage-incapable, healed stale-A CAS
  conflict/self-fencing, and same-/fresh-Peer agreement, but
  `run-status.properties` is failed: its evidence-secret gate found the node
  SQL password text in `config.properties`, the negative-login diagnostic, and
  the PostgreSQL server log. v4 remains an immutable failed diagnostic; only a
  fresh versioned run with generic evidence redaction may close the boundary.

## Stage 1 — Rebaseline around strong coherence

**Status:** Complete at the scaffold boundary

**Outcome:** Goal 4 begins from a concise, trustworthy account of what already
works, what remains outcome-bearing, and what is merely unproven dormant code.

**Focus:** This scaffold banks the trustworthy Goal 3 results, distinguishes
known runtime gaps from unproven dormant behavior, and converts exhaustive
overlap obligations into an explicit risk register without manufacturing
resolution claims. Future repository synchronization is a short execution-loop
check, not a separate review project.

**Completion signal:** Satisfied here: the inherited state, supported core
boundary, falsification triggers, residual risk, and next runtime boundary are
recorded consistently; no global equivalence gate remains an implicit
prerequisite.

## Stage 2 — Finish the authoritative HA spine

**Status:** Complete at the supported credential-scoped HA boundary

**Outcome:** The recovered system preserves one authoritative transaction order
through takeover and adversarial writer-coordination conditions.

**Focus:** Finish the credential-scoped partition/heal gate with secret-free,
self-verifying evidence. After banking it, run at most one bounded deliberate
no-writable-split-brain scenario, and only if it can materially falsify the
supported HA claim. Do not expand this stage into exhaustive partition,
topology, overlap, or scheduler proof. Let observed failures pull only the
implicated source, overlap, or structural question into scope.

**Completion signal:** Accepted work has one monotonic durable order with no
duplicates or losses; client outcomes are consistent with publication;
stale writers cannot remain authoritative; same-Peer and fresh-Peer state agree;
and PostgreSQL, processes, sessions, and ports clean up after every boundary.

**Current evidence:** The claim-sensitive concurrent gate is retained at
`/tmp/datomic-recovered-pair-ha-concurrent-v6` (A wins descriptor publication;
101-entry evidence manifest) and
`/tmp/datomic-recovered-pair-ha-concurrent-v7` (B claims the baseline first;
102-entry evidence manifest). Every manifest entry verifies. The v7 result
requires basis 1001 before recovery, four unavailable Futures, four idempotent
resubmissions, strict commits at 1003/1005/1007/1009, and a fresh-Peer audit at
basis 1009. These runs prove logical-intent accounting and one authoritative
descriptor order for the exercised schedules; they do not claim transparent
survival of original Futures or automatic adoption of unreachable append rows.

The accepted asymmetric PostgreSQL partition/heal gate is retained at
`/tmp/datomic-recovered-pair-ha-partition-v6`. Its 126-entry manifest verifies
in full (manifest-file SHA-256
`4e0a4ca8c8c58863712f8252dd66e320756dd70a02a38288f320ecf924299272`),
and `run-status.properties` is `passed`. A synchronized credential cut took 87
ms: A remained a live, non-stopped JVM with an open transport endpoint but zero
owned SQL sessions; its login failed for the required role-disabled reason;
Peer and B retained SQL reachability; and B advanced coordination revision 6
to 7. After heal, stale A observed exactly one heartbeat CAS conflict and
self-fenced. The continuing Peer and a fresh Peer agreed through B at basis
1066, the sole authoritative log-root row advanced from revision 3 to 5, all
active/Peer/standby role session counts reached zero, and all three service
ports closed. The raw negative-login diagnostic is outside the evidence set;
manifest-eligible text passes generic node-password redaction and a final
literal-secret scan. v4 remains a failed diagnostic and was not modified.

No additional Stage 2 execution is warranted. The retained frozen-active,
in-flight, and concurrent descriptor-CAS schedules already supply bounded
dual-writable/no-writable-split-brain pressure; another credential or scheduler
variant would add breadth rather than materially challenge the supported
claim. Universal packet partitions, arbitrary topology, and exhaustive
scheduler proof remain explicitly `NOT_RUN`.

## Stage 3 — Establish operational coherence

**Status:** Complete at the bounded PostgreSQL operational boundary

**Outcome:** The recovered Peer/Transactor system behaves as one repeatable
operational unit beyond a single transaction demonstration.

**Focus:** Validate configuration/startup/readiness, shutdown/restart/recovery,
monitoring/process events, cache, and bounded maintenance as separate focused
slices. After each slice, perform its Stage 4 explanation before starting the
next. Select facilities by architectural value and observed risk rather than
artifact breadth.

**Completion signal:** The system can be configured, started, observed,
stopped, restarted, and recovered repeatedly without original fallback,
unexplained authoritative-state changes, leaked owned services, or a known
critical lifecycle contradiction.

**Current evidence:** The configuration/startup/readiness slice required no new
service run. Accepted partition v6 supplies current-source normalized SQL
configuration, exact PID/start-time/argv ownership, `System started`, an open
service endpoint, and a successful recovered-Peer seed at basis 1001. The
focused missing-schema run at
`/tmp/datomic-recovered-pair-startup-failure-v1` supplies the failure side: 20
bounded retries retain the PostgreSQL exception, the lifecycle fails exactly
once, the service port never opens, no secondary NPE occurs, exact ownership is
preserved through SIGINT-only cleanup, PostgreSQL remains responsive, and its
69-entry manifest verifies at SHA-256
`40a45828557f88f9677f0926b992d639461c01f8f5a322344190d57fc7ac573b`.
Together they prove that the launcher's `System started` marker is not itself
readiness. The source also acknowledges per-database start after the log claim
but before catchup, so the supported layers are scheduling marker,
coordination-backed owned PID/open port, and finally successful Peer work as
the end-to-end signal. `/tmp/datomic-recovered-pair-live-v3` adds three owned
boots, equal seed/restart snapshots, three bounded SIGINT-only stops, and no
final owned service. Here the runner's `graceful` field means no escalation,
not an ordered drain. Ambient SQL credentials and invalid-property
combinatorics remain coherent but unexercised.

The shutdown/restart/recovery slice also required no new service run. Five
retained manifests verify in full: live-v3 (108 entries), index-v5 (113),
transport-v2 (123), prepublication crash v2 (110), and postpublication crash
v4 (106). Together they prove three bounded SIGINT-only stops, fresh-directory
log replay at 37,372 and 64,032 bytes, explicit persistent-index publication at
`t 1066`, zero-replay fresh-process adoption, both sides of the durable
publication crash boundary, same-Peer reconnect, fresh-Peer equality, zero
owned SQL sessions on the explicitly counted fault paths, closed ports, and
final PostgreSQL shutdown throughout. The runner's
`graceful` field means no TERM/KILL escalation, not an orderly in-JVM drain.
Recovered source does not map normal SIGINT to the Master/Database shutdown
protocols, and PID files remain after the recorded PIDs are absent; same-path
PID reuse, SIGTERM, in-flight drain, and interrupted index construction remain
honest residuals rather than supported guarantees.

The monitoring/process-event slice required no new service run. Source mapping
now separates scheduling markers, observations, authority transitions,
transport publication, database-state transitions, interval measurements, and
critical-failure events. The fully verified index-v5 evidence records callback
initialization, the default `clojure.core/identity` reporter, immediate metric
snapshots, three standby/heartbeat/start/Artemis/catchup chains, positive
catchup plus index adoption, and a zero-replay restart. Partition-v6 adds
60-second interval reports and the exact stale-A conflict, process termination,
final `AlarmHeartbeatFailed`/`Alarm`/`SelfDestruct` metrics, and Artemis-stop
chain while B serves agreeing Peers. This validates local event production,
interval aggregation, default callback invocation, and the observed self-fence
signals. Custom/per-stat callbacks, CloudWatch, ping, and S3 log rotation remain
unexercised. Recovered redaction is targeted rather than universal, so the
partition gate's generic evidence redaction and final literal-secret scan are
part of the supported evidence contract. The separate synchronous
`datomic.process.events` garbage-mark bus has no direct retained delivery
signal. The completed bounded-maintenance slice maps its externally meaningful
completion boundary and confirms that no direct retained effect exists, so the
bus remains coherent but unexercised rather than implicitly pending.

The cache slice also required no new service run. Mutable coordination,
catalog, log-tail, and index-root references bypass every cache and remain
PostgreSQL reads/CAS operations; only immutable UUID-addressed values, derived
query plans, and bounded client-correlation state are cached. Current Peer and
Transactor `datomic.cache` hashes still match the 214-entry, fully verified
cache-overlap v3 input ledger. Its isolated recovered lanes cover constructors,
hit/miss routing, read-ahead, population/clear, same-key in-flight collapse,
failure cleanup/retry, and repair-stack behavior. Partition-v6 supplies the
integrated path: A records 168 object-cache probes/101 hits/29 residents, B
records 41/15/21 while catching up 37,372 bytes, and both still produce one
lineage plus same-/fresh-Peer equality. Index-v5 adds three empty fresh-JVM
caches, positive and zero replay, index adoption, and identical snapshots.
Local object and index-array cache correctness is validated without claiming
capacity/performance equivalence. Query-plan hit rate and the thin Peer admin
clear dispatcher are unisolated. Spy memcached/direct valcache are coherent but
unexercised optional value accelerators; the alternate Folsom client and
universal optional-cache outage behavior remain partial/outside the supported
core.

The bounded-maintenance slice closes without a fresh process run. The already
retained index-v5 gate is the outcome-bearing maintenance proof: recovered
`request-index` returned queue acceptance at requested basis 1066, `sync-index`
observed completion at 1066, PostgreSQL grew from 42 rows/18,993 value bytes to
86/34,597, and a third fresh Transactor loaded `tail-t = index-t = 1066` with
zero replay and the identical Peer snapshot. Its `PASS` status and all 113
manifest entries were reverified; the manifest-file SHA-256 remains
`c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238`.
Source mapping now separates request queue acknowledgement from root-CAS/
Peer-adoption completion, the synchronous Peer-local object-cache release from
durable maintenance, and fire-and-forget `gc-storage` from actual asynchronous
deletion. No cache-clear-only run was added because the dispatcher is thin and
its clear/refetch mechanics are already validated. No garbage run was added
because a material proof requires more than 1,000 exactly accounted retired
IDs plus destructive asynchronous deletion/live-root assertions; a small
zero-count run would not test reclamation. Explicit index maintenance is
validated; cache-release dispatch and durable garbage marking are coherent but
directly unexercised; active-database garbage collection remains partial.

## Stage 4 — Build the architectural reconstruction

**Status:** Complete at the recovered PostgreSQL teaching boundary

**Outcome:** A reader can understand the recovered Datomic system from both its
architecture and its source.

**Focus:** Map Peer, transport, Transactor, transaction pipeline, durable
storage, log, indexes, coordination, HA, and important lifecycle facilities to
their recovered namespaces, classes, and runtime evidence. For each Stage 3
subsystem slice, immediately add its contracts and state transitions. Explain
intentional uncertainty without turning the explanation into an artifact dump.

**Completion signal:** A reader can trace a transaction from API submission
through ordering, durable publication, Peer visibility, index adoption,
restart, and failover using the recovered source and cited runtime evidence.

**Current narrative:**
`transactor/reports/recovered-system-architecture.md` now maps SQL property
normalization/validation, PID ownership, asynchronous lifecycle startup,
coordination-before-serving, transport readiness, per-database log claim, and
fatal startup cleanup to recovered source and retained runs. The HA source map
in `transactor/reports/postgresql-vertical-slice.md` separately traces
coordination CAS, descriptor CAS, Peer reconnect, and stale-process fencing.
The same architecture report now distinguishes bounded operator stops from
critical-failure handlers, maps Master/Artemis and Database shutdown contracts,
traces persistent-index load plus descriptor-referenced log catchup, and binds
normal/crash recovery to five fully verified retained manifests.
Its monitoring section now maps the PID/thread event envelope, timed-event and
counter aggregation, interval reset/snapshot semantics, callback construction,
signal taxonomy, targeted redaction boundary, and conflict-to-final-metrics
self-fence sequence. It explicitly avoids treating `System started`,
`:transactor/start`, standby observation, metrics scheduling, and external
delivery as stronger readiness or authority claims than their producers
support.
The cache section now separates PostgreSQL reference authority from immutable
value acceleration, maps the per-key in-flight/object-cache and index-array
state machines, distinguishes memory index and query plans from result caches,
and binds real hit/miss metrics plus fresh-process reconstruction to retained
evidence. It also explains why optional raw-value caches cannot become HA or
log authority and classifies their unexercised implementations without making
them release obligations.
The maintenance section now distinguishes three completion contracts: local
cache release, queued explicit indexing followed by `sync-index`, and
fire-and-forget garbage collection. It traces index semaphore/log segmentation,
immutable index construction, root CAS, adoption, garbage publication, the
in-memory mark threshold, the separate durable garbage-root tree, strict
cutoff deletion, and asynchronous failure alarms. Retained index-v5 evidence
closes the supported maintenance behavior while the destructive and optional
facilities remain bounded explicitly.
The central teaching spine now starts from PostgreSQL's immutable-value/
revisioned-selector model and follows `transact-async` through Peer correlation
and backpressure, non-durable Artemis admission, the single Transactor
processor's novelty/order decision, speculative local apply, parallel log and
result encoding, immutable tail creation, the authoritative
`pod-log-tail/<db-id>` CAS, post-CAS notification, Peer basis advance,
reconnect/unknown-outcome semantics, persistent indexing, restart catchup, and
both HA descriptor schedules. It explicitly distinguishes transport ACK,
descriptor publication, result delivery, and persistent-index publication.
Ten retained teaching-spine manifests were reverified in full, and the two
acknowledgement crash cuts explain why unavailable means outcome unknown rather
than absent. This satisfies the Stage 4 traceability signal without asserting
automatic resubmission or universal Future survival.

## Stage 5 — Bound the remainder and release the study system

**Status:** Complete at the supported PostgreSQL educational release boundary
(2026-08-29)

**Outcome:** The recovered core has a defensible completion boundary, while
optional systems and residual uncertainty are represented honestly.

**Focus:** Classify important maintenance and optional components—such as
backup/restore, full text, excision, garbage collection, Peer Server/thin
Client, REST, Presto, Console, and alternative stores—as validated, coherent but
unexercised, partially recovered, or out of scope. Execute additional work only
when it materially improves the educational system or resolves a surfaced
contradiction.

**Completion signal:** The PostgreSQL-backed core remains green and teachable;
no known critical contradiction remains; optional and dormant areas have clear
boundaries; the risk register is honest; and the repository contains a
resumable, runnable, source-mapped study system.

**Rolling classifications:** PostgreSQL SQL startup, PID ownership/critical
cleanup, bounded stop/fresh-directory restart, persistent-index publication/
adoption, exercised HA endpoint publication, and local metrics/process-event
wiring are validated. Local object/index-array/query-plan/correlation cache
mechanics and their PostgreSQL-path semantics are also validated; exact
capacity, eviction timing, and performance equivalence are not claimed. In-JVM
orderly drain and PID-file lifecycle are partial.
Ping/S3 log rotation, memcached/valcache, and REST are coherent but unexercised.
The internal `datomic.process.events` garbage-mark bus and durable garbage tree
are coherent but unexercised; their greater-than-1,000 batching threshold and
separate root CAS are now source-mapped. Peer `release-object-cache` dispatch
is coherent but directly unexercised, although the underlying clear/refetch
mechanics are validated. Explicit `request-index` maintenance is validated.
Active-database `gc-storage` is partial because its client call has no
completion future and exact retired-ID deletion/live-root preservation is
`NOT_RUN`. The recovered backup engine has isolated Peer-lane evidence, but
recovered-pair production backup/restore remains partial: all 118 entries of
`/tmp/datomic-stage2-adversarial-final-v2` reverify at manifest SHA-256
`25e5f821a84bbde27cb85a985a2728cffbe73509dfd2f7b70c115ed6ca2cf721`,
but that lane used a test-only `stage2-file:` adapter and an isolated licensed
Transactor fixture, with no original classes on the candidate classpath.
Full-text indexing/search/reclamation and excision are coherent but
unexercised and outside the core release claim. Deleted-database garbage
collection is partial: its catalog marking and batched log/index/ref teardown
are source-coherent, but exact deletion, restored-ID preservation,
interruption/retry, and catalog cleanup are `NOT_RUN`.
External monitoring callbacks/CloudWatch, cloud credential redaction, and
transport TLS are partial; alternate Folsom cache behavior is also partial and
outside the supported core. Their source paths exist, but external delivery,
universal log sanitization, encrypted candidate transport, and universal
optional-cache outage behavior are `NOT_RUN`.
Recovered Peer Server and the recovered `datomic.peer-client` in-process
adapter are coherent but unexercised. The former connects Peer-backed catalog,
Client SPI, authentication, Transit, bounded Nano HTTPS, and health surfaces;
the latter adapts `datomic.client.api` protocols directly to embedded recovered
Peer operations. Neither has retained behavioral evidence. The actual network
thin Client remains in hash-pinned ordinary `client*.jar` dependencies and is
out of the recovered PostgreSQL scope. REST is source-coherent but its optional
startup branch is unexercised. The licensed-original optional Presto and
Console JARs are inventory evidence only and remain out of scope.
DynamoDB/S3 storage, Cassandra, Couchbase, Infinispan, and dev/H2 stores are out
of the PostgreSQL-backed Goal 4 scope. These labels are recorded in the
architecture report and create no automatic implementation obligation.

**Final release signoff:** The supported release is the recovered Peer plus the
complete recovered Transactor over PostgreSQL and plaintext Artemis. It covers
the important configuration/readiness, transaction rejection/order,
publication/acknowledgement, log/index, Peer reconnect and unknown-outcome,
restart/recovery, monitoring, cache, explicit-index maintenance, and
credential-scoped HA paths explained in the architecture report. The ten
teaching-spine manifests reverify in full. Partition-v6 remains the accepted HA
gate with `PASS` summary/run status, 126/126 verified entries, empty forbidden
runtime-hash and literal-secret findings, no original implementation origins,
one authoritative lineage, zero final owned SQL sessions, and stopped
services; v4 remains an immutable failed diagnostic. The current runner,
partition probe, and shared Peer workload hashes still equal the accepted v6
input ledger. The runner help entry point, shell syntax for the runner and
documented build/validation scripts, local links in the three handoff
documents, and whitespace integrity pass. Only this plan and the architectural
teaching report differ in the final working tree, so no ceremony-only process
rerun was warranted. No known retained evidence contradicts the bounded
PostgreSQL claim.

## Goal completion

Goal 4 is complete when the recovered Peer and Transactor form a strongly
coherent, runnable, and explainable PostgreSQL-backed Datomic system across the
important supported paths without original implementation fallback; the
remaining uncertainty is explicitly bounded; and no known evidence contradicts
the recovered architecture. Universal equivalence of all dormant overlap code
is not required.

**Completion status:** Achieved on 2026-08-29 at the supported boundary above.
The architectural reconstruction is the source-mapped handoff, the recovered-
pair runner is the executable handoff, and the rolling ledger is the honest
limit on optional and residual claims.

## Next direct action

No further Goal 4 implementation is required. On the next recovered source,
runner, or probe change, compare its hash with the accepted input ledger and
rerun only the smallest affected focused gate, then update the cited retained
manifest and this plan. Do not reopen an optional facility unless an explicit
need or concrete contradiction brings it into scope.
