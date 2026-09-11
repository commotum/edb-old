# Goal 3 — Sustainable schema and identity evolution

## Objective and ownership

A long-lived database can add schema and named partitions after ordinary data growth, with safe default placement and documented identity/numeric semantics.

This is Stage 2 of [Goal 0](../goal-0/0-plan.md), owning ST-03, ST-01, SC-01, SC-02.
Status: complete; all internal stages and integrated compatibility accepted.
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
Current next action: return to Goal 0 and execute Goal 4. Reopen this child only
for an integrated gap it owns; preserve its retained fixtures and completed work.

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
  typed EDN refs. The corrected CLI suite passed 3/3; its default-placement,
  preview, explicit-force and config-change/exact-retry workflow took 7.137s.
- ST-03 old behavior: public retract-only tempids issue IDs without live user facts.
  At frontier 524,288 named-partition installation failed
  schema/invalid-partition-install; schema installation succeeded at 1,048,576
  (inclusive), then failed schema/attribute-id-out-of-range at 1,048,577.
  Both eager/native speculative paths agreed. Permanent desired-success boundary
  tests reproduce the failure; the large tests are opt-in and must be run explicitly.
- Pure repaired boundary: 1,047,577 ordinary allocations in 65 batches, both late
  installations succeed, complete 1.445s optimized. Four smaller tests pass for
  independent domains, explicit no-op reservation, typed Ref/tuple Ref witnesses
  and numbers that are not identities. Durable boundary results follow below;
  broader compatibility acceptance remains required.

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

### Compatibility acceptance in progress

- Retained identity-repair receipts were checked without migrating their shared
  source installation (253 logical databases). The pre-31 Rust library backed up
  only `repair_old_receipt_c0bc499`, generation 1 / basis 15, from physical
  `atomic_repair` / `public` at schema 30 (71 objects, 5.301s). Current code
  restored it into isolated schema `identity_compat_xbpxsl` in the acceptance
  database (4.495s), then the explicitly selected ignored `identity_upgrade`
  verification passed 1/1 (0.76s). Both old map/program collision receipts remain
  exact; two new requests retain the repaired behavior. Source rows and schema
  remain unchanged. Source canonical/request/26-tempid fingerprint:
  `3b284cb2d086f16283c6e55f2f96a8ace24d309429b664717ec816c5cfe86d10`.
  Original receipt-file SHA256:
  `e625cb49f32df376c9cf1e5135582347ad502de21dd7ff713f52f210dd123c32`.
  Evidence and backup: `/tmp/atomic-identity-isolation.XbpXSl`; optimized test
  binary SHA256 `cdbbb1a63ab519ae1aae756d9b013952be57b76103b6c6236511bf41401072a1`.
- Exact allocation-state peer tests passed 2/2 on PostgreSQL (32.05s), including
  native cached advancement/reopen, unchanged-basis physical refresh, independent
  successor metadata, zero-SQL warm reads, retryable proof errors, and genuine
  populated-v11 generation-zero read/recovery preserving original payloads.
- A first broad parallel backup run exceeded the fixture server's 80-connection
  cap: two backup tests failed with PostgreSQL 53300. That run is not acceptance.
  The broad public/application suite is being rerun serially; server settings
  were not changed and no server restart was used.
- The genuine gen0 backup/restore regression found an existing integration gap:
  restore created historical semantic coordinates only for native request-base
  receipts, not older ordinary receipts. The latter then failed exact db-before
  reopening (`postgres/request-before-root-missing`). This is an owning-child
  blocker being repaired, not an unsupported-legacy exclusion. Initial fixture
  attempts to write gen0 with the current writer were invalid setup and replaced
  with historical v11 seeding before migration.
- Clippy completed after removing three warnings introduced by this stage;
  the existing unrelated warnings remain. Historical program-reference migration
  tests also need genuine v1 fixture data rather than pretending current v2
  content was produced under schema 23. Neither pending check is a pass yet.

Further verified checks and remaining integration work:

