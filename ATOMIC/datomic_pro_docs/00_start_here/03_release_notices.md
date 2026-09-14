---
title: "Release Notices"
site: "Datomic"
source: "https://docs.datomic.com/release-notices.html"
word_count: 2603
---

This document is a list of critical release notices that all Datomic users should be aware of. Some pertain to specific versions of Datomic. Others refer to changes occurring on specific dates.

This document **is not** a comprehensive change log. Changes to Datomic can be found in `CHANGES.md` in the root of the Datomic distribution.

Releases with four version number components are bugfix-only releases based on their three-component parents, e.g. 0.8.4020.24 is a bugfix release for 0.8.4020.

## 2026-05-13 Java 11 Will Be No Longer Supported

Datomic releases after May 30th, 2026 will no longer support running on Java 11 and will require running on Java 17 or later.

See: [https://forum.datomic.com/t/release-notice-datomic-pro-after-may-30th-2026-releases-of-datomic-pro-will-require-running-on-java-17-or-later/2624](https://forum.datomic.com/t/release-notice-datomic-pro-after-may-30th-2026-releases-of-datomic-pro-will-require-running-on-java-17-or-later/2624)

## 2025-10-23 Excision Repair Tool

Datomic [Release 1.0.7469](https://docs.datomic.com/changes/pro.html#1.0.7469) fixes a bug that prevented excisions from removing datoms from the [as-of](https://docs.datomic.com/clojure/index.html#datomic.api/as-of) or [history](https://docs.datomic.com/clojure/index.html#datomic.api/history) indexes. This release also includes a tool to detect incomplete excisions to the index and optionally re-apply them. This bug only affects databases that contain excision datoms (`:db/excise`). This bug does not affect ordinary database values (those without [as-of](https://docs.datomic.com/clojure/index.html#datomic.api/as-of) or [history](https://docs.datomic.com/clojure/index.html#datomic.api/history)), nor the transaction log.

The tool works in both report and repair modes while the transactor runs. The tool needs read permissions to storage, like a peer. Repair mode requires write permissions.

### Report Mode

Report mode finds datoms that should have been excised by reading all history database indexes. Scanning large databases takes significant time.

```
bin/run -Xmx${MEM} -m datomic.tools.excise-history report ${DB_URI} ${REPORT_FILE_NAME}
```

The tool prints a summary map that includes an `:ok` entry. When `:ok` is `true`, the bug did not affect the database, and repair is not needed. When `:ok` is `false`, the database’s indexes contain datoms that the original excision did not remove. Run the tool in repair mode to fully apply excisions. The tool writes complete results, including all affected datoms, to the report file.

> **Note:** the `$REPORT_FILE_NAME` will contain the identified unexcised datoms for your review and which index they remain in. After running the tool you may want to delete the file for compliance reasons.

### Repair Mode

- The Datomic team recommends taking a backup prior to any repair.
- Ensure that the transactor runs at least version [1.0.7469](https://docs.datomic.com/changes/pro.html#1.0.7469).

Repair mode will find and also remove datoms that the original excision did not remove. Like excision, repair is irreversible.

```
bin/run -Xmx${MEM} -m datomic.tools.excise-history repair ${DB_URI} ${REPORT_FILE_NAME} ${MAX_ATTEMPTS}
```

When a transactor is running, its indexing process may try to update the index at the same time as the repair process. If the transactor encounters an index the repair tool generated, it may self-terminate. If the repair process encounters an index that the transactor created, then repair will retry up to `$MAX_ATTEMPTS` (default 3). If the repair does not complete, the transactor may be writing new indexes more frequently than the repair tool can update them. In this case, consider reducing transactor index frequency by raising [memory-index-threshold](https://docs.datomic.com/operation/capacity.html), slowing the rate of transactions into the system, or by not running a transactor during repair (`dev` protocol databases require a running transactor). The minimum value for `$MEM` should be `1g` and all settings for `datomic.readConcurrency` and `datomic.writeConcurrency` are applicable.

## 2024-12-16 Updating to Clojure 1.11.4

Datomic 1.0.7277 will be the last feature release that runs on Clojure 1.9. Starting with the following release, new Datomic feature releases will require Clojure 1.11.4.

Clojure applications that use the Datomic Peer library will have to upgrade to Clojure 1.11.4. Users of Datomic from other languages will not be affected.

Until then, you can use maven/leiningen dependency settings, on a peer-by-peer basis, to adopt Clojure 1.11.4 sooner.

Deps.edn:

```
{:deps {org.clojure/clojure {:mvn/version "1.11.4"}
        com.datomic/peer {:mvn/version "1.0.7277"
                          :exclusions [org.clojure/clojure]}}}
```

Leiningen dependencies:

```
[org.clojure/clojure "1.11.4"]
[com.datomic/peer "1.0.7277"
 :exclusions [org.clojure/clojure]]
```

Maven dependencies:

```
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>clojure</artifactId>
  <version>1.11.4</version>
</dependency>
<dependency>
  <groupId>com.datomic</groupId>
  <artifactId>peer</artifactId>
  <version>1.0.7277</version>
  <exclusions>
    <exclusion>
      <groupId>org.clojure</groupId>
      <artifactId>clojure</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

## 2023-19-10 1.0.7021 Critical Release Fixes for Transactor and Peer API

- Fixed regression introduced in 1.0.7010 where query thread pool could deadlock under highly concurrent load, preventing queries from completing.
- Fixed a regression introduced in transactors in 1.0.7010 Peer tx-range can temporarily fail to return a durably-logged transaction coincident with the start of an indexing job until a few more transactions are processed.

Moving to this release is strongly advised for both peers and transactors.

## 2023-03-03 Java 8 Will Be No Longer Supported

Datomic releases after March 31st, 2023 will no longer support running on Java 8. This is following the announcement last year Oracle has ended public updates for Java 8 for Commercial use.

See: [https://forum.datomic.com/t/release-notice-datomic-on-prem-after-march-31st-2023-on-prem-releases-will-no-longer-support-running-on-java-8/2193](https://forum.datomic.com/t/release-notice-datomic-on-prem-after-march-31st-2023-on-prem-releases-will-no-longer-support-running-on-java-8/2193)

## 2020-13-02 0.9.6045 Bug Fix for Datoms

Release 0.9.6045 fixes regression introduced in 0.9.6014 where `datoms`, `seek-datoms`, and `index-range` return incorrect results if `Iterable.iterator` is called more than once on the returned value.

Moving to this release is strongly advised.

## 2018-28-03 0.9.5697 Important Security Update

Release 0.9.5697 fixes two security vulnerabilities in Datomic On-Prem transactors running the free: or dev: storage protocol. Users of dev: and free: are encouraged to upgrade as quickly as possible.

See [https://forum.datomic.com/t/important-security-update-0-9-5697/379](https://forum.datomic.com/t/important-security-update-0-9-5697/379).

## 2017-06-06 0.9.5561.50 Bug Fix for Catalog

Release 0.9.5561.50 fixes a bug in the catalog that, in the unlikely circumstance where one has deleted a database and restored it from a backup without first having called *gc-deleted-dbs*, can cause a subsequent *gc-deleted-dbs* to delete that (active) database.

Moving to this release is strongly advised.

## 2016-28-11 0.9.5530 Unlimited Peers and Clients

As of this release all Datomic licenses are valid for Unlimited Peers and/or Clients.

## 2016-28-09 0.9.5404 ActiveMQ Artemis 1.4.0

Peers from this release forward **cannot** connect to older transactors. Attempting to connect a peer from this release (or newer) to an older transactor will result in the following exception:

```
ActiveMQConnectionTimedOutException AMQ119013: Timed out waiting to receive cluster topology. Group:null  org.apache.activemq.artemis.core.client.impl.ServerLocatorImpl.createSessionFactory (ServerLocatorImpl.java:816)
```

When upgrading to (or past) this release, you must upgrade transactors prior to upgrading peers.

## 2016-29-06 0.9.5385 HornetQ 2.4.7

Peers from this release forward **cannot** connect to older transactors. Attempting to connect a peer from this release (or newer) to an older transactor will result in the following exception:

```
HornetQConnectionTimedOutException HQ119013: Timed out waiting to receive cluster topology. Group:null  org.hornetq.core.client.impl.ServerLocatorImpl.createSessionFactory (ServerLocatorImpl.java:946)
```

When upgrading to (or past) this release, you must upgrade transactors prior to upgrading peers.

## 2016-31-05 0.9.5372 Clojure 1.8

The transactor, peer, and console now require Clojure 1.8 or greater

## 2015-03-12 0.9.5344 AWS SDK Change

The full Amazon AWS SDK for Java is no longer shipped with the Datomic peer library. If you are using AWS, see the section in the [storage docs](https://docs.datomic.com/overview/storage.html#provisioning-dynamo) about including the AWS SDK for Java for help with configuring your peer with this upgrade.

## 2015-09-10 0.9.5327 Bug Fix for Cassandra Storage

Release 0.9.5327 fixes a bug that could cause a transactor to terminate when deleting a database against Cassandra storage. We recommend all Cassandra users adopt this release as soon as possible

## Updating to Clojure 1.7

Datomic 0.9.5327 will be the last feature release that runs on Clojure 1.6. Starting with the following release, new Datomic feature releases will require Clojure 1.7.

Clojure applications that use the Datomic Peer library will have to upgrade to Clojure 1.7. Users of Datomic from other languages will not be affected.

Until then, you can use maven/leiningen dependency settings, on a peer-by-peer basis, to adopt Clojure 1.7 sooner.

Leiningen dependencies:

```
[org.clojure/clojure "1.7.0"]
[com.datomic/datomic-pro "0.9.5327"
 :exclusions [... org.clojure/clojure]]
```

Maven dependencies:

```
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>clojure</artifactId>
  <version>1.7.0</version>
</dependency>
<dependency>
  <groupId>com.datomic</groupId>
  <artifactId>datomic-pro</artifactId>
  <version>0.9.5327</version>
  <exclusions>
    <exclusion>
      <groupId>org.clojure</groupId>
      <artifactId>clojure</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

You can also update the Clojure version of a transactor you are running on your behalf by replacing the Clojure jar in the `lib` directory. Just like the Peer library, the Datomic Transactor currently includes Clojure 1.6 by default but is compatible with Clojure 1.7. Starting with the next feature released, the Transactor will run exclusively on Clojure 1.7.

## 2015-17-09 0.9.5302 Important Fix for All Users

Release 0.9.5302 eliminates a bug where given a particular and extremely unlikely interleaving of indexing failures and transactor successions, some transactions can be incorporated into the log but not in the indexes.

We encourage all users to update their transactors to this release.

## 2014-09-10 0.9.4956 PostgreSQL Driver Change

Release 0.9.4956 no longer ships the PostgreSQL driver in the peer library. If you are using PostgreSQL, see the section in the storage docs about JDBC drivers for help with [configuring your peers with this upgrade](https://docs.datomic.com/overview/storage.html#sql-database)

## 2014-09-09 0.9.4899 Clojure 1.6

The transactor, peer, and console now require Clojure 1.6 or greater

## 2014-21-08 0.9.4880.6

Release 0.9.4880.6 fixes a bug in HA that affects 0.9.4880.2 only. All HA users should adopt this release as soon as possible.

## 2014-11-08 0.9.4880.2

Release 0.9.4880.2 fixes a bug where some indexed retractions are not enforced. This can cause old values to reappear, or uniqueness constraints to consider retracted values. Upgrading the transactor to this release will repair the problem during the next indexing job.

All users of Datomic are encouraged to move to this release. As always, we recommend taking a backup before adopting a new release.

The first indexing job after upgrading to 0.9.4880 or later will take longer and perform more I/O than a normal indexing job.

## Updating to Clojure 1.6

Datomic will run on Clojure 1.5 until August 31, 2014. After that date, all new Datomic feature releases will run exclusively on Clojure 1.6.

After August 31, Clojure applications that use the Datomic Peer library will have to upgrade to Clojure 1.6. Users of Datomic from other languages will not be affected.

Until then, you can use maven/leiningen dependency settings, on a peer-by-peer basis, to adopt Clojure 1.6 sooner.

Leiningen dependencies:

```
[org.clojure/clojure "1.6.0"]
[com.datomic/datomic-pro "0.9.4815.10"
 :exclusions [... org.clojure/clojure]]
```

Maven dependencies:

```
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>clojure</artifactId>
  <version>1.6.0</version>
</dependency>
<dependency>
  <groupId>com.datomic</groupId>
  <artifactId>datomic-pro</artifactId>
  <version>0.9.4815.10</version>
  <exclusions>
    <exclusion>
      <groupId>org.clojure</groupId>
      <artifactId>clojure</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

You can also update the Clojure version of a transactor you are running on your behalf by replacing the Clojure jar in the `lib` directory. Just like the Peer library, the Datomic Transactor currently includes Clojure 1.5 by default but is compatible with Clojure 1.6. After August 31, the Transactor will run exclusively on Clojure 1.6.

## 2014-06-06 0.9.4815

Release 0.9.4815 now requires Java 7 or later for the transactor. Peers continue to work with Java 6 or later.

## 2014-04-16 0.9.4724

The 0.9.4724 release upgrades the backup format to improve backup capacity against large databases. Datomic now stores backups using a subdirectory structure.

0.9.4724 can restore backups taken by previous versions of Datomic. However, previous versions of Datomic will not be able to restore newer backups.

Attempting to use an older version of Datomic to restore a newer backup will result in the following stack trace:

```
java.lang.IllegalArgumentException: No matching clause: 3
    at datomic.backup$backup__GT_mem.invoke(backup.clj:301)
    at datomic.backup$read_roots.invoke(backup.clj:320)
    at datomic.backup$create_restore_job.invoke(backup.clj:392)
    at datomic.backup$restore_db.invoke(backup.clj:438)
    at datomic.backup$restore.invoke(backup.clj:533)
    at datomic.backup_cli$restore.invoke(backup_cli.clj:31)
```

## 2014-24-03: 0.9.4699 Changes to Index Format

[Adaptive indexing](https://blog.datomic.com/2014/03/datomic-adaptive-indexing.html) changes the format of Datomic indexes. Both transactors and peers must upgrade, and cannot go back to a previous release.

Users should backup existing databases before upgrading to this release.

Adaptive indexing automatically manages the memory index for you, and should generally be used with the following default settings, regardless of the shape of your transaction load.

```
memory-index-threshold=32m
memory-index-max=512m
```

If you have an older transactor properties file with different memory-index settings, you should change them to match the settings above.

Peers can read old versions of the index format and thus can be upgraded first while continuing to run existing transactors.

Transactors will automatically upgrade indexes to the new adaptive format. There is no one-time cost associated with this upgrade.

To realize the benefits of adaptive indexing, we've had to move away from synchronous updating of full-text indexes. Fulltext indexes are now updated in the background and are not guaranteed to be aware of the most recent transaction(s). You should not consider fulltext to be part of the basis of a DB, but rather an eventually consistent supplement to a DB. (In practice, fulltext indexes will typically be complete within a few moments of the most recent transaction).

Datomic versions before 0.9.4699 cannot read adaptive indexes, and will fail with the following stacktrace:

```
java.lang.NullPointerException
at com.google.common.base.Preconditions.checkNotNull(Preconditions.java:191)
at com.google.common.cache.LocalCache.getIfPresent(LocalCache.java:3988)
at com.google.common.cache.LocalCache$LocalManualCache.getIfPresent(LocalCache.java:4783)
at datomic.cache$eval2673$fn__2674.invoke(cache.clj:65)
```

## 2014-13-03: 0.9.4609 Breaks Peer/Transactor Compatibility

In version 0.9.4609, Datomic upgraded to HornetQ 2.3.17. However, HornetQ had introduced a backward compatibility breaking change between the older version Datomic used before 0.9.4609 and HornetQ 2.3.17.

This change breaks compatibility with versions before 0.9.4609, so peers and transactors must be upgraded together across this version.

## 2014-07-02: Log Format v2

Version 0.9.4532 upgrades the database log to format version 2. All peers and transactors in a system must move together to this version or later.

The transactor and peers are backward compatible with log version 1. Transactors will automatically upgrade database logs to version 2 when a database is used. The performance cost of this upgrade is negligible and upgrades can be performed against production systems.

Older versions of the peer and transactor are **not** forward compatible with log version 2, and will report the following error:

```
NullPointerException   java.util.UUID.fromString
```

Downgrading to log version 1 is possible. This release includes a command line tool that can be used to downgrade a database to log version 1:

```
bin/datomic revert-to-log-version-1 {your-database-uri}
```

## 2014-22-01: Alter Schema

Alter Schema ([https://blog.datomic.com/2014/01/schema-alteration.html](https://blog.datomic.com/2014/01/schema-alteration.html)) breaks compatibility with older versions. Once a schema alteration has been performed on a database, only connect peers and transactors running at least version 0.9.4470 to that database.

## 2013-31-07: Three Important Changes

Memory settings for the transactor must be explicitly specified in the transactor properties file. There are no default settings, so existing transactor property files that lack these settings will no longer work. The transactor properties sample files include three complete examples, representing ongoing usage, one-time imports, and constrained-memory usage during development.

Stricter validation of datoms within a single transaction, preventing duplication of the same datom and multiple assertions of cardinality-one attributes. This will cause transactions that would formerly have succeeded to fail with a validation error.

The transactor and peer now require Clojure 1.5.1 or greater.

## 2013-23-05: Important Fix for All Users

Release 0.8.3971 fixes a bug where some active log segments are marked as garbage, which can result in log corruption after a call to *gcStorage*. Moving to this release is strongly advised.

## 2013-16-05: Important Fix for HA Users

Release 0.8.3952 fixes a bug where an HA transactor pool can become stuck and no transactor can start. All HA users should adopt this release as soon as possible.

## 2013-09-04: Updating Clojure to 1.5

Datomic will run on Clojure 1.4 until June 1, 2013. After that date, all Datomic releases will run exclusively on Clojure 1.5.

After June 1, Clojure applications that use the Datomic Peer library will have to upgrade to Clojure 1.5. Users of Datomic from other languages will not be affected.

Until then, you can use maven/leiningen dependency settings, on a peer-by-peer basis, to adopt Clojure 1.5 sooner.

Leiningen dependencies:

```
[org.clojure/clojure "1.5.1"]
[com.datomic/datomic-pro "0.8.3862"
 :exclusions [... org.clojure/clojure]]
```

Maven dependencies:

```
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>clojure</artifactId>
  <version>1.5.1</version>
</dependency>
<dependency>
  <groupId>com.datomic</groupId>
  <artifactId>datomic-pro</artifactId>
  <version>0.8.3862</version>
  <exclusions>
    <exclusion>
      <groupId>org.clojure</groupId>
      <artifactId>clojure</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

The Datomic Peer currently includes Clojure 1.4 by default but is compatible with Clojure 1.5.

You can also update the Clojure version of a transactor you are running on your behalf by replacing the Clojure jar in the `lib` directory. Just like the Peer library, the Datomic Transactor currently includes Clojure 1.4 by default but is compatible with Clojure 1.5. After June 1, the Transactor will run exclusively on Clojure 1.5.

## 2013-09-01: 0.8.3705 Breaks Peer/Transactor Compatibility

In version 0.8.3705, Datomic upgraded to HornetQ 2.2.21, to get key bug fixes and enhancements to HornetQ. However, HornetQ had introduced a backward-compatibility breaking change between the older version Datomic used before 0.8.3705 and HornetQ 2.2.21.

This change breaks compatibility with versions before 0.8.3705, so peers and transactors must be upgraded together across this version.