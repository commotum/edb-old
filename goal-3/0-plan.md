# Goal 3 — Finish the recovered Datomic system

## Objective

Finish the educational recovery and explanation of Datomic Pro 1.0.7277 using
bounded semantic equivalence: a recovered Peer and complete recovered
Transactor that work together through the PostgreSQL-backed write, log, index,
transport, acknowledgement, and HA paths without original implementation
fallback. Resolve every Peer/Transactor overlap before lower-priority lifecycle,
optional-interface, or alternative-backend work is allowed to obscure the core.

Goal 2 and its retained evidence are historical inputs. This file is the
authoritative policy, status, and next-action record for Goal 3.

## Acceptance policy

Global exact-AOT equivalence is not the normal recovery boundary. The normal
boundary is **bounded semantic equivalence**, established in this order:

1. **Ownership and isolation:** candidate runtimes contain no original Peer or
   Transactor implementation classes.
2. **Surface:** namespaces, Vars, callable arities, protocols, class roles,
   methods, and fields agree where applicable.
3. **Local structure:** namespace/class-role/method bodies agree under only
   conservative, evidence-backed normalization.
4. **JVM validity:** recovered classes load and pass JVM verification.
5. **Critical behavior:** focused PostgreSQL, transaction, log, index,
   transport, acknowledgement, and HA gates pass.
6. **Escalation:** exact-AOT investigation is allowed only for a specific
   unexplained executable difference, ABI/surface mismatch, differential
   behavior failure, or explicit user request. It is never a global
   prerequisite.

Generated numeric IDs, debug metadata, static Var constants, capture clearing,
Clojure-version lowering, and verifier scaffolding may be normalized only under
guarded predicates that have positive and negative evidence. Calls, constants,
arguments, branches, reads/writes, side effects, exception behavior, locking,
and transaction boundaries remain semantic.

Every overlap has one of four statuses:

- `RESOLVED_EQUIVALENT`: no unexplained difference remains in the claimed
  domain.
- `RESOLVED_DIVERGENT`: a real artifact difference is understood, bounded, and
  intentionally preserved.
- `BOUNDED_PARTIAL`: supported behavior is proved, but identified dormant
  behavior or a required evidence rung remains uncovered.
- `OPEN`: an unexplained executable difference remains.

Only the two `RESOLVED_*` states count toward the 117-row completion boundary.

## Constraints

- Work in `/home/jake/Developer/atomic/datomic-rev`. Keep
  `/home/jake/Developer/atomic/goal-1` untouched and uninspected.
- Preserve the recovered Peer as the reference boundary and recover the whole
  Transactor, including all 117 namespace-name overlaps.
- Keep licensed originals isolated as fingerprinted evidence or behavioral
  oracles; never admit their implementation classes into candidate runtimes.
- PostgreSQL is the primary durable backend. Defer optional interfaces and
  alternative stores until the authoritative transaction and HA paths close.
- Repair generic causes from source, bytecode, and behavior evidence. Add one
  focused regression for each confirmed defect.
- Keep one executable boundary active. A rerun requires a named falsified
  assertion or confirmed defect; after one evidence-backed correction and one
  rerun, bank the result or record the localized blocker before expanding.
- Preserve unrelated worktree changes, bound owned processes, and require
  cleanup and durability evidence before reporting PASS.

## Actual state

- **Stage 1 bounded recovery is complete.** The complete 247-source Transactor
  corpus is deterministic; 272/272 effective namespaces load, and all 247
  callable/root/class shapes agree. The production exact-AOT attempt at
  `/tmp/datomic-goal3-stage1-production-v4` failed in the normalized comparator
  after both 3,431-class candidate trees compiled; scanner and verifier were
  `NOT_RUN`. The bounded v122 diagnostic stopped with 1,051 ambiguous nodes in
  927 groups at search index 45 after its 1,000-node limit. This is preserved
  negative diagnostic evidence, not a Stage 1 failure and not an exact-AOT
  PASS.
- **Overlap ledger:** 13/117 are fully resolved; 104 are incomplete. Of those
  incomplete rows, nine are `BOUNDED_PARTIAL` and 95 are `OPEN`. The nine-row
  CFG result proves reachable parity between the original Peer and original
  Transactor artifacts, but it does not compare recovered candidate methods;
  therefore it is useful evidence, not a completed recovery classification.
- The parked ownership-corrected classifier reports 116/117 exact class-role
  rows, 102/117 exact method-ABI rows, 43 exact-field rows, and 69 rows whose
  field delta is generated static Vars only. These are triage facts, not
  promotions. The classifier must not be rerun or expanded while the selected
  HA boundary remains unfinished.
