# Goal 2 — Recover and Understand the Complete Datomic Pro 1.0.7277 System

## Objective

Produce an evidence-backed educational source recovery and architectural
reconstruction of classic Datomic Pro 1.0.7277 beyond the already recovered
Peer. Recover the complete Transactor artifact, explain and validate the
authoritative write, durability, indexing, coordination, and HA paths against
PostgreSQL first, and then cover the remaining maintenance, operational, and
optional access components in priority order.

The result should let a reader study how the Peer, Transactor, and durable
storage cooperate without relying on the original Peer or Transactor AOT
classes in candidate runtimes. Licensed originals may be used only as
fingerprinted, isolated evidence or behavioral oracles.

## Material constraints

- Bind recovery claims to Datomic Pro `1.0.7277` and the exact source artifacts:
  - `peer-1.0.7277.jar`, SHA-256
    `cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`;
  - `datomic-transactor-pro-1.0.7277.jar`, SHA-256
    `d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692`.
- Preserve the completed Peer recovery as a reference and regression boundary.
  Do not edit or inspect `goal-1` unless the user explicitly changes that
  boundary.
- Recover the entire Transactor artifact: all classes, resources, 162 Datomic
  namespaces, and both its 45 Transactor-only and 117 Peer-overlapping
  namespaces. Overlap is not evidence of bytecode or source equivalence.
- Keep candidate and oracle execution visibly separate. An original Peer or
  Transactor implementation must not enter a recovered candidate classpath.
- Use PostgreSQL as the primary durable backend. Treat PostgreSQL, JDBC,
  Artemis, Lucene, Jetty, and similar libraries as external machinery; recover
  the Datomic-specific semantics around them.
- Use disposable storage, bounded processes, strict cleanup, and evidence that
  cannot report success before cleanup and durability checks complete.
- Preserve unrelated worktree changes. Prefer deterministic regeneration and
  bytecode-constrained repairs over inferred rewrites or cosmetic cleanup.
- Defer alternative storage backends until the PostgreSQL-backed system and
  higher-priority components are complete.
- Match every completion claim to observable evidence and state uncertainty or
  evidence limits plainly.

## Known context

- The Peer artifact contains 5,517 classes and 142 Datomic namespace
  initializers. Its recovered source and validation evidence live in this
  repository and currently form the reference implementation.
- The Transactor artifact contains 9,682 classes and 162 Datomic namespace
  initializers. Its 117 namespace-name overlaps with the Peer require explicit
  comparison rather than wholesale reuse of Peer sources.
- `core2-1.0.140.jar` is support packaging, not a fourth architectural role;
  its classes duplicate the Peer-embedded core2 classes for this distribution.
- PostgreSQL is the external storage service. Datomic's SQL, key/value,
  cluster, catalog, and coordination layers live inside the Peer and
  Transactor artifacts.
- Log writing, persistent indexing, transport, lifecycle, backup, and
  maintenance are logical subsystems rather than additional mandatory
  data-plane servers.

## Stages

### Stage 0 — Freeze the reference boundary

**Status:** Pending

**Outcome:** A trusted starting point for Goal 2 that preserves the recovered
Peer and identifies every input to the Transactor recovery.

**Focus:** Reconcile the actual worktree and retained evidence with the Peer
baseline; fingerprint the complete Transactor artifact, distribution
dependencies, resources, launch scripts, and optional Datomic-owned artifacts;
record candidate/oracle boundaries before modifying recovery tooling.

**Completion signal:** The Peer baseline still passes its authoritative gates,
the Transactor corpus and dependency boundary are completely inventoried, and
the plan records any changed facts that affect later stages.

### Stage 1 — Recover the whole Transactor corpus

**Status:** Pending

**Outcome:** A deterministic, navigable structural recovery of the entire
Transactor JAR, not merely its Transactor-only namespace names.

**Focus:** Account for every class and resource; reconstruct all Datomic
namespaces and handwritten Java surfaces; distinguish Datomic code from bundled
or external dependencies; retain reproducible provenance and exact artifact
manifests.

**Completion signal:** Every Transactor entry is classified, the recovered
source tree regenerates deterministically, all source is readable/loadable at
the appropriate structural boundary, and no unexplained class or resource gap
remains.

### Stage 2 — Reconcile the shared semantic kernel

**Status:** Pending

**Outcome:** Correct Transactor-specific versions of the namespaces shared in
name with the Peer, with reuse and divergence justified by evidence.

**Focus:** Compare database values, transaction representations, validation,
builtins, stored functions, serialization, log/index structures, storage,
cluster, catalog, and coordination code across both artifacts. Preserve exact
ABI and authored surfaces while repairing only bytecode-constrained recovery
defects.

**Completion signal:** All 117 overlaps are classified and resolved; the
Transactor shared kernel builds and loads without original Peer/Transactor AOT
fallback; surface and focused behavior comparisons have no unexplained
differences.

### Stage 3 — Establish a recovered Transactor process

**Status:** Pending

