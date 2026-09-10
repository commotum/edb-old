# Atomic

Atomic is a native Rust/PostgreSQL database inspired by Datomic: immutable
database values and facts, serialized declarative transactions, schema and
identity, history/time views, peer-local Datalog/Pull, controlled persisted
programs, and operational recovery. Retained Datomic Pro documentation governs
semantics; recovered source is architectural evidence, not a JVM/wire target.
Earlier goal passes remain preserved in `goal-archive/`.

The supported product includes a local or verified-TLS remote transactor,
separate Rust applications, immutable cached peer reads, shared speculative
branches, exact snapshot references, persisted native query programs, safe
transaction planning, named/implicit partitions, UUID helpers and peer-local
fulltext. Bounded restartable transaction consumers and an administrative CLI
cover observation, backup/restore, inspection, GC and explicit recovery.

[Goal 0](goal-0/0-plan.md) owns the completed seven-stage acceptance, including
final operating measurements and rebuilt integration. See
[product acceptance](docs/acceptance.md) for verified boundaries
and limits. Historical100,000-record G3 results remain in
[G3 Goal 6](goal-archive/G3/goal-6/0-plan.md); they are not measurements of the
newer implementation or a universal production/Datomic-parity certification.

## Start here

- [Run the supported local or remote transactor and separate application](docs/application.md)
  using the `atomic` binary, explicit PostgreSQL setup and restricted runtime roles.
- [React to transactions with bounded durable consumers](docs/change-consumers.md).
- [Back up, verify, restore, inspect and maintain databases](docs/admin.md).
- [Interpret product acceptance and the measured operating envelope](docs/acceptance.md).
- [Measure independent readers, cold opens and mixed analytics/write traffic](docs/read-load.md).
- [Author persisted native query programs](docs/programs.md) with predicates,
  recursive rules, negation, historical sources and dynamic attributes.
- [Compose native database, tuple and log queries](docs/queries.md), reuse query
  structure and configure join/resource limits.
- [`goal-0/0-plan.md`](goal-0/0-plan.md) defines the product objective and
  required capabilities; [`0-loop.md`](goal-0/0-loop.md) guides execution and
  [`0-prompt.md`](goal-0/0-prompt.md) provides the continuation prompt.
- [`goal-1/0-plan.md`](goal-1/0-plan.md) is the first child: runnable local
  transactor/application, minimal explicit setup and reusable verification.
  Goal0 scaffolds and executes each following child, reconciling results until
  the overall objective is complete.
- [`goal-archive/`](goal-archive/README.md) indexes original Goals0–8 in G1,
  corrective Goals9–17 in G2, and the completed native-core Goals0–6 in G3.
  Their plans and completion labels are historical evidence, not active policy.
- [The corrective evidence ledger](goal-archive/G2/goal-9/EVIDENCE_LEDGER.md)
  maps prior repairs and unresolved questions to the docs and recovered source.
- [The active operational guide](docs/operations.md) describes provisioning,
  I/O policy, backup/restore, GC and excision. The archived
  [tiered writer architecture](goal-archive/G2/goal-16/ARCHITECTURE.md) retains
  implementation evidence; check it against current code.
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

These small examples establish application paths, not scale acceptance. G3 Goal6
records measured deployment/maintenance drivers, actual runs and
the workload-specific operating envelope.

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
new inputs select new grammars and (for code literals) program ABI 6. Expanded
native query templates select ABI 7 only when used, preserving earlier bytes.
Transactions
on filtered values use the full basis and retain the filters on their result:
`as_of` is not a branch of the past. History values cannot transact. Controlled
generation follows this same docs-first native rule. Archived G3 Goal3 records
its live semantic acceptance; G3 Goal6 records integrated load and failure acceptance.

## Native read access

Queries and Pull use exact immutable `DatabaseValue`s, including temporal and
custom-filtered values. `QueryControl` and `PullControl` default to no arbitrary
work/row/depth/entity ceilings; configure explicit budgets and cancellation for
untrusted or broad reads. Pull's documented default many-valued limit remains
1,000. Limits are cooperative logical-work policies, not hard allocator or SQL
preemption guarantees. Result cloning, comparison, formatting and destruction,
unlimited Pull, explicit selector ownership, and component `Entity::touch` use
heap traversal instead of depending on Rust call-stack depth.

`QueryEngine::sequence` / `DatabaseValue::query_sequence` prepare joins and
aggregates eagerly, then defer Pull and transforms until each row is consumed.
`remaining_rows()` counts prepared tuples without running those projections;
a later error terminates the iterator. Distinct entity bindings remain distinct
even when their projected maps compare equal. `PullAttribute::transform` accepts
native conversions or a named `PullTransform::new` Rust callback; transforms see
missing nil values, run before defaults, and propagate errors. Arbitrary local
Rust callbacks are trusted/cooperative, not forcibly preempted.

`Function::Query` embeds a native subquery. It uses the clause's exact source as
`$` and retains named sources, sharing the enclosing resource controls.
`Aggregate::Rand(n)` samples with replacement; `Sample(n)` returns up to n
distinct values. This randomness is local read behavior, not persisted program
semantics. `QueryResult::into_return_maps` gives keyword/string/symbol keys plus
positional access; use `into_return_maps_with_arity` when empty-result arity must
also be checked. Stack-safe `QueryValue` destruction requires the consuming
`into_map`/`into_collection`/`into_tuple`/`into_scalar` accessors to move fields;
borrowed enum matching is unchanged.

`Connection::log()` / `Peer::log()` capture an immutable authenticated transaction
log, independently of writer availability. `tx_range` uses inclusive start and
exclusive end T/Tx/instant bounds; `tx_ids` and `tx_data` expose query-friendly
transaction data without reconstructing the database. Log data retains original
noHistory transactions. `DatabaseValue::index_pull` lazily projects AVET/AEVT
ranges with forward/reverse bounds, offset and optional limit, using the exact
captured database and documented reference/cardinality rules.

Native [fulltext search](docs/fulltext.md) provides versioned string analysis,
phrase/Boolean/prefix expressions, BM25 ranking and structured Datalog joins.
Immutable PostgreSQL search projections are built in the background; every hit
is checked against the supplied database view. Coverage may lag and is explicit;
search is not a complete-membership correctness constraint. Schema upgrades,
stored ABI9 programs and the separate-process application are verified on real
PostgreSQL; Goal5 records lifecycle and measured-cost acceptance.
Native advisory read tracing/prefetch is implemented and verified in
Goal3; Goal6 adds versioned authenticated cross-host hint transport. Hints never become
transaction meaning or durable request identity. Cold submissions may still
incur additional index reads. Neither search lag nor missing hint transport changes
identity, transactions, history, or local Datalog/Pull semantics. See archived
G3 Goal4 for the earlier source-based decisions and measured read fixtures.
G3 Goal5 records operational integrity evidence and the completed GC repair;
G3 Goal6 records integrated
acceptance alongside the preserved unsuccessful earlier large-run evidence.

## Working boundary

Treat the recovered source as evidence, not as the unpublished original source
tree. Compilation erased comments, formatting, some names, and some macro
forms. Preserve observed behavior and architectural invariants; do not copy JVM
or Clojure machinery merely because it appears in the recovery.

The last pre-clean repository state is Git commit
`b8ebb1af74e6357d8242b2713af92f98d9e20e3a`. It retains the deleted recovery
apparatus if a narrowly identified historical fact must be consulted later.
