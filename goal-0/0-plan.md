# Goal 0 — Deliver the Native Datomic Product

## Objective and authority

Complete the existing Rust/PostgreSQL Datomic-inspired database as a usable
application and operator product, including every capability assigned below.
Preserve the working engine and all accepted upgrades from Rich Hickey's
Deconstructing the Database, Writing Datomic in Clojure and The Functional
Database. This is a dependency-aware resequencing, not a reduced objective.

Use /home/jake/Developer/atomic/datomic_pro_docs as semantic authority and
/home/jake/Developer/atomic/1.0.7705 as architectural/algorithmic evidence.
Native Rust and PostgreSQL are the implementation; JVM/Clojure execution,
Datomic wire/storage compatibility, alternate stores and exhaustive decompilation
equivalence are not objectives.

## Current state and execution ownership

**Status:** In progress. Goals1–5 are complete; Goal6 is the sole active child.
[Goal1](../goal-1/0-plan.md) records the runnable baseline; Goal2 records storage.
G1 (original Goals0–8), G2 (Goals9–17) and G3 (the completed native-engine pass)
under goal-archive are historical evidence, not active instructions.

The library already provides a fenced transactor, same-host submission, native
peers, immutable values/history, pure with, query/Pull/entity navigation,
in-memory fixtures, authenticated logs, indexing, backup/restore and GC.
Extend those APIs rather than replacing them. Missing capabilities are assigned
below; source observations are not fresh benchmark results.

G3 recorded real PostgreSQL recovery/retry/GC acceptance and a 100,000-record
import at 129.213 records/s. Restore took 3367.648s and inspection 2595.920s on
the declared host. These are retained baselines, not SLAs or new verification.
The data exceeded configured caches, not physical RAM; two submitting peers
and local db() capture did not establish read scaling or offline native queries.

Targeted starting points, not an audit gate: native service/local_transport/
connection for the runnable path; peer/tree_store/persistent_tree for storage;
database_value/recent_btset/query/program for functional APIs; identity/
tiered_assessor for partitions; backup/operations for administration. Recovered
datomic db.clj, query.clj/datalog.clj, cache.clj/kv_cache.clj and transactor
index.clj/update.clj provide the corresponding algorithmic leads; fulltext.clj,
fulltext_index.clj/lucene.clj cover search. Reuse tests/transactor_differential.rs,
tests/native_speculation.rs and the existing process/native workflow examples.

Stages1–7 map to goal-1 through goal-7. Keep one child active. Reconcile existing
children; use $scaffold-goal for a missing child's three scaffold files.
No recursive goals, corrective parent or pre-scaffolding the entire roadmap.
Return to the parent after each child and continue until integrated acceptance.

The order reduces rework; it is not a claim that every predecessor is a hard
dependency. Settle small endpoint, logical-identity and format contracts when
first needed, without implementing unrelated later stages. Keep one evolving
application and reusable fixtures. Full operator CLI delivery moves to Stage6,
but storage changes must pass backup/restore/GC checks in their owning stage.

## Invariants and evidence rules

- Preserve immutable facts/values, declarative serialized transactions, strong
  identity, schema as data, temporal/history views, peer-local reads, durable
  acknowledgement, exact retry and fenced authority. Preserve existing data,
  repairs, applied migration checksums and durable request/program meaning;
  extend formats explicitly and retain old-format compatibility.
- Every assigned capability remains required. Runtime opt-in caches/hints still
  require implementation and measurements. Only deeper transaction pipelining
  is a measurement-gated architectural decision. Report a material scope change
  to the user; do not silently relabel gaps optional or commercial polish.
- PostgreSQL is the sole durable authority. SSD cache data is disposable.
  No mandatory external cache, search service, broker, central SQL query
  evaluator, web console, marketplace release or new replication manager.
- Use restricted runtime roles, explicit PostgreSQL TLS/I/O policy, separate
  administrative authority, redacted diagnostics and explicit destructive
  targets/retention. Respect existing fixtures; crash/reclamation checks need
  isolated disposable targets. Do not hide provisioning or full recovery in
  ordinary startup/read paths.
- Keep bounded ordinary state, cancellation and configurable resource policy.
  Broad scans/admin work may scale with data; do not invent constant-work
  guarantees or arbitrary semantic limits to make tests pass.
