# Goal 3 — Exact numeric operations and bounded comparison

## Objective and constraints

Complete Goal0 Stage3, repairs R4/R11. All supported arbitrary-precision values
must participate in native query arithmetic and numeric aggregates with explicit,
correct promotion/error behavior. Comparison and hashing must not expand compact
decimal exponents or allocate arbitrary-precision integers for routine Long/Ref
comparisons. Status: **in progress**, Goal0's only active child.

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
- **Status:** In progress.

### 2. Implement shared exact arithmetic and bounded numeric identity

- **Outcome:** Operations support all numeric value types appropriately; identity
  and ordering cost follows represented information rather than empty exponent span.
- **Focus:** Native shared numeric helpers, checked resource/overflow handling,
  nonexpanding comparison/hash forms, ordinary-number fast paths and query budgets.
  Preserve existing query scheduling and canonical codec paths.
- **Completion signal:** New and adjacent query/value/hash/join/rule tests pass;
  scale-varying and common-number diagnostics include whole operations and cleanup.
  Exact operations never silently round; approximate results are intentional.
- **Status:** Not started.

### 3. Verify native application and durable compatibility

- **Outcome:** Numeric repairs survive actual PostgreSQL commit/reopen/retry,
  retained views, query joins/aggregates and prepared-query reuse.
- **Focus:** Reuse current CLI/application fixtures; independently expected values,
  old/new scale-sensitive facts, canonical hashes/receipts, mixed-source queries;
  measured scoped costs with an honest distinction between counts and elapsed time.
- **Completion signal:** Configured PG actually runs; original witnesses and
  relevant earlier regressions pass; formatting/all-target compilation pass.
  Fold results into Goal0, mark Stage3 complete and execute Stage4.
- **Status:** Not started.

## Evidence and continuation

Use low-debug/nonincremental builds as in Goal0; don't rebuild all profiles.
Disposable durable PG15.11 remains at127.0.0.1:55471, user/database `atomic_repair`,
cluster `/tmp/atomic-repair-pg.vA037i/data`; `ATOMIC_POSTGRES_TRANSPORT=plaintext`.
Approved host execution is necessary for sockets. Test schemas must remain isolated.
Old-engine receipt witness details are in Goal1; do not overwrite its baseline.

Continuation: inspect numeric semantics/source and implement R4/R11 with disjoint
ownership for arithmetic and comparison. Root owns integration, compatibility and
parent reconciliation. No grandchildren; on completion execute Goal0 Stage4.
