# Goal 6 — Distributed Applications and Operator Delivery

## Objective and constraints

Deliver Stage6 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: secure multi-host
Rust applications, bounded restartable transaction observation and a complete
administrative CLI. Goal0 owns all requirements/invariants and the remaining
Stage7. This is the sole active child. Preserve working local APIs, existing
data and request/program meaning, exact receipts, serialized fenced authority,
schema/history and repairs. Local datomic_pro_docs governs semantics; recovered
1.0.7705 source informs mechanisms. No JVM/wire compatibility, alternate store,
mandatory broker, replication manager or recursive goal.

Goals1–5 provide local submission, explicit endpoint resolution, immutable native
reads, exact snapshot references, advisory hints, indexed fulltext and operators.
Extend them, not a second engine or query server. PostgreSQL is the durable
authority; network notices are only bounded wakeups repaired from authenticated
logs. Keep the existing lossless report queue distinct from new bounded consumers.
No credential output, implicit migrations/recovery, missing-config test claims
or silent narrowing of remote, lifecycle and security acceptance.

## Stages

### 1. Secure remotely usable submission

**Status:** Active.

**Outcome:** Applications discover/reconnect to the fenced writer and submit
over authenticated, encrypted network transport with exact durable meaning.

**Focus:** TLS/authentication, endpoint lineage/lease binding, bounded admission,
deadlines/unknown outcomes, replacement/rediscovery and versioned optional hints.
Reuse existing request/outcome codecs and report adoption; finish executable/SDK
configuration. Queries remain peer-local with authorized PostgreSQL access.

**Completion signal:** Isolated real network processes exercise remote submit,
replacement/rediscovery, lost response and same-key retry, invalid credentials,
wrong lineage/stale authority and resource errors. Local/remote partition,
program/planning/search results agree; hints cannot change request identity.

### 2. Restartable observation and value handoff

**Status:** Pending; bounded observation can proceed alongside settled endpoints.

**Outcome:** Applications react to other writers and resume after disconnect
without unbounded buffering or assuming exactly-once external side effects.

**Focus:** Cross-process commit/index wakeups, durable gap repair, bounded replay/
live delivery and explicit checkpoint lineage/generation/last-transaction identity.
Retain old unbounded report API semantics. Another authorized process reopens an
exact supported snapshot reference under retention/excision rules.

**Completion signal:** Dropped/coalesced notices, restart/disconnect and checkpoint
resume preserve ordered facts; unavailable history/generation changes fail
explicitly. Idle SQL and observation latency are measured. Cross-process snapshot
handoff agrees after newer transactions/schema; unauthorized/unavailable/pre-
excision cases obey policy. No indefinite pin or external-effect guarantee.

### 3. Complete the operator workflow and integrate

**Status:** Pending; reuse existing operators while remote work proceeds.

**Outcome:** Operators use supported commands for provisioning, backup/verify,
separate-target restore, inspection/status, bounded GC and derived-index repair.

**Focus:** Exact targets, separate administrative authority, retention, preview/
destructive controls, interruption/retry, progress/errors and healthy pending
maintenance distinct from corruption. Extend the evolving application/tests.

**Completion signal:** Actual commands execute successful and safely failing
backup/verify/restore/inspect/authorized GC paths without editing source or relying
on acceptance-only provisioning. All stages compose on PostgreSQL and the real
network boundary. Record evidence, then return to Goal0 and execute Goal7; this
child alone is not product completion.

## Continuation

Started2026-09-09 after Goal5. Implement directly from current APIs. Disposable
user/network namespaces are available for acceptance; keep network interfaces,
credentials, process failures and destructive targets isolated from user data.

