# Atomic

Atomic is a native Rust/PostgreSQL database inspired by Datomic. Applications use
immutable database values, declarative serialized transactions, strong identity,
schema as data, history/time views, and peer-local Datalog, Pull and navigation.

Rust owns the database engine: persistent log/index structures, receipts,
publication, writer fencing, retention and maintenance. PostgreSQL stores opaque
immutable objects and revisioned references. It supplies durable atomic storage
primitives, not transaction or maintenance policy. There is one production
storage path and no JVM/wire compatibility target.

This is a first-release development product. Fresh databases may be required
after format changes; earlier development databases and backups are not supported
upgrade inputs. Installation never resets existing data.

## Start here

- [EDN transactions, queries, pull patterns and readable results](docs/edn.md),
  using native Rust adapters or the file/stdin CLI.
- [Local or verified-TLS remote applications](docs/application.md), including
  explicit PostgreSQL installation and restricted runtime roles.
- [Immutable read composition](docs/read-values.md), including snapshot references,
  time views, entity identity, speculative branches and index access.
- [Datalog](docs/queries.md), [general application data](docs/query-data.md) and
  [native application computation](docs/application-computation.md).
- [Persisted native programs](docs/programs.md), [fulltext](docs/fulltext.md),
  [partitions](docs/partitions.md) and [schema/identity](docs/schema-identity.md).
- [Durable change consumers](docs/change-consumers.md) and
  [logical database lifecycle](docs/database-lifecycle.md).
- [Backup, restore and administration](docs/admin.md),
  [selective offline backup reads](docs/backup-reads.md) and
  [operational guidance](docs/operations.md).
- [Current acceptance and measured limits](docs/acceptance.md), plus
  [reader measurements](docs/read-load.md).

The retained [Datomic Pro documentation](datomic_pro_docs/) governs semantics.
The recovered [1.0.7705 source](1.0.7705/) is architectural evidence, not the
unpublished original source tree or a runnable recovered distribution. Its
`peer/` and `transactor/` directories are sibling artifact-provenance boundaries.
[Tools](tools/) retains narrowly useful decompiler and JVM inspection utilities.

## Build and exercise

```sh
cargo build --offline --bin atomic --examples
cargo test --offline --lib
```

PostgreSQL-dependent tests report missing configuration and return without
exercising PostgreSQL. A green unconfigured run is not PostgreSQL acceptance.

For a focused integration pass, use a dedicated disposable PostgreSQL database and
an administrative test login. Fixtures install the current object/reference
schema; role tests require permission to create restricted test roles.

```sh
ATOMIC_POSTGRES_URL='host=/path/to/socket port=5432 user=atomic_test dbname=atomic_test' \
  cargo test --offline --test edn_transactions --test native_connection \
    --test block_receipts --test block_backup --test transactor_differential \
    -- --test-threads=1
```

Choose relevant targets while developing; use `cargo test --offline --all-targets`
for a broader integration pass when needed. Duplicate stress/measurement campaigns
and historical-format fixtures have been removed. The remaining focused tests
protect current behavior; benchmark throughput with an actual application workload.

The server-crash test is separately opted in with
`ATOMIC_ALLOW_DISPOSABLE_PG_CRASH=1`, `ATOMIC_RESTART_POSTGRES_URL`,
`ATOMIC_RESTART_POSTGRES_DATA`, `ATOMIC_RESTART_PG_CTL`,
`ATOMIC_RESTART_POSTGRES_LOG` and `ATOMIC_RESTART_POSTGRES_OPTIONS`.
The options must specify the disposable server's exact `-k`, `-p` and `-h`
settings. Run `--test postgres_restart_resilience` alone: never point it at a
shared server or run other tests against that server while it is being crashed.
PostgreSQL TLS checks have separate trust/server prerequisites documented in
[operations](docs/operations.md); do not count skipped fixtures as evidence.

Run the public Rust application against the disposable database:

```sh
ATOMIC_POSTGRES_URL='host=/path/to/socket port=5432 user=atomic_test dbname=atomic_test' \
  cargo run --offline --example native_workflow
```

It creates a unique logical database and exercises schema, program deployment,
transactions, queries, Pull, history, held values and reopen. The
`process_workflow` example exercises an independent writer and submitting peers.
The stock `application_workflow` example uses the CLI-managed local or remote
transactor; follow the [application guide](docs/application.md).

## Application model

Capture `Connection::db()` or `Peer::db()` once and pass the resulting
`DatabaseValue` through computations. Capturing a resident value does no I/O;
cold reads authenticate immutable storage objects, while warm reads can be
entirely local. Queries and navigation do not run in the transactor.

`DatabaseValue::with` and `with_forms` return speculative reports without
publishing anything. Branches share their base and own their new information.
Maps, primitive forms, symbolic references and controlled persisted calls use
the same transaction semantics as committed requests. Transactions on filtered
values use the full basis and preserve filters on the result; `as_of` is not
a writable branch of the past. History values cannot transact.

A successful committed report contains the exact before/after values, transaction
data and resolved tempids. After an ambiguous delivery outcome, retry the same
request key and content. Receipt-first resolution preserves the committed result
instead of running the transaction again. A committed response can separately
report failure to open its read values without changing the durable outcome.

Connections observe newer roots in the background using coalesced notifications
and durable catch-up. Change consumers expose explicit checkpoints and bounded
replay. Retained handles and backup captures participate in Rust-owned retention;
a serialized snapshot reference is neither a credential nor a retention pin.
Excision cannot erase bytes or memories already exported to another process.

Read limits, deadlines and cancellation are cooperative resource policies, not
hard allocator or SQL preemption guarantees. Local Rust callbacks are trusted.
Fulltext merges indexed and recent facts with supplied-view validation; an
absent search hit is not an identity or uniqueness constraint. Advisory hints
never become transaction meaning or durable request identity.

Current acceptance and measurement limits are recorded in
[product acceptance](docs/acceptance.md). Historical
implementation results remain in Git history; they are not evidence for the
current storage engine.
