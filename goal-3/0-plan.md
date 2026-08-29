# Goal 3 — Finish the Recovered Datomic System Through Evidence-Driven Convergence

## Objective

Finish the educational recovery and explanation of Datomic Pro 1.0.7277 by
turning the already working recovered Peer and Transactor into a completely
bounded, evidence-backed system. Close the remaining exact-source acceptance
boundary, reconcile every Peer/Transactor overlap, finish transaction
acknowledgement and HA correctness against PostgreSQL, and only then complete
or explicitly bound the lower-priority lifecycle, operational, optional-access,
and alternative-backend components.

Goal 3 updates the execution strategy for the outcome pursued by Goal 2. The
Goal 2 plan and retained evidence remain historical inputs; this file is the
authoritative sequencing and status record for new work.

## Material constraints

- Work in `/home/jake/Developer/atomic/datomic-rev` and keep
  `/home/jake/Developer/atomic/goal-1` untouched and uninspected unless the user
  explicitly changes that boundary.
- Bind claims to Datomic Pro 1.0.7277 and the pinned Peer and Transactor
  artifacts already recorded by Goal 2.
- Preserve the recovered Peer as the reference boundary and recover the whole
  Transactor, including all 117 namespace-name overlaps rather than assuming
  shared names imply shared implementations.
- Keep licensed originals isolated as fingerprinted evidence or behavioral
  oracles. Never admit original Peer or Transactor implementation classes into
  a candidate runtime.
- Use PostgreSQL as the primary durable backend. Defer alternative stores and
  optional interfaces until the authoritative PostgreSQL, transaction,
  transport, and HA paths are complete.
- Repair recovery causes generically from source, bytecode, and behavior
  evidence. Do not accumulate one-off compiler-ID or debug-metadata exceptions
  merely to make a comparator green.
- Use one focused regression for each confirmed defect and rerun only the
  downstream gates whose executable inputs changed.
- Preserve unrelated worktree changes, bound processes and disposable
  services, and require cleanup and durability evidence before reporting PASS.
- Keep this plan concise. Put detailed evidence in repository reports and
  manifests; record here only material facts, decisions, stage status, and the
  next direct action.

## Known starting state

- Goal 2 Stages 0, 3, 4, and 6 are complete: the artifacts are inventoried;
  the recovered Transactor boots and fails safely; PostgreSQL catalog,
  database, value, revision, and publication behavior is proven; and the
  recovered Peer/Transactor round trip through transaction, log, persistent
  indexes, transport interruption, and reconnection works.
- Stage 1 has a complete 247-source recovery. All 272 effective namespaces load
  and all 247 callable/root/class shapes agree. Its sole remaining acceptance
  boundary is the normalized exact-AOT relation for 87 shipped-source
  namespaces and 3,431 classes, followed by the sealed production wrapper.
- The exact-AOT candidate-to-candidate relation passes all 3,431 classes. The
  oracle relations have advanced to a localized generated-class pairing
  ambiguity. The scanner and JVM verifier are independently green. The wrapper
  preflight exposed a bounded mutable-temporary-root sealing defect that must be
  corrected without weakening strict seals for immutable inputs.
- Nine of 117 Peer/Transactor overlap rows are resolved. `datomic.promise` has
  a new, unpromoted four-lane probe and runner in the current worktree and must
  be independently reviewed before its row can be counted as resolved.
- Stage 5 proves rejected transactions, concurrent monotonic ordering, the
  prepublication crash boundary, durable recovery, and restart adoption. The
  post-publication/pre-result acknowledgement window and complete oracle
  equivalence remain open.
- Stage 7 proves one active-to-standby takeover, same-Peer continuation,
  durable post-takeover write, and stale-primary self-fencing. In-flight,
  concurrent, partition, and split-brain rows remain open.
- The stale Goal 2 tool status is not evidence that execution is blocked.
  Derive current state from the repository, retained manifests, and this plan.

## Strategic rule

After the first bounded checkpoint, use a downstream-pulls-upstream cycle:

1. Select one concrete Stage 5 or Stage 7 executable boundary.
2. Run it and localize the first real semantic divergence.
3. Repair a generic recovery cause when one exists.
4. Resolve every Peer/Transactor overlap implicated by that boundary.
5. Rerun the exact boundary and bank the result.

This is not calendar-based alternation. Runtime evidence prioritizes Stage 2
and supplies behavior proof, but it cannot classify dormant code; an exhaustive
117-row closure remains mandatory. Stage 1 should close once and reopen only
if a genuine recovery-tool or promoted-source change invalidates it.

## Stages

### Stage 1 — Seal the bounded recovery checkpoint

**Status:** In progress

**Outcome:** The current whole-corpus recovery and the first pending overlap
row are promoted from promising diagnostics to independently verified,
resumable evidence.

**Focus:** Resolve the generated-class pairing ambiguity by a principled,
general rule; correct the wrapper's mutable-root evidence model while retaining
strict immutable-input policies; rerun clean preflight and the full 87-source,
3,431-class production comparator/scanner/verifier chain; independently review
and run the four-lane `datomic.promise` gate, including its concurrency,
failure, provenance, metadata, and cleanup claims.

**Completion signal:** Both oracle exact-AOT relations and candidate
determinism pass all 3,431 classes; the scanner and JVM verifier pass inside a
self-sealed production wrapper; no original implementation enters candidate
runtimes; `datomic.promise` is either resolved with a checked-in focused gate
or remains explicitly open with a concrete defect; repository reports and
this plan reflect the observed result.

### Stage 2 — Close the durable acknowledgement boundary

