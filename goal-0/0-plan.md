# Goal 0 — Repair semantic and algorithmic gaps in the native product

## Objective

Repair the twelve findings from the September 9 quality review of the existing
Rust/PostgreSQL Datomic-inspired database. Preserve its working information model,
durable data and native architecture while making query/transaction behavior
correct, ownership stack-safe, and routine work proportional to the information
actually consumed or changed. Finish with integrated application and PostgreSQL
evidence, not merely a collection of completed child goals.

Status: **complete (2026-09-10)**. All twelve repairs and integrated acceptance
are established; all seven children are complete. No child is active. Stage6's
integrated retention/publication reopening is repaired and verified.
Stages 1–7 map directly to `goal-1` through `goal-7`; all seven are scaffolded.

## Authority and constraints

- Semantic authority: `/home/jake/Developer/atomic/datomic_pro_docs`. Architectural
  and algorithmic evidence: `/home/jake/Developer/atomic/1.0.7705`. Recovered source
  contains compiler scaffolding and is not necessarily original idiomatic source.
- Keep native Rust and PostgreSQL: typed APIs, immutable database values/facts,
  schema as data, strong identity, declarative serialized transactions, history,
  peer-local computation, exact reports/retries, and reliable recovery/operations.
  JVM/wire/storage-format parity, other backends, and one-to-one source-file
  translation are not objectives. Do not replace the working engine wholesale.
- Preserve existing repairs, persisted bytes/meaning, ordering, canonical hashes,
  request identity, and retained snapshots. If a repair needs a format or request
  version change, make it explicit and prove old-data/retry compatibility. Do not
  reinterpret acknowledged transactions or silently rewrite historical data.
  Return an acknowledged request's recorded receipt and tempids on retry, rather
  than re-executing it under newly repaired semantics.
- Use idiomatic Rust where it preserves the required semantics: explicit types,
  shared immutable values, bounded iterators/worklists, safe ownership, and small
  coherent interfaces. Do not remove semantics merely to simplify implementation.
- Correctness and complete-path resource behavior outrank stylistic lint cleanup.
  Instrumentation must include statistics, locking, allocation and cleanup where
  relevant, not just the favorable inner algorithm. Cache byte accounts are not RSS;
  driver calls are not network round trips; a single sample is not a scale claim.
- Keep one child active. Parallel bounded work inside it is fine; do not create
  recursive goal hierarchies or corrective parents. Reopen the owning stage when
  integration reveals a gap. Do not silently move a required repair out of scope.
- This scaffold does not certify every recovered function's equivalence. Preserve
  honest distinctions between reproduced defects, static risks, and open questions.

## Starting evidence and repair coverage

The review inspected commit `0296ba4`. Reconcile with the current tree before
implementation. These summaries are sufficient to resume without temporary files.

