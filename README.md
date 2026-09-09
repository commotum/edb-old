# Atomic

Atomic is a source-study and Rust-port project. Its retained Datomic Pro
reference corpora are organized by release. The two prior Rust goal passes are
preserved in `goal-archive/`; the fresh `goal-0/` is the active strategy.

The repository contains a substantial PostgreSQL-only Rust reconstruction: a
pure transactional kernel, durable log/recovery, peer indexes and snapshots,
local query/pull, constrained persisted programs, a transaction service, and
operational tooling. Remaining work centers on the native connection and report
boundary, transaction and query/API completeness, operational verification,
and integrated production evidence. The project is not yet claiming the full
Goal 0 production outcome.

## Start here

- [`goal-0/0-plan.md`](goal-0/0-plan.md) defines the objective, current baseline,
  and remaining stages; [`0-loop.md`](goal-0/0-loop.md) guides execution and
  [`0-prompt.md`](goal-0/0-prompt.md) provides the continuation prompt.
- [`goal-1/0-plan.md`](goal-1/0-plan.md) is the first child: restore and verify
  the runnable baseline. Goal 0 scaffolds and executes each following child,
  reconciling results until the overall objective is complete.
- [`goal-archive/`](goal-archive/README.md) indexes the original goals 0–8 in
  A1 and corrective goals 9–17 in A2. Their completion labels are historical.
- [The corrective evidence ledger](goal-archive/A2/goal-9/EVIDENCE_LEDGER.md)
  maps prior repairs and unresolved questions to the docs and recovered source.
- [The operational contract](goal-archive/A2/goal-15/OPERATIONS.md) and
  [tiered writer architecture](goal-archive/A2/goal-16/ARCHITECTURE.md) retain
  detailed implementation evidence; check them against current code.
- [`1.0.7277/`](1.0.7277/) contains the validated historical Peer and
  Transactor reference corpus.
- [`1.0.7705/`](1.0.7705/) contains the newer Peer and Transactor reference
  corpus recovered from the matched 1.0.7705 distribution.
- [`tools/`](tools/) contains the repaired decompiler and the small set of
  reusable JVM inspection tools retained after the clean.

Within each release, `peer/` and `transactor/` are sibling
artifact-provenance boundaries. Neither is structurally subordinate to the
other.

The active tree deliberately does **not** contain a runnable recovered Datomic
distribution or the old broad recovery validation apparatus. Conformance is
being rebuilt in small, Rust-facing pieces as concrete porting questions arise.
Do not restore the old workflow wholesale: that would reintroduce thousands of
historical requirements unrelated to the port.

The default suite runs with (PostgreSQL-dependent tests self-skip without configuration):

```sh
cargo test --offline
```

The complete PostgreSQL acceptance suite requires a migrated disposable
PostgreSQL database and runs serially because restart/corruption fixtures share
server state:

```sh
ATOMIC_POSTGRES_URL='host=/path/to/socket port=5432 user=atomic_test dbname=atomic_test' \
  cargo test --all-targets -- --test-threads=1
```

Restart witnesses additionally require `ATOMIC_POSTGRES_CTL` (the absolute
`pg_ctl` path) and `ATOMIC_POSTGRES_DATA` (that disposable server's data
directory). Do not run other tests on that server during restart checks. Some
TLS and role fixtures have additional prerequisites; a green exit without
those prerequisites is not evidence that they executed.

`postgres_restart_resilience` deliberately uses a separate fixture:
`ATOMIC_RESTART_POSTGRES_URL`, `ATOMIC_RESTART_POSTGRES_DATA`,
`ATOMIC_RESTART_PG_CTL`, `ATOMIC_RESTART_POSTGRES_LOG`, and
`ATOMIC_RESTART_POSTGRES_OPTIONS`. Set the last variable to the server's exact
`-k`, `-p`, and `-h` startup options; `pg_ctl start` does not infer a prior
custom endpoint. Never point a restart fixture at a shared or production server.