- Establish measurements before claiming improvements: total foreground and
  background SQL, including pin/health/metadata work, not only node counters.
  Cache-resident native reads must avoid foreground storage health calls while
  preserving retention/GC safety; this does not promise uncached offline reads.
- Preserve logical snapshot identity across physical indexing changes. Exact
  references require lineage, generation, commit identity, authority and retained
  data. as_of on a newer basis is not exact older reopening or a historical fork.
  Document speculative/opaque-filter key limits; do not pretend closures have
  portable equality or that a database key alone identifies query results.
- Preserve pure with, independent retained branches, exact stored-value identity,
  schema/history/program ownership and safe iterator/drop depth. Preview does
  not reserve a commit. Keep replayable intent/logical references; do not blindly
  submit speculative IDs/datoms or equate sequential previews with one atomic
  transaction. Protect relevant assumptions using basis checks, CAS or invariants.
- Hints are bounded advisory acceleration, not facts, authority or request
  identity. Fulltext candidates may lag and are not a fixed database basis;
  returned facts must match the supplied view. Do not require basis-stable
  ranking, exact Lucene scores or undocumented synchronous freshness/excision.
- Notices are wakeups; authenticated logs repair gaps. Preserve lossless live
  reports while adding a separate bounded restartable consumer. State retention,
  unavailable-history and pre-excision access policy explicitly; no indefinite
  snapshot retention or exactly-once external-side-effect promise.
- Reuse existing pure/native oracles and fault hooks. Start selectable seeds,
  saved replay traces and reusable workloads in Stage1; grow generation and
  automatic failure reduction with the risky changes, not only at the finish.
  A controlled failing fixture may prove the reducer. No replacement testing
  platform, alternate store or exhaustive schedule enumeration.
- Match claims to observed results. Missing-config/self-skipped tests do not
  prove PostgreSQL behavior. Use focused regressions per change and broaden for
  actual risks; preserve relevant G3 evidence without repeating every campaign.
  Record concise decisions/results in plans, not duplicated feature lists in
  every loop/prompt.

## Stages

### 1. Ship a runnable application and reusable baseline

**Status:** Complete (2026-09-09); evidence and commands in Goal1. Real PostgreSQL
15.11 separate-process acceptance used restricted roles and verified restart,
exact retry, retained values, explicit index recovery and seed/replay. The small
application baseline was 828ms first run /728ms replay; it is not a scale claim.

**Outcome:** A supported local transactor and separate Rust application run on
actual PostgreSQL, with a reusable verification path for subsequent changes.

**Focus:** Configured executable lifecycle, logical database versus endpoint
resolution, permissions and restart/exact-outcome behavior. Expose minimal
explicit migration/create/status and diagnosed index-recovery entry points,
not the full administrative suite. Reuse service and process-workflow APIs.
Demonstrate one value-taking calculation over a captured native snapshot and a
complete fabricated in-memory Database. Establish a small repeatable workload,
existing counter limitations and initial seed/replay support in existing tests.

**Completion signal:** Separate processes transact/retry/query/navigate, retain
old/history values, stop/restart and reopen using public APIs without ordinary
eager compatibility recovery. The same calculation works on the in-memory
fixture and reproduces its captured native result after later commits.
Configuration/authority errors are clear; actual PostgreSQL commands, baseline
measurements and a replayable seeded check are recorded. Full telemetry, new
engine features and the complete operator CLI are not this child's finish gate.

### 2. Establish efficient immutable storage and reads

**Status:** Complete in Goal2. Zero-SQL warm reads, observed SQL/transaction
phases, verified batching, versioned compressed projections, opt-in authenticated
SSD reuse, snapshot statistics and finite index requests are implemented and
tested. Bounded codec/upload overlap and generated storage replay/reduction pass;
measured decision retains the ordered transaction path. Final cache-concurrency,
corrupt-header fallback and failure-signature repairs pass. The final actual
PostgreSQL application rerun passed2/2(5.59s). An opt-in protocol relay measured
396 request/ready cycles for66 per-node uploads,6 batched,9 with compression;
these are protocol cycles, not measured TCP RTTs. Driver counts were132/2/3.

