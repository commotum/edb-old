# Goal 8 operations contract

> Historical prototype record. Use
> [`goal-15/OPERATIONS.md`](../goal-15/OPERATIONS.md) for the current
> source-backed lifecycle contract. Its same-lineage restore, physical
> generation collection, and ordinary A=15 excision rules supersede this
> document.

## State classes

Authoritative state is `atomic_databases`, the contiguous
`atomic_transactions` predecessor-hash chain, `atomic_requests`, and the
published `atomic_heads` row. Program content/version rows are authoritative
configuration for future execution but are not needed to replay committed
history. Lease rows are ephemeral coordination. Index segments/manifests are
derived and disposable because peers verify them and fall back to the log.

Migration version/checksum rows define the SQL compatibility boundary. A
backup claiming a database basis must include its bootstrap, every transaction
through that basis, matching request identities, and any program definitions
needed for future behavior. Derived indexes may be copied for recovery speed
but are never required for correctness.

## Protection and recovery

Two supported layers are distinct:

- PostgreSQL physical backup plus WAL archiving/PITR protects the complete
  cluster. Its RPO is the configured WAL archive/replication lag and its RTO is
  the operator's PostgreSQL restore/replay time. Atomic cannot truthfully
  improve or infer those deployment values.
- A portable per-database backup is an external canonical checked bundle at an
  exact committed basis. Its RPO is that basis and its measured RTO is recorded
  by restore rehearsal. Repeating backup to the same directory reuses
  content-addressed immutable transaction/program blobs. The directory is
  exclusively claimed by one database identity.

Live export takes a PostgreSQL repeatable-read, read-only snapshot and verifies
the head/chain within it. Restore creates a new explicit database id in an
empty destination catalog entry; it never overlays a running timeline.
Selectable restore truncates only the backup view, not its files. Deep verify
reads and decodes every blob and reconstructs the database. Operators restart
peers/services or connect new ones after restore.

## Inspection, metrics, and limits

The native equivalent of recovered `integrity` crosschecks the head,
contiguous basis/order, predecessor and content hashes, canonical decoding,
request references, schema/kernel replay, manifest/segment hashes, segment
ordering, and manifest transaction anchors. Inspection is read-only and
reports stable structured problems rather than repairing silently.

Metrics are on-demand open snapshots of counts, bytes, basis/index lag, cache,
queue, lease and error/alarm state. Recovered `Statistics` lo/hi/sum/count maps
to bounded numeric accumulators; exporting is caller-owned. Transaction
operation/datom/encoded-byte and database-history limits reject work before
publication with `Busy`, leaving capacity policy separate from semantics.

## GC and evolution

Only content-addressed segments not referenced by any manifest and programs
not referenced by any version may be collected. Dry-run is the default. Apply
uses PostgreSQL locks and a caller-supplied server-time age horizon, then
rechecks reachability in the deleting statement. Concurrent publication wins
over deletion. Process caches are safe because cached immutable bytes remain
valid, while a future miss either resolves reachable storage or cleanly fails.

Migrations remain ordered, transactional, advisory-locked and checksum
verified. Readers reject unknown canonical format versions. Upgrade is forward
only unless a release explicitly supplies and rehearses a down-migration;
restoring a pre-upgrade backup is disaster recovery, not a schema downgrade.

## Privacy excision

Excision is not retraction or correction. It requires: an exact database,
privileged operator call, a separately stored deeply verified backup id/basis,
and an explicit predicate (entity, selected entity attributes, or attribute,
optionally before a basis). Bootstrap/schema/transaction-instant and prior
excision-audit information are protected.

Entity excision follows component descendants and removes inbound references;
selected-attribute entity excision removes matching inbound/outbound values.
The operation locks the database against publication, records a non-sensitive
predicate audit, rewrites every affected canonical transaction, rehashes the
surviving chain and durable request references atomically, and invalidates all
manifests. Failure rolls back the entire rewrite. Raw payload scans, recovery,
current/history indexes, peer refresh, and backup policy verify absence.

Because hashes necessarily change, outstanding receipts/bookmarks remain valid
by basis but old transaction hashes are invalid after excision. A per-database
generation makes connected peers rebuild on their next `sync`; callers must
still discard any old `Arc<Database>` values they retained, because immutable
snapshots cannot be remotely erased. No process can erase copies already
exported outside the controlled deployment; operator policy must inventory and
expire backups/logs containing the value.

## Authority and retirement

Normal application credentials should have transaction/service access but no
trigger disable, raw mutation, excision, restore, or GC authority. Backup needs
read-only database access plus write access to its external destination.
Restore/excision/GC use separate operator credentials and audit every action.
Retirement means stop services/peers, take/verify or explicitly waive final
backup, revoke credentials, delete the PostgreSQL database/storage with native
tools, and apply the same retention decision to backup/WAL/log destinations.
