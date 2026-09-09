# Running the native acceptance deployment

The supported deployment demonstrated here is Linux, PostgreSQL, one Rust
transaction service and independent same-user peer processes on the same host.
PostgreSQL may be remote through the configured, verified-TLS API. The examples
below use explicitly supplied development PostgreSQL connections; they are not
a TCP transaction service or a PostgreSQL HA manager.

Goal 6's plan is the record of completed runs and workload-specific measurements.
Until that plan closes, these drivers are acceptance work in progress, not an
unqualified production/scale claim. Read [operations.md](operations.md) for
roles, transport policy, upgrade, unknown outcomes, retention and excision.

## Build and provision

Use disposable, separately provisioned PostgreSQL catalogs and an object-owning
test account. Normal applications use the restricted runtime roles described
in the operations guide. Keep any configured passwords out of output/logs.

```sh
cargo build --offline --release --examples
```

Examples deliberately fail when their required connection environment is
missing. The scale driver calls the migrator and creates a uniquely named
logical database. It leaves that data in PostgreSQL for maintenance/recovery
checks; it does not drop a previous database. Use distinct catalogs for source
and restore. Never run crash/GC examples against shared or production systems.

## Independent writer and peers

```sh
ATOMIC_POSTGRES_URL='host=/private/socket port=55432 user=atomic_test dbname=atomic_goal6_scale' \
ATOMIC_SCALE_RECORDS=100000 ATOMIC_SCALE_BATCH=100 \
ATOMIC_SCALE_REQUIRE_CACHE_EXCEEDED=1 \
  target/release/examples/scale_workflow
```

The parent launches a writer and two long-lived submitting peers. Records have
a unique integer key, indexed category, balance and 256-byte payload. Peers
stream batches, retain earlier values and perform local query/Pull checks.
The parent exercises a persisted balance-update function, pure speculative
execution, time/history values, actual writer SIGKILL, fenced replacement,
identical-key replay and writer-offline reopening. SIGKILL only targets the
child process created by this run.

The driver reports declared records/facts/payload bytes, canonical log bytes,
batch latency percentiles, elapsed throughput, actual per-process RSS/high-water
memory, cache/recent accounting, background jobs and physical node-read bytes.
Its default settings are a 4MiB writer node cache, 1MiB peer caches, and 2MiB/8MiB
recent scheduling/backpressure thresholds. A single admitted transaction can
cross the pressure threshold before subsequent admission is parked. Application
values, schema, resident roots, indexing jobs and allocator overhead are separate
from node-cache byte limits. Report queues are opt-in/unbounded and are not used
as an accumulating event archive by this workload.

`cache_exceeded=true` requires measured canonical log bytes greater than all
three configured cache/recent budgets. It does not assert the database exceeds
physical machine RAM or establish a deployment-independent throughput SLA.
Cold/warm query counters describe actual immutable-node reads, not PostgreSQL
buffer-cache misses, disk IOPS or network protocol overhead. The final `PASS`
line provides the logical database ID/basis needed below.

For ordinary library applications, `Connection::connect_with_cache_limits` and
`connect_configured_with_cache_limits` expose independent entry/byte policy;
`cache_stats`, `recent_stats` and `load_stats` expose its effects. Opening a
connection does not start or own a writer. Service capacity and background
indexing settings belong to the independently managed transaction service.

## Portable operations on the same workload

Stop the source writer first (the scale driver does this). The restore endpoint
must be a separate catalog/schema; to run the crash workflow afterward, put the
restore catalog on a dedicated disposable PostgreSQL server.

```sh
ATOMIC_POSTGRES_URL='host=/source/socket port=55432 user=atomic_test dbname=atomic_goal6_scale' \
ATOMIC_RESTORE_POSTGRES_URL='host=/restore/socket port=55434 user=atomic_test dbname=atomic_goal6_restore' \
ATOMIC_DATABASE_ID='ID_FROM_SCALE_PASS' \
  target/release/examples/operations_workflow
```

Each phase runs in a fresh process for meaningful RSS/high-water attribution:
bounded-cache native full scans/fingerprints, first and differential backup,
deep semantic verification, restore, independent current/history/query checks,
original controlled-request replay, and deep inspection. It also checks that
the source did not change during the workflow. Repeated unchanged backup must
reuse immutable objects; restored facts/history must match canonical fingerprints.

