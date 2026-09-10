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

Rust applications use `DatabaseCatalog::{connect_configured,create_if_absent,
resolve,list,rename_checked,retire_checked}`. `CreateDatabaseResult` distinguishes
creation from an existing entry without recovering all data. Existing strict
`PostgresStore::create_database` remains available; its return type is unchanged.
User-facing `Connection`/`Peer` and service startup names resolve once. Low-level
storage IDs and exact snapshot-reference routes are not public-name aliases.

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

Preview does not delete. Apply makes bounded, resumable progress and reports its
phase, selected/removed rows, pin checks and completion. A batch bounds victims,
not all examined metadata or wall-clock time. Live reader/backup/build pins can
prevent progress; that is protection, not a successful reclamation. Retry the same
identity after releasing them. Shared immutable content still referenced by another
database is retained. Issued-identity tombstones prevent historical route reuse.

This is not excision of selected facts and not `DROP SCHEMA`. Ordinary `gc` collects
obsolete structures in a catalog; `gc-deleted` dismantles one retired database.
Completed backups remain independently verifiable. Restore retains canonical
lineage; see the restore instructions and checks in [admin.md](admin.md).

## Upgrade boundary

Migration 34 seeds the name catalog from existing databases one-to-one. It does not
rename their storage IDs or rewrite canonical facts, programs, receipts or manifests.
Quiesce old writers and maintenance processes before migrating, then restart all
processes on the new release. Old startups are fenced by the schema version; this
does not claim that already-running older binaries are safe during migration.
