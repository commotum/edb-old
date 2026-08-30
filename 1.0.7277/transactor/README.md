# Transactor reference source

This subtree holds the recovered Transactor-side reference corpus:

- 247 Clojure source files in `src-clj/`;
- 46 recovered Java-origin source files in `src-java/`.

The Transactor is the service that admits and serializes writes, assigns their
authoritative order, publishes the durable log, coordinates indexing, and
prevents stale writers from retaining authority. The application-facing Peer
corpus is its sibling in `../peer/`.

The 247 Clojure files are a runtime closure, not an authorship claim. It
includes bundled dependency namespaces alongside Datomic namespaces. Classify
a file's origin before drawing conclusions about Datomic-specific style or
architecture.

There is intentionally no active launcher, recovered distribution, PostgreSQL
runner, resource bundle, or broad oracle here. The old recovery harness mixed
many forensic and promotion requirements into one workflow, so it was removed.
Build small Rust-facing fixtures from the source as the port reaches each
contract. The pre-clean version remains recoverable from Git commit
`b8ebb1af74e6357d8242b2713af92f98d9e20e3a`.
