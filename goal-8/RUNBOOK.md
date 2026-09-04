# Atomic operator runbook

> Historical prototype record. The current contract is
> [`goal-15/OPERATIONS.md`](../goal-15/OPERATIONS.md); in particular, the
> restore, generation-GC, and ordinary A=15 excision procedures below have
> been superseded and must not be operated from this file.

This runbook covers the PostgreSQL-only Rust library. The embedding process
owns authentication, transport and scheduling; the named Rust methods are the
stable operator boundary. Substitute real paths, roles, database names and
retention values. Never put credentials in command history or source control.

## Deploy and upgrade

1. Back up the PostgreSQL cluster and deeply verify a portable backup of each
   critical Atomic database.
2. Stop transaction-service leaders. Peers may continue serving immutable old
   snapshots during this read-only window.
3. Run `PostgresMigrator::migrate()` once using the migration role. It takes a
   transaction-scoped advisory lock and rejects changed, gapped, or future
   migration history. Use `grant_runtime_privileges` to provision distinct
   pre-created writer and peer roles after securing the containing schema.
4. Start one `TransactionService` or `TransactionStandby`, confirm it owns a
   fresh lease epoch, then start remaining standbys and peers.
5. Run `PostgresOperator::inspect_database(database_id, true)` and require a
   healthy report before reopening writes.

Migrations are forward-only. A binary that does not know an installed format
or whose migration checksum differs must not serve. Rollback means restoring a
verified pre-upgrade backup into a separate cluster, not editing migration
rows or committed data.

The populated in-place SQL upgrade floor is version 6. Versions 6--8 are
upgraded by canonical log replay so migration 9 can replace its zero
state-commitment placeholders atomically. A populated version 1--5 catalog is
not byte-compatible with that representation and returns
`postgres/upgrade-rebuild-required` before DDL; export it with a compatible old
decoder and import into a freshly provisioned catalog. Empty old catalogs may
run the whole migration chain. This native SQL boundary is distinct from
Datomic's logical `:upgrade-schema` operation.

## Leader loss and failover

1. Stop routing new writes to an unhealthy leader. A timed-out caller treats
   its outcome as unknown and retries only with the identical idempotency key
   and request.
2. Let the old server-time lease expire or release it cleanly. Do not bypass
   the epoch fence.
3. Start/observe `TransactionStandby`; it catches up from the durable head and
   acquires a strictly newer epoch before publishing.
4. Confirm contiguous basis and a healthy deep integrity report. A stale owner
   returning `postgres/leadership-lost` is expected evidence that fencing held.

Peers automatically reborrow PostgreSQL connections during sync and continue
to expose their most recent immutable local value while storage is unavailable.
Timed sync includes connection negotiation in its deadline. Store and tree
maintenance handles can call their checked `reconnect` methods; index
consolidation reselects and retries one whole idempotent build. Do not retry an
arbitrary transaction automatically: resolve its idempotency key first.

## Physical backup and PostgreSQL PITR

Configure continuous WAL archiving to storage outside the primary failure
domain, monitor archive failures, and retain the matching base backups. A
representative online base backup is:

```sh
pg_basebackup -h PGHOST -p PGPORT -U backup_role \
  -D /external/atomic/base-YYYYMMDDTHHMMSSZ \
  -Fp -X stream --checkpoint=fast --manifest-checksums=SHA256
pg_verifybackup /external/atomic/base-YYYYMMDDTHHMMSSZ
```

For PITR, restore a base backup to an empty data directory, set
`restore_command` to fetch archived WAL, set one of PostgreSQL's recovery
targets (time, LSN or restore point), create `recovery.signal`, and start the
isolated instance. After recovery promotes or pauses at the requested target:

1. run `PostgresMigrator::migrate()` only if intentionally upgrading;
2. deep-inspect every Atomic database;
3. recover representative current and historical bases;
4. connect a peer and run application query/pull smoke tests;
5. switch traffic only after verification.