| ID | Finding and starting evidence | Owning stage |
|---|---|---|
| R1 | Negation inside a rule can consult an unfinished positive memo and permanently admit a false result. `eligible(x) := person(x) AND NOT blocked(x)` returned a blocked entity; equivalent direct negation returned none. `query.rs`; recovered `datalog/eval-not-join`. | 1 |
| R2 | Anonymous maps use public string tempids such as `__map/00000000000000000000`. One explicit map with that ID plus one anonymous map merged into one entity in both eager and exact DatabaseValue paths. `transaction.rs`; recovered `db/expand-submap` and fresh tempid allocation. | 1 |
| R3 | All `Or` clauses are considered ready. Predicate-only branches can run before their binding pattern; swapping equivalent outer clauses changes success into insufficient-binding. `query.rs`; query reference, Or Clause Variables. | 1 |
| R4 | BigInt/BigDec are stored value types but numeric query operations omit them. Projection of 1.25 and 2.75 succeeds; BigDec sum fails. `query.rs`; recovered `aggregation` and documented numeric functions. | 3 |
| R5 | Pull collects all attribute values before checking budgets and truncating; limit=1 over 1,000 values visited all 1,000. Reverse and wildcard paths also need review. `pull.rs`; recovered lazy `pull/limit-iterable`. | 5 |
| R6 | Both transaction assessors recursively traverse stored components during retractEntity. A visited set handles cycles, not deep acyclic chains. Static stack-overflow risk; failing depth unmeasured. `database.rs`, `tiered_assessor.rs`; recovered `builtins/component-es-set` worklist. | 2 |
| R7 | Recent-tier append recounts four complete index trees and log chunks; the service traverses backlog statistics just to read a revision. Existing insertion counters omit these scans. `recent.rs`, `recent_btset.rs`, `service.rs`; recovered `btset` retained counts. | 4 |
| R8 | An unchanged schema returns the same Arc, but transaction validation still scans old/new attributes and tuple definitions. `tiered_assessor.rs`, `schema.rs`; existing wide-schema counters omit this work. | 4 |
| R9 | Query range predicates filter after exact-prefix enumeration instead of using available AVET bounds. `query.rs`, native index-range APIs; query reference documents AVET-aware ranges. | 5 |
| R10 | New fulltext sidecars rebuild historical search data, even after unrelated changes. Memory-bounded external sort is not incremental maintenance. `peer_fulltext.rs`, `fulltext_records.rs`, `fulltext_store.rs`; recovered `fulltext-index/update-fulltext`. | 6 |
| R11 | Numeric comparison/hash construction materializes powers of ten for compact BigDecimals; ordinary Long/Ref comparisons also allocate BigInts. Precision admission does not bound exponent cost. `value.rs`; recovered `common/compare`. Bounded debug self-comparison at scale 100,000 took about 48.5 ms, not a throughput benchmark. | 3 |
| R12 | A predecessor-Arc recent-log chain has recursive destruction on last-owner release, one chunk per 32 transactions. Static stack-overflow risk at large tail depth; no failing-depth measurement. `recent.rs`; Clojure GC behavior cannot be assumed for Rust ownership. | 2 |

Previous verification: formatting passed; Clippy had no errors and 25 distinct
warnings. The host library run reported 343 passes/1 ignored, and focused suites
58 passes/2 ignored. PostgreSQL was unset: early returns are not live PG proof.
The broad integration run was interrupted, not completed. Initial sandbox socket/
ownership failures disappeared outside the sandbox and were not source repairs.
The reviewed eager 1,000-transaction frontier test took 272.94 s and passed; that is
reference-path test cost, not measured native transactor throughput.

Optional starting aids, if still present: `/tmp/atomic-quality-review-2026-09-09.md`,
`/tmp/atomic-review-query.rs`, `/tmp/atomic-review-values.rs`. Move useful witnesses
into ordinary repository regression tests during implementation; do not depend on
`/tmp`, create a new review campaign, or repeat the full module inventory first.

## Ordered stages

### 1. Restore declarative correctness — Goal 1

- **Outcome:** Rule negation, disjunction scheduling and anonymous identities
  produce correct results independent of incidental evaluation/allocation order.
- **Focus:** R1/R2/R3; durable regressions and repairs in shared query/normalization
  paths. Preserve positive recursion, explicit sources, upserts, deterministic
  expansion and previously acknowledged request receipts.
- **Completion signal:** All three counterexamples pass with meaningful nearby
  variations; eager, exact and configured PostgreSQL/application paths agree where
  applicable; existing valid recursive/disjunctive queries remain supported.
  Persisted old requests still retry to their original receipts after restart.
- **Status:** Complete (2026-09-10). R1/R2/R3, old-engine receipt compatibility,
  real CLI/application/TLS restart/retry and adjacent suites pass. Stack-safe batched
  negative completion passed513strata on256KiB stack; indexed Or/rule/Not work at
  32/128/512entities is1211/4641/18363 with two fixed-point iterations per size.
  This is accounted work, not a CPU-throughput claim. Details in Goal1.

### 2. Make graph traversal and ownership stack-safe — Goal 2

- **Outcome:** Deep component retraction and last-owner release of long recent
  tails do not depend on call-stack depth or harm shared snapshots.
- **Focus:** R6/R12; explicit worklists in both assessors, ownership-aware iterative
  release, cycles/shared children/incoming references, cancellation and admission.
