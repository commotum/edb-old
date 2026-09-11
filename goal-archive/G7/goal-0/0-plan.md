# Goal 0 — Datomic-style storage: first-release cutover

## Objective

Replace Atomic's relational storage-engine implementation with an idiomatic Rust
engine over a small PostgreSQL storage interface. Store the transaction log and
indexes as immutable block structures; coordinate their publication through
conditional updates to durable root references. Rust understands database
structures and owns their evolution, validation and maintenance. PostgreSQL owns
durability and the atomic storage primitives, not database-engine policy.

This is a full first-release cutover, not another backend option or migration
squash. Preserve the useful product and its documented semantics while removing
the superseded SQL machinery, Rust paths, tests, fixtures and operational guidance.

## Authority and scope

- `/home/jake/Developer/atomic/datomic_pro_docs` is semantic authority, especially
  `04_transactions/05_acid.md`, the index/background-indexing chapters, and the
  storage, availability, backup and excision documentation.
- `/home/jake/Developer/atomic/1.0.7705` is architectural/algorithmic evidence.
  Start with `datomic.kv-store`, `kv-sql`, `sql`, `kv-cluster`, `cluster`, `log`,
  `catalog` and index/peer machinery. This is recovered source, not a requirement
  for byte equivalence, original source formatting or a runnable JVM oracle.
- PostgreSQL is the only production storage provider. A small internal storage
  protocol is part of the intended architecture, not a project to support other
  backends. Do not repeat the archived prohibition against that boundary.
- Translate in spirit: use native Rust ownership, types, error handling and
  concurrency. Do not reproduce Clojure/JVM infrastructure or force an exact
  table count, protocol arity, block size or tree depth.
- No deployment requires compatibility. New database, block, program and backup
  formats may require fresh databases. Do not build old-format readers, upgrade
  migrations, historical-executable tests, rolling-upgrade support, rollback
  converters or a supported dual-engine mode. Reuse good existing codecs when
  useful, but old bytes and SQL layouts are not preservation obligations.
- First-release freedom does not authorize erasing existing databases. Use fresh,
  explicitly isolated development/test targets; do not reset unrelated data.
- Archived goals are evidence of decisions and mistakes, never active requirements.
  Preserve the Datomic source/docs corpora and unrelated user work.

## Architectural acceptance boundary

The storage interface exposes opaque immutable objects and revisioned references,
with the bounded bulk/read/enumeration operations actually needed by the engine.
Reference contents describe engine-owned roots; PostgreSQL must not interpret
transaction, receipt, index, program, fulltext, excision or reclamation semantics.
Creation, conditional replacement and conflict/unknown-outcome handling have
explicit contracts. Objects required by an acknowledged publication are durable.

SQL transactions, conditional updates, ordinary indexes/constraints, and narrowly
justified generic locking, permissions or notification primitives are acceptable.
Engine-specific stored procedures, trigger state machines and feature-specific
relational ledgers are not the destination. Moving the same relational state
machines into Rust SQL strings, or hiding them behind a trait, does not count.
Any remaining SQL safeguard must protect a generic storage property; explain its
purpose briefly. Retain sensible least-privilege and transport protection without
requiring SQL to distrust and independently reimplement the entire Rust engine.

Design root publication, writer fencing, reader/build protection and reclamation
together at the foundation. A separate lease check followed by an unfenced root
update is not sufficient. Concurrent index adoption must not discard newer log
entries; unreachable staged objects must neither become visible nor endanger live
objects. Immutable captured values remain stable under the documented retention,
`noHistory` and excision rules. Do not invent stronger erasure or retention promises
for already exported data, or silently weaken current guarantees to simplify GC.

## Starting evidence and preservation boundary

The consolidated baseline at the start had 68 tables, 92 functions and 150 explicit
triggers, including two constraint triggers. Consolidation removed upgrade steps,
not the architectural substitution. `goal-archive/G1/goal-3/DURABILITY.md` records
the early substitution; the archived parent both requested recognizable Datomic
structure and prescribed a relational destination. This plan supersedes the latter.

Use this initial drift map to orient work; Stage 1 reconciles it with actual code:

