# Product acceptance and operating envelope

The Rust-owned storage cutover and integrated current-version acceptance are
complete. Integration failures were repaired and their affected paths rerun;
the evidence below distinguishes those results from a single clean whole-suite
invocation. This is a first-release product, not an old-format compatibility path.

## Architecture and preservation boundary

PostgreSQL has exactly two opaque tables, `atomic_objects` and `atomic_refs`,
with zero policy functions or triggers. The provider supplies authenticated
immutable I/O, protected writes, reference CAS and generic locks/notifications.
Rust owns log/index trees, receipts, allocation, writer fencing, catalog
lifecycle, retention, collection, excision and backup/restore. See
[operations](operations.md) for installation and trusted-role boundaries.

Current paths cover EDN and typed transactions/queries/Pull, schema/identity,
partitions, temporal and speculative values, entity/index navigation, native and
portable programs, fulltext, local/TLS remote and async applications, exact
receipts/references, change consumers and administration. Offline backups use the
same immutable readers, with source-removal tests proving no PostgreSQL fallback.

The legacy engine, migrations and reader/index/fulltext/backup adapters are
removed. Useful behavioral tests were ported; old SQL-shape and historical-format
tests were removed. Decoded program reuse, protected deployment, cache accounting,
batched uploads, maintenance controls and report continuity were repaired during
integration, not excluded. Existing Datomic source/documentation corpora are
unchanged. Fresh databases are required; installation never erases or converts
an existing database.

## Verified current-storage evidence

Configured tests used isolated schemas on disposable PostgreSQL 15.11, not mocked
providers or old-engine results.

- Broad `all2` finished with three failed cases and an interrupted
  `block_snapshot` target; it was not a clean pass. All four were subsequently
  repaired or completed and verified by the runs below.
- Subsequent full library run: 438 passed in 324.76 s, including repair
  regressions. The final five-test protection group passed in 4.45 s, including two
  new index-publication/bounded-retry cases covering five public observation paths.
  This is **not** a full 440-test run.
- Final `block_snapshot`: 10 passed in 198.61 s. Sixteen other focused targets
  passed. Later TLS application and operator inspection failures exposed a shared
  initial-pin race; the final nine-target integration rerun passed all 20 tests
  after that repair. It covered backup CLI/library/restricted roles, inspection,
  remote routing/transport/application and exact snapshot references. The isolated
  TLS application completed in 17.852 s; stock automatic failover took 4.452 s
  (6.159 s complete workflow), retaining exact retries and held reads.
- Separately opted-in WAL recovery: 1/1 passed in 32.81 s, including actual
  immediate shutdown/redo, acknowledged exact retries, uncommitted-reference
  rollback, retained values/log and writer failover. Server restart took 518 ms.
- Strict Clippy, documentation generation and formatting checks passed.
  Rustdoc ran zero doctests; that is not executable documentation coverage.

## Measured costs and limits

These are unoptimized local Rust measurements, not production-scale capacity,
SLAs or Datomic performance parity. Driver calls are not network round trips.

The standalone `block_live_costs` process passed in 15.34 s wall time with peak
RSS 71,388 KiB. This includes Rust fixture setup and workers, not PostgreSQL or
Cargo. Its 2,048 entities contained 1,572,864 scalar bytes against a 1 MiB cache.
Startup through shutdown took 14.950 s: 9,782 calls, 20,949,013 payload bytes read
and 8,952,849 written. Twenty-four writes took 3.338 s, or 6.681 s through automatic
index completion (four jobs). Thirty-two warm reads took 630 µs and zero SQL;
peak accounted peer-cache bytes were 715,532. Cache accounting is not process RSS.

The 10,000-entity snapshot fixture built a 13,640,218-byte index. Fixture and
eager-oracle setup took 190.524 s; the full ten-test target took 198.61 s. This
setup is not a native ingestion benchmark. Native open/query/`with` costs were
measured separately:

| Cache allowance | Native operation | Payload bytes read | Driver calls |
| --- | ---: | ---: | ---: |
| 0 | 284.658 ms | 470,408 | 135 |
| 16 KiB | 272.162 ms | 469,593 | 134 |

Other complete-path samples expose substantial fixed publication/retention cost:
100 small writes took 12.569 s /31,538 calls /394,373 bytes written; reopen and
exact retry took 330 ms. A small fulltext/program restore took 2.984 s /9,297 calls,
reading 157,245 and writing 73,419 bytes. A 96 MiB sparse corrupt repository object
was rejected with zero measured RSS growth; a valid 2 MiB value remained readable.
The latter proves input admission, not a general memory bound. Foreground metrics
exclude independent background work unless explicitly included by the measurement.

## Final integration status

Exact reports, snapshot references, inspection, backup capture and receipt lookup
now share bounded re-observation of transient initial-pin conflicts. Permanent
PostgreSQL regressions force publication between reference observation and pinning,
verify all five paths and check the retry bound. Explicit one-shot captures remain
strict; errors after successful capture are not hidden, and transactions are never
reexecuted by this read-only retry. Final affected application reruns passed.

Other integrated repairs include lease-safe operator index publication, bounded
excision observation, idle reader-pin cleanup across a GC race and lossless peer
reports across generation handoffs. Five reports spanning two excision generations
remained exact through three GC cycles (8.315 s complete handoff-cleanup fixture).
Current-version correctness and the storage boundary are established by these
checks; high-throughput production capacity is not established by local fixtures.

## Reproduce relevant checks

Build binaries/examples before process fixtures that invoke them:

```sh
cargo build --offline --bin atomic --examples
ATOMIC_POSTGRES_URL='host=/path/to/socket port=5432 user=atomic_test dbname=atomic_test' \
  cargo test --offline --all-targets -- --nocapture --test-threads=1
```

Use a dedicated disposable database and administrative fixture login. Verified
TLS/process tests also need their isolated-network and certificate prerequisites;
see [application setup](application.md) and [transport policy](operations.md).
Missing PostgreSQL, role, TLS or namespace prerequisites can skip real work.
Five opt-in manual tests were ignored, not counted as acceptance.

`postgres_restart_resilience` requires its own disposable server and explicit
`ATOMIC_ALLOW_DISPOSABLE_PG_CRASH=1`, plus `ATOMIC_RESTART_POSTGRES_URL`,
`ATOMIC_RESTART_POSTGRES_DATA`, `ATOMIC_RESTART_PG_CTL`,
`ATOMIC_RESTART_POSTGRES_LOG` and exact `ATOMIC_RESTART_POSTGRES_OPTIONS`.
Run it alone, never against a shared server. See the test and
[transport/recovery guidance](operations.md) for prerequisites.

`transactor_differential` compares saved-seed production transactions with the
pure kernel; `storage_fault_replay` covers interrupted index work, restart, exact
retry and consumer checkpoints. Historical executable/SQL-upgrade matrices are
not supported.
