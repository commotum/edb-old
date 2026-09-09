# Goal 0 — Finish the Native Datomic Database

## Objective

Finish a usable, production-quality Rust database backed only by PostgreSQL
that preserves Datomic's central benefits: immutable facts and database values,
declarative atomic transactions in one authoritative order, strong identity,
schema as information, accessible history and time views, local peer query and
graph navigation, and reliable recovery and operation. Complete the existing
implementation rather than restart it.

Use `datomic_pro_docs/` to determine the intended behavior and
`1.0.7705/` to understand the architecture, algorithms, and performance choices
that make it practical. Translate that design thoughtfully into Rust and
PostgreSQL. Success is a coherent working database, not exhaustive equivalence
to compiled Datomic or completion of an expanding set of goal folders.

## Constraints

- Rust runtime, PostgreSQL storage only. Existing Datomic database formats,
  JVM/Clojure execution, wire compatibility, and other storage backends are
  outside the objective.
- Documentation governs semantics. Recovered source supplies implementation
  evidence and resolves detail; investigate significant disagreements and
  record deliberate native differences. Preserve useful recovered designs
  without making exact classes, tree depth, compiler output, or internal error
  precedence requirements in themselves.
- Preserve immutable complete database values, unordered transaction meaning,
  schema/ident authority, exact snapshot propagation, and one serialized fenced
  writer. Persisted behavior must remain useful, deterministic, and controlled.
- Keep queries and immutable caches at peers. Ordinary reads, writer state,
  and localized updates must work with databases larger than memory through
  durable indexes and bounded recent state. Measure costs against workload and
  selectivity; do not demand constant work for broad scans or restrictive filters.
- Preserve acknowledged commits, explicit unknown outcomes, safe retry,
  failover, and versioned durable meaning. Respect existing data and applied
  migrations; make compatibility boundaries explicit.
- Authenticate ordinary root adoption under a trusted, restricted indexer
  model. Reserve full tree-versus-log equivalence checks for deep verification;
  do not require a new proof system for lazy opens.
- Treat operational limits as explicit policy. Preserve documented semantics
  such as Pull's default cardinality-many limit. Allow inherently broad work
  such as backup, excision, and index creation to have measured, documented
  costs. Preserve immutable captured values without inventing an exact
  cross-rebuild retained-history promise for `noHistory`.
- Verify risky boundaries with real PostgreSQL and relevant failures. A test
  that returns without database configuration is not integration evidence.
  Use targeted source/runtime witnesses; global AOT equivalence and complete
  dormant-branch coverage are not prerequisites.

## Starting point — 2026-09-08

**Status:** Open; Stage2 is reopened by a scaled background-indexing failure. This is a fresh
strategy over the existing code, not a claim that historical repairs vanished.

- The initial Rust goals 0–8 are preserved in `goal-archive/A1/`; corrective
  goals 9–17 are in `goal-archive/A2/`. Their plans, prompts, and completion
  labels are historical evidence, not active instructions. This plan and its
  loop replace both parent loops and their uncreated Goals 18–20.
- The code already includes a substantial semantic kernel, durable log,
  persistent trees, bounded recent state, tiered writer, exact query/pull
  database values, controlled programs, fencing, and lifecycle tooling.
  Corrective Goals 10–16 record focused PostgreSQL and failure evidence.
- The review at code commit `19b799d` attempted
  `cargo test --offline --all-targets --no-fail-fast`; compilation failed because
  a `TieredReadCore` initializer in `src/peer.rs` omitted `lineage_id`. No tests
  executed in that review. Recheck current code before acting.
- The former Goal 17 is partly implemented beyond its written status:
  time-point resolution, raw-boundary normalization, bidirectional cursors,
  a connection facade, and complete report types exist with test fixtures.
  Finish and verify these pieces rather than build them again.
- Static review found catch-up without report enqueueing, fallible report
  adoption after successful commit when ticket waits are out of order,
  connection construction coupled to service startup, eager peer fallbacks,
  and missing background/specialized synchronization at the public boundary.
  These are investigation targets, not newly reproduced runtime failures.