| Area | Current coupling to replace | Intended owner |
| --- | --- | --- |
| Log, heads, requests, allocation | `postgres.rs`, `native_log.rs`, `log_generation.rs`, per-feature SQL records | Rust log/root structures and atomic publication |
| Indexes, commitments, fulltext | `tree_store.rs`, `persistent_commitment.rs`, `fulltext_store*.rs`, SQL publication/membership logic | Rust persistent structures, incremental jobs and shared block storage |
| Exact receipts and retained bases | `receipt_archive*.rs`, relational reference/conversion ledgers | Rust receipt identity, reachability and retention |
| GC, excision, catalog lifecycle | SQL maintenance dispatch/guards, `operations.rs`, `database_catalog.rs` | Rust lifecycle/reclamation algorithms over the same roots and primitives |
| Backup and restore | `backup*.rs`, SQL generations, build and activation records | Portable immutable objects, Rust verification and conditional activation |
| Application connectivity | Peer/service/consumer SQL assumptions | Existing user-facing workflows using the new storage path |

Preserve the working value/transaction/query engine and current usable capabilities:
EDN and typed APIs; identity, schema-as-data, partitions and allocation; transaction
functions and persisted/native program behavior; immutable/time/history/speculative
views; Datalog, Pull, navigation and index access; fulltext; captured references;
local/TLS remote applications; async reads, synchronization and change consumers;
backup/offline reads/restore, inspection, GC, excision and catalog lifecycle.
Use `docs/acceptance.md` and focused source/tests as an inventory, not as a mandate
to preserve every old implementation assertion or optional historical exercise.

Current-version durability, restart recovery, fenced failover and receipt-first
exact retries remain requirements. Valid matching retries retain their committed
identity, tempids, transaction data and exact before/after values under the existing
documented lifecycle rules; conflicting input cannot reuse the key. Unknown outcomes
must be resolved, not reported as definite failures or arbitrarily resubmitted.
Keep selective reads and incremental updates: the cutover must not introduce
whole-database loading/replay into ordinary opens, reads, retries or small writes.

## Parent execution

Stages 1–7 map to root-level `goal-1` through `goal-7`. On execution, reconcile and
resume the matching existing child, or use `$scaffold-goal` to create its three
files when missing. Keep one child active. Children may break down their own work
but must not create grandchildren or corrective parents. Execute each child, then
return here; scaffolding alone does not complete a stage.

Temporary development overlap is acceptable only while a named stage replaces a
subsystem. Delete displaced implementations and tests as ownership transfers;
there is no requirement to preserve a runnable legacy backend at each step. All
stock entry points must use the new path at completion, without hidden fallbacks.

## Ordered stages

### 1. Storage boundary and root model

**Status:** Complete — `goal-1`; real object/ref PostgreSQL foundation verified.

**Outcome:** A small real PostgreSQL object/reference implementation and a coherent
Rust-owned publication, fencing and reachability model replace the old design as
the foundation for subsequent stages.

**Focus:** Reconcile the drift map and capability inventory; make concise keep,
replace and remove decisions. Define log/index/receipt/catalog roots, publication
authority and reader/build protection before constructing separate feature paths.
Implement fresh installation and the generic storage primitives with native codecs,
lossless values, authentication and explicit conflict/failure behavior.

**Completion signal:** Actual PostgreSQL checks exercise object round trips,
immutable-key conflicts, reference creation/CAS races and durable reopen. The plan
records the selected root/fence/retention model and how each displaced responsibility
will move to Rust. A design document or facade over the old schema alone is not done.

### 2. Engine-owned database values and reads

**Status:** Complete — `goal-2`; actual block-backed consumer/selectivity checks passed.

**Outcome:** Rust reconstructs immutable database values from the new roots and
block structures without the old relational log/index catalogs.

**Focus:** Persistent log and covering-index structures, current/history ordering,
schema and allocation metadata, recent-log merging, shared caches and captured
bases. Adapt existing query, Pull, navigation and speculative consumers to the
shared reader. Reuse proven algorithms rather than building a second query engine.

**Completion signal:** New-store fixtures support stable historical/captured views,
selective log/index access, queries and branching through existing public consumers.
Corrupt/missing reachable data fails clearly. Read costs demonstrate selective
loading on data larger than configured caches, not full replay disguised as laziness.

### 3. Serialized publication, exact receipts and recovery

**Status:** Complete — `goal-3`; actual service, receipts, fencing and recovery verified.

**Outcome:** The real transactor durably publishes coherent engine-owned state on
the new store, including exact request outcomes, and recovers after failure.

