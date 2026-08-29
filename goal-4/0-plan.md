# Goal 4 — Finish a Coherent, Study-Quality Datomic Reconstruction

## Objective

Finish the educational recovery and explanation of Datomic Pro 1.0.7277 as a
coherent, runnable PostgreSQL-backed system. The recovered Peer and complete
recovered Transactor must work together across transaction submission, durable
storage, log publication, indexing, transport, acknowledgement, failover, and
normal operation without original implementation fallback. The finished system
must be clear enough to study from source and exercise through meaningful
runtime scenarios.

Goal 4 replaces exhaustive equivalence as the finish line with **strong
coherence**. Completion does not require proving that every dormant branch in
all 117 Peer/Transactor overlaps is universally equivalent. It requires strong
surface and architectural consistency, green critical paths, no known
contradiction on supported paths, focused regressions for confirmed defects,
and an honest account of residual risk and out-of-scope behavior.

Goal 2 and Goal 3 are historical strategy and evidence inputs. This file is the
authoritative strategy and status record for Goal 4.

## Strong-coherence boundary

The normal acceptance boundary is:

1. Candidate ownership and isolation: no original Peer or Transactor
   implementation classes enter a recovered runtime.
2. Expected namespaces and major callable, protocol, class, and subsystem
   surfaces are present and internally consistent.
3. Peer, transport, Transactor, storage, log, index, coordination, and HA
   contracts compose into one intelligible architecture.
4. Critical PostgreSQL-backed success, rejection, recovery, acknowledgement,
   and failover paths pass focused runtime checks.
5. No known unexplained contradiction remains on supported or exercised paths.
6. Confirmed recovery defects are repaired generically and retain focused
   regressions.
7. Dormant, optional, or unsupported behavior is identified as residual risk or
   scope rather than subjected automatically to exhaustive proof.

The 117-row overlap ledger is a diagnostic risk register, not a completion
gate. Investigate a row when evidence is surfaced that could falsify shared
design: a surface or ABI mismatch, an unexplained executable difference on a
relevant path, a differential/runtime failure, a contradictory subsystem
contract, or an explicit user request. Exact-AOT, global graph matching, and
broad compiler-family comparison are escalation tools only; they are never
default prerequisites.

## Material constraints

- Work in `/home/jake/Developer/atomic/datomic-rev` and preserve the recovered
  Peer as the reference boundary.
- Keep `/home/jake/Developer/atomic/goal-1` untouched and uninspected.
- Preserve existing worktree changes unless their ownership and disposition are
  established; do not erase retained recovery work to obtain a clean tree.
- Keep licensed originals isolated as fingerprinted evidence or behavioral
  oracles, never as candidate implementation dependencies.
- Use PostgreSQL as the primary authoritative backend. Optional interfaces and
  alternative stores must not displace the core system objective.
- Keep one outcome-bearing executable or explanatory boundary active at a time.
- Prefer retained evidence and targeted checks. Do not create review-of-review,
  namespace-by-namespace proof campaigns, or comparator projects without a
  concrete falsification trigger.
- Match claims to observation and record genuine uncertainty plainly.

## Execution strategy

Stages 2–4 remain ordered acceptance gates, but they are not a rigid execution
waterfall. Finish the active Stage 2 HA boundary first. After Stage 2 closes,
execute Stages 3 and 4 as evidence-driven subsystem vertical slices: validate
one high-value operational behavior, then immediately record its source map,
contracts, state transitions, and cited runtime evidence before selecting the
next behavior. The teaching narrative must grow with the executable proof,
not wait behind a broad operational campaign.

Stage 5 is a rolling scope ledger throughout that work. Optional facilities
encountered along the way are classified as validated, coherent but
unexercised, partial, or out of scope; classification alone does not create an
implementation obligation. Final release signoff remains last, after the
PostgreSQL core and architectural narrative are coherent.

## Known inherited state

- The complete 247-source Transactor corpus is recovered; 272/272 effective
  namespaces load and all 247 callable/root/class shapes agree under the
  established bounded surface.
- Recovered Peer and Transactor runtimes exclude original implementations and
  cross the primary PostgreSQL write, log, index, restart, transport,
  acknowledgement, and failover paths.
- The in-flight transaction-during-takeover v8 boundary passes: A's pending
  descriptor CAS made the prepared tail authoritative before B claimed and
  caught up that referenced lineage; the original Future reports unavailable,
  the same Peer continues through B, a fresh Peer and SQL root agree, and stale
  A self-fences. This is descriptor-CAS and catchup evidence, not startup
  discovery of an unreferenced immutable row.