- Unresolved transaction tuple references, chainable native speculative
  values, default query/pull ceilings, API omissions, deep backup/archive
  semantic checks, and integrated production evidence remain.
- The newer decompilation is a sufficient source-study corpus. It is not a
  standalone recovered runtime. Consult the archived evidence ledger and
  relevant source directly; restore a narrow oracle only for a consequential
  question that needs executable reference evidence.

## Parent and child execution

Goal 0 owns the overall objective and continuation loop. Each numbered stage
maps to the active repository folder `goal-N/`: Stage 1 to `goal-1/`, through
Stage 6 to `goal-6/`. These names are distinct from the archived goal passes.

For the first unfinished stage, use `$scaffold-goal` to create its three-file
scaffold if absent, or reconcile and resume the existing child if present.
Execute the child through its completion signal, fold material evidence and
status into this plan, then return to the parent and repeat for the next stage.
Creating a scaffold does not complete a stage and is not a stopping point when
running the parent continuation prompt.

Keep one child as the active focus, with integration checks throughout. Fix
necessary prerequisites across stage boundaries without manufacturing new
goals. Revise unfinished stages and their child plans when evidence warrants;
preserve established child identities and completed work. Child requirements
must serve the parent objective and must not reintroduce exhaustive proof gates.
After all children complete, verify the overall success condition and return
any discovered gap to its owning child before declaring Goal 0 complete.

## Stages

### 1. Restore a trustworthy runnable baseline

**Status:** Complete; [Goal 1](../goal-1/0-plan.md) records the verified baseline.

Lineage initialization, forward raw-boundary routing, and atomic catch-up report
publication repaired. Build/clippy and pure baseline pass. Real PostgreSQL
15.11 evidence: 19 peer, 6 durability (including actual restart), and 15 selected
service tests pass; the corrected corruption witness ran separately. A public
schema/transact/query/pull/history/immutable-value/reopen example runs. These
are baseline checks, not full live-suite or scale acceptance. See child evidence
for corrected fixtures and the isolated rerun after restart interference.

**Outcome:** The current implementation builds, its real failures are known,
and an ordinary application workflow provides a continuing integration check.

**Focus:** Reconcile the partial connection changes with actual code; fix the
build; run appropriate existing tests with explicit PostgreSQL execution;
establish a small schema/transact/query/pull/history/reopen workflow. Record
only material failures and evidence, retaining working prior repairs.

**Completion signal:** The build and relevant baseline tests pass, the
application workflow runs against disposable PostgreSQL, and remaining gaps
have concrete locations and ownership in the stages below. Do not require
every later feature to be complete before banking this baseline.

### 2. Complete the native connection and observation model

**Status:** Reopened; [Goal2](../goal-2/0-plan.md) owns the scaled background-index
preload/scheduling defects. Preserve its completed connection/observation work.

Progress: strict independent native reads, non-owning writer attachment and
replacement, ordered service observation independent of ticket waits,
background catch-up with complete tempid receipts, and native index/schema/
excision synchronization now have focused real PostgreSQL evidence. The public
two-peer workflow reads and reopens after writer shutdown. An eight-transaction
lagging-peer witness adopts the indexed prefix with zero recent datoms and zero
eager materializations; this is not a realistic scale benchmark. Independent-
process submission now uses a bounded/versioned same-user Unix socket. The
separate writer/two-peer process workflow passes, as do all 7 native connection
tests, transport commit-loss/receipt checks, writer/observer-stall checks, and
actual dedicated PostgreSQL restart with logical and physical sync. Excision
catch-up retains ordered original reports across multiple generations. Peer
db/sync defaults are native; eager adapters are explicitly named. The broader
PostgreSQL peer rerun passed 19/19. SQL/network
failure-envelope policy and realistic load remain required Stages 5–6 work;
socket delivery deadlines do not cancel arbitrary blocked receipt SQL.

**Outcome:** Applications connect, submit transactions, and observe exact
immutable native values through a coherent API, with reads independent of
writer availability and lifetime.

