# Goal 4 — Complete Useful Native Read Access

## Objective

Complete Goal 0 Stage 4: applications query and navigate exact immutable native
database values, access transaction history efficiently, and use practical read
composition without accidental finite language ceilings. Preserve the working
Rust/PostgreSQL database and completed Goals 1–3.

## Constraints and context

- Inherit Goal 0. One active child, no nested goals or corrective parents.
- `datomic_pro_docs/05_query_and_pull`, `06_indexes/03_index_pull.md`, and
  `07_peer_api/02_shared_reference/00_log_api.md` govern semantics; recovered
  query/pull/log implementations in `1.0.7705` guide algorithms, not JVM parity.
- Native query, pull/entity, history, time windows and raw bidirectional cursors
  already exist. Preserve exact source propagation, aliases, schema, cycle and
  noHistory semantics. Goal 3's pure successors support controlled functions,
  tuple/lookup inputs and filtered values; reopen that child for owning gaps.
- At entry, query defaults limited work/intermediates/results and Pull capped
  depth at 512/entities at 100,000. These accidental limits are now removed;
  traversal uses heap frames. These defaults were not language limits.
  Keep explicit budgets and documented default many-valued Pull limit 1,000.
- Large local results may inherently take proportional memory/work. Lazy pull
  projection does not require a fully streaming Datalog evaluator. Do not claim
  scalability without measurements; realistic combined load belongs to Goal 6.
- Assess fulltext, transaction hints and specialized features individually.
  Implement core/practical low-machinery features; omissions need an honest
  consequence and justification, not blanket parity or determinism exclusions.

## Stages

### 1. Remove accidental read ceilings

**Status:** Complete; final integrated regressions remain Stage 4 work.

**Outcome:** Ordinary queries and unlimited pull recursion work without hidden
finite cutoffs, while callers can impose explicit resource policy.

**Focus:** Query default controls and accounting, iterative pull execution,
cycle/depth semantics and deep result lifetime. Preserve documented many limits,
timeouts/cancellation and shared query/pull budgets.

**Completion signal:** Large-result and small-stack deep-navigation fixtures
cross former ceilings, including ordinary result destruction; explicit limits
reject predictably and existing exact-source/pull/query tests remain correct.

### 2. Expose efficient immutable history and index navigation

**Status:** Complete; public workflow integration remains Stage 4 work.

**Outcome:** Applications traverse transaction-ordered history and lazily pull
from appropriate indexes without eager database reconstruction.

**Focus:** Immutable native log/range access with time boundaries and exact
generation lifetime; index-pull using existing raw cursors and exact values.
Keep reader authority independent of writer availability.

**Completion signal:** Real PostgreSQL tests preserve captured log/index views
through new writes, recovery and relevant generation transitions, demonstrate
bounded cursor access and exercise public application APIs.

### 3. Finish practical read composition

**Status:** Complete.

**Outcome:** The useful native query/pull surface is coherent and documented.

**Focus:** Return maps, nested query composition, pull transforms, lazy result
projection and local random aggregates. Assess fulltext and transaction hints
against the objective and actual source; implement or justify each remaining
specialized omission without hiding a central information-model gap.

**Completion signal:** Implemented features have direct semantic fixtures and
native examples; exact source and resource controls survive composition.
Every material omission has an explicit practical consequence and rationale.

### 4. Return verified read access to Goal 0

**Status:** Complete on focused acceptance, live workflow and all-target clippy.
The longer all-target regression subsequently completed successfully. Reopen
this child if later parent checks reveal a read gap.

**Outcome:** Read access works as part of the application, and Goal 0 advances
to operations/integrity without losing known limitations.

**Focus:** Focused regressions, real PostgreSQL and workflow checks, measured
large-result/deep-navigation costs, accurate API docs and parent reconciliation.

**Completion signal:** Child outcomes hold, no known core read gap is hidden,
and parent Stage 4 records evidence and resumes Goal 5.

## Continuation

Goal 4 is complete on its observable outcomes. Return to Goal 0 and execute
Goal 5; do not stop at this child. The broad regression passed (its expensive
1,000-step eager identity fixture took 311.31s). The final nested additions
passed separately. Final parent checks must include current code and reopen
this child for any read failure. Goals 5–6 remain required.

## Verified checkpoint

- Closure: all-target clippy passes after formatting. Final nested suite 8/8
  passes on actual PostgreSQL in 18.74s: all result/input shapes, stored tuple
  joins, nested maps, nils, unknown inner sources with empty outer input, shared
  controls and captured sources after writer shutdown. Native queries examine
  at most four datoms with zero eager loads. Full Rust all-target regression
  completed with exit 0; library 251 passed/1 ignored. Unconfigured
  PostgreSQL cases in that broad run are explicitly not live evidence.

- Query input/result set formation used quadratic repeated linear searches.
  Stable offset sorting now preserves first representations in O(n log n);
  grouping uses ordered keys. A 100,005-row local input relation projects in
  1.006 seconds; this is not a PostgreSQL-scale claim. Query suites: exact
  sources 5, extensions 2, primitives 5, rules 4, semantics 9, unbounded 2 pass.
