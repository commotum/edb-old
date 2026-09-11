# Product acceptance and operating envelope

## Current architecture

PostgreSQL stores two opaque tables, `atomic_objects` and `atomic_refs`, with
no database-policy functions or triggers. Rust owns immutable log/index trees,
transactions, receipts, writer fencing, publication, catalog lifecycle,
collection, excision and backup/restore.

Eager, speculative and durable transactions use one assessor. Compact stored
queries lower into the shared query engine. Programs and request/outcome
messages each have one current format; unsupported formats are rejected.
PostgreSQL configuration has one typed authority.

Read values capture immutable roots without registering sessions, pins or
generation tokens. GC retains retired structures for a configurable grace
period, normally 30 days. A shorter cutoff must still cover active reads,
lagging report consumers and backup capture. Holding a Rust value does not
override the cutoff. Transaction receipts, current publications and explicit
maintenance roots retain the data needed for their own contracts.

Fulltext reads merge persisted postings with bounded committed and speculative
changes. Physical indexing lag is diagnostic information, not a reason to omit
current matches. Backups copy the canonical publication and its existing
index-plus-log-tail graph. Restore verifies its objects and publication
coordinates; full semantic replay is an explicit audit, not a prerequisite
repeated on every resume or completed retry.

## Verification

The simplification is checked with actual PostgreSQL 15.11 in disposable schemas.
The serial library run passed 433 tests. The integration sweep exposed stale
fixtures and regressions in fulltext tail reads and discontinued composite
schema handling; these were repaired and the affected targets rerun successfully.
Process tests used matching rebuilt executables. Verified TLS and an immediate
PostgreSQL shutdown/restart were exercised separately, not counted as skipped
checks. Formatting, all-target Clippy and local documentation links also pass.

Focused passing suites cover:

- Schema, identity, tuple and entity-map transaction semantics, callbacks and
  speculative values through the shared assessor.
- Stored queries, native predicates, exact query values, prepared-query reuse,
  controls and dependency validation.
- Publication races, stale writers, lost acknowledgements, recovery, GC,
  excision and retained transaction-report continuity.
- Read-only peer/backup roles, canonical unindexed backups, restore resumption,
  completed restore retries and source-independent offline reads.
- Fulltext changes before indexing, temporal/filtered/speculative views,
  replacements, retractions and bounded attribute-selective tail work.
- CLI/application restart and real verified PostgreSQL TLS, including rejection
  of an untrusted certificate and conflicting transport settings.

Tests retain meaningful behavioral and corruption regressions; removed machinery
does not retain vacuous zero-valued counters, old-format golden files, reader
session tests or duplicate transaction/query implementations.

## Costs and limits

Local debug-build checks are not production capacity claims. Fulltext regression
work and retained-byte counts stay unchanged after adding 512 unrelated recent
facts: it visits the requested attribute's recent range, not the whole tail.
The complete `native_workflow` process passed in 0.93 seconds with 23,336 KiB
peak application RSS on the local PostgreSQL fixture, excluding compilation
and PostgreSQL server memory.
Prepared queries reuse successful static rule analysis and a bounded structural
AST cache key; source/schema/input checks and value-dependent planning still run
for each invocation.

Foreground driver counters are not network round trips and exclude independent
background work unless explicitly included. Cache accounting is not process RSS.
Run the application and read-load examples for measurements on the intended
hardware and workload; no Datomic performance-parity claim is made.

## Reproduce

Build process fixtures first, then use a dedicated disposable PostgreSQL database:

```sh
cargo build --offline --bin atomic --examples
ATOMIC_POSTGRES_URL='host=/path/to/socket user=atomic_test dbname=atomic_test' \
  cargo test --offline --all-targets -- --test-threads=1
cargo clippy --offline --all-targets -- -D warnings
cargo fmt --all -- --check
```

TLS tests require their certificate/server settings. Missing PostgreSQL or TLS
prerequisites may skip live work; an unconfigured pass is not storage evidence.

`postgres_restart_resilience` requires its own disposable server and
`ATOMIC_ALLOW_DISPOSABLE_PG_CRASH=1`, plus `ATOMIC_RESTART_POSTGRES_URL`,
`ATOMIC_RESTART_POSTGRES_DATA`, `ATOMIC_RESTART_PG_CTL`,
`ATOMIC_RESTART_POSTGRES_LOG` and `ATOMIC_RESTART_POSTGRES_OPTIONS`.
Run it alone, never against a shared server.