- The serial run passed backup-copy boundary 2/2 (55.05s), backup/restore 10/10
  (719.90s), semantic backup integrity 4/4 (75.29s), identity repairs 5/5,
  operations/excision 1/1, partition authoring 4/4, partition behavior/locality
  4/4 and runtime-role/schema migration boundaries 2/2. These binaries preceded
  the additional legacy-receipt checkpoint repair described below. The previous
  connection-limit failures did not recur. Read-only diagnostics observed
  DataFileImmediateSync during isolated migration setup; no durability settings
  were weakened. The one-time old-partition test lacked its separate environment
  and was skipped, not passed; a fresh genuine-old-binary witness is being run.
- The application command failed because its separately built example executable
  was still pre-schema-31. This was test setup, not a reason to relax the schema
  fence. Both `--bin atomic --example application_workflow` were rebuilt with
  `cargo build --offline --release -j4` (56.66s); application and remaining public
  read/schema checks are being rerun. Do not count the earlier application as a pass.
- Historical program-reference coverage now uses a genuine pre-31 native Rust
  portable backup, stored as a small reviewable hex test fixture. Current-v2 tests
  remain; only historical schema downgrade cases restore authentic v1 content.
  Removing migration 31 first rejects non-v1 content, then removes its actual DDL;
  canonical payloads/hashes are never rewritten to fabricate history. The target
  passed 9/9 (eight actual PostgreSQL tests and pure fixture authentication), with
  one explicitly ignored fixture-regeneration tool, in 167.34s. It verifies
  schema23-to-31 checksums, canonical fingerprints, program references, corruption
  rejection, GC resume and paused restores. Log:
  `/tmp/atomic-program-reference-fixture.USlj4V/migration-upgrade-current.log`;
  tested binary SHA256 `54cb90f154c004ddbfd1e268857680c3f6237b85e981115683b1ab8933282012`.
- Legacy restore needs historical physical indexes as well as semantic roots.
  The repair reuses incremental tree/AVET construction and generation-owned
  completed archives, without changing old request kinds or canonical receipts.
  Checkpoints cap receipt tails at 256 transactions, 4 MiB accounted bytes and
  16,384 datoms (one historical transaction is indivisible). Scratch ownership
  uses existing durable intents and at most 16 pins on one dedicated session;
  complete archives own descendants before those pins are released. Admin retry
  must authenticate archive structure and semantic content before claiming a
  prior restore complete. Full backup verification still uses its existing eager
  history representation; this is not a claim that whole restore RSS is bounded.
  The expanded PostgreSQL/GC/corruption/restart regression remains pending.

Latest acceptance reconciliation:

- After rebuilding the CLI and application example together, `product_cli`
  passed 2/2 (27.82s): restricted roles, two writer restarts, two application
  processes, exact repeated transactions and diagnosed/recovered missing physical
  publication. READ_VALUES_OK ran both rounds; warm queries issued zero SQL.
  The stale-example failure above is resolved, not counted as a successful run.
- Remaining serialized public tests passed: read entity identity 2/2, index
  authoring 5/5, captured invocation 4/4, mixed-source sequences 7/7, schema
  information repairs 8/8, temporal schema repairs 2/2 and successor schema 3/3.
  Complete sequence preparation/consumption/drop samples for 32/128/512 rows
  were 0.275/0.987/2.172ms when consuming all rows; these are local samples, not
  a general scalability certificate. Ten thousand entity equality/hash/token
  iterations took 1.448ms with zero SQL.
- The previously skipped pre-partition case was explicitly exercised using the
  genuine old executable `/tmp/atomic-pre-partition.MKkZlH/atomic`, SHA256
  `ae81325a36a131183df02724304027df67b3f9b57bfbc79334be7bb46e02ab6d`.
  It created a fresh schema-26 fixture, then current migration 31 and
  `partition_upgrade` passed 1/1 (1.74s), preserving original genesis, exact
  retry, old values and recovery. Genesis SHA256:
  `b99d04f25079c8646fc066975d6d9b952984a68e314d9d8299db62ed511cab75`.
  Only the newly owned `partition_goal3_egwbab` schema was removed afterward;
  catalog verification found zero matches. Original fixtures were untouched.
  Log: `/tmp/atomic-partition-goal3.egWBAb/upgrade.log`.
