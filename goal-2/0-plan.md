# Goal 2 — Efficient Immutable Storage and Native Reads

## Objective and constraints

Deliver Stage2 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: measurable,
independently cached native reads, efficient authenticated immutable block/index
I/O, snapshot statistics and finite asynchronous index requests. The parent owns
all capabilities, invariants and integrated acceptance; this is the sole active
child. Use local Datomic docs as semantic authority and 1.0.7705 as architectural
evidence. Preserve the engine, formats/migration checksums, exact retries,
restricted roles, retained values, authenticated root-last publication and all
Goal1 repairs. PostgreSQL remains authoritative; SSD data is disposable.

Reuse the supported CLI/application, existing pure/native oracles and fault hooks.
No alternative store, replacement test framework, recursive children or hidden
scope exclusions. Account for metadata/pin/health/background SQL, not just node
reads. Scans may scale with data; measurements, not arbitrary timing gates,
determine cost claims. Deeper transaction pipelining is the only conditional
implementation: decide it from measured phases.

## Stages

### 1. Observe operations and decouple cached reads

**Status:** In progress.

**Outcome:** Total SQL and transaction-phase costs are attributable; immutable
cache hits do not wait for storage health or unrelated cold misses.

**Focus:** Nested/concurrent operation contexts, metric callbacks, foreground/
background attribution, cache/pin separation, bounded coalesced storage misses.
Retained-value authentication and GC safety remain explicit when sessions fail.

**Completion signal:** Warm native queries issue no foreground SQL and work
during temporary storage loss within supported retention; unavailable cold data
fails safely. Slow misses do not block unrelated hits. Pin loss/reacquisition,
GC and restart preserve exact old values. Repeat the application with declared
total-I/O/phase measurements before claiming improvements.

### 2. Version physical blocks and add bounded SSD reuse

**Status:** Pending.

**Outcome:** Compression reduces suitable physical payloads while preserving
canonical content identity; optional bounded SSD caching safely reuses blocks.

**Focus:** Old/new format decoding, authenticated canonical hashes, bounded
decompression, access/format separation and cache corruption/eviction/fallback.
Explicit restart, retention and excision policy; no new durable authority.

**Completion signal:** Old and compressed blocks survive reopen/backup/restore;
malformed or oversized payloads fail safely. SSD disabled/enabled, corruption,
eviction and restart reuse pass. Measure CPU, memory and transferred/cache/stored
bytes on compressible and poorly compressible inputs; distinguish native payload
size from PostgreSQL disk allocation.

### 3. Batch indexing and complete maintenance APIs

**Status:** Pending.

**Outcome:** Verified bounded uploads and useful overlap reduce index I/O; users
obtain snapshot statistics and request finite asynchronous indexing.

**Focus:** Lazy finite-target indexing, durable blocks before published roots,
bounded concurrency, db_stats using indexes and request_index/sync_index semantics.
Retain cancellation and report time/filtered-view scan costs.

**Completion signal:** Interrupted/conflicting uploads cannot publish missing
blocks; backup/restore/GC remain correct. Measure round trips, throughput and
peak memory. Real PostgreSQL checks establish history/per-attribute statistics
and finite index targets while newer transactions arrive.

### 4. Verify fault schedules and settle the pipeline decision

**Status:** Pending.

**Outcome:** Storage changes have generated/replayable/reducible failure evidence
and a measured ordered-transactor pipeline decision.

**Focus:** Grow existing trace and fault support alongside risky changes. Add
automatic reduction; a controlled failing fixture may prove reduction. Use phase
measurements to implement justified pipeline overlap or retain the current path.

**Completion signal:** Seeded storage failures replay and reduce, relevant
regressions and evolving application pass on actual PostgreSQL. If pipeline
changes, dependent failure, ordering, acknowledgement and unknown-outcome checks
pass; otherwise retain the measured rationale. Fold results into Goal0 and
continue its first unfinished child, not another parent or audit phase.

## Continuation

Started 2026-09-09 after Goal1 acceptance. First action: total operation/phase I/O
attribution and inspect native cache/pin coupling. Other stages remain required.
Use the isolated test cluster only for failures; preserve existing retained data.