- **Completion signal:** Isolated-process small-stack regressions cover deep stored
  graphs built from flat data and long-tail drop/consolidation. Retraction semantics,
  retained values, shared predecessors and bounded failure paths remain correct;
  real PG retraction/restart works. No arbitrary graph-depth cap substitutes for
  stack-safe traversal. Reproduction need not crash the main test runner.
- **Status:** Complete (2026-09-10). Iterative component traversal passes eager
  1024/2048 and exact1024/8192-node isolated256KiB-stack tests; iterative final-owner
  log release passes32768chunks plus sharing/racing owners. Live CLI/PG128/2048-node
  retraction,65transaction shared-tail consolidation and restart/exact retry pass.
  Integrated long-request lease self-expiry is repaired by renewing only the
  continuously validated/locked epoch before fresh/replay commits. Forced-expiry,
  failed-request non-revival and takeover tests pass; default duration unchanged.
  Adjacent live writer/oracle, metadata/recent and Goal1 combined checks pass.

### 3. Complete and bound numeric behavior — Goal 3

- **Outcome:** Supported arbitrary-precision numbers work in arithmetic/aggregates,
  and comparison/hashing avoid unnecessary scale-sized expansion and hot-path
  allocation while preserving established equality and ordering contracts.
- **Focus:** R4/R11; exact numeric promotion, checked overflow/error behavior,
  decimal scale, mixed types, floats/NaNs/signed zero, tuple keys and prepared queries.
- **Completion signal:** Exact BigInt/BigDec arithmetic and aggregate regressions
  pass, including 1.25+2.75. Comparison/hash consistency and stored-scale distinctions
  survive encode/reopen/retry. Bounded varying-scale and common-number measurements
  show the intended cost behavior without enormous allocations. Do not silently
  cast decimals to f64 or change persisted index ordering to gain speed.
  Re-run earlier joins/rules after comparator changes; arithmetic promotion does
  not by itself authorize changing equality, index order or canonical commitments.
- **Status:** Complete (2026-09-10). R4/R11 exact arithmetic and scale-bounded
  comparison/hash pass independent rational/canonical witnesses, actual PG
  scale/history/index/restart/retry,50 adjacent query checks and17 final optimized
  checks. Integrated persisted-query numeric-cap reset and premature statistical
  intermediate overflow are repaired with permanent regressions. Complete query
  work193/769/3073 at32/128/512rows is invariant across five decimal scales through
  i64 extremes; ordinary Long/Ref compare/hash allocates zero. Durable codecs,
  index order and exact receipt identity are unchanged; full evidence in Goal3.

### 4. Restore delta-sized transaction bookkeeping — Goal 4

- **Outcome:** Ordinary small transactions avoid rescanning unchanged schemas,
  accumulated recent trees/logs and service backlogs for bookkeeping.
- **Focus:** R7/R8; maintained immutable aggregates, constant-time revision access,
  validated-schema reuse, and truthful complete-append counters. Build on Goal2's
  recent-log ownership repair rather than reimplementing that structure twice.
- **Completion signal:** Varying tail length, queued work and schema width with fixed
  transaction size shows no avoidable full-tail/schema scans, counting the complete
  path with instrumentation that does not itself scan the measured structures.
  Statistics match independently computed reference totals. Changed schema, tuples,
  predicates, recovery, concurrent indexing/adoption and real PG writes still pass.
- **Status:** Complete (2026-09-10). Maintained recent/backlog totals,
  validated-schema reuse, cached metadata sizes and pending-readiness sharing are
  implemented and verified. Actual PG fixed-delta schema scan76→0 is reproduced/
  repaired; widths38/122/410, tails1/33/129, restart/adoption, programs/tuples and
  earlier CLI repairs pass. The adjacent index gate now measures complete reads
  at1024/4096items:91/91reads,41/41metadata reads,32/32newnodes, with independent
  current/history and failed-publication recovery checks. Read bytes grow with
  directory width; no constant-byte-cost claim. Optimized complete-path samples,
  formatting and all-target Clippy pass; detailed evidence/limits in Goal4.

### 5. Bound peer reads and exploit index ranges — Goal 5

- **Outcome:** Pull limits stop unnecessary traversal, and selective query ranges
  use existing indexes while preserving the supplied immutable view.
