# Goal 6 — Distributed Applications and Operator Delivery

## Objective and constraints

Deliver Stage6 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: secure multi-host
Rust applications, bounded restartable transaction observation and a complete
administrative CLI. Goal0 owns all requirements/invariants and the remaining
Stage7. This child is complete; Goal7 is now active. Preserve working local APIs, existing
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

**Status:** Complete2026-09-09.

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

**Status:** Complete2026-09-09, including actual PostgreSQL TLS listeners.

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

**Status:** Complete2026-09-09, including interrupted restore/retry.

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

## Verified delivery and decisions

The TLS listener uses mandatory certificate/name verification plus a redacted
32-byte bearer token. Discovery is PostgreSQL-authorized and bound to lineage,
current lease epoch and listener instance. Admission/frames/deadlines are bounded;
one deadline covers transport handshake through acknowledgment, not separate
PostgreSQL discovery/report opening. Optional versioned hints are not request
identity; valid excess hints can be ignored. Exact committed outcomes survive
local report failure. CLI/private credential files and the evolving application
support both existing local transport and remote discovery.

Actual two-network-namespace application acceptance passed15.72s: verified TLS,
restricted roles, separate writer/application, bad token rejection, real writer
SIGKILL/replacement/rediscovery and identical-key replay. The unchanged planning,
partition and fulltext workflow reaches basis11. Another process reopens the
captured basis2 reference after newer schema/data and replacement; authorization
is required. Excision rejects a new pre-excision reopening while an already held
value remains exact. References grant neither authority nor retention. This is
an integration fixture, not a throughput result. Dedicated remote tests cover
wrong roots/names/lineage/stale endpoints, deadline/admission, altered/stale/foreign
hints and lost committed responses. Stored native query/transaction program
preview, commit, retry, replacement and program rebinding passed1/1(3.73s),
preserving original encoded requests/programs and exact historical receipts.

Post-commit wakeups use a nonblocking bounded/coalescing publisher. Notification
SQL cannot decide commit success; queue saturation/failures are observable and
durable-log catch-up repairs loss. Listener registration precedes initial catch-up,
reconnect triggers repair, and quiet anti-entropy defaults to30s. The synchronous
driver's unbounded notification VecDeque was replaced with direct async protocol
polling: at most64 messages per batch and one retained wakeup bit. Same verified
PostgreSQL configuration/I/O policy applies. Flood witness512 notices/504coalesced,
peakbatch64, no fabricated transactions. Consumer suite6/6 and legacy connection/
policy8/8 passed actual PostgreSQL. Separate writer sample: entire reader process
0SQL over500ms idle; consumer76.617ms/peer76.620ms observation. These are debug
samples, not latency guarantees or writer-process SQL totals.

ChangeConsumer delivers one authenticated event, persists only an acknowledged
contiguous checkpoint under CAS, and resumes after unacknowledged replay. A
transaction byte limit is admission, not a semantic transaction limit. Checkpoints
are login-isolated via RLS, bound to lineage/generation/T/hash, and are not pins
or canonical backup data. Generation/history failures are explicit; no indefinite
retention or exactly-once external effect. Existing lossless reports remain.

Supported administrative commands now cover migrate/grants/create/status,
backup/list/verify, guarded separate-target restore, inspect, bounded explicit-
retention GC and diagnosed fulltext rebuild. Preview is default for destructive
operations; exact physical database/catalog guards apply. PostgreSQL LOGIN roles
are provisioned by normal PostgreSQL administration, not implicitly on startup.
Actual disposable CLI suite passed2/2(15.14s), including locked-target restore
SIGTERM and identical-command retry, backup reuse, deep offline verification,
wrong targets/lineages/roles, history preservation, real GC and missing-search-
root repair. No production fault hook or user-data target was used.

Credential ownership/regular-file/size/redaction unit test passed. Local product
regression2/2(8.90s) passed after the shared application/test-support refactor;
20 warmed calculations use0SQL, restart SSD reuse18hits. Final rebuilt executable
rerun follows the final observation changes before child closure.

Final rebuilt suite: admin2/2(12.84s), consumers6/6(12.68s), roles1/1(3.18s),
local product2/2(8.90s), isolated-network product1/1(15.24s), remoteTLS3/3(11.34s).
Actual separate hostssl-only PostgreSQL TLS suite2/2(3.62s) passed with both async
LISTEN backends verified TLS1.3 via pg_stat_ssl; consumer checkpoint resume and
independent peer advancement passed,60.801ms observation sample. Missing private
trust root and plaintext sslmode=disable both rejected. All-target check and
optimized executable build pass. No self-skipped PostgreSQL test is counted.

## Continuation

Complete2026-09-09. Return to Goal0 and execute Goal7 integrated measurements,
bottleneck repairs and final acceptance. Reopen this child for an integrated gap.
