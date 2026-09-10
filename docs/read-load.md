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
for read_load_records in 2048 4096; do
  ATOMIC_POSTGRES_URL='host=... user=... dbname=...' \
  ATOMIC_POSTGRES_TRANSPORT=plaintext \
  ATOMIC_CONNECT_TIMEOUT_MS=5000 \
  ATOMIC_STATEMENT_TIMEOUT_MS=30000 \
  ATOMIC_LOCK_TIMEOUT_MS=10000 \
  ATOMIC_READ_CACHE_BYTES=1048576 \
  target/release/examples/read_load --records "$read_load_records" --duration-ms 1500
done
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
1 MiB (2 MiB at 4096 records). Each reader has a 32-entry/1 MiB decoded-node
cache allowance. The full scan's encoded-plus-decoded working set exceeds this
allowance at both sizes, demonstrated by repeated cache misses and evictions;
payload length alone is not the cache charge. Native roots, schema, recent novelty, query results, runtime
threads and sample vectors are **outside** that cache allowance. The writer has
a 256-entry/4 MiB node cache and 2/8 MiB recent-index threshold/maximum.
`ATOMIC_READ_CACHE_BYTES` can override the reader allowance (64 KiB..16 MiB).
In particular, 256 KiB is useful for reproducing the oversized-leaf boundary:
repeating a query does not make its working set resident if a single decoded
node cannot fit. `cache_oversized_bypasses` distinguishes this from eviction.

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

## Recorded operating envelope

Measured on September 9, 2026 (Pacific): optimized Rust 1.90.0 / LLVM 20.1.8,
Linux x86-64, PostgreSQL 15.11 over a local Unix socket, AMD Threadripper 2950X
(16 cores / 32 visible logical CPUs), about 126 GiB host RAM, ext4 on an NVMe
device. PostgreSQL had 128 MiB shared buffers and 100 maximum connections.
Other agents paused heavy work for the measured windows, but this was still a
shared desktop host, without CPU affinity/frequency control. The root cgroup
CPU quota file was unavailable; 32 visible CPUs is not a reserved CPU quota.
PostgreSQL/OS caches were **not** flushed. SSD cache was disabled.

Both final sizes used the exact commands above and one executable, SHA-256
`de77f8b2a38124837794a33ce586f53129c1e41dc31428d2bc5e928631977d55`.
The source default was subsequently changed from 768 KiB to the explicitly
measured 1 MiB; the environment override in these commands is unchanged.
Each warm/mixed workload window was requested for 1.5 seconds. No sample cap
was reached. Every result/cache/materialization check passed, SQL errors were
zero, and all generated schemas/roles were successfully removed.

Warm selective queries (latencies in microseconds; SQL counts cover the whole
phase, **not** per query):

| Records | Readers | Queries/s | p50 / p95 / p99 | Query-scope SQL | Reader + idle-writer SQL |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 2048 | 1 | 79,569 | 12 / 12 / 21 | 0 | 21 |
| 2048 | 2 | 104,453 | 13 / 36 / 42 | 0 | 29 |
| 2048 | 4 | 155,234 | 13 / 60 / 67 | 0 | 40 |
| 4096 | 1 | 122 | 7979 / 8966 / 9583 | 1480 | 1495 |
| 4096 | 2 | 237 | 8567 / 9123 / 9813 | 2856 | 2871 |
| 4096 | 4 | 497 | 7979 / 9057 / 11287 | 6000 | 6015 |

The 2048-record four-key working set occupied exactly four cache entries and
854,498 accounted bytes. Its warm windows had **zero misses, evictions or
oversized bypasses**; mixed ordinary queries also performed zero foreground
SQL. This demonstrates independent cached query processing, but not linear
scale: four readers used about four process-core equivalents for roughly 1.95x
the single-reader throughput, with larger tails. PostgreSQL CPU is additional
and unmeasured.

The fixed allowance did **not** hold the selected paths at 4096 records:
four misses and four evictions per repeated query, no oversized bypasses,
and eight SQL calls (four node loads plus foreground pin checks). This is a
measured working-set boundary, not a claimed constant-cost read at every
database size. The cache was deliberately not increased again to hide it.

Two preliminary 2048-record configurations show why fitting the actual block
working set matters:

| Cache allowance | Warm queries/s at 1 / 2 / 4 readers | Foreground SQL/query | Observed behavior |
| ---: | ---: | ---: | --- |
| 256 KiB | 336 / 622 / 1161 | 2 | One miss/query, no evictions; oversized-leaf bypass diagnosis |
| 768 KiB | 145 / 302 / 585 | 8 | Four misses and evictions/query; zero oversized bypasses |
| 1 MiB | 79,569 / 104,453 / 155,234 | 0 | All four selected blocks resident |