- **Focus:** R5/R9; forward/reverse/wildcard Pull, lazy cursors, in-loop cancellation
  and allocation budgets, safe planner range pushdown using Goal3's numeric contract.
- **Completion signal:** The 1,000-value limit=1 witness no longer enumerates all
  unrelated values. High-fanout PG checks report actual yielded datoms/pages/SQL;
  selective indexed ranges stay selective as unrelated data grows. Compare with an
  independent scan for mixed types, boundaries, filters/history and unindexed
  or not-yet-ready AVET fallbacks. Limits apply to visible values, so filtered views
  can legitimately examine extra candidates. Required scans are acceptable when
  accounted for; universal zero-I/O
  or constant-time promises are not the goal.
- **Status:** Complete (2026-09-10). Lazy Pull, all six comparison hints and strict
  prefix seeks pass permanent local and final optimized CLI/PG128/1024/4096 matrix
  (22.18s), including views, pending AVET, restart/exact retries. Pull visits1/1/2/4
  for forward/reverse/nested/wildcard; selective range5 candidates/work52 at all
  sizes. Hidden native/recent skips now poll and charge work:512-fact counterexample
  formerly reported4, now521 and interrupts at max_work32. Final local optimized34,
  configured privatePG8 and adjacent suites pass. Driver calls include pin checks,
  separately from cursor SQL/bytes; growing page widths remain visible. Bounded
  cache-recency cost retained explicitly. Clippy passes with25 warnings, including
  one new bounded per-probe iterator-layout warning for Stage7 hygiene. Goal5 details.

### 6. Maintain fulltext incrementally — Goal 6

- **Outcome:** Unchanged search content is reused and small text changes update
  the affected projection instead of rebuilding all historical text each publication.
- **Focus:** R10; persistent projection reuse/novelty integration, retractions,
  schema changes, supplied-view validation, lag, authenticated publication and GC.
- **Completion signal:** Compare unchanged, non-text-only and fixed text deltas
  against growing corpora; count reads, tokenization, writes, memory and elapsed
  work. Incremental results agree with a full-rebuild oracle through history,
  restart, interrupted publication, excision and reclamation. Keep explicit full
  rebuild/repair for genuine invalidation; authenticate reused projections to their
  retained source and never reuse pages to bypass excision. No Lucene format/ranking
  parity needed.
- **Status:** Complete, including integrated reopening (2026-09-10). Program GC
  now compares exact batch membership while preserving oldest-first public order;
  512+1 aged-program regression and operations15/15 pass. Administrative indexing
  completes an authenticated same-head winner's pending live fold instead of a
  redundant full rebuild; deterministic regression, peer20/20 and background9/9 pass.
  R10 authenticated history diff/shared-page
  maintenance passes optimized primaryPG2/2(27.47s), storage4/4, lifecycle4/4 and
  local16/16.64/256/1024corpora: nontext0input/0pageuploads, fixedtext5inputs and
  21/23/31uploads,50tokenbytes. Complete source/SQL/retention costs remain visible;
  metadata and changed-leaf work are not claimed constant. Full-rebuild oracles,
  noHistory, views, restart/retry, excision, legacyFORMAT1 and restricted-role GC
  pass. Initial empty-corpus loading uses admission-preserving bulk merge, repairing
  a newly caught COW setup regression. Stage7 repaired the five mechanical lint
  sites here and the bounded iterator-layout warning from Stage5.

### 7. Establish integrated acceptance and finish Rust hygiene — Goal 7

- **Outcome:** The repaired product works as one application, with trustworthy
  semantics, durable compatibility, measured costs and maintainable native code.
- **Focus:** All R1–R12 together; reuse `examples/application_workflow.rs`, current
  CLI/test support and relevant operator workflows. Focus refactoring/lints on
  touched responsibilities, not cosmetic file-for-file translation.
