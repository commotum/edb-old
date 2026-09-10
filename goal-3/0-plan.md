# Goal 3 — Sustainable schema and identity evolution

## Objective and ownership

A long-lived database can add schema and named partitions after ordinary data growth, with safe default placement and documented identity/numeric semantics.

This is Stage 2 of [Goal 0](../goal-0/0-plan.md), owning ST-03, ST-01, SC-01, SC-02.
Status: active; internal Stage 2 (native and durable implementation).
Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

First reproduce ST-03 at both boundaries; separate reserved-schema allocation from ordinary issuance without rewriting existing EIDs, facts or receipts. Define persisted/recovered allocation and explicit/default/match precedence. Validate composite-upsert and NaN candidates before changing behavior; retain superior native semantics when correct.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** A long-lived database can add schema and named partitions after ordinary data growth, with safe default placement and documented identity/numeric semantics.
**Focus:** First reproduce ST-03 at both boundaries; separate reserved-schema allocation from ordinary issuance without rewriting existing EIDs, facts or receipts. Define persisted/recovered allocation and explicit/default/match precedence. Validate composite-upsert and NaN candidates before changing behavior; retain superior native semantics when correct.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Boundary workloads demonstrate automatic schema/partition installation beyond ordinary-data frontiers; restart, speculative/committed agreement, concurrent authority and old exact retries pass. Default placement works for anonymous/nested maps with explicit override. SC-01/SC-02 have source-backed dispositions and regressions, not unsupported parity claims.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: finish ATLC v2 integration through writer, recovery, peer,
COW and backup; verify the retained pre-repair high-frontier database without
reseeding, then run complete application/compatibility acceptance.

## Verified reconciliation and implementation evidence (2026-09-10)

- SC-01/SC-02: tests/schema_identity_contracts.rs passed 3/3, including actual
  PostgreSQL. Composite explicit identity is a pre-derivation upsert hint, matching
  recovered ProcessExpander/getData ordering; constituent-only duplicate derives
  a uniqueness error. Native Float/Double NaN replacement is sound and remains
  stronger than the documented JVM limitation. No unnecessary engine changes.
  docs/schema-identity.md explains both. The PostgreSQL workflow attempted 29
  transactions (including expected atomic failures), then consolidated, reopened
  and recovered in 1.2905s excluding fixture setup/drop; this is not throughput.
- ST-01: additive TransactionDefaults APIs preserve old user-default calls, schema
  and transaction placement, explicit force/match/component/upsert behavior.
  Resolve configured names only after receipt lookup; missing/nonpartition/reserved
  defaults fail fresh requests clearly, but do not reinterpret saved outcomes.
  Four focused tests passed, including actual PostgreSQL restart/config-change
  retries (2.328s complete workflow). Three binary argument tests passed. New CLI
  placement test initially assumed query IDs print as bare Long; fixed to decode
  typed EDN refs. Final CLI rerun is pending; not yet claimed complete.
- ST-03 old behavior: public retract-only tempids issue IDs without live user facts.
  At frontier 524,288 named-partition installation failed
  schema/invalid-partition-install; schema installation succeeded at 1,048,576
  (inclusive), then failed schema/attribute-id-out-of-range at 1,048,577.
  Both eager/native speculative paths agreed. Permanent desired-success boundary
  tests reproduce the failure; the large tests are opt-in and must be run explicitly.
- Pure repaired boundary: 1,047,577 ordinary allocations in 65 batches, both late
  installations succeed, complete 1.445s optimized. Four smaller tests pass for
  independent domains, explicit no-op reservation, typed Ref/tuple Ref witnesses
  and numbers that are not identities. Durable acceptance is still pending.

### Durable allocation decision

Use a private reserved high-water mark, with independent selection in partition
zero while the ordinary frontier remains an upper bound. Never infer occupancy
from current schema alone: receipt-only allocations, retractions/noHistory and
typed reference-only IDs matter. Preserve all prior EIDs and exact receipts.

ATLC v2 authenticates the reserved frontier and low numeric issuance witnesses;
v1 stays decodable/byte-identical. The terminal canonical content is already bound
by exact generation/basis/transaction hash, so it serves as the checkpoint without
changing physical manifest or semantic-state formats. Native immutable endpoints
cache only successful proof results, never share a proof across different bases.
Old native v1 requires a streamed proof once, persisted by the next v2 commit.
Gen0 retains its old allocator until an explicit existing generation conversion;
this compatibility path must be documented and exercised, not hidden.

Migration 31 admits v2 and fences old writers; it does not rewrite existing content.
COW must observe source reservation before filtering and never regress from v2.
For legacy physical excision, protected cutoff facts provide a conservative issued
prefix for erased reference-only IDs. Historical prefixes lacking later cutoff
facts require a conservative guard. Explicit high reserved IDs can still exhaust
the finite reserved range; arbitrary hole recycling is not this repair.

### Retained pre-repair PostgreSQL witness (do not reseed)

- Acceptance database edn_acceptance_20260910 on the already-running private socket
  /tmp/atomic-repair-pg.vA037i port 55471. Only fixture-scoped schemas are modified;
  do not restart the physical server or run broad migration/cleanup.
