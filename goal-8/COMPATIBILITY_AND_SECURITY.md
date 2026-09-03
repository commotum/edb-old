# Compatibility and security policy

## Compatibility

- Rust public types and canonical formats are native Atomic contracts, not
  Datomic JVM APIs, Fressian bytes or existing-database formats.
- Every durable blob has an explicit checked kind/version. Unknown versions,
  noncanonical encodings and checksum mismatches fail closed.
- SQL migrations are monotonically numbered, transactional and checksum
  pinned. Changes to an applied migration are forbidden; evolution adds a new
  migration and tests mixed persistent state through restart. Populated v6 is
  the current in-place floor; older populated native prototypes require
  old-decoder export/rebuild, while v6--v8 history is canonically replayed to
  backfill authenticated state commitments during upgrade.
- PostgreSQL is the sole backend. Supported PostgreSQL major versions must be
  named and exercised by each release; current acceptance evidence is 15.11.
- The semantic support boundary and deliberate omissions are recorded in the
  completed child contracts, especially `goal-5/SUPPORTED_SURFACE.md` and
  `goal-6/RUNTIME_CONTRACT.md`.

## Roles and secrets

Use separate least-privilege PostgreSQL roles:

- application peers read catalog/head/log/index/program rows;
- the transaction service additionally inserts transactions/requests and
  conditionally advances heads, but cannot disable triggers or mutate history;
- backup reads authoritative/configuration rows and writes only its external
  destination;
- migration/operator performs DDL, verified restore, GC and excision and is
  never exposed through the application transaction API.

`PostgresMigrator::grant_runtime_privileges` installs the concrete writer and
peer table grants after rejecting elevated, inherited, owning, or non-distinct
roles. Runtime service, peer, indexer, and tree-writer startup validates the
complete checksummed migration prefix and refuses an unknown newer version
before reading or publishing database state.
`PostgresStore` has no DDL/migration entry point; only `PostgresMigrator` can
open an unchecked administrative session. Runtime handles retain their
connection policy for checked reborrow, but transaction submission is never
blindly retried across an ambiguous connection loss.

Require TLS across host boundaries, authenticate at the embedding service,
rotate database credentials, restrict `pg_hba.conf`, and encrypt PostgreSQL,
WAL archives and portable backups at rest using deployment-owned facilities.
Atomic does not log credentials. Callers must avoid putting sensitive values
in idempotency keys, program names, database ids, excision ids or audit labels.

## Execution and denial-of-service boundaries

Persisted programs are a closed deterministic bytecode with no ambient I/O,
clock, randomness or native calls. Query, pull, program, transaction and queue
work have explicit limits. PostgreSQL statement/lock timeouts, connection
limits and storage/WAL alarms remain deployment responsibilities. Integrity
and backup decoding cap lengths and reject malformed or noncanonical data.

Excision and restore require PostgreSQL trigger-bypass authority because they
rewrite otherwise immutable rows. They must run only in the audited operator
process after target resolution and backup verification. Exported immutable
snapshots, replicas, backups and logs are outside the live cache invalidation
boundary and require explicit inventory/retention handling.