- **Completion signal:** Finish the broad supported regression run (or clearly
  diagnose a remaining blocker), formatting and all-target Clippy. Exercise real
  PostgreSQL with non-skipping evidence, separate application/transactor processes,
  remote/local submission, restart/exact retry, snapshots/history, programs,
  concurrent indexing, fulltext, backup/restore and GC/excision where affected.
  Replay saved/generated semantic and lifecycle cases, publish reproducible scoped
  measurements, and reopen owning stages for gaps. All twelve findings are repaired
  or decisively disproved against their original observations; no silent omissions.
- **Status:** Complete (2026-09-10). Initial configured broad run
  finished:954 reported passes/21 failures/6 ignored/7 separately exercised
  filters across139 target summaries. One reported pass early-returned the opt-in
  protocol check, which was subsequently run explicitly. Nine failed targets are
  individually pass after repairs. The final runtime broad run finished973passes/
  4failures; full corrected targets library408/408, backup10/10 and unchanged
  service-worker9/9 subsequently pass, establishing977ordinary cases of supported
  coverage across runs, not a claim that one broad command had zero exit status.
  Real final application/crash/old-receipt and saved24-action/48-step replay checks
  pass. Formatting/all-target Clippy pass with24pre-existing warnings. Evidence,
  failed-fixture history and qualified costs are reconciled in Goal7 and
  `docs/acceptance.md`; no repair or integrated gap remains open.

## Adjacent observations: explicit disposition, not automatic feature expansion

The review also mentioned explicit-ID nested-map restrictions, catalog lifecycle
APIs, cache hit recency scans, report-opening deadlines and large modules. They
are not all established repair requirements: catalog CRUD would add product
features, and separate report-opening deadlines are already documented.

At the relevant stage, inspect overlapping behavior and record a short disposition:
fix when it is necessary for the owning repair, substantiate a remaining limitation,
or request a scope decision for materially new behavior. In particular, measure
cache recency when assessing peer costs, and resolve the docs/source ambiguity
before relaxing nested-map rules. Do not silently promote optional feature work
to a parent completion gate, or use this paragraph to exclude an R1–R12 defect.

Disposition: keep the measured bounded cache-recency policy (Goal5), existing
documented report-opening deadlines and current catalog APIs. Refactor only the
touched responsibilities, not large files for their own sake. Docs transaction
data/Nested Maps requires component or unique attributes; recovered
`db/expand-submap` additionally bypasses child-ID creation when an explicit ID is
present. The native stricter authoring guard follows the literal docs contract;
relaxing that form is optional new input support, not required for anonymous-ID
collision repair. No such relaxation was silently introduced; explicit references
and separate top-level entity maps remain available.

## Execution evidence and continuation

Use disposable PostgreSQL fixtures, never production. Distinguish configured PG
execution from early-return tests, and sandbox failures from product defects. Use
approvals for required host/socket/fixture access; report unavailable authority.
Prefer bounded normal-build diagnostics with `CARGO_PROFILE_DEV_DEBUG=0`,
`CARGO_PROFILE_TEST_DEBUG=0`, `CARGO_INCREMENTAL=0` to avoid the former 63 GiB build
cache. Use a consistent optimized profile for performance comparisons, record
dataset/cache/concurrency/configuration and actual whole-path costs. Do not rebuild
every test variant repeatedly or skip a correctness regression merely because it
is slow; schedule expensive reference checks deliberately.

Completion evidence lives in tests and concise child/parent updates: changed
behavior, commands/fixture conditions, observed results/costs, compatibility
decisions and any outstanding uncertainty. A scaffold, green lint count or completed
child is not Goal0's finish line. Parent completion requires Stage7 plus reconciled
completion signals for all earlier stages.

Continuation (2026-09-10): Goal0 and Goals1–7 are complete. Preserve the repairs,
regressions, immutable data and genuine old-engine witnesses. Future work requires
a new user objective or a reproduced regression; do not restart completed stages
or add optional features automatically. Detailed qualifications, failed-fixture
history and final saved-trace/application evidence are in Goal7 and the acceptance guide.
Real PostgreSQL15.11 fixture
is `/tmp/atomic-repair-pg.vA037i/data`, port55471, with normal durability enabled;
connection/upgrade witness details are in Goal1. Independent TLS55472 and crash55473
fixtures remain available; Goal7 records their exact paths. They are disposable
verification infrastructure, not a deployed production service.
