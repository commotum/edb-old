# Independent-reader campaign

`examples/read_load.rs` runs bounded real Datalog work in separate OS processes:
1, 2 and 4 ordinary readers, one writer, and (during each mixed phase) one
additional analytics reader. It is a repeatable measurement example, not a
performance acceptance threshold or a replacement for the application lifecycle
checks. Its account/key, balance and payload workload follows `scale_workflow`.

Build once, then run that same optimized executable for both sizes:

```sh
cargo build --release --example read_load
sha256sum target/release/examples/read_load
ATOMIC_POSTGRES_URL='host=... user=... dbname=...' \
ATOMIC_POSTGRES_TRANSPORT=plaintext \
ATOMIC_CONNECT_TIMEOUT_MS=5000 \
ATOMIC_STATEMENT_TIMEOUT_MS=30000 \
ATOMIC_LOCK_TIMEOUT_MS=10000 \
target/release/examples/read_load --records 2048 --duration-ms 1500
# Repeat with --records 4096; preserve the other settings and executable.
```

The URL must authorize creation of a disposable schema and two login roles.
The coordinator explicitly migrates that schema, grants the standard restricted
writer/peer privileges and creates its database. It removes only its generated
schema and roles on normal completion; it never restarts PostgreSQL, flushes OS
caches or runs global GC. A killed coordinator can leave the printed schema and
its `_w`/`_p` roles for explicit operator cleanup. Keep credentials out of command
arguments and recorded output. Verified TLS and the usual environment I/O
settings work through the shared configuration; SSD-cache settings must be unset
for this RAM-cache campaign. Administration uses a separately measured process.

Each record contains three datoms: a unique indexed integer key, an integer
balance, and a distinct deterministic 512-byte ASCII payload. Seed batches have
128 records. A canonical consolidation finishes before readers open; seed data
is not merely an unindexed recent tier. The default payload total alone is
1 MiB (2 MiB at 4096 records), above each reader's 32-entry/256 KiB decoded-node
cache allowance. Native roots, schema, recent novelty, query results, runtime
threads and sample vectors are **outside** that cache allowance. The writer has
a 256-entry/4 MiB node cache and 2/8 MiB recent-index threshold/maximum.

## Work and measurements

- **Cold:** fresh reader processes wait at a common open barrier, open native
  connections, then execute an actual keyed query. This is peer-RAM-cold, with
  PostgreSQL/OS caches uncontrolled and already exercised by seed/indexing.
  The 1/2/4 samples describe this burst; their p99 is not a statistically useful
  startup service-level objective.
- **Warm:** each reader first warms four keyed Datalog queries, then repeatedly
  captures a database value and executes those queries. The writer remains live
  but idle, including lease and background work in its measurements.
- **Mixed:** those ordinary readers continue while an extra process repeatedly
  reads the complete payload relation and verifies its row count/byte sum. The
  writer commits ordinary key-lookup balance updates, waiting 20 ms after each
  completed transaction. Commit timing excludes that pacing delay. This is a
  closed-loop mixed workload, not an open-loop saturation/queueing benchmark.

Every operation's monotonic-clock latency is retained in a bounded vector
(200,000 samples / 1.6 MB raw sample storage per reader) and merged for exact
nearest-rank p50/p95/p99 at microsecond resolution. `sample_cap=1` explicitly
reports an early stop at this bound. Throughput uses the coordinator's elapsed
phase window, including barrier delivery and collection, so it does not hide
coordination overhead. `_ordinary`, `_analytics` and `_writer` summaries have
separate latency populations; `_all` populations mix operation kinds and are
useful primarily for aggregate costs, not a query latency claim.

`foreground_sql` comes from an explicit Query operation context. `sql` is the
whole-process driver-call count, including connections' observation, pins,
writer lease maintenance, indexing and notification publication. Connection
handshakes and control calls are also shown separately. `attribution` contains
`OperationKind:calls:errors:elapsed_us`; parent/child attribution is inclusive,
so do not sum those categories to derive total physical calls. SQL timing is
additive driver-call wall time, not PostgreSQL CPU, packets or round trips.
`result_bytes` counts known decoded result-cell bytes, not full network bytes.

Phase windows intentionally exclude open/warmup/shutdown gaps. The final
`all_process_lifetimes` sum includes those gaps, every reader and the writer;
the separate `controller_setup_cleanup` line includes migration/setup/cleanup
SQL. Do not add overlapping phase counts to these lifetime counts. As with any
process snapshot, final worker teardown after the last published snapshot is
not measured; normal service/connection shutdown is explicitly joined first.

CPU is `getrusage(RUSAGE_SELF)` user+system across all process threads, excluding
PostgreSQL backend/OS CPU. RSS is a Linux `/proc` snapshot after each phase;
summed RSS snapshots are not a synchronized peak. Summed process high-water
marks are not the simultaneous aggregate peak either. Both include the harness
and allocated query/sample buffers. Cache hits/misses/evictions and accounted
peak bytes are reported separately, with semantic result checks, cache bounds
and zero compatibility materializations enforced. No latency or scaling ratio
is an assertion.

Run these short campaigns with other heavy work paused. More readers can use
more CPU, but still share the host, PostgreSQL, storage, cache and network. The
printed CPU quota/parallelism and recorded host configuration are essential to
interpreting results; this does not prove cross-host linear scale, offline
availability, storage-cold behavior or a production capacity ceiling.