- Concurrent accepted submissions during takeover now pass in both relevant
  descriptor-CAS schedules. In v6 stale A published all four logical writes
  before fencing (four adopted, zero resubmitted). In deterministic v7 B first
  claimed and caught up the unchanged baseline while A remained frozen, after
  which the Peer resubmitted exactly the four absent logical writes (zero
  adopted, four resubmitted). Both runs finish at one strict order through
  basis 1009, with unavailable original Futures, exact-once logical effects,
  stale-A fencing, fresh-Peer agreement, verified evidence, and clean shutdown.
- The advisory overlap ledger currently contains 13 fully resolved rows, nine
  bounded-partial rows, and 95 open rows. These counts describe certainty, not
  a known defect count or Goal 4 completion percentage.
- The accepted credential-scoped asymmetric storage-reachability result is now
  retained at `/tmp/datomic-recovered-pair-ha-partition-v6`. Broader packet
  loss/reordering and arbitrary multi-node topologies remain unproved by design.
  The recovered architecture still needs a consolidated source map and teaching
  narrative.
- The credential-scoped asymmetric PostgreSQL partition/heal v4 execution is
  behaviorally green but is **not accepted evidence**. It demonstrated B's
  promotion while A remained live and storage-incapable, healed stale-A CAS
  conflict/self-fencing, and same-/fresh-Peer agreement, but
  `run-status.properties` is failed: its evidence-secret gate found the node
  SQL password text in `config.properties`, the negative-login diagnostic, and
  the PostgreSQL server log. v4 remains an immutable failed diagnostic; only a
  fresh versioned run with generic evidence redaction may close the boundary.

## Stage 1 — Rebaseline around strong coherence

**Status:** Complete at the scaffold boundary

**Outcome:** Goal 4 begins from a concise, trustworthy account of what already
works, what remains outcome-bearing, and what is merely unproven dormant code.

**Focus:** This scaffold banks the trustworthy Goal 3 results, distinguishes
known runtime gaps from unproven dormant behavior, and converts exhaustive
overlap obligations into an explicit risk register without manufacturing
resolution claims. Future repository synchronization is a short execution-loop
check, not a separate review project.

**Completion signal:** Satisfied here: the inherited state, supported core
boundary, falsification triggers, residual risk, and next runtime boundary are
recorded consistently; no global equivalence gate remains an implicit
prerequisite.

## Stage 2 — Finish the authoritative HA spine

**Status:** Complete at the supported credential-scoped HA boundary

**Outcome:** The recovered system preserves one authoritative transaction order
through takeover and adversarial writer-coordination conditions.

**Focus:** Finish the credential-scoped partition/heal gate with secret-free,
self-verifying evidence. After banking it, run at most one bounded deliberate
no-writable-split-brain scenario, and only if it can materially falsify the
supported HA claim. Do not expand this stage into exhaustive partition,
topology, overlap, or scheduler proof. Let observed failures pull only the
implicated source, overlap, or structural question into scope.

**Completion signal:** Accepted work has one monotonic durable order with no
duplicates or losses; client outcomes are consistent with publication;
stale writers cannot remain authoritative; same-Peer and fresh-Peer state agree;
and PostgreSQL, processes, sessions, and ports clean up after every boundary.

**Current evidence:** The claim-sensitive concurrent gate is retained at
`/tmp/datomic-recovered-pair-ha-concurrent-v6` (A wins descriptor publication;
101-entry evidence manifest) and
`/tmp/datomic-recovered-pair-ha-concurrent-v7` (B claims the baseline first;
102-entry evidence manifest). Every manifest entry verifies. The v7 result
requires basis 1001 before recovery, four unavailable Futures, four idempotent
resubmissions, strict commits at 1003/1005/1007/1009, and a fresh-Peer audit at
basis 1009. These runs prove logical-intent accounting and one authoritative
descriptor order for the exercised schedules; they do not claim transparent
survival of original Futures or automatic adoption of unreachable append rows.

