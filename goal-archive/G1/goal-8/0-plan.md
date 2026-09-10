# Goal 8 — Lifecycle and Operational Completion

## Objective

Make the Rust/PostgreSQL database operable across protection, recovery,
diagnosis, bounded growth, privacy erasure, and software evolution without
weakening its immutable-value, history, serial transaction, and peer semantics.
Use PostgreSQL's strengths directly and provide executable verification plus
concise operator procedures rather than a generic operations framework.

This realizes the final Stage 8 of `goal-0/0-plan.md` and owns the integrated
Goal 0 acceptance audit.

## Constraints

- PostgreSQL remains the only database storage and Rust the implementation
  language. Prefer PostgreSQL-native consistent snapshot/PITR tools where they
  are the more reliable operational primitive.
- Preserve live consistent backup, selectable point-in-time restore, deep
  verification, immutable peer reads, synchronous schema safety, explicit
  progress, and permanent audit evidence of privacy excision.
- Treat `datomic_pro_docs` backup/restore, monitoring/capacity, caching,
  schema/index evolution, GC, excision, security and deployment guidance as
  semantic/operational authority. Trace `1.0.7705` backup, integrity, monitor,
  garbage, excise, index and lifecycle control flow before fixing behavior.
- Backup is protection outside the live database failure domain. A catalog row
  in the same PostgreSQL cluster is not a backup.
- Restore never overwrites a live system silently. It targets an explicitly
  quiescent/empty destination, verifies identity/checksums/chain before
  publication, and is rehearsed rather than merely documented.
- Excision is exceptional, privileged, auditable, irreversible without a
  pre-excision backup, and removes matching information from authoritative log
  and all derived indexes/caches. It is not ordinary correction.
- GC may delete only provably unreachable derived immutable values after a
  safety horizon. Authoritative transaction history remains until an explicit
  excision/retention policy owns its removal.
- Metrics and diagnostics are bounded snapshots/counters with stable names;
  exporters are application concerns. Do not add a monitoring server or cloud
  vendor SDK.
- Schema and format migration remain checksum/version guarded, restartable, and
  downgrade policy is explicit. No speculative multi-backend compatibility.

## Known context

- Goals 1–7 establish canonical checked formats, immutable chained history,
  real restart recovery, content-addressed peer indexes/programs, bounded
  caches/query/program/service work, migration checksums, corruption failures,
  and fenced HA.
- PostgreSQL physical backup/PITR can protect the whole system consistently;
  per-database portable backup additionally needs bootstrap, transaction and
  request rows plus canonical chain verification.
- Derived index manifests are transaction-anchored but disposable; peers fall
  back to the authoritative log on absence/corruption. This makes them the
  first safe GC target and allows excision to invalidate/rebuild them.
- The authoritative log currently rejects all mutation. Privacy excision will
  require one narrowly scoped privileged rewrite that rehashes the entire
  surviving chain atomically, updates references, invalidates derived data,
  and records what predicate was applied without retaining erased values.

## Stages

### 1. Operational contract and source map

**Status:** Complete. `OPERATIONS_CONTRACT.md` classifies authoritative,
configuration, coordination and derived state; maps recovered backup,
integrity, statistics/monitor, garbage and excise intent; fixes physical versus
portable RPO/RTO, restore quiescence, inspection/metrics/limit boundaries,
reachability GC, migration policy, privileged excision rewrite/cache rules,
credentials and retirement. It explicitly states that already exported copies
cannot be erased by the live system.

**Outcome:** Recovery, consistency, metrics, capacity, GC, excision, migration,
security and retirement guarantees are explicit with recovered/native mapping.

**Completion signal:** A concise contract distinguishes authoritative from
derived state, declares RPO/RTO and quiescence assumptions, defines protected
data and destructive authority, and names every executable/operator boundary.

### 2. Integrity inspection, metrics, and capacity controls

**Status:** Complete. `PostgresOperator::inspect_database` crosschecks basis,
contiguous predecessor/content hashes, canonical envelopes, request references,
kernel replay, manifest anchors, segment presence/hash/order and emits stable
counts/bytes/current/history/index-lag/program/lease/orphan metrics.
`CapacityLimits` bounds operation count, canonical transaction bytes and
history length before publication while allowing prior idempotent outcomes to
resolve. Real PostgreSQL fixtures prove healthy deep inspection, raw corruption
detection/recovery, metrics and every capacity rejection remaining invisible.

**Outcome:** Operators can inspect every database/chain/derived root, obtain
stable bounded metrics, and enforce declared limits before exhaustion.

**Completion signal:** Rust APIs and PostgreSQL tests detect missing/forked/
corrupt history and bad derived references, report basis/bytes/counts/cache/
queue/lease health, and reject configured transaction/database growth limits.

### 3. Backup, point-in-time restore, and verification