**Status:** Pending

**Outcome:** Transaction success is understood and proven across the complete
publication-to-client-result interval.

**Focus:** Exercise the post-publication/pre-result failure cut alongside the
already proven prepublication cut; compare accepted and rejected outcomes with
the licensed oracle where required; prove authoritative root, log, basis, and
Peer-visible state before and after restart; distinguish permitted orphan
immutable values from published state.

**Completion signal:** Injected failures cannot report success before durable
publication; a transaction durably published before acknowledgement has one
unambiguous recoverable outcome with no duplicate committed effect; restart
and fresh-Peer evidence agree with PostgreSQL; the complete Stage 5 boundary is
honestly green or a specific semantic blocker is recorded.

### Stage 3 — Resolve the runtime-pulled shared kernel

**Status:** Pending

**Outcome:** The highest-leverage transaction and transport overlaps are
resolved using the green main path and acknowledgement evidence rather than
source notation alone.

**Focus:** After `datomic.promise`, prioritize `datomic.queue`,
`datomic.builtins`, `datomic.reconnector2`, `datomic.connector`, and
`datomic.artemis-client`. Pull `datomic.update`, `datomic.log`, `datomic.db`,
`datomic.fressian`, or `datomic.error` forward when an executable boundary
implicates them. Attach existing behavior evidence first; add a focused probe
only for a material uncovered branch. Preserve intentional compiler-lineage or
product differences explicitly.

**Completion signal:** Every row in the prioritized cohort has a source/ABI/
bytecode classification and sufficient focused behavior evidence; no runtime
success is used to overclaim dormant behavior; all confirmed recovery defects
have generic regressions; the 117-row ledger and resolved/open count are exact.

### Stage 4 — Converge HA and its implicated overlaps

**Status:** Pending

**Outcome:** Failover preserves one authoritative writer, monotonic committed
history, and deterministic client outcomes while advancing the relevant Stage
2 rows.

**Focus:** Run one bounded HA row at a time in this order: in-flight transaction
during takeover; concurrent submissions across takeover; acknowledgement loss
around publication; lease expiry and network partition; deliberate split-brain
race. Let failures pull coordination, cluster, reconnect, transport, log, and
database overlaps upstream, then rerun the exact HA boundary before moving on.

**Completion signal:** The HA matrix proves one active writer, stale-writer
fencing, no duplicate or lost acknowledged transaction, monotonic committed
order, same-Peer and fresh-Peer recovery, no writable split-brain interval, and
complete cleanup; every implicated overlap is resolved or has a genuine,
localized blocker.

### Stage 5 — Exhaust the artifact and explain the core

**Status:** Pending

**Outcome:** Stage 2 is complete for the entire artifact, and the recovered
PostgreSQL-backed Peer/Transactor system is both trustworthy and teachable.

**Focus:** Classify the remaining overlaps by subsystem rather than by
alphabetical one-off tests: transaction/database semantics; storage,
catalog, log, index, and coordination; transport and Peer adoption; caches and
maintenance; then dormant residuals. Close the partial central rows and all
117 total rows with exact guarded normalization, static proof, existing runtime
evidence, or a focused behavior gate as appropriate. Consolidate the
architecture explanation of Peer, Transactor, storage, log, indexing,
transport, coordination, and HA.

**Completion signal:** All 117 overlaps are classified and resolved with no
unexplained semantic difference; the recovered source builds and runs without
original implementation fallback; all affected PostgreSQL, transaction,
index, transport, and HA regressions pass; the architecture/source map lets a
reader trace a transaction from Peer submission through durable publication,
index adoption, and failover.

### Stage 6 — Complete lifecycle, features, and operations

**Status:** Pending

**Outcome:** Correctness-sensitive facilities beyond ordinary transactions
compose with the proven PostgreSQL core.

**Focus:** In priority order: full-text indexing/search; filesystem and S3
backup/restore; stored-value garbage collection, excision, integrity, and
repair; non-authoritative caches/value-cache layers; monitoring, metrics,
logging, licensing, authentication, diagnostics, and provisioning.

**Completion signal:** Each included facility has a clear artifact/source map,
an isolated candidate validation with meaningful failure and cleanup coverage,
and a bounded explanation of how it composes with the authoritative core.

### Stage 7 — Close optional access tiers and the system map

**Status:** Pending

**Outcome:** The complete classic Datomic topology is recovered and explained,
with optional or deferred components bounded honestly.

**Focus:** In order: Peer Server and thin Client API; REST; Presto integration;
Console; then DynamoDB/S3, Cassandra, H2/dev, Hot Rod, and other alternative
stores only after the PostgreSQL system is stable. Recover and validate where
material; otherwise record precise artifact, source, dependency, and deferral
boundaries.

**Completion signal:** Required core stages remain green; optional artifacts
and interfaces have reproducible recovery/evidence or a justified explicit
boundary; the final subsystem and source-ownership map contains no component
misrepresented as recovered or validated.

## Goal completion

Goal 3 is complete only when the recovered Peer and complete recovered
Transactor can be studied and exercised together through PostgreSQL-backed
write, log, index, transport, acknowledgement, and HA paths without original
implementation fallback; all 117 overlaps are resolved; the remaining ranked
components satisfy Stages 6 and 7 through recovery or precise explicit
bounding; and all important claims are supported by reproducible evidence.

## Next direct action

Audit the current exact-AOT wrapper/comparator changes and the unpromoted
`datomic.promise` gate. Fix only confirmed defects, run fresh isolated evidence
roots, and close Stage 1 before expanding the HA matrix or beginning lower-
priority facilities.
