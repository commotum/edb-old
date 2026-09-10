# Goal 3 — Exact numeric operations and bounded comparison

## Objective and constraints

Complete Goal0 Stage3, repairs R4/R11. All supported arbitrary-precision values
must participate in native query arithmetic and numeric aggregates with explicit,
correct promotion/error behavior. Comparison and hashing must not expand compact
decimal exponents or allocate arbitrary-precision integers for routine Long/Ref
comparisons. Status: **complete** (2026-09-10); Goal0 is also complete.

Authority: `/home/jake/Developer/atomic/datomic_pro_docs`; architecture/algorithmic
evidence: `/home/jake/Developer/atomic/1.0.7705`. Preserve Goals1–2 repairs, numeric
logical equality, total index ordering, top-level stored decimal scale distinctions,
tuple semantics, canonical encoding/hashes and exact old request receipts. No JVM
parity, new numeric product types, alternate stores or silent conversion of exact
decimals to f64. Explicit approximate operations/mixed floating inputs need a
documented contract. Resource failure must be clear and leave prepared queries reusable.

Starting code: `query.rs::numeric_function`, numeric `aggregate`, `as_f64` exclude
BigInt/BigDec; `value.rs::Numeric` expands powers of ten and constructs BigInts even
for ordinary Long/Ref comparisons. Existing logical hash consumers must be included.
Reconcile existing division/overflow behavior before changing it; persisted ordering
cannot be altered merely to imitate another runtime. Goal5 depends on this contract.

## Ordered work

### 1. Pin numeric contracts and permanent witnesses

- **Outcome:** Arithmetic/promotion and logical/stored comparison boundaries are
  explicit, backed by docs/source and tests rather than implicit casts.
- **Focus:** BigDec1.25+2.75, big integers, mixed exact/float values, overflow,
  zero division, aggregate result types, scale extremes, NaNs/signed zero, tuples.
- **Completion signal:** Permanent witnesses cover the reproduced omissions and
  comparison/hash consistency, including huge compact exponents without attempting
  enormous allocations. Existing durable/value semantics are captured.
- **Status:** Complete.

### 2. Implement shared exact arithmetic and bounded numeric identity

- **Outcome:** Operations support all numeric value types appropriately; identity
  and ordering cost follows represented information rather than empty exponent span.
- **Focus:** Native shared numeric helpers, checked resource/overflow handling,
  nonexpanding comparison/hash forms, ordinary-number fast paths and query budgets.
  Preserve existing query scheduling and canonical codec paths.
- **Completion signal:** New and adjacent query/value/hash/join/rule tests pass;
  scale-varying and common-number diagnostics include whole operations and cleanup.
  Exact operations never silently round; approximate results are intentional.
- **Status:** Complete.

### 3. Verify native application and durable compatibility

- **Outcome:** Numeric repairs survive actual PostgreSQL commit/reopen/retry,
  retained views, query joins/aggregates and prepared-query reuse.
- **Focus:** Reuse current CLI/application fixtures; independently expected values,
  old/new scale-sensitive facts, canonical hashes/receipts, mixed-source queries;
  measured scoped costs with an honest distinction between counts and elapsed time.
- **Completion signal:** Configured PG actually runs; original witnesses and
  relevant earlier regressions pass; formatting/all-target compilation pass.
  Fold results into Goal0, mark Stage3 complete and execute Stage4.
- **Status:** Complete.

## Evidence and continuation

Contract decision (2026-09-10): the schema reference documents arbitrary-precision
BigInt/BigDec (stored limits8192bits/1024digits), but the current omission is in
query computation, not storage. Recovered `datomic/aggregation.clj` sums with `+`,
returns double avg/stddev, and uses truncating integer median. Keep existing native
Long checked overflow/truncating division. BigInt promotes integral operations;
BigDec with exact operands uses exact decimal operations, terminating division or
an explicit error. Explicit floating inputs select Double approximation with range
checks. Sum preselects the group's domain; decimal median uses an exact midpoint,
integral median retains truncation; avg/variance/stddev are explicitly approximate.
This native decimal-median choice extends the existing float midpoint rather than
copying recovered `quot`'s surprising decimal truncation. No new ratio type.
Query admission must bound actual coefficient/alignment growth, not reject a cheap
compact exponent merely because its numeric value is huge/tiny. Stored precision
limits do not automatically become query result limits.
Statistical intermediates widen where needed: Long median uses an i128 midpoint
and exact-domain variance centers differences before approximation. This corrects
avoidable intermediate overflow/precision loss without changing explicit checked
Long arithmetic or Long sum. Exact means sum/divide before Double conversion so
large terms may cancel; no arbitrary-precision input is cast prematurely.

Admission decision: add native `QueryControl.max_numeric_bytes` (default16MiB) for
temporary coefficient work, separate from the join allowance. Bound growth before
alignment/multiplication, charge existing work and respect cancellation/deadlines;
program queries also constrain it to their remaining shared value budget. This is
an explicit necessary operational control, not a new numeric type/feature. Compact
zero/identity operations remain admissible without scale-sized padding.

R11 design: compare/hash sign, significant decimal digits and an i128 base10
exponent without padding scale-sized zeros. Binary floats are converted exactly
with IEEE-bounded work. Native Long/Ref comparisons and logical hashes get stack/
primitive paths. Preserve existing exact rational ordering and special-value
equivalence; logical join hash bytes may change (process-local only), canonical
transaction/request/value encodings may not. Independent pre-change golden bytes
and public semantic matrix are being captured alongside the shared repair.

Use low-debug/nonincremental builds as in Goal0; don't rebuild all profiles.
Disposable durable PG15.11 remains at127.0.0.1:55471, user/database `atomic_repair`,
cluster `/tmp/atomic-repair-pg.vA037i/data`; `ATOMIC_POSTGRES_TRANSPORT=plaintext`.
Approved host execution is necessary for sockets. Test schemas must remain isolated.
Old-engine receipt witness details are in Goal1; do not overwrite its baseline.