**Status:** Complete. `PortableBackup` takes PostgreSQL repeatable-read live
snapshots into an externally claimed directory, reuses content-addressed
bootstrap/transaction/program objects, writes checksummed versioned basis
manifests, lists points, deeply decodes/replays all content, and performs a
privileged atomic restore only into an absent target. Real tests back up basis
1 then basis 2 with reuse, restore both points with identical current/history
and active program state, reject target overwrite, corrupt an external object
and fail closed, then repair and reverify. PostgreSQL physical/WAL PITR remains
the whole-cluster layer documented in the final runbook.

**Outcome:** Consistent external backups can be listed, deeply verified, and
restored to an explicit basis into a safe destination with identical state.

**Completion signal:** A live-backup rehearsal, incremental/repeat behavior,
corruption test, and restore to latest and earlier basis prove identical
canonical database/history; PostgreSQL physical/PITR runbook and tested command
preconditions are recorded.

### 4. Derived-value garbage collection and evolution

**Status:** Complete. `PostgresOperator` exposes separate inventory and apply
operations with a PostgreSQL server-time age horizon. It decodes every
manifest to compute exact global segment reachability, locks manifest and
program-version publication while selecting/deleting, and rechecks program
reachability in SQL. Schema/index evolution remains forward-only through
transactional, advisory-locked, checksum-verified migrations and disposable
derived manifests. Real PostgreSQL tests retain an immutable peer snapshot
while reclaiming aged orphan segment/program content and race GC against a
consolidator; the published manifest remains complete and peer reads recover.

**Outcome:** Unreferenced index/program artifacts can be inventoried and safely
reclaimed without affecting current/history reads or concurrent publication.

**Completion signal:** Dry-run and apply modes use an age horizon and SQL locks;
concurrent index/deploy races retain reachable values, cache misses recover,
and schema/index/migration progress is inspectable and restart-safe.

### 5. Audited privacy excision and cache invalidation

**Status:** Complete. Migration 5 adds an immutable non-value audit and a
monotonic per-database excision generation. The privileged Rust operator
requires a deep verified backup of the locked current head, implements the
recovered entity/selected-attribute/attribute predicate with exclusive
`before_t`, recursive component and inbound-ref behavior, protects bootstrap
and transaction-instant facts, and atomically rewrites/re-hashes the complete
log plus requests/head while dropping derived manifests and immediately
deleting every now-unreferenced segment under a publication-conflicting lock. A connected peer
observes the generation and rebuilds before returning from sync. PostgreSQL
tests prove an injected post-rewrite failure rolls everything back, a raw byte
scan cannot find erased values, old external `Arc` snapshots remain honestly
unerasable, restart recovery is exact, the audit remains, and replay is
idempotent.

**Outcome:** Privileged entity/attribute/time predicates permanently remove
selected datoms from authoritative history, rebuild chain identity atomically,
invalidate all derived copies, and preserve a non-sensitive excision audit.

**Completion signal:** Mandatory verified-backup acknowledgement, protected
schema rejection, recursive component/inbound-ref behavior, before-t limits,
interrupted rewrite rollback, peer/cache invalidation, restart recovery, and
raw PostgreSQL scans prove erased values are absent while the audit remains.

### 6. Runbooks, compatibility, security, and integrated acceptance

**Status:** Complete. `RUNBOOK.md` gives executable deployment/migration,
failover, PostgreSQL physical/WAL PITR, portable backup/restore, diagnosis,
capacity, GC, excision, incident and retirement procedures.
`COMPATIBILITY_AND_SECURITY.md` fixes format/migration support, role, secret,
execution and destructive-authority policy. `INTEGRATED_ACCEPTANCE.md` audits
every Goal 0 guarantee and deliberate native boundary. PostgreSQL 15.11
accepted and checksum-verified a streamed physical backup, booted the copied
cluster through recovery, and exposed its Atomic migration/catalog state.
The final strict gate passes format, warning-denying clippy, and the serial
all-target suite: 102 tests pass, with only the deliberately ignored crash
worker entry invoked by its passing parent; two real server restarts and the
client-process abort path execute.

**Outcome:** Operators have concise procedures for deploy/upgrade/failover,
backup/restore, diagnosis, capacity, GC, excision, incident response, and safe
retirement, with an explicit compatibility/security policy.

**Completion signal:** Rehearsals and the complete format/lint/unit/integration/
restart/fault/privacy suite pass in a production-shaped PostgreSQL deployment;
the Goal 0 success condition is audited from behavior rather than stage labels.

## Operations exit condition

Goal 8 is complete when a PostgreSQL-only deployment can be protected,
restored, verified, observed, capacity-bounded, garbage-collected, upgraded,
privacy-excised, diagnosed and retired using tested Rust boundaries and concise
runbooks, and the full Goal 0 system retains its semantic, concurrency,
durability and availability guarantees under the integrated acceptance suite.

**Status:** Achieved. All six operational stages have executable evidence; the
integrated audit found and closed derived segment retention during excision,
and identifies no remaining Goal 0 success-condition gap.
