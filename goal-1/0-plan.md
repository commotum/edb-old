# Goal 1 — Restore declarative query and identity correctness

## Objective and ownership

Complete Stage1 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: repair negative
rule evaluation (R1), disjunction readiness (R3) and anonymous identity collisions
(R2) in the existing product, preserving valid behavior and durable retries.

Status: **complete (2026-09-10)**. Goal0 now owns continuation at Goal2. This
child's internal steps do not create additional goal folders; the other parent
stages remain required.

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
- **Status:** Complete: permanent counterexamples, baseline reproduction and actual
  pre-repair PostgreSQL receipts captured before rebuild.

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
- **Status:** Complete: typed negative-completion task stack, batched demands,
  tracked memo completeness and binding-aware Or pass semantic/resource regressions.

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
- **Status:** Complete: eager/exact, generated/nested forms, upserts, configured
  PostgreSQL restart/retry and real pre-repair receipt compatibility verified.

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
- **Status:** Complete: final CLI/PG regression, nearby query/transaction suites,
  upgrade receipt verification, formatting and all-target Clippy checked.

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

### Verified execution evidence (2026-09-10)

- Baseline `c0bc499`; the retained old review executable still reproduces R1/R3.
  New `query_dependency_repairs` cases pass (10); 55 nearby query checks pass,
  with one opt-in measurement ignored. These runs did not establish PG coverage.
- `transaction.rs` now reserves the complete expanded explicit-tempid namespace
  before deterministic anonymous allocation. No persisted encoding, request hash,
  public EntityRef or receipt format changed. Ordinary allocation remains stable;
  historical collided entities are not split or reinterpreted on retry.
- PostgreSQL 15.11 is actually running, with fsync/synchronous_commit/
  full_page_writes on, in disposable `/tmp/atomic-repair-pg.vA037i/data`, loopback
  port55471. Test URL: `host=127.0.0.1 port=55471 user=atomic_repair dbname=atomic_repair`;
  `ATOMIC_POSTGRES_TRANSPORT=plaintext`. Socket tests use approved host execution.
- `tests/identity_upgrade.rs` was compiled with rustc against the old rlib before
  any Cargo rebuild. Seed and repaired verify both passed: direct-map and stored
  emitter collisions still return the exact old hash/tempids/datoms/before/after/
  history on retry; fresh request keys produce distinct entities. Baseline binary
  `/tmp/atomic-repair-pg.vA037i/identity_upgrade_old`, original receipt
  `old-receipts.txt`, logical DB `repair_old_receipt_c0bc499`. This ignored-by-default
  two-build witness requires explicit seed/verify environment; a skipped ordinary
  run is not upgrade proof. The same permanent test can repeat future upgrades.
- `declarative_repairs_postgres` passed (15.78s): actual CLI transactor process,
  restricted writer/peer roles, R1/R2/R3, old snapshots/history and restart/exact
  retry. `anonymous_identity` passed all seven cases with PG configured (5.83s).
  Local-callback primitive admission unit test passed; expansion stops at the
  first over-budget primitive instead of retaining all callback output.
- `product_cli` passed both tests (20.74s), including two separate application
  processes, restricted roles and missing-publication recovery. All three
  `remote_transport` tests passed (52.24s), including stored-query preview,
  lost-response retry and replacement/rebinding. Log: `stage1-product.log` in the
  fixture root. Example whole-workflow measurements are diagnostics, not scale
  claims: initial application1665ms, twenty warmed calculations6828us and zero SQL
  calls inside that calculation loop; total application148SQL calls.
- Configured adjacent batch passed26/26 with `--include-ignored`: transaction_phases,
  partitions, partition_authoring, program_transactions, program_tuple_input,
  program_postgres and ref_unique_identity. Sixteen enter PG; the phase test ran
  all six size/queue cases. Four-request wall diagnostics for1/64/256data ops were
  138.6/492.1/2062.6ms unqueued and125.9/585.7/2132.4ms queued; SQLcalls145/184/355.
  These unoptimized concurrent-run samples are not throughput guarantees.
- All-target Clippy completed without errors (existing warnings remain for Stage7).
  Stack-safe negative tasks passed15 focused semantic/resource tests and55 nearby
  query checks. A separate indexed scaling regression then caught repeated memo
  solving in `Or -> required rule -> Not`: work3978/52624/800170 for32/128/512entities,
  despite linear datom reads. This is a repair regression, not an accepted limit.

Final verification: the composed-query regression is repaired by batched demand,
deferred solving and retained positive-memo completeness. At32/128/512entities,
work is1211/4641/18363, datoms105/419/1673 and accounted bytes130701/523003/2094541;
fixed-point iterations remain2. Accounted bytes are not RSS. All15 dependency and
3 independently authored indexed scaling tests pass; final55 nearby query tests
pass (one existing opt-in benchmark ignored). Strata16/64/256/512/513 pass on256KiB
stack without a new depth cap; this is stack-safety proof, not linearCPU in rule
definition count. Final CLI/PG combined check passes8.04s; old-receipt verify
passes0.59s. Latest identity/remote batch passed7+3tests with PG configured.
`cargo fmt --all -- --check`, `git diff --check`, and low-debug/nonincremental
`cargo clippy --offline --all-targets` pass (existing warnings retained for Stage7).

Continuation: return to Goal0; Goal2 is active. Preserve these regressions and
the unchanged durable codecs. Reopen Goal1 if integrated checks reveal a query/
identity regression. The parent product repair is not complete.
