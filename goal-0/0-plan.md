# Goal 0 — Complete the Native Datomic Product and Missing Capabilities

## Objective

Turn the existing working Rust/PostgreSQL Datomic-inspired database into a
straightforward application and operator product, and implement the useful
capabilities identified in the follow-up review. Preserve the verified engine;
do not restart its implementation or treat its previous acceptance as proof that
the deployment interface and feature surface are finished.

Deliver a usable transactor executable, administrative commands, secure
cross-host submission/reconnection and change observation, shareable exact
snapshot references, practical application APIs, operational instrumentation and
transaction hints, storage-independent cached reads, an opt-in SSD cache,
richer persisted-program queries and plain-data/log query sources, integrated
fulltext, and partition locality controls. Finish with one deployed application
exercising these together and measured operating costs and read scaling.

Use `/home/jake/Developer/atomic/datomic_pro_docs` as semantic authority and
`/home/jake/Developer/atomic/1.0.7705` as architectural and algorithmic evidence.
Translate useful capabilities and designs into native Rust and PostgreSQL;
JVM classes, Clojure execution, Datomic wire/storage compatibility, other stores,
and exhaustive decompilation equivalence are not objectives.

## Constraints

- Preserve immutable facts and database values, declarative serialized
  transactions, strong identity, schema as data, history/time views, peer-local
  query/navigation, acknowledged commits, exact retry and fenced writer authority.
- Preserve existing code, data, durable formats, applied migration checksums,
  restricted roles and completed repairs. Extend durable meaning through explicit
  versions/migrations; do not silently reinterpret old requests or program code.
- Every capability named in this plan has an owning stage and remains required.
  Lack of parity requirements permits native implementations, not silently
  deleting features. Bring a material scope change to the user with concrete
  evidence; do not hide it behind “optional,” “commercial polish,” or completion
  language. Tactics and native API shapes remain implementation decisions.
- Keep PostgreSQL as the sole durable authority and execute application queries
  at peers. A disposable SSD cache is reconstructible acceleration, not another
  authoritative store. Its implementation is required; enabling it is optional.
  No mandatory external cache, broker or search service, or central SQL query
  evaluator as a shortcut around integrated peer-local fulltext. A shared cache
  or replica-read router is not required by this phase.
- Preserve bounded ordinary state and configurable resource policy. Background
  work must be finite/resumable. Broad scans and administrative operations may
  cost proportionally to data; report their costs instead of demanding constant
  work or imposing undocumented semantic limits.
- Fulltext candidate availability is eventually consistent, not part of an
  exact database basis. Validate returned facts against the supplied database
  view. Do not invent synchronous freshness, basis-stable ranking, exact Lucene
  scores, or unconditional fulltext excision as prerequisites absent from the
  semantic contract. Document supported analysis/query syntax and lifecycle.
- Hints are bounded, optional acceleration—not transaction data, facts, trust
  evidence, or authority. Changed/stale/missing hints must preserve request
  identity and results. Implement and measure them; a neutral benchmark may
  justify keeping them opt-in, not claiming an unmeasured improvement.
- A native controlled program runtime is appropriate; a permanently conjunctive
  query subset is not required by determinism. Expand expressiveness while
  preserving deterministic persisted execution and shared resource controls.
- Cache-resident native reads must not depend on a foreground storage health
  round trip. Preserve safe root/generation ownership and GC behavior when
  changing pin maintenance; simply deleting health checks is not a solution.
  This does not promise offline access to uncached data or expired snapshots.
  Account for health, pin, metadata and background I/O as well as node reads.
- Notifications are wakeups, not durable transaction authority. Recover gaps
  from the authenticated log; periodic recovery/failure checks remain allowed.
  Preserve existing lossless live report queues. A separate bounded restartable
  consumer must specify checkpoint/replay and excision behavior, not imply
  exactly-once external side effects or require a consumer-group platform.
- Snapshot references name exact committed values with database lineage and
  generation, not just a wall-clock time or a mutable endpoint. Reopening
  requires authorization and retained data; unavailable/reclaimed references
  fail explicitly. Do not promise indefinite retention, serialize arbitrary
  speculative values, or freeze fulltext freshness/ranking through a reference.
- Use actual PostgreSQL and relevant failure/security witnesses. Tests that
  return early for missing configuration are not integration evidence. Respect
  existing fixtures; crash/reclamation tests require isolated disposable targets.