- The recovered PostgreSQL path proves catalog/database identity, value and
  revision persistence, log/index roots, coordination, publication, transaction
  rejection, concurrent monotonic ordering, pre-publication failure, and the
  post-publication/pre-result acknowledgement cut.
- **The corrected HA v8 boundary is banked PASS** at
  `/tmp/datomic-recovered-pair-ha-inflight-v8`. Standby startup replay adopted
  the in-flight transaction exactly once at t=1003; the original Future failed
  unavailable; the same Peer committed a new follow-up through promoted B at
  t=1005; a fresh Peer matched basis, canonical state, and SQL log-root revision
  6; stale A self-fenced; and all processes, sessions, and ports were cleaned up.
  The 101-entry evidence manifest verifies, and its file SHA-256 is
  `5243885f1c0c85dbe2967171856258ad7f7665fd38391bd72444b4c72c1a2887`.

## Stages

### Stage 1 — Seal bounded source recovery

**Status:** Complete at the bounded corpus/structural boundary

The deterministic recovered corpus, ownership/isolation boundary, complete load
surface, callable/class surface, JVM-valid exercised runtime, and focused
regressions satisfy normal acceptance. Exact-AOT remains a failed, nonblocking
diagnostic and may be reopened only by an escalation trigger in this plan.

### Stage 2 — Reconcile all 117 overlaps

**Status:** Active — 13 resolved, 9 bounded partial, 95 open

Classify by subsystem and by the evidence ladder, using runtime failures to pull
high-value rows forward. Static original-artifact parity alone cannot close a
recovered-candidate row. `BOUNDED_PARTIAL` never counts as resolved. Preserve
intentional Peer/Transactor and compiler-lineage differences explicitly.

### Stage 3 — Close transaction acknowledgement

**Status:** Recovered-pair boundary complete; licensed-oracle breadth remains
bounded

Rejected and concurrent submissions, pre-publication failure, and
post-publication/pre-result failure have durable PostgreSQL, same-Peer, and
fresh-Peer evidence with no duplicate committed effect. Reopen only for a
specific uncovered oracle or executable difference.

### Stage 4 — Complete PostgreSQL HA

**Status:** Active — in-flight takeover row complete

The next HA rows are concurrent submissions across takeover, lease/partition
behavior, and a deliberate split-brain race. Each must prove one authoritative
writer, monotonic history, deterministic client outcomes, stale-writer fencing,
Peer recovery, and cleanup, and may advance only the overlaps it actually
closes.

### Stage 5 — Exhaust the core artifact and explain it

**Status:** Pending completion of Stages 2 and 4

Close all remaining transaction/database, storage/catalog/log/index/
coordination, transport/adoption, and dormant core overlaps. Produce a source
and architecture map tracing a transaction from Peer submission through
durable publication, index adoption, and failover.

### Stage 6 — Lifecycle, operations, and maintenance

**Status:** Deferred

After the PostgreSQL/HA core: configuration, startup/shutdown, monitoring,
process events, caches, full-text search, backup/restore, garbage collection,
excision, integrity/repair, licensing, authentication, diagnostics, and
provisioning. Each included facility needs an artifact/source map, isolated
candidate validation, meaningful failure coverage, and cleanup.

### Stage 7 — Optional access tiers and alternative stores

**Status:** Deferred

Classify and recover or precisely bound Peer Server/thin Client, REST, Presto,
Console, DynamoDB/S3, Cassandra, H2/dev, Hot Rod, and other alternative stores
only after the authoritative PostgreSQL/HA core is stable or when a specific
core blocker requires one.

## Goal completion

Goal 3 is complete only when the recovered Peer and complete recovered
Transactor operate together through the required PostgreSQL-backed core without
original implementation fallback; every overlap is `RESOLVED_EQUIVALENT` or
`RESOLVED_DIVERGENT`; the HA matrix is complete; remaining ranked components
are recovered and validated or precisely bounded; and all material claims have
reproducible evidence.

## Next direct action

At the next continuation, make concurrent accepted submissions during
active-to-standby takeover the single executable boundary. Use it to test one
monotonic committed order with no duplicates or losses, deterministic outcomes
for every submitting Peer, one authoritative writer, fresh-Peer equality,
stale-primary fencing, and cleanup. Advance only overlap rows whose remaining
behavior this boundary actually closes; do not reopen exact-AOT or the parked
global classifier without an explicit escalation trigger.