Compression preserves authoritative canonical rows and all earlier checksums;
optional projections reduce suitable transfer/cache bytes but add PostgreSQL
storage/CPU. Real SSD native check:500573 canonical/7820 compressed bytes;
reopen3SQL retention checks versus cold6SQL. Supported app process restart reused
18 blocks and warmed calculations made zeroSQL. Serial isolated backup suite
10/10 passes; initial parallel global-maintenance contention is retained in
Goal2, not counted as a passing run. Goal2 owns detailed measurements and limits.

**Outcome:** Measurable, safely cached native reads and efficient immutable
block/index I/O form a stable foundation for later features.

**Focus:** Add operation contexts, nested/concurrent I/O attribution,
transaction-phase timing and metric callbacks before changing costs. Separate
cache access from storage/pin maintenance; coalesce misses with bounded storage
concurrency. Settle canonical hashes versus compressed physical bytes before
finalizing the opt-in bounded SSD cache and upload representation. Implement
versioned compression, verified batched node uploads and useful bounded overlap;
retain lazy finite-target indexing and durable-blocks-before-root publication.
SSD reads require authentication, format/access separation, safe fallback,
restart reuse and explicit retention/excision policy.

Add snapshot db_stats (history-datom and useful per-attribute counts via indexes,
not deep inspection) and asynchronous finite-target request_index composing
with sync_index; report scan costs for time/filtered views. Extend existing
fault tests with generated schedules, replay and automatic reduction as these
storage boundaries change. Use phase measurements to decide deeper transactor
pipelining: implement justified overlap or retain the current path with evidence.
Any changed pipeline preserves one ordered authority and failure/ack/retry meaning.

**Completion signal:** A fully warmed native query makes no foreground SQL and
survives temporary storage loss within supported retention; unavailable data
fails safely. Slow cold misses do not block unrelated cache hits. Pin loss,
GC/reacquisition and restart preserve exact old values. SSD disablement,
eviction, corruption and restart reuse are exercised.
Old/new compressed blocks retain content hashes and survive backup/restore;
malformed/oversized decompression fails safely. Conflicting/interrupted uploads
cannot publish incomplete roots. Measure round trips, throughput, peak memory,
codec CPU and transferred/cache/stored bytes on compressible and poorly
compressible data, separating native payload savings from PostgreSQL disk use.
Statistics/index-request semantics and costs are verified on real PostgreSQL.
A seeded fault trace replays and can be reduced; the pipeline decision has phase
evidence and, if changed, dependent-failure/unknown-outcome/ordering checks.

### 3. Complete functional values and useful query/programming

**Status:** Complete in Goal3. Shared indexed branches, exact supported references,
V2 native programs, tuple/log sources, prepared reuse/grouped/hash joins, bounded
advisory hints and executable safe planning pass real PostgreSQL acceptance.
Required-manifest retention is distinct from root-independent keys(noHistory).
Depth4096 has height14/selective24nodes;16k-row numeric join109ms versus125s
reference (one fixture). Warm grouped probes0SQL. Blocked hints do not delay ack;
cold/warm hints added16SQL and showed no benefit in the small sample. Goal3 owns
measurements, limits and failure evidence. Stage6 still owns remote transport.

**Outcome:** Applications efficiently compose queries and speculative values,
identify/reopen exact supported snapshots, and plan safe changes using native APIs.

**Focus:** Replace accumulated speculative-vector copying/scanning with shared
ownership and appropriate current/history/removal indexes. Expose inexpensive
logical committed snapshot/view keys and versioned exact references/reopening
using existing coordinates; include temporal/history modifiers, not physical
roots or cache allocations. Local APIs enforce existing authorization/retention;
cross-host handoff is exercised in Stage6.

Expand persisted QueryTemplate support for predicates, rules, negation,
historical sources and dynamic attributes. Reuse native controls, versioned
durable forms and existing recursive-rule machinery. Provide convenient native
authoring without requiring a new language/JIT; add raw-datom/tuple database-pattern
sources and tx_ids/tx_data-style log integration. Preserve existing multiple
databases and relation inputs. Implement prepared-query reuse with bounded
structural caching, appropriate bounded hash joins and grouped database probes,
retaining selective seeks and source/schema-dependent validation on every run.