- Keep one child active and a flat parent/child structure. No corrective parent,
  recursive goals, blanket reimplementation, or repeated exhaustive audits.
  Basic executable/configuration/reconnection usability is part of the product;
  a mandatory web health endpoint, console, marketplace release or new PostgreSQL
  replication manager is not. Existing storage HA remains deployment policy.

## Starting context — 2026-09-09

**Status:** Planned; Goal1 is scaffolded and is the first child to execute.
This request creates the next phase, not a claim that its features are implemented.

- `goal-archive/G1/` preserves original Goals0–8 (formerly A1);
  `goal-archive/G2/` preserves corrective Goals9–17 (formerly A2);
  `goal-archive/G3/` preserves the just-completed Goals0–6. All are evidence,
  not active instructions. The current `goal-0/` owns this phase.
- G3 established a native engine, independent peer processes, application
  semantics, recovery and operations on real PostgreSQL. The 100,000-record
  import, writer replacement, portable restore/deep inspection, actual server
  crash, exact retry and full snapshot/history checks through GC passed.
  Preserve the fixtures and distinguish the recorded failed attempts from passes.
- The current package is one unpublished `atomic-core` library plus example
  executables. Submission is in-process or same-host/same-user Unix sockets;
  adapter restart currently creates a new endpoint. PostgreSQL TLS already exists.
- Native `with`, query/Pull, temporal views, log, `entid_at`, entity navigation,
  synchronous consolidation, `sync_index`, and service/cache/query counters exist.
  Missing small surfaces include snapshot `db_stats`, asynchronous
  `request_index`, and time-ordered UUID helpers. Do not recreate working APIs.
- Persisted `QueryTemplate` permits conjunctions of fixed-attribute patterns,
  unlike the richer peer query language. Fulltext and transaction hints are
  absent. Entity validation permits only the three built-in partitions.
- G3 measured import at129.213 records/s on its declared host; deep restore
  took3367.648s and inspection2595.920s. These are baselines, not universal SLAs
  or acceptable-by-definition performance. Data exceeded configured caches,
  not physical RAM. Use the retained evidence and profile actual bottlenecks.

The follow-up review of Rich Hickey's *Deconstructing the Database* identified
these additional gaps by source inspection, not new test runs:

- Native cursor opening calls `root_pins.ensure()` / `SELECT 1`; node-read
  counters omit that traffic. Cache hits share a lock with slow PostgreSQL
  reads. An in-memory `db()` capture is not proof of storage-independent queries.
- Independent connection observers poll storage at roughly 100ms; immediate
  commit hints are process-local. Log replay and blocking live reports exist,
  but supported persistent consumer checkpoints/resume do not.
- Exact snapshot opening exists internally, not as a public export/reopen API.
  `as_of` on a newly opened value preserves that value's schema and basis; it
  is not a substitute for reopening an exact older value.
- Multiple database sources and relation inputs exist, but plain datom tuples
  cannot replace a database-pattern source unchanged. Public log traversal and
  `tx_ids`/`tx_data` exist without corresponding query integration.
- Native node caching is in-process only. G3's representative warm query still
  made the same four node reads as cold; its two submitting peers did not prove
  increasing-reader throughput or isolation from a scan-heavy analytics peer.
  Transaction provenance, standby takeover and background indexing already
  exist and need integration, not replacement subsystems.

Targeted source leads, not an exhaustive reading gate: recovered
`datomic/launcher.clj` and `coordination.clj`/`reconnector2.clj` for deployment;
`stats.clj` and `common.clj` for small APIs; `db.clj`'s `with-tx+opts` and
transactor `update.clj` for advisory prefetch; `fulltext.clj`,
`fulltext_index.clj` and `lucene.clj` for search; and `db.clj`'s partition
allocation/assignment paths. The local transaction-hints, partitions, fulltext
schema/query and release-notice docs establish the corresponding contracts.
For the talk-derived additions, consult `datomic/connector.clj` / `peer.clj`
for notifications and catch-up, `cache.clj` / `kv_cache.clj` for concurrent
misses and cache tiers, and the local synchronization, log, caching and Valcache
docs. Native starting points include `connection.rs`, `peer.rs`'s exact open,
root pins and node loader, `native_log.rs`, and `query.rs`'s sources/functions.

## Execution ownership

Stages1–7 map to repository `goal-1/` through `goal-7/`. Goal0 owns the entire
outcome, not an additional implementation layer. Reconcile an existing child;
use `$scaffold-goal` to create a missing child's `0-plan.md`, `0-loop.md` and
`0-prompt.md`. Only Goal1 is created now. Do not pre-create speculative hierarchies.