- Iterative Pull: existing 13 tests plus three pure witnesses pass. A 2,048-node
  graph traverses on a 256 KiB stack with ordinary clone/equality/drop; a compact
  component DAG expands to 131,071 maps. Actual PostgreSQL 768-node captured
  value passes after new writes and writer shutdown with zero eager loads.
  Explicit controls and the documented many-valued default 1,000 remain.
- Native immutable log: actual PostgreSQL 3/3 in 51.86s, plus corruption/fused
  cursor 1/1 in 17.89s. Captured generation/basis, noHistory facts, T/Tx/Instant
  ceilings, excision/recovery and restricted peer authority are exercised.
  Traversal buffers one authenticated transaction beyond the shared snapshot;
  this is a memory-shape statement, not a throughput benchmark.
- Index-pull: actual PostgreSQL 6/6 in 36.30s; pure 5/5. A 512-entity fixture
  with offset 3/limit 2 performs no construction loads, exactly two projections,
  ten cursor reads/196,281 bytes/five leaf reads and zero eager materializations.
  AVET/AEVT direction, many refs, duplicates, exact views and cancellation pass.
- All-target compilation passes after adding consuming QueryValue accessors.
  Stack-safe Drop means owned enum fields must be extracted with into_map,
  into_collection, into_tuple or into_scalar; borrowed pattern matching is unchanged.
- Deep diagnostics/comparison: five tests pass, including 10,000-level result
  formatting/drop on 256 KiB and 1,000-level pathological map keys/duplicate-key
  values. A real stack overflow was reproduced before iterative canonical
  normalization. Nonempty map comparison uses linear additional arena storage;
  it is not advertised as constant-space.
- Pull transforms and ownership: pure 4/4, existing Pull 13/13 and unbounded
  3/3 pass; live transform fixture 1/1 in 18.26s. Nil reaches transforms before
  defaults; defaults are not transformed. Named native callbacks propagate
  errors/panics and obey before/after cooperative controls. Explicit pattern
  clone/equality/drop and component touch/shared-cache drop are iterative,
  including concurrent last-owner destruction on 128 KiB stacks.
- Native return maps: five tests cover typed keyword/string/symbol keys,
  positional find order, nested results and invalid/empty shapes. An explicit
  arity overload validates empty results whose QueryResult erased find width.
- Query sequence prepares joins/aggregates but defers Pull/transforms. It owns
  exact sources, counts prepared rows without projection and shares the original
  deadline/work/cancel account. Five composition fixtures pass, including local
  rand/sample and equal Pull maps from distinct entity bindings; projection must
  not deduplicate these (recovered query.clj apply-pf, 1506–1518).
- Persisted query extensions no longer reset parent fuel on each call. Four
  pure budget tests pass (measured fuel deltas, sibling exhaustion, deadlines,
  cancellation and remaining allowances); live native fixture 1/1 in 18.14s.
  ProgramRuntime adds a shared-budget exact query entry without changing stored
  ABI. Runtime 22 and exact-source 5 regressions pass. Native callbacks remain
  trusted cooperative Rust, not a sandbox or forced preemption boundary.
- Extended public workflow passes on actual PostgreSQL port 55433:
  `workflow-163985-1788935959372806748`, t=3. Preserved all controlled tuple,
  lookup/speculation checks; added log/index-pull, return maps and deferred
  transforms consumed after writer shutdown. No server restart in this run.

## Specialized feature decisions

- Fulltext is intentionally not implemented; `db/fulltext` remains rejected.
  Schema docs 03_schema/00_schema_data_reference.md:114 and query reference:873
  require analyzed, ranked, eventually consistent per-attribute search, not
  substring equality. Recovered peer/datomic/fulltext.clj search-iterable:174
  merges search hits with exact database/window membership. Honest support needs
  a search index and its snapshot/lifecycle design; it is not necessary for core
  Datalog, identity or information history. Applications needing token search
  must maintain a separate search projection. No fulltext compatibility claim.
- Transaction hints are optional nonauthoritative peer-to-writer segment
  prefetch (04_transactions/08_transaction_hints.md; peer/db.clj:7938 and
  transactor/update.clj:348–450), with hint failures ignored. Native bounded
  transaction read memoization exists, but there is no cross-peer hint channel.
  Defer that unmeasured optimization; cold writes can do more index I/O. This
  does not change transaction meaning or acknowledgment guarantees.
- Return-map syntax, Clojure dynamic function resolution and qseq wrapper types
  are native Rust APIs, not wire/EDN compatibility requirements. Local random
  aggregates are supported independently of deterministic persisted execution.

## Next-child findings (not completion claims)

Goal 5 has two reproduced static gaps: backup tree verification checks hashes,
shape and claimed state coordinates, not actual derived datoms against log
state (including request-base archives); and older program-generation marks
can retain an obsolete complete=true after the dependency walker was repaired.
Use existing live semantic-projection comparison and authenticated backfill
machinery, version the completeness witness, and keep GC fail-closed until repair.
Goal 6 must still measure the combined deployment under declared load/failure.