Once read/overlay interfaces settle, add speculative read tracing, bounded hint
generation, authenticated cache admission, overlapping prefetch and cancellation.
Stage6 transports hints without changing durable request identity.
Extend the common application to branch/compare/discard/select/revalidate intent
and commit, resolving logical IDs safely; a small helper is sufficient if needed.

**Completion signal:** Measure branch depth/width, cumulative construction and
selective-read work, retained/discarded memory and stack/drop safety; small
extensions share prior data rather than copy its accumulated prefix. Native,
pure and generated checks preserve all branch semantics and absence of writes.
Selective speculative reads use indexes rather than scan unrelated accumulated
novelty; retain cheap value cloning as well as branch sharing.
Supported snapshot keys compare consistently across reopening/indexing without
DB scans; different logical views do not collide, unsupported keys are explicit,
and unavailable references fail rather than opening a different value.

Representative persisted rules cover all five expanded capabilities; speculation,
commit, prior program versions, retry and recovery agree. The same query runs on
native and tuple fixtures; log ranges join with provenance facts. Prepared
executions reuse structure without stale bindings. Growing-input/output join
tests preserve equality, duplicates, aggregation and cancellation while measuring
work/memory/I/O. Selective probes retain their advantage.
Valid/stale/altered/absent hints preserve results; cold/warm tests report queue/
service latency, I/O, peer overhead, cache effects and benefit or lack thereof.
Planning proves discarded alternatives leave live
state unchanged, selected intent resolves IDs correctly, protected stale
assumptions reject/replan, and reports expose the exact committed value.

### 4. Complete partition identity and locality

**Status:** Complete in Goal4 (2026-09-09). Named/implicit allocation, force/match,
component affinity and versioned program/forms are implemented. Old-binary data
passed exact explicit upgrade without changing genesis; fresh-EID/reference and
delayed-install regressions pass. Restricted-role application/retry/restart now
extends throughbasis9 with stored UUID helpers. Final real PostgreSQL partition
suite4/4 passed; grouped tenant sample16→3leaf reads and32→6SQL, warm0SQLboth.
One global frontier/native user default and signed UUID index order are retained;
Goal4/documents explain limits. This is measured locality, not universal speed.

**Outcome:** Applications control new-entity placement using native partition
semantics and have documented time-ordered UUID helpers.

**Focus:** Custom/named and implicit partitions, force/match assignment, schema,
allocation/tempid/upsert interactions and native forms/durable validation.
Remove the three-partition implementation whitelist without changing existing
IDs or receipts. Add UUID utilities with explicit layout/clock/order semantics.
This is a smaller independent delivery, not a prerequisite of fulltext.

**Completion signal:** Related-entity and tenant/customer workflows exercise
both partition forms and assignments. Invalid/conflicting inputs reject;
identity, speculation, indexes, history, recovery and retry agree. Measure
locality without a universal speedup or treating partitions as access control.
UUID checks do not claim global transaction ordering. Repeat remote coverage
when Stage6 delivers networking.

### 5. Add integrated peer-local fulltext

**Status:** Complete in Goal5. Compatible fulltext metadata/ABI9, native
analysis/ranking, Datalog/program integration, exact-view filtering and the
separate-process app throughbasis11 pass real PostgreSQL checks. Storage
publication interruption/repair/restore/GC/excision, restricted roles and
observable background failure/idle retry pass. Native402-document selective
fixture reads10167of211095searchbytes/14SQL; warm0SQL, cache15321bytes. Full
source-specific rebuild/spill cost and eventual coverage are explicit; no
incremental-search or universal scale claim. Goal5 owns detailed evidence.

**Outcome:** Analyzed, relevance-ranked string search composes with Datalog and
respects the queried database view without another service.

**Focus:** Versioned schema/search metadata, native analysis/query syntax,
background indexing, immutable search ownership and current/history filtering.
Build on stable storage/cache/publication boundaries and implement
rebuild/recovery/backup/GC integration now, not as deferred operational polish.

**Completion signal:** Search joins structured facts and survives updates,
retractions, reopen, backup/restore and reclamation. Document/test analyzer
semantics, lag, ranking and supported excision behavior. Queries execute at
peers over PostgreSQL-backed search data; substring matching is not fulltext.