Execute the first unfinished child through its completion signal, update this
plan with material results, then return to `0-loop.md` and execute the next.
Scaffolding or finishing one child does not finish the parent. Fix discovered
prerequisites in their owning code without erasing prior work or opening several
competing active children. Reopen an owning child for an integrated failure.

## Stages

### 1. Make the native database usable without example harnesses

**Status:** Pending; [Goal1](../goal-1/0-plan.md) is scaffolded.

**Outcome:** Developers and operators can run the transactor and routine
administration through supported executables and use the missing small APIs.

**Focus:** Configured local transactor lifecycle, safe administrative commands,
normal application setup, restricted-role guidance, snapshot statistics,
asynchronous finite-target indexing requests, and time-ordered UUID utilities.
Reuse existing services, operators, indexes and typed APIs.
Document current read/storage and report-queue limits accurately; later stages
own the talk-derived runtime changes, not a new prerequisite gate for Goal1.

**Completion signal:** A real executable-backed application provisions an
explicit fixture, submits/queries, retains old values, stops/restarts and reopens
durably. Administrative commands preserve privilege and destructive-operation
boundaries. The three small APIs have direct semantic/cost checks. This is local
product acceptance; Stage2 still owns secure multi-host deployment.

### 2. Support distributed applications, reaction and snapshot handoff

**Status:** Pending.

**Outcome:** Applications connect to a stable logical database and submit to the
current transactor across hosts, react to changes across restart, and share
references to exact committed database values.

**Focus:** Native authenticated/encrypted submission, endpoint discovery,
reconnection, lease/lineage binding, admission/deadline policy and exact receipt
handoff. Add bounded cross-process commit/index wakeups with log gap repair;
support a bounded replay/live consumption API and example with durable
checkpoint/resume semantics bound to database lineage, generation and the last
processed transaction. Expose versioned exact snapshot references and
authorized reopening using existing native coordinates, including lineage,
generation and exact commit identity. Reuse native codecs and authority; no
Datomic broker/wire emulation, mandatory HTTP interface or new message broker.

**Completion signal:** Independent networked application/transactor processes
exercise remote submission, replacement and rediscovery with exact old values,
acknowledged/unknown outcomes and identical-key retry. Invalid credentials,
wrong lineage and stale endpoints fail safely. Verify a genuine network boundary
with separate hosts or isolated network environments, not only Unix sockets.
Measure idle storage traffic and commit-observation latency. Dropped/coalesced
notices, disconnects and consumer restarts recover correctly from checkpoints
without unbounded buffering; replay and unavailable-history behavior are explicit.
Another process reopens a retained snapshot reference after newer commits and
schema changes and obtains the same exact value. Invalid/wrong-lineage or
unavailable references fail explicitly. Retained pre-excision references follow
the documented access/retention policy rather than silently opening a new value.

### 3. Deliver independent cached reads, cost visibility and hints

**Status:** Pending.

**Outcome:** Applications can attribute I/O and transaction costs to their own
operations, perform cache-resident queries without foreground storage calls,
reuse an optional SSD cache, and supply useful bounded advisory prefetch hints.

**Focus:** Operation contexts, nested/concurrent attribution, transaction-phase
timing and metric callbacks covering all storage work. Separate cache access
from bounded storage work, coalesce same-hash misses, and move pin maintenance
off routine warm reads while preserving reclamation safety. Implement a bounded,
opt-in local SSD cache of authenticated immutable bytes with safe fallback,
access/format separation, restart reuse and documented excision/retention policy.
Build speculative read tracing, versioned hint transport, authenticated cache
admission, overlapping bounded prefetch and cancellation on this read path.

**Completion signal:** Concurrent labelled operations have correctly attributed
measurements, distinguishing foreground work and background maintenance. A
fully warmed native query performs no foreground SQL and works through a
temporary storage outage within the supported snapshot-retention contract;
uncached/unavailable data fails safely. A deliberately slow cold miss does not
hold unrelated cache hits behind its storage operation. GC, pin loss/reacquisition
and restart checks preserve exact old values. SSD cache disablement, eviction,
corruption and reuse across process restart have direct checks and measured
costs/benefits; neither cache availability nor cache bytes establish authority.
Valid, stale, altered and absent hints preserve facts and retry identity.
Cold/warm dependency workloads measure queue/service latency, I/O,
peer overhead and cache effects. Publish the measured benefit or lack thereof;
the feature must exist without requiring it to improve every workload.

### 4. Expand useful persisted programming and native authoring

**Status:** Pending.

**Outcome:** Persisted business rules can use practical deterministic query
composition, and developers can author native queries/programs conveniently
and query database values, plain data and transaction logs together.

