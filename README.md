# Atomic

Atomic is a source-study and Rust-port project. Its retained Datomic Pro
reference corpora are organized by release. Historical plans, one-off runners,
generated reports, packaged resources, and build products have been removed
from the active tree.

The current job is to learn the system embodied by recovered Datomic Pro Peer
and Transactor releases, express its durable ideas as language-neutral
contracts, and implement those contracts in Rust.

## Start here

- [`goal-0/0-plan.md`](goal-0/0-plan.md) is the full PostgreSQL-only Rust reconstruction roadmap.
- [`goal-1/0-plan.md`](goal-1/0-plan.md) is the semantic-foundation milestone.
- [`goal-1/SEMANTICS.md`](goal-1/SEMANTICS.md) is the native semantic contract now implemented by the kernel in [`src/`](src/) and checked in [`tests/`](tests/).
- [`goal-2/0-plan.md`](goal-2/0-plan.md) records the completed single-process Rust transactional kernel and its deliberate boundaries.
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

The semantic and kernel conformance suites now run with:

```sh
cargo test --offline
```

## Working boundary

Treat the recovered source as evidence, not as the unpublished original source
tree. Compilation erased comments, formatting, some names, and some macro
forms. Preserve observed behavior and architectural invariants; do not copy JVM
or Clojure machinery merely because it appears in the recovery.

The last pre-clean repository state is Git commit
`b8ebb1af74e6357d8242b2713af92f98d9e20e3a`. It retains the deleted recovery
apparatus if a narrowly identified historical fact must be consulted later.
