# Query a backup directly

`BackupConnection::open(path)` opens the latest published point without a
transactor, PostgreSQL connection, restore or full replay. `open_point(path,
&point)` selects an exact `BackupPoint` from `PortableBackup::list_backup_points`.
`db()` and `log()` keep that fixed point even when later backups are published.
Retain the point's lineage, generation, basis and manifest hash when sharing it.
An online `SnapshotReference` is not a backup locator.

The values use the existing query, Pull, entity, history/time-view, local `with`,
program-resolution and log APIs. Speculation never changes the backup. Physical
AVET readiness remains a property of the captured value. Fulltext uses the
existing local scan implementation when a native search accelerator is absent;
do not assume indexed search costs for that path.

The stock executable accepts the same EDN commands without PostgreSQL credentials:

```sh
atomic query --repository /private/backups/customers --file query.edn
atomic pull --repository /private/backups/customers --file pattern.edn --entity 12345
atomic with --repository /private/backups/customers --file transaction.edn
```

Use `--backup-basis N --backup-generation N` together to select a listed point.
`--database` and `--repository` are mutually exclusive. File `-` reads stdin;
results remain EDN. Named query sources also accept
`{:repository "/private/backups/customers" :basis 42 :generation 0 :log true}`;
omit `:log` for a database source, or both coordinates for the latest point.

## Cost and integrity

Backup envelope5 includes an exact immutable read index and a sparse transaction
lookup, published before the backup root. Capture reuses a complete native index
when available. If indexing lags, capture performs a streaming full-index pass:
completed encoded nodes are written promptly, with active leaf/boundary data and
routing references retained. This can make capture more expensive; it does not
move a hidden full restore into the reader. Log lookup updates reuse old tree
paths. Nothing changes PostgreSQL canonical data.

Opening reads root and schema/ident metadata; data leaves and log payloads are
loaded on demand and authenticated against their immutable references. Missing
or corrupt required files return errors, and failed cursors fuse. A previously
authenticated cached value remains valid. Direct reading is not a substitute
for deep semantic backup verification.

`BackupReadConfig` bounds decoded-node cache entries/bytes, including zero to
disable it. Snapshot roots/schema/idents, active cursors and caller-retained
results are separate memory. `read_stats()` reports immutable-object bytes/read
counts, including object admission; repository directory/claim/locator metadata
and OS block traffic are not included. Complete-path wall time includes them.
`cache_stats()` reports retained cache bytes, hits and evictions.

At64 and8192 entities, the optimized PostgreSQL-created fixture read16 immutable
objects to open, then2 more for a three-result query; examined datoms were4 at
both sizes. Object bytes through that query were39,063 and571,060, and cache
residency126,958 and1,985,947 bytes. Open+two queries+consume/drop took2.14 and
11.19ms on the test host. This is selective access, not a constant-memory or
constant-latency claim; default leaves grow up to their normal limits.

These development backups are not a cross-version compatibility promise. Create
a current-version backup from a supported database; unsupported versions fail
explicitly. No tool silently resets a database. Current-version durability,
restart, exact retries and backup/restore remain required. Backup repositories
are private operator-owned directories. Excision does not erase independent
older backups or copies; retention and deletion remain explicit operations.