**Focus:** Receipt-first admission, pure transaction assessment, schema/program
validation, identity/allocation state and immutable log growth. Bind writer authority
to publication, preserve ordering/acknowledgement semantics, and reconnect existing
service/submission paths to this single writer. Do not use an eager temporary engine
or relational request tables to close the stage.

**Completion signal:** Real application transactions, matching/conflicting retries,
lost acknowledgements, concurrent/stale writers, writer death/replacement and reopen
behave correctly on the new store. Small writes and retained/reopened receipt reads
have measured complete-path costs; normal recovery uses the appropriate persisted
basis and tail, with full-log verification reserved for explicit deep inspection.

### 4. Incremental indexes and live applications

**Status:** Complete — `goal-4`; incremental indexing and live applications verified,
including the final shared read-observation publication-race repair.

**Outcome:** Background work and peer applications use the same new storage engine
while transactions continue, without SQL publication/membership state machines.

**Focus:** Rust consolidation, structural sharing, atomic index adoption, fulltext
pages/dependencies, index readiness/fallback, backpressure and cache/prefetch behavior.
Preserve local and remote connectivity, async consumption, peer synchronization,
endpoint/failover behavior and durable transaction consumers using small generic
storage/notification primitives rather than new specialized SQL catalogs.

**Completion signal:** Separate applications observe coherent values through
concurrent writes, indexing and writer restart. Fulltext and temporal/program query
paths work; incremental work scales with the affected data, retained handles remain
valid, and warm resident queries demonstrate the intended peer-local behavior.

### 5. Retention, reclamation and lifecycle

**Status:** Complete — `goal-5`; actual lifecycle/GC/excision/operator checks passed.

**Outcome:** Rust owns safe, resumable reachability and lifecycle operations; SQL
no longer runs garbage-collection, excision or database-retirement workflows.

**Focus:** Shared object reachability, reader/build/receipt protections, program and
fulltext dependencies, abandoned work, retention and documented excision behavior.
Implement catalog creation/rename/retirement/name reuse without redirecting existing
identities. Use the foundation's protection/publication protocols, including hooks
needed by backup/restore, instead of accumulating new per-feature control tables.

**Completion signal:** Concurrent readers, writer/index publication, retained retries
and protected builds survive interrupted/resumed collection and lifecycle work.
Eligible garbage is actually reclaimed in bounded work; reachable data is not.
Excision and name reuse preserve their documented meaning and stale workers cannot
publish or reclaim after losing authority.

### 6. Backup, restore and administration

**Status:** Complete — `goal-6`; actual portable backup, restore and administration verified.

**Outcome:** Current-format portable backup, selective offline reads, restore and
administration operate entirely over the new block/reference engine.

**Focus:** Coherent capture including a newly created database, protected roots,
streaming copy and authentication, explicit deep verification, resumable restore
and conditional destination activation. Preserve operator controls, transport/role
safety, readable diagnostics, bounded input admission and non-blocking async cleanup.
Do not retain the old schema or backup reader as a recovery escape hatch.
Port the deferred backup-specific regressions in `catalog_backup`,
`reclamation_backup` and `fulltext_backup_restore`; paused/headless restore must
retain prestaged native program dependencies and reject stale activation. These
behaviors are not proved by the normal held-value GC tests.

**Completion signal:** Actual CLI/library capture, offline query, verification,
interrupted/retried restore and post-restore exact retries work on fresh targets.
Malformed input is rejected before unreasonable allocation; ordinary backup-backed
opens/reads remain selective. Measurements include capture, verification, restore
and cleanup—not only convenient inner loops.

### 7. Full cutover, cleanup and integrated acceptance

**Status:** Complete — `goal-7`; full cutover, cleanup and integrated current-version
acceptance established, including reopened owning-stage repairs.

**Outcome:** One first-release product remains: the new Rust storage engine over
thin PostgreSQL storage, with no supported or hidden old implementation.

**Focus:** Finish routing all stock typed/EDN, file/stdin, local/remote application,
service and administrative entry points. Remove superseded migrations, SQL routines,
Rust modules/branches, dependencies, binaries/helpers, fixtures and misleading docs.
Delete old-format/upgrade/SQL-shape tests; port the meaningful behavior and failure
regressions, consolidating duplicates. Failing product behavior is not grounds for
deleting a test. Keep source/docs evidence and unrelated work intact.

