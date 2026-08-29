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

## Known inherited state

- The complete 247-source Transactor corpus is recovered; 272/272 effective
  namespaces load and all 247 callable/root/class shapes agree under the
  established bounded surface.
- Recovered Peer and Transactor runtimes exclude original implementations and
  cross the primary PostgreSQL write, log, index, restart, transport,
  acknowledgement, and failover paths.
- The in-flight transaction-during-takeover v8 boundary passes: the standby
  adopts the transaction exactly once, the original Future reports unavailable,
  the same Peer continues through the promoted standby, a fresh Peer and SQL
  root agree, and the stale primary self-fences.
- The advisory overlap ledger currently contains 13 fully resolved rows, nine
  bounded-partial rows, and 95 open rows. These counts describe certainty, not
  a known defect count or Goal 4 completion percentage.
- Remaining high-value runtime uncertainty centers on concurrent submissions
  during takeover, lease/partition behavior, and deliberate split-brain
  pressure. The recovered architecture also needs a consolidated source map and
  teaching narrative.

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

**Status:** Pending

**Outcome:** The recovered system preserves one authoritative transaction order
through takeover and adversarial writer-coordination conditions.

**Focus:** Complete the remaining high-value concurrency, lease/partition, and
split-brain scenarios one boundary at a time. Let observed failures pull only
the implicated source, overlap, or structural question into scope.

**Completion signal:** Accepted work has one monotonic durable order with no
duplicates or losses; client outcomes are consistent with publication;
stale writers cannot remain authoritative; same-Peer and fresh-Peer state agree;
and PostgreSQL, processes, sessions, and ports clean up after every boundary.

## Stage 3 — Establish operational coherence

**Status:** Pending

**Outcome:** The recovered Peer/Transactor system behaves as one repeatable
operational unit beyond a single transaction demonstration.

**Focus:** Validate the important configuration, startup, readiness, shutdown,
restart, monitoring/process-event, cache, and bounded maintenance behavior that
supports studying and operating the PostgreSQL core. Select facilities by
architectural value and observed risk rather than artifact breadth.

**Completion signal:** The system can be configured, started, observed,
stopped, restarted, and recovered repeatedly without original fallback,
unexplained authoritative-state changes, leaked owned services, or a known
critical lifecycle contradiction.

## Stage 4 — Build the architectural reconstruction

**Status:** Pending

**Outcome:** A reader can understand the recovered Datomic system from both its
architecture and its source.

**Focus:** Map Peer, transport, Transactor, transaction pipeline, durable
storage, log, indexes, coordination, HA, and important lifecycle facilities to
their recovered namespaces, classes, and runtime evidence. Explain the main
state transitions and intentional uncertainty without turning the explanation
into an artifact dump.

**Completion signal:** A reader can trace a transaction from API submission
through ordering, durable publication, Peer visibility, index adoption,
restart, and failover using the recovered source and cited runtime evidence.

## Stage 5 — Bound the remainder and release the study system

**Status:** Pending

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

After a bounded current-state check, make concurrent accepted submissions
during active-to-standby takeover the single executable boundary. Prove one
durable monotonic order, deterministic Peer outcomes, no duplicate or lost
accepted work, stale-writer fencing, fresh-Peer agreement, and cleanup. If the
live repository surfaces a more fundamental contradiction, address that
instead; otherwise do not create another planning, review, classifier, or
evidence-infrastructure lane before running this boundary.