- These public binaries preceded the final legacy receipt-archive proof changes.
  Run the expanded legacy witness and relevant changed-path backup regressions
  on the final build; retain the distinction between snapshots of test evidence.
- After the archive matcher/shared semantic proof refactor, the optimized public
  semantic-integrity target passed 4/4 (23.55s), and copy/retry boundaries passed
  2/2 (9.03s). This includes hash-valid forged indexes, earlier request-base
  semantics, pending AVET, noHistory and missing/corrupt unchanged-copy objects.
  Normal-library Clippy passed (11.84s; 15 existing unrelated warnings). It first
  caught a call to a test-only hash convenience method; production code now uses
  the identical SHA256 of encoded content directly. A further narrow strict-v2
  freshness check is being shared with recovery before final acceptance.
- The latest ordinary native receipt backup → restore → backup → restore
  regression passed 1/1 (239.25s), including exact replay with fewer index reads
  than the complete archived tree. Its deliberately one-datom segments make this
  a compatibility/resource-boundary witness, not a throughput benchmark.
- The final matcher-generation-fence build passed the isolated restore
  failure/ambiguous-commit retry regression 1/1 (21.58s). Transaction interruption,
  partial staging and retry remain atomic. Subsequent malformed-v2 admission
  guards have their own pure negative/valid-upsert regressions; they add no
  archive I/O or publication behavior.

### Final acceptance and handoff

All three internal stages are complete. The chronological pending notes above
record intermediate states, not current blockers.

- Genuine v11 generation-zero upgrade passed 1/1 with no skips (242.46s total,
  including observed host filesystem journal waits). It exercised 515 historical
  updates; interrupted same-target restore and retry; 20 zero-age GC probes;
  missing/corrupt archive rejection and repaired retry; explicit administrative
  consolidation for the log-only backup; fresh reserved schema ID 1001 and v2
  commit; reopening and exact first/middle/last receipts. Existing canonical
  payloads, identities and before/after information remain intact.
- Four historical checkpoints used ten incremental builder steps, including six
  AVET projection steps, 102 node outputs and 741,544 encoded bytes; maximum one
  upload was 24 nodes / 211,478 bytes. Interrupted restore plus successful retry
  took 13.931s with 39,508 attributed driver calls and 17,556,625 returned cell
  bytes. This is the complete measured phase, not just the favorable builder.
  Full administrative verification still materializes history as documented.
- Reopened exact receipt checks had bounded before/after tails of 1/2, 2/3 and
  0/1 transactions. Complete read/check/drop samples were 44.008, 37.789 and
  32.673ms, with 56, 56 and 54 attributed driver calls. These are local workload
  samples, not production throughput claims.
- The complete PostgreSQL witness used optimized binary SHA256
  `d6899ef8a7a804fc040237da3d0508a228632ab6651446d2e31903417fb794fb`.
  The final source adds only the malformed reserved-receipt upper-bound guard to
  that behavior; it changes no valid receipt, codec, I/O or publication path.
  Final optimized SHA256
  `08ddc9de2192a783740b558fc7aa0b4e9cab61a9be251e8ed11ae07787174f1b`
  passed 26 focused tests: allocation/receipt guards 3, portable receipt positive
  1, reserved allocation 7, canonical lineage log 11 and COW 4. The earlier final
  proof snapshot also passed shared tree semantics 2 and restore builder 2.
  This explicit artifact split avoids claiming an unrun full-suite snapshot.
- Final normal-library Clippy passed in 11.11s with 15 existing unrelated
  warnings and no new diagnostics; `git diff --check` passed. No PostgreSQL
  durability setting, original historical fixture or existing canonical bytes
  were weakened/replaced to obtain acceptance.

Continuation: Goal 4 is the next child. This child is complete; Goal 0 is not.