**Focus:** Separate connection attachment from embedded service ownership;
finish ordered background advancement, complete opt-in reports, and durable
success/unknown handling independent of ticket wait order. Complete native
db/sync defaults, raw forward/reverse traversal, T/Tx/instant views, and
transaction/index/schema/excision synchronization. Make eager oracle/admin
access explicit. Choose the smallest concrete deployment and delivery design
that supports independent peers and the single writer.

**Completion signal:** Own and external writes yield complete ordered reports;
concurrent submissions, catch-up, reconnect, and writer replacement preserve
exact old values and acknowledgement semantics. Native cursors and time views
match semantic fixtures with measured lazy access. An ordinary application
path does not materialize the eager database or require ownership of the writer.

### 3. Close transaction and speculative-value semantics

**Status:** Complete; [Goal 3](../goal-3/0-plan.md) records transaction acceptance.

Progress: primitive native `with` returns immutable before/after reports and
supports flattened speculative chains. Eager/native/PostgreSQL differentials
cover current/history/time/raw traversal, schema/index/noHistory changes and
ident aliases with no durable advancement or eager loads. A 2,000-step small-
stack witness and query/pull application branch pass. Tuple/ref-shaped lookup
inputs and controlled runtime values now pass eager/native/socket/recovery
fixtures with old receipts and program hashes preserved. The shared native/
durable controlled pipeline passes actual PostgreSQL acceptance, including code
retention after reclaim, shared limits, exact predicates and the docs' "as-of
is not a branch" rule. Final library 250 passed/1 ignored; semantic 22, tuple
schema 9, runtime 22 pass. Full controlled suite 4/4 and filtered native test
pass live; public controlled/tuple/speculation workflow reopens at t=3.

**Outcome:** The native API expresses the documented core transaction inputs
and supports pure, chainable speculative database successors.

**Focus:** Reference-shaped tuple slots and lookup keys, tempid/upsert
resolution, native `with`, complete successor validation, and controlled
function/predicate composition. Reuse existing semantic access and tiered
assessment. Version request/program changes where required, keeping stored
values distinct from unresolved transaction input.

**Completion signal:** Source-backed fixtures and eager/native differentials
cover legal and invalid reference forms, successive speculative transactions,
schema changes, and programs. Durable submission, retry, and recovery preserve
the same accepted facts and identity without full-database reconstruction.

### 4. Finish useful query, pull, and history access

**Status:** Complete; [Goal 4](../goal-4/0-plan.md) records native read acceptance.

Progress: accidental query/Pull defaults removed, iterative traversal and deep
result/selector ownership verified, native log and index-pull pass real PG,
and useful query/Pull composition is implemented. Shared persisted-query fuel,
deferred projections, return maps and native random aggregates have focused
tests. The expanded public workflow and all eight final nested shape fixtures
pass on actual PostgreSQL after writer shutdown. All-target clippy and broad
Rust regression pass; unconfigured PG cases are not live evidence. Fulltext and transaction
hints have individual documented consequences/deferral rationale, not blanket
parity exclusions. Stages 5–6 remain required.

**Outcome:** Local queries and navigation preserve exact snapshot semantics,
have no accidental finite language limits, and expose a truthful useful surface.

**Focus:** Explicit resource controls, stack-safe unlimited pull recursion,
accessible log history, and remaining query/pull APIs. Assess nested queries,
return maps, pull transforms, lazy result projection, index-pull, fulltext,
random aggregates, and transaction hints against the objective and source.
Implement core and practical low-machinery features; justify specialized
omissions individually. Deterministic persisted execution does not by itself
forbid randomness in local read queries. Lazy result projection need not imply
a wholly streaming Datalog evaluator.

**Completion signal:** Large-result/deep-navigation fixtures work absent
explicit limits; documented limits remain correct. Implemented features have
direct semantic checks and an application example. Every material remaining
omission is identified with its consequence; none removes a central benefit
or is hidden behind a parity claim.

### 5. Establish operational integrity and recovery

**Status:** Complete again; [Goal 5](../goal-5/0-plan.md) records the repaired
ordinary-GC liveness gap and current25 operational evidence.