Portable backups and semantic verification are broad operations. Deep replay
can retain current/history information; comparing retained request archives can
add substantial work. Do not compare these memory/latency costs to a selective
peer read or silently infer constant-memory administration. The driver reports
repository bytes/objects and per-phase cost, and retains its private backup
directory. `ATOMIC_BACKUP_DIRECTORY` selects an explicit repository if desired.
Retained backups contain subject data; apply your independent retention policy.

## Crash PostgreSQL and recover acknowledged information

This driver requires an existing restored workload and explicit destructive-
availability opt-in. It verifies the connection's actual `data_directory`,
requires a directory below the system temporary directory, and checks `fsync`,
`synchronous_commit` and `full_page_writes` are on. Supply the exact startup
options for that dedicated server, including any external configuration path.

```sh
ATOMIC_ALLOW_DISPOSABLE_PG_CRASH=1 \
ATOMIC_RESTART_POSTGRES_URL='host=/restore/socket port=55434 user=atomic_test dbname=atomic_goal6_restore' \
ATOMIC_DATABASE_ID='ID_FROM_SCALE_PASS' \
ATOMIC_RESTART_POSTGRES_DATA='/tmp/DEDICATED_FIXTURE/data' \
ATOMIC_RESTART_PG_CTL='/absolute/path/to/pg_ctl' \
ATOMIC_RESTART_POSTGRES_LOG='/tmp/DEDICATED_FIXTURE/server.log' \
ATOMIC_RESTART_POSTGRES_OPTIONS='-c config_file=/tmp/DEDICATED_FIXTURE/postgresql.conf' \
  target/release/examples/restart_workflow
```

The test commits a marker with a live writer, immediately stops PostgreSQL
without a clean checkpoint, restarts it, replaces the writer, replays the exact
acknowledged request and commits a successor. Captured native values, a bounded
sample of workload facts, old log data, history/Pull and cache-cold reopening
must agree. `db()` stays local while storage is unavailable; uncached reads
still require storage. The fixture attempts to restart its server even when a
later check fails. This changes server availability and adds two marker
transactions; it does not delete database files.

Reported recovery latency is for the declared local server/workload, not a
network partition bound. OS/TCP settings and SQL statement deadlines have the
separate limits described in the operations guide. Fenced writer replacement
does not implement PostgreSQL replication, quorum failover or off-site recovery.

## Reclaim obsolete storage while retaining exact information

Run this after the source consistency checks above, because it adds one marker
transaction. Every writer in the selected installation must be stopped. The
driver requires explicit zero-retention opt-in and checks that the actual
PostgreSQL catalog name begins with `atomic_goal6_`. Global GC affects all logical
databases in that installation schema; isolate it or report the other fixtures
included in its costs. Never substitute production connection settings.

```sh
ATOMIC_ALLOW_DISPOSABLE_GC=1 \
ATOMIC_POSTGRES_URL='host=/source/socket port=55432 user=atomic_test dbname=atomic_goal6_scale' \
ATOMIC_DATABASE_ID='ID_FROM_SCALE_PASS' \
ATOMIC_GC_MAX_BATCHES=4096 ATOMIC_GC_WALL_SECONDS=1800 \
  target/release/examples/gc_workflow
```

The driver consolidates a successor, runs bounded GC first with an old snapshot
held and then after releasing it, and validates cache-disabled old/current/history
samples and an independent reopen. It reports actual installation row/payload
counts, deletions, receipt-archive conversion work, I/O, per-batch time, elapsed
time and RSS/high-water memory. Zero eligible work must also include completed
publication folds; an empty physical-deletion list alone is not completion.

Batch and wall budgets apply separately to the two windows. The wall budget is
checked between calls, not as forced SQL/Rust preemption. A cap returns an
incomplete result rather than claiming all reclamation finished. Native receipt
archives retain required immutable nodes, and total conversion authenticates
each distinct retained physical closure. Report those costs separately from
ordinary transactions. Production normally uses the documented30-day horizon,
not the zero age chosen for this disposable experiment. PostgreSQL space reuse
and returning relation space to the OS are different from deleting Atomic rows.