Run the public-API application workflow against a disposable database:

```sh
ATOMIC_POSTGRES_URL='host=/path/to/socket port=5432 user=atomic_test dbname=atomic_test' \
  cargo run --offline --example native_workflow
```

It explicitly provisions a unique logical database, then uses the connection
API for schema, transact, query, pull, history, immutable old values, and reopen.
It fails if PostgreSQL configuration is missing. The writer has a separate
lifetime; a second read-only peer observes its commits and reopens after writer
shutdown. Submission attachment in this example is an in-process handle, not a
remote writer protocol.

The Unix-only independent-process workflow runs a writer and two submitting
peer child processes, with a separate observer:

```sh
ATOMIC_POSTGRES_URL='host=/path/to/socket port=5432 user=atomic_test dbname=atomic_test' \
  cargo run --offline --example process_workflow
```

`LocalTransactionServer` exposes an existing writer through a bounded, versioned
native Unix socket. `Connection::transact_socket` submits full declarative forms
and opens exact native receipt values using the peer's read-only PostgreSQL
permissions. Socket access is restricted to the same OS user (private 0700
directory, 0600 socket); this is a same-host deployment, not a TCP service or
Datomic wire protocol. Restarting the adapter creates a new endpoint to pass
to callers. PostgreSQL transport can independently require verified TLS.

A lost response after attempted delivery is `UnknownOutcome`: retry the same
request key and content. A confirmed commit returns `CommittedTransaction`;
its `report` can separately fail to open without changing the known commit.
Remote semantic errors retain category, details, and structured anomaly; the
original code is in `details["remote_code"]`. Delivery uses a total socket
deadline; native receipt opening uses the configured PostgreSQL I/O policy.

Both `Connection::db()` and `Peer::db()` capture native immutable values without
I/O. Eager diagnostic access is explicitly named `Peer::db_compatibility`,
`try_db_compatibility`, `sync_compatibility`, and `sync_to_compatibility`.
Connections advance in the background without blocking the writer: a bounded
one-slot hint channel is repaired from the durable log. Only explicitly enabled
transaction-report queues are unbounded; consumers must drain or disable them.
`observation_error()` is separate from a transaction's durable outcome.
Dropping a read connection requests observer shutdown without waiting for a
stalled storage read; its worker and pins release when that in-flight read ends.

These examples establish application paths, not production or scale acceptance.

`DatabaseValue::with(&ops, tx_instant)` now returns a pure
`SpeculativeTransactionReport`; its `db_after` can be extended, queried, pulled,
or viewed through history/time/raw indexes without advancing PostgreSQL or a
connection. Speculative chains share their committed base and retain their own
information delta. `with` accepts primitive operations; `with_forms` also accepts
maps and persisted controlled calls, using durable binding/predicate checks.
`with_forms_with_limits` exposes operation/read/program and code-retention
resource policy. Tuple ref slots accept `TxValue::Tuple` with symbolic references
and nils; structured lookup keys use `EntityRef::LookupInput`. Stored values
remain fully resolved. Ordinary request and program hashes remain unchanged;
new inputs select new grammars and (for code literals) program ABI 6. Transactions
on filtered values use the full basis and retain the filters on their result:
`as_of` is not a branch of the past. History values cannot transact. Controlled
generation follows this same docs-first native rule. Goal 3's live acceptance
is recorded in its plan; overall query, operations and scale work remains.

## Working boundary

Treat the recovered source as evidence, not as the unpublished original source
tree. Compilation erased comments, formatting, some names, and some macro
forms. Preserve observed behavior and architectural invariants; do not copy JVM
or Clojure machinery merely because it appears in the recovery.

The last pre-clean repository state is Git commit
`b8ebb1af74e6357d8242b2713af92f98d9e20e3a`. It retains the deleted recovery
apparatus if a narrowly identified historical fact must be consulted later.