Verified so far (2026-09-10):

- Original live CLI/PG numeric regression reproduced `query/numeric-type` on
  BigDec sum (3.37s); normal stored decimals were already accepted. The permanent
  test also owns prepared arithmetic, compact±100000 scales, scale-only history,
  native root publication and restart/exact retry after the repair.
- R11 shared helper is implemented. `cargo test --offline --lib value:: -- --nocapture`
  passed32/32 (15.91s): five new numeric, eight existing Value and nineteen nearby
  DatabaseValue tests. A232-value independent rational oracle covers about54k pairs;
  211finite binary samples hash like independently expanded exact decimals.
- `numeric_contract`6/6 pass (0.19s).16full pre-change canonical value encodings,
  a634byte transaction (SHA256 `d597bce13f95f90bff95b00b01d7b0d5ec2e9e1d7aeb2f64c5a8fbf2e8706a60`)
  and submission digest remain exact. Independent public ordering/scale/tuple,
  IEEE extreme and raw/input hash-join cases pass. No codec changes.
- Ordinary121000Long/Ref comparisons +11000logical hashes allocated zero bytes.
  For100comparisons+200hashes of3digit decimals, zero allocations at scales
  0/1000/100000/i64MAX−1/i64MIN. With1024digits all scales used the same1600allocations,
  627400requested bytes/2481peak temporary bytes/zero retained bytes. These are
  thread-local allocator-instrumented unit diagnostics, not production RSS/throughput.
- `query_numeric_cost`1/1 passes all15complete prepared-query/check/result-drop
  cases:32/128/512rows ×five scales. Work193/769/3073 and accounted allocation
  bytes34254/137128/549160 are invariant across scales. Unoptimized samples1–10ms
  include checks/drop, exclude caller-owned input construction; optimized checks
  remain before acceptance. R4 implementation and integrated PG retest are pending.

Subsequent verification and remaining integrated gaps:

- Focused release profile (`CARGO_PROFILE_RELEASE_DEBUG=0 CARGO_INCREMENTAL=0`)
  passed all6canonical tests and15complete query-cost cases;32/128/512row samples
  were159–313/404–1093/1314–2620µs across scales, same exact work/byte counts.
  Optimized private numeric5/5 also passed with identical allocation counts;
  complete121000integer compares+11000hashes sampled1.443ms including instrumentation.
  These scoped samples are not end-to-end transactor throughput. Cache total3.3GiB.
- R4 implemented; public arithmetic8/8, existing query semantics9/9 and private
  helper3/3 pass. Helper checks include2975exact division round trips, cheap compact
  identities/negation at scale boundaries, representable result-scale rebalance,
  checked growth and range failures. Query numeric admission is explicit; statistics
  stream/drop exact differences rather than retaining an extra coefficient vector.
- Actual `numeric_repairs_postgres` now passes3.90s (numeric phase2080ms), including
  canonical value-byte comparisons on reopened history and replay. Goal1 combined
  CLI1/1 and query-extension budgets5/5 pass with real PG. All-target Clippy has no
  errors, existing warnings remain. Earlier nested queries, joins, rules, source
  composition and prepared representations pass; the existing opt-in runtime
  measurement is ignored, not claimed as executed.
- Interim finding (resolved by final verification below): stricter outer numeric admission reset in a
  persisted program's ExecuteQuery; propagate it privately through ProgramBudget
  and retain a public nested-program regression. Also public exact-input stddev
  witnesses [-1e308,+1e308] and999zeros+1e309 reproduce premature internal-double
  overflow although final results fit. Normalize exact differences before conversion
  and rescale the final statistic; keep genuine final-domain overflow explicit.

Final verification (2026-09-10): both integrated R4 gaps are repaired. Private
ProgramBudget propagation preserves the outer numeric cap through persisted and
nested queries without changing ProgramControl or its encoding; three public
program-budget regressions pass. Exact statistical differences are normalized
before floating conversion, then rescaled; five public range regressions pass,
including the two reproduced failures, negligible contributions and genuine final
overflow/underflow. Exact zero/one shortcuts preserve the nonzero operand's scale,
not JVM preferred-scale padding; this native calculation contract is documented.

- Final actual PostgreSQL/application batch: canonical contracts6, numeric PG1,
  extension budgets5, arithmetic8 and statistics5 all pass. Numeric PG took3.89s,
  numeric phase2068ms, including index publication, stored-scale history, writer
  restart and canonical-byte exact replay. A host permission-review timeout did
  not start the test; its approved retry ran successfully, not a skipped check.
- Adjacent query batch passes50 tests; one pre-existing opt-in measurement remains
  ignored. Real PG source/nested/log checks ran. Earlier negation/join work bounds
  remain intact. Final optimized batch passes17 tests: arithmetic8/statistics5/
  program budgets3/complete-query cost1 with all15 size/scale cases. Complete
  query/check/drop samples32/128/512rows:143–299/509–1001/2429–4065µs; work and
  accounted bytes remain193/769/3073 and34254/137128/549160 across all exponents.
- Formatting, diff whitespace and all-target Clippy pass;25 distinct warnings
  remain in unchanged code, none in new numeric helpers/tests. No durable codec,
  index-order, request-identity or old-receipt reinterpretation was introduced.

Continuation: Goal3 and Goal0 integrated completion signals are established;
reopen Goal3 only if later integration exposes a numeric gap. Overall product
acceptance still requires Goals4–7. Temporary command logs are under
`/tmp/atomic-repair-pg.vA037i/stage3-*`; permanent regressions own the evidence.
