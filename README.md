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

The pure suite runs with:

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

## Working boundary

Treat the recovered source as evidence, not as the unpublished original source
tree. Compilation erased comments, formatting, some names, and some macro
forms. Preserve observed behavior and architectural invariants; do not copy JVM
or Clojure machinery merely because it appears in the recovery.

The last pre-clean repository state is Git commit
`b8ebb1af74e6357d8242b2713af92f98d9e20e3a`. It retains the deleted recovery
apparatus if a narrowly identified historical fact must be consulted later.
