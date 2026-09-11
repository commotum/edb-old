# Logical database lifecycle

A public name is a way to find a database, not the database's identity. Atomic keeps
the storage ID and lineage stable while its public name changes. Facts, transaction
receipts, database values and canonical manifest bytes are not rewritten by rename.
Reusing an old name creates a different database with a new storage ID and lineage.

## Provision and rename

Use the catalog-owner PostgreSQL credentials for mutations; ordinary writer and
peer roles cannot rename or delete databases. The usual explicit PostgreSQL/TLS
configuration applies. Listing and resolution use read-only privileges.

```sh
atomic create --database customers
atomic create --database customers  # EXISTS, not an error or a schema change
atomic list-databases --limit 100
atomic list-databases --after customers --limit 100
atomic rename --database customers --new-name people
```

Rename previews its source identity. Apply with the exact lineage from that output:

```sh
atomic rename --database customers --new-name people --lineage UUID-FROM-PREVIEW --apply
```

The lineage is checked atomically with the name change. A changed/reused source name
cannot silently target a different database. An occupied destination is an error;
there are no implicit historical aliases. Existing handles and the active writer
keep their captured identity. New applications connect using `people`; attempting
to open `customers` fails until that name is deliberately created again.

Listings are keyset-paginated: `--after` is exclusive, default limit 1000, maximum
4096. A full page does not prove another page exists; continue from its last name.
For `--retired`, the cursor is the last storage ID, since there is no active name.
An unfinished restore reserves a visible name even before its first readable
database value exists. Listing is not a readiness check. Create returns `EXISTS`
for that reservation; operators can retire it, and a late restore publication
must then fail instead of reviving it.

Rust applications use `DatabaseCatalog::{connect_configured,create_if_absent,
resolve,list,rename_checked,retire_checked}`. `CreateDatabaseResult` distinguishes
creation from an existing entry without recovering all data. Existing strict
`PostgresStore::create_database` remains available; its return type is unchanged.
User-facing `Connection`/`Peer` and service startup names resolve once. Low-level
storage IDs and exact snapshot-reference routes are not public-name aliases.
Do not feed `connection.identity().database_id()` back into a named connect after
rename. Keep the connection, use the current public name, or reopen an exact
snapshot reference while the identity is active.

## Retire, then reclaim

```sh
atomic delete --database people
atomic delete --database people --lineage UUID-FROM-PREVIEW --apply
atomic list-databases --retired
```

Delete removes the active name and fences its writer. New connections and exact
snapshot reopening are rejected. Already captured, pinned immutable values remain
readable: deletion is not mutation of a value your application already holds.
Retirement does not immediately erase stored facts or invalidate completed portable
backups. Release old values/connections before expecting reclamation to proceed.
These are live storage pins, not a promise of permanent availability for offline
handles. Connection/server loss can release them; choose retention accordingly.

Storage reclamation is a separate owner operation against an exact retired storage
ID and lineage. Supply the physical PostgreSQL database and catalog schema as an
additional target check. Choose a retention interval appropriate to your recovery
policy; zero is suitable for a disposable test fixture, not a default recommendation.

```sh
atomic gc-deleted --storage-id ID --lineage UUID --postgres-database atomic \
  --catalog-schema public --older-than-seconds 2592000
atomic gc-deleted --storage-id ID --lineage UUID --postgres-database atomic \
  --catalog-schema public --older-than-seconds 2592000 --apply --batches 10
```

Preview changes no database rows. Apply makes bounded, resumable progress and
reports its phase, selected/removed/inserted/updated rows, immutable objects read,
pin checks and completion. Each batch touches at most 512 data/frontier rows,
plus fixed progress bookkeeping. This is not a bound on all examined metadata,
SQL execution time or process memory. Discovery reads one immutable object at a
time; legacy segment sharing may require checking all remaining legacy manifests.
Live reader/backup/build pins can
prevent progress; that is protection, not a successful reclamation. Retry the same
identity after releasing them. Shared immutable content still referenced by another
database is retained. Issued-identity tombstones prevent historical route reuse.

Completion means the retired database's attributable metadata and exclusive
reachable objects have been collected. It does not certify erasure of every
historical physical byte: obsolete objects already detached from their owner by
earlier maintenance remain the responsibility of ordinary catalog-wide `gc`.
Neither command erases external backups, PostgreSQL backups/WAL or replicas.
Incomplete shared-tree reachability evidence causes a reported failure to progress,
not speculative deletion; repair or finish the corresponding maintenance first.

This is not excision of selected facts and not `DROP SCHEMA`. Ordinary `gc` collects
obsolete structures in a catalog; `gc-deleted` dismantles one retired database.
Completed backups remain independently verifiable. Restore retains canonical
lineage. Restoring a retired lineage into the same catalog requires finishing its
reclamation first, or choosing another catalog. After reclamation, restore allocates
a fresh storage route while retaining the backup's lineage; old exact-reference
routes remain retired. See the restore checks in [admin.md](admin.md).

## Catalog initialization

Run `atomic migrate` on a fresh PostgreSQL catalog to install the single current
schema baseline, including the database name catalog. Historical schema upgrades
are unsupported; catalogs initialized by earlier releases require a fresh catalog.
Runtime processes require the current schema version before opening a database.