The first run preceded the explicit `cache_oversized_bypasses` output field;
its diagnosis follows the retained miss/no-eviction evidence and the cache's
oversized-admission branch, not an invented recorded bypass count. Its binary
hash was `2701c658e84825f69531a1ec4ee8ba77c09cc72fd51122155799eda00d616d6c`.
The 768 KiB run used the final binary. These are capacity-boundary observations,
not a controlled attribution of writer latency changes between builds.

Mixed workload, ordinary-query latency in microseconds. Each row includes one
extra independent analytics process and one paced writer:

| Records | Ordinary readers | Ordinary queries/s | p50 / p95 / p99 | Scans/s | Commits/s | All-process SQL |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 2048 | 1 | 62,536 | 12 / 31 / 35 | 52.72 | 27.02 | 4683 |
| 2048 | 2 | 86,674 | 12 / 56 / 71 | 50.29 | 28.74 | 5223 |
| 2048 | 4 | 137,330 | 13 / 79 / 98 | 52.80 | 27.72 | 5798 |
| 4096 | 1 | 108 | 8857 / 11292 / 15520 | 27.19 | 25.86 | 5897 |
| 4096 | 2 | 219 | 8653 / 11369 / 14720 | 25.04 | 25.69 | 7441 |
| 4096 | 4 | 422 | 8879 / 12249 / 15451 | 22.97 | 26.91 | 10751 |

At four ordinary readers, analytics p50/p95/p99 were
18.411/20.896/25.192 ms (2048 records) and 43.307/51.297/51.743 ms (4096).
Commit p50/p95/p99 were 16.184/17.499/19.899 ms and
16.932/21.393/22.685 ms respectively. Across all mixed rows, analytics
performed six or ten node misses per full scan (2048/4096), with continuing
evictions and no oversized bypasses. Ordinary processing, full scans and the
ordered writer therefore shared real storage/CPU work; a scan was not merely
counting cached handles.

Whole-process resource/cost examples (reader + scanner + writer, excluding
the coordinator and PostgreSQL backend CPU):

| Records, phase | CPU seconds / phase wall seconds | RSS snapshot sum | SQL driver wall sum | Decoded SQL result bytes |
| --- | ---: | ---: | ---: | ---: |
| 2048, warm 4 + idle writer | 6.015 / 1.509 | 104.9 MiB | 0.084 s | 297 |
| 2048, mixed 4 + scan + writer | 7.471 / 1.515 | 124.3 MiB | 1.608 s | 62,596,761 |
| 4096, mixed 4 + scan + writer | 5.857 / 1.523 | 141.3 MiB | 3.200 s | 174,703,959 |

The 2048 warm reader processes individually used about 13 MiB RSS despite the
1 MiB cache allowance. The four-reader mixed high-water sums were 136.9 MiB
and 149.5 MiB; those are sums of process-lifetime peaks, not a simultaneous
resident-memory peak or a configured total-memory ceiling.

Cold open + first-query p50/p95/p99 were 46.390/56.330/56.330 ms for the
2048-record four-process burst (138 total peer SQL calls), and
55.342/66.320/66.320 ms for 4096 (159 calls). Those four-sample tails describe
only these peer-cold bursts against warm shared storage.

Entire campaign lifetimes, including seed, open/warmup, window gaps, joined
shutdown and the separately counted controller setup/cleanup: **25,279 SQL
calls / 71 connects** at 2048 records, and **60,811 SQL calls / 83 connects**
at 4096. Controller SQL contributed 442 calls to each. These totals must not
be added to the overlapping phase counts. Complete runs took about 13.9 and
20.0 seconds; the whole-process timing limitations above still apply.

Captured summary and per-process metric output is retained locally at:

- `/tmp/atomic-read-load-2048-cache256k.txt`
- `/tmp/atomic-read-load-2048-cache768k.txt`
- `/tmp/atomic-read-load-2048-cache1m.txt`
- `/tmp/atomic-read-load-4096-cache1m.txt`

These `/tmp` artifacts are local run evidence, not durable repository assets;
the source and commands reproduce the campaign. Per-operation samples are
bounded internal data used for the printed exact quantiles, not a persisted
trace. The example's pure protocol/percentile test passed, and all four live
campaign configurations completed their native semantic and resource checks.
