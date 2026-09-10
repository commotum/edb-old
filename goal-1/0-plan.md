# Goal 1 — Restore declarative query and identity correctness

## Objective and ownership

Complete Stage1 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: repair negative
rule evaluation (R1), disjunction readiness (R3) and anonymous identity collisions
(R2) in the existing product, preserving valid behavior and durable retries.

Status: **in progress: regression setup and shared repairs**. This is Goal0's first active
child. Its internal steps do not create additional goal folders. After its
completion, return to Goal0; the other parent stages remain required.

Use the Datomic docs and recovered 1.0.7705 source named in the parent. Code pointers
below are starting points, not prescribed patch boundaries. Reconcile current
code/tests before implementation and preserve existing fixes. No rewrite, wire/JVM
parity, blanket rejection of useful query forms, or reserved-name shortcut may
substitute for the requested repair.

## Essential starting cases

### R1: complete negative subqueries before taking their complement

Use a tuple source containing `[1, "person", true]` and `[1, "blocked", true]`.
Define `blocked(x)` by the blocked pattern and `eligible(x)` by the person pattern
plus `not-join [x] (blocked x)`. Query eligible entities. Expected: none. Reviewed
behavior: entity1 is incorrectly returned, while equivalent direct negation works.

`src/query.rs` creates a nested rule memo while `solving_rules` is true and reads
its unfinished empty relation; the global fixed point subsequently only adds
answers. Consult recovered `datomic/datalog.clj` (`eval-not-join`, rule evaluation)
and the query reference's negation/rule scoping/binding rules.

Preserve positive recursive fixed points, alternative rule definitions, required
bindings, explicit sources and bounded cancellation. Resolve negative dependencies
at the correct logical stage; genuinely unsupported negative cycles must fail
clearly rather than emit provisional answers. Do not reject valid acyclic negated
rules to avoid fixing the evaluator, or simply reorder the witness.

### R3: schedule Or only when each branch's required inputs are available

Use source `[1, "person", true]`, a binding pattern `[?e "person" ?v]`, and an
Or whose two branches test `?e = 1` and `?e = 2`. Both equivalent outer clause
orders should return entity1. Reviewed behavior: placing the binding pattern
before Or can fail with `query/insufficient-binding`; the reverse order succeeds.

Inspect `clause_ready`, branch variable analysis and scoring in `src/query.rs`.
The query reference explicitly says Or is delayed until necessary variables bind.
Cover Or/OrJoin input/output requirements, nested branches, functions/predicates,
rules and genuinely insufficient input without changing valid relational meaning.

### R2: generated identities must not overlap caller-supplied identities

Install a cardinality-many string `tag` attribute. Submit two entity maps: one
with explicit `Temp("__map/00000000000000000000")` and tag `"explicit"`, the other
with no ID and tag `"anonymous"`. Expected: two entities. Reviewed eager and exact
DatabaseValue paths both produced one entity with both tags. Cardinality-one
attributes can instead show a spurious conflict.

Inspect `src/transaction.rs` anonymous allocation/normalization and every shared
expansion path, including nested/generated/persisted forms. Recovered `db.clj`
allocates fresh numeric anonymous IDs separately from interning explicit strings.
Use collision-free deterministic identity allocation or a distinct internal type;
leave implementation choice to evidence. A random name without retry guarantees,
a prefix ban, or only scanning the initial unexpanded forms is insufficient.

Preserve explicit tempid coalescing, reference resolution, unique-identity upserts,
declarative form-order normalization and exact old request receipts. Audit request
canonicalization before changing normalized forms/codecs. Already committed
collisions cannot necessarily reveal the caller's lost intent: do not fabricate a
historical repair or silently split old entities. State that limitation and any
safe diagnosis path; future submissions must not repeat the collision.

## Ordered work

### 1. Make the counterexamples permanent regressions

- **Outcome:** Repository tests expose all three defects and retain successful
  nearby baselines, so subsequent changes cannot mistake reduced functionality
  for correctness.