**Outcome:** A bounded recovered process that owns its launch, configuration,
lifecycle, and shutdown without yet claiming transaction correctness.

**Focus:** Recover launcher, Transactor entry points, configuration and
extension layers, lifecycle wiring, process ownership, resource cleanup, and a
minimal no-write startup mode.

**Completion signal:** The source-built Transactor starts from a recorded
candidate classpath, reaches a deterministic ready boundary, and stops cleanly
under success and injected startup failure without loading the original
Transactor implementation.

### Stage 4 — Make PostgreSQL the proven durable substrate

**Status:** Pending

**Outcome:** A recovered Transactor foundation that can safely create, locate,
read, and update Datomic storage state through disposable PostgreSQL.

**Focus:** SQL and key/value adapters, revision CAS, clustered values and
references, system catalog, database identity, log-tail and index-root
references, active endpoint coordination, and failure-safe root publication.

**Completion signal:** Disposable PostgreSQL gates prove exact value bytes,
revision behavior, catalog/coordination semantics, database identity, and root
publication/rejection while all candidate and service boundaries remain
auditable and cleanup-complete.

### Stage 5 — Recover transactions and the durable log

**Status:** Pending

**Outcome:** An authoritative single-writer path whose transaction semantics,
total order, durable commit, and acknowledgement boundary are understood and
validated.

**Focus:** The Transactor update processor and writer; tempids, lookup refs,
uniqueness/upserts, CAS, schema changes, transaction functions, basis `t`
assignment, request ordering, log tree construction, durable publication,
notifications, and crash/failure boundaries.

**Completion signal:** Direct and end-to-end PostgreSQL workloads match the
licensed oracle for accepted and rejected transactions; concurrent submissions
produce one monotonic order; injected failures demonstrate that success is
never acknowledged before the exact durable commit boundary.

### Stage 6 — Produce indexes and complete the Peer round trip

**Status:** Pending

**Outcome:** Committed transactions become persistent indexes and flow through
the real Peer–Transactor protocol into correct immutable Peer database values.

**Focus:** Persistent index scheduling/building/publication; EAVT, AEVT, AVET,
and VAET/reverse-reference roots; memory-tail replay and Peer adoption;
Transactor Artemis server, shared Fressian protocol, Peer connector,
notifications, syncs, errors, reconnection, and cleanup.

**Completion signal:** A recovered Peer and recovered Transactor complete the
full submit–commit–notify–query cycle on PostgreSQL without original AOT
implementations; index lag/adoption and transport interruption tests preserve
the exact logical database state.

### Stage 7 — Prove active/standby correctness

**Status:** Pending

**Outcome:** The recovered lifecycle and storage coordination enforce one
authoritative writer across standby, failure, and takeover transitions.

**Focus:** Master and standby loops, heartbeats, fencing, endpoint discovery,
semaphores, takeover, reconnection, in-flight outcomes, monotonic `t`, and
split-brain prevention.

**Completion signal:** Bounded failover matrices show one active writer,
monotonic committed history, deterministic client recovery, no duplicate or
lost acknowledged transaction, and no writable split-brain interval.

### Stage 8 — Complete data lifecycle, features, and operations

**Status:** Pending

**Outcome:** The recovered core supports the major correctness-sensitive and
production-facing facilities beyond ordinary transactions.

**Focus:** In this priority order: full-text writer/search integration;
filesystem and S3 backup/restore adapters and CLI; stored-value garbage
collection, excision, integrity, and repair; non-authoritative caches and
value-cache layers; metrics, monitoring, logging/rotation, licensing,
authentication, diagnostics, and provisioning. Keep JVM GC telemetry distinct
from Datomic stored-value reclamation.

**Completion signal:** Each included facility has an explicit artifact/source
map, isolated candidate validation, failure and cleanup coverage, and a bounded
claim that composes with the PostgreSQL-backed core without weakening its
durability or correctness guarantees.

### Stage 9 — Recover optional access tiers and close the system map

**Status:** Pending

**Outcome:** The remaining Datomic-owned interfaces are either recovered and
validated or explicitly deferred with a complete, evidence-backed boundary.

**Focus:** In order: Peer Server and thin Client API; REST; Presto integration;
Console; then alternative storage backends such as DynamoDB/S3, Cassandra,
H2/dev, and Hot Rod only after PostgreSQL and higher-priority components are
stable. Reconcile all documentation, reproduction paths, and subsystem maps.

**Completion signal:** Optional artifacts and interfaces have reproducible
source/evidence or a precise justified deferral; the complete classic Datomic
topology and source ownership map is documented; all required core stages are
green; and no known gap is mislabeled as a recovered component.

## Goal completion

Goal 2 is complete only when the recovered Peer and recovered Transactor can be
studied and exercised together against PostgreSQL through the authoritative
write, log, index, transport, and HA paths, with the remaining ranked
components recovered or explicitly bounded as described above. A successful
Transactor boot, a single transaction, or recovery of only the 45 unique
namespaces is not completion.