Integrated testing found that active-generation receipt bases pin the oldest
physical publication and prevent later prefix-ordered retirement indefinitely.
Migration25 now provides bounded resumable same-hash archive handoff preserving
receipts and snapshot pins. Actual tiny workflow reclaims publications8→2 and
nodes138→120 with blocked prefixes2→0 and no lost receipt bindings. New ordinary
conversion, interrupted kind1-generation/excision, backup/restore/exact retry,
upgrade/role and all15GC+3integrity checks pass. A second archive/semantic-GC
phase mismatch exposed by this lifecycle was also repaired and verified. Costs
of distinct retained receipt closures remain to be measured at Stage6 scale.

Deep backup/archive and pending-index provenance checks now reject validly
hashed false trees. Migration24 repairs versioned code roots, preserves paused
restore roots and rejects truncated published/retired logs atomically; all eight
upgrade fixtures pass live. Real backup/restore, GC/pins, interrupted excision,
role/TLS, configured SQL timeout/unknown-retry and actual PostgreSQL restart
witnesses pass. Native and separate writer/two-peer workflows pass on schema24.
The active operator guide states quiesced repair, retention and deadline limits.
Broad administrative costs and deployment scale remain required Stage6 work.

**Outcome:** Operators can protect, recover, upgrade, inspect, reclaim, and
excise the database under an explicit and tested trust and retention model.

**Focus:** Deep semantic comparison of backup and request-archive trees with
their authoritative log points; restore/retry correctness; root and generation
lifetimes; migration/runtime authority; secure transport; excision completion
and external retention limits. Revisit existing code only where fresh evidence
shows a gap. Document costs of broad administrative operations. Reconcile older
program generation-reference rows for dependencies previously omitted inside
callable lookup references and dual-predicate bodies; new traversal is repaired.

**Completion signal:** Meaningful corruption, interrupted backup/restore,
restart, GC/pin, upgrade, and excision witnesses pass on PostgreSQL. Deep checks
detect internally consistent false derived trees; normal trusted-root opens
remain lazy. Operator procedures state supported guarantees and limitations.

### 6. Demonstrate the complete system under realistic load and failure

**Status:** Paused; [Goal6](../goal-6/0-plan.md) preserves the scaled workflow
while owning Goal2 repairs the newly exposed indexer failure and scheduling.

**Outcome:** One reproducible deployment and acceptance workflow demonstrates
the original database objective and a measured operating envelope.

**Focus:** Independent peers, serialized concurrent writes, representative
queries and programs, data/history larger than caches, consolidation, abrupt
process/server failures, failover, recovery, and operational procedures.
Provision isolated fixtures and prove tests execute. Measure memory, range I/O,
recovery latency, and throughput against declared workload and resource sizes;
fix discovered defects directly rather than creating another corrective parent.

**Completion signal:** The coherent application/deployment workflow and
risk-appropriate integrated tests pass; retained snapshots and committed history
remain correct under failure; measurements support the claimed scale. Current
API documentation and run instructions describe the actual supported system.

## Success and continuation

Complete when the Rust transactor and independent peers, backed only by
PostgreSQL, deliver the core information model as one usable, tested,
recoverable system, with measured operational behavior and intentional,
documented differences from Datomic. No known core correctness or usability
failure may be relabeled as a non-core omission to close the goal.

**Continuation checkpoint — 2026-09-09:** Goal2 is the only active child again.
Goal 3 is verified; preserve its input grammar, controlled/filtered speculative
successors and dependency retention. Goal 4 read-depth/history/composition APIs
and public workflow pass. Goal5 closes semantic backup verification, versioned
program-reference repair. Goal6 exposed active-receipt publication-prefix GC
blocking, repeated index leaf decoding and a missing import retry policy.
The GC/commitment SQL batching and retry repairs pass focused live checks.
The next100k run reached head982/98100records but its indexer failed with a
missing in-memory node that exists durably. Repair and verify that boundary
omission and finite background scheduling in Goal2, then resume Goal6's complete
scaled deployment/recovery/maintenance. Neither failed100k run is acceptance.
No overall production outcome has yet been claimed.
