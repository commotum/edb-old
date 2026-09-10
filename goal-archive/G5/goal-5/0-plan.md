# Goal 5 — Bounded peer reads and indexed query ranges

## Objective and constraints

Complete Goal0 Stage5, repairs R5/R9: pull limits stop avoidable traversal and
selective query predicates use available index bounds without changing results.
Status: **complete** (2026-09-10). Goal0's integrated acceptance is also complete.

Use `datomic_pro_docs` as semantic authority and `1.0.7705` as architectural
evidence. Preserve Goals1–4, immutable supplied views, stored order/canonical bytes,
exact numeric semantics, query dependencies, cursor admission and exact retries.
Keep native Rust/PostgreSQL and existing application/test support. Do not add query
languages, stores, cache features or universal zero-I/O guarantees. Filters may
require additional candidates before a visible-value limit is satisfied.

Starting witnesses: pull limit1 over1000values visited all1000 before truncation;
range predicates enumerate exact-prefix candidates then filter instead of using
AVET bounds. Inspect forward/reverse/wildcard and history/readiness fallback paths.

## Ordered work

### 1. Bound pull consumption and admission

- **Outcome:** Limited navigation consumes only needed visible values, with
  cancellation and resource accounting during traversal rather than afterward.
- **Focus:** R5 forward/reverse/wildcard, nested pulls, defaults/transforms and
  immutable view-aware lazy cursors; avoid eager intermediate collection.
- **Completion signal:** Permanent1000-value limit1 regression plus variations
  pass; independent results agree through filtered/history/since views and reverse
  navigation. Visit/allocation counters include actual consumption and cleanup.
- **Status:** Complete; public eight-test matrix, private control/lookup/native
  admission checks and adjacent navigation/view tests pass, including live PG.

### 2. Push safe range constraints into existing indexes

- **Outcome:** Selective ranges avoid unrelated AVET candidates without losing
  mixed-numeric, boundary, binding-order or supplied-view semantics.
- **Focus:** R9 planner constraints and range cursors, using Goal3 ordering and
  preserving predicate evaluation/errors and unavailable/unready index fallbacks.
- **Completion signal:** Independent forced scans agree for inclusive/exclusive,
  reversed operands, mixed types, conjunctions, history/filters and fallback cases.
  Growing unrelated data does not force full eligible-index enumeration.
- **Status:** Complete; eleven range tests and native boundary regressions
  pass. All six existing comparisons use safe hints; strict bounds and != skip
  equal-value groups, with unavailable/unready index fallbacks preserved.

### 3. Verify complete peer/application costs

- **Outcome:** Both repairs are established on real PostgreSQL and application
  paths with trustworthy read/cost measurements and neighboring regressions.
- **Focus:** High-fanout and selective-range fixtures, cold/warm caches, actual
  datoms/pages/SQL, complete query/pull/result-drop costs and cancellation. Inspect
  overlapping cache recency behavior and record its disposition without silently
  adding optional features. Reuse current fixtures and prior compatibility checks.
- **Completion signal:** Actual PG/application checks and relevant previous query,
  numeric, navigation and view tests pass; scoped varying-size costs, formatting
  and relevant Clippy results are recorded. Fold evidence into Goal0 and proceed
  to Stage6, not a corrective parent.
- **Status:** Complete; final optimized local34 and combined live PG matrix pass,
  with complete foreground driver/cursor costs and bounded limitations below.

## Evidence and continuation

Durable disposable PostgreSQL15.11 is at127.0.0.1:55471, user/database
`atomic_repair`, plaintext, cluster `/tmp/atomic-repair-pg.vA037i/data`.
Use unique fixture schemas and approved host access; preserve Goal1's genuine
old-engine receipt database. Low-debug/nonincremental builds as in Goal0.

Initial evidence (2026-09-10):

- Actual CLI/PG R5 baseline fails in2.85s:128-value limit1 visits128, one cursor,
  twoSQL node reads/23888bytes, one directory/leaf, complete pull/check/drop8978µs.
  Seed/publication947ms is separately excluded. Private cursor edits overlapped
  an attempted rebuild; the witness used the already-built pre-repair binary,
  not an unbuildable intermediate tree. Log: `stage5-pg-before.log` in fixture dir.
- Adjacent decoded-node cache hit recency uses VecDeque::retain under its mutex:
  O(resident entries), bounded by configured entry/byte caps, not O(database size).
  Two permanent private diagnostics pass; lock/get/Arc-drop at32/128/512/2048/4096
  residents takes1.617/4.815/13.193/30.705/60.986µs unoptimized vs0.220–0.499µs peek.
  Fixed128-resident cap stays about2.5–5.0µs as distinct inserted keys grow128→8192;
  actual cache/eviction stats and independent LRU oracle agree. Recency visit count
  is source-inferred, not a new measured SQL counter. This can dominate isolated
  hot-cache lookup at large configured capacity; it is not called free or O(1).
  Disposition: retain the explicitly bounded cache policy for R5/R9; replacement
  would be optional separate work, not necessary to stop database-sized traversal.
  Prepared-query lookup also searches at most64 cached entries; explicit prepared
  queries bypass that lookup. No automatic cache-feature expansion.

Implemented/verified local evidence:

- R5 uses controlled prefix cursors and recovered next-attribute seeks for wildcard
  discovery; lookup-ref resolution no longer collects its whole prefix. Eight
  public tests pass (1.56s): limit1 visits1, wildcard2; two visible filtered values
  visit11 candidates at widths32/128/1000; two nested siblings visit2 edges plus
  2 child values. Default1000/unlimited, transforms/defaults, rejection cancellation,
  reuse and temporal/speculative views pass; history Pull remains rejected.
  Test-only marker ID/optional retraction/temporary borrow errors were corrected,
  not misreported as product defects. Seven private admission tests pass (1.64s),
  including lookup cancellation, overlay skips and incomplete-prefix memo safety.
  Adjacent Pull/entity tests21/21 and DatabaseValue tests19/19 pass.