**Completion signal:** A fresh installation and the integrated current-version
application/operations workflow pass, including real PostgreSQL crash/WAL recovery,
writer failover, publication races, exact retries, backup/restore and GC interactions.
The final SQL/source review confirms engine policy is in Rust, no feature-specific
SQL ledgers/state machines or old-path fallbacks remain, and the capability inventory
is accounted for. Representative larger-than-cache runs report end-to-end latency,
memory, storage calls/bytes and write/maintenance amplification without claiming
unmeasured scale. Removed files and remaining limits are reported plainly.

## Verification and finish line

Use existing useful tests and evolving application fixtures throughout the stages.
Prefer targeted regressions during implementation and integrated runs at meaningful
boundaries; do not repeatedly run historical matrices. PostgreSQL tests that skipped
are not evidence of execution. Restart tests use a dedicated disposable server.
Choose representative workloads and honest budgets from measured behavior; there is
no mandated historical executable comparison, universal performance SLA or exhaustive
decompilation-equivalence campaign.

Both behavioral correctness and architecture must pass. Fewer SQL lines, a new
trait, one completed child, an in-memory demo or updated documentation cannot close
this parent. Reopen the owning stage for integrated gaps; do not exclude a current
core capability or reclassify old SQL machinery as mandatory merely to finish.

## Continuation

Complete: all seven stages and integrated parent acceptance are established.
No child remains active and no known core gap is deferred. Child plans retain
implementation decisions; `docs/acceptance.md` records the current evidence,
reproduction prerequisites and operating limits.

The fresh PostgreSQL schema contains only `atomic_objects` and `atomic_refs`,
with zero policy functions or user triggers, verified on PostgreSQL itself.
Rust owns the log/index structures, receipt-first publication, writer fencing,
reader/build protections, retention/GC, excision, lifecycle and backup/restore.
Superseded SQL, legacy engine/readers, compatibility paths and obsolete fixtures
are removed; useful regressions and shared Rust algorithms remain. The Datomic
reference corpora are unchanged and unrelated databases were not reset. Deleted
tracked files remain recoverable from Git history, not from a runtime fallback.

Integration required real repairs, not scope exclusions: canonical signed-zero
reports, program caching/protected deployment, cache accounting and bounded uploads,
operator index controls and lease-safe publication, generation-spanning report
handoffs, bounded excision observation, idle pin cleanup across GC, and shared
bounded initial-pin observation for exact reports/references/inspection/backup/
receipt lookup. The last helper retries only transient capture conflicts; it never
resubmits a transaction or relaxes authority/corruption checks. Ordinary retained
values do not keep an immortal chain of future-report handoffs alive.

Verified final evidence: the full library run passed 438 tests (324.76 s), followed
by the five-test protection group (4.45 s), including two new deterministic
publication-race/bounded-retry tests. This is not a full 440-test run. The final
10,000-entity snapshot target passed all ten tests (198.61 s). Sixteen other
affected targets passed; the final nine-target application/operations rerun passed
all 20 tests, including isolated TLS, automatic failover, backup roles/CLI,
inspection, exact outcome lookup and serialized references. Earlier broad runs
failed and one large fixture was interrupted; these failures were repaired and
the affected paths rerun, not counted as passes. Five optional manual-cost tests
remain opt-in, not acceptance evidence.

Actual immediate-shutdown/WAL recovery passed on our disposable cluster: redo,
acknowledged exact retry, uncommitted-reference rollback, retained values/log,
standby takeover and a successor write (518 ms restart, 32.81 s complete fixture).
Native and separate-process stock applications also passed. Strict all-target
Clippy, formatting, rustdoc and final source/schema checks passed; the doctest
command contained zero tests and is not application evidence.

Measured limits: the standalone 2,048-entity live workflow completed startup through
shutdown in 14.950 s, with 71,388 KiB process peak RSS, 9,782 driver calls,
20,949,013 payload bytes read and 8,952,849 written; 32 warm reads took 630 µs
with zero SQL. Selective open/query/speculation read about 470 KB of a 13.64 MB
index in 272–285 ms. Full fixture setup is separately reported, not sold as native
ingestion throughput. Fixed publication/retention cost remains substantial: these
debug/local results establish measured behavior, not a production capacity SLA.

The parent loop has reached its finish line. Preserve this completed state; future
work should start from a new user objective or a concrete reproduced regression,
not recreate the removed compatibility obligations or repeat an open-ended audit.