- **Focus:** Small independent expected results; clause/rule/input permutations,
  explicit-source cases and identity counts. Reuse existing test support rather
  than reproducing the entire acceptance harness. Optional `/tmp` witnesses from
  the review are aids, not dependencies. Establish the configured PG fixture early
  and capture a pre-repair acknowledged-request fixture before changing identity
  normalization, including its original receipt/tempid map and program dependencies.
- **Completion signal:** Regressions demonstrate current failures, or current-tree
  evidence explains a previously completed repair. The normal eager/exact/public
  entry points are represented; baseline valid queries/identities still pass.
- **Status:** Not started.

### 2. Repair query dependency semantics

- **Outcome:** R1/R3 produce correct stable results under valid clause/rule order
  changes, including composition of negation and disjunction.
- **Focus:** Readiness/input analysis and negative-subquery completion around the
  existing set-oriented evaluator. Use docs/source to settle supported recursive
  semantics; maintain scope, budgets, prepared-query and stored-program behavior.
- **Completion signal:** The query counterexamples and positive/mutual recursion,
  nested negation/disjunction, multiple source, required binding and cancellation
  cases pass with explicit expected results. Include negated calls to recursive
  positive rules, not just nonrecursive negative clauses. No incomplete memo escapes as an
  authoritative negative answer, and genuine binding errors remain errors.
- **Status:** Not started.

### 3. Repair anonymous identity without breaking durable meaning

- **Outcome:** R2 no longer aliases unrelated entities in any relevant authoring
  path, while intentional identity/upsert relationships remain intact.
- **Focus:** Shared normalization and expanded forms; multiple anonymous maps,
  explicit reserved-looking names, nested references, persisted emitters and
  input permutations. Preserve request/receipt identity across deployment changes.
- **Completion signal:** Both eager and exact witnesses produce two entities;
  intentional shared IDs and unique upserts still merge correctly. Configured PG
  commits, restart and exact retry agree with returned reports. Any required codec/
  request versioning has a pre-change fixture proving old receipt compatibility;
  no durable migration is introduced merely for convenience.
- **Status:** Not started.

### 4. Verify the combined application behavior and hand back to Goal 0

- **Outcome:** The three repairs work through the native application, not only
  isolated tuple/oracle tests, with clear regression and compatibility evidence.
- **Focus:** Use the evolving application example, CLI/process support and a
  disposable real PostgreSQL database. Exercise map authoring, stored functions
  where applicable, peer-local rule/Or queries on committed values, immutable
  db-before/db-after and history, restart and exact retry. Check local/remote
  boundaries when normalization or request encoding is touched. Complete relevant
  broad tests; diagnose environment restrictions without counting skips as proof.
- **Completion signal:** R1/R2/R3 counterexamples and combined variations pass;
  query/transaction regressions and configured application/PG checks pass;
  formatting/all-target compilation and relevant Clippy checks show no unexplained
  regression. Record actual commands, fixture execution, compatibility decisions
  and uncertainty. Mark parent Stage1 complete and return to its first unfinished
  stage rather than ending the overall product effort here.
- **Status:** Not started.

## Evidence policy and continuation

Parent invariants and resource/evidence rules apply. Do not compare two paths that
share the same bug and call that semantic proof; keep independently stated expected
relations/entities. Do not hide evaluator work behind uncounted helpers or reset
shared budgets during negative subqueries. Later stages own numeric, graph-depth,
fulltext and scalability repairs; preserve those paths without quietly absorbing
all twelve repairs into this child.

Use `CARGO_PROFILE_DEV_DEBUG=0`, `CARGO_PROFILE_TEST_DEBUG=0`, `CARGO_INCREMENTAL=0`
for routine diagnostics to control build output. Use real disposable PG fixtures
with evidence the tests executed. Missing privileges/configuration are blockers
to the affected completion check, not successful tests. Source changes, not this
scaffold, must add permanent regressions.

Continuation (2026-09-10): query and normalization repairs are underway from
`c0bc499`. Establish the disposable PG fixture and preserve the old executable/
acknowledged receipt before rebuilding; then run permanent counterexamples and
shared/application regression checks. No step is marked complete yet. Final
child signal remains step4, followed by returning to the parent loop.