- Schema allocation_growth_1358380_1789065384168366437, logical database growth,
  lineage e6038c13-1f58-4a16-a676-c3d966014c19, generation 1, basis 65,
  ordinary frontier 1,048,577. Both failures and restart/first exact retry verified.
- Old receipt growth-0000: 16,384 IDs 17592186045416..17592186061799;
  digest 7c078db2a8df8e445e03f8a3fa629e1ba26d29c66e8d14800a080856d04a5b79;
  tx cce0b5eb6afbdf72b693da3b5a9fd911c49a4324da6ff29da331d647184f4ed9;
  state 7f686514ee5869379b9b372b45f5ce1533d71fd2f7cc25339f4274e7aff461ec.
- Complete old durable workload/restart ~156.52s; 8,389,716 canonical payload bytes,
  1,047,577 receipt-tempid rows, 263,856,128 table/index bytes. These are local
  complete-path measurements, not pure-kernel throughput or production sizing.
- Baseline binary /tmp/atomic-allocation-boundary.Fa414o/before-report-fixed,
  SHA256 7756401b6a92993667b97d0ee5c80ffad8f61bf389a0170b03c8f36a0c0fcb43;
  log /tmp/atomic-allocation-boundary.Fa414o/postgres-before-retained.log.

### ST-03 implemented allocation and verified boundary acceptance

- Eager and exact assessment now preobserve explicit partition-zero EIDs,
  attribute IDs and typed Ref/tuple Ref inputs before assigning any tempid.
  Partition-zero allocation has its own retained cursor; ordinary issuance is
  unchanged except that its final frontier remains an upper bound on both
  domains. Typed explicit schema installation retains its prior constraints.
  Four public small regressions passed (0.28s), and four private replay tests
  passed (0.25s), including input-only/receipt-only reservations, ordinary-gap
  rejection, typed installation mixed with both allocation domains, and strict
  v1 versus explicit authenticated v2 replay.
- Compatibility boundary: legacy raw no-op explicit operands were not identity
  allocations and left neither a retained fact nor a named receipt ID. Their
  unknowable external intent cannot be reconstructed retrospectively. The legacy
  proof preserves every durable allocated/reference identity witness; it does
  not reserve the entire ordinary prefix merely because raw input is absent.
  V2 additionally retains these conservative input-only reservations going
  forward. Physically erased previously stored Ref witnesses are different:
  the authenticated excision cutoff guard remains required.
- The retained old fixture above was upgraded **without reseeding** by
  `postgres_upgrade_retained_legacy_growth_without_rewriting_original_receipts`.
  Migration 31 touched only that installation schema. Previously failing
  requests now assign partition 1000 at basis 66 and schema 1001 at basis 67;
  the ordinary frontier stays 1,048,577. Old and both new requests replay their
  exact outcomes after writer/peer reopening. A streamed SHA256 over all 65
  original canonical payloads, transaction/request metadata and 1,047,577 named
  allocations is identical before/after:
  `be2be9586ed0c063af938e327af89598784ac4ba71af50260666cab5719a13b5`.
  This configured test passed 1/1 in 4.004s; the fixture is deliberately retained
  at basis 67, not its original basis 65. Log:
  `/tmp/atomic-allocation-boundary.Fa414o/postgres-upgrade-2.log`.
- A separately created, all-v2 PostgreSQL history passed the full optimized
  `postgres_late_allocation_growth_retains_frontier_and_receipts_across_restart`
  witness: 1,047,577 ordinary allocations in 65 batches of at most 16,384, late
  partition 1000 and schema 1001, 67 total commits, frontier 1,048,577, restart and
  exact first-receipt replay. Actual canonical payload bytes: 8,390,755; receipt
  tempid rows: 1,047,579; receipt table plus indexes: 267,321,344 bytes. The
  service-growth/restart/reporting timer was 179.859s; full test setup/execution/
  cleanup was 194.44s. This ran alongside other acceptance work and is not a
  controlled throughput comparison with the earlier 156.52s old-format run.
  Its disposable schema `allocation_growth_1381263_1789066492754957817` was
  automatically removed; a subsequent catalog lookup returned zero matches.
  Log: `/tmp/atomic-allocation-boundary.Fa414o/postgres-modern-growth.log`.
- Commands used the configured disposable socket database, plaintext transport,
  low-debug/nonincremental builds and `cargo test --offline --release -j4`.
  Both expensive PostgreSQL tests were explicitly selected with
  `--exact --ignored --nocapture`; no ignored test was counted as exercised.
  The no-reseed case also set
  `ATOMIC_ALLOCATION_UPGRADE_SCHEMA=allocation_growth_1358380_1789065384168366437`.
  Tested optimized binary SHA256:
  `cb38cbefeb60dce2c833d758492c584f0932210f4d8f0900b813e42f04d6354f`.
  Before-write fingerprint SQL initially had an ambiguous JOIN; the test-only
  join was corrected before any migration or transaction, then the whole check
  was rerun successfully. Owning allocator files are released; this evidence
  does not by itself close the child’s other integrated compatibility work.