### 6. Complete distributed applications and operator delivery

**Status:** Active in Goal6.

**Outcome:** Secure multi-host applications and operators use the completed
capabilities through supported deployment and administrative interfaces.

**Focus:** Authenticated/encrypted submission, stable discovery/reconnection,
lease/lineage binding, admission/deadline policy, exact receipts and replacement.
Build on Stage1's endpoint contract and existing versioned submission codec.
Implement bounded cross-process commit/index wakeups with durable-log repair,
bounded replay/live consumers and durable checkpoints bound to lineage,
generation and last processed transaction. Carry Stage3 references and advisory
hints through authenticated/versioned transport and finish app configuration.

Complete the coherent administrative CLI: provision/create, backup/verification,
separate-target restore, inspection/status and bounded GC with explicit
targets, authority, retention, preview/destructive controls, progress and errors.
Reuse existing operators and interruption/retry behavior; report healthy pending
maintenance separately from corruption. These CLI commands need not make broad
operations constant-time.

**Completion signal:** Actual network boundaries (separate hosts or isolated
network environments, not Unix sockets alone) exercise remote submit, takeover,
rediscovery, unknown outcomes and identical-key retry. Invalid credentials,
wrong lineage and stale endpoints fail safely. Dropped/coalesced notices,
disconnect/restart and consumer checkpoints recover without unbounded buffering;
measure idle SQL and observation latency.
Another process reopens an authorized retained snapshot after newer commits and
schema changes with identical supported identity/value. Invalid, unavailable
and pre-excision references obey explicit policy. Transported hints preserve
identity/results under alteration, staleness and absence. Remote partition,
program, planning and search workflows agree with local behavior.
Real admin commands exercise backup/verify/restore/inspect/authorized GC and safe
failure/retry without source edits or acceptance-harness setup.

### 7. Establish integrated operating acceptance

**Status:** Pending.

**Outcome:** Every required capability works together, with reproducible
instructions and a measured, honest operating envelope.

**Focus:** Run the evolving executable/SDK application through integrated
security, failure, lifecycle and upgrade scenarios. Expand the existing seeded
campaigns and reduced regressions across transactions, retries, reconnect/reopen,
indexing and relevant faults. Measure increasing reader counts, warm selective
queries, cold-start bursts and scan-heavy analytics alongside ordinary traffic.
Profile/improve material operating bottlenecks, including G3 restore/inspection,
without weakening integrity. Revisit the pipeline decision only if new phase
evidence warrants it; do not first introduce major test infrastructure here.
Goal2 also identified commitment-tree point-load I/O inside transaction encoding:
four256-operation commits used1108–1109SQL calls in that phase. Profile this
alongside restore/inspection; it is not evidence for adding more transaction
pipeline stages before understanding the work.

**Completion signal:** Real PostgreSQL acceptance covers every stage's features
and relevant interrupted/retry/upgrade boundaries. Publish throughput, latency
tails, memory/cache and total foreground/background PostgreSQL load, separating
peer query CPU scaling from shared-storage limits. Include branch, query/join,
hint, compression and index-I/O measurements; use more than one reader count,
not local basis access or eager reads as substitutes. No linear-scaling or
arbitrary-offline promise. Seeded schedules replay; failing traces reduce
(a controlled fixture is sufficient), and regressions are retained.
Distinguish new results from G3 baselines and tested from untested conditions.
An unresolved feature or integrated gap reopens its owner; only the complete
product and integrated acceptance close Goal0.

## Continuation

Active: Goal6, on 2026-09-09. Goals1–5 passed their real PostgreSQL/application,
semantic/failure and measured-cost signals; see their evidence records. Goal2
retains ordered transaction processing and records commitment-tree I/O for Stage7.
Next: implement authenticated remote delivery, bounded durable consumers and
administrative commands. Disposable user/network namespaces are available for
real isolated-network acceptance (`unshare --user --map-root-user --net true`
passed); PostgreSQL stays in the isolated fixture, reachable by private socket.
No later stage or integrated completion is claimed. Keep this plan authoritative;
loops/prompts are continuation guides.