The accepted asymmetric PostgreSQL partition/heal gate is retained at
`/tmp/datomic-recovered-pair-ha-partition-v6`. Its 126-entry manifest verifies
in full (manifest-file SHA-256
`4e0a4ca8c8c58863712f8252dd66e320756dd70a02a38288f320ecf924299272`),
and `run-status.properties` is `passed`. A synchronized credential cut took 87
ms: A remained a live, non-stopped JVM with an open transport endpoint but zero
owned SQL sessions; its login failed for the required role-disabled reason;
Peer and B retained SQL reachability; and B advanced coordination revision 6
to 7. After heal, stale A observed exactly one heartbeat CAS conflict and
self-fenced. The continuing Peer and a fresh Peer agreed through B at basis
1066, the sole authoritative log-root row advanced from revision 3 to 5, all
active/Peer/standby role session counts reached zero, and all three service
ports closed. The raw negative-login diagnostic is outside the evidence set;
manifest-eligible text passes generic node-password redaction and a final
literal-secret scan. v4 remains a failed diagnostic and was not modified.

No additional Stage 2 execution is warranted. The retained frozen-active,
in-flight, and concurrent descriptor-CAS schedules already supply bounded
dual-writable/no-writable-split-brain pressure; another credential or scheduler
variant would add breadth rather than materially challenge the supported
claim. Universal packet partitions, arbitrary topology, and exhaustive
scheduler proof remain explicitly `NOT_RUN`.

## Stage 3 — Establish operational coherence

**Status:** Pending acceptance; execution will interleave with Stage 4 by
subsystem after Stage 2 closes

**Outcome:** The recovered Peer/Transactor system behaves as one repeatable
operational unit beyond a single transaction demonstration.

**Focus:** Validate configuration/startup/readiness, shutdown/restart/recovery,
monitoring/process events, cache, and bounded maintenance as separate focused
slices. After each slice, perform its Stage 4 explanation before starting the
next. Select facilities by architectural value and observed risk rather than
artifact breadth.

**Completion signal:** The system can be configured, started, observed,
stopped, restarted, and recovered repeatedly without original fallback,
unexplained authoritative-state changes, leaked owned services, or a known
critical lifecycle contradiction.

## Stage 4 — Build the architectural reconstruction

**Status:** Pending acceptance; explanatory slices will be written alongside
Stage 3 execution

**Outcome:** A reader can understand the recovered Datomic system from both its
architecture and its source.

**Focus:** Map Peer, transport, Transactor, transaction pipeline, durable
storage, log, indexes, coordination, HA, and important lifecycle facilities to
their recovered namespaces, classes, and runtime evidence. For each Stage 3
subsystem slice, immediately add its contracts and state transitions. Explain
intentional uncertainty without turning the explanation into an artifact dump.

**Completion signal:** A reader can trace a transaction from API submission
through ordering, durable publication, Peer visibility, index adoption,
restart, and failover using the recovered source and cited runtime evidence.

## Stage 5 — Bound the remainder and release the study system

**Status:** Rolling scope ledger active; final release signoff pending

**Outcome:** The recovered core has a defensible completion boundary, while
optional systems and residual uncertainty are represented honestly.

**Focus:** Classify important maintenance and optional components—such as
backup/restore, full text, excision, garbage collection, Peer Server/thin
Client, REST, Presto, Console, and alternative stores—as validated, coherent but
unexercised, partially recovered, or out of scope. Execute additional work only
when it materially improves the educational system or resolves a surfaced
contradiction.

**Completion signal:** The PostgreSQL-backed core remains green and teachable;
no known critical contradiction remains; optional and dormant areas have clear
boundaries; the risk register is honest; and the repository contains a
resumable, runnable, source-mapped study system.

## Goal completion

Goal 4 is complete when the recovered Peer and Transactor form a strongly
coherent, runnable, and explainable PostgreSQL-backed Datomic system across the
important supported paths without original implementation fallback; the
remaining uncertainty is explicitly bounded; and no known evidence contradicts
the recovered architecture. Universal equivalence of all dormant overlap code
is not required.

## Next direct action

Bank the completed HA source map and evidence narrative, then take the first
interleaved Stage 3/4 subsystem slice: configuration, startup, and readiness.
Use the retained fresh-catalog and missing-schema/startup evidence plus at most
one focused check only if a material contract remains unobserved. Immediately
map configuration normalization, schema/log claim, heartbeat ownership,
transport publication, readiness, fatal-start cleanup, and their state
transitions to recovered source and cited evidence. Update the rolling Stage 5
ledger for any optional configuration facilities encountered; do not start the
shutdown/restart slice until this executable-and-explanatory boundary is
coherent.
