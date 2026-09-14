---
title: "Backup and Restore"
site: "Datomic"
source: "https://docs.datomic.com/operation/backup.html"
description: "Learn how to effectively backup your Datomic database."
word_count: 1098
---

[Backup](#backing-up) copies a Datomic database to a file system directory or to an [AWS S3 Bucket](https://aws.amazon.com/s3/) which you have appropriate [permissions](#aws-permissions).

[Restore](#restoring) copies a Datomic database backup to a Datomic system.

Backup and restore are useful for:

- Disaster recovery
- Moving databases from one storage to another

Backups can be performed at any time against a live system and repeat backups to the same backup-uri take advantage of [differential backup](#differential-backup).

## Backing Up

You can use the `backup-db` command to back up a database at a database-uri to storage at a [backup URI](#uri-syntax):

```
bin/datomic -Xmx4g -Xms4g backup-db from-db-uri to-backup-uri
```

The backup process needs memory proportional to database size.

Give the backup process at least as much memory as [the transactor process is given](https://docs.datomic.com/operation/capacity.html#transactor-memory) for a system, or approximately 500MB per billion datoms, whichever value is higher.

Backup URIs are per database. You can backup the same database at different points in time to a single backup URI.

A backup from a database URI and [restore](#restoring) to that database URI should not occur at the same time.

> You can **not** back up different databases to a single backup-uri.
> 
> Attempting to backup different databases to the same backup URI will result in an error:
> 
> - `java.lang.IllegalArgumentException: :backup/claim-failed Backup storage already used by...`

Backups to s3 can be encrypted with [S3 server-side encryption](https://docs.aws.amazon.com/AmazonS3/latest/userguide/serv-side-encryption.html). By default, ****backups are not stored encrypted****. To enable encryption, pass the `--encryption sse` flag:

```
bin/datomic -Xmx4g -Xms4g backup-db --encryption sse from-db-uri to-backup-uri
```

### Differential Backup

The first time you perform a backup it must backup the entire database.

Repeat backups to the same backup-uri do not need to copy unchanged segments. Repeatedly backing up the same database to the same backup-uri will substantially reduce backup times. This approach is strongly recommended, especially for large databases.

## Listing Backups

You can use the `list-backups` command to list the available points in time t that can be restored:

```
bin/datomic list-backups backup-uri
```

## Restoring

You can use the `restore-db` command to restore a database.

To restore a database you must ****first**** do the following:

- Shut down all peers and:
	- `:dev` storage - the transactor must be running
		- All other storages - shut down transactors
- (*Optional*) Have a valid `t` from the backup, obtained with the [`list-backups` command](#listing-backups)

> There should be no process actively backing up to a backup-uri when restoring from that database URI.

```
bin/datomic -Xmx4g -Xms4g restore-db from-backup-uri to-db-uri
```

You should give the restore process as much memory as you [give the transactor process](https://docs.datomic.com/operation/capacity.html#transactor-memory) for that system.

By default, the most recent backup will be restored. You can restore to any `t` output by [list backups](#listing-backups) by putting that t at the end of the command:

```
bin/datomic -Xmx4g -Xms4g restore-db from-backup-uri to-db-uri t
```

You can restore into a database URI that already points to a different point-in-time for the same database. You cannot restore into a database URI that points to a different database.

Restore can rename databases. However, you cannot restore a single database to two different database URIs within the same storage.

It is not necessary to do anything to storage that's been restored to (e.g. deleting old files or tables) before or after restoring. However, restoring to an "empty" storage will use the least possible storage space.

Restart peers and transactors after a restore. The instructions below describe how to perform these steps, depending on which storage you are restoring to.

### Restoring to:dev Storage

`:dev` storage requires a running transactor during restore, because storage resides inside the transactor process. Start the transactor before running a restore, and then shutdown and restart the transactor after the restore completes.

### Restoring to All Other Storage

With all system processes down, run the restore.

When the restore has been completed successfully, start the transactor and peers.

## Verifying Backups

You can use the `verify-backup` command to verify that a backup includes all segments and (optionally) that all segments are readable.

The positional arguments to `verify-backup` are a backup-uri, a boolean for `read-all` and the backup t you wish to verify.

```
bin/datomic -Xmx4g -Xms4g verify-backup backup-uri read-all t
```

Note that:

- If `read-all` is `false`, `verify-backup` will verify that all segments of the log and index trees are present in backup storage. This basic verification is optimized for speed and minimal use of I/O.
- If `read-all` is `true`, `verify-backup` will additionally read back every segment of the database. This deeper verification has I/O cost is proportional to the size of the entire database and is suitable as a check e.g. bit-level corruption.

## Reading from Backups

The datomic:backup: protocol accepts any backup location described in the [URI section](#uri-syntax). You can use list-backups to get the `datomic:backup:` protocol uri and a list of all the \`t\`'s it contains. These URI represent connectable db values and can be used for any read only workflow. For more information see our documentation on [read only connections](https://docs.datomic.com/operation/read-only.html)

## Backup, Restore, and Verify Performance

Backup, restore, and verify performance can be configured through [system properties](https://docs.datomic.com/operation/system-properties.html#backup-properties).

## Deleting Backups

Backup space can be reclaimed by deleting the entire content hierarchy at a particular backup-uri's location.

This will reclaim all space, and delete all the different point-in-time backups associated with that URI.

Deleting a single point in time within a backup-uri is not supported.

## Backup URI Syntax

backup-uris have the following syntax:

| Destination | Syntax |
| --- | --- |
| Directory | `file:/full/path/to/backup-directory` |
| S3 | `s3://bucket/prefix` |

Database URI syntax is described in the API docs for `connect`:

- Java - [Peer.connect()](https://docs.datomic.com/javadoc/datomic/Peer.html#connect-java.lang.Object-)
- Clojure - [datomic.api/connect](https://docs.datomic.com/clojure/index.html#datomic.api/connect)

## AWS Permissions

When backing up from DDB, the required AWS permissions are the same as those a Peer would use, i.e. storage read-only. However, when restoring to DDB, the AWS permissions should be those used by the transactor, i.e. read/write credentials. Check [AWS access control](https://docs.datomic.com/operation/aws-access-control.html) for information on providing the backup/restore processes access to the necessary resources.

### IAM Role Policies

Peers performing backups will need to be able to write to nested keys within the backup S3 bucket. Peers performing restore will need to read from the backup S3 bucket. The following role policy would grant both permissions:

```
{"Statement":
 [{"Effect":"Allow",
   "Action":["s3:*"],
   "Resource":
   ["arn:aws:s3:::{bucket-name}", "arn:aws:s3:::{bucket-name}/*"]}]}
```

Additionally, peers performing restore to a DDB table will need to be able to both read from and write to that table:

```
{"Statement":
 [{"Effect":"Allow",
   "Action":["dynamodb:*"],
   "Resource":"arn:aws:dynamodb:*:{account-id}:table/{table-name}"}]}
```

## Limitations

Backup and restore are not suitable for cloning a database within a single storage. If you attempt to restore a database to a storage that already contains that database, but under a different name, the restore operation will fail with the message:

Backup and restore operations require durable storage and will not work with the `:mem` database.