- R9 direct conjunctive hints preserve the original predicates and resolved probe
  grouping. Private after-prefix positions skip logical numeric/tuple ties without
  invalid sentinel EIDs, public boundary changes or encoding changes. != subtracts
  excluded points into disjoint ranges. Eleven range tests pass; indexed queries
  examine5 raw candidates at128/512/2048 rows; strict-boundary fanout examines3,
  != examines4, using1/2 seeks. Source polls and candidates are charged, including
  rejected views.86 neighboring checks actually execute locally; two PG early
  returns are not counted. Scoped strict kernel tests3/3 plus index/reverse/full-T
  neighbors8/8 pass. Native recent strict seeks examine2datoms, comparisons8/12/15
  and node visits2/3/3 at32/256/1024 ties; durable routing reads one directory/leaf.
- Separate configured PG batch passes22 checks, zero skips, one explicit benchmark
  ignored: declarative CLI1, numeric CLI1, native speculation3, exact sources5,
  extensions2 and runtime sources10. Persisted-query/log cases genuinely execute;
  earlier roles, stored-scale/history, restart and exact retries pass.
- The first combined CLI/PG matrix passed128/1024 but4096-item bulk seed lost its
  response. The retry run measured the endpoint's unchanged30s deadline at30129ms;
  the exact same request recovered its committed receipt (replayed=true) at49189ms.
  No new request identity or deadline relaxation. This is unoptimized bulk setup,
  excluded from peer-read costs, not claimed transaction throughput. The complete
  combined matrix subsequently passed (131.86s), including4096 readiness, retained
  pending-index view, filtered/history reads and separate-process restart/retry.
  At128/1024/4096, forward/reverse each visit1, nested2 and wildcard4 candidates;
  selective range5/work40, strict tie range4/work33 and != tie range5/work36.
  Warm measured operations perform0 SQL reads. Cold application working sets
  normally read2–4 nodes; bootstrap metadata may already cache colocated data.
  Cold node bytes/time vary with leaf/directory packing (nested4096 reads280110
  bytes), not merely returned datoms. Unindexed fallback visits128/1024/4096 and
  work537/4121/16409; necessary scans stay visible. These work counts predate the
  final additional native skip-loop polls and will be reconciled with final runs.
- Initial optimized snapshot passed31 scoped tests (cache2, bias3, private Pull7,
  public Pull8, ranges11);1000-value limit1 visits1 in5µs. Final native merge-loop
  polling changes require a final snapshot before those costs are acceptance.

Final acceptance:

- PeerIndexCursor and RecentCursor now poll during hidden merge, current-group,
  membership and retraction loops; false/error fuses without leaking lookahead.
  Range fences apply only to ordered candidates, not unordered source lookahead.
  The live512-fact/511-retraction witness previously reported4 work; completion now
  charges521, and max_work32 fails on33. A recent -1 still precedes durable511 at
  upper bound0. Eight configured private PG checks pass in2.76s; DatabaseValue19,
  RecentTier18 and35 actually exercised recent-neighborhood tests pass. Three new
  Recent tests cover interruption, fusing and partial-group peak accounting.
- Final optimized snapshot:34 local tests pass (cache2, boundary3, recent-control3,
  private Pull7 with PG test explicitly excluded, public Pull8, range11). Sixteen
  complete warm query/check/drop operations take406/187/190µs at128/512/2048;
  setup15/20/116ms is separate. These are scoped samples, not throughput promises.
  Final release cache lock/get/drop costs80ns→5.425µs at32→4096 residents; fixed128
  cap201–303ns through8192 distinct inserts. Bounded recency policy remains explicit.
- Final optimized combined CLI/PG test passes1/1, zero skips, in22.18s. The same
  128/1024/4096 matrix covers views, pending AVET fallback, cancellation, publication,
  separate-process restart and exact receipts. Indexed ranges examine5 candidates/
  work52 at all sizes; strict ties4/work43; != ties5/work50. Necessary unindexed
  fallback work grows to24605 at4096. Forward/reverse/nested/wildcard visits1/1/2/4.
  All measured warm operations issue0 driver calls. At4096 cold forward uses4
  foreground driver calls,2 cursor-node reads/139800bytes,5733µs including check/drop;
  nested8 calls,4 nodes/280110bytes,10302µs; indexed4 calls,2 nodes/140388bytes,5403µs.
  Pin/health SQL is counted by OperationContext separately from cursor-node stats.
  Cold bootstrap metadata can already cache colocated data. No constant-byte claim.
- Formatting and all-target offline Clippy pass. There are25 distinct warnings,
  not the identical baseline: an old grouping type-complexity warning is removed;
  new256B range iterator enum warning is bounded per probe, not per result/fact.
  Retain it for this repair; focused representation hygiene belongs to Stage7.
  No suppression or silent claim that all warnings predate these changes.

Permanent witnesses: `tests/peer_read_repairs_postgres.rs`, `tests/pull_bounded.rs`,
`tests/query_ranges.rs` and private admission/boundary/cache tests. Optional raw
logs in the fixture directory: `stage5-pg-before.log`, `stage5-pg-retry.log`,
`stage5-pg-release.log`, `stage5-release-cost.log`. No canonical bytes or durable
meaning changed. Continuation: Goal0 and all seven children are complete; preserve
these regressions. Stage7 resolved the per-probe layout warning described above.
