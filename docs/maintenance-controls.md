# Prefetch and maintenance controls

These optional controls trade background work against CPU and storage contention.
Defaults preserve single-lane hint/index preparation and unpaced maintenance.
They do not change transaction meaning, canonical index roots or receipt identity.

## Advisory transaction prefetch

Set `ServiceOptions.hint_prefetch = HintPrefetchConcurrency { max_workers: 3 }`
when starting a service or standby. The permitted range is 0–8; zero disables
prefetch. The default is one. A process-wide ceiling of eight also includes
workers left draining after a service stops. Saturation skips optional hints.

Each worker has independent database/pin/miss lanes and claims a different
prefix. The existing `HintPrefetchOptions` prefix, datom, byte, timeout and
cancellation allowances are shared by the request, not multiplied by the worker
count. Datom admission is exact; the byte check permits at most one in-flight
datom per active lane beyond the remaining allowance. This bounds useful read
work, not driver buffers or process RSS. A queued transaction still uses its own
authenticated current database; stale, canceled or erroneous hints are advisory.

The transaction never joins these workers before acknowledging its result.
Cancellation prevents further work, but does not forcibly interrupt synchronous
connection or SQL calls. `HintExecution::snapshot()` may continue changing after
acknowledgement. `peak_active_workers` measures overlapping worker lifetimes;
`prefetch_nanos` and `overlap_nanos` sum lane intervals, not end-to-end wall time.
Use processing latency and actual I/O alongside them; more lanes can be slower
or consume extra I/O on an already warm workload.

## Incremental index preparation

`ServiceOptions.index_preparation_parallelism` and
`PostgresIndexer::with_index_preparation_parallelism(workers)` accept 1–8,
defaulting to one. They prepare the eight current/history physical-index edit
streams in bounded groups of that width. Sorting and canonical edit preparation
can overlap; mutable SQL/path-cache access, tree merges and publication remain
serial in their original deterministic order. Every worker is joined before
the group is consumed, including on error.

Only the selected group's prepared edits coexist. Raising the setting can
therefore increase temporary memory in proportion to the admitted transaction
delta and group width. This is not a total-memory limit. Initial full builds,
AVET backfill steps, compressed-node encoding and database connection counts
are not parallelized by this setting. It is useful when incremental edit
preparation consumes meaningful CPU, not when SQL or upload dominates.

`PostgresIndexer::tree_store_stats()` exposes preparation groups, workers
started, observed peak overlapping workers and summed group-wall nanoseconds,
alongside existing upload counters. Peak overlap is not CPU utilization. Compare
the same input's preparation time and complete publication costs before choosing
a larger setting; no speedup is promised.

## Cooperative maintenance pacing

`MaintenanceControl::new(pause, cancel_flag)` provides a cloneable cancellation
flag and a pause after each completed batch. Attach it with
`PostgresTreeStore::with_maintenance_control`,
`PostgresIndexer::with_maintenance_control`, `PostgresOperator::with_maintenance_control`, or the backup operator's matching
method. An external bounded maintenance loop can call `check()` before work and
`after_batch()` after each successful committed/copied batch. Call it outside SQL
write transactions and exclusive storage locks. Backup may retain its read-only
consistent snapshot and generation pin. Clones share counters and cancellation, but each
caller pauses independently: this is not a global bandwidth limiter.

Pacing checks cancellation between short sleeps. Existing I/O and CPU work within
one batch keep their original interruption rules. Cancellation returns
`maintenance/canceled` without undoing completed work. Tree upload cancellation
can leave valid staged nodes, which an ordinary retry reuses; it does not publish
an incomplete root. Existing build-intent pins continue protecting the upload
while its owner is active. Durable publication and content validation are unchanged.

`MaintenanceStats` records completed batches, pauses and actual pacing wall time.
A batch is counted even when cancellation is noticed immediately afterward.
Zero pause performs no sleep; omitting the control preserves the original path.
The pause is applied after the final batch too, before any following publication.
For GC, use it between already bounded, committed collector calls rather than
holding a collector transaction asleep. A pause reduces scheduling pressure; it
does not impose a bytes-per-second guarantee.
