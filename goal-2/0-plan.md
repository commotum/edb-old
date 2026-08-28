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
- Recover the entire Transactor artifact: all classes and resources, all 247
  compiled namespace initializers (162 Datomic and 85 bundled-library
  namespaces), and both its 45 Transactor-only and 117 Peer-overlapping
  Datomic namespaces. Overlap is not evidence of bytecode or source
  equivalence.
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
- The Transactor artifact contains 9,682 classes and 247 namespace
  initializers: 162 under `datomic.*` and 85 from bundled libraries. Its 117
  Datomic namespace-name overlaps with the Peer require explicit comparison
  rather than wholesale reuse of Peer sources.
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

**Status:** Complete

**Outcome:** A trusted starting point for Goal 2 that preserves the recovered
Peer and identifies every input to the Transactor recovery.

**Focus:** Reconcile the actual worktree and retained evidence with the Peer
baseline; fingerprint the complete Transactor artifact, distribution
dependencies, resources, launch scripts, and optional Datomic-owned artifacts;
record candidate/oracle boundaries before modifying recovery tooling.

**Completion signal:** The Peer baseline still passes its authoritative gates,
the Transactor corpus and dependency boundary are completely inventoried, and
the plan records any changed facts that affect later stages.

**Evidence:** The unchanged Peer inputs match all final manifests and retained
evidence was reverified. `datomic-rev/transactor/reports/stage-0-boundary.md`
records the complete corpus, launch/dependency map, optional products, and
candidate/oracle policy. The deterministic checked-in baseline accounts for
all 9,871 archive entries, and its `manifest.sha256` verifies in full.

### Stage 1 — Recover the whole Transactor corpus

**Status:** In progress

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

**Current evidence:** The first bounded whole-corpus run recovered 237 of 247
initializer namespaces and read-validated every emitted file. Seven early
failures were structural decompiler defects exposed by Java 8 AOT control
flow: six non-returning methods were losing their accumulated bodies, and
`datomic.index` had three implicit function loops whose inferred else boundary
was replaced with `nil`. Both repairs have focused regressions; a fresh
regeneration after them reproduced the complete 142-namespace Peer baseline at
its unchanged sorted manifest SHA-256
`ec43cd7e963514d855b0b6080c485caa638858dced0a3e40d4fca0b6866531be`.

All 85 bundled namespace initializers map one-to-one to exact `.clj` or `.cljc`
entries in 22 of the 533 hash-verified distribution dependency JARs. The
checked-in recovery runner reproduced two byte-identical 85-source trees; its
source-manifest SHA-256 is
`1edebbeb8241f5e811d4a38ba0b654332e5046b2d48754558d65e5408b62c83f`.
The ownership map is
`datomic-rev/transactor/reports/stage-1-bundled-source-ownership.tsv`; exact
source ownership does not yet substitute for the separate AOT-equivalence
gate.

Entry-closure accounting now maps all 9,682 classfiles to all 293 source
owners: 9,630 AOT classes to 247 namespace closures and 52 Java classes to 46
Java source candidates, with zero orphan and zero unresolved rows. Two
source-less generated proxies require an explicitly recorded longest-prefix
assignment corroborated by constructor calls; the other 1,993 nested-prefix
overlaps resolve uniquely through `SourceFile`. Independent result trees are
byte-identical at manifest-file SHA-256
`33cfe7358aac525f657b175d8a639b42e4a16160a388ea0282a31c035c387484`.
The reproduction tool and compact maps live under
`datomic-rev/transactor/tools`, `scripts`, and `reports/stage-1-*`.

All 11 resources are also classified: eight are byte-identical to Peer and
three are Transactor-only. This exposed a stricter isolation issue: the frozen
byte-parity Peer reference artifact includes the vendor Transactor key and
trust stores. Preserve that artifact as reference/oracle evidence, but never
use its resource profile for the integration candidate. The clean candidate
must regenerate metadata, reconstruct semantic registrations, source optional
third-party UI assets independently, generate per-environment TLS identity and
trust, and hard-deny vendor JKS hashes
`f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1`
and `ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b`.

The apparent heap failures in `datomic.backup-cli`, `datomic.datalog`, and
`datomic.update` were algorithmic expansion at an impure-loop self edge, not a
need for a larger heap; all three complete in bounded 512 MiB JVMs after the
control-flow repair. Two independent diagnostic runs then emitted 247
reader-valid sources with identical source-manifest-file SHA-256
`65f75b7f4d458c2405c1ec624a4d7b29bbdd0409d1e5f110f1265bb29e682b99`.
That result is explicitly rejected as a completion gate: a full sentinel scan
found five `BROKEN DECOMP` bodies in bundled namespaces and seven in Datomic
namespaces (`datalog`, `update`, `kv-cluster`, `rest`, `valcache`, and two
`validators` bodies). Exact shipped source removes the bundled five. Strict
recovery exposed that the first terminal-loop fallback also truncated a nested
`datalog` iterator continuation, so that broad fallback is being replaced by
narrow bytecode-derived loop and nested-try handling rather than accepted
because its output reads. The canonical runner now decompiles only the 162
Datomic initializers with `:lenient? false`, merges the exact 85 bundled
sources, validates both `.clj` and `.cljc`, and fails on any remaining Datomic
sentinel.

The complete Java-origin slice is now recovered under
`datomic-rev/transactor/src-java`: 46 sources compile deterministically to all
52 expected classes with 122 fields and 303 methods, 52/52 exact ABI matches,
and 52/52 exact normalized executable-code matches. The checked-in validator
uses Corretto 11.0.22 and ten explicit hash-pinned compile dependencies; Peer,
Transactor, `core2`, and unrelated AOT implementation JARs are absent. The
licensed Transactor JAR is streamed only after compilation as an out-of-tree
ZIP oracle. Twenty generated classes are raw-byte identical to originals,
which is valid exact recompilation and proves why raw-hash inequality is not an
isolation test. See
`datomic-rev/transactor/reports/stage-1-java-recovery.md`. Stage 1 remains in
progress until every Datomic namespace is strict-green, two merged recoveries
are identical and promoted, and the remaining structural load/surface and
candidate-resource gates close.

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

## Current continuation

**Current stage:** Stage 1 — recover the whole Transactor corpus.

**Proven:** Stage 0 is closed; the full archive and dependency boundary are
hash-bound; every one of 9,682 classes and 11 resources has a deterministic
owner or explicit treatment; and all 85 bundled namespaces have deterministic
exact shipped-source ownership. The apparent three-namespace heap problem is
an identified control-flow bug, not a capacity requirement. The Java-origin
slice is checked in and independently reproduced from 46 sources to all 52
classes with exact ABI/code and an isolated compile/oracle boundary.

**Still open:** Finish the narrow strict decompiler repairs for all seven
known Datomic sentinels and any failures they reveal, rerun the complete merged
source recovery twice, require exact equality, promote its 247-source tree,
and establish the remaining structural load/surface and candidate-resource
gates. The newest decompiler changes also need the complete unchanged Peer
manifest regression. No recovered Transactor runtime is claimed yet.

**Next direct action:** Make every known affected namespace strict-green,
rerun the full 142-namespace Peer regression, then execute two independent
canonical recoveries of 162 decompiled Datomic plus 85 exact shipped sources
and compare/promote their manifests.

## Goal completion

Goal 2 is complete only when the recovered Peer and recovered Transactor can be
studied and exercised together against PostgreSQL through the authoritative
write, log, index, transport, and HA paths, with the remaining ranked
components recovered or explicitly bounded as described above. A successful
Transactor boot, a single transaction, or recovery of only the 45 unique
namespaces is not completion.