RPO is the confirmed WAL archive/replication lag. RTO is the measured restore,
WAL replay and validation time. Record both per rehearsal. On PostgreSQL 15.11
the project rehearsal completed an online `pg_basebackup`, passed
`pg_verifybackup`, booted the isolated copy through crash recovery, and read
migration/catalog state from it.

## Portable per-database backup and restore

Use `PortableBackup::backup_database(id, external_directory)` for a live
repeatable-read backup. Keep the directory outside the primary failure domain;
its `CLAIM` binds it to one source database and its immutable objects are
reused by later basis manifests.

Before trusting a recovery point, call
`PortableBackup::verify_backup(directory, basis, true)`. Restore with
`restore_backup(directory, basis, new_database_id)`. The target id must be
absent; restore never overwrites a live timeline. Then deep-inspect, connect a
new peer, and compare application-level current/history/query fixtures. Keep
the source and target quiescent while deciding which identity receives
traffic.

## Diagnose and bound capacity

`PostgresOperator::inspect_database(id, true)` is read-only. Alarm on any
problem, rising `index_lag`, history/transaction bytes near policy, sustained
service `Busy`, dropped subscriber reports, lease expiry, or cache miss/eviction
behavior inconsistent with the workload. Do not repair hash, request, head or
manifest rows by hand; isolate writes, preserve evidence, restore or fix the
owning code path, and rerun deep inspection.

Set `PostgresStore::set_capacity_limits` before serving. Limits cover operation
count, canonical payload bytes and history transactions. A rejection is
`Busy`, publishes nothing, and does not prevent resolution of an already
committed idempotency key. Size PostgreSQL/WAL, service queue, peer segment
cache, query controls and program controls independently from measurements.

## Derived-content GC

1. Run `garbage_inventory(age_horizon)` and retain the hashes/counts in the
   operator record.
2. Ensure the horizon exceeds the longest plausible in-flight publisher and
   any incident hold.
3. Run `collect_garbage(age_horizon)` with the operator role.
4. Deep-inspect databases and open a cache-cold peer.

GC deletes only globally unreferenced segments and unversioned programs. It
never deletes transaction history. Publication locks and a final reachability
check make a concurrent manifest/program version win over collection.

## Privacy excision

Excision is irreversible correction of retained information, not ordinary
data correction. First create an external portable backup at the current head
and obtain `BackupVerification` with deep verification. Inventory every other
copy (physical backups, WAL archives, replicas, exports, logs and retained
application snapshots) and apply the same legal retention decision there.

Submit one stable `ExcisionSpec` id and an entity, selected-entity-attributes,
or attribute target with optional exclusive `before_t` to
`PostgresOperator::excise_database`. The operator locks the head, verifies the
backup describes it, rewrites the chain atomically, invalidates manifests,
increments the peer generation and stores only the predicate audit. Retry an
ambiguous result with the identical excision id/spec/backup.

After success:

1. call `sync()` on every controlled peer and discard all previously retained
   `Arc<Database>` snapshots;
2. deep-inspect and restart-recover the database;
3. scan authoritative payloads for the prohibited byte values when such a
   byte-level check is meaningful;
4. consolidate fresh indexes, then run cache-cold current/history/query tests;
5. run GC only after the chosen safety horizon.

Bootstrap/transaction entities and `:db/txInstant` are protected. Entity
excision follows components and inbound refs; attribute-target excision leaves
component/inbound coordination to the application, matching the documented
Datomic boundary.

## Incident response and retirement

For corruption, fork, unexpected leader, or privacy failure: freeze writes,
preserve PostgreSQL logs/WAL and the integrity report, resolve unknown requests
by their original idempotency keys, and restore into isolation. Never conceal
an incident by editing hashes or deleting audit rows.

For retirement: stop clients/leaders/peers; take and verify a final backup or
record an explicit waiver; revoke application, backup and operator roles;
drop the explicitly named PostgreSQL database/cluster with native tooling;
expire replicas, WAL archives, physical/portable backups and exports under the
same retention decision; and retain only the permitted non-sensitive audit.
