# Integrated Goal 0 acceptance audit

This audit follows executable behavior rather than child-goal labels. The
acceptance environment is a disposable local PostgreSQL 15.11 cluster using
the same SQL engine, locks, triggers, WAL, restart and crash behavior as a
deployed instance. Deployment-specific network/TLS, replication lag, storage
latency and capacity are intentionally not inferred from this environment.

## Guarantees and evidence

| Goal 0 guarantee | Primary implementation | Acceptance evidence |
| --- | --- | --- |
| Canonical values, immutable datoms and deterministic unordered transactions | `value`, `datom`, `schema`, `database`, `transaction` | semantic and kernel conformance suites, ordering laws, permutation/state-machine checks |
| Identity, schema evolution, history and time views | pure `Database::with`, immutable roots/history chunks | upsert/lookup/composite/schema fixtures and current/history/as-of/since checks |
| Atomic durable PostgreSQL publication and exact recovery | migrations 1/5, checked `ATMC` envelopes, `PostgresStore` | concurrent expected-basis writers, every publication fault, process abort, acknowledgement loss, corruption, actual PostgreSQL restart |
| Independent local peers and scalable immutable reads | content-addressed segments/manifests, base+tail, bounded LRU, `Peer` | lag/catch-up, old snapshots, cache pressure, corrupt-base fallback, concurrent consolidators and restart |
| Local query, pull and entity API | typed Datalog/query planner, pull graph traversal | oracle/index differential tests, recursion/logical/aggregate/input shapes, cancellation/limits and immutable-snapshot queries |
| Deterministic custom behavior | checked straight-line bytecode, exact hash registry/cache | cross-decode byte identity, fuel/stack/output/cancel limits, immutable deploy/activation, predicate and query integration, restart/corruption |
| Serialized available transaction service | bounded worker, idempotent tickets/reports, server-time epoch lease | overload/unknown-outcome reconciliation, stale fencing, concurrent takeover, failover and 240-transaction stress |
| Protection, diagnosis, bounded growth and privacy | `PortableBackup`, `PostgresOperator`, capacity/GC/excision, migration 5 generation/audit | live incremental backups, deep corruption rejection, exact earlier/latest restore, integrity corruption, capacity invisibility, concurrent GC, excision rollback/raw scans/recovery/cache invalidation |

The strict build gate is `cargo fmt --all --check` plus
`cargo clippy --all-targets -- -D warnings`. The final serial acceptance run
executes 103 test entries: 102 pass and one deliberately ignored entry is the
crash-worker subprocess invoked by its parent test. The run includes two real
PostgreSQL server restarts and a client abort with an open transaction.

The separate physical-protection rehearsal ran PostgreSQL 15.11
`pg_basebackup -Fp -X stream --manifest-checksums=SHA256`, passed
`pg_verifybackup`, started the copied data directory on an isolated socket,
completed backup recovery, and queried migration/catalog state (`5` installed
migrations, all checksums present) before clean shutdown. Portable backup tests
separately prove per-database basis selection and exact logical restoration.

## Deliberate native boundaries

- PostgreSQL is the only store. There is no backend trait, JVM/Clojure runtime,
  Datomic wire/API/Fressian compatibility, or ability to open existing Datomic
  databases.
- Schema is typed transaction information exposed through `Database::schema`
  and retained as versioned `SchemaChange` history, rather than boot-id-shaped
  schema datoms in the query relation. This preserves synchronous evolution,
  historical audit and local introspection without importing Datomic bootstrap
  identity machinery; it is a semantic surface difference.
- Query/pull use a typed Rust AST, not EDN parsing or host-language coercion.
  Fulltext, arbitrary JVM calls, random aggregates, lazy `qseq`, legacy pull
  syntax and the other boundaries in `goal-5/SUPPORTED_SURFACE.md` are not
  claimed.
- Persisted programmability is deliberately less expressive than Clojure
  database functions. The closed bytecode trades general host code for
  reproducibility, metering and absence of ambient authority.
- The transaction service is an embeddable transport-neutral Rust boundary,
  not an HTTP/JMS compatibility server. Authentication, routing and process
  supervision belong to its embedding deployment.
- Excision is made more atomic than recovered Datomic: PostgreSQL rewrites the
  complete surviving chain, durable request hashes, head, audit, generation
  and derived invalidation together. Hash bookmarks change. Controlled peers
  rebuild on generation change, but already exported backups/WAL/logs and old
  application-held immutable snapshots require explicit retirement.
- Physical PITR RPO/RTO, HA topology, credentials, TLS and resource sizes are
  deployment facts. The runbook states how to measure/operate them; this local
  suite does not pretend to certify an unknown production environment.

## Result

Within those explicit boundaries, the Rust transactor/kernel and independent
peers backed only by PostgreSQL form one recoverable system. The core semantic,
concurrency, durability, peer-observable, deterministic-program, availability,
backup/restore, integrity, bounded-growth and privacy guarantees have direct
tests. No known gap remains in the Goal 0 success condition.