**Focus:** Close the current restrictions on predicates, rules, negation,
historical sources and dynamic attributes in program queries. Reuse the native
query model and resource controls where appropriate, with versioned durable
forms. Add ergonomic query/program authoring over existing representations;
typed builders or a safe data/text front end need not embed Clojure or reproduce
its exact syntax. Do not require a new general-purpose programming language.
Add plain datom/tuple sources that substitute for database-pattern sources
without rewriting the query. Integrate the existing authenticated log API with
query through native `tx_ids`/`tx_data` equivalents. Preserve existing multiple
database sources and relation-input semantics; keep adapters bounded and under
query controls rather than requiring PostgreSQL for plain-data tests.

**Completion signal:** Representative business rules exercise all five formerly
excluded query capabilities, with source-backed semantics and native/durable
checks. Speculation, durable execution, old program versions, retry and recovery
agree. Unsafe/nondeterministic execution remains controlled; application examples
show usable authoring rather than only hand-assembled low-level instructions.
The same query runs over a real database source and an equivalent small datom
fixture. Log-derived changes join to entity/transaction provenance facts with
correct range boundaries, cancellation and documented source semantics.

### 5. Add integrated peer-local fulltext

**Status:** Pending.

**Outcome:** Selected string attributes support analyzed, relevance-ranked
search that composes with Datalog and respects the queried database view.

**Focus:** Versioned schema/search metadata, native analysis and search,
background indexing, immutable durable search ownership, query integration,
current/history visibility and rebuild/recovery/backup/GC behavior. Consult the
recovered per-attribute search, candidate filtering and publication designs.

**Completion signal:** An application searches text and joins results with
structured facts. Documented analyzer/query semantics, lag and view filtering
are tested across updates/retractions, reopen, backup/restore and reclamation.
Queries run at peers using PostgreSQL-backed durable search data. Supported
excision behavior is explicit and safe; no extra service or substring-only
substitute is presented as fulltext.

### 6. Add partition assignment and useful locality controls

**Status:** Pending.

**Outcome:** Applications can group related entities into custom/implicit
partitions and control new-entity placement without changing transaction meaning.

**Focus:** Named partition schema data, implicit partition helpers, force/match
assignment, allocation/tempid/upsert interactions, native input forms and durable
validation. The current whitelist is an implementation restriction, not a
PostgreSQL requirement. Preserve existing IDs and exact receipt meaning.

**Completion signal:** Related-entity and tenant/customer examples exercise both
partition forms and force/match assignment through local and remote submission.
Invalid/conflicting assignments reject consistently; identity, speculation,
indexes, historical views, recovery and retry remain correct. Measure locality
under an appropriate workload without promising a universal speedup or making
partitions an authorization boundary.

### 7. Establish integrated product and operating acceptance

**Status:** Pending.

**Outcome:** The newly implemented capabilities work together as a usable native
database, with reproducible deployment instructions and an honest operating envelope.

**Focus:** Exercise the actual executable/SDK path with remote peers, programs,
search, locality, notifications/resumable consumers, snapshot handoff, query
sources, caching, failures and lifecycle operations. Measure increasing reader
counts, warm selective queries, cold-start bursts and a scan-heavy analytics
peer alongside normal application traffic. Include throughput, latency tails,
memory/cache behavior and total PostgreSQL load; separate query CPU scaling
from shared-storage bottlenecks. Profile and improve material bottlenecks,
including the expensive G3 administrative paths, without weakening integrity
or inventing an unmeasured performance target.

**Completion signal:** Real PostgreSQL end-to-end acceptance covers every named
capability and relevant interrupted/retry/upgrade boundaries. Measurements and
instructions distinguish ordinary and broad work, changed versus retained
baselines, and tested versus untested conditions. Every required feature has
implemented, exercised evidence; no missing capability is hidden as an exclusion.
Read-scaling evidence uses more than one fixed peer count and measures foreground
plus background/health/pin traffic, not only node-read counters. Validate cached
native query behavior rather than substituting eager compatibility reads or
local basis access. Publish the observed limits without promising linear scaling
or arbitrary offline reads. Only then close this parent.

## Continuation

Scaffolds reconciled with the talk-derived additions; implementation of this
phase has not begun. Read Goal1 and reconcile current executable/API surfaces,
then implement Stage1 directly. G3
remains the preserved baseline, not an instruction to repeat its entire campaign.
At session boundaries record the active child, last verified result, actual
remaining issue and next useful action. Report blockers plainly when new
authority or information is genuinely required.
