# Goal 4 — Partition Identity and Locality

## Objective and constraints

Complete Stage4 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: named and
implicit partitions, force/match allocation, compatible schema/identity/recovery,
and documented time-ordered UUID helpers. Goal0 owns every invariant and the
remaining product; this is the sole active child. Use local datomic_pro_docs for
semantics and1.0.7705 for algorithms. Preserve working data, old IDs, genesis
hashes, canonical requests, receipts, programs and Goals1–3 repairs. No alternate
stores, JVM/wire parity, recursive goals or silent scope exclusions.

Current native IDs already encode20partition bits and42eidx bits, with a global
issued frontier and indexed entid_at. Missing pieces include the three-partition
whitelist, named-partition metadata and user-only allocation/replay checks.
Reuse those foundations; partitions organize locality, not storage shards or
authorization. Speculation is not an ID reservation; UUID time is not tx order.

## Stages

### 1. Native partition model and compatible information

**Status:** Complete.

**Outcome:** Named/implicit partition identities are validated and represented
through ordinary schema/installation facts without rewriting existing genesis.

**Focus:** Structural ID versus database-aware validation, implicit helpers,
named installation/query/navigation and ordinary exact vocabulary upgrade.
Keep existing global issuance and index ordering; do not add per-partition stores.

**Completion signal:** New/old databases resolve partitions correctly; invalid
IDs/installations fail clearly, retained values/history/recovery preserve exact
facts and old genesis/checksums remain valid.

### 2. Allocation policy and native authoring

**Status:** Complete.

**Outcome:** Transactions assign new related entities using named/implicit
force/match policy, and applications can generate useful time-ordered UUIDs.

**Focus:** Shared eager/native allocation logic, tempid/upsert/component
interactions, native form/program/request encoding and old-byte preservation.
Separate partition policy from domain facts. Define UUID layout, timestamp
precision/range, random bits and clock-regression/order limits explicitly.

**Completion signal:** Pure/speculative/native allocation agrees; invalid or
contradictory policy rejects without moving existing identities. Old requests
retain meaning and new forms survive exact retry/restart. UUID boundary/layout/
roundtrip checks make no global-order guarantee.

### 3. Application/locality acceptance and parent handoff

**Status:** Complete.

**Outcome:** The evolving tenant/customer application uses partitions and UUIDs
through public native APIs with measured locality and durable lifecycle evidence.

**Focus:** Extend reusable application/tests, compare grouped versus mixed index
reads, retain schema/history/speculation/retry checks and exercise PostgreSQL
indexing/recovery. Stage6 repeats the scenario remotely; that is not implemented
here. Record decisions/results in this plan and Goal0.

**Completion signal:** Actual PostgreSQL related-entity and tenant workflows pass
both partition forms/assignments with measured storage/cache work and no universal
speed claim. All preceding signals hold together. Return to Goal0, scaffold or
resume Goal5 and execute; completing this child does not finish the product.

## Decisions and verified results

- Existing20-bit partition/42-bit entity-index layout and one global issued
  frontier are retained. Named partitions use installed low-half DB entities;
  implicit partitions use the high half. This is locality, not authorization or
  physical sharding. Default allocation remains native user4; force/match APIs
  provide custom placement without copying Datomic configuration properties.
- Ordinary partition installation is schema data. Force overrides affinity,
  schema entities remain system-assigned, nested component maps inherit parent
  policy, and identity upserts never move existing EIDs. Temporary affinity
  follows original allocation policy, while lookup/existing affinity follows
  the existing EID. Contradictory directives reject rather than depend on input
  order; anchored policies resolve iteratively with memoized suffixes.
- New request grammar3/submission-hash grammar4 and program ABI8 are used only
  for new partition forms/instructions. Earlier meanings/bytes stay unchanged.
  Existing program/query regression runs passed14 encoding,22 runtime and7
  native query tests; authoring4/4 includes actual PostgreSQL stored invocation.
- `partition_vocabulary_upgrade_ops()` is an explicit exact ordinary transaction
  for old databases. No startup rewrite, migration-checksum edit or redefined
  genesis. The original4051-byte pre-excision and4588-byte pre-partition genesis
  profiles remain hash-checked. Exact upgrade/replay and modified/partial/mixed
  payload rejection pass the pure compatibility test.
- Actual old executable `/tmp/atomic-pre-partition.MKkZlH/atomic` SHA256
  `ae81325a36a131183df02724304027df67b3f9b57bfbc79334be7bb46e02ab6d`
  created and explicitly consolidated isolated schema
  `atomic_partition_upgrade_mkkzlh`, database `legacy-partitions`. The new binary
  then passed `tests/partition_upgrade.rs`1/1 in1.29s: native old-value capture,
  no hidden upgrade, preview, commit, exact retry, retained old information,
  consolidation/restart/recovery and unchanged genesis bytes/hash
  `b99d04f25079c8646fc066975d6d9b952984a68e314d9d8299db62ed511cab75`.
  This fixture is now upgraded; reproduce by provisioning another isolated
  schema with that old executable, not mutating the stored genesis backward.
- Real PostgreSQL release partition suite4/4 passed in2.00s: both forms,
  allocation policies, identity, retained/history, exact retry and injected
  indexing failure with unchanged publication followed by recovery. Tenant
  locality sample8tenants×32entities,32-datom leaves,128-entry peer cache:
  interleaved→grouped fresh native cache16→3leaf reads,32→6SQLcalls,
  19,189→3,660canonical bytes,6,039→1,247SQL cell bytes,6.179→2.439ms.
  Same64returned datoms. Warm98→95µs and0SQL/0leaf in both cases. PostgreSQL/OS
  buffers were not flushed; this aligned workload is not a universal speedup.
- Adversarial checks repaired two gaps: fresh EID and nested Ref values must
  match the exact issued witness, not merely a valid partition plus frontier;
  delayed named installation must retain an ident created in an earlier tx.
  Partition unit3/3 and lineage5/5 pass, including4096-policy-chain stack/work
  behavior and force-to-current-TX issuance. UUID3/3, vocabulary5/5 and existing
  excision8/8 pass. Final all-targets compile passes. Root actual PostgreSQL
  authoring4/4(2.26s) and partition4/4(5.17s) pass after core hardening; costs
  remain16→3leaf/32→6SQL, while debug timings differ from the release sample.
- Evolving application now installs partition/UUID schema at7, named partition
  at8 and tenant+component+UUID facts at9. Restricted-role separate-process
  acceptance passed two application runs around transactor restart and explicit
  missing-index recovery at9. Original schema/seed/update and planning(v1 keys,
  finalbasis6) remain unchanged. UUID intent is retained/recovered on fixture
  retry, not regenerated; this is documented, not claimed as a general outbox.

## Continuation

Complete2026-09-09. Final release actual PostgreSQL4/4 in2.06s includes delayed
installation as eager/speculative/durable data, followed by the full lifecycle.
Latest sample costs remain16→3leaf/32→6SQL, cold5.936→1.823ms and warm96→91µs.
The bounded adversarial review has no remaining confirmed finding. Return to
Goal0; Goal5 is next. Fulltext/distributed/integrated acceptance remain unfinished.
