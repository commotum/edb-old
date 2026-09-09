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
- `QueryControl::default` currently limits work/intermediates/results;
  `PullControl::default` caps depth at 512 and entities at 100,000. Unlimited
  recursion still uses Rust recursion. These defaults are not language limits.
  Keep explicit budgets and documented default many-valued Pull limit 1,000.
- Large local results may inherently take proportional memory/work. Lazy pull
  projection does not require a fully streaming Datalog evaluator. Do not claim
  scalability without measurements; realistic combined load belongs to Goal 6.
- Assess fulltext, transaction hints and specialized features individually.
  Implement core/practical low-machinery features; omissions need an honest
  consequence and justification, not blanket parity or determinism exclusions.

## Stages

### 1. Remove accidental read ceilings

**Status:** Active.

**Outcome:** Ordinary queries and unlimited pull recursion work without hidden
finite cutoffs, while callers can impose explicit resource policy.

**Focus:** Query default controls and accounting, iterative pull execution,
cycle/depth semantics and deep result lifetime. Preserve documented many limits,
timeouts/cancellation and shared query/pull budgets.

**Completion signal:** Large-result and small-stack deep-navigation fixtures
cross former ceilings, including ordinary result destruction; explicit limits
reject predictably and existing exact-source/pull/query tests remain correct.

### 2. Expose efficient immutable history and index navigation

**Status:** Pending.

**Outcome:** Applications traverse transaction-ordered history and lazily pull
from appropriate indexes without eager database reconstruction.

**Focus:** Immutable native log/range access with time boundaries and exact
generation lifetime; index-pull using existing raw cursors and exact values.
Keep reader authority independent of writer availability.

**Completion signal:** Real PostgreSQL tests preserve captured log/index views
through new writes, recovery and relevant generation transitions, demonstrate
bounded cursor access and exercise public application APIs.

### 3. Finish practical read composition

**Status:** Pending.

**Outcome:** The useful native query/pull surface is coherent and documented.

**Focus:** Return maps, nested query composition, pull transforms, lazy result
projection and local random aggregates. Assess fulltext and transaction hints
against the objective and actual source; implement or justify each remaining
specialized omission without hiding a central information-model gap.

**Completion signal:** Implemented features have direct semantic fixtures and
native examples; exact source and resource controls survive composition.
Every material omission has an explicit practical consequence and rationale.

### 4. Return verified read access to Goal 0

**Status:** Pending.

**Outcome:** Read access works as part of the application, and Goal 0 advances
to operations/integrity without losing known limitations.

**Focus:** Focused regressions, real PostgreSQL and workflow checks, measured
large-result/deep-navigation costs, accurate API docs and parent reconciliation.

**Completion signal:** Child outcomes hold, no known core read gap is hidden,
and parent Stage 4 records evidence and resumes Goal 5.

## Continuation

Goal 4 Stage 1 is active. Replace accidental finite defaults with explicit policy
and make unlimited pull execution/result handling stack-safe. Reuse existing
native readers; do not replace the database or expand archived goal loops.
Disposable PostgreSQL fixtures and completed transaction evidence are recorded
in Goals 2–3. Goals 5–6 remain required after this child completes.
